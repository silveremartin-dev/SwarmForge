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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.net.URL;
import org.swarmforge.client.util.I18nManager;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.feather.Feather;

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
        this(null);
    }

    public SplashScreen(Stage owner) {
        splashStage = new Stage();
        splashStage.initStyle(StageStyle.TRANSPARENT);
        if (owner != null) {
            splashStage.initOwner(owner);
        }

        // Window Icon registration for taskbar appearance right on startup
        org.swarmforge.client.util.IconUtils.applyWindowIcons(splashStage);

        I18nManager i18n = I18nManager.getInstance();

        // Header Title & Application Logo Icon Banner
        javafx.scene.Node logoNode;
        URL iconUrl = SplashScreen.class.getResource("/icons/icon.png");
        if (iconUrl != null) {
            Image appIconImg = new Image(iconUrl.toExternalForm(), 48, 48, true, true);
            ImageView iconView = new ImageView(appIconImg);
            iconView.setFitWidth(48);
            iconView.setFitHeight(48);
            iconView.setPreserveRatio(true);
            logoNode = iconView;
        } else {
            FontIcon logoIcon = new FontIcon(Feather.DISC);
            logoIcon.setIconSize(42);
            logoIcon.setStyle("-fx-icon-color: #f59e0b;");
            logoNode = logoIcon;
        }

        Label titleLabel = new Label(i18n.get("splash.title"));
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        Label subtitleLabel = new Label(i18n.get("splash.subtitle"));
        subtitleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #38bdf8; -fx-font-weight: bold;");

        VBox titleBox = new VBox(2, titleLabel, subtitleLabel);

        HBox headerBox = new HBox(14, logoNode, titleBox);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        // Status & Progress Bar Controls
        statusLabel = new Label(i18n.get("splash.status.init"));
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
        Label footerLabel = new Label(i18n.get("splash.footer"));
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
        I18nManager i18n = I18nManager.getInstance();
        String[] steps = {
            i18n.get("splash.step.1"),
            i18n.get("splash.step.2"),
            i18n.get("splash.step.3"),
            i18n.get("splash.step.4"),
            i18n.get("splash.step.5"),
            i18n.get("splash.step.6"),
            i18n.get("splash.step.7")
        };

        Timeline timeline = new Timeline();
        int totalSteps = steps.length;

        for (int i = 0; i < totalSteps; i++) {
            final int stepIdx = i;
            double progress = (i + 1.0) / totalSteps;
            int percentage = (int) Math.round(progress * 100);

            KeyFrame kf = new KeyFrame(
                Duration.millis(380 * (i + 1)),
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

