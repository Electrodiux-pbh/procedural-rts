package com.electrodiux.graphics;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import com.electrodiux.assets.Texture;
import com.electrodiux.block.BlockMetadata;
import com.electrodiux.block.BlockRegister;
import com.electrodiux.block.Blocks;
import com.electrodiux.world.Chunk;
import com.electrodiux.world.World;

public class SwingRenderer {

    private JFrame frame;
    private Canvas canvas;

    public SwingRenderer() {
        frame = new JFrame();

        frame.setSize(50 * 13, 800);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        canvas = new Canvas();
        frame.add(canvas);

        canvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                Keyboard.keyPressed(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                Keyboard.keyReleased(e.getKeyCode());
            }
        });

        canvas.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                Mouse.mouseButtonCallBack(e.getButton(), Mouse.PRESSED);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Mouse.mouseButtonCallBack(e.getButton(), Mouse.RELEASED);
            }

        });

        canvas.addMouseWheelListener(new MouseWheelListener() {

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                Mouse.mouseScrollCallBack(0, e.getWheelRotation());
            }

        });

        canvas.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Mouse.mousePosCallBack(e.getX(), e.getY());
            }

        });

        frame.setVisible(true);

        canvas.createBufferStrategy(2);
    }

    public void setTitle(String string) {
        this.frame.setTitle(string);
    }

    public static final int blocksOnScreen = 50;

    private final Tile[] renderCache = new Tile[blocksOnScreen * blocksOnScreen * 128];
    private final BufferedImage[] textureCache = new BufferedImage[BlockRegister.blocksMetadata.length];
    private int cacheX, cacheZ, cacheSize;

    private static class Tile {
        public BufferedImage texture;
        public int x, y;

        public Tile(BufferedImage texture, int x, int y) {
            this.texture = texture;
            this.x = x;
            this.y = y;
        }
    }

    private static final Color SkyBoxColor = new Color(0x66baff);

    public void render(World world, float posX, float posZ, float posY) {
        BufferStrategy bf = canvas.getBufferStrategy();

        Graphics g = bf.getDrawGraphics();

        g.setColor(SkyBoxColor);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        int size = canvas.getWidth() / blocksOnScreen;

        int xStart = (int) (-2 * posZ - posX) + 125;
        int zStart = (int) (-2 * posZ + posX) + 75;

        if (cacheX == xStart && cacheZ == zStart && cacheSize == size) {
            renderCache(g, size, posY);
        } else {
            cacheX = xStart;
            cacheZ = zStart;

            if (cacheSize != size) {
                for (int i = 0; i < BlockRegister.blocksMetadata.length; i++) {
                    BlockMetadata block = BlockRegister.blocksMetadata[i];
                    if (block == null)
                        continue;
                    textureCache[i] = Texture.redimension(block.getTexture(), size, size);
                }
                cacheSize = size;
            }

            int xEnd = xStart + blocksOnScreen;
            int zEnd = zStart + blocksOnScreen;

            int tileXFactor = size >> 1;
            int tileZFactor = -(size >> 1);
            int tileYFactor = size >> 2;

            int cacheIdx = 0;
            for (int x = xStart; x < xEnd; x++) {
                for (int z = zStart; z < zEnd; z++) {
                    int chunkX = x >> Chunk.CHUNK_SIZE_BYTESHIFT;
                    int chunkZ = z >> Chunk.CHUNK_SIZE_BYTESHIFT;
                    Chunk chunk = world.getChunk(chunkX, chunkZ);
                    if (chunk == null) {
                        world.loadChunk(chunkX, chunkZ);
                        continue;
                    }
                    for (int y = 0; y < world.getWorldHeight(); y++) {
                        short[] blocks = chunk.getBlocks();

                        int blockIndex = Chunk.getBlockIndexWithWorldCoords(x, y, z);

                        if (blocks[blockIndex] != Blocks.AIR && isVisible(world, x, y, z, xStart, zStart, xEnd, zEnd)) {
                            BufferedImage texture = textureCache[blocks[blockIndex]];

                            if (texture == null) {
                                continue;
                            }

                            int xR = (int) (((x * tileXFactor + z * tileZFactor) - tileXFactor) +
                                    posX * size);
                            int yR = (int) (((x - y) * tileYFactor + (z - y) * tileYFactor) + posZ * size);

                            if (renderCache[cacheIdx] == null) {
                                renderCache[cacheIdx] = new Tile(texture, xR, yR);
                            } else {
                                Tile t = renderCache[cacheIdx];
                                t.x = xR;
                                t.y = yR;
                                t.texture = texture;
                            }
                            cacheIdx++;

                            yR -= posY;

                            if (xR > -size && xR < canvas.getWidth() && yR > -size && yR < canvas.getHeight()) {
                                g.drawImage(texture, xR, yR, size, size, null);
                            }
                        }
                    }
                }
            }
            while (renderCache[cacheIdx] != null) {
                renderCache[cacheIdx] = null;
                cacheIdx++;
            }
        }

        g.setColor(Color.RED);
        g.drawString("PosX: " + posX + " PosZ: " + posZ + " PosY: " + posY, 4, 14);

        g.dispose();
        bf.show();
    }

    private void renderCache(Graphics g, int size, float posY) {
        for (Tile tile : renderCache) {
            if (tile == null)
                break;
            int y = (int) (tile.y - posY);
            int x = tile.x;
            if (x > -size && x < canvas.getWidth() && y > -size && y < canvas.getHeight()) {
                g.drawImage(tile.texture, x, y, size, size, null);
            }
        }
    }

    private boolean isVisible(World world, int x, int y, int z, int xStart, int zStart, int xEnd, int zEnd) {
        if (isHideBlock(world, x, y + 1, z))
            return true;
        if (isHideBlock(world, x, y, z + 1))
            return true;
        if (isHideBlock(world, x + 1, y, z))
            return true;
        if (x == xEnd - 1 || z == zEnd - 1 || x == xStart || z == zStart)
            return true;
        return false;
    }

    private boolean isHideBlock(World world, int x, int y, int z) {
        if (y < 0 || y >= world.getWorldHeight()) {
            return false;
        }
        short block = world.getBlock(x, y, z);
        return block == Blocks.AIR || BlockRegister.getBlockMetadata(block).isTransparent();
    }

}
