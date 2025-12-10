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
     * Draws a widget to the front of the list (top layer display).
     *
     * @param wightList Widget list
     * @param listIndex Index of the widget to draw
     * @param positions Calculated target positions
     * @param isFocusState Whether in focus state (Staff/Spellbook)
     */
    public static void drawWight(List<ScrollSpellWight> wightList,
                                  int listIndex,
                                  List<Vector2i> positions,
                                  boolean isFocusState) {
        if (wightList == null || listIndex < 0 || listIndex >= wightList.size() || positions == null) {
            return;
        }

        ScrollSpellWight targetWight = wightList.get(listIndex);
        if (targetWight == null || targetWight == ScrollSpellWight.EMPTY) {
            return;
        }

        // Move target widget to the front
        for (int i = listIndex; i > 0; i--) {
            wightList.set(i, wightList.get(i - 1));
        }
        wightList.set(0, targetWight);

        // Execute movement animation
        if (isFocusState) {
            // Focus state: first widget moves to focus position, others move down
            wightList.getFirst().moveFocus(positions.getFirst());
            for (int i = 1; i < wightList.size(); i++) {
                wightList.get(i).moveDown(positions.get(i));
            }
        } else {
            // Non-focus state: all widgets move down
            for (int i = 0; i < wightList.size(); i++) {
                wightList.get(i).moveDown(positions.get(i));
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


        // 实际的切换逻辑由ClientScrollData处理
        // 这里只是记录日志
    }
}

