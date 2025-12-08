package org.nomoremagicchoices.api.handle;


import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.nomoremagicchoices.api.init.TagInit;
import org.nomoremagicchoices.api.selection.SpellSelectionState;
import org.nomoremagicchoices.gui.SpellSelectionLayerV2;
import org.nomoremagicchoices.gui.SpellSelectionProvider;

@EventBusSubscriber(Dist.CLIENT)
public class SpellSelectionHandle {


    @SubscribeEvent
    public static void handle(ClientTickEvent.Post event) {
//        Minecraft mc = Minecraft.getInstance();
//
//
//        if (mc.player != null) {
//            var player = mc.player;
//
//            var layer = SpellSelectionProvider.instance.getCustomLayer();
//            var state = layer.getSpellSelectionState();
//
//            var mainHand = player.getMainHandItem();
//
//
//            if (mainHand.isEmpty() && player.getOffhandItem().isEmpty()) {
//                state = SpellSelectionState.EmptyHand;
//            } else if (mainHand.is(TagInit.SKILL_WEAPON)) {
//                state = SpellSelectionState.Weapon;
//            } else if (player.getMainHandItem().has(ComponentRegistry.CASTING_IMPLEMENT) ||player.getOffhandItem().has(ComponentRegistry.CASTING_IMPLEMENT)) {
//                state = SpellSelectionState.Staff;
//            }
//            layer.setSpellSelectionState(state);
//
//
//        }
//    }
    }
}
