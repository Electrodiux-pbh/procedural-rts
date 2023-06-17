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
import com.electrodiux.util.Timer;
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
        world.generate(12);
        System.out.println("Time to generate: " + (System.currentTimeMillis() -
                startGenerating) + "ms");

        entity = new Entity(UUID.randomUUID(), new Properties().texture("blocks/stone.png").model("player.obj"));
        entity.getPosition().setY(100);

        world.addEntity(entity);

        Timer ticker = new Timer(20);
        ticker.addHandler(world::tick);
        ticker.start();

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