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
            while(key.wasPressed()){
                if (hasWeapon && SpellSelectionProvider.customGui()){
                    // 使用 SpellGroupData 获取当前组索引，确保与 ClientScrollData 同步
                    int currentGroupIndex = SpellGroupData.getCurrentGroupIndex();
                    int slotIndexInGroup = keys.indexOf(key);
                    int first = currentGroupIndex * 4;
                    int i = slotIndexInGroup + first;

                    // 发送快速施法包到服务器
                    PacketDistributor.sendToServer(new QuickCastPacket(i));
                    break;
                }
            }
        }
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
