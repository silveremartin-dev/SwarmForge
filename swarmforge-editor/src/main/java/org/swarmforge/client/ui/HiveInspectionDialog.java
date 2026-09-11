/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.NotificationOverlay;
import org.swarmforge.client.util.ThemeManager;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;

import java.util.Random;

/**
 * Interactive Dadant Hive & Frame Inspection Studio (Inspection Apicole).
 * Displays full 10-frame hive cross-sections, honey crown, bee bread pollen,
 * capped worker/drone brood, queen spotting, and Varroa destructor sanitary status.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class HiveInspectionDialog extends Stage {

    private final Colony colony;
    private int selectedFrameIndex = 4; // Center brood frame by default (0-9)
    private final Canvas frameCanvas;
    private final Label lblFrameStats;
    private final Label lblQueenStatus;
    private final Label lblHoneyTotal;
    private final Label lblVarroaLevel;
    private final ProgressBar varroaBar;

    public HiveInspectionDialog(Stage owner, Colony colony) {
        this.colony = colony;
        initModality(Modality.WINDOW_MODAL);
        if (owner != null) initOwner(owner);

        I18nManager i18n = I18nManager.getInstance();
        setTitle(i18n.get("hive.inspect.title", "🐝 Inspection Apicole de la Ruche Dadant"));

        VBox root = new VBox(12);
        root.setPadding(new Insets(16));
        root.getStyleClass().add("card-pane");

        // 1. Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        FontIcon icon = new FontIcon(Feather.LAYERS);
        icon.setIconSize(24);
        icon.setIconColor(Color.web("#f59e0b"));

        VBox titleBox = new VBox(2);
        Label title = new Label(i18n.get("hive.inspect.header", "🐝 Tableau de Contrôle Apicole - Visite de Printemps"));
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        String colName = (colony != null && colony.getSpecies() != null) ? colony.getSpecies().getCommonName() : "Ruche Pastorale #1";
        Label sub = new Label("Colonie : " + colName + " | Modèle : Dadant 10 Cadres");
        sub.setStyle("-fx-font-size: 11px; -fx-text-fill: #38bdf8;");
        titleBox.getChildren().addAll(title, sub);
        header.getChildren().addAll(icon, titleBox);

        // 2. Global Colony Metrics Card
        HBox statsBar = new HBox(16);
        statsBar.setPadding(new Insets(10));
        statsBar.setAlignment(Pos.CENTER_LEFT);
        statsBar.getStyleClass().add("card-pane");

        lblQueenStatus = new Label("👑 Reine : Présente & Féconde");
        lblQueenStatus.setStyle("-fx-font-weight: bold; -fx-text-fill: #10b981;");

        lblHoneyTotal = new Label("🍯 Miel total : 14.8 kg");
        lblHoneyTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #f59e0b;");

        lblVarroaLevel = new Label("🛡️ Varroa : Faible (< 1.2%)");
        lblVarroaLevel.setStyle("-fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        varroaBar = new ProgressBar(0.12);
        varroaBar.setPrefWidth(80);

        statsBar.getChildren().addAll(lblQueenStatus, new Separator(Orientation.VERTICAL),
                lblHoneyTotal, new Separator(Orientation.VERTICAL),
                lblVarroaLevel, varroaBar);

        // 3. Frame Selector Bar (10 Frames)
        HBox frameButtonsBox = new HBox(6);
        frameButtonsBox.setAlignment(Pos.CENTER);
        ToggleGroup group = new ToggleGroup();

        for (int i = 0; i < 10; i++) {
            final int fIdx = i;
            ToggleButton tb = new ToggleButton("Cadre " + (i + 1));
            tb.setToggleGroup(group);
            tb.setPrefWidth(72);
            if (i == selectedFrameIndex) tb.setSelected(true);
            tb.setOnAction(e -> {
                selectedFrameIndex = fIdx;
                drawFrame();
                updateFrameStats();
            });
            frameButtonsBox.getChildren().add(tb);
        }

        // 4. Main Frame Drawing Canvas
        VBox canvasHolder = new VBox(8);
        canvasHolder.setAlignment(Pos.CENTER);
        canvasHolder.setPadding(new Insets(8));
        canvasHolder.getStyleClass().add("card-pane");

        frameCanvas = new Canvas(620, 290);
        drawFrame();

        lblFrameStats = new Label();
        lblFrameStats.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #94a3b8;");
        updateFrameStats();

        canvasHolder.getChildren().addAll(frameCanvas, lblFrameStats);

        // 5. Action Footer
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnHarvest = new Button("🍯 Récolter le Miel Mûr");
        btnHarvest.setOnAction(e -> {
            NotificationOverlay.show(root, "🍯 Récolte effectuée : 3.5 kg de miel doré extrait avec succès !", NotificationOverlay.NotificationType.SUCCESS);
            updateFrameStats();
        });

        Button btnTreat = new Button("🧪 Traitement Sanitaire Acide Oxalique");
        btnTreat.setOnAction(e -> {
            NotificationOverlay.show(root, "🧪 Traitement flash appliqué : Taux de varroa abaissé à 0.2%", NotificationOverlay.NotificationType.SUCCESS);
            varroaBar.setProgress(0.02);
            lblVarroaLevel.setText("🛡️ Varroa : Très Faible (0.2%)");
        });

        Button btnClose = new Button("Fermer la Ruche");
        btnClose.setOnAction(e -> close());

        actions.getChildren().addAll(btnHarvest, btnTreat, btnClose);

        root.getChildren().addAll(header, statsBar, frameButtonsBox, canvasHolder, actions);

        Scene scene = new Scene(root, 660, 520);
        ThemeManager.getInstance().applyTheme(scene);
        setScene(scene);
    }

    private void drawFrame() {
        GraphicsContext gc = frameCanvas.getGraphicsContext2D();
        double w = frameCanvas.getWidth();
        double h = frameCanvas.getHeight();

        gc.clearRect(0, 0, w, h);

        // Outer Wooden Frame
        double bx = 20, by = 15, bw = w - 40, bh = h - 30;

        // Top wooden ear bar
        gc.setFill(Color.web("#854d0e"));
        gc.fillRect(bx - 10, by, bw + 20, 12);

        // Left, Right, Bottom bars
        gc.fillRect(bx, by + 12, 10, bh - 12);
        gc.fillRect(bx + bw - 10, by + 12, 10, bh - 12);
        gc.fillRect(bx, by + bh - 10, bw, 10);

        // Wax Foundation Comb interior
        double cx = bx + 10, cy = by + 12, cw = bw - 20, ch = bh - 22;
        gc.setFill(Color.web("#d97706")); // Golden beeswax
        gc.fillRect(cx, cy, cw, ch);

        // Hexagonal mesh background pattern
        gc.setStroke(Color.web("#b45309", 0.35));
        gc.setLineWidth(1.0);
        for (double x = cx; x < cx + cw; x += 12) {
            gc.strokeLine(x, cy, x, cy + ch);
        }
        for (double y = cy; y < cy + ch; y += 10) {
            gc.strokeLine(cx, y, cx + cw, y);
        }

        // Biological Zones based on frame position (Center = Brood, Edge = Honey stores)
        boolean isBroodFrame = selectedFrameIndex >= 2 && selectedFrameIndex <= 7;
        Random rnd = new Random(selectedFrameIndex * 100L);

        if (isBroodFrame) {
            // 1. Top Capped Honey Crescent
            LinearGradient honeyGrad = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#fde047")), new Stop(1, Color.web("#ca8a04")));
            gc.setFill(honeyGrad);
            gc.fillOval(cx + 30, cy + 5, cw - 60, ch * 0.45);

            // 2. Pollen / Bee Bread intermediate band
            gc.setFill(Color.web("#ea580c", 0.85)); // Orange bee bread
            gc.fillOval(cx + 45, cy + ch * 0.22, cw - 90, ch * 0.65);

            // 3. Central Compact Capped Brood Nest
            gc.setFill(Color.web("#78350f", 0.92)); // Warm brown capped worker brood
            gc.fillOval(cx + 60, cy + ch * 0.35, cw - 120, ch * 0.58);

            // 4. Spotting Queen on frame #4 or #5
            if (selectedFrameIndex == 4) {
                double qx = cx + cw * 0.52;
                double qy = cy + ch * 0.62;
                gc.setFill(Color.web("#10b981")); // Marked Green Queen (year color code)
                gc.fillOval(qx - 8, qy - 8, 16, 16);
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(2.0);
                gc.strokeOval(qx - 8, qy - 8, 16, 16);

                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
                gc.fillText("👑 REINE MARQUÉE", qx - 45, qy - 12);
            }
        } else {
            // Edge Honey Reserve Frame (Full Capped Golden Honey)
            LinearGradient fullHoney = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#fef08a")), new Stop(0.7, Color.web("#eab308")), new Stop(1, Color.web("#a16207")));
            gc.setFill(fullHoney);
            gc.fillRect(cx + 4, cy + 4, cw - 8, ch - 8);

            // Honeycomb wax operculation caps texture
            gc.setStroke(Color.web("#fef9c3", 0.60));
            gc.setLineWidth(1.5);
            for (int i = 0; i < 40; i++) {
                double hx = cx + 20 + rnd.nextDouble() * (cw - 40);
                double hy = cy + 20 + rnd.nextDouble() * (ch - 40);
                gc.strokeOval(hx, hy, 14, 8);
            }
        }
    }

    private void updateFrameStats() {
        boolean isBroodFrame = selectedFrameIndex >= 2 && selectedFrameIndex <= 7;
        if (isBroodFrame) {
            lblFrameStats.setText(String.format("Cadre #%d (Cœur de Couvain) : 1 850 alvéoles de couvain operculé | 420 g miel de rive | 310 g pain d'abeille", selectedFrameIndex + 1));
        } else {
            lblFrameStats.setText(String.format("Cadre #%d (Réserve Latérale) : 2.85 kg de Miel Toutes Fleurs 100%% Operculé (Humidité 17.4%%)", selectedFrameIndex + 1));
        }
    }
}
