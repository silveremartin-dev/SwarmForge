/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
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
class SimulationTest {

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
    void testSpatialIndex() {
        assertNotNull(simulation.getSpatialIndex());
    }

    @Test
    void testFoodIndex() {
        assertNotNull(simulation.getFoodIndex());
    }
}
