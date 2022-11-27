package com.electrodiux.world;

import static com.electrodiux.world.Chunk.CHUNK_HEIGHT;
import static com.electrodiux.world.Chunk.CHUNK_SIZE;

import com.electrodiux.block.Blocks;
import com.electrodiux.util.NoiseGenerator;

public class ChunkGenerator {

    private long seed;
    private NoiseGenerator noiseGenerator;

    public ChunkGenerator(long seed) {
        this.seed = seed;
        noiseGenerator = new NoiseGenerator(this.seed);
    }

    public Chunk createChunk(int x, int z) {
        Chunk chunk = new Chunk(x, z);
        generate(chunk);
        return chunk;
    }

    public void generateDebug(Chunk chunk, int chkX, int chkZ) {
        fill(chunk, 0, 0, 0, 15, 0, 15,
                ((chunk.getChunkX() + chunk.getChunkZ() % 2) % 2 == 0) ? Blocks.WATER : Blocks.STONE);
    }

    public void generate(Chunk chunk) {
        heightMaps(chunk);
        // NoiseGenerator blockGenerator = new NoiseGenerator(seed, 256, 5, 2.4f, 0.5f);
        // NoiseGenerator heightGenerator = new NoiseGenerator(seed, 256 * 2, 5, 2.4f,
        // 0.5f);
        // blockGenerator.setClampValues(-100, 100);
        // heightGenerator.setClampValues(-100, 100);
        // NoiseGenerator treeGenerator = new NoiseGenerator(seed, 16, 20, 2f, 1.5f);
        // NoiseGenerator oreGenerator = new NoiseGenerator(seed, 16, 3, 2f, 1.5f);

        // NoiseGenerator caveGenerator = new NoiseGenerator(seed * 100, 256, 5, 2.4f,
        // 0.5f);

        // float[] blockNoise = blockGenerator.getNoise2D(CHUNK_SIZE, CHUNK_SIZE, chkX *
        // CHUNK_SIZE, chkZ * CHUNK_SIZE,
        // 100, 3, 2.64f, 0.548f);

        // float[] heightNoise = heightGenerator.getNoise2D(CHUNK_SIZE, CHUNK_SIZE,
        // CHUNK_SIZE *
        // chkX, CHUNK_SIZE * chkZ, 100);

        // short[] blocks = emptyChunk.getBlocks();

        // // HEIGHT
        // for (int j = 0; j < heightNoise.length; j++) {
        // int bkX = (j % CHUNK_SIZE);
        // int bkZ = (j / CHUNK_SIZE);

        // int blocksIndx = bkX + bkZ * CHUNK_SIZE;
        // float block = (blockNoise[blocksIndx] + 100) / 2;

        // int height = ((int) heightNoise[j] + 100) / 2;

        // if (height >= CHUNK_HEIGHT) {
        // height = CHUNK_HEIGHT - 1;
        // }

        // if (block >= 75 && height >= 60) {
        // blocks[Chunk.getBlockIndex(bkX, height, bkZ)] = Blocks.STONE;
        // downFill(emptyChunk, bkX, bkZ, height - 1, Blocks.STONE);
        // continue;
        // }

        // if (height < 47) {
        // blocks[Chunk.getBlockIndex(bkX, 47, bkZ)] = Blocks.WATER;
        // columFill(emptyChunk, bkX, bkZ, height, 47, Blocks.WATER);
        // columFill(emptyChunk, bkX, bkZ, height - 7, height - 3, Blocks.DIRT);
        // columFill(emptyChunk, bkX, bkZ, height - 4, height - 1, Blocks.SAND);
        // downFill(emptyChunk, bkX, bkZ, height - 8, Blocks.STONE);
        // } else if (height < 56 && block < 50) {
        // columFill(emptyChunk, bkX, bkZ, height - 2, height, Blocks.SAND);
        // columFill(emptyChunk, bkX, bkZ, height - 6, height - 3, Blocks.DIRT);
        // downFill(emptyChunk, bkX, bkZ, height - 7, Blocks.STONE);
        // } else {
        // blocks[Chunk.getBlockIndex(bkX, height, bkZ)] = Blocks.GRASS;
        // columFill(emptyChunk, bkX, bkZ, height - 6, height - 1, Blocks.DIRT);
        // downFill(emptyChunk, bkX, bkZ, height - 7, Blocks.STONE);
        // }
        // }

        // // CARVER
        // caveGenerator.setClampValues(-1, 1);
        // float[] cave = caveGenerator.getNoise2D(CHUNK_SIZE, CHUNK_SIZE,
        // emptyChunk.getChunkX() * CHUNK_SIZE,
        // emptyChunk.getChunkZ() * CHUNK_SIZE);
        // caveGenerator.setClampValues(-100, 100);
        // float[] caveHeight = caveGenerator.getNoise2D(CHUNK_SIZE, CHUNK_SIZE,
        // emptyChunk.getChunkX() * CHUNK_SIZE,
        // emptyChunk.getChunkZ() * CHUNK_SIZE, 100, 3, 2.4f, 0.5f);

        // for (int i = 0; i < CHUNK_AREA; i++) {
        // int x = i % CHUNK_SIZE;
        // int z = i / CHUNK_SIZE;

        // if (Math.abs(cave[i]) < 0.15f) {
        // for (int j = (int) (Math.abs(cave[i]) * 70); j >= 0; j--) {
        // int height = (((int) caveHeight[i] + 100) / 2) - 20 - j;
        // short block = getBlock(emptyChunk, x, height, z);
        // if (block == Blocks.STONE || block == Blocks.DIRT || block == Blocks.GRASS) {
        // setBlock(emptyChunk, x, height, z, Blocks.AIR);
        // }
        // }
        // }
        // }

        // // DECOCATOR
        // treeGenerator.setClampValues(-5, 5);
        // float[] trees = treeGenerator.getNoise2D(CHUNK_SIZE, CHUNK_SIZE,
        // emptyChunk.getBlockX(),
        // emptyChunk.getBlockZ());

        // for (int i = 0; i < CHUNK_AREA; i++) {
        // int x = i % CHUNK_SIZE;
        // int z = i / CHUNK_SIZE;

        // int y = getHighestYAt(emptyChunk, x, z);
        // short block = blocks[Chunk.getBlockIndex(x, y, z)];

        // if (block != Blocks.GRASS) {
        // continue;
        // }

        // if (block == Blocks.GRASS) {
        // if (trees[i] > 3) {
        // setBlock(emptyChunk, x, y, z, Blocks.DIRT);
        // columFill(emptyChunk, x, z, y + 1, y + 4, Blocks.LOG);
        // fill(emptyChunk, x - 2, y + 5, z - 2, x + 2, y + 6, z + 2, Blocks.LEAVE);
        // fill(emptyChunk, x - 1, y + 7, z - 1, x + 1, y + 7, z + 1, Blocks.LEAVE);

        // setBlock(emptyChunk, x, y + 8, z, Blocks.LEAVE);
        // setBlock(emptyChunk, x - 1, y + 8, z, Blocks.LEAVE);
        // setBlock(emptyChunk, x + 1, y + 8, z, Blocks.LEAVE);
        // setBlock(emptyChunk, x, y + 8, z - 1, Blocks.LEAVE);
        // setBlock(emptyChunk, x, y + 8, z + 1, Blocks.LEAVE);
        // } else if (trees[i] < -3) {
        // setBlock(emptyChunk, x, y + 1, z, Blocks.FLOWER);
        // }
        // }
        // }

        // oreGenerator.setClampValues(-1, 1);
        // float[] ores = oreGenerator.getNoise2D(CHUNK_SIZE, CHUNK_SIZE,
        // emptyChunk.getChunkX() * CHUNK_SIZE,
        // emptyChunk.getChunkZ() * CHUNK_SIZE);

        // for (int i = 0; i < CHUNK_AREA; i++) {
        // int x = i % CHUNK_SIZE;
        // int z = i / CHUNK_SIZE;

        // if (ores[i] < -0.4f) {
        // int height = (int) (((ores[i] * 100) + 100) / 2);
        // if (getBlock(emptyChunk, x, height, z) == Blocks.STONE) {
        // setBlock(emptyChunk, x, height, z, Blocks.IRON);
        // }
        // }
        // }
    }

    private void heightMaps(Chunk chunk) {
        // Chunk Size
        noiseGenerator.setClampValues(-100, 100);
        noiseGenerator.setChunkSize(512);
        float[] heightNoise = noiseGenerator.getNoise2D(CHUNK_SIZE, CHUNK_SIZE, CHUNK_SIZE * chunk.getChunkX(),
                CHUNK_SIZE * chunk.getChunkZ(), 100, 5, 2.4f, 0.5f);

        for (int j = 0; j < heightNoise.length; j++) {
            int bkX = (j % CHUNK_SIZE);
            int bkZ = (j / CHUNK_SIZE);

            // int blocksIndx = bkX + bkZ * CHUNK_SIZE;
            // float block = (blockNoise[blocksIndx] + 100) / 2;

            int height = ((int) heightNoise[j] + 100) / 2;

            if (height >= CHUNK_HEIGHT) {
                height = CHUNK_HEIGHT - 1;
            }

            downFill(chunk, bkX, bkZ, height, Blocks.STONE);

            // if (block >= 75 && height >= 60) {
            // blocks[Chunk.getBlockIndex(bkX, height, bkZ)] = Blocks.STONE;
            // downFill(emptyChunk, bkX, bkZ, height - 1, Blocks.STONE);
            // continue;
            // }

            // if (height < 47) {
            // blocks[Chunk.getBlockIndex(bkX, 47, bkZ)] = Blocks.WATER;
            // columFill(emptyChunk, bkX, bkZ, height, 47, Blocks.WATER);
            // columFill(emptyChunk, bkX, bkZ, height - 7, height - 3, Blocks.DIRT);
            // columFill(emptyChunk, bkX, bkZ, height - 4, height - 1, Blocks.SAND);
            // downFill(emptyChunk, bkX, bkZ, height - 8, Blocks.STONE);
            // } else if (height < 56 && block < 50) {
            // columFill(emptyChunk, bkX, bkZ, height - 2, height, Blocks.SAND);
            // columFill(emptyChunk, bkX, bkZ, height - 6, height - 3, Blocks.DIRT);
            // downFill(emptyChunk, bkX, bkZ, height - 7, Blocks.STONE);
            // } else {
            // blocks[Chunk.getBlockIndex(bkX, height, bkZ)] = Blocks.GRASS;
            // columFill(emptyChunk, bkX, bkZ, height - 6, height - 1, Blocks.DIRT);
            // downFill(emptyChunk, bkX, bkZ, height - 7, Blocks.STONE);
            // }
        }
    }

    private void downFill(Chunk chunk, int x, int z, int yHeight, short block) {
        short[] blocks = chunk.getBlocks();
        for (int y = 0; y <= yHeight; y++) {
            blocks[Chunk.getBlockIndex(x, y, z)] = block;
        }
    }

    private void setBlock(Chunk chunk, int x, int y, int z, short block) {
        if (outOfBounds(x, y, z))
            return;
        chunk.getBlocks()[Chunk.getBlockIndex(x, y, z)] = block;
    }

    public short getBlock(Chunk chunk, int x, int y, int z) {
        if (outOfBounds(x, y, z))
            return Blocks.AIR;
        return chunk.getBlocks()[Chunk.getBlockIndex(x, y, z)];
    }

    public int getHighestYAt(Chunk chunk, int x, int z) {
        short[] blocks = chunk.getBlocks();
        for (int y = CHUNK_HEIGHT - 1; y >= 0; y--) {
            if (blocks[Chunk.getBlockIndex(x, y, z)] != Blocks.AIR) {
                return y;
            }
        }
        return 0;
    }

    public boolean outOfBounds(int x, int y, int z) {
        return x < 0 || y < 0 || z < 0 || x >= CHUNK_SIZE || y >= CHUNK_HEIGHT || z >= CHUNK_SIZE;
    }

    private void columFill(Chunk chunk, int x, int z, int yStart, int yEnd, short block) {
        for (int y = yStart; y <= yEnd; y++) {
            setBlock(chunk, x, y, z, block);
        }
    }

    public void fill(Chunk chunk, int x1, int y1, int z1, int x2, int y2, int z2, short block) {
        short[] blocks = chunk.getBlocks();
        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                for (int y = y1; y <= y2; y++) {
                    blocks[Chunk.getBlockIndex(x, y, z)] = block;
                }
            }
        }
    }

}
