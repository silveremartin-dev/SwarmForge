/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.core.engine.ComputeAccelerationMode;
import org.swarmforge.core.engine.EnginePreferences;
import org.swarmforge.core.engine.SimulationEngineType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helper class constructing the configuration form grids (World/Weather,
 * DateTime/Seed, Physics/Compute Acceleration, Limits/Duration, and Species
 * section) for {@link SimulationControlPanel}.
 */
class SimulationConfigGridBuilder {

    private final SimulationControlPanel panel;
    private final I18nManager i18n = I18nManager.getInstance();

    SimulationConfigGridBuilder(SimulationControlPanel panel) {
        this.panel = panel;
    }

    Button createScenarioActionButton(String textKey, Feather icon, String styleClass, String tooltipKey, Runnable action) {
        Button btn = new Button();
        btn.textProperty().bind(i18n.createStringBinding(textKey));
        btn.setGraphic(new FontIcon(icon));
        btn.getStyleClass().add(styleClass);
        btn.setMinWidth(Region.USE_PREF_SIZE);
        btn.tooltipProperty().bind(i18n.createTooltipBinding(tooltipKey));
        btn.setOnAction(e -> action.run());
        return btn;
    }

    GridPane buildWorldWeatherGrid() {
        GridPane gridWorldWeather = new GridPane();
        gridWorldWeather.setHgap(10);
        gridWorldWeather.setVgap(8);

        Label lbl1World = new Label();
        lbl1World.textProperty().bind(i18n.createStringBinding("sim.step1.world"));
        lbl1World.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        lbl1World.getStyleClass().add("accent-title");
        lbl1World.setTooltip(new Tooltip("3D biotope and terrain relief type loaded from WorldPresetManager."));

        Label lbl2Weather = new Label();
        lbl2Weather.textProperty().bind(i18n.createStringBinding("sim.step2.weather"));
        lbl2Weather.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        lbl2Weather.getStyleClass().add("accent-title");
        lbl2Weather.setTooltip(new Tooltip("Climate profile and seasonal weather from WeatherPresetManager."));

        Button btnAlignWeather = new Button();
        btnAlignWeather.textProperty().bind(i18n.createStringBinding("sim.btn.align_weather"));
        btnAlignWeather.setStyle("-fx-background-color: #334155; -fx-text-fill: #e2e8f0; -fx-font-size: 10px;");
        btnAlignWeather.tooltipProperty().bind(i18n.createTooltipBinding("sim.btn.align_weather.tt"));
        btnAlignWeather.setOnAction(e -> panel.alignWeatherWithWorld(true));

        ToggleButton btnWeatherSim = new ToggleButton();
        btnWeatherSim.textProperty().bind(i18n.createStringBinding("sim.weather.simulated.btn"));
        btnWeatherSim.setSelected(true);
        btnWeatherSim.setStyle("-fx-font-size: 10px; -fx-padding: 2 6;");
        btnWeatherSim.tooltipProperty().bind(i18n.createTooltipBinding("sim.weather.simulated.tt"));

        ToggleButton btnWeatherReal = new ToggleButton();
        btnWeatherReal.textProperty().bind(i18n.createStringBinding("sim.weather.real.btn"));
        btnWeatherReal.setStyle("-fx-font-size: 10px; -fx-padding: 2 6;");
        btnWeatherReal.tooltipProperty().bind(i18n.createTooltipBinding("sim.weather.real.tt"));

        ToggleGroup weatherToggleGroup = new ToggleGroup();
        btnWeatherSim.setToggleGroup(weatherToggleGroup);
        btnWeatherReal.setToggleGroup(weatherToggleGroup);

        HBox modeSwitcher = new HBox(4, btnWeatherSim, btnWeatherReal);
        modeSwitcher.setAlignment(Pos.CENTER_LEFT);

        HBox simulatedWeatherRow = new HBox(6, panel.comboWeather, btnAlignWeather);
        simulatedWeatherRow.setAlignment(Pos.CENTER_LEFT);

        TextField txtRealCity = new TextField("Paris");
        txtRealCity.setPrefWidth(125);
        txtRealCity.setStyle("-fx-font-size: 10px;");
        txtRealCity.setPromptText("Ville ou Lat, Lon...");
        Button btnFetchReal = new Button("Obtenir");
        btnFetchReal.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold;");
        btnFetchReal.setOnAction(e -> panel.fetchRealWeather(txtRealCity.getText()));
        txtRealCity.setOnAction(e -> panel.fetchRealWeather(txtRealCity.getText()));

        Button btnSyncFromWorld = new Button();
        btnSyncFromWorld.setGraphic(new FontIcon(Feather.MAP_PIN));
        btnSyncFromWorld.setStyle("-fx-background-color: #334155; -fx-text-fill: #38bdf8; -fx-font-size: 10px;");
        btnSyncFromWorld.setTooltip(new Tooltip("Synchroniser avec les coordonnées (Lat/Lon/Alt) du Monde actuel"));
        btnSyncFromWorld.setOnAction(e -> panel.fetchRealWeatherFromWorld(txtRealCity));

        panel.lblRealWeatherStatus.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");
        panel.lblRealWeatherStatus.setText("Cliquez pour charger Open-Meteo");

        HBox realWeatherRow = new HBox(5, txtRealCity, btnFetchReal, btnSyncFromWorld, panel.lblRealWeatherStatus);
        realWeatherRow.setAlignment(Pos.CENTER_LEFT);
        realWeatherRow.setVisible(false);
        realWeatherRow.setManaged(false);

        btnWeatherSim.setOnAction(e -> {
            btnWeatherSim.setSelected(true);
            panel.isRealWeatherMode = false;
            simulatedWeatherRow.setVisible(true);
            simulatedWeatherRow.setManaged(true);
            realWeatherRow.setVisible(false);
            realWeatherRow.setManaged(false);
        });

        btnWeatherReal.setOnAction(e -> {
            btnWeatherReal.setSelected(true);
            panel.isRealWeatherMode = true;
            simulatedWeatherRow.setVisible(false);
            simulatedWeatherRow.setManaged(false);
            realWeatherRow.setVisible(true);
            realWeatherRow.setManaged(true);
            if (panel.cachedRealWeatherData == null) {
                panel.fetchRealWeatherFromWorld(txtRealCity);
            }
        });

        VBox weatherControlsBox = new VBox(4, modeSwitcher, simulatedWeatherRow, realWeatherRow);

        gridWorldWeather.add(lbl1World, 0, 0);
        gridWorldWeather.add(panel.comboWorld, 1, 0);
        gridWorldWeather.add(lbl2Weather, 0, 1);
        gridWorldWeather.add(weatherControlsBox, 1, 1);
        return gridWorldWeather;
    }

    GridPane buildDateTimeSeedGrid() {
        GridPane gridDateTimeSeed = new GridPane();
        gridDateTimeSeed.setHgap(10);
        gridDateTimeSeed.setVgap(8);

        Label lbl3DateTime = new Label();
        lbl3DateTime.textProperty().bind(i18n.createStringBinding("sim.step3.datetime"));
        lbl3DateTime.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        lbl3DateTime.getStyleClass().add("accent-title");

        HBox startDateTimeBox = panel.playbackSubPanel.buildStartDateTimeBox();

        Label lblSeedTitle = new Label();
        lblSeedTitle.textProperty().bind(i18n.createStringBinding("sim.step4.seed"));
        lblSeedTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        lblSeedTitle.getStyleClass().add("accent-title");
        lblSeedTitle.setTooltip(new Tooltip("Master random seed controlling exact scenario determinism and reproducibility."));

        panel.txtSeed.setPrefWidth(110);
        panel.txtSeed.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        panel.txtSeed.setTooltip(new Tooltip("Numerical value of the random seed."));

        Button btnRandSeed = new Button();
        btnRandSeed.textProperty().bind(i18n.createStringBinding("sim.btn.rand_seed"));
        btnRandSeed.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-font-size: 10px;");
        btnRandSeed.tooltipProperty().bind(i18n.createTooltipBinding("sim.btn.rand_seed.tt"));
        btnRandSeed.disableProperty().bind(panel.txtSeed.disabledProperty());
        btnRandSeed.setOnAction(e -> {
            panel.txtSeed.setText(String.valueOf((long) (Math.random() * 900000 + 100000)));
            panel.updateValidationPanel();
        });

        HBox seedBox = new HBox(6, panel.txtSeed, btnRandSeed);
        seedBox.setAlignment(Pos.CENTER_LEFT);

        gridDateTimeSeed.add(lbl3DateTime, 0, 0);
        gridDateTimeSeed.add(startDateTimeBox, 1, 0);
        gridDateTimeSeed.add(lblSeedTitle, 0, 1);
        gridDateTimeSeed.add(seedBox, 1, 1);
        return gridDateTimeSeed;
    }

    GridPane buildPhysicsGrid() {
        GridPane gridPhysics = new GridPane();
        gridPhysics.setHgap(10);
        gridPhysics.setVgap(8);

        Label lbl4Step = new Label();
        lbl4Step.textProperty().bind(i18n.createStringBinding("sim.step5.step"));
        lbl4Step.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        lbl4Step.getStyleClass().add("accent-title");
        lbl4Step.setTooltip(new Tooltip("Defines the Δt numerical integration fidelity for physics and ethology equations."));

        panel.scenarioStepCombo.getItems().setAll(
                "16.6 ms (60 Hz - Max Physics Fidelity / Default)",
                "50 ms (20 Hz - Standard Precision)",
                "100 ms (10 Hz - Fast Mode)",
                "1.0 s (Macroscopic Ecosystem Mode)",
                "5.0 s (Ultra Macroscopic Mode)"
        );
        panel.scenarioStepCombo.getSelectionModel().selectFirst();
        panel.scenarioStepCombo.setPrefWidth(160);
        panel.scenarioStepCombo.setStyle("-fx-font-size: 11px;");
        panel.scenarioStepCombo.setTooltip(new Tooltip("Fixed Δt calculation step persisted in scenario (guaranteeing perfect seed-based determinism)."));
        panel.scenarioStepCombo.setOnAction(e -> {
            int idx = panel.scenarioStepCombo.getSelectionModel().getSelectedIndex();
            switch (idx) {
                case 0 -> panel.simulationStepSeconds = 0.0166f;
                case 1 -> panel.simulationStepSeconds = 0.05f;
                case 2 -> panel.simulationStepSeconds = 0.1f;
                case 3 -> panel.simulationStepSeconds = 1.0f;
                case 4 -> panel.simulationStepSeconds = 5.0f;
            }
            panel.updateStepDtLabel();
            panel.updateValidationPanel();
            if (panel.onStepChange != null) panel.onStepChange.accept(panel.simulationStepSeconds);
        });

        Label lblDeterminismLegend = new Label();
        lblDeterminismLegend.textProperty().bind(i18n.createStringBinding("sim.step5.determinism_legend"));
        lblDeterminismLegend.setStyle("-fx-font-size: 9.5px; -fx-font-style: italic;");
        lblDeterminismLegend.getStyleClass().add("sub-title-gray");

        VBox physicsBox = new VBox(4, panel.scenarioStepCombo, lblDeterminismLegend);
        gridPhysics.add(lbl4Step, 0, 0);
        gridPhysics.add(physicsBox, 1, 0);

        Label lblEngine = new Label();
        lblEngine.textProperty().bind(i18n.createStringBinding("sim.engine.backend", "Moteur de Calcul (Engine)"));
        lblEngine.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        lblEngine.getStyleClass().add("accent-title");
        lblEngine.tooltipProperty().bind(i18n.createTooltipBinding("sim.engine.backend.tt"));

        updateEngineAndAccelComboTexts();
        i18n.localeProperty().addListener((obs, oldLoc, newLoc) -> updateEngineAndAccelComboTexts());

        panel.engineBackendCombo.setPrefWidth(240);
        panel.engineBackendCombo.setStyle("-fx-font-size: 10px;");
        panel.engineBackendCombo.tooltipProperty().bind(i18n.createTooltipBinding("sim.engine.backend.tt"));
        panel.engineBackendCombo.getSelectionModel().selectedIndexProperty().addListener((o, oldIdx, newIdx) -> {
            if (newIdx != null) {
                int idx = newIdx.intValue();
                if (idx == 1) {
                    EnginePreferences.setSelectedEngineType(SimulationEngineType.JAVA_ECS);
                } else if (idx == 2) {
                    EnginePreferences.setSelectedEngineType(SimulationEngineType.RUST_NATIVE);
                } else {
                    EnginePreferences.resetToDefaults();
                }
            }
        });

        Label lblAccel = new Label();
        lblAccel.textProperty().bind(i18n.createStringBinding("sim.compute.acceleration", "Accélération Matérielle"));
        lblAccel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        lblAccel.getStyleClass().add("accent-title");
        lblAccel.tooltipProperty().bind(i18n.createTooltipBinding("sim.compute.acceleration.tt"));

        panel.computeAccelerationCombo.setPrefWidth(240);
        panel.computeAccelerationCombo.setStyle("-fx-font-size: 10px;");
        panel.computeAccelerationCombo.tooltipProperty().bind(i18n.createTooltipBinding("sim.compute.acceleration.tt"));
        panel.computeAccelerationCombo.getSelectionModel().selectedIndexProperty().addListener((o, oldIdx, newIdx) -> {
            if (newIdx != null) {
                int idx = newIdx.intValue();
                if (idx == 1) {
                    EnginePreferences.setSelectedAccelerationMode(ComputeAccelerationMode.GPU_ACCELERATED);
                } else if (idx == 2) {
                    EnginePreferences.setSelectedAccelerationMode(ComputeAccelerationMode.CPU_MULTITHREADED_SIMD);
                } else {
                    EnginePreferences.setSelectedAccelerationMode(ComputeAccelerationMode.AUTO);
                }
            }
        });

        gridPhysics.add(lblEngine, 0, 1);
        gridPhysics.add(panel.engineBackendCombo, 1, 1);
        gridPhysics.add(lblAccel, 0, 2);
        gridPhysics.add(panel.computeAccelerationCombo, 1, 2);
        return gridPhysics;
    }

    GridPane buildLimitsGrid() {
        GridPane gridLimits = new GridPane();
        gridLimits.setHgap(10);
        gridLimits.setVgap(8);

        Label lblMaxTicks = new Label();
        lblMaxTicks.textProperty().bind(i18n.createStringBinding("sim.step6.duration"));
        lblMaxTicks.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        lblMaxTicks.getStyleClass().add("accent-title");
        lblMaxTicks.setTooltip(new Tooltip("Maximum simulation duration. Select value and unit (Days by default, s, min, h, months, years, ticks, or unlimited)."));

        panel.maxDurationSpinner.setPrefWidth(90);
        panel.maxDurationSpinner.setEditable(true);
        panel.maxDurationSpinner.setTooltip(new Tooltip("Maximum execution duration value in selected unit."));
        panel.maxDurationSpinner.valueProperty().addListener((o, oldV, newV) -> {
            panel.updateDurationCalculatedLabel();
            panel.updateValidationPanel();
        });

        panel.durationUnitCombo.getItems().setAll(
                "Days (d)", "Hours (h)", "Minutes (min)", "Seconds (s)",
                "Months (30d)", "Years (365d)", "Ticks", "∞ Unlimited"
        );
        panel.durationUnitCombo.getSelectionModel().select("Days (d)");
        panel.durationUnitCombo.setPrefWidth(140);
        panel.durationUnitCombo.setStyle("-fx-font-size: 10px;");
        panel.durationUnitCombo.setTooltip(new Tooltip(i18n.get("sim.duration_unit.tt")));
        panel.durationUnitCombo.valueProperty().addListener((o, oldV, newV) -> {
            boolean isUnlimited = "∞ Unlimited".equals(newV) || "∞ Illimité".equals(newV);
            panel.maxDurationSpinner.setDisable(isUnlimited);
            panel.updateDurationCalculatedLabel();
            panel.updateValidationPanel();
        });

        panel.lblDurationCalculatedInfo.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold;");
        panel.lblDurationCalculatedInfo.getStyleClass().add("accent-title");
        panel.updateDurationCalculatedLabel();

        HBox durationBox = new HBox(6, panel.maxDurationSpinner, panel.durationUnitCombo);
        durationBox.setAlignment(Pos.CENTER_LEFT);
        VBox durationFullBox = new VBox(4, durationBox, panel.lblDurationCalculatedInfo);

        Label lblMinPop = new Label();
        lblMinPop.textProperty().bind(i18n.createStringBinding("sim.step7.minpop"));
        lblMinPop.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        lblMinPop.getStyleClass().add("accent-title");
        lblMinPop.setTooltip(new Tooltip("Minimum total living population threshold. If living count drops below this, simulation stops automatically."));

        panel.minPopStopSpinner.setPrefWidth(90);
        panel.minPopStopSpinner.setEditable(true);
        panel.minPopStopSpinner.setTooltip(new Tooltip("Minimum living population triggering emergency stop (0 = disabled)."));
        panel.minPopStopSpinner.valueProperty().addListener((o, oldV, newV) -> panel.updateValidationPanel());

        gridLimits.add(lblMaxTicks, 0, 0);
        gridLimits.add(durationFullBox, 1, 0);
        gridLimits.add(lblMinPop, 0, 1);
        gridLimits.add(panel.minPopStopSpinner, 1, 1);
        return gridLimits;
    }

    VBox buildSpeciesSection() {
        VBox section3Container = new VBox(8);
        section3Container.getStyleClass().add("card-pane");

        HBox speciesAddBar = new HBox(8);
        speciesAddBar.setAlignment(Pos.CENTER_LEFT);

        Label lbl5SpeciesHeader = new Label();
        lbl5SpeciesHeader.textProperty().bind(i18n.createStringBinding("sim.step8.species"));
        lbl5SpeciesHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        lbl5SpeciesHeader.getStyleClass().add("accent-title");

        List<String> sortedSpeciesNames = new ArrayList<>(panel.speciesPresetManager.getPresetNames());
        Collections.sort(sortedSpeciesNames);
        panel.comboAvailableSpecies.getItems().setAll(sortedSpeciesNames);
        if (!panel.comboAvailableSpecies.getItems().isEmpty()) {
            panel.comboAvailableSpecies.getSelectionModel().selectFirst();
        }
        panel.comboAvailableSpecies.setPrefWidth(160);

        Button btnAddSpeciesToScenario = new Button();
        btnAddSpeciesToScenario.textProperty().bind(i18n.createStringBinding("sim.btn.add_species"));
        btnAddSpeciesToScenario.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        btnAddSpeciesToScenario.setTooltip(new Tooltip());
        btnAddSpeciesToScenario.getTooltip().textProperty().bind(i18n.createStringBinding("sim.btn.add_species.tt"));
        btnAddSpeciesToScenario.setOnAction(e -> {
            String selected = panel.comboAvailableSpecies.getValue();
            if (selected != null && !selected.isEmpty()) {
                panel.addSpeciesCard(selected);
            }
        });

        Button btnAutoPreset = new Button();
        btnAutoPreset.textProperty().bind(i18n.createStringBinding("sim.btn.recommended_preset"));
        btnAutoPreset.setStyle("-fx-background-color: #334155; -fx-text-fill: #e2e8f0; -fx-font-size: 10px;");
        btnAutoPreset.setTooltip(new Tooltip());
        btnAutoPreset.getTooltip().textProperty().bind(i18n.createStringBinding("sim.btn.rec_preset.tt"));
        btnAutoPreset.setOnAction(e -> panel.applyMetaPreset(panel.comboMeta.getValue()));

        speciesAddBar.getChildren().addAll(lbl5SpeciesHeader, panel.comboAvailableSpecies, btnAddSpeciesToScenario, btnAutoPreset);

        panel.addSpeciesCard("Black Garden Ant (Lasius niger)");

        section3Container.getChildren().addAll(speciesAddBar, panel.speciesListContainer);
        return section3Container;
    }

    void updateEngineAndAccelComboTexts() {
        int engineIdx = Math.max(0, panel.engineBackendCombo.getSelectionModel().getSelectedIndex());
        panel.engineBackendCombo.getItems().setAll(
                i18n.get("sim.engine.backend.auto"),
                i18n.get("sim.engine.backend.java"),
                i18n.get("sim.engine.backend.rust")
        );
        panel.engineBackendCombo.getSelectionModel().select(engineIdx);

        int accelIdx = Math.max(0, panel.computeAccelerationCombo.getSelectionModel().getSelectedIndex());
        panel.computeAccelerationCombo.getItems().setAll(
                i18n.get("sim.compute.acceleration.auto"),
                i18n.get("sim.compute.acceleration.gpu"),
                i18n.get("sim.compute.acceleration.cpu")
        );
        panel.computeAccelerationCombo.getSelectionModel().select(accelIdx);
    }
}
