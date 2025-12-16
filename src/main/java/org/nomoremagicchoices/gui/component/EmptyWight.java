package org.nomoremagicchoices.gui.component;

import io.redspace.ironsspellbooks.api.spells.SpellData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2i;

import java.util.List;

public class EmptyWight extends AbstractWight {
    public EmptyWight() {
        super(new WightContext(new Vector2i(0,0),State.Down),List.of(SpellData.EMPTY),8);
    }

    public static final AbstractWight EMPTY = new EmptyWight();


    @Override
    public void render(GuiGraphics context, DeltaTracker partialTick) {

    }

    @Override
    double getRealOffset(double interpolatedOffset) {
        return 0;
    }

    @Override
    void renderSlot(GuiGraphics context, SpellData spell, int x, int y) {

    }

    @Override
    ResourceLocation getTexture() {
        return null;
    }
}
