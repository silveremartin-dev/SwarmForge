/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.simulation.SimulationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * Behavior Tree implementation for ant behavior.
 * Hierarchical organization of tasks (Sequence, Selector, Leaf).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class BehaviorTreeArchitecture implements ReasoningArchitecture {

    private Node root;

    public BehaviorTreeArchitecture() {
        buildTree();
    }

    private void buildTree() {
        // Root Selector: Priority list
        Selector rootSelector = new Selector();

        // 1. Survival Sequence
        Sequence survival = new Sequence();
        survival.addChild(new Condition((ind, ctx) -> ind.getEnergy() < 0.2f));
        survival.addChild(new Task(Action.ActionType.REST));
        rootSelector.addChild(survival);

        // 2. Defense Sequence
        Sequence defense = new Sequence();
        defense.addChild(new Condition((ind, ctx) -> ctx != null && ctx.hasEnemyNearby(ind)));
        defense.addChild(new Selector() {
            {
                addChild(new Sequence() {
                    {
                        addChild(new Condition((ind, ctx) -> ind.getCaste() == Individual.Caste.SOLDIER));
                        addChild(new Task(Action.ActionType.ATTACK));
                    }
                });
                addChild(new Task(Action.ActionType.FLEE)); // Non-soldiers flee
            }
        });
        rootSelector.addChild(defense);

        // 3. Foraging Sequence
        Sequence foraging = new Sequence();
        foraging.addChild(new Condition((ind, ctx) -> !ind.isCarryingFood()));
        foraging.addChild(new Selector() {
            {
                // Sub-selector: Found food? Follow trail? Random?
                addChild(new Sequence() {
                    {
                        addChild(new Condition((ind, ctx) -> ctx != null && ctx.hasFoodNearby(ind)));
                        addChild(new Task(Action.ActionType.FORAGE)); // Pick up
                    }
                });
                addChild(new Sequence() {
                    {
                        addChild(new Condition((ind, ctx) -> ctx != null
                                && ctx.getFoodPheromone(ind.getX(), ind.getY(), ind.getZ()) > 0.1f));
                        addChild(new Task(Action.ActionType.FOLLOW_TRAIL));
                    }
                });
                addChild(new Task(Action.ActionType.EXPLORE));
            }
        });
        rootSelector.addChild(foraging);

        // 4. Return Home Sequence
        Sequence returnHome = new Sequence();
        returnHome.addChild(new Condition((ind, ctx) -> ind.isCarryingFood()));
        returnHome.addChild(new Selector() {
            {
                addChild(new Sequence() {
                    {
                        addChild(new Condition((ind, ctx) -> isAtNext(ind)));
                        addChild(new Task(Action.ActionType.DEPOSIT_FOOD));
                    }
                });
                addChild(new Task(Action.ActionType.RETURN_HOME));
            }
        });
        rootSelector.addChild(returnHome);

        // Default
        rootSelector.addChild(new Task(Action.ActionType.REST));

        this.root = rootSelector;
    }

    private boolean isAtNext(Individual ind) {
        return (ind.getHomeX() - ind.getX()) * (ind.getHomeX() - ind.getX()) +
                (ind.getHomeY() - ind.getY()) * (ind.getHomeY() - ind.getY()) < 4.0;
    }

    @Override
    public ArchitectureType getType() {
        return ArchitectureType.BEHAVIOR_TREE;
    }

    @Override
    public String getName() {
        return "Behavior Tree";
    }

    @Override
    public void initialize(Individual individual) {
        // Stateless nodes for now
    }

    @Override
    public Action decide(Individual individual, SimulationContext context) {
        NodeStatus status = root.tick(individual, context);
        if (status instanceof NodeStatus.Running running) {
            return running.action;
        } else if (status instanceof NodeStatus.Success success) {
            return success.action != null ? success.action : Action.rest();
        }
        return Action.rest();
    }

    @Override
    public void update(Individual individual, Action executedAction, ActionResult result) {
        // No state update needed for simple stateless tree
    }

    @Override
    public void reset() {
        // No state
    }

    @Override
    public ReasoningArchitecture clone() {
        return new BehaviorTreeArchitecture();
    }

    // === Tree Nodes ===

    interface Node {
        NodeStatus tick(Individual ind, SimulationContext ctx);
    }

    interface NodeStatus {
        record Success(Action action) implements NodeStatus {
        }

        record Failure() implements NodeStatus {
        }

        record Running(Action action) implements NodeStatus {
        }
    }

    static class Task implements Node {
        private final Action.ActionType type;

        public Task(Action.ActionType type) {
            this.type = type;
        }

        @Override
        public NodeStatus tick(Individual ind, SimulationContext ctx) {
            Action action = switch (type) {
                case MOVE, EXPLORE -> randomMove();
                case ATTACK -> Action.attack(ctx != null ? ctx.getNearestEnemy(ind) : null);
                case FOLLOW_TRAIL -> followTrail(ind, ctx);
                case FORAGE -> Action.forage();
                case RETURN_HOME -> Action.returnHome();
                case DEPOSIT_FOOD -> new Action(Action.ActionType.DEPOSIT_FOOD, 0, 0, 0, 1f, null);
                case FLEE -> flee(ind);
                default -> Action.rest();
            };
            return new NodeStatus.Success(action);
        }

        private Action randomMove() {
            float angle = (float) (Math.random() * Math.PI * 2);
            return Action.move((float) Math.cos(angle), (float) Math.sin(angle), 0);
        }

        private Action followTrail(Individual ind, SimulationContext ctx) {
            if (ctx == null)
                return randomMove();
            return Action.followTrail(ctx.getFoodPheromoneGradientX(ind.getX(), ind.getY(), ind.getZ()),
                    ctx.getFoodPheromoneGradientY(ind.getX(), ind.getY(), ind.getZ()), 0);
        }

        private Action flee(Individual ind) {
            // Run away from home? No, usually flee TO home.
            // Actually flee typically means run away from danger, or run to safety (home).
            // Let's assume flee to home.
            return Action.returnHome();
        }
    }

    static class Condition implements Node {
        private final BiPredicate<Individual, SimulationContext> predicate;

        public Condition(BiPredicate<Individual, SimulationContext> predicate) {
            this.predicate = predicate;
        }

        @Override
        public NodeStatus tick(Individual ind, SimulationContext ctx) {
            return predicate.test(ind, ctx) ? new NodeStatus.Success(null) : new NodeStatus.Failure();
        }
    }

    static class Sequence implements Node {
        private final List<Node> children = new ArrayList<>();

        public void addChild(Node child) {
            children.add(child);
        }

        @Override
        public NodeStatus tick(Individual ind, SimulationContext ctx) {
            for (Node child : children) {
                NodeStatus status = child.tick(ind, ctx);
                if (status instanceof NodeStatus.Failure)
                    return new NodeStatus.Failure();
                if (status instanceof NodeStatus.Running)
                    return status;
                // If Success, continue to next.
                // Note: The LAST success determines the action.
                // Or maybe Sequence succeeds if all succeed. Ideally, a sequence executes one
                // by one.
                // For a decision tree per tick, if a condition succeeds, we go to next.
                // If a task succeeds, we return that action.
                if (status instanceof NodeStatus.Success s && s.action != null) {
                    return s;
                }
            }
            return new NodeStatus.Success(null); // All conditions passed but no action?
        }
    }

    static class Selector implements Node {
        private final List<Node> children = new ArrayList<>();

        public void addChild(Node child) {
            children.add(child);
        }

        @Override
        public NodeStatus tick(Individual ind, SimulationContext ctx) {
            for (Node child : children) {
                NodeStatus status = child.tick(ind, ctx);
                if (status instanceof NodeStatus.Success)
                    return status;
                if (status instanceof NodeStatus.Running)
                    return status;
            }
            return new NodeStatus.Failure();
        }
    }
}
