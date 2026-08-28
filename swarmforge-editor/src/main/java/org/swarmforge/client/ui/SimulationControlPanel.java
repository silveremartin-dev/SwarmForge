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

    // Termination Limits
    private final Spinner<Integer> maxTicksSpinner = new Spinner<>(1000, 1_000_000, 100_000, 5000);
    private final Spinner<Integer> minPopStopSpinner = new Spinner<>(0, 1000, 0, 5);

    // 3. Multi-Species Scenario Config List
    private final ComboBox<String> comboAvailableSpecies = new ComboBox<>();
    private final VBox speciesListContainer = new VBox(10);
    private final List<SpeciesConfigCard> speciesCardList = new ArrayList<>();
    private volatile ScenarioSetupSnapshot lastSetupSnapshot;

    public record AccessoryConfigSnapshot(
        String name,
        String role,
        boolean enabled,
        int initialCount,
        String renewalStrategy
    ) {}

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

    public record ScenarioSetupSnapshot(
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
    ) {}

    public boolean isCreatingScenario() {
        return isCreatingScenario;
    }

    public ScenarioSetupSnapshot captureSetupSnapshotOnFXThread() {
        List<SpeciesConfigSnapshot> snapshots = new ArrayList<>();
        for (SpeciesConfigCard card : speciesCardList) {
            snapshots.add(card.getSnapshot());
        }
        return new ScenarioSetupSnapshot(
            getMasterSeed(),
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

        org.swarmforge.client.util.I18nManager i18n = I18nManager.getInstance();

        // 1. Top Standardized Header Bar
        VBox headerVBox = new VBox(6);
        headerVBox.setPadding(new Insets(8, 10, 5, 10));

        HBox headerRow = new HBox(8);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label lblPresetHeader = new Label();
        lblPresetHeader.textProperty().bind(i18n.createStringBinding("sim.preset_header"));
        lblPresetHeader.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 18px;");
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
        bImportScenario.setTooltip(new Tooltip("Importer un fichier JSON de scénario."));
        bImportScenario.setOnAction(e -> handleImportScenario());

        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        Label lblMeta = new Label("★ Preset Global de Scénario :");
        lblMeta.setStyle("-fx-text-fill: #a78bfa; -fx-font-weight: bold; -fx-font-size: 11px;");

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
        comboMeta.setTooltip(new Tooltip("Sélectionnez ou créez un preset global assemblant biotope, climat et faune."));
        HBox.setHgrow(comboMeta, Priority.ALWAYS);
        comboMeta.setOnAction(e -> applyMetaPreset(comboMeta.getValue()));
        if (!comboMeta.getItems().isEmpty()) {
            comboMeta.getSelectionModel().selectFirst();
            applyMetaPreset(comboMeta.getValue());
        }

        metaRow.getChildren().addAll(lblMeta, comboMeta, bSaveScenario, bDeleteScenario, bExportScenario, bImportScenario);

        VBox descBox = new VBox(4);
        Label lblDesc = new Label("📝 Description Scientifique du Scénario :");
        lblDesc.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px; -fx-font-weight: bold;");
        areaDescription.setPrefRowCount(2);
        areaDescription.setPromptText("Description et objectifs d'expérimentation du scénario...");
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

        // 1 & 2 & 3 & 4 Section: World, Weather, Start Date/Time & Physics/Seed Presets
        GridPane gridWorldWeather = new GridPane();
        gridWorldWeather.setHgap(10); gridWorldWeather.setVgap(8);

        Label lbl1World = new Label("1. 🌍 Preset de Monde (Biotope) :");
        lbl1World.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px;");
        lbl1World.setTooltip(new Tooltip("Type de biotope et de relief 3D chargé depuis WorldPresetManager."));

        Label lbl2Weather = new Label("2. 🌤️ Preset Météo & Climat :");
        lbl2Weather.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px;");
        lbl2Weather.setTooltip(new Tooltip("Profil climatique et saisonnier issu de WeatherPresetManager."));

        Label lbl3DateTime = new Label("3. 📅 Date/Heure de Départ :");
        lbl3DateTime.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px;");
        DatePicker startDatePicker = new DatePicker(startDateTime.toLocalDate());
        startDatePicker.setPrefWidth(125);
        startDatePicker.setStyle("-fx-font-size: 11px;");

        Spinner<Integer> startTimeHourSpinner = new Spinner<>(0, 23, startDateTime.getHour());
        startTimeHourSpinner.setPrefWidth(54);
        startTimeHourSpinner.setEditable(true);
        startTimeHourSpinner.setStyle("-fx-font-size: 10px;");

        Spinner<Integer> startTimeMinuteSpinner = new Spinner<>(0, 59, startDateTime.getMinute());
        startTimeMinuteSpinner.setPrefWidth(54);
        startTimeMinuteSpinner.setEditable(true);
        startTimeMinuteSpinner.setStyle("-fx-font-size: 10px;");

        Spinner<Integer> startTimeSecondSpinner = new Spinner<>(0, 59, startDateTime.getSecond());
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
        lblH.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px; -fx-font-weight: bold;");
        Label lblM = new Label("m");
        lblM.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px; -fx-font-weight: bold;");
        Label lblS = new Label("s");
        lblS.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px; -fx-font-weight: bold;");

        HBox startDateTimeBox = new HBox(4,
            startDatePicker,
            startTimeHourSpinner, lblH,
            startTimeMinuteSpinner, lblM,
            startTimeSecondSpinner, lblS
        );
        startDateTimeBox.setAlignment(Pos.CENTER_LEFT);

        Label lbl4Step = new Label("4. ⏱️ Pas Physique (Δt) & Graine (Seed) :");
        lbl4Step.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px;");
        lbl4Step.setTooltip(new Tooltip("Définit la fidélité d'intégration numérique Δt des équations physiques/éthologiques, le seed aléatoire et les seuils d'arrêt de simulation."));

        scenarioStepCombo.getItems().setAll(
            "16.6 ms (60 Hz - Fidélité Physique Max / Défaut)",
            "50 ms (20 Hz - Précision Standard)",
            "100 ms (10 Hz - Mode Rapide)",
            "1.0 s (Mode Macroscopique Écosystème)",
            "5.0 s (Mode Ultra Macroscopique)"
        );
        scenarioStepCombo.getSelectionModel().selectFirst();
        scenarioStepCombo.setPrefWidth(240);
        scenarioStepCombo.setStyle("-fx-font-size: 11px;");
        scenarioStepCombo.setTooltip(new Tooltip("Pas de calcul Δt figé et persisté dans le scénario (garantissant un déterminisme parfait basé sur le Seed)."));
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

        Button btnAlignWeather = new Button("🔄 Align");
        btnAlignWeather.setStyle("-fx-background-color: #334155; -fx-text-fill: #e2e8f0; -fx-font-size: 10px;");
        btnAlignWeather.setTooltip(new Tooltip("Aligner le climat sur le biotope sélectionné"));
        btnAlignWeather.setOnAction(e -> alignWeatherWithWorld(true));
        HBox weatherRow = new HBox(6, comboWeather, btnAlignWeather);

        // Seed & Limits Control Row (Integrated into Section 4)
        HBox seedAndLimitsRow = new HBox(10);
        seedAndLimitsRow.setAlignment(Pos.CENTER_LEFT);

        Label lblSeed = new Label("🎲 Seed :");
        lblSeed.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-font-size: 10px;");
        txtSeed.setPrefWidth(80);
        txtSeed.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");

        Button btnRandSeed = new Button("🎲 Nouveau");
        btnRandSeed.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-font-size: 10px;");
        btnRandSeed.setOnAction(e -> {
            txtSeed.setText(String.valueOf((long)(Math.random() * 900000 + 100000)));
            updateValidationPanel();
        });

        Label lblMaxTicks = new Label("⏱️ Durée Max :");
        lblMaxTicks.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px; -fx-font-weight: bold;");
        maxTicksSpinner.setPrefWidth(90);
        maxTicksSpinner.setEditable(true);
        maxTicksSpinner.valueProperty().addListener((o, oldV, newV) -> updateValidationPanel());

        Label lblMinPop = new Label("🛑 Arrêt Pop. Min :");
        lblMinPop.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px; -fx-font-weight: bold;");
        minPopStopSpinner.setPrefWidth(65);
        minPopStopSpinner.setEditable(true);
        minPopStopSpinner.valueProperty().addListener((o, oldV, newV) -> updateValidationPanel());

        seedAndLimitsRow.getChildren().addAll(lblSeed, txtSeed, btnRandSeed, new Separator(Orientation.VERTICAL), lblMaxTicks, maxTicksSpinner, new Separator(Orientation.VERTICAL), lblMinPop, minPopStopSpinner);

        Label lblDeterminismLegend = new Label("💡 Note : Pour une graine (Seed) et un pas de calcul (Δt) donnés, la simulation est entièrement déterministe et reproductible.");
        lblDeterminismLegend.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 9.5px; -fx-font-style: italic;");

        VBox physicsAndSeedBox = new VBox(6, scenarioStepCombo, seedAndLimitsRow, lblDeterminismLegend);

        gridWorldWeather.add(lbl1World, 0, 0); gridWorldWeather.add(comboWorld, 1, 0);
        gridWorldWeather.add(lbl2Weather, 0, 1); gridWorldWeather.add(weatherRow, 1, 1);
        gridWorldWeather.add(lbl3DateTime, 0, 2); gridWorldWeather.add(startDateTimeBox, 1, 2);
        gridWorldWeather.add(lbl4Step, 0, 3); gridWorldWeather.add(physicsAndSeedBox, 1, 3);

        // 5. Multi-Species Scenario Section Header & Controls
        VBox section3Container = new VBox(8);
        section3Container.getStyleClass().add("card-pane");

        HBox speciesAddBar = new HBox(8);
        speciesAddBar.setAlignment(Pos.CENTER_LEFT);

        Label lbl5SpeciesHeader = new Label("5. 🐜 Espèces & Écosystème du Scénario :");
        lbl5SpeciesHeader.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 12px;");

        java.util.List<String> sortedSpeciesNames = new java.util.ArrayList<>(speciesPresetManager.getPresetNames());
        java.util.Collections.sort(sortedSpeciesNames);
        comboAvailableSpecies.getItems().setAll(sortedSpeciesNames);
        if (!comboAvailableSpecies.getItems().isEmpty()) {
            comboAvailableSpecies.getSelectionModel().selectFirst();
        }
        comboAvailableSpecies.setPrefWidth(260);

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

        // 6. Scenario Validation & Warnings Panel
        validationPanel = buildValidationPanel();

        // 7. Checkpoints & TitledPanes
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
        inlineProgressBar.setStyle("-fx-accent: #38bdf8;");

        inlineProgressLabel = new Label("Initializing scenario...");
        inlineProgressLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 10px;");

        applyProgressBox = new HBox(8, inlineProgressBar, inlineProgressLabel);
        applyProgressBox.setAlignment(Pos.CENTER_LEFT);
        applyProgressBox.setVisible(false);
        applyProgressBox.setManaged(false);

        btnApplyPresets.setOnAction(e -> handleApplyScenario());

        scenarioCard.getChildren().addAll(metaRow, descBox, new Separator(), gridWorldWeather, new Separator(), section3Container, validationPanel, checkpointsPane, btnApplyPresets, applyProgressBox);

        updateValidationPanel();

        // Line 1: Date & Heure Row
        HBox dateTimeRow = new HBox(8);
        dateTimeRow.setAlignment(Pos.CENTER_LEFT);

        lblDateTime = new Label("📅 Date & Heure : 2026-03-20 08:00:00 (Jour 1)");
        lblDateTime.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px;");
        lblDateTime.setTooltip(new Tooltip("Horodateur et cycle nycthéméral simulé (date, heure et jour)."));

        dateTimeRow.getChildren().add(lblDateTime);

        // Line 2: Playback Controls
        HBox playbackRow1 = new HBox(4);
        playbackRow1.setAlignment(Pos.CENTER);

        btnGoToBeginning = createIconButton(Feather.SKIP_BACK, "Début de la simulation (Retour au pas #0)");
        btnRewind = createIconButton(Feather.REWIND, "Retour arrière (-100 pas)");
        btnStepBack = createIconButton(Feather.CHEVRON_LEFT, "Pas arrière (-1 pas)");
        btnPlay = createIconButton(Feather.PLAY, "Démarrer / Reprendre la simulation");
        btnPause = createIconButton(Feather.PAUSE, "Mettre la simulation en pause");
        btnStepForward = createIconButton(Feather.CHEVRON_RIGHT, "Pas avant (+1 pas)");
        btnFastForward = createIconButton(Feather.FAST_FORWARD, "Avance rapide (+100 pas)");
        btnGoToEnd = createIconButton(Feather.SKIP_FORWARD, "Sauter à la fin de la simulation (Dernier pas enregistré)");

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

        Label lblSpeedLabel = new Label("🚀 Vitesse :");
        lblSpeedLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px; -fx-font-weight: bold;");

        lblSpeed = new Label("1.0x");
        lblSpeed.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px;");
        lblSpeed.setPrefWidth(55);

        speedSlider = new Slider(0.1, 20.0, 1.0);
        speedSlider.setShowTickLabels(false);
        speedSlider.setShowTickMarks(false);
        speedSlider.setPrefWidth(120);
        speedSlider.setTooltip(new Tooltip("Ajustez le facteur d'accélération temporelle de la simulation (0.1x à 20x)."));

        Button btnMaxSpeed = new Button("🚀 MAX");
        btnMaxSpeed.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand;");
        btnMaxSpeed.setTooltip(new Tooltip("Activer la vitesse maximale d'exécution (Maximum CPU throughput)"));

        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!"MAX".equals(btnMaxSpeed.getUserData())) {
                currentSpeed = newVal.floatValue();
                lblSpeed.setText(String.format("%.1fx", currentSpeed));
                if (onSpeedChange != null) onSpeedChange.accept(currentSpeed);
            }
        });

        btnMaxSpeed.setOnAction(e -> {
            if ("MAX".equals(btnMaxSpeed.getUserData())) {
                btnMaxSpeed.setUserData(null);
                btnMaxSpeed.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand;");
                currentSpeed = (float) speedSlider.getValue();
                lblSpeed.setText(String.format("%.1fx", currentSpeed));
            } else {
                btnMaxSpeed.setUserData("MAX");
                btnMaxSpeed.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: #fef08a; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand; -fx-effect: innershadow(three-pass-box, #d946ef, 5, 0, 0, 0);");
                currentSpeed = 1000.0f;
                lblSpeed.setText("MAX 🚀");
            }
            if (onSpeedChange != null) onSpeedChange.accept(currentSpeed);
        });

        speedSliderRow.getChildren().addAll(lblSpeedLabel, speedSlider, lblSpeed, btnMaxSpeed);

        playbackAndSpeedPanel.setPadding(new Insets(8));
        playbackAndSpeedPanel.getStyleClass().add("card-pane");

        Label lblPlaybackHeader = new Label("⏱️ Contrôles Temps, Vitesse & Lecture");
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
            btnApplyPresets.setText("⚡ APPLIQUER ET CRÉER LA SIMULATION");
            btnApplyPresets.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 10 16; -fx-background-radius: 5;");
            btnApplyPresets.setTooltip(new Tooltip("Réinitialise la simulation en appliquant le scénario multi-espèces, les nids filtrés, l'écosystème et la graine aléatoire."));
        }
        if (inlineProgressBar != null) {
            inlineProgressBar.setProgress(0);
            inlineProgressLabel.setText("❌ Calculs et création du scénario annulés par l'utilisateur.");
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
                "Une simulation est actuellement en cours d'exécution (Tick: " + currentTick + ").\n\n" +
                "Voulez-vous vraiment réinitialiser et remplacer la simulation active par cette nouvelle configuration de scénario ?"
            );
            confirmAlert.setTitle("Réinitialiser la Simulation");
            confirmAlert.setHeaderText("Confirmation requise");
            java.util.Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        }

        // Note: Pop-up dialog removed per design request. All scenario parameter warnings & auto-fix tools
        // are dynamically evaluated and displayed in the Validation Panel right above Checkpoints.

        isCreatingScenario = true;
        if (btnApplyPresets != null) {
            btnApplyPresets.setText("❌ ANNULER LE CALCUL EN COURS");
            btnApplyPresets.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 10 16; -fx-background-radius: 5; -fx-cursor: hand;");
            btnApplyPresets.setTooltip(new Tooltip("Cliquer pour interrompre la création du scénario et libérer la mémoire."));
        }

        if (isPlaying) {
            isPlaying = false;
            updateButtonStates();
            if (onPause != null) onPause.accept(null);
        }
        long seed = getMasterSeed();
        String selectedWorld = getSelectedWorld();
        String selectedWeather = getSelectedWeather();
        String scenarioName = comboMeta.getValue() != null ? comboMeta.getValue() : "Nouveau Scénario";
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
                activeCreationFuture.thenRun(() -> javafx.application.Platform.runLater(() -> {
                    if (!isCreatingScenario) return;
                    isCreatingScenario = false;
                    if (btnApplyPresets != null) {
                        btnApplyPresets.setText("⚡ APPLIQUER ET CRÉER LA SIMULATION");
                        btnApplyPresets.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 10 16; -fx-background-radius: 5;");
                        btnApplyPresets.setTooltip(new Tooltip("Réinitialise la simulation en appliquant le scénario multi-espèces, les nids filtrés, l'écosystème et la graine aléatoire."));
                    }

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
                "Climat aligné avec succès sur '" + targetWeather + "' selon le biotope de '" + world + "' !",
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
                areaDescription.setText("🔬 SCÉNARIO ACADÉMIQUE / PROFIL : " + scenario.getTitle() + "\n\n" + scenario.getDescription());
            }
            if (txtSeed != null) {
                txtSeed.setText(String.valueOf(scenario.getMasterSeed()));
            }
            if (maxTicksSpinner != null && scenario.getMaxSimulationTicks() > 0) {
                maxTicksSpinner.getValueFactory().setValue((int) Math.min(Integer.MAX_VALUE, scenario.getMaxSimulationTicks()));
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
        fontIcon.setIconColor(javafx.scene.paint.Color.web("#f8fafc"));

        Button btn = new Button();
        btn.setGraphic(fontIcon);
        btn.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 4; -fx-min-width: 32px; -fx-min-height: 28px; -fx-padding: 3px 6px; -fx-cursor: hand; -fx-border-color: #334155; -fx-border-radius: 4;");
        if (tooltip != null && !tooltip.isEmpty()) btn.setTooltip(new Tooltip(tooltip));
        btn.disabledProperty().addListener((obs, oldV, newV) -> btn.setOpacity(newV ? 0.35 : 1.0));
        return btn;
    }

    private Button createButton(String text, String tooltip) {
        Button btn = new Button(text);
        btn.setStyle("-fx-font-size: 12px; -fx-font-family: 'Segoe UI Emoji', 'Segoe UI Symbol', sans-serif; -fx-background-color: #1e293b; -fx-text-fill: #f8fafc; " +
                "-fx-background-radius: 4; -fx-min-width: 29px; -fx-min-height: 28px; -fx-padding: 2px 4px; -fx-cursor: hand; -fx-border-color: #334155; -fx-border-radius: 4;");
        if (tooltip != null && !tooltip.isEmpty()) btn.setTooltip(new Tooltip(tooltip));
        btn.disabledProperty().addListener((obs, oldV, newV) -> btn.setOpacity(newV ? 0.35 : 1.0));
        return btn;
    }

    private Button createSmallButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-font-size: 10px; -fx-background-color: #334155; -fx-text-fill: #e2e8f0; -fx-background-radius: 3; -fx-padding: 3 8; -fx-cursor: hand;");
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
            } else if (isPaused) {
                btnPlay.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 4; -fx-min-width: 32px; -fx-min-height: 28px; -fx-padding: 3px 6px; -fx-cursor: hand; -fx-border-color: #334155; -fx-border-radius: 4;");
                btnPause.setStyle("-fx-background-color: #d97706; -fx-background-radius: 4; -fx-min-width: 32px; -fx-min-height: 28px; -fx-padding: 3px 6px; -fx-cursor: hand; -fx-border-color: #f59e0b; -fx-border-width: 2px; -fx-border-radius: 4;");
                if (playIcon != null) playIcon.setIconColor(javafx.scene.paint.Color.web("#94a3b8"));
                if (pauseIcon != null) pauseIcon.setIconColor(javafx.scene.paint.Color.web("#ffffff"));
            } else {
                btnPlay.setStyle("-fx-background-color: #0284c7; -fx-background-radius: 4; -fx-min-width: 32px; -fx-min-height: 28px; -fx-padding: 3px 6px; -fx-cursor: hand; -fx-border-color: #38bdf8; -fx-border-radius: 4;");
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
        long hours = wholeSec / 3600;
        long minutes = (wholeSec % 3600) / 60;
        long seconds = wholeSec % 60;
        long cs = (long) Math.round((totalSeconds - Math.floor(totalSeconds)) * 100);
        if (cs >= 100) cs = 99;
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
        return maxTicksSpinner != null && maxTicksSpinner.getValue() != null ? maxTicksSpinner.getValue().longValue() : 100_000L;
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
                "Description obligatoire : Veuillez saisir une description scientifique dans la zone 'Description Scientifique du Scénario' avant d'enregistrer le preset."
            ).show();
            return;
        }

        String name = comboMeta.getValue() != null ? comboMeta.getValue() : "Nouveau Scénario";
        TextInputDialog dialog = org.swarmforge.client.util.ThemeManager.createTextInputDialog(name);
        dialog.setTitle("Enregistrer le Preset Scénario");
        dialog.setHeaderText("Nom du preset de scénario (Description capturée) :");
        dialog.showAndWait().ifPresent(scenarioName -> {
            if (!scenarioName.trim().isEmpty()) {
                String clean = scenarioName.trim();
                if (!comboMeta.getItems().contains(clean)) comboMeta.getItems().add(clean);
                comboMeta.getSelectionModel().select(clean);
                org.swarmforge.client.util.ThemeManager.createAlert(
                    Alert.AlertType.INFORMATION,
                    "Preset scénario avec description enregistré : " + clean
                ).show();
            }
        });
    }

    private void handleDeleteScenario() {
        String selected = comboMeta.getValue();
        if (selected == null || selected.isEmpty()) return;
        Alert alert = org.swarmforge.client.util.ThemeManager.createAlert(
            Alert.AlertType.CONFIRMATION,
            "Voulez-vous vraiment supprimer le preset scénario '" + selected + "' ?"
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
        chooser.setTitle("Exporter la configuration du scénario");
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
                    "Scénario exporté avec succès sous " + file.getName(),
                    org.swarmforge.client.util.NotificationOverlay.NotificationType.SUCCESS
                );
            } catch (Exception ex) {
                org.swarmforge.client.util.ThemeManager.createAlert(
                    Alert.AlertType.ERROR,
                    "Erreur lors de l'exportation du scénario : " + ex.getMessage()
                ).show();
            }
        }
    }

    private void handleImportScenario() {
        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Importer une configuration de scénario");
        chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("JSON Files", "*.json"));
        java.io.File file = chooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                ScenarioSetupSnapshot snapshot = mapper.readValue(file, ScenarioSetupSnapshot.class);
                if (snapshot != null) {
                    txtSeed.setText(String.valueOf(snapshot.seed()));
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
                    updateValidationPanel();
                    org.swarmforge.client.util.NotificationOverlay.show(
                        this,
                        "Scénario importé avec succès depuis " + file.getName(),
                        org.swarmforge.client.util.NotificationOverlay.NotificationType.SUCCESS
                    );
                }
            } catch (Exception ex) {
                org.swarmforge.client.util.ThemeManager.createAlert(
                    Alert.AlertType.ERROR,
                    "Erreur lors de l'importation du scénario : " + ex.getMessage()
                ).show();
            }
        }
    }

    private VBox buildCheckpointsPane() {
        VBox box = new VBox(6);
        box.getStyleClass().add("card-pane");
        box.setStyle("-fx-border-color: #d97706; -fx-border-width: 1;");

        Label lblTitle = new Label("7. 🔖 Points de Contrôle & Journal d'Interventions Mode Divin :");
        lblTitle.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-font-size: 11px;");

        comboCheckpoints.setPrefWidth(260);
        comboCheckpoints.setPromptText("Aucun point de contrôle enregistré");

        Button bRestore = new Button("⏪ Restaurer Checkpoint");
        bRestore.setStyle("-fx-background-color: #d97706; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-cursor: hand;");
        bRestore.setTooltip(new Tooltip("Restaure la simulation au pas de temps exact du checkpoint."));
        bRestore.setOnAction(e -> {
            org.swarmforge.core.simulation.SimulationCheckpoint sel = comboCheckpoints.getValue();
            if (sel != null && onRestoreCheckpoint != null) {
                onRestoreCheckpoint.accept(sel);
            }
        });

        HBox row = new HBox(8, new Label("Points de contrôle :") {{ setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 10px;"); }}, comboCheckpoints, bRestore);
        row.setAlignment(Pos.CENTER_LEFT);

        Label lblInfo = new Label("💡 Chaque point de contrôle sauvegarde l'état physique exact et le journal d'interventions Mode Divin.");
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
                String borderCol = "HIGH".equals(warn.severity()) ? "#ef4444" : ("MEDIUM".equals(warn.severity()) ? "#f59e0b" : "#38bdf8");
                warnRow.setStyle("-fx-background-color: rgba(30, 41, 59, 0.7); -fx-border-color: " + borderCol + "; -fx-border-radius: 4px; -fx-padding: 6 10;");

                Label lblMsg = new Label(warn.message());
                lblMsg.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 10.5px; -fx-wrap-text: true;");
                lblMsg.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(lblMsg, Priority.ALWAYS);

                Button btnFix = new Button(warn.fixButtonText());
                btnFix.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-cursor: hand; -fx-padding: 3 8; -fx-background-radius: 3;");
                btnFix.setTooltip(new Tooltip("Cliquer pour corriger automatiquement ce paramètre du scénario"));
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
                "🔴 Aucune espèce d'insecte configurée. La simulation démarrera dans un terrarium sans colonie.",
                "➕ Ajouter (Lasius niger)",
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
                        "👑 Ajouter 1 Reine",
                        () -> card.setQueenCount(1)
                    ));
                }

                if (card.getQueenCount() > 0 && card.getBroodCount() <= 0) {
                    list.add(new ScenarioWarning(
                        "DÉMOGRAPHIE", "MEDIUM",
                        String.format("🟠 Colonie '%s' : Reine présente mais couvain nul (0 couvain). Renouvellement de la colonie compromis.", card.getSpeciesName()),
                        "🥚 Ajouter 500 Couvain",
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
                        "🏰 Agrandir le Nid",
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
                        "🔄 Aligner sur Nid Adapté",
                        () -> card.setNestType(compats.get(0))
                    ));
                }

                // Initial food reserve check
                if (card.getInitialFood() <= 0) {
                    list.add(new ScenarioWarning(
                        "RESSOURCES", "INFO",
                        String.format("💡 Colonie '%s' : Réserve initiale de nourriture nulle (0 unités). Risque de famine rapide.", card.getSpeciesName()),
                        "🍖 Allouer 500 Nourriture",
                        () -> card.setInitialFood(500)
                    ));
                }
            }

            if (totalAnts == 0) {
                list.add(new ScenarioWarning(
                    "ESPÈCES", "HIGH",
                    "🔴 Effectif total d'insectes nul (0 ouvrières, 0 reines).",
                    "➕ Ajouter 500 Ouvrières",
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
                    "🌤️ Aligner Climat",
                    () -> alignWeatherWithWorld()
                ));
            } else if (isPolarWorld && (weatherLower.contains("arid") || weatherLower.contains("tropical"))) {
                list.add(new ScenarioWarning(
                    "CLIMAT", "HIGH",
                    String.format("🔴 Incompatibilité Climat/Biotope : Biotope nordique/alpin ('%s') associé à un climat aride/tropical ('%s').", world, weather),
                    "🌤️ Aligner Climat",
                    () -> alignWeatherWithWorld()
                ));
            } else if (isTropicalWorld && weatherLower.contains("arctic")) {
                list.add(new ScenarioWarning(
                    "CLIMAT", "HIGH",
                    String.format("🔴 Incompatibilité Climat/Biotope : Jungle tropicale ('%s') sous un climat arctique ('%s').", world, weather),
                    "🌤️ Aligner Climat",
                    () -> alignWeatherWithWorld()
                ));
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
                "⏱️ Régler Δt à 50 ms",
                () -> {
                    scenarioStepCombo.getSelectionModel().select(1);
                    simulationStepSeconds = 0.05f;
                    updateStepDtLabel();
                }
            ));
        }

        // 4. Durée Max (Ticks)
        if (maxTicksSpinner.getValue() != null && maxTicksSpinner.getValue() < 1000) {
            list.add(new ScenarioWarning(
                "DURÉE", "INFO",
                String.format("💡 Durée maximale très courte (%,d ticks). La simulation risque de s'arrêter avant l'émergence des comportements.", maxTicksSpinner.getValue()),
                "⏱️ Régler à 100 000 Ticks",
                () -> maxTicksSpinner.getValueFactory().setValue(100000)
            ));
        }

        // 5. Seuil Arrêt Population Min
        if (minPopStopSpinner.getValue() != null && minPopStopSpinner.getValue() > 0 && minPopStopSpinner.getValue() >= grandTotalAnts && grandTotalAnts > 0) {
            list.add(new ScenarioWarning(
                "ARRÊT", "MEDIUM",
                String.format("🟠 Seuil d'arrêt de population (%,d ind.) >= population initiale (%,d ind.). La simulation s'arrêtera immédiatement.", minPopStopSpinner.getValue(), grandTotalAnts),
                "🛑 Désactiver le Seuil (0)",
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
                "🎲 Nouveau Seed",
                () -> txtSeed.setText(String.valueOf((long)(Math.random() * 900000 + 100000)))
            ));
        }

        // 7. Dynamic Trophic & Food Web Checks (Predator vs Prey availability)
        for (SpeciesConfigCard card : speciesCardList) {
            if (card.hasActivePredatorAccessory() && !card.hasActivePreyAccessory() && card.getInitialFood() <= 0) {
                list.add(new ScenarioWarning(
                    "ÉCOSYSTÈME / TROPHIQUE", "MEDIUM",
                    String.format("🟠 Incompatibilité Trophique : L'écosystème de '%s' comporte des prédateurs sans proies d'appoint ni réserve initiale de nourriture.", card.getSpeciesName()),
                    "🦗 Activer Proies (Arthropodes / Ténébrions)",
                    () -> {
                        card.enableAccessorySpeciesRole("PROIE");
                        card.setInitialFood(500);
                    }
                ));
            }
        }

        return list;
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
            lblTitle.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 12px;");

            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            Button btnRemove = new Button("🗑️ Remove Species");
            btnRemove.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-cursor: hand;");
            btnRemove.setOnAction(e -> onRemove.run());

            header.getChildren().addAll(lblTitle, sp, btnRemove);

            // 1. Filtered Nest, Placement & Inter-Nest Strategy
            HBox nestRow = new HBox(8);
            nestRow.setAlignment(Pos.CENTER_LEFT);

            Label lblNest = new Label("🏰 Nest Type (Filtered for " + getShortSpeciesName(speciesName) + ") :");
            lblNest.setStyle("-fx-text-fill: #a78bfa; -fx-font-weight: bold; -fx-font-size: 10px;");

            nestTypeCombo.getItems().setAll(getCompatibleNestTypes(speciesName));
            if (!nestTypeCombo.getItems().isEmpty()) nestTypeCombo.getSelectionModel().selectFirst();
            nestTypeCombo.setTooltip(new Tooltip("Nest architectures suited to the ecological needs and capabilities of this species. The nest is pre-generated from these parameters."));

            Label lblPlacement = new Label("📍 Placement :");
            lblPlacement.setStyle("-fx-text-fill: #a78bfa; -fx-font-weight: bold; -fx-font-size: 10px;");

            nestPlacementCombo.getItems().addAll(
                "📍 Center of Map (Optimal Unflooded Zone)",
                "✋ Manual Placement (Coordinates X, Z)",
                "👑 Queen Foundation (Virgin Surface Soil)",
                "🎲 Random Position (Dispersed Across Map)"
            );
            nestPlacementCombo.getSelectionModel().selectFirst();
            nestPlacementCombo.setTooltip(new Tooltip("Spatial placement strategy of the nest in the 3D grid."));

            Label lblInitialFood = new Label("🍖 Food :");
            lblInitialFood.setStyle("-fx-text-fill: #a78bfa; -fx-font-weight: bold; -fx-font-size: 10px;");

            initialFoodSpinner.setPrefWidth(75);
            initialFoodSpinner.setEditable(true);
            initialFoodSpinner.setTooltip(new Tooltip("Initial food resource reserve allocated to the nest."));

            nestRow.getChildren().addAll(lblNest, nestTypeCombo, lblPlacement, nestPlacementCombo, lblInitialFood, initialFoodSpinner);

            // Manual Position Box (Visible when manual placement is chosen)
            HBox manualPosBox = new HBox(8);
            manualPosBox.setAlignment(Pos.CENTER_LEFT);
            manualPosBox.setStyle("-fx-padding: 4 0 0 0;");

            Label lblManualX = new Label("X Coordinate :");
            lblManualX.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 10px;");
            posXSpinner.setPrefWidth(85); posXSpinner.setEditable(true);

            Label lblManualZ = new Label("Z Coordinate :");
            lblManualZ.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 10px;");
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
            lblRelation.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-font-size: 10px;");

            nestRelationCombo.getItems().addAll(
                "⚔️ Monocolonial Competition (Inter-nest Warfare - Cuticular Hydrocarbons)",
                "🤝 Supercolony Unicolonial (Tolerance, Mutual Cooperation & Shared Workers/Brood)",
                "🛡️ Territorial Neutrality (Passive Avoidance without Direct Combat)"
            );
            nestRelationCombo.getSelectionModel().selectFirst();
            nestRelationCombo.setPrefWidth(420);
            nestRelationCombo.setTooltip(new Tooltip("Configures behavioral interactions if multiple nests of the same species are instantiated in the simulation."));

            relationRow.getChildren().addAll(lblRelation, nestRelationCombo);

            CheckBox chkSupercolonyMember = new CheckBox("🤝 Join Polycalic Supercolony Network (Cooperation, free passage & brood sharing among allied nests)");
            chkSupercolonyMember.setSelected(false);
            chkSupercolonyMember.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 10px; -fx-font-weight: bold;");
            chkSupercolonyMember.setTooltip(new Tooltip("Check to integrate this colony into the cooperative supercolony network. Unchecked nests remain independent colonies in competition or warfare."));

            relationBox.getChildren().addAll(relationRow, chkSupercolonyMember);

            // 2. Demographics & AI Engines Standard Block (Unified AI engine by default across castes)
            VBox demoBox = new VBox(6);
            demoBox.getStyleClass().add("card-pane");

            Label lblDemoTitle = new Label("🧠 Demographics & Castes (Queens, Workers, Soldiers & Unified AI Engine)");
            lblDemoTitle.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px;");

            Label lblSpatialInfo = new Label("ℹ️ Demographics & AI Note: Population sizes (Queens, Workers, Soldiers) are calculated automatically based on the selected nest size, customizable via counters.");
            lblSpatialInfo.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 9.5px; -fx-wrap-text: true;");

            GridPane demoGrid = new GridPane();
            demoGrid.setHgap(8); demoGrid.setVgap(4);

            for (ArchitectureType type : ArchitectureType.values()) {
                workerEngineCombo.getItems().add(type);
                soldierEngineCombo.getItems().add(type);
                queenEngineCombo.getItems().add(type);
            }
            // Unified AI Engine across castes by default
            workerEngineCombo.getSelectionModel().select(ArchitectureType.BEHAVIOR_TREE);
            soldierEngineCombo.getSelectionModel().select(ArchitectureType.BEHAVIOR_TREE);
            queenEngineCombo.getSelectionModel().select(ArchitectureType.BEHAVIOR_TREE);

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
            workerSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5000000, 50000, 1000));
            soldierSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 500000, 5000, 500));
            queenSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 5, 1));
            broodSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 2000000, 20000, 1000));

            // Auto-adjust demographics when nest architecture type changes
            nestTypeCombo.valueProperty().addListener((o, oldV, newV) -> {
                applyDemographicsFromNest(newV);
                triggerChange();
            });

            queenSpinner.valueProperty().addListener((o, oldV, newV) -> triggerChange());
            workerSpinner.valueProperty().addListener((o, oldV, newV) -> triggerChange());
            soldierSpinner.valueProperty().addListener((o, oldV, newV) -> triggerChange());
            broodSpinner.valueProperty().addListener((o, oldV, newV) -> triggerChange());
            initialFoodSpinner.valueProperty().addListener((o, oldV, newV) -> triggerChange());

            // Trigger initial population calculation to match selected nest preset
            applyDemographicsFromNest(nestTypeCombo.getValue());

            Label lblQ = new Label("👑 Reines :"); lblQ.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 10px;");
            Label lblQEngine = new Label("Moteur IA :"); lblQEngine.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;");
            demoGrid.add(lblQ, 0, 0); demoGrid.add(queenSpinner, 1, 0); demoGrid.add(lblQEngine, 2, 0); demoGrid.add(queenEngineCombo, 3, 0);

            Label lblW = new Label("🐜 Ouvrières :"); lblW.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 10px;");
            Label lblWEngine = new Label("Moteur IA :"); lblWEngine.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;");
            demoGrid.add(lblW, 0, 1); demoGrid.add(workerSpinner, 1, 1); demoGrid.add(lblWEngine, 2, 1); demoGrid.add(workerEngineCombo, 3, 1);

            Label lblS = new Label("⚔️ Soldats :"); lblS.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 10px;");
            Label lblSEngine = new Label("Moteur IA :"); lblSEngine.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;");
            demoGrid.add(lblS, 0, 2); demoGrid.add(soldierSpinner, 1, 2); demoGrid.add(lblSEngine, 2, 2); demoGrid.add(soldierEngineCombo, 3, 2);

            Label lblB = new Label("🥚 Couvain :"); lblB.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 10px;");
            demoGrid.add(lblB, 0, 3); demoGrid.add(broodSpinner, 1, 3);

            demoBox.getChildren().addAll(lblDemoTitle, lblSpatialInfo, demoGrid);

            // 3. Filtered Accessory Species Section (Proies, Prédateurs & Commensaux)
            Label lblAccessoryTitle = new Label("🦗 Espèces Accessoires & Interprétations Écologiques (Filtrées pour " + getShortSpeciesName(speciesName) + ") :");
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

                Label lblCount = new Label("Effectif Initial :");
                lblCount.setStyle("-fx-text-fill: #aaa; -fx-font-size: 9px;");

                Spinner<Integer> countSpinner = new Spinner<>(5, 1000, info.defaultCount(), 10);
                countSpinner.setPrefWidth(70); countSpinner.setEditable(true);
                countSpinner.valueProperty().addListener((o, oldV, newV) -> triggerChange());

                Label lblStrategy = new Label("Taux Renouvellement :");
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
                strategyCombo.setTooltip(new Tooltip("Stratégie de proposition de renouvellement pour maintenir la pression sans déstabiliser la simulation."));
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

        private void applyDemographicsFromNest(String nestType) {
            if (nestType == null || queenSpinner == null || workerSpinner == null || soldierSpinner == null || broodSpinner == null) return;
            String lower = nestType.toLowerCase();
            if (lower.contains("jeune") || lower.contains("young") || lower.contains("joven") || lower.contains("jung") || lower.contains("幼") ||
                lower.contains("tige") || lower.contains("stem") || lower.contains("tallo") || lower.contains("stängel") || lower.contains("茎") ||
                lower.contains("galle") || lower.contains("gall") || lower.contains("agalla") || lower.contains("瘿") || lower.contains("初")) {
                queenSpinner.getValueFactory().setValue(1);
                workerSpinner.getValueFactory().setValue(100);
                soldierSpinner.getValueFactory().setValue(0);
                broodSpinner.getValueFactory().setValue(50);
            } 
            else if (lower.contains("mature") || lower.contains("maduro") || lower.contains("reif") || lower.contains("成熟") ||
                     lower.contains("carton") || lower.contains("cartón") || lower.contains("karton") || lower.contains("纸") ||
                     lower.contains("bivouac") || lower.contains("biwak") || lower.contains("露营") ||
                     lower.contains("pot") || lower.contains("vaseta") || lower.contains("topf") || lower.contains("壶")) {
                queenSpinner.getValueFactory().setValue(1);
                workerSpinner.getValueFactory().setValue(5000);
                soldierSpinner.getValueFactory().setValue(500);
                broodSpinner.getValueFactory().setValue(2500);
            } 
            else if (lower.contains("supercolonie") || lower.contains("supercolony") || lower.contains("supercolonia") || lower.contains("superkolonie") || lower.contains("超级") ||
                     lower.contains("champignon") || lower.contains("fungus") || lower.contains("hongo") || lower.contains("pilz") || lower.contains("真菌") ||
                     lower.contains("mégapole") || lower.contains("megacity") || lower.contains("megaciudad") || lower.contains("megastadt") || lower.contains("巨城") ||
                     lower.contains("voûte") || lower.contains("vault") || lower.contains("bóveda") || lower.contains("gewölbe") || lower.contains("拱")) {
                queenSpinner.getValueFactory().setValue(50);
                workerSpinner.getValueFactory().setValue(500000);
                soldierSpinner.getValueFactory().setValue(50000);
                broodSpinner.getValueFactory().setValue(200000);
            } 
            else {
                queenSpinner.getValueFactory().setValue(5);
                workerSpinner.getValueFactory().setValue(50000);
                soldierSpinner.getValueFactory().setValue(5000);
                broodSpinner.getValueFactory().setValue(20000);
            }
        }

        public VBox getCardPane() { return cardPane; }
        public String getSpeciesName() { return speciesName; }
        public String getNestType() { return nestTypeCombo.getValue(); }
        public void setNestType(String nestType) {
            if (nestTypeCombo.getItems().contains(nestType)) {
                nestTypeCombo.getSelectionModel().select(nestType);
            } else if (nestType != null) {
                nestTypeCombo.getItems().add(nestType);
                nestTypeCombo.getSelectionModel().select(nestType);
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
     * Helper method to filter nest types based on biological species capability.
     * Guaranteed exact biological match without invalid cross-species fallbacks.
     */
    public static List<String> getCompatibleNestTypes(String speciesName) {
        List<String> nests = new ArrayList<>();
        nests.add("Young Subterranean Burrow");
        nests.add("Mature Subterranean Galleries");
        nests.add("Old Expansive Nest");
        nests.add("Complex Supercolony");

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

    /**
     * Helper method to filter compatible accessory species (Proies, Prédateurs, Commensaux, Pathogènes).
     * Excludes neutral or biologically incompatible species.
     */
    public static List<AccessorySpeciesInfo> getCompatibleAccessorySpecies(String speciesName) {
        List<AccessorySpeciesInfo> list = new ArrayList<>();
        if (speciesName == null) return list;
        String lower = speciesName.toLowerCase();

        if (lower.contains("atta") || lower.contains("coupeuse")) {
            list.add(new AccessorySpeciesInfo("Graminées à Graines & Feuillage", "COMMENSAL", "Feuillage apportant la biomasse végétale symbiotique", 150));
            list.add(new AccessorySpeciesInfo("Mousse Humide (Polytrichum)", "COMMENSAL", "Substrat maintenant l'hygrométrie du nid champignonniste", 80));
            list.add(new AccessorySpeciesInfo("Champignon Entomopathogène (Cordyceps)", "PATHOGEN", "Cordyceps ciblant les ouvrières en zone forestière", 20));
        } else if (lower.contains("messor") || lower.contains("pogonomyrmex") || lower.contains("moissonneuse")) {
            list.add(new AccessorySpeciesInfo("Graminées à Graines (Herbes & Biomasse)", "PROIE", "Graines végétales pour la récolte et les greniers", 200));
            list.add(new AccessorySpeciesInfo("Larves de Ténébrion (Proies Protéiques)", "PROIE", "Arthropodes proies apportant l'azote au couvain", 40));
            list.add(new AccessorySpeciesInfo("Fourmilion Piégeur (Myrmeleon)", "PRÉDATEUR", "Prédateur piégeur en entonnoir de sable", 10));
        } else if (lower.contains("apis") || lower.contains("abeille")) {
            list.add(new AccessorySpeciesInfo("Graminées & Fleurs Nectarifères", "COMMENSAL", "Fleurs mellifères fournissant nectar et pollen", 120));
            list.add(new AccessorySpeciesInfo("Acarien Parasite (Varroa destructor)", "PATHOGEN", "Acarien parasite hématophage du couvain d'abeilles", 30));
            list.add(new AccessorySpeciesInfo("Araignée Chasseresse / Faux-Bourdon", "PRÉDATEUR", "Prédateurs capturant les butineuses au vol", 15));
        } else if (lower.contains("reticulitermes") || lower.contains("macrotermes") || lower.contains("termite")) {
            list.add(new AccessorySpeciesInfo("Mousse Humide & Bois Ligneux", "COMMENSAL", "Biomasse cellulosique digestive", 100));
            list.add(new AccessorySpeciesInfo("Larves de Ténébrion (Proies Protéiques)", "PROIE", "Proies arthropodes résiduelles du sol", 30));
            list.add(new AccessorySpeciesInfo("Guêpe Chasseresse / Araignée Souterraine", "PRÉDATEUR", "Prédateurs spécialistes des galeries", 12));
        } else if (lower.contains("vespula") || lower.contains("guêpe")) {
            list.add(new AccessorySpeciesInfo("Larves de Ténébrion & Chenilles (Proies)", "PROIE", "Insectes proies chassés pour la protéine du couvain", 60));
            list.add(new AccessorySpeciesInfo("Graminées & Fleurs (Nectar)", "COMMENSAL", "Sources sucrées énergétiques pour adultes", 80));
            list.add(new AccessorySpeciesInfo("Champignon Entomopathogène (Cordyceps)", "PATHOGEN", "Spores véhiculées lors des vols", 15));
        } else { // Lasius, Solenopsis, Formica, etc.
            list.add(new AccessorySpeciesInfo("Pucerons du Pin (Cinara / Miellat)", "COMMENSAL", "Pucerons exploités en trophobiose pour le miellat sucré", 80));
            list.add(new AccessorySpeciesInfo("Graminées à Graines (Herbes & Biomasse)", "COMMENSAL", "Végétation locale fournissant substrat et ombrage", 100));
            list.add(new AccessorySpeciesInfo("Larves de Ténébrion (Proies Protéiques)", "PROIE", "Arthropodes proies apportant les protéines", 50));
            list.add(new AccessorySpeciesInfo("Fourmilion Piégeur (Myrmeleon)", "PRÉDATEUR", "Prédateur naturel des patrouilleuses", 15));
            list.add(new AccessorySpeciesInfo("Champignon Entomopathogène (Cordyceps)", "PATHOGEN", "Parasite mycélien ciblant la colonie", 10));
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
        return "Galeries Souterraines Matures";
    }

    private void updateStepDtLabel() {
        if (lblStepDt != null) {
            lblStepDt.setText(String.format("Δt = %.3f s", simulationStepSeconds));
        }
    }
}
