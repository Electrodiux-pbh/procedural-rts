package com.electrodiux.block;

import java.awt.Color;
import java.util.Objects;

import com.electrodiux.block.BlockRegister.BlockDefinitionRegister;
import com.electrodiux.graphics.textures.Sprite;
import com.electrodiux.register.Registrable;

public class BlockDefinition implements Registrable {

    private final String blockId;
    private final String blockName;

    private final Color mapColor;
    private final boolean transparent;

    private final Sprite[] textures;

    BlockDefinition(BlockDefinitionRegister builder) {
        this.blockName = builder.blockName;
        this.blockId = builder.blockId;

        this.mapColor = builder.mapColor;
        this.transparent = builder.transparent;
        this.textures = builder.textures;
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

    public Sprite getTexture(int face) {
        Objects.checkIndex(face, textures.length);
        return textures[face];
    }

    public boolean isTransparent() {
        return transparent;
    }

    @Override
    public String getRegistryName() {
        return blockId;
    }
}