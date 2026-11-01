package com.kyoka.addon;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, KyokaMod.MODID);

    public static final RegistryObject<Item> KYOKA_SEALED = ITEMS.register("kyoka_suigetsu_sealed",
            () -> new KyokaSwordItem(new Item.Properties().tab(ItemGroup.TAB_COMBAT).stacksTo(1), false));

    public static final RegistryObject<Item> KYOKA_RELEASED = ITEMS.register("kyoka_suigetsu",
            () -> new KyokaSwordItem(new Item.Properties().tab(ItemGroup.TAB_COMBAT).stacksTo(1), true));

    public static final RegistryObject<Item> KYOKA_HOGYOKU = ITEMS.register("kyoka_hogyoku",
            () -> new HogyokuItem(new Item.Properties().tab(ItemGroup.TAB_MISC).stacksTo(1)));

    public static final RegistryObject<Item> HOGYOKU_WING_BLACK = ITEMS.register("hogyoku_wing_black",
            () -> new HogyokuWingArmorBlack(new Item.Properties().tab(ItemGroup.TAB_COMBAT).stacksTo(1)));

    public static final RegistryObject<Item> HOGYOKU_WING_WHITE = ITEMS.register("hogyoku_wing_white",
            () -> new HogyokuWingArmorWhite(new Item.Properties().tab(ItemGroup.TAB_COMBAT).stacksTo(1)));
}
