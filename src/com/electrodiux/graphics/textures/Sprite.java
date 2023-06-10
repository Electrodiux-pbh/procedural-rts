package com.electrodiux.graphics.textures;

public class Sprite {

    private float[] texCoords;

    public Sprite() {
        this(new float[] {
                0.0f, 0.0f,
                1.0f, 0.0f,
                1.0f, 1.0f,
                0.0f, 1.0f,
        });
    }

    public Sprite(float[] texCoords) {
        this.texCoords = texCoords;
    }

    public float[] getTexCoords() {
        return texCoords;
    }

}
