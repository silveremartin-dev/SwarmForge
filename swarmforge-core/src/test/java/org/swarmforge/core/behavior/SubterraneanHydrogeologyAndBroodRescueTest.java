/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.simulation.BehaviorStrategy;
import org.swarmforge.core.simulation.NursingBehavior;
import org.swarmforge.core.species.CustomSpecies;
import org.swarmforge.core.world.SoilHydricCoupling;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Subterranean Hydrogeology & Nurse Brood Flood Evacuation.
 */
public class SubterraneanHydrogeologyAndBroodRescueTest {

    @Test
    @DisplayName("Verify Soil Hydric Coupling Percolation and Frost Front")
    void testPercolationAndSaturation() {
        SoilHydricCoupling hydric = new SoilHydricCoupling(32);
        // Simulate heavy continuous rain (50 mm/h for 4 hours at 20°C)
        hydric.tick(50.0f, 0.0f, 20.0f, 4.0f);

        assertTrue(hydric.getMoistureAtDepth(0) > 60.0f, "Surface moisture should increase after rain");
        assertTrue(hydric.getMoistureAtDepth(1) > 40.0f, "Subsurface moisture should receive percolated rainwater");
    }

    @Test
    @DisplayName("Verify Nurse Ant Emergency Brood Evacuation Under Flooding")
    void testFloodBroodEvacuation() {
        CustomSpecies species = new CustomSpecies();
        species.setScientificName("Lasius niger");
        Colony colony = new Colony(species, 25f, 25f, -3f);

        Individual nurse = new Individual(colony.getId(), Individual.Caste.NURSE, 25f, 25f, -3.0f);
        nurse.setAmbientHumidityPercent(95.0f); // Flooded chamber
        nurse.setAmbientTemperatureC(10.0f);

        NursingBehavior nursingBehavior = new NursingBehavior();
        BehaviorStrategy.BehaviorContext ctx = new BehaviorStrategy.BehaviorContext(
                Float.MAX_VALUE, 0.0f, 1.0f, 0.0f, 0.0f, false, true
        );

        nursingBehavior.execute(nurse, null, colony, ctx);

        assertEquals(Individual.CarriedItem.BROOD, nurse.getCarriedItem(), "Nurse should grab brood during flood");
        assertEquals(Individual.AiState.EVACUATE_FLOOD, nurse.getState(), "Nurse AI state should transition to EVACUATE_FLOOD");
        assertTrue(nurse.getZ() > -3.0f, "Nurse should move vertically upward (+Z) towards dry chambers");
    }
}
