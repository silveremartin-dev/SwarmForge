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
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.client.ui.species.*;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.NotificationOverlay;
import org.swarmforge.client.util.ThemeManager;
import org.swarmforge.core.domain.CasteTemplate;
import org.swarmforge.core.species.DefaultSpecies;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Visual Studio Suite for editing Eusocial Insect Species and Castes.
 * Modular architecture orchestrating specialized sub-panes for taxonomy, queens, castes,
 * developmental stages, diets, sensory perception, and nest architectural preferences.
 */
public class SpeciesEditorPane extends VBox {

    final SpeciesPresetManager presetManager = new SpeciesPresetManager();
    final I18nManager i18n = I18nManager.getInstance();

    ComboBox<String> presetCombo;
    private TabPane mainTabPane;
    private VBox warningBannerBox;

    // Modular Sub-Panes
    private final ObservableList<org.swarmforge.client.ui.species.CasteRow> casteRows = FXCollections.observableArrayList();
    final SpeciesTaxonomyPane taxonomyPane;
    private final SpeciesQueensPane queensPane;
    private final SpeciesCastesPane castesPane;
    private final SpeciesDevelopmentStagesPane stagesPane;
    private final SpeciesDietPane dietPane;
    private final SpeciesSensoryPane sensoryPane;
    private final SpeciesNestPreferencesPane nestPane;

    private Consumer<DefaultSpecies> onApplyListener;
    private Consumer<DefaultSpecies> onGenerateNestForSpeciesListener;

    boolean isUpdatingFields = false;
    boolean isDirty = false;
    String lastSelectedPreset = null;

    public SpeciesEditorPane() {
        setSpacing(10);

        // Warning banner for parameter consistency
        warningBannerBox = new VBox(8);
        warningBannerBox.setStyle("-fx-background-color: rgba(234, 179, 8, 0.15); -fx-border-color: #eab308; -fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 10px;");
        warningBannerBox.setVisible(false);
        warningBannerBox.setManaged(false);

        // Instantiate modular sub-panes
        taxonomyPane = new SpeciesTaxonomyPane(this::onFieldEdited, this::validateParameters);
        queensPane = new SpeciesQueensPane(this::onFieldEdited, this::validateParameters);
        castesPane = new SpeciesCastesPane(casteRows, this::onFieldEdited);
        stagesPane = new SpeciesDevelopmentStagesPane(this::onFieldEdited);
        dietPane = new SpeciesDietPane(this::onFieldEdited);
        sensoryPane = new SpeciesSensoryPane();
        nestPane = new SpeciesNestPreferencesPane(this::onFieldEdited, this::validateParameters, e -> handleGenerateNestForSpecies());

        casteRows.addListener((javafx.collections.ListChangeListener<org.swarmforge.client.ui.species.CasteRow>) c -> {
            onFieldEdited();
            validateParameters();
        });

        // TabPane assembly
        mainTabPane = createTabPane();

        getChildren().addAll(buildHeader(), warningBannerBox, mainTabPane);

        // Load initial preset if present
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

        Label title = new Label();
        title.textProperty().bind(i18n.createStringBinding("species.editor_title"));
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        HBox toolbar = createTopToolbar();

        r.getChildren().addAll(title, sp, toolbar);
        v.getChildren().addAll(r, new Separator());
        return v;
    }

    private HBox createTopToolbar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);

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
                Alert alert = ThemeManager.createAlert(
                    Alert.AlertType.CONFIRMATION,
                    "Warning: You have unsaved changes on the current species.\n\nDo you really want to load preset '" + sel + "' and discard your changes?"
                );
                alert.setTitle(i18n.get("common.dialog.unsaved_title"));
                alert.setHeaderText(i18n.get("dialog.preset.change.header"));
                Optional<ButtonType> res = alert.showAndWait();
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
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab tabTaxonomy = new Tab();
        tabTaxonomy.textProperty().bind(i18n.createStringBinding("species.tab.taxonomy"));
        tabTaxonomy.setContent(taxonomyPane.getContainer());
        tabTaxonomy.setGraphic(new FontIcon(Feather.BOOK));

        Tab tabQueens = new Tab();
        tabQueens.textProperty().bind(i18n.createStringBinding("species.tab.queens"));
        tabQueens.setContent(queensPane.getContainer());
        tabQueens.setGraphic(new FontIcon(Feather.AWARD));

        Tab tabCastes = new Tab();
        tabCastes.textProperty().bind(i18n.createStringBinding("species.tab.castes"));
        tabCastes.setContent(castesPane.getContainer());
        tabCastes.setGraphic(new FontIcon(Feather.USERS));

        Tab tabStages = new Tab();
        tabStages.textProperty().bind(i18n.createStringBinding("species.tab.stages"));
        tabStages.setContent(stagesPane.getContainer());
        tabStages.setGraphic(new FontIcon(Feather.CLOCK));

        Tab tabDiet = new Tab();
        tabDiet.textProperty().bind(i18n.createStringBinding("species.tab.diet"));
        tabDiet.setContent(dietPane.getContainer());
        tabDiet.setGraphic(new FontIcon(Feather.FEATHER));

        Tab tabSensors = new Tab();
        tabSensors.textProperty().bind(i18n.createStringBinding("species.tab.sensors"));
        tabSensors.setContent(sensoryPane.getContainer());
        tabSensors.setGraphic(new FontIcon(Feather.EYE));

        Tab tabNest = new Tab();
        tabNest.textProperty().bind(i18n.createStringBinding("species.tab.nest"));
        tabNest.setContent(nestPane.getContainer());
        tabNest.setGraphic(new FontIcon(Feather.HOME));

        tabPane.getTabs().addAll(tabTaxonomy, tabQueens, tabCastes, tabStages, tabDiet, tabSensors, tabNest);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        return tabPane;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public boolean promptUnsavedChanges() {
        return SpeciesPresetActionHelper.promptUnsavedChanges(this);
    }

    public void setOnApply(Consumer<DefaultSpecies> listener) {
        this.onApplyListener = listener;
    }

    public void setOnGenerateNestForSpecies(Consumer<DefaultSpecies> listener) {
        this.onGenerateNestForSpeciesListener = listener;
    }

    private void handleGenerateNestForSpecies() {
        DefaultSpecies s = buildSpeciesFromUI();
        if (onGenerateNestForSpeciesListener != null) {
            onGenerateNestForSpeciesListener.accept(s);
        } else {
            ThemeManager.createAlert(Alert.AlertType.INFORMATION, "Active species configured for nest generation: " + s.getCommonName()).show();
        }
    }

    public List<String> validateParameters() {
        return SpeciesValidator.validateAndRender(taxonomyPane, queensPane, nestPane, casteRows, warningBannerBox, this::validateParameters);
    }

    void handleAddPreset() {
        SpeciesPresetActionHelper.handleAddPreset(this);
    }

    void handleDeletePreset() {
        SpeciesPresetActionHelper.handleDeletePreset(this);
    }

    void handleSaveDisk() {
        SpeciesPresetActionHelper.handleSaveDisk(this);
    }

    void handleLoadDisk() {
        SpeciesPresetActionHelper.handleLoadDisk(this);
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

    public void loadPresetToUI(DefaultSpecies s) {
        if (s == null) return;
        isUpdatingFields = true;
        try {
            taxonomyPane.populateFromSpecies(s);
            queensPane.populateFromSpecies(s);
            stagesPane.populateFromSpecies(s);
            dietPane.populateFromSpecies(s);
            sensoryPane.populateFromSpecies(s);
            nestPane.populateFromSpecies(s);

            // Populate Castes
            casteRows.clear();
            if (s.getCasteTemplates() != null) {
                for (CasteTemplate ct : s.getCasteTemplates()) {
                    double body = ct.getBodyLengthMm() > 0 ? ct.getBodyLengthMm() : ct.getAttribute("size_mm", 5.0f);
                    double head = ct.getHeadWidthMm() > 0 ? ct.getHeadWidthMm() : (body * 0.25);
                    org.swarmforge.client.ui.species.CasteRow row = new org.swarmforge.client.ui.species.CasteRow(ct.getName(), body, head, ct.getLifespan(), ct.getBaseHealth(), ct.getBaseDamage(), ct.isCanFly());
                    row.setDefense(ct.getBaseDefense());
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
                    row.setWalkSpeedMps(ct.getWalkSpeedMps() > 0.0f ? ct.getWalkSpeedMps() : s.getWalkingSpeed());
                    row.setFlySpeedMps(ct.getFlySpeedMps() > 0.0f ? ct.getFlySpeedMps() : (ct.isCanFly() ? s.getFlyingSpeed() : 0.0f));
                    casteRows.add(row);
                }
            }
            if (castesPane.getTableView() != null && !casteRows.isEmpty()) {
                castesPane.getTableView().getSelectionModel().selectFirst();
            }
            validateParameters();
        } finally {
            isUpdatingFields = false;
            isDirty = false;
        }
    }

    public DefaultSpecies buildSpeciesFromUI() {
        DefaultSpecies s = new DefaultSpecies();
        taxonomyPane.applyToSpecies(s);
        s.setPresetName(s.getCommonName());
        queensPane.applyToSpecies(s);
        stagesPane.applyToSpecies(s);
        dietPane.applyToSpecies(s);
        sensoryPane.applyToSpecies(s);
        nestPane.applyToSpecies(s);

        if (!casteRows.isEmpty()) {
            s.setVenomType(casteRows.get(0).getVenomType());
        } else {
            s.setVenomType("NONE");
        }

        List<CasteTemplate> templates = new ArrayList<>();
        for (org.swarmforge.client.ui.species.CasteRow r : casteRows) {
            CasteTemplate ct = new CasteTemplate(r.getName(), r.getHealth(), r.getDamage());
            ct.setBaseDefense(r.getDefense());
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
            ct.setWalkSpeedMps(r.getWalkSpeedMps());
            ct.setFlySpeedMps(r.getFlySpeedMps());
            templates.add(ct);
        }
        s.setCasteTemplates(templates);

        return s;
    }

    // --- Backward Compatibility Subclasses & Delegations ---
    public static class FormattedDoubleStringConverter extends javafx.util.StringConverter<Double> {
        @Override
        public String toString(Double object) {
            if (object == null) return "0";
            if (object == (long) object.doubleValue()) return String.format(Locale.US, "%d", (long) object.doubleValue());
            return String.format(Locale.US, "%.2f", object);
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
            if (object == (long) object.floatValue()) return String.format(Locale.US, "%d", (long) object.floatValue());
            return String.format(Locale.US, "%.2f", object);
        }
        @Override
        public Float fromString(String string) {
            try { return Float.parseFloat(string.trim()); } catch (Exception e) { return 0.0f; }
        }
    }

    public static class CasteRow extends org.swarmforge.client.ui.species.CasteRow {
        public CasteRow(String name, double bodyLengthMm, double headWidthMm, int lifespan, float health, float damage, boolean canFly) {
            super(name, bodyLengthMm, headWidthMm, lifespan, health, damage, canFly);
        }
    }

    public static String getDietTitle(String diet) {
        return SpeciesUIFormatting.getDietTitle(diet);
    }

    public static String getDietDescription(String diet) {
        return SpeciesUIFormatting.getDietDescription(diet);
    }

    public static String getNestTypeTitle(String type) {
        return SpeciesUIFormatting.getNestTypeTitle(type);
    }

    public static String getNestTypeDescription(String type) {
        return SpeciesUIFormatting.getNestTypeDescription(type);
    }
}
