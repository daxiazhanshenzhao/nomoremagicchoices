package org.nomoremagicchoices;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.slf4j.Logger;

@EventBusSubscriber
public class Debug {
    public static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onDebugEvent(LivingEvent.LivingJumpEvent event){
        if (event.getEntity() instanceof Player player){
            SpellKeyVisuals.getSpellKey();
        }

    }
}
