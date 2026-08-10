/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.client.util.I18nManager;

import java.util.Map;

/**
 * Biologically grounded Hive & Nest Placement Evaluator.
 * Analyzes architectural topology, elevation height, solar exposure, moisture,
 * and floral proximity to compute an optimal placement score (0-100%) with recommendations.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class HivePlacementEvaluatorDialog extends Stage {

    private final Slider heightSlider;
    private final Slider tempSlider;
    private final Slider moistureSlider;
    private final Slider foragingSlider;
    private final Slider compactionSlider;
    private final ComboBox<String> orientationCombo;

    private final ProgressBar scoreProgressBar;
    private final Label scoreLabel;
    private final Label badgeLabel;
    private final VBox recommendationsBox;

    private final String architecture;
    private final String material;

    public HivePlacementEvaluatorDialog(Stage owner, Map<String, Object> nestConfig) {
        initModality(Modality.WINDOW_MODAL);
        if (owner != null) initOwner(owner);

        I18nManager i18n = I18nManager.getInstance();
        setTitle(i18n.get("nest.eval.title", "🧪 Évaluateur de Placement de Ruche & Nid"));

        this.architecture = nestConfig != null && nestConfig.containsKey("architecture") 
                ? (String) nestConfig.get("architecture") : "BURROW_UNDERGROUND";
        this.material = nestConfig != null && nestConfig.containsKey("material") 
                ? (String) nestConfig.get("material") : "EARTH";

        VBox root = new VBox(12);
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: #18181b; -fx-font-family: 'Segoe UI', sans-serif;");

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        FontIcon icon = new FontIcon(Feather.CHECK_CIRCLE);
        icon.setIconSize(24);
        icon.setIconColor(Color.web("#38bdf8"));

        Label title = new Label(i18n.get("nest.eval.header", "🧪 Évaluation & Validation du Placement Spatiale"));
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #f4f4f5;");

        header.getChildren().addAll(icon, title);

        Label subTitle = new Label("Architecture: " + architecture + " | Matériau: " + material);
        subTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #38bdf8; -fx-font-weight: bold;");

        // Left Panel: Environmental Controls
        VBox controlsCard = new VBox(8);
        controlsCard.setPadding(new Insets(12));
        controlsCard.getStyleClass().add("card-pane");
        controlsCard.setStyle("-fx-background-color: #27272a; -fx-background-radius: 8; -fx-border-color: #3f3f46; -fx-border-radius: 8;");

        Label ctrlTitle = new Label("📍 Conditions Environnementales du Terrarium :");
        ctrlTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #a1a1aa;");

        // 1. Height / Elevation
        double defaultHeight = architecture.contains("WOODEN_BEEHIVE") ? 1.2 
                            : architecture.contains("PAPER_PEDUNCULATE") ? 4.5 
                            : architecture.contains("ARBOREAL") ? 6.0 : 0.0;
        heightSlider = createSlider(-2.0, 15.0, defaultHeight);
        HBox heightBox = createSliderRow(i18n.get("nest.eval.height", "Hauteur & Élévation (m) :"), heightSlider, "m");

        // 2. Solar Orientation
        orientationCombo = new ComboBox<>();
        orientationCombo.getItems().addAll(
            "Sud-Est (South-East - Sun Morning)",
            "Sud (South - Full Solar)",
            "Est (East - Morning Light)",
            "Ouest (West - Evening Heat)",
            "Nord (North - Shaded / Cool)"
        );
        orientationCombo.getSelectionModel().selectFirst();
        orientationCombo.setPrefWidth(220);
        orientationCombo.setOnAction(e -> calculateScore());

        HBox orientRow = new HBox(8, new Label("Exposition Solaire :"), orientationCombo);
        orientRow.setAlignment(Pos.CENTER_LEFT);

        // 3. Ambient Microclimate Temp
        tempSlider = createSlider(5.0, 42.0, 22.0);
        HBox tempBox = createSliderRow(i18n.get("nest.eval.thermal", "Température Ambiante (°C) :"), tempSlider, "°C");

        // 4. Substrate Moisture
        moistureSlider = createSlider(0.0, 100.0, 45.0);
        HBox moistureBox = createSliderRow(i18n.get("nest.eval.moisture", "Humidité Substrat (%) :"), moistureSlider, "%");

        // 5. Floral Foraging Radius
        foragingSlider = createSlider(5.0, 300.0, 35.0);
        HBox foragingBox = createSliderRow(i18n.get("nest.eval.foraging", "Distance Fleurs / Eau (m) :"), foragingSlider, "m");

        // 6. Structural Anchor / Soil Compaction
        compactionSlider = createSlider(10.0, 150.0, 65.0);
        HBox compactionBox = createSliderRow(i18n.get("nest.eval.structural", "Compacité Support (kPa) :"), compactionSlider, "kPa");

        controlsCard.getChildren().addAll(ctrlTitle, new Separator(), heightBox, orientRow, tempBox, moistureBox, foragingBox, compactionBox);

        // Right Panel: Results & Recommendations
        VBox resultCard = new VBox(10);
        resultCard.setPadding(new Insets(12));
        resultCard.setStyle("-fx-background-color: #27272a; -fx-background-radius: 8; -fx-border-color: #3f3f46; -fx-border-radius: 8;");

        Label resTitle = new Label("📊 Score de Viabilité & Diagnostic :");
        resTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #a1a1aa;");

        scoreProgressBar = new ProgressBar(0.85);
        scoreProgressBar.setPrefWidth(320);
        scoreProgressBar.setPrefHeight(20);
        scoreProgressBar.setStyle("-fx-accent: #22c55e;");

        scoreLabel = new Label("85%");
        scoreLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #22c55e;");

        badgeLabel = new Label("🟢 EMPLACEMENT OPTIMAL");
        badgeLabel.setStyle("-fx-background-color: #15803d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 12;");

        HBox scoreHeaderBox = new HBox(12, scoreLabel, badgeLabel);
        scoreHeaderBox.setAlignment(Pos.CENTER_LEFT);

        recommendationsBox = new VBox(6);
        recommendationsBox.setPadding(new Insets(8));
        recommendationsBox.setStyle("-fx-background-color: #18181b; -fx-background-radius: 6;");

        resultCard.getChildren().addAll(resTitle, new Separator(), scoreProgressBar, scoreHeaderBox, new Label("💡 Recommandations & Alertes :"), recommendationsBox);

        HBox contentBox = new HBox(12, controlsCard, resultCard);
        HBox.setHgrow(controlsCard, Priority.ALWAYS);
        HBox.setHgrow(resultCard, Priority.ALWAYS);

        // Buttons Footer
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button btnClose = new Button("Fermer");
        btnClose.getStyleClass().add("btn-secondary");
        btnClose.setOnAction(e -> close());

        Button btnApply = new Button("✓ Appliquer les Ajustements");
        btnApply.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold;");
        btnApply.setOnAction(e -> close());

        footer.getChildren().addAll(btnClose, btnApply);

        root.getChildren().addAll(header, subTitle, new Separator(), contentBox, footer);

        Scene scene = new Scene(root, 680, 520);
        try {
            scene.getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());
        } catch (Exception ignored) {}
        setScene(scene);

        calculateScore();
    }

    private Slider createSlider(double min, double max, double value) {
        Slider s = new Slider(min, max, value);
        s.setPrefWidth(160);
        s.valueProperty().addListener((o, a, b) -> calculateScore());
        return s;
    }

    private HBox createSliderRow(String labelText, Slider slider, String unit) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Label lbl = new Label(labelText);
        lbl.setPrefWidth(170);

        Label valLbl = new Label(String.format("%.1f %s", slider.getValue(), unit));
        valLbl.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-min-width: 55;");
        slider.valueProperty().addListener((o, a, n) -> valLbl.setText(String.format("%.1f %s", n.doubleValue(), unit)));

        row.getChildren().addAll(lbl, slider, valLbl);
        return row;
    }

    private void calculateScore() {
        double height = heightSlider.getValue();
        double temp = tempSlider.getValue();
        double moisture = moistureSlider.getValue();
        double foraging = foragingSlider.getValue();
        double compaction = compactionSlider.getValue();
        String orient = orientationCombo.getValue() != null ? orientationCombo.getValue() : "";

        double score = 100.0;
        recommendationsBox.getChildren().clear();

        I18nManager i18n = I18nManager.getInstance();

        // 1. Height & Elevation Clearance Checks
        if (architecture.contains("WOODEN_BEEHIVE")) {
            if (height < 0.4) {
                score -= 25.0;
                addRec("⚠️ Ruche en Bois : Risque d'humidité et d'attaque par les prédateurs de sol. Élevez la ruche à au moins 0.5m du sol.");
            } else if (height > 2.5) {
                score -= 15.0;
                addRec("ℹ️ Hauteur élevée : Exposition au vent fort susceptible de perturber la planche d'envol des abeilles.");
            } else {
                addRec("✅ Élévation idéale (0.5m - 2.0m) : Isolation du sol mouillé et accès aisé.");
            }
        } else if (architecture.contains("PAPER_PEDUNCULATE")) {
            if (height < 2.5) {
                score -= 35.0;
                addRec("🚨 Guêpier Suspendu : Hauteur sous 2.5m vulnérable aux prédateurs terrestres et au passage mammifère.");
            } else {
                addRec("✅ Ancrage aérien optimal : Pédoncule fixé en hauteur à l'abri du sol.");
            }
        } else if (architecture.contains("BURROW_UNDERGROUND")) {
            if (height > 0.5) {
                score -= 30.0;
                addRec("⚠️ Galerie souterraine placée au-dessus de la surface du sol.");
            } else {
                addRec("✅ Profondeur idéale : Protection thermique naturelle de la terre.");
            }
        }

        // 2. Thermal Microclimate
        if (temp < 15.0) {
            score -= 20.0;
            addRec("🥶 Température froide (< 15°C) : Ralentissement du métabolisme du couvain.");
        } else if (temp > 35.0) {
            score -= 25.0;
            addRec("🔥 Stress thermique (> 35°C) : Risque de fonte de la cire ou de déshydratation des larves.");
        } else {
            addRec("✅ Température idéale (18°C - 28°C) pour l'incubation du couvain.");
        }

        // 3. Solar Orientation
        if (orient.contains("Sud-Est") || orient.contains("South-East")) {
            addRec("☀️ Orientation Sud-Est optimale : Le soleil du matin réchauffe l'entrée et stimule le départ au foraging.");
        } else if (orient.contains("Nord") || orient.contains("North")) {
            score -= 10.0;
            addRec("☁️ Orientation Nord : Ombrage permanent ralentissant le démarrage matinal.");
        }

        // 4. Substrate Moisture
        if (material.contains("WOOD") || material.contains("BEESWAX")) {
            if (moisture > 75.0) {
                score -= 20.0;
                addRec("💧 Humidité trop élevée (> 75%) : Risque de moisissure du bois et dégradation de la propolis.");
            }
        } else if (architecture.contains("SUBTERRANEAN_FUNGI_VAULT")) {
            if (moisture < 80.0) {
                score -= 30.0;
                addRec("🍄 Jardins à Champignons : Déshydratation critique du mycélium (exige > 85% d'humidité).");
            }
        }

        // 5. Foraging Proximity
        if (foraging > 150.0) {
            score -= 15.0;
            addRec("🌻 Distance florale / eau élevée (> 150m) : Augmentation du coût métabolique des allers-retours.");
        } else {
            addRec("🌻 Proximité florale excellente (< 50m).");
        }

        // Final Score Bounding
        score = Math.max(0.0, Math.min(100.0, score));

        scoreProgressBar.setProgress(score / 100.0);
        scoreLabel.setText(String.format("%.0f%%", score));

        if (score >= 85) {
            scoreProgressBar.setStyle("-fx-accent: #22c55e;");
            scoreLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #22c55e;");
            badgeLabel.setText("🟢 EMPLACEMENT OPTIMAL");
            badgeLabel.setStyle("-fx-background-color: #15803d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 12;");
        } else if (score >= 65) {
            scoreProgressBar.setStyle("-fx-accent: #eab308;");
            scoreLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #eab308;");
            badgeLabel.setText("🟡 EMPLACEMENT ACCEPTABLE");
            badgeLabel.setStyle("-fx-background-color: #a16207; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 12;");
        } else if (score >= 40) {
            scoreProgressBar.setStyle("-fx-accent: #f97316;");
            scoreLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #f97316;");
            badgeLabel.setText("🟠 EMPLACEMENT SOUS-OPTIMAL");
            badgeLabel.setStyle("-fx-background-color: #c2410c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 12;");
        } else {
            scoreProgressBar.setStyle("-fx-accent: #ef4444;");
            scoreLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
            badgeLabel.setText("🔴 EMPLACEMENT DANGEREUX");
            badgeLabel.setStyle("-fx-background-color: #b91c1c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 12;");
        }
    }

    private void addRec(String text) {
        Label l = new Label(text);
        l.setWrapText(true);
        l.setStyle("-fx-font-size: 11px; -fx-text-fill: #e4e4e7;");
        recommendationsBox.getChildren().add(l);
    }
}
