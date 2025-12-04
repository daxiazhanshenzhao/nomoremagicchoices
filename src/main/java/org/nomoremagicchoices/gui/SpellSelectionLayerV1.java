package org.nomoremagicchoices.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.compat.Curios;
import io.redspace.ironsspellbooks.gui.overlays.SpellBarOverlay;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.nomoremagicchoices.Nomoremagicchoices;
import org.nomoremagicchoices.player.ClientInput;
import org.nomoremagicchoices.player.ModKeyMapping;

import java.util.List;

/**
 * 法术选择栏渲染层
 * 负责在屏幕上渲染法术栏、滚动条和选中的法术图标
 */
public class SpellSelectionLayerV1 implements LayeredDraw.Layer {

    // ========== 纹理资源 ==========
    private static final ResourceLocation TEXTURE = SpellBarOverlay.TEXTURE;
    private static final ResourceLocation SCROLL_BACKGROUND = ResourceLocation.fromNamespaceAndPath(
            Nomoremagicchoices.MODID, "textures/gui/scroll.png");
    private static final ResourceLocation SCROLL_HANDLE = ResourceLocation.fromNamespaceAndPath(
            Nomoremagicchoices.MODID, "textures/gui/scroller.png");

    // ========== 布局常量 ==========
    private static final int SPELLS_PER_ROW = 4;           // 每行显示的法术数量
    private static final int SPELL_SLOT_SIZE = 22;          // 法术槽大小
    private static final int SPELL_ICON_SIZE = 16;          // 法术图标大小
    private static final int SPELL_ICON_OFFSET = 3;         // 图标在槽内的偏移
    private static final int HOTBAR_LEFT_OFFSET = 91;       // 物品栏左边距
    private static final int SPELL_BAR_GAP = 100;           // 法术栏与物品栏的间距
    private static final int SPELL_BAR_VERTICAL_OFFSET = -35; // 法术栏垂直偏移

    // ========== 滚动条常量 ==========
    private static final int SCROLL_WIDTH = 4;              // 滚动条背景宽度
    private static final int SCROLL_HEIGHT = 22;            // 滚动条背景高度
    private static final int SCROLL_HANDLE_WIDTH = 2;       // 滚动条滑块宽度
    private static final int SCROLL_HANDLE_HEIGHT = 7;      // 滚动条滑块高度
    private static final int SCROLL_USABLE_HEIGHT = SCROLL_HEIGHT - SCROLL_HANDLE_HEIGHT - 1; // 滚动条可用高度（减去1像素偏移）

    // ========== 显示时间常量 ==========
    private static final int POST_CAST_DISPLAY_TICKS = 20;  // 施法后显示时长（1秒）
    private static final int SELECTION_DISPLAY_TICKS = 20;  // 选择后显示时长（1秒）

    // ========== 状态追踪 ==========
    private static boolean wasCasting = false;
    private static int castEndTick = 0;
    private static int lastSelectedIndex = -1;
    private static int selectionChangeTick = 0;

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        SpellSelectionManager manager = ClientMagicData.getSpellSelectionManager();
        if (manager.getSpellCount() <= 0) return;

        List<SpellData> allSpells = manager.getAllSpells().stream()
                .map(slot -> slot.spellData)
                .toList();

        int currentGroup = calculateCurrentGroup(manager);
        SpellBarPosition position = calculateSpellBarPosition(guiGraphics);

        // 渲染法术槽
        renderSpellSlots(guiGraphics, manager, allSpells, currentGroup, position);

        // 渲染选中的法术图标
        renderSelectedSpellIcon(guiGraphics, manager, player, position);

        // 渲染滚动条
        renderScrollBar(guiGraphics, manager, allSpells.size(), currentGroup, position);

        // 更新状态追踪
        updateCastingState(player, manager);
    }

    /**
     * 计算当前法术组索引
     */
    private int calculateCurrentGroup(SpellSelectionManager manager) {
        return manager.getSelectionIndex() / SPELLS_PER_ROW;
    }

    /**
     * 计算法术栏位置
     */
    private SpellBarPosition calculateSpellBarPosition(GuiGraphics guiGraphics) {
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        int hotbarLeftX = screenWidth / 2 - HOTBAR_LEFT_OFFSET;
        int spellBarWidth = (SPELLS_PER_ROW - 1) * SPELL_SLOT_SIZE + SPELL_SLOT_SIZE;
        int spellBarX = hotbarLeftX - SPELL_BAR_GAP - spellBarWidth + 76;
        int spellBarY = screenHeight + SPELL_BAR_VERTICAL_OFFSET;

        return new SpellBarPosition(spellBarX, spellBarY, spellBarWidth);
    }

    /**
     * 渲染法术槽
     */
    private void renderSpellSlots(GuiGraphics guiGraphics, SpellSelectionManager manager,
                                  List<SpellData> allSpells, int currentGroup, SpellBarPosition position) {
        int startIndex = currentGroup * SPELLS_PER_ROW;
        int endIndex = Math.min(startIndex + SPELLS_PER_ROW, allSpells.size());

        for (int i = startIndex; i < endIndex; i++) {
            int slotIndex = i - startIndex;
            int slotX = position.x + slotIndex * SPELL_SLOT_SIZE;

            renderSingleSpellSlot(guiGraphics, manager, allSpells.get(i), i, slotX, position.y);
        }
    }

    /**
     * 渲染单个法术槽
     */
    private void renderSingleSpellSlot(GuiGraphics guiGraphics, SpellSelectionManager manager,
                                      SpellData spellData, int index, int x, int y) {
        // 绘制背景框
        guiGraphics.blit(TEXTURE, x, y, 66, 84, SPELL_SLOT_SIZE, SPELL_SLOT_SIZE);

        boolean isFromSpellbook = manager.getAllSpells().get(index).slot.equals(Curios.SPELLBOOK_SLOT);
        int overlayU = 22 + (isFromSpellbook ? 0 : 110);
        guiGraphics.blit(TEXTURE, x, y, overlayU, 84, SPELL_SLOT_SIZE, SPELL_SLOT_SIZE);

        // 绘制法术图标
        if (!spellData.equals(SpellData.EMPTY)) {
            guiGraphics.blit(spellData.getSpell().getSpellIconResource(),
                    x + SPELL_ICON_OFFSET, y + SPELL_ICON_OFFSET,
                    0, 0, SPELL_ICON_SIZE, SPELL_ICON_SIZE, SPELL_ICON_SIZE, SPELL_ICON_SIZE);

            // 绘制冷却遮罩
            renderCooldownOverlay(guiGraphics, spellData, x, y);
        }

        // 渲染按键名称
        if (ClientInput.getHasWeapon()){
            renderKeyName(guiGraphics, index, x, y);
        }

    }

    /**
     * 渲染冷却遮罩
     */
    private void renderCooldownOverlay(GuiGraphics guiGraphics, SpellData spellData, int x, int y) {
        float cooldownPercent = ClientMagicData.getCooldownPercent(spellData.getSpell());
        if (cooldownPercent > 0) {
            RenderSystem.enableBlend();
            int pixels = (int) (SPELL_ICON_SIZE * cooldownPercent + 1.0F);
            guiGraphics.blit(TEXTURE,
                    x + SPELL_ICON_OFFSET,
                    y + SPELL_ICON_OFFSET + SPELL_ICON_SIZE - pixels,
                    47, 87 + SPELL_ICON_SIZE - pixels,
                    SPELL_ICON_SIZE, pixels);
        }
    }

    /**
     * 渲染选中的法术图标
     */
    private void renderSelectedSpellIcon(GuiGraphics guiGraphics, SpellSelectionManager manager,
                                        Player player, SpellBarPosition position) {
        if (!shouldShowSelectedSpell(player)) return;

        SpellData selectedSpell = manager.getSelectedSpellData();
        if (selectedSpell.equals(SpellData.EMPTY)) return;

        int iconX = position.x + position.width + 10 + 100 + 3 - 6;
        int iconY = position.y + 3 - 20;

        guiGraphics.blit(selectedSpell.getSpell().getSpellIconResource(),
                iconX, iconY, 0, 0, SPELL_ICON_SIZE, SPELL_ICON_SIZE, SPELL_ICON_SIZE, SPELL_ICON_SIZE);
    }

    /**
     * 判断是否应该显示选中的法术
     */
    private boolean shouldShowSelectedSpell(Player player) {
        boolean isCasting = ClientMagicData.isCasting();
        boolean isWithinPostCastWindow = (player.tickCount - castEndTick) <= POST_CAST_DISPLAY_TICKS && castEndTick > 0;
        boolean isWithinSelectionWindow = (player.tickCount - selectionChangeTick) <= SELECTION_DISPLAY_TICKS && selectionChangeTick > 0;

        return isCasting || isWithinPostCastWindow || isWithinSelectionWindow;
    }

    /**
     * 渲染滚动条
     */
    private void renderScrollBar(GuiGraphics guiGraphics, SpellSelectionManager manager,
                                 int totalSpells, int currentGroup, SpellBarPosition position) {
        int totalGroups = calculateTotalGroups(totalSpells);
        if (totalGroups <= 1) return;

        int scrollX = position.x + SPELLS_PER_ROW * SPELL_SLOT_SIZE;
        int scrollY = position.y;

        // 绘制滚动条背景
        guiGraphics.blit(SCROLL_BACKGROUND, scrollX, scrollY, 0, 0,
                SCROLL_WIDTH, SCROLL_HEIGHT, SCROLL_WIDTH, SCROLL_HEIGHT);

        // 计算滚动条滑块位置（类似创造模式物品栏）
        int handleY = calculateScrollHandlePosition(currentGroup, totalGroups, scrollY);

        // 绘制滚动条滑块
        guiGraphics.blit(SCROLL_HANDLE, scrollX + 1, handleY, 0, 0,
                SCROLL_HANDLE_WIDTH, SCROLL_HANDLE_HEIGHT,
                SCROLL_HANDLE_WIDTH, SCROLL_HANDLE_HEIGHT);

        // 绘制页码指示
        String pageText = String.valueOf(currentGroup + 1);
        guiGraphics.drawString(Minecraft.getInstance().font, pageText,
                scrollX + 6, scrollY + 7, 0xFFFFFF);
    }

    /**
     * 渲染按键名称
     */
    private void renderKeyName(GuiGraphics guiGraphics, int spellIndex, int x, int y) {
        // 获取当前组内的槽位索引（0-3）
        int slotInGroup = spellIndex % SPELLS_PER_ROW;

        // 获取对应的按键
        String keyName = getKeyNameForSlot(slotInGroup);

        if (keyName != null && !keyName.isEmpty()) {
            var font = Minecraft.getInstance().font;
            int textWidth = font.width(keyName);

            // 在法术槽上方居中渲染按键名称
            int textX = x + (SPELL_SLOT_SIZE - textWidth) / 2;
            int textY = y - 10; // 在槽位上方10像素处

            // 渲染白色文本（带阴影）
            guiGraphics.drawString(font, keyName, textX, textY, 0xFFFFFF, true);
        }
    }

    /**
     * 根据槽位索引获取对应的按键名称
     */
    private String getKeyNameForSlot(int slotIndex) {
        return switch (slotIndex) {
            case 0 -> ModKeyMapping.SKILL_1.get().getTranslatedKeyMessage().getString();
            case 1 -> ModKeyMapping.SKILL_2.get().getTranslatedKeyMessage().getString();
            case 2 -> ModKeyMapping.SKILL_3.get().getTranslatedKeyMessage().getString();
            case 3 -> ModKeyMapping.SKILL_4.get().getTranslatedKeyMessage().getString();
            default -> "";
        };
    }

    /**
     * 计算滚动条滑块位置（动态）
     */
    private int calculateScrollHandlePosition(int currentGroup, int totalGroups, int scrollY) {
        if (totalGroups <= 1) {
            return scrollY + 1;
        }

        // 计算滚动进度 (0.0 到 1.0)
        float scrollProgress = (float) currentGroup / (totalGroups - 1);

        // 将进度映射到可用的滚动范围
        int handleOffset = Math.round(scrollProgress * SCROLL_USABLE_HEIGHT);

        return scrollY + 1 + handleOffset;
    }

    /**
     * 计算总组数
     */
    private int calculateTotalGroups(int totalSpells) {
        return (totalSpells + SPELLS_PER_ROW - 1) / SPELLS_PER_ROW;
    }

    /**
     * 更新��法和选择状态
     */
    private void updateCastingState(Player player, SpellSelectionManager manager) {
        boolean isCasting = ClientMagicData.isCasting();

        // 更新施法状态
        if (isCasting) {
            wasCasting = true;
        } else if (wasCasting) {
            castEndTick = player.tickCount;
            wasCasting = false;
        }

        // 更新选择状态
        int currentSelectedIndex = manager.getSelectionIndex();
        if (currentSelectedIndex != lastSelectedIndex) {
            selectionChangeTick = player.tickCount;
            lastSelectedIndex = currentSelectedIndex;
        }
    }

    /**
     * 获取当前法术组（用于外部调用）
     */
    public static int getCurrentGroup() {
        SpellSelectionManager manager = ClientMagicData.getSpellSelectionManager();
        return manager.getSelectionIndex() / SPELLS_PER_ROW;
    }

    /**
     * 设置当前法术组（用于外部调用）
     * @param groupIndex 目标法术组索引（从0开始）
     */
    public static void setCurrentGroup(int groupIndex) {
        SpellSelectionManager manager = ClientMagicData.getSpellSelectionManager();
        int totalSpells = manager.getSpellCount();
        if (totalSpells <= 0) return;

        // 计算总组数
        int totalGroups = (totalSpells + SPELLS_PER_ROW - 1) / SPELLS_PER_ROW;

        // 限制组索引在有效范围内
        int clampedGroup = Math.max(0, Math.min(groupIndex, totalGroups - 1));

        // 计算该组的第一个法术索引
        int targetIndex = clampedGroup * SPELLS_PER_ROW;

        // 确保索引不超过法术总数
        targetIndex = Math.min(targetIndex, totalSpells - 1);

        // 设置选中的法术索引
        manager.makeSelection(targetIndex);
    }

    /**
     * 切换到下一个法术组（循环）
     * 到达最后一组时回到第一组
     */
    public static void nextGroup() {
        SpellSelectionManager manager = ClientMagicData.getSpellSelectionManager();
        int totalSpells = manager.getSpellCount();
        if (totalSpells <= 0) return;

        int totalGroups = (totalSpells + SPELLS_PER_ROW - 1) / SPELLS_PER_ROW;
        int currentGroup = getCurrentGroup();

        // 循环到下一组，如果是最后一组则回到第一组
        int nextGroup = (currentGroup + 1) % totalGroups;
        setCurrentGroup(nextGroup);
    }

    /**
     * 切换到上一个法术组（循环）
     * 到达第一组时回到最后一组
     */
    public static void previousGroup() {
        SpellSelectionManager manager = ClientMagicData.getSpellSelectionManager();
        int totalSpells = manager.getSpellCount();
        if (totalSpells <= 0) return;

        int totalGroups = (totalSpells + SPELLS_PER_ROW - 1) / SPELLS_PER_ROW;
        int currentGroup = getCurrentGroup();

        // 循环到上一组，如果是第一组则回到最后一组
        int prevGroup = (currentGroup - 1 + totalGroups) % totalGroups;
        setCurrentGroup(prevGroup);
    }

    /**
     * 法术栏位置信息
     */
    private record SpellBarPosition(int x, int y, int width) {}
}
