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
    public static final String ICON_PATH = "/icons/icon.png";
    public static final String APP_USER_MODEL_ID = "SwarmForge.SimulationStudio.App";

    private static final List<Image> CACHED_ICONS = new ArrayList<>();
    private static boolean taskbarIconSet = false;
    private static boolean appUserModelIdSet = false;

    /**
     * Initializes early AppUserModelID on Windows OS before any GUI components are created.
     */
    public static void initEarlyTaskbarAppId() {
        if (!appUserModelIdSet) {
            setWindowsAppUserModelID(APP_USER_MODEL_ID);
            appUserModelIdSet = true;
        }
    }

    /**
     * Applies multi-resolution application icons to the specified stage.
     * Also registers Windows AppUserModelID and updates AWT Taskbar icon.
     *
     * @param stage Target JavaFX stage
     */
    public static void applyWindowIcons(Stage stage) {
        try {
            // 1. Register Windows AppUserModelID once per process for taskbar icon separation & grouping
            initEarlyTaskbarAppId();

            // 2. Apply native OS Taskbar icon via java.awt.Taskbar once
            if (!taskbarIconSet && !java.awt.GraphicsEnvironment.isHeadless() && java.awt.Taskbar.isTaskbarSupported()) {
                try {
                    java.awt.Taskbar taskbar = java.awt.Taskbar.getTaskbar();
                    if (taskbar.isSupported(java.awt.Taskbar.Feature.ICON_IMAGE)) {
                        java.awt.image.BufferedImage awtImg = loadAwtIconImage();
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

            if (stage == null) return;

            // 3. Load & Cache JavaFX Icons (Original + Multi-Resolution) once
            if (CACHED_ICONS.isEmpty()) {
                loadCachedIcons();
            }

            // 4. Bind cached multi-resolution icons to stage
            if (!CACHED_ICONS.isEmpty()) {
                stage.getIcons().setAll(CACHED_ICONS);
            }
        } catch (Exception e) {
            LOG.warning("Failed to apply stage icons: " + e.getMessage());
        }
    }

    private static java.awt.image.BufferedImage loadAwtIconImage() {
        try (java.io.InputStream is = IconUtils.class.getResourceAsStream(ICON_PATH)) {
            if (is != null) {
                return javax.imageio.ImageIO.read(is);
            }
        } catch (Exception ignored) {}

        // Fallback search on disk
        String[] fallbackPaths = {
            "icons/icon.png",
            "swarmforge-editor/src/main/resources/icons/icon.png",
            "swarmforge-client/src/main/resources/icons/icon.png"
        };
        for (String p : fallbackPaths) {
            java.io.File f = new java.io.File(p);
            if (f.exists()) {
                try {
                    return javax.imageio.ImageIO.read(f);
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private static void loadCachedIcons() {
        int[] sizes = {16, 24, 32, 48, 64, 128, 256};
        URL iconUrl = IconUtils.class.getResource(ICON_PATH);

        if (iconUrl != null) {
            for (int s : sizes) {
                try {
                    Image iconSized = new Image(iconUrl.toExternalForm(), s, s, true, true, false);
                    if (!iconSized.isError()) {
                        CACHED_ICONS.add(iconSized);
                    }
                } catch (Exception e) {
                    LOG.fine("Could not load icon size " + s + ": " + e.getMessage());
                }
            }
        } else {
            // Fallback via stream or disk
            try (java.io.InputStream is = IconUtils.class.getResourceAsStream(ICON_PATH)) {
                if (is != null) {
                    Image mainImage = new Image(is);
                    if (!mainImage.isError()) {
                        CACHED_ICONS.add(mainImage);
                    }
                }
            } catch (Exception ignored) {}

            String[] fallbackPaths = {
                "icons/icon.png",
                "swarmforge-editor/src/main/resources/icons/icon.png",
                "swarmforge-client/src/main/resources/icons/icon.png"
            };
            for (String p : fallbackPaths) {
                java.io.File f = new java.io.File(p);
                if (f.exists()) {
                    try {
                        String fileUrl = f.toURI().toURL().toExternalForm();
                        for (int s : sizes) {
                            Image iconSized = new Image(fileUrl, s, s, true, true, false);
                            if (!iconSized.isError()) {
                                CACHED_ICONS.add(iconSized);
                            }
                        }
                        break;
                    } catch (Exception ignored) {}
                }
            }
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
            try {
                java.lang.reflect.Method method = shell32Class.getMethod("SetCurrentProcessExplicitAppUserModelID", String.class);
                method.invoke(instance, appId);
                LOG.info("SetCurrentProcessExplicitAppUserModelID set successfully via JNA (String) to: " + appId);
                return;
            } catch (NoSuchMethodException e) {
                Class<?> wStringClass = Class.forName("com.sun.jna.WString");
                Object wString = wStringClass.getConstructor(String.class).newInstance(appId);
                java.lang.reflect.Method method = shell32Class.getMethod("SetCurrentProcessExplicitAppUserModelID", wStringClass);
                method.invoke(instance, wString);
                LOG.info("SetCurrentProcessExplicitAppUserModelID set successfully via JNA (WString) to: " + appId);
                return;
            }
        } catch (ClassNotFoundException e) {
            LOG.fine("JNA Shell32 not on classpath; trying direct Native Function fallback.");
        } catch (Throwable e) {
            LOG.fine("Could not set AppUserModelID via Shell32 instance: " + e.getMessage());
        }

        // Direct JNA Native / Function invocation fallback
        try {
            Class<?> nativeClass = Class.forName("com.sun.jna.Native");
            Class<?> functionClass = Class.forName("com.sun.jna.Function");
            java.lang.reflect.Method getFunction = functionClass.getMethod("getFunction", String.class, String.class);
            Object func = getFunction.invoke(null, "shell32", "SetCurrentProcessExplicitAppUserModelID");
            Class<?> wStringClass = Class.forName("com.sun.jna.WString");
            Object wString = wStringClass.getConstructor(String.class).newInstance(appId);
            java.lang.reflect.Method invoke = functionClass.getMethod("invoke", Class.class, Object[].class);
            invoke.invoke(func, int.class, new Object[]{new Object[]{wString}});
            LOG.info("SetCurrentProcessExplicitAppUserModelID set via direct JNA Function to: " + appId);
        } catch (Throwable t) {
            LOG.fine("Native JNA fallback for AppUserModelID skipped: " + t.getMessage());
        }
    }
}
