/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Modern Splash Screen with animated loading progress bar displayed during SwarmForge application startup.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SplashScreen {

    private final Stage splashStage;
    private final ProgressBar progressBar;
    private final Label statusLabel;
    private final Label percentLabel;

    public SplashScreen() {
        splashStage = new Stage();
        splashStage.initStyle(StageStyle.TRANSPARENT);

        // Window Icon registration for taskbar appearance
        try {
            java.io.InputStream iconStream = getClass().getResourceAsStream("/icons/icon.png");
            if (iconStream != null) {
                splashStage.getIcons().add(new javafx.scene.image.Image(iconStream));
            }
        } catch (Exception ignored) {}

        // Header Title & Eusocial Insect Icon Banner
        FontIcon logoIcon = new FontIcon(Feather.DISC);
        logoIcon.setIconSize(42);
        logoIcon.setStyle("-fx-icon-color: #f59e0b;");

        Label titleLabel = new Label("SwarmForge Studio");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        Label subtitleLabel = new Label("Simulateur de Sociétés d'Insectes (Fourmis, Termites, Guêpes & Abeilles)");
        subtitleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #38bdf8; -fx-font-weight: bold;");

        VBox titleBox = new VBox(2, titleLabel, subtitleLabel);

        HBox headerBox = new HBox(14, logoIcon, titleBox);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        // Status & Progress Bar Controls
        statusLabel = new Label("Initialisation du moteur de simulation eusociale...");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #e2e8f0;");

        percentLabel = new Label("0 %");
        percentLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        HBox statusBox = new HBox(statusLabel, new Region(), percentLabel);
        HBox.setHgrow(statusBox.getChildren().get(1), Priority.ALWAYS);

        progressBar = new ProgressBar(0.0);
        progressBar.setPrefWidth(460);
        progressBar.setPrefHeight(10);
        progressBar.setStyle("-fx-accent: #0284c7; -fx-control-inner-background: #1e293b;");

        // Footer Metadata
        Label footerLabel = new Label("v2.5.0 • Silvère Martin-Michiellot & Gemini AI (Google DeepMind)");
        footerLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748b;");

        VBox contentBox = new VBox(16, headerBox, statusBox, progressBar, footerLabel);
        contentBox.setPadding(new Insets(22));
        contentBox.setStyle(
            "-fx-background-color: #0f172a; " +
            "-fx-border-color: #38bdf8; " +
            "-fx-border-width: 1.5px; " +
            "-fx-border-radius: 12px; " +
            "-fx-background-radius: 12px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.65), 18, 0, 0, 6);"
        );

        Scene scene = new Scene(contentBox);
        scene.setFill(Color.TRANSPARENT);
        splashStage.setScene(scene);
        splashStage.setAlwaysOnTop(true);
    }

    /**
     * Display the splash screen window centered on screen.
     */
    public void show() {
        splashStage.show();
        splashStage.centerOnScreen();
    }

    /**
     * Start the step-by-step progress bar animation and launch callback when completed.
     *
     * @param onFinished Action to execute when loading reaches 100%
     */
    public void startProgressAndLaunch(Runnable onFinished) {
        String[] steps = {
            "Initialisation des modules SwarmForge...",
            "Chargement des dictionnaires d'internationalisation (I18n)...",
            "Chargement des presets d'espèces & génétique...",
            "Initialisation du moteur audio & synthétiseur procédural...",
            "Préparation du viewport 3D (jMonkeyEngine & JavaFX)...",
            "Connexion à la base de persistance SQLite...",
            "Démarrage du studio..."
        };

        Timeline timeline = new Timeline();
        int totalSteps = steps.length;

        for (int i = 0; i < totalSteps; i++) {
            final int stepIdx = i;
            double progress = (i + 1.0) / totalSteps;
            int percentage = (int) Math.round(progress * 100);

            KeyFrame kf = new KeyFrame(
                Duration.millis(220 * (i + 1)),
                e -> {
                    statusLabel.setText(steps[stepIdx]);
                    progressBar.setProgress(progress);
                    percentLabel.setText(percentage + " %");
                }
            );
            timeline.getKeyFrames().add(kf);
        }

        timeline.setOnFinished(e -> {
            splashStage.close();
            if (onFinished != null) {
                onFinished.run();
            }
        });

        timeline.play();
    }

    /**
     * Explicitly close the splash screen.
     */
    public void close() {
        splashStage.close();
    }
}
