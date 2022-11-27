package com.electrodiux.main;

import java.util.Random;

import com.electrodiux.graphics.Keyboard;
import com.electrodiux.graphics.Mouse;
import com.electrodiux.graphics.SwingRenderer;
import com.electrodiux.util.Timer;
import com.electrodiux.util.TimerHandler;
import com.electrodiux.world.World;

public class Main {

    public static void main(String[] args) {
        long seed = new Random().nextLong();

        System.out.println("Seed: " + seed);

        World world = new World(seed);
        long startGenerating = System.currentTimeMillis();
        world.generate();
        System.out.println("Time to generate: " + (System.currentTimeMillis() - startGenerating) + "ms");

        SwingRenderer window = new SwingRenderer();
        Timer timer = new Timer(60);
        timer.addHandler(new TimerHandler() {

            private int lastFps;
            private float posX, posZ, posY;

            @Override
            public void update() {
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
    }
}