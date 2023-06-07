package com.electrodiux.generation;

import com.electrodiux.register.Registrable;
import com.electrodiux.world.Chunk;

public abstract class Structure implements Registrable {

    private String structureId;

    private double rarity;

    public Structure(Properties properties) {
        this.structureId = properties.structureId;
        this.rarity = properties.rarity;
    }

    public String getStructureId() {
        return structureId;
    }

    public double getRarity() {
        return rarity;
    }

    @Override
    public String getRegistryName() {
        return structureId;
    }

    public abstract void generate(Chunk chunk);

    public static class Properties {

        private String structureId;
        private double rarity;

        public Properties(String structureId) {
            this.structureId = structureId;
            this.rarity = 1;
        }

        public Properties rarity(double rarity) {
            this.rarity = rarity;
            return this;
        }

    }

}
