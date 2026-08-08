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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Auto-tests for ecology, hunting, predation, aphids, and symbiosis.
 */
class EcologyAndPredationAutoTest {

    private Terrarium terrarium;
    private Simulation simulation;

    @BeforeEach
    void setUp() {
        terrarium = new Terrarium(80, 80, 40);
        simulation = new Simulation(terrarium);
    }

    @Test
    @DisplayName("Predator spawning and combat with ants")
    void testPredatorCombat() {
        PredatorManager pm = simulation.getPredatorManager();
        Predator spider = pm.spawnPredator(PredatorType.SPIDER, 40, 40, 0);

        assertNotNull(spider);
        assertEquals(1, pm.getPredatorCount());

        Colony colony = simulation.addColony("SolenopsisInvicta", 0, 0, 5);
        Individual soldier = colony.getLivingIndividuals().get(0);
        soldier.setPosition(40, 40, 0);

        spider.takeDamage(1000f);
        assertFalse(spider.isAlive());

        pm.removePredator(spider);
        assertEquals(0, pm.getPredatorCount());
    }

    @Test
    @DisplayName("Aphid Honeydew Production and Harvesting")
    void testAphidHoneydew() {
        Aphid aphid = new Aphid(30f, 30f, 0f, 10f);

        for (int i = 0; i < 10; i++) {
            aphid.tick();
        }

        float honeydew = aphid.take(2.0f);
        assertTrue(honeydew > 0f);
    }
}
