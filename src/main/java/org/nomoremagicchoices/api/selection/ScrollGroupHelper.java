package org.nomoremagicchoices.api.selection;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.joml.Vector2i;
import org.nomoremagicchoices.Nomoremagicchoices;
import org.nomoremagicchoices.api.event.ChangeGroupEvent;
import org.nomoremagicchoices.gui.component.ScrollSpellWight;

import java.util.List;

/**
 * 法术组滚动助手 - 负责处理"抽书"和"塞书"的逻辑
 * 类比：将法术组想象成一叠书，可以从中间抽出某本书到最上面，或将最上面的书插入到指定位置
 */
@EventBusSubscriber
public class ScrollGroupHelper {

    /**
     * 抽书操作：将指定index的法术组抽出到列表开头（顶层显示）
     * 就像从书堆中抽出一本书放到最上面，其他书自然下落
     *
     * @param wightList 法术Widget列表
     * @param listIndex 要抽出的Widget在列表中的索引
     * @param positions 计算好的目标位置列表
     * @param isEmptyHand 是否为空手状态
     */
    public static void drawWight(List<ScrollSpellWight> wightList,
                                  int listIndex,
                                  List<Vector2i> positions,
                                  boolean isEmptyHand) {
        if (wightList == null || listIndex < 0 || listIndex >= wightList.size() || positions == null) {
            Nomoremagicchoices.LOGGER.warn("drawWight参数无效: wightList=" + (wightList != null) + ", listIndex=" + listIndex + ", positions=" + (positions != null));
            return;
        }

        Nomoremagicchoices.LOGGER.info("开始抽书操作: listIndex=" + listIndex + ", isEmptyHand=" + isEmptyHand);

        // 获取要抽出的Widget
        ScrollSpellWight targetWight = wightList.get(listIndex);

        if (targetWight == null || targetWight == ScrollSpellWight.EMPTY) {
            Nomoremagicchoices.LOGGER.warn("目标Widget为空或EMPTY，取消操作");
            return;
        }

        // 将目标Widget从原位置移除，后面的元素前移
        for (int i = listIndex; i > 0; i--) {
            wightList.set(i, wightList.get(i - 1));
        }

        // 将目标Widget放到列表开头（index=0，显示在最上面）
        wightList.set(0, targetWight);

        // 执行移动动画
        if (isEmptyHand) {
            // 空手模式：所有Widget都使用moveDown方法，保持Down状态相对移动
            for (int i = 0; i < wightList.size(); i++) {
                ScrollSpellWight wight = wightList.get(i);
                Vector2i targetPos = positions.get(i);
                wight.moveDown(targetPos);
                Nomoremagicchoices.LOGGER.info("Widget[" + i + "] (groupIndex=" + wight.getGroupIndex() + ") moveDown to (" + targetPos.x + ", " + targetPos.y + ")");
            }
        } else {
            // 持有物品模式：第一个（index=0）移到Focus位置
            ScrollSpellWight topWight = wightList.getFirst();
            Vector2i focusPos = positions.getFirst();
            topWight.moveFocus(focusPos);
            Nomoremagicchoices.LOGGER.info("顶部Widget[0] (groupIndex=" + topWight.getGroupIndex() + ") moveFocus to (" + focusPos.x + ", " + focusPos.y + ")");

            // 其他Widget移到Down位置
            for (int i = 1; i < wightList.size(); i++) {
                ScrollSpellWight wight = wightList.get(i);
                Vector2i targetPos = positions.get(i);
                wight.moveDown(targetPos);
            }
        }
    }

    /**
     * 塞书操作：将末尾的法术组插入到指定位置
     * 就像将最上面的书插入到书堆的某个位置，该位置之后的书向后移动
     *
     * @param wightList 法术Widget列表
     * @param listIndex 要插入的目标位置索引
     * @param positions 计算好的目标位置列表
     */
    public static void addWight(List<ScrollSpellWight> wightList,
                                int listIndex,
                                List<Vector2i> positions) {
        if (wightList == null || listIndex < 0 || listIndex >= wightList.size() || positions == null) {
            Nomoremagicchoices.LOGGER.warn("addWight参数无效");
            return;
        }

        Nomoremagicchoices.LOGGER.info("开始塞书操作: listIndex=" + listIndex);

        // 获取列表末尾的Widget（当前在顶层的）
        ScrollSpellWight lastWight = wightList.get(wightList.size() - 1);

        if (lastWight == null || lastWight == ScrollSpellWight.EMPTY) {
            Nomoremagicchoices.LOGGER.warn("末尾Widget为空或EMPTY，取消操作");
            return;
        }

        // 将listIndex位置及之后的元素后移
        for (int i = wightList.size() - 1; i > listIndex; i--) {
            wightList.set(i, wightList.get(i - 1));
        }

        // 将末尾Widget插入到目标位置
        wightList.set(listIndex, lastWight);

        // 执行移动动画 - 塞书时所有Widget都保持Down状态移动
        for (int i = 0; i < wightList.size(); i++) {
            ScrollSpellWight wight = wightList.get(i);
            Vector2i targetPos = positions.get(i);
            wight.moveDown(targetPos);
            Nomoremagicchoices.LOGGER.info("Widget[" + i + "] (groupIndex=" + wight.getGroupIndex() + ") moveDown to (" + targetPos.x + ", " + targetPos.y + ")");
        }
    }

    /**
     * 处理切换组事件
     * 当玩家切换法术组时触发
     */
    @SubscribeEvent
    public static void handleChangeGroup(ChangeGroupEvent event) {
        int oldIndex = event.getOldGroup();
        int newIndex = event.getNewGroup();

        Nomoremagicchoices.LOGGER.info("检测到切换组事件: " + oldIndex + " -> " + newIndex);

        // 实际的切换逻辑由ClientScrollData处理
        // 这里只是记录日志
    }
}

