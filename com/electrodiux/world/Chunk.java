package com.electrodiux.world;

import java.io.Serializable;

import com.electrodiux.block.Blocks;

public class Chunk implements Serializable {

    public static final int CHUNK_SIZE = 16; // 16 es potencia natural de 2
    public static final int CHUNK_SIZE_BITMASK = 15; // 16 - 1
    public static final int CHUNK_SIZE_BYTESHIFT = 4; // log2 16 = 4
    public static final int CHUNK_AREA_BYTESHIFT = 8; // log2 256 = 4; 256 = 16^2
    public static final int CHUNK_AREA = CHUNK_SIZE * CHUNK_SIZE;
    public static final int CHUNK_HEIGHT = 256; // es potencia natural de 2

    private short[] blocks;

    private final int chunkX, chunkZ;

    public Chunk(int x, int z) {
        this.chunkX = x;
        this.chunkZ = z;
        blocks = new short[CHUNK_AREA * CHUNK_HEIGHT];
    }

    public short[] getBlocks() {
        return blocks;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public int getBlockX() {
        return chunkX << CHUNK_SIZE_BYTESHIFT;
    }

    public int getBlockZ() {
        return chunkZ << CHUNK_SIZE_BYTESHIFT;
    }

    public short getBlock(int x, int y, int z) {
        if (outOfBounds(x, y, z))
            return Blocks.AIR;
        return blocks[getBlockIndex(x, y, z)];
    }

    public int getHightestYAt(int x, int z) {
        for (int y = CHUNK_HEIGHT - 1; y >= 0; y--) {
            if (blocks[Chunk.getBlockIndex(x, y, z)] != Blocks.AIR) {
                return y;
            }
        }
        return 0;
    }

    public static int getBlockIndex(int x, int y, int z) {
        return x + (z << CHUNK_SIZE_BYTESHIFT) + (y << CHUNK_AREA_BYTESHIFT);
    }

    public static boolean outOfBounds(int x, int y, int z) {
        return y < 0 || y >= CHUNK_HEIGHT || x < 0 || x >= CHUNK_SIZE || z < 0 || z >= CHUNK_SIZE;
    }

    /*
     * Para obtener el módulo de un valor usando una máscara de bits en Java, puedes
     * usar el operador de "and" binario (&) junto con la máscara de bits deseada.
     * 
     * Por ejemplo, si quieres calcular el módulo de un número entero n con respecto
     * a 8 (es decir, obtener el resto de la división de n entre 8), puedes usar la
     * siguiente expresión:
     * 
     * int mod8 = n & 7;
     * 
     * Esto funciona porque 8 es el número binario 1000, y 7 es el número binario
     * 0111. Cuando se aplica el operador "and" a estos dos números, se obtiene el
     * número binario 0000, que es el mismo que 0 en base decimal.
     * 
     * Para obtener el módulo de un número entero con respecto a cualquier otro
     * valor, basta con ajustar la máscara de bits en consecuencia. Por ejemplo,
     * para obtener el módulo de un número entero n con respecto a 16, puedes usar
     * la siguiente expresión:
     * 
     * int mod16 = n & 15;
     * 
     * En general, si quieres obtener el módulo de un número entero n con respecto a
     * un valor "modulo", puedes usar la siguiente expresión:
     * int modulo = n & (modulo - 1);
     */

    private static int calculateIndexInChunk(int coordinate) {
        // x = -16 => x = -15 because negative chunks starts at
        // -1 and positive chunks starts at 0

        if (coordinate < 0) {
            return CHUNK_SIZE - 1 - (-(coordinate + 1) & CHUNK_SIZE_BITMASK);
        }

        return coordinate & CHUNK_SIZE_BITMASK;
    }

    public static int getBlockIndexWithWorldCoords(int x, int y, int z) {
        return getBlockIndex(calculateIndexInChunk(x), y, calculateIndexInChunk(z));
    }
}