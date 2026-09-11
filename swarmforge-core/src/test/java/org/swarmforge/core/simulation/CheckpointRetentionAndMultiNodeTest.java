/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.spatial.BorderMigrationSystem;
import org.swarmforge.core.species.CustomSpecies;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Checkpoint Retention Rotation & Multi-Terrarium Border Migration.
 */
class CheckpointRetentionAndMultiNodeTest {

    @Test
    @DisplayName("Verify Checkpoint Retention FIFO Pruning")
    void testFifoPruning(@TempDir Path tempDir) throws IOException {
        CheckpointRetentionManager manager = new CheckpointRetentionManager(tempDir);
        manager.setMaxAutoCheckpoints(3);

        Path simDir = tempDir.resolve("sim_main");
        Files.createDirectories(simDir);

        // Create 5 dummy auto-checkpoints with staged timestamps
        for (int i = 1; i <= 5; i++) {
            Path file = simDir.resolve(String.format("auto_checkpoint_tick_%08d_%d.sfcp.gz", i * 100, i * 1000L));
            Files.write(file, new byte[100]);
            file.toFile().setLastModified(System.currentTimeMillis() + i * 1000L);
        }

        assertEquals(5, simDir.toFile().listFiles().length);
        manager.pruneOldCheckpoints(simDir);

        File[] remaining = simDir.toFile().listFiles((d, n) -> n.startsWith("auto_checkpoint_"));
        assertNotNull(remaining);
        assertEquals(3, remaining.length, "Should keep at most 3 auto-checkpoints according to retention policy");
    }

    @Test
    @DisplayName("Verify Border Migration Cross-Terrarium Handoff")
    void testBorderMigration() {
        Terrarium terrarium = new Terrarium(100, 100, 30);
        Simulation simulation = new Simulation(terrarium);

        CustomSpecies species = new CustomSpecies();
        species.setScientificName("Formica rufa");
        Colony colony = new Colony(species, 50f, 50f, 5f);
        simulation.addColony(colony);

        // Place ant exceeding East boundary (X = 102.0 >= 100.0)
        Individual ant = new Individual(colony.getId(), Individual.Caste.WORKER, 102.0f, 50.0f, 5.0f);
        colony.addIndividual(ant);

        BorderMigrationSystem migrationSystem = new BorderMigrationSystem();
        BorderMigrationSystem.TerrariumCoord currentCoord = new BorderMigrationSystem.TerrariumCoord(0, 0);

        List<BorderMigrationSystem.MigrationPayload> migrations = migrationSystem.checkAndProcessBoundaryMigrations(simulation, currentCoord);

        assertEquals(1, migrations.size(), "Should detect 1 cross-border migration event");
        BorderMigrationSystem.MigrationPayload payload = migrations.get(0);
        assertEquals(1, payload.targetCoord().gridX(), "Target grid X should be +1 (East adjacent terrarium)");
        assertEquals(0, payload.targetCoord().gridY(), "Target grid Y should remain 0");
        assertEquals(0.5f, ant.getX(), 0.01f, "Ant X should be translated to West boundary (0.5f) of adjacent tile");
    }
}
