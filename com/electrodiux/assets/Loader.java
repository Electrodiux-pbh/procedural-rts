package com.electrodiux.assets;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

public class Loader {

    public static Texture loadTexture(String path) {
        BufferedImage buffer;
        try {
            Image img = ImageIO.read(Loader.class.getResourceAsStream("/src/assets/textures/" + path));

            buffer = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics g = buffer.getGraphics();
            g.drawImage(img, 0, 0, null);
            g.dispose();
        } catch (Exception e) {
            e.printStackTrace();
            buffer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }

        Texture texture = new Texture(buffer);
        return texture;
    }
}
