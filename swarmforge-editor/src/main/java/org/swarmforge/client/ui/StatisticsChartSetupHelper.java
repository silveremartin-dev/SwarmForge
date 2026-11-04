/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.swarmforge.client.util.I18nManager;

import java.util.List;

/**
 * Factory and setup helper for charts, series bindings, and KPI cards in {@link StatisticsDashboard}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
final class StatisticsChartSetupHelper {

    private StatisticsChartSetupHelper() {
    }

    static LineChart<Number, Number> createChart(String titleKey, String yLabelKey) {
        I18nManager i18n = I18nManager.getInstance();
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

    static void bindSeriesCheckbox(CheckBox chk, String labelKey, String ttKey,
                                   LineChart<Number, Number> chart, XYChart.Series<Number, Number> series) {
        I18nManager i18n = I18nManager.getInstance();
        chk.textProperty().bind(i18n.createStringBinding(labelKey));
        if (ttKey != null) chk.tooltipProperty().bind(i18n.createTooltipBinding(ttKey));
        chk.setSelected(true);
        chk.selectedProperty().addListener((obs, oldV, newV) -> toggleSeries(chart, series, newV));
    }

    static void toggleSeries(LineChart<Number, Number> chart, XYChart.Series<Number, Number> series, boolean show) {
        if (show) {
            if (!chart.getData().contains(series)) {
                chart.getData().add(series);
            }
        } else {
            chart.getData().remove(series);
        }
    }

    static GridPane createSummaryPanel(Label lblPopulation, Label lblColoniesCount, Label lblQueens,
                                       Label lblWorkers, Label lblSoldiers, Label lblFood,
                                       Label lblWater, Label lblTickRate, Label lblSimTime) {
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

    static HBox createKpiCard(String labelKey, Label valueLabel, String tooltipDesc) {
        I18nManager i18n = I18nManager.getInstance();
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

    static void populateTimeWindowItems(ComboBox<String> comboTimeWindow, I18nManager i18n) {
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

    static void updateSeriesNames(
            I18nManager i18n,
            XYChart.Series<Number, Number> totalPopSeries,
            XYChart.Series<Number, Number> queensSeries,
            XYChart.Series<Number, Number> workersSeries,
            XYChart.Series<Number, Number> soldiersSeries,
            XYChart.Series<Number, Number> malesSeries,
            XYChart.Series<Number, Number> behaviorForagingSeries,
            XYChart.Series<Number, Number> behaviorDiggingSeries,
            XYChart.Series<Number, Number> behaviorNursingSeries,
            XYChart.Series<Number, Number> behaviorGuardingSeries,
            XYChart.Series<Number, Number> behaviorRoyalCareSeries,
            XYChart.Series<Number, Number> behaviorRestingSeries,
            XYChart.Series<Number, Number> foodSeries,
            XYChart.Series<Number, Number> waterSeries,
            XYChart.Series<Number, Number> proteinSeries,
            XYChart.Series<Number, Number> birthsSeries,
            XYChart.Series<Number, Number> deathsSeries,
            XYChart.Series<Number, Number> tempSeries,
            XYChart.Series<Number, Number> rainSeries,
            XYChart.Series<Number, Number> pheroSeries,
            XYChart.Series<Number, Number> tpsSeries,
            XYChart.Series<Number, Number> antHealthSeries,
            XYChart.Series<Number, Number> antEnergySeries,
            XYChart.Series<Number, Number> antDistanceSeries
    ) {
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

    static void updateChartXAxes(List<LineChart<Number, Number>> charts, double currentTimeSec, double window) {
        for (LineChart<Number, Number> c : charts) {
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
}
