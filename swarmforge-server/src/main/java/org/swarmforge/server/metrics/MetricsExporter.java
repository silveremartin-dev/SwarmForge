/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server.metrics;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.simulation.Simulation;

/**
 * Exports simulation metrics in Prometheus text format.
 * Provides gauges and counters for monitoring.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class MetricsExporter {

        private final Simulation simulation;

        // Counters (monotonically increasing)
        private long totalBirths = 0;
        private long totalDeaths = 0;
        private long totalPredatorKills = 0;

        public MetricsExporter(Simulation simulation) {
                this.simulation = simulation;
        }

        /**
         * Generate Prometheus-formatted metrics.
         */
        public String export() {
                StringBuilder sb = new StringBuilder();

                // Simulation metadata
                appendMetric(sb, "swarmforge_simulation_tick", "Current simulation tick",
                                "gauge", simulation.getTickCount());
                appendMetric(sb, "swarmforge_simulation_running", "Is simulation running (1/0)",
                                "gauge", simulation.isRunning() ? 1 : 0);

                // Colony metrics
                int totalPopulation = 0;
                int totalWorkers = 0;
                int totalSoldiers = 0;
                int totalQueens = 0;
                float totalFood = 0;

                for (Colony colony : simulation.getColonies()) {
                        String colonyId = colony.getId().toString().substring(0, 8);

                        int pop = colony.getLivingIndividuals().size();
                        totalPopulation += pop;

                        appendMetricWithLabel(sb, "swarmforge_colony_population",
                                        "Colony population", "gauge", "colony", colonyId, pop);

                        int workers = 0, soldiers = 0, queens = 0;
                        for (Individual ind : colony.getLivingIndividuals()) {
                                switch (ind.getCaste()) {
                                        case WORKER -> workers++;
                                        case SOLDIER -> soldiers++;
                                        case QUEEN -> queens++;
                                        default -> {
                                        }
                                }
                        }
                        totalWorkers += workers;
                        totalSoldiers += soldiers;
                        totalQueens += queens;

                        appendMetricWithLabel(sb, "swarmforge_colony_workers",
                                        "Worker count", "gauge", "colony", colonyId, workers);
                        appendMetricWithLabel(sb, "swarmforge_colony_soldiers",
                                        "Soldier count", "gauge", "colony", colonyId, soldiers);

                        float food = colony.getFoodStored();
                        totalFood += food;
                        appendMetricWithLabel(sb, "swarmforge_colony_food",
                                        "Stored food", "gauge", "colony", colonyId, food);
                }

                // Global metrics
                appendMetric(sb, "swarmforge_total_population", "Total ant population",
                                "gauge", totalPopulation);
                appendMetric(sb, "swarmforge_total_workers", "Total workers",
                                "gauge", totalWorkers);
                appendMetric(sb, "swarmforge_total_soldiers", "Total soldiers",
                                "gauge", totalSoldiers);
                appendMetric(sb, "swarmforge_total_queens", "Total queens",
                                "gauge", totalQueens);
                appendMetric(sb, "swarmforge_total_food", "Total food stored",
                                "gauge", totalFood);

                // Predator metrics
                var predatorMgr = simulation.getPredatorManager();
                appendMetric(sb, "swarmforge_predator_count", "Active predators",
                                "gauge", predatorMgr.getPredatorCount());
                appendMetric(sb, "swarmforge_predator_kills_total", "Total kills by predators",
                                "counter", predatorMgr.getTotalKills());
                appendMetric(sb, "swarmforge_predators_killed_total", "Predators killed",
                                "counter", predatorMgr.getPredatorsKilled());

                // Weather metrics
                var weather = simulation.getWeather();
                appendMetric(sb, "swarmforge_weather_temperature", "Current temperature (C)",
                                "gauge", weather.getTemperature());
                appendMetric(sb, "swarmforge_weather_humidity", "Current humidity (%)",
                                "gauge", weather.getHumidity());

                // Food sources
                appendMetric(sb, "swarmforge_food_sources", "Active food sources",
                                "gauge", simulation.getFoodSources().size());

                // Counters
                appendMetric(sb, "swarmforge_births_total", "Total births",
                                "counter", totalBirths);
                appendMetric(sb, "swarmforge_deaths_total", "Total deaths",
                                "counter", totalDeaths);

                return sb.toString();
        }

        private void appendMetric(StringBuilder sb, String name, String help,
                        String type, Number value) {
                sb.append("# HELP ").append(name).append(" ").append(help).append("\n");
                sb.append("# TYPE ").append(name).append(" ").append(type).append("\n");
                sb.append(name).append(" ").append(value).append("\n");
        }

        private void appendMetricWithLabel(StringBuilder sb, String name, String help,
                        String type, String labelName, String labelValue,
                        Number value) {
                sb.append("# HELP ").append(name).append(" ").append(help).append("\n");
                sb.append("# TYPE ").append(name).append(" ").append(type).append("\n");
                sb.append(name).append("{").append(labelName).append("=\"")
                                .append(labelValue).append("\"} ").append(value).append("\n");
        }

        // Counter incrementers
        public void incrementBirths() {
                totalBirths++;
        }

        public void incrementDeaths() {
                totalDeaths++;
        }

        public void addPredatorKill() {
                totalPredatorKills++;
        }

        public long getTotalBirths() {
                return totalBirths;
        }

        public long getTotalDeaths() {
                return totalDeaths;
        }

        public long getTotalPredatorKills() {
                return totalPredatorKills;
        }
}
