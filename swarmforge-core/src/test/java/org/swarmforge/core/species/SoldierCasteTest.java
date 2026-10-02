/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.species;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.domain.Individual;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class SoldierCasteTest {

    @Test
    @DisplayName("Verify Soldier Caste Configuration & Specialization")
    public void testSoldierConfiguration() {
        DefaultSpecies species = SpeciesRegistry.getInstance().getSpecies("atta-cephalotes");
        
        // Create an individual (using SOLDIER caste)
        Individual soldier = new Individual(UUID.randomUUID(), Individual.Caste.SOLDIER, 0, 0, 0);
        
        // Apply species configuration
        species.configureIndividual(soldier);
        soldier.setSpecies(species);

        // Verify stats
        assertTrue(soldier.getEnergyLevel() > 0.9f);
        
        // Verify identity
        assertEquals("Atta cephalotes", species.getScientificName());
        assertTrue(soldier.isSoldier());
    }
}
