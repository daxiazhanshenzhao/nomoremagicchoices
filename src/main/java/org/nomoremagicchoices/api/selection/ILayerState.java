package org.nomoremagicchoices.api.selection;

import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public interface ILayerState extends IGuiOverlay {

    void renderBg(ResourceLocation texture, GuiGraphics context, int x, int y,int uOffset,int vOffset, int width, int height,int textureWidth,int textureHeight);

    void render1(GuiGraphics guiGraphics, float deltaTracker);
}
