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
import javafx.stage.Stage;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.NotificationOverlay;
import org.swarmforge.client.ui.nest.NestPreviewAreaPane;
import org.swarmforge.client.ui.nest.NestSpeciesArchetypeAdapter;
import org.swarmforge.client.ui.nest.NestTaxonomyConfigPane;
import org.swarmforge.client.ui.nest.NestPlacementViabilityPane;
import org.swarmforge.client.util.ThemeManager;

public class NestGeneratorPane extends BorderPane {
    private final I18nManager i18n = I18nManager.getInstance();
    private final org.swarmforge.client.ui.SpeciesPresetManager speciesPresetMgr = new org.swarmforge.client.ui.SpeciesPresetManager();

        // Interactive multi-view preview canvas pane
    private NestPreviewAreaPane previewPane;

    // Modular sub-panes
    private NestTaxonomyConfigPane taxonomyPane;
    private NestPlacementViabilityPane viabilityPane;

    private Label passageCheckLabel;
    private org.swarmforge.core.species.DefaultSpecies activeDefaultSpecies;
    private boolean isUpdatingSpeciesCombo = false;

    private Slider workerSizeSlider;
    private Slider depthSlider, tunnelWidthSlider, branchingSlider;
    private Label lblTotalChambersValue;
    private final Map<String, Spinner<Integer>> chamberSpinners = new LinkedHashMap<>();

    // Seed Control
    private long nestSeed = 123456L;
    private TextField seedField;

    // Presets
    private ComboBox<String> presetsCombo;
    private final NestPresetManager presetMgr = new NestPresetManager();
    private boolean isDirty = false;
    private String lastSelectedPreset = null;

    public boolean isDirty() {
        return isDirty;
    }

    public boolean promptUnsavedChanges() {
        if ("true".equals(System.getProperty("swarmforge.test")) || Boolean.getBoolean("headless") || Boolean.getBoolean("testfx.headless")) return true;
        if (!isDirty) return true;
        String currentName = lastSelectedPreset != null ? lastSelectedPreset : "";
        boolean hasCurrentPreset = !currentName.isEmpty();

        Alert alert = org.swarmforge.client.util.ThemeManager.createAlert(
            Alert.AlertType.CONFIRMATION,
            i18n.get("dialog.unsaved.nest_editor") + "\n"
            + (hasCurrentPreset ? i18n.get("preset.current") + ": \"" + currentName + "\"" : "")
        );
        alert.setTitle(i18n.get("common.dialog.unsaved_title"));
        alert.setHeaderText(i18n.get("dialog.exit.nest_editor"));
        alert.getDialogPane().setPrefWidth(480);
        alert.getDialogPane().setMaxWidth(500);

        ButtonType btnUpdate  = hasCurrentPreset
            ? new ButtonType("💾 " + i18n.get("preset.update") + " \"" + currentName + "\"", ButtonBar.ButtonData.OK_DONE)
            : null;
        ButtonType btnSaveAs  = new ButtonType("📝 " + i18n.get("preset.save_as"), ButtonBar.ButtonData.OTHER);
        ButtonType btnDiscard = new ButtonType("🗑 " + i18n.get("preset.discard"), ButtonBar.ButtonData.OTHER);
        ButtonType btnCancel  = new ButtonType(i18n.get("common.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

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
        confirmAlert.setHeaderText(i18n.get("preset.delete.header.nest"));

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

    private ScrollPane buildConfig() {
        I18nManager i18n = I18nManager.getInstance();
        VBox cfg = new VBox(10);
        cfg.setPadding(new Insets(10));
        cfg.setPrefWidth(330);

        taxonomyPane = new NestTaxonomyConfigPane(
            this::onManualParameterChanged,
            () -> { regen(); repaint(); },
            this::configureFromSpecies,
            stageIdx -> {
                if (activeDefaultSpecies != null) {
                    NestSpeciesArchetypeAdapter.applyStageScaling(this, activeDefaultSpecies, stageIdx);
                }
            }
        );

        // Morphological Parameters (Auto-calculated from species & adjustable sliders)
        workerSizeSlider  = mkSlider(2.0, 30.0, 5.0);
        workerSizeSlider.tooltipProperty().bind(i18n.createTooltipBinding("nest.worker.size.tt"));

        depthSlider       = mkSlider(0.5, 8.0, 2.5);
        depthSlider.tooltipProperty().bind(i18n.createTooltipBinding("nest.depth.tt"));

        tunnelWidthSlider = mkSlider(2.0, 25.0, 4.0);
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
            if (activeDefaultSpecies != null) {
                configureFromSpecies(activeDefaultSpecies);
                NotificationOverlay.show(this, java.text.MessageFormat.format(i18n.get("nest.adapt.success"), activeDefaultSpecies.getCommonName()), NotificationOverlay.NotificationType.SUCCESS);
            } else {
                onSpeciesModelSelected();
                String selSp = getSpeciesModelCombo() != null && getSpeciesModelCombo().getValue() != null ? getSpeciesModelCombo().getValue() : "?";
                NotificationOverlay.show(this, java.text.MessageFormat.format(i18n.get("nest.adapt.success"), selSp), NotificationOverlay.NotificationType.SUCCESS);
            }
        });

        Button btnInspectHive = new Button("ðŸ " + i18n.get("hive.inspect.button", "Inspection Apicole (Cadres)"));
        btnInspectHive.setPrefWidth(270);
        btnInspectHive.setTooltip(new Tooltip(i18n.get("hive.inspect.tooltip", "Ouvrir l'inspection apicole dÃ©taillÃ©e des 10 cadres Dadant")));
        btnInspectHive.setOnAction(e -> {
            Stage owner = getScene() != null && getScene().getWindow() instanceof Stage ? (Stage) getScene().getWindow() : null;
            new HiveInspectionDialog(owner, null).show();
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
            taxonomyPane,
            new Separator(),
            lblSeed, seedRow,
            btnAutoAdapt,
            btnInspectHive,
            lblWorkerAuto, sv(workerSizeSlider, "mm"),
            lblTunnelAuto, sv(tunnelWidthSlider, "mm"),
            passageCheckLabel,
            lblMaxDepth,   sv(depthSlider, "m"),
            lblBranching,  sv(branchingSlider, "")
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
            {"nest.chambers.queen", "1", "ðŸ‘‘ Queen Chamber", "Number of royal chambers reserved for the queen and egg laying."},
            {"nest.chambers.brood", "3", "ðŸ¥š Brood Chambers", "Nursery chambers for eggs, larvae, and pupae."},
            {"nest.chambers.food", "4", "ðŸ– Food Storage", "Storage chambers and pots for food (honey, pollen, seeds, prey)."},
            {"nest.chambers.entrance", "2", "ðŸšª Entrances", "Number of exits and entrance holes to the outside."},
            {"nest.chambers.waste", "1", "ðŸ—‘ Waste Dumps", "Waste dumps for refuse, corpses, and debris."},
            {"nest.chambers.fungus", "0", "ðŸ„ Fungus Gardens", "Cultivation chambers for symbiotic fungus (Atta)."}
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

        // Spatial Viability Diagnostics Card
        viabilityPane = new NestPlacementViabilityPane(
            this::getArchitecture,
            () -> (int) getChamberCount(),
            () -> activeDefaultSpecies,
            this::getSp
        );

        cfg.getChildren().addAll(masterBlock, cdBlock, viabilityPane);
        ScrollPane sc = new ScrollPane(cfg);
        sc.setFitToWidth(true); sc.setPrefWidth(360); sc.setMaxWidth(360);
        return sc;
    }

    public void populateSpeciesModelCombo() {
        if (taxonomyPane != null) taxonomyPane.populateSpeciesModelCombo();
    }

    public void populateGenusCombo() {
        if (taxonomyPane != null) taxonomyPane.populateGenusCombo();
    }

    public void filterSpeciesModelComboByCategory(String category) {
        if (taxonomyPane != null) taxonomyPane.filterSpeciesModelComboByCategory(category);
    }

    public void updatePlacementViabilityScore() {
        if (viabilityPane != null) viabilityPane.updatePlacementViabilityScore();
    }

    public void setCategorySelectValue(String val) {
        if (taxonomyPane != null) taxonomyPane.setCategorySelectValue(val);
    }

    public void setArchSelectValue(String val) {
        if (taxonomyPane != null) taxonomyPane.setArchSelectValue(val);
    }

    public void setMatSelectValue(String val) {
        if (taxonomyPane != null) taxonomyPane.setMatSelectValue(val);
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

    private HBox sv(Slider s, String unit) {
        HBox b = new HBox(8); b.setAlignment(Pos.CENTER_LEFT);
        String unitSuffix = (unit != null && !unit.isEmpty()) ? " " + unit : "";
        Label v = new Label(fmt(s.getValue()) + unitSuffix);
        v.setStyle("-fx-text-fill:#00d4ff;-fx-min-width:48;-fx-font-weight:bold;");
        s.valueProperty().addListener((o,a,n) -> v.setText(fmt(n.doubleValue()) + unitSuffix));
        b.getChildren().addAll(s, v);
        return b;
    }

    private HBox sv(Slider s) {
        return sv(s, "");
    }

    private String fmt(double d) { return String.format("%.1f", d); }

        // View area

    private VBox buildViews() {
        previewPane = new NestPreviewAreaPane(() -> nest, this::getTunnelWidth);
        return previewPane;
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

    public void updateTotalChambers() {
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
            I18nManager i18n = I18nManager.getInstance();
            if (getSpeciesModelCombo() != null) {
                getSpeciesModelCombo().setValue(i18n.get("nest.species.custom_config"));
            }
            if (getSpeciesStatusLabel() != null) {
                getSpeciesStatusLabel().setText(i18n.get("nest.status.custom_modified"));
            }
            if (presetsCombo != null && presetsCombo.getSelectionModel().getSelectedItem() != null) {
                presetsCombo.getSelectionModel().clearSelection();
            }
        } finally {
            isUpdatingSpeciesCombo = false;
        }
    }

    public void updatePassageCheckLabel() {
        if (passageCheckLabel == null) return;
        I18nManager i18n = I18nManager.getInstance();
        double wMm = workerSizeSlider != null ? workerSizeSlider.getValue() : 4.0;
        double tunnelScale = tunnelWidthSlider != null ? tunnelWidthSlider.getValue() : 2.0;
        double effectiveWidthMm = Math.max(tunnelScale * 3.0, (tunnelScale / 2.0) * wMm * 1.25);

        if (effectiveWidthMm < wMm * 0.95) {
            passageCheckLabel.setText(String.format(i18n.get("nest.tunnel.narrow"), wMm));
            passageCheckLabel.setStyle("-fx-font-size:10px;-fx-text-fill:#ef4444;-fx-font-weight:bold;");
        } else if (effectiveWidthMm < wMm * 1.2) {
            passageCheckLabel.setText(String.format(i18n.get("nest.tunnel.single_file"), wMm));
            passageCheckLabel.setStyle("-fx-font-size:10px;-fx-text-fill:#eab308;-fx-font-weight:bold;");
        } else {
            passageCheckLabel.setText(String.format(i18n.get("nest.tunnel.fluid"), effectiveWidthMm));
            passageCheckLabel.setStyle("-fx-font-size:10px;-fx-text-fill:#22c55e;-fx-font-weight:bold;");
        }
    }

    public void onSpeciesModelSelected() {
        String sel = getSpeciesModelCombo() != null ? getSpeciesModelCombo().getValue() : null;
        if (sel == null) return;
        I18nManager i18n = I18nManager.getInstance();

        if (sel.contains("Custom Nest Configuration") || sel.contains("Custom Species") || sel.equals(i18n.get("nest.species.custom_config"))) {
            if (getSpeciesStatusLabel() != null) {
                getSpeciesStatusLabel().setText(i18n.get("nest.status.custom_unlinked"));
            }
            return;
        }

        org.swarmforge.client.ui.SpeciesPresetManager presetManager = speciesPresetMgr;
        String cleanSel = sel.replaceAll("^[^a-zA-Z0-9(]+\\s*", "").trim();
        for (String presetName : presetManager.getPresetNames()) {
            if (cleanSel.equalsIgnoreCase(presetName) || cleanSel.toLowerCase().contains(presetName.toLowerCase()) || presetName.toLowerCase().contains(cleanSel.toLowerCase())) {
                configureFromSpecies(presetManager.getPreset(presetName));
                return;
            }
        }
    }

    public void configureFromSpecies(org.swarmforge.core.species.DefaultSpecies species) {
        if (species == null) return;
        this.activeDefaultSpecies = species;
        NestSpeciesArchetypeAdapter.configureFromSpecies(this, species);
    }

    private void onNestStageChanged() {
        int stageIndex = getNestStageCombo() != null ? getNestStageCombo().getSelectionModel().getSelectedIndex() : 2;
        if (stageIndex < 0) stageIndex = 2;
        if (activeDefaultSpecies != null) {
            NestSpeciesArchetypeAdapter.applyStageScaling(this, activeDefaultSpecies, stageIndex);
        } else {
            String sel = getSpeciesModelCombo() != null ? getSpeciesModelCombo().getValue() : null;
            if (sel != null && !sel.contains("Custom")) {
                org.swarmforge.client.ui.SpeciesPresetManager mgr = speciesPresetMgr;
                String cleanSel = sel.replaceAll("^[^a-zA-Z0-9(]+\\s*", "").trim();
                for (String presetName : mgr.getPresetNames()) {
                    if (cleanSel.equalsIgnoreCase(presetName) || cleanSel.toLowerCase().contains(presetName.toLowerCase()) || presetName.toLowerCase().contains(cleanSel.toLowerCase())) {
                        NestSpeciesArchetypeAdapter.applyStageScaling(this, mgr.getPreset(presetName), stageIndex);
                        return;
                    }
                }
            }
        }
    }
    public void setUpdatingSpeciesCombo(boolean updating) {
        this.isUpdatingSpeciesCombo = updating;
        if (taxonomyPane != null) taxonomyPane.setUpdatingSpeciesCombo(updating);
    }
    public ComboBox<String> getCategorySelect() { return taxonomyPane != null ? taxonomyPane.getCategorySelect() : null; }
    public ComboBox<String> getGenusSelect() { return taxonomyPane != null ? taxonomyPane.getGenusSelect() : null; }
    public ComboBox<String> getSpeciesModelCombo() { return taxonomyPane != null ? taxonomyPane.getSpeciesModelCombo() : null; }
    public ComboBox<String> getNestStageCombo() { return taxonomyPane != null ? taxonomyPane.getNestStageCombo() : null; }
    public Label getSpeciesStatusLabel() { return taxonomyPane != null ? taxonomyPane.getSpeciesStatusLabel() : null; }
    public Slider getBranchingSlider() { return branchingSlider; }
    public Slider getDepthSlider() { return depthSlider; }
    public Slider getWorkerSizeSlider() { return workerSizeSlider; }
    public Slider getTunnelWidthSlider() { return tunnelWidthSlider; }
private double num(Map<String,Object> m, String k) { return ((Number)m.get(k)).doubleValue(); }
    public void setSp(String k, int v) { Spinner<Integer> s = chamberSpinners.get(k); if (s!=null) s.getValueFactory().setValue(v); }
    private int getSp(String k) { Spinner<Integer> s = chamberSpinners.get(k); return s!=null ? s.getValue() : 0; }

    public void regen()   { nest = NestAlgorithm.generate(this); }
    public void repaint() { if (previewPane != null) previewPane.repaint(); }

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
            ComboBox<String> catSel = getCategorySelect();
            if (cfg.containsKey("taxonCategory") && catSel != null) {
                setCategorySelectValue(String.valueOf(cfg.get("taxonCategory")));
                populateGenusCombo();
            }
            ComboBox<String> gSel = getGenusSelect();
            if (cfg.containsKey("genus") && gSel != null) {
                String savedGenus = String.valueOf(cfg.get("genus"));
                if (!savedGenus.isEmpty()) {
                    if (gSel.getItems().contains(savedGenus)) {
                        gSel.setValue(savedGenus);
                    } else {
                        for (String item : gSel.getItems()) {
                            if (item.toLowerCase().startsWith(savedGenus.toLowerCase()) || item.toLowerCase().contains(savedGenus.toLowerCase())) {
                                gSel.setValue(item);
                                break;
                            }
                        }
                    }
                }
            }
            if (cfg.containsKey("architecture")) setArchSelectValue(String.valueOf(cfg.get("architecture")));
            if (cfg.containsKey("material")) setMatSelectValue(String.valueOf(cfg.get("material")));
            if (cfg.containsKey("workerSizeMm") && workerSizeSlider != null) workerSizeSlider.setValue(num(cfg, "workerSizeMm"));
            if (cfg.containsKey("depth") && depthSlider != null) depthSlider.setValue(num(cfg, "depth"));
            if (cfg.containsKey("tunnelWidth") && tunnelWidthSlider != null) tunnelWidthSlider.setValue(num(cfg, "tunnelWidth"));
            if (cfg.containsKey("branching") && branchingSlider != null) branchingSlider.setValue(num(cfg, "branching"));
            ComboBox<String> stgCombo = getNestStageCombo();
            if (cfg.containsKey("nestStageIndex") && stgCombo != null) {
                int idx = ((Number) cfg.get("nestStageIndex")).intValue();
                if (idx >= 0 && idx < stgCombo.getItems().size()) stgCombo.getSelectionModel().select(idx);
            }
            if (cfg.containsKey("chamberDistribution") && cfg.get("chamberDistribution") instanceof Map<?,?> distMap) {
                distMap.forEach((k, v) -> setSp(String.valueOf(k), ((Number) v).intValue()));
                updateTotalChambers();
            }
            if (viabilityPane != null) {
                if (cfg.containsKey("envHeight") && viabilityPane.getEvalHeightSlider() != null) viabilityPane.getEvalHeightSlider().setValue(num(cfg, "envHeight"));
                if (cfg.containsKey("envOrientation") && viabilityPane.getEvalOrientationCombo() != null) viabilityPane.getEvalOrientationCombo().setValue(String.valueOf(cfg.get("envOrientation")));
                if (cfg.containsKey("envTemp") && viabilityPane.getEvalTempSlider() != null) viabilityPane.getEvalTempSlider().setValue(num(cfg, "envTemp"));
                if (cfg.containsKey("envMoisture") && viabilityPane.getEvalMoistureSlider() != null) viabilityPane.getEvalMoistureSlider().setValue(num(cfg, "envMoisture"));
                if (cfg.containsKey("envForaging") && viabilityPane.getEvalForagingSlider() != null) viabilityPane.getEvalForagingSlider().setValue(num(cfg, "envForaging"));
                if (cfg.containsKey("envCompaction") && viabilityPane.getEvalCompactionSlider() != null) viabilityPane.getEvalCompactionSlider().setValue(num(cfg, "envCompaction"));
                viabilityPane.updatePlacementViabilityScore();
            }

            // Resolve and select matching Reference Species for this preset
            populateSpeciesModelCombo();
            String matchedSpecies = null;
            ComboBox<String> spCombo = getSpeciesModelCombo();
            if (cfg.containsKey("referenceSpecies")) {
                matchedSpecies = String.valueOf(cfg.get("referenceSpecies"));
            }
            if (spCombo != null) {
                if (matchedSpecies != null && !matchedSpecies.isBlank()) {
                    for (String item : spCombo.getItems()) {
                        if (item.toLowerCase().contains(matchedSpecies.toLowerCase())) {
                            spCombo.setValue(item);
                            matchedSpecies = item;
                            break;
                        }
                    }
                } else {
                    String currentArch = getArchitecture();
                    String defaultMatch = switch (currentArch) {
                        case "WAX_COMB_HEXAGONAL", "WOODEN_BEEHIVE" -> "Apis";
                        case "WAX_POTS_CLUSTER" -> "Bombus";
                        case "PAPER_PEDUNCULATE" -> "Vespa";
                        case "CATHEDRAL_MOUND" -> "Macrotermes";
                        case "BIVOUAC_LIVING_NEST" -> "Eciton";
                        case "SUBTERRANEAN_FUNGI_VAULT" -> "Atta";
                        case "CARTON_NEST" -> "Crematogaster";
                        case "BAMBOO_STEM_NEST" -> "Temnothorax";
                        case "ARBOREAL_SILK_LEAF" -> "Oecophylla";
                        case "HOLLOW_TRUNK_NEST" -> "Camponotus";
                        case "SURFACE_MOUND" -> "Formica";
                        default -> "Lasius";
                    };
                    for (String item : spCombo.getItems()) {
                        if (item.toLowerCase().contains(defaultMatch.toLowerCase())) {
                            spCombo.setValue(item);
                            matchedSpecies = item;
                            break;
                        }
                    }
                    if (matchedSpecies == null && !spCombo.getItems().isEmpty()) {
                        spCombo.getSelectionModel().selectFirst();
                        matchedSpecies = spCombo.getValue();
                    }
                }
            }
            Label spStatusLbl = getSpeciesStatusLabel();
            if (spStatusLbl != null && matchedSpecies != null) {
                spStatusLbl.setText("Nest preset loaded (Reference: " + matchedSpecies.replaceAll("^[^a-zA-Z0-9(]+\\s*", "") + ")");
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
        ComboBox<String> spCombo = getSpeciesModelCombo();
        c.put("referenceSpecies", spCombo != null && spCombo.getValue() != null ? spCombo.getValue() : "Custom");
        c.put("seed",         nestSeed);
        ComboBox<String> catSel = getCategorySelect();
        c.put("taxonCategory", catSel != null && catSel.getValue() != null ? catSel.getValue() : "Ants (Formicidae)");
        ComboBox<String> gSel = getGenusSelect();
        c.put("genus",        gSel != null && gSel.getValue() != null ? gSel.getValue() : "");
        c.put("architecture", getArchitecture());
        c.put("material",     getMaterial());
        c.put("workerSizeMm", workerSizeSlider.getValue());
        c.put("depth",        (int) depthSlider.getValue());
        c.put("chamberCount", (int) getChamberCount());
        c.put("tunnelWidth",  (int) tunnelWidthSlider.getValue());
        c.put("branching",    (int) branchingSlider.getValue());
        ComboBox<String> stgCombo = getNestStageCombo();
        c.put("nestStageIndex", stgCombo != null ? stgCombo.getSelectionModel().getSelectedIndex() : 2);
        c.put("envHeight", (viabilityPane != null && viabilityPane.getEvalHeightSlider() != null) ? viabilityPane.getEvalHeightSlider().getValue() : 0.0);
        c.put("envOrientation", (viabilityPane != null && viabilityPane.getEvalOrientationCombo() != null && viabilityPane.getEvalOrientationCombo().getValue() != null) ? viabilityPane.getEvalOrientationCombo().getValue() : "East (Morning Light)");
        c.put("envTemp", (viabilityPane != null && viabilityPane.getEvalTempSlider() != null) ? viabilityPane.getEvalTempSlider().getValue() : 22.0);
        c.put("envMoisture", (viabilityPane != null && viabilityPane.getEvalMoistureSlider() != null) ? viabilityPane.getEvalMoistureSlider().getValue() : 45.0);
        c.put("envForaging", (viabilityPane != null && viabilityPane.getEvalForagingSlider() != null) ? viabilityPane.getEvalForagingSlider().getValue() : 35.0);
        c.put("envCompaction", (viabilityPane != null && viabilityPane.getEvalCompactionSlider() != null) ? viabilityPane.getEvalCompactionSlider().getValue() : 65.0);
        Map<String,Integer> dist = new LinkedHashMap<>();
        chamberSpinners.forEach((k,v) -> dist.put(k, v.getValue()));
        c.put("chamberDistribution", dist);
        return c;
    }

    // â”€â”€ Expose params for NestAlgorithm â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    String getArchitecture() { return taxonomyPane != null ? taxonomyPane.getArchitecture() : NestRenderer.normalizeArchKey(""); }
    String getMaterial()     { return taxonomyPane != null ? taxonomyPane.getMaterial() : NestRenderer.normalizeMatKey(""); }
    double getWorkerSizeMm() { return workerSizeSlider.getValue(); }
    double getDepth()        { return depthSlider.getValue(); }
    double getTunnelWidth()  { return tunnelWidthSlider.getValue(); }
    double getBranching()    { return branchingSlider.getValue(); }
    double getChamberCount() { return chamberSpinners.values().stream().mapToInt(Spinner::getValue).sum(); }
    int    sp(String k)      { return getSp(k); }

    // â”€â”€ Inner model classes â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

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
