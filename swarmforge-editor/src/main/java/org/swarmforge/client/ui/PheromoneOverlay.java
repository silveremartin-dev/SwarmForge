/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import org.swarmforge.client.util.I18nManager;

import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.swarmforge.core.simulation.HeatmapEngine;

/**
 * 2D overlay visualization for pheromone trails, tunnel occupancy, chamber specialization,
 * and soil stability heatmaps.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class PheromoneOverlay extends VBox {

    private final ResizableCanvas canvas;
    private final GraphicsContext gc;

    // Pheromone & Heatmap colors
    private static final Color[] OVERLAY_COLORS = {
            Color.rgb(76, 175, 80, 0.8),   // FOOD - Green
            Color.rgb(33, 150, 243, 0.8),  // HOME - Blue
            Color.rgb(244, 67, 54, 0.8),   // ALARM - Red
            Color.rgb(255, 193, 7, 0.8),   // TRAIL - Yellow
            Color.rgb(156, 39, 176, 0.8),  // QUEEN - Purple
            Color.rgb(255, 152, 0, 0.8),   // BROOD - Orange
            Color.rgb(96, 125, 139, 0.8),  // DEATH - Gray
            Color.rgb(0, 150, 136, 0.8),   // TERRITORY - Teal
            Color.rgb(233, 30, 99, 0.8),   // TUNNEL OCCUPANCY - Crimson Hot
            Color.rgb(255, 215, 0, 0.8),   // CHAMBER SPECIALIZATION - Gold
            Color.rgb(139, 195, 74, 0.8),  // SOIL STABILITY - Lime
            Color.rgb(3, 169, 244, 0.8)    // SOIL MOISTURE - Cyan
    };

    private static final String[] OVERLAY_NAMES = {
            "Food Pheromone", "Home Pheromone", "Alarm Pheromone", "Trail Pheromone",
            "Queen Pheromone", "Brood Pheromone", "Death Pheromone", "Territory Pheromone",
            "Tunnel Occupancy & Traffic", "Chamber Specialization", "Soil Stability (Mohr-Coulomb)", "Soil Moisture"
    };

    private int selectedType = 0; // FOOD by default
    private float opacity = 0.7f;
    private float threshold = 0.01f;
    private boolean showGrid = false;

    private float[][] currentData;
    private int dataWidth, dataHeight;
    private VBox controlsPane;

    public PheromoneOverlay(int width, int height) {
        setSpacing(0);
        setPadding(new Insets(0));

        // Canvas
        canvas = new ResizableCanvas(Math.max(10, width), Math.max(10, height));
        gc = canvas.getGraphicsContext2D();

        // Control Panel VBox (for placement in right panel)
        controlsPane = createControlsPane();

        getChildren().add(canvas);
        VBox.setVgrow(canvas, Priority.ALWAYS);

        clear();
    }

    public VBox getControlsPane() {
        return controlsPane;
    }

    public VBox createControlsPane() {
        VBox box = new VBox(6);
        box.setPadding(new Insets(8));
        box.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-padding: 8; -fx-background-radius: 6;");

        Label lblHeader = new Label(I18nManager.getInstance().get("pheromone.overlay_title"));
        lblHeader.setStyle("-fx-text-fill: #a78bfa; -fx-font-weight: bold; -fx-font-size: 11px;");

        // Overlay type selector
        Label lblType = new Label(I18nManager.getInstance().get("pheromone.mode"));
        lblType.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 10px; -fx-font-weight: bold;");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(OVERLAY_NAMES);
        typeCombo.getSelectionModel().select(0);
        typeCombo.setMaxWidth(Double.MAX_VALUE);
        typeCombo.setStyle("-fx-font-size: 10px;");
        typeCombo.setOnAction(e -> {
            selectedType = typeCombo.getSelectionModel().getSelectedIndex();
            redraw();
        });

        // Opacity slider
        Label lblOpacityVal = new Label(String.format("Opacité : %d%%", (int)(opacity * 100)));
        lblOpacityVal.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 10px; -fx-min-width: 70;");

        Slider opacitySlider = new Slider(0.1, 1.0, opacity);
        opacitySlider.setPrefWidth(120);
        opacitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            opacity = newVal.floatValue();
            lblOpacityVal.setText(String.format("Opacité : %d%%", (int)(opacity * 100)));
            redraw();
        });

        HBox opacityBox = new HBox(6, new Label(I18nManager.getInstance().get("pheromone.opacity")) {{ setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 10px;"); }}, opacitySlider, lblOpacityVal);

        // Threshold slider
        Label lblThresholdVal = new Label(String.format("Seuil : %.2f", threshold));
        lblThresholdVal.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 10px; -fx-min-width: 70;");

        Slider thresholdSlider = new Slider(0.0, 0.5, threshold);
        thresholdSlider.setPrefWidth(120);
        thresholdSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            threshold = newVal.floatValue();
            lblThresholdVal.setText(String.format("Seuil : %.2f", threshold));
            redraw();
        });

        HBox thresholdBox = new HBox(6, new Label(I18nManager.getInstance().get("pheromone.threshold")) {{ setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 10px;"); }}, thresholdSlider, lblThresholdVal);

        // Grid toggle
        CheckBox gridCheck = new CheckBox(I18nManager.getInstance().get("pheromone.weather_grid"));
        gridCheck.setSelected(showGrid);
        gridCheck.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 10px;");
        gridCheck.setOnAction(e -> {
            showGrid = gridCheck.isSelected();
            redraw();
        });

        Label lblRenderMode = new Label("🎨 Mode de Rendu :");
        lblRenderMode.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 10px; -fx-font-weight: bold;");

        ComboBox<String> renderModeCombo = new ComboBox<>();
        renderModeCombo.getItems().addAll(
                "🌊 Carte de Densité (Heatmap Continu)",
                "✨ Système Voxel (Points & Lueur)",
                "🛰️ Hybride SIG (Surface + Cœur Voxel)"
        );
        renderModeCombo.getSelectionModel().select(2);
        renderModeCombo.setMaxWidth(Double.MAX_VALUE);
        renderModeCombo.setStyle("-fx-font-size: 10px;");

        // Multi-Channel GIS Layer Toggles
        Label lblChannels = new Label("📡 Couches de Phéromones (Canaux) :");
        lblChannels.setStyle("-fx-text-fill: #a78bfa; -fx-font-size: 10px; -fx-font-weight: bold;");

        CheckBox chkFood = new CheckBox("🟣 Piste Alimentaire (Violet)");
        chkFood.setSelected(true); chkFood.setStyle("-fx-text-fill: #c084fc; -fx-font-size: 10px;");

        CheckBox chkHome = new CheckBox("🔵 Nid / Exploration (Bleu)");
        chkHome.setSelected(true); chkHome.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 10px;");

        CheckBox chkAlarm = new CheckBox("🔴 Alarme / Danger (Rouge)");
        chkAlarm.setSelected(true); chkAlarm.setStyle("-fx-text-fill: #f87171; -fx-font-size: 10px;");

        CheckBox chkRecruit = new CheckBox("🟠 Recrutement (Ambre)");
        chkRecruit.setSelected(true); chkRecruit.setStyle("-fx-text-fill: #fbbf24; -fx-font-size: 10px;");

        CheckBox chkQueen = new CheckBox("💖 Royale & Couvain (Rose)");
        chkQueen.setSelected(true); chkQueen.setStyle("-fx-text-fill: #f472b6; -fx-font-size: 10px;");

        VBox layersBox = new VBox(3, chkFood, chkHome, chkAlarm, chkRecruit, chkQueen);
        layersBox.setStyle("-fx-padding: 4; -fx-background-color: rgba(15,23,42,0.6); -fx-background-radius: 4;");

        box.getChildren().addAll(
                lblHeader,
                lblType, typeCombo,
                lblRenderMode, renderModeCombo,
                lblChannels, layersBox,
                opacityBox,
                thresholdBox,
                gridCheck);

        return box;
    }

    /**
     * Update pheromone / heatmap data for display.
     * 
     * @param data   2D array of normalized values (0-1)
     * @param width  Grid width
     * @param height Grid height
     */
    public void updateData(float[][] data, int width, int height) {
        this.currentData = data;
        this.dataWidth = width;
        this.dataHeight = height;
        redraw();
    }

    /**
     * Redraw the overlay with high visual fidelity.
     */
    public void redraw() {
        if (canvas == null || gc == null || canvas.getWidth() < 10 || canvas.getHeight() < 10) return;
        gc.setFill(Color.rgb(15, 23, 42, 0.9));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (currentData == null || dataWidth == 0 || dataHeight == 0) {
            return;
        }

        double cellWidth = canvas.getWidth() / dataWidth;
        double cellHeight = canvas.getHeight() / dataHeight;

        Color baseColor = OVERLAY_COLORS[selectedType % OVERLAY_COLORS.length];

        for (int x = 0; x < dataWidth; x++) {
            if (x >= currentData.length || currentData[x] == null) continue;
            for (int y = 0; y < dataHeight; y++) {
                if (y >= currentData[x].length) continue;
                float value = currentData[x][y];

                if (value > threshold) {
                    Color cellColor;
                    if (selectedType == 9) { // Chamber Specialization (Categorical Palette)
                        cellColor = getChamberColor(value, opacity);
                    } else if (selectedType == 8) { // Tunnel Occupancy (Hot Thermal Palette)
                        cellColor = Color.hsb(Math.max(0, 240 - value * 240), 0.9, 0.9, opacity);
                    } else {
                        double intensity = Math.min(1.0, value * 1.5);
                        cellColor = Color.color(
                                baseColor.getRed(),
                                baseColor.getGreen(),
                                baseColor.getBlue(),
                                intensity * opacity);
                    }

                    gc.setFill(cellColor);
                    gc.fillRect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
                }
            }
        }

        // Draw grid if enabled
        if (showGrid) {
            gc.setStroke(Color.rgb(148, 163, 184, 0.25));
            gc.setLineWidth(0.5);

            for (int x = 0; x <= dataWidth; x++) {
                gc.strokeLine(x * cellWidth, 0, x * cellWidth, canvas.getHeight());
            }
            for (int y = 0; y <= dataHeight; y++) {
                gc.strokeLine(0, y * cellHeight, canvas.getWidth(), y * cellHeight);
            }
        }

        // Draw legend
        drawLegend();
    }

    private Color getChamberColor(float code, float alpha) {
        if (code >= 0.85f) return Color.rgb(234, 179, 8, alpha);   // Queen Chamber (Gold)
        if (code >= 0.65f) return Color.rgb(249, 115, 22, alpha);  // Brood Nursery (Orange)
        if (code >= 0.45f) return Color.rgb(34, 197, 94, alpha);   // Food Storage (Green)
        if (code >= 0.35f) return Color.rgb(16, 185, 129, alpha);  // Fungus Garden (Emerald)
        if (code >= 0.15f) return Color.rgb(99, 102, 241, alpha);  // Waste/Cemetery (Indigo)
        return Color.rgb(6, 182, 212, alpha);                       // Entrance (Cyan)
    }

    private void drawLegend() {
        gc.setFill(Color.rgb(30, 41, 59, 0.95));
        gc.fillRect(10, 10, 220, 30);

        gc.setFill(OVERLAY_COLORS[selectedType % OVERLAY_COLORS.length]);
        gc.fillRect(15, 17, 16, 16);

        gc.setFill(Color.WHITE);
        gc.fillText(OVERLAY_NAMES[selectedType], 38, 29);
    }

    /**
     * Clear the overlay.
     */
    public void clear() {
        if (canvas == null || gc == null || canvas.getWidth() < 10 || canvas.getHeight() < 10) return;
        gc.setFill(Color.rgb(15, 23, 42));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.GRAY);
        gc.fillText("Aucune donnée de heatmap disponible", canvas.getWidth() / 2 - 90, canvas.getHeight() / 2);
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void setSelectedType(int type) {
        if (type >= 0 && type < OVERLAY_NAMES.length) {
            this.selectedType = type;
            redraw();
        }
    }
}
