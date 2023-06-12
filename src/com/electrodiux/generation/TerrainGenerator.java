package com.electrodiux.generation;

import static com.electrodiux.world.Chunk.CHUNK_HEIGHT;
import static com.electrodiux.world.Chunk.CHUNK_SIZE;

import com.electrodiux.block.Blocks;
import com.electrodiux.world.Chunk;
import com.electrodiux.world.World;

public abstract class TerrainGenerator {

    protected World world;
    protected long seed;

    protected Permutation blockShufflePermutation;

    public TerrainGenerator(World world) {
        this.world = world;
        this.seed = world.getWorldSeed();

        blockShufflePermutation = new Permutation(Permutation.calculatePermutedSeed(seed, "block_shuffle"));
    }

    public abstract Chunk generateChunk(int xPos, int zPos);

    protected void downFill(Chunk chunk, int x, int z, int yHeight, short block) {
        short[] blocks = chunk.getBlocks();
        for (int y = 0; y <= yHeight; y++) {
            blocks[Chunk.getBlockIndex(x, y, z)] = block;
        }
    }

    protected void setBlock(Chunk chunk, int x, int y, int z, short block) {
        if (outOfBounds(x, y, z))
            return;
        chunk.getBlocks()[Chunk.getBlockIndex(x, y, z)] = block;
    }

    protected void setBlock(Chunk chunk, int x, int y, int z, short... blocks) {
        if (outOfBounds(x, y, z))
            return;
        chunk.getBlocks()[Chunk.getBlockIndex(x, y, z)] = blockShuffle(x, y, z, blocks);
    }

    protected short getBlock(Chunk chunk, int x, int y, int z) {
        if (outOfBounds(x, y, z))
            return Blocks.AIR;
        return chunk.getBlocks()[Chunk.getBlockIndex(x, y, z)];
    }

    protected int getHighestYAt(Chunk chunk, int x, int z) {
        short[] blocks = chunk.getBlocks();
        for (int y = CHUNK_HEIGHT - 1; y >= 0; y--) {
            short block = blocks[Chunk.getBlockIndex(x, y, z)];
            if (block != Blocks.AIR) {
                return y;
            }
        }
        return 0;
    }

    protected short getHighestBlockAt(Chunk chunk, int x, int z) {
        short[] blocks = chunk.getBlocks();
        for (int y = CHUNK_HEIGHT - 1; y >= 0; y--) {
            short block = blocks[Chunk.getBlockIndex(x, y, z)];
            if (block != Blocks.AIR) {
                return block;
            }
        }
        return 0;
    }

    protected boolean outOfBounds(int x, int y, int z) {
        return x < 0 || y < 0 || z < 0 || x >= CHUNK_SIZE || y >= CHUNK_HEIGHT || z >= CHUNK_SIZE;
    }

    protected void columFill(Chunk chunk, int x, int z, int yStart, int yEnd, short block) {
        for (int y = yStart; y <= yEnd; y++) {
            setBlock(chunk, x, y, z, block);
        }
    }

    protected void fill(Chunk chunk, int x1, int y1, int z1, int x2, int y2, int z2, short... block) {
        short[] blocks = chunk.getBlocks();
        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                for (int y = y1; y <= y2; y++) {
                    if (outOfBounds(x, y, z))
                        continue;
                    blocks[Chunk.getBlockIndex(x, y, z)] = blockShuffle(x, y, z, blockShufflePermutation, block);
                }
            }
        }
    }

    protected void pyramid(Chunk chunk, int x1, int y1, int z1, int x2, int y2, int z2, short... block) {
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

                    blocks[Chunk.getBlockIndex(x, y, z)] = blockShuffle(x, y, z, blockShufflePermutation, block);
                }
            }
            x1++;
            x2--;
            z1++;
            z2--;
        }
    }

    protected short blockShuffle(int x, int y, int z, short... blocks) {
        return blockShuffle(x, y, z, blockShufflePermutation, blocks);
    }

    public static short blockShuffle(int x, int y, int z, Permutation permutation, short... blocks) {
        if (blocks.length == 1)
            return blocks[0];

        int numBlocks = blocks.length;
        double partSize = 1.0 / numBlocks;

        float randomValue = permutation.permutationValue(x, y, z);

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

}
