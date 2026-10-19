package com.kyoka.addon;

import net.minecraft.network.PacketBuffer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class TogglePacket {
    public TogglePacket() {}

    public static void encode(TogglePacket msg, PacketBuffer buf) {}

    public static TogglePacket decode(PacketBuffer buf) {
        return new TogglePacket();
    }

    public static void handle(TogglePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null) return;
            ItemStack held = player.getMainHandItem();
            int count = held.getCount();
            if (held.getItem() == ModItems.KYOKA_SEALED.get()) {
                player.setItemInHand(Hand.MAIN_HAND, new ItemStack(ModItems.KYOKA_RELEASED.get(), count));
            } else if (held.getItem() == ModItems.KYOKA_RELEASED.get()) {
                player.setItemInHand(Hand.MAIN_HAND, new ItemStack(ModItems.KYOKA_SEALED.get(), count));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
