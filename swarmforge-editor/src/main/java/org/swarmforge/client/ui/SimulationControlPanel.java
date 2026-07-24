/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

    private final Label lblSpeed;
    private final Label lblTick;
    private final Label lblTime;

    private boolean isPlaying = false;
    private float currentSpeed = 1.0f;
    private long currentTick = 0;
    private long maxTick = 0;

    // Callbacks
    private Consumer<Void> onPlay;
    private Consumer<Void> onPause;
    private Consumer<Void> onStop;
    private Consumer<Float> onSpeedChange;
    private Consumer<Long> onSeek;
    private Consumer<Integer> onRewind;

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
        setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #444; -fx-border-width: 1 0 0 0;");

        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();

        // === Row 1: Playback Controls ===
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
            // Single step forward - would need simulation support
        });

        playbackRow.getChildren().addAll(
                btnRewind, btnStepBack, btnPlay, btnPause, btnStop, btnStepForward, btnFastForward);

        // === Row 2: Speed Control ===
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

        // === Row 3: Timeline ===
        HBox timelineRow = new HBox(10);
        timelineRow.setAlignment(Pos.CENTER);

        lblTick = new Label(i18n.get("control.tick", 0));
        lblTick.setStyle("-fx-text-fill: #aaa;");
        lblTick.setPrefWidth(100);

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

        // Add all rows
        getChildren().addAll(playbackRow, speedRow, timelineRow);

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
     * Update the current tick display.
     */
    public void updateTick(long tick, long maxTick) {
        this.currentTick = tick;
        this.maxTick = maxTick;

        lblTick.setText("Tick: " + tick);
        lblTime.setText(formatTime(tick));

        if (!timelineSlider.isValueChanging()) {
            timelineSlider.setMax(Math.max(maxTick, tick + 100));
            timelineSlider.setValue(tick);
        }
    }

    private String formatTime(long ticks) {
        long seconds = ticks / 60;
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

    /**
     * Set playing state (from external source).
     */
    public void setPlaying(boolean playing) {
        this.isPlaying = playing;
        updateButtonStates();
    }
}
