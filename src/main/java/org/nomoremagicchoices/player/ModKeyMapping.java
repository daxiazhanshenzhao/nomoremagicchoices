package org.nomoremagicchoices.player;

import io.redspace.ironsspellbooks.player.KeyState;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.jarjar.nio.util.Lazy;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import javax.swing.*;

@EventBusSubscriber
public class ModKeyMapping {

    public static final String SKILL_CATEGORY = "key.categories.nomoremagicchoices";


    public static final Lazy<KeyMapping> SKILL_1 = Lazy.of(() -> new SkillKey("key.nomoremagicchoices.skill1",1, 49, SKILL_CATEGORY));
    public static final Lazy<KeyMapping> SKILL_2 = Lazy.of(() -> new SkillKey("key.nomoremagicchoices.skill2", 2,50, SKILL_CATEGORY));
    public static final Lazy<KeyMapping> SKILL_3 = Lazy.of(() -> new SkillKey("key.nomoremagicchoices.skill3", 3,51, SKILL_CATEGORY));
    public static final Lazy<KeyMapping> SKILL_4 = Lazy.of(() -> new SkillKey("key.nomoremagicchoices.skill4", 4,52, SKILL_CATEGORY));


    @SubscribeEvent
    public static void onRegister(RegisterKeyMappingsEvent event) {
        event.register(SKILL_1.get());
        event.register(SKILL_2.get());
        event.register(SKILL_3.get());
        event.register(SKILL_4.get());
    }




}
