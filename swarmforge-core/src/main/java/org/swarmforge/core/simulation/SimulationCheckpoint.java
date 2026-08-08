/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.event.GodModeIntervention;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates a simulation checkpoint containing both the physical state snapshot
 * and the recorded God Mode intervention journal up to that tick.
 * Guarantees full deterministic reproducibility of custom simulation runs.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class SimulationCheckpoint implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final String name;
    private final long tick;
    private final long timestamp;
    private final SimulationSnapshot snapshot;
    private final List<GodModeIntervention> interventionsRecorded;

    public SimulationCheckpoint(String name, long tick, SimulationSnapshot snapshot, List<GodModeIntervention> interventionsRecorded) {
        this.id = java.util.UUID.randomUUID().toString();
        this.name = name != null && !name.trim().isEmpty() ? name : "Checkpoint @ Tick #" + tick;
        this.tick = tick;
        this.timestamp = System.currentTimeMillis();
        this.snapshot = snapshot;
        this.interventionsRecorded = interventionsRecorded != null ? new ArrayList<>(interventionsRecorded) : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getTick() {
        return tick;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public SimulationSnapshot getSnapshot() {
        return snapshot;
    }

    public List<GodModeIntervention> getInterventionsRecorded() {
        return new ArrayList<>(interventionsRecorded);
    }

    public byte[] toCompressedBytes() throws java.io.IOException {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try (java.util.zip.GZIPOutputStream gzos = new java.util.zip.GZIPOutputStream(baos);
             java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(gzos)) {
            oos.writeObject(this);
            oos.flush();
        }
        return baos.toByteArray();
    }

    public static SimulationCheckpoint fromCompressedBytes(byte[] compressedData) throws java.io.IOException, ClassNotFoundException {
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(compressedData);
        try (java.util.zip.GZIPInputStream gzis = new java.util.zip.GZIPInputStream(bais);
             java.io.ObjectInputStream ois = new java.io.ObjectInputStream(gzis)) {
            return (SimulationCheckpoint) ois.readObject();
        }
    }

    @Override
    public String toString() {
        return String.format("%s (Tick #%d, %d Interventions)", name, tick, interventionsRecorded.size());
    }
}
