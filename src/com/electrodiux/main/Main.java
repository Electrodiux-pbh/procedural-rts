package com.electrodiux.main;

import java.util.Random;
import java.util.UUID;

import org.lwjgl.opengl.GL;

import com.electrodiux.block.Blocks;
import com.electrodiux.entities.Entity;
import com.electrodiux.entities.Entity.Properties;
import com.electrodiux.generation.WorldGenerator;
import com.electrodiux.graphics.Renderer;
import com.electrodiux.graphics.Window;
import com.electrodiux.world.World;

public class Main {

    // private static int lastFps = 0;
    // private static float posX, posY, posZ;
    // private static Entity entity;

    // public static void main(String[] args) {
    // load();

    // long seed = new Random().nextLong();
    // seed = -757926025042869238L;

    // System.out.println("Seed: " + seed);

    // World world = new World(seed);
    // long startGenerating = System.currentTimeMillis();
    // world.generate();
    // System.out.println("Time to generate: " + (System.currentTimeMillis() -
    // startGenerating) + "ms");

    // entity = new Entity(UUID.randomUUID(), new
    // Properties().texture("entity2.png"));
    // entity.getPosition()
    // .setY(world.getHighestYAt(entity.getPosition().getBlockX(),
    // entity.getPosition().getBlockZ()) + 1);

    // world.addEntity(entity);

    // SwingRenderer window = new SwingRenderer();

    // Timer timer = new Timer(60);
    // timer.addHandler(new TimerHandler() {

    // @Override
    // public void update() {
    // window.render(world, posX, posZ, posY);

    // float velocity = (Keyboard.isKeyPressed(Keyboard.GLFW_KEY_RIGHT_CONTROL) ? 50
    // : 20) *
    // timer.getDeltaTime();

    // float entityVelocity =
    // (Keyboard.isKeyPressed(Keyboard.GLFW_KEY_RIGHT_CONTROL) ? 30 : 8) *
    // timer.getDeltaTime();

    // if (Keyboard.isKeyPressed(Keyboard.GLFW_KEY_W)) {
    // posZ += velocity;
    // }
    // if (Keyboard.isKeyPressed(Keyboard.GLFW_KEY_S)) {
    // posZ -= velocity;
    // }
    // if (Keyboard.isKeyPressed(Keyboard.GLFW_KEY_A)) {
    // posX += velocity;
    // }
    // if (Keyboard.isKeyPressed(Keyboard.GLFW_KEY_D)) {
    // posX -= velocity;
    // }

    // boolean moved = false;

    // if (Keyboard.isKeyPressed(Keyboard.GLFW_KEY_UP)) {
    // entity.getPosition().add(-entityVelocity, 0, 0);
    // moved = true;
    // }
    // if (Keyboard.isKeyPressed(Keyboard.GLFW_KEY_DOWN)) {
    // entity.getPosition().add(entityVelocity, 0, 0);
    // moved = true;
    // }
    // if (Keyboard.isKeyPressed(Keyboard.GLFW_KEY_LEFT)) {
    // entity.getPosition().add(0, 0, entityVelocity);
    // moved = true;
    // }
    // if (Keyboard.isKeyPressed(Keyboard.GLFW_KEY_RIGHT)) {
    // entity.getPosition().add(0, 0, -entityVelocity);
    // moved = true;
    // }

    // if (moved == true) {
    // Position pos = entity.getPosition();
    // pos.setY(world.getHighestYAt(pos.getBlockX(), pos.getBlockZ()) + 1);
    // }

    // if (Mouse.getScrollY() != 0) {
    // posY += Mouse.getScrollY() * 50;
    // }

    // if (timer.getFps() != lastFps) {
    // window.setTitle("Arena (" + timer.getFps() + ") fps");
    // lastFps = timer.getFps();
    // }
    // }
    // });
    // timer.start();

    // Timer chunkTimer = new Timer(0.1F);
    // chunkTimer.addHandler(new TimerHandler() {

    // @Override
    // public void update() {
    // Collection<Chunk> chunks = world.getChunks();

    // int chunkX = ((int) (-2 * posZ - posX + SwingRenderer.blocksOnScreen / 2)
    // + 100) / Chunk.CHUNK_SIZE;
    // int chunkZ = ((int) (-2 * posZ + posX - SwingRenderer.blocksOnScreen / 2)
    // + 100) / Chunk.CHUNK_SIZE;

    // for (Chunk chunk : chunks) {
    // if (chunk.getXPos() < chunkX - 32 || chunk.getXPos() > chunkX + 32
    // || chunk.getZPos() < chunkZ - 32 || chunk.getZPos() > chunkZ + 32) {
    // world.unloadChunk(chunk.getXPos(), chunk.getZPos());
    // }
    // }
    // }
    // });
    // chunkTimer.start();
    // }

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
    }
}