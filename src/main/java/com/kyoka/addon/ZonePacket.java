package com.kyoka.addon;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ZonePacket {
    public ZonePacket() {}

    public static void encode(ZonePacket msg, PacketBuffer buf) {}

    public static ZonePacket decode(PacketBuffer buf) {
        return new ZonePacket();
    }

    public static void handle(ZonePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) KyokaSkills.toggleZone(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
