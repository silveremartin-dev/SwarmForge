/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior;

import org.swarmforge.core.simulation.SimulationContext;
import java.util.HashMap;
import java.util.Map;

/**
 * Belief-Desire-Intention (BDI) Reasoning Architecture for eusocial agents.
 * Models symbolic decision making using internal belief states, drive-based desires,
 * active intention plans, and a reward utility matrix.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class BDIArchitecture implements ReasoningArchitecture {

    private static final long serialVersionUID = 1L;

    /**
     * Agent Beliefs (Croyances) - Mental state & environment perception.
     */
    public static class Beliefs implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        public float knownHomeX, knownHomeY, knownHomeZ;
        public float knownFoodX, knownFoodY, knownFoodZ;
        public boolean hasFoodLocation = false;
        public boolean hasHomeLocation = false;
        public float perceivedThreatLevel = 0.0f;
        public float lastPheromoneSignal = 0.0f;
        public long lastUpdatedTick = 0;

        public Beliefs clone() {
            Beliefs b = new Beliefs();
            b.knownHomeX = this.knownHomeX;
            b.knownHomeY = this.knownHomeY;
            b.knownHomeZ = this.knownHomeZ;
            b.knownFoodX = this.knownFoodX;
            b.knownFoodY = this.knownFoodY;
            b.knownFoodZ = this.knownFoodZ;
            b.hasFoodLocation = this.hasFoodLocation;
            b.hasHomeLocation = this.hasHomeLocation;
            b.perceivedThreatLevel = this.perceivedThreatLevel;
            b.lastPheromoneSignal = this.lastPheromoneSignal;
            b.lastUpdatedTick = this.lastUpdatedTick;
            return b;
        }
    }

    /**
     * Agent Desires (Désirs / Motivations) - Drive state values [0.0 - 1.0].
     */
    public enum DesireType {
        SURVIVAL_HUNGER,
        COLONY_NUTRITION,
        QUEEN_CARE,
        NEST_MAINTENANCE,
        DEFENSE,
        GROOMING_HYGIENE,
        REST
    }

    /**
     * Agent Intentions (Intentions / Plans).
     */
    public enum IntentionType {
        GO_FORAGING,
        RETURN_TO_NEST,
        DEPOSIT_RESOURCES,
        ATTACK_ENEMY,
        CLEAN_SELF,
        REST_AND_RECOVER,
        FOLLOW_PHEROMONE_TRAIL
    }

    private final Beliefs beliefs = new Beliefs();
    private final Map<DesireType, Float> desireWeights = new HashMap<>();
    private IntentionType currentIntention = IntentionType.GO_FORAGING;
    private float totalAccumulatedReward = 0.0f;

    public BDIArchitecture() {
        initDefaultDesires();
    }

    private void initDefaultDesires() {
        desireWeights.put(DesireType.SURVIVAL_HUNGER, 0.5f);
        desireWeights.put(DesireType.COLONY_NUTRITION, 0.7f);
        desireWeights.put(DesireType.QUEEN_CARE, 0.4f);
        desireWeights.put(DesireType.NEST_MAINTENANCE, 0.3f);
        desireWeights.put(DesireType.DEFENSE, 0.8f);
        desireWeights.put(DesireType.GROOMING_HYGIENE, 0.2f);
        desireWeights.put(DesireType.REST, 0.1f);
    }

    @Override
    public ArchitectureType getType() {
        return ArchitectureType.BDI;
    }

    @Override
    public String getName() {
        return "Symbolic BDI (Belief-Desire-Intention)";
    }

    @Override
    public void initialize(AgentView agent) {
        if (agent != null) {
            beliefs.knownHomeX = agent.getX();
            beliefs.knownHomeY = agent.getY();
            beliefs.knownHomeZ = agent.getZ();
            beliefs.hasHomeLocation = true;
        }
    }

    @Override
    public Action decide(AgentView agent, SimulationContext context) {
        if (agent == null) {
            return Action.rest();
        }

        // 1. Update Beliefs from perception & agent state
        updateBeliefs(agent, context);

        // 2. Select Highest Priority Desire
        DesireType topDesire = evaluateDesires(agent);

        // 3. Form Intention & Select Plan Action
        currentIntention = formIntention(topDesire, agent);

        // 4. Translate Intention into Low-Level Action
        return executeIntentionPlan(currentIntention, agent);
    }

    private void updateBeliefs(AgentView agent, SimulationContext context) {
        if (agent.getEnergyLevel() < 0.3f) {
            desireWeights.put(DesireType.SURVIVAL_HUNGER, 1.0f - agent.getEnergyLevel());
        }
        if (agent.isCarryingFood()) {
            desireWeights.put(DesireType.COLONY_NUTRITION, 0.9f);
        }
    }

    private DesireType evaluateDesires(AgentView agent) {
        DesireType topDesire = DesireType.COLONY_NUTRITION;
        float maxWeight = -1.0f;
        for (Map.Entry<DesireType, Float> entry : desireWeights.entrySet()) {
            if (entry.getValue() > maxWeight) {
                maxWeight = entry.getValue();
                topDesire = entry.getKey();
            }
        }
        return topDesire;
    }

    private IntentionType formIntention(DesireType desire, AgentView agent) {
        if (agent.isCarryingFood()) {
            return IntentionType.RETURN_TO_NEST;
        }
        return switch (desire) {
            case DEFENSE -> IntentionType.ATTACK_ENEMY;
            case SURVIVAL_HUNGER, COLONY_NUTRITION -> IntentionType.GO_FORAGING;
            case REST -> IntentionType.REST_AND_RECOVER;
            case GROOMING_HYGIENE -> IntentionType.CLEAN_SELF;
            default -> IntentionType.GO_FORAGING;
        };
    }

    private Action executeIntentionPlan(IntentionType intention, AgentView agent) {
        return switch (intention) {
            case RETURN_TO_NEST -> Action.returnHome();
            case GO_FORAGING -> Action.forage();
            case ATTACK_ENEMY -> Action.attack(null);
            case REST_AND_RECOVER, CLEAN_SELF -> Action.rest();
            default -> Action.forage();
        };
    }

    @Override
    public void update(AgentView agent, Action executedAction, ActionResult result) {
        if (result != null) {
            totalAccumulatedReward += result.reward();
            if (!result.success()) {
                // Adjust desire priorities on failure
                float currentWeight = desireWeights.getOrDefault(DesireType.COLONY_NUTRITION, 0.5f);
                desireWeights.put(DesireType.COLONY_NUTRITION, Math.max(0.1f, currentWeight - 0.05f));
            }
        }
    }

    @Override
    public void reset() {
        beliefs.hasFoodLocation = false;
        initDefaultDesires();
        totalAccumulatedReward = 0.0f;
    }

    @Override
    public ReasoningArchitecture clone() {
        BDIArchitecture copy = new BDIArchitecture();
        copy.desireWeights.putAll(this.desireWeights);
        return copy;
    }

    public Beliefs getBeliefs() {
        return beliefs;
    }

    public float getTotalAccumulatedReward() {
        return totalAccumulatedReward;
    }
}
