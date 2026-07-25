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
        btnAddPreset.getStyleClass().add("btn-primary");
        btnAddPreset.setOnAction(e -> handleAddPreset());

        Button btnSaveDisk = new Button("Save to Disk...", new FontIcon(Feather.SAVE));
        btnSaveDisk.getStyleClass().add("btn-secondary");
        btnSaveDisk.setOnAction(e -> handleSaveDisk());

        Button btnLoadDisk = new Button("Load from Disk...", new FontIcon(Feather.FOLDER));
        btnLoadDisk.getStyleClass().add("btn-secondary");
        btnLoadDisk.setOnAction(e -> handleLoadDisk());

        Button btnGlossary = new Button("Glossaire & Aide", new FontIcon(Feather.HELP_CIRCLE));
        btnGlossary.getStyleClass().add("btn-info");
        btnGlossary.setStyle("-fx-background-color: #0d6efd; -fx-text-fill: white; -fx-font-weight: bold;");
        btnGlossary.setOnAction(e -> showGlossaryDialog());

        Button btnApply = new Button("Apply to World", new FontIcon(Feather.CHECK));
        btnApply.getStyleClass().add("btn-primary");
        btnApply.setOnAction(e -> {
            CustomSpecies s = buildSpeciesFromUI();
            if (onApplyListener != null) {
                onApplyListener.accept(s);
            } else {
                new Alert(Alert.AlertType.INFORMATION, "Species '" + s.getCommonName() + "' active!").show();
            }
        });

        bar.getChildren().addAll(lblPreset, presetCombo, lblName, presetNameField, btnAddPreset, btnSaveDisk, btnLoadDisk, btnGlossary, btnApply);
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

        Tab tabSensors = new Tab("Sensors & Perception", createSensorsPane());
        tabSensors.setGraphic(new FontIcon(Feather.EYE));

        List<Tab> tabs = List.of(tabTaxonomy, tabQueens, tabStages, tabCastes, tabDiet, tabNest, tabSensors);
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

    // --- Tab 3: Development Stages & Caste Transition Matrix ---
    private ScrollPane createStagesPane() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(15));

        VBox cardDurations = new VBox(10);
        cardDurations.getStyleClass().add("card-pane");
        Label titleDurations = new Label("⏱ Durées de Métamorphose par Stade");
        titleDurations.getStyleClass().add("card-title");

        GridPane gridDurations = createGrid();
        eggDurationField = new TextField("300");
        larvaDurationField = new TextField("600");
        larvaDietCombo = new ComboBox<>(FXCollections.observableArrayList("HIGH_PROTEIN_MEAT", "SUGAR_HONEY", "FUNGUS", "CELLULOSE", "SEEDS", "OMNIVORE"));
        larvaDietCombo.getSelectionModel().select("HIGH_PROTEIN_MEAT");
        pupaDurationField = new TextField("500");

        gridDurations.addRow(0, createWhiteLabel("Durée du stade Œuf (ticks):"), eggDurationField);
        gridDurations.addRow(1, createWhiteLabel("Durée du stade Larve (ticks):"), larvaDurationField);
        gridDurations.addRow(2, createWhiteLabel("Régime alimentaire des Larves:"), larvaDietCombo);
        gridDurations.addRow(3, createWhiteLabel("Durée du stade Nymphe/Cocon (ticks):"), pupaDurationField);
        cardDurations.getChildren().addAll(titleDurations, gridDurations);

        VBox cardMatrix = new VBox(10);
        cardMatrix.getStyleClass().add("card-pane");
        Label titleMatrix = new Label("📊 Matrice de Détermination des Castes (Nutrition & Phéromones)");
        titleMatrix.getStyleClass().add("card-title");

        GridPane gridMatrix = createGrid();
        TextField proteinMinorF = new TextField("0.35");
        TextField proteinMajorF = new TextField("0.70");
        TextField proteinSoldierF = new TextField("0.85");
        TextField proteinQueenF = new TextField("0.95");
        Slider pheroInhibSlider = new Slider(0.0, 1.0, 0.8);
        pheroInhibSlider.setShowTickLabels(true); pheroInhibSlider.setShowTickMarks(true);
        CheckBox haplodiploidyCheck = new CheckBox("Arrhénotokie / Haplodiploïdie (Œuf non-fécondé = Mâle)");
        haplodiploidyCheck.setSelected(true);

        gridMatrix.addRow(0, createWhiteLabel("Seuil Protéique Ouvrière Minor (%):"), proteinMinorF);
        gridMatrix.addRow(1, createWhiteLabel("Seuil Protéique Ouvrière Major (%):"), proteinMajorF);
        gridMatrix.addRow(2, createWhiteLabel("Seuil Protéique Soldat (%):"), proteinSoldierF);
        gridMatrix.addRow(3, createWhiteLabel("Seuil Protéique Nourriture Royale (%):"), proteinQueenF);
        gridMatrix.addRow(4, createWhiteLabel("Inhibition Phéromonale Reine:"), pheroInhibSlider);
        gridMatrix.addRow(5, createWhiteLabel("Détermination des Mâles:"), haplodiploidyCheck);
        cardMatrix.getChildren().addAll(titleMatrix, gridMatrix);

        VBox cardImmunity = new VBox(10);
        cardImmunity.getStyleClass().add("card-pane");
        Label titleImmunity = new Label("🦠 Immunité & Défense Sanitaire (Allogrooming & Hygiène)");
        titleImmunity.getStyleClass().add("card-title");

        GridPane gridImmunity = createGrid();
        Slider pathResistanceSlider = new Slider(0.0, 1.0, 0.5);
        pathResistanceSlider.setShowTickLabels(true); pathResistanceSlider.setShowTickMarks(true);
        Slider groomingSlider = new Slider(0.0, 1.0, 0.7);
        groomingSlider.setShowTickLabels(true); groomingSlider.setShowTickMarks(true);

        gridImmunity.addRow(0, createWhiteLabel("Résistance Immunitaire Pathogènes:"), pathResistanceSlider);
        gridImmunity.addRow(1, createWhiteLabel("Efficacité Toilette Sociale (Grooming):"), groomingSlider);
        cardImmunity.getChildren().addAll(titleImmunity, gridImmunity);

        box.getChildren().addAll(cardDurations, cardMatrix, cardImmunity);
        return wrapScroll(box);
    }

    // --- Tab 4: Castes & Morphology ---
    private ScrollPane createCastesPane() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(15));

        Label infoLabel = new Label("Définition des castes et gabarit (Double-cliquez sur une cellule pour éditer en ligne)");
        infoLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-style: italic;");

        casteTable = new TableView<>(casteRows);
        casteTable.setEditable(true);
        casteTable.setPrefHeight(240);

        TableColumn<CasteRow, String> nameCol = new TableColumn<>("Caste");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(e -> e.getRowValue().setName(e.getNewValue()));
        nameCol.setPrefWidth(130);

        TableColumn<CasteRow, Double> bodyCol = new TableColumn<>("Longueur (mm)");
        bodyCol.setCellValueFactory(new PropertyValueFactory<>("bodyLengthMm"));
        bodyCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new javafx.util.converter.DoubleStringConverter()));
        bodyCol.setOnEditCommit(e -> e.getRowValue().setBodyLengthMm(e.getNewValue()));
        bodyCol.setPrefWidth(95);

        TableColumn<CasteRow, Double> headCol = new TableColumn<>("Tête (mm)");
        headCol.setCellValueFactory(new PropertyValueFactory<>("headWidthMm"));
        headCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new javafx.util.converter.DoubleStringConverter()));
        headCol.setOnEditCommit(e -> e.getRowValue().setHeadWidthMm(e.getNewValue()));
        headCol.setPrefWidth(75);

        TableColumn<CasteRow, Double> tunnelCol = new TableColumn<>("Ø Tunnel Min (mm)");
        tunnelCol.setCellValueFactory(new PropertyValueFactory<>("minTunnelMm"));
        tunnelCol.setPrefWidth(115);

        TableColumn<CasteRow, Integer> lifeCol = new TableColumn<>("Vie (ticks)");
        lifeCol.setCellValueFactory(new PropertyValueFactory<>("lifespan"));
        lifeCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new javafx.util.converter.IntegerStringConverter()));
        lifeCol.setOnEditCommit(e -> e.getRowValue().setLifespan(e.getNewValue()));
        lifeCol.setPrefWidth(85);

        TableColumn<CasteRow, Float> healthCol = new TableColumn<>("Santé");
        healthCol.setCellValueFactory(new PropertyValueFactory<>("health"));
        healthCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new javafx.util.converter.FloatStringConverter()));
        healthCol.setOnEditCommit(e -> e.getRowValue().setHealth(e.getNewValue()));
        healthCol.setPrefWidth(65);

        TableColumn<CasteRow, Float> dmgCol = new TableColumn<>("Attaque");
        dmgCol.setCellValueFactory(new PropertyValueFactory<>("damage"));
        dmgCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new javafx.util.converter.FloatStringConverter()));
        dmgCol.setOnEditCommit(e -> e.getRowValue().setDamage(e.getNewValue()));
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

        casteTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                casteNameF.setText(newVal.getName());
                casteBodyF.setText(String.valueOf(newVal.getBodyLengthMm()));
                casteHeadF.setText(String.valueOf(newVal.getHeadWidthMm()));
                casteLifeF.setText(String.valueOf(newVal.getLifespan()));
                casteHealthF.setText(String.valueOf(newVal.getHealth()));
                casteDmgF.setText(String.valueOf(newVal.getDamage()));
                casteFlyCheck.setSelected(newVal.isCanFly());
            }
        });

        casteForm.addRow(0, createWhiteLabel("Nom Caste:"), casteNameF, createWhiteLabel("Longueur Corps (mm):"), casteBodyF, createWhiteLabel("Largeur Tête (mm):"), casteHeadF);
        casteForm.addRow(1, createWhiteLabel("Durée de vie:"), casteLifeF, createWhiteLabel("Santé de base:"), casteHealthF, createWhiteLabel("Dégâts Attaque:"), casteDmgF);
        casteForm.addRow(2, createWhiteLabel("Capacité Vol:"), casteFlyCheck);

        HBox casteBtns = new HBox(10);
        Button btnAddCaste = new Button("Ajouter / Mettre à jour Caste");
        btnAddCaste.getStyleClass().add("btn-primary");
        btnAddCaste.setOnAction(e -> {
            try {
                double body = Double.parseDouble(casteBodyF.getText());
                double head = Double.parseDouble(casteHeadF.getText());
                CasteRow sel = casteTable.getSelectionModel().getSelectedItem();
                if (sel != null && sel.getName().equalsIgnoreCase(casteNameF.getText())) {
                    sel.setBodyLengthMm(body);
                    sel.setHeadWidthMm(head);
                    sel.setLifespan(Integer.parseInt(casteLifeF.getText()));
                    sel.setHealth(Float.parseFloat(casteHealthF.getText()));
                    sel.setDamage(Float.parseFloat(casteDmgF.getText()));
                    sel.setCanFly(casteFlyCheck.isSelected());
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
                    casteRows.add(row);
                }
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Format de nombre invalide.").show();
            }
        });

        Button btnDelCaste = new Button("Supprimer Sélection");
        btnDelCaste.getStyleClass().add("btn-danger");
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

        nestTypeCombo = new ComboBox<>(FXCollections.observableArrayList(
            "WAX_COMB_HEXAGONAL",
            "WAX_POTS_CLUSTER",
            "PAPER_PEDUNCULATE",
            "CATHEDRAL_MOUND",
            "ARBOREAL_SILK_LEAF",
            "SUBTERRANEAN_FUNGI_VAULT",
            "CARTON_NEST",
            "BAMBOO_STEM_NEST",
            "BIVOUAC_LIVING_NEST",
            "MOUND",
            "TREE",
            "MATURE",
            "SIMPLE"
        ));
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

        venomCombo = new ComboBox<>(FXCollections.observableArrayList("NONE", "FORMIC_ACID", "VENOMOUS_STING", "CHEMICAL_SPRAY", "POWERFUL_MANDIBLES"));
        venomCombo.getSelectionModel().select("FORMIC_ACID");

        grid.addRow(0, createTooltipLabel("Type de Nid Spécifique (NestType):", "Sélectionne l'architecture géométrique préférentielle construite par cette espèce (ex: cire hexagonale, cathédrale, dôme, soie)."), nestTypeCombo);
        grid.addRow(1, createTooltipLabel("Température Optimale (°C):", "Température interne idéale du nid pour l'incubation du couvain."), optTempField);
        grid.addRow(2, createTooltipLabel("Température Minima (°C):", "Seuil de température au-dessous duquel les individus tombent en léthargie/engourdissement."), minTempField);
        grid.addRow(3, createTooltipLabel("Température Maxima (°C):", "Température critique au-dessus de laquelle survient le choc thermique et la mortalité."), maxTempField);
        grid.addRow(4, createTooltipLabel("Niveau d'Agressivité:", "Propension comportementale à engager le combat contre d'autres colonies."), aggressionSlider);
        grid.addRow(5, createTooltipLabel("Territorialité:", "Intensité de patrouille et défense exclusive de la zone d'influence autour du nid."), territorialitySlider);
        grid.addRow(6, createTooltipLabel("Arme / Type de Venin:", "Système défensif ou toxine éjectée (acide formique, venin à aiguillon, mandibules)."), venomCombo);

        return wrapScroll(grid);
    }

    // --- Tab 7: Sensory Systems & Motor Biomechanics ---
    private ScrollPane createSensorsPane() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(15));

        // 1. Sensory Card
        VBox cardSensors = new VBox(10);
        cardSensors.getStyleClass().add("card-pane");
        Label titleSensors = new Label("📡 Systèmes Sensoriels & Perception Environnementale (SI)");
        titleSensors.getStyleClass().add("card-title");

        GridPane gridSensors = createGrid();

        hasMagnetoreceptionCheckBox = new CheckBox("Magnétoréception (Champ magnétique terrestre - Termites / Fourmi boussole)");
        magnetoSensField = new TextField("5.0");
        thermoSensField = new TextField("0.5");
        gasSensField = new TextField("400.0");
        visualAcuityField = new TextField("1.0");
        minLightField = new TextField("0.05");

        hasVibrationSensingCheckBox = new CheckBox("Perception Vibrations du Substrat (Organe de Johnston / Tambourinage alerte)");
        hasVibrationSensingCheckBox.setSelected(true);
        vibrationSensField = new TextField("10.0");

        hasHygroreceptionCheckBox = new CheckBox("Hygroréception (Détection gradient d'humidité relative)");
        hasHygroreceptionCheckBox.setSelected(true);
        hygroSensField = new TextField("2.0");

        hasElectrosensingCheckBox = new CheckBox("Électroréception (Charges électrostatiques atmosphériques & fleurs - Abeilles / Guêpes)");
        electroSensField = new TextField("50.0");

        hasPolarizedLightCheckBox = new CheckBox("Orientation Lumière Polarisée UV (Boussole céleste - Abeilles / Cataglyphis)");

        gridSensors.addRow(0, createWhiteLabel("Magnétoréception:"), hasMagnetoreceptionCheckBox);
        gridSensors.addRow(1, createWhiteLabel("Seuil Magnétoréception (µT):"), magnetoSensField);
        gridSensors.addRow(2, createWhiteLabel("Sensibilité Gradient Thermique (°C / K):"), thermoSensField);
        gridSensors.addRow(3, createWhiteLabel("Seuil Détection Gaz CO₂ (ppm):"), gasSensField);
        gridSensors.addRow(4, createWhiteLabel("Acuité Visuelle (Yeux composés):"), visualAcuityField);
        gridSensors.addRow(5, createWhiteLabel("Seuil Éclairement Min. (lux):"), minLightField);
        gridSensors.addRow(6, createWhiteLabel("Sensibilité Vibrations Substrat:"), hasVibrationSensingCheckBox);
        gridSensors.addRow(7, createWhiteLabel("Seuil Vibrations (dB):"), vibrationSensField);
        gridSensors.addRow(8, createWhiteLabel("Hygroréception (Humidité):"), hasHygroreceptionCheckBox);
        gridSensors.addRow(9, createWhiteLabel("Seuil Gradient Humidité (%):"), hygroSensField);
        gridSensors.addRow(10, createWhiteLabel("Capteur Électrostatique:"), hasElectrosensingCheckBox);
        gridSensors.addRow(11, createWhiteLabel("Seuil Électrique (V/m):"), electroSensField);
        gridSensors.addRow(12, createWhiteLabel("Boussole Lumière Polarisée UV:"), hasPolarizedLightCheckBox);

        cardSensors.getChildren().addAll(titleSensors, gridSensors);

        // 2. Motor & Biomechanical Card
        VBox cardMotor = new VBox(10);
        cardMotor.getStyleClass().add("card-pane");
        Label titleMotor = new Label("⚙️ Systèmes Moteurs & Capacité Biomécanique (SI)");
        titleMotor.getStyleClass().add("card-title");

        GridPane gridMotor = createGrid();

        wingbeatHzField = new TextField("200.0");
        hasHoveringCheckBox = new CheckBox("Vol Stationnaire (Hovering flight - Abeilles / Guêpes)");
        maxPayloadRatioField = new TextField("5.0");
        bitingForceMpaField = new TextField("15.0");
        hasAutothysisCheckBox = new CheckBox("Autothysie (Explosion chimique suicidaire de défense)");
        hasAroliaAdhesionCheckBox = new CheckBox("Adhésion Ventouses Arolia (Marche sur parois verticales & plafonds lisses)");
        hasAroliaAdhesionCheckBox.setSelected(true);

        gridMotor.addRow(0, createWhiteLabel("Fréquence Vol Battement Ailes (Hz):"), wingbeatHzField);
        gridMotor.addRow(1, createWhiteLabel("Vol Stationnaire:"), hasHoveringCheckBox);
        gridMotor.addRow(2, createWhiteLabel("Ratio Charge Maximale Transportée (/masse):"), maxPayloadRatioField);
        gridMotor.addRow(3, createWhiteLabel("Force Mandibulaire de Cisaillement (MPa):"), bitingForceMpaField);
        gridMotor.addRow(4, createWhiteLabel("Défense Autothysie Explosive:"), hasAutothysisCheckBox);
        gridMotor.addRow(5, createWhiteLabel("Adhésion Arolia (Verticale/Plafond):"), hasAroliaAdhesionCheckBox);

        cardMotor.getChildren().addAll(titleMotor, gridMotor);

        // 3. Dynamic Plugin Extensibility Card
        VBox cardPlugins = new VBox(10);
        cardPlugins.getStyleClass().add("card-pane");
        Label titlePlugins = new Label("🔌 Attributs d'Extension & Plugins (Extensibilité Dynamique)");
        titlePlugins.getStyleClass().add("card-title");

        GridPane gridPlugins = createGrid();
        TextField customKeyField = new TextField();
        customKeyField.setPromptText("Clé du paramètre (ex: bioluminescence)");
        TextField customValueField = new TextField();
        customValueField.setPromptText("Valeur (ex: 1.5 ou true)");

        Button btnAddCustomAttr = new Button("Ajouter Attribut Plugin");
        btnAddCustomAttr.getStyleClass().add("btn-secondary");
        btnAddCustomAttr.setOnAction(e -> {
            String k = customKeyField.getText().trim();
            String v = customValueField.getText().trim();
            if (!k.isEmpty()) {
                new Alert(Alert.AlertType.INFORMATION, "Attribut d'extension '" + k + "' enregistrable pour l'espèce active.").show();
            }
        });

        gridPlugins.addRow(0, createWhiteLabel("Clé Paramètre Plugin:"), customKeyField);
        gridPlugins.addRow(1, createWhiteLabel("Valeur:"), customValueField);
        gridPlugins.addRow(2, new Label(), btnAddCustomAttr);

        cardPlugins.getChildren().addAll(titlePlugins, gridPlugins);

        box.getChildren().addAll(cardSensors, cardMotor, cardPlugins);
        return wrapScroll(box);
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
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold;");
        if (tooltipText != null && !tooltipText.isEmpty()) {
            Tooltip tt = new Tooltip(tooltipText);
            tt.setStyle("-fx-font-size: 12px; -fx-max-width: 380px; -fx-wrap-text: true;");
            l.setTooltip(tt);
            l.setStyle("-fx-font-weight: bold; -fx-text-fill: #38bdf8; -fx-underline: true; -fx-cursor: hand;");
        }
        return l;
    }

    private Label createWhiteLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold;");
        return l;
    }

    private void showGlossaryDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("📖 Glossaire & Guide de Conception des Espèces Eusociales");
        dialog.setHeaderText("Guide Détaillé des Paramètres Biologiques et Architectures de Nids");

        TabPane tabPane = new TabPane();
        tabPane.setPrefSize(680, 480);

        // Tab 1: Nids & Architectures
        VBox vNest = new VBox(10); vNest.setPadding(new Insets(15));
        vNest.getChildren().add(new Label("🏠 Architectures de Nids Supportées Nativement :"));
        String[][] nestDocs = {
            {"WAX_COMB_HEXAGONAL", "Rayons verticaux de cire à cellules hexagonales 3D parallèles (Abeilles domestiques Apis mellifera)."},
            {"WAX_POTS_CLUSTER", "Grappes de pots sphériques en cire et propolis pour nectar et couvain (Bourdons Bombus)."},
            {"PAPER_PEDUNCULATE", "Nid suspendu en papier mâché rattaché par un pédoncule, enveloppe protectrice (Guêpes & Frelons)."},
            {"CATHEDRAL_MOUND", "Tourelles en ciment salivaire avec puits de ventilation convective et loges royales (Termites)."},
            {"ARBOREAL_SILK_LEAF", "Capsule de feuilles vivantes cousues par de la soie larvaire dans la canopée (Fourmis tisserandes Oecophylla)."},
            {"SUBTERRANEAN_FUNGI_VAULT", "Cavernes souterraines profondes hébergeant des jardins à champignons cultivés (Fourmis Atta/Acromyrmex)."},
            {"CARTON_NEST", "Nid en bois mâché cartonné accroché aux troncs et branches (Fourmis Crematogaster/Azteca)."},
            {"BAMBOO_STEM_NEST", "Nid tubulaire aménagé dans des tiges creuses ou entre-nœuds de bambou (Fourmis Pseudomyrmex)."},
            {"BIVOUAC_LIVING_NEST", "Nid temporaire vivant formé par les corps entrelacés des ouvrières (Fourmis légionnaires Eciton)."},
            {"MOUND", "Dôme parabolique d'aiguilles de pin capteur de chaleur solaire (Fourmis rousses Formica rufa)."},
            {"TREE / DEAD_WOOD", "Galeries excavées dans le bois mort ou le cœur des arbres (Fourmis charpentières Camponotus)."},
            {"MATURE / SIMPLE", "Réseau de galeries souterraines élémentaires ou matures avec chambres d'œufs et de stockage."}
        };
        for (String[] doc : nestDocs) {
            Label titleLbl = new Label("• " + doc[0] + " :");
            titleLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #38bdf8;");
            Label descLbl = new Label(doc[1]);
            descLbl.setWrapText(true);
            vNest.getChildren().add(new HBox(5, titleLbl, descLbl));
        }

        // Tab 2: Reines & Socialité
        VBox vSocial = new VBox(10); vSocial.setPadding(new Insets(15));
        vSocial.getChildren().addAll(
            createGlossaryEntry("Mode de Reine", "MONOGYNE (1 reine unique), POLYGYNE (plusieurs reines réparties), GAMERGATES (ouvrières fécondes reproductrices)."),
            createGlossaryEntry("Roi Reproducteur", "Caractéristique clé des termites chez qui le mâle (roi) vit en permanence aux côtés de la reine."),
            createGlossaryEntry("Essaimage / Vol Nuptial", "AERIAL_SWARM (nuée aérienne), SWARM_DIVISION (division d'essaim abeilles), BUDDING (bouturage de nid), IN_NEST (accouplement interne)."),
            createGlossaryEntry("Inhibition Phéromonale", "Phéromone émise par la reine pour bloquer la différenciation de nouvelles reines au sein du couvain.")
        );

        // Tab 3: Capteurs & Biomécanique
        VBox vSensors = new VBox(10); vSensors.setPadding(new Insets(15));
        vSensors.getChildren().addAll(
            createGlossaryEntry("Magnétoréception (µT)", "Perception du champ magnétique terrestre permettant aux termites d'orienter leurs galeries sans lumière."),
            createGlossaryEntry("Hygroréception (%)", "Détection du gradient d'humidité du sol pour placer le couvain aux zones optimales d'incubation."),
            createGlossaryEntry("Gaz CO₂ (ppm)", "Seuil de dioxyde de carbone déclenchant la percée de puits d'aération contre l'asphyxie (hypercapnie)."),
            createGlossaryEntry("Organe Subgénual (dB)", "Perception des micro-vibrations du substrat pour les signaux d'alarme par tambourinage."),
            createGlossaryEntry("Lumière Polarisée UV", "Boussole céleste UV utilisée par les abeilles et fourmis des déserts (Cataglyphis) pour s'orienter par rapport au soleil."),
            createGlossaryEntry("Force Mandibulaire (MPa)", "Pression de morsure des mandibules pour trancher le bois mort, le papier ou les feuilles."),
            createGlossaryEntry("Autothysis", "Mise à feu suicidaire d'organes sécréteurs libérant une glu défensive toxique (Colobopsis explodens)."),
            createGlossaryEntry("Adhésion Ventouses Arolia", "Coussinets tarsaux adhésifs permettant de grimper sur le verre ou marcher au plafond.")
        );

        Tab tab1 = new Tab("Architectures de Nids", new ScrollPane(vNest));
        Tab tab2 = new Tab("Reines & Structure Sociale", new ScrollPane(vSocial));
        Tab tab3 = new Tab("Sensors & Biomécanique", new ScrollPane(vSensors));

        tabPane.getTabs().addAll(tab1, tab2, tab3);
        dialog.getDialogPane().setContent(tabPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private HBox createGlossaryEntry(String term, String definition) {
        Label t = new Label("• " + term + " : ");
        t.setStyle("-fx-font-weight: bold; -fx-text-fill: #38bdf8; -fx-min-width: 180px;");
        Label d = new Label(definition);
        d.setWrapText(true);
        HBox box = new HBox(5, t, d);
        box.setPadding(new Insets(3, 0, 3, 0));
        return box;
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
    }
}
