package org.nomoremagicchoices.gui.component;

import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class Moving{

    public List<WightContext> tasks;

    private List<Vector2i> positions;


    public Moving(){
        tasks = new ArrayList<>();
        positions = new ArrayList<>();
    }

    public static Moving start(){
        return new Moving();
    }

    /**
     * 进行一系列的移动，最后的state以末尾最后一个ender的state为准
     * @param pos
     * @return
     */
    public Moving addPos(Vector2i pos){
        positions.add(pos);
        return this;
    }

    public Moving endState(State state){

        this.tasks = new ArrayList<>();
        for(Vector2i pos: positions){
            this.tasks.add(new WightContext(pos, state));
        }

        return this;
    }
}