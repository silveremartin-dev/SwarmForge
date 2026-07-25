/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;

/**
 * Dedicated Editor Pane for Accessory Species (Flora, Aphids, Prey Insects, Fungi, Detritivores)
 * with realistic latitude, biome, photoperiod, and seasonal dynamics.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class AccessorySpeciesEditorPane extends VBox {

    // UI Controls
    private ComboBox<String> accessoryPresetCombo;
    private TextField accessoryNameField;
    private ComboBox<String> categoryCombo;
    private ComboBox<String> biomeCombo;
    private TextField latitudeField;

    // Seasonal Multipliers (0.0 to 1.0)
    private Slider springSlider;
    private Slider summerSlider;
    private Slider autumnSlider;
    private Slider winterSlider;

    // Thermal & Growth Parameters
    private TextField minTempField;
    private TextField optTempField;
    private TextField maxTempField;
    private TextField growthRateField;
    private TextField initialBiomassField;
    private CheckBox diapauseCheck;

    public AccessorySpeciesEditorPane() {
        setSpacing(15);
        setPadding(new Insets(15));
        getStyleClass().add("card-pane");

        // Header Title
        Label headerLabel = new Label("🌿 Accessory & Resource Species Editor (Flora, Prey & Mutualists)");
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        // Toolbar
        HBox topToolbar = createToolbar();

        // Content Split: Left (Taxonomy & Thermal), Middle (Seasonal Matrix), Right (Predators & Pathogens)
        HBox contentBox = new HBox(15);
        VBox colLeft = createTaxonomyCard();
        VBox colMid = createSeasonalCard();
        VBox colRight = createPredatorPathogenCard();

        contentBox.getChildren().addAll(colLeft, colMid, colRight);
        HBox.setHgrow(colLeft, Priority.ALWAYS);
        HBox.setHgrow(colMid, Priority.ALWAYS);
        HBox.setHgrow(colRight, Priority.ALWAYS);

        getChildren().addAll(headerLabel, topToolbar, new Separator(), contentBox);
    }

    private HBox createToolbar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);

        Label lblPreset = new Label("Preset Accessory:");
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
        accessoryPresetCombo.getSelectionModel().selectFirst();
        accessoryPresetCombo.setPrefWidth(240);

        Button btnLoad = new Button("Load Preset", new FontIcon(Feather.FOLDER));
        btnLoad.getStyleClass().add("btn-secondary");

        Button btnSave = new Button("Save Preset JSON...", new FontIcon(Feather.SAVE));
        btnSave.getStyleClass().add("btn-primary");
        btnSave.setOnAction(e -> handleSave());

        bar.getChildren().addAll(lblPreset, accessoryPresetCombo, btnLoad, btnSave);
        return bar;
    }

    private VBox createTaxonomyCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card-pane");

        Label title = new Label("Classification & Thermal Tolerance");
        title.getStyleClass().add("card-title");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);

        accessoryNameField = new TextField("Graminées à Graines (Messor)");
        categoryCombo = new ComboBox<>(FXCollections.observableArrayList(
                "FLORA (Plantes & Graines)",
                "APHID_MUTUALIST (Pucerons & Miellat)",
                "PREY_INSECT (Insectes Proies)",
                "PREDATOR (Prédateurs: Spiders, Ant-lions, Birds)",
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
        initialBiomassField = new TextField("150.0");
        diapauseCheck = new CheckBox("Diapause Hivernale Automatique (Engourdissement sous 10°C)");
        diapauseCheck.setSelected(true);

        grid.addRow(0, createLabel("Nom de l'Espèce:"), accessoryNameField);
        grid.addRow(1, createLabel("Catégorie Écologique:"), categoryCombo);
        grid.addRow(2, createLabel("Biome Principal:"), biomeCombo);
        grid.addRow(3, createLabel("Latitude de Référence (°N/°S):"), latitudeField);
        grid.addRow(4, createLabel("Température Min Growth (°C):"), minTempField);
        grid.addRow(5, createLabel("Température Optimum (°C):"), optTempField);
        grid.addRow(6, createLabel("Température Max Growth (°C):"), maxTempField);
        grid.addRow(7, createLabel("Taux de Croissance (g/m²/j):"), growthRateField);
        grid.addRow(8, createLabel("Biomasse Initiale (g/m²):"), initialBiomassField);
        grid.addRow(9, createLabel("Hivernation:"), diapauseCheck);

        card.getChildren().addAll(title, grid);
        return card;
    }

    private VBox createSeasonalCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card-pane");

        Label title = new Label("Profil de Saisonnalité (Activité)");
        title.getStyleClass().add("card-title");

        springSlider = createSlider(0.8);
        summerSlider = createSlider(1.0);
        autumnSlider = createSlider(0.6);
        winterSlider = createSlider(0.1);

        VBox slidersBox = new VBox(12,
                new Label("🌸 Printemps (Mars - Mai):"), springSlider,
                new Label("☀️ Été (Juin - Août):"), summerSlider,
                new Label("🍂 Automne (Septembre - Novembre):"), autumnSlider,
                new Label("❄️ Hiver (Décembre - Février):"), winterSlider
        );

        Label hint = new Label("💡 Ces coefficients pondèrent la reproduction, la germination et la présence des ressources selon la saison.");
        hint.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-wrap-text: true;");

        card.getChildren().addAll(title, slidersBox, new Separator(), hint);
        return card;
    }

    private VBox createPredatorPathogenCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card-pane");

        Label title = new Label("🦅 Prédateurs & 🦠 Pathogènes / Maladies");
        title.getStyleClass().add("card-title");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);

        ComboBox<String> targetCasteCombo = new ComboBox<>(FXCollections.observableArrayList("Ouvrières", "Nymphes / Couvain", "Sexués / Reines", "Toutes Castes"));
        targetCasteCombo.getSelectionModel().selectFirst();

        ComboBox<String> huntModeCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Piège / Entonnoir (Fourmilion)",
                "Embrouille / Affût (Araignée)",
                "Attaque Directe (Oiseau / Tamandua)",
                "Parasitoïde (Ponte interne / Guêpe)"
        ));
        huntModeCombo.getSelectionModel().selectFirst();

        TextField killRateField = new TextField("3.5");

        ComboBox<String> pathogenVectorCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Spores Aériennes (Cordyceps)",
                "Contact Sol & Galerie",
                "Toilette / Allogrooming",
                "Nourriture Contaminée"
        ));
        pathogenVectorCombo.getSelectionModel().selectFirst();

        TextField transmissionR0Field = new TextField("2.4");
        TextField incubationDaysField = new TextField("4.0");
        TextField mortalityRateField = new TextField("15.0");

        grid.addRow(0, createLabel("Caste Cible Prédatée:"), targetCasteCombo);
        grid.addRow(1, createLabel("Mode de Chasse / Attaque:"), huntModeCombo);
        grid.addRow(2, createLabel("Taux de Capture (victimes/j):"), killRateField);
        grid.addRow(3, createLabel("Vecteur Pathogène / Maladie:"), pathogenVectorCombo);
        grid.addRow(4, createLabel("Taux Transmission R0:"), transmissionR0Field);
        grid.addRow(5, createLabel("Incubation (jours):"), incubationDaysField);
        grid.addRow(6, createLabel("Mortalité (%/jour):"), mortalityRateField);

        card.getChildren().addAll(title, grid);
        return card;
    }

    private Slider createSlider(double initialVal) {
        Slider s = new Slider(0.0, 1.0, initialVal);
        s.setShowTickLabels(true);
        s.setShowTickMarks(true);
        s.setMajorTickUnit(0.25);
        return s;
    }

    private Label createLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold;");
        return l;
    }

    private void handleSave() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Sauvegarder l'espèce accessoire");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        chooser.setInitialFileName("swarmforge-accessory-" + accessoryNameField.getText().toLowerCase().replaceAll("[^a-z0-9]+", "-") + ".json");

        File f = chooser.showSaveDialog(getScene().getWindow());
        if (f != null) {
            new Alert(Alert.AlertType.INFORMATION, "Espèce accessoire sauvegardée sous " + f.getName()).show();
        }
    }
}
