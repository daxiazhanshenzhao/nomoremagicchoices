# 修复Widget移动被中断时的坐标错误问题

## 问题根源

经过深入分析，发现问题的根本原因在于 **Widget在Moving状态时，如果收到新的移动指令，会被直接忽略**。

### 问题场景重现

1. **场景1：施法期间切换物品**
   ```
   时刻T0: 玩家手持Weapon，Widget A在位置P1
   时刻T1: 玩家释放持续性法术，Widget A开始从P1移动到P2（Moving状态，offset=0.2）
   时刻T2: 玩家切换到Staff
   时刻T3: updateState() 检测到状态变为Staff，调用 handleStateChange()
   时刻T4: handleStateChange() 调用 moveFocus(P3)
   时刻T5: moveFocus() 检测到 state == Moving，直接 return（关键问题！）
   时刻T6: Widget A 继续移动到旧目标P2（错误！）
   时刻T7: 移动完成，center 被设置为 P2（错误的位置！）
   时刻T8: state 变为 targetState（可能也是错误的）
   ```

2. **场景2：初始进入游戏**
   ```
   时刻T0: 进入游戏，Widget初始化，开始移动到位置P1
   时刻T1: Widget处于Moving状态（offset=0.3）
   时刻T2: 第一次tick，检测到状态变化或组切换
   时刻T3: 调用新的移动指令到位置P2
   时刻T4: 由于Widget还在Moving，新指令被忽略
   时刻T5: Widget移动到P1（错误位置）
   ```

### 旧代码的问题

```java
public void moveDown(Vector2i ender) {
    if (state.equals(State.Moving)) {
        return;  // ❌ 直接忽略新的移动指令！
    }
    
    this.ender = new Vector2i(ender);
    setOffset(0);
    state = State.Moving;
    targetState = State.Down;
}

public void moveFocus(Vector2i ender) {
    if (state.equals(State.Moving)) {
        return;  // ❌ 直接忽略新的移动指令！
    }
    
    this.ender = new Vector2i(ender);
    setOffset(0);
    state = State.Moving;
    targetState = State.Focus;
}
```

**为什么这样设计**？
- 原本的设计可能是为了防止重复的移动指令
- 或者是为了保证动画的连贯性

**为什么会出问题**？
- 没有考虑到目标可能会在移动过程中改变
- 当目标改变时，Widget应该响应新的目标，而不是继续移动到旧目标

## 修复方案

### 核心思路

**当Widget正在Moving状态时收到新的移动指令，应该：**
1. 检查新目标是否与当前目标相同
2. 如果相同，忽略（避免重复）
3. 如果不同，**从当前实际位置重新开始移动到新目标**

### 关键点：计算当前实际位置

当Widget在Moving状态时，它的实际位置是通过插值计算的：
- `center`: 起始位置
- `ender`: 目标位置
- `offset`: 移动进度 (0.0 ~ 1.0)
- 实际位置 = `center + (ender - center) * getRealOffset(offset)`

所以，当目标改变时，我们需要：
1. 计算当前的实际位置
2. 将实际位置设为新的 `center`
3. 设置新的 `ender`
4. 重置 `offset = 0`
5. 从新的 `center` 重新开始移动到新的 `ender`

### 新代码实现

```java
public void moveDown(Vector2i ender) {
    // 如果正在移动，检查目标是否改变
    if (state.equals(State.Moving)) {
        // 如果目标位置和目标状态都没变，不需要重新移动
        if (this.ender.equals(ender) && this.targetState == State.Down) {
            return;
        }
        
        // ✅ 目标改变了，需要重新开始移动
        // 1. 计算当前实际位置（基于当前offset）
        double realOffset = getRealOffset(offset);
        int currentX = getXPosition(realOffset);
        int currentY = getYPosition(realOffset);
        
        // 2. 将当前实际位置设为新的起点
        this.center.set(currentX, currentY);
        
        // 3. 设置新的目标位置和状态
        this.ender = new Vector2i(ender);
        this.targetState = State.Down;
        
        // 4. 重置offset，从当前位置重新开始移动
        setOffset(0);
        return;
    }

    // 不在Moving状态，正常开始新的移动
    this.ender = new Vector2i(ender);
    setOffset(0);
    state = State.Moving;
    targetState = State.Down;
}

public void moveFocus(Vector2i ender) {
    // 同样的逻辑，只是targetState不同
    if (state.equals(State.Moving)) {
        if (this.ender.equals(ender) && this.targetState == State.Focus) {
            return;
        }
        
        double realOffset = getRealOffset(offset);
        int currentX = getXPosition(realOffset);
        int currentY = getYPosition(realOffset);
        
        this.center.set(currentX, currentY);
        this.ender = new Vector2i(ender);
        this.targetState = State.Focus;
        
        setOffset(0);
        return;
    }

    this.ender = new Vector2i(ender);
    setOffset(0);
    state = State.Moving;
    targetState = State.Focus;
}
```

## 修复后的行为

### 场景1：施法期间切换物品（修复后）

```
时刻T0: 玩家手持Weapon，Widget A在位置P1
时刻T1: 玩家释放持续性法术，Widget A开始从P1移动到P2（Moving状态，offset=0.2）
时刻T2: 玩家切换到Staff
时刻T3: updateState() 检测到状态变为Staff，调用 handleStateChange()
时刻T4: handleStateChange() 调用 moveFocus(P3)
时刻T5: moveFocus() 检测到目标改变：
        - 计算当前实际位置：P_current = P1 + (P2 - P1) * 0.2
        - 设置 center = P_current
        - 设置 ender = P3
        - 重置 offset = 0
时刻T6: Widget A 从 P_current 开始移动到 P3 ✅
时刻T7: 移动完成，center 被设置为 P3 ✅
时刻T8: state 变为 Focus ✅
```

### 场景2：初始进入游戏（修复后）

```
时刻T0: 进入游戏，Widget初始化，开始移动到位置P1
时刻T1: Widget处于Moving状态（offset=0.3）
时刻T2: 第一次tick，检测到状态变化或组切换
时刻T3: 调用新的移动指令到位置P2
时刻T4: 检测到目标改变，计算当前位置并重新开始移动 ✅
时刻T5: Widget从当前位置平滑移动到P2 ✅
```

## 动画效果

### 旧逻辑的动画问题
```
Widget在移动 ----[忽略新指令]----> 移动到错误位置 -----> 下一帧突然跳到正确位置（闪烁）
```

### 新逻辑的动画效果
```
Widget在移动 ----[中断并重新计算]----> 从当前位置平滑移动到新位置 ✅
```

新逻辑保证了：
1. ✅ 不会出现位置跳跃
2. ✅ 动画始终连贯
3. ✅ 目标始终是最新的正确位置

## 其他相关修复

### 1. SpellSelectionState.isFocus()
```java
public boolean isFocus(){
    return (this == Weapon) || (this == Staff) || (this == Spellbook);
}
```
让Weapon也支持焦点状态，持有武器时widget会突出显示。

### 2. SpellGroupData 索引跟踪
```java
private int lastValidSelectionIndex = -1;
private boolean selectionInitialized = false;
```
避免在施法期间使用错误的选择索引进行组切换。

### 3. ClientScrollData.tickHandle() 执行顺序
```java
public static void tickHandle() {
    updateState();        // 先更新状态
    handleKeyPress();
    updateTick();
    updateWidgets();
    
    if (ClientMagicData.isCasting()) {
        return;           // 施法期间不同步
    }
    
    syncSelection();
    // ...延迟同步逻辑
}
```
确保状态总是最新的，施法期间不执行组切换。

## 测试验证

### 测试1：施法期间快速切换物品
**步骤**：
1. 手持Weapon
2. 释放持续性法术（比如持续施法类型）
3. 在施法动画期间快速切换到Staff，再切回Weapon

**预期结果**：
- ✅ Widget始终平滑移动
- ✅ 没有位置跳跃或闪烁
- ✅ 最终位置正确

### 测试2：初始进入游戏
**步骤**：
1. 进入游戏世界
2. 观察Widget初始化

**预期结果**：
- ✅ Widget直接出现在正确位置或平滑移动到正确位置
- ✅ 没有先移动到错误位置再跳回来的情况

### 测试3：移动过程中切换组
**步骤**：
1. 按下组切换快捷键
2. Widget开始移动
3. 在移动过程中再次按下快捷键

**预期结果**：
- ✅ Widget从当前位置平滑移动到新组位置
- ✅ 没有完成第一次移动再开始第二次移动的情况

## 技术细节

### getRealOffset() 的作用
```java
public double getRealOffset(double interpolatedOffset){
    return interpolatedOffset * interpolatedOffset * (3.0 - 2.0 * interpolatedOffset);
}
```
这是一个**平滑步函数（Smoothstep）**，将线性进度转换为平滑的缓动效果：
- 0.0 -> 0.0 (慢速启动)
- 0.5 -> 0.5 (中速)
- 1.0 -> 1.0 (慢速结束)

### getXPosition() 和 getYPosition()
```java
private int getXPosition(double realOffset){
    return (int) (center.x + (ender.x - center.x) * realOffset);
}
private int getYPosition(double realOffset){
    return (int) (center.y + (ender.y - center.y) * realOffset);
}
```
线性插值计算实际位置：
- `realOffset = 0.0`: 返回 `center`（起点）
- `realOffset = 1.0`: 返回 `ender`（终点）
- `realOffset = 0.5`: 返回中间位置

## 总结

这个问题的关键在于**移动状态的中断处理**：

1. **旧逻辑**：忽略新指令，继续移动到旧目标（❌）
2. **新逻辑**：从当前位置重新开始移动到新目标（✅）

修复后的代码确保：
- ✅ Widget的实际位置始终正确
- ✅ 动画始终连贯流畅
- ✅ 目标改变时能正确响应
- ✅ 避免位置跳跃和闪烁

这个修复结合之前的施法检查和索引跟踪机制，彻底解决了"施法期间切换物品导致widget位置错误"的问题。

