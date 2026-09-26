/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server.compute;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.gpu.SparsePheromoneGrid;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.spatial.BorderMigrationSystem;
import org.swarmforge.core.species.CustomSpecies;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-End Cluster Integration Test for 2-Node Megaterrarium Sharded Simulation.
 * Simulates concurrent execution across two worker nodes (West Node [0,0] & East Node [1,0])
 * with continuous entity migration and real-time pheromone boundary halo synchronization.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class MegaterrariumTwoNodeClusterTest {

    @Test
    @DisplayName("Verify 2-Node Megaterrarium Concurrent Simulation, Border Migration & Halo Sync")
    void testTwoNodeMegaterrariumCluster() throws Exception {
        int width = 100;
        int height = 100;
        int depth = 30;

        // 1. Initialize Node 0 (West Tile [0, 0])
        Terrarium terrariumNode0 = new Terrarium(width, height, depth);
        Simulation simNode0 = new Simulation(terrariumNode0);
        CustomSpecies speciesWest = new CustomSpecies();
        speciesWest.setScientificName("Lasius niger");
        Colony colonyWest = new Colony(speciesWest, 50f, 50f, 5f);
        simNode0.addColony(colonyWest);

        // 2. Initialize Node 1 (East Tile [1, 0])
        Terrarium terrariumNode1 = new Terrarium(width, height, depth);
        Simulation simNode1 = new Simulation(terrariumNode1);
        CustomSpecies speciesEast = new CustomSpecies();
        speciesEast.setScientificName("Formica rufa");
        Colony colonyEast = new Colony(speciesEast, 50f, 50f, 5f);
        simNode1.addColony(colonyEast);

        // 3. Populate each node with 500 individuals
        for (int i = 0; i < 500; i++) {
            Individual antW = new Individual(colonyWest.getId(), Individual.Caste.WORKER, 50f + (i % 20), 50f + (i % 20), 5f);
            colonyWest.addIndividual(antW);

            Individual antE = new Individual(colonyEast.getId(), Individual.Caste.WORKER, 50f + (i % 20), 50f + (i % 20), 5f);
            colonyEast.addIndividual(antE);
        }

        // Place a migrating ant on Node 0 heading East across border (X = 101.5f >= 100f)
        Individual migrantToEast = new Individual(colonyWest.getId(), Individual.Caste.WORKER, 101.5f, 50f, 5f);
        colonyWest.addIndividual(migrantToEast);

        // Place a migrating ant on Node 1 heading West across border (X = -1.2f < 0f)
        Individual migrantToWest = new Individual(colonyEast.getId(), Individual.Caste.WORKER, -1.2f, 50f, 5f);
        colonyEast.addIndividual(migrantToWest);

        // 4. Initialize Pheromone Grids & BoundaryHaloSync
        SparsePheromoneGrid pheroGrid0 = new SparsePheromoneGrid(width, height, depth);
        SparsePheromoneGrid pheroGrid1 = new SparsePheromoneGrid(width, height, depth);
        BoundaryHaloSync haloSync = new BoundaryHaloSync();
        BorderMigrationSystem migrationSystem = new BorderMigrationSystem();

        // Deposit pheromone on Node 0 East Border (X = 99, Y = 50, Z = 5)
        pheroGrid0.deposit(99, 50, 5, 0, 80.0f);

        // Deposit pheromone on Node 1 West Border (X = 0, Y = 50, Z = 5)
        pheroGrid1.deposit(0, 50, 5, 1, 60.0f);

        BorderMigrationSystem.TerrariumCoord coordNode0 = new BorderMigrationSystem.TerrariumCoord(0, 0);
        BorderMigrationSystem.TerrariumCoord coordNode1 = new BorderMigrationSystem.TerrariumCoord(1, 0);

        AtomicInteger migratedCount = new AtomicInteger(0);
        migrationSystem.addListener(payload -> migratedCount.incrementAndGet());

        // 5. Execute 2-Node Parallel Simulation Ticks
        ExecutorService clusterExecutor = Executors.newFixedThreadPool(2);
        int totalTicks = 20;

        for (int tick = 0; tick < totalTicks; tick++) {
            // A. Check and process Boundary Entity Migrations BEFORE/AFTER step
            List<BorderMigrationSystem.MigrationPayload> migFrom0 = migrationSystem.checkAndProcessBoundaryMigrations(simNode0, coordNode0);
            for (BorderMigrationSystem.MigrationPayload m : migFrom0) {
                if (m.targetCoord().gridX() == 1 && m.targetCoord().gridY() == 0) {
                    colonyWest.removeIndividual(m.individual());
                    colonyEast.addIndividual(m.individual());
                }
            }

            List<BorderMigrationSystem.MigrationPayload> migFrom1 = migrationSystem.checkAndProcessBoundaryMigrations(simNode1, coordNode1);
            for (BorderMigrationSystem.MigrationPayload m : migFrom1) {
                if (m.targetCoord().gridX() == 0 && m.targetCoord().gridY() == 0) {
                    colonyEast.removeIndividual(m.individual());
                    colonyWest.addIndividual(m.individual());
                }
            }

            // B. Concurrent Step on Node 0 & Node 1
            Future<?> future0 = clusterExecutor.submit(() -> simNode0.tick());
            Future<?> future1 = clusterExecutor.submit(() -> simNode1.tick());
            future0.get(5, TimeUnit.SECONDS);
            future1.get(5, TimeUnit.SECONDS);

            // C. Synchronize Boundary Pheromone Halos (East Border of Node 0 <-> West Border of Node 1)
            Map<Long, float[]> eastSlice0 = haloSync.extractBoundarySlice(pheroGrid0, width, height, BoundaryHaloSync.BoundaryDirection.EAST);
            Map<Long, float[]> westSlice1 = haloSync.extractBoundarySlice(pheroGrid1, width, height, BoundaryHaloSync.BoundaryDirection.WEST);

            haloSync.applyAndBlendBoundarySlice(pheroGrid1, eastSlice0, width, height, BoundaryHaloSync.BoundaryDirection.WEST, 0.5f);
            haloSync.applyAndBlendBoundarySlice(pheroGrid0, westSlice1, width, height, BoundaryHaloSync.BoundaryDirection.EAST, 0.5f);
        }

        clusterExecutor.shutdown();
        assertTrue(clusterExecutor.awaitTermination(5, TimeUnit.SECONDS));

        // 6. Assertions
        assertTrue(migratedCount.get() >= 2, "Both cross-border migrant ants should have been detected and transferred");
        assertTrue(migrantToEast.getX() >= 0.0f && migrantToEast.getX() <= 15.0f, "Migrant to East should now reside inside Node 1 West boundary area");
        assertTrue(migrantToWest.getX() >= 85.0f && migrantToWest.getX() <= 100.0f, "Migrant to West should now reside inside Node 0 East boundary area");

        // Verify Pheromone Halo Diffusion across the 2 nodes
        float[] blendedPheroOnNode1 = pheroGrid1.readAll(0, 50, 5);
        assertNotNull(blendedPheroOnNode1, "Node 1 should have received blended halo pheromones from Node 0");
        assertTrue(blendedPheroOnNode1[0] > 0.0f, "Channel 0 pheromone from Node 0 should be diffused into Node 1 border");

        float[] blendedPheroOnNode0 = pheroGrid0.readAll(99, 50, 5);
        assertNotNull(blendedPheroOnNode0, "Node 0 should have received blended halo pheromones from Node 1");
        assertTrue(blendedPheroOnNode0[1] > 0.0f, "Channel 1 pheromone from Node 1 should be diffused into Node 0 border");
    }
}
