package com.kyoka.addon;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SwapPacket {
    public SwapPacket() {}

    public static void encode(SwapPacket msg, PacketBuffer buf) {}

    public static SwapPacket decode(PacketBuffer buf) {
        return new SwapPacket();
    }

    public static void handle(SwapPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null || !KyokaEvents.isPassiveActive(player)) return;

            KyokaEvents.State state = KyokaEvents.state(player);
            LivingEntity target = state.lastAttacker;
            state.lastAttacker = null;
            if (target == null || !target.isAlive()) return;
            if (target.distanceToSqr(player) > 400) return;
            if (!SubordinationHelper.isSubordinate(player, target)) return;

            Vector3d p = player.position();
            Vector3d t = target.position();
            player.setPos(t.x, t.y, t.z);
            target.setPos(p.x, p.y, p.z);
            target.hurt(DamageSource.playerAttack(player),
                    2.0f * (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE));
        });
        ctx.get().setPacketHandled(true);
    }
}
