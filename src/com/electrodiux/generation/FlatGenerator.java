package com.electrodiux.generation;

import com.electrodiux.block.Blocks;
import com.electrodiux.world.Chunk;
import com.electrodiux.world.Chunk.ChunkStatus;
import com.electrodiux.world.World;

public class FlatGenerator extends TerrainGenerator {

    public FlatGenerator(World world) {
        super(world);
    }

    @Override
    public Chunk generateChunk(Chunk chunk) {
        // for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
        // for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
        // if ((x + z) % 2 == 0) {
        // setBlock(chunk, x, 1, z, Blocks.LOG);
        // } else {
        // setBlock(chunk, x, 10, z, Blocks.GRASS_BLOCK);
        // }
        // }
        // }
        fill(chunk, 0, 0, 0, 15, 9, 15, Blocks.DIRT);
        fill(chunk, 0, 10, 0, 15, 10, 15, Blocks.GRASS_BLOCK);

        chunk.setChunkStatus(ChunkStatus.COMPLETE);

        return chunk;
    }

}
