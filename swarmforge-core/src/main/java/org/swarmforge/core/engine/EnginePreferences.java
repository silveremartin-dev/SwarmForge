package org.swarmforge.core.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.prefs.Preferences;

/**
 * Manages user preferences, CLI arguments, and persistent configuration for simulation engines,
 * concurrency models (single-core vs multi-core), and hardware acceleration backends.
 *
 * Defaults automatically to the highest performance configuration available:
 *  - Engine Backend: {@link SimulationEngineType#JAVA_ECS} (or auto-detected Rust SIMD)
 *  - Acceleration: {@link ComputeAccelerationMode#AUTO}
 *  - Concurrency: Available processors (or single-core if requested via CLI/property)
 *
 * CLI and system properties override persistent preferences:
 *  - {@code --engine=<auto|java|rust>} / {@code -Dswarmforge.engine=...} / {@code SWARMFORGE_ENGINE=...}
 *  - {@code --accel=<auto|gpu|cpu>} / {@code -Dswarmforge.compute.acceleration=...}
 *  - {@code --threads=<N>} / {@code --single-core} / {@code --multi-core} / {@code -Dswarmforge.threads=...}
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public final class EnginePreferences {

    private static final Logger log = LoggerFactory.getLogger(EnginePreferences.class);
    private static final Preferences PREFS = Preferences.userNodeForPackage(EnginePreferences.class);

    private static final String KEY_ENGINE_BACKEND = "engine.backend.strategy";
    private static final String KEY_COMPUTE_ACCELERATION = "compute.acceleration.mode";
    private static final String KEY_THREAD_COUNT = "engine.thread.count";

    private static SimulationEngineType selectedEngineType;
    private static ComputeAccelerationMode selectedAccelerationMode;
    private static int threadCount = Runtime.getRuntime().availableProcessors();

    static {
        loadPreferences();
    }

    private EnginePreferences() {
        // Static preferences manager
    }

    public static synchronized void loadPreferences() {
        String sysEngine = System.getProperty("swarmforge.engine");
        if (sysEngine == null || sysEngine.isBlank()) {
            sysEngine = System.getenv("SWARMFORGE_ENGINE");
        }

        String savedEngine = sysEngine != null ? sysEngine : PREFS.get(KEY_ENGINE_BACKEND, "AUTO");
        try {
            if ("AUTO".equalsIgnoreCase(savedEngine)) {
                selectedEngineType = RustNativeEngine.isNativeLibraryAvailable()
                        ? SimulationEngineType.RUST_NATIVE
                        : SimulationEngineType.JAVA_ECS;
            } else {
                selectedEngineType = SimulationEngineType.valueOf(savedEngine.toUpperCase());
            }
        } catch (Exception e) {
            selectedEngineType = SimulationEngineType.JAVA_ECS;
        }

        String sysAccel = System.getProperty("swarmforge.compute.acceleration");
        if (sysAccel == null || sysAccel.isBlank()) {
            sysAccel = System.getenv("SWARMFORGE_ACCEL");
        }
        String savedAccel = sysAccel != null ? sysAccel : PREFS.get(KEY_COMPUTE_ACCELERATION, "AUTO");
        try {
            selectedAccelerationMode = ComputeAccelerationMode.valueOf(savedAccel.toUpperCase());
        } catch (Exception e) {
            selectedAccelerationMode = ComputeAccelerationMode.AUTO;
        }

        String sysThreads = System.getProperty("swarmforge.threads");
        if (sysThreads != null && !sysThreads.isBlank()) {
            try {
                threadCount = Math.max(1, Integer.parseInt(sysThreads.trim()));
            } catch (NumberFormatException ignored) {}
        }

        log.info("EnginePreferences initialized: Backend={}, ComputeAcceleration={}, Threads={}",
                selectedEngineType, selectedAccelerationMode, threadCount);
    }

    /**
     * Parses standard SwarmForge command-line arguments and applies them to active engine settings.
     *
     * @param args Command-line arguments.
     * @return true if arguments were processed successfully.
     */
    public static synchronized boolean applyCommandLineArgs(String[] args) {
        if (args == null || args.length == 0) {
            return false;
        }

        for (int i = 0; i < args.length; i++) {
            String arg = args[i].trim();

            if (arg.startsWith("--engine=")) {
                String val = arg.substring("--engine=".length());
                applyEngineString(val);
            } else if ("--engine".equalsIgnoreCase(arg) && i + 1 < args.length) {
                applyEngineString(args[++i]);
            } else if ("--rust".equalsIgnoreCase(arg)) {
                setSelectedEngineType(SimulationEngineType.RUST_NATIVE);
            } else if ("--java".equalsIgnoreCase(arg)) {
                setSelectedEngineType(SimulationEngineType.JAVA_ECS);
            } else if (arg.startsWith("--accel=")) {
                String val = arg.substring("--accel=".length());
                applyAccelerationString(val);
            } else if ("--accel".equalsIgnoreCase(arg) && i + 1 < args.length) {
                applyAccelerationString(args[++i]);
            } else if ("--gpu".equalsIgnoreCase(arg)) {
                setSelectedAccelerationMode(ComputeAccelerationMode.GPU_ACCELERATED);
            } else if ("--cpu".equalsIgnoreCase(arg)) {
                setSelectedAccelerationMode(ComputeAccelerationMode.CPU_MULTITHREADED_SIMD);
            } else if (arg.startsWith("--threads=")) {
                try {
                    int t = Integer.parseInt(arg.substring("--threads=".length()));
                    setThreadCount(t);
                } catch (NumberFormatException ignored) {}
            } else if ("--threads".equalsIgnoreCase(arg) && i + 1 < args.length) {
                try {
                    int t = Integer.parseInt(args[++i]);
                    setThreadCount(t);
                } catch (NumberFormatException ignored) {}
            } else if ("--single-core".equalsIgnoreCase(arg) || "--monocoeur".equalsIgnoreCase(arg) || "--monocoeur".equalsIgnoreCase(arg)) {
                setThreadCount(1);
            } else if ("--multi-core".equalsIgnoreCase(arg) || "--multicoeur".equalsIgnoreCase(arg)) {
                setThreadCount(Runtime.getRuntime().availableProcessors());
            }
        }
        return true;
    }

    private static void applyEngineString(String val) {
        if ("rust".equalsIgnoreCase(val) || "native".equalsIgnoreCase(val)) {
            setSelectedEngineType(SimulationEngineType.RUST_NATIVE);
            System.setProperty("swarmforge.engine", "rust");
        } else if ("java".equalsIgnoreCase(val) || "ecs".equalsIgnoreCase(val)) {
            setSelectedEngineType(SimulationEngineType.JAVA_ECS);
            System.setProperty("swarmforge.engine", "java");
        } else {
            resetToDefaults();
            System.setProperty("swarmforge.engine", "auto");
        }
    }

    private static void applyAccelerationString(String val) {
        if ("gpu".equalsIgnoreCase(val) || "gpu_accelerated".equalsIgnoreCase(val)) {
            setSelectedAccelerationMode(ComputeAccelerationMode.GPU_ACCELERATED);
            System.setProperty("swarmforge.compute.acceleration", "gpu");
        } else if ("cpu".equalsIgnoreCase(val) || "cpu_multithreaded_simd".equalsIgnoreCase(val)) {
            setSelectedAccelerationMode(ComputeAccelerationMode.CPU_MULTITHREADED_SIMD);
            System.setProperty("swarmforge.compute.acceleration", "cpu");
        } else {
            setSelectedAccelerationMode(ComputeAccelerationMode.AUTO);
            System.setProperty("swarmforge.compute.acceleration", "auto");
        }
    }

    public static synchronized SimulationEngineType getSelectedEngineType() {
        if (selectedEngineType == null) {
            loadPreferences();
        }
        return selectedEngineType;
    }

    public static synchronized void setSelectedEngineType(SimulationEngineType engineType) {
        selectedEngineType = engineType != null ? engineType : SimulationEngineType.JAVA_ECS;
        PREFS.put(KEY_ENGINE_BACKEND, selectedEngineType.name());
        System.setProperty("swarmforge.engine", selectedEngineType.name().toLowerCase());
        log.info("Engine backend preference updated to: {}", selectedEngineType);
    }

    public static synchronized ComputeAccelerationMode getSelectedAccelerationMode() {
        if (selectedAccelerationMode == null) {
            loadPreferences();
        }
        return selectedAccelerationMode;
    }

    public static synchronized void setSelectedAccelerationMode(ComputeAccelerationMode mode) {
        selectedAccelerationMode = mode != null ? mode : ComputeAccelerationMode.AUTO;
        PREFS.put(KEY_COMPUTE_ACCELERATION, selectedAccelerationMode.name());
        System.setProperty("swarmforge.compute.acceleration", selectedAccelerationMode.name().toLowerCase());
        log.info("Compute acceleration preference updated to: {}", selectedAccelerationMode);
    }

    public static synchronized int getThreadCount() {
        return threadCount;
    }

    public static synchronized void setThreadCount(int count) {
        threadCount = Math.max(1, count);
        System.setProperty("swarmforge.threads", String.valueOf(threadCount));
        PREFS.putInt(KEY_THREAD_COUNT, threadCount);
        log.info("Simulation thread count configured to: {}", threadCount);
    }

    public static synchronized boolean isSingleCore() {
        return threadCount == 1;
    }

    public static synchronized void resetToDefaults() {
        selectedEngineType = RustNativeEngine.isNativeLibraryAvailable()
                ? SimulationEngineType.RUST_NATIVE
                : SimulationEngineType.JAVA_ECS;
        selectedAccelerationMode = ComputeAccelerationMode.AUTO;
        threadCount = Runtime.getRuntime().availableProcessors();
        PREFS.put(KEY_ENGINE_BACKEND, "AUTO");
        PREFS.put(KEY_COMPUTE_ACCELERATION, "AUTO");
        PREFS.putInt(KEY_THREAD_COUNT, threadCount);
        log.info("EnginePreferences reset to optimal auto-detected defaults.");
    }
}
