/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core;

import org.junit.jupiter.api.*;
import org.swarmforge.core.domain.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Terrarium world container.
 */
class TerrariumTest {

    private Terrarium terrarium;

    @BeforeEach
    void setUp() {
        terrarium = new Terrarium(100, 100, 50);
    }

    @Test
    @DisplayName("New terrarium should have correct dimensions")
    void testDimensions() {
        assertEquals(100, terrarium.getWidth());
        assertEquals(100, terrarium.getHeight());
        assertEquals(50, terrarium.getDepth());
    }

    @Test
    @DisplayName("Getting unset cell should return air")
    void testDefaultCell() {
        TerrariumCell cell = terrarium.getCell(50, 50, 25);
        assertEquals(TerrariumCell.Material.AIR, cell.material());
    }

    @Test
    @DisplayName("Setting and getting cell should work")
    void testSetGetCell() {
        TerrariumCell earthCell = TerrariumCell.earth(10, 20, 5);
        terrarium.setCell(earthCell);

        TerrariumCell retrieved = terrarium.getCell(10, 20, 5);
        assertEquals(TerrariumCell.Material.EARTH, retrieved.material());
    }

    @Test
    @DisplayName("Out of bounds should return earth (solid)")
    void testOutOfBounds() {
        TerrariumCell cell = terrarium.getCell(-1, 0, 0);
        assertEquals(TerrariumCell.Material.EARTH, cell.material());

        cell = terrarium.getCell(200, 0, 0);
        assertEquals(TerrariumCell.Material.EARTH, cell.material());
    }

    @Test
    @DisplayName("inBounds should correctly validate coordinates")
    void testInBounds() {
        assertTrue(terrarium.inBounds(0, 0, 0));
        assertTrue(terrarium.inBounds(99, 99, 49));
        assertFalse(terrarium.inBounds(-1, 0, 0));
        assertFalse(terrarium.inBounds(100, 0, 0));
    }
}
