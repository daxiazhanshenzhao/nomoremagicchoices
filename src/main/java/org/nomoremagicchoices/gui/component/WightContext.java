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
            if (!this.position().equals(ender.position())){
                return true;
            }
        }return false;
    }


}
