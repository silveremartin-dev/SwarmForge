/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core;

import org.junit.jupiter.api.*;
import org.swarmforge.core.domain.*;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Individual.Caste;
import org.swarmforge.core.simulation.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Core simulation auto-tests.
 */
class SimulationAutoTest {

    private Terrarium terrarium;
    private Simulation simulation;

    @BeforeEach
    void setUp() {
        terrarium = new Terrarium(100, 100, 50);
        simulation = new Simulation(terrarium);
    }

    @Test
    @DisplayName("Simulation state management (Start, Pause, Stop, Speed)")
    void testStateManagement() {
        assertEquals(Simulation.State.STOPPED, simulation.getState());
        simulation.setSpeedMultiplier(2.0f);
        assertEquals(2.0f, simulation.getSpeedMultiplier());

        simulation.start();
        assertEquals(Simulation.State.RUNNING, simulation.getState());

        simulation.pause();
        assertEquals(Simulation.State.PAUSED, simulation.getState());

        simulation.stop();
        assertEquals(Simulation.State.STOPPED, simulation.getState());
    }

    @Test
    @DisplayName("Colony creation and individual spawning")
    void testColonySpawning() {
        Colony colony = simulation.addColony("FormicaRufa", 1, 50, 10);
        assertNotNull(colony);
        assertEquals(61, colony.getPopulation());
        assertTrue(colony.hasQueen());
        assertEquals(1, colony.countByCaste(Individual.Caste.QUEEN));
        assertEquals(50, colony.countByCaste(Individual.Caste.WORKER));
        assertEquals(10, colony.countByCaste(Individual.Caste.SOLDIER));
    }

    @Test
    @DisplayName("Tick processing and spatial index updates")
    void testTickAndSpatialIndex() {
        Colony colony = simulation.addColony("LasiusNiger", 1, 20, 5);
        simulation.tick();

        assertEquals(1L, simulation.getTickCount());
        assertNotNull(simulation.getSpatialIndex());
        assertNotNull(simulation.getFoodIndex());
    }

    @Test
    @DisplayName("Simulation checkpoint creation and restoration")
    void testCheckpointing() {
        simulation.addColony("FormicaRufa", 1, 10, 0);
        for (int i = 0; i < 15; i++) {
            simulation.tick();
        }

        SimulationCheckpoint cp = simulation.createCheckpoint("CP15");
        assertNotNull(cp);
        assertEquals(15L, cp.getTick());

        for (int i = 0; i < 10; i++) {
            simulation.tick();
        }
        assertEquals(25L, simulation.getTickCount());

        boolean restored = simulation.restoreCheckpoint(cp);
        assertTrue(restored);
        assertEquals(15L, simulation.getTickCount());
    }
}
