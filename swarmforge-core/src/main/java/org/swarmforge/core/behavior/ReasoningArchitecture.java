/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior;


import org.swarmforge.core.simulation.SimulationContext;

/**
 * Base interface for ant behavior reasoning architectures.
 * Implementations include FSM, Neural Network, Fuzzy Logic, and Behavior Trees.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public interface ReasoningArchitecture extends java.io.Serializable {

    /**
     * Architecture type identifier.
     */
    enum ArchitectureType {
        BEHAVIOR_TREE("Behavior Tree (Tree-based AI)"),
        BLACKBOARD("Blackboard System (Shared Knowledge Base)"),
        FINITE_STATE_MACHINE("Finite State Machine (FSM)"),
        BDI("BDI (Belief-Desire-Intention)"),
        FUZZY_LOGIC("Fuzzy Logic Engine"),
        HYBRID("Hybrid Architecture (Combined)"),
        NEURAL_NETWORK("Spiking Neural Network (SNN / ANN)");

        private final String displayName;

        ArchitectureType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static ArchitectureType parse(String val) {
            if (val == null || val.trim().isEmpty()) return BEHAVIOR_TREE;
            String s = val.toUpperCase().trim();
            if (s.contains("BDI")) return BDI;
            if (s.contains("BLACKBOARD") || s.contains("TABLEAU")) return BLACKBOARD;
            if (s.contains("FINITE") || s.contains("FSM") || s.contains("AUTOMATE")) return FINITE_STATE_MACHINE;
            if (s.contains("FUZZY") || s.contains("FLOU")) return FUZZY_LOGIC;
            if (s.contains("NEURAL") || s.contains("SNN") || s.contains("ANN") || s.contains("NEURONE")) return NEURAL_NETWORK;
            if (s.contains("HYBRID") || s.contains("HYBRIDE")) return HYBRID;
            if (s.contains("BEHAVIOR") || s.contains("TREE") || s.contains("ARBRE")) return BEHAVIOR_TREE;
            for (ArchitectureType t : values()) {
                if (t.name().equalsIgnoreCase(val) || t.getDisplayName().equalsIgnoreCase(val)) {
                    return t;
                }
            }
            return BEHAVIOR_TREE;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    /**
     * Get the architecture type.
     */
    ArchitectureType getType();

    /**
     * Get human-readable name.
     */
    String getName();

    /**
     * Initialize the architecture for a specific individual.
     */
    void initialize(AgentView agent);

    /**
     * Compute the next action for the individual.
     * 
     * @param agent   The ant making the decision
     * @param context Current simulation context (neighbors, pheromones, etc.)
     * @return The action to execute
     */
    Action decide(AgentView agent, SimulationContext context);

    /**
     * Update internal state after action execution.
     */
    void update(AgentView agent, Action executedAction, ActionResult result);

    /**
     * Reset the architecture state.
     */
    void reset();

    /**
     * Clone this architecture for a new individual.
     */
    ReasoningArchitecture clone();

    /**
     * Action that can be performed by an ant.
     */
    record Action(
            ActionType type,
            float directionX,
            float directionY,
            float directionZ,
            float intensity,
            Object target) {
        public enum ActionType {
            MOVE,
            FORAGE,
            RETURN_HOME,
            DEPOSIT_FOOD,
            DEPOSIT_PHEROMONE,
            ATTACK,
            FLEE,
            GROOM,
            NURSE,
            REST,
            EXPLORE,
            FOLLOW_TRAIL,
            COMMUNICATE,
            ABORT_AND_RETURN
        }

        public static Action move(float dx, float dy, float dz) {
            return new Action(ActionType.MOVE, dx, dy, dz, 1.0f, null);
        }

        public static Action attack(Object target) {
            return new Action(ActionType.ATTACK, 0, 0, 0, 1.0f, target);
        }

        public static Action forage() {
            return new Action(ActionType.FORAGE, 0, 0, 0, 1.0f, null);
        }

        public static Action returnHome() {
            return new Action(ActionType.RETURN_HOME, 0, 0, 0, 1.0f, null);
        }

        public static Action abortAndReturn() {
            return new Action(ActionType.ABORT_AND_RETURN, 0, 0, 0, 1.0f, null);
        }

        public static Action rest() {
            return new Action(ActionType.REST, 0, 0, 0, 1.0f, null);
        }

        public static Action followTrail(float dx, float dy, float dz) {
            return new Action(ActionType.FOLLOW_TRAIL, dx, dy, dz, 1.0f, null);
        }

        public static Action depositFood() {
            return new Action(ActionType.DEPOSIT_FOOD, 0, 0, 0, 1.0f, null);
        }
    }

    /**
     * Result of executing an action.
     */
    record ActionResult(
            boolean success,
            float reward,
            String message) {
        public static ActionResult ok() {
            return new ActionResult(true, 1.0f, "OK");
        }

        public static ActionResult failure(String reason) {
            return new ActionResult(false, -1.0f, reason);
        }
    }
}
