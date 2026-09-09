/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import org.swarmforge.core.domain.Colony;

import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;
import org.swarmforge.core.species.CustomSpecies;
import org.swarmforge.core.event.SimulationEvent;

/**
 * Unit tests for Simulation class.
 */
public class SimulationTest {

    private Simulation simulation;
    private Terrarium terrarium;

    @BeforeEach
    void setUp() {
        terrarium = new Terrarium(50, 50, 20);
        // Initialize terrarium with air
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
    }

    @Test
    void testSimulationCreation() {
        assertNotNull(simulation);
        assertEquals(terrarium, simulation.getTerrarium());
        assertEquals(0, simulation.getTickCount());
    }

    @Test
    void testAddColony() {
        CustomSpecies species = new CustomSpecies();
        species.setScientificName("Testus antus");
        Colony colony = new Colony(species, 25f, 25f, 5f);
        simulation.addColony(colony);

        assertEquals(1, simulation.getColonies().size());
        assertTrue(simulation.getColonies().contains(colony));
    }

    @Test
    void testSpawnFood() {
        simulation.spawnFood(10f, 10f, 5f, 100f, org.swarmforge.core.domain.ResourceType.SUGAR);

        assertEquals(1, simulation.getFoodSources().size());
        var food = simulation.getFoodSources().get(0);
        assertEquals(10f, food.getX());
        assertEquals(100f, food.getQuantity());
    }

    @Test
    void testGetPredatorManager() {
        assertNotNull(simulation.getPredatorManager());
    }

    @Test
    void testGetWeather() {
        assertNotNull(simulation.getWeather());
    }

    @Test
    void testGetPathfinder() {
        assertNotNull(simulation.getPathfinder());
    }

    @Test
    void testGetPheromoneGrid() {
        assertNotNull(simulation.getPheromoneGrid());
    }

    @Test
    void testSpeedMultiplier() {
        simulation.setSpeedMultiplier(2.0f);
        assertEquals(2.0f, simulation.getSpeedMultiplier());

        simulation.setSpeedMultiplier(0.5f);
        assertEquals(0.5f, simulation.getSpeedMultiplier());
    }

    @Test
    void testStateTransitions() {
        assertFalse(simulation.isRunning());

        simulation.start();
        assertTrue(simulation.isRunning());

        simulation.pause();
        assertFalse(simulation.isRunning());

        simulation.stop();
        assertFalse(simulation.isRunning());
    }

    @Test
    void testQueueEvent() {
        SimulationEvent event = new SimulationEvent(
                SimulationEvent.EventType.INFO, 0, "Test event");
        simulation.queueEvent(event);

        var events = simulation.pollEvents();
        assertEquals(1, events.size());
        assertEquals("Test event", events.get(0).getMessage());
    }

    @Test
    void testFoodIndex() {
        assertNotNull(simulation.getFoodIndex());
    }

    @Test
    void testRewindAndSnapshotRestorePreservesHistory() {
        Colony colony = simulation.addColony("Lasius niger", 1, 10, 2, 25f, 25f);
        assertNotNull(colony);

        // Run 50 ticks
        for (int i = 0; i < 50; i++) {
            simulation.tick();
        }
        assertEquals(50, simulation.getTickCount());
        assertTrue(simulation.getHistory().getCount() > 0);
        int historyCountBeforeRewind = simulation.getHistory().getCount();

        // Rewind 20 steps
        boolean rewindOk = simulation.rewind(20);
        assertTrue(rewindOk);
        assertEquals(30, simulation.getTickCount());

        // History buffer must NOT be wiped after rewind!
        assertTrue(simulation.getHistory().getCount() >= historyCountBeforeRewind);

        // Rewind again 10 steps
        boolean secondRewindOk = simulation.rewind(10);
        assertTrue(secondRewindOk);
        assertEquals(20, simulation.getTickCount());

        // Seek forward back to tick 40
        boolean seekOk = simulation.seekToTick(40);
        assertTrue(seekOk);
        assertEquals(40, simulation.getTickCount());
    }

    @Test
    void testColonyStatisticsTruncateAfter() {
        org.swarmforge.core.domain.ColonyStatistics stats = new org.swarmforge.core.domain.ColonyStatistics();
        stats.record(10, 100, 50f, 50f, 0, 0);
        stats.record(20, 120, 60f, 55f, 1, 2);
        stats.record(30, 140, 70f, 60f, 2, 4);
        stats.record(40, 160, 80f, 65f, 3, 6);

        assertEquals(4, stats.getHistory().size());

        // Truncate after tick 25
        stats.truncateAfter(25);
        assertEquals(2, stats.getHistory().size());
        assertEquals(20, stats.getHistory().get(1).tick());
    }

    @Test
    void testEventBusTruncateAfter() {
        org.swarmforge.core.event.EventBus bus = org.swarmforge.core.event.EventBus.getInstance();
        bus.clearHistory();

        bus.publish(new SimulationEvent(SimulationEvent.EventType.INFO, 10, "Event 1"));
        bus.publish(new SimulationEvent(SimulationEvent.EventType.INFO, 20, "Event 2"));
        bus.publish(new SimulationEvent(SimulationEvent.EventType.INFO, 30, "Event 3"));

        assertEquals(3, bus.getHistory(0, 100).size());

        bus.truncateAfter(20);
        var remaining = bus.getHistory(0, 100);
        assertEquals(2, remaining.size());
        assertEquals(20, remaining.get(1).getTick());
    }
}

