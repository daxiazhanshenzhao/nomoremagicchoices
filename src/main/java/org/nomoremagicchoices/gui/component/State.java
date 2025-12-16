package org.nomoremagicchoices.gui.component;


/**
 * 法术图标的显示状态枚举
 * <p>定义了法术图标在不同状态下的渲染尺寸和位置：
 * <ul>
 * <li>{@link State#Down} - 法术图标位于底部位置，渲染尺寸：20×20像素
 * <li>{@link State#Moving} - 法术图标处于移动过渡状态，从底部移动到顶部（或反向）
 * <li>{@link State#Focus} - 法术图标位于顶部焦点位置，渲染尺寸：22×22像素
 * </ul>
 */
public enum State{
    Down(0),
    Moving(1),
    Focus(2);

    State(final int value) {
        this.value = value;
    }
    private final int value;
    public int getValue() {
        return value;
    }


}
