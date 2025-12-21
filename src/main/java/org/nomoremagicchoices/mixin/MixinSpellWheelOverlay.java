package org.nomoremagicchoices.mixin;


import io.redspace.ironsspellbooks.gui.overlays.SpellWheelOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SpellWheelOverlay.class)
public class MixinSpellWheelOverlay{

    @Shadow
    private int wheelSelection;


}
