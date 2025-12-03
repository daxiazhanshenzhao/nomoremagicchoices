package org.nomoremagicchoices.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.ConfigValue<Boolean> ENABLE_CUSTOM_UI;

    public static final ModConfigSpec SPEC;

    static {
        ENABLE_CUSTOM_UI = BUILDER.define("Enable Custom Bar", true);
        SPEC = BUILDER.build();

    }
}
