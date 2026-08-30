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

    private static final long[] LUT_256 = new long[256];
    private static final int[] DECODE_LUT_512 = new int[512];

    static {
        for (int i = 0; i < 256; i++) {
            long r = 0;
            for (int b = 0; b < 8; b++) {
                if ((i & (1 << b)) != 0) {
                    r |= (1L << (b * 3));
                }
            }
            LUT_256[i] = r;
        }

        for (int i = 0; i < 512; i++) {
            int res = 0;
            if ((i & 1) != 0) res |= 1;
            if ((i & 8) != 0) res |= 2;
            if ((i & 64) != 0) res |= 4;
            DECODE_LUT_512[i] = res;
        }
    }

    private static long splitBy3(int a) {
        long x = a & MAX_COORD;
        return LUT_256[(int) (x & 0xFF)]
                | (LUT_256[(int) ((x >> 8) & 0xFF)] << 24)
                | (LUT_256[(int) ((x >> 16) & 0xFF)] << 48);
    }

    private static int compact3(long m) {
        long x = m & 0x1249249249249249L;
        return (DECODE_LUT_512[(int) (x & 0x1FF)])
                | (DECODE_LUT_512[(int) ((x >> 9) & 0x1FF)] << 3)
                | (DECODE_LUT_512[(int) ((x >> 18) & 0x1FF)] << 6)
                | (DECODE_LUT_512[(int) ((x >> 27) & 0x1FF)] << 9)
                | (DECODE_LUT_512[(int) ((x >> 36) & 0x1FF)] << 12)
                | (DECODE_LUT_512[(int) ((x >> 45) & 0x1FF)] << 15)
                | (DECODE_LUT_512[(int) ((x >> 54) & 0x1FF)] << 18);
    }
}
