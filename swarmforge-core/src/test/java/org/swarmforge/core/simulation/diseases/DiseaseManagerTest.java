/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation.diseases;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.species.CustomSpecies;

/**
 * Unit tests for DiseaseManager class.
 */
class DiseaseManagerTest {

    private Simulation simulation;
    private Colony colony;
    private DiseaseManager diseaseManager;

    @BeforeEach
    void setUp() {
        Terrarium terrarium = new Terrarium(50, 50, 20);
        for (int x = 0; x < 50; x++) {
            for (int y = 0; y < 50; y++) {
                for (int z = 0; z < 20; z++) {
                    terrarium.setCell(new TerrariumCell(
                            x, y, z, TerrariumCell.Material.AIR,
                            new float[TerrariumCell.PHEROMONE_TYPES], 25f, 50f));
                }
            }
        }
        simulation = new Simulation(terrarium);

        CustomSpecies species = new CustomSpecies();
        species.setScientificName("Testus antus");
        colony = new Colony(species, 25f, 25f, 5f);
        simulation.addColony(colony);

        // Add some ants
        for (int i = 0; i < 20; i++) {
            Individual ant = colony.createWorker();
            colony.addIndividual(ant);
        }

        diseaseManager = new DiseaseManager(simulation);
    }

    @Test
    void testDiseaseManagerCreation() {
        assertNotNull(diseaseManager);
    }

    @Test
    void testInitialInfectionCount() {
        assertEquals(0, diseaseManager.getActiveInfections());
    }

    @Test
    void testRegisterDisease() {
        Disease fungal = new FungalInfection();
        diseaseManager.registerDisease(fungal);

        assertTrue(diseaseManager.getDiseases().contains(fungal));
    }

    @Test
    void testInfectIndividual() {
        Disease fungal = new FungalInfection();
        diseaseManager.registerDisease(fungal);

        Individual ant = colony.getLivingIndividuals().get(0);
        diseaseManager.infect(ant, fungal);

        assertTrue(diseaseManager.isInfected(ant.getId().toString()));
    }

    @Test
    void testTickDoesNotThrow() {
        diseaseManager.registerDisease(new FungalInfection());
        diseaseManager.registerDisease(new MiteInfestation());
        diseaseManager.registerDisease(new BacterialGutInfection());

        assertDoesNotThrow(() -> {
            for (int i = 0; i < 100; i++) {
                diseaseManager.tick();
            }
        });
    }

    @Test
    void testStartOutbreak() {
        Disease fungal = new FungalInfection();
        diseaseManager.registerDisease(fungal);

        diseaseManager.startOutbreak(fungal, 5); // 5 initial infections

        assertTrue(diseaseManager.getActiveInfections() > 0);
    }

    @Test
    void testGetStatistics() {
        int totalInfections = diseaseManager.getTotalInfections();
        assertNotNull(totalInfections);
    }

    @Test
    void testFungalInfectionProperties() {
        FungalInfection fungal = new FungalInfection();

        assertEquals("Cordyceps Fungal Infection", fungal.getName());
        assertEquals(Disease.Severity.LETHAL, fungal.getSeverity());
        assertEquals(Disease.TransmissionMode.CONTACT, fungal.getTransmissionMode());
    }

    @Test
    void testMiteInfestationProperties() {
        MiteInfestation mite = new MiteInfestation();

        assertNotNull(mite.getName());
        assertNotNull(mite.getTransmissionMode());
    }

    @Test
    void testBacterialInfectionProperties() {
        BacterialGutInfection bacterial = new BacterialGutInfection();

        assertNotNull(bacterial.getName());
        assertNotNull(bacterial.getTransmissionMode());
    }
}
