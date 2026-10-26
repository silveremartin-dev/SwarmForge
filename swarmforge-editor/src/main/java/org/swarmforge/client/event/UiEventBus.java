/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.event;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * High-performance, decoupled event bus for SwarmForge Studio UI and 3D Viewport interactions.
 * Supports typed sealed events, subscription filtering, and automatic JavaFX Application Thread dispatching.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public final class UiEventBus {

    private static final Logger LOG = LoggerFactory.getLogger(UiEventBus.class);
    private static final UiEventBus INSTANCE = new UiEventBus();

    private final Map<Class<? extends UiEvent>, List<Consumer<? extends UiEvent>>> subscribers = new ConcurrentHashMap<>();

    private UiEventBus() {
    }

    public static UiEventBus getInstance() {
        return INSTANCE;
    }

    /**
     * Subscribe a consumer to a specific typed UI event.
     */
    public <T extends UiEvent> void subscribe(Class<T> eventType, Consumer<T> consumer) {
        if (eventType == null || consumer == null) return;
        subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(consumer);
    }

    /**
     * Unsubscribe a consumer from a specific event type.
     */
    @SuppressWarnings("unchecked")
    public <T extends UiEvent> void unsubscribe(Class<T> eventType, Consumer<T> consumer) {
        if (eventType == null || consumer == null) return;
        List<Consumer<? extends UiEvent>> list = subscribers.get(eventType);
        if (list != null) {
            list.remove(consumer);
        }
    }

    /**
     * Publish an event synchronously on the current thread.
     */
    @SuppressWarnings("unchecked")
    public <T extends UiEvent> void publish(T event) {
        if (event == null) return;
        List<Consumer<? extends UiEvent>> list = subscribers.get(event.getClass());
        if (list != null) {
            for (Consumer<? extends UiEvent> consumer : list) {
                try {
                    ((Consumer<T>) consumer).accept(event);
                } catch (Exception ex) {
                    LOG.error("Error dispatching UI event {}: {}", event.getClass().getSimpleName(), ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * Publish an event guaranteed to execute on the JavaFX Application Thread.
     */
    public <T extends UiEvent> void publishFx(T event) {
        if (event == null) return;
        if (Platform.isFxApplicationThread()) {
            publish(event);
        } else {
            Platform.runLater(() -> publish(event));
        }
    }
}
