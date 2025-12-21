package org.nomoremagicchoices.api.event;



import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import org.nomoremagicchoices.api.selection.SpellSelectionState;

public class ChangeHandEvent extends Event{

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

    @Override
    public boolean isCancelable() {
        return true;
    }
}
