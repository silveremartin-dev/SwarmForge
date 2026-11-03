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
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.swarmforge.client.util.I18nManager;

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
}
