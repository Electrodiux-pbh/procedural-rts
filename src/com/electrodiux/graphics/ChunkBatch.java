package com.electrodiux.graphics;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import com.electrodiux.block.BlockDefinition;
import com.electrodiux.block.BlockRegister;
import com.electrodiux.block.Blocks;
import com.electrodiux.graphics.textures.Sprite;
import com.electrodiux.graphics.textures.Texture;
import com.electrodiux.world.Chunk;
import com.electrodiux.world.World;

public class ChunkBatch {

    private static final int MAX_BATCH_SIZE = 1000;

    private List<RenderBatch> batches;
    private int facesCount = 0;
    private volatile boolean buffered = false;
    private volatile boolean isComputed = false;
    private volatile boolean deletedBatch = false;

    public ChunkBatch() {
        batches = new ArrayList<RenderBatch>();
    }

    public synchronized void render(Texture texture) {
        if (!buffered)
            return;

        GL13.glActiveTexture(GL13.GL_TEXTURE0);

        if (texture != null)
            texture.bind();

        for (RenderBatch batch : batches) {
            batch.render(texture);
        }

        if (texture != null)
            texture.unbind();
    }

    public synchronized void computeMesh(Chunk chunk, World world) {
        facesCount = 0;
        isComputed = false;

        resetBatches();

        chunk.calcLight();

        for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                for (int y = 0; y < world.getWorldHeight(); y++) {
                    short[] blocks = chunk.getBlocks();

                    int index = Chunk.getBlockIndex(x, y, z);

                    if (blocks[index] == Blocks.AIR) {
                        continue;
                    }

                    addVisibleFaces(world, chunk, x, y, z, BlockRegister.blocksMetadata[blocks[index]]);
                }
            }
        }

        buffered = false;
        isComputed = true;
    }

    private void resetBatches() {
        for (RenderBatch batch : batches) {
            batch.resetBatch();
        }
    }

    private Matrix4f transformMatrix = new Matrix4f();
    private Face face = new Face(null, null, 0);

    private void addVisibleFaces(World world, Chunk chunk, int x, int y, int z, BlockDefinition block) {
        transformMatrix.identity();
        transformMatrix.translate(x + chunk.getBlockX(), y, z + chunk.getBlockZ());

        byte light = chunk.getLight(x, y, z);

        if (isVisibleFace(world, chunk, x, y + 1, z, block))
            addFace(getFace(Blocks.FACE_TOP, block, light));
        if (isVisibleFace(world, chunk, x, y - 1, z, block))
            addFace(getFace(Blocks.FACE_BOTTOM, block, light));
        if (isVisibleFace(world, chunk, x, y, z + 1, block))
            addFace(getFace(Blocks.FACE_FRONT, block, light));
        if (isVisibleFace(world, chunk, x, y, z - 1, block))
            addFace(getFace(Blocks.FACE_BACK, block, light));
        if (isVisibleFace(world, chunk, x + 1, y, z, block))
            addFace(getFace(Blocks.FACE_RIGHT, block, light));
        if (isVisibleFace(world, chunk, x - 1, y, z, block))
            addFace(getFace(Blocks.FACE_LEFT, block, light));
    }

    private boolean isVisibleFace(World world, Chunk chunk, int x, int y, int z, BlockDefinition block) {
        short otherBlock = chunk.getBlockId(x, y, z);
        if (otherBlock == Blocks.NULL) {
            otherBlock = world.getBlock(chunk.getWorldXFromLocal(x), y,
                    chunk.getWorldZFromLocal(z));
        }

        if (otherBlock == Blocks.AIR) {
            return true;
        }

        BlockDefinition otherBlockDefinition = BlockRegister.getBlock(otherBlock);

        if (otherBlockDefinition.isTransparent()) {
            if (block.hasInternalFaces()) {
                return true;
            }

            if (otherBlockDefinition != block) {
                return true;
            }
        }

        return false;
    }

    private void addFace(Face face) {
        if (face == null) {
            return;
        }

        boolean added = false;
        for (RenderBatch batch : batches) {
            if (batch.hasRoom()) {
                batch.addFace(face);
                added = true;
                break;
            }
        }

        if (!added) {
            RenderBatch newBatch = new RenderBatch(MAX_BATCH_SIZE);
            newBatch.addFace(face);
            batches.add(newBatch);
        }

        facesCount++;
    }

    private Face getFace(int faceIdx, BlockDefinition block, byte light) {
        float[] vertices = new float[12];
        System.arraycopy(cubeVertices, faceIdx * 12, vertices, 0, 12);

        Sprite texture = block.getTexture(faceIdx);
        if (texture == null)
            return null;

        float[] texCoords = texture.getTexCoords();

        face.set(getVertices(vertices), texCoords, light);

        return face;
    }

    private float[] getVertices(float[] vertices) {
        Vector4f vec = new Vector4f();

        for (int i = 0; i < vertices.length; i += 3) {
            vec.set(vertices[i + 0], vertices[i + 1], vertices[i + 2], 1);
            vec.mul(transformMatrix);

            vertices[i + 0] = vec.x;
            vertices[i + 1] = vec.y;
            vertices[i + 2] = vec.z;
        }

        return vertices;
    }

    public synchronized void bufferData() {
        Iterator<RenderBatch> iter = batches.iterator();
        int numBatches = (int) Math.ceilDiv(facesCount, MAX_BATCH_SIZE);

        for (int i = 0; iter.hasNext(); i++) {
            RenderBatch batch = iter.next();
            if (i > numBatches) {
                batch.clearBufferData();
                iter.remove();
                continue;
            }

            if (!batch.isAllocated()) {
                batch.allocateBatch();
            }

            batch.bufferData();
        }

        buffered = true;
    }

    public synchronized void clearBufferData() {
        deletedBatch = true;

        Iterator<RenderBatch> iter = batches.iterator();
        while (iter.hasNext()) {
            RenderBatch batch = iter.next();
            batch.clearBufferData();
            iter.remove();
        }
    }

    public boolean isBuffered() {
        return this.buffered;
    }

    public boolean isComputed() {
        return this.isComputed;
    }

    public boolean isDeleted() {
        return this.deletedBatch;
    }

    // #region Block Faces

    private static final float[] cubeVertices = {
            // Front face
            1.0f, 0.0f, 1.0f, // Bottom-right
            0.0f, 0.0f, 1.0f, // Bottom-left
            0.0f, 1.0f, 1.0f, // Top-left
            1.0f, 1.0f, 1.0f, // Top-right

            // Back face
            0.0f, 0.0f, 0.0f, // Bottom-left
            1.0f, 0.0f, 0.0f, // Bottom-right
            1.0f, 1.0f, 0.0f, // Top-right
            0.0f, 1.0f, 0.0f, // Top-left

            // Left face
            0.0f, 0.0f, 1.0f, // Bottom-back
            0.0f, 0.0f, 0.0f, // Bottom-front
            0.0f, 1.0f, 0.0f, // Top-front
            0.0f, 1.0f, 1.0f, // Top-back

            // Right face
            1.0f, 0.0f, 0.0f, // Bottom-front
            1.0f, 0.0f, 1.0f, // Bottom-back
            1.0f, 1.0f, 1.0f, // Top-back
            1.0f, 1.0f, 0.0f, // Top-front

            // Top face
            1.0f, 1.0f, 1.0f, // Front-right
            0.0f, 1.0f, 1.0f, // Front-left
            0.0f, 1.0f, 0.0f, // Back-left
            1.0f, 1.0f, 0.0f, // Back-right

            // Bottom face
            1.0f, 0.0f, 0.0f, // Back-right
            0.0f, 0.0f, 0.0f, // Back-left
            0.0f, 0.0f, 1.0f, // Front-left
            1.0f, 0.0f, 1.0f, // Front-right
    };

    // #endregion

    private static class RenderBatch {
        private static final int POS_SIZE = 3;
        private static final int POS_OFFSET = 0;

        private static final int VERTICES_PER_FACE = 4;

        private static final int TEXTURE_SIZE = 2;
        private static final int TEXTURE_OFFSET = POS_OFFSET + POS_SIZE * Float.BYTES;

        private static final int ATTRIBUTES_SIZE = 1;
        private static final int ATTRIBUTES_OFFSET = TEXTURE_OFFSET + TEXTURE_SIZE * Float.BYTES;

        private static final int VERTEX_STRIDE = (POS_SIZE + TEXTURE_SIZE) * Float.BYTES
                + (ATTRIBUTES_SIZE) * Integer.BYTES;

        private int numSprites;
        private boolean isAllocated;
        private ByteBuffer vertices;

        private int vaoID, vboID, eboID;
        private int maxBatchSize;

        public RenderBatch(int maxBatchSize) {
            this.maxBatchSize = maxBatchSize;

            this.numSprites = 0;

            // 4 vertices quads
            vertices = MemoryUtil.memAlloc(maxBatchSize * VERTICES_PER_FACE * VERTEX_STRIDE);
        }

        public void allocateBatch() {
            if (!isAllocated()) {
                // Generate and bind a Vertex Array Object
                vaoID = GL30.glGenVertexArrays();
                GL30.glBindVertexArray(vaoID);

                // Allocate space for vertices
                vboID = GL30.glGenBuffers();
                GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboID);
                GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertices.capacity(),
                        GL30.GL_DYNAMIC_DRAW);

                // Create and upload indices buffer
                eboID = GL30.glGenBuffers();
                int[] indices = generateIndices();
                GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, eboID);
                GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, indices,
                        GL30.GL_STATIC_DRAW);

                // Enable the buffer attribute pointers
                GL30.glVertexAttribPointer(0, POS_SIZE, GL30.GL_FLOAT, false, VERTEX_STRIDE, POS_OFFSET);
                GL30.glEnableVertexAttribArray(0);

                GL30.glVertexAttribPointer(1, TEXTURE_SIZE, GL30.GL_FLOAT, false, VERTEX_STRIDE, TEXTURE_OFFSET);
                GL30.glEnableVertexAttribArray(1);

                GL30.glVertexAttribIPointer(2, ATTRIBUTES_SIZE, GL30.GL_INT, VERTEX_STRIDE, ATTRIBUTES_OFFSET);
                GL30.glEnableVertexAttribArray(2);

                isAllocated = true;
            }
        }

        public void clearBufferData() {
            if (isAllocated()) {
                GL30.glDeleteBuffers(vboID);
                GL30.glDeleteBuffers(eboID);
                GL30.glDeleteVertexArrays(vaoID);
            }
            MemoryUtil.memFree(vertices);
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
                GL30.glEnableVertexAttribArray(2);

                GL30.glDrawElements(GL30.GL_TRIANGLES, this.numSprites * 6, GL30.GL_UNSIGNED_INT, 0);

                GL30.glDisableVertexAttribArray(0);
                GL30.glDisableVertexAttribArray(1);
                GL30.glDisableVertexAttribArray(2);

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
            int offset = index * VERTICES_PER_FACE * VERTEX_STRIDE;

            for (int i = 0; i < VERTICES_PER_FACE; i++) {
                int vertexOffset = offset + i * VERTEX_STRIDE;

                vertices.putFloat(vertexOffset, face.vertices[i * 3]);
                vertexOffset += Float.BYTES;
                vertices.putFloat(vertexOffset, face.vertices[i * 3 + 1]);
                vertexOffset += Float.BYTES;
                vertices.putFloat(vertexOffset, face.vertices[i * 3 + 2]);
                vertexOffset += Float.BYTES;

                vertices.putFloat(vertexOffset, face.texCoords[i * 2]);
                vertexOffset += Float.BYTES;
                vertices.putFloat(vertexOffset, face.texCoords[i * 2 + 1]);
                vertexOffset += Float.BYTES;

                // 20 bits of 32 available
                // Sky | Light | Light Color
                // 0000--0000----0000-0000-0000

                int attributes = 0;
                attributes |= face.lightLevel << 24;
                attributes |= (face.lightColor & 0x0FFF) << 12;

                // static debug value
                vertices.putInt(vertexOffset, attributes);
                vertexOffset += Integer.BYTES;
            }
        }

        private int[] generateIndices() {
            int[] elements = new int[6 * maxBatchSize];

            for (int i = 0; i < maxBatchSize; i++) {
                final int offset = VERTICES_PER_FACE * i;
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
    }

    private static class Face {

        private float[] vertices;
        private float[] texCoords;

        private byte lightLevel;
        private short lightColor;

        public Face(float[] vertices, float[] texCoords, int lightLevel) {
            this.vertices = vertices;
            this.texCoords = texCoords;
            this.lightLevel = (byte) lightLevel;

            this.lightColor = (short) 0xfff;
        }

        public void set(float[] vertices, float[] texCoords, int lightLevel) {
            this.vertices = vertices;
            this.texCoords = texCoords;
            this.lightLevel = (byte) lightLevel;
        }

    }

}
