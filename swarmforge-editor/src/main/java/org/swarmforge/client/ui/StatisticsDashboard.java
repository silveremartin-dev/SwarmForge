/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import org.swarmforge.client.util.I18nManager;

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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Versatile Real-Time Dynamic Colony & Eco-Engine Statistics Dashboard.
 * Displays statistics in real-time temporal units with time-window controls (3 min default),
 * species & colony separation, caste breakdowns, bio-resource graphs, weather/climate metrics,
 * TPS performance tracking, and CSV export capabilities.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class StatisticsDashboard extends VBox {

    private static final int MAX_DATA_POINTS = 3600; // Store up to 1 hour of data at 1 Hz

    public static class ColonyStats extends org.swarmforge.client.ui.ColonyStats {
        public ColonyStats() {
            super();
        }

        public ColonyStats(long simTicks, int population, int queens, int workers, int soldiers,
                           double food, double water, double tickRate, int births, int deaths) {
            super(simTicks, population, queens, workers, soldiers, food, water, tickRate, births, deaths);
        }
    }

    private final Map<String, XYChart.Series<Number, Number>> colonySeriesMap = new LinkedHashMap<>();
    private final XYChart.Series<Number, Number> totalPopSeries = new XYChart.Series<>(), queensSeries = new XYChart.Series<>(), workersSeries = new XYChart.Series<>(), soldiersSeries = new XYChart.Series<>(), malesSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> foodSeries = new XYChart.Series<>(), waterSeries = new XYChart.Series<>(), proteinSeries = new XYChart.Series<>(), birthsSeries = new XYChart.Series<>(), deathsSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> tempSeries = new XYChart.Series<>(), rainSeries = new XYChart.Series<>(), pheroSeries = new XYChart.Series<>(), tpsSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> behaviorForagingSeries = new XYChart.Series<>(), behaviorDiggingSeries = new XYChart.Series<>(), behaviorNursingSeries = new XYChart.Series<>(), behaviorGuardingSeries = new XYChart.Series<>(), behaviorRoyalCareSeries = new XYChart.Series<>(), behaviorRestingSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> antHealthSeries = new XYChart.Series<>(), antEnergySeries = new XYChart.Series<>(), antDistanceSeries = new XYChart.Series<>();

    private final LineChart<Number, Number> chartMultiColony, chartCastes, chartResources, chartWeather, chartBehaviors, chartPerformance, chartIndividualAnt;
    private String scenarioName = "swarmforge";
    private final StatisticsTrackedAntCard trackedAntCard = new StatisticsTrackedAntCard(() -> {
        antHealthSeries.getData().clear(); antEnergySeries.getData().clear(); antDistanceSeries.getData().clear();
    });

    public void setOnTrackIndividual(java.util.function.Consumer<String> listener) { trackedAntCard.setOnTrackAntListener(listener); }
    public void setOnSelectIndividual(java.util.function.Consumer<String> listener) { trackedAntCard.setOnSelectAntListener(listener); }
    public String getScenarioName() { return scenarioName; }
    public void setScenarioName(String scenarioName) { if (scenarioName != null && !scenarioName.isBlank()) this.scenarioName = scenarioName; }
    private final I18nManager i18n = I18nManager.getInstance();
    public void setTrackedIndividualId(String individualId) { trackedAntCard.setTrackedIndividualId(individualId); }
    public void setTrackedIndividual(org.swarmforge.core.domain.Individual individual) { trackedAntCard.setTrackedIndividual(individual); }
    public void updateIndividualTelemetryFromEntity(org.swarmforge.core.domain.Individual ant) { trackedAntCard.updateIndividualTelemetryFromEntity(ant); }
    private final VBox chartsContainer = new VBox(14);
    private final ComboBox<String> comboGraphView = new ComboBox<>(), comboTimeWindow = new ComboBox<>();
    private final CheckBox chkTotalPop = new CheckBox(), chkWorkers = new CheckBox(), chkSoldiers = new CheckBox(), chkQueens = new CheckBox(), chkMales = new CheckBox();
    private final CheckBox chkFood = new CheckBox(), chkWater = new CheckBox(), chkProtein = new CheckBox(), chkBirths = new CheckBox(), chkDeaths = new CheckBox();
    private final CheckBox chkTemp = new CheckBox(), chkRain = new CheckBox(), chkPhero = new CheckBox(), chkTps = new CheckBox();
    private final CheckBox chkForaging = new CheckBox(), chkDigging = new CheckBox(), chkNursing = new CheckBox(), chkGuarding = new CheckBox(), chkRoyalCare = new CheckBox(), chkResting = new CheckBox();
    private final CheckBox chkAntHealth = new CheckBox(), chkAntEnergy = new CheckBox(), chkAntDistance = new CheckBox();
    private final List<ColonyStats> historyList = new ArrayList<>();
    private final Label lblPopulation = new Label("0"), lblColoniesCount = new Label("0"), lblQueens = new Label("0"), lblWorkers = new Label("0"), lblSoldiers = new Label("0");
    private final Label lblFood = new Label("0.0"), lblWater = new Label("0.0"), lblTickRate = new Label("0 tps"), lblSimTime = new Label("00:00:00 (0.0 s)");

    private long lastGraphUpdateMillis = 0;
    private double currentSelectedWindowSec = 180.0; // 3 minutes default

    public StatisticsDashboard() {
        setSpacing(14);
        setPadding(new Insets(14));

        // === Header Toolbar ===
        HBox headerBar = new HBox(10);
        headerBar.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label();
        titleLabel.textProperty().bind(i18n.createStringBinding("stats.dashboard_title"));
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0284c7;");
        titleLabel.tooltipProperty().bind(i18n.createTooltipBinding("stats.dashboard_title.tt"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // View Mode Selector
        Label lblViewMode = new Label();
        lblViewMode.textProperty().bind(i18n.createStringBinding("stats.graph_view.label"));
        lblViewMode.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-font-weight: bold;");
        populateGraphViewItems();
        comboGraphView.tooltipProperty().bind(i18n.createTooltipBinding("stats.graph_view.tt"));
        comboGraphView.setOnAction(e -> updateVisibleCharts());

        // Zoom Controls
        Button btnZoomIn = new Button("🔍+");
        btnZoomIn.setStyle("-fx-background-color: #334155; -fx-text-fill: #38bdf8; -fx-font-weight: bold;");
        btnZoomIn.tooltipProperty().bind(i18n.createTooltipBinding("stats.zoom.in.tt"));
        btnZoomIn.setOnAction(e -> {
            if (currentSelectedWindowSec <= 0) {
                currentSelectedWindowSec = 300.0;
            } else {
                currentSelectedWindowSec = Math.max(15.0, currentSelectedWindowSec / 1.5);
            }
            if (!historyList.isEmpty()) {
                updateChartXAxes(historyList.get(historyList.size() - 1).getSimTimeSeconds());
            }
        });

        Button btnZoomOut = new Button("🔍-");
        btnZoomOut.setStyle("-fx-background-color: #334155; -fx-text-fill: #38bdf8; -fx-font-weight: bold;");
        btnZoomOut.tooltipProperty().bind(i18n.createTooltipBinding("stats.zoom.out.tt"));
        btnZoomOut.setOnAction(e -> {
            if (currentSelectedWindowSec > 0) {
                currentSelectedWindowSec = currentSelectedWindowSec * 1.5;
                if (currentSelectedWindowSec > 3600.0) {
                    currentSelectedWindowSec = -1.0; // All history
                }
            }
            if (!historyList.isEmpty()) {
                updateChartXAxes(historyList.get(historyList.size() - 1).getSimTimeSeconds());
            }
        });

        Button btnZoomReset = new Button("🔍 100%");
        btnZoomReset.setStyle("-fx-background-color: #334155; -fx-text-fill: #94a3b8;");
        btnZoomReset.tooltipProperty().bind(i18n.createTooltipBinding("stats.zoom.reset.tt"));
        btnZoomReset.setOnAction(e -> {
            currentSelectedWindowSec = 180.0;
            comboTimeWindow.getSelectionModel().select(1);
            if (!historyList.isEmpty()) {
                updateChartXAxes(historyList.get(historyList.size() - 1).getSimTimeSeconds());
            }
        });

        // Time Window Selector (3 Minutes default as requested)
        Label lblTimeWindow = new Label();
        lblTimeWindow.textProperty().bind(i18n.createStringBinding("stats.tick_rate"));
        lblTimeWindow.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-font-weight: bold;");
        populateTimeWindowItems();
        comboTimeWindow.getSelectionModel().select(1); // 3 Minutes default
        comboTimeWindow.tooltipProperty().bind(i18n.createTooltipBinding("stats.time_window.tt"));
        comboTimeWindow.setOnAction(e -> {
            int idx = comboTimeWindow.getSelectionModel().getSelectedIndex();
            switch (idx) {
                case 0 -> currentSelectedWindowSec = 60.0;
                case 1 -> currentSelectedWindowSec = 180.0;
                case 2 -> currentSelectedWindowSec = 300.0;
                case 3 -> currentSelectedWindowSec = 600.0;
                case 4 -> currentSelectedWindowSec = -1.0; // All history
            }
            if (!historyList.isEmpty()) {
                updateChartXAxes(historyList.get(historyList.size() - 1).getSimTimeSeconds());
            }
        });

        Button btnExport = new Button();
        btnExport.textProperty().bind(i18n.createStringBinding("stats.export_btn"));
        btnExport.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.UPLOAD));
        btnExport.getStyleClass().add("btn-primary");
        btnExport.tooltipProperty().bind(i18n.createTooltipBinding("stats.export_btn.tt"));
        btnExport.setOnAction(e -> exportToCSV());

        Button btnClear = new Button();
        btnClear.textProperty().bind(i18n.createStringBinding("log.btn.clear"));
        btnClear.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.TRASH_2));
        btnClear.getStyleClass().add("btn-secondary");
        btnClear.tooltipProperty().bind(i18n.createTooltipBinding("stats.clear_btn.tt"));
        btnClear.setOnAction(e -> clear());

        headerBar.getChildren().addAll(titleLabel, spacer, lblViewMode, comboGraphView, btnZoomIn, btnZoomOut, btnZoomReset, lblTimeWindow, comboTimeWindow, btnClear, btnExport);

        // === Summary KPI Cards ===
        GridPane summaryGrid = createSummaryPanel();
        TitledPane summaryPane = new TitledPane();
        summaryPane.textProperty().bind(i18n.createStringBinding("stats.summary"));
        summaryPane.setContent(summaryGrid);
        summaryPane.setCollapsible(false);
        summaryPane.tooltipProperty().bind(i18n.createTooltipBinding("stats.summary.tt"));

        // === Metric Checkboxes Bar ===
        VBox selectorBox = new VBox(6);
        selectorBox.getStyleClass().add("card-pane");

        Label lblSelect = new Label();
        lblSelect.textProperty().bind(i18n.createStringBinding("stats.series_select"));
        lblSelect.getStyleClass().add("card-title");
        lblSelect.tooltipProperty().bind(i18n.createTooltipBinding("stats.series_select.tt"));

        chartMultiColony = createChart("stats.chart.multi_colony.title", "stats.chart.multi_colony.y");
        chartCastes = createChart("stats.chart.castes.title", "stats.chart.castes.y");
        chartBehaviors = createChart("stats.chart.behaviors.title", "stats.chart.behaviors.y");
        chartResources = createChart("stats.chart.resources.title", "stats.chart.resources.y");
        chartWeather = createChart("stats.chart.weather.title", "stats.chart.weather.y");
        chartPerformance = createChart("stats.chart.performance.title", "stats.chart.performance.y");
        chartIndividualAnt = createChart("stats.chart.individual.title", "stats.chart.individual.y");

        bindSeriesCheckbox(chkTotalPop, "stats.population_total", "stats.chk.total_pop.tt", chartCastes, totalPopSeries);
        bindSeriesCheckbox(chkWorkers, "stats.workers", "stats.chk.workers.tt", chartCastes, workersSeries);
        bindSeriesCheckbox(chkSoldiers, "stats.soldiers", "stats.chk.soldiers.tt", chartCastes, soldiersSeries);
        bindSeriesCheckbox(chkQueens, "stats.queens", "stats.chk.queens.tt", chartCastes, queensSeries);
        bindSeriesCheckbox(chkMales, "stats.chk.males", null, chartCastes, malesSeries);

        bindSeriesCheckbox(chkForaging, "stats.behavior.foraging", null, chartBehaviors, behaviorForagingSeries);
        bindSeriesCheckbox(chkDigging, "stats.behavior.digging", null, chartBehaviors, behaviorDiggingSeries);
        bindSeriesCheckbox(chkNursing, "stats.behavior.nursing", null, chartBehaviors, behaviorNursingSeries);
        bindSeriesCheckbox(chkGuarding, "stats.behavior.guarding", null, chartBehaviors, behaviorGuardingSeries);
        bindSeriesCheckbox(chkRoyalCare, "stats.behavior.royal_care", null, chartBehaviors, behaviorRoyalCareSeries);
        bindSeriesCheckbox(chkResting, "stats.behavior.resting", null, chartBehaviors, behaviorRestingSeries);

        bindSeriesCheckbox(chkFood, "stats.food", "stats.chk.food.tt", chartResources, foodSeries);
        bindSeriesCheckbox(chkWater, "stats.water", "stats.chk.water.tt", chartResources, waterSeries);
        bindSeriesCheckbox(chkProtein, "stats.chk.protein", null, chartResources, proteinSeries);
        bindSeriesCheckbox(chkBirths, "stats.births", "stats.chk.births.tt", chartResources, birthsSeries);
        bindSeriesCheckbox(chkDeaths, "stats.deaths", "stats.chk.deaths.tt", chartResources, deathsSeries);
        bindSeriesCheckbox(chkTemp, "stats.chk.temp", null, chartWeather, tempSeries);
        bindSeriesCheckbox(chkRain, "stats.chk.rain", null, chartWeather, rainSeries);
        bindSeriesCheckbox(chkPhero, "stats.chk.phero", null, chartWeather, pheroSeries);
        bindSeriesCheckbox(chkTps, "stats.tps", "stats.chk.tps.tt", chartPerformance, tpsSeries);
        bindSeriesCheckbox(chkAntHealth, "stats.chk.ant_health", null, chartIndividualAnt, antHealthSeries);
        bindSeriesCheckbox(chkAntEnergy, "stats.chk.ant_energy", null, chartIndividualAnt, antEnergySeries);
        bindSeriesCheckbox(chkAntDistance, "stats.chk.ant_distance", null, chartIndividualAnt, antDistanceSeries);

        FlowPane checkFlow = new FlowPane(12, 6,
                chkTotalPop, chkWorkers, chkSoldiers, chkQueens, chkMales,
                chkForaging, chkDigging, chkNursing, chkGuarding, chkRoyalCare, chkResting,
                chkFood, chkWater, chkProtein, chkBirths, chkDeaths,
                chkTemp, chkRain, chkPhero, chkTps,
                chkAntHealth, chkAntEnergy, chkAntDistance
        );
        selectorBox.getChildren().addAll(lblSelect, checkFlow);

        updateSeriesNames();
        i18n.localeProperty().addListener((obs, oldLoc, newLoc) -> {
            populateGraphViewItems();
            populateTimeWindowItems();
            updateSeriesNames();
        });

        chartCastes.getData().addAll(totalPopSeries, queensSeries, workersSeries, soldiersSeries, malesSeries);
        chartBehaviors.getData().addAll(behaviorForagingSeries, behaviorDiggingSeries, behaviorNursingSeries, behaviorGuardingSeries, behaviorRoyalCareSeries, behaviorRestingSeries);
        chartResources.getData().addAll(foodSeries, waterSeries, proteinSeries, birthsSeries, deathsSeries);
        chartWeather.getData().addAll(tempSeries, rainSeries, pheroSeries);
        chartPerformance.getData().addAll(tpsSeries);
        chartIndividualAnt.getData().addAll(antHealthSeries, antEnergySeries, antDistanceSeries);

        updateVisibleCharts();

        ScrollPane scrollCharts = new ScrollPane(chartsContainer);
        scrollCharts.setFitToWidth(true);
        VBox.setVgrow(scrollCharts, Priority.ALWAYS);

        getChildren().addAll(headerBar, summaryPane, selectorBox, scrollCharts);
    }

    private void bindSeriesCheckbox(CheckBox chk, String labelKey, String ttKey, LineChart<Number, Number> chart, XYChart.Series<Number, Number> series) {
        chk.textProperty().bind(i18n.createStringBinding(labelKey));
        if (ttKey != null) chk.tooltipProperty().bind(i18n.createTooltipBinding(ttKey));
        chk.setSelected(true);
        chk.selectedProperty().addListener((obs, oldV, newV) -> toggleSeries(chart, series, newV));
    }

    private void populateGraphViewItems() {
        int selected = comboGraphView.getSelectionModel().getSelectedIndex();
        comboGraphView.getItems().setAll(
                i18n.get("stats.view.all", "All Charts (Global View)"),
                i18n.get("stats.view.demographics", "Multi-Colony Demographics & Species"),
                i18n.get("stats.view.castes", "Caste Distribution (Queens, Workers, Soldiers)"),
                i18n.get("stats.view.behaviors", "Ethological Behaviors Distribution"),
                i18n.get("stats.view.resources", "Resources & Biomass (Food, Water)"),
                i18n.get("stats.view.ecosystem", "Ecosystem & Climate (Temp, Rain)"),
                i18n.get("stats.view.tps", "Engine Performance (TPS)"),
                i18n.get("stats.view.telemetry", "Individual Telemetry (Specific Ant)")
        );
        if (selected >= 0 && selected < comboGraphView.getItems().size()) {
            comboGraphView.getSelectionModel().select(selected);
        } else {
            comboGraphView.getSelectionModel().selectFirst();
        }
    }

    private void populateTimeWindowItems() {
        int selected = comboTimeWindow.getSelectionModel().getSelectedIndex();
        comboTimeWindow.getItems().setAll(
                i18n.get("stats.window.1m"),
                i18n.get("stats.window.3m"),
                i18n.get("stats.window.5m"),
                i18n.get("stats.window.10m"),
                i18n.get("stats.window.all")
        );
        if (selected >= 0 && selected < comboTimeWindow.getItems().size()) {
            comboTimeWindow.getSelectionModel().select(selected);
        } else {
            comboTimeWindow.getSelectionModel().select(1);
        }
    }

    private void updateSeriesNames() {
        totalPopSeries.setName(i18n.get("stats.series.total_pop", "Global Total Population"));
        queensSeries.setName(i18n.get("stats.queens", "Queens"));
        workersSeries.setName(i18n.get("stats.workers", "Workers"));
        soldiersSeries.setName(i18n.get("stats.soldiers", "Soldiers"));
        malesSeries.setName(i18n.get("stats.series.males", "Males"));

        behaviorForagingSeries.setName(i18n.get("stats.behavior.series.foraging", "🌾 Foraging (Harvest)"));
        behaviorDiggingSeries.setName(i18n.get("stats.behavior.series.digging", "⛏️ Excavation (Galleries)"));
        behaviorNursingSeries.setName(i18n.get("stats.behavior.series.nursing", "🍼 Brood Care (Larvae)"));
        behaviorGuardingSeries.setName(i18n.get("stats.behavior.series.guarding", "🛡️ Guard & Defense"));
        behaviorRoyalCareSeries.setName(i18n.get("stats.behavior.series.royal_care", "👑 Royal Care"));
        behaviorRestingSeries.setName(i18n.get("stats.behavior.series.resting", "💤 Resting / Inactivity"));

        foodSeries.setName(i18n.get("stats.food", "Stored Food"));
        waterSeries.setName(i18n.get("stats.water", "Water / Humidity"));
        proteinSeries.setName(i18n.get("stats.series.protein", "Proteins"));
        birthsSeries.setName(i18n.get("stats.births", "Cumulative Births"));
        deathsSeries.setName(i18n.get("stats.deaths", "Cumulative Deaths"));

        tempSeries.setName(i18n.get("stats.series.temp", "Air Temp (°C)"));
        rainSeries.setName(i18n.get("stats.series.rain", "Rainfall (mm/h)"));
        pheroSeries.setName(i18n.get("stats.series.phero", "Pheromone Intensity"));

        tpsSeries.setName(i18n.get("stats.tps", "Engine Speed (TPS)"));

        antHealthSeries.setName(i18n.get("stats.series.ant_health", "Individual Health (%)"));
        antEnergySeries.setName(i18n.get("stats.series.ant_energy", "Energy / Lipids (%)"));
        antDistanceSeries.setName(i18n.get("stats.series.ant_distance", "Traveled Distance (m)"));
    }

    private void updateVisibleCharts() {
        chartsContainer.getChildren().clear();
        int selected = comboGraphView.getSelectionModel().getSelectedIndex();
        switch (selected) {
            case 0 -> chartsContainer.getChildren().addAll(chartMultiColony, chartCastes, chartBehaviors, chartResources, chartWeather, chartPerformance, trackedAntCard.getCardPane(), chartIndividualAnt);
            case 1 -> chartsContainer.getChildren().add(chartMultiColony);
            case 2 -> chartsContainer.getChildren().add(chartCastes);
            case 3 -> chartsContainer.getChildren().add(chartBehaviors);
            case 4 -> chartsContainer.getChildren().add(chartResources);
            case 5 -> chartsContainer.getChildren().add(chartWeather);
            case 6 -> chartsContainer.getChildren().add(chartPerformance);
            case 7 -> chartsContainer.getChildren().addAll(trackedAntCard.getCardPane(), chartIndividualAnt);
        }
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
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        grid.add(createKpiCard("stats.population", lblPopulation, "Total living individuals in the simulation"), 0, 0);
        grid.add(createKpiCard("stats.active_colonies", lblColoniesCount, "Number of distinct colonies & species residing in the terrarium"), 1, 0);
        grid.add(createKpiCard("stats.queens", lblQueens, "Genetically reproductive queens"), 2, 0);
        grid.add(createKpiCard("stats.workers", lblWorkers, "Workers for foraging and brood care"), 3, 0);

        grid.add(createKpiCard("stats.soldiers", lblSoldiers, "Soldiers armed with defensive mandibles"), 0, 1);
        grid.add(createKpiCard("stats.food", lblFood, "Food reserves accumulated in nests"), 1, 1);
        grid.add(createKpiCard("stats.water", lblWater, "Water reserves and nest hygrometry level"), 2, 1);
        grid.add(createKpiCard("stats.tick_rate", lblTickRate, "Engine calculation speed in Ticks Per Second"), 3, 1);

        grid.add(createKpiCard("stats.sim_time", lblSimTime, "Elapsed simulation time and converted theoretical date"), 0, 2, 4, 1);

        return grid;
    }

    private HBox createKpiCard(String labelKey, Label valueLabel, String tooltipDesc) {
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getStyleClass().add("card-pane");
        box.setPadding(new Insets(6, 12, 6, 12));

        Label lbl = new Label();
        lbl.textProperty().bind(i18n.createStringBinding(labelKey));
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

        valueLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #38bdf8;");

        box.getChildren().addAll(lbl, valueLabel);
        Tooltip.install(box, new Tooltip(tooltipDesc));
        return box;
    }

    private LineChart<Number, Number> createChart(String titleKey, String yLabelKey) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.labelProperty().bind(i18n.createStringBinding("stats.chart.x_axis"));
        xAxis.setForceZeroInRange(false);

        NumberAxis yAxis = new NumberAxis();
        yAxis.labelProperty().bind(i18n.createStringBinding(yLabelKey));

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.titleProperty().bind(i18n.createStringBinding(titleKey));
        chart.setCreateSymbols(false);
        chart.setAnimated(false);
        chart.setLegendVisible(true);
        chart.setPrefHeight(230);
        chart.getStyleClass().add("chart-holder");

        return chart;
    }

    public void update(ColonyStats stats) {
        if (stats == null) return;
        historyList.add(stats);
        if (historyList.size() > MAX_DATA_POINTS) {
            historyList.remove(0);
        }

        double timeSeconds = stats.getSimTimeSeconds();
        long now = System.currentTimeMillis();

        // Rate limit graph updates to ~2 Hz (every 500ms) to ensure smooth curve progression without frantic leftward shifting
        boolean updateGraphsNow = (now - lastGraphUpdateMillis >= 500);
        if (updateGraphsNow) {
            lastGraphUpdateMillis = now;
        }

        Platform.runLater(() -> {
            // Update KPI Cards continuously
            lblPopulation.setText(String.valueOf(stats.population));
            int colCount = stats.colonyCasteCounts.isEmpty() ? (stats.population > 0 ? 1 : 0) : stats.colonyCasteCounts.size();
            lblColoniesCount.setText(String.valueOf(colCount));
            lblQueens.setText(String.valueOf(stats.queens));
            lblWorkers.setText(String.valueOf(stats.workers));
            lblSoldiers.setText(String.valueOf(stats.soldiers));
            lblFood.setText(String.format("%.1f", stats.food));
            lblWater.setText(String.format("%.1f", stats.water));
            lblTickRate.setText(String.format(java.util.Locale.US, "%.1f / %.0f TPS", stats.tickRate, stats.targetTickRate > 0 ? stats.targetTickRate : 60.0));
            lblSimTime.setText(StatisticsCsvExporter.formatTime(stats.simTicks, stats.stepTimeSeconds));

            if (updateGraphsNow) {
                // 1. Multi-Colony & Species Chart Update
                if (stats.colonyCasteCounts != null && !stats.colonyCasteCounts.isEmpty()) {
                    for (Map.Entry<String, Map<String, Integer>> entry : stats.colonyCasteCounts.entrySet()) {
                        String colName = entry.getKey();
                        Map<String, Integer> casteMap = entry.getValue();
                        int colTotal = casteMap.getOrDefault("Total", casteMap.values().stream().mapToInt(Integer::intValue).sum());

                        XYChart.Series<Number, Number> series = colonySeriesMap.computeIfAbsent(colName, k -> {
                            XYChart.Series<Number, Number> s = new XYChart.Series<>();
                            s.setName(k);
                            chartMultiColony.getData().add(s);
                            return s;
                        });

                        series.getData().add(new XYChart.Data<>(timeSeconds, colTotal));
                        trimSeries(series);
                    }
                } else if (stats.population > 0) {
                    // Default fallback colony series
                    XYChart.Series<Number, Number> series = colonySeriesMap.computeIfAbsent("Colonie Primaire (Lasius niger)", k -> {
                        XYChart.Series<Number, Number> s = new XYChart.Series<>();
                        s.setName(k);
                        chartMultiColony.getData().add(s);
                        return s;
                    });
                    series.getData().add(new XYChart.Data<>(timeSeconds, stats.population));
                    trimSeries(series);
                }

                // 2. Castes Breakdown Chart
                totalPopSeries.getData().add(new XYChart.Data<>(timeSeconds, stats.population));
                queensSeries.getData().add(new XYChart.Data<>(timeSeconds, stats.queens));
                workersSeries.getData().add(new XYChart.Data<>(timeSeconds, stats.workers));
                soldiersSeries.getData().add(new XYChart.Data<>(timeSeconds, stats.soldiers));
                malesSeries.getData().add(new XYChart.Data<>(timeSeconds, stats.males));

                // 3. Ethological Behaviors Chart
                behaviorForagingSeries.getData().add(new XYChart.Data<>(timeSeconds, stats.stateForaging));
                behaviorDiggingSeries.getData().add(new XYChart.Data<>(timeSeconds, stats.stateDigging));
                behaviorNursingSeries.getData().add(new XYChart.Data<>(timeSeconds, stats.stateNursing));
                behaviorGuardingSeries.getData().add(new XYChart.Data<>(timeSeconds, stats.stateGuarding));
                behaviorRoyalCareSeries.getData().add(new XYChart.Data<>(timeSeconds, stats.stateRoyalCare));
                behaviorRestingSeries.getData().add(new XYChart.Data<>(timeSeconds, stats.stateResting));

                // 4. Resources Chart
                foodSeries.getData().add(new XYChart.Data<>(timeSeconds, stats.food));
                waterSeries.getData().add(new XYChart.Data<>(timeSeconds, stats.water));
                proteinSeries.getData().add(new XYChart.Data<>(timeSeconds, stats.protein));
                birthsSeries.getData().add(new XYChart.Data<>(timeSeconds, stats.births));
                deathsSeries.getData().add(new XYChart.Data<>(timeSeconds, stats.deaths));

                // 5. Weather Chart
                tempSeries.getData().add(new XYChart.Data<>(timeSeconds, stats.temperature));
                rainSeries.getData().add(new XYChart.Data<>(timeSeconds, stats.rainfall));
                pheroSeries.getData().add(new XYChart.Data<>(timeSeconds, stats.pheromones));

                // 6. Performance Chart
                tpsSeries.getData().add(new XYChart.Data<>(timeSeconds, stats.tickRate));

                // 7. Individual Ant Telemetry Chart & KPI Update
                double simulatedHealth = Math.max(50.0, 100.0 - (stats.simTicks % 100) * 0.1);
                double simulatedEnergy = Math.max(20.0, 95.0 - (stats.simTicks % 150) * 0.2);
                double simulatedDistance = (stats.simTicks * 0.08);
                double simulatedPayload = (stats.simTicks % 20 > 10 ? 3.5 : 0.0);

                trackedAntCard.updateSyntheticTelemetry(simulatedHealth, simulatedEnergy, simulatedDistance, simulatedPayload);

                antHealthSeries.getData().add(new XYChart.Data<>(timeSeconds, simulatedHealth));
                antEnergySeries.getData().add(new XYChart.Data<>(timeSeconds, simulatedEnergy));
                antDistanceSeries.getData().add(new XYChart.Data<>(timeSeconds, simulatedDistance));

                getAllFixedSeries().forEach(this::trimSeries);
                updateChartXAxes(timeSeconds);
            }
        });
    }

    private List<XYChart.Series<Number, Number>> getAllFixedSeries() {
        return List.of(
                totalPopSeries, queensSeries, workersSeries, soldiersSeries, malesSeries,
                behaviorForagingSeries, behaviorDiggingSeries, behaviorNursingSeries,
                behaviorGuardingSeries, behaviorRoyalCareSeries, behaviorRestingSeries,
                foodSeries, waterSeries, proteinSeries, birthsSeries, deathsSeries,
                tempSeries, rainSeries, pheroSeries, tpsSeries,
                antHealthSeries, antEnergySeries, antDistanceSeries
        );
    }

    private void updateChartXAxes(double currentTimeSec) {
        double window = currentSelectedWindowSec;
        for (LineChart<Number, Number> c : List.of(chartMultiColony, chartCastes, chartBehaviors, chartResources, chartWeather, chartPerformance)) {
            NumberAxis xAxis = (NumberAxis) c.getXAxis();
            if (window <= 0) xAxis.setAutoRanging(true);
            else {
                xAxis.setAutoRanging(false);
                xAxis.setLowerBound(Math.max(0, currentTimeSec - window));
                xAxis.setUpperBound(Math.max(window, currentTimeSec));
                xAxis.setTickUnit(window / 6.0);
            }
        }
    }

    private void trimSeries(XYChart.Series<Number, Number> series) {
        if (series.getData().size() > MAX_DATA_POINTS) series.getData().remove(0);
    }

    public void truncateAfter(double simTimeSeconds) {
        Runnable doTruncate = () -> {
            historyList.removeIf(s -> s.getSimTimeSeconds() > simTimeSeconds);
            colonySeriesMap.values().forEach(s -> s.getData().removeIf(d -> d.getXValue() != null && d.getXValue().doubleValue() > simTimeSeconds));
            getAllFixedSeries().forEach(s -> s.getData().removeIf(d -> d.getXValue() != null && d.getXValue().doubleValue() > simTimeSeconds));
            updateChartXAxes(simTimeSeconds);
        };
        if (Platform.isFxApplicationThread()) doTruncate.run(); else Platform.runLater(doTruncate);
    }

    public void clear() {
        Runnable doClear = () -> {
            historyList.clear();
            colonySeriesMap.values().forEach(s -> s.getData().clear());
            chartMultiColony.getData().clear(); colonySeriesMap.clear();
            getAllFixedSeries().forEach(s -> s.getData().clear());
            lblPopulation.setText("0"); lblColoniesCount.setText("0"); lblQueens.setText("0");
            lblWorkers.setText("0"); lblSoldiers.setText("0"); lblFood.setText("0.0");
            lblWater.setText("0.0"); lblTickRate.setText("0 tps"); lblSimTime.setText("00:00:00 (0.0 s)");
            trackedAntCard.resetTelemetryLabels();
        };
        if (Platform.isFxApplicationThread()) doClear.run(); else Platform.runLater(doClear);
    }

    private void exportToCSV() { StatisticsCsvExporter.exportToCSV(getScene() != null ? getScene().getWindow() : null, scenarioName, historyList, i18n); }
}
