package org.nomoremagicchoices.mixin;


import com.llamalad7.mixinextras.injector.WrapWithCondition;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import org.nomoremagicchoices.api.init.TagInit;
import org.nomoremagicchoices.api.handle.ClientInputHandle;

import org.nomoremagicchoices.api.selection.ClientData;
import org.nomoremagicchoices.api.selection.ScrollWightData;
import org.nomoremagicchoices.player.ModKeyMapping;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Minecraft.class,priority = 999)
public class SkillCombatMixin {

    /**
     * 拦截原版的 this.player.getInventory().selected = i; 操作
     * 通过返回 false 来阻止原版的物品栏切换逻辑
     * @param instance 玩家物品栏实例
     * @param index 要切换到的物品栏索引 (0-8)
     * @return false 表示取消原版的赋值操作，true 表示允许原版操作
     */
    @SuppressWarnings("deprecation")
    @WrapWithCondition(method = "handleKeybinds", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Inventory;selected:I", ordinal = 0, opcode = Opcodes.PUTFIELD))
    private boolean handleInput(Inventory instance, int index){
        var mc = Minecraft.getInstance();
        if (mc.player != null) {

            boolean hasSkillWeaponTag = mc.player.getMainHandItem().is(TagInit.SKILL_WEAPON) || mc.player.getMainHandItem().has(ComponentRegistry.CASTING_IMPLEMENT);
            if (hasSkillWeaponTag) {
                ClientInputHandle.setHasWeapon(true);
            } else {
                ClientInputHandle.setHasWeapon(false);
            }

            // 只有当技能键绑定到数字键时才拦截物品栏切换
            boolean shouldIntercept = hasSkillWeaponTag && ModKeyMapping.isAnySkillKeyBoundToNumber() && !ClientData.getClientHandData().getState().isFocus();
            return !shouldIntercept;
        }


        return true;
    }


    //拦截原版右键
    //拦截原版q，f，c键

}
