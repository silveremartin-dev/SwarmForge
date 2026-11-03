/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.client.util.I18nManager;

/**
 * Builds Apex Predator Invasions and Genetic Mutations / Mutagens intervention cards
 * for {@link InterventionCategoryBuilder}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
class InterventionBiologicalCategoryBuilder {

    private final InterventionCategoryBuilder parent;
    private final InterventionPanel panel;

    InterventionBiologicalCategoryBuilder(InterventionCategoryBuilder parent, InterventionPanel panel) {
        this.parent = parent;
        this.panel = panel;
    }

    Node createInvasionSubBlockNode() {
        I18nManager i18n = I18nManager.getInstance();
        VBox box = new VBox(10);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        Label lblType = new Label();
        lblType.textProperty().bind(i18n.createStringBinding("god.invasion.type.label"));
        lblType.setStyle("-fx-font-weight: bold;");
        parent.invasionTypeSelect = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(
                "Anteater Raid",
                "Army Ant Raid",
                "Parasitic Mite Infestation",
                "Locust Swarm",
                "Wasp Squad Attack",
                "Spiders & Mantis Raid",
                "🛡️ Protection / Absence de Prédateurs (Safe Sanctuary)"
        ));
        parent.invasionTypeSelect.getSelectionModel().selectFirst();
        parent.invasionTypeSelect.setPrefWidth(300);
        parent.invasionTypeSelect.tooltipProperty().bind(i18n.createTooltipBinding("god.invasion.type.tt"));

        Label lblCount = new Label();
        lblCount.textProperty().bind(i18n.createStringBinding("god.invasion.count.label"));
        lblCount.setStyle("-fx-font-weight: bold;");
        parent.invasionCountSpinner = new Spinner<>(0, 500, 25);
        parent.invasionCountSpinner.setEditable(true);
        parent.invasionCountSpinner.setPrefWidth(120);
        parent.invasionCountSpinner.tooltipProperty().bind(i18n.createTooltipBinding("god.invasion.count.tt"));

        Label lblInvasionNote = new Label();
        lblInvasionNote.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px; -fx-font-style: italic;");

        Label lblPredatorLifecycleNote = new Label();
        lblPredatorLifecycleNote.textProperty().bind(i18n.createStringBinding("god.invasion.lifecycle_note"));
        lblPredatorLifecycleNote.getStyleClass().add("legend-hover-info");
        lblPredatorLifecycleNote.setStyle("-fx-font-size: 11px; -fx-font-style: italic;");
        lblPredatorLifecycleNote.setWrapText(true);

        Runnable updateInvasionStatus = () -> {
            String type = parent.invasionTypeSelect.getValue();
            boolean isProtection = type != null && (type.contains("Protection") || type.contains("Absence") || type.contains("Sanctuary"));
            Integer cnt = parent.invasionCountSpinner.getValue();
            boolean isZero = cnt != null && cnt == 0;

            if (isProtection) {
                parent.invasionCountSpinner.setDisable(true);
                lblInvasionNote.setText(i18n.get("god.invasion.sanctuary"));
                lblInvasionNote.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 11px; -fx-font-weight: bold;");
            } else if (isZero) {
                parent.invasionCountSpinner.setDisable(false);
                lblInvasionNote.setText(i18n.get("god.invasion.zero_pressure"));
                lblInvasionNote.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 11px; -fx-font-weight: bold;");
            } else {
                parent.invasionCountSpinner.setDisable(false);
                lblInvasionNote.setText(i18n.get("god.invasion.active_raid", (cnt != null ? cnt : 0)));
                lblInvasionNote.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px; -fx-font-style: italic;");
            }
        };

        parent.invasionTypeSelect.valueProperty().addListener((o, a, b) -> updateInvasionStatus.run());
        parent.invasionCountSpinner.valueProperty().addListener((o, a, b) -> updateInvasionStatus.run());
        updateInvasionStatus.run();

        Label lblDuration = new Label();
        lblDuration.textProperty().bind(i18n.createStringBinding("god.invasion.duration.label"));
        lblDuration.setStyle("-fx-font-weight: bold;");
        parent.invasionDurationSlider = new Slider(15, 43200, 120);
        parent.invasionDurationSlider.setPrefWidth(180);
        parent.invasionDurationSlider.tooltipProperty().bind(i18n.createTooltipBinding("god.invasion.duration.tt"));
        parent.invasionDurationValLabel = new Label("2h (120 min)");
        parent.invasionDurationValLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
        parent.invasionDurationSlider.valueProperty().addListener((o, a, b) -> {
            int mins = b.intValue();
            parent.invasionDurationValLabel.setText(mins < 60 ? mins + " min" : (mins < 1440) ? (mins / 60) + "h " + (mins % 60) + "m" : (mins / 1440) + " Days");
        });

        grid.add(lblType, 0, 0); grid.add(parent.invasionTypeSelect, 1, 0);
        grid.add(lblCount, 0, 1); grid.add(parent.invasionCountSpinner, 1, 1);
        grid.add(lblDuration, 0, 2); grid.add(parent.invasionDurationSlider, 1, 2); grid.add(parent.invasionDurationValLabel, 2, 2);

        Button btnScheduleInvasion = new Button();
        btnScheduleInvasion.textProperty().bind(i18n.createStringBinding("god.invasion.btn"));
        btnScheduleInvasion.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnScheduleInvasion.setGraphic(new FontIcon(Feather.SHIELD_OFF));
        btnScheduleInvasion.tooltipProperty().bind(i18n.createTooltipBinding("god.invasion.btn.tt"));
        btnScheduleInvasion.setOnAction(e -> panel.scheduler.scheduleInvasionEvent());

        box.getChildren().addAll(grid, lblInvasionNote, lblPredatorLifecycleNote, btnScheduleInvasion);
        return box;
    }

    Node createMutationSubBlockNode() {
        I18nManager i18n = I18nManager.getInstance();
        VBox box = new VBox(10);

        Label lblMutationTargetNote = new Label();
        lblMutationTargetNote.textProperty().bind(i18n.createStringBinding("god.mutation.targetnote"));
        lblMutationTargetNote.setStyle("-fx-text-fill: #ec4899; -fx-font-size: 11px; -fx-font-style: italic;");
        lblMutationTargetNote.tooltipProperty().bind(i18n.createTooltipBinding("god.mutation.targetnote.tt"));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        Label lblType = new Label();
        lblType.textProperty().bind(i18n.createStringBinding("god.mutation.type.label"));
        lblType.setStyle("-fx-font-weight: bold;");
        parent.mutationTypeSelect = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(
                "Royal Jelly Overfeeding (Queen Oviposition Boost)",
                "Frenzy & Metabolism (Attack & Speed Boost)",
                "Biological Longevity (Senescence Protection)",
                "Anarchy & Caste Perturbation (Role Disorganization)"
        ));
        parent.mutationTypeSelect.getSelectionModel().selectFirst();
        parent.mutationTypeSelect.setPrefWidth(340);
        parent.mutationTypeSelect.tooltipProperty().bind(i18n.createTooltipBinding("god.mutation.type.tt"));

        Label lblCasteScope = new Label();
        lblCasteScope.textProperty().bind(i18n.createStringBinding("god.mutation.caste.label"));
        lblCasteScope.setStyle("-fx-font-weight: bold;");
        parent.mutationCasteScopeSelect = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(
                "👑 All Castes (Queen, Workers, Soldiers, Brood)",
                "🔨 Workers & Foragers Only",
                "🛡️ Soldier Caste Only",
                "👑 Queen Only (Reproductive Lineage)",
                "🥚 Brood Only (Eggs, Larvae, Pupae)"
        ));
        parent.mutationCasteScopeSelect.getSelectionModel().selectFirst();
        parent.mutationCasteScopeSelect.setPrefWidth(340);
        parent.mutationCasteScopeSelect.tooltipProperty().bind(i18n.createTooltipBinding("god.mutation.caste.tt"));

        Label lblIntensity = new Label();
        lblIntensity.textProperty().bind(i18n.createStringBinding("god.mutation.intensity.label"));
        lblIntensity.setStyle("-fx-font-weight: bold;");
        parent.mutationIntensitySlider = new Slider(0.1, 5.0, 1.5);
        parent.mutationIntensitySlider.setPrefWidth(180);
        parent.mutationIntensitySlider.tooltipProperty().bind(i18n.createTooltipBinding("god.mutation.intensity.tt"));
        parent.mutationIntensityValLabel = new Label("1.5x (+50% Boost)");
        parent.mutationIntensityValLabel.setStyle("-fx-text-fill: #ec4899; -fx-font-weight: bold;");
        parent.mutationIntensitySlider.valueProperty().addListener((o, a, b) -> {
            double v = b.doubleValue();
            if (v < 1.0) {
                parent.mutationIntensityValLabel.setText(String.format("%.2fx (-%.0f%% Weakening)", v, (1.0 - v) * 100.0));
                parent.mutationIntensityValLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            } else if (Math.abs(v - 1.0) < 0.05) {
                parent.mutationIntensityValLabel.setText("1.00x (Neutral Baseline)");
                parent.mutationIntensityValLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold;");
            } else {
                parent.mutationIntensityValLabel.setText(String.format("%.1fx (+%.0f%% Boost)", v, (v - 1.0) * 100.0));
                parent.mutationIntensityValLabel.setStyle("-fx-text-fill: #ec4899; -fx-font-weight: bold;");
            }
        });

        Label lblDur = new Label();
        lblDur.textProperty().bind(i18n.createStringBinding("god.mutation.duration.label"));
        lblDur.setStyle("-fx-font-weight: bold;");
        parent.mutationDurationSlider = new Slider(5, 1440, 60);
        parent.mutationDurationSlider.setPrefWidth(180);
        parent.mutationDurationSlider.tooltipProperty().bind(i18n.createTooltipBinding("god.mutation.duration.tt"));
        parent.mutationDurationValLabel = new Label("60 min (1h)");
        parent.mutationDurationValLabel.setStyle("-fx-text-fill: #ec4899; -fx-font-weight: bold;");
        parent.mutationDurationSlider.valueProperty().addListener((o, a, b) -> {
            int mins = b.intValue();
            parent.mutationDurationValLabel.setText(mins < 60 ? mins + " min" : (mins / 60) + "h " + (mins % 60) + "m");
        });

        parent.mutationExplanationLabel = new Label();
        parent.mutationExplanationLabel.setWrapText(true);
        parent.mutationExplanationLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #f472b6; -fx-background-color: rgba(236,72,153,0.1); -fx-padding: 8; -fx-background-radius: 4;");

        Runnable updateMutationExplanation = () -> {
            String type = parent.mutationTypeSelect.getValue();
            double mult = parent.mutationIntensitySlider.getValue();
            double pct = (mult - 1.0) * 100.0;
            if (type != null && type.contains("Royal Jelly")) {
                parent.mutationExplanationLabel.setText(i18n.get("god.mutation.exp.royal_jelly", pct));
            } else if (type != null && type.contains("Frenzy")) {
                parent.mutationExplanationLabel.setText(i18n.get("god.mutation.exp.frenzy", mult));
            } else if (type != null && type.contains("Longevity")) {
                parent.mutationExplanationLabel.setText(i18n.get("god.mutation.exp.longevity"));
            } else if (type != null && type.contains("Anarchy")) {
                parent.mutationExplanationLabel.setText(i18n.get("god.mutation.exp.anarchy"));
            }
        };

        parent.mutationTypeSelect.valueProperty().addListener((o, a, b) -> updateMutationExplanation.run());
        parent.mutationIntensitySlider.valueProperty().addListener((o, a, b) -> updateMutationExplanation.run());
        updateMutationExplanation.run();

        grid.add(lblType, 0, 0); grid.add(parent.mutationTypeSelect, 1, 0);
        grid.add(lblCasteScope, 0, 1); grid.add(parent.mutationCasteScopeSelect, 1, 1);
        grid.add(lblIntensity, 0, 2); grid.add(parent.mutationIntensitySlider, 1, 2); grid.add(parent.mutationIntensityValLabel, 2, 2);
        grid.add(lblDur, 0, 3); grid.add(parent.mutationDurationSlider, 1, 3); grid.add(parent.mutationDurationValLabel, 2, 3);

        Button btnScheduleMutation = new Button();
        btnScheduleMutation.textProperty().bind(i18n.createStringBinding("god.mutation.btn"));
        btnScheduleMutation.setStyle("-fx-background-color: #ec4899; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnScheduleMutation.setGraphic(new FontIcon(Feather.ACTIVITY));
        btnScheduleMutation.tooltipProperty().bind(i18n.createTooltipBinding("god.mutation.btn.tt"));
        btnScheduleMutation.setOnAction(e -> panel.scheduler.scheduleMutationEvent());

        box.getChildren().addAll(lblMutationTargetNote, grid, parent.mutationExplanationLabel, btnScheduleMutation);
        return box;
    }
}
