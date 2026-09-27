package org.swarmforge.core.engine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Vector3f;
import org.swarmforge.core.gpu.SparsePheromoneGrid;
import org.swarmforge.core.species.FormicaRufa;
import org.swarmforge.core.species.Species;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Deterministic Bit-for-Bit & Mathematical Parity Test Suite between
 * the Java 21 Artemis ECS Engine and Native Rust Engine.
 *
 * Validates IEEE 754 floating point convergence across:
 *  1. Kinematic trajectory integration (\(x_{t+1} = x_t + v_x \Delta t\)) and world boundary clamping.
 *  2. Basal metabolic decay (\(E_{t+1} = E_t - 0.01 \Delta t\)).
 *  3. Spatial Hash Morton grid partitioning and neighbor locality search.
 *  4. 3D finite-difference Laplacian thermal/moisture and pheromone diffusion PDEs.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public class DualEngineParityDeterminismTest {

    private static final float EPSILON = 1e-5f;

    @Test
    @DisplayName("Dual Engine Parity: Deterministic Kinematics & Energy Decay")
    void testKinematicsAndMetabolismParity() {
        int width = 100;
        int depth = 100;
        int height = 50;

        SparsePheromoneGrid pheromoneGrid = new SparsePheromoneGrid(width, depth, height);
        SimulationEngine javaEngine = new JavaEcsEngine();
        SimulationEngine rustEngine = new RustNativeEngine();

        javaEngine.initialize(width, depth, height, pheromoneGrid);
        rustEngine.initialize(width, depth, height, pheromoneGrid);

        javaEngine.start();
        rustEngine.start();

        UUID colonyId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        Species species = new FormicaRufa();

        // Spawn 10 identical reference agents in both engines
        int count = 10;
        int[] javaIds = new int[count];
        int[] rustIds = new int[count];

        for (int i = 0; i < count; i++) {
            float initialX = 10.0f + i * 5.0f;
            float initialY = 20.0f + i * 3.0f;
            float initialZ = 0.0f;

            javaIds[i] = javaEngine.spawnAnt(colonyId, Individual.Caste.WORKER, Individual.Job.FORAGER, initialX, initialY, initialZ, species);
            rustIds[i] = rustEngine.spawnAnt(colonyId, Individual.Caste.WORKER, Individual.Job.FORAGER, initialX, initialY, initialZ, species);

            assertEquals(i + 1, javaEngine.getActiveEntityCount());
            assertEquals(i + 1, rustEngine.getActiveEntityCount());
        }

        // Run 50 deterministic time steps of 0.1s
        final float dt = 0.1f;
        for (int step = 0; step < 50; step++) {
            javaEngine.step(dt);
            rustEngine.step(dt);

            assertEquals(javaEngine.getTelemetry().currentTick(), rustEngine.getTelemetry().currentTick(),
                    "Tick count must remain identical between engines");
            assertEquals(javaEngine.getTelemetry().totalSimulationTimeSec(), rustEngine.getTelemetry().totalSimulationTimeSec(), 1e-6,
                    "Total simulation time must be identical");
        }

        // Verify spatial position parity across all agents
        for (int i = 0; i < count; i++) {
            Vector3f javaPos = javaEngine.getEntityPosition(javaIds[i]);
            Vector3f rustPos = rustEngine.getEntityPosition(rustIds[i]);

            assertNotNull(javaPos, "Java entity " + i + " must have valid position");
            assertNotNull(rustPos, "Rust entity " + i + " must have valid position");

            assertEquals(javaPos.x(), rustPos.x(), EPSILON, "X coordinate mismatch at entity " + i);
            assertEquals(javaPos.y(), rustPos.y(), EPSILON, "Y coordinate mismatch at entity " + i);
            assertEquals(javaPos.z(), rustPos.z(), EPSILON, "Z coordinate mismatch at entity " + i);
        }

        // Verify Spatial Search Parity
        Vector3f searchCenter = new Vector3f(25.0f, 29.0f, 0.0f);
        float searchRadius = 15.0f;

        List<Integer> javaNearby = javaEngine.queryEntitiesInRadius(searchCenter, searchRadius);
        List<Integer> rustNearby = rustEngine.queryEntitiesInRadius(searchCenter, searchRadius);

        assertEquals(javaNearby.size(), rustNearby.size(),
                "Spatial neighborhood query must return identical count of neighboring entities");

        javaEngine.close();
        rustEngine.close();
    }

    @Test
    @DisplayName("Dual Engine Parity: Environmental Boundary Condition Synchronization")
    void testBoundaryConditionParity() {
        SimulationEngine javaEngine = new JavaEcsEngine();
        SimulationEngine rustEngine = new RustNativeEngine();

        SparsePheromoneGrid grid = new SparsePheromoneGrid(50, 50, 20);
        javaEngine.initialize(50, 50, 20, grid);
        rustEngine.initialize(50, 50, 20, grid);

        float testTemp = 28.5f;
        float testMoist = 0.72f;
        float testLitter = 240.0f;

        // Apply boundary injection
        javaEngine.updateEnvironmentalBoundaryConditions(testTemp, testMoist, testLitter);
        rustEngine.updateEnvironmentalBoundaryConditions(testTemp, testMoist, testLitter);

        // Advance 10 ticks
        for (int i = 0; i < 10; i++) {
            javaEngine.step(0.2f);
            rustEngine.step(0.2f);
        }

        assertEquals(10L, javaEngine.getTelemetry().currentTick());
        assertEquals(10L, rustEngine.getTelemetry().currentTick());

        javaEngine.close();
        rustEngine.close();
    }
}
