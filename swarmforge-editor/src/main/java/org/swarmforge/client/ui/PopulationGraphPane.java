/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;

/**
 * Real-time population graphs for simulation statistics.
 *
 * @author Gemini AI Assistant
 */
public class PopulationGraphPane extends VBox {

    private static final int MAX_DATA_POINTS = 300; // 5 minutes at 1/sec

    private final LineChart<Number, Number> populationChart;
    private final LineChart<Number, Number> resourcesChart;

    private final XYChart.Series<Number, Number> totalPopSeries;
    private final XYChart.Series<Number, Number> workersSeries;
    private final XYChart.Series<Number, Number> queensSeries;

    private final XYChart.Series<Number, Number> foodSeries;
    private final XYChart.Series<Number, Number> birthsSeries;
    private final XYChart.Series<Number, Number> deathsSeries;

    private long lastUpdate = 0;
    private int dataPointIndex = 0;

    public PopulationGraphPane() {
        setSpacing(10);
        setStyle("-fx-padding: 10; -fx-background-color: #1a1a2e;");

        // Population Chart
        NumberAxis xAxis1 = new NumberAxis();
        xAxis1.setLabel("Time (s)");
        xAxis1.setAutoRanging(false);
        xAxis1.setLowerBound(0);
        xAxis1.setUpperBound(MAX_DATA_POINTS);

        NumberAxis yAxis1 = new NumberAxis();
        yAxis1.setLabel("Population");

        populationChart = new LineChart<>(xAxis1, yAxis1);
        populationChart.setTitle("Colony Population");
        populationChart.setCreateSymbols(false);
        populationChart.setAnimated(false);
        populationChart.setPrefHeight(200);

        totalPopSeries = new XYChart.Series<>();
        totalPopSeries.setName("Total");
        workersSeries = new XYChart.Series<>();
        workersSeries.setName("Workers");
        queensSeries = new XYChart.Series<>();
        queensSeries.setName("Queens");

        populationChart.getData().addAll(java.util.Arrays.asList(totalPopSeries, workersSeries, queensSeries));

        // Resources Chart
        NumberAxis xAxis2 = new NumberAxis();
        xAxis2.setLabel("Time (s)");
        xAxis2.setAutoRanging(false);
        xAxis2.setLowerBound(0);
        xAxis2.setUpperBound(MAX_DATA_POINTS);

        NumberAxis yAxis2 = new NumberAxis();
        yAxis2.setLabel("Count");

        resourcesChart = new LineChart<>(xAxis2, yAxis2);
        resourcesChart.setTitle("Resources & Events");
        resourcesChart.setCreateSymbols(false);
        resourcesChart.setAnimated(false);
        resourcesChart.setPrefHeight(200);

        foodSeries = new XYChart.Series<>();
        foodSeries.setName("Food");
        birthsSeries = new XYChart.Series<>();
        birthsSeries.setName("Births");
        deathsSeries = new XYChart.Series<>();
        deathsSeries.setName("Deaths");

        resourcesChart.getData().addAll(java.util.Arrays.asList(foodSeries, birthsSeries, deathsSeries));

        getChildren().addAll(populationChart, resourcesChart);

        // Apply dark theme styling
        applyDarkTheme();
    }

    /**
     * Update charts with new data point.
     */
    public void addDataPoint(int totalPop, int workers, int queens,
            int food, int births, int deaths) {
        // Limit update rate to 1/second
        long now = System.currentTimeMillis();
        if (now - lastUpdate < 1000)
            return;
        lastUpdate = now;

        // Add data points
        totalPopSeries.getData().add(new XYChart.Data<>(dataPointIndex, totalPop));
        workersSeries.getData().add(new XYChart.Data<>(dataPointIndex, workers));
        queensSeries.getData().add(new XYChart.Data<>(dataPointIndex, queens));

        foodSeries.getData().add(new XYChart.Data<>(dataPointIndex, food));
        birthsSeries.getData().add(new XYChart.Data<>(dataPointIndex, births));
        deathsSeries.getData().add(new XYChart.Data<>(dataPointIndex, deaths));

        dataPointIndex++;

        // Remove old data to keep chart responsive
        if (totalPopSeries.getData().size() > MAX_DATA_POINTS) {
            totalPopSeries.getData().remove(0);
            workersSeries.getData().remove(0);
            queensSeries.getData().remove(0);
            foodSeries.getData().remove(0);
            birthsSeries.getData().remove(0);
            deathsSeries.getData().remove(0);

            // Shift x-axis
            ((NumberAxis) populationChart.getXAxis()).setLowerBound(dataPointIndex - MAX_DATA_POINTS);
            ((NumberAxis) populationChart.getXAxis()).setUpperBound(dataPointIndex);
            ((NumberAxis) resourcesChart.getXAxis()).setLowerBound(dataPointIndex - MAX_DATA_POINTS);
            ((NumberAxis) resourcesChart.getXAxis()).setUpperBound(dataPointIndex);
        }
    }

    /**
     * Clear all data (e.g., when loading new simulation).
     */
    public void clearData() {
        totalPopSeries.getData().clear();
        workersSeries.getData().clear();
        queensSeries.getData().clear();
        foodSeries.getData().clear();
        birthsSeries.getData().clear();
        deathsSeries.getData().clear();
        dataPointIndex = 0;
    }

    private void applyDarkTheme() {
        String chartStyle = """
                -fx-background-color: #1a1a2e;
                -fx-plot-background-color: #16213e;
                -fx-chart-legend-background-color: #1a1a2e;
                """;
        populationChart.setStyle(chartStyle);
        resourcesChart.setStyle(chartStyle);
    }
}
