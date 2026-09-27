package org.swarmforge.core.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for instantiating SwarmForge simulation compute engines.
 *
 * Automatically detects hardware capabilities and native libraries, or selects
 * the desired engine via system property {@code -Dswarmforge.engine=auto|java|rust}
 * or environment variable {@code SWARMFORGE_ENGINE=auto|java|rust}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public final class SimulationEngineFactory {

    private static final Logger log = LoggerFactory.getLogger(SimulationEngineFactory.class);

    private SimulationEngineFactory() {
        // Static factory
    }

    /**
     * Creates a simulation engine according to environment configuration or best auto-detection.
     *
     * @return Fully operational {@link SimulationEngine} instance.
     */
    public static SimulationEngine createDefaultEngine() {
        String configuredEngine = System.getProperty("swarmforge.engine");
        if (configuredEngine == null || configuredEngine.isBlank()) {
            configuredEngine = System.getenv("SWARMFORGE_ENGINE");
        }
        if (configuredEngine == null || configuredEngine.isBlank()) {
            configuredEngine = "auto";
        }

        return createEngine(configuredEngine);
    }

    /**
     * Creates a simulation engine by name or strategy string:
     * <ul>
     *   <li>{@code "auto"} - Prefers Rust native if compiled binary is present, otherwise falls back to pure Java ECS.</li>
     *   <li>{@code "java"} / {@code "ecs"} - Pure Java 21 Artemis-odb ECS engine.</li>
     *   <li>{@code "rust"} / {@code "native"} - Hardware-accelerated Rust engine (with fallback if library missing).</li>
     * </ul>
     *
     * @param engineName Strategy name ("auto", "java", "rust").
     * @return Initialized engine instance.
     */
    public static SimulationEngine createEngine(String engineName) {
        String normalized = engineName != null ? engineName.trim().toLowerCase() : "auto";

        switch (normalized) {
            case "rust":
            case "native":
                log.info("Explicitly selecting Native Rust simulation engine.");
                return new RustNativeEngine();

            case "java":
            case "ecs":
            case "jvm":
                log.info("Explicitly selecting Pure Java 21 Artemis ECS simulation engine.");
                return new JavaEcsEngine();

            case "auto":
            default:
                if (RustNativeEngine.isNativeLibraryAvailable()) {
                    log.info("Auto-detection selected Native Rust SIMD Engine (libswarmforge_core_rust found).");
                    return new RustNativeEngine();
                } else {
                    log.info("Auto-detection selected Pure Java 21 Artemis ECS Engine.");
                    return new JavaEcsEngine();
                }
        }
    }

    /**
     * Creates an engine of the specified {@link SimulationEngineType}.
     *
     * @param type Target engine type enum.
     * @return Initialized engine instance.
     */
    public static SimulationEngine createEngine(SimulationEngineType type) {
        if (type == null) {
            return createDefaultEngine();
        }
        return switch (type) {
            case JAVA_ECS -> new JavaEcsEngine();
            case RUST_NATIVE -> new RustNativeEngine();
        };
    }
}
