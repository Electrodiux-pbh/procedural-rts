package com.electrodiux.events;

import com.electrodiux.world.Chunk;

public abstract class ChunkEvent extends Event {

    private Chunk chunk;

    public ChunkEvent(Chunk chunk) {
        this.chunk = chunk;
    }

    public Chunk getChunk() {
        return chunk;
    }

}
