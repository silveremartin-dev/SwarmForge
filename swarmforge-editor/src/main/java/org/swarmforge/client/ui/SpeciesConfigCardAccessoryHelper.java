/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper component building accessory species checkboxes, initial counts, and replenishment strategy combos
 * for {@link SpeciesConfigCard}.
 */
class SpeciesConfigCardAccessoryHelper {

    static class AccessoryRowControls {
        final CheckBox chk;
        final String name;
        final String role;
        final Spinner<Integer> countSpinner;
        final ComboBox<String> strategyCombo;

        AccessoryRowControls(CheckBox chk, String name, String role, Spinner<Integer> countSpinner, ComboBox<String> strategyCombo) {
            this.chk = chk;
            this.name = name;
            this.role = role;
            this.countSpinner = countSpinner;
            this.strategyCombo = strategyCombo;
        }
    }

    static void setupAccessoryRows(
            String speciesName,
            VBox accessoryBoxPane,
            List<AccessoryRowControls> accessoryControlsList,
            Runnable onChange
    ) {
        accessoryBoxPane.getChildren().clear();
        accessoryControlsList.clear();
        List<SpeciesConfigCard.AccessorySpeciesInfo> compatList = SpeciesConfigCard.getCompatibleAccessorySpecies(speciesName);

        VBox listVBox = new VBox(4);
        for (SpeciesConfigCard.AccessorySpeciesInfo info : compatList) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);

            CheckBox chk = new CheckBox(info.name());
            chk.setSelected(true);
            chk.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
            chk.getStyleClass().add("bold-label");
            chk.setTooltip(new Tooltip(info.description()));
            chk.setPrefWidth(220);
            chk.selectedProperty().addListener((o, oldV, newV) -> {
                if (onChange != null) onChange.run();
            });

            Label lblRole = new Label("[" + info.role() + "]");
            lblRole.setStyle(getRoleStyle(info.role()));
            lblRole.getStyleClass().add("bold-label");
            lblRole.setPrefWidth(95);

            Label lblCount = new Label("Initial Count :");
            lblCount.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold;");
            lblCount.getStyleClass().add("bold-label");

            Spinner<Integer> countSpinner = new Spinner<>(5, 1000, info.defaultCount(), 10);
            countSpinner.setPrefWidth(70);
            countSpinner.setEditable(true);
            countSpinner.valueProperty().addListener((o, oldV, newV) -> {
                if (onChange != null) onChange.run();
            });

            Label lblStrategy = new Label("Replenishment Strategy :");
            lblStrategy.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold;");
            lblStrategy.getStyleClass().add("bold-label");

            ComboBox<String> strategyCombo = new ComboBox<>();
            strategyCombo.getItems().addAll(
                "🔄 Dynamic Equilibrium (Auto-maintained < 30% Pop)",
                "⚡ Episodic Incursions (Pop-in Waves every 500 Ticks)",
                "⏳ Finite Depletion (Limited Resource / Prey Reserves)",
                "🌿 Seasonal Logistic Regeneration (10-50%/day)"
            );
            strategyCombo.getSelectionModel().selectFirst();
            strategyCombo.setStyle("-fx-font-size: 9px;");
            strategyCombo.setTooltip(new Tooltip("Replenishment strategy to maintain pressure without destabilizing the simulation."));
            strategyCombo.valueProperty().addListener((o, oldV, newV) -> {
                if (onChange != null) onChange.run();
            });

            accessoryControlsList.add(new AccessoryRowControls(chk, info.name(), info.role(), countSpinner, strategyCombo));

            row.getChildren().addAll(chk, lblRole, lblCount, countSpinner, lblStrategy, strategyCombo);
            listVBox.getChildren().add(row);
        }
        accessoryBoxPane.getChildren().add(listVBox);
    }

    static String getRoleStyle(String role) {
        if (role == null) return "-fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 9.5px;";
        String r = role.toUpperCase().trim();
        if (r.contains("PROIE") || r.contains("PREY")) {
            return "-fx-text-fill: #22c55e; -fx-font-weight: bold; -fx-font-size: 9.5px;";
        } else if (r.contains("PRED") || r.contains("PRÉD")) {
            return "-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 9.5px;";
        } else if (r.contains("COMMENSAL") || r.contains("SYMBIOT") || r.contains("FLORA")) {
            return "-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 9.5px;";
        } else if (r.contains("PATHOG") || r.contains("PARASIT")) {
            return "-fx-text-fill: #eab308; -fx-font-weight: bold; -fx-font-size: 9.5px;";
        }
        return "-fx-text-fill: #a78bfa; -fx-font-weight: bold; -fx-font-size: 9.5px;";
    }

    static String getShortSpeciesName(String full) {
        if (full == null) return "";
        int idx = full.indexOf("(");
        return idx > 0 ? full.substring(0, idx).trim() : full;
    }
}
