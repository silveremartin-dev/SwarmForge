package org.swarmforge.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Capture real UI screenshots of each tab in SwarmForge Studio directly from JavaFX.
 */
public class CaptureRealScreenshots extends Application {

    private static final Logger LOG = Logger.getLogger(CaptureRealScreenshots.class.getName());

    @Override
    public void start(Stage primaryStage) {
        System.out.println(">>> STARTING CaptureRealScreenshots JavaFX App <<<");
        SwarmForgeClient clientApp = new SwarmForgeClient();
        
        try {
            clientApp.start(primaryStage);
            primaryStage.show();
            primaryStage.toFront();
        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
            return;
        }

        Scene scene = primaryStage.getScene();
        TabPane tabPane = null;
        if (scene.getRoot() instanceof javafx.scene.layout.BorderPane bp) {
            if (bp.getCenter() instanceof TabPane tp) {
                tabPane = tp;
            }
        }

        if (tabPane == null) {
            System.err.println("CRITICAL: Could not find TabPane in root layout!");
            Platform.exit();
            return;
        }

        final TabPane finalTabPane = tabPane;

        new Thread(() -> {
            try {
                System.out.println("Waiting 2.5 seconds for UI scene initialization and CSS styles...");
                Thread.sleep(2500);

                File outputDir = new File("c:/Silvere/Encours/Developpement/SwarmForge/docs/images/real_shots");
                outputDir.mkdirs();
                System.out.println("Output directory created: " + outputDir.getAbsolutePath());

                int tabCount = finalTabPane.getTabs().size();
                System.out.println("Total tabs to capture: " + tabCount);

                for (int i = 0; i < tabCount; i++) {
                    final int tabIdx = i;
                    final Tab tab = finalTabPane.getTabs().get(i);
                    final String tabName = tab.getText() != null ? tab.getText().replaceAll("[^a-zA-Z0-9_-]", "_") : "tab_" + i;

                    System.out.println("Capturing Tab " + (tabIdx + 1) + "/" + tabCount + ": " + tabName);
                    CountDownLatch latch = new CountDownLatch(1);

                    Platform.runLater(() -> {
                        finalTabPane.getSelectionModel().select(tabIdx);
                        Platform.runLater(() -> {
                            try {
                                WritableImage snapshot = scene.snapshot(null);
                                BufferedImage bImage = convertToBufferedImage(snapshot);
                                File outFile = new File(outputDir, String.format("shot_%02d_%s.png", tabIdx + 1, tabName));
                                ImageIO.write(bImage, "png", outFile);
                                System.out.println("SUCCESSFULLY SAVED REAL SCREENSHOT: " + outFile.getAbsolutePath());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            } finally {
                                latch.countDown();
                            }
                        });
                    });

                    boolean ok = latch.await(5, TimeUnit.SECONDS);
                    if (!ok) {
                        System.err.println("Timed out waiting for tab " + tabIdx);
                    }
                    Thread.sleep(800);
                }

                System.out.println(">>> ALL REAL SCREENSHOTS CAPTURED SUCCESSFULLY! <<<");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Platform.exit();
                System.exit(0);
            }
        }).start();
    }

    private static BufferedImage convertToBufferedImage(Image fxImage) {
        int w = (int) fxImage.getWidth();
        int h = (int) fxImage.getHeight();
        BufferedImage bImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        PixelReader reader = fxImage.getPixelReader();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                bImage.setRGB(x, y, reader.getArgb(x, y));
            }
        }
        return bImage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
