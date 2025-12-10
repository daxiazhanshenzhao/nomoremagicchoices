package org.nomoremagicchoices.gui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2i;
import org.nomoremagicchoices.Nomoremagicchoices;

import java.util.List;

public class ScrollSpellWight implements IMoveWight{

    public static final int TOTAL_TICKS = 8;
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Nomoremagicchoices.MODID, "textures/gui/icons.png");
    public static final ScrollSpellWight EMPTY = new ScrollSpellWight(new Vector2i(0,0), new Vector2i(0,0), List.of(SpellData.EMPTY), -1);

    // 法术槽间隔常量
    /** Down状态下的法术槽水平间隔（像素） */
    public static final int SPELL_SPACING_DOWN = 20;
    /** Focus状态下的法术槽水平间隔（像素） */
    public static final int SPELL_SPACING_FOCUS = 22;

    private State state = State.Down;
    private State targetState = State.Down; // 移动完成后的目标状态

    private Vector2i center;
    private Vector2i ender;

    private List<SpellData> groupSpells;
    private int groupIndex; // 组索引

    private double offset = 0;


    private ScrollSpellWight(Vector2i center, Vector2i ender, List<SpellData> groupSpells, int groupIndex) {
        this.center = center;
        this.ender = ender;
        this.groupSpells = groupSpells;
        this.groupIndex = groupIndex;
    }


    public static ScrollSpellWight create(int centerX, int centerY, List<SpellData> groupSpells, int groupIndex){
        return new ScrollSpellWight(new Vector2i(centerX,centerY), new Vector2i(centerX,centerY), groupSpells, groupIndex);
    }




    @Override
    public void moveTo(Vector2i ender) {
        if (state.equals(State.Moving)) {
            Nomoremagicchoices.LOGGER.warn("已经在移动中，忽略此次moveTo调用");
            return;
        }

        // 设置目标位置（创建新对象避免引用问题）
        this.ender = new Vector2i(ender);

        // 重置offset并开始移动
        setOffset(0);
        state = State.Moving;

        // 默认根据坐标关系决定目标状态
        if (ender.y < center.y) {
            targetState = State.Focus;
        } else {
            targetState = State.Down;
        }

        Nomoremagicchoices.LOGGER.info("开始移动: from (" + center.x + "," + center.y + ") to (" + ender.x + "," + ender.y + "), 目标状态: " + targetState);
    }

    /**
     * 在底层移动（保持在底层状态）
     * 用于底层法术槽之间的移动，移动完成后保持Down状态
     * 空手模式下使用此方法
     */
    public void moveDown(Vector2i ender) {
        if (state.equals(State.Moving)) {
            Nomoremagicchoices.LOGGER.warn("已经在移动中，忽略此次moveDown调用");
            return;
        }

        this.ender = new Vector2i(ender);
        setOffset(0);
        state = State.Moving;
        targetState = State.Down; // 明确设置目标状态为Down

        Nomoremagicchoices.LOGGER.info("底层移动: from (" + center.x + "," + center.y + ") to (" + ender.x + "," + ender.y + ")");
    }

    /**
     * 移动到焦点位置
     * 移动完成后状态变为Focus
     * 持有物品模式下，将选中的法术组移到顶部时使用此方法
     */
    public void moveFocus(Vector2i ender) {
        if (state.equals(State.Moving)) {
            Nomoremagicchoices.LOGGER.warn("已经在移动中，忽略此次moveFocus调用");
            return;
        }

        this.ender = new Vector2i(ender);
        setOffset(0);
        state = State.Moving;
        targetState = State.Focus; // 明确设置目标状态为Focus

        Nomoremagicchoices.LOGGER.info("焦点移动: from (" + center.x + "," + center.y + ") to (" + ender.x + "," + ender.y + "), 目标状态: Focus");
    }



    public void tick(){
        if (state.equals(State.Moving)){
            // 每个tick增加固定量
            double increment = 1.0 / TOTAL_TICKS;
            setOffset(offset + increment);

            // 移动完成后更新状态和位置
            if (offset >= 1.0){
                // 精确设置为1.0，避免浮点数误差
                setOffset(1.0);

                // 使用预设的目标状态，而不是重新判断
                state = targetState;

                // 更新中心位置为目标位置
                this.center.set(ender);
                setOffset(0);

                Nomoremagicchoices.LOGGER.info("移动完成，新状态: " + state + ", 位置: (" + center.x + ", " + center.y + ")");
            }
        }
    }
    public void render(GuiGraphics context, DeltaTracker partialTick){
        if (groupSpells == null || groupSpells.isEmpty()) return;

        context.pose().pushPose();

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
                    break;
            }

            slotIndex++;
        }

        context.pose().popPose();

    }

    public void renderSlot(GuiGraphics context,SpellData spell,int x,int y){

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
    public enum State{
        Down(0),
        Moving(1),
        Focus(2);

        State(final int value) {
            this.value = value;
        }
        private final int value;
        public int getValue() {
            return value;
        }
    }

    public List<SpellData> getGroupSpells() {
        return groupSpells;
    }

    public int getGroupIndex() {
        return groupIndex;
    }
}
