package com.electrodiux.graphics.textures;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;

import com.electrodiux.graphics.Loader;

public class TextureAtlas {

    private Map<String, AtlasTexture> textures;
    private int textureWidth, textureHeight;

    public TextureAtlas(int textureWidth, int textureHeight) {
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;

        this.textures = new HashMap<>();
    }

    public Texture createAtlasTexture(int filter, boolean usesMipMaps, float anisotropicExt) {
        int decreicingFactor = 2;
        int steps = textureWidth / decreicingFactor;

        BufferedImage[] images = new BufferedImage[steps];
        for (int i = 0; i < steps; i++) {
            images[i] = createBufferedAtlas(textureWidth - decreicingFactor * i, textureHeight - decreicingFactor * i);
        }

        Texture texture = Loader.loadTexture(images, filter, GL30.GL_CLAMP_TO_EDGE);

        return texture;
    }

    public List<Sprite> getTextureSprites() {
        final int atlasCols = (int) Math.ceil(Math.sqrt(textures.size()));
        final int atlasRows = (int) Math.ceil(textures.size() / (float) atlasCols);

        final int width = atlasCols * textureWidth;
        final int height = atlasRows * textureHeight;

        List<Sprite> sprites = new ArrayList<>();
        int currentX = 0;
        int currentY = height - textureHeight;

        final float offset = 0.1f;

        for (int i = 0; i < textures.size(); i++) {
            float topY = (currentY + textureHeight - offset) / (float) height;
            float rightX = (currentX + textureWidth - offset) / (float) width;
            float leftX = (currentX + offset) / (float) width;
            float bottomY = (currentY + offset) / (float) height;

            float[] texCoords = {
                    rightX, bottomY,
                    leftX, bottomY,
                    leftX, topY,
                    rightX, topY
            };

            Sprite sprite = new Sprite(texCoords);
            sprites.add(sprite);

            currentX += textureWidth;
            if (currentX >= width) {
                currentX = 0;
                currentY -= textureHeight;
            }
        }

        return sprites;
    }

    public BufferedImage createBufferedAtlas(int spriteWidth, int spriteHeight) {
        final int atlasCols = (int) Math.ceil(Math.sqrt(textures.size()));
        final int atlasRows = (int) Math.ceil(textures.size() / (float) atlasCols);

        BufferedImage atlas = new BufferedImage(atlasCols * spriteWidth, atlasRows * spriteHeight, 4);

        for (AtlasTexture texture : textures.values()) {
            BufferedImage image = texture.image;

            int x = texture.index % atlasCols;
            int y = texture.index / atlasCols;

            atlas.drawImage(x * spriteWidth, y * spriteHeight, spriteWidth, spriteHeight, image);

            STBImage.stbi_image_free(image.getData());
        }

        atlas.flipVertically();

        return atlas;
    }

    public int loadTexture(String path) throws IOException {
        if (textures.containsKey(path)) {
            return textures.get(path).index;
        }

        InputStream in = TextureAtlas.class.getResourceAsStream(path);
        if (in == null)
            throw new IOException("There is no texture at " + path);

        BufferedImage image = loadTexture(in);
        AtlasTexture texture = new AtlasTexture(textures.size(), image);
        textures.put(path, texture);

        return texture.index;
    }

    private BufferedImage loadTexture(InputStream in) throws IOException {
        byte[] imageBytes = in.readAllBytes();

        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);

        ByteBuffer imageBuffer = BufferUtils.createByteBuffer(imageBytes.length);
        imageBuffer.put(imageBytes).flip();

        ByteBuffer data = STBImage.stbi_load_from_memory(imageBuffer, width, height, channels, 0);
        BufferedImage img = new BufferedImage(width.get(), height.get(), channels.get(), data);

        return img;
    }

    public int size() {
        return textures.size();
    }

    private static class AtlasTexture {

        private int index;

        private BufferedImage image;

        public AtlasTexture(int index, BufferedImage image) {
            this.index = index;
            this.image = image;
        }

    }

}
