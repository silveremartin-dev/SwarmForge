/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import org.swarmforge.client.util.I18nManager;

import java.util.Locale;

/**
 * Builds and updates the live meteorological overlay HUD for {@link WorldEditorPane}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
final class WorldEditorWeatherOverlayHelper {

    private WorldEditorWeatherOverlayHelper() {
    }

    static VBox createWeatherOverlayPane(WorldEditorPane pane) {
        VBox box = new VBox(6);
        box.setPadding(new Insets(10, 14, 10, 14));
        box.setPrefWidth(390);
        box.setMinWidth(380);
        box.setMaxWidth(440);
        box.getStyleClass().addAll("card-pane", "weather-hud-box");
        box.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        box.setPickOnBounds(false);
        box.setMouseTransparent(true);
        box.setVisible(pane.isSimulationMode && pane.isWeatherOverlayVisible);
        box.setManaged(pane.isSimulationMode && pane.isWeatherOverlayVisible);

        Label title = new Label();
        title.textProperty().bind(I18nManager.getInstance().createStringBinding("weather.info_overlay.title"));
        title.getStyleClass().add("weather-hud-title");

        pane.lblWeatherCoords = new Label("📍 Lat: -- • Lon: -- • Alt: --");
        pane.lblWeatherCoords.getStyleClass().add("bold-label");
        pane.lblWeatherCoords.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(4);
        ColumnConstraints col0 = new ColumnConstraints();
        col0.setMinWidth(175);
        col0.setPrefWidth(180);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(175);
        col1.setPrefWidth(180);
        grid.getColumnConstraints().addAll(col0, col1);

        pane.lblWeatherTemp = new Label("🌡️ Temp: --");
        pane.lblWeatherTemp.setStyle("-fx-text-fill: #f87171; -fx-font-size: 10.5px; -fx-font-weight: bold;");
        pane.lblWeatherTemp.setWrapText(true);

        pane.lblWeatherHum = new Label("💧 Humidité: --");
        pane.lblWeatherHum.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 10.5px;");
        pane.lblWeatherHum.setWrapText(true);

        pane.lblWeatherLight = new Label("☀️ Lumière: --");
        pane.lblWeatherLight.setStyle("-fx-text-fill: #fde047; -fx-font-size: 10.5px;");
        pane.lblWeatherLight.setWrapText(true);

        pane.lblWeatherWindSpd = new Label("💨 Vent: --");
        pane.lblWeatherWindSpd.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 10.5px;");
        pane.lblWeatherWindSpd.setWrapText(true);

        pane.lblWeatherWindDir = new Label("🧭 Dir: --");
        pane.lblWeatherWindDir.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 10.5px;");
        pane.lblWeatherWindDir.setWrapText(true);

        pane.lblWeatherRain = new Label("🌧️ Pluie: --");
        pane.lblWeatherRain.setStyle("-fx-text-fill: #60a5fa; -fx-font-size: 10.5px;");
        pane.lblWeatherRain.setWrapText(true);

        pane.lblWeatherPressure = new Label("📉 Pression: --");
        pane.lblWeatherPressure.setStyle("-fx-text-fill: #a7f3d0; -fx-font-size: 10.5px;");
        pane.lblWeatherPressure.setWrapText(true);

        pane.lblWeatherMoon = new Label("🌔 Lune: --");
        pane.lblWeatherMoon.setStyle("-fx-text-fill: #c084fc; -fx-font-size: 10.5px;");
        pane.lblWeatherMoon.setWrapText(true);

        grid.add(pane.lblWeatherTemp, 0, 0);
        grid.add(pane.lblWeatherWindSpd, 1, 0);

        grid.add(pane.lblWeatherHum, 0, 1);
        grid.add(pane.lblWeatherWindDir, 1, 1);

        grid.add(pane.lblWeatherLight, 0, 2);
        grid.add(pane.lblWeatherRain, 1, 2);

        grid.add(pane.lblWeatherPressure, 0, 3);
        grid.add(pane.lblWeatherMoon, 1, 3);

        pane.lblWeatherStatus = new Label("☀️ Ensoleillé • Jour");
        pane.lblWeatherStatus.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 10.5px; -fx-font-weight: bold;");
        pane.lblWeatherStatus.setWrapText(true);

        pane.lblWeatherPhotoperiod = new Label("🌅 Lever: -- • 🌇 Coucher: --");
        pane.lblWeatherPhotoperiod.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 10px;");
        pane.lblWeatherPhotoperiod.setWrapText(true);

        box.getChildren().addAll(title, pane.lblWeatherCoords, grid, new Separator(), pane.lblWeatherStatus, pane.lblWeatherPhotoperiod);
        StackPane.setAlignment(box, Pos.TOP_LEFT);
        StackPane.setMargin(box, new Insets(10, 10, 10, 10));
        return box;
    }

    static void updateWeatherOverlay(WorldEditorPane pane) {
        if (pane.weatherOverlayPane == null || !pane.isSimulationMode || !pane.isWeatherOverlayVisible) return;
        if (pane.activeSimulation == null || pane.activeSimulation.getWeather() == null) {
            pane.weatherOverlayPane.setVisible(false);
            pane.weatherOverlayPane.setManaged(false);
            return;
        }
        pane.weatherOverlayPane.setVisible(pane.isWeatherOverlayVisible);
        pane.weatherOverlayPane.setManaged(pane.isWeatherOverlayVisible);

        org.swarmforge.core.world.WeatherSystem w = pane.activeSimulation.getWeather();
        double lat = w.getLatitude();
        double lon = w.getLongitude();
        double alt = w.getAltitude();
        float temp = w.getTemperature();
        float hum = w.getHumidity();
        float light = w.getLightLevel() * 100.0f;
        float windSpd = w.getWindSpeed();
        String windDir = w.getWindDirection();
        float windAngle = w.getWindDirectionAngle();
        float rain = w.getRainfall();
        float snow = w.getSnowfall();
        float press = w.getPressure();
        boolean isDay = w.isDaytime();

        if (pane.lblWeatherCoords != null) {
            String latStr = String.format(Locale.US, "%.2f°%s", Math.abs(lat), lat >= 0 ? "N" : "S");
            String lonStr = String.format(Locale.US, "%.2f°%s", Math.abs(lon), lon >= 0 ? "E" : "W");
            pane.lblWeatherCoords.setText(String.format(Locale.US, "📍 %s, %s • %.0f m", latStr, lonStr, alt));
        }

        pane.lblWeatherTemp.setText(String.format(Locale.US, "🌡️ Temp: %.1f °C", temp));
        pane.lblWeatherHum.setText(String.format(Locale.US, "💧 Humidité: %.0f %%", hum));
        pane.lblWeatherLight.setText(String.format(Locale.US, "☀️ Lumière: %.0f %%", light));
        pane.lblWeatherWindSpd.setText(String.format(Locale.US, "💨 Vent: %.1f km/h", windSpd));
        pane.lblWeatherWindDir.setText(String.format("🧭 Dir: %s (%.0f°)", windDir != null ? windDir : "SO", windAngle));
        pane.lblWeatherPressure.setText(String.format(Locale.US, "📉 Pression: %.1f hPa", press));

        String moonIcon = "🌕";
        String moonName = "Pleine Lune";
        try {
            java.lang.reflect.Method mIcon = w.getClass().getMethod("getMoonPhaseIcon");
            java.lang.reflect.Method mKey = w.getClass().getMethod("getMoonPhaseKey");
            if (mIcon != null) moonIcon = (String) mIcon.invoke(w);
            if (mKey != null) {
                String k = (String) mKey.invoke(w);
                if (k != null) {
                    String val = I18nManager.getInstance().get(k);
                    if (val != null && !val.isBlank()) moonName = val;
                }
            }
        } catch (Exception ignored) {}
        pane.lblWeatherMoon.setText(String.format("%s Lune: %s", moonIcon, moonName));

        if (rain > 0) {
            pane.lblWeatherRain.setText(String.format(Locale.US, "🌧️ Pluie: %.1f mm/h", rain));
        } else if (snow > 0) {
            pane.lblWeatherRain.setText(String.format(Locale.US, "❄️ Neige: %.1f mm/h", snow));
        } else {
            pane.lblWeatherRain.setText("🌧️ Pluie: 0.0 mm/h");
        }

        String dayStr = I18nManager.getInstance().get("weather.daytime");
        if (dayStr == null) dayStr = "Jour";
        String nightStr = I18nManager.getInstance().get("weather.nighttime");
        if (nightStr == null) nightStr = "Nuit";
        String dayNightStr = isDay ? dayStr : nightStr;

        org.swarmforge.core.world.WeatherMarkovChain.WeatherState state = w.getWeatherState();
        String weatherLabel = switch (state) {
            case SUNNY -> "☀️ Ensoleillé, ciel dégagé";
            case PARTLY_CLOUDY -> "⛅ Passages nuageux";
            case OVERCAST -> "☁️ Temps couvert";
            case LIGHT_RAIN -> "🌧️ Pluie fine continue";
            case HEAVY_RAIN -> "🌧️ Fortes précipitations";
            case THUNDERSTORM -> "⛈️ Orageux avec éclairs";
            case SANDSTORM -> "🏜️ Tempête de sable";
            case HAIL -> "🌨️ Averses de grêle";
            case BLIZZARD -> "❄️ Blizzard et vents violents";
            case TEMPEST -> "🌪️ Tempête et bourrasques";
            case HEATWAVE -> "🔥 Vague de chaleur";
            case DROUGHT -> "🌵 Sécheresse";
            case SNOW -> "❄️ Chutes de neige";
            case FOG -> "🌫️ Brouillard matinal dense";
            default -> state.label != null ? state.label : "🌤️ " + state.name();
        };

        pane.lblWeatherStatus.setText(String.format("%s • %s", weatherLabel, dayNightStr));

        double latRad = Math.toRadians(lat);
        int dayOfYear = w.getDayOfYear() > 0 ? w.getDayOfYear() : 80;
        double declinationRad = Math.toRadians(23.45 * Math.sin(Math.toRadians(360.0 / 365.0 * (dayOfYear - 81))));
        double tanProduct = -Math.tan(latRad) * Math.tan(declinationRad);
        double dayHours = 12.0;
        if (tanProduct < 1.0 && tanProduct > -1.0) {
            dayHours = Math.toDegrees(Math.acos(tanProduct)) / 15.0 * 2.0;
        } else if (tanProduct <= -1.0) {
            dayHours = 24.0;
        } else {
            dayHours = 0.0;
        }

        int dayH = (int) dayHours;
        int dayM = (int) Math.round((dayHours - dayH) * 60);
        double halfDay = dayHours / 2.0;
        int sunriseH = Math.max(0, (int) (12.0 - halfDay));
        int sunriseM = Math.max(0, (int) Math.round(((12.0 - halfDay) - sunriseH) * 60));
        int sunsetH = Math.min(23, (int) (12.0 + halfDay));
        int sunsetM = Math.max(0, (int) Math.round(((12.0 + halfDay) - sunsetH) * 60));

        pane.lblWeatherPhotoperiod.setText(String.format(Locale.US, "🌅 Lever: %02dh%02d • 🌇 Coucher: %02dh%02d (Durée: %dh%02dm)",
            sunriseH, sunriseM, sunsetH, sunsetM, dayH, dayM));
    }
}
