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
    public static final byte DIORITE = 11;
    public static final byte GRANITE = 12;
    public static final byte COBBLESTONE = 13;
    public static final byte MOSSY_COBBLESTONE = 14;
    public static final byte OAK_PLANKS = 15;
    public static final byte STONEBRICKS = 16;
    public static final byte MOSSY_STONEBRICKS = 17;
    public static final byte BLUE_ORCHID = 18;
    public static final byte PAEONIA = 19;

    public static void loadBlocks() {
        BlockRegister.blocksMetadata[Blocks.AIR] = null;

        BlockRegister.register("grass", Blocks.GRASS).setTexture("grass.png").build();
        BlockRegister.register("dirt", Blocks.DIRT).setTexture("dirt.png").build();
        BlockRegister.register("stone", Blocks.STONE).setTexture("stone.png").build();
        BlockRegister.register("water", Blocks.WATER).setTexture("water.png").build();
        BlockRegister.register("sand", Blocks.SAND).setTexture("sand.png").build();
        BlockRegister.register("oak_log", Blocks.LOG).setTexture("log.png").build();
        BlockRegister.register("leave", Blocks.LEAVE).setTexture("leave.png").setTransparent(true).build();
        BlockRegister.register("iron", Blocks.IRON).setTexture("iron.png").build();
        BlockRegister.register("flower", Blocks.FLOWER).setTexture("flower.png").setTransparent(true).build();
        BlockRegister.register("lava", Blocks.LAVA).setTexture("lava.png").build();
        BlockRegister.register("diorite", Blocks.DIORITE).setTexture("diorite.png").build();
        BlockRegister.register("granite", Blocks.GRANITE).setTexture("granite.png").build();
        BlockRegister.register("cobblestone", Blocks.COBBLESTONE).setTexture("cobblestone.png").build();
        BlockRegister.register("mossy_cobblestone", Blocks.MOSSY_COBBLESTONE).setTexture("mossy_cobblestone.png")
                .build();
        BlockRegister.register("oak_planks", Blocks.OAK_PLANKS).setTexture("oak_planks.png").build();
        BlockRegister.register("stonebricks", Blocks.STONEBRICKS).setTexture("stone_bricks.png").build();
        BlockRegister.register("mossy_stonebricks", Blocks.MOSSY_STONEBRICKS).setTexture("mossy_stone_bricks.png")
                .build();
        BlockRegister.register("blue_orchid", Blocks.BLUE_ORCHID).setTexture("blue_orchid.png").setTransparent(true)
                .build();
        BlockRegister.register("paeonia", Blocks.PAEONIA).setTexture("paeonia.png").setTransparent(true).build();
    }
}
