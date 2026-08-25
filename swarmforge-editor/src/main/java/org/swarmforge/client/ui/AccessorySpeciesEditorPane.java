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
    private Tab tabHelp;

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

    // Integrated Documentation Help
    private TextField helpSearchField;
    private VBox helpEntriesBox;
    private final List<HBox> helpEntriesList = new ArrayList<>();

    public AccessorySpeciesEditorPane() {
        setSpacing(10);

        // Main TabPane
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        tabTaxonomy = new Tab(i18n.get("accessory.tab.taxonomy"), new ScrollPane(createTaxonomyCard()));
        tabSeasonal = new Tab(i18n.get("accessory.tab.seasonal"), new ScrollPane(createSeasonalCard()));
        tabPredators = new Tab(i18n.get("accessory.tab.predators"), new ScrollPane(createPredatorPathogenCard()));

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
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

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
        lblPreset.setStyle("-fx-font-weight: bold;");
        lblPreset.setGraphic(new FontIcon(Feather.SLIDERS));

        accessoryPresetCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Graminées à Graines (Herbes & Biomasse)",
                "Fleurs Nectarifères & Nectaires (Acacia EFN)",
                "Mousse Humide (Polytrichum / Substrat)",
                "Pucerons du Pin (Cinara pini / Miellat)",
                "Cochenilles des Racines (Eurhizococcus / Souterrain)",
                "Larves de Ténébrion (Proies Protéiques)",
                "Termites Proies (Microtermes / Nourriture)",
                "Fourmilion Piégeur (Myrmeleon / Entonnoir)",
                "Araignée Sauteuse (Salticidae / Affût)",
                "Tamandua / Tamanoir (Attaque Nid Directe)",
                "Guêpe Parasitoïde (Eucharitidae / Ponte)",
                "Champignon Entomopathogène (Cordyceps Zombie)",
                "Acarien Parasite (Varroa destructor)",
                "Microsporidie Intestinale (Nosema bombi)",
                "Champignon Symbiotique Atta (Leucoagaricus)",
                "Champignon des Termites (Termitomyces)",
                "Collemboles Détritivores (Nettoyage Dépotoir)",
                "Staphylin Myrmécophile (Lomechusa Commensal)"
        ));
        accessoryPresetCombo.setEditable(true);
        accessoryPresetCombo.promptTextProperty().bind(i18n.createStringBinding("preset.prompt"));
        accessoryPresetCombo.setTooltip(new Tooltip("Sélectionnez une espèce accessoire pré-configurée (Plantes, Pucerons, Proies, Prédateurs, Pathogènes, Champignons, Détritivores)."));
        accessoryPresetCombo.getSelectionModel().selectFirst();
        accessoryPresetCombo.setPrefWidth(300);

        accessoryPresetCombo.setOnAction(e -> {
            if (isUpdatingFields) return;
            String sel = accessoryPresetCombo.getValue();
            if (sel == null || sel.equals(lastSelectedPreset)) return;

            if (isDirty) {
                Alert alert = org.swarmforge.client.util.ThemeManager.createAlert(
                    Alert.AlertType.CONFIRMATION,
                    "Attention : Vous avez des modifications non enregistrées sur l'espèce accessoire actuelle.\n\nVoulez-vous vraiment charger le preset '" + sel + "' et abandonner vos modifications ?"
                );
                alert.setTitle("Modifications non enregistrées");
                alert.setHeaderText("Changement de preset d'espèce accessoire");
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
        btnSave.setTooltip(new Tooltip("Enregistrer la configuration de l'espèce accessoire."));
        btnSave.setOnAction(e -> handleAddPreset());

        btnDelete = new Button();
        btnDelete.setGraphic(new FontIcon(Feather.TRASH_2));
        btnDelete.textProperty().bind(i18n.createStringBinding("preset.delete"));
        btnDelete.getStyleClass().add("btn-danger");
        btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold;");
        btnDelete.setTooltip(new Tooltip("Supprimer l'espèce accessoire sélectionnée."));
        btnDelete.setOnAction(e -> handleDeletePreset());

        btnExport = new Button();
        btnExport.setGraphic(new FontIcon(Feather.DOWNLOAD));
        btnExport.textProperty().bind(i18n.createStringBinding("preset.export"));
        btnExport.getStyleClass().add("btn-secondary");
        btnExport.setTooltip(new Tooltip("Exporter l'espèce accessoire au format JSON."));
        btnExport.setOnAction(e -> handleSave());

        btnImport = new Button();
        btnImport.setGraphic(new FontIcon(Feather.UPLOAD));
        btnImport.textProperty().bind(i18n.createStringBinding("preset.import"));
        btnImport.getStyleClass().add("btn-secondary");
        btnImport.setTooltip(new Tooltip("Importer un fichier JSON d'espèce accessoire."));
        btnImport.setOnAction(e -> handleLoad());

        bar.getChildren().addAll(lblPreset, accessoryPresetCombo, btnSave, btnDelete, new Separator(Orientation.VERTICAL), btnExport, btnImport);
        return bar;
    }

    private VBox createTaxonomyCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("card-pane");

        Label title = new Label(i18n.get("accessory.card.taxonomy.title"));
        title.getStyleClass().add("card-title");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: -fx-accent;");

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

        accessoryNameField = new TextField("Graminées à Graines (Messor)");
        categoryCombo = new ComboBox<>(FXCollections.observableArrayList(
                "FLORA",
                "APHID_MUTUALIST",
                "PREY_INSECT",
                "PREDATOR",
                "PATHOGEN_PARASITE",
                "FUNGI",
                "DETRITIVORE"
        ));
        ComboBoxTooltipHelper.setupDescriptiveComboBox(categoryCombo,
            val -> switch (val) {
                case "FLORA" -> "🌿 FLORA (Plantes & Graines)";
                case "APHID_MUTUALIST" -> "🐄 APHID_MUTUALIST (Pucerons & Miellat)";
                case "PREY_INSECT" -> "🐛 PREY_INSECT (Insectes Proies)";
                case "PREDATOR" -> "🕷️ PREDATOR (Araignées, Fourmilions, Oiseaux)";
                case "PATHOGEN_PARASITE" -> "🦠 PATHOGEN_PARASITE (Cordyceps, Acariens)";
                case "FUNGI" -> "🍄 FUNGI (Champignons Symbiotiques)";
                case "DETRITIVORE" -> "🍂 DETRITIVORE (Collemboles & Cloportes)";
                default -> val;
            },
            val -> switch (val) {
                case "FLORA" -> "Végétation, plantes nectarifères et graminées fournissant des graines et du nectar à la colonie.";
                case "APHID_MUTUALIST" -> "Pucerons et cochenilles exploités en trophobiose pour la récolte de miellat sucré.";
                case "PREY_INSECT" -> "Proies Arthropodes (chenilles, grillons, mouches) chassées pour l'apport en protéines.";
                case "PREDATOR" -> "Prédateurs naturels régulant la population de la colonie (araignées, fourmilions, reptiles).";
                case "PATHOGEN_PARASITE" -> "Parasites et champignons entomopathogènes provoquant des épidémies et altérant la santé de la colonie.";
                case "FUNGI" -> "Basidiomycètes ou ascomycètes cultivés par les insectes attines ou termites champignonnistes.";
                case "DETRITIVORE" -> "Organismes détritivores nettoyant les dépotoirs de la colonie et recyclant la matière organique.";
                default -> "";
            }
        );
        categoryCombo.getSelectionModel().selectFirst();

        biomeCombo = new ComboBox<>(FXCollections.observableArrayList(
                "TEMPERATE_DECIDUOUS",
                "MEDITERRANEAN",
                "TROPICAL_RAINFOREST",
                "ARID_DESERT",
                "TAIGA_BOREAL"
        ));
        ComboBoxTooltipHelper.setupDescriptiveComboBox(biomeCombo,
            val -> switch (val) {
                case "TEMPERATE_DECIDUOUS" -> "🌳 Forêt Tempérée Décidue (4 Saisons)";
                case "MEDITERRANEAN" -> "🌿 Maquis Méditerranéen (Été Sec / Hiver Doux)";
                case "TROPICAL_RAINFOREST" -> "🌴 Forêt Tropicale Humide";
                case "ARID_DESERT" -> "🏜️ Désert Aride (Pluies Épisodiques)";
                case "TAIGA_BOREAL" -> "🌲 Taïga Boréale (Saison Végétative Courte)";
                default -> val;
            },
            val -> switch (val) {
                case "TEMPERATE_DECIDUOUS" -> "Climat tempéré avec 4 saisons bien marquées, diapause hivernale et floraison printanière.";
                case "MEDITERRANEAN" -> "Étés chauds et arides, hivers doux et pluvieux favorisant les espèces granivores et thermophiles.";
                case "TROPICAL_RAINFOREST" -> "Température et hygrométrie élevées constantes avec biodiversité et compétition intenses.";
                case "ARID_DESERT" -> "Conditions extrêmes de chaleur et de sécheresse avec activité nocturne ou crépusculaire.";
                case "TAIGA_BOREAL" -> "Hivers longs et glacials, saison végétative très courte nécessitant une forte accumulation de réserves.";
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
        individualCountSpinner.setTooltip(new Tooltip("Nombre total d'individus de cette espèce accessoire introduits initialement."));

        nestDispatchCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Tous les nids hôtes compatibles (Filtrage biologique)",
                "Nids de l'Espèce Principale uniquement (Nid #1)",
                "Répartition uniforme sur tous les nids du monde",
                "Extérieur uniquement (Zone d'exploration hors nids)",
                "Loges d'élevage / Couvain uniquement (Commensaux & Parasites)"
        ));
        nestDispatchCombo.getSelectionModel().selectFirst();
        nestDispatchCombo.setTooltip(new Tooltip("Règle de répartition des individus entre les nids : affecte les organismes uniquement dans les nids qui les acceptent et exclut ceux qui les réfutent."));

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
        grid.addRow(10, new Label("Nombre d'Individus à Introduire :"), individualCountSpinner);
        grid.addRow(11, new Label("Règle de Répartition dans les Nids :"), nestDispatchCombo);
        grid.addRow(12, createLabelKey("accessory.field.diapause", "accessory.field.diapause.tt"), diapauseCheck);

        card.getChildren().addAll(title, grid);
        return card;
    }

    private VBox createSeasonalCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("card-pane");

        Label title = new Label(i18n.get("accessory.card.seasonal.title"));
        title.getStyleClass().add("card-title");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: -fx-accent;");

        hemisphereCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Hémisphère Nord (Printemps = Mars-Mai, Hiver = Déc-Fév)",
                "Hémisphère Sud (Printemps = Sept-Nov, Hiver = Juin-Août)",
                "Zone Équatoriale / Intertropicale (Saisons des Pluies & Sèches)"
        ));
        hemisphereCombo.getSelectionModel().selectFirst();
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
        seasonHintLabel.setStyle("-fx-font-size: 11px; -fx-wrap-text: true;");

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
        title.getStyleClass().add("card-title");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: -fx-accent;");

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

        targetCasteCombo = new ComboBox<>(FXCollections.observableArrayList("Ouvrières", "Nymphes / Couvain", "Sexués / Reines", "Toutes Castes"));
        targetCasteCombo.getSelectionModel().selectFirst();

        huntModeCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Piège / Entonnoir (Fourmilion)",
                "Embrouille / Affût (Araignée)",
                "Attaque Directe (Oiseau / Tamandua)",
                "Parasitoïde (Ponte interne / Guêpe)"
        ));
        huntModeCombo.getSelectionModel().selectFirst();

        killRateField = new TextField("3.5");

        pathogenVectorCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Spores Aériennes (Cordyceps)",
                "Contact Sol & Galerie",
                "Toilette / Allogrooming",
                "Nourriture Contaminée"
        ));
        pathogenVectorCombo.getSelectionModel().selectFirst();

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

    private VBox createHelpTabContent() {
        VBox mainBox = new VBox(12);
        mainBox.setPadding(new Insets(15));

        Label helpTitle = new Label(i18n.get("accessory.help.title"));
        helpTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: -fx-accent;");

        helpSearchField = new TextField();
        helpSearchField.setPromptText(i18n.get("accessory.help.search_prompt"));
        helpSearchField.setStyle("-fx-font-size: 13px;");
        helpSearchField.textProperty().addListener((obs, oldText, newText) -> filterHelpEntries(newText));

        helpEntriesBox = new VBox(10);
        helpEntriesBox.setPadding(new Insets(10, 0, 10, 0));

        populateHelpEntries();

        ScrollPane sc = new ScrollPane(helpEntriesBox);
        sc.setFitToWidth(true);
        VBox.setVgrow(sc, Priority.ALWAYS);

        mainBox.getChildren().addAll(helpTitle, helpSearchField, sc);
        return mainBox;
    }

    private void populateHelpEntries() {
        helpEntriesList.clear();
        helpEntriesBox.getChildren().clear();

        // Entry 1: Flora & Plant Biomass
        addHelpEntry(
                "FLORA & Biomasse Végétale (g/m²)",
                "Les plantes et graines constituent la ressource primaire trophique. La biomasse surfacique initiale (g/m²) et le taux de croissance (g/m²/j) déterminent la quantité de graines et de tissus végétaux disponibles pour le forage des ouvrières (ex: Messor, Atta)."
        );

        // Entry 2: Aphid Mutualists
        addHelpEntry(
                "APHID_MUTUALIST & Élevage de Pucerons",
                "Les pucerons synthétisent le miellat riche en glucides. Les ouvrières protègent les colonies de pucerons contre les coccinelles et en récoltent le liquide sucré par stimulation antennaire (trophobiose)."
        );

        // Entry 3: Prey Insects
        addHelpEntry(
                "PREY_INSECT & Apport Protéique",
                "Les chenilles, larves de ténébrions et grillons fournissent l'azote et les acides aminés essentiels au développement des larves d'insectes eusociaux et à l'ovogenèse des reines."
        );

        // Entry 4: Predators & Hunt Modes
        addHelpEntry(
                "PREDATOR & Modes de Chasse (Fourmilion, Araignée, Oiseaux)",
                "Les prédateurs ciblent des castes spécifiques (ex: ouvrières fourrageuses) via des pièges (entonnoirs de sable du fourmilion), des toiles d'araignées ou des attaques directes, régulant la densité de la colonie."
        );

        // Entry 5: Pathogens & Parasites
        addHelpEntry(
                "PATHOGEN_PARASITE & Épidémiologie (Cordyceps, Varroa, R0)",
                "Les champignons entomopathogènes (Cordyceps) et acariens parasites se propagent par spores aériennes ou allogrooming. Le taux R0 et l'incubation (jours) modélisent les épizooties au sein du nid."
        );

        // Entry 6: Latitude & Photoperiod
        addHelpEntry(
                "Latitude (°N/°S) & Photopériode Solaires",
                "La latitude définit la déclinaison solaire et la durée du jour tout au long de l'année. Elle synchronise le comportement d'amassage, la ponte et la préparation à l'hivernation."
        );

        // Entry 7: Thermal Tolerance Ranges
        addHelpEntry(
                "Tolérances Thermiques (Min, Opt, Max °C)",
                "La croissance et la survie des espèces accessoires dépendent de la température ambiante. Sous la température minimale ou au-delà de la maximale, l'activité est stoppée ou la mortalité s'accroît."
        );

        // Entry 8: Diapause & Overwintering
        addHelpEntry(
                "Hivernation / Diapause Automatique",
                "Mécanisme de léthargie physiologique déclenché lorsque la température descend sous 10°C ou lors du raccourcissement automnal des jours, réduisant le métabolisme et la consommation."
        );
    }

    private void addHelpEntry(String title, String description) {
        Label tLabel = new Label("• " + title + " : ");
        tLabel.getStyleClass().add("help-entry-title");
        tLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: -fx-accent; -fx-min-width: 220px;");
        tLabel.setWrapText(true);

        Label dLabel = new Label(description);
        dLabel.getStyleClass().add("help-entry-desc");
        dLabel.setWrapText(true);

        HBox row = new HBox(6, tLabel, dLabel);
        row.getStyleClass().add("help-entry-row");
        row.setPadding(new Insets(8));
        HBox.setHgrow(dLabel, Priority.ALWAYS);

        helpEntriesList.add(row);
        helpEntriesBox.getChildren().add(row);
    }

    private void filterHelpEntries(String query) {
        helpEntriesBox.getChildren().clear();
        if (query == null || query.trim().isEmpty()) {
            helpEntriesBox.getChildren().addAll(helpEntriesList);
            return;
        }

        String lowerQ = query.toLowerCase().trim();
        for (HBox row : helpEntriesList) {
            Label tLabel = (Label) row.getChildren().get(0);
            Label dLabel = (Label) row.getChildren().get(1);
            if (tLabel.getText().toLowerCase().contains(lowerQ) || dLabel.getText().toLowerCase().contains(lowerQ)) {
                helpEntriesBox.getChildren().add(row);
            }
        }
    }

    private Slider createSlider(double initialVal) {
        Slider s = new Slider(0.0, 1.0, initialVal);
        s.setShowTickLabels(true);
        s.setShowTickMarks(true);
        s.setMajorTickUnit(0.25);
        Tooltip t = new Tooltip("Coefficient multiplicateur d'activité biologique (0.0 = inactif, 1.0 = activité maximale)");
        t.setShowDelay(Duration.millis(100));
        Tooltip.install(s, t);
        return s;
    }

    private Label createLabelKey(String keyText, String keyTooltip) {
        Label l = new Label(i18n.get(keyText));
        l.setStyle("-fx-font-weight: bold;");
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
        return l;
    }

    private void handleAddPreset() {
        String editedText = accessoryPresetCombo.getEditor() != null ? accessoryPresetCombo.getEditor().getText().trim() : "";
        String fieldText = accessoryNameField != null ? accessoryNameField.getText().trim() : "";
        String name = !editedText.isEmpty() ? editedText : (!fieldText.isEmpty() ? fieldText : "swarmforge-accessory-custom");

        if (accessoryPresetCombo.getItems().contains(name)) {
            Alert confirmAlert = org.swarmforge.client.util.ThemeManager.createAlert(
                Alert.AlertType.CONFIRMATION,
                "Le preset d'espèce accessoire '" + name + "' existe déjà.\n\nVoulez-vous le remplacer par la configuration actuelle ?"
            );
            confirmAlert.setTitle("Remplacer le Preset Existant");
            confirmAlert.setHeaderText("Confirmation de remplacement");
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
        NotificationOverlay.show(this, "Preset espèce accessoire enregistré : " + name, NotificationOverlay.NotificationType.SUCCESS);
    }

    private void handleLoad() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Charger une espèce accessoire");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File f = chooser.showOpenDialog(getScene().getWindow());
        if (f != null) {
            NotificationOverlay.show(this, "Espèce accessoire chargée depuis " + f.getName(), NotificationOverlay.NotificationType.INFO);
        }
    }

    private void handleSave() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Sauvegarder l'espèce accessoire");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        chooser.setInitialFileName("swarmforge-accessory-" + (accessoryNameField != null ? accessoryNameField.getText().toLowerCase().replaceAll("[^a-z0-9]+", "-") : "custom") + ".json");

        File f = chooser.showSaveDialog(getScene().getWindow());
        if (f != null) {
            NotificationOverlay.show(this, "Espèce accessoire sauvegardée sous " + f.getName(), NotificationOverlay.NotificationType.SUCCESS);
        }
    }

    private void handleDeletePreset() {
        Alert confirmAlert = org.swarmforge.client.util.ThemeManager.createAlert(
            Alert.AlertType.CONFIRMATION,
            String.format(i18n.get("preset.delete.confirm"), accessoryPresetCombo.getValue())
        );
        confirmAlert.setTitle(i18n.get("preset.delete.title"));
        confirmAlert.setHeaderText("Supprimer l'Espèce Accessoire");

        confirmAlert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String selected = accessoryPresetCombo.getValue();
                if (selected != null) {
                    accessoryPresetCombo.getItems().remove(selected);
                    if (!accessoryPresetCombo.getItems().isEmpty()) {
                        accessoryPresetCombo.getSelectionModel().selectFirst();
                    }
                }
                NotificationOverlay.show(this, "Preset espèce accessoire supprimé.", NotificationOverlay.NotificationType.INFO);
            }
        });
    }

    private void refreshI18nLabels() {
        headerLabel.setText(i18n.get("accessory.title"));

        tabTaxonomy.setText(i18n.get("accessory.tab.taxonomy"));
        tabSeasonal.setText(i18n.get("accessory.tab.seasonal"));
        tabPredators.setText(i18n.get("accessory.tab.predators"));
        tabHelp.setText(i18n.get("accessory.tab.help"));

        updateSeasonLabels();
        if (seasonHintLabel != null) {
            seasonHintLabel.setText(i18n.get("accessory.season.hint"));
        }
        if (helpSearchField != null) {
            helpSearchField.setPromptText(i18n.get("accessory.help.search_prompt"));
        }
    }

    private boolean isUpdatingFields = false;
    private boolean isDirty = false;
    private String lastSelectedPreset = null;

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
            if (name.contains("Graminées")) {
                accessoryNameField.setText("Graminées à Graines (Messor)");
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
                targetCasteCombo.setValue("Toutes Castes");
                huntModeCombo.setValue("Attaque Directe (Oiseau / Tamandua)");
                killRateField.setText("0.0");
                pathogenVectorCombo.setValue("Spores Aériennes (Cordyceps)");
                transmissionR0Field.setText("0.0"); incubationDaysField.setText("0.0"); mortalityRateField.setText("0.0");
            } else if (name.contains("Pucerons")) {
                accessoryNameField.setText("Pucerons du Pin (Cinara / Miellat)");
                categoryCombo.setValue("APHID_MUTUALIST");
                biomeCombo.setValue("TEMPERATE_DECIDUOUS");
                latitudeField.setText("48.5");
                minTempField.setText("8.0"); optTempField.setText("20.0"); maxTempField.setText("30.0");
                growthRateField.setText("2.5");
                initialBiomassDensityField.setText("80.0"); initialPopulationDensityField.setText("100.0");
                diapauseCheck.setSelected(true);
                hemisphereCombo.getSelectionModel().select(0);
                seasonSlider1.setValue(0.7); seasonSlider2.setValue(1.0); seasonSlider3.setValue(0.5); seasonSlider4.setValue(0.0);
                targetCasteCombo.setValue("Ouvrières");
                huntModeCombo.setValue("Embrouille / Affût (Araignée)");
                killRateField.setText("0.2");
                pathogenVectorCombo.setValue("Toilette / Allogrooming");
                transmissionR0Field.setText("1.2"); incubationDaysField.setText("5.0"); mortalityRateField.setText("5.0");
            } else if (name.contains("Ténébrion")) {
                accessoryNameField.setText("Larves de Ténébrion (Proies Protéiques)");
                categoryCombo.setValue("PREY_INSECT");
                biomeCombo.setValue("TEMPERATE_DECIDUOUS");
                latitudeField.setText("43.0");
                minTempField.setText("10.0"); optTempField.setText("25.0"); maxTempField.setText("38.0");
                growthRateField.setText("3.0");
                initialBiomassDensityField.setText("200.0"); initialPopulationDensityField.setText("50.0");
                diapauseCheck.setSelected(false);
                seasonSlider1.setValue(0.6); seasonSlider2.setValue(0.9); seasonSlider3.setValue(0.7); seasonSlider4.setValue(0.2);
                targetCasteCombo.setValue("Ouvrières");
                huntModeCombo.setValue("Attaque Directe (Oiseau / Tamandua)");
                killRateField.setText("1.5");
                transmissionR0Field.setText("0.0"); incubationDaysField.setText("0.0"); mortalityRateField.setText("0.0");
            } else if (name.contains("Fourmilion")) {
                accessoryNameField.setText("Fourmilion Piégeur (Myrmeleon / Prédateur)");
                categoryCombo.setValue("PREDATOR");
                biomeCombo.setValue("MEDITERRANEAN");
                latitudeField.setText("38.0");
                minTempField.setText("12.0"); optTempField.setText("28.0"); maxTempField.setText("42.0");
                growthRateField.setText("0.5");
                initialBiomassDensityField.setText("20.0"); initialPopulationDensityField.setText("5.0");
                diapauseCheck.setSelected(true);
                seasonSlider1.setValue(0.5); seasonSlider2.setValue(1.0); seasonSlider3.setValue(0.8); seasonSlider4.setValue(0.1);
                targetCasteCombo.setValue("Ouvrières");
                huntModeCombo.setValue("Piège / Entonnoir (Fourmilion)");
                killRateField.setText("5.0");
                transmissionR0Field.setText("0.0"); incubationDaysField.setText("0.0"); mortalityRateField.setText("0.0");
            } else if (name.contains("Cordyceps") || name.contains("Entomopathogène")) {
                accessoryNameField.setText("Champignon Entomopathogène (Cordyceps)");
                categoryCombo.setValue("PATHOGEN_PARASITE");
                biomeCombo.setValue("TROPICAL_RAINFOREST");
                latitudeField.setText("3.0");
                minTempField.setText("15.0"); optTempField.setText("26.0"); maxTempField.setText("34.0");
                growthRateField.setText("4.0");
                initialBiomassDensityField.setText("10.0"); initialPopulationDensityField.setText("30.0");
                diapauseCheck.setSelected(false);
                seasonSlider1.setValue(1.0); seasonSlider2.setValue(0.8); seasonSlider3.setValue(1.0); seasonSlider4.setValue(0.8);
                targetCasteCombo.setValue("Ouvrières");
                huntModeCombo.setValue("Parasitoïde (Ponte interne / Guêpe)");
                killRateField.setText("2.0");
                pathogenVectorCombo.setValue("Spores Aériennes (Cordyceps)");
                transmissionR0Field.setText("3.8"); incubationDaysField.setText("3.0"); mortalityRateField.setText("25.0");
            } else if (name.contains("Varroa") || name.contains("Acarien")) {
                accessoryNameField.setText("Acarien Parasite (Varroa destructor)");
                categoryCombo.setValue("PATHOGEN_PARASITE");
                biomeCombo.setValue("TEMPERATE_DECIDUOUS");
                latitudeField.setText("45.0");
                minTempField.setText("12.0"); optTempField.setText("24.0"); maxTempField.setText("36.0");
                growthRateField.setText("2.8");
                initialBiomassDensityField.setText("5.0"); initialPopulationDensityField.setText("80.0");
                diapauseCheck.setSelected(false);
                seasonSlider1.setValue(0.7); seasonSlider2.setValue(1.0); seasonSlider3.setValue(0.6); seasonSlider4.setValue(0.2);
                targetCasteCombo.setValue("Nymphes / Couvain");
                huntModeCombo.setValue("Parasitoïde (Ponte interne / Guêpe)");
                killRateField.setText("1.0");
                pathogenVectorCombo.setValue("Contact Sol & Galerie");
                transmissionR0Field.setText("2.8"); incubationDaysField.setText("2.0"); mortalityRateField.setText("15.0");
            } else if (name.contains("Mousse")) {
                accessoryNameField.setText("Mousse Humide (Polytrichum / Substrat)");
                categoryCombo.setValue("FLORA");
                biomeCombo.setValue("TAIGA_BOREAL");
                latitudeField.setText("60.0");
                minTempField.setText("2.0"); optTempField.setText("18.0"); maxTempField.setText("28.0");
                growthRateField.setText("0.8");
                initialBiomassDensityField.setText("300.0"); initialPopulationDensityField.setText("10.0");
                diapauseCheck.setSelected(true);
                seasonSlider1.setValue(0.9); seasonSlider2.setValue(0.7); seasonSlider3.setValue(0.4); seasonSlider4.setValue(0.1);
                targetCasteCombo.setValue("Toutes Castes");
                huntModeCombo.setValue("Attaque Directe (Oiseau / Tamandua)");
                killRateField.setText("0.0");
            } else if (name.contains("Nectaires")) {
                accessoryNameField.setText("Fleurs Nectarifères & Nectaires (Acacia EFN)");
                categoryCombo.setValue("FLORA");
                biomeCombo.setValue("MEDITERRANEAN");
                latitudeField.setText("35.0");
                minTempField.setText("10.0"); optTempField.setText("25.0"); maxTempField.setText("38.0");
                growthRateField.setText("1.8"); initialBiomassDensityField.setText("180.0"); initialPopulationDensityField.setText("40.0");
                diapauseCheck.setSelected(false);
            } else if (name.contains("Cochenilles")) {
                accessoryNameField.setText("Cochenilles des Racines (Eurhizococcus)");
                categoryCombo.setValue("APHID_MUTUALIST");
                biomeCombo.setValue("TEMPERATE_DECIDUOUS");
                latitudeField.setText("44.0");
                minTempField.setText("6.0"); optTempField.setText("21.0"); maxTempField.setText("32.0");
                growthRateField.setText("1.5"); initialBiomassDensityField.setText("60.0"); initialPopulationDensityField.setText("120.0");
            } else if (name.contains("Termites Proies")) {
                accessoryNameField.setText("Termites Proies (Microtermes)");
                categoryCombo.setValue("PREY_INSECT");
                biomeCombo.setValue("TROPICAL_RAINFOREST");
                latitudeField.setText("5.0");
                minTempField.setText("18.0"); optTempField.setText("28.0"); maxTempField.setText("36.0");
                growthRateField.setText("3.5"); initialBiomassDensityField.setText("250.0"); initialPopulationDensityField.setText("300.0");
            } else if (name.contains("Araignée")) {
                accessoryNameField.setText("Araignée Sauteuse (Salticidae)");
                categoryCombo.setValue("PREDATOR");
                biomeCombo.setValue("TEMPERATE_DECIDUOUS");
                latitudeField.setText("46.0");
                minTempField.setText("8.0"); optTempField.setText("23.0"); maxTempField.setText("35.0");
                growthRateField.setText("0.6"); initialBiomassDensityField.setText("15.0"); initialPopulationDensityField.setText("8.0");
            } else if (name.contains("Tamandua")) {
                accessoryNameField.setText("Tamandua / Tamanoir (Attaque Nid Directe)");
                categoryCombo.setValue("PREDATOR");
                biomeCombo.setValue("TROPICAL_RAINFOREST");
                latitudeField.setText("-2.0");
                minTempField.setText("20.0"); optTempField.setText("30.0"); maxTempField.setText("40.0");
                growthRateField.setText("0.1"); initialBiomassDensityField.setText("1.0"); initialPopulationDensityField.setText("1.0");
                killRateField.setText("25.0");
            } else if (name.contains("Guêpe Parasitoïde")) {
                accessoryNameField.setText("Guêpe Parasitoïde (Eucharitidae)");
                categoryCombo.setValue("PATHOGEN_PARASITE");
                biomeCombo.setValue("TROPICAL_RAINFOREST");
                latitudeField.setText("8.0");
                minTempField.setText("16.0"); optTempField.setText("27.0"); maxTempField.setText("35.0");
                growthRateField.setText("2.0"); initialBiomassDensityField.setText("5.0"); initialPopulationDensityField.setText("40.0");
            } else if (name.contains("Microsporidie") || name.contains("Nosema")) {
                accessoryNameField.setText("Microsporidie Intestinale (Nosema bombi)");
                categoryCombo.setValue("PATHOGEN_PARASITE");
                biomeCombo.setValue("TEMPERATE_DECIDUOUS");
                latitudeField.setText("50.0");
                minTempField.setText("4.0"); optTempField.setText("18.0"); maxTempField.setText("30.0");
                growthRateField.setText("3.2"); initialBiomassDensityField.setText("2.0"); initialPopulationDensityField.setText("150.0");
            } else if (name.contains("Leucoagaricus") || name.contains("Symbiotique Atta")) {
                accessoryNameField.setText("Champignon Symbiotique Atta (Leucoagaricus)");
                categoryCombo.setValue("FUNGI");
                biomeCombo.setValue("TROPICAL_RAINFOREST");
                latitudeField.setText("0.0");
                minTempField.setText("18.0"); optTempField.setText("26.0"); maxTempField.setText("32.0");
                growthRateField.setText("4.5"); initialBiomassDensityField.setText("400.0"); initialPopulationDensityField.setText("1.0");
            } else if (name.contains("Termitomyces")) {
                accessoryNameField.setText("Champignon des Termites (Termitomyces)");
                categoryCombo.setValue("FUNGI");
                biomeCombo.setValue("TROPICAL_RAINFOREST");
                latitudeField.setText("-5.0");
                minTempField.setText("19.0"); optTempField.setText("27.0"); maxTempField.setText("33.0");
                growthRateField.setText("4.0"); initialBiomassDensityField.setText("350.0"); initialPopulationDensityField.setText("1.0");
            } else if (name.contains("Collemboles") || name.contains("Dépotoir")) {
                accessoryNameField.setText("Collemboles Détritivores (Nettoyage Dépotoir)");
                categoryCombo.setValue("DETRITIVORE");
                biomeCombo.setValue("TEMPERATE_DECIDUOUS");
                latitudeField.setText("48.0");
                minTempField.setText("5.0"); optTempField.setText("19.0"); maxTempField.setText("28.0");
                growthRateField.setText("2.2"); initialBiomassDensityField.setText("50.0"); initialPopulationDensityField.setText("200.0");
            } else if (name.contains("Staphylin") || name.contains("Lomechusa")) {
                accessoryNameField.setText("Staphylin Myrmécophile (Lomechusa Commensal)");
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
