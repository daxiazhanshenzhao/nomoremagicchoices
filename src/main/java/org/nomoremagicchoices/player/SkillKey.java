package org.nomoremagicchoices.player;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;


public class SkillKey extends KeyMapping {

    public SkillKey(String name, int skillIndex,int keyCode, String category) {
        super(name, keyCode, category);
        this.skillIndex = skillIndex;
    }

    private int skillIndex;






}
