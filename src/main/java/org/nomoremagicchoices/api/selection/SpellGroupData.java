package org.nomoremagicchoices.api.selection;

import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.neoforged.neoforge.common.NeoForge;
import org.nomoremagicchoices.api.event.ChangeGroupEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SpellGroupData {
    //将法术分为组，能够根据组的index获取法术组数据
    private int currentGroupIndex;
    private static int groupCount;

    // 每组最多包含的法术数量
    private static final int SPELLS_PER_GROUP = 4;

    // 存储所有法术的列表
    private List<SpellData> allSpells;

    // 单例实例
    public static SpellGroupData instance = new SpellGroupData();

    private SpellGroupData() {
        this.currentGroupIndex = 0;
        this.allSpells = new ArrayList<>();
        updateSpells();
    }

    /**
     * 更新法术列表并重新计算分组数量
     */
    public void updateSpells() {
        var ssm = ClientMagicData.getSpellSelectionManager();

        // 从 SpellSelectionManager 获取所有法术
        this.allSpells = ssm.getAllSpells().stream()
                .map(slot -> slot.spellData)
                .collect(Collectors.toList());

        // 计算总共需要多少组（向上取整）
        groupCount = (int) Math.ceil((double) allSpells.size() / SPELLS_PER_GROUP);

        // 确保 groupCount 至少为 0
        if (groupCount < 0) {
            groupCount = 0;
        }

        // 确保当前索引在有效范围内
        if (this.currentGroupIndex >= groupCount && groupCount > 0) {
            this.currentGroupIndex = groupCount - 1;
        } else if (groupCount == 0) {
            this.currentGroupIndex = 0;
        }
    }

    /**
     * 设置当前组索引
     * @param newGroupIndex 新的组索引
     */
    public void setCurrentGroupIndex(int newGroupIndex) {
        ChangeGroupEvent event = new ChangeGroupEvent(this, this.currentGroupIndex, newGroupIndex);

        // 如果事件被取消，直接返回，不修改索引
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
            return;
        }

        int validatedIndex = event.getNewGroup();
        this.currentGroupIndex = Math.clamp(validatedIndex, 0, Math.max(0, groupCount - 1));
    }

    /**
     * 改变组索引（相对变化）
     * @param delta 索引变化量
     */
    public void changeIndex(int delta) {
        setCurrentGroupIndex(this.currentGroupIndex + delta);
    }

    /**
     * 根据组索引获取该组的法术列表（最多4个）
     * @param groupIndex 组索引
     * @return 该组的法术列表，可能为空或不满4个
     */
    public List<SpellData> getSpellsByIndex(int groupIndex) {
        // 索引越界检查
        if (groupIndex < 0 || groupIndex >= groupCount || allSpells.isEmpty()) {
            return new ArrayList<>();
        }

        // 计算起始和结束位置
        int startIndex = groupIndex * SPELLS_PER_GROUP;
        int endIndex = Math.min(startIndex + SPELLS_PER_GROUP, allSpells.size());

        // 返回该组的法术子列表
        return new ArrayList<>(allSpells.subList(startIndex, endIndex));
    }

    /**
     * 获取当前组的法术列表
     * @return 当前组的法术列表
     */
    public List<SpellData> getCurrentGroupSpells() {
        return getSpellsByIndex(this.currentGroupIndex);
    }

    /**
     * 获取所有法术列表
     * @return 所有法术的列表
     */
    public List<SpellData> getAllSpells() {
        return new ArrayList<>(allSpells);
    }

    /**
     * 获取当前组索引
     * @return 当前组索引
     */
    public int getCurrentGroupIndex() {
        return currentGroupIndex;
    }

    /**
     * 获取总组数
     * @return 总组数
     */
    public static int getGroupCount() {
        return groupCount;
    }

    /**
     * 获取每组的法术数量上限
     * @return 每组最多4个法术
     */
    public static int getSpellsPerGroup() {
        return SPELLS_PER_GROUP;
    }
}
