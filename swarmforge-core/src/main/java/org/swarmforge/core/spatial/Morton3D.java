/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.spatial;

/**
 * Morton 3D encoder/decoder for Z-order curve spatial indexing.
 * Provides O(1) encode/decode for 3D coordinates to/from a single long key.
 * Enables cache-friendly iteration and efficient spatial queries.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public final class Morton3D {

    private static final int MAX_COORD = 0x1FFFFF; // 21 bits per dimension

    private Morton3D() {
    }

    /**
     * Encode 3D coordinates into a Morton code.
     *
     * @param x X coordinate (0 to 2097151)
     * @param y Y coordinate (0 to 2097151)
     * @param z Z coordinate (0 to 2097151)
     * @return 63-bit Morton code
     */
    public static long encode(int x, int y, int z) {
        return splitBy3(x) | (splitBy3(y) << 1) | (splitBy3(z) << 2);
    }

    /**
     * Decode Morton code to X coordinate.
     */
    public static int decodeX(long morton) {
        return compact3(morton);
    }

    /**
     * Decode Morton code to Y coordinate.
     */
    public static int decodeY(long morton) {
        return compact3(morton >> 1);
    }

    /**
     * Decode Morton code to Z coordinate.
     */
    public static int decodeZ(long morton) {
        return compact3(morton >> 2);
    }

    /**
     * Decode Morton code to [x, y, z] array.
     */
    public static int[] decode(long morton) {
        return new int[] { decodeX(morton), decodeY(morton), decodeZ(morton) };
    }

    private static long splitBy3(int a) {
        long x = a & MAX_COORD;
        x = (x | x << 32) & 0x1f00000000ffffL;
        x = (x | x << 16) & 0x1f0000ff0000ffL;
        x = (x | x << 8) & 0x100f00f00f00f00fL;
        x = (x | x << 4) & 0x10c30c30c30c30c3L;
        x = (x | x << 2) & 0x1249249249249249L;
        return x;
    }

    private static int compact3(long m) {
        long x = m & 0x1249249249249249L;
        x = (x ^ (x >> 2)) & 0x10c30c30c30c30c3L;
        x = (x ^ (x >> 4)) & 0x100f00f00f00f00fL;
        x = (x ^ (x >> 8)) & 0x1f0000ff0000ffL;
        x = (x ^ (x >> 16)) & 0x1f00000000ffffL;
        x = (x ^ (x >> 32)) & MAX_COORD;
        return (int) x;
    }
}
