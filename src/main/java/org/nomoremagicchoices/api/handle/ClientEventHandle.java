package org.nomoremagicchoices.api.handle;

import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.nomoremagicchoices.Nomoremagicchoices;

import org.nomoremagicchoices.api.selection.ClientData;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEventHandle {

    @SubscribeEvent
    public static void clientTickEvent(TickEvent.PlayerTickEvent event) {

        if (event.player instanceof LocalPlayer) {
            ClientData.tick();

        }





    }
    @SubscribeEvent
    public static void changeEvent(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof LocalPlayer player) {
            Nomoremagicchoices.LOGGER.info("已经发生了物品栏位转换");
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
