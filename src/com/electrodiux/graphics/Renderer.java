package com.electrodiux.graphics;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.electrodiux.block.BlockRegister;
import com.electrodiux.input.Keyboard;
import com.electrodiux.input.Mouse;
import com.electrodiux.math.MathUtils;
import com.electrodiux.math.Vector3;
import com.electrodiux.util.Timer;
import com.electrodiux.world.Chunk;
import com.electrodiux.world.World;

public class Renderer {

    private Window window;
    private Shader shader;

    private Camera camera;
    private Vector3 position = new Vector3(), rotation = new Vector3();

    private Map<Chunk, ChunkBatch> chunkBatches;
    private World world;

    private final int renderDistance = 12;

    public Renderer(Window window) {
        this.window = window;

        this.chunkBatches = new HashMap<Chunk, ChunkBatch>();

        this.camera = new Camera();
    }

    private void load() {
        position.y(100);
        camera.setAspectRatio(window.getWidth(), window.getHeight());
        camera.getBackgroundColor().set(Color.LIGHT_BLUE);
        camera.setzFar(400f);

        try {
            shader = Shader.loadShader("/assets/shaders/default.glsl");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Timer chunkTimer = new Timer(0.25F);
        chunkTimer.addHandler(() -> {
            int xStart = (int) (camera.position().x() / Chunk.CHUNK_SIZE) - renderDistance;
            int zStart = (int) (camera.position().z() / Chunk.CHUNK_SIZE) - renderDistance;

            int xEnd = xStart + renderDistance * 2 + 1;
            int zEnd = zStart + renderDistance * 2 + 1;

            for (int x = xStart; x < xEnd; x++) {
                for (int z = zStart; z < zEnd; z++) {
                    Chunk chunk = world.getChunk(x, z);
                    if (chunk == null) {
                        world.loadChunk(x, z);
                    }
                }
            }
        });
        chunkTimer.start();
    }

    public void run() {
        Keyboard.configureKeyboard(window);
        GLFW.glfwSetInputMode(window.getWindowID(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
        Mouse.configureMouse(window);

        load();

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

    private void render() {
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        enableCulling(GL11.GL_BACK);

        camera.clearColor();
        camera.setProjectionsToShader(shader);

        if (world != null) {
            renderWorld(world);
        }
    }

    private void renderWorld(World world) {
        shader.use();
        computeNextChunksMeshes();
        for (Entry<Chunk, ChunkBatch> entry : chunkBatches.entrySet()) {
            entry.getValue().render(BlockRegister.getTextureAtlas());
        }
        shader.detach();
    }

    private void update(float deltaTime) {
        position.add(getMoveVector(deltaTime));
        rotation.add(getRotationVector(deltaTime));
        rotation.x = MathUtils.clamp((float) -Math.PI / 2, rotation.x, (float) Math.PI / 2);

        camera.position().set(position);
        camera.rotation().set(rotation);
    }

    private Vector3 getMoveVector(float deltaTime) {
        Vector3 move = new Vector3();

        final float cos = (float) Math.cos(rotation.y());
        final float sin = (float) Math.sin(rotation.y());

        if (Keyboard.isKeyPressed(Keyboard.GLFW_KEY_W))
            move.add(sin, 0, -cos);

        if (Keyboard.isKeyPressed(Keyboard.GLFW_KEY_S))
            move.add(-sin, 0, cos);

        if (Keyboard.isKeyPressed(Keyboard.GLFW_KEY_A))
            move.add(-cos, 0, -sin);

        if (Keyboard.isKeyPressed(Keyboard.GLFW_KEY_D))
            move.add(cos, 0, sin);

        if (Keyboard.isKeyPressed(Keyboard.GLFW_KEY_SPACE))
            move.add(0, 2, 0);
        if (Keyboard.isKeyPressed(Keyboard.GLFW_KEY_LEFT_SHIFT))
            move.add(0, -2, 0);

        return move.normalize().mul(deltaTime * (Keyboard.isKeyPressed(Keyboard.GLFW_KEY_LEFT_CONTROL) ? 35 : 20));
    }

    private Vector3 getRotationVector(float deltaTime) {
        final float value = 5 * deltaTime;
        return new Vector3((float) Math.toRadians(-Mouse.getDY()) * value,
                (float) Math.toRadians(-Mouse.getDX()) * value, 0);
    }

    private void computeNextChunksMeshes() {
        int xStart = (int) (camera.position().x() / Chunk.CHUNK_SIZE) - renderDistance;
        int zStart = (int) (camera.position().z() / Chunk.CHUNK_SIZE) - renderDistance;

        int xEnd = xStart + renderDistance * 2 + 1;
        int zEnd = zStart + renderDistance * 2 + 1;

        for (Chunk chunk : world.getChunks()) {
            ChunkBatch batch = chunkBatches.get(chunk);

            if (chunk.getXPos() >= xStart && chunk.getXPos() <= xEnd && chunk.getZPos() >= zStart
                    && chunk.getZPos() <= zEnd) {

                if (batch == null) {
                    batch = new ChunkBatch();
                    chunkBatches.put(chunk, batch);
                    batch.computeMesh(chunk, world);
                    batch.bufferData();
                }

            } else if (!(chunk.getXPos() >= xStart - 1 && chunk.getXPos() <= xEnd + 1 && chunk.getZPos() >= zStart - 1
                    && chunk.getZPos() <= zEnd + 1)) {

                if (batch != null) {
                    batch.clearBufferData();
                    chunkBatches.remove(chunk);
                }

            }
        }
    }

    private void recalculateChunks() {
        int xStart = (int) (camera.position().x() / Chunk.CHUNK_SIZE) - renderDistance;
        int zStart = (int) (camera.position().z() / Chunk.CHUNK_SIZE) - renderDistance;

        int xEnd = xStart + renderDistance * 2 + 1;
        int zEnd = zStart + renderDistance * 2 + 1;

        for (int x = xStart; x < xEnd; x++) {
            for (int z = zStart; z < zEnd; z++) {
                Chunk chunk = world.getChunk(x, z);
                if (chunk == null) {
                    world.loadChunk(x, z);
                    continue;
                }

                ChunkBatch batch = chunkBatches.get(chunk);
                if (batch == null) {
                    batch = new ChunkBatch();
                    chunkBatches.put(chunk, batch);
                }
                batch.computeMesh(chunk, world);
                batch.bufferData();
            }
        }
    }

    // private Matrix4f transformMatrix = new Matrix4f();

    // private void renderObject(Model model, Texture texture, Matrix4f
    // transformMatrix) {
    // shader.setMatrix4f("transformMatrix", transformMatrix);

    // GL30.glBindVertexArray(model.getVaoId());

    // enableVertexAttribArrays();

    // GL13.glActiveTexture(GL13.GL_TEXTURE0);

    // if (texture != null)
    // texture.bind();

    // GL11.glDrawElements(GL11.GL_TRIANGLES, model.getVertexCount(),
    // GL11.GL_UNSIGNED_INT, 0);

    // if (texture != null)
    // texture.unbind();

    // disableVertexAttribArrays();

    // GL30.glBindVertexArray(0);
    // }

    // private void enableVertexAttribArrays() {
    // GL20.glEnableVertexAttribArray(0);
    // GL20.glEnableVertexAttribArray(1);
    // }

    // private void disableVertexAttribArrays() {
    // GL20.glDisableVertexAttribArray(0);
    // GL20.glDisableVertexAttribArray(1);
    // }

    private void enableCulling(int mode) {
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(mode);
    }

    // private void disableCulling() {
    // GL11.glDisable(GL11.GL_CULL_FACE);
    // }

    public void setWorld(World world) {
        this.world = world;
    }

}
