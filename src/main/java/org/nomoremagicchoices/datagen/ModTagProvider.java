package org.nomoremagicchoices.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import org.nomoremagicchoices.Nomoremagicchoices;
import org.nomoremagicchoices.api.init.TagInit;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

/**
 * 物品标签数据生成器
 * 用于生成模组的自定义物品标签（Tags）
 */
public class ModTagProvider extends ItemTagsProvider {

    public ModTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                         CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, Nomoremagicchoices.MODID, existingFileHelper);
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void addTags(HolderLookup.Provider provider) {
        // 技能武器标签 - 添加所有武器类物品
        tag(TagInit.SKILL_WEAPON)
            .add(Items.WOODEN_SWORD)
            .add(Items.STONE_SWORD)
            .add(Items.IRON_SWORD)
            .add(Items.GOLDEN_SWORD)
            .add(Items.DIAMOND_SWORD)
            .add(Items.NETHERITE_SWORD)
            .add(Items.WOODEN_AXE)
            .add(Items.STONE_AXE)
            .add(Items.IRON_AXE)
            .add(Items.GOLDEN_AXE)
            .add(Items.DIAMOND_AXE)
            .add(Items.NETHERITE_AXE)
            .add(Items.TRIDENT)
            .add(Items.BOW)
            .add(Items.CROSSBOW);
    }
}
