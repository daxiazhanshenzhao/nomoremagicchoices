package org.nomoremagicchoices;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class DrawableInfo {

        public enum Anchor { LEADING, TRAILING, CENTER }
        public record Component(DrawRect draw, Texture texture) {
            public void draw(GuiGraphics context, int x, int y, Anchor hAnchor, Anchor vAnchor) {
                switch (hAnchor) {
                    case LEADING -> {
                        x = x;
                    }
                    case CENTER -> {
                        x -= draw().width / 2;
                    }
                    case TRAILING -> {
                        x -= draw().width;
                    }
                }
                switch (vAnchor) {
                    case LEADING -> {
                        y = y;
                    }
                    case CENTER -> {
                        y -= draw().height / 2;
                    }
                    case TRAILING -> {
                        y -= draw().height;
                    }
                }
                context.blit(texture().id, x, y, draw().u, draw().v, draw().width, draw().height, texture().width, texture().height);
            }

            public void drawFlexibleWidth(GuiGraphics context, int x, int y, int width, Anchor vAnchor) {
                switch (vAnchor) {
                    case LEADING -> {
                        y = y;
                    }
                    case CENTER -> {
                        y -= draw().height / 2;
                    }
                    case TRAILING -> {
                        y -= draw().height;
                    }
                }
                context.blit(texture().id, x, y, draw().u, draw().v, width, draw().height, texture().width, texture().height);
            }
        }
        public record DrawRect(int u, int v, int width, int height) {}
        public record Texture(ResourceLocation id, int width, int height) {}

}