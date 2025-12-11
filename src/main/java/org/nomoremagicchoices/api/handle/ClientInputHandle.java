package org.nomoremagicchoices.api.handle;

import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.network.casting.CancelCastPacket;
import io.redspace.ironsspellbooks.network.casting.QuickCastPacket;
import io.redspace.ironsspellbooks.player.ClientMagicData;

import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.nomoremagicchoices.api.init.TagInit;
import org.nomoremagicchoices.api.selection.ClientData;
import org.nomoremagicchoices.api.selection.ScrollWightData;
import org.nomoremagicchoices.api.selection.SpellGroupData;
import org.nomoremagicchoices.gui.SpellSelectionLayerV1;
import org.nomoremagicchoices.player.KeyState;
import org.nomoremagicchoices.player.ModKeyMapping;

import java.util.List;

@EventBusSubscriber
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
         handleGroup();

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
    public static void rightClick(PlayerInteractEvent.RightClickItem event){
        if (ClientMagicData.isCasting()) {
            PacketDistributor.sendToServer(new CancelCastPacket(true));
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        handlePlayerHand();
    }

    // 已移至 ClientScrollData.handleRunning() 处理，避免重复消耗按键事件
    public static void handleGroup() {
        if (ScrollWightData.isTicking()) return;

        if (ModKeyMapping.CHANG_GROUP.get().consumeClick()){
            SpellGroupData.move(1);
        }
        if (ModKeyMapping.NEXT_GROUP.get().consumeClick()){
            SpellGroupData.add();
        }
        if (ModKeyMapping.PREV_GROUP.get().consumeClick()){
            SpellGroupData.less();
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

        for (KeyState key : keys){
            if (key.wasPressed() && hasWeapon){
                // 使用 SpellGroupData 获取当前组索引，确保与 ClientScrollData 同步
                int currentGroupIndex = SpellGroupData.getCurrentGroupIndex();
                int slotIndexInGroup = keys.indexOf(key);
                int i = slotIndexInGroup + currentGroupIndex * 4;

                SpellSelectionManager spellSelectionManager = ClientMagicData.getSpellSelectionManager();
                if (!ClientMagicData.isCasting()&& (ClientMagicData.getCooldownPercent(ClientMagicData.getSpellSelectionManager().getSpellData(i).getSpell()) <=0)  ) {
                    spellSelectionManager.makeSelection(i);
                }
                PacketDistributor.sendToServer(new QuickCastPacket(i));
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
