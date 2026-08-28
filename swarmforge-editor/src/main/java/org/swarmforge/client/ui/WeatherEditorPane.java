/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.NotificationOverlay;
import org.swarmforge.client.util.ThemeManager;

/**
 * Weather and Climate Editor - Realistic Geographic & Atmospheric Climate System.
 * Features Real-World Location Search & Open-Meteo Geocoding, Vegetation Cover estimation,
 * Latitude/Longitude/Altitude Solar Photoperiod, Barometric Pressure, Soil Thermal Inertia,
 * Sandstorm/Duststorm flight impairment, Lightning & Convective Fire Disasters, and 12-Month Interactive Curves.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class WeatherEditorPane extends BorderPane {

    private static final String[] MONTH_KEYS = {
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    private static final int[] MONTH_MID_DAYS = { 15, 45, 75, 105, 135, 165, 195, 225, 255, 285, 315, 345 };

    // Parameter Tabs
    public enum ParameterType {
        TEMPERATURE("🌡 Température (°C)", -40, 50, "°C", 10),
        WIND("💨 Vent (km/h)", 0, 150, " km/h", 25),
        RAIN("🌧 Précipitations (mm)", 0, 500, " mm", 100),
        HUMIDITY("💧 Humidité Relative (%)", 0, 100, "%", 20),
        OVERVIEW("📊 Vue d'Ensemble", 0, 100, "", 20);

        public final String title;
        public final double minVal;
        public final double maxVal;
        public final String unit;
        public final double tickStep;

        ParameterType(String title, double minVal, double maxVal, String unit, double tickStep) {
            this.title = title;
            this.minVal = minVal;
            this.maxVal = maxVal;
            this.unit = unit;
            this.tickStep = tickStep;
        }
    }

    // 12 Monthly Values Arrays
    private final double[] tempMin = new double[12];
    private final double[] tempAvg = new double[12];
    private final double[] tempMax = new double[12];

    private final double[] windMin = new double[12];
    private final double[] windAvg = new double[12];
    private final double[] windMax = new double[12];

    private final double[] rainMin = new double[12];
    private final double[] rainAvg = new double[12];
    private final double[] rainMax = new double[12];

    private final double[] humidityMin = new double[12];
    private final double[] humidityAvg = new double[12];
    private final double[] humidityMax = new double[12];

    // Geographic & Physical Atmospheric Controls
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

    // Daylight Hours Calculated per Month (Photoperiod)
    private final double[] daylightHours = new double[12];
    private Label[] daylightLabels = new Label[12];

    // Presets Manager
    private ComboBox<String> presetsCombo;
    private final WeatherPresetManager presetMgr = new WeatherPresetManager();
    private Consumer<Map<String, Object>> onApplyCallback;

    // Active Curve View
    private ParameterType activeParam = ParameterType.TEMPERATURE;
    private ResizableCanvas curveCanvas;
    private GraphicsContext gc;
    private ToggleGroup paramToggleGroup;

    // Drag / Hover Interaction state
    private int draggedMonth = -1;
    private int draggedCurve = -1; // 0 = Min, 1 = Avg, 2 = Max
    private String hoverInfoText = "";

    // Spinners Grid for numerical editing
    private final Spinner<Double>[] minSpinners = new Spinner[12];
    private final Spinner<Double>[] avgSpinners = new Spinner[12];
    private final Spinner<Double>[] maxSpinners = new Spinner[12];
    private boolean updatingSpinners = false;

    // Physical Coherence Banner
    private Label coherenceStatusBadge;
    private VBox coherenceDetailsBox;

    // Disaster Probabilities Settings
    private final Map<String, Slider> disasterProbabilities = new HashMap<>();

    private boolean isDirty = false;
    private boolean isUpdatingFields = false;
    private String lastSelectedPreset = null;

    public boolean isDirty() {
        return isDirty;
    }

    public boolean promptUnsavedChanges() {
        if (!isDirty) return true;
        I18nManager i18n = I18nManager.getInstance();
        Alert alert = ThemeManager.createAlert(
            Alert.AlertType.CONFIRMATION,
            "Vous avez des modifications non enregistrées dans l'Éditeur Météo & Climat. Voulez-vous enregistrer vos modifications avant de continuer ?"
        );
        alert.setTitle("Modifications non enregistrées");
        alert.setHeaderText("Quitter l'éditeur météo ?");

        ButtonType btnSave = new ButtonType(i18n.get("common.btn.save", "Enregistrer"), ButtonBar.ButtonData.OK_DONE);
        ButtonType btnDiscard = new ButtonType("Abandonner", ButtonBar.ButtonData.OTHER);
        ButtonType btnCancel = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnSave, btnDiscard, btnCancel);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == btnSave) {
            doSavePreset();
            return !isDirty;
        } else if (result.isPresent() && result.get() == btnDiscard) {
            isDirty = false;
            return true;
        }
        return false;
    }

    public WeatherEditorPane() {
        setTop(buildHeader());

        // Center: Scrollable main content
        VBox mainContent = new VBox(15);
        mainContent.setPadding(new Insets(10, 15, 15, 15));

        // Section 1: Geographic & Real Weather Fetcher (City Search, Open-Meteo, Latitude, Longitude, Altitude, Daylight, Vegetation)
        TitledPane geoSection = buildGeographicSection();

        // Section 2: Atmosphere & Soil Microclimate (Pressure, Wind Direction, Soil Thermal Inertia)
        TitledPane atmoSection = buildAtmosphereSoilSection();

        // Section 3: Monthly Interactive Curves Chart & Editor
        VBox curvesSection = buildCurvesSection();

        // Section 4: Physical Coherence Checks
        TitledPane coherenceSection = buildCoherenceSection();

        // Section 5: Natural Disasters (Fire, Lightning, Sandstorm flight impairment, Flood, Tornado, Freeze)
        TitledPane disasterPane = createDisastersSection();

        mainContent.getChildren().addAll(geoSection, atmoSection, curvesSection, coherenceSection, disasterPane);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        setCenter(scrollPane);

        attachUserChangeListeners();

        // Load default preset
        refreshPresetsCombo();
        if (!presetsCombo.getItems().isEmpty()) {
            presetsCombo.getSelectionModel().selectFirst();
            String first = presetsCombo.getValue();
            lastSelectedPreset = first;
            if (presetMgr.contains(first)) {
                applyPresetConfig(presetMgr.get(first));
            }
        } else {
            applyDefaultValues();
        }

        ThemeManager.getInstance().currentThemeProperty().addListener((obs, oldTheme, newTheme) -> redrawCurves());
    }

    // ── Header Bar ─────────────────────────────────────────────────────────────
    private VBox buildHeader() {
        I18nManager i18n = I18nManager.getInstance();
        VBox v = new VBox(6);
        v.setPadding(new Insets(8, 10, 5, 10));

        HBox r = new HBox(8);
        r.setAlignment(Pos.CENTER_LEFT);

        Label t = new Label();
        t.textProperty().bind(i18n.createStringBinding("weather.title"));
        t.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label lp = new Label();
        lp.textProperty().bind(i18n.createStringBinding("preset.label"));
        lp.setStyle("-fx-font-weight: bold;");
        lp.setGraphic(new FontIcon(Feather.SLIDERS));

        presetsCombo = new ComboBox<>();
        presetsCombo.setEditable(true);
        presetsCombo.setPrefWidth(210);
        presetsCombo.promptTextProperty().bind(i18n.createStringBinding("preset.prompt"));
        presetsCombo.setTooltip(new Tooltip("Select a pre-configured climate profile (Mediterranean, Tropical, Arid, Boreal, etc.)."));
        presetsCombo.setOnAction(e -> {
            if (isUpdatingFields) return;
            String s = presetsCombo.getValue();
            if (s == null || s.equals(lastSelectedPreset)) return;

            if (isDirty) {
                Alert alert = org.swarmforge.client.util.ThemeManager.createAlert(
                    Alert.AlertType.CONFIRMATION,
                    "Warning: You have unsaved modifications in the current climate profile.\n\nDo you want to load preset '" + s + "' and discard changes?"
                );
                alert.setTitle(I18nManager.getInstance().get("common.dialog.unsaved"));
                alert.setHeaderText("Climate Profile Change");
                java.util.Optional<ButtonType> res = alert.showAndWait();
                if (res.isEmpty() || res.get() != ButtonType.OK) {
                    isUpdatingFields = true;
                    try {
                        presetsCombo.setValue(lastSelectedPreset);
                    } finally {
                        isUpdatingFields = false;
                    }
                    return;
                }
            }

            if (presetMgr.contains(s)) {
                lastSelectedPreset = s;
                applyPresetConfig(presetMgr.get(s));
            }
        });

        Button bAdd = new Button();
        bAdd.setGraphic(new FontIcon(Feather.SAVE));
        bAdd.getStyleClass().add("btn-secondary");
        bAdd.textProperty().bind(i18n.createStringBinding("preset.save"));
        bAdd.setTooltip(new Tooltip("Save current climate configuration as new preset."));
        bAdd.setOnAction(e -> doSavePreset());

        Button bDel = new Button();
        bDel.setGraphic(new FontIcon(Feather.TRASH_2));
        bDel.getStyleClass().add("btn-danger");
        bDel.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold;");
        bDel.textProperty().bind(i18n.createStringBinding("preset.delete"));
        bDel.setTooltip(new Tooltip("Delete selected climate profile."));
        bDel.setOnAction(e -> doDeletePreset());

        Button bExp = new Button();
        bExp.setGraphic(new FontIcon(Feather.DOWNLOAD));
        bExp.getStyleClass().add("btn-secondary");
        bExp.textProperty().bind(i18n.createStringBinding("preset.export"));
        bExp.setTooltip(new Tooltip("Export climate profile to JSON format."));
        bExp.setOnAction(e -> doExport());

        Button bImp = new Button();
        bImp.setGraphic(new FontIcon(Feather.UPLOAD));
        bImp.getStyleClass().add("btn-secondary");
        bImp.textProperty().bind(i18n.createStringBinding("preset.import"));
        bImp.setTooltip(new Tooltip("Import JSON climate configuration file."));
        bImp.setOnAction(e -> doImport());

        r.getChildren().addAll(t, sp, lp, presetsCombo, bAdd, bDel,
                new Separator(Orientation.VERTICAL), bExp, bImp);

        v.getChildren().addAll(r, new Separator());
        return v;
    }

    private void doDeletePreset() {
        String sel = presetsCombo.getValue();
        if (sel == null || sel.isEmpty()) return;

        I18nManager i18n = I18nManager.getInstance();
        Alert confirmAlert = org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.CONFIRMATION, String.format(i18n.get("preset.delete.confirm"), sel));
        confirmAlert.setTitle(i18n.get("preset.delete.title"));
        confirmAlert.setHeaderText("Supprimer le Profil Climat");

        confirmAlert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                presetMgr.delete(sel);
                refreshPresetsCombo();
                if (!presetsCombo.getItems().isEmpty()) {
                    presetsCombo.getSelectionModel().selectFirst();
                } else {
                    presetsCombo.getSelectionModel().clearSelection();
                }
                NotificationOverlay.show(this, "Preset climat supprimé.", NotificationOverlay.NotificationType.INFO);
            }
        });
    }


    private void refreshPresetsCombo() {
        String cur = presetsCombo.getValue();
        presetsCombo.getItems().setAll(presetMgr.names());
        FXCollections.sort(presetsCombo.getItems());
        if (cur != null && presetsCombo.getItems().contains(cur)) {
            presetsCombo.setValue(cur);
        }
    }

    // ── Geographic & Real Weather Search Section ───────────────────────────────

    private TitledPane buildGeographicSection() {
        I18nManager i18n = I18nManager.getInstance();
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        // Row 1: Real-World Open-Meteo Geocoding Search Bar
        HBox searchRow = new HBox(10);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        Label lblSearch = new Label();
        lblSearch.textProperty().bind(i18n.createStringBinding("weather.geo.search_label"));
        lblSearch.setStyle("-fx-font-weight:bold;");

        citySearchField = new TextField("Paris");
        citySearchField.promptTextProperty().bind(i18n.createStringBinding("weather.geo.search_prompt"));
        citySearchField.setPrefWidth(220);
        citySearchField.setOnAction(e -> fetchRealWeather(citySearchField.getText()));

        Button btnFetch = new Button();
        btnFetch.getStyleClass().add("btn-primary");
        btnFetch.textProperty().bind(i18n.createStringBinding("weather.geo.search_btn"));
        btnFetch.setOnAction(e -> fetchRealWeather(citySearchField.getText()));

        geoStatusLabel = new Label(I18nManager.getInstance().get("weather.hint.city"));
        geoStatusLabel.setStyle("-fx-text-fill:#aaa;-fx-font-size:11;");

        searchRow.getChildren().addAll(lblSearch, citySearchField, btnFetch, geoStatusLabel);

        // Row 2: Latitude, Longitude, Altitude & Vegetation Cover
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);

        Label lblLat = new Label(I18nManager.getInstance().get("weather.geo.lat"));
        lblLat.setStyle("-fx-font-weight:bold;");
        latSpinner = new Spinner<>(-90.0, 90.0, 48.8, 0.5);
        latSpinner.setEditable(true);
        latSpinner.setPrefWidth(100);
        latSpinner.valueProperty().addListener((o, a, b) -> {
            updatePhotoperiod();
            updateAltitudeLapseRate();
            updateVegetationEstimate();
            updateCoherenceStatus();
        });

        Label lblLon = new Label(I18nManager.getInstance().get("weather.geo.lon"));
        lblLon.setStyle("-fx-font-weight:bold;");
        lonSpinner = new Spinner<>(-180.0, 180.0, 2.35, 0.5);
        lonSpinner.setEditable(true);
        lonSpinner.setPrefWidth(100);

        Label lblAlt = new Label(I18nManager.getInstance().get("weather.geo.alt"));
        lblAlt.setStyle("-fx-font-weight:bold;");
        altSpinner = new Spinner<>(0.0, 4000.0, 100.0, 50.0);
        altSpinner.setEditable(true);
        altSpinner.setPrefWidth(110);
        altSpinner.valueProperty().addListener((o, a, b) -> {
            updateAltitudeLapseRate();
            updateCoherenceStatus();
        });

        Label lblVegTitle = new Label();
        lblVegTitle.textProperty().bind(i18n.createStringBinding("weather.geo.veg_label"));
        lblVegTitle.setStyle("-fx-font-weight:bold;");

        vegCoverLabel = new Label("🌳 Forêt Tempérée Décidue (Temperate Deciduous Forest)");
        vegCoverLabel.setStyle("-fx-text-fill:#28a745;-fx-font-weight:bold;");

        grid.add(lblLat, 0, 0);
        grid.add(latSpinner, 1, 0);
        grid.add(lblLon, 2, 0);
        grid.add(lonSpinner, 3, 0);
        grid.add(lblAlt, 4, 0);
        grid.add(altSpinner, 5, 0);

        grid.add(lblVegTitle, 0, 1);
        grid.add(vegCoverLabel, 1, 1, 5, 1);

        // Row 3: Photoperiod (Daylight Hours per Month) Preview Box
        VBox photoBox = new VBox(6);
        photoBox.setPadding(new Insets(8));
        photoBox.getStyleClass().add("card-pane");

        Label photoTitle = new Label("☀️ Photopériodisme Calculé (Heures d'ensoleillement théoriques / jour) :");
        photoTitle.getStyleClass().add("card-title");
        photoTitle.setStyle("-fx-font-size: 11px;");

        HBox hoursBox = new HBox(8);
        hoursBox.setAlignment(Pos.CENTER_LEFT);

        for (int m = 0; m < 12; m++) {
            VBox col = new VBox(2);
            col.setAlignment(Pos.CENTER);
            Label mLbl = new Label(MONTH_KEYS[m]);
            mLbl.setStyle("-fx-font-size:9;-fx-text-fill:#aaa;");
            daylightLabels[m] = new Label("12.0h");
            daylightLabels[m].setStyle("-fx-font-size:10;-fx-font-weight:bold;-fx-text-fill:#ffc107;");
            col.getChildren().addAll(mLbl, daylightLabels[m]);
            hoursBox.getChildren().add(col);
        }

        photoBox.getChildren().addAll(photoTitle, hoursBox);

        container.getChildren().addAll(searchRow, new Separator(), grid, photoBox);

        TitledPane pane = new TitledPane();
        pane.textProperty().bind(i18n.createStringBinding("weather.geo.title"));
        pane.setContent(container);
        styleTitledPane(pane);
        pane.setExpanded(true);

        updatePhotoperiod();
        return pane;
    }

    private void fetchRealWeather(String cityQuery) {
        if (cityQuery == null || cityQuery.isBlank()) return;
        I18nManager i18n = I18nManager.getInstance();

        geoStatusLabel.setText(i18n.get("weather.geo.status_fetching", cityQuery));
        geoStatusLabel.setStyle("-fx-text-fill:#ffc107;-fx-font-size:11;");

        new Thread(() -> {
            try {
                // 1. Open-Meteo Geocoding API
                String geoUrlStr = "https://geocoding-api.open-meteo.com/v1/search?name="
                        + java.net.URLEncoder.encode(cityQuery, StandardCharsets.UTF_8)
                        + "&count=1&language=fr";

                String geoJson = readHttpUrl(geoUrlStr);
                com.fasterxml.jackson.databind.JsonNode root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(geoJson);

                if (!root.has("results") || root.get("results").isEmpty()) {
                    Platform.runLater(() -> {
                        geoStatusLabel.setText(i18n.get("weather.geo.status_error", "Lieu introuvable"));
                        geoStatusLabel.setStyle("-fx-text-fill:#ff4757;-fx-font-size:11;");
                        NotificationOverlay.show(this, "Lieu \"" + cityQuery + "\" introuvable.", NotificationOverlay.NotificationType.WARNING);
                    });
                    return;
                }

                com.fasterxml.jackson.databind.JsonNode loc = root.get("results").get(0);
                String name = loc.get("name").asText();
                double lat = loc.get("latitude").asDouble();
                double lon = loc.get("longitude").asDouble();
                double alt = loc.has("elevation") ? loc.get("elevation").asDouble() : 100.0;

                // 2. Fetch Open-Meteo Archive Climate Data (Daily parameters aggregated over 2023)
                String climateUrlStr = String.format(Locale.US,
                        "https://archive-api.open-meteo.com/v1/archive?latitude=%.4f&longitude=%.4f&start_date=2023-01-01&end_date=2023-12-31&daily=temperature_2m_max,temperature_2m_min,temperature_2m_mean,precipitation_sum,wind_speed_10m_max",
                        lat, lon);

                String climateJson = readHttpUrl(climateUrlStr);
                com.fasterxml.jackson.databind.JsonNode climateRoot = new com.fasterxml.jackson.databind.ObjectMapper().readTree(climateJson);

                if (climateRoot.has("daily")) {
                    com.fasterxml.jackson.databind.JsonNode dNode = climateRoot.get("daily");
                    com.fasterxml.jackson.databind.JsonNode timeN = dNode.get("time");
                    com.fasterxml.jackson.databind.JsonNode tMaxN = dNode.get("temperature_2m_max");
                    com.fasterxml.jackson.databind.JsonNode tMinN = dNode.get("temperature_2m_min");
                    com.fasterxml.jackson.databind.JsonNode tAvgN = dNode.get("temperature_2m_mean");
                    com.fasterxml.jackson.databind.JsonNode rainN = dNode.get("precipitation_sum");
                    com.fasterxml.jackson.databind.JsonNode windN = dNode.get("wind_speed_10m_max");

                    double[] monthlyTMaxSum = new double[12];
                    double[] monthlyTMinSum = new double[12];
                    double[] monthlyTAvgSum = new double[12];
                    double[] monthlyRainSum = new double[12];
                    double[] monthlyWindSum = new double[12];
                    int[] monthlyDaysCount = new int[12];

                    if (timeN != null && timeN.isArray()) {
                        for (int i = 0; i < timeN.size(); i++) {
                            String dateStr = timeN.get(i).asText();
                            if (dateStr.length() >= 7) {
                                int monthIndex = Integer.parseInt(dateStr.substring(5, 7)) - 1;
                                if (monthIndex >= 0 && monthIndex < 12) {
                                    monthlyDaysCount[monthIndex]++;
                                    if (tMaxN != null && i < tMaxN.size() && !tMaxN.get(i).isNull()) monthlyTMaxSum[monthIndex] += tMaxN.get(i).asDouble();
                                    if (tMinN != null && i < tMinN.size() && !tMinN.get(i).isNull()) monthlyTMinSum[monthIndex] += tMinN.get(i).asDouble();
                                    if (tAvgN != null && i < tAvgN.size() && !tAvgN.get(i).isNull()) monthlyTAvgSum[monthIndex] += tAvgN.get(i).asDouble();
                                    if (rainN != null && i < rainN.size() && !rainN.get(i).isNull()) monthlyRainSum[monthIndex] += rainN.get(i).asDouble();
                                    if (windN != null && i < windN.size() && !windN.get(i).isNull()) monthlyWindSum[monthIndex] += windN.get(i).asDouble();
                                }
                            }
                        }
                    }

                    for (int m = 0; m < 12; m++) {
                        int count = Math.max(1, monthlyDaysCount[m]);
                        tempMax[m] = monthlyTMaxSum[m] / count;
                        tempMin[m] = monthlyTMinSum[m] / count;
                        tempAvg[m] = monthlyTAvgSum[m] / count;

                        rainAvg[m] = monthlyRainSum[m];
                        rainMin[m] = Math.max(0, rainAvg[m] * 0.4);
                        rainMax[m] = rainAvg[m] * 1.6;

                        windAvg[m] = monthlyWindSum[m] / count;
                        windMin[m] = Math.max(0, windAvg[m] * 0.4);
                        windMax[m] = windAvg[m] * 1.8;

                        double approxHum = Math.min(95, Math.max(25, 60.0 + (rainAvg[m] > 50 ? 15 : -10) - (tempAvg[m] > 25 ? 15 : 0)));
                        humidityAvg[m] = approxHum;
                        humidityMin[m] = Math.max(10, approxHum - 15);
                        humidityMax[m] = Math.min(100, approxHum + 15);
                    }
                }

                Platform.runLater(() -> {
                    latSpinner.getValueFactory().setValue(lat);
                    lonSpinner.getValueFactory().setValue(lon);
                    altSpinner.getValueFactory().setValue(alt);

                    updatePhotoperiod();
                    updateAltitudeLapseRate();
                    updateVegetationEstimate();
                    updateSpinnersForActiveParam();
                    syncAndValidate();
                    redrawCurves();

                    geoStatusLabel.setText(i18n.get("weather.geo.status_success", name, String.format(Locale.US, "%.2f", lat), String.format(Locale.US, "%.2f", lon), (int) alt));
                    geoStatusLabel.setStyle("-fx-text-fill:#28a745;-fx-font-weight:bold;-fx-font-size:11;");

                    NotificationOverlay.show(this, "Climat réel de " + name + " (" + String.format(Locale.US, "%.2f°N, %.2f°E", lat, lon) + ") chargé avec succès !", NotificationOverlay.NotificationType.SUCCESS);
                });

            } catch (Exception ex) {
                Platform.runLater(() -> {
                    geoStatusLabel.setText(i18n.get("weather.geo.status_error", ex.getMessage()));
                    geoStatusLabel.setStyle("-fx-text-fill:#ff4757;-fx-font-size:11;");
                    NotificationOverlay.show(this, "Erreur météo: " + ex.getMessage(), NotificationOverlay.NotificationType.ERROR);
                });
            }
        }).start();
    }

    private String readHttpUrl(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(6000);
        conn.setReadTimeout(6000);
        int status = conn.getResponseCode();
        java.io.InputStream stream = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
        if (stream == null) {
            throw new java.io.IOException("Erreur HTTP " + status + " pour " + urlStr);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            if (status >= 400) {
                throw new java.io.IOException("Open-Meteo API (" + status + "): " + sb.toString());
            }
            return sb.toString();
        }
    }

    private void updateVegetationEstimate() {
        if (vegCoverLabel == null || latSpinner == null) return;
        double lat = latSpinner.getValue();
        double annualTemp = getAvg(tempAvg);
        double annualRain = getSum(rainAvg);

        org.swarmforge.core.domain.BioclimaticZone zone = org.swarmforge.core.domain.BioclimaticZone.classify(lat, annualTemp, annualRain);
        vegCoverLabel.setText(zone.getDisplayName() + " — Adéquation: " + zone.getRecommendedInsectSpecies());
    }

    private void updatePhotoperiod() {
        if (latSpinner == null) return;
        double latRad = Math.toRadians(latSpinner.getValue());

        for (int m = 0; m < 12; m++) {
            int dayOfYear = MONTH_MID_DAYS[m];
            double declinationRad = Math.toRadians(23.45 * Math.sin(Math.toRadians(360.0 / 365.0 * (dayOfYear - 81))));
            double tanProduct = -Math.tan(latRad) * Math.tan(declinationRad);
            double hours;
            if (tanProduct >= 1.0) {
                hours = 0.0;
            } else if (tanProduct <= -1.0) {
                hours = 24.0;
            } else {
                double hourAngle = Math.acos(tanProduct);
                hours = Math.toDegrees(hourAngle) / 15.0 * 2.0;
            }

            daylightHours[m] = Math.round(hours * 10.0) / 10.0;
            if (daylightLabels[m] != null) {
                daylightLabels[m].setText(String.format("%.1fh", daylightHours[m]));
            }
        }
    }

    private void updateAltitudeLapseRate() {
        if (altSpinner == null || pressureSpinner == null) return;
        double alt = altSpinner.getValue();
        double press = 1013.25 * Math.pow(1.0 - 2.25577e-5 * alt, 5.25588);
        pressureSpinner.getValueFactory().setValue(Math.round(press * 10.0) / 10.0);
    }

    // ── Atmosphere & Soil Microclimate Card ────────────────────────────────────

    private TitledPane buildAtmosphereSoilSection() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setMinWidth(240);
        c1.setPrefWidth(260);
        c1.setHgrow(Priority.NEVER);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        // Barometric Pressure (hPa)
        pressureSpinner = new Spinner<>(800.0, 1100.0, 1013.2, 1.0);
        pressureSpinner.setEditable(true);
        pressureSpinner.setPrefWidth(100);
        Label lblPress = createTooltipLabel("🎚️ Pression Atmosphérique (hPa) :", "Pression barométrique de base calculée selon le modèle OACI de l'atmosphère standard en hectopascals (hPa).", pressureSpinner, "Pression Atmosphérique");

        windDirCombo = new ComboBox<>();
        windDirCombo.getItems().addAll("E", "N", "NE", "NW", "S", "SE", "SW", "W");
        ComboBoxTooltipHelper.setupDescriptiveComboBox(windDirCombo,
            val -> switch (val) {
                case "N" -> "⬆️ Nord (N)";
                case "NE" -> "↗️ Nord-Est (NE)";
                case "E" -> "➡️ Est (E)";
                case "SE" -> "↘️ Sud-Est (SE)";
                case "S" -> "⬇️ Sud (S)";
                case "SW" -> "↙️ Sud-Ouest (SW)";
                case "W" -> "⬅️ Ouest (W)";
                case "NW" -> "↖️ Nord-Ouest (NW)";
                default -> val;
            },
            val -> switch (val) {
                case "N" -> "Vent venant du Nord (0°). Air généralement froid et sec.";
                case "NE" -> "Vent venant du Nord-Est (45°). Flux continental froid/frais.";
                case "E" -> "Vent venant de l'Est (90°). Vent d'Est sec.";
                case "SE" -> "Vent venant du Sud-Est (135°). Vent chaud et continental.";
                case "S" -> "Vent venant du Sud (180°). Masse d'air chaude et tropicale.";
                case "SW" -> "Vent venant du Sud-Ouest (225°). Flux océanique doux et humide.";
                case "W" -> "Vent venant de l'Ouest (270°). Vent d'Ouest maritime instable.";
                case "NW" -> "Vent venant du Nord-Ouest (315°). Air maritime frais et pluvieux.";
                default -> "";
            }
        );
        windDirCombo.setValue("SW");
        windDirCombo.setPrefWidth(80);
        Label lblWindDir = createTooltipLabel("🧭 Direction du Vent Dominant :", "Direction dominante des masses d'air agissant sur la dispersion des plumes phéromonales et l'envol des sexués.", windDirCombo);

        soilInertiaSpinner = new Spinner<>(0.5, 14.0, 3.0, 0.5);
        soilInertiaSpinner.setEditable(true);
        soilInertiaSpinner.setPrefWidth(90);
        Label lblSoilInertia = createTooltipLabel("🧱 Inertie Thermique du Sol (Jours) :", "Déphasage thermique en jours de retard entre la température moyenne de l'air et la température du sol.", soilInertiaSpinner, "Sol & Microclimat");

        depthAttenSpinner = new Spinner<>(0.0, 1.0, 0.85, 0.05);
        depthAttenSpinner.setEditable(true);
        depthAttenSpinner.setPrefWidth(90);
        Label lblAtten = createTooltipLabel("🕳️ Atténuation en Profondeur (0-1) :", "Facteur d'atténuation de l'amplitude thermique quotidienne/annuelle dans les galeries souterraines du nid.", depthAttenSpinner);

        grid.add(lblPress, 0, 0);
        grid.add(pressureSpinner, 1, 0);
        grid.add(lblWindDir, 2, 0);
        grid.add(windDirCombo, 3, 0);
        grid.add(lblSoilInertia, 0, 1);
        grid.add(soilInertiaSpinner, 1, 1);
        grid.add(lblAtten, 2, 1);
        grid.add(depthAttenSpinner, 3, 1);

        TitledPane pane = new TitledPane("🌫️ Atmosphère, Vents & Microclimat Souterrain du Sol", grid);
        styleTitledPane(pane);
        pane.setExpanded(true);
        return pane;
    }

    private Label createTooltipLabel(String text, String tooltipText) {
        return createTooltipLabel(text, tooltipText, (javafx.scene.Node) null, null);
    }

    private Label createTooltipLabel(String text, String tooltipText, javafx.scene.Node targetControl) {
        return createTooltipLabel(text, tooltipText, targetControl, null);
    }

    private Label createTooltipLabel(String text, String tooltipText, String glossaryTerm) {
        return createTooltipLabel(text, tooltipText, null, glossaryTerm);
    }

    private Label createTooltipLabel(String text, String tooltipText, javafx.scene.Node targetControl, String glossaryTerm) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold;");
        if (tooltipText != null && !tooltipText.isEmpty()) {
            Tooltip tt = new Tooltip(tooltipText);
            tt.setMaxWidth(380);
            tt.setWrapText(true);
            l.setTooltip(tt);
            if (targetControl != null) {
                if (targetControl instanceof Control) {
                    ((Control) targetControl).setTooltip(tt);
                } else {
                    Tooltip.install(targetControl, tt);
                }
            }
        }
        if (glossaryTerm != null && !glossaryTerm.isEmpty()) {
            l.setStyle("-fx-font-weight: bold; -fx-text-fill: #38bdf8; -fx-underline: true; -fx-cursor: hand;");
            l.setOnMouseClicked(e -> GlossaryDialog.show(glossaryTerm));
        } else {
            l.setStyle("-fx-font-weight: bold;");
        }
        return l;
    }

    // ── Curves Section & Canvas Chart Editor ──────────────────────────────────

    private VBox buildCurvesSection() {
        I18nManager i18n = I18nManager.getInstance();
        VBox container = new VBox(8);
        container.setPadding(new Insets(10));
        container.getStyleClass().add("card-pane");

        Label title = new Label();
        title.textProperty().bind(i18n.createStringBinding("weather.curves.title"));
        title.getStyleClass().add("card-title");
        title.setStyle("-fx-font-size: 14px;");

        // Parameter selector toolbar
        HBox paramBar = new HBox(8);
        paramBar.setAlignment(Pos.CENTER_LEFT);
        paramToggleGroup = new ToggleGroup();

        for (ParameterType type : ParameterType.values()) {
            ToggleButton tb = new ToggleButton(type.title);
            tb.setToggleGroup(paramToggleGroup);
            tb.setUserData(type);
            tb.setStyle("-fx-font-size:11;-fx-font-weight:bold;");
            if (type == ParameterType.TEMPERATURE) tb.setSelected(true);
            paramBar.getChildren().add(tb);
        }

        paramToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                activeParam = (ParameterType) newVal.getUserData();
                updateSpinnersForActiveParam();
                redrawCurves();
            }
        });

        // Canvas setup
        curveCanvas = new ResizableCanvas(760, 260);
        gc = curveCanvas.getGraphicsContext2D();

        setupCanvasInteractions();

        StackPane canvasHolder = new StackPane(curveCanvas);
        canvasHolder.getStyleClass().add("chart-holder");
        canvasHolder.setMinHeight(200);
        canvasHolder.setPrefHeight(260);
        canvasHolder.setMaxHeight(260);

        // Bind canvas dimensions and clip to container so it cannot bleed over other elements
        curveCanvas.widthProperty().bind(canvasHolder.widthProperty());
        curveCanvas.heightProperty().bind(canvasHolder.heightProperty());
        curveCanvas.widthProperty().addListener((obs, oldV, newV) -> redrawCurves());
        curveCanvas.heightProperty().addListener((obs, oldV, newV) -> redrawCurves());

        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.widthProperty().bind(canvasHolder.widthProperty());
        clip.heightProperty().bind(canvasHolder.heightProperty());
        canvasHolder.setClip(clip);

        // Legend bar
        HBox legendBar = buildCurvesLegendBar();

        // Monthly Spinner Table Grid inside a scroll pane for responsive fitting
        GridPane spinnerGrid = buildMonthlySpinnersGrid();
        ScrollPane spinnerScroll = new ScrollPane(spinnerGrid);
        spinnerScroll.setFitToWidth(true);
        spinnerScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spinnerScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");

        container.getChildren().addAll(title, paramBar, canvasHolder, legendBar, spinnerScroll);
        return container;
    }

    private HBox buildCurvesLegendBar() {
        I18nManager i18n = I18nManager.getInstance();
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(4, 8, 4, 8));

        Label minLbl = createDotLabel(i18n.get("weather.curves.min") + " (Cyan)", Color.web("#00d4ff"));
        Label avgLbl = createDotLabel(i18n.get("weather.curves.avg") + " (Or)", Color.web("#ffc107"));
        Label maxLbl = createDotLabel(i18n.get("weather.curves.max") + " (Rouge)", Color.web("#ff4757"));

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label hint = new Label("💡 Cliquez et glissez sur les points du graphique pour modifier les courbes");
        hint.setStyle("-fx-text-fill:#888;-fx-font-size:10;-fx-font-style:italic;");

        bar.getChildren().addAll(minLbl, avgLbl, maxLbl, sp, hint);
        return bar;
    }

    private Label createDotLabel(String text, Color color) {
        Label l = new Label("● " + text);
        l.setStyle("-fx-text-fill: " + toHexString(color) + ";-fx-font-weight:bold;-fx-font-size:11;");
        return l;
    }

    private String toHexString(Color c) {
        return String.format("#%02X%02X%02X",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
    }

    private GridPane buildMonthlySpinnersGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(4);
        grid.setVgap(4);
        grid.setPadding(new Insets(6, 0, 0, 0));

        for (int m = 0; m < 12; m++) {
            Label mLbl = new Label(MONTH_KEYS[m]);
            mLbl.setAlignment(Pos.CENTER);
            mLbl.setStyle("-fx-font-size:10;-fx-font-weight:bold;-fx-text-fill:#aaa;-fx-min-width:55;");
            grid.add(mLbl, m, 0);

            // Max Spinner (Top)
            maxSpinners[m] = new Spinner<>(-100.0, 1000.0, 0.0, 1.0);
            styleSpinner(maxSpinners[m], "#ff4757");

            // Avg Spinner (Middle)
            avgSpinners[m] = new Spinner<>(-100.0, 1000.0, 0.0, 1.0);
            styleSpinner(avgSpinners[m], "#ffc107");

            // Min Spinner (Bottom)
            minSpinners[m] = new Spinner<>(-100.0, 1000.0, 0.0, 1.0);
            styleSpinner(minSpinners[m], "#00d4ff");

            final int monthIdx = m;
            maxSpinners[m].valueProperty().addListener((o, a, b) -> {
                if (updatingSpinners) return;
                setParamVal(activeParam, 2, monthIdx, b);
                syncAndValidate();
            });
            avgSpinners[m].valueProperty().addListener((o, a, b) -> {
                if (updatingSpinners) return;
                setParamVal(activeParam, 1, monthIdx, b);
                syncAndValidate();
            });
            minSpinners[m].valueProperty().addListener((o, a, b) -> {
                if (updatingSpinners) return;
                setParamVal(activeParam, 0, monthIdx, b);
                syncAndValidate();
            });

            grid.add(maxSpinners[m], m, 1);
            grid.add(avgSpinners[m], m, 2);
            grid.add(minSpinners[m], m, 3);
        }

        return grid;
    }

    private void styleSpinner(Spinner<Double> s, String textColor) {
        s.setPrefWidth(58);
        s.setEditable(true);
        s.setStyle("-fx-font-size:9;-fx-text-fill:" + textColor + ";");
    }

    private void updateSpinnersForActiveParam() {
        if (activeParam == ParameterType.OVERVIEW) return;
        updatingSpinners = true;

        double[] minArr = getArr(activeParam, 0);
        double[] avgArr = getArr(activeParam, 1);
        double[] maxArr = getArr(activeParam, 2);

        for (int m = 0; m < 12; m++) {
            minSpinners[m].getValueFactory().setValue(Math.round(minArr[m] * 10.0) / 10.0);
            avgSpinners[m].getValueFactory().setValue(Math.round(avgArr[m] * 10.0) / 10.0);
            maxSpinners[m].getValueFactory().setValue(Math.round(maxArr[m] * 10.0) / 10.0);
        }

        updatingSpinners = false;
    }

    // ── Canvas Rendering & Interaction ─────────────────────────────────────────

    private void setupCanvasInteractions() {
        curveCanvas.setOnMousePressed(e -> {
            if (activeParam == ParameterType.OVERVIEW) return;
            double mx = e.getX();
            double my = e.getY();

            double padL = 50, padR = 20, padT = 30, padB = 30;
            double w = curveCanvas.getWidth() - padL - padR;
            double h = curveCanvas.getHeight() - padT - padB;

            double monthStep = w / 11.0;
            int closestM = (int) Math.round((mx - padL) / monthStep);
            closestM = Math.max(0, Math.min(11, closestM));

            double xM = padL + closestM * monthStep;
            if (Math.abs(mx - xM) > 25) return;

            double[] minArr = getArr(activeParam, 0);
            double[] avgArr = getArr(activeParam, 1);
            double[] maxArr = getArr(activeParam, 2);

            double yMin = valToY(minArr[closestM], activeParam, padT, h);
            double yAvg = valToY(avgArr[closestM], activeParam, padT, h);
            double yMax = valToY(maxArr[closestM], activeParam, padT, h);

            double dMin = Math.abs(my - yMin);
            double dAvg = Math.abs(my - yAvg);
            double dMax = Math.abs(my - yMax);

            if (dMin <= dAvg && dMin <= dMax && dMin < 20) {
                draggedCurve = 0;
                draggedMonth = closestM;
            } else if (dAvg <= dMin && dAvg <= dMax && dAvg < 20) {
                draggedCurve = 1;
                draggedMonth = closestM;
            } else if (dMax < 20) {
                draggedCurve = 2;
                draggedMonth = closestM;
            }
        });

        curveCanvas.setOnMouseDragged(e -> {
            if (draggedMonth < 0 || activeParam == ParameterType.OVERVIEW) return;

            double padT = 30;
            double h = curveCanvas.getHeight() - 30 - 30;
            double newVal = yToVal(e.getY(), activeParam, padT, h);

            setParamVal(activeParam, draggedCurve, draggedMonth, newVal);
            syncAndValidate();
            updateSpinnersForActiveParam();
            redrawCurves();
        });

        curveCanvas.setOnMouseReleased(e -> {
            draggedMonth = -1;
            draggedCurve = -1;
        });

        curveCanvas.setOnMouseMoved(e -> {
            if (activeParam == ParameterType.OVERVIEW) return;
            double mx = e.getX();
            double padL = 50, padR = 20;
            double w = curveCanvas.getWidth() - padL - padR;
            double monthStep = w / 11.0;

            int m = (int) Math.round((mx - padL) / monthStep);
            if (m >= 0 && m < 12 && Math.abs(mx - (padL + m * monthStep)) < 25) {
                double[] minArr = getArr(activeParam, 0);
                double[] avgArr = getArr(activeParam, 1);
                double[] maxArr = getArr(activeParam, 2);
                String precipType = getPrecipitationType(m);
                hoverInfoText = String.format("%s (%s): Min=%.1f%s, Avg=%.1f%s, Max=%.1f%s",
                        MONTH_KEYS[m], precipType, minArr[m], activeParam.unit, avgArr[m], activeParam.unit, maxArr[m], activeParam.unit);
            } else {
                hoverInfoText = "";
            }
            redrawCurves();
        });

        curveCanvas.setOnMouseExited(e -> {
            hoverInfoText = "";
            redrawCurves();
        });
    }

    private String getPrecipitationType(int month) {
        if (tempMax[month] < 0) return "❄️ Neige";
        if (tempAvg[month] > 28 && rainAvg[month] > 150) return "🧊 Orages / Grêle";
        if (rainAvg[month] > 5) return "🌧 Pluie";
        return "☀️ Dégagé";
    }

    private void syncAndValidate() {
        updateVegetationEstimate();
        updateCoherenceStatus();
    }

    private void redrawCurves() {
        if (curveCanvas == null || gc == null || curveCanvas.getWidth() < 10 || curveCanvas.getHeight() < 10) return;
        double w = curveCanvas.getWidth();
        double h = curveCanvas.getHeight();

        boolean isDark = ThemeManager.getInstance().getCurrentTheme() == ThemeManager.Theme.DARK;
        Color bgCol = isDark ? Color.web("#18181b") : Color.web("#ffffff");
        Color gridCol = isDark ? Color.web("#3f3f46") : Color.web("#e2e8f0");
        Color textCol = isDark ? Color.web("#a1a1aa") : Color.web("#475569");
        Color tooltipBg = isDark ? Color.web("#27272a", 0.95) : Color.web("#ffffff", 0.95);
        Color tooltipText = isDark ? Color.web("#f4f4f5") : Color.web("#0f172a");

        gc.clearRect(0, 0, w, h);
        gc.setFill(bgCol);
        gc.fillRect(0, 0, w, h);

        if (activeParam == ParameterType.OVERVIEW) {
            drawOverviewMode(w, h);
            return;
        }

        double padL = 50, padR = 20, padT = 30, padB = 30;
        double chartW = w - padL - padR;
        double chartH = h - padT - padB;

        // Y-Grid and ticks
        gc.setStroke(gridCol);
        gc.setLineWidth(1.0);
        gc.setFont(javafx.scene.text.Font.font(10));
        gc.setFill(textCol);

        double range = activeParam.maxVal - activeParam.minVal;
        for (double v = activeParam.minVal; v <= activeParam.maxVal; v += activeParam.tickStep) {
            double y = padT + chartH * (1.0 - (v - activeParam.minVal) / range);
            gc.strokeLine(padL, y, padL + chartW, y);
            gc.fillText(String.format("%.0f%s", v, activeParam.unit), 5, y + 4);
        }

        // X-Grid & Month Labels
        double monthStep = chartW / 11.0;
        for (int m = 0; m < 12; m++) {
            double x = padL + m * monthStep;
            gc.strokeLine(x, padT, x, padT + chartH);
            gc.fillText(MONTH_KEYS[m], x - 10, h - 10);
        }

        double[] minArr = getArr(activeParam, 0);
        double[] avgArr = getArr(activeParam, 1);
        double[] maxArr = getArr(activeParam, 2);

        // Filled translucent band between Min & Max
        gc.setFill(Color.web("#0284c7", 0.12));
        gc.beginPath();
        for (int m = 0; m < 12; m++) {
            double x = padL + m * monthStep;
            double yMax = valToY(maxArr[m], activeParam, padT, chartH);
            if (m == 0) gc.moveTo(x, yMax);
            else gc.lineTo(x, yMax);
        }
        for (int m = 11; m >= 0; m--) {
            double x = padL + m * monthStep;
            double yMin = valToY(minArr[m], activeParam, padT, chartH);
            gc.lineTo(x, yMin);
        }
        gc.closePath();
        gc.fill();

        // Curves
        drawSingleCurve(minArr, Color.web("#0284c7"), padL, padT, monthStep, chartH);
        drawSingleCurve(avgArr, Color.web("#eab308"), padL, padT, monthStep, chartH);
        drawSingleCurve(maxArr, Color.web("#ef4444"), padL, padT, monthStep, chartH);

        // Interactive Points / Handles
        for (int m = 0; m < 12; m++) {
            double x = padL + m * monthStep;
            drawHandle(x, valToY(minArr[m], activeParam, padT, chartH), Color.web("#0284c7"), draggedMonth == m && draggedCurve == 0);
            drawHandle(x, valToY(avgArr[m], activeParam, padT, chartH), Color.web("#eab308"), draggedMonth == m && draggedCurve == 1);
            drawHandle(x, valToY(maxArr[m], activeParam, padT, chartH), Color.web("#ef4444"), draggedMonth == m && draggedCurve == 2);
        }

        // Hover Info Text
        if (!hoverInfoText.isEmpty()) {
            gc.setFill(tooltipBg);
            gc.setStroke(Color.web("#0284c7"));
            gc.fillRect(padL + 10, padT + 5, 290, 24);
            gc.strokeRect(padL + 10, padT + 5, 290, 24);

            gc.setFill(tooltipText);
            gc.setFont(javafx.scene.text.Font.font("System", 11));
            gc.fillText(hoverInfoText, padL + 18, padT + 21);
        }
    }

    private void drawOverviewMode(double w, double h) {
        ParameterType[] params = { ParameterType.TEMPERATURE, ParameterType.WIND, ParameterType.RAIN, ParameterType.HUMIDITY };
        double subW = (w - 30) / 2.0;
        double subH = (h - 30) / 2.0;

        boolean isDark = ThemeManager.getInstance().getCurrentTheme() == ThemeManager.Theme.DARK;
        Color subBg = isDark ? Color.web("#27272a") : Color.web("#f1f5f9");
        Color subBorder = isDark ? Color.web("#3f3f46") : Color.web("#cbd5e1");
        Color titleCol = isDark ? Color.web("#f4f4f5") : Color.web("#0f172a");

        for (int i = 0; i < 4; i++) {
            double rx = 10 + (i % 2) * (subW + 10);
            double ry = 10 + (i / 2) * (subH + 10);

            gc.setFill(subBg);
            gc.fillRect(rx, ry, subW, subH);
            gc.setStroke(subBorder);
            gc.strokeRect(rx, ry, subW, subH);

            gc.setFill(titleCol);
            gc.setFont(javafx.scene.text.Font.font(11));
            gc.fillText(params[i].title, rx + 8, ry + 16);

            double padL = rx + 30;
            double padT = ry + 25;
            double cW = subW - 40;
            double cH = subH - 35;
            double mStep = cW / 11.0;

            double[] minArr = getArr(params[i], 0);
            double[] avgArr = getArr(params[i], 1);
            double[] maxArr = getArr(params[i], 2);

            drawSingleCurve(minArr, Color.web("#0284c7"), padL, padT, mStep, cH, params[i]);
            drawSingleCurve(avgArr, Color.web("#eab308"), padL, padT, mStep, cH, params[i]);
            drawSingleCurve(maxArr, Color.web("#ef4444"), padL, padT, mStep, cH, params[i]);
        }
    }

    private void drawSingleCurve(double[] arr, Color col, double padL, double padT, double mStep, double chartH) {
        drawSingleCurve(arr, col, padL, padT, mStep, chartH, activeParam);
    }

    private void drawSingleCurve(double[] arr, Color col, double padL, double padT, double mStep, double chartH, ParameterType param) {
        gc.setStroke(col);
        gc.setLineWidth(2.0);
        gc.beginPath();
        for (int m = 0; m < 12; m++) {
            double x = padL + m * mStep;
            double y = valToY(arr[m], param, padT, chartH);
            if (m == 0) gc.moveTo(x, y);
            else gc.lineTo(x, y);
        }
        gc.stroke();
    }

    private void drawHandle(double x, double y, Color color, boolean isHovered) {
        gc.setFill(isHovered ? Color.WHITE : color);
        gc.fillOval(x - 4, y - 4, 8, 8);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.0);
        gc.strokeOval(x - 4, y - 4, 8, 8);
    }

    private double valToY(double val, ParameterType param, double padT, double chartH) {
        double range = param.maxVal - param.minVal;
        double clamped = Math.max(param.minVal, Math.min(param.maxVal, val));
        return padT + chartH * (1.0 - (clamped - param.minVal) / range);
    }

    private double yToVal(double y, ParameterType param, double padT, double chartH) {
        double range = param.maxVal - param.minVal;
        double norm = 1.0 - (y - padT) / chartH;
        norm = Math.max(0.0, Math.min(1.0, norm));
        return param.minVal + norm * range;
    }

    private double[] getArr(ParameterType param, int curve) {
        return switch (param) {
            case TEMPERATURE -> curve == 0 ? tempMin : (curve == 1 ? tempAvg : tempMax);
            case WIND -> curve == 0 ? windMin : (curve == 1 ? windAvg : windMax);
            case RAIN -> curve == 0 ? rainMin : (curve == 1 ? rainAvg : rainMax);
            case HUMIDITY -> curve == 0 ? humidityMin : (curve == 1 ? humidityAvg : humidityMax);
            case OVERVIEW -> tempAvg;
        };
    }

    private void setParamVal(ParameterType param, int curve, int month, double val) {
        double[] arr = getArr(param, curve);
        arr[month] = val;
    }

    // ── Physical Coherence Engine ─────────────────────────────────────────────

    private TitledPane buildCoherenceSection() {
        I18nManager i18n = I18nManager.getInstance();
        VBox box = new VBox(8);
        box.setPadding(new Insets(10));

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

        box.getChildren().addAll(banner, new Separator(), coherenceDetailsBox);

        TitledPane pane = new TitledPane();
        pane.textProperty().bind(i18n.createStringBinding("weather.coherence.title"));
        pane.setContent(box);
        pane.setExpanded(true);
        styleTitledPane(pane);

        updateCoherenceStatus();
        return pane;
    }

    private void updateCoherenceStatus() {
        if (coherenceStatusBadge == null) return;
        I18nManager i18n = I18nManager.getInstance();

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        for (int m = 0; m < 12; m++) {
            String mName = MONTH_KEYS[m];

            checkOrder("Température", mName, tempMin[m], tempAvg[m], tempMax[m], errors);
            checkOrder("Vent", mName, windMin[m], windAvg[m], windMax[m], errors);
            checkOrder("Pluviométrie", mName, rainMin[m], rainAvg[m], rainMax[m], errors);
            checkOrder("Humidité", mName, humidityMin[m], humidityAvg[m], humidityMax[m], errors);

            if (rainAvg[m] > 120 && humidityAvg[m] < 35) {
                warnings.add(String.format("⚠️ %s : Forte pluie (%.0f mm) mais air très sec (%.0f%% HR). Risque d'évaporation (Virga).",
                        mName, rainAvg[m], humidityAvg[m]));
            }

            if (tempMax[m] < 0 && rainAvg[m] > 10) {
                warnings.add(String.format("❄️ %s : Pluies sous 0°C (Max %.1f°C). Converti en précipitation neigeuse (Neige).",
                        mName, tempMax[m]));
            }

            if (tempAvg[m] > 38 && humidityAvg[m] > 80) {
                warnings.add(String.format("🔥 %s : Température extrême (%.1f°C) et forte humidité (%.0f%% HR). Indice de chaleur critique.",
                        mName, tempAvg[m], humidityAvg[m]));
            }

            // Sandstorm warning (High wind + drought)
            if (windAvg[m] > 40 && rainAvg[m] < 10 && humidityAvg[m] < 25) {
                warnings.add(String.format("🏜️ %s : Vent fort (%.0f km/h) & sécheresse extrême. Tempête de sable/poussière (Vol d'insectes impossible).",
                        mName, windAvg[m]));
            }
        }

        coherenceDetailsBox.getChildren().clear();

        if (!errors.isEmpty()) {
            coherenceStatusBadge.setText(i18n.get("weather.coherence.error"));
            coherenceStatusBadge.setStyle("-fx-text-fill:#ff4757;-fx-font-weight:bold;-fx-font-size:12;");
            for (String err : errors) {
                Label l = new Label("• " + err);
                l.setStyle("-fx-text-fill:#ff4757;-fx-font-size:11;");
                coherenceDetailsBox.getChildren().add(l);
            }
        } else if (!warnings.isEmpty()) {
            coherenceStatusBadge.setText(i18n.get("weather.coherence.warn"));
            coherenceStatusBadge.setStyle("-fx-text-fill:#ffc107;-fx-font-weight:bold;-fx-font-size:12;");
            for (String warn : warnings) {
                Label l = new Label("• " + warn);
                l.setStyle("-fx-text-fill:#ffc107;-fx-font-size:11;");
                coherenceDetailsBox.getChildren().add(l);
            }
        } else {
            coherenceStatusBadge.setText(i18n.get("weather.coherence.ok"));
            coherenceStatusBadge.setStyle("-fx-text-fill:#28a745;-fx-font-weight:bold;-fx-font-size:12;");
        }
    }

    private void checkOrder(String name, String month, double min, double avg, double max, List<String> errors) {
        if (min > avg) {
            errors.add(String.format("%s (%s) : Min (%.1f) > Moyenne (%.1f)", name, month, min, avg));
        }
        if (avg > max) {
            errors.add(String.format("%s (%s) : Moyenne (%.1f) > Max (%.1f)", name, month, avg, max));
        }
    }

    private void doHarmonizeCoherence() {
        for (int m = 0; m < 12; m++) {
            harmonizeTriplet(tempMin, tempAvg, tempMax, m);
            harmonizeTriplet(windMin, windAvg, windMax, m);
            harmonizeTriplet(rainMin, rainAvg, rainMax, m);
            harmonizeTriplet(humidityMin, humidityAvg, humidityMax, m);

            if (rainAvg[m] > 100 && humidityAvg[m] < 40) {
                humidityAvg[m] = 45;
                humidityMax[m] = Math.max(humidityMax[m], 60);
            }
        }

        updateSpinnersForActiveParam();
        syncAndValidate();
        redrawCurves();
        NotificationOverlay.show(this, "Courbes réharmonisées avec succès selon les règles de cohérence physique !", NotificationOverlay.NotificationType.SUCCESS);
    }

    private void harmonizeTriplet(double[] min, double[] avg, double[] max, int m) {
        if (min[m] > avg[m]) min[m] = avg[m] - 2.0;
        if (max[m] < avg[m]) max[m] = avg[m] + 2.0;
    }

    // ── Natural Disasters Section ─────────────────────────────────────────────

    private TitledPane createDisastersSection() {
        I18nManager i18n = I18nManager.getInstance();
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        Label warning = new Label();
        warning.textProperty().bind(i18n.createStringBinding("weather.disasters.warning"));
        warning.setStyle("-fx-font-style: italic;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);

        String[][] disasters = {
                { "weather.disasters.fire", "1.0", "🔥 Fire & Lightning" },
                { "weather.disasters.flood", "2.0", "🌊 Flash Flood" },
                { "weather.disasters.sandstorm", "1.5", "🏜️ Sandstorm / Dust Storm" },
                { "weather.disasters.lightning", "3.0", "⚡ Lightning Strikes" },
                { "weather.disasters.tornado", "0.5", "🌪 Tornado" },
                { "weather.disasters.drought", "3.0", "🏜 Drought" },
                { "weather.disasters.freeze", "2.0", "❄️ Hard Freeze" }
        };

        int row = 0;
        for (String[] disaster : disasters) {
            Label label = new Label();
            label.textProperty().bind(i18n.createStringBinding(disaster[0]));
            label.setStyle("-fx-min-width: 140;");

            Slider slider = new Slider(0, 20, Double.parseDouble(disaster[1]));
            slider.setPrefWidth(200);
            slider.setShowTickLabels(true);
            slider.setMajorTickUnit(5);

            Label valueLabel = new Label(disaster[1] + "% /an");
            valueLabel.setStyle("-fx-min-width: 80;");
            slider.valueProperty().addListener((obs, old, val) ->
                    valueLabel.setText(String.format("%.1f%% /an", val.doubleValue())));

            disasterProbabilities.put(disaster[2], slider);

            grid.add(label, 0, row);
            grid.add(slider, 1, row);
            grid.add(valueLabel, 2, row);
            row++;
        }

        content.getChildren().addAll(warning, grid);

        TitledPane pane = new TitledPane();
        pane.textProperty().bind(i18n.createStringBinding("weather.disasters.title"));
        pane.setContent(content);
        styleTitledPane(pane);
        pane.setExpanded(false);
        return pane;
    }

    private void styleTitledPane(TitledPane pane) {
        pane.setCollapsible(true);
    }

    private void onFieldEdited() {
        if (isUpdatingFields) return;
        isDirty = true;
        if (presetsCombo != null && presetsCombo.getSelectionModel().getSelectedItem() != null) {
            isUpdatingFields = true;
            try {
                presetsCombo.getSelectionModel().clearSelection();
            } finally {
                isUpdatingFields = false;
            }
        }
    }

    private void attachUserChangeListeners() {
        if (latSpinner != null) latSpinner.valueProperty().addListener((o, a, b) -> onFieldEdited());
        if (lonSpinner != null) lonSpinner.valueProperty().addListener((o, a, b) -> onFieldEdited());
        if (altSpinner != null) altSpinner.valueProperty().addListener((o, a, b) -> onFieldEdited());
        if (pressureSpinner != null) pressureSpinner.valueProperty().addListener((o, a, b) -> onFieldEdited());
        if (windDirCombo != null) windDirCombo.valueProperty().addListener((o, a, b) -> onFieldEdited());
        if (soilInertiaSpinner != null) soilInertiaSpinner.valueProperty().addListener((o, a, b) -> onFieldEdited());
        if (depthAttenSpinner != null) depthAttenSpinner.valueProperty().addListener((o, a, b) -> onFieldEdited());

        for (int i = 0; i < 12; i++) {
            if (minSpinners[i] != null) minSpinners[i].valueProperty().addListener((o, a, b) -> onFieldEdited());
            if (avgSpinners[i] != null) avgSpinners[i].valueProperty().addListener((o, a, b) -> onFieldEdited());
            if (maxSpinners[i] != null) maxSpinners[i].valueProperty().addListener((o, a, b) -> onFieldEdited());
        }

        for (Slider s : disasterProbabilities.values()) {
            if (s != null) s.valueProperty().addListener((o, a, b) -> onFieldEdited());
        }
    }

    // ── Preset & Data Binding Actions ─────────────────────────────────────────

    private void applyPresetConfig(Map<String, Object> cfg) {
        if (cfg == null) return;
        isUpdatingFields = true;
        try {
            if (cfg.containsKey("latitude") && latSpinner != null) latSpinner.getValueFactory().setValue(num(cfg, "latitude"));
            if (cfg.containsKey("longitude") && lonSpinner != null) lonSpinner.getValueFactory().setValue(num(cfg, "longitude"));
            if (cfg.containsKey("altitude") && altSpinner != null) altSpinner.getValueFactory().setValue(num(cfg, "altitude"));
            if (cfg.containsKey("basePressure") && pressureSpinner != null) pressureSpinner.getValueFactory().setValue(num(cfg, "basePressure"));
            if (cfg.containsKey("windDirection") && windDirCombo != null) windDirCombo.setValue((String) cfg.get("windDirection"));
            if (cfg.containsKey("soilInertiaDays") && soilInertiaSpinner != null) soilInertiaSpinner.getValueFactory().setValue(num(cfg, "soilInertiaDays"));
            if (cfg.containsKey("depthAttenuation") && depthAttenSpinner != null) depthAttenSpinner.getValueFactory().setValue(num(cfg, "depthAttenuation"));

            if (citySearchField != null) {
                if (cfg.containsKey("cityName")) {
                    citySearchField.setText(String.valueOf(cfg.get("cityName")));
                } else {
                    String pName = String.valueOf(cfg.getOrDefault("presetName", ""));
                    String city = switch (pName) {
                        case "Arctic" -> "Longyearbyen";
                        case "Arid" -> "Tamanrasset";
                        case "Mediterranean" -> "Marseille";
                        case "Tropical" -> "Manaus";
                        default -> "Paris";
                    };
                    citySearchField.setText(city);
                }
            }

            copyList(cfg, "tempMin", tempMin);
            copyList(cfg, "tempAvg", tempAvg);
            copyList(cfg, "tempMax", tempMax);

            copyList(cfg, "windMin", windMin);
            copyList(cfg, "windAvg", windAvg);
            copyList(cfg, "windMax", windMax);

            copyList(cfg, "rainMin", rainMin);
            copyList(cfg, "rainAvg", rainAvg);
            copyList(cfg, "rainMax", rainMax);

            copyList(cfg, "humidityMin", humidityMin);
            copyList(cfg, "humidityAvg", humidityAvg);
            copyList(cfg, "humidityMax", humidityMax);

            updatePhotoperiod();
            updateSpinnersForActiveParam();
            syncAndValidate();
            redrawCurves();
        } finally {
            isUpdatingFields = false;
            isDirty = false;
        }
    }

    private double num(Map<String, Object> m, String k) {
        return ((Number) m.get(k)).doubleValue();
    }

    @SuppressWarnings("unchecked")
    private void copyList(Map<String, Object> cfg, String key, double[] target) {
        if (!cfg.containsKey(key)) return;
        Object val = cfg.get(key);
        if (val instanceof List) {
            List<Number> list = (List<Number>) val;
            for (int i = 0; i < Math.min(12, list.size()); i++) {
                target[i] = list.get(i).doubleValue();
            }
        }
    }

    private void applyDefaultValues() {
        Map<String, Object> def = presetMgr.get("Temperate");
        if (def != null) applyPresetConfig(def);
    }

    private void doSavePreset() {
        String defaultName = (presetsCombo.getEditor() != null && !presetsCombo.getEditor().getText().isBlank())
                ? presetsCombo.getEditor().getText().trim()
                : (presetsCombo.getValue() != null ? presetsCombo.getValue() : "Custom Climate Profile");
        TextInputDialog d = org.swarmforge.client.util.ThemeManager.createTextInputDialog(defaultName);
        d.setTitle("Enregistrer Preset Climat");
        d.setHeaderText("Nom du profil climatique :");
        d.setContentText("Nom :");
        d.showAndWait().ifPresent(name -> {
            if (name == null || name.isBlank()) return;
            String clean = name.trim();
            if (presetMgr.contains(clean)) {
                Alert confirmAlert = org.swarmforge.client.util.ThemeManager.createAlert(
                    Alert.AlertType.CONFIRMATION,
                    "Le preset climatique '" + clean + "' existe déjà.\n\nVoulez-vous le remplacer par la configuration actuelle ?"
                );
                confirmAlert.setTitle("Remplacer le Preset Existant");
                confirmAlert.setHeaderText("Confirmation de remplacement");
                java.util.Optional<ButtonType> res = confirmAlert.showAndWait();
                if (res.isEmpty() || res.get() != ButtonType.OK) {
                    return;
                }
            }
            presetMgr.save(clean, getConfiguration());
            isUpdatingFields = true;
            try {
                refreshPresetsCombo();
                presetsCombo.setValue(clean);
            } finally {
                isUpdatingFields = false;
            }
            lastSelectedPreset = clean;
            isDirty = false;
            NotificationOverlay.show(this, "Preset \"" + clean + "\" sauvegardé avec succès.", NotificationOverlay.NotificationType.SUCCESS);
        });
    }

    private void doExport() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Exporter Profil Climat");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        fc.setInitialFileName("weather_climate.json");
        File f = fc.showSaveDialog(getScene().getWindow());
        if (f == null) return;
        try {
            new com.fasterxml.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(f, getConfiguration());
            NotificationOverlay.show(this, "Profil exporté avec succès !", NotificationOverlay.NotificationType.SUCCESS);
        } catch (Exception ex) {
            NotificationOverlay.show(this, "Erreur d'exportation : " + ex.getMessage(), NotificationOverlay.NotificationType.ERROR);
        }
    }

    private void doImport() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Importer Profil Climat");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        File f = fc.showOpenDialog(getScene().getWindow());
        if (f == null) return;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> cfg = new com.fasterxml.jackson.databind.ObjectMapper().readValue(f, Map.class);
            applyPresetConfig(cfg);
            NotificationOverlay.show(this, "Profil importé avec succès !", NotificationOverlay.NotificationType.SUCCESS);
        } catch (Exception ex) {
            NotificationOverlay.show(this, "Erreur d'importation : " + ex.getMessage(), NotificationOverlay.NotificationType.ERROR);
        }
    }

    private void applyToWorld() {
        if (onApplyCallback != null) {
            onApplyCallback.accept(getConfiguration());
            NotificationOverlay.show(this, "Profil climatique appliqué au monde de simulation.", NotificationOverlay.NotificationType.SUCCESS);
        } else {
            NotificationOverlay.show(this, "Aucun éditeur de monde connecté.", NotificationOverlay.NotificationType.WARNING);
        }
    }

    public void setOnApply(Consumer<Map<String, Object>> cb) {
        this.onApplyCallback = cb;
    }

    public Map<String, Object> getConfiguration() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("presetName", presetsCombo.getValue() != null ? presetsCombo.getValue() : "Custom");

        config.put("latitude", latSpinner != null ? latSpinner.getValue() : 48.8);
        config.put("longitude", lonSpinner != null ? lonSpinner.getValue() : 2.35);
        config.put("altitude", altSpinner != null ? altSpinner.getValue() : 100.0);
        config.put("basePressure", pressureSpinner != null ? pressureSpinner.getValue() : 1013.25);
        config.put("windDirection", windDirCombo != null ? windDirCombo.getValue() : "SW");
        config.put("soilInertiaDays", soilInertiaSpinner != null ? soilInertiaSpinner.getValue() : 3.0);
        config.put("depthAttenuation", depthAttenSpinner != null ? depthAttenSpinner.getValue() : 0.85);

        config.put("vegetationCover", vegCoverLabel != null ? vegCoverLabel.getText() : "");
        config.put("daylightHours", toList(daylightHours));

        config.put("tempMin", toList(tempMin));
        config.put("tempAvg", toList(tempAvg));
        config.put("tempMax", toList(tempMax));

        config.put("windMin", toList(windMin));
        config.put("windAvg", toList(windAvg));
        config.put("windMax", toList(windMax));

        config.put("rainMin", toList(rainMin));
        config.put("rainAvg", toList(rainAvg));
        config.put("rainMax", toList(rainMax));

        config.put("humidityMin", toList(humidityMin));
        config.put("humidityAvg", toList(humidityAvg));
        config.put("humidityMax", toList(humidityMax));

        Map<String, Double> disasters = new LinkedHashMap<>();
        disasterProbabilities.forEach((k, v) -> disasters.put(k, v.getValue()));
        config.put("disasters", disasters);

        return config;
    }

    // ── Utility Helpers ───────────────────────────────────────────────────────

    private List<Double> toList(double[] arr) {
        List<Double> list = new ArrayList<>(arr.length);
        for (double d : arr) list.add(d);
        return list;
    }

    private double getAvg(double[] arr) {
        double s = 0;
        for (double d : arr) s += d;
        return s / arr.length;
    }

    private double getSum(double[] arr) {
        double s = 0;
        for (double d : arr) s += d;
        return s;
    }
}
