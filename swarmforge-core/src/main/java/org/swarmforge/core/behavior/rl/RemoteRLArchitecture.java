/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior.rl;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.swarmforge.core.behavior.AgentView;
import org.swarmforge.core.behavior.ReasoningArchitecture;

import org.swarmforge.core.simulation.SimulationContext;
import org.swarmforge.protocol.grpc.*;

import java.util.logging.Logger;

/**
 * ReasoningArchitecture that offloads decision making to a remote RL service
 * (or sidecar).
 * Falls back to local simple logic if connection fails.
 */
public class RemoteRLArchitecture implements ReasoningArchitecture {

    private static final Logger LOG = Logger.getLogger(RemoteRLArchitecture.class.getName());

    // Shared Channel (assumed singleton for now, or managed centrally)
    private static ManagedChannel channel;
    private static RLServiceGrpc.RLServiceBlockingStub blockingStub;

    // Fallback brain
    private final ReasoningArchitecture fallbackBrain;

    private final String modelId;
    // private RLState lastState; // Unused
    // private int lastActionIndex = -1; // Unused

    public RemoteRLArchitecture(String modelId) {
        this.modelId = modelId;
        // Default fallback (e.g. random or simple state machine)
        this.fallbackBrain = new RLArchitecture();
        ensureConnection();
    }

    private synchronized void ensureConnection() {
        if (channel == null || channel.isShutdown()) {
            // In k8s/production this would be "swarmforge-ai-service:50051"
            // For now assume local sidecar or mock
            String target = "localhost:50051";
            channel = ManagedChannelBuilder.forTarget(target)
                    .usePlaintext()
                    .build();
            blockingStub = RLServiceGrpc.newBlockingStub(channel);
            LOG.info("Connected to AI Service at " + target);
        }
    }

    @Override
    public ArchitectureType getType() {
        return ArchitectureType.HYBRID;
    }

    @Override
    public String getName() {
        return "Remote RL Agent (" + modelId + ")";
    }

    @Override
    public void initialize(AgentView agent) {
        fallbackBrain.initialize(agent);
    }

    private long lastDecisionTime = 0;
    private Action currentAction = null;
    private long actionDuration = 0;

    @Override
    public Action decide(AgentView agent, SimulationContext context) {
        long now = System.currentTimeMillis();

        if (currentAction != null && (now - lastDecisionTime) < actionDuration) {
            return currentAction;
        }

        try {
            // 1. Observe
            Observation observation = observe(agent, context);

            // 2. Predict (RPC)
            PredictRequest request = PredictRequest.newBuilder()
                    .setModelId(modelId)
                    .setAgentId(agent.getAgentId())
                    .setState(observation)
                    .build();

            PredictResponse response = blockingStub.predict(request);

            // 3. Convert to Action
            int actionIndex = response.getActionIndex();
            // this.lastActionIndex = actionIndex; // Removed unused field

            Action newAction = decodeAction(actionIndex, agent);
            
            // Set cooldown based on action type (very simple heuristic for now)
            // e.g. MOVE takes 500ms, FORAGE takes 2000ms
            this.lastDecisionTime = now;
            this.currentAction = newAction;
            this.actionDuration = (long) (500 + Math.random() * 500); // Randomize slightly

            return newAction;

        } catch (Exception e) {
            // LOG.warning("Remote inference failed: " + e.getMessage() + ". Falling
            // back.");
            // Silent fallback to avoid log spam in high freq loop
            return fallbackBrain.decide(agent, context);
        }
    }

    @Override
    public void update(AgentView agent, Action executedAction, ActionResult result) {
        // Optional: Send training data (S, A, R, S') to server
        // This usually requires waiting for Next State (S') which happens in next tick.
        // For MVP we skip training loop here or implement async logic.
        fallbackBrain.update(agent, executedAction, result);
    }

    private Observation observe(AgentView agent, SimulationContext ctx) {
        // Flatten state
        Observation.Builder obs = Observation.newBuilder();

        obs.addValues(agent.getEnergyLevel());
        obs.addValues(1.0f); // Health fallback
        obs.addValues(agent.isCarryingFood() ? 1f : 0f);
        // ... Add sensor data

        return obs.build();
    }

    @Override
    public void reset() {
        // Re-establish connection or reset internal state if needed
        // lastState = null;
        // lastActionIndex = -1;
    }

    @Override
    public ReasoningArchitecture clone() {
        return new RemoteRLArchitecture(this.modelId);
    }

    private Action decodeAction(int index, AgentView agent) {
        // Map integer index back to core Action
        // Must match QTable.RLAction or Python side definition
        // 0=MOVE_FWD, 1=TURN_L, 2=TURN_R, 3=PICKUP, 4=DROP
        switch (index) {
            case 0:
                return new Action(Action.ActionType.MOVE, 0, 0, 0, 1.0f, null);
            case 1:
                return new Action(Action.ActionType.MOVE, -1, 0, 0, 0.5f, null);
            case 2:
                return new Action(Action.ActionType.MOVE, 1, 0, 0, 0.5f, null);
            case 3:
                return new Action(Action.ActionType.FORAGE, 0, 0, 0, 1.0f, null);
            case 4:
                return new Action(Action.ActionType.DEPOSIT_FOOD, 0, 0, 0, 1.0f, null);
            default:
                return new Action(Action.ActionType.REST, 0, 0, 0, 1.0f, null);
        }
    }
}
