package org.nomoremagicchoices.api.selection;

import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.joml.Vector2i;
import org.nomoremagicchoices.Nomoremagicchoices;
import org.nomoremagicchoices.api.event.ChangeSpellEvent;
import org.nomoremagicchoices.api.init.TagInit;
import org.nomoremagicchoices.gui.component.ScrollSpellWight;
import org.nomoremagicchoices.player.ModKeyMapping;

import java.util.List;

/**
 * 客户端滚动数据管理类
 * 负责管理所有法术Widget的状态、移动和渲染逻辑
 * 所有逻辑都封装在此类中，LayerV2只从这里获取数据进行渲染
 */
@EventBusSubscriber(Dist.CLIENT)
public class ClientScrollData {

    public static Minecraft mc;

    public static SpellSelectionState state = SpellSelectionState.EmptyHand;
    public static final int TOTAL_TICKS = 8;
    public static int cTick = 0;
    public static boolean isRunning = false;


    private static List<ScrollSpellWight> spellWightList;

    static {
        mc = Minecraft.getInstance();
    }

    private ClientScrollData() {
        // 私有构造函数，防止实例化

    }

    /**
     * 每tick调用的主处理方法
     * 处理按键、状态更新和Widget动画
     */
    public static void tickHandle(){
        handleRunning();
        handleCurrentTick();
        handleWightTick();
        handleState();
    }

    /**
     * 处理状态更新
     * 根据玩家手持物品判断当前状态
     */
    private static void handleState() {
        if (mc.player != null) {
            var player = mc.player;
            var mainHand = player.getMainHandItem();

            SpellSelectionState oldState = state;

            if (mainHand.isEmpty() && player.getOffhandItem().isEmpty()) {
                state = SpellSelectionState.EmptyHand;
            } else if (mainHand.is(TagInit.SKILL_WEAPON)) {
                state = SpellSelectionState.Weapon;
            } else if (player.getMainHandItem().has(ComponentRegistry.CASTING_IMPLEMENT) ||
                       player.getOffhandItem().has(ComponentRegistry.CASTING_IMPLEMENT)) {
                state = SpellSelectionState.Staff;
            }

            // 状态切换时的处理
            if (oldState != state) {
                handleStateChange(oldState, state);
            }
        }
    }

    /**
     * 处理状态切换
     * @param oldState 旧状态
     * @param newState 新状态
     */
    private static void handleStateChange(SpellSelectionState oldState, SpellSelectionState newState) {
        if (spellWightList == null || spellWightList.isEmpty()) return;

        Nomoremagicchoices.LOGGER.info("状态切换: " + oldState + " -> " + newState);

        if (newState == SpellSelectionState.EmptyHand) {
            // 切换到空手：所有Widget移动到Down位置，带动画
            List<Vector2i> positions = calculatePositions(newState);

            // 使用moveDown方法为所有Widget触发移动动画
            for (int i = 0; i < spellWightList.size(); i++) {
                ScrollSpellWight wight = spellWightList.get(i);
                Vector2i targetPos = positions.get(i);
                wight.moveDown(targetPos);
                Nomoremagicchoices.LOGGER.info("切换到空手: Widget[" + i + "] moveDown to (" + targetPos.x + ", " + targetPos.y + ")");
            }
        } else {
            // 切换到持有物品：currentGroup已经在列表开头（index=0），直接让它移到Focus位置
            List<Vector2i> positions = calculatePositions(newState);

            // 第一个Widget（currentGroup）移到Focus位置
            if (!spellWightList.isEmpty()) {
                ScrollSpellWight firstWight = spellWightList.getFirst();
                Vector2i focusPos = positions.getFirst();
                firstWight.moveFocus(focusPos);

                Nomoremagicchoices.LOGGER.info("切换到武器/法杖: Widget[0] (groupIndex=" + firstWight.getGroupIndex() +
                    ") moveFocus to (" + focusPos.x + ", " + focusPos.y + ")");

                // 其他Widget保持在Down位置
                for (int i = 1; i < spellWightList.size(); i++) {
                    ScrollSpellWight wight = spellWightList.get(i);
                    Vector2i downPos = positions.get(i);
                    wight.moveDown(downPos);
                    Nomoremagicchoices.LOGGER.info("Widget[" + i + "] (groupIndex=" + wight.getGroupIndex() +
                        ") 保持Down状态，移动到 (" + downPos.x + ", " + downPos.y + ")");
                }
            }
        }
    }

    /**
     * 处理所有Widget的tick更新
     */
    private static void handleWightTick() {
        if (spellWightList == null) return;
        spellWightList.forEach(ScrollSpellWight::tick);
    }

    /**
     * 处理切换组按键
     */
    public static void handleRunning(){
        // 检测向后切换（下一组）
        boolean nextPressed = ModKeyMapping.NEXT_GROUP.get().consumeClick();
        // 检测向前切换（上一组）
        boolean prevPressed = ModKeyMapping.PREV_GROUP.get().consumeClick();
        // 检测R键切换（向后）
        boolean changPressed = ModKeyMapping.CHANG_GROUP.get().consumeClick();

        if ((nextPressed || prevPressed || changPressed) && !isRunning){
            isRunning = true;

            // 确保spellWightList已初始化
            if (spellWightList == null || spellWightList.isEmpty()) {
                Nomoremagicchoices.LOGGER.warn("spellWightList 未初始化，正在初始化...");
                update();
                return;
            }

            // 获取当前组索引
            int currentGroupIndex = SpellGroupData.instance.getCurrentGroupIndex();
            int totalGroups = SpellGroupData.getGroupCount();

            // 确定切换方向
            int nextGroupIndex;
            if (prevPressed) {
                // 向前切换（上一组）
                nextGroupIndex = (currentGroupIndex - 1 + totalGroups) % totalGroups;
                Nomoremagicchoices.LOGGER.info("向前切换组");
            } else {
                // 向后切换（下一组）- R键或右箭头
                nextGroupIndex = (currentGroupIndex + 1) % totalGroups;
                Nomoremagicchoices.LOGGER.info("向后切换组");
            }

            SpellGroupData.instance.setCurrentGroupIndex(nextGroupIndex);
            Nomoremagicchoices.LOGGER.info("切换组: " + currentGroupIndex + " -> " + nextGroupIndex);

            // 执行切换动画
            switchToGroup(nextGroupIndex);
        }
    }

    /**
     * 切换到指定组
     * 将当前在第一位的组放回原位置，将目标组抽到第一位
     *
     * @param targetGroupIndex 目标组的groupIndex
     */
    private static void switchToGroup(int targetGroupIndex) {
        if (spellWightList == null || spellWightList.isEmpty()) {
            return;
        }

        // 找到目标组在列表中的位置
        int targetListIndex = -1;
        for (int i = 0; i < spellWightList.size(); i++) {
            if (spellWightList.get(i).getGroupIndex() == targetGroupIndex) {
                targetListIndex = i;
                break;
            }
        }

        if (targetListIndex != -1) {
            // 计算目标位置
            List<Vector2i> positions = calculatePositions(state);

            // 执行抽书操作：将目标组抽到第一位
            boolean isEmptyHand = (state == SpellSelectionState.EmptyHand);
            ScrollGroupHelper.drawWight(spellWightList, targetListIndex, positions, isEmptyHand);

            Nomoremagicchoices.LOGGER.info("执行切换动画: 将groupIndex=" + targetGroupIndex + " 从listIndex=" + targetListIndex + " 抽到第一位");
        } else {
            Nomoremagicchoices.LOGGER.warn("未找到目标组在列表中的位置: groupIndex=" + targetGroupIndex);
        }
    }

    /**
     * 处理当前tick计数
     */
    public static void handleCurrentTick(){
        if (isRunning){
            cTick++;
            if (cTick >= TOTAL_TICKS) {
                cTick = 0; // 达到8后归0
                isRunning = false; // 允许下次触发
            }
        }
    }

    /**
     * 更新Widget列表
     * 每次法术槽位变化时调用
     *
     * 初始化逻辑：
     * 1. 创建所有组的Widget，按groupIndex顺序
     * 2. 将currentGroup移到列表末尾（这样它会显示在第一排）
     * 3. 根据当前状态设置Widget的初始状态
     */
    public static List<ScrollSpellWight> update(){
        Nomoremagicchoices.LOGGER.info("开始初始化 Widget 列表");

        SpellGroupData groupData = getSpellGroupData();
        groupData.updateSpells();

        int groupCount = SpellGroupData.getGroupCount();

        if (groupCount == 0) {
            Nomoremagicchoices.LOGGER.warn("没有法术组，Widget列表为空");
            spellWightList = NonNullList.create();
            return spellWightList;
        }

        // 创建临时列表来存储所有Widget
        List<ScrollSpellWight> tempList = NonNullList.withSize(groupCount, ScrollSpellWight.EMPTY);

        // 计算初始位置
        int screenWidth = mc != null ? mc.getWindow().getGuiScaledWidth() : 1920;
        int screenHeight = mc != null ? mc.getWindow().getGuiScaledHeight() : 1080;
        List<Vector2i> initialPositions = calculatePositionsInternal(groupCount, screenWidth, screenHeight, state);

        // 获取当前组索引
        int currentGroupIndex = groupData.getCurrentGroupIndex();

        // 先创建所有Widget（按groupIndex顺序）
        for (int i = 0; i < groupCount; i++) {
            List<SpellData> groupSpells = groupData.getSpellsByIndex(i);
            Vector2i pos = initialPositions.get(i);

            ScrollSpellWight wight = ScrollSpellWight.create(pos.x, pos.y, groupSpells, i);
            tempList.set(i, wight);
        }

        // 将currentGroup放到列表开头（显示在最上面）
        spellWightList = NonNullList.withSize(groupCount, ScrollSpellWight.EMPTY);

        // 第一个位置：当前组（显示在最上面）
        spellWightList.set(0, tempList.get(currentGroupIndex));

        // 后续位置：其他组（保持相对顺序）
        int insertIndex = 1;
        for (int i = 0; i < groupCount; i++) {
            if (i != currentGroupIndex) {
                spellWightList.set(insertIndex, tempList.get(i));
                insertIndex++;
            }
        }


        // 设置初始状态
        for (int i = 0; i < spellWightList.size(); i++) {
            ScrollSpellWight wight = spellWightList.get(i);

            if (state == SpellSelectionState.EmptyHand) {
                wight.down();
            } else if (i == 0) {
                // 第一个（currentGroup）在Focus状态
                wight.focus();
            } else {
                wight.down();
            }
        }

        Nomoremagicchoices.LOGGER.info("初始化了 " + groupCount + " 个法术组Widget，currentGroup=" + currentGroupIndex + " 在列表开头（index=0）");

        return spellWightList;
    }

    /**
     * 计算所有Widget的目标位置
     * 坐标计算逻辑
     *
     * @param currentState 当前状态
     * @return 位置列表
     */
    public static List<Vector2i> calculatePositions(SpellSelectionState currentState) {
        if (spellWightList == null) {
            return List.of();
        }

        int screenWidth = mc != null ? mc.getWindow().getGuiScaledWidth() : 1920;
        int screenHeight = mc != null ? mc.getWindow().getGuiScaledHeight() : 1080;

        return calculatePositionsInternal(spellWightList.size(), screenWidth, screenHeight, currentState);
    }

    /**
     * 内部坐标计算方法
     * 根据不同状态计算Widget的位置
     *
     * 排列规则：
     * - 空手状态：第一个Widget（index=0, currentGroup）在最上面，其他Widget从上到下依次排列
     * - 持有物品状态：第一个Widget（index=0, currentGroup）在Focus位置（顶部焦点），其他在底部Down位置
     *
     * @param count Widget数量
     * @param screenWidth 屏幕宽度
     * @param screenHeight 屏幕高度
     * @param currentState 当前状态
     * @return 位置列表
     */
    private static List<Vector2i> calculatePositionsInternal(int count, int screenWidth, int screenHeight, SpellSelectionState currentState) {
        List<Vector2i> positions = NonNullList.withSize(count, new Vector2i(0, 0));

        // 基础坐标：屏幕中心偏右下
        int baseX = screenWidth / 2 + 50;
        int baseY = screenHeight / 2 + 20;

        if (currentState == SpellSelectionState.EmptyHand) {
            // 空手模式：第一个Widget（currentGroup）在最上面（baseY），其他Widget依次向下排列
            for (int i = 0; i < count; i++) {
                int y = baseY + (i * org.nomoremagicchoices.gui.SpellSelectionLayerV2.WIDGET_VERTICAL_SPACING);
                positions.set(i, new Vector2i(baseX, y));
            }
        } else {
            // 持有物品模式：第一个（currentGroup）在顶部Focus位置，其他在底部Down位置
            // 第一个在顶部焦点位置
            int focusY = baseY + org.nomoremagicchoices.gui.SpellSelectionLayerV2.FOCUS_Y_OFFSET;
            positions.set(0, new Vector2i(baseX, focusY));

            // 其他Widget在底部
            for (int i = 1; i < count; i++) {
                int y = baseY + ((i - 1) * org.nomoremagicchoices.gui.SpellSelectionLayerV2.WIDGET_VERTICAL_SPACING);
                positions.set(i, new Vector2i(baseX, y));
            }
        }

        return positions;
    }

    /**
     * 获取法术组数据管理器
     */
    public static SpellGroupData getSpellGroupData(){
        return SpellGroupData.instance;
    }

    /**
     * 获取Widget列表
     * 如果未初始化则自动初始化
     */
    public static List<ScrollSpellWight> getSpellWightList() {
        if (spellWightList == null) {
            update();
        }
        return spellWightList;
    }

    /**
     * 获取当前状态
     */
    public static SpellSelectionState getState() {
        return state;
    }

    /**
     * 处理法术变更事件
     * 当法术槽位发生变化时重新初始化
     */
    @SubscribeEvent
    public static void updateHandle(ChangeSpellEvent event){
        update();
    }

    /**
     * 处理组切换事件
     * 当SpellGroupData触发组切换时，执行Widget的移动动画
     */
    @SubscribeEvent
    public static void onChangeGroup(org.nomoremagicchoices.api.event.ChangeGroupEvent event) {
        if (spellWightList == null || spellWightList.isEmpty()) {
            return;
        }

        int newGroupIndex = event.getNewGroup();
        Nomoremagicchoices.LOGGER.info("收到组切换事件: " + event.getOldGroup() + " -> " + newGroupIndex);

        // 使用统一的切换方法
        switchToGroup(newGroupIndex);
    }
}

