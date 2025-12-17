package org.swarmforge.core.simulation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.species.CustomSpecies;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PersistenceTest {

    @TempDir
    Path tempDir;

    @Test
    void testSaveLoadCycle() throws IOException, ClassNotFoundException {
        // 1. Setup Simulation
        Terrarium terrarium = new Terrarium(100, 100, 50);
        Simulation sim = new Simulation(terrarium);
        sim.getPheromoneGrid().setMaxHeightAboveGround(100);

        CustomSpecies species = new CustomSpecies();
        species.setCommonName("Test Ant");
        species.setScientificName("Testus");
        Colony colony = new Colony(species, 50, 50, 10);
        colony.addIndividual(new Individual(colony.getId(), Individual.Caste.QUEEN, 50, 50, 10));
        Individual worker = new Individual(colony.getId(), Individual.Caste.WORKER, 51, 51, 10);
        worker.setHealth(80f);
        colony.addIndividual(worker);

        sim.addColony(colony);

        // Deposit some pheromone
        sim.getPheromoneGrid().deposit(50, 50, 10, 0, 1.0f);

        // Advance season
        sim.getSeasonManager().setDayOfYear(100);

        // 2. Save
        Path saveFile = tempDir.resolve("save.sim");
        SimulationSerializer.saveToFile(sim, saveFile.toString());

        assertTrue(java.nio.file.Files.exists(saveFile));

        // 3. Reset and Load
        sim.reset(0);
        assertEquals(0, sim.getColonies().size());

        SimulationSerializer.loadFromFile(sim, saveFile.toString());

        // 4. Verify
        assertEquals(1, sim.getColonies().size());
        Colony loadedColony = sim.getColonies().get(0);
        assertEquals(2, loadedColony.getLivingIndividuals().size());

        Individual loadedWorker = loadedColony.getLivingIndividuals().stream()
                .filter(i -> i.getCaste() == Individual.Caste.WORKER)
                .findFirst().orElseThrow();

        assertEquals(80f, loadedWorker.getHealth(), 0.01f);

        // Check pheromone
        float pVal = sim.getPheromoneGrid().read(50, 50, 10, 0);
        assertTrue(pVal > 0.0f, "Pheromone should be preserved");

        // Check season
        assertEquals(100, sim.getSeasonManager().getDayOfYear(), "Day of year should be preserved");
    }
}
