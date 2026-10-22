package com.kyoka.addon;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class CrystalPacket {
    public CrystalPacket() {}

    public static void encode(CrystalPacket msg, PacketBuffer buf) {}

    public static CrystalPacket decode(PacketBuffer buf) {
        return new CrystalPacket();
    }

    public static void handle(CrystalPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) KyokaSkills.activateCrystal(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
