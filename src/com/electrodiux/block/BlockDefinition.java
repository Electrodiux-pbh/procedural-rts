package com.electrodiux.block;

import java.awt.Color;
import java.util.Objects;

import com.electrodiux.block.BlockRegister.BlockDefinitionRegister;
import com.electrodiux.graphics.textures.Sprite;
import com.electrodiux.math.MathUtils;
import com.electrodiux.register.Registrable;
import com.electrodiux.world.Chunk;

public class BlockDefinition implements Registrable {

    private final String blockId;
    private final String blockName;

    private final Color mapColor;
    private final boolean transparent;

    private final boolean translucent;
    private final byte lightEmision;

    private final boolean internalFaces;

    private final Sprite[] textures;

    BlockDefinition(BlockDefinitionRegister builder) {
        this.blockName = builder.blockName;
        this.blockId = builder.blockId;

        this.mapColor = builder.mapColor;
        this.transparent = builder.transparent;
        this.translucent = builder.translucent;

        this.internalFaces = builder.internalFaces;

        this.textures = builder.textures;

        this.lightEmision = (byte) MathUtils.clamp(builder.lightEmision, Chunk.MIN_LIGHT_LEVEL, Chunk.MAX_LIGHT_LEVEL);
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

    public boolean hasInternalFaces() {
        return internalFaces;
    }

    @Override
    public String getRegistryName() {
        return blockId;
    }

    public boolean isInternalFaces() {
        return internalFaces;
    }

    public Sprite[] getTextures() {
        return textures;
    }

    public boolean isTranslucent() {
        return translucent;
    }

    public byte getLightEmision() {
        return lightEmision;
    }

    public boolean emitsLight() {
        return lightEmision > 0;
    }

}