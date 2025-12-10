package org.nomoremagicchoices.api.selection;

import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import org.nomoremagicchoices.Nomoremagicchoices;
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

    // 记录上一次非施法状态下的选择索引，用于避免施法期间的错误同步
    private int lastValidSelectionIndex = -1;

    // 标记是否已经初始化过选择索引
    private boolean selectionInitialized = false;

    // 记录上一次的施法状态
    private boolean wasCasting = false;

    // 记录施法开始时的索引
    private int indexBeforeCasting = -1;

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

        // SpellSelectionManager可能在游戏初始化早期为null，需要检查
        if (ssm == null) return;

        // 从 SpellSelectionManager 获取所有法术
        // 过滤掉null元素，避免NullPointerException
        var allSpellsList = ssm.getAllSpells();
        if (allSpellsList == null) {
            this.allSpells = new ArrayList<>();
            groupCount = 0;
            this.currentGroupIndex = 0;
            return;
        }

        this.allSpells = allSpellsList.stream()
                .filter(slot -> slot != null && slot.spellData != null && !slot.spellData.equals(SpellData.EMPTY))
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

        // 重置选择初始化标记，允许下一次同步重新初始化
        selectionInitialized = false;
        lastValidSelectionIndex = -1;
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

        int oldIndex = this.currentGroupIndex;
        int validatedIndex = event.getNewGroup();
        this.currentGroupIndex = Math.clamp(validatedIndex, 0, Math.max(0, groupCount - 1));

        // 如果索引确实改变了，自动选中新组的第一个法术
        if (oldIndex != this.currentGroupIndex) {
            selectFirstSpellOfCurrentGroup();
        }
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

    /**
     * 将当前组的第一个法术设置为Iron's Spellbooks的选中法术
     * 此方法会查找当前组第一个法术在SpellSelectionManager中的索引，
     * 并调用makeSelection将其设置为当前选中的法术
     *
     * @return 如果成功设置返回true，否则返回false
     */
    public boolean selectFirstSpellOfCurrentGroup() {
        // 获取当前组的法术列表
        List<SpellData> currentGroupSpells = getCurrentGroupSpells();

        // 检查当前组是否有法术
        if (currentGroupSpells.isEmpty()) {
            return false;
        }

        // 获取第一个法术
        SpellData firstSpell = currentGroupSpells.getFirst();

        // 检查法术是否为空
        if (firstSpell == null || firstSpell.equals(SpellData.EMPTY)) {
            return false;
        }

        // 获取SpellSelectionManager
        var spellSelectionManager = ClientMagicData.getSpellSelectionManager();

        // SpellSelectionManager可能在游戏初始化早期为null
        if (spellSelectionManager == null) {
            return false;
        }

        // 查找第一个法术在所有法术列表中的索引
        int targetIndex = -1;
        for (int i = 0; i < allSpells.size(); i++) {
            SpellData spellData = allSpells.get(i);
            if (spellData != null && spellData.equals(firstSpell)) {
                targetIndex = i;
                break;
            }
        }

        // 如果找到了索引，调用makeSelection
        if (targetIndex >= 0) {
            spellSelectionManager.makeSelection(targetIndex);
            return true;
        }

        return false;
    }

    /**
     * 检测并同步当前选中的法术到对应的组
     * 当玩家通过SpellWheelOverlay或其他方式改变makeSelection时，
     * 这个方法会检测到变化并自动切换currentGroup到选中法术所在的组
     *
     * 关键：施法期间不同步索引变化，因为施法时getSelectionIndex()返回正在释放的法术索引，
     * 而不是玩家实际选择的索引，这会导致错误的组切换
     *
     * @return 如果检测到变化并成功切换组返回true，否则返回false
     */
    public boolean syncGroupFromSelection() {
        // 获取SpellSelectionManager
        var spellSelectionManager = ClientMagicData.getSpellSelectionManager();

        // SpellSelectionManager可能在游戏初始化早期为null
        if (spellSelectionManager == null) {
            return false;
        }

        // 获取当前选中的法术索引
        int selectedIndex = spellSelectionManager.getSelectionIndex();
        boolean isCasting = ClientMagicData.isCasting();

        // 如果索引无效，返回false
        if (selectedIndex < 0 || selectedIndex >= allSpells.size()) {
            wasCasting = isCasting;
            return false;
        }

        // 初始化检查：第一次调用时直接记录当前索引，不执行切换
        if (!selectionInitialized) {
            // 如果正在施法，延迟初始化，避免记录错误的索引
            if (isCasting) {
                wasCasting = true;
                return false;
            }
            lastValidSelectionIndex = selectedIndex;
            selectionInitialized = true;
            wasCasting = false;
            return false;
        }

        // 关键保护：施法状态跟踪
        // 如果从非施法状态进入施法状态，记录施法前的索引
        if (!wasCasting && isCasting) {
            indexBeforeCasting = lastValidSelectionIndex;
        }

        // 如果从施法状态结束，并且索引发生了变化
        // 这种变化很可能是Iron's Spellbooks的内部行为，不是玩家主动选择
        if (wasCasting && !isCasting) {
            // 如果施法结束后索引变化了，忽略这个变化
            if (selectedIndex != indexBeforeCasting) {
                // 恢复施法前的索引记录，不切换组
                lastValidSelectionIndex = indexBeforeCasting;
                wasCasting = false;
                indexBeforeCasting = -1;
                return false;
            }
        }

        // 更新施法状态
        wasCasting = isCasting;

        // 先计算目标组，用于更精确的判断
        int targetGroupIndex = selectedIndex / SPELLS_PER_GROUP;
        int lastGroupIndex = lastValidSelectionIndex >= 0 ? lastValidSelectionIndex / SPELLS_PER_GROUP : -1;

        // 检查索引是否真正改变了
        // 如果索引和上次记录的一样，说明没有真正的选择变化
        if (lastValidSelectionIndex == selectedIndex) {
            return false;
        }

        // 关键保护1：如果正在施法，索引变化是因为施法，不是玩家主动选择
        // 忽略这个变化，不更新lastValidSelectionIndex，也不切换组
        if (isCasting) {
            return false;
        }

        // 关键保护2：如果索引变化了，但目标组和当前组相同
        // 说明这可能是组内法术切换或施法结束后的索引恢复，不应该触发组切换
        if (targetGroupIndex == this.currentGroupIndex) {
            // 更新记录的索引，但不切换组
            lastValidSelectionIndex = selectedIndex;
            return false;
        }

        // 关键保护3：如果上次记录的组索引和目标组索引相同
        // 说明玩家只是在同一组内切换法术，不应该触发组切换
        if (lastGroupIndex == targetGroupIndex) {
            // 更新记录的索引，但不切换组
            lastValidSelectionIndex = selectedIndex;
            return false;
        }

        // 只有在非施法状态下，且索引变化导致组变化时，才被认为是玩家的主动选择
        // 更新记录的索引
        lastValidSelectionIndex = selectedIndex;

        // 切换到目标组（不触发selectFirstSpellOfCurrentGroup，避免循环）
        ChangeGroupEvent event = new ChangeGroupEvent(this, this.currentGroupIndex, targetGroupIndex);

        // 如果事件被取消，返回false
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
            return false;
        }

        int validatedIndex = event.getNewGroup();
        this.currentGroupIndex = Math.clamp(validatedIndex, 0, Math.max(0, groupCount - 1));

        return true;

    }

}
