package org.nomoremagicchoices.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 客户端配置类
 * 管理所有客户端的配置选项，包括UI显示、位置偏移、动画效果等
 *
 * 配置文件位置：config/nomoremagicchoices-client.toml
 *
 * @author NoMoreMagicChoices
 * @since 1.0
 */
public class ClientConfig {
    
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    
    // ========== UI 显示开关 ==========

    /**
     * 启用自定义法术选择界面
     * - true: 使用本模组的自定义UI
     * - false: 使用原版/Iron's Spellbooks的默认UI
     */
    public static final ModConfigSpec.ConfigValue<Boolean> ENABLE_CUSTOM_UI;

    /**
     * 启用法术选择界面背景
     * - true: 渲染背景图片
     * - false: 不渲染背景（仅显示法术图标）
     */
    public static final ModConfigSpec.ConfigValue<Boolean> ENABLE_BACKGROUND;
    
    // ========== 位置偏移配置 ==========

    /**
     * 背景X轴偏移量
     * 调整法术选择背景图片的水平位置
     * - 正值：向右移动
     * - 负值：向左移动
     * - 单位：像素
     */
    public static final ModConfigSpec.ConfigValue<Integer> BACKGROUND_X_OFFSET;

    /**
     * 背景Y轴偏移量
     * 调整法术选择背景图片的垂直位置
     * - 正值：向下移动
     * - 负值：向上移动
     * - 单位：像素
     */
    public static final ModConfigSpec.ConfigValue<Integer> BACKGROUND_Y_OFFSET;

    /**
     * 法术图标X轴偏移量
     * 调整所有法术图标的水平位置（独立于背景偏移）
     * - 正值：向右移动
     * - 负值：向左移动
     * - 单位：像素
     */
    public static final ModConfigSpec.ConfigValue<Integer> SPELL_X_OFFSET;

    /**
     * 法术图标Y轴偏移量
     * 调整所有法术图标的垂直位置（独立于背景偏移）
     * - 正值：向下移动
     * - 负值：向上移动
     * - 单位：像素
     */
    public static final ModConfigSpec.ConfigValue<Integer> SPELL_Y_OFFSET;
    
    // ========== 动画与交互配置 ==========

    /**
     * 聚焦状态上浮高度
     * 当法术组获得焦点时，额外向上移动的距离
     * - 值越大，聚焦效果越明显
     * - 建议范围：5-20像素
     * - 默认：10像素
     */
    public static final ModConfigSpec.ConfigValue<Integer> FOCUS_HEIGHT;

    /**
     * 自定义UI管理的法术数量上限
     * 控制有多少个法术槽使用自定义UI显示
     *
     * 使用场景：
     * - 默认值 8：前8个法术使用自定义UI，第9个及以后使用原版系统
     * - 设为 0：完全禁用自定义UI，所有法术使用原版系统
     * - 设为大值(如999)：所有法术都使用自定义UI
     *
     * 注意：此配置不影响实际可学习的法术数量，仅影响UI显示方式
     */
    public static final ModConfigSpec.ConfigValue<Integer> MINE_CUSTOM_SPELL;

    /**
     * 动画缓动模式
     * 控制法术槽在状态切换时的移动动画曲线
     *
     * 可选模式：
     * - 0 (Smoothstep)    - 平滑加减速，自然流畅【推荐】
     * - 1 (EaseOutBack)   - 快速到达并回弹约20%，活泼动感
     * - 2 (EaseOutCubic)  - 快速启动平滑结束，干脆利落
     * - 其他值 (Linear)   - 匀速运动，简单直接
     *
     * 默认：0 (Smoothstep)
     */
    public static final ModConfigSpec.ConfigValue<Integer> SPEED_LINE_MODE;

    public static final ModConfigSpec SPEC;
    
    static {
        // ========== UI Display Settings ==========

        ENABLE_CUSTOM_UI = BUILDER
                .comment("Enable custom spell selection interface",
                        "true: Use custom UI",
                        "false: Use vanilla UI",
                        "Default: true")
                .define("Enable Custom Bar", true);
        
        ENABLE_BACKGROUND = BUILDER
                .comment("Enable spell selection interface background",
                        "true: Render background",
                        "false: Do not render background",
                        "Default: true")
                .define("Enable Background", true);

        // ========== Position Offset Settings ==========

        BACKGROUND_X_OFFSET = BUILDER
                .comment("Background X Offset",
                        "Horizontal offset of the spell selection background",
                        "Positive: right, Negative: left",
                        "Unit: pixels",
                        "Default: 0")
                .define("Background X Offset", 0);

        BACKGROUND_Y_OFFSET = BUILDER
                .comment("Background Y Offset",
                        "Vertical offset of the spell selection background",
                        "Positive: down, Negative: up",
                        "Unit: pixels",
                        "Default: 0")
                .define("Background Y Offset", 0);

        SPELL_X_OFFSET = BUILDER
                .comment("Spell X Offset",
                        "Horizontal offset of spell icons (independent of background)",
                        "Positive: right, Negative: left",
                        "Unit: pixels",
                        "Default: 0")
                .define("Spell X Offset", 0);

        SPELL_Y_OFFSET = BUILDER
                .comment("Spell Y Offset",
                        "Vertical offset of spell icons (independent of background)",
                        "Positive: down, Negative: up",
                        "Unit: pixels",
                        "Default: 0")
                .define("Spell Y Offset", 0);

        // ========== Animation and Interaction Settings ==========

        FOCUS_HEIGHT = BUILDER
                .comment("Focus state additional height",
                        "Height that the spell group moves up when focused",
                        "Larger values = more noticeable, recommended range: 5-20",
                        "Unit: pixels",
                        "Default: 10")
                .define("Focus Height", 10);
        
        MINE_CUSTOM_SPELL = BUILDER
                .comment("Custom spell count limit",
                        "Controls the number of spell slots using custom UI",
                        "Spells beyond this limit will fall back to vanilla system",
                        "Examples:",
                        "  8 (default): First 8 spells use custom UI",
                        "  0: Completely use vanilla system",
                        "  999: All spells use custom UI",
                        "Default: 8")
                .define("Mine Custom Spell Limit", 9);

        SPEED_LINE_MODE = BUILDER
                .comment("Animation easing mode",
                        "Controls movement animation curve for spell slot transitions",
                        "Available modes:",
                        "  0 - Smoothstep: Smooth acceleration/deceleration, natural (recommended)",
                        "  1 - EaseOutBack: Fast approach with ~20% overshoot bounce, dynamic",
                        "  2 - EaseOutCubic: Fast start smooth stop, clean",
                        "  Other values - Linear: Constant speed, simple",
                        "Default: 0 (Smoothstep)")
                .defineInRange("Speed Line Mode", 1, 0, 2);

        SPEC = BUILDER.build();
    }
}
