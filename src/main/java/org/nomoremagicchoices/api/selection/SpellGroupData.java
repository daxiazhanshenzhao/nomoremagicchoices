package org.nomoremagicchoices.api.selection;

import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.neoforged.neoforge.common.NeoForge;
import org.nomoremagicchoices.api.event.ChangeGroupEvent;

import java.util.List;

public class SpellGroupData {
    //将法术分为组，能够根据组的index获取法术组数据
    private int currentGroupIndex;
    private static int groupCount;

    private List<SpellData> spellsMax4;

    public void setCurrentGroupIndex(int currentGroupIndex) {
        ChangeGroupEvent event = new ChangeGroupEvent(this, this.currentGroupIndex, currentGroupIndex);

        // 如果事件被取消，直接返回，不修改索引
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
            return;
        }

        int newIndex = event.getNewGroup();
        this.currentGroupIndex = Math.clamp(newIndex, 0, this.groupCount);
    }


    public void changeIndex(int delta) {
        setCurrentGroupIndex(this.currentGroupIndex + delta);
    }

    public SpellGroupData() {
        var ssm = ClientMagicData.getSpellSelectionManager();
        var spellIndex = ssm.getSelectionIndex();
        var spells = ssm.getAllSpells();
    }


    public List<SpellData> getSpellsByIndex(){
        spellsMax4.clear();




        return spellsMax4;
    }

    public static int getGroupCount() {
        return groupCount;
    }
}
