/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.scenario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Scenario collision prevention, deterministic checksum calculation,
 * and content equality comparison.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ScenarioChecksumAndCollisionTest {

    @Test
    @DisplayName("Deterministic checksum generation for identical configurations")
    void testDeterministicChecksum() {
        Scenario s1 = AcademicScenarios.createLevyVsBrownianScenario(42L);
        Scenario s2 = AcademicScenarios.createLevyVsBrownianScenario(42L);

        String checksum1 = s1.calculateChecksum();
        String checksum2 = s2.calculateChecksum();

        assertNotNull(checksum1);
        assertEquals(16, checksum1.length());
        assertEquals(checksum1, checksum2, "Checksums for identical scenario configurations must be identical");
        assertTrue(s1.isContentEqualTo(s2));
    }

    @Test
    @DisplayName("Checksum divergence upon configuration change")
    void testChecksumDivergenceOnParameterChange() {
        Scenario s1 = AcademicScenarios.createLevyVsBrownianScenario(42L);
        Scenario s2 = AcademicScenarios.createLevyVsBrownianScenario(42L);

        // Modify a biological parameter in s2
        s2.setInitialTemperature(35.0f); // Heatwave modification

        String checksum1 = s1.calculateChecksum();
        String checksum2 = s2.calculateChecksum();

        assertNotEquals(checksum1, checksum2, "Modifying temperature must produce divergent checksum");
        assertFalse(s1.isContentEqualTo(s2), "Scenarios with different parameters must not be content equal");
    }

    @Test
    @DisplayName("Academic scenarios have unique checksums and built-in protection")
    void testAcademicScenariosUniqueness() {
        List<Scenario> list = AcademicScenarios.getAllAcademicScenarios(42L);
        assertFalse(list.isEmpty());

        java.util.Set<String> ids = new java.util.HashSet<>();
        java.util.Set<String> checksums = new java.util.HashSet<>();

        for (Scenario s : list) {
            assertTrue(s.isBuiltIn(), "Academic scenario must have builtIn=true");
            assertEquals("Academic Reference", s.getAuthor());
            assertTrue(ids.add(s.getId()), "Scenario ID must be unique: " + s.getId());
            assertTrue(checksums.add(s.getContentChecksum()), "Scenario checksum must be unique: " + s.getTitle());
        }
    }

    @Test
    @DisplayName("Self-contained scenario bundles embedded species, world and climate")
    void testSelfContainedScenarioBundle() {
        Scenario scenario = new Scenario("CUSTOM_SELF_CONTAINED_01", "Mission Amazonie Profonde", "Scénario auto-contenu sans dépendances");
        
        // 1. Create and embed custom species
        org.swarmforge.core.species.CustomSpecies alienAnt = new org.swarmforge.core.species.CustomSpecies();
        alienAnt.setId("alien_ant_01");
        alienAnt.setCommonName("Fourmi des Profondeurs");
        alienAnt.setScientificName("Crypticus formicoid");
        alienAnt.setAggression(0.95f);
        alienAnt.setWorkerSpeed(0.88f);
        scenario.embedSpecies(alienAnt);

        // 2. Embed custom world & climate
        scenario.setEmbeddedWorldConfig(java.util.Map.of("depth", 100, "biome", "TROPICAL_DEEP", "soilMixingRate", 0.75));
        scenario.setEmbeddedClimateConfig(java.util.Map.of("temperatureMin", 28.0, "temperatureMax", 38.0, "humidity", 90.0));

        assertTrue(scenario.isSelfContained(), "Scenario with embedded assets must report isSelfContained = true");
        assertEquals(1, scenario.getEmbeddedSpecies().size());
        assertEquals("Crypticus formicoid", scenario.getEmbeddedSpecies().get("alien_ant_01").getScientificName());

        // Checksum must be deterministic and include embedded assets
        String checksum = scenario.calculateChecksum();
        assertNotNull(checksum);
        assertEquals(16, checksum.length());

        // Registering into registry
        org.swarmforge.core.species.SpeciesRegistry.getInstance().register(alienAnt);
        assertTrue(org.swarmforge.core.species.SpeciesRegistry.getInstance().get("Crypticus formicoid").isPresent());
    }

    @Test
    @DisplayName("CustomSpecies checksum determinism and modification divergence")
    void testCustomSpeciesChecksum() {
        org.swarmforge.core.species.CustomSpecies sp1 = new org.swarmforge.core.species.CustomSpecies();
        sp1.setScientificName("Atta cephalotes");
        sp1.setAggression(0.4f);

        org.swarmforge.core.species.CustomSpecies sp2 = new org.swarmforge.core.species.CustomSpecies();
        sp2.setScientificName("Atta cephalotes");
        sp2.setAggression(0.4f);

        assertEquals(sp1.calculateChecksum(), sp2.calculateChecksum());
        assertTrue(sp1.isContentEqualTo(sp2));

        sp2.setAggression(0.85f);
        assertNotEquals(sp1.calculateChecksum(), sp2.calculateChecksum());
        assertFalse(sp1.isContentEqualTo(sp2));
    }
}
