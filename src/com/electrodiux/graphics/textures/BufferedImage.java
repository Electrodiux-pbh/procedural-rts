package com.electrodiux.graphics.textures;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImageWrite;

public class BufferedImage {

    private ByteBuffer data;
    private int width, height;
    private int numChannels;

    public BufferedImage(int width, int height, int numChannels) {
        this(width, height, numChannels, null);
    }

    public BufferedImage(int width, int height, int numChannels, ByteBuffer data) {
        this.width = width;
        this.height = height;
        this.numChannels = numChannels;

        if (data != null) {
            this.data = data;
        } else {
            this.data = BufferUtils.createByteBuffer(width * height * numChannels);
        }
    }

    public void setColor(int x, int y, int color) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            return;

        int location = (x + width * y) * numChannels;

        if (numChannels >= 1)
            data.put(location, (byte) ((color >> 24) & 0xFF));
        if (numChannels >= 2)
            data.put(location + 1, (byte) ((color >> 16) & 0xFF));
        if (numChannels >= 3)
            data.put(location + 2, (byte) ((color >> 8) & 0xFF));
        if (numChannels >= 4)
            data.put(location + 3, (byte) (color & 0xFF));

    }

    public int getColor(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            return 0;

        int location = (x + width * y) * numChannels;
        int color = 0;

        if (numChannels >= 1)
            color |= (data.get(location) & 0xFF) << 24;
        if (numChannels >= 2)
            color |= (data.get(location + 1) & 0xFF) << 16;
        if (numChannels >= 3)
            color |= (data.get(location + 2) & 0xFF) << 8;
        if (numChannels >= 4)
            color |= (data.get(location + 3) & 0xFF);
        else
            color |= 0xFF;

        return color;
    }

    public void drawImage(int xPos, int yPos, int width, int height, BufferedImage img) {
        for (int x = 0; x < img.getWidth(); x++) {
            if (x >= width)
                continue;
            for (int y = 0; y < img.getHeight(); y++) {
                if (y >= height)
                    continue;
                this.setColor(x + xPos, y + yPos, img.getColor(x, y));
            }
        }
    }

    public void flipVertically() {
        int rowSize = width * numChannels;
        int rowCount = height / 2;
        byte[] rowBuffer = new byte[rowSize];

        for (int y = 0; y < rowCount; y++) {
            int topOffset = y * rowSize;
            int bottomOffset = (height - y - 1) * rowSize;

            // Copy top row to buffer
            data.position(topOffset);
            data.get(rowBuffer);

            // Copy bottom row to top row
            data.position(bottomOffset);
            byte[] topRow = new byte[rowSize];
            data.get(topRow);
            data.position(bottomOffset);
            data.put(rowBuffer);

            // Copy buffer (original top row) to bottom row
            data.position(topOffset);
            data.put(topRow);
        }

        data.rewind();
    }

    public ByteBuffer getData() {
        return data;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getNumChannels() {
        return numChannels;
    }

    public static int getColor(int red, int green, int blue) {
        return getColor(red, green, blue, 255);
    }

    public static int getColor(int red, int green, int blue, int alpha) {
        int color = (red << 24) | (green << 16) | (blue << 8) | alpha;
        return color;
    }

    public static void saveImage(BufferedImage image, String path) {
        STBImageWrite.stbi_write_png(path, image.getWidth(), image.getHeight(), image.getNumChannels(),
                image.getData(), image.getWidth() * image.getNumChannels() * Byte.BYTES);
    }

}