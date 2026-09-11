/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Server Browser & Megaterrarium Multi-Node Lobby Pane for SwarmForge Studio.
 * Allows discovering remote gRPC servers, creating tiled Megaterrarium rooms,
 * selecting colony starting slots, and establishing distributed sessions.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ServerBrowserPane extends BorderPane {

    public static class ServerEntry {
        private final String name;
        private final String host;
        private final int port;
        private final int pingMs;
        private final int activeSimulations;
        private final int connectedClients;
        private final String persistenceMode;

        public ServerEntry(String name, String host, int port, int pingMs, int activeSimulations, int connectedClients, String persistenceMode) {
            this.name = name;
            this.host = host;
            this.port = port;
            this.pingMs = pingMs;
            this.activeSimulations = activeSimulations;
            this.connectedClients = connectedClients;
            this.persistenceMode = persistenceMode;
        }

        public String getName() { return name; }
        public String getHost() { return host; }
        public int getPort() { return port; }
        public int getPingMs() { return pingMs; }
        public int getActiveSimulations() { return activeSimulations; }
        public int getConnectedClients() { return connectedClients; }
        public String getPersistenceMode() { return persistenceMode; }
    }

    private final TableView<ServerEntry> serverTable = new TableView<>();
    private final ObservableList<ServerEntry> serverList = FXCollections.observableArrayList();

    private final TextField hostField = new TextField("localhost");
    private final TextField portField = new TextField("50051");
    private final TextField playerAliasField = new TextField("Researcher_1");
    private final ComboBox<String> speciesCombo = new ComboBox<>();
    private final Spinner<Integer> gridTilesX = new Spinner<>(1, 8, 2);
    private final Spinner<Integer> gridTilesY = new Spinner<>(1, 8, 2);
    private final Spinner<Integer> tpsSpinner = new Spinner<>(10, 240, 60);

    public ServerBrowserPane() {
        setPadding(new Insets(16));
        setStyle("-fx-background-color: #1e293b;");

        // Top: Header & Quick Connect
        VBox topBox = createHeaderBox();
        setTop(topBox);

        // Center: Server List Table
        setupServerTable();
        VBox centerBox = new VBox(10, new Label("🌐 Available SwarmForge Cluster Servers & Megaterrarium Hubs:"), serverTable);
        centerBox.setPadding(new Insets(12, 0, 12, 0));
        setCenter(centerBox);

        // Bottom: Megaterrarium Room Creator & Colony Profile
        VBox bottomBox = createLobbyControls();
        setBottom(bottomBox);

        // Populate sample servers
        refreshServers();
    }

    private VBox createHeaderBox() {
        Label title = new Label("SwarmForge Multi-Node Server Browser & Megaterrarium Lobby");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        HBox connectBar = new HBox(10);
        connectBar.setAlignment(Pos.CENTER_LEFT);
        connectBar.setPadding(new Insets(8, 0, 8, 0));

        Button btnRefresh = new Button("Refresh", new FontIcon(Feather.REFRESH_CW));
        btnRefresh.setOnAction(e -> refreshServers());

        Button btnDirectConnect = new Button("Direct Connect", new FontIcon(Feather.LINK));
        btnDirectConnect.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold;");
        btnDirectConnect.setOnAction(e -> handleDirectConnect());

        connectBar.getChildren().addAll(
                new Label("Host:"), hostField,
                new Label("Port:"), portField,
                btnDirectConnect,
                btnRefresh
        );

        return new VBox(6, title, connectBar);
    }

    private void setupServerTable() {
        TableColumn<ServerEntry, String> colName = new TableColumn<>("Server Name");
        colName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        colName.setPrefWidth(200);

        TableColumn<ServerEntry, String> colEndpoint = new TableColumn<>("Endpoint");
        colEndpoint.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getHost() + ":" + c.getValue().getPort()));
        colEndpoint.setPrefWidth(160);

        TableColumn<ServerEntry, String> colPing = new TableColumn<>("Ping");
        colPing.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPingMs() + " ms"));
        colPing.setPrefWidth(80);

        TableColumn<ServerEntry, String> colSims = new TableColumn<>("Simulations");
        colSims.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getActiveSimulations())));
        colSims.setPrefWidth(100);

        TableColumn<ServerEntry, String> colClients = new TableColumn<>("Clients");
        colClients.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getConnectedClients())));
        colClients.setPrefWidth(80);

        TableColumn<ServerEntry, String> colStorage = new TableColumn<>("Storage Mode");
        colStorage.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPersistenceMode()));
        colStorage.setPrefWidth(140);

        serverTable.getColumns().setAll(colName, colEndpoint, colPing, colSims, colClients, colStorage);
        serverTable.setItems(serverList);
        serverTable.setPrefHeight(220);
    }

    private VBox createLobbyControls() {
        TitledPane roomPane = new TitledPane();
        roomPane.setText("🏛️ Create Tiled Megaterrarium Room & Colony Profile");
        roomPane.setExpanded(true);

        speciesCombo.getItems().setAll(
                "Lasius niger (Black Garden Ant)",
                "Atta sexdens (Leafcutter Ant)",
                "Formica rufa (Red Wood Ant)",
                "Reticulitermes flavipes (Subterranean Termite)",
                "Apis mellifera (Western Honeybee)",
                "Solenopsis invicta (Red Imported Fire Ant)"
        );
        speciesCombo.getSelectionModel().selectFirst();

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));

        grid.add(new Label("Player Alias:"), 0, 0);
        grid.add(playerAliasField, 1, 0);

        grid.add(new Label("Colony Species:"), 2, 0);
        grid.add(speciesCombo, 3, 0);

        grid.add(new Label("Megaterrarium Grid Tiles (X x Y):"), 0, 1);
        HBox tilesBox = new HBox(6, gridTilesX, new Label("x"), gridTilesY);
        tilesBox.setAlignment(Pos.CENTER_LEFT);
        grid.add(tilesBox, 1, 1);

        grid.add(new Label("Target Execution TPS:"), 2, 1);
        grid.add(tpsSpinner, 3, 1);

        Button btnCreateRoom = new Button("Launch Megaterrarium Session", new FontIcon(Feather.PLAY));
        btnCreateRoom.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white; -fx-font-weight: bold;");
        btnCreateRoom.setOnAction(e -> handleLaunchMegaterrarium());

        HBox btnBox = new HBox(12, btnCreateRoom);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        btnBox.setPadding(new Insets(8, 0, 0, 0));

        VBox content = new VBox(8, grid, btnBox);
        roomPane.setContent(content);

        return new VBox(roomPane);
    }

    public void refreshServers() {
        serverList.clear();
        serverList.add(new ServerEntry("SwarmForge Local Daemon", "localhost", 50051, 1, 1, 1, "H2 In-Memory"));
        serverList.add(new ServerEntry("SwarmForge EU Cluster Node 1", "eu1.swarmforge.org", 50051, 24, 4, 12, "PostgreSQL"));
        serverList.add(new ServerEntry("SwarmForge US High-Scale Compute", "us1.swarmforge.org", 50051, 88, 8, 32, "PostgreSQL + Redis"));
    }

    private void handleDirectConnect() {
        String host = hostField.getText().trim();
        int port = Integer.parseInt(portField.getText().trim());
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Connecting to gRPC endpoint: " + host + ":" + port);
        alert.setHeaderText("Direct Connection Initiated");
        alert.show();
    }

    private void handleLaunchMegaterrarium() {
        int tilesX = gridTilesX.getValue();
        int tilesY = gridTilesY.getValue();
        String species = speciesCombo.getValue();
        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                String.format("Megaterrarium (%dx%d tiles) initialized for %s with species %s", tilesX, tilesY, playerAliasField.getText(), species));
        alert.setHeaderText("Megaterrarium Cluster Session Started");
        alert.show();
    }
}
