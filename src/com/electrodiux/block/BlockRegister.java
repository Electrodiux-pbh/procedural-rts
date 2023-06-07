package com.electrodiux.block;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.electrodiux.assets.Loader;
import com.electrodiux.assets.Texture;

public class BlockRegister {

    private static final Map<String, BlockDefinition> blocks = new HashMap<>();

    public static final BlockDefinition[] blocksMetadata = new BlockDefinition[30];

    public static final BlockDefinition getBlockMetadata(int index) {
        if (index < 0 || index >= blocksMetadata.length)
            return null;
        return blocksMetadata[index];
    }

    public BlockDefinition getBlockMetadata(String blockId) {
        return blocks.get(blockId);
    }

    public static BlockDefinitionRegister register(String blockId, short index) {
        if (blocks.containsKey(blockId)) {
            throw new IllegalStateException("The block with id \"" + blockId + "\" already exists");
        }
        return new BlockDefinitionRegister(blockId, index);
    }

    public static class BlockDefinitionRegister {
        String blockName;
        String blockId;

        Color mapColor;
        Texture texture;
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
            this.texture = Loader.loadTexture(texturePath);
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
