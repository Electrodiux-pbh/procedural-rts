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
        BufferedImage image = createBufferedAtlas();
        BufferedImage.saveImage(image, "atlas.png");

        Texture texture = Loader.loadTexture(image, filter, usesMipMaps, anisotropicExt);

        return texture;
    }

    public List<Sprite> getTextureSprites(Texture texture) {
        List<Sprite> sprites = new ArrayList<>();
        int currentX = 0;
        int currentY = texture.getHeight() - textureHeight;

        final float offset = 0.1f;

        for (int i = 0; i < textures.size(); i++) {
            float topY = (currentY + textureHeight - offset) / (float) texture.getHeight();
            float rightX = (currentX + textureWidth - offset) / (float) texture.getWidth();
            float leftX = (currentX + offset) / (float) texture.getWidth();
            float bottomY = (currentY + offset) / (float) texture.getHeight();

            float[] texCoords = {
                    rightX, bottomY,
                    leftX, bottomY,
                    leftX, topY,
                    rightX, topY
            };

            Sprite sprite = new Sprite(texCoords);
            sprites.add(sprite);

            currentX += textureWidth;
            if (currentX >= texture.getWidth()) {
                currentX = 0;
                currentY -= textureHeight;
            }
        }

        return sprites;
    }

    public BufferedImage createBufferedAtlas() {
        int atlasRows = (int) Math.ceil(Math.sqrt(textures.size()));
        int atlasCols = (int) Math.ceil(textures.size() / (float) atlasRows);

        BufferedImage atlas = new BufferedImage(atlasRows * textureWidth, atlasCols * textureHeight, 4);

        for (AtlasTexture texture : textures.values()) {
            BufferedImage image = texture.image;

            int x = texture.index % atlasRows;
            int y = texture.index / atlasRows;

            atlas.drawImage(x * textureWidth, y * textureHeight, textureWidth, textureHeight, image);

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
