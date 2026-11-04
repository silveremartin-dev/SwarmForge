/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.core.simulation.SimulationCheckpoint;

/**
 * Builds and formats the simulation checkpoint selection and restoration card.
 */
class SimulationCheckpointsSubPanel {

    private final SimulationControlPanel panel;
    private final I18nManager i18n = I18nManager.getInstance();

    SimulationCheckpointsSubPanel(SimulationControlPanel panel) {
        this.panel = panel;
    }

    VBox buildCheckpointsPane(ComboBox<SimulationCheckpoint> comboCheckpoints) {
        VBox box = new VBox(6);
        box.getStyleClass().add("card-pane");

        Label lblTitle = new Label();
        lblTitle.textProperty().bind(i18n.createStringBinding("sim.checkpoints.title"));
        lblTitle.getStyleClass().add("accent-title");

        comboCheckpoints.setPrefWidth(220);
        comboCheckpoints.promptTextProperty().bind(i18n.createStringBinding("sim.checkpoints.none"));
        comboCheckpoints.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(SimulationCheckpoint item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : formatCheckpointLabel(item));
            }
        });
        comboCheckpoints.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(SimulationCheckpoint item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : formatCheckpointLabel(item));
            }
        });

        Button bRestore = new Button();
        bRestore.textProperty().bind(i18n.createStringBinding("sim.checkpoints.btn.restore"));
        bRestore.getStyleClass().add("primary-pill-button");
        bRestore.tooltipProperty().bind(i18n.createTooltipBinding("sim.checkpoints.btn.restore.tt"));
        bRestore.setOnAction(e -> {
            SimulationCheckpoint sel = comboCheckpoints.getValue();
            if (sel != null && panel.onRestoreCheckpoint != null) {
                panel.onRestoreCheckpoint.accept(sel);
            }
        });

        Label lblCp = new Label();
        lblCp.textProperty().bind(i18n.createStringBinding("sim.checkpoints.label"));
        lblCp.getStyleClass().add("sub-title-gray");

        HBox row = new HBox(8, lblCp, comboCheckpoints, bRestore);
        row.setAlignment(Pos.CENTER_LEFT);

        Label lblInfo = new Label();
        lblInfo.textProperty().bind(i18n.createStringBinding("sim.checkpoints.info"));
        lblInfo.getStyleClass().add("sub-title-gray");

        box.getChildren().addAll(lblTitle, row, lblInfo);
        return box;
    }

    private String formatCheckpointLabel(SimulationCheckpoint cp) {
        if (cp == null) return "";
        String rawName = cp.getName();
        String displayName = rawName;
        if (rawName != null && (rawName.startsWith("Départ") || rawName.contains("Initial Setup") || cp.getTick() == 0)) {
            displayName = i18n.get("checkpoint.initial_setup", "Initial Setup") + " (#0)";
        } else if (rawName != null && rawName.startsWith("Auto-Snapshot")) {
            displayName = i18n.get("checkpoint.auto_snapshot", "Auto-Snapshot") + " (Tick #" + cp.getTick() + ")";
        }
        return String.format("%s (Tick #%d, %d %s)",
                displayName,
                cp.getTick(),
                cp.getInterventionsRecorded().size(),
                i18n.get("checkpoint.interventions", "interventions"));
    }
}
