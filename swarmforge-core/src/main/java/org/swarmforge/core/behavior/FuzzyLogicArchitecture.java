/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior;

import org.swarmforge.core.behavior.AgentView;
import org.swarmforge.core.simulation.SimulationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Fuzzy Logic implementation for ant behavior.
 * Uses fuzzy membership functions and rules for smooth decision blending.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class FuzzyLogicArchitecture implements ReasoningArchitecture {

    // Fuzzy input variables
    private final Map<String, Float> inputs = new HashMap<>();

    // Fuzzy output (action weights)
    private final Map<Action.ActionType, Float> actionWeights = new HashMap<>();

    @Override
    public ArchitectureType getType() {
        return ArchitectureType.FUZZY_LOGIC;
    }

    @Override
    public String getName() {
        return "Fuzzy Logic Controller";
    }

    @Override
    public void initialize(AgentView agent) {
        inputs.clear();
        actionWeights.clear();
    }

    @Override
    public Action decide(AgentView agent, SimulationContext context) {
        // Fuzzify inputs
        fuzzifyInputs(agent, context);

        // Apply fuzzy rules
        applyRules();

        // Defuzzify to get best action
        return defuzzify(agent);
    }

    private void fuzzifyInputs(AgentView agent, SimulationContext context) {
        // Energy level (0-1)
        float energy = agent.getEnergyLevel();
        inputs.put("energy_low", trapezoid(energy, 0, 0, 0.2f, 0.4f));
        inputs.put("energy_medium", trapezoid(energy, 0.3f, 0.4f, 0.6f, 0.7f));
        inputs.put("energy_high", trapezoid(energy, 0.6f, 0.8f, 1.0f, 1.0f));

        // Hunger (inverse of food carried)
        float hunger = agent.isCarryingFood() ? 0 : 1;
        inputs.put("hungry", hunger);
        inputs.put("fed", 1 - hunger);

        // Pheromone signals
        if (context != null) {
            float foodPhero = context.getFoodPheromone(agent.getX(), agent.getY(), agent.getZ());
            inputs.put("food_trail_none", trapezoid(foodPhero, 0, 0, 0.1f, 0.2f));
            inputs.put("food_trail_weak", trapezoid(foodPhero, 0.1f, 0.2f, 0.4f, 0.5f));
            inputs.put("food_trail_strong", trapezoid(foodPhero, 0.4f, 0.6f, 1.0f, 1.0f));

            float homePhero = context.getHomePheromone(agent.getX(), agent.getY(), agent.getZ());
            inputs.put("near_home", trapezoid(homePhero, 0.5f, 0.7f, 1.0f, 1.0f));
            inputs.put("far_from_home", trapezoid(homePhero, 0, 0, 0.2f, 0.4f));

            // Danger
            float danger = context.hasEnemyNearby(agent) ? 1.0f : 0.0f;
            inputs.put("danger_high", danger);
            inputs.put("danger_low", 1 - danger);
        }

        // Distance from home
        float dx = agent.getHomeX() - agent.getX();
        float dy = agent.getHomeY() - agent.getY();
        float distHome = (float) Math.sqrt(dx * dx + dy * dy);
        inputs.put("home_close", trapezoid(distHome, 0, 0, 5, 15));
        inputs.put("home_far", trapezoid(distHome, 10, 30, 100, 100));
    }

    private void applyRules() {
        actionWeights.clear();

        // Rule: IF hungry AND food_trail_strong THEN forage
        addWeight(Action.ActionType.FORAGE,
                and(get("hungry"), get("food_trail_strong")) * 0.9f);

        // Rule: IF fed AND home_far THEN return_home
        addWeight(Action.ActionType.RETURN_HOME,
                and(get("fed"), get("home_far")) * 0.8f);

        // Rule: IF energy_low THEN rest
        addWeight(Action.ActionType.REST, get("energy_low") * 0.95f);

        // Rule: IF danger_high AND NOT soldier THEN flee
        addWeight(Action.ActionType.FLEE, get("danger_high") * 0.9f);

        // Rule: IF food_trail_none AND energy_high THEN explore
        addWeight(Action.ActionType.EXPLORE,
                and(get("food_trail_none"), get("energy_high")) * 0.5f);

        // Rule: IF food_trail_weak THEN follow_trail
        addWeight(Action.ActionType.FOLLOW_TRAIL, get("food_trail_weak") * 0.6f);

        // Default: some exploration
        addWeight(Action.ActionType.EXPLORE, 0.1f);
    }

    private Action defuzzify(AgentView agent) {
        // Find action with highest weight
        Action.ActionType bestAction = Action.ActionType.EXPLORE;
        float bestWeight = 0;

        for (Map.Entry<Action.ActionType, Float> entry : actionWeights.entrySet()) {
            if (entry.getValue() > bestWeight) {
                bestWeight = entry.getValue();
                bestAction = entry.getKey();
            }
        }

        return createAction(bestAction, agent, bestWeight);
    }

    private Action createAction(Action.ActionType type, AgentView agent, float intensity) {
        return switch (type) {
            case FORAGE -> Action.forage();
            case RETURN_HOME -> Action.returnHome();
            case REST -> Action.rest();
            case FLEE, EXPLORE -> {
                float angle = (float) (Math.random() * Math.PI * 2);
                yield Action.move((float) Math.cos(angle) * intensity, (float) Math.sin(angle) * intensity, 0);
            }
            case FOLLOW_TRAIL -> Action.followTrail(intensity, intensity, 0);
            default -> Action.rest();
        };
    }

    @Override
    public void update(AgentView agent, Action executedAction, ActionResult result) {
        // Fuzzy logic is stateless per-tick
    }

    @Override
    public void reset() {
        inputs.clear();
        actionWeights.clear();
    }

    @Override
    public ReasoningArchitecture clone() {
        return new FuzzyLogicArchitecture();
    }

    // === Fuzzy Operations ===

    /**
     * Trapezoid membership function.
     */
    private float trapezoid(float x, float a, float b, float c, float d) {
        if (x <= a || x >= d)
            return 0;
        if (x >= b && x <= c)
            return 1;
        if (x > a && x < b)
            return (x - a) / (b - a);
        return (d - x) / (d - c);
    }

    private float get(String name) {
        return inputs.getOrDefault(name, 0f);
    }

    private float and(float a, float b) {
        return Math.min(a, b);
    }

    private void addWeight(Action.ActionType action, float weight) {
        actionWeights.merge(action, weight, (v1, v2) -> Math.max(v1, v2));
    }
}
