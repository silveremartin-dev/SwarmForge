/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.scene.paint.Color;
import org.swarmforge.core.world.WeatherMarkovChain;
import org.swarmforge.core.world.WeatherSystem;

import java.util.Locale;
import java.util.Random;

/**
 * Renders atmospheric and weather overlays (clouds, rain, snow, hail, fog, lightning, day-night tint).
 */
public final class WorldEditorWeatherOverlayRenderer {

    private WorldEditorWeatherOverlayRenderer() {}

    public static void drawWeatherOverlay3D(WorldEditorPane pane, double w, double h) {
        if (pane.activeSimulation == null || pane.activeSimulation.getWeather() == null) {
            return;
        }

        WeatherSystem weather = pane.activeSimulation.getWeather();
        WeatherMarkovChain.WeatherState state = weather.getWeatherState();
        if (state == null) state = WeatherMarkovChain.WeatherState.SUNNY;

        long timeMs = System.currentTimeMillis();
        double timeSec = timeMs / 1000.0;

        // Miss Météo Polling logic: Update photoperiod & forecast report every 10 seconds (10,000ms)
        if (timeMs - pane.lastMeteoPollMs > 10000 || pane.cachedMeteoReportText.isEmpty()) {
            pane.lastMeteoPollMs = timeMs;

            int simHour = 14;
            int simMinute = 33;
            if (pane.activeSimulation != null && pane.activeSimulation.getDayNightCycle() != null) {
                simHour = pane.activeSimulation.getDayNightCycle().getHour();
                simMinute = (int) (pane.activeSimulation.getDayNightCycle().getPhase() * 24.0 * 60.0) % 60;
            }

            double latRad = Math.toRadians(48.8);
            int dayOfYear = 80;
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

            pane.cachedPhotoperiodText = String.format(Locale.US, "⏱ %02dh%02d | 🌅 Lever: %02dh%02d | 🌇 Coucher: %02dh%02d | Durée: %dh%02dm",
                simHour, simMinute, sunriseH, sunriseM, sunsetH, sunsetM, dayH, dayM);

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
            pane.cachedMeteoReportText = weatherLabel + String.format(Locale.US, " (%.1f°C, Vent: %.0f km/h %s)",
                weather.getTemperature(), weather.getWindSpeed(), weather.getWindDirection());
        }

        // 1. Atmosphere & Lighting Vignette
        float lightLevel = weather.getLightLevel();
        if (lightLevel < 0.85f || state != WeatherMarkovChain.WeatherState.SUNNY) {
            double opacity = Math.max(0.0, Math.min(0.08, (0.85 - lightLevel) * 0.12));
            if (opacity > 0.005) {
                pane.gc3D.setFill(Color.web("#0f172a", opacity));
                pane.gc3D.fillRect(0, 0, w, h);
            }
        } else {
            pane.gc3D.setFill(Color.web("#fef08a", 0.035));
            pane.gc3D.fillRect(0, 0, w, h);
        }

        // 2. Rolling Cloud Shadows
        if (state == WeatherMarkovChain.WeatherState.OVERCAST ||
            state == WeatherMarkovChain.WeatherState.LIGHT_RAIN ||
            state == WeatherMarkovChain.WeatherState.HEAVY_RAIN ||
            state == WeatherMarkovChain.WeatherState.THUNDERSTORM ||
            state == WeatherMarkovChain.WeatherState.FOG) {

            pane.gc3D.setFill(Color.web("#0f172a", 0.15));
            double cloudX1 = (timeMs * 0.02) % (w + 300) - 150;
            double cloudX2 = (timeMs * 0.035) % (w + 400) - 200;
            pane.gc3D.fillOval(cloudX1, 10, 260, 60);
            pane.gc3D.fillOval(cloudX2, 30, 320, 70);
        }

        // 3. Fog Effect Overlay
        if (state == WeatherMarkovChain.WeatherState.FOG) {
            pane.gc3D.setFill(Color.web("#cbd5e1", 0.22));
            pane.gc3D.fillRect(0, 0, w, h);
        }

        // 4. Precipitation Effects
        float windSpeed = weather.getWindSpeed();
        double effectiveWindAngleRad = Math.toRadians(weather.getWindDirectionAngle()) - Math.toRadians(pane.azimuth);
        double windDx = Math.cos(effectiveWindAngleRad) * (windSpeed * 0.35);
        double windDy = 14.0 + Math.sin(effectiveWindAngleRad) * (windSpeed * 0.15);

        Random r = new Random(54321);

        if (state == WeatherMarkovChain.WeatherState.LIGHT_RAIN ||
            state == WeatherMarkovChain.WeatherState.HEAVY_RAIN ||
            state == WeatherMarkovChain.WeatherState.THUNDERSTORM) {

            int particleCount = (state == WeatherMarkovChain.WeatherState.HEAVY_RAIN ||
                                 state == WeatherMarkovChain.WeatherState.THUNDERSTORM) ? 80 : 35;

            pane.gc3D.setStroke(Color.web("#38bdf8", 0.50));
            pane.gc3D.setLineWidth(1.1);
            for (int i = 0; i < particleCount; i++) {
                double rx = (r.nextDouble() * w + timeSec * (120.0 + windDx * 10.0) + i * 47) % w;
                double ry = (r.nextDouble() * h + timeSec * (350.0 + windDy * 10.0) + i * 61) % h;
                pane.gc3D.strokeLine(rx, ry, rx + windDx, ry + windDy);
            }
        }

        // 5. Snow Flakes
        if (state == WeatherMarkovChain.WeatherState.SNOW ||
            state == WeatherMarkovChain.WeatherState.BLIZZARD) {

            int snowCount = (state == WeatherMarkovChain.WeatherState.BLIZZARD) ? 90 : 40;
            pane.gc3D.setFill(Color.web("#f8fafc", 0.80));
            for (int i = 0; i < snowCount; i++) {
                double sx = (r.nextDouble() * w + timeSec * (40.0 + windDx * 5.0) + Math.sin(timeSec + i) * 15.0 + i * 31) % w;
                double sy = (r.nextDouble() * h + timeSec * (80.0 + windDy * 2.0) + i * 53) % h;
                double size = 1.5 + r.nextDouble() * 2.0;
                pane.gc3D.fillOval(sx, sy, size, size);
            }
        }

        // 6. Hail Impact Particles
        if (state == WeatherMarkovChain.WeatherState.HAIL) {
            pane.gc3D.setFill(Color.web("#f8fafc", 0.90));
            for (int i = 0; i < 30; i++) {
                double hx = (r.nextDouble() * w + timeSec * 60.0 + i * 67) % w;
                double hy = (r.nextDouble() * h + timeSec * 420.0 + i * 89) % h;
                pane.gc3D.fillOval(hx, hy, 3.0, 3.0);
                if (hy > h * 0.6) {
                    pane.gc3D.setStroke(Color.web("#ffffff", 0.50));
                    pane.gc3D.setLineWidth(0.9);
                    pane.gc3D.strokeOval(hx - 3, hy - 1, 6, 2);
                }
            }
        }

        // 7. Thunderstorm Strobe Flash
        if (state == WeatherMarkovChain.WeatherState.THUNDERSTORM) {
            if (r.nextDouble() < 0.02) {
                pane.gc3D.setFill(Color.web("#ffffff", 0.35));
                pane.gc3D.fillRect(0, 0, w, h);

                double strikeDistMeters = 350.0 + r.nextDouble() * 1050.0;
                org.swarmforge.client.audio.SimulationAudioManager.getInstance().triggerLightningThunderStrike(strikeDistMeters);
            }
        }
    }
}
