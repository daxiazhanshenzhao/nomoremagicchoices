package org.nomoremagicchoices.gui.component;

import org.joml.Vector2i;

public record WightContext(Vector2i position, State state){


    /**
     * 将当前坐标与目标做对比，用于tick计时器
     * @param ender
     * @return true为能够运行
     */
    public boolean canRun(WightContext ender){
        if (ender != null){
            // 如果位置不同，需要移动
            if (!this.position().equals(ender.position())){
                return true;
            }
            // 如果位置相同但状态不同，也需要执行状态变化
            if (!this.state().equals(ender.state())){
                return true;
            }
        }
        return false;
    }


}
