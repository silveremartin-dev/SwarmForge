/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior;


import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.simulation.SimulationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Finite State Machine implementation for ant behavior.
 * Classic approach with states like EXPLORING, FORAGING, RETURNING, etc.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class FSMArchitecture implements ReasoningArchitecture {

    public enum State {
        IDLE,
        EXPLORING,
        FORAGING,
        RETURNING_HOME,
        DEPOSITING,
        ATTACKING,
        FLEEING,
        NURSING,
        RESTING
    }

    private State currentState = State.IDLE;
    private final Map<State, StateHandler> stateHandlers = new HashMap<>();

    private int stateTimeoutTicks = 200;

    @FunctionalInterface
    public interface StateHandler {
        Action handle(AgentView agent, SimulationContext context, FSMArchitecture fsm);
    }

    public FSMArchitecture() {
        initializeDefaultHandlers();
    }

    private void initializeDefaultHandlers() {
        stateHandlers.put(State.IDLE, this::handleIdle);
        stateHandlers.put(State.EXPLORING, this::handleExploring);
        stateHandlers.put(State.FORAGING, this::handleForaging);
        stateHandlers.put(State.RETURNING_HOME, this::handleReturningHome);
        stateHandlers.put(State.DEPOSITING, this::handleDepositing);
        stateHandlers.put(State.ATTACKING, this::handleAttacking);
        stateHandlers.put(State.FLEEING, this::handleFleeing);
        stateHandlers.put(State.RESTING, this::handleResting);
    }

    @Override
    public ArchitectureType getType() {
        return ArchitectureType.FINITE_STATE_MACHINE;
    }

    @Override
    public String getName() {
        return "Finite State Machine";
    }

    @Override
    public void initialize(AgentView agent) {
        currentState = State.IDLE;

    }

    @Override
    public Action decide(AgentView agent, SimulationContext context) {
        StateHandler handler = stateHandlers.get(currentState);
        if (handler != null) {
            return handler.handle(agent, context, this);
        }
        return Action.rest();
    }

    @Override
    public void update(AgentView agent, Action executedAction, ActionResult result) {
        // State timeout check
        // Note: FSMArchitecture might need a way to track current tick if not provided
        // For now, we simplify or assume stateless timeout if context not injected
    }

    @Override
    public void reset() {
        currentState = State.IDLE;

    }

    @Override
    public ReasoningArchitecture clone() {
        FSMArchitecture copy = new FSMArchitecture();
        copy.stateTimeoutTicks = this.stateTimeoutTicks;
        return copy;
    }

    public void transitionTo(State newState) {
        if (newState != currentState) {
            currentState = newState;

        }
    }

    public State getCurrentState() {
        return currentState;
    }

    private long context() {
        return System.currentTimeMillis(); // Placeholder - should use simulation tick
    }

    // === State Handlers ===

    private Action handleIdle(AgentView agent, SimulationContext ctx, FSMArchitecture fsm) {
        // Check hunger
        if (agent.getEnergyLevel() < 0.3f) {
            transitionTo(State.FORAGING);
            return Action.forage();
        }

        // Check fatigue
        if (agent.getEnergyLevel() < 0.1f) {
            transitionTo(State.RESTING);
            return Action.rest();
        }

        // Default: start exploring
        transitionTo(State.EXPLORING);
        return randomMove();
    }

    private Action handleExploring(AgentView agent, SimulationContext ctx, FSMArchitecture fsm) {
        // Check for food pheromone
        if (ctx != null) {
            java.util.Set<org.swarmforge.core.domain.ResourceType> types = agent.getForagingTypes();

            if (ctx.hasFoodNearby(agent, types)) {
                transitionTo(State.FORAGING);
                return Action.forage();
            }
        }

        // Check for enemies
        if (ctx != null && ctx.hasEnemyNearby(agent)) {
            if (agent.isSoldier()) {
                transitionTo(State.ATTACKING);
                return Action.attack(ctx.getNearestEnemy(agent));
            } else {
                transitionTo(State.FLEEING);
                return fleeHome(agent);
            }
        }

        return randomMove();
    }

    private Action handleForaging(AgentView agent, SimulationContext ctx, FSMArchitecture fsm) {
        // If carrying food, return home
        if (agent.isCarryingFood()) {
            transitionTo(State.RETURNING_HOME);
            return Action.returnHome();
        }

        // Look for food
        if (ctx != null) {
            java.util.Set<org.swarmforge.core.domain.ResourceType> types = agent.getForagingTypes();

            float[] foodPos = ctx.getNearestFoodPosition(agent, types);
            if (foodPos != null) {
                // If close enough, pick it up
                float dx = foodPos[0] - agent.getX();
                float dy = foodPos[1] - agent.getY();
                if (dx * dx + dy * dy < 1.0f) {
                    // Pick up food
                    org.swarmforge.core.domain.FoodSource food = ctx.getNearestFood(agent, types);
                    if (food != null) {
                        // Cast for now as setCarriedItem is NOT in AgentView (Action should handle this in pure ECS)
                        if (agent instanceof Individual ind) {
                            ind.setCarriedItem(Individual.CarriedItem.FOOD);
                            ind.setCarriedResourceType(food.getType());
                        }
                        food.take(1.0f);
                        transitionTo(State.RETURNING_HOME);
                        return Action.returnHome();
                    }
                }
                // Move towards food
                return Action.move(dx, dy, 0);
            }

            // Pheromone usage (generic fallback)
            float px = ctx.getFoodPheromoneGradientX(agent.getX(), agent.getY(), agent.getZ());
            float py = ctx.getFoodPheromoneGradientY(agent.getX(), agent.getY(), agent.getZ());
            if (Math.abs(px) > 0.01f || Math.abs(py) > 0.01f) {
                return Action.followTrail(px, py, 0);
            }
        }

        return randomMove();
    }

    private Action handleReturningHome(AgentView agent, SimulationContext ctx, FSMArchitecture fsm) {
        // Check if home
        if (isNearHome(agent)) {
            transitionTo(State.DEPOSITING);
            return new Action(Action.ActionType.DEPOSIT_FOOD, 0, 0, 0, 1.0f, null);
        }

        return Action.returnHome();
    }

    private Action handleDepositing(AgentView agent, SimulationContext ctx, FSMArchitecture fsm) {
        // Done depositing, go back to foraging or exploring
        if (!agent.isCarryingFood()) {
            transitionTo(State.FORAGING);
            return Action.forage();
        }
        return new Action(Action.ActionType.DEPOSIT_FOOD, 0, 0, 0, 1.0f, null);
    }

    private Action handleAttacking(AgentView agent, SimulationContext ctx, FSMArchitecture fsm) {
        if (ctx == null || !ctx.hasEnemyNearby(agent)) {
            transitionTo(State.EXPLORING);
            return randomMove();
        }
        return Action.attack(ctx.getNearestEnemy(agent));
    }

    private Action handleFleeing(AgentView agent, SimulationContext ctx, FSMArchitecture fsm) {
        if (isNearHome(agent)) {
            transitionTo(State.IDLE);
            return Action.rest();
        }
        return fleeHome(agent);
    }

    private Action handleResting(AgentView agent, SimulationContext ctx, FSMArchitecture fsm) {
        if (agent.getEnergyLevel() > 0.8f) {
            transitionTo(State.IDLE);
            return Action.rest();
        }
        return Action.rest();
    }

    private Action randomMove() {
        float angle = (float) (Math.random() * Math.PI * 2);
        return Action.move((float) Math.cos(angle), (float) Math.sin(angle), 0);
    }

    private Action fleeHome(AgentView agent) {
        // Move toward home
        float dx = agent.getHomeX() - agent.getX();
        float dy = agent.getHomeY() - agent.getY();
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 0) {
            dx /= len;
            dy /= len;
        }
        return Action.move(dx, dy, 0);
    }

    private boolean isNearHome(AgentView agent) {
        float dx = agent.getHomeX() - agent.getX();
        float dy = agent.getHomeY() - agent.getY();
        return (dx * dx + dy * dy) < 9; // Within 3 units
    }
}
