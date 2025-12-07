/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.logging.Logger;

/**
 * JavaFX client application for SwarmForge.
 * Provides 3D visualization and simulation controls.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SwarmForgeClient extends Application {

        private static final Logger LOG = Logger.getLogger(SwarmForgeClient.class.getName());

        private String serverHost = "localhost";
        private int serverPort = 50051;

        @Override
        public void start(Stage primaryStage) {
                LOG.info("Starting SwarmForge Client...");

                // Main layout
                BorderPane root = new BorderPane();

                // Menu bar
                MenuBar menuBar = createMenuBar();
                root.setTop(menuBar);

                // Center - 3D viewport
                StackPane viewport = new StackPane();
                viewport.setStyle("-fx-background-color: #000;");

                // Initialize JME view
                org.swarmforge.client.view.GameViewPane gamePane = new org.swarmforge.client.view.GameViewPane(1024,
                                720);
                viewport.getChildren().add(gamePane);

                root.setCenter(viewport);

                // Right - Control panel
                VBox controlPanel = createControlPanel();
                root.setRight(controlPanel);

                // Bottom - Status bar
                HBox statusBar = createStatusBar();
                root.setBottom(statusBar);

                // Scene setup
                Scene scene = new Scene(root, 1280, 720);
                scene.getStylesheets()
                                .add(getClass().getResource("/styles/dark-theme.css") != null
                                                ? getClass().getResource("/styles/dark-theme.css").toExternalForm()
                                                : "");

                primaryStage.setTitle("SwarmForge - Eusocial Insect Simulation");
                primaryStage.setScene(scene);
                primaryStage.show();

                LOG.info("Client started");
        }

        private MenuBar createMenuBar() {
                MenuBar menuBar = new MenuBar();

                // File menu
                Menu fileMenu = new Menu("File");
                fileMenu.getItems().addAll(
                                new MenuItem("Connect to Server..."),
                                new MenuItem("Load Checkpoint..."),
                                new MenuItem("Save Checkpoint..."),
                                new SeparatorMenuItem(),
                                new MenuItem("Exit"));

                // Simulation menu
                Menu simMenu = new Menu("Simulation");
                simMenu.getItems().addAll(
                                new MenuItem("Start"),
                                new MenuItem("Pause"),
                                new MenuItem("Stop"),
                                new SeparatorMenuItem(),
                                new MenuItem("Settings..."));

                // View menu
                Menu viewMenu = new Menu("View");
                viewMenu.getItems().addAll(
                                new CheckMenuItem("Show Pheromones"),
                                new CheckMenuItem("Show Tunnels"),
                                new CheckMenuItem("Show Statistics"),
                                new SeparatorMenuItem(),
                                new MenuItem("Reset Camera"));

                // Help menu
                Menu helpMenu = new Menu("Help");
                helpMenu.getItems().addAll(
                                new MenuItem("Documentation"),
                                new MenuItem("About SwarmForge"));

                menuBar.getMenus().addAll(fileMenu, simMenu, viewMenu, helpMenu);
                return menuBar;
        }

        private VBox createControlPanel() {
                VBox panel = new VBox(10);
                panel.setPrefWidth(250);
                panel.setStyle("-fx-background-color: #16213e; -fx-padding: 10;");

                // Connection status
                Label connLabel = new Label("Server: Disconnected");
                connLabel.setStyle("-fx-text-fill: #e94560;");

                // Simulation controls
                TitledPane simControls = new TitledPane("Simulation", new VBox(5,
                                new Button("▶ Start"),
                                new Button("⏸ Pause"),
                                new Button("⏹ Stop"),
                                new Separator(),
                                new Label("Speed:"),
                                new Slider(1, 120, 60)));

                // Statistics
                TitledPane stats = new TitledPane("Statistics", new VBox(5,
                                new Label("Tick: 0"),
                                new Label("Population: 0"),
                                new Label("Colonies: 0"),
                                new Label("FPS: 0")));

                // Camera controls
                TitledPane camera = new TitledPane("Camera", new VBox(5,
                                new Button("Top View"),
                                new Button("Side View"),
                                new Button("Follow Queen"),
                                new Separator(),
                                new Label("Zoom:"),
                                new Slider(0.1, 10, 1)));

                panel.getChildren().addAll(connLabel, simControls, stats, camera);
                return panel;
        }

        private HBox createStatusBar() {
                HBox bar = new HBox(20);
                bar.setStyle("-fx-background-color: #0f3460; -fx-padding: 5 10;");
                bar.getChildren().addAll(
                                new Label("Ready"),
                                new Label("Tick: 0"),
                                new Label("FPS: 0"));
                bar.getChildren().forEach(n -> ((Label) n).setStyle("-fx-text-fill: #e94560;"));
                return bar;
        }

        public static void main(String[] args) {
                launch(args);
        }
}
