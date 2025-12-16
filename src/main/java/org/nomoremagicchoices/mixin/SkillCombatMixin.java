package org.nomoremagicchoices.mixin;


import com.llamalad7.mixinextras.injector.WrapWithCondition;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.nomoremagicchoices.api.init.TagInit;
import org.nomoremagicchoices.api.handle.ClientInputHandle;

import org.nomoremagicchoices.api.selection.ClientData;
import org.nomoremagicchoices.api.selection.ClientHandData;
import org.nomoremagicchoices.api.selection.ScrollWightData;
import org.nomoremagicchoices.player.ModKeyMapping;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Minecraft.class,priority = 999)
@OnlyIn(Dist.CLIENT)
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
            // 统一武器检查逻辑，与ClientInputHandle保持一致
            boolean hasSkillWeaponTag = mc.player.getMainHandItem().is(TagInit.SKILL_WEAPON);
            boolean hasStaff = mc.player.getMainHandItem().has(ComponentRegistry.CASTING_IMPLEMENT) 
                               || mc.player.getOffhandItem().has(ComponentRegistry.CASTING_IMPLEMENT);
            boolean hasWeapon = hasSkillWeaponTag || hasStaff;
            
            // 更新ClientInputHandle的状态
            ClientInputHandle.setHasWeapon(hasWeapon);

            // 只有当技能键绑定到数字键时才拦截物品栏切换
            boolean shouldIntercept = hasWeapon && ModKeyMapping.isAnySkillKeyBoundToNumber();
            
//             调试信息（可以注释掉）
//             if (hasWeapon) {
//                 System.out.println("SkillCombatMixin: 持有武器/法杖，技能键绑定到数字键: " + ModKeyMapping.isAnySkillKeyBoundToNumber());
//                 System.out.println("SkillCombatMixin: 是否拦截数字键切换物品栏: " + shouldIntercept);
//                 System.out.println("SkillCombatMixin: 目标物品栏索引: " + index);
//             }
            
            return !shouldIntercept;
        }

        return true;
    }


    //拦截原版右键
    //拦截原版q，f，c键

}
