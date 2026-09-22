/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.ThemeManager;
import org.swarmforge.client.util.IconUtils;
import org.swarmforge.server.ServerConfig;

/**
 * Interactive dialog for configuring and launching a local or LAN SwarmForge server instance.
 * Fully themed, internationalized, and equipped with detailed parameter impact guides.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ServerLauncherDialog extends Dialog<ServerConfig> {

    private final ComboBox<String> comboInterface = new ComboBox<>();
    private final TextField txtGrpcPort = new TextField("50051");
    private final TextField txtRestPort = new TextField("51051");
    private final ComboBox<String> comboStorage = new ComboBox<>();

    // Postgres / Redis fields
    private final VBox postgresConfigBox = new VBox(6);
    private final TextField txtDbHost = new TextField("localhost");
    private final TextField txtDbPort = new TextField("5432");
    private final TextField txtDbName = new TextField("swarmforge");
    private final TextField txtDbUser = new TextField("swarmforge");
    private final PasswordField txtDbPass = new PasswordField();
    private final TextField txtRedisHost = new TextField("localhost");
    private final TextField txtRedisPort = new TextField("6379");

    public ServerLauncherDialog(String defaultHost, int defaultPort) {
        I18nManager i18n = I18nManager.getInstance();

        setTitle(i18n.get("sim.server.dialog.title", "⚙️ Configuration & Démarrage du Serveur SwarmForge"));
        setHeaderText(i18n.get("sim.server.dialog.header", "Assistant de Démarrage du Serveur Local SwarmForge"));

        DialogPane pane = getDialogPane();
        pane.getStyleClass().add("card-pane");

        // Apply application theme and stage icon
        ThemeManager.getInstance().applyTheme(this);
        IconUtils.applyWindowIcons(this);

        VBox root = new VBox(12);
        root.setPadding(new Insets(10));
        root.setMinWidth(480);
        root.setMaxWidth(560);

        // 1. Network Section
        comboInterface.getItems().setAll(
                i18n.get("sim.server.dialog.net.iface.local", "localhost (Machine locale uniquement - Sécurisé)"),
                i18n.get("sim.server.dialog.net.iface.all", "0.0.0.0 (Toutes interfaces réseau - Accessible LAN / Multijoueur)")
        );
        comboInterface.getSelectionModel().selectFirst();
        comboInterface.setMaxWidth(Double.MAX_VALUE);
        comboInterface.setTooltip(new Tooltip(i18n.get("sim.server.dialog.net.iface.tt")));

        if (defaultPort > 0) {
            txtGrpcPort.setText(String.valueOf(defaultPort));
            txtRestPort.setText(String.valueOf(defaultPort + 1000));
        }

        txtGrpcPort.setTooltip(new Tooltip(i18n.get("sim.server.dialog.net.grpc.tt")));
        txtRestPort.setTooltip(new Tooltip(i18n.get("sim.server.dialog.net.rest.tt")));

        Label lblNetTitle = new Label(i18n.get("sim.server.dialog.net.title", "🌐 Paramètres Réseau & Ports :"));
        lblNetTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        GridPane gridNet = new GridPane();
        gridNet.setHgap(8);
        gridNet.setVgap(8);
        gridNet.add(new Label(i18n.get("sim.server.dialog.net.iface", "Interface d'écoute :")), 0, 0);
        gridNet.add(comboInterface, 1, 0, 3, 1);
        gridNet.add(new Label(i18n.get("sim.server.dialog.net.grpc", "Port gRPC :")), 0, 1);
        gridNet.add(txtGrpcPort, 1, 1);
        gridNet.add(new Label(i18n.get("sim.server.dialog.net.rest", "Port REST API :")), 2, 1);
        gridNet.add(txtRestPort, 3, 1);

        // 2. Storage Mode Section
        comboStorage.getItems().setAll(
                i18n.get("sim.server.dialog.storage.h2", "● H2 / SQLite Autonome (Zéro dépendance, Prêt à l'emploi)"),
                i18n.get("sim.server.dialog.storage.postgres", "● PostgreSQL + Redis (Cluster de Production / Persistance Avancée)")
        );
        comboStorage.getSelectionModel().selectFirst();
        comboStorage.setMaxWidth(Double.MAX_VALUE);
        comboStorage.setTooltip(new Tooltip(i18n.get("sim.server.dialog.storage.tt")));

        Label lblStorageTitle = new Label(i18n.get("sim.server.dialog.storage.title", "💾 Mode de Persistance :"));
        lblStorageTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        // Postgres sub-fields
        postgresConfigBox.setStyle("-fx-background-color: rgba(15, 23, 42, 0.6); -fx-padding: 8; -fx-background-radius: 6;");
        GridPane gridPg = new GridPane();
        gridPg.setHgap(6);
        gridPg.setVgap(6);
        gridPg.add(new Label("Hôte DB :"), 0, 0);
        gridPg.add(txtDbHost, 1, 0);
        gridPg.add(new Label("Port DB :"), 2, 0);
        gridPg.add(txtDbPort, 3, 0);
        gridPg.add(new Label("Base :"), 0, 1);
        gridPg.add(txtDbName, 1, 1);
        gridPg.add(new Label("Utilisateur :"), 2, 1);
        gridPg.add(txtDbUser, 3, 1);
        gridPg.add(new Label("Mot de passe :"), 0, 2);
        gridPg.add(txtDbPass, 1, 2);
        gridPg.add(new Label("Hôte Redis :"), 2, 2);
        gridPg.add(txtRedisHost, 3, 2);
        postgresConfigBox.getChildren().addAll(new Label("Paramètres de connexion Base de Données :"), gridPg);
        postgresConfigBox.setVisible(false);
        postgresConfigBox.setManaged(false);

        comboStorage.valueProperty().addListener((obs, oldV, newV) -> {
            boolean isPg = newV != null && newV.contains("PostgreSQL");
            postgresConfigBox.setVisible(isPg);
            postgresConfigBox.setManaged(isPg);
        });

        // 3. Neutral Compute Daemon & Scenario Architecture Notice
        VBox scenarioInfoCard = new VBox(4);
        scenarioInfoCard.setStyle("-fx-background-color: rgba(16, 185, 129, 0.08); -fx-border-color: rgba(16, 185, 129, 0.3); -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8;");
        Label lblInfoTitle = new Label(i18n.get("sim.server.dialog.neutral_server.title", "🗺️ Moteur Distribué & Déploiement de Scénario :"));
        lblInfoTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #10b981;");
        Label lblInfoText = new Label(i18n.get("sim.server.dialog.neutral_server.desc",
                "Le serveur SwarmForge fonctionne comme un démon de calcul haute performance neutre. Les dimensions spatiales, le biotope, le climat et la topologie mégaterrarium (sharding) sont automatiquement transmis lors du déploiement du scénario par l'Hôte."));
        lblInfoText.setStyle("-fx-font-size: 10px; -fx-text-fill: #cbd5e1;");
        lblInfoText.setWrapText(true);
        scenarioInfoCard.getChildren().addAll(lblInfoTitle, lblInfoText);

        // 4. Parameter Impact Explanation Card
        VBox impactCard = new VBox(4);
        impactCard.setStyle("-fx-background-color: rgba(56, 189, 248, 0.08); -fx-border-color: rgba(56, 189, 248, 0.3); -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8;");
        Label lblImpactHeader = new Label(i18n.get("sim.server.dialog.impact.title", "ℹ️ Impact des Paramètres :"));
        lblImpactHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #38bdf8;");
        Label lblImpactText = new Label(i18n.get("sim.server.dialog.impact.desc",
                "• L'interface '0.0.0.0' ouvre le serveur à vos collègues/amis sur le réseau local LAN.\n• Le mode H2 ne nécessite aucun service externe installé.\n• Le mode PostgreSQL + Redis est recommandé pour les clusters de production multi-nœuds."));
        lblImpactText.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");
        lblImpactText.setWrapText(true);
        impactCard.getChildren().addAll(lblImpactHeader, lblImpactText);

        root.getChildren().addAll(
                lblNetTitle, gridNet,
                new Separator(),
                lblStorageTitle, comboStorage, postgresConfigBox,
                new Separator(),
                scenarioInfoCard,
                new Separator(),
                impactCard
        );

        pane.setContent(root);

        ButtonType btnStart = new ButtonType(
                i18n.get("sim.server.dialog.btn.launch", "🚀 Démarrer le Serveur & Déployer"),
                ButtonBar.ButtonData.OK_DONE
        );
        ButtonType btnCancel = new ButtonType(
                i18n.get("common.cancel", "Annuler"),
                ButtonBar.ButtonData.CANCEL_CLOSE
        );
        pane.getButtonTypes().addAll(btnStart, btnCancel);

        setResultConverter(dialogButton -> {
            if (dialogButton == btnStart) {
                int grpcPort = 50051;
                try {
                    grpcPort = Integer.parseInt(txtGrpcPort.getText().trim());
                } catch (Exception ignored) {}

                int w = 256;
                int h = 256;
                int d = 128;
                int g = 64;

                boolean isPg = comboStorage.getValue() != null && comboStorage.getValue().contains("PostgreSQL");
                if (isPg) {
                    int dbP = 5432;
                    int rP = 6379;
                    try { dbP = Integer.parseInt(txtDbPort.getText().trim()); } catch (Exception ignored) {}
                    try { rP = Integer.parseInt(txtRedisPort.getText().trim()); } catch (Exception ignored) {}
                    return new ServerConfig(
                            grpcPort, w, h, d, g,
                            48.8566, 2.3522, System.currentTimeMillis(),
                            txtDbHost.getText().trim(), dbP, txtDbName.getText().trim(),
                            txtDbUser.getText().trim(), txtDbPass.getText().trim(),
                            txtRedisHost.getText().trim(), rP
                    );
                } else {
                    return new ServerConfig(
                            grpcPort, w, h, d, g,
                            48.8566, 2.3522, System.currentTimeMillis(),
                            "", 0, "", "", "",
                            "", 0
                    );
                }
            }
            return null;
        });
    }
}
