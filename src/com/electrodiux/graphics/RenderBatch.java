package com.electrodiux.graphics;

import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import com.electrodiux.graphics.textures.Texture;

public class RenderBatch {
    private final int POS_SIZE = 3;
    private final int TEXTURE_SIZE = 2;

    private final int POS_OFFSET = 0;
    private final int TEXTURE_OFFSET = POS_OFFSET + POS_SIZE * Float.BYTES;

    private final int VERTEX_SIZE = 5;
    private final int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

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
        // Find offset within array (4 vertices per sprite)
        int offset = index * 4 * VERTEX_SIZE;

        // Add vertices with the appropriate properties
        for (int i = 0; i < 4; i++) {
            // Load position
            vertices[offset + 0] = face.vertices[i * 3 + 0];
            vertices[offset + 1] = face.vertices[i * 3 + 1];
            vertices[offset + 2] = face.vertices[i * 3 + 2];

            // Load color
            vertices[offset + 3] = face.texCoords[i * 2 + 0];
            vertices[offset + 4] = face.texCoords[i * 2 + 1];

            offset += VERTEX_SIZE;
        }
    }

    private int[] generateIndices() {
        // VERTEX_SIZE indices per quad (3 per triangle)
        int[] elements = new int[6 * maxBatchSize];
        for (int i = 0; i < maxBatchSize; i++) {
            loadElementIndices(elements, i);
        }

        return elements;
    }

    private void loadElementIndices(int[] elements, int index) {
        int offsetArrayIndex = 6 * index;
        int offset = 4 * index;

        // 3, 2, 0, 0, 2, 1 7, VERTEX_SIZE, 4, 4, VERTEX_SIZE, 5
        // Triangle 1
        elements[offsetArrayIndex] = offset + 3;
        elements[offsetArrayIndex + 1] = offset + 2;
        elements[offsetArrayIndex + 2] = offset + 0;

        // Triangle 2
        elements[offsetArrayIndex + 3] = offset + 0;
        elements[offsetArrayIndex + 4] = offset + 2;
        elements[offsetArrayIndex + 5] = offset + 1;
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
