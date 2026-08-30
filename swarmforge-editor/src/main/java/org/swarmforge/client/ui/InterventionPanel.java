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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Intervention Panel - God Mode controls for runtime simulation manipulation.
 * Features an overarching Scheduled Event Queue system with atomic event types,
 * multi-selection, chronological sorting, calendar time formatting,
 * spatial sliders (centered by default on terrain surface), relative vs absolute
 * climate delta modifiers with duration, brood management (eggs, larvae, pupae),
 * and direct manipulation of abiotic physical drivers, pheromones, invasions, and genetic boosts.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class InterventionPanel extends BorderPane {

    public enum Category {
        ENTITIES(Feather.USERS, "Entities & Brood", "#3b82f6"),
        RESOURCE(Feather.PACKAGE, "Resources", "#10b981"),
        DISASTER(Feather.ALERT_TRIANGLE, "Disaster", "#ef4444"),
        ABIOTIC(Feather.SUN, "Climate & Abiotic", "#f59e0b"),
        PHEROMONE(Feather.WIND, "Pheromones", "#8b5cf6"),
        INVASION(Feather.SHIELD_OFF, "Invasions", "#dc2626"),
        MUTATION(Feather.ACTIVITY, "Genetics & Mutation", "#ec4899");

        public final Feather icon;
        public final String label;
        public final String color;

        Category(Feather icon, String label, String color) {
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
        public String entityAction = "SPAWN"; // SPAWN, KILL, EXTINCT, BROOD_KILL, BROOD_SPAWN
        public String caste = "Worker";
        public int count = 10;
        // Default position: CENTER of map (32, 32) at terrain surface level (1.0)
        public float posX = 32.0f, posY = 32.0f, posZ = 1.0f;

        public String resourceType = "Surface Food";
        public String foodNature = "Seeds & Grains";
        public float amount = 100f;

        public String disasterType = "Flood & Heavy Rain";
        public float intensity = 0.5f;
        public int durationMinutes = 60;

        // Abiotic Climate Parameters (Relative Offset vs Absolute Target)
        public boolean isRelativeAbiotic = true;
        public float tempCelsius = 22.0f;
        public float humidityPercent = 65.0f;
        public float windMetersPerSec = 1.5f;
        public float solarWattsPerM2 = 450.0f;

        public float deltaTempCelsius = 5.0f;
        public float deltaHumidityPercent = 20.0f;
        public float deltaWindMetersPerSec = 2.0f;
        public float deltaSolarWattsPerM2 = 100.0f;

        public float radius = 5.0f;

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

        public ScheduledEvent copyWithOffset(long additionalTicks, double stepSec) {
            long newTarget = this.targetTick + additionalTicks;
            String newTime = formatTickToCalendarTime(newTarget, stepSec);
            ScheduledEvent clone = new ScheduledEvent(newTarget, newTime, this.category, this.eventType, this.colonyTarget, this.description);
            clone.entityAction = this.entityAction;
            clone.caste = this.caste;
            clone.count = this.count;
            clone.posX = this.posX; clone.posY = this.posY; clone.posZ = this.posZ;
            clone.resourceType = this.resourceType;
            clone.foodNature = this.foodNature;
            clone.amount = this.amount;
            clone.disasterType = this.disasterType;
            clone.intensity = this.intensity;
            clone.durationMinutes = this.durationMinutes;
            clone.isRelativeAbiotic = this.isRelativeAbiotic;
            clone.tempCelsius = this.tempCelsius;
            clone.humidityPercent = this.humidityPercent;
            clone.windMetersPerSec = this.windMetersPerSec;
            clone.solarWattsPerM2 = this.solarWattsPerM2;
            clone.deltaTempCelsius = this.deltaTempCelsius;
            clone.deltaHumidityPercent = this.deltaHumidityPercent;
            clone.deltaWindMetersPerSec = this.deltaWindMetersPerSec;
            clone.deltaSolarWattsPerM2 = this.deltaSolarWattsPerM2;
            clone.radius = this.radius;
            clone.executed = false;
            clone.paused = false;
            return clone;
        }
    }

    private Spinner<Integer> antCountSpinner;
    private ComboBox<String> casteSelect;
    private ComboBox<String> colonySelect;
    private ComboBox<String> entityActionSelect;

    // Spatial Sliders (X: Ouest-Est, Y: Sud-Nord, Z: Profondeur-Altitude)
    private Slider posXSlider, posYSlider, posZSlider;
    private Label posXValLabel, posYValLabel, posZValLabel;

    private ComboBox<String> disasterSelect;
    private Slider intensitySlider;
    private Slider durationSlider;
    private Label durationValLabel;

    private ComboBox<String> eventColonySelect;
    private Spinner<Integer> spDay, spHour, spMin, spSec;
    private Label lblTargetTickSummary;

    // Abiotic Controls: Relative Delta vs Absolute Target Mode + Duration
    private ComboBox<String> abioticModeSelect;
    private Slider abioticDurationSlider;
    private Label abioticDurationValLabel;

    private Slider tempSlider, deltaTempSlider;
    private Label tempValLabel, deltaTempValLabel;
    private Slider humiditySlider, deltaHumiditySlider;
    private Label humidityValLabel, deltaHumidityValLabel;
    private Slider windSlider, deltaWindSlider;
    private Label windValLabel, deltaWindValLabel;
    private Slider solarSlider, deltaSolarSlider;
    private Label solarValLabel, deltaSolarValLabel;

    private Label derivedPheroLabel;
    private Label derivedPrimaryProductivityLabel;
    private Label derivedOvipositionLabel;

    private Slider foodSlider;
    private ComboBox<String> resourceTypeSelect;
    private ComboBox<String> foodNatureSelect;

    // Pheromones Controls with Intensity & Duration
    private ComboBox<String> pheromoneTypeSelect;
    private Slider pheroRadiusSlider, pheroIntensitySlider, pheroDurationSlider;
    private Label pheroRadiusValLabel, pheroIntensityValLabel, pheroDurationValLabel;

    // Invasions Controls with Count/Intensity & Duration
    private ComboBox<String> invasionTypeSelect;
    private Spinner<Integer> invasionCountSpinner;
    private Slider invasionDurationSlider;
    private Label invasionDurationValLabel;

    // Mutation Controls with Intensity & Duration
    private ComboBox<String> mutationTypeSelect;
    private ComboBox<String> mutationCasteScopeSelect;
    private Slider mutationIntensitySlider, mutationDurationSlider;
    private Label mutationIntensityValLabel, mutationDurationValLabel;
    private Label mutationExplanationLabel;

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

        default void triggerPheromoneEvent(String type, float x, float y, float z, float intensity, float radius, float durationMinutes) {}
        default void triggerInvasionEvent(String type, float x, float y, float z, int count, float durationMinutes) {}
        default void applyGeneticBoost(String colonyId, String boostType, float intensity, float durationMinutes) {}
        default void modifyAbioticClimate(boolean relative, float temp, float hum, float wind, float solar, float durationMinutes) {}
        default void manageBrood(String colonyId, String broodType, String action, int count) {}
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

        simStateWarningLabel = new Label(i18n.get("god.warn.stopped"));
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

        // 2. OUTER MASTER CONTAINER: Event & Intervention Configuration Sub-blocks
        VBox subBlocksOuterContainer = new VBox(14);
        subBlocksOuterContainer.setPadding(new Insets(16));
        subBlocksOuterContainer.getStyleClass().add("sub-blocks-container");

        Label subBlocksHeader = new Label();
        subBlocksHeader.textProperty().bind(i18n.createStringBinding("god.block.header"));
        subBlocksHeader.getStyleClass().add("accent-title");
        subBlocksHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        subBlocksOuterContainer.getChildren().addAll(subBlocksHeader, new Separator());

        // Sub-block 1: Time Target & Colony Target
        subBlocksOuterContainer.getChildren().add(createCardSubBlock("god.block.time_colony", Feather.CLOCK, createTimeAndColonyConfigNode()));

        // Sub-block 2: Spatial 3D Positioning (Centered on Terrain Surface by Default)
        subBlocksOuterContainer.getChildren().add(createCardSubBlock("god.block.spatial", Feather.MAP_PIN, createSpatialPositionSubBlockNode()));

        // Sub-block 3: Entities, Castes & Brood (Apparition / Élimination / Couvain)
        subBlocksOuterContainer.getChildren().add(createCardSubBlock("god.block.entities_manage", Feather.USERS, createEntitiesSubBlockNode()));

        // Sub-block 4: Resources & Biomass (With Complete Social Insect Food Taxonomy)
        subBlocksOuterContainer.getChildren().add(createCardSubBlock("god.block.resources", Feather.BOX, createResourcesSubBlockNode()));

        // Sub-block 5: Disasters & Environmental Events (Magnitude & Multi-Week Duration)
        subBlocksOuterContainer.getChildren().add(createCardSubBlock("god.block.disasters", Feather.ZAP, createDisastersSubBlockNode()));

        // Sub-block 6: Abiotic Physical Drivers (Relative Delta +/- & Duration Mode)
        subBlocksOuterContainer.getChildren().add(createCardSubBlock("god.block.abiotic_delta", Feather.THERMOMETER, createParametersSubBlockNode()));

        // Sub-block 7: Pheromones & Behavioral Disruption (Intensity & Duration)
        subBlocksOuterContainer.getChildren().add(createCardSubBlock("god.block.pheromones", Feather.WIND, createPheromoneSubBlockNode()));

        // Sub-block 8: Apex Predators & Biological Invasions (Intensity & Duration)
        subBlocksOuterContainer.getChildren().add(createCardSubBlock("god.block.invasions", Feather.SHIELD_OFF, createInvasionSubBlockNode()));

        // Sub-block 9: Genetics, Metabolic Boosts & Mutagens (Intensity & Duration)
        subBlocksOuterContainer.getChildren().add(createCardSubBlock("god.block.mutations", Feather.ACTIVITY, createMutationSubBlockNode()));

        mainContent.getChildren().add(subBlocksOuterContainer);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        setCenter(scrollPane);
    }

    private VBox createCardSubBlock(String titleKey, Feather icon, Node contentNode) {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.getStyleClass().add("sub-card");

        FontIcon titleIconNode = new FontIcon(icon);
        titleIconNode.setIconSize(14);
        titleIconNode.setIconColor(javafx.scene.paint.Color.web("#38bdf8"));

        Label title = new Label("", titleIconNode);
        title.textProperty().bind(i18n.createStringBinding(titleKey));
        title.getStyleClass().add("accent-title");

        card.getChildren().addAll(title, new Separator(), contentNode);
        return card;
    }

    /**
     * Master Scheduled Event Queue Box with Multi-Selection, Chronological Sorting,
     * Repeat Event capability, and clean HH:mm:ss time formatting.
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
        scheduledEventsListView.setPrefHeight(180);
        scheduledEventsListView.getStyleClass().add("log-text-area");
        scheduledEventsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        scheduledEventsListView.setCellFactory(param -> new ListCell<ScheduledEvent>() {
            @Override
            protected void updateItem(ScheduledEvent ev, boolean empty) {
                super.updateItem(ev, empty);
                if (empty || ev == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    HBox cellBox = new HBox(8);
                    cellBox.setAlignment(Pos.CENTER_LEFT);
                    cellBox.setPadding(new Insets(4, 8, 4, 8));

                    FontIcon catIconNode = new FontIcon(ev.category.icon);
                    catIconNode.setIconSize(12);
                    catIconNode.setIconColor(javafx.scene.paint.Color.WHITE);

                    Label badgeCat = new Label(" " + ev.category.label, catIconNode);
                    badgeCat.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 3;", ev.category.color));

                    Label timeLabel = new Label("[" + ev.timeFormatted + "]");
                    timeLabel.getStyleClass().add("accent-title");
                    timeLabel.setStyle("-fx-font-size: 11px;");

                    Label descLabel = new Label(ev.eventType + (ev.colonyTarget != null ? " (" + ev.colonyTarget + ")" : "") + " : " + ev.description);
                    descLabel.setStyle("-fx-font-size: 11px;");
                    HBox.setHgrow(descLabel, Priority.ALWAYS);

                    FontIcon stIconNode = new FontIcon(ev.executed ? Feather.CHECK_CIRCLE : ev.paused ? Feather.PAUSE_CIRCLE : Feather.CLOCK);
                    stIconNode.setIconSize(11);
                    stIconNode.setIconColor(javafx.scene.paint.Color.WHITE);

                    String statusText = ev.executed ? " Executed" : ev.paused ? " Paused" : " Pending";
                    String statusBg = ev.executed ? "#16a34a" : ev.paused ? "#d97706" : "#0284c7";
                    Label statusBadge = new Label(statusText, stIconNode);
                    statusBadge.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 3;", statusBg));

                    cellBox.getChildren().addAll(badgeCat, timeLabel, descLabel, statusBadge);

                    if (isSelected()) {
                        setStyle("-fx-background-color: #334155; -fx-background-radius: 4;");
                        cellBox.setStyle("-fx-background-color: #334155; -fx-background-radius: 4;");
                    } else {
                        setStyle("-fx-background-color: transparent;");
                        cellBox.setStyle("-fx-background-color: transparent;");
                    }

                    if (ev.executed) {
                        cellBox.setOpacity(0.75);
                    } else {
                        cellBox.setOpacity(1.0);
                    }

                    setGraphic(cellBox);
                }
            }
        });

        // Queue Control Toolbar Buttons
        HBox btnToolbar = new HBox(8);
        btnToolbar.setAlignment(Pos.CENTER_LEFT);

        Button btnRepeatEv = new Button();
        btnRepeatEv.textProperty().bind(i18n.createStringBinding("god.queue.btn.repeat"));
        btnRepeatEv.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnRepeatEv.tooltipProperty().bind(i18n.createTooltipBinding("god.queue.repeat.tt"));
        btnRepeatEv.setOnAction(e -> repeatSelectedEvents(10));

        Button btnDeleteEv = new Button();
        btnDeleteEv.textProperty().bind(i18n.createStringBinding("god.queue.btn.delete"));
        btnDeleteEv.getStyleClass().add("btn-danger");
        btnDeleteEv.tooltipProperty().bind(i18n.createTooltipBinding("god.queue.delete.tt"));
        btnDeleteEv.setOnAction(e -> {
            List<ScheduledEvent> selected = new ArrayList<>(scheduledEventsListView.getSelectionModel().getSelectedItems());
            for (ScheduledEvent ev : selected) {
                if (ev != null && !ev.executed) {
                    scheduledEventsList.remove(ev);
                }
            }
            sortScheduledEvents();
        });

        Button btnClearAll = new Button();
        btnClearAll.textProperty().bind(i18n.createStringBinding("god.queue.btn.clear_all"));
        btnClearAll.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnClearAll.tooltipProperty().bind(i18n.createTooltipBinding("god.queue.clear_all.tt"));
        btnClearAll.setOnAction(e -> {
            scheduledEventsList.removeIf(ev -> !ev.executed);
            sortScheduledEvents();
        });

        btnToolbar.getChildren().addAll(btnRepeatEv, btnDeleteEv, btnClearAll);

        box.getChildren().addAll(lblQueueTitle, scheduledEventsListView, btnToolbar);
        return box;
    }

    private void sortScheduledEvents() {
        scheduledEventsList.sort(Comparator.comparingLong((ScheduledEvent ev) -> ev.targetTick));
    }

    private void repeatSelectedEvents(int offsetSeconds) {
        List<ScheduledEvent> selected = new ArrayList<>(scheduledEventsListView.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()) return;

        long offsetTicks = Math.max(1, Math.round(offsetSeconds / simulationStepSec));
        List<ScheduledEvent> newEvents = new ArrayList<>();

        for (ScheduledEvent ev : selected) {
            if (ev != null) {
                ScheduledEvent clone = ev.copyWithOffset(offsetTicks, simulationStepSec);
                newEvents.add(clone);
            }
        }

        scheduledEventsList.addAll(newEvents);
        sortScheduledEvents();

        scheduledEventsListView.getSelectionModel().clearSelection();
        for (ScheduledEvent clone : newEvents) {
            scheduledEventsListView.getSelectionModel().select(clone);
        }
        scrollToFirstUpcomingEvent();
    }

    /**
     * Sub-block 1: Calendar Time Input & Colony Target
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
        spDay.tooltipProperty().bind(i18n.createTooltipBinding("god.time.spinners.tt"));
        spHour = new Spinner<>(0, 23, 8); spHour.setEditable(true); spHour.setPrefWidth(60);
        spHour.tooltipProperty().bind(i18n.createTooltipBinding("god.time.spinners.tt"));
        spMin = new Spinner<>(0, 59, 0); spMin.setEditable(true); spMin.setPrefWidth(60);
        spMin.tooltipProperty().bind(i18n.createTooltipBinding("god.time.spinners.tt"));
        spSec = new Spinner<>(0, 59, 0); spSec.setEditable(true); spSec.setPrefWidth(60);
        spSec.tooltipProperty().bind(i18n.createTooltipBinding("god.time.spinners.tt"));

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
        eventColonySelect.tooltipProperty().bind(i18n.createTooltipBinding("god.time.colony.tt"));

        Button btnSyncTime = new Button();
        btnSyncTime.textProperty().bind(i18n.createStringBinding("god.btn.sync_time"));
        btnSyncTime.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand;");
        btnSyncTime.setGraphic(new FontIcon(Feather.CLOCK));
        btnSyncTime.tooltipProperty().bind(i18n.createTooltipBinding("god.btn.sync_time.tt"));
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

        lblTargetTickSummary = new Label("🎯 Target Timestamp: Day 1 08:00:00 — Target Colony: All Colonies (Global)");
        lblTargetTickSummary.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px;");

        Runnable updateSummary = () -> {
            lblTargetTickSummary.setText(String.format("🎯 Target Timestamp: Day %d %02d:%02d:%02d — Colony: %s",
                    spDay.getValue(), spHour.getValue(), spMin.getValue(), spSec.getValue(), eventColonySelect.getValue()));
        };

        spDay.valueProperty().addListener((o, a, b) -> updateSummary.run());
        spHour.valueProperty().addListener((o, a, b) -> updateSummary.run());
        spMin.valueProperty().addListener((o, a, b) -> updateSummary.run());
        spSec.valueProperty().addListener((o, a, b) -> updateSummary.run());
        eventColonySelect.valueProperty().addListener((o, a, b) -> updateSummary.run());

        box.getChildren().addAll(timeRow, lblTargetTickSummary);
        return box;
    }

    // Dynamic Terrain Dimensions & Terrarium Reference for Spatial Sliders
    private double terrainWidthMeters = 64.0;
    private double terrainLengthMeters = 64.0;
    private double terrainDepthMeters = 5.0;
    private double terrainMaxAltitudeMeters = 25.0;
    private Label terrainDimensionsInfoLabel;
    private CheckBox atSurfaceCheckBox;
    private org.swarmforge.core.domain.Terrarium terrariumRef;

    public void setTerrarium(org.swarmforge.core.domain.Terrarium terrarium) {
        this.terrariumRef = terrarium;
        if (terrarium != null) {
            setTerrainDimensions(terrarium.getWidth(), terrarium.getHeight(), 5.0, 25.0);
        }
    }

    public float getCalculatedSurfaceZ(double posX, double posY) {
        if (atSurfaceCheckBox != null && !atSurfaceCheckBox.isSelected()) {
            return posZSlider != null ? (float) posZSlider.getValue() : 1.0f;
        }
        if (terrariumRef != null) {
            int cellX = Math.max(0, Math.min(terrariumRef.getWidth() - 1, (int) posX));
            int cellY = Math.max(0, Math.min(terrariumRef.getHeight() - 1, (int) posY));
            for (int z = terrariumRef.getDepth() - 1; z >= 0; z--) {
                var cell = terrariumRef.getCell(cellX, cellY, z);
                if (cell != null && cell.material() != org.swarmforge.core.domain.TerrariumCell.Material.AIR) {
                    return (float) (z + 1.0);
                }
            }
        }
        return 1.0f; // Default surface height
    }

    public void setTerrainDimensions(double widthMeters, double lengthMeters, double depthMeters, double maxAltitudeMeters) {
        this.terrainWidthMeters = Math.max(1.0, widthMeters);
        this.terrainLengthMeters = Math.max(1.0, lengthMeters);
        this.terrainDepthMeters = Math.max(0.1, depthMeters);
        this.terrainMaxAltitudeMeters = Math.max(1.0, maxAltitudeMeters);
        updateSpatialSliderRanges();
    }

    public void setTerrainSize(double sizeMeters) {
        setTerrainDimensions(sizeMeters, sizeMeters, 5.0, 25.0);
    }

    public void updateSpatialSliderRanges() {
        if (posXSlider == null || posYSlider == null || posZSlider == null) return;

        double centerX = terrainWidthMeters / 2.0;
        double centerY = terrainLengthMeters / 2.0;

        posXSlider.setMin(0.0);
        posXSlider.setMax(terrainWidthMeters);
        posXSlider.setMajorTickUnit(Math.max(1.0, terrainWidthMeters / 4.0));
        posXSlider.setValue(centerX);

        posYSlider.setMin(0.0);
        posYSlider.setMax(terrainLengthMeters);
        posYSlider.setMajorTickUnit(Math.max(1.0, terrainLengthMeters / 4.0));
        posYSlider.setValue(centerY);

        posZSlider.setMin(-terrainDepthMeters);
        posZSlider.setMax(terrainMaxAltitudeMeters);

        if (terrainDimensionsInfoLabel != null) {
            terrainDimensionsInfoLabel.setText(String.format("🗺️ Active Terrain Coverage: %.1f m × %.1f m (Max Depth: -%.1f m | Max Alt: %.1f m)",
                    terrainWidthMeters, terrainLengthMeters, terrainDepthMeters, terrainMaxAltitudeMeters));
        }
    }

    /**
     * Sub-block 2: Spatial 3D Positioning (Sliders X, Y, Z, Dynamic Terrain Size Centered)
     */
    private Node createSpatialPositionSubBlockNode() {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        VBox box = new VBox(10);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);

        terrainDimensionsInfoLabel = new Label(String.format("🗺️ Active Terrain Coverage: %.1f m × %.1f m (Max Depth: -%.1f m | Max Alt: %.1f m)",
                terrainWidthMeters, terrainLengthMeters, terrainDepthMeters, terrainMaxAltitudeMeters));
        terrainDimensionsInfoLabel.setStyle("-fx-text-fill: #0ea5e9; -fx-font-size: 11px; -fx-font-weight: bold;");

        // X Slider (West -> East)
        Label lblX = new Label();
        lblX.textProperty().bind(i18n.createStringBinding("god.spatial.x.label"));
        lblX.setStyle("-fx-font-weight: bold;");
        lblX.tooltipProperty().bind(i18n.createTooltipBinding("god.spatial.x.tt"));
        
        double centerX = terrainWidthMeters / 2.0;
        posXSlider = new Slider(0.0, terrainWidthMeters, centerX);
        posXSlider.setPrefWidth(260);
        posXSlider.setMajorTickUnit(Math.max(1.0, terrainWidthMeters / 4.0));
        posXSlider.setMinorTickCount(3);
        posXSlider.setSnapToTicks(false);
        posXSlider.tooltipProperty().bind(i18n.createTooltipBinding("god.spatial.x.slider.tt"));
        posXValLabel = new Label();
        posXValLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");
        
        Runnable updateXText = () -> {
            double v = posXSlider.getValue();
            double c = terrainWidthMeters / 2.0;
            double pct = Math.min(100.0, Math.max(0.0, (v / terrainWidthMeters) * 100.0));
            String note = (Math.abs(v - c) < (terrainWidthMeters * 0.05)) ? " (Center)" :
                          (v < terrainWidthMeters * 0.15) ? " (Far West)" :
                          (v > terrainWidthMeters * 0.85) ? " (Far East)" : "";
            posXValLabel.setText(String.format("%.1f m / %.1f m (%.0f%%%s)", v, terrainWidthMeters, pct, note));
        };
        posXSlider.valueProperty().addListener((o, a, b) -> updateXText.run());
        updateXText.run();

        // Y Slider (South -> North)
        Label lblY = new Label();
        lblY.textProperty().bind(i18n.createStringBinding("god.spatial.y.label"));
        lblY.setStyle("-fx-font-weight: bold;");
        lblY.tooltipProperty().bind(i18n.createTooltipBinding("god.spatial.y.tt"));

        double centerY = terrainLengthMeters / 2.0;
        posYSlider = new Slider(0.0, terrainLengthMeters, centerY);
        posYSlider.setPrefWidth(260);
        posYSlider.setMajorTickUnit(Math.max(1.0, terrainLengthMeters / 4.0));
        posYSlider.setMinorTickCount(3);
        posYSlider.setSnapToTicks(false);
        posYSlider.tooltipProperty().bind(i18n.createTooltipBinding("god.spatial.y.slider.tt"));
        posYValLabel = new Label();
        posYValLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");

        Runnable updateYText = () -> {
            double v = posYSlider.getValue();
            double c = terrainLengthMeters / 2.0;
            double pct = Math.min(100.0, Math.max(0.0, (v / terrainLengthMeters) * 100.0));
            String note = (Math.abs(v - c) < (terrainLengthMeters * 0.05)) ? " (Center)" :
                          (v < terrainLengthMeters * 0.15) ? " (Far South)" :
                          (v > terrainLengthMeters * 0.85) ? " (Far North)" : "";
            posYValLabel.setText(String.format("%.1f m / %.1f m (%.0f%%%s)", v, terrainLengthMeters, pct, note));
        };
        posYSlider.valueProperty().addListener((o, a, b) -> updateYText.run());
        updateYText.run();

        // Z Slider (Depth -> Surface -> Altitude)
        Label lblZ = new Label();
        lblZ.textProperty().bind(i18n.createStringBinding("god.spatial.z.label"));
        lblZ.setStyle("-fx-font-weight: bold;");
        lblZ.tooltipProperty().bind(i18n.createTooltipBinding("god.spatial.z.tt"));
        posZSlider = new Slider(-terrainDepthMeters, terrainMaxAltitudeMeters, 1.0);
        posZSlider.setPrefWidth(260);
        posZSlider.setMajorTickUnit(5.0);
        posZSlider.setMinorTickCount(4);
        posZSlider.setSnapToTicks(false);
        posZSlider.tooltipProperty().bind(i18n.createTooltipBinding("god.spatial.z.slider.tt"));
        posZValLabel = new Label();
        posZValLabel.setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold;");

        atSurfaceCheckBox = new CheckBox("🌿 À la surface du terrain / Surface Level (Z auto)");
        atSurfaceCheckBox.setSelected(true);
        atSurfaceCheckBox.setStyle("-fx-font-weight: bold; -fx-text-fill: #22c55e; -fx-cursor: hand;");
        atSurfaceCheckBox.tooltipProperty().bind(i18n.createTooltipBinding("god.spatial.atsurface.tt"));

        Runnable updateZText = () -> {
            boolean atSurf = atSurfaceCheckBox.isSelected();
            posZSlider.setDisable(atSurf);
            if (atSurf) {
                double x = posXSlider != null ? posXSlider.getValue() : 0;
                double y = posYSlider != null ? posYSlider.getValue() : 0;
                float surfZ = getCalculatedSurfaceZ(x, y);
                posZValLabel.setText(String.format(i18n.get("god.z_surface"), x, y, surfZ));
                posZValLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
            } else {
                double v = posZSlider.getValue();
                String desc = (v < 0.0) ? String.format("%.1f m (Subterranean / Max: -%.1f m)", v, terrainDepthMeters) :
                              (Math.abs(v - 1.0) < 0.5) ? "1.0 m (Terrain Surface)" :
                              (v <= 5.0) ? String.format("%.1f m (Low Relief)", v) :
                              String.format("%.1f m (Canopy / Aerial - Max: %.1f m)", v, terrainMaxAltitudeMeters);
                posZValLabel.setText(desc);
                posZValLabel.setStyle(v < 0.0 ? "-fx-text-fill: #f59e0b; -fx-font-weight: bold;" : v > 5.0 ? "-fx-text-fill: #c084fc; -fx-font-weight: bold;" : "-fx-text-fill: #4ade80; -fx-font-weight: bold;");
            }
        };

        atSurfaceCheckBox.selectedProperty().addListener((o, a, b) -> updateZText.run());
        posZSlider.valueProperty().addListener((o, a, b) -> updateZText.run());
        posXSlider.valueProperty().addListener((o, a, b) -> { if (atSurfaceCheckBox.isSelected()) updateZText.run(); });
        posYSlider.valueProperty().addListener((o, a, b) -> { if (atSurfaceCheckBox.isSelected()) updateZText.run(); });
        updateZText.run();

        grid.add(lblX, 0, 0); grid.add(posXSlider, 1, 0); grid.add(posXValLabel, 2, 0);
        grid.add(lblY, 0, 1); grid.add(posYSlider, 1, 1); grid.add(posYValLabel, 2, 1);
        grid.add(lblZ, 0, 2); grid.add(posZSlider, 1, 2); grid.add(posZValLabel, 2, 2);
        grid.add(atSurfaceCheckBox, 1, 3, 2, 1);

        Button btnResetCenter = new Button();
        btnResetCenter.textProperty().bind(i18n.createStringBinding("god.spatial.reset.btn"));
        btnResetCenter.setStyle("-fx-background-color: rgba(56,189,248,0.15); -fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-border-color: #38bdf8; -fx-border-radius: 4; -fx-cursor: hand;");
        btnResetCenter.tooltipProperty().bind(i18n.createTooltipBinding("god.spatial.reset.btn.tt"));
        btnResetCenter.setOnAction(e -> {
            posXSlider.setValue(terrainWidthMeters / 2.0);
            posYSlider.setValue(terrainLengthMeters / 2.0);
            posZSlider.setValue(1.0);
            atSurfaceCheckBox.setSelected(true);
        });

        box.getChildren().addAll(terrainDimensionsInfoLabel, grid, btnResetCenter);
        return box;
    }

    private long calculateTargetSeconds() {
        int d = spDay.getValue() != null ? spDay.getValue() : 1;
        int h = spHour.getValue() != null ? spHour.getValue() : 8;
        int m = spMin.getValue() != null ? spMin.getValue() : 0;
        int s = spSec.getValue() != null ? spSec.getValue() : 0;
        return (long) (d - 1) * 86400L + h * 3600L + m * 60L + s;
    }

    public static String formatTickToCalendarTime(long tick, double stepSec) {
        long targetSec = (long) (tick * stepSec);
        int d = (int) (targetSec / 86400L) + 1;
        long rem = targetSec % 86400L;
        int h = (int) (rem / 3600L);
        rem %= 3600L;
        int m = (int) (rem / 60L);
        int s = (int) (rem % 60L);
        return String.format("Day %d %02d:%02d:%02d", d, h, m, s);
    }

    /**
     * Sub-block 3: Entities, Castes & Brood Management
     */
    private Node createEntitiesSubBlockNode() {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        VBox box = new VBox(10);
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);

        Label lblAction = new Label();
        lblAction.textProperty().bind(i18n.createStringBinding("god.entities.action.label"));
        lblAction.setStyle("-fx-font-weight: bold;");
        entityActionSelect = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(
                "➕ Spawn / Inject Population (Adult / Brood)",
                "☠️ Targeted Individual Elimination",
                "🐣 Brood Depuration / Elimination",
                "💀 Total Colony Extinction"
        ));
        entityActionSelect.getSelectionModel().selectFirst();
        entityActionSelect.setPrefWidth(260);
        entityActionSelect.tooltipProperty().bind(i18n.createTooltipBinding("god.entities.action.tt"));

        Label casteLabel = new Label();
        casteLabel.textProperty().bind(i18n.createStringBinding("god.entities.caste.label"));
        casteLabel.setStyle("-fx-font-weight: bold;");
        casteSelect = new ComboBox<>();
        casteSelect.setPrefWidth(260);
        casteSelect.tooltipProperty().bind(i18n.createTooltipBinding("god.entities.caste.tt"));
        updateCastesForSelectedColony(eventColonySelect != null ? eventColonySelect.getValue() : null);

        Label countLabel = new Label();
        countLabel.textProperty().bind(i18n.createStringBinding("god.entities.count.label"));
        countLabel.setStyle("-fx-font-weight: bold;");
        antCountSpinner = new Spinner<>(1, 1000, 10);
        antCountSpinner.setEditable(true);
        antCountSpinner.setPrefWidth(120);
        antCountSpinner.tooltipProperty().bind(i18n.createTooltipBinding("god.entities.count.tt"));

        grid.add(lblAction, 0, 0); grid.add(entityActionSelect, 1, 0);
        grid.add(casteLabel, 0, 1); grid.add(casteSelect, 1, 1);
        grid.add(countLabel, 0, 2); grid.add(antCountSpinner, 1, 2);

        HBox actionBtnRow = new HBox(8);
        Button btnScheduleEntities = new Button();
        btnScheduleEntities.textProperty().bind(i18n.createStringBinding("god.btn.schedule_entities"));
        btnScheduleEntities.getStyleClass().add("btn-primary");
        btnScheduleEntities.tooltipProperty().bind(i18n.createTooltipBinding("god.entities.btn.tt"));
        btnScheduleEntities.setOnAction(e -> scheduleEntitiesEvent());

        actionBtnRow.getChildren().add(btnScheduleEntities);

        box.getChildren().addAll(grid, actionBtnRow);
        return box;
    }

    /**
     * Sub-block 4: Resources & Biomass
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
        resourceTypeSelect.tooltipProperty().bind(i18n.createTooltipBinding("god.resources.type.tt"));

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
        foodNatureSelect.tooltipProperty().bind(i18n.createTooltipBinding("god.resources.nature.tt"));

        Label foodLabel = new Label();
        foodLabel.textProperty().bind(i18n.createStringBinding("god.res.qty"));
        foodSlider = new Slider(10, 1000, 100);
        foodSlider.setPrefWidth(180);
        foodSlider.tooltipProperty().bind(i18n.createTooltipBinding("god.resources.qty.tt"));
        Label foodValue = new Label("100 g");
        foodValue.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");

        Runnable updateResUnit = () -> {
            double val = foodSlider.getValue();
            String rType = resourceTypeSelect.getValue();
            String fNat = foodNatureSelect.getValue();
            String unit = "g";
            if ((rType != null && rType.contains("Water")) ||
                (fNat != null && (fNat.contains("Honeydew") || fNat.contains("Nectar") || fNat.contains("Jelly")))) {
                unit = "mL";
            }
            foodValue.setText(String.format("%.0f %s", val, unit));
        };

        foodSlider.valueProperty().addListener((o,a,b) -> updateResUnit.run());
        resourceTypeSelect.valueProperty().addListener((o,a,b) -> updateResUnit.run());
        foodNatureSelect.valueProperty().addListener((o,a,b) -> updateResUnit.run());
        updateResUnit.run();

        grid.add(lblResType, 0, 0); grid.add(resourceTypeSelect, 1, 0);
        grid.add(lblNature, 0, 1); grid.add(foodNatureSelect, 1, 1);
        grid.add(foodLabel, 0, 2); grid.add(foodSlider, 1, 2); grid.add(foodValue, 2, 2);

        HBox actionBtnRow = new HBox(8);
        Button btnScheduleRes = new Button();
        btnScheduleRes.textProperty().bind(i18n.createStringBinding("god.btn.schedule_res"));
        btnScheduleRes.getStyleClass().add("btn-primary");
        btnScheduleRes.tooltipProperty().bind(i18n.createTooltipBinding("god.resources.btn.tt"));
        btnScheduleRes.setOnAction(e -> scheduleResourceEvent());

        actionBtnRow.getChildren().add(btnScheduleRes);

        box.getChildren().addAll(grid, actionBtnRow);
        return box;
    }

    /**
     * Sub-block 5: Environmental Disasters
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
        disasterSelect.tooltipProperty().bind(i18n.createTooltipBinding("god.disasters.type.tt"));

        Label intLabel = new Label();
        intLabel.textProperty().bind(i18n.createStringBinding("god.disasters.magnitude"));
        intensitySlider = new Slider(0.1, 1.0, 0.5);
        intensitySlider.setPrefWidth(180);
        intensitySlider.tooltipProperty().bind(i18n.createTooltipBinding("god.disasters.intensity.tt"));
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
        durationSlider.tooltipProperty().bind(i18n.createTooltipBinding("god.disasters.duration.tt"));
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
        btnScheduleDisaster.tooltipProperty().bind(i18n.createTooltipBinding("god.disasters.btn.tt"));
        btnScheduleDisaster.setOnAction(e -> scheduleDisasterEvent());

        Button btnStopDisasters = new Button();
        btnStopDisasters.textProperty().bind(i18n.createStringBinding("god.disasters.btn_stop"));
        btnStopDisasters.getStyleClass().add("btn-danger");
        btnStopDisasters.setOnAction(e -> {
            if (callback != null) callback.stopDisasters();
        });

        actionBtnRow.getChildren().addAll(btnScheduleDisaster, btnStopDisasters);

        box.getChildren().addAll(grid, actionBtnRow);
        return box;
    }

    /**
     * Sub-block 6: Abiotic Physical Drivers with Relative Delta (+/-) & Duration or Absolute Fixed Target
     */
    private Node createParametersSubBlockNode() {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        VBox box = new VBox(10);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);

        Label lblMode = new Label();
        lblMode.textProperty().bind(i18n.createStringBinding("god.abiotic.mode.label"));
        lblMode.setStyle("-fx-font-weight: bold;");
        abioticModeSelect = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(
                "📈 Relative Shift (Δ +/- relative to current climate)",
                "🎯 Fixed Absolute Value (Override current climate)"
        ));
        abioticModeSelect.getSelectionModel().selectFirst();
        abioticModeSelect.setPrefWidth(340);
        abioticModeSelect.setTooltip(new Tooltip("Toggle between relative delta (+/- offset) and fixed absolute values for temperature, humidity, wind, and solar radiation."));

        Label lblDuration = new Label();
        lblDuration.textProperty().bind(i18n.createStringBinding("god.abiotic.duration.label"));
        lblDuration.setStyle("-fx-font-weight: bold;");
        abioticDurationSlider = new Slider(5, 43200, 60);
        abioticDurationSlider.setPrefWidth(200);
        abioticDurationSlider.setTooltip(new Tooltip("Duration during which the abiotic modification remains active in spatio-temporal simulation."));
        abioticDurationValLabel = new Label("60 min (1h)");
        abioticDurationValLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
        abioticDurationSlider.valueProperty().addListener((o, a, b) -> {
            int mins = b.intValue();
            abioticDurationValLabel.setText(mins < 60 ? mins + " min" : (mins < 1440) ? (mins / 60) + "h " + (mins % 60) + "m" : (mins / 1440) + " Days");
        });

        Label lblTemp = new Label(i18n.get("god.abiotic.temp.label"));
        lblTemp.setStyle("-fx-font-weight: bold;");
        deltaTempSlider = new Slider(-20.0, 20.0, 5.0);
        deltaTempSlider.setPrefWidth(200);
        deltaTempSlider.setTooltip(new Tooltip("Temperature delta or target absolute temperature (°C). Impacts nest metabolism and activity."));
        deltaTempValLabel = new Label("+5.0 °C (Warming)");
        deltaTempValLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");

        Label lblHumidity = new Label(i18n.get("god.abiotic.humidity.label"));
        lblHumidity.setStyle("-fx-font-weight: bold;");
        deltaHumiditySlider = new Slider(-50.0, 50.0, 20.0);
        deltaHumiditySlider.setPrefWidth(200);
        deltaHumiditySlider.setTooltip(new Tooltip("Soil and air relative humidity delta or target percentage (%)."));
        deltaHumidityValLabel = new Label("+20.0 % (Humidifying)");
        deltaHumidityValLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");

        Label lblWind = new Label(i18n.get("god.abiotic.wind.label"));
        lblWind.setStyle("-fx-font-weight: bold;");
        deltaWindSlider = new Slider(-10.0, 15.0, 2.0);
        deltaWindSlider.setPrefWidth(200);
        deltaWindSlider.setTooltip(new Tooltip("Wind speed delta or fixed target (m/s). Regulates surface pheromone dissipation."));
        deltaWindValLabel = new Label("+2.0 m/s");
        deltaWindValLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");

        Label lblSolar = new Label(i18n.get("god.abiotic.solar.label"));
        lblSolar.setStyle("-fx-font-weight: bold;");
        deltaSolarSlider = new Slider(-500.0, 500.0, 100.0);
        deltaSolarSlider.setPrefWidth(200);
        deltaSolarSlider.setTooltip(new Tooltip("Direct solar radiation flux delta or fixed target (W/m²). Regulates photosynthesis and mound heating."));
        deltaSolarValLabel = new Label("+100 W/m²");
        deltaSolarValLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");

        abioticModeSelect.valueProperty().addListener((o, oldVal, newVal) -> {
            boolean isRel = newVal != null && newVal.contains("Relative");
            if (isRel) {
                lblTemp.setText("Temperature Shift (Δ °C):");
                deltaTempSlider.setMin(-20.0); deltaTempSlider.setMax(20.0); deltaTempSlider.setValue(5.0);
                deltaTempValLabel.setText("+5.0 °C (Warming)");
                deltaTempValLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");

                lblHumidity.setText("Humidity Shift (Δ %):");
                deltaHumiditySlider.setMin(-50.0); deltaHumiditySlider.setMax(50.0); deltaHumiditySlider.setValue(20.0);
                deltaHumidityValLabel.setText("+20.0 % (Humidifying)");
                deltaHumidityValLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");

                lblWind.setText("Wind Shift (Δ m/s):");
                deltaWindSlider.setMin(-10.0); deltaWindSlider.setMax(15.0); deltaWindSlider.setValue(2.0);
                deltaWindValLabel.setText("+2.0 m/s");

                lblSolar.setText("Radiation Shift (Δ W/m²):");
                deltaSolarSlider.setMin(-500.0); deltaSolarSlider.setMax(500.0); deltaSolarSlider.setValue(100.0);
                deltaSolarValLabel.setText("+100 W/m²");
            } else {
                lblTemp.setText("Target Fixed Temperature (°C):");
                deltaTempSlider.setMin(-10.0); deltaTempSlider.setMax(50.0); deltaTempSlider.setValue(22.0);
                deltaTempValLabel.setText("22.0 °C");
                deltaTempValLabel.setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold;");

                lblHumidity.setText("Target Fixed Humidity (%):");
                deltaHumiditySlider.setMin(0.0); deltaHumiditySlider.setMax(100.0); deltaHumiditySlider.setValue(65.0);
                deltaHumidityValLabel.setText("65.0 %");
                deltaHumidityValLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");

                lblWind.setText("Target Fixed Wind Speed (m/s):");
                deltaWindSlider.setMin(0.0); deltaWindSlider.setMax(30.0); deltaWindSlider.setValue(1.5);
                deltaWindValLabel.setText("1.5 m/s");

                lblSolar.setText("Target Fixed Solar Radiation (W/m²):");
                deltaSolarSlider.setMin(0.0); deltaSolarSlider.setMax(1200.0); deltaSolarSlider.setValue(450.0);
                deltaSolarValLabel.setText("450 W/m²");
            }
            updateDerivedPhysicalOutputs();
        });

        deltaTempSlider.valueProperty().addListener((o, a, b) -> {
            double v = b.doubleValue();
            boolean isRel = abioticModeSelect.getValue() != null && abioticModeSelect.getValue().contains("Relative");
            if (isRel) {
                deltaTempValLabel.setText(String.format("%+.1f °C (%s)", v, v > 0 ? "Warming" : v < 0 ? "Cooling" : "Unchanged"));
                deltaTempValLabel.setStyle(v > 0 ? "-fx-text-fill: #ef4444; -fx-font-weight: bold;" : v < 0 ? "-fx-text-fill: #38bdf8; -fx-font-weight: bold;" : "-fx-text-fill: #94a3b8; -fx-font-weight: bold;");
            } else {
                deltaTempValLabel.setText(String.format("%.1f °C", v));
                deltaTempValLabel.setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold;");
            }
            updateDerivedPhysicalOutputs();
        });

        deltaHumiditySlider.valueProperty().addListener((o, a, b) -> {
            double v = b.doubleValue();
            boolean isRel = abioticModeSelect.getValue() != null && abioticModeSelect.getValue().contains("Relative");
            if (isRel) {
                deltaHumidityValLabel.setText(String.format("%+.1f %% (%s)", v, v > 0 ? "Humidifying" : v < 0 ? "Drying" : "Unchanged"));
            } else {
                deltaHumidityValLabel.setText(String.format("%.1f %%", v));
            }
            updateDerivedPhysicalOutputs();
        });

        deltaWindSlider.valueProperty().addListener((o, a, b) -> {
            double v = b.doubleValue();
            boolean isRel = abioticModeSelect.getValue() != null && abioticModeSelect.getValue().contains("Relative");
            if (isRel) {
                deltaWindValLabel.setText(String.format("%+.1f m/s", v));
            } else {
                deltaWindValLabel.setText(String.format("%.1f m/s", v));
            }
            updateDerivedPhysicalOutputs();
        });

        deltaSolarSlider.valueProperty().addListener((o, a, b) -> {
            double v = b.doubleValue();
            boolean isRel = abioticModeSelect.getValue() != null && abioticModeSelect.getValue().contains("Relative");
            if (isRel) {
                deltaSolarValLabel.setText(String.format("%+.0f W/m²", v));
            } else {
                deltaSolarValLabel.setText(String.format("%.0f W/m²", v));
            }
            updateDerivedPhysicalOutputs();
        });

        grid.add(lblMode, 0, 0); grid.add(abioticModeSelect, 1, 0);
        grid.add(lblTemp, 0, 1); grid.add(deltaTempSlider, 1, 1); grid.add(deltaTempValLabel, 2, 1);
        grid.add(lblHumidity, 0, 2); grid.add(deltaHumiditySlider, 1, 2); grid.add(deltaHumidityValLabel, 2, 2);
        grid.add(lblWind, 0, 3); grid.add(deltaWindSlider, 1, 3); grid.add(deltaWindValLabel, 2, 3);
        grid.add(lblSolar, 0, 4); grid.add(deltaSolarSlider, 1, 4); grid.add(deltaSolarValLabel, 2, 4);
        grid.add(lblDuration, 0, 5); grid.add(abioticDurationSlider, 1, 5); grid.add(abioticDurationValLabel, 2, 5);

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
        btnScheduleAbiotic.setGraphic(new FontIcon(Feather.SUN));
        btnScheduleAbiotic.tooltipProperty().bind(i18n.createTooltipBinding("god.abiotic.btn.tt"));
        btnScheduleAbiotic.setOnAction(e -> scheduleAbioticEvent());

        actionBtnRow.getChildren().add(btnScheduleAbiotic);

        box.getChildren().addAll(grid, derivedBox, actionBtnRow);
        return box;
    }

    /**
     * Sub-block 7: Pheromone & Behavioral Perturbations (With Intensity & Duration)
     */
    private Node createPheromoneSubBlockNode() {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        VBox box = new VBox(10);

        Label lblCenterNote = new Label();
        lblCenterNote.textProperty().bind(i18n.createStringBinding("god.phero.centernote"));
        lblCenterNote.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 11px; -fx-font-style: italic;");
        lblCenterNote.tooltipProperty().bind(i18n.createTooltipBinding("god.phero.centernote.tt"));

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);

        Label lblType = new Label();
        lblType.textProperty().bind(i18n.createStringBinding("god.phero.type.label"));
        lblType.setStyle("-fx-font-weight: bold;");
        pheromoneTypeSelect = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(
                "Alarm Pheromone Storm",
                "Exploration Trail Eraser",
                "Synthetic Queen Attractant Beacon",
                "Biomass Attractant Pulse"
        ));
        pheromoneTypeSelect.getSelectionModel().selectFirst();
        pheromoneTypeSelect.setPrefWidth(300);
        pheromoneTypeSelect.tooltipProperty().bind(i18n.createTooltipBinding("god.phero.type.tt"));

        Label lblRadius = new Label();
        lblRadius.textProperty().bind(i18n.createStringBinding("god.phero.radius.label"));
        lblRadius.setStyle("-fx-font-weight: bold;");
        pheroRadiusSlider = new Slider(1.0, 30.0, 10.0);
        pheroRadiusSlider.setPrefWidth(180);
        pheroRadiusSlider.tooltipProperty().bind(i18n.createTooltipBinding("god.phero.radius.tt"));
        pheroRadiusValLabel = new Label("10.0 m");
        pheroRadiusValLabel.setStyle("-fx-text-fill: #8b5cf6; -fx-font-weight: bold;");
        pheroRadiusSlider.valueProperty().addListener((o, a, b) -> pheroRadiusValLabel.setText(String.format("%.1f m", b.doubleValue())));

        Label lblIntensity = new Label();
        lblIntensity.textProperty().bind(i18n.createStringBinding("god.phero.intensity.label"));
        lblIntensity.setStyle("-fx-font-weight: bold;");
        pheroIntensitySlider = new Slider(0.1, 1.0, 0.8);
        pheroIntensitySlider.setPrefWidth(180);
        pheroIntensitySlider.tooltipProperty().bind(i18n.createTooltipBinding("god.phero.intensity.tt"));
        pheroIntensityValLabel = new Label("High (0.8)");
        pheroIntensityValLabel.setStyle("-fx-text-fill: #8b5cf6; -fx-font-weight: bold;");
        pheroIntensitySlider.valueProperty().addListener((o, a, b) -> pheroIntensityValLabel.setText(String.format("%.1f", b.doubleValue())));

        Label lblDuration = new Label();
        lblDuration.textProperty().bind(i18n.createStringBinding("god.phero.duration.label"));
        lblDuration.setStyle("-fx-font-weight: bold;");
        pheroDurationSlider = new Slider(5, 1440, 30);
        pheroDurationSlider.setPrefWidth(180);
        pheroDurationSlider.tooltipProperty().bind(i18n.createTooltipBinding("god.phero.duration.tt"));
        pheroDurationValLabel = new Label("30 min");
        pheroDurationValLabel.setStyle("-fx-text-fill: #8b5cf6; -fx-font-weight: bold;");
        pheroDurationSlider.valueProperty().addListener((o, a, b) -> {
            int mins = b.intValue();
            pheroDurationValLabel.setText(mins < 60 ? mins + " min" : (mins / 60) + "h " + (mins % 60) + "m");
        });

        grid.add(lblType, 0, 0); grid.add(pheromoneTypeSelect, 1, 0);
        grid.add(lblRadius, 0, 1); grid.add(pheroRadiusSlider, 1, 1); grid.add(pheroRadiusValLabel, 2, 1);
        grid.add(lblIntensity, 0, 2); grid.add(pheroIntensitySlider, 1, 2); grid.add(pheroIntensityValLabel, 2, 2);
        grid.add(lblDuration, 0, 3); grid.add(pheroDurationSlider, 1, 3); grid.add(pheroDurationValLabel, 2, 3);

        Button btnSchedulePhero = new Button();
        btnSchedulePhero.textProperty().bind(i18n.createStringBinding("god.phero.btn"));
        btnSchedulePhero.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnSchedulePhero.setGraphic(new FontIcon(Feather.WIND));
        btnSchedulePhero.tooltipProperty().bind(i18n.createTooltipBinding("god.phero.btn.tt"));
        btnSchedulePhero.setOnAction(e -> schedulePheromoneEvent());

        box.getChildren().addAll(lblCenterNote, grid, btnSchedulePhero);
        return box;
    }

    /**
     * Sub-block 8: Apex Predators & Biological Invasions (With Count & Duration)
     */
    private Node createInvasionSubBlockNode() {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        VBox box = new VBox(10);
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);

        Label lblType = new Label();
        lblType.textProperty().bind(i18n.createStringBinding("god.invasion.type.label"));
        lblType.setStyle("-fx-font-weight: bold;");
        invasionTypeSelect = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(
                "Anteater Raid",
                "Army Ant Raid",
                "Parasitic Mite Infestation",
                "Locust Swarm",
                "Wasp Squad Attack",
                "Spiders & Mantis Raid",
                "🛡️ Protection / Absence de Prédateurs (Safe Sanctuary)"
        ));
        invasionTypeSelect.getSelectionModel().selectFirst();
        invasionTypeSelect.setPrefWidth(300);
        invasionTypeSelect.tooltipProperty().bind(i18n.createTooltipBinding("god.invasion.type.tt"));

        Label lblCount = new Label();
        lblCount.textProperty().bind(i18n.createStringBinding("god.invasion.count.label"));
        lblCount.setStyle("-fx-font-weight: bold;");
        invasionCountSpinner = new Spinner<>(0, 500, 25);
        invasionCountSpinner.setEditable(true);
        invasionCountSpinner.setPrefWidth(120);
        invasionCountSpinner.tooltipProperty().bind(i18n.createTooltipBinding("god.invasion.count.tt"));

        Label lblInvasionNote = new Label("⚔️ Active Raid / Attack (25 specimens introduced).");
        lblInvasionNote.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px; -fx-font-style: italic;");

        Label lblPredatorLifecycleNote = new Label("⏳ Cycle & Retreat: Predators hunt for the duration of the event. Upon expiration, remaining individuals leave the territory (despawn / retreat).");
        lblPredatorLifecycleNote.getStyleClass().add("legend-hover-info");
        lblPredatorLifecycleNote.setStyle("-fx-font-size: 11px; -fx-font-style: italic;");
        lblPredatorLifecycleNote.setWrapText(true);

        Runnable updateInvasionStatus = () -> {
            String type = invasionTypeSelect.getValue();
            boolean isProtection = type != null && (type.contains("Protection") || type.contains("Absence") || type.contains("Sanctuary"));
            Integer cnt = invasionCountSpinner.getValue();
            boolean isZero = cnt != null && cnt == 0;

            if (isProtection) {
                invasionCountSpinner.setDisable(true);
                lblInvasionNote.setText("🛡️ Safe Sanctuary: Eliminates all current predators and blocks all raids during the event.");
                lblInvasionNote.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 11px; -fx-font-weight: bold;");
            } else if (isZero) {
                invasionCountSpinner.setDisable(false);
                lblInvasionNote.setText("🛡️ Zero Predator Pressure (0 predators): Repels and clears active predators from zone.");
                lblInvasionNote.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 11px; -fx-font-weight: bold;");
            } else {
                invasionCountSpinner.setDisable(false);
                lblInvasionNote.setText("⚔️ Active Raid / Attack (" + (cnt != null ? cnt : 0) + " specimens introduced).");
                lblInvasionNote.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px; -fx-font-style: italic;");
            }
        };

        invasionTypeSelect.valueProperty().addListener((o, a, b) -> updateInvasionStatus.run());
        invasionCountSpinner.valueProperty().addListener((o, a, b) -> updateInvasionStatus.run());
        updateInvasionStatus.run();

        Label lblDuration = new Label();
        lblDuration.textProperty().bind(i18n.createStringBinding("god.invasion.duration.label"));
        lblDuration.setStyle("-fx-font-weight: bold;");
        invasionDurationSlider = new Slider(15, 43200, 120);
        invasionDurationSlider.setPrefWidth(180);
        invasionDurationSlider.tooltipProperty().bind(i18n.createTooltipBinding("god.invasion.duration.tt"));
        invasionDurationValLabel = new Label("2h (120 min)");
        invasionDurationValLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
        invasionDurationSlider.valueProperty().addListener((o, a, b) -> {
            int mins = b.intValue();
            invasionDurationValLabel.setText(mins < 60 ? mins + " min" : (mins < 1440) ? (mins / 60) + "h " + (mins % 60) + "m" : (mins / 1440) + " Days");
        });

        grid.add(lblType, 0, 0); grid.add(invasionTypeSelect, 1, 0);
        grid.add(lblCount, 0, 1); grid.add(invasionCountSpinner, 1, 1);
        grid.add(lblDuration, 0, 2); grid.add(invasionDurationSlider, 1, 2); grid.add(invasionDurationValLabel, 2, 2);

        Button btnScheduleInvasion = new Button();
        btnScheduleInvasion.textProperty().bind(i18n.createStringBinding("god.invasion.btn"));
        btnScheduleInvasion.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnScheduleInvasion.setGraphic(new FontIcon(Feather.SHIELD_OFF));
        btnScheduleInvasion.tooltipProperty().bind(i18n.createTooltipBinding("god.invasion.btn.tt"));
        btnScheduleInvasion.setOnAction(e -> scheduleInvasionEvent());

        box.getChildren().addAll(grid, lblInvasionNote, lblPredatorLifecycleNote, btnScheduleInvasion);
        return box;
    }

    /**
     * Sub-block 9: Genetics, Metabolic Boosts & Mutagens (With Intensity & Duration)
     */
    private Node createMutationSubBlockNode() {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        VBox box = new VBox(10);

        Label lblMutationTargetNote = new Label();
        lblMutationTargetNote.textProperty().bind(i18n.createStringBinding("god.mutation.targetnote"));
        lblMutationTargetNote.setStyle("-fx-text-fill: #ec4899; -fx-font-size: 11px; -fx-font-style: italic;");
        lblMutationTargetNote.tooltipProperty().bind(i18n.createTooltipBinding("god.mutation.targetnote.tt"));

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);

        Label lblType = new Label();
        lblType.textProperty().bind(i18n.createStringBinding("god.mutation.type.label"));
        lblType.setStyle("-fx-font-weight: bold;");
        mutationTypeSelect = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(
                "Royal Jelly Overfeeding (Queen Oviposition Boost)",
                "Frenzy & Metabolism (Attack & Speed Boost)",
                "Biological Longevity (Senescence Protection)",
                "Anarchy & Caste Perturbation (Role Disorganization)"
        ));
        mutationTypeSelect.getSelectionModel().selectFirst();
        mutationTypeSelect.setPrefWidth(340);
        mutationTypeSelect.tooltipProperty().bind(i18n.createTooltipBinding("god.mutation.type.tt"));

        Label lblCasteScope = new Label();
        lblCasteScope.textProperty().bind(i18n.createStringBinding("god.mutation.caste.label"));
        lblCasteScope.setStyle("-fx-font-weight: bold;");
        mutationCasteScopeSelect = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(
                "👑 All Castes (Queen, Workers, Soldiers, Brood)",
                "🔨 Workers & Foragers Only",
                "🛡️ Soldier Caste Only",
                "👑 Queen Only (Reproductive Lineage)",
                "🥚 Brood Only (Eggs, Larvae, Pupae)"
        ));
        mutationCasteScopeSelect.getSelectionModel().selectFirst();
        mutationCasteScopeSelect.setPrefWidth(340);
        mutationCasteScopeSelect.tooltipProperty().bind(i18n.createTooltipBinding("god.mutation.caste.tt"));

        Label lblIntensity = new Label();
        lblIntensity.textProperty().bind(i18n.createStringBinding("god.mutation.intensity.label"));
        lblIntensity.setStyle("-fx-font-weight: bold;");
        mutationIntensitySlider = new Slider(0.1, 5.0, 1.5);
        mutationIntensitySlider.setPrefWidth(180);
        mutationIntensitySlider.tooltipProperty().bind(i18n.createTooltipBinding("god.mutation.intensity.tt"));
        mutationIntensityValLabel = new Label("1.5x (+50% Boost)");
        mutationIntensityValLabel.setStyle("-fx-text-fill: #ec4899; -fx-font-weight: bold;");
        mutationIntensitySlider.valueProperty().addListener((o, a, b) -> {
            double v = b.doubleValue();
            if (v < 1.0) {
                mutationIntensityValLabel.setText(String.format("%.2fx (-%.0f%% Weakening)", v, (1.0 - v) * 100.0));
                mutationIntensityValLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            } else if (Math.abs(v - 1.0) < 0.05) {
                mutationIntensityValLabel.setText("1.00x (Neutral Baseline)");
                mutationIntensityValLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold;");
            } else {
                mutationIntensityValLabel.setText(String.format("%.1fx (+%.0f%% Boost)", v, (v - 1.0) * 100.0));
                mutationIntensityValLabel.setStyle("-fx-text-fill: #ec4899; -fx-font-weight: bold;");
            }
        });

        Label lblDur = new Label();
        lblDur.textProperty().bind(i18n.createStringBinding("god.mutation.duration.label"));
        lblDur.setStyle("-fx-font-weight: bold;");
        mutationDurationSlider = new Slider(5, 1440, 60);
        mutationDurationSlider.setPrefWidth(180);
        mutationDurationSlider.tooltipProperty().bind(i18n.createTooltipBinding("god.mutation.duration.tt"));
        mutationDurationValLabel = new Label("60 min (1h)");
        mutationDurationValLabel.setStyle("-fx-text-fill: #ec4899; -fx-font-weight: bold;");
        mutationDurationSlider.valueProperty().addListener((o, a, b) -> {
            int mins = b.intValue();
            mutationDurationValLabel.setText(mins < 60 ? mins + " min" : (mins / 60) + "h " + (mins % 60) + "m");
        });

        mutationExplanationLabel = new Label();
        mutationExplanationLabel.setWrapText(true);
        mutationExplanationLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #f472b6; -fx-background-color: rgba(236,72,153,0.1); -fx-padding: 8; -fx-background-radius: 4;");
        
        Runnable updateMutationExplanation = () -> {
            String type = mutationTypeSelect.getValue();
            double mult = mutationIntensitySlider.getValue();
            double pct = (mult - 1.0) * 100.0;
            if (type != null && type.contains("Royal Jelly")) {
                mutationExplanationLabel.setText(i18n.get("god.mutation.exp.royal_jelly", pct));
            } else if (type != null && type.contains("Frenzy")) {
                mutationExplanationLabel.setText(i18n.get("god.mutation.exp.frenzy", mult));
            } else if (type != null && type.contains("Longevity")) {
                mutationExplanationLabel.setText(i18n.get("god.mutation.exp.longevity"));
            } else if (type != null && type.contains("Anarchy")) {
                mutationExplanationLabel.setText(i18n.get("god.mutation.exp.anarchy"));
            }
        };

        mutationTypeSelect.valueProperty().addListener((o, a, b) -> updateMutationExplanation.run());
        mutationIntensitySlider.valueProperty().addListener((o, a, b) -> updateMutationExplanation.run());
        updateMutationExplanation.run();

        grid.add(lblType, 0, 0); grid.add(mutationTypeSelect, 1, 0);
        grid.add(lblCasteScope, 0, 1); grid.add(mutationCasteScopeSelect, 1, 1);
        grid.add(lblIntensity, 0, 2); grid.add(mutationIntensitySlider, 1, 2); grid.add(mutationIntensityValLabel, 2, 2);
        grid.add(lblDur, 0, 3); grid.add(mutationDurationSlider, 1, 3); grid.add(mutationDurationValLabel, 2, 3);

        Button btnScheduleMutation = new Button();
        btnScheduleMutation.textProperty().bind(i18n.createStringBinding("god.mutation.btn"));
        btnScheduleMutation.setStyle("-fx-background-color: #ec4899; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnScheduleMutation.setGraphic(new FontIcon(Feather.ACTIVITY));
        btnScheduleMutation.tooltipProperty().bind(i18n.createTooltipBinding("god.mutation.btn.tt"));
        btnScheduleMutation.setOnAction(e -> scheduleMutationEvent());

        box.getChildren().addAll(lblMutationTargetNote, grid, mutationExplanationLabel, btnScheduleMutation);
        return box;
    }

    private void updateDerivedPhysicalOutputs() {
        if (deltaTempSlider == null || abioticModeSelect == null) return;

        boolean isRel = abioticModeSelect.getValue() != null && abioticModeSelect.getValue().contains("Relative");
        double temp = deltaTempSlider.getValue();
        double hum = deltaHumiditySlider.getValue();
        double wind = deltaWindSlider.getValue();
        double solar = deltaSolarSlider.getValue();

        if (isRel) {
            derivedPheroLabel.setText(String.format("• Climate Delta: ΔT = %+.1f°C | ΔHumidity = %+.1f%% | ΔWind = %+.1fm/s", temp, hum, wind));
            derivedPrimaryProductivityLabel.setText(String.format("• Relative Solar Radiation: %+.0f W/m² (Duration: %d min)", solar, (int) abioticDurationSlider.getValue()));
            derivedOvipositionLabel.setText("• Estimated Thermal Impact on Nest Activity: " + (temp > 0 ? "Metabolic Acceleration" : temp < 0 ? "Slowdown / Lethargy" : "Stability"));
        } else {
            derivedPheroLabel.setText(String.format("• Fixed Absolute Climate: T = %.1f°C | Humidity = %.1f%% | Wind = %.1fm/s", temp, hum, wind));
            derivedPrimaryProductivityLabel.setText(String.format("• Fixed Solar Radiation: %.0f W/m² (Duration: %d min)", solar, (int) abioticDurationSlider.getValue()));
            derivedOvipositionLabel.setText("• Fixed Metabolic Regime at " + String.format("%.1f°C", temp) + " (Override bioclimatic engine)");
        }
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
        javafx.application.Platform.runLater(() -> {
            syncTimePickersWithCurrentTick(10);
            scrollToFirstUpcomingEvent();
        });
    }

    private void scrollToFirstUpcomingEvent() {
        if (scheduledEventsListView == null || scheduledEventsList.isEmpty()) return;
        for (int i = 0; i < scheduledEventsList.size(); i++) {
            ScheduledEvent ev = scheduledEventsList.get(i);
            if (!ev.executed || ev.targetTick >= currentSimulationTick) {
                scheduledEventsListView.scrollTo(i);
                break;
            }
        }
    }

    private ScheduledEvent createBaseEvent(Category category, String eventType, String desc) {
        long tSec = calculateTargetSeconds();
        long rawTargetTick = Math.max(1, Math.round(tSec / simulationStepSec));
        // Enforce scheduling constraint: cannot schedule interventions in the past (must be >= currentSimulationTick)
        long targetTick = Math.max(currentSimulationTick, rawTargetTick);
        String timeFormatted = formatTickToCalendarTime(targetTick, simulationStepSec);
        String colTarget = eventColonySelect.getValue();
        return new ScheduledEvent(targetTick, timeFormatted, category, eventType, colTarget, desc);
    }

    private void scheduleEntitiesEvent() {
        String colTarget = (eventColonySelect != null && eventColonySelect.getValue() != null) ? eventColonySelect.getValue() : "All Colonies";
        String actionStr = entityActionSelect.getValue();
        String caste = casteSelect.getValue();
        int count = antCountSpinner.getValue();
        float x = (float) posXSlider.getValue();
        float y = (float) posYSlider.getValue();
        float z = getCalculatedSurfaceZ(x, y);

        ScheduledEvent ev = createBaseEvent(Category.ENTITIES, actionStr,
                String.format("%s %d x %s à (%.1f, %.1f, %.1f)", actionStr, count, caste, x, y, z));
        ev.colonyTarget = colTarget;
        ev.caste = caste;
        ev.count = count;
        ev.posX = x; ev.posY = y; ev.posZ = z;
        ev.entityAction = actionStr.contains("Injection") || actionStr.contains("Apparition") ? "SPAWN" :
                          actionStr.contains("Couvain") ? "BROOD_KILL" :
                          actionStr.contains("Extinction") ? "EXTINCT" : "KILL";

        scheduledEventsList.add(ev);
        sortScheduledEvents();
        scrollToFirstUpcomingEvent();
    }

    private void scheduleResourceEvent() {
        String resType = resourceTypeSelect.getValue();
        String foodNat = foodNatureSelect.getValue();
        float qty = (float) foodSlider.getValue();
        float x = (float) posXSlider.getValue();
        float y = (float) posYSlider.getValue();
        float z = getCalculatedSurfaceZ(x, y);

        String unit = "g";
        if ((resType != null && resType.contains("Water")) ||
            (foodNat != null && (foodNat.contains("Honeydew") || foodNat.contains("Nectar") || foodNat.contains("Jelly")))) {
            unit = "mL";
        }

        ScheduledEvent ev = createBaseEvent(Category.RESOURCE, resType,
                String.format("Apport %.0f %s (%s) - %s à (%.1f, %.1f, %.1f)", qty, unit, resType, foodNat, x, y, z));
        ev.resourceType = resType;
        ev.foodNature = foodNat;
        ev.amount = qty;
        ev.posX = x; ev.posY = y; ev.posZ = z;

        scheduledEventsList.add(ev);
        sortScheduledEvents();
        scrollToFirstUpcomingEvent();
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
        sortScheduledEvents();
        scrollToFirstUpcomingEvent();
    }

    private void scheduleAbioticEvent() {
        boolean isRel = abioticModeSelect.getValue() != null && abioticModeSelect.getValue().contains("Relative");
        int durMins = (int) abioticDurationSlider.getValue();
        float valTemp = (float) deltaTempSlider.getValue();
        float valHum = (float) deltaHumiditySlider.getValue();
        float valWind = (float) deltaWindSlider.getValue();
        float valSolar = (float) deltaSolarSlider.getValue();

        String desc = isRel ?
                String.format("Δ Climat (%d min) : ΔT=%+.1f°C, ΔH=%+.0f%%, ΔVent=%+.1fm/s, ΔSol=%+.0fW/m²", durMins, valTemp, valHum, valWind, valSolar) :
                String.format("Climat Absolu (%d min) : T=%.1f°C, H=%.0f%%, Vent=%.1fm/s, Sol=%.0fW/m²", durMins, valTemp, valHum, valWind, valSolar);

        ScheduledEvent ev = createBaseEvent(Category.ABIOTIC, isRel ? "Variation Climat" : "Climat Absolu Fixe", desc);
        ev.isRelativeAbiotic = isRel;
        ev.durationMinutes = durMins;
        if (isRel) {
            ev.deltaTempCelsius = valTemp;
            ev.deltaHumidityPercent = valHum;
            ev.deltaWindMetersPerSec = valWind;
            ev.deltaSolarWattsPerM2 = valSolar;
        } else {
            ev.tempCelsius = valTemp;
            ev.humidityPercent = valHum;
            ev.windMetersPerSec = valWind;
            ev.solarWattsPerM2 = valSolar;
        }

        scheduledEventsList.add(ev);
        sortScheduledEvents();
        scrollToFirstUpcomingEvent();
    }

    private void schedulePheromoneEvent() {
        String type = pheromoneTypeSelect.getValue();
        float radius = (float) pheroRadiusSlider.getValue();
        float intensity = (float) pheroIntensitySlider.getValue();
        int durMins = (int) pheroDurationSlider.getValue();
        float x = (float) posXSlider.getValue();
        float y = (float) posYSlider.getValue();
        float z = getCalculatedSurfaceZ(x, y);

        ScheduledEvent ev = createBaseEvent(Category.PHEROMONE, type,
                String.format("%s (Rayon: %.1fm, Intensité: %.1f, Durée: %d min) à (%.1f, %.1f, %.1f)", type, radius, intensity, durMins, x, y, z));
        ev.radius = radius;
        ev.intensity = intensity;
        ev.durationMinutes = durMins;
        ev.posX = x; ev.posY = y; ev.posZ = z;

        scheduledEventsList.add(ev);
        sortScheduledEvents();
        scrollToFirstUpcomingEvent();
    }

    private void scheduleInvasionEvent() {
        String type = invasionTypeSelect.getValue();
        boolean isProtection = type != null && (type.contains("Protection") || type.contains("Absence") || type.contains("Sanctuary"));
        int count = isProtection ? 0 : invasionCountSpinner.getValue();
        int durMins = (int) invasionDurationSlider.getValue();
        float x = (float) posXSlider.getValue();
        float y = (float) posYSlider.getValue();
        float z = getCalculatedSurfaceZ(x, y);

        String descStr = (count == 0 || isProtection)
                ? String.format("🛡️ Protection Prédatrice / Absence de Raids (%s, Durée: %d min)", type, durMins)
                : String.format("⚔️ %s (%d spécimens, Durée: %d min) à (%.1f, %.1f, %.1f)", type, count, durMins, x, y, z);

        ScheduledEvent ev = createBaseEvent(Category.INVASION, type, descStr);
        ev.count = count;
        ev.durationMinutes = durMins;
        ev.posX = x; ev.posY = y; ev.posZ = z;

        scheduledEventsList.add(ev);
        sortScheduledEvents();
        scrollToFirstUpcomingEvent();
    }

    private void scheduleMutationEvent() {
        String type = mutationTypeSelect.getValue();
        String casteScope = mutationCasteScopeSelect != null ? mutationCasteScopeSelect.getValue() : "👑 All Castes";
        float intensity = (float) mutationIntensitySlider.getValue();
        int durMins = (int) mutationDurationSlider.getValue();
        String targetCol = (eventColonySelect != null && eventColonySelect.getValue() != null) ? eventColonySelect.getValue() : "All Colonies";

        String descStr = (intensity < 1.0f)
                ? String.format("%s sur %s [%s] (Afaiblissement: %.2fx / -%.0f%%, Durée: %d min)", type, targetCol, casteScope, intensity, (1.0f - intensity) * 100.0f, durMins)
                : String.format("%s sur %s [%s] (Intensité: %.1fx / +%.0f%%, Durée: %d min)", type, targetCol, casteScope, intensity, (intensity - 1.0f) * 100.0f, durMins);

        ScheduledEvent ev = createBaseEvent(Category.MUTATION, type, descStr);
        ev.intensity = intensity;
        ev.durationMinutes = durMins;
        ev.caste = casteScope;
        ev.colonyTarget = targetCol;

        scheduledEventsList.add(ev);
        sortScheduledEvents();
        scrollToFirstUpcomingEvent();
    }

    private void executeScheduledEvent(ScheduledEvent ev) {
        String colStr = (ev.colonyTarget != null && !ev.colonyTarget.contains("Global")) ? ev.colonyTarget : "Colonie Primaire";
        if (callback != null) {
            switch (ev.category) {
                case ENTITIES -> {
                    if (ev.caste != null && (ev.caste.contains("Brood") || ev.caste.contains("Couvain") || ev.caste.contains("Œufs") || ev.caste.contains("Larves") || ev.caste.contains("Nymphes"))) {
                        callback.manageBrood(colStr, ev.caste, ev.entityAction, ev.count);
                    } else if ("KILL".equals(ev.entityAction) || "EXTINCT".equals(ev.entityAction) || "BROOD_KILL".equals(ev.entityAction)) {
                        callback.killAnts(colStr, ev.caste, "EXTINCT".equals(ev.entityAction) ? 99999 : ev.count);
                    } else {
                        callback.spawnAnts(colStr, ev.caste, ev.count, ev.posX, ev.posY, ev.posZ);
                    }
                }
                case RESOURCE -> callback.spawnFood(ev.posX, ev.posY, ev.posZ, ev.amount);
                case DISASTER -> callback.triggerDisaster(ev.disasterType, ev.intensity);
                case ABIOTIC -> {
                    if (ev.isRelativeAbiotic) {
                        callback.modifyAbioticClimate(true, ev.deltaTempCelsius, ev.deltaHumidityPercent, ev.deltaWindMetersPerSec, ev.deltaSolarWattsPerM2, ev.durationMinutes);
                    } else {
                        callback.modifyAbioticClimate(false, ev.tempCelsius, ev.humidityPercent, ev.windMetersPerSec, ev.solarWattsPerM2, ev.durationMinutes);
                        callback.modifyParameter("temperatureCelsius", ev.tempCelsius);
                        callback.modifyParameter("humidityPercent", ev.humidityPercent);
                        callback.modifyParameter("windSpeed", ev.windMetersPerSec);
                        callback.modifyParameter("solarRadiation", ev.solarWattsPerM2);
                    }
                }
                case PHEROMONE -> callback.triggerPheromoneEvent(ev.eventType, ev.posX, ev.posY, ev.posZ, ev.intensity, ev.radius, ev.durationMinutes);
                case INVASION -> callback.triggerInvasionEvent(ev.eventType, ev.posX, ev.posY, ev.posZ, ev.count, ev.durationMinutes);
                case MUTATION -> callback.applyGeneticBoost(colStr, ev.eventType, ev.intensity, ev.durationMinutes);
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
            javafx.application.Platform.runLater(() -> {
                sortScheduledEvents();
                scheduledEventsListView.refresh();
            });
        }
    }

    /**
     * Completely purges all scheduled God Mode events (used when creating a new simulation).
     */
    public void clearScheduledEvents() {
        scheduledEventsList.clear();
        currentSimulationTick = 0;
        if (scheduledEventsListView != null) {
            javafx.application.Platform.runLater(() -> scheduledEventsListView.refresh());
        }
    }

    /**
     * Resets execution flags on scheduled events so they can trigger again when rewinding to tick 0.
     */
    public void resetEventsState() {
        for (ScheduledEvent ev : scheduledEventsList) {
            ev.executed = false;
        }
        currentSimulationTick = 0;
        if (scheduledEventsListView != null) {
            javafx.application.Platform.runLater(() -> scheduledEventsListView.refresh());
        }
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

    public void setCallback(InterventionCallback callback) {
        this.callback = callback;
    }

    public void updateAvailableColonies(List<String> activeColonyNames) {
        if (activeColonyNames == null || activeColonyNames.isEmpty()) return;

        if (eventColonySelect != null) {
            List<String> items = new ArrayList<>();
            items.add("All Colonies (Global)");
            items.addAll(activeColonyNames);
            String selEv = eventColonySelect.getValue();
            eventColonySelect.getItems().setAll(items);
            if (selEv != null && eventColonySelect.getItems().contains(selEv)) {
                eventColonySelect.getSelectionModel().select(selEv);
            } else {
                eventColonySelect.getSelectionModel().selectFirst();
            }
        }

        updateCastesForSelectedColony(eventColonySelect != null ? eventColonySelect.getValue() : null);
    }

    private void updateCastesForSelectedColony(String colonyName) {
        if (casteSelect == null) return;

        casteSelect.getItems().clear();

        // Always include Brood options
        casteSelect.getItems().addAll(
                "Brood / Complex Brood (Eggs, Larvae, Pupae)",
                "Eggs / Spawned Eggs",
                "Larvae",
                "Pupae & Cocoons"
        );

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
