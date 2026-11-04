/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.swarmforge.client.util.I18nManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds interactive substrates, nest interior, pheromone fields, and caste legends
 * for the world editor and simulation overlay HUD.
 */
class WorldEditorLegendPanelBuilder {

    static VBox createStandaloneLegendPanel() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(6));
        panel.setStyle("-fx-background-color: transparent;");

        Label titleSub = new Label("🧱 Substrats Géologiques");
        titleSub.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");
        FlowPane subFlow = new FlowPane(4, 4);
        subFlow.setPrefWrapLength(240);
        String[][] subs = {
            {"organic", "#523219", "Organique"},
            {"earth", "#3d2817", "Terre / Humus"},
            {"sand", "#eab308", "Sable"},
            {"clay", "#9a3412", "Argile"},
            {"silt", "#ca8a04", "Limon"},
            {"peat", "#451a03", "Tourbe"},
            {"gravel", "#94a3b8", "Gravier"},
            {"stone", "#64748b", "Roche"},
            {"cavities", "#0f172a", "Cavités"},
            {"roots", "#78350f", "Racines"},
            {"river", "#0284c7", "Eau / Rivière"},
            {"vegetation", "#15803d", "Végétation"}
        };
        for (String[] s : subs) {
            HBox b = new HBox(4);
            b.setAlignment(Pos.CENTER_LEFT);
            b.setPadding(new Insets(2, 5, 2, 5));
            b.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-background-radius: 4; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 4; -fx-border-width: 0.5;");
            Canvas dot = new Canvas(8, 8);
            GraphicsContext g = dot.getGraphicsContext2D();
            g.setFill(Color.web(s[1]));
            g.fillOval(0, 0, 8, 8);
            g.setStroke(Color.WHITE);
            g.setLineWidth(0.6);
            g.strokeOval(0, 0, 8, 8);
            Label lbl = new Label(s[2]);
            lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #e2e8f0;");
            b.getChildren().addAll(dot, lbl);
            subFlow.getChildren().add(b);
        }

        Label titleNest = new Label("🏰 Galeries & Chambres");
        titleNest.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #10b981;");
        FlowPane nestFlow = new FlowPane(4, 4);
        nestFlow.setPrefWrapLength(240);
        String[][] chambers = {
            {"👑 Chambre Royale", "#d946ef"},
            {"🥚 Couvain", "#f8fafc"},
            {"🍄 Meule Champignon", "#a855f7"},
            {"🌿 Étable à Pucerons", "#ec4899"},
            {"🌾 Grenier à Graines", "#22c55e"},
            {"❄️ Salle Diapause", "#0284c7"},
            {"🗑️ Dépotoir", "#eab308"},
            {"🚪 Entrées / Puits", "#9a3412"}
        };
        for (String[] ch : chambers) {
            HBox b = new HBox(4);
            b.setAlignment(Pos.CENTER_LEFT);
            b.setPadding(new Insets(2, 5, 2, 5));
            b.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-background-radius: 4; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 4; -fx-border-width: 0.5;");
            Canvas dot = new Canvas(8, 8);
            GraphicsContext g = dot.getGraphicsContext2D();
            g.setFill(Color.web(ch[1]));
            g.fillOval(0, 0, 8, 8);
            g.setStroke(Color.WHITE);
            g.setLineWidth(0.6);
            g.strokeOval(0, 0, 8, 8);
            Label lbl = new Label(ch[0]);
            lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #e2e8f0;");
            b.getChildren().addAll(dot, lbl);
            nestFlow.getChildren().add(b);
        }

        panel.getChildren().addAll(
            titleSub, subFlow,
            new Separator(),
            titleNest, nestFlow
        );
        return panel;
    }

    static VBox buildLegendPanel(WorldEditorPane pane) {
        VBox panel = new VBox(6);
        panel.setPadding(new Insets(6));
        panel.getStyleClass().add("legend-card-pane");

        pane.legendContentBox = new VBox(6);

        Label titleSubstrates = new Label();
        titleSubstrates.textProperty().bind(I18nManager.getInstance().createStringBinding("legend.substrates.title"));
        titleSubstrates.getStyleClass().add("legend-title");

        FlowPane substrateItemsPane = new FlowPane(4, 4);
        substrateItemsPane.setPrefWrapLength(220);

        pane.lblHoverInfo = new Label();
        pane.lblHoverInfo.setText(I18nManager.getInstance().get("world.hover_info"));
        pane.lblHoverInfo.getStyleClass().add("legend-hover-info");
        pane.lblHoverInfo.setWrapText(true);
        pane.lblHoverInfo.setMaxWidth(230);

        List<String[]> substrateList = new ArrayList<>();
        substrateList.add(new String[]{"organic", "#523219"});
        substrateList.add(new String[]{"earth", "#3d2817"});
        substrateList.add(new String[]{"sand", "#eab308"});
        substrateList.add(new String[]{"clay", "#9a3412"});
        substrateList.add(new String[]{"silt", "#ca8a04"});
        substrateList.add(new String[]{"peat", "#451a03"});
        substrateList.add(new String[]{"gravel", "#94a3b8"});
        substrateList.add(new String[]{"stone", "#64748b"});
        substrateList.add(new String[]{"cavities", "#0f172a"});
        substrateList.add(new String[]{"roots", "#78350f"});
        substrateList.add(new String[]{"river", "#0284c7"});
        substrateList.add(new String[]{"vegetation", "#15803d"});

        for (String[] it : substrateList) {
            String key = it[0];
            HBox item = new HBox(4);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setPadding(new Insets(2, 4, 2, 4));
            item.getStyleClass().add("legend-item-box");

            Canvas dot = new Canvas(8, 8);
            GraphicsContext g = dot.getGraphicsContext2D();
            g.setFill(Color.web(it[1]));
            g.fillOval(0, 0, 8, 8);
            g.setStroke(Color.WHITE);
            g.setLineWidth(0.6);
            g.strokeOval(0, 0, 8, 8);

            Label lbl = new Label();
            lbl.textProperty().bind(I18nManager.getInstance().createStringBinding("legend.substrate." + key));
            lbl.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("legend.substrate." + key + ".desc"));
            item.getChildren().addAll(dot, lbl);
            substrateItemsPane.getChildren().add(item);
        }

        pane.legendContentBox.getChildren().addAll(
            titleSubstrates,
            substrateItemsPane
        );

        panel.getChildren().add(pane.legendContentBox);

        Label titleNestInterior = new Label();
        titleNestInterior.textProperty().bind(I18nManager.getInstance().createStringBinding("legend.nest.title"));
        titleNestInterior.getStyleClass().add("legend-title");

        FlowPane nestItemsFlow = new FlowPane(4, 4);
        nestItemsFlow.setPrefWrapLength(220);

        List<String[]> nestList = new ArrayList<>();
        nestList.add(new String[]{"royal", "#d946ef", "dot"});
        nestList.add(new String[]{"brood", "#f8fafc", "dot_stroke"});
        nestList.add(new String[]{"fungus", "#a855f7", "dot"});
        nestList.add(new String[]{"aphid", "#ec4899", "dot"});
        nestList.add(new String[]{"granary", "#22c55e", "dot"});
        nestList.add(new String[]{"diapause", "#0284c7", "dot"});
        nestList.add(new String[]{"waste", "#eab308", "dot"});
        nestList.add(new String[]{"shafts", "#f59e0b", "line"});
        nestList.add(new String[]{"subnivean", "#e0f2fe", "line"});
        nestList.add(new String[]{"entrances", "#9a3412", "dot"});

        for (String[] it : nestList) {
            String key = it[0];
            HBox item = new HBox(4);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setPadding(new Insets(2, 4, 2, 4));
            item.getStyleClass().add("legend-item-box");

            Node symbolNode;
            if ("line".equals(it[2])) {
                Canvas lineCanvas = new Canvas(10, 8);
                GraphicsContext g = lineCanvas.getGraphicsContext2D();
                g.setStroke(Color.web(it[1]));
                g.setLineWidth(2.0);
                g.strokeLine(0, 4, 10, 4);
                symbolNode = lineCanvas;
            } else {
                Canvas dot = new Canvas(8, 8);
                GraphicsContext g = dot.getGraphicsContext2D();
                g.setFill(Color.web(it[1]));
                g.fillOval(0, 0, 8, 8);
                if ("dot_stroke".equals(it[2])) {
                    g.setStroke(Color.web("#94a3b8"));
                    g.setLineWidth(0.8);
                    g.strokeOval(0, 0, 8, 8);
                } else {
                    g.setStroke(Color.WHITE);
                    g.setLineWidth(0.6);
                    g.strokeOval(0, 0, 8, 8);
                }
                symbolNode = dot;
            }

            Label lbl = new Label();
            lbl.setStyle("-fx-font-size: 10px;");
            lbl.textProperty().bind(I18nManager.getInstance().createStringBinding("legend.nest." + key));
            lbl.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("legend.nest." + key + ".desc"));
            item.getChildren().addAll(symbolNode, lbl);
            nestItemsFlow.getChildren().add(item);
        }

        pane.nestLegendBox = new VBox(4);
        pane.nestLegendBox.getChildren().addAll(new Separator(), titleNestInterior, nestItemsFlow);
        pane.nestLegendBox.setVisible(pane.isSimulationMode);
        pane.nestLegendBox.setManaged(pane.isSimulationMode);
        panel.getChildren().add(pane.nestLegendBox);

        Label titlePheromones = new Label();
        titlePheromones.textProperty().bind(I18nManager.getInstance().createStringBinding("legend.phero.title"));
        titlePheromones.getStyleClass().add("legend-title");

        FlowPane pheroItemsFlow = new FlowPane(4, 4);
        pheroItemsFlow.setPrefWrapLength(220);

        List<String[]> pheroList = new ArrayList<>();
        pheroList.add(new String[]{"home", "#06b6d4"});
        pheroList.add(new String[]{"food", "#f59e0b"});
        pheroList.add(new String[]{"danger", "#ef4444"});
        pheroList.add(new String[]{"queen", "#ec4899"});
        pheroList.add(new String[]{"necrophoric", "#78716c"});

        for (String[] it : pheroList) {
            String key = it[0];
            HBox item = new HBox(4);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setPadding(new Insets(2, 4, 2, 4));
            item.getStyleClass().add("legend-item-box");

            Canvas dot = new Canvas(8, 8);
            GraphicsContext g = dot.getGraphicsContext2D();
            g.setFill(Color.web(it[1]));
            g.fillOval(0, 0, 8, 8);
            g.setStroke(Color.WHITE);
            g.setLineWidth(0.6);
            g.strokeOval(0, 0, 8, 8);

            Label lbl = new Label();
            lbl.setStyle("-fx-font-size: 10px;");
            lbl.textProperty().bind(I18nManager.getInstance().createStringBinding("legend.phero." + key));
            lbl.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("legend.phero." + key + ".desc"));
            item.getChildren().addAll(dot, lbl);
            pheroItemsFlow.getChildren().add(item);
        }

        pane.pheroLegendBox = new VBox(4);
        pane.pheroLegendBox.getChildren().addAll(new Separator(), titlePheromones, pheroItemsFlow);
        pane.pheroLegendBox.setVisible(pane.isSimulationMode);
        pane.pheroLegendBox.setManaged(pane.isSimulationMode);
        panel.getChildren().add(pane.pheroLegendBox);

        Label titleCastes = new Label();
        titleCastes.textProperty().bind(I18nManager.getInstance().createStringBinding("legend.castes.title"));
        titleCastes.getStyleClass().add("legend-title");

        FlowPane casteItemsFlow = new FlowPane(4, 4);
        casteItemsFlow.setPrefWrapLength(220);

        List<String[]> casteList = new ArrayList<>();
        casteList.add(new String[]{"queen", "#e11d48"});
        casteList.add(new String[]{"worker", "#10b981"});
        casteList.add(new String[]{"soldier", "#ea580c"});
        casteList.add(new String[]{"media", "#0284c7"});
        casteList.add(new String[]{"minor", "#14b8a6"});
        casteList.add(new String[]{"drone", "#eab308"});
        casteList.add(new String[]{"brood", "#f8fafc"});

        for (String[] it : casteList) {
            String key = it[0];
            HBox item = new HBox(4);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setPadding(new Insets(2, 4, 2, 4));
            item.getStyleClass().add("legend-item-box");

            Canvas dot = new Canvas(8, 8);
            GraphicsContext g = dot.getGraphicsContext2D();
            g.setFill(Color.web(it[1]));
            g.fillOval(0, 0, 8, 8);
            g.setStroke(Color.WHITE);
            g.setLineWidth(0.6);
            g.strokeOval(0, 0, 8, 8);

            Label lbl = new Label();
            lbl.setStyle("-fx-font-size: 10px;");
            lbl.textProperty().bind(I18nManager.getInstance().createStringBinding("legend.caste." + key));
            lbl.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("legend.caste." + key + ".desc"));
            item.getChildren().addAll(dot, lbl);
            casteItemsFlow.getChildren().add(item);
        }

        pane.castesLegendBox = new VBox(4);
        pane.castesLegendBox.getChildren().addAll(new Separator(), titleCastes, casteItemsFlow);
        pane.castesLegendBox.setVisible(pane.isSimulationMode);
        pane.castesLegendBox.setManaged(pane.isSimulationMode);
        panel.getChildren().add(pane.castesLegendBox);

        return panel;
    }
}
