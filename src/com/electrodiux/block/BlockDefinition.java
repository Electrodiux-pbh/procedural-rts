package com.electrodiux.block;

import java.awt.Color;

import com.electrodiux.block.BlockRegister.BlockDefinitionRegister;
import com.electrodiux.graphics.Texture;
import com.electrodiux.register.Registrable;

public class BlockDefinition implements Registrable {

    private final String blockId;
    private final String blockName;

    private final Color mapColor;
    private final Texture texture;
    private final boolean transparent;

    BlockDefinition(BlockDefinitionRegister builder) {
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

    @Override
    public String getRegistryName() {
        return blockId;
    }
}