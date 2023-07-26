package com.electrodiux.main;

import java.util.Random;
import java.util.UUID;

import org.lwjgl.opengl.GL;

import com.electrodiux.Manager;
import com.electrodiux.block.BlockRegister;
import com.electrodiux.entities.Entity;
import com.electrodiux.entities.Entity.Properties;
import com.electrodiux.generation.FlatGenerator;
import com.electrodiux.graphics.Renderer;
import com.electrodiux.graphics.Window;
import com.electrodiux.util.Timer;
import com.electrodiux.world.World;

public class Main {

    private static Entity entity;
    private static World world;

    public static void main(String[] args) {
        Window window = new Window(640, 360, "Arena", true, false);
        GL.createCapabilities();
        load();

        long seed = new Random().nextLong();
        // seed = -757926025042869238L;

        System.out.println("Seed: " + seed);

        world = new World(seed);
        world.setGenerator(new FlatGenerator(world));

        System.out.println("Start generating:");
        long startGenerating = System.currentTimeMillis();
        world.generate(12);
        System.out.println("Time to generate: " + (System.currentTimeMillis() - startGenerating) + "ms");

        entity = new Entity(UUID.randomUUID(), new Properties().texture("blocks/stone.png").model("player.obj"));
        entity.getPosition().setY(100);

        world.addEntity(entity);

        Timer ticker = new Timer(20);
        ticker.addHandler(Main::tick);
        ticker.start();

        window.setVisibility(true);
        Renderer renderer = new Renderer(window);
        renderer.setWorld(world);
        renderer.run();
    }

    private static void tick() {
        world.tick();
        Manager.eventManager().dispatchEvents();
    }

    public static void load() {
        Manager.load();
        BlockRegister.endRegistry();
    }
}