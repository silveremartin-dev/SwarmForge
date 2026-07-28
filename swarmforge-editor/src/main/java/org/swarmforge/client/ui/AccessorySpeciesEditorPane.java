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
        setSpacing(12);
        setPadding(new Insets(15));
        getStyleClass().add("card-pane");

        // Header Title
        headerLabel = new Label(i18n.get("accessory.title"));
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        // Toolbar
        HBox topToolbar = createToolbar();

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

        getChildren().addAll(headerLabel, topToolbar, new Separator(), tabPane);
    }

    private HBox createToolbar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);

        lblPreset = new Label();
        lblPreset.textProperty().bind(i18n.createStringBinding("preset.label"));
        lblPreset.setStyle("-fx-font-weight: bold;");

        accessoryPresetCombo = new ComboBox<>(FXCollections.observableArrayList(
                "swarmforge-accessory-gramineae",
                "swarmforge-accessory-cinara-aphid",
                "swarmforge-accessory-tenebrio-larva",
                "swarmforge-accessory-myrmeleon-antlion",
                "swarmforge-accessory-cordyceps-fungus",
                "swarmforge-accessory-varroa-mite",
                "swarmforge-accessory-polytrichum-moss"
        ));
        accessoryPresetCombo.promptTextProperty().bind(i18n.createStringBinding("preset.prompt"));
        accessoryPresetCombo.getSelectionModel().selectFirst();
        accessoryPresetCombo.setPrefWidth(240);

        btnSave = new Button();
        btnSave.textProperty().bind(i18n.createStringBinding("preset.save"));
        btnSave.getStyleClass().add("btn-secondary");
        btnSave.setOnAction(e -> handleAddPreset());

        btnDelete = new Button();
        btnDelete.textProperty().bind(i18n.createStringBinding("preset.delete"));
        btnDelete.getStyleClass().add("btn-danger");
        btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold;");
        btnDelete.setOnAction(e -> handleDeletePreset());

        btnExport = new Button();
        btnExport.textProperty().bind(i18n.createStringBinding("preset.export"));
        btnExport.getStyleClass().add("btn-secondary");
        btnExport.setOnAction(e -> handleSave());

        btnImport = new Button();
        btnImport.textProperty().bind(i18n.createStringBinding("preset.import"));
        btnImport.getStyleClass().add("btn-secondary");
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
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

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
                "FLORA (Plantes & Graines)",
                "APHID_MUTUALIST (Pucerons & Miellat)",
                "PREY_INSECT (Insectes Proies)",
                "PREDATOR (Prédateurs: Araignées, Fourmilions, Oiseaux)",
                "PATHOGEN_PARASITE (Pathogènes: Cordyceps, Microsporidies, Acariens)",
                "FUNGI (Champignons Symbiotiques)",
                "DETRITIVORE (Collemboles & Cloportes)"
        ));
        categoryCombo.getSelectionModel().selectFirst();

        biomeCombo = new ComboBox<>(FXCollections.observableArrayList(
                "TEMPERATE_DECIDUOUS (4 Saisons Distinctes)",
                "MEDITERRANEAN (Été Sec / Hiver Doux)",
                "TROPICAL_RAINFOREST (Saison Humide / Sèche)",
                "ARID_DESERT (Pluies Épisodiques)",
                "TAIGA_BOREAL (Saison Végétative Courte)"
        ));
        biomeCombo.getSelectionModel().selectFirst();

        latitudeField = new TextField("45.0");
        minTempField = new TextField("5.0");
        optTempField = new TextField("22.0");
        maxTempField = new TextField("35.0");
        growthRateField = new TextField("1.2");
        initialBiomassDensityField = new TextField("150.0");
        initialPopulationDensityField = new TextField("25.0");

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
        grid.addRow(10, createLabelKey("accessory.field.diapause", "accessory.field.diapause.tt"), diapauseCheck);

        card.getChildren().addAll(title, grid);
        return card;
    }

    private VBox createSeasonalCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("card-pane");

        Label title = new Label(i18n.get("accessory.card.seasonal.title"));
        title.getStyleClass().add("card-title");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

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
        seasonHintLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-wrap-text: true;");

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
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

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
        helpTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

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
        tLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0284c7; -fx-min-width: 220px;");
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
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: #e4e4e7;");
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
        String name = accessoryNameField != null ? accessoryNameField.getText().trim() : "";
        if (name.isEmpty()) name = "swarmforge-accessory-custom";
        if (!accessoryPresetCombo.getItems().contains(name)) {
            accessoryPresetCombo.getItems().add(name);
        }
        accessoryPresetCombo.getSelectionModel().select(name);
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
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(i18n.get("preset.delete.title"));
        confirmAlert.setHeaderText("Supprimer l'Espèce Accessoire");
        confirmAlert.setContentText(String.format(i18n.get("preset.delete.confirm"), accessoryPresetCombo.getValue()));

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
}
