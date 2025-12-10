# 修复空手切换到武器的重复排序问题

## 修改时间
2025-12-10

## 问题描述
从空手切换到武器/法杖时，Widget会从底部移动到最上方，这是不符合预期的行为。

### 问题现象
1. 空手状态：currentGroup 已经在列表末尾（显示在第一排）
2. 切换到武器：`handleStateChange` 调用 `drawWight` 重新排序
3. `drawWight` 再次将 currentGroup 移到列表末尾
4. 结果：currentGroup 从底部位置移动到顶部，产生不必要的移动动画

## 问题根源

### 初始化逻辑（update方法）
```java
// 将currentGroup移到列表末尾（显示在第一排）
for (int i = 0; i < groupCount; i++) {
    if (i != currentGroupIndex) {
        spellWightList.set(insertIndex, tempList.get(i));
        insertIndex++;
    }
}
// 最后添加当前组（在列表末尾）
spellWightList.set(groupCount - 1, tempList.get(currentGroupIndex));
```

**结果**：currentGroup 始终在列表末尾

### 旧的状态切换逻辑（有问题）
```java
// 切换到持有物品：将当前组移到Focus位置
int currentGroupIndex = SpellGroupData.instance.getCurrentGroupIndex();
List<Vector2i> positions = calculatePositions(newState);

// 找到当前组在列表中的位置
for (int i = 0; i < spellWightList.size(); i++) {
    if (spellWightList.get(i).getGroupIndex() == currentGroupIndex) {
        // 执行抽书操作，将当前组移到顶部
        ScrollGroupHelper.drawWight(spellWightList, i, positions, false);
        break;
    }
}
```

**问题**：
1. currentGroup 已经在列表末尾（i = size - 1）
2. `drawWight` 再次将它从末尾"抽"到末尾
3. 由于位置计算的问题，导致从底部移动到顶部

## 解决方案

### 修复后的逻辑
```java
// 切换到持有物品：currentGroup应该已经在列表末尾，直接让它移到Focus位置
// 不需要重新排序，因为初始化时已经将currentGroup放在末尾了
List<Vector2i> positions = calculatePositions(newState);

// 最后一个Widget（currentGroup）移到Focus位置
if (!spellWightList.isEmpty()) {
    ScrollSpellWight lastWight = spellWightList.getLast();
    Vector2i focusPos = positions.getLast();
    lastWight.moveFocus(focusPos);
    
    // 其他Widget保持在Down位置（它们的位置不变）
    for (int i = 0; i < spellWightList.size() - 1; i++) {
        ScrollSpellWight wight = spellWightList.get(i);
        Vector2i downPos = positions.get(i);
        wight.moveDown(downPos);
    }
}
```

### 修复要点
1. **不再调用 `drawWight`**：避免重复排序
2. **直接操作列表末尾**：currentGroup 已经在末尾，直接让它移到Focus位置
3. **其他Widget保持Down状态**：触发 `moveDown` 确保位置正确

## 工作流程对比

### 修复前（错误）
```
空手状态
├─ Widget[0] (groupIndex=1)  baseY + 0
├─ Widget[1] (groupIndex=2)  baseY + 10
└─ Widget[2] (groupIndex=0)  baseY + 20  ← currentGroup 在末尾

切换到武器
├─ 查找 currentGroup (在 index=2)
├─ 调用 drawWight(listIndex=2)
│   ├─ 将 Widget[2] 从末尾"抽"到末尾（实际上没变）
│   └─ 但是位置计算出错，导致从底部移到顶部
└─ Widget[2] 从 baseY+20 移动到 baseY-50 ❌ 错误！
```

### 修复后（正确）
```
空手状态
├─ Widget[0] (groupIndex=1)  baseY + 0
├─ Widget[1] (groupIndex=2)  baseY + 10
└─ Widget[2] (groupIndex=0)  baseY + 20  ← currentGroup 在末尾

切换到武器
├─ 不重新排序，列表顺序不变
├─ Widget[2] (末尾) 调用 moveFocus(baseY-50)
│   └─ 从 baseY+20 移动到 baseY-50 ✓ 正确！
├─ Widget[0] 调用 moveDown(baseY+0)   保持位置
└─ Widget[1] 调用 moveDown(baseY+10)  保持位置
```

## 状态转换表

| 转换方向 | 旧逻辑 | 新逻辑 |
|---------|--------|--------|
| 空手 → 武器 | ❌ 调用drawWight重新排序 | ✅ 直接移动末尾Widget到Focus |
| 武器 → 空手 | ✅ 所有Widget moveDown | ✅ 所有Widget moveDown |
| 武器 → 武器 | N/A | N/A |
| 空手 → 空手 | N/A | N/A |

## 测试验证

### 场景1：初始化后切换到武器
```
初始化（空手）
└─ currentGroup 在列表末尾，显示在第一排

切换到武器
├─ 列表顺序不变
└─ currentGroup (末尾Widget) 上移到Focus位置 ✓
```

### 场景2：武器切换回空手
```
武器状态
└─ currentGroup 在Focus位置（列表末尾）

切换回空手
├─ 列表顺序不变
└─ 所有Widget（包括currentGroup）下移到Down位置 ✓
```

### 场景3：空手状态下切换组
```
空手状态
└─ 按R键切换到下一组

执行流程
├─ setCurrentGroupIndex() 触发
├─ switchToGroup() 调用 drawWight
│   └─ 重新排序，新的currentGroup移到末尾
└─ 新currentGroup 显示在第一排 ✓
```

## 关键修改点

### 文件：ClientScrollData.java
**方法**：`handleStateChange()`

**修改前**：
```java
} else {
    // 切换到持有物品：将当前组移到Focus位置
    int currentGroupIndex = SpellGroupData.instance.getCurrentGroupIndex();
    List<Vector2i> positions = calculatePositions(newState);

    // 找到当前组在列表中的位置
    for (int i = 0; i < spellWightList.size(); i++) {
        if (spellWightList.get(i).getGroupIndex() == currentGroupIndex) {
            // 执行抽书操作，将当前组移到顶部
            ScrollGroupHelper.drawWight(spellWightList, i, positions, false);
            break;
        }
    }
}
```

**修改后**：
```java
} else {
    // 切换到持有物品：currentGroup应该已经在列表末尾，直接让它移到Focus位置
    // 不需要重新排序，因为初始化时已经将currentGroup放在末尾了
    List<Vector2i> positions = calculatePositions(newState);
    
    // 最后一个Widget（currentGroup）移到Focus位置
    if (!spellWightList.isEmpty()) {
        ScrollSpellWight lastWight = spellWightList.getLast();
        Vector2i focusPos = positions.getLast();
        lastWight.moveFocus(focusPos);
        
        // 其他Widget保持在Down位置
        for (int i = 0; i < spellWightList.size() - 1; i++) {
            ScrollSpellWight wight = spellWightList.get(i);
            Vector2i downPos = positions.get(i);
            wight.moveDown(downPos);
        }
    }
}
```

## 总结

### 问题原因
重复排序：`update()` 已经将 currentGroup 放到末尾，`handleStateChange()` 又调用 `drawWight()` 重新排序，导致逻辑混乱。

### 解决方案
- 状态切换时**不再重新排序**
- 直接操作已经在正确位置的 Widget
- 只触发位置移动动画，不改变列表顺序

### 适用场景
- ✅ 空手 → 武器/法杖：直接移动末尾Widget到Focus
- ✅ 武器/法杖 → 空手：所有Widget下移
- ✅ 空手状态切换组：使用 `drawWight()` 重新排序

### 优势
1. 逻辑更清晰：职责分离，状态切换不负责排序
2. 性能更好：减少不必要的列表操作
3. 动画正确：Widget按预期移动，没有跳跃

