/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import java.util.concurrent.locks.ReentrantLock;

/**
 * History buffer for simulation rewind functionality.
 * Uses a ring buffer to store recent snapshots efficiently.
 *
 * <p>
 * Features:
 * </p>
 * <ul>
 * <li>Fixed-size ring buffer (constant memory)</li>
 * <li>Configurable snapshot interval</li>
 * <li>Thread-safe operations</li>
 * <li>Seek to any stored tick</li>
 * </ul>
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class SimulationHistory {

    private final SimulationSnapshot[] buffer;
    private final int capacity;
    private int head = 0;
    private int count = 0;
    private final ReentrantLock lock = new ReentrantLock();

    private final int snapshotInterval;
    private long lastSnapshotTick = -1;

    /**
     * Create a history buffer.
     * 
     * @param capacity         Maximum number of snapshots to store
     * @param snapshotInterval Ticks between snapshots
     */
    public SimulationHistory(int capacity, int snapshotInterval) {
        this.capacity = capacity;
        this.buffer = new SimulationSnapshot[capacity];
        this.snapshotInterval = snapshotInterval;
    }

    /**
     * Create with default settings (1000 snapshots, every 60 ticks = 1 second).
     */
    public SimulationHistory() {
        this(1000, 60);
    }

    /**
     * Record a snapshot if interval has elapsed.
     * 
     * @param simulation Current simulation state
     * @return true if snapshot was recorded
     */
    public boolean recordIfNeeded(Simulation simulation) {
        long currentTick = simulation.getTickCount();

        if (currentTick - lastSnapshotTick >= snapshotInterval) {
            record(SimulationSnapshot.capture(simulation));
            lastSnapshotTick = currentTick;
            return true;
        }
        return false;
    }

    /**
     * Record a snapshot.
     */
    public void record(SimulationSnapshot snapshot) {
        lock.lock();
        try {
            buffer[head] = snapshot;
            head = (head + 1) % capacity;
            if (count < capacity) {
                count++;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get the most recent snapshot.
     */
    public SimulationSnapshot getLatest() {
        lock.lock();
        try {
            if (count == 0)
                return null;
            int index = (head - 1 + capacity) % capacity;
            return buffer[index];
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get snapshot closest to the given tick.
     */
    public SimulationSnapshot getAtTick(long tick) {
        lock.lock();
        try {
            SimulationSnapshot best = null;
            long bestDiff = Long.MAX_VALUE;

            for (int i = 0; i < count; i++) {
                int index = (head - 1 - i + capacity) % capacity;
                SimulationSnapshot snap = buffer[index];
                if (snap == null)
                    continue;

                long diff = Math.abs(snap.getTick() - tick);
                if (diff < bestDiff) {
                    bestDiff = diff;
                    best = snap;
                }
            }
            return best;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get snapshot N steps back from latest.
     * 
     * @param stepsBack 0 = latest, 1 = previous, etc.
     */
    public SimulationSnapshot getStepsBack(int stepsBack) {
        lock.lock();
        try {
            if (stepsBack >= count)
                return null;
            int index = (head - 1 - stepsBack + capacity) % capacity;
            return buffer[index];
        } finally {
            lock.unlock();
        }
    }

    /**
     * Seek simulation to a specific tick.
     * 
     * @param simulation Target simulation
     * @param tick       Target tick
     * @return true if restore was successful
     */
    public boolean seekToTick(Simulation simulation, long tick) {
        SimulationSnapshot snapshot = getAtTick(tick);
        if (snapshot != null) {
            snapshot.restore(simulation);
            return true;
        }
        return false;
    }

    /**
     * Rewind by a number of steps.
     * 
     * @param simulation Target simulation
     * @param steps      Number of snapshot steps to rewind
     * @return true if rewind was successful
     */
    public boolean rewind(Simulation simulation, int steps) {
        SimulationSnapshot snapshot = getStepsBack(steps);
        if (snapshot != null) {
            snapshot.restore(simulation);

            // Trim future snapshots
            lock.lock();
            try {
                count = Math.max(0, count - steps);
                head = (head - steps + capacity) % capacity;
            } finally {
                lock.unlock();
            }
            return true;
        }
        return false;
    }

    /**
     * Clear all history.
     */
    public void clear() {
        lock.lock();
        try {
            for (int i = 0; i < capacity; i++) {
                buffer[i] = null;
            }
            head = 0;
            count = 0;
            lastSnapshotTick = -1;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get the number of stored snapshots.
     */
    public int getCount() {
        return count;
    }

    /**
     * Get the buffer capacity.
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Get the snapshot interval in ticks.
     */
    public int getSnapshotInterval() {
        return snapshotInterval;
    }

    /**
     * Get earliest available tick.
     */
    public long getEarliestTick() {
        lock.lock();
        try {
            if (count == 0)
                return -1;
            int index = (head - count + capacity) % capacity;
            return buffer[index] != null ? buffer[index].getTick() : -1;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get latest available tick.
     */
    public long getLatestTick() {
        SimulationSnapshot latest = getLatest();
        return latest != null ? latest.getTick() : -1;
    }

    /**
     * Get total memory usage estimate in bytes.
     */
    public long getMemoryUsage() {
        long total = 0;
        lock.lock();
        try {
            for (int i = 0; i < count; i++) {
                int index = (head - 1 - i + capacity) % capacity;
                if (buffer[index] != null) {
                    total += buffer[index].getEstimatedSize();
                }
            }
        } finally {
            lock.unlock();
        }
        return total;
    }
}
