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
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.swarmforge.core.behavior.ReasoningArchitecture.ArchitectureType;
import org.swarmforge.core.scenario.AcademicScenarios;
import org.swarmforge.core.scenario.Scenario;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Scenario Editor Pane for SwarmForge Studio.
 * Master composition editor aggregating world, climate, species, initial demographics,
 * caste behavior engine mapping, scheduled events, and academic metrics for simulation runs.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ScenarioEditorPane extends BorderPane {

    private final ScenarioPresetManager presetManager = new ScenarioPresetManager();
    private Scenario currentScenario;

    // UI Components
    private ComboBox<String> presetCombo;
    private TextField titleField;
    private TextArea descriptionArea;
    private TextField academicCategoryField;
    private TextField masterSeedField;
    private Spinner<Long> maxTicksSpinner;
    private Spinner<Integer> minPopStopSpinner;

    // World & Biome
    private Spinner<Integer> widthSpinner, heightSpinner, depthSpinner;
    private ComboBox<String> biomeCombo;
    private Slider soilDensitySlider;

    // Climate
    private Slider temperatureSlider, humiditySlider;
    private CheckBox dayNightCheck;

    // Demographics & Behavioral Engine Selectors
    private TextField speciesNameField;
    private Spinner<Integer> queenSpinner, workerSpinner, soldierSpinner, initialFoodSpinner;
    private ComboBox<ArchitectureType> workerEngineCombo, soldierEngineCombo, queenEngineCombo;

    // Resources
    private Spinner<Integer> foodPatchesSpinner, aphidColoniesSpinner, seedPlantsSpinner, preySpawnersSpinner;

    // Scheduled Events ListView
    private ListView<String> eventsListView;

    // Academic Target Metrics CheckBoxes
    private CheckBox metricForagingCheck, metricBifurcationCheck, metricTaskEntropyCheck;
    private CheckBox metricFractalCheck, metricTerritorialCheck, metricBroodCheck;

    // Callbacks
    private Consumer<Scenario> onLaunchCallback;

    public ScenarioEditorPane() {
        this.currentScenario = AcademicScenarios.createLevyVsBrownianScenario(42L);
        setTop(buildHeader());
        setLeft(buildConfig());
        setCenter(buildPreview());
        loadScenarioToUI(currentScenario);
    }

    private VBox buildHeader() {
        VBox v = new VBox(6);
        v.setPadding(new Insets(8, 12, 6, 12));
        v.setStyle("-fx-background-color: #18181b; -fx-border-color: #27272a; -fx-border-width: 0 0 1 0;");

        HBox r = new HBox(10);
        r.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("🎬 Éditeur de Scénario");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        presetCombo = new ComboBox<>();
        presetCombo.setPromptText("Sélectionner un scénario preset...");
        presetCombo.getItems().addAll(presetManager.getPresetNames());
        presetCombo.setOnAction(e -> {
            String selected = presetCombo.getValue();
            if (selected != null && presetManager.get(selected) != null) {
                this.currentScenario = presetManager.get(selected);
                loadScenarioToUI(currentScenario);
            }
        });

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button bNew = new Button("➕ Nouveau Scénario");
        bNew.getStyleClass().add("btn-secondary");
        bNew.setOnAction(e -> createNewScenario());

        Button bSave = new Button("💾 Sauvegarder Scénario");
        bSave.getStyleClass().add("btn-secondary");
        bSave.setOnAction(e -> saveCurrentScenario());

        Button bLaunch = new Button("🚀 Lancer la Simulation");
        bLaunch.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold;");
        bLaunch.setOnAction(e -> triggerLaunch());

        r.getChildren().addAll(title, new Label("Preset:"), presetCombo, sp, bNew, bSave, new Separator(Orientation.VERTICAL), bLaunch);

        Label subtitle = new Label("Configuration globale de simulation : graines déterministes, distribution des castes, moteurs d'IA et métriques académiques.");
        subtitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        v.getChildren().addAll(r, subtitle);
        return v;
    }

    private ScrollPane buildConfig() {
        VBox cfg = new VBox(12);
        cfg.setPadding(new Insets(10));
        cfg.setPrefWidth(360);
        cfg.setStyle("-fx-background-color: #121214;");

        Accordion accordion = new Accordion();

        TitledPane paneMeta = new TitledPane("📋 1. Métadonnées & Exécution", buildMetaBlock());
        TitledPane paneWorld = new TitledPane("🌍 2. Monde & Substrat du Sol", buildWorldBlock());
        TitledPane paneClimate = new TitledPane("🌤️ 3. Climat & Photopériode", buildClimateBlock());
        TitledPane paneDemo = new TitledPane("🐜 4. Colonie & Moteurs d'IA par Caste", buildDemoBlock());
        TitledPane paneRes = new TitledPane("🌱 5. Ressources & Écosystème", buildResBlock());
        TitledPane paneEvents = new TitledPane("⚡ 6. Événements Programmés", buildEventsBlock());
        TitledPane paneMetrics = new TitledPane("📊 7. Télémétrie Académique", buildMetricsBlock());

        accordion.getPanes().addAll(paneMeta, paneWorld, paneClimate, paneDemo, paneRes, paneEvents, paneMetrics);
        accordion.setExpandedPane(paneMeta);

        cfg.getChildren().add(accordion);

        ScrollPane sc = new ScrollPane(cfg);
        sc.setFitToWidth(true);
        sc.setPrefWidth(380);
        return sc;
    }

    private VBox buildMetaBlock() {
        titleField = new TextField();
        descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setWrapText(true);

        academicCategoryField = new TextField("Éthologie / Swarm Intelligence");
        masterSeedField = new TextField("42");

        maxTicksSpinner = new Spinner<>(1000L, 1_000_000L, 100_000L, 5000L);
        maxTicksSpinner.setEditable(true);

        minPopStopSpinner = new Spinner<>(0, 1000, 0, 5);

        return new VBox(6,
                new Label("Titre du Scénario :"), titleField,
                new Label("Description Scientifique :"), descriptionArea,
                new Label("Domaine Académique :"), academicCategoryField,
                new Label("Graine Aléatoire (Master Seed) :"), masterSeedField,
                new Label("Durée Max (Ticks) :"), maxTicksSpinner,
                new Label("Seuil Arrêt Population Min :"), minPopStopSpinner
        );
    }

    private VBox buildWorldBlock() {
        widthSpinner = new Spinner<>(64, 1024, 256, 32);
        heightSpinner = new Spinner<>(64, 1024, 256, 32);
        depthSpinner = new Spinner<>(16, 256, 64, 16);

        biomeCombo = new ComboBox<>();
        biomeCombo.getItems().addAll("TEMPERATE_FOREST", "ARID_SAVANNA", "TROPICAL_RAINFOREST", "MEDITERRANEAN_SCRUB", "SUBTERRANEAN_CAVE");
        biomeCombo.getSelectionModel().selectFirst();

        soilDensitySlider = new Slider(0.1, 1.0, 0.6);
        soilDensitySlider.setShowTickLabels(true);

        return new VBox(6,
                new Label("Biome :"), biomeCombo,
                new Label("Dimensions (Largeur x Hauteur x Profondeur) :"),
                new HBox(6, widthSpinner, heightSpinner, depthSpinner),
                new Label("Compacité du Sol :"), soilDensitySlider
        );
    }

    private VBox buildClimateBlock() {
        temperatureSlider = new Slider(-10.0, 45.0, 22.0);
        humiditySlider = new Slider(0.0, 1.0, 0.65);
        dayNightCheck = new CheckBox("Cycle Jour / Nuit dynamique");
        dayNightCheck.setSelected(true);

        return new VBox(6,
                new Label("Température Initiale (°C) :"), temperatureSlider,
                new Label("Hygrométrie Initiale :"), humiditySlider,
                dayNightCheck
        );
    }

    private VBox buildDemoBlock() {
        speciesNameField = new TextField("Formica fusca");
        queenSpinner = new Spinner<>(1, 20, 1);
        workerSpinner = new Spinner<>(10, 5000, 150, 25);
        soldierSpinner = new Spinner<>(0, 1000, 20, 5);
        initialFoodSpinner = new Spinner<>(0, 10000, 200, 50);

        workerEngineCombo = new ComboBox<>();
        soldierEngineCombo = new ComboBox<>();
        queenEngineCombo = new ComboBox<>();

        for (ArchitectureType type : ArchitectureType.values()) {
            workerEngineCombo.getItems().add(type);
            soldierEngineCombo.getItems().add(type);
            queenEngineCombo.getItems().add(type);
        }

        workerEngineCombo.getSelectionModel().select(ArchitectureType.BEHAVIOR_TREE);
        soldierEngineCombo.getSelectionModel().select(ArchitectureType.FUZZY_LOGIC);
        queenEngineCombo.getSelectionModel().select(ArchitectureType.BDI);

        GridPane grid = new GridPane();
        grid.setHgap(6); grid.setVgap(6);

        grid.add(new Label("Caste Ouvrières :"), 0, 0); grid.add(workerSpinner, 1, 0); grid.add(workerEngineCombo, 2, 0);
        grid.add(new Label("Caste Soldats :"), 0, 1); grid.add(soldierSpinner, 1, 1); grid.add(soldierEngineCombo, 2, 1);
        grid.add(new Label("Reines :"), 0, 2); grid.add(queenSpinner, 1, 2); grid.add(queenEngineCombo, 2, 2);

        return new VBox(8,
                new Label("Espèce Principale :"), speciesNameField,
                new Label("Répartition des Castes & Moteurs d'IA :"), grid,
                new Label("Stock de Nourriture Initial :"), initialFoodSpinner
        );
    }

    private VBox buildResBlock() {
        foodPatchesSpinner = new Spinner<>(0, 100, 10);
        aphidColoniesSpinner = new Spinner<>(0, 50, 2);
        seedPlantsSpinner = new Spinner<>(0, 100, 15);
        preySpawnersSpinner = new Spinner<>(0, 20, 3);

        return new VBox(6,
                new Label("Sources de Nourriture :"), foodPatchesSpinner,
                new Label("Colonies de Pucerons :"), aphidColoniesSpinner,
                new Label("Plantes à Graines (Graminées) :"), seedPlantsSpinner,
                new Label("Générateurs de Proies Móviles :"), preySpawnersSpinner
        );
    }

    private VBox buildEventsBlock() {
        eventsListView = new ListView<>();
        eventsListView.setPrefHeight(100);

        Button bAddEvent = new Button("➕ Ajouter Événement");
        bAddEvent.setOnAction(e -> {
            eventsListView.getItems().add("Tick 20000 : Vague de Chaleur (+10°C)");
        });

        return new VBox(6,
                new Label("Événements Environnementaux Programmés :"),
                eventsListView,
                bAddEvent
        );
    }

    private VBox buildMetricsBlock() {
        metricForagingCheck = new CheckBox("FORAGING_EFFICIENCY_INDEX (Efficacité de récolte)"); metricForagingCheck.setSelected(true);
        metricBifurcationCheck = new CheckBox("TRAIL_BIFURCATION_COUNT (Choix des pistes)"); metricBifurcationCheck.setSelected(true);
        metricTaskEntropyCheck = new CheckBox("TASK_ALLOCATION_ENTROPY (Division du travail)"); metricTaskEntropyCheck.setSelected(true);
        metricFractalCheck = new CheckBox("TUNNEL_FRACTAL_DIMENSION (Architecture du nid)"); metricFractalCheck.setSelected(true);
        metricTerritorialCheck = new CheckBox("TERRITORIAL_DOMINANCE_RATIO (Dominance)"); metricTerritorialCheck.setSelected(false);
        metricBroodCheck = new CheckBox("BROOD_SURVIVAL_RATE (Survie du couvain)"); metricBroodCheck.setSelected(true);

        return new VBox(6,
                new Label("Métriques & Télémétrie Académique à Enregistrer :"),
                metricForagingCheck, metricBifurcationCheck, metricTaskEntropyCheck,
                metricFractalCheck, metricTerritorialCheck, metricBroodCheck
        );
    }

    private VBox buildPreview() {
        VBox v = new VBox(10);
        v.setPadding(new Insets(16));
        v.setStyle("-fx-background-color: #09090b;");

        Label title = new Label("📊 Aperçu de la Configuration du Scénario");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        TextArea summaryText = new TextArea();
        summaryText.setEditable(false);
        summaryText.setWrapText(true);
        VBox.setVgrow(summaryText, Priority.ALWAYS);
        summaryText.setStyle("-fx-control-inner-background: #18181b; -fx-text-fill: #e4e4e7; -fx-font-family: monospace;");
        summaryText.setText(generateSummaryText());

        v.getChildren().addAll(title, summaryText);
        return v;
    }

    private String generateSummaryText() {
        if (currentScenario == null) return "Aucun scénario chargé.";
        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("  SWARMFORGE SCENARIO SPECIFICATION\n");
        sb.append("====================================================\n\n");
        sb.append("Scénario        : ").append(currentScenario.getTitle()).append("\n");
        sb.append("Catégorie       : ").append(currentScenario.getAcademicCategory()).append("\n");
        sb.append("Seed Déterministe: ").append(currentScenario.getMasterSeed()).append("\n");
        sb.append("Biome           : ").append(currentScenario.getBiomeName()).append("\n");
        sb.append("Dimensions Sol  : ").append(currentScenario.getWidth()).append("x")
          .append(currentScenario.getHeight()).append("x").append(currentScenario.getDepth()).append(" voxels\n\n");
        sb.append("Colonies Initiales : ").append(currentScenario.getColonies().size()).append("\n");
        for (Scenario.ColonySetup c : currentScenario.getColonies()) {
            sb.append("  - ").append(c.speciesName()).append(" (ID: ").append(c.colonyId()).append(")\n");
            sb.append("    Queens: ").append(c.queenCount()).append(" | Workers: ").append(c.workerCount()).append("\n");
            sb.append("    Moteurs d'IA: ").append(c.casteEngineMap()).append("\n");
        }
        sb.append("\nMétriques Académiques Cibles:\n");
        for (String m : currentScenario.getTargetMetrics()) {
            sb.append("  [✓] ").append(m).append("\n");
        }
        return sb.toString();
    }

    private void loadScenarioToUI(Scenario scenario) {
        if (scenario == null) return;
        titleField.setText(scenario.getTitle());
        descriptionArea.setText(scenario.getDescription());
        academicCategoryField.setText(scenario.getAcademicCategory());
        masterSeedField.setText(String.valueOf(scenario.getMasterSeed()));

        biomeCombo.setValue(scenario.getBiomeName());
        widthSpinner.getValueFactory().setValue(scenario.getWidth());
        heightSpinner.getValueFactory().setValue(scenario.getHeight());
        depthSpinner.getValueFactory().setValue(scenario.getDepth());
        soilDensitySlider.setValue(scenario.getSoilDensity());

        temperatureSlider.setValue(scenario.getInitialTemperature());
        humiditySlider.setValue(scenario.getInitialHumidity());
        dayNightCheck.setSelected(scenario.isDayNightCycleEnabled());

        if (!scenario.getColonies().isEmpty()) {
            Scenario.ColonySetup setup = scenario.getColonies().get(0);
            speciesNameField.setText(setup.speciesName());
            queenSpinner.getValueFactory().setValue(setup.queenCount());
            workerSpinner.getValueFactory().setValue(setup.workerCount());
            if (setup.casteEngineMap().containsKey("WORKER")) {
                workerEngineCombo.setValue(setup.casteEngineMap().get("WORKER"));
            }
            if (setup.casteEngineMap().containsKey("QUEEN")) {
                queenEngineCombo.setValue(setup.casteEngineMap().get("QUEEN"));
            }
        }
    }

    private void createNewScenario() {
        this.currentScenario = new Scenario("CUSTOM_" + System.currentTimeMillis(), "Nouveau Scénario Personnalisé", "Description du scénario");
        loadScenarioToUI(currentScenario);
    }

    private void saveCurrentScenario() {
        if (currentScenario == null) return;
        currentScenario.setTitle(titleField.getText());
        currentScenario.setDescription(descriptionArea.getText());
        currentScenario.setAcademicCategory(academicCategoryField.getText());
        try {
            currentScenario.setMasterSeed(Long.parseLong(masterSeedField.getText().trim()));
        } catch (Exception ignored) {}
        presetManager.save(currentScenario);
    }

    private void triggerLaunch() {
        if (onLaunchCallback != null && currentScenario != null) {
            onLaunchCallback.accept(currentScenario);
        }
    }

    public void setOnLaunchCallback(Consumer<Scenario> callback) {
        this.onLaunchCallback = callback;
    }
}
