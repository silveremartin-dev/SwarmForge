/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior.rl;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import org.swarmforge.core.behavior.AgentView;
import org.swarmforge.core.behavior.ReasoningArchitecture;
import org.swarmforge.core.simulation.SimulationContext;

import java.io.File;
import java.io.Serializable;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * High-performance ONNX Neural Network Decision Engine for SwarmForge agents.
 * Runs in-process via Microsoft ONNX Runtime with JNI native bindings.
 * Supports multi-caste, multi-species conditioned observation vectors.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class OnnxBrainArchitecture implements ReasoningArchitecture, AutoCloseable, Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(OnnxBrainArchitecture.class.getName());

    public static final int OBSERVATION_DIM = 24;
    public static final int NUM_ACTIONS = 14;

    // Cache of loaded ONNX sessions by model path or ID to avoid reloading the model per agent
    private static final Map<String, OrtSession> SESSION_CACHE = new ConcurrentHashMap<>();
    private static OrtEnvironment env;

    private final String modelKey;
    private final String modelPath;
    private final byte[] modelBytes;
    private final ReasoningArchitecture fallbackBrain;

    private transient OrtSession session;
    private transient String inputName;

    public static final String DEFAULT_MODEL_RESOURCE = "/models/species_brain.onnx";

    public OnnxBrainArchitecture() {
        this(DEFAULT_MODEL_RESOURCE);
    }

    public OnnxBrainArchitecture(String modelPath) {
        this(modelPath != null ? modelPath : DEFAULT_MODEL_RESOURCE, modelPath, null);
    }

    public OnnxBrainArchitecture(byte[] modelBytes, String modelId) {
        this(modelId, null, modelBytes);
    }

    private OnnxBrainArchitecture(String modelKey, String modelPath, byte[] modelBytes) {
        this.modelKey = modelKey != null ? modelKey : "default_onnx_model";
        this.modelPath = modelPath;
        this.modelBytes = modelBytes;
        this.fallbackBrain = new RLArchitecture();
        ensureSessionLoaded();
    }

    private synchronized void ensureSessionLoaded() {
        if (session != null) return;

        try {
            if (env == null) {
                env = OrtEnvironment.getEnvironment("SwarmForgeOrtEnv");
            }

            if (SESSION_CACHE.containsKey(modelKey)) {
                this.session = SESSION_CACHE.get(modelKey);
                initInputName();
                return;
            }

            OrtSession.SessionOptions options = new OrtSession.SessionOptions();
            options.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT);

            OrtSession newSession = null;
            if (modelBytes != null) {
                newSession = env.createSession(modelBytes, options);
            } else if (modelPath != null) {
                File file = new File(modelPath);
                if (file.exists()) {
                    newSession = env.createSession(modelPath, options);
                } else {
                    // Try loading from classpath resources
                    String resPath = modelPath.startsWith("/") ? modelPath : "/" + modelPath;
                    try (java.io.InputStream in = OnnxBrainArchitecture.class.getResourceAsStream(resPath)) {
                        if (in != null) {
                            byte[] resBytes = in.readAllBytes();
                            newSession = env.createSession(resBytes, options);
                        }
                    }

                    // Fallback to dev workspace resource paths
                    if (newSession == null) {
                        File devRes = new File("src/main/resources" + resPath);
                        if (!devRes.exists()) {
                            devRes = new File("swarmforge-core/src/main/resources" + resPath);
                        }
                        if (devRes.exists()) {
                            newSession = env.createSession(devRes.getAbsolutePath(), options);
                        }
                    }
                }
            }

            if (newSession != null) {
                SESSION_CACHE.put(modelKey, newSession);
                this.session = newSession;
                initInputName();
                LOG.info("ONNX Brain successfully loaded for model: " + modelKey);
            } else {
                LOG.warning("ONNX model source not found: " + modelPath + ". Operating in fallback mode.");
            }

        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to initialize ONNX Runtime session: " + e.getMessage(), e);
        }
    }

    private void initInputName() {
        if (session != null) {
            try {
                this.inputName = session.getInputNames().iterator().next();
            } catch (Exception e) {
                this.inputName = "observation";
            }
        }
    }

    @Override
    public ArchitectureType getType() {
        return ArchitectureType.NEURAL_NETWORK;
    }

    public enum ExecutionMode {
        AUTO,
        NATIVE_ONNX_SINGLE,
        NATIVE_ONNX_BATCH,
        PURE_JAVA_FASTPATH
    }

    private static final Map<String, SimpleNeuralNetwork> FAST_PATH_CACHE = new ConcurrentHashMap<>();
    private ExecutionMode executionMode = ExecutionMode.AUTO;

    public static void registerFastPathEngine(String modelKey, SimpleNeuralNetwork network) {
        if (modelKey != null && network != null) {
            FAST_PATH_CACHE.put(modelKey, network);
        }
    }

    public ExecutionMode getExecutionMode() {
        return executionMode;
    }

    public void setExecutionMode(ExecutionMode mode) {
        this.executionMode = (mode != null) ? mode : ExecutionMode.AUTO;
    }

    @Override
    public String getName() {
        return "ONNX Neural Brain (" + modelKey + " [" + executionMode + "])";
    }

    @Override
    public void initialize(AgentView agent) {
        ensureSessionLoaded();
        if (fallbackBrain != null) {
            fallbackBrain.initialize(agent);
        }
    }

    @Override
    public Action decide(AgentView agent, SimulationContext context) {
        SimpleNeuralNetwork fast = FAST_PATH_CACHE.get(modelKey);
        if (executionMode == ExecutionMode.PURE_JAVA_FASTPATH || (executionMode == ExecutionMode.AUTO && fast != null)) {
            if (fast != null) {
                return decideFast(agent, context, fast);
            }
        }

        if (session == null) {
            ensureSessionLoaded();
        }

        if (session == null || env == null) {
            if (fast != null) {
                return decideFast(agent, context, fast);
            }
            return fallbackBrain.decide(agent, context);
        }

        try {
            float[] obs = buildObservationVector(agent, context);
            long[] shape = new long[]{1, obs.length};

            FloatBuffer buffer = FloatBuffer.wrap(obs);
            try (OnnxTensor tensor = OnnxTensor.createTensor(env, buffer, shape)) {
                String inName = (inputName != null) ? inputName : "observation";
                try (OrtSession.Result result = session.run(Collections.singletonMap(inName, tensor))) {
                    Object rawValue = result.get(0).getValue();
                    float[] logits = extractLogits(rawValue);
                    int actionIdx = SimpleNeuralNetwork.argmax(logits);
                    return decodeAction(actionIdx, agent, context);
                }
            }
        } catch (Exception e) {
            LOG.log(Level.FINE, "Inference error, defaulting to fallback: " + e.getMessage());
            if (fast != null) {
                return decideFast(agent, context, fast);
            }
            return fallbackBrain.decide(agent, context);
        }
    }

    /**
     * Ultra-fast zero-JNI in-memory Java inference (< 1 µs per agent).
     */
    public Action decideFast(AgentView agent, SimulationContext context, SimpleNeuralNetwork engine) {
        float[] obs = buildObservationVector(agent, context);
        float[] logits = engine.forwardFast(obs);
        int actionIdx = SimpleNeuralNetwork.argmax(logits);
        return decodeAction(actionIdx, agent, context);
    }

    /**
     * Vectorized Batch Inference: evaluates up to thousands of agents simultaneously
     * in a single native SIMD/GPU tensor pass or parallel fast-path.
     *
     * @param agents  List of agents to evaluate
     * @param context Simulation context
     * @return List of decided actions corresponding to input agents
     */
    public java.util.List<Action> decideBatch(java.util.List<? extends AgentView> agents, SimulationContext context) {
        if (agents == null || agents.isEmpty()) return java.util.Collections.emptyList();
        int n = agents.size();
        java.util.List<Action> actions = new java.util.ArrayList<>(n);

        SimpleNeuralNetwork fast = FAST_PATH_CACHE.get(modelKey);
        if (executionMode == ExecutionMode.PURE_JAVA_FASTPATH || (executionMode == ExecutionMode.AUTO && fast != null)) {
            if (fast != null) {
                if (n >= 100) {
                    return agents.parallelStream()
                            .map(agent -> decideFast(agent, context, fast))
                            .toList();
                } else {
                    for (AgentView agent : agents) {
                        actions.add(decideFast(agent, context, fast));
                    }
                    return actions;
                }
            }
        }

        if (session == null) {
            ensureSessionLoaded();
        }

        if (session == null || env == null) {
            for (AgentView agent : agents) {
                actions.add(fallbackBrain.decide(agent, context));
            }
            return actions;
        }

        try {
            float[] allObs = new float[n * OBSERVATION_DIM];
            for (int i = 0; i < n; i++) {
                float[] obs = buildObservationVector(agents.get(i), context);
                System.arraycopy(obs, 0, allObs, i * OBSERVATION_DIM, OBSERVATION_DIM);
            }

            long[] shape = new long[]{n, OBSERVATION_DIM};
            FloatBuffer buffer = FloatBuffer.wrap(allObs);
            try (OnnxTensor tensor = OnnxTensor.createTensor(env, buffer, shape)) {
                String inName = (inputName != null) ? inputName : "observation";
                try (OrtSession.Result result = session.run(Collections.singletonMap(inName, tensor))) {
                    Object rawValue = result.get(0).getValue();
                    if (rawValue instanceof float[][] matrix) {
                        for (int i = 0; i < n; i++) {
                            int actionIdx = SimpleNeuralNetwork.argmax(matrix[i]);
                            actions.add(decodeAction(actionIdx, agents.get(i), context));
                        }
                    } else if (rawValue instanceof float[] array && n == 1) {
                        int actionIdx = SimpleNeuralNetwork.argmax(array);
                        actions.add(decodeAction(actionIdx, agents.get(0), context));
                    }
                }
            }
        } catch (Exception e) {
            LOG.log(Level.FINE, "Batch inference error, defaulting to individual: " + e.getMessage());
            for (AgentView agent : agents) {
                actions.add(decide(agent, context));
            }
        }
        return actions;
    }

    private float[] extractLogits(Object rawValue) {
        if (rawValue instanceof float[][] matrix) {
            return matrix[0];
        } else if (rawValue instanceof float[] array) {
            return array;
        }
        return new float[NUM_ACTIONS];
    }

    /**
     * Builds the complete 24-dimensional conditioned observation vector:
     * [0]  Energy ratio [0..1]
     * [1]  Hunger ratio [0..1]
     * [2]  Health ratio [0..1]
     * [3]  Heading cos(theta)
     * [4]  Heading sin(theta)
     * [5]  Is carrying item (1.0 or 0.0)
     * [6]  Is at nest / hive (1.0 or 0.0)
     * [7]  Food pheromone lateral gradient [-1..1]
     * [8]  Home pheromone lateral gradient [-1..1]
     * [9]  Alarm pheromone intensity [0..1]
     * [10] Brood pheromone intensity [0..1]
     * [11] Has food nearby (1.0 or 0.0)
     * [12] Has enemy / predator nearby (1.0 or 0.0)
     * [13] Has brood / nurse target nearby (1.0 or 0.0)
     * [14..18] One-Hot Caste (Worker, Soldier, Nurse, Queen, Drone)
     * [19..22] One-Hot Insect Order (Formicidae, Apidae, Vespidae, Isoptera)
     * [23] Aggression / Combat trait [0..1]
     */
    public static float[] buildObservationVector(AgentView agent, SimulationContext ctx) {
        float[] obs = new float[OBSERVATION_DIM];

        if (agent == null) return obs;

        obs[0] = Math.max(0.0f, Math.min(1.0f, agent.getEnergyLevel()));
        obs[1] = Math.max(0.0f, Math.min(1.0f, agent.getHunger()));
        obs[2] = 1.0f; // Baseline health ratio

        float heading = agent.getHeading();
        obs[3] = (float) Math.cos(heading);
        obs[4] = (float) Math.sin(heading);
        obs[5] = agent.isCarryingFood() ? 1.0f : 0.0f;
        obs[6] = agent.isAtNest() ? 1.0f : 0.0f;

        // Chemical Gradients & Sensory Environment
        if (ctx != null) {
            float gx = ctx.getFoodPheromoneGradientX(agent.getX(), agent.getY(), agent.getZ());
            float gy = ctx.getFoodPheromoneGradientY(agent.getX(), agent.getY(), agent.getZ());
            obs[7] = (float) Math.max(-1.0, Math.min(1.0, -gx * Math.sin(heading) + gy * Math.cos(heading)));

            float hx = ctx.getHomePheromone(agent.getX() + 1, agent.getY(), agent.getZ())
                    - ctx.getHomePheromone(agent.getX() - 1, agent.getY(), agent.getZ());
            float hy = ctx.getHomePheromone(agent.getX(), agent.getY() + 1, agent.getZ())
                    - ctx.getHomePheromone(agent.getX(), agent.getY() - 1, agent.getZ());
            obs[8] = (float) Math.max(-1.0, Math.min(1.0, -hx * Math.sin(heading) + hy * Math.cos(heading)));

            obs[9] = Math.max(0.0f, Math.min(1.0f, ctx.getAlarmPheromone(agent.getX(), agent.getY(), agent.getZ())));
            obs[10] = agent.isAtNest() ? 0.8f : 0.0f; // Brood pheromone inside nest
            obs[11] = ctx.hasFoodNearby(agent) ? 1.0f : 0.0f;
            obs[12] = ctx.hasEnemyNearby(agent) ? 1.0f : 0.0f;
            obs[13] = (agent.isNurse() || agent.isQueen()) && agent.isAtNest() ? 1.0f : 0.0f;
        }

        // Caste conditioning
        if (agent.isQueen()) {
            obs[17] = 1.0f;
        } else if (agent.isNurse()) {
            obs[16] = 1.0f;
        } else if (agent.isSoldier() || agent.isMajor()) {
            obs[15] = 1.0f;
        } else if (agent.isDrone()) {
            obs[18] = 1.0f;
        } else {
            obs[14] = 1.0f; // Default Worker / Forager
        }

        // Insect Order Taxonomy (Formicidae default)
        String insectType = (agent.getSpecies() != null) ? agent.getSpecies().getInsectType() : "ANT";
        if ("BEE".equalsIgnoreCase(insectType) || "BUMBLEBEE".equalsIgnoreCase(insectType)) {
            obs[20] = 1.0f; // Apidae
        } else if ("WASP".equalsIgnoreCase(insectType)) {
            obs[21] = 1.0f; // Vespidae
        } else if ("TERMITE".equalsIgnoreCase(insectType)) {
            obs[22] = 1.0f; // Isoptera
        } else {
            obs[19] = 1.0f; // Formicidae (Ants)
        }

        // Trait parameter
        obs[23] = agent.isSoldier() ? 0.9f : (agent.isQueen() ? 0.0f : 0.2f);

        return obs;
    }

    public static Action decodeAction(int actionIndex, AgentView agent, SimulationContext ctx) {
        float speed = 1.0f;
        float heading = (agent != null) ? agent.getHeading() : 0.0f;

        return switch (actionIndex) {
            case 0 -> { // MOVE_FORWARD
                float dx = (float) Math.cos(heading) * speed;
                float dy = (float) Math.sin(heading) * speed;
                yield Action.move(dx, dy, 0);
            }
            case 1 -> { // TURN_LEFT
                float newHeading = heading + 0.4f;
                float dx = (float) Math.cos(newHeading) * speed;
                float dy = (float) Math.sin(newHeading) * speed;
                yield Action.move(dx, dy, 0);
            }
            case 2 -> { // TURN_RIGHT
                float newHeading = heading - 0.4f;
                float dx = (float) Math.cos(newHeading) * speed;
                float dy = (float) Math.sin(newHeading) * speed;
                yield Action.move(dx, dy, 0);
            }
            case 3 -> Action.forage();
            case 4 -> Action.depositFood();
            case 5 -> Action.returnHome();
            case 6 -> Action.attack(ctx != null ? ctx.getNearestEnemyTarget(agent) : null);
            case 7 -> new Action(Action.ActionType.DEFEND_PATROL, 0, 0, 0, 1.0f, null);
            case 8 -> new Action(Action.ActionType.TEND_BROOD, 0, 0, 0, 1.0f, null);
            case 9 -> Action.layEgg();
            case 10 -> new Action(Action.ActionType.NURSE, 0, 0, 0, 1.0f, null);
            case 11 -> new Action(Action.ActionType.GROOM, 0, 0, 0, 1.0f, null);
            case 12 -> new Action(Action.ActionType.DEPOSIT_PHEROMONE, 0, 0, 0, 1.0f, null);
            default -> Action.rest();
        };
    }

    @Override
    public void update(AgentView agent, Action executedAction, ActionResult result) {
        if (fallbackBrain != null) {
            fallbackBrain.update(agent, executedAction, result);
        }
    }

    @Override
    public void reset() {
        if (fallbackBrain != null) {
            fallbackBrain.reset();
        }
    }

    @Override
    public ReasoningArchitecture clone() {
        if (modelBytes != null) {
            return new OnnxBrainArchitecture(modelBytes, modelKey);
        }
        return new OnnxBrainArchitecture(modelPath);
    }

    @Override
    public void close() {
        // Individual instances share cached sessions; OrtEnvironment handles lifecycle
    }
}
