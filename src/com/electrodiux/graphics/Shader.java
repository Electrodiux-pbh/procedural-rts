package com.electrodiux.graphics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;

import org.joml.Matrix2f;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import com.electrodiux.math.Vector2;
import com.electrodiux.math.Vector3;

public class Shader {

    private String filePath;

    private int vertexID, fragmentID, shaderProgram;

    private boolean beingUsed = false;

    public static Shader loadShader(String filePath) throws IOException {
        if (!filePath.startsWith("/"))
            filePath = "/" + filePath;

        InputStream in = Shader.class.getResourceAsStream(filePath);

        Shader shader = new Shader(filePath);
        shader.load(in);

        return shader;
    }

    public static Shader loadShader(InputStream in) throws IOException {
        Shader shader = new Shader("stream-path/" + Integer.toHexString(in.hashCode()));
        shader.load(in);

        return shader;
    }

    private Shader(String filePath) {
        this.filePath = filePath;
    }

    private void load(InputStream stream) throws IOException {
        try {
            String src = getSouceOfStream(stream);
            String[] shaderSrc = src.split("(#type)( )+([a-zA-z]+)");

            int index = 0;
            int eol = 0;

            String vertexSrc = null, fragmentSrc = null;

            for (int i = 1; i < shaderSrc.length; i++) {
                index = src.indexOf("#type", eol) + 6;
                eol = src.indexOf("\n", index);
                String type = src.substring(index, eol).trim();

                switch (type) {
                    case "vertex":
                        vertexSrc = shaderSrc[i];
                        break;
                    case "fragment":
                        fragmentSrc = shaderSrc[i];
                        break;
                    default:
                        throw new IOException("Unexpected token '" + type + "'");
                }
            }

            if (vertexSrc == null || fragmentSrc == null) {
                throw new IOException("Fragment or Vertex is null");
            }

            compile(vertexSrc, fragmentSrc);

            mat2Buff = BufferUtils.createFloatBuffer(4); // 2 * 2 = 4
            mat3Buff = BufferUtils.createFloatBuffer(9); // 3 * 3 = 9
            mat4Buff = BufferUtils.createFloatBuffer(16); // 4 * 4 = 16

        } catch (IOException e) {
            throw new IOException("Could not open file for shader: '" + this.filePath + "'", e);
        }
    }

    private String getSouceOfStream(InputStream stream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(stream));

        String line = "";
        String data = "";

        while ((line = in.readLine()) != null) {
            data += line + "\n";
        }
        in.close();

        return data;
    }

    private void compile(String vertexSrc, String fragmentSrc) throws IllegalStateException {
        vertexID = compileShader(GL20.GL_VERTEX_SHADER, vertexSrc,
                "Vertex shader at: '" + filePath + "' compilation failed.");
        fragmentID = compileShader(GL20.GL_FRAGMENT_SHADER, fragmentSrc,
                "Fragment shader at: '" + filePath + "' compilation failed.");

        shaderProgram = GL20.glCreateProgram();
        GL20.glAttachShader(shaderProgram, vertexID);
        GL20.glAttachShader(shaderProgram, fragmentID);
        GL20.glLinkProgram(shaderProgram);

        int success = GL20.glGetProgrami(shaderProgram, GL20.GL_LINK_STATUS);
        if (success == GL20.GL_FALSE) {
            int len = GL20.glGetProgrami(shaderProgram, GL20.GL_INFO_LOG_LENGTH);
            throw new IllegalStateException(
                    "Linking of shaders failed: " + GL20.glGetProgramInfoLog(shaderProgram, len));
        }
    }

    private int compileShader(int shaderType, String src, String errorMessage) throws IllegalStateException {
        int success;
        int id;

        id = GL20.glCreateShader(shaderType);
        GL20.glShaderSource(id, src);
        GL20.glCompileShader(id);

        success = GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS);
        if (success == GL20.GL_FALSE) {
            int len = GL20.glGetShaderi(id, GL20.GL_INFO_LOG_LENGTH);
            throw new IllegalStateException(errorMessage + "\n" + GL20.glGetShaderInfoLog(id, len));
        }

        return id;
    }

    // #region Uniform setters

    private FloatBuffer mat2Buff, mat3Buff, mat4Buff;

    public void setMatrix4f(String varName, Matrix4f matrix) {
        matrix.get(mat4Buff);
        GL20.glUniformMatrix4fv(startSetVariable(varName), false, mat4Buff);
    }

    public void setMatrix3f(String varName, Matrix3f matrix) {
        matrix.get(mat3Buff);
        GL20.glUniformMatrix3fv(startSetVariable(varName), false, mat3Buff);
    }

    public void setMatrix2f(String varName, Matrix2f matrix) {
        matrix.get(mat2Buff);
        GL20.glUniformMatrix2fv(startSetVariable(varName), false, mat2Buff);
    }

    public void setVector4f(String varName, Vector4f vec) {
        GL20.glUniform4f(startSetVariable(varName), vec.x, vec.y, vec.z, vec.w);
    }

    public void setColor(String varName, Color value) {
        GL20.glUniform4f(startSetVariable(varName), value.r(), value.g(), value.b(), value.a());
    }

    public void setVector3f(String varName, Vector3f vec) {
        GL20.glUniform3f(startSetVariable(varName), vec.x, vec.y, vec.z);
    }

    public void setVector3(String varName, Vector3 vec) {
        GL20.glUniform3f(startSetVariable(varName), vec.x(), vec.y(), vec.z());
    }

    public void setVector2f(String varName, Vector2f vec) {
        GL20.glUniform2f(startSetVariable(varName), vec.x, vec.y);
    }

    public void setVector2(String varName, Vector2 vec) {
        GL20.glUniform2f(startSetVariable(varName), vec.x(), vec.y());
    }

    public void setMatrix4fArray(String varName, Matrix4f[] matrices) {
        int[] locations = getUniformsLocations(varName, matrices.length);
        for (int i = 0; i < matrices.length; i++) {
            matrices[i].get(mat4Buff);
            GL20.glUniformMatrix4fv(locations[i], false, mat4Buff);
        }
    }

    public void setMatrix3fArray(String varName, Matrix3f[] matrices) {
        int[] locations = getUniformsLocations(varName, matrices.length);
        for (int i = 0; i < matrices.length; i++) {
            matrices[i].get(mat3Buff);
            GL20.glUniformMatrix3fv(locations[i], false, mat3Buff);
        }
    }

    public void setMatrix2fArray(String varName, Matrix2f[] matrices) {
        int[] locations = getUniformsLocations(varName, matrices.length);
        for (int i = 0; i < matrices.length; i++) {
            matrices[i].get(mat2Buff);
            GL20.glUniformMatrix2fv(locations[i], false, mat2Buff);
        }
    }

    public void setVector4fArray(String varName, Vector4f[] vectors) {
        int[] locations = getUniformsLocations(varName, vectors.length);
        for (int i = 0; i < vectors.length; i++) {
            GL20.glUniform4f(locations[i], vectors[i].x, vectors[i].y, vectors[i].z, vectors[i].w);
        }
    }

    public void setColorArray(String varName, Color[] values) {
        int[] locations = getUniformsLocations(varName, values.length);
        for (int i = 0; i < values.length; i++) {
            GL20.glUniform4f(locations[i], values[i].r(), values[i].g(), values[i].b(), values[i].a());
        }
    }

    public void setVector3fArray(String varName, Vector3f[] vectors) {
        int[] locations = getUniformsLocations(varName, vectors.length);
        for (int i = 0; i < vectors.length; i++) {
            GL20.glUniform3f(locations[i], vectors[i].x, vectors[i].y, vectors[i].z);
        }
    }

    public void setVector3Array(String varName, Vector3[] vectors) {
        int[] locations = getUniformsLocations(varName, vectors.length);
        for (int i = 0; i < vectors.length; i++) {
            GL20.glUniform3f(locations[i], vectors[i].x(), vectors[i].y(), vectors[i].z());
        }
    }

    public void setVector2fArray(String varName, Vector2f[] vectors) {
        int[] locations = getUniformsLocations(varName, vectors.length);
        for (int i = 0; i < vectors.length; i++) {
            GL20.glUniform2f(locations[i], vectors[i].x, vectors[i].y);
        }
    }

    public void setVector2Array(String varName, Vector2[] vectors) {
        int[] locations = getUniformsLocations(varName, vectors.length);
        for (int i = 0; i < vectors.length; i++) {
            GL20.glUniform2f(locations[i], vectors[i].x(), vectors[i].y());
        }
    }

    public void setFloat(String varName, float value) {
        GL20.glUniform1f(startSetVariable(varName), value);
    }

    public void setInt(String varName, int value) {
        GL20.glUniform1i(startSetVariable(varName), value);
    }

    public void setBoolean(String varName, boolean value) {
        GL20.glUniform1i(startSetVariable(varName), value ? 1 : 0);
    }

    public void setIntArray(String varName, int[] values) {
        GL20.glUniform1iv(startSetVariable(varName), values);
    }

    private int startSetVariable(String varName) {
        use();
        return GL20.glGetUniformLocation(shaderProgram, varName);
    }

    public int getUniformLocation(String varName) {
        return GL20.glGetUniformLocation(shaderProgram, varName);
    }

    public int[] getUniformsLocations(String varName, int length) {
        int[] locations = new int[length];
        for (int i = 0; i < locations.length; i++) {
            locations[i] = GL20.glGetUniformLocation(shaderProgram, varName + "[" + i + "]");
        }
        return locations;
    }

    // #endregion

    public void use() {
        if (!beingUsed) {
            GL20.glUseProgram(shaderProgram);
            beingUsed = true;
        }
    }

    public void detach() {
        GL20.glUseProgram(0);
        beingUsed = false;
    }

    public void destroy() {
        detach();
        GL20.glDetachShader(shaderProgram, vertexID);
        GL20.glDeleteShader(vertexID);
        GL20.glDetachShader(shaderProgram, fragmentID);
        GL20.glDeleteShader(fragmentID);
        GL20.glDeleteProgram(shaderProgram);
    }

    public int getShaderProgramID() {
        return shaderProgram;
    }

    public int getVertexID() {
        return vertexID;
    }

    public int getFragmentID() {
        return fragmentID;
    }

}
