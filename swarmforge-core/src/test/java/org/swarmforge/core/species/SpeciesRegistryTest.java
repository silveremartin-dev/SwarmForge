/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.species;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Automated test suite validating that all 14+ builtin eusocial species
 * (Leafcutter Ants, Honey Bees, Bumblebees, Wasps, Hornets, Termites, Aphids, Thrips, Wood Beetles)
 * are properly loaded in the SpeciesRegistry.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SpeciesRegistryTest {

    @Test
    @DisplayName("Verify Builtin Eusocial Species Registration Parity")
    void testBuiltinSpeciesRegistration() {
        SpeciesRegistry registry = SpeciesRegistry.getInstance();
        Collection<CustomSpecies> speciesList = registry.getAllSpecies();

        assertTrue(speciesList.size() >= 14, "Expected at least 14 registered eusocial species, got: " + speciesList.size());

        // Test Leafcutter Ant (Atta cephalotes)
        assertTrue(registry.get("Atta cephalotes").isPresent());
        Species atta = registry.get("Atta cephalotes").get();
        assertEquals("Atta cephalotes", atta.getScientificName());

        // Test Buff-Tailed Bumblebee (Bombus terrestris)
        assertTrue(registry.get("Bombus terrestris").isPresent());
        Species bombus = registry.get("Bombus terrestris").get();
        assertTrue(bombus.isWorkersCanFly());
        assertEquals("BEE", bombus.getInsectType());

        // Test European Hornet (Vespa crabro)
        assertTrue(registry.get("Vespa crabro").isPresent());
        Species vespa = registry.get("Vespa crabro").get();
        assertTrue(vespa.isWorkersCanFly());
        assertEquals("WASP", vespa.getInsectType());

        // Test Honey Bee (Apis mellifera)
        assertTrue(registry.get("Apis mellifera").isPresent());

        // Test Subterranean Termite (Reticulitermes flavipes)
        assertTrue(registry.get("Reticulitermes flavipes").isPresent());

        // Test Bamboo Aphid (Pseudoregma bambucicola)
        assertTrue(registry.get("Pseudoregma bambucicola").isPresent());

        // Test Gall Thrips (Kladothrips harteri)
        assertTrue(registry.get("Kladothrips harteri").isPresent());

        // Test Eucalyptus Wood Beetle (Austroplatypus incompertus)
        assertTrue(registry.get("Austroplatypus incompertus").isPresent());
    }
}
