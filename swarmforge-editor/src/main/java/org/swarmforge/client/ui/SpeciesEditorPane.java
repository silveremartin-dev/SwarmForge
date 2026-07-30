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

import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.NotificationOverlay;

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
    private TabPane mainTabPane;
    private Tab tabGlossary;

    private ComboBox<String> presetCombo;

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

    private VBox warningBannerBox;
    private Consumer<CustomSpecies> onApplyListener;
    private Consumer<CustomSpecies> onGenerateNestForSpeciesListener;

    public SpeciesEditorPane() {
        setSpacing(15);
        setPadding(new Insets(15));

        // Header Title
        Label headerLabel = new Label("Concepteur d'Espèces Eusociales");
        headerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // 1. Top Action Toolbar (Presets & File Operations)
        HBox topToolbar = createTopToolbar();

        // Warning Banner for parameter inconsistencies
        warningBannerBox = new VBox(8);
        warningBannerBox.setStyle("-fx-background-color: rgba(234, 179, 8, 0.15); -fx-border-color: #eab308; -fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 10px;");
        warningBannerBox.setVisible(false);
        warningBannerBox.setManaged(false);

        // 2. TabPane for Parameter Sections
        TabPane tabPane = createTabPane();

        getChildren().addAll(headerLabel, topToolbar, warningBannerBox, new Separator(), tabPane);

        casteRows.addListener((javafx.collections.ListChangeListener<CasteRow>) c -> validateParameters());

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

    public void setOnGenerateNestForSpecies(Consumer<CustomSpecies> listener) {
        this.onGenerateNestForSpeciesListener = listener;
    }

    private HBox createTopToolbar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10));

        I18nManager i18n = I18nManager.getInstance();

        Label lblPreset = new Label();
        lblPreset.textProperty().bind(i18n.createStringBinding("preset.label"));
        lblPreset.setStyle("-fx-font-weight: bold;");

        presetCombo = new ComboBox<>();
        presetCombo.promptTextProperty().bind(i18n.createStringBinding("preset.prompt"));
        presetCombo.setTooltip(new Tooltip("Sélectionnez une espèce d'insecte eusocial pré-configurée (Lasius, Atta, Apis, Bombus, Vespula, Macrotermes, etc.)."));
        presetCombo.getItems().setAll(presetManager.getPresetNames());
        presetCombo.setPrefWidth(240);
        presetCombo.setOnAction(e -> {
            String sel = presetCombo.getValue();
            if (sel != null && presetManager.contains(sel)) {
                loadPresetToUI(presetManager.getPreset(sel));
            }
        });

        Button btnSave = new Button();
        btnSave.textProperty().bind(i18n.createStringBinding("preset.save"));
        btnSave.getStyleClass().add("btn-secondary");
        btnSave.setTooltip(new Tooltip("Enregistrer la configuration actuelle de l'espèce comme nouveau preset."));
        btnSave.setOnAction(e -> handleAddPreset());

        Button btnDelete = new Button();
        btnDelete.textProperty().bind(i18n.createStringBinding("preset.delete"));
        btnDelete.getStyleClass().add("btn-danger");
        btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold;");
        btnDelete.setTooltip(new Tooltip("Supprimer le preset d'espèce sélectionné."));
        btnDelete.setOnAction(e -> handleDeletePreset());

        Button btnExport = new Button();
        btnExport.textProperty().bind(i18n.createStringBinding("preset.export"));
        btnExport.getStyleClass().add("btn-secondary");
        btnExport.setTooltip(new Tooltip("Exporter les paramètres de l'espèce au format JSON."));
        btnExport.setOnAction(e -> handleSaveDisk());

        Button btnImport = new Button();
        btnImport.textProperty().bind(i18n.createStringBinding("preset.import"));
        btnImport.getStyleClass().add("btn-secondary");
        btnImport.setTooltip(new Tooltip("Importer un fichier JSON de configuration d'espèce."));
        btnImport.setOnAction(e -> handleLoadDisk());

        Button bHelp = new Button("📖 Aide & Glossaire");
        bHelp.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold;");
        bHelp.setTooltip(new Tooltip("Ouvrir le glossaire pédagogique des structures sociales, reines et comportements."));
        bHelp.setOnAction(e -> GlossaryDialog.show("social"));

        bar.getChildren().addAll(lblPreset, presetCombo, btnSave, btnDelete, bHelp, new Separator(Orientation.VERTICAL), btnExport, btnImport);
        return bar;
    }

    private TabPane createTabPane() {
        mainTabPane = new TabPane();
        mainTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab tabTaxonomy = new Tab("Taxonomie & Groupes", createTaxonomyPane());
        tabTaxonomy.setGraphic(new FontIcon(Feather.BOOK));

        Tab tabQueens = new Tab("Colonie & Reines", createQueensPane());
        tabQueens.setGraphic(new FontIcon(Feather.AWARD));

        Tab tabStages = new Tab("Stades de Vie", createStagesPane());
        tabStages.setGraphic(new FontIcon(Feather.CLOCK));

        Tab tabCastes = new Tab("Castes & Morphologie", createCastesPane());
        tabCastes.setGraphic(new FontIcon(Feather.USERS));

        Tab tabDiet = new Tab("Régime & Métabolisme", createDietPane());
        tabDiet.setGraphic(new FontIcon(Feather.FEATHER));

        Tab tabNest = new Tab("Nid & Comportement", createNestPane());
        tabNest.setGraphic(new FontIcon(Feather.HOME));

        Tab tabSensors = new Tab("Capteurs & Perception", createSensorsPane());
        tabSensors.setGraphic(new FontIcon(Feather.EYE));

        List<Tab> tabs = List.of(tabTaxonomy, tabQueens, tabStages, tabCastes, tabDiet, tabNest, tabSensors);
        for (Tab t : tabs) {
            Label tabLabel = new Label(t.getText());
            tabLabel.getStyleClass().add("tab-label");
            t.setText("");
            t.setGraphic(new HBox(5, t.getGraphic(), tabLabel));
        }

        mainTabPane.getTabs().addAll(tabs);
        VBox.setVgrow(mainTabPane, Priority.ALWAYS);
        return mainTabPane;
    }

    // --- Tab 1: Taxonomy ---
    private ScrollPane createTaxonomyPane() {
        GridPane grid = createGrid();

        commonNameField = new TextField("Fourmi Noire des Jardins");
        scientificNameField = new TextField("Lasius niger");

        insectTypeCombo = new ComboBox<>(FXCollections.observableArrayList("ANT", "BEE", "WASP", "TERMITE", "OTHER"));
        ComboBoxTooltipHelper.setupDescriptiveComboBox(insectTypeCombo,
            val -> switch (val) {
                case "ANT" -> "🐜 Formicidae (Fourmis)";
                case "BEE" -> "🐝 Apidae (Abeilles)";
                case "WASP" -> "🐝 Vespidae (Guêpes)";
                case "TERMITE" -> "🐜 Termitoidae (Termites)";
                default -> "🪲 Autre Taxon Eusocial";
            },
            val -> switch (val) {
                case "ANT" -> "Insectes eusociaux formant de vastes colonies avec spécialisation poussée des castes et architecture sous-terraine.";
                case "BEE" -> "Insectes hyménoptères eusociaux produisant du miel, pratiquant la danse des abeilles et logeant en rayons de cire.";
                case "WASP" -> "Hyménoptères eusociaux ou solitaires prédateurs bâtissant des nids en papier/carton mâché à partir de fibres de bois.";
                case "TERMITE" -> "Isoptères eusociaux consommant de la cellulose, organisés en castes aveugles sous la conduite d'un couple royal (Reine + Roi).";
                default -> "Autres arthropodes subsociaux ou eusociaux (ex: Thrips, Pucerons galligènes, Crevettes eusociales).";
            }
        );
        insectTypeCombo.getSelectionModel().select("ANT");

        categoryCombo = new ComboBox<>(FXCollections.observableArrayList(
                org.swarmforge.core.species.SpeciesCategory.EUSOCIAL_PRIMARY,
                org.swarmforge.core.species.SpeciesCategory.EUSOCIAL_POLYGYNE,
                org.swarmforge.core.species.SpeciesCategory.PARASITIC_QUEEN,
                org.swarmforge.core.species.SpeciesCategory.SUBSOCIAL_INCIPIENT
        ));
        ComboBoxTooltipHelper.setupDescriptiveComboBox(categoryCombo,
            cat -> cat != null ? cat.label : "",
            cat -> cat != null ? cat.label : ""
        );
        categoryCombo.getSelectionModel().select(org.swarmforge.core.species.SpeciesCategory.EUSOCIAL_PRIMARY);
        categoryCombo.setOnAction(e -> validateParameters());

        Label categoryHintLabel = new Label("ℹ️ Cet éditeur gère les espèces eusociales (Fourmis, Abeilles, Guêpes, Termites). Les proies, prédateurs et commensaux sont gérés dans l'onglet dédié 'Espèces Associées & Commensaux'.");
        categoryHintLabel.getStyleClass().add("help-entry-desc");
        categoryHintLabel.setStyle("-fx-font-size: 11px; -fx-wrap-text: true;");

        // Taxon links HBox
        HBox taxonLinks = new HBox(10);
        taxonLinks.setAlignment(Pos.CENTER_LEFT);
        Label lblLinksTitle = new Label("Fiches Taxonomiques :");
        lblLinksTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");

        Hyperlink linkFormicidae = new Hyperlink("🐜 Formicidae (Fourmis)");
        linkFormicidae.setOnAction(e -> openWiki("https://fr.wikipedia.org/wiki/Formicidae"));

        Hyperlink linkApidae = new Hyperlink("🐝 Apidae (Abeilles)");
        linkApidae.setOnAction(e -> openWiki("https://fr.wikipedia.org/wiki/Apidae"));

        Hyperlink linkVespidae = new Hyperlink("🐝 Vespidae (Guêpes)");
        linkVespidae.setOnAction(e -> openWiki("https://fr.wikipedia.org/wiki/Vespidae"));

        Hyperlink linkTermitoidae = new Hyperlink("🐜 Termitoidae (Termites)");
        linkTermitoidae.setOnAction(e -> openWiki("https://fr.wikipedia.org/wiki/Termite"));

        taxonLinks.getChildren().addAll(lblLinksTitle, linkFormicidae, linkApidae, linkVespidae, linkTermitoidae);

        descriptionArea = new TextArea("Description de l'espèce...");
        descriptionArea.setPrefRowCount(4);

        grid.addRow(0, createTooltipLabel("Nom Commun (fr/en):", "Nom vernaculaire de l'espèce dans le langage courant.", commonNameField), commonNameField);
        grid.addRow(1, createTooltipLabel("Nom Scientifique (Binomial):", "Nomenclature binomiale latine officielle (ex: Lasius niger, Formica rufa, Atta cephalotes).", scientificNameField), scientificNameField);
        grid.addRow(2, createTooltipLabel("Ordre Taxonomique / Famille:", "Grand groupe taxonomique d'insectes eusociaux (Fourmi, Abeille, Guêpe, Termite).", insectTypeCombo), insectTypeCombo);
        grid.addRow(3, new Label(""), taxonLinks);
        grid.addRow(4, createTooltipLabel("Rôle Écologique / Catégorie:", "Statut trophique et rôle fonctionnel dans l'écosystème de simulation.", categoryCombo), categoryCombo);
        grid.addRow(5, new Label(""), categoryHintLabel);
        grid.addRow(6, createTooltipLabel("Description & Notes Écologiques:", "Résumé descriptif de la biologie, de l'habitat et du comportement de l'espèce.", descriptionArea), descriptionArea);

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

        queenModeCombo = new ComboBox<>(FXCollections.observableArrayList("MONOGYNE", "POLYGYNE", "GAMERGATES"));
        ComboBoxTooltipHelper.setupDescriptiveComboBox(queenModeCombo,
            val -> switch (val) {
                case "MONOGYNE" -> "👑 Monogyne (Une seule Reine)";
                case "POLYGYNE" -> "👑👑 Polygyne (Multiples Reines)";
                case "GAMERGATES" -> "🐜 Gamergates (Ouvrières Reproductrices)";
                default -> val;
            },
            val -> switch (val) {
                case "MONOGYNE" -> "La colonie ne tolère stricte qu'une unique reine féconde. La mort de la reine entraîne le déclin terminal de la colonie.";
                case "POLYGYNE" -> "Plusieurs reines fécondes cohabitent pacifiquement dans le même nid, assurant une ponte massive et une pérennité accrue.";
                case "GAMERGATES" -> "Absence de caste reine morphologique distincte : des ouvrières spécialisées (gamergates) s'accouplent et assurent la ponte.";
                default -> "";
            }
        );
        queenModeCombo.getSelectionModel().select("MONOGYNE");
        queenModeCombo.setOnAction(e -> {
            if ("MONOGYNE".equals(queenModeCombo.getValue())) {
                queenCountSpinner.getValueFactory().setValue(1);
            }
            validateParameters();
        });

        queenCountSpinner = new Spinner<>(1, 500, 1);
        queenCountSpinner.valueProperty().addListener((obs, oldV, newV) -> {
            if ("MONOGYNE".equals(queenModeCombo.getValue()) && newV > 1) {
                queenCountSpinner.getValueFactory().setValue(1);
            }
            validateParameters();
        });

        queenLifespanField = new TextField("25000");
        queenEggRateField = new TextField("25.0");

        hasKingCheckBox = new CheckBox("Présence d'un Roi Reproducteur (Termites)");
        hasKingCheckBox.setOnAction(e -> validateParameters());

        kingLifespanField = new TextField("15000");
        nuptialFlightCombo = new ComboBox<>(FXCollections.observableArrayList("AERIAL_SWARM", "SWARM_DIVISION", "BUDDING", "IN_NEST"));
        ComboBoxTooltipHelper.setupDescriptiveComboBox(nuptialFlightCombo,
            val -> switch (val) {
                case "AERIAL_SWARM" -> "🌤️ Vol Nuptial Aérien";
                case "SWARM_DIVISION" -> "🐝 Essaimage par Division";
                case "BUDDING" -> "🌱 Bouturage de Nid (Sociotomie)";
                case "IN_NEST" -> "🕳️ Accouplement Intranidale";
                default -> val;
            },
            val -> switch (val) {
                case "AERIAL_SWARM" -> "Synchronisation synchrone de princesses et de mâles s'envolant massivement dans les airs lors de conditions météo chaudes et humides.";
                case "SWARM_DIVISION" -> "La reine mère quitte le nid d'origine accompagnée d'une cohorte d'ouvrières pour fonder une nouvelle colonie à proximité.";
                case "BUDDING" -> "Séparation progressive d'un groupe d'ouvrières avec une ou plusieurs reines fertiles vers un nid satellite adjacent.";
                case "IN_NEST" -> "Accouplement des ailés à l'intérieur même du nid d'origine sans vol aérien risqué (fréquent chez les espèces parasites).";
                default -> "";
            }
        );
        nuptialFlightCombo.getSelectionModel().select("AERIAL_SWARM");

        grid.addRow(0, createTooltipLabel("Structure Gynique (Mode Reine):", "Mode d'organisation des reines reproductrices : Monogyne (1 reine), Polygyne (plusieurs reines), ou Gamergates (ouvrières pondeuses).", queenModeCombo, "Monogyne"), queenModeCombo);
        grid.addRow(1, createTooltipLabel("Effectif de Reines Fondatrices (ind):", "Nombre initial ou maximum de reines reproductrices fertiles résidant dans la colonie.", queenCountSpinner), queenCountSpinner);
        grid.addRow(2, createTooltipLabel("Durée de Vie Reine (jours):", "Longévité maximale de la reine avant sénescence naturelle et fin de fertilité.", queenLifespanField), queenLifespanField);
        grid.addRow(3, createTooltipLabel("Taux de Ponte Royale (œufs/j):", "Nombre d'œufs pondus par reine par jour dans des conditions environnementales optimales.", queenEggRateField), queenEggRateField);
        grid.addRow(4, createTooltipLabel("Roi Reproducteur (Isoptera):", "Présence d'un mâle reproducteur permanent (roi) vivant aux côtés de la reine, caractéristique des Termites.", hasKingCheckBox, "King"), hasKingCheckBox);
        grid.addRow(5, createTooltipLabel("Durée de Vie Roi (jours):", "Longévité du roi reproducteur chez les espèces isoptères.", kingLifespanField), kingLifespanField);
        grid.addRow(6, createTooltipLabel("Vol Nuptial / Mode d'Essaimage:", "Stratégie de dispersion et d'accouplement : Essaimage aérien, division d'essaim, bouturage de nid ou accouplement intranidale.", nuptialFlightCombo, "Nuptial"), nuptialFlightCombo);

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
        ComboBoxTooltipHelper.setupDescriptiveComboBox(larvaDietCombo,
            val -> switch (val) {
                case "HIGH_PROTEIN_MEAT" -> "🥩 Protéines & Chasse d'Insectes";
                case "SUGAR_HONEY" -> "🍯 Nectar, Miellat & Sucres";
                case "FUNGUS" -> "🍄 Meule de Champignon Symbiotique";
                case "CELLULOSE" -> "🪵 Cellulose & Bois Mâché";
                case "SEEDS" -> "🌾 Graines Granivores (Pain de Fourmi)";
                case "OMNIVORE" -> "🥗 Régime Opportuniste Omnivore";
                default -> val;
            },
            val -> switch (val) {
                case "HIGH_PROTEIN_MEAT" -> "Régime hautement protéique à base d'insectes proies broyés, indispensable au développement rapide du couvain et des soldats.";
                case "SUGAR_HONEY" -> "Régime liquide riche en glucides (nectar floral, miellat de pucerons, jus de fruits) apportant l'énergie métabolique.";
                case "FUNGUS" -> "Nourriture mycélienne cultivée par la colonie dans des chambres souterraines à partir de substrat végétal mâché (Atta).";
                case "CELLULOSE" -> "Digestats de bois et de fibres végétales dégradés par des protozoaires et bactéries symbiotiques intestinales (Termites).";
                case "SEEDS" -> "Graines récoltées, décortiquées et broyées avec la salive enzymatique pour former le 'pain de fourmi' (Messor).";
                case "OMNIVORE" -> "Alimentation variée combinant miellat, cadavres d'arthropodes, graines et liquides sucrés.";
                default -> "";
            }
        );
        larvaDietCombo.getSelectionModel().select("HIGH_PROTEIN_MEAT");
        pupaDurationField = new TextField("500");

        gridDurations.addRow(0, createTooltipLabel("Durée Stade Œuf (jours):", "Période d'incubation requise avant le premier stade larvaire.", eggDurationField), eggDurationField);
        gridDurations.addRow(1, createTooltipLabel("Durée Stade Larvaire (jours):", "Période de développement et d'alimentation intensive de la larve.", larvaDurationField), larvaDurationField);
        gridDurations.addRow(2, createTooltipLabel("Régime Alimentaire Larvaire:", "Nourriture spécifique apportée par les ouvrières nourrices aux larves en croissance.", larvaDietCombo), larvaDietCombo);
        gridDurations.addRow(3, createTooltipLabel("Durée Stade Nymphal / Cocon (jours):", "Durée de métamorphose au cours de laquelle se forme l'imago adulte.", pupaDurationField), pupaDurationField);
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

        gridMatrix.addRow(0, createTooltipLabel("Seuil Protéique Ouvrière Minor (%):", "Proportion minimale de protéines dans l'alimentation larvaire nécessaire pour différencier une ouvrière minor.", proteinMinorF), proteinMinorF);
        gridMatrix.addRow(1, createTooltipLabel("Seuil Protéique Ouvrière Major (%):", "Proportion de protéines requise pour induire la différenciation d'une ouvrière de grande taille (major).", proteinMajorF), proteinMajorF);
        gridMatrix.addRow(2, createTooltipLabel("Seuil Protéique Soldat (%):", "Proportion de protéines exigeante nécessaire pour la caste des soldats.", proteinSoldierF), proteinSoldierF);
        gridMatrix.addRow(3, createTooltipLabel("Seuil Protéique Nourriture Royale (%):", "Seuil nutritionnel maximal induisant la différenciation en reine féconde.", proteinQueenF), proteinQueenF);
        gridMatrix.addRow(4, createTooltipLabel("Inhibition Phéromonale Reine:", "Effet d'inhibition chimique émis par la reine pour empêcher le développement d'autres reines.", pheroInhibSlider, "Inhibition"), pheroInhibSlider);
        gridMatrix.addRow(5, createTooltipLabel("Détermination des Mâles (Arrhénotokie):", "Arrhénotokie / Haplodiploïdie : les œufs non-fécondés (haploïdes) donnent des mâles, les œufs fécondés (diploïdes) donnent des femelles.", haplodiploidyCheck, "Haplodiploïdie"), haplodiploidyCheck);
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

        gridImmunity.addRow(0, createTooltipLabel("Résistance Immunitaire Pathogènes (%):", "Capacité physiologique de résistance globale aux spores et infections fongiques/bactériennes.", pathResistanceSlider), pathResistanceSlider);
        gridImmunity.addRow(1, createTooltipLabel("Efficacité Toilette Sociale (Grooming %):", "Efficacité du léchage et déparasitage mutuel entre individus pour réduire la charge de germes.", groomingSlider), groomingSlider);
        cardImmunity.getChildren().addAll(titleImmunity, gridImmunity);

        box.getChildren().addAll(cardDurations, cardMatrix, cardImmunity);
        return wrapScroll(box);
    }

    // --- Tab 4: Castes & Morphology ---
    private ScrollPane createCastesPane() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(15));

        Label infoLabel = new Label("💡 Tableau récapitulatif des castes. Cliquez sur une ligne pour inspecter ou modifier l'intégralité de ses paramètres ci-dessous.");
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
        bodyCol.setOnEditCommit(e -> {
            e.getRowValue().setBodyLengthMm(e.getNewValue());
            casteTable.refresh();
        });
        bodyCol.setPrefWidth(95);

        TableColumn<CasteRow, Double> headCol = new TableColumn<>("Tête (mm)");
        headCol.setCellValueFactory(new PropertyValueFactory<>("headWidthMm"));
        headCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new javafx.util.converter.DoubleStringConverter()));
        headCol.setOnEditCommit(e -> {
            e.getRowValue().setHeadWidthMm(e.getNewValue());
            casteTable.refresh();
        });
        headCol.setPrefWidth(75);

        TableColumn<CasteRow, Double> tunnelCol = new TableColumn<>("Ø Tunnel Min (mm)");
        tunnelCol.setCellValueFactory(new PropertyValueFactory<>("minTunnelMm"));
        tunnelCol.setPrefWidth(125);

        TableColumn<CasteRow, Integer> lifeCol = new TableColumn<>("Vie (jours)");
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

        TableColumn<CasteRow, Float> ratioCol = new TableColumn<>("Ratio Cible");
        ratioCol.setCellValueFactory(new PropertyValueFactory<>("targetRatio"));
        ratioCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new javafx.util.converter.FloatStringConverter()));
        ratioCol.setOnEditCommit(e -> e.getRowValue().setTargetRatio(e.getNewValue()));
        ratioCol.setPrefWidth(75);

        TableColumn<CasteRow, String> archCol = new TableColumn<>("Modèle Décision");
        archCol.setCellValueFactory(new PropertyValueFactory<>("decisionArch"));
        archCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
        archCol.setOnEditCommit(e -> e.getRowValue().setDecisionArch(e.getNewValue()));
        archCol.setPrefWidth(110);

        TableColumn<CasteRow, Float> forageCol = new TableColumn<>("Poids Récolte");
        forageCol.setCellValueFactory(new PropertyValueFactory<>("foragingWeight"));
        forageCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new javafx.util.converter.FloatStringConverter()));
        forageCol.setOnEditCommit(e -> e.getRowValue().setForagingWeight(e.getNewValue()));
        forageCol.setPrefWidth(85);

        TableColumn<CasteRow, Float> defCol = new TableColumn<>("Poids Défense");
        defCol.setCellValueFactory(new PropertyValueFactory<>("defenseWeight"));
        defCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new javafx.util.converter.FloatStringConverter()));
        defCol.setOnEditCommit(e -> e.getRowValue().setDefenseWeight(e.getNewValue()));
        defCol.setPrefWidth(85);

        TableColumn<CasteRow, Float> excCol = new TableColumn<>("Poids Excavation");
        excCol.setCellValueFactory(new PropertyValueFactory<>("excavationWeight"));
        excCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new javafx.util.converter.FloatStringConverter()));
        excCol.setOnEditCommit(e -> e.getRowValue().setExcavationWeight(e.getNewValue()));
        excCol.setPrefWidth(95);

        TableColumn<CasteRow, Float> nurseCol = new TableColumn<>("Poids Soins");
        nurseCol.setCellValueFactory(new PropertyValueFactory<>("nursingWeight"));
        nurseCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new javafx.util.converter.FloatStringConverter()));
        nurseCol.setOnEditCommit(e -> e.getRowValue().setNursingWeight(e.getNewValue()));
        nurseCol.setPrefWidth(75);

        TableColumn<CasteRow, String> venomTypeCol = new TableColumn<>("Arme Venin");
        venomTypeCol.setCellValueFactory(new PropertyValueFactory<>("venomType"));
        venomTypeCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
        venomTypeCol.setOnEditCommit(e -> e.getRowValue().setVenomType(e.getNewValue()));
        venomTypeCol.setPrefWidth(110);

        TableColumn<CasteRow, Float> venomToxCol = new TableColumn<>("Toxicité Venin");
        venomToxCol.setCellValueFactory(new PropertyValueFactory<>("venomToxicity"));
        venomToxCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new javafx.util.converter.FloatStringConverter()));
        venomToxCol.setOnEditCommit(e -> e.getRowValue().setVenomToxicity(e.getNewValue()));
        venomToxCol.setPrefWidth(90);

        casteTable.getColumns().addAll(nameCol, bodyCol, headCol, tunnelCol, lifeCol, healthCol, dmgCol, flyCol, ratioCol, archCol, forageCol, defCol, excCol, nurseCol, venomTypeCol, venomToxCol);

        // Controls to add/edit caste (Inspector Panel)
        VBox casteInspectorCard = new VBox(10);
        casteInspectorCard.getStyleClass().add("card-pane");
        Label titleInspector = new Label("🔍 Inspecteur & Éditeur de la Caste sélectionnée");
        titleInspector.getStyleClass().add("card-title");

        GridPane casteForm = createGrid();
        TextField casteNameF = new TextField("Soldat");
        TextField casteBodyF = new TextField("6.0");
        TextField casteHeadF = new TextField("1.8");
        TextField casteLifeF = new TextField("5000");
        TextField casteHealthF = new TextField("120");
        TextField casteDmgF = new TextField("15");
        CheckBox casteFlyCheck = new CheckBox("Volant");

        // Advanced Caste Parameters
        TextField targetRatioF = new TextField("0.25");
        ComboBox<String> decisionArchCombo = new ComboBox<>();
        decisionArchCombo.getItems().addAll("BDI", "NEURAL_NETWORK", "FSM", "BEHAVIOR_TREE", "FUZZY_LOGIC");
        ComboBoxTooltipHelper.setupDescriptiveComboBox(decisionArchCombo, SpeciesEditorPane::getDecisionArchTitle, SpeciesEditorPane::getDecisionArchDescription);
        decisionArchCombo.setValue("BDI");

        TextField foragingWField = new TextField("0.30");
        TextField defenseWField = new TextField("0.20");
        TextField excavationWField = new TextField("0.20");
        TextField nursingWField = new TextField("0.15");

        ComboBox<String> casteVenomCombo = new ComboBox<>();
        casteVenomCombo.getItems().addAll("NONE", "FORMIC_ACID", "SOLENOPSIN", "NEUROTOXIN", "CYTOTOXIN", "TERPENE_RESIN", "AUTOTHYSIS_BOMB", "ACID_SPRAY", "POWERFUL_MANDIBLES");
        ComboBoxTooltipHelper.setupDescriptiveComboBox(casteVenomCombo, SpeciesEditorPane::getVenomTitle, SpeciesEditorPane::getVenomDescription);
        casteVenomCombo.setValue("NONE");
        TextField casteVenomToxField = new TextField("10.0");

        casteTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                casteNameF.setText(newVal.getName());
                casteBodyF.setText(String.valueOf(newVal.getBodyLengthMm()));
                casteHeadF.setText(String.valueOf(newVal.getHeadWidthMm()));
                casteLifeF.setText(String.valueOf(newVal.getLifespan()));
                casteHealthF.setText(String.valueOf(newVal.getHealth()));
                casteDmgF.setText(String.valueOf(newVal.getDamage()));
                casteFlyCheck.setSelected(newVal.isCanFly());

                targetRatioF.setText(String.valueOf(newVal.getTargetRatio()));
                decisionArchCombo.setValue(newVal.getDecisionArch());
                foragingWField.setText(String.valueOf(newVal.getForagingWeight()));
                defenseWField.setText(String.valueOf(newVal.getDefenseWeight()));
                excavationWField.setText(String.valueOf(newVal.getExcavationWeight()));
                nursingWField.setText(String.valueOf(newVal.getNursingWeight()));
                casteVenomCombo.setValue(newVal.getVenomType());
                casteVenomToxField.setText(String.valueOf(newVal.getVenomToxicity()));
            }
        });

        casteForm.addRow(0, createTooltipLabel("Nom Caste:", "Appellation fonctionnelle de la caste au sein de la colonie.", casteNameF), casteNameF, createTooltipLabel("Longueur Corps (mm):", "Longueur totale du corps du sommet de la tête à l'apex de l'abdomen en mm.", casteBodyF), casteBodyF, createTooltipLabel("Largeur Tête (mm):", "Largeur maximale de la capsule céphalique déterminant le diamètre minimal des galeries.", casteHeadF), casteHeadF);
        casteForm.addRow(1, createTooltipLabel("Durée de vie (jours):", "Espérance de vie moyenne des membres de cette caste en jours.", casteLifeF), casteLifeF, createTooltipLabel("Santé de base:", "Points de vie initiaux de la caste.", casteHealthF), casteHealthF, createTooltipLabel("Dégâts Attaque:", "Valeur des dégâts physiques infligés par coup ou morsure.", casteDmgF), casteDmgF);
        casteForm.addRow(2, createTooltipLabel("Ratio Cible (%):", "Pourcentage cible de cette caste dans la colonie", targetRatioF), targetRatioF,
                createTooltipLabel("Modèle Décision:", "Architecture cognitive (BDI, Réseau de Neurones, FSM, Arbre de Comportement, Logique Floue)", decisionArchCombo, "FSM"), decisionArchCombo,
                createTooltipLabel("Capacité Vol:", "Indique si les individus de cette caste sont munis d'ailes et capables de piloter le vol.", casteFlyCheck), casteFlyCheck);
        casteForm.addRow(3, createTooltipLabel("Poids Récolte:", "Poids d'allocation pour le forage", foragingWField), foragingWField,
                createTooltipLabel("Poids Défense:", "Poids d'allocation pour la défense/garde", defenseWField), defenseWField,
                createTooltipLabel("Poids Excavation:", "Poids d'allocation pour l'excavation", excavationWField), excavationWField);
        casteForm.addRow(4, createTooltipLabel("Poids Soins:", "Poids d'allocation pour le couvain", nursingWField), nursingWField,
                createTooltipLabel("Arme Venin:", "Type de venin ou projection chimique spécifique à la caste", casteVenomCombo), casteVenomCombo,
                createTooltipLabel("Toxicité Venin:", "Dégâts ou effet toxique par action de venin", casteVenomToxField), casteVenomToxField);

        HBox casteBtns = new HBox(10);
        Button btnAddCaste = new Button("Ajouter / Mettre à jour Caste", new FontIcon(Feather.PLUS_CIRCLE));
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
                    sel.setTargetRatio(Float.parseFloat(targetRatioF.getText()));
                    sel.setDecisionArch(decisionArchCombo.getValue());
                    sel.setForagingWeight(Float.parseFloat(foragingWField.getText()));
                    sel.setDefenseWeight(Float.parseFloat(defenseWField.getText()));
                    sel.setExcavationWeight(Float.parseFloat(excavationWField.getText()));
                    sel.setNursingWeight(Float.parseFloat(nursingWField.getText()));
                    sel.setVenomType(casteVenomCombo.getValue());
                    sel.setVenomToxicity(Float.parseFloat(casteVenomToxField.getText()));
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
                    row.setVenomType(casteVenomCombo.getValue());
                    row.setVenomToxicity(Float.parseFloat(casteVenomToxField.getText()));
                    casteRows.add(row);
                }
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Format de nombre invalide.").show();
            }
        });

        Button btnDelCaste = new Button("Supprimer la caste sélectionnée", new FontIcon(Feather.TRASH_2));
        btnDelCaste.getStyleClass().add("btn-danger");
        btnDelCaste.setOnAction(e -> {
            CasteRow sel = casteTable.getSelectionModel().getSelectedItem();
            if (sel == null) {
                new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner une caste dans le tableau avant de la supprimer.").show();
                return;
            }
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmation de suppression");
            confirmAlert.setHeaderText("Supprimer la caste : " + sel.getName());
            confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette caste ? Cette action est irréversible.");
            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    casteRows.remove(sel);
                }
            });
        });

        casteBtns.getChildren().addAll(btnAddCaste, btnDelCaste);
        casteInspectorCard.getChildren().addAll(titleInspector, casteForm, casteBtns);

        box.getChildren().addAll(infoLabel, casteTable, casteInspectorCard);
        return wrapScroll(box);
    }

    // --- Tab 5: Diet & Metabolism ---
    private ScrollPane createDietPane() {
        GridPane grid = createGrid();

        primaryDietCombo = new ComboBox<>(FXCollections.observableArrayList("SUGARS_NECTAR", "INSECTS_MEAT", "SEEDS", "FUNGUS", "WOOD_CELLULOSE", "HONEYDEW", "OMNIVORE"));
        ComboBoxTooltipHelper.setupDescriptiveComboBox(primaryDietCombo, SpeciesEditorPane::getDietTitle, SpeciesEditorPane::getDietDescription);
        primaryDietCombo.getSelectionModel().select("HONEYDEW");

        secondaryDietCombo = new ComboBox<>(FXCollections.observableArrayList("NONE", "INSECTS_MEAT", "SUGARS_NECTAR", "SEEDS"));
        ComboBoxTooltipHelper.setupDescriptiveComboBox(secondaryDietCombo, SpeciesEditorPane::getDietTitle, SpeciesEditorPane::getDietDescription);
        secondaryDietCombo.getSelectionModel().select("INSECTS_MEAT");

        foodConsumptionField = new TextField("0.5");
        waterReqField = new TextField("0.2");
        workerLifespanField = new TextField("6000");
        workerSpeedField = new TextField("0.5");
        viewDistanceField = new TextField("5.0");
        colonySizeField = new TextField("15000");

        megaColonyCheckBox = new CheckBox("Forme des Supercolonies (Agglomération de nids)");

        flyCheckBox = new CheckBox("Ouvrières capables de voler (Abeilles / Guêpes)");

        grid.addRow(0, createTooltipLabel("Nourriture Principale:", "Source trophique primaire consommée pour l'énergie métabolique de la colonie."), primaryDietCombo);
        grid.addRow(1, createTooltipLabel("Nourriture Secondaire:", "Source trophique complémentaire (ex: apport protéique en période de couvain)."), secondaryDietCombo);
        grid.addRow(2, createTooltipLabel("Consommation Métabolique (g/ind/j):", "Masse de nourriture consommée quotidiennement par un individu adulte."), foodConsumptionField);
        grid.addRow(3, createTooltipLabel("Besoin Hydrique (mL/ind/j):", "Volume d'eau nécessaire quotidiennement pour maintenir l'hydratation et le métabolisme."), waterReqField);
        grid.addRow(4, createTooltipLabel("Espérance de Vie Ouvrière (jours):", "Durée de vie moyenne des ouvrières adultes en l'absence de traumatisme."), workerLifespanField);
        grid.addRow(5, createTooltipLabel("Vitesse Maximale Déplacement (cm/s):", "Vitesse de marche au sol dans des conditions idéales de terrain."), workerSpeedField);
        grid.addRow(6, createTooltipLabel("Rayon Détection Visuelle (cm):", "Distance maximale de perception visuelle des objets, ennemis et nourriture."), viewDistanceField);
        grid.addRow(7, createTooltipLabel("Population Colonie Mature (ind):", "Taille moyenne de la population d'une colonie mature à l'équilibre écologique."), colonySizeField);
        grid.addRow(8, createTooltipLabel("Supercolonies (Unicolonialité):", "Capacité à former un réseau de nids inter-connectés sans agressivité intra-spécifique."), megaColonyCheckBox);
        grid.addRow(9, createTooltipLabel("Capacité de Vol Ouvrières:", "Indique si les ouvrières adultes possèdent des ailes fonctionnelles pour le vol (ex: Abeilles, Guêpes)."), flyCheckBox);

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

        venomCombo = new ComboBox<>(FXCollections.observableArrayList("NONE", "FORMIC_ACID", "VENOMOUS_STING", "CHEMICAL_SPRAY", "POWERFUL_MANDIBLES"));
        ComboBoxTooltipHelper.setupDescriptiveComboBox(venomCombo, SpeciesEditorPane::getVenomTitle, SpeciesEditorPane::getVenomDescription);
        venomCombo.getSelectionModel().select("FORMIC_ACID");

        optTempField.textProperty().addListener((obs, o, n) -> validateParameters());
        minTempField.textProperty().addListener((obs, o, n) -> validateParameters());
        maxTempField.textProperty().addListener((obs, o, n) -> validateParameters());

        Button btnGenerateSpeciesNest = new Button("📐 Générer & Prévisualiser le Nid pour cette Espèce", new FontIcon(Feather.HOME));
        btnGenerateSpeciesNest.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");
        btnGenerateSpeciesNest.setOnAction(e -> {
            CustomSpecies s = buildSpeciesFromUI();
            if (onGenerateNestForSpeciesListener != null) {
                onGenerateNestForSpeciesListener.accept(s);
            } else {
                new Alert(Alert.AlertType.INFORMATION, "Espèce active configurée pour la génération de nid : " + s.getCommonName()).show();
            }
        });

        grid.addRow(0, createTooltipLabel("Type de Nid Spécifique (NestType):", "Sélectionne l'architecture géométrique préférentielle construite par cette espèce (ex: cire hexagonale, cathédrale, dôme, soie)."), nestTypeCombo);
        grid.addRow(1, createTooltipLabel("Température Optimale (°C):", "Température interne idéale du nid pour l'incubation du couvain."), optTempField);
        grid.addRow(2, createTooltipLabel("Température Minima (°C):", "Seuil de température au-dessous duquel les individus tombent en léthargie/engourdissement."), minTempField);
        grid.addRow(3, createTooltipLabel("Température Maxima (°C):", "Température critique au-dessus de laquelle survient le choc thermique et la mortalité."), maxTempField);
        grid.addRow(4, createTooltipLabel("Niveau d'Agressivité:", "Propension comportementale à engager le combat contre d'autres colonies."), aggressionSlider);
        grid.addRow(5, createTooltipLabel("Territorialité:", "Intensité de patrouille et défense exclusive de la zone d'influence autour du nid."), territorialitySlider);
        grid.addRow(6, createTooltipLabel("Arme / Type de Venin:", "Système défensif ou toxine éjectée (acide formique, venin à aiguillon, mandibules)."), venomCombo);
        grid.addRow(7, createTooltipLabel("Génération de Nid Synchronisée:", "Ouvre le Générateur de Nids et pré-configure l'architecture, le matériau et la distribution des chambres pour cette espèce."), btnGenerateSpeciesNest);

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
            warnings.add("Structure Gynique Monogyne : une seule reine reproductrice est autorisée par colonie (valeur actuelle : " + qCount + ").");
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
                warnings.add("Roi Reproducteur activé (Isoptera), mais aucune caste 'Roi' n'est définie dans le tableau des castes.");
            }
        }

        // 3. Category non-eusocial warning
        org.swarmforge.core.species.SpeciesCategory cat = categoryCombo != null ? categoryCombo.getValue() : null;
        if (cat != null && cat != org.swarmforge.core.species.SpeciesCategory.EUSOCIAL_PRIMARY) {
            warnings.add("Catégorie " + cat.label + " sélectionnée. Remarque : les proies, prédateurs et commensaux doivent être configurés dans l'onglet dédié 'Proies & Prédateurs'.");
        }

        // 4. Temperature consistency
        try {
            float optT = parseFloat(optTempField.getText(), 24.0f);
            float minT = parseFloat(minTempField.getText(), 10.0f);
            float maxT = parseFloat(maxTempField.getText(), 38.0f);
            if (minT >= maxT) {
                warnings.add("Plage thermique invalide : la température minimale (" + minT + "°C) doit être inférieure à la maximale (" + maxT + "°C).");
            } else if (optT < minT || optT > maxT) {
                warnings.add("Température optimale (" + optT + "°C) hors des limites [Min: " + minT + "°C, Max: " + maxT + "°C].");
            }
        } catch (Exception ignored) {}

        // Update warning banner
        warningBannerBox.getChildren().clear();
        if (warnings.isEmpty()) {
            warningBannerBox.setVisible(false);
            warningBannerBox.setManaged(false);
        } else {
            warningBannerBox.setVisible(true);
            warningBannerBox.setManaged(true);

            Label titleLabel = new Label("⚠️ Avertissements de cohérence des paramètres :");
            titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #f59e0b; -fx-font-size: 13px;");
            warningBannerBox.getChildren().add(titleLabel);

            for (String w : warnings) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);

                Label wLbl = new Label("• " + w);
                wLbl.setStyle("-fx-text-fill: #fbbf24; -fx-font-size: 12px;");
                wLbl.setWrapText(true);

                if (w.contains("caste 'Roi'")) {
                    Button btnAddKing = new Button("➕ Ajouter la caste 'Roi' (Isoptera)", new FontIcon(Feather.PLUS_CIRCLE));
                    btnAddKing.setStyle("-fx-background-color: #d97706; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
                    btnAddKing.setOnAction(e -> {
                        CasteRow roiRow = new CasteRow("Roi Reproducteur", 8.0, 2.2, 15000, 150, 5, false);
                        casteRows.add(roiRow);
                        validateParameters();
                    });
                    row.getChildren().addAll(wLbl, btnAddKing);
                } else if (w.contains("Monogyne")) {
                    Button btnFixMonogyne = new Button("🔧 Ajuster à 1 reine", new FontIcon(Feather.CHECK));
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

        gridSensors.addRow(0, createTooltipLabel("Magnétoréception (Champ Terrestre):", "Capacité à percevoir les lignes du champ magnétique terrestre pour l'orientation.", hasMagnetoreceptionCheckBox, "Magnétoréception"), hasMagnetoreceptionCheckBox);
        gridSensors.addRow(1, createTooltipLabel("Seuil Détection Champ Magnétique (µT):", "Sensibilité minimale du récepteur magnétique en micro-Teslas.", magnetoSensField), magnetoSensField);
        gridSensors.addRow(2, createTooltipLabel("Sensibilité Thermique (Δ°C/mm):", "Capacité à détecter des gradients de température pour la thermorégulation.", thermoSensField), thermoSensField);
        gridSensors.addRow(3, createTooltipLabel("Seuil Détection CO₂ Nodal (ppm):", "Seuil de détection du dioxyde de carbone pour la ventilation du nid en parties par million.", gasSensField), gasSensField);
        gridSensors.addRow(4, createTooltipLabel("Acuité Visuelle Ommatediale (0-1):", "Résolution visuelle relative fournie par les ommatidies des yeux composés.", visualAcuityField), visualAcuityField);
        gridSensors.addRow(5, createTooltipLabel("Seuil Luminosité Minimale (lux):", "Niveau de lumière minimal permettant la vision nocturne ou crépusculaire.", minLightField), minLightField);
        gridSensors.addRow(6, createTooltipLabel("Perception Vibrations Substrat (Subgenual):", "Sensibilité aux vibrations sismiques et mécaniques transmises par le sol.", hasVibrationSensingCheckBox, "Subgenual"), hasVibrationSensingCheckBox);
        gridSensors.addRow(7, createTooltipLabel("Seuil Vibration Substrat (dB):", "Intensité minimale des vibrations mesurable par l'organe subgénual en décibels.", vibrationSensField), vibrationSensField);
        gridSensors.addRow(8, createTooltipLabel("Hygroréception (Humidité Relative):", "Capacité à percevoir les gradients d'humidité atmosphérique et du sol.", hasHygroreceptionCheckBox), hasHygroreceptionCheckBox);
        gridSensors.addRow(9, createTooltipLabel("Sensibilité Humidité Relative (%):", "Gradient d'humidité minimum détectable par les récepteurs antennaires en pourcentage.", hygroSensField), hygroSensField);
        gridSensors.addRow(10, createTooltipLabel("Champ Électrostatique Mimétique:", "Sensibilité aux charges électrostatiques atmosphériques et florales.", hasElectrosensingCheckBox), hasElectrosensingCheckBox);
        gridSensors.addRow(11, createTooltipLabel("Seuil Électrique Atmosphérique (V/m):", "Seuil de détection du champ électrique en Volts par mètre.", electroSensField), electroSensField);
        gridSensors.addRow(12, createTooltipLabel("Boussole Lumière Polarisée UV:", "Utilisation du motif de polarisation UV du ciel pour la navigation spatiale rétrospective.", hasPolarizedLightCheckBox, "UV"), hasPolarizedLightCheckBox);

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

        gridMotor.addRow(0, createTooltipLabel("Fréquence Battement d'Ailes (Hz):", "Fréquence d'oscillation des ailes en Hertz pour les espèces volantes.", wingbeatHzField), wingbeatHzField);
        gridMotor.addRow(1, createTooltipLabel("Vol Stationnaire (Hovering):", "Capacité à maintenir une position immobile en vol battu.", hasHoveringCheckBox), hasHoveringCheckBox);
        gridMotor.addRow(2, createTooltipLabel("Capacité Portance Mécanique (g/g):", "Multiplicateur de charge transportable rapporté au propre poids du corps.", maxPayloadRatioField), maxPayloadRatioField);
        gridMotor.addRow(3, createTooltipLabel("Force Mandibulaire Cisaillement (MPa):", "Pression maximale exercée par les mandibules en MégaPascals.", bitingForceMpaField, "Mandibule"), bitingForceMpaField);
        gridMotor.addRow(4, createTooltipLabel("Défense Autothysie Explosive:", "Capacité de défense suicidaire par rupture de la paroi abdominale et sécrétion engluante.", hasAutothysisCheckBox, "Autothysie"), hasAutothysisCheckBox);
        gridMotor.addRow(5, createTooltipLabel("Adhésion Ventouses Arolia (Plafond):", "Présence de ventouses arolia sous les tarses pour la marche sur parois verticales et plafonds lisses.", hasAroliaAdhesionCheckBox, "Arolia"), hasAroliaAdhesionCheckBox);

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
        String name = commonNameField != null ? commonNameField.getText().trim() : "";
        if (name.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Veuillez spécifier un nom commun dans l'onglet Taxonomie.").show();
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

    private void handleDeletePreset() {
        String selected = presetCombo.getValue();
        if (selected == null || selected.isEmpty()) return;

        I18nManager i18n = I18nManager.getInstance();
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(i18n.get("preset.delete.title"));
        confirmAlert.setHeaderText("Supprimer l'Espèce");
        confirmAlert.setContentText(String.format(i18n.get("preset.delete.confirm"), selected));

        confirmAlert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                presetManager.delete(selected);
                presetCombo.getItems().setAll(presetManager.getPresetNames());
                if (!presetCombo.getItems().isEmpty()) {
                    presetCombo.getSelectionModel().selectFirst();
                } else {
                    presetCombo.getSelectionModel().clearSelection();
                }
                NotificationOverlay.show(this, "Preset espèce supprimé.", NotificationOverlay.NotificationType.INFO);
            }
        });
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
                casteRows.add(row);
            }
        }
        validateParameters();
    }

    private CustomSpecies buildSpeciesFromUI() {
        CustomSpecies s = new CustomSpecies();
        s.setPresetName(commonNameField != null ? commonNameField.getText().trim() : "");

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
            l.setStyle("-fx-font-weight: bold; -fx-text-fill: #38bdf8; -fx-underline: true; -fx-cursor: hand;");
            l.setOnMouseClicked(e -> GlossaryDialog.show(glossaryTerm));
        } else {
            l.setStyle("-fx-font-weight: bold;");
        }
        return l;
    }

    private Label createWhiteLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold;");
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
        t.setStyle("-fx-font-weight: bold; -fx-text-fill: #38bdf8; -fx-min-width: 200px;");
        Label d = new Label(description);
        d.setWrapText(true);
        HBox row = new HBox(5, t, d);
        row.setPadding(new Insets(4, 0, 4, 0));
        box.getChildren().add(row);
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
    }

    public static String getDietTitle(String diet) {
        if (diet == null) return "";
        return switch (diet) {
            case "SUGARS_NECTAR" -> "🍯 Nectar & Liquides Sucrés";
            case "INSECTS_MEAT" -> "🥩 Proies Protéiques / Insectes";
            case "SEEDS" -> "🌾 Graines (Granivorie / Pain de Fourmi)";
            case "FUNGUS" -> "🍄 Champignons Symbiotiques";
            case "WOOD_CELLULOSE" -> "🪵 Cellulose & Fibres de Bois";
            case "HONEYDEW" -> "💧 Miellat d'Homoptères (Pucerons)";
            case "OMNIVORE" -> "🥗 Omnivore Polyphage";
            case "NONE" -> "🚫 Aucun Régime Secondaire";
            default -> diet;
        };
    }

    public static String getDietDescription(String diet) {
        if (diet == null) return "";
        return switch (diet) {
            case "SUGARS_NECTAR" -> "Apport en glucides simples fournissant l'énergie métabolique directe pour l'activité quotidienne des adultes.";
            case "INSECTS_MEAT" -> "Protéines animales indispensables au développement des larves en croissance et à la ponte de la reine.";
            case "SEEDS" -> "Stockage et broyage de graines végétales riches en amidon pour la constitution de réserves sur-saisonnières.";
            case "FUNGUS" -> "Culture de champignons basidiomycètes sur compost de matière végétale mâchée dans des chambres dédiées.";
            case "WOOD_CELLULOSE" -> "Digestion de fibres de bois grâce aux protozoaires et bactéries symbiotiques digestives (Termites).";
            case "HONEYDEW" -> "Exploitation de pucerons et cochenilles en trophobiose pour la récolte régulière d'excrétions sucrées.";
            case "OMNIVORE" -> "Alimentation opportuniste s'adaptant à toutes les ressources trophiques disponibles sans spécialisation.";
            case "NONE" -> "Aucune source trophique complémentaire ou secondaire nécessaire.";
            default -> "";
        };
    }

    public static String getNestTypeTitle(String type) {
        if (type == null) return "";
        return switch (type) {
            case "WAX_COMB_HEXAGONAL" -> "🐝 Rayons de Cire Hexagonaux";
            case "WAX_POTS_CLUSTER" -> "🍯 Grappes de Pots de Cire";
            case "PAPER_PEDUNCULATE" -> "🐝 Nid en Papier Mâché Suspendu";
            case "CATHEDRAL_MOUND" -> "🏰 Termitière Cathédrale";
            case "ARBOREAL_SILK_LEAF" -> "🍃 Nid Arboricole en Feuilles Tissées";
            case "SUBTERRANEAN_FUNGI_VAULT" -> "🍄 Nid Souterrain à Champignonnières";
            case "CARTON_NEST" -> "📦 Nid Arboricole en Carton Mâché";
            case "BAMBOO_STEM_NEST" -> "🎋 Nid Cavitaire dans Tiges & Trous";
            case "BIVOUAC_LIVING_NEST" -> "🐜 Bivouac Vivant Structuré";
            case "MOUND" -> "🏔️ Dôme de Terre / Aiguilles";
            case "TREE" -> "🪵 Troncs Creux & Bois Mort";
            case "MATURE" -> "🏛️ Nid Établi / Structuré";
            case "SIMPLE" -> "🕳️ Galeries Souterraines Simples";
            default -> type;
        };
    }

    public static String getNestTypeDescription(String type) {
        if (type == null) return "";
        return switch (type) {
            case "WAX_COMB_HEXAGONAL" -> "Structure alvéolée suspendue bâtie en cire sécrétée par les glandes abdominales des ouvrières (Abeilles).";
            case "WAX_POTS_CLUSTER" -> "Alvéoles sphériques et pots de stockage de miel et de pollen organisés en grappes irrégulières (Bourdons).";
            case "PAPER_PEDUNCULATE" -> "Alvéoles ouvertes en papier cartonné fabriqué à partir de fibres de bois mâchées et salivées (Vespides).";
            case "CATHEDRAL_MOUND" -> "Edifice imposant en terre maçonnée doté de cheminées d'aération régulant la température et le CO2 (Termites).";
            case "ARBOREAL_SILK_LEAF" -> "Feuilles vivantes cousues entre elles au moyen de fils de soie sécrétés par les larves tenues par les ouvrières (Oecophylla).";
            case "SUBTERRANEAN_FUNGI_VAULT" -> "Vaste réseau de cavités souterraines abritant des meules de champignon symbiotique (Fourmis coupe-feuille).";
            case "CARTON_NEST" -> "Structure sphérique arboricole ou cavitaire en carton mâché à base de sciure et de salive sucrée (Lasius fuliginosus).";
            case "BAMBOO_STEM_NEST" -> "Nid opportuniste aménagé dans des cavités préexistantes (tiges creuses de bambou, galles, bois foré).";
            case "BIVOUAC_LIVING_NEST" -> "Nid temporaire constitué exclusivement des corps entrelacés de milliers d'ouvrières (Fourmis Légionnaires).";
            case "MOUND" -> "Monticule souterrain surmonté d'un dôme isolant de terre et d'aiguilles de pin accumulées (Formica rufa).";
            case "TREE" -> "Galeries forées directement dans le bois mort ou l'écorce des troncs d'arbres en décomposition (Camponotus).";
            case "MATURE" -> "Nid souterrain très développé avec multiples réseaux de chambres à couvain, reines et greniers.";
            case "SIMPLE" -> "Nid souterrain rudimentaire composé de quelques galeries sous des pierres ou touffes d'herbe.";
            default -> "";
        };
    }

    public static String getVenomTitle(String v) {
        if (v == null) return "";
        return switch (v) {
            case "NONE" -> "🚫 Sans Venin (Attaque Physique)";
            case "FORMIC_ACID" -> "🧪 Acide Formique (Projection)";
            case "VENOMOUS_STING" -> "🗡️ Aiguillon Venimeux (Dard)";
            case "CHEMICAL_SPRAY" -> "💨 Spray Chimique Répulsif";
            case "POWERFUL_MANDIBLES" -> "✂️ Mandibles Puissantes (Cisaille)";
            case "SOLENOPSIN" -> "🔥 Solénopsine (Alcaloïde Brûlant)";
            case "NEUROTOXIN" -> "🧠 Neurotoxine Paralysante";
            case "CYTOTOXIN" -> "🧫 Cytotoxine Nécrotique";
            case "TERPENE_RESIN" -> "🌲 Résine Terpénique Collante";
            case "AUTOTHYSIS_BOMB" -> "💥 Autothyse (Explosion Suicidaire)";
            default -> v;
        };
    }

    public static String getVenomDescription(String v) {
        if (v == null) return "";
        return switch (v) {
            case "NONE" -> "Pas de venin chimique. Combat exclusivement par morsures de mandibles et lutte mécanique.";
            case "FORMIC_ACID" -> "Projection à distance ou application d'acide formique concentré provoquant des brûlures chimiques corrosives.";
            case "VENOMOUS_STING" -> "Injection directe de venin protéique au moyen d'un dard abdominal rétractile provoquant douleur intense et paralysie.";
            case "CHEMICAL_SPRAY" -> "Pulvérisation d'un spray répulsif ou asphyxiant provoquant la désorientation des assaillants.";
            case "POWERFUL_MANDIBLES" -> "Mandibles hypertrophiées capables d'exercer une pression mécanique létale ou de décapiter les proies.";
            case "SOLENOPSIN" -> "Alcaloïde toxique nécrotique provoquant une douleur de brûlure vive et une pustule locale (Solenopsis invicta).";
            case "NEUROTOXIN" -> "Substance ciblant le système nerveux central des arthropodes pour bloquer la transmission neuromusculaire.";
            case "CYTOTOXIN" -> "Toxine nécrotique détruisant les membranes cellulaires de l'adversaire lors de la piqûre.";
            case "TERPENE_RESIN" -> "Liquide visqueux terpénique expulsé sous pression par la tête des soldats nasutes pour engluer l'ennemi.";
            case "AUTOTHYSIS_BOMB" -> "Contraction musculaire extrême provoquant la rupture de la paroi abdominale et l'explosion d'une colle toxique.";
            default -> "";
        };
    }

    public static String getDecisionArchTitle(String arch) {
        if (arch == null) return "";
        return switch (arch) {
            case "BDI" -> "🧠 Modèle BDI (Belief-Desire-Intention)";
            case "NEURAL_NETWORK" -> "⚡ Réseau de Neurones Artificiels";
            case "FSM" -> "🔄 Automate à États Finis (FSM)";
            case "BEHAVIOR_TREE" -> "🌳 Arbre de Comportements";
            case "FUZZY_LOGIC" -> "🌫️ Logique Floue (Fuzzy Logic)";
            default -> arch;
        };
    }

    public static String getDecisionArchDescription(String arch) {
        if (arch == null) return "";
        return switch (arch) {
            case "BDI" -> "Architecture cognitive avancée gérant des Croyances, Désirs et Intentions pour des stratégies à long terme.";
            case "NEURAL_NETWORK" -> "Réseau de neurones récurrent ou feed-forward transformant les entrées sensorielles en décisions motrices.";
            case "FSM" -> "Machine à états déterministe basculant instantanément entre Récolte, Défense, Soins du couvain et Repos.";
            case "BEHAVIOR_TREE" -> "Arbre hiérarchique de sélecteurs et de séquences très lisible et modulaire.";
            case "FUZZY_LOGIC" -> "Évaluation de conditions floues (ex: 'Faim élevée' AND 'Danger moyen') pour nuancer les comportements.";
            default -> "";
        };
    }
}
