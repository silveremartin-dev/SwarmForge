/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.core.domain.Individual;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * Encapsulated individual tracked specimen card for {@link StatisticsDashboard}.
 *
 * <p>Presents individual telemetry (vitality, caste, age, ethology, carried items,
 * and Euclidean distance from natal nest) with navigation controls.</p>
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class StatisticsTrackedAntCard {

    private final I18nManager i18n = I18nManager.getInstance();
    private final VBox cardPane;

    private final TextField txtAntSearchId = new TextField("ant_1");
    private final Label lblTrackedIdDisplay = new Label("ant_1");
    private final Label lblIndivHealth = new Label("100.0%");
    private final Label lblIndivEnergy = new Label("95.0%");
    private final Label lblIndivDistance = new Label("0.0 m");
    private final Label lblIndivPayload = new Label("0.0 mg");
    private final Label lblIndivTask = new Label("Foraging (Nectar)");
    private final Label lblIndivCasteAge = new Label("Worker (14 days)");
    private final Label lblIndivSpecies = new Label("Formica fusca");
    private final Label lblIndivColony = new Label("Colonie #1");

    private String trackedAntId = "ant_1";
    private Consumer<String> onTrackAntListener;
    private Consumer<String> onSelectAntListener;
    private Runnable onSeriesReset;

    public StatisticsTrackedAntCard(Runnable onSeriesReset) {
        this.onSeriesReset = onSeriesReset;
        this.cardPane = buildCard();
    }

    private VBox buildCard() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        box.getStyleClass().add("card-pane");

        Label title = new Label();
        title.textProperty().bind(i18n.createStringBinding("stats.indiv.title"));
        title.getStyleClass().addAll("card-title", "accent-title");

        HBox inputRow = new HBox(8);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        Label lblPrompt = new Label();
        lblPrompt.textProperty().bind(i18n.createStringBinding("stats.indiv.prompt"));
        lblPrompt.getStyleClass().add("field-label");

        txtAntSearchId.promptTextProperty().bind(i18n.createStringBinding("inspector.prompt.id"));
        txtAntSearchId.setPrefWidth(140);
        txtAntSearchId.tooltipProperty().bind(i18n.createTooltipBinding("stats.ant_search.tt"));
        txtAntSearchId.setOnAction(e -> triggerTrackAndTrace());

        Button btnPrevAnt = new Button();
        btnPrevAnt.textProperty().bind(i18n.createStringBinding("inspector.btn.prev"));
        btnPrevAnt.setGraphic(new FontIcon(Feather.CHEVRON_LEFT));
        btnPrevAnt.getStyleClass().add("btn-secondary");
        btnPrevAnt.setOnAction(e -> cycleAnt(-1));

        Button btnNextAnt = new Button();
        btnNextAnt.textProperty().bind(i18n.createStringBinding("inspector.btn.next"));
        btnNextAnt.setGraphic(new FontIcon(Feather.CHEVRON_RIGHT));
        btnNextAnt.getStyleClass().add("btn-secondary");
        btnNextAnt.setOnAction(e -> cycleAnt(1));

        Button btnTrack = new Button();
        btnTrack.textProperty().bind(i18n.createStringBinding("stats.track_btn"));
        btnTrack.setGraphic(new FontIcon(Feather.TARGET));
        btnTrack.getStyleClass().add("btn-primary");
        btnTrack.tooltipProperty().bind(i18n.createTooltipBinding("stats.track_btn.tt"));
        btnTrack.setOnAction(e -> triggerTrackAndTrace());

        inputRow.getChildren().addAll(lblPrompt, txtAntSearchId, btnPrevAnt, btnNextAnt, btnTrack);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(8);
        lblTrackedIdDisplay.getStyleClass().addAll("metric-value-accent", "bold-title");

        addIndivField(grid, "stats.indiv.id", lblTrackedIdDisplay, 0, 0, 1);
        addIndivField(grid, "stats.indiv.caste_age", lblIndivCasteAge, 2, 0, 1);
        addIndivField(grid, "stats.indiv.species", lblIndivSpecies, 0, 1, 1);
        addIndivField(grid, "stats.indiv.colony", lblIndivColony, 2, 1, 1);
        addIndivField(grid, "stats.indiv.health", lblIndivHealth, 0, 2, 1);
        addIndivField(grid, "stats.indiv.energy", lblIndivEnergy, 2, 2, 1);
        addIndivField(grid, "stats.indiv.distance", lblIndivDistance, 0, 3, 1);
        addIndivField(grid, "stats.indiv.payload", lblIndivPayload, 2, 3, 1);
        addIndivField(grid, "stats.indiv.task", lblIndivTask, 0, 4, 3);

        box.getChildren().addAll(title, inputRow, new Separator(), grid);
        return box;
    }

    private void addIndivField(GridPane grid, String labelKey, Label valueLabel, int col, int row, int colSpan) {
        Label titleLbl = new Label();
        titleLbl.textProperty().bind(i18n.createStringBinding(labelKey));
        titleLbl.getStyleClass().add("field-label");
        grid.add(titleLbl, col, row);
        grid.add(valueLabel, col + 1, row, colSpan, 1);
    }

    public void triggerTrackAndTrace() {
        String val = txtAntSearchId.getText() != null ? txtAntSearchId.getText().trim() : "";
        if (!val.isEmpty()) {
            setTrackedIndividualId(val);
            if (onTrackAntListener != null) {
                onTrackAntListener.accept(val);
            }
        }
    }

    public void cycleAnt(int delta) {
        String current = (trackedAntId != null && !trackedAntId.isBlank()) ? trackedAntId.trim() : "ant_1";
        int num = 1;
        String prefix = "ant_";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("^(.*?)(\\d+)$").matcher(current);
        if (m.find()) {
            prefix = m.group(1);
            try {
                num = Integer.parseInt(m.group(2));
            } catch (Exception ignored) {
                // Keep default number on unparseable trailing digits
            }
        }
        num = Math.max(1, num + delta);
        String newId = prefix + num;
        setTrackedIndividualId(newId);
        if (onSelectAntListener != null) {
            onSelectAntListener.accept(newId);
        }
    }

    public void setTrackedIndividualId(String individualId) {
        if (individualId != null && !individualId.trim().isEmpty()) {
            this.trackedAntId = individualId.trim();
            txtAntSearchId.setText(this.trackedAntId);
            lblTrackedIdDisplay.setText(this.trackedAntId);
            if (onSeriesReset != null) {
                onSeriesReset.run();
            }
        }
    }

    public void setTrackedIndividual(Individual individual) {
        if (individual != null) {
            String idStr = individual.getFormattedId();
            setTrackedIndividualId(idStr);
            updateIndividualTelemetryFromEntity(individual);
        }
    }

    public void updateIndividualTelemetryFromEntity(Individual ant) {
        if (ant == null) return;
        Platform.runLater(() -> {
            String shortUuid = ant.getId() != null ? ant.getId().toString().substring(0, Math.min(8, ant.getId().toString().length())) : "N/A";
            lblTrackedIdDisplay.setText(String.format("%s (#%s)", ant.getFormattedId(), shortUuid));
            if (!ant.isAlive() || ant.getHealth() <= 0) {
                String cod = ant.getCauseOfDeath() != null ? " (" + ant.getCauseOfDeath() + ")" : "";
                lblIndivHealth.setText(i18n.get("stats.indiv.dead", cod));
            } else {
                lblIndivHealth.setText(String.format(Locale.US, "%.1f%%", ant.getHealth()));
            }
            lblIndivEnergy.setText(String.format(Locale.US, "%.1f%%", ant.getEnergy()));
            double distNest = Math.hypot(ant.getX() - ant.getHomeX(), ant.getY() - ant.getHomeY());
            lblIndivDistance.setText(i18n.get("stats.indiv.dist_fmt", distNest));
            String payload = (ant.getCarriedItem() != null && ant.getCarriedItem() != Individual.CarriedItem.NONE)
                    ? ant.getCarriedItem().name()
                    : "0.0 mg";
            lblIndivPayload.setText(payload);
            double ageDays = ant.getAge() / 86400.0;
            String casteName = ant.getCaste() != null ? ant.getCaste().name() : "Worker";
            lblIndivCasteAge.setText(i18n.get("stats.indiv.age_fmt", casteName, ageDays));
            String spName = ant.getSpecies() != null
                    ? (ant.getSpecies().getCommonName() != null ? ant.getSpecies().getCommonName() : ant.getSpecies().getScientificName())
                    : i18n.get("stats.indiv.species_fallback", "Formicidae");
            lblIndivSpecies.setText(spName != null ? spName : "Formicidae");
            String colStr = ant.getColonyId() != null ? ant.getColonyId().toString().substring(0, Math.min(8, ant.getColonyId().toString().length())) : "1";
            lblIndivColony.setText(i18n.get("stats.indiv.colony_fmt", colStr));

            String ethology;
            if (ant.getCaste() == Individual.Caste.QUEEN) {
                ethology = i18n.get("stats.ethology.queen", "👑 Soins Royaux / Ponte");
            } else if (ant.getState() == Individual.AiState.FORAGE || ant.getJob() == Individual.Job.FORAGER || ant.isCarryingFood()) {
                ethology = i18n.get("stats.ethology.forage", "🌾 Fourragement (Récolte / Transport)");
            } else if (ant.getState() == Individual.AiState.DIG || ant.getJob() == Individual.Job.BUILDER || ant.getCarriedItem() == Individual.CarriedItem.EARTH) {
                ethology = i18n.get("stats.ethology.dig", "⛏️ Excavation / Creusage");
            } else if (ant.getState() == Individual.AiState.TEND_BROOD || ant.getJob() == Individual.Job.NURSE || ant.getCarriedItem() == Individual.CarriedItem.BROOD) {
                ethology = i18n.get("stats.ethology.nurse", "🍼 Soins aux Larves (Nursing)");
            } else if (ant.getState() == Individual.AiState.ATTACKING || ant.getState() == Individual.AiState.PATROL || ant.getJob() == Individual.Job.GUARD || ant.getCaste() == Individual.Caste.SOLDIER) {
                ethology = i18n.get("stats.ethology.guard", "🛡️ Garde & Défense");
            } else {
                ethology = i18n.get("stats.ethology.rest", "💤 Repos / Inactivité");
            }

            String jobStr = ant.getJob() != null ? ant.getJob().toString() : "Forager";
            String stateStr = ant.getState() != null ? ant.getState().toString() : "ACTIVE";
            lblIndivTask.setText(String.format(Locale.US, "%s [%s / %s]", ethology, jobStr, stateStr));
        });
    }

    public void updateSyntheticTelemetry(double health, double energy, double distance, double payload) {
        lblIndivHealth.setText(String.format(Locale.US, "%.1f%%", health));
        lblIndivEnergy.setText(String.format(Locale.US, "%.1f%%", energy));
        lblIndivDistance.setText(String.format(Locale.US, "%.2f m", distance));
        lblIndivPayload.setText(String.format(Locale.US, "%.1f mg", payload));
    }

    public void resetTelemetryLabels() {
        lblIndivHealth.setText("100.0%");
        lblIndivEnergy.setText("100.0%");
        lblIndivDistance.setText("0.00 m");
        lblIndivPayload.setText("0.0 mg");
    }

    public VBox getCardPane() {
        return cardPane;
    }

    public String getTrackedAntId() {
        return trackedAntId;
    }

    public void setOnTrackAntListener(Consumer<String> onTrackAntListener) {
        this.onTrackAntListener = onTrackAntListener;
    }

    public void setOnSelectAntListener(Consumer<String> onSelectAntListener) {
        this.onSelectAntListener = onSelectAntListener;
    }
}
