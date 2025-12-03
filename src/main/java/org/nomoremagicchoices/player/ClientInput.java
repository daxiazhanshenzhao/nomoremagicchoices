package org.nomoremagicchoices.player;

import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.network.casting.QuickCastPacket;
import io.redspace.ironsspellbooks.player.ClientMagicData;

import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.nomoremagicchoices.Nomoremagicchoices;
import org.nomoremagicchoices.gui.SpellSelectionLayerV1;

import java.util.List;

@EventBusSubscriber
public class ClientInput {

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
        Nomoremagicchoices.LOGGER.info("haha");

        handleSkill();


    }

    public static void handleSkill(){

        for (KeyState key : keys){
            if (key.wasPressed() && hasWeapon){
                int i = keys.indexOf(key) + SpellSelectionLayerV1.getCurrentGroup()*4;

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

    public static void setHasWeapon(boolean hasWeapon) {
        ClientInput.hasWeapon = hasWeapon;
    }
}
