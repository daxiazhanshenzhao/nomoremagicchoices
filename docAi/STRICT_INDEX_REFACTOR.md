# 严格索引对应 - 重构完成

## ✅ 重构目标

解决空槽位导致的渲染错位问题，使用**严格的索引对应关系**。

## 🔄 重构策略

### **核心思想：保持索引对应，渲染时跳过空槽位**

**之前的问题：**
```
原始槽位: [Sp0] [EMPTY] [Sp2] [Sp3] [EMPTY] [Sp5]
索引:      0      1       2      3      4       5

过滤后:   [Sp0] [Sp2] [Sp3] [Sp5]
新索引:    0      1      2      3

❌ 问题：选中索引5的Sp5，在过滤后变成索引3
❌ 导致：计算的组号错误，渲染位置错位
```

**重构后的方案：**
```
所有槽位: [Sp0] [EMPTY] [Sp2] [Sp3] [EMPTY] [Sp5]
索引:      0      1       2      3      4       5

不过滤，保持原始索引！

✅ 选中索引5 → currentGroup = 5 / 4 = 1（第1组）
✅ 第1组包含索引4-7的槽位：[EMPTY] [Sp5]
✅ 渲染时：跳过EMPTY，只渲染Sp5在slotIndex=1的位置
```

---

## 📝 代码改动

### **1. 修改数据结构**

```java
// 之前：
private List<AbstractSpell> currentSpells = new ArrayList<>();

// 之后：
private List<SpellData> currentSpells = new ArrayList<>();  // 存储SpellData而不是AbstractSpell
```

**原因：** 需要保留空槽位信息（SpellData.EMPTY），才能保持索引对应。

---

### **2. 修改 init() 方法**

```java
// 之前（过滤空槽位）：
this.allSpells = selectionManager.getAllSpells().stream()
    .map((slot) -> slot.spellData)
    .filter(spellData -> spellData != null && !spellData.equals(SpellData.EMPTY))  // ❌ 过滤
    .toList();

// 之后（保留所有槽位）：
this.allSpells = selectionManager.getAllSpells().stream()
    .map((slot) -> slot.spellData)
    .toList();  // ✅ 不过滤，保持原始索引
```

**关键改动：**
- ❌ 删除了 `filter()` 操作
- ✅ 保留所有槽位（包括 `SpellData.EMPTY`）
- ✅ 使用原始的 `spellCount`
- ✅ 直接使用 `selectionIndex / 4` 计算组号

---

### **3. 修改 updateCurrentSpells() 方法**

```java
// 之前（过滤空槽位）：
for (int globalIndex = startIndex; globalIndex < endIndex; globalIndex++) {
    if (globalIndex < allSpells.size()) {
        SpellData spellData = allSpells.get(globalIndex);
        if (spellData != null && !spellData.equals(SpellData.EMPTY)) {  // ❌ 过滤
            this.currentSpells.add(spellData.getSpell());
        }
    }
}

// 之后（保留所有槽位）：
for (int globalIndex = startIndex; globalIndex < endIndex; globalIndex++) {
    if (globalIndex < allSpells.size()) {
        SpellData spellData = allSpells.get(globalIndex);
        this.currentSpells.add(spellData);  // ✅ 添加所有槽位，包括空槽位
    }
}
```

**关键改动：**
- ❌ 删除了空槽位检查
- ✅ 添加所有槽位到 `currentSpells`
- ✅ 保持严格的索引对应：`currentSpells[0]` 对应全局索引 `startIndex + 0`

---

### **4. 修改 render() 方法**

```java
// 之前（直接遍历有效法术）：
int n = 0;
for (AbstractSpell spell : currentSpells) {
    guiGraphics.blit(TEXTURE, centerX + n * spacing, centerY, ...);
    guiGraphics.blit(spell.getSpellIconResource(), ...);
    n++;
}

// 之后（遍历所有槽位，跳过空槽位）：
for (int slotIndex = 0; slotIndex < currentSpells.size(); slotIndex++) {
    SpellData spellData = currentSpells.get(slotIndex);
    
    // 跳过空槽位
    if (spellData == null || spellData.equals(SpellData.EMPTY)) {
        continue;
    }
    
    AbstractSpell spell = spellData.getSpell();
    if (spell == null) {
        continue;
    }
    
    // 使用 slotIndex 保证严格的位置对应
    int x = centerX + slotIndex * spacing;
    
    guiGraphics.blit(TEXTURE, x, centerY, ...);
    guiGraphics.blit(spell.getSpellIconResource(), x + 3, centerY + 3, ...);
}
```

**关键改动：**
- ✅ 使用索引遍历而不是增强 for 循环
- ✅ 添加空槽位检查，跳过但不影响索引
- ✅ 使用 `slotIndex` 计算位置，保证严格对应

---

### **5. 删除 calculateActualIndex() 方法**

```java
// ❌ 删除了整个方法
private int calculateActualIndex(int globalSelectionIndex) { ... }
```

**原因：** 不再需要索引映射，直接使用原始索引。

---

## 📊 对比示例

### **场景：8个槽位，包含空槽位**

```
槽位配置：
索引:  0      1       2      3       4      5      6       7
槽位: [Sp0] [EMPTY] [Sp2] [EMPTY] [Sp4] [Sp5] [EMPTY] [Sp7]
```

#### **之前的逻辑（有bug）：**

```
过滤后: [Sp0] [Sp2] [Sp4] [Sp5] [Sp7]
新索引:  0     1     2     3     4

如果选中原始索引5 (Sp5)：
  1. calculateActualIndex(5) → 返回3
  2. currentGroup = 3 / 4 = 0
  3. 第0组包含：[Sp0, Sp2, Sp4, Sp5]
  4. 渲染位置：Sp0在x=0, Sp2在x=1, Sp4在x=2, Sp5在x=3
  
❌ 问题：Sp5应该在第1组，但显示在第0组
❌ 问题：渲染位置不对应原始槽位索引
```

#### **重构后的逻辑（正确）：**

```
不过滤: [Sp0] [EMPTY] [Sp2] [EMPTY] [Sp4] [Sp5] [EMPTY] [Sp7]
索引:    0      1       2      3       4     5      6       7

分组（每组4个槽位）：
  第0组（索引0-3）: [Sp0] [EMPTY] [Sp2] [EMPTY]
  第1组（索引4-7）: [Sp4] [Sp5] [EMPTY] [Sp7]

如果选中索引5 (Sp5)：
  1. currentGroup = 5 / 4 = 1
  2. 第1组包含：[Sp4] [Sp5] [EMPTY] [Sp7]
  3. currentSpells = [Sp4的SpellData, Sp5的SpellData, EMPTY, Sp7的SpellData]
  4. 渲染：
     - slotIndex=0: Sp4, x=centerX+0*spacing
     - slotIndex=1: Sp5, x=centerX+1*spacing
     - slotIndex=2: EMPTY → 跳过
     - slotIndex=3: Sp7, x=centerX+3*spacing
  
✅ 正确：Sp5在第1组
✅ 正确：渲染位置严格对应槽位索引
✅ 正确：即使有空槽位，位置也不会错位
```

---

## 🎯 重构优势

### **1. 严格的索引对应**
```java
// 全局索引 globalIndex = currentGroup * 4 + slotIndex
// 例如：第1组的第1个槽位 = 1 * 4 + 1 = 5
```

### **2. 渲染位置正确**
```java
// 渲染位置 = centerX + slotIndex * spacing
// slotIndex 严格对应槽位在组内的位置（0-3）
```

### **3. 空槽位不影响布局**
```
有空槽位:   [Sp0] [    ] [Sp2] [Sp3]
渲染位置:    x=0   跳过    x=2   x=3

✅ Sp2 渲染在第2个位置，不是第1个位置
✅ 保留空槽位的视觉空间
```

### **4. 代码更简单**
- ❌ 删除了复杂的 `calculateActualIndex()` 方法
- ❌ 删除了 `filter()` 操作
- ✅ 直接使用原始索引
- ✅ 逻辑更清晰

---

## 🧪 测试用例

### **测试1：连续槽位**
```
槽位: [Sp0] [Sp1] [Sp2] [Sp3] [Sp4]
选中: 索引2 (Sp2)
结果: currentGroup=0, 渲染Sp0在x=0, Sp1在x=1, Sp2在x=2, Sp3在x=3 ✅
```

### **测试2：有空槽位**
```
槽位: [Sp0] [EMPTY] [Sp2] [Sp3]
选中: 索引2 (Sp2)
结果: currentGroup=0, 渲染Sp0在x=0, 跳过x=1, Sp2在x=2, Sp3在x=3 ✅
```

### **测试3：跨组边界**
```
槽位: [Sp0] [Sp1] [Sp2] [Sp3] | [Sp4] [EMPTY] [Sp6] [Sp7]
选中: 索引4 (Sp4)
结果: currentGroup=1, 第1组渲染Sp4在x=0, 跳过x=1, Sp6在x=2, Sp7在x=3 ✅
```

---

## 🚀 总结

**重构完成！现在代码：**

✅ **保持严格的索引对应关系**  
✅ **不过滤空槽位，在渲染时跳过**  
✅ **渲染位置正确，不会错位**  
✅ **代码更简单，逻辑更清晰**  
✅ **支持任意空槽位配置**  

**核心原则：数据层保持真实，渲染层处理显示。**

