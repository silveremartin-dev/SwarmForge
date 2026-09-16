/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Live multiplayer scoreboard and colony inspection HUD overlay for SwarmForge.
 * Shows connected players, colony species, live populations, food stocks, queen status,
 * and allows centering the 3D camera on any colony's nest location.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class MultiplayerScoreboardOverlay extends VBox {

    public static class ColonyEntry {
        public final String id;
        public final String playerName;
        public final String speciesName;
        public final String teamColorHex;
        public final int population;
        public final int workers;
        public final int soldiers;
        public final int queens;
        public final float foodStored;
        public final boolean isQueenAlive;
        public final float nestX;
        public final float nestY;
        public final float nestZ;
        public final boolean isLocalPlayer;

        public ColonyEntry(String id, String playerName, String speciesName, String teamColorHex,
                           int population, int workers, int soldiers, int queens,
                           float foodStored, boolean isQueenAlive,
                           float nestX, float nestY, float nestZ, boolean isLocalPlayer) {
            this.id = id;
            this.playerName = playerName;
            this.speciesName = speciesName;
            this.teamColorHex = teamColorHex;
            this.population = population;
            this.workers = workers;
            this.soldiers = soldiers;
            this.queens = queens;
            this.foodStored = foodStored;
            this.isQueenAlive = isQueenAlive;
            this.nestX = nestX;
            this.nestY = nestY;
            this.nestZ = nestZ;
            this.isLocalPlayer = isLocalPlayer;
        }
    }

    private final VBox coloniesContainer = new VBox(6);
    private final Label lblSessionTitle = new Label("👥 Session & Colonies en Direct");
    private final Label lblSessionSubtitle = new Label("Matchmaking / Monde Persistant");
    private final Button btnToggleCollapse = new Button();
    private final Button btnClose = new Button();
    private boolean isCollapsed = false;
    private Consumer<ColonyEntry> onFocusColonyListener;

    public MultiplayerScoreboardOverlay() {
        setSpacing(8);
        setPadding(new Insets(10));
        setMaxWidth(340);
        setMinWidth(280);
        setStyle("-fx-background-color: rgba(15, 23, 42, 0.88); " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: rgba(56, 189, 248, 0.4); " +
                "-fx-border-radius: 10; " +
                "-fx-border-width: 1.2; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 12, 0, 0, 4);");

        // Header
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        lblSessionTitle.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 12px;");
        lblSessionSubtitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px;");
        titleBox.getChildren().addAll(lblSessionTitle, lblSessionSubtitle);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        btnToggleCollapse.setGraphic(new FontIcon(Feather.MINUS));
        btnToggleCollapse.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-cursor: hand; -fx-padding: 4;");
        btnToggleCollapse.setOnAction(e -> toggleCollapse());

        btnClose.setGraphic(new FontIcon(Feather.X));
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: #f87171; -fx-cursor: hand; -fx-padding: 4;");
        btnClose.setOnAction(e -> hideWithAnimation());

        header.getChildren().addAll(new FontIcon(Feather.USERS), titleBox, btnToggleCollapse, btnClose);
        getChildren().addAll(header, new Separator(), coloniesContainer);
    }

    public void setOnFocusColony(Consumer<ColonyEntry> listener) {
        this.onFocusColonyListener = listener;
    }

    public void setSessionInfo(String title, String subtitle) {
        if (title != null) lblSessionTitle.setText(title);
        if (subtitle != null) lblSessionSubtitle.setText(subtitle);
    }

    public void updateColonies(List<ColonyEntry> entries) {
        coloniesContainer.getChildren().clear();
        if (entries == null || entries.isEmpty()) {
            Label lblEmpty = new Label("Aucune colonie active détectée.");
            lblEmpty.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic; -fx-font-size: 11px;");
            coloniesContainer.getChildren().add(lblEmpty);
            return;
        }

        for (ColonyEntry entry : entries) {
            coloniesContainer.getChildren().add(createColonyCard(entry));
        }
    }

    private Node createColonyCard(ColonyEntry entry) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(6, 8, 6, 8));
        card.setStyle("-fx-background-color: rgba(30, 41, 59, 0.7); -fx-background-radius: 6; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 6;");

        HBox topRow = new HBox(6);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Circle teamCircle = new Circle(5);
        try {
            teamCircle.setFill(Color.web(entry.teamColorHex != null ? entry.teamColorHex : "#38bdf8"));
        } catch (Exception e) {
            teamCircle.setFill(Color.web("#38bdf8"));
        }

        Label lblName = new Label(entry.playerName != null ? entry.playerName : "Colonie " + entry.id);
        lblName.setStyle("-fx-text-fill: " + (entry.isLocalPlayer ? "#38bdf8" : "#f1f5f9") + "; -fx-font-weight: bold; -fx-font-size: 11px;");
        if (entry.isLocalPlayer) {
            Label lblYou = new Label("(Vous)");
            lblYou.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 9px; -fx-font-weight: bold;");
            topRow.getChildren().addAll(teamCircle, lblName, lblYou);
        } else {
            topRow.getChildren().addAll(teamCircle, lblName);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnFocus = new Button("", new FontIcon(Feather.CROSSHAIR));
        btnFocus.setTooltip(new Tooltip("Centrer la caméra 3D sur ce nid"));
        btnFocus.setStyle("-fx-background-color: rgba(56, 189, 248, 0.15); -fx-text-fill: #38bdf8; -fx-cursor: hand; -fx-padding: 3 6; -fx-background-radius: 4;");
        btnFocus.setOnAction(e -> {
            if (onFocusColonyListener != null) {
                onFocusColonyListener.accept(entry);
            }
        });

        topRow.getChildren().addAll(spacer, btnFocus);

        // Species & Stats Row
        Label lblSpecies = new Label(entry.speciesName != null ? entry.speciesName : "Espèce Inconnue");
        lblSpecies.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px;");

        HBox statsRow = new HBox(10);
        statsRow.setAlignment(Pos.CENTER_LEFT);

        Label lblPop = new Label(String.format("🐜 %d ind. (O:%d / S:%d)", entry.population, entry.workers, entry.soldiers));
        lblPop.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 10px;");

        Label lblFood = new Label(String.format("🍯 %.0f mg", entry.foodStored));
        lblFood.setStyle("-fx-text-fill: #fbbf24; -fx-font-size: 10px;");

        Label lblQueen = new Label(entry.isQueenAlive ? "👑 Reine" : "💀 Orpheline");
        lblQueen.setStyle("-fx-text-fill: " + (entry.isQueenAlive ? "#4ade80" : "#ef4444") + "; -fx-font-size: 10px;");

        statsRow.getChildren().addAll(lblPop, lblFood, lblQueen);
        card.getChildren().addAll(topRow, lblSpecies, statsRow);
        return card;
    }

    public void toggleCollapse() {
        isCollapsed = !isCollapsed;
        coloniesContainer.setVisible(!isCollapsed);
        coloniesContainer.setManaged(!isCollapsed);
        btnToggleCollapse.setGraphic(new FontIcon(isCollapsed ? Feather.PLUS : Feather.MINUS));
    }

    public void hideWithAnimation() {
        FadeTransition ft = new FadeTransition(Duration.millis(200), this);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            setVisible(false);
            setManaged(false);
            setOpacity(1.0);
        });
        ft.play();
    }

    public void showWithAnimation() {
        setVisible(true);
        setManaged(true);
        setOpacity(0.0);
        FadeTransition ft = new FadeTransition(Duration.millis(200), this);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }
}
