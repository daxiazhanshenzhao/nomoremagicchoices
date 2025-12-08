# ClientScrollData.update() 方法使用说明

## 概述

`update()` 方法用于初始化和更新 `spellWightList`，它会根据当前玩家的法术数据创建对应的可视化组件。

## 方法签名

```java
public static List<Pair<ScrollSpellWight, Integer>> update()
```

## 功能说明

### 1. 更新法术数据
- 获取 `SpellGroupData` 单例实例
- 调用 `updateSpells()` 刷新法术列表
- 重新计算法术分组数量

### 2. 初始化 Widget 列表
- 根据法术组数量初始化 `spellWightList`
- 为每个法术组创建对应的 `ScrollSpellWight` 实例
- 将 Widget 和索引配对存储

### 3. 返回完整列表
- 返回初始化完成的 `spellWightList`

## 实现细节

```java
public static List<Pair<ScrollSpellWight,Integer>> update(){
    // 1. 获取SpellGroupData实例并更新法术列表
    SpellGroupData groupData = getSpellGroupData();
    groupData.updateSpells();
    
    // 2. 获取总组数
    int groupCount = SpellGroupData.getGroupCount();
    
    // 3. 初始化spellWightList，大小为组数
    spellWightList = NonNullList.withSize(groupCount, Pair.of(ScrollSpellWight.EMPTY, 0));
    
    // 4. 为每个法术组创建对应的ScrollSpellWight
    for (int i = 0; i < groupCount; i++) {
        // 获取该组的法术列表（最多4个）
        List<SpellData> groupSpells = groupData.getSpellsByIndex(i);
        
        // 创建ScrollSpellWight（初始位置为(0,0)）
        ScrollSpellWight wight = ScrollSpellWight.create(0, 0, groupSpells);
        
        // 存储widget和索引的配对
        spellWightList.set(i, Pair.of(wight, i));
    }
    
    return spellWightList;
}
```

## 使用场景

### 场景1：游戏初始化时
在玩家加入游戏或法术系统初始化时调用：
```java
// 初始化法术Widget列表
ClientScrollData.update();
```

### 场景2：法术槽位变化时
当玩家的法术列表发生变化时（例如：学习新法术、装备/卸载法术书等）：
```java
// 更新法术Widget列表
ClientScrollData.update();
```

### 场景3：切换法术组时
在切换法术组后可能需要重新初始化Widget：
```java
SpellGroupData groupData = ClientScrollData.getSpellGroupData();
groupData.changeIndex(1);  // 切换到下一组
ClientScrollData.update();  // 重新初始化Widget
```

## 数据结构说明

### spellWightList 结构
```java
List<Pair<ScrollSpellWight, Integer>> spellWightList;
```

- **类型**: `List<Pair<ScrollSpellWight, Integer>>`
- **第一个元素**: `ScrollSpellWight` - 法术组的可视化组件
- **第二个元素**: `Integer` - 该组在整体列表中的索引

### 示例数据
假设玩家有 10 个法术，分为 3 组：
```
spellWightList[0] = Pair.of(ScrollSpellWight(法术0-3), 0)
spellWightList[1] = Pair.of(ScrollSpellWight(法术4-7), 1)
spellWightList[2] = Pair.of(ScrollSpellWight(法术8-9), 2)
```

## 边界情况处理

### 情况1：没有法术
```java
groupCount = 0
spellWightList.size() = 0
返回空列表
```

### 情况2：法术数量不满4个
```java
// 假设只有2个法术
groupCount = 1
spellWightList[0] = Pair.of(ScrollSpellWight(2个法术), 0)
```

### 情况3：法术数量刚好是4的倍数
```java
// 假设有8个法术
groupCount = 2
spellWightList[0] = Pair.of(ScrollSpellWight(法术0-3), 0)
spellWightList[1] = Pair.of(ScrollSpellWight(法术4-7), 1)
```

## 与其他方法的配合

### 1. 配合 tickHandle()
```java
public static void tickHandle() {
    handleRunning();
    handleCurrentTick();
    handleWightTick();  // 使用 spellWightList 进行tick更新
}
```

### 2. 配合 renderHandle()
```java
public static void renderHandle(GuiGraphics context, DeltaTracker partialTick) {
    handleWightRender(context, partialTick);  // 使用 spellWightList 进行渲染
}
```

### 3. 配合 Stream API
使用了 Java Stream API 来高效处理 Widget 列表：
```java
// 在 handleWightTick() 中
spellWightList.stream()
        .map(Pair::getFirst)
        .forEach(IMoveWight::tick);

// 在 handleWightRender() 中
spellWightList.stream()
        .map(Pair::getFirst)
        .forEach(wight -> wight.render(context, partialTick));
```

## 注意事项

1. **线程安全**: 此方法设计用于客户端单线程环境，不保证线程安全
2. **性能考虑**: 
   - 避免频繁调用 `update()`，仅在必要时更新
   - Stream API 使用方法引用以提高性能
3. **初始化顺序**: 确保 `SpellGroupData.instance` 已正确初始化
4. **空值检查**: `handleWightRender()` 和 `handleWightTick()` 中已添加空值检查

## 完整工作流程

```
1. 调用 update()
   ↓
2. SpellGroupData.updateSpells() - 从游戏获取法术数据
   ↓
3. 计算法术组数量 (每4个一组)
   ↓
4. 初始化 spellWightList
   ↓
5. 为每组创建 ScrollSpellWight 实例
   ↓
6. 返回完整的 Widget 列表
   ↓
7. tickHandle() / renderHandle() 使用该列表进行更新和渲染
```

## 相关方法

- `SpellGroupData.updateSpells()` - 更新法术数据
- `SpellGroupData.getSpellsByIndex(int)` - 获取指定组的法术
- `ScrollSpellWight.create()` - 创建Widget实例
- `handleWightTick()` - Tick更新所有Widget
- `handleWightRender()` - 渲染所有Widget

