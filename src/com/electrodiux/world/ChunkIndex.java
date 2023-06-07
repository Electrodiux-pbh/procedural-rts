package com.electrodiux.world;

public class ChunkIndex {

    protected int x, z;

    protected ChunkIndex(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    @Override
    public int hashCode() {
        return 16337 + (31 * x) + z; // 17 * 31
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChunkIndex idx) {
            return idx.x == x && idx.z == z;
        }
        return false;
    }

}
