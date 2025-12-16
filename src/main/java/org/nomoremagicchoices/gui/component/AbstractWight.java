package org.nomoremagicchoices.gui.component;

import io.redspace.ironsspellbooks.api.spells.SpellData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2i;

import java.util.List;

public abstract class AbstractWight {

    //TODO:以后看能不能用四元数的矩阵变换进行移动
    public void tick(){
        if (tasks == null || tasks.isEmpty()) {
            return;
        }

        // 检查索引越界
        if (cTaskIndex >= tasks.size()) {
            tasks.clear();
            cTaskIndex = 0;
            return;
        }

        // 获取当前任务
        this.ender = tasks.get(cTaskIndex);

        // 检查是否可以执行当前任务
        if (!center.canRun(ender)) {

            cTaskIndex++;
            return;
        }

        // 任务开始时设置状态为Moving
        if (center.state() != State.Moving) {
            // 创建新的WightContext，保持位置不变，状态改为Moving
            this.center = new WightContext(center.position(), State.Moving);
        }

        // 执行移动动画
        double increment = 1.0 / totalTick;
        setOffset(offset + increment);

        if (offset >= 1.0) {
            // 完成当前任务，只更新位置，不更新状态
            // 保持Moving状态直到所有任务完成
            var newPosition = ender.position();
            this.center = new WightContext(newPosition, State.Moving);
            setOffset(0);
            cTaskIndex++;

            // 检查是否所有任务都已完成
            if (cTaskIndex >= tasks.size()) {
                // 所有任务完成，更新为最后一个ender的状态
                this.center = ender;
                tasks.clear();
                cTaskIndex = 0;
            }
        }
    }
    public abstract void render(GuiGraphics context, DeltaTracker partialTick);


    /**
     * 移动公式曲线
     * @param interpolatedOffset 偏移
     * @return
     */
    abstract double getRealOffset(double interpolatedOffset);
    abstract void renderSlot(GuiGraphics context,SpellData spell,int x,int y);

    protected WightContext center;    //当前坐标和状态
    protected double offset = 0;      //当前的偏移量

    protected List<SpellData> groupSpells;

    protected int totalTick; //总运行时间 tick

    protected WightContext ender; //目标
    protected List<WightContext> tasks;
    private int cTaskIndex;

    public AbstractWight(WightContext center, List<SpellData> groupSpells,int totalTick){
        this.center = center;
        this.groupSpells = groupSpells;
        this.totalTick = totalTick;
    }

    abstract ResourceLocation getTexture();

    protected void setOffset(double offset) {
        this.offset = Math.clamp(offset, 0, 1);
    }

    public void addTasks(Moving moving) {
        this.tasks = moving.tasks;
    }

    protected int getXPosition(double realOffset) {
        return (int) (center.position().x + (ender.position().x - center.position().x) * realOffset);
    }
    protected int getYPosition(double realOffset){
        return (int) (center.position().y + (ender.position().y - center.position().y) * realOffset);
    }

    public boolean compareEqualsSpell(AbstractWight target) {
        var oldSpell = this.getGroupSpells().getFirst().getSpell();
        var newSpell = target.getGroupSpells().getFirst().getSpell();

        return oldSpell.equals(newSpell);
    }

    public List<SpellData> getGroupSpells() {
        return groupSpells;
    }

    public WightContext getCenter() {
        return center;
    }
}
