package org.nomoremagicchoices.api.event;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class RenderBgEvent extends Event implements ICancellableEvent {


    private ResourceLocation texture;
    private int x, y, width, height,uOffset,vOffset,textureWidth,textureHeight;

    public RenderBgEvent(ResourceLocation texture, GuiGraphics context, int x, int y, int uOffset, int vOffset, int width, int height,int textureWidth,int textureHeight) {
        this.texture = texture;
        this.x = x;
        this.y = y;
        this.uOffset = uOffset;
        this.vOffset = vOffset;
        this.width = width;
        this.height = height;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }


    public ResourceLocation getTexture() {
        return texture;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setTexture(ResourceLocation texture) {
        this.texture = texture;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getuOffset() {
        return uOffset;
    }

    public int getvOffset() {
        return vOffset;
    }

    public void setuOffset(int uOffset) {
        this.uOffset = uOffset;
    }

    public void setvOffset(int vOffset) {
        this.vOffset = vOffset;
    }

    public int getTextureHeight() {
        return textureHeight;
    }

    public int getTextureWidth() {
        return textureWidth;
    }

    public void setTextureHeight(int textureHeight) {
        this.textureHeight = textureHeight;
    }

    public void setTextureWidth(int textureWidth) {
        this.textureWidth = textureWidth;
    }
}
