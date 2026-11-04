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
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.NotificationOverlay;
import org.swarmforge.core.domain.AccessoryPreset;

import java.io.File;

/**
 * Dedicated Editor Pane for Accessory & Predator Species (Flora, Aphids, Prey Insects, Predators, Pathogens, Fungi, Detritivores)
 * with realistic latitude, photoperiod, seasonal dynamics, surface-relative densities, pedagogical tooltips,
 * integrated searchable help documentation, and safety confirmation dialogs.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class AccessorySpeciesEditorPane extends VBox {

    private final I18nManager i18n = I18nManager.getInstance();
    private final AccessoryPresetManager presetManager = new AccessoryPresetManager();
    private final AccessorySpeciesCardsBuilder cardsBuilder = new AccessorySpeciesCardsBuilder(this);

    // UI Controls - Header & Toolbar
    private Label headerLabel;
    private Label lblPreset;
    private ComboBox<String> accessoryPresetCombo;
    private Button btnSave;
    private Button btnDelete;
    private Button btnExport;
    private Button btnImport;

    // Tabs
    private TabPane tabPane;
    private Tab tabTaxonomy;
    private Tab tabSeasonal;
    private Tab tabPredators;

    // UI Controls - Taxonomy & Thermal
    TextField accessoryNameField;
    ComboBox<String> categoryCombo;
    ComboBox<String> biomeCombo;
    TextField latitudeField;

    // Thermal & Density Parameters
    TextField minTempField;
    TextField optTempField;
    TextField maxTempField;
    TextField growthRateField;
    TextField initialBiomassDensityField;
    TextField initialPopulationDensityField;
    Spinner<Integer> individualCountSpinner;
    ComboBox<String> nestDispatchCombo;
    CheckBox diapauseCheck;

    // Seasonal Controls & Hemisphere
    ComboBox<String> hemisphereCombo;
    Label seasonLabel1;
    Label seasonLabel2;
    Label seasonLabel3;
    Label seasonLabel4;
    Slider seasonSlider1;
    Slider seasonSlider2;
    Slider seasonSlider3;
    Slider seasonSlider4;
    Label seasonHintLabel;

    // Predators & Pathogens Controls
    ComboBox<String> targetCasteCombo;
    ComboBox<String> huntModeCombo;
    TextField killRateField;
    ComboBox<String> pathogenVectorCombo;
    TextField transmissionR0Field;
    TextField incubationDaysField;
    TextField mortalityRateField;

    private boolean isDirty = false;
    private boolean isUpdatingFields = false;
    private String lastSelectedPreset = null;

    public boolean isDirty() {
        return isDirty;
    }

    void setDirty(boolean isDirty) {
        this.isDirty = isDirty;
    }

    void setUpdatingFields(boolean isUpdatingFields) {
        this.isUpdatingFields = isUpdatingFields;
    }

    void setLastSelectedPreset(String lastSelectedPreset) {
        this.lastSelectedPreset = lastSelectedPreset;
    }

    public boolean promptUnsavedChanges() {
        return AccessorySpeciesPresetIoHelper.promptUnsavedChanges(this, accessoryPresetCombo, lastSelectedPreset, this::handleAddPreset);
    }

    public AccessorySpeciesEditorPane() {
        setSpacing(10);

        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        tabTaxonomy = new Tab(i18n.get("accessory.tab.taxonomy"));
        tabTaxonomy.setGraphic(new FontIcon(Feather.LIST));
        tabTaxonomy.setContent(new ScrollPane(cardsBuilder.createTaxonomyCard()));

        tabSeasonal = new Tab(i18n.get("accessory.tab.seasonal"));
        tabSeasonal.setGraphic(new FontIcon(Feather.SUN));
        tabSeasonal.setContent(new ScrollPane(cardsBuilder.createSeasonalCard()));

        tabPredators = new Tab(i18n.get("accessory.tab.predators"));
        tabPredators.setGraphic(new FontIcon(Feather.SHIELD));
        tabPredators.setContent(new ScrollPane(cardsBuilder.createPredatorPathogenCard()));

        tabPane.getTabs().addAll(tabTaxonomy, tabSeasonal, tabPredators);

        i18n.localeProperty().addListener((obs, oldL, newL) -> refreshI18nLabels());

        getChildren().addAll(buildHeader(), tabPane);

        attachUserChangeListeners();
        if (!accessoryPresetCombo.getItems().isEmpty()) {
            applyAccessoryPreset(accessoryPresetCombo.getValue());
        }
    }

    private VBox buildHeader() {
        VBox v = new VBox(6);
        v.setPadding(new Insets(8, 10, 5, 10));

        HBox r = new HBox(8);
        r.setAlignment(Pos.CENTER_LEFT);

        headerLabel = new Label(i18n.get("accessory.title"));
        headerLabel.getStyleClass().add("title-header-label");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        HBox toolbar = createToolbar();

        r.getChildren().addAll(headerLabel, sp, toolbar);
        v.getChildren().addAll(r, new Separator());
        return v;
    }

    private HBox createToolbar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);

        lblPreset = new Label();
        lblPreset.textProperty().bind(i18n.createStringBinding("preset.label"));
        lblPreset.getStyleClass().add("bold-label");
        lblPreset.setGraphic(new FontIcon(Feather.SLIDERS));

        accessoryPresetCombo = new ComboBox<>(FXCollections.observableArrayList(presetManager.getPresetNames()));
        FXCollections.sort(accessoryPresetCombo.getItems());
        accessoryPresetCombo.setEditable(true);
        accessoryPresetCombo.promptTextProperty().bind(i18n.createStringBinding("preset.prompt"));
        Tooltip presetTt = new Tooltip(i18n.get("accessory.preset.label.tt"));
        presetTt.setShowDelay(Duration.millis(100));
        accessoryPresetCombo.setTooltip(presetTt);
        accessoryPresetCombo.getSelectionModel().selectFirst();
        accessoryPresetCombo.setPrefWidth(300);

        accessoryPresetCombo.setOnAction(e -> {
            if (isUpdatingFields) return;
            String sel = accessoryPresetCombo.getValue();
            if (sel == null || sel.equals(lastSelectedPreset)) return;

            if (isDirty) {
                Alert alert = org.swarmforge.client.util.ThemeManager.createAlert(
                    Alert.AlertType.CONFIRMATION,
                    "Warning: You have unsaved changes in the current accessory species.\n\nDo you really want to load preset '" + sel + "' and discard changes?"
                );
                alert.setTitle("Unsaved Changes");
                alert.setHeaderText("Accessory Preset Change");
                java.util.Optional<ButtonType> res = alert.showAndWait();
                if (res.isEmpty() || res.get() != ButtonType.OK) {
                    isUpdatingFields = true;
                    try {
                        accessoryPresetCombo.setValue(lastSelectedPreset);
                    } finally {
                        isUpdatingFields = false;
                    }
                    return;
                }
            }

            if (sel != null) {
                lastSelectedPreset = sel;
                applyAccessoryPreset(sel);
            }
        });

        btnSave = new Button();
        btnSave.setGraphic(new FontIcon(Feather.SAVE));
        btnSave.textProperty().bind(i18n.createStringBinding("preset.save"));
        btnSave.getStyleClass().add("btn-secondary");
        btnSave.tooltipProperty().bind(i18n.createTooltipBinding("preset.save.tt"));
        btnSave.setOnAction(e -> handleAddPreset());

        btnDelete = new Button();
        btnDelete.setGraphic(new FontIcon(Feather.TRASH_2));
        btnDelete.textProperty().bind(i18n.createStringBinding("preset.delete"));
        btnDelete.getStyleClass().add("btn-danger");
        btnDelete.tooltipProperty().bind(i18n.createTooltipBinding("preset.delete.tt"));
        btnDelete.setOnAction(e -> handleDeletePreset());

        btnExport = new Button();
        btnExport.setGraphic(new FontIcon(Feather.DOWNLOAD));
        btnExport.textProperty().bind(i18n.createStringBinding("preset.export"));
        btnExport.getStyleClass().add("btn-secondary");
        btnExport.tooltipProperty().bind(i18n.createTooltipBinding("preset.export.tt"));
        btnExport.setOnAction(e -> handleSave());

        btnImport = new Button();
        btnImport.setGraphic(new FontIcon(Feather.UPLOAD));
        btnImport.textProperty().bind(i18n.createStringBinding("preset.import"));
        btnImport.getStyleClass().add("btn-secondary");
        btnImport.tooltipProperty().bind(i18n.createTooltipBinding("preset.import.tt"));
        btnImport.setOnAction(e -> handleLoad());

        bar.getChildren().addAll(lblPreset, accessoryPresetCombo, btnSave, btnDelete, new Separator(Orientation.VERTICAL), btnExport, btnImport);
        return bar;
    }

    private void handleAddPreset() {
        AccessorySpeciesPresetIoHelper.handleAddPreset(this, presetManager, accessoryPresetCombo, i18n);
    }

    private void handleLoad() {
        AccessorySpeciesPresetIoHelper.handleLoad(this, presetManager, accessoryPresetCombo, i18n);
    }

    private void handleSave() {
        AccessorySpeciesPresetIoHelper.handleSave(this, presetManager, i18n);
    }

    private void handleDeletePreset() {
        AccessorySpeciesPresetIoHelper.handleDeletePreset(this, presetManager, accessoryPresetCombo, i18n);
    }

    private void refreshI18nLabels() {
        if (headerLabel != null) headerLabel.setText(i18n.get("accessory.title"));

        if (tabTaxonomy != null) tabTaxonomy.setText(i18n.get("accessory.tab.taxonomy"));
        if (tabSeasonal != null) tabSeasonal.setText(i18n.get("accessory.tab.seasonal"));
        if (tabPredators != null) tabPredators.setText(i18n.get("accessory.tab.predators"));

        cardsBuilder.updateSeasonLabels();
        if (seasonHintLabel != null) {
            seasonHintLabel.setText(i18n.get("accessory.season.hint"));
        }
    }

    private void clearPresetSelection() {
        if (!isUpdatingFields && accessoryPresetCombo != null) {
            isDirty = true;
            isUpdatingFields = true;
            try {
                accessoryPresetCombo.getSelectionModel().clearSelection();
            } finally {
                isUpdatingFields = false;
            }
        }
    }

    private void attachUserChangeListeners() {
        Runnable clearLsn = this::clearPresetSelection;
        if (accessoryNameField != null) accessoryNameField.textProperty().addListener((o, a, b) -> clearLsn.run());
        if (categoryCombo != null) categoryCombo.valueProperty().addListener((o, a, b) -> clearLsn.run());
        if (biomeCombo != null) biomeCombo.valueProperty().addListener((o, a, b) -> clearLsn.run());
        if (latitudeField != null) latitudeField.textProperty().addListener((o, a, b) -> clearLsn.run());
        if (minTempField != null) minTempField.textProperty().addListener((o, a, b) -> clearLsn.run());
        if (optTempField != null) optTempField.textProperty().addListener((o, a, b) -> clearLsn.run());
        if (maxTempField != null) maxTempField.textProperty().addListener((o, a, b) -> clearLsn.run());
        if (growthRateField != null) growthRateField.textProperty().addListener((o, a, b) -> clearLsn.run());
        if (initialBiomassDensityField != null) initialBiomassDensityField.textProperty().addListener((o, a, b) -> clearLsn.run());
        if (initialPopulationDensityField != null) initialPopulationDensityField.textProperty().addListener((o, a, b) -> clearLsn.run());
        if (diapauseCheck != null) diapauseCheck.selectedProperty().addListener((o, a, b) -> clearLsn.run());
        if (hemisphereCombo != null) hemisphereCombo.valueProperty().addListener((o, a, b) -> clearLsn.run());
        if (seasonSlider1 != null) seasonSlider1.valueProperty().addListener((o, a, b) -> clearLsn.run());
        if (seasonSlider2 != null) seasonSlider2.valueProperty().addListener((o, a, b) -> clearLsn.run());
        if (seasonSlider3 != null) seasonSlider3.valueProperty().addListener((o, a, b) -> clearLsn.run());
        if (seasonSlider4 != null) seasonSlider4.valueProperty().addListener((o, a, b) -> clearLsn.run());
        if (targetCasteCombo != null) targetCasteCombo.valueProperty().addListener((o, a, b) -> clearLsn.run());
        if (huntModeCombo != null) huntModeCombo.valueProperty().addListener((o, a, b) -> clearLsn.run());
        if (killRateField != null) killRateField.textProperty().addListener((o, a, b) -> clearLsn.run());
        if (pathogenVectorCombo != null) pathogenVectorCombo.valueProperty().addListener((o, a, b) -> clearLsn.run());
        if (transmissionR0Field != null) transmissionR0Field.textProperty().addListener((o, a, b) -> clearLsn.run());
        if (incubationDaysField != null) incubationDaysField.textProperty().addListener((o, a, b) -> clearLsn.run());
        if (mortalityRateField != null) mortalityRateField.textProperty().addListener((o, a, b) -> clearLsn.run());
    }

    void applyAccessoryPreset(String name) {
        AccessorySpeciesPresetIoHelper.applyAccessoryPreset(this, presetManager, name);
    }
}
