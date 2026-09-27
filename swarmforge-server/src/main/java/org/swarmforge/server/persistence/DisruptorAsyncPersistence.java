/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server.persistence;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ultra-low latency Lock-Free Ring Buffer (LMAX Disruptor Pattern) for Asynchronous Telemetry & Persistence.
 *
 * Decouples the high-frequency physics simulation loop from PostgreSQL/H2 I/O bottlenecks.
 *  - Zero locks, zero synchronization contention on the producer side.
 *  - Cache-line padded sequence counters (preventing CPU false sharing across cores).
 *  - Pre-allocated event entries avoiding any GC allocation on the hot path.
 *  - High-throughput asynchronous batch drainer running on dedicated virtual threads.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public final class DisruptorAsyncPersistence {

    private static final Logger LOG = Logger.getLogger(DisruptorAsyncPersistence.class.getName());
    private static final int DEFAULT_RING_SIZE = 65_536; // Must be power of 2

    // Cache-line padded sequence cursor to eliminate false sharing
    public static class PaddedAtomicLong {
        public volatile long p1, p2, p3, p4, p5, p6, p7;
        public volatile long value = 0L;
        public volatile long p8, p9, p10, p11, p12, p13, p14;

        private static final VarHandle VALUE_HANDLE;
        static {
            try {
                VALUE_HANDLE = MethodHandles.lookup().findVarHandle(PaddedAtomicLong.class, "value", long.class);
            } catch (ReflectiveOperationException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        public long get() {
            return (long) VALUE_HANDLE.getVolatile(this);
        }

        public long getOpaque() {
            return (long) VALUE_HANDLE.getOpaque(this);
        }

        public void setOpaque(long val) {
            VALUE_HANDLE.setOpaque(this, val);
        }

        public boolean compareAndSet(long expected, long update) {
            return VALUE_HANDLE.compareAndSet(this, expected, update);
        }

        public long getAndAdd(long delta) {
            return (long) VALUE_HANDLE.getAndAdd(this, delta);
        }
    }

    public enum EventType {
        COLONY_METRICS,
        TICK_TELEMETRY,
        CHECKPOINT_SAVE,
        ECOSYSTEM_LOG
    }

    public static final class PersistenceEvent {
        public EventType type;
        public String colonyId;
        public long tick;
        public float biomass;
        public int population;
        public double latitude;
        public double longitude;
        public byte[] payload;
        public long timestamp;

        public void reset() {
            this.type = null;
            this.colonyId = null;
            this.tick = 0L;
            this.biomass = 0.0f;
            this.population = 0;
            this.payload = null;
            this.timestamp = 0L;
        }
    }

    private final int ringSize;
    private final int mask;
    private final PersistenceEvent[] ringBuffer;

    private final PaddedAtomicLong producerCursor = new PaddedAtomicLong();
    private final PaddedAtomicLong consumerCursor = new PaddedAtomicLong();

    private final DatabaseManager databaseManager;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread consumerThread;

    public DisruptorAsyncPersistence(DatabaseManager databaseManager) {
        this(databaseManager, DEFAULT_RING_SIZE);
    }

    public DisruptorAsyncPersistence(DatabaseManager databaseManager, int ringSize) {
        if (Integer.bitCount(ringSize) != 1) {
            throw new IllegalArgumentException("Ring size must be a power of 2");
        }
        this.databaseManager = databaseManager;
        this.ringSize = ringSize;
        this.mask = ringSize - 1;
        this.ringBuffer = new PersistenceEvent[ringSize];

        for (int i = 0; i < ringSize; i++) {
            ringBuffer[i] = new PersistenceEvent();
        }
    }

    /**
     * Starts the asynchronous consumer daemon thread.
     */
    public synchronized void start() {
        if (running.compareAndSet(false, true)) {
            consumerThread = Thread.ofVirtual().name("disruptor-db-consumer").start(this::drainLoop);
            LOG.info("✓ LMAX Disruptor Async Persistence engine initialized (Ring size: " + ringSize + ")");
        }
    }

    /**
     * Publishes a colony metric update into the ring buffer without blocking.
     *
     * @return true if published, false if ring buffer was saturated.
     */
    public boolean publishColonyMetric(String colonyId, long tick, float biomass, int population) {
        long currentProd = producerCursor.get();
        long currentCons = consumerCursor.getOpaque();

        if (currentProd - currentCons >= ringSize) {
            // Buffer full, drop or yield
            return false;
        }

        long nextSeq = producerCursor.getAndAdd(1);
        int index = (int) (nextSeq & mask);
        PersistenceEvent event = ringBuffer[index];

        event.type = EventType.COLONY_METRICS;
        event.colonyId = colonyId;
        event.tick = tick;
        event.biomass = biomass;
        event.population = population;
        event.timestamp = System.currentTimeMillis();

        return true;
    }

    private void drainLoop() {
        while (running.get() || consumerCursor.get() < producerCursor.get()) {
            long cons = consumerCursor.get();
            long prod = producerCursor.get();

            if (cons < prod) {
                int batchLimit = 256;
                int processed = 0;

                while (cons < prod && processed < batchLimit) {
                    int index = (int) (cons & mask);
                    PersistenceEvent event = ringBuffer[index];
                    persistEvent(event);
                    event.reset();
                    cons++;
                    processed++;
                }
                consumerCursor.setOpaque(cons);
            } else {
                Thread.onSpinWait();
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void persistEvent(PersistenceEvent event) {
        if (databaseManager == null || event.type == null) return;

        try {
            Connection conn = databaseManager.getConnection();
            if (conn == null || conn.isClosed()) return;

            if (event.type == EventType.COLONY_METRICS) {
                String sql = "UPDATE colonies SET biomass = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setFloat(1, event.biomass);
                    stmt.setString(2, event.colonyId);
                    stmt.executeUpdate();
                }
            }
        } catch (Exception e) {
            LOG.log(Level.FINE, "Failed async persistence event", e);
        }
    }

    /**
     * Gracefully flushes and closes the Disruptor engine.
     */
    public synchronized void stop() {
        if (running.compareAndSet(true, false)) {
            if (consumerThread != null) {
                try {
                    consumerThread.join(2000);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public long getPublishedCount() {
        return producerCursor.get();
    }

    public long getConsumedCount() {
        return consumerCursor.get();
    }
}
