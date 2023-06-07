package com.electrodiux.entities;

import java.io.IOException;
import java.util.UUID;

import com.electrodiux.Position;
import com.electrodiux.graphics.Loader;
import com.electrodiux.graphics.Texture;

public class Entity {

    private Position position;
    private UUID uuid;

    private Properties properties;

    public Entity(UUID uuid, Properties properties) {
        this.position = new Position();
        this.uuid = uuid;
        this.properties = properties;
    }

    public Position getPosition() {
        return position;
    }

    public Texture getTexture() {
        return properties.texture;
    }

    public Properties getProperties() {
        return properties;
    }

    public UUID getUuid() {
        return uuid;
    }

    public static class Properties {

        private Texture texture;

        public Properties texture(String texturePath) {
            try {
                this.texture = Loader.loadTexture("/assets/textures/" + texturePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return this;
        }

    }

}
