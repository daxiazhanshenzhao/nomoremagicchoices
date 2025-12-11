package org.nomoremagicchoices.api.selection;

import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;

public class ClientData {


    private ClientHandData clientHandData;
    private ScrollWightData scrollWightData;
    private SpellGroupData spellGroupData;

    private SpellSelectionManager manager;
    private Minecraft mc;
    private LocalPlayer player;

    private ClientData(Minecraft mc,SpellSelectionManager manager){

        this.mc = mc;
        player = mc.player;
        this.manager = manager;

        clientHandData = new ClientHandData(mc);
        spellGroupData = new SpellGroupData(manager);
        scrollWightData = new ScrollWightData(spellGroupData,clientHandData);
    }
    public SpellGroupData getSpellGroupData() {
        return spellGroupData;
    }

    public ClientHandData getClientHandData() {
        return clientHandData;
    }

    public ScrollWightData getScrollWightData() {
        return scrollWightData;
    }

    public void tick() {

            spellGroupData.tick();
            clientHandData.tick();
            scrollWightData.tick();

    }

    //manager========================================
    //group



}
