/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.scenario;

import org.junit.jupiter.api.Test;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.event.GodModeIntervention;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.simulation.SimulationCheckpoint;
import org.swarmforge.core.species.SpeciesRegistry;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Scenario Definition, Scheduled God Mode Events,
 * Scenario Serialization at T=0, and Mid-Simulation Checkpointing.
 */
public class ScenarioGodModeAndCheckpointTest {

    @Test
    public void testStandardScenarioSetupAndGodModeEvents() {
        Scenario scenario = new Scenario("SCENARIO_STANDARD_TEST", "Standard Established Colony", "Healthy start with God Mode event scheduling");
        scenario.setMasterSeed(999L);
        scenario.setBiomeName("TEMPERATE_FOREST");
        scenario.setInitialTemperature(22.0f);
        scenario.setInitialHumidity(0.70f);

        // Add standard colony setup
        scenario.addColony(new Scenario.ColonySetup("Lasius niger", "COLONY_LASIUS", 1, 50, 5, 200, null));

        // Schedule God Mode events for T > 0
        scenario.addEvent(new Scenario.ScenarioEvent(100L, "GOD_MODE_FOOD_DROP", "Spawning food drop", java.util.Map.of("amount", 500f)));
        scenario.addEvent(new Scenario.ScenarioEvent(250L, "GOD_MODE_PATHOGEN_OUTBREAK", "Triggering fungal pathogen outbreak", java.util.Map.of("disasterType", "FUNGAL_OUTBREAK", "intensity", 0.8f)));

        assertEquals(2, scenario.getScheduledEvents().size());
        assertEquals("GOD_MODE_FOOD_DROP", scenario.getScheduledEvents().get(0).eventType());
        assertEquals(250L, scenario.getScheduledEvents().get(1).triggerTick());
    }

    @Test
    public void testSimulationCheckpointCreationAndRestoration() throws Exception {
        org.swarmforge.core.domain.Terrarium terrarium = new org.swarmforge.core.domain.Terrarium(100, 100, 32);
        Simulation sim = new Simulation(terrarium);
        sim.setMasterSeed(4242L);

        Colony colony = sim.addColony("Lasius niger", 1, 20, 2);
        assertNotNull(colony);

        // Run simulation for 50 ticks
        for (int i = 0; i < 50; i++) {
            sim.tick();
        }

        long tick50 = sim.getTickCount();
        assertEquals(50L, tick50);

        // Record a God Mode intervention at tick 50
        GodModeIntervention intervention = GodModeIntervention.triggerDisaster(50L, "FLASH_FLOOD", 0.9f);
        sim.logIntervention(intervention);

        // Create Checkpoint
        SimulationCheckpoint checkpoint = sim.createCheckpoint("Checkpoint_Tick50");
        assertNotNull(checkpoint);
        assertEquals(50L, checkpoint.getTick());
        assertEquals(1, checkpoint.getInterventionsRecorded().size());

        // Serialize checkpoint to compressed bytes
        byte[] compressed = checkpoint.toCompressedBytes();
        assertTrue(compressed.length > 0);

        // Deserialize checkpoint
        SimulationCheckpoint restoredCheckpoint = SimulationCheckpoint.fromCompressedBytes(compressed);
        assertEquals("Checkpoint_Tick50", restoredCheckpoint.getName());
        assertEquals(50L, restoredCheckpoint.getTick());

        // Advance simulation to tick 100
        for (int i = 0; i < 50; i++) {
            sim.tick();
        }
        assertEquals(100L, sim.getTickCount());

        // Restore checkpoint back to tick 50
        boolean success = sim.restoreCheckpoint(restoredCheckpoint);
        assertTrue(success);
        assertEquals(50L, sim.getTickCount());
        assertEquals(1, sim.getInterventionJournal().size());
    }
}
