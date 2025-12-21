package org.nomoremagicchoices.player;

import net.minecraft.client.KeyMapping;



public class SkillKey extends KeyMapping {

    public SkillKey(String name, int skillIndex,int keyCode, String category) {
        super(name, keyCode, category);
        this.skillIndex = skillIndex;
    }

    private int skillIndex;






}
