/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import org.swarmforge.client.util.I18nManager;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.core.behavior.ReasoningArchitecture.ArchitectureType;
import org.swarmforge.core.domain.CasteTemplate;
import org.swarmforge.core.species.CustomSpecies;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

/**
 * Advanced Simulation Control and Scenario Manager Panel.
 * <p>
 * Structured strict scenario ordering:
 * 1. Preset du Monde (Biotope / World Preset)
 * 2. Preset Météo (Weather & Climate Profile)
 * 3. Dynamic Multi-Species & Ecosystem Scenario Configuration List:
 *    - Choice of species to add
 *    - Filtered nest types per species
 *    - Nest placement strategy & Pre-generation options
 *    - Biologically filtered accessory species (Preys, Predators, Commensals, Pathogens)
 *    - Demographics (Workers, Soldiers, Queens) and Caste AI Engines per species
 *    - Initial population counts & Renewal / Replenishment balance strategies for accessories
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class SimulationControlPanel extends VBox {

    private final Button btnGoToBeginning;
    private final Button btnRewind;
    private final Button btnStepBack;
    private final Button btnPlay;
    private final Button btnPause;
    private final Button btnStepForward;
    private final Button btnFastForward;
    private final Button btnGoToEnd;

    private final Slider speedSlider;
    private Slider timelineSlider;
    private final ComboBox<String> scenarioStepCombo = new ComboBox<>();

    private final Label lblSpeed;
    private Label lblTick;
    private Label lblTime;
    private final Label lblDateTime;
    private Label lblStepDt;

    // Preset Managers
    private final SpeciesPresetManager speciesPresetManager = new SpeciesPresetManager();
    private final WorldPresetManager worldPresetManager = new WorldPresetManager();
    private final WeatherPresetManager weatherPresetManager = new WeatherPresetManager();
    private final NestPresetManager nestPresetManager = new NestPresetManager();
    private final ScenarioPresetManager scenarioPresetManager = new ScenarioPresetManager();

    // 1. World & 2. Weather Combos
    private final ComboBox<String> comboMeta = new ComboBox<>();
    private final ComboBox<String> comboWorld = new ComboBox<>();
    private final ComboBox<String> comboWeather = new ComboBox<>();
    private final TextField txtSeed = new TextField("12345");
    private final TextArea areaDescription = new TextArea();

    // Start Date & Time Controls
    private DatePicker startDatePicker;
    private Spinner<Integer> startTimeHourSpinner;
    private Spinner<Integer> startTimeMinuteSpinner;
    private Spinner<Integer> startTimeSecondSpinner;

    // Termination Limits & Duration Controls
    private final Spinner<Double> maxDurationSpinner = new Spinner<>(1.0, 2_000_000_000.0, 100.0, 1.0);
    private final ComboBox<String> durationUnitCombo = new ComboBox<>();
    private final Label lblDurationCalculatedInfo = new Label();
    private final Spinner<Integer> minPopStopSpinner = new Spinner<>(0, 1000, 0, 5);

    // 3. Multi-Species Scenario Config List
    private final ComboBox<String> comboAvailableSpecies = new ComboBox<>();
    private final VBox speciesListContainer = new VBox(10);
    private final List<SpeciesConfigCard> speciesCardList = new ArrayList<>();
    private volatile ScenarioSetupSnapshot lastSetupSnapshot;

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public record AccessoryConfigSnapshot(
        String name,
        String role,
        boolean enabled,
        int initialCount,
        String renewalStrategy
    ) {}

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public record SpeciesConfigSnapshot(
        String speciesName,
        String nestType,
        int queenCount,
        int workerCount,
        int soldierCount,
        int broodCount,
        int initialFood,
        ArchitectureType workerEngine,
        ArchitectureType soldierEngine,
        ArchitectureType queenEngine,
        List<AccessoryConfigSnapshot> accessorySnapshots
    ) {}

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public record ScenarioSetupSnapshot(
        long seed,
        String startDateTimeIso,
        float simulationStepSeconds,
        double maxDurationValue,
        String maxDurationUnit,
        int minPopStopThreshold,
        String description,
        String selectedWorld,
        String selectedWeather,
        String selectedSpecies,
        String selectedNestType,
        int queenCount,
        int workerCount,
        int soldierCount,
        int broodCount,
        List<SpeciesConfigSnapshot> speciesSnapshots
    ) {
        public ScenarioSetupSnapshot(
            long seed,
            String selectedWorld,
            String selectedWeather,
            String selectedSpecies,
            String selectedNestType,
            int queenCount,
            int workerCount,
            int soldierCount,
            int broodCount,
            List<SpeciesConfigSnapshot> speciesSnapshots
        ) {
            this(seed, null, 0.0166f, 30.0, "Jours (j)", 0, "", selectedWorld, selectedWeather, selectedSpecies, selectedNestType, queenCount, workerCount, soldierCount, broodCount, speciesSnapshots);
        }
    }

    public boolean isCreatingScenario() {
        return isCreatingScenario;
    }

    public ScenarioSetupSnapshot captureSetupSnapshotOnFXThread() {
        List<SpeciesConfigSnapshot> snapshots = new ArrayList<>();
        for (SpeciesConfigCard card : speciesCardList) {
            snapshots.add(card.getSnapshot());
        }
        String dtIso = startDateTime != null ? startDateTime.toString() : LocalDateTime.now().toString();
        double maxVal = maxDurationSpinner != null && maxDurationSpinner.getValue() != null ? maxDurationSpinner.getValue() : 100.0;
        String maxUnit = durationUnitCombo != null && durationUnitCombo.getValue() != null ? durationUnitCombo.getValue() : "Jours (j)";
        int minPop = minPopStopSpinner != null && minPopStopSpinner.getValue() != null ? minPopStopSpinner.getValue() : 0;
        String desc = areaDescription != null ? areaDescription.getText() : "";

        return new ScenarioSetupSnapshot(
            getMasterSeed(),
            dtIso,
            getSimulationStepSeconds(),
            maxVal,
            maxUnit,
            minPop,
            desc,
            getSelectedWorld(),
            getSelectedWeather(),
            getSelectedSpecies(),
            getSelectedNestType(),
            getQueenCount(),
            getWorkerCount(),
            getSoldierCount(),
            getBroodCount(),
            snapshots
        );
    }

    public ScenarioSetupSnapshot getLastSetupSnapshot() {
        return lastSetupSnapshot;
    }

    // Scenario Validation & Warning Panel
    private VBox validationPanel;
    private VBox warningsContainer;

    private boolean isPlaying = false;
    private boolean isPaused = false;
    private boolean isStopped = true;
    private float currentSpeed = 1.0f;
    private long currentTick = 0;
    private long maxTick = 0;
    private long highestRecordedTick = 0;
    private float simulationStepSeconds = 0.0166f;
    private LocalDateTime startDateTime = LocalDateTime.of(2026, 3, 20, 8, 0, 0);
    private LocalDateTime currentDateTime = startDateTime;

    // Callbacks
    private Consumer<Void> onPlay;
    private Consumer<Void> onPause;
    private Consumer<Void> onStop;
    private Consumer<Float> onSpeedChange;
    private Consumer<Long> onSeek;
    private Consumer<Integer> onRewind;
    private Consumer<Void> onStepForward;
    private Consumer<Float> onStepChange;
    private Consumer<String> onCreateCheckpoint;
    private Consumer<org.swarmforge.core.simulation.SimulationCheckpoint> onRestoreCheckpoint;
    private Consumer<Long> onApplyPresets;

    private final ComboBox<org.swarmforge.core.simulation.SimulationCheckpoint> comboCheckpoints = new ComboBox<>();
    private final VBox playbackAndSpeedPanel = new VBox(8);

    private Button btnApplyPresets;
    private boolean isCreatingScenario = false;
    private javafx.animation.Timeline activeCreationTimeline = null;
    private java.util.concurrent.CompletableFuture<?> activeCreationFuture = null;

    private ProgressBar inlineProgressBar;
    private Label inlineProgressLabel;
    private HBox applyProgressBox;

    public SimulationControlPanel() {
        setSpacing(10);
        setMaxWidth(820);
        setPrefWidth(820);

        org.swarmforge.client.util.I18nManager i18n = I18nManager.getInstance();

        // 1. Top Standardized Header Bar
        VBox headerVBox = new VBox(6);
        headerVBox.setPadding(new Insets(8, 10, 5, 10));

        HBox headerRow = new HBox(8);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label lblPresetHeader = new Label();
        lblPresetHeader.textProperty().bind(i18n.createStringBinding("sim.preset_header"));
        lblPresetHeader.getStyleClass().add("header-title-large");
        lblPresetHeader.setTooltip(new Tooltip());
        lblPresetHeader.getTooltip().textProperty().bind(i18n.createStringBinding("sim.preset_header.tt"));

        headerRow.getChildren().add(lblPresetHeader);
        headerVBox.getChildren().addAll(headerRow, new Separator());

        // Top Meta-Scenario Header Card
        VBox scenarioCard = new VBox(10);
        scenarioCard.getStyleClass().add("card-pane");

        Button bSaveScenario = new Button();
        bSaveScenario.textProperty().bind(i18n.createStringBinding("common.btn.save"));
        bSaveScenario.setGraphic(new FontIcon(Feather.SAVE));
        bSaveScenario.getStyleClass().add("btn-secondary");
        bSaveScenario.setTooltip(new Tooltip("Save current scenario configuration"));
        bSaveScenario.setOnAction(e -> handleSaveScenario());

        Button bDeleteScenario = new Button(I18nManager.getInstance().get("common.btn.delete"));
        bDeleteScenario.setGraphic(new FontIcon(Feather.TRASH_2));
        bDeleteScenario.getStyleClass().add("btn-danger");
        bDeleteScenario.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold;");
        bDeleteScenario.setTooltip(new Tooltip("Delete selected scenario"));
        bDeleteScenario.setOnAction(e -> handleDeleteScenario());

        Button bExportScenario = new Button(I18nManager.getInstance().get("common.btn.export"));
        bExportScenario.setGraphic(new FontIcon(Feather.DOWNLOAD));
        bExportScenario.getStyleClass().add("btn-secondary");
        bExportScenario.setTooltip(new Tooltip("Export scenario configuration to JSON"));
        bExportScenario.setOnAction(e -> handleExportScenario());

        Button bImportScenario = new Button(I18nManager.getInstance().get("common.btn.import"));
        bImportScenario.setGraphic(new FontIcon(Feather.UPLOAD));
        bImportScenario.getStyleClass().add("btn-secondary");
        bImportScenario.setTooltip(new Tooltip("Import a scenario JSON file."));
        bImportScenario.setOnAction(e -> handleImportScenario());

        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        Label lblMeta = new Label("★ Global Scenario Preset :");
        lblMeta.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        lblMeta.getStyleClass().add("purple-accent-title");

        Set<String> scenarioSet = new TreeSet<>(scenarioPresetManager.getPresetNames());
        for (org.swarmforge.core.scenario.Scenario sc : org.swarmforge.core.scenario.AcademicScenarios.getAllAcademicScenarios(12345L)) {
            scenarioSet.add(sc.getTitle());
        }
        scenarioSet.add(I18nManager.getInstance().get("sim.preset.title.amazon"));
        scenarioSet.add(I18nManager.getInstance().get("sim.preset.title.granivore"));
        scenarioSet.add(I18nManager.getInstance().get("sim.preset.title.war"));
        scenarioSet.add(I18nManager.getInstance().get("sim.preset.title.beehive"));
        scenarioSet.add(I18nManager.getInstance().get("sim.preset.title.termites"));
        scenarioSet.add(I18nManager.getInstance().get("sim.preset.title.taiga"));
        scenarioSet.add(I18nManager.getInstance().get("sim.preset.title.claustral"));
        scenarioSet.add(I18nManager.getInstance().get("sim.preset.title.supercolony"));

        comboMeta.getItems().setAll(scenarioSet);
        comboMeta.setEditable(true);
        comboMeta.setMaxWidth(Double.MAX_VALUE);
        comboMeta.setTooltip(new Tooltip("Select or create a global scenario preset combining biotope, climate, and fauna."));
        HBox.setHgrow(comboMeta, Priority.ALWAYS);
        comboMeta.setOnAction(e -> applyMetaPreset(comboMeta.getValue()));
        if (!comboMeta.getItems().isEmpty()) {
            comboMeta.getSelectionModel().selectFirst();
            applyMetaPreset(comboMeta.getValue());
        }

        metaRow.getChildren().addAll(lblMeta, comboMeta, bSaveScenario, bDeleteScenario, bExportScenario, bImportScenario);

        VBox descBox = new VBox(4);
        Label lblDesc = new Label("📝 Scientific Scenario Description :");
        lblDesc.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
        lblDesc.getStyleClass().add("sub-title-gray");
        areaDescription.setPrefRowCount(2);
        areaDescription.setPromptText("Scenario description and experimental objectives...");
        areaDescription.setWrapText(true);
        areaDescription.setStyle("-fx-font-size: 11px;");
        descBox.getChildren().addAll(lblDesc, areaDescription);

        // Populate World and Weather combos in alphabetical order
        java.util.List<String> sortedWorldNames = new java.util.ArrayList<>(worldPresetManager.names());
        java.util.Collections.sort(sortedWorldNames);
        comboWorld.getItems().setAll(sortedWorldNames);
        if (!comboWorld.getItems().isEmpty()) comboWorld.getSelectionModel().selectFirst();

        java.util.List<String> sortedWeatherNames = new java.util.ArrayList<>(weatherPresetManager.names());
        java.util.Collections.sort(sortedWeatherNames);
        comboWeather.getItems().setAll(sortedWeatherNames);
        if (!comboWeather.getItems().isEmpty()) comboWeather.getSelectionModel().selectFirst();

        // Section 1: Biotope & Weather Presets
        GridPane gridWorldWeather = new GridPane();
        gridWorldWeather.setHgap(10); gridWorldWeather.setVgap(8);

        Label lbl1World = new Label("1. 🌍 World Preset (Biotope) :");
        lbl1World.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        lbl1World.getStyleClass().add("accent-title");
        lbl1World.setTooltip(new Tooltip("3D biotope and terrain relief type loaded from WorldPresetManager."));

        Label lbl2Weather = new Label("2. 🌤️ Weather & Climate Preset :");
        lbl2Weather.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        lbl2Weather.getStyleClass().add("accent-title");
        lbl2Weather.setTooltip(new Tooltip("Climate profile and seasonal weather from WeatherPresetManager."));

        Button btnAlignWeather = new Button("🔄 Align");
        btnAlignWeather.setStyle("-fx-background-color: #334155; -fx-text-fill: #e2e8f0; -fx-font-size: 10px;");
        btnAlignWeather.setTooltip(new Tooltip("Align climate with the selected biotope"));
        btnAlignWeather.setOnAction(e -> alignWeatherWithWorld(true));
        HBox weatherRow = new HBox(6, comboWeather, btnAlignWeather);

        gridWorldWeather.add(lbl1World, 0, 0); gridWorldWeather.add(comboWorld, 1, 0);
        gridWorldWeather.add(lbl2Weather, 0, 1); gridWorldWeather.add(weatherRow, 1, 1);

        // Section 2: Start Date, Time & Master Seed
        GridPane gridDateTimeSeed = new GridPane();
        gridDateTimeSeed.setHgap(10); gridDateTimeSeed.setVgap(8);

        Label lbl3DateTime = new Label("3. 📅 Start Date & Time :");
        lbl3DateTime.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        lbl3DateTime.getStyleClass().add("accent-title");
        startDatePicker = new DatePicker(startDateTime.toLocalDate());
        startDatePicker.setPrefWidth(125);
        startDatePicker.setStyle("-fx-font-size: 11px;");

        startTimeHourSpinner = new Spinner<>(0, 23, startDateTime.getHour());
        startTimeHourSpinner.setPrefWidth(54);
        startTimeHourSpinner.setEditable(true);
        startTimeHourSpinner.setStyle("-fx-font-size: 10px;");

        startTimeMinuteSpinner = new Spinner<>(0, 59, startDateTime.getMinute());
        startTimeMinuteSpinner.setPrefWidth(54);
        startTimeMinuteSpinner.setEditable(true);
        startTimeMinuteSpinner.setStyle("-fx-font-size: 10px;");

        startTimeSecondSpinner = new Spinner<>(0, 59, startDateTime.getSecond());
        startTimeSecondSpinner.setPrefWidth(54);
        startTimeSecondSpinner.setEditable(true);
        startTimeSecondSpinner.setStyle("-fx-font-size: 10px;");

        Runnable syncStartDateTimeFromUI = () -> {
            if (startDatePicker.getValue() != null) {
                int h = startTimeHourSpinner.getValue() != null ? Math.max(0, Math.min(23, startTimeHourSpinner.getValue())) : 0;
                int m = startTimeMinuteSpinner.getValue() != null ? Math.max(0, Math.min(59, startTimeMinuteSpinner.getValue())) : 0;
                int s = startTimeSecondSpinner.getValue() != null ? Math.max(0, Math.min(59, startTimeSecondSpinner.getValue())) : 0;
                startDateTime = LocalDateTime.of(startDatePicker.getValue(), LocalTime.of(h, m, s));
                updateTick(currentTick, maxTick);
            }
        };

        startDatePicker.setOnAction(e -> syncStartDateTimeFromUI.run());
        startTimeHourSpinner.valueProperty().addListener((o, oldV, newV) -> syncStartDateTimeFromUI.run());
        startTimeMinuteSpinner.valueProperty().addListener((o, oldV, newV) -> syncStartDateTimeFromUI.run());
        startTimeSecondSpinner.valueProperty().addListener((o, oldV, newV) -> syncStartDateTimeFromUI.run());

        Label lblH = new Label("h");
        lblH.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
        lblH.getStyleClass().add("sub-title-gray");
        Label lblM = new Label("m");
        lblM.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
        lblM.getStyleClass().add("sub-title-gray");
        Label lblS = new Label("s");
        lblS.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
        lblS.getStyleClass().add("sub-title-gray");

        HBox startDateTimeBox = new HBox(4,
            startDatePicker,
            startTimeHourSpinner, lblH,
            startTimeMinuteSpinner, lblM,
            startTimeSecondSpinner, lblS
        );
        startDateTimeBox.setAlignment(Pos.CENTER_LEFT);

        Label lblSeedTitle = new Label("4. 🎲 Master Random Seed :");
        lblSeedTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        lblSeedTitle.getStyleClass().add("accent-title");
        lblSeedTitle.setTooltip(new Tooltip("Master random seed controlling exact scenario determinism and reproducibility."));

        txtSeed.setPrefWidth(110);
        txtSeed.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        txtSeed.setTooltip(new Tooltip("Numerical value of the random seed."));

        Button btnRandSeed = new Button("🎲 New");
        btnRandSeed.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-font-size: 10px;");
        btnRandSeed.setTooltip(new Tooltip("Generate a new random seed."));
        btnRandSeed.setOnAction(e -> {
            txtSeed.setText(String.valueOf((long)(Math.random() * 900000 + 100000)));
            updateValidationPanel();
        });

        HBox seedBox = new HBox(6, txtSeed, btnRandSeed);
        seedBox.setAlignment(Pos.CENTER_LEFT);

        gridDateTimeSeed.add(lbl3DateTime, 0, 0); gridDateTimeSeed.add(startDateTimeBox, 1, 0);
        gridDateTimeSeed.add(lblSeedTitle, 0, 1); gridDateTimeSeed.add(seedBox, 1, 1);

        // Section 3: Physics Step (dt)
        GridPane gridPhysics = new GridPane();
        gridPhysics.setHgap(10); gridPhysics.setVgap(8);

        Label lbl4Step = new Label("5. ⏱️ Physics Step (Integration Δt) :");
        lbl4Step.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        lbl4Step.getStyleClass().add("accent-title");
        lbl4Step.setTooltip(new Tooltip("Defines the Δt numerical integration fidelity for physics and ethology equations."));

        scenarioStepCombo.getItems().setAll(
            "16.6 ms (60 Hz - Max Physics Fidelity / Default)",
            "50 ms (20 Hz - Standard Precision)",
            "100 ms (10 Hz - Fast Mode)",
            "1.0 s (Macroscopic Ecosystem Mode)",
            "5.0 s (Ultra Macroscopic Mode)"
        );
        scenarioStepCombo.getSelectionModel().selectFirst();
        scenarioStepCombo.setPrefWidth(160);
        scenarioStepCombo.setStyle("-fx-font-size: 11px;");
        scenarioStepCombo.setTooltip(new Tooltip("Fixed Δt calculation step persisted in scenario (guaranteeing perfect seed-based determinism)."));
        scenarioStepCombo.setOnAction(e -> {
            int idx = scenarioStepCombo.getSelectionModel().getSelectedIndex();
            switch (idx) {
                case 0 -> simulationStepSeconds = 0.0166f;
                case 1 -> simulationStepSeconds = 0.05f;
                case 2 -> simulationStepSeconds = 0.1f;
                case 3 -> simulationStepSeconds = 1.0f;
                case 4 -> simulationStepSeconds = 5.0f;
            }
            updateStepDtLabel();
            updateValidationPanel();
            if (onStepChange != null) onStepChange.accept(simulationStepSeconds);
        });

        Label lblDeterminismLegend = new Label("💡 Note: For a given Seed and Δt step, simulation execution is fully deterministic and reproducible.");
        lblDeterminismLegend.setStyle("-fx-font-size: 9.5px; -fx-font-style: italic;");
        lblDeterminismLegend.getStyleClass().add("sub-title-gray");

        VBox physicsBox = new VBox(4, scenarioStepCombo, lblDeterminismLegend);
        gridPhysics.add(lbl4Step, 0, 0); gridPhysics.add(physicsBox, 1, 0);

        // Section 4: Termination Limits & Duration Controls
        GridPane gridLimits = new GridPane();
        gridLimits.setHgap(10); gridLimits.setVgap(8);

        Label lblMaxTicks = new Label("6. ⌛ Maximum Simulation Duration :");
        lblMaxTicks.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        lblMaxTicks.getStyleClass().add("accent-title");
        lblMaxTicks.setTooltip(new Tooltip("Maximum simulation duration. Select value and unit (Days by default, s, min, h, months, years, ticks, or unlimited)."));

        maxDurationSpinner.setPrefWidth(90);
        maxDurationSpinner.setEditable(true);
        maxDurationSpinner.setTooltip(new Tooltip("Maximum execution duration value in selected unit."));
        maxDurationSpinner.valueProperty().addListener((o, oldV, newV) -> {
            updateDurationCalculatedLabel();
            updateValidationPanel();
        });

        durationUnitCombo.getItems().setAll(
            "Days (d)",
            "Hours (h)",
            "Minutes (min)",
            "Seconds (s)",
            "Months (30d)",
            "Years (365d)",
            "Ticks",
            "∞ Unlimited"
        );
        durationUnitCombo.getSelectionModel().select("Days (d)");
        durationUnitCombo.setPrefWidth(140);
        durationUnitCombo.setStyle("-fx-font-size: 10px;");
        durationUnitCombo.setTooltip(new Tooltip(I18nManager.getInstance().get("sim.duration_unit.tt")));
        durationUnitCombo.valueProperty().addListener((o, oldV, newV) -> {
            boolean isUnlimited = "∞ Unlimited".equals(newV) || "∞ Illimité".equals(newV);
            maxDurationSpinner.setDisable(isUnlimited);
            updateDurationCalculatedLabel();
            updateValidationPanel();
        });

        lblDurationCalculatedInfo.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold;");
        lblDurationCalculatedInfo.getStyleClass().add("accent-title");
        updateDurationCalculatedLabel();

        HBox durationBox = new HBox(6, maxDurationSpinner, durationUnitCombo);
        durationBox.setAlignment(Pos.CENTER_LEFT);
        VBox durationFullBox = new VBox(4, durationBox, lblDurationCalculatedInfo);

        Label lblMinPop = new Label("7. 🛑 Min Population Stop Threshold :");
        lblMinPop.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        lblMinPop.getStyleClass().add("accent-title");
        lblMinPop.setTooltip(new Tooltip("Minimum total living population threshold. If living count drops below this, simulation stops automatically."));

        minPopStopSpinner.setPrefWidth(90);
        minPopStopSpinner.setEditable(true);
        minPopStopSpinner.setTooltip(new Tooltip("Minimum living population triggering emergency stop (0 = disabled)."));
        minPopStopSpinner.valueProperty().addListener((o, oldV, newV) -> updateValidationPanel());

        gridLimits.add(lblMaxTicks, 0, 0); gridLimits.add(durationFullBox, 1, 0);
        gridLimits.add(lblMinPop, 0, 1); gridLimits.add(minPopStopSpinner, 1, 1);

        // Section 5: Multi-Species Scenario Section Header & Controls
        VBox section3Container = new VBox(8);
        section3Container.getStyleClass().add("card-pane");

        HBox speciesAddBar = new HBox(8);
        speciesAddBar.setAlignment(Pos.CENTER_LEFT);

        Label lbl5SpeciesHeader = new Label("8. 🐜 Species & Ecosystem Scenario :");
        lbl5SpeciesHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        lbl5SpeciesHeader.getStyleClass().add("accent-title");

        java.util.List<String> sortedSpeciesNames = new java.util.ArrayList<>(speciesPresetManager.getPresetNames());
        java.util.Collections.sort(sortedSpeciesNames);
        comboAvailableSpecies.getItems().setAll(sortedSpeciesNames);
        if (!comboAvailableSpecies.getItems().isEmpty()) {
            comboAvailableSpecies.getSelectionModel().selectFirst();
        }
        comboAvailableSpecies.setPrefWidth(160);

        Button btnAddSpeciesToScenario = new Button();
        btnAddSpeciesToScenario.textProperty().bind(i18n.createStringBinding("sim.btn.add_species"));
        btnAddSpeciesToScenario.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        btnAddSpeciesToScenario.setTooltip(new Tooltip());
        btnAddSpeciesToScenario.getTooltip().textProperty().bind(i18n.createStringBinding("sim.btn.add_species.tt"));
        btnAddSpeciesToScenario.setOnAction(e -> {
            String selected = comboAvailableSpecies.getValue();
            if (selected != null && !selected.isEmpty()) {
                addSpeciesCard(selected);
            }
        });

        Button btnAutoPreset = new Button("✨ Recommended Preset");
        btnAutoPreset.setStyle("-fx-background-color: #334155; -fx-text-fill: #e2e8f0; -fx-font-size: 10px;");
        btnAutoPreset.setTooltip(new Tooltip());
        btnAutoPreset.getTooltip().textProperty().bind(i18n.createStringBinding("sim.btn.rec_preset.tt"));
        btnAutoPreset.setOnAction(e -> applyMetaPreset(comboMeta.getValue()));

        speciesAddBar.getChildren().addAll(lbl5SpeciesHeader, comboAvailableSpecies, btnAddSpeciesToScenario, btnAutoPreset);

        // Add initial default species card (Black Garden Ant)
        addSpeciesCard("Black Garden Ant (Lasius niger)");

        section3Container.getChildren().addAll(speciesAddBar, speciesListContainer);

        // Listener on Meta-Preset selection
        comboMeta.setOnAction(e -> {
            applyMetaPreset(comboMeta.getValue());
            updateValidationPanel();
        });

        // Interrupt running simulation if presets change
        Runnable interruptIfRunning = () -> {
            if (isPlaying) {
                isPlaying = false;
                updateButtonStates();
                if (onPause != null) onPause.accept(null);
            }
            updateValidationPanel();
        };
        comboWorld.setOnAction(e -> interruptIfRunning.run());
        comboWeather.setOnAction(e -> interruptIfRunning.run());
        txtSeed.textProperty().addListener((o, oldV, newV) -> updateValidationPanel());

        // Scenario Validation & Warnings Panel
        validationPanel = buildValidationPanel();

        // Checkpoints & Divine Mode
        VBox checkpointsPane = buildCheckpointsPane();

        // Apply & Start Button with Inline Progress Bar
        btnApplyPresets = new Button("⚡ APPLY AND CREATE SIMULATION");
        btnApplyPresets.setMaxWidth(Double.MAX_VALUE);
        btnApplyPresets.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 10 16; -fx-background-radius: 5;");
        btnApplyPresets.setTooltip(new Tooltip());
        btnApplyPresets.getTooltip().textProperty().bind(i18n.createStringBinding("sim.btn.apply_presets.tt"));

        inlineProgressBar = new ProgressBar(0);
        inlineProgressBar.setMaxWidth(Double.MAX_VALUE);
        inlineProgressBar.setPrefHeight(6);
        inlineProgressBar.setStyle("-fx-accent: #0284c7;");

        inlineProgressLabel = new Label("Initializing scenario...");
        inlineProgressLabel.setStyle("-fx-font-size: 10px;");
        inlineProgressLabel.getStyleClass().add("accent-title");

        applyProgressBox = new HBox(8, inlineProgressBar, inlineProgressLabel);
        applyProgressBox.setAlignment(Pos.CENTER_LEFT);
        applyProgressBox.setVisible(false);
        applyProgressBox.setManaged(false);

        btnApplyPresets.setOnAction(e -> handleApplyScenario());

        scenarioCard.getChildren().addAll(
            metaRow,
            descBox,
            new Separator(),
            gridWorldWeather,
            new Separator(),
            gridDateTimeSeed,
            new Separator(),
            gridPhysics,
            new Separator(),
            gridLimits,
            new Separator(),
            section3Container,
            validationPanel,
            checkpointsPane,
            btnApplyPresets,
            applyProgressBox
        );

        updateValidationPanel();

        // Line 1: Date & Heure Row
        HBox dateTimeRow = new HBox(8);
        dateTimeRow.setAlignment(Pos.CENTER_LEFT);

        lblDateTime = new Label("📅 Date & Heure : 2026-03-20 08:00:00 (Jour 1)");
        lblDateTime.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        lblDateTime.getStyleClass().add("accent-title");
        lblDateTime.setTooltip(new Tooltip("Timestamp and simulated nycthemeral cycle (date, time, and day)."));

        dateTimeRow.getChildren().add(lblDateTime);

        // Line 2: Playback Controls
        HBox playbackRow1 = new HBox(4);
        playbackRow1.setAlignment(Pos.CENTER);

        btnGoToBeginning = createIconButton(Feather.SKIP_BACK, "Beginning of simulation (Return to step #0)");
        btnRewind = createIconButton(Feather.REWIND, "Rewind (-100 steps)");
        btnStepBack = createIconButton(Feather.CHEVRON_LEFT, "Step backward (-1 step)");
        btnPlay = createIconButton(Feather.PLAY, "Start / Resume simulation");
        btnPause = createIconButton(Feather.PAUSE, "Pause simulation");
        btnStepForward = createIconButton(Feather.CHEVRON_RIGHT, "Step forward (+1 step)");
        btnFastForward = createIconButton(Feather.FAST_FORWARD, "Fast forward (+100 steps)");
        btnGoToEnd = createIconButton(Feather.SKIP_FORWARD, "Jump to end of simulation (Last recorded step)");

        btnGoToBeginning.setOnAction(e -> {
            isPlaying = false;
            isPaused = true;
            isStopped = false;
            currentTick = 0;
            updateTick(0, highestRecordedTick);
            updateButtonStates();
            if (onPause != null) onPause.accept(null);
            if (onSeek != null) onSeek.accept(0L);
        });

        btnRewind.setOnAction(e -> {
            isPlaying = false;
            isPaused = true;
            isStopped = false;
            currentTick = Math.max(0, currentTick - 100);
            updateTick(currentTick, highestRecordedTick);
            updateButtonStates();
            if (onPause != null) onPause.accept(null);
            if (onRewind != null) onRewind.accept(100);
        });

        btnStepBack.setOnAction(e -> {
            isPlaying = false;
            isPaused = true;
            isStopped = false;
            currentTick = Math.max(0, currentTick - 1);
            updateTick(currentTick, highestRecordedTick);
            updateButtonStates();
            if (onPause != null) onPause.accept(null);
            if (onRewind != null) onRewind.accept(1);
        });

        btnPlay.setOnAction(e -> {
            isPlaying = true;
            isPaused = false;
            isStopped = false;
            updateButtonStates();
            if (onPlay != null) onPlay.accept(null);
        });

        btnPause.setOnAction(e -> {
            isPlaying = false;
            isPaused = true;
            isStopped = false;
            updateButtonStates();
            if (onPause != null) onPause.accept(null);
        });

        btnStepForward.setOnAction(e -> {
            if (!isPlaying) {
                isPaused = true;
                isStopped = false;
                updateButtonStates();
                if (onPause != null) onPause.accept(null);
                if (onStepForward != null) onStepForward.accept(null);
            }
        });

        btnFastForward.setOnAction(e -> {
            if (!isPlaying) {
                isPaused = true;
                isStopped = false;
                long target = Math.min(highestRecordedTick, currentTick + 100);
                updateTick(target, highestRecordedTick);
                updateButtonStates();
                if (onPause != null) onPause.accept(null);
                if (onSeek != null) onSeek.accept(target);
            }
        });

        btnGoToEnd.setOnAction(e -> {
            isPlaying = false;
            isPaused = true;
            isStopped = false;
            currentTick = highestRecordedTick;
            updateTick(highestRecordedTick, highestRecordedTick);
            updateButtonStates();
            if (onPause != null) onPause.accept(null);
            if (onSeek != null) onSeek.accept(highestRecordedTick);
        });

        playbackRow1.getChildren().addAll(btnGoToBeginning, btnRewind, btnStepBack, btnPlay, btnPause, btnStepForward, btnFastForward, btnGoToEnd);

        // Line 3: Speed Slider & Readout Label
        HBox speedSliderRow = new HBox(8);
        speedSliderRow.setAlignment(Pos.CENTER);

        Label lblSpeedLabel = new Label("🚀 Speed :");
        lblSpeedLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
        lblSpeedLabel.getStyleClass().add("sub-title-gray");

        lblSpeed = new Label("1.0x");
        lblSpeed.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        lblSpeed.getStyleClass().add("accent-title");
        lblSpeed.setPrefWidth(55);
        lblSpeed.setPrefWidth(55);

        speedSlider = new Slider(0.1, 20.0, 1.0);
        speedSlider.setShowTickLabels(false);
        speedSlider.setShowTickMarks(false);
        speedSlider.setPrefWidth(120);
        speedSlider.setTooltip(new Tooltip("Adjust temporal acceleration factor (0.1x to 20x)."));

        Button btnMaxSpeed = new Button("🚀 MAX");
        btnMaxSpeed.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 4 10 4 10;");
        btnMaxSpeed.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        btnMaxSpeed.setTooltip(new Tooltip("Enable maximum execution speed (Maximum CPU throughput)"));

        final boolean[] isProgrammaticSpeedChange = { false };

        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (isProgrammaticSpeedChange[0]) return;
            if ("MAX".equals(btnMaxSpeed.getUserData())) {
                btnMaxSpeed.setUserData(null);
                btnMaxSpeed.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 4 10 4 10;");
            }
            currentSpeed = newVal.floatValue();
            lblSpeed.setText(String.format("%.1fx", currentSpeed));
            if (onSpeedChange != null) onSpeedChange.accept(currentSpeed);
        });

        btnMaxSpeed.setOnAction(e -> {
            if ("MAX".equals(btnMaxSpeed.getUserData())) {
                btnMaxSpeed.setUserData(null);
                btnMaxSpeed.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 4 10 4 10;");
                currentSpeed = (float) speedSlider.getValue();
                lblSpeed.setText(String.format("%.1fx", currentSpeed));
            } else {
                btnMaxSpeed.setUserData("MAX");
                btnMaxSpeed.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: #fef08a; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 4 10 4 10; -fx-effect: innershadow(three-pass-box, #d946ef, 5, 0, 0, 0);");
                isProgrammaticSpeedChange[0] = true;
                try {
                    speedSlider.setValue(speedSlider.getMax());
                } finally {
                    isProgrammaticSpeedChange[0] = false;
                }
                currentSpeed = 1000.0f;
                lblSpeed.setText("MAX 🚀");
            }
            if (onSpeedChange != null) onSpeedChange.accept(currentSpeed);
        });

        speedSliderRow.getChildren().addAll(lblSpeedLabel, speedSlider, lblSpeed, btnMaxSpeed);

        playbackAndSpeedPanel.setPadding(new Insets(8));
        playbackAndSpeedPanel.getStyleClass().add("card-pane");

        Label lblPlaybackHeader = new Label("⏱️ Time, Speed & Playback Controls");
        lblPlaybackHeader.setStyle("-fx-text-fill: #a78bfa; -fx-font-weight: bold; -fx-font-size: 11px;");

        playbackAndSpeedPanel.getChildren().addAll(lblPlaybackHeader, dateTimeRow, playbackRow1, speedSliderRow);

        getChildren().addAll(headerVBox, scenarioCard);
        updateButtonStates();
    }

    private void addSpeciesCard(String speciesName) {
        // Prevent duplicate cards for the exact same species instance unless user wants
        SpeciesConfigCard card = new SpeciesConfigCard(speciesName, () -> {
            speciesCardList.removeIf(c -> c.getSpeciesName().equals(speciesName));
            refreshSpeciesListContainer();
            updateValidationPanel();
        });
        card.setOnChange(this::updateValidationPanel);
        speciesCardList.add(card);
        refreshSpeciesListContainer();
        updateValidationPanel();
    }

    private void refreshSpeciesListContainer() {
        speciesListContainer.getChildren().clear();
        for (SpeciesConfigCard card : speciesCardList) {
            speciesListContainer.getChildren().add(card.getCardPane());
        }
    }

    private void cancelScenarioCreation() {
        isCreatingScenario = false;
        if (activeCreationTimeline != null) {
            activeCreationTimeline.stop();
            activeCreationTimeline = null;
        }
        if (activeCreationFuture != null) {
            activeCreationFuture.cancel(true);
            activeCreationFuture = null;
        }
        if (btnApplyPresets != null) {
            btnApplyPresets.setText("⚡ APPLY AND CREATE SIMULATION");
            btnApplyPresets.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 10 16; -fx-background-radius: 5;");
            btnApplyPresets.setTooltip(new Tooltip("Reset simulation applying multi-species scenario, filtered nests, ecosystem, and random seed."));
        }
        if (inlineProgressBar != null) {
            inlineProgressBar.setProgress(0);
            inlineProgressLabel.setText("❌ Scenario creation cancelled by user.");
        }
        System.out.println("[INFO] [SwarmForge Engine] Instanciation et calculs du scénario annulés par l'utilisateur. Mémoire libérée.");
        System.gc();

        javafx.animation.PauseTransition hideDelay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(2000));
        hideDelay.setOnFinished(ev -> {
            if (applyProgressBox != null) {
                applyProgressBox.setVisible(false);
                applyProgressBox.setManaged(false);
            }
        });
        hideDelay.play();
    }

    private void handleApplyScenario() {
        if (isCreatingScenario) {
            cancelScenarioCreation();
            return;
        }

        if (isPlaying || currentTick > 0) {
            Alert confirmAlert = org.swarmforge.client.util.ThemeManager.createAlert(
                Alert.AlertType.CONFIRMATION,
                "A simulation is currently running (Tick: " + currentTick + ").\n\n" +
                "Are you sure you want to reset and replace the active simulation with this new scenario configuration?"
            );
            confirmAlert.setTitle("Reset Simulation");
            confirmAlert.setHeaderText("Confirmation Required");
            java.util.Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        }

        // Note: Pop-up dialog removed per design request. All scenario parameter warnings & auto-fix tools
        // are dynamically evaluated and displayed in the Validation Panel right above Checkpoints.

        isCreatingScenario = true;
        if (btnApplyPresets != null) {
            btnApplyPresets.setText("❌ CANCEL CALCULATION");
            btnApplyPresets.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 10 16; -fx-background-radius: 5; -fx-cursor: hand;");
            btnApplyPresets.setTooltip(new Tooltip("Click to interrupt scenario creation and free memory."));
        }

        if (isPlaying) {
            isPlaying = false;
            updateButtonStates();
            if (onPause != null) onPause.accept(null);
        }
        long seed = getMasterSeed();
        String selectedWorld = getSelectedWorld();
        String selectedWeather = getSelectedWeather();
        String scenarioName = comboMeta.getValue() != null ? comboMeta.getValue() : "New Scenario";
        this.lastSetupSnapshot = captureSetupSnapshotOnFXThread();

        System.out.println("\n================================================================================");
        System.out.println("[INFO] [SwarmForge Engine] === INITIALISATION DU SCÉNARIO & DE L'ÉCOSYSTÈME ===");
        System.out.println("[INFO] [SwarmForge Engine] Scénario : " + scenarioName + " | Seed: " + seed);
        System.out.println("[INFO] [SwarmForge Engine] Step 1/5 [15%]: Initialisation du maillage 3D biotope & grille spatiale ('" + selectedWorld + "')...");

        if (applyProgressBox != null) {
            applyProgressBox.setVisible(true);
            applyProgressBox.setManaged(true);
            inlineProgressBar.setProgress(0.15);
            inlineProgressLabel.setText("Step 1/5 [15%]: Initializing 3D biotope mesh & spatial grid ('" + selectedWorld + "')...");
        }

        activeCreationTimeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(250), e -> {
                if (!isCreatingScenario) return;
                System.out.println("[INFO] [SwarmForge Engine] Step 2/5 [35%]: Calcul du profil microclimatique & météo ('" + selectedWeather + "')...");
                if (inlineProgressBar != null) {
                    inlineProgressBar.setProgress(0.35);
                    inlineProgressLabel.setText("Step 2/5 [35%]: Calculating microclimate profile & weather parameters ('" + selectedWeather + "')...");
                }
            }),
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(550), e -> {
                if (!isCreatingScenario) return;
                System.out.println("[INFO] [SwarmForge Engine] Step 3/5 [60%]: Construction des structures de nids souterrains & arboricoles...");
                if (inlineProgressBar != null) {
                    inlineProgressBar.setProgress(0.60);
                    inlineProgressLabel.setText("Step 3/5 [60%]: Constructing subterranean & arboreal nest structures...");
                }
            }),
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(900), e -> {
                if (!isCreatingScenario) return;
                System.out.println("[INFO] [SwarmForge Engine] Step 4/5 [85%]: Instanciation des colonies multi-espèces (" + speciesCardList.size() + " espèces) & moteurs IA...");
                if (inlineProgressBar != null) {
                    inlineProgressBar.setProgress(0.85);
                    inlineProgressLabel.setText("Step 4/5 [85%]: Instantiating multi-species colonies (" + speciesCardList.size() + " species) & caste AI engines...");
                }
            }),
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(1200), e -> {
                if (!isCreatingScenario) return;
                System.out.println("[INFO] [SwarmForge Engine] Step 5/5 [95%]: Population des espèces accessoires, proies & réseaux trophiques...");
                if (inlineProgressBar != null) {
                    inlineProgressBar.setProgress(0.95);
                    inlineProgressLabel.setText("Step 5/5 [95%]: Populating accessory species, prey & ecosystem food webs...");
                }
            }),
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(1500), e -> {
                if (!isCreatingScenario) return;
                activeCreationFuture = java.util.concurrent.CompletableFuture.runAsync(() -> {
                    if (isCreatingScenario && onApplyPresets != null) {
                        onApplyPresets.accept(seed);
                    }
                });
                activeCreationFuture.whenComplete((res, ex) -> javafx.application.Platform.runLater(() -> {
                    if (!isCreatingScenario) return;
                    isCreatingScenario = false;
                    if (btnApplyPresets != null) {
                        btnApplyPresets.setText("⚡ APPLY AND CREATE SIMULATION");
                        btnApplyPresets.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 10 16; -fx-background-radius: 5;");
                        btnApplyPresets.setTooltip(new Tooltip("Reset simulation applying multi-species scenario, filtered nests, ecosystem, and random seed."));
                    }

                    if (ex != null) {
                        System.err.println("[ERROR] [SwarmForge Engine] Exception during scenario creation: " + ex.getMessage());
                        ex.printStackTrace();
                        if (inlineProgressBar != null) {
                            inlineProgressBar.setProgress(1.0);
                            inlineProgressLabel.setText("⚠️ Initialization completed with warnings: " + ex.getMessage());
                        }
                    } else {
                        org.swarmforge.core.event.EventBus.getInstance().publish(
                            org.swarmforge.core.event.SimulationEvent.obtain(
                                org.swarmforge.core.event.SimulationEvent.EventType.COLONY_FOUNDED,
                                org.swarmforge.core.event.SimulationEvent.Severity.INFO,
                                0,
                                "🌍 Scenario '" + scenarioName + "' initialized (" + speciesCardList.size() + " species configured)",
                                null
                            )
                        );

                        if (inlineProgressBar != null) {
                            inlineProgressBar.setProgress(1.0);
                            inlineProgressLabel.setText("✅ Scenario '" + scenarioName + "' initialized successfully!");
                        }
                        System.out.println("[INFO] [SwarmForge Engine] === INITIALISATION DU SCÉNARIO COMPLÉTÉE AVEC SUCCÈS ===");
                        System.out.println("================================================================================\n");
                    }

                    javafx.animation.PauseTransition hideDelay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(1400));
                    hideDelay.setOnFinished(ev -> {
                        if (applyProgressBox != null) {
                            applyProgressBox.setVisible(false);
                            applyProgressBox.setManaged(false);
                        }
                    });
                    hideDelay.play();
                }));
            })
        );
        activeCreationTimeline.play();
    }

    private void alignWeatherWithWorld() {
        alignWeatherWithWorld(false);
    }

    private void alignWeatherWithWorld(boolean showNotification) {
        String world = comboWorld.getValue();
        if (world == null) return;
        String targetWeather = "Temperate";
        if (world.contains("Tropical") || world.contains("Amazon")) {
            targetWeather = "Tropical";
        } else if (world.contains("Aride") || world.contains("Desert") || world.contains("Savane")) {
            targetWeather = "Arid";
        } else if (world.contains("Alpin") || world.contains("Toundra") || world.contains("Montagne")) {
            targetWeather = "Polar";
        } else {
            targetWeather = "Temperate";
        }
        selectComboIfPresent(comboWeather, targetWeather);
        updateValidationPanel();
        if (showNotification) {
            org.swarmforge.client.util.NotificationOverlay.show(
                this,
                "Climate successfully aligned to '" + targetWeather + "' for biotope '" + world + "'!",
                org.swarmforge.client.util.NotificationOverlay.NotificationType.SUCCESS
            );
        }
    }

    public void updateScenarioCreationProgress(double progressFraction, String statusMessage) {
        javafx.application.Platform.runLater(() -> {
            if (inlineProgressBar != null) {
                inlineProgressBar.setProgress(Math.min(1.0, Math.max(0.0, progressFraction)));
            }
            if (inlineProgressLabel != null) {
                inlineProgressLabel.setText(statusMessage);
            }
        });
    }

    private void applyMetaPreset(String metaName) {
        if (metaName == null) return;
        speciesCardList.clear();

        // 1. Try resolving exact or partial match from ScenarioPresetManager (Academic & Custom Scenarios)
        org.swarmforge.core.scenario.Scenario scenario = scenarioPresetManager.get(metaName);
        if (scenario == null) {
            for (org.swarmforge.core.scenario.Scenario s : scenarioPresetManager.getAll().values()) {
                if (s.getTitle().equalsIgnoreCase(metaName) || s.getId().equalsIgnoreCase(metaName) || metaName.contains(s.getTitle())) {
                    scenario = s;
                    break;
                }
            }
        }

        if (scenario != null) {
            if (areaDescription != null) {
                areaDescription.setText("🔬 ACADEMIC SCENARIO / PROFILE: " + scenario.getTitle() + "\n\n" + scenario.getDescription());
            }
            if (txtSeed != null) {
                txtSeed.setText(String.valueOf(scenario.getMasterSeed()));
            }
            if (maxDurationSpinner != null && scenario.getMaxSimulationTicks() > 0) {
                if (durationUnitCombo != null) durationUnitCombo.getSelectionModel().select("Ticks");
                maxDurationSpinner.getValueFactory().setValue((double) scenario.getMaxSimulationTicks());
            }
            if (minPopStopSpinner != null && scenario.getMinPopulationStopThreshold() >= 0) {
                minPopStopSpinner.getValueFactory().setValue(scenario.getMinPopulationStopThreshold());
            }

            // Load simulation step size dt if present
            if (scenario.getSimulationStepSeconds() > 0) {
                this.simulationStepSeconds = scenario.getSimulationStepSeconds();
                if (Math.abs(simulationStepSeconds - 0.0166f) < 0.005f) {
                    scenarioStepCombo.getSelectionModel().select(0);
                } else if (Math.abs(simulationStepSeconds - 0.05f) < 0.01f) {
                    scenarioStepCombo.getSelectionModel().select(1);
                } else if (Math.abs(simulationStepSeconds - 0.1f) < 0.01f) {
                    scenarioStepCombo.getSelectionModel().select(2);
                } else if (Math.abs(simulationStepSeconds - 1.0f) < 0.1f) {
                    scenarioStepCombo.getSelectionModel().select(3);
                } else if (Math.abs(simulationStepSeconds - 5.0f) < 0.5f) {
                    scenarioStepCombo.getSelectionModel().select(4);
                }
                updateStepDtLabel();
            }

            // Biome to world mapping
            String biome = scenario.getBiomeName();
            if (biome != null) {
                switch (biome.toUpperCase()) {
                    case "ARID_SAVANNA", "DESERT", "ARID" -> selectComboIfPresent(comboWorld, "Arid Savanna (Serengeti, TZ)");
                    case "TROPICAL_RAINFOREST", "TROPICAL" -> selectComboIfPresent(comboWorld, "Tropical Rainforest (Manaus, BR)");
                    case "ALPINE_TUNDRA", "POLAR", "TUNDRA" -> selectComboIfPresent(comboWorld, "Alpine Tundra (Valais, CH)");
                    case "BOREAL_TAIGA", "TAIGA" -> selectComboIfPresent(comboWorld, "Boreal Taiga (Rovaniemi, FI)");
                    default -> selectComboIfPresent(comboWorld, "Temperate Deciduous (Fontainebleau, FR)");
                }
            }
            alignWeatherWithWorld();

            // Populate species cards
            if (!scenario.getColonies().isEmpty()) {
                for (org.swarmforge.core.scenario.Scenario.ColonySetup setup : scenario.getColonies()) {
                    addSpeciesCard(setup.speciesName());
                    if (!speciesCardList.isEmpty()) {
                        SpeciesConfigCard card = speciesCardList.get(speciesCardList.size() - 1);
                        card.setQueenCount(setup.queenCount());
                        card.setWorkerCount(setup.workerCount());
                        card.setSoldierCount(setup.soldierCount());
                        card.setInitialFood(setup.initialFoodStore());
                    }
                }
            }
            return;
        }

        // 2. Built-in Preset Fallbacks
        if (metaName == null) return;
        String mLower = metaName.toLowerCase();
        if (mLower.contains("claustral") || mLower.contains("démarrage") || mLower.contains("monogyne") || mLower.contains("foundation")) {
            selectComboIfPresent(comboWorld, "Temperate Deciduous (Fontainebleau, FR)");
            selectComboIfPresent(comboWeather, "Temperate");
            areaDescription.setText(I18nManager.getInstance().get("sim.preset.desc.claustral"));
            addSpeciesCard("Fourmi Noire des Jardins (Lasius niger)");
            if (!speciesCardList.isEmpty()) {
                SpeciesConfigCard card = speciesCardList.get(0);
                card.setQueenCount(1);
                card.setWorkerCount(0);
                card.setSoldierCount(0);
            }
        } else if (mLower.contains("amazon") || mLower.contains("atta") || mLower.contains("attine") || mLower.contains("neotropical")) {
            selectComboIfPresent(comboWorld, "Tropical Rainforest (Manaus, BR)");
            selectComboIfPresent(comboWeather, "Tropical");
            areaDescription.setText(I18nManager.getInstance().get("sim.preset.desc.amazon"));
            addSpeciesCard("Fourmi Coupeuse de Feuilles (Atta sexdens)");
        } else if (mLower.contains("granivore") || mLower.contains("messor") || mLower.contains("steppe") || mLower.contains("xeric")) {
            selectComboIfPresent(comboWorld, "Arid Desert (Erg Chebbi, MA)");
            selectComboIfPresent(comboWeather, "Arid");
            areaDescription.setText(I18nManager.getInstance().get("sim.preset.desc.granivore"));
            addSpeciesCard("Fourmi Moissonneuse (Pogonomyrmex barbatus)");
        } else if (mLower.contains("guerre") || mLower.contains("war") || mLower.contains("territoriale") || mLower.contains("competition") || mLower.contains("solenopsis")) {
            selectComboIfPresent(comboWorld, "Temperate Deciduous (Fontainebleau, FR)");
            selectComboIfPresent(comboWeather, "Temperate");
            areaDescription.setText(I18nManager.getInstance().get("sim.preset.desc.war"));
            addSpeciesCard("Fourmi de Feu (Solenopsis invicta)");
            addSpeciesCard("Fourmi Noire des Jardins (Lasius niger)");
        } else if (mLower.contains("rucher") || mLower.contains("beehive") || mLower.contains("apis") || mLower.contains("bee")) {
            selectComboIfPresent(comboWorld, "Temperate Deciduous (Fontainebleau, FR)");
            selectComboIfPresent(comboWeather, "Temperate");
            areaDescription.setText(I18nManager.getInstance().get("sim.preset.desc.beehive"));
            addSpeciesCard("Abeille à Miel (Apis mellifera)");
        } else if (mLower.contains("termite") || mLower.contains("macrotermes") || mLower.contains("cathédrale") || mLower.contains("cathedral")) {
            selectComboIfPresent(comboWorld, "Tropical Rainforest (Manaus, BR)");
            selectComboIfPresent(comboWeather, "Tropical");
            areaDescription.setText(I18nManager.getInstance().get("sim.preset.desc.termites"));
            addSpeciesCard("Termite Souterrain (Reticulitermes flavipes)");
        } else if (mLower.contains("taïga") || mLower.contains("taiga") || mLower.contains("boréale") || mLower.contains("boreal") || mLower.contains("formica")) {
            selectComboIfPresent(comboWorld, "Boreal Taiga (Rovaniemi, FI)");
            selectComboIfPresent(comboWeather, "Arctic");
            areaDescription.setText(I18nManager.getInstance().get("sim.preset.desc.taiga"));
            addSpeciesCard("Fourmi Noire des Jardins (Lasius niger)");
        } else if (mLower.contains("supercolonie") || mLower.contains("supercolony") || mLower.contains("polycalique") || mLower.contains("linepithema")) {
            selectComboIfPresent(comboWorld, "Temperate Deciduous (Fontainebleau, FR)");
            selectComboIfPresent(comboWeather, "Temperate");
            areaDescription.setText(I18nManager.getInstance().get("sim.preset.desc.supercolony"));
            addSpeciesCard("Fourmi Noire des Jardins (Lasius niger)");
        }
    }

    private Button createIconButton(Feather icon, String tooltip) {
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(13);

        Button btn = new Button();
        btn.setGraphic(fontIcon);
        btn.getStyleClass().add("ctrl-btn");
        if (tooltip != null && !tooltip.isEmpty()) btn.setTooltip(new Tooltip(tooltip));
        btn.disabledProperty().addListener((obs, oldV, newV) -> btn.setOpacity(newV ? 0.35 : 1.0));
        return btn;
    }

    private Button createButton(String text, String tooltip) {
        Button btn = new Button(text);
        btn.getStyleClass().add("ctrl-btn");
        if (tooltip != null && !tooltip.isEmpty()) btn.setTooltip(new Tooltip(tooltip));
        btn.disabledProperty().addListener((obs, oldV, newV) -> btn.setOpacity(newV ? 0.35 : 1.0));
        return btn;
    }

    private Button createSmallButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("ctrl-btn-small");
        return btn;
    }

    public void resetTimelineTicks() {
        this.currentTick = 0;
        this.highestRecordedTick = 0;
        this.maxTick = 0;
        updateTick(0, 0);
    }

    private void updateButtonStates() {
        if (btnPlay != null && btnPause != null) {
            FontIcon playIcon = (btnPlay.getGraphic() instanceof FontIcon) ? (FontIcon) btnPlay.getGraphic() : null;
            FontIcon pauseIcon = (btnPause.getGraphic() instanceof FontIcon) ? (FontIcon) btnPause.getGraphic() : null;
            if (isPlaying) {
                btnPlay.setStyle("-fx-background-color: #16a34a; -fx-background-radius: 4; -fx-min-width: 32px; -fx-min-height: 28px; -fx-padding: 3px 6px; -fx-cursor: hand; -fx-border-color: #22c55e; -fx-border-width: 2px; -fx-border-radius: 4;");
                btnPause.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 4; -fx-min-width: 32px; -fx-min-height: 28px; -fx-padding: 3px 6px; -fx-cursor: hand; -fx-border-color: #334155; -fx-border-radius: 4;");
                if (playIcon != null) playIcon.setIconColor(javafx.scene.paint.Color.web("#ffffff"));
                if (pauseIcon != null) pauseIcon.setIconColor(javafx.scene.paint.Color.web("#94a3b8"));
            } else {
                btnPlay.setStyle("-fx-background-color: #0284c7; -fx-background-radius: 4; -fx-min-width: 32px; -fx-min-height: 28px; -fx-padding: 3px 6px; -fx-cursor: hand; -fx-border-color: #38bdf8; -fx-border-width: 2px; -fx-border-radius: 4;");
                btnPause.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 4; -fx-min-width: 32px; -fx-min-height: 28px; -fx-padding: 3px 6px; -fx-cursor: hand; -fx-border-color: #334155; -fx-border-radius: 4;");
                if (playIcon != null) playIcon.setIconColor(javafx.scene.paint.Color.web("#ffffff"));
                if (pauseIcon != null) pauseIcon.setIconColor(javafx.scene.paint.Color.web("#f8fafc"));
            }
        }

        if (btnRewind != null) btnRewind.setDisable(false);
        if (btnStepBack != null) btnStepBack.setDisable(false);
        if (btnStepForward != null) btnStepForward.setDisable(isPlaying);
        if (btnFastForward != null) btnFastForward.setDisable(isPlaying);
        if (btnGoToBeginning != null) btnGoToBeginning.setDisable(false);
        if (btnGoToEnd != null) btnGoToEnd.setDisable(isPlaying);
    }

    public void updateTick(long tick, long maxTick) {
        this.currentTick = tick;
        if (tick > this.highestRecordedTick) {
            this.highestRecordedTick = tick;
        }
        this.maxTick = Math.max(this.highestRecordedTick, Math.max(tick, maxTick));
        double totalSecs = tick * simulationStepSeconds;
        long totalSecondsElapsed = (long) totalSecs;
        currentDateTime = startDateTime.plusSeconds(totalSecondsElapsed);

        String timeStr = formatSimulationTime(tick, simulationStepSeconds);
        lblDateTime.setText("📅 Date & Heure : " + currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + String.format(" (Jour %d)", 1 + (totalSecondsElapsed / 86400)));
        if (lblTick != null) {
            lblTick.setText(timeStr + String.format(" (Pas #%d)", tick));
        }
        if (lblTime != null) lblTime.setText(timeStr);

        if (timelineSlider != null && !timelineSlider.isValueChanging()) {
            timelineSlider.setMax(Math.max(this.maxTick, tick + 100));
            timelineSlider.setValue(tick);
        }
    }

    public static String formatSimulationTime(long tick, double stepSeconds) {
        double totalSeconds = tick * stepSeconds;
        long wholeSec = (long) totalSeconds;
        long days = wholeSec / 86400;
        long hours = (wholeSec % 86400) / 3600;
        long minutes = (wholeSec % 3600) / 60;
        long seconds = wholeSec % 60;
        long cs = (long) Math.round((totalSeconds - Math.floor(totalSeconds)) * 100);
        if (cs >= 100) cs = 99;
        if (days > 0) {
            return String.format("%dj %02dh %02dm %02ds", days, hours, minutes, seconds);
        }
        return String.format("%02dh %02dm %02ds %02dcs", hours, minutes, seconds, cs);
    }

    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long hours = minutes / 60;
        return String.format("%02dh %02dm %02ds 00cs", hours, minutes % 60, seconds % 60);
    }

    public void setSpeed(float speed) {
        this.currentSpeed = speed;
        if (speedSlider != null) speedSlider.setValue(speed);
    }

    public float getSpeedMultiplier() {
        return currentSpeed;
    }

    public void setOnPlay(Consumer<Void> callback) { this.onPlay = callback; }
    public void setOnPause(Consumer<Void> callback) { this.onPause = callback; }
    public void setOnStop(Consumer<Void> callback) { this.onStop = callback; }
    public void setOnSpeedChange(Consumer<Float> callback) { this.onSpeedChange = callback; }
    public void setOnSeek(Consumer<Long> callback) { this.onSeek = callback; }
    public void setOnRewind(Consumer<Integer> callback) { this.onRewind = callback; }
    public void setOnStepForward(Consumer<Void> callback) { this.onStepForward = callback; }
    public void setOnStepChange(Consumer<Float> callback) { this.onStepChange = callback; }
    public void setOnCreateCheckpoint(Consumer<String> cb) { this.onCreateCheckpoint = cb; }
    public void setOnRestoreCheckpoint(Consumer<org.swarmforge.core.simulation.SimulationCheckpoint> cb) { this.onRestoreCheckpoint = cb; }
    public void setOnApplyPresets(Consumer<Long> callback) { this.onApplyPresets = callback; }

    public void updateCheckpoints(List<org.swarmforge.core.simulation.SimulationCheckpoint> checkpoints) {
        comboCheckpoints.getItems().clear();
        if (checkpoints != null) {
            comboCheckpoints.getItems().addAll(checkpoints);
            if (!comboCheckpoints.getItems().isEmpty()) {
                comboCheckpoints.getSelectionModel().selectLast();
            }
        }
    }

    public long getCurrentTick() { return currentTick; }
    public long getMaxTick() { return maxTick; }
    public float getSimulationStepSeconds() { return simulationStepSeconds; }

    public boolean isPlaying() { return isPlaying; }
    public boolean isPaused() { return isPaused; }

    public void setPlaying(boolean playing) {
        this.isPlaying = playing;
        this.isPaused = !playing;
        this.isStopped = false;
        updateButtonStates();
    }

    public void setPaused(boolean paused) {
        this.isPaused = paused;
        this.isPlaying = !paused;
        this.isStopped = false;
        updateButtonStates();
    }

    public String getSelectedScenarioName() {
        if (comboMeta != null && comboMeta.getValue() != null && !comboMeta.getValue().trim().isEmpty()) {
            return comboMeta.getValue().trim();
        }
        return "Scenario";
    }

    public String getSelectedWorld() {
        return comboWorld.getValue() != null ? comboWorld.getValue() : "Tempéré Standard (Temperate Forest)";
    }

    public String getSelectedWeather() {
        return comboWeather.getValue() != null ? comboWeather.getValue() : "Temperate";
    }

    public List<String> getSelectedSpeciesList() {
        List<String> list = new ArrayList<>();
        for (SpeciesConfigCard card : speciesCardList) {
            list.add(card.getSpeciesName());
        }
        if (list.isEmpty()) list.add("Fourmi Noire des Jardins (Lasius niger)");
        return list;
    }

    public String getSelectedSpecies() {
        List<String> list = getSelectedSpeciesList();
        return list.get(0);
    }

    public int getQueenCount() {
        return speciesCardList.isEmpty() ? 1 : speciesCardList.get(0).getQueenCount();
    }

    public int getWorkerCount() {
        return speciesCardList.isEmpty() ? 500 : speciesCardList.get(0).getWorkerCount();
    }

    public List<SpeciesConfigCard> getSpeciesCards() {
        return Collections.unmodifiableList(speciesCardList);
    }

    public int getSoldierCount() {
        return speciesCardList.isEmpty() ? 50 : speciesCardList.get(0).getSoldierCount();
    }

    public ArchitectureType getWorkerEngine() {
        return speciesCardList.isEmpty() ? ArchitectureType.BEHAVIOR_TREE : speciesCardList.get(0).getWorkerEngine();
    }

    public ArchitectureType getSoldierEngine() {
        return speciesCardList.isEmpty() ? ArchitectureType.FUZZY_LOGIC : speciesCardList.get(0).getSoldierEngine();
    }

    public ArchitectureType getQueenEngine() {
        return speciesCardList.isEmpty() ? ArchitectureType.BDI : speciesCardList.get(0).getQueenEngine();
    }

    public long getMasterSeed() {
        try {
            return Long.parseLong(txtSeed.getText().trim());
        } catch (Exception e) {
            return 12345L;
        }
    }

    public long getMaxSimulationTicks() {
        if (durationUnitCombo != null && ("∞ Unlimited".equals(durationUnitCombo.getValue()) || "∞ Illimité".equals(durationUnitCombo.getValue()))) {
            return Long.MAX_VALUE;
        }
        double val = maxDurationSpinner != null && maxDurationSpinner.getValue() != null ? maxDurationSpinner.getValue() : 100_000.0;
        String unit = durationUnitCombo != null ? durationUnitCombo.getValue() : "Ticks";
        double dt = getSimulationStepSeconds();
        if (dt <= 0) dt = 0.016666666666666666;

        double ticks;
        if (unit != null && (unit.startsWith("Seconds") || unit.startsWith("Secondes"))) {
            ticks = val / dt;
        } else if (unit != null && (unit.startsWith("Minutes") || unit.startsWith("Minutes"))) {
            ticks = (val * 60.0) / dt;
        } else if (unit != null && (unit.startsWith("Hours") || unit.startsWith("Heures"))) {
            ticks = (val * 3600.0) / dt;
        } else if (unit != null && (unit.startsWith("Days") || unit.startsWith("Jours"))) {
            ticks = (val * 86400.0) / dt;
        } else if (unit != null && (unit.startsWith("Months") || unit.startsWith("Mois"))) {
            ticks = (val * 30.0 * 86400.0) / dt;
        } else if (unit != null && (unit.startsWith("Years") || unit.startsWith("Années"))) {
            ticks = (val * 365.25 * 86400.0) / dt;
        } else {
            ticks = val;
        }
        return (long) Math.min(Long.MAX_VALUE, Math.max(1.0, ticks));
    }

    public int getMinPopulationStopThreshold() {
        return minPopStopSpinner != null && minPopStopSpinner.getValue() != null ? minPopStopSpinner.getValue() : 0;
    }

    public VBox getPlaybackAndSpeedPanel() {
        return playbackAndSpeedPanel;
    }

    private void handleSaveScenario() {
        String desc = areaDescription.getText() != null ? areaDescription.getText().trim() : "";
        if (desc.isEmpty()) {
            org.swarmforge.client.util.ThemeManager.createAlert(
                Alert.AlertType.WARNING,
                "Description required: Please enter a scientific description in the 'Scientific Scenario Description' area before saving the preset."
            ).show();
            return;
        }

        String name = comboMeta.getValue() != null ? comboMeta.getValue() : "New Scenario";
        TextInputDialog dialog = org.swarmforge.client.util.ThemeManager.createTextInputDialog(name);
        dialog.setTitle("Save Scenario Preset");
        dialog.setHeaderText("Scenario preset name (Description captured):");
        dialog.showAndWait().ifPresent(scenarioName -> {
            if (!scenarioName.trim().isEmpty()) {
                String clean = scenarioName.trim();
                if (!comboMeta.getItems().contains(clean)) comboMeta.getItems().add(clean);
                comboMeta.getSelectionModel().select(clean);
                org.swarmforge.client.util.ThemeManager.createAlert(
                    Alert.AlertType.INFORMATION,
                    "Scenario preset saved with description: " + clean
                ).show();
            }
        });
    }

    private void handleDeleteScenario() {
        String selected = comboMeta.getValue();
        if (selected == null || selected.isEmpty()) return;
        Alert alert = org.swarmforge.client.util.ThemeManager.createAlert(
            Alert.AlertType.CONFIRMATION,
            "Are you sure you want to delete scenario preset '" + selected + "'?"
        );
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                comboMeta.getItems().remove(selected);
                if (!comboMeta.getItems().isEmpty()) comboMeta.getSelectionModel().selectFirst();
            }
        });
    }

    private void handleExportScenario() {
        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Export Scenario Configuration");
        chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("JSON Files", "*.json"));
        chooser.setInitialFileName("scenario-preset.json");
        java.io.File file = chooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try {
                ScenarioSetupSnapshot snapshot = captureSetupSnapshotOnFXThread();
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
                mapper.writeValue(file, snapshot);
                org.swarmforge.client.util.NotificationOverlay.show(
                    this,
                    "Scenario successfully exported to " + file.getName(),
                    org.swarmforge.client.util.NotificationOverlay.NotificationType.SUCCESS
                );
            } catch (Exception ex) {
                org.swarmforge.client.util.ThemeManager.createAlert(
                    Alert.AlertType.ERROR,
                    "Error exporting scenario: " + ex.getMessage()
                ).show();
            }
        }
    }

    private void handleImportScenario() {
        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Import Scenario Configuration");
        chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("JSON Files", "*.json"));
        java.io.File file = chooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                ScenarioSetupSnapshot snapshot = mapper.readValue(file, ScenarioSetupSnapshot.class);
                if (snapshot != null) {
                    if (snapshot.seed() != 0) {
                        txtSeed.setText(String.valueOf(snapshot.seed()));
                    }
                    if (snapshot.startDateTimeIso() != null && !snapshot.startDateTimeIso().isEmpty()) {
                        try {
                            startDateTime = LocalDateTime.parse(snapshot.startDateTimeIso());
                            if (startDatePicker != null) startDatePicker.setValue(startDateTime.toLocalDate());
                            if (startTimeHourSpinner != null) startTimeHourSpinner.getValueFactory().setValue(startDateTime.getHour());
                            if (startTimeMinuteSpinner != null) startTimeMinuteSpinner.getValueFactory().setValue(startDateTime.getMinute());
                            if (startTimeSecondSpinner != null) startTimeSecondSpinner.getValueFactory().setValue(startDateTime.getSecond());
                            updateTick(currentTick, maxTick);
                        } catch (Exception ignored) {}
                    }
                    if (snapshot.simulationStepSeconds() > 0) {
                        this.simulationStepSeconds = snapshot.simulationStepSeconds();
                        if (Math.abs(simulationStepSeconds - 0.0166f) < 0.005f) {
                            scenarioStepCombo.getSelectionModel().select(0);
                        } else if (Math.abs(simulationStepSeconds - 0.05f) < 0.01f) {
                            scenarioStepCombo.getSelectionModel().select(1);
                        } else if (Math.abs(simulationStepSeconds - 0.1f) < 0.01f) {
                            scenarioStepCombo.getSelectionModel().select(2);
                        } else if (Math.abs(simulationStepSeconds - 1.0f) < 0.1f) {
                            scenarioStepCombo.getSelectionModel().select(3);
                        } else if (Math.abs(simulationStepSeconds - 5.0f) < 0.5f) {
                            scenarioStepCombo.getSelectionModel().select(4);
                        }
                        updateStepDtLabel();
                    }
                    if (snapshot.maxDurationValue() > 0 && maxDurationSpinner != null) {
                        maxDurationSpinner.getValueFactory().setValue(snapshot.maxDurationValue());
                    }
                    if (snapshot.maxDurationUnit() != null && durationUnitCombo != null) {
                        selectComboIfPresent(durationUnitCombo, snapshot.maxDurationUnit());
                    }
                    if (snapshot.minPopStopThreshold() >= 0 && minPopStopSpinner != null) {
                        minPopStopSpinner.getValueFactory().setValue(snapshot.minPopStopThreshold());
                    }
                    if (snapshot.description() != null && areaDescription != null) {
                        areaDescription.setText(snapshot.description());
                    }
                    if (snapshot.selectedWorld() != null) selectComboIfPresent(comboWorld, snapshot.selectedWorld());
                    if (snapshot.selectedWeather() != null) selectComboIfPresent(comboWeather, snapshot.selectedWeather());

                    speciesCardList.clear();
                    if (snapshot.speciesSnapshots() != null && !snapshot.speciesSnapshots().isEmpty()) {
                        for (SpeciesConfigSnapshot sSnap : snapshot.speciesSnapshots()) {
                            addSpeciesCard(sSnap.speciesName());
                            if (!speciesCardList.isEmpty()) {
                                SpeciesConfigCard card = speciesCardList.get(speciesCardList.size() - 1);
                                if (sSnap.nestType() != null) card.setNestType(sSnap.nestType());
                                card.setQueenCount(sSnap.queenCount());
                                card.setWorkerCount(sSnap.workerCount());
                                card.setSoldierCount(sSnap.soldierCount());
                                card.setInitialFood(sSnap.initialFood());
                                if (sSnap.accessorySnapshots() != null) {
                                    card.setAccessorySnapshots(sSnap.accessorySnapshots());
                                }
                            }
                        }
                    }
                    refreshSpeciesListContainer();
                    updateDurationCalculatedLabel();
                    updateValidationPanel();
                    org.swarmforge.client.util.NotificationOverlay.show(
                        this,
                        "Scenario successfully imported from " + file.getName(),
                        org.swarmforge.client.util.NotificationOverlay.NotificationType.SUCCESS
                    );
                }
            } catch (Exception ex) {
                org.swarmforge.client.util.ThemeManager.createAlert(
                    Alert.AlertType.ERROR,
                    "Error importing scenario: " + ex.getMessage()
                ).show();
            }
        }
    }

    private VBox buildCheckpointsPane() {
        VBox box = new VBox(6);
        box.getStyleClass().add("card-pane");
        box.setStyle("-fx-border-color: #d97706; -fx-border-width: 1;");

        Label lblTitle = new Label("7. 🔖 Checkpoints & Divine Mode Intervention Log :");
        lblTitle.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-font-size: 11px;");

        comboCheckpoints.setPrefWidth(160);
        comboCheckpoints.setPromptText("No checkpoints recorded");

        Button bRestore = new Button("⏪ Restore Checkpoint");
        bRestore.setStyle("-fx-background-color: #d97706; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-cursor: hand;");
        bRestore.setTooltip(new Tooltip("Restores simulation to exact tick timestamp of checkpoint."));
        bRestore.setOnAction(e -> {
            org.swarmforge.core.simulation.SimulationCheckpoint sel = comboCheckpoints.getValue();
            if (sel != null && onRestoreCheckpoint != null) {
                onRestoreCheckpoint.accept(sel);
            }
        });

        HBox row = new HBox(8, new Label("Checkpoints:") {{ setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 10px;"); }}, comboCheckpoints, bRestore);
        row.setAlignment(Pos.CENTER_LEFT);

        Label lblInfo = new Label("💡 Each checkpoint saves exact physical state and Divine Mode intervention log.");
        lblInfo.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");

        box.getChildren().addAll(lblTitle, row, lblInfo);
        return box;
    }

    private void selectComboIfPresent(ComboBox<String> combo, String val) {
        if (combo.getItems().contains(val)) {
            combo.getSelectionModel().select(val);
        } else if (!combo.getItems().isEmpty()) {
            combo.getSelectionModel().selectFirst();
        }
    }

    // =========================================================================
    // SCENARIO VALIDATION & DYNAMIC WARNING ENGINE
    // =========================================================================

    public record ScenarioWarning(
        String category,
        String severity,
        String message,
        String fixButtonText,
        Runnable fixAction
    ) {}

    private VBox buildValidationPanel() {
        VBox box = new VBox(6);
        box.getStyleClass().add("card-pane");
        box.setStyle("-fx-border-color: #38bdf8; -fx-border-width: 1px; -fx-border-radius: 5px;");

        Label lblTitle = new Label(I18nManager.getInstance().get("sim.validation.title"));
        lblTitle.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px;");

        Label lblSubtitle = new Label(I18nManager.getInstance().get("sim.validation.subtitle"));
        lblSubtitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 9.5px;");

        warningsContainer = new VBox(6);

        box.getChildren().addAll(lblTitle, lblSubtitle, new Separator(), warningsContainer);
        return box;
    }

    public void updateValidationPanel() {
        if (warningsContainer == null) return;
        warningsContainer.getChildren().clear();

        List<ScenarioWarning> warnings = evaluateScenarioWarnings();
        if (warnings.isEmpty()) {
            HBox okBox = new HBox(8);
            okBox.setAlignment(Pos.CENTER_LEFT);
            okBox.setStyle("-fx-background-color: rgba(34, 197, 94, 0.1); -fx-border-color: #22c55e; -fx-border-radius: 4px; -fx-padding: 6 10;");

            Label lblOkIcon = new Label("✅");
            lblOkIcon.setStyle("-fx-font-size: 12px;");

            Label lblOkText = new Label(I18nManager.getInstance().get("sim.validation.valid"));
            lblOkText.setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold; -fx-font-size: 11px;");

            okBox.getChildren().addAll(lblOkIcon, lblOkText);
            warningsContainer.getChildren().add(okBox);
        } else {
            for (ScenarioWarning warn : warnings) {
                HBox warnRow = new HBox(10);
                warnRow.setAlignment(Pos.CENTER_LEFT);
                String styleClass = "HIGH".equals(warn.severity()) ? "warn-row-high" : ("MEDIUM".equals(warn.severity()) ? "warn-row-medium" : "warn-row-info");
                warnRow.getStyleClass().add(styleClass);

                Label lblMsg = new Label(warn.message());
                lblMsg.getStyleClass().add("warn-msg-text");
                lblMsg.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(lblMsg, Priority.ALWAYS);

                Button btnFix = new Button(warn.fixButtonText());
                btnFix.getStyleClass().add("btn-primary");
                btnFix.setStyle("-fx-font-size: 10px; -fx-padding: 3 8; -fx-background-radius: 3;");
                btnFix.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("sim.warn.fix_tt"));
                btnFix.setOnAction(e -> {
                    warn.fixAction().run();
                    updateValidationPanel();
                });

                warnRow.getChildren().addAll(lblMsg, btnFix);
                warningsContainer.getChildren().add(warnRow);
            }
        }
    }

    public List<ScenarioWarning> evaluateScenarioWarnings() {
        List<ScenarioWarning> list = new ArrayList<>();

        // 1. Population & Espèces
        if (speciesCardList.isEmpty()) {
            list.add(new ScenarioWarning(
                "ESPÈCES", "HIGH",
                "🔴 No insect species configured. Simulation will start in an empty terrarium without colony.",
                "➕ Add (Lasius niger)",
                () -> addSpeciesCard("Fourmi Noire des Jardins (Lasius niger)")
            ));
        } else {
            int totalAnts = 0;
            for (SpeciesConfigCard card : speciesCardList) {
                int cardAnts = card.getQueenCount() + card.getWorkerCount() + card.getSoldierCount() + card.getBroodCount();
                totalAnts += cardAnts;

                if (card.getQueenCount() == 0) {
                    list.add(new ScenarioWarning(
                        "DÉMOGRAPHIE", "MEDIUM",
                        String.format("🟠 Colonie '%s' : Aucune Reine (0 reine). La colonie s'éteindra sans renouvellement du couvain.", card.getSpeciesName()),
                        "👑 Add 1 Queen",
                        () -> card.setQueenCount(1)
                    ));
                }

                if (card.getQueenCount() > 0 && card.getBroodCount() <= 0) {
                    list.add(new ScenarioWarning(
                        "DÉMOGRAPHIE", "MEDIUM",
                        String.format("🟠 Colonie '%s' : Reine présente mais couvain nul (0 couvain). Renouvellement de la colonie compromis.", card.getSpeciesName()),
                        "🥚 Add 500 Brood",
                        () -> card.setBroodCount(500)
                    ));
                }

                String nestType = card.getNestType() != null ? card.getNestType() : getSelectedNestType();
                int estCap = estimateNestCapacity(nestType);
                if (cardAnts > estCap) {
                    list.add(new ScenarioWarning(
                        "NID", "MEDIUM",
                        String.format("🟠 Colonie '%s' : Population (%,d ind.) > Capacité du nid '%s' (~%,d ind.). %,d ind. émergeront en surface.",
                            card.getSpeciesName(), cardAnts, nestType, estCap, (cardAnts - estCap)),
                        "🏰 Expand Nest",
                        () -> {
                            List<String> compats = getCompatibleNestTypes(card.getSpeciesName());
                            if (compats.size() > 1) {
                                card.setNestType(compats.get(compats.size() - 1));
                            } else {
                                card.setWorkerCount(Math.min(card.getWorkerCount(), estCap));
                            }
                        }
                    ));
                }

                // Biological Nest Incompatibility check
                List<String> compats = getCompatibleNestTypes(card.getSpeciesName());
                if (card.getNestType() != null && !compats.isEmpty() && !compats.contains(card.getNestType())) {
                    list.add(new ScenarioWarning(
                        "NID", "MEDIUM",
                        String.format("🟠 Le nid '%s' n'est pas biologiquement adapté à l'espèce '%s'.", card.getNestType(), card.getSpeciesName()),
                        "🔄 Align with Adapted Nest",
                        () -> card.setNestType(compats.get(0))
                    ));
                }

                // Initial food reserve check
                if (card.getInitialFood() <= 0) {
                    list.add(new ScenarioWarning(
                        "RESSOURCES", "INFO",
                        String.format("💡 Colonie '%s' : Réserve initiale de nourriture nulle (0 unités). Risque de famine rapide.", card.getSpeciesName()),
                        "🍖 Allocate 500 Food",
                        () -> card.setInitialFood(500)
                    ));
                }
            }

            if (totalAnts == 0) {
                list.add(new ScenarioWarning(
                    "ESPÈCES", "HIGH",
                    "🔴 Total insect count zero (0 workers, 0 queens).",
                    "➕ Add 500 Workers",
                    () -> {
                        if (!speciesCardList.isEmpty()) {
                            speciesCardList.get(0).setWorkerCount(500);
                            speciesCardList.get(0).setQueenCount(1);
                        }
                    }
                ));
            }
        }

        // 2. Climat vs Biotope Alignment
        String world = comboWorld.getValue();
        String weather = comboWeather.getValue();
        if (world != null && weather != null) {
            String wLower = world.toLowerCase();
            String weatherLower = weather.toLowerCase();

            boolean isAridWorld = wLower.contains("désert") || wLower.contains("aride") || wLower.contains("arid") || wLower.contains("erg");
            boolean isPolarWorld = wLower.contains("alpin") || wLower.contains("toundra") || wLower.contains("arctic") || wLower.contains("taïga");
            boolean isTropicalWorld = wLower.contains("tropical") || wLower.contains("amazon") || wLower.contains("jungle");

            if (isAridWorld && (weatherLower.contains("arctic") || weatherLower.contains("polar"))) {
                list.add(new ScenarioWarning(
                    "CLIMAT", "HIGH",
                    String.format("🔴 Incompatibilité Climat/Biotope : Biotope aride ('%s') associé à un climat polaire ('%s').", world, weather),
                    "🌤️ Align Climate",
                    () -> alignWeatherWithWorld()
                ));
            } else if (isPolarWorld && (weatherLower.contains("arid") || weatherLower.contains("tropical"))) {
                list.add(new ScenarioWarning(
                    "CLIMAT", "HIGH",
                    String.format("🔴 Incompatibilité Climat/Biotope : Biotope nordique/alpin ('%s') associé à un climat aride/tropical ('%s').", world, weather),
                    "🌤️ Align Climate",
                    () -> alignWeatherWithWorld()
                ));
            } else if (isTropicalWorld && weatherLower.contains("arctic")) {
                list.add(new ScenarioWarning(
                    "CLIMAT", "HIGH",
                    String.format("🔴 Incompatibilité Climat/Biotope : Jungle tropicale ('%s') sous un climat arctique ('%s').", world, weather),
                    "🌤️ Align Climate",
                    () -> alignWeatherWithWorld()
                ));
            }
        }

        // 2b. Species Thermal Tolerance vs Biome Temperature Cross-Reference
        if (world != null || weather != null) {
            float estimatedTemp = estimateAmbientTemperature(world, weather);
            for (SpeciesConfigCard card : speciesCardList) {
                String speciesName = card.getSpeciesName();
                org.swarmforge.core.species.Species sp = org.swarmforge.core.species.SpeciesRegistry.getInstance().getSpecies(speciesName);
                if (sp != null) {
                    float minT = sp.getMinTempCelsius();
                    float maxT = sp.getMaxTempCelsius();
                    if (estimatedTemp < minT || estimatedTemp > maxT) {
                        list.add(new ScenarioWarning(
                            "STRESS THERMIQUE", "HIGH",
                            String.format("⚠️ Risque de Stress Thermique : Température estimée (%.1f°C) hors des tolérances de '%s' [%.1f°C - %.1f°C].",
                                estimatedTemp, card.getSpeciesName(), minT, maxT),
                            "🌡️ Adjust Compatible Climate",
                            () -> autoSelectCompatibleEnvironmentForSpecies(sp)
                        ));
                    }
                }
            }
        }

        // 3. Pas de calcul Δt vs Population scale
        int grandTotalAnts = 0;
        for (SpeciesConfigCard card : speciesCardList) {
            grandTotalAnts += card.getQueenCount() + card.getWorkerCount() + card.getSoldierCount() + card.getBroodCount();
        }
        if (simulationStepSeconds >= 5.0f && grandTotalAnts > 50000) {
            list.add(new ScenarioWarning(
                "PHYSIQUE", "INFO",
                String.format("💡 Pas de calcul Δt très élevé (5.0 s) pour %,d individus. Des imprécisions d'intégration éthologique peuvent se produire.", grandTotalAnts),
                "⏱️ Set Δt to 50 ms",
                () -> {
                    scenarioStepCombo.getSelectionModel().select(1);
                    simulationStepSeconds = 0.05f;
                    updateStepDtLabel();
                }
            ));
        }

        // 4. Durée Max
        String dUnit = durationUnitCombo != null ? durationUnitCombo.getValue() : "Days (d)";
        if ("Days (d)".equals(dUnit) && maxDurationSpinner.getValue() != null && maxDurationSpinner.getValue() > 365) {
            list.add(new ScenarioWarning(
                "DURÉE", "INFO",
                String.format("💡 Durée maximale très élevée (%.0f jours). Une durée de 30 à 100 jours est généralement recommandée.", maxDurationSpinner.getValue()),
                "⏱️ Set to 30 days",
                () -> maxDurationSpinner.getValueFactory().setValue(30.0)
            ));
        } else if ("Ticks".equals(dUnit) && maxDurationSpinner.getValue() != null && maxDurationSpinner.getValue() < 1000) {
            list.add(new ScenarioWarning(
                "DURÉE", "INFO",
                String.format("💡 Nombre de ticks très court (%.0f). La simulation risque de s'arrêter prématurément.", maxDurationSpinner.getValue()),
                "⏱️ Set to 100,000 ticks",
                () -> maxDurationSpinner.getValueFactory().setValue(100000.0)
            ));
        }

        // 5. Seuil Arrêt Population Min
        if (minPopStopSpinner.getValue() != null && minPopStopSpinner.getValue() > 0 && minPopStopSpinner.getValue() >= grandTotalAnts && grandTotalAnts > 0) {
            list.add(new ScenarioWarning(
                "ARRÊT", "MEDIUM",
                String.format("🟠 Seuil d'arrêt de population (%,d ind.) >= population initiale (%,d ind.). La simulation s'arrêtera immédiatement.", minPopStopSpinner.getValue(), grandTotalAnts),
                "🛑 Disable Threshold (0)",
                () -> minPopStopSpinner.getValueFactory().setValue(0)
            ));
        }

        // 6. Seed Validity
        try {
            Long.parseLong(txtSeed.getText().trim());
        } catch (Exception e) {
            list.add(new ScenarioWarning(
                "SEED", "INFO",
                "💡 Graine aléatoire (Seed) non numérique ou vide. Un seed par défaut sera attribué.",
                "🎲 New Seed",
                () -> txtSeed.setText(String.valueOf((long)(Math.random() * 900000 + 100000)))
            ));
        }

        // 7. Dynamic Trophic & Food Web Checks (Predator vs Prey availability)
        for (SpeciesConfigCard card : speciesCardList) {
            if (card.hasActivePredatorAccessory() && !card.hasActivePreyAccessory() && card.getInitialFood() <= 0) {
                list.add(new ScenarioWarning(
                    "ÉCOSYSTÈME / TROPHIQUE", "MEDIUM",
                    String.format("🟠 Incompatibilité Trophique : L'écosystème de '%s' comporte des prédateurs sans proies d'appoint ni réserve initiale de nourriture.", card.getSpeciesName()),
                    "🦗 Enable Prey (Arthropods / Mealworms)",
                    () -> {
                        card.enableAccessorySpeciesRole("PROIE");
                        card.setInitialFood(500);
                    }
                ));
            }
        }

        return list;
    }

    private float estimateAmbientTemperature(String world, String weather) {
        String wStr = (world != null ? world : "") + " " + (weather != null ? weather : "");
        String s = wStr.toLowerCase();
        if (s.contains("arctic") || s.contains("polar") || s.contains("toundra") || s.contains("alpin") || s.contains("rovaniemi")) {
            return -5.0f;
        } else if (s.contains("arid") || s.contains("desert") || s.contains("désert") || s.contains("chebbi") || s.contains("savanna")) {
            return 38.0f;
        } else if (s.contains("tropical") || s.contains("manaus") || s.contains("rainforest") || s.contains("jungle")) {
            return 28.0f;
        } else {
            return 20.0f; // Temperate default
        }
    }

    private void autoSelectCompatibleEnvironmentForSpecies(org.swarmforge.core.species.Species sp) {
        if (sp == null) return;
        float minT = sp.getMinTempCelsius();
        float maxT = sp.getMaxTempCelsius();
        if (minT >= 22.0f) {
            selectComboIfPresent(comboWorld, "Arid Savanna (Serengeti, TZ)");
            selectComboIfPresent(comboWeather, "Sunny Warm (28°C)");
        } else if (maxT <= 18.0f) {
            selectComboIfPresent(comboWorld, "Boreal Taiga (Rovaniemi, FI)");
            selectComboIfPresent(comboWeather, "Cold Polar (-5°C)");
        } else {
            selectComboIfPresent(comboWorld, "Temperate Deciduous (Fontainebleau, FR)");
            selectComboIfPresent(comboWeather, "Temperate Mild (20°C)");
        }
    }

    // =========================================================================
    // INNER CLASSES: Multi-Species Configuration Card & Accessory Config
    // =========================================================================

    /**
     * Interactive UI Card for configuring a single main species inside a scenario.
     * Contains filtered nest options, nest placement, pre-generation options,
     * biologically filtered accessories, demographics, and AI engines per caste.
     */
    public static class SpeciesConfigCard {

        private final String speciesName;
        private final VBox cardPane = new VBox(8);

        private Runnable onChange;

        public void setOnChange(Runnable onChange) {
            this.onChange = onChange;
        }

        private void triggerChange() {
            if (onChange != null) onChange.run();
        }

        private final ComboBox<String> nestTypeCombo = new ComboBox<>();
        private final ComboBox<String> nestStageCombo = new ComboBox<>();
        private final ComboBox<String> nestPlacementCombo = new ComboBox<>();
        private final ComboBox<String> nestRelationCombo = new ComboBox<>();
        private final Spinner<Double> posXSpinner = new Spinner<>(-500.0, 500.0, 0.0, 10.0);
        private final Spinner<Double> posZSpinner = new Spinner<>(-500.0, 500.0, 0.0, 10.0);
        private final Spinner<Integer> initialFoodSpinner = new Spinner<>(0, 50000, 500, 50);

        private final Spinner<Integer> queenSpinner = new Spinner<>(0, 50, 1);
        private final Spinner<Integer> workerSpinner = new Spinner<>(0, 10000, 500, 50);
        private final Spinner<Integer> soldierSpinner = new Spinner<>(0, 2000, 50, 10);
        private final Spinner<Integer> broodSpinner = new Spinner<>(0, 2000000, 20000, 1000);

        private final ComboBox<ArchitectureType> workerEngineCombo = new ComboBox<>();
        private final ComboBox<ArchitectureType> soldierEngineCombo = new ComboBox<>();
        private final ComboBox<ArchitectureType> queenEngineCombo = new ComboBox<>();

        private final VBox accessoryBoxPane = new VBox(4);

        public SpeciesConfigCard(String speciesName, Runnable onRemove) {
            this.speciesName = speciesName;

            cardPane.getStyleClass().add("card-pane");

            // Header
            HBox header = new HBox(8);
            header.setAlignment(Pos.CENTER_LEFT);

            Label lblTitle = new Label("🐜 Species: " + speciesName);
            lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
            lblTitle.getStyleClass().add("accent-title");

            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            Button btnRemove = new Button("🗑️ Remove Species");
            btnRemove.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-cursor: hand;");
            btnRemove.setOnAction(e -> onRemove.run());

            header.getChildren().addAll(lblTitle, sp, btnRemove);

            // 1. Filtered Nest, Placement & Inter-Nest Strategy
            HBox nestRow = new HBox(8);
            nestRow.setAlignment(Pos.CENTER_LEFT);

            Label lblNest = new Label("🏰 Nest Architecture :");
            lblNest.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");
            lblNest.getStyleClass().add("purple-accent-title");

            selectDefaultNestArchitectureForSpecies(speciesName, nestTypeCombo);
            nestTypeCombo.setTooltip(new Tooltip("Nest physical shape and architectural typology suited to this species."));

            Label lblStage = new Label("🌱 Nest Stage / Age :");
            lblStage.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");
            lblStage.getStyleClass().add("purple-accent-title");

            nestStageCombo.getItems().addAll(
                "👑 Claustral Founding (1 Queen, 0 Workers, 10 Brood)",
                "🌱 Young Colony (1 Queen, 100 Workers, 50 Brood)",
                "🏰 Mature Colony (1 Queen, 5,000 Workers, 2,500 Brood)",
                "🌐 Complex Supercolony (50 Queens, 500,000 Workers, 200,000 Brood)"
            );
            nestStageCombo.getSelectionModel().select(1); // Young colony by default
            nestStageCombo.setTooltip(new Tooltip("Colony demographic maturity stage. Automatically adjusts caste counts."));

            Label lblPlacement = new Label("📍 Placement :");
            lblPlacement.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");
            lblPlacement.getStyleClass().add("purple-accent-title");

            nestPlacementCombo.getItems().addAll(
                "📍 Center of Map (Optimal Unflooded Zone)",
                "✋ Manual Placement (Coordinates X, Z)",
                "👑 Queen Foundation (Virgin Surface Soil)",
                "🎲 Random Position (Dispersed Across Map)"
            );
            nestPlacementCombo.getSelectionModel().selectFirst();
            nestPlacementCombo.setTooltip(new Tooltip("Spatial placement strategy of the nest in the 3D grid."));

            Label lblInitialFood = new Label("🍖 Food :");
            lblInitialFood.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");
            lblInitialFood.getStyleClass().add("purple-accent-title");

            initialFoodSpinner.setPrefWidth(75);
            initialFoodSpinner.setEditable(true);
            initialFoodSpinner.setTooltip(new Tooltip("Initial food resource reserve allocated to the nest."));

            nestRow.getChildren().addAll(lblNest, nestTypeCombo, lblStage, nestStageCombo, lblPlacement, nestPlacementCombo, lblInitialFood, initialFoodSpinner);

            // Manual Position Box (Visible when manual placement is chosen)
            HBox manualPosBox = new HBox(8);
            manualPosBox.setAlignment(Pos.CENTER_LEFT);
            manualPosBox.setStyle("-fx-padding: 4 0 0 0;");

            Label lblManualX = new Label("X Coordinate :");
            lblManualX.setStyle("-fx-font-size: 10px;");
            lblManualX.getStyleClass().add("accent-title");
            posXSpinner.setPrefWidth(85); posXSpinner.setEditable(true);

            Label lblManualZ = new Label("Z Coordinate :");
            lblManualZ.setStyle("-fx-font-size: 10px;");
            lblManualZ.getStyleClass().add("accent-title");
            posZSpinner.setPrefWidth(85); posZSpinner.setEditable(true);

            manualPosBox.getChildren().addAll(lblManualX, posXSpinner, lblManualZ, posZSpinner);
            manualPosBox.setVisible(false);
            manualPosBox.setManaged(false);

            nestPlacementCombo.valueProperty().addListener((o, oldV, newV) -> {
                boolean isManual = newV != null && (newV.contains("Manual") || newV.contains("Manuel"));
                manualPosBox.setVisible(isManual);
                manualPosBox.setManaged(isManual);
            });

            // Inter-Nest Relationship Strategy & Supercolony Checkbox Row
            VBox relationBox = new VBox(4);

            HBox relationRow = new HBox(8);
            relationRow.setAlignment(Pos.CENTER_LEFT);

            Label lblRelation = new Label("⚔️ Inter-Nest Strategy (Same Species) :");
            lblRelation.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");
            lblRelation.getStyleClass().add("sub-title");

            nestRelationCombo.getItems().addAll(
                "⚔️ Monocolonial Competition (Inter-nest Warfare - Cuticular Hydrocarbons)",
                "🤝 Supercolony Unicolonial (Tolerance, Mutual Cooperation & Shared Workers/Brood)",
                "🛡️ Territorial Neutrality (Passive Avoidance without Direct Combat)"
            );
            nestRelationCombo.getSelectionModel().selectFirst();
            nestRelationCombo.setPrefWidth(270);
            nestRelationCombo.setTooltip(new Tooltip("Configures behavioral interactions if multiple nests of the same species are instantiated in the simulation."));

            relationRow.getChildren().addAll(lblRelation, nestRelationCombo);

            CheckBox chkSupercolonyMember = new CheckBox("🤝 Join Polycalic Supercolony Network (Cooperation, free passage & brood sharing among allied nests)");
            chkSupercolonyMember.setSelected(false);
            chkSupercolonyMember.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
            chkSupercolonyMember.getStyleClass().add("accent-title");
            chkSupercolonyMember.setTooltip(new Tooltip("Check to integrate this colony into the cooperative supercolony network. Unchecked nests remain independent colonies in competition or warfare."));

            relationBox.getChildren().addAll(relationRow, chkSupercolonyMember);

            // 2. Demographics & AI Engines Standard Block (Unified AI engine by default across castes)
            VBox demoBox = new VBox(6);
            demoBox.getStyleClass().add("card-pane");

            Label lblDemoTitle = new Label("🧠 Demographics & Castes (Queens, Workers, Soldiers & Unified AI Engine)");
            lblDemoTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
            lblDemoTitle.getStyleClass().add("accent-title");

            Label lblSpatialInfo = new Label("ℹ️ Demographics & AI Note: Population sizes (Queens, Workers, Soldiers, Brood) are automatically proportioned to the selected nest development stage.");
            lblSpatialInfo.setStyle("-fx-font-size: 9.5px; -fx-wrap-text: true;");
            lblSpatialInfo.getStyleClass().add("sub-title-gray");

            GridPane demoGrid = new GridPane();
            demoGrid.setHgap(8); demoGrid.setVgap(4);

            for (ArchitectureType type : ArchitectureType.values()) {
                workerEngineCombo.getItems().add(type);
                soldierEngineCombo.getItems().add(type);
                queenEngineCombo.getItems().add(type);
            }

            SpeciesPresetManager spm = new SpeciesPresetManager();
            CustomSpecies speciesObj = spm.getPreset(speciesName);
            ArchitectureType workerArch = ArchitectureType.BEHAVIOR_TREE;
            ArchitectureType soldierArch = ArchitectureType.BEHAVIOR_TREE;
            ArchitectureType queenArch = ArchitectureType.BEHAVIOR_TREE;

            if (speciesObj != null && speciesObj.getCasteTemplates() != null && !speciesObj.getCasteTemplates().isEmpty()) {
                for (CasteTemplate ct : speciesObj.getCasteTemplates()) {
                    String cName = ct.getName() != null ? ct.getName().toLowerCase() : "";
                    ArchitectureType parsed = ArchitectureType.parse(ct.getDecisionArchitectureType());
                    if (cName.contains("worker") || cName.contains("ouvrier")) {
                        workerArch = parsed;
                    } else if (cName.contains("soldier") || cName.contains("soldat") || cName.contains("major")) {
                        soldierArch = parsed;
                    } else if (cName.contains("queen") || cName.contains("reine")) {
                        queenArch = parsed;
                    } else {
                        workerArch = parsed;
                        soldierArch = parsed;
                        queenArch = parsed;
                    }
                }
            }

            workerEngineCombo.getSelectionModel().select(workerArch);
            soldierEngineCombo.getSelectionModel().select(soldierArch);
            queenEngineCombo.getSelectionModel().select(queenArch);

            // Sync caste engines when worker engine changes (unless user explicitly changes others)
            workerEngineCombo.valueProperty().addListener((o, oldV, newV) -> {
                if (newV != null) {
                    soldierEngineCombo.getSelectionModel().select(newV);
                    queenEngineCombo.getSelectionModel().select(newV);
                }
            });

            workerSpinner.setPrefWidth(90); workerSpinner.setEditable(true);
            soldierSpinner.setPrefWidth(90); soldierSpinner.setEditable(true);
            queenSpinner.setPrefWidth(90); queenSpinner.setEditable(true);
            broodSpinner.setPrefWidth(90); broodSpinner.setEditable(true);

            // Set higher default ranges for realistic biological scale (up to millions for supercolonies)
            workerSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5000000, 100, 100));
            soldierSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 500000, 10, 10));
            queenSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 1, 1));
            broodSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 2000000, 50, 50));

            // Auto-adjust demographics when nest stage changes
            nestStageCombo.valueProperty().addListener((o, oldV, newV) -> {
                applyDemographicsFromStage(nestStageCombo.getSelectionModel().getSelectedIndex());
                triggerChange();
            });

            queenSpinner.valueProperty().addListener((o, oldV, newV) -> triggerChange());
            workerSpinner.valueProperty().addListener((o, oldV, newV) -> triggerChange());
            soldierSpinner.valueProperty().addListener((o, oldV, newV) -> triggerChange());
            broodSpinner.valueProperty().addListener((o, oldV, newV) -> triggerChange());
            initialFoodSpinner.valueProperty().addListener((o, oldV, newV) -> triggerChange());

            // Trigger initial population calculation for default Stage 1 (Young Colony)
            applyDemographicsFromStage(1);

            Label lblQ = new Label("👑 Queens :"); lblQ.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 10px;");
            Label lblQEngine = new Label("AI Engine :"); lblQEngine.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;");
            demoGrid.add(lblQ, 0, 0); demoGrid.add(queenSpinner, 1, 0); demoGrid.add(lblQEngine, 2, 0); demoGrid.add(queenEngineCombo, 3, 0);

            Label lblW = new Label("🐜 Workers :"); lblW.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 10px;");
            Label lblWEngine = new Label("AI Engine :"); lblWEngine.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;");
            demoGrid.add(lblW, 0, 1); demoGrid.add(workerSpinner, 1, 1); demoGrid.add(lblWEngine, 2, 1); demoGrid.add(workerEngineCombo, 3, 1);

            Label lblS = new Label("⚔️ Soldiers :"); lblS.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 10px;");
            Label lblSEngine = new Label("AI Engine :"); lblSEngine.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;");
            demoGrid.add(lblS, 0, 2); demoGrid.add(soldierSpinner, 1, 2); demoGrid.add(lblSEngine, 2, 2); demoGrid.add(soldierEngineCombo, 3, 2);

            Label lblB = new Label("🥚 Brood :"); lblB.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 10px;");
            demoGrid.add(lblB, 0, 3); demoGrid.add(broodSpinner, 1, 3);

            demoBox.getChildren().addAll(lblDemoTitle, lblSpatialInfo, demoGrid);

            // 3. Filtered Accessory Species Section (Proies, Prédateurs & Commensaux)
            Label lblAccessoryTitle = new Label("🦗 Accessory Species & Ecological Roles (Filtered for " + getShortSpeciesName(speciesName) + ") :");
            lblAccessoryTitle.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 10px;");

            setupAccessoryRows(speciesName);

            cardPane.getChildren().addAll(header, nestRow, manualPosBox, relationBox, new Separator(), demoBox, new Separator(), lblAccessoryTitle, accessoryBoxPane);
        }

        private final List<AccessoryRowControls> accessoryControlsList = new ArrayList<>();

        private static class AccessoryRowControls {
            CheckBox chk;
            String name;
            String role;
            Spinner<Integer> countSpinner;
            ComboBox<String> strategyCombo;

            AccessoryRowControls(CheckBox chk, String name, String role, Spinner<Integer> countSpinner, ComboBox<String> strategyCombo) {
                this.chk = chk;
                this.name = name;
                this.role = role;
                this.countSpinner = countSpinner;
                this.strategyCombo = strategyCombo;
            }
        }

        private void setupAccessoryRows(String speciesName) {
            accessoryBoxPane.getChildren().clear();
            accessoryControlsList.clear();
            List<AccessorySpeciesInfo> compatList = getCompatibleAccessorySpecies(speciesName);

            VBox listVBox = new VBox(4);
            for (AccessorySpeciesInfo info : compatList) {
                HBox row = new HBox(8);
                row.setAlignment(Pos.CENTER_LEFT);

                CheckBox chk = new CheckBox(info.name());
                chk.setSelected(true);
                chk.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 10px; -fx-font-weight: bold;");
                chk.setTooltip(new Tooltip(info.description()));
                chk.setPrefWidth(220);
                chk.selectedProperty().addListener((o, oldV, newV) -> triggerChange());

                Label lblRole = new Label("[" + info.role() + "]");
                lblRole.setStyle(getRoleStyle(info.role()));
                lblRole.setPrefWidth(90);

                Label lblCount = new Label("Initial Count :");
                lblCount.setStyle("-fx-text-fill: #aaa; -fx-font-size: 9px;");

                Spinner<Integer> countSpinner = new Spinner<>(5, 1000, info.defaultCount(), 10);
                countSpinner.setPrefWidth(70); countSpinner.setEditable(true);
                countSpinner.valueProperty().addListener((o, oldV, newV) -> triggerChange());

                Label lblStrategy = new Label("Replenishment Strategy :");
                lblStrategy.setStyle("-fx-text-fill: #aaa; -fx-font-size: 9px;");

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
                strategyCombo.valueProperty().addListener((o, oldV, newV) -> triggerChange());

                accessoryControlsList.add(new AccessoryRowControls(chk, info.name(), info.role(), countSpinner, strategyCombo));

                row.getChildren().addAll(chk, lblRole, lblCount, countSpinner, lblStrategy, strategyCombo);
                listVBox.getChildren().add(row);
            }
            accessoryBoxPane.getChildren().add(listVBox);
        }

        private String getRoleStyle(String role) {
            return switch (role) {
                case "PROIE" -> "-fx-text-fill: #22c55e; -fx-font-weight: bold; -fx-font-size: 9px;";
                case "PRÉDATEUR" -> "-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 9px;";
                case "COMMENSAL" -> "-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 9px;";
                case "PATHOGÈNE" -> "-fx-text-fill: #eab308; -fx-font-weight: bold; -fx-font-size: 9px;";
                default -> "-fx-text-fill: #94a3b8; -fx-font-size: 9px;";
            };
        }

        private String getShortSpeciesName(String full) {
            if (full == null) return "";
            int idx = full.indexOf("(");
            return idx > 0 ? full.substring(0, idx).trim() : full;
        }

        private void applyDemographicsFromStage(int stageIndex) {
            if (queenSpinner == null || workerSpinner == null || soldierSpinner == null || broodSpinner == null) return;
            switch (stageIndex) {
                case 0 -> { // Claustral Founding Cell
                    queenSpinner.getValueFactory().setValue(1);
                    workerSpinner.getValueFactory().setValue(0);
                    soldierSpinner.getValueFactory().setValue(0);
                    broodSpinner.getValueFactory().setValue(10);
                }
                case 1 -> { // Incipient / Young Nest
                    queenSpinner.getValueFactory().setValue(1);
                    workerSpinner.getValueFactory().setValue(100);
                    soldierSpinner.getValueFactory().setValue(10);
                    broodSpinner.getValueFactory().setValue(50);
                }
                case 3 -> { // Complex Supercolony Network
                    queenSpinner.getValueFactory().setValue(50);
                    workerSpinner.getValueFactory().setValue(500000);
                    soldierSpinner.getValueFactory().setValue(50000);
                    broodSpinner.getValueFactory().setValue(200000);
                }
                default -> { // Established / Mature Colony (Stage 2)
                    queenSpinner.getValueFactory().setValue(1);
                    workerSpinner.getValueFactory().setValue(5000);
                    soldierSpinner.getValueFactory().setValue(500);
                    broodSpinner.getValueFactory().setValue(2500);
                }
            }
        }

        public VBox getCardPane() { return cardPane; }
        public String getSpeciesName() { return speciesName; }
        public String getNestType() { return nestTypeCombo.getValue(); }
        public String getNestStage() { return nestStageCombo.getValue(); }
        public int getNestStageIndex() { return nestStageCombo.getSelectionModel().getSelectedIndex(); }
        public void setNestType(String nestType) {
            if (nestTypeCombo.getItems().contains(nestType)) {
                nestTypeCombo.getSelectionModel().select(nestType);
            } else if (nestType != null) {
                nestTypeCombo.getItems().add(nestType);
                nestTypeCombo.getSelectionModel().select(nestType);
            }
            triggerChange();
        }
        public void setNestStageIndex(int index) {
            if (index >= 0 && index < nestStageCombo.getItems().size()) {
                nestStageCombo.getSelectionModel().select(index);
                applyDemographicsFromStage(index);
            }
            triggerChange();
        }
        public int getQueenCount() { return queenSpinner.getValue(); }
        public void setQueenCount(int count) { queenSpinner.getValueFactory().setValue(count); triggerChange(); }
        public int getWorkerCount() { return workerSpinner.getValue(); }
        public void setWorkerCount(int count) { workerSpinner.getValueFactory().setValue(count); triggerChange(); }
        public int getSoldierCount() { return soldierSpinner.getValue(); }
        public void setSoldierCount(int count) { soldierSpinner.getValueFactory().setValue(count); triggerChange(); }
        public int getBroodCount() { return broodSpinner.getValue(); }
        public void setBroodCount(int count) { broodSpinner.getValueFactory().setValue(count); triggerChange(); }
        public ArchitectureType getWorkerEngine() { return workerEngineCombo.getValue(); }
        public ArchitectureType getSoldierEngine() { return soldierEngineCombo.getValue(); }
        public ArchitectureType getQueenEngine() { return queenEngineCombo.getValue(); }
        public int getInitialFood() { return initialFoodSpinner.getValue(); }
        public void setInitialFood(int food) { initialFoodSpinner.getValueFactory().setValue(food); triggerChange(); }

        public List<AccessoryConfigSnapshot> getAccessorySnapshots() {
            List<AccessoryConfigSnapshot> list = new ArrayList<>();
            for (AccessoryRowControls ctrl : accessoryControlsList) {
                list.add(new AccessoryConfigSnapshot(
                    ctrl.name,
                    ctrl.role,
                    ctrl.chk.isSelected(),
                    ctrl.countSpinner.getValue(),
                    ctrl.strategyCombo.getValue()
                ));
            }
            return list;
        }

        public void setAccessorySnapshots(List<AccessoryConfigSnapshot> snapshots) {
            if (snapshots == null) return;
            for (AccessoryConfigSnapshot sSnap : snapshots) {
                for (AccessoryRowControls ctrl : accessoryControlsList) {
                    if (ctrl.name.equalsIgnoreCase(sSnap.name())) {
                        ctrl.chk.setSelected(sSnap.enabled());
                        ctrl.countSpinner.getValueFactory().setValue(sSnap.initialCount());
                        if (ctrl.strategyCombo.getItems().contains(sSnap.renewalStrategy())) {
                            ctrl.strategyCombo.getSelectionModel().select(sSnap.renewalStrategy());
                        }
                    }
                }
            }
            triggerChange();
        }

        public boolean hasActivePreyAccessory() {
            for (AccessoryRowControls ctrl : accessoryControlsList) {
                if ("PROIE".equals(ctrl.role) && ctrl.chk.isSelected()) {
                    return true;
                }
            }
            return false;
        }

        public boolean hasActivePredatorAccessory() {
            for (AccessoryRowControls ctrl : accessoryControlsList) {
                if ("PRÉDATEUR".equals(ctrl.role) && ctrl.chk.isSelected()) {
                    return true;
                }
            }
            return false;
        }

        public void enableAccessorySpeciesRole(String role) {
            for (AccessoryRowControls ctrl : accessoryControlsList) {
                if (role.equals(ctrl.role)) {
                    ctrl.chk.setSelected(true);
                }
            }
            triggerChange();
        }

        public SpeciesConfigSnapshot getSnapshot() {
            return new SpeciesConfigSnapshot(
                speciesName,
                getNestType(),
                getQueenCount(),
                getWorkerCount(),
                getSoldierCount(),
                getBroodCount(),
                getInitialFood(),
                getWorkerEngine(),
                getSoldierEngine(),
                getQueenEngine(),
                getAccessorySnapshots()
            );
        }
    }

    private int estimateNestCapacity(String nestType) {
        if (nestType == null) return 350;
        String lower = nestType.toLowerCase();
        if (lower.contains("jeune") || lower.contains("young") || lower.contains("tige") || lower.contains("stem") || lower.contains("galle")) {
            return 150;
        } else if (lower.contains("mature") || lower.contains("carton") || lower.contains("bivouac") || lower.contains("pot") || lower.contains("cavité") || lower.contains("dôme")) {
            return 500;
        } else if (lower.contains("supercolonie") || lower.contains("supercolony") || lower.contains("champignon") || lower.contains("vault") || lower.contains("cathédrale")) {
            return 2500;
        }
        return 350;
    }

    public record AccessorySpeciesInfo(String name, String role, String description, int defaultCount) {
        public AccessorySpeciesInfo(String name, String role, String description) {
            this(name, role, description, 50);
        }
    }

    /**
     * Helper method to filter physical nest architectures based on biological species capability.
     * Guaranteed exact biological match returning pure physical nest typologies.
     */
    public static List<String> getCompatibleNestArchitectures(String speciesName) {
        List<String> nests = new ArrayList<>();
        nests.add("Subterranean Burrows & Galleries");

        if (speciesName != null) {
            String lower = speciesName.toLowerCase();
            if (lower.contains("atta") || lower.contains("coupeuse")) {
                nests.add("Subterranean Fungi Vault");
            } else if (lower.contains("apis") || lower.contains("abeille")) {
                nests.add("Hexagonal Wax Comb");
                nests.add("Wooden Beehive");
            } else if (lower.contains("bombus") || lower.contains("bourdon")) {
                nests.add("Wax & Propolis Pots");
            } else if (lower.contains("vespula") || lower.contains("guêpe")) {
                nests.add("Suspended Paper Nest");
            } else if (lower.contains("reticulitermes") || lower.contains("macrotermes") || lower.contains("termite")) {
                nests.add("Cathedral Termite Mound");
            } else if (lower.contains("crematogaster") || lower.contains("carton")) {
                nests.add("Carton Wooden Nest");
            } else if (lower.contains("temnothorax") || lower.contains("gall") || lower.contains("galle")) {
                nests.add("Hollow Stem & Gall Nest");
            } else if (lower.contains("camponotus") || lower.contains("carpenter") || lower.contains("charpentière")) {
                nests.add("Tree Trunk Cavity Nest");
            } else if (lower.contains("eciton") || lower.contains("army") || lower.contains("légionnaire")) {
                nests.add("Living Bivouac");
            } else if (lower.contains("oecophylla") || lower.contains("weaver") || lower.contains("tisserande")) {
                nests.add("Arboreal Silk Nest");
            } else if (lower.contains("formica") || lower.contains("wood ant") || lower.contains("rousse")) {
                nests.add("Solar Needle Mound");
            }
        }
        Collections.sort(nests);
        return nests;
    }

    public static void selectDefaultNestArchitectureForSpecies(String speciesName, ComboBox<String> combo) {
        List<String> list = getCompatibleNestArchitectures(speciesName);
        combo.getItems().setAll(list);
        if (speciesName != null) {
            String matched = null;
            for (String arch : list) {
                if (!arch.startsWith("Subterranean Burrows")) {
                    matched = arch;
                    break;
                }
            }
            if (matched != null) {
                combo.getSelectionModel().select(matched);
                return;
            }
        }
        if (!combo.getItems().isEmpty()) {
            combo.getSelectionModel().selectFirst();
        }
    }

    public static List<String> getCompatibleNestTypes(String speciesName) {
        return getCompatibleNestArchitectures(speciesName);
    }

    /**
     * Helper method to filter compatible accessory species (Proies, Prédateurs, Commensaux, Pathogènes).
     * Excludes neutral or biologically incompatible species.
     */
    public static List<AccessorySpeciesInfo> getCompatibleAccessorySpecies(String speciesName) {
        List<AccessorySpeciesInfo> list = new ArrayList<>();
        if (speciesName == null) return list;
        String lower = speciesName.toLowerCase();

        if (lower.contains("atta") || lower.contains("coupeuse")) {
            list.add(new AccessorySpeciesInfo("Seeded Grasses & Foliage", "COMMENSAL", "Foliage providing symbiotic plant biomass", 150));
            list.add(new AccessorySpeciesInfo("Humid Moss (Polytrichum)", "COMMENSAL", "Substrate maintaining humidity for fungus garden", 80));
            list.add(new AccessorySpeciesInfo("Entomopathogenic Fungus (Cordyceps)", "PATHOGEN", "Cordyceps targeting workers in forest environments", 20));
        } else if (lower.contains("messor") || lower.contains("pogonomyrmex") || lower.contains("moissonneuse")) {
            list.add(new AccessorySpeciesInfo("Seeded Grasses (Herbs & Biomass)", "PROIE", "Plant seeds for harvesting and granaries", 200));
            list.add(new AccessorySpeciesInfo("Mealworm Larvae (Protein Prey)", "PROIE", "Arthropod prey providing nitrogen to brood", 40));
            list.add(new AccessorySpeciesInfo("Antlion Pitfall Trap (Myrmeleon)", "PRÉDATEUR", "Funnel-web pitfall predator in sand", 10));
        } else if (lower.contains("apis") || lower.contains("abeille")) {
            list.add(new AccessorySpeciesInfo("Nectariferous Grasses & Flowers", "COMMENSAL", "Melliferous flowers supplying nectar and pollen", 120));
            list.add(new AccessorySpeciesInfo("Parasitic Mite (Varroa destructor)", "PATHOGEN", "Hematophagous parasitic mite targeting bee brood", 30));
            list.add(new AccessorySpeciesInfo("Hunting Spider / Robber Fly", "PRÉDATEUR", "Predators capturing foragers in flight", 15));
        } else if (lower.contains("reticulitermes") || lower.contains("macrotermes") || lower.contains("termite")) {
            list.add(new AccessorySpeciesInfo("Humid Moss & Woody Timber", "COMMENSAL", "Digestible cellulosic biomass", 100));
            list.add(new AccessorySpeciesInfo("Mealworm Larvae (Protein Prey)", "PROIE", "Residual soil arthropod prey", 30));
            list.add(new AccessorySpeciesInfo("Hunting Wasp / Subterranean Spider", "PRÉDATEUR", "Gallery specialist predators", 12));
        } else if (lower.contains("vespula") || lower.contains("guêpe")) {
            list.add(new AccessorySpeciesInfo("Mealworm Larvae & Caterpillars (Prey)", "PROIE", "Insect prey hunted for brood protein", 60));
            list.add(new AccessorySpeciesInfo("Grasses & Flowers (Nectar)", "COMMENSAL", "Sugary energy sources for adults", 80));
            list.add(new AccessorySpeciesInfo("Entomopathogenic Fungus (Cordyceps)", "PATHOGEN", "Spores carried during flight", 15));
        } else { // Lasius, Solenopsis, Formica, etc.
            list.add(new AccessorySpeciesInfo("Pine Aphids (Cinara / Honeydew)", "COMMENSAL", "Aphids farmed in trophobiosis for sugary honeydew", 80));
            list.add(new AccessorySpeciesInfo("Seeded Grasses (Herbs & Biomass)", "COMMENSAL", "Local vegetation providing substrate and shade", 100));
            list.add(new AccessorySpeciesInfo("Mealworm Larvae (Protein Prey)", "PROIE", "Arthropodes proies apportant les protéines", 50));
            list.add(new AccessorySpeciesInfo("Antlion Pitfall Trap (Myrmeleon)", "PRÉDATEUR", "Natural predator of patrolling workers", 15));
            list.add(new AccessorySpeciesInfo("Entomopathogenic Fungus (Cordyceps)", "PATHOGEN", "Mycelial parasite targeting colony", 10));
        }
        list.sort(Comparator.comparing(AccessorySpeciesInfo::name));
        return list;
    }

    private void handleAlignClimateWithWorld() {
        String world = comboWorld.getValue();
        if (world == null) world = "";
        String targetWeather = "Temperate";
        String wLower = world.toLowerCase();

        if (wLower.contains("désert") || wLower.contains("arid") || wLower.contains("erg") || wLower.contains("sahara")) {
            targetWeather = "Arid";
        } else if (wLower.contains("taïga") || wLower.contains("boreal") || wLower.contains("arctic") || wLower.contains("toundra") || wLower.contains("rovaniemi")) {
            targetWeather = "Arctic";
        } else if (wLower.contains("tropical") || wLower.contains("amazon") || wLower.contains("manaus") || wLower.contains("jungle")) {
            targetWeather = "Tropical";
        } else if (wLower.contains("méditerranée") || wLower.contains("marseille") || wLower.contains("mediterranean")) {
            targetWeather = "Mediterranean";
        }

        if (comboWeather.getItems().contains(targetWeather)) {
            comboWeather.getSelectionModel().select(targetWeather);
        }
    }

    public void pauseSimulation() {
        if (isPlaying) {
            isPlaying = false;
            isPaused = true;
            isStopped = false;
            updateButtonStates();
            if (onPause != null) onPause.accept(null);
        }
    }

    public int getBroodCount() {
        if (speciesCardList != null && !speciesCardList.isEmpty()) {
            return speciesCardList.get(0).getBroodCount();
        }
        return 20000;
    }

    public String getSelectedNestType() {
        if (speciesCardList != null && !speciesCardList.isEmpty() && speciesCardList.get(0).getNestType() != null) {
            return speciesCardList.get(0).getNestType();
        }
        return "Subterranean Burrows & Galleries";
    }

    private void updateStepDtLabel() {
        if (lblStepDt != null) {
            lblStepDt.setText(String.format("Δt = %.3f s", simulationStepSeconds));
        }
        updateDurationCalculatedLabel();
    }

    public void updateDurationCalculatedLabel() {
        if (lblDurationCalculatedInfo == null) return;
        if (durationUnitCombo != null && ("∞ Unlimited".equals(durationUnitCombo.getValue()) || "∞ Illimité".equals(durationUnitCombo.getValue()))) {
            lblDurationCalculatedInfo.setText(I18nManager.getInstance().get("sim.duration_unlimited"));
            return;
        }
        long totalTicks = getMaxSimulationTicks();
        double dt = getSimulationStepSeconds();
        if (dt <= 0) dt = 0.016666666666666666;

        String formatted = formatSimulationTime(totalTicks, dt);
        lblDurationCalculatedInfo.setText(String.format("⏱️ Equivalent Duration: %s (%,d steps at Δt = %.3fs)", formatted, totalTicks, dt));
    }
}
