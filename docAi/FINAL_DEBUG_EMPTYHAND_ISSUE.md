# 最终调试方案 - EmptyHand 状态错误追踪

## 当前问题

根据最新日志，`EmptyHand -> EmptyHand` 的无效状态切换已经被成功跳过，但**根本问题依然存在**：

```
*** Casting just ended! Pending state: EmptyHand, Current state: EmptyHand
→ Pending state change skipped (already in EmptyHand state)
```

**核心问题**：为什么在施法期间会有 `pendingStateChange = EmptyHand` 被设置？

## 可能的原因

### 原因1：切换武器的瞬间主副手都为空
在 Minecraft 中，切换物品栏或切换物品时，可能有1-2个 tick 的时间，主手和副手都是空的：

```
Tick 1: MainHand=Weapon, OffHand=Empty
Tick 2: MainHand=Empty, OffHand=Empty  <-- 切换中间状态
Tick 3: MainHand=Staff, OffHand=Empty
```

如果 `updateState()` 在 Tick 2 被调用，就会错误地检测为 `EmptyHand`。

### 原因2：某些物品不被识别
玩家手持的物品既不是 `SKILL_WEAPON`，也没有 `CASTING_IMPLEMENT`，导致被归类为 `EmptyHand`：

```java
if (mainHand.isEmpty() && offHand.isEmpty()) {
    state = SpellSelectionState.EmptyHand;
} else if (mainHand.is(TagInit.SKILL_WEAPON)) {
    state = SpellSelectionState.Weapon;
} else if (mainHand.has(ComponentRegistry.CASTING_IMPLEMENT) || offHand.has(ComponentRegistry.CASTING_IMPLEMENT)) {
    state = SpellSelectionState.Staff;
} else {
    state = SpellSelectionState.EmptyHand;  // <-- 这里！
}
```

### 原因3：施法期间物品被临时清空
Iron's Spellbooks 在施法期间可能会临时修改物品栏状态。

## 新增的调试日志

现在 `updateState()` 会输出详细的物品检测信息：

```
╔════════════════════════════════════════════════════
║ STATE CHANGE DETECTED in updateState()
║ Weapon -> EmptyHand
║ MainHand: item.minecraft.iron_sword (empty=false)
║ OffHand: item.minecraft.air (empty=true)
║ isWeapon=false, hasCastingImpl=false
║ isCasting: true
╚════════════════════════════════════════════════════
```

这会告诉我们：
1. 主手和副手具体是什么物品
2. 是否为空
3. 是否被识别为武器或法杖
4. 当时是否在施法

## 测试步骤

### 测试场景：持续施法时切换武器

1. **准备**
   - 手持一个 SKILL_WEAPON（如铁剑）
   - 物品栏有一个 CASTING_IMPLEMENT（如法杖）

2. **执行**
   - 释放持续性法术（如 Fire Breath）
   - 在施法期间按数字键切换到法杖

3. **观察日志**
   - 查找 `╔════ STATE CHANGE DETECTED` 的日志
   - 关注以下信息：
     * MainHand 和 OffHand 的具体物品
     * isWeapon 和 hasCastingImpl 的值
     * isCasting 的状态
     * 状态切换的时机

### 预期看到的日志

**正常情况**：
```
╔════════════════════════════════════════════════════
║ STATE CHANGE DETECTED in updateState()
║ Weapon -> Staff
║ MainHand: item.irons_spellbooks.iron_spell_book (empty=false)
║ OffHand: item.minecraft.air (empty=true)
║ isWeapon=false, hasCastingImpl=true
║ isCasting: true
╚════════════════════════════════════════════════════
⚠ State change DELAYED (casting): Weapon -> Staff [PENDING]
```

**异常情况（需要修复）**：
```
╔════════════════════════════════════════════════════
║ STATE CHANGE DETECTED in updateState()
║ Weapon -> EmptyHand  <-- 错误！
║ MainHand: item.minecraft.air (empty=true)  <-- 为什么是空的？
║ OffHand: item.minecraft.air (empty=true)
║ isWeapon=false, hasCastingImpl=false
║ isCasting: true
╚════════════════════════════════════════════════════
⚠ State change DELAYED (casting): Weapon -> EmptyHand [PENDING]
```

## 根据日志的修复方案

### 如果是原因1：切换瞬间为空

**解决方案**：添加状态记忆，在检测到 EmptyHand 时，如果上一个状态不是 EmptyHand，则保持上一个状态：

```java
private static SpellSelectionState lastNonEmptyState = SpellSelectionState.EmptyHand;

private static void updateState() {
    // ...物品检测...
    
    SpellSelectionState newState;
    if (mainHand.isEmpty() && offHand.isEmpty()) {
        // 如果之前不是空手，可能是切换中间状态，保持上一个非空状态
        if (oldState != SpellSelectionState.EmptyHand) {
            newState = oldState;  // 保持不变
        } else {
            newState = SpellSelectionState.EmptyHand;
        }
    } else {
        // ...正常检测...
        if (newState != SpellSelectionState.EmptyHand) {
            lastNonEmptyState = newState;
        }
    }
    
    state = newState;
}
```

### 如果是原因2：物品不被识别

**解决方案**：检查 TagInit.SKILL_WEAPON 和 ComponentRegistry.CASTING_IMPLEMENT 的定义，确保所有武器和法杖都被正确标记。

### 如果是原因3：施法期间物品被清空

**解决方案**：在施法期间完全跳过 `updateState()`：

```java
private static void updateState() {
    if (mc.player == null) return;
    
    // 如果正在施法，不更新状态，避免施法期间的状态混乱
    if (ClientMagicData.isCasting()) {
        return;
    }
    
    // ...正常的状态检测...
}
```

## 下一步行动

1. **运行游戏并重现问题**
2. **复制完整的状态切换日志**（从 `╔════` 开始的部分）
3. **分析日志，确定是哪种原因**
4. **根据原因选择对应的修复方案**

## 临时禁用建议

如果问题持续存在，可以临时禁用延迟机制，改为立即执行：

```java
private static void handleStateChange(SpellSelectionState newState) {
    if (newState == state) {
        return;
    }
    
    // 临时禁用延迟机制
    // if (ClientMagicData.isCasting()) {
    //     pendingStateChange = newState;
    //     return;
    // }
    
    handleStateChangeInternal(newState);
}
```

这样至少能确保状态切换立即生效，虽然可能在施法期间有轻微的视觉闪烁，但总比错位好。

## 总结

现在代码已经添加了最详细的调试日志，下一次运行时，你会看到：
- ✅ 每次状态变化的完整信息
- ✅ 主手和副手的具体物品
- ✅ 物品识别的详细结果
- ✅ 是否在施法
- ✅ 是立即执行还是延迟执行
- ✅ pending 状态是否被正确跳过

**请再次测试并提供完整的日志输出，我会根据日志确定根本原因并提供最终的修复方案！**

