package org.swarmforge.client.util;

import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Utility for loading and applying window icons across SwarmForge JavaFX stages and dialogs.
 * Provides multi-resolution icon bindings and Windows AppUserModelID registration
 * to guarantee correct rendering on Windows taskbar, titlebar, Alt-Tab overlay, and high-DPI displays.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class IconUtils {

    private static final Logger LOG = Logger.getLogger(IconUtils.class.getName());
    private static final String ICON_PATH = "/icons/icon.png";
    private static final String APP_USER_MODEL_ID = "SwarmForge.SimulationStudio.App";

    private static final List<Image> CACHED_ICONS = new ArrayList<>();
    private static boolean taskbarIconSet = false;
    private static boolean appUserModelIdSet = false;

    /**
     * Applies multi-resolution application icons to the specified stage.
     * Also registers Windows AppUserModelID and updates AWT Taskbar icon.
     *
     * @param stage Target JavaFX stage
     */
    public static void applyWindowIcons(Stage stage) {
        try {
            // 1. Register Windows AppUserModelID once per process for taskbar icon separation & grouping
            if (!appUserModelIdSet) {
                setWindowsAppUserModelID(APP_USER_MODEL_ID);
                appUserModelIdSet = true;
            }

            // 2. Apply native OS Taskbar icon via java.awt.Taskbar once
            if (!taskbarIconSet && !java.awt.GraphicsEnvironment.isHeadless() && java.awt.Taskbar.isTaskbarSupported()) {
                java.awt.Taskbar taskbar = java.awt.Taskbar.getTaskbar();
                if (taskbar.isSupported(java.awt.Taskbar.Feature.ICON_IMAGE)) {
                    try (java.io.InputStream is = IconUtils.class.getResourceAsStream(ICON_PATH)) {
                        if (is != null) {
                            java.awt.image.BufferedImage awtImg = javax.imageio.ImageIO.read(is);
                            if (awtImg != null) {
                                taskbar.setIconImage(awtImg);
                                taskbarIconSet = true;
                                LOG.info("AWT Taskbar icon updated successfully.");
                            }
                        }
                    } catch (Exception ex) {
                        LOG.fine("Could not set AWT Taskbar icon: " + ex.getMessage());
                    }
                }
            }

            if (stage == null) return;

            // 3. Load & Cache JavaFX Icons (Original + Multi-Resolution) once
            if (CACHED_ICONS.isEmpty()) {
                URL iconUrl = IconUtils.class.getResource(ICON_PATH);
                if (iconUrl != null) {
                    try (java.io.InputStream is = iconUrl.openStream()) {
                        Image mainImage = new Image(is);
                        if (!mainImage.isError()) {
                            CACHED_ICONS.add(mainImage);
                        }
                    } catch (Exception e) {
                        LOG.warning("Could not load main icon image: " + e.getMessage());
                    }

                    int[] sizes = {16, 32, 48, 64, 128, 256};
                    for (int s : sizes) {
                        try (java.io.InputStream is = iconUrl.openStream()) {
                            Image iconSized = new Image(is, s, s, true, true);
                            if (!iconSized.isError()) {
                                CACHED_ICONS.add(iconSized);
                            }
                        } catch (Exception e) {
                            LOG.fine("Could not load icon size " + s + ": " + e.getMessage());
                        }
                    }
                } else {
                    LOG.warning("Could not find icon resource: " + ICON_PATH);
                }
            }

            // 4. Bind cached multi-resolution icons to stage
            if (!CACHED_ICONS.isEmpty()) {
                stage.getIcons().setAll(CACHED_ICONS);
            }
        } catch (Exception e) {
            LOG.warning("Failed to apply stage icons: " + e.getMessage());
        }
    }

    /**
     * Applies icon to a JavaFX Dialog or Alert window stage.
     *
     * @param dialog target Dialog window
     */
    public static void applyWindowIcons(javafx.scene.control.Dialog<?> dialog) {
        if (dialog == null) return;
        try {
            javafx.scene.control.DialogPane dialogPane = dialog.getDialogPane();
            if (dialogPane != null && dialogPane.getScene() != null) {
                javafx.stage.Window window = dialogPane.getScene().getWindow();
                if (window instanceof Stage stage) {
                    applyWindowIcons(stage);
                }
            }
        } catch (Exception e) {
            LOG.fine("Could not apply icon to dialog stage: " + e.getMessage());
        }
    }

    /**
     * Sets the Windows AppUserModelID via Win32 Shell32 API if running on Windows OS.
     * This prevents Windows Taskbar from falling back to generic javaw.exe icon.
     */
    private static void setWindowsAppUserModelID(String appId) {
        if (!System.getProperty("os.name", "").toLowerCase().contains("win")) {
            return;
        }

        try {
            Class<?> shell32Class = Class.forName("com.sun.jna.platform.win32.Shell32");
            Object instance = shell32Class.getField("INSTANCE").get(null);
            java.lang.reflect.Method method = shell32Class.getMethod("SetCurrentProcessExplicitAppUserModelID", String.class);
            method.invoke(instance, appId);
            LOG.info("SetCurrentProcessExplicitAppUserModelID set successfully via JNA to: " + appId);
        } catch (ClassNotFoundException e) {
            LOG.fine("JNA Shell32 not on classpath; using JavaFX stage icon fallback.");
        } catch (Exception e) {
            LOG.fine("Could not set AppUserModelID via JNA: " + e.getMessage());
        }
    }
}
