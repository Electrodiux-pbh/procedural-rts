package com.electrodiux.world;

import java.io.Serializable;

import com.electrodiux.block.BlockDefinition;
import com.electrodiux.block.BlockRegister;
import com.electrodiux.block.Blocks;

public class Chunk implements Serializable {

    public static final int CHUNK_SIZE = 16; // 16 es potencia natural de 2
    public static final int CHUNK_SIZE_BITMASK = 15; // 16 - 1
    public static final int CHUNK_AREA = CHUNK_SIZE * CHUNK_SIZE;
    public static final int CHUNK_HEIGHT = 256; // es potencia natural de 2

    public static final int CHUNK_PALLETE_BATCH_SIZE = 10;

    private final short[] blocks;

    private final int xPos, zPos;

    private ChunkStatus status;

    public static enum ChunkStatus {
        EMPTY,
        NOISE,
        SURFACE,
        CARVERS,
        HEIGHTMAPS,
        COMPLETE
    }

    public Chunk(int x, int z) {
        this.xPos = x;
        this.zPos = z;
        status = ChunkStatus.EMPTY;

        blocks = new short[CHUNK_AREA * CHUNK_HEIGHT];
    }

    public short[] getBlocks() {
        return blocks;
    }

    public int getXPos() {
        return xPos;
    }

    public int getZPos() {
        return zPos;
    }

    public int getBlockX() {
        return xPos * CHUNK_SIZE;
    }

    public int getBlockZ() {
        return zPos * CHUNK_SIZE;
    }

    public short getBlockId(int x, int y, int z) {
        if (outOfBounds(x, y, z))
            return Blocks.NULL;
        return blocks[getBlockIndex(x, y, z)];
    }

    public BlockDefinition getBlock(int x, int y, int z) {
        short id = getBlockId(x, y, z);
        if (id == -1)
            return null;
        return BlockRegister.getBlock(id);
    }

    public void setBlock(short block, int x, int y, int z) {
        if (outOfBounds(x, y, z))
            return;
        blocks[getBlockIndex(x, y, z)] = block;
    }

    public int getHightestYAt(int x, int z) {
        for (int y = CHUNK_HEIGHT - 1; y >= 0; y--) {
            if (blocks[Chunk.getBlockIndex(x, y, z)] != Blocks.AIR) {
                return y;
            }
        }
        return 0;
    }

    public int getWorldXFromLocal(int localX) {
        return getWorldCoord(localX, xPos);
    }

    public int getWorldZFromLocal(int localZ) {
        return getWorldCoord(localZ, zPos);
    }

    private static int getWorldCoord(int local, int chunk) {
        return chunk * CHUNK_SIZE + local;
    }

    public ChunkStatus getChunkStatus() {
        return status;
    }

    public void setChunkStatus(ChunkStatus status) {
        this.status = status;
    }

    public static int getBlockIndex(int x, int y, int z) {
        return x + (z * CHUNK_SIZE) + (y * CHUNK_AREA);
    }

    public static boolean outOfBounds(int x, int y, int z) {
        return y < 0 || y >= CHUNK_HEIGHT || x < 0 || x >= CHUNK_SIZE || z < 0 || z >= CHUNK_SIZE;
    }

    private static int calculateIndexInChunk(int coordinate) {
        // x = -16 => x = -15 because negative chunks starts at
        // -1 and positive chunks starts at 0

        if (coordinate < 0) {
            return CHUNK_SIZE - 1 - (-(coordinate + 1) & CHUNK_SIZE_BITMASK);
        }

        return coordinate & CHUNK_SIZE_BITMASK;
    }

    public static int getBlockIndexWithWorldCoords(int x, int y, int z) {
        return getBlockIndex(calculateIndexInChunk(x), y, calculateIndexInChunk(z));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 17;
        result = prime * result + xPos;
        result = prime * result + zPos;
        return result;
    }
}