/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 SilvÃ¨re Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * UI Sub-block factory and control holder for {@link InterventionPanel}.
 * Builds the 7 God-Mode intervention category cards (Entities, Resources,
 * Disasters, Abiotic Climate, Pheromones, Invasions, and Genetic Mutations).
 */
class InterventionCategoryBuilder {

    private final InterventionPanel panel;

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
    }

    /**
     * Sub-block 3: Entities, Castes & Brood Management
     */
    Node createEntitiesSubBlockNode() {
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
        btnScheduleRes.setOnAction(e -> panel.scheduler.scheduleResourceEvent());

        actionBtnRow.getChildren().add(btnScheduleRes);

        box.getChildren().addAll(grid, actionBtnRow);
        return box;
    }

    /**
     * Sub-block 5: Environmental Disasters
     */
    Node createDisastersSubBlockNode() {
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
        btnScheduleAbiotic.setOnAction(e -> panel.scheduler.scheduleAbioticEvent());

        actionBtnRow.getChildren().add(btnScheduleAbiotic);

        box.getChildren().addAll(grid, derivedBox, actionBtnRow);
        return box;
    }

    /**
     * Sub-block 7: Pheromone & Behavioral Perturbations (With Intensity & Duration)
     */
    Node createPheromoneSubBlockNode() {
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
        btnSchedulePhero.setOnAction(e -> panel.scheduler.schedulePheromoneEvent());

        box.getChildren().addAll(lblCenterNote, grid, btnSchedulePhero);
        return box;
    }

    /**
     * Sub-block 8: Apex Predators & Biological Invasions (With Count & Duration)
     */
    Node createInvasionSubBlockNode() {
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

        Label lblInvasionNote = new Label();
        lblInvasionNote.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px; -fx-font-style: italic;");

        Label lblPredatorLifecycleNote = new Label();
        lblPredatorLifecycleNote.textProperty().bind(i18n.createStringBinding("god.invasion.lifecycle_note"));
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
                lblInvasionNote.setText(i18n.get("god.invasion.sanctuary"));
                lblInvasionNote.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 11px; -fx-font-weight: bold;");
            } else if (isZero) {
                invasionCountSpinner.setDisable(false);
                lblInvasionNote.setText(i18n.get("god.invasion.zero_pressure"));
                lblInvasionNote.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 11px; -fx-font-weight: bold;");
            } else {
                invasionCountSpinner.setDisable(false);
                lblInvasionNote.setText(i18n.get("god.invasion.active_raid", (cnt != null ? cnt : 0)));
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
        btnScheduleInvasion.setOnAction(e -> panel.scheduler.scheduleInvasionEvent());

        box.getChildren().addAll(grid, lblInvasionNote, lblPredatorLifecycleNote, btnScheduleInvasion);
        return box;
    }

    /**
     * Sub-block 9: Genetics, Metabolic Boosts & Mutagens (With Intensity & Duration)
     */
    Node createMutationSubBlockNode() {
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
        btnScheduleMutation.setOnAction(e -> panel.scheduler.scheduleMutationEvent());

        box.getChildren().addAll(lblMutationTargetNote, grid, mutationExplanationLabel, btnScheduleMutation);
        return box;
    }

    void updateDerivedPhysicalOutputs() {
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
        } else if (colonyName != null && (colonyName.contains("Vespula") || colonyName.contains("GuÃªpe"))) {
            casteSelect.getItems().addAll("Foundress (Queen)", "Hunter Worker");
        } else if (colonyName != null && (colonyName.contains("Solenopsis") || colonyName.contains("Feu"))) {
            casteSelect.getItems().addAll("Major Worker / Soldier", "Minor Worker", "Queen");
        } else {
            casteSelect.getItems().addAll("Foundress Queen", "Generalist Worker", "Guardian Soldier", "Nurse", "Scout");
        }
        casteSelect.getSelectionModel().selectFirst();
    }
}