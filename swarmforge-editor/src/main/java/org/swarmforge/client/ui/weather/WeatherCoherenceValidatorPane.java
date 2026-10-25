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

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;
import org.swarmforge.client.util.NotificationOverlay;
import org.swarmforge.client.util.I18nManager;

/**
 * Physical Coherence Validation and Auto-Harmonization Panel for the Weather Editor.
 * Checks thermodynamic and aerodynamic rules (e.g. Min &lt;= Avg &lt;= Max, high rain with dry air,
 * freezing temperatures with rainfall, sandstorm conditions) and provides automated correction.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class WeatherCoherenceValidatorPane extends VBox {

    public static final String[] MONTH_KEYS = {
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    private final double[] tempMin;
    private final double[] tempAvg;
    private final double[] tempMax;

    private final double[] windMin;
    private final double[] windAvg;
    private final double[] windMax;

    private final double[] rainMin;
    private final double[] rainAvg;
    private final double[] rainMax;

    private final double[] humidityMin;
    private final double[] humidityAvg;
    private final double[] humidityMax;

    private final Runnable onHarmonized;

    private Label coherenceStatusBadge;
    private VBox coherenceDetailsBox;

    public WeatherCoherenceValidatorPane(
            double[] tempMin, double[] tempAvg, double[] tempMax,
            double[] windMin, double[] windAvg, double[] windMax,
            double[] rainMin, double[] rainAvg, double[] rainMax,
            double[] humidityMin, double[] humidityAvg, double[] humidityMax,
            Runnable onHarmonized) {
        this.tempMin = tempMin;
        this.tempAvg = tempAvg;
        this.tempMax = tempMax;
        this.windMin = windMin;
        this.windAvg = windAvg;
        this.windMax = windMax;
        this.rainMin = rainMin;
        this.rainAvg = rainAvg;
        this.rainMax = rainMax;
        this.humidityMin = humidityMin;
        this.humidityAvg = humidityAvg;
        this.humidityMax = humidityMax;
        this.onHarmonized = onHarmonized;

        buildUI();
    }

    private void buildUI() {
        I18nManager i18n = I18nManager.getInstance();
        setSpacing(8);
        setPadding(new Insets(10));
        getStyleClass().add("card-pane");

        Label title = new Label();
        title.textProperty().bind(i18n.createStringBinding("weather.coherence.title"));
        title.getStyleClass().add("card-title");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        HBox banner = new HBox(12);
        banner.setAlignment(Pos.CENTER_LEFT);

        coherenceStatusBadge = new Label();
        coherenceStatusBadge.setStyle("-fx-font-weight:bold;-fx-font-size:12;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button bHarmonize = new Button(i18n.get("weather.coherence.harmonize"));
        bHarmonize.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold;");
        bHarmonize.setOnAction(e -> doHarmonizeCoherence());

        banner.getChildren().addAll(coherenceStatusBadge, sp, bHarmonize);

        coherenceDetailsBox = new VBox(4);
        coherenceDetailsBox.setPadding(new Insets(4, 0, 0, 0));

        getChildren().addAll(title, banner, new Separator(), coherenceDetailsBox);

        updateCoherenceStatus();
    }

    public void updateCoherenceStatus() {
        if (coherenceStatusBadge == null) return;
        I18nManager i18n = I18nManager.getInstance();

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        for (int m = 0; m < 12; m++) {
            String mName = MONTH_KEYS[m];

            checkOrder("Temperature", mName, tempMin[m], tempAvg[m], tempMax[m], errors);
            checkOrder("Vent", mName, windMin[m], windAvg[m], windMax[m], errors);
            checkOrder("Precipitation", mName, rainMin[m], rainAvg[m], rainMax[m], errors);
            checkOrder("Humidity", mName, humidityMin[m], humidityAvg[m], humidityMax[m], errors);

            if (rainAvg[m] > 120 && humidityAvg[m] < 35) {
                warnings.add(String.format("⚠ %s: Heavy rain (%.0f mm) but very dry air (%.0f%% RH). Evaporation risk (Virga).",
                        mName, rainAvg[m], humidityAvg[m]));
            }

            if (tempMax[m] < 0 && rainAvg[m] > 10) {
                warnings.add(String.format("❄️ %s: Rainfall below 0°C (Max %.1f°C). Converted to snowfall.",
                        mName, tempMax[m]));
            }

            if (tempAvg[m] > 38 && humidityAvg[m] > 80) {
                warnings.add(String.format("🔥 %s: Extreme temperature (%.1f°C) and high humidity (%.0f%% RH). Critical heat index.",
                        mName, tempAvg[m], humidityAvg[m]));
            }

            if (windAvg[m] > 40 && rainAvg[m] < 10 && humidityAvg[m] < 25) {
                warnings.add(String.format("🏜️ %s: Gale winds (%.0f km/h) & arid conditions: Dust storm risk.",
                        mName, windAvg[m]));
            }
        }

        coherenceDetailsBox.getChildren().clear();

        if (errors.isEmpty() && warnings.isEmpty()) {
            coherenceStatusBadge.setText(i18n.get("weather.coherence.badge_ok"));
            coherenceStatusBadge.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold; -fx-font-size: 13;");
            Label lbl = new Label(i18n.get("weather.coherence.msg_perfect"));
            lbl.setStyle("-fx-font-style: italic; -fx-text-fill: #6c757d;");
            coherenceDetailsBox.getChildren().add(lbl);
        } else if (!errors.isEmpty()) {
            coherenceStatusBadge.setText(i18n.get("weather.coherence.badge_error", errors.size()));
            coherenceStatusBadge.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold; -fx-font-size: 13;");
            for (String err : errors) {
                Label l = new Label(err);
                l.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 11;");
                coherenceDetailsBox.getChildren().add(l);
            }
        } else {
            coherenceStatusBadge.setText(i18n.get("weather.coherence.badge_warn", warnings.size()));
            coherenceStatusBadge.setStyle("-fx-text-fill: #ffc107; -fx-font-weight: bold; -fx-font-size: 13;");
            for (String w : warnings) {
                Label l = new Label(w);
                l.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 11;");
                coherenceDetailsBox.getChildren().add(l);
            }
        }
    }

    private void checkOrder(String param, String month, double min, double avg, double max, List<String> errors) {
        if (min > avg) {
            errors.add(String.format("❌ %s (%s): Min (%.1f) > Moyenne (%.1f)", param, month, min, avg));
        }
        if (avg > max) {
            errors.add(String.format("❌ %s (%s): Moyenne (%.1f) > Max (%.1f)", param, month, avg, max));
        }
    }

    public void doHarmonizeCoherence() {
        for (int m = 0; m < 12; m++) {
            harmonizeTriplet(tempMin, tempAvg, tempMax, m);
            harmonizeTriplet(windMin, windAvg, windMax, m);
            harmonizeTriplet(rainMin, rainAvg, rainMax, m);
            harmonizeTriplet(humidityMin, humidityAvg, humidityMax, m);

            humidityMin[m] = Math.max(0, Math.min(100, humidityMin[m]));
            humidityAvg[m] = Math.max(0, Math.min(100, humidityAvg[m]));
            humidityMax[m] = Math.max(0, Math.min(100, humidityMax[m]));

            windMin[m] = Math.max(0, windMin[m]);
            windAvg[m] = Math.max(0, windAvg[m]);
            windMax[m] = Math.max(0, windMax[m]);

            rainMin[m] = Math.max(0, rainMin[m]);
            rainAvg[m] = Math.max(0, rainAvg[m]);
            rainMax[m] = Math.max(0, rainMax[m]);

            if (rainAvg[m] > 100 && humidityAvg[m] < 40) {
                humidityAvg[m] = 45;
                humidityMax[m] = Math.max(humidityMax[m], 60);
            }
        }

        updateCoherenceStatus();
        if (onHarmonized != null) {
            onHarmonized.run();
        }
        NotificationOverlay.show(this, "Curves successfully harmonized according to physical coherence rules!", NotificationOverlay.NotificationType.SUCCESS);
    }

    private void harmonizeTriplet(double[] min, double[] avg, double[] max, int m) {
        if (min[m] > avg[m]) min[m] = avg[m] - 2.0;
        if (max[m] < avg[m]) max[m] = avg[m] + 2.0;
    }
}
