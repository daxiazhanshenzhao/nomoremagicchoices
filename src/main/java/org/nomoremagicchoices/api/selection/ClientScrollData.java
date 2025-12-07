package org.nomoremagicchoices.api.selection;

import com.mojang.datafixers.util.Pair;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.core.NonNullList;
import org.nomoremagicchoices.gui.component.ScrollSpellWight;
import org.nomoremagicchoices.player.ModKeyMapping;

import java.util.List;
import java.util.Map;

public class ClientScrollData {

    public static final int TOTAL_TICKS = 8;
    public static int cTick = 0;
    public static boolean isRunning = false;




    public static void tickHandle(){
        handleRunning();
        handleCurrentTick();

    }

    public static void handleRunning(){
        if (ModKeyMapping.CHANG_GROUP.get().consumeClick() && !isRunning){
            isRunning = true;
        }
    }
    public static void handleCurrentTick(){
        if (isRunning){
            cTick++;
            if (cTick >= TOTAL_TICKS) {
                cTick = 0; // 达到8后归0
                isRunning = false; // 允许下次触发
            }
        }
    }


    //每次更新法术槽位时更新
    public static List<Pair<ScrollSpellWight,Integer>> update(){
        int size = SpellGroupData.getGroupCount();


        spellWightList = NonNullList.withSize(size+1, Pair.of(ScrollSpellWight.Empty,0));

        return spellWightList;
    }
    public static List<Pair<ScrollSpellWight,Integer>> spellWightList;

    public static void drawWight(){

    }
    public static void addWight(){
        
    }

}
