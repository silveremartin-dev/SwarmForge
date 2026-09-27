package org.swarmforge.core.engine;

/**
 * Enumeration of available SwarmForge simulation compute engines.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public enum SimulationEngineType {
    /**
     * High-throughput pure Java 21 Artemis-odb ECS engine with Morton3D spatial hashing.
     * Guaranteed 100% portable across all operating systems and JVM environments.
     */
    JAVA_ECS("Pure Java 21 Artemis ECS", "org.swarmforge.core.engine.JavaEcsEngine", true),

    /**
     * Ultra-low-latency Native Rust Engine executing via Java 21 Project Panama (Foreign Function & Memory API).
     * Provides SIMD-vectorized entity processing, zero GC pressure, and direct memory mapping.
     */
    RUST_NATIVE("Native Rust SIMD Engine (Panama FFM)", "org.swarmforge.core.engine.RustNativeEngine", false);

    private final String displayName;
    private final String implementationClass;
    private final boolean defaultEngine;

    SimulationEngineType(String displayName, String implementationClass, boolean defaultEngine) {
        this.displayName = displayName;
        this.implementationClass = implementationClass;
        this.defaultEngine = defaultEngine;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getImplementationClass() {
        return implementationClass;
    }

    public boolean isDefaultEngine() {
        return defaultEngine;
    }
}
