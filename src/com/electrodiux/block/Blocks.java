package com.electrodiux.block;

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
        public static final byte RED_TULIP = 9;
        public static final byte LAVA = 10;
        public static final byte DIORITE = 11;
        public static final byte GRANITE = 12;
        public static final byte COBBLESTONE = 13;
        public static final byte MOSSY_COBBLESTONE = 14;
        public static final byte OAK_PLANKS = 15;
        public static final byte STONEBRICKS = 16;
        public static final byte MOSSY_STONEBRICKS = 17;
        public static final byte BLUE_ORCHID = 18;
        public static final byte POPPY = 19;
        public static final byte TORCH = 20;

        public static final int FACE_TOP = 4;
        public static final int FACE_BOTTOM = 5;
        public static final int FACE_RIGHT = 3;
        public static final int FACE_LEFT = 2;
        public static final int FACE_FRONT = 0;
        public static final int FACE_BACK = 1;

        public static void loadBlocks() {
                BlockRegister.blocksMetadata[Blocks.AIR] = null;

                BlockRegister.register("grass_block", Blocks.GRASS_BLOCK)
                                .setTexture("grass_block.png")
                                .setTexture("grass_block_top.png", FACE_TOP)
                                .setTexture("dirt.png", FACE_BOTTOM)
                                .register();

                BlockRegister.register("dirt", Blocks.DIRT)
                                .setTexture("dirt.png")
                                .register();

                BlockRegister.register("stone", Blocks.STONE)
                                .setTexture("stone.png")
                                .register();

                BlockRegister.register("water", Blocks.WATER)
                                .setTexture("water.png")
                                .setTransparent(true)
                                .setTranslucent(true)
                                .register();

                BlockRegister.register("sand", Blocks.SAND)
                                .setTexture("sand.png")
                                .register();

                BlockRegister.register("oak_log", Blocks.LOG)
                                .setTexture("log.png")
                                .setTexture("log_top.png", FACE_TOP, FACE_BOTTOM)
                                .setTexture("log.png").register();

                BlockRegister.register("leave", Blocks.LEAVE)
                                .setTexture("leave.png")
                                .setTransparent(true)
                                .setInternalFaces(true)
                                .register();

                BlockRegister.register("iron", Blocks.IRON)
                                .setTexture("iron.png")
                                .register();

                BlockRegister.register("red_tulip", Blocks.RED_TULIP)
                                .setTexture("red_tulip.png")
                                .setTransparent(true)
                                .setTranslucent(true)
                                .register();

                BlockRegister.register("lava", Blocks.LAVA)
                                .setTexture("lava.png")
                                .setTransparent(true)
                                .setLightEmision(12)
                                .register();

                BlockRegister.register("diorite", Blocks.DIORITE)
                                .setTexture("diorite.png")
                                .register();

                BlockRegister.register("granite", Blocks.GRANITE)
                                .setTexture("granite.png")
                                .register();

                BlockRegister.register("cobblestone", Blocks.COBBLESTONE)
                                .setTexture("cobblestone.png")
                                .register();

                BlockRegister.register("mossy_cobblestone", Blocks.MOSSY_COBBLESTONE)
                                .setTexture("mossy_cobblestone.png")
                                .register();

                BlockRegister.register("oak_planks", Blocks.OAK_PLANKS)
                                .setTexture("oak_planks.png")
                                .register();

                BlockRegister.register("stonebricks", Blocks.STONEBRICKS)
                                .setTexture("stone_bricks.png")
                                .register();

                BlockRegister.register("mossy_stonebricks", Blocks.MOSSY_STONEBRICKS)
                                .setTexture("mossy_stone_bricks.png")
                                .register();

                BlockRegister.register("blue_orchid", Blocks.BLUE_ORCHID)
                                .setTexture("blue_orchid.png")
                                .setTransparent(true)
                                .setTranslucent(true)
                                .register();

                BlockRegister.register("poppy", Blocks.POPPY)
                                .setTexture("poppy.png")
                                .setTransparent(true)
                                .setTranslucent(true)
                                .register();

                BlockRegister.register("torch", Blocks.TORCH)
                                .setTexture("torch.png")
                                .setTransparent(true)
                                .setLightEmision(5)
                                .register();
        }
}
