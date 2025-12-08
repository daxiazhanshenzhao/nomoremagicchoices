package org.nomoremagicchoices.gui.component;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Vector2f;
import org.joml.Vector2i;

public interface IMoveWight {

    void moveTo(Vector2i ender);
    void render(GuiGraphics context, DeltaTracker partialTick);
    void tick();
}
