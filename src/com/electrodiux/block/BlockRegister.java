package com.electrodiux.block;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.lwjgl.opengl.GL11;

import com.electrodiux.graphics.textures.Sprite;
import com.electrodiux.graphics.textures.Texture;
import com.electrodiux.graphics.textures.TextureAtlas;

public class BlockRegister {

    private static final Map<String, BlockDefinition> blocks = new HashMap<>();
    private static List<BlockDefinitionRegister> blockRegisters = new ArrayList<>();

    public static final BlockDefinition[] blocksMetadata = new BlockDefinition[30];

    public static final BlockDefinition getBlock(int index) {
        if (index < 0 || index >= blocksMetadata.length)
            return null;
        return blocksMetadata[index];
    }

    public static BlockDefinition getBlock(String blockId) {
        return blocks.get(blockId);
    }

    public static BlockDefinitionRegister register(String blockId, short index) {
        if (blocks.containsKey(blockId)) {
            throw new IllegalStateException("The block with id \"" + blockId + "\" already exists");
        }
        return new BlockDefinitionRegister(blockId, index);
    }

    private static TextureAtlas textureAtlas = new TextureAtlas(16, 16);
    private static Texture atlasTexture;

    public static Texture getAtlasTexture() {
        return atlasTexture;
    }

    public static void endRegistry() {
        atlasTexture = textureAtlas.createAtlasTexture(GL11.GL_NEAREST, true, 4);
        List<Sprite> sprites = textureAtlas.getTextureSprites(atlasTexture);

        for (BlockDefinitionRegister register : blockRegisters) {
            register.textures = new Sprite[register.textureAtlasIndices.length];
            for (int i = 0; i < register.textureAtlasIndices.length; i++) {
                register.textures[i] = sprites.get(register.textureAtlasIndices[i]);
            }

            BlockDefinition block = new BlockDefinition(register);

            if (blocks.containsKey(register.blockId)) {
                throw new IllegalStateException("The block with id " + register.blockId + " already exists");
            }
            blocks.put(register.blockId, block);

            blocksMetadata[register.index] = block;

            System.out.println("Registered block: \"" + register.blockId + "\" with index of " + register.index);
        }

        textureAtlas = null;
    }

    public static class BlockDefinitionRegister {
        String blockName;
        String blockId;

        Color mapColor;
        boolean transparent;
        boolean internalFaces = false;

        Sprite[] textures;
        private int[] textureAtlasIndices;

        private short index;

        protected BlockDefinitionRegister(String blockId, short index) {
            this.blockId = blockId;
            this.index = index;
            this.textureAtlasIndices = new int[6];
            Arrays.fill(textureAtlasIndices, -1);
        }

        public BlockDefinitionRegister setColor(Color c) {
            this.mapColor = c;
            return this;
        }

        public BlockDefinitionRegister setTexture(String texturePath) {
            try {
                int textureIndex = textureAtlas.loadTexture("/assets/textures/blocks/" + texturePath);

                for (int i = 0; i < textureAtlasIndices.length; i++) {
                    if (textureAtlasIndices[i] == -1)
                        textureAtlasIndices[i] = textureIndex;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return this;
        }

        public BlockDefinitionRegister setTexture(String texturePath, int... indicies) {
            try {
                int textureIndex = textureAtlas.loadTexture("/assets/textures/blocks/" + texturePath);

                for (int idx : indicies) {
                    Objects.checkIndex(idx, textureAtlasIndices.length);
                    textureAtlasIndices[idx] = textureIndex;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return this;
        }

        public BlockDefinitionRegister setTransparent(boolean transparent) {
            this.transparent = transparent;
            return this;
        }

        public BlockDefinitionRegister setInternalFaces(boolean internalFaces) {
            this.internalFaces = internalFaces;
            return this;
        }

        public BlockDefinitionRegister setBlockName(String blockName) {
            this.blockName = blockName;
            return this;
        }

        public void register() {
            if (blockName == null) {
                blockName = blockId;
            }

            blockRegisters.add(this);
        }
    }

}
