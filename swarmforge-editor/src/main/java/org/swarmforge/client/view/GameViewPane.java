/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view;

import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import com.jme3.system.JmeContext;

/**
 * JavaFX pane hosting the JMonkeyEngine view.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class GameViewPane extends Pane {

    private final ImageView imageView;
    private final JmeGameApp gameApp;
    private final WritableImage writableImage;

    public GameViewPane(int width, int height) {
        this.setPrefSize(width, height);

        writableImage = new WritableImage(width, height);
        imageView = new ImageView(writableImage);
        getChildren().add(imageView);

        gameApp = new JmeGameApp(width, height);
        gameApp.setTargetImage(writableImage);

        // Start JME in separate daemon thread
        Thread jmeThread = new Thread(() -> {
            gameApp.start(JmeContext.Type.OffscreenSurface);
        }, "JME-Main");
        jmeThread.setDaemon(true);
        jmeThread.start();
    }

    public JmeGameApp getGameApp() {
        return gameApp;
    }

    public void setGamifiedVoxelMode(boolean gamified) {
        if (gameApp != null) {
            gameApp.setGamifiedVoxelMode(gamified);
        }
    }

    public void stop() {
        gameApp.stop();
    }

    public void setTerrainListener(JmeGameApp.TerrainModificationListener listener) {
        gameApp.setTerrainListener(listener);
    }
}
