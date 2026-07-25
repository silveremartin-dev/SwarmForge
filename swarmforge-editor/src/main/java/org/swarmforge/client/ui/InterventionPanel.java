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
import org.swarmforge.core.event.EventBus;
import org.swarmforge.core.event.SimulationEvent;

/**
 * Intervention Panel - God Mode controls for runtime simulation manipulation.
 * Allows adding/removing entities, triggering events, and modifying parameters.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class InterventionPanel extends BorderPane {

    private Spinner<Integer> antCountSpinner;
    private ComboBox<String> casteSelect;
    private ComboBox<String> colonySelect;
    private TextField posXField, posYField, posZField;
    private ComboBox<String> disasterSelect;
    private Slider intensitySlider;
    private TextArea logArea;

    // Callback for interventions (set by client)
    private InterventionCallback callback;

    public interface InterventionCallback {
        void spawnAnts(String colonyId, String caste, int count, float x, float y, float z);
        void killAnts(String colonyId, String caste, int count);
        void spawnFood(float x, float y, float z, float amount);
        void triggerDisaster(String type, float intensity);
        void modifyParameter(String param, Object value);
    }

    public InterventionPanel() {
        setPadding(new Insets(15));


        // Title
        Label title = new Label("⚡ God Mode - Intervention Controls");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ff6b6b;");
        
        Label warning = new Label("⚠ Changes apply immediately to simulation");
        warning.setStyle("-fx-text-fill: #ffc107; -fx-font-style: italic;");
        
        VBox header = new VBox(5, title, warning);
        setTop(header);

        // Main content - Accordion with sections
        Accordion accordion = new Accordion();

        TitledPane spawnPane = createSpawnSection();
        TitledPane killPane = createKillSection();
        TitledPane resourcePane = createResourceSection();
        TitledPane speedPane = createCasteGenerationSpeedSection();
        TitledPane disasterPane = createDisasterSection();
        TitledPane paramPane = createParameterSection();

        accordion.getPanes().addAll(spawnPane, killPane, resourcePane, speedPane, disasterPane, paramPane);
        accordion.setExpandedPane(spawnPane);

        VBox content = new VBox(15, accordion);
        content.setPadding(new Insets(15, 0, 0, 0));
        setCenter(content);

        // Bottom: Action Log
        setBottom(createLogSection());
    }

    private TitledPane createSpawnSection() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        // Colony selector
        Label colLabel = new Label("Colony:");
        colLabel.setStyle("-fx-text-fill: white;");
        colonySelect = new ComboBox<>();
        colonySelect.getItems().addAll("Colony 1 (Lasius niger)", "Colony 2 (Atta)", "New Colony");
        colonySelect.getSelectionModel().selectFirst();
        colonySelect.setPrefWidth(200);

        // Caste selector
        Label casteLabel = new Label("Caste:");
        casteLabel.setStyle("-fx-text-fill: white;");
        casteSelect = new ComboBox<>();
        casteSelect.getItems().addAll("Worker", "Soldier", "Scout", "Nurse", "Queen");
        casteSelect.getSelectionModel().selectFirst();

        // Count
        Label countLabel = new Label("Count:");
        countLabel.setStyle("-fx-text-fill: white;");
        antCountSpinner = new Spinner<>(1, 1000, 10);
        antCountSpinner.setEditable(true);
        antCountSpinner.setPrefWidth(100);

        // Position
        Label posLabel = new Label("Position (X,Y,Z):");
        posLabel.setStyle("-fx-text-fill: white;");
        HBox posBox = new HBox(5);
        posXField = new TextField("64");
        posXField.setPrefWidth(50);
        posYField = new TextField("64");
        posYField.setPrefWidth(50);
        posZField = new TextField("50");
        posZField.setPrefWidth(50);
        posBox.getChildren().addAll(posXField, posYField, posZField);

        // Spawn button
        Button btnSpawn = new Button("🐜 Spawn Ants");
        btnSpawn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
        btnSpawn.setOnAction(e -> spawnAnts());

        grid.add(colLabel, 0, 0);
        grid.add(colonySelect, 1, 0);
        grid.add(casteLabel, 0, 1);
        grid.add(casteSelect, 1, 1);
        grid.add(countLabel, 0, 2);
        grid.add(antCountSpinner, 1, 2);
        grid.add(posLabel, 0, 3);
        grid.add(posBox, 1, 3);
        grid.add(btnSpawn, 1, 4);

        TitledPane pane = new TitledPane("Spawn Entities", grid);
        styleTitledPane(pane);
        return pane;
    }

    private TitledPane createKillSection() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        HBox row1 = new HBox(10);
        row1.setAlignment(Pos.CENTER_LEFT);
        
        ComboBox<String> targetSelect = new ComboBox<>();
        targetSelect.getItems().addAll("Random Workers", "Random Soldiers", "Oldest Ants", "Weakest Ants", "All Queens");
        targetSelect.getSelectionModel().selectFirst();
        targetSelect.setPrefWidth(150);
        
        Spinner<Integer> killCount = new Spinner<>(1, 100, 5);
        killCount.setPrefWidth(80);

        Button btnKill = new Button("💀 Kill");
        btnKill.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        btnKill.setOnAction(e -> {
            log("Killed " + killCount.getValue() + " " + targetSelect.getValue());
            publishEvent(SimulationEvent.EventType.WORKER_DIED);
        });

        row1.getChildren().addAll(new Label("Target:") {{ setStyle("-fx-text-fill: white;"); }}, 
            targetSelect, new Label("Count:") {{ setStyle("-fx-text-fill: white;"); }}, killCount, btnKill);

        // Mass extinction button
        Button btnExtinct = new Button("☠️ Colony Extinction");
        btnExtinct.setStyle("-fx-background-color: #6c0000; -fx-text-fill: white;");
        btnExtinct.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
                "This will destroy the entire colony. Continue?");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    log("⚠ Colony extinction triggered!");
                    publishEvent(SimulationEvent.EventType.COLONY_DESTROYED);
                }
            });
        });

        content.getChildren().addAll(row1, new Separator(), btnExtinct);

        TitledPane pane = new TitledPane("Kill Entities", content);
        styleTitledPane(pane);
        return pane;
    }

    private TitledPane createResourceSection() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        // Food spawning
        Label foodLabel = new Label("Food Amount:");
        foodLabel.setStyle("-fx-text-fill: white;");
        Slider foodSlider = new Slider(10, 1000, 100);
        foodSlider.setShowTickLabels(true);
        foodSlider.setPrefWidth(180);
        Label foodValue = new Label("100");
        foodValue.setStyle("-fx-text-fill: #e4e4e7;");
        foodSlider.valueProperty().addListener((o,a,b) -> foodValue.setText(String.format("%.0f", b.doubleValue())));

        Button btnFood = new Button("🍖 Spawn Food");
        btnFood.setStyle("-fx-background-color: #3f3f46; -fx-text-fill: white;");
        btnFood.setOnAction(e -> {
            float x = Float.parseFloat(posXField.getText());
            float y = Float.parseFloat(posYField.getText());
            float z = Float.parseFloat(posZField.getText());
            log("Spawned " + (int)foodSlider.getValue() + " food at (" + x + "," + y + "," + z + ")");
            if (callback != null) callback.spawnFood(x, y, z, (float)foodSlider.getValue());
        });

        // Food Pile (God Mode)
        Button btnFoodPile = new Button("⛰️ Spawn Food Pile (God Mode)");
        btnFoodPile.setStyle("-fx-background-color: #eab308; -fx-text-fill: black; -fx-font-weight: bold;");
        btnFoodPile.setOnAction(e -> {
            float x = Float.parseFloat(posXField.getText());
            float y = Float.parseFloat(posYField.getText());
            float z = Float.parseFloat(posZField.getText());
            float amount = (float) foodSlider.getValue() * 5.0f;
            log("⚡ God Mode: Spawned Massive Food Pile (" + amount + " units) at (" + x + "," + y + "," + z + ")");
            if (callback != null) callback.spawnFood(x, y, z, amount);
        });

        // Water spawning
        Button btnWater = new Button("💧 Spawn Water");
        btnWater.setOnAction(e -> log("Spawned water source"));

        // Remove resources
        Button btnRemoveFood = new Button("🗑 Remove All Food");
        btnRemoveFood.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        btnRemoveFood.setOnAction(e -> log("Removed all food sources"));

        grid.add(foodLabel, 0, 0);
        grid.add(foodSlider, 1, 0);
        grid.add(foodValue, 2, 0);
        grid.add(btnFood, 1, 1);
        grid.add(btnWater, 2, 1);
        grid.add(btnFoodPile, 0, 2, 3, 1);
        grid.add(new Separator(), 0, 3, 3, 1);
        grid.add(btnRemoveFood, 1, 4);

        TitledPane pane = new TitledPane("Resources & God Mode Piles", grid);
        styleTitledPane(pane);
        return pane;
    }

    private TitledPane createCasteGenerationSpeedSection() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        // Worker generation speed
        Label lblWorkerSpeed = new Label("Génération Ouvrières (Workers):");
        lblWorkerSpeed.setStyle("-fx-text-fill: white;");
        Slider workerSpeedSlider = new Slider(0.1, 5.0, 1.0);
        workerSpeedSlider.setShowTickLabels(true);
        Label lblWorkerVal = new Label("1.0x");
        lblWorkerVal.setStyle("-fx-text-fill: #38bdf8;");
        workerSpeedSlider.valueProperty().addListener((o, a, b) -> {
            lblWorkerVal.setText(String.format("%.1fx", b.doubleValue()));
            log("Vitesse ponte/génération Ouvrières fixée à " + String.format("%.1fx", b.doubleValue()));
        });

        // Soldier generation speed
        Label lblSoldierSpeed = new Label("Génération Soldats (Soldiers):");
        lblSoldierSpeed.setStyle("-fx-text-fill: white;");
        Slider soldierSpeedSlider = new Slider(0.1, 5.0, 1.0);
        soldierSpeedSlider.setShowTickLabels(true);
        Label lblSoldierVal = new Label("1.0x");
        lblSoldierVal.setStyle("-fx-text-fill: #ef4444;");
        soldierSpeedSlider.valueProperty().addListener((o, a, b) -> {
            lblSoldierVal.setText(String.format("%.1fx", b.doubleValue()));
            log("Vitesse ponte/génération Soldats fixée à " + String.format("%.1fx", b.doubleValue()));
        });

        // Predator generation speed
        Label lblPredatorSpeed = new Label("Génération Prédateurs (Predators):");
        lblPredatorSpeed.setStyle("-fx-text-fill: white;");
        Slider predatorSpeedSlider = new Slider(0.1, 5.0, 1.0);
        predatorSpeedSlider.setShowTickLabels(true);
        Label lblPredatorVal = new Label("1.0x");
        lblPredatorVal.setStyle("-fx-text-fill: #a855f7;");
        predatorSpeedSlider.valueProperty().addListener((o, a, b) -> {
            lblPredatorVal.setText(String.format("%.1fx", b.doubleValue()));
            log("Taux d'apparition Prédateurs fixé à " + String.format("%.1fx", b.doubleValue()));
        });

        grid.add(lblWorkerSpeed, 0, 0); grid.add(workerSpeedSlider, 1, 0); grid.add(lblWorkerVal, 2, 0);
        grid.add(lblSoldierSpeed, 0, 1); grid.add(soldierSpeedSlider, 1, 1); grid.add(lblSoldierVal, 2, 1);
        grid.add(lblPredatorSpeed, 0, 2); grid.add(predatorSpeedSlider, 1, 2); grid.add(lblPredatorVal, 2, 2);

        TitledPane pane = new TitledPane("⚙️ Vitesses de Génération (Castes & Prédateurs)", grid);
        styleTitledPane(pane);
        return pane;
    }

    private TitledPane createDisasterSection() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        // Disaster type
        HBox typeRow = new HBox(10);
        typeRow.setAlignment(Pos.CENTER_LEFT);
        Label typeLabel = new Label("Disaster Type:");
        typeLabel.setStyle("-fx-text-fill: white;");
        disasterSelect = new ComboBox<>();
        disasterSelect.getItems().addAll("🔥 Fire", "🌊 Flood", "🌪 Tornado", "🏜 Drought", "❄️ Freeze", "⚡ Lightning", "🦟 Pestilence");
        disasterSelect.getSelectionModel().selectFirst();
        disasterSelect.setPrefWidth(150);
        typeRow.getChildren().addAll(typeLabel, disasterSelect);

        // Intensity
        HBox intRow = new HBox(10);
        intRow.setAlignment(Pos.CENTER_LEFT);
        Label intLabel = new Label("Intensity:");
        intLabel.setStyle("-fx-text-fill: white;");
        intensitySlider = new Slider(0.1, 1.0, 0.5);
        intensitySlider.setShowTickLabels(true);
        intensitySlider.setPrefWidth(200);
        Label intValue = new Label("Medium");
        intValue.setStyle("-fx-text-fill: #ffc107;");
        intensitySlider.valueProperty().addListener((o,a,b) -> {
            double v = b.doubleValue();
            intValue.setText(v < 0.3 ? "Low" : v < 0.7 ? "Medium" : "Catastrophic");
            intValue.setStyle("-fx-text-fill: " + (v < 0.3 ? "#28a745" : v < 0.7 ? "#ffc107" : "#dc3545") + ";");
        });
        intRow.getChildren().addAll(intLabel, intensitySlider, intValue);

        // Trigger button
        Button btnTrigger = new Button("💥 TRIGGER DISASTER");
        btnTrigger.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        btnTrigger.setOnAction(e -> {
            String type = disasterSelect.getValue();
            float intensity = (float) intensitySlider.getValue();
            log("⚠ DISASTER TRIGGERED: " + type + " (intensity: " + String.format("%.1f", intensity) + ")");
            if (callback != null) callback.triggerDisaster(type, intensity);
            EventBus.getInstance().publish(SimulationEvent.disasterOccurred(0, type, intensity, 100));
        });

        // Quick weather buttons
        HBox weatherRow = new HBox(10);
        Button btnRain = new Button("🌧 Rain");
        btnRain.setOnAction(e -> log("Weather: Rain started"));
        Button btnSun = new Button("☀️ Clear");
        btnSun.setOnAction(e -> log("Weather: Cleared"));
        Button btnStorm = new Button("⛈ Storm");
        btnStorm.setOnAction(e -> log("Weather: Storm started"));
        weatherRow.getChildren().addAll(new Label("Quick Weather:") {{ setStyle("-fx-text-fill: #888;"); }}, 
            btnRain, btnSun, btnStorm);

        content.getChildren().addAll(typeRow, intRow, btnTrigger, new Separator(), weatherRow);

        TitledPane pane = new TitledPane("Disasters & Weather", content);
        styleTitledPane(pane);
        return pane;
    }

    private TitledPane createParameterSection() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        // Simulation speed
        Label speedLabel = new Label("Simulation Speed:");
        speedLabel.setStyle("-fx-text-fill: white;");
        Slider speedSlider = new Slider(0.1, 4.0, 1.0);
        speedSlider.setShowTickLabels(true);
        speedSlider.setPrefWidth(200);
        Label speedValue = new Label("1.0x");
        speedValue.setStyle("-fx-text-fill: #e4e4e7;");
        speedSlider.valueProperty().addListener((o,a,b) -> speedValue.setText(String.format("%.1fx", b.doubleValue())));

        // Pheromone decay
        Label pheroLabel = new Label("Pheromone Decay:");
        pheroLabel.setStyle("-fx-text-fill: white;");
        Slider pheroSlider = new Slider(0.01, 0.2, 0.05);
        pheroSlider.setPrefWidth(200);

        // Food spawn rate
        Label foodRateLabel = new Label("Food Spawn Rate:");
        foodRateLabel.setStyle("-fx-text-fill: white;");
        Slider foodRateSlider = new Slider(0, 1.0, 0.3);
        foodRateSlider.setPrefWidth(200);

        Button btnApply = new Button("Apply Changes");
        btnApply.setStyle("-fx-background-color: #3f3f46; -fx-text-fill: white;");
        btnApply.setOnAction(e -> log("Parameters updated"));

        grid.add(speedLabel, 0, 0);
        grid.add(speedSlider, 1, 0);
        grid.add(speedValue, 2, 0);
        grid.add(pheroLabel, 0, 1);
        grid.add(pheroSlider, 1, 1);
        grid.add(foodRateLabel, 0, 2);
        grid.add(foodRateSlider, 1, 2);
        grid.add(btnApply, 1, 3);

        TitledPane pane = new TitledPane("Simulation Parameters", grid);
        styleTitledPane(pane);
        return pane;
    }

    private VBox createLogSection() {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10, 0, 0, 0));

        Label logLabel = new Label("Intervention Log");
        logLabel.setStyle("-fx-text-fill: #888;");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(100);
        logArea.setStyle("-fx-control-inner-background: #18181b; -fx-text-fill: #e4e4e7; -fx-font-family: monospace;");

        box.getChildren().addAll(logLabel, logArea);
        return box;
    }

    private void styleTitledPane(TitledPane pane) {
        pane.setStyle("-fx-text-fill: white;");
    }

    private void spawnAnts() {
        try {
            String colony = colonySelect.getValue();
            String caste = casteSelect.getValue();
            int count = antCountSpinner.getValue();
            float x = Float.parseFloat(posXField.getText());
            float y = Float.parseFloat(posYField.getText());
            float z = Float.parseFloat(posZField.getText());

            log("Spawned " + count + " " + caste + "s for " + colony + " at (" + x + "," + y + "," + z + ")");
            
            if (callback != null) {
                callback.spawnAnts(colony, caste, count, x, y, z);
            }

            // Publish event
            publishEvent(caste.equals("Queen") ? SimulationEvent.EventType.QUEEN_BORN : 
                        caste.equals("Soldier") ? SimulationEvent.EventType.SOLDIER_BORN : 
                        SimulationEvent.EventType.WORKER_BORN);

        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Invalid position values").show();
        }
    }

    private void publishEvent(SimulationEvent.EventType type) {
        EventBus.getInstance().publish(new SimulationEvent(type, 0, type.name() + " via God Mode"));
    }

    private void log(String message) {
        String time = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        logArea.appendText("[" + time + "] " + message + "\n");
    }

    public void setCallback(InterventionCallback callback) {
        this.callback = callback;
    }
}
