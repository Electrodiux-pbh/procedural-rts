package com.electrodiux.graphics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import com.electrodiux.block.BlockDefinition;
import com.electrodiux.block.BlockRegister;
import com.electrodiux.block.Blocks;
import com.electrodiux.graphics.RenderBatch.Face;
import com.electrodiux.graphics.textures.Sprite;
import com.electrodiux.graphics.textures.Texture;
import com.electrodiux.world.Chunk;
import com.electrodiux.world.World;

public class ChunkBatch {

    private static final int MAX_BATCH_SIZE = 1024 * 6;

    private List<RenderBatch> batches;
    private int facesCount = 0;
    private volatile boolean buffered = false;
    private volatile boolean isComputed = false;

    public ChunkBatch() {
        batches = new ArrayList<RenderBatch>();
    }

    public synchronized void render(Texture texture) {
        if (!buffered)
            return;

        for (RenderBatch batch : batches) {
            batch.render(texture);
        }
    }

    public synchronized void computeMesh(Chunk chunk, World world) {
        facesCount = 0;
        isComputed = false;

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
            batches.add(newBatch);
            newBatch.addFace(face);
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
        float[] v = new float[vertices.length];
        Vector4f vec = new Vector4f();

        for (int i = 0; i < vertices.length; i += 3) {
            vec.set(vertices[i + 0], vertices[i + 1], vertices[i + 2], 1);
            vec.mul(transformMatrix);

            v[i + 0] = vec.x;
            v[i + 1] = vec.y;
            v[i + 2] = vec.z;
        }
        return v;
    }

    public synchronized void bufferData() {
        buffered = false;

        Iterator<RenderBatch> iter = batches.iterator();

        double divisionResult = (double) facesCount / MAX_BATCH_SIZE;
        int numBatches = (int) Math.floor(divisionResult);
        if (numBatches < divisionResult) {
            numBatches++;
        }

        int i = 0;
        while (iter.hasNext()) {
            RenderBatch batch = iter.next();
            if (i >= numBatches) {
                batch.clearBufferData();
                iter.remove();
                continue;
            }

            if (!batch.isAllocated()) {
                batch.allocateBatch();
            }

            batch.bufferData();
            i++;
        }

        buffered = true;
    }

    public void clearBufferData() {
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

    // #region Block Faces

    private static final float[] cubeVertices = {
            // Front face
            0.5f, -0.5f, 0.5f, // Bottom-right
            -0.5f, -0.5f, 0.5f, // Bottom-left
            -0.5f, 0.5f, 0.5f, // Top-left
            0.5f, 0.5f, 0.5f, // Top-right

            // Back face
            -0.5f, -0.5f, -0.5f, // Bottom-left
            0.5f, -0.5f, -0.5f, // Bottom-right
            0.5f, 0.5f, -0.5f, // Top-right
            -0.5f, 0.5f, -0.5f, // Top-left

            // Left face
            -0.5f, -0.5f, 0.5f, // Bottom-back
            -0.5f, -0.5f, -0.5f, // Bottom-front
            -0.5f, 0.5f, -0.5f, // Top-front
            -0.5f, 0.5f, 0.5f, // Top-back

            // Right face
            0.5f, -0.5f, -0.5f, // Bottom-front
            0.5f, -0.5f, 0.5f, // Bottom-back
            0.5f, 0.5f, 0.5f, // Top-back
            0.5f, 0.5f, -0.5f, // Top-front

            // Top face
            0.5f, 0.5f, 0.5f, // Front-right
            -0.5f, 0.5f, 0.5f, // Front-left
            -0.5f, 0.5f, -0.5f, // Back-left
            0.5f, 0.5f, -0.5f, // Back-right

            // Bottom face
            0.5f, -0.5f, -0.5f, // Back-right
            -0.5f, -0.5f, -0.5f, // Back-left
            -0.5f, -0.5f, 0.5f, // Front-left
            0.5f, -0.5f, 0.5f, // Front-right
    };

    // #endregion

}
