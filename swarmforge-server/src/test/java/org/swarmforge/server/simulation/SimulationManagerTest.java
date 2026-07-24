/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server.simulation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.swarmforge.core.simulation.Simulation;
import static org.junit.jupiter.api.Assertions.*;

class SimulationManagerTest {

    @Test
    @DisplayName("Should create and retrieve simulation")
    void testCreateAndRetrieve() {
        SimulationManager manager = new SimulationManager();
        manager.createSimulation("sim1", "Test Sim 1", 100, 100, 50);

        assertTrue(manager.getSimulation("sim1").isPresent());
        assertEquals("Test Sim 1", manager.getSimulationName("sim1").get());

        Simulation sim = manager.getSimulation("sim1").get();
        assertNotNull(sim);
        assertEquals(100, sim.getTerrarium().getWidth());
    }

    @Test
    @DisplayName("Should remove simulation")
    void testRemove() {
        SimulationManager manager = new SimulationManager();
        manager.createSimulation("sim2", "Test Sim 2", 100, 100, 50);

        assertTrue(manager.getSimulation("sim2").isPresent());

        manager.removeSimulation("sim2");
        assertFalse(manager.getSimulation("sim2").isPresent());
    }

    @Test
    @DisplayName("Should handle duplicates nicely (log warning, ignored)")
    void testDuplicateCreate() {
        SimulationManager manager = new SimulationManager();
        manager.createSimulation("sim3", "First", 100, 100, 50);
        manager.createSimulation("sim3", "Second", 200, 200, 100); // Should be ignored or fail safe

        assertEquals("First", manager.getSimulationName("sim3").get());
        assertEquals(100, manager.getSimulation("sim3").get().getTerrarium().getWidth());
    }
}
