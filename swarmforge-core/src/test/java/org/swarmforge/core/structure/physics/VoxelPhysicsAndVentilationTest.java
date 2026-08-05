/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.structure.physics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class VoxelPhysicsAndVentilationTest {

    @Test
    @DisplayName("Test 13 Nest Typologies Parameters")
    void testNestTypologies() {
        assertEquals(13, NestType.values().length);

        NestType tower = NestType.TOWER_CHIMNEY;
        assertTrue(tower.getChimneyDraftMultiplier() > 1.0f);

        NestType mound = NestType.SUBTERRANEAN_MOUND;
        assertTrue(mound.getThermalInsulation() > 1.0f);
    }

    @Test
    @DisplayName("Test Overburden Stress & Structural Stability Collapse")
    void testStructuralStability() {
        NestVoxelGrid grid = new NestVoxelGrid(10, 10, 10, NestType.SUBTERRANEAN_SIMPLE);
        StructuralStabilityAnalyzer analyzer = new StructuralStabilityAnalyzer();

        analyzer.updateOverburdenStress(grid);
        NestVoxelGrid.VoxelCell bottomCell = grid.getVoxel(5, 0, 5);
        assertNotNull(bottomCell);
        assertTrue(bottomCell.getOverburdenStressKPa() > 0.0f);

        // Create an unstable wide tunnel under heavy sand
        grid.setMaterial(5, 5, 5, VoxelMaterial.SAND);
        grid.setMaterial(5, 4, 5, VoxelMaterial.AIR);

        float risk = analyzer.calculateCollapseRisk(grid, 5, 4, 5, 0.8f);
        assertTrue(risk >= 0.0f);

        Random rng = new Random(42);
        List<StructuralStabilityAnalyzer.CaveInEvent> caveIns = analyzer.evaluateAndTriggerCaveIns(grid, 0.9f, rng);
        assertNotNull(caveIns);
    }

    @Test
    @DisplayName("Test Chimney Effect Stack Draft & CO2 Ventilation")
    void testPassiveVentilation() {
        NestVoxelGrid grid = new NestVoxelGrid(8, 12, 8, NestType.TOWER_CHIMNEY);
        PassiveVentilationEngine engine = new PassiveVentilationEngine();

        // Set warm interior temperature and high CO2 build up
        for (int x = 2; x <= 5; x++) {
            for (int y = 2; y <= 8; y++) {
                grid.setMaterial(x, y, 4, VoxelMaterial.AIR);
                NestVoxelGrid.VoxelCell cell = grid.getVoxel(x, y, 4);
                cell.setTemperatureC(28.0f);
                cell.setCo2Ppm(2500.0f);
            }
        }

        float draftVelocity = engine.calculateStackEffectAirflow(grid, 18.0f, 2.0f);
        assertTrue(draftVelocity > 0.0f);

        PassiveVentilationEngine.NestAtmosphereState state = engine.simulateVentilationStep(
                grid, 18.0f, 400.0f, 2.0f, 50
        );

        assertNotNull(state);
        assertTrue(state.totalAirflowDraftMPerSec() > 0.0f);
        // CO2 should be purged towards external ambient (400 ppm)
        assertTrue(state.meanNestCo2Ppm() < 2500.0f);
    }
}
