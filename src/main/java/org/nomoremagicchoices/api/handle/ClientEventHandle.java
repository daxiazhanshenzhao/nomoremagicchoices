package org.nomoremagicchoices.api.handle;


import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.nomoremagicchoices.api.selection.ClientScrollData;

@EventBusSubscriber(Dist.CLIENT)
public class ClientEventHandle {

    @SubscribeEvent
    public static void clientTickEvent(ClientTickEvent.Pre event) {
        ClientScrollData.tickHandle();

    }



}
