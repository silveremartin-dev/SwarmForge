package org.swarmforge.core.engine;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Vector3f;
import org.swarmforge.core.gpu.SparsePheromoneGrid;
import org.swarmforge.core.species.Species;

import java.util.List;
import java.util.UUID;

/**
 * High-performance Simulation Engine Service Provider Interface (SPI).
 * 
 * Defines the contract for SwarmForge simulation compute backends, enabling transparent
 * switching between the Pure Java 21 Artemis-odb ECS engine and native hardware-accelerated
 * engines (such as Rust SIMD via Project Panama).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public interface SimulationEngine extends AutoCloseable {

    /**
     * Initializes the simulation engine with environmental boundaries and spatial domain.
     *
     * @param worldWidthMeters  World size along X axis (in meters).
     * @param worldDepthMeters  World size along Y axis (in meters).
     * @param worldHeightMeters World size along Z axis (in meters).
     * @param pheromoneGrid     Sparse pheromone grid instance for chemical signaling.
     */
    void initialize(int worldWidthMeters, int worldDepthMeters, int worldHeightMeters, SparsePheromoneGrid pheromoneGrid);

    /**
     * Starts or resumes continuous simulation execution.
     */
    void start();

    /**
     * Pauses the simulation execution loop.
     */
    void pause();

    /**
     * Stops the simulation and flushes pending entity states.
     */
    void stop();

    /**
     * Resets the entire world state and clears all entities and pheromones.
     */
    void reset();

    /**
     * Advances the simulation by one discrete time step.
     *
     * @param deltaSeconds Physical elapsed time for this tick (e.g. 0.1s for 10Hz).
     */
    void step(float deltaSeconds);

    /**
     * Spawns an individual ant entity into the simulation world.
     *
     * @param colonyId Colony unique identifier.
     * @param caste    Biological caste (QUEEN, WORKER, SOLDIER, MALE).
     * @param job      Functional ethological role (FORAGER, NURSE, GUARD, etc.).
     * @param x        Initial X coordinate in meters.
     * @param y        Initial Y coordinate in meters.
     * @param z        Initial Z coordinate in meters.
     * @param species  Taxonomic species definition.
     * @return Unique numerical entity ID within the engine backend.
     */
    int spawnAnt(UUID colonyId, Individual.Caste caste, Individual.Job job,
                 float x, float y, float z, Species species);

    /**
     * Spawns an individual into the engine from an existing Individual domain model.
     *
     * @param individual Domain individual instance.
     * @return Numerical entity ID.
     */
    int spawnIndividual(Individual individual);

    /**
     * Removes an entity from the simulation.
     *
     * @param entityId Numerical identifier of the entity to despawn.
     * @return true if entity was found and removed, false otherwise.
     */
    boolean despawnEntity(int entityId);

    /**
     * Returns the total count of currently active entities.
     *
     * @return Number of live agents in the simulation world.
     */
    int getActiveEntityCount();

    /**
     * Queries all live entity IDs located within a spherical radius around a 3D coordinate.
     *
     * @param center 3D center of the search sphere.
     * @param radius Radius in meters.
     * @return List of entity IDs located in the sphere.
     */
    List<Integer> queryEntitiesInRadius(Vector3f center, float radius);

    /**
     * Returns the 3D position of an entity.
     *
     * @param entityId Numerical entity ID.
     * @return Position vector, or null if entity is invalid/dead.
     */
    Vector3f getEntityPosition(int entityId);

    /**
     * Propagates real-time environmental boundary variables (surface temperature, soil moisture ratio, leaf litter biomass)
     * directly into the physical subterranean solvers.
     *
     * @param surfaceTemp          Ambient surface temperature in °C.
     * @param surfaceMoistureRatio Volumetric water content ratio [0.0 - 1.0].
     * @param leafLitterBiomass    Biomass in g/m².
     */
    void updateEnvironmentalBoundaryConditions(float surfaceTemp, float surfaceMoistureRatio, float leafLitterBiomass);

    /**
     * Returns the underlying engine implementation type.
     *
     * @return SimulationEngineType (JAVA_ECS or RUST_NATIVE).
     */
    SimulationEngineType getEngineType();

    /**
     * Captures and returns an instantaneous telemetry snapshot of engine health and throughput.
     *
     * @return EngineTelemetry record.
     */
    EngineTelemetry getTelemetry();

    /**
     * Checks if the engine is currently initialized and operational.
     *
     * @return true if active and ready to process ticks.
     */
    boolean isOperational();

    @Override
    default void close() {
        stop();
    }
}
