/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior;

import org.swarmforge.core.behavior.json.JsonBrainArchitecture;
import org.swarmforge.core.behavior.plugin.JavaBrainClassLoader;
import org.swarmforge.core.behavior.rl.OnnxBrainArchitecture;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global Thread-Safe Cognitive Architecture and Brain Plugin Registry.
 * Coordinates built-in reasoning engines, imported ONNX neural models, dynamic Java bytecode plugins,
 * and declarative .sfbrain / JSON behavior trees.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public final class BrainPluginRegistry {

    private static final Logger LOG = Logger.getLogger(BrainPluginRegistry.class.getName());
    private static final BrainPluginRegistry INSTANCE = new BrainPluginRegistry();

    public interface RegistryListener {
        void onBrainRegistered(CustomBrainDescriptor descriptor);
        void onBrainUnregistered(String brainId);
    }

    private final Map<String, CustomBrainDescriptor> registeredBrains = new ConcurrentHashMap<>();
    private final List<RegistryListener> listeners = new CopyOnWriteArrayList<>();

    private BrainPluginRegistry() {
        registerBuiltInArchitectures();
        scanDefaultDirectories();
    }

    public static BrainPluginRegistry getInstance() {
        return INSTANCE;
    }

    public void addListener(RegistryListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(RegistryListener listener) {
        listeners.remove(listener);
    }

    private void registerBuiltInArchitectures() {
        registerBrain(new CustomBrainDescriptor(
                "BEHAVIOR_TREE",
                "Behavior Tree (Hierarchical Selector/Sequence)",
                "Standard modular behavior tree with reactive node evaluation and state execution.",
                CustomBrainDescriptor.BrainSourceType.BUILTIN,
                "",
                "SwarmForge Core Team",
                "2.0.0",
                Map.of("category", "Classical AI", "deterministic", "true"),
                BehaviorTreeArchitecture::new
        ));

        registerBrain(new CustomBrainDescriptor(
                "FINITE_STATE_MACHINE",
                "Finite State Machine (FSM)",
                "Fast state machine with discrete transition matrices and biological triggers.",
                CustomBrainDescriptor.BrainSourceType.BUILTIN,
                "",
                "SwarmForge Core Team",
                "2.0.0",
                Map.of("category", "Classical AI", "deterministic", "true"),
                FSMArchitecture::new
        ));

        registerBrain(new CustomBrainDescriptor(
                "BDI",
                "BDI (Belief-Desire-Intention)",
                "Cognitive architecture driven by internal mental states, desires, and tactical intentions.",
                CustomBrainDescriptor.BrainSourceType.BUILTIN,
                "",
                "SwarmForge Core Team",
                "2.0.0",
                Map.of("category", "Cognitive Agent", "deterministic", "true"),
                BDIArchitecture::new
        ));

        registerBrain(new CustomBrainDescriptor(
                "FUZZY_LOGIC",
                "Fuzzy Logic Engine",
                "Continuous membership functions for adaptive thresholding and environmental fuzzy inference.",
                CustomBrainDescriptor.BrainSourceType.BUILTIN,
                "",
                "SwarmForge Core Team",
                "2.0.0",
                Map.of("category", "Soft Computing", "deterministic", "true"),
                FuzzyLogicArchitecture::new
        ));

        registerBrain(new CustomBrainDescriptor(
                "BLACKBOARD",
                "Blackboard System (Shared Knowledge Base)",
                "Opportunistic multi-agent blackboard system communicating via shared social memory.",
                CustomBrainDescriptor.BrainSourceType.BUILTIN,
                "",
                "SwarmForge Core Team",
                "2.0.0",
                Map.of("category", "Distributed AI", "deterministic", "true"),
                FSMArchitecture::new
        ));

        registerBrain(new CustomBrainDescriptor(
                "HYBRID",
                "Hybrid Cognitive Architecture (Multi-Engine)",
                "Hybrid model combining reactive FSM reflexes, fuzzy transitions, and neural policy reinforcement.",
                CustomBrainDescriptor.BrainSourceType.BUILTIN,
                "",
                "SwarmForge Core Team",
                "2.0.0",
                Map.of("category", "Hybrid AI", "deterministic", "false"),
                org.swarmforge.core.behavior.rl.RLArchitecture::new
        ));

        registerBrain(new CustomBrainDescriptor(
                "NEURAL_NETWORK",
                "Spiking Neural Network / Default ONNX Brain",
                "Deep learning inference using trained neural models via ONNX runtime.",
                CustomBrainDescriptor.BrainSourceType.BUILTIN,
                OnnxBrainArchitecture.DEFAULT_MODEL_RESOURCE,
                "SwarmForge Neuroethology Lab",
                "2.0.0",
                Map.of("category", "Deep Learning", "framework", "ONNX"),
                OnnxBrainArchitecture::new
        ));
    }

    public void registerBrain(CustomBrainDescriptor descriptor) {
        if (descriptor == null) return;
        registeredBrains.put(descriptor.getId().toUpperCase(), descriptor);
        LOG.info("Registered cognitive brain architecture: " + descriptor);
        for (RegistryListener l : listeners) {
            try {
                l.onBrainRegistered(descriptor);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error in BrainRegistry listener", e);
            }
        }
    }

    public void unregisterBrain(String brainId) {
        if (brainId == null) return;
        CustomBrainDescriptor removed = registeredBrains.remove(brainId.toUpperCase());
        if (removed != null) {
            LOG.info("Unregistered cognitive brain: " + brainId);
            for (RegistryListener l : listeners) {
                try {
                    l.onBrainUnregistered(brainId);
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Error in BrainRegistry listener", e);
                }
            }
        }
    }

    public Optional<CustomBrainDescriptor> getDescriptor(String brainId) {
        if (brainId == null) return Optional.empty();
        return Optional.ofNullable(registeredBrains.get(brainId.toUpperCase()));
    }

    public List<CustomBrainDescriptor> getRegisteredBrains() {
        return new ArrayList<>(registeredBrains.values());
    }

    public List<String> getAvailableBrainIds() {
        return new ArrayList<>(registeredBrains.keySet());
    }

    public boolean hasBrain(String brainId) {
        return brainId != null && registeredBrains.containsKey(brainId.toUpperCase());
    }

    /**
     * Instantiates a ReasoningArchitecture by its registered ID or type name.
     */
    public ReasoningArchitecture createBrain(String brainId) {
        if (brainId == null || brainId.isBlank()) {
            return new FSMArchitecture();
        }

        String key = brainId.toUpperCase().trim();
        CustomBrainDescriptor descriptor = registeredBrains.get(key);
        if (descriptor != null) {
            return descriptor.createInstance();
        }

        // Fallback to standard enum parser
        return ReasoningArchitecture.create(ReasoningArchitecture.ArchitectureType.parse(brainId));
    }

    /**
     * Imports an external ONNX neural network model file (.onnx).
     */
    public CustomBrainDescriptor importOnnxModel(File onnxFile, String customName, String description) throws Exception {
        if (onnxFile == null || !onnxFile.exists()) {
            throw new IllegalArgumentException("ONNX file does not exist: " + onnxFile);
        }

        String baseName = onnxFile.getName().replaceAll("(?i)\\.onnx$", "");
        String brainId = "ONNX_" + baseName.toUpperCase().replaceAll("[^A-Z0-9_]", "_");
        String displayName = customName != null && !customName.isBlank() ? customName : "ONNX Neural Model (" + baseName + ")";
        String desc = description != null ? description : "Imported deep neural network model loaded from " + onnxFile.getName();

        // Verify ONNX model loads cleanly
        try (OnnxBrainArchitecture testBrain = new OnnxBrainArchitecture(onnxFile.getAbsolutePath())) {
            LOG.info("Successfully validated ONNX model: " + onnxFile.getAbsolutePath());
        }

        String absolutePath = onnxFile.getAbsolutePath();
        CustomBrainDescriptor descriptor = new CustomBrainDescriptor(
                brainId,
                displayName,
                desc,
                CustomBrainDescriptor.BrainSourceType.ONNX_MODEL,
                absolutePath,
                "Imported ONNX",
                "1.0.0",
                Map.of("filePath", absolutePath, "framework", "ONNX Runtime", "observationDim", "24", "actionDim", "14"),
                () -> new OnnxBrainArchitecture(absolutePath)
        );

        registerBrain(descriptor);
        return descriptor;
    }

    /**
     * Imports a Java bytecode plugin (.jar or .class) containing classes implementing ReasoningArchitecture.
     */
    public List<CustomBrainDescriptor> importJavaPlugin(File jarFile) throws Exception {
        if (jarFile == null || !jarFile.exists()) {
            throw new IllegalArgumentException("Plugin file does not exist: " + jarFile);
        }

        List<Class<? extends ReasoningArchitecture>> brainClasses = JavaBrainClassLoader.findBrainClasses(jarFile);
        if (brainClasses.isEmpty()) {
            throw new IllegalArgumentException("No classes implementing ReasoningArchitecture were found in " + jarFile.getName());
        }

        List<CustomBrainDescriptor> loaded = new ArrayList<>();
        for (Class<? extends ReasoningArchitecture> clazz : brainClasses) {
            String className = clazz.getSimpleName();
            String brainId = "JAVA_" + className.toUpperCase().replaceAll("[^A-Z0-9_]", "_");
            String displayName = "Java Plugin: " + className;
            String desc = "Custom Java reasoning architecture plugin loaded from " + jarFile.getName();

            CustomBrainDescriptor descriptor = new CustomBrainDescriptor(
                    brainId,
                    displayName,
                    desc,
                    CustomBrainDescriptor.BrainSourceType.JAVA_PLUGIN,
                    jarFile.getAbsolutePath(),
                    "External Contributor",
                    "1.0.0",
                    Map.of("jarPath", jarFile.getAbsolutePath(), "className", clazz.getName()),
                    () -> {
                        try {
                            return clazz.getDeclaredConstructor().newInstance();
                        } catch (Exception e) {
                            LOG.log(Level.SEVERE, "Failed to instantiate Java brain plugin: " + clazz.getName(), e);
                            return new FSMArchitecture();
                        }
                    }
            );

            registerBrain(descriptor);
            loaded.add(descriptor);
        }

        return loaded;
    }

    /**
     * Imports a declarative JSON / .sfbrain behavior tree file.
     */
    public CustomBrainDescriptor importJsonBrain(File jsonFile) throws Exception {
        if (jsonFile == null || !jsonFile.exists()) {
            throw new IllegalArgumentException("JSON brain file does not exist: " + jsonFile);
        }

        JsonBrainArchitecture brain = JsonBrainArchitecture.fromFile(jsonFile);
        String brainId = "SFBRAIN_" + brain.getBrainId().toUpperCase().replaceAll("[^A-Z0-9_]", "_");
        String displayName = brain.getName() != null ? brain.getName() : jsonFile.getName();
        String desc = brain.getDescription() != null && !brain.getDescription().isBlank() ? brain.getDescription() : "Declarative behavior tree loaded from " + jsonFile.getName();

        String absolutePath = jsonFile.getAbsolutePath();
        CustomBrainDescriptor descriptor = new CustomBrainDescriptor(
                brainId,
                displayName,
                desc,
                CustomBrainDescriptor.BrainSourceType.DECLARATIVE_JSON_GRAPH,
                absolutePath,
                "Behavior Tree Designer",
                "1.0.0",
                Map.of("filePath", absolutePath, "format", "JSON / .sfbrain"),
                () -> {
                    try {
                        return JsonBrainArchitecture.fromFile(new File(absolutePath));
                    } catch (Exception e) {
                        LOG.log(Level.SEVERE, "Failed to re-parse JSON brain: " + absolutePath, e);
                        return new FSMArchitecture();
                    }
                }
        );

        registerBrain(descriptor);
        return descriptor;
    }

    /**
     * Automatically scans directory for cognitive brains.
     */
    public void scanDirectory(Path directoryPath) {
        if (directoryPath == null || !Files.exists(directoryPath) || !Files.isDirectory(directoryPath)) {
            return;
        }

        try (var stream = Files.walk(directoryPath, 2)) {
            stream.filter(Files::isRegularFile).forEach(p -> {
                String name = p.getFileName().toString().toLowerCase();
                File f = p.toFile();
                try {
                    if (name.endsWith(".onnx")) {
                        importOnnxModel(f, null, null);
                    } else if (name.endsWith(".jar")) {
                        importJavaPlugin(f);
                    } else if (name.endsWith(".sfbrain") || (name.endsWith(".json") && name.contains("brain"))) {
                        importJsonBrain(f);
                    }
                } catch (Exception e) {
                    LOG.log(Level.FINE, "Failed to auto-load brain candidate: " + p, e);
                }
            });
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error scanning brain directory: " + directoryPath, e);
        }
    }

    private void scanDefaultDirectories() {
        try {
            Path defaultBrainsDir = Paths.get("data", "brains");
            if (Files.exists(defaultBrainsDir)) {
                scanDirectory(defaultBrainsDir);
            }
            Path pluginsBrainsDir = Paths.get("plugins", "brains");
            if (Files.exists(pluginsBrainsDir)) {
                scanDirectory(pluginsBrainsDir);
            }
        } catch (Exception e) {
            LOG.log(Level.FINE, "No default brain directories found on startup.", e);
        }
    }
}
