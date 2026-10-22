package com.kyoka.addon;

import bleachawaken.deephantom.com.BleachawakenModVariables;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Random;

public class QuestRewardEvents {
    // Chance per completed quest step to receive the sealed Kyoka Suigetsu (once per player).
    public static final float CHANCE = 0.30f;

    private static final String REWARDED_KEY = "kyoka_reward_given";
    private static final String LAST_QUEST_KEY = "kyoka_last_quest";
    private static final String LAST_SIDE_KEY = "kyoka_last_side";
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide) return;
        PlayerEntity player = event.player;
        if (player.tickCount % 20 != 0) return;
        if (player.getPersistentData().getBoolean(REWARDED_KEY)) return;

        BleachawakenModVariables.PlayerVariables v = ReiatsuHelper.vars(player);
        if (v == null) return;

        double quest = v.quest_number;
        double side = v.side_quest;

        // First time we see this player: just remember the current values, no reward.
        if (!player.getPersistentData().contains(LAST_QUEST_KEY)) {
            player.getPersistentData().putDouble(LAST_QUEST_KEY, quest);
            player.getPersistentData().putDouble(LAST_SIDE_KEY, side);
            return;
        }

        double lastQuest = player.getPersistentData().getDouble(LAST_QUEST_KEY);
        double lastSide = player.getPersistentData().getDouble(LAST_SIDE_KEY);
        player.getPersistentData().putDouble(LAST_QUEST_KEY, quest);
        player.getPersistentData().putDouble(LAST_SIDE_KEY, side);

        boolean progressed = quest > lastQuest || side > lastSide;
        if (progressed && RANDOM.nextFloat() < CHANCE) {
            giveSealed(player);
            player.getPersistentData().putBoolean(REWARDED_KEY, true);
        }
    }

    private static void giveSealed(PlayerEntity player) {
        ItemStack sword = new ItemStack(ModItems.KYOKA_SEALED.get());
        if (!player.inventory.add(sword)) {
            player.drop(sword, false);
        }
        player.sendMessage(new StringTextComponent("Вы нашли занпакто: Kyoka Suigetsu (запечатанный)"),
                player.getUUID());
    }
}
