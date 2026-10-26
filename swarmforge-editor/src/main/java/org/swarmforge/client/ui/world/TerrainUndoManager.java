/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.world;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Undo/Redo manager for terrain and world editing operations in SwarmForge Studio.
 * Maintains bounded command stacks with JavaFX observable properties for UI action enablement.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class TerrainUndoManager {

    private static final Logger LOG = LoggerFactory.getLogger(TerrainUndoManager.class);
    private static final int DEFAULT_MAX_STACK_SIZE = 50;

    private final int maxStackSize;
    private final Deque<TerrainCommand> undoStack = new ArrayDeque<>();
    private final Deque<TerrainCommand> redoStack = new ArrayDeque<>();

    private final BooleanProperty canUndoProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty canRedoProperty = new SimpleBooleanProperty(false);

    public TerrainUndoManager() {
        this(DEFAULT_MAX_STACK_SIZE);
    }

    public TerrainUndoManager(int maxStackSize) {
        this.maxStackSize = Math.max(5, maxStackSize);
    }

    /**
     * Executes a new command and pushes it onto the undo stack, clearing redo history.
     */
    public synchronized void executeCommand(TerrainCommand command) {
        if (command == null) return;
        try {
            command.execute();
            if (undoStack.size() >= maxStackSize) {
                undoStack.removeLast();
            }
            undoStack.push(command);
            redoStack.clear();
            updateProperties();
            LOG.debug("Executed terrain command: {}", command.getDescription());
        } catch (Exception ex) {
            LOG.error("Failed to execute terrain command: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Reverts the most recent command on the undo stack and pushes it onto redo stack.
     */
    public synchronized boolean undo() {
        if (undoStack.isEmpty()) return false;
        TerrainCommand cmd = undoStack.pop();
        try {
            cmd.undo();
            redoStack.push(cmd);
            updateProperties();
            LOG.info("Undid terrain command: {}", cmd.getDescription());
            return true;
        } catch (Exception ex) {
            LOG.error("Error undoing terrain command: {}", ex.getMessage(), ex);
            return false;
        }
    }

    /**
     * Reapplies the most recent command on the redo stack and pushes it onto undo stack.
     */
    public synchronized boolean redo() {
        if (redoStack.isEmpty()) return false;
        TerrainCommand cmd = redoStack.pop();
        try {
            cmd.execute();
            undoStack.push(cmd);
            updateProperties();
            LOG.info("Redid terrain command: {}", cmd.getDescription());
            return true;
        } catch (Exception ex) {
            LOG.error("Error redoing terrain command: {}", ex.getMessage(), ex);
            return false;
        }
    }

    /**
     * Clears both undo and redo histories.
     */
    public synchronized void clear() {
        undoStack.clear();
        redoStack.clear();
        updateProperties();
    }

    public synchronized boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public synchronized boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public BooleanProperty canUndoProperty() {
        return canUndoProperty;
    }

    public BooleanProperty canRedoProperty() {
        return canRedoProperty;
    }

    private void updateProperties() {
        canUndoProperty.set(!undoStack.isEmpty());
        canRedoProperty.set(!redoStack.isEmpty());
    }
}
