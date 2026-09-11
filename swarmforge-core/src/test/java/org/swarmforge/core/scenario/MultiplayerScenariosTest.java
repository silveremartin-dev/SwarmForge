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
 * Test suite verifying multiplayer scenario configuration, validation guards, and sharded megaterrarium setup.
 */
public class MultiplayerScenariosTest {

    @Test
    @DisplayName("Verify Multiplayer Scenario Presets & Metadata")
    public void testMultiplayerScenariosCreation() {
        List<Scenario> mpScenarios = AcademicScenarios.getAllMultiplayerScenarios(42L);
        assertNotNull(mpScenarios);
        assertEquals(3, mpScenarios.size());

        for (Scenario sc : mpScenarios) {
            assertTrue(sc.isMultiplayerOnly(), "Scenario " + sc.getId() + " must be flagged as multiplayer-only!");
            assertTrue(sc.getRequiredPlayerCount() >= 2, "Multiplayer scenario must require at least 2 players!");
            assertNotNull(sc.getBiomeName());
            assertFalse(sc.getColonies().isEmpty());
        }

        // Test Scenario 1: 1v1 Battle Arena
        Scenario arena = mpScenarios.get(0);
        assertEquals("MP_01_BATTLE_ARENA_1V1", arena.getId());
        assertEquals(2, arena.getRequiredPlayerCount());
        assertEquals(2, arena.getGridTilesX());
        assertEquals(1, arena.getGridTilesY());
        assertEquals(2, arena.getColonies().size());
        assertTrue(arena.getTargetMetrics().contains("TERRITORIAL_CONTROL_PERCENT"));

        // Test Scenario 2: Coop Tribute & Trade
        Scenario trade = mpScenarios.get(1);
        assertEquals("MP_02_COOP_TRIBUTE_TRADE", trade.getId());
        assertEquals(2, trade.getRequiredPlayerCount());
        assertTrue(trade.getTargetMetrics().contains("TRIBUTE_TRANSFER_VOLUME_TOTAL"));

        // Test Scenario 3: 4-Node Megaterrarium
        Scenario megaterrarium = mpScenarios.get(2);
        assertEquals("MP_03_MEGATERRARIUM_4NODE_ALLIANCE", megaterrarium.getId());
        assertEquals(4, megaterrarium.getRequiredPlayerCount());
        assertEquals(2, megaterrarium.getGridTilesX());
        assertEquals(2, megaterrarium.getGridTilesY());
        assertEquals(4, megaterrarium.getColonies().size());
        assertTrue(megaterrarium.getTargetMetrics().contains("POLYCALIC_SUPERCOLONY_POPULATION"));
    }

    @Test
    @DisplayName("Verify Single-Player Execution Guard for Multiplayer-Only Scenarios")
    public void testMultiplayerOnlyExecutionGuard() {
        Scenario mpArena = AcademicScenarios.createMultiplayerBattleArena1v1(42L);
        assertTrue(mpArena.isMultiplayerOnly());

        // Attempting to run in solo / single-player must throw IllegalStateException
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            mpArena.validateExecutionEnvironment(false);
        });
        assertTrue(ex.getMessage().contains("exclusivement Multijoueur"));

        // Validating in multiplayer mode must succeed without exception
        assertDoesNotThrow(() -> {
            mpArena.validateExecutionEnvironment(true);
        });
    }

    @Test
    @DisplayName("Verify Single-Player Standard Scenarios Run In Solo Mode")
    public void testSoloScenariosValidation() {
        Scenario soloScenario = AcademicScenarios.createLevyVsBrownianScenario(42L);
        assertFalse(soloScenario.isMultiplayerOnly());

        // Solo scenario can run in both solo and multiplayer environments
        assertDoesNotThrow(() -> soloScenario.validateExecutionEnvironment(false));
        assertDoesNotThrow(() -> soloScenario.validateExecutionEnvironment(true));
    }
}