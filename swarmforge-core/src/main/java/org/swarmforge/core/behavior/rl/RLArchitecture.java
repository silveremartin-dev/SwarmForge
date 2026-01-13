/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior.rl;

import org.swarmforge.core.behavior.ReasoningArchitecture;
import org.swarmforge.core.behavior.AgentView; // New Interface

import org.swarmforge.core.simulation.SimulationContext;

/**
 * Reinforcement Learning Architecture using Q-Learning.
 * Refactored to use AgentView for ECS compatibility.
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
    public void initialize(AgentView agent) {
        // Reset transient state
        lastState = null;
        lastAction = null;
    }

    // === ECS / AgentView Compatible Methods ===

    public Action decide(AgentView agent, SimulationContext context) {
        // 1. Observe State
        RLState currentState = observeState(agent, context);

        // 0. Learn from previous action (S, A, R, S')
        performLearningUpdate(currentState);

        // 2. Choose Action from QTable
        QTable.RLAction rlAction = Q_TABLE.chooseAction(currentState);

        // 3. Store for update
        this.lastState = currentState;
        this.lastAction = rlAction;

        // 4. Execute (Convert RLAction to Game Action)
        return convertAction(rlAction, agent);
    }


    @Override
    public void update(AgentView agent, Action executedAction, ActionResult result) {
         if (lastState != null && lastAction != null) {
            double reward = calculateReward(agent, executedAction, result);
            this.currentReward = reward;
        }
    }

    private void performLearningUpdate(RLState nextState) {
        if (lastState != null && lastAction != null) {
            Q_TABLE.update(lastState, lastAction, currentReward, nextState);
        }
    }
    
    public static QTable getQTable() {
        return Q_TABLE;
    }

    private RLState observeState(AgentView ind, SimulationContext ctx) {
        // Simple discrete state observation
        boolean hasFood = ind.isCarryingFood();
        boolean isAtNest = ind.isAtNest(); // Use Interface method
        boolean isLoaded = ind.isCarryingFood();

        // Pheromones (Simplified) - Need position
        RLState.PheromoneDirection foodDir = RLState.PheromoneDirection.NONE;

        if (ctx != null) {
            float px = ctx.getFoodPheromoneGradientX(ind.getX(), ind.getY(), ind.getZ());
            if (px > 0.1)
                foodDir = RLState.PheromoneDirection.RIGHT;
            else if (px < -0.1)
                foodDir = RLState.PheromoneDirection.LEFT;
        }

        RLState.PheromoneDirection homeDir = RLState.PheromoneDirection.NONE; // Similar logic

        return new RLState(hasFood, foodDir, homeDir, isAtNest, isLoaded);
    }
    
    private double calculateReward(AgentView ind, Action executedAction, ActionResult result) {
         double reward = -0.1; // Living cost

        if (executedAction.type() == Action.ActionType.DEPOSIT_FOOD && result.success()) {
            reward += 100.0;
        }
        if (executedAction.type() == Action.ActionType.FORAGE && ind.isCarryingFood()) { // Successfully picked up
            reward += 50.0;
        }

        return reward;
    }
    

    private Action convertAction(QTable.RLAction rlAction, AgentView ind) {
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
