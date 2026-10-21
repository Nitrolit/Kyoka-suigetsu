package com.kyoka.addon;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class HogyokuArmorEvents {
    @SubscribeEvent
    public void onEquipChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntityLiving() instanceof PlayerEntity)) return;
        if (event.getSlot() != EquipmentSlotType.CHEST) return;
        if (event.getFrom().getItem() != ModItems.KYOKA_HOGYOKU_SUBORDINATION.get()) return;
        if (event.getTo().getItem() == ModItems.KYOKA_HOGYOKU_SUBORDINATION.get()) return;

        PlayerEntity player = (PlayerEntity) event.getEntityLiving();
        if (player.level.isClientSide) return;

        // Remove one Subordination item from the inventory and give a damaged Hogyoku in its place.
        for (int i = 0; i < player.inventory.getContainerSize(); i++) {
            ItemStack stack = player.inventory.getItem(i);
            if (stack.getItem() == ModItems.KYOKA_HOGYOKU_SUBORDINATION.get()) {
                player.inventory.setItem(i, ItemStack.EMPTY);
                ItemStack damaged = new ItemStack(ModItems.KYOKA_HOGYOKU_DAMAGED.get());
                if (!player.inventory.add(damaged)) {
                    player.drop(damaged, false);
                }
                return;
            }
        }
    }
}
