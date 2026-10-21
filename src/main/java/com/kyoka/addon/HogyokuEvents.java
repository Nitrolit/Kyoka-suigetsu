package com.kyoka.addon;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class HogyokuEvents {
    // Extra outgoing damage while the released sword is held (on top of the Hogyoku growth).
    public static final double SWORD_EXTRA_DAMAGE = 0.10;

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide) return;
        PlayerEntity player = event.player;
        if (player.tickCount % 10 != 0) return;
        HogyokuHelper.tryRegen(player);
        HogyokuHelper.applySpeed(player);
    }

    // Works for ALL damage sources: mobs, falling, fire, lava, etc.
    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        float amount = event.getAmount();

        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof PlayerEntity) {
            PlayerEntity owner = (PlayerEntity) attacker;
            double g = HogyokuHelper.activeGrowth(owner);
            if (g > 0) amount *= (float) (1.0 + g);
            if (KyokaEvents.isReleasedHeld(owner)) amount *= (float) (1.0 + SWORD_EXTRA_DAMAGE);
        }

        if (event.getEntityLiving() instanceof PlayerEntity) {
            PlayerEntity victim = (PlayerEntity) event.getEntityLiving();
            double g = HogyokuHelper.activeGrowth(victim);
            if (g > 0) amount *= (float) (1.0 - g);
            if (HogyokuHelper.stage(victim) >= 1) HogyokuHelper.addHit(victim);
        }

        event.setAmount(amount);
    }

    // Jump +% (no jump attribute exists in 1.16.5, so the jump impulse is scaled here).
    @SubscribeEvent
    public void onJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntityLiving() instanceof PlayerEntity)) return;
        PlayerEntity player = (PlayerEntity) event.getEntityLiving();
        double g = HogyokuHelper.activeGrowth(player);
        if (g <= 0) return;
        Vector3d m = player.getDeltaMovement();
        player.setDeltaMovement(m.x, m.y * (1.0 + g), m.z);
    }
}
