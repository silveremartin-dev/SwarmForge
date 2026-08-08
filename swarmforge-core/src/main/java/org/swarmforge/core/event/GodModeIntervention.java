/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.event;

import java.io.Serializable;

/**
 * Encapsulates a God Mode intervention performed by the user at a specific simulation tick.
 * Stored in the simulation intervention journal to guarantee exact deterministic replay
 * and checkpoint restoration.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public record GodModeIntervention(
    long tick,
    long timestamp,
    ActionType actionType,
    String targetColony,
    String caste,
    int count,
    float x, float y, float z,
    float amount,
    String disasterType,
    float intensity,
    String paramName,
    Object paramValue
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum ActionType {
        SPAWN_ANTS,
        KILL_ANTS,
        SPAWN_FOOD,
        TRIGGER_DISASTER,
        STOP_DISASTERS,
        MODIFY_PARAMETER
    }

    public static GodModeIntervention spawnAnts(long tick, String targetColony, String caste, int count, float x, float y, float z) {
        return new GodModeIntervention(tick, System.currentTimeMillis(), ActionType.SPAWN_ANTS, targetColony, caste, count, x, y, z, 0f, null, 0f, null, null);
    }

    public static GodModeIntervention killAnts(long tick, String targetColony, String caste, int count) {
        return new GodModeIntervention(tick, System.currentTimeMillis(), ActionType.KILL_ANTS, targetColony, caste, count, 0f, 0f, 0f, 0f, null, 0f, null, null);
    }

    public static GodModeIntervention spawnFood(long tick, float x, float y, float z, float amount) {
        return new GodModeIntervention(tick, System.currentTimeMillis(), ActionType.SPAWN_FOOD, null, null, 0, x, y, z, amount, null, 0f, null, null);
    }

    public static GodModeIntervention triggerDisaster(long tick, String disasterType, float intensity) {
        return new GodModeIntervention(tick, System.currentTimeMillis(), ActionType.TRIGGER_DISASTER, null, null, 0, 0f, 0f, 0f, 0f, disasterType, intensity, null, null);
    }

    public static GodModeIntervention stopDisasters(long tick) {
        return new GodModeIntervention(tick, System.currentTimeMillis(), ActionType.STOP_DISASTERS, null, null, 0, 0f, 0f, 0f, 0f, null, 0f, null, null);
    }

    public static GodModeIntervention modifyParameter(long tick, String paramName, Object paramValue) {
        return new GodModeIntervention(tick, System.currentTimeMillis(), ActionType.MODIFY_PARAMETER, null, null, 0, 0f, 0f, 0f, 0f, null, 0f, paramName, paramValue);
    }
}
