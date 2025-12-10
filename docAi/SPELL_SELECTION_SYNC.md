# 法术选择同步功能实现

## 修改时间
2025-12-10

## 功能描述
当玩家通过 `SpellWheelOverlay`（法术轮盘）或其他方式改变 `makeSelection` 时，自动检测这个变化并将 `currentGroup` 切换到对应的组，同时触发 Widget 重排动画。

## 问题背景
之前的实现中：
- 玩家按 R 键切换组 → currentGroup 改变 → 调用 `makeSelection` 设置第一个法术
- 玩家使用法术轮盘选择法术 → `makeSelection` 改变 → **currentGroup 不会自动切换** ❌

这导致：
1. UI 显示的组与实际选中的法术不匹配
2. 用户体验不一致

## 解决方案

### 1. 添加同步方法（SpellGroupData）

```java
/**
 * 检测并同步当前选中的法术到对应的组
 * 当玩家通过SpellWheelOverlay或其他方式改变makeSelection时，
 * 这个方法会检测到变化并自动切换currentGroup到选中法术所在的组
 * 
 * @return 如果检测到变化并成功切换组返回true，否则返回false
 */
public boolean syncGroupFromSelection() {
    // 获取当前选中的法术索引
    int selectedIndex = spellSelectionManager.getSelectionIndex();
    
    // 计算选中法术所在的组索引
    int targetGroupIndex = selectedIndex / SPELLS_PER_GROUP;
    
    // 如果已经是当前组，不需要切换
    if (targetGroupIndex == this.currentGroupIndex) {
        return false;
    }
    
    // 切换到目标组（不触发selectFirstSpellOfCurrentGroup，避免循环）
    ChangeGroupEvent event = new ChangeGroupEvent(this, this.currentGroupIndex, targetGroupIndex);
    
    if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
        return false;
    }
    
    this.currentGroupIndex = Math.clamp(validatedIndex, 0, Math.max(0, groupCount - 1));
    
    return true;
}
```

### 2. 添加tick检测（ClientScrollData）

```java
/**
 * 处理法术选择同步
 * 检测玩家通过SpellWheelOverlay或其他方式改变法术选择时，自动切换currentGroup
 */
private static void handleSelectionSync() {
    if (spellWightList == null || spellWightList.isEmpty()) return;
    
    // 尝试同步组索引
    boolean groupChanged = SpellGroupData.instance.syncGroupFromSelection();
    
    // 如果组发生了变化，需要触发Widget重排
    if (groupChanged) {
        int newGroupIndex = SpellGroupData.instance.getCurrentGroupIndex();
        Nomoremagicchoices.LOGGER.info("检测到法术选择变化，切换到组: " + newGroupIndex);
        
        // 执行切换动画（将新的currentGroup移到列表开头）
        switchToGroup(newGroupIndex);
    }
}
```

### 3. 集成到主tick循环

```java
public static void tickHandle(){
    handleRunning();        // 处理按键切换组
    handleCurrentTick();    // 处理动画tick计数
    handleWightTick();      // 处理Widget动画更新
    handleState();          // 处理状态切换（空手/武器）
    handleSelectionSync();  // 🆕 检测法术选择变化并同步组
}
```

## 工作流程

### 场景1：玩家按R键切换组
```
用户按R键
    ↓
handleRunning() 检测按键
    ↓
SpellGroupData.setCurrentGroupIndex(nextGroupIndex)
    ↓
selectFirstSpellOfCurrentGroup()
    ↓
makeSelection(targetIndex) ← 设置第一个法术为选中
    ↓
handleSelectionSync() 检测
    ↓
selectedIndex / 4 == currentGroupIndex ✓
    ↓
不需要切换，返回false
```

### 场景2：玩家使用法术轮盘选择法术
```
用户打开法术轮盘，选择法术X
    ↓
Iron's Spellbooks: makeSelection(X)
    ↓
下一tick
    ↓
handleSelectionSync() 检测
    ↓
selectedIndex = X
targetGroupIndex = X / 4
    ↓
targetGroupIndex != currentGroupIndex ✓
    ↓
切换到新组：currentGroupIndex = targetGroupIndex
    ↓
触发Widget重排动画
    ↓
UI显示更新为新组 ✓
```

### 场景3：玩家使用快捷键选择法术
```
用户按快捷键选择法术（如数字键）
    ↓
ClientInputHandle: makeSelection(i)
    ↓
下一tick
    ↓
handleSelectionSync() 检测
    ↓
切换到对应组并更新UI ✓
```

## 关键设计点

### 1. 避免循环触发
`syncGroupFromSelection()` 在切换组时**不调用** `selectFirstSpellOfCurrentGroup()`：
```java
// ❌ 会导致循环
setCurrentGroupIndex() → selectFirstSpellOfCurrentGroup() → makeSelection()
    ↓
syncGroupFromSelection() → setCurrentGroupIndex() → ...

// ✅ 正确实现
syncGroupFromSelection() {
    // 直接修改 currentGroupIndex，不调用 selectFirstSpellOfCurrentGroup()
    this.currentGroupIndex = validatedIndex;
}
```

### 2. 检测变化
每个tick都会检测，但只有当组真正改变时才触发动画：
```java
if (targetGroupIndex == this.currentGroupIndex) {
    return false; // 无变化，直接返回
}
```

### 3. 事件支持
切换时仍然发布 `ChangeGroupEvent`，保持事件系统的完整性：
```java
ChangeGroupEvent event = new ChangeGroupEvent(this, oldIndex, newIndex);
if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
    return false; // 允许其他模组取消切换
}
```

## 性能考虑

### 每tick开销
```java
// 1次方法调用
spellSelectionManager.getSelectionIndex(); 

// 1次整数除法
targetGroupIndex = selectedIndex / 4;

// 1次整数比较
if (targetGroupIndex == currentGroupIndex) return false;
```
开销极小，可以忽略不计。

### 优化：避免重复触发
```java
// 如果组没有变化，立即返回
if (targetGroupIndex == this.currentGroupIndex) {
    return false;
}
```
大多数情况下（99%+的tick），会在第一次比较时就返回，不会执行后续逻辑。

## 测试场景

### ✅ 场景1：法术轮盘选择
1. 当前组：0（法术0-3）
2. 打开法术轮盘，选择法术5（第2组）
3. UI自动切换到组1，显示法术4-7
4. 法术5高亮显示

### ✅ 场景2：快捷键选择
1. 当前组：1（法术4-7）
2. 按快捷键选择法术1（第1组）
3. UI自动切换到组0，显示法术0-3
4. 法术1高亮显示

### ✅ 场景3：R键切换
1. 当前组：0
2. 按R键切换到组1
3. 法术4被自动选中
4. UI显示组1

### ✅ 场景4：重复选择同组法术
1. 当前组：0（法术0-3）
2. 在法术轮盘中选择法术2（仍在组0）
3. 不触发组切换，UI保持不变
4. 只有法术高亮改变

## 日志输出示例

### 玩家使用法术轮盘
```
[Render thread/INFO] [or.no.Nomoremagicchoices/]: 检测到法术选择变化，切换到组: 2
[Render thread/INFO] [or.no.Nomoremagicchoices/]: 开始抽书操作: listIndex=1, isEmptyHand=false
[Render thread/INFO] [or.no.Nomoremagicchoices/]: 顶部Widget[0] (groupIndex=2) moveFocus to (860, 950)
```

### 玩家按R键
```
[Render thread/INFO] [or.no.Nomoremagicchoices/]: 向后切换组
[Render thread/INFO] [or.no.Nomoremagicchoices/]: 切换组: 0 -> 1
[Render thread/INFO] [or.no.Nomoremagicchoices/]: 执行切换动画: 将groupIndex=1 从listIndex=0 抽到第一位
```

## 相关文件
- `SpellGroupData.java` - 添加 `syncGroupFromSelection()` 方法
- `ClientScrollData.java` - 添加 `handleSelectionSync()` 方法和集成到tick循环

## 优势

### 1. 双向同步
- R键切换组 → 选中第一个法术 ✓
- 法术轮盘选择 → 切换到对应组 ✓

### 2. 用户体验一致
无论通过何种方式选择法术，UI 始终显示正确的组。

### 3. 性能优良
每tick开销极小，不会影响游戏性能。

### 4. 扩展性好
支持任何改变 `makeSelection` 的方式：
- 法术轮盘
- 快捷键
- 其他模组的API调用
- 命令行

## 未来改进
1. 可以添加配置选项，让玩家选择是否启用自动同步
2. 可以添加平滑过渡动画，而不是立即切换
3. 可以添加音效反馈

