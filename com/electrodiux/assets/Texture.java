package com.electrodiux.assets;

import java.awt.image.BufferedImage;

public class Texture {

    private BufferedImage data;

    Texture(BufferedImage data) {
        this.data = data;
    }

    public BufferedImage getData() {
        return data;
    }

}
