package com.electrodiux.world;

import com.electrodiux.block.Blocks;

public class Chunk {

    public static final int CHUNK_SIZE = 16;
    public static final int CHUNK_SIZE_BYTESHIFT = 4;
    public static final int CHUNK_AREA = CHUNK_SIZE * CHUNK_SIZE;
    public static final int CHUNK_HEIGHT = 128;

    private final short[] blocks;

    private final int chunkX, chunkZ;

    public Chunk(int x, int z) {
        this.chunkX = x;
        this.chunkZ = z;
        blocks = new short[CHUNK_AREA * CHUNK_HEIGHT];
    }

    public short[] getBlocks() {
        return blocks;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public int getBlockX() {
        return chunkX * CHUNK_SIZE;
    }

    public int getBlockZ() {
        return chunkZ * CHUNK_SIZE;
    }

    public short getBlock(int x, int y, int z) {
        if (outOfBounds(x, y, z))
            return Blocks.AIR;
        return blocks[getBlockIndex(x, y, z)];
    }

    public int getHightestYAt(int x, int z) {
        for (int y = CHUNK_HEIGHT - 1; y >= 0; y--) {
            if (blocks[Chunk.getBlockIndex(x, y, z)] != Blocks.AIR) {
                return y;
            }
        }
        return 0;
    }

    public int getHashKey() {
        return Chunk.getHashKey(chunkX, chunkZ);
    }

    public static int getHashKey(int x, int z) {
        return 31 * Double.valueOf(x).hashCode()
                + Double.valueOf(z).hashCode();
    }

    public static int getBlockIndex(int x, int y, int z) {
        return x + (z << CHUNK_SIZE_BYTESHIFT) + CHUNK_AREA * y;
    }

    public static boolean outOfBounds(int x, int y, int z) {
        return x < 0 || y < 0 || z < 0 || x >= CHUNK_SIZE || y >= CHUNK_HEIGHT || z >= CHUNK_SIZE;
    }

    public static int getBlockIndexWithWorldCoords(int x, int y, int z) {
        // x = -16 => x = -15 beacuse negative chunks starts at
        // -1 and positive chunks starts at 0

        if (x < 0) {
            x = CHUNK_SIZE - 1 - ((-x - 1) % CHUNK_SIZE);
        } else {
            x = x % CHUNK_SIZE;
        }

        if (z < 0) {
            z = CHUNK_SIZE - 1 - ((-z - 1) % CHUNK_SIZE);
        } else {
            z = z % CHUNK_SIZE;
        }

        return getBlockIndex(x, y, z);
    }

}