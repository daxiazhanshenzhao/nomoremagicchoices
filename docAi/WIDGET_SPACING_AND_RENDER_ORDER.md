# Widget间距常量优化与渲染顺序调整

## 修改时间
2025-12-10

## 修改内容

### 1. 添加间距常量到 SpellSelectionLayerV2

在 `SpellSelectionLayerV2.java` 中添加了两个公共静态常量：

```java
/** Widget在Y轴的垂直间隔（像素） */
public static final int WIDGET_VERTICAL_SPACING = 30;

/** Focus状态下Widget相对于基础位置的Y轴偏移（像素，负值表示向上） */
public static final int FOCUS_Y_OFFSET = -50;
```

#### 优势
- **集中管理**：所有UI相关的布局参数都在渲染层统一管理
- **易于调整**：修改间距时只需改一个地方
- **语义清晰**：常量名称明确说明了用途

### 2. 修改 ClientScrollData 使用新常量

将硬编码的间距值替换为对 `SpellSelectionLayerV2` 常量的引用：

**修改前：**
```java
int y = baseY + (i * 30); // 硬编码
int focusY = baseY - 50;  // 硬编码
```

**修改后：**
```java
int y = baseY + (i * org.nomoremagicchoices.gui.SpellSelectionLayerV2.WIDGET_VERTICAL_SPACING);
int focusY = baseY + org.nomoremagicchoices.gui.SpellSelectionLayerV2.FOCUS_Y_OFFSET;
```

### 3. 调整渲染顺序

修改了 `SpellSelectionLayerV2.render()` 方法中的渲染循环顺序。

**修改前：**
```java
// 正序渲染（从小到大）
for (ScrollSpellWight wight : wightList) {
    if (wight != null && wight != ScrollSpellWight.EMPTY) {
        wight.render(context, partialTick);
    }
}
```

**修改后：**
```java
// 倒序渲染（从大到小）
for (int i = wightList.size() - 1; i >= 0; i--) {
    ScrollSpellWight wight = wightList.get(i);
    if (wight != null && wight != ScrollSpellWight.EMPTY) {
        wight.render(context, partialTick);
    }
}
```

#### 渲染顺序说明
- **列表顺序**：index 0, 1, 2, 3 ... (小→大)
- **渲染顺序**：index大的先渲染，index小的后渲染
- **显示效果**：index小的Widget显示在最上层（覆盖index大的）

#### 为什么这样渲染？
在GUI渲染中，后绘制的内容会覆盖先绘制的内容。因此：
1. 先渲染 index 3 (最底层)
2. 再渲染 index 2
3. 再渲染 index 1
4. 最后渲染 index 0 (最顶层)

这样可以实现**重叠堆放**效果，index小的Widget在最上方。

## 使用场景

### 场景1：调整Widget间距
如果觉得Widget之间太紧密或太松散：

```java
// 在 SpellSelectionLayerV2.java 中修改
public static final int WIDGET_VERTICAL_SPACING = 35; // 改为35像素
```

所有使用该常量的地方会自动更新。

### 场景2：调整Focus位置
如果觉得Focus状态的Widget距离底部Widget太近或太远：

```java
// 在 SpellSelectionLayerV2.java 中修改
public static final int FOCUS_Y_OFFSET = -60; // 改为向上60像素
```

### 场景3：实现重叠堆放效果
现在的渲染顺序支持Widget重叠堆放：
- 最上层（index 0）：currentGroup - 完全可见
- 第二层（index 1）：可能被部分遮挡
- 第三层（index 2）：可能被部分遮挡
- 底层（index 3）：可能被大部分遮挡

## 技术细节

### 常量访问方式
由于 `ClientScrollData` 和 `SpellSelectionLayerV2` 不在同一个包，需要使用完整类名：

```java
org.nomoremagicchoices.gui.SpellSelectionLayerV2.WIDGET_VERTICAL_SPACING
org.nomoremagicchoices.gui.SpellSelectionLayerV2.FOCUS_Y_OFFSET
```

如果觉得太长，可以在 `ClientScrollData` 中添加静态导入：
```java
import static org.nomoremagicchoices.gui.SpellSelectionLayerV2.WIDGET_VERTICAL_SPACING;
import static org.nomoremagicchoices.gui.SpellSelectionLayerV2.FOCUS_Y_OFFSET;
```

### 渲染顺序对比

| 渲染方式 | Widget 渲染顺序 | 显示结果 |
|---------|----------------|---------|
| 正序 (旧) | 0→1→2→3 | index大的在最上层 |
| 倒序 (新) | 3→2→1→0 | index小的在最上层 |

### 位置计算逻辑
```java
// 空手模式（所有Widget都在Down状态）
Widget[0]: baseY + (0 * 30) = baseY + 0   ← 最上层，后渲染
Widget[1]: baseY + (1 * 30) = baseY + 30
Widget[2]: baseY + (2 * 30) = baseY + 60
Widget[3]: baseY + (3 * 30) = baseY + 90  ← 最底层，先渲染

// 持有武器模式（最后一个在Focus位置）
Widget[0]: baseY + (0 * 30) = baseY + 0
Widget[1]: baseY + (1 * 30) = baseY + 30
Widget[2]: baseY + (2 * 30) = baseY + 60
Widget[3]: baseY - 50 (Focus位置)         ← currentGroup，在顶部
```

## 配置建议

### 紧凑布局
```java
public static final int WIDGET_VERTICAL_SPACING = 25;
public static final int FOCUS_Y_OFFSET = -40;
```

### 标准布局（当前）
```java
public static final int WIDGET_VERTICAL_SPACING = 30;
public static final int FOCUS_Y_OFFSET = -50;
```

### 宽松布局
```java
public static final int WIDGET_VERTICAL_SPACING = 35;
public static final int FOCUS_Y_OFFSET = -60;
```

### 堆叠布局（重叠效果）
```java
public static final int WIDGET_VERTICAL_SPACING = 15; // 小间距
public static final int FOCUS_Y_OFFSET = -50;
```

## 相关文件
- `SpellSelectionLayerV2.java` - 添加常量，修改渲染顺序
- `ClientScrollData.java` - 使用新常量替代硬编码

## 视觉效果

### 渲染顺序示意图
```
渲染顺序：3 → 2 → 1 → 0

屏幕显示：
┌─────────┐
│Widget 0 │ ← 最上层（currentGroup，最后渲染）
├─────────┤
│Widget 1 │
├─────────┤
│Widget 2 │
├─────────┤
│Widget 3 │ ← 最底层（先渲染）
└─────────┘
```

### 重叠效果示意图
当 `WIDGET_VERTICAL_SPACING` 较小时：
```
┌─────────┐
│Widget 0 │ ← 完全可见
│─────────│
│Widget 1 │ ← 部分被遮挡
│─────────│
│Widget 2 │ ← 大部分被遮挡
└─────────┘
  Widget 3   ← 几乎完全被遮挡
```

## 未来改进方向
1. 添加配置文件支持，让玩家自定义间距
2. 支持动态调整间距（如按住Shift时展开所有Widget）
3. 添加鼠标悬停时的高亮效果
4. 支持水平堆叠模式

