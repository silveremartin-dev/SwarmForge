/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.client.event.UiEvent;
import org.swarmforge.client.event.UiEventBus;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.core.behavior.ReasoningArchitecture.ArchitectureType;
import org.swarmforge.core.engine.ComputeAccelerationMode;
import org.swarmforge.core.engine.EnginePreferences;
import org.swarmforge.core.engine.SimulationEngineType;
import org.swarmforge.core.scenario.AcademicScenarios;
import org.swarmforge.core.scenario.Scenario;
import org.swarmforge.core.simulation.SimulationCheckpoint;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

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

    private final I18nManager i18n = I18nManager.getInstance();

    Button btnGoToBeginning;
    Button btnRewind;
    Button btnStepBack;
    Button btnPlay;
    Button btnStepForward;
    Button btnFastForward;
    Button btnGoToEnd;

    Slider speedSlider;
    private Slider timelineSlider;
    final ComboBox<String> scenarioStepCombo = new ComboBox<>();
    final ComboBox<String> engineBackendCombo = new ComboBox<>();
    final ComboBox<String> computeAccelerationCombo = new ComboBox<>();

    Label lblSpeed;
    private Label lblTick;
    private Label lblTime;
    Label lblDateTime;
    private Label lblStepDt;

    // Preset Managers
    final SpeciesPresetManager speciesPresetManager = new SpeciesPresetManager();
    final WorldPresetManager worldPresetManager = new WorldPresetManager();
    final WeatherPresetManager weatherPresetManager = new WeatherPresetManager();
    private final NestPresetManager nestPresetManager = new NestPresetManager();
    final ScenarioPresetManager scenarioPresetManager = new ScenarioPresetManager();
    final org.swarmforge.client.ui.control.RealWeatherApiClient weatherApiClient = new org.swarmforge.client.ui.control.RealWeatherApiClient();

    // 1. World & 2. Weather Combos
    final ComboBox<String> comboMeta = new ComboBox<>();
    final ComboBox<String> comboWorld = new ComboBox<>();
    final ComboBox<String> comboWeather = new ComboBox<>();
    private final ComboBox<String> comboExecutionMode = new ComboBox<>();
    final TextField txtSeed = new TextField("12345");
    final TextArea areaDescription = new TextArea();

    // Weather Mode Fields (Simulated vs Real Open-Meteo)
    final Label lblRealWeatherStatus = new Label();
    Map<String, Object> cachedRealWeatherData = null;
    boolean isRealWeatherMode = false;

    // Start Date & Time Controls
    DatePicker startDatePicker;
    Spinner<Integer> startTimeHourSpinner;
    Spinner<Integer> startTimeMinuteSpinner;
    Spinner<Integer> startTimeSecondSpinner;

    // Termination Limits & Duration Controls
    final Spinner<Double> maxDurationSpinner = new Spinner<>(1.0, 2_000_000_000.0, 100.0, 1.0);
    final ComboBox<String> durationUnitCombo = new ComboBox<>();
    final Label lblDurationCalculatedInfo = new Label();
    final Spinner<Integer> minPopStopSpinner = new Spinner<>(0, 1000, 0, 5);

    // Multiplayer & Megaterrarium Fields
    final CheckBox chkMultiplayerOnly = new CheckBox();
    final Spinner<Integer> spinRequiredPlayers = new Spinner<>(1, 16, 1);
    final Spinner<Integer> spinGridTilesX = new Spinner<>(1, 8, 1);
    final Spinner<Integer> spinGridTilesY = new Spinner<>(1, 8, 1);
    private final org.swarmforge.client.ui.control.MultiplayerServerCard multiplayerCard = new org.swarmforge.client.ui.control.MultiplayerServerCard(speciesPresetManager, chkMultiplayerOnly, spinRequiredPlayers, spinGridTilesX, spinGridTilesY, this::updateValidationPanel);

    // 3. Multi-Species Scenario Config List
    final ComboBox<String> comboAvailableSpecies = new ComboBox<>();
    final VBox speciesListContainer = new VBox(10);
    final List<SpeciesConfigCard> speciesCardList = new ArrayList<>();
    volatile ScenarioSetupSnapshot lastSetupSnapshot;

    private final SimulationScenarioIoHelper ioHelper = new SimulationScenarioIoHelper(this);
    final SimulationPlaybackSubPanel playbackSubPanel = new SimulationPlaybackSubPanel(this);
    private final SimulationConfigGridBuilder gridBuilder = new SimulationConfigGridBuilder(this);

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
        String maxUnit = durationUnitCombo != null && durationUnitCombo.getValue() != null ? durationUnitCombo.getValue() : "Days (d)";
        int minPop = minPopStopSpinner != null && minPopStopSpinner.getValue() != null ? minPopStopSpinner.getValue() : 0;
        String desc = areaDescription != null ? areaDescription.getText() : "";
        boolean mpOnly = chkMultiplayerOnly != null && chkMultiplayerOnly.isSelected();
        int reqPlayers = spinRequiredPlayers != null && spinRequiredPlayers.getValue() != null ? spinRequiredPlayers.getValue() : 1;
        int tilesX = spinGridTilesX != null && spinGridTilesX.getValue() != null ? spinGridTilesX.getValue() : 1;
        int tilesY = spinGridTilesY != null && spinGridTilesY.getValue() != null ? spinGridTilesY.getValue() : 1;
        String engineBackend = engineBackendCombo != null && engineBackendCombo.getValue() != null ? engineBackendCombo.getValue() : "AUTO";
        String computeAccel = computeAccelerationCombo != null && computeAccelerationCombo.getValue() != null ? computeAccelerationCombo.getValue() : "AUTO";

        return new ScenarioSetupSnapshot(
            getMasterSeed(), dtIso, getSimulationStepSeconds(), maxVal, maxUnit, minPop, desc,
            getSelectedWorld(), getSelectedWeather(), getSelectedSpecies(), getSelectedNestType(),
            getQueenCount(), getWorkerCount(), getSoldierCount(), getBroodCount(),
            snapshots, mpOnly, reqPlayers, tilesX, tilesY, engineBackend, computeAccel
        );
    }

    public ScenarioSetupSnapshot getLastSetupSnapshot() {
        return lastSetupSnapshot;
    }

    private VBox validationPanel;

    boolean isPlaying = false;
    boolean isPaused = false;
    boolean isStopped = true;
    float currentSpeed = 1.0f;
    long currentTick = 0;
    long maxTick = 0;
    long highestRecordedTick = 0;
    float simulationStepSeconds = 0.0166f;
    LocalDateTime startDateTime = LocalDateTime.of(2026, 3, 20, 8, 0, 0);
    private LocalDateTime currentDateTime = startDateTime;

    // Callbacks
    Consumer<Void> onPlay;
    Consumer<Void> onPause;
    Consumer<Void> onStop;
    Consumer<Float> onSpeedChange;
    Consumer<Long> onSeek;
    Consumer<Integer> onRewind;
    Consumer<Integer> onStepForward;
    Consumer<Float> onStepChange;
    Consumer<String> onCreateCheckpoint;
    Consumer<SimulationCheckpoint> onRestoreCheckpoint;
    Consumer<Long> onApplyPresets;
    Consumer<LocalDateTime> onStartDateTimeChange;

    private final ComboBox<SimulationCheckpoint> comboCheckpoints = new ComboBox<>();
    final VBox playbackAndSpeedPanel = new VBox(8);

    private Button btnApplyPresets;
    boolean isCreatingScenario = false;
    Timeline activeCreationTimeline = null;
    java.util.concurrent.CompletableFuture<?> activeCreationFuture = null;

    ProgressBar inlineProgressBar;
    Label inlineProgressLabel;
    HBox applyProgressBox;

    public SimulationControlPanel() {
        setSpacing(10);
        setMaxWidth(Double.MAX_VALUE);
        setPrefWidth(Region.USE_COMPUTED_SIZE);

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

        // Execution Engine Selector
        HBox execModeBox = new HBox(8);
        execModeBox.setAlignment(Pos.CENTER_LEFT);
        Label lblExecMode = new Label();
        lblExecMode.textProperty().bind(i18n.createStringBinding("sim.exec_mode.title"));
        lblExecMode.getStyleClass().addAll("accent-title", "compact-combo");
        lblExecMode.tooltipProperty().bind(i18n.createTooltipBinding("sim.exec_mode.title.tt"));

        comboExecutionMode.getItems().setAll(
            i18n.get("sim.exec_mode.local", "● Mode Local Embarqué (In-Process CPU)"),
            i18n.get("sim.exec_mode.server", "● Mode Serveur SwarmForge (Distant / Cluster gRPC)")
        );
        comboExecutionMode.getSelectionModel().selectFirst();
        comboExecutionMode.setMaxWidth(Double.MAX_VALUE);
        comboExecutionMode.getStyleClass().add("compact-combo");
        comboExecutionMode.setTooltip(new Tooltip(i18n.get("sim.exec_mode.tt")));
        HBox.setHgrow(comboExecutionMode, Priority.ALWAYS);
        execModeBox.getChildren().addAll(lblExecMode, comboExecutionMode);

        VBox scenarioBodyBox = new VBox(10);

        Runnable updateAuthorityLock = () -> {
            boolean isServer = isServerExecutionMode();
            boolean isJoin = isServerJoinMode();

            boolean lockLocalWorld = isServer && isJoin;
            scenarioBodyBox.setDisable(lockLocalWorld);

            multiplayerCard.updateRoleVisibility(isServer, isJoin);

            updateApplyPresetsButtonState(isCreatingScenario);
            updateValidationPanel();
        };

        comboExecutionMode.valueProperty().addListener((obs, oldV, newV) -> updateAuthorityLock.run());
        multiplayerCard.getComboServerRole().valueProperty().addListener((obs, oldV, newV) -> updateAuthorityLock.run());

        Button bSaveScenario = createScenarioActionButton("common.btn.save", Feather.SAVE, "btn-secondary", "sim.btn.save.tt", this::handleSaveScenario);
        Button bDeleteScenario = createScenarioActionButton("common.btn.delete", Feather.TRASH_2, "btn-danger", "sim.btn.delete.tt", this::handleDeleteScenario);
        Button bExportScenario = createScenarioActionButton("common.btn.export", Feather.DOWNLOAD, "btn-secondary", "sim.btn.export.tt", this::handleExportScenario);
        Button bImportScenario = createScenarioActionButton("common.btn.import", Feather.UPLOAD, "btn-secondary", "sim.btn.import.tt", this::handleImportScenario);

        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        Label lblMeta = new Label();
        lblMeta.textProperty().bind(i18n.createStringBinding("sim.preset.meta"));
        lblMeta.getStyleClass().add("purple-accent-title");
        lblMeta.setMinWidth(Region.USE_PREF_SIZE);

        List<String> scenarioList = new ArrayList<>();
        scenarioList.add(i18n.get("sim.preset.custom_new", "[➕ Scénario Personnalisé (Création libre)]"));

        for (Scenario sc : AcademicScenarios.getAllAcademicScenarios(12345L)) {
            scenarioList.add("🔬 " + sc.getTitle());
        }
        for (Scenario sc : AcademicScenarios.getAllMultiplayerScenarios(12345L)) {
            scenarioList.add("⚔️ " + sc.getTitle());
        }
        for (String name : scenarioPresetManager.getPresetNames()) {
            if (!name.startsWith("ACAD_") && !name.startsWith("MP_") && !scenarioList.contains(name)
                    && !scenarioList.contains("🔬 " + name) && !scenarioList.contains("⚔️ " + name) && !scenarioList.contains("📌 " + name)) {
                scenarioList.add("📌 " + name);
            }
        }

        comboMeta.getItems().setAll(scenarioList);
        comboMeta.setEditable(false);
        comboMeta.setMaxWidth(Double.MAX_VALUE);
        comboMeta.setTooltip(new Tooltip(i18n.get("sim.preset.meta.tt", "Sélectionnez un scénario académique ou multijoueur, ou composez un scénario personnalisé.")));
        HBox.setHgrow(comboMeta, Priority.ALWAYS);
        comboMeta.setOnAction(e -> applyMetaPreset(comboMeta.getValue()));

        metaRow.getChildren().addAll(lblMeta, comboMeta);

        HBox presetActionsRow = new HBox(8, bSaveScenario, bDeleteScenario, bExportScenario, bImportScenario);
        presetActionsRow.setAlignment(Pos.CENTER_LEFT);

        if (!comboMeta.getItems().isEmpty()) {
            comboMeta.getSelectionModel().select(comboMeta.getItems().size() > 1 ? 1 : 0);
            applyMetaPreset(comboMeta.getValue());
        }

        VBox descBox = new VBox(4);
        Label lblDesc = new Label();
        lblDesc.textProperty().bind(i18n.createStringBinding("sim.desc.title"));
        lblDesc.getStyleClass().addAll("sub-title-gray", "card-caption-title");
        areaDescription.setPrefRowCount(2);
        areaDescription.setPromptText("Scenario description and experimental objectives...");
        areaDescription.setWrapText(true);
        areaDescription.getStyleClass().add("compact-text-area");
        descBox.getChildren().addAll(lblDesc, areaDescription);

        // Populate World and Weather combos
        List<String> sortedWorldNames = new ArrayList<>(worldPresetManager.names());
        Collections.sort(sortedWorldNames);
        comboWorld.getItems().setAll(sortedWorldNames);
        if (!comboWorld.getItems().isEmpty()) comboWorld.getSelectionModel().selectFirst();
        comboWorld.setOnAction(e -> alignWeatherWithWorld(false));

        List<String> sortedWeatherNames = new ArrayList<>(weatherPresetManager.names());
        Collections.sort(sortedWeatherNames);
        comboWeather.getItems().setAll(sortedWeatherNames);
        if (!comboWeather.getItems().isEmpty()) comboWeather.getSelectionModel().selectFirst();

        // Section 1: Biotope & Weather Presets
        GridPane gridWorldWeather = buildWorldWeatherGrid();

        // Section 2: Start Date, Time & Master Seed
        GridPane gridDateTimeSeed = buildDateTimeSeedGrid();

        // Section 3: Physics Step (dt) & Compute Acceleration
        GridPane gridPhysics = buildPhysicsGrid();

        // Section 4: Termination Limits & Duration Controls
        GridPane gridLimits = buildLimitsGrid();

        // Section 5: Multi-Species Scenario Section Header & Controls
        VBox section3Container = buildSpeciesSection();

        comboMeta.setOnAction(e -> {
            applyMetaPreset(comboMeta.getValue());
            updateValidationPanel();
        });

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

        validationPanel = buildValidationPanel();
        VBox checkpointsPane = playbackSubPanel.buildCheckpointsPane(comboCheckpoints);

        btnApplyPresets = new Button();
        btnApplyPresets.setMaxWidth(Double.MAX_VALUE);
        updateApplyPresetsButtonState(false);

        inlineProgressBar = new ProgressBar(0);
        inlineProgressBar.setMaxWidth(Double.MAX_VALUE);
        inlineProgressBar.setPrefHeight(6);
        inlineProgressBar.getStyleClass().add("scenario-progress-bar");

        inlineProgressLabel = new Label("Initializing scenario...");
        inlineProgressLabel.getStyleClass().addAll("accent-title", "scenario-progress-label");

        applyProgressBox = new HBox(8, inlineProgressBar, inlineProgressLabel);
        applyProgressBox.setAlignment(Pos.CENTER_LEFT);
        applyProgressBox.setVisible(false);
        applyProgressBox.setManaged(false);

        btnApplyPresets.setOnAction(e -> handleApplyScenario());

        VBox megaterrariumCard = createMultiplayerAndMegaterrariumCard();

        scenarioBodyBox.getChildren().addAll(
            metaRow, presetActionsRow, descBox, new Separator(),
            gridWorldWeather, new Separator(),
            gridDateTimeSeed, new Separator(),
            gridPhysics, new Separator(),
            gridLimits, new Separator(),
            megaterrariumCard, new Separator(),
            section3Container, new Separator(),
            checkpointsPane
        );

        updateAuthorityLock.run();

        scenarioCard.getChildren().addAll(
            execModeBox,
            multiplayerCard.getServerNetworkBox(),
            new Separator(),
            scenarioBodyBox,
            validationPanel,
            btnApplyPresets,
            applyProgressBox
        );

        updateValidationPanel();
        playbackSubPanel.populatePlaybackAndSpeedPanel();

        getChildren().addAll(headerVBox, scenarioCard);
        updateButtonStates();
    }

    private Button createScenarioActionButton(String textKey, Feather icon, String styleClass, String tooltipKey, Runnable action) {
        return gridBuilder.createScenarioActionButton(textKey, icon, styleClass, tooltipKey, action);
    }

    private GridPane buildWorldWeatherGrid() {
        return gridBuilder.buildWorldWeatherGrid();
    }

    private GridPane buildDateTimeSeedGrid() {
        return gridBuilder.buildDateTimeSeedGrid();
    }

    private GridPane buildPhysicsGrid() {
        return gridBuilder.buildPhysicsGrid();
    }

    private GridPane buildLimitsGrid() {
        return gridBuilder.buildLimitsGrid();
    }

    private VBox buildSpeciesSection() {
        return gridBuilder.buildSpeciesSection();
    }

    void addSpeciesCard(String speciesName) {
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

    void refreshSpeciesListContainer() {
        speciesListContainer.getChildren().clear();
        for (SpeciesConfigCard card : speciesCardList) {
            speciesListContainer.getChildren().add(card.getCardPane());
        }
    }

    private VBox createMultiplayerAndMegaterrariumCard() {
        return multiplayerCard.createMegaterrariumCard();
    }

    void updateApplyPresetsButtonState(boolean isCreating) {
        if (btnApplyPresets == null) return;
        btnApplyPresets.textProperty().unbind();
        if (btnApplyPresets.getTooltip() != null) {
            btnApplyPresets.getTooltip().textProperty().unbind();
        } else {
            btnApplyPresets.setTooltip(new Tooltip());
        }

        btnApplyPresets.getStyleClass().removeAll("danger-button", "action-primary-button", "btn-danger", "btn-primary");
        if (isCreating) {
            btnApplyPresets.textProperty().bind(i18n.createStringBinding("sim.btn.cancel_creation"));
            btnApplyPresets.getStyleClass().addAll("btn-danger", "danger-button");
            btnApplyPresets.getTooltip().textProperty().bind(i18n.createStringBinding("sim.btn.cancel_creation.tt"));
        } else if (isServerExecutionMode()) {
            btnApplyPresets.setText(isServerJoinMode() ?
                i18n.get("sim.server.btn.join", "🌐 Rejoindre la Partie Serveur") :
                i18n.get("sim.server.btn.deploy", "🚀 Déployer Scénario sur Serveur"));
            btnApplyPresets.getStyleClass().addAll("btn-primary", "action-primary-button");
            btnApplyPresets.getTooltip().setText(isServerJoinMode() ?
                i18n.get("sim.server.role.join", "Rejoint la simulation partagée sur le serveur SwarmForge.") :
                i18n.get("sim.server.role.host", "Déploie le scénario et initialise le monde sur le serveur SwarmForge."));
        } else {
            btnApplyPresets.textProperty().bind(i18n.createStringBinding("sim.btn.apply_create"));
            btnApplyPresets.getStyleClass().addAll("btn-primary", "action-primary-button");
            btnApplyPresets.getTooltip().textProperty().bind(i18n.createStringBinding("sim.btn.apply_presets.tt"));
        }
    }

    private void handleApplyScenario() {
        ioHelper.handleApplyScenario();
    }

    void alignWeatherWithWorld() {
        alignWeatherWithWorld(false);
    }

    void alignWeatherWithWorld(boolean showNotification) {
        ioHelper.alignWeatherWithWorld(showNotification);
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

    void applyMetaPreset(String rawMetaName) {
        ioHelper.applyMetaPreset(rawMetaName);
    }

    public void resetTimelineTicks() {
        this.currentTick = 0;
        this.highestRecordedTick = 0;
        this.maxTick = 0;
        this.isPlaying = false;
        this.isPaused = false;
        this.isStopped = true;
        updateTick(0, 0);
        updateButtonStates();
    }

    void updateButtonStates() {
        playbackSubPanel.updateButtonStates();
        UiEventBus.getInstance().publishFx(new UiEvent.SimulationStateChangedUiEvent(isPlaying, isPaused, isStopped, currentTick, currentSpeed));
    }

    public void updateTick(long tick, long maxTick) {
        updateTick(tick, maxTick, tick * (double) simulationStepSeconds);
    }

    public void updateTick(long tick, long maxTick, double elapsedSimulationSeconds) {
        this.currentTick = tick;
        if (tick > this.highestRecordedTick) {
            this.highestRecordedTick = tick;
        }
        this.maxTick = Math.max(this.highestRecordedTick, Math.max(tick, maxTick));
        updateButtonStates();
        double totalSecs = (elapsedSimulationSeconds >= 0.0) ? elapsedSimulationSeconds : (tick * (double) simulationStepSeconds);
        long totalSecondsElapsed = (long) totalSecs;
        currentDateTime = startDateTime.plusNanos((long) (totalSecs * 1_000_000_000L));

        String timeStr = formatSimulationTimeSeconds(totalSecs);
        lblDateTime.setText("📅 " + currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + String.format(" (Jour %d)", 1 + (totalSecondsElapsed / 86400)) + "  |  ⏱️ " + timeStr + String.format(" (Pas #%d)", tick));
        if (lblTick != null) {
            lblTick.setText(timeStr + String.format(" (Pas #%d)", tick));
        }
        if (lblTime != null) lblTime.setText(timeStr);

        if (timelineSlider != null && !timelineSlider.isValueChanging()) {
            timelineSlider.setMax(Math.max(this.maxTick, tick + 100));
            timelineSlider.setValue(tick);
        }
    }

    public static String formatSimulationTimeSeconds(double totalSeconds) {
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

    public static String formatSimulationTime(long tick, double stepSeconds) {
        return formatSimulationTimeSeconds(tick * stepSeconds);
    }

    public void setSpeed(float speed) {
        this.currentSpeed = speed;
        if (speedSlider != null) speedSlider.setValue(speed);
    }

    public float getSpeedMultiplier() {
        return currentSpeed;
    }

    public LocalDateTime getStartDateTime() {
        if (startDatePicker != null && startDatePicker.getValue() != null &&
            startTimeHourSpinner != null && startTimeMinuteSpinner != null && startTimeSecondSpinner != null) {
            return LocalDateTime.of(
                startDatePicker.getValue(),
                LocalTime.of(
                    startTimeHourSpinner.getValue(),
                    startTimeMinuteSpinner.getValue(),
                    startTimeSecondSpinner.getValue()
                )
            );
        }
        return startDateTime != null ? startDateTime : LocalDateTime.of(2026, 3, 20, 8, 0, 0);
    }

    public void setOnPlay(Consumer<Void> callback) { this.onPlay = callback; }
    public void setOnPause(Consumer<Void> callback) { this.onPause = callback; }
    public void setOnStop(Consumer<Void> callback) { this.onStop = callback; }
    public void setOnSpeedChange(Consumer<Float> callback) { this.onSpeedChange = callback; }
    public void setOnSeek(Consumer<Long> callback) { this.onSeek = callback; }
    public void setOnRewind(Consumer<Integer> callback) { this.onRewind = callback; }
    public void setOnStepForward(Consumer<Integer> callback) { this.onStepForward = callback; }
    public void setOnStepChange(Consumer<Float> callback) { this.onStepChange = callback; }
    public void setOnCreateCheckpoint(Consumer<String> cb) { this.onCreateCheckpoint = cb; }
    public void setOnRestoreCheckpoint(Consumer<SimulationCheckpoint> cb) { this.onRestoreCheckpoint = cb; }
    public void setOnApplyPresets(Consumer<Long> callback) { this.onApplyPresets = callback; }
    public void setOnStartDateTimeChange(Consumer<LocalDateTime> callback) { this.onStartDateTimeChange = callback; }

    public boolean isServerExecutionMode() {
        String val = comboExecutionMode.getValue();
        return val != null && (val.contains("Serveur") || val.contains("Server") || val.contains("gRPC") || val.contains("Cluster") || val.contains("Servidor") || val.contains("服务器"));
    }

    public void setExecutionMode(boolean serverMode) {
        if (serverMode && comboExecutionMode.getItems().size() > 1) {
            comboExecutionMode.getSelectionModel().select(1);
        } else {
            comboExecutionMode.getSelectionModel().selectFirst();
        }
    }

    public String getServerHost() { return multiplayerCard.getServerHost(); }
    public int getServerPort() { return multiplayerCard.getServerPort(); }
    public void setServerHost(String host) { multiplayerCard.setServerHost(host); }
    public void setServerPort(int port) { multiplayerCard.setServerPort(port); }
    public boolean isServerConnected() { return multiplayerCard.isServerConnected(); }
    public void setServerConnectionStatus(boolean connected, String message) { multiplayerCard.setServerConnectionStatus(connected, message); }
    public boolean isServerJoinMode() {
        String val = multiplayerCard.getComboServerRole().getValue();
        return val == null || val.contains("Rejoindre") || val.contains("Join") || val.contains("Beitreten") || val.contains("Unirse") || val.contains("加入");
    }
    public String getPlayerAlias() { return multiplayerCard.getPlayerAlias(); }
    public String getPlayerSpecies() { return multiplayerCard.getPlayerSpecies(); }
    public void setServerSessionStats(String stats) { multiplayerCard.setServerSessionStats(stats); }
    public void setOnServerConnect(Runnable r) { multiplayerCard.setOnServerConnect(r); }
    public void setOnServerDisconnect(Runnable r) { multiplayerCard.setOnServerDisconnect(r); }
    public void setOnServerDiscover(Runnable r) { multiplayerCard.setOnServerDiscover(r); }
    public void setOnServerStart(Runnable r) { multiplayerCard.setOnServerStart(r); }
    public Runnable getOnServerConnectAction() { return multiplayerCard.getOnServerConnectAction(); }

    public void updateCheckpoints(List<SimulationCheckpoint> checkpoints) {
        comboCheckpoints.getItems().clear();
        if (checkpoints != null) {
            comboCheckpoints.getItems().addAll(checkpoints);
            if (!comboCheckpoints.getItems().isEmpty()) {
                comboCheckpoints.getSelectionModel().selectLast();
            }
        }
    }

    public int getCheckpointsCount() {
        return comboCheckpoints.getItems().size();
    }

    public long getCurrentTick() { return currentTick; }
    public long getMaxTick() { return maxTick; }
    public float getSimulationStepSeconds() { return simulationStepSeconds; }
    public void setSimulationStepSeconds(float dt) { this.simulationStepSeconds = dt; updateStepDtLabel(); }

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

    Supplier<Map<String, Object>> worldConfigSupplier;

    public void setWorldConfigSupplier(Supplier<Map<String, Object>> supplier) {
        this.worldConfigSupplier = supplier;
    }

    public boolean isRealWeatherMode() {
        return isRealWeatherMode;
    }

    public Map<String, Object> getRealWeatherData() {
        return cachedRealWeatherData;
    }

    public void fetchRealWeatherFromWorld(TextField targetField) {
        ioHelper.fetchRealWeatherFromWorld(targetField);
    }

    public void fetchRealWeather(String cityQuery) {
        ioHelper.fetchRealWeather(cityQuery);
    }

    public void fetchRealWeatherByCoordinates(double lat, double lon, double alt, String name) {
        ioHelper.fetchRealWeatherByCoordinates(lat, lon, alt, name);
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
        return getSelectedSpeciesList().get(0);
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
        } else if (unit != null && unit.startsWith("Minutes")) {
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

    private void handleSaveScenario() { ioHelper.handleSaveScenario(); }
    private void handleDeleteScenario() { ioHelper.handleDeleteScenario(); }
    private void handleExportScenario() { ioHelper.handleExportScenario(); }
    private void handleImportScenario() { ioHelper.handleImportScenario(); }

    private void updateEngineAndAccelComboTexts() {
        int engineIdx = Math.max(0, engineBackendCombo.getSelectionModel().getSelectedIndex());
        engineBackendCombo.getItems().setAll(
            i18n.get("sim.engine.backend.auto"),
            i18n.get("sim.engine.backend.java"),
            i18n.get("sim.engine.backend.rust")
        );
        engineBackendCombo.getSelectionModel().select(engineIdx);

        int accelIdx = Math.max(0, computeAccelerationCombo.getSelectionModel().getSelectedIndex());
        computeAccelerationCombo.getItems().setAll(
            i18n.get("sim.compute.acceleration.auto"),
            i18n.get("sim.compute.acceleration.gpu"),
            i18n.get("sim.compute.acceleration.cpu")
        );
        computeAccelerationCombo.getSelectionModel().select(accelIdx);
    }

    public record ScenarioWarning(
        String category,
        String severity,
        String message,
        String fixButtonText,
        Runnable fixAction
    ) {
        public ScenarioWarning(ScenarioValidationPane.ScenarioWarning w) {
            this(w.category(), w.severity(), w.message(), w.fixButtonText(), w.fixAction());
        }
    }

    private ScenarioValidationPane validationPane;

    private VBox buildValidationPanel() {
        if (validationPane == null) {
            validationPane = new ScenarioValidationPane(this);
        }
        return validationPane.getContainer();
    }

    public void updateValidationPanel() {
        if (validationPane != null) {
            validationPane.updateValidationPanel();
        }
    }

    public List<ScenarioWarning> evaluateScenarioWarnings() {
        if (validationPane == null) {
            validationPane = new ScenarioValidationPane(this);
        }
        List<ScenarioWarning> result = new ArrayList<>();
        for (ScenarioValidationPane.ScenarioWarning w : validationPane.evaluateScenarioWarnings()) {
            result.add(new ScenarioWarning(w));
        }
        return result;
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

    void updateStepDtLabel() {
        if (lblStepDt != null) {
            lblStepDt.setText(String.format("Δt = %.3f s", simulationStepSeconds));
        }
        updateDurationCalculatedLabel();
    }

    public static int estimateNestCapacity(String nestType) {
        return SpeciesConfigCard.estimateNestCapacity(nestType);
    }

    public static List<String> getCompatibleNestTypes(String speciesName) {
        return SpeciesConfigCard.getCompatibleNestTypes(speciesName);
    }

    public void updateDurationCalculatedLabel() {
        if (lblDurationCalculatedInfo == null) return;
        if (durationUnitCombo != null && ("∞ Unlimited".equals(durationUnitCombo.getValue()) || "∞ Illimité".equals(durationUnitCombo.getValue()))) {
            lblDurationCalculatedInfo.setText(i18n.get("sim.duration_unlimited"));
            return;
        }
        long totalTicks = getMaxSimulationTicks();
        double dt = getSimulationStepSeconds();
        if (dt <= 0) dt = 0.016666666666666666;

        String formatted = formatSimulationTime(totalTicks, dt);
        lblDurationCalculatedInfo.setText(String.format("⏱️ Equivalent Duration: %s (%,d steps at Δt = %.3fs)", formatted, totalTicks, dt));
    }
}
