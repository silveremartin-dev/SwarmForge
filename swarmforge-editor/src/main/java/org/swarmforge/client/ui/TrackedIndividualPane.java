/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 SilvÃ¨re Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import org.swarmforge.client.util.I18nManager;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Predator;

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
 * @author SilvÃ¨re Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class TrackedIndividualPane extends VBox {

    final Label titleLabel;
    private final Button btnPrevAnt;
    private final Button btnNextAnt;
    private final Button btnClose;

    // Telemetry Labels
    final Label lblSpeciesColony;
    final Label lblHealthText;
    final ProgressBar healthBar;
    final Label lblEnergyHungerThirst;
    final Label lblAgeStageJob;
    final Label lblAiState;
    final Label lblPos3D;
    final Label lblHeadingCargo;
    final Label lblChcGestalt;
    private final Label lblSearchStatus;

    // Interactive Action Controls
    final Button btnFollowTps;
    final Button btnFollowFps;
    final Button btnCenter;
    private final TextField txtAntId;
    private final Button btnDirectFollow;
    private final VBox telemetryBox;

    private Individual currentAnt = null;
    private boolean isFollowing = false;

    private Consumer<Individual> onFollowAntHandler;
    private java.util.function.BiConsumer<Individual, CameraFollowMode> onFollowAntModeHandler;
    private Consumer<String> onFollowAntByIdHandler;
    private Runnable onStopFollowHandler;
    private Runnable onCenterHandler;
    private Runnable onCloseHandler;
    private Runnable onNextAntHandler;
    private Runnable onPreviousAntHandler;

    public TrackedIndividualPane() {
        setSpacing(6);
        setPadding(new Insets(10, 12, 10, 12));
        setPrefWidth(340);
        setMaxWidth(360);
        setStyle("-fx-background-color: rgba(15, 23, 42, 0.94); " +
                "-fx-border-color: #f59e0b; -fx-border-width: 1.5; " +
                "-fx-border-radius: 10; -fx-background-radius: 10;");

        // Header
        HBox headerBox = new HBox(6);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        titleLabel = new Label();
        titleLabel.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.no_ant_title"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnPrevAnt = new Button("â—€");
        btnPrevAnt.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("tracked_ant.btn_prev.tt"));
        btnPrevAnt.setOnAction(e -> {
            if (onPreviousAntHandler != null) {
                onPreviousAntHandler.run();
            }
        });

        btnNextAnt = new Button("â–¶");
        btnNextAnt.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("tracked_ant.btn_next.tt"));
        btnNextAnt.setOnAction(e -> {
            if (onNextAntHandler != null) {
                onNextAntHandler.run();
            }
        });

        btnClose = new Button("âœ•");
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 0 4 0 4; -fx-cursor: hand;");
        btnClose.setOnMouseEntered(e -> btnClose.setStyle("-fx-background-color: rgba(239, 68, 68, 0.2); -fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 0 4 0 4; -fx-background-radius: 4; -fx-cursor: hand;"));
        btnClose.setOnMouseExited(e -> btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 0 4 0 4; -fx-cursor: hand;"));
        btnClose.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("tracked_ant.close_tt"));
        btnClose.setOnAction(e -> {
            if (onStopFollowHandler != null) {
                onStopFollowHandler.run();
            }
            setVisible(false);
            if (onCloseHandler != null) {
                onCloseHandler.run();
            }
        });

        headerBox.getChildren().addAll(titleLabel, spacer, btnPrevAnt, btnNextAnt, btnClose);

        Separator sep1 = new Separator();
        sep1.setStyle("-fx-background-color: rgba(245, 158, 11, 0.3);");

        // Telemetry Box
        telemetryBox = new VBox(5);

        // 0. Species & Colony Row
        lblSpeciesColony = new Label("ðŸ§¬ EspÃ¨ce: - | ðŸ›ï¸ Colonie: -");
        lblSpeciesColony.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

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
                lblSpeciesColony,
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

        // Row 1: Camera Modes (TPS & FPS)
        HBox followActionRow = new HBox(6);
        btnFollowTps = new Button();
        btnFollowTps.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.btn_follow_tps"));
        btnFollowTps.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("tracked_ant.btn_follow_tps.tt"));
        btnFollowTps.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnFollowTps, Priority.ALWAYS);
        btnFollowTps.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 5 8;");
        btnFollowTps.setOnAction(e -> {
            if (currentAnt != null) {
                if (onFollowAntModeHandler != null) {
                    onFollowAntModeHandler.accept(currentAnt, CameraFollowMode.TPS);
                } else if (onFollowAntHandler != null) {
                    onFollowAntHandler.accept(currentAnt);
                }
            }
        });

        btnFollowFps = new Button();
        btnFollowFps.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.btn_follow_fps"));
        btnFollowFps.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("tracked_ant.btn_follow_fps.tt"));
        btnFollowFps.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnFollowFps, Priority.ALWAYS);
        btnFollowFps.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 5 8;");
        btnFollowFps.setOnAction(e -> {
            if (currentAnt != null) {
                if (onFollowAntModeHandler != null) {
                    onFollowAntModeHandler.accept(currentAnt, CameraFollowMode.FPS);
                } else if (onFollowAntHandler != null) {
                    onFollowAntHandler.accept(currentAnt);
                }
            }
        });

        followActionRow.getChildren().addAll(btnFollowTps, btnFollowFps);

        // Row 2: Center Viewport Button (Full Width when entity is selected)
        btnCenter = new Button();
        btnCenter.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.btn_center"));
        btnCenter.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("tracked_ant.btn_center.tt"));
        btnCenter.setMaxWidth(Double.MAX_VALUE);
        btnCenter.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 5 8; -fx-background-radius: 4;");
        btnCenter.setOnAction(e -> {
            if (onCenterHandler != null) {
                onCenterHandler.run();
            }
        });

        // Row 3: ID Search Row
        HBox searchRow = new HBox(6);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        txtAntId = new TextField();
        txtAntId.promptTextProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.prompt"));
        txtAntId.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("tracked_ant.prompt.tt"));
        txtAntId.setStyle("-fx-background-color: #1e293b; -fx-text-fill: white; -fx-border-color: #334155; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-size: 11px;");
        HBox.setHgrow(txtAntId, Priority.ALWAYS);
        txtAntId.setOnAction(e -> triggerSearch());

        btnDirectFollow = new Button();
        btnDirectFollow.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.btn_search"));
        btnDirectFollow.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("tracked_ant.btn_search.tt"));
        btnDirectFollow.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 4 10;");
        btnDirectFollow.setOnAction(e -> triggerSearch());

        searchRow.getChildren().addAll(txtAntId, btnDirectFollow);

        lblSearchStatus = new Label();
        lblSearchStatus.setStyle("-fx-font-size: 10px; -fx-text-fill: #ef4444;");
        lblSearchStatus.setVisible(false);

        controlsBox.getChildren().addAll(followActionRow, btnCenter, searchRow, lblSearchStatus);

        getChildren().addAll(headerBox, sep1, telemetryBox, sep2, controlsBox);
        org.swarmforge.client.util.ThemeManager.getInstance().currentThemeProperty().addListener((obs, o, n) -> applyThemeStyle());
        applyThemeStyle();
        setNoAntSelectedState();
    }

    public void applyThemeStyle() {
        boolean isDark = org.swarmforge.client.util.ThemeManager.getInstance().isDarkMode();
        if (isDark) {
            setStyle("-fx-background-color: rgba(15, 23, 42, 0.94); " +
                    "-fx-border-color: #f59e0b; -fx-border-width: 1.5; " +
                    "-fx-border-radius: 10; -fx-background-radius: 10;");
            titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #f59e0b;");
            btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand;");
            btnPrevAnt.setStyle("-fx-background-color: rgba(51, 65, 85, 0.8); -fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 1 6; -fx-background-radius: 4; -fx-border-color: #475569; -fx-border-radius: 4; -fx-border-width: 1; -fx-cursor: hand;");
            btnNextAnt.setStyle("-fx-background-color: rgba(51, 65, 85, 0.8); -fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 1 6; -fx-background-radius: 4; -fx-border-color: #475569; -fx-border-radius: 4; -fx-border-width: 1; -fx-cursor: hand;");
            lblSpeciesColony.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");
            lblHealthText.setStyle("-fx-font-size: 11px; -fx-text-fill: #cbd5e1;");
            lblEnergyHungerThirst.setStyle("-fx-font-size: 11px; -fx-text-fill: #38bdf8;");
            lblAgeStageJob.setStyle("-fx-font-size: 11px; -fx-text-fill: #e2e8f0;");
            lblAiState.setStyle("-fx-font-size: 11px; -fx-text-fill: #a78bfa;");
            lblPos3D.setStyle("-fx-font-size: 11px; -fx-text-fill: #cbd5e1;");
            lblHeadingCargo.setStyle("-fx-font-size: 11px; -fx-text-fill: #cbd5e1;");
            lblChcGestalt.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
            txtAntId.setStyle("-fx-background-color: #1e293b; -fx-text-fill: white; -fx-border-color: #334155; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-size: 11px;");
        } else {
            setStyle("-fx-background-color: rgba(255, 255, 255, 0.96); " +
                    "-fx-border-color: #0284c7; -fx-border-width: 1.5; " +
                    "-fx-border-radius: 10; -fx-background-radius: 10; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 4);");
            titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0369a1;");
            btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand;");
            btnPrevAnt.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #0284c7; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 1 6; -fx-background-radius: 4; -fx-border-color: #cbd5e1; -fx-border-radius: 4; -fx-border-width: 1; -fx-cursor: hand;");
            btnNextAnt.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #0284c7; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 1 6; -fx-background-radius: 4; -fx-border-color: #cbd5e1; -fx-border-radius: 4; -fx-border-width: 1; -fx-cursor: hand;");
            lblSpeciesColony.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #0284c7;");
            lblHealthText.setStyle("-fx-font-size: 11px; -fx-text-fill: #334155;");
            lblEnergyHungerThirst.setStyle("-fx-font-size: 11px; -fx-text-fill: #0284c7;");
            lblAgeStageJob.setStyle("-fx-font-size: 11px; -fx-text-fill: #1e293b;");
            lblAiState.setStyle("-fx-font-size: 11px; -fx-text-fill: #6d28d9;");
            lblPos3D.setStyle("-fx-font-size: 11px; -fx-text-fill: #334155;");
            lblHeadingCargo.setStyle("-fx-font-size: 11px; -fx-text-fill: #334155;");
            lblChcGestalt.setStyle("-fx-font-size: 11px; -fx-text-fill: #475569;");
            txtAntId.setStyle("-fx-background-color: #f8fafc; -fx-text-fill: #0f172a; -fx-border-color: #cbd5e1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-size: 11px;");
        }
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

    private Predator currentPredator = null;

    public void setNoAntSelectedState() {
        if (!javafx.application.Platform.isFxApplicationThread()) {
            javafx.application.Platform.runLater(this::setNoAntSelectedState);
            return;
        }
        this.currentAnt = null;
        this.currentPredator = null;
        this.isFollowing = false;

        titleLabel.textProperty().unbind();
        titleLabel.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.no_ant_title"));

        lblHealthText.textProperty().unbind();
        lblHealthText.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.health_none"));
        healthBar.setProgress(0);

        lblSpeciesColony.setText("ðŸ§¬ EspÃ¨ce: - | ðŸ›ï¸ Colonie: -");

        lblEnergyHungerThirst.textProperty().unbind();
        lblEnergyHungerThirst.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.metrics_none"));

        lblAgeStageJob.textProperty().unbind();
        lblAgeStageJob.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.age_job_none"));

        lblAiState.textProperty().unbind();
        lblAiState.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.ai_state_none"));
        lblAiState.setTooltip(null);
        lblAiState.setStyle("-fx-font-size: 11px; -fx-text-fill: #a78bfa;");

        lblPos3D.textProperty().unbind();
        lblPos3D.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.pos3d_none"));

        lblHeadingCargo.textProperty().unbind();
        lblHeadingCargo.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.heading_cargo_none"));

        lblChcGestalt.textProperty().unbind();
        lblChcGestalt.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.chc_gestalt_none"));
        lblChcGestalt.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        btnFollowTps.setDisable(true);
        btnFollowFps.setDisable(true);
        btnCenter.setVisible(false);
        btnCenter.setManaged(false);
        btnCenter.setDisable(true);
        clearSearchStatus();
    }

    public void updateAnt(Individual ant, boolean following) {
        if (!javafx.application.Platform.isFxApplicationThread()) {
            javafx.application.Platform.runLater(() -> updateAnt(ant, following));
            return;
        }
        this.currentAnt = ant;
        this.currentPredator = null;
        this.isFollowing = following;

        if (ant == null) {
            setNoAntSelectedState();
            return;
        }

        String idStr = ant.getId() != null ? ant.getId().toString() : "N/A";
        String shortId = idStr.length() > 8 ? idStr.substring(0, 8) : idStr;
        String formattedId = ant.getFormattedId();

        titleLabel.textProperty().unbind();
        String statusText = following ? I18nManager.getInstance().get("tracked_ant.status_tracked") : I18nManager.getInstance().get("tracked_ant.status_selected");
        titleLabel.setText(String.format("ðŸŽ¯ %s %s: %s [%s] (#%s)", I18nManager.getInstance().get("tracked_ant.title"), statusText, formatCaste(ant.getCaste()), formattedId, shortId));

        // Species & Colony
        String speciesName = ant.getSpecies() != null ? ant.getSpecies().getScientificName() : "Formica fusca";
        String colonyIdStr = ant.getColonyId() != null ? ant.getColonyId().toString().substring(0, Math.min(8, ant.getColonyId().toString().length())) : "N/A";
        lblSpeciesColony.setText(String.format("ðŸ§¬ %s | ðŸ›ï¸ Colonie #%s", speciesName, colonyIdStr));

        // Health
        double health = ant.getHealth();
        boolean alive = ant.isAlive() && health > 0;
        lblHealthText.textProperty().unbind();
        if (!alive) {
            String cod = ant.getCauseOfDeath() != null ? " (" + ant.getCauseOfDeath() + ")" : "";
            lblHealthText.setText(I18nManager.getInstance().get("tracked_ant.health", 0.0) + " ðŸ’€ [MORT / DÃ‰CÃ‰DÃ‰" + cod + "]");
            healthBar.setProgress(0);
            healthBar.setStyle("-fx-accent: #64748b;");
        } else {
            lblHealthText.setText(I18nManager.getInstance().get("tracked_ant.health", health));
            healthBar.setProgress(Math.max(0, Math.min(1.0, health / 100.0)));
            healthBar.setStyle(health > 50 ? "-fx-accent: #22c55e;" : "-fx-accent: #ef4444;");
        }

        // Energy / Hunger / Thirst
        lblEnergyHungerThirst.textProperty().unbind();
        lblEnergyHungerThirst.setText(I18nManager.getInstance().get("tracked_ant.metrics", ant.getEnergy(), ant.getHunger(), ant.getThirst()));

        // Age in Days (1 day = 86400 seconds)
        double ageDays = ant.getAge() / 86400.0;
        lblAgeStageJob.textProperty().unbind();
        lblAgeStageJob.setText(I18nManager.getInstance().get("tracked_ant.age_job", ageDays, formatLifeStage(ant.getLifeStage()), formatJob(ant.getJob())));

        // AI State & Behaviors
        String rawBehaviors = ant.getActiveBehaviorsSummary();
        lblAiState.textProperty().unbind();
        String stateStr = String.format("ðŸ§  IA : %s | %s", ant.getState() != null ? ant.getState() : "PATROUILLE", formatJob(ant.getJob()));
        lblAiState.setText(stateStr);

        // Rich Mouse-over Tooltip with complete behavioral and ethological state
        String fullAiDetails = TrackedIndividualFormatter.buildAntAiTooltip(ant);
        Tooltip aiTooltip = new Tooltip(fullAiDetails);
        aiTooltip.setShowDelay(javafx.util.Duration.millis(80));
        aiTooltip.setStyle("-fx-font-size: 11px;");
        lblAiState.setTooltip(aiTooltip);
        lblAiState.setStyle("-fx-font-size: 11px; -fx-text-fill: #a78bfa; -fx-cursor: hand; -fx-underline: true;");

        // 3D Position
        lblPos3D.textProperty().unbind();
        lblPos3D.setText(I18nManager.getInstance().get("tracked_ant.pos3d", ant.getX(), ant.getY(), ant.getZ()));

        // Heading & Cargo
        String cargo = formatCarriedItem(ant.getCarriedItem());
        lblHeadingCargo.textProperty().unbind();
        lblHeadingCargo.setText(I18nManager.getInstance().get("tracked_ant.heading_cargo", Math.toDegrees(ant.getHeading()), cargo));

        // CHC Gestalt
        lblChcGestalt.textProperty().unbind();
        lblChcGestalt.setText(I18nManager.getInstance().get("tracked_ant.chc_gestalt"));
        lblChcGestalt.setStyle("-fx-font-size: 11px; -fx-text-fill: #4ade80;");

        btnFollowTps.setDisable(false);
        btnFollowFps.setDisable(false);

        btnCenter.setVisible(true);
        btnCenter.setManaged(true);
        btnCenter.setDisable(false);
        clearSearchStatus();
    }

    public void updatePredator(Predator predator, boolean following) {
        if (!javafx.application.Platform.isFxApplicationThread()) {
            javafx.application.Platform.runLater(() -> updatePredator(predator, following));
            return;
        }
        this.currentPredator = predator;
        this.currentAnt = null;
        this.isFollowing = following;
        TrackedEntityUpdater.updatePredator(this, predator, following);
    }

    public void updateChamber(org.swarmforge.core.simulation.TunnelNetwork.TunnelNode node, org.swarmforge.core.domain.Colony colony) {
        this.currentAnt = null;
        this.currentPredator = null;
        this.isFollowing = false;
        TrackedEntityUpdater.updateChamber(this, node, colony);
    }

    public static String formatCaste(Individual.Caste caste) {
        return TrackedIndividualFormatter.formatCaste(caste);
    }

    public static String formatLifeStage(Individual.LifeStage stage) {
        return TrackedIndividualFormatter.formatLifeStage(stage);
    }

    public static String formatJob(Individual.Job job) {
        return TrackedIndividualFormatter.formatJob(job);
    }

    public static String formatCarriedItem(Individual.CarriedItem item) {
        return TrackedIndividualFormatter.formatCarriedItem(item);
    }

    public static String formatActionType(Object actionType) {
        return TrackedIndividualFormatter.formatActionType(actionType);
    }

    public static String formatNodeType(Object type) {
        return TrackedIndividualFormatter.formatNodeType(type);
    }

    public void setOnFollowAnt(Consumer<Individual> handler) {
        this.onFollowAntHandler = handler;
    }

    public void setOnFollowAntMode(java.util.function.BiConsumer<Individual, CameraFollowMode> handler) {
        this.onFollowAntModeHandler = handler;
    }

    public void setOnFollowAntById(Consumer<String> handler) {
        this.onFollowAntByIdHandler = handler;
    }

    public void setOnStopFollow(Runnable handler) {
        this.onStopFollowHandler = handler;
    }

    public void setOnCenter(Runnable handler) {
        this.onCenterHandler = handler;
    }

    public void setOnClose(Runnable handler) {
        this.onCloseHandler = handler;
    }

    public void setOnNextAnt(Runnable handler) {
        this.onNextAntHandler = handler;
    }

    public void setOnPreviousAnt(Runnable handler) {
        this.onPreviousAntHandler = handler;
    }

    public Individual getCurrentAnt() {
        return currentAnt;
    }

    public Predator getCurrentPredator() {
        return currentPredator;
    }
}


