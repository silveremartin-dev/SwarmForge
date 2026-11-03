/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.NotificationOverlay;
import org.swarmforge.client.util.ThemeManager;
import org.swarmforge.core.species.DefaultSpecies;

import java.io.File;
import java.util.Optional;

/**
 * Handles preset persistence, disk import/export, and unsaved changes confirmation
 * dialogs for {@link SpeciesEditorPane}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
final class SpeciesPresetActionHelper {

    private SpeciesPresetActionHelper() {
    }

    static boolean promptUnsavedChanges(SpeciesEditorPane pane) {
        if ("true".equals(System.getProperty("swarmforge.test")) || Boolean.getBoolean("headless") || Boolean.getBoolean("testfx.headless")) {
            return true;
        }
        if (!pane.isDirty) return true;

        I18nManager i18n = pane.i18n;
        String currentName = pane.lastSelectedPreset != null ? pane.lastSelectedPreset : "";
        boolean hasCurrentPreset = !currentName.isEmpty();

        Alert alert = ThemeManager.createAlert(
                Alert.AlertType.CONFIRMATION,
                i18n.get("dialog.unsaved.species_editor") + "\n"
                        + (hasCurrentPreset ? i18n.get("preset.current") + ": \"" + currentName + "\"" : "")
        );
        alert.setTitle(i18n.get("common.dialog.unsaved_title"));
        alert.setHeaderText(i18n.get("dialog.exit.species_editor"));

        ButtonType btnUpdate = hasCurrentPreset
                ? new ButtonType(i18n.get("preset.update") + " \"" + currentName + "\"", ButtonBar.ButtonData.OK_DONE)
                : null;
        ButtonType btnSaveAs = new ButtonType(i18n.get("preset.save_as"), ButtonBar.ButtonData.OTHER);
        ButtonType btnDiscard = new ButtonType(i18n.get("preset.discard"), ButtonBar.ButtonData.OTHER);
        ButtonType btnCancel = new ButtonType(i18n.get("common.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        if (btnUpdate != null) {
            alert.getButtonTypes().setAll(btnUpdate, btnSaveAs, btnDiscard, btnCancel);
        } else {
            alert.getButtonTypes().setAll(btnSaveAs, btnDiscard, btnCancel);
        }
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isEmpty() || result.get() == btnCancel) return false;
        if (result.get() == btnDiscard) {
            pane.isDirty = false;
            return true;
        }
        if (btnUpdate != null && result.get() == btnUpdate) {
            DefaultSpecies species = pane.buildSpeciesFromUI();
            species.setPresetName(currentName);
            pane.presetManager.addPreset(currentName, species);
            pane.isUpdatingFields = true;
            try {
                if (!pane.presetCombo.getItems().contains(currentName)) {
                    pane.presetCombo.getItems().add(currentName);
                }
                pane.presetCombo.getSelectionModel().select(currentName);
            } finally {
                pane.isUpdatingFields = false;
            }
            pane.lastSelectedPreset = currentName;
            pane.isDirty = false;
            NotificationOverlay.show(pane, "Preset \"" + currentName + "\" updated.", NotificationOverlay.NotificationType.SUCCESS);
            return true;
        }
        if (result.get() == btnSaveAs) {
            handleAddPreset(pane);
            return !pane.isDirty;
        }
        return false;
    }

    static void handleAddPreset(SpeciesEditorPane pane) {
        String editedText = pane.presetCombo.getEditor() != null ? pane.presetCombo.getEditor().getText().trim() : "";
        String common = pane.taxonomyPane.commonNameField.getText().trim();
        String scientific = pane.taxonomyPane.scientificNameField.getText().trim();

        String name = editedText;
        if (name.isEmpty()) {
            if (!common.isEmpty() && !scientific.isEmpty()) {
                name = common + " (" + scientific + ")";
            } else if (!common.isEmpty()) {
                name = common;
            } else if (!scientific.isEmpty()) {
                name = scientific;
            }
        }

        if (name.isEmpty()) {
            ThemeManager.createAlert(Alert.AlertType.WARNING, "Please specify a common name, scientific name, or preset name.").show();
            return;
        }

        if (pane.presetManager.contains(name)) {
            Alert confirmAlert = ThemeManager.createAlert(
                    Alert.AlertType.CONFIRMATION,
                    "Species preset '" + name + "' already exists.\n\nDo you want to replace it with current configuration?"
            );
            confirmAlert.setTitle("Replace Existing Preset");
            confirmAlert.setHeaderText("Replacement Confirmation");
            Optional<ButtonType> res = confirmAlert.showAndWait();
            if (res.isEmpty() || res.get() != ButtonType.OK) {
                return;
            }
        }

        DefaultSpecies species = pane.buildSpeciesFromUI();
        species.setPresetName(name);
        pane.presetManager.addPreset(name, species);

        pane.isUpdatingFields = true;
        try {
            if (!pane.presetCombo.getItems().contains(name)) {
                pane.presetCombo.getItems().add(name);
            }
            pane.presetCombo.getSelectionModel().select(name);
        } finally {
            pane.isUpdatingFields = false;
        }
        pane.lastSelectedPreset = name;
        pane.isDirty = false;

        NotificationOverlay.show(pane, "Species preset '" + name + "' saved.", NotificationOverlay.NotificationType.SUCCESS);
    }

    static void handleDeletePreset(SpeciesEditorPane pane) {
        String selected = pane.presetCombo.getValue();
        if (selected == null || selected.isEmpty()) return;

        I18nManager i18n = pane.i18n;
        Alert confirmAlert = ThemeManager.createAlert(Alert.AlertType.CONFIRMATION, String.format(i18n.get("preset.delete.confirm"), selected));
        confirmAlert.setTitle(i18n.get("preset.delete.title"));
        confirmAlert.setHeaderText(i18n.get("preset.delete.header.species"));

        confirmAlert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                pane.presetManager.delete(selected);
                pane.presetCombo.getItems().setAll(pane.presetManager.getPresetNames());
                if (!pane.presetCombo.getItems().isEmpty()) {
                    pane.presetCombo.getSelectionModel().selectFirst();
                } else {
                    pane.presetCombo.getSelectionModel().clearSelection();
                }
                NotificationOverlay.show(pane, "Species preset deleted.", NotificationOverlay.NotificationType.INFO);
            }
        });
    }

    static void handleSaveDisk(SpeciesEditorPane pane) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Species");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        chooser.setInitialFileName("species_" + pane.taxonomyPane.commonNameField.getText().replaceAll("\\s+", "_") + ".json");

        File f = chooser.showSaveDialog(pane.getScene().getWindow());
        if (f != null) {
            try {
                DefaultSpecies species = pane.buildSpeciesFromUI();
                pane.presetManager.saveToFile(f, species);
                ThemeManager.createAlert(Alert.AlertType.INFORMATION, "Species successfully saved as " + f.getName()).show();
            } catch (Exception ex) {
                ThemeManager.createAlert(Alert.AlertType.ERROR, "Save Error: " + ex.getMessage()).show();
            }
        }
    }

    static void handleLoadDisk(SpeciesEditorPane pane) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Load Species");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File f = chooser.showOpenDialog(pane.getScene().getWindow());
        if (f != null) {
            try {
                DefaultSpecies species = pane.presetManager.loadFromFile(f);
                pane.loadPresetToUI(species);
                ThemeManager.createAlert(Alert.AlertType.INFORMATION, "Espèce '" + species.getCommonName() + "' chargée avec succès!").show();
            } catch (Exception ex) {
                ThemeManager.createAlert(Alert.AlertType.ERROR, "Load Error: " + ex.getMessage()).show();
            }
        }
    }
}
