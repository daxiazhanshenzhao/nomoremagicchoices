package org.nomoremagicchoices.api.handle;

import io.redspace.ironsspellbooks.network.casting.CancelCastPacket;
import io.redspace.ironsspellbooks.network.casting.QuickCastPacket;
import io.redspace.ironsspellbooks.player.ClientMagicData;

import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.nomoremagicchoices.Nomoremagicchoices;
import org.nomoremagicchoices.api.init.TagInit;
import org.nomoremagicchoices.api.selection.SpellGroupData;
import org.nomoremagicchoices.gui.SpellSelectionProvider;
import org.nomoremagicchoices.player.KeyState;
import org.nomoremagicchoices.player.ModKeyMapping;

import java.util.List;

@EventBusSubscriber(Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class ClientInputHandle {

    private static final KeyState SKILL_1 = getKeyState(ModKeyMapping.SKILL_1.get());
    private static final KeyState SKILL_2 = getKeyState(ModKeyMapping.SKILL_2.get());
    private static final KeyState SKILL_3 = getKeyState(ModKeyMapping.SKILL_3.get());
    private static final KeyState SKILL_4 = getKeyState(ModKeyMapping.SKILL_4.get());

    private static boolean hasWeapon = false;

    private static final List<KeyState> keys = List.of(
            SKILL_1,
            SKILL_2,
            SKILL_3,
            SKILL_4
    );

    /**
     * 处理滚轮事件
     * 只在持有技能武器/法杖且正在施法时取消滚轮切换物品栏
     * 这样可以防止施法期间误操作切换物品导致施法中断
     * 同时允许其他情况下（如空手、持有普通物品）正常使用滚轮
     */
    @SubscribeEvent
    public static void scrollEvent(InputEvent.MouseScrollingEvent event){

        if (true) return;
        // 只在施法期间取消滚轮输入，防止切换物品栏导致施法中断
        if (ClientMagicData.isCasting()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void rightClick(PlayerInteractEvent.RightClickItem event){
        if (ClientMagicData.isCasting()) {
            PacketDistributor.sendToServer(new CancelCastPacket(true));
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        handlePlayerHand();
        handleGroup();
        handleSkill(); // 在 ClientTick 中处理技能按键，适配鼠标操作
    }



    // 已移至 ClientScrollData.handleRunning() 处理，避免重复消耗按键事件
    public static void handleGroup() {
        // 移除isTicking检查，允许在动画期间切换组
        // 这样可以确保玩家随时可以通过法术轮盘切换法术
        if (!SpellSelectionProvider.customGui()) return;

        while (ModKeyMapping.NEXT_GROUP.get().consumeClick()){
            SpellGroupData.add();
        }
        while (ModKeyMapping.PREV_GROUP.get().consumeClick()){
            SpellGroupData.less();
        }
        while (ModKeyMapping.CHANG_GROUP.get().consumeClick()){
            SpellGroupData.add();
        }
    }

    public static void handlePlayerHand(){
        var mc = Minecraft.getInstance();
        if (mc.player != null) {
            var mainHand = mc.player.getMainHandItem();
            var offHand = mc.player.getOffhandItem();

            boolean hasSkillWeaponTag = mainHand.is(TagInit.SKILL_WEAPON);
            boolean hasStaff = mainHand.has(ComponentRegistry.CASTING_IMPLEMENT)
                            || offHand.has(ComponentRegistry.CASTING_IMPLEMENT);

            hasWeapon = hasSkillWeaponTag || hasStaff;
        }
    }

    public static void handleSkill(){
        // 先更新所有按键状态
        update();

        for (KeyState key : keys){
            while(key.getKey().consumeClick()){
                if (hasWeapon && SpellSelectionProvider.customGui() && !key.isNumberKey()){
                    // 使用 SpellGroupData 获取当前组索引，确保与 ClientScrollData 同步
                    int currentGroupIndex = SpellGroupData.getCurrentGroupIndex();
                    int slotIndexInGroup = keys.indexOf(key);
                    int first = currentGroupIndex * 4;
                    int i = slotIndexInGroup + first;
                    Nomoremagicchoices.LOGGER.info("触发了1次");
                    // 发送快速施法包到服务器
                    PacketDistributor.sendToServer(new QuickCastPacket(i));
                    break;
                }
            }
        }
    }

    /**
     * 检查是否应该拦截数字键切换物品栏，并处理数字键技能释放
     * @return true 表示应该拦截物品栏切换（允许技能释放），false 表示允许原版物品栏切换
     */
    public static boolean handleNumberKey(){
        var mc = Minecraft.getInstance();
        if (mc.player == null) {
            return false;
        }

        // 只有在持有武器、自定义GUI启用、且技能键绑定到数字键时才处理
        if (!hasWeapon || !SpellSelectionProvider.customGui() || !ModKeyMapping.isAnySkillKeyBoundToNumber()) {
            return false;
        }

        // 更新按键状态
        update();

        // 遍历所有技能按键，处理数字键的技能释放
        boolean hasNumberKeyPressed = false;
        for (KeyState key : keys){
            // 只处理绑定到数字键的技能
            if (key.isNumberKey()) {
                // 检查是否有按键事件（不消耗，只检查）
                if (key.getKey().isDown()) {
                    hasNumberKeyPressed = true;
                }

                // 消耗按键事件并释放技能
                while(key.getKey().consumeClick()){
                    int currentGroupIndex = SpellGroupData.getCurrentGroupIndex();
                    int slotIndexInGroup = keys.indexOf(key);
                    int first = currentGroupIndex * 4;
                    int i = slotIndexInGroup + first;
                    Nomoremagicchoices.LOGGER.info("数字键触发技能: " + (slotIndexInGroup + 1));
                    // 发送快速施法包到服务器
                    PacketDistributor.sendToServer(new QuickCastPacket(i));
                }
            }
        }

        // 如果有任何数字键被按下，就拦截物品栏切换
        return hasNumberKeyPressed;
    }


    public static KeyState getKeyState(KeyMapping key){
        return new KeyState(key);
    }

    public static void update(){
        for (KeyState key : keys){
            key.update();
        }
    }

    public static boolean getHasWeapon() {
        return hasWeapon;
    }

    public static void setHasWeapon(boolean hasWeapon) {
        ClientInputHandle.hasWeapon = hasWeapon;
    }


}
