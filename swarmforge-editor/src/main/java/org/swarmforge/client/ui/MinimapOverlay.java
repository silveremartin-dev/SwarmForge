/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import org.swarmforge.client.util.I18nManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.simulation.Simulation;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Minimap overlay matching the World Editor's dual 2D maps system:
 * - Top-Down View (Vue du dessus): Ant density heatmap, nests, camera viewport rect, river path.
 * - Side Profile View (Vue de profil / coupe): Height profile, soil stratigraphy, subterranean ant depth & nests.
 * Features click-to-navigate and view synchronization options.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class MinimapOverlay extends VBox {

    private final ResizableCanvas canvasTop;
    private final GraphicsContext gcTop;
    private final ResizableCanvas canvasSide;
    private final GraphicsContext gcSide;
    private final VBox mapContentBox;

    private int worldWidth = 100;
    private int worldHeight = 100;
    private int worldDepth = 32;

    // Camera viewport indicator
    private float cameraX, cameraY;
    private float viewportWidth = 20, viewportHeight = 20;

    // Navigation callback (x, y in world coords)
    private BiConsumer<Float, Float> onNavigate;

    // Ant density grid
    private int[][] densityGridTop;
    private int[][] densityGridSide; // X vs Z depth
    private static final int GRID_RES = 32;

    private boolean syncViews = true;
    private boolean isCollapsed = false;

    public MinimapOverlay(int width) {
        setSpacing(4);
        setPadding(new Insets(6));
        setStyle("-fx-background-color: rgba(15, 23, 42, 0.95); -fx-border-color: #0284c7; -fx-border-width: 1.5; -fx-border-radius: 6; -fx-background-radius: 6;");

        // CRITICAL FIX: Restrict Max Size so StackPane does NOT stretch VBox over full screen
        setPrefWidth(width + 16);
        setMaxWidth(width + 20);
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        int w = Math.max(10, width);
        int topH = Math.max(10, (int) (w * 0.7));
        int sideH = Math.max(10, (int) (w * 0.5));

        this.canvasTop = new ResizableCanvas(w, topH);
        this.gcTop = canvasTop.getGraphicsContext2D();

        this.canvasSide = new ResizableCanvas(w, sideH);
        this.gcSide = canvasSide.getGraphicsContext2D();

        this.densityGridTop = new int[GRID_RES][GRID_RES];
        this.densityGridSide = new int[GRID_RES][GRID_RES];

        // Header Labels matching World Editor style
        Label lblHeader = new Label(I18nManager.getInstance().get("minimap.title"));
        lblHeader.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        Label lblTop = new Label(I18nManager.getInstance().get("minimap.topdown"));
        lblTop.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #cbd5e1;");

        Label lblSide = new Label(I18nManager.getInstance().get("minimap.sideview"));
        lblSide.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #cbd5e1;");

        CheckBox chkSync = new CheckBox(I18nManager.getInstance().get("minimap.sync"));
        chkSync.setSelected(true);
        chkSync.setStyle("-fx-text-fill: #00d4ff; -fx-font-size: 9px; -fx-font-weight: bold;");
        chkSync.selectedProperty().addListener((o, a, b) -> this.syncViews = b);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        mapContentBox = new VBox(4, lblTop, canvasTop, lblSide, canvasSide);

        Button btnCollapse = new Button("−");
        btnCollapse.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 0 4; -fx-cursor: hand;");
        btnCollapse.setOnAction(e -> {
            isCollapsed = !isCollapsed;
            mapContentBox.setVisible(!isCollapsed);
            mapContentBox.setManaged(!isCollapsed);
            btnCollapse.setText(isCollapsed ? "+" : "−");
        });

        HBox headerBox = new HBox(4, lblHeader, chkSync, spacer, btnCollapse);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(headerBox, mapContentBox);

        // Click handlers
        canvasTop.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (onNavigate != null) {
                float wx = (float) (e.getX() / canvasTop.getWidth() * worldWidth);
                float wy = (float) (e.getY() / canvasTop.getHeight() * worldHeight);
                onNavigate.accept(wx, wy);
            }
        });

        canvasSide.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (onNavigate != null) {
                float wx = (float) (e.getX() / canvasSide.getWidth() * worldWidth);
                float wz = (float) (e.getY() / canvasSide.getHeight() * worldHeight);
                onNavigate.accept(wx, wz);
            }
        });

        clear();
    }

    /**
     * Update minimap with simulation data.
     */
    public void update(Simulation simulation) {
        if (simulation == null)
            return;

        Terrarium terrarium = simulation.getTerrarium();
        if (terrarium != null) {
            worldWidth = terrarium.getWidth();
            worldHeight = terrarium.getHeight();
            worldDepth = terrarium.getDepth();
        }

        // Clear density grids
        for (int i = 0; i < GRID_RES; i++) {
            for (int j = 0; j < GRID_RES; j++) {
                densityGridTop[i][j] = 0;
                densityGridSide[i][j] = 0;
            }
        }

        // Count ants in grid cells
        for (Colony colony : simulation.getColonies()) {
            for (Individual ant : colony.getLivingIndividuals()) {
                int gx = (int) (ant.getX() / worldWidth * GRID_RES);
                int gy = (int) (ant.getY() / worldHeight * GRID_RES);
                int gz = (int) (ant.getZ() / worldDepth * GRID_RES);

                if (gx >= 0 && gx < GRID_RES && gy >= 0 && gy < GRID_RES) {
                    densityGridTop[gx][gy]++;
                }
                if (gx >= 0 && gx < GRID_RES && gz >= 0 && gz < GRID_RES) {
                    densityGridSide[gx][gz]++;
                }
            }
        }

        redraw(simulation.getColonies());
    }

    private void redraw(List<Colony> colonies) {
        redrawTop(colonies);
        redrawSide(colonies);
    }

    private void redrawTop(List<Colony> colonies) {
        if (canvasTop == null || gcTop == null || canvasTop.getWidth() < 10 || canvasTop.getHeight() < 10) return;
        double w = canvasTop.getWidth();
        double h = canvasTop.getHeight();

        // Background
        gcTop.setFill(Color.rgb(15, 23, 42));
        gcTop.fillRect(0, 0, w, h);

        // Grid outline
        gcTop.setStroke(Color.rgb(51, 65, 85));
        gcTop.setLineWidth(1);
        gcTop.strokeRect(1, 1, w - 2, h - 2);

        // Density heatmap
        float cellW = (float) w / GRID_RES;
        float cellH = (float) h / GRID_RES;

        int maxDensity = 1;
        for (int[] row : densityGridTop) {
            for (int val : row) {
                maxDensity = Math.max(maxDensity, val);
            }
        }

        for (int x = 0; x < GRID_RES; x++) {
            for (int y = 0; y < GRID_RES; y++) {
                int count = densityGridTop[x][y];
                if (count > 0) {
                    float intensity = Math.min(1f, count / (float) maxDensity);
                    gcTop.setFill(Color.rgb(
                            (int) (50 + 205 * intensity),
                            (int) (180 + 75 * intensity),
                            50,
                            0.4 + 0.5 * intensity));
                    gcTop.fillRect(x * cellW, y * cellH, cellW, cellH);
                }
            }
        }

        // Colony nests
        if (colonies != null) {
            for (Colony colony : colonies) {
                if (colony == null) continue;
                float nx = colony.getNestX() / worldWidth * (float) w;
                float ny = colony.getNestY() / worldHeight * (float) h;

                gcTop.setFill(new RadialGradient(
                        0, 0, nx, ny, 12,
                        false, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(251, 191, 36, 0.8)),
                        new Stop(1, Color.TRANSPARENT)));
                gcTop.fillOval(nx - 12, ny - 12, 24, 24);

                gcTop.setFill(Color.ORANGE);
                gcTop.fillOval(nx - 3, ny - 3, 6, 6);
                gcTop.setStroke(Color.WHITE);
                gcTop.setLineWidth(1);
                gcTop.strokeOval(nx - 3, ny - 3, 6, 6);
            }
        }

        // Camera viewport rectangle
        float vpX = cameraX / worldWidth * (float) w;
        float vpY = cameraY / worldHeight * (float) h;
        float vpW = viewportWidth / worldWidth * (float) w;
        float vpH = viewportHeight / worldHeight * (float) h;

        gcTop.setStroke(Color.rgb(56, 189, 248, 0.9));
        gcTop.setLineWidth(1.5);
        gcTop.strokeRect(vpX - vpW / 2, vpY - vpH / 2, Math.max(8, vpW), Math.max(8, vpH));

        // Grid lines
        gcTop.setStroke(Color.rgb(51, 65, 85, 0.4));
        gcTop.setLineWidth(0.5);
        for (int i = 1; i < 4; i++) {
            gcTop.strokeLine(i * w / 4, 0, i * w / 4, h);
            gcTop.strokeLine(0, i * h / 4, w, i * h / 4);
        }
    }

    private void redrawSide(List<Colony> colonies) {
        if (canvasSide == null || gcSide == null || canvasSide.getWidth() < 10 || canvasSide.getHeight() < 10) return;
        double w = canvasSide.getWidth();
        double h = canvasSide.getHeight();

        // Background dark slate
        gcSide.setFill(Color.rgb(15, 23, 42));
        gcSide.fillRect(0, 0, w, h);

        // Stratigraphy background bands (Humus, Argile/Sable, Bedrock)
        gcSide.setFill(Color.web("#3d2817")); // Humus surface
        gcSide.fillRect(0, 0, w, h * 0.25);
        gcSide.setFill(Color.web("#9a3412")); // Argile mid
        gcSide.fillRect(0, h * 0.25, w, h * 0.45);
        gcSide.setFill(Color.web("#64748b")); // Pierre / Bedrock
        gcSide.fillRect(0, h * 0.70, w, h * 0.30);

        // Water table line
        gcSide.setStroke(Color.web("#0284c7"));
        gcSide.setLineWidth(1.2);
        gcSide.strokeLine(0, h * 0.75, w, h * 0.75);

        // Side ant density heatmap
        float cellW = (float) w / GRID_RES;
        float cellH = (float) h / GRID_RES;

        for (int x = 0; x < GRID_RES; x++) {
            for (int z = 0; z < GRID_RES; z++) {
                int count = densityGridSide[x][z];
                if (count > 0) {
                    gcSide.setFill(Color.rgb(250, 204, 21, 0.7));
                    gcSide.fillRect(x * cellW, z * cellH, cellW, cellH);
                }
            }
        }

        // Colony nest depth markers
        if (colonies != null) {
            for (Colony colony : colonies) {
                if (colony == null) continue;
                float nx = colony.getNestX() / worldWidth * (float) w;
                float nz = colony.getNestZ() / worldDepth * (float) h;

                gcSide.setFill(Color.web("#d97706"));
                gcSide.fillOval(nx - 4, nz - 4, 8, 8);
                gcSide.setStroke(Color.WHITE);
                gcSide.setLineWidth(1);
                gcSide.strokeOval(nx - 4, nz - 4, 8, 8);
            }
        }

        // Camera depth indicator
        float vpX = cameraX / worldWidth * (float) w;
        gcSide.setStroke(Color.rgb(56, 189, 248, 0.9));
        gcSide.setLineWidth(1.5);
        gcSide.strokeLine(vpX, 0, vpX, h);

        // Border
        gcSide.setStroke(Color.rgb(51, 65, 85));
        gcSide.setLineWidth(1);
        gcSide.strokeRect(1, 1, w - 2, h - 2);
    }

    /**
     * Clear the minimaps.
     */
    public void clear() {
        if (canvasTop != null && gcTop != null && canvasTop.getWidth() >= 10 && canvasTop.getHeight() >= 10) {
            gcTop.setFill(Color.rgb(15, 23, 42));
            gcTop.fillRect(0, 0, canvasTop.getWidth(), canvasTop.getHeight());
            gcTop.setFill(Color.GRAY);
            gcTop.fillText(I18nManager.getInstance().get("minimap.topdown"), canvasTop.getWidth() / 2 - 60, canvasTop.getHeight() / 2);
        }

        if (canvasSide != null && gcSide != null && canvasSide.getWidth() >= 10 && canvasSide.getHeight() >= 10) {
            gcSide.setFill(Color.rgb(15, 23, 42));
            gcSide.fillRect(0, 0, canvasSide.getWidth(), canvasSide.getHeight());
            gcSide.setFill(Color.GRAY);
            gcSide.fillText(I18nManager.getInstance().get("minimap.sideview"), canvasSide.getWidth() / 2 - 60, canvasSide.getHeight() / 2);
        }
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

    public Canvas getCanvasTop() {
        return canvasTop;
    }

    public Canvas getCanvasSide() {
        return canvasSide;
    }
}
