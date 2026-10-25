/*
 * SwarmForge - Ant Colony & Insect Simulation Platform
 * Copyright (c) 2024-2026 Silvère Martin-Michiellot & SwarmForge Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.swarmforge.client.ui.weather;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.swarmforge.client.ui.control.RealWeatherApiClient;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.NotificationOverlay;

/**
 * Geographic and Atmospheric Physical Configuration Card for the Weather Editor.
 * Includes Open-Meteo real-world geocoding search, latitude/longitude solar photoperiod calculation,
 * barometric altitude lapse rate, soil thermal inertia, and vegetation biome estimation.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class WeatherGeographicAtmospherePane extends VBox {

    private static final String[] MONTH_KEYS = {
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    private static final int[] MONTH_MID_DAYS = { 15, 45, 75, 105, 135, 165, 195, 225, 255, 285, 315, 345 };

    private final RealWeatherApiClient weatherApiClient = new RealWeatherApiClient();

    private final double[] tempAvg;
    private final double[] rainAvg;
    private final Runnable onDataChanged;
    private final Runnable onClimateLoaded;

    // Controls
    private TextField citySearchField;
    private Label geoStatusLabel;
    private Label vegCoverLabel;
    private Spinner<Double> latSpinner;
    private Spinner<Double> lonSpinner;
    private Spinner<Double> altSpinner;
    private Spinner<Double> pressureSpinner;
    private ComboBox<String> windDirCombo;
    private Spinner<Double> soilInertiaSpinner;
    private Spinner<Double> depthAttenSpinner;

    // Photoperiod
    private final double[] daylightHours = new double[12];
    private final Label[] daylightLabels = new Label[12];

    public WeatherGeographicAtmospherePane(
            double[] tempAvg,
            double[] rainAvg,
            Runnable onDataChanged,
            Runnable onClimateLoaded) {
        this.tempAvg = tempAvg;
        this.rainAvg = rainAvg;
        this.onDataChanged = onDataChanged;
        this.onClimateLoaded = onClimateLoaded;

        setSpacing(10);
        buildUI();
    }

    private void buildUI() {
        getChildren().addAll(createGeographicSection(), createAtmosphereSoilSection());
        updatePhotoperiod();
        updateVegetationEstimate();
    }

    private VBox createGeographicSection() {
        I18nManager i18n = I18nManager.getInstance();
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));
        container.getStyleClass().add("card-pane");

        Label title = new Label();
        title.textProperty().bind(i18n.createStringBinding("weather.geo.title"));
        title.getStyleClass().add("card-title");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Row 1: Search Bar
        HBox searchRow = new HBox(10);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        Label lblSearch = new Label();
        lblSearch.textProperty().bind(i18n.createStringBinding("weather.geo.search_label"));
        lblSearch.setStyle("-fx-font-weight:bold;");

        citySearchField = new TextField("Paris");
        citySearchField.promptTextProperty().bind(i18n.createStringBinding("weather.geo.search_prompt"));
        citySearchField.setPrefWidth(220);
        citySearchField.tooltipProperty().bind(i18n.createTooltipBinding("weather.geo.city.tt"));
        citySearchField.setOnAction(e -> fetchRealWeather(citySearchField.getText()));

        Button btnFetch = new Button();
        btnFetch.getStyleClass().add("btn-primary");
        btnFetch.textProperty().bind(i18n.createStringBinding("weather.geo.search_btn"));
        btnFetch.tooltipProperty().bind(i18n.createTooltipBinding("weather.geo.fetch.tt"));
        btnFetch.setOnAction(e -> fetchRealWeather(citySearchField.getText()));

        geoStatusLabel = new Label(i18n.get("weather.hint.city"));
        geoStatusLabel.setStyle("-fx-text-fill:#aaa;-fx-font-size:11;");

        searchRow.getChildren().addAll(lblSearch, citySearchField, btnFetch, geoStatusLabel);

        // Row 2: Latitude, Longitude, Altitude & Vegetation Cover
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);

        Label lblLat = new Label();
        lblLat.textProperty().bind(i18n.createStringBinding("weather.geo.lat"));
        lblLat.setStyle("-fx-font-weight:bold;");
        latSpinner = new Spinner<>(-90.0, 90.0, 48.8, 0.5);
        latSpinner.setEditable(true);
        latSpinner.setPrefWidth(100);
        latSpinner.tooltipProperty().bind(i18n.createTooltipBinding("weather.geo.lat.tt"));
        latSpinner.valueProperty().addListener((o, a, b) -> {
            updatePhotoperiod();
            updateAltitudeLapseRate();
            updateVegetationEstimate();
            if (onDataChanged != null) onDataChanged.run();
        });

        Label lblLon = new Label();
        lblLon.textProperty().bind(i18n.createStringBinding("weather.geo.lon"));
        lblLon.setStyle("-fx-font-weight:bold;");
        lonSpinner = new Spinner<>(-180.0, 180.0, 2.35, 0.5);
        lonSpinner.setEditable(true);
        lonSpinner.setPrefWidth(100);
        lonSpinner.tooltipProperty().bind(i18n.createTooltipBinding("weather.geo.lon.tt"));
        lonSpinner.valueProperty().addListener((o, a, b) -> {
            if (onDataChanged != null) onDataChanged.run();
        });

        Label lblAlt = new Label();
        lblAlt.textProperty().bind(i18n.createStringBinding("weather.geo.alt"));
        lblAlt.setStyle("-fx-font-weight:bold;");
        altSpinner = new Spinner<>(0.0, 4000.0, 100.0, 50.0);
        altSpinner.setEditable(true);
        altSpinner.setPrefWidth(110);
        altSpinner.tooltipProperty().bind(i18n.createTooltipBinding("weather.geo.alt.tt"));
        altSpinner.valueProperty().addListener((o, a, b) -> {
            updateAltitudeLapseRate();
            if (onDataChanged != null) onDataChanged.run();
        });

        Label lblVegTitle = new Label();
        lblVegTitle.textProperty().bind(i18n.createStringBinding("weather.geo.veg_label"));
        lblVegTitle.setStyle("-fx-font-weight:bold;");

        vegCoverLabel = new Label("🌳 Temperate Deciduous Forest");
        vegCoverLabel.setStyle("-fx-text-fill:#28a745;-fx-font-weight:bold;");

        grid.add(lblLat, 0, 0);
        grid.add(latSpinner, 1, 0);
        grid.add(lblLon, 2, 0);
        grid.add(lonSpinner, 3, 0);
        grid.add(lblAlt, 4, 0);
        grid.add(altSpinner, 5, 0);
        grid.add(lblVegTitle, 6, 0);
        grid.add(vegCoverLabel, 7, 0);

        // Row 3: Daylight Photoperiod Table
        VBox photoBox = new VBox(4);
        Label photoTitle = new Label();
        photoTitle.textProperty().bind(i18n.createStringBinding("weather.geo.photoperiod_title"));
        photoTitle.setStyle("-fx-font-weight:bold;-fx-font-size:11;");

        HBox photoGrid = new HBox(8);
        photoGrid.setAlignment(Pos.CENTER_LEFT);
        for (int m = 0; m < 12; m++) {
            VBox col = new VBox(2);
            col.setAlignment(Pos.CENTER);
            Label ml = new Label(MONTH_KEYS[m]);
            ml.setStyle("-fx-font-size:10;-fx-text-fill:#888;");
            daylightLabels[m] = new Label("12.0 h");
            daylightLabels[m].setStyle("-fx-font-size:11;-fx-font-weight:bold;");
            col.getChildren().addAll(ml, daylightLabels[m]);
            photoGrid.getChildren().add(col);
        }
        photoBox.getChildren().addAll(photoTitle, photoGrid);

        container.getChildren().addAll(title, searchRow, grid, new Separator(), photoBox);
        return container;
    }

    private VBox createAtmosphereSoilSection() {
        I18nManager i18n = I18nManager.getInstance();
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));
        container.getStyleClass().add("card-pane");

        Label title = new Label();
        title.textProperty().bind(i18n.createStringBinding("weather.atmo.title"));
        title.getStyleClass().add("card-title");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);

        Label lblP = new Label();
        lblP.textProperty().bind(i18n.createStringBinding("weather.atmo.pressure"));
        lblP.setStyle("-fx-font-weight:bold;");
        pressureSpinner = new Spinner<>(700.0, 1100.0, 1013.25, 1.0);
        pressureSpinner.setEditable(true);
        pressureSpinner.setPrefWidth(100);
        pressureSpinner.tooltipProperty().bind(i18n.createTooltipBinding("weather.atmo.pressure.tt"));
        pressureSpinner.valueProperty().addListener((o, a, b) -> {
            if (onDataChanged != null) onDataChanged.run();
        });

        Label lblWd = new Label();
        lblWd.textProperty().bind(i18n.createStringBinding("weather.atmo.wind_dir"));
        lblWd.setStyle("-fx-font-weight:bold;");
        windDirCombo = new ComboBox<>(FXCollections.observableArrayList(
                "N", "NE", "E", "SE", "S", "SW", "W", "NW"
        ));
        windDirCombo.setValue("SW");
        windDirCombo.setPrefWidth(90);
        windDirCombo.tooltipProperty().bind(i18n.createTooltipBinding("weather.atmo.wind_dir.tt"));
        windDirCombo.valueProperty().addListener((o, a, b) -> {
            if (onDataChanged != null) onDataChanged.run();
        });

        Label lblInertia = new Label();
        lblInertia.textProperty().bind(i18n.createStringBinding("weather.atmo.soil_inertia"));
        lblInertia.setStyle("-fx-font-weight:bold;");
        soilInertiaSpinner = new Spinner<>(0.5, 30.0, 3.0, 0.5);
        soilInertiaSpinner.setEditable(true);
        soilInertiaSpinner.setPrefWidth(90);
        soilInertiaSpinner.tooltipProperty().bind(i18n.createTooltipBinding("weather.atmo.soil_inertia.tt"));
        soilInertiaSpinner.valueProperty().addListener((o, a, b) -> {
            if (onDataChanged != null) onDataChanged.run();
        });

        Label lblDepth = new Label();
        lblDepth.textProperty().bind(i18n.createStringBinding("weather.atmo.depth_atten"));
        lblDepth.setStyle("-fx-font-weight:bold;");
        depthAttenSpinner = new Spinner<>(0.1, 1.0, 0.85, 0.05);
        depthAttenSpinner.setEditable(true);
        depthAttenSpinner.setPrefWidth(90);
        depthAttenSpinner.tooltipProperty().bind(i18n.createTooltipBinding("weather.atmo.depth_atten.tt"));
        depthAttenSpinner.valueProperty().addListener((o, a, b) -> {
            if (onDataChanged != null) onDataChanged.run();
        });

        grid.add(lblP, 0, 0);
        grid.add(pressureSpinner, 1, 0);
        grid.add(lblWd, 2, 0);
        grid.add(windDirCombo, 3, 0);
        grid.add(lblInertia, 4, 0);
        grid.add(soilInertiaSpinner, 5, 0);
        grid.add(lblDepth, 6, 0);
        grid.add(depthAttenSpinner, 7, 0);

        container.getChildren().addAll(title, grid);
        return container;
    }

    public void updatePhotoperiod() {
        double latRad = Math.toRadians(latSpinner.getValue());
        for (int m = 0; m < 12; m++) {
            int dayOfYear = MONTH_MID_DAYS[m];
            double declination = Math.toRadians(23.45 * Math.sin(Math.toRadians(360.0 / 365.0 * (dayOfYear - 81))));
            double cosHourAngle = -Math.tan(latRad) * Math.tan(declination);

            double hours;
            if (cosHourAngle >= 1.0) {
                hours = 0.0;
            } else if (cosHourAngle <= -1.0) {
                hours = 24.0;
            } else {
                hours = 24.0 * Math.acos(cosHourAngle) / Math.PI;
            }

            daylightHours[m] = hours;
            if (daylightLabels[m] != null) {
                daylightLabels[m].setText(String.format(Locale.US, "%.1f h", hours));
            }
        }
    }

    public void updateAltitudeLapseRate() {
        double altM = altSpinner.getValue();
        double p = 1013.25 * Math.pow(1.0 - 0.0065 * altM / 288.15, 5.255);
        if (pressureSpinner != null) {
            pressureSpinner.getValueFactory().setValue(Math.round(p * 10.0) / 10.0);
        }
    }

    public void updateVegetationEstimate() {
        if (vegCoverLabel == null) return;
        double lat = Math.abs(latSpinner.getValue());
        double meanTemp = 0;
        double totalRain = 0;
        for (int m = 0; m < 12; m++) {
            meanTemp += tempAvg[m];
            totalRain += rainAvg[m];
        }
        meanTemp /= 12.0;

        String veg;
        String color;
        if (lat > 65 || meanTemp < -5) {
            veg = "❄️ Arctic Tundra & Permafrost";
            color = "#a0d2eb";
        } else if (lat > 50 && meanTemp < 10) {
            veg = "🌲 Boreal Taiga (Coniferous Forest)";
            color = "#2e8b57";
        } else if (totalRain < 250) {
            veg = "🏜️ Arid Desert / Xeric Shrubland";
            color = "#d4a373";
        } else if (totalRain < 600 && meanTemp > 18) {
            veg = "🌾 Savanna & Dry Grassland";
            color = "#e9c46a";
        } else if (meanTemp > 20 && totalRain > 1500) {
            veg = "🌴 Tropical Rainforest (High Biodiversity)";
            color = "#2a9d8f";
        } else if (meanTemp > 14 && totalRain > 800) {
            veg = "🌿 Subtropical Wet Sclerophyll";
            color = "#38b000";
        } else {
            veg = "🌳 Temperate Deciduous Forest";
            color = "#28a745";
        }

        vegCoverLabel.setText(veg);
        vegCoverLabel.setStyle("-fx-text-fill:" + color + ";-fx-font-weight:bold;");
    }

    public void fetchRealWeather(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) return;
        I18nManager i18n = I18nManager.getInstance();
        geoStatusLabel.setText(i18n.get("weather.geo.status_fetching", cityName.trim()));
        geoStatusLabel.setStyle("-fx-text-fill:#e67e22;-fx-font-size:11;");

        weatherApiClient.resolveLocation(cityName.trim(), loc -> {
            Platform.runLater(() -> {
                if (citySearchField != null) citySearchField.setText(loc.name());
                latSpinner.getValueFactory().setValue(loc.latitude());
                lonSpinner.getValueFactory().setValue(loc.longitude());
                altSpinner.getValueFactory().setValue(loc.altitude());

                new Thread(() -> {
                    try {
                        RealWeatherApiClient.AnnualClimateData climate = weatherApiClient.fetchHistoricalClimate(loc.latitude(), loc.longitude());
                        Platform.runLater(() -> {
                            if (climate != null) {
                                for (int m = 0; m < 12; m++) {
                                    tempAvg[m] = climate.tempAvg()[m];
                                    rainAvg[m] = climate.rainAvg()[m];
                                }
                            }
                            updatePhotoperiod();
                            updateAltitudeLapseRate();
                            updateVegetationEstimate();

                            if (onClimateLoaded != null) onClimateLoaded.run();

                            geoStatusLabel.setText(i18n.get("weather.geo.status_success", loc.name(), String.format(Locale.US, "%.2f", loc.latitude()), String.format(Locale.US, "%.2f", loc.longitude()), (int) loc.altitude()));
                            geoStatusLabel.setStyle("-fx-text-fill:#28a745;-fx-font-weight:bold;-fx-font-size:11;");

                            NotificationOverlay.show(this, "Real climate of " + loc.name() + " (" + String.format(Locale.US, "%.2f°N, %.2f°E, %dm", loc.latitude(), loc.longitude(), (int)loc.altitude()) + ") successfully loaded!", NotificationOverlay.NotificationType.SUCCESS);
                        });
                    } catch (Exception ex) {
                        Platform.runLater(() -> {
                            geoStatusLabel.setText(i18n.get("weather.geo.status_error", ex.getMessage()));
                            geoStatusLabel.setStyle("-fx-text-fill:#ff4757;-fx-font-size:11;");
                        });
                    }
                }).start();
            });
        }, errMsg -> {
            Platform.runLater(() -> {
                geoStatusLabel.setText(errMsg);
                geoStatusLabel.setStyle("-fx-text-fill:#ff4757;-fx-font-size:11;");
            });
        });
    }

    // Getters and Setters
    public double getLatitude() { return latSpinner != null ? latSpinner.getValue() : 48.8; }
    public double getLongitude() { return lonSpinner != null ? lonSpinner.getValue() : 2.35; }
    public double getAltitude() { return altSpinner != null ? altSpinner.getValue() : 100.0; }
    public double getBasePressure() { return pressureSpinner != null ? pressureSpinner.getValue() : 1013.25; }
    public String getWindDirection() { return windDirCombo != null ? windDirCombo.getValue() : "SW"; }
    public double getSoilInertiaDays() { return soilInertiaSpinner != null ? soilInertiaSpinner.getValue() : 3.0; }
    public double getDepthAttenuation() { return depthAttenSpinner != null ? depthAttenSpinner.getValue() : 0.85; }
    public String getVegetationCover() { return vegCoverLabel != null ? vegCoverLabel.getText() : ""; }
    public double[] getDaylightHours() { return daylightHours; }

    public void setLatitude(double val) { if (latSpinner != null) latSpinner.getValueFactory().setValue(val); }
    public void setLongitude(double val) { if (lonSpinner != null) lonSpinner.getValueFactory().setValue(val); }
    public void setAltitude(double val) { if (altSpinner != null) altSpinner.getValueFactory().setValue(val); }
    public void setBasePressure(double val) { if (pressureSpinner != null) pressureSpinner.getValueFactory().setValue(val); }
    public void setWindDirection(String dir) { if (windDirCombo != null) windDirCombo.setValue(dir); }
    public void setSoilInertiaDays(double val) { if (soilInertiaSpinner != null) soilInertiaSpinner.getValueFactory().setValue(val); }
    public void setDepthAttenuation(double val) { if (depthAttenSpinner != null) depthAttenSpinner.getValueFactory().setValue(val); }
}
