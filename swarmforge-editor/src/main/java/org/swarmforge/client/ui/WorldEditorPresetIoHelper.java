package org.swarmforge.client.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import org.swarmforge.client.util.NotificationOverlay;
import org.swarmforge.client.util.ThemeManager;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Handles preset loading, saving, deletion, unsaved change prompts,
 * and JSON import/export for {@link WorldEditorPane}.
 */
public final class WorldEditorPresetIoHelper {

    private WorldEditorPresetIoHelper() {}

    public static boolean promptUnsavedChanges(WorldEditorPane pane) {
        if ("true".equals(System.getProperty("swarmforge.test")) || Boolean.getBoolean("headless") || Boolean.getBoolean("testfx.headless")) return true;
        if (!pane.isDirty) return true;
        String currentName = pane.lastSelectedPreset != null ? pane.lastSelectedPreset : "";
        boolean hasCurrentPreset = !currentName.isEmpty();

        Alert alert = ThemeManager.createAlert(
            Alert.AlertType.CONFIRMATION,
            "You have unsaved changes in the World Editor.\n"
            + (hasCurrentPreset ? "Current preset: \"" + currentName + "\"" : "No preset selected.")
        );
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText("Exit World Editor?");

        ButtonType btnUpdate = hasCurrentPreset
            ? new ButtonType("💾 Update \"" + currentName + "\"", ButtonBar.ButtonData.OK_DONE)
            : null;
        ButtonType btnSaveAs = new ButtonType("📝 Save As...", ButtonBar.ButtonData.OTHER);
        ButtonType btnDiscard = new ButtonType("🗑 Discard", ButtonBar.ButtonData.OTHER);
        ButtonType btnCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        if (btnUpdate != null) {
            alert.getButtonTypes().setAll(btnUpdate, btnSaveAs, btnDiscard, btnCancel);
        } else {
            alert.getButtonTypes().setAll(btnSaveAs, btnDiscard, btnCancel);
        }
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isEmpty() || result.get() == btnCancel) return false;
        if (result.get() == btnDiscard) { pane.isDirty = false; return true; }
        if (btnUpdate != null && result.get() == btnUpdate) {
            pane.presetManager.save(currentName, pane.getConfiguration());
            pane.isUpdatingFields = true;
            try {
                pane.presetsCombo.getItems().setAll(pane.presetManager.names());
                pane.presetsCombo.setValue(currentName);
            } finally {
                pane.isUpdatingFields = false;
            }
            pane.lastSelectedPreset = currentName;
            pane.isDirty = false;
            NotificationOverlay.show(pane, "Preset \"" + currentName + "\" updated.", NotificationOverlay.NotificationType.SUCCESS);
            return true;
        }
        if (result.get() == btnSaveAs) {
            pane.handleSavePreset();
            return !pane.isDirty;
        }
        return false;
    }

    public static void handleSavePreset(WorldEditorPane pane) {
        String defaultName = (pane.presetsCombo.getEditor() != null && !pane.presetsCombo.getEditor().getText().isBlank())
                ? pane.presetsCombo.getEditor().getText().trim()
                : (pane.presetsCombo.getValue() != null ? pane.presetsCombo.getValue() : "Nouveau Preset Monde");
        TextInputDialog dialog = ThemeManager.createTextInputDialog(defaultName);
        dialog.setTitle("Enregistrer le Preset Monde");
        dialog.setHeaderText("Saisissez un nom pour ce preset de monde :");
        dialog.setContentText("Nom :");
        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                String cleanName = name.trim();
                if (pane.presetManager.contains(cleanName)) {
                    Alert confirmAlert = ThemeManager.createAlert(
                        Alert.AlertType.CONFIRMATION,
                        "World preset '" + cleanName + "' already exists.\n\nDo you want to overwrite it with current configuration?"
                    );
                    confirmAlert.setTitle("Overwrite Existing Preset");
                    confirmAlert.setHeaderText("Overwrite Confirmation");
                    Optional<ButtonType> res = confirmAlert.showAndWait();
                    if (res.isEmpty() || res.get() != ButtonType.OK) {
                        return;
                    }
                }
                pane.presetManager.save(cleanName, pane.getConfiguration());
                pane.presetsCombo.getItems().setAll(pane.presetManager.names());
                pane.presetsCombo.getSelectionModel().select(cleanName);
                pane.lastSelectedPreset = cleanName;
                pane.isDirty = false;
                NotificationOverlay.show(pane, "World preset saved: " + cleanName, NotificationOverlay.NotificationType.SUCCESS);
            }
        });
    }

    public static void handleDeletePreset(WorldEditorPane pane) {
        String selected = pane.presetsCombo.getValue();
        if (selected == null || selected.isEmpty()) return;

        Alert confirmAlert = ThemeManager.createAlert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer le preset '" + selected + "' ?");
        confirmAlert.setTitle("Supprimer le Preset");
        confirmAlert.setHeaderText("Supprimer le Preset Monde");

        confirmAlert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                pane.presetManager.delete(selected);
                pane.presetsCombo.getItems().setAll(pane.presetManager.names());
                if (!pane.presetsCombo.getItems().isEmpty()) {
                    pane.presetsCombo.getSelectionModel().selectFirst();
                } else {
                    pane.presetsCombo.getSelectionModel().clearSelection();
                }
                ThemeManager.createAlert(Alert.AlertType.INFORMATION, "World preset deleted.").show();
            }
        });
    }

    public static Map<String, Object> getConfiguration(WorldEditorPane pane) {
        Map<String, Object> cfg = new LinkedHashMap<>();
        cfg.put("presetName", pane.presetsCombo != null && pane.presetsCombo.getValue() != null ? pane.presetsCombo.getValue() : "Custom World");
        cfg.put("cityName", pane.cityNameField != null ? pane.cityNameField.getText() : "Fontainebleau, FR");
        cfg.put("latitude", pane.latField != null ? pane.latField.getText() : "48.4047");
        cfg.put("longitude", pane.lonField != null ? pane.lonField.getText() : "2.7016");
        cfg.put("elevation", pane.altField != null ? pane.altField.getText() : "35");
        cfg.put("reliefSeed", pane.reliefSeedField != null ? pane.reliefSeedField.getText() : "774829");
        cfg.put("soilSeed", pane.soilSeedField != null ? pane.soilSeedField.getText() : "123456");
        cfg.put("hydroSeed", pane.hydroSeedField != null ? pane.hydroSeedField.getText() : "987654");
        cfg.put("floraSeed", pane.floraSeedField != null ? pane.floraSeedField.getText() : "774829");
        cfg.put("structSeed", pane.structSeedField != null ? pane.structSeedField.getText() : "555123");

        cfg.put("mountainSlopeAngle", pane.slopeAngleSlider != null ? pane.slopeAngleSlider.getValue() : 0.0);
        cfg.put("mountainSlopeDirection", pane.slopeDirectionCombo != null ? pane.slopeDirectionCombo.getSelectionModel().getSelectedIndex() : 0);

        cfg.put("surfaceSizeMeters", pane.surfaceSizeSlider != null ? pane.surfaceSizeSlider.getValue() : 25.0);
        cfg.put("depthMeters", pane.depthSlider != null ? pane.depthSlider.getValue() : 1.5);
        cfg.put("resolutionMm", pane.resolutionSlider != null ? pane.resolutionSlider.getValue() : 25.0);
        cfg.put("roughness", pane.roughnessSlider != null ? pane.roughnessSlider.getValue() : 0.45);
        cfg.put("compaction", pane.compactionSlider != null ? pane.compactionSlider.getValue() : 0.6);
        cfg.put("stratification", pane.stratificationSlider != null ? pane.stratificationSlider.getValue() : 0.7);
        cfg.put("mixingRate", pane.mixingRateSlider != null ? pane.mixingRateSlider.getValue() : 0.3);
        cfg.put("baseHumidity", pane.baseHumiditySlider != null ? pane.baseHumiditySlider.getValue() : 0.35);
        cfg.put("voidDensity", pane.voidDensitySlider != null ? pane.voidDensitySlider.getValue() : 0.08);

        Map<String, Object> soilMap = new LinkedHashMap<>();
        soilMap.put("earth", pane.earthSpinner != null ? pane.earthSpinner.getValue() : 40);
        soilMap.put("sand", pane.sandSpinner != null ? pane.sandSpinner.getValue() : 20);
        soilMap.put("silt", pane.siltSpinner != null ? pane.siltSpinner.getValue() : 15);
        soilMap.put("clay", pane.claySpinner != null ? pane.claySpinner.getValue() : 15);
        soilMap.put("gravel", pane.gravelSpinner != null ? pane.gravelSpinner.getValue() : 5);
        soilMap.put("peat", pane.peatSpinner != null ? pane.peatSpinner.getValue() : 0);
        soilMap.put("stone", pane.stoneSpinner != null ? pane.stoneSpinner.getValue() : 5);
        soilMap.put("organic", pane.organicSpinner != null ? pane.organicSpinner.getValue() : 0);
        cfg.put("soilComposition", soilMap);

        cfg.put("treeSpeciesIndex", pane.comboTreeSpecies != null ? pane.comboTreeSpecies.getSelectionModel().getSelectedIndex() : 0);

        Map<String, Object> treeMap = new LinkedHashMap<>();
        treeMap.put("oak", pane.oakPctSpinner != null ? pane.oakPctSpinner.getValue() : 40);
        treeMap.put("pine", pane.pinePctSpinner != null ? pane.pinePctSpinner.getValue() : 30);
        treeMap.put("acacia", pane.acaciaPctSpinner != null ? pane.acaciaPctSpinner.getValue() : 10);
        treeMap.put("cactus", pane.cactusPctSpinner != null ? pane.cactusPctSpinner.getValue() : 5);
        treeMap.put("birch", pane.birchPctSpinner != null ? pane.birchPctSpinner.getValue() : 5);
        treeMap.put("bamboo", pane.bambooPctSpinner != null ? pane.bambooPctSpinner.getValue() : 5);
        treeMap.put("deadWood", pane.deadWoodPctSpinner != null ? pane.deadWoodPctSpinner.getValue() : 5);
        cfg.put("treeComposition", treeMap);

        cfg.put("edibleFloraDensity", pane.edibleDensitySlider != null ? pane.edibleDensitySlider.getValue() : 35.0);
        cfg.put("nonEdibleFloraDensity", pane.nonEdibleDensitySlider != null ? pane.nonEdibleDensitySlider.getValue() : 25.0);
        cfg.put("leafLitter", pane.leafLitterSlider != null ? pane.leafLitterSlider.getValue() : 15.0);
        cfg.put("twigDebris", pane.twigDebrisSlider != null ? pane.twigDebrisSlider.getValue() : 10.0);

        cfg.put("aphidPlant", pane.aphidPlantCheck != null && pane.aphidPlantCheck.isSelected());
        cfg.put("nectarFlowers", pane.nectarFlowersCheck != null && pane.nectarFlowersCheck.isSelected());
        cfg.put("seedGrass", pane.seedGrassCheck != null && pane.seedGrassCheck.isSelected());
        cfg.put("fungusFoliage", pane.fungusFoliageCheck != null && pane.fungusFoliageCheck.isSelected());
        cfg.put("moss", pane.mossCheck != null && pane.mossCheck.isSelected());
        cfg.put("pineLitter", pane.pineLitterCheck != null && pane.pineLitterCheck.isSelected());
        cfg.put("fernObstacle", pane.fernObstacleCheck != null && pane.fernObstacleCheck.isSelected());

        cfg.put("hasRiver", pane.riverCheck != null && pane.riverCheck.isSelected());
        cfg.put("riverWidthMm", pane.riverWidthSlider != null ? pane.riverWidthSlider.getValue() : 120.0);
        cfg.put("riverVelocity", pane.riverVelocitySlider != null ? pane.riverVelocitySlider.getValue() : 0.5);
        cfg.put("staticPools", pane.staticPoolsSlider != null ? pane.staticPoolsSlider.getValue() : 2.0);
        cfg.put("waterTableDepthCm", pane.waterTableDepthSlider != null ? pane.waterTableDepthSlider.getValue() : 15.0);
        cfg.put("treeCount", pane.treeCountSlider != null ? (int) pane.treeCountSlider.getValue() : 8);
        cfg.put("hollowLogs", pane.hollowLogsSlider != null ? (int) pane.hollowLogsSlider.getValue() : 4);
        cfg.put("rockCrevices", pane.rockCrevicesSlider != null ? (int) pane.rockCrevicesSlider.getValue() : 6);
        return cfg;
    }

    public static void doExport(WorldEditorPane pane) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Exporter Configuration du Monde");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        fc.setInitialFileName("world_preset.json");
        File f = fc.showSaveDialog(pane.getScene().getWindow());
        if (f == null) return;
        try {
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(f, pane.getConfiguration());
            ThemeManager.createAlert(Alert.AlertType.INFORMATION, "Preset saved successfully.").show();
        } catch (Exception ex) {
            ThemeManager.createAlert(Alert.AlertType.ERROR, "Erreur d'exportation: " + ex.getMessage()).show();
        }
    }

    public static void doImport(WorldEditorPane pane) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Importer Configuration du Monde");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        File f = fc.showOpenDialog(pane.getScene().getWindow());
        if (f == null) return;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> cfg = new ObjectMapper().readValue(f, Map.class);
            pane.loadConfiguration(cfg);
            ThemeManager.createAlert(Alert.AlertType.INFORMATION, "World preset loaded.").show();
        } catch (Exception ex) {
            ThemeManager.createAlert(Alert.AlertType.ERROR, "Erreur d'importation: " + ex.getMessage()).show();
        }
    }

    public static void loadConfiguration(WorldEditorPane pane, Map<String, Object> cfg) {
        if (cfg == null) return;
        pane.isUpdatingFields = true;
        try {
            if (cfg.containsKey("presetName") && pane.presetsCombo != null) {
                String pName = String.valueOf(cfg.get("presetName"));
                if (pane.presetsCombo.getItems().contains(pName)) {
                    pane.presetsCombo.setValue(pName);
                }
            }
            if (cfg.containsKey("cityName") && pane.cityNameField != null) {
                pane.cityNameField.setText(String.valueOf(cfg.get("cityName")));
            } else if (cfg.containsKey("presetName") && pane.cityNameField != null) {
                String pName = String.valueOf(cfg.get("presetName"));
                if (pName.contains("(") && pName.contains(")")) {
                    pane.cityNameField.setText(pName.substring(pName.indexOf("(") + 1, pName.indexOf(")")));
                }
            }
            if (pane.citySearchField != null && pane.cityNameField != null) {
                pane.citySearchField.setText(pane.cityNameField.getText());
            }
            if (cfg.containsKey("reliefSeed") && pane.reliefSeedField != null) pane.reliefSeedField.setText(String.valueOf(cfg.get("reliefSeed")));
            if (cfg.containsKey("soilSeed") && pane.soilSeedField != null) pane.soilSeedField.setText(String.valueOf(cfg.get("soilSeed")));
            if (cfg.containsKey("hydroSeed") && pane.hydroSeedField != null) pane.hydroSeedField.setText(String.valueOf(cfg.get("hydroSeed")));
            if (cfg.containsKey("floraSeed") && pane.floraSeedField != null) pane.floraSeedField.setText(String.valueOf(cfg.get("floraSeed")));
            if (cfg.containsKey("structSeed") && pane.structSeedField != null) pane.structSeedField.setText(String.valueOf(cfg.get("structSeed")));

            if (cfg.containsKey("mountainSlopeAngle") && pane.slopeAngleSlider != null) pane.slopeAngleSlider.setValue(((Number) cfg.get("mountainSlopeAngle")).doubleValue());
            if (cfg.containsKey("mountainSlopeDirection") && pane.slopeDirectionCombo != null) {
                int idx = ((Number) cfg.get("mountainSlopeDirection")).intValue();
                if (idx >= 0 && idx < pane.slopeDirectionCombo.getItems().size()) pane.slopeDirectionCombo.getSelectionModel().select(idx);
            }

            if (cfg.containsKey("surfaceSizeMeters")) pane.surfaceSizeSlider.setValue(((Number) cfg.get("surfaceSizeMeters")).doubleValue());
            if (cfg.containsKey("depthMeters") && pane.depthSlider != null) pane.depthSlider.setValue(((Number) cfg.get("depthMeters")).doubleValue());
            if (cfg.containsKey("depth") && pane.depthSlider != null) pane.depthSlider.setValue(((Number) cfg.get("depth")).doubleValue());
            if (cfg.containsKey("resolutionMm")) pane.resolutionSlider.setValue(((Number) cfg.get("resolutionMm")).doubleValue());
            if (cfg.containsKey("resolution")) pane.resolutionSlider.setValue(((Number) cfg.get("resolution")).doubleValue());
            if (cfg.containsKey("roughness")) pane.roughnessSlider.setValue(((Number) cfg.get("roughness")).doubleValue());
            if (cfg.containsKey("compaction")) pane.compactionSlider.setValue(((Number) cfg.get("compaction")).doubleValue());
            if (cfg.containsKey("stratification") && pane.stratificationSlider != null) pane.stratificationSlider.setValue(((Number) cfg.get("stratification")).doubleValue());
            if (cfg.containsKey("mixingRate") && pane.mixingRateSlider != null) pane.mixingRateSlider.setValue(((Number) cfg.get("mixingRate")).doubleValue());
            if (cfg.containsKey("baseHumidity") && pane.baseHumiditySlider != null) pane.baseHumiditySlider.setValue(((Number) cfg.get("baseHumidity")).doubleValue());
            if (cfg.containsKey("latitude") && pane.latField != null) pane.latField.setText(String.valueOf(cfg.get("latitude")));
            if (cfg.containsKey("longitude") && pane.lonField != null) pane.lonField.setText(String.valueOf(cfg.get("longitude")));
            if (cfg.containsKey("elevation") && pane.altField != null) pane.altField.setText(String.valueOf(cfg.get("elevation")));
            if (cfg.containsKey("latitude") && cfg.containsKey("longitude")) {
                try {
                    double lat = Double.parseDouble(String.valueOf(cfg.get("latitude")));
                    double lon = Double.parseDouble(String.valueOf(cfg.get("longitude")));
                    double alt = cfg.containsKey("elevation") ? Double.parseDouble(String.valueOf(cfg.get("elevation"))) : 0.0;
                    pane.applyBioclimaticAdaptation(lat, lon, alt);
                    if (pane.geoStatusLabel != null) {
                        String cName = pane.cityNameField != null ? pane.cityNameField.getText() : "Serengeti, TZ";
                        pane.geoStatusLabel.setText("🟢 " + cName + " (Lat: " + String.format(Locale.US, "%.4f", lat)
                                + "°, Lon: " + String.format(Locale.US, "%.4f", lon)
                                + "°, Alt: " + (int)alt + "m)");
                        pane.geoStatusLabel.setStyle("-fx-text-fill: #4ade80; -fx-font-size: 10px;");
                    }
                } catch (Exception ignored) {}
            }

            if (cfg.containsKey("soilComposition") && cfg.get("soilComposition") instanceof Map<?,?> soilMap) {
                if (soilMap.containsKey("earth") && pane.earthSpinner != null) pane.earthSpinner.getValueFactory().setValue(((Number) soilMap.get("earth")).intValue());
                if (soilMap.containsKey("sand") && pane.sandSpinner != null) pane.sandSpinner.getValueFactory().setValue(((Number) soilMap.get("sand")).intValue());
                if (soilMap.containsKey("silt") && pane.siltSpinner != null) pane.siltSpinner.getValueFactory().setValue(((Number) soilMap.get("silt")).intValue());
                if (soilMap.containsKey("clay") && pane.claySpinner != null) pane.claySpinner.getValueFactory().setValue(((Number) soilMap.get("clay")).intValue());
                if (soilMap.containsKey("gravel") && pane.gravelSpinner != null) pane.gravelSpinner.getValueFactory().setValue(((Number) soilMap.get("gravel")).intValue());
                if (soilMap.containsKey("peat") && pane.peatSpinner != null) pane.peatSpinner.getValueFactory().setValue(((Number) soilMap.get("peat")).intValue());
                if (soilMap.containsKey("stone") && pane.stoneSpinner != null) pane.stoneSpinner.getValueFactory().setValue(((Number) soilMap.get("stone")).intValue());
                if (soilMap.containsKey("organic") && pane.organicSpinner != null) pane.organicSpinner.getValueFactory().setValue(((Number) soilMap.get("organic")).intValue());
            }

            if (cfg.containsKey("treeSpeciesIndex") && pane.comboTreeSpecies != null) {
                int idx = ((Number) cfg.get("treeSpeciesIndex")).intValue();
                if (idx >= 0 && idx < pane.comboTreeSpecies.getItems().size()) pane.comboTreeSpecies.getSelectionModel().select(idx);
            }

            if (cfg.containsKey("treeComposition") && cfg.get("treeComposition") instanceof Map<?,?> treeMap) {
                if (treeMap.containsKey("oak") && pane.oakPctSpinner != null) pane.oakPctSpinner.getValueFactory().setValue(((Number) treeMap.get("oak")).intValue());
                if (treeMap.containsKey("pine") && pane.pinePctSpinner != null) pane.pinePctSpinner.getValueFactory().setValue(((Number) treeMap.get("pine")).intValue());
                if (treeMap.containsKey("acacia") && pane.acaciaPctSpinner != null) pane.acaciaPctSpinner.getValueFactory().setValue(((Number) treeMap.get("acacia")).intValue());
                if (treeMap.containsKey("cactus") && pane.cactusPctSpinner != null) pane.cactusPctSpinner.getValueFactory().setValue(((Number) treeMap.get("cactus")).intValue());
                if (treeMap.containsKey("birch") && pane.birchPctSpinner != null) pane.birchPctSpinner.getValueFactory().setValue(((Number) treeMap.get("birch")).intValue());
                if (treeMap.containsKey("bamboo") && pane.bambooPctSpinner != null) pane.bambooPctSpinner.getValueFactory().setValue(((Number) treeMap.get("bamboo")).intValue());
                if (treeMap.containsKey("deadWood") && pane.deadWoodPctSpinner != null) pane.deadWoodPctSpinner.getValueFactory().setValue(((Number) treeMap.get("deadWood")).intValue());
            }

            if (cfg.containsKey("floraSeed") && pane.floraSeedField != null) pane.floraSeedField.setText(String.valueOf(cfg.get("floraSeed")));
            if (cfg.containsKey("edibleFloraDensity") && pane.edibleDensitySlider != null) pane.edibleDensitySlider.setValue(((Number) cfg.get("edibleFloraDensity")).doubleValue());
            if (cfg.containsKey("nonEdibleFloraDensity") && pane.nonEdibleDensitySlider != null) pane.nonEdibleDensitySlider.setValue(((Number) cfg.get("nonEdibleFloraDensity")).doubleValue());
            if (cfg.containsKey("leafLitter") && pane.leafLitterSlider != null) pane.leafLitterSlider.setValue(((Number) cfg.get("leafLitter")).doubleValue());
            if (cfg.containsKey("twigDebris") && pane.twigDebrisSlider != null) pane.twigDebrisSlider.setValue(((Number) cfg.get("twigDebris")).doubleValue());

            if (cfg.containsKey("aphidPlant") && pane.aphidPlantCheck != null) pane.aphidPlantCheck.setSelected((Boolean) cfg.get("aphidPlant"));
            if (cfg.containsKey("nectarFlowers") && pane.nectarFlowersCheck != null) pane.nectarFlowersCheck.setSelected((Boolean) cfg.get("nectarFlowers"));
            if (cfg.containsKey("seedGrass") && pane.seedGrassCheck != null) pane.seedGrassCheck.setSelected((Boolean) cfg.get("seedGrass"));
            if (cfg.containsKey("fungusFoliage") && pane.fungusFoliageCheck != null) pane.fungusFoliageCheck.setSelected((Boolean) cfg.get("fungusFoliage"));
            if (cfg.containsKey("moss") && pane.mossCheck != null) pane.mossCheck.setSelected((Boolean) cfg.get("moss"));
            if (cfg.containsKey("pineLitter") && pane.pineLitterCheck != null) pane.pineLitterCheck.setSelected((Boolean) cfg.get("pineLitter"));
            if (cfg.containsKey("fernObstacle") && pane.fernObstacleCheck != null) pane.fernObstacleCheck.setSelected((Boolean) cfg.get("fernObstacle"));

            if (cfg.containsKey("hasRiver") && pane.riverCheck != null) pane.riverCheck.setSelected((Boolean) cfg.get("hasRiver"));
            if (cfg.containsKey("riverWidthMm") && pane.riverWidthSlider != null) pane.riverWidthSlider.setValue(((Number) cfg.get("riverWidthMm")).doubleValue());
            if (cfg.containsKey("riverVelocity") && pane.riverVelocitySlider != null) pane.riverVelocitySlider.setValue(((Number) cfg.get("riverVelocity")).doubleValue());
            if (cfg.containsKey("staticPools") && pane.staticPoolsSlider != null) pane.staticPoolsSlider.setValue(((Number) cfg.get("staticPools")).doubleValue());
            if (cfg.containsKey("waterTableDepthCm") && pane.waterTableDepthSlider != null) pane.waterTableDepthSlider.setValue(((Number) cfg.get("waterTableDepthCm")).doubleValue());
            else if (cfg.containsKey("waterTableDepth") && pane.waterTableDepthSlider != null) pane.waterTableDepthSlider.setValue(((Number) cfg.get("waterTableDepth")).doubleValue());

            if (cfg.containsKey("treeCount") && pane.treeCountSlider != null) pane.treeCountSlider.setValue(((Number) cfg.get("treeCount")).doubleValue());
            if (cfg.containsKey("hollowLogs") && pane.hollowLogsSlider != null) pane.hollowLogsSlider.setValue(((Number) cfg.get("hollowLogs")).doubleValue());
            if (cfg.containsKey("rockCrevices") && pane.rockCrevicesSlider != null) pane.rockCrevicesSlider.setValue(((Number) cfg.get("rockCrevices")).doubleValue());
        } finally {
            pane.isUpdatingFields = false;
        }

        pane.regenerateAndRepaint();
    }
}
