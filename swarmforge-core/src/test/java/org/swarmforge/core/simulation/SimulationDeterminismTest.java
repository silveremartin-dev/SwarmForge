package org.swarmforge.core.simulation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Terrarium;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates 100% bit-exact determinism across separate simulation executions
 * sharing identical master random seeds.
 */
public class SimulationDeterminismTest {

    @Test
    @DisplayName("Identical seeds produce identical ant coordinates and biological states after N ticks")
    void testDeterministicSimulationRun() {
        long masterSeed = 987654321L;

        // Run 1
        Simulation sim1 = createSeededSimulation(masterSeed);
        for (int i = 0; i < 60; i++) {
            sim1.tick(0.016666667f);
        }

        // Run 2
        Simulation sim2 = createSeededSimulation(masterSeed);
        for (int i = 0; i < 60; i++) {
            sim2.tick(0.016666667f);
        }

        // Compare colonies
        assertEquals(sim1.getColonies().size(), sim2.getColonies().size(), "Colony counts must match");
        Colony c1 = sim1.getColonies().get(0);
        Colony c2 = sim2.getColonies().get(0);

        List<Individual> ants1 = c1.getLivingIndividuals();
        List<Individual> ants2 = c2.getLivingIndividuals();

        assertEquals(ants1.size(), ants2.size(), "Individual populations must match exactly");
        assertTrue(ants1.size() > 0, "Simulation should have active ants");

        for (int i = 0; i < ants1.size(); i++) {
            Individual a1 = ants1.get(i);
            Individual a2 = ants2.get(i);

            assertEquals(a1.getX(), a2.getX(), 0.0001f, "Ant " + i + " X position must match bitwise");
            assertEquals(a1.getY(), a2.getY(), 0.0001f, "Ant " + i + " Y position must match bitwise");
            assertEquals(a1.getZ(), a2.getZ(), 0.0001f, "Ant " + i + " Z position must match bitwise");
            assertEquals(a1.getHealth(), a2.getHealth(), 0.0001f, "Ant " + i + " health must match bitwise");
            assertEquals(a1.getEnergy(), a2.getEnergy(), 0.0001f, "Ant " + i + " energy must match bitwise");
            assertEquals(a1.getCaste(), a2.getCaste(), "Ant " + i + " caste must match");
        }
    }

    private Simulation createSeededSimulation(long seed) {
        Individual.resetAntNumberGenerator();
        Terrarium terrarium = new Terrarium(64, 64, 32);
        Simulation sim = new Simulation(terrarium, seed);
        sim.addColony("FormicaRufa", 1, 30, 5, 20, 32.0f, 32.0f);
        return sim;
    }
}