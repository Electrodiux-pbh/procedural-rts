package com.electrodiux.input;

import org.lwjgl.glfw.GLFW;

import com.electrodiux.graphics.Window;

public final class Keyboard extends GLFW {

	private static boolean keyPressed[] = new boolean[360];
	private static boolean keyTyped[] = new boolean[360];

	private static boolean clear;

	private Keyboard() {
		clear = true;
	}

	public static void configureKeyboard(Window window) {
		GLFW.glfwSetKeyCallback(window.getWindowID(), Keyboard::keyCallBack);
	}

	private static void keyCallBack(long window, int key, int scancode, int action, int mods) {
		if (key >= keyPressed.length || key < 0)
			return;

		if (action == GLFW.GLFW_PRESS) {
			keyPressed[key] = true;
			keyTyped[key] = true;
			clear = false;
		} else if (action == GLFW.GLFW_RELEASE) {
			keyPressed[key] = false;
		}
	}

	public static void endFrame() {
		for (int i = 0; i < keyTyped.length; i++) {
			keyTyped[i] = false;
		}
	}

	public static void clear() {
		if (!clear) {
			for (int i = 0; i < keyPressed.length; i++) {
				keyPressed[i] = false;
			}
			for (int i = 0; i < keyTyped.length; i++) {
				keyTyped[i] = false;
			}
			clear = true;
		}
	}

	public static boolean isKeyPressed(int key) {
		if (key >= keyPressed.length || key < 0)
			return false;
		return keyPressed[key];
	}

	public static boolean isKeyTyped(int key) {
		if (key >= keyTyped.length || key < 0)
			return false;
		return keyTyped[key];
	}

}
