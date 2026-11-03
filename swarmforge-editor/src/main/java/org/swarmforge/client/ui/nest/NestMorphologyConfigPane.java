/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.nest;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.client.ui.HiveInspectionDialog;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.NotificationOverlay;

import java.util.Random;

/**
 * Configuration card for nest seed and morphological parameters (depth, worker size,
 * tunnel width, branching ratio, and hive inspection).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class NestMorphologyConfigPane extends VBox {

    private final I18nManager i18n = I18nManager.getInstance();

    private long nestSeed = 123456L;
    private final TextField seedField;
    private final Slider workerSizeSlider;
    private final Slider depthSlider;
    private final Slider tunnelWidthSlider;
    private final Slider branchingSlider;
    private final Label passageCheckLabel;

    public NestMorphologyConfigPane(
            Runnable onRegenAndRepaint,
            Runnable onAutoAdapt,
            VBox parentContainer
    ) {
        super(7);

        // Seed Control
        Label lblSeed = new Label();
        lblSeed.textProperty().bind(i18n.createStringBinding("nest.seed.label"));
        lblSeed.getStyleClass().addAll("bold-label", "sync-status-label");

        seedField = new TextField(String.valueOf(nestSeed));
        seedField.setPrefWidth(90);
        seedField.tooltipProperty().bind(i18n.createTooltipBinding("nest.seed.tt"));
        seedField.setOnAction(e -> applySeedFromField(onRegenAndRepaint));
        seedField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) applySeedFromField(onRegenAndRepaint);
        });

        Button btnNewSeed = new Button();
        btnNewSeed.textProperty().bind(i18n.createStringBinding("nest.seed.new_btn"));
        btnNewSeed.setGraphic(new FontIcon(Feather.REFRESH_CW));
        btnNewSeed.setStyle("-fx-background-color:#8b5cf6;-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:11px;-fx-padding:5 10;-fx-background-radius:4;");
        btnNewSeed.tooltipProperty().bind(i18n.createTooltipBinding("nest.seed.new_btn.tt"));
        btnNewSeed.setOnAction(e -> {
            setSeed(new Random().nextLong(100000, 999999));
            if (onRegenAndRepaint != null) onRegenAndRepaint.run();
            NotificationOverlay.show(parentContainer != null ? parentContainer : this,
                    java.text.MessageFormat.format(i18n.get("nest.seed.applied"), nestSeed),
                    NotificationOverlay.NotificationType.INFO);
        });

        HBox seedRow = new HBox(8, seedField, btnNewSeed);
        seedRow.setAlignment(Pos.CENTER_LEFT);

        Button btnAutoAdapt = new Button();
        btnAutoAdapt.textProperty().bind(i18n.createStringBinding("nest.adapt.btn"));
        btnAutoAdapt.setGraphic(new FontIcon(Feather.ZAP));
        btnAutoAdapt.setStyle("-fx-background-color:#0284c7;-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:11px;-fx-padding:5 12;-fx-background-radius:4;");
        btnAutoAdapt.tooltipProperty().bind(i18n.createTooltipBinding("nest.adapt.btn.tt"));
        btnAutoAdapt.setOnAction(e -> {
            if (onAutoAdapt != null) onAutoAdapt.run();
        });

        Button btnInspectHive = new Button("\uD83D\uDC1D " + i18n.get("hive.inspect.button", "Inspection Apicole (Cadres)"));
        btnInspectHive.setPrefWidth(270);
        btnInspectHive.setTooltip(new Tooltip(i18n.get("hive.inspect.tooltip", "Ouvrir l'inspection apicole détaillée des 10 cadres Dadant")));
        btnInspectHive.setOnAction(e -> {
            Stage owner = getScene() != null && getScene().getWindow() instanceof Stage ? (Stage) getScene().getWindow() : null;
            new HiveInspectionDialog(owner, null).show();
        });

        // Morphological Sliders
        workerSizeSlider = mkSlider(2.0, 30.0, 5.0);
        workerSizeSlider.tooltipProperty().bind(i18n.createTooltipBinding("nest.worker.size.tt"));

        depthSlider = mkSlider(0.5, 8.0, 2.5);
        depthSlider.tooltipProperty().bind(i18n.createTooltipBinding("nest.depth.tt"));

        tunnelWidthSlider = mkSlider(2.0, 25.0, 4.0);
        tunnelWidthSlider.tooltipProperty().bind(i18n.createTooltipBinding("nest.tunnel.width.tt"));

        branchingSlider = mkSlider(1, 5, 3);
        branchingSlider.tooltipProperty().bind(i18n.createTooltipBinding("nest.branching.tt"));

        addLsn(onRegenAndRepaint, workerSizeSlider, depthSlider, tunnelWidthSlider, branchingSlider);

        Label lblWorkerAuto = new Label();
        lblWorkerAuto.textProperty().bind(i18n.createStringBinding("nest.worker.size.label"));
        lblWorkerAuto.setStyle("-fx-font-size:10px;-fx-text-fill:#94a3b8;");

        Label lblTunnelAuto = new Label();
        lblTunnelAuto.textProperty().bind(i18n.createStringBinding("nest.tunnel.label"));
        lblTunnelAuto.setStyle("-fx-font-size:10px;-fx-text-fill:#94a3b8;");

        passageCheckLabel = new Label(i18n.get("nest.passage.ok"));
        passageCheckLabel.setStyle("-fx-font-size:10px;-fx-text-fill:#22c55e;-fx-font-weight:bold;");
        updatePassageCheckLabel();

        workerSizeSlider.valueProperty().addListener((o, oldV, newV) -> updatePassageCheckLabel());
        tunnelWidthSlider.valueProperty().addListener((o, oldV, newV) -> updatePassageCheckLabel());

        Label lblMaxDepth = new Label();
        lblMaxDepth.textProperty().bind(i18n.createStringBinding("nest.arch.max_depth"));
        Label lblBranching = new Label();
        lblBranching.textProperty().bind(i18n.createStringBinding("nest.arch.branching"));

        getChildren().addAll(
                lblSeed, seedRow,
                btnAutoAdapt,
                btnInspectHive,
                lblWorkerAuto, sv(workerSizeSlider, "mm"),
                lblTunnelAuto, sv(tunnelWidthSlider, "mm"),
                passageCheckLabel,
                lblMaxDepth, sv(depthSlider, "m"),
                lblBranching, sv(branchingSlider, "")
        );
    }

    private void applySeedFromField(Runnable onRegenAndRepaint) {
        if (seedField == null) return;
        try {
            long val = Long.parseLong(seedField.getText().trim());
            if (val != nestSeed) {
                nestSeed = val;
                if (onRegenAndRepaint != null) onRegenAndRepaint.run();
            }
        } catch (NumberFormatException ex) {
            seedField.setText(String.valueOf(nestSeed));
        }
    }

    public long getSeed() {
        return nestSeed;
    }

    public void setSeed(long seed) {
        this.nestSeed = seed;
        if (seedField != null) {
            seedField.setText(String.valueOf(seed));
        }
    }

    public void updatePassageCheckLabel() {
        if (passageCheckLabel == null) return;
        double wMm = workerSizeSlider != null ? workerSizeSlider.getValue() : 4.0;
        double tunnelScale = tunnelWidthSlider != null ? tunnelWidthSlider.getValue() : 2.0;
        double effectiveWidthMm = Math.max(tunnelScale * 3.0, (tunnelScale / 2.0) * wMm * 1.25);

        if (effectiveWidthMm < wMm * 0.95) {
            passageCheckLabel.setText(String.format(i18n.get("nest.tunnel.narrow"), wMm));
            passageCheckLabel.setStyle("-fx-font-size:10px;-fx-text-fill:#ef4444;-fx-font-weight:bold;");
        } else if (effectiveWidthMm < wMm * 1.2) {
            passageCheckLabel.setText(String.format(i18n.get("nest.tunnel.single_file"), wMm));
            passageCheckLabel.setStyle("-fx-font-size:10px;-fx-text-fill:#eab308;-fx-font-weight:bold;");
        } else {
            passageCheckLabel.setText(String.format(i18n.get("nest.tunnel.fluid"), effectiveWidthMm));
            passageCheckLabel.setStyle("-fx-font-size:10px;-fx-text-fill:#22c55e;-fx-font-weight:bold;");
        }
    }

    private void addLsn(Runnable callback, Slider... sliders) {
        for (Slider s : sliders) {
            s.valueProperty().addListener((o, a, b) -> {
                if (callback != null) callback.run();
            });
        }
    }

    private Slider mkSlider(double min, double max, double val) {
        Slider s = new Slider(min, max, val);
        s.setShowTickLabels(true);
        s.setShowTickMarks(true);
        s.setMajorTickUnit((max - min) / 4);
        s.setPrefWidth(165);
        return s;
    }

    private HBox sv(Slider s, String unit) {
        HBox b = new HBox(8);
        b.setAlignment(Pos.CENTER_LEFT);
        String unitSuffix = (unit != null && !unit.isEmpty()) ? " " + unit : "";
        Label v = new Label(fmt(s.getValue()) + unitSuffix);
        v.setStyle("-fx-text-fill:#00d4ff;-fx-min-width:48;-fx-font-weight:bold;");
        s.valueProperty().addListener((o, a, n) -> v.setText(fmt(n.doubleValue()) + unitSuffix));
        b.getChildren().addAll(s, v);
        return b;
    }

    private String fmt(double d) {
        return String.format("%.1f", d);
    }

    public Slider getWorkerSizeSlider() { return workerSizeSlider; }
    public Slider getDepthSlider() { return depthSlider; }
    public Slider getTunnelWidthSlider() { return tunnelWidthSlider; }
    public Slider getBranchingSlider() { return branchingSlider; }
    public Label getPassageCheckLabel() { return passageCheckLabel; }
}
