package com.kyoka.addon;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.util.ResourceLocation;

@Mod(KyokaMod.MODID)
public class KyokaMod {
    public static final String MODID = "kyoka";
    public static final String PROTOCOL = "1";
    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    public KyokaMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(modBus);
        modBus.addListener(ClientSetup::onClientSetup);
        modBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(new KyokaEvents());
        MinecraftForge.EVENT_BUS.register(new HogyokuEvents());
        MinecraftForge.EVENT_BUS.register(new CraterEvents());
    }

    private void commonSetup(final net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) {
        NETWORK.registerMessage(0, TogglePacket.class, TogglePacket::encode, TogglePacket::decode, TogglePacket::handle);
        NETWORK.registerMessage(1, SwapPacket.class, SwapPacket::encode, SwapPacket::decode, SwapPacket::handle);
    }
}
