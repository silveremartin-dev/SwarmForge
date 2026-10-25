/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.swarmforge.client.util.I18nManager;

/**
 * Builds the right render options sidebar, interactive geological/chamber/pheromone/caste
 * legends, and live meteorological overlay HUD for {@link WorldEditorPane}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
final class WorldEditorRightSidebarAndLegendBuilder {

    private WorldEditorRightSidebarAndLegendBuilder() {
    }

    static VBox createStandaloneLegendPanel() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(6));
        panel.setStyle("-fx-background-color: transparent;");

        Label titleSub = new Label("🧱 Substrats Géologiques");
        titleSub.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");
        FlowPane subFlow = new FlowPane(4, 4);
        subFlow.setPrefWrapLength(240);
        String[][] subs = {
            {"organic", "#523219", "Organique"},
            {"earth", "#3d2817", "Terre / Humus"},
            {"sand", "#eab308", "Sable"},
            {"clay", "#9a3412", "Argile"},
            {"silt", "#ca8a04", "Limon"},
            {"peat", "#451a03", "Tourbe"},
            {"gravel", "#94a3b8", "Gravier"},
            {"stone", "#64748b", "Roche"},
            {"cavities", "#0f172a", "Cavités"},
            {"roots", "#78350f", "Racines"},
            {"river", "#0284c7", "Eau / Rivière"},
            {"vegetation", "#15803d", "Végétation"}
        };
        for (String[] s : subs) {
            HBox b = new HBox(4);
            b.setAlignment(Pos.CENTER_LEFT);
            b.setPadding(new Insets(2, 5, 2, 5));
            b.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-background-radius: 4; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 4; -fx-border-width: 0.5;");
            Canvas dot = new Canvas(8, 8);
            GraphicsContext g = dot.getGraphicsContext2D();
            g.setFill(Color.web(s[1]));
            g.fillOval(0, 0, 8, 8);
            g.setStroke(Color.WHITE);
            g.setLineWidth(0.6);
            g.strokeOval(0, 0, 8, 8);
            Label lbl = new Label(s[2]);
            lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #e2e8f0;");
            b.getChildren().addAll(dot, lbl);
            subFlow.getChildren().add(b);
        }

        Label titleNest = new Label("🏰 Galeries & Chambres");
        titleNest.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #10b981;");
        FlowPane nestFlow = new FlowPane(4, 4);
        nestFlow.setPrefWrapLength(240);
        String[][] chambers = {
            {"👑 Chambre Royale", "#d946ef"},
            {"🥚 Couvain", "#f8fafc"},
            {"🍄 Meule Champignon", "#a855f7"},
            {"🌿 Étable à Pucerons", "#ec4899"},
            {"🌾 Grenier à Graines", "#22c55e"},
            {"❄️ Salle Diapause", "#0284c7"},
            {"🗑️ Dépotoir", "#eab308"},
            {"🚪 Entrées / Puits", "#9a3412"}
        };
        for (String[] ch : chambers) {
            HBox b = new HBox(4);
            b.setAlignment(Pos.CENTER_LEFT);
            b.setPadding(new Insets(2, 5, 2, 5));
            b.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-background-radius: 4; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 4; -fx-border-width: 0.5;");
            Canvas dot = new Canvas(8, 8);
            GraphicsContext g = dot.getGraphicsContext2D();
            g.setFill(Color.web(ch[1]));
            g.fillOval(0, 0, 8, 8);
            g.setStroke(Color.WHITE);
            g.setLineWidth(0.6);
            g.strokeOval(0, 0, 8, 8);
            Label lbl = new Label(ch[0]);
            lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #e2e8f0;");
            b.getChildren().addAll(dot, lbl);
            nestFlow.getChildren().add(b);
        }

        panel.getChildren().addAll(
            titleSub, subFlow,
            new Separator(),
            titleNest, nestFlow
        );
        return panel;
    }

    static VBox createWeatherOverlayPane(WorldEditorPane pane) {
        VBox box = new VBox(6);
        box.setPadding(new Insets(10, 14, 10, 14));
        box.setPrefWidth(390);
        box.setMinWidth(380);
        box.setMaxWidth(440);
        box.setStyle("-fx-background-color: rgba(15, 23, 42, 0.95); -fx-background-radius: 8px; -fx-border-color: rgba(56, 189, 248, 0.6); -fx-border-width: 1.2px; -fx-border-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.7), 10, 0, 0, 3);");
        box.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        box.setPickOnBounds(false);
        box.setMouseTransparent(true);
        box.setVisible(pane.isSimulationMode && pane.isWeatherOverlayVisible);
        box.setManaged(pane.isSimulationMode && pane.isWeatherOverlayVisible);

        Label title = new Label();
        title.textProperty().bind(I18nManager.getInstance().createStringBinding("weather.info_overlay.title"));
        title.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 11.5px; -fx-font-weight: bold;");

        pane.lblWeatherCoords = new Label("📍 Lat: -- • Lon: -- • Alt: --");
        pane.lblWeatherCoords.setStyle("-fx-text-fill: #7dd3fc; -fx-font-size: 10px; -fx-font-weight: bold;");
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

    static VBox buildRightRenderOptionsPanel(WorldEditorPane pane) {
        VBox panel = new VBox(6);
        panel.setPadding(new Insets(8));
        panel.getStyleClass().add("render-options-panel");

        Label title = new Label();
        title.textProperty().bind(I18nManager.getInstance().createStringBinding("world.render_layers.title"));
        title.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold;");
        title.getStyleClass().add("legend-title");

        pane.showMinimapCheck = new CheckBox("🗺️ Minimap 2D");
        pane.showMinimapCheck.setSelected(pane.isDualMinimapVisible());
        pane.showMinimapCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("sidebar.chk.minimap"));
        pane.showMinimapCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("sidebar.chk.minimap.tt"));
        pane.showMinimapCheck.selectedProperty().addListener((obs, oldV, newV) -> pane.setDualMinimapVisible(newV));
        pane.syncViewsCheckBox = new CheckBox("🔄 Synchroniser");
        pane.syncViewsCheckBox.textProperty().bind(I18nManager.getInstance().createStringBinding("minimap.sync"));
        pane.syncViewsCheckBox.setSelected(true);
        pane.syncViewsCheckBox.getStyleClass().add("legend-checkbox");
        VBox.setMargin(pane.syncViewsCheckBox, new Insets(0, 0, 0, 16));

        pane.showLegendCheckBox = new CheckBox("👁️ Afficher les légendes");
        pane.showLegendCheckBox.textProperty().bind(I18nManager.getInstance().createStringBinding("legend.show"));
        pane.showLegendCheckBox.setSelected(true);
        pane.showLegendCheckBox.getStyleClass().add("legend-checkbox");
        pane.showLegendCheckBox.setOnAction(e -> pane.setShowLegend(pane.showLegendCheckBox.isSelected()));

        pane.showTerrainCheck = new CheckBox("🏞️ Render 3D Terrain");
        pane.showTerrainCheck.setSelected(true);
        pane.showTerrainCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("sidebar.chk.terrain"));
        pane.showTerrainCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.terrain.tt"));
        pane.showTerrainCheck.selectedProperty().addListener((obs, oldV, newV) -> pane.setTerrainVisible(newV));

        pane.showChamferedBezelCheck = new CheckBox("📐 Substrate Chamfered Bezel");
        pane.showChamferedBezelCheck.setSelected(true);
        pane.showChamferedBezelCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("sidebar.chk.skirt"));
        pane.showChamferedBezelCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.bezel.tt"));
        pane.showChamferedBezelCheck.selectedProperty().addListener((obs, oldV, newV) -> pane.setShow3DSkirt(newV));

        pane.showGravelInclusionsCheck = new CheckBox();
        pane.showGravelInclusionsCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.layer.gravel_inclusions"));
        pane.showGravelInclusionsCheck.setSelected(true);
        pane.showGravelInclusionsCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.gravel.tt"));

        pane.showSubstrateStratigraphyCheck = new CheckBox();
        pane.showSubstrateStratigraphyCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.layer.stratigraphy"));
        pane.showSubstrateStratigraphyCheck.setSelected(true);
        pane.showSubstrateStratigraphyCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.stratigraphy.tt"));

        pane.showTranslucentVolumetricModeCheck = new CheckBox();
        pane.showTranslucentVolumetricModeCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.layer.translucency"));
        pane.showTranslucentVolumetricModeCheck.setSelected(false);
        pane.showTranslucentVolumetricModeCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.translucent.tt"));

        pane.showHumidityCheck = new CheckBox();
        pane.showHumidityCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.layer.humidity_map"));
        pane.showHumidityCheck.setSelected(false);
        pane.showHumidityCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.humidity.tt"));

        pane.showOrganicCheck = new CheckBox(); pane.showOrganicCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.layer.organic")); pane.showOrganicCheck.setSelected(true);
        pane.showEarthCheck = new CheckBox(); pane.showEarthCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.layer.earth")); pane.showEarthCheck.setSelected(true);
        pane.showSandCheck = new CheckBox(); pane.showSandCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.layer.sand")); pane.showSandCheck.setSelected(true);
        pane.showClayCheck = new CheckBox(); pane.showClayCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.layer.clay")); pane.showClayCheck.setSelected(true);
        pane.showSiltCheck = new CheckBox(); pane.showSiltCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.layer.silt")); pane.showSiltCheck.setSelected(true);
        pane.showPeatCheck = new CheckBox(); pane.showPeatCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.layer.peat")); pane.showPeatCheck.setSelected(true);
        pane.showGravelCheck = new CheckBox(); pane.showGravelCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.layer.gravel")); pane.showGravelCheck.setSelected(true);
        pane.showStoneCheck = new CheckBox(); pane.showStoneCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.layer.stone")); pane.showStoneCheck.setSelected(true);

        pane.showGalleriesCheck = new CheckBox("🕳️ Natural Cavities & Voids"); pane.showGalleriesCheck.setSelected(true);
        pane.showGalleriesCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("sidebar.chk.nest"));
        pane.showGalleriesCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.galleries.tt"));
        pane.showGalleriesCheck.selectedProperty().addListener((o, a, b) -> {
            pane.isGalleriesVisible = b;
            pane.repaintAllViews();
        });

        pane.showVegetationCheck = new CheckBox("🌿 Vegetation & Trees"); pane.showVegetationCheck.setSelected(true);
        pane.showVegetationCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("sidebar.chk.trees"));
        pane.showRootsCheck = new CheckBox();
        pane.showRootsCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.layer.roots"));
        pane.showRootsCheck.setSelected(true);
        pane.showRootsCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.roots.tt"));
        pane.showPhCheck = new CheckBox();
        pane.showPhCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.layer.ph_map"));
        pane.showPhCheck.setSelected(false);
        pane.showPhCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.ph.tt"));

        pane.slicePlaneSlider = pane.mkSlider(0.0, 100.0, 100.0);
        pane.addLsn(pane.slicePlaneSlider);
        pane.slicePlaneSlider.valueProperty().addListener((o, a, n) -> {
            if (pane.slicePlaneChangeListener != null) {
                pane.slicePlaneChangeListener.accept(n.doubleValue());
            }
        });

        pane.showAntTrackingCheck = new CheckBox();
        pane.showAntTrackingCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.render.ant_tracking"));
        pane.showAntTrackingCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.ant_tracking.tt"));
        pane.showAntTrackingCheck.setSelected(pane.isSimulationMode);
        pane.showAntTrackingCheck.setVisible(pane.isSimulationMode);
        pane.showAntTrackingCheck.setManaged(pane.isSimulationMode);
        pane.showAntTrackingCheck.selectedProperty().addListener((obs, oldV, newV) -> pane.setAntTrackingEnabled(newV));

        pane.showChamberOverlayCheck = new CheckBox();
        pane.showChamberOverlayCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.render.chamber_overlay"));
        pane.showChamberOverlayCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.chamber_overlay.tt"));
        pane.showChamberOverlayCheck.setSelected(false);
        pane.showChamberOverlayCheck.setVisible(pane.isSimulationMode);
        pane.showChamberOverlayCheck.setManaged(pane.isSimulationMode);
        pane.showChamberOverlayCheck.selectedProperty().addListener((obs, oldV, newV) -> pane.setChamberOverlayVisible(newV));

        pane.showElevationIsolinesCheck = new CheckBox();
        pane.showElevationIsolinesCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.render.isolines_elevation"));
        pane.showElevationIsolinesCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.isolines_elevation.tt"));
        pane.showElevationIsolinesCheck.setSelected(false);
        pane.showElevationIsolinesCheck.selectedProperty().addListener((obs, oldV, newV) -> pane.repaintAllViews());

        pane.showClimateIsolinesCheck = new CheckBox();
        pane.showClimateIsolinesCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.render.isolines_climate"));
        pane.showClimateIsolinesCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.isolines_climate.tt"));
        pane.showClimateIsolinesCheck.setSelected(false);
        pane.showClimateIsolinesCheck.selectedProperty().addListener((obs, oldV, newV) -> pane.repaintAllViews());

        pane.showPheromoneIsolinesCheck = new CheckBox();
        pane.showPheromoneIsolinesCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.render.isolines_pheromone"));
        pane.showPheromoneIsolinesCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.isolines_pheromone.tt"));
        pane.showPheromoneIsolinesCheck.setSelected(false);
        pane.showPheromoneIsolinesCheck.selectedProperty().addListener((obs, oldV, newV) -> pane.repaintAllViews());

        pane.showUVVisionModeCheck = new CheckBox();
        pane.showUVVisionModeCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.render.uv_vision"));
        pane.showUVVisionModeCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.uv_vision.tt"));
        pane.showUVVisionModeCheck.setSelected(false);
        pane.showUVVisionModeCheck.selectedProperty().addListener((obs, oldV, newV) -> {
            pane.isUVVisionMode = newV;
            if (pane.gameView != null && pane.gameView.getGameApp() != null) {
                pane.gameView.getGameApp().setUVVisionMode(newV);
            }
            pane.repaintAllViews();
        });

        pane.showOmmatidialVisionModeCheck = new CheckBox();
        pane.showOmmatidialVisionModeCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.render.ommatidial_vision"));
        pane.showOmmatidialVisionModeCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.ommatidial_vision.tt"));
        pane.showOmmatidialVisionModeCheck.setSelected(false);
        pane.showOmmatidialVisionModeCheck.selectedProperty().addListener((obs, oldV, newV) -> {
            pane.isOmmatidialVisionMode = newV;
            if (pane.gameView != null && pane.gameView.getGameApp() != null) {
                pane.gameView.getGameApp().setOmmatidialVisionMode(newV);
            }
            pane.repaintAllViews();
        });

        pane.addBoolLsn(pane.showTerrainCheck, pane.showChamferedBezelCheck, pane.showGravelInclusionsCheck, pane.showSubstrateStratigraphyCheck,
                   pane.showTranslucentVolumetricModeCheck, pane.showHumidityCheck,
                   pane.showOrganicCheck, pane.showEarthCheck, pane.showSandCheck, pane.showClayCheck, pane.showSiltCheck, pane.showPeatCheck, pane.showGravelCheck, pane.showStoneCheck, pane.showGalleriesCheck,
                   pane.showVegetationCheck, pane.showRootsCheck, pane.showPhCheck,
                   pane.showAntTrackingCheck, pane.showChamberOverlayCheck,
                   pane.showElevationIsolinesCheck, pane.showClimateIsolinesCheck, pane.showPheromoneIsolinesCheck, pane.showUVVisionModeCheck, pane.showOmmatidialVisionModeCheck);

        Label visHeader = new Label();
        visHeader.textProperty().bind(I18nManager.getInstance().createStringBinding("world.layer_visibility.title"));
        visHeader.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
        visHeader.getStyleClass().add("purple-title");

        VBox visBox = new VBox(4,
            visHeader,
            pane.showMinimapCheck,
            pane.syncViewsCheckBox,
            pane.showLegendCheckBox,
            new Separator(),
            pane.showAntTrackingCheck,
            pane.showChamberOverlayCheck,
            new Separator(),
            pane.showElevationIsolinesCheck,
            new Separator(),
            pane.showTerrainCheck, pane.showOrganicCheck, pane.showEarthCheck, pane.showSandCheck, pane.showClayCheck, pane.showSiltCheck, pane.showPeatCheck, pane.showGravelCheck, pane.showStoneCheck, pane.showGalleriesCheck,
            new Separator(),
            pane.showVegetationCheck, pane.showRootsCheck, pane.showPhCheck, pane.showSubstrateStratigraphyCheck,
            pane.showHumidityCheck
        );

        Label slicePlaneLabel = new Label();
        slicePlaneLabel.textProperty().bind(I18nManager.getInstance().createStringBinding("sidebar.lbl.slice"));

        panel.getChildren().addAll(title, new Separator(), pane.showTerrainCheck, pane.showChamferedBezelCheck, pane.showSubstrateStratigraphyCheck,
                                   slicePlaneLabel, pane.sv(pane.slicePlaneSlider, "%"),
                                   pane.showTranslucentVolumetricModeCheck, new Separator(), visBox);
        return panel;
    }

    static VBox buildLegendPanel(WorldEditorPane pane) {
        VBox panel = new VBox(6);
        panel.setPadding(new Insets(6));
        panel.getStyleClass().add("legend-card-pane");

        pane.legendContentBox = new VBox(6);

        Label titleSubstrates = new Label();
        titleSubstrates.textProperty().bind(I18nManager.getInstance().createStringBinding("legend.substrates.title"));
        titleSubstrates.getStyleClass().add("legend-title");

        FlowPane substrateItemsPane = new FlowPane(4, 4);
        substrateItemsPane.setPrefWrapLength(220);

        pane.lblHoverInfo = new Label();
        pane.lblHoverInfo.setText(I18nManager.getInstance().get("world.hover_info"));
        pane.lblHoverInfo.getStyleClass().add("legend-hover-info");
        pane.lblHoverInfo.setWrapText(true);
        pane.lblHoverInfo.setMaxWidth(230);

        List<String[]> substrateList = new ArrayList<>();
        substrateList.add(new String[]{"organic", "#523219"});
        substrateList.add(new String[]{"earth", "#3d2817"});
        substrateList.add(new String[]{"sand", "#eab308"});
        substrateList.add(new String[]{"clay", "#9a3412"});
        substrateList.add(new String[]{"silt", "#ca8a04"});
        substrateList.add(new String[]{"peat", "#451a03"});
        substrateList.add(new String[]{"gravel", "#94a3b8"});
        substrateList.add(new String[]{"stone", "#64748b"});
        substrateList.add(new String[]{"cavities", "#0f172a"});
        substrateList.add(new String[]{"roots", "#78350f"});
        substrateList.add(new String[]{"river", "#0284c7"});
        substrateList.add(new String[]{"vegetation", "#15803d"});

        for (String[] it : substrateList) {
            String key = it[0];
            HBox item = new HBox(4);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setPadding(new Insets(2, 4, 2, 4));
            item.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-background-radius: 4;");

            Canvas dot = new Canvas(8, 8);
            GraphicsContext g = dot.getGraphicsContext2D();
            g.setFill(Color.web(it[1]));
            g.fillOval(0, 0, 8, 8);
            g.setStroke(Color.WHITE);
            g.setLineWidth(0.6);
            g.strokeOval(0, 0, 8, 8);

            Label lbl = new Label();
            lbl.textProperty().bind(I18nManager.getInstance().createStringBinding("legend.substrate." + key));
            lbl.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("legend.substrate." + key + ".desc"));
            item.getChildren().addAll(dot, lbl);
            substrateItemsPane.getChildren().add(item);
        }

        pane.legendContentBox.getChildren().addAll(
            titleSubstrates,
            substrateItemsPane
        );

        panel.getChildren().add(pane.legendContentBox);

        Label titleNestInterior = new Label();
        titleNestInterior.textProperty().bind(I18nManager.getInstance().createStringBinding("legend.nest.title"));
        titleNestInterior.getStyleClass().add("legend-title");

        FlowPane nestItemsFlow = new FlowPane(4, 4);
        nestItemsFlow.setPrefWrapLength(220);

        List<String[]> nestList = new ArrayList<>();
        nestList.add(new String[]{"royal", "#d946ef", "dot"});
        nestList.add(new String[]{"brood", "#f8fafc", "dot_stroke"});
        nestList.add(new String[]{"fungus", "#a855f7", "dot"});
        nestList.add(new String[]{"aphid", "#ec4899", "dot"});
        nestList.add(new String[]{"granary", "#22c55e", "dot"});
        nestList.add(new String[]{"diapause", "#0284c7", "dot"});
        nestList.add(new String[]{"waste", "#eab308", "dot"});
        nestList.add(new String[]{"shafts", "#f59e0b", "line"});
        nestList.add(new String[]{"subnivean", "#e0f2fe", "line"});
        nestList.add(new String[]{"entrances", "#9a3412", "dot"});

        for (String[] it : nestList) {
            String key = it[0];
            HBox item = new HBox(4);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setPadding(new Insets(2, 4, 2, 4));
            item.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 4;");

            Node symbolNode;
            if ("line".equals(it[2])) {
                Canvas lineCanvas = new Canvas(10, 8);
                GraphicsContext g = lineCanvas.getGraphicsContext2D();
                g.setStroke(Color.web(it[1]));
                g.setLineWidth(2.0);
                g.strokeLine(0, 4, 10, 4);
                symbolNode = lineCanvas;
            } else {
                Canvas dot = new Canvas(8, 8);
                GraphicsContext g = dot.getGraphicsContext2D();
                g.setFill(Color.web(it[1]));
                g.fillOval(0, 0, 8, 8);
                if ("dot_stroke".equals(it[2])) {
                    g.setStroke(Color.web("#94a3b8"));
                    g.setLineWidth(0.8);
                    g.strokeOval(0, 0, 8, 8);
                } else {
                    g.setStroke(Color.WHITE);
                    g.setLineWidth(0.6);
                    g.strokeOval(0, 0, 8, 8);
                }
                symbolNode = dot;
            }

            Label lbl = new Label();
            lbl.setStyle("-fx-font-size: 10px;");
            lbl.textProperty().bind(I18nManager.getInstance().createStringBinding("legend.nest." + key));
            lbl.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("legend.nest." + key + ".desc"));
            item.getChildren().addAll(symbolNode, lbl);
            nestItemsFlow.getChildren().add(item);
        }

        pane.nestLegendBox = new VBox(4);
        pane.nestLegendBox.getChildren().addAll(new Separator(), titleNestInterior, nestItemsFlow);
        pane.nestLegendBox.setVisible(pane.isSimulationMode);
        pane.nestLegendBox.setManaged(pane.isSimulationMode);
        panel.getChildren().add(pane.nestLegendBox);

        Label titlePheromones = new Label();
        titlePheromones.textProperty().bind(I18nManager.getInstance().createStringBinding("legend.phero.title"));
        titlePheromones.getStyleClass().add("legend-title");

        FlowPane pheroItemsFlow = new FlowPane(4, 4);
        pheroItemsFlow.setPrefWrapLength(220);

        List<String[]> pheroList = new ArrayList<>();
        pheroList.add(new String[]{"home", "#06b6d4"});
        pheroList.add(new String[]{"food", "#f59e0b"});
        pheroList.add(new String[]{"danger", "#ef4444"});
        pheroList.add(new String[]{"queen", "#ec4899"});
        pheroList.add(new String[]{"necrophoric", "#78716c"});

        for (String[] it : pheroList) {
            String key = it[0];
            HBox item = new HBox(4);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setPadding(new Insets(2, 4, 2, 4));
            item.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 4;");

            Canvas dot = new Canvas(8, 8);
            GraphicsContext g = dot.getGraphicsContext2D();
            g.setFill(Color.web(it[1]));
            g.fillOval(0, 0, 8, 8);
            g.setStroke(Color.WHITE);
            g.setLineWidth(0.6);
            g.strokeOval(0, 0, 8, 8);

            Label lbl = new Label();
            lbl.setStyle("-fx-font-size: 10px;");
            lbl.textProperty().bind(I18nManager.getInstance().createStringBinding("legend.phero." + key));
            lbl.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("legend.phero." + key + ".desc"));
            item.getChildren().addAll(dot, lbl);
            pheroItemsFlow.getChildren().add(item);
        }

        pane.pheroLegendBox = new VBox(4);
        pane.pheroLegendBox.getChildren().addAll(new Separator(), titlePheromones, pheroItemsFlow);
        pane.pheroLegendBox.setVisible(pane.isSimulationMode);
        pane.pheroLegendBox.setManaged(pane.isSimulationMode);
        panel.getChildren().add(pane.pheroLegendBox);

        Label titleCastes = new Label();
        titleCastes.textProperty().bind(I18nManager.getInstance().createStringBinding("legend.castes.title"));
        titleCastes.getStyleClass().add("legend-title");

        FlowPane casteItemsFlow = new FlowPane(4, 4);
        casteItemsFlow.setPrefWrapLength(220);

        List<String[]> casteList = new ArrayList<>();
        casteList.add(new String[]{"queen", "#e11d48"});
        casteList.add(new String[]{"worker", "#10b981"});
        casteList.add(new String[]{"soldier", "#ea580c"});
        casteList.add(new String[]{"media", "#0284c7"});
        casteList.add(new String[]{"minor", "#14b8a6"});
        casteList.add(new String[]{"drone", "#eab308"});
        casteList.add(new String[]{"brood", "#f8fafc"});

        for (String[] it : casteList) {
            String key = it[0];
            HBox item = new HBox(4);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setPadding(new Insets(2, 4, 2, 4));
            item.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 4;");

            Canvas dot = new Canvas(8, 8);
            GraphicsContext g = dot.getGraphicsContext2D();
            g.setFill(Color.web(it[1]));
            g.fillOval(0, 0, 8, 8);
            g.setStroke(Color.WHITE);
            g.setLineWidth(0.6);
            g.strokeOval(0, 0, 8, 8);

            Label lbl = new Label();
            lbl.setStyle("-fx-font-size: 10px;");
            lbl.textProperty().bind(I18nManager.getInstance().createStringBinding("legend.caste." + key));
            lbl.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("legend.caste." + key + ".desc"));
            item.getChildren().addAll(dot, lbl);
            casteItemsFlow.getChildren().add(item);
        }

        pane.castesLegendBox = new VBox(4);
        pane.castesLegendBox.getChildren().addAll(new Separator(), titleCastes, casteItemsFlow);
        pane.castesLegendBox.setVisible(pane.isSimulationMode);
        pane.castesLegendBox.setManaged(pane.isSimulationMode);
        panel.getChildren().add(pane.castesLegendBox);

        return panel;
    }
}
