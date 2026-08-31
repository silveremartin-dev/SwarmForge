/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.client.util.I18nManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.swarmforge.client.util.NotificationOverlay;

/**
 * Dedicated Editor Pane for Accessory & Predator Species (Flora, Aphids, Prey Insects, Predators, Pathogens, Fungi, Detritivores)
 * with realistic latitude, photoperiod, seasonal dynamics, surface-relative densities, pedagogical tooltips,
 * integrated searchable help documentation, and safety confirmation dialogs.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class AccessorySpeciesEditorPane extends VBox {

    private final I18nManager i18n = I18nManager.getInstance();

    // UI Controls - Header & Toolbar
    private Label headerLabel;
    private Label lblPreset;
    private ComboBox<String> accessoryPresetCombo;
    private Button btnSave;
    private Button btnDelete;
    private Button btnExport;
    private Button btnImport;

    // Tabs
    private TabPane tabPane;
    private Tab tabTaxonomy;
    private Tab tabSeasonal;
    private Tab tabPredators;

    // UI Controls - Taxonomy & Thermal
    private TextField accessoryNameField;
    private ComboBox<String> categoryCombo;
    private ComboBox<String> biomeCombo;
    private TextField latitudeField;

    // Thermal & Density Parameters
    private TextField minTempField;
    private TextField optTempField;
    private TextField maxTempField;
    private TextField growthRateField;
    private TextField initialBiomassDensityField;
    private TextField initialPopulationDensityField;
    private Spinner<Integer> individualCountSpinner;
    private ComboBox<String> nestDispatchCombo;
    private CheckBox diapauseCheck;

    // Seasonal Controls & Hemisphere
    private ComboBox<String> hemisphereCombo;
    private Label seasonLabel1;
    private Label seasonLabel2;
    private Label seasonLabel3;
    private Label seasonLabel4;
    private Slider seasonSlider1;
    private Slider seasonSlider2;
    private Slider seasonSlider3;
    private Slider seasonSlider4;
    private Label seasonHintLabel;

    // Predators & Pathogens Controls
    private ComboBox<String> targetCasteCombo;
    private ComboBox<String> huntModeCombo;
    private TextField killRateField;
    private ComboBox<String> pathogenVectorCombo;
    private TextField transmissionR0Field;
    private TextField incubationDaysField;
    private TextField mortalityRateField;

    private boolean isDirty = false;
    private boolean isUpdatingFields = false;
    private String lastSelectedPreset = null;

    public boolean isDirty() {
        return isDirty;
    }

    public boolean promptUnsavedChanges() {
        if (!isDirty) return true;
        I18nManager i18n = I18nManager.getInstance();

        String currentName = lastSelectedPreset != null ? lastSelectedPreset : "";
        boolean hasCurrentPreset = !currentName.isEmpty();

        Alert alert = org.swarmforge.client.util.ThemeManager.createAlert(
            Alert.AlertType.CONFIRMATION,
            "You have unsaved changes in the Accessory Species Editor.\n"
            + (hasCurrentPreset ? "Current preset: \"" + currentName + "\"" : "No preset selected.")
        );
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText("Exit Accessory Species Editor?");

        ButtonType btnUpdate   = hasCurrentPreset
            ? new ButtonType("💾 Update \"" + currentName + "\"", ButtonBar.ButtonData.OK_DONE)
            : null;
        ButtonType btnSaveAs   = new ButtonType("📝 Save As...", ButtonBar.ButtonData.OTHER);
        ButtonType btnDiscard  = new ButtonType("🗑 Discard", ButtonBar.ButtonData.OTHER);
        ButtonType btnCancel   = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        if (btnUpdate != null) {
            alert.getButtonTypes().setAll(btnUpdate, btnSaveAs, btnDiscard, btnCancel);
        } else {
            alert.getButtonTypes().setAll(btnSaveAs, btnDiscard, btnCancel);
        }
        java.util.Optional<ButtonType> result = alert.showAndWait();

        if (!result.isPresent() || result.get() == btnCancel) {
            return false;
        }
        if (result.get() == btnDiscard) {
            isDirty = false;
            return true;
        }
        if (btnUpdate != null && result.get() == btnUpdate) {
            // Mise à jour du preset courant
            handleAddPreset();
            return !isDirty;
        }
        if (result.get() == btnSaveAs) {
            // Saisie d'un nouveau nom
            TextInputDialog dlg = new TextInputDialog(currentName.isEmpty() ? "nouveau-preset" : currentName + " (copie)");
            dlg.setTitle("Save As");
            dlg.setHeaderText("New accessory species preset name:");
            dlg.setContentText("Name:");
            org.swarmforge.client.util.ThemeManager.getInstance().applyTheme(dlg.getDialogPane().getScene());
            java.util.Optional<String> nameResult = dlg.showAndWait();
            if (nameResult.isPresent() && !nameResult.get().trim().isEmpty()) {
                String newName = nameResult.get().trim();
                isUpdatingFields = true;
                try {
                    if (!accessoryPresetCombo.getItems().contains(newName)) {
                        accessoryPresetCombo.getItems().add(newName);
                    }
                    accessoryPresetCombo.getSelectionModel().select(newName);
                } finally {
                    isUpdatingFields = false;
                }
                lastSelectedPreset = newName;
                isDirty = false;
                NotificationOverlay.show(this, "Accessory species preset saved as: " + newName, NotificationOverlay.NotificationType.SUCCESS);
                return true;
            }
            return false;
        }
        return false;
    }

    public AccessorySpeciesEditorPane() {
        setSpacing(10);

        // Main TabPane
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        tabTaxonomy = new Tab(i18n.get("accessory.tab.taxonomy"));
        tabTaxonomy.setContent(new ScrollPane(createTaxonomyCard()));

        tabSeasonal = new Tab(i18n.get("accessory.tab.seasonal"));
        tabSeasonal.setContent(new ScrollPane(createSeasonalCard()));

        tabPredators = new Tab(i18n.get("accessory.tab.predators"));
        tabPredators.setContent(new ScrollPane(createPredatorPathogenCard()));

        tabPane.getTabs().addAll(tabTaxonomy, tabSeasonal, tabPredators);

        // Dynamic Locale Listener for UI Updating
        i18n.localeProperty().addListener((obs, oldL, newL) -> refreshI18nLabels());

        getChildren().addAll(buildHeader(), tabPane);

        attachUserChangeListeners();
        if (!accessoryPresetCombo.getItems().isEmpty()) {
            applyAccessoryPreset(accessoryPresetCombo.getValue());
        }
    }

    private VBox buildHeader() {
        VBox v = new VBox(6);
        v.setPadding(new Insets(8, 10, 5, 10));

        HBox r = new HBox(8);
        r.setAlignment(Pos.CENTER_LEFT);

        headerLabel = new Label(i18n.get("accessory.title"));
        headerLabel.getStyleClass().add("title-header-label");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        HBox toolbar = createToolbar();

        r.getChildren().addAll(headerLabel, sp, toolbar);
        v.getChildren().addAll(r, new Separator());
        return v;
    }

    private HBox createToolbar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);

        lblPreset = new Label();
        lblPreset.textProperty().bind(i18n.createStringBinding("preset.label"));
        lblPreset.getStyleClass().add("bold-label");
        lblPreset.setGraphic(new FontIcon(Feather.SLIDERS));

        accessoryPresetCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Seed-Bearing Grasses (Messor / Seeds & Biomass)",
                "Nectar Flowers & EFN (Acacia Nectaries)",
                "Humid Moss (Polytrichum / Substrate)",
                "Pine Aphids (Cinara pini / Honeydew)",
                "Root Mealybugs (Eurhizococcus / Subterranean)",
                "Mealworm Larvae (Protein Prey)",
                "Termite Prey (Microtermes / Food)",
                "Antlion Pitfall (Myrmeleon / Funnel Trap)",
                "Jumping Spider (Salticidae / Ambush)",
                "Asian Hornet (Flying Bee Hunter)",
                "European Bee-eater Wasp (Philanthus / Bee Hunter)",
                "European Honey Buzzard (Raptor Wasp Hunter)",
                "Megaponera Termite Raider (Termite Specialist)",
                "Black Woodpecker (Bark Beetle & Ant Predator)",
                "Tamandua Anteater (Direct Nest Raid)",
                "Parasitoid Wasp (Eucharitidae / Egg-laying)",
                "Entomopathogenic Fungus (Zombie Cordyceps)",
                "Parasitic Mite (Varroa destructor)",
                "Intestinal Microsporidian (Nosema bombi)",
                "Atta Symbiotic Fungus (Leucoagaricus)",
                "Termite Cultivated Fungus (Termitomyces)",
                "Garbage Springtails (Detritivore Cleaner)",
                "Myrmecophilous Beetle (Lomechusa Commensal)"
        ));
        FXCollections.sort(accessoryPresetCombo.getItems());
        accessoryPresetCombo.setEditable(true);
        accessoryPresetCombo.promptTextProperty().bind(i18n.createStringBinding("preset.prompt"));
        Tooltip presetTt = new Tooltip(i18n.get("accessory.preset.label.tt"));
        presetTt.setShowDelay(Duration.millis(100));
        accessoryPresetCombo.setTooltip(presetTt);
        accessoryPresetCombo.getSelectionModel().selectFirst();
        accessoryPresetCombo.setPrefWidth(300);

        accessoryPresetCombo.setOnAction(e -> {
            if (isUpdatingFields) return;
            String sel = accessoryPresetCombo.getValue();
            if (sel == null || sel.equals(lastSelectedPreset)) return;

            if (isDirty) {
                Alert alert = org.swarmforge.client.util.ThemeManager.createAlert(
                    Alert.AlertType.CONFIRMATION,
                    "Warning: You have unsaved changes in the current accessory species.\n\nDo you really want to load preset '" + sel + "' and discard changes?"
                );
                alert.setTitle("Unsaved Changes");
                alert.setHeaderText("Accessory Preset Change");
                java.util.Optional<ButtonType> res = alert.showAndWait();
                if (res.isEmpty() || res.get() != ButtonType.OK) {
                    isUpdatingFields = true;
                    try {
                        accessoryPresetCombo.setValue(lastSelectedPreset);
                    } finally {
                        isUpdatingFields = false;
                    }
                    return;
                }
            }

            if (sel != null) {
                lastSelectedPreset = sel;
                applyAccessoryPreset(sel);
            }
        });

        btnSave = new Button();
        btnSave.setGraphic(new FontIcon(Feather.SAVE));
        btnSave.textProperty().bind(i18n.createStringBinding("preset.save"));
        btnSave.getStyleClass().add("btn-secondary");
        btnSave.tooltipProperty().bind(i18n.createTooltipBinding("preset.save.tt"));
        btnSave.setOnAction(e -> handleAddPreset());

        btnDelete = new Button();
        btnDelete.setGraphic(new FontIcon(Feather.TRASH_2));
        btnDelete.textProperty().bind(i18n.createStringBinding("preset.delete"));
        btnDelete.getStyleClass().add("btn-danger");
        btnDelete.tooltipProperty().bind(i18n.createTooltipBinding("preset.delete.tt"));
        btnDelete.setOnAction(e -> handleDeletePreset());

        btnExport = new Button();
        btnExport.setGraphic(new FontIcon(Feather.DOWNLOAD));
        btnExport.textProperty().bind(i18n.createStringBinding("preset.export"));
        btnExport.getStyleClass().add("btn-secondary");
        btnExport.tooltipProperty().bind(i18n.createTooltipBinding("preset.export.tt"));
        btnExport.setOnAction(e -> handleSave());

        btnImport = new Button();
        btnImport.setGraphic(new FontIcon(Feather.UPLOAD));
        btnImport.textProperty().bind(i18n.createStringBinding("preset.import"));
        btnImport.getStyleClass().add("btn-secondary");
        btnImport.tooltipProperty().bind(i18n.createTooltipBinding("preset.import.tt"));
        btnImport.setOnAction(e -> handleLoad());

        bar.getChildren().addAll(lblPreset, accessoryPresetCombo, btnSave, btnDelete, new Separator(Orientation.VERTICAL), btnExport, btnImport);
        return bar;
    }

    private VBox createTaxonomyCard() {
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

        accessoryNameField = new TextField("Seed-Bearing Grasses (Messor)");
        categoryCombo = new ComboBox<>(FXCollections.observableArrayList(
                "APHID_MUTUALIST",
                "DETRITIVORE",
                "FLORA",
                "FUNGI",
                "PATHOGEN_PARASITE",
                "PREDATOR",
                "PREY_INSECT"
        ));
        ComboBoxTooltipHelper.setupDescriptiveComboBox(categoryCombo,
            val -> switch (val) {
                case "FLORA" -> "🌿 FLORA (Plants & Seeds)";
                case "APHID_MUTUALIST" -> "🐄 APHID_MUTUALIST (Aphids & Honeydew)";
                case "PREY_INSECT" -> "🐛 PREY_INSECT (Prey Insects)";
                case "PREDATOR" -> "🕷️ PREDATOR (Spiders, Antlions, Birds)";
                case "PATHOGEN_PARASITE" -> "🦠 PATHOGEN_PARASITE (Cordyceps, Mites)";
                case "FUNGI" -> "🍄 FUNGI (Symbiotic Fungi)";
                case "DETRITIVORE" -> "🍂 DETRITIVORE (Springtails & Woodlice)";
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
        categoryCombo.getSelectionModel().selectFirst();

        biomeCombo = new ComboBox<>(FXCollections.observableArrayList(
                "ARID_DESERT",
                "MEDITERRANEAN",
                "TAIGA_BOREAL",
                "TEMPERATE_DECIDUOUS",
                "TROPICAL_RAINFOREST"
        ));
        ComboBoxTooltipHelper.setupDescriptiveComboBox(biomeCombo,
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
        biomeCombo.getSelectionModel().selectFirst();

        latitudeField = new TextField("45.0");
        minTempField = new TextField("5.0");
        optTempField = new TextField("22.0");
        maxTempField = new TextField("35.0");
        growthRateField = new TextField("1.2");
        initialBiomassDensityField = new TextField("150.0");
        initialPopulationDensityField = new TextField("25.0");

        individualCountSpinner = new Spinner<>(0, 10000, 50, 10);
        individualCountSpinner.setEditable(true);
        individualCountSpinner.tooltipProperty().bind(i18n.createTooltipBinding("accessory.field.individual_count.tt"));

        nestDispatchCombo = new ComboBox<>(FXCollections.observableArrayList(
                "All Compatible Host Nests (Biological Filtering)",
                "Brood Chambers Only (Commensals & Parasites)",
                "Exterior Exploration Zone Only (Outside Nests)",
                "Primary Species Nests Only (Nest #1)",
                "Uniform Distribution Across All World Nests"
        ));
        FXCollections.sort(nestDispatchCombo.getItems());
        nestDispatchCombo.getSelectionModel().selectFirst();
        nestDispatchCombo.tooltipProperty().bind(i18n.createTooltipBinding("accessory.field.nest_dispatch.tt"));

        diapauseCheck = new CheckBox(i18n.get("accessory.field.diapause.check"));
        diapauseCheck.setSelected(true);
        Tooltip tDiapause = new Tooltip(i18n.get("accessory.field.diapause.tt"));
        tDiapause.setShowDelay(Duration.millis(100));
        diapauseCheck.setTooltip(tDiapause);

        grid.addRow(0, createLabelKey("accessory.field.name", "accessory.field.name.tt"), accessoryNameField);
        grid.addRow(1, createLabelKey("accessory.field.category", "accessory.field.category.tt"), categoryCombo);
        grid.addRow(2, createLabelKey("accessory.field.biome", "accessory.field.biome.tt"), biomeCombo);
        grid.addRow(3, createLabelKey("accessory.field.latitude", "accessory.field.latitude.tt"), latitudeField);
        grid.addRow(4, createLabelKey("accessory.field.min_temp", "accessory.field.min_temp.tt"), minTempField);
        grid.addRow(5, createLabelKey("accessory.field.opt_temp", "accessory.field.opt_temp.tt"), optTempField);
        grid.addRow(6, createLabelKey("accessory.field.max_temp", "accessory.field.max_temp.tt"), maxTempField);
        grid.addRow(7, createLabelKey("accessory.field.growth_rate", "accessory.field.growth_rate.tt"), growthRateField);
        grid.addRow(8, createLabelKey("accessory.field.biomass_density", "accessory.field.biomass_density.tt"), initialBiomassDensityField);
        grid.addRow(9, createLabelKey("accessory.field.pop_density", "accessory.field.pop_density.tt"), initialPopulationDensityField);
        grid.addRow(10, createLabelKey("accessory.field.individual_count", "accessory.field.individual_count.tt"), individualCountSpinner);
        grid.addRow(11, createLabelKey("accessory.field.nest_dispatch", "accessory.field.nest_dispatch.tt"), nestDispatchCombo);
        grid.addRow(12, createLabelKey("accessory.field.diapause", "accessory.field.diapause.tt"), diapauseCheck);

        card.getChildren().addAll(title, grid);
        return card;
    }

    private VBox createSeasonalCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("card-pane");

        Label title = new Label(i18n.get("accessory.card.seasonal.title"));
        title.getStyleClass().add("card-title-label");

        hemisphereCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Northern Hemisphere (Spring = Mar-May, Winter = Dec-Feb)",
                "Southern Hemisphere (Spring = Sep-Nov, Winter = Jun-Aug)",
                "Equatorial / Tropical Zone (Rainy & Dry Seasons)"
        ));
        hemisphereCombo.getSelectionModel().selectFirst();
        hemisphereCombo.tooltipProperty().bind(i18n.createTooltipBinding("accessory.field.hemisphere.tt"));
        hemisphereCombo.setOnAction(e -> updateSeasonLabels());

        seasonSlider1 = createSlider(0.8);
        seasonSlider2 = createSlider(1.0);
        seasonSlider3 = createSlider(0.6);
        seasonSlider4 = createSlider(0.1);

        seasonLabel1 = createLabelKey("accessory.season.spring.north", null);
        seasonLabel2 = createLabelKey("accessory.season.summer.north", null);
        seasonLabel3 = createLabelKey("accessory.season.autumn.north", null);
        seasonLabel4 = createLabelKey("accessory.season.winter.north", null);

        VBox slidersBox = new VBox(10,
                createLabelKey("accessory.field.hemisphere", "accessory.field.hemisphere.tt"),
                hemisphereCombo,
                new Separator(),
                seasonLabel1, seasonSlider1,
                seasonLabel2, seasonSlider2,
                seasonLabel3, seasonSlider3,
                seasonLabel4, seasonSlider4
        );

        seasonHintLabel = new Label(i18n.get("accessory.season.hint"));
        seasonHintLabel.getStyleClass().add("header-subtitle");

        card.getChildren().addAll(title, slidersBox, new Separator(), seasonHintLabel);
        return card;
    }

    private void updateSeasonLabels() {
        int index = hemisphereCombo.getSelectionModel().getSelectedIndex();
        if (index == 1) {
            // Southern Hemisphere
            seasonLabel1.setText(i18n.get("accessory.season.spring.south"));
            seasonLabel2.setText(i18n.get("accessory.season.summer.south"));
            seasonLabel3.setText(i18n.get("accessory.season.autumn.south"));
            seasonLabel4.setText(i18n.get("accessory.season.winter.south"));
        } else if (index == 2) {
            // Equatorial / Intertropical
            seasonLabel1.setText(i18n.get("accessory.season.rain1.eq"));
            seasonLabel2.setText(i18n.get("accessory.season.dry1.eq"));
            seasonLabel3.setText(i18n.get("accessory.season.rain2.eq"));
            seasonLabel4.setText(i18n.get("accessory.season.dry2.eq"));
        } else {
            // Northern Hemisphere (Default)
            seasonLabel1.setText(i18n.get("accessory.season.spring.north"));
            seasonLabel2.setText(i18n.get("accessory.season.summer.north"));
            seasonLabel3.setText(i18n.get("accessory.season.autumn.north"));
            seasonLabel4.setText(i18n.get("accessory.season.winter.north"));
        }
    }

    private VBox createPredatorPathogenCard() {
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

        targetCasteCombo = new ComboBox<>(FXCollections.observableArrayList("All Castes", "Brood / Pupae", "Queens / Alates", "Workers"));
        FXCollections.sort(targetCasteCombo.getItems());
        targetCasteCombo.getSelectionModel().selectFirst();
        Tooltip tcTt = new Tooltip(i18n.get("accessory.field.target_caste.tt"));
        tcTt.setShowDelay(Duration.millis(100));
        targetCasteCombo.setTooltip(tcTt);

        huntModeCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Ambush / Stalking (Spider)",
                "Direct Attack (Bird / Anteater)",
                "Parasitoid (Internal Egg-laying / Wasp)",
                "Trap / Funnel (Antlion)"
        ));
        FXCollections.sort(huntModeCombo.getItems());
        huntModeCombo.getSelectionModel().selectFirst();
        Tooltip hmTt = new Tooltip(i18n.get("accessory.field.hunt_mode.tt"));
        hmTt.setShowDelay(Duration.millis(100));
        huntModeCombo.setTooltip(hmTt);

        killRateField = new TextField("3.5");

        pathogenVectorCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Airborne Spores (Cordyceps)",
                "Contaminated Food",
                "Grooming / Allogrooming",
                "Soil & Gallery Contact"
        ));
        FXCollections.sort(pathogenVectorCombo.getItems());
        pathogenVectorCombo.getSelectionModel().selectFirst();
        Tooltip pvTt = new Tooltip(i18n.get("accessory.field.pathogen_vector.tt"));
        pvTt.setShowDelay(Duration.millis(100));
        pathogenVectorCombo.setTooltip(pvTt);

        transmissionR0Field = new TextField("2.4");
        incubationDaysField = new TextField("4.0");
        mortalityRateField = new TextField("15.0");

        grid.addRow(0, createLabelKey("accessory.field.target_caste", "accessory.field.target_caste.tt"), targetCasteCombo);
        grid.addRow(1, createLabelKey("accessory.field.hunt_mode", "accessory.field.hunt_mode.tt"), huntModeCombo);
        grid.addRow(2, createLabelKey("accessory.field.kill_rate", "accessory.field.kill_rate.tt"), killRateField);
        grid.addRow(3, createLabelKey("accessory.field.pathogen_vector", "accessory.field.pathogen_vector.tt"), pathogenVectorCombo);
        grid.addRow(4, createLabelKey("accessory.field.transmission_r0", "accessory.field.transmission_r0.tt"), transmissionR0Field);
        grid.addRow(5, createLabelKey("accessory.field.incubation_days", "accessory.field.incubation_days.tt"), incubationDaysField);
        grid.addRow(6, createLabelKey("accessory.field.mortality_rate", "accessory.field.mortality_rate.tt"), mortalityRateField);

        card.getChildren().addAll(title, grid);
        return card;
    }

    private Slider createSlider(double initialVal) {
        Slider s = new Slider(0.0, 1.0, initialVal);
        s.setShowTickLabels(true);
        s.setShowTickMarks(true);
        s.setMajorTickUnit(0.25);
        Tooltip t = new Tooltip(i18n.get("accessory.season.multiplier.tt"));
        t.setShowDelay(Duration.millis(100));
        Tooltip.install(s, t);
        return s;
    }

    private Label createLabelKey(String keyText, String keyTooltip) {
        return createLabelKey(keyText, keyTooltip, null);
    }

    private Label createLabelKey(String keyText, String keyTooltip, String glossaryTerm) {
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

    private void handleAddPreset() {
        String editedText = accessoryPresetCombo.getEditor() != null ? accessoryPresetCombo.getEditor().getText().trim() : "";
        String fieldText = accessoryNameField != null ? accessoryNameField.getText().trim() : "";
        String name = !editedText.isEmpty() ? editedText : (!fieldText.isEmpty() ? fieldText : "swarmforge-accessory-custom");

        if (accessoryPresetCombo.getItems().contains(name)) {
            Alert confirmAlert = org.swarmforge.client.util.ThemeManager.createAlert(
                Alert.AlertType.CONFIRMATION,
                String.format(i18n.get("preset.delete.confirm"), name)
            );
            confirmAlert.setTitle(i18n.get("preset.delete.title"));
            confirmAlert.setHeaderText(i18n.get("accessory.delete.confirm_header"));
            java.util.Optional<ButtonType> res = confirmAlert.showAndWait();
            if (res.isEmpty() || res.get() != ButtonType.OK) {
                return;
            }
        }

        isUpdatingFields = true;
        try {
            if (!accessoryPresetCombo.getItems().contains(name)) {
                accessoryPresetCombo.getItems().add(name);
            }
            accessoryPresetCombo.getSelectionModel().select(name);
        } finally {
            isUpdatingFields = false;
        }
        lastSelectedPreset = name;
        isDirty = false;
        NotificationOverlay.show(this, i18n.get("accessory.preset.save") + " : " + name, NotificationOverlay.NotificationType.SUCCESS);
    }

    private void handleLoad() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(i18n.get("nest.preset.import"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File f = chooser.showOpenDialog(getScene().getWindow());
        if (f != null) {
            NotificationOverlay.show(this, i18n.get("nest.preset.import") + " : " + f.getName(), NotificationOverlay.NotificationType.INFO);
        }
    }

    private void handleSave() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(i18n.get("nest.preset.export"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        chooser.setInitialFileName("swarmforge-accessory-" + (accessoryNameField != null ? accessoryNameField.getText().toLowerCase().replaceAll("[^a-z0-9]+", "-") : "custom") + ".json");

        File f = chooser.showSaveDialog(getScene().getWindow());
        if (f != null) {
            NotificationOverlay.show(this, i18n.get("nest.preset.export") + " : " + f.getName(), NotificationOverlay.NotificationType.SUCCESS);
        }
    }

    private void handleDeletePreset() {
        Alert confirmAlert = org.swarmforge.client.util.ThemeManager.createAlert(
            Alert.AlertType.CONFIRMATION,
            String.format(i18n.get("preset.delete.confirm"), accessoryPresetCombo.getValue())
        );
        confirmAlert.setTitle(i18n.get("preset.delete.title"));
        confirmAlert.setHeaderText(i18n.get("accessory.delete.confirm_header"));

        confirmAlert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String selected = accessoryPresetCombo.getValue();
                if (selected != null) {
                    accessoryPresetCombo.getItems().remove(selected);
                    if (!accessoryPresetCombo.getItems().isEmpty()) {
                        accessoryPresetCombo.getSelectionModel().selectFirst();
                    }
                }
                NotificationOverlay.show(this, i18n.get("preset.delete.title"), NotificationOverlay.NotificationType.INFO);
            }
        });
    }

    private void refreshI18nLabels() {
        if (headerLabel != null) headerLabel.setText(i18n.get("accessory.title"));

        if (tabTaxonomy != null) tabTaxonomy.setText(i18n.get("accessory.tab.taxonomy"));
        if (tabSeasonal != null) tabSeasonal.setText(i18n.get("accessory.tab.seasonal"));
        if (tabPredators != null) tabPredators.setText(i18n.get("accessory.tab.predators"));

        updateSeasonLabels();
        if (seasonHintLabel != null) {
            seasonHintLabel.setText(i18n.get("accessory.season.hint"));
        }
    }

    private void clearPresetSelection() {
        if (!isUpdatingFields && accessoryPresetCombo != null) {
            isDirty = true;
            isUpdatingFields = true;
            try {
                accessoryPresetCombo.getSelectionModel().clearSelection();
            } finally {
                isUpdatingFields = false;
            }
        }
    }

    private void attachUserChangeListeners() {
        Runnable clearLsn = this::clearPresetSelection;
        if (accessoryNameField != null) accessoryNameField.textProperty().addListener((o, a, b) -> clearLsn.run());
        if (categoryCombo != null) categoryCombo.valueProperty().addListener((o, a, b) -> clearLsn.run());
        if (biomeCombo != null) biomeCombo.valueProperty().addListener((o, a, b) -> clearLsn.run());
        if (latitudeField != null) latitudeField.textProperty().addListener((o, a, b) -> clearLsn.run());
        if (minTempField != null) minTempField.textProperty().addListener((o, a, b) -> clearLsn.run());
        if (optTempField != null) optTempField.textProperty().addListener((o, a, b) -> clearLsn.run());
        if (maxTempField != null) maxTempField.textProperty().addListener((o, a, b) -> clearLsn.run());
        if (growthRateField != null) growthRateField.textProperty().addListener((o, a, b) -> clearLsn.run());
        if (initialBiomassDensityField != null) initialBiomassDensityField.textProperty().addListener((o, a, b) -> clearLsn.run());
        if (initialPopulationDensityField != null) initialPopulationDensityField.textProperty().addListener((o, a, b) -> clearLsn.run());
        if (diapauseCheck != null) diapauseCheck.selectedProperty().addListener((o, a, b) -> clearLsn.run());
        if (hemisphereCombo != null) hemisphereCombo.valueProperty().addListener((o, a, b) -> clearLsn.run());
        if (seasonSlider1 != null) seasonSlider1.valueProperty().addListener((o, a, b) -> clearLsn.run());
        if (seasonSlider2 != null) seasonSlider2.valueProperty().addListener((o, a, b) -> clearLsn.run());
        if (seasonSlider3 != null) seasonSlider3.valueProperty().addListener((o, a, b) -> clearLsn.run());
        if (seasonSlider4 != null) seasonSlider4.valueProperty().addListener((o, a, b) -> clearLsn.run());
        if (targetCasteCombo != null) targetCasteCombo.valueProperty().addListener((o, a, b) -> clearLsn.run());
        if (huntModeCombo != null) huntModeCombo.valueProperty().addListener((o, a, b) -> clearLsn.run());
        if (killRateField != null) killRateField.textProperty().addListener((o, a, b) -> clearLsn.run());
        if (pathogenVectorCombo != null) pathogenVectorCombo.valueProperty().addListener((o, a, b) -> clearLsn.run());
        if (transmissionR0Field != null) transmissionR0Field.textProperty().addListener((o, a, b) -> clearLsn.run());
        if (incubationDaysField != null) incubationDaysField.textProperty().addListener((o, a, b) -> clearLsn.run());
        if (mortalityRateField != null) mortalityRateField.textProperty().addListener((o, a, b) -> clearLsn.run());
    }

    private void applyAccessoryPreset(String name) {
        if (name == null || name.isEmpty()) return;
        isUpdatingFields = true;
        try {
            if (name.contains("Grasses") || name.contains("Graminées")) {
                accessoryNameField.setText("Seed-Bearing Grasses (Messor)");
                categoryCombo.setValue("FLORA");
                biomeCombo.setValue("TEMPERATE_DECIDUOUS");
                latitudeField.setText("45.0");
                minTempField.setText("5.0");
                optTempField.setText("22.0");
                maxTempField.setText("35.0");
                growthRateField.setText("1.2");
                initialBiomassDensityField.setText("150.0");
                initialPopulationDensityField.setText("25.0");
                diapauseCheck.setSelected(true);
                hemisphereCombo.getSelectionModel().select(0);
                seasonSlider1.setValue(0.8); seasonSlider2.setValue(1.0); seasonSlider3.setValue(0.6); seasonSlider4.setValue(0.1);
                targetCasteCombo.setValue("All Castes");
                huntModeCombo.setValue("Direct Attack (Bird / Anteater)");
                killRateField.setText("0.0");
                pathogenVectorCombo.setValue("Airborne Spores (Cordyceps)");
                transmissionR0Field.setText("0.0"); incubationDaysField.setText("0.0"); mortalityRateField.setText("0.0");
            } else if (name.contains("Aphids") || name.contains("Pucerons")) {
                accessoryNameField.setText("Pine Aphids (Cinara / Honeydew)");
                categoryCombo.setValue("APHID_MUTUALIST");
                biomeCombo.setValue("TEMPERATE_DECIDUOUS");
                latitudeField.setText("48.5");
                minTempField.setText("8.0"); optTempField.setText("20.0"); maxTempField.setText("30.0");
                growthRateField.setText("2.5");
                initialBiomassDensityField.setText("80.0"); initialPopulationDensityField.setText("100.0");
                diapauseCheck.setSelected(true);
                hemisphereCombo.getSelectionModel().select(0);
                seasonSlider1.setValue(0.7); seasonSlider2.setValue(1.0); seasonSlider3.setValue(0.5); seasonSlider4.setValue(0.0);
                targetCasteCombo.setValue("Workers");
                huntModeCombo.setValue("Ambush / Stalking (Spider)");
                killRateField.setText("0.2");
                pathogenVectorCombo.setValue("Grooming / Allogrooming");
                transmissionR0Field.setText("1.2"); incubationDaysField.setText("5.0"); mortalityRateField.setText("5.0");
            } else if (name.contains("Mealworm") || name.contains("Ténébrion")) {
                accessoryNameField.setText("Mealworm Larvae (Protein Prey)");
                categoryCombo.setValue("PREY_INSECT");
                biomeCombo.setValue("TEMPERATE_DECIDUOUS");
                latitudeField.setText("43.0");
                minTempField.setText("10.0"); optTempField.setText("25.0"); maxTempField.setText("38.0");
                growthRateField.setText("3.0");
                initialBiomassDensityField.setText("200.0"); initialPopulationDensityField.setText("50.0");
                diapauseCheck.setSelected(false);
                seasonSlider1.setValue(0.6); seasonSlider2.setValue(0.9); seasonSlider3.setValue(0.7); seasonSlider4.setValue(0.2);
                targetCasteCombo.setValue("Workers");
                huntModeCombo.setValue("Direct Attack (Bird / Anteater)");
                killRateField.setText("1.5");
                transmissionR0Field.setText("0.0"); incubationDaysField.setText("0.0"); mortalityRateField.setText("0.0");
            } else if (name.contains("Antlion") || name.contains("Fourmilion")) {
                accessoryNameField.setText("Antlion Pitfall (Myrmeleon / Predator)");
                categoryCombo.setValue("PREDATOR");
                biomeCombo.setValue("MEDITERRANEAN");
                latitudeField.setText("38.0");
                minTempField.setText("12.0"); optTempField.setText("28.0"); maxTempField.setText("42.0");
                growthRateField.setText("0.5");
                initialBiomassDensityField.setText("20.0"); initialPopulationDensityField.setText("5.0");
                diapauseCheck.setSelected(true);
                seasonSlider1.setValue(0.5); seasonSlider2.setValue(1.0); seasonSlider3.setValue(0.8); seasonSlider4.setValue(0.1);
                targetCasteCombo.setValue("Workers");
                huntModeCombo.setValue("Trap / Funnel (Antlion)");
                killRateField.setText("5.0");
                transmissionR0Field.setText("0.0"); incubationDaysField.setText("0.0"); mortalityRateField.setText("0.0");
            } else if (name.contains("Cordyceps") || name.contains("Entomopathogenic") || name.contains("Entomopathogène")) {
                accessoryNameField.setText("Entomopathogenic Fungus (Cordyceps)");
                categoryCombo.setValue("PATHOGEN_PARASITE");
                biomeCombo.setValue("TROPICAL_RAINFOREST");
                latitudeField.setText("3.0");
                minTempField.setText("15.0"); optTempField.setText("26.0"); maxTempField.setText("34.0");
                growthRateField.setText("4.0");
                initialBiomassDensityField.setText("10.0"); initialPopulationDensityField.setText("30.0");
                diapauseCheck.setSelected(false);
                seasonSlider1.setValue(1.0); seasonSlider2.setValue(0.8); seasonSlider3.setValue(1.0); seasonSlider4.setValue(0.8);
                targetCasteCombo.setValue("Workers");
                huntModeCombo.setValue("Parasitoid (Internal Egg-laying / Wasp)");
                killRateField.setText("2.0");
                pathogenVectorCombo.setValue("Airborne Spores (Cordyceps)");
                transmissionR0Field.setText("3.8"); incubationDaysField.setText("3.0"); mortalityRateField.setText("25.0");
            } else if (name.contains("Varroa") || name.contains("Mite") || name.contains("Acarien")) {
                accessoryNameField.setText("Parasitic Mite (Varroa destructor)");
                categoryCombo.setValue("PATHOGEN_PARASITE");
                biomeCombo.setValue("TEMPERATE_DECIDUOUS");
                latitudeField.setText("45.0");
                minTempField.setText("12.0"); optTempField.setText("24.0"); maxTempField.setText("36.0");
                growthRateField.setText("2.8");
                initialBiomassDensityField.setText("5.0"); initialPopulationDensityField.setText("80.0");
                diapauseCheck.setSelected(false);
                seasonSlider1.setValue(0.7); seasonSlider2.setValue(1.0); seasonSlider3.setValue(0.6); seasonSlider4.setValue(0.2);
                targetCasteCombo.setValue("Brood / Pupae");
                huntModeCombo.setValue("Parasitoid (Internal Egg-laying / Wasp)");
                killRateField.setText("1.0");
                pathogenVectorCombo.setValue("Soil & Gallery Contact");
                transmissionR0Field.setText("2.8"); incubationDaysField.setText("2.0"); mortalityRateField.setText("15.0");
            } else if (name.contains("Moss") || name.contains("Mousse")) {
                accessoryNameField.setText("Humid Moss (Polytrichum / Substrate)");
                categoryCombo.setValue("FLORA");
                biomeCombo.setValue("TAIGA_BOREAL");
                latitudeField.setText("60.0");
                minTempField.setText("2.0"); optTempField.setText("18.0"); maxTempField.setText("28.0");
                growthRateField.setText("0.8");
                initialBiomassDensityField.setText("300.0"); initialPopulationDensityField.setText("10.0");
                diapauseCheck.setSelected(true);
                seasonSlider1.setValue(0.9); seasonSlider2.setValue(0.7); seasonSlider3.setValue(0.4); seasonSlider4.setValue(0.1);
                targetCasteCombo.setValue("All Castes");
                huntModeCombo.setValue("Direct Attack (Bird / Anteater)");
                killRateField.setText("0.0");
            } else if (name.contains("Nectaries") || name.contains("Nectaires")) {
                accessoryNameField.setText("Nectar Flowers & Nectaries (Acacia EFN)");
                categoryCombo.setValue("FLORA");
                biomeCombo.setValue("MEDITERRANEAN");
                latitudeField.setText("35.0");
                minTempField.setText("10.0"); optTempField.setText("25.0"); maxTempField.setText("38.0");
                growthRateField.setText("1.8"); initialBiomassDensityField.setText("180.0"); initialPopulationDensityField.setText("40.0");
                diapauseCheck.setSelected(false);
            } else if (name.contains("Mealybugs") || name.contains("Cochenilles")) {
                accessoryNameField.setText("Root Mealybugs (Eurhizococcus)");
                categoryCombo.setValue("APHID_MUTUALIST");
                biomeCombo.setValue("TEMPERATE_DECIDUOUS");
                latitudeField.setText("44.0");
                minTempField.setText("6.0"); optTempField.setText("21.0"); maxTempField.setText("32.0");
                growthRateField.setText("1.5"); initialBiomassDensityField.setText("60.0"); initialPopulationDensityField.setText("120.0");
            } else if (name.contains("Termite Prey") || name.contains("Termites Proies")) {
                accessoryNameField.setText("Termite Prey (Microtermes)");
                categoryCombo.setValue("PREY_INSECT");
                biomeCombo.setValue("TROPICAL_RAINFOREST");
                latitudeField.setText("5.0");
                minTempField.setText("18.0"); optTempField.setText("28.0"); maxTempField.setText("36.0");
                growthRateField.setText("3.5"); initialBiomassDensityField.setText("250.0"); initialPopulationDensityField.setText("300.0");
            } else if (name.contains("Spider") || name.contains("Araignée")) {
                accessoryNameField.setText("Jumping Spider (Salticidae)");
                categoryCombo.setValue("PREDATOR");
                biomeCombo.setValue("TEMPERATE_DECIDUOUS");
                latitudeField.setText("46.0");
                minTempField.setText("8.0"); optTempField.setText("23.0"); maxTempField.setText("35.0");
                growthRateField.setText("0.6"); initialBiomassDensityField.setText("15.0"); initialPopulationDensityField.setText("8.0");
            } else if (name.contains("Hornet") || name.contains("Frelon")) {
                accessoryNameField.setText("Asian Hornet (Bee Predator)");
                categoryCombo.setValue("PREDATOR");
                biomeCombo.setValue("TEMPERATE_DECIDUOUS");
                latitudeField.setText("44.5");
                minTempField.setText("10.0"); optTempField.setText("25.0"); maxTempField.setText("36.0");
                growthRateField.setText("1.4"); initialBiomassDensityField.setText("25.0"); initialPopulationDensityField.setText("12.0");
                targetCasteCombo.setValue("Workers"); huntModeCombo.setValue("Direct Attack (Bird / Anteater)"); killRateField.setText("8.0");
            } else if (name.contains("Philanthus") || name.contains("Philanthe")) {
                accessoryNameField.setText("European Bee-eater Wasp (Philanthus)");
                categoryCombo.setValue("PREDATOR");
                biomeCombo.setValue("MEDITERRANEAN");
                latitudeField.setText("42.0");
                minTempField.setText("12.0"); optTempField.setText("26.0"); maxTempField.setText("38.0");
                growthRateField.setText("1.0"); initialBiomassDensityField.setText("10.0"); initialPopulationDensityField.setText("6.0");
                targetCasteCombo.setValue("Workers"); huntModeCombo.setValue("Ambush / Stalking (Spider)"); killRateField.setText("6.0");
            } else if (name.contains("Buzzard") || name.contains("Bondrée")) {
                accessoryNameField.setText("European Honey Buzzard (Raptor Wasp Hunter)");
                categoryCombo.setValue("PREDATOR");
                biomeCombo.setValue("TEMPERATE_DECIDUOUS");
                latitudeField.setText("49.0");
                minTempField.setText("8.0"); optTempField.setText("22.0"); maxTempField.setText("32.0");
                growthRateField.setText("0.2"); initialBiomassDensityField.setText("2.0"); initialPopulationDensityField.setText("2.0");
                targetCasteCombo.setValue("All Castes"); huntModeCombo.setValue("Direct Attack (Bird / Anteater)"); killRateField.setText("35.0");
            } else if (name.contains("Megaponera")) {
                accessoryNameField.setText("Megaponera Termite Raider (Termite Raid)");
                categoryCombo.setValue("PREDATOR");
                biomeCombo.setValue("TROPICAL_RAINFOREST");
                latitudeField.setText("-1.0");
                minTempField.setText("20.0"); optTempField.setText("29.0"); maxTempField.setText("38.0");
                growthRateField.setText("2.5"); initialBiomassDensityField.setText("80.0"); initialPopulationDensityField.setText("150.0");
                targetCasteCombo.setValue("Workers"); huntModeCombo.setValue("Direct Attack (Bird / Anteater)"); killRateField.setText("12.0");
            } else if (name.contains("Woodpecker") || name.contains("Pic Noir")) {
                accessoryNameField.setText("Black Woodpecker (Bark Beetle Predator)");
                categoryCombo.setValue("PREDATOR");
                biomeCombo.setValue("TAIGA_BOREAL");
                latitudeField.setText("58.0");
                minTempField.setText("-5.0"); optTempField.setText("18.0"); maxTempField.setText("30.0");
                growthRateField.setText("0.3"); initialBiomassDensityField.setText("4.0"); initialPopulationDensityField.setText("3.0");
                targetCasteCombo.setValue("Workers"); huntModeCombo.setValue("Direct Attack (Bird / Anteater)"); killRateField.setText("20.0");
            } else if (name.contains("Tamandua")) {
                accessoryNameField.setText("Tamandua Anteater (Direct Nest Raid)");
                categoryCombo.setValue("PREDATOR");
                biomeCombo.setValue("TROPICAL_RAINFOREST");
                latitudeField.setText("-2.0");
                minTempField.setText("20.0"); optTempField.setText("30.0"); maxTempField.setText("40.0");
                growthRateField.setText("0.1"); initialBiomassDensityField.setText("1.0"); initialPopulationDensityField.setText("1.0");
                killRateField.setText("25.0");
            } else if (name.contains("Parasitoid") || name.contains("Guêpe Parasitoïde")) {
                accessoryNameField.setText("Parasitoid Wasp (Eucharitidae)");
                categoryCombo.setValue("PATHOGEN_PARASITE");
                biomeCombo.setValue("TROPICAL_RAINFOREST");
                latitudeField.setText("8.0");
                minTempField.setText("16.0"); optTempField.setText("27.0"); maxTempField.setText("35.0");
                growthRateField.setText("2.0"); initialBiomassDensityField.setText("5.0"); initialPopulationDensityField.setText("40.0");
            } else if (name.contains("Microsporidian") || name.contains("Microsporidie") || name.contains("Nosema")) {
                accessoryNameField.setText("Intestinal Microsporidian (Nosema bombi)");
                categoryCombo.setValue("PATHOGEN_PARASITE");
                biomeCombo.setValue("TEMPERATE_DECIDUOUS");
                latitudeField.setText("50.0");
                minTempField.setText("4.0"); optTempField.setText("18.0"); maxTempField.setText("30.0");
                growthRateField.setText("3.2"); initialBiomassDensityField.setText("2.0"); initialPopulationDensityField.setText("150.0");
            } else if (name.contains("Leucoagaricus") || name.contains("Symbiotic Atta")) {
                accessoryNameField.setText("Atta Symbiotic Fungus (Leucoagaricus)");
                categoryCombo.setValue("FUNGI");
                biomeCombo.setValue("TROPICAL_RAINFOREST");
                latitudeField.setText("0.0");
                minTempField.setText("18.0"); optTempField.setText("26.0"); maxTempField.setText("32.0");
                growthRateField.setText("4.5"); initialBiomassDensityField.setText("400.0"); initialPopulationDensityField.setText("1.0");
            } else if (name.contains("Termitomyces")) {
                accessoryNameField.setText("Termite Cultivated Fungus (Termitomyces)");
                categoryCombo.setValue("FUNGI");
                biomeCombo.setValue("TROPICAL_RAINFOREST");
                latitudeField.setText("-5.0");
                minTempField.setText("19.0"); optTempField.setText("27.0"); maxTempField.setText("33.0");
                growthRateField.setText("4.0"); initialBiomassDensityField.setText("350.0"); initialPopulationDensityField.setText("1.0");
            } else if (name.contains("Springtails") || name.contains("Collemboles")) {
                accessoryNameField.setText("Garbage Springtails (Detritivore Cleaner)");
                categoryCombo.setValue("DETRITIVORE");
                biomeCombo.setValue("TEMPERATE_DECIDUOUS");
                latitudeField.setText("48.0");
                minTempField.setText("5.0"); optTempField.setText("19.0"); maxTempField.setText("28.0");
                growthRateField.setText("2.2"); initialBiomassDensityField.setText("50.0"); initialPopulationDensityField.setText("200.0");
            } else if (name.contains("Myrmecophilous") || name.contains("Staphylin") || name.contains("Lomechusa")) {
                accessoryNameField.setText("Myrmecophilous Beetle (Lomechusa Commensal)");
                categoryCombo.setValue("DETRITIVORE");
                biomeCombo.setValue("TEMPERATE_DECIDUOUS");
                latitudeField.setText("47.0");
                minTempField.setText("7.0"); optTempField.setText("20.0"); maxTempField.setText("30.0");
                growthRateField.setText("1.1"); initialBiomassDensityField.setText("15.0"); initialPopulationDensityField.setText("30.0");
            }
        } finally {
            isUpdatingFields = false;
        }
    }
}
