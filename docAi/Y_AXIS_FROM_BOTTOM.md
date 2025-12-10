# Y轴坐标系统改为从屏幕底部计算

## 修改时间
2025-12-10

## 修改内容

### 问题描述
原来的Y轴坐标是从屏幕中心开始计算偏移的：
```java
int baseY = screenHeight / 2 + 20;
```

这种方式的问题：
- 不同分辨率下，Widget相对于屏幕底部的距离会变化
- 难以保持Widget在屏幕底部的固定位置
- 调整时需要考虑屏幕中心的位置

### 解决方案
改为从屏幕底部开始计算：
```java
int baseY = screenHeight - 80;
```

### 常量定义

#### SpellSelectionLayerV2.java
```java
/** Widget渲染基础位置距离屏幕底部的距离（像素，正值表示向上） */
public static final int BASE_Y_OFFSET_FROM_BOTTOM = 80;
```

#### ClientScrollData.java
```java
// Y轴：屏幕高度 - 距离底部的距离
int baseY = screenHeight - org.nomoremagicchoices.gui.SpellSelectionLayerV2.BASE_Y_OFFSET_FROM_BOTTOM;
```

## 坐标计算逻辑

### 修改前（从屏幕中心）
```
屏幕顶部 (Y=0)
    ↓
    ↓
屏幕中心 (Y=screenHeight/2)
    ↓ +20像素
baseY ← Widget基础位置
    ↓
    ↓
屏幕底部 (Y=screenHeight)
```

**问题**：
- 1080p显示器：baseY = 540 + 20 = 560
- 720p显示器：baseY = 360 + 20 = 380
- Widget距离底部的距离不一致

### 修改后（从屏幕底部）
```
屏幕顶部 (Y=0)
    ↓
    ↓
    ↓
baseY ← Widget基础位置
    ↑ 80像素（固定）
屏幕底部 (Y=screenHeight)
```

**优势**：
- 1080p显示器：baseY = 1080 - 80 = 1000
- 720p显示器：baseY = 720 - 80 = 640
- Widget始终距离底部80像素，位置一致

## 坐标示例

### 1080p分辨率 (1920×1080)
```
修改前：
baseY = 1080 / 2 + 20 = 560
距离底部 = 1080 - 560 = 520像素

修改后：
baseY = 1080 - 80 = 1000
距离底部 = 1080 - 1000 = 80像素 ✓
```

### 720p分辨率 (1280×720)
```
修改前：
baseY = 720 / 2 + 20 = 380
距离底部 = 720 - 380 = 340像素

修改后：
baseY = 720 - 80 = 640
距离底部 = 720 - 640 = 80像素 ✓
```

### 4K分辨率 (3840×2160)
```
修改前：
baseY = 2160 / 2 + 20 = 1100
距离底部 = 2160 - 1100 = 1060像素

修改后：
baseY = 2160 - 80 = 2080
距离底部 = 2160 - 2080 = 80像素 ✓
```

## Widget排列示例

假设有3个Widget，WIDGET_VERTICAL_SPACING = 8：

```
屏幕底部 (Y=screenHeight)
    ↑ 80像素
Widget[2] (Y = baseY + 16) ← 第三个组
    ↑ 8像素
Widget[1] (Y = baseY + 8)  ← 第二个组
    ↑ 8像素
Widget[0] (Y = baseY)      ← currentGroup（最上面）
```

在1080p显示器上：
- Widget[0]: Y = 1000 (距离底部80像素)
- Widget[1]: Y = 1008 (距离底部72像素)
- Widget[2]: Y = 1016 (距离底部64像素)

## 调整建议

### 如果想让Widget更靠近底部
```java
public static final int BASE_Y_OFFSET_FROM_BOTTOM = 50; // 距离底部50像素
```

### 如果想让Widget更远离底部
```java
public static final int BASE_Y_OFFSET_FROM_BOTTOM = 120; // 距离底部120像素
```

### 如果想让Widget在屏幕中间
```java
// 需要修改计算逻辑，或者使用一个很大的值
public static final int BASE_Y_OFFSET_FROM_BOTTOM = 540; // 1080p下在中间
```

## 优势总结

### 1. 一致性
不同分辨率下，Widget始终保持相对于屏幕底部的固定距离。

### 2. 直观性
`BASE_Y_OFFSET_FROM_BOTTOM = 80` 直接表示"距离底部80像素"，一目了然。

### 3. 易于调整
只需修改一个常量，就能精确控制Widget距离底部的距离。

### 4. 适应性
无论玩家使用何种分辨率，UI布局都保持一致的视觉效果。

## 相关文件
- `SpellSelectionLayerV2.java` - 定义常量
- `ClientScrollData.java` - 使用常量计算坐标

## 技术细节

### 坐标系统
Minecraft GUI坐标系统：
- 原点 (0, 0) 在屏幕左上角
- X轴向右为正
- Y轴向下为正
- 屏幕底部：Y = screenHeight

### 计算公式
```java
baseY = screenHeight - BASE_Y_OFFSET_FROM_BOTTOM
```

其中：
- `screenHeight`：当前屏幕高度（像素）
- `BASE_Y_OFFSET_FROM_BOTTOM`：距离底部的距离（像素）
- `baseY`：Widget基础位置的Y坐标

### 其他Widget位置
```java
// 空手模式
Widget[i] Y坐标 = baseY + (i * WIDGET_VERTICAL_SPACING)

// 武器模式
Widget[0] (Focus) Y坐标 = baseY + FOCUS_Y_OFFSET
Widget[i>0] (Down) Y坐标 = baseY + ((i-1) * WIDGET_VERTICAL_SPACING)
```

## 未来改进
1. 可以添加X轴也从屏幕右边缘计算的选项
2. 支持配置文件让玩家自定义距离
3. 添加安全边距，确保Widget不会超出屏幕

