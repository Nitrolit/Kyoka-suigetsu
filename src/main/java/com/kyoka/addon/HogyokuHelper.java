package com.kyoka.addon;

import bleachawaken.deephantom.com.BleachawakenModVariables;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;

import java.util.UUID;

public final class HogyokuHelper {
    public static final double USE_COST = 1200.0;
    public static final double REGEN_COST = 100.0;
    public static final int REGEN_COOLDOWN = 500;
    public static final int HITS_PER_STEP = 10;
    public static final double GROWTH_STEP = 0.01;
    public static final double GROWTH_CAP = 0.20;

    private static final String STAGE_KEY = "kyoka_hogyoku_stage";
    private static final String COOLDOWN_KEY = "kyoka_hogyoku_cd";
    private static final String HITS_KEY = "kyoka_hogyoku_hits";
    private static final UUID SPEED_ID = UUID.fromString("6b1f2c3a-1111-4a2b-9c3d-000000000001");

    private HogyokuHelper() {}

    public static int stage(PlayerEntity player) {
        return player.getPersistentData().getInt(STAGE_KEY);
    }

    public static void setStage(PlayerEntity player, int stage) {
        player.getPersistentData().putInt(STAGE_KEY, stage);
    }

    public static int hits(PlayerEntity player) {
        return player.getPersistentData().getInt(HITS_KEY);
    }

    public static void addHit(PlayerEntity player) {
        player.getPersistentData().putInt(HITS_KEY, hits(player) + 1);
    }

    // +1% per 10 received hits, capped at 20% (reached at 200 hits).
    public static double growth(PlayerEntity player) {
        double steps = hits(player) / HITS_PER_STEP;
        return Math.min(GROWTH_CAP, steps * GROWTH_STEP);
    }

    // Bonuses are active only while the owner has the stage set.
    public static double activeGrowth(PlayerEntity player) {
        return stage(player) >= 1 ? growth(player) : 0.0;
    }

    public static void applySpeed(PlayerEntity player) {
        ModifiableAttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;
        speed.removeModifier(SPEED_ID);
        double g = activeGrowth(player);
        if (g > 0) {
            speed.addTransientModifier(new AttributeModifier(SPEED_ID, "kyoka_hogyoku_speed", g,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    // Stage 1: heal to 65% when HP is at 40% or below, cost 100 reiatsu, cooldown 500 ticks.
    public static void tryRegen(PlayerEntity player) {
        if (stage(player) < 1) return;
        long now = player.level.getGameTime();
        if (player.getPersistentData().getLong(COOLDOWN_KEY) > now) return;
        if (player.getHealth() > player.getMaxHealth() * 0.40f) return;

        BleachawakenModVariables.PlayerVariables v = ReiatsuHelper.vars(player);
        if (v == null || v.reiatsu_Bar < REGEN_COST) return;

        ReiatsuHelper.spend(player, REGEN_COST);
        player.setHealth(player.getMaxHealth() * 0.65f);
        player.getPersistentData().putLong(COOLDOWN_KEY, now + REGEN_COOLDOWN);
    }
}
