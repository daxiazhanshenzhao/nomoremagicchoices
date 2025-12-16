package org.nomoremagicchoices.gui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2i;
import org.nomoremagicchoices.Nomoremagicchoices;

import org.nomoremagicchoices.api.selection.ClientData;
import org.nomoremagicchoices.api.selection.ClientHandData;
import org.nomoremagicchoices.api.selection.SpellSelectionState;
import org.nomoremagicchoices.player.ModKeyMapping;

import java.util.List;


@Deprecated
public class ScrollSpellWight implements IMoveWight{

    public static final int TOTAL_TICKS = 2;
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Nomoremagicchoices.MODID, "textures/gui/icons.png");
    public static final ResourceLocation KEY_TEXTURE = ResourceLocation.fromNamespaceAndPath(Nomoremagicchoices.MODID, "textures/gui/keys.png");

    // 法术槽间隔常量
    /** Down状态下的法术槽水平间隔（像素） */
    public static final int SPELL_SPACING_DOWN = 20;
    /** Focus状态下的法术槽水平间隔（像素） */
    public static final int SPELL_SPACING_FOCUS = 22;
    public static final ScrollSpellWight EMPTY = ScrollSpellWight.create(new Vector2i(0,0), List.of(SpellData.EMPTY),0, ClientData.getClientHandData());

    private State state = State.Down;
    private State enderState = State.Down; // 移动完成后的目标状态

    private Vector2i center;
    private Vector2i ender;

    private List<SpellData> groupSpells;
    private int groupIndex; // 组索引

    private double offset = 0;

    private ClientHandData handData;

    private ScrollSpellWight(Vector2i center, Vector2i ender, List<SpellData> groupSpells, int groupIndex,ClientHandData clientHandData) {
        this.center = center;
        this.ender = ender;
        this.groupSpells = groupSpells;
        this.groupIndex = groupIndex;
        this.handData = clientHandData;
    }


    public static ScrollSpellWight create(int centerX, int centerY, List<SpellData> groupSpells, int groupIndex,ClientHandData clientHandData){
        return new ScrollSpellWight(new Vector2i(centerX,centerY), new Vector2i(centerX,centerY), groupSpells, groupIndex, clientHandData);
    }

    public static ScrollSpellWight create(Vector2i center, List<SpellData> groupSpells, int groupIndex,ClientHandData clientHandData){
        return new ScrollSpellWight(new Vector2i(center), new Vector2i(center), groupSpells, groupIndex,clientHandData);
    }



    @Override
    public void moveTo(Vector2i ender) {
        if (state.equals(State.Moving)) {
            return;
        }

        // 设置目标位置（创建新对象避免引用问题）
        this.ender = new Vector2i(ender);


        setOffset(0);
        state = State.Moving;

    }

    /**
     * 万能移动方法
     * @param ender
     * @param enderState
     */
    public void move(Vector2i ender,State enderState){
        if (state.equals(State.Moving) || this.ender.equals(ender)) return;

        this.ender = ender;
        this.enderState = enderState;




    }





    public void tick(){
        if (state.equals(State.Moving)){
            // 每个tick增加固定量
            double increment = 1.0 / TOTAL_TICKS;
            setOffset(offset + increment);

            // 移动完成后更新状态和位置
            if (offset >= 1.0){
                state = enderState;
                this.center.set(ender);
                setOffset(0);

            }
        }
    }
    public void render(GuiGraphics context, DeltaTracker partialTick){
        if (groupSpells == null || groupSpells.isEmpty()) {
            return;
        }

//        context.pose().pushPose();

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
                    renderSlot(context, spellData, center.x + slotOffsetX, center.y);
                    break;
                case Moving:
                    // 渲染Moving状态的法术，使用Down间隔（移动过程中保持紧凑）
                    // 计算平滑插值：当前offset + 本帧的部分tick进度
                    // getGameTimeDeltaPartialTick 返回 0.0-1.0 之间的值，表示当前帧在一个tick中的进度
                    double frameProgress = partialTick.getGameTimeDeltaPartialTick(false);
                    double interpolatedOffset = offset + (frameProgress / TOTAL_TICKS);

                    // 确保插值不超过1.0，避免抖动
                    interpolatedOffset = Math.min(interpolatedOffset, 1.0);

                    double realOffset = getRealOffset(interpolatedOffset);

                    int x = getXPosition(realOffset) + slotOffsetX;
                    int y = getYPosition(realOffset);

                    renderSlot(context, spellData, x, y);
                    break;
                case Focus:
                    // 渲染Focus状态的法术，使用Focus间隔（更宽松）
                    renderSlot(context, spellData, center.x + slotOffsetX-3, center.y);
                    renderKey(context, slotIndex, center.x + slotOffsetX-3, center.y -14+2+3);
                    break;
            }

            slotIndex++;
        }

//        context.pose().popPose();

    }

    public void renderSlot(GuiGraphics context,SpellData spell,int x,int y){
        // 调试信息


        //黑色背景
//        if (state.equals(State.Down)){
//            context.blit(TEXTURE, x, y, 66, 84, 22, 22);
//        }
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
            if (state.equals(State.Focus)) {
                context.blit(TEXTURE, x, y, 66, 84, 22, 22);
            }

            context.blit(TEXTURE, x + 3, y + 3 + 16 - pixels, 47, 87 + 16 - pixels, 16, pixels);
        }
        //绘制冷却框
        if (cooldownPercent <=0){
            if (state.equals(State.Focus)) {
                context.blit(TEXTURE, x, y, 88, 84, 22, 22);
            }else {
                context.blit(TEXTURE, x +1, y + 1, 156, 85, 20, 20);
            }
        }
    }


    private void setOffset(double offset) {
        this.offset = Math.clamp(offset, 0, 1);
    }

    public double getRealOffset(double interpolatedOffset){

        return interpolatedOffset * interpolatedOffset * (3.0 - 2.0 * interpolatedOffset);
    }

    private int getXPosition(double realOffset){
        return (int) (center.x + (ender.x - center.x) * realOffset);
    }
    private int getYPosition(double realOffset){
        return (int) (center.y + (ender.y - center.y) * realOffset);
    }

    /**
     * Renders the key hint for a spell slot.
     *
     * @param context Graphics context
     * @param slotIndex Index of the spell within the group (0-3)
     * @param x X position
     * @param y Y position
     */
    public void renderKey(GuiGraphics context, int slotIndex, int x, int y) {
        if (state != State.Focus) return;

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
            // 法杖固定显示鼠标右键（第一个法术槽）
            if (slotIndex == 0 && handData.getState().equals(SpellSelectionState.Staff)) {
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

    /**
     * Maps key codes to texture coordinates for rendering key hints.
     * Mouse buttons (0-6) return texture coordinates with isMouse=true.
     * Keyboard keys return isMouse=false and will be rendered as text.
     *
     * @param keyCode GLFW key code
     * @return BlitContext containing texture coordinates and rendering type
     */
    private BlitContext getContext(int keyCode) {
        return switch (keyCode) {
            // Mouse buttons (GLFW codes: 0-6) - 使用纹理渲染
            case 0 -> new BlitContext(true, 0, 0, 9, 12);       // Left Click
            case 1 -> new BlitContext(true, 16, 0, 9, 12);      // Right Click
            case 2 -> new BlitContext(true, 32, 0, 9, 12);      // Middle Click
            case 3 -> new BlitContext(true, 0, 16, 10, 12);     // Side Button 1 (Back)
            case 4 -> new BlitContext(true, 16, 16, 10, 12);    // Side Button 2 (Forward)
            case 5 -> new BlitContext(true, 32, 16, 9, 12);     // Extra Button 1
            case 6 -> new BlitContext(true, 48, 16, 9, 12);     // Extra Button 2

            // Keyboard keys - 使用文字渲染 (设置width>0以通过检查)
            default -> new BlitContext(false, 0, 0, 22, 16);
        };
    }

    public void down(){
        this.state = State.Down;
    }
    public void moving(){
        this.state = State.Moving;
    }
    public void focus(){
        this.state = State.Focus;
    }


    /**
     * 法术图标的显示状态枚举
     * <p>定义了法术图标在不同状态下的渲染尺寸和位置：
     * <ul>
     * <li>{@link State#Down} - 法术图标位于底部位置，渲染尺寸：20×20像素
     * <li>{@link State#Moving} - 法术图标处于移动过渡状态，从底部移动到顶部（或反向）
     * <li>{@link State#Focus} - 法术图标位于顶部焦点位置，渲染尺寸：22×22像素
     * </ul>
     */


    public List<SpellData> getGroupSpells() {
        return groupSpells;
    }

    public int getGroupIndex() {
        return groupIndex;
    }

    public Vector2i getCenter() {
        return center;
    }
    public boolean isFocus() {
        return state.equals(State.Focus);
    }


    public boolean compareEqualsSpell(ScrollSpellWight wight){
        var oldSpell = this.getGroupSpells().getFirst().getSpell();
        var newSpell = wight.getGroupSpells().getFirst().getSpell();

        return oldSpell.equals(newSpell);
    }
















    @Deprecated
    /**
     * 在底层移动（保持在底层状态）
     * 用于底层法术槽之间的移动，移动完成后保持Down状态
     * 空手模式下使用此方法
     */
    public void moveDown(Vector2i ender) {
        // 如果正在移动，检查目标是否改变
        if (state.equals(State.Moving)) {
            // 如果目标位置和目标状态都没变，不需要重新移动
            if (this.ender.equals(ender) && this.enderState == State.Down) {
                return;
            }
            // 目标改变了，需要重新开始移动
            // 1. 计算当前实际位置（基于当前offset）
            double realOffset = getRealOffset(offset);
            int currentX = getXPosition(realOffset);
            int currentY = getYPosition(realOffset);

            // 2. 将当前实际位置设为新的起点
            this.center.set(currentX, currentY);

            // 3. 设置新的目标位置和状态
            this.ender = new Vector2i(ender);
            this.enderState = State.Down;

            // 4. 重置offset，从当前位置重新开始移动
            setOffset(0);
            return;
        }

        this.ender = new Vector2i(ender);
        setOffset(0);
        state = State.Moving;
        enderState = State.Down; // 明确设置目标状态为Down

    }

    @Deprecated
    /**
     * 移动到焦点位置
     * 移动完成后状态变为Focus
     * 持有物品模式下，将选中的法术组移到顶部时使用此方法
     */
    public void moveFocus(Vector2i ender) {
        // 如果正在移动，检查目标是否改变
        if (state.equals(State.Moving)) {
            // 如果目标位置和目标状态都没变，不需要重新移动
            if (this.ender.equals(ender) && this.enderState == State.Focus) {
                return;
            }
            // 目标改变了，需要重新开始移动
            double realOffset = getRealOffset(offset);
            int currentX = getXPosition(realOffset);
            int currentY = getYPosition(realOffset);

            this.center.set(currentX, currentY);
            this.ender = new Vector2i(ender);
            this.enderState = State.Focus;
            setOffset(0);
            return;
        }

        this.ender = new Vector2i(ender);
        setOffset(0);
        state = State.Moving;
        enderState = State.Focus;
    }
}
