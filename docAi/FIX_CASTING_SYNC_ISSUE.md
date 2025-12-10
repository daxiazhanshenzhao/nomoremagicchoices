# 修复施法期间切换物品导致的同步问题

## 问题描述

### 问题1：释放技能时切换物品，顶层widget回到错误的中间坐标
**现象**：
- 手持Weapon释放持续性法术时，切换到Staff或其他物品
- 顶层的widget会移动到一个不正确的中间位置
- 切换回Weapon后，又恢复正常

### 问题2：持有Weapon时不会有widget突出显示
**现象**：
- 持有Weapon类型的物品时，widget不会突出显示
- 而持有Staff或Spellbook时可以正常突出显示

### 问题3：新进入游戏时也有类似问题
**现象**：
- 每次新进入游戏时，widget位置可能不正确
- 需要切换几次物品才能恢复正常

## 根本原因分析

### 问题1和问题3的原因：
当玩家正在释放持续性法术时，`spellSelectionManager.getSelectionIndex()` 返回的是**正在释放的法术的索引**，而不是玩家通过UI选择的法术索引。

**问题链**：
1. 玩家手持Weapon释放持续性法术（比如索引5的法术）
2. 切换到Staff
3. `updateState()` 更新状态为Staff
4. `syncGroupFromSelection()` 被调用
5. `getSelectionIndex()` 返回5（正在释放的法术）
6. 计算出目标组是 5/4 = 1
7. 错误地切换到组1，widget移动到错误位置

**初始化问题**：
- 进入游戏时，`lastValidSelectionIndex` 初始值为 -1
- 如果玩家恰好在施法状态，第一次同步会使用错误的索引

### 问题2的原因：
`SpellSelectionState.isFocus()` 方法只对 `Staff` 和 `Spellbook` 返回 `true`，而 `Weapon` 返回 `false`。这导致持有武器时，系统认为不应该有焦点状态。

## 修复方案

### 修复问题2：让Weapon也支持焦点状态
**文件**：`SpellSelectionState.java`

**修改**：
```java
public boolean isFocus(){
    return (this == Weapon) || (this == Staff) || (this == Spellbook);
}
```

现在 `Weapon` 也会返回 `true`，使持有武器时widget能够正确突出显示。

### 修复问题1和问题3：避免施法期间的错误同步

#### 1. 添加选择索引跟踪机制
**文件**：`SpellGroupData.java`

**添加字段**：
```java
// 记录上一次非施法状态下的选择索引，用于避免施法期间的错误同步
private int lastValidSelectionIndex = -1;

// 标记是否已经初始化过选择索引
private boolean selectionInitialized = false;
```

#### 2. 改进 `syncGroupFromSelection()` 方法
**关键逻辑**：
1. **施法检查**：如果正在施法，直接返回false，不执行任何同步
2. **初始化检查**：第一次调用时只记录当前索引，不执行切换
3. **索引变化验证**：只有当索引真正改变时才执行切换
4. **更新记录**：每次非施法状态下的同步都更新 `lastValidSelectionIndex`

**代码**：
```java
public boolean syncGroupFromSelection() {
    var spellSelectionManager = ClientMagicData.getSpellSelectionManager();
    int selectedIndex = spellSelectionManager.getSelectionIndex();

    if (selectedIndex < 0 || selectedIndex >= allSpells.size()) {
        return false;
    }

    // 施法期间不同步，避免使用错误的索引
    if (ClientMagicData.isCasting()) {
        return false;
    }

    // 初始化：第一次调用时只记录，不切换
    if (!selectionInitialized) {
        lastValidSelectionIndex = selectedIndex;
        selectionInitialized = true;
        return false;
    }

    // 索引没变化，不需要切换
    if (lastValidSelectionIndex == selectedIndex) {
        return false;
    }

    // 更新记录
    lastValidSelectionIndex = selectedIndex;

    // 执行切换逻辑...
}
```

#### 3. 重置初始化标记
**文件**：`SpellGroupData.java` 的 `updateSpells()` 方法

当法术列表更新时（比如玩家学习了新法术），重置初始化标记：
```java
public void updateSpells() {
    // ...现有逻辑...
    
    // 重置选择初始化标记，允许下一次同步重新初始化
    selectionInitialized = false;
    lastValidSelectionIndex = -1;
}
```

#### 4. 移除 `updateState()` 中的施法检查
**文件**：`ClientScrollData.java`

**原因**：
- 之前的实现在施法期间完全阻止状态更新
- 这会导致玩家切换物品时状态无法正确反映
- 状态更新应该始终反映玩家的实际持有物品

**修改**：
- 移除 `updateState()` 中的 `isCasting()` 检查
- 允许状态正常更新
- 组切换的控制完全交给 `syncGroupFromSelection()`

#### 5. 调整 `tickHandle()` 执行顺序
**文件**：`ClientScrollData.java`

**新顺序**：
1. `updateState()` - 先更新状态
2. `handleKeyPress()` - 处理按键
3. `updateTick()` - 更新tick计数
4. `updateWidgets()` - 更新widget动画
5. **施法检查** - 如果正在施法，直接返回
6. `syncSelection()` - 同步选择（只在非施法时）
7. **延迟同步** - 处理施法结束后的延迟同步

## 测试场景

### 测试1：施法期间切换物品
1. 手持Weapon
2. 释放一个持续性法术（比如持续施法类型的法术）
3. 在施法期间切换到Staff
4. **预期结果**：widget应该保持在正确位置，不会跳到错误的中间位置

### 测试2：持有Weapon时widget显示
1. 手持Weapon
2. **预期结果**：第一个widget应该突出显示在顶部

### 测试3：初始进入游戏
1. 进入游戏
2. **预期结果**：widget应该在正确位置，不需要切换物品来修正

### 测试4：学习新法术
1. 学习新法术，触发 `updateSpells()`
2. 切换组
3. **预期结果**：组切换应该正常工作

## 技术细节

### 为什么施法期间 getSelectionIndex() 会变化？
Iron's Spellbooks 的 `SpellSelectionManager` 在玩家施法时，会将当前选择索引设置为正在释放的法术索引。这是为了：
- 显示正在释放的法术图标
- 冷却时间显示
- 其他UI反馈

但这对我们的组切换逻辑造成了干扰。

### 为什么要记录 lastValidSelectionIndex？
- 跟踪玩家的真实选择意图
- 过滤掉施法导致的临时索引变化
- 只在真正的用户选择变化时才触发组切换

### 为什么需要初始化标记？
- 避免在第一次调用时就执行切换
- 给系统一个稳定的初始状态
- 防止进入游戏时的错误同步

## 相关文件

- `SpellSelectionState.java` - 状态枚举，定义 `isFocus()` 方法
- `SpellGroupData.java` - 法术组数据管理，包含 `syncGroupFromSelection()` 核心逻辑
- `ClientScrollData.java` - 客户端滚动数据管理，包含 `tickHandle()` 和 `updateState()`
- `ScrollSpellWight.java` - Widget组件，处理动画和渲染

## 总结

这个修复通过三个层面解决了问题：

1. **状态层面**：让Weapon也支持焦点状态
2. **同步层面**：避免施法期间的错误同步
3. **初始化层面**：添加初始化保护机制

核心思想是：**只在玩家真正改变选择时才同步，而不是响应系统内部的临时状态变化**。

