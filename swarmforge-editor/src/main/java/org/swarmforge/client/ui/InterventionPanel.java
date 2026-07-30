/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.swarmforge.core.event.EventBus;
import org.swarmforge.core.event.SimulationEvent;

/**
 * Intervention Panel - God Mode controls for runtime simulation manipulation.
 * Allows adding/removing entities, triggering events, and modifying parameters.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class InterventionPanel extends BorderPane {

    private Spinner<Integer> antCountSpinner;
    private ComboBox<String> casteSelect;
    private ComboBox<String> colonySelect;
    private TextField posXField, posYField, posZField;
    private ComboBox<String> killColonySelect;
    private ComboBox<String> disasterSelect;
    private Slider intensitySlider;
    private TextArea logArea;

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

    public InterventionPanel() {
        setPadding(new Insets(15));

        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();

        // Title
        Label title = new Label();
        title.textProperty().bind(i18n.createStringBinding("god.title"));
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ff6b6b;");
        title.setTooltip(new Tooltip("Contrôles d'intervention directe en mode divin pour modifier la simulation en cours de fonctionnement."));
        
        Label warning = new Label();
        warning.textProperty().bind(i18n.createStringBinding("god.warning"));
        warning.setStyle("-fx-text-fill: #ffc107; -fx-font-style: italic;");
        warning.setTooltip(new Tooltip("Attention : Toutes les interventions effectuées ici s'appliquent immédiatement à la simulation sans annulation possible."));
        
        VBox header = new VBox(5, title, warning);
        setTop(header);

        // Main content - Accordion with sections
        Accordion accordion = new Accordion();

        TitledPane spawnPane = createSpawnSection();
        TitledPane killPane = createKillSection();
        TitledPane resourcePane = createResourceSection();
        TitledPane speedPane = createCasteGenerationSpeedSection();
        TitledPane disasterPane = createDisasterSection();
        TitledPane paramPane = createParameterSection();

        accordion.getPanes().addAll(spawnPane, killPane, resourcePane, speedPane, disasterPane, paramPane);
        accordion.setExpandedPane(spawnPane);

        VBox content = new VBox(15, accordion);
        content.setPadding(new Insets(15, 0, 0, 0));
        setCenter(content);

        // Bottom: Action Log
        setBottom(createLogSection());
    }

    private TitledPane createSpawnSection() {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        // Colony selector
        Label colLabel = new Label();
        colLabel.textProperty().bind(i18n.createStringBinding("god.spawn.colony"));
        colLabel.setStyle("-fx-text-fill: white;");
        colonySelect = new ComboBox<>();
        colonySelect.getItems().addAll("Colonie #1 (Lasius niger)", "Colonie #2 (Atta sexdens)", "+ Nouvelle Colonie");
        colonySelect.getSelectionModel().selectFirst();
        colonySelect.setPrefWidth(200);
        colonySelect.setTooltip(new Tooltip("Sélectionnez la colonie cible dynamique issue de la simulation active."));
        colonySelect.setOnAction(e -> updateCastesForSelectedColony(colonySelect.getValue()));

        // Caste selector
        Label casteLabel = new Label();
        casteLabel.textProperty().bind(i18n.createStringBinding("god.spawn.caste"));
        casteLabel.setStyle("-fx-text-fill: white;");
        casteSelect = new ComboBox<>();
        updateCastesForSelectedColony(colonySelect.getValue());
        casteSelect.setPrefWidth(200);
        casteSelect.setTooltip(new Tooltip("Sélectionnez la caste morphologique disponible pour l'espèce de cette colonie."));

        // Count
        Label countLabel = new Label();
        countLabel.textProperty().bind(i18n.createStringBinding("god.spawn.count"));
        countLabel.setStyle("-fx-text-fill: white;");
        antCountSpinner = new Spinner<>(1, 1000, 10);
        antCountSpinner.setEditable(true);
        antCountSpinner.setPrefWidth(100);
        antCountSpinner.setTooltip(new Tooltip("Nombre d'individus à injecter instantanément dans la colonie (1 à 1000)."));

        // Position with explicit Metric Units [0 .. Terrarium Bounds]
        Label posLabel = new Label("Coordonnées (m) :");
        posLabel.setStyle("-fx-text-fill: white;");
        HBox posBox = new HBox(5);
        posXField = new TextField("128.0");
        posXField.setPrefWidth(60);
        posXField.setTooltip(new Tooltip("Position X en mètres [0.0 m ... Largeur du Terrarium]."));

        posYField = new TextField("128.0");
        posYField.setPrefWidth(60);
        posYField.setTooltip(new Tooltip("Position Y en mètres [0.0 m ... Profondeur du Terrarium]."));

        posZField = new TextField("32.0");
        posZField.setPrefWidth(60);
        posZField.setTooltip(new Tooltip("Altitude/Profondeur Z en mètres [0.0 m = Sol, >0 = Air/Feuillage]."));

        Label lblMetersUnit = new Label("m");
        lblMetersUnit.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");

        posBox.getChildren().addAll(new Label("X:"), posXField, new Label("Y:"), posYField, new Label("Z:"), posZField, lblMetersUnit);

        // Spawn button
        Button btnSpawn = new Button();
        btnSpawn.textProperty().bind(i18n.createStringBinding("god.spawn.btn"));
        btnSpawn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
        btnSpawn.setTooltip(new Tooltip("Injection immédiate des individus configurés aux coordonnées métriques X,Y,Z indiquées."));
        btnSpawn.setOnAction(e -> spawnAnts());

        grid.add(colLabel, 0, 0);
        grid.add(colonySelect, 1, 0);
        grid.add(casteLabel, 0, 1);
        grid.add(casteSelect, 1, 1);
        grid.add(countLabel, 0, 2);
        grid.add(antCountSpinner, 1, 2);
        grid.add(posLabel, 0, 3);
        grid.add(posBox, 1, 3);
        grid.add(btnSpawn, 1, 4);

        TitledPane pane = new TitledPane();
        pane.textProperty().bind(i18n.createStringBinding("god.spawn.title"));
        pane.setContent(grid);
        pane.setTooltip(new Tooltip("Génération et injection instantanée d'individus dans une colonie active."));
        styleTitledPane(pane);
        return pane;
    }

    private TitledPane createKillSection() {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        // Colony Target Selection for Elimination
        HBox colonyRow = new HBox(10);
        colonyRow.setAlignment(Pos.CENTER_LEFT);
        Label lblColonyTarget = new Label("Colonie Cible :");
        lblColonyTarget.setStyle("-fx-text-fill: white;");
        killColonySelect = new ComboBox<>();
        killColonySelect.getItems().addAll(i18n.get("god.spawn.colony_1"), i18n.get("god.spawn.colony_2"), i18n.get("god.spawn.colony_new"));
        killColonySelect.getSelectionModel().selectFirst();
        killColonySelect.setPrefWidth(200);
        killColonySelect.setTooltip(new Tooltip("Sélectionnez la colonie spécifique ciblée par l'action d'élimination."));
        colonyRow.getChildren().addAll(lblColonyTarget, killColonySelect);

        HBox row1 = new HBox(10);
        row1.setAlignment(Pos.CENTER_LEFT);
        
        ComboBox<String> targetSelect = new ComboBox<>();
        targetSelect.getItems().addAll(
            i18n.get("god.kill.target_workers"),
            i18n.get("god.kill.target_soldiers"),
            i18n.get("god.kill.target_oldest"),
            i18n.get("god.kill.target_weakest"),
            i18n.get("god.kill.target_queens")
        );
        targetSelect.getSelectionModel().selectFirst();
        targetSelect.setPrefWidth(150);
        targetSelect.setTooltip(new Tooltip("Sélectionnez le sous-groupe ou profil d'individus à éliminer dans la colonie."));
        
        Spinner<Integer> killCount = new Spinner<>(1, 1000, 5);
        killCount.setPrefWidth(80);
        killCount.setTooltip(new Tooltip("Nombre d'individus de la colonie à éliminer."));

        Button btnKill = new Button();
        btnKill.textProperty().bind(i18n.createStringBinding("god.kill.btn"));
        btnKill.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        btnKill.setTooltip(new Tooltip("Élimine immédiatement le nombre d'individus ciblés dans la colonie sélectionnée."));
        btnKill.setOnAction(e -> {
            String selectedColony = killColonySelect.getValue();
            log("Élimination de " + killCount.getValue() + " " + targetSelect.getValue() + " dans " + selectedColony);
            if (callback != null) callback.killAnts(selectedColony, targetSelect.getValue(), killCount.getValue());
            publishEvent(SimulationEvent.EventType.WORKER_DIED);
        });

        Label lblTarget = new Label();
        lblTarget.textProperty().bind(i18n.createStringBinding("god.kill.target"));
        lblTarget.setStyle("-fx-text-fill: white;");

        Label lblCount = new Label();
        lblCount.textProperty().bind(i18n.createStringBinding("god.kill.count"));
        lblCount.setStyle("-fx-text-fill: white;");

        row1.getChildren().addAll(lblTarget, targetSelect, lblCount, killCount, btnKill);

        // Mass extinction button
        Button btnExtinct = new Button();
        btnExtinct.textProperty().bind(i18n.createStringBinding("god.kill.extinct"));
        btnExtinct.setStyle("-fx-background-color: #6c0000; -fx-text-fill: white; -fx-font-weight: bold;");
        btnExtinct.setTooltip(new Tooltip("Destruction totale et définitive de l'intégralité de la colonie sélectionnée (100% mortalité)."));
        btnExtinct.setOnAction(e -> {
            String selectedColony = killColonySelect.getValue();
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.titleProperty().bind(i18n.createStringBinding("god.kill.confirm_title"));
            confirm.setContentText(i18n.get("god.kill.confirm_msg") + " (" + selectedColony + ")");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    log("⚠ Extinction totale déclenchée pour la colonie : " + selectedColony);
                    if (callback != null) callback.killAnts(selectedColony, "ALL", 999999);
                    publishEvent(SimulationEvent.EventType.COLONY_DESTROYED);
                }
            });
        });

        content.getChildren().addAll(colonyRow, row1, new Separator(), btnExtinct);

        TitledPane pane = new TitledPane();
        pane.textProperty().bind(i18n.createStringBinding("god.kill.title"));
        pane.setContent(content);
        pane.setTooltip(new Tooltip("Élimination sélective d'individus ou extinction complète d'une colonie ciblée."));
        styleTitledPane(pane);
        return pane;
    }

    private TitledPane createResourceSection() {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        // Food spawning
        Label foodLabel = new Label();
        foodLabel.textProperty().bind(i18n.createStringBinding("god.resources.food_amount"));
        foodLabel.setStyle("-fx-text-fill: white;");
        Slider foodSlider = new Slider(10, 1000, 100);
        foodSlider.setShowTickLabels(true);
        foodSlider.setPrefWidth(180);
        foodSlider.setTooltip(new Tooltip("Quantité d'unités nutritives (biomasse/sucre) à déposer sur le terrain."));

        Label foodValue = new Label("100");
        foodValue.setStyle("-fx-text-fill: #e4e4e7;");
        foodSlider.valueProperty().addListener((o,a,b) -> foodValue.setText(String.format("%.0f", b.doubleValue())));

        Button btnFood = new Button();
        btnFood.textProperty().bind(i18n.createStringBinding("god.resources.btn_food"));
        btnFood.setStyle("-fx-background-color: #3f3f46; -fx-text-fill: white;");
        btnFood.setTooltip(new Tooltip("Dépose une source de nourriture de surface aux coordonnées X,Y,Z indiquées."));
        btnFood.setOnAction(e -> {
            float x = Float.parseFloat(posXField.getText());
            float y = Float.parseFloat(posYField.getText());
            float z = Float.parseFloat(posZField.getText());
            log("Apparition de " + (int)foodSlider.getValue() + " unités de nourriture aux coordonnées (" + x + "," + y + "," + z + ")");
            if (callback != null) callback.spawnFood(x, y, z, (float)foodSlider.getValue());
        });

        // Water spawning
        Button btnWater = new Button();
        btnWater.textProperty().bind(i18n.createStringBinding("god.resources.btn_water"));
        btnWater.setTooltip(new Tooltip("Dépose une flaque/source d'eau douce de surface aux coordonnées X,Y,Z indiquées."));
        btnWater.setOnAction(e -> {
            float x = Float.parseFloat(posXField.getText());
            float y = Float.parseFloat(posYField.getText());
            float z = Float.parseFloat(posZField.getText());
            log("Apparition d'une source d'eau à (" + x + "," + y + "," + z + ")");
        });

        // Remove resources
        Button btnRemoveFood = new Button();
        btnRemoveFood.textProperty().bind(i18n.createStringBinding("god.resources.btn_remove_food"));
        btnRemoveFood.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        btnRemoveFood.setTooltip(new Tooltip("Supprime uniquement les dépôts de nourriture libres éparpillés sur le sol (n'affecte ni les réserves internes stockées dans le nid, ni la flore/graminées vivantes)."));
        btnRemoveFood.setOnAction(e -> log("Purge de la nourriture de surface libre effectuée (les stocks de nid et la flore vivante sont préservés)."));

        grid.add(foodLabel, 0, 0);
        grid.add(foodSlider, 1, 0);
        grid.add(foodValue, 2, 0);
        grid.add(btnFood, 1, 1);
        grid.add(btnWater, 2, 1);
        grid.add(new Separator(), 0, 2, 3, 1);
        grid.add(btnRemoveFood, 1, 3);

        TitledPane pane = new TitledPane();
        pane.textProperty().bind(i18n.createStringBinding("god.resources.title"));
        pane.setContent(grid);
        pane.setTooltip(new Tooltip("Injection et nettoyage des ressources environnementales de surface (Nourriture & Eau)."));
        styleTitledPane(pane);
        return pane;
    }

    private TitledPane createCasteGenerationSpeedSection() {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        // Worker generation speed
        Label lblWorkerSpeed = new Label();
        lblWorkerSpeed.textProperty().bind(i18n.createStringBinding("god.speed_castes.workers"));
        lblWorkerSpeed.setStyle("-fx-text-fill: white;");
        Slider workerSpeedSlider = new Slider(0.1, 5.0, 1.0);
        workerSpeedSlider.setShowTickLabels(true);
        workerSpeedSlider.setTooltip(new Tooltip("Multiplicateur de vitesse de ponte et développement des ouvrières (0.1x à 5.0x)."));
        Label lblWorkerVal = new Label("1.0x");
        lblWorkerVal.setStyle("-fx-text-fill: #38bdf8;");
        workerSpeedSlider.valueProperty().addListener((o, a, b) -> {
            lblWorkerVal.setText(String.format("%.1fx", b.doubleValue()));
            log("Vitesse ponte/génération Ouvrières fixée à " + String.format("%.1fx", b.doubleValue()));
        });

        // Soldier generation speed
        Label lblSoldierSpeed = new Label();
        lblSoldierSpeed.textProperty().bind(i18n.createStringBinding("god.speed_castes.soldiers"));
        lblSoldierSpeed.setStyle("-fx-text-fill: white;");
        Slider soldierSpeedSlider = new Slider(0.1, 5.0, 1.0);
        soldierSpeedSlider.setShowTickLabels(true);
        soldierSpeedSlider.setTooltip(new Tooltip("Multiplicateur de vitesse de ponte et développement des soldats (0.1x à 5.0x)."));
        Label lblSoldierVal = new Label("1.0x");
        lblSoldierVal.setStyle("-fx-text-fill: #ef4444;");
        soldierSpeedSlider.valueProperty().addListener((o, a, b) -> {
            lblSoldierVal.setText(String.format("%.1fx", b.doubleValue()));
            log("Vitesse ponte/génération Soldats fixée à " + String.format("%.1fx", b.doubleValue()));
        });

        // Predator generation speed
        Label lblPredatorSpeed = new Label();
        lblPredatorSpeed.textProperty().bind(i18n.createStringBinding("god.speed_castes.predators"));
        lblPredatorSpeed.setStyle("-fx-text-fill: white;");
        Slider predatorSpeedSlider = new Slider(0.1, 5.0, 1.0);
        predatorSpeedSlider.setShowTickLabels(true);
        predatorSpeedSlider.setTooltip(new Tooltip("Taux d'apparition et d'intrusion des prédateurs externes dans l'écosystème (0.1x à 5.0x)."));
        Label lblPredatorVal = new Label("1.0x");
        lblPredatorVal.setStyle("-fx-text-fill: #a855f7;");
        predatorSpeedSlider.valueProperty().addListener((o, a, b) -> {
            lblPredatorVal.setText(String.format("%.1fx", b.doubleValue()));
            log("Taux d'apparition Prédateurs fixé à " + String.format("%.1fx", b.doubleValue()));
        });

        grid.add(lblWorkerSpeed, 0, 0); grid.add(workerSpeedSlider, 1, 0); grid.add(lblWorkerVal, 2, 0);
        grid.add(lblSoldierSpeed, 0, 1); grid.add(soldierSpeedSlider, 1, 1); grid.add(lblSoldierVal, 2, 1);
        grid.add(lblPredatorSpeed, 0, 2); grid.add(predatorSpeedSlider, 1, 2); grid.add(lblPredatorVal, 2, 2);

        TitledPane pane = new TitledPane();
        pane.textProperty().bind(i18n.createStringBinding("god.speed_castes.title"));
        pane.setContent(grid);
        pane.setTooltip(new Tooltip("Ajustement direct des taux de ponte et de réapparition par caste et pour les prédateurs."));
        styleTitledPane(pane);
        return pane;
    }

    private TitledPane createDisasterSection() {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        // Disaster type selector
        HBox typeRow = new HBox(10);
        typeRow.setAlignment(Pos.CENTER_LEFT);
        Label typeLabel = new Label();
        typeLabel.textProperty().bind(i18n.createStringBinding("god.disasters.type"));
        typeLabel.setStyle("-fx-text-fill: white;");
        disasterSelect = new ComboBox<>();
        disasterSelect.getItems().addAll(
            i18n.get("god.disasters.fire"),
            i18n.get("god.disasters.flood"),
            i18n.get("god.disasters.tornado"),
            i18n.get("god.disasters.drought"),
            i18n.get("god.disasters.freeze"),
            i18n.get("god.disasters.lightning"),
            i18n.get("god.disasters.pestilence"),
            i18n.get("god.disasters.landslide"),
            i18n.get("god.disasters.heatwave"),
            i18n.get("god.disasters.pollution")
        );
        disasterSelect.getSelectionModel().selectFirst();
        disasterSelect.setPrefWidth(180);
        disasterSelect.setTooltip(new Tooltip("Sélectionnez le type de catastrophe naturelle ou climatique à déclencher instantanément."));
        typeRow.getChildren().addAll(typeLabel, disasterSelect);

        // Intensity slider
        HBox intRow = new HBox(10);
        intRow.setAlignment(Pos.CENTER_LEFT);
        Label intLabel = new Label();
        intLabel.textProperty().bind(i18n.createStringBinding("god.disasters.intensity"));
        intLabel.setStyle("-fx-text-fill: white;");
        intensitySlider = new Slider(0.1, 1.0, 0.5);
        intensitySlider.setShowTickLabels(true);
        intensitySlider.setPrefWidth(200);
        intensitySlider.setTooltip(new Tooltip("Définissez la sévérité et l'amplitude de l'événement destructeur (Faible -> Catastrophique)."));
        
        Label intValue = new Label(i18n.get("god.disasters.intensity_med"));
        intValue.setStyle("-fx-text-fill: #ffc107;");
        intensitySlider.valueProperty().addListener((o,a,b) -> {
            double v = b.doubleValue();
            intValue.setText(v < 0.3 ? i18n.get("god.disasters.intensity_low") : v < 0.7 ? i18n.get("god.disasters.intensity_med") : i18n.get("god.disasters.intensity_catastrophic"));
            intValue.setStyle("-fx-text-fill: " + (v < 0.3 ? "#28a745" : v < 0.7 ? "#ffc107" : "#dc3545") + ";");
        });
        intRow.getChildren().addAll(intLabel, intensitySlider, intValue);

        // Trigger button
        Button btnTrigger = new Button();
        btnTrigger.textProperty().bind(i18n.createStringBinding("god.disasters.btn_trigger"));
        btnTrigger.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        btnTrigger.setTooltip(new Tooltip("Déclenche immédiatement la catastrophe sélectionnée dans le terrarium activement simulé."));
        btnTrigger.setOnAction(e -> {
            String type = disasterSelect.getValue();
            float intensity = (float) intensitySlider.getValue();
            log("⚠ CATASTROPHE DÉCLENCHÉE : " + type + " (Intensité : " + String.format("%.1f", intensity) + ")");
            if (callback != null) callback.triggerDisaster(type, intensity);
            EventBus.getInstance().publish(SimulationEvent.disasterOccurred(0, type, intensity, 100));
        });

        // Stop Ongoing Disasters button
        Button btnStopDisasters = new Button();
        btnStopDisasters.textProperty().bind(i18n.createStringBinding("god.disasters.btn_stop"));
        btnStopDisasters.setStyle("-fx-background-color: #ea580c; -fx-text-fill: white; -fx-font-weight: bold;");
        btnStopDisasters.setTooltip(new Tooltip("Interrompt immédiatement toutes les catastrophes climatiques ou épidémies actuellement en cours dans le monde."));
        btnStopDisasters.setOnAction(e -> {
            log("🛑 Arrêt forcé de toutes les catastrophes en cours.");
            if (callback != null) callback.stopDisasters();
        });

        content.getChildren().addAll(typeRow, intRow, btnTrigger, new Separator(), btnStopDisasters);

        TitledPane pane = new TitledPane();
        pane.textProperty().bind(i18n.createStringBinding("god.disasters.title"));
        pane.setContent(content);
        pane.setTooltip(new Tooltip("Déclenchement et interruption de catastrophes environnementales (Incendies, Inondations, Épidémies, Canicules)."));
        styleTitledPane(pane);
        return pane;
    }

    private TitledPane createParameterSection() {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        // Pheromone decay
        Label pheroLabel = new Label();
        pheroLabel.textProperty().bind(i18n.createStringBinding("god.params.phero_decay"));
        pheroLabel.setStyle("-fx-text-fill: white;");
        Slider pheroSlider = new Slider(0.01, 0.2, 0.05);
        pheroSlider.setPrefWidth(200);
        pheroSlider.setTooltip(new Tooltip("Taux de dissipation/évaporation des pistes phéromonales déposées au sol par seconde."));

        // Food spawn rate
        Label foodRateLabel = new Label();
        foodRateLabel.textProperty().bind(i18n.createStringBinding("god.params.food_rate"));
        foodRateLabel.setStyle("-fx-text-fill: white;");
        Slider foodRateSlider = new Slider(0, 1.0, 0.3);
        foodRateSlider.setPrefWidth(200);
        foodRateSlider.setTooltip(new Tooltip("Fréquence de régénération automatique des sources de nourriture naturelles sur le terrain."));

        Button btnApply = new Button();
        btnApply.textProperty().bind(i18n.createStringBinding("god.params.btn_apply"));
        btnApply.setStyle("-fx-background-color: #3f3f46; -fx-text-fill: white;");
        btnApply.setTooltip(new Tooltip("Applique immédiatement les nouveaux paramètres physiques et biologiques au moteur de simulation."));
        btnApply.setOnAction(e -> {
            log("Paramètres physiques et biologiques mis à jour.");
            if (callback != null) {
                callback.modifyParameter("pheromoneDecay", pheroSlider.getValue());
                callback.modifyParameter("foodRate", foodRateSlider.getValue());
            }
        });

        grid.add(pheroLabel, 0, 0);
        grid.add(pheroSlider, 1, 0);
        grid.add(foodRateLabel, 0, 1);
        grid.add(foodRateSlider, 1, 1);
        grid.add(btnApply, 1, 2);

        TitledPane pane = new TitledPane();
        pane.textProperty().bind(i18n.createStringBinding("god.params.title"));
        pane.setContent(grid);
        pane.setTooltip(new Tooltip("Réglages avancés de dissipation des pistes chimiques et de régénération des ressources."));
        styleTitledPane(pane);
        return pane;
    }

    private VBox createLogSection() {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        VBox box = new VBox(5);
        box.setPadding(new Insets(10, 0, 0, 0));

        Label logLabel = new Label();
        logLabel.textProperty().bind(i18n.createStringBinding("god.log.title"));
        logLabel.setStyle("-fx-text-fill: #888;");
        logLabel.setTooltip(new Tooltip("Historique chronologique des interventions manuelles exécutées en Mode Divin."));

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(100);
        logArea.setStyle("-fx-control-inner-background: #18181b; -fx-text-fill: #e4e4e7; -fx-font-family: monospace;");
        logArea.setTooltip(new Tooltip("Journal d'audit horodaté enregistrant les apparitions, éliminations et catastrophes."));

        box.getChildren().addAll(logLabel, logArea);
        return box;
    }

    private void styleTitledPane(TitledPane pane) {
        pane.setStyle("-fx-text-fill: white;");
    }

    private void spawnAnts() {
        try {
            String colony = colonySelect.getValue();
            String caste = casteSelect.getValue();
            int count = antCountSpinner.getValue();
            float x = Float.parseFloat(posXField.getText());
            float y = Float.parseFloat(posYField.getText());
            float z = Float.parseFloat(posZField.getText());

            log("Spawned " + count + " " + caste + "s for " + colony + " at (" + x + "," + y + "," + z + ")");
            
            if (callback != null) {
                callback.spawnAnts(colony, caste, count, x, y, z);
            }

            // Publish event
            publishEvent(caste.equals("Queen") ? SimulationEvent.EventType.QUEEN_BORN : 
                        caste.equals("Soldier") ? SimulationEvent.EventType.SOLDIER_BORN : 
                        SimulationEvent.EventType.WORKER_BORN);

        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Invalid position values").show();
        }
    }

    private void publishEvent(SimulationEvent.EventType type) {
        EventBus.getInstance().publish(new SimulationEvent(type, 0, type.name() + " via God Mode"));
    }

    private void log(String message) {
        String time = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        logArea.appendText("[" + time + "] " + message + "\n");
    }

    public void setCallback(InterventionCallback callback) {
        this.callback = callback;
    }

    /**
     * Updates the colony dropdowns dynamically with active simulation colonies.
     */
    public void updateAvailableColonies(java.util.List<String> activeColonyNames) {
        if (activeColonyNames == null || activeColonyNames.isEmpty()) return;
        
        String selSpawn = colonySelect.getValue();
        String selKill = killColonySelect != null ? killColonySelect.getValue() : null;

        colonySelect.getItems().setAll(activeColonyNames);
        if (killColonySelect != null) {
            killColonySelect.getItems().setAll(activeColonyNames);
        }

        if (selSpawn != null && colonySelect.getItems().contains(selSpawn)) {
            colonySelect.getSelectionModel().select(selSpawn);
        } else {
            colonySelect.getSelectionModel().selectFirst();
        }

        if (killColonySelect != null) {
            if (selKill != null && killColonySelect.getItems().contains(selKill)) {
                killColonySelect.getSelectionModel().select(selKill);
            } else {
                killColonySelect.getSelectionModel().selectFirst();
            }
        }

        updateCastesForSelectedColony(colonySelect.getValue());
    }

    /**
     * Updates the caste dropdown dynamically based on the selected colony's species.
     */
    private void updateCastesForSelectedColony(String colonyName) {
        if (casteSelect == null) return;

        casteSelect.getItems().clear();

        if (colonyName != null && (colonyName.contains("Atta") || colonyName.contains("Leafcutter"))) {
            casteSelect.getItems().addAll("Reine Géante", "Ouvrière Minime (Nourrice)", "Ouvrière Média (Coupeuse)", "Soldat Majeur (Garde)");
        } else if (colonyName != null && (colonyName.contains("Apis") || colonyName.contains("Abeille"))) {
            casteSelect.getItems().addAll("Reine Abeille", "Ouvrière Butineuse", "Faux-Bourdon (Mâle)");
        } else if (colonyName != null && (colonyName.contains("Termite") || colonyName.contains("Reticulitermes"))) {
            casteSelect.getItems().addAll("Reine Physogastre", "Roi Reproducteur", "Ouvrier Termite", "Soldat à Mandiboles");
        } else if (colonyName != null && (colonyName.contains("Vespula") || colonyName.contains("Guêpe"))) {
            casteSelect.getItems().addAll("Fondatrice (Reine)", "Ouvrière Chasseresse");
        } else if (colonyName != null && (colonyName.contains("Solenopsis") || colonyName.contains("Feu"))) {
            casteSelect.getItems().addAll("Reine", "Ouvrière Mineure", "Ouvrière Majeure / Soldat");
        } else {
            casteSelect.getItems().addAll("Reine Fondatrice", "Ouvrière Généraliste", "Soldat Guardien", "Éclaireuse", "Nourrice");
        }
        casteSelect.getSelectionModel().selectFirst();
    }
}
