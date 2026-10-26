/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.world;

/**
 * Command interface for reversible terrain alterations and world editing operations.
 * Implements the classic Command Pattern with support for Undo/Redo.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public interface TerrainCommand {

    /**
     * Executes the terrain editing operation.
     */
    void execute();

    /**
     * Undoes the terrain editing operation, restoring prior voxel state.
     */
    void undo();

    /**
     * Human-readable label for this editing command (e.g. for tooltips or undo history).
     */
    String getDescription();
}
