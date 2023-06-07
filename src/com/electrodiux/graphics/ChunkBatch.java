package com.electrodiux.graphics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import com.electrodiux.block.BlockRegister;
import com.electrodiux.block.Blocks;
import com.electrodiux.graphics.RenderBatch.Face;
import com.electrodiux.world.Chunk;
import com.electrodiux.world.World;

public class ChunkBatch {

    private static final int MAX_BATCH_SIZE = 1024 * 5;

    private List<RenderBatch> batches;
    private int facesCount = 0;

    public ChunkBatch() {
        batches = new ArrayList<RenderBatch>();
    }

    public void render(Texture texture) {
        for (RenderBatch batch : batches) {
            batch.render(texture);
        }
    }

    public void computeMesh(Chunk chunk, World world) {
        facesCount = 0;

        for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                for (int y = 0; y < world.getWorldHeight(); y++) {
                    short[] blocks = chunk.getBlocks();

                    int index = Chunk.getBlockIndex(x, y, z);

                    if (blocks[index] == Blocks.AIR) {
                        continue;
                    }

                    Texture texture = BlockRegister.blocksMetadata[blocks[index]].getTexture();

                    if (texture == null) {
                        continue;
                    }

                    addVisibleFaces(world, chunk, x, y, z, texture);
                }
            }
        }

        rebufferBatches();
    }

    private Matrix4f transformMatrix = new Matrix4f();
    private Face face = new Face(null, null);

    private void addVisibleFaces(World world, Chunk chunk, int x, int y, int z, Texture texture) {
        transformMatrix.identity();
        transformMatrix.translate(x + chunk.getBlockX(), y, z + chunk.getBlockZ());

        if (isHideBlock(world, chunk, x, y + 1, z))
            addFace(getFace(FACE_TOP));
        if (isHideBlock(world, chunk, x, y - 1, z))
            addFace(getFace(FACE_BOTTOM));
        if (isHideBlock(world, chunk, x, y, z + 1))
            addFace(getFace(FACE_FRONT));
        if (isHideBlock(world, chunk, x, y, z - 1))
            addFace(getFace(FACE_BACK));
        if (isHideBlock(world, chunk, x + 1, y, z))
            addFace(getFace(FACE_RIGHT));
        if (isHideBlock(world, chunk, x - 1, y, z))
            addFace(getFace(FACE_LEFT));
    }

    private boolean isHideBlock(World world, Chunk chunk, int x, int y, int z) {
        short block = chunk.getBlockId(x, y, z);
        if (block == -1) {
            // block = world.getBlock(x + chunk.getBlockX(), y, z + chunk.getBlockZ());ç
            return true;
        }
        return block == Blocks.AIR || BlockRegister.getBlock(block).isTransparent();
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
            RenderBatch newBatch = new RenderBatch(MAX_BATCH_SIZE);
            newBatch.allocateBatch();
            batches.add(newBatch);
            newBatch.addFace(face);
        }
        facesCount++;
    }

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
            vec.mul(transformMatrix);

            v[i + 0] = vec.x;
            v[i + 1] = vec.y;
            v[i + 2] = vec.z;
        }
        return v;
    }

    public void rebufferBatches() {
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
                batch.clearBatch();
                iter.remove();
            }
            batch.rebufferData();
            i++;
        }
    }

    // #region Block Faces

    private static final int FACE_TOP = 4;
    private static final int FACE_BOTTOM = 5;
    private static final int FACE_RIGHT = 3;
    private static final int FACE_LEFT = 2;
    private static final int FACE_FRONT = 0;
    private static final int FACE_BACK = 1;

    private static final float[] cubeVertices = {
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

    private static final float[] cubeTexCoords = {
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

    // #endregion

}
