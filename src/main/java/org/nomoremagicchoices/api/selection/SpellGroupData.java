package org.nomoremagicchoices.api.selection;

import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.core.NonNullList;
import net.neoforged.fml.common.EventBusSubscriber;
import org.nomoremagicchoices.Nomoremagicchoices;

import java.util.List;


public class SpellGroupData{

    //从0开始
    private static int currentGroupIndex = 0;
    private static int groupCount = 0;
    private static int selectIndex;

    private static List<SpellData> spells = NonNullList.withSize(1, SpellData.EMPTY);
    private static SpellSelectionManager manager;


    // 每组最多包含的法术数量
    private static final int SPELLS_PER_GROUP = 4;


    public static void init(SpellSelectionManager spellManager) {
        manager = spellManager;
        spells = manager.getAllSpells().stream().map(selectionOption -> selectionOption.spellData).toList();
        update();
    }


    public static void update(){

        selectIndex = manager.getSelectionIndex();
        currentGroupIndex = selectIndex / SPELLS_PER_GROUP;
        // 计算组数：向上取整的除法
        groupCount = (spells.size() + SPELLS_PER_GROUP - 1) / SPELLS_PER_GROUP;
        var scrollData = ClientData.getScrollWightData();

        scrollData.update();
        Nomoremagicchoices.LOGGER.info("你他们怎么法杖这么多事情");


    }


    public static void tick(){
        if (manager == null) return;

         var newSpells = manager.getAllSpells().stream().map(selectionOption -> selectionOption.spellData).toList();
         var newSelectIndex = manager.getSelectionIndex();

         if (!newSpells.equals(spells) || newSelectIndex != selectIndex) {
            update();
         }
    }

    public static List<SpellData> getCurrentSpells(){
        if (currentGroupIndex < 0 || currentGroupIndex >= groupCount) {
            return NonNullList.withSize(0, SpellData.EMPTY);
        }
        
        int startIndex = currentGroupIndex * SPELLS_PER_GROUP;
        int endIndex = Math.min(startIndex + SPELLS_PER_GROUP, spells.size());
        
        // 确保startIndex不超过spells.size()
        if (startIndex >= spells.size()) {
            return NonNullList.withSize(0, SpellData.EMPTY);
        }
        
        return spells.subList(startIndex, endIndex);
    }

    public static int getCurrentGroupIndex() {
        return currentGroupIndex;
    }

    /**
     *
     * @param groupIndex 最好为0，最大为size-1
     * @return
     */
    public static List<SpellData> getIndexSpells(int groupIndex){
        if (groupIndex < 0 || groupIndex >= groupCount) {
            return NonNullList.withSize(0, SpellData.EMPTY);
        }
        
        int startIndex = groupIndex * SPELLS_PER_GROUP;
        int endIndex = Math.min(startIndex + SPELLS_PER_GROUP, spells.size());
        
        // 确保startIndex不超过spells.size()
        if (startIndex >= spells.size()) {
            return NonNullList.withSize(0, SpellData.EMPTY);
        }
        
        return spells.subList(startIndex, endIndex);
    }


    public static void add() {
        move(1);
    }


    public static void less() {
        move(-1);
    }


    public static void move(int offset) {

        int newIndex = currentGroupIndex + offset;

        if (groupCount > 0) {

            while (newIndex < 0) {
                newIndex += groupCount;
            }

            newIndex = newIndex % groupCount;
        } else {

            newIndex = 0;
        }
        
        currentGroupIndex = newIndex;
        var selectIndex = Math.min(currentGroupIndex * SPELLS_PER_GROUP, spells.size() - 1);
        manager.makeSelection(selectIndex);
    }

    public static int getGroupCount() {
        return groupCount;
    }

    /**
     * 检测按键按下
     */
    public static void handleChange(){

    }
}
