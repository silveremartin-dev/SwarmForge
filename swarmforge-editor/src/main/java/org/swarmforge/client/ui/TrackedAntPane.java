/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import org.swarmforge.core.domain.Individual;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * Tracked Ant HUD & Interactive Inspection Pane for SwarmForge Viewport.
 * Combines compact real-time biological & spatial telemetry with interactive follow controls.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class TrackedAntPane extends VBox {

    private final Label titleLabel;
    private final Button btnClose;

    // Telemetry Labels
    private final Label lblHealthText;
    private final ProgressBar healthBar;
    private final Label lblEnergyHungerThirst;
    private final Label lblAgeStageJob;
    private final Label lblAiState;
    private final Label lblPos3D;
    private final Label lblHeadingCargo;
    private final Label lblChcGestalt;
    private final Label lblSearchStatus;

    // Interactive Action Controls
    private final Button btnFollowThisAnt;
    private final Button btnStopFollow;
    private final TextField txtAntId;
    private final Button btnDirectFollow;
    private final VBox telemetryBox;

    private Individual currentAnt = null;
    private boolean isFollowing = false;

    private Consumer<Individual> onFollowAntHandler;
    private Consumer<String> onFollowAntByIdHandler;
    private Runnable onStopFollowHandler;

    public TrackedAntPane() {
        setSpacing(6);
        setPadding(new Insets(10, 12, 10, 12));
        setPrefWidth(330);
        setMaxWidth(340);
        setStyle("-fx-background-color: rgba(15, 23, 42, 0.94); " +
                "-fx-border-color: #f59e0b; -fx-border-width: 1.5; " +
                "-fx-border-radius: 10; -fx-background-radius: 10;");

        // Header
        HBox headerBox = new HBox(8);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        titleLabel = new Label("🎯 FOURMI SUIVIE");
        titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #f59e0b;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnClose = new Button("✕");
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand;");
        btnClose.setTooltip(new Tooltip("Fermer le panneau de suivi"));
        btnClose.setOnAction(e -> {
            if (onStopFollowHandler != null) {
                onStopFollowHandler.run();
            }
            setVisible(false);
        });

        headerBox.getChildren().addAll(titleLabel, spacer, btnClose);

        Separator sep1 = new Separator();
        sep1.setStyle("-fx-background-color: rgba(245, 158, 11, 0.3);");

        // Telemetry Box
        telemetryBox = new VBox(5);

        // 1. Health Row
        HBox healthRow = new HBox(8);
        healthRow.setAlignment(Pos.CENTER_LEFT);
        lblHealthText = new Label("💓 Santé : -- / 100");
        lblHealthText.setStyle("-fx-font-size: 11px; -fx-text-fill: #cbd5e1;");

        Region healthSpacer = new Region();
        HBox.setHgrow(healthSpacer, Priority.ALWAYS);

        healthBar = new ProgressBar(1.0);
        healthBar.setPrefWidth(90);
        healthBar.setPrefHeight(10);
        healthBar.setStyle("-fx-accent: #22c55e;");
        healthRow.getChildren().addAll(lblHealthText, healthSpacer, healthBar);

        // 2. Energy / Hunger / Thirst
        lblEnergyHungerThirst = new Label("⚡ Énergie: --% | 🍗 Faim: --% | 💧 Soif: --%");
        lblEnergyHungerThirst.setStyle("-fx-font-size: 11px; -fx-text-fill: #38bdf8;");

        // 3. Age / Stage / Job
        lblAgeStageJob = new Label("🎂 Âge: -- jours | Stade: -- | Tâche: --");
        lblAgeStageJob.setStyle("-fx-font-size: 11px; -fx-text-fill: #e2e8f0;");

        // 4. AI State
        lblAiState = new Label("🧠 IA: IDLE");
        lblAiState.setStyle("-fx-font-size: 11px; -fx-text-fill: #a78bfa;");

        // 5. 3D Position
        lblPos3D = new Label("📍 Pos 3D: (X: --, Y: --, Z: --)");
        lblPos3D.setStyle("-fx-font-size: 11px; -fx-text-fill: #cbd5e1;");

        // 6. Heading & Cargo
        lblHeadingCargo = new Label("🚀 Cap (Heading): --° | 📦 Cargo: Aucun");
        lblHeadingCargo.setStyle("-fx-font-size: 11px; -fx-text-fill: #cbd5e1;");

        // 7. CHC Gestalt Status
        lblChcGestalt = new Label("🧪 Gestalt Hydrocarbonée Cuticulaire (CHC): Authentifiée");
        lblChcGestalt.setStyle("-fx-font-size: 11px; -fx-text-fill: #4ade80;");

        telemetryBox.getChildren().addAll(
                healthRow,
                lblEnergyHungerThirst,
                lblAgeStageJob,
                lblAiState,
                lblPos3D,
                lblHeadingCargo,
                lblChcGestalt
        );

        Separator sep2 = new Separator();
        sep2.setStyle("-fx-background-color: rgba(245, 158, 11, 0.3);");

        // Buttons & Controls
        VBox controlsBox = new VBox(6);

        HBox followActionRow = new HBox(6);
        btnFollowThisAnt = new Button("🎥 Follow this Ant");
        btnFollowThisAnt.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnFollowThisAnt, Priority.ALWAYS);
        btnFollowThisAnt.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand;");
        btnFollowThisAnt.setOnAction(e -> {
            if (currentAnt != null && onFollowAntHandler != null) {
                onFollowAntHandler.accept(currentAnt);
            }
        });

        btnStopFollow = new Button("🛑 Arrêter le suivi");
        btnStopFollow.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand;");
        btnStopFollow.setOnAction(e -> {
            if (onStopFollowHandler != null) {
                onStopFollowHandler.run();
            }
        });

        followActionRow.getChildren().addAll(btnFollowThisAnt, btnStopFollow);

        // ID Search Row
        HBox searchRow = new HBox(6);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        txtAntId = new TextField();
        txtAntId.setPromptText("ID Fourmi (ex: ant_1)");
        txtAntId.setStyle("-fx-background-color: #1e293b; -fx-text-fill: white; -fx-border-color: #334155; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-size: 11px;");
        HBox.setHgrow(txtAntId, Priority.ALWAYS);
        txtAntId.setOnAction(e -> triggerSearch());

        btnDirectFollow = new Button("🎥 Follow");
        btnDirectFollow.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand;");
        btnDirectFollow.setOnAction(e -> triggerSearch());

        searchRow.getChildren().addAll(txtAntId, btnDirectFollow);

        lblSearchStatus = new Label();
        lblSearchStatus.setStyle("-fx-font-size: 10px; -fx-text-fill: #ef4444;");
        lblSearchStatus.setVisible(false);

        controlsBox.getChildren().addAll(followActionRow, searchRow, lblSearchStatus);

        getChildren().addAll(headerBox, sep1, telemetryBox, sep2, controlsBox);
        setNoAntSelectedState();
    }

    private void triggerSearch() {
        String targetId = txtAntId.getText() != null ? txtAntId.getText().trim() : "";
        if (!targetId.isEmpty() && onFollowAntByIdHandler != null) {
            onFollowAntByIdHandler.accept(targetId);
        }
    }

    public void setSearchStatusError(String message) {
        lblSearchStatus.setText(message);
        lblSearchStatus.setVisible(true);
    }

    public void clearSearchStatus() {
        lblSearchStatus.setVisible(false);
    }

    public void setNoAntSelectedState() {
        this.currentAnt = null;
        this.isFollowing = false;
        titleLabel.setText("🎯 SUIVI DE FOURMI");
        lblHealthText.setText("💓 Santé : -- / 100");
        healthBar.setProgress(0);
        lblEnergyHungerThirst.setText("⚡ Énergie: --% | 🍗 Faim: --% | 💧 Soif: --%");
        lblAgeStageJob.setText("🎂 Âge: -- jours | Stade: -- | Tâche: --");
        lblAiState.setText("🧠 IA: Aucune fourmi sélectionnée");
        lblPos3D.setText("📍 Pos 3D: (X: --, Y: --, Z: --)");
        lblHeadingCargo.setText("🚀 Cap (Heading): --° | 📦 Cargo: Aucun");
        lblChcGestalt.setText("🧪 Gestalt (CHC): Attente de sélection");
        lblChcGestalt.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        btnFollowThisAnt.setDisable(true);
        btnStopFollow.setVisible(false);
        btnStopFollow.setManaged(false);
        clearSearchStatus();
    }

    public void updateAnt(Individual ant, boolean following) {
        this.currentAnt = ant;
        this.isFollowing = following;

        if (ant == null) {
            setNoAntSelectedState();
            return;
        }

        String idStr = ant.getId() != null ? ant.getId().toString() : "N/A";
        String shortId = idStr.length() > 8 ? idStr.substring(0, 8) : idStr;
        titleLabel.setText(String.format("🎯 FOURMI %s : %s #%s", following ? "SUIVIE" : "SÉLECTIONNÉE", ant.getCaste(), shortId));

        // Health
        double health = ant.getHealth();
        lblHealthText.setText(String.format(Locale.US, "💓 Santé : %.0f / 100", health));
        healthBar.setProgress(Math.max(0, Math.min(1.0, health / 100.0)));
        healthBar.setStyle(health > 50 ? "-fx-accent: #22c55e;" : "-fx-accent: #ef4444;");

        // Energy / Hunger / Thirst
        lblEnergyHungerThirst.setText(String.format(Locale.US, "⚡ Énergie: %.0f%% | 🍗 Faim: %.0f%% | 💧 Soif: %.0f%%",
                ant.getEnergy(), ant.getHunger(), ant.getThirst()));

        // Age / Stage / Job
        double ageDays = ant.getAge() / 600.0;
        lblAgeStageJob.setText(String.format(Locale.US, "🎂 Âge: %.1f jours | Stade: %s | Tâche: %s",
                ageDays, ant.getLifeStage(), ant.getJob()));

        // AI State & Behaviors
        String behaviorsStr = ant.getActiveBehaviorsSummary();
        String stateStr = String.format("🧠 IA: %s [%s]",
                ant.getState() != null ? ant.getState() : "IDLE",
                behaviorsStr != null ? behaviorsStr : "");
        if (stateStr.length() > 46) {
            stateStr = stateStr.substring(0, 43) + "...";
        }
        lblAiState.setText(stateStr);

        // 3D Position
        lblPos3D.setText(String.format(Locale.US, "📍 Pos 3D: (X: %.1f, Y: %.1f, Z: %.1f)", ant.getX(), ant.getY(), ant.getZ()));

        // Heading & Cargo
        String cargo = ant.getCarriedItem() != Individual.CarriedItem.NONE ? ant.getCarriedItem().name() : "Aucun";
        lblHeadingCargo.setText(String.format(Locale.US, "🚀 Cap (Heading): %.0f° | 📦 Cargo: %s", Math.toDegrees(ant.getHeading()), cargo));

        // CHC Gestalt
        lblChcGestalt.setText("🧪 Gestalt Hydrocarbonée Cuticulaire (CHC): Authentifiée");
        lblChcGestalt.setStyle("-fx-font-size: 11px; -fx-text-fill: #4ade80;");

        btnFollowThisAnt.setDisable(false);
        btnStopFollow.setVisible(following);
        btnStopFollow.setManaged(following);
        clearSearchStatus();
    }

    public void setOnFollowAnt(Consumer<Individual> handler) {
        this.onFollowAntHandler = handler;
    }

    public void setOnFollowAntById(Consumer<String> handler) {
        this.onFollowAntByIdHandler = handler;
    }

    public void setOnStopFollow(Runnable handler) {
        this.onStopFollowHandler = handler;
    }

    public Individual getCurrentAnt() {
        return currentAnt;
    }
}
