package org.nomoremagicchoices;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.nomoremagicchoices.config.ClientConfig;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Nomoremagicchoices.MODID)
public class Nomoremagicchoices {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "nomoremagicchoices";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Nomoremagicchoices(IEventBus modEventBus, ModContainer modContainer) {
        // 注册客户端配置文件
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
    }

}
