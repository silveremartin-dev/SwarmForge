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
import org.swarmforge.client.util.I18nManager;

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

        public String getDisplayName() {
            org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
            return switch (this) {
                case ENTITIES -> i18n.get("god.cat.entities");
                case RESOURCE -> i18n.get("god.cat.resource");
                case DISASTER -> i18n.get("god.cat.disaster");
                case ABIOTIC -> i18n.get("god.cat.abiotic");
                case PHEROMONE -> i18n.get("god.cat.pheromone");
                case INVASION -> i18n.get("god.cat.invasion");
                case MUTATION -> i18n.get("god.cat.mutation");
            };
        }
    }

    public static class ScheduledEvent extends InterventionScheduledEvent {
        public ScheduledEvent(long targetTick, String timeFormatted, Category category, String eventType, String colonyTarget, String description) {
            super(targetTick, timeFormatted, category, eventType, colonyTarget, description);
        }
    }

    Slider posXSlider, posYSlider, posZSlider;
    Label posXValLabel, posYValLabel, posZValLabel;

    ComboBox<String> eventColonySelect;
    Spinner<Integer> spDay, spHour, spMin, spSec;
    Label lblTargetTickSummary;
    Label simStateWarningLabel;

    long currentSimulationTick = 0;
    double simulationStepSec = 0.016666666666666666;

    InterventionCallback callback;
    final InterventionCategoryBuilder categoryBuilder = new InterventionCategoryBuilder(this);
    final InterventionEventScheduler scheduler = new InterventionEventScheduler(this, categoryBuilder);

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
        title.getStyleClass().addAll("header-title-large", "accent-danger-title");
        title.setTooltip(new Tooltip(i18n.get("god.title.tt")));

        simStateWarningLabel = new Label(i18n.get("god.warn.stopped"));
        simStateWarningLabel.getStyleClass().add("notice-warn");
        simStateWarningLabel.setMaxWidth(Double.MAX_VALUE);

        VBox header = new VBox(5, title, simStateWarningLabel);
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
        subBlocksOuterContainer.getChildren().add(createCardSubBlock("god.block.entities_manage", Feather.USERS, categoryBuilder.createEntitiesSubBlockNode()));

        // Sub-block 4: Resources & Biomass (With Complete Social Insect Food Taxonomy)
        subBlocksOuterContainer.getChildren().add(createCardSubBlock("god.block.resources", Feather.BOX, categoryBuilder.createResourcesSubBlockNode()));

        // Sub-block 5: Disasters & Environmental Events (Magnitude & Multi-Week Duration)
        subBlocksOuterContainer.getChildren().add(createCardSubBlock("god.block.disasters", Feather.ZAP, categoryBuilder.createDisastersSubBlockNode()));

        // Sub-block 6: Abiotic Physical Drivers (Relative Delta +/- & Duration Mode)
        subBlocksOuterContainer.getChildren().add(createCardSubBlock("god.block.abiotic_delta", Feather.THERMOMETER, categoryBuilder.createParametersSubBlockNode()));

        // Sub-block 7: Pheromones & Behavioral Disruption (Intensity & Duration)
        subBlocksOuterContainer.getChildren().add(createCardSubBlock("god.block.pheromones", Feather.WIND, categoryBuilder.createPheromoneSubBlockNode()));

        // Sub-block 8: Apex Predators & Biological Invasions (Intensity & Duration)
        subBlocksOuterContainer.getChildren().add(createCardSubBlock("god.block.invasions", Feather.SHIELD_OFF, categoryBuilder.createInvasionSubBlockNode()));

        // Sub-block 9: Genetics, Metabolic Boosts & Mutagens (Intensity & Duration)
        subBlocksOuterContainer.getChildren().add(createCardSubBlock("god.block.mutations", Feather.ACTIVITY, categoryBuilder.createMutationSubBlockNode()));

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
        return InterventionEventQueueCard.createQueueBlock(
                scheduledEventsListView,
                scheduledEventsList,
                () -> simulationStepSec,
                this::scrollToFirstUpcomingEvent
        );
    }

    void sortScheduledEvents() {
        InterventionEventQueueCard.sortScheduledEvents(scheduledEventsList);
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

        lblTargetTickSummary = new Label();
        lblTargetTickSummary.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px;");

        Runnable updateSummary = () -> {
            lblTargetTickSummary.setText(I18nManager.getInstance().get("god.spatial.target_summary",
                    spDay.getValue(), spHour.getValue(), spMin.getValue(), spSec.getValue(), eventColonySelect.getValue()));
        };
        updateSummary.run();

        spDay.valueProperty().addListener((o, a, b) -> updateSummary.run());
        spHour.valueProperty().addListener((o, a, b) -> updateSummary.run());
        spMin.valueProperty().addListener((o, a, b) -> updateSummary.run());
        spSec.valueProperty().addListener((o, a, b) -> updateSummary.run());
        eventColonySelect.valueProperty().addListener((o, a, b) -> updateSummary.run());

        box.getChildren().addAll(timeRow, lblTargetTickSummary);
        return box;
    }

    // Dynamic Terrain Dimensions & Terrarium Reference for Spatial Sliders
    double terrainWidthMeters = 64.0;
    double terrainLengthMeters = 64.0;
    double terrainDepthMeters = 5.0;
    double terrainMaxAltitudeMeters = 25.0;
    Label terrainDimensionsInfoLabel;
    CheckBox atSurfaceCheckBox;
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
            return terrariumRef.getSurfaceElevation((float) posX, (float) posY);
        }
        return 1.0f; // Default surface height
    }

    public void setTerrainDimensions(double widthMeters, double lengthMeters, double depthMeters, double maxAltitudeMeters) {
        Runnable action = () -> {
            this.terrainWidthMeters = Math.max(1.0, widthMeters);
            this.terrainLengthMeters = Math.max(1.0, lengthMeters);
            this.terrainDepthMeters = Math.max(0.1, depthMeters);
            this.terrainMaxAltitudeMeters = Math.max(1.0, maxAltitudeMeters);
            updateSpatialSliderRanges();
        };
        if (javafx.application.Platform.isFxApplicationThread()) {
            action.run();
        } else {
            javafx.application.Platform.runLater(action);
        }
    }

    public void setTerrainSize(double sizeMeters) {
        setTerrainDimensions(sizeMeters, sizeMeters, 5.0, 25.0);
    }

    public void updateSpatialSliderRanges() {
        Runnable action = () -> {
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
                terrainDimensionsInfoLabel.setText(I18nManager.getInstance().get("god.spatial.terrain_coverage",
                        terrainWidthMeters, terrainLengthMeters, terrainDepthMeters, terrainMaxAltitudeMeters));
            }
        };
        if (javafx.application.Platform.isFxApplicationThread()) {
            action.run();
        } else {
            javafx.application.Platform.runLater(action);
        }
    }

    private Node createSpatialPositionSubBlockNode() {
        return InterventionEventQueueCard.createSpatialPositionSubBlockNode(this);
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

    // â”€â”€ ATOMIC SCHEDULING & EXECUTION DELEGATION â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

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

    void scrollToFirstUpcomingEvent() {
        InterventionEventQueueCard.scrollToFirstUpcomingEvent(scheduledEventsListView, scheduledEventsList, currentSimulationTick);
    }

    ScheduledEvent createBaseEvent(Category category, String eventType, String desc) {
        long tSec = calculateTargetSeconds();
        long rawTargetTick = Math.max(1, Math.round(tSec / simulationStepSec));
        long targetTick = Math.max(currentSimulationTick, rawTargetTick);
        String timeFormatted = formatTickToCalendarTime(targetTick, simulationStepSec);
        String colTarget = eventColonySelect.getValue();
        return new ScheduledEvent(targetTick, timeFormatted, category, eventType, colTarget, desc);
    }

    void addAndSortScheduledEvent(ScheduledEvent ev) {
        scheduledEventsList.add(ev);
        sortScheduledEvents();
        scrollToFirstUpcomingEvent();
    }

    public void processScheduledEvents(long currentTick) {
        this.currentSimulationTick = currentTick;
        boolean needsRefresh = false;
        for (ScheduledEvent ev : scheduledEventsList) {
            if (ev.executed && currentTick < ev.targetTick) {
                ev.executed = false;
                needsRefresh = true;
            } else if (!ev.executed && !ev.paused && currentTick >= ev.targetTick) {
                ev.executed = true;
                scheduler.executeScheduledEvent(ev);
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

    public void clearScheduledEvents() {
        Runnable action = () -> {
            scheduledEventsList.clear();
            currentSimulationTick = 0;
            if (scheduledEventsListView != null) {
                scheduledEventsListView.refresh();
            }
        };
        if (javafx.application.Platform.isFxApplicationThread()) action.run();
        else javafx.application.Platform.runLater(action);
    }

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
        Runnable action = () -> {
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
        };
        if (javafx.application.Platform.isFxApplicationThread()) action.run();
        else javafx.application.Platform.runLater(action);
    }

    public void setCallback(InterventionCallback callback) {
        this.callback = callback;
    }

    public void updateAvailableColonies(List<String> activeColonyNames) {
        Runnable action = () -> {
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
            categoryBuilder.updateCastesForSelectedColony(eventColonySelect != null ? eventColonySelect.getValue() : null);
        };
        if (javafx.application.Platform.isFxApplicationThread()) action.run();
        else javafx.application.Platform.runLater(action);
    }
}