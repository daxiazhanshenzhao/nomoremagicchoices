# SpellSelectionLayerV1 重构说明

## 重构目标 ✅

1. ✅ **提升可读性** - 将大方法拆分为小的、职责单一的方法
2. ✅ **精简代码结构** - 移除重复代码，提取常量
3. ✅ **取消硬编码** - 移除静态的 `currentGroup` 变量，改为动态计算
4. ✅ **动态滚动条** - 实现类似创造模式物品栏的滚动条逻辑

## 主要改进

### 1. 移除硬编码的法术组
**之前：**
```java
private static int currentGroup = 0; // 静态变量，硬编码
```

**现在：**
```java
private int calculateCurrentGroup(SpellSelectionManager manager) {
    return manager.getSelectionIndex() / SPELLS_PER_ROW;
}
```
- 每次渲染时动态计算当前组
- 不再维护静态状态
- 更加可靠，不会出现状态不同步问题

### 2. 动态滚动条位置计算
**核心算法：**
```java
private int calculateScrollHandlePosition(int currentGroup, int totalGroups, int scrollY) {
    if (totalGroups <= 1) {
        return scrollY + 1;
    }
    
    // 计算滚动进度 (0.0 到 1.0)
    float scrollProgress = (float) currentGroup / (totalGroups - 1);
    
    // 将进度映射到可用的滚动范围
    int handleOffset = Math.round(scrollProgress * SCROLL_USABLE_HEIGHT);
    
    return scrollY + 1 + handleOffset;
}
```

**工作原理：**
- `SCROLL_USABLE_HEIGHT = 22 - 6 = 16` 像素（可用滚动范围）
- 滚动进度 = 当前组 / (总组数 - 1)
- 滑块位置 = 起始Y + 1 + (进度 × 可用高度)

**示例：**
- 3组法术，第1组：进度 = 0/2 = 0.0，位置 = Y + 1 + 0 = Y + 1（顶部）
- 3组法术，第2组：进度 = 1/2 = 0.5，位置 = Y + 1 + 8 = Y + 9（中间）
- 3组法术，第3组：进度 = 2/2 = 1.0，位置 = Y + 1 + 16 = Y + 17（底部）

### 3. 代码结构优化

**之前：** 所有逻辑在一个 `render()` 方法中，超过 150 行

**现在：** 拆分为多个职责单一的方法

```
render()
├── calculateCurrentGroup()          // 计算当前组
├── calculateSpellBarPosition()      // 计算位置
├── renderSpellSlots()               // 渲染法术槽
│   └── renderSingleSpellSlot()      // 渲染单个槽
│       └── renderCooldownOverlay()  // 渲染冷却
├── renderSelectedSpellIcon()        // 渲染选中图标
│   └── shouldShowSelectedSpell()    // 判断是否显示
├── renderScrollBar()                // 渲染滚动条
│   ├── calculateTotalGroups()       // 计算总组数
│   └── calculateScrollHandlePosition() // 计算滑块位置
└── updateCastingState()             // 更新状态
```

### 4. 常量提取与命名优化

**之前：**
```java
private static final int SPACING = 22;
int totalWidth = (SPELLS_PER_GROUP - 1) * SPACING + 22;
```

**现在：**
```java
private static final int SPELL_SLOT_SIZE = 22;      // 法术槽大小
private static final int SPELL_ICON_SIZE = 16;      // 法术图标大小
private static final int SPELL_ICON_OFFSET = 3;     // 图标偏移
private static final int SCROLL_USABLE_HEIGHT = 16; // 滚动条可用高度
```

### 5. 使用 Record 简化数据结构

**新增：**
```java
private record SpellBarPosition(int x, int y, int width) {}
```

替代多个分散的变量，使位置信息更加清晰。

## 性能优化

1. **减少重复计算** - 位置信息只计算一次，传递给各个方法
2. **提前返回** - 不满足条件时立即返回，避免不必要的计算
3. **流式处理** - 使用 Stream API 简化集合操作

## 可维护性提升

1. **方法职责单一** - 每个方法只做一件事
2. **清晰的命名** - 方法名清楚地表达其功能
3. **完整的注释** - 每个方法都有 JavaDoc 注释
4. **常量集中管理** - 所有魔法数字都提取为常量

## 兼容性

- ✅ 完全兼容现有的 `getCurrentGroup()` 静态方法
- ✅ 保持原有的渲染效果
- ✅ 支持任意数量的法术（不再限制于固定组数）

## 测试建议

1. **不同法术数量**：测试 1-20 个法术的显示效果
2. **滚动条动画**：切换法术组，观察滚动条是否平滑移动
3. **边界情况**：只有 1 组法术时，滚动条应该不显示
4. **创造模式**：确认在创造模式下不显示法术栏

