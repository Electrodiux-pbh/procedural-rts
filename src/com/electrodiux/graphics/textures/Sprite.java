package com.electrodiux.graphics.textures;

public class Sprite extends Texture {

    private float[] texCoords;

    public Sprite(int textureId) {
        this(textureId, new float[] {
                0.0f, 0.0f,
                1.0f, 0.0f,
                1.0f, 1.0f,
                0.0f, 1.0f,
        });
    }

    public Sprite(Texture texture, float[] texCoords) {
        this(texture.textureId, texCoords);
    }

    public Sprite(int textureId, float[] texCoords) {
        super(textureId);
        this.texCoords = texCoords;
    }

    public float[] getTexCoords() {
        return texCoords;
    }

}
