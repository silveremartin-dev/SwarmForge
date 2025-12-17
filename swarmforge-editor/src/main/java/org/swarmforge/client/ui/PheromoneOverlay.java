/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

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

/**
 * 2D overlay visualization for pheromone trails.
 * Can be used as an overlay on top of the 3D view or standalone.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class PheromoneOverlay extends VBox {

    private final Canvas canvas;
    private final GraphicsContext gc;

    // Pheromone type colors
    private static final Color[] PHEROMONE_COLORS = {
            Color.rgb(76, 175, 80, 0.7), // FOOD - Green
            Color.rgb(33, 150, 243, 0.7), // HOME - Blue
            Color.rgb(244, 67, 54, 0.7), // ALARM - Red
            Color.rgb(255, 193, 7, 0.7), // TRAIL - Yellow
            Color.rgb(156, 39, 176, 0.7), // QUEEN - Purple
            Color.rgb(255, 152, 0, 0.7), // BROOD - Orange
            Color.rgb(96, 125, 139, 0.7), // DEATH - Gray
            Color.rgb(0, 150, 136, 0.7) // TERRITORY - Teal
    };

    private static final String[] PHEROMONE_NAMES = {
            "Food", "Home", "Alarm", "Trail", "Queen", "Brood", "Death", "Territory"
    };

    private int selectedType = 0; // FOOD by default
    private float opacity = 0.7f;
    private float threshold = 0.01f;
    private boolean showGrid = false;

    private float[][] currentData;
    private int dataWidth, dataHeight;

    public PheromoneOverlay(int width, int height) {
        setSpacing(5);
        setPadding(new Insets(5));

        // Control bar
        HBox controls = createControls();

        // Canvas
        canvas = new Canvas(width, height);
        gc = canvas.getGraphicsContext2D();

        getChildren().addAll(controls, canvas);
        VBox.setVgrow(canvas, Priority.ALWAYS);

        clear();
    }

    private HBox createControls() {
        HBox box = new HBox(10);
        box.setPadding(new Insets(5));
        box.setStyle("-fx-background-color: #333;");

        // Pheromone type selector
        Label lblType = new Label("Type:");
        lblType.setStyle("-fx-text-fill: white;");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(PHEROMONE_NAMES);
        typeCombo.getSelectionModel().select(0);
        typeCombo.setOnAction(e -> {
            selectedType = typeCombo.getSelectionModel().getSelectedIndex();
            redraw();
        });

        // Opacity slider
        Label lblOpacity = new Label("Opacity:");
        lblOpacity.setStyle("-fx-text-fill: white;");

        Slider opacitySlider = new Slider(0.1, 1.0, 0.7);
        opacitySlider.setPrefWidth(100);
        opacitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            opacity = newVal.floatValue();
            redraw();
        });

        // Threshold slider
        Label lblThreshold = new Label("Threshold:");
        lblThreshold.setStyle("-fx-text-fill: white;");

        Slider thresholdSlider = new Slider(0, 0.5, 0.01);
        thresholdSlider.setPrefWidth(100);
        thresholdSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            threshold = newVal.floatValue();
            redraw();
        });

        // Grid toggle
        CheckBox gridCheck = new CheckBox("Grid");
        gridCheck.setStyle("-fx-text-fill: white;");
        gridCheck.setOnAction(e -> {
            showGrid = gridCheck.isSelected();
            redraw();
        });

        box.getChildren().addAll(
                lblType, typeCombo,
                lblOpacity, opacitySlider,
                lblThreshold, thresholdSlider,
                gridCheck);

        return box;
    }

    /**
     * Update pheromone data for display.
     * 
     * @param data   2D array of pheromone values (0-1 normalized)
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
     * Redraw the overlay.
     */
    public void redraw() {
        gc.setFill(Color.rgb(20, 20, 20, 0.8));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (currentData == null || dataWidth == 0 || dataHeight == 0) {
            return;
        }

        double cellWidth = canvas.getWidth() / dataWidth;
        double cellHeight = canvas.getHeight() / dataHeight;

        Color baseColor = PHEROMONE_COLORS[selectedType];

        for (int x = 0; x < dataWidth; x++) {
            for (int y = 0; y < dataHeight; y++) {
                float value = currentData[x][y];

                if (value > threshold) {
                    // Scale value to visible intensity
                    double intensity = Math.min(1.0, value * 2);
                    Color cellColor = Color.color(
                            baseColor.getRed(),
                            baseColor.getGreen(),
                            baseColor.getBlue(),
                            intensity * opacity);

                    gc.setFill(cellColor);
                    gc.fillRect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
                }
            }
        }

        // Draw grid if enabled
        if (showGrid) {
            gc.setStroke(Color.rgb(100, 100, 100, 0.3));
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

    private void drawLegend() {
        gc.setFill(Color.rgb(50, 50, 50, 0.9));
        gc.fillRect(10, 10, 120, 25);

        gc.setFill(PHEROMONE_COLORS[selectedType]);
        gc.fillRect(15, 15, 15, 15);

        gc.setFill(Color.WHITE);
        gc.fillText(PHEROMONE_NAMES[selectedType] + " Pheromone", 35, 27);
    }

    /**
     * Clear the overlay.
     */
    public void clear() {
        gc.setFill(Color.rgb(20, 20, 20));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.GRAY);
        gc.fillText("No pheromone data", canvas.getWidth() / 2 - 50, canvas.getHeight() / 2);
    }

    /**
     * Get canvas for embedding.
     */
    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Set the selected pheromone type to display.
     */
    public void setSelectedType(int type) {
        if (type >= 0 && type < PHEROMONE_COLORS.length) {
            this.selectedType = type;
            redraw();
        }
    }
}
