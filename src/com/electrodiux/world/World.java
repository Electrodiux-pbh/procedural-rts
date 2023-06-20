package com.electrodiux.world;

import static com.electrodiux.world.Chunk.CHUNK_HEIGHT;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.electrodiux.Position;
import com.electrodiux.block.Blocks;
import com.electrodiux.entities.Entity;
import com.electrodiux.generation.TerrainGenerator;
import com.electrodiux.world.Chunk.ChunkStatus;

public class World {

    private transient TerrainGenerator generator;

    private transient ExecutorService chunkGeneratorService;

    private final Map<UUID, Entity> entities;
    private final Map<ChunkIndex, Chunk> chunks;

    private long seed;

    private long worldTicks;

    public World(long seed) {
        this.seed = seed;
        this.chunks = new ConcurrentHashMap<>();
        this.entities = new ConcurrentHashMap<>();

        this.chunkGeneratorService = Executors.newFixedThreadPool(6);
    }

    public void setGenerator(TerrainGenerator generator) {
        this.generator = generator;
    }

    public void loadChunk(int x, int z) {
        ChunkIndex idx = getChunkIndex(x, z);
        if (chunks.containsKey(idx)) {
            return;
        } else {
            generateChunkAsync(idx);
        }
    }

    public void unloadChunk(int x, int z) {
        Chunk chunk = chunks.remove(getChunkIndex(x, z));
        if (chunk != null) {
            chunk.unload();
        }
    }

    public void unloadChunk(Chunk chunk) {
        Objects.requireNonNull(chunk, "The chunk cannot be null!");
        chunks.remove(getChunkIndex(chunk.getXPos(), chunk.getZPos()));
        chunk.unload();
    }

    public void unloadChunks() {
        for (Chunk chunk : chunks.values()) {
            chunk.unload();
        }
        chunks.clear();
    }

    public void generate(int radius) {
        final int cx = 0;
        final int cz = 0;

        final int size = (radius * 2 + 1);
        CountDownLatch latch = new CountDownLatch(size * size);

        for (int l = 0; l <= radius; l++) {
            int p1x = cx - l;
            int p1z = cz - l;

            int p2x = cx + l;
            int p2z = cz + l;

            for (int i = p1x; i <= p2x; i++) {
                generateChunk(i, p1z, latch);
            }

            for (int i = p1z; i <= p2z; i++) {
                generateChunk(p2x, i, latch);
            }

            for (int i = p2x; i >= p1x; i--) {
                generateChunk(i, p2z, latch);
            }

            for (int i = p2z; i >= p1z; i--) {
                generateChunk(p1x, i, latch);
            }
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void generateChunk(int xPos, int zPos, CountDownLatch latch) {
        chunkGeneratorService.execute(() -> {
            Chunk chunk = generator.generateChunk(xPos, zPos);
            chunks.put(getChunkIndex(xPos, zPos), chunk);
            latch.countDown();
        });
    }

    private void generateChunkAsync(ChunkIndex index) {
        Chunk chunk = new Chunk(this, index.getX(), index.getZ());
        chunks.put(index, chunk);
        chunkGeneratorService.execute(() -> {
            generator.generateChunk(chunk);
            chunk.setChunkStatus(ChunkStatus.COMPLETE);
        });
    }

    public static ChunkIndex getChunkIndex(int x, int z) {
        return new ChunkIndex(x, z);
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
        return chunks.get(getChunkIndex(x, z));
    }

    public Chunk getChunkFromWorldCoords(int x, int z) {
        return getChunk(Chunk.getChunkXFromWorld(x), Chunk.getChunkZFromWorld(z));
    }

    public short getBlock(int x, int y, int z) {
        if (outOfBounds(y))
            return Blocks.AIR;
        return getBlock0(x, y, z);
    }

    public byte getLight(int x, int y, int z) {
        if (outOfBounds(y))
            return Chunk.MIN_LIGHT_LEVEL;
        return getLight0(x, y, z);
    }

    private byte getLight0(int x, int y, int z) {
        Chunk chunk = getChunkFromWorldCoords(x, z);

        if (chunk == null)
            return Chunk.MIN_LIGHT_LEVEL;

        return chunk.getLight(Chunk.getBlockIndexWithWorldCoords(x, y, z));
    }

    public short getBlock(Position pos) {
        return getBlock(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
    }

    private short getBlock0(int x, int y, int z) {
        Chunk chunk = getChunkFromWorldCoords(x, z);

        if (chunk == null)
            return Blocks.AIR;

        return chunk.getBlockId(Chunk.getBlockIndexWithWorldCoords(x, y, z));
    }

    public Collection<Chunk> getLoadedChunks() {
        return chunks.values();
    }

    public int loadedChunksCount() {
        return chunks.size();
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
