package com.electrodiux.world;

public class ChunkIndexSearch extends ChunkIndex {

    public ChunkIndexSearch(int x, int z) {
        super(x, z);
    }

    public void set(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setZ(int z) {
        this.z = z;
    }

}
