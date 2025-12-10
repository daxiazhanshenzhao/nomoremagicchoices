# Minecraft 滚轮切换物品栏的 Mixin 注入点

## 核心注入点

### 1. Minecraft.handleKeybinds() 方法

这是处理所有键盘输入的核心方法，包括滚轮切换物品栏。

**位置**：`net.minecraft.client.Minecraft.handleKeybinds()`

**相关代码**（反编译后）：
```java
// 在 Minecraft.handleKeybinds() 方法中
while (this.options.keyHotbarSlots[i].consumeClick()) {
    if (this.player.isSpectator()) {
        this.gui.getSpectatorGui().onHotbarSelected(i);
    } else {
        this.player.getInventory().selected = i;  // ← 这里是关键！
    }
}
```

### 2. 当前项目中的实现

你的项目已经在 `SkillCombatMixin.java` 中实现了拦截：

```java
@WrapWithCondition(
    method = "handleKeybinds", 
    at = @At(
        value = "FIELD", 
        target = "Lnet/minecraft/world/entity/player/Inventory;selected:I", 
        ordinal = 0, 
        opcode = Opcodes.PUTFIELD
    )
)
private boolean handleInput(Inventory instance, int index) {
    // 返回 false 阻止原版切换
    // 返回 true 允许原版切换
}
```

**这个 Mixin 同时拦截了**：
- ✅ 数字键 1-9 切换物品栏
- ✅ 滚轮切换物品栏

因为它们都是通过修改 `Inventory.selected` 字段来实现的。

## 如果需要单独拦截滚轮

如果你只想拦截滚轮而不拦截数字键，需要更精确的注入点。

### 方案1：区分滚轮和数字键（推荐）

在现有的 Mixin 中添加判断：

```java
@WrapWithCondition(method = "handleKeybinds", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Inventory;selected:I", ordinal = 0, opcode = Opcodes.PUTFIELD))
private boolean handleInput(Inventory instance, int index) {
    var mc = Minecraft.getInstance();
    if (mc.player == null) return true;
    
    // 获取当前选中的槽位
    int currentSelected = instance.selected;
    
    // 判断是否是滚轮切换（连续的槽位变化）
    boolean isScrollWheel = Math.abs(index - currentSelected) == 1 
                         || (index == 0 && currentSelected == 8) 
                         || (index == 8 && currentSelected == 0);
    
    // 判断是否是数字键切换（跳跃的槽位变化）
    boolean isNumberKey = !isScrollWheel;
    
    if (isScrollWheel) {
        // 滚轮切换的处理逻辑
        return handleScrollWheel(instance, index);
    } else {
        // 数字键切换的处理逻辑
        return handleNumberKey(instance, index);
    }
}
```

### 方案2：使用 MouseHandler（更底层）

如果需要在滚轮输入阶段就拦截：

```java
@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    
    @Inject(
        method = "onScroll",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onMouseScroll(long window, double xOffset, double yOffset, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;
        
        // yOffset > 0 表示向上滚
        // yOffset < 0 表示向下滚
        
        boolean shouldIntercept = shouldInterceptScroll();
        
        if (shouldIntercept) {
            // 自定义滚轮逻辑
            handleCustomScroll(yOffset);
            ci.cancel();  // 取消原版滚轮处理
        }
    }
}
```

**注意**：`MouseHandler.onScroll` 方法需要找到正确的混淆映射名称。

### 方案3：修改 Inventory.selected（字段级别）

直接拦截字段的写入操作（你当前的方案）：

```java
@WrapWithCondition(
    method = "handleKeybinds", 
    at = @At(
        value = "FIELD", 
        target = "Lnet/minecraft/world/entity/player/Inventory;selected:I",
        opcode = Opcodes.PUTFIELD
    )
)
```

**优点**：
- ✅ 同时拦截数字键和滚轮
- ✅ 注入点稳定
- ✅ 不需要关心输入来源

**缺点**：
- ❌ 无法区分是滚轮还是数字键
- ❌ 可能拦截到其他修改 selected 的代码

## 滚轮切换的完整流程

```
1. 玩家滚动鼠标滚轮
   ↓
2. MouseHandler.onScroll(long window, double xOffset, double yOffset)
   - GLFW 回调
   - yOffset > 0: 向上滚 → 槽位 -1
   - yOffset < 0: 向下滚 → 槽位 +1
   ↓
3. Minecraft.handleKeybinds()
   - 处理滚轮导致的快捷栏变化
   - 计算新的槽位索引
   ↓
4. this.player.getInventory().selected = newIndex
   - 修改选中的槽位
   - ← **你的 Mixin 在这里拦截**
   ↓
5. 渲染更新
   - HotbarWidget 更新显示
```

## 推荐的实现方案

基于你当前的代码，我建议增强现有的 Mixin：

```java
@Mixin(value = Minecraft.class, priority = 999)
public class SkillCombatMixin {
    
    @WrapWithCondition(
        method = "handleKeybinds", 
        at = @At(
            value = "FIELD", 
            target = "Lnet/minecraft/world/entity/player/Inventory;selected:I", 
            ordinal = 0, 
            opcode = Opcodes.PUTFIELD
        )
    )
    private boolean handleInput(Inventory instance, int index) {
        var mc = Minecraft.getInstance();
        if (mc.player == null) return true;

        boolean hasSkillWeaponTag = mc.player.getMainHandItem().is(TagInit.SKILL_WEAPON) 
                                 || mc.player.getMainHandItem().has(ComponentRegistry.CASTING_IMPLEMENT);
        
        if (hasSkillWeaponTag) {
            ClientInputHandle.setHasWeapon(true);
            
            // 判断是滚轮还是数字键
            int currentSlot = instance.selected;
            boolean isScrollWheel = isScrollWheelChange(currentSlot, index);
            
            if (isScrollWheel) {
                // 滚轮切换的特殊处理
                return handleScrollWheelChange(currentSlot, index);
            } else {
                // 数字键切换的处理
                boolean shouldIntercept = ModKeyMapping.isAnySkillKeyBoundToNumber() 
                                       && !ClientScrollData.getSpellWightList().isEmpty();
                return !shouldIntercept;
            }
        } else {
            ClientInputHandle.setHasWeapon(false);
        }

        return true;
    }
    
    private boolean isScrollWheelChange(int from, int to) {
        // 检查是否是连续的槽位变化
        int diff = Math.abs(to - from);
        return diff == 1 || diff == 8;  // 8 表示从 0 到 8 或 8 到 0 的环绕
    }
    
    private boolean handleScrollWheelChange(int from, int to) {
        // 滚轮切换的自定义逻辑
        // 例如：切换法术组而不是切换物品栏
        boolean scrollingUp = (to == from - 1) || (from == 0 && to == 8);
        
        if (scrollingUp) {
            // 向上滚动 - 切换到上一组
            // ClientScrollData.previousGroup();
        } else {
            // 向下滚动 - 切换到下一组
            // ClientScrollData.nextGroup();
        }
        
        return false;  // 阻止原版物品栏切换
    }
}
```

## 相关的 Minecraft 类

1. **`net.minecraft.client.Minecraft`**
   - `handleKeybinds()` - 处理所有键盘输入

2. **`net.minecraft.client.MouseHandler`**
   - `onScroll(long, double, double)` - 处理滚轮输入

3. **`net.minecraft.world.entity.player.Inventory`**
   - `selected` 字段 - 当前选中的物品栏槽位 (0-8)

4. **`net.minecraft.client.Options`**
   - `keyHotbarSlots` - 快捷栏按键绑定数组

## 总结

**当前最佳实践**：
- ✅ 在 `Minecraft.handleKeybinds()` 中拦截 `Inventory.selected` 的修改
- ✅ 通过计算槽位变化判断是滚轮还是数字键
- ✅ 根据不同的输入方式执行不同的逻辑

**你的项目已经实现了核心拦截**，只需要在 `handleInput` 方法中添加区分逻辑即可分别处理滚轮和数字键输入。

