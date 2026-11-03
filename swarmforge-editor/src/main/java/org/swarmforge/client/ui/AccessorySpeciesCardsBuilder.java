/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.swarmforge.client.util.I18nManager;

/**
 * Builds the UI cards (Taxonomy, Seasonal Dynamics, Predators & Pathogens)
 * for {@link AccessorySpeciesEditorPane}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
class AccessorySpeciesCardsBuilder {

    private final AccessorySpeciesEditorPane pane;
    private final I18nManager i18n = I18nManager.getInstance();

    AccessorySpeciesCardsBuilder(AccessorySpeciesEditorPane pane) {
        this.pane = pane;
    }

    VBox createTaxonomyCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("card-pane");

        Label title = new Label(i18n.get("accessory.card.taxonomy.title"));
        title.getStyleClass().add("card-title-label");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(240);
        col1.setPrefWidth(260);
        col1.setHgrow(Priority.NEVER);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(col1, col2);

        pane.accessoryNameField = new TextField("Seed-Bearing Grasses (Messor)");
        pane.categoryCombo = new ComboBox<>(FXCollections.observableArrayList(
                "APHID_MUTUALIST",
                "DETRITIVORE",
                "FLORA",
                "FUNGI",
                "PATHOGEN_PARASITE",
                "PREDATOR",
                "PREY_INSECT"
        ));
        ComboBoxTooltipHelper.setupDescriptiveComboBox(pane.categoryCombo,
            val -> switch (val) {
                case "FLORA" -> "🌿 Flora (Vegetation & Seeds)";
                case "APHID_MUTUALIST" -> "🐄 Aphid Mutualists (Trophobiosis & Honeydew)";
                case "PREY_INSECT" -> "🐛 Prey Insects (Protein Hunt)";
                case "PREDATOR" -> "🕷️ Predators (Spiders, Antlions, Birds)";
                case "PATHOGEN_PARASITE" -> "🦠 Pathogens & Parasites (Cordyceps, Mites)";
                case "FUNGI" -> "🍄 Symbiotic Fungi (Cultivated Gardens)";
                case "DETRITIVORE" -> "🍂 Detritivores (Springtails & Refuse Recyclers)";
                default -> val;
            },
            val -> switch (val) {
                case "FLORA" -> "Vegetation, nectar-producing plants, and seed-bearing grasses supplying food reserves.";
                case "APHID_MUTUALIST" -> "Aphids and scale insects farmed in trophobiosis for sweet honeydew harvesting.";
                case "PREY_INSECT" -> "Arthropod prey (caterpillars, crickets, flies) hunted for essential protein intake.";
                case "PREDATOR" -> "Natural predators regulating colony populations (spiders, antlions, reptiles, birds).";
                case "PATHOGEN_PARASITE" -> "Parasites and entomopathogenic fungi inducing epidemics and impairing colony health.";
                case "FUNGI" -> "Symbiotic basidiomycete or ascomycete fungi cultivated by leafcutter ants or termites.";
                case "DETRITIVORE" -> "Detritivorous organisms cleaning colony refuse dumps and recycling organic matter.";
                default -> "";
            }
        );
        pane.categoryCombo.getSelectionModel().selectFirst();

        pane.biomeCombo = new ComboBox<>(FXCollections.observableArrayList(
                "ARID_DESERT",
                "MEDITERRANEAN",
                "TAIGA_BOREAL",
                "TEMPERATE_DECIDUOUS",
                "TROPICAL_RAINFOREST"
        ));
        ComboBoxTooltipHelper.setupDescriptiveComboBox(pane.biomeCombo,
            val -> switch (val) {
                case "TEMPERATE_DECIDUOUS" -> "🌳 Temperate Deciduous Forest (4 Seasons)";
                case "MEDITERRANEAN" -> "🌿 Mediterranean Maquis (Dry Summer / Mild Winter)";
                case "TROPICAL_RAINFOREST" -> "🌴 Tropical Rainforest";
                case "ARID_DESERT" -> "🏜️ Arid Desert (Episodic Rain)";
                case "TAIGA_BOREAL" -> "🌲 Boreal Taiga (Short Growing Season)";
                default -> val;
            },
            val -> switch (val) {
                case "TEMPERATE_DECIDUOUS" -> "Temperate climate with 4 distinct seasons, winter diapause, and spring bloom.";
                case "MEDITERRANEAN" -> "Hot arid summers and mild rainy winters favoring granivorous and thermophilic species.";
                case "TROPICAL_RAINFOREST" -> "Consistently high temperature and humidity with intense biodiversity and competition.";
                case "ARID_DESERT" -> "Extreme heat and arid conditions with nocturnal or crepuscular activity patterns.";
                case "TAIGA_BOREAL" -> "Long freezing winters and very short growing season requiring high food reserve storage.";
                default -> "";
            }
        );
        pane.biomeCombo.getSelectionModel().selectFirst();

        pane.latitudeField = new TextField("45.0");
        pane.minTempField = new TextField("5.0");
        pane.optTempField = new TextField("22.0");
        pane.maxTempField = new TextField("35.0");
        pane.growthRateField = new TextField("1.2");
        pane.initialBiomassDensityField = new TextField("150.0");
        pane.initialPopulationDensityField = new TextField("25.0");

        pane.individualCountSpinner = new Spinner<>(0, 10000, 50, 10);
        pane.individualCountSpinner.setEditable(true);
        pane.individualCountSpinner.tooltipProperty().bind(i18n.createTooltipBinding("accessory.field.individual_count.tt"));

        pane.nestDispatchCombo = new ComboBox<>(FXCollections.observableArrayList(
                "All Compatible Host Nests (Biological Filtering)",
                "Brood Chambers Only (Commensals & Parasites)",
                "Exterior Exploration Zone Only (Outside Nests)",
                "Primary Species Nests Only (Nest #1)",
                "Uniform Distribution Across All World Nests"
        ));
        FXCollections.sort(pane.nestDispatchCombo.getItems());
        pane.nestDispatchCombo.getSelectionModel().selectFirst();
        pane.nestDispatchCombo.tooltipProperty().bind(i18n.createTooltipBinding("accessory.field.nest_dispatch.tt"));

        pane.diapauseCheck = new CheckBox(i18n.get("accessory.field.diapause.check"));
        pane.diapauseCheck.setSelected(true);
        Tooltip tDiapause = new Tooltip(i18n.get("accessory.field.diapause.tt"));
        tDiapause.setShowDelay(Duration.millis(100));
        pane.diapauseCheck.setTooltip(tDiapause);

        grid.addRow(0, createLabelKey("accessory.field.name", "accessory.field.name.tt"), pane.accessoryNameField);
        grid.addRow(1, createLabelKey("accessory.field.category", "accessory.field.category.tt"), pane.categoryCombo);
        grid.addRow(2, createLabelKey("accessory.field.biome", "accessory.field.biome.tt"), pane.biomeCombo);
        grid.addRow(3, createLabelKey("accessory.field.latitude", "accessory.field.latitude.tt"), pane.latitudeField);
        grid.addRow(4, createLabelKey("accessory.field.min_temp", "accessory.field.min_temp.tt"), pane.minTempField);
        grid.addRow(5, createLabelKey("accessory.field.opt_temp", "accessory.field.opt_temp.tt"), pane.optTempField);
        grid.addRow(6, createLabelKey("accessory.field.max_temp", "accessory.field.max_temp.tt"), pane.maxTempField);
        grid.addRow(7, createLabelKey("accessory.field.growth_rate", "accessory.field.growth_rate.tt"), pane.growthRateField);
        grid.addRow(8, createLabelKey("accessory.field.biomass_density", "accessory.field.biomass_density.tt"), pane.initialBiomassDensityField);
        grid.addRow(9, createLabelKey("accessory.field.pop_density", "accessory.field.pop_density.tt"), pane.initialPopulationDensityField);
        grid.addRow(10, createLabelKey("accessory.field.individual_count", "accessory.field.individual_count.tt"), pane.individualCountSpinner);
        grid.addRow(11, createLabelKey("accessory.field.nest_dispatch", "accessory.field.nest_dispatch.tt"), pane.nestDispatchCombo);
        grid.addRow(12, createLabelKey("accessory.field.diapause", "accessory.field.diapause.tt"), pane.diapauseCheck);

        card.getChildren().addAll(title, grid);
        return card;
    }

    VBox createSeasonalCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("card-pane");

        Label title = new Label(i18n.get("accessory.card.seasonal.title"));
        title.getStyleClass().add("card-title-label");

        pane.hemisphereCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Northern Hemisphere (Spring = Mar-May, Winter = Dec-Feb)",
                "Southern Hemisphere (Spring = Sep-Nov, Winter = Jun-Aug)",
                "Equatorial / Tropical Zone (Rainy & Dry Seasons)"
        ));
        pane.hemisphereCombo.getSelectionModel().selectFirst();
        pane.hemisphereCombo.tooltipProperty().bind(i18n.createTooltipBinding("accessory.field.hemisphere.tt"));
        pane.hemisphereCombo.setOnAction(e -> updateSeasonLabels());

        pane.seasonSlider1 = createSlider(0.8);
        pane.seasonSlider2 = createSlider(1.0);
        pane.seasonSlider3 = createSlider(0.6);
        pane.seasonSlider4 = createSlider(0.1);

        pane.seasonLabel1 = createLabelKey("accessory.season.spring.north", null);
        pane.seasonLabel2 = createLabelKey("accessory.season.summer.north", null);
        pane.seasonLabel3 = createLabelKey("accessory.season.autumn.north", null);
        pane.seasonLabel4 = createLabelKey("accessory.season.winter.north", null);

        VBox slidersBox = new VBox(10,
                createLabelKey("accessory.field.hemisphere", "accessory.field.hemisphere.tt"),
                pane.hemisphereCombo,
                new Separator(),
                pane.seasonLabel1, pane.seasonSlider1,
                pane.seasonLabel2, pane.seasonSlider2,
                pane.seasonLabel3, pane.seasonSlider3,
                pane.seasonLabel4, pane.seasonSlider4
        );

        pane.seasonHintLabel = new Label(i18n.get("accessory.season.hint"));
        pane.seasonHintLabel.getStyleClass().add("header-subtitle");

        card.getChildren().addAll(title, slidersBox, new Separator(), pane.seasonHintLabel);
        return card;
    }

    void updateSeasonLabels() {
        if (pane.hemisphereCombo == null) return;
        int index = pane.hemisphereCombo.getSelectionModel().getSelectedIndex();
        if (index == 1) {
            pane.seasonLabel1.setText(i18n.get("accessory.season.spring.south"));
            pane.seasonLabel2.setText(i18n.get("accessory.season.summer.south"));
            pane.seasonLabel3.setText(i18n.get("accessory.season.autumn.south"));
            pane.seasonLabel4.setText(i18n.get("accessory.season.winter.south"));
        } else if (index == 2) {
            pane.seasonLabel1.setText(i18n.get("accessory.season.rain1.eq"));
            pane.seasonLabel2.setText(i18n.get("accessory.season.dry1.eq"));
            pane.seasonLabel3.setText(i18n.get("accessory.season.rain2.eq"));
            pane.seasonLabel4.setText(i18n.get("accessory.season.dry2.eq"));
        } else {
            pane.seasonLabel1.setText(i18n.get("accessory.season.spring.north"));
            pane.seasonLabel2.setText(i18n.get("accessory.season.summer.north"));
            pane.seasonLabel3.setText(i18n.get("accessory.season.autumn.north"));
            pane.seasonLabel4.setText(i18n.get("accessory.season.winter.north"));
        }
    }

    VBox createPredatorPathogenCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("card-pane");

        Label title = new Label(i18n.get("accessory.card.predators.title"));
        title.getStyleClass().add("card-title-label");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(240);
        col1.setPrefWidth(260);
        col1.setHgrow(Priority.NEVER);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(col1, col2);

        pane.targetCasteCombo = new ComboBox<>(FXCollections.observableArrayList("All Castes", "Brood / Pupae", "Queens / Alates", "Workers"));
        FXCollections.sort(pane.targetCasteCombo.getItems());
        pane.targetCasteCombo.getSelectionModel().selectFirst();
        Tooltip tcTt = new Tooltip(i18n.get("accessory.field.target_caste.tt"));
        tcTt.setShowDelay(Duration.millis(100));
        pane.targetCasteCombo.setTooltip(tcTt);

        pane.huntModeCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Ambush / Stalking (Spider)",
                "Direct Attack (Bird / Anteater)",
                "Parasitoid (Internal Egg-laying / Wasp)",
                "Trap / Funnel (Antlion)"
        ));
        FXCollections.sort(pane.huntModeCombo.getItems());
        pane.huntModeCombo.getSelectionModel().selectFirst();
        Tooltip hmTt = new Tooltip(i18n.get("accessory.field.hunt_mode.tt"));
        hmTt.setShowDelay(Duration.millis(100));
        pane.huntModeCombo.setTooltip(hmTt);

        pane.killRateField = new TextField("3.5");

        pane.pathogenVectorCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Airborne Spores (Cordyceps)",
                "Contaminated Food",
                "Grooming / Allogrooming",
                "Soil & Gallery Contact"
        ));
        FXCollections.sort(pane.pathogenVectorCombo.getItems());
        pane.pathogenVectorCombo.getSelectionModel().selectFirst();
        Tooltip pvTt = new Tooltip(i18n.get("accessory.field.pathogen_vector.tt"));
        pvTt.setShowDelay(Duration.millis(100));
        pane.pathogenVectorCombo.setTooltip(pvTt);

        pane.transmissionR0Field = new TextField("2.4");
        pane.incubationDaysField = new TextField("4.0");
        pane.mortalityRateField = new TextField("15.0");

        grid.addRow(0, createLabelKey("accessory.field.target_caste", "accessory.field.target_caste.tt"), pane.targetCasteCombo);
        grid.addRow(1, createLabelKey("accessory.field.hunt_mode", "accessory.field.hunt_mode.tt"), pane.huntModeCombo);
        grid.addRow(2, createLabelKey("accessory.field.kill_rate", "accessory.field.kill_rate.tt"), pane.killRateField);
        grid.addRow(3, createLabelKey("accessory.field.pathogen_vector", "accessory.field.pathogen_vector.tt"), pane.pathogenVectorCombo);
        grid.addRow(4, createLabelKey("accessory.field.transmission_r0", "accessory.field.transmission_r0.tt"), pane.transmissionR0Field);
        grid.addRow(5, createLabelKey("accessory.field.incubation_days", "accessory.field.incubation_days.tt"), pane.incubationDaysField);
        grid.addRow(6, createLabelKey("accessory.field.mortality_rate", "accessory.field.mortality_rate.tt"), pane.mortalityRateField);

        card.getChildren().addAll(title, grid);
        return card;
    }

    Slider createSlider(double initialVal) {
        Slider s = new Slider(0.0, 1.0, initialVal);
        s.setShowTickLabels(true);
        s.setShowTickMarks(true);
        s.setMajorTickUnit(0.25);
        Tooltip t = new Tooltip(i18n.get("accessory.season.multiplier.tt"));
        t.setShowDelay(Duration.millis(100));
        Tooltip.install(s, t);
        return s;
    }

    Label createLabelKey(String keyText, String keyTooltip) {
        return createLabelKey(keyText, keyTooltip, null);
    }

    Label createLabelKey(String keyText, String keyTooltip, String glossaryTerm) {
        Label l = new Label(i18n.get(keyText));
        l.getStyleClass().add("bold-label");
        l.setWrapText(true);
        l.setMinHeight(Region.USE_PREF_SIZE);
        l.setMinWidth(Region.USE_PREF_SIZE);
        if (keyTooltip != null && !keyTooltip.isEmpty()) {
            Tooltip t = new Tooltip(i18n.get(keyTooltip));
            t.setShowDelay(Duration.millis(100));
            t.setMaxWidth(320);
            t.setWrapText(true);
            l.setTooltip(t);
        }
        if (glossaryTerm != null && !glossaryTerm.isEmpty()) {
            l.getStyleClass().add("glossary-link");
            l.setCursor(javafx.scene.Cursor.HAND);
            l.setOnMouseClicked(e -> org.swarmforge.client.ui.GlossaryDialog.show(glossaryTerm));
        }
        return l;
    }
}
