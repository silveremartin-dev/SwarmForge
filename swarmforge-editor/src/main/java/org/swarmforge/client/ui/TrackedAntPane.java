/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import org.swarmforge.client.util.I18nManager;
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

        titleLabel = new Label();
        titleLabel.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.no_ant_title"));
        titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #f59e0b;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnClose = new Button("✕");
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand;");
        btnClose.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("tracked_ant.close_tt"));
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
        lblHealthText = new Label();
        lblHealthText.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.health_none"));
        lblHealthText.setStyle("-fx-font-size: 11px; -fx-text-fill: #cbd5e1;");

        Region healthSpacer = new Region();
        HBox.setHgrow(healthSpacer, Priority.ALWAYS);

        healthBar = new ProgressBar(1.0);
        healthBar.setPrefWidth(90);
        healthBar.setPrefHeight(10);
        healthBar.setStyle("-fx-accent: #22c55e;");
        healthRow.getChildren().addAll(lblHealthText, healthSpacer, healthBar);

        // 2. Energy / Hunger / Thirst
        lblEnergyHungerThirst = new Label();
        lblEnergyHungerThirst.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.metrics_none"));
        lblEnergyHungerThirst.setStyle("-fx-font-size: 11px; -fx-text-fill: #38bdf8;");

        // 3. Age / Stage / Job
        lblAgeStageJob = new Label();
        lblAgeStageJob.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.age_job_none"));
        lblAgeStageJob.setStyle("-fx-font-size: 11px; -fx-text-fill: #e2e8f0;");

        // 4. AI State
        lblAiState = new Label();
        lblAiState.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.ai_state_none"));
        lblAiState.setStyle("-fx-font-size: 11px; -fx-text-fill: #a78bfa;");

        // 5. 3D Position
        lblPos3D = new Label();
        lblPos3D.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.pos3d_none"));
        lblPos3D.setStyle("-fx-font-size: 11px; -fx-text-fill: #cbd5e1;");

        // 6. Heading & Cargo
        lblHeadingCargo = new Label();
        lblHeadingCargo.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.heading_cargo_none"));
        lblHeadingCargo.setStyle("-fx-font-size: 11px; -fx-text-fill: #cbd5e1;");

        // 7. CHC Gestalt Status
        lblChcGestalt = new Label();
        lblChcGestalt.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.chc_gestalt_none"));
        lblChcGestalt.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

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
        btnFollowThisAnt = new Button();
        btnFollowThisAnt.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.btn_follow"));
        btnFollowThisAnt.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnFollowThisAnt, Priority.ALWAYS);
        btnFollowThisAnt.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand;");
        btnFollowThisAnt.setOnAction(e -> {
            if (currentAnt != null && onFollowAntHandler != null) {
                onFollowAntHandler.accept(currentAnt);
            }
        });

        btnStopFollow = new Button();
        btnStopFollow.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.btn_stop"));
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
        txtAntId.promptTextProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.prompt"));
        txtAntId.setStyle("-fx-background-color: #1e293b; -fx-text-fill: white; -fx-border-color: #334155; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-size: 11px;");
        HBox.setHgrow(txtAntId, Priority.ALWAYS);
        txtAntId.setOnAction(e -> triggerSearch());

        btnDirectFollow = new Button();
        btnDirectFollow.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.btn_search"));
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

        titleLabel.textProperty().unbind();
        titleLabel.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.no_ant_title"));

        lblHealthText.textProperty().unbind();
        lblHealthText.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.health_none"));
        healthBar.setProgress(0);

        lblEnergyHungerThirst.textProperty().unbind();
        lblEnergyHungerThirst.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.metrics_none"));

        lblAgeStageJob.textProperty().unbind();
        lblAgeStageJob.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.age_job_none"));

        lblAiState.textProperty().unbind();
        lblAiState.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.ai_state_none"));

        lblPos3D.textProperty().unbind();
        lblPos3D.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.pos3d_none"));

        lblHeadingCargo.textProperty().unbind();
        lblHeadingCargo.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.heading_cargo_none"));

        lblChcGestalt.textProperty().unbind();
        lblChcGestalt.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.chc_gestalt_none"));
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

        titleLabel.textProperty().unbind();
        String statusText = following ? I18nManager.getInstance().get("tracked_ant.status_tracked") : I18nManager.getInstance().get("tracked_ant.status_selected");
        titleLabel.setText(String.format("🎯 %s %s: %s #%s", I18nManager.getInstance().get("tracked_ant.title"), statusText, ant.getCaste(), shortId));

        // Health
        double health = ant.getHealth();
        lblHealthText.textProperty().unbind();
        lblHealthText.setText(I18nManager.getInstance().get("tracked_ant.health", health));
        healthBar.setProgress(Math.max(0, Math.min(1.0, health / 100.0)));
        healthBar.setStyle(health > 50 ? "-fx-accent: #22c55e;" : "-fx-accent: #ef4444;");

        // Energy / Hunger / Thirst
        lblEnergyHungerThirst.textProperty().unbind();
        lblEnergyHungerThirst.setText(I18nManager.getInstance().get("tracked_ant.metrics", ant.getEnergy(), ant.getHunger(), ant.getThirst()));

        // Age / Stage / Job
        double ageDays = ant.getAge() / 600.0;
        lblAgeStageJob.textProperty().unbind();
        lblAgeStageJob.setText(I18nManager.getInstance().get("tracked_ant.age_job", ageDays, ant.getLifeStage(), ant.getJob()));

        // AI State & Behaviors
        String behaviorsStr = ant.getActiveBehaviorsSummary();
        lblAiState.textProperty().unbind();
        String stateStr = I18nManager.getInstance().get("tracked_ant.ai_state", ant.getState() != null ? ant.getState() : "IDLE", behaviorsStr != null ? behaviorsStr : "");
        if (stateStr.length() > 46) {
            stateStr = stateStr.substring(0, 43) + "...";
        }
        lblAiState.setText(stateStr);

        // 3D Position
        lblPos3D.textProperty().unbind();
        lblPos3D.setText(I18nManager.getInstance().get("tracked_ant.pos3d", ant.getX(), ant.getY(), ant.getZ()));

        // Heading & Cargo
        String cargo = ant.getCarriedItem() != Individual.CarriedItem.NONE ? ant.getCarriedItem().name() : I18nManager.getInstance().get("tracked_ant.cargo_none");
        lblHeadingCargo.textProperty().unbind();
        lblHeadingCargo.setText(I18nManager.getInstance().get("tracked_ant.heading_cargo", Math.toDegrees(ant.getHeading()), cargo));

        // CHC Gestalt
        lblChcGestalt.textProperty().unbind();
        lblChcGestalt.setText(I18nManager.getInstance().get("tracked_ant.chc_gestalt"));
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
