package org.swarmforge.core.engine;

/**
 * Hardware compute acceleration strategy for intensive physical solvers
 * (3D pheromone diffusion, subterranean hydrology, allometric biomechanics).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public enum ComputeAccelerationMode {
    /**
     * Automatically detect the most efficient hardware available:
     * Prefers GPU (OpenCL / TornadoVM / WebGPU) when a compatible device is detected,
     * otherwise falls back seamlessly to multi-threaded CPU SIMD vector calculations.
     */
    AUTO("Auto-Detect (Optimal: GPU if present, CPU SIMD fallback)", true),

    /**
     * Force GPU compute acceleration via OpenCL / TornadoVM / WebGPU compute pipelines.
     */
    GPU_ACCELERATED("GPU Hardware Acceleration (OpenCL / TornadoVM / WebGPU)", false),

    /**
     * Force pure CPU multithreaded execution using Java 21 Vector API (AVX-512 / NEON)
     * and parallel worker thread pools.
     */
    CPU_MULTITHREADED_SIMD("Pure CPU Multithreaded (SIMD Vector API)", false);

    private final String displayName;
    private final boolean defaultMode;

    ComputeAccelerationMode(String displayName, boolean defaultMode) {
        this.displayName = displayName;
        this.defaultMode = defaultMode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isDefaultMode() {
        return defaultMode;
    }
}
