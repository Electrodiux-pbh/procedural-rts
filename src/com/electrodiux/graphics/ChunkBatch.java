package com.electrodiux.graphics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL13;

import com.electrodiux.block.BlockDefinition;
import com.electrodiux.block.BlockRegister;
import com.electrodiux.block.Blocks;
import com.electrodiux.graphics.RenderBatch.Face;
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
    private Face face = new Face(null, null);

    private void addVisibleFaces(World world, Chunk chunk, int x, int y, int z, BlockDefinition block) {
        transformMatrix.identity();
        transformMatrix.translate(x + chunk.getBlockX(), y, z + chunk.getBlockZ());

        if (isVisibleFace(world, chunk, x, y + 1, z, block))
            addFace(getFace(Blocks.FACE_TOP, block));
        if (isVisibleFace(world, chunk, x, y - 1, z, block))
            addFace(getFace(Blocks.FACE_BOTTOM, block));
        if (isVisibleFace(world, chunk, x, y, z + 1, block))
            addFace(getFace(Blocks.FACE_FRONT, block));
        if (isVisibleFace(world, chunk, x, y, z - 1, block))
            addFace(getFace(Blocks.FACE_BACK, block));
        if (isVisibleFace(world, chunk, x + 1, y, z, block))
            addFace(getFace(Blocks.FACE_RIGHT, block));
        if (isVisibleFace(world, chunk, x - 1, y, z, block))
            addFace(getFace(Blocks.FACE_LEFT, block));
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

    private Face getFace(int faceIdx, BlockDefinition block) {
        float[] vertices = new float[12];
        System.arraycopy(cubeVertices, faceIdx * 12, vertices, 0, 12);

        Sprite texture = block.getTexture(faceIdx);
        if (texture == null)
            return null;

        float[] texCoords = texture.getTexCoords();

        face.set(getVertices(vertices), texCoords);

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

}
