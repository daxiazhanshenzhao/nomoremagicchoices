# 空手切换回落动画修复

## 问题描述
从空手切换到武器状态时，Widget会有上移动画，但是从武器切换回空手时，Widget不会自动回落到底部位置，需要再按一次R键才能看到效果。

## 问题原因
在 `ClientScrollData.handleStateChange()` 方法中，切换到空手状态时，代码只是简单地调用了 `spellWightList.forEach(ScrollSpellWight::down)`，这个方法只是设置Widget的状态为Down，而没有触发实际的移动动画。

```java
// 旧代码 - 只设置状态，不触发动画
if (newState == SpellSelectionState.EmptyHand) {
    spellWightList.forEach(ScrollSpellWight::down);
}
```

而 `ScrollSpellWight.down()` 方法的实现是：
```java
public void down(){
    this.state = State.Down;  // 仅仅设置状态，没有移动逻辑
}
```

## 解决方案
修改 `handleStateChange()` 方法，当切换到空手状态时：
1. 计算所有Widget在空手状态下的目标位置
2. 为每个Widget调用 `moveDown()` 方法，触发带动画的移动
3. `moveDown()` 方法会设置目标位置、重置offset、启动Moving状态，并在移动完成后自动设置为Down状态

```java
// 新代码 - 触发带动画的移动
if (newState == SpellSelectionState.EmptyHand) {
    // 切换到空手：所有Widget移动到Down位置，带动画
    List<Vector2i> positions = calculatePositions(newState);
    
    // 使用moveDown方法为所有Widget触发移动动画
    for (int i = 0; i < spellWightList.size(); i++) {
        ScrollSpellWight wight = spellWightList.get(i);
        Vector2i targetPos = positions.get(i);
        wight.moveDown(targetPos);
        Nomoremagicchoices.LOGGER.info("切换到空手: Widget[" + i + "] moveDown to (" + targetPos.x + ", " + targetPos.y + ")");
    }
}
```

## 技术细节

### moveDown() 方法的作用
`ScrollSpellWight.moveDown()` 方法执行以下操作：
1. 检查是否已经在移动中（避免重复触发）
2. 设置目标位置 `ender`
3. 重置offset为0（开始新的移动）
4. 设置状态为 `Moving`
5. 设置目标状态为 `Down`（移动完成后的状态）

### tick() 方法的动画逻辑
每个游戏tick，`ScrollSpellWight.tick()` 会：
1. 如果处于Moving状态，增加offset值（1.0 / TOTAL_TICKS）
2. 当offset >= 1.0时，移动完成
3. 设置状态为预设的目标状态（Down或Focus）
4. 更新center位置为ender位置
5. 重置offset为0

### 与持有物品状态的对比
- **切换到空手**：所有Widget使用 `moveDown()` 移动到底部位置
- **切换到武器**：通过 `ScrollGroupHelper.drawWight()` 重新排列Widget，当前组移到顶部（Focus位置）

## 测试验证
修复后，应该能观察到以下行为：
1. 从空手切换到武器：Widget平滑上移到Focus位置 ✓
2. 从武器切换到空手：Widget平滑下移回到Down位置 ✓（已修复）
3. 切换过程有8个tick的平滑动画
4. 不需要额外按R键就能看到动画效果

## 相关文件
- `ClientScrollData.java` - 主修改文件，handleStateChange()方法
- `ScrollSpellWight.java` - Widget类，包含moveDown()和tick()方法
- `ScrollGroupHelper.java` - 抽书/塞书逻辑辅助类

## 修改时间
2025-12-10

