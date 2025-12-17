/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation.behaviors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.species.Species;
import org.swarmforge.core.species.LasiusNiger;

/**
 * Unit tests for QueenBehavior class.
 */
class QueenBehaviorTest {

    private Simulation simulation;
    private Colony colony;
    private Individual queen;
    private QueenBehavior behavior;

    @BeforeEach
    void setUp() {
        Terrarium terrarium = new Terrarium(50, 50, 20);
        for (int x = 0; x < 50; x++) {
            for (int y = 0; y < 50; y++) {
                for (int z = 0; z < 20; z++) {
                    terrarium.setCell(new TerrariumCell(
                            x, y, z, TerrariumCell.Material.AIR,
                            new float[TerrariumCell.PHEROMONE_TYPES], 25f, 50f));
                }
            }
        }
        simulation = new Simulation(terrarium);

        Species species = new LasiusNiger();
        colony = new Colony(species, 25f, 25f, 5f);
        simulation.addColony(colony);

        queen = colony.createQueen();
        colony.addIndividual(queen);

        behavior = new QueenBehavior(colony, simulation);
    }

    @Test
    void testQueenBehaviorCreation() {
        assertNotNull(behavior);
        assertEquals(QueenBehavior.QueenState.VIRGIN, behavior.getState());
    }

    @Test
    void testInitialStateIsVirgin() {
        assertEquals(QueenBehavior.QueenState.VIRGIN, behavior.getState());
        assertFalse(behavior.isMatingFlightComplete());
    }

    @Test
    void testTickDoesNotThrow() {
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 100; i++) {
                behavior.tick(queen);
            }
        });
    }

    @Test
    void testTickWithNullQueen() {
        assertDoesNotThrow(() -> behavior.tick(null));
    }

    @Test
    void testTickWithNonQueenCaste() {
        Individual worker = colony.createWorker();
        assertDoesNotThrow(() -> behavior.tick(worker));
    }

    @Test
    void testEggsLaidCounterStarts() {
        assertEquals(0, behavior.getEggsLaidTotal());
    }

    @Test
    void testStoredSpermInitiallyZero() {
        assertEquals(0, behavior.getStoredSperm());
    }

    @Test
    void testSetState() {
        behavior.setState(QueenBehavior.QueenState.MATED);
        assertEquals(QueenBehavior.QueenState.MATED, behavior.getState());
    }

    @Test
    void testFertilityRate() {
        assertEquals(1.0f, behavior.getFertilityRate());
    }

    @Test
    void testAllQueenStates() {
        for (QueenBehavior.QueenState state : QueenBehavior.QueenState.values()) {
            behavior.setState(state);
            assertEquals(state, behavior.getState());
        }
    }
}
