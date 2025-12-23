package org.nomoremagicchoices.api.init;


import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.nomoremagicchoices.Nomoremagicchoices;
import org.nomoremagicchoices.gui.SpellSelectionProvider;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = Nomoremagicchoices.MODID)
public class OverlayInit {

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {

        event.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "spell_selection_new", SpellSelectionProvider.instance);
    }
}


