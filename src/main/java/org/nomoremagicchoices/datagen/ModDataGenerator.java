package org.nomoremagicchoices.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.nomoremagicchoices.Nomoremagicchoices;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = Nomoremagicchoices.MODID)
public class ModDataGenerator {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        // 创建 BlockTagsProvider（ItemTagsProvider 需要它作为依赖）
        ModBlockTagProvider blockTagsProvider = new ModBlockTagProvider(output, lookupProvider, existingFileHelper);

        // 注册 BlockTagsProvider
        generator.addProvider(event.includeServer(), blockTagsProvider);

        // 注册 ItemTagsProvider（用于生成 SKILL_WEAPON 物品标签）
        generator.addProvider(
            event.includeServer(),
            new ModTagProvider(
                output,
                lookupProvider,
                blockTagsProvider.contentsGetter(),
                existingFileHelper
            )
        );
    }
}
