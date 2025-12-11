package org.nomoremagicchoices.api.selection;

public enum SpellSelectionState {
    EmptyHand(0),
    Weapon(1),
    Staff(2),
    Spellbook(3);

    SpellSelectionState(final int value) {
        this.value = value;
    }

    final int value;

    public int getValue() {
        return value;
    }

    public boolean isFocus(){
        return (this == Weapon) || (this == Staff);
    }
}
