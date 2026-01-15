package org.nomoremagicchoices.gui;

import io.redspace.ironsspellbooks.gui.overlays.SpellBarOverlay;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import org.jetbrains.annotations.NotNull;
import org.nomoremagicchoices.api.selection.ILayerState;
import org.nomoremagicchoices.config.ClientConfig;

@EventBusSubscriber(Dist.CLIENT)
public class SpellSelectionProvider implements LayeredDraw.Layer {


    public static SpellSelectionProvider instance = new SpellSelectionProvider();
    private final ILayerState customLayer;

    public SpellSelectionProvider() {
//        this.customLayer = new SpellSelectionLayerV1();
        this.customLayer = new SpellSelectionLayerV2();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (ClientConfig.ENABLE_CUSTOM_UI.get()){
            // 法术数量少于阈值时使用自定义UI
            if (customGui()){
                customLayer.render(guiGraphics, deltaTracker);
            }
        }
    }



    public ILayerState getCustomLayer() {
        return customLayer;
    }

    @SubscribeEvent
    public static void RenderGuiEvent(RenderGuiLayerEvent.Pre event) {
        if (event.getLayer() instanceof SpellBarOverlay){
            if (ClientConfig.ENABLE_CUSTOM_UI.get()) {
                // 法术数量少于阈值时取消原版UI（使用自定义UI）
                if (customGui()){
                    event.setCanceled(true);
                }
                // 否则（法术数量 >= 阈值）不取消，使用原版UI
            }
        }
    }

    /**
     * 法术小于配置的值
     * @return ture 运行，false 不运行
     */
    public static boolean customGui(){
        if (ClientMagicData.getSpellSelectionManager() == null) return false;
        if (ClientMagicData.getSpellSelectionManager().getAllSpells().isEmpty()) return false;

        return ClientMagicData.getSpellSelectionManager().getAllSpells().size() < ClientConfig.MINE_CUSTOM_SPELL.get();
    }
}
