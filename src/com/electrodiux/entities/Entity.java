package com.electrodiux.entities;

import java.io.IOException;
import java.util.UUID;

import org.lwjgl.opengl.GL11;

import com.electrodiux.Position;
import com.electrodiux.graphics.Loader;
import com.electrodiux.graphics.Model;
import com.electrodiux.graphics.textures.Texture;
import com.electrodiux.math.Vector3;

public class Entity {

    private Position position;
    private Vector3 rotation;

    private UUID uuid;

    private Properties properties;

    public Entity(UUID uuid, Properties properties) {
        this.position = new Position();
        this.rotation = new Vector3();
        this.uuid = uuid;
        this.properties = properties;
    }

    public Position getPosition() {
        return position;
    }

    public Vector3 getRotation() {
        return rotation;
    }

    public Texture getTexture() {
        return properties.texture;
    }

    public Model getModel() {
        return properties.model;
    }

    public Properties getProperties() {
        return properties;
    }

    public UUID getUUID() {
        return uuid;
    }

    public static class Properties {

        private Texture texture;
        private Model model;

        public Properties texture(String texturePath) {
            try {
                this.texture = Loader.loadTexture("/assets/textures/" + texturePath, GL11.GL_NEAREST);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return this;
        }

        public Properties model(String modelPath) {
            try {
                this.model = Loader.loadObjModel("/assets/models/" + modelPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return this;
        }

    }

}
