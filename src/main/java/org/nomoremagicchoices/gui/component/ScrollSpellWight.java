package org.nomoremagicchoices.gui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.nomoremagicchoices.Nomoremagicchoices;
import org.nomoremagicchoices.api.selection.ClientScrollData;
import org.nomoremagicchoices.api.selection.SpellSelectionState;

import java.util.List;

public class ScrollSpellWight implements IMoveWight{

    public final int TOTAL_TICKS = ClientScrollData.TOTAL_TICKS;
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Nomoremagicchoices.MODID, "textures/gui/icons.png");
    public static ScrollSpellWight Empty = new ScrollSpellWight(new Vector2i(0,0), new Vector2i(0,0));

    private State state = State.Down;

    private Vector2i center;
    private Vector2i ender;

    private List<SpellData> groupSpells;

    private double offset = 0;


    private ScrollSpellWight(Vector2i center, Vector2i ender,List<SpellData> groupSpells) {
        this.center = center;
        this.ender = ender;
        this.groupSpells = groupSpells;

    }


    public static ScrollSpellWight create(int centerX,int centerY,List<SpellData> groupSpells){
        return new ScrollSpellWight(new Vector2i(centerX,centerY),new Vector2i(centerX,centerY),groupSpells);
    }




    @Override
    public void moveTo(Vector2i ender) {
        if (state.equals(State.Moving)) return;
        state = State.Moving;

        if (offset == 1){
            //根据ender和center坐标关系决定State
            if (ender.y < center.y) {
                state = State.Up;
            } else {
                state = State.Down;
            }
            //重置出发点
            setOffset(0);
            this.center.set(ender);
        }



    }





    public void tick(){
        if (state.equals(State.Moving)){
            setOffset(offset + (double) 1 /TOTAL_TICKS);
        }


    }

    public void render(GuiGraphics context, DeltaTracker partialTick){
        context.pose().pushPose();


        for (SpellData spellData : groupSpells){

            switch (state){
                case Down:
                    // 渲染向下状态的法术
                    break;
                case Moving:
                    // 计算平滑插值：当前offset + 本帧的增量
                    double interpolatedOffset = offset + partialTick.getGameTimeDeltaTicks() / TOTAL_TICKS;
                    double realOffset = getRealOffset(interpolatedOffset);

                    int x = getXPosition(realOffset);
                    int y = getYPosition(realOffset);

                    renderSlot(context,spellData, x, y);


                    break;
                case Up:
                    // 渲染向上状态的法术
                    break;
            }

        }

        context.pose().popPose();

    }

    public void renderSlot(GuiGraphics context,SpellData spell,int x,int y){

        //黑色背景
        if (state.equals(State.Down)){
            context.blit(TEXTURE, x, y, 66, 84, 22, 22);
        }
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

            context.blit(TEXTURE, x + 3, y + 3 + 16 - pixels, 47, 87 + 16 - pixels, 16, pixels);
        }
        //绘制冷却框
        if (cooldownPercent <=0){
            if (state.equals(State.Up)) {
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

    public int getXPosition(double realOffset){
        return (int) (center.x + (ender.x - center.x) * realOffset);
    }
    public int getYPosition(double realOffset){
        return (int) (center.y + (ender.y - center.y) * realOffset);
    }


    public enum State{
        Down(0),
        Moving(1),
        Up(2);

        State(final int value) {
            this.value = value;
        }
        private final int value;
        public int getValue() {
            return value;
        }
    }



}
