/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.event;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central event bus for simulation events.
 * Supports synchronous and asynchronous event dispatch, filtering, and replay.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class EventBus {

    private static final Logger LOG = LoggerFactory.getLogger(EventBus.class);
    private static final EventBus INSTANCE = new EventBus();

    // Listeners by event type
    private final Map<SimulationEvent.EventType, List<Consumer<SimulationEvent>>> listeners = new ConcurrentHashMap<>();

    // Global listeners (receive all events)
    private final List<Consumer<SimulationEvent>> globalListeners = new CopyOnWriteArrayList<>();

    // Event history for replay
    private final List<SimulationEvent> eventHistory = Collections.synchronizedList(new ArrayList<>());
    private boolean recordingEnabled = true;
    private int maxHistorySize = 10000;

    // Async executor
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    // Asynchronous Disk Log Streaming
    private boolean diskLoggingEnabled = true;
    private String scenarioName = "swarmforge";
    private java.io.PrintWriter diskWriter = null;
    private final ExecutorService diskExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "SwarmForge-DiskEventLogger");
        t.setDaemon(true);
        return t;
    });

    private EventBus() {
        startDiskLogging("swarmforge");
    }

    public synchronized void startDiskLogging(String scenario) {
        if (scenario != null && !scenario.isBlank()) {
            this.scenarioName = scenario.replaceAll("[^a-zA-Z0-9_-]", "_");
        }
        closeDiskLogging();
        try {
            java.io.File logDir = new java.io.File("logs");
            if (!logDir.exists()) logDir.mkdirs();
            String timestamp = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                    .format(java.time.LocalDateTime.now());
            java.io.File logFile = new java.io.File(logDir, String.format("events_%s_%s.csv", this.scenarioName, timestamp));
            diskWriter = new java.io.PrintWriter(new java.io.BufferedWriter(new java.io.FileWriter(logFile, java.nio.charset.StandardCharsets.UTF_8, true)));
            diskWriter.println("SequenceID,Timestamp,Tick,Type,Severity,Message,Data");
            diskWriter.flush();
            LOG.info("Started streaming simulation events to disk: {}", logFile.getAbsolutePath());
        } catch (Exception e) {
            LOG.error("Failed to initialize disk event logger", e);
        }
    }

    public synchronized void closeDiskLogging() {
        if (diskWriter != null) {
            try {
                diskWriter.flush();
                diskWriter.close();
            } catch (Exception ignored) {}
            diskWriter = null;
        }
    }

    public void setDiskLoggingEnabled(boolean enabled) {
        this.diskLoggingEnabled = enabled;
    }

    public boolean isDiskLoggingEnabled() {
        return diskLoggingEnabled;
    }

    private void writeEventToDiskAsync(SimulationEvent event) {
        if (!diskLoggingEnabled) return;
        diskExecutor.submit(() -> {
            synchronized (this) {
                if (diskWriter == null) {
                    startDiskLogging(scenarioName);
                }
                if (diskWriter != null) {
                    String dataStr = event.getData() != null ? event.getData().toString().replace("\"", "'") : "";
                    diskWriter.printf("%d,%s,%d,%s,%s,\"%s\",\"%s\"%n",
                            event.getSequenceId(), event.getTimestamp(), event.getTick(),
                            event.getType(), event.getSeverity(),
                            event.getMessage() != null ? event.getMessage().replace("\"", "'") : "",
                            dataStr);
                    diskWriter.flush();
                }
            }
        });
    }

    public static EventBus getInstance() {
        return INSTANCE;
    }

    /**
     * Subscribe to a specific event type.
     */
    public void subscribe(SimulationEvent.EventType type, Consumer<SimulationEvent> listener) {
        listeners.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    /**
     * Subscribe to all events.
     */
    public void subscribeAll(Consumer<SimulationEvent> listener) {
        globalListeners.add(listener);
    }

    /**
     * Subscribe with a filter predicate.
     */
    public void subscribe(Predicate<SimulationEvent> filter, Consumer<SimulationEvent> listener) {
        subscribeAll(event -> {
            if (filter.test(event)) {
                listener.accept(event);
            }
        });
    }

    /**
     * Unsubscribe from a specific event type.
     */
    public void unsubscribe(SimulationEvent.EventType type, Consumer<SimulationEvent> listener) {
        List<Consumer<SimulationEvent>> list = listeners.get(type);
        if (list != null) {
            list.remove(listener);
        }
    }

    /**
     * Unsubscribe from all events.
     */
    public void unsubscribeAll(Consumer<SimulationEvent> listener) {
        globalListeners.remove(listener);
    }

    /**
     * Publish an event synchronously.
     */
    public void publish(SimulationEvent event) {
        // Asynchronously stream event to disk log
        writeEventToDiskAsync(event);

        // Record to history
        boolean stored = false;
        // Record to history
        if (recordingEnabled) {
            synchronized (eventHistory) {
                eventHistory.add(event);
                stored = true;
                // Trim if needed
                while (eventHistory.size() > maxHistorySize) {
                    SimulationEvent evicted = eventHistory.remove(0);
                    evicted.recycle(); // Return evicted event to pool
                }
            }
        }

        // Notify type-specific listeners
        List<Consumer<SimulationEvent>> typeListeners = listeners.get(event.getType());
        if (typeListeners != null) {
            for (Consumer<SimulationEvent> listener : typeListeners) {
                try {
                    listener.accept(event);
                } catch (Exception e) {
                    LOG.error("Event listener error: ", e);
                }
            }
        }

        // Notify global listeners
        for (Consumer<SimulationEvent> listener : globalListeners) {
            try {
                listener.accept(event);
            } catch (Exception e) {
                LOG.error("Global event listener error: ", e);
            }
        }

        // Recycle event to pool only if we didn't store it for history
        if (!stored) {
            event.recycle();
        }
    }

    /**
     * Publish an event asynchronously.
     */
    public void publishAsync(SimulationEvent event) {
        executor.submit(() -> publish(event));
    }

    /**
     * Get event history.
     */
    public List<SimulationEvent> getHistory() {
        return new ArrayList<>(eventHistory);
    }

    /**
     * Get filtered history.
     */
    public List<SimulationEvent> getHistory(SimulationEvent.EventType type) {
        return eventHistory.stream()
                .filter(e -> e.getType() == type)
                .toList();
    }

    /**
     * Get history in tick range.
     */
    public List<SimulationEvent> getHistory(long fromTick, long toTick) {
        return eventHistory.stream()
                .filter(e -> e.getTick() >= fromTick && e.getTick() <= toTick)
                .toList();
    }

    /**
     * Replay events to a listener.
     */
    public void replay(Consumer<SimulationEvent> listener) {
        for (SimulationEvent event : eventHistory) {
            listener.accept(event);
        }
    }

    /**
     * Replay events of a specific type.
     */
    public void replay(SimulationEvent.EventType type, Consumer<SimulationEvent> listener) {
        eventHistory.stream()
                .filter(e -> e.getType() == type)
                .forEach(listener);
    }

    /**
     * Clear event history.
     */
    public void clearHistory() {
        eventHistory.clear();
    }

    /**
     * Enable/disable event recording.
     */
    public void setRecordingEnabled(boolean enabled) {
        this.recordingEnabled = enabled;
    }

    /**
     * Set maximum history size.
     */
    public void setMaxHistorySize(int size) {
        this.maxHistorySize = size;
    }

    /**
     * Get event counts by type.
     */
    public Map<SimulationEvent.EventType, Long> getEventCounts() {
        Map<SimulationEvent.EventType, Long> counts = new HashMap<>();
        for (SimulationEvent event : eventHistory) {
            counts.merge(event.getType(), 1L, (a, b) -> a + b);
        }
        return counts;
    }

    /**
     * Get most recent events.
     */
    public List<SimulationEvent> getRecentEvents(int count) {
        int size = eventHistory.size();
        int start = Math.max(0, size - count);
        return new ArrayList<>(eventHistory.subList(start, size));
    }

    /**
     * Shutdown the async executor.
     */
    public void shutdown() {
        executor.shutdown();
    }
}
