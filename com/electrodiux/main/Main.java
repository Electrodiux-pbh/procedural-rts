package com.electrodiux.main;

import java.util.Map;
import java.util.Random;

import com.electrodiux.graphics.Keyboard;
import com.electrodiux.graphics.Mouse;
import com.electrodiux.graphics.SwingRenderer;
import com.electrodiux.util.Timer;
import com.electrodiux.util.TimerHandler;
import com.electrodiux.world.Chunk;
import com.electrodiux.world.World;

public class Main {

    private static int lastFps = 0;
    private static float posX, posY, posZ;

    public static void main(String[] args) {
        long seed = new Random().nextLong();
        seed = -757926025042869238L;

        System.out.println("Seed: " + seed);

        World world = new World(seed);
        long startGenerating = System.currentTimeMillis();
        world.generate();
        System.out.println("Time to generate: " + (System.currentTimeMillis() - startGenerating) + "ms");

        SwingRenderer window = new SwingRenderer();

        Timer timer = new Timer(60);
        timer.addHandler(new TimerHandler() {

            @Override
            public void update() {
                world.update(timer.getDeltaTime());
                window.render(world, posX, posZ, posY);

                float velocity = (Keyboard.isKeyPressed(Keyboard.VK_CONTROL) ? 50 : 20) *
                        timer.getDeltaTime();

                if (Keyboard.isKeyPressed(Keyboard.VK_W)) {
                    posZ += velocity;
                }
                if (Keyboard.isKeyPressed(Keyboard.VK_S)) {
                    posZ -= velocity;
                }
                if (Keyboard.isKeyPressed(Keyboard.VK_A)) {
                    posX += velocity;
                }
                if (Keyboard.isKeyPressed(Keyboard.VK_D)) {
                    posX -= velocity;
                }

                if (Mouse.getScrollY() != 0) {
                    posY += Mouse.getScrollY() * 50;
                }

                Mouse.updateMouse();
                if (timer.getFps() != lastFps) {
                    window.setTitle("Arena (" + timer.getFps() + ") fps");
                    lastFps = timer.getFps();
                }
            }
        });
        timer.start();

        Timer chunkTimer = new Timer(0.1F);
        chunkTimer.addHandler(new TimerHandler() {

            @Override
            public void update() {
                Map<Integer, Chunk> chunks = world.getChunks();

                int chunkX = ((int) (-2 * posZ - posX + SwingRenderer.blocksOnScreen / 2)
                        + 100) >> Chunk.CHUNK_SIZE_BYTESHIFT;
                int chunkZ = ((int) (-2 * posZ + posX - SwingRenderer.blocksOnScreen / 2)
                        + 100) >> Chunk.CHUNK_SIZE_BYTESHIFT;

                chunks.forEach((idx, chunk) -> {
                    if (chunk.getChunkX() < chunkX - 32 || chunk.getChunkX() > chunkX + 32
                            || chunk.getChunkZ() < chunkZ - 32 || chunk.getChunkZ() > chunkZ + 32) {
                        world.unloadChunk(chunk.getChunkX(), chunk.getChunkZ());
                    }
                });
            }
        });
        chunkTimer.start();
    }
}