# Datagen 使用说明

## 如何运行 Datagen

在项目根目录运行以下命令来生成 SKILL_WEAPON 标签：

```powershell
.\gradlew runData
```

## 生成的文件

运行 datagen 后，会在以下位置生成标签文件：

```
src/generated/resources/data/nomoremagicchoices/tags/item/skill_weapon.json
```

## 标签内容

生成的 `skill_weapon.json` 将包含以下物品：
- 所有剑类武器（木剑、石剑、铁剑、金剑、钻石剑、下界合金剑）
- 所有斧类武器（木斧、石斧、铁斧、金斧、钻石斧、下界合金斧）
- 三叉戟
- 弓
- 弩

## 如何修改标签

如果需要添加或删除武器，请编辑：
```
src/main/java/org/nomoremagicchoices/datagen/ModTagProvider.java
```

在 `addTags` 方法中修改 `tag(TagInit.SKILL_WEAPON)` 的内容。

## 相关文件

1. **TagInit.java** - 定义标签
2. **ModTagProvider.java** - 生成物品标签数据
3. **ModBlockTagProvider.java** - 生成方块标签数据（目前为空）
4. **ModDataGenerator.java** - 注册所有数据生成器

