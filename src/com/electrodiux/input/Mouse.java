package com.electrodiux.input;

import java.util.Arrays;

import org.lwjgl.glfw.GLFW;

import com.electrodiux.graphics.Window;

public final class Mouse extends GLFW {

	// public static final int CURSOR_ARROW = ImGuiMouseCursor.Arrow;
	// public static final int CURSOR_TEXT_INPUT = ImGuiMouseCursor.TextInput;
	// public static final int CURSOR_RESIZE_ALL = ImGuiMouseCursor.ResizeAll;
	// public static final int CURSOR_RESIZE_NS = ImGuiMouseCursor.ResizeNS;
	// public static final int CURSOR_RESIZE_EW = ImGuiMouseCursor.ResizeEW;
	// public static final int CURSOR_RESIZE_NESW = ImGuiMouseCursor.ResizeNESW;
	// public static final int CURSOR_RESIZE_NWSE = ImGuiMouseCursor.ResizeNWSE;
	// public static final int CURSOR_HAND = ImGuiMouseCursor.Hand;
	// public static final int CURSOR_NOT_ALLOWED = ImGuiMouseCursor.NotAllowed;
	// public static final int CURSOR_GRAB = 9;
	// public static final int CURSOR_GRABBING = 10;

	public static final int AMMOUNT_OF_CURSORS = 11;

	private static double scrollX, scrollY;
	private static double xPos, yPos, lastX, lastY;
	private static boolean buttonBuffer[] = new boolean[3];
	private static boolean draggin;
	private static boolean onScreen;
	private static boolean clean;

	private Mouse() {
		setValuesAsDefault();
	}

	private void setValuesAsDefault() {
		scrollX = 0.0f;
		scrollY = 0.0f;
		xPos = 0.0f;
		yPos = 0.0f;
		lastX = 0.0f;
		lastX = 0.0f;
	}

	public static void configureMouse(Window window) {
		GLFW.glfwSetCursorPosCallback(window.getWindowID(), Mouse::mousePosCallBack);
		GLFW.glfwSetMouseButtonCallback(window.getWindowID(), Mouse::mouseButtonCallBack);
		GLFW.glfwSetScrollCallback(window.getWindowID(), Mouse::mouseScrollCallBack);
	}

	private static void mousePosCallBack(long window, double xPos, double yPos) {
		lastX = Mouse.xPos;
		lastY = Mouse.yPos;
		Mouse.xPos = xPos;
		Mouse.yPos = yPos;
		draggin = buttonBuffer[0] || buttonBuffer[1] || buttonBuffer[2];
	}

	private static void mouseScrollCallBack(long window, double xOffset, double yOffset) {
		scrollX = xOffset;
		scrollY = yOffset;
	}

	private static void mouseButtonCallBack(long window, int button, int action, int mods) {
		onScreen = true;
		if (button >= buttonBuffer.length || button < 0)
			return;
		if (action == GLFW.GLFW_PRESS) {
			buttonBuffer[button] = true;
			clean = false;
		} else if (action == GLFW.GLFW_RELEASE) {
			buttonBuffer[button] = false;
			draggin = false;
		}
	}

	public static void clear() {
		if (!clean) {
			Arrays.fill(buttonBuffer, false);
			draggin = false;
			clean = true;
			scrollX = 0;
			scrollY = 0;
			onScreen = false;
		}
	}

	public static void endFrame() {
		scrollX = 0;
		scrollY = 0;
		lastX = xPos;
		lastY = yPos;
	}

	public static void setMouseOnScreen(boolean onScreen) {
		Mouse.onScreen = onScreen;
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

	// public static void setCursor(int cursorID) {
	// if (cursorID >= 0 && cursorID < Mouse.AMMOUNT_OF_CURSORS)
	// ImGui.setMouseCursor(cursorID);
	// }

}
