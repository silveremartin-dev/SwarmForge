/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core;

import org.junit.jupiter.api.*;
import org.swarmforge.core.scenario.SimulationAutoTestRunner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end exhaustive scenario runner test.
 */
class SimulationExhaustiveScenarioTest {

    @Test
    @DisplayName("Run All Simulation Auto-Test Scenarios")
    void testRunAllSimulationScenarios() {
        SimulationAutoTestRunner runner = new SimulationAutoTestRunner();
        SimulationAutoTestRunner.AutoTestReport report = runner.runAllTests();

        System.out.println(report.generateSummary());

        assertTrue(report.isAllPassed(), "All simulation scenario tests should pass. Summary: " + report.generateSummary());
        assertEquals(report.getTotalCount(), report.getPassedCount());
    }
}
