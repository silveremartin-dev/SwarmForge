/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.client.util.I18nManager;

/**
 * Builds and manages the time picker spinners and colony selection components for {@link InterventionPanel}.
 */
class InterventionTimeConfigHelper {

    private final InterventionPanel panel;

    InterventionTimeConfigHelper(InterventionPanel panel) {
        this.panel = panel;
    }

    Node createTimeAndColonyConfigNode() {
        I18nManager i18n = I18nManager.getInstance();
        VBox box = new VBox(8);

        HBox timeRow = new HBox(8);
        timeRow.setAlignment(Pos.CENTER_LEFT);

        Label lblTimeHeader = new Label();
        lblTimeHeader.textProperty().bind(i18n.createStringBinding("god.time.header"));
        lblTimeHeader.getStyleClass().add("bold-label");

        panel.spDay = new Spinner<>(1, 365, 1);
        panel.spDay.setEditable(true);
        panel.spDay.setPrefWidth(65);
        panel.spDay.tooltipProperty().bind(i18n.createTooltipBinding("god.time.spinners.tt"));

        panel.spHour = new Spinner<>(0, 23, 8);
        panel.spHour.setEditable(true);
        panel.spHour.setPrefWidth(60);
        panel.spHour.tooltipProperty().bind(i18n.createTooltipBinding("god.time.spinners.tt"));

        panel.spMin = new Spinner<>(0, 59, 0);
        panel.spMin.setEditable(true);
        panel.spMin.setPrefWidth(60);
        panel.spMin.tooltipProperty().bind(i18n.createTooltipBinding("god.time.spinners.tt"));

        panel.spSec = new Spinner<>(0, 59, 0);
        panel.spSec.setEditable(true);
        panel.spSec.setPrefWidth(60);
        panel.spSec.tooltipProperty().bind(i18n.createTooltipBinding("god.time.spinners.tt"));

        Label lblColonyTarget = new Label();
        lblColonyTarget.textProperty().bind(i18n.createStringBinding("god.time.colony"));
        lblColonyTarget.getStyleClass().add("bold-label");

        Label lblDay = new Label(); lblDay.textProperty().bind(i18n.createStringBinding("god.time.day"));
        Label lblHour = new Label(); lblHour.textProperty().bind(i18n.createStringBinding("god.time.hour"));
        Label lblMin = new Label(); lblMin.textProperty().bind(i18n.createStringBinding("god.time.min"));
        Label lblSec = new Label(); lblSec.textProperty().bind(i18n.createStringBinding("god.time.sec"));

        panel.eventColonySelect = new ComboBox<>();
        panel.eventColonySelect.getItems().addAll(
                i18n.get("god.colony.all"),
                i18n.get("god.colony.default1"),
                i18n.get("god.colony.default2")
        );
        panel.eventColonySelect.getSelectionModel().selectFirst();
        panel.eventColonySelect.setPrefWidth(210);
        panel.eventColonySelect.tooltipProperty().bind(i18n.createTooltipBinding("god.time.colony.tt"));

        Button btnSyncTime = new Button();
        btnSyncTime.textProperty().bind(i18n.createStringBinding("god.btn.sync_time"));
        btnSyncTime.getStyleClass().add("primary-pill-button");
        btnSyncTime.setGraphic(new FontIcon(Feather.CLOCK));
        btnSyncTime.tooltipProperty().bind(i18n.createTooltipBinding("god.btn.sync_time.tt"));
        btnSyncTime.setOnAction(e -> syncTimePickersWithCurrentTick(10));

        timeRow.getChildren().addAll(
                lblTimeHeader,
                lblDay, panel.spDay,
                lblHour, panel.spHour,
                lblMin, panel.spMin,
                lblSec, panel.spSec,
                lblColonyTarget, panel.eventColonySelect,
                btnSyncTime
        );

        panel.lblTargetTickSummary = new Label();
        panel.lblTargetTickSummary.getStyleClass().add("accent-subtext");

        Runnable updateSummary = () -> {
            panel.lblTargetTickSummary.setText(I18nManager.getInstance().get("god.spatial.target_summary",
                    panel.spDay.getValue(), panel.spHour.getValue(), panel.spMin.getValue(), panel.spSec.getValue(),
                    panel.eventColonySelect.getValue()));
        };
        updateSummary.run();

        panel.spDay.valueProperty().addListener((o, a, b) -> updateSummary.run());
        panel.spHour.valueProperty().addListener((o, a, b) -> updateSummary.run());
        panel.spMin.valueProperty().addListener((o, a, b) -> updateSummary.run());
        panel.spSec.valueProperty().addListener((o, a, b) -> updateSummary.run());
        panel.eventColonySelect.valueProperty().addListener((o, a, b) -> updateSummary.run());

        box.getChildren().addAll(timeRow, panel.lblTargetTickSummary);
        return box;
    }

    void syncTimePickersWithCurrentTick(int offsetSeconds) {
        long targetSec = (long) (panel.currentSimulationTick * panel.simulationStepSec) + offsetSeconds;
        int d = (int) (targetSec / 86400L) + 1;
        long rem = targetSec % 86400L;
        int h = (int) (rem / 3600L);
        rem %= 3600L;
        int m = (int) (rem / 60L);
        int s = (int) (rem % 60L);

        panel.spDay.getValueFactory().setValue(Math.max(1, d));
        panel.spHour.getValueFactory().setValue(h);
        panel.spMin.getValueFactory().setValue(m);
        panel.spSec.getValueFactory().setValue(s);
    }

    long calculateTargetSeconds() {
        int d = panel.spDay.getValue() != null ? panel.spDay.getValue() : 1;
        int h = panel.spHour.getValue() != null ? panel.spHour.getValue() : 8;
        int m = panel.spMin.getValue() != null ? panel.spMin.getValue() : 0;
        int s = panel.spSec.getValue() != null ? panel.spSec.getValue() : 0;
        return (long) (d - 1) * 86400L + h * 3600L + m * 60L + s;
    }
}
