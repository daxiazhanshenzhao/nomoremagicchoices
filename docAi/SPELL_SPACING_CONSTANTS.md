# 法术间隔常量优化

## 修改时间
2025-12-10

## 修改内容

### 添加的常量
在 `ScrollSpellWight` 类中添加了两个常量，用于控制不同状态下法术槽之间的水平间隔：

```java
/** Down状态下的法术槽水平间隔（像素） */
public static final int SPELL_SPACING_DOWN = 20;

/** Focus状态下的法术槽水平间隔（像素） */
public static final int SPELL_SPACING_FOCUS = 24;
```

### 修改的逻辑

#### 1. 状态间隔规则
- **Down状态**：使用 `SPELL_SPACING_DOWN = 20` 像素间隔（紧凑排列）
- **Moving状态**：使用 `SPELL_SPACING_DOWN = 20` 像素间隔（与Down保持一致）
- **Focus状态**：使用 `SPELL_SPACING_FOCUS = 24` 像素间隔（更宽松，突出显示）

#### 2. render方法优化
修改了 `render()` 方法的实现：

```java
// 根据状态选择法术间隔
int spacing = (state == State.Focus) ? SPELL_SPACING_FOCUS : SPELL_SPACING_DOWN;

// 为每个法术设置不同的X坐标
int slotOffsetX = slotIndex * spacing;
```

替代了之前的硬编码值 `slotIndex * 20` 和 `slotIndex * 24`。

## 优势

### 1. 易于调整
现在只需要修改类常量即可调整所有法术槽的间隔，无需在代码多处修改。

### 2. 代码可读性
使用命名常量比魔法数字更清晰，一眼就能看出不同状态使用的间隔值。

### 3. 维护性
如果需要调整UI布局，只需修改常量定义，降低出错风险。

## 使用示例

### 调整间隔大小
如果觉得Focus状态下法术槽太紧密，可以直接修改：

```java
// 将Focus状态的间隔从24改为28像素
public static final int SPELL_SPACING_FOCUS = 28;
```

### 调整Down状态间隔
如果觉得Down状态下法术槽太紧密，可以修改：

```java
// 将Down状态的间隔从20改为22像素
public static final int SPELL_SPACING_DOWN = 22;
```

## 技术细节

### Moving状态的处理
Moving状态使用Down的间隔值，这样在移动过程中：
- 从Down到Focus：法术槽从紧凑逐渐变宽松
- 从Focus到Down：法术槽从宽松逐渐变紧凑

这种处理让动画过渡更自然，因为间隔变化是在移动完成后状态切换时发生的，而不是在Moving过程中。

### 计算逻辑
```java
int slotOffsetX = slotIndex * spacing;
```
- slotIndex: 从0开始的法术槽索引
- spacing: 根据状态选择的间隔值
- slotOffsetX: 该法术槽相对于组Widget中心的X偏移

例如：
- Down状态，第2个法术槽（slotIndex=1）：偏移 = 1 * 20 = 20像素
- Focus状态，第2个法术槽（slotIndex=1）：偏移 = 1 * 24 = 24像素

## 相关文件
- `ScrollSpellWight.java` - 添加常量和修改渲染逻辑

## 后续可能的优化
1. 可以考虑添加垂直间隔常量（如果将来需要垂直方向的法术槽排列）
2. 可以添加配置文件支持，让玩家自定义这些间隔值
3. 可以添加动画过渡，让间隔变化更平滑

