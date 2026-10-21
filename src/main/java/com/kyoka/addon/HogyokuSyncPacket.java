package com.kyoka.addon;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class HogyokuSyncPacket {
    public final int stage;
    public final int hits;
    public final int falls;
    public final int blocks;

    public HogyokuSyncPacket(int stage, int hits, int falls, int blocks) {
        this.stage = stage;
        this.hits = hits;
        this.falls = falls;
        this.blocks = blocks;
    }

    public static void encode(HogyokuSyncPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.stage);
        buf.writeInt(msg.hits);
        buf.writeInt(msg.falls);
        buf.writeInt(msg.blocks);
    }

    public static HogyokuSyncPacket decode(PacketBuffer buf) {
        return new HogyokuSyncPacket(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
    }

    public static void handle(HogyokuSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            HudData.stage = msg.stage;
            HudData.hits = msg.hits;
            HudData.falls = msg.falls;
            HudData.blocks = msg.blocks;
        });
        ctx.get().setPacketHandled(true);
    }
}
