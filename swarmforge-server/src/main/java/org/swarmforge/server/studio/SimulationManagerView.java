/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server.studio;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.UUID;

/**
 * Manager to create, configure, and resume simulations.
 */
public class SimulationManagerView extends VBox {

    public SimulationManagerView() {
        setSpacing(20);
        setPadding(new Insets(20));

        Label header = new Label("Create New Simulation");
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        getChildren().add(header);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        // World Selection
        form.add(new Label("World Template:"), 0, 0);
        ComboBox<String> worldCombo = new ComboBox<>();
        worldCombo.getItems().addAll("Temperate Forest", "Amazon Rainforest", "Sahara Desert", "Urban Garden");
        worldCombo.getSelectionModel().selectFirst();
        form.add(worldCombo, 1, 0);

        // Species Selection
        form.add(new Label("Primary Species:"), 0, 1);
        ComboBox<String> speciesCombo = new ComboBox<>();
        speciesCombo.getItems().addAll("Lasius niger", "Atta cephalotes", "Solenopsis invicta");
        speciesCombo.getSelectionModel().selectFirst();
        form.add(speciesCombo, 1, 1);

        // Climate
        form.add(new Label("Start Season:"), 0, 2);
        ComboBox<String> seasonCombo = new ComboBox<>();
        seasonCombo.getItems().addAll("Spring", "Summer", "Autumn", "Winter");
        seasonCombo.getSelectionModel().selectFirst();
        form.add(seasonCombo, 1, 2);

        getChildren().add(form);

        // Action Buttons
        HBox actions = new HBox(10);
        Button createBtn = new Button("Initialize Simulation");
        createBtn.getStyleClass().add("accent-button");
        createBtn.setOnAction(e -> {
            // Logic to create simulation entry in DB
            System.out.println("Creating simulation: " + worldCombo.getValue() + " with " + speciesCombo.getValue());
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Simulation Created! ID: " + UUID.randomUUID());
            alert.show();
        });

        Button loadBtn = new Button("Load Checkpoint...");
        loadBtn.setOnAction(e -> System.out.println("Opening checkpoint loader..."));

        actions.getChildren().addAll(createBtn, loadBtn);
        getChildren().add(actions);

        // Distributed Nodes
        Label distributeLabel = new Label("Compute Nodes (Headless Slaves)");
        distributeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 20 0 0 0;");
        getChildren().add(distributeLabel);

        ListView<String> nodesList = new ListView<>();
        nodesList.getItems().add("Localhost (Master) - [GPU: RTX 4090]");
        nodesList.getItems().add("Node-01 (Offline)");
        nodesList.setPrefHeight(150);
        getChildren().add(nodesList);
    }
}
