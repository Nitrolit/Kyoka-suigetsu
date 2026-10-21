package com.kyoka.addon;

import bleachawaken.deephantom.com.BleachawakenModVariables;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.UUID;

public final class HogyokuHelper {
    public static final double USE_COST = 1200.0;
    public static final int HITS_PER_STEP = 10;
    public static final double GROWTH_STEP = 0.01;
    public static final double GROWTH_CAP = 0.20;
    public static final int MAX_HITS = 200;
    public static final int FALLS_NEEDED = 10;
    public static final int BLOCKS_NEEDED = 10;
    public static final int MAX_STAGE = 4;
    public static final double SKILL_BONUS = 0.30;

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

    // Setting a stage resets the stage counters. Stage 2 gives the Subordination armor.
    public static void setStage(PlayerEntity player, int stage) {
        player.getPersistentData().putInt(STAGE_KEY, stage);
        player.getPersistentData().putInt(FALLS_KEY, 0);
        player.getPersistentData().putInt(BLOCKS_KEY, 0);
        if (stage == 2) giveArmor(player);
        sync(player);
    }

    private static void giveArmor(PlayerEntity player) {
        ItemStack armor = new ItemStack(ModItems.KYOKA_HOGYOKU_SUBORDINATION.get());
        if (!player.inventory.add(armor)) {
            player.drop(armor, false);
        }
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
    // Stage 3: 10 fall damage events and 10 blocked hits move to stage 4.
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

    // Blocked hits count only in stage 3.
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

    // +30% skill damage while the Subordination armor is worn (stage 2 and up).
    public static double armorSkillBonus(PlayerEntity player) {
        if (stage(player) < 2) return 0.0;
        ItemStack chest = player.getItemBySlot(EquipmentSlotType.CHEST);
        return chest.getItem() == ModItems.KYOKA_HOGYOKU_SUBORDINATION.get() ? SKILL_BONUS : 0.0;
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

    // Regeneration by stage.
    private static double regenThreshold(int stage) { return stage >= 2 ? 0.60 : 0.40; }
    private static double regenHeal(int stage) { return stage >= 2 ? 0.80 : 0.65; }
    private static double regenCost(int stage) { return stage >= 2 ? 200.0 : 100.0; }

    private static int regenCooldown(int stage) {
        switch (stage) {
            case 1: return 500;
            case 2: return 340;
            case 3: return 200;
            default: return 100;
        }
    }

    public static void tryRegen(PlayerEntity player) {
        int s = stage(player);
        if (s < 1) return;
        long now = player.level.getGameTime();
        if (player.getPersistentData().getLong(COOLDOWN_KEY) > now) return;
        if (player.getHealth() > player.getMaxHealth() * regenThreshold(s)) return;

        BleachawakenModVariables.PlayerVariables v = ReiatsuHelper.vars(player);
        double cost = regenCost(s);
        if (v == null || v.reiatsu_Bar < cost) return;

        ReiatsuHelper.spend(player, cost);
        player.setHealth((float) (player.getMaxHealth() * regenHeal(s)));
        player.getPersistentData().putLong(COOLDOWN_KEY, now + regenCooldown(s));
    }
}
