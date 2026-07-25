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

import java.util.function.Consumer;

/**
 * Live Simulation Inspector Pane for Voxels, Ants, and Environmental Objects.
 * Displays real-time inspection properties when clicking objects in 3D viewport.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class LiveInspectorPane extends VBox {

    private final Label titleLabel;
    private final Label subTitleLabel;
    private final GridPane statsGrid;

    private final ProgressBar healthBar;
    private final ProgressBar energyBar;
    private final ProgressBar hungerBar;

    private final Button btnFollowAnt;
    private Consumer<String> onFollowAntCallback;

    private String currentInspectedAntId = null;

    public LiveInspectorPane() {
        setSpacing(10);
        setPadding(new Insets(12));
        setPrefWidth(280);
        setStyle("-fx-background-color: rgba(24, 24, 27, 0.92); -fx-border-color: #38bdf8; " +
                "-fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");

        // Header
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        titleLabel = new Label("🔍 Inspection");
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnClose = new Button("✕");
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: #aaa; -fx-font-weight: bold;");
        btnClose.setOnAction(e -> setVisible(false));

        header.getChildren().addAll(titleLabel, spacer, btnClose);

        subTitleLabel = new Label("Cliquez sur un voxel ou une fourmi dans la vue 3D");
        subTitleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #a1a1aa; -fx-wrap-text: true;");

        // Stats grid
        statsGrid = new GridPane();
        statsGrid.setHgap(10);
        statsGrid.setVgap(6);
        statsGrid.setPadding(new Insets(5, 0, 5, 0));

        // Progress bars for ants
        healthBar = createProgressBar("#22c55e");
        energyBar = createProgressBar("#3b82f6");
        hungerBar = createProgressBar("#eab308");

        // Follow Ant Button
        btnFollowAnt = new Button("🎥 Suivre cette Fourmi");
        btnFollowAnt.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold;");
        btnFollowAnt.setPrefWidth(250);
        btnFollowAnt.setOnAction(e -> {
            if (currentInspectedAntId != null && onFollowAntCallback != null) {
                onFollowAntCallback.accept(currentInspectedAntId);
            }
        });
        btnFollowAnt.setVisible(false);

        getChildren().addAll(header, subTitleLabel, new Separator(), statsGrid, btnFollowAnt);
        setVisible(true);
    }

    private ProgressBar createProgressBar(String colorHex) {
        ProgressBar pb = new ProgressBar(1.0);
        pb.setPrefWidth(140);
        pb.setStyle("-fx-accent: " + colorHex + ";");
        return pb;
    }

    /**
     * Display properties of a clicked voxel/block.
     */
    public void inspectVoxel(int x, int y, int z, String material, float moisture, float temp, float compaction) {
        titleLabel.setText("🧱 Voxel (" + x + ", " + y + ", " + z + ")");
        subTitleLabel.setText("Propriétés géologiques & physico-chimiques");
        currentInspectedAntId = null;
        btnFollowAnt.setVisible(false);

        statsGrid.getChildren().clear();
        addGridRow("Matériau:", material, 0);
        addGridRow("Humidité du sol:", String.format("%.1f%%", moisture), 1);
        addGridRow("Température:", String.format("%.1f °C", temp), 2);
        addGridRow("Compaction:", String.format("%.1f kPa", compaction), 3);
        addGridRow("Profondeur (Z):", z + " voxel(s)", 4);
        addGridRow("Stabilité:", compaction > 40 ? "Excellente (Stable)" : "Friable (Risque éboulement)", 5);

        setVisible(true);
    }

    /**
     * Display properties of a clicked ant/individual.
     */
    public void inspectAnt(String id, String caste, String stage, float health, float energy, float hunger, float age, String job) {
        this.currentInspectedAntId = id;
        titleLabel.setText("🐜 Individu : " + caste);
        subTitleLabel.setText("ID: " + (id.length() > 8 ? id.substring(0, 8) + "..." : id));
        btnFollowAnt.setVisible(true);

        statsGrid.getChildren().clear();
        addGridRow("Caste:", caste, 0);
        addGridRow("Stade de Vie:", stage, 1);
        addGridRow("Rôle / Job:", job, 2);

        // Health
        healthBar.setProgress(Math.max(0, Math.min(1.0, health / 100.0)));
        addGridControl("Santé:", healthBar, 3);

        // Energy
        energyBar.setProgress(Math.max(0, Math.min(1.0, energy / 100.0)));
        addGridControl("Énergie:", energyBar, 4);

        // Hunger
        hungerBar.setProgress(Math.max(0, Math.min(1.0, hunger / 100.0)));
        addGridControl("Faim:", hungerBar, 5);

        addGridRow("Âge (Ticks):", String.format("%.0f", age), 6);

        setVisible(true);
    }

    private void addGridRow(String label, String value, int row) {
        Label l = new Label(label);
        l.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 11px;");
        Label v = new Label(value);
        v.setStyle("-fx-text-fill: #f4f4f5; -fx-font-weight: bold; -fx-font-size: 11px;");
        statsGrid.add(l, 0, row);
        statsGrid.add(v, 1, row);
    }

    private void addGridControl(String label, Control control, int row) {
        Label l = new Label(label);
        l.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 11px;");
        statsGrid.add(l, 0, row);
        statsGrid.add(control, 1, row);
    }

    public void setOnFollowAnt(Consumer<String> callback) {
        this.onFollowAntCallback = callback;
    }
}
