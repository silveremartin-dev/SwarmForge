/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.simulation.Simulation;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Minimap overlay showing top-down view of the terrarium.
 * Features:
 * - Colony nest positions
 * - Ant density heatmap
 * - Click-to-navigate
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class MinimapOverlay extends StackPane {

    private final Canvas canvas;
    private final GraphicsContext gc;
    private final int size;

    private int worldWidth = 100;
    private int worldHeight = 100;

    // Camera viewport indicator
    private float cameraX, cameraY;
    private float viewportWidth = 20, viewportHeight = 20;

    // Click callback (x, y in world coords)
    private BiConsumer<Float, Float> onNavigate;

    // Ant density grid
    private int[][] densityGrid;
    private static final int GRID_RESOLUTION = 32;

    public MinimapOverlay(int size) {
        this.size = size;
        this.canvas = new Canvas(size, size);
        this.gc = canvas.getGraphicsContext2D();
        this.densityGrid = new int[GRID_RESOLUTION][GRID_RESOLUTION];

        getChildren().add(canvas);

        // Styling
        setStyle("-fx-background-color: rgba(0,0,0,0.7); -fx-border-color: #555; -fx-border-width: 2;");
        setMaxSize(size, size);
        setMinSize(size, size);

        // Click handler
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, this::handleClick);

        clear();
    }

    private void handleClick(MouseEvent e) {
        if (onNavigate != null) {
            float worldX = (float) (e.getX() / size * worldWidth);
            float worldY = (float) (e.getY() / size * worldHeight);
            onNavigate.accept(worldX, worldY);
        }
    }

    /**
     * Update minimap with simulation data.
     */
    public void update(Simulation simulation) {
        if (simulation == null)
            return;

        var terrarium = simulation.getTerrarium();
        worldWidth = terrarium.getWidth();
        worldHeight = terrarium.getHeight();

        // Clear density grid
        for (int i = 0; i < GRID_RESOLUTION; i++) {
            for (int j = 0; j < GRID_RESOLUTION; j++) {
                densityGrid[i][j] = 0;
            }
        }

        // Count ants in grid cells
        for (Colony colony : simulation.getColonies()) {
            for (Individual ant : colony.getLivingIndividuals()) {
                int gx = (int) (ant.getX() / worldWidth * GRID_RESOLUTION);
                int gy = (int) (ant.getY() / worldHeight * GRID_RESOLUTION);
                if (gx >= 0 && gx < GRID_RESOLUTION && gy >= 0 && gy < GRID_RESOLUTION) {
                    densityGrid[gx][gy]++;
                }
            }
        }

        redraw(simulation.getColonies());
    }

    private void redraw(List<Colony> colonies) {
        // Background
        gc.setFill(Color.rgb(20, 30, 20));
        gc.fillRect(0, 0, size, size);

        // Draw terrain outline
        gc.setStroke(Color.rgb(60, 80, 60));
        gc.setLineWidth(1);
        gc.strokeRect(1, 1, size - 2, size - 2);

        // Draw density heatmap
        float cellW = (float) size / GRID_RESOLUTION;
        float cellH = (float) size / GRID_RESOLUTION;

        int maxDensity = 1;
        for (int[] row : densityGrid) {
            for (int val : row) {
                maxDensity = Math.max(maxDensity, val);
            }
        }

        for (int x = 0; x < GRID_RESOLUTION; x++) {
            for (int y = 0; y < GRID_RESOLUTION; y++) {
                int count = densityGrid[x][y];
                if (count > 0) {
                    float intensity = Math.min(1f, count / (float) maxDensity);
                    gc.setFill(Color.rgb(
                            (int) (50 + 200 * intensity),
                            (int) (100 + 100 * intensity),
                            50,
                            0.3 + 0.5 * intensity));
                    gc.fillRect(x * cellW, y * cellH, cellW, cellH);
                }
            }
        }

        // Draw colony nests
        for (Colony colony : colonies) {
            float nx = colony.getNestX() / worldWidth * size;
            float ny = colony.getNestY() / worldHeight * size;

            // Nest glow effect
            gc.setFill(new RadialGradient(
                    0, 0, nx, ny, 15,
                    false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(255, 200, 100, 0.8)),
                    new Stop(1, Color.TRANSPARENT)));
            gc.fillOval(nx - 15, ny - 15, 30, 30);

            // Nest marker
            gc.setFill(Color.ORANGE);
            gc.fillOval(nx - 4, ny - 4, 8, 8);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1);
            gc.strokeOval(nx - 4, ny - 4, 8, 8);
        }

        // Draw camera viewport indicator
        float vpX = cameraX / worldWidth * size;
        float vpY = cameraY / worldHeight * size;
        float vpW = viewportWidth / worldWidth * size;
        float vpH = viewportHeight / worldHeight * size;

        gc.setStroke(Color.rgb(100, 200, 255, 0.8));
        gc.setLineWidth(2);
        gc.strokeRect(vpX - vpW / 2, vpY - vpH / 2, vpW, vpH);

        // Draw grid lines
        gc.setStroke(Color.rgb(60, 80, 60, 0.5));
        gc.setLineWidth(0.5);
        for (int i = 1; i < 4; i++) {
            gc.strokeLine(i * size / 4, 0, i * size / 4, size);
            gc.strokeLine(0, i * size / 4, size, i * size / 4);
        }
    }

    /**
     * Clear the minimap.
     */
    public void clear() {
        gc.setFill(Color.rgb(20, 30, 20));
        gc.fillRect(0, 0, size, size);

        gc.setFill(Color.GRAY);
        gc.fillText("No data", size / 2 - 20, size / 2);
    }

    /**
     * Update camera position indicator.
     */
    public void updateCameraPosition(float x, float y, float viewW, float viewH) {
        this.cameraX = x;
        this.cameraY = y;
        this.viewportWidth = viewW;
        this.viewportHeight = viewH;
    }

    /**
     * Set navigation callback.
     */
    public void setOnNavigate(BiConsumer<Float, Float> callback) {
        this.onNavigate = callback;
    }

    /**
     * Get the canvas for embedding.
     */
    public Canvas getCanvas() {
        return canvas;
    }
}
