package com.electrodiux.graphics.textures;

import java.util.ArrayList;
import java.util.List;

public class SpriteAtlas {

    private Texture texture;
    private List<Sprite> sprites;

    public SpriteAtlas(Texture texture, int spriteWidth, int spriteHeight, int numSprites, int spacing) {
        this.sprites = new ArrayList<Sprite>();
        this.texture = texture;

        int currentX = 0;
        int currentY = texture.getHeight() - spriteHeight;

        float horizontalFix = (float) spriteWidth / (texture.getWidth() * 100);

        for (int i = 0; i < numSprites; i++) {
            float topY = (currentY + spriteHeight) / (float) texture.getHeight() - horizontalFix;
            float rightX = (currentX + spriteWidth) / (float) texture.getWidth() - horizontalFix;
            float leftX = currentX / (float) texture.getWidth() + horizontalFix;
            float bottomY = currentY / (float) texture.getHeight() + horizontalFix;

            float[] texCoords = {
                    rightX, bottomY,
                    leftX, bottomY,
                    leftX, topY,
                    rightX, topY
            };
            Sprite sprite = new Sprite(this.texture, texCoords);
            this.sprites.add(sprite);

            currentX += spriteWidth + spacing;
            if (currentX >= texture.getWidth()) {
                currentX = 0;
                currentY -= spriteHeight + spacing;
            }
        }
    }

    public Sprite getSprite(int index) {
        return this.sprites.get(index);
    }

}
