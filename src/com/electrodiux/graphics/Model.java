package com.electrodiux.graphics;

public class Model {

    private int vaoId;
    private int vertexCount;

    public Model(int vaoId, int vertexCount) {
        this.vaoId = vaoId;
        this.vertexCount = vertexCount;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public int getVaoId() {
        return vaoId;
    }
}
