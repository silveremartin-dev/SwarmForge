/*
 * SwarmForge - Ant Colony & Insect Simulation Platform
 * Copyright (c) 2024-2026 Silvère Martin-Michiellot & SwarmForge Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.swarmforge.client.ui.nest;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.ThemeManager;
import org.swarmforge.core.species.DefaultSpecies;

/**
 * Spatial Viability and Microclimatic Placement Diagnostics Card
 * for the Procedural Nest Architecture Studio.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class NestPlacementViabilityPane extends VBox {

    private Slider evalHeightSlider;
    private ComboBox<String> evalOrientationCombo;
    private Slider evalTempSlider;
    private Slider evalMoistureSlider;
    private Slider evalForagingSlider;
    private Slider evalCompactionSlider;

    private ProgressBar evalScoreProgressBar;
    private Label evalScoreLabel;
    private Label evalBadgeLabel;
    private VBox evalRecommendationsBox;

    private final Supplier<String> architectureSupplier;
    private final IntSupplier totalChambersSupplier;
    private final Supplier<DefaultSpecies> activeSpeciesSupplier;
    private final ToIntFunction<String> chamberCountFunction;

    public NestPlacementViabilityPane(
            Supplier<String> architectureSupplier,
            IntSupplier totalChambersSupplier,
            Supplier<DefaultSpecies> activeSpeciesSupplier,
            ToIntFunction<String> chamberCountFunction) {
        this.architectureSupplier = architectureSupplier;
        this.totalChambersSupplier = totalChambersSupplier;
        this.activeSpeciesSupplier = activeSpeciesSupplier;
        this.chamberCountFunction = chamberCountFunction;

        setSpacing(10);
        buildUI();
    }

    private void buildUI() {
        I18nManager i18n = I18nManager.getInstance();

        // 1. Environmental Placement Parameters Block
        Label envTitle = new Label();
        envTitle.textProperty().bind(i18n.createStringBinding("nest.env.title"));
        envTitle.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:#38bdf8;");

        evalHeightSlider = mkSlider(-2.0, 15.0, 0.0);
        evalHeightSlider.tooltipProperty().bind(i18n.createTooltipBinding("nest.env.height.tt"));
        evalHeightSlider.valueProperty().addListener((o, a, n) -> updatePlacementViabilityScore());
        HBox heightBox = createEvalSliderRow(i18n.get("nest.env.height.label"), evalHeightSlider, "m");

        evalOrientationCombo = new ComboBox<>();
        populateOrientationCombo();
        evalOrientationCombo.setPrefWidth(220);
        evalOrientationCombo.setOnAction(e -> updatePlacementViabilityScore());

        Label orientLabel = new Label();
        orientLabel.textProperty().bind(i18n.createStringBinding("nest.env.solar.label"));
        HBox orientRow = new HBox(8, orientLabel, evalOrientationCombo);
        orientRow.setAlignment(Pos.CENTER_LEFT);

        evalTempSlider = mkSlider(5.0, 42.0, 22.0);
        evalTempSlider.tooltipProperty().bind(i18n.createTooltipBinding("nest.env.temp.tt"));
        evalTempSlider.valueProperty().addListener((o, a, n) -> updatePlacementViabilityScore());
        HBox tempBox = createEvalSliderRow(i18n.get("nest.env.temp.label"), evalTempSlider, "°C");

        evalMoistureSlider = mkSlider(0.0, 100.0, 45.0);
        evalMoistureSlider.tooltipProperty().bind(i18n.createTooltipBinding("nest.env.moisture.tt"));
        evalMoistureSlider.valueProperty().addListener((o, a, n) -> updatePlacementViabilityScore());
        HBox moistureBox = createEvalSliderRow(i18n.get("nest.env.moisture.label"), evalMoistureSlider, "%");

        evalForagingSlider = mkSlider(5.0, 300.0, 35.0);
        evalForagingSlider.tooltipProperty().bind(i18n.createTooltipBinding("nest.env.foraging.tt"));
        evalForagingSlider.valueProperty().addListener((o, a, n) -> updatePlacementViabilityScore());
        HBox foragingBox = createEvalSliderRow(i18n.get("nest.env.foraging.label"), evalForagingSlider, "m");

        evalCompactionSlider = mkSlider(10.0, 150.0, 65.0);
        evalCompactionSlider.tooltipProperty().bind(i18n.createTooltipBinding("nest.env.compaction.tt"));
        evalCompactionSlider.valueProperty().addListener((o, a, n) -> updatePlacementViabilityScore());
        HBox compactionBox = createEvalSliderRow(i18n.get("nest.env.compaction.label"), evalCompactionSlider, "kPa");

        VBox envBlock = new VBox(6,
            envTitle, new Separator(),
            heightBox, orientRow, tempBox, moistureBox, foragingBox, compactionBox
        );
        envBlock.getStyleClass().add("card-pane");

        // 2. Real-Time Spatial Viability Diagnostics Card
        Label evalTitle = new Label();
        evalTitle.textProperty().bind(i18n.createStringBinding("nest.viability.title"));
        evalTitle.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:#38bdf8;");

        evalScoreProgressBar = new ProgressBar(0.85);
        evalScoreProgressBar.setPrefWidth(160);
        evalScoreProgressBar.setPrefHeight(16);

        evalScoreLabel = new Label("85%");
        evalScoreLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #22c55e;");

        evalBadgeLabel = new Label(i18n.get("nest.eval.badge.optimal"));
        evalBadgeLabel.setStyle("-fx-background-color: #15803d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 10px;");

        HBox scoreHeaderBox = new HBox(8, evalScoreProgressBar, evalScoreLabel, evalBadgeLabel);
        scoreHeaderBox.setAlignment(Pos.CENTER_LEFT);

        evalRecommendationsBox = new VBox(4);
        evalRecommendationsBox.setPadding(new Insets(6));
        evalRecommendationsBox.getStyleClass().add("card-pane");

        Label viabilityScoreLabel = new Label();
        viabilityScoreLabel.textProperty().bind(i18n.createStringBinding("nest.viability.score.label"));
        Label viabilityDiagLabel = new Label();
        viabilityDiagLabel.textProperty().bind(i18n.createStringBinding("nest.viability.diag.label"));

        VBox diagBlock = new VBox(6,
            evalTitle, new Separator(),
            viabilityScoreLabel,
            scoreHeaderBox,
            viabilityDiagLabel,
            evalRecommendationsBox
        );
        diagBlock.getStyleClass().add("card-pane");

        getChildren().addAll(envBlock, diagBlock);
        updatePlacementViabilityScore();
    }

    private void populateOrientationCombo() {
        if (evalOrientationCombo == null) return;
        I18nManager i18n = I18nManager.getInstance();
        int selIdx = evalOrientationCombo.getSelectionModel().getSelectedIndex();
        evalOrientationCombo.getItems().clear();
        evalOrientationCombo.getItems().addAll(
            i18n.get("nest.solar.east"),
            i18n.get("nest.solar.north"),
            i18n.get("nest.solar.south"),
            i18n.get("nest.solar.southeast"),
            i18n.get("nest.solar.west")
        );
        if (selIdx >= 0 && selIdx < evalOrientationCombo.getItems().size()) {
            evalOrientationCombo.getSelectionModel().select(selIdx);
        } else {
            evalOrientationCombo.getSelectionModel().selectFirst();
        }
    }

    public void updatePlacementViabilityScore() {
        if (evalScoreProgressBar == null) return;
        I18nManager i18n = I18nManager.getInstance();
        double height = evalHeightSlider != null ? evalHeightSlider.getValue() : 0.0;
        double temp = evalTempSlider != null ? evalTempSlider.getValue() : 22.0;
        double moisture = evalMoistureSlider != null ? evalMoistureSlider.getValue() : 45.0;
        double foraging = evalForagingSlider != null ? evalForagingSlider.getValue() : 35.0;
        double compaction = evalCompactionSlider != null ? evalCompactionSlider.getValue() : 65.0;

        String arch = architectureSupplier != null ? architectureSupplier.get() : "BURROW_UNDERGROUND";

        double score = 100.0;
        evalRecommendationsBox.getChildren().clear();

        // 1. Height & Elevation Clearance Checks
        if (arch.contains("BEEHIVE") || arch.contains("WAX_COMB") || arch.contains("beehive") || arch.contains("Ruche")) {
            if (height < 0.4) {
                score -= 25.0;
                addEvalRec(i18n.get("nest.eval.rec.beehive_low"));
            } else if (height > 2.5) {
                score -= 15.0;
                addEvalRec(i18n.get("nest.eval.rec.beehive_high"));
            } else {
                addEvalRec(i18n.get("nest.eval.rec.beehive_ideal"));
            }
        } else if (arch.contains("PAPER_PEDUNCULATE") || arch.contains("paper") || arch.contains("papier")) {
            if (height < 2.5) {
                score -= 35.0;
                addEvalRec(i18n.get("nest.eval.rec.paper_low"));
            } else {
                addEvalRec(i18n.get("nest.eval.rec.paper_optimal"));
            }
        } else if (arch.contains("BURROW") || arch.contains("FUNGI_VAULT") || arch.contains("subterranean") || arch.contains("souterrain")) {
            if (height > 0.5) {
                score -= 30.0;
                addEvalRec(i18n.get("nest.eval.rec.subterranean_high"));
            } else {
                addEvalRec(i18n.get("nest.eval.rec.subterranean_ideal"));
            }
        }

        // 2. Thermal Microclimate
        if (temp < 15.0) { score -= 20.0; addEvalRec(i18n.get("nest.eval.rec.temp_cold")); }
        else if (temp > 35.0) { score -= 25.0; addEvalRec(i18n.get("nest.eval.rec.temp_hot")); }
        else { addEvalRec(i18n.get("nest.eval.rec.temp_optimal")); }

        // 3. Moisture
        if (moisture < 20.0) { score -= 20.0; addEvalRec(i18n.get("nest.eval.rec.moisture_dry")); }
        else if (moisture > 80.0) { score -= 20.0; addEvalRec(i18n.get("nest.eval.rec.moisture_wet")); }
        else { addEvalRec(i18n.get("nest.eval.rec.moisture_balanced")); }

        // 4. Foraging
        if (foraging > 150.0) { score -= 20.0; addEvalRec(i18n.get("nest.eval.rec.foraging_far")); }
        else { addEvalRec(i18n.get("nest.eval.rec.foraging_near")); }

        // 5. Compaction
        if (compaction < 30.0) { score -= 15.0; addEvalRec(i18n.get("nest.eval.rec.compaction_loose")); }

        // 6. Colony Population vs Nest Capacity Validation & Spillover Diagnostics
        int totalChambers = totalChambersSupplier != null ? totalChambersSupplier.getAsInt() : 10;
        int estNestCap = Math.max(100, totalChambers * 100);
        DefaultSpecies species = activeSpeciesSupplier != null ? activeSpeciesSupplier.get() : null;
        int estPop = species != null ? species.getTypicalColonySize() : 300;
        int queenChambers = chamberCountFunction != null ? chamberCountFunction.applyAsInt("👑 Queen Chamber") : 1;
        int foodChambers = chamberCountFunction != null ? chamberCountFunction.applyAsInt("🍖 Food Storage") : 2;

        if (estPop > estNestCap) {
            score -= 15.0;
            addEvalRec(String.format(i18n.get("nest.eval.rec.cap_overflow"), estNestCap, estPop, estPop - estNestCap));
        } else {
            addEvalRec(String.format(i18n.get("nest.eval.rec.cap_sufficient"), estNestCap, estPop));
        }

        if (queenChambers == 0) {
            score -= 20.0;
            addEvalRec(i18n.get("nest.eval.rec.queen_missing"));
        }
        if (foodChambers == 0) {
            score -= 10.0;
            addEvalRec(i18n.get("nest.eval.rec.food_missing"));
        }

        score = Math.max(0.0, Math.min(100.0, score));
        evalScoreProgressBar.setProgress(score / 100.0);
        evalScoreLabel.setText(String.format("%.0f%%", score));

        if (score >= 80) {
            evalScoreProgressBar.setStyle("-fx-accent: #22c55e;");
            evalScoreLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #22c55e;");
            evalBadgeLabel.setText(i18n.get("nest.eval.badge.optimal"));
            evalBadgeLabel.setStyle("-fx-background-color: #15803d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 10px;");
        } else if (score >= 50) {
            evalScoreProgressBar.setStyle("-fx-accent: #f59e0b;");
            evalScoreLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #f59e0b;");
            evalBadgeLabel.setText(i18n.get("nest.eval.badge.viable"));
            evalBadgeLabel.setStyle("-fx-background-color: #b45309; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 10px;");
        } else {
            evalScoreProgressBar.setStyle("-fx-accent: #ef4444;");
            evalScoreLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
            evalBadgeLabel.setText(i18n.get("nest.eval.badge.critical"));
            evalBadgeLabel.setStyle("-fx-background-color: #b91c1c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 10px;");
        }
    }

    private void addEvalRec(String msg) {
        Label l = new Label(msg);
        boolean isDark = ThemeManager.getInstance().getCurrentTheme() == ThemeManager.Theme.DARK;
        String textFill = isDark ? "#e4e4e7" : "#0f172a";
        l.setStyle("-fx-font-size: 10px; -fx-text-fill: " + textFill + "; -fx-wrap-text: true;");
        evalRecommendationsBox.getChildren().add(l);
    }

    private HBox createEvalSliderRow(String labelText, Slider slider, String unit) {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(labelText);
        lbl.setPrefWidth(140);
        lbl.setStyle("-fx-font-size:11px;");

        Label valLbl = new Label(String.format("%.1f %s", slider.getValue(), unit));
        valLbl.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-min-width: 48; -fx-font-size:11px;");
        slider.valueProperty().addListener((o, a, n) -> valLbl.setText(String.format("%.1f %s", n.doubleValue(), unit)));

        row.getChildren().addAll(lbl, slider, valLbl);
        return row;
    }

    private Slider mkSlider(double min, double max, double val) {
        Slider s = new Slider(min, max, val);
        s.setShowTickMarks(false);
        s.setShowTickLabels(false);
        s.setPrefWidth(120);
        return s;
    }

    public Slider getEvalHeightSlider() { return evalHeightSlider; }
    public ComboBox<String> getEvalOrientationCombo() { return evalOrientationCombo; }
    public Slider getEvalTempSlider() { return evalTempSlider; }
    public Slider getEvalMoistureSlider() { return evalMoistureSlider; }
    public Slider getEvalForagingSlider() { return evalForagingSlider; }
    public Slider getEvalCompactionSlider() { return evalCompactionSlider; }
}
