package org.nomoremagicchoices.api.selection;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;

public interface ILayerState extends LayeredDraw.Layer {

    void renderBg(ResourceLocation texture, GuiGraphics context, int x, int y,int uOffset,int vOffset, int width, int height,int textureWidth,int textureHeight);
}
