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
        final int prime = 31;
        int result = 17;
        result = prime * result + x;
        result = prime * result + z;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (obj instanceof ChunkIndex idx) {
            return idx.x == this.x && idx.z == this.z;
        }
        return false;
    }

}
