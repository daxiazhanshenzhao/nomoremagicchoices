package org.nomoremagicchoices.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import org.nomoremagicchoices.Nomoremagicchoices;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

/**
 * 方块标签数据生成器
 * 用于生成模组的自定义方块标签（Block Tags）
 */
public class ModBlockTagProvider extends BlockTagsProvider {

    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                               @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Nomoremagicchoices.MODID, existingFileHelper);
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void addTags(HolderLookup.Provider provider) {
        // 在这里添加方块标签
        // 例如：
        // tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.MAGIC_BLOCK.get());
    }
}

