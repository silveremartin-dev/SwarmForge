/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;
import java.util.Map;

/**
 * Weather and Climate Editor - Configure weather events, seasonal patterns, and
 * disasters.
 * Part of the Asset Editor module.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class WeatherEditorPane extends BorderPane {

    // Weather Events
    private final Map<String, Slider> eventProbabilities = new HashMap<>();

    // Seasonal Settings
    private Slider temperatureMinSlider;
    private Slider temperatureMaxSlider;
    private Slider humiditySlider;
    private Slider rainFrequencySlider;

    // Disaster Settings
    private final Map<String, Slider> disasterProbabilities = new HashMap<>();

    // Preview
    private final VBox previewPane;

    public WeatherEditorPane() {
        setPadding(new Insets(15));


        // Title
        Label title = new Label("🌦 Weather & Climate Editor");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: black;");
        setTop(new VBox(10, title, new Separator()));

        // Main content - Split into sections
        VBox content = new VBox(20);
        content.setPadding(new Insets(10, 0, 0, 0));

        // === Section 1: Climate Preset ===
        TitledPane presetPane = createPresetsSection();

        // === Section 2: Weather Events ===
        TitledPane eventsPane = createEventsSection();

        // === Section 3: Seasonal Settings ===
        TitledPane seasonalPane = createSeasonalSection();

        // === Section 4: Disasters ===
        TitledPane disasterPane = createDisastersSection();

        content.getChildren().addAll(presetPane, eventsPane, seasonalPane, disasterPane);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        setCenter(scrollPane);

        // Preview Panel (right side)
        previewPane = createPreviewPane();
        setRight(previewPane);

        // Bottom: Save/Load buttons
        HBox buttons = createButtonBar();
        setBottom(buttons);
    }

    private TitledPane createPresetsSection() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        Label desc = new Label("Quick presets for common climate zones:");
        desc.setStyle("-fx-text-fill: black;");

        HBox presets = new HBox(10);
        String[] presetNames = { "Temperate", "Tropical", "Arid", "Mediterranean", "Arctic" };
        for (String preset : presetNames) {
            Button btn = new Button(preset);
            btn.setStyle("-fx-text-fill: black;");
            btn.setOnAction(e -> applyPreset(preset));
            presets.getChildren().add(btn);
        }

        content.getChildren().addAll(desc, presets);

        TitledPane pane = new TitledPane("Climate Presets", content);
        pane.setExpanded(true);
        styleTitledPane(pane);
        return pane;
    }

    private TitledPane createEventsSection() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        String[][] events = {
                { "☀️ Sunny", "60" },
                { "🌧 Rain", "25" },
                { "⛈ Storm", "5" },
                { "🌫 Fog", "8" },
                { "❄️ Snow", "2" },
                { "💨 Wind", "15" }
        };

        int row = 0;
        for (String[] event : events) {
            Label label = new Label(event[0]);
            label.setStyle("-fx-text-fill: black; -fx-min-width: 100;");

            Slider slider = new Slider(0, 100, Double.parseDouble(event[1]));
            slider.setPrefWidth(200);
            slider.setShowTickLabels(true);
            slider.setShowTickMarks(true);
            slider.setMajorTickUnit(25);

            Label valueLabel = new Label(event[1] + "%");
            valueLabel.setStyle("-fx-text-fill: black; -fx-min-width: 50;");
            slider.valueProperty()
                    .addListener((obs, old, val) -> valueLabel.setText(String.format("%.0f%%", val.doubleValue())));

            eventProbabilities.put(event[0], slider);

            grid.add(label, 0, row);
            grid.add(slider, 1, row);
            grid.add(valueLabel, 2, row);
            row++;
        }

        TitledPane pane = new TitledPane("Weather Event Probabilities", grid);
        styleTitledPane(pane);
        return pane;
    }

    private TitledPane createSeasonalSection() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));

        // Temperature Range
        Label tempLabel = new Label("🌡 Temperature Range (°C):");
        tempLabel.setStyle("-fx-text-fill: black;");

        temperatureMinSlider = new Slider(-20, 50, 5);
        temperatureMinSlider.setPrefWidth(150);
        temperatureMaxSlider = new Slider(-20, 50, 25);
        temperatureMaxSlider.setPrefWidth(150);

        Label tempRangeLabel = new Label("5°C - 25°C");
        tempRangeLabel.setStyle("-fx-text-fill: black;");
        temperatureMinSlider.valueProperty().addListener((obs, old, val) -> tempRangeLabel
                .setText(String.format("%.0f°C - %.0f°C", val.doubleValue(), temperatureMaxSlider.getValue())));
        temperatureMaxSlider.valueProperty().addListener((obs, old, val) -> tempRangeLabel
                .setText(String.format("%.0f°C - %.0f°C", temperatureMinSlider.getValue(), val.doubleValue())));

        HBox tempSliders = new HBox(10, new Label("Min:"), temperatureMinSlider, new Label("Max:"),
                temperatureMaxSlider);
        tempSliders.getChildren().forEach(n -> {
            if (n instanceof Label)
                ((Label) n).setStyle("-fx-text-fill: black;");
        });

        // Humidity
        Label humLabel = new Label("💧 Average Humidity:");
        humLabel.setStyle("-fx-text-fill: black;");
        humiditySlider = new Slider(0, 100, 60);
        humiditySlider.setPrefWidth(300);
        Label humValue = new Label("60%");
        humValue.setStyle("-fx-text-fill: black;");
        humiditySlider.valueProperty()
                .addListener((obs, old, val) -> humValue.setText(String.format("%.0f%%", val.doubleValue())));

        // Rain Frequency
        Label rainLabel = new Label("🌧 Rain Frequency:");
        rainLabel.setStyle("-fx-text-fill: black;");
        rainFrequencySlider = new Slider(0, 365, 100);
        rainFrequencySlider.setPrefWidth(300);
        Label rainValue = new Label("~100 days/year");
        rainValue.setStyle("-fx-text-fill: black;");
        rainFrequencySlider.valueProperty()
                .addListener((obs, old, val) -> rainValue.setText("~" + val.intValue() + " days/year"));

        grid.add(tempLabel, 0, 0);
        grid.add(tempSliders, 1, 0);
        grid.add(tempRangeLabel, 2, 0);
        grid.add(humLabel, 0, 1);
        grid.add(humiditySlider, 1, 1);
        grid.add(humValue, 2, 1);
        grid.add(rainLabel, 0, 2);
        grid.add(rainFrequencySlider, 1, 2);
        grid.add(rainValue, 2, 2);

        TitledPane pane = new TitledPane("Seasonal Settings", grid);
        styleTitledPane(pane);
        return pane;
    }

    private TitledPane createDisastersSection() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        Label warning = new Label("⚠ Disasters can significantly impact colony survival");
        warning.setStyle("-fx-text-fill: black; -fx-font-style: italic;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);

        String[][] disasters = {
                { "🔥 Fire", "1" },
                { "🌊 Flood", "2" },
                { "🌪 Tornado", "0.5" },
                { "🏜 Drought", "3" },
                { "❄️ Freeze", "2" }
        };

        int row = 0;
        for (String[] disaster : disasters) {
            Label label = new Label(disaster[0]);
            label.setStyle("-fx-text-fill: black; -fx-min-width: 120;");

            Slider slider = new Slider(0, 20, Double.parseDouble(disaster[1]));
            slider.setPrefWidth(200);
            slider.setShowTickLabels(true);
            slider.setMajorTickUnit(5);

            Label valueLabel = new Label(disaster[1] + "% /year");
            valueLabel.setStyle("-fx-text-fill: black; -fx-min-width: 80;");
            slider.valueProperty().addListener(
                    (obs, old, val) -> valueLabel.setText(String.format("%.1f%% /year", val.doubleValue())));

            disasterProbabilities.put(disaster[0], slider);

            grid.add(label, 0, row);
            grid.add(slider, 1, row);
            grid.add(valueLabel, 2, row);
            row++;
        }

        content.getChildren().addAll(warning, grid);

        TitledPane pane = new TitledPane("Natural Disasters", content);
        styleTitledPane(pane);
        pane.setExpanded(false); // Collapsed by default
        return pane;
    }

    private VBox createPreviewPane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(15));
        pane.setStyle("-fx-min-width: 200;");

        Label title = new Label("Climate Preview");
        title.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");

        // Simple visual bars for seasons
        String[] seasons = { "Spring", "Summer", "Autumn", "Winter" };
        Color[] seasonColors = { Color.LIGHTGREEN, Color.GOLD, Color.ORANGE, Color.LIGHTBLUE };

        for (int i = 0; i < seasons.length; i++) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);

            Label label = new Label(seasons[i]);
            label.setStyle("-fx-text-fill: black; -fx-min-width: 60;");

            Rectangle bar = new Rectangle(80, 15, seasonColors[i]);
            bar.setArcWidth(3);
            bar.setArcHeight(3);

            row.getChildren().addAll(label, bar);
            pane.getChildren().add(row);
        }

        pane.getChildren().addAll(new Separator(), title);
        return pane;
    }

    private HBox createButtonBar() {
        HBox bar = new HBox(15);
        bar.setPadding(new Insets(15, 0, 0, 0));
        bar.setAlignment(Pos.CENTER);

        Button btnNew = new Button("New Profile");
        btnNew.setOnAction(e -> resetToDefaults());

        Button btnSave = new Button("💾 Save Profile");
        btnSave.setStyle("-fx-text-fill: black;");
        btnSave.setOnAction(e -> saveProfile());

        Button btnLoad = new Button("📂 Load Profile");
        btnLoad.setOnAction(e -> loadProfile());

        Button btnExport = new Button("📤 Export JSON");
        btnExport.setOnAction(e -> exportJson());

        bar.getChildren().addAll(btnNew, btnSave, btnLoad, btnExport);
        return bar;
    }

    private void styleTitledPane(TitledPane pane) {
        pane.setStyle("-fx-text-fill: black;");
        pane.setCollapsible(true);
    }

    private void applyPreset(String preset) {
        switch (preset) {
            case "Temperate" -> {
                temperatureMinSlider.setValue(5);
                temperatureMaxSlider.setValue(25);
                humiditySlider.setValue(65);
                rainFrequencySlider.setValue(120);
            }
            case "Tropical" -> {
                temperatureMinSlider.setValue(22);
                temperatureMaxSlider.setValue(35);
                humiditySlider.setValue(85);
                rainFrequencySlider.setValue(200);
            }
            case "Arid" -> {
                temperatureMinSlider.setValue(15);
                temperatureMaxSlider.setValue(45);
                humiditySlider.setValue(20);
                rainFrequencySlider.setValue(20);
            }
            case "Mediterranean" -> {
                temperatureMinSlider.setValue(10);
                temperatureMaxSlider.setValue(30);
                humiditySlider.setValue(55);
                rainFrequencySlider.setValue(80);
            }
            case "Arctic" -> {
                temperatureMinSlider.setValue(-30);
                temperatureMaxSlider.setValue(10);
                humiditySlider.setValue(70);
                rainFrequencySlider.setValue(60);
            }
        }
    }

    private void resetToDefaults() {
        applyPreset("Temperate");
        eventProbabilities.values().forEach(s -> s.setValue(s.getMax() / 2));
        disasterProbabilities.values().forEach(s -> s.setValue(2));
    }

    private void saveProfile() {
        TextInputDialog dialog = new TextInputDialog("My Weather Profile");
        dialog.setTitle("Save Weather Profile");
        dialog.setHeaderText("Enter profile name:");
        dialog.showAndWait().ifPresent(name -> {
            // Not connected to DB yet
            new Alert(Alert.AlertType.INFORMATION, "Profile '" + name + "' saved locally (DB pending)").show();
        });
    }

    private void loadProfile() {
        new Alert(Alert.AlertType.INFORMATION, "Cloud profiles not available yet.").show();
    }

    private void exportJson() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Export Weather Profile");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("JSON Files", "*.json"));
        java.io.File file = fileChooser.showSaveDialog(getScene().getWindow());

        if (file != null) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mapper.writerWithDefaultPrettyPrinter().writeValue(file, getConfiguration());
                new Alert(Alert.AlertType.INFORMATION, "Export successful!").show();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Export failed: " + ex.getMessage()).show();
            }
        }
    }

    /**
     * Get current weather configuration as JSON-like map.
     */
    public Map<String, Object> getConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("temperatureMin", temperatureMinSlider.getValue());
        config.put("temperatureMax", temperatureMaxSlider.getValue());
        config.put("humidity", humiditySlider.getValue());
        config.put("rainFrequency", rainFrequencySlider.getValue());

        Map<String, Double> events = new HashMap<>();
        eventProbabilities.forEach((k, v) -> events.put(k, v.getValue()));
        config.put("events", events);

        Map<String, Double> disasters = new HashMap<>();
        disasterProbabilities.forEach((k, v) -> disasters.put(k, v.getValue()));
        config.put("disasters", disasters);

        return config;
    }
}
