package org.nomoremagicchoices;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Nomoremagicchoices.MODID)
public class Nomoremagicchoices {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "nomoremagicchoices";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Nomoremagicchoices(IEventBus modEventBus, ModContainer modContainer) {
        // 注册通用设置事件
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Common setup for NoMoreMagicChoices");
    }
}
