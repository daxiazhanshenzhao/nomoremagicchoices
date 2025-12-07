package org.nomoremagicchoices.api.selection;

import net.minecraft.client.gui.LayeredDraw;

public interface ILayerState extends LayeredDraw.Layer {

    SpellSelectionState getSpellSelectionState();
    void setSpellSelectionState(SpellSelectionState spellSelectionState);
}
