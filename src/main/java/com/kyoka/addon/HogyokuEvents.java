package com.kyoka.addon;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class HogyokuEvents {
    public static final double SWORD_EXTRA_DAMAGE = 0.10;

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide) return;
        PlayerEntity player = event.player;
        if (player.tickCount % 10 != 0) return;
        HogyokuHelper.tryRegen(player);
        HogyokuHelper.applySpeed(player);
        HogyokuHelper.sync(player);
    }

    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        float amount = event.getAmount();

        // Dealt hits: the owner's attack counts and gets the bonus.
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof PlayerEntity && attacker != event.getEntityLiving()) {
            PlayerEntity owner = (PlayerEntity) attacker;
            double g = HogyokuHelper.activeGrowth(owner);
            if (g > 0) amount *= (float) (1.0 + g);
            if (KyokaEvents.isReleasedHeld(owner)) amount *= (float) (1.0 + SWORD_EXTRA_DAMAGE);
            if (HogyokuHelper.stage(owner) >= 1) HogyokuHelper.addHit(owner);
        }

        // Received hits: reduced by the bonus, counted, and fall damage counted for stages 2-3.
        if (event.getEntityLiving() instanceof PlayerEntity) {
            PlayerEntity victim = (PlayerEntity) event.getEntityLiving();
            double g = HogyokuHelper.activeGrowth(victim);
            if (g > 0) amount *= (float) (1.0 - g);
            if (HogyokuHelper.stage(victim) >= 1) HogyokuHelper.addHit(victim);
            if (event.getSource() == DamageSource.FALL) HogyokuHelper.addFall(victim);
        }

        event.setAmount(amount);
    }

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
