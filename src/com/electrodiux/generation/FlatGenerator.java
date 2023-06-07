package com.electrodiux.generation;

import com.electrodiux.block.Blocks;
import com.electrodiux.world.Chunk;
import com.electrodiux.world.World;

public class FlatGenerator extends TerrainGenerator {

    public FlatGenerator(World world) {
        super(world);
    }

    @Override
    public Chunk generateChunk(int xPos, int zPos) {
        Chunk chunk = new Chunk(xPos, zPos);

        fill(chunk, 0, 0, 0, 15, 9, 15, Blocks.DIRT);
        fill(chunk, 0, 10, 0, 15, 10, 15, Blocks.GRASS_BLOCK);

        return chunk;
    }

}
