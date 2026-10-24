package com.kyoka.addon;

import bleachawaken.deephantom.com.BleachawakenModVariables;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class KyokaSkills {
    public static final double CRYSTAL_RADIUS = 25.0;
    public static final int CRYSTAL_TICKS = 400;
    public static final int CRYSTAL_COOLDOWN = 400;
    public static final double ZONE_RADIUS = 6.0;
    public static final double ZONE_COST = 5.0;        // reiatsu per ZONE_PERIOD ticks
    public static final float ZONE_DAMAGE = 0.5f;      // damage per ZONE_PERIOD ticks
    public static final int ZONE_PERIOD = 3;           // ticks

    private static final String CRYSTAL_CD = "kyoka_crystal_cd";
    private static final String CRYSTAL_UNTIL = "kyoka_crystal_until";
    private static final String ZONE_ON = "kyoka_zone_on";
    private static final Map<UUID, Set<UUID>> FROZEN = new HashMap<>();
    private static final Random RANDOM = new Random();

    public static boolean isCrystal(PlayerEntity p) {
        return p.level.getGameTime() < p.getPersistentData().getLong(CRYSTAL_UNTIL);
    }

    public static boolean holdingKyoka(PlayerEntity p) {
        Item item = p.getMainHandItem().getItem();
        return item == ModItems.KYOKA_SEALED.get() || item == ModItems.KYOKA_RELEASED.get();
    }

    public static void activateCrystal(PlayerEntity player) {
        if (player.level.isClientSide || !holdingKyoka(player)) return;
        long now = player.level.getGameTime();
        if (player.getPersistentData().getLong(CRYSTAL_CD) > now) return;
        player.getPersistentData().putLong(CRYSTAL_UNTIL, now + CRYSTAL_TICKS);
        player.getPersistentData().putLong(CRYSTAL_CD, now + CRYSTAL_COOLDOWN);
        FROZEN.put(player.getUUID(), new HashSet<>());
    }

    public static void toggleZone(PlayerEntity player) {
        if (player.level.isClientSide || !holdingKyoka(player)) return;
        if (player.getPersistentData().getBoolean(ZONE_ON)) {
            player.getPersistentData().putBoolean(ZONE_ON, false);
            player.sendMessage(new StringTextComponent("Зона выключена"), player.getUUID());
            return;
        }
        BleachawakenModVariables.PlayerVariables v = ReiatsuHelper.vars(player);
        if (v == null || v.reiatsu_Bar < ZONE_COST) return;
        player.getPersistentData().putBoolean(ZONE_ON, true);
        player.sendMessage(new StringTextComponent("Зона включена"), player.getUUID());
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide) return;
        PlayerEntity player = event.player;
        ServerWorld world = (ServerWorld) player.level;

        // Crystallization: freeze mobs in range while active, release them when it ends.
        Set<UUID> frozen = FROZEN.get(player.getUUID());
        if (frozen != null) {
            if (isCrystal(player)) {
                AxisAlignedBB box = player.getBoundingBox().inflate(CRYSTAL_RADIUS);
                List<LivingEntity> near = world.getEntitiesOfClass(LivingEntity.class, box,
                        e -> e != player && e.isAlive());
                for (LivingEntity e : near) {
                    e.setDeltaMovement(Vector3d.ZERO);
                    if (e instanceof MobEntity) ((MobEntity) e).setNoAi(true);
                    frozen.add(e.getUUID());
                }
            } else {
                for (UUID id : frozen) {
                    Entity e = world.getEntity(id);
                    if (e instanceof MobEntity) ((MobEntity) e).setNoAi(false);
                }
                FROZEN.remove(player.getUUID());
            }
        }

        // Zone: every 3 ticks costs 5 reiatsu, deals 0.5 damage and slows/weakens enemies.
        if (player.tickCount % ZONE_PERIOD == 0 && player.getPersistentData().getBoolean(ZONE_ON)) {
            BleachawakenModVariables.PlayerVariables v = ReiatsuHelper.vars(player);
            if (!holdingKyoka(player) || v == null || v.reiatsu_Bar < ZONE_COST) {
                player.getPersistentData().putBoolean(ZONE_ON, false);
                player.sendMessage(new StringTextComponent("Зона выключена"), player.getUUID());
                return;
            }
            ReiatsuHelper.spend(player, ZONE_COST);

            AxisAlignedBB box = player.getBoundingBox().inflate(ZONE_RADIUS);
            for (LivingEntity e : world.getEntitiesOfClass(LivingEntity.class, box,
                    x -> x != player && x.isAlive())) {
                e.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 40, 1));
                e.addEffect(new EffectInstance(Effects.WEAKNESS, 40, 0));
                e.hurt(DamageSource.playerAttack(player), ZONE_DAMAGE);
            }

            // Grey "rain": smoke particles falling inside the zone.
            for (int i = 0; i < 12; i++) {
                double x = player.getX() + (RANDOM.nextDouble() * 2 - 1) * ZONE_RADIUS;
                double z = player.getZ() + (RANDOM.nextDouble() * 2 - 1) * ZONE_RADIUS;
                double y = player.getY() + 2.0 + RANDOM.nextDouble() * 3.0;
                world.sendParticles(ParticleTypes.SMOKE, x, y, z, 1, 0, -0.3, 0, 0.0);
            }
        }
    }
}
