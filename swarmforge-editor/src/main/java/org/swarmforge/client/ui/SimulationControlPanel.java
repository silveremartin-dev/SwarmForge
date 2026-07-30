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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Simulation control panel with playback, speed, and timeline controls.
 * Provides VCR-like controls for simulation management.
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
    private final javafx.scene.control.ComboBox<String> stepSizeCombo;

    private final Label lblSpeed;
    private Label lblTick;
    private Label lblTime;
    private final Label lblDateTime;

    private final javafx.scene.control.ComboBox<String> comboMeta = new javafx.scene.control.ComboBox<>();
    private final javafx.scene.control.ComboBox<String> comboWorld = new javafx.scene.control.ComboBox<>();
    private final javafx.scene.control.ComboBox<String> comboSpecies = new javafx.scene.control.ComboBox<>();
    private final javafx.scene.control.ComboBox<String> comboNest = new javafx.scene.control.ComboBox<>();
    private final javafx.scene.control.ComboBox<String> comboPreyPred = new javafx.scene.control.ComboBox<>();
    private final javafx.scene.control.ComboBox<String> comboWeather = new javafx.scene.control.ComboBox<>();
    private final javafx.scene.control.TextField txtSeed = new javafx.scene.control.TextField("12345");

    private final SpeciesPresetManager speciesPresetManager = new SpeciesPresetManager();
    private final WorldPresetManager worldPresetManager = new WorldPresetManager();
    private final WeatherPresetManager weatherPresetManager = new WeatherPresetManager();
    private final NestPresetManager nestPresetManager = new NestPresetManager();

    private boolean isPlaying = false;
    private float currentSpeed = 1.0f;
    private long currentTick = 0;
    private long maxTick = 0;
    private float simulationStepSeconds = 0.016f; // Default 1/60s step
    private java.time.LocalDateTime startDateTime = java.time.LocalDateTime.of(2026, 3, 20, 8, 0, 0);
    private java.time.LocalDateTime currentDateTime = startDateTime;

    // Callbacks
    private Consumer<Void> onPlay;
    private Consumer<Void> onPause;
    private Consumer<Void> onStop;
    private Consumer<Float> onSpeedChange;
    private Consumer<Long> onSeek;
    private Consumer<Integer> onRewind;
    private Consumer<Float> onStepChange;

    /** Get current tick for external state queries. */
    public long getCurrentTick() {
        return currentTick;
    }

    /** Get max tick for external state queries. */
    public long getMaxTick() {
        return maxTick;
    }

    private final VBox playbackAndSpeedPanel = new VBox(8);

    public VBox getPlaybackAndSpeedPanel() {
        return playbackAndSpeedPanel;
    }

    public SimulationControlPanel() {
        setSpacing(8);
        setPadding(new Insets(10));
        setStyle("-fx-border-color: #3f3f46; -fx-border-width: 1 0 0 0;");

        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();

        // === Row 0: Presets Selection (Positioned ABOVE Simulation Controls) ===
        VBox presetBox = new VBox(6);
        presetBox.getStyleClass().add("card-pane");

        HBox headerRow = new HBox(8);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label lblPresetHeader = new Label("🔖 Presets du Scénario & Écosystème (Synchronisés avec les Éditeurs)");
        lblPresetHeader.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px;");
        lblPresetHeader.setTooltip(new Tooltip("Configuration globale du scénario scientifique et chargement dynamique des presets d'espèces, mondes, nids et météo."));

        Region spHeader = new Region();
        HBox.setHgrow(spHeader, Priority.ALWAYS);

        Button bHelp = new Button("📖 Aide & Glossaire");
        bHelp.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
        bHelp.setTooltip(new Tooltip("Ouvrir le glossaire pédagogique de la simulation et des comportements d'essaim."));
        bHelp.setOnAction(e -> GlossaryDialog.show("sim"));

        headerRow.getChildren().addAll(lblPresetHeader, spHeader, bHelp);

        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        Label lblMeta = new Label("★ Scénario (Méta-Preset) :");
        lblMeta.setStyle("-fx-text-fill: #a78bfa; -fx-font-weight: bold; -fx-font-size: 11px;");
        
        comboMeta.getItems().addAll(
            "🌿 Écosystème Amazonien (Atta sexdens & Champignon)",
            "🏜 Steppe Aride Granivore (Messor barbatus & Graminées)",
            "⚔️ Guerre Territoriale (Solenopsis invicta vs Lasius niger)",
            "🐝 Rucher Arboricole (Apis mellifera & Butinage)",
            "🏛️ Cathédrale de Termites (Reticulitermes & Macrotermes)"
        );
        comboMeta.getSelectionModel().selectFirst();
        comboMeta.setMaxWidth(Double.MAX_VALUE);
        comboMeta.setTooltip(new Tooltip("Scénarios pré-configurés cohérents assemblant espèce, nid, biotope et météo adaptés."));
        HBox.setHgrow(comboMeta, Priority.ALWAYS);

        metaRow.getChildren().addAll(lblMeta, comboMeta);

        // Populate individual combo boxes from their respective Preset Managers!
        comboWorld.getItems().setAll(worldPresetManager.names());
        if (!comboWorld.getItems().isEmpty()) comboWorld.getSelectionModel().selectFirst();
        comboWorld.setTooltip(new Tooltip("Type de biotope/terrain chargé depuis WorldPresetManager."));

        comboSpecies.getItems().setAll(speciesPresetManager.getPresetNames());
        if (!comboSpecies.getItems().isEmpty()) comboSpecies.getSelectionModel().selectFirst();
        comboSpecies.setTooltip(new Tooltip("Espèce principale issue de SpeciesPresetManager (contient toutes les castes et paramètres biologiques)."));

        comboNest.getItems().setAll(nestPresetManager.names());
        if (!comboNest.getItems().isEmpty()) comboNest.getSelectionModel().selectFirst();
        comboNest.setTooltip(new Tooltip("Architecture de nid issue de NestPresetManager."));

        comboPreyPred.getItems().addAll(
            "Pucerons & Fourmilion",
            "Incursion Guêpe Solitaire & Araignée",
            "Termites & Guêpe Chasseresse",
            "Ressources Abondantes (Sans prédateurs)"
        );
        comboPreyPred.getSelectionModel().selectFirst();
        comboPreyPred.setTooltip(new Tooltip("Faune auxiliaire et interactions proies/prédateurs."));

        comboWeather.getItems().setAll(weatherPresetManager.names());
        if (!comboWeather.getItems().isEmpty()) comboWeather.getSelectionModel().selectFirst();
        comboWeather.setTooltip(new Tooltip("Profil climatique issu de WeatherPresetManager."));

        // Setup listener for Meta-Presets to auto-select coherent options
        comboMeta.setOnAction(e -> applyMetaPreset(comboMeta.getValue()));

        // Grid for individual presets
        javafx.scene.layout.GridPane gridPresets = new javafx.scene.layout.GridPane();
        gridPresets.setHgap(8);
        gridPresets.setVgap(4);

        // Master Seed Row (Deterministic Replay Integrity)
        HBox seedRow = new HBox(8);
        seedRow.setAlignment(Pos.CENTER_LEFT);
        Label lblSeed = new Label("🎲 Graine Aléatoire (Seed Replay) :");
        lblSeed.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-font-size: 10px;");
        txtSeed.setPrefWidth(90);
        txtSeed.setStyle("-fx-background-color: #0f172a; -fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-border-color: #334155;");
        txtSeed.setTooltip(new Tooltip("Graine pseudo-aléatoire garantissant la réplicabilité exacte de la simulation."));

        Button btnRandSeed = new Button("🎲 Nouveau Seed");
        btnRandSeed.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-font-size: 10px;");
        btnRandSeed.setTooltip(new Tooltip("Génère une nouvelle graine aléatoire."));
        btnRandSeed.setOnAction(e -> txtSeed.setText(String.valueOf((long)(Math.random() * 900000 + 100000))));

        seedRow.getChildren().addAll(lblSeed, txtSeed, btnRandSeed);

        // Apply Button
        Button btnApplyPresets = new Button("⚡ APPLIQUER À LA SIMULATION");
        btnApplyPresets.setMaxWidth(Double.MAX_VALUE);
        btnApplyPresets.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 5;");
        btnApplyPresets.setTooltip(new Tooltip("Réinitialise la simulation en appliquant l'ensemble des presets et la graine déterministe."));
        btnApplyPresets.setOnAction(e -> {
            if (isPlaying) {
                isPlaying = false;
                updateButtonStates();
                if (onPause != null) onPause.accept(null);
            }
            long seed = getMasterSeed();
            if (onApplyPresets != null) {
                onApplyPresets.accept(seed);
            }
            btnApplyPresets.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 5;");
            new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION,
                "Presets et Master Seed (#" + seed + ") appliqués ! Le monde, les colonies et le biotope ont été reconfigurés et réinitialisés avec succès."
            ).show();
        });

        // Preset change listener that pauses simulation if running
        Runnable interruptIfRunning = () -> {
            if (isPlaying) {
                isPlaying = false;
                updateButtonStates();
                if (onPause != null) onPause.accept(null);
            }
            btnApplyPresets.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 5;");
        };

        comboWorld.setOnAction(e -> interruptIfRunning.run());
        comboSpecies.setOnAction(e -> interruptIfRunning.run());
        comboNest.setOnAction(e -> interruptIfRunning.run());
        comboPreyPred.setOnAction(e -> interruptIfRunning.run());
        comboWeather.setOnAction(e -> interruptIfRunning.run());

        javafx.scene.control.ComboBox<String> comboNestPlacement = new javafx.scene.control.ComboBox<>();
        comboNestPlacement.getItems().addAll(
            "📍 Positionnement optimal (Centre carte hors d'eau)",
            "👑 Fondation Reine Seule Claustrale (Sol Vierge)",
            "🎲 Positionnement aléatoire autour du centre (Hors eau)",
            "🏛️ Nids multiples répartis (Cohabitation / Guerre)"
        );
        comboNestPlacement.getSelectionModel().selectFirst();
        comboNestPlacement.setTooltip(new Tooltip("Strategie d'implantation initiale du nid dans le relief 3D."));
        comboNestPlacement.setOnAction(e -> interruptIfRunning.run());

        Label l1 = new Label("🌍 Monde :"); l1.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;");
        Label l2 = new Label("🐜 Espèce(s) :"); l2.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;");
        Label l3 = new Label("🏰 Nid Type :"); l3.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;");
        Label l4 = new Label("📍 Placement Nid :"); l4.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;");
        Label l5 = new Label("🦗 Proies/Prédateurs :"); l5.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;");
        Label l6 = new Label("⛅ Météo/Climat :"); l6.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;");

        gridPresets.add(l1, 0, 0); gridPresets.add(comboWorld, 1, 0);
        gridPresets.add(l2, 2, 0); gridPresets.add(comboSpecies, 3, 0);
        gridPresets.add(l3, 0, 1); gridPresets.add(comboNest, 1, 1);
        gridPresets.add(l4, 2, 1); gridPresets.add(comboNestPlacement, 3, 1);
        gridPresets.add(l5, 0, 2); gridPresets.add(comboPreyPred, 1, 2);
        gridPresets.add(l6, 2, 2); gridPresets.add(comboWeather, 3, 2);

        presetBox.getChildren().addAll(headerRow, metaRow, gridPresets, seedRow, btnApplyPresets);

        // === Row 1: Date/Time Display & Real-Time Status ===
        HBox dateTimeRow = new HBox(8);
        dateTimeRow.setAlignment(Pos.CENTER);

        lblDateTime = new Label("📅 Date & Heure : 2026-03-20 08:00:00");
        lblDateTime.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px;");
        lblDateTime.setTooltip(new Tooltip("Temps biologique simulé calculé à partir du nombre de ticks et de la durée du pas dt."));

        Label lblStepTitle = new Label("Pas de Simulation :");
        lblStepTitle.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;");

        stepSizeCombo = new javafx.scene.control.ComboBox<>();
        stepSizeCombo.getItems().addAll("16.6 ms (60 Hz)", "50 ms (20 Hz)", "100 ms (10 Hz)", "1.0 s", "5.0 s");
        stepSizeCombo.getSelectionModel().selectFirst();
        stepSizeCombo.setStyle("-fx-font-size: 10px;");
        Tooltip stepTt = new Tooltip("Durée du pas de simulation dt (Intervalle de temps physique/biologique calculé par itération/tick).");
        Tooltip.install(stepSizeCombo, stepTt);
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

        // === Row 2: Playback Controls (Rewind, StepBack, Play, Pause, Stop, StepForward, FastForward) ===
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
            if (onPlay != null)
                onPlay.accept(null);
        });

        btnPause.setOnAction(e -> {
            isPlaying = false;
            updateButtonStates();
            if (onPause != null)
                onPause.accept(null);
        });

        btnStop.setOnAction(e -> {
            isPlaying = false;
            updateButtonStates();
            if (onStop != null)
                onStop.accept(null);
        });

        btnRewind.setOnAction(e -> {
            if (onRewind != null)
                onRewind.accept(10);
        });

        btnFastForward.setOnAction(e -> {
            setSpeed(Math.min(10f, currentSpeed + 1f));
        });

        btnStepBack.setOnAction(e -> {
            if (onRewind != null)
                onRewind.accept(1);
        });

        btnStepForward.setOnAction(e -> {
            if (!isPlaying) {
                currentTick++;
                updateTick(currentTick, Math.max(maxTick, currentTick));
                if (onSeek != null) onSeek.accept(currentTick);
            }
        });

        playbackRow.getChildren().addAll(
                btnRewind, btnStepBack, btnPlay, btnPause, btnStop, btnStepForward, btnFastForward);

        // === Row 3: Speed Control & Multipliers ===
        HBox speedRow = new HBox(6);
        speedRow.setAlignment(Pos.CENTER);

        Label lblSpeedLabel = new Label();
        lblSpeedLabel.textProperty().bind(i18n.createStringBinding("control.speed"));
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
            if (onSpeedChange != null)
                onSpeedChange.accept(currentSpeed);
        });

        Button btnNormal = createSmallButton("1x");
        btnNormal.setOnAction(e -> setSpeed(1.0f));

        Button btnDouble = createSmallButton("2x");
        btnDouble.setOnAction(e -> setSpeed(2.0f));

        Button btnQuad = createSmallButton("4x");
        btnQuad.setOnAction(e -> setSpeed(4.0f));

        speedRow.getChildren().addAll(
                lblSpeedLabel, speedSlider, lblSpeed, btnNormal, btnDouble, btnQuad);

        // Populate playbackAndSpeedPanel (to be mounted on Visual View tab above "Lancer Simulation")
        playbackAndSpeedPanel.setPadding(new Insets(8));
        playbackAndSpeedPanel.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-padding: 8; -fx-background-radius: 6; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 6;");

        Label lblPlaybackHeader = new Label("⏱️ Contrôles Temps, Vitesse & Lecture");
        lblPlaybackHeader.setStyle("-fx-text-fill: #a78bfa; -fx-font-weight: bold; -fx-font-size: 11px;");

        playbackAndSpeedPanel.getChildren().addAll(lblPlaybackHeader, dateTimeRow, playbackRow, speedRow);

        // Main layout of SimulationControlPanel contains the Preset and Scenario configuration card
        getChildren().addAll(presetBox);

        updateButtonStates();
    }

    private Button createButton(String text, String tooltip) {
        Button btn = new Button(text);
        btn.setStyle("-fx-font-size: 16; -fx-background-color: #333; -fx-text-fill: white; " +
                "-fx-background-radius: 5; -fx-min-width: 40; -fx-min-height: 35;");
        btn.setTooltip(new Tooltip(tooltip));
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle().replace("#333", "#555")));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace("#555", "#333")));
        return btn;
    }

    private Button createSmallButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-font-size: 11; -fx-background-color: #444; -fx-text-fill: white; " +
                "-fx-background-radius: 3; -fx-padding: 3 8;");
        return btn;
    }

    private void updateButtonStates() {
        btnPlay.setDisable(isPlaying);
        btnPause.setDisable(!isPlaying);
        btnRewind.setDisable(isPlaying);
        btnStepBack.setDisable(isPlaying);
        btnStepForward.setDisable(isPlaying);
    }

    /**
     * Update the current tick and real-time clock display.
     */
    public void updateTick(long tick, long maxTick) {
        this.currentTick = tick;
        this.maxTick = maxTick;

        // Calculate real time progression
        long totalSecondsElapsed = (long) (tick * simulationStepSeconds);
        currentDateTime = startDateTime.plusSeconds(totalSecondsElapsed);

        lblDateTime.setText("📅 Date & Heure : " + currentDateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        if (lblTick != null) lblTick.setText(String.format("Tick: %d (dt: %.3fs)", tick, simulationStepSeconds));
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

    /**
     * Set the speed value programmatically.
     */
    public void setSpeed(float speed) {
        speedSlider.setValue(speed);
    }

    // === Callback Setters ===

    public void setOnPlay(Consumer<Void> callback) {
        this.onPlay = callback;
    }

    public void setOnPause(Consumer<Void> callback) {
        this.onPause = callback;
    }

    public void setOnStop(Consumer<Void> callback) {
        this.onStop = callback;
    }

    public void setOnSpeedChange(Consumer<Float> callback) {
        this.onSpeedChange = callback;
    }

    public void setOnSeek(Consumer<Long> callback) {
        this.onSeek = callback;
    }

    public void setOnRewind(Consumer<Integer> callback) {
        this.onRewind = callback;
    }

    public void setOnStepChange(Consumer<Float> callback) {
        this.onStepChange = callback;
    }

    /**
     * Set playing state (from external source).
     */
    public void setPlaying(boolean playing) {
        this.isPlaying = playing;
        updateButtonStates();
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public String getSelectedWorld() {
        return comboWorld.getValue() != null ? comboWorld.getValue() : "Tempéré Standard (Temperate Forest)";
    }

    public String getSelectedWeather() {
        return comboWeather.getValue() != null ? comboWeather.getValue() : "Temperate";
    }

    public String getSelectedSpecies() {
        return comboSpecies.getValue() != null ? comboSpecies.getValue() : "Fourmi Noire des Jardins (Lasius niger)";
    }

    private void applyMetaPreset(String metaName) {
        if (metaName == null) return;
        if (metaName.contains("Amazonien") || metaName.contains("Atta")) {
            selectComboIfPresent(comboSpecies, "Fourmi Coupeuse de Feuilles (Atta sexdens)");
            selectComboIfPresent(comboWorld, "Forêt Tropicale (Tropical Rainforest)");
            selectComboIfPresent(comboWeather, "Tropical");
            selectComboIfPresent(comboNest, "Leafcutter Fungus Farm (Atta)");
            selectComboIfPresent(comboPreyPred, "Ressources Abondantes (Sans prédateurs)");
        } else if (metaName.contains("Granivore") || metaName.contains("Messor")) {
            selectComboIfPresent(comboSpecies, "Fourmi Moissonneuse (Pogonomyrmex barbatus)");
            selectComboIfPresent(comboWorld, "Désert Aride (Arid Desert)");
            selectComboIfPresent(comboWeather, "Arid");
            selectComboIfPresent(comboNest, "Mature Ant Burrow");
            selectComboIfPresent(comboPreyPred, "Pucerons & Fourmilion");
        } else if (metaName.contains("Guerre") || metaName.contains("Territoriale")) {
            selectComboIfPresent(comboSpecies, "Fourmi de Feu (Solenopsis invicta)");
            selectComboIfPresent(comboWorld, "Tempéré Standard (Temperate Forest)");
            selectComboIfPresent(comboWeather, "Temperate");
            selectComboIfPresent(comboNest, "Complex Supercolony");
            selectComboIfPresent(comboPreyPred, "Incursion Guêpe Solitaire & Araignée");
        } else if (metaName.contains("Rucher") || metaName.contains("Apis")) {
            selectComboIfPresent(comboSpecies, "Abeille à Miel (Apis mellifera)");
            selectComboIfPresent(comboWorld, "Tempéré Standard (Temperate Forest)");
            selectComboIfPresent(comboWeather, "Temperate");
            selectComboIfPresent(comboNest, "Honeybee Wax Comb (Apis)");
            selectComboIfPresent(comboPreyPred, "Ressources Abondantes (Sans prédateurs)");
        } else if (metaName.contains("Termites")) {
            selectComboIfPresent(comboSpecies, "Termite Souterrain (Reticulitermes flavipes)");
            selectComboIfPresent(comboWorld, "Forêt Tropicale (Tropical Rainforest)");
            selectComboIfPresent(comboWeather, "Tropical");
            selectComboIfPresent(comboNest, "Termite Cathedral Mound (Macrotermes)");
            selectComboIfPresent(comboPreyPred, "Termites & Guêpe Chasseresse");
        }
    }

    private Consumer<Long> onApplyPresets;

    public void setOnApplyPresets(Consumer<Long> callback) {
        this.onApplyPresets = callback;
    }

    public long getMasterSeed() {
        try {
            return Long.parseLong(txtSeed.getText().trim());
        } catch (Exception e) {
            return 1337L;
        }
    }

    private void selectComboIfPresent(javafx.scene.control.ComboBox<String> combo, String val) {
        if (combo.getItems().contains(val)) {
            combo.getSelectionModel().select(val);
        } else if (!combo.getItems().isEmpty()) {
            combo.getSelectionModel().selectFirst();
        }
    }
}
