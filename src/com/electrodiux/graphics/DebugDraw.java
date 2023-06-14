package com.electrodiux.graphics;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glPointSize;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.io.IOException;
import java.util.Arrays;

import org.joml.Vector4f;

import com.electrodiux.math.Vector3;

public class DebugDraw {

    private static final int MAX_LINES = 1024;
    private static final int MAX_POINTS = 1024;

    private static boolean loaded = false;
    private static boolean active = false;

    private static DebugRenderBatch linesBatch;
    private static DebugRenderBatch pointsBatch;

    private static Shader debugShader;

    public static void load() {
        try {
            debugShader = Shader.loadShader("/assets/shaders/debug.glsl");

            // Each line have two points
            linesBatch = new DebugRenderBatch(MAX_LINES * 2);
            linesBatch.load();

            pointsBatch = new DebugRenderBatch(MAX_POINTS);
            pointsBatch.load();

            loaded = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void render(Camera camera) {
        if (!loaded) {
            return;
        }

        if (!active || (linesBatch.count <= 0 && pointsBatch.count <= 0)) {
            return;
        }

        debugShader.use();
        debugShader.setMatrix4f("projection", camera.getProjectionMatrix());
        debugShader.setMatrix4f("view", camera.getViewMatrix());

        glLineWidth(3.5f);
        linesBatch.render(GL_LINES, 2); // Two per line
        glPointSize(6f);
        pointsBatch.render(GL_POINTS, 1); // One per point

        linesBatch.reset();
        pointsBatch.reset();

        debugShader.detach();
    }

    // private static void addLineToVertexBuffer(DebugRenderBatch batch, Vector3
    // from, Vector3 to, Color color) {
    // addLineToVertexBuffer(batch, from.x(), from.y(), from.z(), to.x(), to.y(),
    // to.z(), color);
    // }

    private static void addLineToVertexBuffer(DebugRenderBatch batch, float fromx, float fromy, float fromz, float tox,
            float toy, float toz, Color color) {
        if (batch.count >= MAX_LINES) {
            return;
        }

        batch.vertexArray[batch.vertexIndex + 0] = fromx;
        batch.vertexArray[batch.vertexIndex + 1] = fromy;
        batch.vertexArray[batch.vertexIndex + 2] = fromz;
        batch.vertexArray[batch.vertexIndex + 3] = color.r();
        batch.vertexArray[batch.vertexIndex + 4] = color.g();
        batch.vertexArray[batch.vertexIndex + 5] = color.b();
        batch.vertexIndex += 6;

        batch.vertexArray[batch.vertexIndex + 0] = tox;
        batch.vertexArray[batch.vertexIndex + 1] = toy;
        batch.vertexArray[batch.vertexIndex + 2] = toz;
        batch.vertexArray[batch.vertexIndex + 3] = color.r();
        batch.vertexArray[batch.vertexIndex + 4] = color.g();
        batch.vertexArray[batch.vertexIndex + 5] = color.b();
        batch.vertexIndex += 6;

        batch.count++;
    }

    private static void addPointToVertexBuffer(DebugRenderBatch batch, float px, float py, float pz, Color color) {
        if (batch.count >= MAX_POINTS) {
            return;
        }

        batch.vertexArray[batch.vertexIndex + 0] = px;
        batch.vertexArray[batch.vertexIndex + 1] = py;
        batch.vertexArray[batch.vertexIndex + 2] = pz;
        batch.vertexArray[batch.vertexIndex + 3] = color.r();
        batch.vertexArray[batch.vertexIndex + 4] = color.g();
        batch.vertexArray[batch.vertexIndex + 5] = color.b();
        batch.vertexIndex += 6;

        batch.count++;
    }

    private static class DebugRenderBatch {
        private float[] vertexArray;
        private int vertexIndex = 0;
        private int count = 0;
        private int vaoId, vboId;

        public DebugRenderBatch(int maxCapacity) {
            vertexArray = new float[maxCapacity * 6];
        }

        public void load() {
            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            vboId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, vertexArray.length * Float.BYTES, GL_DYNAMIC_DRAW);

            glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
            glEnableVertexAttribArray(0);

            glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
            glEnableVertexAttribArray(0);
        }

        public void render(int renderMode, int elementsPerCount) {
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            float[] subData = Arrays.copyOfRange(vertexArray, 0, count * 6 * elementsPerCount);
            glBufferSubData(GL_ARRAY_BUFFER, 0, subData);

            glBindVertexArray(vaoId);
            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);

            glDrawArrays(renderMode, 0, subData.length);

            glDisableVertexAttribArray(0);
            glDisableVertexAttribArray(1);
            glBindVertexArray(0);
        }

        public void reset() {
            count = 0;
            vertexIndex = 0;
        }
    }

    public static void addLine(Vector4f from, Vector4f to, Color color) {
        addLine(from.x, from.y, from.z, to.x, to.y, to.z, color);
    }

    public static void addLine(Vector3 from, Vector3 to, Color color) {
        addLine(from.x(), from.y(), from.z(), to.x(), to.y(), to.z(), color);
    }

    public static void addLine(float fromX, float fromY, float fromZ, float toX, float toY, float toZ, Color color) {
        if (loaded) {
            addLineToVertexBuffer(linesBatch, fromX, fromY, fromZ, toX, toY, toZ, color);
        }
    }

    public static void addPoint(Vector3 position, Color color) {
        addPoint(position.x(), position.y(), position.z(), color);
    }

    public static void addPoint(float px, float py, float pz, Color color) {
        if (loaded) {
            addPointToVertexBuffer(pointsBatch, px, py, pz, color);
        }
    }

    // public static void renderString(String text, float x, float y, Color color,
    // Font font) {
    // GL11.glBindTexture(GL11.GL_TEXTURE_2D, font.getFontTextureId());
    // GL11.glColor3f(color.r(), color.g(), color.b());
    // GL11.glRasterPos2f(x, y);

    // }

    public static boolean isActive() {
        return active;
    }

    public static void setActive(boolean active) {
        DebugDraw.active = active;
    }

}