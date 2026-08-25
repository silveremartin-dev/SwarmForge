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

import java.time.LocalDateTime;
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

    private final Button btnPlay;
    private final Button btnPause;
    private final Button btnStop;
    private final Button btnRewind;
    private final Button btnFastForward;
    private final Button btnStepBack;
    private final Button btnStepForward;

    private final Slider speedSlider;
    private Slider timelineSlider;
    private final ComboBox<String> stepSizeCombo;

    private final Label lblSpeed;
    private Label lblTick;
    private Label lblTime;
    private final Label lblDateTime;

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
    private final Spinner<Long> maxTicksSpinner = new Spinner<>(1000L, 1_000_000L, 100_000L, 5000L);
    private final Spinner<Integer> minPopStopSpinner = new Spinner<>(0, 1000, 0, 5);

    // 3. Multi-Species Scenario Config List
    private final ComboBox<String> comboAvailableSpecies = new ComboBox<>();
    private final VBox speciesListContainer = new VBox(10);
    private final List<SpeciesConfigCard> speciesCardList = new ArrayList<>();

    private boolean isPlaying = false;
    private float currentSpeed = 1.0f;
    private long currentTick = 0;
    private long maxTick = 0;
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
    private Consumer<Float> onStepChange;
    private Consumer<String> onCreateCheckpoint;
    private Consumer<org.swarmforge.core.simulation.SimulationCheckpoint> onRestoreCheckpoint;
    private Consumer<Long> onApplyPresets;

    private final ComboBox<org.swarmforge.core.simulation.SimulationCheckpoint> comboCheckpoints = new ComboBox<>();
    private final VBox playbackAndSpeedPanel = new VBox(8);

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

        Label lblPresetHeader = new Label("Configuration du Scénario & Écosystème Multi-Espèces");
        lblPresetHeader.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 18px;");
        lblPresetHeader.setTooltip(new Tooltip("Définition scientifique du scénario, sélection du biotope, climat et assemblage dynamique des espèces."));

        Region spHeader = new Region();
        HBox.setHgrow(spHeader, Priority.ALWAYS);

        Button bSaveScenario = new Button(I18nManager.getInstance().get("common.btn.save"));
        bSaveScenario.setGraphic(new FontIcon(Feather.SAVE));
        bSaveScenario.getStyleClass().add("btn-secondary");
        bSaveScenario.setTooltip(new Tooltip("Enregistrer le scénario actuel."));
        bSaveScenario.setOnAction(e -> handleSaveScenario());

        Button bDeleteScenario = new Button(I18nManager.getInstance().get("common.btn.delete"));
        bDeleteScenario.setGraphic(new FontIcon(Feather.TRASH_2));
        bDeleteScenario.getStyleClass().add("btn-danger");
        bDeleteScenario.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold;");
        bDeleteScenario.setTooltip(new Tooltip("Supprimer le scénario sélectionné."));
        bDeleteScenario.setOnAction(e -> handleDeleteScenario());

        Button bExportScenario = new Button(I18nManager.getInstance().get("common.btn.export"));
        bExportScenario.setGraphic(new FontIcon(Feather.DOWNLOAD));
        bExportScenario.getStyleClass().add("btn-secondary");
        bExportScenario.setTooltip(new Tooltip("Exporter le scénario en JSON."));
        bExportScenario.setOnAction(e -> handleExportScenario());

        Button bImportScenario = new Button(I18nManager.getInstance().get("common.btn.import"));
        bImportScenario.setGraphic(new FontIcon(Feather.UPLOAD));
        bImportScenario.getStyleClass().add("btn-secondary");
        bImportScenario.setTooltip(new Tooltip("Importer un fichier JSON de scénario."));
        bImportScenario.setOnAction(e -> handleImportScenario());

        headerRow.getChildren().addAll(lblPresetHeader, spHeader, bSaveScenario, bDeleteScenario, bExportScenario, bImportScenario);
        headerVBox.getChildren().addAll(headerRow, new Separator());

        // Top Meta-Scenario Header Card
        VBox scenarioCard = new VBox(10);
        scenarioCard.getStyleClass().add("card-pane");
        scenarioCard.setStyle("-fx-background-color: #18181b; -fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #27272a; -fx-border-radius: 8;");

        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        Label lblMeta = new Label("★ Preset Global de Scénario :");
        lblMeta.setStyle("-fx-text-fill: #a78bfa; -fx-font-weight: bold; -fx-font-size: 11px;");

        comboMeta.getItems().setAll(scenarioPresetManager.getPresetNames());
        if (comboMeta.getItems().isEmpty()) {
            comboMeta.getItems().addAll(
                "🌿 Écosystème Amazonien (Atta sexdens & Champignon)",
                "🏜 Steppe Aride Granivore (Messor barbatus & Graminées)",
                "⚔️ Guerre Territoriale (Solenopsis invicta vs Lasius niger)",
                "🐝 Rucher Arboricole (Apis mellifera & Butinage)",
                "🏛️ Cathédrale de Termites (Reticulitermes & Macrotermes)"
            );
        }
        comboMeta.getSelectionModel().selectFirst();
        comboMeta.setEditable(true);
        comboMeta.setMaxWidth(Double.MAX_VALUE);
        comboMeta.setTooltip(new Tooltip("Sélectionnez ou créez un preset global assemblant biotope, climat et faune."));
        HBox.setHgrow(comboMeta, Priority.ALWAYS);

        metaRow.getChildren().addAll(lblMeta, comboMeta);

        VBox descBox = new VBox(4);
        Label lblDesc = new Label("📝 Description Scientifique du Scénario :");
        lblDesc.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px; -fx-font-weight: bold;");
        areaDescription.setPrefRowCount(2);
        areaDescription.setPromptText("Description et objectifs d'expérimentation du scénario...");
        areaDescription.setWrapText(true);
        areaDescription.setStyle("-fx-font-size: 11px; -fx-control-inner-background: #09090b; -fx-text-fill: #e2e8f0;");
        descBox.getChildren().addAll(lblDesc, areaDescription);

        // Populate World and Weather combos
        comboWorld.getItems().setAll(worldPresetManager.names());
        if (!comboWorld.getItems().isEmpty()) comboWorld.getSelectionModel().selectFirst();

        comboWeather.getItems().setAll(weatherPresetManager.names());
        if (!comboWeather.getItems().isEmpty()) comboWeather.getSelectionModel().selectFirst();

        // 1 & 2 Section: World & Weather Presets (Top order)
        GridPane gridWorldWeather = new GridPane();
        gridWorldWeather.setHgap(10); gridWorldWeather.setVgap(8);

        Label lbl1World = new Label("1. 🌍 Preset de Monde (Biotope) :");
        lbl1World.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px;");
        lbl1World.setTooltip(new Tooltip("Type de biotope et de relief 3D chargé depuis WorldPresetManager."));

        Label lbl2Weather = new Label("2. 🌤️ Preset Météo & Climat :");
        lbl2Weather.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px;");
        lbl2Weather.setTooltip(new Tooltip("Profil climatique et saisonnier issu de WeatherPresetManager."));

        comboWorld.setMaxWidth(Double.MAX_VALUE);
        comboWeather.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(comboWorld, Priority.ALWAYS);
        GridPane.setHgrow(comboWeather, Priority.ALWAYS);

        gridWorldWeather.add(lbl1World, 0, 0); gridWorldWeather.add(comboWorld, 1, 0);
        gridWorldWeather.add(lbl2Weather, 0, 1); gridWorldWeather.add(comboWeather, 1, 1);

        // 3. Multi-Species Scenario Section Header & Controls
        VBox section3Container = new VBox(8);
        section3Container.setStyle("-fx-background-color: #121214; -fx-padding: 10; -fx-background-radius: 6; -fx-border-color: #27272a; -fx-border-radius: 6;");

        HBox speciesAddBar = new HBox(8);
        speciesAddBar.setAlignment(Pos.CENTER_LEFT);

        Label lbl3SpeciesHeader = new Label("3. 🐜 Espèces & Écosystème du Scénario :");
        lbl3SpeciesHeader.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 12px;");

        comboAvailableSpecies.getItems().setAll(speciesPresetManager.getPresetNames());
        if (!comboAvailableSpecies.getItems().isEmpty()) {
            comboAvailableSpecies.getSelectionModel().selectFirst();
        }
        comboAvailableSpecies.setPrefWidth(260);

        Button btnAddSpeciesToScenario = new Button("➕ Ajouter cette espèce au scénario");
        btnAddSpeciesToScenario.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        btnAddSpeciesToScenario.setTooltip(new Tooltip("Ajoute une nouvelle espèce active au scénario avec son nid filtré, ses accessoires filtrés et sa démographie."));
        btnAddSpeciesToScenario.setOnAction(e -> {
            String selected = comboAvailableSpecies.getValue();
            if (selected != null && !selected.isEmpty()) {
                addSpeciesCard(selected);
            }
        });

        Button btnAutoPreset = new Button("✨ Preset Recommandé");
        btnAutoPreset.setStyle("-fx-background-color: #334155; -fx-text-fill: #e2e8f0; -fx-font-size: 10px;");
        btnAutoPreset.setTooltip(new Tooltip("Applique la configuration recomandée d'espèces et proies pour le scénario courant."));
        btnAutoPreset.setOnAction(e -> applyMetaPreset(comboMeta.getValue()));

        speciesAddBar.getChildren().addAll(lbl3SpeciesHeader, comboAvailableSpecies, btnAddSpeciesToScenario, btnAutoPreset);

        // Add initial default species card (Lasius niger)
        addSpeciesCard("Fourmi Noire des Jardins (Lasius niger)");

        section3Container.getChildren().addAll(speciesAddBar, speciesListContainer);

        // Listener on Meta-Preset selection
        comboMeta.setOnAction(e -> applyMetaPreset(comboMeta.getValue()));

        // Interrupt running simulation if presets change
        Runnable interruptIfRunning = () -> {
            if (isPlaying) {
                isPlaying = false;
                updateButtonStates();
                if (onPause != null) onPause.accept(null);
            }
        };
        comboWorld.setOnAction(e -> interruptIfRunning.run());
        comboWeather.setOnAction(e -> interruptIfRunning.run());

        // Checkpoints & TitledPanes
        VBox checkpointsPane = buildCheckpointsPane();

        // Seed & Limits Row
        HBox seedAndLimitsRow = new HBox(12);
        seedAndLimitsRow.setAlignment(Pos.CENTER_LEFT);

        Label lblSeed = new Label("🎲 Graine Aléatoire (Seed) :");
        lblSeed.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-font-size: 10px;");
        txtSeed.setPrefWidth(90);
        txtSeed.setStyle("-fx-background-color: #0f172a; -fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-border-color: #334155;");

        Button btnRandSeed = new Button("🎲 Nouveau Seed");
        btnRandSeed.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-font-size: 10px;");
        btnRandSeed.setOnAction(e -> txtSeed.setText(String.valueOf((long)(Math.random() * 900000 + 100000))));

        Label lblMaxTicks = new Label("⏱️ Durée Max (Ticks) :");
        lblMaxTicks.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px; -fx-font-weight: bold;");
        maxTicksSpinner.setPrefWidth(100);
        maxTicksSpinner.setEditable(true);

        Label lblMinPop = new Label("🛑 Seuil Arrêt Pop. Min :");
        lblMinPop.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px; -fx-font-weight: bold;");
        minPopStopSpinner.setPrefWidth(70);
        minPopStopSpinner.setEditable(true);

        seedAndLimitsRow.getChildren().addAll(lblSeed, txtSeed, btnRandSeed, new Separator(Orientation.VERTICAL), lblMaxTicks, maxTicksSpinner, new Separator(Orientation.VERTICAL), lblMinPop, minPopStopSpinner);

        // Apply & Start Button with Inline Progress Bar
        Button btnApplyPresets = new Button("⚡ APPLIQUER ET LANCER LA SIMULATION");
        btnApplyPresets.setMaxWidth(Double.MAX_VALUE);
        btnApplyPresets.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 10 16; -fx-background-radius: 5;");
        btnApplyPresets.setTooltip(new Tooltip("Réinitialise la simulation en appliquant le scénario multi-espèces, les nids filtrés, l'écosystème et la graine aléatoire."));

        inlineProgressBar = new ProgressBar(0);
        inlineProgressBar.setMaxWidth(Double.MAX_VALUE);
        inlineProgressBar.setPrefHeight(6);
        inlineProgressBar.setStyle("-fx-accent: #38bdf8;");

        inlineProgressLabel = new Label("Initialisation du scénario...");
        inlineProgressLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 10px;");

        applyProgressBox = new HBox(8, inlineProgressBar, inlineProgressLabel);
        applyProgressBox.setAlignment(Pos.CENTER_LEFT);
        applyProgressBox.setVisible(false);
        applyProgressBox.setManaged(false);

        btnApplyPresets.setOnAction(e -> handleApplyScenario());

        scenarioCard.getChildren().addAll(metaRow, descBox, new Separator(), gridWorldWeather, new Separator(), section3Container, checkpointsPane, seedAndLimitsRow, btnApplyPresets, applyProgressBox);

        // Date/Time Row & Step Size
        HBox dateTimeRow = new HBox(8);
        dateTimeRow.setAlignment(Pos.CENTER);

        lblDateTime = new Label("📅 Date & Heure : 2026-03-20 08:00:00");
        lblDateTime.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px;");

        Label lblStepTitle = new Label("Pas de Simulation :");
        lblStepTitle.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;");

        stepSizeCombo = new ComboBox<>();
        stepSizeCombo.getItems().addAll("16.6 ms (60 Hz)", "50 ms (20 Hz)", "100 ms (10 Hz)", "1.0 s", "5.0 s");
        stepSizeCombo.getSelectionModel().selectFirst();
        stepSizeCombo.setStyle("-fx-font-size: 10px;");
        stepSizeCombo.setOnAction(e -> {
            int idx = stepSizeCombo.getSelectionModel().getSelectedIndex();
            switch (idx) {
                case 0 -> simulationStepSeconds = 0.0166f;
                case 1 -> simulationStepSeconds = 0.05f;
                case 2 -> simulationStepSeconds = 0.1f;
                case 3 -> simulationStepSeconds = 1.0f;
                case 4 -> simulationStepSeconds = 5.0f;
            }
            if (onStepChange != null) onStepChange.accept(simulationStepSeconds);
        });

        dateTimeRow.getChildren().addAll(lblDateTime, new Separator(Orientation.VERTICAL), lblStepTitle, stepSizeCombo);

        // Playback Controls Row
        HBox playbackRow = new HBox(4);
        playbackRow.setAlignment(Pos.CENTER);

        btnRewind = createButton("⏪", i18n.get("control.rewind_tt"));
        btnStepBack = createButton("⏮", i18n.get("control.step_back_tt"));
        btnPlay = createButton("▶", i18n.get("control.play_tt"));
        btnPause = createButton("⏸", i18n.get("control.pause_tt"));
        btnStop = createButton("⏹", i18n.get("control.stop_tt"));
        btnStepForward = createButton("⏭", i18n.get("control.step_fw_tt"));
        btnFastForward = createButton("⏩", i18n.get("control.ff_tt"));

        btnPlay.setOnAction(e -> {
            isPlaying = true;
            updateButtonStates();
            if (onPlay != null) onPlay.accept(null);
        });

        btnPause.setOnAction(e -> {
            isPlaying = false;
            updateButtonStates();
            if (onPause != null) onPause.accept(null);
        });

        btnStop.setOnAction(e -> {
            isPlaying = false;
            updateButtonStates();
            if (onStop != null) onStop.accept(null);
        });

        btnRewind.setOnAction(e -> {
            if (onRewind != null) onRewind.accept(10);
        });

        btnFastForward.setOnAction(e -> setSpeed(Math.min(10f, currentSpeed + 1f)));

        btnStepBack.setOnAction(e -> {
            if (onRewind != null) onRewind.accept(1);
        });

        btnStepForward.setOnAction(e -> {
            if (!isPlaying) {
                currentTick++;
                updateTick(currentTick, Math.max(maxTick, currentTick));
                if (onSeek != null) onSeek.accept(currentTick);
            }
        });

        playbackRow.getChildren().addAll(btnRewind, btnStepBack, btnPlay, btnPause, btnStop, btnStepForward, btnFastForward);

        // Speed Row
        HBox speedRow = new HBox(6);
        speedRow.setAlignment(Pos.CENTER);

        Label lblSpeedLabel = new Label("Vitesse :");
        lblSpeedLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;");

        lblSpeed = new Label("1.0x");
        lblSpeed.setStyle("-fx-text-fill: #4fc3f7; -fx-font-weight: bold; -fx-font-size: 11px;");
        lblSpeed.setPrefWidth(35);

        speedSlider = new Slider(0.1, 10.0, 1.0);
        speedSlider.setShowTickLabels(false);
        speedSlider.setShowTickMarks(false);
        speedSlider.setPrefWidth(80);
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            currentSpeed = newVal.floatValue();
            lblSpeed.setText(String.format("%.1fx", currentSpeed));
            if (onSpeedChange != null) onSpeedChange.accept(currentSpeed);
        });

        Button btnNormal = createSmallButton("1x"); btnNormal.setOnAction(e -> setSpeed(1.0f));
        Button btnDouble = createSmallButton("2x"); btnDouble.setOnAction(e -> setSpeed(2.0f));
        Button btnQuad = createSmallButton("4x"); btnQuad.setOnAction(e -> setSpeed(4.0f));

        speedRow.getChildren().addAll(lblSpeedLabel, speedSlider, lblSpeed, btnNormal, btnDouble, btnQuad);

        playbackAndSpeedPanel.setPadding(new Insets(8));
        playbackAndSpeedPanel.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-padding: 8; -fx-background-radius: 6; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 6;");

        Label lblPlaybackHeader = new Label("⏱️ Contrôles Temps, Vitesse & Lecture");
        lblPlaybackHeader.setStyle("-fx-text-fill: #a78bfa; -fx-font-weight: bold; -fx-font-size: 11px;");

        playbackAndSpeedPanel.getChildren().addAll(lblPlaybackHeader, dateTimeRow, playbackRow, speedRow);

        getChildren().addAll(headerVBox, scenarioCard);
        updateButtonStates();
    }

    private void addSpeciesCard(String speciesName) {
        // Prevent duplicate cards for the exact same species instance unless user wants
        SpeciesConfigCard card = new SpeciesConfigCard(speciesName, () -> {
            speciesCardList.removeIf(c -> c.getSpeciesName().equals(speciesName));
            refreshSpeciesListContainer();
        });
        speciesCardList.add(card);
        refreshSpeciesListContainer();
    }

    private void refreshSpeciesListContainer() {
        speciesListContainer.getChildren().clear();
        for (SpeciesConfigCard card : speciesCardList) {
            speciesListContainer.getChildren().add(card.getCardPane());
        }
    }

    private void handleApplyScenario() {
        if (isPlaying) {
            isPlaying = false;
            updateButtonStates();
            if (onPause != null) onPause.accept(null);
        }
        long seed = getMasterSeed();

        // Show inline loading bar right under the button
        if (applyProgressBox != null) {
            applyProgressBox.setVisible(true);
            applyProgressBox.setManaged(true);
            inlineProgressBar.setProgress(0.2);
            inlineProgressLabel.setText("Initialisation du scénario '" + comboMeta.getValue() + "'...");
        }

        if (onApplyPresets != null) {
            onApplyPresets.accept(seed);
        }

        org.swarmforge.core.event.EventBus.getInstance().publish(
            org.swarmforge.core.event.SimulationEvent.obtain(
                org.swarmforge.core.event.SimulationEvent.EventType.SIMULATION_STARTED,
                org.swarmforge.core.event.SimulationEvent.Severity.INFO,
                0,
                "🌍 Scénario '" + comboMeta.getValue() + "' appliqué (" + speciesCardList.size() + " espèces configurées)",
                null
            )
        );

        // Complete inline progress and hide after a brief moment
        if (applyProgressBox != null) {
            inlineProgressBar.setProgress(1.0);
            inlineProgressLabel.setText("✅ Scénario initialisé avec succès !");
            javafx.animation.PauseTransition hideDelay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(1200));
            hideDelay.setOnFinished(e -> {
                applyProgressBox.setVisible(false);
                applyProgressBox.setManaged(false);
            });
            hideDelay.play();
        }
    }

    private void alignWeatherWithWorld() {
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
        org.swarmforge.client.util.ThemeManager.createAlert(
            Alert.AlertType.INFORMATION,
            "Climat aligné avec succès sur '" + targetWeather + "' selon le biotope et la zone latitudinale de '" + world + "' !"
        ).show();
    }

    private void applyMetaPreset(String metaName) {
        if (metaName == null) return;
        speciesCardList.clear();

        if (metaName.contains("Fondation Claustrale") || metaName.contains("Démarrage")) {
            selectComboIfPresent(comboWorld, "Tempéré Standard (Temperate Forest)");
            selectComboIfPresent(comboWeather, "Temperate");
            areaDescription.setText("Scénario d'initialisation biologique modélisant la fondation solitaire d'une reine claustrale de Lasius niger après le vol nuptial d'été. La reine vit en réclusion souterraine complète et métabolise ses muscles alaires pour nourrir le premier couvain d'ouvrières nanitiques. Nécessite une humidité constante et un sol stable.");
            addSpeciesCard("Fourmi Noire des Jardins (Lasius niger)");
            if (!speciesCardList.isEmpty()) {
                SpeciesConfigCard card = speciesCardList.get(0);
                card.setQueenCount(1);
                card.setWorkerCount(0);
                card.setSoldierCount(0);
            }
        } else if (metaName.contains("Amazonien") || metaName.contains("Atta")) {
            selectComboIfPresent(comboWorld, "Forêt Tropicale (Tropical Rainforest)");
            selectComboIfPresent(comboWeather, "Tropical");
            areaDescription.setText("Scénario d'écosystème néotropical axé sur la symbiose obligée entre la fourmi coupeuse de feuilles Atta sexdens et son champignon Leucoagaricus gongylophorus. Les ouvrières découpent le feuillage pour alimenter les meules souterraines tout en défendant le nid contre les parasites Cordyceps.");
            addSpeciesCard("Fourmi Coupeuse de Feuilles (Atta sexdens)");
        } else if (metaName.contains("Granivore") || metaName.contains("Messor")) {
            selectComboIfPresent(comboWorld, "Désert Aride (Arid Desert)");
            selectComboIfPresent(comboWeather, "Arid");
            areaDescription.setText("Scénario xérique simulant la collecte, le transport et le décorticage de graines par les fourmis moissonneuses Messor barbarus. Les majors aux mandibules puissantes préparent le pain de fourmi stocké dans les chambres greniers sèches du nid.");
            addSpeciesCard("Fourmi Moissonneuse (Pogonomyrmex barbatus)");
        } else if (metaName.contains("Guerre") || metaName.contains("Territoriale")) {
            selectComboIfPresent(comboWorld, "Tempéré Standard (Temperate Forest)");
            selectComboIfPresent(comboWeather, "Temperate");
            areaDescription.setText("Scénario d'affrontement interspécifique direct entre la fourmi de feu invasive Solenopsis invicta et la fourmi noire indigène Lasius niger pour le contrôle territorial des sources de miellat et des proies protéiques.");
            addSpeciesCard("Fourmi de Feu (Solenopsis invicta)");
            addSpeciesCard("Fourmi Noire des Jardins (Lasius niger)");
        } else if (metaName.contains("Rucher") || metaName.contains("Apis")) {
            selectComboIfPresent(comboWorld, "Tempéré Standard (Temperate Forest)");
            selectComboIfPresent(comboWeather, "Temperate");
            areaDescription.setText("Scénario d'organisation d'une ruche d'Apis mellifera en cavité d'arbre. Les butineuses exécutent des danses frétillantes pour indiquer les coordonnées des fleurs nectarifères et maintenir la thermorégulation du couvain.");
            addSpeciesCard("Abeille à Miel (Apis mellifera)");
        } else if (metaName.contains("Termites") || metaName.contains("Macrotermes")) {
            selectComboIfPresent(comboWorld, "Forêt Tropicale (Tropical Rainforest)");
            selectComboIfPresent(comboWeather, "Tropical");
            areaDescription.setText("Scénario modélisant la construction d'une termitière géante cathédrale par Macrotermes bellicosus avec ventilation passive régulant le CO2 et l'humidité requise par leurs champignons digestifs.");
            addSpeciesCard("Termite Souterrain (Reticulitermes flavipes)");
        } else if (metaName.contains("Alpin") || metaName.contains("Hivernale")) {
            selectComboIfPresent(comboWorld, "Tempéré Standard (Temperate Forest)");
            selectComboIfPresent(comboWeather, "Temperate");
            areaDescription.setText("Scénario boréal montagnard avec Formica rufa. La colonie dresse un dôme d'aiguilles de pin orienté au sud pour capter le rayonnement solaire printanier et réchauffer le couvain avant la diapause.");
            addSpeciesCard("Fourmi de Feu (Solenopsis invicta)");
        } else if (metaName.contains("Supercolonie") || metaName.contains("Polycalique")) {
            selectComboIfPresent(comboWorld, "Tempéré Standard (Temperate Forest)");
            selectComboIfPresent(comboWeather, "Temperate");
            areaDescription.setText("Scénario d'organisation polycalique unicoloniale. Plusieurs nids distincts partagent la même signature chimique cuticulaire et coopèrent pacifiquement sans agression inter-nids, échangeant ouvrières et couvain.");
            addSpeciesCard("Fourmi Noire des Jardins (Lasius niger)");
        } else {
            selectComboIfPresent(comboWorld, "Tempéré Standard (Temperate Forest)");
            selectComboIfPresent(comboWeather, "Temperate");
            areaDescription.setText("Scénario général d'écosystème terrestre multi-espèces.");
            addSpeciesCard("Fourmi Noire des Jardins (Lasius niger)");
        }
    }

    private Button createButton(String text, String tooltip) {
        Button btn = new Button(text);
        btn.setStyle("-fx-font-size: 14px; -fx-font-family: 'Segoe UI Emoji', 'Segoe UI Symbol', sans-serif; -fx-background-color: #1e293b; -fx-text-fill: #f8fafc; " +
                "-fx-background-radius: 5; -fx-min-width: 42px; -fx-min-height: 32px; -fx-padding: 4px 8px; -fx-cursor: hand; -fx-border-color: #334155; -fx-border-radius: 5;");
        if (tooltip != null && !tooltip.isEmpty()) btn.setTooltip(new Tooltip(tooltip));
        btn.disabledProperty().addListener((obs, oldV, newV) -> btn.setOpacity(newV ? 0.35 : 1.0));
        return btn;
    }

    private Button createSmallButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-font-size: 10px; -fx-background-color: #334155; -fx-text-fill: #e2e8f0; -fx-background-radius: 3; -fx-padding: 3 8; -fx-cursor: hand;");
        return btn;
    }

    private void updateButtonStates() {
        btnPlay.setDisable(isPlaying);
        btnPause.setDisable(!isPlaying);
        btnRewind.setDisable(isPlaying);
        btnStepBack.setDisable(isPlaying);
        btnStepForward.setDisable(isPlaying);
    }

    public void updateTick(long tick, long maxTick) {
        this.currentTick = tick;
        this.maxTick = maxTick;
        long totalSecondsElapsed = (long) (tick * simulationStepSeconds);
        currentDateTime = startDateTime.plusSeconds(totalSecondsElapsed);

        lblDateTime.setText("📅 Date & Heure : " + currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + String.format(" (Jour %d)", 1 + (totalSecondsElapsed / 86400)));
        if (lblTick != null) {
            double rateTPS = 1.0 / simulationStepSeconds;
            lblTick.setText(String.format("Tick actuel: %d (Cible: %d | Durée: %s | Cadence: %.0f ticks/sec TPS)", tick, maxTick, formatTime(totalSecondsElapsed), rateTPS));
        }
        if (lblTime != null) lblTime.setText(formatTime(totalSecondsElapsed));

        if (timelineSlider != null && !timelineSlider.isValueChanging()) {
            timelineSlider.setMax(Math.max(maxTick, tick + 100));
            timelineSlider.setValue(tick);
        }
    }

    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long hours = minutes / 60;
        return String.format("%d:%02d:%02d", hours, minutes % 60, seconds % 60);
    }

    public void setSpeed(float speed) {
        speedSlider.setValue(speed);
    }

    public void setOnPlay(Consumer<Void> callback) { this.onPlay = callback; }
    public void setOnPause(Consumer<Void> callback) { this.onPause = callback; }
    public void setOnStop(Consumer<Void> callback) { this.onStop = callback; }
    public void setOnSpeedChange(Consumer<Float> callback) { this.onSpeedChange = callback; }
    public void setOnSeek(Consumer<Long> callback) { this.onSeek = callback; }
    public void setOnRewind(Consumer<Integer> callback) { this.onRewind = callback; }
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
    public void setPlaying(boolean playing) {
        this.isPlaying = playing;
        updateButtonStates();
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
        return speciesCardList.isEmpty() ? 150 : speciesCardList.get(0).getWorkerCount();
    }

    public int getSoldierCount() {
        return speciesCardList.isEmpty() ? 20 : speciesCardList.get(0).getSoldierCount();
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

    public VBox getPlaybackAndSpeedPanel() {
        return playbackAndSpeedPanel;
    }

    private void handleSaveScenario() {
        String name = comboMeta.getValue() != null ? comboMeta.getValue() : "Nouveau Scénario";
        TextInputDialog dialog = org.swarmforge.client.util.ThemeManager.createTextInputDialog(name);
        dialog.setTitle("Enregistrer le Preset Scénario");
        dialog.setHeaderText("Nom du preset de scénario :");
        dialog.showAndWait().ifPresent(scenarioName -> {
            if (!scenarioName.trim().isEmpty()) {
                String clean = scenarioName.trim();
                if (!comboMeta.getItems().contains(clean)) comboMeta.getItems().add(clean);
                comboMeta.getSelectionModel().select(clean);
                org.swarmforge.client.util.ThemeManager.createAlert(
                    Alert.AlertType.INFORMATION,
                    "Preset scénario enregistré : " + clean
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
            org.swarmforge.client.util.ThemeManager.createAlert(
                Alert.AlertType.INFORMATION,
                "Configuration exportée sous " + file.getName()
            ).show();
        }
    }

    private void handleImportScenario() {
        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Importer une configuration de scénario");
        chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("JSON Files", "*.json"));
        java.io.File file = chooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            org.swarmforge.client.util.ThemeManager.createAlert(
                Alert.AlertType.INFORMATION,
                "Scénario importé avec succès depuis " + file.getName()
            ).show();
        }
    }

    private VBox buildCheckpointsPane() {
        VBox box = new VBox(6);
        box.setStyle("-fx-background-color: #18181b; -fx-padding: 10; -fx-background-radius: 6; -fx-border-color: #d97706; -fx-border-width: 1;");

        Label lblTitle = new Label("🔖 Points de Contrôle & Journal d'Interventions Mode Divin");
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

        private final ComboBox<String> nestTypeCombo = new ComboBox<>();
        private final ComboBox<String> nestPlacementCombo = new ComboBox<>();
        private final ComboBox<String> nestRelationCombo = new ComboBox<>();
        private final Spinner<Double> posXSpinner = new Spinner<>(-500.0, 500.0, 0.0, 10.0);
        private final Spinner<Double> posZSpinner = new Spinner<>(-500.0, 500.0, 0.0, 10.0);
        private final Spinner<Integer> initialFoodSpinner = new Spinner<>(0, 50000, 500, 50);

        private final Spinner<Integer> queenSpinner = new Spinner<>(0, 50, 1);
        private final Spinner<Integer> workerSpinner = new Spinner<>(0, 10000, 150, 25);
        private final Spinner<Integer> soldierSpinner = new Spinner<>(0, 2000, 20, 5);

        private final ComboBox<ArchitectureType> workerEngineCombo = new ComboBox<>();
        private final ComboBox<ArchitectureType> soldierEngineCombo = new ComboBox<>();
        private final ComboBox<ArchitectureType> queenEngineCombo = new ComboBox<>();

        private final VBox accessoryBoxPane = new VBox(4);

        public SpeciesConfigCard(String speciesName, Runnable onRemove) {
            this.speciesName = speciesName;

            cardPane.setStyle("-fx-background-color: #18181b; -fx-padding: 10; -fx-background-radius: 6; -fx-border-color: #0284c7; -fx-border-width: 1;");

            // Header
            HBox header = new HBox(8);
            header.setAlignment(Pos.CENTER_LEFT);

            Label lblTitle = new Label("🐜 Espèce : " + speciesName);
            lblTitle.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 12px;");

            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            Button btnRemove = new Button("🗑️ Supprimer l'Espèce");
            btnRemove.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-cursor: hand;");
            btnRemove.setOnAction(e -> onRemove.run());

            header.getChildren().addAll(lblTitle, sp, btnRemove);

            // 1. Filtered Nest, Placement & Inter-Nest Strategy
            HBox nestRow = new HBox(8);
            nestRow.setAlignment(Pos.CENTER_LEFT);

            Label lblNest = new Label("🏰 Type de Nid (Filtré pour " + getShortSpeciesName(speciesName) + ") :");
            lblNest.setStyle("-fx-text-fill: #a78bfa; -fx-font-weight: bold; -fx-font-size: 10px;");

            nestTypeCombo.getItems().setAll(getCompatibleNestTypes(speciesName));
            if (!nestTypeCombo.getItems().isEmpty()) nestTypeCombo.getSelectionModel().selectFirst();
            nestTypeCombo.setTooltip(new Tooltip("Architectures de nids adaptées aux capacités et besoins écologiques de cette espèce. Le nid est systématiquement pré-généré à partir de ces paramètres."));

            Label lblPlacement = new Label("📍 Placement :");
            lblPlacement.setStyle("-fx-text-fill: #a78bfa; -fx-font-weight: bold; -fx-font-size: 10px;");

            nestPlacementCombo.getItems().addAll(
                "📍 Centre de la Carte (Optimal hors d'eau)",
                "👑 Fondation Reine (Sol Vierge - Surface)",
                "🎲 Positionnement Aléatoire (Dispersé)",
                "✋ Placement Manuel (Coordonnées X, Z)"
            );
            nestPlacementCombo.getSelectionModel().selectFirst();
            nestPlacementCombo.setTooltip(new Tooltip("Stratégie d'implantation spatiale du nid dans la grille 3D. Pour simuler plusieurs nids, ajoutez à nouveau la même espèce dans le scénario."));

            Label lblInitialFood = new Label("🍖 Nourriture :");
            lblInitialFood.setStyle("-fx-text-fill: #a78bfa; -fx-font-weight: bold; -fx-font-size: 10px;");

            initialFoodSpinner.setPrefWidth(75);
            initialFoodSpinner.setEditable(true);
            initialFoodSpinner.setTooltip(new Tooltip("Réserve initiale de ressources alimentaires attribuée au nid au lancement."));

            nestRow.getChildren().addAll(lblNest, nestTypeCombo, lblPlacement, nestPlacementCombo, lblInitialFood, initialFoodSpinner);

            // Manual Position Box (Visible when manual placement is chosen)
            HBox manualPosBox = new HBox(8);
            manualPosBox.setAlignment(Pos.CENTER_LEFT);
            manualPosBox.setStyle("-fx-padding: 4 0 0 0;");

            Label lblManualX = new Label("Coordonnée X :");
            lblManualX.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 10px;");
            posXSpinner.setPrefWidth(85); posXSpinner.setEditable(true);

            Label lblManualZ = new Label("Coordonnée Z :");
            lblManualZ.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 10px;");
            posZSpinner.setPrefWidth(85); posZSpinner.setEditable(true);

            manualPosBox.getChildren().addAll(lblManualX, posXSpinner, lblManualZ, posZSpinner);
            manualPosBox.setVisible(false);
            manualPosBox.setManaged(false);

            nestPlacementCombo.valueProperty().addListener((o, oldV, newV) -> {
                boolean isManual = newV != null && newV.contains("Manuel");
                manualPosBox.setVisible(isManual);
                manualPosBox.setManaged(isManual);
            });

            // Inter-Nest Relationship Strategy & Supercolony Checkbox Row
            VBox relationBox = new VBox(4);

            HBox relationRow = new HBox(8);
            relationRow.setAlignment(Pos.CENTER_LEFT);

            Label lblRelation = new Label("⚔️ Stratégie Inter-Nids (Même Espèce) :");
            lblRelation.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-font-size: 10px;");

            nestRelationCombo.getItems().addAll(
                "⚔️ Monocoloniale (Compétition & Guerre inter-nids - Reconnaissance par hydrocarbures cuticulaires)",
                "🤝 Supercolonie Unicoloniale (Tolérance, entraide & échange de couvain/ouvrières)",
                "🛡️ Neutralité Territoriale (Évitement passif sans affrontement direct)"
            );
            nestRelationCombo.getSelectionModel().selectFirst();
            nestRelationCombo.setPrefWidth(420);
            nestRelationCombo.setTooltip(new Tooltip("Configure les interactions comportementales si plusieurs nids de la même espèce sont instanciés dans la simulation."));

            relationRow.getChildren().addAll(lblRelation, nestRelationCombo);

            CheckBox chkSupercolonyMember = new CheckBox("🤝 Rejoindre le réseau Supercolonie Polycalique (Coopération, libre passage & partage de couvain entre nids alliés)");
            chkSupercolonyMember.setSelected(false);
            chkSupercolonyMember.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 10px; -fx-font-weight: bold;");
            chkSupercolonyMember.setTooltip(new Tooltip("Cocher cette case pour intégrer cette colonie au réseau coopératif de supercolonie. Les nids dont cette case est décochée restent des colonies indépendantes et autonomes en guerre ou compétition."));

            relationBox.getChildren().addAll(relationRow, chkSupercolonyMember);

            // 2. Demographics & AI Engines Standard Block (Moved before Accessory Species)
            VBox demoBox = new VBox(6);
            demoBox.setStyle("-fx-background-color: #121214; -fx-padding: 8; -fx-background-radius: 6; -fx-border-color: #334155; -fx-border-width: 1;");

            Label lblDemoTitle = new Label("🧠 Démographie & Castes (Reines, Ouvrières, Soldats & Moteurs IA)");
            lblDemoTitle.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px;");

            Label lblSpatialInfo = new Label("ℹ️ Répartition Spatiale des Castes : Les Reines et le Couvain sont déposés au cœur de la chambre royale souterraine. Les Ouvrières sont réparties dans les galeries et chambres de réserve, les Soldats à proximité des accès au nid, et les Forageuses en patrouille à la surface du sol.");
            lblSpatialInfo.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 9.5px; -fx-wrap-text: true;");

            GridPane demoGrid = new GridPane();
            demoGrid.setHgap(8); demoGrid.setVgap(4);

            for (ArchitectureType type : ArchitectureType.values()) {
                workerEngineCombo.getItems().add(type);
                soldierEngineCombo.getItems().add(type);
                queenEngineCombo.getItems().add(type);
            }
            workerEngineCombo.getSelectionModel().select(ArchitectureType.BEHAVIOR_TREE);
            soldierEngineCombo.getSelectionModel().select(ArchitectureType.FUZZY_LOGIC);
            queenEngineCombo.getSelectionModel().select(ArchitectureType.BDI);

            workerSpinner.setPrefWidth(80); workerSpinner.setEditable(true);
            soldierSpinner.setPrefWidth(80); soldierSpinner.setEditable(true);
            queenSpinner.setPrefWidth(80); queenSpinner.setEditable(true);

            demoGrid.add(new Label("👑 Reines :") {{ setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 10px;"); }}, 0, 0); demoGrid.add(queenSpinner, 1, 0); demoGrid.add(new Label("Moteur IA :") {{ setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;"); }}, 2, 0); demoGrid.add(queenEngineCombo, 3, 0);
            demoGrid.add(new Label("🐜 Ouvrières :") {{ setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 10px;"); }}, 0, 1); demoGrid.add(workerSpinner, 1, 1); demoGrid.add(new Label("Moteur IA :") {{ setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;"); }}, 2, 1); demoGrid.add(workerEngineCombo, 3, 1);
            demoGrid.add(new Label("⚔️ Soldats :") {{ setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 10px;"); }}, 0, 2); demoGrid.add(soldierSpinner, 1, 2); demoGrid.add(new Label("Moteur IA :") {{ setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;"); }}, 2, 2); demoGrid.add(soldierEngineCombo, 3, 2);

            demoBox.getChildren().addAll(lblDemoTitle, lblSpatialInfo, demoGrid);

            // 3. Filtered Accessory Species Section (Proies, Prédateurs & Commensaux)
            Label lblAccessoryTitle = new Label("🦗 Espèces Accessoires & Interprétations Écologiques (Filtrées pour " + getShortSpeciesName(speciesName) + ") :");
            lblAccessoryTitle.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 10px;");

            setupAccessoryRows(speciesName);

            cardPane.getChildren().addAll(header, nestRow, manualPosBox, relationBox, new Separator(), demoBox, new Separator(), lblAccessoryTitle, accessoryBoxPane);
        }

        private void setupAccessoryRows(String speciesName) {
            accessoryBoxPane.getChildren().clear();
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

                Label lblRole = new Label("[" + info.role() + "]");
                lblRole.setStyle(getRoleStyle(info.role()));
                lblRole.setPrefWidth(90);

                Label lblCount = new Label("Effectif Initial :");
                lblCount.setStyle("-fx-text-fill: #aaa; -fx-font-size: 9px;");

                Spinner<Integer> countSpinner = new Spinner<>(5, 1000, info.defaultCount(), 10);
                countSpinner.setPrefWidth(70); countSpinner.setEditable(true);

                Label lblStrategy = new Label("Taux Renouvellement :");
                lblStrategy.setStyle("-fx-text-fill: #aaa; -fx-font-size: 9px;");

                ComboBox<String> strategyCombo = new ComboBox<>();
                strategyCombo.getItems().addAll(
                    "🔄 Équilibre Dynamique (Maintien auto à < 30% Pop)",
                    "🌿 Régénération Logistique Saisonnière (10-50%/jour)",
                    "⏳ Épuisement Fini (Ressources / Proies Limitées)",
                    "⚡ Incursions Épisodiques (Pop-in Vagues tous les 500 Ticks)"
                );
                strategyCombo.getSelectionModel().selectFirst();
                strategyCombo.setStyle("-fx-font-size: 9px;");
                strategyCombo.setTooltip(new Tooltip("Stratégie de proposition de renouvellement pour maintenir la pression sans déstabiliser la simulation."));

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

        public VBox getCardPane() { return cardPane; }
        public String getSpeciesName() { return speciesName; }
        public int getQueenCount() { return queenSpinner.getValue(); }
        public void setQueenCount(int count) { queenSpinner.getValueFactory().setValue(count); }
        public int getWorkerCount() { return workerSpinner.getValue(); }
        public void setWorkerCount(int count) { workerSpinner.getValueFactory().setValue(count); }
        public int getSoldierCount() { return soldierSpinner.getValue(); }
        public void setSoldierCount(int count) { soldierSpinner.getValueFactory().setValue(count); }
        public ArchitectureType getWorkerEngine() { return workerEngineCombo.getValue(); }
        public ArchitectureType getSoldierEngine() { return soldierEngineCombo.getValue(); }
        public ArchitectureType getQueenEngine() { return queenEngineCombo.getValue(); }
        public int getInitialFood() { return initialFoodSpinner.getValue(); }
    }

    public record AccessorySpeciesInfo(String name, String role, String description, int defaultCount) {
        public AccessorySpeciesInfo(String name, String role, String description) {
            this(name, role, description, 50);
        }
    }

    /**
     * Helper method to filter nest types based on biological species capability.
     */
    public static List<String> getCompatibleNestTypes(String speciesName) {
        List<String> nests = new ArrayList<>();
        if (speciesName == null) return List.of("Young Ant Burrow (Lasius niger)");
        String lower = speciesName.toLowerCase();
        if (lower.contains("atta") || lower.contains("coupeuse")) {
            nests.add("Leafcutter Fungus Vault (Atta sexdens)");
            nests.add("Mature Ant Burrow (Formica fusca)");
            nests.add("Young Ant Burrow (Lasius niger)");
        } else if (lower.contains("apis") || lower.contains("abeille")) {
            nests.add("Honeybee Wax Comb (Apis mellifera)");
            nests.add("Bumblebee Pot Cluster (Bombus terrestris)");
        } else if (lower.contains("vespula") || lower.contains("guêpe")) {
            nests.add("Paper Wasp Nest (Vespula vulgaris)");
        } else if (lower.contains("reticulitermes") || lower.contains("macrotermes") || lower.contains("termite")) {
            nests.add("Termite Cathedral Mound (Macrotermes bellicosus)");
        } else if (lower.contains("messor") || lower.contains("pogonomyrmex") || lower.contains("moissonneuse")) {
            nests.add("Mature Ant Burrow (Messor barbarus)");
            nests.add("Young Ant Burrow (Lasius niger)");
        } else {
            nests.add("Mature Ant Burrow (Formica fusca)");
            nests.add("Complex Supercolony (Linepithema humile)");
            nests.add("Young Ant Burrow (Lasius niger)");
            nests.add("Leafcutter Fungus Vault (Atta sexdens)");
        }
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
        return list;
    }
}
