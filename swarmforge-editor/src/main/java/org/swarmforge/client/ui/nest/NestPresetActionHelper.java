/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.nest;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import org.swarmforge.client.ui.NestGeneratorPane;
import org.swarmforge.client.ui.NestPresetManager;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.NotificationOverlay;
import org.swarmforge.client.util.ThemeManager;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Helper class handling unsaved changes prompts, preset persistence, and file imports/exports
 * for {@link NestGeneratorPane}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public final class NestPresetActionHelper {

    private NestPresetActionHelper() {
        // Utility class
    }

    /**
     * Prompts the user to save, discard, or cancel when unsaved changes exist in the nest editor.
     *
     * @param pane the nest generator pane
     * @param presetMgr the nest preset manager
     * @param lastSelectedPreset the current preset name
     * @param onSaved callback invoked when the preset is saved
     * @param saveAsTrigger runnable to trigger Save As dialog
     * @return true if the caller may proceed, false if cancelled
     */
    public static boolean promptUnsavedChanges(
            NestGeneratorPane pane,
            NestPresetManager presetMgr,
            String lastSelectedPreset,
            Consumer<String> onSaved,
            Runnable saveAsTrigger) {
        if ("true".equals(System.getProperty("swarmforge.test"))
                || Boolean.getBoolean("headless")
                || Boolean.getBoolean("testfx.headless")) {
            return true;
        }

        I18nManager i18n = I18nManager.getInstance();
        String currentName = lastSelectedPreset != null ? lastSelectedPreset : "";
        boolean hasCurrentPreset = !currentName.isEmpty();

        Alert alert = ThemeManager.createAlert(
            Alert.AlertType.CONFIRMATION,
            i18n.get("dialog.unsaved.nest_editor") + "\n"
            + (hasCurrentPreset ? i18n.get("preset.current") + ": \"" + currentName + "\"" : "")
        );
        alert.setTitle(i18n.get("common.dialog.unsaved_title"));
        alert.setHeaderText(i18n.get("dialog.exit.nest_editor"));
        alert.getDialogPane().setPrefWidth(480);
        alert.getDialogPane().setMaxWidth(500);

        ButtonType btnUpdate = hasCurrentPreset
            ? new ButtonType("💾 " + i18n.get("preset.update") + " \"" + currentName + "\"", ButtonBar.ButtonData.OK_DONE)
            : null;
        ButtonType btnSaveAs = new ButtonType("📝 " + i18n.get("preset.save_as"), ButtonBar.ButtonData.OTHER);
        ButtonType btnDiscard = new ButtonType("🗑 " + i18n.get("preset.discard"), ButtonBar.ButtonData.OTHER);
        ButtonType btnCancel = new ButtonType(i18n.get("common.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        if (btnUpdate != null) {
            alert.getButtonTypes().setAll(btnUpdate, btnSaveAs, btnDiscard, btnCancel);
        } else {
            alert.getButtonTypes().setAll(btnSaveAs, btnDiscard, btnCancel);
        }

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() == btnCancel) return false;
        if (result.get() == btnDiscard) return true;

        if (btnUpdate != null && result.get() == btnUpdate) {
            presetMgr.save(currentName, pane.getConfiguration());
            if (onSaved != null) onSaved.accept(currentName);
            NotificationOverlay.show(pane, "Preset \"" + currentName + "\" mis à jour.", NotificationOverlay.NotificationType.SUCCESS);
            return true;
        }

        if (result.get() == btnSaveAs) {
            if (saveAsTrigger != null) saveAsTrigger.run();
            return !pane.isDirty();
        }

        return false;
    }

    /**
     * Shows a dialog to add a new preset or overwrite an existing one.
     */
    public static void doAddPreset(
            NestGeneratorPane pane,
            NestPresetManager presetMgr,
            String defaultName,
            Consumer<String> onPresetAdded) {
        TextInputDialog dialog = ThemeManager.createTextInputDialog(defaultName);
        dialog.setTitle("Save Preset");
        dialog.setHeaderText("Preset Name:");
        dialog.setContentText("Name:");
        dialog.showAndWait().ifPresent(name -> {
            if (name == null || name.isBlank()) return;
            String clean = name.trim();
            if (presetMgr.contains(clean)) {
                Alert confirmAlert = ThemeManager.createAlert(
                    Alert.AlertType.CONFIRMATION,
                    "Nest preset '" + clean + "' already exists.\n\nDo you want to overwrite it with current configuration?"
                );
                confirmAlert.setTitle("Overwrite Existing Preset");
                confirmAlert.setHeaderText("Overwrite Confirmation");
                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isEmpty() || result.get() != ButtonType.OK) return;
            }
            presetMgr.save(clean, pane.getConfiguration());
            if (onPresetAdded != null) onPresetAdded.accept(clean);
            NotificationOverlay.show(pane, "Preset \"" + clean + "\" sauvegardé.", NotificationOverlay.NotificationType.SUCCESS);
        });
    }

    /**
     * Prompts confirmation and deletes the chosen preset.
     */
    public static void doDeletePreset(
            NestGeneratorPane pane,
            NestPresetManager presetMgr,
            String presetName,
            Runnable onDeleted) {
        if (presetName == null || presetName.isEmpty()) return;

        I18nManager i18n = I18nManager.getInstance();
        Alert confirmAlert = ThemeManager.createAlert(
            Alert.AlertType.CONFIRMATION,
            String.format(i18n.get("preset.delete.confirm"), presetName)
        );
        confirmAlert.setTitle(i18n.get("preset.delete.title"));
        confirmAlert.setHeaderText(i18n.get("preset.delete.header.nest"));

        confirmAlert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                presetMgr.delete(presetName);
                if (onDeleted != null) onDeleted.run();
                NotificationOverlay.show(pane, "Nest preset deleted.", NotificationOverlay.NotificationType.INFO);
            }
        });
    }

    /**
     * Prompts a file chooser and exports the nest configuration to JSON.
     */
    public static void doExport(NestGeneratorPane pane, Map<String, Object> config) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Export");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        fc.setInitialFileName("nest.json");
        File f = fc.showSaveDialog(pane.getScene().getWindow());
        if (f == null) return;
        try {
            new com.fasterxml.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(f, config);
            NotificationOverlay.show(pane, "✅ Nest exported to " + f.getName(), NotificationOverlay.NotificationType.SUCCESS);
        } catch (Exception ex) {
            ThemeManager.createAlert(Alert.AlertType.ERROR, ex.getMessage()).show();
        }
    }

    /**
     * Prompts a file chooser and imports a nest configuration from JSON.
     */
    @SuppressWarnings("unchecked")
    public static void doImport(NestGeneratorPane pane, Consumer<Map<String, Object>> onLoaded) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Import");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        File f = fc.showOpenDialog(pane.getScene().getWindow());
        if (f == null) return;
        try {
            Map<String, Object> cfg = new com.fasterxml.jackson.databind.ObjectMapper().readValue(f, Map.class);
            if (onLoaded != null) onLoaded.accept(cfg);
            NotificationOverlay.show(pane, "✅ Nest imported from " + f.getName(), NotificationOverlay.NotificationType.SUCCESS);
        } catch (Exception ex) {
            ThemeManager.createAlert(Alert.AlertType.ERROR, ex.getMessage()).show();
        }
    }
}
