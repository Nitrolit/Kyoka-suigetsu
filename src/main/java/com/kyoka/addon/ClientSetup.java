package com.kyoka.addon;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.registry.ClientRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = KyokaMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {
    public static final KeyMapping TOGGLE_KEY = new KeyMapping(
            "key.kyoka.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, "key.categories.kyoka");

    public static void onClientSetup(FMLClientSetupEvent event) {
        ClientRegistry.registerKeyBinding(TOGGLE_KEY);
    }

    @Mod.EventBusSubscriber(modid = KyokaMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeClientEvents {
        @SubscribeEvent
        public static void onKey(InputEvent.KeyInputEvent event) {
            while (TOGGLE_KEY.consumeClick()) {
                KyokaMod.NETWORK.sendToServer(new TogglePacket());
            }
        }
    }
}
