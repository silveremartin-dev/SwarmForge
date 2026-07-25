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
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.core.domain.CasteTemplate;
import org.swarmforge.core.species.CustomSpecies;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Rich Species Editor Pane for defining realistic eusocial species (ants, bees, wasps, termites).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SpeciesEditorPane extends VBox {

    private final SpeciesPresetManager presetManager = new SpeciesPresetManager();

    // UI Fields
    private ComboBox<String> presetCombo;
    private TextField presetNameField;

    // General & Taxonomy
    private TextField commonNameField;
    private TextField scientificNameField;
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

    // Development Stages
    private TextField eggDurationField;
    private TextField larvaDurationField;
    private ComboBox<String> larvaDietCombo;
    private TextField pupaDurationField;

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

    // Nest & Behavior
    private ComboBox<String> nestTypeCombo;
    private TextField optTempField;
    private TextField minTempField;
    private TextField maxTempField;
    private Slider aggressionSlider;
    private Slider territorialitySlider;
    private ComboBox<String> venomCombo;

    private Consumer<CustomSpecies> onApplyListener;

    public SpeciesEditorPane() {
        setSpacing(15);
        setPadding(new Insets(15));

        // Header Title
        Label headerLabel = new Label("Eusocial Species Designer");
        headerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // 1. Top Action Toolbar (Presets & File Operations)
        HBox topToolbar = createTopToolbar();

        // 2. TabPane for Parameter Sections
        TabPane tabPane = createTabPane();

        getChildren().addAll(headerLabel, topToolbar, new Separator(), tabPane);

        // Load default preset if available
        if (!presetManager.getPresetNames().isEmpty()) {
            String firstPreset = presetManager.getPresetNames().iterator().next();
            presetCombo.getSelectionModel().select(firstPreset);
            loadPresetToUI(presetManager.getPreset(firstPreset));
        }
    }

    public void setOnApply(Consumer<CustomSpecies> listener) {
        this.onApplyListener = listener;
    }

    private HBox createTopToolbar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10));

        Label lblPreset = new Label("Preset:");
        lblPreset.setStyle("-fx-font-weight: bold;");

        presetCombo = new ComboBox<>();
        presetCombo.getItems().addAll(presetManager.getPresetNames());
        presetCombo.setPrefWidth(240);
        presetCombo.setOnAction(e -> {
            String sel = presetCombo.getValue();
            if (sel != null && presetManager.contains(sel)) {
                loadPresetToUI(presetManager.getPreset(sel));
            }
        });

        Label lblName = new Label("Name:");
        lblName.setStyle("-fx-font-weight: bold;");

        presetNameField = new TextField();
        presetNameField.setPromptText("Configuration Preset Name");
        presetNameField.setPrefWidth(220);

        Button btnAddPreset = new Button("Add to Presets", new FontIcon(Feather.PLUS_CIRCLE));
        btnAddPreset.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        btnAddPreset.setOnAction(e -> handleAddPreset());

        Button btnSaveDisk = new Button("Save to Disk...", new FontIcon(Feather.SAVE));
        btnSaveDisk.setStyle("-fx-background-color: #1565c0; -fx-text-fill: white;");
        btnSaveDisk.setOnAction(e -> handleSaveDisk());

        Button btnLoadDisk = new Button("Load from Disk...", new FontIcon(Feather.FOLDER));
        btnLoadDisk.setStyle("-fx-background-color: #424242; -fx-text-fill: white;");
        btnLoadDisk.setOnAction(e -> handleLoadDisk());

        Button btnApply = new Button("Apply to World", new FontIcon(Feather.CHECK));
        btnApply.setStyle("-fx-background-color: #e65100; -fx-text-fill: white; -fx-font-weight: bold;");
        btnApply.setOnAction(e -> {
            CustomSpecies s = buildSpeciesFromUI();
            if (onApplyListener != null) {
                onApplyListener.accept(s);
            } else {
                new Alert(Alert.AlertType.INFORMATION, "Species '" + s.getCommonName() + "' active!").show();
            }
        });

        bar.getChildren().addAll(lblPreset, presetCombo, lblName, presetNameField, btnAddPreset, btnSaveDisk, btnLoadDisk, btnApply);
        return bar;
    }

    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab tabTaxonomy = new Tab("Taxonomy & Group", createTaxonomyPane());
        tabTaxonomy.setGraphic(new FontIcon(Feather.BOOK));

        Tab tabQueens = new Tab("Colony & Queens", createQueensPane());
        tabQueens.setGraphic(new FontIcon(Feather.AWARD));

        Tab tabStages = new Tab("Life Stages", createStagesPane());
        tabStages.setGraphic(new FontIcon(Feather.CLOCK));

        Tab tabCastes = new Tab("Castes & Morphology", createCastesPane());
        tabCastes.setGraphic(new FontIcon(Feather.USERS));

        Tab tabDiet = new Tab("Diet & Metabolism", createDietPane());
        tabDiet.setGraphic(new FontIcon(Feather.FEATHER));

        Tab tabNest = new Tab("Nest & Behavior", createNestPane());
        tabNest.setGraphic(new FontIcon(Feather.HOME));

        List<Tab> tabs = List.of(tabTaxonomy, tabQueens, tabStages, tabCastes, tabDiet, tabNest);
        for (Tab t : tabs) {
            Label tabLabel = new Label(t.getText());
            tabLabel.getStyleClass().add("tab-label");
            t.setText("");
            t.setGraphic(new HBox(5, t.getGraphic(), tabLabel));
        }

        tabPane.getTabs().addAll(tabs);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        return tabPane;
    }

    // --- Tab 1: Taxonomy ---
    private ScrollPane createTaxonomyPane() {
        GridPane grid = createGrid();

        commonNameField = new TextField("Fourmi Noire des Jardins");
        scientificNameField = new TextField("Lasius niger");

        insectTypeCombo = new ComboBox<>(FXCollections.observableArrayList("ANT", "BEE", "WASP", "TERMITE", "OTHER"));
        insectTypeCombo.getSelectionModel().select("ANT");

        categoryCombo = new ComboBox<>(FXCollections.observableArrayList(org.swarmforge.core.species.SpeciesCategory.values()));
        categoryCombo.getSelectionModel().select(org.swarmforge.core.species.SpeciesCategory.EUSOCIAL_PRIMARY);

        descriptionArea = new TextArea("Description de l'espèce...");
        descriptionArea.setPrefRowCount(4);

        grid.addRow(0, createWhiteLabel("Nom Commun:"), commonNameField);
        grid.addRow(1, createWhiteLabel("Nom Scientifique:"), scientificNameField);
        grid.addRow(2, createWhiteLabel("Groupe d'Insectes:"), insectTypeCombo);
        grid.addRow(3, createWhiteLabel("Rôle Écologique / Catégorie:"), categoryCombo);
        grid.addRow(4, createWhiteLabel("Description:"), descriptionArea);

        return wrapScroll(grid);
    }

    // --- Tab 2: Colony & Queens (Aspect CRITIQUE) ---
    private ScrollPane createQueensPane() {
        GridPane grid = createGrid();

        queenModeCombo = new ComboBox<>(FXCollections.observableArrayList("MONOGYNE", "POLYGYNE", "GAMERGATES"));
        queenModeCombo.getSelectionModel().select("MONOGYNE");

        queenCountSpinner = new Spinner<>(1, 500, 1);
        queenLifespanField = new TextField("25000");
        queenEggRateField = new TextField("25.0");

        hasKingCheckBox = new CheckBox("Présence d'un Roi Reproducteur (Termites)");

        kingLifespanField = new TextField("15000");
        nuptialFlightCombo = new ComboBox<>(FXCollections.observableArrayList("AERIAL_SWARM", "SWARM_DIVISION", "BUDDING", "IN_NEST"));
        nuptialFlightCombo.getSelectionModel().select("AERIAL_SWARM");

        grid.addRow(0, createWhiteLabel("Mode de Reine:"), queenModeCombo);
        grid.addRow(1, createWhiteLabel("Nombre de Reines dans le nid:"), queenCountSpinner);
        grid.addRow(2, createWhiteLabel("Durée de vie Reine (ticks):"), queenLifespanField);
        grid.addRow(3, createWhiteLabel("Taux de ponte (œufs/jour):"), queenEggRateField);
        grid.addRow(4, createWhiteLabel("Roi Reproducteur:"), hasKingCheckBox);
        grid.addRow(5, createWhiteLabel("Durée de vie Roi (ticks):"), kingLifespanField);
        grid.addRow(6, createWhiteLabel("Mode de Vol Nuptial / Essaimage:"), nuptialFlightCombo);

        return wrapScroll(grid);
    }

    // --- Tab 3: Development Stages ---
    private ScrollPane createStagesPane() {
        GridPane grid = createGrid();

        eggDurationField = new TextField("300");
        larvaDurationField = new TextField("600");
        larvaDietCombo = new ComboBox<>(FXCollections.observableArrayList("HIGH_PROTEIN_MEAT", "SUGAR_HONEY", "FUNGUS", "CELLULOSE", "SEEDS", "OMNIVORE"));
        larvaDietCombo.getSelectionModel().select("HIGH_PROTEIN_MEAT");
        pupaDurationField = new TextField("500");

        grid.addRow(0, createWhiteLabel("Durée du stade Œuf (ticks):"), eggDurationField);
        grid.addRow(1, createWhiteLabel("Durée du stade Larve (ticks):"), larvaDurationField);
        grid.addRow(2, createWhiteLabel("Régime alimentaire des Larves:"), larvaDietCombo);
        grid.addRow(3, createWhiteLabel("Durée du stade Nymphe/Cocon (ticks):"), pupaDurationField);

        return wrapScroll(grid);
    }

    // --- Tab 4: Castes & Morphology ---
    private ScrollPane createCastesPane() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(15));

        Label infoLabel = new Label("Définition des castes et gabarit (Longueur, Largeur Tête & Diamètre minimal des Tunnels pour le déplacement)");
        infoLabel.setStyle("-fx-text-fill: #aaa; -fx-font-style: italic;");

        casteTable = new TableView<>(casteRows);
        casteTable.setPrefHeight(240);

        TableColumn<CasteRow, String> nameCol = new TableColumn<>("Caste");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(130);

        TableColumn<CasteRow, Double> bodyCol = new TableColumn<>("Longueur (mm)");
        bodyCol.setCellValueFactory(new PropertyValueFactory<>("bodyLengthMm"));
        bodyCol.setPrefWidth(95);

        TableColumn<CasteRow, Double> headCol = new TableColumn<>("Tête (mm)");
        headCol.setCellValueFactory(new PropertyValueFactory<>("headWidthMm"));
        headCol.setPrefWidth(75);

        TableColumn<CasteRow, Double> tunnelCol = new TableColumn<>("Ø Tunnel Min (mm)");
        tunnelCol.setCellValueFactory(new PropertyValueFactory<>("minTunnelMm"));
        tunnelCol.setPrefWidth(115);

        TableColumn<CasteRow, Integer> lifeCol = new TableColumn<>("Vie (ticks)");
        lifeCol.setCellValueFactory(new PropertyValueFactory<>("lifespan"));
        lifeCol.setPrefWidth(85);

        TableColumn<CasteRow, Float> healthCol = new TableColumn<>("Santé");
        healthCol.setCellValueFactory(new PropertyValueFactory<>("health"));
        healthCol.setPrefWidth(65);

        TableColumn<CasteRow, Float> dmgCol = new TableColumn<>("Attaque");
        dmgCol.setCellValueFactory(new PropertyValueFactory<>("damage"));
        dmgCol.setPrefWidth(65);

        TableColumn<CasteRow, Boolean> flyCol = new TableColumn<>("Volant");
        flyCol.setCellValueFactory(new PropertyValueFactory<>("canFly"));
        flyCol.setPrefWidth(60);

        casteTable.getColumns().addAll(nameCol, bodyCol, headCol, tunnelCol, lifeCol, healthCol, dmgCol, flyCol);

        // Controls to add/edit caste
        GridPane casteForm = createGrid();
        TextField casteNameF = new TextField("Soldat");
        TextField casteBodyF = new TextField("6.0");
        TextField casteHeadF = new TextField("1.8");
        TextField casteLifeF = new TextField("5000");
        TextField casteHealthF = new TextField("120");
        TextField casteDmgF = new TextField("15");
        CheckBox casteFlyCheck = new CheckBox("Volant");

        casteForm.addRow(0, createWhiteLabel("Nom Caste:"), casteNameF, createWhiteLabel("Longueur Corps (mm):"), casteBodyF, createWhiteLabel("Largeur Tête (mm):"), casteHeadF);
        casteForm.addRow(1, createWhiteLabel("Durée de vie:"), casteLifeF, createWhiteLabel("Santé de base:"), casteHealthF, createWhiteLabel("Dégâts Attaque:"), casteDmgF);
        casteForm.addRow(2, createWhiteLabel("Capacité Vol:"), casteFlyCheck);

        HBox casteBtns = new HBox(10);
        Button btnAddCaste = new Button("Ajouter Caste");
        btnAddCaste.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        btnAddCaste.setOnAction(e -> {
            try {
                double body = Double.parseDouble(casteBodyF.getText());
                double head = Double.parseDouble(casteHeadF.getText());
                CasteRow row = new CasteRow(
                        casteNameF.getText(),
                        body,
                        head,
                        Integer.parseInt(casteLifeF.getText()),
                        Float.parseFloat(casteHealthF.getText()),
                        Float.parseFloat(casteDmgF.getText()),
                        casteFlyCheck.isSelected()
                );
                casteRows.add(row);
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Format de nombre invalide.").show();
            }
        });

        Button btnDelCaste = new Button("Supprimer Sélection");
        btnDelCaste.setStyle("-fx-background-color: #c62828; -fx-text-fill: white;");
        btnDelCaste.setOnAction(e -> {
            CasteRow sel = casteTable.getSelectionModel().getSelectedItem();
            if (sel != null) casteRows.remove(sel);
        });

        casteBtns.getChildren().addAll(btnAddCaste, btnDelCaste);

        box.getChildren().addAll(infoLabel, casteTable, casteForm, casteBtns);
        return wrapScroll(box);
    }

    // --- Tab 5: Diet & Metabolism ---
    private ScrollPane createDietPane() {
        GridPane grid = createGrid();

        primaryDietCombo = new ComboBox<>(FXCollections.observableArrayList("SUGARS_NECTAR", "INSECTS_MEAT", "SEEDS", "FUNGUS", "WOOD_CELLULOSE", "HONEYDEW", "OMNIVORE"));
        primaryDietCombo.getSelectionModel().select("HONEYDEW");

        secondaryDietCombo = new ComboBox<>(FXCollections.observableArrayList("NONE", "INSECTS_MEAT", "SUGARS_NECTAR", "SEEDS"));
        secondaryDietCombo.getSelectionModel().select("INSECTS_MEAT");

        foodConsumptionField = new TextField("0.5");
        waterReqField = new TextField("0.2");
        workerLifespanField = new TextField("6000");
        workerSpeedField = new TextField("0.5");
        viewDistanceField = new TextField("5.0");
        colonySizeField = new TextField("15000");

        megaColonyCheckBox = new CheckBox("Forme des Supercolonies (Agglomération de nids)");

        flyCheckBox = new CheckBox("Ouvrières capables de voler (Abeilles / Guêpes)");

        grid.addRow(0, createWhiteLabel("Nourriture Principale:"), primaryDietCombo);
        grid.addRow(1, createWhiteLabel("Nourriture Secondaire:"), secondaryDietCombo);
        grid.addRow(2, createWhiteLabel("Consommation / jour / individu:"), foodConsumptionField);
        grid.addRow(3, createWhiteLabel("Besoin en Eau:"), waterReqField);
        grid.addRow(4, createWhiteLabel("Durée de vie Ouvrière (ticks):"), workerLifespanField);
        grid.addRow(5, createWhiteLabel("Vitesse de déplacement:"), workerSpeedField);
        grid.addRow(6, createWhiteLabel("Distance de vision (détection):"), viewDistanceField);
        grid.addRow(7, createWhiteLabel("Taille typique de colonie mature:"), colonySizeField);
        grid.addRow(8, createWhiteLabel("Supercolonies:"), megaColonyCheckBox);
        grid.addRow(9, createWhiteLabel("Vol des Ouvrières:"), flyCheckBox);

        return wrapScroll(grid);
    }

    // --- Tab 6: Nest & Behavior ---
    private ScrollPane createNestPane() {
        GridPane grid = createGrid();

        nestTypeCombo = new ComboBox<>(FXCollections.observableArrayList("UNDERGROUND_BURROW", "MOUND", "WOOD_TUNNELS", "PAPER_NEST", "WAX_COMB", "ARBOREAL_LEAF"));
        nestTypeCombo.getSelectionModel().select("UNDERGROUND_BURROW");

        optTempField = new TextField("24.0");
        minTempField = new TextField("10.0");
        maxTempField = new TextField("38.0");

        aggressionSlider = new Slider(0.0, 1.0, 0.3);
        aggressionSlider.setShowTickLabels(true);
        aggressionSlider.setShowTickMarks(true);

        territorialitySlider = new Slider(0.0, 1.0, 0.5);
        territorialitySlider.setShowTickLabels(true);
        territorialitySlider.setShowTickMarks(true);

        venomCombo = new ComboBox<>(FXCollections.observableArrayList("NONE", "FORMIC_ACID", "VENOMOUS_STING", "CHEMICAL_SPRAY", "POWERFUL_MANDIBLES"));
        venomCombo.getSelectionModel().select("FORMIC_ACID");

        grid.addRow(0, createWhiteLabel("Type de Nid construit:"), nestTypeCombo);
        grid.addRow(1, createWhiteLabel("Température Optimale (°C):"), optTempField);
        grid.addRow(2, createWhiteLabel("Température Minima (°C):"), minTempField);
        grid.addRow(3, createWhiteLabel("Température Maxima (°C):"), maxTempField);
        grid.addRow(4, createWhiteLabel("Niveau d'Agressivité:"), aggressionSlider);
        grid.addRow(5, createWhiteLabel("Territorialité:"), territorialitySlider);
        grid.addRow(6, createWhiteLabel("Arme / Type de Venin:"), venomCombo);

        return wrapScroll(grid);
    }

    // --- Helper Methods ---

    private void handleAddPreset() {
        String name = presetNameField.getText().trim();
        if (name.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Veuillez spécifier un nom pour le preset.").show();
            return;
        }

        CustomSpecies species = buildSpeciesFromUI();
        species.setPresetName(name);

        presetManager.addPreset(name, species);

        if (!presetCombo.getItems().contains(name)) {
            presetCombo.getItems().add(name);
        }
        presetCombo.getSelectionModel().select(name);

        new Alert(Alert.AlertType.INFORMATION, "Preset '" + name + "' ajouté et sauvegardé.").show();
    }

    private void handleSaveDisk() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Sauvegarder l'espèce");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        chooser.setInitialFileName("species_" + commonNameField.getText().replaceAll("\\s+", "_") + ".json");

        File f = chooser.showSaveDialog(getScene().getWindow());
        if (f != null) {
            try {
                CustomSpecies species = buildSpeciesFromUI();
                presetManager.saveToFile(f, species);
                new Alert(Alert.AlertType.INFORMATION, "Espèce enregistrée avec succès sous " + f.getName()).show();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Erreur de sauvegarde: " + ex.getMessage()).show();
            }
        }
    }

    private void handleLoadDisk() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Charger une espèce");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File f = chooser.showOpenDialog(getScene().getWindow());
        if (f != null) {
            try {
                CustomSpecies species = presetManager.loadFromFile(f);
                loadPresetToUI(species);
                new Alert(Alert.AlertType.INFORMATION, "Espèce '" + species.getCommonName() + "' chargée avec succès!").show();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Erreur de chargement: " + ex.getMessage()).show();
            }
        }
    }

    private void loadPresetToUI(CustomSpecies s) {
        if (s == null) return;

        presetNameField.setText(s.getPresetName() != null ? s.getPresetName() : s.getCommonName());

        commonNameField.setText(s.getCommonName());
        scientificNameField.setText(s.getScientificName());
        insectTypeCombo.getSelectionModel().select(s.getInsectType());
        categoryCombo.getSelectionModel().select(s.getCategory());
        descriptionArea.setText(s.getDescription());

        queenModeCombo.getSelectionModel().select(s.getQueenCountMode());
        queenCountSpinner.getValueFactory().setValue(s.getQueenCount());
        queenLifespanField.setText(String.valueOf(s.getQueenLifespan()));
        queenEggRateField.setText(String.valueOf(s.getQueenEggLayingRate()));
        hasKingCheckBox.setSelected(s.isHasKing());
        kingLifespanField.setText(String.valueOf(s.getKingLifespan()));
        nuptialFlightCombo.getSelectionModel().select(s.getNuptialFlightType());

        eggDurationField.setText(String.valueOf(s.getEggStageDuration()));
        larvaDurationField.setText(String.valueOf(s.getLarvaStageDuration()));
        larvaDietCombo.getSelectionModel().select(s.getLarvaDietRequirement());
        pupaDurationField.setText(String.valueOf(s.getPupaStageDuration()));

        primaryDietCombo.getSelectionModel().select(s.getPrimaryDiet());
        secondaryDietCombo.getSelectionModel().select(s.getSecondaryDiet());
        foodConsumptionField.setText(String.valueOf(s.getDailyFoodConsumption()));
        waterReqField.setText(String.valueOf(s.getWaterRequirement()));
        workerLifespanField.setText(String.valueOf(s.getWorkerLifespan()));
        workerSpeedField.setText(String.valueOf(s.getWorkerSpeed()));
        viewDistanceField.setText(String.valueOf(s.getViewDistance()));
        colonySizeField.setText(String.valueOf(s.getTypicalColonySize()));
        megaColonyCheckBox.setSelected(s.formsMegaColonies());
        flyCheckBox.setSelected(s.isWorkersCanFly());

        nestTypeCombo.getSelectionModel().select(s.getNestType());
        optTempField.setText(String.valueOf(s.getOptimalTempCelsius()));
        minTempField.setText(String.valueOf(s.getMinTempCelsius()));
        maxTempField.setText(String.valueOf(s.getMaxTempCelsius()));
        aggressionSlider.setValue(s.getAggression());
        territorialitySlider.setValue(s.getTerritoriality());
        venomCombo.getSelectionModel().select(s.getVenomType());

        // Castes
        casteRows.clear();
        if (s.getCasteTemplates() != null) {
            for (CasteTemplate ct : s.getCasteTemplates()) {
                double body = ct.getBodyLengthMm() > 0 ? ct.getBodyLengthMm() : ct.getAttribute("size_mm", 5.0f);
                double head = ct.getHeadWidthMm() > 0 ? ct.getHeadWidthMm() : (body * 0.25);
                casteRows.add(new CasteRow(ct.getName(), body, head, ct.getLifespan(), ct.getBaseHealth(), ct.getBaseDamage(), ct.isCanFly()));
            }
        }
    }

    private CustomSpecies buildSpeciesFromUI() {
        CustomSpecies s = new CustomSpecies();
        s.setPresetName(presetNameField.getText().trim().isEmpty() ? commonNameField.getText() : presetNameField.getText().trim());

        s.setCommonName(commonNameField.getText());
        s.setScientificName(scientificNameField.getText());
        s.setInsectType(insectTypeCombo.getValue());
        s.setCategory(categoryCombo.getValue() != null ? categoryCombo.getValue() : org.swarmforge.core.species.SpeciesCategory.EUSOCIAL_PRIMARY);
        s.setDescription(descriptionArea.getText());

        s.setQueenCountMode(queenModeCombo.getValue());
        s.setQueenCount(queenCountSpinner.getValue());
        s.setQueenLifespan(parseInt(queenLifespanField.getText(), 25000));
        s.setQueenEggLayingRate(parseFloat(queenEggRateField.getText(), 15.0f));
        s.setHasKing(hasKingCheckBox.isSelected());
        s.setKingLifespan(parseInt(kingLifespanField.getText(), 15000));
        s.setNuptialFlightType(nuptialFlightCombo.getValue());

        s.setEggStageDuration(parseInt(eggDurationField.getText(), 300));
        s.setLarvaStageDuration(parseInt(larvaDurationField.getText(), 600));
        s.setLarvaDietRequirement(larvaDietCombo.getValue());
        s.setPupaStageDuration(parseInt(pupaDurationField.getText(), 500));

        s.setPrimaryDiet(primaryDietCombo.getValue());
        s.setSecondaryDiet(secondaryDietCombo.getValue());
        s.setDailyFoodConsumption(parseFloat(foodConsumptionField.getText(), 0.5f));
        s.setWaterRequirement(parseFloat(waterReqField.getText(), 0.2f));
        s.setWorkerLifespan(parseInt(workerLifespanField.getText(), 5000));
        s.setWorkerSpeed(parseFloat(workerSpeedField.getText(), 0.5f));
        s.setViewDistance(parseFloat(viewDistanceField.getText(), 5.0f));
        s.setTypicalColonySize(parseInt(colonySizeField.getText(), 1000));
        s.setFormsMegaColonies(megaColonyCheckBox.isSelected());
        s.setWorkersCanFly(flyCheckBox.isSelected());

        s.setNestType(nestTypeCombo.getValue());
        s.setOptimalTempCelsius(parseFloat(optTempField.getText(), 24.0f));
        s.setMinTempCelsius(parseFloat(minTempField.getText(), 10.0f));
        s.setMaxTempCelsius(parseFloat(maxTempField.getText(), 38.0f));
        s.setAggression((float) aggressionSlider.getValue());
        s.setTerritoriality((float) territorialitySlider.getValue());
        s.setVenomType(venomCombo.getValue());

        // Castes
        List<CasteTemplate> templates = new ArrayList<>();
        for (CasteRow r : casteRows) {
            CasteTemplate ct = new CasteTemplate(r.getName(), r.getHealth(), r.getDamage());
            ct.setLifespan(r.getLifespan());
            ct.setCanFly(r.isCanFly());
            ct.setBodyLengthMm((float) r.getBodyLengthMm());
            ct.setHeadWidthMm((float) r.getHeadWidthMm());
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

    private Label createWhiteLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold;");
        return l;
    }

    private GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(15));
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
        public double getBodyLengthMm() { return bodyLengthMm; }
        public double getHeadWidthMm() { return headWidthMm; }
        public double getMinTunnelMm() { return Math.max(1.0, Math.round(headWidthMm * 1.4 * 10.0) / 10.0); }
        public int getLifespan() { return lifespan; }
        public float getHealth() { return health; }
        public float getDamage() { return damage; }
        public boolean isCanFly() { return canFly; }
    }
}
