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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.net.URL;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.IconUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.feather.Feather;

/**
 * Modern High-Fidelity Splash Screen displaying an atmospheric formicarium / vivarium background
 * with an animated loading progress bar during SwarmForge application startup.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SplashScreen {

    private static final double SPLASH_WIDTH = 640;
    private static final double SPLASH_HEIGHT = 380;

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
        IconUtils.applyWindowIcons(splashStage);

        I18nManager i18n = I18nManager.getInstance();

        // 1. Terrarium Background Image Layer
        ImageView bgImageView = new ImageView();
        URL bgUrl = SplashScreen.class.getResource("/images/splash_bg.jpg");
        if (bgUrl == null) {
            bgUrl = SplashScreen.class.getResource("/images/splash_bg.png");
        }
        if (bgUrl != null) {
            Image bgImg = new Image(bgUrl.toExternalForm(), SPLASH_WIDTH, SPLASH_HEIGHT, false, true);
            bgImageView.setImage(bgImg);
            bgImageView.setFitWidth(SPLASH_WIDTH);
            bgImageView.setFitHeight(SPLASH_HEIGHT);
            bgImageView.setPreserveRatio(false);
            bgImageView.setSmooth(true);
        }

        // 2. High-fidelity glassmorphism dark gradient overlay
        // Keeps the lively terrarium, glowing chambers & moss clearly visible while providing contrast for typography
        Region gradientOverlay = new Region();
        gradientOverlay.setPrefSize(SPLASH_WIDTH, SPLASH_HEIGHT);
        gradientOverlay.setStyle(
            "-fx-background-color: linear-gradient(to bottom, " +
            "rgba(15, 23, 42, 0.50) 0%, " +
            "rgba(15, 23, 42, 0.30) 35%, " +
            "rgba(15, 23, 42, 0.65) 70%, " +
            "rgba(15, 23, 42, 0.95) 100%);"
        );

        // 3. Header: Logo + Title + Badges
        javafx.scene.Node logoNode;
        URL iconUrl = SplashScreen.class.getResource("/icons/icon.png");
        if (iconUrl != null) {
            Image appIconImg = new Image(iconUrl.toExternalForm(), 44, 44, true, true);
            ImageView iconView = new ImageView(appIconImg);
            iconView.setFitWidth(44);
            iconView.setFitHeight(44);
            iconView.setPreserveRatio(true);
            logoNode = iconView;
        } else {
            FontIcon logoIcon = new FontIcon(Feather.DISC);
            logoIcon.setIconSize(40);
            logoIcon.setStyle("-fx-icon-color: #38bdf8;");
            logoNode = logoIcon;
        }

        Label titleLabel = new Label(i18n.get("splash.title"));
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #38bdf8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.85), 6, 0, 0, 2);");

        Label badgeLabel = new Label("SIMULATION STUDIO");
        badgeLabel.setStyle(
            "-fx-background-color: rgba(56, 189, 248, 0.20); " +
            "-fx-text-fill: #7dd3fc; " +
            "-fx-font-size: 9px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 2 7 2 7; " +
            "-fx-border-color: rgba(56, 189, 248, 0.45); " +
            "-fx-border-radius: 10px; " +
            "-fx-background-radius: 10px;"
        );

        HBox titleRow = new HBox(10, titleLabel, badgeLabel);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label subtitleLabel = new Label(i18n.get("splash.subtitle"));
        subtitleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e2e8f0; -fx-font-weight: 500; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.85), 4, 0, 0, 1);");

        VBox titleBox = new VBox(3, titleRow, subtitleLabel);

        HBox headerBox = new HBox(14, logoNode, titleBox);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(18, 22, 12, 22));

        // Center spacer area allowing terrarium visuals to shine
        Region centerSpacer = new Region();
        VBox.setVgrow(centerSpacer, Priority.ALWAYS);

        // 4. Status & Progress Bar Controls
        statusLabel = new Label(i18n.get("splash.status.init"));
        statusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #f1f5f9; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.9), 4, 0, 0, 1);");

        percentLabel = new Label("0 %");
        percentLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #38bdf8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.9), 4, 0, 0, 1);");

        HBox statusBox = new HBox(statusLabel, new Region(), percentLabel);
        HBox.setHgrow(statusBox.getChildren().get(1), Priority.ALWAYS);

        progressBar = new ProgressBar(0.0);
        progressBar.setPrefWidth(SPLASH_WIDTH - 44);
        progressBar.setPrefHeight(8);
        progressBar.setStyle("-fx-accent: #0ea5e9; -fx-control-inner-background: rgba(15, 23, 42, 0.85);");

        // 5. Footer Metadata
        Label footerLabel = new Label(i18n.get("splash.footer"));
        footerLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");

        Label taxaLabel = new Label("Formicidae • Apidae • Vespidae • Isoptera");
        taxaLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #64748b; -fx-font-style: italic;");

        HBox footerBox = new HBox(footerLabel, new Region(), taxaLabel);
        HBox.setHgrow(footerBox.getChildren().get(1), Priority.ALWAYS);
        footerBox.setAlignment(Pos.CENTER_LEFT);

        VBox bottomBox = new VBox(10, statusBox, progressBar, footerBox);
        bottomBox.setPadding(new Insets(10, 22, 18, 22));

        VBox foregroundContent = new VBox(headerBox, centerSpacer, bottomBox);
        foregroundContent.setPrefSize(SPLASH_WIDTH, SPLASH_HEIGHT);

        StackPane rootPane = new StackPane(bgImageView, gradientOverlay, foregroundContent);
        rootPane.setStyle("-fx-background-color: #0f172a;");
        rootPane.setPrefSize(SPLASH_WIDTH, SPLASH_HEIGHT);
        rootPane.setMinSize(SPLASH_WIDTH, SPLASH_HEIGHT);
        rootPane.setMaxSize(SPLASH_WIDTH, SPLASH_HEIGHT);

        // Rounded corners clipping
        Rectangle clip = new Rectangle(SPLASH_WIDTH, SPLASH_HEIGHT);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        rootPane.setClip(clip);

        // Container with cyan border & ambient drop shadow
        StackPane container = new StackPane(rootPane);
        container.setPadding(new Insets(10));
        container.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-border-color: rgba(56, 189, 248, 0.4); " +
            "-fx-border-width: 1.5px; " +
            "-fx-border-radius: 14px; " +
            "-fx-background-radius: 14px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.75), 24, 0, 0, 8);"
        );

        // Draggable window support
        final double[] dragDelta = new double[2];
        container.setOnMousePressed(mouseEvent -> {
            dragDelta[0] = splashStage.getX() - mouseEvent.getScreenX();
            dragDelta[1] = splashStage.getY() - mouseEvent.getScreenY();
        });
        container.setOnMouseDragged(mouseEvent -> {
            splashStage.setX(mouseEvent.getScreenX() + dragDelta[0]);
            splashStage.setY(mouseEvent.getScreenY() + dragDelta[1]);
        });

        Scene scene = new Scene(container);
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
            splashStage.setAlwaysOnTop(false);
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

