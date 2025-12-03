package org.nomoremagicchoices.api.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.nomoremagicchoices.Nomoremagicchoices;

/**
 * 模组的自定义标签（Tags）定义
 */
public class TagInit {

    /**
     * 技能武器标签 - 用于标识可以使用技能的武器
     */
    public static final TagKey<Item> SKILL_WEAPON = TagKey.create(
        Registries.ITEM,
        ResourceLocation.fromNamespaceAndPath(Nomoremagicchoices.MODID, "skill_weapon")
    );
}
