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

    public static final Lazy<KeyMapping> CHANG_GROUP = Lazy.of(() -> new KeyMapping("key.nomoremagicchoices.change_group", 54, SKILL_CATEGORY));
    public static final Lazy<KeyMapping> NEXT_GROUP = Lazy.of(() -> new KeyMapping("key.nomoremagicchoices.next_group", 262, SKILL_CATEGORY)); // Right Arrow
    public static final Lazy<KeyMapping> PREV_GROUP = Lazy.of(() -> new KeyMapping("key.nomoremagicchoices.prev_group", 263, SKILL_CATEGORY)); // Left Arrow

    @SubscribeEvent
    public static void onRegister(RegisterKeyMappingsEvent event) {
        event.register(SKILL_1.get());
        event.register(SKILL_2.get());
        event.register(SKILL_3.get());
        event.register(SKILL_4.get());
        event.register(CHANG_GROUP.get());
        event.register(NEXT_GROUP.get());
        event.register(PREV_GROUP.get());
    }

    public static boolean isAnySkillKeyBoundToNumber() {
        int key1 = ModKeyMapping.SKILL_1.get().getKey().getValue();
        int key2 = ModKeyMapping.SKILL_2.get().getKey().getValue();
        int key3 = ModKeyMapping.SKILL_3.get().getKey().getValue();
        int key4 = ModKeyMapping.SKILL_4.get().getKey().getValue();

        return isNumberKey(key1) || isNumberKey(key2) ||
                isNumberKey(key3) || isNumberKey(key4);
    }
    public static boolean isNumberKey(int keyCode) {
        return keyCode >= 49 && keyCode <= 57; // 49='1', 57='9'
    }

}
