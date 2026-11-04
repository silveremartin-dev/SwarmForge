/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.scene.control.Tooltip;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Predator;
import org.swarmforge.core.simulation.TunnelNetwork;

import java.util.Locale;

/**
 * Helper updater for telemetry displays of predators and nest chambers in {@link TrackedIndividualPane}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
final class TrackedEntityUpdater {

    private TrackedEntityUpdater() {
    }

    static void setNoAntSelectedState(TrackedIndividualPane pane) {
        pane.titleLabel.textProperty().unbind();
        pane.titleLabel.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.no_ant_title"));

        pane.lblHealthText.textProperty().unbind();
        pane.lblHealthText.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.health_none"));
        pane.healthBar.setProgress(0);

        pane.lblSpeciesColony.setText("🧬 Espèce: - | 🏛️ Colonie: -");

        pane.lblEnergyHungerThirst.textProperty().unbind();
        pane.lblEnergyHungerThirst.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.metrics_none"));

        pane.lblAgeStageJob.textProperty().unbind();
        pane.lblAgeStageJob.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.age_job_none"));

        pane.lblAiState.textProperty().unbind();
        pane.lblAiState.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.ai_state_none"));
        pane.lblAiState.setTooltip(null);
        pane.lblAiState.setStyle("-fx-font-size: 11px; -fx-text-fill: #a78bfa;");

        pane.lblPos3D.textProperty().unbind();
        pane.lblPos3D.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.pos3d_none"));

        pane.lblHeadingCargo.textProperty().unbind();
        pane.lblHeadingCargo.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.heading_cargo_none"));

        pane.lblChcGestalt.textProperty().unbind();
        pane.lblChcGestalt.textProperty().bind(I18nManager.getInstance().createStringBinding("tracked_ant.chc_gestalt_none"));
        pane.lblChcGestalt.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        pane.btnFollowTps.setDisable(true);
        pane.btnFollowFps.setDisable(true);
        pane.btnCenter.setVisible(false);
        pane.btnCenter.setManaged(false);
        pane.btnCenter.setDisable(true);
        pane.clearSearchStatus();
    }

    static void updateAnt(TrackedIndividualPane pane, Individual ant, boolean following) {
        if (ant == null) {
            pane.setNoAntSelectedState();
            return;
        }

        String idStr = ant.getId() != null ? ant.getId().toString() : "N/A";
        String shortId = idStr.length() > 8 ? idStr.substring(0, 8) : idStr;
        String formattedId = ant.getFormattedId();

        pane.titleLabel.textProperty().unbind();
        String statusText = following ? I18nManager.getInstance().get("tracked_ant.status_tracked") : I18nManager.getInstance().get("tracked_ant.status_selected");
        pane.titleLabel.setText(String.format("🎯 %s %s: %s [%s] (#%s)", I18nManager.getInstance().get("tracked_ant.title"), statusText, TrackedIndividualPane.formatCaste(ant.getCaste()), formattedId, shortId));

        // Species & Colony
        String speciesName = ant.getSpecies() != null ? ant.getSpecies().getScientificName() : "Formica fusca";
        String colonyIdStr = ant.getColonyId() != null ? ant.getColonyId().toString().substring(0, Math.min(8, ant.getColonyId().toString().length())) : "N/A";
        pane.lblSpeciesColony.setText(String.format("🧬 %s | 🏛️ Colonie #%s", speciesName, colonyIdStr));

        // Health
        double health = ant.getHealth();
        boolean alive = ant.isAlive() && health > 0;
        pane.lblHealthText.textProperty().unbind();
        if (!alive) {
            String cod = ant.getCauseOfDeath() != null ? " (" + ant.getCauseOfDeath() + ")" : "";
            pane.lblHealthText.setText(I18nManager.getInstance().get("tracked_ant.health", 0.0) + " 💀 [MORT / DÉCÉDÉ" + cod + "]");
            pane.healthBar.setProgress(0);
            pane.healthBar.setStyle("-fx-accent: #64748b;");
        } else {
            pane.lblHealthText.setText(I18nManager.getInstance().get("tracked_ant.health", health));
            pane.healthBar.setProgress(Math.max(0, Math.min(1.0, health / 100.0)));
            pane.healthBar.setStyle(health > 50 ? "-fx-accent: #22c55e;" : "-fx-accent: #ef4444;");
        }

        // Energy / Hunger / Thirst
        pane.lblEnergyHungerThirst.textProperty().unbind();
        pane.lblEnergyHungerThirst.setText(I18nManager.getInstance().get("tracked_ant.metrics", ant.getEnergy(), ant.getHunger(), ant.getThirst()));

        // Age in Days (1 day = 86400 seconds)
        double ageDays = ant.getAge() / 86400.0;
        pane.lblAgeStageJob.textProperty().unbind();
        pane.lblAgeStageJob.setText(I18nManager.getInstance().get("tracked_ant.age_job", ageDays, TrackedIndividualPane.formatLifeStage(ant.getLifeStage()), TrackedIndividualPane.formatJob(ant.getJob())));

        // AI State & Behaviors
        pane.lblAiState.textProperty().unbind();
        String stateStr = String.format("🧠 IA : %s | %s", ant.getState() != null ? ant.getState() : "PATROUILLE", TrackedIndividualPane.formatJob(ant.getJob()));
        pane.lblAiState.setText(stateStr);

        // Rich Mouse-over Tooltip with complete behavioral and ethological state
        String fullAiDetails = TrackedIndividualFormatter.buildAntAiTooltip(ant);
        Tooltip aiTooltip = new Tooltip(fullAiDetails);
        aiTooltip.setShowDelay(javafx.util.Duration.millis(80));
        aiTooltip.setStyle("-fx-font-size: 11px;");
        pane.lblAiState.setTooltip(aiTooltip);
        pane.lblAiState.setStyle("-fx-font-size: 11px; -fx-text-fill: #a78bfa; -fx-cursor: hand; -fx-underline: true;");

        // 3D Position
        pane.lblPos3D.textProperty().unbind();
        pane.lblPos3D.setText(I18nManager.getInstance().get("tracked_ant.pos3d", ant.getX(), ant.getY(), ant.getZ()));

        // Heading & Cargo
        String cargo = TrackedIndividualPane.formatCarriedItem(ant.getCarriedItem());
        pane.lblHeadingCargo.textProperty().unbind();
        pane.lblHeadingCargo.setText(I18nManager.getInstance().get("tracked_ant.heading_cargo", Math.toDegrees(ant.getHeading()), cargo));

        // CHC Gestalt
        pane.lblChcGestalt.textProperty().unbind();
        pane.lblChcGestalt.setText(I18nManager.getInstance().get("tracked_ant.chc_gestalt"));
        pane.lblChcGestalt.setStyle("-fx-font-size: 11px; -fx-text-fill: #4ade80;");

        pane.btnFollowTps.setDisable(false);
        pane.btnFollowFps.setDisable(false);

        pane.btnCenter.setVisible(true);
        pane.btnCenter.setManaged(true);
        pane.btnCenter.setDisable(false);
        pane.clearSearchStatus();
    }

    static void updatePredator(TrackedIndividualPane pane, Predator predator, boolean following) {
        if (predator == null) {
            pane.setNoAntSelectedState();
            return;
        }

        String idStr = predator.getId() != null ? predator.getId().toString() : "N/A";
        String shortId = idStr.length() > 8 ? idStr.substring(0, 8) : idStr;

        pane.titleLabel.textProperty().unbind();
        String statusText = following ? I18nManager.getInstance().get("tracked_ant.status_tracked") : I18nManager.getInstance().get("tracked_ant.status_selected");
        pane.titleLabel.setText(String.format("🎯 %s: %s #%s", predator.getType().getDisplayName(), statusText, shortId));

        // Species & Type
        pane.lblSpeciesColony.setText(String.format("🦅 %s | Tactique: %s", predator.getType().getDisplayName(), predator.getType().getHuntingStyle()));

        // Health
        double health = predator.getHealth();
        double maxHealth = predator.getMaxHealth();
        boolean alive = predator.isAlive() && health > 0;
        pane.lblHealthText.textProperty().unbind();
        if (!alive) {
            pane.lblHealthText.setText(String.format(Locale.US, "Santé: 0.0 / %.0f 💀 [MORT]", maxHealth));
            pane.healthBar.setProgress(0);
            pane.healthBar.setStyle("-fx-accent: #64748b;");
        } else {
            pane.lblHealthText.setText(String.format(Locale.US, "Santé: %.1f / %.0f", health, maxHealth));
            pane.healthBar.setProgress(Math.max(0, Math.min(1.0, health / maxHealth)));
            pane.healthBar.setStyle((health / maxHealth) > 0.5 ? "-fx-accent: #22c55e;" : "-fx-accent: #ef4444;");
        }

        // Energy & Hunger
        pane.lblEnergyHungerThirst.textProperty().unbind();
        pane.lblEnergyHungerThirst.setText(String.format(Locale.US, "⚡ Énergie: %.0f%% | 🍗 Faim: %.0f%%", predator.getEnergy(), predator.getHunger()));

        // Age & Kills
        pane.lblAgeStageJob.textProperty().unbind();
        double predAgeDays = predator.getAgeInSeconds() / 86400.0;
        pane.lblAgeStageJob.setText(String.format(Locale.US, "⏳ Âge: %.1f jours | ⚔️ Butins: %d", predAgeDays, predator.getKillCount()));

        // AI State (Focus strictly on AI intelligence, decisions, and sensory perception)
        String targetName = predator.getCurrentTarget() != null ? ("Fourmi " + predator.getCurrentTarget().getCaste()) : "Aucune (Recherche)";
        String stateStr = String.format("🧠 IA : %s | Cible: %s", predator.getState(), targetName);
        pane.lblAiState.textProperty().unbind();
        pane.lblAiState.setText(stateStr);

        String fullPredatorDetails = TrackedIndividualFormatter.buildPredatorAiTooltip(predator, targetName);
        Tooltip aiTooltip = new Tooltip(fullPredatorDetails);
        aiTooltip.setShowDelay(javafx.util.Duration.millis(80));
        aiTooltip.setStyle("-fx-font-size: 11px;");
        pane.lblAiState.setTooltip(aiTooltip);
        pane.lblAiState.setStyle("-fx-font-size: 11px; -fx-text-fill: #f59e0b; -fx-cursor: hand; -fx-underline: true;");

        // 3D Position
        pane.lblPos3D.textProperty().unbind();
        pane.lblPos3D.setText(String.format(Locale.US, "📍 Pos: (%.1f, %.1f, %.1f)", predator.getX(), predator.getY(), predator.getZ()));

        // Heading & Trap
        pane.lblHeadingCargo.textProperty().unbind();
        pane.lblHeadingCargo.setText(String.format(Locale.US, "🧭 Cap: %.0f° | Piège: %s", Math.toDegrees(predator.getHeading()), predator.isTrapBuilt() ? "Construit" : "Aucun"));

        // Status
        pane.lblChcGestalt.textProperty().unbind();
        pane.lblChcGestalt.setText(predator.isAlive() ? "🟢 Menace Active" : "⚫ Inactif");
        pane.lblChcGestalt.setStyle(predator.isAlive() ? "-fx-font-size: 11px; -fx-text-fill: #ef4444;" : "-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        pane.btnFollowTps.setDisable(true);
        pane.btnFollowFps.setDisable(true);
        pane.btnCenter.setVisible(true);
        pane.btnCenter.setManaged(true);
        pane.btnCenter.setDisable(false);
        pane.clearSearchStatus();
    }

    static void updateChamber(TrackedIndividualPane pane, TunnelNetwork.TunnelNode node, Colony colony) {
        if (node == null) {
            pane.setNoAntSelectedState();
            return;
        }

        pane.titleLabel.textProperty().unbind();
        pane.titleLabel.setText("🏛️ Chambre : " + node.type().name());

        String speciesName = (colony != null && colony.getSpecies() != null) ? colony.getSpecies().getScientificName() : "Formica sp.";
        String colonyIdStr = (colony != null && colony.getId() != null) ? colony.getId().toString().substring(0, Math.min(8, colony.getId().toString().length())) : "N/A";
        pane.lblSpeciesColony.setText(String.format("🧬 %s | 🏛️ Colonie #%s", speciesName, colonyIdStr));

        // Capacity and occupant count
        TrackedIndividualFormatter.ChamberOccupantStats stats = TrackedIndividualFormatter.computeChamberOccupants(node, colony);

        pane.lblHealthText.textProperty().unbind();
        pane.lblHealthText.setText(String.format(Locale.US, "👥 Occupants réels : %d (👑 %d | ⚒️ %d | ⚔️ %d | ♂ %d)",
                stats.total(), stats.queens(), stats.workers(), stats.soldiers(), stats.males()));
        pane.healthBar.setProgress(Math.min(1.0, stats.total() / 50.0));
        pane.healthBar.setStyle("-fx-accent: #38bdf8;");

        pane.lblEnergyHungerThirst.textProperty().unbind();
        float foodStored = (colony != null) ? colony.getFoodStored() : 0.0f;
        pane.lblEnergyHungerThirst.setText(String.format(Locale.US, "📦 Réserve Nourriture: %.1f | Rayon: %.1f m", foodStored, node.radiusX()));

        pane.lblAgeStageJob.textProperty().unbind();
        pane.lblAgeStageJob.setText(String.format(Locale.US, "📍 Position: X=%.1f m, Y=%.1f m, Profondeur=%.1f m", node.x(), node.y(), -node.z()));

        pane.lblAiState.textProperty().unbind();
        pane.lblAiState.setText("Type : " + TrackedIndividualFormatter.formatNodeType(node.type()));
        pane.lblAiState.setTooltip(new Tooltip("Chambre biogène souterraine\nVolume lenticulaire adapté à la régulation microclimatique"));
        pane.lblAiState.setStyle("-fx-font-size: 11px; -fx-text-fill: #38bdf8;");

        pane.lblPos3D.textProperty().unbind();
        pane.lblPos3D.setText(String.format(Locale.US, "Dimensions cavité: Rx=%.1fm, Ry=%.1fm, Rz=%.1fm", node.radiusX(), node.radiusY(), node.radiusZ()));

        pane.lblHeadingCargo.textProperty().unbind();
        pane.lblHeadingCargo.setText("Identifiant Nodule: #" + (node.id() != null ? node.id().toString().substring(0, Math.min(8, node.id().toString().length())) : "0"));

        pane.lblChcGestalt.textProperty().unbind();
        pane.lblChcGestalt.setText("🟢 Structure Stable & Intacte");
        pane.lblChcGestalt.setStyle("-fx-font-size: 11px; -fx-text-fill: #22c55e;");

        pane.btnFollowTps.setDisable(true);
        pane.btnFollowFps.setDisable(true);
        pane.btnCenter.setVisible(true);
        pane.btnCenter.setManaged(true);
        pane.btnCenter.setDisable(false);
        pane.clearSearchStatus();
    }
}
