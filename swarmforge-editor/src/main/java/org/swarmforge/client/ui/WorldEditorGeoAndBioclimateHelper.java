/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import java.util.Locale;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.swarmforge.client.util.I18nManager;

/**
 * Geographic location fetching (Open-Meteo Geocoding & Elevation APIs),
 * bioclimatic zone adaptation, and species ecological compatibility scoring
 * for {@link WorldEditorPane}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
final class WorldEditorGeoAndBioclimateHelper {

    private WorldEditorGeoAndBioclimateHelper() {
    }

    static VBox buildTerrainSourceBlock(WorldEditorPane pane) {
        pane.cityNameField = new TextField("Serengeti, TZ");
        pane.cityNameField.setPromptText("Ex: Serengeti, Paris, Manaus, Lhasa...");
        pane.cityNameField.setPrefWidth(150);
        pane.cityNameField.setOnAction(e -> fetchCityCoordinates(pane, pane.cityNameField.getText()));

        pane.latField = new TextField("-2.3333");
        pane.latField.setPrefWidth(58);
        pane.lonField = new TextField("34.8333");
        pane.lonField.setPrefWidth(58);
        pane.altField = new TextField("1500");
        pane.altField.setPrefWidth(50);

        Button btnSyncGeo = new Button();
        btnSyncGeo.textProperty().bind(I18nManager.getInstance().createStringBinding("world.geo.sync"));
        btnSyncGeo.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
        btnSyncGeo.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.geo.sync.tt"));

        Button btnAdaptLocation = new Button();
        btnAdaptLocation.textProperty().bind(I18nManager.getInstance().createStringBinding("world.geo.adapt"));
        btnAdaptLocation.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
        btnAdaptLocation.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.geo.adapt.tt"));

        pane.geoStatusLabel = new Label();
        pane.geoStatusLabel.textProperty().bind(I18nManager.getInstance().createStringBinding("world.geo.status_default"));
        pane.geoStatusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8; -fx-wrap-text: true;");

        Runnable triggerGeoAction = () -> {
            String cName = pane.cityNameField.getText().trim();
            if (!cName.isEmpty()) {
                fetchCityCoordinates(pane, cName);
            } else {
                try {
                    double lat = Double.parseDouble(pane.latField.getText().trim());
                    double lon = Double.parseDouble(pane.lonField.getText().trim());
                    double alt = pane.altField != null && !pane.altField.getText().trim().isEmpty() ? Double.parseDouble(pane.altField.getText().trim()) : 0.0;
                    applyBioclimaticAdaptation(pane, lat, lon, alt);
                    pane.geoStatusLabel.textProperty().unbind();
                    pane.geoStatusLabel.setText(I18nManager.getInstance().get("world.geo.status_applied", lat, lon, (int) alt));
                    pane.geoStatusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #4ade80; -fx-wrap-text: true;");
                    pane.regenerateAndRepaint();
                } catch (Exception ex) {
                    pane.geoStatusLabel.textProperty().unbind();
                    pane.geoStatusLabel.setText(I18nManager.getInstance().get("world.geo.status_invalid"));
                    pane.geoStatusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #ef4444;");
                }
            }
        };

        btnSyncGeo.setMaxWidth(Double.MAX_VALUE);
        btnAdaptLocation.setMaxWidth(Double.MAX_VALUE);
        btnSyncGeo.setOnAction(e -> triggerGeoAction.run());
        btnAdaptLocation.setOnAction(e -> triggerGeoAction.run());

        Label lblCity = new Label();
        lblCity.textProperty().bind(I18nManager.getInstance().createStringBinding("world.geo.city"));
        lblCity.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;");
        HBox cityRow = new HBox(6, lblCity, pane.cityNameField);
        cityRow.setAlignment(Pos.CENTER_LEFT);

        Label lblLat = new Label();
        lblLat.textProperty().bind(I18nManager.getInstance().createStringBinding("world.geo.lat"));
        lblLat.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;");
        Label lblLon = new Label();
        lblLon.textProperty().bind(I18nManager.getInstance().createStringBinding("world.geo.lon"));
        lblLon.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;");
        Label lblAlt = new Label();
        lblAlt.textProperty().bind(I18nManager.getInstance().createStringBinding("world.geo.alt"));
        lblAlt.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;");

        HBox gpsRow = new HBox(5, lblLat, pane.latField, lblLon, pane.lonField, lblAlt, pane.altField);
        gpsRow.setAlignment(Pos.CENTER_LEFT);

        VBox actionBtnBox = new VBox(5, btnSyncGeo, btnAdaptLocation);
        actionBtnBox.setPadding(new Insets(4, 0, 4, 0));

        return new VBox(8, cityRow, gpsRow, actionBtnBox, pane.geoStatusLabel);
    }

    static void fetchCityCoordinates(WorldEditorPane pane, String cityQuery) {
        String query = cityQuery != null ? cityQuery.trim() : "";
        pane.geoStatusLabel.textProperty().unbind();
        pane.geoStatusLabel.setText(I18nManager.getInstance().get("world.geo.status_fetching"));
        pane.geoStatusLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 10px;");

        new Thread(() -> {
            try {
                if (query.isEmpty()) {
                    double lat = Double.parseDouble(pane.latField.getText().trim());
                    double lon = Double.parseDouble(pane.lonField.getText().trim());
                    double alt = 0.0;
                    try {
                        String elevUrlStr = String.format(Locale.US, "https://api.open-meteo.com/v1/elevation?latitude=%.4f&longitude=%.4f", lat, lon);
                        java.net.URL url = new java.net.URL(elevUrlStr);
                        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        conn.setConnectTimeout(5000);
                        conn.setReadTimeout(5000);
                        if (conn.getResponseCode() == 200) {
                            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                                StringBuilder sb = new StringBuilder();
                                String line;
                                while ((line = reader.readLine()) != null) sb.append(line);
                                com.fasterxml.jackson.databind.JsonNode root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(sb.toString());
                                if (root.has("elevation") && root.get("elevation").isArray() && !root.get("elevation").isEmpty()) {
                                    alt = root.get("elevation").get(0).asDouble();
                                }
                            }
                        }
                    } catch (Exception ignored) {}

                    final double finalLat = lat;
                    final double finalLon = lon;
                    final double finalAlt = alt;

                    javafx.application.Platform.runLater(() -> {
                        if (pane.altField != null) pane.altField.setText(String.format(Locale.US, "%.0f", finalAlt));
                        applyBioclimaticAdaptation(pane, finalLat, finalLon, finalAlt);
                        pane.geoStatusLabel.setText("🟢 GIS applied for Lat: " + String.format(Locale.US, "%.4f", finalLat)
                                + "°, Lon: " + String.format(Locale.US, "%.4f", finalLon)
                                + "°, Alt: " + (int) finalAlt + "m");
                        pane.geoStatusLabel.setStyle("-fx-text-fill: #4ade80; -fx-font-size: 10px;");
                        pane.regenerateAndRepaint();
                    });
                    return;
                }

                String geoUrlStr = "https://geocoding-api.open-meteo.com/v1/search?name="
                        + java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8)
                        + "&count=1&language=en";

                java.net.URL url = new java.net.URL(geoUrlStr);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() == 200) {
                    try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) sb.append(line);

                        com.fasterxml.jackson.databind.JsonNode root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(sb.toString());
                        if (root.has("results") && !root.get("results").isEmpty()) {
                            com.fasterxml.jackson.databind.JsonNode loc = root.get("results").get(0);
                            String name = loc.get("name").asText();
                            double lat = loc.get("latitude").asDouble();
                            double lon = loc.get("longitude").asDouble();
                            double alt = loc.has("elevation") && !loc.get("elevation").isNull() ? loc.get("elevation").asDouble() : 0.0;

                            javafx.application.Platform.runLater(() -> {
                                pane.latField.setText(String.format(Locale.US, "%.4f", lat));
                                pane.lonField.setText(String.format(Locale.US, "%.4f", lon));
                                if (pane.altField != null) pane.altField.setText(String.format(Locale.US, "%.0f", alt));
                                if (pane.cityNameField != null) pane.cityNameField.setText(name);
                                applyBioclimaticAdaptation(pane, lat, lon, alt);
                                pane.geoStatusLabel.setText("🟢 " + name + " (Lat: " + String.format(Locale.US, "%.4f", lat)
                                        + "°, Lon: " + String.format(Locale.US, "%.4f", lon)
                                        + "°, Alt: " + (int) alt + "m)");
                                pane.geoStatusLabel.setStyle("-fx-text-fill: #4ade80; -fx-font-size: 10px;");
                                pane.regenerateAndRepaint();
                            });
                            return;
                        }
                    }
                }
                javafx.application.Platform.runLater(() -> {
                    pane.geoStatusLabel.setText("⚠ City \"" + query + "\" not found.");
                    pane.geoStatusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 10px;");
                });
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    pane.geoStatusLabel.setText("⚠ Geocoding error : " + ex.getMessage());
                    pane.geoStatusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 10px;");
                });
            }
        }).start();
    }

    static void applyBioclimaticAdaptation(WorldEditorPane pane, double lat, double lon, double alt) {
        double baseSeaLevelTemp = 20.0 * (1.0 - Math.abs(lat) / 90.0);
        double adjustedTemp = baseSeaLevelTemp - (alt * 0.0065);
        double annualRainfall = Math.abs(lat) < 23.5 ? 2000.0 : (Math.abs(lat) > 60 ? 300.0 : (alt > 2000 ? 400.0 : 700.0));

        org.swarmforge.core.domain.BioclimaticZone zone = org.swarmforge.core.domain.BioclimaticZone.classify(
            lat, adjustedTemp, annualRainfall
        );

        if (pane.lblBioclimaticZoneBadge != null) {
            pane.lblBioclimaticZoneBadge.setText(zone.getDisplayName() + String.format(Locale.US, " (%dm)", (int) alt));
        }

        switch (zone) {
            case ARID_DESERT -> {
                if (pane.comboTreeSpecies != null) pane.comboTreeSpecies.getSelectionModel().select(3);
                if (pane.sandSpinner != null) pane.sandSpinner.getValueFactory().setValue(80);
                if (pane.stoneSpinner != null) pane.stoneSpinner.getValueFactory().setValue(15);
                if (pane.earthSpinner != null) pane.earthSpinner.getValueFactory().setValue(5);
                if (pane.claySpinner != null) pane.claySpinner.getValueFactory().setValue(0);
                if (pane.organicSpinner != null) pane.organicSpinner.getValueFactory().setValue(0);
                if (pane.baseHumiditySlider != null) pane.baseHumiditySlider.setValue(0.12);
                if (pane.waterTableDepthSlider != null) pane.waterTableDepthSlider.setValue(3500.0);
                if (pane.riverCheck != null) pane.riverCheck.setSelected(false);
                if (pane.staticPoolsSlider != null) pane.staticPoolsSlider.setValue(0);
                if (pane.seedGrassCheck != null) pane.seedGrassCheck.setSelected(true);
                if (pane.aphidPlantCheck != null) pane.aphidPlantCheck.setSelected(false);
                if (pane.fungusFoliageCheck != null) pane.fungusFoliageCheck.setSelected(false);
                if (pane.pineLitterCheck != null) pane.pineLitterCheck.setSelected(false);
                if (pane.mossCheck != null) pane.mossCheck.setSelected(false);
                if (pane.edibleDensitySlider != null) pane.edibleDensitySlider.setValue(15.0);
                if (pane.leafLitterSlider != null) pane.leafLitterSlider.setValue(5.0);
            }
            case TROPICAL_RAINFOREST -> {
                if (pane.comboTreeSpecies != null) pane.comboTreeSpecies.getSelectionModel().select(2);
                if (pane.earthSpinner != null) pane.earthSpinner.getValueFactory().setValue(45);
                if (pane.claySpinner != null) pane.claySpinner.getValueFactory().setValue(40);
                if (pane.sandSpinner != null) pane.sandSpinner.getValueFactory().setValue(10);
                if (pane.stoneSpinner != null) pane.stoneSpinner.getValueFactory().setValue(5);
                if (pane.organicSpinner != null) pane.organicSpinner.getValueFactory().setValue(15);
                if (pane.baseHumiditySlider != null) pane.baseHumiditySlider.setValue(0.85);
                if (pane.riverCheck != null) pane.riverCheck.setSelected(true);
                if (pane.riverWidthSlider != null) pane.riverWidthSlider.setValue(180.0);
                if (pane.staticPoolsSlider != null) pane.staticPoolsSlider.setValue(4);
                if (pane.waterTableDepthSlider != null) pane.waterTableDepthSlider.setValue(600.0);
                if (pane.fungusFoliageCheck != null) pane.fungusFoliageCheck.setSelected(true);
                if (pane.nectarFlowersCheck != null) pane.nectarFlowersCheck.setSelected(true);
                if (pane.aphidPlantCheck != null) pane.aphidPlantCheck.setSelected(true);
                if (pane.fernObstacleCheck != null) pane.fernObstacleCheck.setSelected(true);
                if (pane.edibleDensitySlider != null) pane.edibleDensitySlider.setValue(75.0);
                if (pane.leafLitterSlider != null) pane.leafLitterSlider.setValue(65.0);
            }
            case ARCTIC_TUNDRA -> {
                if (pane.comboTreeSpecies != null) pane.comboTreeSpecies.getSelectionModel().select(4);
                if (pane.stoneSpinner != null) pane.stoneSpinner.getValueFactory().setValue(50);
                if (pane.earthSpinner != null) pane.earthSpinner.getValueFactory().setValue(30);
                if (pane.sandSpinner != null) pane.sandSpinner.getValueFactory().setValue(10);
                if (pane.claySpinner != null) pane.claySpinner.getValueFactory().setValue(10);
                if (pane.organicSpinner != null) pane.organicSpinner.getValueFactory().setValue(5);
                if (pane.baseHumiditySlider != null) pane.baseHumiditySlider.setValue(0.30);
                if (pane.waterTableDepthSlider != null) pane.waterTableDepthSlider.setValue(1000.0);
                if (pane.riverCheck != null) pane.riverCheck.setSelected(alt < 3500);
                if (pane.staticPoolsSlider != null) pane.staticPoolsSlider.setValue(1);
                if (pane.mossCheck != null) pane.mossCheck.setSelected(true);
                if (pane.pineLitterCheck != null) pane.pineLitterCheck.setSelected(true);
                if (pane.fungusFoliageCheck != null) pane.fungusFoliageCheck.setSelected(false);
                if (pane.aphidPlantCheck != null) pane.aphidPlantCheck.setSelected(false);
                if (pane.edibleDensitySlider != null) pane.edibleDensitySlider.setValue(20.0);
                if (pane.leafLitterSlider != null) pane.leafLitterSlider.setValue(25.0);
            }
            case MEDITERRANEAN -> {
                if (pane.comboTreeSpecies != null) pane.comboTreeSpecies.getSelectionModel().select(1);
                if (pane.earthSpinner != null) pane.earthSpinner.getValueFactory().setValue(35);
                if (pane.stoneSpinner != null) pane.stoneSpinner.getValueFactory().setValue(25);
                if (pane.sandSpinner != null) pane.sandSpinner.getValueFactory().setValue(25);
                if (pane.claySpinner != null) pane.claySpinner.getValueFactory().setValue(15);
                if (pane.organicSpinner != null) pane.organicSpinner.getValueFactory().setValue(5);
                if (pane.baseHumiditySlider != null) pane.baseHumiditySlider.setValue(0.35);
                if (pane.waterTableDepthSlider != null) pane.waterTableDepthSlider.setValue(1800.0);
                if (pane.riverCheck != null) pane.riverCheck.setSelected(true);
                if (pane.staticPoolsSlider != null) pane.staticPoolsSlider.setValue(1);
                if (pane.seedGrassCheck != null) pane.seedGrassCheck.setSelected(true);
                if (pane.nectarFlowersCheck != null) pane.nectarFlowersCheck.setSelected(true);
                if (pane.edibleDensitySlider != null) pane.edibleDensitySlider.setValue(40.0);
            }
            case BOREAL_TAIGA -> {
                if (pane.comboTreeSpecies != null) pane.comboTreeSpecies.getSelectionModel().select(1);
                if (pane.earthSpinner != null) pane.earthSpinner.getValueFactory().setValue(40);
                if (pane.stoneSpinner != null) pane.stoneSpinner.getValueFactory().setValue(25);
                if (pane.sandSpinner != null) pane.sandSpinner.getValueFactory().setValue(20);
                if (pane.claySpinner != null) pane.claySpinner.getValueFactory().setValue(15);
                if (pane.organicSpinner != null) pane.organicSpinner.getValueFactory().setValue(10);
                if (pane.baseHumiditySlider != null) pane.baseHumiditySlider.setValue(0.55);
                if (pane.waterTableDepthSlider != null) pane.waterTableDepthSlider.setValue(1200.0);
                if (pane.pineLitterCheck != null) pane.pineLitterCheck.setSelected(true);
                if (pane.mossCheck != null) pane.mossCheck.setSelected(true);
                if (pane.riverCheck != null) pane.riverCheck.setSelected(true);
                if (pane.edibleDensitySlider != null) pane.edibleDensitySlider.setValue(35.0);
                if (pane.leafLitterSlider != null) pane.leafLitterSlider.setValue(50.0);
            }
            default -> {
                if (pane.comboTreeSpecies != null) pane.comboTreeSpecies.getSelectionModel().select(0);
                if (pane.earthSpinner != null) pane.earthSpinner.getValueFactory().setValue(50);
                if (pane.sandSpinner != null) pane.sandSpinner.getValueFactory().setValue(20);
                if (pane.claySpinner != null) pane.claySpinner.getValueFactory().setValue(20);
                if (pane.stoneSpinner != null) pane.stoneSpinner.getValueFactory().setValue(10);
                if (pane.organicSpinner != null) pane.organicSpinner.getValueFactory().setValue(10);
                if (pane.baseHumiditySlider != null) pane.baseHumiditySlider.setValue(0.45);
                if (pane.waterTableDepthSlider != null) pane.waterTableDepthSlider.setValue(1500.0);
                if (pane.aphidPlantCheck != null) pane.aphidPlantCheck.setSelected(true);
                if (pane.nectarFlowersCheck != null) pane.nectarFlowersCheck.setSelected(true);
                if (pane.seedGrassCheck != null) pane.seedGrassCheck.setSelected(true);
                if (pane.riverCheck != null) pane.riverCheck.setSelected(true);
                if (pane.staticPoolsSlider != null) pane.staticPoolsSlider.setValue(2);
                if (pane.edibleDensitySlider != null) pane.edibleDensitySlider.setValue(45.0);
                if (pane.leafLitterSlider != null) pane.leafLitterSlider.setValue(40.0);
            }
        }
        updateEcologicalCompatibilityScores(pane);
    }

    static void handleAdaptWorldToSpecies(WorldEditorPane pane) {
        int idx = pane.speciesAdaptCombo.getSelectionModel().getSelectedIndex();
        switch (idx) {
            case 0 -> {
                pane.surfaceSizeSlider.setValue(25.0); pane.depthSlider.setValue(3.0);
                pane.nectarFlowersCheck.setSelected(true); pane.edibleDensitySlider.setValue(80);
                pane.staticPoolsSlider.setValue(2);
            }
            case 1 -> {
                String pKey = "Tropical Rainforest (Manaus, BR)";
                if (pane.presetManager.contains(pKey)) {
                    pane.presetsCombo.getSelectionModel().select(pKey);
                    pane.loadConfiguration(pane.presetManager.get(pKey));
                } else {
                    pane.surfaceSizeSlider.setValue(25.0); pane.depthSlider.setValue(3.0); pane.baseHumiditySlider.setValue(0.85);
                    pane.earthSpinner.getValueFactory().setValue(45); pane.claySpinner.getValueFactory().setValue(40);
                    pane.comboTreeSpecies.getSelectionModel().select(2);
                    pane.fungusFoliageCheck.setSelected(true); pane.aphidPlantCheck.setSelected(true);
                }
            }
            case 2 -> {
                pane.surfaceSizeSlider.setValue(25.0); pane.depthSlider.setValue(3.0);
                pane.comboTreeSpecies.getSelectionModel().select(6);
                pane.deadWoodPctSpinner.getValueFactory().setValue(50); pane.oakPctSpinner.getValueFactory().setValue(40);
                pane.hollowLogsSlider.setValue(6); pane.treeCountSlider.setValue(8);
            }
            case 3 -> {
                String pKey = "Arid Desert (Erg Chebbi, MA)";
                if (pane.presetManager.contains(pKey)) {
                    pane.presetsCombo.getSelectionModel().select(pKey);
                    pane.loadConfiguration(pane.presetManager.get(pKey));
                } else {
                    pane.surfaceSizeSlider.setValue(25.0); pane.depthSlider.setValue(3.0); pane.baseHumiditySlider.setValue(0.12);
                    pane.sandSpinner.getValueFactory().setValue(80); pane.stoneSpinner.getValueFactory().setValue(15);
                    pane.comboTreeSpecies.getSelectionModel().select(3);
                    pane.riverCheck.setSelected(false);
                }
            }
            case 4 -> {
                String pKey = "Permafrost Tundra (Svalbard, NO)";
                if (pane.presetManager.contains(pKey)) {
                    pane.presetsCombo.getSelectionModel().select(pKey);
                    pane.loadConfiguration(pane.presetManager.get(pKey));
                } else {
                    pane.surfaceSizeSlider.setValue(25.0); pane.depthSlider.setValue(3.0); pane.baseHumiditySlider.setValue(0.55);
                    pane.earthSpinner.getValueFactory().setValue(40); pane.stoneSpinner.getValueFactory().setValue(25);
                    pane.comboTreeSpecies.getSelectionModel().select(1);
                    pane.pineLitterCheck.setSelected(true); pane.mossCheck.setSelected(true);
                }
            }
            case 5 -> {
                String pKey = "Temperate Deciduous (Fontainebleau, FR)";
                if (pane.presetManager.contains(pKey)) {
                    pane.presetsCombo.getSelectionModel().select(pKey);
                    pane.loadConfiguration(pane.presetManager.get(pKey));
                } else {
                    pane.surfaceSizeSlider.setValue(25.0); pane.depthSlider.setValue(3.0); pane.baseHumiditySlider.setValue(0.45);
                    pane.earthSpinner.getValueFactory().setValue(50); pane.sandSpinner.getValueFactory().setValue(20);
                    pane.comboTreeSpecies.getSelectionModel().select(0);
                    pane.aphidPlantCheck.setSelected(true); pane.nectarFlowersCheck.setSelected(true);
                }
            }
            case 6 -> {
                String pKey = "Mediterranean Shrubland (Corsica, FR)";
                if (pane.presetManager.contains(pKey)) {
                    pane.presetsCombo.getSelectionModel().select(pKey);
                    pane.loadConfiguration(pane.presetManager.get(pKey));
                } else {
                    pane.surfaceSizeSlider.setValue(25.0); pane.depthSlider.setValue(3.0); pane.baseHumiditySlider.setValue(0.30);
                    pane.sandSpinner.getValueFactory().setValue(35); pane.earthSpinner.getValueFactory().setValue(45);
                    pane.seedGrassCheck.setSelected(true);
                }
            }
            case 7 -> {
                String pKey = "Acacia Savanna (Serengeti, TZ)";
                if (pane.presetManager.contains(pKey)) {
                    pane.presetsCombo.getSelectionModel().select(pKey);
                    pane.loadConfiguration(pane.presetManager.get(pKey));
                } else {
                    pane.comboTreeSpecies.getSelectionModel().select(2);
                    pane.acaciaPctSpinner.getValueFactory().setValue(80); pane.nectarFlowersCheck.setSelected(true);
                }
            }
            case 8 -> {
                pane.surfaceSizeSlider.setValue(25.0); pane.depthSlider.setValue(3.0); pane.voidDensitySlider.setValue(0.20);
                pane.baseHumiditySlider.setValue(0.60); pane.deadWoodPctSpinner.getValueFactory().setValue(60);
                pane.hollowLogsSlider.setValue(7);
            }
            case 9 -> {
                pane.surfaceSizeSlider.setValue(25.0); pane.depthSlider.setValue(3.0); pane.baseHumiditySlider.setValue(0.70);
                pane.earthSpinner.getValueFactory().setValue(60); pane.claySpinner.getValueFactory().setValue(25);
                pane.waterTableDepthSlider.setValue(10); pane.staticPoolsSlider.setValue(3);
            }
        }
        pane.regenerateAndRepaint();
        if (pane.lblAdaptStatus != null) {
            pane.lblAdaptStatus.setText("🟢 Ecosystem successfully adapted to: " + pane.speciesAdaptCombo.getValue());
        }
    }

    static void updateTreeSpeciesSpinnersFromPreset(WorldEditorPane pane) {
        int idx = pane.comboTreeSpecies.getSelectionModel().getSelectedIndex();
        switch (idx) {
            case 0 -> { pane.oakPctSpinner.getValueFactory().setValue(70); pane.pinePctSpinner.getValueFactory().setValue(10); pane.acaciaPctSpinner.getValueFactory().setValue(0); pane.cactusPctSpinner.getValueFactory().setValue(0); pane.birchPctSpinner.getValueFactory().setValue(10); pane.bambooPctSpinner.getValueFactory().setValue(0); pane.deadWoodPctSpinner.getValueFactory().setValue(10); }
            case 1 -> { pane.oakPctSpinner.getValueFactory().setValue(10); pane.pinePctSpinner.getValueFactory().setValue(75); pane.acaciaPctSpinner.getValueFactory().setValue(0); pane.cactusPctSpinner.getValueFactory().setValue(0); pane.birchPctSpinner.getValueFactory().setValue(5); pane.bambooPctSpinner.getValueFactory().setValue(0); pane.deadWoodPctSpinner.getValueFactory().setValue(10); }
            case 2 -> { pane.oakPctSpinner.getValueFactory().setValue(0); pane.pinePctSpinner.getValueFactory().setValue(0); pane.acaciaPctSpinner.getValueFactory().setValue(90); pane.cactusPctSpinner.getValueFactory().setValue(0); pane.birchPctSpinner.getValueFactory().setValue(0); pane.bambooPctSpinner.getValueFactory().setValue(0); pane.deadWoodPctSpinner.getValueFactory().setValue(10); }
            case 3 -> { pane.oakPctSpinner.getValueFactory().setValue(0); pane.pinePctSpinner.getValueFactory().setValue(0); pane.acaciaPctSpinner.getValueFactory().setValue(15); pane.cactusPctSpinner.getValueFactory().setValue(75); pane.birchPctSpinner.getValueFactory().setValue(0); pane.bambooPctSpinner.getValueFactory().setValue(0); pane.deadWoodPctSpinner.getValueFactory().setValue(10); }
            case 4 -> { pane.oakPctSpinner.getValueFactory().setValue(15); pane.pinePctSpinner.getValueFactory().setValue(15); pane.acaciaPctSpinner.getValueFactory().setValue(0); pane.cactusPctSpinner.getValueFactory().setValue(0); pane.birchPctSpinner.getValueFactory().setValue(60); pane.bambooPctSpinner.getValueFactory().setValue(0); pane.deadWoodPctSpinner.getValueFactory().setValue(10); }
            case 5 -> { pane.oakPctSpinner.getValueFactory().setValue(5); pane.pinePctSpinner.getValueFactory().setValue(0); pane.acaciaPctSpinner.getValueFactory().setValue(0); pane.cactusPctSpinner.getValueFactory().setValue(0); pane.birchPctSpinner.getValueFactory().setValue(5); pane.bambooPctSpinner.getValueFactory().setValue(80); pane.deadWoodPctSpinner.getValueFactory().setValue(10); }
            case 6 -> { pane.oakPctSpinner.getValueFactory().setValue(10); pane.pinePctSpinner.getValueFactory().setValue(10); pane.acaciaPctSpinner.getValueFactory().setValue(0); pane.cactusPctSpinner.getValueFactory().setValue(0); pane.birchPctSpinner.getValueFactory().setValue(10); pane.bambooPctSpinner.getValueFactory().setValue(0); pane.deadWoodPctSpinner.getValueFactory().setValue(70); }
        }
        updateEcologicalCompatibilityScores(pane);
        pane.repaintAllViews();
    }

    static void updateEcologicalCompatibilityScores(WorldEditorPane pane) {
        int oak = pane.oakPctSpinner != null ? pane.oakPctSpinner.getValue() : 45;
        int pine = pane.pinePctSpinner != null ? pane.pinePctSpinner.getValue() : 20;
        int acacia = pane.acaciaPctSpinner != null ? pane.acaciaPctSpinner.getValue() : 10;
        int birch = pane.birchPctSpinner != null ? pane.birchPctSpinner.getValue() : 10;
        int cactus = pane.cactusPctSpinner != null ? pane.cactusPctSpinner.getValue() : 0;
        int deadWood = pane.deadWoodPctSpinner != null ? pane.deadWoodPctSpinner.getValue() : 10;

        double lat = 48.8;
        if (pane.latField != null) {
            try { lat = Double.parseDouble(pane.latField.getText().trim()); } catch (Exception ignored) {}
        }
        double baseHum = pane.baseHumiditySlider != null ? pane.baseHumiditySlider.getValue() : 0.35;
        org.swarmforge.core.domain.BioclimaticZone zone = org.swarmforge.core.domain.BioclimaticZone.classify(lat, 20.0 * (1.0 - Math.abs(lat) / 90.0), baseHum * 2000.0);
        if (pane.lblBioclimaticZoneBadge != null) {
            pane.lblBioclimaticZoneBadge.setText(zone.getDisplayName());
        }

        boolean seedGrass = pane.seedGrassCheck != null && pane.seedGrassCheck.isSelected();
        boolean nectarFlowers = pane.nectarFlowersCheck != null && pane.nectarFlowersCheck.isSelected();
        boolean aphidPlant = pane.aphidPlantCheck != null && pane.aphidPlantCheck.isSelected();
        boolean fungus = pane.fungusFoliageCheck != null && pane.fungusFoliageCheck.isSelected();

        int earth = pane.earthSpinner != null ? pane.earthSpinner.getValue() : 50;
        int sand = pane.sandSpinner != null ? pane.sandSpinner.getValue() : 20;

        int attaScore = Math.min(100, (fungus ? 30 : 0) + oak + birch + acacia / 2);
        int aphidScore = Math.min(100, (aphidPlant ? 25 : 0) + pine + birch + oak / 2);
        int woodScore = Math.min(100, deadWood + oak + pine / 2);
        int acaciaScore = Math.min(100, (nectarFlowers ? 20 : 0) + acacia + deadWood / 2);
        int cactusScore = Math.min(100, cactus + acacia / 2 + (sand > 40 ? 20 : 0));
        int pogoScore = Math.min(100, (seedGrass ? 35 : 0) + (sand > 30 ? 25 : 10) + birch / 2 + 20);
        int termiteScore = Math.min(100, deadWood + (earth > 40 ? 25 : 10) + oak / 2 + 15);
        int apisScore = Math.min(100, (nectarFlowers ? 40 : 10) + oak / 2 + birch / 2 + acacia / 2 + 15);
        int vespulaScore = Math.min(100, deadWood / 2 + oak / 2 + pine / 2 + (nectarFlowers ? 20 : 0) + 30);
        int solenopsisScore = Math.min(100, (earth > 40 ? 30 : 10) + (baseHum > 0.4 ? 30 : 10) + 30);

        if (pane.lblAttaCompatScore != null) pane.lblAttaCompatScore.setText(attaScore + "% (Foliage / Fungus)");
        if (pane.lblAphidCompatScore != null) pane.lblAphidCompatScore.setText(aphidScore + "% (Aphids / Honeydew)");
        if (pane.lblWoodNestCompatScore != null) pane.lblWoodNestCompatScore.setText(woodScore + "% (Wood Excavation)");
        if (pane.lblAcaciaAntCompatScore != null) pane.lblAcaciaAntCompatScore.setText(acaciaScore + "% (Acacia Nectaries)");
        if (pane.lblCactusAntCompatScore != null) pane.lblCactusAntCompatScore.setText(cactusScore + "% (Arid Soil / Cactus)");
        if (pane.lblPogonomyrmexCompatScore != null) pane.lblPogonomyrmexCompatScore.setText(pogoScore + "% (Grasses / Seeds)");
        if (pane.lblTermiteCompatScore != null) pane.lblTermiteCompatScore.setText(termiteScore + "% (Dead Wood / Cellulose)");
        if (pane.lblApisCompatScore != null) pane.lblApisCompatScore.setText(apisScore + "% (Nectar & Pollen)");
        if (pane.lblVespulaCompatScore != null) pane.lblVespulaCompatScore.setText(vespulaScore + "% (Hunting / Paper Nest)");
        if (pane.lblSolenopsisCompatScore != null) pane.lblSolenopsisCompatScore.setText(solenopsisScore + "% (Soil Mounds / Carnivorous)");
    }
}
