package com.kyoka.addon;

import bleachawaken.deephantom.com.BleachawakenModVariables;
import net.minecraft.entity.player.PlayerEntity;

public final class ReiatsuHelper {
    public static final double PASSIVE_COST = 10.0;
    public static final double MIN_FRACTION = 0.30;

    private ReiatsuHelper() {}

    public static BleachawakenModVariables.PlayerVariables vars(PlayerEntity player) {
        return player.getCapability(BleachawakenModVariables.PLAYER_VARIABLES_CAPABILITY).orElse(null);
    }

    public static boolean hasEnoughReiatsu(PlayerEntity player) {
        BleachawakenModVariables.PlayerVariables v = vars(player);
        if (v == null || v.reiatsu_CurrentMax <= 0) return false;
        return v.reiatsu_Bar / v.reiatsu_CurrentMax >= MIN_FRACTION;
    }

    public static void spend(PlayerEntity player, double amount) {
        BleachawakenModVariables.PlayerVariables v = vars(player);
        if (v == null) return;
        v.reiatsu_Bar = Math.max(0, v.reiatsu_Bar - amount);
        BleachawakenModVariables.syncPlayerVariables(player);
    }
}
