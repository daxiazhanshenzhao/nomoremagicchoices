# 修复持续施法时切换武器导致的Widget错位问题

## 问题描述

**现象**：
从释放持续技能（CONTINUOUS类型）切换到空手或其他物品时，第2排的widget移动到了第1排，第1排的移动到了第2排，但 `currentGroup` 没有改变，只有渲染和组件发生了错位。

**触发条件**：
1. 手持Weapon
2. 释放持续性法术（CastType.CONTINUOUS）
3. 在施法期间切换到空手或其他物品（如Staff）

**只影响CONTINUOUS类型的原因**：
- CONTINUOUS类型的法术施法时间很长
- 其他类型（如INSTANT）施法时间很短，玩家很难在施法期间切换物品
- CONTINUOUS给了足够的时间窗口让玩家切换，暴露了问题

## 根本原因分析

### 问题链路

```
1. 玩家手持Weapon，释放持续性法术
   ├─ Widget Group0 在Focus位置
   ├─ Widget Group1 在Down位置
   └─ currentGroup = 0

2. 玩家在施法期间切换到Staff
   ├─ updateState() 检测到状态变化
   ├─ state 从 Weapon 变为 Staff
   └─ 调用 handleStateChange(Staff)

3. handleStateChange() 执行
   ├─ 假设 spellWightList[0] 是当前组 ❌ 错误假设！
   ├─ 但实际上列表可能被 drawWight() 重排过
   ├─ 调用 spellWightList[0].moveFocus() 
   └─ 但 spellWightList[0] 可能是 Group1！❌

4. 结果：错误的widget移动了
   ├─ Group1 移到了Focus位置 ❌
   ├─ Group0 移到了Down位置 ❌
   └─ 但 currentGroup 还是 0，数据不一致！
```

### 核心问题

1. **列表顺序假设错误**：`handleStateChange()` 假设列表第一个元素是当前组
2. **施法期间的状态切换时机**：在施法期间触发状态切换会导致错误的移动
3. **drawWight重排列表**：`drawWight()` 会重新排列 `spellWightList`，但其他代码没有考虑这一点

## 解决方案

### 方案1：延迟状态切换（主要方案）

**实现位置**：`ClientScrollData.java`

**核心思路**：如果正在持续施法，延迟状态切换直到施法结束。

#### 添加变量跟踪
```java
private static SpellSelectionState pendingStateChange = null;
private static boolean wasCastingLastTick = false;
```

#### 修改 `handleStateChange()`
```java
private static void handleStateChange(SpellSelectionState newState) {
    // 如果正在施法，记录pending状态，延迟执行
    if (ClientMagicData.isCasting()) {
        pendingStateChange = newState;
        return;
    }
    
    handleStateChangeInternal(newState);
}
```

#### 在 `tickHandle()` 中处理延迟切换
```java
public static void tickHandle() {
    // 检测施法结束
    boolean isCastingNow = ClientMagicData.isCasting();
    boolean castingJustEnded = wasCastingLastTick && !isCastingNow;
    wasCastingLastTick = isCastingNow;

    // 如果施法刚结束，执行pending的状态切换
    if (castingJustEnded && pendingStateChange != null) {
        SpellSelectionState pendingState = pendingStateChange;
        pendingStateChange = null;
        if (pendingState != state && spellWightList != null) {
            handleStateChangeInternal(pendingState);
        }
    }
    
    // ...正常流程
}
```

### 方案2：修复列表索引假设

**实现位置**：`ClientScrollData.handleStateChangeInternal()`

**核心思路**：根据 `groupIndex` 查找当前组的widget，而不是假设第一个元素。

```java
// 获取当前组的索引
int currentGroupIndex = SpellGroupData.instance.getCurrentGroupIndex();

// 找到当前组对应的widget在列表中的位置
int currentWidgetListIndex = -1;
for (int i = 0; i < spellWightList.size(); i++) {
    if (spellWightList.get(i).getGroupIndex() == currentGroupIndex) {
        currentWidgetListIndex = i;
        break;
    }
}

// 只对正确的widget调用moveFocus
if (i == currentWidgetListIndex) {
    spellWightList.get(i).moveFocus(positions.getFirst());
}
```

### 方案3：Widget内部拦截（辅助方案）

**实现位置**：`ScrollSpellWight.java`

**核心思路**：在widget的移动方法中添加保护，拒绝明显错误的移动。

#### `moveDown()` 添加保护
```java
public void moveDown(Vector2i ender) {
    // 如果正在施法且当前在Focus状态，拒绝移动到Down
    if (ClientMagicData.isCasting() && this.state == State.Focus) {
        Nomoremagicchoices.LOGGER.debug("Widget Group{} rejected moveDown during casting", groupIndex);
        return;
    }
    
    // ...正常移动逻辑
}
```

#### `moveFocus()` 添加保护
```java
public void moveFocus(Vector2i ender) {
    // 如果正在施法且当前在Down状态，拒绝移动到Focus
    if (ClientMagicData.isCasting() && this.state == State.Down) {
        Nomoremagicchoices.LOGGER.debug("Widget Group{} rejected moveFocus during casting", groupIndex);
        return;
    }
    
    // ...正常移动逻辑
}
```

## 修复后的执行流程

### 正常情况（修复后）

```
1. 玩家手持Weapon，释放持续性法术
   ├─ Widget Group0 在Focus位置
   ├─ isCasting = true
   └─ currentGroup = 0

2. 玩家在施法期间切换到Staff
   ├─ updateState() 检测到状态变化
   ├─ state 从 Weapon 变为 Staff
   └─ 调用 handleStateChange(Staff)

3. handleStateChange() 检测到施法
   ├─ isCasting = true ✅
   ├─ 设置 pendingStateChange = Staff
   └─ 返回，不执行移动 ✅

4. 继续施法...
   ├─ Widget保持原位 ✅
   └─ 等待施法结束

5. 施法结束
   ├─ castingJustEnded = true
   ├─ 执行 handleStateChangeInternal(Staff)
   ├─ 根据 groupIndex 找到正确的Group0
   └─ 移动正确的widget ✅

6. 结果：正确
   ├─ Group0 在Focus位置 ✅
   ├─ Group1 在Down位置 ✅
   └─ currentGroup = 0，数据一致 ✅
```

## 三层保护机制

### 第1层：延迟状态切换
- **位置**：`ClientScrollData.handleStateChange()`
- **作用**：阻止施法期间的状态切换
- **优点**：从源头解决问题

### 第2层：正确查找widget
- **位置**：`ClientScrollData.handleStateChangeInternal()`
- **作用**：根据groupIndex查找，不依赖列表顺序
- **优点**：即使第1层失效也能正确执行

### 第3层：Widget内部拦截
- **位置**：`ScrollSpellWight.moveDown/moveFocus()`
- **作用**：拒绝明显错误的移动请求
- **优点**：最后的防线，防止任何错误移动

## 测试验证

### 测试场景1：持续施法切换武器
**步骤**：
1. 手持Weapon
2. 释放持续性法术（如Fire Breath）
3. 在施法期间切换到Staff
4. 观察widget位置

**预期结果**：
- ✅ Widget在施法期间保持不动
- ✅ 施法结束后正确移动
- ✅ 当前组的widget在Focus位置
- ✅ 其他widget在Down位置

### 测试场景2：持续施法切换到空手
**步骤**：
1. 手持Staff
2. 释放持续性法术
3. 在施法期间切换到空手
4. 观察widget位置

**预期结果**：
- ✅ Widget在施法期间保持不动
- ✅ 施法结束后所有widget移到Down位置
- ✅ 没有错位

### 测试场景3：快速施法（非持续）
**步骤**：
1. 手持Weapon
2. 释放瞬发法术（INSTANT类型）
3. 快速切换武器

**预期结果**：
- ✅ 因为施法时间短，大部分情况不会触发保护
- ✅ 即使触发，也能正确处理

## 关键改进点

1. **状态切换延迟机制**
   - 施法期间不立即切换状态
   - 等待施法结束后再执行
   - 避免中间状态的不一致

2. **正确的widget查找**
   - 不依赖列表顺序
   - 根据groupIndex精确查找
   - 消除假设带来的风险

3. **Widget自我保护**
   - 在最底层添加安全检查
   - 拒绝不合理的移动请求
   - 提供最后的防线

4. **完善的日志**
   - 记录拦截的移动请求
   - 便于调试和问题追踪
   - 可以快速定位问题原因

## 性能影响

- **延迟切换**：几乎无性能影响，只是推迟了操作
- **列表遍历**：O(n)查找，n通常很小（<10）
- **额外检查**：每次移动多1-2个条件判断，可忽略

## 总结

通过三层保护机制，彻底解决了持续施法时切换武器导致的widget错位问题：

1. **延迟状态切换**：在源头阻止问题
2. **正确查找widget**：即使执行也能找对目标
3. **Widget内部拦截**：最后的安全防线

这三层机制互为补充，确保在任何情况下都能正确处理widget的位置和状态。

