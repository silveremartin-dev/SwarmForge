/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.epidemiology;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.ResourceType;
import org.swarmforge.core.species.Species;
import org.swarmforge.core.structure.physics.NestType;
import org.swarmforge.core.structure.physics.NestVoxelGrid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SocialImmunityEpidemiologyTest {

    @Test
    @DisplayName("Test Individual Infection & Spore Germination")
    void testIndividualInfection() {
        UUID id = UUID.randomUUID();
        IndividualInfection infection = new IndividualInfection(id);

        assertEquals(InfectionState.SUSCEPTIBLE, infection.getState());

        infection.exposeToSpores(PathogenType.BEAUVERIA_BASSIANA, 0.40f);
        assertEquals(InfectionState.EXPOSED, infection.getState());
        assertEquals(PathogenType.BEAUVERIA_BASSIANA, infection.getActivePathogen());

        // Simulate germination
        for (int i = 0; i < 200; i++) {
            infection.tick(1.0f);
        }
        assertEquals(InfectionState.INFECTED, infection.getState());
    }

    @Test
    @DisplayName("Test Allogrooming Spore Removal & Social Immunity Acquisition")
    void testAllogrooming() {
        Random rng = new Random(42);
        SocialImmunityManager manager = new SocialImmunityManager(100f, 100f, 0f);

        IndividualInfection groomer = new IndividualInfection(UUID.randomUUID());
        IndividualInfection target = new IndividualInfection(UUID.randomUUID());

        target.exposeToSpores(PathogenType.METARHIZIUM, 0.20f);
        assertEquals(InfectionState.EXPOSED, target.getState());

        boolean success = manager.performAllogrooming(groomer, target, rng);
        assertTrue(success);

        // Spore load on target should be significantly reduced -> back to susceptible + immune boost
        assertEquals(InfectionState.SUSCEPTIBLE, target.getState());
        assertTrue(target.getSocialImmunityLevel() > 0.0f);
    }

    @Test
    @DisplayName("Test Propolis Antiseptic Coating on Nest Voxels")
    void testPropolisCoating() {
        Species testSpecies = new org.swarmforge.core.species.FormicaRufa();
        Colony colony = new Colony(testSpecies, 0, 0, 0);
        colony.addResource(ResourceType.PROPOLIS_RESIN, 5.0f);

        NestVoxelGrid grid = new NestVoxelGrid(5, 5, 5, NestType.SUBTERRANEAN_SIMPLE);
        grid.getVoxel(2, 2, 2).setFungalSporeLoad(0.80f);

        SocialImmunityManager manager = new SocialImmunityManager(50f, 50f, 0f);

        boolean applied = manager.applyPropolisCoating(colony, grid, 2, 2, 2);
        assertTrue(applied);
        assertEquals(4.0f, colony.getResourceAmount(ResourceType.PROPOLIS_RESIN));

        NestVoxelGrid.VoxelCell cell = grid.getVoxel(2, 2, 2);
        assertTrue(cell.getPropolisCoating() > 0.0f);
        // Antiseptic propolis reduces spore load immediately
        assertTrue(cell.getFungalSporeLoad() < 0.80f);
    }

    @Test
    @DisplayName("Test Necrophorism & Quarantining Cadavers to External Midden")
    void testNecrophorism() {
        Individual worker = new Individual(UUID.randomUUID(), Individual.Caste.WORKER, 10f, 10f, 0f);
        Individual deadAnt = new Individual(UUID.randomUUID(), Individual.Caste.WORKER, 10f, 10f, 0f);
        deadAnt.setHealth(0); // Dead ant

        IndividualInfection deadInfection = new IndividualInfection(deadAnt.getId());
        deadInfection.setState(InfectionState.SPORULATING_DEAD);

        SocialImmunityManager manager = new SocialImmunityManager(200f, 200f, 0f);

        boolean success = manager.performNecrophorism(worker, deadAnt, deadInfection);
        assertTrue(success);

        assertEquals(Individual.CarriedItem.DEAD_ANT, worker.getCarriedItem());
        assertEquals(200f, deadAnt.getX());
        assertEquals(200f, deadAnt.getY());
        assertEquals(1, manager.getExternalMidden().cadavers().size());
    }

    @Test
    @DisplayName("Test Complete Epizootic Epidemiology Simulation Step")
    void testEpizooticStep() {
        Random rng = new Random(99);
        SocialImmunityManager manager = new SocialImmunityManager(100f, 100f, 0f);
        NestVoxelGrid grid = new NestVoxelGrid(5, 5, 5, NestType.SUBTERRANEAN_MOUND);

        Individual ant1 = new Individual(UUID.randomUUID(), Individual.Caste.WORKER, 2f, 2f, 2f);
        Individual ant2 = new Individual(UUID.randomUUID(), Individual.Caste.WORKER, 2f, 2f, 2f);

        Map<UUID, IndividualInfection> registry = new HashMap<>();

        SocialImmunityManager.EpizooticReport report = manager.simulateEpidemiologyStep(
                registry, List.of(ant1, ant2), grid, rng
        );

        assertNotNull(report);
        assertEquals(2, report.susceptibleCount());
    }
}
