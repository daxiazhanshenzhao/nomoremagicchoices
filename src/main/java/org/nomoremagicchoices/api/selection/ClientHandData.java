package org.nomoremagicchoices.api.selection;

import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.common.NeoForge;
import org.nomoremagicchoices.Nomoremagicchoices;
import org.nomoremagicchoices.api.handle.ChangeHandEvent;
import org.nomoremagicchoices.api.init.TagInit;

public class ClientHandData {

    private static SpellSelectionState state = SpellSelectionState.EmptyHand;
    private static Minecraft mc;


    public static void init(Minecraft minecraft){
        mc = minecraft;
    }

    public static void tick(){
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
        if (!state.equals(newState)) {
            changeState(newState);
        }
    }

    public static SpellSelectionState getState() {
        return state;
    }


    public static void changeState(SpellSelectionState newState) {

        ChangeHandEvent event = new ChangeHandEvent(state, newState);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return;

        state = event.getNewState();
        
        // 当手部状态变化时，更新ScrollWightData，让法术组收回到最上方
        var scrollWightData = ClientData.getScrollWightData();
        if (scrollWightData != null) {
            scrollWightData.handleHand(event.getOldState(),event.getNewState());
            Nomoremagicchoices.LOGGER.info("触发空手变动");
        }
    }
    public static boolean isFocus() {
        return state.isFocus();
    }
}
