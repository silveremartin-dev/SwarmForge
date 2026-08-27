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
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Intervention Panel - God Mode controls for runtime simulation manipulation.
 * Features an overarching Scheduled Event Queue system with atomic event types,
 * comprehensive insect food taxonomy, multi-day disaster duration scaling,
 * and direct manipulation of fundamental abiotic physical drivers.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class InterventionPanel extends BorderPane {

    public enum Category {
        ENTITIES("🐜", "Entités", "#3b82f6"),
        RESOURCE("🍖", "Ressources", "#10b981"),
        DISASTER("🌊", "Catastrophe", "#ef4444"),
        ABIOTIC("🌡️", "Climat", "#f59e0b");

        public final String icon;
        public final String label;
        public final String color;

        Category(String icon, String label, String color) {
            this.icon = icon;
            this.label = label;
            this.color = color;
        }
    }

    public static class ScheduledEvent {
        public long targetTick;
        public String timeFormatted;
        public Category category;
        public String eventType;
        public String colonyTarget;
        public String description;

        // Category-specific parameters
        public String entityAction = "SPAWN"; // SPAWN, KILL, EXTINCT
        public String caste = "Ouvrière";
        public int count = 10;
        public float posX = 1.0f, posY = 1.0f, posZ = 0.1f;

        public String resourceType = "Surface Food";
        public String foodNature = "Graines & Semences";
        public float amount = 100f;

        public String disasterType = "Flood & Heavy Rain";
        public float intensity = 0.5f;
        public int durationMinutes = 60;

        public float tempCelsius = 22.0f;
        public float humidityPercent = 65.0f;
        public float windMetersPerSec = 1.5f;
        public float solarWattsPerM2 = 450.0f;

        public boolean executed = false;
        public boolean paused = false;

        public ScheduledEvent(long targetTick, String timeFormatted, Category category, String eventType, String colonyTarget, String description) {
            this.targetTick = targetTick;
            this.timeFormatted = timeFormatted;
            this.category = category;
            this.eventType = eventType;
            this.colonyTarget = colonyTarget;
            this.description = description;
        }
    }

    private Spinner<Integer> antCountSpinner;
    private ComboBox<String> casteSelect;
    private ComboBox<String> colonySelect;
    private ComboBox<String> entityActionSelect;
    private TextField posXField, posYField, posZField;
    private ComboBox<String> disasterSelect;
    private Slider intensitySlider;
    private Slider durationSlider;
    private Label durationValLabel;
    private TextArea logArea;

    private ComboBox<String> eventColonySelect;
    private Spinner<Integer> spDay, spHour, spMin, spSec;
    private Label lblTargetTickSummary;

    private Slider tempSlider;
    private Label tempValLabel;
    private Slider humiditySlider;
    private Label humidityValLabel;
    private Slider windSlider;
    private Label windValLabel;
    private Slider solarSlider;
    private Label solarValLabel;

    private Label derivedPheroLabel;
    private Label derivedPrimaryProductivityLabel;
    private Label derivedOvipositionLabel;

    private Slider foodSlider;
    private ComboBox<String> resourceTypeSelect;
    private ComboBox<String> foodNatureSelect;

    private Label simStateWarningLabel;

    private long currentSimulationTick = 0;
    private double simulationStepSec = 0.016666666666666666;

    // Callback for interventions (set by client)
    private InterventionCallback callback;

    public interface InterventionCallback {
        void spawnAnts(String colonyId, String caste, int count, float x, float y, float z);
        void killAnts(String colonyId, String caste, int count);
        void spawnFood(float x, float y, float z, float amount);
        void triggerDisaster(String type, float intensity);
        void stopDisasters();
        void modifyParameter(String param, Object value);
    }

    private final javafx.collections.ObservableList<ScheduledEvent> scheduledEventsList = javafx.collections.FXCollections.observableArrayList();
    private ListView<ScheduledEvent> scheduledEventsListView;

    public InterventionPanel() {
        setPadding(new Insets(15));

        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();

        // Header Section
        Label title = new Label();
        title.textProperty().bind(i18n.createStringBinding("god.title"));
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
        title.setTooltip(new Tooltip(i18n.get("god.title.tt")));

        simStateWarningLabel = new Label();
        simStateWarningLabel.textProperty().bind(i18n.createStringBinding("god.warn.stopped"));
        simStateWarningLabel.getStyleClass().add("notice-warn");
        simStateWarningLabel.setMaxWidth(Double.MAX_VALUE);

        Label persistenceNoticeLabel = new Label();
        persistenceNoticeLabel.textProperty().bind(i18n.createStringBinding("god.notice.physics"));
        persistenceNoticeLabel.getStyleClass().add("notice-info");
        persistenceNoticeLabel.setWrapText(true);
        persistenceNoticeLabel.setMaxWidth(Double.MAX_VALUE);

        VBox header = new VBox(5, title, simStateWarningLabel, persistenceNoticeLabel);
        setTop(header);

        // Main scrollable content
        VBox mainContent = new VBox(15);
        mainContent.setPadding(new Insets(12, 5, 12, 5));

        // 1. OVERARCHING CONTAINER: Scheduled Events Queue
        VBox queueContainer = createScheduledEventsQueueBlock();
        mainContent.getChildren().add(queueContainer);

        // 2. CONFIGURATION SUB-BLOCKS TITLE
        Label subBlocksHeader = new Label();
        subBlocksHeader.textProperty().bind(i18n.createStringBinding("god.subblocks.title"));
        subBlocksHeader.getStyleClass().add("sub-title");
        mainContent.getChildren().add(subBlocksHeader);

        // Sub-block 1: Time Target (Calendar Time Only) & Colony Target
        mainContent.getChildren().add(createCardSubBlock(i18n.get("god.block.time_colony"), createTimeAndColonyConfigNode()));

        // Sub-block 2: Entities & Castes (Apparition / Élimination / Extinction)
        mainContent.getChildren().add(createCardSubBlock(i18n.get("god.block.entities"), createEntitiesSubBlockNode()));

        // Sub-block 3: Resources & Biomass (With Complete Social Insect Food Taxonomy)
        mainContent.getChildren().add(createCardSubBlock(i18n.get("god.block.resources"), createResourcesSubBlockNode()));

        // Sub-block 4: Disasters & Environmental Events (Magnitude & Multi-Week Duration)
        mainContent.getChildren().add(createCardSubBlock(i18n.get("god.block.disasters"), createDisastersSubBlockNode()));

        // Sub-block 5: Abiotic Core Physical Drivers (Temperature, Humidity, Wind, Solar Radiation)
        mainContent.getChildren().add(createCardSubBlock(i18n.get("god.block.abiotic"), createParametersSubBlockNode()));

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        setCenter(scrollPane);

        // Bottom: Action Log
        setBottom(createLogSection());
    }

    private VBox createCardSubBlock(String titleStr, Node contentNode) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.getStyleClass().add("sub-card");
        Label title = new Label(titleStr);
        title.getStyleClass().add("accent-title");
        card.getChildren().addAll(title, new Separator(), contentNode);
        return card;
    }

    /**
     * Master Scheduled Event Queue Box.
     */
    private VBox createScheduledEventsQueueBlock() {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        VBox box = new VBox(10);
        box.setPadding(new Insets(12));
        box.getStyleClass().add("queue-box");

        Label lblQueueTitle = new Label();
        lblQueueTitle.textProperty().bind(i18n.createStringBinding("god.queue.title"));
        lblQueueTitle.getStyleClass().add("accent-title");

        scheduledEventsListView = new ListView<>(scheduledEventsList);
        scheduledEventsListView.setPrefHeight(150);
        scheduledEventsListView.getStyleClass().add("log-text-area");
        scheduledEventsListView.setCellFactory(param -> new ListCell<ScheduledEvent>() {
            @Override
            protected void updateItem(ScheduledEvent ev, boolean empty) {
                super.updateItem(ev, empty);
                if (empty || ev == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox cellBox = new HBox(8);
                    cellBox.setAlignment(Pos.CENTER_LEFT);

                    Label badgeCat = new Label(ev.category.icon + " " + ev.category.label);
                    badgeCat.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 3;", ev.category.color));

                    Label timeLabel = new Label("[" + ev.timeFormatted + " | Tick #" + ev.targetTick + "]");
                    timeLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px;");

                    Label descLabel = new Label(ev.eventType + (ev.colonyTarget != null ? " (" + ev.colonyTarget + ")" : "") + " : " + ev.description);
                    descLabel.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 11px;");
                    HBox.setHgrow(descLabel, Priority.ALWAYS);

                    String statusText = ev.executed ? "✅ Exécuté" : ev.paused ? "⏸️ Suspendu" : "⏳ En Attente";
                    String statusBg = ev.executed ? "#10b981" : ev.paused ? "#f59e0b" : "#0284c7";
                    Label statusBadge = new Label(statusText);
                    statusBadge.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 3;", statusBg));

                    cellBox.getChildren().addAll(badgeCat, timeLabel, descLabel, statusBadge);
                    setGraphic(cellBox);
                }
            }
        });

        // Queue Control Toolbar Buttons
        HBox btnToolbar = new HBox(8);
        btnToolbar.setAlignment(Pos.CENTER_LEFT);

        Button btnRunNow = new Button();
        btnRunNow.textProperty().bind(i18n.createStringBinding("god.queue.btn.run_selected"));
        btnRunNow.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnRunNow.setOnAction(e -> {
            ScheduledEvent ev = scheduledEventsListView.getSelectionModel().getSelectedItem();
            if (ev != null) {
                executeScheduledEvent(ev);
                ev.executed = true;
                scheduledEventsListView.refresh();
            }
        });

        Button btnTogglePause = new Button();
        btnTogglePause.textProperty().bind(i18n.createStringBinding("god.queue.btn.toggle_pause"));
        btnTogglePause.getStyleClass().add("btn-secondary");
        btnTogglePause.setOnAction(e -> {
            ScheduledEvent ev = scheduledEventsListView.getSelectionModel().getSelectedItem();
            if (ev != null) {
                ev.paused = !ev.paused;
                scheduledEventsListView.refresh();
            }
        });

        Button btnDeleteEv = new Button();
        btnDeleteEv.textProperty().bind(i18n.createStringBinding("god.queue.btn.delete"));
        btnDeleteEv.getStyleClass().add("btn-danger");
        btnDeleteEv.setOnAction(e -> {
            ScheduledEvent ev = scheduledEventsListView.getSelectionModel().getSelectedItem();
            if (ev != null) {
                scheduledEventsList.remove(ev);
            }
        });

        Button btnClearAll = new Button();
        btnClearAll.textProperty().bind(i18n.createStringBinding("god.queue.btn.clear_all"));
        btnClearAll.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnClearAll.setOnAction(e -> {
            scheduledEventsList.clear();
            log("🧹 La file d'attente d'événements a été entièrement vidée.");
        });

        btnToolbar.getChildren().addAll(btnRunNow, btnTogglePause, btnDeleteEv, btnClearAll);

        box.getChildren().addAll(lblQueueTitle, scheduledEventsListView, btnToolbar);
        return box;
    }

    /**
     * Sub-block 1: Calendar Time Input (Jour / Heure / Minute / Seconde) & Colony Target
     */
    private Node createTimeAndColonyConfigNode() {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        VBox box = new VBox(8);

        HBox timeRow = new HBox(8);
        timeRow.setAlignment(Pos.CENTER_LEFT);

        Label lblTimeHeader = new Label();
        lblTimeHeader.textProperty().bind(i18n.createStringBinding("god.time.header"));
        lblTimeHeader.setStyle("-fx-font-weight: bold;");

        spDay = new Spinner<>(1, 365, 1); spDay.setEditable(true); spDay.setPrefWidth(65);
        spHour = new Spinner<>(0, 23, 8); spHour.setEditable(true); spHour.setPrefWidth(60);
        spMin = new Spinner<>(0, 59, 0); spMin.setEditable(true); spMin.setPrefWidth(60);
        spSec = new Spinner<>(0, 59, 0); spSec.setEditable(true); spSec.setPrefWidth(60);

        Label lblColonyTarget = new Label();
        lblColonyTarget.textProperty().bind(i18n.createStringBinding("god.time.colony"));
        lblColonyTarget.setStyle("-fx-font-weight: bold;");

        Label lblDay = new Label(); lblDay.textProperty().bind(i18n.createStringBinding("god.time.day"));
        Label lblHour = new Label(); lblHour.textProperty().bind(i18n.createStringBinding("god.time.hour"));
        Label lblMin = new Label(); lblMin.textProperty().bind(i18n.createStringBinding("god.time.min"));
        Label lblSec = new Label(); lblSec.textProperty().bind(i18n.createStringBinding("god.time.sec"));

        eventColonySelect = new ComboBox<>();
        eventColonySelect.getItems().addAll(
                i18n.get("god.colony.all"),
                i18n.get("god.colony.default1"),
                i18n.get("god.colony.default2")
        );
        eventColonySelect.getSelectionModel().selectFirst();
        eventColonySelect.setPrefWidth(210);

        Button btnSyncTime = new Button();
        btnSyncTime.textProperty().bind(i18n.createStringBinding("god.btn.sync_time"));
        btnSyncTime.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand;");
        btnSyncTime.setGraphic(new FontIcon(Feather.CLOCK));
        btnSyncTime.setOnAction(e -> syncTimePickersWithCurrentTick(10));

        timeRow.getChildren().addAll(
                lblTimeHeader,
                lblDay, spDay,
                lblHour, spHour,
                lblMin, spMin,
                lblSec, spSec,
                lblColonyTarget, eventColonySelect,
                btnSyncTime
        );

        lblTargetTickSummary = new Label("🎯 Horodatage visé : Jour 1 08:00:00 (Pas #0) — Colonie Cible: Toutes les Colonies (Global)");
        lblTargetTickSummary.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px;");

        Runnable updateSummary = () -> {
            long tSec = calculateTargetSeconds();
            long tTick = Math.max(1, Math.round(tSec / simulationStepSec));
            lblTargetTickSummary.setText(String.format("🎯 Horodatage visé : Jour %d %02d:%02d:%02d (Pas #%d) — Colonie: %s",
                    spDay.getValue(), spHour.getValue(), spMin.getValue(), spSec.getValue(), tTick, eventColonySelect.getValue()));
        };

        spDay.valueProperty().addListener((o, a, b) -> updateSummary.run());
        spHour.valueProperty().addListener((o, a, b) -> updateSummary.run());
        spMin.valueProperty().addListener((o, a, b) -> updateSummary.run());
        spSec.valueProperty().addListener((o, a, b) -> updateSummary.run());
        eventColonySelect.valueProperty().addListener((o, a, b) -> updateSummary.run());

        box.getChildren().addAll(timeRow, lblTargetTickSummary);
        return box;
    }

    private long calculateTargetSeconds() {
        int d = spDay.getValue() != null ? spDay.getValue() : 1;
        int h = spHour.getValue() != null ? spHour.getValue() : 8;
        int m = spMin.getValue() != null ? spMin.getValue() : 0;
        int s = spSec.getValue() != null ? spSec.getValue() : 0;
        return (long) (d - 1) * 86400L + h * 3600L + m * 60L + s;
    }

    /**
     * Sub-block 2: Entities & Castes (Apparition / Injection & Élimination / Extinction)
     */
    private Node createEntitiesSubBlockNode() {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        VBox box = new VBox(10);
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);

        Label colLabel = new Label();
        colLabel.textProperty().bind(i18n.createStringBinding("god.time.colony"));
        colLabel.setStyle("-fx-font-weight: bold;");
        colonySelect = new ComboBox<>();
        colonySelect.getItems().addAll(
                i18n.get("god.colony.default1"),
                i18n.get("god.colony.default2")
        );
        colonySelect.getSelectionModel().selectFirst();
        colonySelect.setPrefWidth(220);
        colonySelect.setOnAction(e -> updateCastesForSelectedColony(colonySelect.getValue()));

        Label lblAction = new Label();
        lblAction.textProperty().bind(i18n.createStringBinding("god.entities.action"));
        entityActionSelect = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(
                i18n.get("god.entities.action.spawn"),
                i18n.get("god.entities.action.kill"),
                i18n.get("god.entities.action.extinct")
        ));
        entityActionSelect.getSelectionModel().selectFirst();
        entityActionSelect.setPrefWidth(220);

        Label casteLabel = new Label();
        casteLabel.textProperty().bind(i18n.createStringBinding("god.entities.caste"));
        casteSelect = new ComboBox<>();
        casteSelect.setPrefWidth(220);
        updateCastesForSelectedColony(colonySelect.getValue());

        Label countLabel = new Label();
        countLabel.textProperty().bind(i18n.createStringBinding("god.entities.count"));
        antCountSpinner = new Spinner<>(1, 1000, 10);
        antCountSpinner.setEditable(true);
        antCountSpinner.setPrefWidth(100);

        Label posLabel = new Label();
        posLabel.textProperty().bind(i18n.createStringBinding("god.entities.pos"));
        HBox posBox = new HBox(5);
        posXField = new TextField("1.0"); posXField.setPrefWidth(55);
        posYField = new TextField("1.0"); posYField.setPrefWidth(55);
        posZField = new TextField("0.1"); posZField.setPrefWidth(55);
        posBox.getChildren().addAll(new Label("X:"), posXField, new Label("Y:"), posYField, new Label("Z:"), posZField);

        grid.add(colLabel, 0, 0); grid.add(colonySelect, 1, 0);
        grid.add(lblAction, 0, 1); grid.add(entityActionSelect, 1, 1);
        grid.add(casteLabel, 0, 2); grid.add(casteSelect, 1, 2);
        grid.add(countLabel, 0, 3); grid.add(antCountSpinner, 1, 3);
        grid.add(posLabel, 0, 4); grid.add(posBox, 1, 4);

        HBox actionBtnRow = new HBox(8);
        Button btnScheduleEntities = new Button();
        btnScheduleEntities.textProperty().bind(i18n.createStringBinding("god.btn.schedule_entities"));
        btnScheduleEntities.getStyleClass().add("btn-primary");
        btnScheduleEntities.setOnAction(e -> scheduleEntitiesEvent());

        Button btnLiveEntities = new Button();
        btnLiveEntities.textProperty().bind(i18n.createStringBinding("god.btn.live_entities"));
        btnLiveEntities.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnLiveEntities.setOnAction(e -> executeEntitiesLive());

        actionBtnRow.getChildren().addAll(btnScheduleEntities, btnLiveEntities);

        box.getChildren().addAll(grid, actionBtnRow);
        return box;
    }

    /**
     * Sub-block 3: Resources & Biomass with Taxonomy for Ants, Bees, Wasps, and Termites
     */
    private Node createResourcesSubBlockNode() {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        VBox box = new VBox(10);
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);

        Label lblResType = new Label();
        lblResType.textProperty().bind(i18n.createStringBinding("god.res.type"));
        resourceTypeSelect = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(
                i18n.get("god.res.type.surface_food"),
                i18n.get("god.res.type.water"),
                i18n.get("god.res.type.free_food")
        ));
        resourceTypeSelect.getSelectionModel().selectFirst();
        resourceTypeSelect.setPrefWidth(220);

        Label lblNature = new Label();
        lblNature.textProperty().bind(i18n.createStringBinding("god.res.nature"));
        foodNatureSelect = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(
                "Foliar & Plant Biomass (Atta / Leafcutter)",
                "Fungus Mycelium Termitomyces (Termites)",
                "Honeydew & Liquid Sugars (Lasius & Formica)",
                "Humus, Cellulose & Wood Fibers (Termites)",
                "Lignin & Dead Wood (Reticulitermes)",
                "Nectar & Floral Pollen (Apis & Bumblebees)",
                "Proteins & Prey Animals (Solenopsis & Wasps)",
                "Royal Jelly & Larval Brood Food (Nurses / Queens)",
                "Seeds & Grain Biomass (Messor)"
        ));
        foodNatureSelect.getSelectionModel().selectFirst();
        foodNatureSelect.setPrefWidth(330);

        Label foodLabel = new Label();
        foodLabel.textProperty().bind(i18n.createStringBinding("god.res.qty"));
        foodSlider = new Slider(10, 1000, 100);
        foodSlider.setPrefWidth(180);
        Label foodValue = new Label("100 u");
        foodValue.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");
        foodSlider.valueProperty().addListener((o,a,b) -> foodValue.setText(String.format("%.0f u", b.doubleValue())));

        grid.add(lblResType, 0, 0); grid.add(resourceTypeSelect, 1, 0);
        grid.add(lblNature, 0, 1); grid.add(foodNatureSelect, 1, 1);
        grid.add(foodLabel, 0, 2); grid.add(foodSlider, 1, 2); grid.add(foodValue, 2, 2);

        HBox actionBtnRow = new HBox(8);
        Button btnScheduleRes = new Button();
        btnScheduleRes.textProperty().bind(i18n.createStringBinding("god.btn.schedule_res"));
        btnScheduleRes.getStyleClass().add("btn-primary");
        btnScheduleRes.setOnAction(e -> scheduleResourceEvent());

        Button btnLiveRes = new Button();
        btnLiveRes.textProperty().bind(i18n.createStringBinding("god.btn.live_res"));
        btnLiveRes.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnLiveRes.setOnAction(e -> executeResourceLive());

        actionBtnRow.getChildren().addAll(btnScheduleRes, btnLiveRes);

        box.getChildren().addAll(grid, actionBtnRow);
        return box;
    }

    /**
     * Sub-block 4: Environmental Disasters with Magnitude & Multi-Week Duration
     */
    private Node createDisastersSubBlockNode() {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        VBox box = new VBox(10);
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);

        Label typeLabel = new Label();
        typeLabel.textProperty().bind(i18n.createStringBinding("god.disasters.type"));
        disasterSelect = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(
                "Cold Frost & Low Temperature",
                "Drought & Heat Wave",
                "Epidemic & Cordyceps Parasite",
                "Flood & Heavy Rain",
                "Forest Fire",
                "Soil Contamination & Toxins"
        ));
        disasterSelect.getSelectionModel().selectFirst();
        disasterSelect.setPrefWidth(220);

        Label intLabel = new Label();
        intLabel.textProperty().bind(i18n.createStringBinding("god.disasters.magnitude"));
        intensitySlider = new Slider(0.1, 1.0, 0.5);
        intensitySlider.setPrefWidth(180);
        Label intValue = new Label("Moyenne (0.5)");
        intValue.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
        intensitySlider.valueProperty().addListener((o,a,b) -> {
            double v = b.doubleValue();
            intValue.setText(v < 0.3 ? "Faible (" + String.format("%.1f", v) + ")" : v < 0.7 ? "Moyenne (" + String.format("%.1f", v) + ")" : "Catastrophique (" + String.format("%.1f", v) + ")");
        });

        Label durLabel = new Label();
        durLabel.textProperty().bind(i18n.createStringBinding("god.disasters.duration"));
        durationSlider = new Slider(5, 43200, 1440);
        durationSlider.setPrefWidth(180);
        durationValLabel = new Label("24h (1 Jour)");
        durationValLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");
        durationSlider.valueProperty().addListener((o, a, b) -> {
            int mins = b.intValue();
            if (mins < 60) {
                durationValLabel.setText(mins + " min");
            } else if (mins < 1440) {
                int hrs = mins / 60;
                int remMins = mins % 60;
                durationValLabel.setText(hrs + "h" + (remMins > 0 ? " " + remMins + "m" : ""));
            } else {
                int days = mins / 1440;
                int remHrs = (mins % 1440) / 60;
                if (days >= 7) {
                    int wks = days / 7;
                    durationValLabel.setText(String.format("%d Jours (%d Semaine%s)", days, wks, wks > 1 ? "s" : ""));
                } else {
                    durationValLabel.setText(String.format("%d Jours %s", days, remHrs > 0 ? remHrs + "h" : ""));
                }
            }
        });

        grid.add(typeLabel, 0, 0); grid.add(disasterSelect, 1, 0);
        grid.add(intLabel, 0, 1); grid.add(intensitySlider, 1, 1); grid.add(intValue, 2, 1);
        grid.add(durLabel, 0, 2); grid.add(durationSlider, 1, 2); grid.add(durationValLabel, 2, 2);

        HBox actionBtnRow = new HBox(8);
        Button btnScheduleDisaster = new Button();
        btnScheduleDisaster.textProperty().bind(i18n.createStringBinding("god.btn.schedule_disaster"));
        btnScheduleDisaster.getStyleClass().add("btn-primary");
        btnScheduleDisaster.setOnAction(e -> scheduleDisasterEvent());

        Button btnLiveDisaster = new Button();
        btnLiveDisaster.textProperty().bind(i18n.createStringBinding("god.btn.live_disaster"));
        btnLiveDisaster.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnLiveDisaster.setOnAction(e -> executeDisasterLive());

        Button btnStopDisasters = new Button();
        btnStopDisasters.textProperty().bind(i18n.createStringBinding("god.disasters.btn_stop"));
        btnStopDisasters.getStyleClass().add("btn-danger");
        btnStopDisasters.setOnAction(e -> {
            log("🛑 Arrêt forcé de toutes les catastrophes en cours.");
            if (callback != null) callback.stopDisasters();
        });

        actionBtnRow.getChildren().addAll(btnScheduleDisaster, btnLiveDisaster, btnStopDisasters);

        box.getChildren().addAll(grid, actionBtnRow);
        return box;
    }

    /**
     * Sub-block 5: Abiotic Core Physical Drivers (Temperature, Humidity, Wind, Solar Radiation)
     */
    private Node createParametersSubBlockNode() {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        VBox box = new VBox(10);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);

        Label lblTemp = new Label();
        lblTemp.textProperty().bind(i18n.createStringBinding("god.abiotic.temp"));
        tempSlider = new Slider(-10.0, 50.0, 22.0);
        tempSlider.setPrefWidth(180);
        tempValLabel = new Label("22.0 °C");
        tempValLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");
        tempSlider.valueProperty().addListener((o, a, b) -> {
            tempValLabel.setText(String.format("%.1f °C", b.doubleValue()));
            updateDerivedPhysicalOutputs();
        });

        Label lblHumidity = new Label();
        lblHumidity.textProperty().bind(i18n.createStringBinding("god.abiotic.humidity"));
        humiditySlider = new Slider(10.0, 100.0, 65.0);
        humiditySlider.setPrefWidth(180);
        humidityValLabel = new Label("65.0 %");
        humidityValLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");
        humiditySlider.valueProperty().addListener((o, a, b) -> {
            humidityValLabel.setText(String.format("%.1f %%", b.doubleValue()));
            updateDerivedPhysicalOutputs();
        });

        Label lblWind = new Label();
        lblWind.textProperty().bind(i18n.createStringBinding("god.abiotic.wind"));
        windSlider = new Slider(0.0, 25.0, 1.5);
        windSlider.setPrefWidth(180);
        windValLabel = new Label("1.5 m/s");
        windValLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");
        windSlider.valueProperty().addListener((o, a, b) -> {
            windValLabel.setText(String.format("%.1f m/s", b.doubleValue()));
            updateDerivedPhysicalOutputs();
        });

        Label lblSolar = new Label();
        lblSolar.textProperty().bind(i18n.createStringBinding("god.abiotic.solar"));
        solarSlider = new Slider(0.0, 1200.0, 450.0);
        solarSlider.setPrefWidth(180);
        solarValLabel = new Label("450 W/m²");
        solarValLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");
        solarSlider.valueProperty().addListener((o, a, b) -> {
            solarValLabel.setText(String.format("%.0f W/m²", b.doubleValue()));
            updateDerivedPhysicalOutputs();
        });

        grid.add(lblTemp, 0, 0); grid.add(tempSlider, 1, 0); grid.add(tempValLabel, 2, 0);
        grid.add(lblHumidity, 0, 1); grid.add(humiditySlider, 1, 1); grid.add(humidityValLabel, 2, 1);
        grid.add(lblWind, 0, 2); grid.add(windSlider, 1, 2); grid.add(windValLabel, 2, 2);
        grid.add(lblSolar, 0, 3); grid.add(solarSlider, 1, 3); grid.add(solarValLabel, 2, 3);

        VBox derivedBox = new VBox(4);
        derivedBox.getStyleClass().add("header-banner");

        Label lblDerivedHeader = new Label();
        lblDerivedHeader.textProperty().bind(i18n.createStringBinding("god.derived.header"));
        lblDerivedHeader.getStyleClass().add("accent-title");

        derivedPheroLabel = new Label();
        derivedPheroLabel.setStyle("-fx-font-size: 11px;");

        derivedPrimaryProductivityLabel = new Label();
        derivedPrimaryProductivityLabel.setStyle("-fx-font-size: 11px;");

        derivedOvipositionLabel = new Label();
        derivedOvipositionLabel.setStyle("-fx-font-size: 11px;");

        derivedBox.getChildren().addAll(lblDerivedHeader, derivedPheroLabel, derivedPrimaryProductivityLabel, derivedOvipositionLabel);

        updateDerivedPhysicalOutputs();

        HBox actionBtnRow = new HBox(8);
        Button btnScheduleAbiotic = new Button();
        btnScheduleAbiotic.textProperty().bind(i18n.createStringBinding("god.btn.schedule_abiotic"));
        btnScheduleAbiotic.getStyleClass().add("btn-primary");
        btnScheduleAbiotic.setOnAction(e -> scheduleAbioticEvent());

        Button btnLiveAbiotic = new Button();
        btnLiveAbiotic.textProperty().bind(i18n.createStringBinding("god.btn.live_abiotic"));
        btnLiveAbiotic.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnLiveAbiotic.setOnAction(e -> executeAbioticLive());

        actionBtnRow.getChildren().addAll(btnScheduleAbiotic, btnLiveAbiotic);

        box.getChildren().addAll(grid, derivedBox, actionBtnRow);
        return box;
    }

    private void updateDerivedPhysicalOutputs() {
        if (tempSlider == null) return;

        double temp = tempSlider.getValue();
        double hum = humiditySlider.getValue();
        double wind = windSlider.getValue();
        double solar = solarSlider.getValue();

        double calcEvapRate = 0.02 + (temp / 100.0) * (1.0 + wind / 5.0) * (1.0 - hum / 120.0);
        calcEvapRate = Math.max(0.005, Math.min(0.30, calcEvapRate));

        double calcProdRate = (solar / 1000.0) * (hum / 100.0) * (temp > 5 && temp < 40 ? 1.0 : 0.2);
        calcProdRate = Math.max(0.0, Math.min(1.5, calcProdRate));

        double calcOviposRate = temp >= 15 && temp <= 32 ? 1.0 + (temp - 22.0) * 0.05 : Math.max(0.1, 1.0 - Math.abs(temp - 22.0) * 0.08);

        derivedPheroLabel.setText(String.format("• Evaporation Phéromonale Dérivée : %.2f %% / s (T°=%.1f°C, H=%.0f%%, Vent=%.1fm/s)", calcEvapRate * 100.0, temp, hum, wind));
        derivedPrimaryProductivityLabel.setText(String.format("• Photosynthèse & Biomasse Primaire : %.2f u / s (Radiance=%.0fW/m²)", calcProdRate, solar));
        derivedOvipositionLabel.setText(String.format("• Coefficient Thermique de Ponte Reine : %.2fx (Optimum 22-28°C)", calcOviposRate));
    }

    // ── ATOMIC SCHEDULING METHODS ──────────────────────────────────────────────

    private void syncTimePickersWithCurrentTick(int offsetSeconds) {
        long targetSec = (long) (currentSimulationTick * simulationStepSec) + offsetSeconds;
        int d = (int) (targetSec / 86400L) + 1;
        long rem = targetSec % 86400L;
        int h = (int) (rem / 3600L);
        rem %= 3600L;
        int m = (int) (rem / 60L);
        int s = (int) (rem % 60L);

        spDay.getValueFactory().setValue(Math.max(1, d));
        spHour.getValueFactory().setValue(h);
        spMin.getValueFactory().setValue(m);
        spSec.getValueFactory().setValue(s);
    }

    public void syncCurrentSimulationTime(long currentTick, double stepSec) {
        this.currentSimulationTick = currentTick;
        this.simulationStepSec = stepSec > 0 ? stepSec : 0.016666666666666666;
        javafx.application.Platform.runLater(() -> syncTimePickersWithCurrentTick(10));
    }

    private ScheduledEvent createBaseEvent(Category category, String eventType, String desc) {
        long tSec = calculateTargetSeconds();
        long targetTick = Math.max(1, Math.round(tSec / simulationStepSec));
        String timeFormatted = String.format("J%d %02d:%02d:%02d", spDay.getValue(), spHour.getValue(), spMin.getValue(), spSec.getValue());
        String colTarget = eventColonySelect.getValue();
        return new ScheduledEvent(targetTick, timeFormatted, category, eventType, colTarget, desc);
    }

    private void scheduleEntitiesEvent() {
        String colTarget = colonySelect.getValue();
        String actionStr = entityActionSelect.getValue();
        String caste = casteSelect.getValue();
        int count = antCountSpinner.getValue();
        float x = Float.parseFloat(posXField.getText());
        float y = Float.parseFloat(posYField.getText());
        float z = Float.parseFloat(posZField.getText());

        ScheduledEvent ev = createBaseEvent(Category.ENTITIES, actionStr,
                String.format("%s %d x %s à (%.1f, %.1f, %.1f)", actionStr, count, caste, x, y, z));
        ev.colonyTarget = colTarget;
        ev.caste = caste;
        ev.count = count;
        ev.posX = x; ev.posY = y; ev.posZ = z;
        ev.entityAction = actionStr.contains("Injection") || actionStr.contains("Apparition") ? "SPAWN" :
                          actionStr.contains("Extinction") ? "EXTINCT" : "KILL";

        scheduledEventsList.add(ev);
        log(String.format("⏱️ Événement d'Entités programmé pour %s : %s", ev.timeFormatted, ev.description));
    }

    private void executeEntitiesLive() {
        String colTarget = colonySelect.getValue();
        String actionStr = entityActionSelect.getValue();
        String caste = casteSelect.getValue();
        int count = antCountSpinner.getValue();
        float x = Float.parseFloat(posXField.getText());
        float y = Float.parseFloat(posYField.getText());
        float z = Float.parseFloat(posZField.getText());

        if (callback != null) {
            if (actionStr.contains("Élimination") || actionStr.contains("Extinction")) {
                callback.killAnts(colTarget, caste, actionStr.contains("Extinction") ? 99999 : count);
            } else {
                callback.spawnAnts(colTarget, caste, count, x, y, z);
            }
        }
        log(String.format("⚡ Exécution directe d'Entités : %s %d %s [%s]", actionStr, count, caste, colTarget));
    }

    private void scheduleResourceEvent() {
        String resType = resourceTypeSelect.getValue();
        String foodNat = foodNatureSelect.getValue();
        float qty = (float) foodSlider.getValue();
        float x = Float.parseFloat(posXField.getText());
        float y = Float.parseFloat(posYField.getText());
        float z = Float.parseFloat(posZField.getText());

        ScheduledEvent ev = createBaseEvent(Category.RESOURCE, resType,
                String.format("Apport %.0f u (%s) - %s à (%.1f, %.1f, %.1f)", qty, resType, foodNat, x, y, z));
        ev.resourceType = resType;
        ev.foodNature = foodNat;
        ev.amount = qty;
        ev.posX = x; ev.posY = y; ev.posZ = z;

        scheduledEventsList.add(ev);
        log(String.format("⏱️ Événement Ressource programmé pour %s : %s", ev.timeFormatted, ev.description));
    }

    private void executeResourceLive() {
        String resType = resourceTypeSelect.getValue();
        float qty = (float) foodSlider.getValue();
        float x = Float.parseFloat(posXField.getText());
        float y = Float.parseFloat(posYField.getText());
        float z = Float.parseFloat(posZField.getText());

        if (callback != null) {
            callback.spawnFood(x, y, z, qty);
        }
        log(String.format("⚡ Apport de Ressource en direct : %.0f u [%s] à (%.1f, %.1f, %.1f)", qty, resType, x, y, z));
    }

    private void scheduleDisasterEvent() {
        String disasterType = disasterSelect.getValue();
        float intensity = (float) intensitySlider.getValue();
        int durMins = (int) durationSlider.getValue();

        ScheduledEvent ev = createBaseEvent(Category.DISASTER, disasterType,
                String.format("Catastrophe %s (Intensité: %.1f, Durée: %d min)", disasterType, intensity, durMins));
        ev.disasterType = disasterType;
        ev.intensity = intensity;
        ev.durationMinutes = durMins;

        scheduledEventsList.add(ev);
        log(String.format("⏱️ Événement Catastrophe programmé pour %s : %s", ev.timeFormatted, ev.description));
    }

    private void executeDisasterLive() {
        String disasterType = disasterSelect.getValue();
        float intensity = (float) intensitySlider.getValue();

        if (callback != null) {
            callback.triggerDisaster(disasterType, intensity);
        }
        log(String.format("⚡ Déclenchement de Catastrophe en direct : %s (Intensité: %.1f)", disasterType, intensity));
    }

    private void scheduleAbioticEvent() {
        float temp = (float) tempSlider.getValue();
        float hum = (float) humiditySlider.getValue();
        float wind = (float) windSlider.getValue();
        float solar = (float) solarSlider.getValue();

        ScheduledEvent ev = createBaseEvent(Category.ABIOTIC, "Variation Climat",
                String.format("Climat : T=%.1f°C, H=%.0f%%, Vent=%.1fm/s, Solaire=%.0fW/m²", temp, hum, wind, solar));
        ev.tempCelsius = temp;
        ev.humidityPercent = hum;
        ev.windMetersPerSec = wind;
        ev.solarWattsPerM2 = solar;

        scheduledEventsList.add(ev);
        log(String.format("⏱️ Événement Abiotique programmé pour %s : %s", ev.timeFormatted, ev.description));
    }

    private void executeAbioticLive() {
        float temp = (float) tempSlider.getValue();
        float hum = (float) humiditySlider.getValue();
        float wind = (float) windSlider.getValue();
        float solar = (float) solarSlider.getValue();

        if (callback != null) {
            callback.modifyParameter("temperatureCelsius", temp);
            callback.modifyParameter("humidityPercent", hum);
            callback.modifyParameter("windSpeed", wind);
            callback.modifyParameter("solarRadiation", solar);
        }
        log(String.format("⚡ Application de Climat en direct : T=%.1f°C, H=%.0f%%, Vent=%.1fm/s, Solaire=%.0fW/m²", temp, hum, wind, solar));
    }

    private void executeScheduledEvent(ScheduledEvent ev) {
        String colStr = (ev.colonyTarget != null && !ev.colonyTarget.contains("Global")) ? ev.colonyTarget : "Colonie Primaire";
        log(String.format("⚡ Exécution de l'événement programmé (%s) [%s] : %s", ev.timeFormatted, ev.category.label, ev.description));
        if (callback != null) {
            switch (ev.category) {
                case ENTITIES -> {
                    if ("KILL".equals(ev.entityAction) || "EXTINCT".equals(ev.entityAction)) {
                        callback.killAnts(colStr, ev.caste, "EXTINCT".equals(ev.entityAction) ? 99999 : ev.count);
                    } else {
                        callback.spawnAnts(colStr, ev.caste, ev.count, ev.posX, ev.posY, ev.posZ);
                    }
                }
                case RESOURCE -> callback.spawnFood(ev.posX, ev.posY, ev.posZ, ev.amount);
                case DISASTER -> callback.triggerDisaster(ev.disasterType, ev.intensity);
                case ABIOTIC -> {
                    callback.modifyParameter("temperatureCelsius", ev.tempCelsius);
                    callback.modifyParameter("humidityPercent", ev.humidityPercent);
                    callback.modifyParameter("windSpeed", ev.windMetersPerSec);
                    callback.modifyParameter("solarRadiation", ev.solarWattsPerM2);
                }
            }
        }
    }

    /**
     * Processes events for the given simulation tick.
     * Automatically handles simulation time rewind (seeking/rewinding back resets executed events).
     */
    public void processScheduledEvents(long currentTick) {
        this.currentSimulationTick = currentTick;
        boolean needsRefresh = false;
        for (ScheduledEvent ev : scheduledEventsList) {
            if (ev.executed && currentTick < ev.targetTick) {
                ev.executed = false;
                needsRefresh = true;
            }
            else if (!ev.executed && !ev.paused && currentTick >= ev.targetTick) {
                ev.executed = true;
                executeScheduledEvent(ev);
                needsRefresh = true;
            }
        }
        if (needsRefresh && scheduledEventsListView != null) {
            javafx.application.Platform.runLater(() -> scheduledEventsListView.refresh());
        }
    }

    private VBox createLogSection() {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        VBox box = new VBox(5);
        box.setPadding(new Insets(8, 0, 0, 0));

        Label logLabel = new Label();
        logLabel.textProperty().bind(i18n.createStringBinding("god.log.title"));
        logLabel.getStyleClass().add("sub-title");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(90);
        logArea.getStyleClass().add("log-text-area");

        box.getChildren().addAll(logLabel, logArea);
        return box;
    }

    public void setSimulationRunning(boolean running) {
        if (simStateWarningLabel != null) {
            org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
            simStateWarningLabel.textProperty().unbind();
            if (!running) {
                simStateWarningLabel.setText(i18n.get("god.warn.paused"));
                simStateWarningLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-color: rgba(245,158,11,0.15); -fx-padding: 6 10; -fx-background-radius: 4;");
            } else {
                simStateWarningLabel.setText(i18n.get("god.warn.running"));
                simStateWarningLabel.setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-color: rgba(74,222,128,0.15); -fx-padding: 6 10; -fx-background-radius: 4;");
            }
        }
    }

    private void log(String message) {
        String time = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        if (logArea != null) {
            logArea.appendText("[" + time + "] " + message + "\n");
        }
    }

    public void setCallback(InterventionCallback callback) {
        this.callback = callback;
    }

    public void updateAvailableColonies(java.util.List<String> activeColonyNames) {
        if (activeColonyNames == null || activeColonyNames.isEmpty()) return;

        if (colonySelect != null) {
            String selSpawn = colonySelect.getValue();
            colonySelect.getItems().setAll(activeColonyNames);
            if (selSpawn != null && colonySelect.getItems().contains(selSpawn)) {
                colonySelect.getSelectionModel().select(selSpawn);
            } else {
                colonySelect.getSelectionModel().selectFirst();
            }
        }

        if (eventColonySelect != null) {
            java.util.List<String> items = new java.util.ArrayList<>();
            items.add("Toutes les Colonies (Global)");
            items.addAll(activeColonyNames);
            String selEv = eventColonySelect.getValue();
            eventColonySelect.getItems().setAll(items);
            if (selEv != null && eventColonySelect.getItems().contains(selEv)) {
                eventColonySelect.getSelectionModel().select(selEv);
            } else {
                eventColonySelect.getSelectionModel().selectFirst();
            }
        }

        updateCastesForSelectedColony(colonySelect != null ? colonySelect.getValue() : null);
    }

    private void updateCastesForSelectedColony(String colonyName) {
        if (casteSelect == null) return;

        casteSelect.getItems().clear();

        if (colonyName != null && (colonyName.contains("Atta") || colonyName.contains("Leafcutter"))) {
            casteSelect.getItems().addAll("Giant Queen", "Major Soldier (Guard)", "Media Worker (Cutter)", "Minim Worker (Nurse)");
        } else if (colonyName != null && (colonyName.contains("Apis") || colonyName.contains("Abeille"))) {
            casteSelect.getItems().addAll("Drone (Male)", "Forager Worker", "Queen Bee");
        } else if (colonyName != null && (colonyName.contains("Termite") || colonyName.contains("Reticulitermes"))) {
            casteSelect.getItems().addAll("Mandibulate Soldier", "Physogastric Queen", "Reproductive King", "Termite Worker");
        } else if (colonyName != null && (colonyName.contains("Vespula") || colonyName.contains("Guêpe"))) {
            casteSelect.getItems().addAll("Foundress (Queen)", "Hunter Worker");
        } else if (colonyName != null && (colonyName.contains("Solenopsis") || colonyName.contains("Feu"))) {
            casteSelect.getItems().addAll("Major Worker / Soldier", "Minor Worker", "Queen");
        } else {
            casteSelect.getItems().addAll("Foundress Queen", "Generalist Worker", "Guardian Soldier", "Nurse", "Scout");
        }
        casteSelect.getSelectionModel().selectFirst();
    }
}
