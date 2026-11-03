/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.client.util.I18nManager;

/**
 * UI Sub-block factory and control holder for {@link InterventionPanel}.
 * Builds the 7 God-Mode intervention category cards (Entities, Resources,
 * Disasters, Abiotic Climate, Pheromones, Invasions, and Genetic Mutations).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
class InterventionCategoryBuilder {

    private final InterventionPanel panel;
    private final InterventionAbioticCategoryBuilder abioticBuilder;
    private final InterventionBiologicalCategoryBuilder biologicalBuilder;

    Spinner<Integer> antCountSpinner;
    ComboBox<String> casteSelect;
    ComboBox<String> entityActionSelect;

    ComboBox<String> disasterSelect;
    Slider intensitySlider;
    Slider durationSlider;
    Label durationValLabel;

    ComboBox<String> abioticModeSelect;
    Slider abioticDurationSlider;
    Label abioticDurationValLabel;

    Slider deltaTempSlider;
    Label deltaTempValLabel;
    Slider deltaHumiditySlider;
    Label deltaHumidityValLabel;
    Slider deltaWindSlider;
    Label deltaWindValLabel;
    Slider deltaSolarSlider;
    Label deltaSolarValLabel;

    Label derivedPheroLabel;
    Label derivedPrimaryProductivityLabel;
    Label derivedOvipositionLabel;

    Slider foodSlider;
    ComboBox<String> resourceTypeSelect;
    ComboBox<String> foodNatureSelect;

    ComboBox<String> pheromoneTypeSelect;
    Slider pheroRadiusSlider, pheroIntensitySlider, pheroDurationSlider;
    Label pheroRadiusValLabel, pheroIntensityValLabel, pheroDurationValLabel;

    ComboBox<String> invasionTypeSelect;
    Spinner<Integer> invasionCountSpinner;
    Slider invasionDurationSlider;
    Label invasionDurationValLabel;

    ComboBox<String> mutationTypeSelect;
    ComboBox<String> mutationCasteScopeSelect;
    Slider mutationIntensitySlider, mutationDurationSlider;
    Label mutationIntensityValLabel, mutationDurationValLabel;
    Label mutationExplanationLabel;

    InterventionCategoryBuilder(InterventionPanel panel) {
        this.panel = panel;
        this.abioticBuilder = new InterventionAbioticCategoryBuilder(this, panel);
        this.biologicalBuilder = new InterventionBiologicalCategoryBuilder(this, panel);
    }

    /**
     * Sub-block 3: Entities, Castes & Brood Management
     */
    Node createEntitiesSubBlockNode() {
        I18nManager i18n = I18nManager.getInstance();
        VBox box = new VBox(10);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

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
        updateCastesForSelectedColony(panel.eventColonySelect != null ? panel.eventColonySelect.getValue() : null);

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
        btnScheduleEntities.setOnAction(e -> panel.scheduler.scheduleEntitiesEvent());

        actionBtnRow.getChildren().add(btnScheduleEntities);
        box.getChildren().addAll(grid, actionBtnRow);
        return box;
    }

    /**
     * Sub-block 4: Resources & Biomass
     */
    Node createResourcesSubBlockNode() {
        I18nManager i18n = I18nManager.getInstance();
        VBox box = new VBox(10);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

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

        foodSlider.valueProperty().addListener((o, a, b) -> updateResUnit.run());
        resourceTypeSelect.valueProperty().addListener((o, a, b) -> updateResUnit.run());
        foodNatureSelect.valueProperty().addListener((o, a, b) -> updateResUnit.run());
        updateResUnit.run();

        grid.add(lblResType, 0, 0); grid.add(resourceTypeSelect, 1, 0);
        grid.add(lblNature, 0, 1); grid.add(foodNatureSelect, 1, 1);
        grid.add(foodLabel, 0, 2); grid.add(foodSlider, 1, 2); grid.add(foodValue, 2, 2);

        HBox actionBtnRow = new HBox(8);
        Button btnScheduleRes = new Button();
        btnScheduleRes.textProperty().bind(i18n.createStringBinding("god.btn.schedule_res"));
        btnScheduleRes.getStyleClass().add("btn-primary");
        btnScheduleRes.tooltipProperty().bind(i18n.createTooltipBinding("god.resources.btn.tt"));
        btnScheduleRes.setOnAction(e -> panel.scheduler.scheduleResourceEvent());

        actionBtnRow.getChildren().add(btnScheduleRes);
        box.getChildren().addAll(grid, actionBtnRow);
        return box;
    }

    /**
     * Sub-block 5: Environmental Disasters
     */
    Node createDisastersSubBlockNode() {
        I18nManager i18n = I18nManager.getInstance();
        VBox box = new VBox(10);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

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
        intensitySlider.valueProperty().addListener((o, a, b) -> {
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
        btnScheduleDisaster.setOnAction(e -> panel.scheduler.scheduleDisasterEvent());

        Button btnStopDisasters = new Button();
        btnStopDisasters.textProperty().bind(i18n.createStringBinding("god.disasters.btn_stop"));
        btnStopDisasters.getStyleClass().add("btn-danger");
        btnStopDisasters.setOnAction(e -> {
            if (panel.callback != null) panel.callback.stopDisasters();
        });

        actionBtnRow.getChildren().addAll(btnScheduleDisaster, btnStopDisasters);
        box.getChildren().addAll(grid, actionBtnRow);
        return box;
    }

    /**
     * Sub-block 6: Abiotic Physical Drivers with Relative Delta (+/-) & Duration or Absolute Fixed Target
     */
    Node createParametersSubBlockNode() {
        return abioticBuilder.createParametersSubBlockNode();
    }

    /**
     * Sub-block 7: Pheromone & Behavioral Perturbations
     */
    Node createPheromoneSubBlockNode() {
        I18nManager i18n = I18nManager.getInstance();
        VBox box = new VBox(10);

        Label lblCenterNote = new Label();
        lblCenterNote.textProperty().bind(i18n.createStringBinding("god.phero.centernote"));
        lblCenterNote.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 11px; -fx-font-style: italic;");
        lblCenterNote.tooltipProperty().bind(i18n.createTooltipBinding("god.phero.centernote.tt"));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

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
        btnSchedulePhero.setOnAction(e -> panel.scheduler.schedulePheromoneEvent());

        box.getChildren().addAll(lblCenterNote, grid, btnSchedulePhero);
        return box;
    }

    /**
     * Sub-block 8: Apex Predators & Biological Invasions
     */
    Node createInvasionSubBlockNode() {
        return biologicalBuilder.createInvasionSubBlockNode();
    }

    /**
     * Sub-block 9: Genetics, Metabolic Boosts & Mutagens
     */
    Node createMutationSubBlockNode() {
        return biologicalBuilder.createMutationSubBlockNode();
    }

    void updateDerivedPhysicalOutputs() {
        abioticBuilder.updateDerivedPhysicalOutputs();
    }

    void updateCastesForSelectedColony(String colonyName) {
        if (casteSelect == null) return;
        casteSelect.getItems().clear();
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
