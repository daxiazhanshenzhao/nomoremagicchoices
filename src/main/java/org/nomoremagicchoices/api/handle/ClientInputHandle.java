package org.nomoremagicchoices.api.handle;


import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.item.CastingItem;
import io.redspace.ironsspellbooks.network.ServerboundQuickCast;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.setup.Messages;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.nomoremagicchoices.api.init.TagInit;
import org.nomoremagicchoices.api.selection.ClientData;
import org.nomoremagicchoices.api.selection.SpellGroupData;
import org.nomoremagicchoices.player.KeyState;
import org.nomoremagicchoices.player.ModKeyMapping;

import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
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

    @SubscribeEvent
    public static void onClientClick(InputEvent.Key event){
        handleSkill();


    }

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
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        handlePlayerHand();
        handleGroup();
    }



    // 已移至 ClientScrollData.handleRunning() 处理，避免重复消耗按键事件
    public static void handleGroup() {
        // 移除isTicking检查，允许在动画期间切换组
        // 这样可以确保玩家随时可以通过法术轮盘切换法术
        
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
            boolean hasStaff = false;
            if (mainHand.getItem() instanceof CastingItem || offHand.getItem() instanceof CastingItem) {
                hasStaff = true;
            }




            hasWeapon = hasSkillWeaponTag || hasStaff;
        }
    }

    public static void handleSkill(){
        for (KeyState key : keys){
            if (key.wasPressed() && hasWeapon){
                // 使用 SpellGroupData 获取当前组索引，确保与 ClientScrollData 同步
                int currentGroupIndex = SpellGroupData.getCurrentGroupIndex();
                int slotIndexInGroup = keys.indexOf(key);
                int first = currentGroupIndex * 4;
                int i = slotIndexInGroup + first;

                // 无论是否正在施法或法术是否冷却，都更新本地选择索引
                // 这样UI才能正确显示当前选择的法术
                SpellSelectionManager spellSelectionManager = ClientMagicData.getSpellSelectionManager();
                if (spellSelectionManager != null) {
                    // 更新本地选择索引，确保UI同步
                    spellSelectionManager.makeSelection(i);
                }
                
                // 发送快速施法包到服务器

                Messages.sendToServer(new ServerboundQuickCast(i));
                break;
            }
        }

        update();
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
