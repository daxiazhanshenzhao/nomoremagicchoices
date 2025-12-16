package org.nomoremagicchoices.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.ConfigValue<Boolean> ENABLE_CUSTOM_UI;
    public static final ModConfigSpec.ConfigValue<Boolean> ENABLE_BACKGROUND;

    public static final ModConfigSpec.ConfigValue<Integer> CENTER_X_OFFSET;
    public static final ModConfigSpec.ConfigValue<Integer> CENTER_Y_OFFSET;
    public static final ModConfigSpec.ConfigValue<Integer> FOCUS_HEIGHT;

    public static final ModConfigSpec.ConfigValue<Integer> MINE_CUSTOM_SPELL;

    public static final ModConfigSpec SPEC;



    static {
        ENABLE_CUSTOM_UI = BUILDER.define("Enable Custom Bar", true);
        ENABLE_BACKGROUND = BUILDER.define("Enable Background", true);

        CENTER_X_OFFSET = BUILDER.define("Center X Offset",-195);
        CENTER_Y_OFFSET = BUILDER.define("Center Y Offset",-22);
        FOCUS_HEIGHT = BUILDER.define("Focus Height",10);

        MINE_CUSTOM_SPELL = BUILDER.define("Mine Custom Spell", 8);

        SPEC = BUILDER.build();

    }
}
