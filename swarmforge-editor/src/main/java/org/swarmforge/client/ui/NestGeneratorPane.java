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
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.client.ui.nest.*;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.ThemeManager;

import java.util.*;
import java.util.function.Consumer;

/**
 * Visual Studio and Architectural Generator for Social & Eusocial Insect Nests.
 * Manages taxonomy presets, morphological scaling, chamber distribution, spatial viability,
 * and 2D/3D multi-view preview rendering.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class NestGeneratorPane extends BorderPane {

    private final I18nManager i18n = I18nManager.getInstance();
    private final SpeciesPresetManager speciesPresetMgr = new SpeciesPresetManager();

    // Interactive multi-view preview canvas pane
    private NestPreviewAreaPane previewPane;

    // Modular sub-panes
    private NestTaxonomyConfigPane taxonomyPane;
    private NestMorphologyConfigPane morphologyPane;
    private NestChamberDistributionPane chamberPane;
    private NestPlacementViabilityPane viabilityPane;

    private org.swarmforge.core.species.DefaultSpecies activeDefaultSpecies;
    private boolean isUpdatingSpeciesCombo = false;

    // Presets
    private ComboBox<String> presetsCombo;
    private final NestPresetManager presetMgr = new NestPresetManager();
    private boolean isDirty = false;
    private String lastSelectedPreset = null;

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

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        this.isDirty = dirty;
    }

    public boolean promptUnsavedChanges() {
        if (!isDirty) return true;
        boolean allowed = NestPresetActionHelper.promptUnsavedChanges(
            this,
            presetMgr,
            lastSelectedPreset,
            savedName -> {
                isUpdatingSpeciesCombo = true;
                try {
                    refreshPresetsCombo();
                    presetsCombo.setValue(savedName);
                } finally {
                    isUpdatingSpeciesCombo = false;
                }
                lastSelectedPreset = savedName;
                isDirty = false;
            },
            this::doAddPreset
        );
        if (allowed && !isDirty) {
            isDirty = false;
        }
        return allowed;
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private VBox buildHeader() {
        VBox v = new VBox(6);
        v.setPadding(new Insets(8, 10, 5, 10));

        HBox r = new HBox(8);
        r.setAlignment(Pos.CENTER_LEFT);

        Label t = new Label();
        t.textProperty().bind(i18n.createStringBinding("nest.title"));
        t.getStyleClass().addAll("header-title-large", "accent-title");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

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
        NestPresetActionHelper.doDeletePreset(this, presetMgr, sel, () -> {
            refreshPresetsCombo();
            if (!presetsCombo.getItems().isEmpty()) {
                presetsCombo.getSelectionModel().selectFirst();
            } else {
                presetsCombo.getSelectionModel().clearSelection();
            }
        });
    }

    // ── Config Panel ──────────────────────────────────────────────────────────

    private ScrollPane buildConfig() {
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

        morphologyPane = new NestMorphologyConfigPane(
            () -> { regen(); repaint(); },
            () -> {
                if (activeDefaultSpecies != null) {
                    configureFromSpecies(activeDefaultSpecies);
                } else {
                    onSpeciesModelSelected();
                }
            },
            cfg
        );

        VBox masterBlock = new VBox(7, taxonomyPane, new Separator(), morphologyPane);
        masterBlock.getStyleClass().add("card-pane");

        // Chamber Distribution Card
        chamberPane = new NestChamberDistributionPane(() -> {
            onManualParameterChanged();
            regen();
            repaint();
        });

        // Spatial Viability Diagnostics Card
        viabilityPane = new NestPlacementViabilityPane(
            this::getArchitecture,
            () -> (int) getChamberCount(),
            () -> activeDefaultSpecies,
            this::getSp
        );

        cfg.getChildren().addAll(masterBlock, chamberPane, viabilityPane);
        ScrollPane sc = new ScrollPane(cfg);
        sc.setFitToWidth(true);
        sc.setPrefWidth(360);
        sc.setMaxWidth(360);
        return sc;
    }

    private VBox buildViews() {
        previewPane = new NestPreviewAreaPane(() -> nest, this::getTunnelWidth);
        return previewPane;
    }

    // ── Preset Helpers ────────────────────────────────────────────────────────

    private void refreshPresetsCombo() {
        String cur = presetsCombo.getValue();
        presetsCombo.getItems().setAll(presetMgr.names());
        if (cur != null) presetsCombo.setValue(cur);
    }

    private void doAddPreset() {
        String defaultName = (presetsCombo.getEditor() != null && !presetsCombo.getEditor().getText().isBlank())
                ? presetsCombo.getEditor().getText().trim()
                : (presetsCombo.getValue() != null ? presetsCombo.getValue() : "Custom Nest Preset");
        NestPresetActionHelper.doAddPreset(this, presetMgr, defaultName, savedName -> {
            isUpdatingSpeciesCombo = true;
            try {
                refreshPresetsCombo();
                presetsCombo.setValue(savedName);
            } finally {
                isUpdatingSpeciesCombo = false;
            }
            lastSelectedPreset = savedName;
            isDirty = false;
        });
    }

    public void updateTotalChambers() {
        if (chamberPane != null) chamberPane.updateTotalChambers();
    }

    private void onManualParameterChanged() {
        if (isUpdatingSpeciesCombo) return;
        isDirty = true;
        isUpdatingSpeciesCombo = true;
        try {
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
        if (morphologyPane != null) morphologyPane.updatePassageCheckLabel();
    }

    public void onSpeciesModelSelected() {
        String sel = getSpeciesModelCombo() != null ? getSpeciesModelCombo().getValue() : null;
        if (sel == null) return;

        if (sel.contains("Custom Nest Configuration") || sel.contains("Custom Species") || sel.equals(i18n.get("nest.species.custom_config"))) {
            if (getSpeciesStatusLabel() != null) {
                getSpeciesStatusLabel().setText(i18n.get("nest.status.custom_unlinked"));
            }
            return;
        }

        String cleanSel = sel.replaceAll("^[^a-zA-Z0-9(]+\\s*", "").trim();
        for (String presetName : speciesPresetMgr.getPresetNames()) {
            if (cleanSel.equalsIgnoreCase(presetName) || cleanSel.toLowerCase().contains(presetName.toLowerCase()) || presetName.toLowerCase().contains(cleanSel.toLowerCase())) {
                configureFromSpecies(speciesPresetMgr.getPreset(presetName));
                return;
            }
        }
    }

    public void configureFromSpecies(org.swarmforge.core.species.DefaultSpecies species) {
        if (species == null) return;
        this.activeDefaultSpecies = species;
        NestSpeciesArchetypeAdapter.configureFromSpecies(this, species);
    }

    // ── Getters & Setters for Delegated Components ─────────────────────────────

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

    public void setUpdatingSpeciesCombo(boolean updating) {
        this.isUpdatingSpeciesCombo = updating;
        if (taxonomyPane != null) taxonomyPane.setUpdatingSpeciesCombo(updating);
    }

    public ComboBox<String> getCategorySelect() { return taxonomyPane != null ? taxonomyPane.getCategorySelect() : null; }
    public ComboBox<String> getGenusSelect() { return taxonomyPane != null ? taxonomyPane.getGenusSelect() : null; }
    public ComboBox<String> getSpeciesModelCombo() { return taxonomyPane != null ? taxonomyPane.getSpeciesModelCombo() : null; }
    public ComboBox<String> getNestStageCombo() { return taxonomyPane != null ? taxonomyPane.getNestStageCombo() : null; }
    public Label getSpeciesStatusLabel() { return taxonomyPane != null ? taxonomyPane.getSpeciesStatusLabel() : null; }

    public Slider getBranchingSlider() { return morphologyPane != null ? morphologyPane.getBranchingSlider() : null; }
    public Slider getDepthSlider() { return morphologyPane != null ? morphologyPane.getDepthSlider() : null; }
    public Slider getWorkerSizeSlider() { return morphologyPane != null ? morphologyPane.getWorkerSizeSlider() : null; }
    public Slider getTunnelWidthSlider() { return morphologyPane != null ? morphologyPane.getTunnelWidthSlider() : null; }

    public void setSp(String k, int v) { if (chamberPane != null) chamberPane.setSp(k, v); }
    private int getSp(String k) { return chamberPane != null ? chamberPane.getSp(k) : 0; }

    public void regen()   { nest = NestAlgorithm.generate(this); }
    public void repaint() { if (previewPane != null) previewPane.repaint(); }

    public long getSeed() { return morphologyPane != null ? morphologyPane.getSeed() : 123456L; }
    public void setSeed(long seed) { if (morphologyPane != null) morphologyPane.setSeed(seed); }

    public void applyCfg(Map<String, Object> cfg) {
        NestConfigSerializer.applyConfiguration(this, presetsCombo, viabilityPane, chamberPane, cfg);
    }

    public Map<String, Object> getConfiguration() {
        return NestConfigSerializer.exportConfiguration(this, presetsCombo, viabilityPane, chamberPane);
    }

    private void doExport() {
        NestPresetActionHelper.doExport(this, getConfiguration());
    }

    private void doImport() {
        NestPresetActionHelper.doImport(this, this::applyCfg);
    }

    public void setOnApply(Consumer<Map<String, Object>> cb) {
        this.onApplyCallback = cb;
    }

    // ── Algorithm Parameters ──────────────────────────────────────────────────

    public String getArchitecture() { return taxonomyPane != null ? taxonomyPane.getArchitecture() : NestRenderer.normalizeArchKey(""); }
    public String getMaterial()     { return taxonomyPane != null ? taxonomyPane.getMaterial() : NestRenderer.normalizeMatKey(""); }
    public double getWorkerSizeMm() { return morphologyPane != null && morphologyPane.getWorkerSizeSlider() != null ? morphologyPane.getWorkerSizeSlider().getValue() : 5.0; }
    public double getDepth()        { return morphologyPane != null && morphologyPane.getDepthSlider() != null ? morphologyPane.getDepthSlider().getValue() : 2.5; }
    public double getTunnelWidth()  { return morphologyPane != null && morphologyPane.getTunnelWidthSlider() != null ? morphologyPane.getTunnelWidthSlider().getValue() : 4.0; }
    public double getBranching()    { return morphologyPane != null && morphologyPane.getBranchingSlider() != null ? morphologyPane.getBranchingSlider().getValue() : 3.0; }
    public double getChamberCount() { return chamberPane != null ? chamberPane.getTotalChambers() : 0; }
    public int    sp(String k)      { return getSp(k); }

    // ── Inner Model Classes ───────────────────────────────────────────────────

    public static class NestNode {
        public double x, y, z, radius, rx, ry, rz; // Anatomical lenticular radii
        public String type;
        public Color color;

        public NestNode(double x, double y, double z, String type, double r, Color c) {
            this.x = x; this.y = y; this.z = z; this.type = type; this.radius = r; this.color = c;
            this.rx = r; this.ry = r; this.rz = r * 0.55;
        }

        public NestNode(double x, double y, double z, String type, double rx, double ry, double rz, Color c) {
            this.x = x; this.y = y; this.z = z; this.type = type; this.radius = Math.max(rx, ry);
            this.rx = rx; this.ry = ry; this.rz = rz; this.color = c;
        }
    }

    public static class NestEdge {
        public NestNode from, to;
        public List<double[]> pts;

        public NestEdge(NestNode f, NestNode t, List<double[]> p) {
            from = f; to = t; pts = p;
        }
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
