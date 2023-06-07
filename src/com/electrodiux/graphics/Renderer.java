package com.electrodiux.graphics;

import java.io.IOException;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import com.electrodiux.input.Keyboard;
import com.electrodiux.input.Mouse;
import com.electrodiux.world.World;

public class Renderer {

    private Window window;

    private Camera camera;
    private Shader shader;

    private Matrix4f transformMatrix;

    public void run() {
        window = new Window(640, 360, "Multiplayer Physics");

        GL.createCapabilities();

        Keyboard.configureKeyboard(window);
        GLFW.glfwSetInputMode(window.getWindowID(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
        Mouse.configureMouse(window);

        load();
        transformMatrix = new Matrix4f();

        GL11.glCullFace(GL11.GL_BACK);

        double lastTime = GLFW.glfwGetTime();

        while (!GLFW.glfwWindowShouldClose(window.getWindowID())) {
            // calc deltatime
            double currentTime = GLFW.glfwGetTime();
            float deltaTime = (float) (currentTime - lastTime);
            lastTime = currentTime;

            // update and render
            GLFW.glfwPollEvents();
            update(deltaTime);
            render();

            // swapbuffers
            GLFW.glfwSwapBuffers(window.getWindowID());

            Mouse.endFrame();
        }

        Loader.clear();

        System.exit(0);
    }

    private World world;

    private void render() {
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        camera.clearColor();

        camera.setProjectionsToShader(shader);

        if (world != null) {
            renderWorld(world);
        }
    }

    private void renderWorld(World world) {

    }

    private void update(float deltaTime) {

    }

    private float cameraDistance = 0.0f;
    private Model flatTexture;

    private void load() {
        camera = new Camera();
        camera.setAspectRatio(window.getWidth(), window.getHeight());
        camera.getBackgroundColor().set(Color.LIGHT_BLUE);
        camera.setzFar(400f);
        cameraDistance = 10f;

        try {
            shader = Shader.loadShader("/assets/shaders/lightning.glsl");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
