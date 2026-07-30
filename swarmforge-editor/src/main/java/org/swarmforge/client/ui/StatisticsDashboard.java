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
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Real-Time Dynamic Colony & Eco-Engine Statistics Dashboard.
 * Formatted cleanly for both Dark and Light themes with dynamic metric selection.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class StatisticsDashboard extends VBox {

    private static final int MAX_DATA_POINTS = 600;

    public static class ColonyStats {
        public long simTicks;
        public int population;
        public int queens;
        public int workers;
        public int soldiers;
        public double food;
        public double water;
        public double tickRate;
        public int births;
        public int deaths;

        public ColonyStats() {
        }

        public ColonyStats(long simTicks, int population, int queens, int workers, int soldiers,
                           double food, double water, double tickRate, int births, int deaths) {
            this.simTicks = simTicks;
            this.population = population;
            this.queens = queens;
            this.workers = workers;
            this.soldiers = soldiers;
            this.food = food;
            this.water = water;
            this.tickRate = tickRate;
            this.births = births;
            this.deaths = deaths;
        }
    }

    private final XYChart.Series<Number, Number> totalPopSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> queensSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> workersSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> soldiersSeries = new XYChart.Series<>();

    private final XYChart.Series<Number, Number> foodSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> waterSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> birthsSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> deathsSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> tpsSeries = new XYChart.Series<>();

    private final CheckBox chkTotalPop = new CheckBox();
    private final CheckBox chkWorkers = new CheckBox();
    private final CheckBox chkSoldiers = new CheckBox();
    private final CheckBox chkQueens = new CheckBox();
    private final CheckBox chkFood = new CheckBox();
    private final CheckBox chkWater = new CheckBox();
    private final CheckBox chkBirths = new CheckBox();
    private final CheckBox chkDeaths = new CheckBox();
    private final CheckBox chkTps = new CheckBox();

    private final List<ColonyStats> historyList = new ArrayList<>();

    private final Label lblPopulation = new Label("0");
    private final Label lblQueens = new Label("0");
    private final Label lblWorkers = new Label("0");
    private final Label lblSoldiers = new Label("0");
    private final Label lblFood = new Label("0.0");
    private final Label lblWater = new Label("0.0");
    private final Label lblTickRate = new Label("0 tps");
    private final Label lblSimTime = new Label("0:00:00");

    private long dataPointIndex = 0;

    public StatisticsDashboard() {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        setSpacing(14);
        setPadding(new Insets(14));

        // === Header Controls ===
        HBox headerBar = new HBox(15);
        headerBar.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("📊 " + i18n.get("stats.dashboard_title", "Tableau de Bord Statistiques"));
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setStyle("-fx-font-size: 16px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnExport = new Button("📤 " + i18n.get("stats.export_btn", "Exporter Statistiques (CSV / Excel)"));
        btnExport.getStyleClass().add("btn-primary");
        btnExport.setTooltip(new Tooltip("Exporter l'historique complet des métriques enregistrées au format CSV."));
        btnExport.setOnAction(e -> exportToCSV());

        Button btnClear = new Button("🗑 " + i18n.get("log.btn.clear", "Réinitialiser"));
        btnClear.getStyleClass().add("btn-secondary");
        btnClear.setTooltip(new Tooltip("Effacer les données actuelles des graphiques et réinitialiser l'historique."));
        btnClear.setOnAction(e -> clear());

        headerBar.getChildren().addAll(titleLabel, spacer, btnClear, btnExport);

        // === Summary KPI Cards ===
        GridPane summaryGrid = createSummaryPanel();
        TitledPane summaryPane = new TitledPane();
        summaryPane.textProperty().bind(i18n.createStringBinding("stats.summary"));
        summaryPane.setContent(summaryGrid);
        summaryPane.setCollapsible(false);

        // === Interactive Metric Selection Panel ===
        VBox selectorBox = new VBox(8);
        selectorBox.getStyleClass().add("card-pane");

        Label lblSelect = new Label("🎯 " + i18n.get("stats.series_select", "Sélection des statistiques à afficher sur les graphiques :"));
        lblSelect.getStyleClass().add("card-title");

        chkTotalPop.textProperty().bind(i18n.createStringBinding("stats.population_total"));
        chkWorkers.textProperty().bind(i18n.createStringBinding("stats.workers"));
        chkSoldiers.textProperty().bind(i18n.createStringBinding("stats.soldiers"));
        chkQueens.textProperty().bind(i18n.createStringBinding("stats.queens"));
        chkFood.textProperty().bind(i18n.createStringBinding("stats.food"));
        chkWater.textProperty().bind(i18n.createStringBinding("stats.water"));
        chkBirths.textProperty().bind(i18n.createStringBinding("stats.births"));
        chkDeaths.textProperty().bind(i18n.createStringBinding("stats.deaths"));
        chkTps.textProperty().bind(i18n.createStringBinding("stats.tps"));

        chkTotalPop.setTooltip(new Tooltip("Afficher la courbe de la population totale de la colonie."));
        chkWorkers.setTooltip(new Tooltip("Afficher le nombre d'ouvrières en activité."));
        chkSoldiers.setTooltip(new Tooltip("Afficher le nombre de soldats défensifs."));
        chkQueens.setTooltip(new Tooltip("Afficher le nombre de reines reproductrices."));
        chkFood.setTooltip(new Tooltip("Afficher la réserve accumulée de nourriture."));
        chkWater.setTooltip(new Tooltip("Afficher le niveau de réserve d'eau."));
        chkBirths.setTooltip(new Tooltip("Afficher le cumul ou le taux de naissances."));
        chkDeaths.setTooltip(new Tooltip("Afficher le taux de mortalité."));
        chkTps.setTooltip(new Tooltip("Afficher la vitesse de calcul du moteur (TPS)."));

        FlowPane checkFlow = new FlowPane(14, 8);
        checkFlow.getChildren().addAll(chkTotalPop, chkWorkers, chkSoldiers, chkQueens, chkFood, chkWater, chkBirths, chkDeaths, chkTps);

        for (Node n : checkFlow.getChildren()) {
            if (n instanceof CheckBox cb) {
                cb.setSelected(true);
            }
        }

        selectorBox.getChildren().addAll(lblSelect, checkFlow);

        // Setup Chart Series Names
        totalPopSeries.setName(i18n.get("stats.population_total", "Population Totale"));
        queensSeries.setName(i18n.get("stats.queens", "Reines"));
        workersSeries.setName(i18n.get("stats.workers", "Ouvrières"));
        soldiersSeries.setName(i18n.get("stats.soldiers", "Soldats"));

        foodSeries.setName(i18n.get("stats.food", "Nourriture"));
        waterSeries.setName(i18n.get("stats.water", "Eau"));
        birthsSeries.setName(i18n.get("stats.births", "Naissances"));
        deathsSeries.setName(i18n.get("stats.deaths", "Décès"));
        tpsSeries.setName(i18n.get("stats.tps", "Vitesse (TPS)"));

        LineChart<Number, Number> popChart = createChart(i18n.get("stats.pop_chart", "Évolution des Populations"));
        popChart.getData().addAll(totalPopSeries, queensSeries, workersSeries, soldiersSeries);

        LineChart<Number, Number> resChart = createChart(i18n.get("stats.res_chart", "Ressources & Performances Système"));
        resChart.getData().addAll(foodSeries, waterSeries, birthsSeries, deathsSeries, tpsSeries);

        // Bind visibility
        chkTotalPop.selectedProperty().addListener((obs, oldV, newV) -> toggleSeries(popChart, totalPopSeries, newV));
        chkQueens.selectedProperty().addListener((obs, oldV, newV) -> toggleSeries(popChart, queensSeries, newV));
        chkWorkers.selectedProperty().addListener((obs, oldV, newV) -> toggleSeries(popChart, workersSeries, newV));
        chkSoldiers.selectedProperty().addListener((obs, oldV, newV) -> toggleSeries(popChart, soldiersSeries, newV));

        chkFood.selectedProperty().addListener((obs, oldV, newV) -> toggleSeries(resChart, foodSeries, newV));
        chkWater.selectedProperty().addListener((obs, oldV, newV) -> toggleSeries(resChart, waterSeries, newV));
        chkBirths.selectedProperty().addListener((obs, oldV, newV) -> toggleSeries(resChart, birthsSeries, newV));
        chkDeaths.selectedProperty().addListener((obs, oldV, newV) -> toggleSeries(resChart, deathsSeries, newV));
        chkTps.selectedProperty().addListener((obs, oldV, newV) -> toggleSeries(resChart, tpsSeries, newV));

        VBox chartsBox = new VBox(12, popChart, resChart);
        ScrollPane scrollCharts = new ScrollPane(chartsBox);
        scrollCharts.setFitToWidth(true);
        VBox.setVgrow(scrollCharts, Priority.ALWAYS);

        getChildren().addAll(headerBar, summaryPane, selectorBox, scrollCharts);
    }

    private void toggleSeries(LineChart<Number, Number> chart, XYChart.Series<Number, Number> series, boolean show) {
        if (show) {
            if (!chart.getData().contains(series)) {
                chart.getData().add(series);
            }
        } else {
            chart.getData().remove(series);
        }
    }

    private GridPane createSummaryPanel() {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        grid.add(createKpiCard(i18n.get("stats.population", "Population :"), lblPopulation), 0, 0);
        grid.add(createKpiCard(i18n.get("stats.queens", "Reines :"), lblQueens), 1, 0);
        grid.add(createKpiCard(i18n.get("stats.workers", "Ouvrières :"), lblWorkers), 2, 0);
        grid.add(createKpiCard(i18n.get("stats.soldiers", "Soldats :"), lblSoldiers), 3, 0);

        grid.add(createKpiCard(i18n.get("stats.food", "Nourriture :"), lblFood), 0, 1);
        grid.add(createKpiCard(i18n.get("stats.water", "Eau :"), lblWater), 1, 1);
        grid.add(createKpiCard(i18n.get("stats.tick_rate", "Vitesse :"), lblTickRate), 2, 1);
        grid.add(createKpiCard(i18n.get("stats.sim_time", "Temps Simulé :"), lblSimTime), 3, 1);

        return grid;
    }

    private HBox createKpiCard(String labelText, Label valueLabel) {
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getStyleClass().add("card-pane");
        box.setPadding(new Insets(6, 12, 6, 12));

        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-size: 12px;");

        valueLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        box.getChildren().addAll(lbl, valueLabel);
        Tooltip.install(box, new Tooltip(labelText + " Valeur en temps réel"));
        return box;
    }

    private LineChart<Number, Number> createChart(String title) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Samples (Ticks)");
        xAxis.setForceZeroInRange(false);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Valeur");

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setCreateSymbols(false);
        chart.setAnimated(false);
        chart.setLegendVisible(true);
        chart.setPrefHeight(240);
        chart.getStyleClass().add("chart-holder");

        return chart;
    }

    public void update(ColonyStats stats) {
        if (stats == null) return;
        historyList.add(stats);

        Platform.runLater(() -> {
            lblPopulation.setText(String.valueOf(stats.population));
            lblQueens.setText(String.valueOf(stats.queens));
            lblWorkers.setText(String.valueOf(stats.workers));
            lblSoldiers.setText(String.valueOf(stats.soldiers));
            lblFood.setText(String.format("%.1f", stats.food));
            lblWater.setText(String.format("%.1f", stats.water));
            lblTickRate.setText(String.format("%.1f tps", stats.tickRate));
            lblSimTime.setText(formatTime(stats.simTicks));

            dataPointIndex++;

            totalPopSeries.getData().add(new XYChart.Data<>(dataPointIndex, stats.population));
            queensSeries.getData().add(new XYChart.Data<>(dataPointIndex, stats.queens));
            workersSeries.getData().add(new XYChart.Data<>(dataPointIndex, stats.workers));
            soldiersSeries.getData().add(new XYChart.Data<>(dataPointIndex, stats.soldiers));

            foodSeries.getData().add(new XYChart.Data<>(dataPointIndex, stats.food));
            waterSeries.getData().add(new XYChart.Data<>(dataPointIndex, stats.water));
            birthsSeries.getData().add(new XYChart.Data<>(dataPointIndex, stats.births));
            deathsSeries.getData().add(new XYChart.Data<>(dataPointIndex, stats.deaths));
            tpsSeries.getData().add(new XYChart.Data<>(dataPointIndex, stats.tickRate));

            trimSeries(totalPopSeries);
            trimSeries(queensSeries);
            trimSeries(workersSeries);
            trimSeries(soldiersSeries);
            trimSeries(foodSeries);
            trimSeries(waterSeries);
            trimSeries(birthsSeries);
            trimSeries(deathsSeries);
            trimSeries(tpsSeries);
        });
    }

    private void trimSeries(XYChart.Series<Number, Number> series) {
        if (series.getData().size() > MAX_DATA_POINTS) {
            series.getData().remove(0);
        }
    }

    public void clear() {
        historyList.clear();
        dataPointIndex = 0;
        totalPopSeries.getData().clear();
        queensSeries.getData().clear();
        workersSeries.getData().clear();
        soldiersSeries.getData().clear();
        foodSeries.getData().clear();
        waterSeries.getData().clear();
        birthsSeries.getData().clear();
        deathsSeries.getData().clear();
        tpsSeries.getData().clear();

        lblPopulation.setText("0");
        lblQueens.setText("0");
        lblWorkers.setText("0");
        lblSoldiers.setText("0");
        lblFood.setText("0.0");
        lblWater.setText("0.0");
        lblTickRate.setText("0 tps");
        lblSimTime.setText("0:00:00");
    }

    private void exportToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les données statistiques (CSV)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV (*.csv)", "*.csv"));
        fileChooser.setInitialFileName("swarmforge_stats_" + System.currentTimeMillis() + ".csv");

        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
                writer.println("SimTicks;Population;Queens;Workers;Soldiers;Food;Water;TickRate;Births;Deaths");
                for (ColonyStats s : historyList) {
                    writer.printf("%d;%d;%d;%d;%d;%.2f;%.2f;%.2f;%d;%d%n",
                            s.simTicks, s.population, s.queens, s.workers, s.soldiers,
                            s.food, s.water, s.tickRate, s.births, s.deaths);
                }
                new Alert(Alert.AlertType.INFORMATION, "Exportation CSV réussie !\n" + file.getAbsolutePath()).show();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Erreur lors de l'exportation CSV : " + ex.getMessage()).show();
            }
        }
    }

    private String formatTime(long ticks) {
        long totalSeconds = ticks / 20;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
