package org.nomoremagicchoices.api.selection;

import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.List;


public class SpellGroupData implements ISpellGroupManager{

    //从0开始
    private int currentGroupIndex = 0;
    private int groupCount = 0;
    private int selectIndex;

    private List<SpellData> spells;
    private SpellSelectionManager manager;


    // 每组最多包含的法术数量
    private static final int SPELLS_PER_GROUP = 4;

    public SpellGroupData(SpellSelectionManager manager) {
        this.manager = manager;
        this.spells = manager.getAllSpells().stream().map(selectionOption -> selectionOption.spellData).toList();

        update();
    }


    public void update(){

        selectIndex = manager.getSelectionIndex();
        currentGroupIndex = selectIndex / SPELLS_PER_GROUP;
        groupCount = spells.size() / SPELLS_PER_GROUP + 1;

    }


    public void tick(){
        if (manager == null) return;

         var newSpells = manager.getAllSpells().stream().map(selectionOption -> selectionOption.spellData).toList();
         var newSelectIndex = manager.getSelectionIndex();

         if (!newSpells.equals(spells) || newSelectIndex != selectIndex) {
            update();
         }
    }

    public List<SpellData> getCurrentSpells(){
        int startIndex = currentGroupIndex * SPELLS_PER_GROUP;
        int endIndex = Math.min(startIndex + SPELLS_PER_GROUP, spells.size()-1);
        return spells.subList(startIndex, endIndex);
    }

    public int getCurrentGroupIndex() {
        return currentGroupIndex;
    }

    /**
     *
     * @param groupIndex 最好为0，最大为size-1
     * @return
     */
    public List<SpellData> getIndexSpells(int groupIndex){
        int startIndex = groupIndex * SPELLS_PER_GROUP;
        int endIndex = Math.min(startIndex + SPELLS_PER_GROUP, spells.size()-1);
        return spells.subList(startIndex, endIndex);
    }

    @Override
    public void add() {
        move(1);
    }

    @Override
    public void less() {
        move(-1);
    }

    @Override
    public void move(int offset) {
        currentGroupIndex = Math.max(0,currentGroupIndex + offset);
        var selectIndex = Math.min(currentGroupIndex * SPELLS_PER_GROUP, spells.size() - 1);
        manager.makeSelection(selectIndex);
    }

    public int getGroupCount() {
        return groupCount;
    }
}
