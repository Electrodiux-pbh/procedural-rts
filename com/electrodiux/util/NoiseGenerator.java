package com.electrodiux.util;

import java.util.Random;

public class NoiseGenerator {

    private int perm[];

    private float maxClapm;
    private float minClapm;

    private int chunkSize;

    private int octaves;
    private float lacunarityFactor;
    private float persistanceFactor;

    public NoiseGenerator(long seed) {
        this(generatePermutation(seed), 1, 1, 2, 0.5f);
    }

    /**
     * 
     * @param seed              the seed of the noise
     * @param chunkSize         the size of the chunk
     * @param octaves           the number of octaves to generate the final noise
     * @param lacunarityFactor  how much the noise grid reduces per octave
     *                          generation
     * @param persistanceFactor the persistance of the noise per octave in the final
     *                          result
     */
    public NoiseGenerator(long seed, int chunkSize, int octaves, float lacunarityFactor, float persistanceFactor) {
        this(generatePermutation(seed), chunkSize, octaves, lacunarityFactor, persistanceFactor);
    }

    private NoiseGenerator(int[] perm, int chunkSize, int octaves, float lacunarityFactor, float persistanceFactor) {
        setChunkSize(chunkSize);
        setDefaultOctaves(octaves);
        setDefaultLacunarityFactor(lacunarityFactor);
        this.persistanceFactor = persistanceFactor;
        this.perm = perm;
        setClampValues(-1, 1);
    }

    private static int[] generatePermutation(long seed) {
        Random rand = new Random(seed);

        int[] perm = new int[512];
        for (int i = 0; i < 256; i++) {
            perm[256 + i] = perm[i] = rand.nextInt(256);
        }

        return perm;
    }

    public float[] getNoise2D(int width, int height, int posX, int posY) {
        return getNoise2D(width, height, posX, posY, 1, octaves, lacunarityFactor, persistanceFactor);
    }

    public float[] getNoise2D(int width, int height, int posX, int posY, int range) {
        return getNoise2D(width, height, posX, posY, range, octaves, lacunarityFactor, persistanceFactor);
    }

    /**
     * 
     * @param width                the width of the noise area
     * @param height               the height of the noise area
     * @param posX                 the posX of the noise area start
     * @param posY                 the posY of the noise area start
     * @param range                the range of the result [-range, range]
     * @param octaves              the number of octaves to generate the final noise
     * @param lacunarityFactor     how much the noise grid reduces per octave
     *                             generation
     * @param persistanceFactorthe persistance of the noise per octave in the final
     *                             result
     * @return the grid of noise
     */
    public float[] getNoise2D(int width, int height, int posX, int posY, int range, int octaves, float lacunarityFactor,
            float persistanceFactor) {

        if (lacunarityFactor <= 0)
            lacunarityFactor = this.lacunarityFactor;
        if (persistanceFactor <= 0)
            persistanceFactor = this.persistanceFactor;
        if (octaves <= 0)
            octaves = this.octaves;

        float[] noise2D = new float[width * height];

        float persistance = 1f;
        float lacunarity = 1f;

        for (int oct = 0; oct < octaves; oct++) {
            for (int i = 0; i < width * height; i++) {
                int x = i % width;
                int y = i / width;
                double noise = noise2D[x + y * width] + noise(x + posX, y + posY, lacunarity) * persistance;
                if (oct + 1 == octaves) {
                    noise2D[i] = (float) clamp(noise * range, minClapm, maxClapm);
                    continue;
                }
                noise2D[x + y * width] = (float) noise;
            }
            persistance *= persistanceFactor;
            lacunarity *= lacunarityFactor;
        }

        return noise2D;
    }

    private double noise(double x, double y, float lacunarity) {
        x /= chunkSize / lacunarity;
        y /= chunkSize / lacunarity;

        int xi = (int) Math.floor(x) & 255;
        int yi = (int) Math.floor(y) & 255;

        int g1 = perm[perm[xi] + yi];
        int g2 = perm[perm[xi + 1] + yi];
        int g3 = perm[perm[xi] + yi + 1];
        int g4 = perm[perm[xi + 1] + yi + 1];

        double xf = x - Math.floor(x);
        double yf = y - Math.floor(y);

        double d1 = grad(g1, xf, yf);
        double d2 = grad(g2, xf - 1, yf);
        double d3 = grad(g3, xf, yf - 1);
        double d4 = grad(g4, xf - 1, yf - 1);

        double u = fade(xf);
        double v = fade(yf);

        double x1Inter = lerp(u, d1, d2);
        double x2Inter = lerp(u, d3, d4);
        double yInter = lerp(v, x1Inter, x2Inter);

        return yInter;
    }

    private double grad(int hash, double x, double y) {
        switch (hash & 3) {
            case 0:
                return x + y;
            case 1:
                return -x + y;
            case 2:
                return x - y;
            case 3:
                return -x - y;
            default:
                return 0;
        }
    }

    private double lerp(double amount, double left, double right) {
        return ((1 - amount) * left + amount * right);
    }

    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private double clamp(double x, double min, double max) {
        return x < min ? min : x > max ? max : x;
    }

    public void setClampValues(float min, float max) {
        minClapm = min;
        maxClapm = max;
    }

    public void setDefaultLacunarityFactor(float factor) {
        if (factor <= 0) {
            throw new IllegalArgumentException("The lacunarity factor must be bigger than cero");
        }
        this.lacunarityFactor = factor;
    }

    public void setDefaultPersistanceFactor(float factor) {
        if (factor <= 0) {
            throw new IllegalArgumentException("The persistance factor must be bigger than cero");
        }
        this.persistanceFactor = factor;
    }

    public void setDefaultOctaves(int octaves) {
        if (octaves < 1) {
            throw new IllegalArgumentException("The octaves must be 1 or bigger");
        }
        this.octaves = octaves;
    }

    public void setChunkSize(int chunkSize) {
        if (chunkSize < 1) {
            throw new IllegalArgumentException("The chuckSize must be 1 or bigger");
        }
        this.chunkSize = chunkSize;
    }
}
