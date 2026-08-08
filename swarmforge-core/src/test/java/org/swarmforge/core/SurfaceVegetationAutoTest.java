/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core;

import org.junit.jupiter.api.*;
import org.swarmforge.core.domain.*;
import org.swarmforge.core.simulation.*;
import org.swarmforge.core.world.*;
import org.swarmforge.core.world.VegetationSystem.PlantType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Auto-tests for surface vegetation growth, plant harvesting, and ant tree climbing.
 */
class SurfaceVegetationAutoTest {

    private Terrarium terrarium;
    private Simulation simulation;

    @BeforeEach
    void setUp() {
        terrarium = new Terrarium(80, 80, 40);
        simulation = new Simulation(terrarium);
    }

    @Test
    @DisplayName("Vegetation population, growth, and tick update")
    void testVegetationPopulationAndTick() {
        VegetationSystem veg = simulation.getVegetationSystem();
        veg.populate(10, PlantType.GRASS);
        veg.populate(5, PlantType.TREE);

        assertEquals(15, veg.getPlantCount());

        for (int i = 0; i < 50; i++) {
            veg.tick(22f, 0.6f);
        }

        assertTrue(veg.getPlantCount() >= 15);
    }

    @Test
    @DisplayName("Ant plant foraging and tree climbing interactivity")
    void testAntPlantInteractionAndTreeClimbing() {
        VegetationSystem veg = simulation.getVegetationSystem();
        veg.populate(1, PlantType.TREE);

        VegetationSystem.Plant tree = veg.getPlants().get(0);
        tree.growth = 1.0f; // Make tree mature

        Colony colony = simulation.addColony("AttaCephalotes", 0, 1, 0);
        Individual ant = colony.getLivingIndividuals().get(0);
        ant.setPosition(tree.x, tree.z, 0);

        boolean harvested = ant.harvestPlant(tree);
        assertTrue(harvested);
        assertTrue(ant.isCarryingFood());
        assertTrue(ant.isClimbingTree());
        assertTrue(ant.getTreeClimbHeight() > 0f);

        ant.descendTree();
        assertFalse(ant.isClimbingTree());
        assertEquals(0f, ant.getTreeClimbHeight());
    }
}
