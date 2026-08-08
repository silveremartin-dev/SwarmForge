/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.util;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Manages UI Themes (Dark / Light) across the SwarmForge application.
 * Allows dynamic stylesheet switching and scene registration.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ThemeManager {

    private static final Logger LOG = Logger.getLogger(ThemeManager.class.getName());
    private static final ThemeManager INSTANCE = new ThemeManager();

    public enum Theme {
        DARK("Dark Theme", "/styles/dark-theme.css"),
        LIGHT("Light Theme", "/styles/light-theme.css");

        private final String displayName;
        private final String cssPath;

        Theme(String displayName, String cssPath) {
            this.displayName = displayName;
            this.cssPath = cssPath;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getCssPath() {
            return cssPath;
        }
    }

    private final ObjectProperty<Theme> currentTheme = new SimpleObjectProperty<>(Theme.DARK);
    private final Set<Scene> registeredScenes = new HashSet<>();

    private ThemeManager() {
        currentTheme.addListener((obs, oldTheme, newTheme) -> {
            if (oldTheme != newTheme && newTheme != null) {
                updateAllScenes();
            }
        });
    }

    public static ThemeManager getInstance() {
        return INSTANCE;
    }

    public Theme getCurrentTheme() {
        return currentTheme.get();
    }

    public void setTheme(Theme theme) {
        if (theme != null) {
            this.currentTheme.set(theme);
        }
    }

    public ObjectProperty<Theme> currentThemeProperty() {
        return currentTheme;
    }

    public void registerScene(Scene scene) {
        if (scene != null) {
            registeredScenes.add(scene);
            applyTheme(scene);
        }
    }

    public void unregisterScene(Scene scene) {
        if (scene != null) {
            registeredScenes.remove(scene);
        }
    }

    public void applyTheme(Scene scene) {
        if (scene == null) return;
        Theme theme = currentTheme.get();
        scene.getStylesheets().clear();

        URL cssUrl = getClass().getResource(theme.getCssPath());
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            LOG.warning("Could not find stylesheet resource: " + theme.getCssPath());
        }
    }

    /**
     * Applies the current graphic theme (dark/light) to a JavaFX Dialog or Alert.
     */
    public void applyTheme(javafx.scene.control.Dialog<?> dialog) {
        if (dialog == null) return;
        javafx.scene.control.DialogPane dialogPane = dialog.getDialogPane();
        if (dialogPane != null) {
            Theme theme = currentTheme.get();
            dialogPane.getStylesheets().clear();
            URL cssUrl = getClass().getResource(theme.getCssPath());
            if (cssUrl != null) {
                dialogPane.getStylesheets().add(cssUrl.toExternalForm());
            }
            dialogPane.getStyleClass().add("custom-dialog");
        }
    }

    /**
     * Creates and returns a themed Alert dialog.
     */
    public static javafx.scene.control.Alert createAlert(javafx.scene.control.Alert.AlertType alertType, String contentText) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(alertType, contentText);
        getInstance().applyTheme(alert);
        return alert;
    }

    /**
     * Creates and returns a themed TextInputDialog.
     */
    public static javafx.scene.control.TextInputDialog createTextInputDialog(String defaultValue) {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog(defaultValue);
        getInstance().applyTheme(dialog);
        return dialog;
    }

    private void updateAllScenes() {
        registeredScenes.removeIf(scene -> scene.getWindow() == null && !registeredScenes.contains(scene));
        for (Scene scene : registeredScenes) {
            applyTheme(scene);
        }
    }
}
