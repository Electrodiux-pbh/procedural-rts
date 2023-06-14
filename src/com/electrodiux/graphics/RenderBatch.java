package com.electrodiux.graphics;

import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import com.electrodiux.graphics.textures.Texture;

public class RenderBatch {
    private static final int POS_SIZE = 3;
    private static final int TEXTURE_SIZE = 2;

    private static final int POS_OFFSET = 0;
    private static final int TEXTURE_OFFSET = POS_OFFSET + POS_SIZE * Float.BYTES;

    private static final int VERTEX_SIZE = 5;
    private static final int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

    private int numSprites;
    private boolean hasRoom;
    private boolean isAllocated;
    private float[] vertices;

    private int vaoID, vboID, eboID;
    private int maxBatchSize;

    public RenderBatch(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;

        // 4 vertices quads
        vertices = new float[maxBatchSize * 4 * VERTEX_SIZE];

        this.numSprites = 0;
        this.hasRoom = true;
    }

    public void allocateBatch() {
        if (!isAllocated()) {
            // Generate and bind a Vertex Array Object
            vaoID = GL30.glGenVertexArrays();
            GL30.glBindVertexArray(vaoID);

            // Allocate space for vertices
            vboID = GL30.glGenBuffers();
            GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboID);
            GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertices.length * Float.BYTES, GL30.GL_DYNAMIC_DRAW);

            // Create and upload indices buffer
            eboID = GL30.glGenBuffers();
            int[] indices = generateIndices();
            GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, eboID);
            GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, indices, GL30.GL_STATIC_DRAW);

            // Enable the buffer attribute pointers
            GL30.glVertexAttribPointer(0, POS_SIZE, GL30.GL_FLOAT, false, VERTEX_SIZE_BYTES, POS_OFFSET);
            GL30.glEnableVertexAttribArray(0);

            GL30.glVertexAttribPointer(1, TEXTURE_SIZE, GL30.GL_FLOAT, false, VERTEX_SIZE_BYTES, TEXTURE_OFFSET);
            GL30.glEnableVertexAttribArray(1);

            isAllocated = true;
        }
    }

    public void clearBufferData() {
        if (isAllocated()) {
            GL30.glDeleteVertexArrays(vaoID);
            GL30.glDeleteBuffers(vboID);
            GL30.glDeleteBuffers(eboID);
        }
    }

    public void addFace(Face face) {
        if (!hasRoom())
            return;

        int index = this.numSprites;
        this.numSprites++;

        loadVertexProperties(face, index);

        if (numSprites >= this.maxBatchSize) {
            this.hasRoom = false;
        }
    }

    public void render(Texture texture) {
        if (numSprites == 0) {
            return;
        }

        GL30.glBindVertexArray(vaoID);
        GL30.glEnableVertexAttribArray(0);
        GL30.glEnableVertexAttribArray(1);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);

        if (texture != null)
            texture.bind();

        GL30.glDrawElements(GL30.GL_TRIANGLES, this.numSprites * 6, GL30.GL_UNSIGNED_INT, 0);

        if (texture != null)
            texture.unbind();

        GL30.glDisableVertexAttribArray(0);
        GL30.glDisableVertexAttribArray(1);
        GL30.glBindVertexArray(0);
    }

    public void bufferData() {
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboID);
        GL30.glBufferSubData(GL30.GL_ARRAY_BUFFER, 0, vertices);
    }

    private void loadVertexProperties(Face face, int index) {
        int offset = index * 4 * VERTEX_SIZE;

        for (int i = 0; i < 4; i++) {
            int vertexOffset = offset + i * VERTEX_SIZE;
            System.arraycopy(face.vertices, i * 3, vertices, vertexOffset, POS_SIZE);
            System.arraycopy(face.texCoords, i * 2, vertices, vertexOffset + POS_SIZE, TEXTURE_SIZE);
        }
    }

    private int[] generateIndices() {
        int[] elements = new int[6 * maxBatchSize];

        for (int i = 0; i < maxBatchSize; i++) {
            int offset = 4 * i;

            // Triangle 1
            elements[6 * i] = offset + 3;
            elements[6 * i + 1] = offset + 2;
            elements[6 * i + 2] = offset;

            // Triangle 2
            elements[6 * i + 3] = offset;
            elements[6 * i + 4] = offset + 2;
            elements[6 * i + 5] = offset + 1;
        }

        return elements;
    }

    public boolean hasRoom() {
        return this.hasRoom;
    }

    public boolean isAllocated() {
        return this.isAllocated;
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
