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
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Real-time statistics dashboard for colony monitoring and data analysis.
 * Displays population graphs, resource levels, health indicators,
 * interactive metric toggles, and CSV/Excel data export capabilities.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class StatisticsDashboard extends VBox {

    private static final int MAX_DATA_POINTS = 600; // ~10 minutes of history

    // Data History for Export
    private final List<ColonyStats> historyList = new ArrayList<>();

    // Charts
    private final LineChart<Number, Number> populationChart;
    private final LineChart<Number, Number> resourceChart;

    // Population chart series
    private final XYChart.Series<Number, Number> totalPopSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> queensSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> workersSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> soldiersSeries = new XYChart.Series<>();

    // Resource & event chart series
    private final XYChart.Series<Number, Number> foodSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> waterSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> birthsSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> deathsSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> tpsSeries = new XYChart.Series<>();

    // Checkboxes for toggling metrics
    private final CheckBox chkTotalPop = new CheckBox("Population Totale");
    private final CheckBox chkQueens = new CheckBox("Reines");
    private final CheckBox chkWorkers = new CheckBox("Ouvrières");
    private final CheckBox chkSoldiers = new CheckBox("Soldats");
    private final CheckBox chkFood = new CheckBox("Nourriture");
    private final CheckBox chkWater = new CheckBox("Eau");
    private final CheckBox chkBirths = new CheckBox("Naissances");
    private final CheckBox chkDeaths = new CheckBox("Décès");
    private final CheckBox chkTps = new CheckBox("Taux Ticks (TPS)");

    // Stats summary labels
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
        setSpacing(12);
        setPadding(new Insets(14));
        setStyle("-fx-background-color: #181825;");

        // === Header Controls (Title & Export Button) ===
        HBox headerBar = new HBox(15);
        headerBar.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("📊 " + i18n.get("stats.dashboard_title", "Tableau de Bord Statistiques"));
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnExport = new Button("📤 " + i18n.get("stats.export_btn", "Exporter Statistiques (CSV / Excel)"));
        btnExport.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 14;");
        btnExport.setOnAction(e -> exportToCSV());

        Button btnClear = new Button("🗑 " + i18n.get("log.btn.clear", "Réinitialiser"));
        btnClear.setStyle("-fx-background-color: #334155; -fx-text-fill: #94a3b8; -fx-background-radius: 6; -fx-padding: 6 12;");
        btnClear.setOnAction(e -> clear());

        headerBar.getChildren().addAll(titleLabel, spacer, btnClear, btnExport);

        // === Summary KPI Cards ===
        GridPane summaryGrid = createSummaryPanel();
        TitledPane summaryPane = new TitledPane();
        summaryPane.textProperty().bind(i18n.createStringBinding("stats.summary"));
        summaryPane.setContent(summaryGrid);
        summaryPane.setCollapsible(false);

        // === Interactive Metric Selection Panel ("Séries à afficher") ===
        VBox selectorBox = new VBox(8);
        selectorBox.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-padding: 10; -fx-background-radius: 6; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 6;");
        Label lblSelect = new Label("🎯 " + i18n.get("stats.series_select", "Sélection des statistiques à afficher sur les graphiques :"));
        lblSelect.setStyle("-fx-text-fill: #a78bfa; -fx-font-weight: bold; -fx-font-size: 12px;");

        FlowPane checkFlow = new FlowPane(12, 8);
        checkFlow.getChildren().addAll(chkTotalPop, chkWorkers, chkSoldiers, chkQueens, chkFood, chkWater, chkBirths, chkDeaths, chkTps);

        // Styling checkboxes
        String checkStyle = "-fx-text-fill: #e2e8f0; -fx-font-size: 11px;";
        for (Node n : checkFlow.getChildren()) {
            if (n instanceof CheckBox cb) {
                cb.setStyle(checkStyle);
                cb.setSelected(true);
            }
        }

        selectorBox.getChildren().addAll(lblSelect, checkFlow);

        // === Setup Chart Series Names ===
        totalPopSeries.setName(i18n.get("stats.population_total", "Population Totale"));
        queensSeries.setName(i18n.get("stats.queens", "Reines"));
        workersSeries.setName(i18n.get("stats.workers", "Ouvrières"));
        soldiersSeries.setName(i18n.get("stats.soldiers", "Soldats"));

        foodSeries.setName(i18n.get("stats.food", "Nourriture"));
        waterSeries.setName(i18n.get("stats.water", "Eau"));
        birthsSeries.setName(i18n.get("stats.births", "Naissances"));
        deathsSeries.setName(i18n.get("stats.deaths", "Décès"));
        tpsSeries.setName(i18n.get("stats.tps", "TPS"));

        // === Create Charts ===
        populationChart = createChart(i18n.get("stats.pop_chart", "Évolution des Populations"), "Ticks", "Individus");
        resourceChart = createChart(i18n.get("stats.res_chart", "Ressources & Performance"), "Ticks", "Valeur / Quantité");

        // Attach toggle listeners to update visible series dynamically
        setupToggleListeners();

        VBox chartsBox = new VBox(12);
        chartsBox.getChildren().addAll(populationChart, resourceChart);
        VBox.setVgrow(populationChart, Priority.ALWAYS);
        VBox.setVgrow(resourceChart, Priority.ALWAYS);

        // Scrollable container if height is small
        ScrollPane scrollPane = new ScrollPane();
        VBox scrollContent = new VBox(12);
        scrollContent.setPadding(new Insets(4));
        scrollContent.getChildren().addAll(headerBar, summaryPane, selectorBox, chartsBox);
        scrollPane.setContent(scrollContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private void setupToggleListeners() {
        chkTotalPop.selectedProperty().addListener((obs, o, n) -> toggleSeries(populationChart, totalPopSeries, n));
        chkWorkers.selectedProperty().addListener((obs, o, n) -> toggleSeries(populationChart, workersSeries, n));
        chkSoldiers.selectedProperty().addListener((obs, o, n) -> toggleSeries(populationChart, soldiersSeries, n));
        chkQueens.selectedProperty().addListener((obs, o, n) -> toggleSeries(populationChart, queensSeries, n));

        chkFood.selectedProperty().addListener((obs, o, n) -> toggleSeries(resourceChart, foodSeries, n));
        chkWater.selectedProperty().addListener((obs, o, n) -> toggleSeries(resourceChart, waterSeries, n));
        chkBirths.selectedProperty().addListener((obs, o, n) -> toggleSeries(resourceChart, birthsSeries, n));
        chkDeaths.selectedProperty().addListener((obs, o, n) -> toggleSeries(resourceChart, deathsSeries, n));
        chkTps.selectedProperty().addListener((obs, o, n) -> toggleSeries(resourceChart, tpsSeries, n));

        // Initial setup
        toggleSeries(populationChart, totalPopSeries, chkTotalPop.isSelected());
        toggleSeries(populationChart, workersSeries, chkWorkers.isSelected());
        toggleSeries(populationChart, soldiersSeries, chkSoldiers.isSelected());
        toggleSeries(populationChart, queensSeries, chkQueens.isSelected());

        toggleSeries(resourceChart, foodSeries, chkFood.isSelected());
        toggleSeries(resourceChart, waterSeries, chkWater.isSelected());
        toggleSeries(resourceChart, birthsSeries, chkBirths.isSelected());
        toggleSeries(resourceChart, deathsSeries, chkDeaths.isSelected());
        toggleSeries(resourceChart, tpsSeries, chkTps.isSelected());
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
        grid.setPadding(new Insets(12));

        Label popLbl = new Label(i18n.get("stats.population"));
        Label tickRateLbl = new Label(i18n.get("stats.tick_rate"));
        Label queensLbl = new Label(i18n.get("stats.queens"));
        Label simTimeLbl = new Label(i18n.get("stats.sim_time"));
        Label workersLbl = new Label(i18n.get("stats.workers"));
        Label foodLbl = new Label(i18n.get("stats.food"));
        Label soldiersLbl = new Label(i18n.get("stats.soldiers"));
        Label waterLbl = new Label(i18n.get("stats.water"));

        grid.add(popLbl, 0, 0); grid.add(lblPopulation, 1, 0);
        grid.add(tickRateLbl, 2, 0); grid.add(lblTickRate, 3, 0);

        grid.add(queensLbl, 0, 1); grid.add(lblQueens, 1, 1);
        grid.add(simTimeLbl, 2, 1); grid.add(lblSimTime, 3, 1);

        grid.add(workersLbl, 0, 2); grid.add(lblWorkers, 1, 2);
        grid.add(foodLbl, 2, 2); grid.add(lblFood, 3, 2);

        grid.add(soldiersLbl, 0, 3); grid.add(lblSoldiers, 1, 3);
        grid.add(waterLbl, 2, 3); grid.add(lblWater, 3, 3);

        String labelStyle = "-fx-text-fill: #94a3b8; -fx-font-size: 12px;";
        popLbl.setStyle(labelStyle); tickRateLbl.setStyle(labelStyle);
        queensLbl.setStyle(labelStyle); simTimeLbl.setStyle(labelStyle);
        workersLbl.setStyle(labelStyle); foodLbl.setStyle(labelStyle);
        soldiersLbl.setStyle(labelStyle); waterLbl.setStyle(labelStyle);

        String valueStyle = "-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #38bdf8;";
        lblPopulation.setStyle(valueStyle);
        lblQueens.setStyle(valueStyle);
        lblWorkers.setStyle(valueStyle);
        lblSoldiers.setStyle(valueStyle);
        lblFood.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #4ade80;");
        lblWater.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #60a5fa;");
        lblTickRate.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #f59e0b;");
        lblSimTime.setStyle(valueStyle);

        return grid;
    }

    private LineChart<Number, Number> createChart(String title, String xLabel, String yLabel) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel(xLabel);
        xAxis.setAutoRanging(true);
        xAxis.setForceZeroInRange(false);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yLabel);
        yAxis.setAutoRanging(true);

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setCreateSymbols(false);
        chart.setAnimated(false);
        chart.setLegendVisible(true);
        chart.setPrefHeight(220);
        chart.setStyle("-fx-background-color: #1e1e2e; -fx-plot-background-color: #0f172a;");

        return chart;
    }

    /**
     * Update dashboard with new stats sample.
     */
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

            // Population chart data
            totalPopSeries.getData().add(new XYChart.Data<>(dataPointIndex, stats.population));
            queensSeries.getData().add(new XYChart.Data<>(dataPointIndex, stats.queens));
            workersSeries.getData().add(new XYChart.Data<>(dataPointIndex, stats.workers));
            soldiersSeries.getData().add(new XYChart.Data<>(dataPointIndex, stats.soldiers));

            // Resource & performance chart data
            foodSeries.getData().add(new XYChart.Data<>(dataPointIndex, stats.food));
            waterSeries.getData().add(new XYChart.Data<>(dataPointIndex, stats.water));
            birthsSeries.getData().add(new XYChart.Data<>(dataPointIndex, stats.births));
            deathsSeries.getData().add(new XYChart.Data<>(dataPointIndex, stats.deaths));
            tpsSeries.getData().add(new XYChart.Data<>(dataPointIndex, stats.tickRate));

            // Cap data window for chart performance
            if (totalPopSeries.getData().size() > MAX_DATA_POINTS) {
                totalPopSeries.getData().remove(0);
                queensSeries.getData().remove(0);
                workersSeries.getData().remove(0);
                soldiersSeries.getData().remove(0);
                foodSeries.getData().remove(0);
                waterSeries.getData().remove(0);
                birthsSeries.getData().remove(0);
                deathsSeries.getData().remove(0);
                tpsSeries.getData().remove(0);
            }
        });
    }

    /**
     * Export all recorded history to CSV format (Excel compatible).
     */
    public void exportToCSV() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exporter Statistiques de Simulation (CSV / Excel)");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV (*.csv)", "*.csv"));
        chooser.setInitialFileName("swarmforge_simulation_stats.csv");

        File file = chooser.showSaveDialog(getScene() != null ? getScene().getWindow() : null);
        if (file == null) return;

        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            // Write UTF-8 BOM so Excel opens it with French accents and UTF-8 encoding correctly
            pw.write('\uFEFF');

            // Header line (Excel uses semicolon or comma depending on locale, semicolon is safest in FR)
            pw.println("Tick;Temps_Sim;Population_Totale;Reines;Ouvrieres;Soldats;Nourriture;Eau;TPS;Naissances;Deces");

            for (ColonyStats s : historyList) {
                pw.printf("%d;%s;%d;%d;%d;%d;%.2f;%.2f;%.2f;%d;%d%n",
                        s.simTicks,
                        formatTime(s.simTicks),
                        s.population,
                        s.queens,
                        s.workers,
                        s.soldiers,
                        s.food,
                        s.water,
                        s.tickRate,
                        s.births,
                        s.deaths);
            }

            Alert okAlert = new Alert(Alert.AlertType.INFORMATION);
            okAlert.setTitle("Export Réussi");
            okAlert.setHeaderText("Statistiques Exportées");
            okAlert.setContentText("Les données de simulation (" + historyList.size() + " points) ont été sauvegardées dans :\n" + file.getAbsolutePath());
            okAlert.showAndWait();

        } catch (Exception ex) {
            Alert errAlert = new Alert(Alert.AlertType.ERROR);
            errAlert.setTitle("Erreur d'Exportation");
            errAlert.setHeaderText("Échec de l'exportation CSV");
            errAlert.setContentText("Impossible d'écrire le fichier : " + ex.getMessage());
            errAlert.showAndWait();
        }
    }

    private String formatTime(long ticks) {
        long seconds = ticks / 60;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        return String.format("%d:%02d:%02d", hours, minutes % 60, seconds % 60);
    }

    public void clear() {
        historyList.clear();
        Platform.runLater(() -> {
            totalPopSeries.getData().clear();
            queensSeries.getData().clear();
            workersSeries.getData().clear();
            soldiersSeries.getData().clear();
            foodSeries.getData().clear();
            waterSeries.getData().clear();
            birthsSeries.getData().clear();
            deathsSeries.getData().clear();
            tpsSeries.getData().clear();
            dataPointIndex = 0;
            lblPopulation.setText("0");
            lblQueens.setText("0");
            lblWorkers.setText("0");
            lblSoldiers.setText("0");
            lblFood.setText("0.0");
            lblWater.setText("0.0");
            lblTickRate.setText("0 tps");
            lblSimTime.setText("0:00:00");
        });
    }

    public static class ColonyStats {
        public int population;
        public int queens;
        public int workers;
        public int soldiers;
        public int nurses;
        public float food;
        public float water;
        public float tickRate;
        public long simTicks;
        public int births;
        public int deaths;

        public ColonyStats() {}

        public ColonyStats(int population, int queens, int workers, int soldiers,
                           float food, float water, float tickRate, long simTicks) {
            this.population = population;
            this.queens = queens;
            this.workers = workers;
            this.soldiers = soldiers;
            this.food = food;
            this.water = water;
            this.tickRate = tickRate;
            this.simTicks = simTicks;
        }
    }
}

