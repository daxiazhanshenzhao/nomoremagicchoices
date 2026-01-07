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
        // Enable custom UI
        // If true, use custom spell selection UI; if false, use vanilla UI
        ENABLE_CUSTOM_UI = BUILDER
                .comment("Enable custom spell selection interface",
                        "true: Use custom UI",
                        "false: Use vanilla UI")
                .define("Enable Custom Bar", true);

        // Enable background rendering
        // Controls whether to render the background of the spell selection interface
        ENABLE_BACKGROUND = BUILDER
                .comment("Enable spell selection interface background",
                        "true: Render background",
                        "false: Do not render background")
                .define("Enable Background", true);

        // Center point X-axis offset
        // Horizontal position offset of the spell selection interface (relative to screen center)
        CENTER_X_OFFSET = BUILDER
                .comment("Spell selection interface horizontal offset",
                        "Positive value: Offset to the right",
                        "Negative value: Offset to the left",
                        "Unit: pixels")
                .define("Center X Offset", -195);

        // Center point Y-axis offset
        // Vertical position offset of the spell selection interface (relative to screen bottom)
        CENTER_Y_OFFSET = BUILDER
                .comment("Spell selection interface vertical offset",
                        "Positive value: Offset upward",
                        "Negative value: Offset downward",
                        "Unit: pixels")
                .define("Center Y Offset", -22);

        // Focus state height
        // Additional upward movement height when the spell group is in focus state
        FOCUS_HEIGHT = BUILDER
                .comment("Focus state additional height",
                        "Height that the spell group moves up when it gains focus",
                        "Unit: pixels")
                .define("Focus Height", 10);

        // Custom spell count limit
        // When the number of spells learned by the player exceeds this value,
        // spells beyond this limit will be handled by the vanilla system
        // Example: When set to 8, the first 8 spells use custom UI,
        // spells from the 9th onward use the vanilla system
        MINE_CUSTOM_SPELL = BUILDER
                .comment("Custom spell count limit",
                        "Controls the maximum number of spells that use custom UI",
                        "Spells beyond this limit will fall back to the vanilla system",
                        "Default: 8 (first 8 spells use custom UI)",
                        "Set to 0: Completely use vanilla system",
                        "Set to a large value (e.g., 999): All spells use custom UI")
                .define("Mine Custom Spell", 8);

        SPEC = BUILDER.build();

    }
}
