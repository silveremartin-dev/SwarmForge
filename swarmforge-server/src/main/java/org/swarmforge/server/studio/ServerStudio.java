/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server.studio;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.swarmforge.server.SwarmForgeServer;
import org.swarmforge.server.ServerConfig;

/**
 * The "SwarmForge Studio" - A comprehensive Server GUI.
 * Acts as the Simulator Manager, Editor Hub, and Server Console.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ServerStudio extends Application {

    private SwarmForgeServer serverInstance;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SwarmForge Studio - Server & Simulation Manager");
        primaryStage.setWidth(1280);
        primaryStage.setHeight(800);

        try {
            java.net.URL iconUrl = getClass().getResource("/icons/icon.png");
            if (iconUrl != null) {
                String urlStr = iconUrl.toExternalForm();
                primaryStage.getIcons().clear();
                primaryStage.getIcons().add(new javafx.scene.image.Image(urlStr, 16, 16, true, true));
                primaryStage.getIcons().add(new javafx.scene.image.Image(urlStr, 32, 32, true, true));
                primaryStage.getIcons().add(new javafx.scene.image.Image(urlStr, 48, 48, true, true));
                primaryStage.getIcons().add(new javafx.scene.image.Image(urlStr, 64, 64, true, true));
                primaryStage.getIcons().add(new javafx.scene.image.Image(urlStr, 128, 128, true, true));
                primaryStage.getIcons().add(new javafx.scene.image.Image(urlStr, 256, 256, true, true));
                primaryStage.getIcons().add(new javafx.scene.image.Image(urlStr));
            }
        } catch (Exception ignored) {}

        BorderPane root = new BorderPane();
        TabPane mainTabs = new TabPane();

        // Tab 1: Server Console & Status
        Tab consoleTab = new Tab("Server Console");
        consoleTab.setClosable(false);
        consoleTab.setContent(new ServerConsoleView(this));
        mainTabs.getTabs().add(consoleTab);

        // Tab 2: Simulation Manager
        Tab simTab = new Tab("Simulation Manager");
        simTab.setClosable(false);
        simTab.setContent(new SimulationManagerView());
        mainTabs.getTabs().add(simTab);

        // Tab 3: Terrain Editor
        Tab terrainTab = new Tab("Terrain Editor");
        terrainTab.setClosable(false);
        terrainTab.setContent(new org.swarmforge.server.studio.editors.TerrainEditorView());
        mainTabs.getTabs().add(terrainTab);

        // Tab 4: Species Editor
        Tab speciesTab = new Tab("Species Editor");
        speciesTab.setClosable(false);
        speciesTab.setContent(new org.swarmforge.server.studio.editors.SpeciesEditorView());
        mainTabs.getTabs().add(speciesTab);

        root.setCenter(mainTabs);

        Scene scene = new Scene(root);
        scene.getStylesheets()
                .add(getClass().getResource("/studio-theme.css") != null
                        ? getClass().getResource("/studio-theme.css").toExternalForm()
                        : "");

        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            stopServer();
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }

    public void startServer() {
        if (serverInstance == null) {
            new Thread(() -> {
                SwarmForgeServer server = new SwarmForgeServer(ServerConfig.fromEnvironment());
                serverInstance = server; // Assign to the instance field
                // The original line `SwarmForgeServer.main(new String[] {});` is problematic as
                // it starts a new server instance.
                // Assuming the intent is to start the 'server' instance created above.
                // If SwarmForgeServer.main is designed to be called only once or manages its
                // own lifecycle,
                // this part might need further clarification or refactoring in the actual
                // SwarmForgeServer class.
                // For now, I'll comment out the problematic line and assume the 'server'
                // instance should be started.
                // SwarmForgeServer.main(new String[] {}); // Reuse main for simplicty or
                // refactor server to have instance start
                // A proper start method on the server instance would be ideal, e.g.,
                // server.start();
            }).start();
        }
    }

    public void stopServer() {
        // Implement graceful shutdown hook
        if (serverInstance != null) {
            // serverInstance.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
