package org.nomoremagicchoices.api.selection;

import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientData {



    static ClientHandData clientHandData;
    static ScrollWightData scrollWightData;
    static SpellGroupData spellGroupData;

    static SpellSelectionManager manager;
    static Minecraft mc = Minecraft.getInstance();
    static LocalPlayer player;


    public static void init(Minecraft minecraft, SpellSelectionManager spellSelectionManager){
        mc = minecraft;
        player = mc.player;
        manager = spellSelectionManager;

        ClientHandData.init(mc);
        SpellGroupData.init(manager);
    }
    public static SpellGroupData getSpellGroupData() {
        return spellGroupData;
    }

    public static ClientHandData getClientHandData() {
        return clientHandData;
    }

    public static ScrollWightData getScrollWightData() {
        if (scrollWightData == null) {
            scrollWightData = new ScrollWightData(spellGroupData,clientHandData);
        }

        return scrollWightData;
    }

    public static void tick() {
            SpellGroupData.tick();
            ClientHandData.tick();
            getScrollWightData().tick();
    }

    @SubscribeEvent
    public static void initManager(SpellSelectionManager.SpellSelectionEvent event){
        if (event.getEntity() instanceof LocalPlayer) {

            var manager = event.getManager();
            var mc = Minecraft.getInstance();
            ClientData.init(mc,manager);
        }


    }



}
