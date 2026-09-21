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
        public final String participantName;
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
        public final boolean isLocalParticipant;

        public ColonyEntry(String id, String participantName, String speciesName, String teamColorHex,
                           int population, int workers, int soldiers, int queens,
                           float foodStored, boolean isQueenAlive,
                           float nestX, float nestY, float nestZ, boolean isLocalParticipant) {
            this.id = id;
            this.participantName = participantName;
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
            this.isLocalParticipant = isLocalParticipant;
        }

        public String getPlayerName() {
            return participantName;
        }

        public boolean isLocalPlayer() {
            return isLocalParticipant;
        }
    }

    private final VBox coloniesContainer = new VBox(5);
    private final ScrollPane scrollPane = new ScrollPane();
    private final Label lblSessionTitle = new Label();
    private final Label lblSessionSubtitle = new Label();
    private final CheckBox chkActive = new CheckBox();
    private final Button btnClose = new Button();
    private Consumer<ColonyEntry> onFocusColonyListener;
    private Runnable onCloseListener;

    public MultiplayerScoreboardOverlay() {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();

        setSpacing(6);
        setPadding(new Insets(8));
        setMaxWidth(300);
        setMinWidth(260);
        setStyle("-fx-background-color: rgba(15, 23, 42, 0.92); " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: rgba(56, 189, 248, 0.4); " +
                "-fx-border-radius: 8; " +
                "-fx-border-width: 1.2; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 10, 0, 0, 3);");

        // Header
        HBox header = new HBox(6);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(1);
        lblSessionTitle.textProperty().bind(i18n.createStringBinding("multiplayer.scoreboard.title"));
        lblSessionTitle.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px;");

        lblSessionSubtitle.textProperty().bind(i18n.createStringBinding("multiplayer.scoreboard.subtitle"));
        lblSessionSubtitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 9.5px;");
        titleBox.getChildren().addAll(lblSessionTitle, lblSessionSubtitle);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        chkActive.setSelected(true);
        chkActive.setStyle("-fx-font-size: 10px; -fx-text-fill: #38bdf8;");
        chkActive.setTooltip(new Tooltip(i18n.get("multiplayer.scoreboard.active.tt", "Activer ou désactiver l'affichage de ce panneau")));
        chkActive.selectedProperty().addListener((o, oldV, newV) -> {
            scrollPane.setVisible(newV);
            scrollPane.setManaged(newV);
        });

        btnClose.setGraphic(new FontIcon(Feather.X));
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: #f87171; -fx-cursor: hand; -fx-padding: 2;");
        btnClose.setOnAction(e -> {
            hideWithAnimation();
            if (onCloseListener != null) {
                onCloseListener.run();
            }
        });

        header.getChildren().addAll(new FontIcon(Feather.USERS), titleBox, chkActive, btnClose);

        // Scrollable colonies list
        scrollPane.setContent(coloniesContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(230);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 0;");
        coloniesContainer.setStyle("-fx-background-color: transparent;");

        getChildren().addAll(header, new Separator(), scrollPane);

        // Hidden by default until explicit multiplayer mode
        setVisible(false);
        setManaged(false);
    }

    public void setOnClose(Runnable listener) {
        this.onCloseListener = listener;
    }

    public CheckBox getActiveCheckBox() {
        return chkActive;
    }

    public void setOnFocusColony(Consumer<ColonyEntry> listener) {
        this.onFocusColonyListener = listener;
    }

    public void setSessionInfo(String title, String subtitle) {
        if (title != null) {
            lblSessionTitle.textProperty().unbind();
            lblSessionTitle.setText(title);
        }
        if (subtitle != null) {
            lblSessionSubtitle.textProperty().unbind();
            lblSessionSubtitle.setText(subtitle);
        }
    }

    public void updateColonies(List<ColonyEntry> entries) {
        coloniesContainer.getChildren().clear();
        if (entries == null || entries.isEmpty()) {
            Label lblEmpty = new Label(org.swarmforge.client.util.I18nManager.getInstance().get("multiplayer.scoreboard.empty"));
            lblEmpty.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic; -fx-font-size: 10px; -fx-padding: 4;");
            coloniesContainer.getChildren().add(lblEmpty);
            return;
        }

        for (ColonyEntry entry : entries) {
            coloniesContainer.getChildren().add(createColonyCard(entry));
        }
    }

    private Node createColonyCard(ColonyEntry entry) {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        VBox card = new VBox(3);
        card.setPadding(new Insets(4, 6, 4, 6));
        card.setStyle("-fx-background-color: rgba(30, 41, 59, 0.75); -fx-background-radius: 5; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 5;");

        HBox topRow = new HBox(5);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Circle teamCircle = new Circle(4);
        try {
            teamCircle.setFill(Color.web(entry.teamColorHex != null ? entry.teamColorHex : "#38bdf8"));
        } catch (Exception e) {
            teamCircle.setFill(Color.web("#38bdf8"));
        }

        Label lblName = new Label(entry.participantName != null ? entry.participantName : "Colonie " + entry.id);
        lblName.setStyle("-fx-text-fill: " + (entry.isLocalParticipant ? "#38bdf8" : "#f1f5f9") + "; -fx-font-weight: bold; -fx-font-size: 10.5px;");
        if (entry.isLocalParticipant) {
            Label lblYou = new Label(i18n.get("multiplayer.scoreboard.you", "(Vous)"));
            lblYou.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 8.5px; -fx-font-weight: bold;");
            topRow.getChildren().addAll(teamCircle, lblName, lblYou);
        } else {
            topRow.getChildren().addAll(teamCircle, lblName);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnFocus = new Button("", new FontIcon(Feather.CROSSHAIR));
        btnFocus.setTooltip(new Tooltip(i18n.get("multiplayer.scoreboard.focus.tt", "Centrer la caméra 3D sur ce nid")));
        btnFocus.setStyle("-fx-background-color: rgba(56, 189, 248, 0.2); -fx-text-fill: #38bdf8; -fx-cursor: hand; -fx-padding: 2 5; -fx-background-radius: 3; -fx-font-size: 9px;");
        btnFocus.setOnAction(e -> {
            if (onFocusColonyListener != null) {
                onFocusColonyListener.accept(entry);
            }
        });

        topRow.getChildren().addAll(spacer, btnFocus);

        // Species & Stats Row
        Label lblSpecies = new Label(entry.speciesName != null ? entry.speciesName : "Espèce Inconnue");
        lblSpecies.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 9.5px;");

        HBox statsRow = new HBox(8);
        statsRow.setAlignment(Pos.CENTER_LEFT);

        String casteW = i18n.get("multiplayer.scoreboard.caste_w", "O");
        String casteS = i18n.get("multiplayer.scoreboard.caste_s", "S");
        String queenAliveText = i18n.get("multiplayer.scoreboard.queen_alive", "👑 Reine");
        String queenDeadText = i18n.get("multiplayer.scoreboard.queen_dead", "💀 Orpheline");

        Label lblPop = new Label(String.format("🐜 %d (%s:%d/%s:%d)", entry.population, casteW, entry.workers, casteS, entry.soldiers));
        lblPop.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 9.5px;");

        Label lblFood = new Label(String.format("🍯 %.0f mg", entry.foodStored));
        lblFood.setStyle("-fx-text-fill: #fbbf24; -fx-font-size: 9.5px;");

        Label lblQueen = new Label(entry.isQueenAlive ? queenAliveText : queenDeadText);
        lblQueen.setStyle("-fx-text-fill: " + (entry.isQueenAlive ? "#4ade80" : "#ef4444") + "; -fx-font-size: 9.5px;");

        statsRow.getChildren().addAll(lblPop, lblFood, lblQueen);
        card.getChildren().addAll(topRow, lblSpecies, statsRow);
        return card;
    }

    public void hideWithAnimation() {
        FadeTransition ft = new FadeTransition(Duration.millis(150), this);
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
        FadeTransition ft = new FadeTransition(Duration.millis(150), this);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }
}
