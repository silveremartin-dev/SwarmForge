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
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.species.CustomSpecies;
import org.swarmforge.core.behavior.FSMArchitecture;

/**
 * Verification test for behavior integration.
 */
class BehaviorTest {

    private Simulation simulation;
    private Terrarium terrarium;
    private Colony colony;

    @BeforeEach
    void setUp() {
        terrarium = new Terrarium(50, 50, 20);
        simulation = new Simulation(terrarium);

        CustomSpecies species = new CustomSpecies();
        species.setScientificName("Testus Behaviorus");
        colony = new Colony(species, 25f, 25f, 5f);
        simulation.addColony(colony);
        simulation.start();

        // Ensure entrance is clear
        terrarium.setCell(org.swarmforge.core.domain.TerrariumCell.air(25, 25, 5));
        terrarium.setCell(org.swarmforge.core.domain.TerrariumCell.air(24, 25, 5));
        terrarium.setCell(org.swarmforge.core.domain.TerrariumCell.air(26, 25, 5));
        terrarium.setCell(org.swarmforge.core.domain.TerrariumCell.air(25, 24, 5));
        terrarium.setCell(org.swarmforge.core.domain.TerrariumCell.air(25, 26, 5));
    }

    @Test
    void testFSMIntegration() {
        // Create an ant with FSM brain
        Individual ant = colony.createWorker();
        // Ensure it has a brain
        assertNotNull(ant.getBrain(), "Ant should have a brain initialized");
        assertTrue(ant.getBrain() instanceof FSMArchitecture, "Brain should be FSM");

        // Initial position
        float startX = ant.getX();
        float startY = ant.getY();

        // Run simulation for a few ticks
        for (int i = 0; i < 10; i++) {
            simulation.tick();
        }

        // Verify movement occurred (FSM default behavior is to explore/move randomly)
        // Note: It's possible but unlikely it moved exactly back to 0,0, checking for
        // change
        assertFalse(ant.getX() == startX && ant.getY() == startY,
                "Ant should have moved from initial position under FSM control");
    }

    @Test
    void testBrainSwapping() {
        Individual ant = colony.createSoldier();
        assertTrue(ant.getBrain() instanceof FSMArchitecture);

        // Swap to a mock brain or different architecture (e.g. check if we can set it)
        // For now just checking setBrain works
        ant.setBrain(new FSMArchitecture());
        assertNotNull(ant.getBrain());
    }
}
