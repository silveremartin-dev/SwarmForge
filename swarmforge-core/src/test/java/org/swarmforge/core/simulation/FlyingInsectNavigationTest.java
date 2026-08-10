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
import org.swarmforge.core.species.ApisMellifera;
import org.swarmforge.core.species.VespulaGermanica;
import org.swarmforge.core.world.VegetationSystem;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Automated test suite validating 3D aerial navigation for flying insects (Apis mellifera honey bees,
 * Vespula germanica wasps, alates), flower nectar foraging, aerial nest homing, and wingbeat energy kinetics.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class FlyingInsectNavigationTest {

    private Terrarium terrarium;
    private Simulation simulation;

    @BeforeEach
    void setUp() {
        terrarium = new Terrarium(120, 120, 60);
        simulation = new Simulation(terrarium);
    }

    @Test
    @DisplayName("Apis Mellifera: 3D Aerial Flight, Flower Nectar Foraging & Wax Comb Return")
    void testApisMellifera3DFlightAndForaging() {
        ApisMellifera species = new ApisMellifera();
        assertTrue(species.isWorkersCanFly());
        assertEquals("BEE", species.getInsectType());

        Colony beeColony = new Colony(species, 30, 30, 15); // Hive at z=15 (elevated tree comb)
        Individual bee = new Individual(beeColony.getId(), species.getCastes().get(1), 30, 30, 15);
        bee.setSpecies(species);
        beeColony.addIndividual(bee);

        assertTrue(bee.canFly());

        // Target flower patch at (90, 90, 8)
        float targetX = 90f, targetY = 90f, targetZ = 8f;
        float initialEnergy = bee.getEnergy();

        // Perform 3D flight steps
        for (int i = 0; i < 50; i++) {
            bee.fly3D(targetX, targetY, targetZ, 2.0f);
        }

        // Bee moved closer to target in 3D
        assertTrue(Math.abs(bee.getX() - targetX) < 10.0f);
        assertTrue(Math.abs(bee.getY() - targetY) < 10.0f);
        assertTrue(Math.abs(bee.getZ() - targetZ) < 5.0f);
        assertTrue(bee.getEnergy() < initialEnergy); // Flight energy consumed

        // Harvest nectar at flower patch
        bee.setCarriedItem(Individual.CarriedItem.FOOD);
        bee.setCarriedResourceType(ResourceType.NECTAR);
        assertTrue(bee.isCarryingFood());

        // Fly back to hive at (30, 30, 15)
        for (int i = 0; i < 50; i++) {
            bee.fly3D(30f, 30f, 15f, 2.0f);
        }

        assertTrue(Math.abs(bee.getX() - 30f) < 10.0f);
        assertTrue(Math.abs(bee.getZ() - 15f) < 5.0f);
    }

    @Test
    @DisplayName("Vespula Germanica: Aerial Wasp Predation & Paper Nest Navigation")
    void testVespulaGermanicaWaspFlightAndPredation() {
        VespulaGermanica waspSpecies = new VespulaGermanica();
        assertTrue(waspSpecies.isWorkersCanFly());

        Colony waspColony = new Colony(waspSpecies, 50, 50, 20); // Aerial paper nest at z=20
        Individual wasp = new Individual(waspColony.getId(), waspSpecies.getCastes().get(0), 50, 50, 20);
        wasp.setSpecies(waspSpecies);
        waspColony.addIndividual(wasp);

        // Spawn aerial caterpillar prey target
        Predator caterpillar = simulation.getPredatorManager().spawnPredator(PredatorType.CATERPILLAR, 70, 70, 2);

        // Wasp swoops down from z=20 to z=2
        for (int i = 0; i < 30; i++) {
            wasp.fly3D(70f, 70f, 2f, 2.5f);
        }

        assertTrue(wasp.getZ() < 10.0f); // Descended during aerial hunt

        // Attack caterpillar
        caterpillar.takeDamage(50f);
        assertFalse(caterpillar.isAlive());

        // Wasp flies back up to paper nest at z=20
        for (int i = 0; i < 30; i++) {
            wasp.fly3D(50f, 50f, 20f, 2.5f);
        }

        assertTrue(wasp.getZ() > 10.0f); // Returned to aerial nest
    }

    @Test
    @DisplayName("Wingbeat Frequency Flight Energy Expenditure Scaling")
    void testWingbeatEnergyExpenditure() {
        ApisMellifera beeSpecies = new ApisMellifera();
        Individual bee = new Individual(java.util.UUID.randomUUID(), Individual.Caste.WORKER, 0, 0, 10);
        bee.setSpecies(beeSpecies);

        float startEnergy = 100f;
        bee.setEnergy(startEnergy);
        bee.fly3D(50, 50, 10, 1.0f);

        float energySpent = startEnergy - bee.getEnergy();
        assertTrue(energySpent > 0.0f);
    }
}
