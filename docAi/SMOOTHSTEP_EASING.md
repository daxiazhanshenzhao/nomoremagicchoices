# Smoothstep 缓动函数详解

> **文件说明**: 本文档详细介绍了 `ScrollSpellWight.java` 中使用的 Smoothstep 缓动函数
> 
> **创建日期**: 2025-12-07
> 
> **相关文件**: 
> - Java 实现: `src/main/java/org/nomoremagicchoices/gui/component/ScrollSpellWight.java`
> - MATLAB 可视化: `docAi/easing_functions.m` 和 `docAi/smoothstep_analysis.m`

---

## 📐 数学公式

### 标准形式
```
f(t) = 3t² - 2t³
```

### 分解形式
```
f(t) = t² × (3 - 2t)
```

### 导数（速度）
```
f'(t) = 6t - 6t²
```

### 二阶导数（加速度）
```
f''(t) = 6 - 12t
```

---

## 💻 Java 实现

### 当前代码实现
```java
public double getRealOffset(double interpolatedOffset){
    return interpolatedOffset * interpolatedOffset * (3.0 - 2.0 * interpolatedOffset);
}
```

### 完整的插值计算流程
```java
// 1. 在 tick() 方法中更新基础offset
public void tick(){
    if (state.equals(State.Moving)){
        setOffset(offset + (double) 1 / TOTAL_TICKS);
    }
}

// 2. 在 render() 方法中计算帧间插值
double interpolatedOffset = offset + partialTick.getGameTimeDeltaTicks() / TOTAL_TICKS;

// 3. 应用 Smoothstep 缓动函数
double realOffset = getRealOffset(interpolatedOffset);

// 4. 使用 realOffset 计算实际渲染位置
Vector2i position = getPosition(realOffset);
```

---

## 📊 关键数值表

| 时间 t | 位置 f(t) | 速度 f'(t) | 加速度 f''(t) | 说明 |
|--------|-----------|------------|---------------|------|
| 0.00   | 0.000000  | 0.000000   | 6.000000      | 起点，静止状态 |
| 0.10   | 0.028000  | 0.540000   | 4.800000      | 开始加速 |
| 0.25   | 0.156250  | 1.125000   | 3.000000      | 加速中 |
| 0.50   | 0.500000  | 1.500000   | 0.000000      | 最大速度点 |
| 0.75   | 0.843750  | 1.125000   | -3.000000     | 减速中 |
| 0.90   | 0.972000  | 0.540000   | -4.800000     | 即将停止 |
| 1.00   | 1.000000  | 0.000000   | -6.000000     | 终点，静止状态 |

---

## 🎯 函数特性分析

### ✅ 优点

1. **平滑启动和停止**
   - 在 t=0 和 t=1 时速度为 0
   - 给人自然、舒适的视觉体验

2. **简单高效**
   - 只需要两次乘法和一次减法
   - 计算复杂度: O(1)
   - 适合每帧调用

3. **对称性**
   - 关于点 (0.5, 0.5) 中心对称
   - 加速和减速曲线完全对称

4. **连续性**
   - 函数本身连续
   - 一阶导数连续（速度无跳变）
   - 二阶导数连续（加速度平滑）

### ⚠️ 注意事项

1. **最大速度点**
   - 在 t=0.5 时达到最大速度 1.5
   - 比线性插值快 50%

2. **前后对称**
   - 如果需要不对称的缓动（如快进慢出），需要使用其他函数

---

## 🔄 与其他缓动函数对比

### 1. **Linear（线性）**
```java
realOffset = interpolatedOffset;
```
- **特点**: 匀速运动
- **适用**: 机械式移动，不需要动画感
- **缺点**: 开始和结束太突兀

### 2. **Smoothstep（当前使用）** ⭐
```java
realOffset = interpolatedOffset * interpolatedOffset * (3.0 - 2.0 * interpolatedOffset);
```
- **特点**: 平滑启动和停止
- **适用**: 大多数UI动画
- **优点**: 计算简单，效果好

### 3. **Sin 缓动**
```java
realOffset = (Math.sin((interpolatedOffset - 0.5) * Math.PI) + 1.0) / 2.0;
```
- **特点**: 最自然的S型曲线
- **适用**: 需要极致平滑的场景
- **缺点**: 计算复杂（三角函数）

### 4. **开方组合**
```java
realOffset = interpolatedOffset < 0.5 
    ? 2.0 * interpolatedOffset * interpolatedOffset 
    : 1.0 - 2.0 * (1.0 - interpolatedOffset) * (1.0 - interpolatedOffset);
```
- **特点**: 加速更明显
- **适用**: 快速响应的交互
- **缺点**: 分段函数，有条件判断

### 5. **Smootherstep**
```java
realOffset = interpolatedOffset * interpolatedOffset * interpolatedOffset 
    * (interpolatedOffset * (interpolatedOffset * 6.0 - 15.0) + 10.0);
```
- **特点**: 更平滑的加速度曲线
- **适用**: 慢动作或强调动画
- **缺点**: 计算量大

---

## 🎬 动画时序说明

### Minecraft 游戏循环中的应用

```
游戏循环: 20 TPS (Ticks Per Second)
渲染循环: 60+ FPS (Frames Per Second)

假设 TOTAL_TICKS = 20:

Tick 0:  offset = 0/20 = 0.00  → realOffset = 0.000
  └─ Frame 1:  interpolated = 0.00 + 0.0/20 = 0.000 → real = 0.000
  └─ Frame 2:  interpolated = 0.00 + 0.5/20 = 0.025 → real = 0.002
  └─ Frame 3:  interpolated = 0.00 + 1.0/20 = 0.050 → real = 0.008

Tick 1:  offset = 1/20 = 0.05  → realOffset = 0.007
  └─ Frame 4:  interpolated = 0.05 + 0.0/20 = 0.050 → real = 0.007
  └─ Frame 5:  interpolated = 0.05 + 0.5/20 = 0.075 → real = 0.015
  └─ Frame 6:  interpolated = 0.05 + 1.0/20 = 0.100 → real = 0.028

...

Tick 10: offset = 10/20 = 0.50 → realOffset = 0.500 (中点，最大速度)

...

Tick 20: offset = 20/20 = 1.00 → realOffset = 1.000 (终点)
```

### 平滑度提升

- **不使用插值**: 20个位置变化（跳跃式）
- **使用线性插值**: 60+个位置变化（匀速）
- **使用Smoothstep**: 60+个位置变化（加速+减速）

---

## 📈 性能优化建议

### 当前实现已经很高效

```java
// ✅ 优化后的版本（当前使用）
double realOffset = interpolatedOffset * interpolatedOffset * (3.0 - 2.0 * interpolatedOffset);
```

### 可能的微优化（不推荐）

```java
// ⚠️ 预计算常量（几乎无性能提升）
double t2 = interpolatedOffset * interpolatedOffset;
double realOffset = t2 * (3.0 - 2.0 * interpolatedOffset);
```

**结论**: 当前实现已经足够高效，无需优化。

---

## 🧪 MATLAB 测试说明

### 运行可视化脚本

1. **完整对比分析**
   ```matlab
   cd('C:\Users\hp\IdeaProjects\VerShift\nomoremagicchoices\docAi')
   run('easing_functions.m')
   ```
   - 生成 5 种缓动函数的对比图
   - 输出速度和位置数据表
   - 保存为 `easing_functions_comparison.png`

2. **Smoothstep 详细分析**
   ```matlab
   run('smoothstep_analysis.m')
   ```
   - 专注于 Smoothstep 函数特性
   - 包含导数和加速度分析
   - 模拟实际游戏中的动画效果
   - 保存为 `smoothstep_detailed_analysis.png`

### 查看图像内容

- **子图 1**: 位置曲线对比
- **子图 2**: 速度曲线对比（体现加速度变化）
- **子图 3**: Smoothstep 详细分析
- **子图 4**: 动画模拟（分帧展示）
- **子图 5**: 实际 tick 点分布
- **子图 6**: 关键特性文字说明

---

## 🎨 实际应用示例

### 法术滚动动画

```java
// 计算法术图标的Y坐标
public Vector2i getPosition(double realOffset) {
    int startY = center.y;
    int endY = ender.y;
    int currentY = (int)(startY + (endY - startY) * realOffset);
    return new Vector2i(center.x, currentY);
}

// 在 render 方法中使用
switch (state) {
    case Moving:
        double interpolatedOffset = offset + partialTick.getGameTimeDeltaTicks() / TOTAL_TICKS;
        double realOffset = getRealOffset(interpolatedOffset);
        Vector2i position = getPosition(realOffset);
        renderSlot(context, spellData, position.x, position.y);
        break;
}
```

---

## 📚 参考资料

- [Wikipedia: Smoothstep](https://en.wikipedia.org/wiki/Smoothstep)
- [Easings.net](https://easings.net/) - 缓动函数可视化
- [Minecraft Wiki: Rendering](https://minecraft.fandom.com/wiki/Rendering)

---

## ✏️ 修改建议

如果觉得动画效果不满意，可以尝试：

### 更平滑（慢）
```java
// Smootherstep
double realOffset = interpolatedOffset * interpolatedOffset * interpolatedOffset 
    * (interpolatedOffset * (interpolatedOffset * 6.0 - 15.0) + 10.0);
```

### 更快速（敏锐）
```java
// 开方组合
double realOffset = interpolatedOffset < 0.5 
    ? 2.0 * interpolatedOffset * interpolatedOffset 
    : 1.0 - 2.0 * (1.0 - interpolatedOffset) * (1.0 - interpolatedOffset);
```

### 最自然
```java
// Sin缓动
double realOffset = (Math.sin((interpolatedOffset - 0.5) * Math.PI) + 1.0) / 2.0;
```

---

**最后更新**: 2025-12-07

