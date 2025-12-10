# 背景图片渲染修复 - 105×36纹理

## 修复时间
2025-12-10

## 图片信息
- **文件**: `bg.png`
- **尺寸**: 105×36 像素
- **位置**: `textures/gui/bg.png`
- **特点**: 完全占满，无空白边距

## 问题描述
之前的 `blit` 调用缺少纹理总大小参数，导致 Minecraft 无法正确计算 UV 坐标，图片无法正常显示。

## 修复内容

### 修复前（错误）
```java
context.blit(event.getTexture(), event.getX(), event.getY(), 
             event.getuOffset(), event.getvOffset(), 
             event.getWidth(), event.getHeight());
// ❌ 缺少纹理总大小参数
```

### 修复后（正确）
```java
context.blit(event.getTexture(), event.getX(), event.getY(), 
             event.getuOffset(), event.getvOffset(), 
             event.getWidth(), event.getHeight(), 
             105, 36); // ✅ 添加纹理总大小
```

## GuiGraphics.blit() 方法说明

### 方法签名
```java
public void blit(
    ResourceLocation texture,  // 纹理资源位置
    int x,                     // 屏幕X坐标
    int y,                     // 屏幕Y坐标
    int u,                     // 纹理U坐标（左上角）
    int v,                     // 纹理V坐标（左上角）
    int width,                 // 渲染宽度
    int height,                // 渲染高度
    int textureWidth,          // 纹理文件总宽度
    int textureHeight          // 纹理文件总高度
)
```

### 参数说明

#### 屏幕坐标 (x, y)
在您的代码中：
```java
var x = screenWidth / 2 - 100;  // 屏幕中心左移100像素
var y = screenHeight - 20;       // 距离屏幕底部20像素
```

#### 纹理UV坐标 (u, v)
当前设置为 `(0, 0)`，表示从纹理左上角开始读取。

#### 渲染大小 (width, height)
当前设置为 `(105, 36)`，与纹理实际大小一致，表示按原尺寸渲染。

#### 纹理总大小 (textureWidth, textureHeight)
**关键参数**：设置为 `(105, 36)`，告诉 Minecraft 纹理文件的实际尺寸。

## UV坐标计算原理

### 为什么需要纹理总大小？
Minecraft 内部使用归一化的 UV 坐标（0.0 到 1.0）：
```
实际UV = 像素坐标 / 纹理总大小
```

### 示例计算
对于您的 105×36 纹理：
```
左上角 (0, 0):
  U = 0 / 105 = 0.0
  V = 0 / 36 = 0.0

右下角 (105, 36):
  U = 105 / 105 = 1.0
  V = 36 / 36 = 1.0
```

### 如果参数错误会怎样？
假设错误地使用 256×256：
```
右下角计算:
  U = 105 / 256 = 0.41
  V = 36 / 256 = 0.14
```
结果：只渲染纹理的左上角 41%×14% 区域，图片不完整。

## 渲染位置示例

### 1920×1080 分辨率
```
x = 1920 / 2 - 100 = 860
y = 1080 - 20 = 1060

渲染位置：(860, 1060)
渲染区域：860 到 965（宽105）, 1060 到 1096（高36）
```

### 1280×720 分辨率
```
x = 1280 / 2 - 100 = 540
y = 720 - 20 = 700

渲染位置：(540, 700)
渲染区域：540 到 645（宽105）, 700 到 736（高36）
```

## 完整代码
```java
@Override
public void renderBg(ResourceLocation texture, GuiGraphics context, 
                     int x, int y, int uOffset, int vOffset, 
                     int width, int height) {
    RenderBgEvent event = new RenderBgEvent(texture, context, x, y, 
                                             uOffset, vOffset, width, height);
    NeoForge.EVENT_BUS.post(event);

    if (event.isCanceled()) return;

    // blit参数: (纹理, 屏幕X, 屏幕Y, 纹理U, 纹理V, 渲染宽, 渲染高, 纹理总宽, 纹理总高)
    // 您的bg.png是105×36，完全占满
    context.blit(event.getTexture(), event.getX(), event.getY(), 
                 event.getuOffset(), event.getvOffset(), 
                 event.getWidth(), event.getHeight(), 
                 105, 36);
}
```

## 测试检查清单

### ✅ 代码检查
- [x] 添加了纹理总大小参数 `105, 36`
- [x] UV偏移设置为 `0, 0`（从左上角开始）
- [x] 渲染大小设置为 `105, 36`（与纹理大小一致）
- [x] 无编译错误

### ✅ 文件检查
确认以下文件存在且正确：
```
src/main/resources/assets/nomoremagicchoices/textures/gui/bg.png
```
- 文件尺寸：105×36 像素
- 文件格式：PNG
- 文件名：bg.png

### ✅ 游戏内测试
启动游戏后检查：
1. 背景图片是否正常显示
2. 图片是否完整（没有被截断）
3. 图片位置是否正确（屏幕底部，中间偏左）
4. 图片大小是否正确（105×36）

## 调整位置

### 如果想调整水平位置
```java
// 更靠左
var x = screenWidth / 2 - 150;

// 更靠右
var x = screenWidth / 2 - 50;

// 完全居中
var x = screenWidth / 2 - 52; // 52 = 105 / 2
```

### 如果想调整垂直位置
```java
// 更靠下（更接近底部）
var y = screenHeight - 10;

// 更靠上（远离底部）
var y = screenHeight - 50;
```

## 常见问题

### Q1: 图片显示不全？
A: 检查纹理总大小参数是否正确设置为 `105, 36`。

### Q2: 图片被拉伸或压缩？
A: 确保渲染宽高 `(105, 36)` 与纹理实际大小一致。

### Q3: 图片完全不显示？
A: 检查文件路径和文件名是否正确，确认 `bg.png` 存在。

### Q4: 图片位置不对？
A: 调整计算公式中的偏移量：
```java
var x = screenWidth / 2 - 100; // 调整这个100
var y = screenHeight - 20;      // 调整这个20
```

## 相关文件
- `SpellSelectionLayerV2.java` - 渲染层实现
- `bg.png` - 背景图片（105×36）
- `RenderBgEvent.java` - 背景渲染事件

## 总结
通过添加正确的纹理总大小参数 `(105, 36)`，Minecraft 现在能够正确计算 UV 坐标，使 105×36 的背景图片完整显示。

