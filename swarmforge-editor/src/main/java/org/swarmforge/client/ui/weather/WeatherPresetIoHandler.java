/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.weather;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import org.swarmforge.client.ui.WeatherEditorPane;
import org.swarmforge.client.ui.WeatherPresetManager;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.NotificationOverlay;
import org.swarmforge.client.util.ThemeManager;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Handles preset file I/O, import/export, and unsaved changes confirmation dialogs for {@link WeatherEditorPane}.
 */
public class WeatherPresetIoHandler {

    private final WeatherEditorPane pane;
    private final WeatherPresetManager presetMgr;
    private final ComboBox<String> presetsCombo;
    private final Consumer<Map<String, Object>> applyConfigConsumer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WeatherPresetIoHandler(
            WeatherEditorPane pane,
            WeatherPresetManager presetMgr,
            ComboBox<String> presetsCombo,
            Consumer<Map<String, Object>> applyConfigConsumer) {
        this.pane = pane;
        this.presetMgr = presetMgr;
        this.presetsCombo = presetsCombo;
        this.applyConfigConsumer = applyConfigConsumer;
    }

    public void doSavePreset(Runnable onSaved) {
        TextInputDialog dialog = new TextInputDialog("My Climate Profile");
        dialog.setTitle("Save Climate Profile");
        dialog.setHeaderText("Create a new climate preset");
        dialog.setContentText("Profile Name:");

        dialog.showAndWait().ifPresent(name -> {
            String trimmed = name.trim();
            if (trimmed.isEmpty()) return;

            presetMgr.save(trimmed, pane.getConfiguration());
            refreshPresetsCombo();
            presetsCombo.setValue(trimmed);
            if (onSaved != null) onSaved.run();
            NotificationOverlay.show(pane, "Preset \"" + trimmed + "\" successfully saved!", NotificationOverlay.NotificationType.SUCCESS);
        });
    }

    public void doDeletePreset() {
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
                NotificationOverlay.show(pane, "Climate profile deleted.", NotificationOverlay.NotificationType.INFO);
            }
        });
    }

    public void doExport() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Exporter Profil Climat");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        File f = fc.showSaveDialog(pane.getScene().getWindow());
        if (f == null) return;
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(f, pane.getConfiguration());
            NotificationOverlay.show(pane, "Profile successfully exported!", NotificationOverlay.NotificationType.SUCCESS);
        } catch (Exception ex) {
            NotificationOverlay.show(pane, "Export error: " + ex.getMessage(), NotificationOverlay.NotificationType.ERROR);
        }
    }

    public void doImport() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Importer Profil Climat");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        File f = fc.showOpenDialog(pane.getScene().getWindow());
        if (f == null) return;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> cfg = objectMapper.readValue(f, Map.class);
            applyConfigConsumer.accept(cfg);
            NotificationOverlay.show(pane, "Profile successfully imported!", NotificationOverlay.NotificationType.SUCCESS);
        } catch (Exception ex) {
            NotificationOverlay.show(pane, "Import error: " + ex.getMessage(), NotificationOverlay.NotificationType.ERROR);
        }
    }

    public void refreshPresetsCombo() {
        String cur = presetsCombo.getValue();
        presetsCombo.getItems().setAll(presetMgr.names());
        FXCollections.sort(presetsCombo.getItems());
        if (cur != null && presetsCombo.getItems().contains(cur)) {
            presetsCombo.setValue(cur);
        }
    }

    public boolean promptUnsavedChanges(String lastSelectedPreset, Runnable onDiscard, Consumer<String> onUpdated) {
        if ("true".equals(System.getProperty("swarmforge.test")) || Boolean.getBoolean("headless") || Boolean.getBoolean("testfx.headless")) return true;
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
        if (result.get() == btnDiscard) {
            if (onDiscard != null) onDiscard.run();
            return true;
        }
        if (btnUpdate != null && result.get() == btnUpdate) {
            presetMgr.save(currentName, pane.getConfiguration());
            refreshPresetsCombo();
            presetsCombo.setValue(currentName);
            if (onUpdated != null) onUpdated.accept(currentName);
            NotificationOverlay.show(pane, "Preset \"" + currentName + "\" updated.", NotificationOverlay.NotificationType.SUCCESS);
            return true;
        }
        if (result.get() == btnSaveAs) {
            doSavePreset(onDiscard);
            return !pane.isDirty();
        }
        return false;
    }
}
