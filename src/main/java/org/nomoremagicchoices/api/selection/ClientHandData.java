package org.nomoremagicchoices.api.selection;

import io.redspace.ironsspellbooks.item.CastingItem;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;

import org.nomoremagicchoices.api.event.ChangeHandEvent;
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
        } else if (mainHand.getItem() instanceof CastingItem || offHand.getItem()  instanceof CastingItem) {
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
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return;

        state = event.getNewState();
        
        // 当手部状态变化时，更新ScrollWightData，让法术组收回到最上方
        var scrollWightData = ClientData.getScrollWightData();
        if (scrollWightData != null) {
            scrollWightData.handleHand(event.getOldState(),event.getNewState());
        }
    }
    public static boolean isFocus() {
        return state.isFocus();
    }
}
