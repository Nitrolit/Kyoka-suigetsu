package com.kyoka.addon;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class KyokaEvents {
    private static final double RANGE = 4.0;
    private static final float ABILITY_CHANCE = 0.10f;
    private static final int ABILITY_INTERVAL = 20;
    private static final Random RANDOM = new Random();
    private static boolean reflecting = false;

    public static boolean isReleasedHeld(LivingEntity entity) {
        return entity instanceof PlayerEntity
                && ((PlayerEntity) entity).getMainHandItem().getItem() == ModItems.KYOKA_RELEASED.get();
    }

    // Passive works only while the sword is released AND reiatsu is at least 30%.
    public static boolean isPassiveActive(LivingEntity entity) {
        return isReleasedHeld(entity) && ReiatsuHelper.hasEnoughReiatsu((PlayerEntity) entity);
    }

    // Passive drain: 10 reiatsu every 100 ticks while the passive is active.
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide) return;
        PlayerEntity player = event.player;
        if (player.tickCount % 100 != 0) return;
        if (isPassiveActive(player)) {
            ReiatsuHelper.spend(player, ReiatsuHelper.PASSIVE_COST);
        }
    }

    @SubscribeEvent
    public void onAttack(LivingAttackEvent event) {
        LivingEntity victim = event.getEntityLiving();
        if (!isPassiveActive(victim) || reflecting) return;

        event.setCanceled(true);

        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof LivingEntity && attacker != victim) {
            reflecting = true;
            try {
                ((LivingEntity) attacker).hurt(DamageSource.thorns(victim), event.getAmount() / 2.0f);
            } finally {
                reflecting = false;
            }
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.world instanceof ServerWorld)) return;
        ServerWorld world = (ServerWorld) event.world;
        if (world.getGameTime() % ABILITY_INTERVAL != 0) return;

        for (PlayerEntity owner : world.players()) {
            if (!isPassiveActive(owner)) continue;

            List<MobEntity> mobs = world.getEntitiesOfClass(MobEntity.class,
                    owner.getBoundingBox().inflate(RANGE),
                    m -> m.isAlive() && m.getTarget() == owner);

            for (MobEntity mob : mobs) {
                if (RANDOM.nextFloat() >= ABILITY_CHANCE) continue;

                List<LivingEntity> others = world.getEntitiesOfClass(LivingEntity.class,
                        mob.getBoundingBox().inflate(RANGE),
                        e -> e.isAlive() && e != mob && e != owner);

                float damage = (float) mob.getAttributeValue(Attributes.ATTACK_DAMAGE);
                if (!others.isEmpty()) {
                    LivingEntity target = others.stream()
                            .min(Comparator.comparingDouble(e -> e.distanceToSqr(mob)))
                            .get();
                    target.hurt(DamageSource.mobAttack(mob), damage);
                } else {
                    mob.hurt(DamageSource.thorns(owner), damage);
                    mob.setTarget(null);
                    mob.yRot += 180.0f;
                    mob.yHeadRot = mob.yRot;
                    mob.yBodyRot = mob.yRot;
                }
            }
        }
    }
}
