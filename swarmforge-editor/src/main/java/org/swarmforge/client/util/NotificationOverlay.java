/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.util;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Non-blocking, smooth overlay notification toast banner utility for SwarmForge UI.
 * Fully compatible with Light and Dark CSS themes.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class NotificationOverlay {

    public enum NotificationType {
        SUCCESS("#10b981", Feather.CHECK_CIRCLE),
        INFO("#3b82f6", Feather.INFO),
        WARNING("#f59e0b", Feather.ALERT_TRIANGLE),
        ERROR("#ef4444", Feather.ALERT_OCTAGON);

        public final String color;
        public final Feather icon;

        NotificationType(String color, Feather icon) {
            this.color = color;
            this.icon = icon;
        }
    }

    /**
     * Shows a non-blocking toast notification banner over the container.
     */
    public static void show(Pane parent, String message, NotificationType type) {
        if (parent == null || message == null || message.isBlank()) return;

        Platform.runLater(() -> {
            HBox toast = new HBox(10);
            toast.setAlignment(Pos.CENTER_LEFT);
            toast.setPadding(new Insets(10, 16, 10, 16));
            toast.getStyleClass().add("toast-banner");
            toast.setStyle("-fx-border-color: " + type.color + ";");

            FontIcon icon = new FontIcon(type.icon);
            icon.setIconSize(18);
            icon.setStyle("-fx-icon-color: " + type.color + ";");

            Label label = new Label(message);
            label.getStyleClass().add("toast-label");
            label.setWrapText(true);

            toast.getChildren().addAll(icon, label);
            toast.setMaxWidth(500);

            // Determine target container
            Pane target = parent;
            if (parent.getScene() != null && parent.getScene().getRoot() instanceof Pane) {
                target = (Pane) parent.getScene().getRoot();
            }

            if (target instanceof StackPane) {
                StackPane.setAlignment(toast, Pos.BOTTOM_RIGHT);
                StackPane.setMargin(toast, new Insets(0, 24, 24, 0));
            } else {
                toast.setTranslateX(20);
                toast.setTranslateY(20);
            }

            toast.setOpacity(0.0);
            target.getChildren().add(toast);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(250), toast);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            PauseTransition stay = new PauseTransition(Duration.millis(3200));

            FadeTransition fadeOut = new FadeTransition(Duration.millis(350), toast);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            final Pane finalTarget = target;
            SequentialTransition seq = new SequentialTransition(fadeIn, stay, fadeOut);
            seq.setOnFinished(e -> finalTarget.getChildren().remove(toast));
            seq.play();
        });
    }
}
