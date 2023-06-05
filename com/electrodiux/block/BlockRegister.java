package com.electrodiux.block;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.electrodiux.assets.Loader;
import com.electrodiux.assets.Texture;

public class BlockRegister {

    private static final Map<String, BlockMetadata> blocks = new HashMap<>();

    public static final BlockMetadata[] blocksMetadata = new BlockMetadata[30];

    public static final BlockMetadata getBlockMetadata(int index) {
        if (index < 0 || index >= blocksMetadata.length)
            return null;
        return blocksMetadata[index];
    }

    public BlockMetadata getBlockMetadata(String blockId) {
        return blocks.get(blockId);
    }

    public static BlockMetadataRegistryBuilder register(String blockId, short index) {
        if (blocks.containsKey(blockId)) {
            throw new IllegalStateException("The block with id \"" + blockId + "\" already exists");
        }
        return new BlockMetadataRegistryBuilder(blockId, index);
    }

    public static class BlockMetadataRegistryBuilder {
        String blockName;
        String blockId;

        Color mapColor;
        Texture texture;
        boolean transparent;

        private short index;

        protected BlockMetadataRegistryBuilder(String blockId, short index) {
            this.blockId = blockId;
            this.index = index;
        }

        public BlockMetadataRegistryBuilder setColor(Color c) {
            this.mapColor = c;
            return this;
        }

        public BlockMetadataRegistryBuilder setTexture(String texturePath) {
            this.texture = Loader.loadTexture(texturePath);
            return this;
        }

        public BlockMetadataRegistryBuilder setTransparent(boolean transparent) {
            this.transparent = transparent;
            return this;
        }

        public BlockMetadataRegistryBuilder setBlockName(String blockName) {
            this.blockName = blockName;
            return this;
        }

        public BlockMetadata build() {
            if (blockName == null) {
                blockName = blockId;
            }

            BlockMetadata block = new BlockMetadata(this);

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
