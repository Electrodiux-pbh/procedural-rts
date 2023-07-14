package com.electrodiux.events;

import com.electrodiux.world.Chunk;

public class ChunkUnloadEvent extends ChunkEvent {

    public ChunkUnloadEvent(Chunk chunk) {
        super(chunk);
    }

}
