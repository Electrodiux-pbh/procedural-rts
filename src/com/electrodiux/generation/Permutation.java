package com.electrodiux.generation;

import java.util.Random;

public class Permutation {

    private static final int PERMUTATION_SIZE = 1024;
    private static final int HALF_PERMUTATION_SIZE = PERMUTATION_SIZE / 2;
    private static final int PERMUTATION_BITMASK = HALF_PERMUTATION_SIZE - 1;

    private short perm[];

    public Permutation(long seed) {
        Random rand = new Random(seed);

        perm = new short[PERMUTATION_SIZE];
        for (int i = 0; i < perm.length; i++) {
            perm[i] = (short) rand.nextInt(HALF_PERMUTATION_SIZE);
        }
    }

    public static long calculatePermutedSeed(long seed, String permutator) {
        long combinedHash = combineHash(seed, permutator.hashCode());
        return scrambleSeed(combinedHash);
    }

    private static long combineHash(long seed, int permutatorHash) {
        // Combine the seed and permutator hash using bitwise operations
        long combinedHash = seed ^ ((long) permutatorHash << 32);
        return combinedHash;
    }

    private static long scrambleSeed(long seed) {
        // Scramble the seed using bitwise operations
        seed ^= seed >> 33;
        seed *= 0xff51afd7ed558ccdL;
        if (seed % 2 == 0)
            seed = Long.reverseBytes(seed);
        seed ^= seed >> 33;
        seed *= 0xc4ceb9fe1a85ec53L;
        seed ^= seed >> 33;
        return seed;
    }

    public float permutationValue(int x, int y) {
        int xi = (int) Math.floor(x) & PERMUTATION_BITMASK;
        int yi = (int) Math.floor(y) & PERMUTATION_BITMASK;

        int val = perm[perm[xi] + yi];

        return (float) val / (float) HALF_PERMUTATION_SIZE;
    }

    public float permutationValue(int x, int y, int z) {
        int xi = (int) Math.floor(x) & PERMUTATION_BITMASK;
        int yi = (int) Math.floor(y) & PERMUTATION_BITMASK;
        int zi = (int) Math.floor(z) & PERMUTATION_BITMASK;

        int val = perm[perm[perm[xi] + yi] + zi];

        return (float) val / (float) HALF_PERMUTATION_SIZE;
    }

}
