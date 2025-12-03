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

import java.util.List;

public class SpellSelectionLayerV1 implements LayeredDraw.Layer {

    private static final ResourceLocation TEXTURE = SpellBarOverlay.TEXTURE;
    private static final int SPELLS_PER_GROUP = 4;
    private static final int SPACING = 22;
    private static final int POST_CAST_DISPLAY_TICKS = 20*1; // 1秒 = 20 ticks

    private static int currentGroup = 0;
    private static boolean wasCasting = false;
    private static int castEndTick = 0; // 记录施法结束时的 tick
    private static int lastSelectedIndex = -1; // 记录上次选择的法术索引
    private static int selectionChangeTick = 0; // 记录选择变化时的 tick

    private static final ResourceLocation SCROLL = ResourceLocation.fromNamespaceAndPath(Nomoremagicchoices.MODID, "textures/gui/scroll.png");
    private static final ResourceLocation SCROLLER = ResourceLocation.fromNamespaceAndPath(Nomoremagicchoices.MODID, "textures/gui/scroller.png");



    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        SpellSelectionManager manager = ClientMagicData.getSpellSelectionManager();
        if (manager.getSpellCount() <= 0) return;

        // 计算当前组
        this.currentGroup = manager.getSelectionIndex() / SPELLS_PER_GROUP;

        // 获取当前组的法术
        List<SpellData> allSpells = manager.getAllSpells().stream().map(slot -> slot.spellData).toList();
        int startIndex = currentGroup * SPELLS_PER_GROUP;
        int endIndex = Math.min(startIndex + SPELLS_PER_GROUP, allSpells.size());

        // 计算渲染位置
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        // 计算法术栏的起始位置：距离物品栏左边 100 像素
        // 物品栏左边位置 = screenWidth / 2 - 91 (物品栏宽度182的一半)
        // 法术栏位置 = 物品栏左边 - 100（间距）- totalWidth（法术栏宽度）
        int totalWidth = (SPELLS_PER_GROUP - 1) * SPACING + 22; // 法术栏总宽度
        int hotbarLeftX = screenWidth / 2 - 91; // 物品栏左边位置
        int centerX = hotbarLeftX - 100 - totalWidth+76; // 法术栏左边位置
        int centerY = screenHeight - 55+20;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        // 渲染当前组的法术
        for (int i = startIndex; i < endIndex; i++) {
            int slotIndex = i - startIndex;
            int x = centerX + slotIndex * SPACING;

            // 绘制背景框
            guiGraphics.blit(TEXTURE, x, centerY, 66, 84, 22, 22);
            guiGraphics.blit(TEXTURE, x, centerY, 22 + (!manager.getAllSpells().get(i).slot.equals(Curios.SPELLBOOK_SLOT) ? 110 : 0), 84, 22, 22);
            // 绘制法术图标
            SpellData spellData = allSpells.get(i);
            if (!spellData.equals(SpellData.EMPTY)) {
                guiGraphics.blit(spellData.getSpell().getSpellIconResource(), x + 3, centerY + 3, 0, 0, 16, 16, 16, 16);

                //冷却条
                float cooldownPercent = ClientMagicData.getCooldownPercent(spellData.getSpell());
                if (cooldownPercent > 0) {
                    RenderSystem.enableBlend();
                    int pixels = (int) (16.0F * cooldownPercent + 1.0F);
                    // 从下往上渲染冷却条：调整 Y 坐标和纹理 V 坐标
                    guiGraphics.blit(TEXTURE, x + 3, centerY + 3 + 16 - pixels, 47, 87 + 16 - pixels, 16, pixels);
                }
            }
        }

        // 渲染当前选择的法术（施法时、施法后1秒内、切换选择后20tick内）
        boolean isCasting = ClientMagicData.isCasting();

        // 更新施法状态追踪
        if (isCasting) {
            wasCasting = true;
        } else if (wasCasting) {
            // 施法刚结束，记录当前 tick
            castEndTick = player.tickCount;
            wasCasting = false;
        }

        // 追踪法术选择变化
        int currentSelectedIndex = manager.getSelectionIndex();
        if (currentSelectedIndex != lastSelectedIndex) {
            // 选择发生变化，记录当前 tick
            selectionChangeTick = player.tickCount;
            lastSelectedIndex = currentSelectedIndex;
        }

        // 计算是否在施法后的1秒内（20 ticks）
        boolean isWithinPostCastWindow = (player.tickCount - castEndTick) <= POST_CAST_DISPLAY_TICKS && castEndTick > 0;

        // 计算是否在选择变化后的20 ticks内
        boolean isWithinSelectionChangeWindow = (player.tickCount - selectionChangeTick) <= POST_CAST_DISPLAY_TICKS && selectionChangeTick > 0;
        boolean isCreativeMode = player.isCreative() || player.getAbilities().instabuild;
        if (isCreativeMode) return;

        // 如果正在施法、施法后1秒内或切换选择后20tick内，渲染选中的法术图标
        if (isCasting || isWithinPostCastWindow || isWithinSelectionChangeWindow) {
            SpellData selectedSpell = manager.getSelectedSpellData();
            if (!selectedSpell.equals(SpellData.EMPTY)) {
                // 在法术栏右侧10像素处显示选中的法术
                int selectedSpellX = centerX + totalWidth + 10+100+3-6;
                int selectedSpellY = centerY + 3-20; // 与法术图标垂直对齐
                guiGraphics.blit(selectedSpell.getSpell().getSpellIconResource(),
                    selectedSpellX, selectedSpellY, 0, 0, 16, 16, 16, 16);
            }
        }


        //渲染当前法术栏边框：
        if (true){
            int x = centerX+22*4;
            int y = centerY + currentGroup * 7;


            // 先绘制背景 (假设 scroll.png 的实际大小是 4x21)
            guiGraphics.blit(SCROLL,x,centerY,0,0,4,22,4,22);
            //再绘制条 (假设 scroller.png 的实际大小是 2x6)
            guiGraphics.blit(SCROLLER,x+1,y+1,0,0,2,6,2,6);
            guiGraphics.drawString(Minecraft.getInstance().font, String.valueOf(currentGroup + 1), x + 6, centerY + 8, 0xFFFFFF);
        }


    }
    //SpellBarOverlay
    public void changeGroup(int direction) {
        SpellSelectionManager manager = ClientMagicData.getSpellSelectionManager();
        int groupCount = (manager.getSpellCount() + SPELLS_PER_GROUP - 1) / SPELLS_PER_GROUP;

        if (groupCount <= 1) return;

        this.currentGroup = (this.currentGroup + direction + groupCount) % groupCount;
    }

    public static int getCurrentGroup() {
        return currentGroup;
    }

}
