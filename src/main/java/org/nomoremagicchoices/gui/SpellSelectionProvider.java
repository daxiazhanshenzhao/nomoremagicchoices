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
import org.nomoremagicchoices.Nomoremagicchoices;
import org.nomoremagicchoices.api.selection.ILayerState;
import org.nomoremagicchoices.config.ClientConfig;


public class SpellSelectionProvider implements IGuiOverlay {


    public static final SpellSelectionProvider instance = new SpellSelectionProvider();
    private final ILayerState customLayer;

    public SpellSelectionProvider() {
//        this.customLayer = new SpellSelectionLayerV1();
        this.customLayer = new SpellSelectionLayerV2();
    }

    @Override
    public void render(ForgeGui forgeGui, GuiGraphics guiGraphics, float v, int i, int i1) {

        Nomoremagicchoices.LOGGER.info("Rendering Spell Selection Layer");
    }


    public void render(GuiGraphics context,float parTick){
        if (ClientConfig.ENABLE_CUSTOM_UI.get()){
            // 法术数量少于阈值时使用自定义UI
            if (ClientMagicData.getSpellSelectionManager().getAllSpells().size() < ClientConfig.MINE_CUSTOM_SPELL.get()){
                customLayer.render1(context, parTick);
            }
        }
    }




}
