package org.nomoremagicchoices.gui.component;

public class KeyHp {

    public static BlitContext getContext(int keyCode) {
        return switch (keyCode) {
            // Mouse buttons (GLFW codes: 0-6) - 使用纹理渲染
            case 0 -> new BlitContext(true, 0, 0, 9, 12);       // Left Click
            case 1 -> new BlitContext(true, 16, 0, 9, 12);      // Right Click
            case 2 -> new BlitContext(true, 32, 0, 9, 12);      // Middle Click
            case 3 -> new BlitContext(true, 0, 16, 10, 12);     // Side Button 1 (Back)
            case 4 -> new BlitContext(true, 16, 16, 10, 12);    // Side Button 2 (Forward)
            case 5 -> new BlitContext(true, 32, 16, 9, 12);     // Extra Button 1
            case 6 -> new BlitContext(true, 48, 16, 9, 12);     // Extra Button 2

            // Keyboard keys - 使用文字渲染 (设置width>0以通过检查)
            default -> new BlitContext(false, 0, 0, 22, 16);
        };
    }
}
