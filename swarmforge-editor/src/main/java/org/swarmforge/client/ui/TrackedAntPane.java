/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
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
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class TrackedAntPane extends VBox {

    private final Label titleLabel;
    private final Button btnClose;

    // Telemetry Labels
    private final Label lblSpeciesColony;
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
    private final Button btnFollowTps;
    private final Button btnFollowFps;
    private final Button btnCenter;
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

    public TrackedAntPane() {
        setSpacing(6);
        setPadding(new Insets(10, 12, 10, 12));
        setPrefWidth(340);
        setMaxWidth(360);
        setStyle("-fx-background-color: rgba(15, 23, 42, 0.94); " +
                "-fx-border-color: #f59e0b; -fx-border-width: 1.5; " +
                "-fx-border-radius: 10; -fx-background-radius: 10;");

        // Header
        HBox headerBox = new HBox(8);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        titleLabel = new Label();
        titleLabel.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.no_ant_title"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnClose = new Button("✕");
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

        headerBox.getChildren().addAll(titleLabel, spacer, btnClose);

        Separator sep1 = new Separator();
        sep1.setStyle("-fx-background-color: rgba(245, 158, 11, 0.3);");

        // Telemetry Box
        telemetryBox = new VBox(5);

        // 0. Species & Colony Row
        lblSpeciesColony = new Label("🧬 Espèce: - | 🏛️ Colonie: -");
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
        this.currentAnt = null;
        this.currentPredator = null;
        this.isFollowing = false;

        titleLabel.textProperty().unbind();
        titleLabel.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.no_ant_title"));

        lblHealthText.textProperty().unbind();
        lblHealthText.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.health_none"));
        healthBar.setProgress(0);

        lblSpeciesColony.setText("🧬 Espèce: - | 🏛️ Colonie: -");

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
        titleLabel.setText(String.format("🎯 %s %s: %s [%s] (#%s)", I18nManager.getInstance().get("tracked_ant.title"), statusText, ant.getCaste(), formattedId, shortId));

        // Species & Colony
        String speciesName = ant.getSpecies() != null ? ant.getSpecies().getScientificName() : "Formica fusca";
        String colonyIdStr = ant.getColonyId() != null ? ant.getColonyId().toString().substring(0, Math.min(8, ant.getColonyId().toString().length())) : "N/A";
        lblSpeciesColony.setText(String.format("🧬 %s | 🏛️ Colonie #%s", speciesName, colonyIdStr));

        // Health
        double health = ant.getHealth();
        boolean alive = ant.isAlive() && health > 0;
        lblHealthText.textProperty().unbind();
        if (!alive) {
            String cod = ant.getCauseOfDeath() != null ? " (" + ant.getCauseOfDeath() + ")" : "";
            lblHealthText.setText(I18nManager.getInstance().get("tracked_ant.health", 0.0) + " 💀 [MORT / DÉCÉDÉ" + cod + "]");
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
        lblAgeStageJob.setText(I18nManager.getInstance().get("tracked_ant.age_job", ageDays, ant.getLifeStage(), ant.getJob()));

        // AI State & Behaviors
        String rawBehaviors = ant.getActiveBehaviorsSummary();
        lblAiState.textProperty().unbind();
        String stateStr = String.format("🧠 IA : %s | %s", ant.getState() != null ? ant.getState() : "PATROUILLE", ant.getJob() != null ? ant.getJob() : "Généraliste");
        lblAiState.setText(stateStr);

        // Rich Mouse-over Tooltip with complete behavioral and ethological state
        String fullAiDetails = String.format(
            "🧠 IA - État Cognitif : %s\n" +
            "💼 Tâche / Rôle Assigné : %s\n" +
            "⚡ Action en cours : %s\n" +
            "📋 Capacités & Comportements IA Actifs :\n  • %s\n" +
            "🧬 Profil Éthologique : %s",
            ant.getState() != null ? ant.getState() : "PATROUILLE",
            ant.getJob() != null ? ant.getJob() : "Généraliste",
            ant.getCachedAction() != null && ant.getCachedAction().type() != null ? ant.getCachedAction().type().name() : "Recherche autonome",
            (rawBehaviors != null && !rawBehaviors.isBlank()) ? rawBehaviors.replace(";", "\n  • ") : "Patrouille & navigation standard",
            ant.getSpecies() != null ? (ant.getSpecies().getCommonName() != null ? ant.getSpecies().getCommonName() : ant.getSpecies().getScientificName()) : "Standard"
        );
        Tooltip aiTooltip = new Tooltip(fullAiDetails);
        aiTooltip.setShowDelay(javafx.util.Duration.millis(80));
        aiTooltip.setStyle("-fx-font-size: 11px;");
        lblAiState.setTooltip(aiTooltip);
        lblAiState.setStyle("-fx-font-size: 11px; -fx-text-fill: #a78bfa; -fx-cursor: hand; -fx-underline: true;");

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

        btnFollowTps.setDisable(false);
        btnFollowFps.setDisable(false);

        btnCenter.setVisible(true);
        btnCenter.setManaged(true);
        btnCenter.setDisable(false);
        clearSearchStatus();
    }

    public void updatePredator(Predator predator, boolean following) {
        this.currentPredator = predator;
        this.currentAnt = null;
        this.isFollowing = following;

        if (predator == null) {
            setNoAntSelectedState();
            return;
        }

        String idStr = predator.getId() != null ? predator.getId().toString() : "N/A";
        String shortId = idStr.length() > 8 ? idStr.substring(0, 8) : idStr;

        titleLabel.textProperty().unbind();
        String statusText = following ? I18nManager.getInstance().get("tracked_ant.status_tracked") : I18nManager.getInstance().get("tracked_ant.status_selected");
        titleLabel.setText(String.format("🎯 %s: %s #%s", predator.getType().getDisplayName(), statusText, shortId));

        // Species & Type
        lblSpeciesColony.setText(String.format("🦅 %s | Tactique: %s", predator.getType().getDisplayName(), predator.getType().getHuntingStyle()));

        // Health
        double health = predator.getHealth();
        double maxHealth = predator.getMaxHealth();
        boolean alive = predator.isAlive() && health > 0;
        lblHealthText.textProperty().unbind();
        if (!alive) {
            lblHealthText.setText(String.format(Locale.US, "Santé: 0.0 / %.0f 💀 [MORT]", maxHealth));
            healthBar.setProgress(0);
            healthBar.setStyle("-fx-accent: #64748b;");
        } else {
            lblHealthText.setText(String.format(Locale.US, "Santé: %.1f / %.0f", health, maxHealth));
            healthBar.setProgress(Math.max(0, Math.min(1.0, health / maxHealth)));
            healthBar.setStyle((health / maxHealth) > 0.5 ? "-fx-accent: #22c55e;" : "-fx-accent: #ef4444;");
        }

        // Energy & Hunger
        lblEnergyHungerThirst.textProperty().unbind();
        lblEnergyHungerThirst.setText(String.format(Locale.US, "⚡ Énergie: %.0f%% | 🍖 Faim: %.0f%%", predator.getEnergy(), predator.getHunger()));

        // Age & Kills
        lblAgeStageJob.textProperty().unbind();
        double predAgeDays = predator.getAgeInSeconds() / 86400.0;
        lblAgeStageJob.setText(String.format(Locale.US, "⏳ Âge: %.1f jours | ⚔️ Butins: %d", predAgeDays, predator.getKillCount()));

        // AI State (Focus strictly on AI intelligence, decisions, and sensory perception)
        String targetName = predator.getCurrentTarget() != null ? ("Fourmi " + predator.getCurrentTarget().getCaste()) : "Aucune (Recherche)";
        String stateStr = String.format("🧠 IA : %s | Cible: %s", predator.getState(), targetName);
        lblAiState.textProperty().unbind();
        lblAiState.setText(stateStr);

        String fullPredatorDetails = String.format(
            "🧠 IA - État Décisionnel : %s\n" +
            "🎯 Cible Verrouillée : %s\n" +
            "🏹 Tactique de Chasse IA : %s\n" +
            "👁️ Rayon de Perception Sensorielle : %.1f m\n" +
            "🕸️ Piège / Embuscade IA : %s\n" +
            "⚡ Vitesse de Traque IA : %.1f m/s",
            predator.getState(),
            targetName,
            predator.getType().getHuntingStyle(),
            predator.getType().getVisionRange(),
            predator.isTrapBuilt() ? "Posé & Actif" : "En prospection",
            predator.getType().getBaseSpeed()
        );
        Tooltip aiTooltip = new Tooltip(fullPredatorDetails);
        aiTooltip.setShowDelay(javafx.util.Duration.millis(80));
        aiTooltip.setStyle("-fx-font-size: 11px;");
        lblAiState.setTooltip(aiTooltip);
        lblAiState.setStyle("-fx-font-size: 11px; -fx-text-fill: #f59e0b; -fx-cursor: hand; -fx-underline: true;");

        // 3D Position
        lblPos3D.textProperty().unbind();
        lblPos3D.setText(String.format(Locale.US, "📍 Pos: (%.1f, %.1f, %.1f)", predator.getX(), predator.getY(), predator.getZ()));

        // Heading & Trap
        lblHeadingCargo.textProperty().unbind();
        lblHeadingCargo.setText(String.format(Locale.US, "🧭 Cap: %.0f° | Piège: %s", Math.toDegrees(predator.getHeading()), predator.isTrapBuilt() ? "Construit" : "Aucun"));

        // Status
        lblChcGestalt.textProperty().unbind();
        lblChcGestalt.setText(predator.isAlive() ? "🟢 Menace Active" : "⚫ Inactif");
        lblChcGestalt.setStyle(predator.isAlive() ? "-fx-font-size: 11px; -fx-text-fill: #ef4444;" : "-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        btnFollowTps.setDisable(true);
        btnFollowFps.setDisable(true);
        btnCenter.setVisible(true);
        btnCenter.setManaged(true);
        btnCenter.setDisable(false);
        clearSearchStatus();
    }

    public void updateChamber(org.swarmforge.core.simulation.TunnelNetwork.TunnelNode node, org.swarmforge.core.domain.Colony colony) {
        this.currentAnt = null;
        this.currentPredator = null;
        this.isFollowing = false;

        if (node == null) {
            setNoAntSelectedState();
            return;
        }

        titleLabel.textProperty().unbind();
        titleLabel.setText("🏛️ Chambre : " + node.type().name());

        String speciesName = (colony != null && colony.getSpecies() != null) ? colony.getSpecies().getScientificName() : "Formica sp.";
        String colonyIdStr = (colony != null && colony.getId() != null) ? colony.getId().toString().substring(0, Math.min(8, colony.getId().toString().length())) : "N/A";
        lblSpeciesColony.setText(String.format("🧬 %s | 🏛️ Colonie #%s", speciesName, colonyIdStr));

        // Capacity and occupant count
        int occupants = 0;
        int queens = 0, workers = 0, soldiers = 0, males = 0;
        if (colony != null) {
            for (Individual ind : colony.getLivingIndividuals()) {
                double dist = Math.hypot(ind.getX() - node.x(), Math.hypot(ind.getY() - node.y(), ind.getZ() - node.z()));
                if (dist <= Math.max(node.radiusX(), node.radiusZ()) * 1.5) {
                    occupants++;
                    if (ind.getCaste() == Individual.Caste.QUEEN) queens++;
                    else if (ind.getCaste() == Individual.Caste.SOLDIER) soldiers++;
                    else if (ind.getCaste() == Individual.Caste.MALE) males++;
                    else workers++;
                }
            }
        }

        lblHealthText.textProperty().unbind();
        lblHealthText.setText(String.format(Locale.US, "👥 Occupants réels : %d (👑 %d | ⚒️ %d | ⚔️ %d | ♂ %d)", occupants, queens, workers, soldiers, males));
        healthBar.setProgress(Math.min(1.0, occupants / 50.0));
        healthBar.setStyle("-fx-accent: #38bdf8;");

        lblEnergyHungerThirst.textProperty().unbind();
        float foodStored = (colony != null) ? colony.getFoodStored() : 0.0f;
        lblEnergyHungerThirst.setText(String.format(Locale.US, "📦 Réserve Nourriture: %.1f | Rayon: %.1f m", foodStored, node.radiusX()));

        lblAgeStageJob.textProperty().unbind();
        lblAgeStageJob.setText(String.format(Locale.US, "📐 Position: X=%.1f m, Y=%.1f m, Profondeur=%.1f m", node.x(), node.y(), -node.z()));

        lblAiState.textProperty().unbind();
        lblAiState.setText("Type architectural : " + node.type().name());
        lblAiState.setTooltip(new Tooltip("Chambre biogène souterraine\nVolume lenticulaire adapté à la régulation microclimatique"));
        lblAiState.setStyle("-fx-font-size: 11px; -fx-text-fill: #38bdf8;");

        lblPos3D.textProperty().unbind();
        lblPos3D.setText(String.format(Locale.US, "Dimensions cavité: Rx=%.1fm, Ry=%.1fm, Rz=%.1fm", node.radiusX(), node.radiusY(), node.radiusZ()));

        lblHeadingCargo.textProperty().unbind();
        lblHeadingCargo.setText("Identifiant Nodule: #" + (node.id() != null ? node.id().toString().substring(0, Math.min(8, node.id().toString().length())) : "0"));

        lblChcGestalt.textProperty().unbind();
        lblChcGestalt.setText("🟢 Structure Stable & Intacte");
        lblChcGestalt.setStyle("-fx-font-size: 11px; -fx-text-fill: #22c55e;");

        btnFollowTps.setDisable(true);
        btnFollowFps.setDisable(true);
        btnCenter.setVisible(true);
        btnCenter.setManaged(true);
        btnCenter.setDisable(false);
        clearSearchStatus();
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

    public Individual getCurrentAnt() {
        return currentAnt;
    }

    public Predator getCurrentPredator() {
        return currentPredator;
    }
}
