/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

/**
 * Nest Generator - Configure and preview ant nest structures.
 * Allows setting nest type, chamber counts, and tunnel parameters.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class NestGeneratorPane extends BorderPane {

    // Nest Configuration
    private ComboBox<String> nestTypeSelect;
    private Slider depthSlider;
    private Slider chamberCountSlider;
    private Slider tunnelWidthSlider;
    private Slider branchingSlider;

    // Chamber Counts by Type
    private final Map<String, Spinner<Integer>> chamberSpinners = new HashMap<>();

    // Preview Canvas
    private final Canvas previewCanvas;
    private final GraphicsContext gc;

    public NestGeneratorPane() {
        setPadding(new Insets(15));
        setStyle("-fx-background-color: #1a1a2e;");

        // Title
        Label title = new Label("🐜 Nest Generator");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #00d4ff;");
        setTop(new VBox(10, title, new Separator()));

        // Left: Configuration
        VBox configPane = createConfigPane();
        setLeft(configPane);

        // Center: 2D Preview
        previewCanvas = new Canvas(400, 400);
        gc = previewCanvas.getGraphicsContext2D();

        VBox previewBox = new VBox(10);
        previewBox.setPadding(new Insets(10));
        previewBox.setAlignment(Pos.TOP_CENTER);

        Label previewLabel = new Label("Nest Preview (Side View)");
        previewLabel.setStyle("-fx-text-fill: white;");

        StackPane canvasHolder = new StackPane(previewCanvas);
        canvasHolder.setStyle("-fx-background-color: #0a0a1a; -fx-border-color: #333; -fx-border-width: 1;");

        previewBox.getChildren().addAll(previewLabel, canvasHolder);
        setCenter(previewBox);

        // Initial preview
        updatePreview();

        // Bottom: Buttons
        setBottom(createButtonBar());
    }

    private VBox createConfigPane() {
        VBox config = new VBox(15);
        config.setPadding(new Insets(15));
        config.setStyle("-fx-background-color: #16213e; -fx-min-width: 280;");

        // === Nest Type Preset ===
        Label typeLabel = new Label("Nest Type:");
        typeLabel.setStyle("-fx-text-fill: white;");

        nestTypeSelect = new ComboBox<>();
        nestTypeSelect.getItems().addAll("Young Colony", "Mature Colony", "Complex Supercolony",
                "Leafcutter Fungus Farm", "Custom");
        nestTypeSelect.getSelectionModel().selectFirst();
        nestTypeSelect.setPrefWidth(200);
        nestTypeSelect.setOnAction(e -> applyPreset(nestTypeSelect.getValue()));

        // === Depth ===
        Label depthLabel = new Label("Max Depth (blocks):");
        depthLabel.setStyle("-fx-text-fill: white;");
        depthSlider = createSlider(5, 50, 20);
        HBox depthBox = sliderWithValue(depthSlider);

        // === Chamber Count ===
        Label chamberLabel = new Label("Total Chambers:");
        chamberLabel.setStyle("-fx-text-fill: white;");
        chamberCountSlider = createSlider(3, 50, 15);
        HBox chamberBox = sliderWithValue(chamberCountSlider);

        // === Tunnel Width ===
        Label tunnelLabel = new Label("Tunnel Width:");
        tunnelLabel.setStyle("-fx-text-fill: white;");
        tunnelWidthSlider = createSlider(1, 5, 2);
        HBox tunnelBox = sliderWithValue(tunnelWidthSlider);

        // === Branching Factor ===
        Label branchLabel = new Label("Branching Factor:");
        branchLabel.setStyle("-fx-text-fill: white;");
        branchingSlider = createSlider(1, 5, 3);
        HBox branchBox = sliderWithValue(branchingSlider);

        // === Chamber Types ===
        TitledPane chamberPane = createChamberTypesPane();

        config.getChildren().addAll(
                typeLabel, nestTypeSelect,
                new Separator(),
                depthLabel, depthBox,
                chamberLabel, chamberBox,
                tunnelLabel, tunnelBox,
                branchLabel, branchBox,
                new Separator(),
                chamberPane);

        // Listen for changes
        depthSlider.valueProperty().addListener((o, a, b) -> updatePreview());
        chamberCountSlider.valueProperty().addListener((o, a, b) -> updatePreview());
        tunnelWidthSlider.valueProperty().addListener((o, a, b) -> updatePreview());
        branchingSlider.valueProperty().addListener((o, a, b) -> updatePreview());

        return config;
    }

    private TitledPane createChamberTypesPane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));

        String[][] chamberTypes = {
                { "👑 Queen Chamber", "1" },
                { "🥚 Brood Chambers", "3" },
                { "🍖 Food Storage", "4" },
                { "🚪 Entrances", "2" },
                { "🗑 Waste Dumps", "1" },
                { "🍄 Fungus Gardens", "0" }
        };

        int row = 0;
        for (String[] type : chamberTypes) {
            Label label = new Label(type[0]);
            label.setStyle("-fx-text-fill: #aaa;");

            Spinner<Integer> spinner = new Spinner<>(0, 20, Integer.parseInt(type[1]));
            spinner.setPrefWidth(70);
            spinner.valueProperty().addListener((o, a, b) -> updatePreview());

            chamberSpinners.put(type[0], spinner);

            grid.add(label, 0, row);
            grid.add(spinner, 1, row);
            row++;
        }

        TitledPane pane = new TitledPane("Chamber Distribution", grid);
        pane.setExpanded(true);
        pane.setStyle("-fx-text-fill: white;");
        return pane;
    }

    private Slider createSlider(double min, double max, double value) {
        Slider slider = new Slider(min, max, value);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit((max - min) / 4);
        slider.setPrefWidth(180);
        return slider;
    }

    private HBox sliderWithValue(Slider slider) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        Label value = new Label(String.format("%.0f", slider.getValue()));
        value.setStyle("-fx-text-fill: #00d4ff; -fx-min-width: 40;");
        slider.valueProperty().addListener((obs, old, val) -> value.setText(String.format("%.0f", val.doubleValue())));
        box.getChildren().addAll(slider, value);
        return box;
    }

    private HBox createButtonBar() {
        HBox bar = new HBox(15);
        bar.setPadding(new Insets(15, 0, 0, 0));
        bar.setAlignment(Pos.CENTER);

        Button btnGenerate = new Button("🔄 Regenerate Preview");
        btnGenerate.setOnAction(e -> updatePreview());

        Button btnApplyToWorld = new Button("✓ Apply to World Editor");
        btnApplyToWorld.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        btnApplyToWorld.setOnAction(e -> applyToWorld());

        Button btnSave = new Button("💾 Save Template");
        btnSave.setOnAction(e -> saveTemplate());

        Button btnLoad = new Button("📂 Load Template");
        btnLoad.setOnAction(e -> loadTemplate());

        bar.getChildren().addAll(btnGenerate, btnApplyToWorld, btnSave, btnLoad);
        return bar;
    }

    private void applyPreset(String preset) {
        switch (preset) {
            case "Young Colony" -> {
                depthSlider.setValue(10);
                chamberCountSlider.setValue(5);
                tunnelWidthSlider.setValue(2);
                branchingSlider.setValue(2);
                setSpinner("👑 Queen Chamber", 1);
                setSpinner("🥚 Brood Chambers", 2);
                setSpinner("🍖 Food Storage", 1);
                setSpinner("🚪 Entrances", 1);
                setSpinner("🗑 Waste Dumps", 0);
                setSpinner("🍄 Fungus Gardens", 0);
            }
            case "Mature Colony" -> {
                depthSlider.setValue(25);
                chamberCountSlider.setValue(20);
                tunnelWidthSlider.setValue(3);
                branchingSlider.setValue(3);
                setSpinner("👑 Queen Chamber", 1);
                setSpinner("🥚 Brood Chambers", 5);
                setSpinner("🍖 Food Storage", 6);
                setSpinner("🚪 Entrances", 3);
                setSpinner("🗑 Waste Dumps", 2);
                setSpinner("🍄 Fungus Gardens", 0);
            }
            case "Complex Supercolony" -> {
                depthSlider.setValue(45);
                chamberCountSlider.setValue(50);
                tunnelWidthSlider.setValue(4);
                branchingSlider.setValue(5);
                setSpinner("👑 Queen Chamber", 5);
                setSpinner("🥚 Brood Chambers", 12);
                setSpinner("🍖 Food Storage", 15);
                setSpinner("🚪 Entrances", 8);
                setSpinner("🗑 Waste Dumps", 5);
                setSpinner("🍄 Fungus Gardens", 0);
            }
            case "Leafcutter Fungus Farm" -> {
                depthSlider.setValue(35);
                chamberCountSlider.setValue(30);
                tunnelWidthSlider.setValue(3);
                branchingSlider.setValue(4);
                setSpinner("👑 Queen Chamber", 1);
                setSpinner("🥚 Brood Chambers", 4);
                setSpinner("🍖 Food Storage", 3);
                setSpinner("🚪 Entrances", 4);
                setSpinner("🗑 Waste Dumps", 3);
                setSpinner("🍄 Fungus Gardens", 10);
            }
        }
        updatePreview();
    }

    private void setSpinner(String name, int value) {
        Spinner<Integer> spinner = chamberSpinners.get(name);
        if (spinner != null) {
            spinner.getValueFactory().setValue(value);
        }
    }

    private void updatePreview() {
        double w = previewCanvas.getWidth();
        double h = previewCanvas.getHeight();

        // Clear
        gc.setFill(Color.rgb(15, 15, 30));
        gc.fillRect(0, 0, w, h);

        // Draw ground surface
        gc.setFill(Color.rgb(60, 80, 40));
        gc.fillRect(0, 40, w, 10);

        // Draw soil background
        gc.setFill(Color.rgb(80, 50, 30));
        gc.fillRect(0, 50, w, h - 50);

        // Get parameters
        int depth = (int) depthSlider.getValue();
        int chambers = (int) chamberCountSlider.getValue();
        int branching = (int) branchingSlider.getValue();
        double tunnelWidth = tunnelWidthSlider.getValue() * 2;

        // Draw simplified nest structure
        gc.setStroke(Color.rgb(120, 80, 50));
        gc.setLineWidth(tunnelWidth);

        // Main vertical shaft
        double centerX = w / 2;
        double shaftBottom = 50 + (depth * 6);
        gc.strokeLine(centerX, 45, centerX, Math.min(shaftBottom, h - 20));

        // Draw chambers at various depths
        gc.setFill(Color.rgb(100, 70, 40));
        double chamberSpacing = (shaftBottom - 60) / Math.max(1, chambers / 2);

        for (int i = 0; i < chambers; i++) {
            double y = 60 + (i % (chambers / 2 + 1)) * chamberSpacing;
            double xOffset = (i % 2 == 0 ? -1 : 1) * (30 + Math.random() * 50);
            double chamberX = centerX + xOffset;
            double chamberW = 20 + Math.random() * 30;
            double chamberH = 12 + Math.random() * 15;

            // Tunnel to chamber
            gc.strokeLine(centerX, y, chamberX, y);

            // Chamber
            gc.fillOval(chamberX - chamberW / 2, y - chamberH / 2, chamberW, chamberH);
            gc.setStroke(Color.rgb(140, 100, 60));
            gc.strokeOval(chamberX - chamberW / 2, y - chamberH / 2, chamberW, chamberH);
            gc.setStroke(Color.rgb(120, 80, 50));
        }

        // Draw entrance(s)
        int entrances = getSpinnerValue("🚪 Entrances");
        gc.setFill(Color.rgb(40, 60, 30));
        for (int i = 0; i < entrances; i++) {
            double ex = centerX + (i - entrances / 2.0) * 40;
            gc.fillOval(ex - 8, 35, 16, 20);
        }

        // Draw queen chamber (special color)
        if (getSpinnerValue("👑 Queen Chamber") > 0) {
            gc.setFill(Color.rgb(180, 140, 100));
            gc.fillOval(centerX - 25, shaftBottom - 50, 50, 30);
            gc.setStroke(Color.GOLD);
            gc.setLineWidth(2);
            gc.strokeOval(centerX - 25, shaftBottom - 50, 50, 30);
        }

        // Legend
        gc.setFill(Color.WHITE);
        gc.fillText("Depth: " + depth + " blocks", 10, h - 40);
        gc.fillText("Chambers: " + chambers, 10, h - 25);
        gc.fillText("Branching: " + branching, 10, h - 10);
    }

    private int getSpinnerValue(String name) {
        Spinner<Integer> spinner = chamberSpinners.get(name);
        return spinner != null ? spinner.getValue() : 0;
    }

    // Callback for applying to world
    private java.util.function.Consumer<Map<String, Object>> onApplyCallback;

    public void setOnApply(java.util.function.Consumer<Map<String, Object>> callback) {
        this.onApplyCallback = callback;
    }

    private void applyToWorld() {
        if (onApplyCallback != null) {
            onApplyCallback.accept(getConfiguration());
            // Feedback handled by client or main view switch
        } else {
            new Alert(Alert.AlertType.WARNING,
                    "No world editor connected.\n" +
                            "This feature requires the full SwarmForge Studio.")
                    .show();
        }
    }

    private void saveTemplate() {
        TextInputDialog dialog = new TextInputDialog("My Nest Template");
        dialog.setTitle("Save Nest Template");
        dialog.setHeaderText("Enter template name:");
        dialog.showAndWait().ifPresent(name -> {
            // Simple placeholder for save logic
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.io.File file = new java.io.File("nest_template_" + name.replace(" ", "_") + ".json");
                mapper.writerWithDefaultPrettyPrinter().writeValue(file, getConfiguration());
                new Alert(Alert.AlertType.INFORMATION, "Template saved to " + file.getName()).show();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Save failed: " + ex.getMessage()).show();
            }
        });
    }

    private void loadTemplate() {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Load Nest Template");
        dialog.setHeaderText("Enter template name (without .json):");
        dialog.showAndWait().ifPresent(name -> {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.io.File file = new java.io.File("nest_template_" + name.replace(" ", "_") + ".json");
                if (!file.exists()) {
                    new Alert(Alert.AlertType.ERROR, "Template not found: " + file.getName()).show();
                    return;
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> config = mapper.readValue(file, Map.class);

                // Apply loaded config to UI
                if (config.containsKey("nestType"))
                    nestTypeSelect.setValue((String) config.get("nestType"));
                if (config.containsKey("depth"))
                    depthSlider.setValue(((Number) config.get("depth")).doubleValue());
                if (config.containsKey("chamberCount"))
                    chamberCountSlider.setValue(((Number) config.get("chamberCount")).doubleValue());
                if (config.containsKey("tunnelWidth"))
                    tunnelWidthSlider.setValue(((Number) config.get("tunnelWidth")).doubleValue());
                if (config.containsKey("branching"))
                    branchingSlider.setValue(((Number) config.get("branching")).doubleValue());

                if (config.containsKey("chamberDistribution")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Integer> dist = (Map<String, Integer>) config.get("chamberDistribution");
                    dist.forEach((k, v) -> setSpinner(k, v));
                }

                updatePreview();
                new Alert(Alert.AlertType.INFORMATION, "Template loaded!").show();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Load failed: " + ex.getMessage()).show();
                ex.printStackTrace();
            }
        });
    }

    /**
     * Get current nest configuration as a map.
     */
    public Map<String, Object> getConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("nestType", nestTypeSelect.getValue());
        config.put("depth", (int) depthSlider.getValue());
        config.put("chamberCount", (int) chamberCountSlider.getValue());
        config.put("tunnelWidth", (int) tunnelWidthSlider.getValue());
        config.put("branching", (int) branchingSlider.getValue());

        Map<String, Integer> chambers = new HashMap<>();
        chamberSpinners.forEach((k, v) -> chambers.put(k, v.getValue()));
        config.put("chamberDistribution", chambers);

        return config;
    }
}
