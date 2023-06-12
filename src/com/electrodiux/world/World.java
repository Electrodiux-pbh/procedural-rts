package com.electrodiux.world;

import static com.electrodiux.world.Chunk.CHUNK_HEIGHT;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.electrodiux.Position;
import com.electrodiux.block.Blocks;
import com.electrodiux.entities.Entity;
import com.electrodiux.generation.TerrainGenerator;

public class World {

    private transient TerrainGenerator generator;

    private final Map<UUID, Entity> entities;
    private final Map<ChunkIndex, Chunk> chunks;

    private long seed;

    private long worldTicks;

    public World(long seed) {
        this.seed = seed;
        this.chunks = new ConcurrentHashMap<>();
        this.entities = new ConcurrentHashMap<>();
    }

    public void setGenerator(TerrainGenerator generator) {
        this.generator = generator;
    }

    public void loadChunk(int x, int z) {
        ChunkIndexSearch idx = getChunkIndexSearch(x, z);
        if (chunks.containsKey(idx)) {
            return;
        } else {
            Chunk chunk = generator.generateChunk(x, z);
            chunks.put(idx, chunk);
        }
    }

    public void unloadChunk(int x, int z) {
        chunks.remove(getChunkIndexSearch(x, z));
    }

    public void unloadChunk(Chunk chunk) {
        chunks.remove(getChunkIndexSearch(chunk.getXPos(), chunk.getBlockZ()));
    }

    public void generate(int radius) {
        CountDownLatch latch = new CountDownLatch((radius * 2 + 1) * (radius * 2 + 1));
        ExecutorService chunkExecutor = Executors.newFixedThreadPool(10);

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                final int xPos = x;
                final int zPos = z;

                chunkExecutor.submit(() -> {
                    Chunk chunk = generator.generateChunk(xPos, zPos);
                    chunks.put(getChunkIndex(xPos, zPos), chunk);
                    latch.countDown();
                });
            }
        }

        try {
            latch.await();
            chunkExecutor.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static ChunkIndex getChunkIndex(int x, int z) {
        return new ChunkIndex(x, z);
    }

    private ChunkIndexSearch getChunkIndexSearch(int x, int z) {
        return new ChunkIndexSearch(x, z);
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
        return getChunkFromWorldCoords(x, z).getBlocks()[Chunk.getBlockIndexWithWorldCoords(x, y, z)] != Blocks.AIR;
    }

    public void setBlock(int x, int y, int z, short block) {
        if (outOfBounds(y))
            return;
        setBlock0(x, y, z, block);
    }

    private void setBlock0(int x, int y, int z, short block) {
        getChunkFromWorldCoords(x, z).getBlocks()[Chunk.getBlockIndexWithWorldCoords(x, y, z)] = block;
    }

    public int getHighestYAt(int x, int z) {
        Chunk chunk = getChunkFromWorldCoords(x, z);
        if (chunk == null)
            return Blocks.NULL;

        short[] blocks = chunk.getBlocks();
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

    public Chunk getChunkFromWorldCoords(int x, int z) {
        int chunkX = (int) Math.floor(x / (float) Chunk.CHUNK_SIZE);
        int chunkZ = (int) Math.floor(z / (float) Chunk.CHUNK_SIZE);

        return getChunk(chunkX, chunkZ);
    }

    public short getBlock(int x, int y, int z) {
        if (outOfBounds(y))
            return Blocks.AIR;
        return getBlock0(x, y, z);
    }

    public short getBlock(Position pos) {
        return getBlock(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
    }

    private short getBlock0(int x, int y, int z) {
        Chunk chunk = getChunkFromWorldCoords(x, z);

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

    // #region Entities

    public void addEntity(Entity entity) {
        this.entities.put(entity.getUUID(), entity);
    }

    public Collection<Entity> getEntities() {
        return entities.values();
    }

    // #endregion

    // #region Ticking

    public void tick() {
        worldTicks++;

        for (Entity entity : entities.values()) {
            short block = getBlock(entity.getPosition());

            float displacement = 9.8f / 20f;
            if (block == Blocks.AIR) {
                entity.getPosition().add(0, -displacement, 0);
            }
        }
    }

    // #endregion
}
