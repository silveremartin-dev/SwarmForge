/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.scene.canvas.Canvas;

/**
 * A Canvas subclass that reports as resizable.
 * Prevents JavaFX StackPane/HBox infinite layout feedback loops where
 * non-resizable Canvas continuously expands parent panes.
 */
public class ResizableCanvas extends Canvas {

    public ResizableCanvas() {
        super();
    }

    public ResizableCanvas(double width, double height) {
        super(width, height);
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double minWidth(double height) {
        return 10.0;
    }

    @Override
    public double minHeight(double width) {
        return 10.0;
    }

    @Override
    public double prefWidth(double height) {
        return 100.0;
    }

    @Override
    public double prefHeight(double width) {
        return 100.0;
    }

    @Override
    public double maxWidth(double height) {
        return 10000.0;
    }

    @Override
    public double maxHeight(double width) {
        return 10000.0;
    }
}
