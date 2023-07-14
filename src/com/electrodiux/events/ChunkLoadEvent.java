package com.electrodiux.events;

import com.electrodiux.world.Chunk;

public class ChunkLoadEvent extends ChunkEvent {

    public ChunkLoadEvent(Chunk chunk) {
        super(chunk);
    }

}
