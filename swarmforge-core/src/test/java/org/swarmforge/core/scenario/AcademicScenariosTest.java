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
        assertEquals(4, scenarios.size());

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
