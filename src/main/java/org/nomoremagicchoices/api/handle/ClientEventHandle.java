package org.nomoremagicchoices.api.handle;

import io.redspace.ironsspellbooks.gui.overlays.SpellBarOverlay;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.nomoremagicchoices.Nomoremagicchoices;

import org.nomoremagicchoices.api.selection.ClientData;
import org.nomoremagicchoices.config.ClientConfig;
import org.nomoremagicchoices.gui.SpellSelectionProvider;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
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

    @SubscribeEvent
    public static void RenderGuiEvent(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay().overlay() instanceof SpellBarOverlay){
            if (ClientConfig.ENABLE_CUSTOM_UI.get()) {
                // 法术数量少于阈值时取消原版UI（使用自定义UI）
                if (ClientMagicData.getSpellSelectionManager().getAllSpells().size() < ClientConfig.MINE_CUSTOM_SPELL.get()){
                    event.setCanceled(true);
                }
                // 否则（法术数量 >= 阈值）不取消，使用原版UI
            }
        }
        SpellSelectionProvider.instance.render(event.getGuiGraphics(),event.getPartialTick());
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
