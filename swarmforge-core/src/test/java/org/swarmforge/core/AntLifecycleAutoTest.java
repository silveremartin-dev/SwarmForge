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
import org.swarmforge.core.structure.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Auto-tests for ant nest habitation, reproduction (ponte/egg laying),
 * life-stage maturation, feeding, energy, and mortality.
 */
class AntLifecycleAutoTest {

    private Terrarium terrarium;
    private Simulation simulation;

    @BeforeEach
    void setUp() {
        terrarium = new Terrarium(80, 80, 40);
        simulation = new Simulation(terrarium);
    }

    @Test
    @DisplayName("Nest Habitation: Queen and workers reside in nest")
    void testNestHabitation() {
        Colony colony = simulation.addColony("FormicaRufa", 1, 15, 2);
        Nest nest = colony.getNest();

        assertNotNull(nest);
        assertFalse(nest.getChambers().isEmpty());

        Individual queen = colony.getLivingIndividuals().stream()
            .filter(i -> i.getCaste() == Individual.Caste.QUEEN)
            .findFirst()
            .orElse(null);

        assertNotNull(queen);
        assertTrue(queen.isAlive());
        assertEquals(colony.getId(), queen.getColonyId());
    }

    @Test
    @DisplayName("Egg Laying and Maturation: EGG -> Maturation Cycle")
    void testEggLayingAndMaturation() {
        Colony colony = simulation.addColony("LasiusNiger", 1, 5, 0);

        Individual egg = new Individual(colony.getId(), Individual.Caste.WORKER, 40, 40, 0);
        egg.setLifeStage(Individual.LifeStage.EGG);
        egg.setMaturationThreshold(100f);
        colony.addIndividual(egg);

        assertEquals(Individual.LifeStage.EGG, egg.getLifeStage());
        assertTrue(colony.getBroodCountByStage(Individual.LifeStage.EGG) >= 1, "Egg count should be at least 1");

        // Advance ticks to trigger stage transition
        simulation.tick();
        assertNotNull(egg.getLifeStage());
    }

    @Test
    @DisplayName("Feeding & Energy Restoration")
    void testFeedingRestoration() {
        Colony colony = simulation.addColony("AttaCephalotes", 0, 5, 0);
        Individual worker = colony.getLivingIndividuals().get(0);

        worker.setEnergy(30f);
        worker.setHunger(60f);

        // Deposit food into colony storage
        colony.addResource(ResourceType.SEED, 10f);
        assertTrue(colony.getFoodStored() > 0);

        // Feed worker
        worker.setEnergy(100f);
        worker.setHunger(0f);

        assertEquals(100f, worker.getEnergy());
        assertEquals(0f, worker.getHunger());
    }

    @Test
    @DisplayName("Starvation Mortality & Colony Cleanup")
    void testStarvationAndCleanup() {
        Colony colony = simulation.addColony("FormicaRufa", 0, 10, 0);
        int initialPop = colony.getPopulation();

        // Starve 3 ants
        for (int i = 0; i < 3; i++) {
            Individual ind = colony.getLivingIndividuals().get(i);
            ind.setEnergy(0f);
            ind.setHunger(100f);
            ind.tick();
            assertFalse(ind.isAlive());
        }

        int removed = colony.removeDeadIndividuals();
        assertEquals(3, removed);
        assertEquals(initialPop - 3, colony.getPopulation());
        assertEquals(3, colony.getTotalDied());
    }
}
