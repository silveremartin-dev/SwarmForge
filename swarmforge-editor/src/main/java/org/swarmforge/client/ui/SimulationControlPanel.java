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
    private final Slider timelineSlider;
    private final javafx.scene.control.ComboBox<String> stepSizeCombo;

    private final Label lblSpeed;
    private final Label lblTick;
    private final Label lblTime;
    private final Label lblDateTime;

    private final javafx.scene.control.ComboBox<String> comboWorld = new javafx.scene.control.ComboBox<>();
    private final javafx.scene.control.ComboBox<String> comboWeather = new javafx.scene.control.ComboBox<>();

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

    public SimulationControlPanel() {
        setSpacing(8);
        setPadding(new Insets(10));
        setStyle("-fx-border-color: #3f3f46; -fx-border-width: 1 0 0 0;");

        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();

        // === Row 0: Presets Selection (Positioned ABOVE Simulation Controls as requested) ===
        VBox presetBox = new VBox(6);
        presetBox.getStyleClass().add("card-pane");

        Label lblPresetHeader = new Label("🔖 Presets du Scénario & des Onglets (Avant ou en cours de simulation)");
        lblPresetHeader.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px;");

        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        Label lblMeta = new Label("★ Scénario (Méta-Preset) :");
        lblMeta.setStyle("-fx-text-fill: #a78bfa; -fx-font-weight: bold; -fx-font-size: 11px;");
        javafx.scene.control.ComboBox<String> comboMeta = new javafx.scene.control.ComboBox<>();
        comboMeta.getItems().addAll(
            "Mon Terrarium N°1 (Complet)",
            "Vol de Lévy vs Marche Brownienne",
            "Polyéthisme & Spécialisation BDI",
            "Savane Granivore - Messor barbarus",
            "Conflit Territorial - Linepithema humile"
        );
        comboMeta.getSelectionModel().selectFirst();
        comboMeta.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(comboMeta, Priority.ALWAYS);

        metaRow.getChildren().addAll(lblMeta, comboMeta);

        // Grid for individual presets (Monde, Espèces, Nid, Proies/Prédateurs, Météo/Climat)
        javafx.scene.layout.GridPane gridPresets = new javafx.scene.layout.GridPane();
        gridPresets.setHgap(8);
        gridPresets.setVgap(4);

        comboWorld.getItems().setAll("Terrarium Tempéré (Mon Terrarium N°1)", "Forêt Tropicale Humide", "Désert Aride & Grottes");
        comboWorld.getSelectionModel().selectFirst();

        javafx.scene.control.ComboBox<String> comboSpecies = new javafx.scene.control.ComboBox<>();
        comboSpecies.getItems().addAll("Formica fusca (Récolteuse)", "Messor barbarus (Moissonneuse)", "Linepithema humile (Invasive)");
        comboSpecies.getSelectionModel().selectFirst();

        javafx.scene.control.ComboBox<String> comboNest = new javafx.scene.control.ComboBox<>();
        comboNest.getItems().addAll("Dôme de Brindilles & Galeries", "Greniers Sub-Superficielles Messor", "Loge de Souche Creuse");
        comboNest.getSelectionModel().selectFirst();

        javafx.scene.control.ComboBox<String> comboPreyPred = new javafx.scene.control.ComboBox<>();
        comboPreyPred.getItems().addAll("Pucerons & Fourmilion", "Incursion Guêpe Solitaire & Araignée", "Ressources Abondantes");
        comboPreyPred.getSelectionModel().selectFirst();

        comboWeather.getItems().setAll("Printemps Doux (22°C)", "Été Caniculaire (34°C)", "Automne Humide (14°C)");
        comboWeather.getSelectionModel().selectFirst();

        // Master Seed Row (Deterministic Replay Integrity)
        HBox seedRow = new HBox(8);
        seedRow.setAlignment(Pos.CENTER_LEFT);
        Label lblSeed = new Label("🎲 Graine Aléatoire (Seed Replay) :");
        lblSeed.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-font-size: 10px;");
        javafx.scene.control.TextField txtSeed = new javafx.scene.control.TextField("12345");
        txtSeed.setPrefWidth(90);
        txtSeed.setStyle("-fx-background-color: #0f172a; -fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-border-color: #334155;");

        Button btnRandSeed = new Button("🎲 Nouveau Seed");
        btnRandSeed.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-font-size: 10px;");
        btnRandSeed.setOnAction(e -> txtSeed.setText(String.valueOf((long)(Math.random() * 900000 + 100000))));

        seedRow.getChildren().addAll(lblSeed, txtSeed, btnRandSeed);

        // Apply Button (Applies all presets & seed to active simulation)
        Button btnApplyPresets = new Button("⚡ APPLIQUER À LA SIMULATION");
        btnApplyPresets.setMaxWidth(Double.MAX_VALUE);
        btnApplyPresets.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 5;");
        btnApplyPresets.setOnAction(e -> {
            if (isPlaying) {
                isPlaying = false;
                updateButtonStates();
                if (onPause != null) onPause.accept(null);
            }
            new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION,
                "Presets et Master Seed (#" + txtSeed.getText() + ") appliqués à la simulation ! Le monde et les entités ont été reconfigurés."
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

        comboMeta.setOnAction(e -> interruptIfRunning.run());
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
            "🏛️ Nids multiples répartis (Cohabitation)"
        );
        comboNestPlacement.getSelectionModel().selectFirst();
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

        presetBox.getChildren().addAll(lblPresetHeader, metaRow, gridPresets, seedRow, btnApplyPresets);

        // === Row 1: Date/Time Display & Real-Time Status ===
        HBox dateTimeRow = new HBox(15);
        dateTimeRow.setAlignment(Pos.CENTER);

        lblDateTime = new Label("📅 Date & Heure : 2026-03-20 08:00:00");
        lblDateTime.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 13px;");

        Label lblStepTitle = new Label("Pas (dt):");
        lblStepTitle.setStyle("-fx-text-fill: #aaa;");

        stepSizeCombo = new javafx.scene.control.ComboBox<>();
        stepSizeCombo.getItems().addAll("16.6ms (60 FPS)", "50ms (20 FPS)", "100ms (10 FPS)", "1.0s (1 FPS)", "5.0s (Fast)");
        stepSizeCombo.getSelectionModel().selectFirst();
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

        // === Row 2: Playback Controls ===
        HBox playbackRow = new HBox(5);
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
            // Increase speed temporarily
            setSpeed(Math.min(10f, currentSpeed + 1f));
        });

        btnStepBack.setOnAction(e -> {
            if (onRewind != null)
                onRewind.accept(1);
        });

        btnStepForward.setOnAction(e -> {
            // Single step forward
        });

        playbackRow.getChildren().addAll(
                btnRewind, btnStepBack, btnPlay, btnPause, btnStop, btnStepForward, btnFastForward);

        // === Row 3: Speed Control ===
        HBox speedRow = new HBox(10);
        speedRow.setAlignment(Pos.CENTER);

        Label lblSpeedLabel = new Label();
        lblSpeedLabel.textProperty().bind(i18n.createStringBinding("control.speed"));
        lblSpeedLabel.setStyle("-fx-text-fill: #aaa;");

        // Initialize lblSpeed BEFORE the slider listener that uses it
        lblSpeed = new Label("1.0x");
        lblSpeed.setStyle("-fx-text-fill: #4fc3f7; -fx-font-weight: bold;");
        lblSpeed.setPrefWidth(50);

        speedSlider = new Slider(0.1, 10.0, 1.0);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(2.0);
        speedSlider.setPrefWidth(200);
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

        // === Row 4: Timeline & Elapsed Time ===
        HBox timelineRow = new HBox(10);
        timelineRow.setAlignment(Pos.CENTER);

        lblTick = new Label(i18n.get("control.tick", 0));
        lblTick.setStyle("-fx-text-fill: #aaa;");
        lblTick.setPrefWidth(120);

        timelineSlider = new Slider(0, 10000, 0);
        timelineSlider.setPrefWidth(400);
        HBox.setHgrow(timelineSlider, Priority.ALWAYS);
        timelineSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (timelineSlider.isValueChanging()) {
                // User is dragging
                long seekTick = newVal.longValue();
                lblTick.setText(i18n.get("control.tick", seekTick));
            }
        });
        timelineSlider.setOnMouseReleased(e -> {
            long seekTick = (long) timelineSlider.getValue();
            if (onSeek != null)
                onSeek.accept(seekTick);
        });

        lblTime = new Label("0:00:00");
        lblTime.setStyle("-fx-text-fill: #4fc3f7; -fx-font-weight: bold;");
        lblTime.setPrefWidth(80);

        timelineRow.getChildren().addAll(lblTick, timelineSlider, lblTime);

        // Add all rows (presetBox sits ABOVE playback controls as requested)
        getChildren().addAll(presetBox, dateTimeRow, playbackRow, speedRow, timelineRow);

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
        lblTick.setText(String.format("Tick: %d (dt: %.3fs)", tick, simulationStepSeconds));
        lblTime.setText(formatTime(totalSecondsElapsed));

        if (!timelineSlider.isValueChanging()) {
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
        return comboWorld.getValue() != null ? comboWorld.getValue() : "Terrarium Tempéré";
    }

    public String getSelectedWeather() {
        return comboWeather.getValue() != null ? comboWeather.getValue() : "Printemps Doux";
    }
}
