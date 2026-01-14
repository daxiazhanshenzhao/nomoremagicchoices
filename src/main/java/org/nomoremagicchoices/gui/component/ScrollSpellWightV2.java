package org.nomoremagicchoices.gui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.nomoremagicchoices.Nomoremagicchoices;
import org.nomoremagicchoices.api.selection.ClientHandData;
import org.nomoremagicchoices.api.selection.SpellGroupData;
import org.nomoremagicchoices.api.selection.SpellSelectionState;
import org.nomoremagicchoices.config.ClientConfig;
import org.nomoremagicchoices.player.ModKeyMapping;

import java.util.List;

import static org.nomoremagicchoices.gui.component.KeyHp.getContext;

public class ScrollSpellWightV2 extends AbstractWight{

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Nomoremagicchoices.MODID, "textures/gui/icons.png");
    private static final ResourceLocation KEY_TEXTURE = ResourceLocation.fromNamespaceAndPath(Nomoremagicchoices.MODID, "textures/gui/keys.png");

    // ========== Spell Slot 相关纹理（保持不变） ==========
    public static final BlitContext FOCUS_YELLOW = BlitContext.of(0, 0, 22, 22);
    public static final BlitContext FOCUS_SLIVER = BlitContext.of(0, 24, 22, 22);

    public static final BlitContext DOWN_YELLOW = BlitContext.of(24, 0, 22, 22);
    public static final BlitContext DOWN_SLIVER = BlitContext.of(24, 24, 20, 20);

    public static final BlitContext COOLDOWN_SQUARE = BlitContext.of(24, 73, 22, 22);

    // ========== Key 按钮相关纹理 ==========
    /** 键盘按键背景 - 左边框 (3x12) */
    public static final BlitContext KEY_BG_LEFT = BlitContext.of(0, 32, 3, 12);
    /** 键盘按键背景 - 中间可伸缩部分 (起始位置，宽度动态) */
    public static final BlitContext KEY_BG_MIDDLE = BlitContext.of(16, 32, 0, 12);
    /** 键盘按键背景 - 右边框 (3x12) */
    public static final BlitContext KEY_BG_RIGHT = BlitContext.of(13, 32, 3, 12);

    // 法术槽间隔常量
    /** Down状态下的法术槽水平间隔（像素） */
    public static final int SPELL_SPACING_DOWN = 20;
    /** Focus状态下的法术槽水平间隔（像素） */
    public static final int SPELL_SPACING_FOCUS = 22;

    /** 该widget对应的组索引（用于判断是否为当前激活的组） */
    private final int groupIndex;


    public ScrollSpellWightV2(WightContext center, List<SpellData> groupSpells, int totalTick, int groupIndex){
        super(center,groupSpells,totalTick);
        this.groupIndex = groupIndex;
    }

    @Override
    ResourceLocation getTexture() {
        return TEXTURE;
    }

    @Override
    public void tick() {
        super.tick();
    }



    @Override
    public void render(GuiGraphics context, DeltaTracker partialTick) {
        if (groupSpells == null || groupSpells.isEmpty()) {
            return;
        }
        var state = center.state();

        // 根据状态选择法术间隔
        int spacing = (state == State.Focus) ? SPELL_SPACING_FOCUS : SPELL_SPACING_DOWN;

        // 用于记录需要在顶层渲染的 Focus slot（EmptyHand状态下的选中法术）
        int focusSlotIndex = -1;
        SpellData focusSpellData = null;
        int focusX = 0;
        int focusY = 0;

        // 第一阶段：渲染所有普通 slot
        int slotIndex = 0;
        for (SpellData spellData : groupSpells){
            // 计算法术槽的X偏移量
            int slotOffsetX = slotIndex * spacing;

            switch (state){
                case Down:
                    // 检查是否是需要Focus渲染的slot
                    boolean isFocusSlot = ClientHandData.getState().equals(SpellSelectionState.EmptyHand)
                                       && slotIndex == SpellGroupData.getSelectIndex()
                                       && groupIndex == SpellGroupData.getCurrentGroupIndex();

                    if (isFocusSlot) {
                        // 记录Focus slot信息，稍后在顶层渲染
                        focusSlotIndex = slotIndex;
                        focusSpellData = spellData;
                        focusX = center.position().x + slotOffsetX;
                        focusY = center.position().y;
                    } else {
                        // 渲染普通slot
                        renderSlot(context, spellData, center.position().x + slotOffsetX, center.position().y, slotIndex);
                    }
                    break;
                case Moving:
                    // 渲染Moving状态的法术，使用Down间隔（移动过程中保持紧凑）
                    // 计算平滑插值：当前offset + 本帧的部分tick进度
                    double frameProgress = partialTick.getGameTimeDeltaPartialTick(false);
                    double interpolatedOffset = offset + (frameProgress / totalTick);

                    var realOffset = getRealOffset(interpolatedOffset);

                    int x = getXPosition(realOffset) + slotOffsetX;
                    int y = getYPosition(realOffset);

                    renderSlot(context, spellData, x, y, slotIndex);
                    break;
                case Focus:
                    // 渲染Focus状态的法术，使用Focus间隔（更宽松）
                    renderSlot(context, spellData, center.position().x + slotOffsetX-3, center.position().y, slotIndex);
                    renderKey(context, slotIndex, center.position().x + slotOffsetX-3, center.position().y -14+2+3);
                    break;
            }

            slotIndex++;
        }

        // 第二阶段：在顶层渲染 Focus slot（如果存在）
        if (focusSlotIndex != -1 && focusSpellData != null) {
            renderSlot(context, focusSpellData, focusX, focusY, focusSlotIndex);
        }

    }

    @Override
    double getRealOffset(double interpolatedOffset) {
        // 确保输入在 [0, 1] 范围内
        interpolatedOffset = Math.clamp(interpolatedOffset, 0.0, 1.0);

        switch (ClientConfig.SPEED_LINE_MODE.get()){
            case 0:
                // smoothstep 缓动：平滑加速和减速
                return interpolatedOffset * interpolatedOffset * (3.0 - 2.0 * interpolatedOffset);
            case 1:
                // easeOutBack 缓动：快速到达目标，超出到 1.2 倍后回弹
                // c1 控制回弹强度，调整到 3.5 使超出量达到约 1.2 倍
                double c1 = 3.5;
                double c3 = c1 + 1.0;

                return 1.0 + c3 * Math.pow(interpolatedOffset - 1.0, 3)
                                         + c1 * Math.pow(interpolatedOffset - 1.0, 2);
            default:
                return interpolatedOffset;
        }

    }


    @Override
    public void renderSlot(GuiGraphics context,SpellData spell,int x,int y){
        // 获取冷却百分比
        float cooldownPercent = ClientMagicData.getCooldownPercent(spell.getSpell());

        //框：渲染基础边框（始终使用DOWN_YELLOW，位置偏移+1）
        context.blit(TEXTURE, x + 1, y + 1,
                DOWN_YELLOW.uOffset(), DOWN_YELLOW.vOffset(),
                DOWN_YELLOW.width(), DOWN_YELLOW.height());

        //法术图标
        context.blit(spell.getSpell().getSpellIconResource(),
                x + 3, y + 3,
                0, 0, 16, 16, 16, 16);

        //绘制冷却条
        if (cooldownPercent > 0) {
            RenderSystem.enableBlend();
            int pixels = (int) (16 * cooldownPercent + 1.0F);

            // 绘制冷却进度条（使用新的COOLDOWN_SQUARE贴图）
            context.blit(TEXTURE, x + 3, y + 3 + 16 - pixels,
                    COOLDOWN_SQUARE.uOffset() + 3, COOLDOWN_SQUARE.vOffset() + 3 + 16 - pixels,
                    16, pixels);
        }

        //绘制顶层框（根据状态和冷却情况）
        if (cooldownPercent <= 0){
            // 无冷却时：法术就绪，显示金色
            if (center.state().equals(State.Focus)) {
                // Focus状态：YELLOW边框（就绪+高亮）
                context.blit(TEXTURE, x, y,
                        FOCUS_YELLOW.uOffset(), FOCUS_YELLOW.vOffset(),
                        FOCUS_YELLOW.width(), FOCUS_YELLOW.height());
            }
            // Down状态：已经在基础边框中渲染了DOWN_YELLOW，不需要额外渲染
        } else {
            // 冷却中：显示银色边框
            if (center.state().equals(State.Focus)) {
                // Focus状态下的冷却：使用FOCUS_SLIVER
                context.blit(TEXTURE, x, y,
                        FOCUS_SLIVER.uOffset(), FOCUS_SLIVER.vOffset(),
                        FOCUS_SLIVER.width(), FOCUS_SLIVER.height());
            } else {
                // 非Focus状态下的冷却：使用DOWN_SLIVER
                context.blit(TEXTURE, x + 1, y + 1,
                        DOWN_SLIVER.uOffset(), DOWN_SLIVER.vOffset(),
                        DOWN_SLIVER.width(), DOWN_SLIVER.height());
            }
        }
    }

    /**
     * 渲染法术槽（带槽位索引，用于EmptyHand状态下高亮当前选中的法术）
     * @param context 图形上下文
     * @param spell 法术数据
     * @param x X坐标
     * @param y Y坐标
     * @param slotIndex 槽位索引（0-3）
     */
    public void renderSlot(GuiGraphics context, SpellData spell, int x, int y, int slotIndex){
        // 获取当前手持状态和冷却百分比
        boolean shouldRenderFocusBorder = isShouldRenderFocusBorder(slotIndex);
        float cooldownPercent = ClientMagicData.getCooldownPercent(spell.getSpell());

        //框：渲染基础边框
        if (shouldRenderFocusBorder) {
            // EmptyHand状态且是当前选中的法术：使用FOCUS_YELLOW边框（更粗的金色边框）
            context.blit(TEXTURE, x, y,
                    FOCUS_YELLOW.uOffset(), FOCUS_YELLOW.vOffset(),
                    FOCUS_YELLOW.width(), FOCUS_YELLOW.height());
        } else {
            // 渲染普通边框（位置偏移+1）
            context.blit(TEXTURE, x + 1, y + 1,
                    DOWN_YELLOW.uOffset(), DOWN_YELLOW.vOffset(),
                    DOWN_YELLOW.width(), DOWN_YELLOW.height());
        }

        //法术图标
        context.blit(spell.getSpell().getSpellIconResource(),
                x + 3, y + 3,
                0, 0, 16, 16, 16, 16);

        //绘制冷却条
        if (cooldownPercent > 0) {
            RenderSystem.enableBlend();
            int pixels = (int) (16 * cooldownPercent + 1.0F);

            // 绘制冷却进度条（使用新的COOLDOWN_SQUARE贴图）
            context.blit(TEXTURE, x + 3, y + 3 + 16 - pixels,
                    COOLDOWN_SQUARE.uOffset() + 3, COOLDOWN_SQUARE.vOffset() + 3 + 16 - pixels,
                    16, pixels);
        }

        //绘制顶层框（根据冷却状态）
        if (cooldownPercent <= 0){
            // 无冷却时：法术就绪，显示金色
            if (!shouldRenderFocusBorder) {
                if (center.state().equals(State.Focus)) {
                    // Focus状态：YELLOW边框（就绪+高亮）
                    context.blit(TEXTURE, x, y,
                            FOCUS_YELLOW.uOffset(), FOCUS_YELLOW.vOffset(),
                            FOCUS_YELLOW.width(), FOCUS_YELLOW.height());
                }
                // Down状态且非选中：已经在基础边框中渲染了DOWN_YELLOW，不需要额外渲染
            }
            // shouldRenderFocusBorder=true 时，已经在上面渲染了FOCUS_YELLOW，不需要额外渲染
        } else {
            // 冷却中：显示银色边框
            if (center.state().equals(State.Focus) || shouldRenderFocusBorder) {
                // Focus状态或选中状态下的冷却：使用FOCUS_SLIVER
                context.blit(TEXTURE, x, y,
                        FOCUS_SLIVER.uOffset(), FOCUS_SLIVER.vOffset(),
                        FOCUS_SLIVER.width(), FOCUS_SLIVER.height());
            } else {
                // 非Focus状态下的冷却：使用DOWN_SLIVER
                context.blit(TEXTURE, x + 1, y + 1,
                        DOWN_SLIVER.uOffset(), DOWN_SLIVER.vOffset(),
                        DOWN_SLIVER.width(), DOWN_SLIVER.height());
            }
        }
    }

    private boolean isShouldRenderFocusBorder(int slotIndex) {
        SpellSelectionState handState = ClientHandData.getState();
        // 获取当前选中的法术索引（相对索引 0-3）
        int selectIndex = SpellGroupData.getSelectIndex();

        // 判断是否需要渲染 Focus 边框
        // 关键修复：只有当前激活的组（groupIndex == getCurrentGroupIndex()）
        // 且处于 Down 状态、EmptyHand 状态、槽位索引匹配时，才渲染 Focus 边框
        boolean shouldRenderFocusBorder = center.state().equals(State.Down)
                                         && handState.equals(SpellSelectionState.EmptyHand)
                                         && slotIndex == selectIndex
                                         && groupIndex == SpellGroupData.getCurrentGroupIndex();
        return shouldRenderFocusBorder;
    }

    public void renderKey(GuiGraphics context, int slotIndex, int x, int y) {

        var keyMapping = switch (slotIndex) {
            case 0 -> ModKeyMapping.SKILL_1.get();
            case 1 -> ModKeyMapping.SKILL_2.get();
            case 2 -> ModKeyMapping.SKILL_3.get();
            case 3 -> ModKeyMapping.SKILL_4.get();
            default -> null;
        };

        if (keyMapping == null) return;

        int keyCode = keyMapping.getKey().getValue();
        BlitContext blitContext = getContext(keyCode);

        if (blitContext.width() > 0 && blitContext.height() > 0) {
            // 获取当前的相对选择索引
            int selectIndex = SpellGroupData.getSelectIndex();

            // 法杖状态下，根据getSelectIndex()值渲染鼠标右键到对应槽位
            if (slotIndex == selectIndex && ClientHandData.getState().equals(SpellSelectionState.Staff)) {
                BlitContext rightClickContext = getContext(1);
                int centerX = x + 11 - rightClickContext.width() / 2;
                context.blit(KEY_TEXTURE, centerX, y,
                        rightClickContext.uOffset(), rightClickContext.vOffset(),
                        rightClickContext.width(), rightClickContext.height());
            }
            else if (blitContext.isMouse()) {
                // 渲染鼠标按键纹理（居中对齐）
                int centerX = x + 11 - blitContext.width() / 2;
                context.blit(KEY_TEXTURE, centerX, y,
                        blitContext.uOffset(), blitContext.vOffset(),
                        blitContext.width(), blitContext.height());
            }
            else {
                // 渲染键盘按键文字和背景
                var font = Minecraft.getInstance().font;
                String keyName = keyMapping.getTranslatedKeyMessage().getString();

                // 计算文字的实际像素宽度
                int textWidth = font.width(keyName);
                // 左右边框各3px = 总宽度 + 6px（缩小4像素）
                int totalBackgroundWidth = textWidth + 6;

                // 计算按键背景的起始X坐标（居中对齐：法术槽中心点 - 总宽度的一半）
                int keyBackgroundX = x + 11 - totalBackgroundWidth / 2;

                // 绘制按键背景：使用 BlitContext 常量
                // 左边框（3px）
                context.blit(KEY_TEXTURE, keyBackgroundX, y,
                        KEY_BG_LEFT.uOffset(), KEY_BG_LEFT.vOffset(),
                        KEY_BG_LEFT.width(), KEY_BG_LEFT.height());

                // 中间部分（动态宽度）
                context.blit(KEY_TEXTURE, keyBackgroundX + 3, y,
                        KEY_BG_MIDDLE.uOffset(), KEY_BG_MIDDLE.vOffset(),
                        textWidth, KEY_BG_MIDDLE.height());

                // 右边框（3px）
                context.blit(KEY_TEXTURE, keyBackgroundX + 3 + textWidth, y,
                        KEY_BG_RIGHT.uOffset(), KEY_BG_RIGHT.vOffset(),
                        KEY_BG_RIGHT.width(), KEY_BG_RIGHT.height());

                // 绘制文字（左边框后直接开始）
                context.drawString(font, keyName, keyBackgroundX + 3, y + 2, 0xFFFFFF);
            }
        }

    }






}
