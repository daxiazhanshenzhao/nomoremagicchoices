package org.nomoremagicchoices.api.selection;

import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.core.NonNullList;

import java.util.List;

/**
 * 法术分组数据管理类
 * 负责将法术列表按4个一组进行分组管理
 */
public class SpellGroupData {
    
    // 从0开始
    private static int currentGroupIndex = 0;
    private static int groupCount = 0;
    private static int globalSelectionIndex = 0;
    
    private static List<SpellData> spells = NonNullList.withSize(1, SpellData.EMPTY);
    private static SpellSelectionManager manager;
    
    // 每组最多包含的法术数量
    private static final int SPELLS_PER_GROUP = 4;
    
    /**
     * 初始化法术分组数据
     */
    public static void init(SpellSelectionManager spellManager) {
        manager = spellManager;
        if (manager !=null){
            spells = manager.getAllSpells().stream()
                    .map(selectionOption -> selectionOption.spellData)
                    .toList();
        }

        update();
    }
    
    /**
     * 更新所有数据（法术列表、选择索引、组索引等）
     */
    public static void update() {
        if (manager == null) return;
        
        // 更新法术列表
        spells = manager.getAllSpells().stream()
                .map(selectionOption -> selectionOption.spellData)
                .toList();
        
        // 更新全局选择索引
        globalSelectionIndex = manager.getGlobalSelectionIndex();
        
        // 计算当前组索引
        currentGroupIndex = globalSelectionIndex / SPELLS_PER_GROUP;
        
        // 计算总组数（向上取整）
        groupCount = (spells.size() + SPELLS_PER_GROUP - 1) / SPELLS_PER_GROUP;
        
        // 更新滚动部件数据
        var scrollData = ClientData.getScrollWightData();
        scrollData.update();
    }
    
    /**
     * 每tick调用，检查选择索引变化并更新
     */
    public static void tick() {
        if (manager == null) return;
        manager = ClientMagicData.getSpellSelectionManager();
        var newSelectionIndex = manager.getSelectionIndex();
        
        // 检查选择索引是否变化
        if (newSelectionIndex != globalSelectionIndex) {
            update();
        }
    }
    
    /**
     * 获取当前组的法术列表
     */
    public static List<SpellData> getCurrentSpells() {
        if (currentGroupIndex < 0 || currentGroupIndex >= groupCount) {
            return NonNullList.withSize(0, SpellData.EMPTY);
        }
        
        int startIndex = currentGroupIndex * SPELLS_PER_GROUP;
        int endIndex = Math.min(startIndex + SPELLS_PER_GROUP, spells.size());
        
        // 确保起始索引不超过法术列表大小
        if (startIndex >= spells.size()) {
            return NonNullList.withSize(0, SpellData.EMPTY);
        }
        
        return spells.subList(startIndex, endIndex);
    }
    
    /**
     * 获取指定组的法术列表
     * @param groupIndex 组索引（0到groupCount-1）
     */
    public static List<SpellData> getIndexSpells(int groupIndex) {
        if (groupIndex < 0 || groupIndex >= groupCount) {
            return NonNullList.withSize(0, SpellData.EMPTY);
        }
        
        int startIndex = groupIndex * SPELLS_PER_GROUP;
        int endIndex = Math.min(startIndex + SPELLS_PER_GROUP, spells.size());
        
        // 确保起始索引不超过法术列表大小
        if (startIndex >= spells.size()) {
            return NonNullList.withSize(0, SpellData.EMPTY);
        }
        
        return spells.subList(startIndex, endIndex);
    }
    
    /**
     * 切换到下一组
     */
    public static void add() {
        move(1);
    }
    
    /**
     * 切换到上一组
     */
    public static void less() {
        move(-1);
    }
    
    /**
     * 移动指定偏移量的组
     * @param offset 偏移量（正数下一组，负数上一组）
     */
    public static void move(int offset) {
        int newGroupIndex = currentGroupIndex + offset;
        
        // 处理循环组索引
        if (groupCount > 0) {
            while (newGroupIndex < 0) {
                newGroupIndex += groupCount;
            }
            newGroupIndex = newGroupIndex % groupCount;
        } else {
            newGroupIndex = 0;
        }
        
        currentGroupIndex = newGroupIndex;
        
        // 计算新的选择索引并通知管理器
        int newSelectionIndex = Math.min(currentGroupIndex * SPELLS_PER_GROUP, spells.size() - 1);
        manager.makeSelection(newSelectionIndex);
        
        // 立即更新确保状态同步
        update();
    }
    
    /**
     * 获取当前组索引
     */
    public static int getCurrentGroupIndex() {
        return currentGroupIndex;
    }
    
    /**
     * 获取总组数
     */
    public static int getGroupCount() {
        return groupCount;
    }
    
    /**
     * 获取当前组内的相对选择索引（0-3）
     */
    public static int getSelectIndex() {
        if (groupCount == 0 || SPELLS_PER_GROUP == 0) {
            return 0;
        }
        return globalSelectionIndex % SPELLS_PER_GROUP;
    }


}
