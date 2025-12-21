package org.nomoremagicchoices.gui;

import io.redspace.ironsspellbooks.gui.overlays.SpellBarOverlay;
import io.redspace.ironsspellbooks.player.ClientMagicData;

import net.minecraft.client.gui.GuiGraphics;


import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.antlr.v4.runtime.misc.NotNull;
import org.nomoremagicchoices.api.selection.ILayerState;
import org.nomoremagicchoices.config.ClientConfig;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class SpellSelectionProvider implements IGuiOverlay {


    public static SpellSelectionProvider instance = new SpellSelectionProvider();
    private final ILayerState customLayer;

    public SpellSelectionProvider() {
//        this.customLayer = new SpellSelectionLayerV1();
        this.customLayer = new SpellSelectionLayerV2();
    }


    public void render(@NotNull GuiGraphics guiGraphics, float deltaTracker) {
        if (ClientConfig.ENABLE_CUSTOM_UI.get()){
            // 法术数量少于阈值时使用自定义UI
            if (ClientMagicData.getSpellSelectionManager().getAllSpells().size() < ClientConfig.MINE_CUSTOM_SPELL.get()){
                customLayer.render1(guiGraphics, deltaTracker);
            }
        }
    }



    public ILayerState getCustomLayer() {
        return customLayer;
    }

    @SubscribeEvent
    public static void RenderGuiEvent(RenderGuiOverlayEvent event) {
        if (event.getOverlay().overlay() instanceof SpellBarOverlay){
            if (ClientConfig.ENABLE_CUSTOM_UI.get()) {
                // 法术数量少于阈值时取消原版UI（使用自定义UI）
                if (ClientMagicData.getSpellSelectionManager().getAllSpells().size() < ClientConfig.MINE_CUSTOM_SPELL.get()){
                    event.setCanceled(true);
                }
                // 否则（法术数量 >= 阈值）不取消，使用原版UI
            }
        }
    }

    @Override
    public void render(ForgeGui forgeGui, GuiGraphics guiGraphics, float v, int i, int i1) {

    }
}
