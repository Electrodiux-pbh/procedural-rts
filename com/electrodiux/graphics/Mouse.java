package com.electrodiux.graphics;

import java.awt.event.MouseEvent;

public class Mouse {

    public static final int RELEASED = 0;
    public static final int PRESSED = 1;

    public static final int RIGHT_CLICK = MouseEvent.BUTTON1;
    public static final int LEFT_CLICK = MouseEvent.BUTTON3;
    public static final int CENTER_CLICK = MouseEvent.BUTTON2;

    private static float scrollX = 0f, scrollY = 0f;
    private static float xPos = 0f, yPos = 0f, lastX = 0f, lastY = 0f;
    private static boolean buttonBuffer[] = new boolean[3];
    private static boolean draggin = false;
    private static boolean onScreen = false;

    static void mouseButtonCallBack(int button, int action) {
        onScreen = true;
        if (button >= buttonBuffer.length || button < 0)
            return;
        if (action == Mouse.PRESSED) {
            buttonBuffer[button] = true;
        } else if (action == Mouse.RELEASED) {
            buttonBuffer[button] = false;
            draggin = false;
        }
    }

    static void mousePosCallBack(float x, float y) {
        lastX = x;
        lastY = y;
        xPos = x;
        yPos = y;
        draggin = buttonBuffer[0] || buttonBuffer[1] || buttonBuffer[2];
    }

    static void mouseScrollCallBack(float xOffset, float yOffset) {
        scrollX = xOffset;
        scrollY = yOffset;
    }

    public static void updateMouse() {
        scrollX = 0;
        scrollY = 0;
        lastX = xPos;
        lastY = yPos;
    }

    public static float getX() {
        return (float) xPos;
    }

    public static float getY() {
        return (float) yPos;
    }

    public static float getDX() {
        return (float) (lastX - xPos);
    }

    public static float getDY() {
        return (float) (lastY - yPos);
    }

    public static float getScrollX() {
        return (float) scrollX;
    }

    public static float getScrollY() {
        return (float) scrollY;
    }

    public static boolean isDraggin() {
        return draggin;
    }

    public static boolean isMouseButtonDown(int button) {
        if (button >= buttonBuffer.length || button < 0)
            return false;
        return buttonBuffer[button];
    }

    public static boolean isMouseOnScreen() {
        return onScreen;
    }

}
