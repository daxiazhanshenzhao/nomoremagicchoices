package org.nomoremagicchoices.mixin;


import com.llamalad7.mixinextras.injector.WrapWithCondition;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import org.nomoremagicchoices.Nomoremagicchoices;
import org.nomoremagicchoices.api.init.TagInit;
import org.nomoremagicchoices.api.handle.ClientInputHandle;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Minecraft.class,priority = 999)
public class MixinSkillCombat {

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
        // 先更新武器状态，确保ClientInputHandle有最新的武器信息
        var mc = Minecraft.getInstance();
        if (mc.player != null) {
            boolean hasSkillWeaponTag = mc.player.getMainHandItem().is(TagInit.SKILL_WEAPON);
            boolean hasStaff = mc.player.getMainHandItem().has(ComponentRegistry.CASTING_IMPLEMENT) 
                               || mc.player.getOffhandItem().has(ComponentRegistry.CASTING_IMPLEMENT);
            ClientInputHandle.setHasWeapon(hasSkillWeaponTag || hasStaff);
        }

        // 调用统一的逻辑判断方法
        boolean shouldIntercept = ClientInputHandle.handleNumberKey();

        if (shouldIntercept) {
            Nomoremagicchoices.LOGGER.info("拦截了物品栏切换，索引: " + index);
        }

        // 返回false表示拦截（不执行原版物品栏切换），返回true表示允许原版逻辑
        return !shouldIntercept;
    }



    //拦截原版右键
    //拦截原版q，f，c键

}
