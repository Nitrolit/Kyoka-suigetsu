package com.kyoka.addon;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ShunpoCooldownEvents {
    private static boolean adjusting = false;

    // On stage 3+ the bleachawaken shunpo cooldown effect lasts 50% shorter.
    @SubscribeEvent
    public void onPotionAdded(PotionEvent.PotionAddedEvent event) {
        if (adjusting) return;
        LivingEntity entity = event.getEntityLiving();
        if (!(entity instanceof PlayerEntity)) return;
        PlayerEntity player = (PlayerEntity) entity;
        if (HogyokuHelper.stage(player) < 3) return;

        EffectInstance inst = event.getPotionEffect();
        if (!inst.getEffect().getClass().getName().contains("MovementCD")) return;

        int halved = Math.max(1, inst.getDuration() / 2);
        adjusting = true;
        try {
            player.removeEffect(inst.getEffect());
            player.addEffect(new EffectInstance(inst.getEffect(), halved,
                    inst.getAmplifier(), inst.isAmbient(), inst.isVisible()));
        } finally {
            adjusting = false;
        }
    }
}
