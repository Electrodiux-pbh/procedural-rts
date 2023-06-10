package com.electrodiux.main;

import java.util.Random;
import java.util.UUID;

import org.lwjgl.opengl.GL;

import com.electrodiux.block.BlockRegister;
import com.electrodiux.block.Blocks;
import com.electrodiux.entities.Entity;
import com.electrodiux.entities.Entity.Properties;
import com.electrodiux.generation.WorldGenerator;
import com.electrodiux.graphics.Renderer;
import com.electrodiux.graphics.Window;
import com.electrodiux.world.World;

public class Main {

    private static Entity entity;

    public static void main(String[] args) {
        Window window = new Window(640, 360, "Arena", true, false);
        GL.createCapabilities();
        load();

        long seed = new Random().nextLong();
        seed = -757926025042869238L;

        System.out.println("Seed: " + seed);

        World world = new World(seed);
        world.setGenerator(new WorldGenerator(world));
        System.out.println("Start generating:");
        long startGenerating = System.currentTimeMillis();
        world.generate(16);
        System.out.println("Time to generate: " + (System.currentTimeMillis() - startGenerating) + "ms");

        entity = new Entity(UUID.randomUUID(), new Properties().texture("entity.png"));
        entity.getPosition()
                .setY(world.getHighestYAt(entity.getPosition().getBlockX(), entity.getPosition().getBlockZ()) + 1);

        world.addEntity(entity);

        window.setVisibility(true);
        Renderer renderer = new Renderer(window);
        renderer.setWorld(world);
        renderer.run();
    }

    public static void load() {
        Blocks.loadBlocks();
        BlockRegister.endRegistry();
    }
}