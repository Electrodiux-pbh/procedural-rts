package com.electrodiux.block;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.electrodiux.graphics.Loader;
import com.electrodiux.graphics.textures.Sprite;
import com.electrodiux.graphics.textures.Texture;

public class BlockRegister {

    private static final Map<String, BlockDefinition> blocks = new HashMap<>();

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

    private static Texture atlasTexture;

    public static Texture getTextureAtlas() {
        if (atlasTexture == null) {
            try {
                atlasTexture = Loader.loadTexture("/assets/textures/blocks/atlas.png", GL11.GL_NEAREST);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return atlasTexture;
    }

    public static class BlockDefinitionRegister {
        String blockName;
        String blockId;

        Color mapColor;
        Sprite texture;
        boolean transparent;

        private short index;

        protected BlockDefinitionRegister(String blockId, short index) {
            this.blockId = blockId;
            this.index = index;
        }

        public BlockDefinitionRegister setColor(Color c) {
            this.mapColor = c;
            return this;
        }

        public BlockDefinitionRegister setTexture(String texturePath) {
            setTexture(texturePath, GL11.GL_NEAREST);
            return this;
        }

        public BlockDefinitionRegister setTexture(String texturePath, int filter) {
            // try {
            // this.texture = Loader.loadTexture("/assets/textures/blocks/" + texturePath,
            // filter);
            // } catch (IOException e) {
            // e.printStackTrace();
            // }
            return this;
        }

        public BlockDefinitionRegister setTexture(Sprite texture) {
            this.texture = texture;
            return this;
        }

        public BlockDefinitionRegister setTransparent(boolean transparent) {
            this.transparent = transparent;
            return this;
        }

        public BlockDefinitionRegister setBlockName(String blockName) {
            this.blockName = blockName;
            return this;
        }

        public BlockDefinition build() {
            if (blockName == null) {
                blockName = blockId;
            }

            BlockDefinition block = new BlockDefinition(this);

            if (blocks.containsKey(blockId)) {
                throw new IllegalStateException("The block with id " + blockId + " already exists");
            }
            blocks.put(blockId, block);

            blocksMetadata[index] = block;

            System.out.println("Registered block: \"" + blockId + "\" with index of " + index);

            return block;
        }
    }

}
