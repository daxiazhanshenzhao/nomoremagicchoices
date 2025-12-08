# SpellGroupData 使用说明

## 概述

`SpellGroupData` 类用于将所有法术按照每4个一组进行分组管理，支持通过索引获取对应组的法术列表。

## 核心功能

### 1. 自动分组
- 每组最多包含 **4个法术**
- 自动处理法术数量为 **0** 的情况
- 自动处理 **不满4个** 法术的情况
- 使用 `index` 来标识和排序每一组

### 2. 主要方法

#### 2.1 更新法术列表
```java
public void updateSpells()
```
- 从 `ClientMagicData.getSpellSelectionManager()` 获取所有法术
- 自动重新计算分组数量
- 更新当前组索引，确保索引在有效范围内

#### 2.2 根据索引获取法术组
```java
public List<SpellData> getSpellsByIndex(int groupIndex)
```
- 参数：`groupIndex` - 组索引（从0开始）
- 返回：该组的法术列表（最多4个）
- 特殊情况：
  - 如果 `groupIndex` 越界，返回空列表
  - 如果该组不满4个法术，返回实际数量的法术
  - 如果没有法术，返回空列表

#### 2.3 获取当前组法术
```java
public List<SpellData> getCurrentGroupSpells()
```
- 返回当前选中组的法术列表

#### 2.4 获取所有法术
```java
public List<SpellData> getAllSpells()
```
- 返回所有法术的完整列表（未分组）

#### 2.5 切换组索引
```java
public void setCurrentGroupIndex(int newGroupIndex)
```
- 设置当前组索引
- 会触发 `ChangeGroupEvent` 事件
- 自动将索引限制在有效范围内

```java
public void changeIndex(int delta)
```
- 相对改变组索引（例如：delta = 1 表示切换到下一组）

### 3. 获取分组信息

#### 3.1 获取总组数
```java
public static int getGroupCount()
```
- 返回总共有多少组法术

#### 3.2 获取当前组索引
```java
public int getCurrentGroupIndex()
```
- 返回当前选中的组索引

#### 3.3 获取每组法术数量上限
```java
public static int getSpellsPerGroup()
```
- 返回每组最多包含的法术数量（固定为4）

## 使用示例

### 示例1：获取第一组的法术
```java
SpellGroupData groupData = SpellGroupData.instance;
List<SpellData> firstGroup = groupData.getSpellsByIndex(0);

// 处理不同情况
if (firstGroup.isEmpty()) {
    // 没有法术
} else if (firstGroup.size() < 4) {
    // 不满4个法术
} else {
    // 正好4个法术
}
```

### 示例2：遍历所有组
```java
SpellGroupData groupData = SpellGroupData.instance;
int totalGroups = SpellGroupData.getGroupCount();

for (int i = 0; i < totalGroups; i++) {
    List<SpellData> group = groupData.getSpellsByIndex(i);
    // 处理每一组的法术
}
```

### 示例3：切换到下一组
```java
SpellGroupData groupData = SpellGroupData.instance;
groupData.changeIndex(1);  // 切换到下一组
List<SpellData> currentSpells = groupData.getCurrentGroupSpells();
```

### 示例4：在 ClientScrollData 中使用
```java
public static List<Pair<ScrollSpellWight,Integer>> update() {
    SpellGroupData groupData = SpellGroupData.instance;
    groupData.updateSpells();  // 更新法术列表
    
    int size = SpellGroupData.getGroupCount();
    spellWightList = NonNullList.withSize(size+1, Pair.of(ScrollSpellWight.EMPTY, 0));
    
    // 为每一组创建对应的 widget
    for (int i = 0; i < size; i++) {
        List<SpellData> groupSpells = groupData.getSpellsByIndex(i);
        // 创建并初始化 ScrollSpellWight...
    }
    
    return spellWightList;
}
```

## 边界情况处理

### 情况1：没有法术
```java
allSpells.size() == 0
groupCount == 0
getSpellsByIndex(0) 返回空列表
```

### 情况2：只有1-3个法术
```java
allSpells.size() == 2  // 例如
groupCount == 1
getSpellsByIndex(0) 返回包含2个法术的列表
```

### 情况3：正好4个法术
```java
allSpells.size() == 4
groupCount == 1
getSpellsByIndex(0) 返回包含4个法术的列表
```

### 情况4：5-7个法术
```java
allSpells.size() == 6  // 例如
groupCount == 2
getSpellsByIndex(0) 返回包含4个法术的列表
getSpellsByIndex(1) 返回包含2个法术的列表
```

## 注意事项

1. **单例模式**：使用 `SpellGroupData.instance` 访问实例
2. **线程安全**：此类设计用于客户端单线程环境
3. **事件系统**：切换组索引时会触发 `ChangeGroupEvent`，可以被其他模块监听和取消
4. **自动更新**：调用 `updateSpells()` 后会自动重新计算分组，确保数据一致性
5. **防御性编程**：所有返回的列表都是新创建的副本，避免外部修改影响内部状态

## 相关类

- `ClientScrollData` - 使用此类来管理滚动和渲染
- `ChangeGroupEvent` - 组切换事件
- `ScrollSpellWight` - 法术组的可视化组件

