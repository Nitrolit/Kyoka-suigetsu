package com.kyoka.addon;

import bleachawaken.deephantom.com.BleachawakenModVariables;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.UUID;

public final class HogyokuHelper {
    public static final double USE_COST = 1200.0;
    public static final double REGEN_COST = 100.0;
    public static final int REGEN_COOLDOWN = 500;
    public static final int HITS_PER_STEP = 10;
    public static final double GROWTH_STEP = 0.01;
    public static final double GROWTH_CAP = 0.20;
    public static final int MAX_HITS = 200;
    public static final int FALLS_NEEDED = 10;
    public static final int BLOCKS_NEEDED = 10;
    public static final int MAX_STAGE = 4;

    private static final String STAGE_KEY = "kyoka_hogyoku_stage";
    private static final String COOLDOWN_KEY = "kyoka_hogyoku_cd";
    private static final String HITS_KEY = "kyoka_hogyoku_hits";
    private static final String FALLS_KEY = "kyoka_hogyoku_falls";
    private static final String BLOCKS_KEY = "kyoka_hogyoku_blocks";
    private static final UUID SPEED_ID = UUID.fromString("6b1f2c3a-1111-4a2b-9c3d-000000000001");

    private HogyokuHelper() {}

    public static int stage(PlayerEntity player) {
        return player.getPersistentData().getInt(STAGE_KEY);
    }

    // Setting a stage resets the stage counters (falls and blocks).
    public static void setStage(PlayerEntity player, int stage) {
        player.getPersistentData().putInt(STAGE_KEY, stage);
        player.getPersistentData().putInt(FALLS_KEY, 0);
        player.getPersistentData().putInt(BLOCKS_KEY, 0);
        sync(player);
    }

    public static int hits(PlayerEntity player) {
        return player.getPersistentData().getInt(HITS_KEY);
    }

    public static int falls(PlayerEntity player) {
        return player.getPersistentData().getInt(FALLS_KEY);
    }

    public static int blocks(PlayerEntity player) {
        return player.getPersistentData().getInt(BLOCKS_KEY);
    }

    // Stage 1: 200 dealt and received hits move to stage 2.
    public static void addHit(PlayerEntity player) {
        int h = hits(player);
        if (h < MAX_HITS) {
            player.getPersistentData().putInt(HITS_KEY, h + 1);
        }
        if (stage(player) == 1 && hits(player) >= MAX_HITS) {
            setStage(player, 2);
            return;
        }
        sync(player);
    }

    // Stage 2: 10 fall damage events move to stage 3.
    // Stage 3: 10 fall damage events and 10 blocks move to stage 4.
    public static void addFall(PlayerEntity player) {
        int s = stage(player);
        if (s < 2 || s >= MAX_STAGE) return;
        int f = falls(player) + 1;
        player.getPersistentData().putInt(FALLS_KEY, f);
        if (s == 2 && f >= FALLS_NEEDED) {
            setStage(player, 3);
        } else if (s == 3 && f >= FALLS_NEEDED && blocks(player) >= BLOCKS_NEEDED) {
            setStage(player, 4);
        } else {
            sync(player);
        }
    }

    // Blocks count only in stage 3 (for the move to stage 4).
    public static void addBlock(PlayerEntity player) {
        if (stage(player) != 3) return;
        int b = blocks(player) + 1;
        player.getPersistentData().putInt(BLOCKS_KEY, b);
        if (falls(player) >= FALLS_NEEDED && b >= BLOCKS_NEEDED) {
            setStage(player, 4);
        } else {
            sync(player);
        }
    }

    // +1% per 10 hits, capped at 20% (reached at 200 hits).
    public static double growthFromHits(int hits) {
        return Math.min(GROWTH_CAP, (hits / HITS_PER_STEP) * GROWTH_STEP);
    }

    public static double growth(PlayerEntity player) {
        return growthFromHits(hits(player));
    }

    // Bonuses are active only while the owner has the stage set.
    public static double activeGrowth(PlayerEntity player) {
        return stage(player) >= 1 ? growth(player) : 0.0;
    }

    public static void sync(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            KyokaMod.NETWORK.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                    new HogyokuSyncPacket(stage(player), hits(player), falls(player), blocks(player)));
        }
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
