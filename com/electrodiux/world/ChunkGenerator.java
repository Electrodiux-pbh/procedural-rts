package com.electrodiux.world;

import static com.electrodiux.world.Chunk.CHUNK_HEIGHT;
import static com.electrodiux.world.Chunk.CHUNK_SIZE;
import static com.electrodiux.world.Chunk.CHUNK_SIZE_BITMASK;
import static com.electrodiux.world.Chunk.CHUNK_SIZE_BYTESHIFT;

import java.util.Random;

import com.electrodiux.block.Blocks;
import com.electrodiux.util.NoiseGenerator;

public class ChunkGenerator {

    private long seed;
    private NoiseGenerator noiseGenerator;
    private NoiseGenerator carverGenerator;

    private int perm[];

    public ChunkGenerator(long seed) {
        this.seed = seed;

        Random rand = new Random(new Random(seed).nextLong());

        perm = new int[512];
        for (int i = 0; i < 512; i++) {
            perm[i] = rand.nextInt(256);
        }

        noiseGenerator = new NoiseGenerator(this.seed);
        carverGenerator = new NoiseGenerator(this.seed * 100);
    }

    public Chunk createChunk(int x, int z) {
        Chunk chunk = new Chunk(x, z);
        generate(chunk);
        return chunk;
    }

    public void generateDebug(Chunk chunk, int chkX, int chkZ) {
        fill(chunk, 0, 0, 0, 15, 0, 15,
                (((chunk.getChunkX() + chunk.getChunkZ() & 1) & 1) == 0) ? Blocks.WATER : Blocks.STONE);
    }

    public void generate(Chunk chunk) {
        heightMaps(chunk);
        sea(chunk);
        carvers(chunk);
        blocks(chunk);
        stoneBlobs(chunk);
        lava(chunk);
        decorators(chunk);

        structures(chunk);

        fill(chunk, 0, 0, 0, CHUNK_SIZE, 0, CHUNK_SIZE, Blocks.STONE);
    }

    private void heightMaps(Chunk chunk) {
        noiseGenerator.setClampValues(-100, 100);
        noiseGenerator.setChunkSize(512);
        float[] heightNoise = noiseGenerator.getNoise2D(CHUNK_SIZE, CHUNK_SIZE, chunk.getBlockX(), chunk.getBlockZ(),
                100, 5, 2.4f, 0.5f);

        for (int j = 0; j < heightNoise.length; j++) {
            int bkX = (j & CHUNK_SIZE_BITMASK);
            int bkZ = (j >> CHUNK_SIZE_BYTESHIFT);

            int height = (((int) heightNoise[j] + 100) / 2) + 50;

            if (height >= CHUNK_HEIGHT) {
                height = CHUNK_HEIGHT - 1;
            }

            downFill(chunk, bkX, bkZ, height, Blocks.STONE);
        }
    }

    private static final int SEA_LEVEL = 95;
    private static final int LAVA_LEVEL = 10;
    private static final int CAVE_LEVEL = 80;

    private void carvers(Chunk chunk) {
        carverGenerator.setClampValues(-0.9f, 0.8f);
        carverGenerator.setChunkSize(128);

        float[] cave = noiseGenerator.getNoise3D(CHUNK_SIZE, CAVE_LEVEL, CHUNK_SIZE, chunk.getBlockX(), 0,
                chunk.getBlockZ(), 0.8f, 5, 2.4f, 0.5f);

        for (int z = 0; z < CHUNK_SIZE; z++) {
            for (int y = 0; y < CAVE_LEVEL; y++) {
                for (int x = 0; x < CHUNK_SIZE; x++) {
                    int index = x + y * CHUNK_SIZE + z * CHUNK_SIZE * CAVE_LEVEL;
                    if (Math.abs(cave[index]) > 0.4f) {
                        setBlock(chunk, x, y, z, Blocks.AIR);
                    }
                }
            }
        }
    }

    // private void waterCarvers(Chunk chunk) {

    // }

    private void sea(Chunk chunk) {
        short[] blocks = chunk.getBlocks();
        for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                for (int y = SEA_LEVEL;; y--) {
                    int idx = Chunk.getBlockIndex(x, y, z);
                    if (blocks[idx] == Blocks.AIR) {
                        blocks[idx] = Blocks.WATER;
                    } else {
                        break;
                    }
                }
                for (int y = 10; y >= 0; y--) {
                    int idx = Chunk.getBlockIndex(x, y, z);
                    if (blocks[idx] == Blocks.AIR) {
                        blocks[idx] = Blocks.LAVA;
                    }
                }
            }
        }
    }

    private void lava(Chunk chunk) {
        short[] blocks = chunk.getBlocks();
        for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                for (int y = LAVA_LEVEL; y >= 0; y--) {
                    int idx = Chunk.getBlockIndex(x, y, z);
                    if (blocks[idx] == Blocks.AIR) {
                        blocks[idx] = Blocks.LAVA;
                    }
                }
            }
        }
    }

    private void structures(Chunk chunk) {
        // PYRAMID

        float permValue = permutationValue(chunk.getChunkX(), chunk.getChunkZ());

        if (permValue < 0.02f) {
            int height = getMinPositionForStructure(chunk, 0, 0, 14, 14) - 1;
            pyramid(chunk, 0, height, 0, 14, height + CHUNK_SIZE, 14, Blocks.STONEBRICKS, Blocks.MOSSY_STONEBRICKS);
        }

        // HOUSE
        if (permValue > 0.04f && permValue < 0.08) {
            int height = getMinPositionForStructure(chunk, 0, 0, 5, 5);

            fill(chunk, 0, height, 0, 5, height, 5, Blocks.STONEBRICKS);
            fill(chunk, 0, height + 1, 0, 5, height + 5, 5, Blocks.OAK_PLANKS);

            pyramid(chunk, 0, height + 6, 0, 5, height + 12, 5, Blocks.STONEBRICKS);
        }
    }

    private int getMinPositionForStructure(Chunk chunk, int x0, int z0, int x1, int z1) {
        int height = Chunk.CHUNK_HEIGHT;
        for (int x = x0; x < x1; x++) {
            for (int z = z0; z < z1; z++) {
                int value = chunk.getHightestYAt(x, z);
                if (value < height)
                    height = value;
            }
        }
        return height;
    }

    private void stoneBlobs(Chunk chunk) {
        noiseGenerator.setClampValues(-100, 100);
        noiseGenerator.setChunkSize(24);

        float[] stones = noiseGenerator.getNoise3D(CHUNK_SIZE, CHUNK_HEIGHT, CHUNK_SIZE, chunk.getBlockX(), 0,
                chunk.getBlockZ(), 100f, 2, 2.7f, 0.47f);

        for (int z = 0; z < CHUNK_SIZE; z++) {
            for (int y = 0; y < CHUNK_HEIGHT; y++) {
                for (int x = 0; x < CHUNK_SIZE; x++) {
                    int index = x + y * CHUNK_SIZE + z * CHUNK_SIZE * CHUNK_HEIGHT;
                    // System.out.println(stones[index]);
                    if (getBlock(chunk, x, y, z) == Blocks.STONE) {
                        if (stones[index] > 50) {
                            setBlock(chunk, x, y, z, Blocks.DIORITE);
                        } else if (stones[index] < -50) {
                            setBlock(chunk, x, y, z, Blocks.GRANITE);
                        }

                        if (permutationValue(x, y, z) < 0.01f) {
                            setBlock(chunk, x, y, z, Blocks.IRON);
                        }
                    }
                }
            }
        }
    }

    private void blocks(Chunk chunk) {
        noiseGenerator.setClampValues(-100, 100);
        noiseGenerator.setChunkSize(256);
        float[] blockNoise = noiseGenerator.getNoise2D(CHUNK_SIZE, CHUNK_SIZE, chunk.getBlockX(), chunk.getBlockZ(),
                100, 3, 2.64f, 0.548f);

        short[] blocks = chunk.getBlocks();

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int blocksIndx = x + z * CHUNK_SIZE;
                float block = (blockNoise[blocksIndx] + 100) / 2;

                if (block >= 50 && block < 75) {
                    int layerCount = 0;
                    short topBlock = Blocks.AIR;
                    for (int y = CHUNK_HEIGHT - 1; y >= 0; y--) {
                        int idx = Chunk.getBlockIndex(x, y, z);
                        if (blocks[idx] == Blocks.STONE && y >= 40 && layerCount <= 5) {
                            blocks[idx] = topBlock == Blocks.AIR ? Blocks.GRASS : Blocks.DIRT;
                            layerCount++;
                        }
                        topBlock = blocks[idx];
                        if (blocks[idx] == Blocks.AIR) {
                            if (layerCount != 0)
                                break;
                            layerCount = 0;
                        }
                    }
                } else if (block < 50) {
                    int layerCount = 0;
                    for (int y = CHUNK_HEIGHT - 1; y >= 0; y--) {
                        int idx = Chunk.getBlockIndex(x, y, z);
                        if (blocks[idx] == Blocks.STONE) {
                            if (layerCount <= 4)
                                blocks[idx] = Blocks.SAND;
                            else if (layerCount <= 6) {
                                blocks[idx] = Blocks.DIRT;
                            }
                            layerCount++;
                        }
                        if (blocks[idx] == Blocks.AIR) {
                            if (layerCount != 0)
                                break;
                            layerCount = 0;
                        }
                    }
                }
            }
        }
    }

    private void decorators(Chunk chunk) {
        noiseGenerator.setClampValues(-5, 5);
        noiseGenerator.setChunkSize(16);
        float[] trees = noiseGenerator.getNoise2D(CHUNK_SIZE, CHUNK_SIZE, chunk.getBlockX(), chunk.getBlockZ(), 1, 20,
                2f, 1.5f);

        short[] blocks = chunk.getBlocks();

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int y = getHighestYAt(chunk, x, z);
                short block = blocks[Chunk.getBlockIndex(x, y, z)];

                if (block != Blocks.GRASS) {
                    continue;
                }

                int idx = x + z * CHUNK_SIZE;

                if (block == Blocks.GRASS) {
                    if (trees[idx] > 3) {
                        setBlock(chunk, x, y, z, Blocks.DIRT);
                        columFill(chunk, x, z, y + 1, y + 4, Blocks.LOG);
                        fill(chunk, x - 2, y + 5, z - 2, x + 2, y + 6, z + 2, Blocks.LEAVE);
                        fill(chunk, x - 1, y + 7, z - 1, x + 1, y + 7, z + 1, Blocks.LEAVE);

                        setBlock(chunk, x, y + 8, z, Blocks.LEAVE);
                        setBlock(chunk, x - 1, y + 8, z, Blocks.LEAVE);
                        setBlock(chunk, x + 1, y + 8, z, Blocks.LEAVE);
                        setBlock(chunk, x, y + 8, z - 1, Blocks.LEAVE);
                        setBlock(chunk, x, y + 8, z + 1, Blocks.LEAVE);
                    } else if (trees[idx] < -3) {
                        setBlock(chunk, x, y + 1, z, Blocks.FLOWER);
                    } else if (trees[idx] > 2.1f && trees[idx] < 2.6f) {
                        setBlock(chunk, x, y + 1, z,
                                blockShuffle(x, y + 1, z, Blocks.PAEONIA, Blocks.BLUE_ORCHID));
                    }
                }
            }
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

    public void fill(Chunk chunk, int x1, int y1, int z1, int x2, int y2, int z2, short... block) {
        short[] blocks = chunk.getBlocks();
        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                for (int y = y1; y <= y2; y++) {
                    if (outOfBounds(x, y, z))
                        continue;
                    blocks[Chunk.getBlockIndex(x, y, z)] = blockShuffle(x, y, z, block);
                }
            }
        }
    }

    public void pyramid(Chunk chunk, int x1, int y1, int z1, int x2, int y2, int z2, short... block) {
        short[] blocks = chunk.getBlocks();
        yiteration: for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                if (x1 > x2)
                    break yiteration;
                for (int z = z1; z <= z2; z++) {
                    if (z1 > z2)
                        break yiteration;
                    if (outOfBounds(x, y, z))
                        continue;

                    blocks[Chunk.getBlockIndex(x, y, z)] = blockShuffle(x, y, z, block);
                }
            }
            x1++;
            x2--;
            z1++;
            z2--;
        }
    }

    public short blockShuffle(int x, int y, int z, short... blocks) {
        if (blocks.length == 1)
            return blocks[0];

        int numBlocks = blocks.length;
        double partSize = 1.0 / numBlocks;

        float randomValue = permutationValue(x, y, z);

        int selectedBlockIndex = -1;
        for (int i = 0; i < numBlocks; i++) {
            double startRange = i * partSize;
            double endRange = startRange + partSize;

            if (randomValue >= startRange && randomValue < endRange) {
                selectedBlockIndex = i;
                break;
            }
        }

        if (selectedBlockIndex == -1) {
            return blocks[0];
        } else {
            return blocks[selectedBlockIndex];
        }
    }

    private float permutationValue(int x, int y) {
        int xi = (int) Math.floor(x) & 255;
        int yi = (int) Math.floor(y) & 255;

        int val = perm[perm[xi] + yi];

        return (float) val / 256f;
    }

    private float permutationValue(int x, int y, int z) {
        int xi = (int) Math.floor(x) & 255;
        int yi = (int) Math.floor(y) & 255;
        int zi = (int) Math.floor(z) & 255;

        int val = perm[perm[perm[xi] + yi] + zi];

        return (float) val / 256f;
    }

}
