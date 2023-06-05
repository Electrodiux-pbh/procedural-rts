package com.electrodiux.block;

import java.awt.Color;

import com.electrodiux.assets.Texture;
import com.electrodiux.block.BlockRegister.BlockMetadataRegistryBuilder;

public class BlockMetadata {

    private final String blockId;
    private final String blockName;

    private final Color mapColor;
    private final Texture texture;
    private final boolean transparent;

    BlockMetadata(BlockMetadataRegistryBuilder builder) {
        this.blockName = builder.blockName;
        this.blockId = builder.blockId;

        this.mapColor = builder.mapColor;
        this.texture = builder.texture;
        this.transparent = builder.transparent;
    }

    public String getBlockId() {
        return blockId;
    }

    public String getBlockName() {
        return blockName;
    }

    public Color getMapColor() {
        return mapColor;
    }

    public Texture getTexture() {
        return texture;
    }

    public boolean isTransparent() {
        return transparent;
    }
}