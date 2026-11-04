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
    final InterventionTimeConfigHelper timeConfigHelper = new InterventionTimeConfigHelper(this);
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
    private final ListView<ScheduledEvent> scheduledEventsListView = new ListView<>(scheduledEventsList);

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
        return timeConfigHelper.createTimeAndColonyConfigNode();
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
        return timeConfigHelper.calculateTargetSeconds();
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

    private void syncTimePickersWithCurrentTick(int offsetSeconds) {
        timeConfigHelper.syncTimePickersWithCurrentTick(offsetSeconds);
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
                simStateWarningLabel.getStyleClass().removeAll("notice-warn", "notice-success");
                if (!running) {
                    simStateWarningLabel.setText(i18n.get("god.warn.paused"));
                    simStateWarningLabel.getStyleClass().add("notice-warn");
                } else {
                    simStateWarningLabel.setText(i18n.get("god.warn.running"));
                    simStateWarningLabel.getStyleClass().add("notice-success");
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