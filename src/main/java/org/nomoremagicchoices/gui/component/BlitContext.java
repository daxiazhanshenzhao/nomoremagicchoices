package org.nomoremagicchoices.gui.component;

public record BlitContext(boolean isMouse, int uOffset, int vOffset, int width, int height){

    public static BlitContext of(int uOffset, int vOffset, int width, int height) {

        return new BlitContext(false, uOffset, vOffset, width, height);
    }
}
