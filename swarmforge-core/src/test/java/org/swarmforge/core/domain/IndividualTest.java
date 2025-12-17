/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

/**
 * Unit tests for Individual class.
 */
class IndividualTest {

    private Individual worker;
    private UUID colonyId;

    @BeforeEach
    void setUp() {
        colonyId = UUID.randomUUID();
        worker = new Individual(colonyId, Individual.Caste.WORKER, 10f, 20f, 5f);
    }

    @Test
    void testIndividualCreation() {
        assertNotNull(worker.getId());
        assertEquals(colonyId, worker.getColonyId());
        assertEquals(Individual.Caste.WORKER, worker.getCaste());
        assertEquals(10f, worker.getX());
        assertEquals(20f, worker.getY());
        assertEquals(5f, worker.getZ());
    }

    @Test
    void testDefaultState() {
        assertEquals(Individual.LifeStage.ADULT, worker.getLifeStage());
        assertTrue(worker.isAlive());
        assertEquals(100f, worker.getHealth());
    }

    @Test
    void testSetPosition() {
        worker.setPosition(30f, 40f, 10f);

        assertEquals(30f, worker.getX());
        assertEquals(40f, worker.getY());
        assertEquals(10f, worker.getZ());
    }

    @Test
    void testTakeDamage() {
        worker.takeDamage(30f);
        assertEquals(70f, worker.getHealth());
        assertTrue(worker.isAlive());
    }

    @Test
    void testTakeDamageKills() {
        boolean killed = worker.takeDamage(150f);

        assertTrue(killed);
        assertFalse(worker.isAlive());
        assertEquals(0f, worker.getHealth());
    }

    @Test
    void testSetHealth() {
        worker.setHealth(50f);
        assertEquals(50f, worker.getHealth());

        worker.setHealth(0f);
        assertFalse(worker.isAlive());
    }

    @Test
    void testCarriedItem() {
        assertEquals(Individual.CarriedItem.NONE, worker.getCarriedItem());

        worker.setCarriedItem(Individual.CarriedItem.FOOD);
        assertEquals(Individual.CarriedItem.FOOD, worker.getCarriedItem());
    }

    @Test
    void testLifeStageProgression() {
        Individual egg = new Individual(colonyId, Individual.Caste.WORKER, 0, 0, 0);
        egg.setLifeStage(Individual.LifeStage.EGG);

        assertEquals(Individual.LifeStage.EGG, egg.getLifeStage());

        egg.setLifeStage(Individual.LifeStage.LARVA);
        assertEquals(Individual.LifeStage.LARVA, egg.getLifeStage());

        egg.setLifeStage(Individual.LifeStage.PUPA);
        assertEquals(Individual.LifeStage.PUPA, egg.getLifeStage());

        egg.setLifeStage(Individual.LifeStage.ADULT);
        assertEquals(Individual.LifeStage.ADULT, egg.getLifeStage());
    }

    @Test
    void testJob() {
        assertEquals(Individual.Job.IDLE, worker.getJob());

        worker.setJob(Individual.Job.FORAGER);
        assertEquals(Individual.Job.FORAGER, worker.getJob());
    }

    @Test
    void testEnergy() {
        worker.setEnergy(50f);
        assertEquals(50f, worker.getEnergy());

        worker.setEnergy(150f); // Should clamp
        assertEquals(100f, worker.getEnergy());

        worker.setEnergy(-10f); // Should clamp
        assertEquals(0f, worker.getEnergy());
    }

    @Test
    void testAge() {
        assertEquals(0, worker.getAge());

        worker.incrementAge();
        assertEquals(1, worker.getAge());
    }

    @Test
    void testCasteTypes() {
        Individual soldier = new Individual(colonyId, Individual.Caste.SOLDIER, 0, 0, 0);
        Individual queen = new Individual(colonyId, Individual.Caste.QUEEN, 0, 0, 0);
        Individual male = new Individual(colonyId, Individual.Caste.MALE, 0, 0, 0);
        Individual nurse = new Individual(colonyId, Individual.Caste.NURSE, 0, 0, 0);

        assertEquals(Individual.Caste.SOLDIER, soldier.getCaste());
        assertEquals(Individual.Caste.QUEEN, queen.getCaste());
        assertEquals(Individual.Caste.MALE, male.getCaste());
        assertEquals(Individual.Caste.NURSE, nurse.getCaste());
    }
}
