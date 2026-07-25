/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.domain.CasteTemplate;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.species.CustomSpecies;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying SI-compliant parameter hardening, HeatmapEngine matrix output,
 * caste task allocations, decision architecture selection, and venom properties.
 */
public class HeatmapAndCasteHardeningTest {

    private Terrarium terrarium;
    private CustomSpecies species;

    @BeforeEach
    public void setUp() {
        terrarium = new Terrarium(50, 50, 10);
        species = new CustomSpecies();
        species.setScientificName("Formica rufa");
    }

    @Test
    public void testCasteTemplateParametersHardening() {
        CasteTemplate template = new CasteTemplate("Soldier", 120.0f, 15.0f);
        template.setTaskDefenseWeight(0.60f);
        template.setTaskForagingWeight(0.10f);
        template.setTargetRatio(0.20f);
        template.setDecisionArchitectureType("NEURAL_NETWORK");
        template.setVenomType("FORMIC_ACID");
        template.setVenomToxicity(25.0f);

        assertEquals(0.60f, template.getTaskDefenseWeight(), 0.001f);
        assertEquals(0.10f, template.getTaskForagingWeight(), 0.001f);
        assertEquals(0.20f, template.getTargetRatio(), 0.001f);
        assertEquals("NEURAL_NETWORK", template.getDecisionArchitectureType());
        assertEquals("FORMIC_ACID", template.getVenomType());
        assertEquals(25.0f, template.getVenomToxicity(), 0.001f);
    }

    @Test
    public void testHeatmapEnginePheromoneGeneration() {
        float[][] map = HeatmapEngine.generateHeatmap(terrarium, List.of(), 0, HeatmapEngine.HeatmapType.PHEROMONE_FOOD);
        assertNotNull(map);
        assertEquals(50, map.length);
        assertEquals(50, map[0].length);
    }

    @Test
    public void testHeatmapEngineChamberSpecialization() {
        Colony colony = new Colony(species, 25.0f, 25.0f, 0.0f);
        float[][] map = HeatmapEngine.generateHeatmap(terrarium, List.of(colony), 0, HeatmapEngine.HeatmapType.CHAMBER_SPECIALIZATION);
        assertNotNull(map);
        assertEquals(50, map.length);
        assertEquals(50, map[0].length);
        assertTrue(map[25][25] > 0.0f);
    }

    @Test
    public void testHeatmapEngineSoilStability() {
        float[][] map = HeatmapEngine.generateHeatmap(terrarium, List.of(), 0, HeatmapEngine.HeatmapType.SOIL_STABILITY);
        assertNotNull(map);
        assertEquals(50, map.length);
        assertEquals(50, map[0].length);
        assertTrue(map[10][10] >= 0.0f && map[10][10] <= 1.0f);
    }
}
