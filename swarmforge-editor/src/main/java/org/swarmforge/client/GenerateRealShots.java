package org.swarmforge.client;

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

public class GenerateRealShots {

    public static void main(String[] args) {
        System.out.println("=== Starting GenerateRealShots via Platform.startup ===");

        CountDownLatch mainLatch = new CountDownLatch(1);

        try {
            Platform.startup(() -> {
                System.out.println("JavaFX Platform initialized.");
                try {
                    Stage stage = new Stage();
                    SwarmForgeClient app = new SwarmForgeClient();
                    app.start(stage);
                    stage.show();
                    stage.toFront();

                    Scene scene = stage.getScene();
                    TabPane tabPane = null;
                    if (scene.getRoot() instanceof javafx.scene.layout.BorderPane bp) {
                        if (bp.getCenter() instanceof TabPane tp) {
                            tabPane = tp;
                        }
                    }

                    if (tabPane == null) {
                        System.err.println("TabPane not found!");
                        mainLatch.countDown();
                        return;
                    }

                    final TabPane tp = tabPane;

                    new Thread(() -> {
                        try {
                            Thread.sleep(2000);
                            File dir = new File("docs/images/real_shots");
                            dir.mkdirs();
                            System.out.println("Created dir: " + dir.getAbsolutePath());

                            for (int i = 0; i < tp.getTabs().size(); i++) {
                                final int idx = i;
                                final Tab tab = tp.getTabs().get(i);
                                final String name = tab.getText() != null ? tab.getText().replaceAll("[^a-zA-Z0-9_-]", "_") : "tab_" + i;

                                CountDownLatch tabLatch = new CountDownLatch(1);
                                Platform.runLater(() -> {
                                    tp.getSelectionModel().select(idx);
                                    Platform.runLater(() -> {
                                        try {
                                            WritableImage fxImg = scene.snapshot(null);
                                            int w = (int) fxImg.getWidth();
                                            int h = (int) fxImg.getHeight();
                                            BufferedImage bImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                                            PixelReader rdr = fxImg.getPixelReader();
                                            for (int y = 0; y < h; y++) {
                                                for (int x = 0; x < w; x++) {
                                                    bImg.setRGB(x, y, rdr.getArgb(x, y));
                                                }
                                            }
                                            File file = new File(dir, String.format("real_shot_%02d_%s.png", idx + 1, name));
                                            ImageIO.write(bImg, "png", file);
                                            System.out.println("Captured: " + file.getAbsolutePath());
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        } finally {
                                            tabLatch.countDown();
                                        }
                                    });
                                });

                                tabLatch.await(5, TimeUnit.SECONDS);
                                Thread.sleep(500);
                            }
                            System.out.println("=== COMPLETED ALL SCREENSHOT CAPTURES ===");
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            Platform.exit();
                            mainLatch.countDown();
                        }
                    }).start();

                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.exit();
                    mainLatch.countDown();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            mainLatch.countDown();
        }

        try {
            mainLatch.await(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Exiting GenerateRealShots.");
        System.exit(0);
    }
}
