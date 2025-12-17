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
    private long stateEnteredTick = 0;
    private int stateTimeoutTicks = 200;

    @FunctionalInterface
    public interface StateHandler {
        Action handle(Individual individual, SimulationContext context, FSMArchitecture fsm);
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
    public void initialize(Individual individual) {
        currentState = State.IDLE;
        stateEnteredTick = 0;
    }

    @Override
    public Action decide(Individual individual, SimulationContext context) {
        StateHandler handler = stateHandlers.get(currentState);
        if (handler != null) {
            return handler.handle(individual, context, this);
        }
        return Action.rest();
    }

    @Override
    public void update(Individual individual, Action executedAction, ActionResult result) {
        // State timeout check
        if (context() - stateEnteredTick > stateTimeoutTicks) {
            transitionTo(State.IDLE);
        }
    }

    @Override
    public void reset() {
        currentState = State.IDLE;
        stateEnteredTick = 0;
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
            stateEnteredTick = context();
        }
    }

    public State getCurrentState() {
        return currentState;
    }

    private long context() {
        return System.currentTimeMillis(); // Placeholder - should use simulation tick
    }

    // === State Handlers ===

    private Action handleIdle(Individual ind, SimulationContext ctx, FSMArchitecture fsm) {
        // Check hunger
        if (ind.getEnergy() < 0.3f) {
            transitionTo(State.FORAGING);
            return Action.forage();
        }

        // Check fatigue
        if (ind.getEnergy() < 0.1f) {
            transitionTo(State.RESTING);
            return Action.rest();
        }

        // Default: start exploring
        transitionTo(State.EXPLORING);
        return randomMove();
    }

    private Action handleExploring(Individual ind, SimulationContext ctx, FSMArchitecture fsm) {
        // Check for food pheromone
        if (ctx != null) {
            java.util.Set<org.swarmforge.core.domain.ResourceType> types = (ind.getSpecies() != null)
                    ? ind.getSpecies().getForagingTypes()
                    : java.util.Set.of(org.swarmforge.core.domain.ResourceType.SEED);

            if (ctx.hasFoodNearby(ind, types)) {
                transitionTo(State.FORAGING);
                return Action.forage();
            }
        }

        // Check for enemies
        if (ctx != null && ctx.hasEnemyNearby(ind)) {
            if (ind.getCaste() == Individual.Caste.SOLDIER || ind.getJob() == Individual.Job.GUARD) {
                transitionTo(State.ATTACKING);
                return Action.attack(ctx.getNearestEnemy(ind));
            } else {
                transitionTo(State.FLEEING);
                return fleeHome(ind);
            }
        }

        return randomMove();
    }

    private Action handleForaging(Individual ind, SimulationContext ctx, FSMArchitecture fsm) {
        // If carrying food, return home
        if (ind.isCarryingFood()) {
            transitionTo(State.RETURNING_HOME);
            return Action.returnHome();
        }

        // Look for food
        if (ctx != null) {
            java.util.Set<org.swarmforge.core.domain.ResourceType> types = (ind.getSpecies() != null)
                    ? ind.getSpecies().getForagingTypes()
                    : java.util.Set.of(org.swarmforge.core.domain.ResourceType.SEED);

            float[] foodPos = ctx.getNearestFoodPosition(ind, types);
            if (foodPos != null) {
                // If close enough, pick it up
                float dx = foodPos[0] - ind.getX();
                float dy = foodPos[1] - ind.getY();
                if (dx * dx + dy * dy < 1.0f) {
                    // Pick up food
                    org.swarmforge.core.domain.FoodSource food = ((org.swarmforge.core.simulation.SimulationContextImpl) ctx)
                            .getNearestFood(ind, types);
                    if (food != null) {
                        ind.setCarriedItem(Individual.CarriedItem.FOOD);
                        ind.setCarriedResourceType(food.getType());
                        food.take(1.0f);
                        transitionTo(State.RETURNING_HOME);
                        return Action.returnHome();
                    }
                }
                // Move towards food
                return Action.move(dx, dy, 0);
            }

            // Pheromone usage (generic fallback)
            float px = ctx.getFoodPheromoneGradientX(ind.getX(), ind.getY(), ind.getZ());
            float py = ctx.getFoodPheromoneGradientY(ind.getX(), ind.getY(), ind.getZ());
            if (Math.abs(px) > 0.01f || Math.abs(py) > 0.01f) {
                return Action.followTrail(px, py, 0);
            }
        }

        return randomMove();
    }

    private Action handleReturningHome(Individual ind, SimulationContext ctx, FSMArchitecture fsm) {
        // Check if home
        if (isNearHome(ind)) {
            transitionTo(State.DEPOSITING);
            return new Action(Action.ActionType.DEPOSIT_FOOD, 0, 0, 0, 1.0f, null);
        }

        return Action.returnHome();
    }

    private Action handleDepositing(Individual ind, SimulationContext ctx, FSMArchitecture fsm) {
        // Done depositing, go back to foraging or exploring
        if (!ind.isCarryingFood()) {
            transitionTo(State.FORAGING);
            return Action.forage();
        }
        return new Action(Action.ActionType.DEPOSIT_FOOD, 0, 0, 0, 1.0f, null);
    }

    private Action handleAttacking(Individual ind, SimulationContext ctx, FSMArchitecture fsm) {
        if (ctx == null || !ctx.hasEnemyNearby(ind)) {
            transitionTo(State.EXPLORING);
            return randomMove();
        }
        return Action.attack(ctx.getNearestEnemy(ind));
    }

    private Action handleFleeing(Individual ind, SimulationContext ctx, FSMArchitecture fsm) {
        if (isNearHome(ind)) {
            transitionTo(State.IDLE);
            return Action.rest();
        }
        return fleeHome(ind);
    }

    private Action handleResting(Individual ind, SimulationContext ctx, FSMArchitecture fsm) {
        if (ind.getEnergy() > 0.8f) {
            transitionTo(State.IDLE);
            return Action.rest();
        }
        return Action.rest();
    }

    private Action randomMove() {
        float angle = (float) (Math.random() * Math.PI * 2);
        return Action.move((float) Math.cos(angle), (float) Math.sin(angle), 0);
    }

    private Action fleeHome(Individual ind) {
        // Move toward home
        float dx = ind.getHomeX() - ind.getX();
        float dy = ind.getHomeY() - ind.getY();
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 0) {
            dx /= len;
            dy /= len;
        }
        return Action.move(dx, dy, 0);
    }

    private boolean isNearHome(Individual ind) {
        float dx = ind.getHomeX() - ind.getX();
        float dy = ind.getHomeY() - ind.getY();
        return (dx * dx + dy * dy) < 9; // Within 3 units
    }
}
