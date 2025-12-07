package org.nomoremagicchoices.api.init;


import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.nomoremagicchoices.Nomoremagicchoices;
import org.nomoremagicchoices.gui.SpellSelectionProvider;

@EventBusSubscriber(Dist.CLIENT)
public class OverlayInit {

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiLayersEvent event) {

        event.registerBelow(
            VanillaGuiLayers.EXPERIENCE_BAR,
            ResourceLocation.fromNamespaceAndPath(Nomoremagicchoices.MODID, "spell_selection"),
            SpellSelectionProvider.instance
        );
    }
}
