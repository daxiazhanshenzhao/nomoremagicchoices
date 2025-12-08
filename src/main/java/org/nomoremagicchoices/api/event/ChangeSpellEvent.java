package org.nomoremagicchoices.api.event;

import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;
import java.util.List;

/**
 * 法术的数量变更或者排序变更会触发事件
 * 客户端触发
 * @see Dist#CLIENT
 */
public class ChangeSpellEvent extends Event {

    private final int oldCount;
    private final int newCount;

    public ChangeSpellEvent(int oldCount, int newCount) {
        this.oldCount = oldCount;
        this.newCount = newCount;
    }

    public int getOldCount() {
        return oldCount;
    }

    public int getNewCount() {
        return newCount;
    }

    /**
     * 法术数量和排序监听器 - 用于检测法术数量变化和排序变化
     * 需要在客户端tick时调用 tick() 方法来检测变化
     */
    public static class SpellCountMonitor {

        private static List<SpellSelectionManager.SelectionOption> lastSpellList = new ArrayList<>();
        private static boolean initialized = false;

        /**
         * 初始化监听器
         */
        public static void initialize() {
            if (!initialized) {
                try {
                    SpellSelectionManager manager = ClientMagicData.getSpellSelectionManager();
                    if (manager != null) {
                        lastSpellList = new ArrayList<>(manager.getAllSpells());
                        initialized = true;
                    }
                } catch (Exception e) {
                    // 如果获取失败，下次tick再试
                    initialized = false;
                }
            }
        }

        /**
         * 每tick调用此方法检测法术数量和排序变化
         * 如果检测到变化，会触发 ChangeSpellEvent 事件
         */
        public static void tick() {
            // 如果未初始化，先尝试初始化
            if (!initialized) {
                initialize();
                return;
            }

            try {
                SpellSelectionManager manager = ClientMagicData.getSpellSelectionManager();
                if (manager == null) {
                    return;
                }

                var currentSpellList = manager.getAllSpells();

                // 使用 equals 检测列表变化（包括数量和排序）
                if (!currentSpellList.equals(lastSpellList)) {
                    int oldCount = lastSpellList.size();
                    int newCount = currentSpellList.size();

                    ChangeSpellEvent event = new ChangeSpellEvent(oldCount, newCount);
                    NeoForge.EVENT_BUS.post(event);

                    // 更新上次记录的列表
                    lastSpellList = new ArrayList<>(currentSpellList);
                }
            } catch (Exception e) {
                // 发生异常时重置初始化状态
                initialized = false;
            }
        }
    }
}
