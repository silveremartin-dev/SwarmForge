/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.client.ui.weather.WeatherCoherenceValidatorPane;
import org.swarmforge.client.ui.weather.WeatherCurvesEditorPane;
import org.swarmforge.client.ui.weather.WeatherCurvesEditorPane.ParameterType;
import org.swarmforge.client.ui.weather.WeatherDisastersEditorPane;
import org.swarmforge.client.ui.weather.WeatherGeographicAtmospherePane;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.NotificationOverlay;
import org.swarmforge.client.util.ThemeManager;

/**
 * Weather and Climate Editor - Realistic Geographic & Atmospheric Climate System.
 * Orchestrates geographic location, solar photoperiod, barometric pressure,
 * 12-month spline curves, physical coherence, and natural disaster parameters.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class WeatherEditorPane extends BorderPane {

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

    // Sub-Panes
    private WeatherGeographicAtmospherePane geoAtmoPane;
    private WeatherCurvesEditorPane curvesEditorPane;
    private WeatherCoherenceValidatorPane coherencePane;
    private WeatherDisastersEditorPane disastersPane;

    // Presets Manager
    private ComboBox<String> presetsCombo;
    private final WeatherPresetManager presetMgr = new WeatherPresetManager();
    private Consumer<Map<String, Object>> onApplyCallback;

    private boolean isDirty = false;
    private boolean isUpdatingFields = false;
    private String lastSelectedPreset = null;

    public boolean isDirty() {
        return isDirty;
    }

    public boolean promptUnsavedChanges() {
        if ("true".equals(System.getProperty("swarmforge.test")) || Boolean.getBoolean("headless") || Boolean.getBoolean("testfx.headless")) return true;
        if (!isDirty) return true;
        String currentName = lastSelectedPreset != null ? lastSelectedPreset : "";
        boolean hasCurrentPreset = !currentName.isEmpty();

        Alert alert = ThemeManager.createAlert(
            Alert.AlertType.CONFIRMATION,
            "You have unsaved changes in the Weather & Climate Editor.\n"
            + (hasCurrentPreset ? "Current preset: \"" + currentName + "\"" : "No preset selected.")
        );
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText("Exit Weather Editor?");

        ButtonType btnUpdate  = hasCurrentPreset
            ? new ButtonType("💾 Update \"" + currentName + "\"", ButtonBar.ButtonData.OK_DONE)
            : null;
        ButtonType btnSaveAs  = new ButtonType("📝 Save As...", ButtonBar.ButtonData.OTHER);
        ButtonType btnDiscard = new ButtonType("🗑 Discard", ButtonBar.ButtonData.OTHER);
        ButtonType btnCancel  = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        if (btnUpdate != null) {
            alert.getButtonTypes().setAll(btnUpdate, btnSaveAs, btnDiscard, btnCancel);
        } else {
            alert.getButtonTypes().setAll(btnSaveAs, btnDiscard, btnCancel);
        }
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isEmpty() || result.get() == btnCancel) return false;
        if (result.get() == btnDiscard) { isDirty = false; return true; }
        if (btnUpdate != null && result.get() == btnUpdate) {
            presetMgr.save(currentName, getConfiguration());
            isUpdatingFields = true;
            try {
                refreshPresetsCombo();
                presetsCombo.setValue(currentName);
            } finally {
                isUpdatingFields = false;
            }
            lastSelectedPreset = currentName;
            isDirty = false;
            NotificationOverlay.show(this, "Preset \"" + currentName + "\" updated.", NotificationOverlay.NotificationType.SUCCESS);
            return true;
        }
        if (result.get() == btnSaveAs) {
            doSavePreset();
            return !isDirty;
        }
        return false;
    }

    public WeatherEditorPane() {
        setTop(buildHeader());

        // Center: Scrollable main content
        VBox mainContent = new VBox(15);
        mainContent.setPadding(new Insets(10, 15, 15, 15));

        // Sub-Pane 1: Geographic & Atmosphere Microclimate
        geoAtmoPane = new WeatherGeographicAtmospherePane(
                tempAvg, rainAvg,
                this::onFieldEdited,
                () -> {
                    updateSpinnersForActiveParam();
                    syncAndValidate();
                    redrawCurves();
                }
        );

        // Sub-Pane 2: Monthly Interactive Curves Chart & Editor
        curvesEditorPane = new WeatherCurvesEditorPane(
                tempMin, tempAvg, tempMax,
                windMin, windAvg, windMax,
                rainMin, rainAvg, rainMax,
                humidityMin, humidityAvg, humidityMax,
                this::syncAndValidate,
                this::onFieldEdited
        );

        // Sub-Pane 3: Physical Coherence Engine
        coherencePane = new WeatherCoherenceValidatorPane(
                tempMin, tempAvg, tempMax,
                windMin, windAvg, windMax,
                rainMin, rainAvg, rainMax,
                humidityMin, humidityAvg, humidityMax,
                () -> {
                    updateSpinnersForActiveParam();
                    syncAndValidate();
                    redrawCurves();
                }
        );

        // Sub-Pane 4: Natural Disasters
        disastersPane = new WeatherDisastersEditorPane(this::onFieldEdited);

        mainContent.getChildren().addAll(geoAtmoPane, curvesEditorPane, coherencePane, disastersPane);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        setCenter(scrollPane);

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
                Alert alert = ThemeManager.createAlert(
                    Alert.AlertType.CONFIRMATION,
                    "Warning: You have unsaved modifications in the current climate profile.\n\nDo you want to load preset '" + s + "' and discard changes?"
                );
                alert.setTitle(i18n.get("common.dialog.unsaved"));
                alert.setHeaderText("Climate Profile Change");
                Optional<ButtonType> res = alert.showAndWait();
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
        Alert confirmAlert = ThemeManager.createAlert(Alert.AlertType.CONFIRMATION, String.format(i18n.get("preset.delete.confirm"), sel));
        confirmAlert.setTitle(i18n.get("preset.delete.title"));
        confirmAlert.setHeaderText("Delete Climate Profile");

        confirmAlert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                presetMgr.delete(sel);
                refreshPresetsCombo();
                if (!presetsCombo.getItems().isEmpty()) {
                    presetsCombo.getSelectionModel().selectFirst();
                } else {
                    presetsCombo.getSelectionModel().clearSelection();
                }
                NotificationOverlay.show(this, "Climate profile deleted.", NotificationOverlay.NotificationType.INFO);
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

    private void redrawCurves() {
        if (curvesEditorPane != null) curvesEditorPane.redrawCurves();
    }

    private void updateSpinnersForActiveParam() {
        if (curvesEditorPane != null) curvesEditorPane.updateSpinnersForActiveParam();
    }

    private void syncAndValidate() {
        if (geoAtmoPane != null) geoAtmoPane.updateVegetationEstimate();
        if (coherencePane != null) coherencePane.updateCoherenceStatus();
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

    // ── Preset & Data Binding Actions ─────────────────────────────────────────

    private void applyPresetConfig(Map<String, Object> cfg) {
        if (cfg == null) return;
        isUpdatingFields = true;
        try {
            if (geoAtmoPane != null) {
                if (cfg.containsKey("latitude")) geoAtmoPane.setLatitude(num(cfg, "latitude"));
                if (cfg.containsKey("longitude")) geoAtmoPane.setLongitude(num(cfg, "longitude"));
                if (cfg.containsKey("altitude")) geoAtmoPane.setAltitude(num(cfg, "altitude"));
                if (cfg.containsKey("basePressure")) geoAtmoPane.setBasePressure(num(cfg, "basePressure"));
                if (cfg.containsKey("windDirection")) geoAtmoPane.setWindDirection((String) cfg.get("windDirection"));
                if (cfg.containsKey("soilInertiaDays")) geoAtmoPane.setSoilInertiaDays(num(cfg, "soilInertiaDays"));
                if (cfg.containsKey("depthAttenuation")) geoAtmoPane.setDepthAttenuation(num(cfg, "depthAttenuation"));
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

            if (geoAtmoPane != null) geoAtmoPane.updatePhotoperiod();
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
        Object val = cfg.get(key);
        if (val instanceof List<?> l) {
            for (int i = 0; i < Math.min(target.length, l.size()); i++) {
                target[i] = ((Number) l.get(i)).doubleValue();
            }
        }
    }

    private void applyDefaultValues() {
        Map<String, Object> def = presetMgr.get("Temperate Oceanic");
        if (def != null) applyPresetConfig(def);
    }

    private void doSavePreset() {
        TextInputDialog dialog = new TextInputDialog("My Climate Profile");
        dialog.setTitle("Save Climate Profile");
        dialog.setHeaderText("Create a new climate preset");
        dialog.setContentText("Profile Name:");

        dialog.showAndWait().ifPresent(name -> {
            String trimmed = name.trim();
            if (trimmed.isEmpty()) return;

            presetMgr.save(trimmed, getConfiguration());
            isUpdatingFields = true;
            try {
                refreshPresetsCombo();
                presetsCombo.setValue(trimmed);
            } finally {
                isUpdatingFields = false;
            }
            lastSelectedPreset = trimmed;
            isDirty = false;
            NotificationOverlay.show(this, "Preset \"" + trimmed + "\" successfully saved!", NotificationOverlay.NotificationType.SUCCESS);
        });
    }

    private void doExport() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Exporter Profil Climat");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        File f = fc.showSaveDialog(getScene().getWindow());
        if (f == null) return;
        try {
            new com.fasterxml.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter()
                    .writeValue(f, getConfiguration());
            NotificationOverlay.show(this, "Profile successfully exported!", NotificationOverlay.NotificationType.SUCCESS);
        } catch (Exception ex) {
            NotificationOverlay.show(this, "Export error: " + ex.getMessage(), NotificationOverlay.NotificationType.ERROR);
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
            NotificationOverlay.show(this, "Profile successfully imported!", NotificationOverlay.NotificationType.SUCCESS);
        } catch (Exception ex) {
            NotificationOverlay.show(this, "Import error: " + ex.getMessage(), NotificationOverlay.NotificationType.ERROR);
        }
    }

    public void setOnApply(Consumer<Map<String, Object>> cb) {
        this.onApplyCallback = cb;
    }

    public Map<String, Object> getConfiguration() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("presetName", presetsCombo.getValue() != null ? presetsCombo.getValue() : "Custom");

        config.put("latitude", geoAtmoPane != null ? geoAtmoPane.getLatitude() : 48.8);
        config.put("longitude", geoAtmoPane != null ? geoAtmoPane.getLongitude() : 2.35);
        config.put("altitude", geoAtmoPane != null ? geoAtmoPane.getAltitude() : 100.0);
        config.put("basePressure", geoAtmoPane != null ? geoAtmoPane.getBasePressure() : 1013.25);
        config.put("windDirection", geoAtmoPane != null ? geoAtmoPane.getWindDirection() : "SW");
        config.put("soilInertiaDays", geoAtmoPane != null ? geoAtmoPane.getSoilInertiaDays() : 3.0);
        config.put("depthAttenuation", geoAtmoPane != null ? geoAtmoPane.getDepthAttenuation() : 0.85);

        config.put("vegetationCover", geoAtmoPane != null ? geoAtmoPane.getVegetationCover() : "");
        config.put("daylightHours", geoAtmoPane != null ? toList(geoAtmoPane.getDaylightHours()) : List.of());

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

        config.put("disasters", disastersPane != null ? disastersPane.getDisasterValues() : Map.of());

        return config;
    }

    private List<Double> toList(double[] arr) {
        List<Double> list = new ArrayList<>(arr.length);
        for (double d : arr) list.add(d);
        return list;
    }
}
