/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.event;

import java.time.Instant;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

/**
 * Base class for all simulation events.
 * Events are immutable and timestamped for logging and replay.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SimulationEvent {

    public enum EventType {
        // Colony Events
        COLONY_FOUNDED,
        COLONY_DESTROYED,
        QUEEN_BORN,
        QUEEN_DIED,
        WORKER_BORN,
        WORKER_DIED,
        SOLDIER_BORN,
        SOLDIER_DIED,

        // Resource Events
        FOOD_DISCOVERED,
        FOOD_DEPLETED,
        NEST_EXPANDED,
        NEST_DAMAGED,

        // Interaction Events
        RAID_STARTED,
        RAID_ENDED,
        TERRITORY_CLAIMED,
        COMBAT_OCCURRED,

        // Environmental Events
        WEATHER_CHANGED,
        DISASTER_OCCURRED,
        SEASON_CHANGED,

        // Simulation Events
        SIMULATION_STARTED,
        SIMULATION_PAUSED,
        SIMULATION_STOPPED,
        TICK_COMPLETED,
        MILESTONE_REACHED,
        GOD_MODE_INTERVENTION,
        // Generic/Legacy Types
        INFO,
        BIRTH,
        DEATH,
        SYSTEM,
        DEBUG,
        ERROR,
        WARNING
    }

    public enum Severity {
        INFO,
        WARNING,
        CRITICAL
    }

    private static final java.util.concurrent.atomic.AtomicLong SEQUENCE_GENERATOR = new java.util.concurrent.atomic.AtomicLong(1);

    private UUID id;
    private long sequenceId;
    private EventType type;
    private Severity severity;
    private long tick;
    private Instant timestamp;
    private String message;
    private Map<String, Object> data;

    private static final org.swarmforge.core.util.ObjectPool<SimulationEvent> POOL = new org.swarmforge.core.util.ObjectPool<>(
            SimulationEvent::new, 1000, 10000);

    // Private constructor for pooling
    private SimulationEvent() {
        this.data = new HashMap<>();
    }

    /**
     * Obtains an event instance from the pool.
     */
    public static SimulationEvent obtain(EventType type, Severity severity, long tick, String message,
            Map<String, Object> data) {
        SimulationEvent event = POOL.borrow();
        event.init(type, severity, tick, message, data);
        return event;
    }

    public void recycle() {
        this.data.clear();
        this.message = null;
        this.type = null;
        POOL.recycle(this);
    }

    private void init(EventType type, Severity severity, long tick, String message, Map<String, Object> data) {
        this.id = UUID.randomUUID();
        this.sequenceId = SEQUENCE_GENERATOR.getAndIncrement();
        this.type = type;
        this.severity = severity;
        this.tick = tick;
        this.timestamp = Instant.now();
        this.message = message;
        if (data != null) {
            this.data.putAll(data);
        }
    }

    // kept for legacy/testing compatibility, but delegates to init logic if
    // possible or just new
    // Actually, to enforce pooling, we should deprecate public constructors or
    // redirect them.
    // For now, let's keep public constructor as "unpooled" to avoid breaking ALL
    // code,
    // but update static factories to use pool.
    public SimulationEvent(EventType type, long tick, String message) {
        this(type, Severity.INFO, tick, message, new HashMap<>());
    }

    public SimulationEvent(EventType type, Severity severity, long tick, String message, Map<String, Object> data) {
        this.data = new HashMap<>(); // Ensure map exists
        init(type, severity, tick, message, data);
    }

    // Factory methods for common events
    public static SimulationEvent colonyFounded(long tick, String colonyId, String species, int x, int y) {
        Map<String, Object> data = new HashMap<>();
        data.put("colonyId", colonyId);
        data.put("species", species);
        data.put("x", x);
        data.put("y", y);
        return obtain(EventType.COLONY_FOUNDED, Severity.INFO, tick,
                "Colony '" + species + "' founded at (" + x + ", " + y + ")", data);
    }

    public static SimulationEvent queenDied(long tick, String colonyId, String species, String cause) {
        Map<String, Object> data = new HashMap<>();
        data.put("colonyId", colonyId);
        data.put("species", species);
        data.put("cause", cause);
        return obtain(EventType.QUEEN_DIED, Severity.CRITICAL, tick,
                "Queen of '" + species + "' died: " + cause, data);
    }

    public static SimulationEvent raidStarted(long tick, String attackerId, String defenderId) {
        Map<String, Object> data = new HashMap<>();
        data.put("attackerId", attackerId);
        data.put("defenderId", defenderId);
        return obtain(EventType.RAID_STARTED, Severity.WARNING, tick,
                "Raid: Colony " + attackerId + " attacking " + defenderId, data);
    }

    public static SimulationEvent disasterOccurred(long tick, String disasterType, float severity, int affectedArea) {
        Map<String, Object> data = new HashMap<>();
        data.put("disasterType", disasterType);
        data.put("severity", severity);
        data.put("affectedArea", affectedArea);
        return obtain(EventType.DISASTER_OCCURRED, Severity.CRITICAL, tick,
                disasterType + " occurred! Severity: " + severity, data);
    }

    public static SimulationEvent foodDiscovered(long tick, String colonyId, int x, int y, float amount) {
        Map<String, Object> data = new HashMap<>();
        data.put("colonyId", colonyId);
        data.put("x", x);
        data.put("y", y);
        data.put("amount", amount);
        return obtain(EventType.FOOD_DISCOVERED, Severity.INFO, tick,
                "Food source discovered at (" + x + ", " + y + "): " + amount + " units", data);
    }

    public static SimulationEvent milestoneReached(long tick, String milestone, Object value) {
        Map<String, Object> data = new HashMap<>();
        data.put("milestone", milestone);
        data.put("value", value);
        return obtain(EventType.MILESTONE_REACHED, Severity.INFO, tick,
                "Milestone: " + milestone + " = " + value, data);
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public long getSequenceId() {
        return sequenceId;
    }

    public EventType getType() {
        return type;
    }

    public Severity getSeverity() {
        return severity;
    }

    public long getTick() {
        return tick;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, Object> getData() {
        return new HashMap<>(data);
    }

    @SuppressWarnings("unchecked")
    public <T> T getData(String key) {
        return (T) data.get(key);
    }

    @Override
    public String toString() {
        return String.format("[%s] Tick %d: %s - %s", severity, tick, type, message);
    }
}
