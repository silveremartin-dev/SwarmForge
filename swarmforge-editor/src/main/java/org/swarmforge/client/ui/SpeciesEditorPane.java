/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.core.domain.CasteTemplate;
import org.swarmforge.core.species.CustomSpecies;
import org.swarmforge.core.behavior.ReasoningArchitecture.ArchitectureType;

import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.NotificationOverlay;
import org.swarmforge.client.util.ThemeManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Rich Species Editor Pane for defining realistic eusocial species (ants, bees, wasps, termites).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SpeciesEditorPane extends VBox {

    private final I18nManager i18n = I18nManager.getInstance();
    private final SpeciesPresetManager presetManager = new SpeciesPresetManager();

    // UI Fields
    private TabPane mainTabPane;
    private Tab tabGlossary;

    private ComboBox<String> presetCombo;

    // General & Taxonomy
    private TextField commonNameField;
    private TextField scientificNameField;
    private TextField genusField;
    private ComboBox<String> insectTypeCombo;
    private ComboBox<org.swarmforge.core.species.SpeciesCategory> categoryCombo;
    private TextArea descriptionArea;

    // Social Structure & Queens
    private ComboBox<String> queenModeCombo;
    private Spinner<Integer> queenCountSpinner;
    private TextField queenLifespanField;
    private TextField queenEggRateField;
    private CheckBox hasKingCheckBox;
    private TextField kingLifespanField;
    private ComboBox<String> nuptialFlightCombo;

    // Development Stages & Caste Determination Matrix
    private TextField eggDurationField;
    private TextField larvaDurationField;
    private ComboBox<String> larvaDietCombo;
    private TextField pupaDurationField;
    private TextField proteinMinorField;
    private TextField proteinMajorField;
    private TextField proteinSoldierField;
    private TextField proteinQueenField;
    private Slider pheroInhibSlider;
    private CheckBox haplodiploidyCheckBox;
    private Slider pathogenResistanceSlider;
    private Slider groomingSlider;

    // Caste Table
    private TableView<CasteRow> casteTable;
    private ObservableList<CasteRow> casteRows = FXCollections.observableArrayList();

    // Diet & Metabolism
    private ComboBox<String> primaryDietCombo;
    private ComboBox<String> secondaryDietCombo;
    private TextField foodConsumptionField;
    private TextField waterReqField;
    private TextField workerLifespanField;
    private TextField workerSpeedField;
    private TextField viewDistanceField;
    private TextField colonySizeField;
    private CheckBox megaColonyCheckBox;
    private CheckBox flyCheckBox;
    private TextField metabolismField;
    private TextField strengthField;

    // Nest & Behavior
    private ComboBox<String> nestTypeCombo;
    private TextField optTempField;
    private TextField minTempField;
    private TextField maxTempField;
    private Slider aggressionSlider;
    private Slider territorialitySlider;

    // Sensory & Perception Systems
    private CheckBox hasMagnetoreceptionCheckBox;
    private TextField magnetoSensField;
    private TextField thermoSensField;
    private TextField gasSensField;
    private TextField visualAcuityField;
    private TextField minLightField;
    private CheckBox hasVibrationSensingCheckBox;
    private TextField vibrationSensField;
    private CheckBox hasHygroreceptionCheckBox;
    private TextField hygroSensField;
    private CheckBox hasElectrosensingCheckBox;
    private TextField electroSensField;
    private CheckBox hasPolarizedLightCheckBox;

    // Biomechanical & Motor Systems
    private TextField wingbeatHzField;
    private CheckBox hasHoveringCheckBox;
    private TextField maxPayloadRatioField;
    private TextField bitingForceMpaField;
    private CheckBox hasAutothysisCheckBox;
    private CheckBox hasAroliaAdhesionCheckBox;

    private VBox warningBannerBox;
    private Consumer<CustomSpecies> onApplyListener;
    private Consumer<CustomSpecies> onGenerateNestForSpeciesListener;

    private boolean isUpdatingFields = false;
    private boolean isDirty = false;
    private String lastSelectedPreset = null;

    public SpeciesEditorPane() {
        setSpacing(10);

        // Warning Banner for parameter inconsistencies
        warningBannerBox = new VBox(8);
        warningBannerBox.setStyle("-fx-background-color: rgba(234, 179, 8, 0.15); -fx-border-color: #eab308; -fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 10px;");
        warningBannerBox.setVisible(false);
        warningBannerBox.setManaged(false);

        // Initialize Biomechanical & Motor fields
        wingbeatHzField = new TextField("200.0");
        hasHoveringCheckBox = new CheckBox();
        maxPayloadRatioField = new TextField("5.0");
        bitingForceMpaField = new TextField("15.0");
        hasAutothysisCheckBox = new CheckBox();
        hasAroliaAdhesionCheckBox = new CheckBox();

        // 2. TabPane for Parameter Sections
        TabPane tabPane = createTabPane();

        getChildren().addAll(buildHeader(), warningBannerBox, tabPane);

        casteRows.addListener((javafx.collections.ListChangeListener<CasteRow>) c -> {
            onFieldEdited();
            validateParameters();
        });

        attachUserChangeListeners();

        // Load default preset if available
        if (!presetManager.getPresetNames().isEmpty()) {
            String firstPreset = presetManager.getPresetNames().iterator().next();
            presetCombo.getSelectionModel().select(firstPreset);
            lastSelectedPreset = firstPreset;
            loadPresetToUI(presetManager.getPreset(firstPreset));
        }
    }

    private VBox buildHeader() {
        VBox v = new VBox(6);
        v.setPadding(new Insets(8, 10, 5, 10));

        HBox r = new HBox(8);
        r.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(I18nManager.getInstance().get("species.editor_title"));
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        HBox toolbar = createTopToolbar();

        r.getChildren().addAll(title, sp, toolbar);
        v.getChildren().addAll(r, new Separator());
        return v;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public boolean promptUnsavedChanges() {
        if (!isDirty) return true;
        String currentName = lastSelectedPreset != null ? lastSelectedPreset : "";
        boolean hasCurrentPreset = !currentName.isEmpty();

        I18nManager i18n = I18nManager.getInstance();
        Alert alert = ThemeManager.createAlert(
            Alert.AlertType.CONFIRMATION,
            "You have unsaved changes in the Species Editor.\n"
            + (hasCurrentPreset ? "Current preset: \"" + currentName + "\"" : "No preset selected.")
        );
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText("Exit Species Editor?");

        ButtonType btnUpdate  = hasCurrentPreset
            ? new ButtonType("💾 Update \"" + currentName + "\"", ButtonBar.ButtonData.OK_DONE)
            : null;
        ButtonType btnSaveAs  = new ButtonType("📝 Save As...", ButtonBar.ButtonData.OTHER);
        ButtonType btnDiscard = new ButtonType("🗑 Discard", ButtonBar.ButtonData.OTHER);
        ButtonType btnCancel  = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        if (btnUpdate != null) {
            alert.getButtonTypes().setAll(btnUpdate, btnSaveAs, btnDiscard, btnCancel);
        } else {
            alert.getButtonTypes().setAll(btnSaveAs, btnDiscard, btnCancel);
        }
        Optional<ButtonType> result = alert.showAndWait();

        if (!result.isPresent() || result.get() == btnCancel) return false;
        if (result.get() == btnDiscard) { isDirty = false; return true; }
        if (btnUpdate != null && result.get() == btnUpdate) {
            // Mise à jour directe sans dialogue
            CustomSpecies species = buildSpeciesFromUI();
            species.setPresetName(currentName);
            presetManager.addPreset(currentName, species);
            isUpdatingFields = true;
            try {
                if (!presetCombo.getItems().contains(currentName)) {
                    presetCombo.getItems().add(currentName);
                }
                presetCombo.getSelectionModel().select(currentName);
            } finally {
                isUpdatingFields = false;
            }
            lastSelectedPreset = currentName;
            isDirty = false;
            NotificationOverlay.show(this, "Preset \"" + currentName + "\" updated.", NotificationOverlay.NotificationType.SUCCESS);
            return true;
        }
        if (result.get() == btnSaveAs) {
            handleAddPreset();
            return !isDirty;
        }
        return false;
    }

    public void setOnApply(Consumer<CustomSpecies> listener) {
        this.onApplyListener = listener;
    }

    public void setOnGenerateNestForSpecies(Consumer<CustomSpecies> listener) {
        this.onGenerateNestForSpeciesListener = listener;
    }

    private HBox createTopToolbar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);

        I18nManager i18n = I18nManager.getInstance();

        Label lblPreset = new Label();
        lblPreset.textProperty().bind(i18n.createStringBinding("preset.label"));
        lblPreset.setStyle("-fx-font-weight: bold;");
        lblPreset.setGraphic(new FontIcon(Feather.SLIDERS));

        presetCombo = new ComboBox<>();
        presetCombo.setEditable(true);
        presetCombo.promptTextProperty().bind(i18n.createStringBinding("preset.prompt"));
        presetCombo.setTooltip(new Tooltip("Select a pre-configured eusocial insect species (Lasius, Atta, Apis, Bombus, Vespula, Macrotermes, etc.)."));
        presetCombo.getItems().setAll(presetManager.getPresetNames());
        FXCollections.sort(presetCombo.getItems());
        presetCombo.setPrefWidth(240);
        presetCombo.setOnAction(e -> {
            if (isUpdatingFields) return;
            String sel = presetCombo.getValue();
            if (sel == null || sel.equals(lastSelectedPreset)) return;

            if (isDirty) {
                Alert alert = org.swarmforge.client.util.ThemeManager.createAlert(
                    Alert.AlertType.CONFIRMATION,
                    "Warning: You have unsaved changes on the current species.\n\nDo you really want to load preset '" + sel + "' and discard your changes?"
                );
                alert.setTitle(I18nManager.getInstance().get("common.dialog.unsaved"));
                alert.setHeaderText("Species Preset Change");
                java.util.Optional<ButtonType> res = alert.showAndWait();
                if (res.isEmpty() || res.get() != ButtonType.OK) {
                    isUpdatingFields = true;
                    try {
                        presetCombo.setValue(lastSelectedPreset);
                    } finally {
                        isUpdatingFields = false;
                    }
                    return;
                }
            }

            if (presetManager.contains(sel)) {
                lastSelectedPreset = sel;
                loadPresetToUI(presetManager.getPreset(sel));
            }
        });

        Button btnSave = new Button();
        btnSave.setGraphic(new FontIcon(Feather.SAVE));
        btnSave.textProperty().bind(i18n.createStringBinding("preset.save"));
        btnSave.getStyleClass().add("btn-secondary");
        btnSave.setTooltip(new Tooltip("Save current species configuration as a new preset."));
        btnSave.setOnAction(e -> handleAddPreset());

        Button btnDelete = new Button();
        btnDelete.setGraphic(new FontIcon(Feather.TRASH_2));
        btnDelete.textProperty().bind(i18n.createStringBinding("preset.delete"));
        btnDelete.getStyleClass().add("btn-danger");
        btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold;");
        btnDelete.setTooltip(new Tooltip("Delete selected species preset."));
        btnDelete.setOnAction(e -> handleDeletePreset());

        Button btnExport = new Button();
        btnExport.setGraphic(new FontIcon(Feather.DOWNLOAD));
        btnExport.textProperty().bind(i18n.createStringBinding("preset.export"));
        btnExport.getStyleClass().add("btn-secondary");
        btnExport.setTooltip(new Tooltip("Export species parameters to JSON format."));
        btnExport.setOnAction(e -> handleSaveDisk());

        Button btnImport = new Button();
        btnImport.setGraphic(new FontIcon(Feather.UPLOAD));
        btnImport.textProperty().bind(i18n.createStringBinding("preset.import"));
        btnImport.getStyleClass().add("btn-secondary");
        btnImport.setTooltip(new Tooltip("Import species JSON configuration file."));
        btnImport.setOnAction(e -> handleLoadDisk());

        bar.getChildren().addAll(lblPreset, presetCombo, btnSave, btnDelete, new Separator(Orientation.VERTICAL), btnExport, btnImport);
        return bar;
    }

    private TabPane createTabPane() {
        mainTabPane = new TabPane();
        mainTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab tabTaxonomy = new Tab();
        tabTaxonomy.textProperty().bind(i18n.createStringBinding("species.tab.taxonomy"));
        tabTaxonomy.setContent(createTaxonomyPane());
        tabTaxonomy.setGraphic(new FontIcon(Feather.BOOK));

        Tab tabQueens = new Tab();
        tabQueens.textProperty().bind(i18n.createStringBinding("species.tab.queens"));
        tabQueens.setContent(createQueensPane());
        tabQueens.setGraphic(new FontIcon(Feather.AWARD));

        Tab tabCastes = new Tab();
        tabCastes.textProperty().bind(i18n.createStringBinding("species.tab.castes"));
        tabCastes.setContent(createCastesPane());
        tabCastes.setGraphic(new FontIcon(Feather.USERS));

        Tab tabStages = new Tab();
        tabStages.textProperty().bind(i18n.createStringBinding("species.tab.stages"));
        tabStages.setContent(createStagesPane());
        tabStages.setGraphic(new FontIcon(Feather.CLOCK));

        Tab tabDiet = new Tab();
        tabDiet.textProperty().bind(i18n.createStringBinding("species.tab.diet"));
        tabDiet.setContent(createDietPane());
        tabDiet.setGraphic(new FontIcon(Feather.FEATHER));

        Tab tabSensors = new Tab();
        tabSensors.textProperty().bind(i18n.createStringBinding("species.tab.sensors"));
        tabSensors.setContent(createSensorsPane());
        tabSensors.setGraphic(new FontIcon(Feather.EYE));

        Tab tabNest = new Tab();
        tabNest.textProperty().bind(i18n.createStringBinding("species.tab.nest"));
        tabNest.setContent(createNestPane());
        tabNest.setGraphic(new FontIcon(Feather.HOME));

        List<Tab> tabs = List.of(tabTaxonomy, tabQueens, tabCastes, tabStages, tabDiet, tabSensors, tabNest);
        mainTabPane.getTabs().addAll(tabs);
        VBox.setVgrow(mainTabPane, Priority.ALWAYS);
        return mainTabPane;
    }

    // --- Tab 1: Taxonomy ---
    private ScrollPane createTaxonomyPane() {
        GridPane grid = createGrid();

        commonNameField = new TextField("Black Garden Ant");
        scientificNameField = new TextField("Lasius niger");
        genusField = new TextField("Lasius");

        insectTypeCombo = new ComboBox<>(FXCollections.observableArrayList("Ants (Formicidae)", "Bees (Apidae)", "Other Eusocial Taxa", "Termites (Termitoidae)", "Wasps (Vespidae)"));
        ComboBoxTooltipHelper.setupDescriptiveComboBox(insectTypeCombo,
            val -> val,
            val -> switch (val != null ? val : "") {
                case "Ants (Formicidae)" -> "Eusocial insects forming large subterranean colonies with specialized castes.";
                case "Bees (Apidae)" -> "Hymenopteran insects producing honey, practicing bee dances, and building wax combs.";
                case "Wasps (Vespidae)" -> "Predatory hymenopterans constructing paper/carton nests from wood fibers.";
                case "Termites (Termitoidae)" -> "Isoptera consuming cellulose, organized into blind worker/soldier castes under a royal couple.";
                default -> "Other subsocial or eusocial arthropod taxa (e.g. Thrips, Gall aphids, Eusocial shrimp).";
            }
        );
        insectTypeCombo.getSelectionModel().select("Ants (Formicidae)");

        categoryCombo = new ComboBox<>(FXCollections.observableArrayList(
                org.swarmforge.core.species.SpeciesCategory.values()
        ));
        ComboBoxTooltipHelper.setupDescriptiveComboBox(categoryCombo,
            cat -> cat != null ? cat.label : "",
            cat -> cat != null ? cat.label : ""
        );
        categoryCombo.getSelectionModel().select(org.swarmforge.core.species.SpeciesCategory.EUSOCIAL_PRIMARY);
        categoryCombo.setOnAction(e -> validateParameters());

        Label categoryHintLabel = new Label("ℹ️ This editor manages eusocial species (Ants, Bees, Wasps, Termites). Prey, predators, and commensals are managed in the dedicated 'Associated Species & Commensals' panel.");
        categoryHintLabel.getStyleClass().add("help-entry-desc");
        categoryHintLabel.setStyle("-fx-font-size: 11px; -fx-wrap-text: true;");

        // Taxon links HBox
        HBox taxonLinks = new HBox(10);
        taxonLinks.setAlignment(Pos.CENTER_LEFT);
        Label lblLinksTitle = new Label(I18nManager.getInstance().get("species.taxonomy_sheet"));
        lblLinksTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");

        Hyperlink linkFormicidae = new Hyperlink("🐜 Formicidae (Ants)");
        linkFormicidae.setOnAction(e -> openWiki("https://fr.wikipedia.org/wiki/Formicidae"));

        Hyperlink linkApidae = new Hyperlink("🐝 Apidae (Bees)");
        linkApidae.setOnAction(e -> openWiki("https://fr.wikipedia.org/wiki/Apidae"));

        Hyperlink linkVespidae = new Hyperlink("🐝 Vespidae (Wasps)");
        linkVespidae.setOnAction(e -> openWiki("https://fr.wikipedia.org/wiki/Vespidae"));

        Hyperlink linkTermitoidae = new Hyperlink("🐜 Termitoidae (Termites)");
        linkTermitoidae.setOnAction(e -> openWiki("https://fr.wikipedia.org/wiki/Termite"));

        taxonLinks.getChildren().addAll(lblLinksTitle, linkFormicidae, linkApidae, linkVespidae, linkTermitoidae);

        descriptionArea = new TextArea("Species description and ecological notes...");
        descriptionArea.setPrefRowCount(4);

        grid.addRow(0, createTooltipLabel("Common Name:", "Vernacular name of the species in common language.", commonNameField), commonNameField);
        grid.addRow(1, createTooltipLabel("Scientific Name (Binomial):", "Official Latin binomial nomenclature (e.g., Lasius niger, Formica rufa, Atta cephalotes).", scientificNameField), scientificNameField);
        grid.addRow(2, createTooltipLabel("Taxonomic Genus:", "Genus to which the species belongs (e.g., Lasius, Formica, Atta, Apis, Solenopsis, Vespula, Reticulitermes).", genusField), genusField);
        grid.addRow(3, createTooltipLabel("Taxonomic Order / Family:", "Major taxonomic group of eusocial insects (Ant, Bee, Wasp, Termite).", insectTypeCombo), insectTypeCombo);
        grid.addRow(4, new Label(""), taxonLinks);
        grid.addRow(5, createTooltipLabel("Ecological Role / Category:", "Trophic status and functional role in simulation ecosystem.", categoryCombo), categoryCombo);
        grid.addRow(6, new Label(""), categoryHintLabel);
        grid.addRow(7, createTooltipLabel("Description & Ecological Notes:", "Summary of species biology, habitat, and behavior.", descriptionArea), descriptionArea);

        return wrapScroll(grid);
    }

    private void openWiki(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception ex) {
            org.swarmforge.client.ui.GlossaryDialog.show();
        }
    }

    // --- Tab 2: Colony & Queens (Aspect CRITIQUE) ---
    private ScrollPane createQueensPane() {
        GridPane grid = createGrid();

        queenModeCombo = new ComboBox<>(FXCollections.observableArrayList("Gamergates (Reproductive Workers)", "Monogyne (Single Queen)", "Polygyne (Multiple Queens)"));
        ComboBoxTooltipHelper.setupDescriptiveComboBox(queenModeCombo,
            val -> val,
            val -> switch (val != null ? val : "") {
                case "Monogyne (Single Queen)" -> "Strictly one egg-laying queen tolerated per colony. Her death triggers colony decline.";
                case "Polygyne (Multiple Queens)" -> "Multiple fertile queens cohabit peacefully, ensuring high egg laying and resilience.";
                case "Gamergates (Reproductive Workers)" -> "No distinct queen caste; specialized workers (gamergates) mate and lay eggs.";
                default -> "";
            }
        );
        queenModeCombo.getSelectionModel().select("Monogyne (Single Queen)");
        queenModeCombo.setOnAction(e -> {
            if (queenModeCombo.getValue() != null && queenModeCombo.getValue().contains("Monogyne")) {
                queenCountSpinner.getValueFactory().setValue(1);
            }
            validateParameters();
        });

        queenCountSpinner = new Spinner<>(1, 500, 1);
        queenCountSpinner.valueProperty().addListener((obs, oldV, newV) -> {
            if (queenModeCombo.getValue() != null && queenModeCombo.getValue().contains("Monogyne") && newV > 1) {
                queenCountSpinner.getValueFactory().setValue(1);
            }
            validateParameters();
        });

        queenLifespanField = new TextField("25000");
        queenEggRateField = new TextField("25.0");

        hasKingCheckBox = new CheckBox("Presence of Reproductive King (Termites)");
        hasKingCheckBox.setOnAction(e -> validateParameters());

        kingLifespanField = new TextField("15000");
        nuptialFlightCombo = new ComboBox<>(FXCollections.observableArrayList("Aerial Swarm Flight", "Budding / Sociotomy", "In-Nest Mating", "Swarm Division"));
        ComboBoxTooltipHelper.setupDescriptiveComboBox(nuptialFlightCombo,
            val -> val,
            val -> switch (val != null ? val : "") {
                case "Aerial Swarm Flight" -> "Mass synchronized flight of alate alates and males in warm, humid weather.";
                case "Budding / Sociotomy" -> "Progressive separation of workers with fertile queens to a nearby satellite nest.";
                case "In-Nest Mating" -> "Alates mate inside the parent nest without risky flight.";
                case "Swarm Division" -> "Mother queen leaves with a worker cohort to establish a new colony.";
                default -> "";
            }
        );
        nuptialFlightCombo.getSelectionModel().select("Aerial Swarm Flight");
        nuptialFlightCombo.getSelectionModel().select("AERIAL_SWARM");

        colonySizeField = new TextField("15000");
        megaColonyCheckBox = new CheckBox("Forms Supercolonies (Unicoloniality / Multi-nest Network)");

        grid.addRow(0, createTooltipLabel("Gynic Structure (Queen Mode):", "Reproductive queen organization mode: Monogyne (1 queen), Polygyne (multiple queens), or Gamergates (reproductive workers).", queenModeCombo, "Monogyne"), queenModeCombo);
        grid.addRow(1, createTooltipLabel("Founding Queens Count (ind):", "Initial or maximum fertile reproductive queens residing in colony.", queenCountSpinner), queenCountSpinner);
        grid.addRow(2, createTooltipLabel("Queen Lifespan (days):", "Maximum queen lifespan before natural senescence.", queenLifespanField), queenLifespanField);
        grid.addRow(3, createTooltipLabel("Queen Egg Laying Rate (eggs/day):", "Daily eggs laid per queen under optimal conditions.", queenEggRateField), queenEggRateField);
        grid.addRow(4, createTooltipLabel("Reproductive King (Isoptera):", "Presence of a permanent reproductive male (king) living alongside the queen (termites).", hasKingCheckBox, "King"), hasKingCheckBox);
        grid.addRow(5, createTooltipLabel("King Lifespan (days):", "Reproductive king lifespan in isopteran species.", kingLifespanField), kingLifespanField);
        grid.addRow(6, createTooltipLabel("Nuptial Flight / Swarming Mode:", "Dispersal and mating strategy: Aerial swarm, swarm division, budding, or in-nest mating.", nuptialFlightCombo, "Nuptial"), nuptialFlightCombo);
        grid.addRow(7, createTooltipLabel("Mature Colony Population (ind):", "Average population size of a mature colony at ecological equilibrium.", colonySizeField), colonySizeField);
        grid.addRow(8, createTooltipLabel("Supercolonies (Unicoloniality):", "Ability to form interconnected nest networks without intraspecific aggression.", megaColonyCheckBox), megaColonyCheckBox);

        return wrapScroll(grid);
    }

    // --- Tab 3: Development Stages & Caste Transition Matrix ---
    private ScrollPane createStagesPane() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(15));

        VBox cardDurations = new VBox(10);
        cardDurations.getStyleClass().add("card-pane");
        Label titleDurations = new Label("⏱ Metamorphosis Stage Durations");
        titleDurations.getStyleClass().add("card-title");

        GridPane gridDurations = createGrid();
        eggDurationField = new TextField("300");
        larvaDurationField = new TextField("600");
        larvaDietCombo = new ComboBox<>(FXCollections.observableArrayList(
            "Cellulose & Wood Fibers",
            "Fungus Garden Mycelium",
            "High Protein Meat & Insects",
            "Omnivorous Mixed Diet",
            "Seeds & Harvested Grains",
            "Sugars, Honey & Nectar"
        ));
        ComboBoxTooltipHelper.setupDescriptiveComboBox(larvaDietCombo,
            val -> val,
            val -> switch (val != null ? val : "") {
                case "Cellulose & Wood Fibers" -> "Wood digests and plant fibers broken down by intestinal symbionts (Termites).";
                case "Fungus Garden Mycelium" -> "Mycelial food cultivated by the colony on chewed leaf substrate (Atta).";
                case "High Protein Meat & Insects" -> "High protein diet from crushed prey insects, essential for brood and soldier development.";
                case "Omnivorous Mixed Diet" -> "Varied diet combining honeydew, arthropod remains, seeds, and sugary liquids.";
                case "Seeds & Harvested Grains" -> "Harvested, shelled, and crushed seeds forming 'ant bread' (Messor).";
                case "Sugars, Honey & Nectar" -> "Carbohydrate-rich liquid diet (nectar, aphid honeydew, fruit juices) providing metabolic energy.";
                default -> "";
            }
        );
        larvaDietCombo.getSelectionModel().select("High Protein Meat & Insects");
        pupaDurationField = new TextField("500");

        gridDurations.addRow(0, createTooltipLabel("Egg Stage Duration (days):", "Incubation period required before first larval instar.", eggDurationField), eggDurationField);
        gridDurations.addRow(1, createTooltipLabel("Larval Stage Duration (days):", "Period of intensive larval growth and feeding.", larvaDurationField), larvaDurationField);
        gridDurations.addRow(2, createTooltipLabel("Larval Diet Requirement:", "Specific food provided by nurse workers to developing larvae.", larvaDietCombo), larvaDietCombo);
        gridDurations.addRow(3, createTooltipLabel("Pupal Stage / Cocoon Duration (days):", "Metamorphosis duration required to form adult imago.", pupaDurationField), pupaDurationField);
        cardDurations.getChildren().addAll(titleDurations, gridDurations);

        VBox cardMatrix = new VBox(10);
        cardMatrix.getStyleClass().add("card-pane");
        Label titleMatrix = new Label("📊 Caste Determination Matrix (Nutrition & Pheromones)");
        titleMatrix.getStyleClass().add("card-title");

        GridPane gridMatrix = createGrid();
        proteinMinorField = new TextField("0.35");
        proteinMajorField = new TextField("0.70");
        proteinSoldierField = new TextField("0.85");
        proteinQueenField = new TextField("0.95");
        pheroInhibSlider = new Slider(0.0, 1.0, 0.8);
        pheroInhibSlider.setShowTickLabels(true); pheroInhibSlider.setShowTickMarks(true);
        haplodiploidyCheckBox = new CheckBox("Arrhenotoky / Haplodiploidy (Unfertilized egg = Male)");
        haplodiploidyCheckBox.setSelected(true);

        gridMatrix.addRow(0, createTooltipLabel("Minor Worker Protein Threshold (%):", "Minimum larval protein fraction required to differentiate a minor worker.", proteinMinorField), proteinMinorField);
        gridMatrix.addRow(1, createTooltipLabel("Major Worker Protein Threshold (%):", "Protein fraction required to differentiate a major worker.", proteinMajorField), proteinMajorField);
        gridMatrix.addRow(2, createTooltipLabel("Soldier Protein Threshold (%):", "Protein threshold required for soldier caste differentiation.", proteinSoldierField), proteinSoldierField);
        gridMatrix.addRow(3, createTooltipLabel("Royal Food Protein Threshold (%):", "Maximum nutritional threshold inducing fertile queen differentiation.", proteinQueenField), proteinQueenField);
        gridMatrix.addRow(4, createTooltipLabel("Queen Pheromonal Inhibition:", "Chemical inhibition emitted by queen to suppress queen rearing.", pheroInhibSlider, "Inhibition"), pheroInhibSlider);
        gridMatrix.addRow(5, createTooltipLabel("Male Determination (Arrhenotoky):", "Arrhenotoky / Haplodiploidy: unfertilized eggs produce males, fertilized eggs produce females.", haplodiploidyCheckBox, "Haplodiploidy"), haplodiploidyCheckBox);
        cardMatrix.getChildren().addAll(titleMatrix, gridMatrix);

        VBox cardImmunity = new VBox(10);
        cardImmunity.getStyleClass().add("card-pane");
        Label titleImmunity = new Label("🦠 Immunity & Health Defense (Allogrooming & Hygiene)");
        titleImmunity.getStyleClass().add("card-title");

        GridPane gridImmunity = createGrid();
        pathogenResistanceSlider = new Slider(0.0, 1.0, 0.5);
        pathogenResistanceSlider.setShowTickLabels(true); pathogenResistanceSlider.setShowTickMarks(true);
        groomingSlider = new Slider(0.0, 1.0, 0.7);
        groomingSlider.setShowTickLabels(true); groomingSlider.setShowTickMarks(true);

        gridImmunity.addRow(0, createTooltipLabel("Pathogen Immune Resistance (%):", "Physiological resistance to fungal/bacterial pathogens and spores.", pathogenResistanceSlider), pathogenResistanceSlider);
        gridImmunity.addRow(1, createTooltipLabel("Social Grooming Efficacy (%):", "Efficacy of mutual grooming and de-parasitism in reducing spore load.", groomingSlider), groomingSlider);
        cardImmunity.getChildren().addAll(titleImmunity, gridImmunity);

        box.getChildren().addAll(cardDurations, cardMatrix, cardImmunity);
        return wrapScroll(box);
    }

    // --- Tab 4: Castes & Morphology ---
    private ScrollPane createCastesPane() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(15));

        Label infoLabel = new Label("💡 Caste summary table. Click a row to inspect or modify all parameters in the inspector below.");
        infoLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-style: italic;");

        casteTable = new TableView<>(casteRows);
        casteTable.setEditable(true);
        casteTable.setPrefHeight(240);

        TableColumn<CasteRow, String> nameCol = new TableColumn<>("Caste");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(e -> e.getRowValue().setName(e.getNewValue()));
        nameCol.setPrefWidth(130);

        TableColumn<CasteRow, Double> bodyCol = new TableColumn<>("Length (mm)");
        bodyCol.setCellValueFactory(new PropertyValueFactory<>("bodyLengthMm"));
        bodyCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new FormattedDoubleStringConverter()));
        bodyCol.setOnEditCommit(e -> {
            e.getRowValue().setBodyLengthMm(e.getNewValue());
            casteTable.refresh();
        });
        bodyCol.setPrefWidth(95);

        TableColumn<CasteRow, Double> headCol = new TableColumn<>("Head (mm)");
        headCol.setCellValueFactory(new PropertyValueFactory<>("headWidthMm"));
        headCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new FormattedDoubleStringConverter()));
        headCol.setOnEditCommit(e -> {
            e.getRowValue().setHeadWidthMm(e.getNewValue());
            casteTable.refresh();
        });
        headCol.setPrefWidth(75);

        TableColumn<CasteRow, Double> tunnelCol = new TableColumn<>("Ø Tunnel Min (mm)");
        tunnelCol.setCellValueFactory(new PropertyValueFactory<>("minTunnelMm"));
        tunnelCol.setCellFactory(col -> new TableCell<CasteRow, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatDec(item));
            }
        });
        tunnelCol.setPrefWidth(125);

        TableColumn<CasteRow, Integer> lifeCol = new TableColumn<>("Lifespan (d)");
        lifeCol.setCellValueFactory(new PropertyValueFactory<>("lifespan"));
        lifeCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new javafx.util.converter.IntegerStringConverter()));
        lifeCol.setOnEditCommit(e -> e.getRowValue().setLifespan(e.getNewValue()));
        lifeCol.setPrefWidth(85);

        TableColumn<CasteRow, Float> healthCol = new TableColumn<>("Health");
        healthCol.setCellValueFactory(new PropertyValueFactory<>("health"));
        healthCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new FormattedFloatStringConverter()));
        healthCol.setOnEditCommit(e -> e.getRowValue().setHealth(e.getNewValue()));
        healthCol.setPrefWidth(65);

        TableColumn<CasteRow, Float> dmgCol = new TableColumn<>("Attack");
        dmgCol.setCellValueFactory(new PropertyValueFactory<>("damage"));
        dmgCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new FormattedFloatStringConverter()));
        dmgCol.setOnEditCommit(e -> e.getRowValue().setDamage(e.getNewValue()));
        dmgCol.setPrefWidth(65);

        TableColumn<CasteRow, Boolean> flyCol = new TableColumn<>("Flying");
        flyCol.setCellValueFactory(new PropertyValueFactory<>("canFly"));
        flyCol.setCellFactory(col -> new TableCell<CasteRow, Boolean>() {
            private final CheckBox cb = new CheckBox();
            {
                cb.setOnAction(e -> {
                    CasteRow row = getTableView().getItems().get(getIndex());
                    if (row != null) {
                        row.setCanFly(cb.isSelected());
                    }
                });
            }
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    cb.setSelected(item);
                    setGraphic(cb);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        flyCol.setPrefWidth(65);

        TableColumn<CasteRow, Float> ratioCol = new TableColumn<>("Target Ratio");
        ratioCol.setCellValueFactory(new PropertyValueFactory<>("targetRatio"));
        ratioCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new FormattedFloatStringConverter()));
        ratioCol.setOnEditCommit(e -> e.getRowValue().setTargetRatio(e.getNewValue()));
        ratioCol.setPrefWidth(75);

        TableColumn<CasteRow, String> archCol = new TableColumn<>("Decision Model");
        archCol.setCellValueFactory(new PropertyValueFactory<>("decisionArch"));
        archCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
        archCol.setOnEditCommit(e -> e.getRowValue().setDecisionArch(e.getNewValue()));
        archCol.setPrefWidth(110);

        TableColumn<CasteRow, Float> forageCol = new TableColumn<>("Foraging Weight");
        forageCol.setCellValueFactory(new PropertyValueFactory<>("foragingWeight"));
        forageCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new FormattedFloatStringConverter()));
        forageCol.setOnEditCommit(e -> e.getRowValue().setForagingWeight(e.getNewValue()));
        forageCol.setPrefWidth(85);

        TableColumn<CasteRow, Float> defCol = new TableColumn<>("Defense Weight");
        defCol.setCellValueFactory(new PropertyValueFactory<>("defenseWeight"));
        defCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new FormattedFloatStringConverter()));
        defCol.setOnEditCommit(e -> e.getRowValue().setDefenseWeight(e.getNewValue()));
        defCol.setPrefWidth(85);

        TableColumn<CasteRow, Float> excCol = new TableColumn<>("Excavation Weight");
        excCol.setCellValueFactory(new PropertyValueFactory<>("excavationWeight"));
        excCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new FormattedFloatStringConverter()));
        excCol.setOnEditCommit(e -> e.getRowValue().setExcavationWeight(e.getNewValue()));
        excCol.setPrefWidth(95);

        TableColumn<CasteRow, Float> nurseCol = new TableColumn<>("Nursing Weight");
        nurseCol.setCellValueFactory(new PropertyValueFactory<>("nursingWeight"));
        nurseCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new FormattedFloatStringConverter()));
        nurseCol.setOnEditCommit(e -> e.getRowValue().setNursingWeight(e.getNewValue()));
        nurseCol.setPrefWidth(75);

        TableColumn<CasteRow, String> venomTypeCol = new TableColumn<>("Weapons / Venom");
        venomTypeCol.setCellValueFactory(new PropertyValueFactory<>("venomType"));
        venomTypeCol.setCellFactory(col -> new TableCell<CasteRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(getVenomTitle(item));
                }
            }
        });
        venomTypeCol.setPrefWidth(140);

        TableColumn<CasteRow, Float> venomToxCol = new TableColumn<>("Venom Toxicity");
        venomToxCol.setCellValueFactory(new PropertyValueFactory<>("venomToxicity"));
        venomToxCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new FormattedFloatStringConverter()));
        venomToxCol.setOnEditCommit(e -> e.getRowValue().setVenomToxicity(e.getNewValue()));
        venomToxCol.setPrefWidth(90);

        TableColumn<CasteRow, Float> biteCol = new TableColumn<>("Bite Force (MPa)");
        biteCol.setCellValueFactory(new PropertyValueFactory<>("bitingForceMpa"));
        biteCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new FormattedFloatStringConverter()));
        biteCol.setOnEditCommit(e -> e.getRowValue().setBitingForceMpa(e.getNewValue()));
        biteCol.setPrefWidth(95);

        TableColumn<CasteRow, Float> loadCol = new TableColumn<>("Payload (g/g)");
        loadCol.setCellValueFactory(new PropertyValueFactory<>("maxPayloadRatio"));
        loadCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new FormattedFloatStringConverter()));
        loadCol.setOnEditCommit(e -> e.getRowValue().setMaxPayloadRatio(e.getNewValue()));
        loadCol.setPrefWidth(90);

        TableColumn<CasteRow, Float> hzCol = new TableColumn<>("Wings (Hz)");
        hzCol.setCellValueFactory(new PropertyValueFactory<>("wingbeatFrequencyHz"));
        hzCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new FormattedFloatStringConverter()));
        hzCol.setOnEditCommit(e -> e.getRowValue().setWingbeatFrequencyHz(e.getNewValue()));
        hzCol.setPrefWidth(80);

        // Table Columns grouped in phase with the 4 Inspector sections
        casteTable.getColumns().addAll(
            // 👤 Identité & Morphologie
            nameCol, bodyCol, headCol, tunnelCol, healthCol, dmgCol, lifeCol,
            // ⚡ Biomécanique & Vol
            flyCol, hzCol, biteCol, loadCol,
            // 🧠 IA & Allocation Tâches
            archCol, ratioCol, forageCol, defCol, excCol, nurseCol,
            // 🛡️ Armes & Toxines
            venomTypeCol, venomToxCol
        );

        // Controls to add/edit caste (Inspector Panel by Columns)
        VBox casteInspectorCard = new VBox(10);
        casteInspectorCard.getStyleClass().add("card-pane");
        Label titleInspector = new Label("🔍 Selected Caste Inspector & Editor (Column Mode)");
        titleInspector.getStyleClass().add("card-title");

        TextField casteNameF = new TextField("Soldier");
        TextField casteBodyF = new TextField("6.0");
        TextField casteHeadF = new TextField("1.8");
        TextField casteLifeF = new TextField("5000");
        TextField casteHealthF = new TextField("120");
        TextField casteDmgF = new TextField("15");
        CheckBox casteFlyCheck = new CheckBox("Flying");

        // Advanced Caste Parameters
        TextField targetRatioF = new TextField("0.25");
        ComboBox<String> decisionArchCombo = new ComboBox<>();
        for (ArchitectureType type : ArchitectureType.values()) {
            decisionArchCombo.getItems().add(type.getDisplayName());
        }
        ComboBoxTooltipHelper.setupDescriptiveComboBox(decisionArchCombo, SpeciesEditorPane::getDecisionArchTitle, SpeciesEditorPane::getDecisionArchDescription);
        decisionArchCombo.setValue(ArchitectureType.BDI.getDisplayName());

        TextField foragingWField = new TextField("0.30");
        TextField defenseWField = new TextField("0.20");
        TextField excavationWField = new TextField("0.20");
        TextField nursingWField = new TextField("0.15");

        // Motor & Biomechanical Caste Parameters
        TextField casteWingbeatHzF = new TextField("0.0");
        CheckBox casteHoverCheck = new CheckBox("Hovering Flight");
        TextField castePayloadRatioF = new TextField("5.0");
        TextField casteBitingForceMpaF = new TextField("15.0");
        CheckBox casteAutothysisCheck = new CheckBox("Explosive Autothysis Defense");
        CheckBox casteAroliaCheck = new CheckBox("Arolia Adhesive Pads");
        casteAroliaCheck.setSelected(true);

        MenuButton casteVenomMenuButton = new MenuButton("🚫 No Venom (Physical Attack)");
        casteVenomMenuButton.setPrefWidth(220);
        casteVenomMenuButton.getStyleClass().add("btn-secondary");

        List<String> venomOptions = List.of(
            "NONE", "FORMIC_ACID", "VENOMOUS_STING", "CHEMICAL_SPRAY", "ACID_SPRAY",
            "SOLENOPSIN", "NEUROTOXIN", "CYTOTOXIN", "TERPENE_RESIN", "AUTOTHYSIS_BOMB", "POWERFUL_MANDIBLES"
        );

        java.util.Map<String, CheckMenuItem> casteVenomCheckItems = new java.util.LinkedHashMap<>();
        for (String opt : venomOptions) {
            CheckMenuItem item = new CheckMenuItem(getVenomTitle(opt));
            item.setOnAction(e -> {
                if ("NONE".equals(opt) && item.isSelected()) {
                    casteVenomCheckItems.forEach((k, v) -> { if (!"NONE".equals(k)) v.setSelected(false); });
                } else if (!"NONE".equals(opt) && item.isSelected()) {
                    if (casteVenomCheckItems.containsKey("NONE")) casteVenomCheckItems.get("NONE").setSelected(false);
                }
                updateVenomMenuText(casteVenomMenuButton, casteVenomCheckItems);
                onFieldEdited();
            });
            casteVenomCheckItems.put(opt, item);
            casteVenomMenuButton.getItems().add(item);
        }
        updateVenomMenuText(casteVenomMenuButton, casteVenomCheckItems);

        TextField casteVenomToxField = new TextField("10.0");

        casteTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                casteNameF.setText(newVal.getName());
                casteBodyF.setText(formatDec(newVal.getBodyLengthMm()));
                casteHeadF.setText(formatDec(newVal.getHeadWidthMm()));
                casteLifeF.setText(String.valueOf(newVal.getLifespan()));
                casteHealthF.setText(formatDec(newVal.getHealth()));
                casteDmgF.setText(formatDec(newVal.getDamage()));
                casteFlyCheck.setSelected(newVal.isCanFly());

                targetRatioF.setText(formatDec(newVal.getTargetRatio()));
                decisionArchCombo.setValue(newVal.getDecisionArch());
                foragingWField.setText(formatDec(newVal.getForagingWeight()));
                defenseWField.setText(formatDec(newVal.getDefenseWeight()));
                excavationWField.setText(formatDec(newVal.getExcavationWeight()));
                nursingWField.setText(formatDec(newVal.getNursingWeight()));
                setSelectedVenomTypes(casteVenomCheckItems, casteVenomMenuButton, newVal.getVenomType());
                casteVenomToxField.setText(formatDec(newVal.getVenomToxicity()));

                casteWingbeatHzF.setText(formatDec(newVal.getWingbeatFrequencyHz()));
                casteHoverCheck.setSelected(newVal.isHasHoveringCapability());
                castePayloadRatioF.setText(formatDec(newVal.getMaxPayloadRatio()));
                casteBitingForceMpaF.setText(formatDec(newVal.getBitingForceMpa()));
                casteAutothysisCheck.setSelected(newVal.isHasAutothysis());
                casteAroliaCheck.setSelected(newVal.isHasAroliaAdhesion());
            }
        });

        // Inspector Columns Layout (Organized by vertical columns)
        HBox casteInspectorColumns = new HBox(10);

        // Column 1: 👤 Identité & Morphologie
        GridPane col1Grid = createColumnGrid();
        col1Grid.addRow(0, createTooltipLabel("Caste Name:", "Functional caste name within the colony.", casteNameF), casteNameF);
        col1Grid.addRow(1, createTooltipLabel("Body Length (mm):", "Total body length from head to abdominal apex in mm.", casteBodyF), casteBodyF);
        col1Grid.addRow(2, createTooltipLabel("Head Width (mm):", "Maximum head capsule width determining minimum gallery diameter.", casteHeadF), casteHeadF);
        col1Grid.addRow(3, createTooltipLabel("Base Health:", "Initial health points of the caste.", casteHealthF), casteHealthF);
        col1Grid.addRow(4, createTooltipLabel("Attack Damage:", "Physical damage dealt per bite/attack.", casteDmgF), casteDmgF);
        col1Grid.addRow(5, createTooltipLabel("Lifespan (days):", "Average lifespan of caste members in days.", casteLifeF), casteLifeF);
        VBox col1Box = createInspectorColumnBox("👤 Identity & Morphology", col1Grid);

        // Column 2: ⚡ Biomécanique & Vol
        GridPane col2Grid = createColumnGrid();
        col2Grid.addRow(0, createTooltipLabel("Flight Capability:", "Indicates if caste members possess wings and flight capability.", casteFlyCheck), casteFlyCheck);
        col2Grid.addRow(1, createTooltipLabel("Wingbeat Frequency (Hz):", "Wingbeat frequency if caste flies (0 Hz if wingless).", casteWingbeatHzF), casteWingbeatHzF);
        col2Grid.addRow(2, createTooltipLabel("Hovering Flight:", "Ability to maintain stationary position in flight.", casteHoverCheck), casteHoverCheck);
        col2Grid.addRow(3, createTooltipLabel("Biting Force (MPa):", "Mandibular biting force exerted by cephalic muscles.", casteBitingForceMpaF, "Mandibule"), casteBitingForceMpaF);
        col2Grid.addRow(4, createTooltipLabel("Payload Ratio (g/g):", "Maximum carrying load ratio relative to body weight.", castePayloadRatioF), castePayloadRatioF);
        col2Grid.addRow(5, createTooltipLabel("Arolia Adhesion:", "Presence of tarsal arolia pads for walking on vertical walls & ceilings.", casteAroliaCheck, "Arolia"), casteAroliaCheck);
        VBox col2Box = createInspectorColumnBox("⚡ Biomechanics & Flight", col2Grid);

        // Column 3: 🧠 IA & Allocation Tâches
        GridPane col3Grid = createColumnGrid();
        col3Grid.addRow(0, createTooltipLabel("Decision Model:", "Cognitive architecture (BDI, Neural Network, FSM, Behavior Tree, Fuzzy Logic)", decisionArchCombo, "FSM"), decisionArchCombo);
        col3Grid.addRow(1, createTooltipLabel("Target Ratio (%):", "Target percentage of this caste among workers.", targetRatioF), targetRatioF);
        col3Grid.addRow(2, createTooltipLabel("Foraging Weight:", "Task allocation weight for foraging", foragingWField), foragingWField);
        col3Grid.addRow(3, createTooltipLabel("Defense Weight:", "Task allocation weight for defense", defenseWField), defenseWField);
        col3Grid.addRow(4, createTooltipLabel("Excavation Weight:", "Task allocation weight for excavation", excavationWField), excavationWField);
        col3Grid.addRow(5, createTooltipLabel("Nursing Weight:", "Task allocation weight for nursing", nursingWField), nursingWField);
        VBox col3Box = createInspectorColumnBox("🧠 AI & Task Allocation", col3Grid);

        // Column 4: 🛡️ Armes & Venin
        GridPane col4Grid = createColumnGrid();
        col4Grid.addRow(0, createTooltipLabel("Weapons & Venom:", "Defensive weapons and chemical toxins equipped by caste (multiple selection supported)", casteVenomMenuButton), casteVenomMenuButton);
        col4Grid.addRow(1, createTooltipLabel("Venom Toxicity:", "Toxicity damage or effect per venom strike", casteVenomToxField), casteVenomToxField);
        col4Grid.addRow(2, createTooltipLabel("Explosive Autothysis:", "Suicidal explosive abdominal defense unique to this caste.", casteAutothysisCheck, "Autothysie"), casteAutothysisCheck);
        VBox col4Box = createInspectorColumnBox("🛡️ Weapons & Toxins", col4Grid);

        casteInspectorColumns.getChildren().addAll(col1Box, col2Box, col3Box, col4Box);

        HBox casteBtns = new HBox(10);
        Button btnAddCaste = new Button("Add / Update Caste", new FontIcon(Feather.PLUS_CIRCLE));
        btnAddCaste.getStyleClass().add("btn-primary");
        btnAddCaste.setOnAction(e -> {
            try {
                double body = Double.parseDouble(casteBodyF.getText());
                double head = Double.parseDouble(casteHeadF.getText());
                String selectedVenoms = getSelectedVenomTypes(casteVenomCheckItems);
                CasteRow sel = casteTable.getSelectionModel().getSelectedItem();
                if (sel != null && sel.getName().equalsIgnoreCase(casteNameF.getText())) {
                    sel.setBodyLengthMm(body);
                    sel.setHeadWidthMm(head);
                    sel.setLifespan(Integer.parseInt(casteLifeF.getText()));
                    sel.setHealth(Float.parseFloat(casteHealthF.getText()));
                    sel.setDamage(Float.parseFloat(casteDmgF.getText()));
                    sel.setCanFly(casteFlyCheck.isSelected());
                    sel.setTargetRatio(Float.parseFloat(targetRatioF.getText()));
                    sel.setDecisionArch(decisionArchCombo.getValue());
                    sel.setForagingWeight(Float.parseFloat(foragingWField.getText()));
                    sel.setDefenseWeight(Float.parseFloat(defenseWField.getText()));
                    sel.setExcavationWeight(Float.parseFloat(excavationWField.getText()));
                    sel.setNursingWeight(Float.parseFloat(nursingWField.getText()));
                    sel.setVenomType(selectedVenoms);
                    sel.setVenomToxicity(Float.parseFloat(casteVenomToxField.getText()));

                    sel.setWingbeatFrequencyHz(Float.parseFloat(casteWingbeatHzF.getText()));
                    sel.setHasHoveringCapability(casteHoverCheck.isSelected());
                    sel.setMaxPayloadRatio(Float.parseFloat(castePayloadRatioF.getText()));
                    sel.setBitingForceMpa(Float.parseFloat(casteBitingForceMpaF.getText()));
                    sel.setHasAutothysis(casteAutothysisCheck.isSelected());
                    sel.setHasAroliaAdhesion(casteAroliaCheck.isSelected());
                    casteTable.refresh();
                } else {
                    CasteRow row = new CasteRow(
                            casteNameF.getText(),
                            body,
                            head,
                            Integer.parseInt(casteLifeF.getText()),
                            Float.parseFloat(casteHealthF.getText()),
                            Float.parseFloat(casteDmgF.getText()),
                            casteFlyCheck.isSelected()
                    );
                    row.setTargetRatio(Float.parseFloat(targetRatioF.getText()));
                    row.setDecisionArch(decisionArchCombo.getValue());
                    row.setForagingWeight(Float.parseFloat(foragingWField.getText()));
                    row.setDefenseWeight(Float.parseFloat(defenseWField.getText()));
                    row.setExcavationWeight(Float.parseFloat(excavationWField.getText()));
                    row.setNursingWeight(Float.parseFloat(nursingWField.getText()));
                    row.setVenomType(selectedVenoms);
                    row.setVenomToxicity(Float.parseFloat(casteVenomToxField.getText()));

                    row.setWingbeatFrequencyHz(Float.parseFloat(casteWingbeatHzF.getText()));
                    row.setHasHoveringCapability(casteHoverCheck.isSelected());
                    row.setMaxPayloadRatio(Float.parseFloat(castePayloadRatioF.getText()));
                    row.setBitingForceMpa(Float.parseFloat(casteBitingForceMpaF.getText()));
                    row.setHasAutothysis(casteAutothysisCheck.isSelected());
                    row.setHasAroliaAdhesion(casteAroliaCheck.isSelected());
                    casteRows.add(row);
                }
            } catch (Exception ex) {
                org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.ERROR, "Invalid number format.").show();
            }
        });

        Button btnDelCaste = new Button("Delete Selected Caste", new FontIcon(Feather.TRASH_2));
        btnDelCaste.getStyleClass().add("btn-danger");
        btnDelCaste.setOnAction(e -> {
            CasteRow sel = casteTable.getSelectionModel().getSelectedItem();
            if (sel == null) {
                org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.WARNING, "Please select a caste from the table before deleting.").show();
                return;
            }
            Alert confirmAlert = org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this caste? This action cannot be undone.");
            confirmAlert.setTitle("Delete Caste Confirmation");
            confirmAlert.setHeaderText("Delete caste: " + sel.getName());
            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    casteRows.remove(sel);
                }
            });
        });

        casteBtns.getChildren().addAll(btnAddCaste, btnDelCaste);
        casteInspectorCard.getChildren().addAll(titleInspector, casteInspectorColumns, casteBtns);

        box.getChildren().addAll(infoLabel, casteTable, casteInspectorCard);
        return wrapScroll(box);
    }

    // --- Tab 5: Diet & Metabolism ---
    private ScrollPane createDietPane() {
        GridPane grid = createGrid();

        primaryDietCombo = new ComboBox<>(FXCollections.observableArrayList(
            "Fungus & Cultivated Mycelium",
            "Honeydew & Aphid Trophobiosis",
            "Insects & Meat Protein",
            "Omnivorous Polyphagous",
            "Seeds & Granivory Grains",
            "Sugars & Plant Nectar",
            "Wood & Cellulose Fibers"
        ));
        ComboBoxTooltipHelper.setupDescriptiveComboBox(primaryDietCombo, SpeciesEditorPane::getDietTitle, SpeciesEditorPane::getDietDescription);
        primaryDietCombo.getSelectionModel().select("Honeydew & Aphid Trophobiosis");

        secondaryDietCombo = new ComboBox<>(FXCollections.observableArrayList(
            "Fungus & Cultivated Mycelium",
            "Honeydew & Aphid Trophobiosis",
            "Insects & Meat Protein",
            "None (No Secondary Diet)",
            "Seeds & Granivory Grains",
            "Sugars & Plant Nectar",
            "Wood & Cellulose Fibers"
        ));
        ComboBoxTooltipHelper.setupDescriptiveComboBox(secondaryDietCombo, SpeciesEditorPane::getDietTitle, SpeciesEditorPane::getDietDescription);
        secondaryDietCombo.getSelectionModel().select("Insects & Meat Protein");

        foodConsumptionField = new TextField("0.5");
        waterReqField = new TextField("0.2");

        // Legacy / fallback fields now exposed in the Diet tab
        workerLifespanField = new TextField("6000");
        workerSpeedField = new TextField("0.5");
        flyCheckBox = new CheckBox("Workers capable of flight (Winged species)");
        viewDistanceField = new TextField("5.0");
        metabolismField = new TextField("1.0");
        strengthField = new TextField("5.0");

        grid.addRow(0, createTooltipLabel("Primary Diet Source:", "Primary trophic source consumed for metabolic energy of the colony.", primaryDietCombo, "trophallaxis"), primaryDietCombo);
        grid.addRow(1, createTooltipLabel("Secondary Diet Source:", "Complementary trophic source (e.g., protein intake during brood rearing).", secondaryDietCombo, "trophallaxis"), secondaryDietCombo);
        grid.addRow(2, createTooltipLabel("Metabolic Consumption (g/ind/day):", "Daily food mass consumed per adult individual.", foodConsumptionField, "metabolism"), foodConsumptionField);
        grid.addRow(3, createTooltipLabel("Water Requirement (mL/ind/day):", "Daily water volume needed for hydration and metabolism.", waterReqField, "water"), waterReqField);
        grid.addRow(4, createTooltipLabel("Worker Lifespan (days):", "Average lifespan of an adult worker outside caste-specific overrides.", workerLifespanField, "polymorphism"), workerLifespanField);
        grid.addRow(5, createTooltipLabel("Locomotion Speed (m/s):", "Standard surface movement speed of workers in meters per second.", workerSpeedField), workerSpeedField);
        grid.addRow(6, createTooltipLabel("Visual Detection Distance (cm):", "Visual perception radius for food resources and enemies in centimeters.", viewDistanceField), viewDistanceField);
        grid.addRow(7, createTooltipLabel("Worker Flight Capability:", "Check if workers of this species are winged and capable of flight (e.g. Honeybees, Wasps).", flyCheckBox, "flight"), flyCheckBox);
        grid.addRow(8, createTooltipLabel("Global Metabolic Factor (0.1-5.0):", "Multiplier for energy expenditure rate and reserve consumption.", metabolismField, "metabolism"), metabolismField);
        grid.addRow(9, createTooltipLabel("Physio-Muscular Strength (N):", "General physical strength index and mechanical resistance.", strengthField, "mandibule"), strengthField);

        return wrapScroll(grid);
    }

    // --- Tab 7: Nest & Behavior ---
    private ScrollPane createNestPane() {
        GridPane grid = createGrid();

        nestTypeCombo = new ComboBox<>(FXCollections.observableArrayList(
            "ARBOREAL_SILK_LEAF",
            "BAMBOO_STEM_NEST",
            "BIVOUAC_LIVING_NEST",
            "CARTON_NEST",
            "CATHEDRAL_MOUND",
            "MATURE",
            "MOUND",
            "PAPER_PEDUNCULATE",
            "SIMPLE",
            "SUBTERRANEAN_FUNGI_VAULT",
            "TREE",
            "WAX_COMB_HEXAGONAL",
            "WAX_POTS_CLUSTER"
        ));
        ComboBoxTooltipHelper.setupDescriptiveComboBox(nestTypeCombo, SpeciesEditorPane::getNestTypeTitle, SpeciesEditorPane::getNestTypeDescription);
        nestTypeCombo.getSelectionModel().select("MATURE");

        optTempField = new TextField("24.0");
        minTempField = new TextField("10.0");
        maxTempField = new TextField("38.0");

        aggressionSlider = new Slider(0.0, 1.0, 0.3);
        aggressionSlider.setShowTickLabels(true);
        aggressionSlider.setShowTickMarks(true);

        territorialitySlider = new Slider(0.0, 1.0, 0.5);
        territorialitySlider.setShowTickLabels(true);
        territorialitySlider.setShowTickMarks(true);

        optTempField.textProperty().addListener((obs, o, n) -> validateParameters());
        minTempField.textProperty().addListener((obs, o, n) -> validateParameters());
        maxTempField.textProperty().addListener((obs, o, n) -> validateParameters());

        Button btnGenerateSpeciesNest = new Button("📐 Generate & Preview Nest for this Species", new FontIcon(Feather.HOME));
        btnGenerateSpeciesNest.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");
        btnGenerateSpeciesNest.setOnAction(e -> {
            CustomSpecies s = buildSpeciesFromUI();
            if (onGenerateNestForSpeciesListener != null) {
                onGenerateNestForSpeciesListener.accept(s);
            } else {
                org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.INFORMATION, "Active species configured for nest generation: " + s.getCommonName()).show();
            }
        });

        grid.addRow(0, createTooltipLabel("Specific Nest Type (NestType):", "Selects the preferred geometric architecture constructed by this species (e.g. wax comb, cathedral, mound, silk)."), nestTypeCombo);
        grid.addRow(1, createTooltipLabel("Optimal Temperature (°C):", "Ideal internal nest temperature for brood incubation."), optTempField);
        grid.addRow(2, createTooltipLabel("Minimum Temperature (°C):", "Threshold below which individuals enter lethargy/torpor."), minTempField);
        grid.addRow(3, createTooltipLabel("Maximum Temperature (°C):", "Critical temperature threshold above which thermal shock and mortality occur."), maxTempField);
        grid.addRow(4, createTooltipLabel("Aggression Level:", "Behavioral propensity to engage in combat against other colonies."), aggressionSlider);
        grid.addRow(5, createTooltipLabel("Territoriality:", "Intensity of patrolling and exclusive defense of territory around the nest."), territorialitySlider);
        grid.addRow(6, createTooltipLabel("Synchronized Nest Generation:", "Opens the Nest Generator and pre-configures architecture, substrate, and chamber distribution for this species."), btnGenerateSpeciesNest);

        return wrapScroll(grid);
    }

    /**
     * Real-time parameter consistency validator.
     * Detects mismatches (e.g. Monogyne with multiple queens, missing King caste for termites, thermal range anomalies).
     */
    public List<String> validateParameters() {
        List<String> warnings = new ArrayList<>();
        if (warningBannerBox == null) return warnings;

        // 1. Monogyne vs Queen count
        String qMode = queenModeCombo != null ? queenModeCombo.getValue() : "MONOGYNE";
        int qCount = queenCountSpinner != null ? queenCountSpinner.getValue() : 1;
        if ("MONOGYNE".equals(qMode) && qCount > 1) {
            warnings.add("Monogyne Gynic Structure: only 1 reproductive queen is permitted per colony (current value: " + qCount + ").");
        }

        // 2. Has King vs King Caste in caste table
        boolean hasKing = hasKingCheckBox != null && hasKingCheckBox.isSelected();
        if (hasKing) {
            boolean hasKingCaste = casteRows.stream().anyMatch(r -> 
                r.getName().equalsIgnoreCase("Roi") || 
                r.getName().equalsIgnoreCase("King") || 
                r.getName().toLowerCase().contains("roi") ||
                r.getName().toLowerCase().contains("king")
            );
            if (!hasKingCaste) {
                warnings.add("Reproductive King enabled (Isoptera), but no 'King' caste is defined in the caste table.");
            }
        }

        // 3. Category non-eusocial warning
        org.swarmforge.core.species.SpeciesCategory cat = categoryCombo != null ? categoryCombo.getValue() : null;
        if (cat != null && cat != org.swarmforge.core.species.SpeciesCategory.EUSOCIAL_PRIMARY) {
            warnings.add("Catégorie " + cat.label + " selected. Note: prey, predators, and commensals are configured in the environment accessory fauna.");
        }

        // 4. Temperature consistency
        try {
            float optT = parseFloat(optTempField.getText(), 24.0f);
            float minT = parseFloat(minTempField.getText(), 10.0f);
            float maxT = parseFloat(maxTempField.getText(), 38.0f);
            if (minT >= maxT) {
                warnings.add("Invalid thermal range: minimum temperature (" + minT + "°C) must be lower than maximum (" + maxT + "°C).");
            } else if (optT < minT || optT > maxT) {
                warnings.add("Optimal temperature (" + optT + "°C) outside bounds [Min: " + minT + "°C, Max: " + maxT + "°C].");
            }
        } catch (Exception ignored) {}

        // 5. Male Caste warning check for Hymenoptera
        String insectType = insectTypeCombo != null ? insectTypeCombo.getValue() : "ANT";
        if (insectType != null && (insectType.equalsIgnoreCase("ANT") || insectType.equalsIgnoreCase("BEE") || insectType.equalsIgnoreCase("WASP"))) {
            boolean hasMaleCaste = casteRows.stream().anyMatch(r ->
                r.getName().toLowerCase().contains("mâle") ||
                r.getName().toLowerCase().contains("male") ||
                r.getName().toLowerCase().contains("drone") ||
                r.getName().toLowerCase().contains("bourdon")
            );
            if (!hasMaleCaste) {
                warnings.add("Nuptial Flight & Mating: No 'Reproductive Male' caste (Alate / Drone) is defined in the caste table.");
            }
        }

        // Update warning banner
        warningBannerBox.getChildren().clear();
        if (warnings.isEmpty()) {
            warningBannerBox.setVisible(false);
            warningBannerBox.setManaged(false);
        } else {
            warningBannerBox.setVisible(true);
            warningBannerBox.setManaged(true);

            Label titleLabel = new Label("⚠ Parameter Consistency Warnings:");
            titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #f59e0b; -fx-font-size: 13px;");
            warningBannerBox.getChildren().add(titleLabel);

            for (String w : warnings) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);

                Label wLbl = new Label("• " + w);
                wLbl.setStyle("-fx-text-fill: #fbbf24; -fx-font-size: 12px;");
                wLbl.setWrapText(true);

                if (w.contains("caste 'Roi'")) {
                    Button btnAddKing = new Button("➕ Add 'King' Caste (Isoptera)", new FontIcon(Feather.PLUS_CIRCLE));
                    btnAddKing.setStyle("-fx-background-color: #d97706; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
                    btnAddKing.setOnAction(e -> {
                        CasteRow roiRow = new CasteRow("Reproductive King", 8.0, 2.2, 15000, 150, 5, false);
                        casteRows.add(roiRow);
                        validateParameters();
                    });
                    row.getChildren().addAll(wLbl, btnAddKing);
                } else if (w.contains("caste 'Mâle Reproducteur'")) {
                    Button btnAddMale = new Button("➕ Add 'Reproductive Male' Caste", new FontIcon(Feather.PLUS_CIRCLE));
                    btnAddMale.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
                    btnAddMale.setOnAction(e -> {
                        CasteRow maleRow = new CasteRow("Reproductive Male (Alate)", 4.5, 1.1, 500, 45, 0, true);
                        casteRows.add(maleRow);
                        validateParameters();
                    });
                    row.getChildren().addAll(wLbl, btnAddMale);
                } else if (w.contains("Monogyne")) {
                    Button btnFixMonogyne = new Button("🔧 Set to 1 Queen", new FontIcon(Feather.CHECK));
                    btnFixMonogyne.setStyle("-fx-background-color: #d97706; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
                    btnFixMonogyne.setOnAction(e -> {
                        queenCountSpinner.getValueFactory().setValue(1);
                        validateParameters();
                    });
                    row.getChildren().addAll(wLbl, btnFixMonogyne);
                } else {
                    row.getChildren().add(wLbl);
                }

                warningBannerBox.getChildren().add(row);
            }
        }

        return warnings;
    }

    // --- Tab 7: Sensory Systems & Motor Biomechanics ---
    private ScrollPane createSensorsPane() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(15));

        // 1. Sensory Card
        VBox cardSensors = new VBox(10);
        cardSensors.getStyleClass().add("card-pane");
        Label titleSensors = new Label("📡 Sensory Systems & Environmental Perception (SI)");
        titleSensors.getStyleClass().add("card-title");

        GridPane gridSensors = createGrid();

        hasMagnetoreceptionCheckBox = new CheckBox("Magnetoreception (Earth Magnetic Field - Termites / Compass Ant)");
        magnetoSensField = new TextField("5.0");
        thermoSensField = new TextField("0.5");
        gasSensField = new TextField("400.0");
        visualAcuityField = new TextField("1.0");
        viewDistanceField = new TextField("5.0");
        minLightField = new TextField("0.05");

        hasVibrationSensingCheckBox = new CheckBox("Substrate Vibration Perception (Johnston Organ / Alarm Drumming)");
        hasVibrationSensingCheckBox.setSelected(true);
        vibrationSensField = new TextField("10.0");

        hasHygroreceptionCheckBox = new CheckBox("Hygroreception (Relative Humidity Gradient Sensing)");
        hasHygroreceptionCheckBox.setSelected(true);
        hygroSensField = new TextField("2.0");

        hasElectrosensingCheckBox = new CheckBox("Electroreception (Atmospheric & Floral Electrostatic Charges - Bees / Wasps)");
        electroSensField = new TextField("50.0");

        hasPolarizedLightCheckBox = new CheckBox("UV Polarized Light Navigation (Celestial Compass - Bees / Cataglyphis)");

        gridSensors.addRow(0, createTooltipLabel("Magnetoreception (Earth Field):", "Ability to perceive Earth's magnetic field lines for navigation.", hasMagnetoreceptionCheckBox, "Magnetoreception"), hasMagnetoreceptionCheckBox);
        gridSensors.addRow(1, createTooltipLabel("Magnetic Field Threshold (µT):", "Minimum sensitivity of magnetic perception in micro-Teslas.", magnetoSensField), magnetoSensField);
        gridSensors.addRow(2, createTooltipLabel("Thermal Sensitivity (Δ°C/mm):", "Ability to detect temperature gradients for thermoregulation.", thermoSensField), thermoSensField);
        gridSensors.addRow(3, createTooltipLabel("CO₂ Threshold (ppm):", "CO₂ perception threshold for nest ventilation control in ppm.", gasSensField), gasSensField);
        gridSensors.addRow(4, createTooltipLabel("Visual Acuity (0-1):", "Relative visual resolution provided by compound eye ommatidia.", visualAcuityField), visualAcuityField);
        gridSensors.addRow(5, createTooltipLabel("Visual Perception Radius (cm):", "Maximum visual perception range for objects, threats, and food.", viewDistanceField), viewDistanceField);
        gridSensors.addRow(6, createTooltipLabel("Minimum Light Threshold (lux):", "Minimum light level enabling nocturnal or crepuscular vision.", minLightField), minLightField);
        gridSensors.addRow(7, createTooltipLabel("Substrate Vibration Sensing (Subgenual):", "Sensitivity to seismic and mechanical vibrations transmitted through soil.", hasVibrationSensingCheckBox, "Subgenual"), hasVibrationSensingCheckBox);
        gridSensors.addRow(8, createTooltipLabel("Substrate Vibration Threshold (dB):", "Minimum vibration intensity measurable by subgenual organ in decibels.", vibrationSensField), vibrationSensField);
        gridSensors.addRow(9, createTooltipLabel("Hygroreception (Relative Humidity):", "Ability to perceive atmospheric and soil moisture gradients.", hasHygroreceptionCheckBox), hasHygroreceptionCheckBox);
        gridSensors.addRow(10, createTooltipLabel("Humidity Sensitivity (%):", "Minimum humidity gradient detectable by antennal receptors in %.", hygroSensField), hygroSensField);
        gridSensors.addRow(11, createTooltipLabel("Electrostatic Field Perception:", "Sensitivity to atmospheric and floral electrostatic charges.", hasElectrosensingCheckBox), hasElectrosensingCheckBox);
        gridSensors.addRow(12, createTooltipLabel("Atmospheric Electric Threshold (V/m):", "Electric field perception threshold in Volts per meter.", electroSensField), electroSensField);
        gridSensors.addRow(13, createTooltipLabel("UV Polarized Light Compass:", "Use of skylight UV polarization patterns for path integration navigation.", hasPolarizedLightCheckBox, "UV"), hasPolarizedLightCheckBox);

        // Conditional field disabling: gray out sensitivity fields when the capability is absent
        magnetoSensField.disableProperty().bind(hasMagnetoreceptionCheckBox.selectedProperty().not());
        vibrationSensField.disableProperty().bind(hasVibrationSensingCheckBox.selectedProperty().not());
        hygroSensField.disableProperty().bind(hasHygroreceptionCheckBox.selectedProperty().not());
        electroSensField.disableProperty().bind(hasElectrosensingCheckBox.selectedProperty().not());

        cardSensors.getChildren().addAll(titleSensors, gridSensors);

        // Dynamic Plugin Extensibility Card
        VBox cardPlugins = new VBox(10);
        cardPlugins.getStyleClass().add("card-pane");
        Label titlePlugins = new Label("🔌 Extension Attributes & Plugins (Dynamic Extensibility)");
        titlePlugins.getStyleClass().add("card-title");

        GridPane gridPlugins = createGrid();
        TextField customKeyField = new TextField();
        customKeyField.setPromptText("Parameter Key (e.g. bioluminescence)");
        TextField customValueField = new TextField();
        customValueField.setPromptText("Value (e.g. 1.5 or true)");

        Button btnAddCustomAttr = new Button("Add Plugin Attribute");
        btnAddCustomAttr.getStyleClass().add("btn-secondary");
        btnAddCustomAttr.setOnAction(e -> {
            String k = customKeyField.getText().trim();
            String v = customValueField.getText().trim();
            if (!k.isEmpty()) {
                org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.INFORMATION, "Extension attribute '" + k + "' registered for active species.").show();
            }
        });

        gridPlugins.addRow(0, createWhiteLabel("Plugin Parameter Key:"), customKeyField);
        gridPlugins.addRow(1, createWhiteLabel("Value:"), customValueField);
        gridPlugins.addRow(2, new Label(), btnAddCustomAttr);

        cardPlugins.getChildren().addAll(titlePlugins, gridPlugins);

        box.getChildren().addAll(cardSensors, cardPlugins);
        return wrapScroll(box);
    }

    // --- Helper Methods ---

    private void handleAddPreset() {
        String editedText = presetCombo.getEditor() != null ? presetCombo.getEditor().getText().trim() : "";
        String common = commonNameField != null ? commonNameField.getText().trim() : "";
        String scientific = scientificNameField != null ? scientificNameField.getText().trim() : "";

        String name = editedText;
        if (name.isEmpty()) {
            if (!common.isEmpty() && !scientific.isEmpty()) {
                name = common + " (" + scientific + ")";
            } else if (!common.isEmpty()) {
                name = common;
            } else if (!scientific.isEmpty()) {
                name = scientific;
            }
        }

        if (name.isEmpty()) {
            org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.WARNING, "Please specify a common name, scientific name, or preset name.").show();
            return;
        }

        if (presetManager.contains(name)) {
            Alert confirmAlert = org.swarmforge.client.util.ThemeManager.createAlert(
                Alert.AlertType.CONFIRMATION,
                "Species preset '" + name + "' already exists.\n\nDo you want to replace it with current configuration?"
            );
            confirmAlert.setTitle("Replace Existing Preset");
            confirmAlert.setHeaderText("Replacement Confirmation");
            java.util.Optional<ButtonType> res = confirmAlert.showAndWait();
            if (res.isEmpty() || res.get() != ButtonType.OK) {
                return;
            }
        }

        CustomSpecies species = buildSpeciesFromUI();
        species.setPresetName(name);

        presetManager.addPreset(name, species);

        isUpdatingFields = true;
        try {
            if (!presetCombo.getItems().contains(name)) {
                presetCombo.getItems().add(name);
            }
            presetCombo.getSelectionModel().select(name);
        } finally {
            isUpdatingFields = false;
        }
        lastSelectedPreset = name;
        isDirty = false;

        NotificationOverlay.show(this, "Species preset '" + name + "' saved.", NotificationOverlay.NotificationType.SUCCESS);
    }

    private void handleDeletePreset() {
        String selected = presetCombo.getValue();
        if (selected == null || selected.isEmpty()) return;

        I18nManager i18n = I18nManager.getInstance();
        Alert confirmAlert = org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.CONFIRMATION, String.format(i18n.get("preset.delete.confirm"), selected));
        confirmAlert.setTitle(i18n.get("preset.delete.title"));
        confirmAlert.setHeaderText("Delete Species");

        confirmAlert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                presetManager.delete(selected);
                presetCombo.getItems().setAll(presetManager.getPresetNames());
                if (!presetCombo.getItems().isEmpty()) {
                    presetCombo.getSelectionModel().selectFirst();
                } else {
                    presetCombo.getSelectionModel().clearSelection();
                }
                NotificationOverlay.show(this, "Species preset deleted.", NotificationOverlay.NotificationType.INFO);
            }
        });
    }

    private void handleSaveDisk() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Species");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        chooser.setInitialFileName("species_" + commonNameField.getText().replaceAll("\\s+", "_") + ".json");

        File f = chooser.showSaveDialog(getScene().getWindow());
        if (f != null) {
            try {
                CustomSpecies species = buildSpeciesFromUI();
                presetManager.saveToFile(f, species);
                org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.INFORMATION, "Species successfully saved as " + f.getName()).show();
            } catch (Exception ex) {
                org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.ERROR, "Save Error: " + ex.getMessage()).show();
            }
        }
    }

    private void handleLoadDisk() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Load Species");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File f = chooser.showOpenDialog(getScene().getWindow());
        if (f != null) {
            try {
                CustomSpecies species = presetManager.loadFromFile(f);
                loadPresetToUI(species);
                org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.INFORMATION, "Espèce '" + species.getCommonName() + "' chargée avec succès!").show();
            } catch (Exception ex) {
                org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.ERROR, "Load Error: " + ex.getMessage()).show();
            }
        }
    }

    private void onFieldEdited() {
        if (isUpdatingFields) return;
        isDirty = true;
        if (presetCombo != null && presetCombo.getSelectionModel().getSelectedItem() != null) {
            isUpdatingFields = true;
            try {
                presetCombo.getSelectionModel().clearSelection();
            } finally {
                isUpdatingFields = false;
            }
        }
    }

    private void attachUserChangeListeners() {
        TextField[] fields = {
            commonNameField, scientificNameField, genusField, queenLifespanField, queenEggRateField,
            kingLifespanField, eggDurationField, larvaDurationField, pupaDurationField,
            proteinMinorField, proteinMajorField, proteinSoldierField, proteinQueenField,
            foodConsumptionField, waterReqField, workerLifespanField, workerSpeedField,
            viewDistanceField, colonySizeField, optTempField, minTempField, maxTempField,
            magnetoSensField, thermoSensField, gasSensField, visualAcuityField, minLightField,
            vibrationSensField, hygroSensField, electroSensField, wingbeatHzField,
            maxPayloadRatioField, bitingForceMpaField, metabolismField, strengthField
        };
        for (TextField tf : fields) {
            if (tf != null) tf.textProperty().addListener((obs, oldV, newV) -> onFieldEdited());
        }

        ComboBox<?>[] combos = {
            insectTypeCombo, categoryCombo, queenModeCombo, nuptialFlightCombo,
            larvaDietCombo, primaryDietCombo, secondaryDietCombo, nestTypeCombo
        };
        for (ComboBox<?> cb : combos) {
            if (cb != null) cb.valueProperty().addListener((obs, oldV, newV) -> onFieldEdited());
        }

        CheckBox[] checkBoxes = {
            hasKingCheckBox, haplodiploidyCheckBox, megaColonyCheckBox, flyCheckBox, hasMagnetoreceptionCheckBox,
            hasVibrationSensingCheckBox, hasHygroreceptionCheckBox, hasElectrosensingCheckBox,
            hasPolarizedLightCheckBox, hasHoveringCheckBox, hasAutothysisCheckBox,
            hasAroliaAdhesionCheckBox
        };
        for (CheckBox chk : checkBoxes) {
            if (chk != null) chk.selectedProperty().addListener((obs, oldV, newV) -> onFieldEdited());
        }

        Slider[] sliders = { aggressionSlider, territorialitySlider, pheroInhibSlider, pathogenResistanceSlider, groomingSlider };
        for (Slider s : sliders) {
            if (s != null) s.valueProperty().addListener((obs, oldV, newV) -> onFieldEdited());
        }

        if (queenCountSpinner != null) {
            queenCountSpinner.valueProperty().addListener((obs, oldV, newV) -> onFieldEdited());
        }
        if (descriptionArea != null) {
            descriptionArea.textProperty().addListener((obs, oldV, newV) -> onFieldEdited());
        }
    }

    private void loadPresetToUI(CustomSpecies s) {
        if (s == null) return;
        isUpdatingFields = true;
        try {
            commonNameField.setText(s.getCommonName());
            scientificNameField.setText(s.getScientificName());
            if (genusField != null) genusField.setText(s.getGenus());
            selectComboValue(insectTypeCombo, s.getInsectType());
            categoryCombo.getSelectionModel().select(s.getCategory());
            descriptionArea.setText(s.getDescription());

            selectComboValue(queenModeCombo, s.getQueenCountMode());
            queenCountSpinner.getValueFactory().setValue(s.getQueenCount());
            queenLifespanField.setText(String.valueOf(s.getQueenLifespan()));
            queenEggRateField.setText(String.valueOf(s.getQueenEggLayingRate()));
            hasKingCheckBox.setSelected(s.isHasKing());
            kingLifespanField.setText(String.valueOf(s.getKingLifespan()));
            selectComboValue(nuptialFlightCombo, s.getNuptialFlightType());

            eggDurationField.setText(String.valueOf(s.getEggStageDuration()));
            larvaDurationField.setText(String.valueOf(s.getLarvaStageDuration()));
            selectComboValue(larvaDietCombo, s.getLarvaDietRequirement());
            pupaDurationField.setText(String.valueOf(s.getPupaStageDuration()));
            if (proteinMinorField != null) proteinMinorField.setText(String.format(java.util.Locale.US, "%.2f", s.getProteinThresholdMinor()));
            if (proteinMajorField != null) proteinMajorField.setText(String.format(java.util.Locale.US, "%.2f", s.getProteinThresholdMajor()));
            if (proteinSoldierField != null) proteinSoldierField.setText(String.format(java.util.Locale.US, "%.2f", s.getProteinThresholdSoldier()));
            if (proteinQueenField != null) proteinQueenField.setText(String.format(java.util.Locale.US, "%.2f", s.getProteinThresholdQueen()));
            if (pheroInhibSlider != null) pheroInhibSlider.setValue(s.getQueenPheromoneInhibitionFactor());
            if (haplodiploidyCheckBox != null) haplodiploidyCheckBox.setSelected(s.isHaplodiploidyEnabled());
            if (pathogenResistanceSlider != null) pathogenResistanceSlider.setValue(s.getPathogenResistance());
            if (groomingSlider != null) groomingSlider.setValue(s.getGroomingDefenseEfficacy());

            selectComboValue(primaryDietCombo, s.getPrimaryDiet());
            selectComboValue(secondaryDietCombo, s.getSecondaryDiet());
            foodConsumptionField.setText(String.valueOf(s.getDailyFoodConsumption()));
            waterReqField.setText(String.valueOf(s.getWaterRequirement()));
            workerLifespanField.setText(String.valueOf(s.getWorkerLifespan()));
            workerSpeedField.setText(String.valueOf(s.getWorkerSpeed()));
            viewDistanceField.setText(String.valueOf(s.getViewDistance()));
            colonySizeField.setText(String.valueOf(s.getTypicalColonySize()));
            megaColonyCheckBox.setSelected(s.formsMegaColonies());
            flyCheckBox.setSelected(s.isWorkersCanFly());
            if (metabolismField != null) metabolismField.setText(String.valueOf(s.getMetabolism()));
            if (strengthField != null) strengthField.setText(String.valueOf(s.getStrength()));

            nestTypeCombo.getSelectionModel().select(s.getNestType());
            optTempField.setText(String.valueOf(s.getOptimalTempCelsius()));
            minTempField.setText(String.valueOf(s.getMinTempCelsius()));
            maxTempField.setText(String.valueOf(s.getMaxTempCelsius()));
            aggressionSlider.setValue(s.getAggression());
            territorialitySlider.setValue(s.getTerritoriality());

            // Sensory & Perception Profile
            hasMagnetoreceptionCheckBox.setSelected(s.hasMagnetoreception());
            magnetoSensField.setText(String.valueOf(s.getMagnetoreceptionSensitivity()));
            thermoSensField.setText(String.valueOf(s.getThermoreceptionSensitivity()));
            gasSensField.setText(String.valueOf(s.getGasSensitivityCo2Ppm()));
            visualAcuityField.setText(String.valueOf(s.getVisualAcuity()));
            minLightField.setText(String.valueOf(s.getMinLightLevelThreshold()));
            hasVibrationSensingCheckBox.setSelected(s.hasSubstrateVibrationSensing());
            vibrationSensField.setText(String.valueOf(s.getVibrationSensitivityDb()));
            hasHygroreceptionCheckBox.setSelected(s.hasHygroreception());
            hygroSensField.setText(String.valueOf(s.getHygroreceptionSensitivityPercent()));
            hasElectrosensingCheckBox.setSelected(s.hasElectrosensing());
            electroSensField.setText(String.valueOf(s.getElectroceptionSensitivityVolts()));
            hasPolarizedLightCheckBox.setSelected(s.hasPolarizedLightNavigation());

            // Motor & Biomechanical Profile
            wingbeatHzField.setText(String.valueOf(s.getWingbeatFrequencyHz()));
            hasHoveringCheckBox.setSelected(s.hasHoveringCapability());
            maxPayloadRatioField.setText(String.valueOf(s.getMaxCarryingPayloadRatio()));
            bitingForceMpaField.setText(String.valueOf(s.getMandibularBitingForceMPa()));
            hasAutothysisCheckBox.setSelected(s.hasAutothysis());
            hasAroliaAdhesionCheckBox.setSelected(s.hasSubstrateAdhesionArolia());

            // Castes
            casteRows.clear();
            if (s.getCasteTemplates() != null) {
                for (CasteTemplate ct : s.getCasteTemplates()) {
                    double body = ct.getBodyLengthMm() > 0 ? ct.getBodyLengthMm() : ct.getAttribute("size_mm", 5.0f);
                    double head = ct.getHeadWidthMm() > 0 ? ct.getHeadWidthMm() : (body * 0.25);
                    CasteRow row = new CasteRow(ct.getName(), body, head, ct.getLifespan(), ct.getBaseHealth(), ct.getBaseDamage(), ct.isCanFly());
                    row.setForagingWeight(ct.getTaskForagingWeight());
                    row.setDefenseWeight(ct.getTaskDefenseWeight());
                    row.setExcavationWeight(ct.getTaskExcavationWeight());
                    row.setNursingWeight(ct.getTaskNursingWeight());
                    row.setQueenCareWeight(ct.getTaskQueenCareWeight());
                    row.setSanitationWeight(ct.getTaskSanitationWeight());
                    row.setTargetRatio(ct.getTargetRatio());
                    row.setDecisionArch(ct.getDecisionArchitectureType() != null ? ct.getDecisionArchitectureType() : "BDI");
                    row.setVenomType(ct.getVenomType() != null ? ct.getVenomType() : "NONE");
                    row.setVenomToxicity(ct.getVenomToxicity());
                    row.setVenomRangeMm(ct.getVenomRangeMm());

                    row.setWingbeatFrequencyHz(ct.getWingbeatFrequencyHz() >= 0.0f ? ct.getWingbeatFrequencyHz() : (ct.isCanFly() ? s.getWingbeatFrequencyHz() : 0.0f));
                    row.setHasHoveringCapability(ct.getHasHoveringCapability() != null ? ct.getHasHoveringCapability() : (ct.isCanFly() && s.hasHoveringCapability()));
                    row.setMaxPayloadRatio(ct.getMaxCarryingPayloadRatio() >= 0.0f ? ct.getMaxCarryingPayloadRatio() : s.getMaxCarryingPayloadRatio());
                    row.setBitingForceMpa(ct.getMandibularBitingForceMPa() >= 0.0f ? ct.getMandibularBitingForceMPa() : (float) Math.max(1.0, Math.round(s.getMandibularBitingForceMPa() * (head / 1.5) * 10.0) / 10.0));
                    row.setHasAutothysis(ct.getHasAutothysis() != null ? ct.getHasAutothysis() : s.hasAutothysis());
                    row.setHasAroliaAdhesion(ct.getHasSubstrateAdhesionArolia() != null ? ct.getHasSubstrateAdhesionArolia() : s.hasSubstrateAdhesionArolia());
                    casteRows.add(row);
                }
            }
            if (casteTable != null && !casteRows.isEmpty()) {
                casteTable.getSelectionModel().selectFirst();
            }
            validateParameters();
        } finally {
            isUpdatingFields = false;
            isDirty = false;
        }
    }

    private CustomSpecies buildSpeciesFromUI() {
        CustomSpecies s = new CustomSpecies();
        s.setPresetName(commonNameField != null ? commonNameField.getText().trim() : "");

        s.setCommonName(commonNameField.getText());
        s.setScientificName(scientificNameField.getText());
        s.setGenus(genusField != null ? genusField.getText().trim() : "");
        s.setInsectType(mapReadableToTechnical(insectTypeCombo.getValue()));
        s.setCategory(categoryCombo.getValue() != null ? categoryCombo.getValue() : org.swarmforge.core.species.SpeciesCategory.EUSOCIAL_PRIMARY);
        s.setDescription(descriptionArea.getText());

        s.setQueenCountMode(mapReadableToTechnical(queenModeCombo.getValue()));
        s.setQueenCount(queenCountSpinner.getValue());
        s.setQueenLifespan(parseInt(queenLifespanField.getText(), 25000));
        s.setQueenEggLayingRate(parseFloat(queenEggRateField.getText(), 15.0f));
        s.setHasKing(hasKingCheckBox.isSelected());
        s.setKingLifespan(parseInt(kingLifespanField.getText(), 15000));
        s.setNuptialFlightType(mapReadableToTechnical(nuptialFlightCombo.getValue()));

        s.setEggStageDuration(parseInt(eggDurationField.getText(), 300));
        s.setLarvaStageDuration(parseInt(larvaDurationField.getText(), 600));
        s.setLarvaDietRequirement(mapReadableToTechnical(larvaDietCombo.getValue()));
        s.setPupaStageDuration(parseInt(pupaDurationField.getText(), 500));
        if (proteinMinorField != null) s.setProteinThresholdMinor(parseFloat(proteinMinorField.getText(), 0.35f));
        if (proteinMajorField != null) s.setProteinThresholdMajor(parseFloat(proteinMajorField.getText(), 0.70f));
        if (proteinSoldierField != null) s.setProteinThresholdSoldier(parseFloat(proteinSoldierField.getText(), 0.85f));
        if (proteinQueenField != null) s.setProteinThresholdQueen(parseFloat(proteinQueenField.getText(), 0.95f));
        if (pheroInhibSlider != null) s.setQueenPheromoneInhibitionFactor((float) pheroInhibSlider.getValue());
        if (haplodiploidyCheckBox != null) s.setHaplodiploidyEnabled(haplodiploidyCheckBox.isSelected());
        if (pathogenResistanceSlider != null) s.setPathogenResistance((float) pathogenResistanceSlider.getValue());
        if (groomingSlider != null) s.setGroomingDefenseEfficacy((float) groomingSlider.getValue());

        s.setPrimaryDiet(mapReadableToTechnical(primaryDietCombo.getValue()));
        s.setSecondaryDiet(mapReadableToTechnical(secondaryDietCombo.getValue()));
        s.setDailyFoodConsumption(parseFloat(foodConsumptionField.getText(), 0.5f));
        s.setWaterRequirement(parseFloat(waterReqField.getText(), 0.2f));
        s.setWorkerLifespan(parseInt(workerLifespanField.getText(), 5000));
        s.setWorkerSpeed(parseFloat(workerSpeedField.getText(), 0.5f));
        s.setViewDistance(parseFloat(viewDistanceField.getText(), 5.0f));
        s.setTypicalColonySize(parseInt(colonySizeField.getText(), 1000));
        s.setFormsMegaColonies(megaColonyCheckBox.isSelected());
        s.setWorkersCanFly(flyCheckBox.isSelected());
        if (metabolismField != null) s.setMetabolism(parseFloat(metabolismField.getText(), 1.0f));
        if (strengthField != null) s.setStrength(parseFloat(strengthField.getText(), 5.0f));

        s.setNestType(nestTypeCombo.getValue());
        s.setOptimalTempCelsius(parseFloat(optTempField.getText(), 24.0f));
        s.setMinTempCelsius(parseFloat(minTempField.getText(), 10.0f));
        s.setMaxTempCelsius(parseFloat(maxTempField.getText(), 38.0f));
        s.setAggression((float) aggressionSlider.getValue());
        s.setTerritoriality((float) territorialitySlider.getValue());
        if (!casteRows.isEmpty()) {
            s.setVenomType(casteRows.get(0).getVenomType());
        } else {
            s.setVenomType("NONE");
        }

        // Sensory Profile
        s.setHasMagnetoreception(hasMagnetoreceptionCheckBox.isSelected());
        s.setMagnetoreceptionSensitivity(parseFloat(magnetoSensField.getText(), 5.0f));
        s.setThermoreceptionSensitivity(parseFloat(thermoSensField.getText(), 0.5f));
        s.setGasSensitivityCo2Ppm(parseFloat(gasSensField.getText(), 400.0f));
        s.setVisualAcuity(parseFloat(visualAcuityField.getText(), 1.0f));
        s.setMinLightLevelThreshold(parseFloat(minLightField.getText(), 0.05f));
        s.setHasSubstrateVibrationSensing(hasVibrationSensingCheckBox.isSelected());
        s.setVibrationSensitivityDb(parseFloat(vibrationSensField.getText(), 10.0f));
        s.setHasHygroreception(hasHygroreceptionCheckBox.isSelected());
        s.setHygroreceptionSensitivityPercent(parseFloat(hygroSensField.getText(), 2.0f));
        s.setHasElectrosensing(hasElectrosensingCheckBox.isSelected());
        s.setElectroceptionSensitivityVolts(parseFloat(electroSensField.getText(), 50.0f));
        s.setHasPolarizedLightNavigation(hasPolarizedLightCheckBox.isSelected());

        // Motor & Biomechanical Profile
        s.setWingbeatFrequencyHz(parseFloat(wingbeatHzField.getText(), 200.0f));
        s.setHasHoveringCapability(hasHoveringCheckBox.isSelected());
        s.setMaxCarryingPayloadRatio(parseFloat(maxPayloadRatioField.getText(), 5.0f));
        s.setMandibularBitingForceMPa(parseFloat(bitingForceMpaField.getText(), 15.0f));
        s.setHasAutothysis(hasAutothysisCheckBox.isSelected());
        s.setHasSubstrateAdhesionArolia(hasAroliaAdhesionCheckBox.isSelected());

        // Castes
        List<CasteTemplate> templates = new ArrayList<>();
        for (CasteRow r : casteRows) {
            CasteTemplate ct = new CasteTemplate(r.getName(), r.getHealth(), r.getDamage());
            ct.setLifespan(r.getLifespan());
            ct.setCanFly(r.isCanFly());
            ct.setBodyLengthMm((float) r.getBodyLengthMm());
            ct.setHeadWidthMm((float) r.getHeadWidthMm());
            ct.setTaskForagingWeight(r.getForagingWeight());
            ct.setTaskDefenseWeight(r.getDefenseWeight());
            ct.setTaskExcavationWeight(r.getExcavationWeight());
            ct.setTaskNursingWeight(r.getNursingWeight());
            ct.setTaskQueenCareWeight(r.getQueenCareWeight());
            ct.setTaskSanitationWeight(r.getSanitationWeight());
            ct.setTargetRatio(r.getTargetRatio());
            ct.setDecisionArchitectureType(r.getDecisionArch());
            ct.setVenomType(r.getVenomType());
            ct.setVenomToxicity(r.getVenomToxicity());
            ct.setVenomRangeMm(r.getVenomRangeMm());

            ct.setWingbeatFrequencyHz(r.getWingbeatFrequencyHz());
            ct.setHasHoveringCapability(r.isHasHoveringCapability());
            ct.setMaxCarryingPayloadRatio(r.getMaxPayloadRatio());
            ct.setMandibularBitingForceMPa(r.getBitingForceMpa());
            ct.setHasAutothysis(r.isHasAutothysis());
            ct.setHasSubstrateAdhesionArolia(r.isHasAroliaAdhesion());
            templates.add(ct);
        }
        s.setCasteTemplates(templates);

        return s;
    }

    private int parseInt(String s, int fallback) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return fallback; }
    }

    private float parseFloat(String s, float fallback) {
        try { return Float.parseFloat(s.trim()); } catch (Exception e) { return fallback; }
    }

    private Label createTooltipLabel(String text, String tooltipText) {
        return createTooltipLabel(text, tooltipText, (javafx.scene.Node) null, null);
    }

    private Label createTooltipLabel(String text, String tooltipText, javafx.scene.Node targetControl) {
        return createTooltipLabel(text, tooltipText, targetControl, null);
    }

    private Label createTooltipLabel(String text, String tooltipText, String glossaryTerm) {
        return createTooltipLabel(text, tooltipText, null, glossaryTerm);
    }

    private Label createTooltipLabel(String text, String tooltipText, javafx.scene.Node targetControl, String glossaryTerm) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold;");
        if (tooltipText != null && !tooltipText.isEmpty()) {
            Tooltip tt = new Tooltip(tooltipText);
            tt.setStyle("-fx-font-size: 12px; -fx-max-width: 380px; -fx-wrap-text: true;");
            l.setTooltip(tt);
            if (targetControl != null) {
                if (targetControl instanceof Control) {
                    ((Control) targetControl).setTooltip(tt);
                } else {
                    Tooltip.install(targetControl, tt);
                }
            }
        }
        if (glossaryTerm != null && !glossaryTerm.isEmpty()) {
            l.getStyleClass().add("glossary-link");
            l.setCursor(javafx.scene.Cursor.HAND);
            l.setOnMouseClicked(e -> GlossaryDialog.show(glossaryTerm));
        } else {
            l.getStyleClass().add("bold-label");
        }
        return l;
    }

    private Label createWhiteLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("bold-label");
        return l;
    }

    private void showGlossaryDialog() {
        org.swarmforge.client.ui.GlossaryDialog.show();
    }

    private javafx.scene.Node createGlossaryPane() {
        I18nManager i18n = I18nManager.getInstance();

        TabPane subTabPane = new TabPane();
        subTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        subTabPane.setStyle("-fx-background-color: transparent;");

        // Tab 1: Nest Architectures
        VBox vNest = new VBox(10); vNest.setPadding(new Insets(15));
        addGlossaryEntry(vNest, i18n.get("glossary.nest.wax_comb.title"), i18n.get("glossary.nest.wax_comb.desc"));
        addGlossaryEntry(vNest, i18n.get("glossary.nest.wax_pots.title"), i18n.get("glossary.nest.wax_pots.desc"));
        addGlossaryEntry(vNest, i18n.get("glossary.nest.paper_pedunculate.title"), i18n.get("glossary.nest.paper_pedunculate.desc"));
        addGlossaryEntry(vNest, i18n.get("glossary.nest.cathedral.title"), i18n.get("glossary.nest.cathedral.desc"));
        addGlossaryEntry(vNest, i18n.get("glossary.nest.arboreal_silk.title"), i18n.get("glossary.nest.arboreal_silk.desc"));
        addGlossaryEntry(vNest, i18n.get("glossary.nest.fungi_vault.title"), i18n.get("glossary.nest.fungi_vault.desc"));
        addGlossaryEntry(vNest, i18n.get("glossary.nest.carton.title"), i18n.get("glossary.nest.carton.desc"));
        addGlossaryEntry(vNest, i18n.get("glossary.nest.bamboo.title"), i18n.get("glossary.nest.bamboo.desc"));
        addGlossaryEntry(vNest, i18n.get("glossary.nest.bivouac.title"), i18n.get("glossary.nest.bivouac.desc"));
        addGlossaryEntry(vNest, i18n.get("glossary.nest.mound.title"), i18n.get("glossary.nest.mound.desc"));
        addGlossaryEntry(vNest, i18n.get("glossary.nest.wood.title"), i18n.get("glossary.nest.wood.desc"));
        addGlossaryEntry(vNest, i18n.get("glossary.nest.subterranean.title"), i18n.get("glossary.nest.subterranean.desc"));

        // Tab 2: Queens & Sociality
        VBox vSocial = new VBox(10); vSocial.setPadding(new Insets(15));
        addGlossaryEntry(vSocial, i18n.get("glossary.social.queen_mode.title"), i18n.get("glossary.social.queen_mode.desc"));
        addGlossaryEntry(vSocial, i18n.get("glossary.social.king.title"), i18n.get("glossary.social.king.desc"));
        addGlossaryEntry(vSocial, i18n.get("glossary.social.nuptial.title"), i18n.get("glossary.social.nuptial.desc"));
        addGlossaryEntry(vSocial, i18n.get("glossary.social.inhibition.title"), i18n.get("glossary.social.inhibition.desc"));

        // Tab 3: Environment & Soil
        VBox vEnv = new VBox(10); vEnv.setPadding(new Insets(15));
        addGlossaryEntry(vEnv, i18n.get("glossary.env.moisture.title"), i18n.get("glossary.env.moisture.desc"));
        addGlossaryEntry(vEnv, i18n.get("glossary.env.temperature.title"), i18n.get("glossary.env.temperature.desc"));
        addGlossaryEntry(vEnv, i18n.get("glossary.env.co2.title"), i18n.get("glossary.env.co2.desc"));
        addGlossaryEntry(vEnv, i18n.get("glossary.env.solar.title"), i18n.get("glossary.env.solar.desc"));
        addGlossaryEntry(vEnv, i18n.get("glossary.env.magnetic.title"), i18n.get("glossary.env.magnetic.desc"));
        addGlossaryEntry(vEnv, i18n.get("glossary.env.soil_layers.title"), i18n.get("glossary.env.soil_layers.desc"));

        // Tab 4: Behavioral Reasoning Engines
        VBox vReasoning = new VBox(10); vReasoning.setPadding(new Insets(15));
        addGlossaryEntry(vReasoning, i18n.get("glossary.reasoning.fsm.title"), i18n.get("glossary.reasoning.fsm.desc"));
        addGlossaryEntry(vReasoning, i18n.get("glossary.reasoning.fuzzy.title"), i18n.get("glossary.reasoning.fuzzy.desc"));
        addGlossaryEntry(vReasoning, i18n.get("glossary.reasoning.bdi.title"), i18n.get("glossary.reasoning.bdi.desc"));
        addGlossaryEntry(vReasoning, i18n.get("glossary.reasoning.nn.title"), i18n.get("glossary.reasoning.nn.desc"));
        addGlossaryEntry(vReasoning, i18n.get("glossary.reasoning.blackboard.title"), i18n.get("glossary.reasoning.blackboard.desc"));
        addGlossaryEntry(vReasoning, i18n.get("glossary.reasoning.bulk.title"), i18n.get("glossary.reasoning.bulk.desc"));

        // Tab 5: Sensors & Biomechanics
        VBox vSensors = new VBox(10); vSensors.setPadding(new Insets(15));
        addGlossaryEntry(vSensors, i18n.get("glossary.biomech.subgenual.title"), i18n.get("glossary.biomech.subgenual.desc"));
        addGlossaryEntry(vSensors, i18n.get("glossary.biomech.uv.title"), i18n.get("glossary.biomech.uv.desc"));
        addGlossaryEntry(vSensors, i18n.get("glossary.biomech.mandible.title"), i18n.get("glossary.biomech.mandible.desc"));
        addGlossaryEntry(vSensors, i18n.get("glossary.biomech.autothysis.title"), i18n.get("glossary.biomech.autothysis.desc"));
        addGlossaryEntry(vSensors, i18n.get("glossary.biomech.arolia.title"), i18n.get("glossary.biomech.arolia.desc"));

        Tab tNest = new Tab(i18n.get("glossary.tab.nest"), wrapScroll(vNest));
        Tab tSocial = new Tab(i18n.get("glossary.tab.social"), wrapScroll(vSocial));
        Tab tEnv = new Tab(i18n.get("glossary.tab.environment"), wrapScroll(vEnv));
        Tab tReasoning = new Tab(i18n.get("glossary.tab.reasoning"), wrapScroll(vReasoning));
        Tab tSensors = new Tab(i18n.get("glossary.tab.biomechanics"), wrapScroll(vSensors));

        subTabPane.getTabs().addAll(tNest, tSocial, tEnv, tReasoning, tSensors);
        VBox.setVgrow(subTabPane, Priority.ALWAYS);
        return subTabPane;
    }

    private void addGlossaryEntry(VBox box, String title, String description) {
        Label t = new Label("• " + title + " : ");
        t.getStyleClass().add("accent-text");
        t.setMinWidth(200);
        Label d = new Label(description);
        d.setWrapText(true);
        HBox row = new HBox(5, t, d);
        row.setPadding(new Insets(4, 0, 4, 0));
        box.getChildren().add(row);
    }

    private GridPane createColumnGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(6));
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setMinWidth(120);
        c1.setPrefWidth(130);
        c1.setHgrow(Priority.NEVER);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);
        return grid;
    }

    private VBox createInspectorColumnBox(String title, GridPane grid) {
        VBox box = new VBox(8);
        box.getStyleClass().add("render-options-panel");
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("accent-text");
        box.getChildren().addAll(lblTitle, new Separator(), grid);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(15));
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setMinWidth(240);
        c1.setPrefWidth(260);
        c1.setHgrow(Priority.NEVER);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);
        return grid;
    }

    private ScrollPane wrapScroll(javafx.scene.Node content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return scroll;
    }

    // --- Caste Data Model for TableView ---
    public static class CasteRow {
        private String name;
        private double bodyLengthMm;
        private double headWidthMm;
        private int lifespan;
        private float health;
        private float damage;
        private boolean canFly;

        // Task Weights & Caste Ratios
        private float foragingWeight = 0.30f;
        private float defenseWeight = 0.20f;
        private float excavationWeight = 0.20f;
        private float nursingWeight = 0.15f;
        private float queenCareWeight = 0.10f;
        private float sanitationWeight = 0.05f;
        private float targetRatio = 0.25f;

        // Cognitive Model Architecture
        private String decisionArch = "BDI"; // BDI, NEURAL_NETWORK, FSM, BEHAVIOR_TREE, FUZZY_LOGIC

        // Venom Systems
        private String venomType = "NONE";
        private float venomToxicity = 10.0f;
        private float venomRangeMm = 2.0f;

        // Motor & Biomechanical Caste Traits
        private float wingbeatFrequencyHz = 0.0f;
        private boolean hasHoveringCapability = false;
        private float maxPayloadRatio = 5.0f;
        private float bitingForceMpa = 15.0f;
        private boolean hasAutothysis = false;
        private boolean hasAroliaAdhesion = true;

        public CasteRow(String name, double bodyLengthMm, double headWidthMm, int lifespan, float health, float damage, boolean canFly) {
            this.name = name;
            this.bodyLengthMm = bodyLengthMm;
            this.headWidthMm = headWidthMm;
            this.lifespan = lifespan;
            this.health = health;
            this.damage = damage;
            this.canFly = canFly;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public double getBodyLengthMm() { return bodyLengthMm; }
        public void setBodyLengthMm(double bodyLengthMm) { this.bodyLengthMm = bodyLengthMm; }

        public double getHeadWidthMm() { return headWidthMm; }
        public void setHeadWidthMm(double headWidthMm) { this.headWidthMm = headWidthMm; }

        public double getMinTunnelMm() { return Math.max(1.0, Math.round(headWidthMm * 1.4 * 10.0) / 10.0); }

        public int getLifespan() { return lifespan; }
        public void setLifespan(int lifespan) { this.lifespan = lifespan; }

        public float getHealth() { return health; }
        public void setHealth(float health) { this.health = health; }

        public float getDamage() { return damage; }
        public void setDamage(float damage) { this.damage = damage; }

        public boolean isCanFly() { return canFly; }
        public void setCanFly(boolean canFly) { this.canFly = canFly; }

        public float getForagingWeight() { return foragingWeight; }
        public void setForagingWeight(float foragingWeight) { this.foragingWeight = foragingWeight; }

        public float getDefenseWeight() { return defenseWeight; }
        public void setDefenseWeight(float defenseWeight) { this.defenseWeight = defenseWeight; }

        public float getExcavationWeight() { return excavationWeight; }
        public void setExcavationWeight(float excavationWeight) { this.excavationWeight = excavationWeight; }

        public float getNursingWeight() { return nursingWeight; }
        public void setNursingWeight(float nursingWeight) { this.nursingWeight = nursingWeight; }

        public float getQueenCareWeight() { return queenCareWeight; }
        public void setQueenCareWeight(float queenCareWeight) { this.queenCareWeight = queenCareWeight; }

        public float getSanitationWeight() { return sanitationWeight; }
        public void setSanitationWeight(float sanitationWeight) { this.sanitationWeight = sanitationWeight; }

        public float getTargetRatio() { return targetRatio; }
        public void setTargetRatio(float targetRatio) { this.targetRatio = targetRatio; }

        public String getDecisionArch() { return decisionArch; }
        public void setDecisionArch(String decisionArch) { this.decisionArch = decisionArch; }

        public String getVenomType() { return venomType; }
        public void setVenomType(String venomType) { this.venomType = venomType; }

        public float getVenomToxicity() { return venomToxicity; }
        public void setVenomToxicity(float venomToxicity) { this.venomToxicity = venomToxicity; }

        public float getVenomRangeMm() { return venomRangeMm; }
        public void setVenomRangeMm(float venomRangeMm) { this.venomRangeMm = venomRangeMm; }

        public float getWingbeatFrequencyHz() { return wingbeatFrequencyHz; }
        public void setWingbeatFrequencyHz(float wingbeatFrequencyHz) { this.wingbeatFrequencyHz = wingbeatFrequencyHz; }

        public boolean isHasHoveringCapability() { return hasHoveringCapability; }
        public void setHasHoveringCapability(boolean hasHoveringCapability) { this.hasHoveringCapability = hasHoveringCapability; }

        public float getMaxPayloadRatio() { return maxPayloadRatio; }
        public void setMaxPayloadRatio(float maxPayloadRatio) { this.maxPayloadRatio = maxPayloadRatio; }

        public float getBitingForceMpa() { return bitingForceMpa; }
        public void setBitingForceMpa(float bitingForceMpa) { this.bitingForceMpa = bitingForceMpa; }

        public boolean isHasAutothysis() { return hasAutothysis; }
        public void setHasAutothysis(boolean hasAutothysis) { this.hasAutothysis = hasAutothysis; }

        public boolean isHasAroliaAdhesion() { return hasAroliaAdhesion; }
        public void setHasAroliaAdhesion(boolean hasAroliaAdhesion) { this.hasAroliaAdhesion = hasAroliaAdhesion; }
    }

    public static String getDietTitle(String diet) {
        if (diet == null) return "";
        return switch (diet) {
            case "SUGARS_NECTAR" -> "🍯 Nectar & Sugary Liquids";
            case "INSECTS_MEAT" -> "🥩 Protein Prey / Insects";
            case "SEEDS" -> "🌾 Seeds (Granivory / Ant Bread)";
            case "FUNGUS" -> "🍄 Symbiotic Fungi";
            case "WOOD_CELLULOSE" -> "🪵 Cellulose & Wood Fibers";
            case "HONEYDEW" -> "💧 Homopteran Honeydew (Aphids)";
            case "OMNIVORE" -> "🥗 Polyphagous Omnivore";
            case "NONE" -> "🚫 No Secondary Diet";
            default -> diet;
        };
    }

    public static String getDietDescription(String diet) {
        if (diet == null) return "";
        return switch (diet) {
            case "SUGARS_NECTAR" -> "Simple carbohydrate intake providing direct metabolic energy for adult daily activity.";
            case "INSECTS_MEAT" -> "Animal proteins essential for developing larvae and queen egg production.";
            case "SEEDS" -> "Storage and crushing of starch-rich plant seeds for seasonal reserves.";
            case "FUNGUS" -> "Cultivation of basidiomycete fungi on chewed plant material in dedicated chambers.";
            case "WOOD_CELLULOSE" -> "Digestion of wood fibers via symbiotic gut protozoa and bacteria (Termites).";
            case "HONEYDEW" -> "Trophobiotic farming of aphids and scale insects for regular honeydew harvesting.";
            case "OMNIVORE" -> "Opportunistic feeding adapting to all available trophic resources without specialization.";
            case "NONE" -> "No complementary or secondary trophic source required.";
            default -> "";
        };
    }

    public static String getNestTypeTitle(String type) {
        if (type == null) return "";
        return switch (type) {
            case "WAX_COMB_HEXAGONAL" -> "🐝 Hexagonal Wax Combs";
            case "WAX_POTS_CLUSTER" -> "🍯 Wax Pot Clusters";
            case "PAPER_PEDUNCULATE" -> "🐝 Suspended Paper Nest";
            case "CATHEDRAL_MOUND" -> "🏰 Cathedral Termite Mound";
            case "ARBOREAL_SILK_LEAF" -> "🍃 Arboreal Woven Silk Nest";
            case "SUBTERRANEAN_FUNGI_VAULT" -> "🍄 Subterranean Fungi Vault";
            case "CARTON_NEST" -> "📦 Carton Wood Nest";
            case "BAMBOO_STEM_NEST" -> "🎋 Hollow Stem & Cavity Nest";
            case "BIVOUAC_LIVING_NEST" -> "🐜 Living Bivouac Nest";
            case "MOUND" -> "🏔️ Earth / Needle Mound";
            case "TREE" -> "🪵 Tree Trunk Cavity Nest";
            case "MATURE" -> "🏛️ Established Subterranean Nest";
            case "SIMPLE" -> "🕳️ Simple Subterranean Burrows";
            default -> type;
        };
    }

    public static String getNestTypeDescription(String type) {
        if (type == null) return "";
        return switch (type) {
            case "WAX_COMB_HEXAGONAL" -> "Suspended honeycomb structure built from wax secreted by worker abdominal glands (Bees).";
            case "WAX_POTS_CLUSTER" -> "Spherical wax cells and honey/pollen storage pots organized in irregular clusters (Bumblebees).";
            case "PAPER_PEDUNCULATE" -> "Open paper cells constructed from chewed wood fibers mixed with saliva (Wasps).";
            case "CATHEDRAL_MOUND" -> "Imposing soil structure with ventilation shafts regulating temperature and CO2 (Termites).";
            case "ARBOREAL_SILK_LEAF" -> "Living leaves bound together using silk threads secreted by larvae held by workers (Weaver ants).";
            case "SUBTERRANEAN_FUNGI_VAULT" -> "Vast subterranean chamber network housing symbiotic fungal gardens (Leafcutter ants).";
            case "CARTON_NEST" -> "Spherical arboreal or cavity nest constructed from chewed wood paste and saliva.";
            case "BAMBOO_STEM_NEST" -> "Opportunistic nest constructed inside pre-existing cavities (hollow stems, plant galls, wood boreholes).";
            case "BIVOUAC_LIVING_NEST" -> "Temporary nest formed exclusively by the intertwined bodies of thousands of workers (Army ants).";
            case "MOUND" -> "Subterranean mound topped with an insulating dome of soil and pine needles (Wood ants).";
            case "TREE" -> "Galleries excavated directly into dead wood or decaying tree trunks (Carpenter ants).";
            case "MATURE" -> "Highly developed subterranean nest with extensive brood chambers, queen chambers, and granaries.";
            case "SIMPLE" -> "Rudimentary subterranean nest composed of a few galleries beneath stones or grass tufts.";
            default -> "";
        };
    }

    private String getSelectedVenomTypes(java.util.Map<String, CheckMenuItem> items) {
        List<String> selected = new ArrayList<>();
        for (java.util.Map.Entry<String, CheckMenuItem> entry : items.entrySet()) {
            if (entry.getValue().isSelected()) {
                selected.add(entry.getKey());
            }
        }
        if (selected.isEmpty()) return "NONE";
        return String.join(", ", selected);
    }

    private void setSelectedVenomTypes(java.util.Map<String, CheckMenuItem> items, MenuButton btn, String venomStr) {
        if (items == null || btn == null) return;
        List<String> selectedKeys = new ArrayList<>();
        if (venomStr != null && !venomStr.trim().isEmpty()) {
            for (String p : venomStr.split(",")) {
                selectedKeys.add(p.trim());
            }
        }
        if (selectedKeys.isEmpty()) selectedKeys.add("NONE");

        for (java.util.Map.Entry<String, CheckMenuItem> entry : items.entrySet()) {
            entry.getValue().setSelected(selectedKeys.contains(entry.getKey()));
        }
        updateVenomMenuText(btn, items);
    }

    private void updateVenomMenuText(MenuButton btn, java.util.Map<String, CheckMenuItem> items) {
        String selStr = getSelectedVenomTypes(items);
        btn.setText(getVenomTitle(selStr));
    }

    public static String getVenomTitle(String v) {
        if (v == null || v.trim().isEmpty()) return "";
        if (v.contains(",")) {
            String[] parts = v.split(",");
            List<String> titles = new ArrayList<>();
            for (String p : parts) {
                String title = getVenomTitle(p.trim());
                if (!title.isEmpty()) titles.add(title);
            }
            return String.join(", ", titles);
        }
        return switch (v.trim()) {
            case "NONE" -> "🚫 No Venom (Physical Attack)";
            case "FORMIC_ACID" -> "🧪 Formic Acid (Spray)";
            case "VENOMOUS_STING" -> "🗡️ Venomous Sting (Stinger)";
            case "CHEMICAL_SPRAY" -> "💨 Repellent Chemical Spray";
            case "ACID_SPRAY" -> "💨 Acid Repellent Spray";
            case "POWERFUL_MANDIBLES" -> "✂️ Powerful Mandibles (Shear)";
            case "SOLENOPSIN" -> "🔥 Solenopsin (Stinging Alkaloid)";
            case "NEUROTOXIN" -> "🧠 Paralytic Neurotoxin";
            case "CYTOTOXIN" -> "🧫 Necrotic Cytotoxin";
            case "TERPENE_RESIN" -> "🌲 Sticky Terpene Resin";
            case "AUTOTHYSIS_BOMB" -> "💥 Autothysis (Suicidal Bomb)";
            default -> v.trim();
        };
    }

    public static String getVenomDescription(String v) {
        if (v == null || v.trim().isEmpty()) return "";
        if (v.contains(",")) {
            String[] parts = v.split(",");
            List<String> descs = new ArrayList<>();
            for (String p : parts) {
                String desc = getVenomDescription(p.trim());
                if (!desc.isEmpty()) descs.add(desc);
            }
            return String.join(" | ", descs);
        }
        return switch (v.trim()) {
            case "NONE" -> "No chemical venom. Fights exclusively via mandibular bites and mechanical force.";
            case "FORMIC_ACID" -> "Ranged spray or contact application of concentrated formic acid causing chemical burns.";
            case "VENOMOUS_STING" -> "Direct injection of protein venom using a retractable abdominal stinger causing pain and paralysis.";
            case "CHEMICAL_SPRAY", "ACID_SPRAY" -> "Spraying of repellent or corrosive chemicals causing disorientation and chemical irritation.";
            case "POWERFUL_MANDIBLES" -> "Hypertrophied mandibles capable of exerting lethal pressure or decapitating prey.";
            case "SOLENOPSIN" -> "Necrotic alkaloid toxin causing sharp burning pain and localized pustules (Solenopsis invicta).";
            case "NEUROTOXIN" -> "Substance targeting arthropod central nervous system to block neuromuscular transmission.";
            case "CYTOTOXIN" -> "Necrotic toxin destroying opponent cell membranes upon envenomation.";
            case "TERPENE_RESIN" -> "Viscous terpene liquid expelled under pressure from nasute soldier heads to entangle enemies.";
            case "AUTOTHYSIS_BOMB" -> "Extreme muscle contraction breaking abdominal wall and bursting sticky toxic glue.";
            default -> "";
        };
    }

    public static String getDecisionArchTitle(String arch) {
        if (arch == null) return "";
        ArchitectureType t = ArchitectureType.parse(arch);
        return "🧠 " + t.getDisplayName();
    }

    public static String getDecisionArchDescription(String arch) {
        if (arch == null) return "";
        ArchitectureType t = ArchitectureType.parse(arch);
        return switch (t) {
            case BDI -> "Cognitive architecture based on Beliefs, Desires, and Intentions for complex reasoning.";
            case BEHAVIOR_TREE -> "Recursive hierarchical tree of selectors and sequences for modular behavior.";
            case BLACKBOARD -> "Centralized shared memory system where agents query and modify common knowledge.";
            case FINITE_STATE_MACHINE -> "Deterministic state machine with rapid state transitions (Foraging, Defense, Nursing, Resting).";
            case FUZZY_LOGIC -> "Fuzzy inference engine allowing nuanced handling of ambiguous and gradual conditions.";
            case HYBRID -> "Hybrid combination associating reactive FSM/Tree with BDI cognitive planning.";
            case NEURAL_NETWORK -> "Artificial neural network (SNN/ANN) processing continuous sensory signals.";
        };
    }

    public static String formatDec(double val) {
        if (Double.isNaN(val) || Double.isInfinite(val)) return "0";
        if (val == Math.floor(val)) return String.format(java.util.Locale.US, "%.0f", val);
        return String.format(java.util.Locale.US, "%.2f", val);
    }

    public static String formatDec(float val) {
        return formatDec((double) val);
    }

    public static class FormattedDoubleStringConverter extends javafx.util.StringConverter<Double> {
        @Override
        public String toString(Double object) {
            if (object == null) return "0";
            return formatDec(object);
        }
        @Override
        public Double fromString(String string) {
            try { return Double.parseDouble(string.trim()); } catch (Exception e) { return 0.0; }
        }
    }

    public static class FormattedFloatStringConverter extends javafx.util.StringConverter<Float> {
        @Override
        public String toString(Float object) {
            if (object == null) return "0";
            return formatDec(object);
        }
        @Override
        public Float fromString(String string) {
            try { return Float.parseFloat(string.trim()); } catch (Exception e) { return 0.0f; }
        }
    }

    public static void selectComboValue(ComboBox<String> combo, String val) {
        if (combo == null || val == null) return;
        if (combo.getItems().contains(val)) {
            combo.getSelectionModel().select(val);
            return;
        }
        String mapped = mapTechnicalToReadable(val);
        if (mapped != null && combo.getItems().contains(mapped)) {
            combo.getSelectionModel().select(mapped);
        } else {
            for (String item : combo.getItems()) {
                if (item.equalsIgnoreCase(val)) {
                    combo.getSelectionModel().select(item);
                    return;
                }
            }
        }
    }

    public static String mapReadableToTechnical(String val) {
        if (val == null) return "";
        String v = val.trim();
        if ("Ants (Formicidae)".equalsIgnoreCase(v)) return "ANT";
        if ("Bees (Apidae)".equalsIgnoreCase(v)) return "BEE";
        if ("Wasps (Vespidae)".equalsIgnoreCase(v)) return "WASP";
        if ("Termites (Termitoidae)".equalsIgnoreCase(v)) return "TERMITE";
        if ("Other Eusocial Taxa".equalsIgnoreCase(v)) return "OTHER";

        if ("Monogyne (Single Queen)".equalsIgnoreCase(v)) return "MONOGYNE";
        if ("Polygyne (Multiple Queens)".equalsIgnoreCase(v)) return "POLYGYNE";
        if ("Gamergates (Reproductive Workers)".equalsIgnoreCase(v)) return "GAMERGATES";

        if ("Aerial Swarm Flight".equalsIgnoreCase(v)) return "AERIAL_SWARM";
        if ("Swarm Division".equalsIgnoreCase(v)) return "SWARM_DIVISION";
        if ("Budding / Sociotomy".equalsIgnoreCase(v)) return "BUDDING";
        if ("In-Nest Mating".equalsIgnoreCase(v)) return "IN_NEST";

        if ("Cellulose & Wood Fibers".equalsIgnoreCase(v)) return "CELLULOSE";
        if ("Fungus Garden Mycelium".equalsIgnoreCase(v)) return "FUNGUS";
        if ("High Protein Meat & Insects".equalsIgnoreCase(v)) return "HIGH_PROTEIN_MEAT";
        if ("Omnivorous Mixed Diet".equalsIgnoreCase(v)) return "OMNIVORE";
        if ("Seeds & Harvested Grains".equalsIgnoreCase(v)) return "SEEDS";
        if ("Sugars, Honey & Nectar".equalsIgnoreCase(v)) return "SUGAR_HONEY";

        if ("Honeydew & Aphid Trophobiosis".equalsIgnoreCase(v)) return "HONEYDEW";
        if ("Insects & Meat Protein".equalsIgnoreCase(v)) return "INSECTS_MEAT";
        if ("Sugars & Plant Nectar".equalsIgnoreCase(v)) return "SUGARS_NECTAR";
        if ("Seeds & Granivory Grains".equalsIgnoreCase(v)) return "SEEDS";
        if ("Fungus & Cultivated Mycelium".equalsIgnoreCase(v)) return "FUNGUS";
        if ("Wood & Cellulose Fibers".equalsIgnoreCase(v)) return "WOOD_CELLULOSE";
        if ("Omnivorous Polyphagous".equalsIgnoreCase(v)) return "OMNIVORE";
        if ("None (No Secondary Diet)".equalsIgnoreCase(v)) return "NONE";

        if ("BDI (Belief-Desire-Intention)".equalsIgnoreCase(v)) return "BDI";
        if ("Behavior Tree".equalsIgnoreCase(v)) return "BEHAVIOR_TREE";
        if ("FSM (Finite State Machine)".equalsIgnoreCase(v)) return "FSM";
        if ("Fuzzy Logic Engine".equalsIgnoreCase(v)) return "FUZZY_LOGIC";
        if ("Neural Network (ANN)".equalsIgnoreCase(v)) return "NEURAL_NETWORK";

        return val;
    }

    public static String mapTechnicalToReadable(String val) {
        if (val == null) return "";
        return switch (val.toUpperCase().trim()) {
            case "ANT" -> "Ants (Formicidae)";
            case "BEE" -> "Bees (Apidae)";
            case "OTHER" -> "Other Eusocial Taxa";
            case "TERMITE" -> "Termites (Termitoidae)";
            case "WASP" -> "Wasps (Vespidae)";

            case "GAMERGATES" -> "Gamergates (Reproductive Workers)";
            case "MONOGYNE" -> "Monogyne (Single Queen)";
            case "POLYGYNE" -> "Polygyne (Multiple Queens)";

            case "AERIAL_SWARM" -> "Aerial Swarm Flight";
            case "BUDDING" -> "Budding / Sociotomy";
            case "IN_NEST" -> "In-Nest Mating";
            case "SWARM_DIVISION" -> "Swarm Division";

            case "CELLULOSE" -> "Cellulose & Wood Fibers";
            case "FUNGUS" -> "Fungus Garden Mycelium";
            case "HIGH_PROTEIN_MEAT" -> "High Protein Meat & Insects";
            case "OMNIVORE" -> "Omnivorous Mixed Diet";
            case "SEEDS" -> "Seeds & Harvested Grains";
            case "SUGAR_HONEY" -> "Sugars, Honey & Nectar";

            case "HONEYDEW" -> "Honeydew & Aphid Trophobiosis";
            case "INSECTS_MEAT" -> "Insects & Meat Protein";
            case "SUGARS_NECTAR" -> "Sugars & Plant Nectar";
            case "WOOD_CELLULOSE" -> "Wood & Cellulose Fibers";
            case "NONE" -> "None (No Secondary Diet)";

            case "BDI" -> "BDI (Belief-Desire-Intention)";
            case "BEHAVIOR_TREE" -> "Behavior Tree";
            case "FSM" -> "FSM (Finite State Machine)";
            case "FUZZY_LOGIC" -> "Fuzzy Logic Engine";
            case "NEURAL_NETWORK" -> "Neural Network (ANN)";

            case "LOWER_ELEVATION" -> "Lower Elevation";
            case "RAISE_ELEVATION" -> "Raise Elevation";
            case "SMOOTH" -> "Smooth Relief";

            case "CLAY" -> "Clay Substrate";
            case "EARTH" -> "Earth Substrate";
            case "SAND" -> "Sand Substrate";
            case "STONE" -> "Stone Substrate";
            default -> val;
        };
    }
}
