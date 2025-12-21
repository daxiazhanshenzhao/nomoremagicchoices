package org.nomoremagicchoices;

import com.mojang.logging.LogUtils;


import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.nomoremagicchoices.config.ClientConfig;
import org.slf4j.Logger;


@Mod(Nomoremagicchoices.MODID)
public class Nomoremagicchoices {

    public static final String MODID = "nomoremagicchoices";
    public static final Logger LOGGER = LogUtils.getLogger();



    public Nomoremagicchoices() {
        // 注册客户端配置文件
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();


        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC,String.format("%s-client.toml", Nomoremagicchoices.MODID));


        MinecraftForge.EVENT_BUS.register(this);
    }



}
