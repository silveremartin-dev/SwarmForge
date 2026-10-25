/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.control;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.swarmforge.client.ui.SpeciesPresetManager;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.NotificationOverlay;

import java.util.ArrayList;
import java.util.List;

/**
 * UI Component for Multiplayer Server Connection, Matchmaking Lobby & Megaterrarium Sharding Topology.
 * Encapsulates:
 * <ul>
 *   <li>Distributed gRPC server connection controls (host, port, ping/status).</li>
 *   <li>Server role selection (Join / Matchmaking vs Host / Deploy).</li>
 *   <li>Player profile (alias, species choice).</li>
 *   <li>Lobby readiness controls & match start trigger.</li>
 *   <li>Dynamic mode explainer card.</li>
 *   <li>Megaterrarium multi-node spatial sharding configuration (tuiles X/Y, player count).</li>
 * </ul>
 */
public class MultiplayerServerCard {

    private final I18nManager i18n = I18nManager.getInstance();

    private final VBox serverNetworkBox = new VBox(8);
    private final TextField txtServerHost = new TextField("localhost");
    private final TextField txtServerPort = new TextField("50051");
    private final Button btnServerConnect = new Button("🌐 Connecter");
    private final Button btnServerStart = new Button("🚀 Démarrer Serveur...");
    private final Button btnServerDiscover = new Button("🔍 Détecter");
    private final Label lblServerConnectionStatus = new Label("○ Hors-ligne");
    private final Label lblServerSessionStats = new Label("");

    private final ComboBox<String> comboExecutionMode = new ComboBox<>();
    private final ComboBox<String> comboServerRole = new ComboBox<>();
    private final TextField txtPlayerAlias = new TextField(I18nManager.getInstance().get("sim.server.alias.default", "Player_1"));
    private final ComboBox<String> comboPlayerSpecies = new ComboBox<>();
    private final VBox pnlModeExplainer = new VBox(4);

    private final ComboBox<String> comboServerCatalogScenarios = new ComboBox<>();
    private final Button btnLoadServerCatalogScenario = new Button();
    private final Button btnStartServerMatch = new Button();

    private final Label lblLobbyStatus = new Label();
    private final Label lblLobbySlots = new Label();
    private final Button btnPlayerReady = new Button();
    private boolean isLocalPlayerReady = false;
    private String currentLobbyStatus = "LOBBY_WAITING";

    private final HBox hostMatchRow = new HBox(8);
    private final HBox playerProfileRow = new HBox(6);
    private final HBox joinLobbyRow = new HBox(8);

    private final CheckBox chkMultiplayerOnly;
    private final Spinner<Integer> spinRequiredPlayers;
    private final Spinner<Integer> spinGridTilesX;
    private final Spinner<Integer> spinGridTilesY;
    private final Label lblMegaterrariumShapeBadge = new Label();
    private final Label lblMegaterrariumDesc = new Label();

    private boolean isServerConnected = false;
    private Runnable onServerConnectAction;
    private Runnable onServerDisconnectAction;
    private Runnable onServerDiscoverAction;
    private Runnable onServerStartAction;
    private Runnable onValidationUpdate;

    public MultiplayerServerCard(SpeciesPresetManager speciesPresetManager,
                                 CheckBox chkMultiplayerOnly,
                                 Spinner<Integer> spinRequiredPlayers,
                                 Spinner<Integer> spinGridTilesX,
                                 Spinner<Integer> spinGridTilesY,
                                 Runnable onValidationUpdate) {
        this.chkMultiplayerOnly = chkMultiplayerOnly != null ? chkMultiplayerOnly : new CheckBox();
        this.spinRequiredPlayers = spinRequiredPlayers != null ? spinRequiredPlayers : new Spinner<>(1, 16, 1);
        this.spinGridTilesX = spinGridTilesX != null ? spinGridTilesX : new Spinner<>(1, 8, 1);
        this.spinGridTilesY = spinGridTilesY != null ? spinGridTilesY : new Spinner<>(1, 8, 1);
        this.onValidationUpdate = onValidationUpdate;
        buildServerNetworkBox(speciesPresetManager);
    }

    private void buildServerNetworkBox(SpeciesPresetManager speciesPresetManager) {
        serverNetworkBox.setStyle("-fx-background-color: rgba(2, 132, 199, 0.08); -fx-border-color: rgba(2, 132, 199, 0.3); -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8;");
        txtServerHost.setPrefWidth(110);
        txtServerHost.setTooltip(new Tooltip(i18n.get("sim.server.host.tt")));
        txtServerPort.setPrefWidth(65);
        txtServerPort.setTooltip(new Tooltip(i18n.get("sim.server.port.tt")));
        btnServerConnect.getStyleClass().add("btn-primary");
        btnServerConnect.tooltipProperty().bind(i18n.createTooltipBinding("sim.server.btn.connect.tt"));
        btnServerStart.setStyle("-fx-font-size: 11px;");
        btnServerStart.tooltipProperty().bind(i18n.createTooltipBinding("sim.server.btn.start.tt"));
        btnServerDiscover.setStyle("-fx-font-size: 11px;");
        btnServerDiscover.tooltipProperty().bind(i18n.createTooltipBinding("sim.server.btn.discover.tt"));
        btnServerConnect.setOnAction(e -> {
            if (btnServerConnect.getText().contains("Déconnecter") || btnServerConnect.getText().contains("Disconnect")) {
                if (onServerDisconnectAction != null) onServerDisconnectAction.run();
            } else {
                if (onServerConnectAction != null) onServerConnectAction.run();
            }
        });
        btnServerStart.setOnAction(e -> {
            if (onServerStartAction != null) onServerStartAction.run();
        });
        btnServerDiscover.setOnAction(e -> {
            if (onServerDiscoverAction != null) onServerDiscoverAction.run();
        });

        Label lblHost = new Label();
        lblHost.textProperty().bind(i18n.createStringBinding("sim.server.host"));
        Label lblPort = new Label();
        lblPort.textProperty().bind(i18n.createStringBinding("sim.server.port"));
        HBox serverInputsRow = new HBox(6, lblHost, txtServerHost, lblPort, txtServerPort, btnServerConnect, btnServerStart, btnServerDiscover);
        serverInputsRow.setAlignment(Pos.CENTER_LEFT);

        Label lblStatusHead = new Label();
        lblStatusHead.textProperty().bind(i18n.createStringBinding("sim.server.status"));
        lblStatusHead.tooltipProperty().bind(i18n.createTooltipBinding("sim.server.status.tt"));
        lblServerConnectionStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #f87171;");
        lblServerSessionStats.setStyle("-fx-font-size: 11px; -fx-text-fill: #38bdf8;");
        HBox serverStatusRow = new HBox(6, lblStatusHead, lblServerConnectionStatus, lblServerSessionStats);
        serverStatusRow.setAlignment(Pos.CENTER_LEFT);

        // Server Role (Join vs Host)
        comboServerRole.getItems().setAll(
            i18n.get("sim.server.role.join", "● Rejoindre (Matchmaking / Mégaterrarium - Autorité Serveur)"),
            i18n.get("sim.server.role.host", "● Héberger / Déployer Scénario (Mode Chercheur - Autorité Client)")
        );
        comboServerRole.getSelectionModel().selectFirst();
        comboServerRole.setStyle("-fx-font-size: 11px;");
        comboServerRole.setMaxWidth(Double.MAX_VALUE);
        comboServerRole.setTooltip(new Tooltip(i18n.get("sim.server.role.tt")));

        Label lblRole = new Label();
        lblRole.textProperty().bind(i18n.createStringBinding("sim.server.role"));
        HBox serverRoleRow = new HBox(6, lblRole, comboServerRole);
        serverRoleRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(comboServerRole, Priority.ALWAYS);

        // Player profile fields for Server Join Mode
        txtPlayerAlias.setPrefWidth(120);
        txtPlayerAlias.setTooltip(new Tooltip(i18n.get("sim.server.alias.tt")));

        List<String> dynamicSpecies = new ArrayList<>(speciesPresetManager != null ? speciesPresetManager.getPresetNames() : List.of());
        if (dynamicSpecies.isEmpty()) {
            dynamicSpecies.addAll(List.of("Lasius niger", "Atta cephalotes", "Formica rufa", "Solenopsis invicta", "Camponotus ligniperda", "Apis mellifera"));
        }
        comboPlayerSpecies.getItems().setAll(dynamicSpecies);
        comboPlayerSpecies.getSelectionModel().selectFirst();
        comboPlayerSpecies.setStyle("-fx-font-size: 11px;");
        comboPlayerSpecies.setTooltip(new Tooltip(i18n.get("sim.server.species.tt")));

        Label lblAlias = new Label();
        lblAlias.textProperty().bind(i18n.createStringBinding("sim.server.alias"));
        Label lblSpecies = new Label();
        lblSpecies.textProperty().bind(i18n.createStringBinding("sim.server.species"));
        playerProfileRow.getChildren().setAll(lblAlias, txtPlayerAlias, lblSpecies, comboPlayerSpecies);
        playerProfileRow.setAlignment(Pos.CENTER_LEFT);

        // Host Mode: Server Match Launch Button
        btnStartServerMatch.setText(i18n.get("sim.server.btn.start_match", "🚀 Lancer la Partie sur le Serveur"));
        btnStartServerMatch.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-color: #059669; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 4; -fx-cursor: hand;");
        btnStartServerMatch.tooltipProperty().bind(i18n.createTooltipBinding("sim.server.btn.start_match.tt", "Démarre la simulation active sur le serveur une fois que tous les participants ont rejoint le lobby."));
        btnStartServerMatch.setOnAction(e -> handleStartServerMatch());

        hostMatchRow.getChildren().setAll(btnStartServerMatch);
        hostMatchRow.setAlignment(Pos.CENTER_LEFT);

        // Join Mode: Matchmaking Lobby & Player Readiness Row
        lblLobbyStatus.setText("⏳ " + i18n.get("sim.lobby.waiting", "EN ATTENTE DE DÉMARRAGE (LOBBY)"));
        lblLobbyStatus.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #f59e0b; -fx-background-color: rgba(245, 158, 11, 0.15); -fx-padding: 3 8; -fx-background-radius: 4;");

        lblLobbySlots.setText("1/2 " + i18n.get("sim.lobby.slots_count", "Joueur(s) Connecté(s)"));
        lblLobbySlots.setStyle("-fx-font-size: 11px; -fx-text-fill: #38bdf8; -fx-font-weight: bold;");

        btnPlayerReady.setText(i18n.get("sim.lobby.btn_ready", "✓ Je suis Prêt"));
        btnPlayerReady.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-color: #d97706; -fx-text-fill: white; -fx-cursor: hand;");
        btnPlayerReady.tooltipProperty().bind(i18n.createTooltipBinding("sim.lobby.btn_ready.tt", "Indique à l'hôte du serveur que votre colonie et vos paramètres sont prêts."));
        btnPlayerReady.setOnAction(e -> {
            isLocalPlayerReady = !isLocalPlayerReady;
            if (isLocalPlayerReady) {
                btnPlayerReady.setText(i18n.get("sim.lobby.btn_unready", "✕ Pas Prêt"));
                btnPlayerReady.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-color: #059669; -fx-text-fill: white; -fx-cursor: hand;");
                NotificationOverlay.show(serverNetworkBox, "✓ Vous êtes prêt pour la partie !", NotificationOverlay.NotificationType.SUCCESS);
            } else {
                btnPlayerReady.setText(i18n.get("sim.lobby.btn_ready", "✓ Je suis Prêt"));
                btnPlayerReady.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-color: #d97706; -fx-text-fill: white; -fx-cursor: hand;");
                NotificationOverlay.show(serverNetworkBox, "Statut prêt désactivé.", NotificationOverlay.NotificationType.INFO);
            }
        });

        joinLobbyRow.getChildren().setAll(lblLobbyStatus, lblLobbySlots, btnPlayerReady);
        joinLobbyRow.setAlignment(Pos.CENTER_LEFT);

        pnlModeExplainer.setMaxWidth(Double.MAX_VALUE);

        serverNetworkBox.getChildren().addAll(serverInputsRow, serverStatusRow, serverRoleRow, hostMatchRow, playerProfileRow, joinLobbyRow, pnlModeExplainer);
        serverNetworkBox.setVisible(false);
        serverNetworkBox.setManaged(false);
    }

    public VBox createMegaterrariumCard() {
        VBox card = new VBox(8);
        card.getStyleClass().add("card-pane");

        Label lblTitle = new Label();
        lblTitle.textProperty().bind(i18n.createStringBinding("sim.megaterrarium.card.title", "🌐 Topologie Mégaterrarium & Multijoueur"));
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        lblTitle.getStyleClass().add("accent-title");
        lblTitle.tooltipProperty().bind(i18n.createTooltipBinding("sim.megaterrarium.card.tt", "Configuration de la topologie multi-nœuds (sharding spatial) et des paramètres multijoueur."));

        chkMultiplayerOnly.textProperty().bind(i18n.createStringBinding("sim.multiplayer.only_chk", "Scénario Exclusivement Multijoueur"));
        chkMultiplayerOnly.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
        chkMultiplayerOnly.tooltipProperty().bind(i18n.createTooltipBinding("sim.multiplayer.only.tt", "Si coché, ce scénario nécessite plusieurs participants et un serveur/cluster pour s'exécuter."));

        Label lblPlayers = new Label();
        lblPlayers.textProperty().bind(i18n.createStringBinding("sim.multiplayer.required_players", "Nombre de Joueurs Requis :"));
        lblPlayers.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
        lblPlayers.tooltipProperty().bind(i18n.createTooltipBinding("sim.multiplayer.required_players.tt", "Nombre minimum de participants (slots de colonies) pour lancer la partie (1 à 16 joueurs)."));

        spinRequiredPlayers.setPrefWidth(70);
        spinRequiredPlayers.setEditable(true);
        spinRequiredPlayers.setTooltip(new Tooltip(i18n.get("sim.multiplayer.required_players.tt", "Nombre de participants requis (1 à 16).")));

        HBox playersRow = new HBox(8, chkMultiplayerOnly, new Region(), lblPlayers, spinRequiredPlayers);
        playersRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(playersRow.getChildren().get(1), Priority.ALWAYS);

        Label lblGridX = new Label();
        lblGridX.textProperty().bind(i18n.createStringBinding("sim.megaterrarium.tiles_x", "Tuiles X (Colonnes) :"));
        lblGridX.setStyle("-fx-font-size: 11px;");
        lblGridX.tooltipProperty().bind(i18n.createTooltipBinding("sim.megaterrarium.tiles_x.tt", "Nombre de sous-volumes shardés sur l'axe X (1 à 8)."));

        spinGridTilesX.setPrefWidth(65);
        spinGridTilesX.setEditable(true);
        spinGridTilesX.setTooltip(new Tooltip(i18n.get("sim.megaterrarium.tiles_x.tt", "Nombre de tuiles spatiales sur l'axe X (1 à 8).")));

        Label lblGridY = new Label();
        lblGridY.textProperty().bind(i18n.createStringBinding("sim.megaterrarium.tiles_y", "Tuiles Y (Lignes) :"));
        lblGridY.setStyle("-fx-font-size: 11px;");
        lblGridY.tooltipProperty().bind(i18n.createTooltipBinding("sim.megaterrarium.tiles_y.tt", "Nombre de sous-volumes shardés sur l'axe Y (1 à 8)."));

        spinGridTilesY.setPrefWidth(65);
        spinGridTilesY.setEditable(true);
        spinGridTilesY.setTooltip(new Tooltip(i18n.get("sim.megaterrarium.tiles_y.tt", "Nombre de tuiles spatiales sur l'axe Y (1 à 8).")));

        HBox gridSpinnersRow = new HBox(8, lblGridX, spinGridTilesX, lblGridY, spinGridTilesY);
        gridSpinnersRow.setAlignment(Pos.CENTER_LEFT);

        lblMegaterrariumShapeBadge.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-background-color: rgba(59, 130, 246, 0.15); -fx-text-fill: #38bdf8; -fx-padding: 3 8; -fx-background-radius: 4;");
        lblMegaterrariumDesc.setStyle("-fx-font-size: 9.5px; -fx-text-fill: #94a3b8;");
        lblMegaterrariumDesc.setWrapText(true);

        Runnable updateTopologyVisuals = () -> {
            int tx = spinGridTilesX.getValue() != null ? spinGridTilesX.getValue() : 1;
            int ty = spinGridTilesY.getValue() != null ? spinGridTilesY.getValue() : 1;
            int players = spinRequiredPlayers.getValue() != null ? spinRequiredPlayers.getValue() : 1;
            int totalTiles = tx * ty;

            if (totalTiles <= 1) {
                lblMegaterrariumShapeBadge.setText(String.format("🗺️ %s (1 Tile) | %d %s",
                    i18n.get("sim.megaterrarium.single_tile", "Monde Monolithique"),
                    players,
                    i18n.get("sim.megaterrarium.players_count", "Joueur(s)")));
                lblMegaterrariumShapeBadge.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-background-color: rgba(59, 130, 246, 0.15); -fx-text-fill: #38bdf8; -fx-padding: 3 8; -fx-background-radius: 4;");
                lblMegaterrariumDesc.setText(i18n.get("sim.megaterrarium.single_tile_desc", "Simulation standard non shardée. Volume unique calculé sur un seul worker ou thread."));
            } else {
                lblMegaterrariumShapeBadge.setText(String.format("🌐 %s : %d × %d = %d %s | %d %s",
                    i18n.get("sim.megaterrarium.sharded_title", "Mégaterrarium Shardé"),
                    tx, ty, totalTiles,
                    i18n.get("sim.megaterrarium.subvolumes", "Sous-Volumes Shardés"),
                    players,
                    i18n.get("sim.megaterrarium.players_count", "Joueur(s)")));
                lblMegaterrariumShapeBadge.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-background-color: rgba(168, 85, 247, 0.15); -fx-text-fill: #c084fc; -fx-padding: 3 8; -fx-background-radius: 4;");
                lblMegaterrariumDesc.setText(i18n.get("sim.megaterrarium.sharded_desc", "Topologie distribuée multi-nœuds : halo d'échange de phéromones (3 cellules) et migration continue des entités entre sous-volumes shardés."));
            }
            if (onValidationUpdate != null) onValidationUpdate.run();
        };

        spinGridTilesX.valueProperty().addListener((o, oldV, newV) -> updateTopologyVisuals.run());
        spinGridTilesY.valueProperty().addListener((o, oldV, newV) -> updateTopologyVisuals.run());
        spinRequiredPlayers.valueProperty().addListener((o, oldV, newV) -> updateTopologyVisuals.run());
        chkMultiplayerOnly.selectedProperty().addListener((o, oldV, newV) -> updateTopologyVisuals.run());
        updateTopologyVisuals.run();

        card.getChildren().addAll(lblTitle, playersRow, gridSpinnersRow, lblMegaterrariumShapeBadge, lblMegaterrariumDesc);
        return card;
    }

    public void refreshModeExplainerCard(boolean isServer, boolean isJoin) {
        pnlModeExplainer.getChildren().clear();
        pnlModeExplainer.setVisible(isServer);
        pnlModeExplainer.setManaged(isServer);
        if (!isServer) return;

        String bgColor, borderColor, textColor, headerKey, bodyKey;
        if (isJoin) {
            bgColor     = "rgba(251, 191, 36, 0.10)";
            borderColor = "rgba(251, 191, 36, 0.40)";
            textColor   = "#fbbf24";
            headerKey   = "sim.mode_card.join.title";
            bodyKey     = "sim.mode_card.join.body";
        } else {
            bgColor     = "rgba(52, 211, 153, 0.10)";
            borderColor = "rgba(52, 211, 153, 0.40)";
            textColor   = "#34d399";
            headerKey   = "sim.mode_card.host.title";
            bodyKey     = "sim.mode_card.host.body";
        }

        pnlModeExplainer.setStyle(
            "-fx-background-color: " + bgColor + "; " +
            "-fx-border-color: " + borderColor + "; " +
            "-fx-border-radius: 6; -fx-background-radius: 6; " +
            "-fx-padding: 8 10;"
        );

        Label lblHeader = new Label(i18n.get(headerKey, isJoin ? "▶ Join Mode — Matchmaking / Megaterrarium" : "▶ Host Mode — Deploy Custom Scenario"));
        lblHeader.setStyle("-fx-font-size: 10.5px; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");
        lblHeader.setWrapText(true);

        Label lblBody = new Label(i18n.get(bodyKey, ""));
        lblBody.setStyle("-fx-font-size: 10px; -fx-text-fill: #cbd5e1; -fx-line-spacing: 2;");
        lblBody.setWrapText(true);

        Tooltip cardTooltip = new Tooltip(i18n.get(bodyKey + ".tt", i18n.get(bodyKey, "")));
        cardTooltip.setWrapText(true);
        cardTooltip.setMaxWidth(420);
        Tooltip.install(pnlModeExplainer, cardTooltip);

        pnlModeExplainer.getChildren().addAll(lblHeader, lblBody);
    }

    public void updateRoleVisibility(boolean isServer, boolean isJoin) {
        serverNetworkBox.setVisible(isServer);
        serverNetworkBox.setManaged(isServer);
        hostMatchRow.setVisible(isServer && !isJoin);
        hostMatchRow.setManaged(isServer && !isJoin);
        playerProfileRow.setVisible(isServer && isJoin);
        playerProfileRow.setManaged(isServer && isJoin);
        joinLobbyRow.setVisible(isServer && isJoin);
        joinLobbyRow.setManaged(isServer && isJoin);
        refreshModeExplainerCard(isServer, isJoin);
    }

    public Runnable getOnServerConnectAction() {
        return onServerConnectAction;
    }

    private void handleStartServerMatch() {
        if (onServerStartAction != null) {
            onServerStartAction.run();
        }
        currentLobbyStatus = "ACTIVE";
        lblLobbyStatus.setText("🟢 " + i18n.get("sim.lobby.active", "PARTIE EN COURS (ACTIVE)"));
        lblLobbyStatus.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #10b981; -fx-background-color: rgba(16, 185, 129, 0.15); -fx-padding: 3 8; -fx-background-radius: 4;");
        btnStartServerMatch.setDisable(true);
        NotificationOverlay.show(serverNetworkBox, "🚀 Lancement de la partie sur le serveur...", NotificationOverlay.NotificationType.SUCCESS);
    }

    public void setServerConnectionStatus(boolean connected, String message) {
        this.isServerConnected = connected;
        javafx.application.Platform.runLater(() -> {
            if (connected) {
                lblServerConnectionStatus.setText("● " + i18n.get("sim.server.connected", "Connecté") + (message != null && !message.isEmpty() ? " (" + message + ")" : ""));
                lblServerConnectionStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #4ade80; -fx-font-weight: bold;");
                btnServerConnect.setText(i18n.get("sim.server.btn.disconnect", "✕ Déconnecter"));
                lblServerSessionStats.setText(" | " + i18n.get("sim.server.online", "En Ligne (~15ms)"));
            } else {
                lblServerConnectionStatus.setText("○ " + (message != null && !message.isEmpty() ? message : i18n.get("sim.server.offline", "Hors-ligne")));
                lblServerConnectionStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #f87171;");
                btnServerConnect.setText(i18n.get("sim.server.btn.connect", "🌐 Connecter"));
                lblServerSessionStats.setText(" | " + i18n.get("sim.server.offline", "Hors-Ligne"));
            }
            if (onValidationUpdate != null) onValidationUpdate.run();
        });
    }

    // Getters and Setters
    public VBox getServerNetworkBox() { return serverNetworkBox; }
    public ComboBox<String> getComboExecutionMode() { return comboExecutionMode; }
    public ComboBox<String> getComboServerRole() { return comboServerRole; }
    public TextField getTxtPlayerAlias() { return txtPlayerAlias; }
    public ComboBox<String> getComboPlayerSpecies() { return comboPlayerSpecies; }
    public ComboBox<String> getComboServerCatalogScenarios() { return comboServerCatalogScenarios; }
    public Button getBtnLoadServerCatalogScenario() { return btnLoadServerCatalogScenario; }
    public Button getBtnStartServerMatch() { return btnStartServerMatch; }
    public CheckBox getChkMultiplayerOnly() { return chkMultiplayerOnly; }
    public Spinner<Integer> getSpinRequiredPlayers() { return spinRequiredPlayers; }
    public Spinner<Integer> getSpinGridTilesX() { return spinGridTilesX; }
    public Spinner<Integer> getSpinGridTilesY() { return spinGridTilesY; }

    public boolean isServerConnected() { return isServerConnected; }
    public String getServerHost() { return txtServerHost.getText().trim(); }
    public void setServerHost(String host) { txtServerHost.setText(host != null ? host : "localhost"); }
    public int getServerPort() {
        try { return Integer.parseInt(txtServerPort.getText().trim()); } catch (Exception e) { return 50051; }
    }
    public void setServerPort(int port) { txtServerPort.setText(String.valueOf(port)); }
    public String getPlayerAlias() {
        String a = txtPlayerAlias.getText();
        return (a != null && !a.trim().isEmpty()) ? a.trim() : i18n.get("sim.server.alias.default", "Player_1");
    }
    public String getPlayerSpecies() {
        String s = comboPlayerSpecies.getValue();
        return s != null ? s : "Lasius niger";
    }
    public void setServerSessionStats(String stats) {
        javafx.application.Platform.runLater(() -> lblServerSessionStats.setText(stats != null && !stats.isEmpty() ? " | " + stats : ""));
    }
    public void setOnServerConnect(Runnable r) { this.onServerConnectAction = r; }
    public void setOnServerDisconnect(Runnable r) { this.onServerDisconnectAction = r; }
    public void setOnServerDiscover(Runnable r) { this.onServerDiscoverAction = r; }
    public void setOnServerStart(Runnable r) { this.onServerStartAction = r; }
}
