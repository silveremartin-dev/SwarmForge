/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.swarmforge.client.network.SimulationClient;
import org.swarmforge.client.ui.WorldEditorPane;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.species.*;
import org.swarmforge.core.world.Biome;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * SwarmForge Client - High-Fidelity 3D Simulation Viewer & God Mode Controller.
 * Connects to a SwarmForge Server via gRPC or runs local simulated terrariums with full JME3 PBR graphics.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ClientApp extends Application {

    private static final Logger LOG = Logger.getLogger(ClientApp.class.getName());

    private String serverHost = "localhost";
    private int serverPort = 50051;
    private boolean godModeEnabled = true;

    private SimulationClient networkClient;
    private Simulation localSimulation;
    private WorldEditorPane worldEditorPane;

    @Override
    public void start(Stage primaryStage) {
        LOG.info("Starting SwarmForge Dedicated Client...");

        // Load multi-resolution application icons
        try {
            java.net.URL iconUrl = getClass().getResource("/icons/icon.png");
            if (iconUrl != null) {
                String urlStr = iconUrl.toExternalForm();
                primaryStage.getIcons().clear();
                primaryStage.getIcons().add(new javafx.scene.image.Image(urlStr, 16, 16, true, true));
                primaryStage.getIcons().add(new javafx.scene.image.Image(urlStr, 32, 32, true, true));
                primaryStage.getIcons().add(new javafx.scene.image.Image(urlStr, 64, 64, true, true));
                primaryStage.getIcons().add(new javafx.scene.image.Image(urlStr));
            }
        } catch (Exception ignored) {}

        showConnectionDialog(primaryStage);
    }

    private void showConnectionDialog(Stage primaryStage) {
        VBox root = new VBox(18);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #0d1117;");

        // Header Title
        HBox titleBox = new HBox(12);
        titleBox.setAlignment(Pos.CENTER);
        Label title = new Label(I18nManager.getInstance().get("client.title"));
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");
        titleBox.getChildren().add(title);

        Label subtitle = new Label(I18nManager.getInstance().get("client.subtitle"));
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");

        // Connection Form
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setAlignment(Pos.CENTER);

        Label lblHost = new Label(I18nManager.getInstance().get("client.host"));
        lblHost.setStyle("-fx-text-fill: #e2e8f0;");
        TextField txtHost = new TextField(serverHost);
        txtHost.setPromptText("localhost");

        Label lblPort = new Label(I18nManager.getInstance().get("client.port"));
        lblPort.setStyle("-fx-text-fill: #e2e8f0;");
        TextField txtPort = new TextField(String.valueOf(serverPort));
        txtPort.setPromptText("50051");

        form.add(lblHost, 0, 0);
        form.add(txtHost, 1, 0);
        form.add(lblPort, 0, 1);
        form.add(txtPort, 1, 1);

        // God Mode Toggle
        CheckBox chkGodMode = new CheckBox(I18nManager.getInstance().get("client.godmode"));
        chkGodMode.setSelected(true);
        chkGodMode.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");

        // Simulation Selector
        ListView<String> simList = new ListView<>();
        simList.getItems().addAll(
                "🌍 Simulation Locale : Forêt Tempérée (Formica rufa)",
                "🏜️ Simulation Locale : Désert Saharien (Cataglyphis bombycina)",
                "🌴 Simulation Locale : Jungle Tropicale (Atta cephalotes)",
                "🏔️ Simulation Locale : Forêt Boréale (Camponotus pennsylvanicus)"
        );
        simList.getSelectionModel().select(0);
        simList.setPrefHeight(130);
        simList.setStyle("-fx-control-inner-background: #1e293b; -fx-text-fill: white;");

        // Action Buttons
        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);

        Button btnConnectServer = new Button("🌐 " + I18nManager.getInstance().get("client.connect"));
        btnConnectServer.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");
        btnConnectServer.setOnAction(e -> {
            serverHost = txtHost.getText().trim();
            try {
                serverPort = Integer.parseInt(txtPort.getText().trim());
            } catch (Exception ex) {
                serverPort = 50051;
            }
            godModeEnabled = chkGodMode.isSelected();
            connectToServer(primaryStage, serverHost, serverPort);
        });

        Button btnLaunchLocal = new Button("🚀 Lancer Simulation");
        btnLaunchLocal.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");
        btnLaunchLocal.setOnAction(e -> {
            godModeEnabled = chkGodMode.isSelected();
            int selectedIdx = simList.getSelectionModel().getSelectedIndex();
            launchLocalSimulation(primaryStage, selectedIdx);
        });

        buttons.getChildren().addAll(btnLaunchLocal, btnConnectServer);

        Label status = new Label("Prêt - Choisissez une simulation ou connectez-vous au serveur.");
        status.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

        root.getChildren().addAll(titleBox, subtitle, new Separator(), form, chkGodMode,
                new Label("Simulations disponibles :"), simList, buttons, status);

        Scene scene = new Scene(root, 540, 580);
        primaryStage.setTitle("SwarmForge Client - Sélecteur de Simulation");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void launchLocalSimulation(Stage primaryStage, int scenarioIdx) {
        LOG.info("Initializing high-fidelity local simulation scenario #" + scenarioIdx);

        int width = 64, height = 64, depth = 20;
        Terrarium terrarium = new Terrarium(width, height, depth);
        int surfaceLevel = 5;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    TerrariumCell.Material mat = (z < surfaceLevel) ? TerrariumCell.Material.AIR : TerrariumCell.Material.EARTH;
                    terrarium.setCell(new TerrariumCell(
                            x, y, z, mat,
                            new float[TerrariumCell.PHEROMONE_TYPES], 22.0f, 60.0f));
                }
            }
        }

        Biome biome = switch (scenarioIdx) {
            case 1 -> Biome.DESERT;
            case 2 -> Biome.TROPICAL;
            case 3 -> Biome.ALPINE_SNOW;
            default -> Biome.FOREST;
        };
        terrarium.setLatitude(biome.getTypicalLatitude());

        this.localSimulation = new Simulation(terrarium);
        Species species = switch (scenarioIdx) {
            case 1 -> new CataglyphisBombycina();
            case 2 -> new AttaCephalotes();
            case 3 -> new CamponotusPennsylvanicus();
            default -> new FormicaRufa();
        };

        Colony colony = new Colony(species, width / 2.0f, height / 2.0f, 5.0f);
        colony.addProtein(5000.0f);
        colony.addCarbohydrate(5000.0f);
        colony.setWaterStored(5000.0f);
        colony.createQueens(1);

        // Demographics
        int pop = 50;
        for (int i = 0; i < pop; i++) {
            Individual.Caste caste = (i % 6 == 0) ? Individual.Caste.SOLDIER : Individual.Caste.WORKER;
            float x = width / 2.0f + (float) (Math.random() * 8 - 4);
            float y = height / 2.0f + (float) (Math.random() * 8 - 4);
            Individual ind = new Individual(UUID.randomUUID(), caste, x, y, 5.0f);
            ind.setHeading((float) (Math.random() * Math.PI * 2));
            colony.addIndividual(ind);
        }

        this.localSimulation.addColony(colony);
        this.localSimulation.start();

        setupClientSimulationUI(primaryStage, "Simulation Locale (" + biome.getDisplayName() + ")");
    }

    private void connectToServer(Stage primaryStage, String host, int port) {
        LOG.info("Connecting to gRPC Server at " + host + ":" + port);
        try {
            this.networkClient = new SimulationClient();
            this.networkClient.connect(host, port);
            setupClientSimulationUI(primaryStage, "Serveur SwarmForge (" + host + ":" + port + ")");
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Impossible de se connecter au serveur " + host + ":" + port + "\n" + e.getMessage());
            alert.showAndWait();
        }
    }

    private void setupClientSimulationUI(Stage primaryStage, String sessionTitle) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0b0f19;");

        // Main 3D World Viewport
        this.worldEditorPane = new WorldEditorPane();
        this.worldEditorPane.setSimulationMode(true);
        this.worldEditorPane.setActive(true);

        if (localSimulation != null) {
            this.worldEditorPane.setSimulation(localSimulation);
        }

        root.setCenter(this.worldEditorPane);

        // God Mode Control Panel (Right dock)
        if (godModeEnabled) {
            VBox godPanel = createGodModeSidebar();
            root.setRight(godPanel);
        }

        Scene scene = new Scene(root, 1280, 800);
        primaryStage.setTitle("SwarmForge Client - " + sessionTitle);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);

        primaryStage.setOnCloseRequest(e -> {
            if (localSimulation != null) {
                localSimulation.stop();
            }
            if (networkClient != null) {
                networkClient.disconnect();
            }
            Platform.exit();
            System.exit(0);
        });
    }

    private VBox createGodModeSidebar() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(15));
        panel.setPrefWidth(260);
        panel.setStyle("-fx-background-color: #111827; -fx-border-color: #1f2937; -fx-border-width: 0 0 0 1;");

        Label title = new Label("⚡ GOD MODE CONTROLLER");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #f59e0b;");

        // Spawn Interventions
        Label lblSpawn = new Label("🌱 Ressources & Individus");
        lblSpawn.setStyle("-fx-font-weight: bold; -fx-text-fill: #94a3b8;");

        Button btnAddFood = new Button("🍯 Nourriture (+1000)");
        btnAddFood.setMaxWidth(Double.MAX_VALUE);
        btnAddFood.setStyle("-fx-background-color: #065f46; -fx-text-fill: #34d399; -fx-font-weight: bold;");
        btnAddFood.setOnAction(e -> {
            if (localSimulation != null && !localSimulation.getColonies().isEmpty()) {
                Colony col = localSimulation.getColonies().get(0);
                col.addCarbohydrate(1000.0f);
                col.addProtein(1000.0f);
                LOG.info("God Mode: 1000 food added to colony");
            }
        });

        Button btnSpawnWorkers = new Button("🐜 Invoquer 10 Ouvrières");
        btnSpawnWorkers.setMaxWidth(Double.MAX_VALUE);
        btnSpawnWorkers.setStyle("-fx-background-color: #1e3a8a; -fx-text-fill: #60a5fa; -fx-font-weight: bold;");
        btnSpawnWorkers.setOnAction(e -> {
            if (localSimulation != null && !localSimulation.getColonies().isEmpty()) {
                Colony col = localSimulation.getColonies().get(0);
                for (int i = 0; i < 10; i++) {
                    Individual ind = new Individual(UUID.randomUUID(), Individual.Caste.WORKER, col.getNestX() + (float)(Math.random() * 4 - 2), col.getNestY() + (float)(Math.random() * 4 - 2), col.getNestZ());
                    col.addIndividual(ind);
                }
                LOG.info("God Mode: 10 workers spawned");
            }
        });

        // Climate & Disaster Interventions
        Label lblClimate = new Label("⛈️ Climat & Phénomènes");
        lblClimate.setStyle("-fx-font-weight: bold; -fx-text-fill: #94a3b8;");

        Button btnTriggerRain = new Button("🌧️ Déclencher Averse");
        btnTriggerRain.setMaxWidth(Double.MAX_VALUE);
        btnTriggerRain.setStyle("-fx-background-color: #075985; -fx-text-fill: #38bdf8;");
        btnTriggerRain.setOnAction(e -> {
            if (localSimulation != null && localSimulation.getWeather() != null) {
                localSimulation.getWeather().setRainfall(15.0f);
                LOG.info("God Mode: Rainstorm triggered");
            }
        });

        Button btnTriggerHeat = new Button("☀️ Vague de Chaleur (35°C)");
        btnTriggerHeat.setMaxWidth(Double.MAX_VALUE);
        btnTriggerHeat.setStyle("-fx-background-color: #9a3412; -fx-text-fill: #fb923c;");
        btnTriggerHeat.setOnAction(e -> {
            if (localSimulation != null && localSimulation.getWeather() != null) {
                localSimulation.getWeather().setTemperature(35.0f);
                localSimulation.getWeather().setRainfall(0.0f);
                LOG.info("God Mode: Heat wave triggered");
            }
        });

        Button btnFlight = new Button("👑 Vol Nuptial");
        btnFlight.setMaxWidth(Double.MAX_VALUE);
        btnFlight.setStyle("-fx-background-color: #701a75; -fx-text-fill: #f472b6; -fx-font-weight: bold;");
        btnFlight.setOnAction(e -> {
            if (localSimulation != null && localSimulation.getWeather() != null) {
                localSimulation.getWeather().setTemperature(26.0f);
                localSimulation.getWeather().setHumidity(75.0f);
                LOG.info("God Mode: Nuptial flight conditions activated");
            }
        });

        // Destruction / Population Control
        Label lblDestroy = new Label("⚡ Contrôle de Population");
        lblDestroy.setStyle("-fx-font-weight: bold; -fx-text-fill: #94a3b8;");

        Button btnCullHalf = new Button("💀 Réduire Population (-50%)");
        btnCullHalf.setMaxWidth(Double.MAX_VALUE);
        btnCullHalf.setStyle("-fx-background-color: #7f1d1d; -fx-text-fill: #f87171;");
        btnCullHalf.setOnAction(e -> {
            if (localSimulation != null && !localSimulation.getColonies().isEmpty()) {
                Colony col = localSimulation.getColonies().get(0);
                var living = col.getLivingIndividuals();
                int toKill = living.size() / 2;
                for (int i = 0; i < toKill && i < living.size(); i++) {
                    living.get(i).setHealth(0);
                }
                LOG.info("God Mode: Culled " + toKill + " individuals");
            }
        });

        panel.getChildren().addAll(
                title, new Separator(),
                lblSpawn, btnAddFood, btnSpawnWorkers,
                new Separator(),
                lblClimate, btnTriggerRain, btnTriggerHeat, btnFlight,
                new Separator(),
                lblDestroy, btnCullHalf
        );

        return panel;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
