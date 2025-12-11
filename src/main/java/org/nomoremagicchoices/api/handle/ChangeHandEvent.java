package org.nomoremagicchoices.api.handle;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.nomoremagicchoices.api.selection.SpellSelectionState;

public class ChangeHandEvent extends Event implements ICancellableEvent {

    private final SpellSelectionState oldState;
    private SpellSelectionState newState;

    public ChangeHandEvent(SpellSelectionState oldState, SpellSelectionState newState) {
        this.oldState = oldState;
        this.newState = newState;
    }

    public SpellSelectionState getNewState() {
        return newState;
    }

    public SpellSelectionState getOldState() {
        return oldState;
    }

    public void setNewState(SpellSelectionState newState) {
        this.newState = newState;
    }
}
