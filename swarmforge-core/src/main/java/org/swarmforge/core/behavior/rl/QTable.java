/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior.rl;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Manages Q-values for State-Action pairs.
 * Implements standard tabular Q-Learning with epsilon-greedy exploration.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class QTable {

    // Simplified Action set for learning (Movement focused)
    public enum RLAction {
        MOVE_FORWARD,
        TURN_LEFT,
        TURN_RIGHT,
        PICK_UP_FOOD,
        DROP_FOOD,
        WAIT
    }

    private final Map<RLState, Map<RLAction, Double>> table = new HashMap<>();
    private final Random random = new Random();

    // Hyperparameters
    private double alpha = 0.1; // Learning Rate
    private double gamma = 0.9; // Discount Factor
    private double epsilon = 0.1; // Exploration Rate

    public QTable() {
    }

    /**
     * Get the Q-value for a state-action pair.
     */
    public double getQ(RLState state, RLAction action) {
        return table.computeIfAbsent(state, k -> new EnumMap<>(RLAction.class))
                .getOrDefault(action, 0.0);
    }

    /**
     * Get the best action for a given state (Exploitation).
     */
    public RLAction getBestAction(RLState state) {
        Map<RLAction, Double> actions = table.computeIfAbsent(state, k -> new EnumMap<>(RLAction.class));

        RLAction bestAction = RLAction.WAIT;
        double maxQ = -Double.MAX_VALUE;

        // If state is new, initialize with random values or zeros (implicit)
        if (actions.isEmpty()) {
            return RLAction.values()[random.nextInt(RLAction.values().length)];
        }

        for (RLAction action : RLAction.values()) {
            double q = actions.getOrDefault(action, 0.0);
            if (q > maxQ) {
                maxQ = q;
                bestAction = action;
            }
        }
        return bestAction;
    }

    /**
     * Choose an action using Epsilon-Greedy strategy.
     */
    public RLAction chooseAction(RLState state) {
        if (random.nextDouble() < epsilon) {
            // Explore
            return RLAction.values()[random.nextInt(RLAction.values().length)];
        } else {
            // Exploit
            return getBestAction(state);
        }
    }

    /**
     * Update the Q-value based on reward and next state.
     * Q(s,a) = Q(s,a) + alpha * (reward + gamma * max(Q(s', a')) - Q(s,a))
     */
    public void update(RLState state, RLAction action, double reward, RLState nextState) {
        double currentQ = getQ(state, action);
        double maxNextQ = getMaxQ(nextState);

        double newQ = currentQ + alpha * (reward + gamma * maxNextQ - currentQ);

        table.get(state).put(action, newQ);
    }

    public double getMaxQ(RLState state) {
        Map<RLAction, Double> actions = table.get(state);
        if (actions == null || actions.isEmpty())
            return 0.0;

        double maxQ = -Double.MAX_VALUE;
        for (double q : actions.values()) {
            if (q > maxQ)
                maxQ = q;
        }
        return maxQ;
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }
}
