/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.swarmforge.core.species.CustomSpecies;
import org.swarmforge.core.species.Species;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Colony class.
 */
class ColonyTest {

    private Colony colony;
    private Species testSpecies;

    @BeforeEach
    void setUp() {
        CustomSpecies species = new CustomSpecies();
        species.setScientificName("Testus antus");
        species.setCommonName("Test Ant");
        species.setWorkerLifespan(5000);
        species.setQueenLifespan(25000);
        testSpecies = species;
        colony = new Colony(testSpecies, 50f, 50f, 5f);
    }

    @Test
    void testColonyCreation() {
        assertNotNull(colony.getId());
        assertEquals(testSpecies, colony.getSpecies());
        assertEquals(50f, colony.getNestX());
        assertEquals(50f, colony.getNestY());
    }

    @Test
    void testAddIndividual() {
        Individual worker = new Individual(colony.getId(), Individual.Caste.WORKER, 50f, 50f, 5f);
        colony.addIndividual(worker);

        assertEquals(1, colony.getLivingIndividuals().size());
        assertTrue(colony.getLivingIndividuals().contains(worker));
    }

    @Test
    void testRemoveDeadIndividuals() {
        Individual worker = new Individual(colony.getId(), Individual.Caste.WORKER, 50f, 50f, 5f);
        colony.addIndividual(worker);
        worker.takeDamage(100); // Kill the worker
        colony.removeDeadIndividuals();

        assertFalse(colony.getLivingIndividuals().contains(worker));
    }

    @Test
    void testFoodStorage() {
        colony.setFoodStored(0f);
        assertEquals(0f, colony.getFoodStored());

        colony.setFoodStored(50f);
        assertEquals(50f, colony.getFoodStored());

        colony.setFoodStored(colony.getFoodStored() - 20f);
        assertEquals(30f, colony.getFoodStored());
    }

    @Test
    void testFoodCannotGoNegative() {
        colony.setFoodStored(10f);
        colony.setFoodStored(colony.getFoodStored() - 20f);

        // setFoodStored uses Math.max(0, food)
        assertEquals(0f, colony.getFoodStored());
    }

    @Test
    void testGetLivingIndividuals() {
        Individual alive = new Individual(colony.getId(), Individual.Caste.WORKER, 50f, 50f, 5f);
        Individual dead = new Individual(colony.getId(), Individual.Caste.WORKER, 50f, 50f, 5f);

        colony.addIndividual(alive);
        colony.addIndividual(dead);
        dead.takeDamage(100); // Kill this worker

        var living = colony.getLivingIndividuals();
        assertEquals(1, living.size());
        assertTrue(living.contains(alive));
        assertFalse(living.contains(dead));
    }

    @Test
    void testCountByCaste() {
        Individual worker1 = new Individual(colony.getId(), Individual.Caste.WORKER, 50f, 50f, 5f);
        Individual worker2 = new Individual(colony.getId(), Individual.Caste.WORKER, 50f, 50f, 5f);
        Individual soldier = new Individual(colony.getId(), Individual.Caste.SOLDIER, 50f, 50f, 5f);

        colony.addIndividual(worker1);
        colony.addIndividual(worker2);
        colony.addIndividual(soldier);

        assertEquals(2, colony.countByCaste(Individual.Caste.WORKER));
        assertEquals(1, colony.countByCaste(Individual.Caste.SOLDIER));
    }

    @Test
    void testColonyListener() {
        final boolean[] birthCalled = { false };
        final boolean[] deathCalled = { false };

        colony.addListener(new ColonyListener() {
            @Override
            public void onBirth(Colony c, Individual ind) {
                birthCalled[0] = true;
            }

            @Override
            public void onDeath(Colony c, Individual ind) {
                deathCalled[0] = true;
            }
        });

        Individual worker = new Individual(colony.getId(), Individual.Caste.WORKER, 50f, 50f, 5f);
        colony.addIndividual(worker);

        assertTrue(birthCalled[0]);
    }

    @Test
    void testHasQueen() {
        assertFalse(colony.hasQueen());

        Individual queen = new Individual(colony.getId(), Individual.Caste.QUEEN, 50f, 50f, 5f);
        colony.addIndividual(queen);

        assertTrue(colony.hasQueen());
    }
}
