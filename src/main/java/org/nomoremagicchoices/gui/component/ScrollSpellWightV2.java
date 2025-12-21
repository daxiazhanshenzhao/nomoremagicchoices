package org.nomoremagicchoices.gui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.player.ClientMagicData;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.nomoremagicchoices.Nomoremagicchoices;
import org.nomoremagicchoices.api.selection.ClientHandData;
import org.nomoremagicchoices.api.selection.SpellGroupData;
import org.nomoremagicchoices.api.selection.SpellSelectionState;
import org.nomoremagicchoices.player.ModKeyMapping;

import javax.swing.plaf.PanelUI;
import java.util.List;

import static org.nomoremagicchoices.gui.component.KeyHp.getContext;

public class ScrollSpellWightV2 extends AbstractWight{

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Nomoremagicchoices.MODID, "textures/gui/icons.png");
    private static final ResourceLocation KEY_TEXTURE = ResourceLocation.fromNamespaceAndPath(Nomoremagicchoices.MODID, "textures/gui/keys.png");

    // 法术槽间隔常量
    /** Down状态下的法术槽水平间隔（像素） */
    public static final int SPELL_SPACING_DOWN = 20;
    /** Focus状态下的法术槽水平间隔（像素） */
    public static final int SPELL_SPACING_FOCUS = 22;



    public ScrollSpellWightV2(WightContext center, List<SpellData> groupSpells,int totalTick){
        super(center,groupSpells,totalTick);
    }

    @Override
    ResourceLocation getTexture() {
        return TEXTURE;
    }

    @Override
    public void tick() {
        super.tick();
    }




    public void render(GuiGraphics context, float partialTick) {
        if (groupSpells == null || groupSpells.isEmpty()) {
            return;
        }
        var state = center.state();


        // 根据状态选择法术间隔
        int spacing = (state == State.Focus) ? SPELL_SPACING_FOCUS : SPELL_SPACING_DOWN;

        // 为每个法术设置不同的X坐标，避免重叠
        int slotIndex = 0;


        for (SpellData spellData : groupSpells){
            // 计算法术槽的X偏移量
            int slotOffsetX = slotIndex * spacing;

            switch (state){
                case Down:
                    // 渲染Down状态的法术，使用Down间隔
                    renderSlot(context, spellData, center.position().x + slotOffsetX, center.position().y);
                    break;
                case Moving:
                    // 渲染Moving状态的法术，使用Down间隔（移动过程中保持紧凑）
                    // 计算平滑插值：当前offset + 本帧的部分tick进度
                    // getGameTimeDeltaPartialTick 返回 0.0-1.0 之间的值，表示当前帧在一个tick中的进度
                    double frameProgress = partialTick;
                    double interpolatedOffset = offset + (frameProgress / totalTick);

                    var realOffset = getRealOffset(interpolatedOffset);

                    int x = getXPosition(realOffset) + slotOffsetX;
                    int y = getYPosition(realOffset);

                    renderSlot(context, spellData, x, y);
                    break;
                case Focus:
                    // 渲染Focus状态的法术，使用Focus间隔（更宽松）
                    renderSlot(context, spellData, center.position().x + slotOffsetX-3, center.position().y);
                    renderKey(context, slotIndex, center.position().x + slotOffsetX-3, center.position().y -14+2+3);
                    break;
            }

            slotIndex++;
        }

    }

    @Override
    double getRealOffset(double interpolatedOffset) {
        return Math.min(interpolatedOffset * interpolatedOffset * (3.0 - 2.0 * interpolatedOffset),1.0) ;
    }


    @Override
    public void renderSlot(GuiGraphics context,SpellData spell,int x,int y){
        //框
        context.blit(TEXTURE, x, y, 22, 84, 22, 22);
        //法术图标
        context.blit(spell.getSpell().getSpellIconResource(),
                x + 3, y + 3,
                0, 0, 16, 16, 16, 16);
        //绘制冷却条
        float cooldownPercent = ClientMagicData.getCooldownPercent(spell.getSpell());
        if (cooldownPercent > 0) {
            RenderSystem.enableBlend();
            int pixels = (int) (16 * cooldownPercent + 1.0F);
            if (center.state().equals(State.Focus)) {
                context.blit(TEXTURE, x, y, 66, 84, 22, 22);
            }

            context.blit(TEXTURE, x + 3, y + 3 + 16 - pixels, 47, 87 + 16 - pixels, 16, pixels);
        }
        //绘制冷却框
        if (cooldownPercent <=0){
            if (center.state().equals(State.Focus)) {
                context.blit(TEXTURE, x, y, 88, 84, 22, 22);
            }else {
                context.blit(TEXTURE, x +1, y + 1, 156, 85, 20, 20);
            }
        }
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

                // 绘制按键背景：左边框（3px） + 中间可伸缩部分 + 右边框（3px）
                context.blit(KEY_TEXTURE, keyBackgroundX, y, 0, 32, 3, 12);                // 左边框
                context.blit(KEY_TEXTURE, keyBackgroundX + 3, y, 16, 32, textWidth, 12);   // 中间部分
                context.blit(KEY_TEXTURE, keyBackgroundX + 3 + textWidth, y, 13, 32, 3, 12); // 右边框

                // 绘制文字（左边框后直接开始）
                context.drawString(font, keyName, keyBackgroundX + 3, y + 2, 0xFFFFFF);
            }
        }

    }






}
