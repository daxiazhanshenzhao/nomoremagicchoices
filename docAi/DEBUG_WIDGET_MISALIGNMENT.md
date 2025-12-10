# Widget错位问题调试指南

## 问题描述

持续释放法术时切换武器，widget会发生错位。

## 调试步骤

### 1. 添加调试日志

在以下关键位置添加日志输出：

#### ClientScrollData.java

```java
private static void handleStateChange(SpellSelectionState newState) {
    if (spellWightList == null || spellWightList.isEmpty()) return;

    Nomoremagicchoices.LOGGER.info("=== handleStateChange ===");
    Nomoremagicchoices.LOGGER.info("New State: {}", newState);
    Nomoremagicchoices.LOGGER.info("Widget List Order: {}", 
        spellWightList.stream()
            .map(w -> "Group" + w.getGroupIndex())
            .collect(Collectors.joining(", ")));

    List<Vector2i> positions = calculatePositions(newState);
    
    Nomoremagicchoices.LOGGER.info("Calculated Positions:");
    for (int i = 0; i < positions.size(); i++) {
        Nomoremagicchoices.LOGGER.info("  [{}] -> ({}, {})", i, positions.get(i).x, positions.get(i).y);
    }

    if (newState.isFocus()) {
        spellWightList.getFirst().moveFocus(positions.getFirst());
        for (int i = 1; i < spellWightList.size(); i++) {
            spellWightList.get(i).moveDown(positions.get(i));
        }
    } else {
        for (int i = 0; i < spellWightList.size(); i++) {
            spellWightList.get(i).moveDown(positions.get(i));
        }
    }
}

private static void switchToGroup(int targetGroupIndex) {
    if (spellWightList == null || spellWightList.isEmpty()) return;

    Nomoremagicchoices.LOGGER.info("=== switchToGroup ===");
    Nomoremagicchoices.LOGGER.info("Target Group Index: {}", targetGroupIndex);
    Nomoremagicchoices.LOGGER.info("Current State: {}", state);
    Nomoremagicchoices.LOGGER.info("Widget List Order BEFORE: {}", 
        spellWightList.stream()
            .map(w -> "Group" + w.getGroupIndex())
            .collect(Collectors.joining(", ")));

    int targetListIndex = -1;
    for (int i = 0; i < spellWightList.size(); i++) {
        if (spellWightList.get(i).getGroupIndex() == targetGroupIndex) {
            targetListIndex = i;
            break;
        }
    }

    Nomoremagicchoices.LOGGER.info("Target List Index: {}", targetListIndex);

    if (targetListIndex != -1) {
        List<Vector2i> positions = calculatePositions(state);
        
        Nomoremagicchoices.LOGGER.info("Calculated Positions:");
        for (int i = 0; i < positions.size(); i++) {
            Nomoremagicchoices.LOGGER.info("  [{}] -> ({}, {})", i, positions.get(i).x, positions.get(i).y);
        }
        
        ScrollGroupHelper.drawWight(spellWightList, targetListIndex, positions, state.isFocus());
        
        Nomoremagicchoices.LOGGER.info("Widget List Order AFTER: {}", 
            spellWightList.stream()
                .map(w -> "Group" + w.getGroupIndex())
                .collect(Collectors.joining(", ")));
    }
}
```

#### ScrollSpellWight.java

```java
public void moveDown(Vector2i ender) {
    Nomoremagicchoices.LOGGER.info("Widget Group{} moveDown to ({}, {}), current state: {}, current center: ({}, {})", 
        groupIndex, ender.x, ender.y, state, center.x, center.y);
    
    if (state.equals(State.Moving)) {
        if (this.ender.equals(ender) && this.targetState == State.Down) {
            Nomoremagicchoices.LOGGER.info("  -> Already moving to same target, ignored");
            return;
        }
        
        double realOffset = getRealOffset(offset);
        int currentX = getXPosition(realOffset);
        int currentY = getYPosition(realOffset);
        
        Nomoremagicchoices.LOGGER.info("  -> Interrupted! Current actual position: ({}, {}), new target: ({}, {})", 
            currentX, currentY, ender.x, ender.y);
        
        this.center.set(currentX, currentY);
        this.ender = new Vector2i(ender);
        this.targetState = State.Down;
        setOffset(0);
        return;
    }

    this.ender = new Vector2i(ender);
    setOffset(0);
    state = State.Moving;
    targetState = State.Down;
}

public void moveFocus(Vector2i ender) {
    Nomoremagicchoices.LOGGER.info("Widget Group{} moveFocus to ({}, {}), current state: {}, current center: ({}, {})", 
        groupIndex, ender.x, ender.y, state, center.x, center.y);
    
    if (state.equals(State.Moving)) {
        if (this.ender.equals(ender) && this.targetState == State.Focus) {
            Nomoremagicchoices.LOGGER.info("  -> Already moving to same target, ignored");
            return;
        }
        
        double realOffset = getRealOffset(offset);
        int currentX = getXPosition(realOffset);
        int currentY = getYPosition(realOffset);
        
        Nomoremagicchoices.LOGGER.info("  -> Interrupted! Current actual position: ({}, {}), new target: ({}, {})", 
            currentX, currentY, ender.x, ender.y);
        
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

#### SpellGroupData.java

```java
public boolean syncGroupFromSelection() {
    var spellSelectionManager = ClientMagicData.getSpellSelectionManager();
    int selectedIndex = spellSelectionManager.getSelectionIndex();
    boolean isCasting = ClientMagicData.isCasting();
    
    Nomoremagicchoices.LOGGER.info("=== syncGroupFromSelection ===");
    Nomoremagicchoices.LOGGER.info("Selected Index: {}, isCasting: {}, lastValidIndex: {}, initialized: {}", 
        selectedIndex, isCasting, lastValidSelectionIndex, selectionInitialized);

    if (selectedIndex < 0 || selectedIndex >= allSpells.size()) {
        Nomoremagicchoices.LOGGER.info("  -> Invalid index, skipped");
        return false;
    }

    if (!selectionInitialized) {
        if (isCasting) {
            Nomoremagicchoices.LOGGER.info("  -> Initializing delayed due to casting");
            return false;
        }
        lastValidSelectionIndex = selectedIndex;
        selectionInitialized = true;
        Nomoremagicchoices.LOGGER.info("  -> Initialized with index {}", selectedIndex);
        return false;
    }

    if (lastValidSelectionIndex == selectedIndex) {
        Nomoremagicchoices.LOGGER.info("  -> Index not changed, skipped");
        return false;
    }

    if (isCasting) {
        Nomoremagicchoices.LOGGER.info("  -> Index changed during casting, ignored");
        return false;
    }

    int targetGroupIndex = selectedIndex / SPELLS_PER_GROUP;
    Nomoremagicchoices.LOGGER.info("  -> Index changed: {} -> {}, target group: {} (current: {})", 
        lastValidSelectionIndex, selectedIndex, targetGroupIndex, currentGroupIndex);

    if (targetGroupIndex == this.currentGroupIndex) {
        lastValidSelectionIndex = selectedIndex;
        Nomoremagicchoices.LOGGER.info("  -> Target group is current group, updated index but no switch");
        return false;
    }

    lastValidSelectionIndex = selectedIndex;
    Nomoremagicchoices.LOGGER.info("  -> Switching to group {}", targetGroupIndex);
    
    // ... 事件处理
    return true;
}
```

### 2. 重现问题并观察日志

1. 启动游戏
2. 手持Weapon
3. 释放持续性法术
4. 在施法期间切换到Staff
5. 观察日志输出

### 3. 关注以下关键点

#### 关键点1：handleStateChange的调用时机
```
预期：应该在切换武器时立即调用
检查：日志中是否有 "=== handleStateChange ===" 输出
检查：Widget List Order 是否正确
检查：Calculated Positions 是否合理
```

#### 关键点2：syncGroupFromSelection的行为
```
预期：施法期间索引变化应该被忽略
检查：isCasting 是否为 true
检查：是否输出 "Index changed during casting, ignored"
```

#### 关键点3：switchToGroup的调用时机
```
预期：只有在非施法状态且索引真正改变时才调用
检查：是否在施法期间被错误调用
检查：Widget List Order BEFORE 和 AFTER 是否正确
检查：Calculated Positions 是否与widget的实际目标匹配
```

#### 关键点4：Widget移动中断
```
预期：widget在移动过程中收到新指令，应该从当前位置重新开始
检查：是否输出 "Interrupted!" 信息
检查：Current actual position 是否合理
检查：new target 是否正确
```

## 可能的问题场景

### 场景A：handleStateChange和switchToGroup冲突

如果日志显示：
```
=== handleStateChange ===
New State: Staff
Widget List Order: Group0, Group1, Group2
[0] moveFocus to (x1, y1)
[1] moveDown to (x2, y2)
=== syncGroupFromSelection ===
Index changed during casting, ignored  <-- 好
...（几个tick后）...
=== syncGroupFromSelection ===
Index changed: 2 -> 6
=== switchToGroup ===
Target Group Index: 1
Widget List Order BEFORE: Group0, Group1, Group2
Widget List Order AFTER: Group1, Group0, Group2  <-- 重排了！
[0] moveFocus to (x1, y1)  <-- 现在Group1使用了原本Group0的位置！
```

**解决方案**：在 `syncGroupFromSelection` 中添加更严格的检查，确保索引变化是玩家主动选择导致的。

### 场景B：Widget移动中断处理错误

如果日志显示：
```
Widget Group0 moveFocus to (100, 50)
...（移动中）...
Widget Group0 moveDown to (100, 200)  <-- 目标改变
  -> Interrupted! Current actual position: (100, 100), new target: (100, 200)
...（移动中）...
Widget Group0 moveFocus to (100, 300)  <-- 又改变了
  -> Interrupted! Current actual position: (100, 150), new target: (100, 300)
```

**说明**：目标位置频繁改变，可能是因为状态更新和组切换同时触发。

## 建议的修复方案

根据日志分析结果，可能需要：

1. **方案1：延迟syncSelection**
   ```java
   public static void tickHandle() {
       updateState();
       handleKeyPress();
       updateTick();
       updateWidgets();
       
       // 延迟1-2个tick再同步，让状态变化的动画先完成
       if (ticksSinceStateChange > 2) {
           syncSelection();
       }
   }
   ```

2. **方案2：只在选择真正改变时同步**
   ```java
   // 在SpellGroupData中，只有当玩家通过SpellWheel明确选择时才同步
   // 不响应任何程序性的索引变化
   ```

3. **方案3：禁用施法结束后的自动同步**
   ```java
   // 完全移除syncGroupFromSelection，只通过手动切换组
   // 或者只在特定条件下（比如打开SpellWheel后）才同步
   ```

## 临时禁用syncSelection测试

为了确认问题是否在syncSelection，可以临时注释掉：

```java
public static void tickHandle() {
    updateState();
    handleKeyPress();
    updateTick();
    updateWidgets();
    
    // syncSelection();  // 临时禁用
}
```

如果禁用后问题消失，说明问题确实在 `syncGroupFromSelection`。

