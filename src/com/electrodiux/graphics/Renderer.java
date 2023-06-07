package com.electrodiux.graphics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.electrodiux.block.BlockRegister;
import com.electrodiux.block.Blocks;
import com.electrodiux.graphics.RenderBatch.Face;
import com.electrodiux.input.Keyboard;
import com.electrodiux.input.Mouse;
import com.electrodiux.math.MathUtils;
import com.electrodiux.math.Vector3;
import com.electrodiux.world.Chunk;
import com.electrodiux.world.World;

public class Renderer {

    private Window window;

    private Camera camera;
    private Shader shader;

    public Renderer(Window window) {
        this.window = window;
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

    private World world;

    public void setWorld(World world) {
        this.world = world;
    }

    private void render() {
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        // enableCulling(GL11.GL_BACK);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        camera.clearColor();

        camera.setProjectionsToShader(shader);

        if (world != null) {
            renderWorld(world);
        }
    }

    private int xStart, zStart;
    private final int renderDistance = 16 * 2;

    private void renderWorld(World world) {
        shader.use();
        for (RenderBatch batch : batches) {
            batch.render(BlockRegister.blocksMetadata[Blocks.STONE].getTexture());
        }
        shader.detach();
    }

    private void enableVertexAttribArrays() {
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
    }

    private void disableVertexAttribArrays() {
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
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

    private void update(float deltaTime) {
        cameraDistance = MathUtils.clamp(1, cameraDistance - Mouse.getScrollY(), 50);

        position.add(getMoveVector(deltaTime));
        rotation.add(getRotationVector(deltaTime));

        camera.position().set(position);
        camera.rotation().set(rotation);

        xStart = (int) position.x() - renderDistance;
        zStart = (int) position.z() - renderDistance;
    }

    private Vector3 position = new Vector3(), rotation = new Vector3();

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
            move.add(0, 1, 0);

        if (Keyboard.isKeyPressed(Keyboard.GLFW_KEY_LEFT_SHIFT))
            move.add(0, -1, 0);

        return move.normalize().mul(deltaTime * (Keyboard.isKeyPressed(Keyboard.GLFW_KEY_LEFT_CONTROL) ? 15 : 6));
    }

    private Vector3 getRotationVector(float deltaTime) {
        final float value = 5 * deltaTime;
        return new Vector3((float) Math.toRadians(-Mouse.getDY()) * value,
                (float) Math.toRadians(-Mouse.getDX()) * value, 0);
    }

    private float cameraDistance = 0.0f;

    private void load() {
        camera = new Camera();
        position.y(100);
        camera.setAspectRatio(window.getWidth(), window.getHeight());
        camera.getBackgroundColor().set(Color.LIGHT_BLUE);
        camera.setzFar(400f);
        cameraDistance = 10f;

        batches = new ArrayList<>();

        try {
            shader = Shader.loadShader("/assets/shaders/default.glsl");
        } catch (IOException e) {
            e.printStackTrace();
        }

        recalculateChunks();
    }

    private void recalculateChunks() {
        int xEnd = xStart + renderDistance * 2 + 1;
        int zEnd = zStart + renderDistance * 2 + 1;

        for (int x = xStart; x < xEnd; x++) {
            for (int z = zStart; z < zEnd; z++) {
                int chunkX = x / Chunk.CHUNK_SIZE;
                int chunkZ = z / Chunk.CHUNK_SIZE;
                Chunk chunk = world.getChunk(chunkX, chunkZ);
                if (chunk == null) {
                    world.loadChunk(chunkX, chunkZ);
                    continue;
                }
                for (int y = 0; y < world.getWorldHeight(); y++) {
                    short[] blocks = chunk.getBlocks();

                    int blockIndex = Chunk.getBlockIndexWithWorldCoords(x, y, z);

                    if (blocks[blockIndex] == Blocks.AIR) {
                        continue;
                    }

                    Texture texture = BlockRegister.blocksMetadata[blocks[blockIndex]].getTexture();

                    if (texture == null) {
                        continue;
                    }

                    addVisibleFaces(world, x, y, z, xStart, zStart, xEnd, zEnd, texture);

                    // if (blocks[blockIndex] != Blocks.AIR && addVisibleFaces(world, x, y, z,
                    // xStart,
                    // zStart, xEnd, zEnd)) {

                    // transformMatrix.identity();
                    // transformMatrix.translate(x, y, z);

                    // renderObject(cube, texture, transformMatrix);
                    // }
                }
            }
        }

        rebufferBatches();
    }

    private List<RenderBatch> batches;

    private void rebufferBatches() {
        for (RenderBatch batch : batches) {
            batch.rebufferData();
        }
    }

    private void addFace(Face face) {
        boolean added = false;
        for (RenderBatch batch : batches) {
            if (batch.hasRoom()) {
                batch.addFace(face);
                added = true;
                break;
            }
        }
        if (!added) {
            RenderBatch newBatch = new RenderBatch(1024 * 5);
            newBatch.allocateBatch();
            batches.add(newBatch);
            newBatch.addFace(face);
        }
    }

    private void addVisibleFaces(World world, int x, int y, int z, int xStart, int zStart, int xEnd, int zEnd,
            Texture texture) {

        transformFaceMatrix.identity();
        transformFaceMatrix.translate(x, y, z);

        if (isHideBlock(world, x, y + 1, z))
            addFace(getFace(FACE_TOP));
        if (isHideBlock(world, x, y - 1, z))
            addFace(getFace(FACE_BOTTOM));
        if (isHideBlock(world, x, y, z + 1))
            addFace(getFace(FACE_FRONT));
        if (isHideBlock(world, x, y, z - 1))
            addFace(getFace(FACE_BACK));
        if (isHideBlock(world, x + 1, y, z))
            addFace(getFace(FACE_RIGHT));
        if (isHideBlock(world, x - 1, y, z))
            addFace(getFace(FACE_LEFT));
    }

    private boolean isHideBlock(World world, int x, int y, int z) {
        if (y < 0 || y >= world.getWorldHeight()) {
            return false;
        }
        short block = world.getBlock(x, y, z);
        return block == Blocks.AIR || BlockRegister.getBlock(block).isTransparent();
    }

    private static final int FACE_TOP = 4;
    private static final int FACE_BOTTOM = 5;
    private static final int FACE_RIGHT = 3;
    private static final int FACE_LEFT = 2;
    private static final int FACE_FRONT = 0;
    private static final int FACE_BACK = 1;

    private Matrix4f transformFaceMatrix = new Matrix4f();
    private Face face = new Face(null, null);

    private Face getFace(int dataIndex) {
        float[] vertices = new float[12];
        System.arraycopy(cubeVertices, dataIndex * 12, vertices, 0, 12);

        float[] texCoords = new float[8];
        System.arraycopy(cubeTexCoords, dataIndex * 8, texCoords, 0, 8);

        face.set(getVertices(vertices), texCoords);

        return face;
    }

    private float[] getVertices(float[] vertices) {
        float[] v = new float[vertices.length];
        Vector4f vec = new Vector4f();

        for (int i = 0; i < vertices.length; i += 3) {
            vec.set(vertices[i + 0], vertices[i + 1], vertices[i + 2], 1);
            vec.mul(transformFaceMatrix);

            v[i + 0] = vec.x;
            v[i + 1] = vec.y;
            v[i + 2] = vec.z;
        }
        return v;
    }

    private final float[] cubeVertices = {
            // Front face
            -0.5f, -0.5f, 0.5f, // Bottom-left
            0.5f, -0.5f, 0.5f, // Bottom-right
            0.5f, 0.5f, 0.5f, // Top-right
            -0.5f, 0.5f, 0.5f, // Top-left

            // Back face
            -0.5f, -0.5f, -0.5f, // Bottom-left
            0.5f, -0.5f, -0.5f, // Bottom-right
            0.5f, 0.5f, -0.5f, // Top-right
            -0.5f, 0.5f, -0.5f, // Top-left

            // Left face
            -0.5f, -0.5f, -0.5f, // Bottom-front
            -0.5f, -0.5f, 0.5f, // Bottom-back
            -0.5f, 0.5f, 0.5f, // Top-back
            -0.5f, 0.5f, -0.5f, // Top-front

            // Right face
            0.5f, -0.5f, -0.5f, // Bottom-front
            0.5f, -0.5f, 0.5f, // Bottom-back
            0.5f, 0.5f, 0.5f, // Top-back
            0.5f, 0.5f, -0.5f, // Top-front

            // Top face
            -0.5f, 0.5f, 0.5f, // Front-left
            0.5f, 0.5f, 0.5f, // Front-right
            0.5f, 0.5f, -0.5f, // Back-right
            -0.5f, 0.5f, -0.5f, // Back-left

            // Bottom face
            -0.5f, -0.5f, 0.5f, // Front-left
            0.5f, -0.5f, 0.5f, // Front-right
            0.5f, -0.5f, -0.5f, // Back-right
            -0.5f, -0.5f, -0.5f, // Back-left
    };

    private final float[] cubeTexCoords = {
            // Front face
            0.0f, 0.0f, // Bottom-left
            1.0f, 0.0f, // Bottom-right
            1.0f, 1.0f, // Top-right
            0.0f, 1.0f, // Top-left

            // Back face
            1.0f, 0.0f, // Bottom-left
            0.0f, 0.0f, // Bottom-right
            0.0f, 1.0f, // Top-right
            1.0f, 1.0f, // Top-left

            // Left face
            0.0f, 0.0f, // Bottom-front
            1.0f, 0.0f, // Bottom-back
            1.0f, 1.0f, // Top-back
            0.0f, 1.0f, // Top-front

            // Right face
            1.0f, 0.0f, // Bottom-front
            0.0f, 0.0f, // Bottom-back
            0.0f, 1.0f, // Top-back
            1.0f, 1.0f, // Top-front

            // Top face
            0.0f, 1.0f, // Front-left
            1.0f, 1.0f, // Front-right
            1.0f, 0.0f, // Back-right
            0.0f, 0.0f, // Back-left

            // Bottom face
            0.0f, 0.0f, // Front-left
            1.0f, 0.0f, // Front-right
            1.0f, 1.0f, // Back-right
            0.0f, 1.0f // Back-left
    };

    private void enableCulling(int mode) {
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(mode);
    }

    private void disableCulling() {
        GL11.glDisable(GL11.GL_CULL_FACE);
    }

}
