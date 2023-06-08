package com.electrodiux.block;

import com.electrodiux.graphics.textures.SpriteAtlas;

public class Blocks {
        public static final byte NULL = -1;
        public static final byte AIR = 0;
        public static final byte GRASS_BLOCK = 1;
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
                SpriteAtlas spriteAtlas = new SpriteAtlas(BlockRegister.getTextureAtlas(), 16, 16, 17, 0);

                BlockRegister.blocksMetadata[Blocks.AIR] = null;

                BlockRegister.register("grass_block", Blocks.GRASS_BLOCK).setTexture("grass.png")
                                .setTexture(spriteAtlas.getSprite(0)).build();
                BlockRegister.register("dirt", Blocks.DIRT).setTexture("dirt.png")
                                .setTexture(spriteAtlas.getSprite(1)).build();
                BlockRegister.register("stone", Blocks.STONE).setTexture("stone.png")
                                .setTexture(spriteAtlas.getSprite(8)).build();
                BlockRegister.register("water", Blocks.WATER).setTexture("water.png")
                                .setTexture(spriteAtlas.getSprite(6)).build();
                BlockRegister.register("sand", Blocks.SAND).setTexture("sand.png")
                                .setTexture(spriteAtlas.getSprite(9)).build();
                BlockRegister.register("oak_log", Blocks.LOG).setTexture("log.png")
                                .setTexture(spriteAtlas.getSprite(13)).build();
                BlockRegister.register("leave", Blocks.LEAVE).setTexture("leave.png")
                                .setTexture(spriteAtlas.getSprite(14)).setTransparent(true)
                                .build();
                BlockRegister.register("iron", Blocks.IRON).setTexture("iron.png")
                                .setTexture(spriteAtlas.getSprite(16)).build();
                BlockRegister.register("flower", Blocks.FLOWER).setTexture("flower.png")
                                .setTexture(spriteAtlas.getSprite(0)).setTransparent(true)
                                .build();
                BlockRegister.register("lava", Blocks.LAVA).setTexture("lava.png")
                                .setTexture(spriteAtlas.getSprite(15)).build();
                BlockRegister.register("diorite", Blocks.DIORITE).setTexture("diorite.png")
                                .setTexture(spriteAtlas.getSprite(3)).build();
                BlockRegister.register("granite", Blocks.GRANITE).setTexture("granite.png")
                                .setTexture(spriteAtlas.getSprite(2)).build();
                BlockRegister.register("cobblestone", Blocks.COBBLESTONE).setTexture("cobblestone.png")
                                .setTexture(spriteAtlas.getSprite(4)).build();
                BlockRegister.register("mossy_cobblestone", Blocks.MOSSY_COBBLESTONE)
                                .setTexture("mossy_cobblestone.png").setTexture(spriteAtlas.getSprite(12))
                                .build();
                BlockRegister.register("oak_planks", Blocks.OAK_PLANKS).setTexture("oak_planks.png")
                                .setTexture(spriteAtlas.getSprite(10)).build();
                BlockRegister.register("stonebricks", Blocks.STONEBRICKS).setTexture("stone_bricks.png")
                                .setTexture(spriteAtlas.getSprite(7))
                                .build();
                BlockRegister.register("mossy_stonebricks", Blocks.MOSSY_STONEBRICKS)
                                .setTexture("mossy_stone_bricks.png").setTexture(spriteAtlas.getSprite(11))
                                .build();
                BlockRegister.register("blue_orchid", Blocks.BLUE_ORCHID).setTexture("blue_orchid.png")
                                .setTexture(spriteAtlas.getSprite(5))
                                .setTransparent(true)
                                .build();
                BlockRegister.register("paeonia", Blocks.PAEONIA).setTexture("paeonia.png")
                                .setTexture(spriteAtlas.getSprite(0)).setTransparent(true)
                                .build();
        }
}
