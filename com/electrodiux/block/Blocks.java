package com.electrodiux.block;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.electrodiux.assets.Loader;
import com.electrodiux.assets.Texture;

public class Blocks {
    public static final byte AIR = 0;
    public static final byte GRASS = 1;
    public static final byte DIRT = 2;
    public static final byte STONE = 3;
    public static final byte WATER = 4;
    public static final byte SAND = 5;
    public static final byte LOG = 6;
    public static final byte LEAVE = 7;
    public static final byte IRON = 8;
    public static final byte FLOWER = 9;
    public static final byte LAVA = 10;
    public static final byte DIORITE = 11;
    public static final byte GRANITE = 12;

    public static final BlockMetadata[] blocks = new BlockMetadata[13];

    public static final BlockMetadata getBlockType(int index) {
        if (index < 0 || index >= blocks.length)
            return null;
        return blocks[index];
    }

    private static final Map<String, BlockMetadata> blocksMap = new HashMap<String, BlockMetadata>();

    public static BlockMetadataRegister register(String blockId) {
        if (blocksMap.containsKey(blockId)) {
            throw new IllegalStateException("The block with id " + blockId + " already exists");
        }
        return new BlockMetadataRegister(blockId);
    }

    static class BlockMetadataRegister {
        String blockName;
        String blockId;

        Color mapColor;
        Texture texture;
        boolean transparent;

        protected BlockMetadataRegister(String blockId) {
            this.blockId = blockId;
        }

        public BlockMetadataRegister setColor(Color c) {
            this.mapColor = c;
            return this;
        }

        public BlockMetadataRegister setTexture(String texturePath) {
            this.texture = Loader.loadTexture(texturePath);
            return this;
        }

        public BlockMetadataRegister setTransparent(boolean transparent) {
            this.transparent = transparent;
            return this;
        }

        public BlockMetadataRegister setBlockName(String blockName) {
            this.blockName = blockName;
            return this;
        }

        public BlockMetadata build() {
            if (blockName == null) {
                blockName = blockId;
            }

            BlockMetadata block = new BlockMetadata(this);

            if (blocksMap.containsKey(blockId)) {
                throw new IllegalStateException("The block with id " + blockId + " already exists");
            }
            blocksMap.put(blockId, block);

            return block;
        }
    }

    static {
        blocks[Blocks.AIR] = null;
        blocks[Blocks.GRASS] = register("grass").setTexture("grass.png").build();
        blocks[Blocks.DIRT] = register("dirt").setTexture("dirt.png").build();
        blocks[Blocks.STONE] = register("stone").setTexture("stone.png").build();
        blocks[Blocks.WATER] = register("water").setTexture("water.png").build();
        blocks[Blocks.SAND] = register("sand").setTexture("sand.png").build();
        blocks[Blocks.LOG] = register("log").setTexture("log.png").build();
        blocks[Blocks.LEAVE] = register("leave").setTexture("leave.png").setTransparent(true).build();
        blocks[Blocks.IRON] = register("iron").setTexture("iron.png").build();
        blocks[Blocks.FLOWER] = register("flower").setTexture("flower.png").setTransparent(true).build();
        blocks[Blocks.LAVA] = register("lava").setTexture("lava.png").build();
        blocks[Blocks.DIORITE] = register("diorite").setTexture("diorite.png").build();
        blocks[Blocks.GRANITE] = register("granite").setTexture("granite.png").build();
    }
}
