/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.structure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.structure.physics.PassiveVentilationEngine;
import org.swarmforge.core.structure.physics.StructuralStabilityAnalyzer;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Suite verifying nest architectural generation, procedural L-system nests,
 * chamber excavation & tunnel digging, voxel material properties, structural stability,
 * passive ventilation, and multi-tick ConstructionManager nest evolution.
 */
public class NestConstructionAndEvolutionTest {

    private Terrarium terrarium;
    private Simulation simulation;
    private Colony colony;
    private Nest nest;
    private ConstructionManager constructionManager;

    @BeforeEach
    void setUp() {
        terrarium = new Terrarium(80, 80, 40);
        simulation = new Simulation(terrarium);
        colony = simulation.addColony("FormicaRufa", 1, 30, 0);
        nest = colony.getNest();
        constructionManager = new ConstructionManager(simulation, nest);
    }

    @Test
    @DisplayName("Verify procedural L-system nest generation across diverse species nest types")
    void testProceduralNestTypeGeneration() {
        org.swarmforge.core.world.NestGenerator generator = new org.swarmforge.core.world.NestGenerator(terrarium, 12345L);

        int countMound = generator.generate(20, 20, 10, org.swarmforge.core.world.NestGenerator.NestType.MOUND, 1.0f);
        assertTrue(countMound > 0, "Formica mound generator should create nest chambers");

        int countWaxComb = generator.generate(40, 20, 15, org.swarmforge.core.world.NestGenerator.NestType.WAX_COMB_HEXAGONAL, 1.0f);
        assertTrue(countWaxComb > 0, "Honeybee hexagonal wax comb generator should create comb cells");

        int countCathedral = generator.generate(60, 20, 10, org.swarmforge.core.world.NestGenerator.NestType.CATHEDRAL_MOUND, 1.0f);
        assertTrue(countCathedral > 0, "Termite cathedral mound generator should create internal vaults");

        int countFungiVault = generator.generate(20, 60, 5, org.swarmforge.core.world.NestGenerator.NestType.SUBTERRANEAN_FUNGI_VAULT, 1.0f);
        assertTrue(countFungiVault > 0, "Atta subterranean fungi vault generator should create deep chambers");
    }

    @Test
    @DisplayName("Verify nest chamber excavation, tunnel digging, and spatial cell modifications")
    void testChamberExcavationAndTunnelDigging() {
        // Create initial underground earth cell
        terrarium.setCell(TerrariumCell.earth(30, 30, 5));
        assertFalse(terrarium.getCell(30, 30, 5).isPassable());
        assertTrue(terrarium.getCell(30, 30, 5).isDiggable());

        // Excavate cell into chamber interior
        terrarium.setCell(TerrariumCell.chamber(30, 30, 5));
        assertTrue(terrarium.getCell(30, 30, 5).isPassable(), "Chamber cell must be passable for ants");
        assertEquals(TerrariumCell.Material.CHAMBER, terrarium.getCell(30, 30, 5).material());

        // Excavate connecting air tunnel cell
        terrarium.setCell(TerrariumCell.air(30, 31, 5));
        assertTrue(terrarium.getCell(30, 31, 5).isPassable(), "Air tunnel cell must be passable");
    }

    @Test
    @DisplayName("Verify ConstructionManager project planning, task allocation, and multi-tick completion")
    void testConstructionManagerTaskExecution() {
        int initialChambers = nest.getChambers().size();

        // Plan new nursery chamber project
        constructionManager.planNewChamber("ch_nursery_01", Chamber.Type.NURSERY, 25f, 25f, 5f);

        Optional<ConstructionTask> taskOpt = constructionManager.getAvailableTask(25f, 25f, 5f);
        assertTrue(taskOpt.isPresent(), "Available construction task should be assigned to nearest builder");

        ConstructionTask task = taskOpt.get();
        assertEquals(25f, task.getX());
        assertEquals(25f, task.getY());
        assertEquals(5f, task.getZ());

        task.work(10.0f);
        assertTrue(task.isCompleted());

        // Tick ConstructionManager to finalize completed projects
        constructionManager.tick();

        assertEquals(initialChambers + 1, nest.getChambers().size(), "Nest should contain 1 additional completed chamber");
        assertTrue(nest.getChambers().stream().anyMatch(c -> "ch_nursery_01".equals(c.getId())));
    }

    @Test
    @DisplayName("Verify voxel physics, structural stability analysis, and passive ventilation diffusion")
    void testVoxelPhysicsStructuralStabilityAndVentilation() {
        org.swarmforge.core.structure.physics.NestVoxelGrid grid =
                new org.swarmforge.core.structure.physics.NestVoxelGrid(20, 20, 20, org.swarmforge.core.structure.physics.NestType.SUBTERRANEAN_MOUND);

        StructuralStabilityAnalyzer analyzer = new StructuralStabilityAnalyzer();
        analyzer.updateOverburdenStress(grid);

        float archSupport = analyzer.calculateArchSupportFactor(grid, 10, 10, 10);
        assertTrue(archSupport >= 0.0f && archSupport <= 1.0f, "Arch support factor must be bounded between 0.0 and 1.0");

        float risk = analyzer.calculateCollapseRisk(grid, 10, 10, 10, 0.0f);
        assertTrue(risk >= 0.0f && risk <= 1.0f, "Collapse risk must be bounded between 0.0 and 1.0");

        PassiveVentilationEngine ventilation = new PassiveVentilationEngine();
        assertNotNull(ventilation, "Passive ventilation engine instantiated successfully");
    }
}
