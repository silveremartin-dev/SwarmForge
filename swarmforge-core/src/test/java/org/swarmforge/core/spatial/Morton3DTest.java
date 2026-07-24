/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.spatial;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Morton3D spatial encoding.
 */
class Morton3DTest {

    @Test
    @DisplayName("Encode and decode should be reversible")
    void testEncodeDecodeCycle() {
        int[] testCoords = { 0, 1, 10, 100, 1000, 10000 };

        for (int x : testCoords) {
            for (int y : testCoords) {
                for (int z : testCoords) {
                    long encoded = Morton3D.encode(x, y, z);
                    assertEquals(x, Morton3D.decodeX(encoded), "X mismatch for " + x + "," + y + "," + z);
                    assertEquals(y, Morton3D.decodeY(encoded), "Y mismatch for " + x + "," + y + "," + z);
                    assertEquals(z, Morton3D.decodeZ(encoded), "Z mismatch for " + x + "," + y + "," + z);
                }
            }
        }
    }

    @Test
    @DisplayName("Decode array should match individual decodes")
    void testDecodeArray() {
        long morton = Morton3D.encode(123, 456, 789);
        int[] decoded = Morton3D.decode(morton);

        assertEquals(123, decoded[0]);
        assertEquals(456, decoded[1]);
        assertEquals(789, decoded[2]);
    }

    @Test
    @DisplayName("Origin should encode to zero")
    void testOrigin() {
        assertEquals(0L, Morton3D.encode(0, 0, 0));
    }

    @Test
    @DisplayName("Adjacent cells should have similar morton codes")
    void testLocality() {
        long m1 = Morton3D.encode(100, 100, 100);
        long m2 = Morton3D.encode(101, 100, 100);
        long m3 = Morton3D.encode(200, 200, 200);

        // Adjacent cells should differ by small amount
        assertTrue(Math.abs(m1 - m2) < Math.abs(m1 - m3));
    }
}
