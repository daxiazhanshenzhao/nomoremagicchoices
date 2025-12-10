# 法术选择器系统完善说明

## 概述
已完成动态法术选择器系统的重构，实现了"抽书"式的法术组切换效果。

## 核心改动

### 1. 数据结构变更
- **从**: `List<Pair<ScrollSpellWight, Integer>>`
- **到**: `List<ScrollSpellWight>`
- **原因**: 简化结构，将groupIndex直接存储在ScrollSpellWight内部

### 2. 核心类职责划分

#### ScrollSpellWight (法术Widget)
- **职责**: 单个法术组的渲染和动画
- **新增字段**: `groupIndex` - 存储组索引
- **新增方法**: `getGroupIndex()` - 获取组索引
- **核心方法**:
  - `moveDown(Vector2i)`: 在底部移动，保持Down状态（空手模式使用）
  - `moveFocus(Vector2i)`: 移动到焦点位置，完成后变为Focus状态（持有物品模式使用）
  - `tick()`: 处理移动动画的每帧更新

#### ClientScrollData (数据管理中心)
- **职责**: 管理所有Widget的状态、移动和数据
- **核心功能**:
  1. **状态管理**: 根据手持物品判断EmptyHand/Weapon/Staff状态
  2. **Widget列表管理**: 维护`List<ScrollSpellWight> spellWightList`
  3. **坐标计算**: `calculatePositions()` - 计算所有Widget的目标位置
  4. **切换组逻辑**: `handleRunning()` - 处理按键，触发抽书动画
  5. **状态切换处理**: `handleStateChange()` - 空手/持有物品状态切换时的动画

#### ScrollGroupHelper (抽书/塞书逻辑)
- **职责**: 实现"抽书"和"塞书"的核心算法
- **核心方法**:
  - `drawWight()`: 抽书操作 - 将指定Widget移到列表末尾（顶层）
  - `addWight()`: 塞书操作 - 将顶层Widget插入到指定位置

#### SpellSelectionLayerV2 (渲染层)
- **职责**: 仅负责渲染，不处理任何逻辑
- **核心功能**:
  1. 调用`ClientScrollData.tickHandle()`处理tick
  2. 从`ClientScrollData.getSpellWightList()`获取Widget列表
  3. 遍历渲染所有Widget

## 工作流程

### 空手模式 (EmptyHand)
```
1. 玩家切换组 → 按下切换键
2. ClientScrollData.handleRunning() → 找到下一组的Widget
3. ScrollGroupHelper.drawWight(isEmptyHand=true) → 执行抽书
4. 抽书逻辑:
   - 将目标Widget从当前位置移除
   - 后续Widget前移
   - 目标Widget放到列表末尾
   - 所有Widget使用moveDown()保持Down状态相对移动
5. 结果: 选中的组在最上层，其他组向下移动，类似抽出一本书
```

### 持有物品模式 (Weapon/Staff)
```
1. 玩家切换组 → 按下切换键
2. ClientScrollData.handleRunning() → 找到下一组的Widget
3. ScrollGroupHelper.drawWight(isEmptyHand=false) → 执行抽书
4. 抽书逻辑:
   - 将目标Widget从当前位置移除
   - 后续Widget前移
   - 目标Widget放到列表末尾
   - 前面的Widget使用moveDown()保持Down状态
   - 最后一个Widget使用moveFocus()移到焦点位置，完成后状态变为Focus
5. 结果: 选中的组在Focus位置高亮显示，其他组在底部
```

### 状态切换
```
空手 → 持有物品:
1. handleStateChange检测到状态变化
2. 找到当前组在列表中的位置
3. 执行drawWight(isEmptyHand=false)
4. 当前组移到Focus位置，高亮显示

持有物品 → 空手:
1. handleStateChange检测到状态变化
2. 所有Widget调用down()方法
3. 所有Widget变为Down状态，在底部相对移动
```

## 坐标计算

### 空手模式坐标
```java
baseX = screenWidth / 2 + 50
baseY = screenHeight / 2 + 20

Widget[i] 位置:
  x = baseX
  y = baseY + (i * 30)  // 垂直间隔30像素
```

### 持有物品模式坐标
```java
底部Widget[0..n-2]:
  x = baseX
  y = baseY + (i * 30)

焦点Widget[n-1]:
  x = baseX
  y = baseY - 50  // 在底部上方50像素
```

## 动画系统

### 平滑过渡
- **总帧数**: 8 ticks (TOTAL_TICKS = 8)
- **插值函数**: smoothstep `t * t * (3 - 2 * t)`
- **每帧进度**: offset += 1.0 / 8

### 移动状态机
```
State.Down → State.Moving → State.Down    (moveDown)
State.Down → State.Moving → State.Focus   (moveFocus)
State.Focus → State.Moving → State.Down   (moveDown)
```

## 关键特性

1. **抽书效果**: 类似从书堆中抽出一本书，其他书自然下落
2. **状态驱动**: 根据空手/持有物品自动调整显示模式
3. **平滑动画**: 使用smoothstep插值保证平滑过渡
4. **封装性强**: 所有逻辑在ClientScrollData，LayerV2只负责渲染
5. **扩展性好**: 可轻松添加新的移动模式和状态

## 使用方式

### 切换组
```java
// 按下切换键，自动处理
ModKeyMapping.CHANG_GROUP.get().consumeClick()
```

### 手动触发切换
```java
// 设置新的组索引
SpellGroupData.instance.setCurrentGroupIndex(newIndex);
// 系统会自动触发ChangeGroupEvent，ClientScrollData监听并执行动画
```

### 更新法术列表
```java
// 触发ChangeSpellEvent
// ClientScrollData.updateHandle()会自动重新初始化
```

## 注意事项

1. **坐标计算在ClientScrollData中**: 虽然文档说在LayerV2中，但实际封装在ClientScrollData中更合理
2. **groupIndex从0开始**: 对应SpellGroupData中的索引
3. **列表末尾 = 顶层**: 渲染时最后绘制的Widget显示在最上层
4. **EMPTY Widget**: groupIndex = -1，用作占位符

## 文件清单

- `ClientScrollData.java` - 数据管理中心
- `ScrollGroupHelper.java` - 抽书/塞书逻辑
- `ScrollSpellWight.java` - 法术Widget
- `SpellSelectionLayerV2.java` - 渲染层
- `SpellGroupData.java` - 法术组数据管理（已存在）

## 测试建议

1. 测试空手切换组 - 验证抽书效果
2. 测试持有物品切换组 - 验证Focus高亮
3. 测试空手↔持有物品状态切换 - 验证状态转换
4. 测试多组法术 - 验证列表操作正确性
5. 测试动画流畅度 - 验证smoothstep插值效果

