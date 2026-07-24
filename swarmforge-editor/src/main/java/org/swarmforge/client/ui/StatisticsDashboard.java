/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Real-time statistics dashboard for colony monitoring.
 * Displays population graphs, resource levels, and health indicators.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class StatisticsDashboard extends VBox {

    private static final int MAX_DATA_POINTS = 300; // 5 minutes at 60 tps

    // Population chart
    private final XYChart.Series<Number, Number> populationSeries;
    private final XYChart.Series<Number, Number> birthsSeries;
    private final XYChart.Series<Number, Number> deathsSeries;

    // Resource chart
    private final XYChart.Series<Number, Number> foodSeries;
    private final XYChart.Series<Number, Number> waterSeries;

    // Stats labels
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
        setSpacing(10);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: #2b2b2b;");

        // === Summary Panel ===
        GridPane summaryGrid = createSummaryPanel();
        TitledPane summaryPane = new TitledPane("Colony Summary", summaryGrid);
        summaryPane.setCollapsible(false);

        // === Population Chart ===
        populationSeries = new XYChart.Series<>();
        populationSeries.setName("Population");
        birthsSeries = new XYChart.Series<>();
        birthsSeries.setName("Births");
        deathsSeries = new XYChart.Series<>();
        deathsSeries.setName("Deaths");

        LineChart<Number, Number> populationChart = createChart("Population Over Time", "Ticks", "Count");
        populationChart.getData().add(populationSeries);
        populationChart.getData().add(birthsSeries);
        populationChart.getData().add(deathsSeries);
        populationChart.setPrefHeight(200);

        TitledPane popPane = new TitledPane("Population", populationChart);
        popPane.setCollapsible(true);

        // === Resource Chart ===
        foodSeries = new XYChart.Series<>();
        foodSeries.setName("Food");
        waterSeries = new XYChart.Series<>();
        waterSeries.setName("Water");

        LineChart<Number, Number> resourceChart = createChart("Resources", "Ticks", "Amount");
        resourceChart.getData().add(foodSeries);
        resourceChart.getData().add(waterSeries);
        resourceChart.setPrefHeight(200);

        TitledPane resPane = new TitledPane("Resources", resourceChart);
        resPane.setCollapsible(true);

        // Add all to layout
        getChildren().addAll(summaryPane, popPane, resPane);
        VBox.setVgrow(popPane, Priority.ALWAYS);
        VBox.setVgrow(resPane, Priority.ALWAYS);
    }

    private GridPane createSummaryPanel() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));

        // Row 0
        grid.add(new Label("Population:"), 0, 0);
        grid.add(lblPopulation, 1, 0);
        grid.add(new Label("Tick Rate:"), 2, 0);
        grid.add(lblTickRate, 3, 0);

        // Row 1
        grid.add(new Label("Queens:"), 0, 1);
        grid.add(lblQueens, 1, 1);
        grid.add(new Label("Sim Time:"), 2, 1);
        grid.add(lblSimTime, 3, 1);

        // Row 2
        grid.add(new Label("Workers:"), 0, 2);
        grid.add(lblWorkers, 1, 2);
        grid.add(new Label("Food:"), 2, 2);
        grid.add(lblFood, 3, 2);

        // Row 3
        grid.add(new Label("Soldiers:"), 0, 3);
        grid.add(lblSoldiers, 1, 3);
        grid.add(new Label("Water:"), 2, 3);
        grid.add(lblWater, 3, 3);

        // Style labels
        String valueStyle = "-fx-font-weight: bold; -fx-text-fill: #4fc3f7;";
        lblPopulation.setStyle(valueStyle);
        lblQueens.setStyle(valueStyle);
        lblWorkers.setStyle(valueStyle);
        lblSoldiers.setStyle(valueStyle);
        lblFood.setStyle(valueStyle);
        lblWater.setStyle(valueStyle);
        lblTickRate.setStyle(valueStyle);
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

        return chart;
    }

    /**
     * Update dashboard with new stats.
     * Call this from the simulation update loop.
     */
    public void update(ColonyStats stats) {
        Platform.runLater(() -> {
            // Update labels
            lblPopulation.setText(String.valueOf(stats.population));
            lblQueens.setText(String.valueOf(stats.queens));
            lblWorkers.setText(String.valueOf(stats.workers));
            lblSoldiers.setText(String.valueOf(stats.soldiers));
            lblFood.setText(String.format("%.1f", stats.food));
            lblWater.setText(String.format("%.1f", stats.water));
            lblTickRate.setText(String.format("%.1f tps", stats.tickRate));
            lblSimTime.setText(formatTime(stats.simTicks));

            // Add data points
            dataPointIndex++;

            // Population
            XYChart.Data<Number, Number> popData = new XYChart.Data<>(dataPointIndex, stats.population);
            populationSeries.getData().add(popData);
            if (populationSeries.getData().size() > MAX_DATA_POINTS) {
                populationSeries.getData().remove(0);
            }

            // Resources
            foodSeries.getData().add(new XYChart.Data<>(dataPointIndex, stats.food));
            waterSeries.getData().add(new XYChart.Data<>(dataPointIndex, stats.water));

            if (foodSeries.getData().size() > MAX_DATA_POINTS) {
                foodSeries.getData().remove(0);
                waterSeries.getData().remove(0);
            }
        });
    }

    private String formatTime(long ticks) {
        long seconds = ticks / 60;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        return String.format("%d:%02d:%02d", hours, minutes % 60, seconds % 60);
    }

    /**
     * Clear all chart data.
     */
    public void clear() {
        Platform.runLater(() -> {
            populationSeries.getData().clear();
            birthsSeries.getData().clear();
            deathsSeries.getData().clear();
            foodSeries.getData().clear();
            waterSeries.getData().clear();
            dataPointIndex = 0;
        });
    }

    /**
     * Stats data holder.
     */
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

        public ColonyStats() {
        }

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
