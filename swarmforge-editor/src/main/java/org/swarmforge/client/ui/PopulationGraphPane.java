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
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

/**
 * Real-time dynamic multi-colony and multi-caste population graphs for simulation statistics.
 * Tracks population evolution in real temporal units (seconds) across all castes and colonies.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class PopulationGraphPane extends VBox {

    private static final int MAX_DATA_POINTS = 600; // Up to 10 minutes at 1 update/sec

    private final LineChart<Number, Number> populationChart;
    private final LineChart<Number, Number> resourcesChart;

    // Dynamic series map: Key = "ColonyName - CasteName" or "ColonyName (Total)"
    private final Map<String, XYChart.Series<Number, Number>> dynamicSeriesMap = new HashMap<>();

    private final XYChart.Series<Number, Number> foodSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> birthsSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> deathsSeries = new XYChart.Series<>();

    private long lastUpdate = 0;

    public PopulationGraphPane() {
        setSpacing(10);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: #1a1a2e; -fx-padding: 12;");

        // Header Title
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label("📈 Évolution Temporelle des Populations & Ressources");
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");
        titleLabel.setTooltip(new Tooltip("Graphique d'analyse temporelle des effectifs par caste et par colonie en secondes réelles."));
        header.getChildren().add(titleLabel);

        // Population Chart
        NumberAxis xAxis1 = new NumberAxis();
        xAxis1.setLabel("Temps Réel Écoulé (secondes)");
        xAxis1.setAutoRanging(true);

        NumberAxis yAxis1 = new NumberAxis();
        yAxis1.setLabel("Effectif (Individus)");

        populationChart = new LineChart<>(xAxis1, yAxis1);
        populationChart.setTitle("Démographie des Colonies par Caste");
        populationChart.setCreateSymbols(false);
        populationChart.setAnimated(false);
        populationChart.setPrefHeight(240);
        Tooltip.install(populationChart, new Tooltip("Évolution dynamique des effectifs par caste pour toutes les colonies résidant dans la simulation."));

        // Resources Chart
        NumberAxis xAxis2 = new NumberAxis();
        xAxis2.setLabel("Temps Réel Écoulé (secondes)");
        xAxis2.setAutoRanging(true);

        NumberAxis yAxis2 = new NumberAxis();
        yAxis2.setLabel("Quantité / Événements");

        resourcesChart = new LineChart<>(xAxis2, yAxis2);
        resourcesChart.setTitle("Ressources de Surface & Événements Démographiques");
        resourcesChart.setCreateSymbols(false);
        resourcesChart.setAnimated(false);
        resourcesChart.setPrefHeight(220);
        Tooltip.install(resourcesChart, new Tooltip("Suivi temporel des réserves de nourriture libres, cumuls de naissances et de décès."));

        foodSeries.setName("Nourriture Libre");
        birthsSeries.setName("Cumul Naissances");
        deathsSeries.setName("Cumul Décès");

        resourcesChart.getData().addAll(java.util.Arrays.asList(foodSeries, birthsSeries, deathsSeries));

        getChildren().addAll(header, populationChart, resourcesChart);
        applyDarkTheme();
    }

    /**
     * Legacy update helper for single colony / default caste breakdown.
     */
    public void addDataPoint(int totalPop, int workers, int queens, int food, int births, int deaths) {
        addDataPoint(totalPop, workers, queens, 0, food, births, deaths);
    }

    /**
     * Legacy update helper including soldiers.
     */
    public void addDataPoint(int totalPop, int workers, int queens, int soldiers, int food, int births, int deaths) {
        Map<String, Map<String, Integer>> map = new HashMap<>();
        Map<String, Integer> castes = new HashMap<>();
        castes.put("Reines", queens);
        castes.put("Ouvrières", workers);
        if (soldiers > 0) castes.put("Soldats", soldiers);
        castes.put("Total Colonie", totalPop);
        map.put("Colonie #1", castes);

        long timeSec = (long) (System.currentTimeMillis() / 1000);
        addDataPointMultiColony(timeSec, map, food, births, deaths);
    }

    /**
     * Dynamic multi-colony and multi-caste update method.
     * Takes real simulation time in seconds.
     */
    public void addDataPointMultiColony(double timeSeconds, Map<String, Map<String, Integer>> colonyCasteMap, double food, int births, int deaths) {
        long now = System.currentTimeMillis();
        if (now - lastUpdate < 800) return; // Rate limit to ~1 Hz
        lastUpdate = now;

        if (colonyCasteMap != null) {
            for (Map.Entry<String, Map<String, Integer>> colEntry : colonyCasteMap.entrySet()) {
                String colonyName = colEntry.getKey();
                for (Map.Entry<String, Integer> casteEntry : colEntry.getValue().entrySet()) {
                    String casteName = casteEntry.getKey();
                    int count = casteEntry.getValue();
                    String seriesKey = colonyCasteMap.size() > 1 ? colonyName + " - " + casteName : casteName;

                    XYChart.Series<Number, Number> series = dynamicSeriesMap.computeIfAbsent(seriesKey, k -> {
                        XYChart.Series<Number, Number> s = new XYChart.Series<>();
                        s.setName(k);
                        populationChart.getData().add(s);
                        return s;
                    });

                    series.getData().add(new XYChart.Data<>(timeSeconds, count));
                    if (series.getData().size() > MAX_DATA_POINTS) {
                        series.getData().remove(0);
                    }
                }
            }
        }

        foodSeries.getData().add(new XYChart.Data<>(timeSeconds, food));
        birthsSeries.getData().add(new XYChart.Data<>(timeSeconds, births));
        deathsSeries.getData().add(new XYChart.Data<>(timeSeconds, deaths));

        trimSeries(foodSeries);
        trimSeries(birthsSeries);
        trimSeries(deathsSeries);
    }

    private void trimSeries(XYChart.Series<Number, Number> series) {
        if (series.getData().size() > MAX_DATA_POINTS) {
            series.getData().remove(0);
        }
    }

    /**
     * Clear all graph data series and reset.
     */
    public void clearData() {
        for (XYChart.Series<Number, Number> s : dynamicSeriesMap.values()) {
            s.getData().clear();
        }
        populationChart.getData().clear();
        dynamicSeriesMap.clear();

        foodSeries.getData().clear();
        birthsSeries.getData().clear();
        deathsSeries.getData().clear();
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

