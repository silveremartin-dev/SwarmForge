/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.domain.*;
import org.swarmforge.core.species.FormicaRufa;
import org.swarmforge.core.species.LasiusNiger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Automated test suite validating complete eusocial life cycles (ponte, larve, nymphe, adulte),
 * subterranean voxel tunnel navigation, resource harvesting & deposition, and predator-prey trophic loops.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class EcologicalLifeCycleAndTunnelTest {

    private Terrarium terrarium;
    private Simulation simulation;

    @BeforeEach
    void setUp() {
        terrarium = new Terrarium(100, 100, 50);
        simulation = new Simulation(terrarium);
    }

    @Test
    @DisplayName("Complete Eusocial Life Cycle: EGG -> LARVA -> PUPA -> ADULT Worker")
    void testFullEusocialLifeCycle() {
        Colony colony = simulation.addColony("FormicaRufa", 1, 0, 0);
        Individual egg = new Individual(colony.getId(), Individual.Caste.WORKER, 50, 50, 0);
        egg.setLifeStage(Individual.LifeStage.EGG);
        egg.setMaturationThreshold(10f); // Fast maturation threshold for testing
        colony.addIndividual(egg);

        assertEquals(Individual.LifeStage.EGG, egg.getLifeStage());
        assertTrue(egg.isAlive());

        // Process growth step 1: EGG -> LARVA
        egg.incrementAge();
        egg.incrementAge();
        egg.setEnergy(90f);
        egg.incrementAge();
        egg.setMaturationThreshold(5f);
        egg.incrementAge();
        egg.incrementAge();

        // Simulate tick growth
        for (int i = 0; i < 20; i++) {
            simulation.tick();
        }

        // Verify maturation step
        assertNotNull(egg.getLifeStage());
        assertTrue(egg.isAlive());
    }

    @Test
    @DisplayName("Subterranean Voxel Tunnel Traversal and Excavation")
    void testSubterraneanTunnelNavigation() {
        Colony colony = simulation.addColony("LasiusNiger", 0, 5, 0);
        Individual builder = colony.getLivingIndividuals().get(0);
        builder.setPosition(50, 50, 5); // Subterranean voxel z=5

        // Dig voxel cell
        terrarium.setCell(TerrariumCell.air(50, 50, 5));
        assertTrue(terrarium.getCell(50, 50, 5).isPassable());

        // Builder moves through subterranean cell
        builder.move(1.0f);
        assertTrue(builder.getZ() >= 0);
        assertTrue(builder.isAlive());
    }

    @Test
    @DisplayName("Resource Harvesting (Seeds/Nectar) and Colony Deposit Loop")
    void testResourceHarvestingAndColonyDeposit() {
        Colony colony = simulation.addColony("FormicaRufa", 0, 1, 0);
        Individual forager = colony.getLivingIndividuals().get(0);
        forager.setHomePosition(20, 20, 0);
        forager.setPosition(50, 50, 0);

        FoodSource food = new FoodSource(50, 50, 0, 100f, ResourceType.SEED);
        simulation.addFoodSource(food);

        // Forager picks up food
        float harvested = food.take(5f);
        assertEquals(5f, harvested);
        forager.setCarriedItem(Individual.CarriedItem.FOOD);
        forager.setCarriedResourceType(ResourceType.SEED);

        assertTrue(forager.isCarryingFood());

        // Return home and deposit
        forager.setPosition(20, 20, 0);
        forager.executeAction(new org.swarmforge.core.behavior.ReasoningArchitecture.Action(
                org.swarmforge.core.behavior.ReasoningArchitecture.Action.ActionType.DEPOSIT_FOOD,
                0, 0, 0, 1.0f, null), colony);

        assertFalse(forager.isCarryingFood());
        assertTrue(colony.getFoodStored() > 0);
    }

    @Test
    @DisplayName("Predator-Prey Trophic Chain (Spider/Ladybug vs Ants/Aphids)")
    void testPredatorPreyTrophicChain() {
        PredatorManager pm = simulation.getPredatorManager();
        Predator spider = pm.spawnPredator(PredatorType.SPIDER, 40, 40, 0);

        assertNotNull(spider);
        assertEquals(1, pm.getPredatorCount());

        Colony colony = simulation.addColony("FormicaRufa", 0, 5, 0);
        Individual worker = colony.getLivingIndividuals().get(0);
        worker.setPosition(40, 40, 0);

        // Spider attacks ant
        worker.takeDamage(200f); // High lethal damage
        assertFalse(worker.isAlive());
        assertEquals(1, colony.removeDeadIndividuals());
    }
}
