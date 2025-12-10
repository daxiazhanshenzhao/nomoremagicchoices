package org.nomoremagicchoices.api.handle;


import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.nomoremagicchoices.api.event.ChangeSpellEvent;
import org.nomoremagicchoices.api.selection.ClientScrollData;

@EventBusSubscriber(Dist.CLIENT)
public class ClientEventHandle {

    @SubscribeEvent
    public static void clientTickEvent(ClientTickEvent.Pre event) {
        ClientScrollData.tickHandle();
        ChangeSpellEvent.SpellCountMonitor.tick();

    }

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



}
