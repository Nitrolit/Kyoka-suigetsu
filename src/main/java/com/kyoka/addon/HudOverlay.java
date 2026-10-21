package com.kyoka.addon;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KyokaMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HudOverlay {
    @SubscribeEvent
    public static void onText(RenderGameOverlayEvent.Text event) {
        if (HudData.stage < 1) return;

        event.getLeft().add("Хогёку: этап " + HudData.stage + " / " + HogyokuHelper.MAX_STAGE);
        event.getLeft().add("Бонус: +" + (int) Math.round(HogyokuHelper.growthFromHits(HudData.hits) * 100) + "%");

        if (HudData.stage == 1) {
            event.getLeft().add("До этапа 2: ударов " + HudData.hits + " / " + HogyokuHelper.MAX_HITS);
        } else if (HudData.stage == 2) {
            event.getLeft().add("До этапа 3: падений " + HudData.falls + " / " + HogyokuHelper.FALLS_NEEDED);
        } else if (HudData.stage == 3) {
            event.getLeft().add("До этапа 4: падений " + HudData.falls + " / " + HogyokuHelper.FALLS_NEEDED
                    + ", блоков " + HudData.blocks + " / " + HogyokuHelper.BLOCKS_NEEDED);
        } else {
            event.getLeft().add("Максимальный этап пока");
        }
    }
}
