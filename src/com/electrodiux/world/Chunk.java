package com.electrodiux.world;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.ByteBuffer;
import java.util.Objects;

import org.lwjgl.system.MemoryUtil;

import com.electrodiux.Manager;
import com.electrodiux.block.BlockDefinition;
import com.electrodiux.block.BlockRegister;
import com.electrodiux.block.Blocks;
import com.electrodiux.events.ChunkLoadEvent;
import com.electrodiux.events.ChunkUnloadEvent;

public class Chunk implements Externalizable {

    public static final int CHUNK_SIZE = 16; // 16 es potencia natural de 2
    public static final int CHUNK_AREA = CHUNK_SIZE * CHUNK_SIZE;
    public static final int CHUNK_HEIGHT = 256; // es potencia natural de 2

    public static final byte MAX_LIGHT_LEVEL = 15;
    public static final byte MIN_LIGHT_LEVEL = 0;

    public static final int CHUNK_FORMAT_VERSION = 0;

    private transient World world;

    private short[] blocks;
    private ByteBuffer light;

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

    public Chunk(World world, int x, int z) {
        this.world = world;
        this.xPos = x;
        this.zPos = z;

        this.status = ChunkStatus.EMPTY;

        blocks = new short[CHUNK_AREA * CHUNK_HEIGHT];
        light = MemoryUtil.memAlloc(CHUNK_AREA * CHUNK_HEIGHT);
        MemoryUtil.memSet(light, 0x00);
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

    public short getBlockId(int idx) {
        Objects.checkIndex(idx, blocks.length);
        return blocks[idx];
    }

    public BlockDefinition getBlock(int x, int y, int z) {
        short id = getBlockId(x, y, z);
        if (id == -1)
            return null;
        return BlockRegister.getBlock(id);
    }

    public BlockDefinition getBlock(int idx) {
        short id = getBlockId(idx);
        if (id == -1)
            return null;
        return BlockRegister.getBlock(id);
    }

    public void setBlock(short block, int x, int y, int z) {
        if (outOfBounds(x, y, z))
            return;
        blocks[getBlockIndex(x, y, z)] = block;
    }

    public void setBlock(BlockDefinition block, int x, int y, int z) {
        this.setBlock(block.getNumericBlockId(), x, y, z);
    }

    public int getHightestYAt(int x, int z) {
        for (int y = CHUNK_HEIGHT - 1; y >= 0; y--) {
            if (blocks[Chunk.getBlockIndex(x, y, z)] != Blocks.AIR) {
                return y;
            }
        }
        return 0;
    }

    // #region Light

    public byte getLight(int idx) {
        Objects.checkIndex(idx, light.limit());
        return light.get(idx);
    }

    public byte getLight(int x, int y, int z) {
        if (outOfBounds(x, y, z))
            return 0x00;
        return light.get(getBlockIndex(x, y, z));
    }

    public byte getBlockLight(int x, int y, int z) {
        if (outOfBounds(x, y, z))
            return 0;
        return getBlockLightFromLight(light.get(getBlockIndex(x, y, z)));
    }

    public byte getSkyLight(int x, int y, int z) {
        if (outOfBounds(x, y, z))
            return 0;
        return light.get(getBlockIndex(x, y, z));
    }

    public void setLight(int x, int y, int z, byte light) {
        if (outOfBounds(x, y, z))
            return;
        this.light.put(getBlockIndex(x, y, z), light);
    }

    public void setLight(int x, int y, int z, byte blockLight, byte skyLight) {
        if (outOfBounds(x, y, z))
            return;
        this.light.put(getBlockIndex(x, y, z), getLight(blockLight, skyLight));
    }

    public void setSkyLight(int x, int y, int z, byte skyLight) {
        if (outOfBounds(x, y, z))
            return;

        int idx = getBlockIndex(x, y, z);

        byte blockLight = getBlockLightFromLight(this.light.get(idx));

        this.light.put(idx, skyLight);
    }

    public void setBlockLight(int x, int y, int z, byte blockLight) {
        if (outOfBounds(x, y, z))
            return;

        int idx = getBlockIndex(x, y, z);

        byte skyLight = getSkyLightFromLight(this.light.get(idx));

        this.light.put(idx, getLight(blockLight, getLight(blockLight, skyLight)));
    }

    public static byte getLight(byte blockLight, byte skyLight) {
        return (byte) (((skyLight & 0xF0) << 4) | (blockLight & 0x0F));
    }

    public static byte getBlockLightFromLight(byte light) {
        return (byte) (light & 0x0F);
    }

    public static byte getSkyLightFromLight(byte light) {
        return (byte) ((light & 0xF0) >> 4);
    }

    public short getBlockIdWithAdyacent(int x, int y, int z) {
        if (outOfBounds(y))
            return Blocks.AIR;

        if (outOfBounds(x, z))
            return world.getBlock(xPos * CHUNK_SIZE + x, y, zPos * CHUNK_SIZE + z);

        return blocks[getBlockIndex(x, y, z)];
    }

    public BlockDefinition getBlockWithAdyacent(int x, int y, int z) {
        if (outOfBounds(y))
            return null;

        if (outOfBounds(x, z))
            return BlockRegister.getBlock(world.getBlock(xPos * CHUNK_SIZE + x, y, zPos * CHUNK_SIZE + z));

        return BlockRegister.getBlock(blocks[getBlockIndex(x, y, z)]);
    }

    public byte getLightWithAdyacent(int x, int y, int z) {
        if (outOfBounds(y))
            return Chunk.MIN_LIGHT_LEVEL;

        if (outOfBounds(x, z))
            return world.getLight(xPos * CHUNK_SIZE + x, y, zPos * CHUNK_SIZE + z);

        return light.get(getBlockIndex(x, y, z));
    }

    // #endregion

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

    public static boolean outOfBounds(int x, int z) {
        return x < 0 || x >= CHUNK_SIZE || z < 0 || z >= CHUNK_SIZE;
    }

    public static boolean outOfBounds(int y) {
        return y < 0 || y >= CHUNK_HEIGHT;
    }

    public static int getBlockIndexWithWorldCoords(int x, int y, int z) {
        return getBlockIndex(Math.floorMod(x, Chunk.CHUNK_SIZE), y, Math.floorMod(z, Chunk.CHUNK_SIZE));
    }

    public void load() {
        Manager.eventManager().fireEvent(new ChunkLoadEvent(this));
    }

    public void unload() {
        MemoryUtil.memFree(light);

        Manager.eventManager().fireEvent(new ChunkUnloadEvent(this));
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