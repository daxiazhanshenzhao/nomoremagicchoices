# SpellSelectionProvider 使用指南

## ✅ 已完成的实现

你的需求已经完全实现！现在你可以通过 `new SpellSelectionProvider()` 来动态地根据客户端配置文件选择性注册两个 Overlay。

## 📁 文件结构

```
src/main/java/org/nomoremagicchoices/
├── Nomoremagicchoices.java          # 主类，注册配置文件
├── config/
│   └── ClientConfig.java            # 客户端配置
├── gui/
│   ├── SpellSelectionProvider.java  # 动态选择器 ⭐
│   ├── SpellSelectionLayerV1.java   # 自定义UI
│   └── (SpellBarOverlay)            # 原版UI（来自 IronsSpellbooks）
└── api/init/
    └── OverlayInit.java             # Overlay 注册
```

## 🎯 核心实现

### 1. **SpellSelectionProvider.java** - 智能动态选择器

```java
public class SpellSelectionProvider implements LayeredDraw.Layer {
    
    private final SpellSelectionLayerV1 customLayer;
    private final LayeredDraw.Layer defaultLayer;

    public SpellSelectionProvider() {
        this.customLayer = new SpellSelectionLayerV1();
        this.defaultLayer = SpellBarOverlay.instance;
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        // 每次渲染时动态检查配置
        if (ClientConfig.ENABLE_CUSTOM_UI.get()) {
            customLayer.render(guiGraphics, deltaTracker);
        } else {
            defaultLayer.render(guiGraphics, deltaTracker);
        }
    }
}
```

**工作原理：**
- ✨ 直接实现 `LayeredDraw.Layer` 接口
- ✨ 内部持有两个 Layer 实例（自定义和原版）
- ✨ 每次渲染时根据配置动态选择要渲染的 Layer
- ✨ **支持热切换**：配置改变后立即生效，无需重启游戏

### 2. **OverlayInit.java** - 一次注册，永久生效

```java
@EventBusSubscriber(Dist.CLIENT)
public class OverlayInit {

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiLayersEvent event) {
        // 只需注册一次！
        event.registerBelow(
            VanillaGuiLayers.EXPERIENCE_BAR, 
            ResourceLocation.fromNamespaceAndPath(Nomoremagicchoices.MODID, "spell_selection"),
            new SpellSelectionProvider()  // 👈 这就是你想要的效果！
        );
    }
}
```

### 3. **ClientConfig.java** - 配置文件

```java
public class ClientConfig {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.ConfigValue<Boolean> ENABLE_CUSTOM_UI;
    public static final ModConfigSpec SPEC;

    static {
        ENABLE_CUSTOM_UI = BUILDER.define("Enable Custom Bar", true);
        SPEC = BUILDER.build();
    }
}
```

### 4. **Nomoremagicchoices.java** - 注册配置

```java
public Nomoremagicchoices(IEventBus modEventBus, ModContainer modContainer) {
    // 注册客户端配置文件
    modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
}
```

## 🎮 使用方式

### 玩家如何切换 UI？

配置文件位置：`.minecraft/config/nomoremagicchoices-client.toml`

```toml
# 启用自定义法术栏
"Enable Custom Bar" = true   # true = 自定义UI, false = 原版UI
```

### 优势

✅ **简单易用**：只需 `new SpellSelectionProvider()` 即可  
✅ **动态切换**：配置改变后立即生效  
✅ **性能优化**：只创建一次实例，避免重复创建  
✅ **解耦设计**：注册代码和选择逻辑分离  
✅ **易于扩展**：未来可以轻松添加更多 UI 选项

## 🔧 未来扩展

如果你想添加更多 UI 选项，只需：

1. 修改 `ClientConfig.java`，将 Boolean 改为 Enum
2. 在 `SpellSelectionProvider.render()` 中添加更多分支

示例：
```java
// ClientConfig.java
public enum UIStyle { CUSTOM, VANILLA, COMPACT }
public static final ModConfigSpec.EnumValue<UIStyle> UI_STYLE;

// SpellSelectionProvider.java
@Override
public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
    switch (ClientConfig.UI_STYLE.get()) {
        case CUSTOM -> customLayer.render(guiGraphics, deltaTracker);
        case VANILLA -> defaultLayer.render(guiGraphics, deltaTracker);
        case COMPACT -> compactLayer.render(guiGraphics, deltaTracker);
    }
}
```

## 🎉 总结

你的需求已经完美实现！现在：
- ✅ 只需 `new SpellSelectionProvider()` 就能注册
- ✅ 自动根据配置文件选择正确的 Overlay
- ✅ 支持运行时动态切换
- ✅ 代码简洁、易维护

**享受你的智能 UI 切换系统吧！** 🚀

