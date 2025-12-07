package org.nomoremagicchoices.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.gui.overlays.SpellBarOverlay;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import org.nomoremagicchoices.Nomoremagicchoices;
import org.nomoremagicchoices.api.selection.ILayerState;
import org.nomoremagicchoices.api.selection.SpellSelectionState;
import org.nomoremagicchoices.gui.component.ScrollSpellWight;

import java.util.HashMap;
import java.util.List;

public class SpellSelectionLayerV2 implements ILayerState {

    private SpellSelectionState state = SpellSelectionState.EmptyHand;
    private int screenWidth;
    private int screenHeight;

    private SpellSelectionManager manager;
    private List<SpellData> spells;
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Nomoremagicchoices.MODID, "textures/gui/icons.png");

    /**
     * 法术选中状态映射表
     * <p>
     * 用于记录每个法术是否为当前被选中的4个法术之一。
     * 该映射在每次渲染时更新，用于区分已选中和未选中的法术显示效果。
     * </p>
     *
     * @key {@link SpellData} 法术数据对象
     * @value {@link Boolean} 选中状态：{@code true} 表示该法术已被选中，{@code false} 表示未被选中
     */
    private static HashMap<SpellData, Boolean> selectSpell = new HashMap<>();

    @Override
    public SpellSelectionState getSpellSelectionState() {
        return state;
    }

    @Override
    public void setSpellSelectionState(SpellSelectionState spellSelectionState) {
        this.state = spellSelectionState;
    }


    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {

        init(guiGraphics);


        // 检查法术列表是否为空
        if (spells == null || spells.isEmpty()) {
            return; // 没有法术时直接返回，不绘制
        }

        int centerX = screenWidth / 2 - 19*10-2;
        int centerY = screenHeight-34-1;

        updateSelectSpell();

        renderBg(guiGraphics, centerX-8, centerY-2);
        if (state.equals(SpellSelectionState.EmptyHand)) {
            renderAllSpell(spells, guiGraphics, centerX, centerY, false);
        }
        if (state.equals(SpellSelectionState.Weapon)){
            renderAllSpellWithWeapon(selectSpell, guiGraphics, centerX, centerY);



        }







    }


    /**
     * 每tick刷新
     * 计算屏幕宽高
     * 计算centerX，Y坐标
     *
     *
     *
     */
    public void init(GuiGraphics guiGraphics){
        this.screenHeight = guiGraphics.guiHeight();
        this.screenWidth = guiGraphics.guiWidth();
        this.manager = ClientMagicData.getSpellSelectionManager();
        this.spells = manager.getAllSpells().stream().map(s -> s.spellData).toList();
        spells.stream().forEach(spellData -> selectSpell.put(spellData,false));
    }


    /**
     * 更新法术选中状态
     * <p>
     * 根据当前选中的法术索引，将其所在组的4个法术全部标记为选中状态。
     * 法术按每4个一组进行分组（索引0-3为第一组，4-7为第二组，以此类推）。
     * </p>
     */
    public void updateSelectSpell(){
        int spellsCount = spells.size();
        if (spellsCount <= 0) return;

        // 获取当前选中的法术索引
        int selected = manager.getSelectionIndex();

        // 计算当前选中法术所在的组索引（每4个法术为一组）
        int selectedGroupIndex = selected / 4;

        // 计算该组的起始索引和结束索引
        int groupStartIndex = selectedGroupIndex * 4;
        int groupEndIndex = Math.min(groupStartIndex + 4, spellsCount);

        // 遍历所有法术，更新选中状态
        for (int i = 0; i < spellsCount; i++) {
            SpellData spell = spells.get(i);
            // 判断当前法术是否在选中的组内
            boolean isInSelectedGroup = (i >= groupStartIndex && i < groupEndIndex);
            selectSpell.put(spell, isInSelectedGroup);
        }

    }
    public void renderBg(GuiGraphics guiGraphics,int x ,int y){
        ResourceLocation bg = ResourceLocation.fromNamespaceAndPath(Nomoremagicchoices.MODID, "textures/gui/bg.png");

        guiGraphics.blit(bg,x,y,0,0,105,36,105,36);
    }

    /**
     * 渲染单个法术槽位
     * @param spell
     * @param guiGraphics
     * @param x
     * @param y
     * @param selected 是否为被选中的4个槽位
     */
    public void renderSpellSlot(SpellData spell,GuiGraphics guiGraphics,int x,int y,boolean selected) {

        //绘制黑色背景
        if (selected) {
            guiGraphics.blit(TEXTURE, x, y, 66, 84, 22, 22);
        }
        //绘制框
        if (true) {
            guiGraphics.blit(TEXTURE, x, y, 22, 84, 22, 22);
        }
        //绘制法术图标
        if (true){
            guiGraphics.blit(spell.getSpell().getSpellIconResource(),
                    x + 3, y + 3,
                    0, 0, 16, 16, 16, 16);
        }
        //绘制冷却条
        float cooldownPercent = ClientMagicData.getCooldownPercent(spell.getSpell());
        if (cooldownPercent > 0) {
            RenderSystem.enableBlend();
            int pixels = (int) (16 * cooldownPercent + 1.0F);

            guiGraphics.blit(TEXTURE, x + 3, y + 3 + 16 - pixels, 47, 87 + 16 - pixels, 16, pixels);
        }
        //绘制冷却框
        if (cooldownPercent <=0){
            if (selected) {
                guiGraphics.blit(TEXTURE, x, y, 88, 84, 22, 22);
            }else {
                guiGraphics.blit(TEXTURE, x +1, y + 1, 156, 85, 20, 20);
            }
        }

    }





    /**
     * 渲染4个法术槽位（一组法术）
     *
     * @param spells 法术列表，必须包含最多4个法术
     * @param guiGraphics 渲染图形上下文
     * @param x 起始X坐标
     * @param y 起始Y坐标
     * @param selected 是否为选中的4个槽位
     * @throws RuntimeException 如果法术数量大于4
     */
    public void renderFourSlot(List<SpellData> spells, GuiGraphics guiGraphics, int x, int y, boolean selected) {
        if (spells.size() > 4) {
            throw new RuntimeException("Too many spells: expected at most 4, got " + spells.size());
        }

        // 每个槽位的间距（22像素宽度 + 2像素间距）
        final int selectSlotSpacing = 24;
        final int slotSpacing = 22;

        // 只渲染实际存在的法术，不足4个不渲染空槽位
        int index = 0;
        for (SpellData spell : spells) {
            int spacing = selected ? selectSlotSpacing : slotSpacing;
            renderSpellSlot(spell, guiGraphics, x + (index * spacing), y, selected);
            index++;
        }

    }

    /**
     * 渲染所有法术（带选中抽出效果）
     * <p>
     * 渲染所有法术，排列成4列多行的网格布局。
     * 当某一组的4个法术被选中时，整组法术会一起向上抽出到固定高度（screenHeight - 50），
     * 类似扑克牌从牌堆抽出的效果。
     * </p>
     *
     * @param selectSpell 法术选中状态映射表
     * @param guiGraphics 渲染图形上下文
     * @param x 起始X坐标（第一行第一列的位置）
     * @param y 起始Y坐标（第一行的位置）
     */
    public void renderAllSpellWithWeapon(HashMap<SpellData, Boolean> selectSpell, GuiGraphics guiGraphics, int x, int y) {
        if (spells == null || spells.isEmpty()) {
            return;
        }

        final int SLOTS_PER_ROW = 4; // 每行4个槽位
        final int SLOT_SIZE = 20; // 槽位尺寸
        final int NORMAL_SPACING = 20; // 普通状态的槽位间距（与 renderAllSpell 保持一致）
        final int ROW_OVERLAP = 15; // 行与行之间的重叠像素（与 renderAllSpell 保持一致）
        final int ROW_HEIGHT = SLOT_SIZE - ROW_OVERLAP; // 实际行间距（20 - 15 = 5像素）
        final int SELECTED_Y = screenHeight - 50-6; // 选中法术组的固定Y坐标（向上抽出）

        // 找出被选中的组（任意一个法术被选中，就代表这一组被选中）
        int selectedGroupIndex = -1;
        for (int i = 0; i < spells.size(); i++) {
            SpellData spell = spells.get(i);
            if (selectSpell.getOrDefault(spell, false)) {
                selectedGroupIndex = i / SLOTS_PER_ROW;
                break; // 找到被选中的组就退出
            }
        }

        // 从后往前渲染（索引大的先渲染在下层，索引小的后渲染覆盖在上层）
        for (int i = spells.size() - 1; i >= 0; i--) {
            SpellData spell = spells.get(i);
            boolean isSelected = selectSpell.getOrDefault(spell, false);

            // 计算当前法术所在的行和列
            int row = i / SLOTS_PER_ROW;
            int col = i % SLOTS_PER_ROW;

            // 计算渲染坐标
            int slotX = x + (col * NORMAL_SPACING);
            int slotY;

            // 判断当前法术所在的组是否是被选中的组
            // 如果是被选中的组，整组法术都使用固定的向上抽出位置
            if (row == selectedGroupIndex) {
                slotY = SELECTED_Y;
            } else {
                slotY = y + (row * ROW_HEIGHT);
            }

            // 渲染法术槽位
            renderSpellSlot(spell, guiGraphics, slotX, slotY, isSelected);
        }
    }



    /**
     * 渲染所有法术，排列成4列多行的网格布局
     * <p>
     * 每行有4个法术槽位，多行之间重叠15像素。
     * 索引小的法术（上方的行）会渲染在索引大的法术（下方的行）之上，确保上方的法术可见。
     * </p>
     *
     * @param spells 法术列表
     * @param guiGraphics 渲染图形上下文
     * @param x 起始X坐标（第一行第一列的位置）
     * @param y 起始Y坐标（第一行的位置）
     * @param selected 是否为选中状态（影响槽位间距）
     */
    public void renderAllSpell(List<SpellData> spells, GuiGraphics guiGraphics, int x, int y, boolean selected) {
        if (spells == null || spells.isEmpty()) {
            return;
        }

        final int SLOTS_PER_ROW = 4; // 每行4个槽位
        final int SLOT_SIZE = 20; // 槽位尺寸
        final int SELECTED_SPACING = 20; // 选中状态的槽位间距
        final int NORMAL_SPACING = 20; // 普通状态的槽位间距
        final int ROW_OVERLAP = 15; // 行与行之间的重叠像素
        final int ROW_HEIGHT = SLOT_SIZE - ROW_OVERLAP; // 实际行间距（22 - 14 = 8像素）

        int spacing = selected ? SELECTED_SPACING : NORMAL_SPACING;

        // 从后往前渲染（索引大的先渲染在下层，索引小的后渲染覆盖在上层）
        // 这样索引小的法术（上方的行）会渲染在上层，可以看到完整的图片
        for (int i = spells.size() - 1; i >= 0; i--) {
            SpellData spell = spells.get(i);

            // 计算当前法术所在的行和列
            int row = i / SLOTS_PER_ROW;
            int col = i % SLOTS_PER_ROW;

            // 计算渲染坐标
            int slotX = x + (col * spacing);
            int slotY = y + (row * ROW_HEIGHT);

            // 渲染法术槽位（isSelected 始终为 false）
            renderSpellSlot(spell, guiGraphics, slotX, slotY, false);
        }
    }

    public static HashMap<SpellData, Boolean> getSelectSpell() {
        return selectSpell;
    }
}
