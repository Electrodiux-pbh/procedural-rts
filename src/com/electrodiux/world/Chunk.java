package com.electrodiux.world;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.electrodiux.block.BlockDefinition;
import com.electrodiux.block.BlockRegister;
import com.electrodiux.block.Blocks;

public class Chunk implements Externalizable {

    public static final int CHUNK_SIZE = 16; // 16 es potencia natural de 2
    public static final int CHUNK_AREA = CHUNK_SIZE * CHUNK_SIZE;
    public static final int CHUNK_HEIGHT = 256; // es potencia natural de 2

    public static final int CHUNK_PALLETE_BATCH_SIZE = 10;

    public static final int CHUNK_FORMAT_VERSION = 0;

    private short[] blocks;

    private int xPos, zPos;

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
        return xPos * CHUNK_SIZE + localX;
    }

    public int getWorldZFromLocal(int localZ) {
        return zPos * CHUNK_SIZE + localZ;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int version = in.readInt();
        if (version != CHUNK_FORMAT_VERSION)
            return;

        if (in.readObject() instanceof short[] blocks) {
            this.blocks = (short[]) blocks;
        } else {
            throw new ClassNotFoundException("Chunk blocks are not of type short[]");
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.write(CHUNK_FORMAT_VERSION);
        out.writeObject(this.blocks);
    }

    public static int getChunkXFromWorld(int blockX) {
        return Math.floorDiv(blockX, CHUNK_SIZE);
    }

    public static int getChunkZFromWorld(int blockZ) {
        return Math.floorDiv(blockZ, CHUNK_SIZE);
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

    public static int getBlockIndexWithWorldCoords(int x, int y, int z) {
        return getBlockIndex(Math.floorMod(x, Chunk.CHUNK_SIZE), y, Math.floorMod(z, Chunk.CHUNK_SIZE));
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