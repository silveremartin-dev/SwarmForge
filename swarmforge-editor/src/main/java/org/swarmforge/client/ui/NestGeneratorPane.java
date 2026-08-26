/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.Font;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.NotificationOverlay;

public class NestGeneratorPane extends BorderPane {

    // Canvases
    private Canvas canvas3D, canvasSide, canvasTop;
    private GraphicsContext gc3D, gcSide, gcTop;

    // 3D camera
    private double azimuth = 45, elevation = 35, zoom = 7.5;
    private double pan3DX = 0, pan3DY = 0;
    private double lastMX, lastMY;

    // 2D Side view camera (Zoom & Pan)
    private double sideZoom = 1.0;
    private double sidePanX = 0, sidePanY = 0;
    private double lastSideMX, lastSideMY;

    // 2D Top view camera (Zoom & Pan)
    private double topZoom = 1.0;
    private double topPanX = 0, topPanY = 0;
    private double lastTopMX, lastTopMY;

    // Synchronization control
    private CheckBox syncViewsCheckBox;
    private CheckBox showGhostMeshCheckBox;

    // Controls
    private ComboBox<String> speciesModelCombo;
    private ComboBox<String> nestStageCombo;
    private Label speciesStatusLabel;
    private org.swarmforge.core.species.CustomSpecies activeCustomSpecies;
    private boolean isUpdatingSpeciesCombo = false;

    private ComboBox<String> categorySelect;
    private ComboBox<String> archSelect;
    private ComboBox<String> matSelect;
    private Slider workerSizeSlider;
    private Slider depthSlider, tunnelWidthSlider, branchingSlider;
    private Label lblTotalChambersValue;
    private final Map<String, Spinner<Integer>> chamberSpinners = new LinkedHashMap<>();

    // Placement Evaluator Controls (Embedded Live Diagnostics)
    private Slider evalHeightSlider;
    private ComboBox<String> evalOrientationCombo;
    private Slider evalTempSlider;
    private Slider evalMoistureSlider;
    private Slider evalForagingSlider;
    private Slider evalCompactionSlider;
    private ProgressBar evalScoreProgressBar;
    private Label evalScoreLabel;
    private Label evalBadgeLabel;
    private VBox evalRecommendationsBox;

    // Presets
    private ComboBox<String> presetsCombo;
    private final NestPresetManager presetMgr = new NestPresetManager();
    private boolean isDirty = false;
    private String lastSelectedPreset = null;

    public boolean isDirty() {
        return isDirty;
    }

    public boolean promptUnsavedChanges() {
        if (!isDirty) return true;
        I18nManager i18n = I18nManager.getInstance();
        Alert alert = org.swarmforge.client.util.ThemeManager.createAlert(
            Alert.AlertType.CONFIRMATION,
            "Vous avez des modifications non enregistrées dans le Générateur de Nid. Voulez-vous enregistrer vos modifications avant de continuer ?"
        );
        alert.setTitle("Modifications non enregistrées");
        alert.setHeaderText("Quitter l'éditeur de nid ?");

        ButtonType btnSave = new ButtonType(i18n.get("common.btn.save", "Enregistrer"), ButtonBar.ButtonData.OK_DONE);
        ButtonType btnDiscard = new ButtonType("Abandonner", ButtonBar.ButtonData.OTHER);
        ButtonType btnCancel = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnSave, btnDiscard, btnCancel);
        java.util.Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == btnSave) {
            doAddPreset();
            return !isDirty;
        } else if (result.isPresent() && result.get() == btnDiscard) {
            isDirty = false;
            return true;
        }
        return false;
    }

    // Model
    private GeneratedNest nest;
    private Consumer<Map<String, Object>> onApplyCallback;

    public NestGeneratorPane() {
        setTop(buildHeader());
        setLeft(buildConfig());
        setCenter(buildViews());
        refreshPresetsCombo();
        if (!presetsCombo.getItems().isEmpty()) {
            presetsCombo.getSelectionModel().selectFirst();
            String first = presetsCombo.getValue();
            if (presetMgr.contains(first)) applyCfg(presetMgr.get(first));
        }
        regen();
        repaint();
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private VBox buildHeader() {
        I18nManager i18n = I18nManager.getInstance();
        VBox v = new VBox(6);
        v.setPadding(new Insets(8, 10, 5, 10));

        HBox r = new HBox(8);
        r.setAlignment(Pos.CENTER_LEFT);

        Label t = new Label();
        t.textProperty().bind(i18n.createStringBinding("nest.title"));
        t.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Label lp = new Label();
        lp.textProperty().bind(i18n.createStringBinding("preset.label"));
        lp.setStyle("-fx-font-weight: bold;");
        lp.setGraphic(new FontIcon(Feather.SLIDERS));

        presetsCombo = new ComboBox<>();
        presetsCombo.setEditable(true);
        presetsCombo.setPrefWidth(210);
        presetsCombo.promptTextProperty().bind(i18n.createStringBinding("preset.prompt"));
        presetsCombo.setTooltip(new Tooltip("Sélectionnez une configuration pré-définie ou enregistrée (Atta, Apis, Bombus, Vespula, Macrotermes, Crematogaster, Temnothorax, Eciton)."));
        refreshPresetsCombo();
        presetsCombo.setOnAction(e -> {
            if (isUpdatingSpeciesCombo) return;
            String s = presetsCombo.getValue();
            if (s == null || s.equals(lastSelectedPreset)) return;

            if (isDirty) {
                Alert alert = org.swarmforge.client.util.ThemeManager.createAlert(
                    Alert.AlertType.CONFIRMATION,
                    "Attention : Vous avez des modifications non enregistrées sur la configuration du nid.\n\nVoulez-vous vraiment charger le preset '" + s + "' et abandonner vos modifications ?"
                );
                alert.setTitle(I18nManager.getInstance().get("common.dialog.unsaved"));
                alert.setHeaderText("Changement de preset de nid");
                java.util.Optional<ButtonType> res = alert.showAndWait();
                if (res.isEmpty() || res.get() != ButtonType.OK) {
                    isUpdatingSpeciesCombo = true;
                    try {
                        presetsCombo.setValue(lastSelectedPreset);
                    } finally {
                        isUpdatingSpeciesCombo = false;
                    }
                    return;
                }
            }

            if (presetMgr.contains(s)) {
                lastSelectedPreset = s;
                applyCfg(presetMgr.get(s));
            }
        });

        Button bAdd = new Button();
        bAdd.setGraphic(new FontIcon(Feather.SAVE));
        bAdd.textProperty().bind(i18n.createStringBinding("preset.save"));
        bAdd.getStyleClass().add("btn-secondary");
        bAdd.setTooltip(new Tooltip("Enregistrer les paramètres actuels de l'architecture du nid comme nouveau preset."));
        bAdd.setOnAction(e -> doAddPreset());

        Button bDel = new Button();
        bDel.setGraphic(new FontIcon(Feather.TRASH_2));
        bDel.textProperty().bind(i18n.createStringBinding("preset.delete"));
        bDel.getStyleClass().add("btn-danger");
        bDel.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold;");
        bDel.setTooltip(new Tooltip("Supprimer le preset sélectionné de la liste de sauvegarde."));
        bDel.setOnAction(e -> doDeletePreset());

        Button bExp = new Button();
        bExp.setGraphic(new FontIcon(Feather.DOWNLOAD));
        bExp.textProperty().bind(i18n.createStringBinding("preset.export"));
        bExp.getStyleClass().add("btn-secondary");
        bExp.setTooltip(new Tooltip("Exporter l'architecture et la géométrie 3D du nid au format JSON."));
        bExp.setOnAction(e -> doExport());

        Button bImp = new Button();
        bImp.setGraphic(new FontIcon(Feather.UPLOAD));
        bImp.textProperty().bind(i18n.createStringBinding("preset.import"));
        bImp.getStyleClass().add("btn-secondary");
        bImp.setTooltip(new Tooltip("Importer un fichier JSON de configuration de nid."));
        bImp.setOnAction(e -> doImport());

        r.getChildren().addAll(t, sp, lp, presetsCombo, bAdd, bDel,
            new Separator(Orientation.VERTICAL), bExp, bImp);
        v.getChildren().addAll(r, new Separator());
        return v;
    }

    private void doDeletePreset() {
        String sel = presetsCombo.getValue();
        if (sel == null || sel.isEmpty()) return;

        I18nManager i18n = I18nManager.getInstance();
        Alert confirmAlert = org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.CONFIRMATION, String.format(i18n.get("preset.delete.confirm"), sel));
        confirmAlert.setTitle(i18n.get("preset.delete.title"));
        confirmAlert.setHeaderText("Supprimer le Preset Nid");

        confirmAlert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                presetMgr.delete(sel);
                refreshPresetsCombo();
                if (!presetsCombo.getItems().isEmpty()) {
                    presetsCombo.getSelectionModel().selectFirst();
                } else {
                    presetsCombo.getSelectionModel().clearSelection();
                }
                NotificationOverlay.show(this, "Preset nid supprimé.", NotificationOverlay.NotificationType.INFO);
            }
        });
    }

    private Button btn(String text, String bg) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:white;");
        return b;
    }

    // ── Config panel ──────────────────────────────────────────────────────────

    private void populateSpeciesModelCombo() {
        filterSpeciesModelComboByCategory(categorySelect != null ? categorySelect.getValue() : null);
    }

    private void filterSpeciesModelComboByCategory(String category) {
        if (speciesModelCombo == null) return;
        String targetType = null;
        if (category != null) {
            if (category.contains("Ants")) targetType = "ANT";
            else if (category.contains("Honeybees") || category.contains("Bumblebees")) targetType = "BEE";
            else if (category.contains("Wasps")) targetType = "WASP";
            else if (category.contains("Termites")) targetType = "TERMITE";
        }

        String curSel = speciesModelCombo.getValue();
        boolean wasUpdating = isUpdatingSpeciesCombo;
        isUpdatingSpeciesCombo = true;
        try {
            speciesModelCombo.getItems().clear();
            org.swarmforge.client.ui.SpeciesPresetManager mgr = new org.swarmforge.client.ui.SpeciesPresetManager();
            for (String pName : mgr.getPresetNames()) {
                org.swarmforge.core.species.CustomSpecies sp = mgr.getPreset(pName);
                String icon = "🐜";
                String spType = sp != null && sp.getInsectType() != null ? sp.getInsectType().toUpperCase() : "ANT";
                if (spType.contains("BEE") || spType.contains("WASP")) icon = "🐝";
                else if (spType.contains("TERMITE")) icon = "🐜";

                if (targetType == null || spType.contains(targetType) ||
                    (targetType.equals("BEE") && (spType.contains("BEE") || spType.contains("WASP")))) {
                    speciesModelCombo.getItems().add(icon + " " + pName);
                }
            }
            speciesModelCombo.getItems().add("✨ Espèce Personnalisée Active");
            if (curSel != null && speciesModelCombo.getItems().contains(curSel)) {
                speciesModelCombo.setValue(curSel);
            } else if (!speciesModelCombo.getItems().isEmpty()) {
                speciesModelCombo.getSelectionModel().selectFirst();
            }
        } finally {
            isUpdatingSpeciesCombo = wasUpdating;
        }
    }

    private ScrollPane buildConfig() {
        I18nManager i18n = I18nManager.getInstance();
        VBox cfg = new VBox(10);
        cfg.setPadding(new Insets(10));
        cfg.setPrefWidth(330);

        // Master Control Block: 5 Primary Structural & Biological Parameters (Strict Order)
        Label mainSpecsTitle = new Label("🏗️ Morphologie & Spécifications du Nid");
        mainSpecsTitle.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-padding:2 0 2 0;-fx-text-fill:#38bdf8;");

        // 1. Insect Category
        Label lblCat = new Label("1. Catégorie / Famille d'Insectes :");
        lblCat.setStyle("-fx-font-weight:bold;-fx-font-size:11px;-fx-text-fill:#a78bfa;");
        categorySelect = new ComboBox<>();
        categorySelect.setTooltip(new Tooltip("Famille d'insectes eusociaux : adapte la morphologie générale et filtre les espèces de référence."));
        categorySelect.getItems().addAll(
            "🐜 Ants (Formicidae)",
            "🐝 Bumblebees (Bombus)",
            "🐝 Honeybees (Apis)",
            "🐜 Termites (Isoptera)",
            "🐝 Wasps & Hornets (Vespidae)"
        );
        categorySelect.getSelectionModel().selectFirst();
        categorySelect.setPrefWidth(270);
        categorySelect.setOnAction(e -> {
            onCategoryChanged();
            onManualParameterChanged();
        });

        // 2. Species Reference Model (Filtered by Category)
        Label lblSpecies = new Label("2. Espèce de Référence (Compatible) :");
        lblSpecies.setStyle("-fx-font-weight:bold;-fx-font-size:11px;-fx-text-fill:#38bdf8;");
        speciesModelCombo = new ComboBox<>();
        speciesModelCombo.setTooltip(new Tooltip("Modèle d'espèce de référence dont les caractéristiques biologiques déduisent la taille et les galeries."));
        populateSpeciesModelCombo();
        speciesModelCombo.setPrefWidth(270);
        speciesModelCombo.setOnAction(e -> {
            if (!isUpdatingSpeciesCombo) {
                onSpeciesModelSelected();
            }
        });

        // 3. Nest Architecture Type
        Label lblArch = new Label("3. Type d'Architecture du Nid :");
        lblArch.setStyle("-fx-font-weight:bold;-fx-font-size:11px;-fx-text-fill:#eab308;");
        archSelect = new ComboBox<>();
        archSelect.setTooltip(new Tooltip("Architecture biologique : 11 structures réelles (Ruche en cire, Nid papier, Cathédrale termite, Dôme, Souterrain, etc.)."));
        archSelect.getItems().addAll(
            "Arboreal Carton Nest",
            "Arboreal Silk Leaf",
            "Bamboo Stem & Gall",
            "Bivouac Living Nest",
            "Cathedral Mound",
            "Hanging Paper Nest",
            "Hexagonal Wax Comb",
            "Hollow Trunk Cavity",
            "Subterranean Burrow",
            "Subterranean Fungi Vault",
            "Surface Dome Mound",
            "Wax Pots Cluster",
            "Wooden Beehive"
        );
        archSelect.getSelectionModel().selectFirst();
        archSelect.setPrefWidth(270);
        archSelect.setOnAction(e -> {
            onManualParameterChanged();
            regen();
            repaint();
        });

        // 4. Construction Material
        Label lblMat = new Label("4. Matériau de Construction :");
        lblMat.setStyle("-fx-font-weight:bold;-fx-font-size:11px;-fx-text-fill:#22c55e;");
        matSelect = new ComboBox<>();
        matSelect.setTooltip(new Tooltip("Matériau biologique de construction (Terre, Cire, Papier, Ciment stercoral, Soie, Propolis, Carton, Corps vivants)."));
        matSelect.getItems().addAll(
            "Beeswax (Apidae)",
            "Carton & Wood Pulp",
            "Earth & Clay Soil",
            "Living Insect Bodies (Bivouac)",
            "Propolis & Tree Resin",
            "Silk Weave (Oecophylla Larvae)",
            "Stercoral Cement (Termite Feces/Mud)",
            "Tree Branch & Bark",
            "Tree Leaf Tissue",
            "Tree Trunk & Hollow Wood",
            "Wood Plank Construction",
            "Wood Pulp Paper (Vespidae)"
        );
        matSelect.getSelectionModel().selectFirst();
        matSelect.setPrefWidth(270);
        matSelect.setOnAction(e -> {
            onManualParameterChanged();
            regen();
            repaint();
        });

        // 5. Nest Development Stage / Maturity
        Label lblStage = new Label("5. Stade de Développement / Maturité :");
        lblStage.setStyle("-fx-font-weight:bold;-fx-font-size:11px;-fx-text-fill:#ec4899;");
        nestStageCombo = new ComboBox<>();
        nestStageCombo.setTooltip(new Tooltip("Stade de maturité de la colonie (Fondation < 200 ind., Établi 25%, Mature 100%, Vieux Nid / Supercolonie > 200%)."));
        nestStageCombo.getItems().addAll(
            i18n.get("nest.species.age.foundation"),
            i18n.get("nest.species.age.established"),
            i18n.get("nest.species.age.mature"),
            i18n.get("nest.species.age.giant")
        );
        nestStageCombo.setPrefWidth(270);
        nestStageCombo.getSelectionModel().select(2); // Mature by default
        nestStageCombo.setOnAction(e -> {
            if (!isUpdatingSpeciesCombo) {
                onNestStageChanged();
            }
        });

        speciesStatusLabel = new Label("Nid synchronisé avec le modèle d'espèce.");
        speciesStatusLabel.setStyle("-fx-font-size:10;-fx-text-fill:#94a3b8;-fx-wrap-text:true;");

        // Morphological Parameters (Auto-calculated from species & adjustable sliders)
        workerSizeSlider  = mkSlider(2.0, 30.0, 5.0);
        workerSizeSlider.setTooltip(new Tooltip("Échelle morphologique de l'ouvrière (déduite automatiquement de l'espèce de référence)."));

        depthSlider       = mkSlider(4,  60, 20);
        depthSlider.setTooltip(new Tooltip("Profondeur maximale ou hauteur verticale du nid en cm / blocs de grille."));

        tunnelWidthSlider = mkSlider(1,   5,  2);
        tunnelWidthSlider.setTooltip(new Tooltip("Diamètre des galeries (déduit automatiquement de l'espèce de référence)."));

        branchingSlider   = mkSlider(1,   5,  3);
        branchingSlider.setTooltip(new Tooltip("Facteur de ramification et connexions secondaires entre les galeries et chambres."));

        addLsn(workerSizeSlider, depthSlider, tunnelWidthSlider, branchingSlider);

        Button btnAutoAdapt = new Button("⚡ Adapter à l'Espèce");
        btnAutoAdapt.setGraphic(new FontIcon(Feather.ZAP));
        btnAutoAdapt.setStyle("-fx-background-color:#0284c7;-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:11px;-fx-padding:5 12;-fx-background-radius:4;");
        btnAutoAdapt.setTooltip(new Tooltip("Adapter automatiquement la taille des ouvrières, le diamètre des galeries, la profondeur et la répartition des chambres à l'espèce sélectionnée."));
        btnAutoAdapt.setOnAction(e -> {
            if (activeCustomSpecies != null) {
                configureFromSpecies(activeCustomSpecies);
                NotificationOverlay.show(this, "Spécifications du nid adaptées à " + activeCustomSpecies.getCommonName() + " !", NotificationOverlay.NotificationType.SUCCESS);
            } else {
                onSpeciesModelSelected();
                String selSp = speciesModelCombo != null && speciesModelCombo.getValue() != null ? speciesModelCombo.getValue() : "l'espèce sélectionnée";
                NotificationOverlay.show(this, "Spécifications du nid adaptées à " + selSp + " !", NotificationOverlay.NotificationType.SUCCESS);
            }
        });

        Label lblWorkerAuto = new Label("🐜 Taille ouvrière (déduite de l'espèce) :");
        lblWorkerAuto.setStyle("-fx-font-size:10px;-fx-text-fill:#94a3b8;");

        Label lblTunnelAuto = new Label("🚇 Largeur galeries (déduite) :");
        lblTunnelAuto.setStyle("-fx-font-size:10px;-fx-text-fill:#94a3b8;");

        Label lblMaxDepth = new Label(); lblMaxDepth.textProperty().bind(i18n.createStringBinding("nest.arch.max_depth"));
        Label lblBranching = new Label(); lblBranching.textProperty().bind(i18n.createStringBinding("nest.arch.branching"));

        VBox masterBlock = new VBox(7,
            mainSpecsTitle, new Separator(),
            lblCat, categorySelect,
            lblSpecies, speciesModelCombo,
            lblArch, archSelect,
            lblMat, matSelect,
            lblStage, nestStageCombo,
            speciesStatusLabel,
            new Separator(),
            btnAutoAdapt,
            lblWorkerAuto, sv(workerSizeSlider),
            lblTunnelAuto, sv(tunnelWidthSlider),
            lblMaxDepth,   sv(depthSlider),
            lblBranching,  sv(branchingSlider)
        );
        masterBlock.getStyleClass().add("card-pane");

        // Chamber Distribution (non-collapsible)
        Label cdTitle = new Label();
        cdTitle.textProperty().bind(i18n.createStringBinding("nest.chambers.title"));
        cdTitle.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-padding:2 0 2 0;");

        Label lblTotalChambersTitle = new Label();
        lblTotalChambersTitle.textProperty().bind(i18n.createStringBinding("nest.chambers.total"));
        lblTotalChambersTitle.setStyle("-fx-font-weight:bold;");

        lblTotalChambersValue = new Label("11");
        lblTotalChambersValue.setStyle("-fx-font-size:14;-fx-font-weight:bold;");

        HBox totalChambersBox = new HBox(8, lblTotalChambersTitle, lblTotalChambersValue);
        totalChambersBox.setAlignment(Pos.CENTER_LEFT);
        totalChambersBox.setStyle("-fx-padding:2 0 4 0;");

        GridPane grid = new GridPane();
        grid.setHgap(8); grid.setVgap(7); grid.setPadding(new Insets(6));

        String[][] defs = {
            {"nest.chambers.queen", "1", "👑 Queen Chamber", "Nombre de loges royales réservées à la reine et à la ponte."},
            {"nest.chambers.brood", "3", "🥚 Brood Chambers", "Chambres d'élevage spécialisées pour œufs, larves et nymphes."},
            {"nest.chambers.food", "4", "🍖 Food Storage", "Chambres et pots de stockage de nourriture (miel, pollen, graines, proies)."},
            {"nest.chambers.entrance", "2", "🚪 Entrances", "Nombre de sorties et orifices d'accès vers l'extérieur."},
            {"nest.chambers.waste", "1", "🗑 Waste Dumps", "Réceptacles et dépotoirs à détritus, cadavres et résidus."},
            {"nest.chambers.fungus", "0", "🍄 Fungus Gardens", "Cavernes de culture pour le champignon symbiotique (Atta)."}
        };
        int row = 0;
        for (String[] d : defs) {
            Spinner<Integer> s = new Spinner<>(0, 25, Integer.parseInt(d[1]));
            s.setPrefWidth(70); s.setEditable(true);
            s.setTooltip(new Tooltip(d[3]));
            s.valueProperty().addListener((o,a,b) -> {
                updateTotalChambers();
                onManualParameterChanged();
                regen();
                repaint();
            });
            chamberSpinners.put(d[2], s);

            Label chLbl = new Label();
            chLbl.textProperty().bind(i18n.createStringBinding(d[0]));
            chLbl.setTooltip(new Tooltip(d[3]));
            grid.add(chLbl, 0, row);
            grid.add(s, 1, row++);
        }

        updateTotalChambers();

        VBox cdBlock = new VBox(6, cdTitle, new Separator(),
            totalChambersBox, grid);
        cdBlock.getStyleClass().add("card-pane");

        // 1. Environment Placement Parameters Block
        VBox envPlacementBlock = buildEnvironmentPlacementBlock();

        // 2. Real-Time Spatial Viability Diagnostics Block
        VBox viabilityBlock = buildPlacementViabilityCard();

        cfg.getChildren().addAll(masterBlock, cdBlock, envPlacementBlock, viabilityBlock);
        ScrollPane sc = new ScrollPane(cfg);
        sc.setFitToWidth(true); sc.setPrefWidth(360); sc.setMaxWidth(360);
        return sc;
    }

    private VBox buildEnvironmentPlacementBlock() {
        Label envTitle = new Label("🌍 Paramètres de Placement Environnemental");
        envTitle.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:#38bdf8;");

        // 1. Height Slider
        evalHeightSlider = mkSlider(-2.0, 15.0, 0.0);
        evalHeightSlider.setTooltip(new Tooltip("Élévation / Profondeur du nid par rapport au niveau du sol en mètres."));
        evalHeightSlider.valueProperty().addListener((o, a, n) -> updatePlacementViabilityScore());
        HBox heightBox = createEvalSliderRow("Élévation / Hauteur (m) :", evalHeightSlider, "m");

        // 2. Solar Orientation
        evalOrientationCombo = new ComboBox<>();
        evalOrientationCombo.getItems().addAll(
            "East (Morning Light)",
            "North (Shaded / Cool)",
            "South (Full Solar)",
            "South-East (Morning Sun)",
            "West (Evening Heat)"
        );
        evalOrientationCombo.getSelectionModel().selectFirst();
        evalOrientationCombo.setPrefWidth(220);
        evalOrientationCombo.setOnAction(e -> updatePlacementViabilityScore());

        HBox orientRow = new HBox(8, new Label("Exposition Solaire :"), evalOrientationCombo);
        orientRow.setAlignment(Pos.CENTER_LEFT);

        // 3. Thermal Temp
        evalTempSlider = mkSlider(5.0, 42.0, 22.0);
        evalTempSlider.setTooltip(new Tooltip("Température microclimatique ambiante de l'habitat en °C."));
        evalTempSlider.valueProperty().addListener((o, a, n) -> updatePlacementViabilityScore());
        HBox tempBox = createEvalSliderRow("Température (°C) :", evalTempSlider, "°C");

        // 4. Substrate Moisture
        evalMoistureSlider = mkSlider(0.0, 100.0, 45.0);
        evalMoistureSlider.setTooltip(new Tooltip("Taux d'humidité relative du substrat ou de l'air en %."));
        evalMoistureSlider.valueProperty().addListener((o, a, n) -> updatePlacementViabilityScore());
        HBox moistureBox = createEvalSliderRow("Humidité Substrat (%) :", evalMoistureSlider, "%");

        // 5. Foraging Radius
        evalForagingSlider = mkSlider(5.0, 300.0, 35.0);
        evalForagingSlider.setTooltip(new Tooltip("Distance moyenne vers les ressources florales, eau ou proies en mètres."));
        evalForagingSlider.valueProperty().addListener((o, a, n) -> updatePlacementViabilityScore());
        HBox foragingBox = createEvalSliderRow("Distance Fleurs (m) :", evalForagingSlider, "m");

        // 6. Compaction
        evalCompactionSlider = mkSlider(10.0, 150.0, 65.0);
        evalCompactionSlider.setTooltip(new Tooltip("Dureté / Cohésion du substrat porteur en kPa."));
        evalCompactionSlider.valueProperty().addListener((o, a, n) -> updatePlacementViabilityScore());
        HBox compactionBox = createEvalSliderRow("Compacité (kPa) :", evalCompactionSlider, "kPa");

        VBox block = new VBox(6,
            envTitle, new Separator(),
            heightBox, orientRow, tempBox, moistureBox, foragingBox, compactionBox
        );
        block.getStyleClass().add("card-pane");
        return block;
    }

    private VBox buildPlacementViabilityCard() {
        Label evalTitle = new Label("📊 Diagnostic & Viabilité Spatiale en Temps Réel");
        evalTitle.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:#38bdf8;");

        // Results Section
        evalScoreProgressBar = new ProgressBar(0.85);
        evalScoreProgressBar.setPrefWidth(160);
        evalScoreProgressBar.setPrefHeight(16);

        evalScoreLabel = new Label("85%");
        evalScoreLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #22c55e;");

        evalBadgeLabel = new Label("🟢 OPTIMAL");
        evalBadgeLabel.setStyle("-fx-background-color: #15803d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 10px;");

        HBox scoreHeaderBox = new HBox(8, evalScoreProgressBar, evalScoreLabel, evalBadgeLabel);
        scoreHeaderBox.setAlignment(Pos.CENTER_LEFT);

        evalRecommendationsBox = new VBox(4);
        evalRecommendationsBox.setPadding(new Insets(6));
        evalRecommendationsBox.setStyle("-fx-background-color: #18181b; -fx-background-radius: 6;");

        VBox block = new VBox(6,
            evalTitle, new Separator(),
            new Label("📊 Score Global de Viabilité Spatiale :"),
            scoreHeaderBox,
            new Label("💡 Diagnostics & Contraintes Biologiques :"),
            evalRecommendationsBox
        );
        block.getStyleClass().add("card-pane");

        updatePlacementViabilityScore();
        return block;
    }

    private HBox createEvalSliderRow(String labelText, Slider slider, String unit) {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(labelText);
        lbl.setPrefWidth(140);
        lbl.setStyle("-fx-font-size:11px;");

        Label valLbl = new Label(String.format("%.1f %s", slider.getValue(), unit));
        valLbl.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-min-width: 48; -fx-font-size:11px;");
        slider.valueProperty().addListener((o, a, n) -> valLbl.setText(String.format("%.1f %s", n.doubleValue(), unit)));

        row.getChildren().addAll(lbl, slider, valLbl);
        return row;
    }

    private void updatePlacementViabilityScore() {
        if (evalScoreProgressBar == null) return;
        double height = evalHeightSlider != null ? evalHeightSlider.getValue() : 0.0;
        double temp = evalTempSlider != null ? evalTempSlider.getValue() : 22.0;
        double moisture = evalMoistureSlider != null ? evalMoistureSlider.getValue() : 45.0;
        double foraging = evalForagingSlider != null ? evalForagingSlider.getValue() : 35.0;
        double compaction = evalCompactionSlider != null ? evalCompactionSlider.getValue() : 65.0;

        String arch = archSelect != null ? archSelect.getValue() : "BURROW_UNDERGROUND";

        double score = 100.0;
        evalRecommendationsBox.getChildren().clear();

        // 1. Height & Elevation Clearance Checks
        if (arch.contains("BEEHIVE") || arch.contains("WAX_COMB")) {
            if (height < 0.4) {
                score -= 25.0;
                addEvalRec("⚠️ Ruche en Bois : Risque d'humidité et prédateurs de sol. Élever à >= 0.5m du sol.");
            } else if (height > 2.5) {
                score -= 15.0;
                addEvalRec("ℹ️ Hauteur élevée : Exposition au vent fort susceptible de perturber l'envol.");
            } else {
                addEvalRec("✅ Élévation idéale (0.5m - 2.0m) : Protection du sol et isolation.");
            }
        } else if (arch.contains("PAPER_PEDUNCULATE")) {
            if (height < 2.5) {
                score -= 35.0;
                addEvalRec("🚨 Guêpier Suspendu : Hauteur < 2.5m vulnérable aux prédateurs terrestres.");
            } else {
                addEvalRec("✅ Ancrage aérien optimal : Pédoncule fixé en hauteur à l'abri du sol.");
            }
        } else if (arch.contains("BURROW") || arch.contains("FUNGI_VAULT")) {
            if (height > 0.5) {
                score -= 30.0;
                addEvalRec("⚠️ Galerie souterraine placée au-dessus de la surface du sol.");
            } else {
                addEvalRec("✅ Profondeur idéale : Protection thermique naturelle du sol.");
            }
        }

        // 2. Thermal Microclimate
        if (temp < 15.0) { score -= 20.0; addEvalRec("⚠️ Température ambiante fraîche (<15°C) : Développement du couvain ralenti."); }
        else if (temp > 35.0) { score -= 25.0; addEvalRec("🚨 Surchauffe thermique (>35°C) : Risque de fonte des cires ou mortalité."); }
        else { addEvalRec("✅ Microclimat thermique optimal (18°C - 30°C)."); }

        // 3. Moisture
        if (moisture < 20.0) { score -= 20.0; addEvalRec("⚠️ Desséchement du substrat (<20%) : Risque de déshydratation du couvain."); }
        else if (moisture > 80.0) { score -= 20.0; addEvalRec("⚠️ Saturation en eau (>80%) : Risque de moisissure fongique."); }
        else { addEvalRec("✅ Taux d'humidité du substrat équilibré."); }

        // 4. Foraging
        if (foraging > 150.0) { score -= 20.0; addEvalRec("⚠️ Distance florale/eau élevée (>150m) : Dépense énergétique de vol élevée."); }
        else { addEvalRec("✅ Proximité immédiate des ressources florales et eau."); }

        // 5. Compaction
        if (compaction < 30.0) { score -= 15.0; addEvalRec("⚠️ Substrat meuble instable (<30 kPa) : Risque d'effondrement."); }

        score = Math.max(0.0, Math.min(100.0, score));
        evalScoreProgressBar.setProgress(score / 100.0);
        evalScoreLabel.setText(String.format("%.0f%%", score));

        if (score >= 80) {
            evalScoreProgressBar.setStyle("-fx-accent: #22c55e;");
            evalScoreLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #22c55e;");
            evalBadgeLabel.setText("🟢 OPTIMAL");
            evalBadgeLabel.setStyle("-fx-background-color: #15803d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 10px;");
        } else if (score >= 50) {
            evalScoreProgressBar.setStyle("-fx-accent: #f59e0b;");
            evalScoreLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #f59e0b;");
            evalBadgeLabel.setText("🟠 VIABLE");
            evalBadgeLabel.setStyle("-fx-background-color: #b45309; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 10px;");
        } else {
            evalScoreProgressBar.setStyle("-fx-accent: #ef4444;");
            evalScoreLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
            evalBadgeLabel.setText("🔴 CRITIQUE");
            evalBadgeLabel.setStyle("-fx-background-color: #b91c1c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 10px;");
        }
    }

    private void addEvalRec(String msg) {
        Label l = new Label(msg);
        l.setStyle("-fx-font-size: 10px; -fx-text-fill: #e4e4e7; -fx-wrap-text: true;");
        evalRecommendationsBox.getChildren().add(l);
    }


    private void onCategoryChanged() {
        String cat = categorySelect.getValue();
        if (cat == null) return;
        if (cat.contains("Honeybees")) {
            setArchSelectValue("Hexagonal Wax Comb");
            setMatSelectValue("Beeswax (Apidae)");
        } else if (cat.contains("Bumblebees")) {
            setArchSelectValue("Wax Pots Cluster");
            setMatSelectValue("Propolis & Tree Resin");
        } else if (cat.contains("Wasps")) {
            setArchSelectValue("Hanging Paper Nest");
            setMatSelectValue("Wood Pulp Paper (Vespidae)");
        } else if (cat.contains("Termites")) {
            setArchSelectValue("Cathedral Mound");
            setMatSelectValue("Stercoral Cement (Termite Feces/Mud)");
        } else if (cat.contains("Ants")) {
            if (archSelect.getValue() != null && !archSelect.getValue().contains("Silk") && !archSelect.getValue().contains("Dome")) {
                setArchSelectValue("Subterranean Burrow");
            }
            setMatSelectValue("Earth & Clay Soil");
        }
        regen(); repaint();
    }

    private void setArchSelectValue(String val) {
        if (archSelect == null || val == null) return;
        String mapped = switch (val.toUpperCase()) {
            case "WAX_COMB_HEXAGONAL", "HEXAGONAL WAX COMB" -> "Hexagonal Wax Comb";
            case "WAX_POTS_CLUSTER", "WAX POTS CLUSTER" -> "Wax Pots Cluster";
            case "PAPER_PEDUNCULATE", "HANGING PAPER NEST" -> "Hanging Paper Nest";
            case "CATHEDRAL_MOUND", "CATHEDRAL MOUND" -> "Cathedral Mound";
            case "SUBTERRANEAN_FUNGI_VAULT", "SUBTERRANEAN FUNGI VAULT" -> "Subterranean Fungi Vault";
            case "CARTON_NEST", "ARBOREAL CARTON NEST", "ARBOREAL_CARTON_NEST" -> "Arboreal Carton Nest";
            case "BAMBOO_STEM_NEST", "BAMBOO STEM & GALL", "BAMBOO_STEM_GALL" -> "Bamboo Stem & Gall";
            case "BIVOUAC_LIVING_NEST", "BIVOUAC LIVING NEST" -> "Bivouac Living Nest";
            case "ARBOREAL_SILK_LEAF", "ARBOREAL SILK LEAF" -> "Arboreal Silk Leaf";
            case "MOUND", "SURFACE_MOUND", "SURFACE DOME MOUND" -> "Surface Dome Mound";
            case "BURROW_UNDERGROUND", "SUBTERRANEAN BURROW", "SUBTERRANEAN_BURROW", "SIMPLE" -> "Subterranean Burrow";
            case "HOLLOW TRUNK CAVITY", "HOLLOW_TRUNK_CAVITY", "HOLLOW_TRUNK_NEST", "TREE" -> "Hollow Trunk Cavity";
            case "WOODEN BEEHIVE", "WOODEN_BEEHIVE" -> "Wooden Beehive";
            default -> val;
        };
        if (archSelect.getItems().contains(mapped)) {
            archSelect.setValue(mapped);
        } else {
            for (String item : archSelect.getItems()) {
                if (item.equalsIgnoreCase(val)) {
                    archSelect.setValue(item);
                    return;
                }
            }
        }
    }

    private void setMatSelectValue(String val) {
        if (matSelect == null || val == null) return;
        String mapped = switch (val.toUpperCase()) {
            case "BEESWAX" -> "Beeswax (Apidae)";
            case "CARTON_PULP" -> "Carton & Wood Pulp";
            case "EARTH" -> "Earth & Clay Soil";
            case "LIVING_INSECT_BODIES" -> "Living Insect Bodies (Bivouac)";
            case "PROPOLIS" -> "Propolis & Tree Resin";
            case "SILK_WEAVE" -> "Silk Weave (Oecophylla Larvae)";
            case "STERCORAL_CEMENT" -> "Stercoral Cement (Termite Feces/Mud)";
            case "TREE_BRANCH" -> "Tree Branch & Bark";
            case "TREE_LEAF" -> "Tree Leaf Tissue";
            case "TREE_TRUNK" -> "Tree Trunk & Hollow Wood";
            case "WOOD_PLANK" -> "Wood Plank Construction";
            case "WOOD_PULP_PAPER" -> "Wood Pulp Paper (Vespidae)";
            default -> val;
        };
        if (matSelect.getItems().contains(mapped)) {
            matSelect.setValue(mapped);
        } else {
            for (String item : matSelect.getItems()) {
                if (item.equalsIgnoreCase(val)) {
                    matSelect.setValue(item);
                    return;
                }
            }
        }
    }

    private Label lbl(String s) { return new Label(s); }

    private void addLsn(Slider... sliders) {
        for (Slider s : sliders)
            s.valueProperty().addListener((o,a,b) -> { regen(); repaint(); });
    }

    private Slider mkSlider(double min, double max, double val) {
        Slider s = new Slider(min, max, val);
        s.setShowTickLabels(true); s.setShowTickMarks(true);
        s.setMajorTickUnit((max-min)/4); s.setPrefWidth(165);
        return s;
    }

    private HBox sv(Slider s) {
        HBox b = new HBox(8); b.setAlignment(Pos.CENTER_LEFT);
        Label v = new Label(fmt(s.getValue()));
        v.setStyle("-fx-text-fill:#00d4ff;-fx-min-width:32;-fx-font-weight:bold;");
        s.valueProperty().addListener((o,a,n) -> v.setText(fmt(n.doubleValue())));
        b.getChildren().addAll(s, v);
        return b;
    }

    private String fmt(double d) { return String.format("%.1f", d); }

    // ── View area ─────────────────────────────────────────────────────────────

    private VBox buildViews() {
        I18nManager i18n = I18nManager.getInstance();
        canvas3D   = new Canvas(540, 510); gc3D   = canvas3D.getGraphicsContext2D();
        canvasSide = new Canvas(215, 245); gcSide = canvasSide.getGraphicsContext2D();
        canvasTop  = new Canvas(215, 245); gcTop  = canvasTop.getGraphicsContext2D();

        setupMouse();

        Pane h3d = new Pane(canvas3D);
        h3d.setStyle("-fx-border-color:#555;-fx-border-width:1;");
        HBox.setHgrow(h3d, Priority.ALWAYS);

        canvas3D.widthProperty().bind(h3d.widthProperty());
        canvas3D.heightProperty().bind(h3d.heightProperty());
        canvas3D.widthProperty().addListener((obs, oldV, newV) -> repaint());
        canvas3D.heightProperty().addListener((obs, oldV, newV) -> repaint());

        Pane hSide = new Pane(canvasSide);
        hSide.setStyle("-fx-border-color:#444;-fx-border-width:1;");
        hSide.setPrefSize(215, 245);
        canvasSide.widthProperty().bind(hSide.widthProperty());
        canvasSide.heightProperty().bind(hSide.heightProperty());
        canvasSide.widthProperty().addListener((obs, oldV, newV) -> repaint());
        canvasSide.heightProperty().addListener((obs, oldV, newV) -> repaint());

        Pane hTop = new Pane(canvasTop);
        hTop.setStyle("-fx-border-color:#444;-fx-border-width:1;");
        hTop.setPrefSize(215, 245);
        canvasTop.widthProperty().bind(hTop.widthProperty());
        canvasTop.heightProperty().bind(hTop.heightProperty());
        canvasTop.widthProperty().addListener((obs, oldV, newV) -> repaint());
        canvasTop.heightProperty().addListener((obs, oldV, newV) -> repaint());

        Label ls = new Label();
        ls.textProperty().bind(i18n.createStringBinding("nest.view.side"));
        ls.setStyle("-fx-font-size:11;-fx-font-weight:bold;");

        Label lt = new Label();
        lt.textProperty().bind(i18n.createStringBinding("nest.view.top"));
        lt.setStyle("-fx-font-size:11;-fx-font-weight:bold;");

        VBox side = new VBox(5, ls, hSide, lt, hTop);
        side.setPadding(new Insets(0,4,0,8)); side.setAlignment(Pos.TOP_CENTER);

        HBox area = new HBox(6, h3d, side);
        area.setPadding(new Insets(8, 8, 4, 8));
        VBox.setVgrow(area, Priority.ALWAYS);

        // UI Legend & Control Bar under canvas area
        HBox legendBar = buildLegendBar();

        return new VBox(4, area, legendBar);
    }

    private HBox buildLegendBar() {
        I18nManager i18n = I18nManager.getInstance();
        HBox bar = new HBox(10);
        bar.setPadding(new Insets(4, 10, 6, 10));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-border-color:#3f3f46;-fx-border-width:1 0 0 0;");

        syncViewsCheckBox = new CheckBox("🔗 Synchroniser les vues (Zoom & Panning)");
        syncViewsCheckBox.setSelected(true);
        syncViewsCheckBox.setStyle("-fx-text-fill:#00d4ff;-fx-font-weight:bold;-fx-font-size:11;");

        showGhostMeshCheckBox = new CheckBox("👻 Vue fantôme 3D");
        showGhostMeshCheckBox.setSelected(true);
        showGhostMeshCheckBox.setStyle("-fx-text-fill:#38bdf8;-fx-font-weight:bold;-fx-font-size:11;");
        showGhostMeshCheckBox.setTooltip(new Tooltip("Activer ou désactiver l'affichage de la vue fantôme 3D des structures de nid."));
        showGhostMeshCheckBox.setOnAction(e -> repaint());

        Label title = new Label();
        title.textProperty().bind(i18n.createStringBinding("nest.legend.title"));
        title.setStyle("-fx-font-weight:bold;-fx-text-fill:#aaa;-fx-font-size:11;");

        bar.getChildren().addAll(syncViewsCheckBox, showGhostMeshCheckBox, new Separator(Orientation.VERTICAL), title);

        String[][] items = {
            {"nest.legend.entrance", "#32CD32"},
            {"nest.legend.queen", "#FFD700"},
            {"nest.legend.brood", "#00BFFF"},
            {"nest.legend.storage", "#FFA500"},
            {"nest.legend.fungus", "#9370DB"},
            {"nest.legend.waste", "#CD5C5C"},
            {"nest.legend.tunnel", "#708090"}
        };

        for (String[] it : items) {
            HBox item = new HBox(4);
            item.setAlignment(Pos.CENTER_LEFT);
            Canvas dot = new Canvas(9, 9);
            GraphicsContext g = dot.getGraphicsContext2D();
            g.setFill(Color.web(it[1]));
            g.fillOval(0, 0, 9, 9);
            g.setStroke(Color.WHITE);
            g.setLineWidth(0.5);
            g.strokeOval(0, 0, 9, 9);

            Label lbl = new Label();
            lbl.textProperty().bind(i18n.createStringBinding(it[0]));
            lbl.setStyle("-fx-text-fill:#ccc;-fx-font-size:10;");
            item.getChildren().addAll(dot, lbl);
            bar.getChildren().add(item);
        }

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label hint = new Label();
        hint.textProperty().bind(i18n.createStringBinding("nest.legend.hint"));
        hint.setStyle("-fx-text-fill:#888;-fx-font-size:10;-fx-font-style:italic;");

        bar.getChildren().addAll(sp, hint);
        return bar;
    }

    private void setupMouse() {
        // 3D Canvas Orbit & Zoom & Pan
        canvas3D.setOnMousePressed(e -> { lastMX = e.getX(); lastMY = e.getY(); });
        canvas3D.setOnMouseDragged(e -> {
            double dx = e.getX() - lastMX;
            double dy = e.getY() - lastMY;
            if (e.isSecondaryButtonDown() || e.isShiftDown()) {
                // Pan 3D camera
                pan3DX += dx;
                pan3DY += dy;
                if (isSync()) {
                    sidePanX = pan3DX; sidePanY = pan3DY;
                    topPanX = pan3DX; topPanY = pan3DY;
                }
            } else {
                // Orbit 3D camera
                azimuth = (azimuth + dx * 0.65) % 360;
                if (azimuth < 0) azimuth += 360;
                elevation = Math.max(5, Math.min(85, elevation - dy * 0.35));
            }
            lastMX = e.getX(); lastMY = e.getY();
            repaint();
        });
        canvas3D.setOnScroll(e -> {
            zoom = Math.max(2.5, Math.min(22.0, zoom + e.getDeltaY() * 0.025));
            if (isSync()) {
                sideZoom = Math.max(0.3, Math.min(6.0, zoom / 7.5));
                topZoom = sideZoom;
            }
            repaint();
        });
        canvas3D.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                resetAllCameras();
            }
        });

        // 2D Side View Zoom & Pan
        canvasSide.setOnMousePressed(e -> { lastSideMX = e.getX(); lastSideMY = e.getY(); });
        canvasSide.setOnMouseDragged(e -> {
            double dx = e.getX() - lastSideMX;
            double dy = e.getY() - lastSideMY;
            sidePanX += dx;
            sidePanY += dy;
            if (isSync()) {
                topPanX = sidePanX; topPanY = sidePanY;
                pan3DX = sidePanX; pan3DY = sidePanY;
            }
            lastSideMX = e.getX(); lastSideMY = e.getY();
            repaint();
        });
        canvasSide.setOnScroll(e -> {
            sideZoom = Math.max(0.3, Math.min(6.0, sideZoom + e.getDeltaY() * 0.003));
            if (isSync()) {
                topZoom = sideZoom;
                zoom = Math.max(2.5, Math.min(22.0, sideZoom * 7.5));
            }
            repaint();
        });
        canvasSide.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                resetAllCameras();
            }
        });

        // 2D Top View Zoom & Pan
        canvasTop.setOnMousePressed(e -> { lastTopMX = e.getX(); lastTopMY = e.getY(); });
        canvasTop.setOnMouseDragged(e -> {
            double dx = e.getX() - lastTopMX;
            double dy = e.getY() - lastTopMY;
            topPanX += dx;
            topPanY += dy;
            if (isSync()) {
                sidePanX = topPanX; sidePanY = topPanY;
                pan3DX = topPanX; pan3DY = topPanY;
            }
            lastTopMX = e.getX(); lastTopMY = e.getY();
            repaint();
        });
        canvasTop.setOnScroll(e -> {
            topZoom = Math.max(0.3, Math.min(6.0, topZoom + e.getDeltaY() * 0.003));
            if (isSync()) {
                sideZoom = topZoom;
                zoom = Math.max(2.5, Math.min(22.0, topZoom * 7.5));
            }
            repaint();
        });
        canvasTop.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                resetAllCameras();
            }
        });
    }

    private boolean isSync() {
        return syncViewsCheckBox != null && syncViewsCheckBox.isSelected();
    }

    private void resetAllCameras() {
        azimuth = 45; elevation = 35; zoom = 7.5;
        pan3DX = 0; pan3DY = 0;
        sideZoom = 1.0; sidePanX = 0; sidePanY = 0;
        topZoom = 1.0; topPanX = 0; topPanY = 0;
        repaint();
    }

    // ── Preset helpers ────────────────────────────────────────────────────────

    private void refreshPresetsCombo() {
        String cur = presetsCombo.getValue();
        presetsCombo.getItems().setAll(presetMgr.names());
        if (cur != null) presetsCombo.setValue(cur);
    }

    private void doAddPreset() {
        String defaultName = (presetsCombo.getEditor() != null && !presetsCombo.getEditor().getText().isBlank())
                ? presetsCombo.getEditor().getText().trim()
                : (presetsCombo.getValue() != null ? presetsCombo.getValue() : "Custom Nest Preset");
        TextInputDialog d = org.swarmforge.client.util.ThemeManager.createTextInputDialog(defaultName);
        d.setTitle("Save Preset"); d.setHeaderText("Nom du preset :"); d.setContentText("Nom :");
        d.showAndWait().ifPresent(name -> {
            if (name == null || name.isBlank()) return;
            String clean = name.trim();
            if (presetMgr.contains(clean)) {
                Alert confirmAlert = org.swarmforge.client.util.ThemeManager.createAlert(
                    Alert.AlertType.CONFIRMATION,
                    "Le preset de nid '" + clean + "' existe déjà.\n\nVoulez-vous le remplacer par la configuration actuelle ?"
                );
                confirmAlert.setTitle("Remplacer le Preset Existant");
                confirmAlert.setHeaderText("Confirmation de remplacement");
                java.util.Optional<ButtonType> res = confirmAlert.showAndWait();
                if (res.isEmpty() || res.get() != ButtonType.OK) {
                    return;
                }
            }
            presetMgr.save(clean, getConfiguration());
            isUpdatingSpeciesCombo = true;
            try {
                refreshPresetsCombo();
                presetsCombo.setValue(clean);
            } finally {
                isUpdatingSpeciesCombo = false;
            }
            lastSelectedPreset = clean;
            isDirty = false;
            NotificationOverlay.show(this, "Preset \"" + clean + "\" sauvegardé.", NotificationOverlay.NotificationType.SUCCESS);
        });
    }

    private void updateTotalChambers() {
        int total = chamberSpinners.values().stream().mapToInt(Spinner::getValue).sum();
        if (lblTotalChambersValue != null) {
            lblTotalChambersValue.setText(String.valueOf(total));
        }
    }

    private void onManualParameterChanged() {
        if (isUpdatingSpeciesCombo) return;
        isDirty = true;
        isUpdatingSpeciesCombo = true;
        try {
            if (speciesModelCombo != null) {
                speciesModelCombo.setValue("✨ Espèce Personnalisée Active");
            }
            if (speciesStatusLabel != null) {
                speciesStatusLabel.setText("Paramètres du nid modifiés (Nid sur mesure).");
            }
            if (presetsCombo != null && presetsCombo.getSelectionModel().getSelectedItem() != null) {
                presetsCombo.getSelectionModel().clearSelection();
            }
        } finally {
            isUpdatingSpeciesCombo = false;
        }
    }


    public void configureFromSpecies(org.swarmforge.core.species.CustomSpecies species) {
        if (species == null) return;
        this.activeCustomSpecies = species;

        isUpdatingSpeciesCombo = true;
        try {
            // 1. Insect Category & Material & Architecture mapping
            String orderStr = species.getInsectType() != null ? species.getInsectType() : "ANT";
            String nestTypeStr = species.getNestType() != null ? species.getNestType() : "MATURE";

            if ("TERMITE".equalsIgnoreCase(orderStr)) {
                categorySelect.setValue("🐜 Termites (Isoptera)");
                setArchSelectValue("Cathedral Mound");
                setMatSelectValue("Stercoral Cement (Termite Feces/Mud)");
            } else if ("BEE".equalsIgnoreCase(orderStr)) {
                if (species.getCommonName().toLowerCase().contains("bourdon") || (species.getScientificName() != null && species.getScientificName().toLowerCase().contains("bombus"))) {
                    categorySelect.setValue("🐝 Bumblebees (Bombus)");
                    setArchSelectValue("Wax Pots Cluster");
                    setMatSelectValue("Propolis & Tree Resin");
                } else {
                    categorySelect.setValue("🐝 Honeybees (Apis)");
                    setArchSelectValue("Hexagonal Wax Comb");
                    setMatSelectValue("Beeswax (Apidae)");
                }
            } else if ("WASP".equalsIgnoreCase(orderStr)) {
                categorySelect.setValue("🐝 Wasps & Hornets (Vespidae)");
                setArchSelectValue("Hanging Paper Nest");
                setMatSelectValue("Wood Pulp Paper (Vespidae)");
            } else {
                // ANT
                categorySelect.setValue("🐜 Ants (Formicidae)");
                String cName = species.getCommonName() != null ? species.getCommonName().toLowerCase() : "";
                if ("SUBTERRANEAN_FUNGI_VAULT".equalsIgnoreCase(nestTypeStr) || cName.contains("champignonniste") || cName.contains("coupeuse")) {
                    setArchSelectValue("Subterranean Fungi Vault");
                    setMatSelectValue("Earth & Clay Soil");
                } else if ("CARTON_NEST".equalsIgnoreCase(nestTypeStr) || cName.contains("carton")) {
                    setArchSelectValue("Arboreal Carton Nest");
                    setMatSelectValue("Carton & Wood Pulp");
                } else if ("BAMBOO_STEM_NEST".equalsIgnoreCase(nestTypeStr) || cName.contains("temnothorax") || cName.contains("galle") || cName.contains("tige")) {
                    setArchSelectValue("Bamboo Stem & Gall");
                    setMatSelectValue("Wood Pulp Paper (Vespidae)");
                } else if ("BIVOUAC_LIVING_NEST".equalsIgnoreCase(nestTypeStr) || cName.contains("légionnaire") || cName.contains("eciton")) {
                    setArchSelectValue("Bivouac Living Nest");
                    setMatSelectValue("Living Insect Bodies (Bivouac)");
                } else if ("ARBOREAL_SILK_LEAF".equalsIgnoreCase(nestTypeStr) || cName.contains("tisserande")) {
                    setArchSelectValue("Arboreal Silk Leaf");
                    setMatSelectValue("Silk Weave (Oecophylla Larvae)");
                } else if ("MOUND".equalsIgnoreCase(nestTypeStr) || "SURFACE_MOUND".equalsIgnoreCase(nestTypeStr) || cName.contains("fire") || cName.contains("feu")) {
                    setArchSelectValue("Surface Dome Mound");
                    setMatSelectValue("Earth & Clay Soil");
                } else {
                    setArchSelectValue("Subterranean Burrow");
                    setMatSelectValue("Earth & Clay Soil");
                }
            }

            // 2. Body length & Tunnel diameter
            float avgBodyMm = species.getAverageCasteBodyLengthMm();
            workerSizeSlider.setValue(Math.max(2.0, Math.min(30.0, avgBodyMm)));

            float reqTunnelMm = species.getRequiredTunnelDiameterMm();
            tunnelWidthSlider.setValue(Math.max(1.0, Math.min(5.0, Math.round(reqTunnelMm / 1.5))));

            // 3. Nest Development Stage Multipliers (Fondation, Établi, Mature, Vieux Nid)
            int stageIndex = nestStageCombo != null ? nestStageCombo.getSelectionModel().getSelectedIndex() : 2;
            if (stageIndex < 0) stageIndex = 2; // Default MATURE

            double popMultiplier = 1.0;
            double depthMultiplier = 1.0;
            int branchingVal = 3;

            switch (stageIndex) {
                case 0: // FOUNDATION
                    popMultiplier = 0.05;
                    depthMultiplier = 0.45;
                    branchingVal = 1;
                    break;
                case 1: // ESTABLISHED
                    popMultiplier = 0.25;
                    depthMultiplier = 0.75;
                    branchingVal = 2;
                    break;
                case 2: // MATURE
                default:
                    popMultiplier = 1.0;
                    depthMultiplier = 1.0;
                    branchingVal = 3;
                    break;
                case 3: // GIANT
                    popMultiplier = 2.5;
                    depthMultiplier = 1.35;
                    branchingVal = 4;
                    break;
            }

            branchingSlider.setValue(branchingVal);

            int popSize = Math.max(10, (int) (species.getTypicalColonySize() * popMultiplier));
            double depth = Math.max(4, Math.min(60, Math.log10(popSize + 10) * 10 * depthMultiplier));
            depthSlider.setValue(depth);

            // 4. Chamber distribution according to species biology and nest stage
            int queenCount = species.getQueenCount();
            setSp("👑 Queen Chamber", Math.max(1, Math.min(25, stageIndex == 0 ? 1 : queenCount)));

            boolean isFungusGrower = "FUNGUS".equalsIgnoreCase(species.getPrimaryDiet()) || 
                                     "FUNGUS".equalsIgnoreCase(species.getSecondaryDiet()) ||
                                     (species.getCommonName() != null && (species.getCommonName().toLowerCase().contains("champignonniste") ||
                                     species.getCommonName().toLowerCase().contains("atta")));

            int fungusCount = 0;
            if (isFungusGrower) {
                if (stageIndex == 0) fungusCount = 1;
                else if (stageIndex == 1) fungusCount = 3;
                else if (stageIndex == 2) fungusCount = 6;
                else fungusCount = 12;
            }
            setSp("🍄 Fungus Gardens", fungusCount);

            int broodCount = Math.max(1, (int) (Math.sqrt(popSize / 200.0) * (stageIndex == 0 ? 0.5 : stageIndex == 3 ? 1.8 : 1.0)));
            if (stageIndex == 0) broodCount = 1;
            setSp("🥚 Brood Chambers", Math.max(1, Math.min(25, broodCount)));

            int foodCount = Math.max(1, (int) (Math.sqrt(popSize / 300.0) * (stageIndex == 0 ? 0.5 : stageIndex == 3 ? 1.8 : 1.0)));
            if (stageIndex == 0) foodCount = 1;
            setSp("🍖 Food Storage", Math.max(1, Math.min(25, foodCount)));

            int entranceCount = stageIndex == 0 ? 1 : stageIndex == 1 ? 2 : stageIndex == 2 ? 2 : 4;
            setSp("🚪 Entrances", entranceCount);

            int wasteCount = stageIndex == 0 ? 1 : stageIndex == 1 ? 1 : stageIndex == 2 ? 2 : 4;
            setSp("🗑 Waste Dumps", wasteCount);

            updateTotalChambers();

            if (speciesStatusLabel != null) {
                speciesStatusLabel.setText("Nid synchronisé (" + species.getCommonName() + ")");
            }
            if (speciesModelCombo != null) {
                String matchedItem = null;
                for (String item : speciesModelCombo.getItems()) {
                    String cleanItem = item.replaceAll("^[🐜🐝✨]\\s*", "").trim();
                    if (cleanItem.equalsIgnoreCase(species.getPresetName()) ||
                        cleanItem.equalsIgnoreCase(species.getCommonName()) ||
                        (species.getCommonName() != null && cleanItem.toLowerCase().contains(species.getCommonName().toLowerCase())) ||
                        (species.getPresetName() != null && cleanItem.toLowerCase().contains(species.getPresetName().toLowerCase()))) {
                        matchedItem = item;
                        break;
                    }
                }
                if (matchedItem != null) {
                    speciesModelCombo.setValue(matchedItem);
                } else {
                    speciesModelCombo.setValue("✨ Espèce Personnalisée Active");
                }
            }
        } finally {
            isUpdatingSpeciesCombo = false;
        }

        regen();
        repaint();
    }

    private void onSpeciesModelSelected() {
        String sel = speciesModelCombo.getValue();
        if (sel == null) return;

        if (sel.contains("Espèce Personnalisée Active")) {
            if (activeCustomSpecies != null) {
                configureFromSpecies(activeCustomSpecies);
            } else if (speciesStatusLabel != null) {
                speciesStatusLabel.setText("Nid sur mesure (Paramètres personnalisés).");
            }
            return;
        }

        org.swarmforge.client.ui.SpeciesPresetManager presetManager = new org.swarmforge.client.ui.SpeciesPresetManager();
        String cleanSel = sel.replaceAll("^[🐜🐝✨]\\s*", "").trim();
        for (String presetName : presetManager.getPresetNames()) {
            if (cleanSel.equalsIgnoreCase(presetName) || cleanSel.toLowerCase().contains(presetName.toLowerCase()) || presetName.toLowerCase().contains(cleanSel.toLowerCase())) {
                configureFromSpecies(presetManager.getPreset(presetName));
                return;
            }
        }
    }

    private void onNestStageChanged() {
        if (activeCustomSpecies != null) {
            configureFromSpecies(activeCustomSpecies);
        } else {
            String sel = speciesModelCombo.getValue();
            if (sel != null && !sel.contains("Espèce Personnalisée Active")) {
                onSpeciesModelSelected();
            }
        }
    }

    private double num(Map<String,Object> m, String k) { return ((Number)m.get(k)).doubleValue(); }
    private void setSp(String k, int v) { Spinner<Integer> s = chamberSpinners.get(k); if (s!=null) s.getValueFactory().setValue(v); }
    private int getSp(String k) { Spinner<Integer> s = chamberSpinners.get(k); return s!=null ? s.getValue() : 0; }

    private void regen()   { nest = NestAlgorithm.generate(this); }
    private void repaint() { draw3D(); drawSide(); drawTop(); }

    // ── File I/O ──────────────────────────────────────────────────────────────

    private void doExport() {
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Export"); fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("JSON","*.json"));
        fc.setInitialFileName("nest.json");
        File f = fc.showSaveDialog(getScene().getWindow());
        if (f == null) return;
        try { new com.fasterxml.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(f, getConfiguration()); }
        catch (Exception ex) { org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.ERROR, ex.getMessage()).show(); }
    }

    private void doImport() {
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Import"); fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("JSON","*.json"));
        File f = fc.showOpenDialog(getScene().getWindow());
        if (f == null) return;
        try {
            @SuppressWarnings("unchecked")
            Map<String,Object> cfg = new com.fasterxml.jackson.databind.ObjectMapper().readValue(f, Map.class);
            applyCfg(cfg);
        } catch (Exception ex) { org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.ERROR, ex.getMessage()).show(); }
    }

    public void applyCfg(Map<String, Object> cfg) {
        if (cfg == null) return;
        isUpdatingSpeciesCombo = true;
        try {
            if (cfg.containsKey("presetName") && presetsCombo != null) {
                String pName = String.valueOf(cfg.get("presetName"));
                if (presetsCombo.getItems().contains(pName)) {
                    presetsCombo.setValue(pName);
                }
            }
            if (cfg.containsKey("taxonCategory") && categorySelect != null) categorySelect.setValue(String.valueOf(cfg.get("taxonCategory")));
            if (cfg.containsKey("architecture") && archSelect != null) setArchSelectValue(String.valueOf(cfg.get("architecture")));
            if (cfg.containsKey("material") && matSelect != null) setMatSelectValue(String.valueOf(cfg.get("material")));
            if (cfg.containsKey("workerSizeMm") && workerSizeSlider != null) workerSizeSlider.setValue(num(cfg, "workerSizeMm"));
            if (cfg.containsKey("depth") && depthSlider != null) depthSlider.setValue(num(cfg, "depth"));
            if (cfg.containsKey("tunnelWidth") && tunnelWidthSlider != null) tunnelWidthSlider.setValue(num(cfg, "tunnelWidth"));
            if (cfg.containsKey("branching") && branchingSlider != null) branchingSlider.setValue(num(cfg, "branching"));
            if (cfg.containsKey("nestStageIndex") && nestStageCombo != null) {
                int idx = ((Number) cfg.get("nestStageIndex")).intValue();
                if (idx >= 0 && idx < nestStageCombo.getItems().size()) nestStageCombo.getSelectionModel().select(idx);
            }
            if (cfg.containsKey("chamberDistribution") && cfg.get("chamberDistribution") instanceof Map<?,?> distMap) {
                distMap.forEach((k, v) -> setSp(String.valueOf(k), ((Number) v).intValue()));
                updateTotalChambers();
            }
            regen();
            repaint();
        } finally {
            isUpdatingSpeciesCombo = false;
            isDirty = false;
        }
    }

    private void applyToWorld() {
        if (onApplyCallback != null) onApplyCallback.accept(getConfiguration());
        else org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.WARNING,"No world editor connected.").show();
    }

    public void setOnApply(Consumer<Map<String,Object>> cb) { this.onApplyCallback = cb; }

    public Map<String,Object> getConfiguration() {
        Map<String,Object> c = new LinkedHashMap<>();
        c.put("presetName",   presetsCombo.getValue() != null ? presetsCombo.getValue() : "Custom");
        c.put("taxonCategory",categorySelect.getValue());
        c.put("architecture", archSelect.getValue());
        c.put("material",     matSelect.getValue());
        c.put("workerSizeMm", workerSizeSlider.getValue());
        c.put("depth",        (int) depthSlider.getValue());
        c.put("chamberCount", (int) getChamberCount());
        c.put("tunnelWidth",  (int) tunnelWidthSlider.getValue());
        c.put("branching",    (int) branchingSlider.getValue());
        c.put("nestStageIndex", nestStageCombo != null ? nestStageCombo.getSelectionModel().getSelectedIndex() : 2);
        Map<String,Integer> dist = new LinkedHashMap<>();
        chamberSpinners.forEach((k,v) -> dist.put(k, v.getValue()));
        c.put("chamberDistribution", dist);
        return c;
    }

    // ── Expose params for NestAlgorithm ──────────────────────────────────────

    String getArchitecture() { return archSelect.getValue() != null ? archSelect.getValue() : "BURROW_UNDERGROUND"; }
    String getMaterial()     { return matSelect.getValue() != null ? matSelect.getValue() : "EARTH"; }
    double getWorkerSizeMm() { return workerSizeSlider.getValue(); }
    double getDepth()        { return depthSlider.getValue(); }
    double getTunnelWidth()  { return tunnelWidthSlider.getValue(); }
    double getBranching()    { return branchingSlider.getValue(); }
    double getChamberCount() { return chamberSpinners.values().stream().mapToInt(Spinner::getValue).sum(); }
    int    sp(String k)      { return getSp(k); }

    // ── Drawing delegates ─────────────────────────────────────────────────────

    private void draw3D()   { if (canvas3D != null && canvas3D.getWidth() >= 10 && canvas3D.getHeight() >= 10) NestRenderer.draw3D(nest, gc3D, canvas3D.getWidth(), canvas3D.getHeight(), azimuth, elevation, zoom, getTunnelWidth(), pan3DX, pan3DY, showGhostMeshCheckBox == null || showGhostMeshCheckBox.isSelected()); }
    private void drawSide() { if (canvasSide != null && canvasSide.getWidth() >= 10 && canvasSide.getHeight() >= 10) NestRenderer.drawSide(nest, gcSide, canvasSide.getWidth(), canvasSide.getHeight(), getTunnelWidth(), sideZoom, sidePanX, sidePanY); }
    private void drawTop()  { if (canvasTop != null && canvasTop.getWidth() >= 10 && canvasTop.getHeight() >= 10) NestRenderer.drawTop(nest,  gcTop,  canvasTop.getWidth(),  canvasTop.getHeight(),  getTunnelWidth(), topZoom, topPanX, topPanY); }

    // ── Inner model classes ───────────────────────────────────────────────────

    public static class NestNode {
        public double x, y, z, radius, rx, ry, rz; // Anatomical lenticular radii
        public String type; public Color color;
        public NestNode(double x, double y, double z, String type, double r, Color c) {
            this.x=x; this.y=y; this.z=z; this.type=type; this.radius=r; this.color=c;
            this.rx = r; this.ry = r; this.rz = r * 0.55; // Lenticular dome ratio by default
        }
        public NestNode(double x, double y, double z, String type, double rx, double ry, double rz, Color c) {
            this.x=x; this.y=y; this.z=z; this.type=type; this.radius=Math.max(rx, ry);
            this.rx = rx; this.ry = ry; this.rz = rz; this.color=c;
        }
    }

    public static class NestEdge {
        public NestNode from, to;
        public List<double[]> pts;
        public NestEdge(NestNode f, NestNode t, List<double[]> p) { from=f; to=t; pts=p; }
    }

    public static class GeneratedNest {
        public List<NestNode> nodes = new ArrayList<>();
        public List<NestEdge> edges = new ArrayList<>();
        public double maxDepth;
        public String architecture = "BURROW_UNDERGROUND";
        public String material = "EARTH";
        public double workerSizeMm = 5.0;
        public double rootX = 0;
        public double rootY = 0;

        public double getRootX() {
            if (!nodes.isEmpty()) return nodes.get(0).x;
            return rootX;
        }

        public double getRootY() {
            if (!nodes.isEmpty()) return nodes.get(0).y;
            return rootY;
        }
    }
}

