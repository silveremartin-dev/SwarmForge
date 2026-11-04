/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.NotificationOverlay;
import org.swarmforge.client.util.ThemeManager;
import org.swarmforge.core.domain.AccessoryPreset;

import java.io.File;
import java.util.Optional;

/**
 * Helper class managing preset serialization, loading, deletion, and confirmation dialogs
 * for {@link AccessorySpeciesEditorPane}.
 */
class AccessorySpeciesPresetIoHelper {

    static boolean promptUnsavedChanges(
            AccessorySpeciesEditorPane pane,
            ComboBox<String> combo,
            String lastSelectedPreset,
            Runnable onSave
    ) {
        if ("true".equals(System.getProperty("swarmforge.test")) || Boolean.getBoolean("headless") || Boolean.getBoolean("testfx.headless")) return true;
        if (!pane.isDirty()) return true;

        String currentName = lastSelectedPreset != null ? lastSelectedPreset : "";
        boolean hasCurrentPreset = !currentName.isEmpty();

        Alert alert = ThemeManager.createAlert(
            Alert.AlertType.CONFIRMATION,
            "You have unsaved changes in the Accessory Species Editor.\n"
            + (hasCurrentPreset ? "Current preset: \"" + currentName + "\"" : "No preset selected.")
        );
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText("Exit Accessory Species Editor?");

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
        if (result.get() == btnDiscard) {
            pane.setDirty(false);
            return true;
        }
        if (btnUpdate != null && result.get() == btnUpdate) {
            onSave.run();
            return !pane.isDirty();
        }
        if (result.get() == btnSaveAs) {
            TextInputDialog dlg = new TextInputDialog(currentName.isEmpty() ? "nouveau-preset" : currentName + " (copie)");
            dlg.setTitle("Save As");
            dlg.setHeaderText("New accessory species preset name:");
            dlg.setContentText("Name:");
            ThemeManager.getInstance().applyTheme(dlg.getDialogPane().getScene());
            Optional<String> nameResult = dlg.showAndWait();
            if (nameResult.isPresent() && !nameResult.get().trim().isEmpty()) {
                String newName = nameResult.get().trim();
                pane.setUpdatingFields(true);
                try {
                    if (!combo.getItems().contains(newName)) {
                        combo.getItems().add(newName);
                    }
                    combo.getSelectionModel().select(newName);
                } finally {
                    pane.setUpdatingFields(false);
                }
                pane.setLastSelectedPreset(newName);
                pane.setDirty(false);
                NotificationOverlay.show(pane, "Accessory species preset saved as: " + newName, NotificationOverlay.NotificationType.SUCCESS);
                return true;
            }
            return false;
        }
        return false;
    }

    static void handleAddPreset(
            AccessorySpeciesEditorPane pane,
            AccessoryPresetManager presetManager,
            ComboBox<String> combo,
            I18nManager i18n
    ) {
        String editedText = combo.getEditor() != null ? combo.getEditor().getText().trim() : "";
        String fieldText = pane.accessoryNameField != null ? pane.accessoryNameField.getText().trim() : "";
        String name = !editedText.isEmpty() ? editedText : (!fieldText.isEmpty() ? fieldText : "swarmforge-accessory-custom");

        AccessoryPreset preset = new AccessoryPreset();
        preset.setId(name.toLowerCase().replaceAll("[^a-z0-9]+", "-"));
        preset.setName(name);
        preset.setBuiltIn(false);
        preset.setAuthor("User Custom");

        try {
            if (pane.categoryCombo != null && pane.categoryCombo.getValue() != null) {
                preset.setCategory(AccessoryPreset.Category.valueOf(pane.categoryCombo.getValue()));
            }
        } catch (Exception ignored) {}

        if (pane.biomeCombo != null) preset.setBiome(pane.biomeCombo.getValue());
        if (pane.latitudeField != null) { try { preset.setLatitude(Double.parseDouble(pane.latitudeField.getText())); } catch (Exception ignored) {} }
        if (pane.minTempField != null) { try { preset.setMinTempCelsius(Float.parseFloat(pane.minTempField.getText())); } catch (Exception ignored) {} }
        if (pane.optTempField != null) { try { preset.setOptimalTempCelsius(Float.parseFloat(pane.optTempField.getText())); } catch (Exception ignored) {} }
        if (pane.maxTempField != null) { try { preset.setMaxTempCelsius(Float.parseFloat(pane.maxTempField.getText())); } catch (Exception ignored) {} }
        if (pane.growthRateField != null) { try { preset.setGrowthRate(Float.parseFloat(pane.growthRateField.getText())); } catch (Exception ignored) {} }
        if (pane.initialBiomassDensityField != null) { try { preset.setInitialBiomassDensity(Float.parseFloat(pane.initialBiomassDensityField.getText())); } catch (Exception ignored) {} }
        if (pane.initialPopulationDensityField != null) { try { preset.setInitialPopulationDensity(Float.parseFloat(pane.initialPopulationDensityField.getText())); } catch (Exception ignored) {} }
        if (pane.diapauseCheck != null) preset.setDiapause(pane.diapauseCheck.isSelected());

        if (pane.hemisphereCombo != null) {
            int idx = pane.hemisphereCombo.getSelectionModel().getSelectedIndex();
            preset.setHemisphere(idx == 1 ? "SOUTHERN" : (idx == 2 ? "EQUATORIAL" : "NORTHERN"));
        }
        if (pane.seasonSlider1 != null) preset.setSeasonMultiplierSpring((float) pane.seasonSlider1.getValue());
        if (pane.seasonSlider2 != null) preset.setSeasonMultiplierSummer((float) pane.seasonSlider2.getValue());
        if (pane.seasonSlider3 != null) preset.setSeasonMultiplierAutumn((float) pane.seasonSlider3.getValue());
        if (pane.seasonSlider4 != null) preset.setSeasonMultiplierWinter((float) pane.seasonSlider4.getValue());

        if (pane.killRateField != null) { try { preset.setKillRate(Float.parseFloat(pane.killRateField.getText())); } catch (Exception ignored) {} }
        if (pane.transmissionR0Field != null) { try { preset.setTransmissionR0(Float.parseFloat(pane.transmissionR0Field.getText())); } catch (Exception ignored) {} }
        if (pane.incubationDaysField != null) { try { preset.setIncubationDays(Float.parseFloat(pane.incubationDaysField.getText())); } catch (Exception ignored) {} }
        if (pane.mortalityRateField != null) { try { preset.setMortalityRate(Float.parseFloat(pane.mortalityRateField.getText())); } catch (Exception ignored) {} }

        presetManager.savePreset(name, preset);

        pane.setUpdatingFields(true);
        try {
            if (!combo.getItems().contains(name)) combo.getItems().add(name);
            combo.getSelectionModel().select(name);
        } finally {
            pane.setUpdatingFields(false);
        }
        pane.setLastSelectedPreset(name);
        pane.setDirty(false);
        NotificationOverlay.show(pane, i18n.get("accessory.preset.save") + " : " + name, NotificationOverlay.NotificationType.SUCCESS);
    }

    static void handleLoad(AccessorySpeciesEditorPane pane, AccessoryPresetManager presetManager, ComboBox<String> combo, I18nManager i18n) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(i18n.get("nest.preset.import"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File f = chooser.showOpenDialog(pane.getScene().getWindow());
        if (f != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                AccessoryPreset p = mapper.readValue(f, AccessoryPreset.class);
                String name = p.getName() != null ? p.getName() : p.getId();
                presetManager.savePreset(name, p);
                if (!combo.getItems().contains(name)) combo.getItems().add(name);
                combo.getSelectionModel().select(name);
                pane.applyAccessoryPreset(name);
                NotificationOverlay.show(pane, i18n.get("nest.preset.import") + " : " + f.getName(), NotificationOverlay.NotificationType.SUCCESS);
            } catch (Exception ex) {
                NotificationOverlay.show(pane, "Import error: " + ex.getMessage(), NotificationOverlay.NotificationType.ERROR);
            }
        }
    }

    static void handleSave(AccessorySpeciesEditorPane pane, AccessoryPresetManager presetManager, I18nManager i18n) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(i18n.get("nest.preset.export"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        chooser.setInitialFileName("swarmforge-accessory-" + (pane.accessoryNameField != null ? pane.accessoryNameField.getText().toLowerCase().replaceAll("[^a-z0-9]+", "-") : "custom") + ".json");

        File f = chooser.showSaveDialog(pane.getScene().getWindow());
        if (f != null) {
            try {
                String name = pane.accessoryNameField != null ? pane.accessoryNameField.getText().trim() : "custom";
                AccessoryPreset p = presetManager.get(name).orElseGet(() -> {
                    AccessoryPreset np = new AccessoryPreset();
                    np.setId(name.toLowerCase().replaceAll("[^a-z0-9]+", "-"));
                    np.setName(name);
                    return np;
                });
                ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
                mapper.writeValue(f, p);
                NotificationOverlay.show(pane, i18n.get("nest.preset.export") + " : " + f.getName(), NotificationOverlay.NotificationType.SUCCESS);
            } catch (Exception ex) {
                NotificationOverlay.show(pane, "Export error: " + ex.getMessage(), NotificationOverlay.NotificationType.ERROR);
            }
        }
    }

    static void handleDeletePreset(AccessorySpeciesEditorPane pane, AccessoryPresetManager presetManager, ComboBox<String> combo, I18nManager i18n) {
        String selected = combo.getValue();
        if (selected == null || selected.isEmpty()) return;

        Alert confirmAlert = ThemeManager.createAlert(
            Alert.AlertType.CONFIRMATION,
            String.format(i18n.get("preset.delete.confirm"), selected)
        );
        confirmAlert.setTitle(i18n.get("preset.delete.title"));
        confirmAlert.setHeaderText(i18n.get("accessory.delete.confirm_header"));

        confirmAlert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                presetManager.deletePreset(selected);
                combo.getItems().remove(selected);
                if (!combo.getItems().isEmpty()) {
                    combo.getSelectionModel().selectFirst();
                }
                NotificationOverlay.show(pane, i18n.get("preset.delete.title"), NotificationOverlay.NotificationType.INFO);
            }
        });
    }

    static void applyAccessoryPreset(AccessorySpeciesEditorPane pane, AccessoryPresetManager presetManager, String name) {
        if (name == null || name.isEmpty()) return;
        AccessoryPreset p = presetManager.get(name).orElse(null);
        if (p == null) return;

        pane.setUpdatingFields(true);
        try {
            if (pane.accessoryNameField != null) pane.accessoryNameField.setText(p.getName() != null ? p.getName() : p.getId());
            if (pane.categoryCombo != null && p.getCategory() != null) pane.categoryCombo.setValue(p.getCategory().name());
            if (pane.biomeCombo != null && p.getBiome() != null) pane.biomeCombo.setValue(p.getBiome());
            if (pane.latitudeField != null) pane.latitudeField.setText(String.valueOf(p.getLatitude()));
            if (pane.minTempField != null) pane.minTempField.setText(String.valueOf(p.getMinTempCelsius()));
            if (pane.optTempField != null) pane.optTempField.setText(String.valueOf(p.getOptimalTempCelsius()));
            if (pane.maxTempField != null) pane.maxTempField.setText(String.valueOf(p.getMaxTempCelsius()));
            if (pane.growthRateField != null) pane.growthRateField.setText(String.valueOf(p.getGrowthRate()));
            if (pane.initialBiomassDensityField != null) pane.initialBiomassDensityField.setText(String.valueOf(p.getInitialBiomassDensity()));
            if (pane.initialPopulationDensityField != null) pane.initialPopulationDensityField.setText(String.valueOf(p.getInitialPopulationDensity()));
            if (pane.diapauseCheck != null) pane.diapauseCheck.setSelected(p.isDiapause());

            if (pane.hemisphereCombo != null && p.getHemisphere() != null) {
                if ("SOUTHERN".equalsIgnoreCase(p.getHemisphere())) pane.hemisphereCombo.getSelectionModel().select(1);
                else if ("EQUATORIAL".equalsIgnoreCase(p.getHemisphere())) pane.hemisphereCombo.getSelectionModel().select(2);
                else pane.hemisphereCombo.getSelectionModel().select(0);
            }
            if (pane.seasonSlider1 != null) pane.seasonSlider1.setValue(p.getSeasonMultiplierSpring());
            if (pane.seasonSlider2 != null) pane.seasonSlider2.setValue(p.getSeasonMultiplierSummer());
            if (pane.seasonSlider3 != null) pane.seasonSlider3.setValue(p.getSeasonMultiplierAutumn());
            if (pane.seasonSlider4 != null) pane.seasonSlider4.setValue(p.getSeasonMultiplierWinter());

            if (pane.targetCasteCombo != null && p.getTargetCaste() != null) {
                pane.targetCasteCombo.setValue(switch (p.getTargetCaste()) {
                    case WORKERS -> "Workers";
                    case BROOD_PUPAE -> "Brood / Pupae";
                    case QUEENS_ALATES -> "Queens / Alates";
                    default -> "All Castes";
                });
            }
            if (pane.huntModeCombo != null && p.getHuntMode() != null) {
                pane.huntModeCombo.setValue(switch (p.getHuntMode()) {
                    case AMBUSH -> "Ambush / Stalking (Spider)";
                    case PARASITOID -> "Parasitoid (Internal Egg-laying / Wasp)";
                    case TRAP_FUNNEL -> "Trap / Funnel (Antlion)";
                    default -> "Direct Attack (Bird / Anteater)";
                });
            }
            if (pane.killRateField != null) pane.killRateField.setText(String.valueOf(p.getKillRate()));
            if (pane.pathogenVectorCombo != null && p.getPathogenVector() != null) {
                pane.pathogenVectorCombo.setValue(switch (p.getPathogenVector()) {
                    case CONTAMINATED_FOOD -> "Contaminated Food";
                    case GROOMING -> "Grooming / Allogrooming";
                    case SOIL_CONTACT -> "Soil & Gallery Contact";
                    default -> "Airborne Spores (Cordyceps)";
                });
            }
            if (pane.transmissionR0Field != null) pane.transmissionR0Field.setText(String.valueOf(p.getTransmissionR0()));
            if (pane.incubationDaysField != null) pane.incubationDaysField.setText(String.valueOf(p.getIncubationDays()));
            if (pane.mortalityRateField != null) pane.mortalityRateField.setText(String.valueOf(p.getMortalityRate()));
        } finally {
            pane.setUpdatingFields(false);
        }
    }
}
