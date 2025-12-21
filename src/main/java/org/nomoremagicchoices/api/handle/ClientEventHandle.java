package org.nomoremagicchoices.api.handle;

import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.nomoremagicchoices.Nomoremagicchoices;

import org.nomoremagicchoices.api.selection.ClientData;

@EventBusSubscriber(Dist.CLIENT)
public class ClientEventHandle {

    @SubscribeEvent
    public static void clientTickEvent(PlayerTickEvent.Pre event) {

        if (event.getEntity() instanceof LocalPlayer) {
            ClientData.tick();

        }

    }


//    public static void changeItem()

//    @SubscribeEvent
//    public static void onRenderGuiPost(RenderGuiEvent.Pre event) {
//        if (event.getPartialTick().getRealtimeDeltaTicks()> 0f){
//            ClientScrollData.renderHandle(event.getGuiGraphics(),event.getPartialTick());
//        }
//
//    }
//
//    @SubscribeEvent
//    public static void changeWindowSize(){
//
//    }

//    @SubscribeEvent
//    public static void ChangeSpellEvent(ChangeSpellEvent event) {
//        event.getOldCount()
//    }


}
