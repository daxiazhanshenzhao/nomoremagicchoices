package org.nomoremagicchoices.gui;

import io.redspace.ironsspellbooks.gui.overlays.SpellBarOverlay;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import org.jetbrains.annotations.NotNull;
import org.nomoremagicchoices.config.ClientConfig;

@EventBusSubscriber
public class SpellSelectionProvider implements LayeredDraw.Layer {

    private final SpellSelectionLayerV1 customLayer;

    public SpellSelectionProvider() {
        this.customLayer = new SpellSelectionLayerV1();

    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        customLayer.render(guiGraphics, deltaTracker);
    }

    @SubscribeEvent
    public static void RenderGuiEvent(RenderGuiLayerEvent.Pre event) {
        if (ClientConfig.ENABLE_CUSTOM_UI.get()) {
            if (event.getLayer() instanceof SpellBarOverlay){
                event.setCanceled(true);
            }
        }
    }

    public SpellSelectionLayerV1 getCustomLayer() {
        return customLayer;
    }
}

