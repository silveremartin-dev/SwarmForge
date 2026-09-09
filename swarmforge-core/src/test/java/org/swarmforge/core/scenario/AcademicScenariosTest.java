/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.scenario;

import org.junit.jupiter.api.Test;
import org.swarmforge.core.behavior.BDIArchitecture;
import org.swarmforge.core.behavior.ReasoningArchitecture;
import org.swarmforge.core.behavior.ReasoningArchitecture.Action;
import org.swarmforge.core.behavior.ReasoningArchitecture.ActionResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AcademicScenariosTest {

    @Test
    public void testAcademicScenariosCreation() {
        List<Scenario> scenarios = AcademicScenarios.getAllAcademicScenarios(12345L);
        assertNotNull(scenarios);
        assertEquals(14, scenarios.size());

        for (Scenario sc : scenarios) {
            assertNotNull(sc.getBiomeName(), "Scenario " + sc.getId() + " must have a non-null biome name!");
            assertFalse(sc.getBiomeName().isEmpty(), "Scenario " + sc.getId() + " biome name must not be empty!");
        }

        Scenario s1 = scenarios.get(0);
        assertEquals("ACAD_01_LEVY_BROWNIAN", s1.getId());
        assertEquals(12345L, s1.getMasterSeed());
        assertFalse(s1.getColonies().isEmpty());
        assertTrue(s1.getTargetMetrics().contains("FORAGING_EFFICIENCY_INDEX"));

        Scenario s2 = scenarios.get(1);
        assertEquals("ACAD_02_POLYETHISM_BDI", s2.getId());
        assertTrue(s2.getTargetMetrics().contains("SPECIALIZATION_INDEX"));

        Scenario s3 = scenarios.get(2);
        assertEquals("ACAD_03_NEST_MORPHOGENESIS", s3.getId());
        assertTrue(s3.getTargetMetrics().contains("TUNNEL_FRACTAL_DIMENSION"));

        Scenario s4 = scenarios.get(3);
        assertEquals("ACAD_04_INTERSPECIFIC_COMPETITION", s4.getId());
        assertEquals(2, s4.getColonies().size());

        Scenario s9 = scenarios.get(8);
        assertEquals("ACAD_09_DULOSIS_RAID", s9.getId());
        assertEquals(2, s9.getColonies().size());
        assertEquals("TEMPERATE_FOREST", s9.getBiomeName());

        Scenario s10 = scenarios.get(9);
        assertEquals("ACAD_10_SAVANNA_COEVOLUTION", s10.getId());
        assertEquals("SAVANNA", s10.getBiomeName());

        Scenario s11 = scenarios.get(10);
        assertEquals("ACAD_11_ALPINE_THERMOREGULATION", s11.getId());
        assertEquals("ALPINE_TUNDRA", s11.getBiomeName());

        Scenario s12 = scenarios.get(11);
        assertEquals("ACAD_12_BOREAL_SOLAR_DOMES", s12.getId());
        assertEquals("BOREAL_TAIGA", s12.getBiomeName());

        Scenario s13 = scenarios.get(12);
        assertEquals("ACAD_13_STEPPE_HARVESTING", s13.getId());
        assertEquals("STEPPE", s13.getBiomeName());

        Scenario s14 = scenarios.get(13);
        assertEquals("ACAD_14_WETLAND_FLOOD_RAFTING", s14.getId());
        assertEquals("WETLAND", s14.getBiomeName());
    }

    @Test
    public void testBDIArchitectureExecution() {
        BDIArchitecture bdi = new BDIArchitecture();
        assertEquals(ReasoningArchitecture.ArchitectureType.BDI, bdi.getType());

        // Test decision without agent
        Action action = bdi.decide(null, null);
        assertNotNull(action);

        // Test reward update
        bdi.update(null, action, ActionResult.ok());
        assertTrue(bdi.getTotalAccumulatedReward() > 0);
    }
}
