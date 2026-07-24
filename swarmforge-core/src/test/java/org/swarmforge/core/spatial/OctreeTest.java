/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.spatial;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class OctreeTest {

    @Test
    public void testInsertAndQuery() {
        Octree<String> tree = new Octree<>(0, 0, 0, 100, 4, 5);

        tree.insert("A", 10, 10, 10);
        tree.insert("B", 20, 20, 20);
        tree.insert("C", 90, 90, 90); // Far away

        List<String> results = tree.queryRadius(15, 15, 15, 10);
        assertTrue(results.contains("A"));
        assertTrue(results.contains("B"));
        assertFalse(results.contains("C"));
    }

    @Test
    public void testUpdate() {
        Octree<String> tree = new Octree<>(0, 0, 0, 100, 4, 5);
        tree.insert("Mover", 10, 10, 10);

        List<String> initial = tree.queryRadius(10, 10, 10, 5);
        assertEquals(1, initial.size());

        // Move out of range
        tree.update("Mover", 10, 10, 10, 50, 50, 50);

        List<String> after = tree.queryRadius(10, 10, 10, 5);
        assertTrue(after.isEmpty());

        List<String> newLoc = tree.queryRadius(50, 50, 50, 5);
        assertTrue(newLoc.contains("Mover"));
    }
}
