package com.electrodiux.graphics;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.electrodiux.block.BlockRegister;
import com.electrodiux.entities.Entity;
import com.electrodiux.graphics.textures.Texture;
import com.electrodiux.input.Keyboard;
import com.electrodiux.input.Mouse;
import com.electrodiux.math.MathUtils;
import com.electrodiux.math.Vector3;
import com.electrodiux.util.Timer;
import com.electrodiux.world.Chunk;
import com.electrodiux.world.Chunk.ChunkStatus;
import com.electrodiux.world.World;

public class Renderer {

    private Window window;

    private Camera camera;
    private Shader chunksShader;
    private Shader entitiesShader;

    private Map<Chunk, ChunkBatch> chunkBatches;
    private ExecutorService chunkMeshService;

    private World world;
    private Vector3 position = new Vector3(), rotation = new Vector3();

    private int renderDistance = 12;

    public Renderer(Window window) {
        this.window = window;

        this.chunkBatches = new HashMap<Chunk, ChunkBatch>();

        this.camera = new Camera();
    }

    private void load() {
        DebugDraw.load();

        position.set(0, 100, 0);

        camera.getBackgroundColor().set(new Color("#45c8ff"));
        camera.setzFar(renderDistance * Chunk.CHUNK_SIZE * 1.5f);

        window.setSizeCallback((int width, int height) -> {
            camera.setAspectRatio(width, height);
        });

        loadFrustrumPlanes();

        try {
            chunksShader = Shader.loadShader("/assets/shaders/chunks.glsl");
            entitiesShader = Shader.loadShader("/assets/shaders/entities.glsl");
        } catch (IOException e) {
            e.printStackTrace();
        }

        chunksShader.setColor("fogColor", new Color("#8dd4f2"));
        chunksShader.setColor("skyColor", camera.getBackgroundColor());

        Timer chunkTimer = new Timer(1F);
        chunkTimer.addHandler(this::loadNearChunks);
        chunkTimer.start();

        chunkMeshService = Executors.newSingleThreadExecutor();
    }

    // #region Rendering
    private void render() {
        // GL11.glEnable(GL11.GL_BLEND);
        // GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        enableCulling(GL11.GL_BACK);

        camera.clearColor();

        if (world != null) {
            updateFrustumPlanes(camera.getProjectionViewMatrix());

            chunksShader.setMatrix4f("projection", camera.getProjectionMatrix());
            chunksShader.setMatrix4f("view", camera.getViewMatrix());

            chunksShader.setFloat("fogDistance", renderDistance * Chunk.CHUNK_SIZE);

            renderChunks(world);

            entitiesShader.setMatrix4f("projection", camera.getProjectionMatrix());
            entitiesShader.setMatrix4f("view", camera.getViewMatrix());

            renderEntities(world);
        }

        renderChunkBoundaries(Math.floorDiv((int) position.x(), Chunk.CHUNK_SIZE),
                Math.floorDiv((int) position.z(), Chunk.CHUNK_SIZE),
                Color.YELLOW);

        DebugDraw.render(camera);
    }

    // #region Chunk rendering

    private void renderChunks(World world) {
        chunksShader.use();

        int xStart = Chunk.getChunkXFromWorld((int) camera.position().x()) - renderDistance;
        int zStart = Chunk.getChunkZFromWorld((int) camera.position().z()) - renderDistance;
        int xEnd = xStart + renderDistance * 2;
        int zEnd = zStart + renderDistance * 2;

        computeNextChunksMeshes(xStart, zStart, xEnd, zEnd);

        for (Entry<Chunk, ChunkBatch> entry : chunkBatches.entrySet()) {
            Chunk chunk = entry.getKey();
            ChunkBatch batch = entry.getValue();

            if (batch.isDeleted() || (!batch.isComputed() && !batch.isBuffered())) {
                continue;
            }

            boolean inRenderDistance = chunk.getXPos() >= xStart && chunk.getXPos() <= xEnd && chunk.getZPos() >= zStart
                    && chunk.getZPos() <= zEnd;

            boolean inFrustrum = isChunkInFrustum(chunk.getBlockX(), chunk.getBlockZ());

            if (inRenderDistance && inFrustrum) {

                if (!batch.isBuffered()) {
                    batch.bufferData();
                }

                batch.render(BlockRegister.getAtlasTexture());
            }
        }
        chunksShader.detach();
    }

    private void computeChunkMesh(Chunk chunk, ChunkBatch batch) {
        chunkMeshService.execute(new Runnable() {

            @Override
            public void run() {
                batch.computeMesh(chunk, world);

                computeNextChunk(chunk.getXPos() + 1, chunk.getZPos());
                computeNextChunk(chunk.getXPos() - 1, chunk.getZPos());
                computeNextChunk(chunk.getXPos(), chunk.getZPos() + 1);
                computeNextChunk(chunk.getXPos(), chunk.getZPos() - 1);
            }

            private void computeNextChunk(int x, int z) {
                Chunk chunk = world.getChunk(x, z);
                if (chunk == null)
                    return;

                ChunkBatch batch = chunkBatches.get(chunk);
                if (batch == null)
                    return;

                batch.computeMesh(chunk, world);
            }

        });
    }

    private void reloadChunkMeshes() {
        int xStart = Chunk.getChunkXFromWorld((int) camera.position().x()) - renderDistance;
        int zStart = Chunk.getChunkZFromWorld((int) camera.position().z()) - renderDistance;
        int xEnd = xStart + renderDistance * 2;
        int zEnd = zStart + renderDistance * 2;

        clearChunkBatches();

        for (Chunk chunk : world.getLoadedChunks()) {
            if (chunk.getChunkStatus() != ChunkStatus.COMPLETE)
                continue;

            if (chunk.getXPos() >= xStart && chunk.getXPos() <= xEnd && chunk.getZPos() >= zStart
                    && chunk.getZPos() <= zEnd) {

                ChunkBatch newBatch = new ChunkBatch();
                chunkBatches.put(chunk, newBatch);

                computeChunkMesh(chunk, newBatch);
            }
        }
    }

    private void clearChunkBatches() {
        for (ChunkBatch batch : chunkBatches.values()) {
            batch.clearBufferData();
        }
        chunkBatches.clear();
    }

    private void computeNextChunksMeshes(int xStart, int zStart, int xEnd, int zEnd) {
        for (Chunk chunk : world.getLoadedChunks()) {
            if (chunk.getChunkStatus() != ChunkStatus.COMPLETE)
                continue;

            ChunkBatch batch = chunkBatches.get(chunk);

            if (chunk.getXPos() >= xStart && chunk.getXPos() <= xEnd && chunk.getZPos() >= zStart
                    && chunk.getZPos() <= zEnd) {

                if (batch == null) {
                    ChunkBatch newBatch = new ChunkBatch();
                    chunkBatches.put(chunk, newBatch);

                    computeChunkMesh(chunk, newBatch);
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
    // #endregion

    // #region Entity Rendering

    private void renderEntities(World world) {
        GL11.glDisable(GL11.GL_CULL_FACE);

        entitiesShader.use();

        Matrix4f transform = new Matrix4f();

        for (Entity entity : world.getEntities()) {
            Model model = entity.getModel();
            Texture texture = entity.getTexture();

            // DebugDraw.addPoint(entity.getPosition().x(), entity.getPosition().y(),
            // entity.getPosition().z(),
            // Color.PINK);

            MathUtils.transformMatrix(entity.getPosition(), entity.getRotation(), Vector3.ONE, transform);
            entitiesShader.setMatrix4f("transform", transform);

            GL30.glBindVertexArray(model.getVaoId());

            GL20.glEnableVertexAttribArray(0);
            GL20.glEnableVertexAttribArray(1);
            GL20.glEnableVertexAttribArray(2);

            GL13.glActiveTexture(GL13.GL_TEXTURE0);

            if (texture != null)
                texture.bind();

            GL11.glDrawElements(GL11.GL_TRIANGLES, model.getVertexCount(),
                    GL11.GL_UNSIGNED_INT, 0);

            if (texture != null)
                texture.unbind();

            GL20.glDisableVertexAttribArray(0);
            GL20.glDisableVertexAttribArray(1);
            GL20.glDisableVertexAttribArray(2);

            GL30.glBindVertexArray(0);
        }
        entitiesShader.detach();
    }

    // #endregion

    // #endregion

    private void loadNearChunks() {
        final int distance = renderDistance;

        final int cx = Chunk.getChunkXFromWorld((int) position.x());
        final int cz = Chunk.getChunkZFromWorld((int) position.z());

        // Unload far chunks
        final int xStart = cx - distance;
        final int zStart = cz - distance;
        final int xEnd = cx + distance;
        final int zEnd = cz + distance;

        for (Chunk chunk : world.getLoadedChunks()) {
            if (!(chunk.getXPos() >= xStart && chunk.getXPos() <= xEnd && chunk.getZPos() >= zStart
                    && chunk.getZPos() <= zEnd)) {
                world.unloadChunk(chunk);
            }
        }

        // Load near chunks
        for (int l = 0; l <= distance; l++) {
            int p1x = cx - l;
            int p1z = cz - l;

            int p2x = cx + l;
            int p2z = cz + l;

            for (int i = p1x; i <= p2x; i++) {
                world.loadChunk(i, p1z);
            }

            for (int i = p1z; i <= p2z; i++) {
                world.loadChunk(p2x, i);
            }

            for (int i = p2x; i >= p1x; i--) {
                world.loadChunk(i, p2z);
            }

            for (int i = p2z; i >= p1z; i--) {
                world.loadChunk(p1x, i);
            }
        }
    }

    // #region Update Logic

    private void update(float deltaTime) {
        position.add(getMoveVector(deltaTime));
        rotation.add(getRotationVector(deltaTime));
        rotation.x = MathUtils.clamp((float) (-Math.PI / 2), rotation.x, (float) (Math.PI / 2));

        camera.position().set(position);
        camera.rotation().set(rotation);

        if (Keyboard.isKeyTyped(Keyboard.GLFW_KEY_F2)) {
            DebugDraw.setActive(!DebugDraw.isActive());
        }

        if (Keyboard.isKeyTyped(Keyboard.GLFW_KEY_F6)) {
            reloadChunkMeshes();
        }

        if (Keyboard.isKeyTyped(Keyboard.GLFW_KEY_F3)) {
            System.out.println("Loaded chunks: " + world.loadedChunksCount());
            System.out.println("Batches: " + chunkBatches.size());
        }

        if (Keyboard.isKeyTyped(Keyboard.GLFW_KEY_F4)) {
            world.unloadChunks();
            clearChunkBatches();
        }

        if (Keyboard.isKeyTyped(Keyboard.GLFW_KEY_F1)) {
            System.out.println("x: " + position.x() + " y: " + position.y() + " z: " + position.z());
        }

        if (Keyboard.isKeyTyped(Keyboard.GLFW_KEY_UP)) {
            setRenderDistance(renderDistance + 5);
        }

        if (Keyboard.isKeyTyped(Keyboard.GLFW_KEY_DOWN)) {
            setRenderDistance(renderDistance - 5);
        }
    }

    private void setRenderDistance(int distance) {
        renderDistance = distance;
        clearChunkBatches();
    }

    private float velocity = 20;

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

        velocity = MathUtils.clamp(0, velocity + Mouse.getScrollY() * 4, 300);

        return move.normalize().mul(deltaTime * velocity);
    }

    private Vector3 getRotationVector(float deltaTime) {
        final float value = 5 * deltaTime;
        return new Vector3((float) Math.toRadians(-Mouse.getDY()) * value,
                (float) Math.toRadians(-Mouse.getDX()) * value, 0);
    }

    // #endregion

    // #region Frustrum Culling

    private Vector4f[] frustumPlanes = new Vector4f[6];

    private void updateFrustumPlanes(Matrix4f viewProjectionMatrix) {
        // Right plane
        frustumPlanes[0].set(
                viewProjectionMatrix.m03() - viewProjectionMatrix.m00(),
                viewProjectionMatrix.m13() - viewProjectionMatrix.m10(),
                viewProjectionMatrix.m23() - viewProjectionMatrix.m20(),
                viewProjectionMatrix.m33() - viewProjectionMatrix.m30()).normalize();

        // Left plane
        frustumPlanes[1].set(
                viewProjectionMatrix.m03() + viewProjectionMatrix.m00(),
                viewProjectionMatrix.m13() + viewProjectionMatrix.m10(),
                viewProjectionMatrix.m23() + viewProjectionMatrix.m20(),
                viewProjectionMatrix.m33() + viewProjectionMatrix.m30()).normalize();

        // Bottom plane
        frustumPlanes[2].set(
                viewProjectionMatrix.m03() + viewProjectionMatrix.m01(),
                viewProjectionMatrix.m13() + viewProjectionMatrix.m11(),
                viewProjectionMatrix.m23() + viewProjectionMatrix.m21(),
                viewProjectionMatrix.m33() + viewProjectionMatrix.m31()).normalize();

        // Top plane
        frustumPlanes[3].set(
                viewProjectionMatrix.m03() - viewProjectionMatrix.m01(),
                viewProjectionMatrix.m13() - viewProjectionMatrix.m11(),
                viewProjectionMatrix.m23() - viewProjectionMatrix.m21(),
                viewProjectionMatrix.m33() - viewProjectionMatrix.m31()).normalize();

        // Near plane
        frustumPlanes[4].set(
                viewProjectionMatrix.m03() + viewProjectionMatrix.m02(),
                viewProjectionMatrix.m13() + viewProjectionMatrix.m12(),
                viewProjectionMatrix.m23() + viewProjectionMatrix.m22(),
                viewProjectionMatrix.m33() + viewProjectionMatrix.m32()).normalize();

        // Far plane
        frustumPlanes[5].set(
                viewProjectionMatrix.m03() - viewProjectionMatrix.m02(),
                viewProjectionMatrix.m13() - viewProjectionMatrix.m12(),
                viewProjectionMatrix.m23() - viewProjectionMatrix.m22(),
                viewProjectionMatrix.m33() - viewProjectionMatrix.m32()).normalize();
    }

    private boolean isChunkInFrustum(int chunkX, int chunkZ) {
        for (int i = 0; i < frustumPlanes.length; i++) {
            Vector4f plane = frustumPlanes[i];

            float d = plane.x * chunkX + plane.z * chunkZ + plane.w;
            if (d <= -Chunk.CHUNK_SIZE) {
                return false;
            }
        }
        return true;
    }

    private void loadFrustrumPlanes() {
        for (int i = 0; i < frustumPlanes.length; i++) {
            frustumPlanes[i] = new Vector4f();
        }
    }

    private static Vector4f[] ndcCorners = new Vector4f[8];

    static {
        ndcCorners[0] = new Vector4f(-1.0f, -1.0f, -1.0f, 1.0f);
        ndcCorners[1] = new Vector4f(-1.0f, -1.0f, 1.0f, 1.0f);
        ndcCorners[2] = new Vector4f(-1.0f, 1.0f, -1.0f, 1.0f);
        ndcCorners[3] = new Vector4f(-1.0f, 1.0f, 1.0f, 1.0f);
        ndcCorners[4] = new Vector4f(1.0f, -1.0f, -1.0f, 1.0f);
        ndcCorners[5] = new Vector4f(1.0f, -1.0f, 1.0f, 1.0f);
        ndcCorners[6] = new Vector4f(1.0f, 1.0f, -1.0f, 1.0f);
        ndcCorners[7] = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @SuppressWarnings("unused")
    private void renderDebugFrustrum(Camera camera) {
        if (!DebugDraw.isActive())
            return;

        Matrix4f inverseViewMatrix = new Matrix4f();
        camera.getViewMatrix().invert(inverseViewMatrix);

        Matrix4f inverseProjectionMatrix = new Matrix4f();
        camera.getProjectionMatrix().invert(inverseProjectionMatrix);

        Vector4f[] worldCorners = new Vector4f[8];

        for (int i = 0; i < 8; i++) {
            Vector4f ndcCorner = ndcCorners[i];
            Vector4f clipCorner = new Vector4f(ndcCorner.x, ndcCorner.y, ndcCorner.z, 1.0f);
            Vector4f eyeCorner = new Vector4f();

            inverseProjectionMatrix.transform(clipCorner, eyeCorner);
            eyeCorner.mul(1.0f / eyeCorner.w);
            inverseViewMatrix.transform(eyeCorner);

            worldCorners[i] = eyeCorner;
        }

        DebugDraw.addLine(worldCorners[0], worldCorners[1], Color.LIME); // Bottom
        DebugDraw.addLine(worldCorners[1], worldCorners[3], Color.LIME); // Bottom
        DebugDraw.addLine(worldCorners[3], worldCorners[2], Color.LIME); // Bottom
        DebugDraw.addLine(worldCorners[2], worldCorners[0], Color.LIME); // Bottom

        DebugDraw.addLine(worldCorners[4], worldCorners[5], Color.LIME); // Top near
        DebugDraw.addLine(worldCorners[5], worldCorners[7], Color.LIME); // Top far
        DebugDraw.addLine(worldCorners[7], worldCorners[6], Color.LIME); // Top right
        DebugDraw.addLine(worldCorners[6], worldCorners[4], Color.LIME); // Top left

        DebugDraw.addLine(worldCorners[0], worldCorners[4], Color.LIME); // Left
        DebugDraw.addLine(worldCorners[1], worldCorners[5], Color.LIME); // Right
        DebugDraw.addLine(worldCorners[2], worldCorners[6], Color.LIME); // Near
        DebugDraw.addLine(worldCorners[3], worldCorners[7], Color.LIME); // Far
    }

    // #endregion

    private void renderChunkBoundaries(int chunkX, int chunkZ, Color color) {
        if (!DebugDraw.isActive())
            return;

        int xStart = chunkX * Chunk.CHUNK_SIZE;
        int xEnd = xStart + Chunk.CHUNK_SIZE;
        int zStart = chunkZ * Chunk.CHUNK_SIZE;
        int zEnd = zStart + Chunk.CHUNK_SIZE;
        int yStart = 0;
        int yEnd = Chunk.CHUNK_HEIGHT;

        DebugDraw.addLine(xStart, yStart, zStart, xStart, yEnd, zStart, color);
        DebugDraw.addLine(xEnd, yStart, zStart, xEnd, yEnd, zStart, color);
        DebugDraw.addLine(xEnd, yStart, zEnd, xEnd, yEnd, zEnd, color);
        DebugDraw.addLine(xStart, yStart, zEnd, xStart, yEnd, zEnd, color);

        DebugDraw.addLine(xStart, yStart, zStart, xEnd, yStart, zStart, color);
        DebugDraw.addLine(xEnd, yStart, zStart, xEnd, yStart, zEnd, color);
        DebugDraw.addLine(xEnd, yStart, zEnd, xStart, yStart, zEnd, color);
        DebugDraw.addLine(xStart, yStart, zEnd, xStart, yStart, zStart, color);

        DebugDraw.addLine(xStart, yEnd, zStart, xEnd, yEnd, zStart, color);
        DebugDraw.addLine(xEnd, yEnd, zStart, xEnd, yEnd, zEnd, color);
        DebugDraw.addLine(xEnd, yEnd, zEnd, xStart, yEnd, zEnd, color);
        DebugDraw.addLine(xStart, yEnd, zEnd, xStart, yEnd, zStart, color);
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
            Keyboard.endFrame();
        }

        Loader.clear();

        System.exit(0);
    }

    private void enableCulling(int mode) {
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(mode);
    }

    public void setWorld(World world) {
        this.world = world;
    }

}
