/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior.rl;

import org.swarmforge.core.behavior.ReasoningArchitecture;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.simulation.SimulationContext;

/**
 * Reinforcement Learning Architecture using Q-Learning.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class RLArchitecture implements ReasoningArchitecture {

    // Shared brain for all ants using this architecture (Swarm Intelligence!)
    private static final QTable Q_TABLE = new QTable();

    private RLState lastState;
    private QTable.RLAction lastAction;
    private double currentReward = 0;

    @Override
    public ArchitectureType getType() {
        return ArchitectureType.HYBRID; // Or custom
    }

    @Override
    public String getName() {
        return "Q-Learning Agent";
    }

    @Override
    public void initialize(Individual individual) {
        // Reset transient state
        lastState = null;
        lastAction = null;
    }

    @Override
    public Action decide(Individual individual, SimulationContext context) {
        // 1. Observe State
        RLState currentState = observeState(individual, context);

        // 0. Learn from previous action (S, A, R, S')
        performLearningUpdate(currentState);

        // 2. Choose Action from QTable
        QTable.RLAction rlAction = Q_TABLE.chooseAction(currentState);

        // 3. Store for update
        this.lastState = currentState;
        this.lastAction = rlAction;

        // 4. Execute (Convert RLAction to Game Action)
        return convertAction(rlAction, individual);
    }

    @Override
    public void update(Individual individual, Action executedAction, ActionResult result) {
        if (lastState != null && lastAction != null) {
            // 1. Observe New State (after action)
            // Note: We need context for this, but update doesn't provide it.
            // We might need to assume the decide() call in the NEXT tick will handle the
            // "next state" logic,
            // OR we approximate the next state here if possible, but we don't have context.
            // Standard RL loop: S, A, R, S'.
            // Here update() gives us R (result). S' is observed in the NEXT decide().
            // So we delay the update step?

            // Actually, we can calculate the reward here based on the result.
            double reward = calculateReward(individual, executedAction, result);
            this.currentReward = reward;

            // We can't update Q-Table efficiently without S'.
            // We will store the reward and apply the update at the BEGINNING of the next
            // decide() call,
            // where we have the fresh S'.
        }
    }

    private void performLearningUpdate(RLState nextState) {
        if (lastState != null && lastAction != null) {
            Q_TABLE.update(lastState, lastAction, currentReward, nextState);
        }
    }

    private RLState observeState(Individual ind, SimulationContext ctx) {
        // Simple discrete state observation
        boolean hasFood = ind.isCarryingFood();
        boolean isAtNest = (Math.abs(ind.getX() - ind.getHomeX()) < 2.0 && Math.abs(ind.getY() - ind.getHomeY()) < 2.0);
        boolean isLoaded = ind.isCarryingFood();

        // Pheromones (Simplified)
        RLState.PheromoneDirection foodDir = RLState.PheromoneDirection.NONE;

        if (ctx != null) {
            float px = ctx.getFoodPheromoneGradientX(ind.getX(), ind.getY(), ind.getZ());
            if (px > 0.1)
                foodDir = RLState.PheromoneDirection.RIGHT;
            else if (px < -0.1)
                foodDir = RLState.PheromoneDirection.LEFT;
        }

        RLState.PheromoneDirection homeDir = RLState.PheromoneDirection.NONE; // Similar logic for home

        return new RLState(hasFood, foodDir, homeDir, isAtNest, isLoaded);
    }

    public static QTable getQTable() {
        return Q_TABLE;
    }

    private double calculateReward(Individual ind, Action executedAction, ActionResult result) {
        double reward = -0.1; // Living cost

        if (executedAction.type() == Action.ActionType.DEPOSIT_FOOD && result.success()) {
            reward += 100.0;
        }
        if (executedAction.type() == Action.ActionType.FORAGE && ind.isCarryingFood()) { // Successfully picked up
            reward += 50.0;
        }

        return reward;
    }

    private Action convertAction(QTable.RLAction rlAction, Individual ind) {
        // Needs rudimentary movement logic (forward relative to heading)
        float speed = 1.0f;
        float dx = 0, dy = 0;

        switch (rlAction) {
            case MOVE_FORWARD -> {
                dx = (float) Math.cos(ind.getHeading()) * speed;
                dy = (float) Math.sin(ind.getHeading()) * speed;
                return Action.move(dx, dy, 0);
            }
            case TURN_LEFT -> {
                // We emulate turning by moving with a slight angle change?
                // No, Action.move updates heading if we give direction.
                // We need to calculate new direction vector.
                float newHeading = ind.getHeading() + 0.5f;
                dx = (float) Math.cos(newHeading) * speed;
                dy = (float) Math.sin(newHeading) * speed;
                return Action.move(dx, dy, 0);
            }
            case TURN_RIGHT -> {
                float newHeading = ind.getHeading() - 0.5f;
                dx = (float) Math.cos(newHeading) * speed;
                dy = (float) Math.sin(newHeading) * speed;
                return Action.move(dx, dy, 0);
            }
            case PICK_UP_FOOD -> {
                return Action.forage(); // Context will handle actual pickup if close
            }
            case DROP_FOOD -> {
                return new Action(Action.ActionType.DEPOSIT_FOOD, 0, 0, 0, 1.0f, null);
            }
            case WAIT -> {
                return Action.rest();
            }
        }
        return Action.rest();
    }

    @Override
    public void reset() {
        lastState = null;
        lastAction = null;
    }

    @Override
    public ReasoningArchitecture clone() {
        return new RLArchitecture(); // Q_TABLE is static, so shared
    }

}
