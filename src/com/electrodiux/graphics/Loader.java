package com.electrodiux.graphics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;

import com.electrodiux.graphics.textures.BufferedImage;
import com.electrodiux.graphics.textures.Texture;

public class Loader {

    private static List<Integer> attributeLists = new ArrayList<Integer>();
    private static List<Integer> buffers = new ArrayList<Integer>();
    private static List<Integer> textures = new ArrayList<Integer>();

    // #region RawModel

    public static Model loadRawModel(float[] vertices, int[] indices) {
        return loadRawModel(vertices, indices, null);
    }

    public static Model loadRawModel(float[] vertices, int[] indices, float[] textureCoords) {
        int vaoId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoId);

        if (textureCoords == null) {
            textureCoords = new float[indices.length * 2];
        }

        loadIndices(indices);
        storeDataInAttributeList(0, vertices, 3);
        storeDataInAttributeList(1, textureCoords, 2);

        GL30.glBindVertexArray(0);

        attributeLists.add(vaoId);

        return new Model(vaoId, indices.length);
    }

    public static Model loadRawModel(float[] vertices, int[] indices, float[] normals, float[] textureCoords) {
        int vaoId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoId);

        if (textureCoords == null) {
            textureCoords = new float[indices.length * 2];
        }

        loadIndices(indices);
        storeDataInAttributeList(0, vertices, 3);
        storeDataInAttributeList(1, textureCoords, 2);
        storeDataInAttributeList(2, normals, 3);

        GL30.glBindVertexArray(0);

        attributeLists.add(vaoId);

        return new Model(vaoId, indices.length);
    }

    private static void storeDataInAttributeList(int attributeIndex, float[] data, int dataLength) {
        int vboId = GL15.glGenBuffers();
        buffers.add(vboId);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);

        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(data.length);
        vertexBuffer.put(data).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);

        GL20.glVertexAttribPointer(attributeIndex, dataLength, GL11.GL_FLOAT, false, 0, 0);
        GL20.glEnableVertexAttribArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    private static void loadIndices(int[] indices) {
        int eboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboId);
        IntBuffer indexBuffer = BufferUtils.createIntBuffer(indices.length);
        indexBuffer.put(indices).flip();
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW);

        buffers.add(eboId);
    }

    // #endregion

    // #region OBJ objects

    public static Model loadObjModel(File file) throws IOException {
        return loadObjModel(new FileInputStream(file));
    }

    public static Model loadObjModel(String path) throws IOException {
        return loadObjModel(getStreamFromPath(path));
    }

    public static Model loadObjModel(InputStream inputStream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

        try {
            List<Vector3f> vertices = new ArrayList<>();
            List<Vector2f> textures = new ArrayList<>();
            List<Vector3f> normals = new ArrayList<>();
            List<Integer> indices = new ArrayList<>();

            float[] vertexBuffer = null;
            float[] normalBuffer = null;
            float[] textureBuffer = null;
            int[] indicesBuffer = null;

            String line = null;
            while ((line = in.readLine()) != null) {
                String[] data = line.split(" ");

                if (line.startsWith("v ")) {
                    Vector3f vertex = new Vector3f(Float.valueOf(data[1]), Float.valueOf(data[2]),
                            Float.valueOf(data[3]));
                    vertices.add(vertex);
                } else if (line.startsWith("vt ")) {
                    Vector2f textureCoord = new Vector2f(Float.valueOf(data[1]), Float.valueOf(data[2]));
                    textures.add(textureCoord);
                } else if (line.startsWith("vn ")) {
                    Vector3f normal = new Vector3f(Float.valueOf(data[1]), Float.valueOf(data[2]),
                            Float.valueOf(data[3]));
                    normals.add(normal);
                } else if (line.startsWith("f ")) {
                    textureBuffer = new float[vertices.size() * 2];
                    normalBuffer = new float[vertices.size() * 3];
                    break;
                }
            }

            while (line != null) {
                if (!line.startsWith("f ")) {
                    line = in.readLine();
                    continue;
                }

                String[] data = line.split(" ");

                String[] vertex1 = data[1].split("/");
                String[] vertex2 = data[2].split("/");
                String[] vertex3 = data[3].split("/");

                processVertex(vertex1, indices, textures, normals, textureBuffer, normalBuffer);
                processVertex(vertex2, indices, textures, normals, textureBuffer, normalBuffer);
                processVertex(vertex3, indices, textures, normals, textureBuffer, normalBuffer);

                line = in.readLine();
            }

            vertexBuffer = new float[vertices.size() * 3];
            indicesBuffer = new int[indices.size()];

            int vertexPointer = 0;
            for (Vector3f vertex : vertices) {
                vertexBuffer[vertexPointer++] = vertex.x;
                vertexBuffer[vertexPointer++] = vertex.y;
                vertexBuffer[vertexPointer++] = vertex.z;
            }

            for (int i = 0; i < indices.size(); i++) {
                indicesBuffer[i] = indices.get(i);
            }

            return Loader.loadRawModel(vertexBuffer, indicesBuffer, normalBuffer, textureBuffer);
        } catch (IOException e) {
            throw new IOException("An error occurred while loading the model", e);
        } catch (Exception e) {
            System.err.println("An error occurred while loading the model");
            e.printStackTrace();
            return null;
        } finally {
            in.close();
        }
    }

    private static void processVertex(String[] data, List<Integer> indices, List<Vector2f> textures,
            List<Vector3f> normals, float[] textureBuffer, float[] normalsBuffer) {

        int vertexPointer = Integer.parseInt(data[0]) - 1;
        indices.add(vertexPointer);

        Vector2f texture = textures.get(Integer.parseInt(data[1]) - 1);
        textureBuffer[vertexPointer * 2] = texture.x;
        textureBuffer[vertexPointer * 2 + 1] = 1 - texture.y;

        Vector3f normal = normals.get(Integer.parseInt(data[2]) - 1);
        normalsBuffer[vertexPointer * 3] = normal.x;
        normalsBuffer[vertexPointer * 3 + 1] = normal.y;
        normalsBuffer[vertexPointer * 3 + 2] = normal.z;
    }

    // #endregion

    // #region Textures

    public static final float DEFAULT_ANISOTROPIC_EXT = 4.0f;

    public static Texture loadTexture(ByteBuffer image, int imgWidth, int imgHeight, int channels, int filter,
            boolean usesMipmap, float anisotropicExt) {
        int textureId = GL11.glGenTextures();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);

        int type = getTextureType(channels);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, type, imgWidth, imgHeight, 0, type, GL11.GL_UNSIGNED_BYTE,
                image);

        if (usesMipmap) {
            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

            int mipmapFilter = filter == GL11.GL_NEAREST ? GL11.GL_NEAREST_MIPMAP_NEAREST
                    : GL11.GL_LINEAR_MIPMAP_LINEAR;

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, mipmapFilter);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, mipmapFilter);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, 0f);

            if (GL.getCapabilities().GL_EXT_texture_filter_anisotropic) {
                anisotropicExt = Math.min(anisotropicExt,
                        GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT));

                GL11.glTexParameterf(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT,
                        anisotropicExt);
            }
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        textures.add(textureId);

        return new Texture(textureId);
    }

    public static Texture loadTexture(BufferedImage[] images, int filter, int wrap) {
        int textureId = GL11.glGenTextures();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, wrap);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, wrap);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);

        for (int i = 0; i < images.length; i++) {
            BufferedImage image = images[i];

            ByteBuffer imgBuff = image.getData();

            int type = getTextureType(image.getChannels());
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, i, type, image.getWidth(), image.getHeight(), 0, type,
                    GL11.GL_UNSIGNED_BYTE, imgBuff);
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        textures.add(textureId);

        return new Texture(textureId);
    }

    private static int getTextureType(int channels) {
        int type = channels == 3 ? GL11.GL_RGB : channels == 4 ? GL11.GL_RGBA : -1;
        if (type == -1)
            throw new IllegalArgumentException("Unknown number of channels '" + channels + "'");
        return type;
    }

    public static Texture loadTexture(byte[] imageBytes, int filter, boolean usesMipmap, float anisotropicExt)
            throws IOException {
        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);

        ByteBuffer imageBuffer = BufferUtils.createByteBuffer(imageBytes.length);
        imageBuffer.put(imageBytes).flip();

        STBImage.stbi_set_flip_vertically_on_load(true);
        ByteBuffer image = STBImage.stbi_load_from_memory(imageBuffer, width, height, channels, 0);

        Texture texture = null;
        if (image != null) {
            try {
                texture = loadTexture(image, width.get(0), height.get(0), channels.get(0), filter, usesMipmap,
                        anisotropicExt);
            } catch (Exception e) {
                throw new IOException("An error occurred while loading texture to OpenGL", e);
            }
        } else {
            throw new IOException("Could not load the texture image");
        }

        STBImage.stbi_image_free(image);

        return texture;
    }

    public static Texture loadTexture(BufferedImage image, int filter, boolean usesMipmap, float anisotropicExt) {
        return loadTexture(image.getData(), image.getWidth(), image.getHeight(), image.getChannels(), filter,
                usesMipmap, anisotropicExt);
    }

    public static Texture loadTexture(InputStream in, int filter, boolean usesMipmap, float anisotropicExt)
            throws IOException {
        Objects.requireNonNull(in, "The input stream is null");
        byte[] imageBytes = in.readAllBytes();
        return loadTexture(imageBytes, filter, usesMipmap, anisotropicExt);
    }

    public static Texture loadTexture(InputStream in, int filter, boolean usesMipmap) throws IOException {
        Objects.requireNonNull(in, "The input stream is null");
        byte[] imageBytes = in.readAllBytes();
        return loadTexture(imageBytes, filter, usesMipmap, DEFAULT_ANISOTROPIC_EXT);
    }

    public static Texture loadTexture(InputStream in, boolean usesMipmap) throws IOException {
        return loadTexture(in, GL11.GL_LINEAR, usesMipmap);
    }

    public static Texture loadTexture(InputStream in) throws IOException {
        return loadTexture(in, GL11.GL_LINEAR, true);
    }

    public static Texture loadTexture(String path, int filter, boolean usesMipmap) throws IOException {
        return loadTexture(getStreamFromPath(path), filter, usesMipmap);
    }

    public static Texture loadTexture(String path, int filter) throws IOException {
        return loadTexture(getStreamFromPath(path), filter, true);
    }

    public static Texture loadTexture(String path) throws IOException {
        return loadTexture(getStreamFromPath(path), GL11.GL_LINEAR, true);
    }

    private static InputStream getStreamFromPath(String path) throws IOException {
        InputStream in = Loader.class.getResourceAsStream(path);
        if (in == null)
            throw new IOException("No resource at \"" + path + "\"");
        return in;
    }

    public static ByteBuffer loadImage(byte[] imageBytes, IntBuffer width, IntBuffer height, IntBuffer channels)
            throws IOException {
        STBImage.nstbi_set_flip_vertically_on_load(GLFW.GLFW_FALSE);

        ByteBuffer imageBuffer = BufferUtils.createByteBuffer(imageBytes.length);
        imageBuffer.put(imageBytes).flip();

        ByteBuffer image = STBImage.stbi_load_from_memory(imageBuffer, width, height, channels, 0);

        if (image != null) {
            return image;
        } else {
            throw new IOException("Could not load the texture image");
        }
    }

    public static GLFWImage loadImageToGLFW(String path) throws IOException {
        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        IntBuffer c = BufferUtils.createIntBuffer(1);

        InputStream in = getStreamFromPath(path);
        byte[] imageBytes = in.readAllBytes();

        ByteBuffer buff = loadImage(imageBytes, w, h, c);
        GLFWImage img = new GLFWImage(buff);
        img.pixels(buff);
        img.width(w.get());
        img.height(h.get());
        STBImage.stbi_image_free(buff);

        return img;
    }

    // #endregion

    public static void clear() {
        for (int atrList : attributeLists) {
            GL30.glDeleteVertexArrays(atrList);
        }
        for (int buff : buffers) {
            GL15.glDeleteBuffers(buff);
        }
        for (int texture : textures) {
            GL11.glDeleteTextures(texture);
        }
    }

}
