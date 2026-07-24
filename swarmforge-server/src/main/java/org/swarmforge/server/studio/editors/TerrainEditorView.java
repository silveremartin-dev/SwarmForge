/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server.studio.editors;

import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
// import org.swarmforge.core.world.providers.OpenTopographyProvider;

/**
 * Terrain Editor with OpenTopography integration.
 */
public class TerrainEditorView extends BorderPane {

    private Canvas mapCanvas;

    public TerrainEditorView() {
        // Sidebar interactions
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(10));
        sidebar.setPrefWidth(300);
        sidebar.setStyle("-fx-background-color: #2b2b2b;");

        Label title = new Label("Real-World Data");
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        sidebar.getChildren().add(title);

        TextField latField = new TextField("48.8566"); // Paris
        latField.setPromptText("Latitude");
        TextField lonField = new TextField("2.3522");
        lonField.setPromptText("Longitude");

        Button fetchBtn = new Button("Fetch Elevation (SRTM)");
        fetchBtn.setMaxWidth(Double.MAX_VALUE);
        fetchBtn.setOnAction(e -> fetchMap(latField.getText(), lonField.getText()));

        Separator sep = new Separator();

        Label genTitle = new Label("Procedural Generators");
        genTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Button noiseBtn = new Button("Generate Perlin Noise");
        noiseBtn.setMaxWidth(Double.MAX_VALUE);
        noiseBtn.setOnAction(e -> generateNoise());

        sidebar.getChildren().addAll(latField, lonField, fetchBtn, sep, genTitle, noiseBtn);
        setLeft(sidebar);

        // Center Map
        mapCanvas = new Canvas(800, 600);
        VBox centerBox = new VBox(mapCanvas);
        centerBox.setStyle("-fx-background-color: #000;");
        // Center the canvas
        centerBox.setAlignment(javafx.geometry.Pos.CENTER);
        setCenter(centerBox);

        // Draw initial placeholder
        drawPlaceholder();
    }

    private void drawPlaceholder() {
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());
        gc.setStroke(Color.DARKGRAY);
        gc.strokeRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());
        gc.setFill(Color.GRAY);
        gc.fillText("No Terrain Loaded. Fetch Data or Generate.", 300, 300);
    }

    private void fetchMap(String latStr, String lonStr) {
        // Mock implementation for UI responsiveness
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());

        gc.setFill(Color.GREEN);
        gc.fillText("Fetching SRTM Data for " + latStr + ", " + lonStr + "...", 50, 50);

        // In real impl, call topoProvider.fetchElevationData().thenAccept(...)
        // Here we simulate a loaded heightmap
        for (int x = 0; x < 800; x += 10) {
            for (int y = 0; y < 600; y += 10) {
                double noise = Math.random();
                if (noise > 0.5)
                    gc.setFill(Color.FORESTGREEN);
                else
                    gc.setFill(Color.SADDLEBROWN);
                gc.fillRect(x, y, 10, 10);
            }
        }
    }

    private void generateNoise() {
        // Trigger core terrain generator
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, 800, 600);
        // Visual noise
        for (int i = 0; i < 1000; i++) {
            gc.setFill(Color.rgb(100, 100, 255, 0.5));
            gc.fillOval(Math.random() * 800, Math.random() * 600, 20, 20);
        }
    }
}
