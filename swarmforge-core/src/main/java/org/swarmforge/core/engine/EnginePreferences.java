package org.swarmforge.core.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.prefs.Preferences;

/**
 * Manages user preferences and persistent configuration for simulation engines
 * and hardware acceleration backends.
 *
 * Defaults automatically to the highest performance configuration available:
 *  - Engine Backend: {@link SimulationEngineType#JAVA_ECS} (or auto-detected Rust SIMD)
 *  - Acceleration: {@link ComputeAccelerationMode#AUTO}
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public final class EnginePreferences {

    private static final Logger log = LoggerFactory.getLogger(EnginePreferences.class);
    private static final Preferences PREFS = Preferences.userNodeForPackage(EnginePreferences.class);

    private static final String KEY_ENGINE_BACKEND = "engine.backend.strategy";
    private static final String KEY_COMPUTE_ACCELERATION = "compute.acceleration.mode";

    private static SimulationEngineType selectedEngineType;
    private static ComputeAccelerationMode selectedAccelerationMode;

    static {
        loadPreferences();
    }

    private EnginePreferences() {
        // Static preferences manager
    }

    public static synchronized void loadPreferences() {
        String savedEngine = PREFS.get(KEY_ENGINE_BACKEND, "AUTO");
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

        String savedAccel = PREFS.get(KEY_COMPUTE_ACCELERATION, "AUTO");
        try {
            selectedAccelerationMode = ComputeAccelerationMode.valueOf(savedAccel.toUpperCase());
        } catch (Exception e) {
            selectedAccelerationMode = ComputeAccelerationMode.AUTO;
        }

        log.info("EnginePreferences loaded: Backend={}, ComputeAcceleration={}",
                selectedEngineType, selectedAccelerationMode);
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
        log.info("Compute acceleration preference updated to: {}", selectedAccelerationMode);
    }

    public static synchronized void resetToDefaults() {
        selectedEngineType = RustNativeEngine.isNativeLibraryAvailable()
                ? SimulationEngineType.RUST_NATIVE
                : SimulationEngineType.JAVA_ECS;
        selectedAccelerationMode = ComputeAccelerationMode.AUTO;
        PREFS.put(KEY_ENGINE_BACKEND, "AUTO");
        PREFS.put(KEY_COMPUTE_ACCELERATION, "AUTO");
        log.info("EnginePreferences reset to optimal auto-detected defaults.");
    }
}
