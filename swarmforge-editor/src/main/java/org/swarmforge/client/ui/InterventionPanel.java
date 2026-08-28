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
        ENTITIES("🐜", "Entités & Couvain", "#3b82f6"),
        RESOURCE("🍖", "Ressources", "#10b981"),
        DISASTER("🌊", "Catastrophe", "#ef4444"),
        ABIOTIC("🌡️", "Climat & Abiotique", "#f59e0b"),
        PHEROMONE("🌀", "Phéromones", "#8b5cf6"),
        INVASION("🦎", "Invasions", "#dc2626"),
        MUTATION("🧬", "Génétique", "#ec4899");

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
        public String entityAction = "SPAWN"; // SPAWN, KILL, EXTINCT, BROOD_KILL, BROOD_SPAWN
        public String caste = "Ouvrière";
        public int count = 10;
        // Default position: CENTER of map (32, 32) at terrain surface level (1.0)
        public float posX = 32.0f, posY = 32.0f, posZ = 1.0f;

        public String resourceType = "Surface Food";
        public String foodNature = "Graines & Semences";
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
    private Slider mutationIntensitySlider, mutationDurationSlider;
    private Label mutationIntensityValLabel, mutationDurationValLabel;

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

        // 2. OUTER MASTER CONTAINER: Event & Intervention Configuration Sub-blocks
        VBox subBlocksOuterContainer = new VBox(14);
        subBlocksOuterContainer.setPadding(new Insets(16));
        subBlocksOuterContainer.setStyle("-fx-background-color: rgba(15, 23, 42, 0.6); -fx-border-color: rgba(56, 189, 248, 0.35); -fx-border-width: 1.5; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label subBlocksHeader = new Label("🛠️ Panneau de Configuration des Événements & Interventions");
        subBlocksHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        subBlocksOuterContainer.getChildren().addAll(subBlocksHeader, new Separator());

        // Sub-block 1: Time Target & Colony Target
        subBlocksOuterContainer.getChildren().add(createCardSubBlock(i18n.get("god.block.time_colony"), createTimeAndColonyConfigNode()));

        // Sub-block 2: Spatial 3D Positioning (Centered on Terrain Surface by Default)
        subBlocksOuterContainer.getChildren().add(createCardSubBlock("📍 Localisation Spatiale 3D & Altitude du Substrat", createSpatialPositionSubBlockNode()));

        // Sub-block 3: Entities, Castes & Brood (Apparition / Élimination / Couvain)
        subBlocksOuterContainer.getChildren().add(createCardSubBlock("🐜 Entités, Castes & Gestion du Couvain", createEntitiesSubBlockNode()));

        // Sub-block 4: Resources & Biomass (With Complete Social Insect Food Taxonomy)
        subBlocksOuterContainer.getChildren().add(createCardSubBlock(i18n.get("god.block.resources"), createResourcesSubBlockNode()));

        // Sub-block 5: Disasters & Environmental Events (Magnitude & Multi-Week Duration)
        subBlocksOuterContainer.getChildren().add(createCardSubBlock(i18n.get("god.block.disasters"), createDisastersSubBlockNode()));

        // Sub-block 6: Abiotic Physical Drivers (Relative Delta +/- & Duration Mode)
        subBlocksOuterContainer.getChildren().add(createCardSubBlock("🌡️ Conditions Abiotiques (Variations Relatives Δ & Durée)", createParametersSubBlockNode()));

        // Sub-block 7: Pheromones & Behavioral Disruption (Intensity & Duration)
        subBlocksOuterContainer.getChildren().add(createCardSubBlock("🌀 Phéromones & Perturbations Comportementales", createPheromoneSubBlockNode()));

        // Sub-block 8: Apex Predators & Biological Invasions (Intensity & Duration)
        subBlocksOuterContainer.getChildren().add(createCardSubBlock("🦎 Invasions Écologiques & Prédateurs Apicaux", createInvasionSubBlockNode()));

        // Sub-block 9: Genetics, Metabolic Boosts & Mutagens (Intensity & Duration)
        subBlocksOuterContainer.getChildren().add(createCardSubBlock("🧬 Mutagènes, Gelée Royale & Boosts (Intensité & Durée)", createMutationSubBlockNode()));

        mainContent.getChildren().add(subBlocksOuterContainer);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        setCenter(scrollPane);
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

                    Label badgeCat = new Label(ev.category.icon + " " + ev.category.label);
                    badgeCat.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 3;", ev.category.color));

                    Label timeLabel = new Label("[" + ev.timeFormatted + "]");
                    timeLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px;");

                    Label descLabel = new Label(ev.eventType + (ev.colonyTarget != null ? " (" + ev.colonyTarget + ")" : "") + " : " + ev.description);
                    descLabel.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 11px;");
                    HBox.setHgrow(descLabel, Priority.ALWAYS);

                    String statusText = ev.executed ? "✅ Exécutée" : ev.paused ? "⏸️ Suspendue" : "⏳ En attente";
                    String statusBg = ev.executed ? "#16a34a" : ev.paused ? "#d97706" : "#0284c7";
                    Label statusBadge = new Label(statusText);
                    statusBadge.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 3;", statusBg));

                    cellBox.getChildren().addAll(badgeCat, timeLabel, descLabel, statusBadge);

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
        btnRepeatEv.setTooltip(new Tooltip("Duplique les événements sélectionnés décortiqués 10 secondes plus tard."));
        btnRepeatEv.setOnAction(e -> repeatSelectedEvents(10));

        Button btnDeleteEv = new Button();
        btnDeleteEv.textProperty().bind(i18n.createStringBinding("god.queue.btn.delete"));
        btnDeleteEv.getStyleClass().add("btn-danger");
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

        lblTargetTickSummary = new Label("🎯 Horodatage visé : Jour 1 08:00:00 — Colonie Cible: Toutes les Colonies (Global)");
        lblTargetTickSummary.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px;");

        Runnable updateSummary = () -> {
            lblTargetTickSummary.setText(String.format("🎯 Horodatage visé : Jour %d %02d:%02d:%02d — Colonie: %s",
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

    /**
     * Sub-block 2: Spatial 3D Positioning (Sliders X, Y, Z, Default Centered on Terrain Surface)
     */
    private Node createSpatialPositionSubBlockNode() {
        VBox box = new VBox(10);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);

        // X Slider (Ouest -> Est)
        Label lblX = new Label("Axe X (Ouest ◄► Est) :");
        lblX.setStyle("-fx-font-weight: bold;");
        posXSlider = new Slider(0.0, 64.0, 32.0);
        posXSlider.setPrefWidth(260);
        posXSlider.setMajorTickUnit(16.0);
        posXSlider.setMinorTickCount(3);
        posXSlider.setSnapToTicks(false);
        posXValLabel = new Label("32.0 m (Centre de la Carte)");
        posXValLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");
        posXSlider.valueProperty().addListener((o, a, b) -> {
            double v = b.doubleValue();
            String note = (Math.abs(v - 32.0) < 1.0) ? " (Centre)" : (v < 10.0) ? " (Extrême Ouest)" : (v > 54.0) ? " (Extrême Est)" : "";
            posXValLabel.setText(String.format("%.1f m%s", v, note));
        });

        // Y Slider (Sud -> Nord)
        Label lblY = new Label("Axe Y (Sud ◄► Nord) :");
        lblY.setStyle("-fx-font-weight: bold;");
        posYSlider = new Slider(0.0, 64.0, 32.0);
        posYSlider.setPrefWidth(260);
        posYSlider.setMajorTickUnit(16.0);
        posYSlider.setMinorTickCount(3);
        posYSlider.setSnapToTicks(false);
        posYValLabel = new Label("32.0 m (Centre de la Carte)");
        posYValLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");
        posYSlider.valueProperty().addListener((o, a, b) -> {
            double v = b.doubleValue();
            String note = (Math.abs(v - 32.0) < 1.0) ? " (Centre)" : (v < 10.0) ? " (Extrême Sud)" : (v > 54.0) ? " (Extrême Nord)" : "";
            posYValLabel.setText(String.format("%.1f m%s", v, note));
        });

        // Z Slider (Profondeur -> Surface -> Altitude)
        Label lblZ = new Label("Altitude Z (Hauteur) :");
        lblZ.setStyle("-fx-font-weight: bold;");
        posZSlider = new Slider(-5.0, 25.0, 1.0);
        posZSlider.setPrefWidth(260);
        posZSlider.setMajorTickUnit(5.0);
        posZSlider.setMinorTickCount(4);
        posZSlider.setSnapToTicks(false);
        posZValLabel = new Label("1.0 m (Surface du Terrain)");
        posZValLabel.setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold;");
        posZSlider.valueProperty().addListener((o, a, b) -> {
            double v = b.doubleValue();
            String desc = (v < 0.0) ? String.format("%.1f m (Sous-sol Deep)", v) : (Math.abs(v - 1.0) < 0.5) ? "1.0 m (Surface du Terrain)" : (v <= 5.0) ? String.format("%.1f m (Bas Relief)", v) : String.format("%.1f m (Haute Canopée / Air)", v);
            posZValLabel.setText(desc);
            posZValLabel.setStyle(v < 0.0 ? "-fx-text-fill: #f59e0b; -fx-font-weight: bold;" : v > 5.0 ? "-fx-text-fill: #c084fc; -fx-font-weight: bold;" : "-fx-text-fill: #4ade80; -fx-font-weight: bold;");
        });

        grid.add(lblX, 0, 0); grid.add(posXSlider, 1, 0); grid.add(posXValLabel, 2, 0);
        grid.add(lblY, 0, 1); grid.add(posYSlider, 1, 1); grid.add(posYValLabel, 2, 1);
        grid.add(lblZ, 0, 2); grid.add(posZSlider, 1, 2); grid.add(posZValLabel, 2, 2);

        Button btnResetCenter = new Button("📍 Recentrer au Centre de la Carte (Surface : X=32, Y=32, Z=1)");
        btnResetCenter.setStyle("-fx-background-color: rgba(56,189,248,0.15); -fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-border-color: #38bdf8; -fx-border-radius: 4; -fx-cursor: hand;");
        btnResetCenter.setOnAction(e -> {
            posXSlider.setValue(32.0);
            posYSlider.setValue(32.0);
            posZSlider.setValue(1.0);
        });

        box.getChildren().addAll(grid, btnResetCenter);
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
        return String.format("J%d %02d:%02d:%02d", d, h, m, s);
    }

    /**
     * Sub-block 3: Entities, Castes & Brood Management
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
        colonySelect.setPrefWidth(240);
        colonySelect.setOnAction(e -> updateCastesForSelectedColony(colonySelect.getValue()));

        Label lblAction = new Label("Type d'Action :");
        lblAction.setStyle("-fx-font-weight: bold;");
        entityActionSelect = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(
                "➕ Injection / Apparition (Adulte / Couvain)",
                "☠️ Élimination Ciblée d'Individus",
                "🐣 Élimination / Dépuration du Couvain",
                "💀 Extinction Totale de la Colonie"
        ));
        entityActionSelect.getSelectionModel().selectFirst();
        entityActionSelect.setPrefWidth(260);

        Label casteLabel = new Label("Caste / Couvain Cible :");
        casteLabel.setStyle("-fx-font-weight: bold;");
        casteSelect = new ComboBox<>();
        casteSelect.setPrefWidth(260);
        updateCastesForSelectedColony(colonySelect.getValue());

        Label countLabel = new Label("Quantité / Nombre d'Individus :");
        countLabel.setStyle("-fx-font-weight: bold;");
        antCountSpinner = new Spinner<>(1, 1000, 10);
        antCountSpinner.setEditable(true);
        antCountSpinner.setPrefWidth(120);

        grid.add(colLabel, 0, 0); grid.add(colonySelect, 1, 0);
        grid.add(lblAction, 0, 1); grid.add(entityActionSelect, 1, 1);
        grid.add(casteLabel, 0, 2); grid.add(casteSelect, 1, 2);
        grid.add(countLabel, 0, 3); grid.add(antCountSpinner, 1, 3);

        HBox actionBtnRow = new HBox(8);
        Button btnScheduleEntities = new Button();
        btnScheduleEntities.textProperty().bind(i18n.createStringBinding("god.btn.schedule_entities"));
        btnScheduleEntities.getStyleClass().add("btn-primary");
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
     * Sub-block 6: Abiotic Physical Drivers with Relative Delta (+/-) & Duration
     */
    private Node createParametersSubBlockNode() {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        VBox box = new VBox(10);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);

        Label lblMode = new Label("Mode d'Application Climat :");
        lblMode.setStyle("-fx-font-weight: bold;");
        abioticModeSelect = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(
                "📈 Variation Relative (Δ + / - par rapport au Climat Courant)",
                "🎯 Valeur Absolue Fixe (Substitut au Climat Courant)"
        ));
        abioticModeSelect.getSelectionModel().selectFirst();
        abioticModeSelect.setPrefWidth(340);

        Label lblDuration = new Label("Durée d'Application du Climat :");
        lblDuration.setStyle("-fx-font-weight: bold;");
        abioticDurationSlider = new Slider(5, 43200, 60);
        abioticDurationSlider.setPrefWidth(200);
        abioticDurationValLabel = new Label("60 min (1h)");
        abioticDurationValLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
        abioticDurationSlider.valueProperty().addListener((o, a, b) -> {
            int mins = b.intValue();
            abioticDurationValLabel.setText(mins < 60 ? mins + " min" : (mins < 1440) ? (mins / 60) + "h " + (mins % 60) + "m" : (mins / 1440) + " Jours");
        });

        // Relative Temp Delta (-20°C to +20°C)
        Label lblTemp = new Label("Variation Température (Δ °C) :");
        lblTemp.setStyle("-fx-font-weight: bold;");
        deltaTempSlider = new Slider(-20.0, 20.0, 5.0);
        deltaTempSlider.setPrefWidth(200);
        deltaTempValLabel = new Label("+5.0 °C (Réchauffement)");
        deltaTempValLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        deltaTempSlider.valueProperty().addListener((o, a, b) -> {
            double v = b.doubleValue();
            deltaTempValLabel.setText(String.format("%+.1f °C (%s)", v, v > 0 ? "Réchauffement" : v < 0 ? "Refroidissement" : "Inchangé"));
            deltaTempValLabel.setStyle(v > 0 ? "-fx-text-fill: #ef4444; -fx-font-weight: bold;" : v < 0 ? "-fx-text-fill: #38bdf8; -fx-font-weight: bold;" : "-fx-text-fill: #94a3b8; -fx-font-weight: bold;");
            updateDerivedPhysicalOutputs();
        });

        // Relative Humidity Delta (-50% to +50%)
        Label lblHumidity = new Label("Variation Humidité (Δ %) :");
        lblHumidity.setStyle("-fx-font-weight: bold;");
        deltaHumiditySlider = new Slider(-50.0, 50.0, 20.0);
        deltaHumiditySlider.setPrefWidth(200);
        deltaHumidityValLabel = new Label("+20.0 % (Humidification)");
        deltaHumidityValLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");
        deltaHumiditySlider.valueProperty().addListener((o, a, b) -> {
            double v = b.doubleValue();
            deltaHumidityValLabel.setText(String.format("%+.1f %% (%s)", v, v > 0 ? "Humidification" : v < 0 ? "Assèchement" : "Inchangé"));
            updateDerivedPhysicalOutputs();
        });

        // Relative Wind Delta (-10 to +15 m/s)
        Label lblWind = new Label("Variation Vent (Δ m/s) :");
        lblWind.setStyle("-fx-font-weight: bold;");
        deltaWindSlider = new Slider(-10.0, 15.0, 2.0);
        deltaWindSlider.setPrefWidth(200);
        deltaWindValLabel = new Label("+2.0 m/s");
        deltaWindValLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");
        deltaWindSlider.valueProperty().addListener((o, a, b) -> {
            deltaWindValLabel.setText(String.format("%+.1f m/s", b.doubleValue()));
            updateDerivedPhysicalOutputs();
        });

        // Relative Solar Delta (-500 to +500 W/m²)
        Label lblSolar = new Label("Variation Radiance (Δ W/m²) :");
        lblSolar.setStyle("-fx-font-weight: bold;");
        deltaSolarSlider = new Slider(-500.0, 500.0, 100.0);
        deltaSolarSlider.setPrefWidth(200);
        deltaSolarValLabel = new Label("+100 W/m²");
        deltaSolarValLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");
        deltaSolarSlider.valueProperty().addListener((o, a, b) -> {
            deltaSolarValLabel.setText(String.format("%+.0f W/m²", b.doubleValue()));
            updateDerivedPhysicalOutputs();
        });

        grid.add(lblMode, 0, 0); grid.add(abioticModeSelect, 1, 0);
        grid.add(lblDuration, 0, 1); grid.add(abioticDurationSlider, 1, 1); grid.add(abioticDurationValLabel, 2, 1);
        grid.add(lblTemp, 0, 2); grid.add(deltaTempSlider, 1, 2); grid.add(deltaTempValLabel, 2, 2);
        grid.add(lblHumidity, 0, 3); grid.add(deltaHumiditySlider, 1, 3); grid.add(deltaHumidityValLabel, 2, 3);
        grid.add(lblWind, 0, 4); grid.add(deltaWindSlider, 1, 4); grid.add(deltaWindValLabel, 2, 4);
        grid.add(lblSolar, 0, 5); grid.add(deltaSolarSlider, 1, 5); grid.add(deltaSolarValLabel, 2, 5);

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

        actionBtnRow.getChildren().add(btnScheduleAbiotic);

        box.getChildren().addAll(grid, derivedBox, actionBtnRow);
        return box;
    }

    /**
     * Sub-block 7: Pheromone & Behavioral Perturbations (With Intensity & Duration)
     */
    private Node createPheromoneSubBlockNode() {
        VBox box = new VBox(10);
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);

        Label lblType = new Label("Type d'Événement Phéromonal :");
        lblType.setStyle("-fx-font-weight: bold;");
        pheromoneTypeSelect = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(
                "Tempête de Phéromone d'Alarme (Alarm Storm)",
                "Dissolution des Pistes d'Exploration (Trail Eraser)",
                "Attracteur Synthétique de Reine (Fake Queen Beacon)",
                "Phéromone d'Attraction de Biomasse (Attractant Pulse)"
        ));
        pheromoneTypeSelect.getSelectionModel().selectFirst();
        pheromoneTypeSelect.setPrefWidth(300);

        Label lblRadius = new Label("Rayon d'Action (m) :");
        lblRadius.setStyle("-fx-font-weight: bold;");
        pheroRadiusSlider = new Slider(1.0, 30.0, 10.0);
        pheroRadiusSlider.setPrefWidth(180);
        pheroRadiusValLabel = new Label("10.0 m");
        pheroRadiusValLabel.setStyle("-fx-text-fill: #8b5cf6; -fx-font-weight: bold;");
        pheroRadiusSlider.valueProperty().addListener((o, a, b) -> pheroRadiusValLabel.setText(String.format("%.1f m", b.doubleValue())));

        Label lblIntensity = new Label("Intensité / Concentration :");
        lblIntensity.setStyle("-fx-font-weight: bold;");
        pheroIntensitySlider = new Slider(0.1, 1.0, 0.8);
        pheroIntensitySlider.setPrefWidth(180);
        pheroIntensityValLabel = new Label("Forte (0.8)");
        pheroIntensityValLabel.setStyle("-fx-text-fill: #8b5cf6; -fx-font-weight: bold;");
        pheroIntensitySlider.valueProperty().addListener((o, a, b) -> pheroIntensityValLabel.setText(String.format("%.1f", b.doubleValue())));

        Label lblDuration = new Label("Durée de l'Événement (min) :");
        lblDuration.setStyle("-fx-font-weight: bold;");
        pheroDurationSlider = new Slider(5, 1440, 30);
        pheroDurationSlider.setPrefWidth(180);
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

        Button btnSchedulePhero = new Button("➕ Programmer l'Impulsion Phéromonale");
        btnSchedulePhero.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnSchedulePhero.setOnAction(e -> schedulePheromoneEvent());

        box.getChildren().addAll(grid, btnSchedulePhero);
        return box;
    }

    /**
     * Sub-block 8: Apex Predators & Biological Invasions (With Count & Duration)
     */
    private Node createInvasionSubBlockNode() {
        VBox box = new VBox(10);
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);

        Label lblType = new Label("Événement d'Invasion / Prédateur :");
        lblType.setStyle("-fx-font-weight: bold;");
        invasionTypeSelect = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(
                "Attaque de Tamandua / Fourmilier (Anteater Raid)",
                "Raid de Fourmis Légionnaires (Army Ant Raid)",
                "Invasion de Parasites Mites / Varroa (Parasitic Mite Infestation)",
                "Infestation Fringale de Sauterelles (Locust Swarm)"
        ));
        invasionTypeSelect.getSelectionModel().selectFirst();
        invasionTypeSelect.setPrefWidth(300);

        Label lblCount = new Label("Nombre / Gravité d'Individus :");
        lblCount.setStyle("-fx-font-weight: bold;");
        invasionCountSpinner = new Spinner<>(1, 500, 25);
        invasionCountSpinner.setEditable(true);
        invasionCountSpinner.setPrefWidth(120);

        Label lblDuration = new Label("Durée de la Pression (min) :");
        lblDuration.setStyle("-fx-font-weight: bold;");
        invasionDurationSlider = new Slider(15, 43200, 120);
        invasionDurationSlider.setPrefWidth(180);
        invasionDurationValLabel = new Label("2h (120 min)");
        invasionDurationValLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
        invasionDurationSlider.valueProperty().addListener((o, a, b) -> {
            int mins = b.intValue();
            invasionDurationValLabel.setText(mins < 60 ? mins + " min" : (mins < 1440) ? (mins / 60) + "h " + (mins % 60) + "m" : (mins / 1440) + " Jours");
        });

        grid.add(lblType, 0, 0); grid.add(invasionTypeSelect, 1, 0);
        grid.add(lblCount, 0, 1); grid.add(invasionCountSpinner, 1, 1);
        grid.add(lblDuration, 0, 2); grid.add(invasionDurationSlider, 1, 2); grid.add(invasionDurationValLabel, 2, 2);

        Button btnScheduleInvasion = new Button("➕ Programmer l'Invasion Écologique");
        btnScheduleInvasion.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnScheduleInvasion.setOnAction(e -> scheduleInvasionEvent());

        box.getChildren().addAll(grid, btnScheduleInvasion);
        return box;
    }

    /**
     * Sub-block 9: Genetics, Metabolic Boosts & Mutagens (With Intensity & Duration)
     */
    private Node createMutationSubBlockNode() {
        VBox box = new VBox(10);
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);

        Label lblType = new Label("Stimulation Génétique / Mutagène :");
        lblType.setStyle("-fx-font-weight: bold;");
        mutationTypeSelect = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(
                "Suralimentation en Gelée Royale (Boost Oviposition Reine)",
                "Frénésie & Métabolisme (+50% Vitesse/Attaque)",
                "Longévité Biologique (Protection Contre la Senescence)",
                "Anarchie & Perturbation des Castes (Changement de Rôle)"
        ));
        mutationTypeSelect.getSelectionModel().selectFirst();
        mutationTypeSelect.setPrefWidth(320);

        Label lblIntensity = new Label("Facteur d'Intensité / Multiplicateur :");
        lblIntensity.setStyle("-fx-font-weight: bold;");
        mutationIntensitySlider = new Slider(1.1, 5.0, 2.0);
        mutationIntensitySlider.setPrefWidth(180);
        mutationIntensityValLabel = new Label("2.0x (+200%)");
        mutationIntensityValLabel.setStyle("-fx-text-fill: #ec4899; -fx-font-weight: bold;");
        mutationIntensitySlider.valueProperty().addListener((o, a, b) -> mutationIntensityValLabel.setText(String.format("%.1fx", b.doubleValue())));

        Label lblDur = new Label("Durée d'Effet :");
        lblDur.setStyle("-fx-font-weight: bold;");
        mutationDurationSlider = new Slider(5, 1440, 60);
        mutationDurationSlider.setPrefWidth(180);
        mutationDurationValLabel = new Label("60 min (1h)");
        mutationDurationValLabel.setStyle("-fx-text-fill: #ec4899; -fx-font-weight: bold;");
        mutationDurationSlider.valueProperty().addListener((o, a, b) -> {
            int mins = b.intValue();
            mutationDurationValLabel.setText(mins < 60 ? mins + " min" : (mins / 60) + "h " + (mins % 60) + "m");
        });

        grid.add(lblType, 0, 0); grid.add(mutationTypeSelect, 1, 0);
        grid.add(lblIntensity, 0, 1); grid.add(mutationIntensitySlider, 1, 1); grid.add(mutationIntensityValLabel, 2, 1);
        grid.add(lblDur, 0, 2); grid.add(mutationDurationSlider, 1, 2); grid.add(mutationDurationValLabel, 2, 2);

        Button btnScheduleMutation = new Button("➕ Programmer la Stimulation Génétique");
        btnScheduleMutation.setStyle("-fx-background-color: #ec4899; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnScheduleMutation.setOnAction(e -> scheduleMutationEvent());

        box.getChildren().addAll(grid, btnScheduleMutation);
        return box;
    }

    private void updateDerivedPhysicalOutputs() {
        if (deltaTempSlider == null) return;

        double temp = deltaTempSlider.getValue();
        double hum = deltaHumiditySlider.getValue();
        double wind = deltaWindSlider.getValue();
        double solar = deltaSolarSlider.getValue();

        derivedPheroLabel.setText(String.format("• Delta Climat : ΔT = %+.1f°C | ΔHumidité = %+.1f%% | ΔVent = %+.1fm/s", temp, hum, wind));
        derivedPrimaryProductivityLabel.setText(String.format("• Radiance Solaire Relative : %+.0f W/m² (Durée : %d min)", solar, (int) abioticDurationSlider.getValue()));
        derivedOvipositionLabel.setText("• Impact Thermique estimé sur l'activité du Nid : " + (temp > 0 ? "Accélération Métabolique" : temp < 0 ? "Ralentissement / Léthargie" : "Stabilité"));
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
        String colTarget = colonySelect.getValue();
        String actionStr = entityActionSelect.getValue();
        String caste = casteSelect.getValue();
        int count = antCountSpinner.getValue();
        float x = (float) posXSlider.getValue();
        float y = (float) posYSlider.getValue();
        float z = (float) posZSlider.getValue();

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
        float z = (float) posZSlider.getValue();

        ScheduledEvent ev = createBaseEvent(Category.RESOURCE, resType,
                String.format("Apport %.0f u (%s) - %s à (%.1f, %.1f, %.1f)", qty, resType, foodNat, x, y, z));
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
        boolean isRel = abioticModeSelect.getValue().contains("Relative");
        int durMins = (int) abioticDurationSlider.getValue();
        float dTemp = (float) deltaTempSlider.getValue();
        float dHum = (float) deltaHumiditySlider.getValue();
        float dWind = (float) deltaWindSlider.getValue();
        float dSolar = (float) deltaSolarSlider.getValue();

        String desc = isRel ?
                String.format("Δ Climat (%d min) : ΔT=%+.1f°C, ΔH=%+.0f%%, ΔVent=%+.1fm/s, ΔSol=%+.0fW/m²", durMins, dTemp, dHum, dWind, dSolar) :
                String.format("Climat Absolu (%d min)", durMins);

        ScheduledEvent ev = createBaseEvent(Category.ABIOTIC, "Variation Climat", desc);
        ev.isRelativeAbiotic = isRel;
        ev.durationMinutes = durMins;
        ev.deltaTempCelsius = dTemp;
        ev.deltaHumidityPercent = dHum;
        ev.deltaWindMetersPerSec = dWind;
        ev.deltaSolarWattsPerM2 = dSolar;

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
        float z = (float) posZSlider.getValue();

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
        int count = invasionCountSpinner.getValue();
        int durMins = (int) invasionDurationSlider.getValue();
        float x = (float) posXSlider.getValue();
        float y = (float) posYSlider.getValue();
        float z = (float) posZSlider.getValue();

        ScheduledEvent ev = createBaseEvent(Category.INVASION, type,
                String.format("%s (%d individus, Durée: %d min) à (%.1f, %.1f, %.1f)", type, count, durMins, x, y, z));
        ev.count = count;
        ev.durationMinutes = durMins;
        ev.posX = x; ev.posY = y; ev.posZ = z;

        scheduledEventsList.add(ev);
        sortScheduledEvents();
        scrollToFirstUpcomingEvent();
    }

    private void scheduleMutationEvent() {
        String type = mutationTypeSelect.getValue();
        float intensity = (float) mutationIntensitySlider.getValue();
        int durMins = (int) mutationDurationSlider.getValue();

        ScheduledEvent ev = createBaseEvent(Category.MUTATION, type,
                String.format("%s (Intensité: %.1fx, Durée: %d min)", type, intensity, durMins));
        ev.intensity = intensity;
        ev.durationMinutes = durMins;

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
            List<String> items = new ArrayList<>();
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

        // Always include Brood options
        casteSelect.getItems().addAll(
                "Brood / Couvain Complexe (Œufs, Larves, Nymphes)",
                "Eggs / Œufs Frayés",
                "Larvae / Larves",
                "Pupae / Nymphes & Cocons"
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
