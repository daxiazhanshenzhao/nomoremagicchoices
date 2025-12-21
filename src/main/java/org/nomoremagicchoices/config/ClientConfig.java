package org.nomoremagicchoices.config;

import net.minecraftforge.common.ForgeConfigSpec;


public class ClientConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_CUSTOM_UI;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_BACKGROUND;

    public static final ForgeConfigSpec.ConfigValue<Integer> CENTER_X_OFFSET;
    public static final ForgeConfigSpec.ConfigValue<Integer> CENTER_Y_OFFSET;
    public static final ForgeConfigSpec.ConfigValue<Integer> FOCUS_HEIGHT;

    public static final ForgeConfigSpec.ConfigValue<Integer> MINE_CUSTOM_SPELL;

    public static final ForgeConfigSpec SPEC;



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
