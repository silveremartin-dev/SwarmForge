package org.swarmforge.core.engine;

/**
 * Immutable telemetry and performance snapshot from a SwarmForge simulation engine.
 *
 * @param engineType             The active engine implementation type.
 * @param currentTick            The current global simulation tick count.
 * @param totalSimulationTimeSec The accumulated physical simulation time in seconds.
 * @param activeEntities         Number of active live individual agents in the world.
 * @param lastTickDurationMs     Execution time of the last simulation tick in milliseconds.
 * @param averageTickDurationMs  Rolling average tick execution latency in milliseconds.
 * @param updatesPerSecond       Measured simulation ticks/updates per second throughput.
 * @param nativeMemoryBytes      Direct or off-heap memory occupied by spatial buffers/grids.
 * @param isRunning              Current execution state (running vs paused/stopped).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public record EngineTelemetry(
        SimulationEngineType engineType,
        long currentTick,
        double totalSimulationTimeSec,
        int activeEntities,
        double lastTickDurationMs,
        double averageTickDurationMs,
        double updatesPerSecond,
        long nativeMemoryBytes,
        boolean isRunning
) {
    public static EngineTelemetry empty(SimulationEngineType type) {
        return new EngineTelemetry(type, 0L, 0.0, 0, 0.0, 0.0, 0.0, 0L, false);
    }
}
