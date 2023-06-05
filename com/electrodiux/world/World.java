package com.electrodiux.world;

import static com.electrodiux.world.Chunk.CHUNK_HEIGHT;
import static com.electrodiux.world.Chunk.CHUNK_SIZE_BYTESHIFT;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.electrodiux.block.Blocks;

public class World {

    private transient ChunkGenerator generator;

    private final Map<ChunkIndex, Chunk> chunks;

    private long seed;

    public World(long seed) {
        this.seed = seed;

        this.chunks = new ConcurrentHashMap<>();
        this.generator = new ChunkGenerator(seed);
    }

    public void loadChunk(int x, int z) {
        ChunkIndexSearch idx = getChunkIndexSearch(x, z);
        if (chunks.containsKey(idx)) {
            return;
        } else {
            Chunk chunk = generator.createChunk(x, z);
            chunks.put(idx, chunk);
        }
    }

    public void unloadChunk(int x, int z) {
        chunks.remove(getChunkIndexSearch(x, z));
    }

    public void generate() {
        System.out.println("Start generating:");
        final int size = 8;

        final int totalChunks = (size * 2 + 1) * (size * 2 + 1);

        int chunksCount = 0;
        for (int x = -size; x <= size; x++) {
            for (int z = -size; z <= size; z++) {
                Chunk chunk = generator.createChunk(x, z);
                chunks.put(getChunkIndex(x, z), chunk);
                chunksCount++;

                System.out.println("Generated: " + (chunksCount * 100 / totalChunks) + "%");
            }
        }
    }

    public static ChunkIndex getChunkIndex(int x, int z) {
        return new ChunkIndex(x, z);
    }

    private ChunkIndexSearch chunkIdx = new ChunkIndexSearch(0, 0);

    private ChunkIndexSearch getChunkIndexSearch(int x, int z) {
        chunkIdx.x = x;
        chunkIdx.z = z;
        return chunkIdx;
    }

    public static int getXFromIndex(long idx) {
        return (int) (idx >> 32);
    }

    public static int getZFromIndex(long idx) {
        return (int) (idx & 0xFFFFFFFFL);
    }

    public void fill(int x1, int y1, int z1, int x2, int y2, int z2, short block) {
        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                yloop: for (int y = y1; y <= y2; y++) {
                    if (y < 0 || y >= Chunk.CHUNK_HEIGHT) {
                        continue yloop;
                    }
                    setBlock0(x, y, z, block);
                }
            }
        }
    }

    public boolean isBlockAt(int x, int y, int z) {
        if (outOfBounds(y))
            return false;
        return isBlockAt0(x, y, z);
    }

    private boolean isBlockAt0(int x, int y, int z) {
        int chunkX = x >> CHUNK_SIZE_BYTESHIFT;
        int chunkZ = z >> CHUNK_SIZE_BYTESHIFT;

        return getChunk(chunkX, chunkZ).getBlocks()[Chunk.getBlockIndexWithWorldCoords(x, y, z)] != Blocks.AIR;
    }

    public void setBlock(int x, int y, int z, short block) {
        if (outOfBounds(y))
            return;
        setBlock0(x, y, z, block);
    }

    private void setBlock0(int x, int y, int z, short block) {
        int chunkX = x >> CHUNK_SIZE_BYTESHIFT;
        int chunkZ = z >> CHUNK_SIZE_BYTESHIFT;

        getChunk(chunkX, chunkZ).getBlocks()[Chunk.getBlockIndexWithWorldCoords(x, y, z)] = block;
    }

    public int getHighestYAt(int x, int z) {
        int chunkX = x >> CHUNK_SIZE_BYTESHIFT;
        int chunkZ = z >> CHUNK_SIZE_BYTESHIFT;

        short[] blocks = getChunk(chunkX, chunkZ).getBlocks();
        for (int y = CHUNK_HEIGHT - 1; y >= 0; y--) {
            if (blocks[Chunk.getBlockIndexWithWorldCoords(x, y, z)] != Blocks.AIR) {
                return y;
            }
        }
        return 0;
    }

    public Chunk getChunk(int x, int z) {
        return chunks.get(getChunkIndexSearch(x, z));
    }

    public short getBlock(int x, int y, int z) {
        if (outOfBounds(y))
            return Blocks.AIR;
        return getBlock0(x, y, z);
    }

    private short getBlock0(int x, int y, int z) {
        int chunkX = x >> CHUNK_SIZE_BYTESHIFT;
        int chunkZ = z >> CHUNK_SIZE_BYTESHIFT;

        Chunk chunk = getChunk(chunkX, chunkZ);

        if (chunk == null)
            return Blocks.AIR;

        return chunk.getBlocks()[Chunk.getBlockIndexWithWorldCoords(x, y, z)];
    }

    public Collection<Chunk> getChunks() {
        return chunks.values();
    }

    public boolean outOfBounds(int y) {
        return y < 0 || y >= Chunk.CHUNK_HEIGHT;
    }

    public int getWorldHeight() {
        return Chunk.CHUNK_HEIGHT;
    }

    public long getWorldSeed() {
        return seed;
    }
}
