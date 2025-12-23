package org.nomoremagicchoices.player;

import com.mojang.blaze3d.platform.InputConstants;
import io.redspace.ironsspellbooks.player.KeyState;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.Input;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.jarjar.nio.util.Lazy;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;
import org.nomoremagicchoices.Nomoremagicchoices;

import javax.swing.*;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Nomoremagicchoices.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModKeyMapping {

    public static final String SKILL_CATEGORY = "key.categories.nomoremagicchoices";


    public static final Lazy<KeyMapping> SKILL_1 = Lazy.of(() -> new SkillKey("key.nomoremagicchoices.skill1",1, 49, SKILL_CATEGORY));
    public static final Lazy<KeyMapping> SKILL_2 = Lazy.of(() -> new SkillKey("key.nomoremagicchoices.skill2", 2,50, SKILL_CATEGORY));
    public static final Lazy<KeyMapping> SKILL_3 = Lazy.of(() -> new SkillKey("key.nomoremagicchoices.skill3", 3,51, SKILL_CATEGORY));
    public static final Lazy<KeyMapping> SKILL_4 = Lazy.of(() -> new SkillKey("key.nomoremagicchoices.skill4", 4,52, SKILL_CATEGORY));

    public static final Lazy<KeyMapping> CHANG_GROUP = Lazy.of(() -> new KeyMapping("key.nomoremagicchoices.change_group", InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_R, SKILL_CATEGORY));
    public static final Lazy<KeyMapping> NEXT_GROUP = Lazy.of(() -> new KeyMapping("key.nomoremagicchoices.next_group", InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_5, SKILL_CATEGORY)); // Mouse Button 5
    public static final Lazy<KeyMapping> PREV_GROUP = Lazy.of(() -> new KeyMapping("key.nomoremagicchoices.prev_group", InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_4, SKILL_CATEGORY)); // Mouse Button 4

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
