package org.nomoremagicchoices.mixin;

import io.redspace.ironsspellbooks.item.CastingItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import org.nomoremagicchoices.api.init.TagInit;
import org.nomoremagicchoices.api.handle.ClientInputHandle;
import org.nomoremagicchoices.player.ModKeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(value = Minecraft.class, priority = 999)
public class MixinSkillCombat {

    /**
     * 拦截原版的 this.player.getInventory().selected = i; 操作
     * 通过返回 false 来阻止原版的物品栏切换逻辑
     * @param instance 玩家物品栏实例
     * @param index 要切换到的物品栏索引 (0-8)
     */
    @Redirect(
        method = "handleKeybinds",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/entity/player/Inventory;selected:I",
            ordinal = 0,
            opcode = 181 // PUTFIELD opcode
        )
    )
    private void redirectInventorySelected(Inventory instance, int index) {
        var mc = Minecraft.getInstance();
        if (mc.player != null) {
            // 统一武器检查逻辑，与ClientInputHandle保持一致
            boolean hasSkillWeaponTag = mc.player.getMainHandItem().is(TagInit.SKILL_WEAPON);
            boolean hasStaff = false;
            if (mc.player.getMainHandItem().getItem() instanceof CastingItem || mc.player.getOffhandItem().getItem() instanceof CastingItem) {
                hasStaff = true;
            }

            boolean hasWeapon = hasSkillWeaponTag || hasStaff;

            // 更新ClientInputHandle的状态
            ClientInputHandle.setHasWeapon(hasWeapon);

            // 只有当技能键绑定到数字键时才拦截物品栏切换
            boolean shouldIntercept = hasWeapon && ModKeyMapping.isAnySkillKeyBoundToNumber();

            if (!shouldIntercept) {
                // 允许原版操作：执行字段赋值
                instance.selected = index;
            }
            // 如果shouldIntercept为true，则不执行赋值操作，从而阻止物品栏切换
        } else {
            // 如果玩家为null，执行原版操作
            instance.selected = index;
        }
    }

    // 拦截原版右键
    // 拦截原版q，f，c键
}
