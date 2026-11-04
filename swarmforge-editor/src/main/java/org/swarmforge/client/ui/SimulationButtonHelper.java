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
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.client.util.I18nManager;

/**
 * Utility class providing icon buttons and auto-repeat mouse event binding for simulation controls.
 */
class SimulationButtonHelper {

    private SimulationButtonHelper() {}

    static void setupAutoRepeat(Button button, Runnable action) {
        final boolean[] isExecuting = { false };
        Timeline repeatTimeline = new Timeline();
        repeatTimeline.setCycleCount(Timeline.INDEFINITE);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(120), e -> {
            if (!button.isDisabled() && !isExecuting[0]) {
                isExecuting[0] = true;
                try {
                    action.run();
                } finally {
                    isExecuting[0] = false;
                }
            }
        });
        repeatTimeline.getKeyFrames().add(keyFrame);

        PauseTransition initialDelay = new PauseTransition(Duration.millis(350));
        initialDelay.setOnFinished(e -> {
            if (!button.isDisabled()) {
                repeatTimeline.playFromStart();
            }
        });

        button.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() == MouseButton.PRIMARY && !button.isDisabled() && !isExecuting[0]) {
                isExecuting[0] = true;
                try {
                    action.run();
                } finally {
                    isExecuting[0] = false;
                }
                initialDelay.playFromStart();
            }
        });

        EventHandler<MouseEvent> stopRepeat = e -> {
            initialDelay.stop();
            repeatTimeline.stop();
            isExecuting[0] = false;
        };

        button.addEventHandler(MouseEvent.MOUSE_RELEASED, stopRepeat);
        button.addEventHandler(MouseEvent.MOUSE_EXITED, stopRepeat);
        button.disabledProperty().addListener((obs, oldV, newV) -> {
            if (newV) {
                initialDelay.stop();
                repeatTimeline.stop();
                isExecuting[0] = false;
            }
        });
        button.setOnAction(null);
    }

    static Button createIconButton(Feather icon, String tooltipKey) {
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(13);

        Button btn = new Button();
        btn.setGraphic(fontIcon);
        btn.getStyleClass().add("ctrl-btn");
        if (tooltipKey != null && !tooltipKey.isEmpty()) {
            Tooltip tt = new Tooltip();
            if (tooltipKey.contains(".")) {
                tt.textProperty().bind(I18nManager.getInstance().createStringBinding(tooltipKey));
            } else {
                tt.setText(tooltipKey);
            }
            btn.setTooltip(tt);
        }
        btn.disabledProperty().addListener((obs, oldV, newV) -> btn.setOpacity(newV ? 0.35 : 1.0));
        return btn;
    }
}
