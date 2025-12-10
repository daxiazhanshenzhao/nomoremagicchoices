# 自动选中组首法术功能实现

## 修改时间
2025-12-10

## 功能描述
实现了当切换法术组时，自动将新组的第一个法术设置为Iron's Spellbooks的选中法术。

## 实现逻辑

### 1. 核心方法：selectFirstSpellOfCurrentGroup()
位置：`SpellGroupData.java`

```java
public boolean selectFirstSpellOfCurrentGroup() {
    // 1. 获取当前组的法术列表
    List<SpellData> currentGroupSpells = getCurrentGroupSpells();
    
    // 2. 检查是否有法术
    if (currentGroupSpells.isEmpty()) {
        return false;
    }
    
    // 3. 获取第一个法术
    SpellData firstSpell = currentGroupSpells.getFirst();
    
    // 4. 检查法术是否为空
    if (firstSpell == null || firstSpell.equals(SpellData.EMPTY)) {
        return false;
    }
    
    // 5. 在allSpells中查找该法术的索引
    int targetIndex = -1;
    for (int i = 0; i < allSpells.size(); i++) {
        if (allSpells.get(i) != null && allSpells.get(i).equals(firstSpell)) {
            targetIndex = i;
            break;
        }
    }
    
    // 6. 调用SpellSelectionManager.makeSelection()设置选中状态
    if (targetIndex >= 0) {
        ClientMagicData.getSpellSelectionManager().makeSelection(targetIndex);
        return true;
    }
    
    return false;
}
```

### 2. 自动触发机制：setCurrentGroupIndex()
在 `setCurrentGroupIndex()` 方法中集成了自动选中逻辑：

```java
public void setCurrentGroupIndex(int newGroupIndex) {
    // ...事件处理...
    
    int oldIndex = this.currentGroupIndex;
    int validatedIndex = event.getNewGroup();
    this.currentGroupIndex = Math.clamp(validatedIndex, 0, Math.max(0, groupCount - 1));
    
    // 如果索引确实改变了，自动选中新组的第一个法术
    if (oldIndex != this.currentGroupIndex) {
        selectFirstSpellOfCurrentGroup();
    }
}
```

### 3. 触发场景
该功能会在以下场景自动触发：

#### 场景1：按键切换组
在 `ClientScrollData.handleRunning()` 中：
- 按下右箭头键（下一组）
- 按下左箭头键（上一组）
- 按下R键（下一组）

```java
// 用户按键后，设置新的组索引
SpellGroupData.instance.setCurrentGroupIndex(nextGroupIndex);
// setCurrentGroupIndex内部会自动调用selectFirstSpellOfCurrentGroup()
```

#### 场景2：事件触发切换组
任何调用 `setCurrentGroupIndex()` 的地方都会自动触发，包括：
- `ChangeGroupEvent` 触发的组切换
- 程序内部的组索引变更

## 工作流程

```
用户按下切换组按键
    ↓
handleRunning() 检测按键
    ↓
计算新的组索引（nextGroupIndex）
    ↓
调用 SpellGroupData.setCurrentGroupIndex(nextGroupIndex)
    ↓
发布 ChangeGroupEvent（可被取消）
    ↓
验证并设置新索引
    ↓
检测索引是否改变
    ↓
调用 selectFirstSpellOfCurrentGroup()
    ↓
查找新组第一个法术在allSpells中的索引
    ↓
调用 SpellSelectionManager.makeSelection(targetIndex)
    ↓
Iron's Spellbooks将该法术设为选中状态
    ↓
执行切换动画（switchToGroup）
```

## 技术细节

### 1. 索引映射
- **groupIndex**: 法术组的索引（0到groupCount-1）
- **allSpells index**: 法术在总列表中的索引（用于makeSelection）
- **listIndex**: Widget在渲染列表中的索引（用于动画）

### 2. 查找逻辑
使用 `equals()` 方法比较 `SpellData` 对象：
```java
if (allSpells.get(i) != null && allSpells.get(i).equals(firstSpell)) {
    targetIndex = i;
    break;
}
```

### 3. 安全检查
在选中法术前进行多重检查：
1. 当前组是否为空
2. 第一个法术是否为null
3. 第一个法术是否为EMPTY
4. 是否能在allSpells中找到该法术

### 4. 返回值
- `true`: 成功设置选中状态
- `false`: 无法设置（组为空、法术无效、找不到索引等）

## 优势

### 1. 自动化
无需在每个调用 `setCurrentGroupIndex()` 的地方手动调用选中逻辑，减少重复代码。

### 2. 一致性
确保所有组切换场景都会自动选中第一个法术，避免遗漏。

### 3. 解耦
选中逻辑封装在 `SpellGroupData` 中，与UI层（`ClientScrollData`）解耦。

### 4. 可扩展性
如果将来需要选中不同位置的法术（如第二个、最后一个），只需修改一个方法。

## 使用示例

### 示例1：直接切换组
```java
// 切换到第2组
SpellGroupData.instance.setCurrentGroupIndex(1);
// 自动将第2组的第一个法术设为选中状态
```

### 示例2：相对切换
```java
// 切换到下一组
int current = SpellGroupData.instance.getCurrentGroupIndex();
int next = (current + 1) % SpellGroupData.getGroupCount();
SpellGroupData.instance.setCurrentGroupIndex(next);
// 自动选中新组的第一个法术
```

## 相关文件
- `SpellGroupData.java` - 核心实现
- `ClientScrollData.java` - 按键触发和动画处理
- `ModKeyMapping.java` - 按键定义

## 调试信息
可以通过以下日志查看功能运行状态：
```
[Render thread/INFO] [or.no.Nomoremagicchoices/]: 切换组: 0 -> 1
[Render thread/INFO] [or.no.Nomoremagicchoices/]: 已将新组的第一个法术设置为选中状态
```

## 未来改进方向
1. 添加配置选项，让玩家选择是否启用自动选中功能
2. 支持选中组内特定位置的法术（如最常用的法术）
3. 记住每个组上次选中的法术，切换回来时恢复
4. 添加视觉反馈，显示哪个法术被选中

