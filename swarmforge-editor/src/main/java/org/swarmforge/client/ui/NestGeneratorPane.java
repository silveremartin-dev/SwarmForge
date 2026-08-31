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
import org.swarmforge.client.util.ThemeManager;

public class NestGeneratorPane extends BorderPane {
    private final I18nManager i18n = I18nManager.getInstance();

    // Canvases
    private ResizableCanvas canvas3D, canvasSide, canvasTop;
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
    private ComboBox<String> genusSelect;
    private ComboBox<String> nestStageCombo;
    private Label speciesStatusLabel;
    private Label passageCheckLabel;
    private org.swarmforge.core.species.CustomSpecies activeCustomSpecies;
    private boolean isUpdatingSpeciesCombo = false;

    private ComboBox<String> categorySelect;
    private ComboBox<String> archSelect;
    private ComboBox<String> matSelect;
    private Slider workerSizeSlider;
    private Slider depthSlider, tunnelWidthSlider, branchingSlider;
    private Label lblTotalChambersValue;
    private final Map<String, Spinner<Integer>> chamberSpinners = new LinkedHashMap<>();

    // Seed Control
    private long nestSeed = 123456L;
    private TextField seedField;

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
        String currentName = lastSelectedPreset != null ? lastSelectedPreset : "";
        boolean hasCurrentPreset = !currentName.isEmpty();

        Alert alert = org.swarmforge.client.util.ThemeManager.createAlert(
            Alert.AlertType.CONFIRMATION,
            "You have unsaved changes in the Nest Generator.\n"
            + (hasCurrentPreset ? "Current preset: \"" + currentName + "\"" : "No preset selected.")
        );
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText("Quit or change nest preset?");
        alert.getDialogPane().setPrefWidth(480);
        alert.getDialogPane().setMaxWidth(500);

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
        java.util.Optional<ButtonType> result = alert.showAndWait();

        if (!result.isPresent() || result.get() == btnCancel) return false;
        if (result.get() == btnDiscard) { isDirty = false; return true; }
        if (btnUpdate != null && result.get() == btnUpdate) {
            presetMgr.save(currentName, getConfiguration());
            isUpdatingSpeciesCombo = true;
            try {
                refreshPresetsCombo();
                presetsCombo.setValue(currentName);
            } finally {
                isUpdatingSpeciesCombo = false;
            }
            lastSelectedPreset = currentName;
            isDirty = false;
            NotificationOverlay.show(this, "Preset \"" + currentName + "\" mis \u00e0 jour.", NotificationOverlay.NotificationType.SUCCESS);
            return true;
        }
        if (result.get() == btnSaveAs) {
            doAddPreset();
            return !isDirty;
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
        ThemeManager.getInstance().currentThemeProperty().addListener((obs, oldTheme, newTheme) -> {
            updatePlacementViabilityScore();
            repaint();
        });
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
        presetsCombo.tooltipProperty().bind(i18n.createTooltipBinding("nest.presets.combo.tt"));
        refreshPresetsCombo();
        presetsCombo.setOnAction(e -> {
            if (isUpdatingSpeciesCombo) return;
            String s = presetsCombo.getValue();
            if (s == null || s.equals(lastSelectedPreset)) return;

            if (isDirty) {
                boolean proceed = promptUnsavedChanges();
                if (!proceed) {
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
        bAdd.tooltipProperty().bind(i18n.createTooltipBinding("nest.presets.save.tt"));
        bAdd.setOnAction(e -> doAddPreset());

        Button bDel = new Button();
        bDel.setGraphic(new FontIcon(Feather.TRASH_2));
        bDel.textProperty().bind(i18n.createStringBinding("preset.delete"));
        bDel.getStyleClass().add("btn-danger");
        bDel.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold;");
        bDel.tooltipProperty().bind(i18n.createTooltipBinding("nest.presets.delete.tt"));
        bDel.setOnAction(e -> doDeletePreset());

        Button bExp = new Button();
        bExp.setGraphic(new FontIcon(Feather.DOWNLOAD));
        bExp.textProperty().bind(i18n.createStringBinding("preset.export"));
        bExp.getStyleClass().add("btn-secondary");
        bExp.tooltipProperty().bind(i18n.createTooltipBinding("nest.presets.export.tt"));
        bExp.setOnAction(e -> doExport());

        Button bImp = new Button();
        bImp.setGraphic(new FontIcon(Feather.UPLOAD));
        bImp.textProperty().bind(i18n.createStringBinding("preset.import"));
        bImp.getStyleClass().add("btn-secondary");
        bImp.tooltipProperty().bind(i18n.createTooltipBinding("nest.presets.import.tt"));
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
        confirmAlert.setHeaderText("Delete Nest Preset");

        confirmAlert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                presetMgr.delete(sel);
                refreshPresetsCombo();
                if (!presetsCombo.getItems().isEmpty()) {
                    presetsCombo.getSelectionModel().selectFirst();
                } else {
                    presetsCombo.getSelectionModel().clearSelection();
                }
                NotificationOverlay.show(this, "Nest preset deleted.", NotificationOverlay.NotificationType.INFO);
            }
        });
    }

    private Button btn(String text, String bg) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:white;");
        return b;
    }

    // ── Config panel ──────────────────────────────────────────────────────────

    private void populateCategorySelect() {
        if (categorySelect == null) return;
        I18nManager i18n = I18nManager.getInstance();
        String cur = categorySelect.getValue();
        categorySelect.getItems().clear();
        categorySelect.getItems().addAll(
            i18n.get("nest.category.ants"),
            i18n.get("nest.category.bees"),
            i18n.get("nest.category.wasps"),
            i18n.get("nest.category.termites")
        );
        FXCollections.sort(categorySelect.getItems());
        if (cur != null && categorySelect.getItems().contains(cur)) {
            categorySelect.setValue(cur);
        } else {
            categorySelect.getSelectionModel().selectFirst();
        }
    }

    private void populateArchSelect() {
        if (archSelect == null) return;
        I18nManager i18n = I18nManager.getInstance();
        String cur = archSelect.getValue();
        archSelect.getItems().clear();
        archSelect.getItems().addAll(
            i18n.get("nest.arch.arboreal_carton"),
            i18n.get("nest.arch.arboreal_silk"),
            i18n.get("nest.arch.bamboo"),
            i18n.get("nest.arch.bivouac"),
            i18n.get("nest.arch.cathedral"),
            i18n.get("nest.arch.hanging_paper"),
            i18n.get("nest.arch.wax_comb"),
            i18n.get("nest.arch.hollow_trunk"),
            i18n.get("nest.arch.subterranean"),
            i18n.get("nest.arch.fungi_vault"),
            i18n.get("nest.arch.surface_dome"),
            i18n.get("nest.arch.wax_pots"),
            i18n.get("nest.arch.wooden_beehive")
        );
        FXCollections.sort(archSelect.getItems());
        if (cur != null && archSelect.getItems().contains(cur)) {
            archSelect.setValue(cur);
        } else {
            archSelect.getSelectionModel().selectFirst();
        }
    }

    private void populateMatSelect() {
        if (matSelect == null) return;
        I18nManager i18n = I18nManager.getInstance();
        String cur = matSelect.getValue();
        matSelect.getItems().clear();
        matSelect.getItems().addAll(
            i18n.get("nest.mat.beeswax"),
            i18n.get("nest.mat.carton"),
            i18n.get("nest.mat.earth"),
            i18n.get("nest.mat.bivouac"),
            i18n.get("nest.mat.propolis"),
            i18n.get("nest.mat.silk"),
            i18n.get("nest.mat.stercoral"),
            i18n.get("nest.mat.bark"),
            i18n.get("nest.mat.leaf"),
            i18n.get("nest.mat.hollow_wood"),
            i18n.get("nest.mat.planks"),
            i18n.get("nest.mat.paper")
        );
        FXCollections.sort(matSelect.getItems());
        if (cur != null && matSelect.getItems().contains(cur)) {
            matSelect.setValue(cur);
        } else {
            matSelect.getSelectionModel().selectFirst();
        }
    }

    private void populateStageSelect() {
        if (nestStageCombo == null) return;
        I18nManager i18n = I18nManager.getInstance();
        int selIdx = nestStageCombo.getSelectionModel().getSelectedIndex();
        nestStageCombo.getItems().clear();
        nestStageCombo.getItems().addAll(
            i18n.get("nest.stage.incipient"),
            i18n.get("nest.stage.young"),
            i18n.get("nest.stage.mature"),
            i18n.get("nest.stage.supercolony")
        );
        FXCollections.sort(nestStageCombo.getItems());
        if (selIdx >= 0 && selIdx < nestStageCombo.getItems().size()) {
            nestStageCombo.getSelectionModel().select(selIdx);
        } else {
            nestStageCombo.getSelectionModel().select(2);
        }
    }

    private void populateGenusCombo() {
        if (genusSelect == null) return;
        String cat = categorySelect != null ? categorySelect.getValue() : null;
        String curVal = genusSelect.getValue();
        genusSelect.getItems().clear();
        genusSelect.getItems().add("🔬 All Genera");

        if (cat != null) {
            if (cat.contains("Ants")) {
                genusSelect.getItems().addAll(
                    "Atta (Leafcutter Ants)",
                    "Camponotus (Carpenter Ants)",
                    "Lasius (Black Garden Ants)",
                    "Pogonomyrmex (Harvester Ants)",
                    "Solenopsis (Fire Ants)",
                    "Crematogaster (Carton Ants)",
                    "Temnothorax (Twig Ants)",
                    "Eciton (Army Ants)"
                );
            } else if (cat.contains("Honeybees") || cat.contains("Bumblebees")) {
                genusSelect.getItems().addAll("Apis (Honeybees)", "Bombus (Bumblebees)");
            } else if (cat.contains("Wasps")) {
                genusSelect.getItems().addAll("Vespula (Common Wasps)", "Vespa (Hornets)");
            } else if (cat.contains("Termites")) {
                genusSelect.getItems().addAll("Macrotermes (Cathedral Termites)", "Reticulitermes (Subterranean Termites)");
            }
        }
        if (curVal != null && genusSelect.getItems().contains(curVal)) {
            genusSelect.setValue(curVal);
        } else {
            genusSelect.getSelectionModel().selectFirst();
        }
    }

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

        String targetGenus = null;
        if (genusSelect != null && genusSelect.getValue() != null && !genusSelect.getValue().contains("All Genera")) {
            targetGenus = genusSelect.getValue().split(" ")[0].toLowerCase();
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

                boolean categoryMatch = (targetType == null || spType.contains(targetType) ||
                    (targetType.equals("BEE") && (spType.contains("BEE") || spType.contains("WASP"))));

                boolean genusMatch = (targetGenus == null || 
                    (sp != null && sp.getScientificName() != null && sp.getScientificName().toLowerCase().contains(targetGenus)) ||
                    pName.toLowerCase().contains(targetGenus));

                if (categoryMatch && genusMatch) {
                    speciesModelCombo.getItems().add(icon + " " + pName);
                }
            }
            FXCollections.sort(speciesModelCombo.getItems());
            speciesModelCombo.getItems().add("🛠️ Custom Nest Configuration");
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

        Label mainSpecsTitle = new Label();
        mainSpecsTitle.textProperty().bind(i18n.createStringBinding("nest.morphology.section_title"));
        mainSpecsTitle.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-padding:2 0 2 0;-fx-text-fill:#38bdf8;-fx-cursor:hand;");
        mainSpecsTitle.setOnMouseClicked(e -> GlossaryDialog.show("nest"));

        // 1. Insect Category / Family
        Label lblCat = new Label();
        lblCat.textProperty().bind(i18n.createStringBinding("nest.arch.category"));
        lblCat.setStyle("-fx-font-weight:bold;-fx-font-size:11px;-fx-text-fill:#a78bfa;-fx-underline:true;-fx-cursor:hand;");
        lblCat.setOnMouseClicked(e -> GlossaryDialog.show("nest"));
        categorySelect = new ComboBox<>();
        categorySelect.tooltipProperty().bind(i18n.createTooltipBinding("nest.category.select.tt"));
        populateCategorySelect();
        categorySelect.setPrefWidth(270);
        categorySelect.setOnAction(e -> {
            populateGenusCombo();
            filterSpeciesModelComboByCategory(categorySelect.getValue());
            onManualParameterChanged();
        });

        // 2. Insect Genus (Taxonomy filter)
        Label lblGenus = new Label();
        lblGenus.textProperty().bind(i18n.createStringBinding("nest.genus.label"));
        lblGenus.setStyle("-fx-font-weight:bold;-fx-font-size:11px;-fx-text-fill:#c084fc;");
        genusSelect = new ComboBox<>();
        genusSelect.tooltipProperty().bind(i18n.createTooltipBinding("nest.genus.select.tt"));
        populateGenusCombo();
        genusSelect.setPrefWidth(270);
        genusSelect.setOnAction(e -> {
            filterSpeciesModelComboByCategory(categorySelect.getValue());
            onManualParameterChanged();
        });

        // 3. Species Reference Model (Filtered by Category & Genus)
        Label lblSpecies = new Label();
        lblSpecies.textProperty().bind(i18n.createStringBinding("nest.arch.species_ref"));
        lblSpecies.setStyle("-fx-font-weight:bold;-fx-font-size:11px;-fx-text-fill:#38bdf8;");
        speciesModelCombo = new ComboBox<>();
        speciesModelCombo.tooltipProperty().bind(i18n.createTooltipBinding("nest.species.ref.tt"));
        populateSpeciesModelCombo();
        speciesModelCombo.setPrefWidth(270);
        speciesModelCombo.setOnAction(e -> {
            if (!isUpdatingSpeciesCombo) {
                onSpeciesModelSelected();
            }
        });

        // 4. Nest Architecture Type
        Label lblArch = new Label();
        lblArch.textProperty().bind(i18n.createStringBinding("nest.arch.type"));
        lblArch.setStyle("-fx-font-weight:bold;-fx-font-size:11px;-fx-text-fill:#eab308;-fx-underline:true;-fx-cursor:hand;");
        lblArch.setOnMouseClicked(e -> GlossaryDialog.show("nest"));
        archSelect = new ComboBox<>();
        archSelect.tooltipProperty().bind(i18n.createTooltipBinding("nest.arch.type.tt"));
        populateArchSelect();
        archSelect.setPrefWidth(270);
        archSelect.setOnAction(e -> {
            onManualParameterChanged();
            regen();
            repaint();
        });

        // 4. Construction Material
        Label lblMat = new Label();
        lblMat.textProperty().bind(i18n.createStringBinding("nest.arch.material"));
        lblMat.setStyle("-fx-font-weight:bold;-fx-font-size:11px;-fx-text-fill:#22c55e;-fx-underline:true;-fx-cursor:hand;");
        lblMat.setOnMouseClicked(e -> GlossaryDialog.show("materials"));
        matSelect = new ComboBox<>();
        matSelect.tooltipProperty().bind(i18n.createTooltipBinding("nest.arch.material.tt"));
        populateMatSelect();
        matSelect.setPrefWidth(270);
        matSelect.setOnAction(e -> {
            onManualParameterChanged();
            regen();
            repaint();
        });

        // 5. Nest Development Stage / Maturity
        Label lblStage = new Label();
        lblStage.textProperty().bind(i18n.createStringBinding("nest.species.age"));
        lblStage.setStyle("-fx-font-weight:bold;-fx-font-size:11px;-fx-text-fill:#ec4899;");
        nestStageCombo = new ComboBox<>();
        nestStageCombo.tooltipProperty().bind(i18n.createTooltipBinding("nest.species.age.tt"));
        populateStageSelect();
        nestStageCombo.setPrefWidth(270);
        nestStageCombo.setOnAction(e -> {
            if (!isUpdatingSpeciesCombo) {
                onNestStageChanged();
            }
        });

        speciesStatusLabel = new Label(i18n.get("nest.species.status_synced"));
        speciesStatusLabel.setStyle("-fx-font-size:10;-fx-text-fill:#94a3b8;-fx-wrap-text:true;");

        // Morphological Parameters (Auto-calculated from species & adjustable sliders)
        workerSizeSlider  = mkSlider(2.0, 30.0, 5.0);
        workerSizeSlider.tooltipProperty().bind(i18n.createTooltipBinding("nest.worker.size.tt"));

        depthSlider       = mkSlider(4,  60, 20);
        depthSlider.tooltipProperty().bind(i18n.createTooltipBinding("nest.depth.tt"));

        tunnelWidthSlider = mkSlider(1,   5,  2);
        tunnelWidthSlider.tooltipProperty().bind(i18n.createTooltipBinding("nest.tunnel.width.tt"));

        branchingSlider   = mkSlider(1,   5,  3);
        branchingSlider.tooltipProperty().bind(i18n.createTooltipBinding("nest.branching.tt"));

        addLsn(workerSizeSlider, depthSlider, tunnelWidthSlider, branchingSlider);

        Label lblSeed = new Label();
        lblSeed.textProperty().bind(i18n.createStringBinding("nest.seed.label"));
        lblSeed.setStyle("-fx-font-weight:bold;-fx-font-size:11px;-fx-text-fill:#a78bfa;");

        seedField = new TextField(String.valueOf(nestSeed));
        seedField.setPrefWidth(90);
        seedField.tooltipProperty().bind(i18n.createTooltipBinding("nest.seed.tt"));
        seedField.setOnAction(e -> applySeedFromField());
        seedField.focusedProperty().addListener((obs, oldVal, newVal) -> { if (!newVal) applySeedFromField(); });

        Button btnNewSeed = new Button();
        btnNewSeed.textProperty().bind(i18n.createStringBinding("nest.seed.new_btn"));
        btnNewSeed.setGraphic(new FontIcon(Feather.REFRESH_CW));
        btnNewSeed.setStyle("-fx-background-color:#8b5cf6;-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:11px;-fx-padding:5 10;-fx-background-radius:4;");
        btnNewSeed.tooltipProperty().bind(i18n.createTooltipBinding("nest.seed.new_btn.tt"));
        btnNewSeed.setOnAction(e -> {
            setSeed(new Random().nextLong(100000, 999999));
            regen();
            repaint();
            NotificationOverlay.show(this, java.text.MessageFormat.format(i18n.get("nest.seed.applied"), nestSeed), NotificationOverlay.NotificationType.INFO);
        });

        HBox seedRow = new HBox(8, seedField, btnNewSeed);
        seedRow.setAlignment(Pos.CENTER_LEFT);

        Button btnAutoAdapt = new Button();
        btnAutoAdapt.textProperty().bind(i18n.createStringBinding("nest.adapt.btn"));
        btnAutoAdapt.setGraphic(new FontIcon(Feather.ZAP));
        btnAutoAdapt.setStyle("-fx-background-color:#0284c7;-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:11px;-fx-padding:5 12;-fx-background-radius:4;");
        btnAutoAdapt.tooltipProperty().bind(i18n.createTooltipBinding("nest.adapt.btn.tt"));
        btnAutoAdapt.setOnAction(e -> {
            if (activeCustomSpecies != null) {
                configureFromSpecies(activeCustomSpecies);
                NotificationOverlay.show(this, java.text.MessageFormat.format(i18n.get("nest.adapt.success"), activeCustomSpecies.getCommonName()), NotificationOverlay.NotificationType.SUCCESS);
            } else {
                onSpeciesModelSelected();
                String selSp = speciesModelCombo != null && speciesModelCombo.getValue() != null ? speciesModelCombo.getValue() : "?";
                NotificationOverlay.show(this, java.text.MessageFormat.format(i18n.get("nest.adapt.success"), selSp), NotificationOverlay.NotificationType.SUCCESS);
            }
        });

        Label lblWorkerAuto = new Label();
        lblWorkerAuto.textProperty().bind(i18n.createStringBinding("nest.worker.size.label"));
        lblWorkerAuto.setStyle("-fx-font-size:10px;-fx-text-fill:#94a3b8;");

        Label lblTunnelAuto = new Label();
        lblTunnelAuto.textProperty().bind(i18n.createStringBinding("nest.tunnel.label"));
        lblTunnelAuto.setStyle("-fx-font-size:10px;-fx-text-fill:#94a3b8;");

        passageCheckLabel = new Label(i18n.get("nest.passage.ok"));
        passageCheckLabel.setStyle("-fx-font-size:10px;-fx-text-fill:#22c55e;-fx-font-weight:bold;");
        updatePassageCheckLabel();

        workerSizeSlider.valueProperty().addListener((o, oldV, newV) -> updatePassageCheckLabel());
        tunnelWidthSlider.valueProperty().addListener((o, oldV, newV) -> updatePassageCheckLabel());

        Label lblMaxDepth = new Label(); lblMaxDepth.textProperty().bind(i18n.createStringBinding("nest.arch.max_depth"));
        Label lblBranching = new Label(); lblBranching.textProperty().bind(i18n.createStringBinding("nest.arch.branching"));

        VBox masterBlock = new VBox(7,
            mainSpecsTitle, new Separator(),
            lblCat, categorySelect,
            lblGenus, genusSelect,
            lblSpecies, speciesModelCombo,
            lblArch, archSelect,
            lblMat, matSelect,
            lblStage, nestStageCombo,
            speciesStatusLabel,
            new Separator(),
            lblSeed, seedRow,
            btnAutoAdapt,
            lblWorkerAuto, sv(workerSizeSlider),
            lblTunnelAuto, sv(tunnelWidthSlider),
            passageCheckLabel,
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
            {"nest.chambers.queen", "1", "👑 Queen Chamber", "Number of royal chambers reserved for the queen and egg laying."},
            {"nest.chambers.brood", "3", "🥚 Brood Chambers", "Nursery chambers for eggs, larvae, and pupae."},
            {"nest.chambers.food", "4", "🍖 Food Storage", "Storage chambers and pots for food (honey, pollen, seeds, prey)."},
            {"nest.chambers.entrance", "2", "🚪 Entrances", "Number of exits and entrance holes to the outside."},
            {"nest.chambers.waste", "1", "🗑 Waste Dumps", "Waste dumps for refuse, corpses, and debris."},
            {"nest.chambers.fungus", "0", "🍄 Fungus Gardens", "Cultivation chambers for symbiotic fungus (Atta)."}
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
        I18nManager i18n = I18nManager.getInstance();
        Label envTitle = new Label();
        envTitle.textProperty().bind(i18n.createStringBinding("nest.env.title"));
        envTitle.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:#38bdf8;");

        // 1. Height Slider
        evalHeightSlider = mkSlider(-2.0, 15.0, 0.0);
        evalHeightSlider.tooltipProperty().bind(i18n.createTooltipBinding("nest.env.height.tt"));
        evalHeightSlider.valueProperty().addListener((o, a, n) -> updatePlacementViabilityScore());
        HBox heightBox = createEvalSliderRow(i18n.get("nest.env.height.label"), evalHeightSlider, "m");

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

        Label orientLabel = new Label();
        orientLabel.textProperty().bind(i18n.createStringBinding("nest.env.solar.label"));
        HBox orientRow = new HBox(8, orientLabel, evalOrientationCombo);
        orientRow.setAlignment(Pos.CENTER_LEFT);

        // 3. Thermal Temp
        evalTempSlider = mkSlider(5.0, 42.0, 22.0);
        evalTempSlider.tooltipProperty().bind(i18n.createTooltipBinding("nest.env.temp.tt"));
        evalTempSlider.valueProperty().addListener((o, a, n) -> updatePlacementViabilityScore());
        HBox tempBox = createEvalSliderRow(i18n.get("nest.env.temp.label"), evalTempSlider, "°C");

        // 4. Substrate Moisture
        evalMoistureSlider = mkSlider(0.0, 100.0, 45.0);
        evalMoistureSlider.tooltipProperty().bind(i18n.createTooltipBinding("nest.env.moisture.tt"));
        evalMoistureSlider.valueProperty().addListener((o, a, n) -> updatePlacementViabilityScore());
        HBox moistureBox = createEvalSliderRow(i18n.get("nest.env.moisture.label"), evalMoistureSlider, "%");

        // 5. Foraging Radius
        evalForagingSlider = mkSlider(5.0, 300.0, 35.0);
        evalForagingSlider.tooltipProperty().bind(i18n.createTooltipBinding("nest.env.foraging.tt"));
        evalForagingSlider.valueProperty().addListener((o, a, n) -> updatePlacementViabilityScore());
        HBox foragingBox = createEvalSliderRow(i18n.get("nest.env.foraging.label"), evalForagingSlider, "m");

        // 6. Compaction
        evalCompactionSlider = mkSlider(10.0, 150.0, 65.0);
        evalCompactionSlider.tooltipProperty().bind(i18n.createTooltipBinding("nest.env.compaction.tt"));
        evalCompactionSlider.valueProperty().addListener((o, a, n) -> updatePlacementViabilityScore());
        HBox compactionBox = createEvalSliderRow(i18n.get("nest.env.compaction.label"), evalCompactionSlider, "kPa");

        VBox block = new VBox(6,
            envTitle, new Separator(),
            heightBox, orientRow, tempBox, moistureBox, foragingBox, compactionBox
        );
        block.getStyleClass().add("card-pane");
        return block;
    }

    private VBox buildPlacementViabilityCard() {
        I18nManager i18n = I18nManager.getInstance();
        Label evalTitle = new Label();
        evalTitle.textProperty().bind(i18n.createStringBinding("nest.viability.title"));
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
        evalRecommendationsBox.getStyleClass().add("card-pane");

        Label viabilityScoreLabel = new Label();
        viabilityScoreLabel.textProperty().bind(i18n.createStringBinding("nest.viability.score.label"));
        Label viabilityDiagLabel = new Label();
        viabilityDiagLabel.textProperty().bind(i18n.createStringBinding("nest.viability.diag.label"));
        VBox block = new VBox(6,
            evalTitle, new Separator(),
            viabilityScoreLabel,
            scoreHeaderBox,
            viabilityDiagLabel,
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
                addEvalRec("⚠️ Wooden Beehive: Risk of ground moisture and ground predators. Elevate to >= 0.5m.");
            } else if (height > 2.5) {
                score -= 15.0;
                addEvalRec("ℹ️ High Elevation: Strong wind exposure may perturb take-off and landing.");
            } else {
                addEvalRec("✅ Ideal Elevation (0.5m - 2.0m): Ground protection and insulation.");
            }
        } else if (arch.contains("PAPER_PEDUNCULATE")) {
            if (height < 2.5) {
                score -= 35.0;
                addEvalRec("🚨 Hanging Nest: Height < 2.5m vulnerable to terrestrial predators.");
            } else {
                addEvalRec("✅ Optimal Aerial Anchor: Peduncle attached high up, safe from ground.");
            }
        } else if (arch.contains("BURROW") || arch.contains("FUNGI_VAULT")) {
            if (height > 0.5) {
                score -= 30.0;
                addEvalRec("⚠️ Subterranean gallery placed above soil surface.");
            } else {
                addEvalRec("✅ Ideal Depth: Natural thermal soil protection.");
            }
        }

        // 2. Thermal Microclimate
        if (temp < 15.0) { score -= 20.0; addEvalRec("⚠️ Cool ambient temperature (<15°C): Brood development slowed."); }
        else if (temp > 35.0) { score -= 25.0; addEvalRec("🚨 Thermal Overheating (>35°C): Risk of wax melting or mortality."); }
        else { addEvalRec("✅ Optimal thermal microclimate (18°C - 30°C)."); }

        // 3. Moisture
        if (moisture < 20.0) { score -= 20.0; addEvalRec("⚠️ Substrate Desiccation (<20%): Risk of brood dehydration."); }
        else if (moisture > 80.0) { score -= 20.0; addEvalRec("⚠️ Water Saturation (>80%): Risk of fungal mold development."); }
        else { addEvalRec("✅ Balanced substrate moisture level."); }

        // 4. Foraging
        if (foraging > 150.0) { score -= 20.0; addEvalRec("⚠️ High Foraging Distance (>150m): High flight energy expenditure."); }
        else { addEvalRec("✅ Immediate proximity to floral and water resources."); }

        // 5. Compaction
        if (compaction < 30.0) { score -= 15.0; addEvalRec("⚠️ Unstable Loose Substrate (<30 kPa): Collapse risk."); }

        // 6. Colony Population vs Nest Capacity Validation & Spillover Diagnostics
        int totalChambers = (int) getChamberCount();
        int estNestCap = Math.max(40, totalChambers * 25);
        int estPop = activeCustomSpecies != null ? activeCustomSpecies.getTypicalColonySize() : 300;
        int queenChambers = getSp("👑 Queen Chamber");
        int foodChambers = getSp("🍖 Food Storage");

        if (estPop > estNestCap) {
            score -= 15.0;
            addEvalRec(String.format("⚠️ Nest Capacity (~%d ind.) < Population (%d ind.). The %d overflow individuals will emerge on the surface around the crater.", estNestCap, estPop, estPop - estNestCap));
        } else {
            addEvalRec(String.format("✅ Nest capacity (~%d ind.) sufficient for initial population (%d ind.).", estNestCap, estPop));
        }

        if (queenChambers == 0) {
            score -= 20.0;
            addEvalRec("🚨 Missing Queen Chamber (0): The queen and eggs will have no dedicated chamber.");
        }
        if (foodChambers == 0) {
            score -= 10.0;
            addEvalRec("⚠️ Absence of Storage Granaries: Risk of starvation or brood cluttering.");
        }

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
        boolean isDark = ThemeManager.getInstance().getCurrentTheme() == ThemeManager.Theme.DARK;
        String textFill = isDark ? "#e4e4e7" : "#0f172a";
        l.setStyle("-fx-font-size: 10px; -fx-text-fill: " + textFill + "; -fx-wrap-text: true;");
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
        canvas3D   = new ResizableCanvas(540, 510); gc3D   = canvas3D.getGraphicsContext2D();
        canvasSide = new ResizableCanvas(215, 245); gcSide = canvasSide.getGraphicsContext2D();
        canvasTop  = new ResizableCanvas(215, 245); gcTop  = canvasTop.getGraphicsContext2D();

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
        bar.getStyleClass().add("legend-bar");

        syncViewsCheckBox = new CheckBox();
        syncViewsCheckBox.textProperty().bind(i18n.createStringBinding("nest.chk.sync_views"));
        syncViewsCheckBox.setSelected(true);
        syncViewsCheckBox.getStyleClass().add("legend-checkbox");

        showGhostMeshCheckBox = new CheckBox();
        showGhostMeshCheckBox.textProperty().bind(i18n.createStringBinding("nest.chk.ghost_mesh"));
        showGhostMeshCheckBox.setSelected(true);
        showGhostMeshCheckBox.getStyleClass().add("legend-checkbox");
        showGhostMeshCheckBox.setTooltip(new Tooltip());
        showGhostMeshCheckBox.getTooltip().textProperty().bind(i18n.createStringBinding("nest.chk.ghost_mesh.tt"));
        showGhostMeshCheckBox.setOnAction(e -> repaint());

        Label title = new Label();
        title.textProperty().bind(i18n.createStringBinding("nest.legend.title"));
        title.getStyleClass().add("legend-title");

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
            item.getStyleClass().add("legend-item");
            Canvas dot = new Canvas(9, 9);
            GraphicsContext g = dot.getGraphicsContext2D();
            g.setFill(Color.web(it[1]));
            g.fillOval(0, 0, 9, 9);
            g.setStroke(Color.WHITE);
            g.setLineWidth(0.5);
            g.strokeOval(0, 0, 9, 9);

            Label lbl = new Label();
            lbl.textProperty().bind(i18n.createStringBinding(it[0]));
            item.getChildren().addAll(dot, lbl);
            bar.getChildren().add(item);
        }

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label hint = new Label();
        hint.textProperty().bind(i18n.createStringBinding("nest.legend.hint"));
        hint.getStyleClass().add("legend-hover-info");

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
        TextInputDialog dialog = org.swarmforge.client.util.ThemeManager.createTextInputDialog(defaultName);
        dialog.setTitle("Save Preset"); dialog.setHeaderText("Preset Name:"); dialog.setContentText("Name:");
        dialog.showAndWait().ifPresent(name -> {
            if (name == null || name.isBlank()) return;
            String clean = name.trim();
            if (presetMgr.contains(clean)) {
                Alert confirmAlert = org.swarmforge.client.util.ThemeManager.createAlert(
                    Alert.AlertType.CONFIRMATION,
                    "Nest preset '" + clean + "' already exists.\n\nDo you want to overwrite it with current configuration?"
                );
                confirmAlert.setTitle("Overwrite Existing Preset");
                confirmAlert.setHeaderText("Overwrite Confirmation");
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
            NotificationOverlay.show(this, "Preset \"" + clean + "\" saved.", NotificationOverlay.NotificationType.SUCCESS);
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
                speciesModelCombo.setValue("✨ Custom Active Species");
            }
            if (speciesStatusLabel != null) {
                speciesStatusLabel.setText("Nest parameters modified (Custom nest).");
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
                if ("SUBTERRANEAN_FUNGI_VAULT".equalsIgnoreCase(nestTypeStr) || cName.contains("fungus") || cName.contains("leafcutter") || cName.contains("champignonniste") || cName.contains("coupeuse")) {
                    setArchSelectValue("Subterranean Fungi Vault");
                    setMatSelectValue("Earth & Clay Soil");
                } else if ("CARTON_NEST".equalsIgnoreCase(nestTypeStr) || cName.contains("carton")) {
                    setArchSelectValue("Arboreal Carton Nest");
                    setMatSelectValue("Carton & Wood Pulp");
                } else if ("BAMBOO_STEM_NEST".equalsIgnoreCase(nestTypeStr) || cName.contains("temnothorax") || cName.contains("gall") || cName.contains("stem") || cName.contains("tige")) {
                    setArchSelectValue("Bamboo Stem & Gall");
                    setMatSelectValue("Wood Pulp Paper (Vespidae)");
                } else if ("BIVOUAC_LIVING_NEST".equalsIgnoreCase(nestTypeStr) || cName.contains("army") || cName.contains("legionary") || cName.contains("légionnaire") || cName.contains("eciton")) {
                    setArchSelectValue("Bivouac Living Nest");
                    setMatSelectValue("Living Insect Bodies (Bivouac)");
                } else if ("ARBOREAL_SILK_LEAF".equalsIgnoreCase(nestTypeStr) || cName.contains("weaver") || cName.contains("tisserande")) {
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

            // Synchronize Genus selector
            String sciName = species.getScientificName() != null ? species.getScientificName().toLowerCase() : "";
            if (genusSelect != null) {
                if (sciName.contains("vespa")) genusSelect.setValue("Vespa (Hornets)");
                else if (sciName.contains("vespula")) genusSelect.setValue("Vespula (Yellowjackets)");
                else if (sciName.contains("apis")) genusSelect.setValue("Apis (Honey Bees)");
                else if (sciName.contains("bombus")) genusSelect.setValue("Bombus (Bumble Bees)");
                else if (sciName.contains("atta")) genusSelect.setValue("Atta (Leafcutter Ants)");
                else if (sciName.contains("camponotus")) genusSelect.setValue("Camponotus (Carpenter Ants)");
                else if (sciName.contains("lasius")) genusSelect.setValue("Lasius (Black Garden Ants)");
                else if (sciName.contains("pogonomyrmex")) genusSelect.setValue("Pogonomyrmex (Harvester Ants)");
                else if (sciName.contains("solenopsis")) genusSelect.setValue("Solenopsis (Fire Ants)");
                else if (sciName.contains("crematogaster")) genusSelect.setValue("Crematogaster (Acrobat Ants)");
                else if (sciName.contains("temnothorax")) genusSelect.setValue("Temnothorax (Acorn Ants)");
                else if (sciName.contains("eciton")) genusSelect.setValue("Eciton (Army Ants)");
                else if (sciName.contains("macrotermes")) genusSelect.setValue("Macrotermes (Cathedral Termites)");
                else if (sciName.contains("reticulitermes")) genusSelect.setValue("Reticulitermes (Subterranean Termites)");
            }

            // 2. Body length & Tunnel diameter clearance
            float avgBodyMm = species.getAverageCasteBodyLengthMm();
            workerSizeSlider.setValue(Math.max(2.0, Math.min(30.0, avgBodyMm)));

            // Scale tunnel clearance so passage clearance is fluid (>= 1.25x worker size)
            double reqTunnelScale = Math.max(2.0, Math.min(5.0, Math.ceil((avgBodyMm * 1.25) / 3.0)));
            tunnelWidthSlider.setValue(reqTunnelScale);

            // 3. Nest Development Stage Multipliers (Foundation, Established, Mature, Giant Nest)
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
                                     (species.getCommonName() != null && (species.getCommonName().toLowerCase().contains("fungus") ||
                                     species.getCommonName().toLowerCase().contains("leafcutter") ||
                                     species.getCommonName().toLowerCase().contains("champignonniste") ||
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
                speciesStatusLabel.setText("Nest synchronized (" + species.getCommonName() + ")");
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
                    speciesModelCombo.setValue("✨ Custom Active Species");
                }
            }
        } finally {
            isUpdatingSpeciesCombo = false;
        }

        regen();
        repaint();
    }

    private void updatePassageCheckLabel() {
        if (passageCheckLabel == null) return;
        double wMm = workerSizeSlider != null ? workerSizeSlider.getValue() : 4.0;
        double tunnelScale = tunnelWidthSlider != null ? tunnelWidthSlider.getValue() : 2.0;
        // Scaled gallery clearance relative to worker size
        double effectiveWidthMm = Math.max(tunnelScale * 3.0, (tunnelScale / 2.0) * wMm * 1.25);

        if (effectiveWidthMm < wMm * 0.95) {
            passageCheckLabel.setText("⚠️ Tunnel too narrow for workers (" + String.format("%.1f", wMm) + "mm)! Risk of blockage.");
            passageCheckLabel.setStyle("-fx-font-size:10px;-fx-text-fill:#ef4444;-fx-font-weight:bold;");
        } else if (effectiveWidthMm < wMm * 1.2) {
            passageCheckLabel.setText("⚡ Narrow passage (Single file movement - Workers " + String.format("%.1f", wMm) + "mm)");
            passageCheckLabel.setStyle("-fx-font-size:10px;-fx-text-fill:#eab308;-fx-font-weight:bold;");
        } else {
            passageCheckLabel.setText("✅ Fluid tunnel (Smooth passage for workers, majors, queen - " + String.format("%.1f", effectiveWidthMm) + "mm)");
            passageCheckLabel.setStyle("-fx-font-size:10px;-fx-text-fill:#22c55e;-fx-font-weight:bold;");
        }
    }

    private void onSpeciesModelSelected() {
        String sel = speciesModelCombo.getValue();
        if (sel == null) return;

        if (sel.contains("Custom Nest Configuration") || sel.contains("Custom Species")) {
            if (speciesStatusLabel != null) {
                speciesStatusLabel.setText("Custom Parameters (Nest not linked to a preset).");
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
            if (sel != null && !sel.contains("Active Custom Species")) {
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

    private void applySeedFromField() {
        if (seedField == null) return;
        try {
            long val = Long.parseLong(seedField.getText().trim());
            if (val != nestSeed) {
                nestSeed = val;
                regen();
                repaint();
            }
        } catch (NumberFormatException ex) {
            seedField.setText(String.valueOf(nestSeed));
        }
    }

    public long getSeed() { return nestSeed; }
    public void setSeed(long seed) {
        this.nestSeed = seed;
        if (seedField != null) {
            seedField.setText(String.valueOf(seed));
        }
    }

    public void applyCfg(Map<String, Object> cfg) {
        if (cfg == null) return;
        isUpdatingSpeciesCombo = true;
        try {
            if (cfg.containsKey("seed")) {
                try {
                    setSeed(((Number) cfg.get("seed")).longValue());
                } catch (Exception ignored) {}
            }
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
            if (cfg.containsKey("envHeight") && evalHeightSlider != null) evalHeightSlider.setValue(num(cfg, "envHeight"));
            if (cfg.containsKey("envOrientation") && evalOrientationCombo != null) evalOrientationCombo.setValue(String.valueOf(cfg.get("envOrientation")));
            if (cfg.containsKey("envTemp") && evalTempSlider != null) evalTempSlider.setValue(num(cfg, "envTemp"));
            if (cfg.containsKey("envMoisture") && evalMoistureSlider != null) evalMoistureSlider.setValue(num(cfg, "envMoisture"));
            if (cfg.containsKey("envForaging") && evalForagingSlider != null) evalForagingSlider.setValue(num(cfg, "envForaging"));
            if (cfg.containsKey("envCompaction") && evalCompactionSlider != null) evalCompactionSlider.setValue(num(cfg, "envCompaction"));
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
        c.put("seed",         nestSeed);
        c.put("taxonCategory",categorySelect.getValue());
        c.put("architecture", archSelect.getValue());
        c.put("material",     matSelect.getValue());
        c.put("workerSizeMm", workerSizeSlider.getValue());
        c.put("depth",        (int) depthSlider.getValue());
        c.put("chamberCount", (int) getChamberCount());
        c.put("tunnelWidth",  (int) tunnelWidthSlider.getValue());
        c.put("branching",    (int) branchingSlider.getValue());
        c.put("nestStageIndex", nestStageCombo != null ? nestStageCombo.getSelectionModel().getSelectedIndex() : 2);
        c.put("envHeight", evalHeightSlider != null ? evalHeightSlider.getValue() : 0.0);
        c.put("envOrientation", evalOrientationCombo != null ? evalOrientationCombo.getValue() : "East (Morning Light)");
        c.put("envTemp", evalTempSlider != null ? evalTempSlider.getValue() : 22.0);
        c.put("envMoisture", evalMoistureSlider != null ? evalMoistureSlider.getValue() : 45.0);
        c.put("envForaging", evalForagingSlider != null ? evalForagingSlider.getValue() : 35.0);
        c.put("envCompaction", evalCompactionSlider != null ? evalCompactionSlider.getValue() : 65.0);
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

    private void draw3D()   { if (canvas3D != null && gc3D != null && canvas3D.getWidth() >= 10 && canvas3D.getHeight() >= 10 && nest != null) NestRenderer.draw3D(nest, gc3D, canvas3D.getWidth(), canvas3D.getHeight(), azimuth, elevation, zoom, getTunnelWidth(), pan3DX, pan3DY, showGhostMeshCheckBox == null || showGhostMeshCheckBox.isSelected()); }
    private void drawSide() { if (canvasSide != null && gcSide != null && canvasSide.getWidth() >= 10 && canvasSide.getHeight() >= 10 && nest != null) NestRenderer.drawSide(nest, gcSide, canvasSide.getWidth(), canvasSide.getHeight(), getTunnelWidth(), sideZoom, sidePanX, sidePanY); }
    private void drawTop()  { if (canvasTop != null && gcTop != null && canvasTop.getWidth() >= 10 && canvasTop.getHeight() >= 10 && nest != null) NestRenderer.drawTop(nest,  gcTop,  canvasTop.getWidth(),  canvasTop.getHeight(),  getTunnelWidth(), topZoom, topPanX, topPanY); }

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

