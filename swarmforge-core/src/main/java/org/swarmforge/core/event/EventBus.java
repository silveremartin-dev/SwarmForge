/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.event;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Central event bus for simulation events.
 * Supports synchronous and asynchronous event dispatch, filtering, and replay.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class EventBus {

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

    private EventBus() {
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
                    System.err.println("Event listener error: " + e.getMessage());
                }
            }
        }

        // Notify global listeners
        for (Consumer<SimulationEvent> listener : globalListeners) {
            try {
                listener.accept(event);
            } catch (Exception e) {
                System.err.println("Global event listener error: " + e.getMessage());
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
