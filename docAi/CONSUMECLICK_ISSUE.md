# consumeClick() 重复调用问题及解决方案

## 问题描述

在 `ClientScrollData.handleRunning()` 中调用 `ModKeyMapping.CHANG_GROUP.get().consumeClick()` 时，日志 `"你按我干嘛"` 没有被触发。

## 问题原因

### consumeClick() 的工作机制
`consumeClick()` 是一个**消耗性方法**，每次按键点击只能成功调用一次：
- **第一次调用**：返回 `true` 并标记事件为"已消耗"
- **后续调用**：返回 `false`（因为事件已被消耗）

### 代码执行顺序

1. **InputEvent.Key 事件触发** (按键按下时)
   ```java
   ClientInputHandle.onClientClick(InputEvent.Key event)
   └── handleGroup()
       └── ModKeyMapping.CHANG_GROUP.get().consumeClick() // 第一次调用，返回 true
           └── SpellSelectionLayerV1.nextGroup()
   ```

2. **ClientTickEvent.Pre 事件触发** (每游戏tick)
   ```java
   ClientEventHandle.clientTickEvent(ClientTickEvent.Pre event)
   └── ClientScrollData.tickHandle()
       └── handleRunning()
           └── ModKeyMapping.CHANG_GROUP.get().consumeClick() // 第二次调用，返回 false
               └── 条件不满足，日志不会输出
   ```

### 问题根源
**`ClientInputHandle.handleGroup()` 先消耗了按键事件**，导致 `ClientScrollData.handleRunning()` 中的 `consumeClick()` 总是返回 `false`。

## 解决方案

### 已实施方案：注释掉重复的按键处理

在 `ClientInputHandle.java` 中注释掉 `handleGroup()` 的调用：

```java
@SubscribeEvent
public static void onClientClick(InputEvent.Key event){
    handleSkill();
    // handleGroup(); // 注释掉：避免消耗consumeClick事件，由ClientScrollData.handleRunning()处理
}
```

这样 `consumeClick()` 只会在 `ClientScrollData.handleRunning()` 中被调用一次，日志可以正常输出。

## 其他可选方案

### 方案2：使用 isDown() 而不是 consumeClick()

如果需要持续检测按键状态而不是点击事件：

```java
public static void handleRunning(){
    if (ModKeyMapping.CHANG_GROUP.get().isDown() && !isRunning){
        isRunning = true;
        Nomoremagicchoices.LOGGER.info("你按我干嘛");
    }
}
```

**注意**：`isDown()` 会在按键按住期间持续返回 `true`，需要配合状态标志使用。

### 方案3：统一按键处理位置

将所有按键处理逻辑集中到一个地方：

**选项A：只在 ClientInputHandle 中处理**
```java
// ClientInputHandle.java
public static void handleGroup() {
    if (ModKeyMapping.CHANG_GROUP.get().consumeClick()){
        ClientScrollData.triggerGroupChange(); // 通知 ClientScrollData
        SpellSelectionLayerV1.nextGroup();
    }
}

// ClientScrollData.java
public static void triggerGroupChange() {
    if (!isRunning) {
        isRunning = true;
        Nomoremagicchoices.LOGGER.info("你按我干嘛");
    }
}
```

**选项B：只在 ClientScrollData 中处理**（当前方案）
```java
// ClientScrollData.java
public static void handleRunning(){
    if (ModKeyMapping.CHANG_GROUP.get().consumeClick() && !isRunning){
        isRunning = true;
        Nomoremagicchoices.LOGGER.info("你按我干嘛");
        SpellSelectionLayerV1.nextGroup(); // 直接在这里调用
    }
}
```

### 方案4：使用自定义事件

创建一个自定义事件来协调不同模块的按键响应：

```java
// 创建事件
public class GroupChangeEvent extends Event {
    // ...
}

// 在 ClientInputHandle 中发布事件
public static void handleGroup() {
    if (ModKeyMapping.CHANG_GROUP.get().consumeClick()){
        NeoForge.EVENT_BUS.post(new GroupChangeEvent());
    }
}

// 在 ClientScrollData 中监听事件
@SubscribeEvent
public static void onGroupChange(GroupChangeEvent event) {
    if (!isRunning) {
        isRunning = true;
        Nomoremagicchoices.LOGGER.info("你按我干嘛");
    }
}
```

## 调试技巧

### 1. 添加调试日志
```java
public static void handleGroup() {
    boolean consumed = ModKeyMapping.CHANG_GROUP.get().consumeClick();
    Nomoremagicchoices.LOGGER.info("ClientInputHandle.handleGroup() - consumed: " + consumed);
    if (consumed){
        SpellSelectionLayerV1.nextGroup();
    }
}

public static void handleRunning(){
    boolean consumed = ModKeyMapping.CHANG_GROUP.get().consumeClick();
    Nomoremagicchoices.LOGGER.info("ClientScrollData.handleRunning() - consumed: " + consumed + ", isRunning: " + isRunning);
    if (consumed && !isRunning){
        isRunning = true;
        Nomoremagicchoices.LOGGER.info("你按我干嘛");
    }
}
```

### 2. 检查事件触发顺序
```java
@SubscribeEvent
public static void onClientClick(InputEvent.Key event){
    Nomoremagicchoices.LOGGER.info("InputEvent.Key triggered");
    handleSkill();
    handleGroup();
}

@SubscribeEvent
public static void clientTickEvent(ClientTickEvent.Pre event) {
    Nomoremagicchoices.LOGGER.info("ClientTickEvent.Pre triggered");
    ClientScrollData.tickHandle();
}
```

## 最佳实践

1. **避免重复调用 consumeClick()**
   - 一个按键事件在整个处理流程中只应调用一次 `consumeClick()`
   - 如果需要多个模块响应，使用事件系统或回调函数

2. **明确按键处理的职责**
   - 输入层 (ClientInputHandle)：检测按键输入
   - 逻辑层 (ClientScrollData)：处理业务逻辑
   - 表现层 (GUI)：更新视觉效果

3. **使用合适的按键检测方法**
   - `consumeClick()`：检测单次点击，会消耗事件
   - `isDown()`：检测按键状态，不消耗事件
   - `wasPressed()`：类似 consumeClick() 但在某些情况下行为略有不同

4. **事件触发时机**
   - `InputEvent.Key`：按键立即触发，早于 Tick 事件
   - `ClientTickEvent.Pre`：每游戏 tick 开始时触发（20次/秒）
   - `ClientTickEvent.Post`：每游戏 tick 结束时触发

## 相关文件

- `ClientScrollData.java` - 主要的滚动数据处理类
- `ClientInputHandle.java` - 客户端输入处理类
- `ClientEventHandle.java` - 客户端事件处理类
- `ModKeyMapping.java` - 按键映射定义

## 总结

问题的核心在于 **consumeClick() 只能被消耗一次**。在多个地方调用会导致只有第一个调用能成功获取到按键事件。解决方案是确保每个按键事件只在一个地方被消耗，其他地方通过回调、事件或共享状态来获取通知。

