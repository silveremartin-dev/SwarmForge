/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.species;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.client.ui.BrainImportDialog;
import org.swarmforge.client.ui.ComboBoxTooltipHelper;
import org.swarmforge.client.ui.GlossaryDialog;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.NotificationOverlay;
import org.swarmforge.client.util.ThemeManager;
import org.swarmforge.core.behavior.BrainDescriptor;
import org.swarmforge.core.behavior.BrainPluginRegistry;
import org.swarmforge.core.behavior.ReasoningArchitecture.ArchitectureType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Self-contained UI Pane and polymorphic caste editor for the Castes & Morphology tab
 * in the Species Editor (Caste summary table, locomotion, flight, mandibular biting,
 * cognitive reasoning models, and weapon/venom loadouts).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SpeciesCastesPane {

    private final ObservableList<CasteRow> casteRows;
    private final Runnable onFieldEditedCallback;
    private final I18nManager i18n = I18nManager.getInstance();

    private TableView<CasteRow> casteTable;
    private ScrollPane container;

    // Inspector fields
    public final TextField casteNameF = new TextField("Soldier");
    public final TextField casteBodyF = new TextField("6.0");
    public final TextField casteHeadF = new TextField("1.8");
    public final TextField casteLifeF = new TextField("5000");
    public final TextField casteHealthF = new TextField("120");
    public final TextField casteDmgF = new TextField("15");
    public final TextField casteDefenseF = new TextField("0.0");

    public final TextField casteWalkSpeedF = new TextField("0.50");
    public final CheckBox casteAroliaCheck = new CheckBox("Arolia Adhesive Pads");
    public final CheckBox casteFlyCheck = new CheckBox("Flying Capability");
    public final TextField casteFlySpeedF = new TextField("0.00");
    public final TextField casteWingbeatHzF = new TextField("0.0");
    public final CheckBox casteHoverCheck = new CheckBox("Hovering Flight");
    public final TextField casteBitingForceMpaF = new TextField("15.0");
    public final TextField castePayloadRatioF = new TextField("5.0");

    public final TextField targetRatioF = new TextField("0.25");
    public final ComboBox<String> decisionArchCombo = new ComboBox<>();
    public final TextField foragingWField = new TextField("0.30");
    public final TextField defenseWField = new TextField("0.20");
    public final TextField excavationWField = new TextField("0.20");
    public final TextField nursingWField = new TextField("0.15");

    public final MenuButton casteVenomMenuButton = new MenuButton("🚫 No Venom (Physical Attack)");
    public final Map<String, CheckMenuItem> casteVenomCheckItems = new LinkedHashMap<>();
    public final TextField casteVenomToxField = new TextField("10.0");
    public final CheckBox casteAutothysisCheck = new CheckBox("Explosive Autothysis Defense");

    public SpeciesCastesPane(ObservableList<CasteRow> casteRows, Runnable onFieldEditedCallback) {
        this.casteRows = casteRows;
        this.onFieldEditedCallback = onFieldEditedCallback;
        buildUI();
    }

    public ScrollPane getContainer() {
        return container;
    }

    public TableView<CasteRow> getTableView() {
        return casteTable;
    }

    private void buildUI() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(15));

        Label infoLabel = new Label("💡 Caste summary table. Click a row to inspect or modify all parameters in the inspector below.");
        infoLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-style: italic;");

        casteTable = SpeciesCasteTableBuilder.buildCasteTable(casteRows);

        // Inspector Card
        VBox casteInspectorCard = new VBox(10);
        casteInspectorCard.getStyleClass().add("card-pane");
        Label titleInspector = new Label("🔍 Selected Caste Inspector & Editor (Column Mode)");
        titleInspector.getStyleClass().add("card-title");

        casteAroliaCheck.setSelected(true);
        casteFlySpeedF.disableProperty().bind(casteFlyCheck.selectedProperty().not());
        casteWingbeatHzF.disableProperty().bind(casteFlyCheck.selectedProperty().not());
        casteHoverCheck.disableProperty().bind(casteFlyCheck.selectedProperty().not());

        for (ArchitectureType type : ArchitectureType.values()) {
            decisionArchCombo.getItems().add(type.getDisplayName());
        }
        for (BrainDescriptor desc : BrainPluginRegistry.getInstance().getRegisteredBrains()) {
            if (desc.getSourceType() != BrainDescriptor.BrainSourceType.BUILTIN && !decisionArchCombo.getItems().contains(desc.getDisplayName())) {
                decisionArchCombo.getItems().add(desc.getDisplayName());
            }
        }
        ComboBoxTooltipHelper.setupDescriptiveComboBox(decisionArchCombo, SpeciesUIFormatting::getDecisionArchTitle, SpeciesUIFormatting::getDecisionArchDescription);
        decisionArchCombo.setValue(ArchitectureType.BDI.getDisplayName());

        Button btnImportBrain = new Button();
        btnImportBrain.setGraphic(new FontIcon(Feather.CPU));
        btnImportBrain.setTooltip(new Tooltip(i18n.get("species.brain.import_tt", "Import cognitive brain architecture (.onnx, .jar, .sfbrain)")));
        btnImportBrain.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 4 8; -fx-background-radius: 4;");
        btnImportBrain.setOnAction(e -> {
            Stage owner = (Stage) casteInspectorCard.getScene().getWindow();
            new BrainImportDialog(owner, descriptor -> {
                if (!decisionArchCombo.getItems().contains(descriptor.getDisplayName())) {
                    decisionArchCombo.getItems().add(descriptor.getDisplayName());
                }
                decisionArchCombo.setValue(descriptor.getDisplayName());
                NotificationOverlay.show(casteInspectorCard, java.text.MessageFormat.format(i18n.get("species.brain.import_success", "Brain imported: {0}"), descriptor.getDisplayName()), NotificationOverlay.NotificationType.SUCCESS);
            }).show();
        });

        HBox decisionArchBox = new HBox(6, decisionArchCombo, btnImportBrain);
        decisionArchBox.setAlignment(Pos.CENTER_LEFT);

        casteVenomMenuButton.setPrefWidth(220);
        casteVenomMenuButton.getStyleClass().add("btn-secondary");

        List<String> venomOptions = List.of(
            "NONE", "FORMIC_ACID", "VENOMOUS_STING", "CHEMICAL_SPRAY", "ACID_SPRAY",
            "SOLENOPSIN", "NEUROTOXIN", "CYTOTOXIN", "TERPENE_RESIN", "AUTOTHYSIS_BOMB", "POWERFUL_MANDIBLES"
        );

        for (String opt : venomOptions) {
            CheckMenuItem item = new CheckMenuItem(SpeciesUIFormatting.getVenomTitle(opt));
            item.setOnAction(e -> {
                if ("NONE".equals(opt) && item.isSelected()) {
                    casteVenomCheckItems.forEach((k, v) -> { if (!"NONE".equals(k)) v.setSelected(false); });
                } else if (!"NONE".equals(opt) && item.isSelected()) {
                    if (casteVenomCheckItems.containsKey("NONE")) casteVenomCheckItems.get("NONE").setSelected(false);
                }
                updateVenomMenuText(casteVenomMenuButton, casteVenomCheckItems);
                if (onFieldEditedCallback != null) onFieldEditedCallback.run();
            });
            casteVenomCheckItems.put(opt, item);
            casteVenomMenuButton.getItems().add(item);
        }
        updateVenomMenuText(casteVenomMenuButton, casteVenomCheckItems);

        casteTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                casteNameF.setText(newVal.getName());
                casteBodyF.setText(formatDec(newVal.getBodyLengthMm()));
                casteHeadF.setText(formatDec(newVal.getHeadWidthMm()));
                casteLifeF.setText(String.valueOf(newVal.getLifespan()));
                casteHealthF.setText(formatDec(newVal.getHealth()));
                casteDmgF.setText(formatDec(newVal.getDamage()));
                casteDefenseF.setText(formatDec(newVal.getDefense()));

                casteWalkSpeedF.setText(formatDec(newVal.getWalkSpeedMps()));
                casteAroliaCheck.setSelected(newVal.isHasAroliaAdhesion());
                casteFlyCheck.setSelected(newVal.isCanFly());
                casteFlySpeedF.setText(formatDec(newVal.getFlySpeedMps()));
                casteWingbeatHzF.setText(formatDec(newVal.getWingbeatFrequencyHz()));
                casteHoverCheck.setSelected(newVal.isHasHoveringCapability());
                casteBitingForceMpaF.setText(formatDec(newVal.getBitingForceMpa()));
                castePayloadRatioF.setText(formatDec(newVal.getMaxPayloadRatio()));

                targetRatioF.setText(formatDec(newVal.getTargetRatio()));
                decisionArchCombo.setValue(newVal.getDecisionArch());
                foragingWField.setText(formatDec(newVal.getForagingWeight()));
                defenseWField.setText(formatDec(newVal.getDefenseWeight()));
                excavationWField.setText(formatDec(newVal.getExcavationWeight()));
                nursingWField.setText(formatDec(newVal.getNursingWeight()));

                setSelectedVenomTypes(casteVenomCheckItems, casteVenomMenuButton, newVal.getVenomType());
                casteVenomToxField.setText(formatDec(newVal.getVenomToxicity()));
                casteAutothysisCheck.setSelected(newVal.isHasAutothysis());
            }
        });

        HBox casteInspectorColumns = new HBox(10);

        // Column 1: 👤 Identity & Morphology
        GridPane col1Grid = createColumnGrid();
        col1Grid.addRow(0, createTooltipLabel("Caste Name:", "Functional caste name within the colony.", casteNameF), casteNameF);
        col1Grid.addRow(1, createTooltipLabel("Body Length (mm):", "Total body length from head to abdominal apex in mm.", casteBodyF), casteBodyF);
        col1Grid.addRow(2, createTooltipLabel("Head Width (mm):", "Maximum head capsule width determining minimum gallery diameter.", casteHeadF), casteHeadF);
        col1Grid.addRow(3, createTooltipLabel("Base Health:", "Initial health points of the caste.", casteHealthF), casteHealthF);
        col1Grid.addRow(4, createTooltipLabel("Attack Damage:", "Physical damage dealt per bite/attack.", casteDmgF), casteDmgF);
        col1Grid.addRow(5, createTooltipLabel("Base Armor / Defense:", "Cuticular armor reduction points against incoming physical damage.", casteDefenseF), casteDefenseF);
        col1Grid.addRow(6, createTooltipLabel("Lifespan (days):", "Average lifespan of caste members in days.", casteLifeF), casteLifeF);
        VBox col1Box = createInspectorColumnBox("👤 Identity & Morphology", col1Grid);

        // Column 2: ⚡ Locomotion & Biomechanics
        GridPane col2Grid = createColumnGrid();
        col2Grid.addRow(0, createTooltipLabel("Walk Speed (m/s):", "Surface walking speed of this caste in meters per second.", casteWalkSpeedF), casteWalkSpeedF);
        col2Grid.addRow(1, createTooltipLabel("Arolia Adhesion:", "Presence of tarsal arolia pads for walking on vertical walls & ceilings.", casteAroliaCheck, "Arolia"), casteAroliaCheck);
        col2Grid.addRow(2, createTooltipLabel("Flight Capability:", "Indicates if caste members possess wings and flight capability.", casteFlyCheck), casteFlyCheck);
        col2Grid.addRow(3, createTooltipLabel("Fly Speed (m/s):", "Flight speed of this caste in meters per second (0 if wingless).", casteFlySpeedF), casteFlySpeedF);
        col2Grid.addRow(4, createTooltipLabel("Wingbeat Frequency (Hz):", "Wingbeat frequency if caste flies (0 Hz if wingless).", casteWingbeatHzF), casteWingbeatHzF);
        col2Grid.addRow(5, createTooltipLabel("Hovering Flight:", "Ability to maintain stationary position in flight.", casteHoverCheck), casteHoverCheck);
        col2Grid.addRow(6, createTooltipLabel("Biting Force (MPa):", "Mandibular biting force exerted by cephalic muscles.", casteBitingForceMpaF, "Mandibule"), casteBitingForceMpaF);
        col2Grid.addRow(7, createTooltipLabel("Payload Ratio (g/g):", "Maximum carrying load ratio relative to body weight.", castePayloadRatioF), castePayloadRatioF);
        VBox col2Box = createInspectorColumnBox("⚡ Locomotion & Biomechanics", col2Grid);

        // Column 3: 🧠 AI & Task Allocation
        GridPane col3Grid = createColumnGrid();
        col3Grid.addRow(0, createTooltipLabel("Decision Model:", "Cognitive architecture (BDI, Neural Network, FSM, Behavior Tree, Fuzzy Logic, or Imported ONNX/JAR/SFBrain)", decisionArchBox, "FSM"), decisionArchBox);
        col3Grid.addRow(1, createTooltipLabel("Target Ratio (%):", "Target percentage of this caste among workers.", targetRatioF), targetRatioF);
        col3Grid.addRow(2, createTooltipLabel("Foraging Weight:", "Task allocation weight for foraging", foragingWField), foragingWField);
        col3Grid.addRow(3, createTooltipLabel("Defense Weight:", "Task allocation weight for defense", defenseWField), defenseWField);
        col3Grid.addRow(4, createTooltipLabel("Excavation Weight:", "Task allocation weight for excavation", excavationWField), excavationWField);
        col3Grid.addRow(5, createTooltipLabel("Nursing Weight:", "Task allocation weight for nursing", nursingWField), nursingWField);
        VBox col3Box = createInspectorColumnBox("🧠 AI & Task Allocation", col3Grid);

        // Column 4: 🛡️ Weapons & Toxins
        GridPane col4Grid = createColumnGrid();
        col4Grid.addRow(0, createTooltipLabel("Weapons & Venom:", "Defensive weapons and chemical toxins equipped by caste (multiple selection supported)", casteVenomMenuButton), casteVenomMenuButton);
        col4Grid.addRow(1, createTooltipLabel("Venom Toxicity:", "Toxicity damage or effect per venom strike", casteVenomToxField), casteVenomToxField);
        col4Grid.addRow(2, createTooltipLabel("Explosive Autothysis:", "Suicidal explosive abdominal defense unique to this caste.", casteAutothysisCheck, "Autothysie"), casteAutothysisCheck);
        VBox col4Box = createInspectorColumnBox("🛡️ Weapons & Toxins", col4Grid);

        casteInspectorColumns.getChildren().addAll(col1Box, col2Box, col3Box, col4Box);

        HBox casteBtns = new HBox(10);
        Button btnAddCaste = new Button("Add / Update Caste", new FontIcon(Feather.PLUS_CIRCLE));
        btnAddCaste.getStyleClass().add("btn-primary");
        btnAddCaste.setOnAction(e -> {
            try {
                double body = Double.parseDouble(casteBodyF.getText());
                double head = Double.parseDouble(casteHeadF.getText());
                String selectedVenoms = getSelectedVenomTypes(casteVenomCheckItems);
                CasteRow sel = casteTable.getSelectionModel().getSelectedItem();
                if (sel != null && sel.getName().equalsIgnoreCase(casteNameF.getText())) {
                    sel.setBodyLengthMm(body);
                    sel.setHeadWidthMm(head);
                    sel.setLifespan(Integer.parseInt(casteLifeF.getText()));
                    sel.setHealth(Float.parseFloat(casteHealthF.getText()));
                    sel.setDamage(Float.parseFloat(casteDmgF.getText()));
                    sel.setDefense(Float.parseFloat(casteDefenseF.getText()));
                    sel.setCanFly(casteFlyCheck.isSelected());
                    sel.setTargetRatio(Float.parseFloat(targetRatioF.getText()));
                    sel.setDecisionArch(decisionArchCombo.getValue());
                    sel.setForagingWeight(Float.parseFloat(foragingWField.getText()));
                    sel.setDefenseWeight(Float.parseFloat(defenseWField.getText()));
                    sel.setExcavationWeight(Float.parseFloat(excavationWField.getText()));
                    sel.setNursingWeight(Float.parseFloat(nursingWField.getText()));
                    sel.setVenomType(selectedVenoms);
                    sel.setVenomToxicity(Float.parseFloat(casteVenomToxField.getText()));

                    sel.setWingbeatFrequencyHz(Float.parseFloat(casteWingbeatHzF.getText()));
                    sel.setHasHoveringCapability(casteHoverCheck.isSelected());
                    sel.setMaxPayloadRatio(Float.parseFloat(castePayloadRatioF.getText()));
                    sel.setBitingForceMpa(Float.parseFloat(casteBitingForceMpaF.getText()));
                    sel.setHasAutothysis(casteAutothysisCheck.isSelected());
                    sel.setHasAroliaAdhesion(casteAroliaCheck.isSelected());
                    sel.setWalkSpeedMps(Float.parseFloat(casteWalkSpeedF.getText()));
                    sel.setFlySpeedMps(Float.parseFloat(casteFlySpeedF.getText()));
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
                    row.setDefense(Float.parseFloat(casteDefenseF.getText()));
                    row.setTargetRatio(Float.parseFloat(targetRatioF.getText()));
                    row.setDecisionArch(decisionArchCombo.getValue());
                    row.setForagingWeight(Float.parseFloat(foragingWField.getText()));
                    row.setDefenseWeight(Float.parseFloat(defenseWField.getText()));
                    row.setExcavationWeight(Float.parseFloat(excavationWField.getText()));
                    row.setNursingWeight(Float.parseFloat(nursingWField.getText()));
                    row.setVenomType(selectedVenoms);
                    row.setVenomToxicity(Float.parseFloat(casteVenomToxField.getText()));

                    row.setWingbeatFrequencyHz(Float.parseFloat(casteWingbeatHzF.getText()));
                    row.setHasHoveringCapability(casteHoverCheck.isSelected());
                    row.setMaxPayloadRatio(Float.parseFloat(castePayloadRatioF.getText()));
                    row.setBitingForceMpa(Float.parseFloat(casteBitingForceMpaF.getText()));
                    row.setHasAutothysis(casteAutothysisCheck.isSelected());
                    row.setHasAroliaAdhesion(casteAroliaCheck.isSelected());
                    row.setWalkSpeedMps(Float.parseFloat(casteWalkSpeedF.getText()));
                    row.setFlySpeedMps(Float.parseFloat(casteFlySpeedF.getText()));
                    casteRows.add(row);
                }
                if (onFieldEditedCallback != null) onFieldEditedCallback.run();
            } catch (Exception ex) {
                ThemeManager.createAlert(Alert.AlertType.ERROR, "Invalid number format.").show();
            }
        });

        Button btnDelCaste = new Button("Delete Selected Caste", new FontIcon(Feather.TRASH_2));
        btnDelCaste.getStyleClass().add("btn-danger");
        btnDelCaste.setOnAction(e -> {
            CasteRow sel = casteTable.getSelectionModel().getSelectedItem();
            if (sel == null) {
                ThemeManager.createAlert(Alert.AlertType.WARNING, "Please select a caste from the table before deleting.").show();
                return;
            }
            Alert confirmAlert = ThemeManager.createAlert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this caste? This action cannot be undone.");
            confirmAlert.setTitle("Delete Caste Confirmation");
            confirmAlert.setHeaderText("Delete caste: " + sel.getName());
            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    casteRows.remove(sel);
                    if (onFieldEditedCallback != null) onFieldEditedCallback.run();
                }
            });
        });

        casteBtns.getChildren().addAll(btnAddCaste, btnDelCaste);
        casteInspectorCard.getChildren().addAll(titleInspector, casteInspectorColumns, casteBtns);

        box.getChildren().addAll(infoLabel, casteTable, casteInspectorCard);
        container = wrapScroll(box);
    }

    private ScrollPane wrapScroll(VBox content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return sp;
    }

    private GridPane createColumnGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(6));
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setMinWidth(140);
        c1.setPrefWidth(160);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);
        return grid;
    }

    private VBox createInspectorColumnBox(String titleOrKey, GridPane grid) {
        VBox box = new VBox(8);
        box.getStyleClass().add("render-options-panel");
        Label lblTitle = new Label();
        String key = lookupKeyForLabel(titleOrKey);
        lblTitle.textProperty().bind(i18n.createStringBinding(key));
        lblTitle.getStyleClass().add("section-header");
        box.getChildren().addAll(lblTitle, grid);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private Label createTooltipLabel(String text, String tooltipText, Node targetControl) {
        return createTooltipLabel(text, tooltipText, targetControl, null);
    }

    private Label createTooltipLabel(String text, String tooltipText, Node targetControl, String glossaryTerm) {
        Label l = new Label();
        String labelKey = lookupKeyForLabel(text);
        l.textProperty().bind(i18n.createStringBinding(labelKey));
        l.setStyle("-fx-font-weight: bold;");
        if (tooltipText != null && !tooltipText.isEmpty()) {
            Tooltip tt = new Tooltip();
            tt.textProperty().bind(i18n.createStringBinding(tooltipText));
            tt.setStyle("-fx-font-size: 12px; -fx-max-width: 380px; -fx-wrap-text: true;");
            l.setTooltip(tt);
            if (targetControl != null) {
                Tooltip.install(targetControl, tt);
            }
        }
        if (glossaryTerm != null && !glossaryTerm.isEmpty()) {
            l.setStyle(l.getStyle() + "; -fx-text-fill: #38bdf8; -fx-underline: true; -fx-cursor: hand;");
            l.setOnMouseClicked(e -> GlossaryDialog.show());
        }
        return l;
    }

    private String lookupKeyForLabel(String textOrKey) {
        if (textOrKey == null) return "";
        if (i18n.containsKey(textOrKey)) return textOrKey;
        return switch (textOrKey.trim()) {
            case "Caste Name:" -> "species.caste.name";
            case "Body Length (mm):" -> "species.caste.body_length";
            case "Head Width (mm):" -> "species.caste.head_width";
            case "Base Health:" -> "species.caste.health";
            case "Attack Damage:" -> "species.caste.damage";
            case "Base Armor / Defense:" -> "species.caste.armor";
            case "Lifespan (days):" -> "species.caste.lifespan";
            case "Walk Speed (m/s):" -> "species.caste.walk_speed";
            case "Arolia Adhesion:" -> "species.caste.arolia";
            case "Flight Capability:" -> "species.caste.flight";
            case "Fly Speed (m/s):" -> "species.caste.fly_speed";
            case "Wingbeat Frequency (Hz):" -> "species.caste.wingbeat";
            case "Hovering Flight:" -> "species.caste.hovering";
            case "Biting Force (MPa):" -> "species.caste.biting_force";
            case "Payload Ratio (g/g):" -> "species.caste.payload";
            case "Decision Model:" -> "species.caste.decision_model";
            case "Target Ratio (%):" -> "species.caste.target_ratio";
            case "Foraging Weight:" -> "species.caste.foraging_weight";
            case "Defense Weight:" -> "species.caste.defense_weight";
            case "Excavation Weight:" -> "species.caste.excavation_weight";
            case "Nursing Weight:" -> "species.caste.nursing_weight";
            case "Weapons & Venom:" -> "species.caste.weapons";
            case "Venom Toxicity:" -> "species.caste.venom_toxicity";
            case "Explosive Autothysis:" -> "species.caste.autothysis";
            case "👤 Identity & Morphology" -> "species.caste.section.morphology";
            case "⚡ Locomotion & Biomechanics" -> "species.caste.section.biomechanics";
            case "🧠 AI & Task Allocation" -> "species.caste.section.ai";
            case "🛡️ Weapons & Toxins" -> "species.caste.section.weapons";
            default -> textOrKey;
        };
    }

    public static String formatDec(double val) {
        if (Double.isNaN(val) || Double.isInfinite(val)) return "0";
        if (val == Math.floor(val)) return String.format(java.util.Locale.US, "%.0f", val);
        return String.format(java.util.Locale.US, "%.2f", val);
    }

    public static String formatDec(float val) {
        return formatDec((double) val);
    }

    public static String getSelectedVenomTypes(Map<String, CheckMenuItem> items) {
        List<String> selected = new ArrayList<>();
        if (items != null) {
            items.forEach((k, v) -> {
                if (v.isSelected()) selected.add(k);
            });
        }
        if (selected.isEmpty()) return "NONE";
        return String.join(",", selected);
    }

    public static void setSelectedVenomTypes(Map<String, CheckMenuItem> items, MenuButton btn, String venomStr) {
        if (items == null || btn == null) return;
        items.values().forEach(i -> i.setSelected(false));
        if (venomStr == null || venomStr.isBlank() || "NONE".equalsIgnoreCase(venomStr.trim())) {
            if (items.containsKey("NONE")) items.get("NONE").setSelected(true);
        } else {
            String[] tokens = venomStr.split("[,;]");
            for (String tok : tokens) {
                String clean = tok.trim().toUpperCase();
                if (items.containsKey(clean)) {
                    items.get(clean).setSelected(true);
                }
            }
        }
        updateVenomMenuText(btn, items);
    }

    public static void updateVenomMenuText(MenuButton btn, Map<String, CheckMenuItem> items) {
        String selStr = getSelectedVenomTypes(items);
        btn.setText(SpeciesUIFormatting.getVenomTitle(selStr));
    }
}
