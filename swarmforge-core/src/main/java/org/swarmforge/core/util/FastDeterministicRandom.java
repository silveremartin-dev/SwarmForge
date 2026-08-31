/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.util;

/**
 * Fast, 100% deterministic pseudo-random number generator (SplitMix64).
 * Replaces java.util.Random's synchronized CAS (Compare-And-Swap) atomic lock overhead.
 * Guarantees zero lock contention when used in parallel simulation loops while remaining 100% reproducible.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public final class FastDeterministicRandom {

    private long state;

    public FastDeterministicRandom(long seed) {
        this.state = seed == 0 ? 0x9E3779B97F4A7C15L : seed;
    }

    /**
     * Generate next deterministic 64-bit long (SplitMix64 algorithm).
     */
    public long nextLong() {
        long z = (state += 0x9E3779B97F4A7C15L);
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    /**
     * Generate deterministic int in range [0, bound).
     */
    public int nextInt(int bound) {
        if (bound <= 0) return 0;
        long r = (nextLong() >>> 33) * bound;
        return (int) (r >>> 31);
    }

    /**
     * Generate deterministic float in range [0.0f, 1.0f).
     */
    public float nextFloat() {
        return (nextLong() >>> 40) * 0x1.0p-24f;
    }

    /**
     * Generate deterministic double in range [0.0, 1.0).
     */
    public double nextDouble() {
        return (nextLong() >>> 11) * 0x1.0p-53;
    }

    /**
     * Generate deterministic boolean.
     */
    public boolean nextBoolean() {
        return (nextLong() & 1L) != 0;
    }

    public void setSeed(long seed) {
        this.state = seed == 0 ? 0x9E3779B97F4A7C15L : seed;
        this.haveNextNextGaussian = false;
    }

    private double nextNextGaussian;
    private boolean haveNextNextGaussian = false;

    /**
     * Generate 100% deterministic Gaussian (normal) distributed double (mean = 0.0, stdDev = 1.0)
     * using the Box-Muller transformation over SplitMix64.
     */
    public double nextGaussian() {
        if (haveNextNextGaussian) {
            haveNextNextGaussian = false;
            return nextNextGaussian;
        } else {
            double v1, v2, s;
            do {
                v1 = 2.0 * nextDouble() - 1.0;
                v2 = 2.0 * nextDouble() - 1.0;
                s = v1 * v1 + v2 * v2;
            } while (s >= 1.0 || s == 0.0);
            double multiplier = Math.sqrt(-2.0 * Math.log(s) / s);
            nextNextGaussian = v2 * multiplier;
            haveNextNextGaussian = true;
            return v1 * multiplier;
        }
    }

    /**
     * Generate 100% deterministic Gaussian double scaled to specified mean and standard deviation.
     */
    public double nextGaussian(double mean, double stdDev) {
        return mean + nextGaussian() * stdDev;
    }
}
