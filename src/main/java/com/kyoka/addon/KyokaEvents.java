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
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class KyokaEvents {
    private static final double RANGE = 4.0;
    private static final float ABILITY_CHANCE = 0.10f;
    private static final int ABILITY_INTERVAL = 20;
    private static final int MAX_BLOCKS = 10;
    private static final int BREAK_TICKS = 200;
    private static final Random RANDOM = new Random();
    private static final Map<UUID, State> STATES = new HashMap<>();
    private static boolean reflecting = false;

    public static class State {
        public LivingEntity lastAttacker;
        public int hits;
        public long brokenUntil;
    }

    public static State state(PlayerEntity player) {
        return STATES.computeIfAbsent(player.getUUID(), k -> new State());
    }

    public static boolean isReleasedHeld(LivingEntity entity) {
        return entity instanceof PlayerEntity
                && ((PlayerEntity) entity).getMainHandItem().getItem() == ModItems.KYOKA_RELEASED.get();
    }

    // Passive works only while the sword is released AND reiatsu is at least 30%.
    public static boolean isPassiveActive(LivingEntity entity) {
        return isReleasedHeld(entity) && ReiatsuHelper.hasEnoughReiatsu((PlayerEntity) entity);
    }

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
        if (reflecting || !(event.getEntityLiving() instanceof PlayerEntity)) return;
        PlayerEntity victim = (PlayerEntity) event.getEntityLiving();
        if (!isReleasedHeld(victim)) return;

        State st = state(victim);
        Entity attacker = event.getSource().getEntity();
        boolean hasAttacker = attacker instanceof LivingEntity && attacker != victim;
        if (hasAttacker) st.lastAttacker = (LivingEntity) attacker;

        if (!ReiatsuHelper.hasEnoughReiatsu(victim)) return;

        long now = victim.level.getGameTime();
        if (now < st.brokenUntil) return;

        st.hits++;
        if (st.hits <= MAX_BLOCKS) {
            event.setCanceled(true);
            // Reflection applies only to subordinates.
            if (hasAttacker && SubordinationHelper.isSubordinate(victim, (LivingEntity) attacker)) {
                reflecting = true;
                try {
                    ((LivingEntity) attacker).hurt(DamageSource.thorns(victim), event.getAmount() / 2.0f);
                } finally {
                    reflecting = false;
                }
            }
            return;
        }

        // 11th hit: protection breaks, owner takes 3x damage, protection is off for 200 ticks.
        st.hits = 0;
        st.brokenUntil = now + BREAK_TICKS;
        event.setCanceled(true);
        reflecting = true;
        try {
            victim.hurt(event.getSource(), event.getAmount() * 3.0f);
        } finally {
            reflecting = false;
        }
    }

    // Dead entities are removed from every owner's list.
    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntityLiving();
        if (dead.level instanceof ServerWorld) {
            SubordinationHelper.data((ServerWorld) dead.level).removeEverywhere(dead.getUUID());
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
                    m -> m.isAlive() && m.getTarget() == owner && SubordinationHelper.isSubordinate(owner, m));

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
