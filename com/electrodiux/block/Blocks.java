package com.electrodiux.block;

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

    public static final BlockType[] blocks = new BlockType[128];

    public static final BlockType getBlockType(int index) {
        if (index < 0 || index >= blocks.length)
            return null;
        return blocks[index];
    }

    static {
        blocks[Blocks.AIR] = null;
        blocks[Blocks.GRASS] = BlockType.GRASS;
        blocks[Blocks.DIRT] = BlockType.DIRT;
        blocks[Blocks.STONE] = BlockType.STONE;
        blocks[Blocks.WATER] = BlockType.WATER;
        blocks[Blocks.SAND] = BlockType.SAND;
        blocks[Blocks.LOG] = BlockType.LOG;
        blocks[Blocks.LEAVE] = BlockType.LEAVE;
        blocks[Blocks.IRON] = BlockType.IRON;
        blocks[Blocks.FLOWER] = BlockType.FLOWER;
        blocks[Blocks.LAVA] = BlockType.LAVA;
    }
}
