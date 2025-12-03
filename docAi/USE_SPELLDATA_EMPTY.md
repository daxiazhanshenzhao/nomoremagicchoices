# 使用 SpellData.EMPTY 判定空槽位

## ✅ 修改完成

已将所有空槽位判定从 `spellData.getSpell() != null` 改为使用 `SpellData.EMPTY`。

## 📝 修改位置

### 1. **init() 方法 - 过滤空槽位**
```java
// 修改前：
.filter(spellData -> spellData != null && spellData.getSpell() != null)

// 修改后：
.filter(spellData -> spellData != null && !spellData.equals(SpellData.EMPTY))
```

### 2. **calculateActualIndex() 方法 - 计算实际索引**
```java
// 修改前：
if (slot != null && slot.spellData != null && slot.spellData.getSpell() != null)

// 修改后：
if (slot != null && slot.spellData != null && !slot.spellData.equals(SpellData.EMPTY))
```

### 3. **updateCurrentSpells() 方法 - 更新当前组法术**
```java
// 修改前：
if (spellData != null && spellData.getSpell() != null)

// 修改后：
if (spellData != null && !spellData.equals(SpellData.EMPTY))
```

## 🎯 优势

使用 `SpellData.EMPTY` 比检查 `getSpell() != null` 更准确，因为：

1. ✅ **语义清晰** - `SpellData.EMPTY` 是官方定义的空槽位标识
2. ✅ **更可靠** - 不依赖于 spell 对象是否为 null
3. ✅ **性能更好** - 直接对象比较，不需要调用方法
4. ✅ **符合 API 设计** - 使用库提供的标准判定方式

## 📊 示例

```java
// 槽位状态：
[Sp0] [EMPTY] [Sp2] [EMPTY] [Sp4]

// 过滤后（使用 SpellData.EMPTY）：
[Sp0] [Sp2] [Sp4]  ✅ 正确过滤
```

修改完成！现在代码使用标准的 `SpellData.EMPTY` 来判定空槽位了。🎉

