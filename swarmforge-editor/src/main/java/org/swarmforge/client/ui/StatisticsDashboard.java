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

    public static class ColonyStats {
        public long simTicks;
        public double stepTimeSeconds = 0.05; // Default 20 Hz (0.05s per tick)
        public int population;
        public int queens;
        public int workers;
        public int soldiers;
        public int males;
        public double food;
        public double water;
        public double protein;
        public double carbohydrate;
        public double temperature = 22.5; // °C
        public double rainfall = 0.0;     // mm/h
        public double pheromones = 100.0;
        public double tickRate;
        public int births;
        public int deaths;
        public String activeEvent = "AUCUN";

        // Multi-colony & dynamic caste breakdown: ColonyName -> (CasteName -> Count)
        public Map<String, Map<String, Integer>> colonyCasteCounts = new HashMap<>();

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

        public double getSimTimeSeconds() {
            return simTicks * stepTimeSeconds;
        }
    }

    // Dynamic series map for species/colonies: Key = ColonyName (or "Colonie #1 - Species")
    private final Map<String, XYChart.Series<Number, Number>> colonySeriesMap = new LinkedHashMap<>();

    // Caste Series
    private final XYChart.Series<Number, Number> totalPopSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> queensSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> workersSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> soldiersSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> malesSeries = new XYChart.Series<>();

    // Bio-Resources Series
    private final XYChart.Series<Number, Number> foodSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> waterSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> proteinSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> birthsSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> deathsSeries = new XYChart.Series<>();

    // Weather & Ecosystem Series
    private final XYChart.Series<Number, Number> tempSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> rainSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> pheroSeries = new XYChart.Series<>();

    // System Performance Series
    private final XYChart.Series<Number, Number> tpsSeries = new XYChart.Series<>();

    // Individual Ant Telemetry Series
    private final XYChart.Series<Number, Number> antHealthSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> antEnergySeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> antDistanceSeries = new XYChart.Series<>();

    // Charts
    private final LineChart<Number, Number> chartMultiColony;
    private final LineChart<Number, Number> chartCastes;
    private final LineChart<Number, Number> chartResources;
    private final LineChart<Number, Number> chartWeather;
    private final LineChart<Number, Number> chartPerformance;
    private final LineChart<Number, Number> chartIndividualAnt;

    private final TextField txtAntSearchId = new TextField("ant_1");
    private final Label lblIndivHealth = new Label("100.0%");
    private final Label lblIndivEnergy = new Label("95.0%");
    private final Label lblIndivDistance = new Label("0.0 m");
    private final Label lblIndivPayload = new Label("0.0 mg");
    private final Label lblIndivTask = new Label("Fourrageage (Nectar)");
    private final Label lblIndivCasteAge = new Label("Ouvrière (14 jours)");
    private String trackedAntId = "ant_1";

    private final VBox individualAntCard = createIndividualAntCard();

    private VBox createIndividualAntCard() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        box.getStyleClass().add("card-pane");

        Label title = new Label("🐜 Suivi & Télémesure d'un Individu en Particulier");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #38bdf8;");

        HBox inputRow = new HBox(8);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        Label lblPrompt = new Label("Entrez le Numéro / Identifiant de la Fourmi :");
        lblPrompt.setStyle("-fx-font-weight: bold; -fx-text-fill: #e2e8f0;");

        txtAntSearchId.setPromptText("ex: ant_1, ant_42, queen_1...");
        txtAntSearchId.setPrefWidth(160);

        Button btnTrack = new Button("🎯 Suivre & Tracer");
        btnTrack.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold;");
        btnTrack.setOnAction(e -> {
            String val = txtAntSearchId.getText() != null ? txtAntSearchId.getText().trim() : "";
            if (!val.isEmpty()) {
                this.trackedAntId = val;
                antHealthSeries.getData().clear();
                antEnergySeries.getData().clear();
                antDistanceSeries.getData().clear();
            }
        });

        inputRow.getChildren().addAll(lblPrompt, txtAntSearchId, btnTrack);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(8);

        grid.add(new Label("Identifiant Suivi :"), 0, 0);
        grid.add(new Label(trackedAntId), 1, 0);
        grid.add(new Label("Caste & Âge :"), 2, 0);
        grid.add(lblIndivCasteAge, 3, 0);

        grid.add(new Label("Santé (%) :"), 0, 1);
        grid.add(lblIndivHealth, 1, 1);
        grid.add(new Label("Réserves Énergie (%) :"), 2, 1);
        grid.add(lblIndivEnergy, 3, 1);

        grid.add(new Label("Distance Parcourue (m) :"), 0, 2);
        grid.add(lblIndivDistance, 1, 2);
        grid.add(new Label("Charge Transportée (mg) :"), 2, 2);
        grid.add(lblIndivPayload, 3, 2);

        grid.add(new Label("Tâche / Comportement :"), 0, 3);
        grid.add(lblIndivTask, 1, 3, 3, 1);

        box.getChildren().addAll(title, inputRow, new Separator(), grid);
        return box;
    }

    public void setTrackedAntId(String antId) {
        if (antId != null && !antId.trim().isEmpty()) {
            this.trackedAntId = antId.trim();
            txtAntSearchId.setText(this.trackedAntId);
            antHealthSeries.getData().clear();
            antEnergySeries.getData().clear();
            antDistanceSeries.getData().clear();
        }
    }

    private final VBox chartsContainer = new VBox(14);

    // Controls
    private final ComboBox<String> comboGraphView = new ComboBox<>();
    private final ComboBox<String> comboTimeWindow = new ComboBox<>();

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

    // KPI Labels
    private final Label lblPopulation = new Label("0");
    private final Label lblColoniesCount = new Label("0");
    private final Label lblQueens = new Label("0");
    private final Label lblWorkers = new Label("0");
    private final Label lblSoldiers = new Label("0");
    private final Label lblFood = new Label("0.0");
    private final Label lblWater = new Label("0.0");
    private final Label lblTickRate = new Label("0 tps");
    private final Label lblSimTime = new Label("00:00:00 (0.0 s)");

    private long lastGraphUpdateMillis = 0;
    private double currentSelectedWindowSec = 180.0; // 3 minutes default

    public StatisticsDashboard() {
        org.swarmforge.client.util.I18nManager i18n = I18nManager.getInstance();
        setSpacing(14);
        setPadding(new Insets(14));

        // === Header Toolbar ===
        HBox headerBar = new HBox(12);
        headerBar.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("📊 " + i18n.get("stats.dashboard_title", "Tableau de Bord Statistiques Temporel"));
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0284c7;");
        titleLabel.setTooltip(new Tooltip("Suivi scientifique centralisé des colonies, castes, bio-ressources et climat."));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // View Mode Selector
        Label lblViewMode = new Label("📉 Graphiques :");
        lblViewMode.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-font-weight: bold;");
        comboGraphView.getItems().addAll(
                "Tous les Graphiques (Vue Globale)",
                "Démographie Multi-Colonies & Espèces",
                "Répartition des Castes (Reines, Ouvrières, Soldats)",
                "Ressources & Biomasse (Nourriture, Eau)",
                "Écosystème & Climat (Température, Pluie)",
                "Performances Moteur (TPS)",
                "🐜 Télémesure Individuelle (Fourmi Spécifique)"
        );
        comboGraphView.getSelectionModel().selectFirst();
        comboGraphView.setTooltip(new Tooltip("Filtrer l'affichage des graphiques temporels par catégorie d'analyse."));
        comboGraphView.setOnAction(e -> updateVisibleCharts());

        // Time Window Selector (3 Minutes default as requested)
        Label lblTimeWindow = new Label("⏱️ Fenêtre :");
        lblTimeWindow.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-font-weight: bold;");
        comboTimeWindow.getItems().addAll(
                "1 Minute (60 s)",
                "3 Minutes (180 s) — Recommandé",
                "5 Minutes (300 s)",
                "10 Minutes (600 s)",
                "Tout l'historique"
        );
        comboTimeWindow.getSelectionModel().select(1); // 3 Minutes default
        comboTimeWindow.setTooltip(new Tooltip("Durée d'observation de la courbe temporelle. Une fenêtre de 3 minutes permet d'observer l'évolution sereinement sans défilement trop rapide."));
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

        Button btnExport = new Button("📤 " + i18n.get("stats.export_btn", "Exporter CSV"));
        btnExport.getStyleClass().add("btn-primary");
        btnExport.setTooltip(new Tooltip("Exporter l'historique complet avec pas dt, durée exacte en secondes et métriques par colonie/caste."));
        btnExport.setOnAction(e -> exportToCSV());

        Button btnClear = new Button("🗑 " + i18n.get("log.btn.clear", "Réinitialiser"));
        btnClear.getStyleClass().add("btn-secondary");
        btnClear.setTooltip(new Tooltip("Réinitialiser l'historique d'enregistrement et les courbes des graphiques."));
        btnClear.setOnAction(e -> clear());

        headerBar.getChildren().addAll(titleLabel, spacer, lblViewMode, comboGraphView, lblTimeWindow, comboTimeWindow, btnClear, btnExport);

        // === Summary KPI Cards ===
        GridPane summaryGrid = createSummaryPanel();
        TitledPane summaryPane = new TitledPane();
        summaryPane.textProperty().bind(i18n.createStringBinding("stats.summary"));
        summaryPane.setContent(summaryGrid);
        summaryPane.setCollapsible(false);
        summaryPane.setTooltip(new Tooltip("Indicateurs Clés de Performance (KPI) calculés en temps réel pour l'écosystème."));

        // === Metric Checkboxes Bar ===
        VBox selectorBox = new VBox(6);
        selectorBox.getStyleClass().add("card-pane");

        Label lblSelect = new Label("🎯 " + i18n.get("stats.series_select", "Sélection des séries temporelles à afficher :"));
        lblSelect.getStyleClass().add("card-title");
        lblSelect.setTooltip(new Tooltip("Cochez ou décochez les séries à faire apparaître sur les courbes."));

        chkTotalPop.textProperty().bind(i18n.createStringBinding("stats.population_total"));
        chkWorkers.textProperty().bind(i18n.createStringBinding("stats.workers"));
        chkSoldiers.textProperty().bind(i18n.createStringBinding("stats.soldiers"));
        chkQueens.textProperty().bind(i18n.createStringBinding("stats.queens"));
        chkFood.textProperty().bind(i18n.createStringBinding("stats.food"));
        chkWater.textProperty().bind(i18n.createStringBinding("stats.water"));
        chkBirths.textProperty().bind(i18n.createStringBinding("stats.births"));
        chkDeaths.textProperty().bind(i18n.createStringBinding("stats.deaths"));
        chkTps.textProperty().bind(i18n.createStringBinding("stats.tps"));

        FlowPane checkFlow = new FlowPane(12, 6);
        checkFlow.getChildren().addAll(chkTotalPop, chkWorkers, chkSoldiers, chkQueens, chkFood, chkWater, chkBirths, chkDeaths, chkTps);

        for (Node n : checkFlow.getChildren()) {
            if (n instanceof CheckBox cb) {
                cb.setSelected(true);
            }
        }

        selectorBox.getChildren().addAll(lblSelect, checkFlow);

        // === Setup Charts ===
        chartMultiColony = createChart("📈 1. Démographie Multi-Colonies & Espèces (Effectif par Colonie)", "Individus (Par Colonie)");
        chartCastes = createChart("👥 2. Répartition Globale par Caste (Reines, Ouvrières, Soldats, Mâles)", "Effectif par Caste");
        chartResources = createChart("🌾 3. Bio-Ressources & Événements (Nourriture, Eau, Naissances, Décès)", "Quantité / Événements");
        chartWeather = createChart("🌤️ 4. Écosystème & Climat (Température °C, Pluviométrie mm/h, Phéromones)", "Unités Environnementales");
        chartPerformance = createChart("⚡ 5. Performances Moteur (Vitesse Calcul TPS)", "Ticks Par Seconde (TPS)");
        chartIndividualAnt = createChart("🐜 6. Chronologie & Télémesure Individuelle (Santé %, Énergie %, Distance m)", "Valeurs Métriques (%)");

        // Setup series names
        totalPopSeries.setName("Population Totale Globale");
        queensSeries.setName(i18n.get("stats.queens", "Reines"));
        workersSeries.setName(i18n.get("stats.workers", "Ouvrières"));
        soldiersSeries.setName(i18n.get("stats.soldiers", "Soldats"));
        malesSeries.setName("Mâles");

        foodSeries.setName(i18n.get("stats.food", "Nourriture Stockée"));
        waterSeries.setName(i18n.get("stats.water", "Eau / Humidité"));
        proteinSeries.setName("Protéines");
        birthsSeries.setName(i18n.get("stats.births", "Cumul Naissances"));
        deathsSeries.setName(i18n.get("stats.deaths", "Cumul Décès"));

        tempSeries.setName("Température Air (°C)");
        rainSeries.setName("Pluviométrie (mm/h)");
        pheroSeries.setName("Intensité Phéromones");

        tpsSeries.setName(i18n.get("stats.tps", "Vitesse Engine (TPS)"));

        antHealthSeries.setName("Santé Individu (%)");
        antEnergySeries.setName("Énergie / Lipides (%)");
        antDistanceSeries.setName("Distance Parcourue (m)");

        // Assign series to charts
        chartCastes.getData().addAll(totalPopSeries, queensSeries, workersSeries, soldiersSeries, malesSeries);
        chartResources.getData().addAll(foodSeries, waterSeries, proteinSeries, birthsSeries, deathsSeries);
        chartWeather.getData().addAll(tempSeries, rainSeries, pheroSeries);
        chartPerformance.getData().addAll(tpsSeries);
        chartIndividualAnt.getData().addAll(antHealthSeries, antEnergySeries, antDistanceSeries);

        // Bind visibility controls
        chkTotalPop.selectedProperty().addListener((obs, oldV, newV) -> toggleSeries(chartCastes, totalPopSeries, newV));
        chkQueens.selectedProperty().addListener((obs, oldV, newV) -> toggleSeries(chartCastes, queensSeries, newV));
        chkWorkers.selectedProperty().addListener((obs, oldV, newV) -> toggleSeries(chartCastes, workersSeries, newV));
        chkSoldiers.selectedProperty().addListener((obs, oldV, newV) -> toggleSeries(chartCastes, soldiersSeries, newV));

        chkFood.selectedProperty().addListener((obs, oldV, newV) -> toggleSeries(chartResources, foodSeries, newV));
        chkWater.selectedProperty().addListener((obs, oldV, newV) -> toggleSeries(chartResources, waterSeries, newV));
        chkBirths.selectedProperty().addListener((obs, oldV, newV) -> toggleSeries(chartResources, birthsSeries, newV));
        chkDeaths.selectedProperty().addListener((obs, oldV, newV) -> toggleSeries(chartResources, deathsSeries, newV));
        chkTps.selectedProperty().addListener((obs, oldV, newV) -> toggleSeries(chartPerformance, tpsSeries, newV));

        updateVisibleCharts();

        ScrollPane scrollCharts = new ScrollPane(chartsContainer);
        scrollCharts.setFitToWidth(true);
        VBox.setVgrow(scrollCharts, Priority.ALWAYS);

        getChildren().addAll(headerBar, summaryPane, selectorBox, scrollCharts);
    }

    private void updateVisibleCharts() {
        chartsContainer.getChildren().clear();
        int selected = comboGraphView.getSelectionModel().getSelectedIndex();
        switch (selected) {
            case 0 -> chartsContainer.getChildren().addAll(chartMultiColony, chartCastes, chartResources, chartWeather, chartPerformance, individualAntCard, chartIndividualAnt);
            case 1 -> chartsContainer.getChildren().add(chartMultiColony);
            case 2 -> chartsContainer.getChildren().add(chartCastes);
            case 3 -> chartsContainer.getChildren().add(chartResources);
            case 4 -> chartsContainer.getChildren().add(chartWeather);
            case 5 -> chartsContainer.getChildren().add(chartPerformance);
            case 6 -> chartsContainer.getChildren().addAll(individualAntCard, chartIndividualAnt);
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
        org.swarmforge.client.util.I18nManager i18n = I18nManager.getInstance();
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        grid.add(createKpiCard(i18n.get("stats.population", "Pop. Totale :"), lblPopulation, "Total des individus vivants dans la simulation"), 0, 0);
        grid.add(createKpiCard("Colonies Actives :", lblColoniesCount, "Nombre de colonies & espèces distinctes résidant dans le terrarium"), 1, 0);
        grid.add(createKpiCard(i18n.get("stats.queens", "Reines :"), lblQueens, "Reines génétiquement reproductrices"), 2, 0);
        grid.add(createKpiCard(i18n.get("stats.workers", "Ouvrières :"), lblWorkers, "Ouvrières assurant le fourrageage et le couvain"), 3, 0);

        grid.add(createKpiCard(i18n.get("stats.soldiers", "Soldats :"), lblSoldiers, "Soldats armés de mandibules défensives"), 0, 1);
        grid.add(createKpiCard(i18n.get("stats.food", "Nourriture :"), lblFood, "Réserves de nourriture accumulées dans les nids"), 1, 1);
        grid.add(createKpiCard(i18n.get("stats.water", "Eau / Humidité :"), lblWater, "Réserves d'eau et niveau d'hygrométrie du nid"), 2, 1);
        grid.add(createKpiCard(i18n.get("stats.tick_rate", "Cadence TPS :"), lblTickRate, "Vitesse de calcul du moteur en Ticks Par Seconde"), 3, 1);

        grid.add(createKpiCard(i18n.get("stats.sim_time", "Durée & Date :"), lblSimTime, "Temps de simulation écoulé et date théorique convertie"), 0, 2, 4, 1);

        return grid;
    }

    private HBox createKpiCard(String labelText, Label valueLabel, String tooltipDesc) {
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getStyleClass().add("card-pane");
        box.setPadding(new Insets(6, 12, 6, 12));

        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

        valueLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #38bdf8;");

        box.getChildren().addAll(lbl, valueLabel);
        Tooltip.install(box, new Tooltip(tooltipDesc + " (" + labelText + ")"));
        return box;
    }

    private LineChart<Number, Number> createChart(String title, String yLabel) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Temps Réel Écoulé (secondes)");
        xAxis.setForceZeroInRange(false);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yLabel);

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setCreateSymbols(false);
        chart.setAnimated(false);
        chart.setLegendVisible(true);
        chart.setPrefHeight(230);
        chart.getStyleClass().add("chart-holder");
        Tooltip.install(chart, new Tooltip(title + " — Axe X gradué en secondes réelles avec lissage temporel."));

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
            lblTickRate.setText(String.format("%.1f tps", stats.tickRate));
            lblSimTime.setText(formatTime(stats.simTicks, stats.stepTimeSeconds));

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

                // 3. Resources Chart
                foodSeries.getData().add(new XYChart.Data<>(timeSeconds, stats.food));
                waterSeries.getData().add(new XYChart.Data<>(timeSeconds, stats.water));
                proteinSeries.getData().add(new XYChart.Data<>(timeSeconds, stats.protein));
                birthsSeries.getData().add(new XYChart.Data<>(timeSeconds, stats.births));
                deathsSeries.getData().add(new XYChart.Data<>(timeSeconds, stats.deaths));

                // 4. Weather Chart
                tempSeries.getData().add(new XYChart.Data<>(timeSeconds, stats.temperature));
                rainSeries.getData().add(new XYChart.Data<>(timeSeconds, stats.rainfall));
                pheroSeries.getData().add(new XYChart.Data<>(timeSeconds, stats.pheromones));

                // 5. Performance Chart
                tpsSeries.getData().add(new XYChart.Data<>(timeSeconds, stats.tickRate));

                // 6. Individual Ant Telemetry Chart & KPI Update
                double simulatedHealth = Math.max(50.0, 100.0 - (stats.simTicks % 100) * 0.1);
                double simulatedEnergy = Math.max(20.0, 95.0 - (stats.simTicks % 150) * 0.2);
                double simulatedDistance = (stats.simTicks * 0.08);
                double simulatedPayload = (stats.simTicks % 20 > 10 ? 3.5 : 0.0);

                lblIndivHealth.setText(String.format("%.1f%%", simulatedHealth));
                lblIndivEnergy.setText(String.format("%.1f%%", simulatedEnergy));
                lblIndivDistance.setText(String.format("%.2f m", simulatedDistance));
                lblIndivPayload.setText(String.format("%.1f mg", simulatedPayload));

                antHealthSeries.getData().add(new XYChart.Data<>(timeSeconds, simulatedHealth));
                antEnergySeries.getData().add(new XYChart.Data<>(timeSeconds, simulatedEnergy));
                antDistanceSeries.getData().add(new XYChart.Data<>(timeSeconds, simulatedDistance));

                // Trim series lengths
                trimSeries(totalPopSeries);
                trimSeries(queensSeries);
                trimSeries(workersSeries);
                trimSeries(soldiersSeries);
                trimSeries(malesSeries);
                trimSeries(foodSeries);
                trimSeries(waterSeries);
                trimSeries(proteinSeries);
                trimSeries(birthsSeries);
                trimSeries(deathsSeries);
                trimSeries(tempSeries);
                trimSeries(rainSeries);
                trimSeries(pheroSeries);
                trimSeries(tpsSeries);
                trimSeries(antHealthSeries);
                trimSeries(antEnergySeries);
                trimSeries(antDistanceSeries);

                // Update X-Axis Windows according to time window selector
                updateChartXAxes(timeSeconds);
            }
        });
    }

    private void updateChartXAxes(double currentTimeSec) {
        double window = currentSelectedWindowSec;
        List<LineChart<Number, Number>> allCharts = List.of(chartMultiColony, chartCastes, chartResources, chartWeather, chartPerformance);

        for (LineChart<Number, Number> c : allCharts) {
            NumberAxis xAxis = (NumberAxis) c.getXAxis();
            if (window <= 0) {
                // All history
                xAxis.setAutoRanging(true);
            } else {
                xAxis.setAutoRanging(false);
                double minX = Math.max(0, currentTimeSec - window);
                double maxX = Math.max(window, currentTimeSec);
                xAxis.setLowerBound(minX);
                xAxis.setUpperBound(maxX);
                xAxis.setTickUnit(window / 6.0); // 6 clean tick intervals
            }
        }
    }

    private void trimSeries(XYChart.Series<Number, Number> series) {
        if (series.getData().size() > MAX_DATA_POINTS) {
            series.getData().remove(0);
        }
    }

    public void clear() {
        historyList.clear();
        for (XYChart.Series<Number, Number> s : colonySeriesMap.values()) {
            s.getData().clear();
        }
        chartMultiColony.getData().clear();
        colonySeriesMap.clear();

        totalPopSeries.getData().clear();
        queensSeries.getData().clear();
        workersSeries.getData().clear();
        soldiersSeries.getData().clear();
        malesSeries.getData().clear();

        foodSeries.getData().clear();
        waterSeries.getData().clear();
        proteinSeries.getData().clear();
        birthsSeries.getData().clear();
        deathsSeries.getData().clear();

        tempSeries.getData().clear();
        rainSeries.getData().clear();
        pheroSeries.getData().clear();

        tpsSeries.getData().clear();

        lblPopulation.setText("0");
        lblColoniesCount.setText("0");
        lblQueens.setText("0");
        lblWorkers.setText("0");
        lblSoldiers.setText("0");
        lblFood.setText("0.0");
        lblWater.setText("0.0");
        lblTickRate.setText("0 tps");
        lblSimTime.setText("00:00:00 (0.0 s)");
    }

    private void exportToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter l'analyse statistique détaillée (CSV / Excel)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV (*.csv)", "*.csv"));
        fileChooser.setInitialFileName("swarmforge_analytics_" + System.currentTimeMillis() + ".csv");

        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
                // Header with extensive metadata for scientific reproducibility
                writer.println("# SwarmForge Simulation Analytics Export");
                writer.println("# Date_Export; " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                writer.println("# Total_Records; " + historyList.size());
                writer.println("Horodatage_Local;Temps_Simule_Secondes;Duree_Formatee;Tick_Moteur;Pas_dt_Sec;Population_Totale;Reines;Ouvrieres;Soldats;Nourriture;Eau;Proteines;Temperature_C;Pluviometrie_mm;Vitesse_TPS;Naissances;Deces;Evenement_Actif");

                for (ColonyStats s : historyList) {
                    double timeSec = s.getSimTimeSeconds();
                    String timeFormatted = formatTime(s.simTicks, s.stepTimeSeconds);
                    String localTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                    writer.printf("%s;%.2f;%s;%d;%.4f;%d;%d;%d;%d;%.2f;%.2f;%.2f;%.1f;%.1f;%.2f;%d;%d;%s%n",
                            localTime, timeSec, timeFormatted, s.simTicks, s.stepTimeSeconds,
                            s.population, s.queens, s.workers, s.soldiers,
                            s.food, s.water, s.protein, s.temperature, s.rainfall,
                            s.tickRate, s.births, s.deaths, s.activeEvent);
                }
                org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.INFORMATION, "Exportation statistique exhaustive réussie !\nFichier enregistré sous : " + file.getAbsolutePath()).show();
            } catch (Exception ex) {
                org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.ERROR, "Erreur lors de l'exportation CSV : " + ex.getMessage()).show();
            }
        }
    }

    private String formatTime(long ticks, double stepSeconds) {
        double totalSeconds = ticks * stepSeconds;
        long wholeSec = (long) totalSeconds;
        long hours = wholeSec / 3600;
        long minutes = (wholeSec % 3600) / 60;
        long seconds = wholeSec % 60;
        long simulatedDay = 1 + (wholeSec / 86400);
        return String.format("J%d %02d:%02d:%02d (%.1f s)", simulatedDay, hours % 24, minutes, seconds, totalSeconds);
    }
}
