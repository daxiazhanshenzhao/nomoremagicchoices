# 索引概念详解：globalIndex vs selectionIndex

## 📊 核心概念对比

| 概念 | 作用域 | 范围 | 含义 |
|------|--------|------|------|
| **`globalIndex`** | 所有法术 | `0` ~ `spellCount-1` | 法术在整个列表中的绝对位置 |
| **`selectionIndex`** | 当前选中 | `0` ~ `spellCount-1` | 玩家当前选中的法术索引 |
| **`currentGroup`** | 组 | `0` ~ `groupCount-1` | 当前显示的是第几组 |
| **`localIndex`** | 当前组内 | `0` ~ `3` | 法术在当前组内的相对位置 |

---

## 🎯 实例说明（假设有9个法术）

### **场景：玩家有9个法术，当前选中第5个法术**

```
全局视图（allSpells）：
globalIndex:   0     1     2     3     4     5     6     7     8
法术:         Sp0   Sp1   Sp2   Sp3   Sp4   Sp5   Sp6   Sp7   Sp8
                                              ↑ (selectionIndex = 5)

分组视图：
第0组 (currentGroup=0):  [Sp0, Sp1, Sp2, Sp3]  (globalIndex: 0-3)
第1组 (currentGroup=1):  [Sp4, Sp5, Sp6, Sp7]  (globalIndex: 4-7)  ← 当前显示这一组
第2组 (currentGroup=2):  [Sp8]                 (globalIndex: 8)

当前组内视图（currentSpells）：
localIndex:    0     1     2     3
法术:         Sp4   Sp5   Sp6   Sp7
                    ↑ (localIndex = 1)
```

---

## 💻 代码中的使用

### **1. 初始化时计算当前组**

```java
int selectionIndex = selectionManager.getSelectionIndex();  // = 5
this.currentGroup = selectionIndex / 4;  // = 5 / 4 = 1 (第1组)
```

### **2. 更新当前组的法术列表**

```java
private void updateCurrentSpells() {
    this.currentSpells.clear();
    
    // 计算当前组的全局索引范围
    int startIndex = currentGroup * 4;  // = 1 * 4 = 4 (globalIndex)
    int endIndex = Math.min((currentGroup + 1) * 4, spellCount);  // = 8
    
    // 提取 globalIndex 4-7 的法术，存入 currentSpells
    for (int globalIndex = startIndex; globalIndex < endIndex; globalIndex++) {
        SpellData spellData = allSpells.get(globalIndex);
        if (spellData != null && spellData.getSpell() != null) {
            // localIndex 自动为 0, 1, 2, 3
            this.currentSpells.add(spellData.getSpell());
        }
    }
}
```

### **3. 渲染时使用 localIndex**

```java
@Override
public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
    // 遍历当前组的法术（currentSpells）
    for (int localIndex = 0; localIndex < currentSpells.size(); localIndex++) {
        AbstractSpell spell = currentSpells.get(localIndex);
        
        // 使用 localIndex 计算渲染位置
        int x = centerX + localIndex * spacing;  // localIndex: 0, 1, 2, 3
        guiGraphics.blit(spell.getIcon(), x, y, ...);
    }
}
```

---

## 🔄 索引转换关系

### **从 selectionIndex 到 currentGroup**
```java
currentGroup = selectionIndex / 4;
```

**示例：**
- `selectionIndex = 0` → `currentGroup = 0`
- `selectionIndex = 3` → `currentGroup = 0`
- `selectionIndex = 4` → `currentGroup = 1`
- `selectionIndex = 5` → `currentGroup = 1`
- `selectionIndex = 8` → `currentGroup = 2`

### **从 currentGroup 到 globalIndex 范围**
```java
int startGlobalIndex = currentGroup * 4;
int endGlobalIndex = Math.min((currentGroup + 1) * 4, spellCount);
```

**示例：**
- `currentGroup = 0` → `globalIndex 0-3`
- `currentGroup = 1` → `globalIndex 4-7`
- `currentGroup = 2` → `globalIndex 8-8`

### **从 globalIndex 到 localIndex**
```java
int localIndex = globalIndex - (currentGroup * 4);
```

**示例（currentGroup = 1）：**
- `globalIndex = 4` → `localIndex = 0`
- `globalIndex = 5` → `localIndex = 1`
- `globalIndex = 6` → `localIndex = 2`
- `globalIndex = 7` → `localIndex = 3`

---

## 🎮 实际应用场景

### **场景1：玩家切换法术**
```java
// 玩家按下切换键，selectionIndex 从 5 变为 6
int oldSelectionIndex = 5;  // Sp5
int newSelectionIndex = 6;  // Sp6

// 两者都在第1组，不需要切换组
int oldGroup = oldSelectionIndex / 4;  // = 1
int newGroup = newSelectionIndex / 4;  // = 1

if (oldGroup != newGroup) {
    // 切换组，重新加载 currentSpells
    this.currentGroup = newGroup;
    updateCurrentSpells();
}
```

### **场景2：玩家手动切换组**
```java
public void changeGroup(int direction) {
    // 从第1组切换到第2组
    this.currentGroup = (this.currentGroup + direction + this.groupCount) % this.groupCount;
    
    // 重新加载第2组的法术到 currentSpells
    updateCurrentSpells();  // currentSpells 现在包含 [Sp8]
}
```

---

## 🎯 总结

- **`globalIndex`** - "这个法术在整个列表中排第几"
- **`selectionIndex`** - "玩家当前选中的是第几个法术"
- **`currentGroup`** - "当前显示的是第几组"
- **`localIndex`** - "这个法术在当前组内排第几"

**记忆口诀：**
> Global 全局看，Selection 选中谁，  
> Group 组别分，Local 组内数。

希望这个解释能帮助你理解！🚀

