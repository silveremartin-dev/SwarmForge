package org.swarmforge.core.ecology;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.domain.*;
import org.swarmforge.core.simulation.Simulation;

import org.swarmforge.core.species.AttaCephalotes;

import static org.junit.jupiter.api.Assertions.*;

class EcologyTest {

    private Simulation simulation;
    private Terrarium terrarium;

    @BeforeEach
    void setUp() {
        terrarium = new Terrarium(100, 100, 10);
        // Clear a working area
        for (int x = 40; x < 60; x++) {
            for (int y = 40; y < 60; y++) {
                terrarium.setCell(org.swarmforge.core.domain.TerrariumCell.air(x, y, 0));
            }
        }
        simulation = new Simulation(terrarium);
    }

    @Test
    void testAttaFungusCycle() {
        // 1. Setup Atta Colony
        Colony colony = new Colony(new AttaCephalotes(), 50f, 50f, 0f);
        simulation.addColony(colony);

        // 2. Setup Leaf Source
        simulation.spawnFood(52f, 52f, 0f, 100f, ResourceType.LEAF);

        // 3. Create Forager directly to skip birth process
        Individual forager = colony.createWorker();
        forager.setJob(Individual.Job.FORAGER);
        forager.setPosition(50f, 50f, 0f); // At nest
        colony.addIndividual(forager);

        // 4. Run Simulation
        // Forager should:
        // - Detect LEAF (needs SimulationContext to be updated)
        // - Go to LEAF
        // - Pick up LEAF
        // - Return to Nest
        // - Deposit LEAF

        // Initial State
        assertEquals(0f, colony.getResourceAmount(ResourceType.LEAF));
        assertEquals(0f, colony.getResourceAmount(ResourceType.MULCH));
        assertEquals(0f, colony.getResourceAmount(ResourceType.FUNGUS));

        // Advance ticks
        // It takes time to move. Distance 10*sqrt(2) approx 14 units. Speed 0.6.
        // Approx 23 ticks one way.
        // Let's run for 200 ticks.
        for (int i = 0; i < 200; i++) {
            simulation.tick();
        }

        // Check Results
        float leafStored = colony.getResourceAmount(ResourceType.LEAF);
        float mulchStored = colony.getResourceAmount(ResourceType.MULCH);
        float fungusStored = colony.getResourceAmount(ResourceType.FUNGUS);

        System.out.println("Stored Leaves: " + leafStored);
        System.out.println("Stored Mulch: " + mulchStored);
        System.out.println("Stored Fungus: " + fungusStored);

        // Since FungusGarden eagerly determines conversion:
        // If leaf was deposited, it might have been instantly converted to Mulch in
        // next tick.
        // And Mulch converted to Fungus.

        // Validation: At least one of these should be > 0 if the cycle works.
        assertTrue(leafStored > 0 || mulchStored > 0 || fungusStored > 0, "Colony should have gathered resources");

        // Verify conversion happened (if leaves were gathered)
        if (leafStored > 0 || mulchStored > 0) {
            // Let's run more to see fungus growth
            for (int i = 0; i < 100; i++) {
                simulation.tick();
            }
            assertTrue(colony.getResourceAmount(ResourceType.FUNGUS) > 0, "Fungus should be growing");
        }
    }
}
