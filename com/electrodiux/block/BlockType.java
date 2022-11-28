package com.electrodiux.block;

import java.awt.Color;
import java.awt.image.DataBuffer;

import com.electrodiux.assets.Loader;
import com.electrodiux.assets.Texture;

public enum BlockType {
    GRASS(Color.GREEN, "grass"),
    DIRT(Color.YELLOW, "dirt"),
    STONE(Color.GRAY, "stone"),
    WATER(new Color(0x2865c7), "water"),
    SAND(new Color(0xf5e942), "sand"),
    LOG(new Color(0x704b28), "log"),
    LEAVE(new Color(0x377325), "leave"),
    IRON(Color.WHITE, "iron"),
    FLOWER(Color.RED, "flower", true),
    LAVA(Color.ORANGE, "lava");

    private Color color;
    private Texture texture;
    private boolean transparent;

    BlockType(Color c) {
        this.color = c;
        this.texture = Loader.loadTexture("base");
        this.transparent = false;
        DataBuffer buffer = texture.getData().getRaster().getDataBuffer();
        for (int i = 0; i < buffer.getSize(); i++) {
            if (buffer.getElem(i) == 0xFFFFFFFF) {
                buffer.setElem(i, c.getRGB());
            }
        }
    }

    BlockType(Color c, String texturePath) {
        this(c, texturePath, false);
    }

    BlockType(Color c, String texturePath, boolean transparent) {
        this.texture = Loader.loadTexture(texturePath);
        this.color = c;
        this.transparent = transparent;
    }

    public Color getColor() {
        return color;
    }

    public Texture getTexture() {
        return texture;
    }

    public boolean isTransparent() {
        return transparent;
    }
}
