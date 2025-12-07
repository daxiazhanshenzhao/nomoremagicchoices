package org.nomoremagicchoices.api.event;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.nomoremagicchoices.api.selection.SpellGroupData;

public class ChangeGroupEvent extends Event implements ICancellableEvent {

    private final int oldGroup;
    private int newGroup;
    private SpellGroupData spellGroupData;

    public ChangeGroupEvent(SpellGroupData data, int oldGroup, int newGroup) {
        this.oldGroup = oldGroup;
        this.newGroup = newGroup;
        this.spellGroupData = data;
    }

    public int getNewGroup() {
        return newGroup;
    }

    public int getOldGroup() {
        return oldGroup;
    }

    public SpellGroupData getSpellGroupData() {
        return spellGroupData;
    }

    public void setNewGroup(int newGroup) {
        this.newGroup = newGroup;
    }
}
