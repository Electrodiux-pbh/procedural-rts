package com.electrodiux.graphics;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JFrame;

import com.electrodiux.Position;
import com.electrodiux.assets.Texture;
import com.electrodiux.block.BlockDefinition;
import com.electrodiux.block.BlockRegister;
import com.electrodiux.block.Blocks;
import com.electrodiux.entities.Entity;
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

        frame.setVisible(true);

        canvas.createBufferStrategy(2);
    }

    public void setTitle(String string) {
        this.frame.setTitle(string);
    }

    public static final int blocksOnScreen = 50;

    private final Vector<Tile> renderCache = new Vector<>(blocksOnScreen * blocksOnScreen);
    private final BufferedImage[] textureCache = new BufferedImage[BlockRegister.blocksMetadata.length];
    private int cacheX, cacheZ, cacheSize;

    private static class Tile implements Comparable<Tile> {
        public BufferedImage texture;
        public int xR, yR;
        public float x, y, z;

        public byte type;

        int lenX, lenZ;

        public Tile(BufferedImage texture, int xR, int yR, float x, float y, float z, int lenX, int lenZ, int type) {
            this.texture = texture;
            this.xR = xR;
            this.yR = yR;

            this.x = x;
            this.y = y;
            this.z = z;

            this.lenX = lenX;
            this.lenZ = lenZ;

            this.type = (byte) type;
        }

        public void set(BufferedImage texture, int xR, int yR, float x, float y, float z, int lenX, int lenZ,
                int type) {
            this.texture = texture;
            this.xR = xR;
            this.yR = yR;

            this.x = x;
            this.y = y;
            this.z = z;

            this.lenX = lenX;
            this.lenZ = lenZ;

            this.type = (byte) type;
        }

        public int compareTo(Tile t) throws ClassCastException {
            int yCompare = Integer.compare((int) y, (int) t.y);
            if (yCompare != 0)
                return yCompare;

            if (this.z < t.z)
                return -1;
            if (this.z > t.z)
                return 1;
            if (this.x < t.x)
                return -1;
            if (this.x > t.x)
                return 1;
            int i = yR + this.lenZ + this.lenX / 2;
            int j = t.yR + t.lenZ + t.lenX / 2;
            if (i < j)
                return -1;
            if (i > j)
                return 1;
            return 0;

        }
    }

    private static final Color SkyBoxColor = new Color(0x66baff);

    public void render(World world, float posX, float posZ, float posY) {
        BufferStrategy bf = canvas.getBufferStrategy();

        Graphics g = bf.getDrawGraphics();

        g.setColor(SkyBoxColor);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        int size = canvas.getWidth() / blocksOnScreen;

        int tileXFactor = size / 2;
        int tileZFactor = -(size / 2);
        int tileYFactor = size / 4;

        int xStart = (int) (-2 * posZ - posX) + 125;
        int zStart = (int) (-2 * posZ + posX) + 75;

        int xEnd = xStart + blocksOnScreen;
        int zEnd = zStart + blocksOnScreen;

        if (cacheX != xStart || cacheZ != zStart || cacheSize != size) {
            cacheX = xStart;
            cacheZ = zStart;

            renderCache.clear();

            if (cacheSize != size) {
                for (int i = 0; i < BlockRegister.blocksMetadata.length; i++) {
                    BlockDefinition block = BlockRegister.blocksMetadata[i];
                    if (block == null)
                        continue;
                    textureCache[i] = Texture.redimension(block.getTexture(), size, size);
                }
                cacheSize = size;
            }

            int cacheIndex = 0;
            for (int x = xStart; x < xEnd; x++) {
                for (int z = zStart; z < zEnd; z++) {
                    int chunkX = x / Chunk.CHUNK_SIZE;
                    int chunkZ = z / Chunk.CHUNK_SIZE;
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

                            if (cacheIndex < renderCache.size()) {
                                Tile tile = renderCache.get(cacheIndex);
                                tile.set(texture, xR, yR, x, y, z, size, size, 1);
                            } else {
                                renderCache.add(new Tile(texture, xR, yR, x, y, z, size, size, 1));
                            }
                            cacheIndex++;
                        }
                    }
                }
            }

            if (cacheIndex < renderCache.size()) {
                renderCache.subList(cacheIndex, renderCache.size()).clear();
            }
        }

        Collection<Entity> entities = world.getEntities();
        for (Entity entity : entities) {
            Position pos = entity.getPosition();

            if (pos.getX() < xStart || pos.getX() > xEnd || pos.getZ() < zStart || pos.getZ() > zEnd)
                continue;

            int xR = (int) (((pos.getX() * tileXFactor + pos.getZ() * tileZFactor) - tileXFactor) +
                    posX * size);
            int yR = (int) (((pos.getX() - pos.getY()) * tileYFactor + (pos.getZ() - pos.getY()) * tileYFactor)
                    + posZ * size);

            renderCache.add(
                    new Tile(entity.getTexture().getData(), xR, yR, pos.getX() + 0.5f, pos.getY(), pos.getZ() + 0.5f,
                            size, size, 2));
        }

        renderCache(g, size, posY);

        g.setColor(Color.RED);
        g.drawString("PosX: " + posX + " PosZ: " + posZ + " PosY: " + posY, 4, 14);

        g.dispose();
        bf.show();
    }

    private void renderCache(Graphics g, int size, float posY) {
        Collections.sort(renderCache);

        Iterator<Tile> iter = renderCache.iterator();

        while (iter.hasNext()) {
            Tile tile = iter.next();
            if (tile == null)
                break;
            int y = (int) (tile.yR - posY);
            int x = tile.xR;
            if (y > -size && y < canvas.getHeight() && x > -size && x < canvas.getWidth()) {
                g.drawImage(tile.texture, x, y, size, size, null);
            }
            if (tile.type == 2)
                iter.remove();
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
