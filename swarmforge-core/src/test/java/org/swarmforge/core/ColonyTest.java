/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core;

import org.junit.jupiter.api.*;
import org.swarmforge.core.domain.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Colony management.
 */
class ColonyTest {

    private Colony colony;

    @BeforeEach
    void setUp() {
        colony = new Colony(new org.swarmforge.core.species.LasiusNiger(), 50f, 50f, 25f);
    }

    @Test
    @DisplayName("New colony should have zero population")
    void testInitialState() {
        assertEquals(0, colony.getPopulation());
        assertEquals("Lasius niger", colony.getSpeciesName());
        assertFalse(colony.hasQueen());
    }

    @Test
    @DisplayName("Adding individual should increase population")
    void testAddIndividual() {
        colony.addIndividual(new Individual(colony.getId(), Individual.Caste.WORKER, 50, 50, 25));
        assertEquals(1, colony.getPopulation());
        assertEquals(1, colony.getTotalBorn());
    }

    @Test
    @DisplayName("Adding queen should be detectable")
    void testHasQueen() {
        assertFalse(colony.hasQueen());
        colony.addIndividual(new Individual(colony.getId(), Individual.Caste.QUEEN, 50, 50, 25));
        assertTrue(colony.hasQueen());
    }

    @Test
    @DisplayName("Count by caste should work correctly")
    void testCountByCaste() {
        for (int i = 0; i < 10; i++) {
            colony.addIndividual(new Individual(colony.getId(), Individual.Caste.WORKER, 50, 50, 25));
        }
        for (int i = 0; i < 3; i++) {
            colony.addIndividual(new Individual(colony.getId(), Individual.Caste.SOLDIER, 50, 50, 25));
        }

        assertEquals(10, colony.countByCaste(Individual.Caste.WORKER));
        assertEquals(3, colony.countByCaste(Individual.Caste.SOLDIER));
        assertEquals(0, colony.countByCaste(Individual.Caste.QUEEN));
    }

    @Test
    @DisplayName("Food storage should clamp to zero")
    void testFoodStorage() {
        colony.setFoodStored(100f);
        assertEquals(100f, colony.getFoodStored());

        colony.setFoodStored(-50f);
        assertEquals(0f, colony.getFoodStored());
    }
}
