package com.kyoka.addon;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.util.InputMappings;

@Mod.EventBusSubscriber(modid = KyokaMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {
    public static final KeyBinding TOGGLE_KEY = new KeyBinding(
            "key.kyoka.toggle", InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_J, "key.categories.kyoka");

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
