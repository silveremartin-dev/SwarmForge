package org.swarmforge.client.util;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Utility for loading and applying window icons across SwarmForge JavaFX stages and dialogs.
 * Provides multi-resolution icon bindings and Windows AppUserModelID / Win32 HWND registration
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
    private static File cachedIcoFile = null;

    /**
     * Suppresses noisy, benign warnings from jME3 third-party asset loaders (OBJLoader, Material color space).
     */
    public static void silenceJme3Warnings() {
        try {
            java.util.logging.Logger.getLogger("com.jme3.scene.plugins.OBJLoader").setLevel(java.util.logging.Level.SEVERE);
            java.util.logging.Logger.getLogger("com.jme3.scene.plugins.MTLLoader").setLevel(java.util.logging.Level.SEVERE);
            java.util.logging.Logger.getLogger("com.jme3.material.Material").setLevel(java.util.logging.Level.SEVERE);
            java.util.logging.Logger.getLogger("com.jme3.scene.plugins").setLevel(java.util.logging.Level.SEVERE);
            java.util.logging.Logger.getLogger("com.jme3.asset.DesktopAssetManager").setLevel(java.util.logging.Level.WARNING);
        } catch (Throwable ignored) {
        }
    }

    /**
     * Initializes early AppUserModelID on Windows OS before any GUI components are created.
     */
    public static void initEarlyTaskbarAppId() {
        silenceJme3Warnings();
        if (!appUserModelIdSet) {
            setWindowsAppUserModelID(APP_USER_MODEL_ID);
            appUserModelIdSet = true;
        }
    }

    /**
     * Applies multi-resolution application icons to the specified stage.
     * Also registers Windows AppUserModelID, updates AWT Taskbar icon, and binds Win32 HWND icons.
     *
     * @param stage Target JavaFX stage
     */
    public static void applyWindowIcons(Stage stage) {
        try {
            // 1. Register Windows AppUserModelID once per process for taskbar icon separation & grouping
            initEarlyTaskbarAppId();

            // 2. Apply native OS Taskbar icon via java.awt.Taskbar once if supported
            if (!taskbarIconSet && !java.awt.GraphicsEnvironment.isHeadless() && java.awt.Taskbar.isTaskbarSupported()) {
                try {
                    java.awt.Taskbar taskbar = java.awt.Taskbar.getTaskbar();
                    if (taskbar.isSupported(java.awt.Taskbar.Feature.ICON_IMAGE)) {
                        BufferedImage awtImg = loadAwtIconImage();
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

            // 5. Windows native HWND icon binding when stage becomes visible
            if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
                if (stage.isShowing()) {
                    applyNativeHwndIcons();
                } else {
                    stage.showingProperty().addListener((obs, oldVal, isShowing) -> {
                        if (isShowing) {
                            Platform.runLater(IconUtils::applyNativeHwndIcons);
                        }
                    });
                }
            }
        } catch (Exception e) {
            LOG.warning("Failed to apply stage icons: " + e.getMessage());
        }
    }

    public static BufferedImage loadAwtIconImage() {
        try (InputStream is = IconUtils.class.getResourceAsStream(ICON_PATH)) {
            if (is != null) {
                return ImageIO.read(is);
            }
        } catch (Exception ignored) {}

        // Fallback search on disk
        String[] fallbackPaths = {
            "icons/icon.png",
            "swarmforge-editor/src/main/resources/icons/icon.png",
            "swarmforge-client/src/main/resources/icons/icon.png"
        };
        for (String p : fallbackPaths) {
            File f = new File(p);
            if (f.exists()) {
                try {
                    return ImageIO.read(f);
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private static void loadCachedIcons() {
        CACHED_ICONS.clear();
        URL iconUrl = IconUtils.class.getResource(ICON_PATH);
        int[] sizes = {16, 24, 32, 48, 64, 128, 256};

        if (iconUrl != null) {
            String urlStr = iconUrl.toExternalForm();
            for (int s : sizes) {
                try {
                    Image fxImg = new Image(urlStr, s, s, true, true);
                    if (!fxImg.isError()) {
                        CACHED_ICONS.add(fxImg);
                    }
                } catch (Exception e) {
                    LOG.fine("Could not load icon size " + s + ": " + e.getMessage());
                }
            }
            try {
                Image fullImg = new Image(urlStr);
                if (!fullImg.isError()) {
                    CACHED_ICONS.add(fullImg);
                }
            } catch (Exception ignored) {}
        } else {
            // Fallback from disk/AWT
            BufferedImage baseAwt = loadAwtIconImage();
            if (baseAwt != null) {
                for (int s : sizes) {
                    try {
                        BufferedImage scaled = scaleImage(baseAwt, s, s);
                        Image fxImg = convertToFxImage(scaled);
                        if (fxImg != null && !fxImg.isError()) {
                            CACHED_ICONS.add(fxImg);
                        }
                    } catch (Exception e) {
                        LOG.fine("Could not rasterize fallback icon size " + s + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    private static BufferedImage scaleImage(BufferedImage src, int width, int height) {
        BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = dest.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(src, 0, 0, width, height, null);
        g2d.dispose();
        return dest;
    }

    private static Image convertToFxImage(BufferedImage bImg) {
        if (bImg == null) return null;
        WritableImage wr = new WritableImage(bImg.getWidth(), bImg.getHeight());
        PixelWriter pw = wr.getPixelWriter();
        for (int x = 0; x < bImg.getWidth(); x++) {
            for (int y = 0; y < bImg.getHeight(); y++) {
                pw.setArgb(x, y, bImg.getRGB(x, y));
            }
        }
        return wr;
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

    /**
     * Natively applies HICON to all HWNDs belonging to current Java process on Windows OS.
     */
    public static void applyNativeHwndIcons() {
        if (!System.getProperty("os.name", "").toLowerCase().contains("win")) {
            return;
        }

        try {
            File icoFile = getOrCreateAppIcoFile();
            if (icoFile == null || !icoFile.exists()) return;

            Class<?> user32Class = Class.forName("com.sun.jna.platform.win32.User32");
            Class<?> kernel32Class = Class.forName("com.sun.jna.platform.win32.Kernel32");

            Object user32 = user32Class.getField("INSTANCE").get(null);
            Object kernel32 = kernel32Class.getField("INSTANCE").get(null);

            java.lang.reflect.Method getCurrentProcessId = kernel32Class.getMethod("GetCurrentProcessId");
            int currentPid = (int) getCurrentProcessId.invoke(kernel32);

            java.lang.reflect.Method loadImage = user32Class.getMethod(
                "LoadImage",
                Class.forName("com.sun.jna.platform.win32.WinDef$HINSTANCE"),
                String.class,
                int.class,
                int.class,
                int.class,
                int.class
            );

            // WinUser constants: IMAGE_ICON = 1, LR_LOADFROMFILE = 0x0010
            Object hIconSmall = loadImage.invoke(user32, null, icoFile.getAbsolutePath(), 1, 16, 16, 0x0010);
            Object hIconBig = loadImage.invoke(user32, null, icoFile.getAbsolutePath(), 1, 32, 32, 0x0010);

            if (hIconSmall == null && hIconBig == null) return;

            Class<?> hwndClass = Class.forName("com.sun.jna.platform.win32.WinDef$HWND");
            Class<?> wndEnumProcClass = Class.forName("com.sun.jna.platform.win32.WinUser$WNDENUMPROC");

            java.lang.reflect.Method getWindowThreadProcessId = user32Class.getMethod(
                "GetWindowThreadProcessId",
                hwndClass,
                Class.forName("com.sun.jna.ptr.IntByReference")
            );

            Class<?> intByReferenceClass = Class.forName("com.sun.jna.ptr.IntByReference");
            java.lang.reflect.Method sendMessage = user32Class.getMethod(
                "SendMessage",
                hwndClass,
                int.class,
                Class.forName("com.sun.jna.platform.win32.WinDef$WPARAM"),
                Class.forName("com.sun.jna.platform.win32.WinDef$LPARAM")
            );

            Class<?> wparamClass = Class.forName("com.sun.jna.platform.win32.WinDef$WPARAM");
            Class<?> lparamClass = Class.forName("com.sun.jna.platform.win32.WinDef$LPARAM");
            Class<?> pointerClass = Class.forName("com.sun.jna.Pointer");
            java.lang.reflect.Method nativeValue = pointerClass.getMethod("nativeValue", pointerClass);

            Object wparam0 = wparamClass.getConstructor(long.class).newInstance(0L); // ICON_SMALL
            Object wparam1 = wparamClass.getConstructor(long.class).newInstance(1L); // ICON_BIG

            Object lparamSmall = null;
            if (hIconSmall != null) {
                try {
                    java.lang.reflect.Method hIconGetPointer = hIconSmall.getClass().getMethod("getPointer");
                    Object ptr = hIconGetPointer.invoke(hIconSmall);
                    long val = (long) nativeValue.invoke(null, ptr);
                    lparamSmall = lparamClass.getConstructor(long.class).newInstance(val);
                } catch (Exception ignored) {}
            }

            Object lparamBig = null;
            if (hIconBig != null) {
                try {
                    java.lang.reflect.Method hIconGetPointer = hIconBig.getClass().getMethod("getPointer");
                    Object ptr = hIconGetPointer.invoke(hIconBig);
                    long val = (long) nativeValue.invoke(null, ptr);
                    lparamBig = lparamClass.getConstructor(long.class).newInstance(val);
                } catch (Exception ignored) {}
            }

            final Object finalLparamSmall = lparamSmall;
            final Object finalLparamBig = lparamBig;

            Object wndEnumCallback = java.lang.reflect.Proxy.newProxyInstance(
                IconUtils.class.getClassLoader(),
                new Class<?>[]{wndEnumProcClass},
                (proxy, method, args) -> {
                    if ("callback".equals(method.getName())) {
                        Object hwnd = args[0];
                        Object pidRef = intByReferenceClass.getConstructor().newInstance();
                        getWindowThreadProcessId.invoke(user32, hwnd, pidRef);
                        java.lang.reflect.Method getValue = intByReferenceClass.getMethod("getValue");
                        int winPid = (int) getValue.invoke(pidRef);

                        if (winPid == currentPid) {
                            if (finalLparamSmall != null) {
                                sendMessage.invoke(user32, hwnd, 0x0080 /* WM_SETICON */, wparam0, finalLparamSmall);
                            }
                            if (finalLparamBig != null) {
                                sendMessage.invoke(user32, hwnd, 0x0080 /* WM_SETICON */, wparam1, finalLparamBig);
                            }
                        }
                        return true;
                    }
                    return null;
                }
            );

            java.lang.reflect.Method enumWindows = user32Class.getMethod("EnumWindows", wndEnumProcClass, Class.forName("com.sun.jna.Pointer"));
            enumWindows.invoke(user32, wndEnumCallback, null);
            LOG.info("Native Windows HWND taskbar icons applied successfully.");
        } catch (Throwable t) {
            LOG.fine("Native HWND icon attachment skipped: " + t.getMessage());
        }
    }

    private static synchronized File getOrCreateAppIcoFile() {
        if (cachedIcoFile != null && cachedIcoFile.exists()) {
            return cachedIcoFile;
        }

        try {
            BufferedImage baseImg = loadAwtIconImage();
            if (baseImg == null) return null;

            int[] resolutions = {16, 24, 32, 48, 64, 128, 256};
            List<byte[]> pngBytesList = new ArrayList<>();
            List<Integer> sizeList = new ArrayList<>();

            for (int r : resolutions) {
                BufferedImage scaled = scaleImage(baseImg, r, r);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(scaled, "png", baos);
                pngBytesList.add(baos.toByteArray());
                sizeList.add(r);
            }

            File tempIco = new File(System.getProperty("java.io.tmpdir"), "swarmforge_app_icon.ico");
            try (FileOutputStream fos = new FileOutputStream(tempIco)) {
                int count = pngBytesList.size();
                // 1. ICO Header (6 bytes)
                fos.write(new byte[]{0, 0, 1, 0, (byte) count, 0});

                int offset = 6 + (count * 16);
                for (int i = 0; i < count; i++) {
                    int sz = sizeList.get(i);
                    byte bSz = (byte) (sz >= 256 ? 0 : sz);
                    int dataLen = pngBytesList.get(i).length;

                    // 2. Icon Directory Entry (16 bytes each)
                    fos.write(new byte[]{
                        bSz, bSz, 0, 0, 1, 0, 32, 0,
                        (byte) (dataLen & 0xFF),
                        (byte) ((dataLen >> 8) & 0xFF),
                        (byte) ((dataLen >> 16) & 0xFF),
                        (byte) ((dataLen >> 24) & 0xFF),
                        (byte) (offset & 0xFF),
                        (byte) ((offset >> 8) & 0xFF),
                        (byte) ((offset >> 16) & 0xFF),
                        (byte) ((offset >> 24) & 0xFF)
                    });
                    offset += dataLen;
                }

                // 3. Image Data
                for (byte[] b : pngBytesList) {
                    fos.write(b);
                }
                fos.flush();
            }

            tempIco.deleteOnExit();
            cachedIcoFile = tempIco;
            return cachedIcoFile;
        } catch (Exception e) {
            LOG.fine("Could not create ICO file: " + e.getMessage());
            return null;
        }
    }
}
