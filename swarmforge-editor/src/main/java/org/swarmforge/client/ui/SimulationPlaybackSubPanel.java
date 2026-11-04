/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.core.simulation.SimulationCheckpoint;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Helper class encapsulating playback transport controls, auto-repeat button wiring,
 * speed slider controls, start date/time controls, and checkpoint selection UI
 * for {@link SimulationControlPanel}.
 */
class SimulationPlaybackSubPanel {

    private final SimulationControlPanel panel;
    private final I18nManager i18n = I18nManager.getInstance();
    private final SimulationCheckpointsSubPanel checkpointsSubPanel;

    SimulationPlaybackSubPanel(SimulationControlPanel panel) {
        this.panel = panel;
        this.checkpointsSubPanel = new SimulationCheckpointsSubPanel(panel);
    }

    HBox buildStartDateTimeBox() {
        panel.startDatePicker = new DatePicker(panel.startDateTime.toLocalDate());
        panel.startDatePicker.setPrefWidth(125);
        panel.startDatePicker.setStyle("-fx-font-size: 11px;");

        panel.startTimeHourSpinner = new Spinner<>(0, 23, panel.startDateTime.getHour());
        panel.startTimeHourSpinner.setPrefWidth(55);
        panel.startTimeHourSpinner.setEditable(true);
        panel.startTimeHourSpinner.setStyle("-fx-font-size: 10px;");

        panel.startTimeMinuteSpinner = new Spinner<>(0, 59, panel.startDateTime.getMinute());
        panel.startTimeMinuteSpinner.setPrefWidth(55);
        panel.startTimeMinuteSpinner.setEditable(true);
        panel.startTimeMinuteSpinner.setStyle("-fx-font-size: 10px;");

        panel.startTimeSecondSpinner = new Spinner<>(0, 59, panel.startDateTime.getSecond());
        panel.startTimeSecondSpinner.setPrefWidth(55);
        panel.startTimeSecondSpinner.setEditable(true);
        panel.startTimeSecondSpinner.setStyle("-fx-font-size: 10px;");

        Runnable syncStartDateTimeFromUI = () -> {
            if (panel.startDatePicker.getValue() != null) {
                int h = panel.startTimeHourSpinner.getValue() != null ? Math.max(0, Math.min(23, panel.startTimeHourSpinner.getValue())) : 0;
                int m = panel.startTimeMinuteSpinner.getValue() != null ? Math.max(0, Math.min(59, panel.startTimeMinuteSpinner.getValue())) : 0;
                int s = panel.startTimeSecondSpinner.getValue() != null ? Math.max(0, Math.min(59, panel.startTimeSecondSpinner.getValue())) : 0;
                panel.startDateTime = LocalDateTime.of(panel.startDatePicker.getValue(), LocalTime.of(h, m, s));
                panel.updateTick(panel.currentTick, panel.maxTick);
                if (panel.onStartDateTimeChange != null) {
                    panel.onStartDateTimeChange.accept(panel.startDateTime);
                }
            }
        };

        panel.startDatePicker.setOnAction(e -> syncStartDateTimeFromUI.run());
        panel.startTimeHourSpinner.valueProperty().addListener((o, oldV, newV) -> syncStartDateTimeFromUI.run());
        panel.startTimeMinuteSpinner.valueProperty().addListener((o, oldV, newV) -> syncStartDateTimeFromUI.run());
        panel.startTimeSecondSpinner.valueProperty().addListener((o, oldV, newV) -> syncStartDateTimeFromUI.run());

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
            panel.startDatePicker,
            panel.startTimeHourSpinner, lblH,
            panel.startTimeMinuteSpinner, lblM,
            panel.startTimeSecondSpinner, lblS
        );
        startDateTimeBox.setAlignment(Pos.CENTER_LEFT);
        return startDateTimeBox;
    }

    void populatePlaybackAndSpeedPanel() {
        HBox dateTimeRow = new HBox(8);
        dateTimeRow.setAlignment(Pos.CENTER_LEFT);

        panel.lblDateTime = new Label("📅 Date & Heure : 2026-03-20 08:00:00 (Jour 1)");
        panel.lblDateTime.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        panel.lblDateTime.getStyleClass().add("accent-title");
        panel.lblDateTime.setTooltip(new Tooltip("Timestamp and simulated nycthemeral cycle (date, time, and day)."));
        dateTimeRow.getChildren().add(panel.lblDateTime);

        HBox playbackRow1 = new HBox(4);
        playbackRow1.setAlignment(Pos.CENTER);

        panel.btnGoToBeginning = SimulationButtonHelper.createIconButton(Feather.SKIP_BACK, "sim.btn.beginning.tt");
        panel.btnRewind = SimulationButtonHelper.createIconButton(Feather.REWIND, "sim.btn.rewind.tt");
        panel.btnStepBack = SimulationButtonHelper.createIconButton(Feather.CHEVRON_LEFT, "sim.btn.stepback.tt");
        panel.btnPlay = SimulationButtonHelper.createIconButton(Feather.PLAY, "sim.btn.play.tt");
        panel.btnStepForward = SimulationButtonHelper.createIconButton(Feather.CHEVRON_RIGHT, "sim.btn.stepforward.tt");
        panel.btnFastForward = SimulationButtonHelper.createIconButton(Feather.FAST_FORWARD, "sim.btn.fastforward.tt");
        panel.btnGoToEnd = SimulationButtonHelper.createIconButton(Feather.SKIP_FORWARD, "sim.btn.gotoend.tt");

        SimulationButtonHelper.setupAutoRepeat(panel.btnGoToBeginning, this::doGoToBeginning);
        SimulationButtonHelper.setupAutoRepeat(panel.btnRewind, this::doRewind);
        SimulationButtonHelper.setupAutoRepeat(panel.btnStepBack, this::doStepBack);
        SimulationButtonHelper.setupAutoRepeat(panel.btnPlay, this::doTogglePlayPause);
        SimulationButtonHelper.setupAutoRepeat(panel.btnStepForward, this::doStepForward);
        SimulationButtonHelper.setupAutoRepeat(panel.btnFastForward, this::doFastForward);
        SimulationButtonHelper.setupAutoRepeat(panel.btnGoToEnd, this::doGoToEnd);

        playbackRow1.getChildren().addAll(
            panel.btnGoToBeginning, panel.btnRewind, panel.btnStepBack,
            panel.btnPlay, panel.btnStepForward, panel.btnFastForward, panel.btnGoToEnd
        );

        HBox speedSliderRow = new HBox(8);
        speedSliderRow.setAlignment(Pos.CENTER);

        Label lblSpeedLabel = new Label();
        lblSpeedLabel.textProperty().bind(i18n.createStringBinding("sim.speed.label"));
        lblSpeedLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
        lblSpeedLabel.getStyleClass().add("sub-title-gray");

        panel.lblSpeed = new Label("1.0x");
        panel.lblSpeed.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        panel.lblSpeed.getStyleClass().add("accent-title");
        panel.lblSpeed.setPrefWidth(55);

        panel.speedSlider = new Slider(0.1, 20.0, 1.0);
        panel.speedSlider.setShowTickLabels(false);
        panel.speedSlider.setShowTickMarks(false);
        panel.speedSlider.setPrefWidth(120);
        panel.speedSlider.tooltipProperty().bind(i18n.createTooltipBinding("sim.speed.slider.tt"));

        Button btnMaxSpeed = new Button();
        btnMaxSpeed.textProperty().bind(i18n.createStringBinding("sim.btn.max_speed"));
        btnMaxSpeed.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 4 10 4 10;");
        btnMaxSpeed.setMinWidth(Region.USE_PREF_SIZE);
        btnMaxSpeed.tooltipProperty().bind(i18n.createTooltipBinding("sim.speed.max.tt"));

        final boolean[] isProgrammaticSpeedChange = { false };

        panel.speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (isProgrammaticSpeedChange[0]) return;
            if ("MAX".equals(btnMaxSpeed.getUserData())) {
                btnMaxSpeed.setUserData(null);
                btnMaxSpeed.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 4 10 4 10;");
            }
            panel.currentSpeed = newVal.floatValue();
            panel.lblSpeed.setText(String.format("%.1fx", panel.currentSpeed));
            if (panel.onSpeedChange != null) panel.onSpeedChange.accept(panel.currentSpeed);
        });

        btnMaxSpeed.setOnAction(e -> {
            if ("MAX".equals(btnMaxSpeed.getUserData())) {
                btnMaxSpeed.setUserData(null);
                btnMaxSpeed.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 4 10 4 10;");
                panel.currentSpeed = (float) panel.speedSlider.getValue();
                panel.lblSpeed.setText(String.format("%.1fx", panel.currentSpeed));
            } else {
                btnMaxSpeed.setUserData("MAX");
                btnMaxSpeed.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: #fef08a; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 4 10 4 10; -fx-effect: innershadow(three-pass-box, #d946ef, 5, 0, 0, 0);");
                isProgrammaticSpeedChange[0] = true;
                try {
                    panel.speedSlider.setValue(panel.speedSlider.getMax());
                } finally {
                    isProgrammaticSpeedChange[0] = false;
                }
                panel.currentSpeed = 1000.0f;
                panel.lblSpeed.setText("MAX 🚀");
            }
            if (panel.onSpeedChange != null) panel.onSpeedChange.accept(panel.currentSpeed);
        });

        speedSliderRow.getChildren().addAll(lblSpeedLabel, panel.speedSlider, panel.lblSpeed, btnMaxSpeed);

        panel.playbackAndSpeedPanel.setPadding(new Insets(8));
        panel.playbackAndSpeedPanel.getStyleClass().add("card-pane");

        Label lblPlaybackHeader = new Label();
        lblPlaybackHeader.textProperty().bind(i18n.createStringBinding("sim.playback.header"));
        lblPlaybackHeader.setStyle("-fx-text-fill: #a78bfa; -fx-font-weight: bold; -fx-font-size: 11px;");

        panel.playbackAndSpeedPanel.getChildren().setAll(lblPlaybackHeader, dateTimeRow, playbackRow1, speedSliderRow);
    }

    VBox buildCheckpointsPane(ComboBox<SimulationCheckpoint> comboCheckpoints) {
        return checkpointsSubPanel.buildCheckpointsPane(comboCheckpoints);
    }

    void updateButtonStates() {
        if (panel.btnPlay != null) {
            FontIcon playIcon = (panel.btnPlay.getGraphic() instanceof FontIcon) ? (FontIcon) panel.btnPlay.getGraphic() : null;
            if (panel.isPlaying) {
                panel.btnPlay.setStyle("-fx-background-color: #d97706; -fx-background-radius: 4; -fx-min-width: 32px; -fx-min-height: 28px; -fx-padding: 3px 6px; -fx-cursor: hand; -fx-border-color: #f59e0b; -fx-border-width: 2px; -fx-border-radius: 4;");
                if (playIcon != null) {
                    playIcon.setIconCode(Feather.PAUSE);
                    playIcon.setIconColor(Color.web("#ffffff"));
                }
                Tooltip tt = panel.btnPlay.getTooltip();
                if (tt != null) {
                    tt.textProperty().unbind();
                    tt.textProperty().bind(i18n.createStringBinding("sim.btn.pause.tt"));
                }
            } else {
                panel.btnPlay.setStyle("-fx-background-color: #16a34a; -fx-background-radius: 4; -fx-min-width: 32px; -fx-min-height: 28px; -fx-padding: 3px 6px; -fx-cursor: hand; -fx-border-color: #22c55e; -fx-border-width: 2px; -fx-border-radius: 4;");
                if (playIcon != null) {
                    playIcon.setIconCode(Feather.PLAY);
                    playIcon.setIconColor(Color.web("#ffffff"));
                }
                Tooltip tt = panel.btnPlay.getTooltip();
                if (tt != null) {
                    tt.textProperty().unbind();
                    tt.textProperty().bind(i18n.createStringBinding("sim.btn.play.tt"));
                }
            }
        }

        if (panel.btnRewind != null) panel.btnRewind.setDisable(false);
        if (panel.btnStepBack != null) panel.btnStepBack.setDisable(false);
        if (panel.btnStepForward != null) panel.btnStepForward.setDisable(false);
        if (panel.btnFastForward != null) panel.btnFastForward.setDisable(false);
        if (panel.btnGoToBeginning != null) panel.btnGoToBeginning.setDisable(false);
        if (panel.btnGoToEnd != null) panel.btnGoToEnd.setDisable(false);
        if (panel.btnPlay != null) panel.btnPlay.setDisable(false);

        boolean isSimulationActive = panel.isPlaying || panel.currentTick > 0 || panel.highestRecordedTick > 0;
        if (panel.scenarioStepCombo != null) panel.scenarioStepCombo.setDisable(isSimulationActive);
        if (panel.startDatePicker != null) panel.startDatePicker.setDisable(isSimulationActive);
        if (panel.startTimeHourSpinner != null) panel.startTimeHourSpinner.setDisable(isSimulationActive);
        if (panel.startTimeMinuteSpinner != null) panel.startTimeMinuteSpinner.setDisable(isSimulationActive);
        if (panel.startTimeSecondSpinner != null) panel.startTimeSecondSpinner.setDisable(isSimulationActive);
        if (panel.txtSeed != null) panel.txtSeed.setDisable(isSimulationActive);
    }

    private void doGoToBeginning() {
        panel.isPlaying = false;
        panel.isPaused = true;
        panel.isStopped = false;
        panel.currentTick = 0;
        panel.updateTick(0, panel.highestRecordedTick);
        updateButtonStates();
        if (panel.onPause != null) panel.onPause.accept(null);
        if (panel.onSeek != null) panel.onSeek.accept(0L);
    }

    private void doRewind() {
        panel.isPlaying = false;
        panel.isPaused = true;
        panel.isStopped = false;
        long target = Math.max(0, panel.currentTick - 10000);
        panel.currentTick = target;
        panel.updateTick(panel.currentTick, panel.highestRecordedTick);
        updateButtonStates();
        if (panel.onPause != null) panel.onPause.accept(null);
        if (panel.onRewind != null) panel.onRewind.accept(10000);
    }

    private void doStepBack() {
        panel.isPlaying = false;
        panel.isPaused = true;
        panel.isStopped = false;
        long target = Math.max(0, panel.currentTick - 100);
        panel.currentTick = target;
        panel.updateTick(panel.currentTick, panel.highestRecordedTick);
        updateButtonStates();
        if (panel.onPause != null) panel.onPause.accept(null);
        if (panel.onRewind != null) panel.onRewind.accept(100);
    }

    private void doTogglePlayPause() {
        if (panel.isPlaying) {
            doPause();
        } else {
            doPlay();
        }
    }

    private void doPlay() {
        if (!panel.isPlaying) {
            panel.isPlaying = true;
            panel.isPaused = false;
            panel.isStopped = false;
            updateButtonStates();
            if (panel.onPlay != null) panel.onPlay.accept(null);
        }
    }

    private void doPause() {
        panel.isPlaying = false;
        panel.isPaused = true;
        panel.isStopped = false;
        updateButtonStates();
        if (panel.onPause != null) panel.onPause.accept(null);
    }

    private void doStepForward() {
        panel.isPlaying = false;
        panel.isPaused = true;
        panel.isStopped = false;
        long maxTicks = panel.getMaxSimulationTicks();
        if (panel.currentTick >= maxTicks) {
            updateButtonStates();
            return;
        }
        long target = Math.min(maxTicks, panel.currentTick + 100);
        if (target <= panel.highestRecordedTick) {
            panel.currentTick = target;
            panel.updateTick(panel.currentTick, panel.highestRecordedTick);
            updateButtonStates();
            if (panel.onPause != null) panel.onPause.accept(null);
            if (panel.onSeek != null) panel.onSeek.accept(target);
        } else {
            int stepCount = (int) Math.min(100, maxTicks - panel.currentTick);
            updateButtonStates();
            if (panel.onPause != null) panel.onPause.accept(null);
            if (panel.onStepForward != null && stepCount > 0) panel.onStepForward.accept(stepCount);
        }
    }

    private void doFastForward() {
        panel.isPlaying = false;
        panel.isPaused = true;
        panel.isStopped = false;
        long maxTicks = panel.getMaxSimulationTicks();
        if (panel.currentTick >= maxTicks) {
            updateButtonStates();
            return;
        }
        long target = Math.min(maxTicks, panel.currentTick + 10000);
        if (target <= panel.highestRecordedTick) {
            panel.currentTick = target;
            panel.updateTick(target, panel.highestRecordedTick);
            updateButtonStates();
            if (panel.onPause != null) panel.onPause.accept(null);
            if (panel.onSeek != null) panel.onSeek.accept(target);
        } else {
            int stepCount = (int) Math.min(10000, maxTicks - panel.currentTick);
            updateButtonStates();
            if (panel.onPause != null) panel.onPause.accept(null);
            if (panel.onStepForward != null && stepCount > 0) panel.onStepForward.accept(stepCount);
        }
    }

    private void doGoToEnd() {
        panel.isPlaying = false;
        panel.isPaused = true;
        panel.isStopped = false;
        panel.currentTick = panel.highestRecordedTick;
        panel.updateTick(panel.highestRecordedTick, panel.highestRecordedTick);
        updateButtonStates();
        if (panel.onPause != null) panel.onPause.accept(null);
        if (panel.onSeek != null) panel.onSeek.accept(panel.highestRecordedTick);
    }
}
