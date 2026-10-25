/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.ThemeManager;
import org.swarmforge.core.engine.ComputeAccelerationMode;
import org.swarmforge.core.engine.EnginePreferences;
import org.swarmforge.core.engine.RustNativeEngine;
import org.swarmforge.core.engine.SimulationEngineType;

import java.util.Locale;

/**
 * Settings and Configuration pane for SwarmForge Studio.
 * Enforces centralized simulation parameters: SI/metric units, UI themes,
 * internationalization (i18n), execution backend, and hardware acceleration.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SettingsPane extends ScrollPane {

    public SettingsPane() {
        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        I18nManager i18n = I18nManager.getInstance();
        VBox main = new VBox(20);
        main.setPadding(new Insets(20));

        Label title = new Label();
        title.textProperty().bind(i18n.createStringBinding("settings.title"));
        title.getStyleClass().add("card-title");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        VBox headerBox = new VBox(6);
        headerBox.getChildren().addAll(title, new Separator());

        // --- CARD 1: GENERAL & UI (Language / Theme) ---
        VBox cardUi = new VBox(14);
        cardUi.getStyleClass().add("card-pane");
        cardUi.setPadding(new Insets(16));

        Label uiCardTitle = new Label();
        uiCardTitle.textProperty().bind(i18n.createStringBinding("settings.title"));
        uiCardTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        GridPane gridUi = new GridPane();
        gridUi.setHgap(20);
        gridUi.setVgap(14);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(220);
        col1.setPrefWidth(240);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        gridUi.getColumnConstraints().addAll(col1, col2);

        // 1. Language Row
        Label langLabel = new Label();
        langLabel.textProperty().bind(i18n.createStringBinding("settings.language"));
        langLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600;");
        langLabel.tooltipProperty().bind(i18n.createTooltipBinding("settings.language.tt"));

        ComboBox<String> langCombo = new ComboBox<>();
        langCombo.getItems().addAll("English", "Français", "Español", "Deutsch", "中文");
        langCombo.setMaxWidth(Double.MAX_VALUE);
        langCombo.tooltipProperty().bind(i18n.createTooltipBinding("settings.language.combo.tt"));

        Locale currentLoc = i18n.getLocale();
        String langStr = currentLoc.getLanguage();
        if (langStr.equals("fr")) langCombo.setValue("Français");
        else if (langStr.equals("es")) langCombo.setValue("Español");
        else if (langStr.equals("de")) langCombo.setValue("Deutsch");
        else if (langStr.equals("zh")) langCombo.setValue("中文");
        else langCombo.setValue("English");

        langCombo.setOnAction(e -> {
            String val = langCombo.getValue();
            if ("Français".equals(val)) i18n.setLocale(Locale.FRENCH);
            else if ("Español".equals(val)) i18n.setLocale(Locale.forLanguageTag("es"));
            else if ("Deutsch".equals(val)) i18n.setLocale(Locale.forLanguageTag("de"));
            else if ("中文".equals(val)) i18n.setLocale(Locale.forLanguageTag("zh"));
            else i18n.setLocale(Locale.ENGLISH);
        });

        // 2. Theme Row
        Label themeLabel = new Label();
        themeLabel.textProperty().bind(i18n.createStringBinding("settings.theme"));
        themeLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600;");
        themeLabel.tooltipProperty().bind(i18n.createTooltipBinding("settings.theme.tt"));

        ComboBox<String> themeCombo = new ComboBox<>();
        themeCombo.getItems().addAll("Dark Theme", "Light Theme");
        themeCombo.setMaxWidth(Double.MAX_VALUE);
        themeCombo.tooltipProperty().bind(i18n.createTooltipBinding("settings.theme.combo.tt"));
        if (ThemeManager.getInstance().getCurrentTheme() == ThemeManager.Theme.DARK) {
            themeCombo.setValue("Dark Theme");
        } else {
            themeCombo.setValue("Light Theme");
        }

        themeCombo.setOnAction(e -> {
            if ("Dark Theme".equals(themeCombo.getValue())) {
                ThemeManager.getInstance().setTheme(ThemeManager.Theme.DARK);
            } else {
                ThemeManager.getInstance().setTheme(ThemeManager.Theme.LIGHT);
            }
        });

        gridUi.add(langLabel, 0, 0);
        gridUi.add(langCombo, 1, 0);
        gridUi.add(themeLabel, 0, 1);
        gridUi.add(themeCombo, 1, 1);

        cardUi.getChildren().addAll(uiCardTitle, new Separator(), gridUi);

        // --- CARD 2: LOCAL SIMULATION ENGINE & CONCURRENCY ---
        VBox cardEngine = new VBox(14);
        cardEngine.getStyleClass().add("card-pane");
        cardEngine.setPadding(new Insets(16));

        Label engineCardTitle = new Label();
        engineCardTitle.textProperty().bind(i18n.createStringBinding("settings.engine.title"));
        engineCardTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        Label engineCardDesc = new Label();
        engineCardDesc.textProperty().bind(i18n.createStringBinding("settings.engine.desc"));
        engineCardDesc.setWrapText(true);
        engineCardDesc.getStyleClass().add("text-muted");

        GridPane gridEngine = new GridPane();
        gridEngine.setHgap(20);
        gridEngine.setVgap(14);
        gridEngine.getColumnConstraints().addAll(col1, col2);

        // 2.1 Backend Engine Selector
        Label backendLabel = new Label();
        backendLabel.textProperty().bind(i18n.createStringBinding("settings.engine.backend"));
        backendLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600;");
        backendLabel.tooltipProperty().bind(i18n.createTooltipBinding("settings.engine.backend.tt"));

        ComboBox<SimulationEngineType> backendCombo = new ComboBox<>();
        backendCombo.getItems().addAll(
            SimulationEngineType.JAVA_ECS,
            SimulationEngineType.RUST_NATIVE
        );
        backendCombo.setMaxWidth(Double.MAX_VALUE);
        backendCombo.tooltipProperty().bind(i18n.createTooltipBinding("settings.engine.backend.tt"));

        backendCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(SimulationEngineType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item == SimulationEngineType.RUST_NATIVE
                        ? i18n.get("settings.engine.backend.rust")
                        : i18n.get("settings.engine.backend.java"));
                }
            }
        });
        backendCombo.setButtonCell(backendCombo.getCellFactory().call(null));

        SimulationEngineType curEngine = EnginePreferences.getSelectedEngineType();
        backendCombo.setValue(curEngine != null ? curEngine : SimulationEngineType.JAVA_ECS);

        Label rustStatusBadge = new Label();
        boolean rustAvail = RustNativeEngine.isNativeLibraryAvailable();
        if (rustAvail) {
            rustStatusBadge.textProperty().bind(i18n.createStringBinding("settings.engine.rust_available"));
            rustStatusBadge.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-size: 11px;");
        } else {
            rustStatusBadge.textProperty().bind(i18n.createStringBinding("settings.engine.rust_unavailable"));
            rustStatusBadge.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 11px;");
        }

        VBox backendBox = new VBox(6, backendCombo, rustStatusBadge);

        backendCombo.setOnAction(e -> {
            SimulationEngineType selected = backendCombo.getValue();
            if (selected != null) {
                EnginePreferences.setSelectedEngineType(selected);
            }
        });

        // 2.2 Concurrency & Threading (Single-Core vs Multi-Core)
        Label concurrencyLabel = new Label();
        concurrencyLabel.textProperty().bind(i18n.createStringBinding("settings.engine.concurrency"));
        concurrencyLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600;");
        concurrencyLabel.tooltipProperty().bind(i18n.createTooltipBinding("settings.engine.concurrency.tt"));

        ToggleGroup concurrencyGroup = new ToggleGroup();
        RadioButton rbSingleCore = new RadioButton();
        rbSingleCore.textProperty().bind(i18n.createStringBinding("settings.engine.single_core"));
        rbSingleCore.setToggleGroup(concurrencyGroup);
        rbSingleCore.tooltipProperty().bind(i18n.createTooltipBinding("settings.engine.concurrency.tt"));

        RadioButton rbMultiCore = new RadioButton();
        rbMultiCore.textProperty().bind(i18n.createStringBinding("settings.engine.multi_core"));
        rbMultiCore.setToggleGroup(concurrencyGroup);
        rbMultiCore.tooltipProperty().bind(i18n.createTooltipBinding("settings.engine.concurrency.tt"));

        int maxCores = Runtime.getRuntime().availableProcessors();
        int currentThreads = EnginePreferences.getThreadCount();

        Spinner<Integer> threadSpinner = new Spinner<>(1, Math.max(1, maxCores), currentThreads);
        threadSpinner.setEditable(true);
        threadSpinner.setPrefWidth(120);
        threadSpinner.tooltipProperty().bind(i18n.createTooltipBinding("settings.engine.threads.tt"));

        Label threadUnitLabel = new Label();
        threadUnitLabel.textProperty().bind(i18n.createStringBinding("settings.engine.threads.unit"));
        threadUnitLabel.setStyle("-fx-font-size: 12px;");

        HBox threadsRow = new HBox(8, rbMultiCore, threadSpinner, threadUnitLabel);
        threadsRow.setAlignment(Pos.CENTER_LEFT);

        if (EnginePreferences.isSingleCore()) {
            rbSingleCore.setSelected(true);
            threadSpinner.setDisable(true);
        } else {
            rbMultiCore.setSelected(true);
            threadSpinner.setDisable(false);
        }

        rbSingleCore.setOnAction(e -> {
            EnginePreferences.setThreadCount(1);
            threadSpinner.getValueFactory().setValue(1);
            threadSpinner.setDisable(true);
        });

        rbMultiCore.setOnAction(e -> {
            int t = threadSpinner.getValue() > 1 ? threadSpinner.getValue() : maxCores;
            threadSpinner.getValueFactory().setValue(t);
            EnginePreferences.setThreadCount(t);
            threadSpinner.setDisable(false);
        });

        threadSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && rbMultiCore.isSelected()) {
                EnginePreferences.setThreadCount(newVal);
            }
        });

        VBox concurrencyBox = new VBox(8, rbSingleCore, threadsRow);

        // 2.3 Hardware Acceleration Mode (GPU vs CPU)
        Label accelLabel = new Label();
        accelLabel.textProperty().bind(i18n.createStringBinding("settings.engine.accel"));
        accelLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600;");
        accelLabel.tooltipProperty().bind(i18n.createTooltipBinding("settings.engine.accel.tt"));

        ComboBox<ComputeAccelerationMode> accelCombo = new ComboBox<>();
        accelCombo.getItems().addAll(
            ComputeAccelerationMode.AUTO,
            ComputeAccelerationMode.GPU_ACCELERATED,
            ComputeAccelerationMode.CPU_MULTITHREADED_SIMD
        );
        accelCombo.setMaxWidth(Double.MAX_VALUE);
        accelCombo.tooltipProperty().bind(i18n.createTooltipBinding("settings.engine.accel.tt"));

        accelCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ComputeAccelerationMode item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(switch (item) {
                        case GPU_ACCELERATED -> i18n.get("settings.engine.accel.gpu");
                        case CPU_MULTITHREADED_SIMD -> i18n.get("settings.engine.accel.cpu");
                        default -> i18n.get("settings.engine.accel.auto");
                    });
                }
            }
        });
        accelCombo.setButtonCell(accelCombo.getCellFactory().call(null));

        // Re-render button cells when language changes
        i18n.localeProperty().addListener((o, oldL, newL) -> {
            backendCombo.setButtonCell(backendCombo.getCellFactory().call(null));
            accelCombo.setButtonCell(accelCombo.getCellFactory().call(null));
        });

        ComputeAccelerationMode curAccel = EnginePreferences.getSelectedAccelerationMode();
        accelCombo.setValue(curAccel != null ? curAccel : ComputeAccelerationMode.AUTO);

        accelCombo.setOnAction(e -> {
            ComputeAccelerationMode selected = accelCombo.getValue();
            if (selected != null) {
                EnginePreferences.setSelectedAccelerationMode(selected);
            }
        });

        gridEngine.add(backendLabel, 0, 0);
        gridEngine.add(backendBox, 1, 0);
        gridEngine.add(concurrencyLabel, 0, 1);
        gridEngine.add(concurrencyBox, 1, 1);
        gridEngine.add(accelLabel, 0, 2);
        gridEngine.add(accelCombo, 1, 2);

        cardEngine.getChildren().addAll(engineCardTitle, engineCardDesc, new Separator(), gridEngine);

        main.getChildren().addAll(headerBox, cardUi, cardEngine);
        VBox.setVgrow(main, Priority.ALWAYS);

        setContent(main);
    }
}
