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

/**
 * Intervention Panel - God Mode controls for runtime simulation manipulation.
 * Features an overarching Scheduled Event Queue system, comprehensive food taxonomy,
 * multi-day disaster duration scaling, and direct manipulation of fundamental abiotic physical drivers.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class InterventionPanel extends BorderPane {

    private Spinner<Integer> antCountSpinner;
    private ComboBox<String> casteSelect;
    private ComboBox<String> colonySelect;
    private TextField posXField, posYField, posZField;
    private ComboBox<String> disasterSelect;
    private Slider intensitySlider;
    private Slider durationSlider;
    private Label durationValLabel;
    private TextArea logArea;

    private ComboBox<String> eventColonySelect;
    private Spinner<Integer> spDay, spHour, spMin, spSec;

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

    public static class ScheduledEvent {
        public long targetTick;
        public String timeFormatted;
        public String category; // ENTITIES, RESOURCE, DISASTER, PARAMETERS
        public String eventType;
        public String colonyTarget;
        public String description;
        public int count = 10;
        public String caste = "Ouvrière";
        public float posX = 1.0f, posY = 1.0f, posZ = 0.1f;
        public float amount = 100f;
        public String foodNature = "Graines & Semences";
        public float intensity = 0.5f;
        public int durationMinutes = 60;
        public float tempCelsius = 22.0f;
        public float humidityPercent = 65.0f;
        public float windMetersPerSec = 1.5f;
        public float solarWattsPerM2 = 450.0f;
        public boolean executed = false;
        public boolean paused = false;

        public ScheduledEvent(long targetTick, String timeFormatted, String category, String eventType, String colonyTarget, String description) {
            this.targetTick = targetTick;
            this.timeFormatted = timeFormatted;
            this.category = category;
            this.eventType = eventType;
            this.colonyTarget = colonyTarget;
            this.description = description;
        }
    }

    private final javafx.collections.ObservableList<ScheduledEvent> scheduledEventsList = javafx.collections.FXCollections.observableArrayList();
    private ListView<String> scheduledEventsListView;

    public InterventionPanel() {
        setPadding(new Insets(15));

        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();

        // Title
        Label title = new Label();
        title.textProperty().bind(i18n.createStringBinding("god.title"));
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ff6b6b;");
        title.setTooltip(new Tooltip("Contrôles d'intervention directe en mode divin pour modifier la simulation en cours de fonctionnement."));

        simStateWarningLabel = new Label("⚠️ Simulation arrêtée — Démarrez la simulation (▶) pour exécuter des interventions en Mode Divin.");
        simStateWarningLabel.setStyle("-fx-text-fill: #f87171; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-color: rgba(239,68,68,0.15); -fx-padding: 6 10; -fx-background-radius: 4;");
        simStateWarningLabel.setMaxWidth(Double.MAX_VALUE);

        Label persistenceNoticeLabel = new Label("⚡ Horodatage & Physique Abiotique : Toutes vos interventions sont inscrites dans le continuum spatio-temporel du monde. La dissipation chimique et la productivité sont dérivées déterministement des drivers physiques (Température, Humidité, Vent, Radiance).");
        persistenceNoticeLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 10px; -fx-background-color: rgba(56,189,248,0.1); -fx-padding: 6 10; -fx-background-radius: 4; -fx-border-color: rgba(56,189,248,0.3); -fx-border-width: 1;");
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
        Label subBlocksHeader = new Label("🛠️ Sous-blocs de Configuration des Événements & Interventions :");
        subBlocksHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #f59e0b; -fx-font-size: 13px;");
        mainContent.getChildren().add(subBlocksHeader);

        // Sub-block 1: Time Target (Calendar Time Only) & Colony Target
        mainContent.getChildren().add(createCardSubBlock("📅 Horodatage Cible (Jour / HH:MM:SS) & Colonie Affectée", createTimeAndColonyConfigNode()));

        // Sub-block 2: Entities & Castes (Apparition / Élimination / Extinction)
        mainContent.getChildren().add(createCardSubBlock("🐜 Entités & Castes (Apparition / Élimination / Extinction)", createEntitiesSubBlockNode()));

        // Sub-block 3: Resources & Biomass (With Complete Social Insect Food Taxonomy)
        mainContent.getChildren().add(createCardSubBlock("🍖 Ressources & Biomasse (Taxonomie Complète des Insectes Sociaux)", createResourcesSubBlockNode()));

        // Sub-block 4: Disasters & Environmental Events (Magnitude & Multi-Week Duration)
        mainContent.getChildren().add(createCardSubBlock("🌊 Catastrophes & Climat (Magnitude & Échelle Multi-Semaines)", createDisastersSubBlockNode()));

        // Sub-block 5: Abiotic Core Physical Drivers (Temperature, Humidity, Wind, Solar Radiation)
        mainContent.getChildren().add(createCardSubBlock("🌡️ Conditions Abiotiques & Microclimat Sol/Air (Moteur Physico-Chimique)", createParametersSubBlockNode()));

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
        card.setStyle("-fx-background-color: #18181b; -fx-border-color: #27272a; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
        Label title = new Label(titleStr);
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #38bdf8; -fx-font-size: 12px;");
        card.getChildren().addAll(title, new Separator(), contentNode);
        return card;
    }

    /**
     * Overarching container holding the Scheduled Event Queue Table and master operation buttons.
     */
    private VBox createScheduledEventsQueueBlock() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: rgba(2, 132, 199, 0.08); -fx-border-color: #0284c7; -fx-border-width: 1.5; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label lblQueueTitle = new Label("⏱️ File d'Événements Programmés (Master Event Queue)");
        lblQueueTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #38bdf8; -fx-font-size: 14px;");

        scheduledEventsListView = new ListView<>();
        scheduledEventsListView.setPrefHeight(130);
        scheduledEventsListView.setStyle("-fx-control-inner-background: #0f172a; -fx-text-fill: #e4e4e7; -fx-font-family: monospace;");

        // Action Toolbar Buttons
        HBox btnToolbar = new HBox(8);
        btnToolbar.setAlignment(Pos.CENTER_LEFT);

        Button btnScheduleConfigured = new Button("➕ Programmer l'Événement Configuré Ci-Dessous");
        btnScheduleConfigured.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnScheduleConfigured.setOnAction(e -> scheduleCurrentConfiguredEvent());

        Button btnExecuteLive = new Button("⚡ Exécuter Immédiatement (En Direct)");
        btnExecuteLive.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnExecuteLive.setOnAction(e -> executeCurrentConfiguredEventLive());

        Button btnDeleteEv = new Button("🗑️ Supprimer");
        btnDeleteEv.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand;");
        btnDeleteEv.setOnAction(e -> {
            int idx = scheduledEventsListView.getSelectionModel().getSelectedIndex();
            if (idx >= 0 && idx < scheduledEventsList.size()) {
                scheduledEventsList.remove(idx);
                refreshScheduledEventsListView();
            }
        });

        Button btnRunNow = new Button("▶️ Exécuter Sélectionné");
        btnRunNow.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-cursor: hand;");
        btnRunNow.setOnAction(e -> {
            int idx = scheduledEventsListView.getSelectionModel().getSelectedIndex();
            if (idx >= 0 && idx < scheduledEventsList.size()) {
                ScheduledEvent ev = scheduledEventsList.get(idx);
                executeScheduledEvent(ev);
                ev.executed = true;
                refreshScheduledEventsListView();
            }
        });

        Button btnTogglePause = new Button("⏸️ Pauser/Réactiver");
        btnTogglePause.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-cursor: hand;");
        btnTogglePause.setOnAction(e -> {
            int idx = scheduledEventsListView.getSelectionModel().getSelectedIndex();
            if (idx >= 0 && idx < scheduledEventsList.size()) {
                ScheduledEvent ev = scheduledEventsList.get(idx);
                ev.paused = !ev.paused;
                refreshScheduledEventsListView();
            }
        });

        btnToolbar.getChildren().addAll(btnScheduleConfigured, btnExecuteLive, btnDeleteEv, btnRunNow, btnTogglePause);

        box.getChildren().addAll(lblQueueTitle, scheduledEventsListView, btnToolbar);
        return box;
    }

    /**
     * Sub-block 1: Calendar Time Input (Jour / Heure / Minute / Seconde) & Colony Target
     */
    private Node createTimeAndColonyConfigNode() {
        VBox box = new VBox(8);

        HBox timeRow = new HBox(8);
        timeRow.setAlignment(Pos.CENTER_LEFT);

        Label lblTimeHeader = new Label("Date & Heure Cibles :");
        lblTimeHeader.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        spDay = new Spinner<>(1, 365, 1); spDay.setEditable(true); spDay.setPrefWidth(65);
        spHour = new Spinner<>(0, 23, 8); spHour.setEditable(true); spHour.setPrefWidth(60);
        spMin = new Spinner<>(0, 59, 0); spMin.setEditable(true); spMin.setPrefWidth(60);
        spSec = new Spinner<>(0, 59, 0); spSec.setEditable(true); spSec.setPrefWidth(60);

        Label lblColonyTarget = new Label("Colonie Cible :");
        lblColonyTarget.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        eventColonySelect = new ComboBox<>();
        eventColonySelect.getItems().addAll("Toutes les Colonies (Global)", "Colonie #1 (Lasius niger)", "Colonie #2 (Atta sexdens)");
        eventColonySelect.getSelectionModel().selectFirst();
        eventColonySelect.setPrefWidth(210);

        timeRow.getChildren().addAll(
                lblTimeHeader,
                new Label("Jour:"), spDay,
                new Label("H:"), spHour,
                new Label("M:"), spMin,
                new Label("S:"), spSec,
                lblColonyTarget, eventColonySelect
        );

        box.getChildren().add(timeRow);
        return box;
    }

    /**
     * Sub-block 2: Entities & Castes (Apparition / Injection & Élimination / Extinction)
     */
    private Node createEntitiesSubBlockNode() {
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);

        Label colLabel = new Label("Colonie Cible :");
        colLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        colonySelect = new ComboBox<>();
        colonySelect.getItems().addAll("Colonie #1 (Lasius niger)", "Colonie #2 (Atta sexdens)");
        colonySelect.getSelectionModel().selectFirst();
        colonySelect.setPrefWidth(220);
        colonySelect.setOnAction(e -> updateCastesForSelectedColony(colonySelect.getValue()));

        Label lblAction = new Label("Type d'Action :");
        lblAction.setStyle("-fx-text-fill: white;");
        ComboBox<String> actionCombo = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(
                "➕ Injection / Apparition",
                "☠️ Élimination Ciblée",
                "💀 Extinction Totale de la Colonie"
        ));
        actionCombo.getSelectionModel().selectFirst();
        actionCombo.setPrefWidth(220);

        Label casteLabel = new Label("Caste Cible :");
        casteLabel.setStyle("-fx-text-fill: white;");
        casteSelect = new ComboBox<>();
        casteSelect.setPrefWidth(220);
        updateCastesForSelectedColony(colonySelect.getValue());

        Label countLabel = new Label("Quantité :");
        countLabel.setStyle("-fx-text-fill: white;");
        antCountSpinner = new Spinner<>(1, 1000, 10);
        antCountSpinner.setEditable(true);
        antCountSpinner.setPrefWidth(100);

        Label posLabel = new Label("Position (m) :");
        posLabel.setStyle("-fx-text-fill: white;");
        HBox posBox = new HBox(5);
        posXField = new TextField("1.0"); posXField.setPrefWidth(55);
        posYField = new TextField("1.0"); posYField.setPrefWidth(55);
        posZField = new TextField("0.1"); posZField.setPrefWidth(55);
        posBox.getChildren().addAll(new Label("X:"), posXField, new Label("Y:"), posYField, new Label("Z:"), posZField);

        grid.add(colLabel, 0, 0); grid.add(colonySelect, 1, 0);
        grid.add(lblAction, 0, 1); grid.add(actionCombo, 1, 1);
        grid.add(casteLabel, 0, 2); grid.add(casteSelect, 1, 2);
        grid.add(countLabel, 0, 3); grid.add(antCountSpinner, 1, 3);
        grid.add(posLabel, 0, 4); grid.add(posBox, 1, 4);

        return grid;
    }

    /**
     * Sub-block 3: Resources & Biomass with Taxonomy for Ants, Bees, Wasps, and Termites
     */
    private Node createResourcesSubBlockNode() {
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);

        Label lblResType = new Label("Type de Ressource :");
        lblResType.setStyle("-fx-text-fill: white;");
        resourceTypeSelect = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(
                "🍖 Biomasse / Nourriture de Surface",
                "💧 Source d'Eau Douce",
                "🧹 Purge de la Nourriture Libre"
        ));
        resourceTypeSelect.getSelectionModel().selectFirst();
        resourceTypeSelect.setPrefWidth(220);

        Label lblNature = new Label("Nature & Taxonomie Alimentaire :");
        lblNature.setStyle("-fx-text-fill: white;");
        foodNatureSelect = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(
                "🍃 Semences & Graines (Granivores / Messor)",
                "🍯 Miellat & Sucres Liquides (Aphidophages / Lasius & Formica)",
                "🌱 Biomasse Foliaire & Végétale (Atta / Leafcutter)",
                "🥩 Protéines & Proies Animales (Carnivores / Solenopsis & Guêpes)",
                "🌸 Nectar & Pollen Floral (Abeilles / Apis & Bourdons)",
                "🪵 Lignine & Bois Mort (Termites Lignivores / Reticulitermes)",
                "🍂 Humus, Cellulose & Cartonnage (Termites Humivores)",
                "🍄 Mycélium Termitomyces (Termites Champignonnistes)",
                "👑 Gelée Royale & Bouillie Larvaire (Nourrices / Reines)"
        ));
        foodNatureSelect.getSelectionModel().selectFirst();
        foodNatureSelect.setPrefWidth(330);

        Label foodLabel = new Label("Quantité (unités) :");
        foodLabel.setStyle("-fx-text-fill: white;");
        foodSlider = new Slider(10, 1000, 100);
        foodSlider.setPrefWidth(180);
        Label foodValue = new Label("100 u");
        foodValue.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");
        foodSlider.valueProperty().addListener((o,a,b) -> foodValue.setText(String.format("%.0f u", b.doubleValue())));

        grid.add(lblResType, 0, 0); grid.add(resourceTypeSelect, 1, 0);
        grid.add(lblNature, 0, 1); grid.add(foodNatureSelect, 1, 1);
        grid.add(foodLabel, 0, 2); grid.add(foodSlider, 1, 2); grid.add(foodValue, 2, 2);

        return grid;
    }

    /**
     * Sub-block 4: Environmental Disasters with Magnitude & Multi-Week Duration
     */
    private Node createDisastersSubBlockNode() {
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);

        Label typeLabel = new Label("Type de Catastrophe :");
        typeLabel.setStyle("-fx-text-fill: white;");
        disasterSelect = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(
                "🔥 Incendie de Forêt",
                "🌊 Inondation / Pluie Diluvienne",
                "☣️ Épidémie / Parasite Cordyceps",
                "☀️ Sécheresse / Canicule Extrême",
                "❄️ Gel Intense & Baisse Température",
                "☠️ Pollution Sol / Toxines"
        ));
        disasterSelect.getSelectionModel().selectFirst();
        disasterSelect.setPrefWidth(220);

        Label intLabel = new Label("Magnitude / Intensité :");
        intLabel.setStyle("-fx-text-fill: white;");
        intensitySlider = new Slider(0.1, 1.0, 0.5);
        intensitySlider.setPrefWidth(180);
        Label intValue = new Label("Moyenne (0.5)");
        intValue.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
        intensitySlider.valueProperty().addListener((o,a,b) -> {
            double v = b.doubleValue();
            intValue.setText(v < 0.3 ? "Faible (" + String.format("%.1f", v) + ")" : v < 0.7 ? "Moyenne (" + String.format("%.1f", v) + ")" : "Catastrophique (" + String.format("%.1f", v) + ")");
        });

        Label durLabel = new Label("Durée de l'Événement :");
        durLabel.setStyle("-fx-text-fill: white;");
        // Slider scale: 5 min to 43,200 min (30 days / 1 month)
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

        Button btnStopDisasters = new Button("🛑 Arrêter Toutes les Catastrophes");
        btnStopDisasters.setStyle("-fx-background-color: #ea580c; -fx-text-fill: white; -fx-font-weight: bold;");
        btnStopDisasters.setOnAction(e -> {
            log("🛑 Arrêt forcé de toutes les catastrophes en cours.");
            if (callback != null) callback.stopDisasters();
        });

        grid.add(typeLabel, 0, 0); grid.add(disasterSelect, 1, 0);
        grid.add(intLabel, 0, 1); grid.add(intensitySlider, 1, 1); grid.add(intValue, 2, 1);
        grid.add(durLabel, 0, 2); grid.add(durationSlider, 1, 2); grid.add(durationValLabel, 2, 2);
        grid.add(btnStopDisasters, 1, 3);

        return grid;
    }

    /**
     * Sub-block 5: Abiotic Core Physical Drivers (Temperature, Humidity, Wind, Solar Radiation)
     */
    private Node createParametersSubBlockNode() {
        VBox box = new VBox(10);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);

        // 1. Température Ambiante Air & Sol (°C)
        Label lblTemp = new Label("🌡️ Température Air/Sol (°C) :");
        lblTemp.setStyle("-fx-text-fill: white;");
        tempSlider = new Slider(-10.0, 50.0, 22.0);
        tempSlider.setPrefWidth(180);
        tempValLabel = new Label("22.0 °C");
        tempValLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");
        tempSlider.valueProperty().addListener((o, a, b) -> {
            tempValLabel.setText(String.format("%.1f °C", b.doubleValue()));
            updateDerivedPhysicalOutputs();
        });

        // 2. Humidité Relative Air (%)
        Label lblHumidity = new Label("💧 Humidité Relative Air (%) :");
        lblHumidity.setStyle("-fx-text-fill: white;");
        humiditySlider = new Slider(10.0, 100.0, 65.0);
        humiditySlider.setPrefWidth(180);
        humidityValLabel = new Label("65.0 %");
        humidityValLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");
        humiditySlider.valueProperty().addListener((o, a, b) -> {
            humidityValLabel.setText(String.format("%.1f %%", b.doubleValue()));
            updateDerivedPhysicalOutputs();
        });

        // 3. Vent & Circulation d'Air (m/s)
        Label lblWind = new Label("💨 Vitesse du Vent (m/s) :");
        lblWind.setStyle("-fx-text-fill: white;");
        windSlider = new Slider(0.0, 25.0, 1.5);
        windSlider.setPrefWidth(180);
        windValLabel = new Label("1.5 m/s");
        windValLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");
        windSlider.valueProperty().addListener((o, a, b) -> {
            windValLabel.setText(String.format("%.1f m/s", b.doubleValue()));
            updateDerivedPhysicalOutputs();
        });

        // 4. Rayonnement Solaire (W/m²)
        Label lblSolar = new Label("☀️ Radiance Solaire (W/m²) :");
        lblSolar.setStyle("-fx-text-fill: white;");
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

        // Derived physical outputs display panel
        VBox derivedBox = new VBox(4);
        derivedBox.setStyle("-fx-background-color: rgba(56, 189, 248, 0.06); -fx-padding: 8; -fx-background-radius: 6; -fx-border-color: rgba(56, 189, 248, 0.2); -fx-border-width: 1;");

        Label lblDerivedHeader = new Label("📊 Conéquences Physico-Chimiques & Biologiques Calculées par le Moteur :");
        lblDerivedHeader.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 11px; -fx-font-weight: bold;");

        derivedPheroLabel = new Label();
        derivedPheroLabel.setStyle("-fx-text-fill: #e4e4e7; -fx-font-size: 11px;");

        derivedPrimaryProductivityLabel = new Label();
        derivedPrimaryProductivityLabel.setStyle("-fx-text-fill: #e4e4e7; -fx-font-size: 11px;");

        derivedOvipositionLabel = new Label();
        derivedOvipositionLabel.setStyle("-fx-text-fill: #e4e4e7; -fx-font-size: 11px;");

        derivedBox.getChildren().addAll(lblDerivedHeader, derivedPheroLabel, derivedPrimaryProductivityLabel, derivedOvipositionLabel);

        updateDerivedPhysicalOutputs();

        box.getChildren().addAll(grid, derivedBox);
        return box;
    }

    private void updateDerivedPhysicalOutputs() {
        if (tempSlider == null) return;

        double temp = tempSlider.getValue();
        double hum = humiditySlider.getValue();
        double wind = windSlider.getValue();
        double solar = solarSlider.getValue();

        // Evaporation equation (Antoine/Magnus vapor pressure approximation)
        double calcEvapRate = 0.02 + (temp / 100.0) * (1.0 + wind / 5.0) * (1.0 - hum / 120.0);
        calcEvapRate = Math.max(0.005, Math.min(0.30, calcEvapRate));

        // Primary productivity (Photosynthetic PAR model)
        double calcProdRate = (solar / 1000.0) * (hum / 100.0) * (temp > 5 && temp < 40 ? 1.0 : 0.2);
        calcProdRate = Math.max(0.0, Math.min(1.5, calcProdRate));

        // Queen Oviposition thermal efficiency
        double calcOviposRate = temp >= 15 && temp <= 32 ? 1.0 + (temp - 22.0) * 0.05 : Math.max(0.1, 1.0 - Math.abs(temp - 22.0) * 0.08);

        derivedPheroLabel.setText(String.format("• Evaporation Phéromonale Dérivée : %.2f %% / s (T°=%.1f°C, H=%.0f%%, Vent=%.1fm/s)", calcEvapRate * 100.0, temp, hum, wind));
        derivedPrimaryProductivityLabel.setText(String.format("• Photosynthèse & Biomasse Primaire : %.2f u / s (Radiance=%.0fW/m²)", calcProdRate, solar));
        derivedOvipositionLabel.setText(String.format("• Coefficient Thermique de Ponte Reine : %.2fx (Optimum 22-28°C)", calcOviposRate));
    }

    /**
     * Reads configured sub-block state and schedules an event into the Queue.
     */
    private void scheduleCurrentConfiguredEvent() {
        double stepSec = 0.016666666666666666;
        int d = spDay.getValue();
        int h = spHour.getValue();
        int m = spMin.getValue();
        int s = spSec.getValue();
        long totalSec = (long) (d - 1) * 86400L + h * 3600L + m * 60L + s;
        long targetTick = Math.max(1, Math.round(totalSec / stepSec));
        String timeFormatted = String.format("J%d %02d:%02d:%02d", d, h, m, s);

        String colTarget = eventColonySelect.getValue();
        String caste = casteSelect.getValue();
        int count = antCountSpinner.getValue();
        float posX = Float.parseFloat(posXField.getText());
        float posY = Float.parseFloat(posYField.getText());
        float posZ = Float.parseFloat(posZField.getText());
        String disasterType = disasterSelect.getValue();
        float intensity = (float) intensitySlider.getValue();
        int durMins = (int) durationSlider.getValue();
        String foodNat = foodNatureSelect.getValue();

        ScheduledEvent ev = new ScheduledEvent(targetTick, timeFormatted, "MIXED", disasterType, colTarget,
                String.format("Event %s (Mag:%.1f, Durée:%dmin) [%d %s - %s]", disasterType, intensity, durMins, count, caste, foodNat));
        ev.caste = caste;
        ev.count = count;
        ev.posX = posX; ev.posY = posY; ev.posZ = posZ;
        ev.intensity = intensity;
        ev.durationMinutes = durMins;
        ev.foodNature = foodNat;
        ev.amount = (float) foodSlider.getValue();
        ev.tempCelsius = (float) tempSlider.getValue();
        ev.humidityPercent = (float) humiditySlider.getValue();
        ev.windMetersPerSec = (float) windSlider.getValue();
        ev.solarWattsPerM2 = (float) solarSlider.getValue();

        scheduledEventsList.add(ev);
        refreshScheduledEventsListView();
        log(String.format("⏱️ Événement programmé pour le Jour %d %02d:%02d:%02d : %s [%s]",
                d, h, m, s, ev.eventType, ev.colonyTarget));
    }

    /**
     * Executes the currently configured sub-block values immediately in real-time.
     */
    private void executeCurrentConfiguredEventLive() {
        String colTarget = colonySelect.getValue();
        String caste = casteSelect.getValue();
        int count = antCountSpinner.getValue();
        float x = Float.parseFloat(posXField.getText());
        float y = Float.parseFloat(posYField.getText());
        float z = Float.parseFloat(posZField.getText());

        if (callback != null) {
            callback.spawnAnts(colTarget, caste, count, x, y, z);
            callback.triggerDisaster(disasterSelect.getValue(), (float) intensitySlider.getValue());
            callback.modifyParameter("temperatureCelsius", tempSlider.getValue());
            callback.modifyParameter("humidityPercent", humiditySlider.getValue());
            callback.modifyParameter("windSpeed", windSlider.getValue());
            callback.modifyParameter("solarRadiation", solarSlider.getValue());
        }
        log(String.format("⚡ Intervention directe exécutée : Apparition %d [%s] (%s) [Climat: T=%.1f°C, H=%.0f%%]",
                count, caste, colTarget, tempSlider.getValue(), humiditySlider.getValue()));
    }

    private void refreshScheduledEventsListView() {
        if (scheduledEventsListView == null) return;
        javafx.collections.ObservableList<String> items = javafx.collections.FXCollections.observableArrayList();
        for (ScheduledEvent ev : scheduledEventsList) {
            String status = ev.executed ? "✅ Exécuté" : ev.paused ? "⏸️ En Pause" : "⏳ En Attente";
            String colStr = (ev.colonyTarget != null && !ev.colonyTarget.contains("Global")) ? " [" + ev.colonyTarget + "]" : "";
            String timeStr = ev.timeFormatted != null && !ev.timeFormatted.isEmpty() ? " (" + ev.timeFormatted + ")" : "";
            items.add(String.format("[%s]%s : %s%s — %s", status, timeStr, ev.eventType, colStr, ev.description));
        }
        scheduledEventsListView.setItems(items);
    }

    private void executeScheduledEvent(ScheduledEvent ev) {
        String colStr = (ev.colonyTarget != null && !ev.colonyTarget.contains("Global")) ? ev.colonyTarget : "Colonie Primaire";
        String msg = String.format("⚡ Exécution de l'événement programmé (%s) : %s [%s] (%s)", ev.timeFormatted, ev.eventType, colStr, ev.description);
        log(msg);
        if (callback != null) {
            if (ev.eventType.contains("Incendie") || ev.eventType.contains("Inondation") || ev.eventType.contains("Épidémie") || ev.eventType.contains("Sécheresse")) {
                callback.triggerDisaster(ev.eventType, ev.intensity);
            } else if (ev.eventType.contains("Nourriture") || ev.eventType.contains("Biomasse")) {
                callback.spawnFood(ev.posX, ev.posY, ev.posZ, ev.amount);
            } else {
                callback.spawnAnts(colStr, ev.caste, ev.count, ev.posX, ev.posY, ev.posZ);
            }
        }
    }

    /**
     * Processes events for the given simulation tick.
     * Automatically handles simulation time rewind (seeking/rewinding back resets executed events).
     */
    public void processScheduledEvents(long currentTick) {
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
        if (needsRefresh) {
            javafx.application.Platform.runLater(this::refreshScheduledEventsListView);
        }
    }

    private VBox createLogSection() {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        VBox box = new VBox(5);
        box.setPadding(new Insets(8, 0, 0, 0));

        Label logLabel = new Label();
        logLabel.textProperty().bind(i18n.createStringBinding("god.log.title"));
        logLabel.setStyle("-fx-text-fill: #888;");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(90);
        logArea.setStyle("-fx-control-inner-background: #18181b; -fx-text-fill: #e4e4e7; -fx-font-family: monospace;");

        box.getChildren().addAll(logLabel, logArea);
        return box;
    }

    private Label simStateWarningLabel;

    public void setSimulationRunning(boolean running) {
        if (simStateWarningLabel != null) {
            if (!running) {
                simStateWarningLabel.setText("⏸ Simulation en pause — Mode Divin actif (les interventions modifient l'état du monde immédiatement).");
                simStateWarningLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-color: rgba(245,158,11,0.15); -fx-padding: 6 10; -fx-background-radius: 4;");
            } else {
                simStateWarningLabel.setText("● Simulation en cours — Mode Divin actif en temps réel.");
                simStateWarningLabel.setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-color: rgba(74,222,128,0.15); -fx-padding: 6 10; -fx-background-radius: 4;");
            }
        }
    }

    private void log(String message) {
        String time = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        logArea.appendText("[" + time + "] " + message + "\n");
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
            casteSelect.getItems().addAll("Ouvrière Média (Coupeuse)", "Ouvrière Minime (Nourrice)", "Soldat Majeur (Garde)", "Reine Géante");
        } else if (colonyName != null && (colonyName.contains("Apis") || colonyName.contains("Abeille"))) {
            casteSelect.getItems().addAll("Ouvrière Butineuse", "Reine Abeille", "Faux-Bourdon (Mâle)");
        } else if (colonyName != null && (colonyName.contains("Termite") || colonyName.contains("Reticulitermes"))) {
            casteSelect.getItems().addAll("Ouvrier Termite", "Soldat à Mandiboles", "Reine Physogastre", "Roi Reproducteur");
        } else if (colonyName != null && (colonyName.contains("Vespula") || colonyName.contains("Guêpe"))) {
            casteSelect.getItems().addAll("Ouvrière Chasseresse", "Fondatrice (Reine)");
        } else if (colonyName != null && (colonyName.contains("Solenopsis") || colonyName.contains("Feu"))) {
            casteSelect.getItems().addAll("Ouvrière Mineure", "Ouvrière Majeure / Soldat", "Reine");
        } else {
            casteSelect.getItems().addAll("Ouvrière Généraliste", "Soldat Guardien", "Éclaireuse", "Nourrice", "Reine Fondatrice");
        }
        casteSelect.getSelectionModel().selectFirst();
    }
}
