package com.kyoka.addon;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class KyokaCommands {
    @SubscribeEvent
    public void onRegister(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("kyoka")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.literal("hogyoku")
                                .then(Commands.argument("stage", IntegerArgumentType.integer(0, 4))
                                        .executes(ctx -> {
                                            ServerPlayerEntity player = ctx.getSource().getPlayerOrException();
                                            int stage = IntegerArgumentType.getInteger(ctx, "stage");
                                            apply(player, stage);
                                            ctx.getSource().sendSuccess(
                                                    new StringTextComponent("Хогёку: этап " + stage), false);
                                            return 1;
                                        }))));
    }

    // Moves the owner to the given stage with the counters that stage requires.
    private static void apply(ServerPlayerEntity player, int stage) {
        HogyokuHelper.setStage(player, stage);
        player.getPersistentData().putInt("kyoka_hogyoku_hits", stage >= 2 ? HogyokuHelper.MAX_HITS : 0);
        if (stage >= 3) player.getPersistentData().putInt("kyoka_hogyoku_falls", HogyokuHelper.FALLS_NEEDED);
        if (stage >= 4) player.getPersistentData().putInt("kyoka_hogyoku_blocks", HogyokuHelper.BLOCKS_NEEDED);
        HogyokuHelper.sync(player);
    }
}
