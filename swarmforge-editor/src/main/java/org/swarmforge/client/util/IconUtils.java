/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.util;

import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.URL;
import java.util.logging.Logger;

/**
 * Utility for loading and applying window icons across SwarmForge JavaFX stages.
 * Provides multi-resolution icon bindings to guarantee correct rendering on Windows taskbar,
 * titlebar, Alt-Tab overlay, and high-DPI displays.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class IconUtils {

    private static final Logger LOG = Logger.getLogger(IconUtils.class.getName());
    private static final String ICON_PATH = "/icons/icon.png";

    /**
     * Applies multi-resolution application icons to the specified stage.
     *
     * @param stage Target JavaFX stage
     */
    public static void applyWindowIcons(Stage stage) {
        if (stage == null) return;
        try {
            URL iconUrl = IconUtils.class.getResource(ICON_PATH);
            if (iconUrl != null) {
                stage.getIcons().clear();
                // Load original resolution image synchronously via InputStream
                try (java.io.InputStream is = iconUrl.openStream()) {
                    Image mainImage = new Image(is);
                    if (!mainImage.isError()) {
                        stage.getIcons().add(mainImage);
                    }
                }
                // Add multi-resolution icons for Windows titlebar (16/32/48/64/128/256) loaded synchronously
                int[] sizes = {16, 32, 48, 64, 128, 256};
                for (int s : sizes) {
                    try (java.io.InputStream is = iconUrl.openStream()) {
                        Image iconSized = new Image(is, s, s, true, true);
                        if (!iconSized.isError()) {
                            stage.getIcons().add(iconSized);
                        }
                    }
                }

                // Apply native OS Taskbar icon via java.awt.Taskbar if supported
                try {
                    if (!java.awt.GraphicsEnvironment.isHeadless() && java.awt.Taskbar.isTaskbarSupported()) {
                        java.awt.Taskbar taskbar = java.awt.Taskbar.getTaskbar();
                        if (taskbar.isSupported(java.awt.Taskbar.Feature.ICON_IMAGE)) {
                            java.awt.image.BufferedImage awtImg = javax.imageio.ImageIO.read(iconUrl);
                            if (awtImg != null) {
                                taskbar.setIconImage(awtImg);
                            }
                        }
                    }
                } catch (Throwable t) {
                    LOG.fine("AWT Taskbar icon setting not supported on this platform: " + t.getMessage());
                }
            } else {
                LOG.warning("Could not find icon resource: " + ICON_PATH);
            }
        } catch (Exception e) {
            LOG.warning("Failed to apply stage icons: " + e.getMessage());
        }
    }
}
