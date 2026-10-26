/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.event;

import java.util.UUID;

/**
 * Sealed hierarchy of typed UI events for reactive JavaFX 21 & 3D view decoupling.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public sealed interface UiEvent permits
        UiEvent.VoxelSelectedUiEvent,
        UiEvent.IndividualFocusedUiEvent,
        UiEvent.SimulationStateChangedUiEvent,
        UiEvent.TerrainModifiedUiEvent,
        UiEvent.NotificationUiEvent {

    /**
     * Fired when a 3D or 2D voxel is hovered or selected.
     */
    record VoxelSelectedUiEvent(
            int x, int y, int z,
            int materialId,
            double moisture,
            double temperature,
            double pheromoneIntensity
    ) implements UiEvent {}

    /**
     * Fired when an individual insect is focused or selected in the viewport.
     */
    record IndividualFocusedUiEvent(
            UUID individualId,
            String speciesName,
            String caste,
            double health,
            double energy,
            double x, double y, double z
    ) implements UiEvent {}

    /**
     * Fired when simulation lifecycle state transitions (play, pause, step, speed).
     */
    record SimulationStateChangedUiEvent(
            boolean isPlaying,
            boolean isPaused,
            boolean isStopped,
            long currentTick,
            float currentSpeed
    ) implements UiEvent {}

    /**
     * Fired when a sub-region of terrain is modified (brush, excavation, deposition).
     */
    record TerrainModifiedUiEvent(
            int minX, int minY, int minZ,
            int maxX, int maxY, int maxZ,
            String operation
    ) implements UiEvent {}

    /**
     * Fired for user notifications and toasts.
     */
    record NotificationUiEvent(
            String level,
            String title,
            String message
    ) implements UiEvent {}
}
