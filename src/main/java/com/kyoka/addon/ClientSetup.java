package com.kyoka.addon;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = KyokaMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {
    public static final KeyBinding TOGGLE_KEY = new KeyBinding(
            "key.kyoka.toggle", InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_J, "key.categories.kyoka");
    public static final KeyBinding SWAP_KEY = new KeyBinding(
            "key.kyoka.swap", InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_B, "key.categories.kyoka");
    public static final KeyBinding BLINK_KEY = new KeyBinding(
            "key.kyoka.blink", InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.categories.kyoka");
    public static final KeyBinding CRYSTAL_KEY = new KeyBinding(
            "key.kyoka.crystal", InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_X, "key.categories.kyoka");
    public static final KeyBinding ZONE_KEY = new KeyBinding(
            "key.kyoka.zone", InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.kyoka");

    public static void onClientSetup(FMLClientSetupEvent event) {
        ClientRegistry.registerKeyBinding(TOGGLE_KEY);
        ClientRegistry.registerKeyBinding(SWAP_KEY);
        ClientRegistry.registerKeyBinding(BLINK_KEY);
        ClientRegistry.registerKeyBinding(CRYSTAL_KEY);
        ClientRegistry.registerKeyBinding(ZONE_KEY);
    }

    @Mod.EventBusSubscriber(modid = KyokaMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeClientEvents {
        @SubscribeEvent
        public static void onKey(InputEvent.KeyInputEvent event) {
            while (TOGGLE_KEY.consumeClick()) {
                KyokaMod.NETWORK.sendToServer(new TogglePacket());
            }
            while (SWAP_KEY.consumeClick()) {
                KyokaMod.NETWORK.sendToServer(new SwapPacket());
            }
            while (BLINK_KEY.consumeClick()) {
                KyokaMod.NETWORK.sendToServer(new BlinkPacket());
            }
            while (CRYSTAL_KEY.consumeClick()) {
                KyokaMod.NETWORK.sendToServer(new CrystalPacket());
            }
            while (ZONE_KEY.consumeClick()) {
                KyokaMod.NETWORK.sendToServer(new ZonePacket());
            }
        }
    }
}
