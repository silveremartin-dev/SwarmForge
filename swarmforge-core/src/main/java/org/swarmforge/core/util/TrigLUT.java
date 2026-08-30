/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.util;

/**
 * Fast, 100% deterministic Trigonometric Lookup Table (LUT) for O(1) sine/cosine calculations.
 * Replaces expensive transcendental CPU instructions for high-density agent orientation math.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class TrigLUT {

    private static final int RAD_BITS = 12; // 4096 entries
    private static final int RAD_MASK = (1 << RAD_BITS) - 1;
    private static final float RAD_TO_INDEX = (float) ((1 << RAD_BITS) / (2.0 * Math.PI));

    private static final float[] SIN_TABLE = new float[1 << RAD_BITS];
    private static final float[] COS_TABLE = new float[1 << RAD_BITS];

    static {
        double radStep = (2.0 * Math.PI) / (1 << RAD_BITS);
        for (int i = 0; i < (1 << RAD_BITS); i++) {
            SIN_TABLE[i] = (float) Math.sin(i * radStep);
            COS_TABLE[i] = (float) Math.cos(i * radStep);
        }
    }

    /**
     * Fast O(1) sine calculation for radians angle.
     */
    public static float sin(float radians) {
        int index = (int) (radians * RAD_TO_INDEX) & RAD_MASK;
        return SIN_TABLE[index];
    }

    /**
     * Fast O(1) cosine calculation for radians angle.
     */
    public static float cos(float radians) {
        int index = (int) (radians * RAD_TO_INDEX) & RAD_MASK;
        return COS_TABLE[index];
    }

    /**
     * Fast O(1) fast atan2 approximation for orientation steering.
     */
    public static float atan2(float y, float x) {
        if (x == 0.0f) {
            if (y > 0.0f) return (float) (Math.PI / 2.0);
            if (y == 0.0f) return 0.0f;
            return (float) (-Math.PI / 2.0);
        }

        float atan;
        float z = y / x;
        if (Math.abs(z) < 1.0f) {
            atan = z / (1.0f + 0.28f * z * z);
            if (x < 0.0f) {
                if (y < 0.0f) return atan - (float) Math.PI;
                return atan + (float) Math.PI;
            }
        } else {
            atan = (float) (Math.PI / 2.0) - z / (z * z + 0.28f);
            if (y < 0.0f) return atan - (float) Math.PI;
        }
        return atan;
    }
}
