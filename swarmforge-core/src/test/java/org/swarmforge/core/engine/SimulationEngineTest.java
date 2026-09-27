package org.swarmforge.core.engine;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
 * Rigorous validation test suite for SwarmForge Dual-Engine SPI implementations
 * (Java Artemis-odb ECS Engine, Rust Native Engine fallback, and SimulationEngineFactory).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
class SimulationEngineTest {

    private SimulationEngine javaEngine;
    private SimulationEngine rustEngine;
    private SparsePheromoneGrid pheromoneGrid;

    @BeforeEach
    void setUp() {
        pheromoneGrid = new SparsePheromoneGrid(100, 100, 50);
        javaEngine = SimulationEngineFactory.createEngine(SimulationEngineType.JAVA_ECS);
        rustEngine = SimulationEngineFactory.createEngine(SimulationEngineType.RUST_NATIVE);

        javaEngine.initialize(100, 100, 50, pheromoneGrid);
        rustEngine.initialize(100, 100, 50, pheromoneGrid);
    }

    @AfterEach
    void tearDown() {
        if (javaEngine != null) javaEngine.close();
        if (rustEngine != null) rustEngine.close();
    }

    @Test
    @DisplayName("Java ECS Engine: Lifecycle, Individual Spawning, and Step Simulation")
    void testJavaEcsEngineLifecycleAndStepping() {
        assertTrue(javaEngine.isOperational(), "JavaEcsEngine should be operational after init");
        assertEquals(SimulationEngineType.JAVA_ECS, javaEngine.getEngineType());

        javaEngine.start();

        UUID colonyId = UUID.randomUUID();
        Species species = new FormicaRufa();

        int id1 = javaEngine.spawnAnt(colonyId, Individual.Caste.WORKER, Individual.Job.FORAGER, 10.0f, 15.0f, 0.0f, species);
        int id2 = javaEngine.spawnAnt(colonyId, Individual.Caste.SOLDIER, Individual.Job.GUARD, 12.0f, 15.0f, 0.0f, species);

        assertTrue(id1 >= 0, "Spawned entity 1 must have valid ID");
        assertTrue(id2 >= 0, "Spawned entity 2 must have valid ID");
        assertEquals(2, javaEngine.getActiveEntityCount(), "Active entity count must match spawned entities");

        // Advance 10 ticks
        for (int i = 0; i < 10; i++) {
            javaEngine.step(0.1f);
        }

        EngineTelemetry telemetry = javaEngine.getTelemetry();
        assertEquals(10L, telemetry.currentTick());
        assertEquals(1.0, telemetry.totalSimulationTimeSec(), 0.001);
        assertTrue(telemetry.averageTickDurationMs() >= 0.0);
        assertEquals(2, telemetry.activeEntities());

        // Test spatial radius query (interaction radius 5m around (10, 15, 0))
        List<Integer> nearby = javaEngine.queryEntitiesInRadius(new Vector3f(10.0f, 15.0f, 0.0f), 5.0f);
        assertNotNull(nearby);
        assertTrue(nearby.contains(id1), "Spatial query should locate agent 1");
        assertTrue(nearby.contains(id2), "Spatial query should locate agent 2 within 5m");

        // Test despawn
        boolean despawned = javaEngine.despawnEntity(id1);
        assertTrue(despawned);
        javaEngine.step(0.1f);
        assertEquals(1, javaEngine.getActiveEntityCount());
    }

    @Test
    @DisplayName("Rust Native Engine: Seamless Fallback & Contract Verification")
    void testRustNativeEngineFallbackAndContract() {
        assertTrue(rustEngine.isOperational(), "Rust Native Engine must be operational");
        assertEquals(SimulationEngineType.RUST_NATIVE, rustEngine.getEngineType());

        rustEngine.start();

        UUID colonyId = UUID.randomUUID();
        Species species = new FormicaRufa();

        int id = rustEngine.spawnAnt(colonyId, Individual.Caste.QUEEN, Individual.Job.IDLE, 25.0f, 30.0f, 0.0f, species);
        assertTrue(id >= 0);
        assertEquals(1, rustEngine.getActiveEntityCount());

        rustEngine.step(0.05f);

        EngineTelemetry telemetry = rustEngine.getTelemetry();
        assertEquals(1L, telemetry.currentTick());
        assertEquals(0.05, telemetry.totalSimulationTimeSec(), 0.001);

        rustEngine.updateEnvironmentalBoundaryConditions(24.5f, 0.65f, 120.0f);
        rustEngine.pause();
    }

    @Test
    @DisplayName("SimulationEngineFactory: Default & Strategy Resolution")
    void testEngineFactoryStrategies() {
        SimulationEngine defaultEngine = SimulationEngineFactory.createDefaultEngine();
        assertNotNull(defaultEngine);
        assertTrue(defaultEngine.getEngineType() == SimulationEngineType.JAVA_ECS ||
                   defaultEngine.getEngineType() == SimulationEngineType.RUST_NATIVE);

        SimulationEngine forcedJava = SimulationEngineFactory.createEngine("java");
        assertEquals(SimulationEngineType.JAVA_ECS, forcedJava.getEngineType());

        SimulationEngine forcedRust = SimulationEngineFactory.createEngine("rust");
        assertEquals(SimulationEngineType.RUST_NATIVE, forcedRust.getEngineType());
    }
}
