package org.swarmforge.core.ecology;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.domain.*;
import org.swarmforge.core.simulation.Aphid;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.species.LasiusNiger;
import org.swarmforge.core.domain.ResourceType;

import static org.junit.jupiter.api.Assertions.*;

class SymbiosisTest {

    private Simulation simulation;
    private Terrarium terrarium;

    @BeforeEach
    void setUp() {
        terrarium = new Terrarium(100, 100, 10);
        simulation = new Simulation(terrarium);
    }

    @Test
    void testAphidMilking() {
        // 1. Setup Lasius Colony
        Colony colony = new Colony(new LasiusNiger(), 50f, 50f, 0f);
        simulation.addColony(colony);

        // 2. Setup Aphid (regenerating food)
        Aphid aphid = new Aphid(51f, 51f, 0f, 5f); // Max 5 honeydew
        simulation.addFoodSource(aphid);

        // 3. Create Forager
        Individual forager = colony.createWorker();
        forager.setJob(Individual.Job.FORAGER);
        forager.setPosition(50f, 50f, 0f); // At nest
        colony.addIndividual(forager);

        // 4. Run Simulation
        // Forager should find Aphid, take some honeydew.

        // Initial check
        assertEquals(0f, colony.getResourceAmount(ResourceType.HONEYDEW));

        // Run for enough ticks
        for (int i = 0; i < 500; i++) {
            simulation.tick();
        }

        // Verify Honeydew collected
        float honeydew = colony.getResourceAmount(ResourceType.HONEYDEW);
        System.out.println("Honeydew collected: " + honeydew);
        assertTrue(honeydew > 0, "Ants should collect honeydew from Aphids");
    }
}
