package com.kyoka.addon;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;

public class HogyokuSubordinationArmor extends ArmorItem {
    private static final IArmorMaterial MATERIAL = new IArmorMaterial() {
        @Override
        public int getDurabilityForSlot(EquipmentSlotType slot) { return 500; }

        @Override
        public int getDefenseForSlot(EquipmentSlotType slot) {
            return slot == EquipmentSlotType.CHEST ? 8 : 3;
        }

        @Override
        public int getEnchantmentValue() { return 15; }

        @Override
        public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_IRON; }

        @Override
        public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }

        @Override
        public String getName() { return "kyoka_subordination"; }

        @Override
        public float getToughness() { return 2.0f; }

        @Override
        public float getKnockbackResistance() { return 0.0f; }
    };

    public HogyokuSubordinationArmor(Item.Properties properties) {
        super(MATERIAL, EquipmentSlotType.CHEST, properties);
    }
}
