package org.nomoremagicchoices.api.selection;

import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.nomoremagicchoices.api.init.TagInit;

public class ClientHandData {

    private SpellSelectionState state;
    private Minecraft mc;

    public ClientHandData(Minecraft mc){
        this.mc = mc;
    }

    public void tick(){
        if (mc == null) return;

        var player = mc.player;

        if (player == null) return;

        var mainHand = player.getMainHandItem();
        var offHand = player.getOffhandItem();

        var newState = SpellSelectionState.EmptyHand;

        if (mainHand.isEmpty() && offHand.isEmpty()) {
            newState = SpellSelectionState.EmptyHand;
        } else if (mainHand.is(TagInit.SKILL_WEAPON)) {
            newState = SpellSelectionState.Weapon;
        } else if (mainHand.has(ComponentRegistry.CASTING_IMPLEMENT) || offHand.has(ComponentRegistry.CASTING_IMPLEMENT)) {
            newState = SpellSelectionState.Staff;
        } else {
            newState = SpellSelectionState.EmptyHand;
        }

        state = newState;
    }

    public SpellSelectionState getState() {
        return state;
    }
}
