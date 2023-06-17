package com.electrodiux.generation;

import static com.electrodiux.world.Chunk.CHUNK_HEIGHT;
import static com.electrodiux.world.Chunk.CHUNK_SIZE;

import java.util.ArrayList;
import java.util.List;

import com.electrodiux.block.Blocks;
import com.electrodiux.graphics.textures.BufferedImage;
import com.electrodiux.math.MathUtils;
import com.electrodiux.register.Register;
import com.electrodiux.world.Chunk;
import com.electrodiux.world.Chunk.ChunkStatus;
import com.electrodiux.world.World;

public class WorldGenerator extends TerrainGenerator {

    private Register<Structure> structureRegistry;

    private NoiseGenerator noiseGenerator;
    private NoiseGenerator carverGenerator;

    private Permutation permutation;

    public WorldGenerator(World world) {
        super(world);

        structureRegistry = new Register<Structure>();

        noiseGenerator = new NoiseGenerator(Permutation.calculatePermutedSeed(seed, "noise"));
        carverGenerator = new NoiseGenerator(Permutation.calculatePermutedSeed(seed, "carver"));

        permutation = new Permutation(Permutation.calculatePermutedSeed(seed, "permutation"));

        // noiseGenerator.setClampValues(-1, 1);
        // int size = 500 * 5;

        // float[] continentalnessNoise = noiseGenerator.getNoise2D(size, size, -size /
        // 2, -size / 2,
        // 1, 700, 5, 2.35f, 0.5f);

        // createNoiseImage(continentalnessNoise, size, size, "continentalness");
    }

    private void createNoiseImage(float[] noise, int width, int height, String name) {
        BufferedImage image = new BufferedImage(width, height, 4);

        for (int i = 0; i < noise.length; i++) {
            int x = i % width;
            int y = i / width;

            int grayScale = (int) Math.round(((noise[i] + 1) / 2f) * 255);
            image.setColor(x, y, BufferedImage.getColor(grayScale, grayScale,
                    grayScale));
        }

        BufferedImage.saveImage(image, name + "(" + seed + ").png");
    }

    public Register<Structure> getStructureRegistry() {
        return structureRegistry;
    }

    public Chunk generateChunk(Chunk chunk) {
        heightMaps(chunk);
        chunk.setChunkStatus(ChunkStatus.HEIGHTMAPS);

        carvers(chunk);

        sea(chunk);
        blocks(chunk);
        stoneBlobs(chunk);
        lava(chunk);
        decorators(chunk);
        structures(chunk);
        fill(chunk, 0, 0, 0, CHUNK_SIZE, 0, CHUNK_SIZE, Blocks.STONE);

        chunk.setChunkStatus(ChunkStatus.COMPLETE);

        return chunk;
    }

    public void generateDebug(Chunk chunk, int chkX, int chkZ) {
        fill(chunk, 0, 0, 0, 15, 0, 15,
                (((chunk.getXPos() + chunk.getZPos() & 1) & 1) == 0) ? Blocks.WATER : Blocks.STONE);
    }

    private void heightMaps(Chunk chunk) {

        List<Entry<Float, Integer>> continentalness = new ArrayList<>();
        addValue(continentalness, -1f, 42);
        addValue(continentalness, -0.6802474455071f, 45);
        addValue(continentalness, -0.48f, 50);
        addValue(continentalness, -0.4609768892642f, 57);
        addValue(continentalness, -0.2202108300039f, 62);
        addValue(continentalness, -0.18f, 65f);
        addValue(continentalness, -0.02f, 72f);
        addValue(continentalness, -0.12f, 70f);
        addValue(continentalness, 0.297522467576f, 86);
        addValue(continentalness, 0.5873908882166f, 133);
        addValue(continentalness, 0.6934403104022f, 150);
        addValue(continentalness, 1f, 173);

        noiseGenerator.setClampValues(-1, 1);
        float[] continentalnessNoise = noiseGenerator.getNoise2D(CHUNK_SIZE, CHUNK_SIZE, chunk.getBlockX(),
                chunk.getBlockZ(),
                1, 700, 5, 2.35f, 0.5f);

        for (int i = 0; i < continentalnessNoise.length; i++) {
            int x = i % Chunk.CHUNK_SIZE;
            int y = i / Chunk.CHUNK_SIZE;

            int height = interpolateHeight(continentalness, continentalnessNoise[i]);
            if (height >= Chunk.CHUNK_HEIGHT) {
                height = Chunk.CHUNK_HEIGHT - 1;
            }

            downFill(chunk, x, y, height, Blocks.STONE);
        }
    }

    private static class Entry<K, V> {
        private K key;
        private V value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }

    private static void addValue(List<Entry<Float, Integer>> reationTable, float mod, float height) {
        reationTable.add(new Entry<Float, Integer>(mod, (int) height));
    }

    public static int interpolateHeight(List<Entry<Float, Integer>> reationTable, float value) {
        int index = -1;
        int length = reationTable.size();

        // Find the index of the interval containing the value
        for (int i = 0; i < length - 1; i++) {
            if (value >= reationTable.get(i).getKey() && value < reationTable.get(i + 1).getKey()) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            // If the value is outside the defined range, return the first or last height
            // value
            if (value < reationTable.get(0).getKey()) {
                return reationTable.get(0).getValue();
            } else {
                return (int) reationTable.get(length - 1).getValue();
            }
        }

        // Perform linear interpolation
        float startValue = reationTable.get(index).getKey();
        float endValue = reationTable.get(index + 1).getKey();
        int startHeight = reationTable.get(index).getValue();
        int endHeight = reationTable.get(index + 1).getValue();

        float interpolationRatio = (value - startValue) / (endValue - startValue);
        int interpolatedHeight = (int) MathUtils.slerp(startHeight, endHeight, interpolationRatio);

        return interpolatedHeight;
    }

    private static final int SEA_LEVEL = 65;
    private static final int LAVA_LEVEL = 10;
    private static final int CAVE_LEVEL = 40;

    private void carvers(Chunk chunk) {
        carverGenerator.setClampValues(-0.9f, 0.8f);
        float[] cave = noiseGenerator.getNoise3D(CHUNK_SIZE, CAVE_LEVEL, CHUNK_SIZE, chunk.getBlockX(), 0,
                chunk.getBlockZ(), 0.8f, 128, 5, 2.4f, 0.5f);

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

        float permValue = permutation.permutationValue(chunk.getXPos(), chunk.getZPos());

        if (permValue < 0.02f && getHighestBlockAt(chunk, 0, 0) == Blocks.SAND) {
            int height = getMinPositionForStructure(chunk, 0, 0, 14, 14) - 1;
            pyramid(chunk, 0, height, 0, 14, height + CHUNK_SIZE, 14, Blocks.STONEBRICKS, Blocks.MOSSY_STONEBRICKS);
        }

        // HOUSE
        if (permValue > 0.04f && permValue < 0.08 && getHighestBlockAt(chunk, 0, 0) == Blocks.GRASS_BLOCK) {
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

        float[] stones = noiseGenerator.getNoise3D(CHUNK_SIZE, CHUNK_HEIGHT, CHUNK_SIZE, chunk.getBlockX(), 0,
                chunk.getBlockZ(), 100f, 24, 2, 2.7f, 0.47f);

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

                        if (permutation.permutationValue(x, y, z) < 0.005f) {
                            setBlock(chunk, x, y, z, Blocks.IRON);
                        }
                    }
                }
            }
        }
    }

    private void blocks(Chunk chunk) {
        // noiseGenerator.setClampValues(-100, 100);
        // float[] blockNoise = noiseGenerator.getNoise2D(CHUNK_SIZE, CHUNK_SIZE,
        // chunk.getBlockX(), chunk.getBlockZ(),
        // 100, 256, 3, 2.64f, 0.548f);

        short[] blocks = chunk.getBlocks();

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                // int blocksIndx = x + z * CHUNK_SIZE;
                // float block = (blockNoise[blocksIndx] + 100) / 2;

                int layerCount = 0;
                short topBlock = Blocks.AIR;
                for (int y = CHUNK_HEIGHT - 1; y >= 0; y--) {
                    int idx = Chunk.getBlockIndex(x, y, z);
                    if (blocks[idx] == Blocks.STONE && y >= 40 && layerCount <= 5) {
                        blocks[idx] = topBlock == Blocks.AIR ? Blocks.GRASS_BLOCK : Blocks.DIRT;
                        layerCount++;
                    }
                    topBlock = blocks[idx];
                    if (blocks[idx] == Blocks.AIR) {
                        if (layerCount != 0)
                            break;
                        layerCount = 0;
                    }
                }
                // if (block >= 50 && block < 75) {

                // } else if (block < 50) {
                // int layerCount = 0;
                // for (int y = CHUNK_HEIGHT - 1; y >= 0; y--) {
                // int idx = Chunk.getBlockIndex(x, y, z);
                // if (blocks[idx] == Blocks.STONE) {
                // if (layerCount <= 4)
                // blocks[idx] = Blocks.SAND;
                // else if (layerCount <= 6) {
                // blocks[idx] = Blocks.DIRT;
                // }
                // layerCount++;
                // }
                // if (blocks[idx] == Blocks.AIR) {
                // if (layerCount != 0)
                // break;
                // layerCount = 0;
                // }
                // }
                // }
            }
        }
    }

    private void paths(Chunk chunk) {
        float[] paths = noiseGenerator.getNoise2D(CHUNK_SIZE, CHUNK_SIZE, chunk.getBlockX(), chunk.getBlockZ(), 1, 200,
                2, 1.6f, 0.55f);

        short[] blocks = chunk.getBlocks();

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                if (Math.abs(paths[x + z * CHUNK_SIZE]) < 0.025f) {
                    for (int y = Chunk.CHUNK_HEIGHT - 1; y >= 0; y--) {
                        short block = blocks[Chunk.getBlockIndex(x, y, z)];

                        if (block == Blocks.GRASS_BLOCK) {
                            setBlock(chunk, x, y, z, Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE, Blocks.STONEBRICKS,
                                    Blocks.MOSSY_STONEBRICKS);
                            break;
                        }
                    }
                }

            }
        }
    }

    private void decorators(Chunk chunk) {
        paths(chunk);

        noiseGenerator.setClampValues(-5, 5);
        float[] trees = noiseGenerator.getNoise2D(CHUNK_SIZE, CHUNK_SIZE, chunk.getBlockX(), chunk.getBlockZ(), 1, 16,
                20, 2f, 1.5f);

        short[] blocks = chunk.getBlocks();

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int y = getHighestYAt(chunk, x, z);
                short block = blocks[Chunk.getBlockIndex(x, y, z)];

                if (block != Blocks.GRASS_BLOCK) {
                    continue;
                }

                int idx = x + z * CHUNK_SIZE;

                if (block == Blocks.GRASS_BLOCK) {
                    if (trees[idx] > 2.3f) {
                        setBlock(chunk, x, y, z, Blocks.DIRT);

                        final int height = permutation.permutationValue(x, y, z, 2, 4);

                        columFill(chunk, x, z, y + 1, y + height + 1, Blocks.LOG);
                        replace(chunk, x - 2, y + height + 1, z - 2, x + 2, y + height + 2, z + 2, Blocks.AIR,
                                Blocks.LEAVE);
                        replace(chunk, x - 1, y + height + 2, z - 1, x + 1, y + height + 3, z + 1, Blocks.AIR,
                                Blocks.LEAVE);

                        setBlock(chunk, x, y + height + 4, z, Blocks.LEAVE);
                        setBlock(chunk, x - 1, y + height + 4, z, Blocks.LEAVE);
                        setBlock(chunk, x + 1, y + height + 4, z, Blocks.LEAVE);
                        setBlock(chunk, x, y + height + 4, z - 1, Blocks.LEAVE);
                        setBlock(chunk, x, y + height + 4, z + 1, Blocks.LEAVE);
                    } else if (trees[idx] < -3) {
                        setBlock(chunk, x, y + 1, z, Blocks.RED_TULIP);
                    } else if (trees[idx] > 2.1f && trees[idx] < 2.6f) {
                        setBlock(chunk, x, y + 1, z,
                                blockShuffle(x, y + 1, z, Blocks.POPPY, Blocks.BLUE_ORCHID));
                    }
                }
            }
        }
    }

}
