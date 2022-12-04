package com.electrodiux.assets;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class Texture {

    private BufferedImage data;

    public Texture(BufferedImage data) {
        this.data = data;
    }

    public BufferedImage getData() {
        return data;
    }

    public static BufferedImage redimension(Texture texture, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(texture.data.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
        g2d.dispose();

        return resizedImage;
    }

}
