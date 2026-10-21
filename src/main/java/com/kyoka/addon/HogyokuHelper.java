package com.kyoka.addon;

import bleachawaken.deephantom.com.BleachawakenModVariables;
import net.minecraft.entity.player.PlayerEntity;

public final class HogyokuHelper {
    public static final double USE_COST = 1200.0;
    public static final double REGEN_COST = 100.0;
    public static final int REGEN_COOLDOWN = 500;
    private static final String STAGE_KEY = "kyoka_hogyoku_stage";
    private static final String COOLDOWN_KEY = "kyoka_hogyoku_cd";

    private HogyokuHelper() {}

    public static int stage(PlayerEntity player) {
        return player.getPersistentData().getInt(STAGE_KEY);
    }

    public static void setStage(PlayerEntity player, int stage) {
        player.getPersistentData().putInt(STAGE_KEY, stage);
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
