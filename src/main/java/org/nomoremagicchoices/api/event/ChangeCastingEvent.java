package org.nomoremagicchoices.api.event;

import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Event fired when casting state changes.
 * Specifically fires when isCasting changes from true to false (casting ends).
 */
@Deprecated
public class ChangeCastingEvent extends Event {

    public ChangeCastingEvent() {
    }

    /**
     * Casting state monitor.
     * Monitors ClientMagicData.isCasting() and fires ChangeCastingEvent when casting ends.
     */
    public static class CastingStateMonitor {

        private static boolean lastCastingState = false;
        private static boolean initialized = false;
        /**
         * Initialize the monitor.
         */
        public static void initialize() {
            if (!initialized) {
                try {
                    lastCastingState = ClientMagicData.isCasting();
                    initialized = true;
                } catch (Exception e) {
                    initialized = false;
                }
            }
        }

        /**
         * Call this method every tick to detect casting state changes.
         * Fires ChangeCastingEvent when casting ends (isCasting: true -> false).
         *
         * @deprecated This method has been removed from {@code ClientEventHandle.clientTickEvent()}
         *             and should not be called. It serves no purpose as no code subscribes to
         *             {@link ChangeCastingEvent}. Removing this call eliminates unnecessary
         *             overhead from checking casting state every tick.
         */
        @Deprecated(since = "1.0", forRemoval = true)
        public static void tick() {
            if (!initialized) {
                initialize();
                return;
            }

            try {
                boolean currentCastingState = ClientMagicData.isCasting();

                // 只在施法结束时触发事件 (true -> false)
                if (lastCastingState && !currentCastingState) {
                    ChangeCastingEvent event = new ChangeCastingEvent();
                    NeoForge.EVENT_BUS.post(event);
                }

                lastCastingState = currentCastingState;
            } catch (Exception e) {
                initialized = false;
            }
        }
    }
}
