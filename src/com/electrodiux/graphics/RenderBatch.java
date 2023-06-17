package com.electrodiux.graphics;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import com.electrodiux.graphics.textures.Texture;

public class RenderBatch {
    private static final int POS_SIZE = 3;
    private static final int TEXTURE_SIZE = 2;

    private static final int POS_OFFSET = 0;
    private static final int TEXTURE_OFFSET = POS_OFFSET + POS_SIZE * Float.BYTES;

    private static final int VERTEX_SIZE = 5;
    private static final int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

    private int numSprites;
    private boolean isAllocated;
    private FloatBuffer vertices;

    private int vaoID, vboID, eboID;
    private int maxBatchSize;

    public RenderBatch(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;

        this.numSprites = 0;

        // 4 vertices quads
        vertices = MemoryUtil.memAllocFloat(maxBatchSize * 4 * VERTEX_SIZE);
    }

    public void allocateBatch() {
        if (!isAllocated()) {
            // Generate and bind a Vertex Array Object
            vaoID = GL30.glGenVertexArrays();
            GL30.glBindVertexArray(vaoID);

            // Allocate space for vertices
            vboID = GL30.glGenBuffers();
            GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboID);
            GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertices.capacity() * Float.BYTES,
                    GL30.GL_DYNAMIC_DRAW);

            // Create and upload indices buffer
            eboID = GL30.glGenBuffers();
            int[] indices = generateIndices();
            GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, eboID);
            GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, indices,
                    GL30.GL_STATIC_DRAW);

            // Enable the buffer attribute pointers
            GL30.glVertexAttribPointer(0, POS_SIZE, GL30.GL_FLOAT, false,
                    VERTEX_SIZE_BYTES, POS_OFFSET);
            GL30.glEnableVertexAttribArray(0);

            GL30.glVertexAttribPointer(1, TEXTURE_SIZE, GL30.GL_FLOAT, false,
                    VERTEX_SIZE_BYTES, TEXTURE_OFFSET);
            GL30.glEnableVertexAttribArray(1);

            isAllocated = true;
        }
    }

    public void clearBufferData() {
        // if (isAllocated()) {
        GL30.glDeleteBuffers(vboID);
        GL30.glDeleteBuffers(eboID);
        GL30.glDeleteVertexArrays(vaoID);
        MemoryUtil.memFree(vertices);
        // }
    }

    public void addFace(Face face) {
        if (!hasRoom())
            return;

        int index = this.numSprites;
        this.numSprites++;

        loadVertexProperties(face, index);
    }

    public void render(Texture texture) {
        if (numSprites == 0) {
            return;
        }

        if (isAllocated()) {
            GL30.glBindVertexArray(vaoID);
            GL30.glEnableVertexAttribArray(0);
            GL30.glEnableVertexAttribArray(1);

            GL30.glDrawElements(GL30.GL_TRIANGLES, this.numSprites * 6,
                    GL30.GL_UNSIGNED_INT, 0);

            GL30.glDisableVertexAttribArray(0);
            GL30.glDisableVertexAttribArray(1);
            GL30.glBindVertexArray(0);
        }
    }

    public void bufferData() {
        if (isAllocated()) {
            GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboID);
            GL30.glBufferSubData(GL30.GL_ARRAY_BUFFER, 0, vertices);
        }
    }

    private void loadVertexProperties(Face face, int index) {
        int offset = index * 4 * VERTEX_SIZE;

        for (int i = 0; i < 4; i++) {
            int vertexOffset = offset + i * VERTEX_SIZE;

            vertices.put(vertexOffset, face.vertices, i * 3, POS_SIZE);
            vertices.put(vertexOffset + POS_SIZE, face.texCoords, i * 2, TEXTURE_SIZE);
        }
    }

    private int[] generateIndices() {
        int[] elements = new int[6 * maxBatchSize];

        for (int i = 0; i < maxBatchSize; i++) {
            final int offset = 4 * i;
            final int elmOff = 6 * i;

            // Triangle 1
            elements[elmOff] = offset + 3;
            elements[elmOff + 1] = offset + 2;
            elements[elmOff + 2] = offset;

            // Triangle 2
            elements[elmOff + 3] = offset;
            elements[elmOff + 4] = offset + 2;
            elements[elmOff + 5] = offset + 1;
        }

        return elements;
    }

    public boolean hasRoom() {
        return numSprites < this.maxBatchSize;
    }

    public boolean isAllocated() {
        return this.isAllocated;
    }

    public void resetBatch() {
        this.numSprites = 0;
    }

    public static class Face {

        private float[] vertices;
        private float[] texCoords;

        public Face(float[] vertices, float[] texCoords) {
            this.vertices = vertices;
            this.texCoords = texCoords;
        }

        public void set(float[] vertices, float[] texCoords) {
            this.vertices = vertices;
            this.texCoords = texCoords;
        }

    }
}
