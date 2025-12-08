# Spell Count Monitor 使用指南

## 概述

`ChangeSpellEvent` 提供了完整的法术数量变更检测功能，所有代码都集中在一个文件中。

## 文件位置

```
src/main/java/org/nomoremagicchoices/api/event/ChangeSpellEvent.java
```

## 核心组件

### 1. ChangeSpellEvent (事件类)

当法术数量发生变化时触发的事件。

**属性：**
- `oldCount`: 旧的法术数量
- `newCount`: 新的法术数量

**方法：**
- `getOldCount()`: 获取旧的法术数量
- `getNewCount()`: 获取新的法术数量

### 2. SpellCountMonitor (监听器类)

静态内部类，负责监控法术数量的变化。

**方法：**

#### `initialize()`
初始化监听器。会尝试获取 `SpellSelectionManager` 并记录当前法术数量。

#### `tick()`
每个客户端tick调用此方法，检测法术数量变化。
- 如果未初始化，会自动尝试初始化
- 检测到数量变化时会触发 `ChangeSpellEvent` 事件
- 自动处理异常并重置状态

#### `reset()`
重置监听器状态，清空记录的法术数量。

#### `getLastSpellCount()`
获取上次记录的法术数量。

#### `isInitialized()`
检查监听器是否已初始化。

## 使用方法

### 1. 在客户端Tick中调用监听器

已在 `ClientEventHandle` 中集成：

```java
@SubscribeEvent
public static void clientTickEvent(ClientTickEvent.Pre event) {
    ClientScrollData.tickHandle();
    ChangeSpellEvent.SpellCountMonitor.tick();
}
```

### 2. 监听事件

在任何需要响应法术数量变化的地方，订阅 `ChangeSpellEvent`：

```java
@SubscribeEvent
public static void onSpellCountChange(ChangeSpellEvent event) {
    int oldCount = event.getOldCount();
    int newCount = event.getNewCount();
    
    // 处理法术数量变化
    if (newCount > oldCount) {
        // 法术增加
        System.out.println("法术数量增加: " + oldCount + " -> " + newCount);
    } else {
        // 法术减少
        System.out.println("法术数量减少: " + oldCount + " -> " + newCount);
    }
    
    // 可以在这里更新 UI、刷新数据等
}
```

### 3. 集成到 SpellGroupData

可以在 `SpellGroupData` 中监听此事件自动更新：

```java
@SubscribeEvent
public static void onSpellCountChange(ChangeSpellEvent event) {
    // 自动更新法术分组数据
    SpellGroupData.instance.updateSpells();
}
```

## 工作流程

```
Client Tick
    ↓
SpellCountMonitor.tick()
    ↓
检查初始化状态
    ↓
获取当前法术列表
    ↓
使用 equals() 与上次记录比较
    ↓
如果列表变化（数量或排序）
    ↓
触发 ChangeSpellEvent
    ↓
更新记录的列表
```

## 检测机制

使用 `List.equals()` 方法进行简单直接的比较：
- **数量变化**: 列表大小不同时，equals 返回 false
- **排序变化**: 元素顺序不同时，equals 返回 false
- **内容变化**: 法术替换时，equals 返回 false

这种方式简单高效，无需复杂的哈希计算。

## 特点

1. **自动初始化**: 首次调用 `tick()` 时会自动初始化
2. **异常处理**: 出现异常时会自动重置并在下次重试
3. **线程安全**: 使用静态变量，适合客户端单线程环境
4. **单一文件**: 所有代码都在一个文件中，便于维护
5. **简单高效**: 使用 equals 判定，无需复杂的哈希计算
6. **全面检测**: 同时检测数量、排序和内容变化

## 注意事项

1. 此功能仅在客户端使用 (`@Dist.CLIENT`)
2. 必须在客户端tick中定期调用 `tick()` 方法
3. 监听器会自动处理 `SpellSelectionManager` 为 null 的情况
4. 事件会在每次数量变化时触发，不论增加还是减少

## 示例：完整的事件监听器

```java
@EventBusSubscriber(Dist.CLIENT)
public class SpellEventListener {
    
    @SubscribeEvent
    public static void onSpellCountChange(ChangeSpellEvent event) {
        int oldCount = event.getOldCount();
        int newCount = event.getNewCount();
        
        Nomoremagicchoices.LOGGER.info(
            "法术数量从 {} 变更为 {}", oldCount, newCount
        );
        
        // 更新相关数据
        SpellGroupData.instance.updateSpells();
        
        // 刷新UI
        // ...
    }
}
```

## 扩展

如果需要监听更多类型的变化（如法术排序），可以：

1. 在 `SpellCountMonitor` 中添加更多追踪字段
2. 创建新的事件类型
3. 在 `tick()` 方法中添加更多检测逻辑

但建议保持单一职责原则，让监听器只关注数量变化。

