package com.kyoka.addon;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class BlinkPacket {
    public static final int COOLDOWN_TICKS = 10; // 0.5 seconds
    public static final int MAX_DISTANCE = 2;
    private static final String CD_KEY = "kyoka_blink_cd";

    public BlinkPacket() {}

    public static void encode(BlinkPacket msg, PacketBuffer buf) {}

    public static BlinkPacket decode(PacketBuffer buf) {
        return new BlinkPacket();
    }

    public static void handle(BlinkPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null) return;
            if (HogyokuHelper.stage(player) < 3) return;

            long now = player.level.getGameTime();
            if (player.getPersistentData().getLong(CD_KEY) > now) return;

            World world = player.level;
            Vector3d look = player.getLookAngle();
            Vector3d dir = new Vector3d(look.x, 0, look.z);
            if (dir.lengthSqr() < 1.0E-6) return;
            dir = dir.normalize();

            // Find the furthest free spot within 2 blocks: both feet and head cells must be empty.
            Vector3d best = null;
            for (int d = MAX_DISTANCE; d >= 1; d--) {
                double x = player.getX() + dir.x * d;
                double z = player.getZ() + dir.z * d;
                BlockPos feet = new BlockPos(Math.floor(x), Math.floor(player.getY()), Math.floor(z));
                BlockPos head = feet.above();
                if (world.isEmptyBlock(feet) && world.isEmptyBlock(head)) {
                    best = new Vector3d(x, player.getY(), z);
                    break;
                }
            }
            if (best == null) return;

            player.getPersistentData().putLong(CD_KEY, now + COOLDOWN_TICKS);
            player.connection.teleport(best.x, best.y, best.z, player.yRot, player.xRot);
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    net.minecraft.util.SoundEvents.ENDERMAN_TELEPORT,
                    net.minecraft.util.SoundCategory.PLAYERS, 0.6f, 1.4f);
        });
        ctx.get().setPacketHandled(true);
    }
                              }
