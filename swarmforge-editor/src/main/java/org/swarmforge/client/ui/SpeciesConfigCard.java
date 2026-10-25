/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 SilvÃ¨re Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.core.behavior.ReasoningArchitecture.ArchitectureType;

import java.util.*;

public class SpeciesConfigCard {
    public static void selectComboIfPresent(ComboBox<String> combo, String val) {
        if (val == null || combo == null) return;
        if (combo.getItems().contains(val)) {
            combo.getSelectionModel().select(val);
            return;
        }
        String valLower = val.toLowerCase().trim();
        for (String item : combo.getItems()) {
            if (item != null) {
                String itemLower = item.toLowerCase().trim();
                if (itemLower.contains(valLower) || valLower.contains(itemLower)) {
                    combo.getSelectionModel().select(item);
                    return;
                }
            }
        }
    }

        private final String speciesName;
        private final VBox cardPane = new VBox(8);

        private Runnable onChange;

        public void setOnChange(Runnable onChange) {
            this.onChange = onChange;
        }

        private void triggerChange() {
            if (onChange != null) onChange.run();
        }

        private final ComboBox<String> nestTypeCombo = new ComboBox<>();
        private final ComboBox<String> nestStageCombo = new ComboBox<>();
        private final ComboBox<String> nestPlacementCombo = new ComboBox<>();
        private final ComboBox<String> nestRelationCombo = new ComboBox<>();
        private final Spinner<Double> posXSpinner = new Spinner<>(-500.0, 500.0, 0.0, 10.0);
        private final Spinner<Double> posZSpinner = new Spinner<>(-500.0, 500.0, 0.0, 10.0);
        private final Spinner<Integer> initialFoodSpinner = new Spinner<>(0, 50000, 500, 50);
        private HBox manualPosBox;

        private final Spinner<Integer> queenSpinner = new Spinner<>(0, 50, 1);
        private final Spinner<Integer> workerSpinner = new Spinner<>(0, 10000, 500, 50);
        private final Spinner<Integer> soldierSpinner = new Spinner<>(0, 2000, 50, 10);
        private final Spinner<Integer> broodSpinner = new Spinner<>(0, 2000000, 200, 50);

        private final ComboBox<ArchitectureType> workerEngineCombo = new ComboBox<>();
        private final ComboBox<ArchitectureType> soldierEngineCombo = new ComboBox<>();
        private final ComboBox<ArchitectureType> queenEngineCombo = new ComboBox<>();

        private final VBox accessoryBoxPane = new VBox(4);

        public SpeciesConfigCard(String speciesName, Runnable onRemove) {
            this.speciesName = speciesName;

            cardPane.getStyleClass().add("card-pane");

            // Header
            HBox header = new HBox(8);
            header.setAlignment(Pos.CENTER_LEFT);

            Label lblTitle = new Label();
            lblTitle.textProperty().bind(javafx.beans.binding.Bindings.concat("ðŸœ ", I18nManager.getInstance().createStringBinding("sim.species.title"), ": ", speciesName));
            lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
            lblTitle.getStyleClass().add("accent-title");

            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            Button btnRemove = new Button();
            btnRemove.textProperty().bind(I18nManager.getInstance().createStringBinding("sim.btn.remove_species"));
            btnRemove.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-cursor: hand;");
            btnRemove.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("sim.btn.remove_species.tt", "Supprime cette colonie et cette espÃ¨ce de la configuration du scÃ©nario."));
            btnRemove.setOnAction(e -> {
                Alert confirm = org.swarmforge.client.util.ThemeManager.createAlert(
                    Alert.AlertType.CONFIRMATION,
                    "Voulez-vous vraiment retirer l'espÃ¨ce '" + speciesName + "' de ce scÃ©nario ?"
                );
                confirm.showAndWait().ifPresent(btn -> {
                    if (btn == ButtonType.OK) onRemove.run();
                });
            });

            header.getChildren().addAll(lblTitle, sp, btnRemove);

            // 1. Filtered Nest, Placement & Inter-Nest Strategy
            VBox nestConfigBox = new VBox(6);

            HBox nestRow = new HBox(8);
            nestRow.setAlignment(Pos.CENTER_LEFT);

            Label lblNest = new Label();
            lblNest.textProperty().bind(I18nManager.getInstance().createStringBinding("sim.nest.architecture"));
            lblNest.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");
            lblNest.getStyleClass().add("purple-accent-title");

            selectDefaultNestArchitectureForSpecies(speciesName, nestTypeCombo);
            nestTypeCombo.setTooltip(new Tooltip("Nest physical shape and architectural typology suited to this species."));
            nestRow.getChildren().addAll(lblNest, nestTypeCombo);

            HBox stageRow = new HBox(8);
            stageRow.setAlignment(Pos.CENTER_LEFT);

            Label lblStage = new Label();
            lblStage.textProperty().bind(I18nManager.getInstance().createStringBinding("sim.nest.stage"));
            lblStage.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");
            lblStage.getStyleClass().add("purple-accent-title");

            nestStageCombo.setTooltip(new Tooltip("Colony demographic maturity stage. Automatically adjusts caste counts."));
            stageRow.getChildren().addAll(lblStage, nestStageCombo);

            HBox placementRow = new HBox(8);
            placementRow.setAlignment(Pos.CENTER_LEFT);

            Label lblPlacement = new Label();
            lblPlacement.textProperty().bind(I18nManager.getInstance().createStringBinding("sim.nest.placement"));
            lblPlacement.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");
            lblPlacement.getStyleClass().add("purple-accent-title");

            nestPlacementCombo.setTooltip(new Tooltip("Spatial placement strategy of the nest in the 3D grid."));
            placementRow.getChildren().addAll(lblPlacement, nestPlacementCombo);

            HBox foodRow = new HBox(8);
            foodRow.setAlignment(Pos.CENTER_LEFT);

            Label lblInitialFood = new Label();
            lblInitialFood.textProperty().bind(I18nManager.getInstance().createStringBinding("sim.nest.food"));
            lblInitialFood.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");
            lblInitialFood.getStyleClass().add("purple-accent-title");

            initialFoodSpinner.setPrefWidth(75);
            initialFoodSpinner.setEditable(true);
            initialFoodSpinner.setTooltip(new Tooltip("Initial food resource reserve allocated to the nest."));
            foodRow.getChildren().addAll(lblInitialFood, initialFoodSpinner);

            nestConfigBox.getChildren().addAll(nestRow, stageRow, placementRow, foodRow);

            // Manual Position Box (Visible when manual placement is chosen)
            manualPosBox = new HBox(8);
            manualPosBox.setAlignment(Pos.CENTER_LEFT);
            manualPosBox.setStyle("-fx-padding: 4 0 0 0;");

            Label lblManualX = new Label();
            lblManualX.textProperty().bind(I18nManager.getInstance().createStringBinding("sim.nest.coord_x"));
            lblManualX.setStyle("-fx-font-size: 10px;");
            lblManualX.getStyleClass().add("accent-title");
            posXSpinner.setPrefWidth(85); posXSpinner.setEditable(true);

            Label lblManualZ = new Label();
            lblManualZ.textProperty().bind(I18nManager.getInstance().createStringBinding("sim.nest.coord_z"));
            lblManualZ.setStyle("-fx-font-size: 10px;");
            lblManualZ.getStyleClass().add("accent-title");
            posZSpinner.setPrefWidth(85); posZSpinner.setEditable(true);

            manualPosBox.getChildren().addAll(lblManualX, posXSpinner, lblManualZ, posZSpinner);
            manualPosBox.setVisible(false);
            manualPosBox.setManaged(false);

            nestPlacementCombo.setOnAction(e -> {
                boolean manual = nestPlacementCombo.getSelectionModel().getSelectedIndex() == 1;
                manualPosBox.setVisible(manual);
                manualPosBox.setManaged(manual);
            });

            // Nest Relationship & Supercolony Membership Row
            VBox relationBox = new VBox(6);

            HBox relationRow = new HBox(8);
            relationRow.setAlignment(Pos.CENTER_LEFT);

            Label lblRelation = new Label();
            lblRelation.textProperty().bind(I18nManager.getInstance().createStringBinding("sim.nest.relationship"));
            lblRelation.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");
            lblRelation.getStyleClass().add("purple-accent-title");

            nestRelationCombo.setTooltip(new Tooltip("Diplomatic relationship towards other nests of the same or different species."));

            relationRow.getChildren().addAll(lblRelation, nestRelationCombo);

            CheckBox chkSupercolonyMember = new CheckBox();
            chkSupercolonyMember.textProperty().bind(I18nManager.getInstance().createStringBinding("sim.card.supercolony"));
            chkSupercolonyMember.setSelected(false);
            chkSupercolonyMember.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
            chkSupercolonyMember.getStyleClass().add("accent-title");
            chkSupercolonyMember.setTooltip(new Tooltip("Check to integrate this colony into the cooperative supercolony network. Unchecked nests remain independent colonies in competition or warfare."));

            relationBox.getChildren().addAll(relationRow, chkSupercolonyMember);

            // 2. Demographics & AI Engines Standard Block
            VBox demoBox = new VBox(6);
            demoBox.getStyleClass().add("card-pane");

            Label lblDemoTitle = new Label();
            lblDemoTitle.textProperty().bind(I18nManager.getInstance().createStringBinding("sim.demographics.title"));
            lblDemoTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
            lblDemoTitle.getStyleClass().add("accent-title");

            Label lblSpatialInfo = new Label();
            lblSpatialInfo.textProperty().bind(I18nManager.getInstance().createStringBinding("sim.demographics.note"));
            lblSpatialInfo.setStyle("-fx-font-size: 9.5px; -fx-wrap-text: true;");
            lblSpatialInfo.getStyleClass().add("sub-title-gray");

            GridPane demoGrid = new GridPane();
            demoGrid.setHgap(8); demoGrid.setVgap(4);

            for (ArchitectureType type : ArchitectureType.values()) {
                workerEngineCombo.getItems().add(type);
                soldierEngineCombo.getItems().add(type);
                queenEngineCombo.getItems().add(type);
            }

            ArchitectureType workerArch = ArchitectureType.BEHAVIOR_TREE;
            ArchitectureType soldierArch = ArchitectureType.BEHAVIOR_TREE;
            ArchitectureType queenArch = ArchitectureType.BEHAVIOR_TREE;

            NestPresetManager npm = new NestPresetManager();
            Map<String, Object> nP = npm.get(nestTypeCombo.getValue());
            if (nP != null && nP.get("preferredBrainArchitecture") != null) {
                try {
                    ArchitectureType parsed = ArchitectureType.valueOf(nP.get("preferredBrainArchitecture").toString().toUpperCase());
                    workerArch = parsed;
                    soldierArch = parsed;
                    queenArch = parsed;
                } catch (Exception ignored) {}
            }

            workerEngineCombo.getSelectionModel().select(workerArch);
            soldierEngineCombo.getSelectionModel().select(soldierArch);
            queenEngineCombo.getSelectionModel().select(queenArch);

            workerEngineCombo.valueProperty().addListener((o, oldV, newV) -> {
                if (newV != null) {
                    soldierEngineCombo.getSelectionModel().select(newV);
                    queenEngineCombo.getSelectionModel().select(newV);
                }
            });

            workerSpinner.setPrefWidth(90); workerSpinner.setEditable(true);
            soldierSpinner.setPrefWidth(90); soldierSpinner.setEditable(true);
            queenSpinner.setPrefWidth(90); queenSpinner.setEditable(true);
            broodSpinner.setPrefWidth(90); broodSpinner.setEditable(true);

            workerSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5000000, 100, 100));
            soldierSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 500000, 10, 10));
            queenSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 1, 1));
            broodSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 2000000, 50, 50));

            nestStageCombo.valueProperty().addListener((o, oldV, newV) -> {
                applyDemographicsFromStage(nestStageCombo.getSelectionModel().getSelectedIndex());
                triggerChange();
            });

            queenSpinner.valueProperty().addListener((o, oldV, newV) -> triggerChange());
            workerSpinner.valueProperty().addListener((o, oldV, newV) -> triggerChange());
            soldierSpinner.valueProperty().addListener((o, oldV, newV) -> triggerChange());
            broodSpinner.valueProperty().addListener((o, oldV, newV) -> triggerChange());
            initialFoodSpinner.valueProperty().addListener((o, oldV, newV) -> triggerChange());

            applyDemographicsFromStage(1);

            Label lblQ = new Label(); lblQ.textProperty().bind(I18nManager.getInstance().createStringBinding("sim.caste.queens")); lblQ.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;"); lblQ.getStyleClass().add("bold-label");
            Label lblQEngine = new Label(); lblQEngine.textProperty().bind(I18nManager.getInstance().createStringBinding("sim.ai.engine")); lblQEngine.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;"); lblQEngine.getStyleClass().add("bold-label");
            demoGrid.add(lblQ, 0, 0); demoGrid.add(queenSpinner, 1, 0); demoGrid.add(lblQEngine, 2, 0); demoGrid.add(queenEngineCombo, 3, 0);

            Label lblW = new Label(); lblW.textProperty().bind(I18nManager.getInstance().createStringBinding("sim.caste.workers")); lblW.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;"); lblW.getStyleClass().add("bold-label");
            Label lblWEngine = new Label(); lblWEngine.textProperty().bind(I18nManager.getInstance().createStringBinding("sim.ai.engine")); lblWEngine.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;"); lblWEngine.getStyleClass().add("bold-label");
            demoGrid.add(lblW, 0, 1); demoGrid.add(workerSpinner, 1, 1); demoGrid.add(lblWEngine, 2, 1); demoGrid.add(workerEngineCombo, 3, 1);

            Label lblS = new Label(); lblS.textProperty().bind(I18nManager.getInstance().createStringBinding("sim.caste.soldiers")); lblS.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;"); lblS.getStyleClass().add("bold-label");
            Label lblSEngine = new Label(); lblSEngine.textProperty().bind(I18nManager.getInstance().createStringBinding("sim.ai.engine")); lblSEngine.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;"); lblSEngine.getStyleClass().add("bold-label");
            demoGrid.add(lblS, 0, 2); demoGrid.add(soldierSpinner, 1, 2); demoGrid.add(lblSEngine, 2, 2); demoGrid.add(soldierEngineCombo, 3, 2);

            Label lblB = new Label(); lblB.textProperty().bind(I18nManager.getInstance().createStringBinding("sim.caste.brood")); lblB.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;"); lblB.getStyleClass().add("bold-label");
            demoGrid.add(lblB, 0, 3); demoGrid.add(broodSpinner, 1, 3);

            demoBox.getChildren().addAll(lblDemoTitle, lblSpatialInfo, demoGrid);

            Label lblAccessoryTitle = new Label();
            lblAccessoryTitle.textProperty().bind(javafx.beans.binding.Bindings.concat("ðŸŒ¿ ", I18nManager.getInstance().createStringBinding("sim.accessory.title"), " (", getShortSpeciesName(speciesName), ") :"));
            lblAccessoryTitle.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 10px;");
            lblAccessoryTitle.getStyleClass().add("accent-title");

            updateTexts();
            I18nManager.getInstance().localeProperty().addListener((obs, oldLoc, newLoc) -> updateTexts());

            setupAccessoryRows(speciesName);

            cardPane.getChildren().addAll(header, nestConfigBox, manualPosBox, relationBox, new Separator(), demoBox, new Separator(), lblAccessoryTitle, accessoryBoxPane);
        }

        public void updateTexts() {
            int stageIdx = nestStageCombo.getSelectionModel().getSelectedIndex();
            if (stageIdx < 0) stageIdx = 1;
            nestStageCombo.getItems().setAll(
                I18nManager.getInstance().get("sim.stage.claustral"),
                I18nManager.getInstance().get("sim.stage.young"),
                I18nManager.getInstance().get("sim.stage.mature"),
                I18nManager.getInstance().get("sim.stage.supercolony")
            );
            nestStageCombo.getSelectionModel().select(stageIdx);

            int placementIdx = nestPlacementCombo.getSelectionModel().getSelectedIndex();
            if (placementIdx < 0) placementIdx = 0;
            nestPlacementCombo.getItems().setAll(
                I18nManager.getInstance().get("sim.placement.center"),
                I18nManager.getInstance().get("sim.placement.manual"),
                I18nManager.getInstance().get("sim.placement.queen"),
                I18nManager.getInstance().get("sim.placement.random")
            );
            nestPlacementCombo.getSelectionModel().select(placementIdx);

            int relIdx = nestRelationCombo.getSelectionModel().getSelectedIndex();
            if (relIdx < 0) relIdx = 0;
            nestRelationCombo.getItems().setAll(
                I18nManager.getInstance().get("sim.relation.hostile"),
                I18nManager.getInstance().get("sim.relation.allied"),
                I18nManager.getInstance().get("sim.relation.neutral")
            );
            nestRelationCombo.getSelectionModel().select(relIdx);
        }

        private final List<AccessoryRowControls> accessoryControlsList = new ArrayList<>();

        private static class AccessoryRowControls {
            CheckBox chk;
            String name;
            String role;
            Spinner<Integer> countSpinner;
            ComboBox<String> strategyCombo;

            AccessoryRowControls(CheckBox chk, String name, String role, Spinner<Integer> countSpinner, ComboBox<String> strategyCombo) {
                this.chk = chk;
                this.name = name;
                this.role = role;
                this.countSpinner = countSpinner;
                this.strategyCombo = strategyCombo;
            }
        }

        private void setupAccessoryRows(String speciesName) {
            accessoryBoxPane.getChildren().clear();
            accessoryControlsList.clear();
            List<AccessorySpeciesInfo> compatList = getCompatibleAccessorySpecies(speciesName);

            VBox listVBox = new VBox(4);
            for (AccessorySpeciesInfo info : compatList) {
                HBox row = new HBox(8);
                row.setAlignment(Pos.CENTER_LEFT);

                CheckBox chk = new CheckBox(info.name());
                chk.setSelected(true);
                chk.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
                chk.getStyleClass().add("bold-label");
                chk.setTooltip(new Tooltip(info.description()));
                chk.setPrefWidth(220);
                chk.selectedProperty().addListener((o, oldV, newV) -> triggerChange());

                Label lblRole = new Label("[" + info.role() + "]");
                lblRole.setStyle(getRoleStyle(info.role()));
                lblRole.getStyleClass().add("bold-label");
                lblRole.setPrefWidth(95);

                Label lblCount = new Label("Initial Count :");
                lblCount.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold;");
                lblCount.getStyleClass().add("bold-label");

                Spinner<Integer> countSpinner = new Spinner<>(5, 1000, info.defaultCount(), 10);
                countSpinner.setPrefWidth(70); countSpinner.setEditable(true);
                countSpinner.valueProperty().addListener((o, oldV, newV) -> triggerChange());

                Label lblStrategy = new Label("Replenishment Strategy :");
                lblStrategy.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold;");
                lblStrategy.getStyleClass().add("bold-label");

                ComboBox<String> strategyCombo = new ComboBox<>();
                strategyCombo.getItems().addAll(
                    "ðŸ”„ Dynamic Equilibrium (Auto-maintained < 30% Pop)",
                    "âš¡ Episodic Incursions (Pop-in Waves every 500 Ticks)",
                    "â³ Finite Depletion (Limited Resource / Prey Reserves)",
                    "ðŸŒ¿ Seasonal Logistic Regeneration (10-50%/day)"
                );
                strategyCombo.getSelectionModel().selectFirst();
                strategyCombo.setStyle("-fx-font-size: 9px;");
                strategyCombo.setTooltip(new Tooltip("Replenishment strategy to maintain pressure without destabilizing the simulation."));
                strategyCombo.valueProperty().addListener((o, oldV, newV) -> triggerChange());

                accessoryControlsList.add(new AccessoryRowControls(chk, info.name(), info.role(), countSpinner, strategyCombo));

                row.getChildren().addAll(chk, lblRole, lblCount, countSpinner, lblStrategy, strategyCombo);
                listVBox.getChildren().add(row);
            }
            accessoryBoxPane.getChildren().add(listVBox);
        }

        private String getRoleStyle(String role) {
            if (role == null) return "-fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 9.5px;";
            String r = role.toUpperCase().trim();
            if (r.contains("PROIE") || r.contains("PREY")) {
                return "-fx-text-fill: #22c55e; -fx-font-weight: bold; -fx-font-size: 9.5px;";
            } else if (r.contains("PRED") || r.contains("PRÃ‰D")) {
                return "-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 9.5px;";
            } else if (r.contains("COMMENSAL") || r.contains("SYMBIOT") || r.contains("FLORA")) {
                return "-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 9.5px;";
            } else if (r.contains("PATHOG") || r.contains("PARASIT")) {
                return "-fx-text-fill: #eab308; -fx-font-weight: bold; -fx-font-size: 9.5px;";
            }
            return "-fx-text-fill: #a78bfa; -fx-font-weight: bold; -fx-font-size: 9.5px;";
        }

        private String getShortSpeciesName(String full) {
            if (full == null) return "";
            int idx = full.indexOf("(");
            return idx > 0 ? full.substring(0, idx).trim() : full;
        }

        private void applyDemographicsFromStage(int stageIndex) {
            if (queenSpinner == null || workerSpinner == null || soldierSpinner == null || broodSpinner == null) return;
            switch (stageIndex) {
                case 0 -> { // Claustral Founding Cell
                    queenSpinner.getValueFactory().setValue(1);
                    workerSpinner.getValueFactory().setValue(0);
                    soldierSpinner.getValueFactory().setValue(0);
                    broodSpinner.getValueFactory().setValue(10);
                }
                case 1 -> { // Incipient / Young Nest
                    queenSpinner.getValueFactory().setValue(1);
                    workerSpinner.getValueFactory().setValue(100);
                    soldierSpinner.getValueFactory().setValue(10);
                    broodSpinner.getValueFactory().setValue(50);
                }
                case 3 -> { // Complex Supercolony Network
                    queenSpinner.getValueFactory().setValue(50);
                    workerSpinner.getValueFactory().setValue(500000);
                    soldierSpinner.getValueFactory().setValue(50000);
                    broodSpinner.getValueFactory().setValue(200000);
                }
                default -> { // Established / Mature Colony (Stage 2)
                    queenSpinner.getValueFactory().setValue(1);
                    workerSpinner.getValueFactory().setValue(5000);
                    soldierSpinner.getValueFactory().setValue(500);
                    broodSpinner.getValueFactory().setValue(2500);
                }
            }
        }

        public VBox getCardPane() { return cardPane; }
        public String getSpeciesName() { return speciesName; }
        public String getNestType() { return nestTypeCombo.getValue(); }
        public String getNestStage() { return nestStageCombo.getValue(); }
        public int getNestStageIndex() { return nestStageCombo.getSelectionModel().getSelectedIndex(); }
        public void setNestType(String nestType) {
            if (nestTypeCombo.getItems().contains(nestType)) {
                nestTypeCombo.getSelectionModel().select(nestType);
            } else if (nestType != null) {
                nestTypeCombo.getItems().add(nestType);
                nestTypeCombo.getSelectionModel().select(nestType);
            }
            triggerChange();
        }
        public void setNestStageIndex(int index) {
            if (index >= 0 && index < nestStageCombo.getItems().size()) {
                nestStageCombo.getSelectionModel().select(index);
                applyDemographicsFromStage(index);
            }
            triggerChange();
        }
        public int getQueenCount() { return queenSpinner.getValue(); }
        public void setQueenCount(int count) { queenSpinner.getValueFactory().setValue(count); triggerChange(); }
        public int getWorkerCount() { return workerSpinner.getValue(); }
        public void setWorkerCount(int count) { workerSpinner.getValueFactory().setValue(count); triggerChange(); }
        public int getSoldierCount() { return soldierSpinner.getValue(); }
        public void setSoldierCount(int count) { soldierSpinner.getValueFactory().setValue(count); triggerChange(); }
        public int getBroodCount() { return broodSpinner.getValue(); }
        public void setBroodCount(int count) { broodSpinner.getValueFactory().setValue(count); triggerChange(); }
        public ArchitectureType getWorkerEngine() { return workerEngineCombo.getValue(); }
        public ArchitectureType getSoldierEngine() { return soldierEngineCombo.getValue(); }
        public ArchitectureType getQueenEngine() { return queenEngineCombo.getValue(); }
        public int getInitialFood() { return initialFoodSpinner.getValue(); }
        public void setInitialFood(int food) { initialFoodSpinner.getValueFactory().setValue(food); triggerChange(); }

        public List<AccessoryConfigSnapshot> getAccessorySnapshots() {
            List<AccessoryConfigSnapshot> list = new ArrayList<>();
            for (AccessoryRowControls ctrl : accessoryControlsList) {
                list.add(new AccessoryConfigSnapshot(
                    ctrl.name,
                    ctrl.role,
                    ctrl.chk.isSelected(),
                    ctrl.countSpinner.getValue(),
                    ctrl.strategyCombo.getValue()
                ));
            }
            return list;
        }

        public void setAccessorySnapshots(List<AccessoryConfigSnapshot> snapshots) {
            if (snapshots == null) return;
            for (AccessoryConfigSnapshot sSnap : snapshots) {
                for (AccessoryRowControls ctrl : accessoryControlsList) {
                    if (ctrl.name.equalsIgnoreCase(sSnap.name())) {
                        ctrl.chk.setSelected(sSnap.enabled());
                        ctrl.countSpinner.getValueFactory().setValue(sSnap.initialCount());
                        if (ctrl.strategyCombo.getItems().contains(sSnap.renewalStrategy())) {
                            ctrl.strategyCombo.getSelectionModel().select(sSnap.renewalStrategy());
                        }
                    }
                }
            }
            triggerChange();
        }

        public boolean hasActivePreyAccessory() {
            for (AccessoryRowControls ctrl : accessoryControlsList) {
                if ("PROIE".equals(ctrl.role) && ctrl.chk.isSelected()) {
                    return true;
                }
            }
            return false;
        }

        public boolean hasActivePredatorAccessory() {
            for (AccessoryRowControls ctrl : accessoryControlsList) {
                if ("PRÃ‰DATEUR".equals(ctrl.role) && ctrl.chk.isSelected()) {
                    return true;
                }
            }
            return false;
        }

        public void enableAccessorySpeciesRole(String role) {
            for (AccessoryRowControls ctrl : accessoryControlsList) {
                if (role.equals(ctrl.role)) {
                    ctrl.chk.setSelected(true);
                }
            }
            triggerChange();
        }

        public String getNestPlacement() { return nestPlacementCombo.getValue(); }
        public void setNestPlacement(String placement) {
            selectComboIfPresent(nestPlacementCombo, placement);
            boolean manual = nestPlacementCombo.getSelectionModel().getSelectedIndex() == 1;
            if (manualPosBox != null) {
                manualPosBox.setVisible(manual);
                manualPosBox.setManaged(manual);
            }
            triggerChange();
        }
        public double getCustomX() { return posXSpinner.getValue(); }
        public void setCustomX(double x) { posXSpinner.getValueFactory().setValue(x); triggerChange(); }
        public double getCustomZ() { return posZSpinner.getValue(); }
        public void setCustomZ(double z) { posZSpinner.getValueFactory().setValue(z); triggerChange(); }
        public String getNestRelation() { return nestRelationCombo.getValue(); }
        public void setNestRelation(String relation) {
            selectComboIfPresent(nestRelationCombo, relation);
            triggerChange();
        }

        public SpeciesConfigSnapshot getSnapshot() {
            return new SpeciesConfigSnapshot(
                speciesName,
                getNestType(),
                getQueenCount(),
                getWorkerCount(),
                getSoldierCount(),
                getBroodCount(),
                getInitialFood(),
                getWorkerEngine(),
                getSoldierEngine(),
                getQueenEngine(),
                getAccessorySnapshots(),
                getNestPlacement(),
                getCustomX(),
                getCustomZ(),
                getNestRelation()
            );
        }

    public static int estimateNestCapacity(String nestType) {
        if (nestType == null) return 25000;
        String lower = nestType.toLowerCase();
        if (lower.contains("stem") || lower.contains("gall") || lower.contains("galle") || lower.contains("tige") || lower.contains("founding") || lower.contains("claustral")) {
            return 500;
        } else if (lower.contains("bivouac") || lower.contains("pot") || lower.contains("silk") || lower.contains("paper") || lower.contains("guÃªpe")) {
            return 5000;
        } else if (lower.contains("tree") || lower.contains("cavity") || lower.contains("carton") || lower.contains("comb") || lower.contains("beehive") || lower.contains("ruche")) {
            return 20000;
        } else if (lower.contains("subterranean") || lower.contains("burrow") || lower.contains("galerie") || lower.contains("souterrain") || lower.contains("needle") || lower.contains("dÃ´me")) {
            return 50000;
        } else if (lower.contains("supercolonie") || lower.contains("supercolony") || lower.contains("fungi") || lower.contains("vault") || lower.contains("cathÃ©drale") || lower.contains("cathedral")) {
            return 500000;
        }
        return 25000;
    }

    public record AccessorySpeciesInfo(String name, String role, String description, int defaultCount) {
        public AccessorySpeciesInfo(String name, String role, String description) {
            this(name, role, description, 50);
        }
    }

    /**
     * Helper method to filter physical nest architectures based on biological species capability.
     * Guaranteed exact biological match returning pure physical nest typologies.
     */
    public static List<String> getCompatibleNestArchitectures(String speciesName) {
        List<String> nests = new ArrayList<>();
        nests.add("Subterranean Burrows & Galleries");

        if (speciesName != null) {
            String lower = speciesName.toLowerCase();
            if (lower.contains("atta") || lower.contains("coupeuse")) {
                nests.add("Subterranean Fungi Vault");
            } else if (lower.contains("apis") || lower.contains("abeille")) {
                nests.add("Hexagonal Wax Comb");
                nests.add("Wooden Beehive");
            } else if (lower.contains("bombus") || lower.contains("bourdon")) {
                nests.add("Wax & Propolis Pots");
            } else if (lower.contains("vespula") || lower.contains("guÃªpe")) {
                nests.add("Suspended Paper Nest");
            } else if (lower.contains("reticulitermes") || lower.contains("macrotermes") || lower.contains("termite")) {
                nests.add("Cathedral Termite Mound");
            } else if (lower.contains("crematogaster") || lower.contains("carton")) {
                nests.add("Carton Wooden Nest");
            } else if (lower.contains("temnothorax") || lower.contains("gall") || lower.contains("galle")) {
                nests.add("Hollow Stem & Gall Nest");
            } else if (lower.contains("camponotus") || lower.contains("carpenter") || lower.contains("charpentiÃ¨re")) {
                nests.add("Tree Trunk Cavity Nest");
            } else if (lower.contains("eciton") || lower.contains("army") || lower.contains("lÃ©gionnaire")) {
                nests.add("Living Bivouac");
            } else if (lower.contains("oecophylla") || lower.contains("weaver") || lower.contains("tisserande")) {
                nests.add("Arboreal Silk Nest");
            } else if (lower.contains("formica") || lower.contains("wood ant") || lower.contains("rousse")) {
                nests.add("Solar Needle Mound");
            }
        }
        Collections.sort(nests);
        return nests;
    }

    public static void selectDefaultNestArchitectureForSpecies(String speciesName, ComboBox<String> combo) {
        List<String> list = getCompatibleNestArchitectures(speciesName);
        combo.getItems().setAll(list);
        if (speciesName != null) {
            String matched = null;
            for (String arch : list) {
                if (!arch.startsWith("Subterranean Burrows")) {
                    matched = arch;
                    break;
                }
            }
            if (matched != null) {
                combo.getSelectionModel().select(matched);
                return;
            }
        }
        if (!combo.getItems().isEmpty()) {
            combo.getSelectionModel().selectFirst();
        }
    }

    public static List<String> getCompatibleNestTypes(String speciesName) {
        return getCompatibleNestArchitectures(speciesName);
    }

    /**
     * Helper method to filter compatible accessory species (Proies, PrÃ©dateurs, Commensaux, PathogÃ¨nes).
     * Excludes neutral or biologically incompatible species.
     */
    public static List<AccessorySpeciesInfo> getCompatibleAccessorySpecies(String speciesName) {
        List<AccessorySpeciesInfo> list = new ArrayList<>();
        if (speciesName == null) return list;
        String lower = speciesName.toLowerCase();

        if (lower.contains("atta") || lower.contains("coupeuse")) {
            list.add(new AccessorySpeciesInfo("Seeded Grasses & Foliage", "COMMENSAL", "Foliage providing symbiotic plant biomass", 150));
            list.add(new AccessorySpeciesInfo("Humid Moss (Polytrichum)", "COMMENSAL", "Substrate maintaining humidity for fungus garden", 80));
            list.add(new AccessorySpeciesInfo("Entomopathogenic Fungus (Cordyceps)", "PATHOGEN", "Cordyceps targeting workers in forest environments", 20));
        } else if (lower.contains("messor") || lower.contains("pogonomyrmex") || lower.contains("moissonneuse")) {
            list.add(new AccessorySpeciesInfo("Seeded Grasses (Herbs & Biomass)", "PROIE", "Plant seeds for harvesting and granaries", 200));
            list.add(new AccessorySpeciesInfo("Mealworm Larvae (Protein Prey)", "PROIE", "Arthropod prey providing nitrogen to brood", 40));
            list.add(new AccessorySpeciesInfo("Antlion Pitfall Trap (Myrmeleon)", "PRÃ‰DATEUR", "Funnel-web pitfall predator in sand", 10));
        } else if (lower.contains("apis") || lower.contains("abeille")) {
            list.add(new AccessorySpeciesInfo("Nectariferous Grasses & Flowers", "COMMENSAL", "Melliferous flowers supplying nectar and pollen", 120));
            list.add(new AccessorySpeciesInfo("Parasitic Mite (Varroa destructor)", "PATHOGEN", "Hematophagous parasitic mite targeting bee brood", 30));
            list.add(new AccessorySpeciesInfo("Hunting Spider / Robber Fly", "PRÃ‰DATEUR", "Predators capturing foragers in flight", 15));
        } else if (lower.contains("reticulitermes") || lower.contains("macrotermes") || lower.contains("termite")) {
            list.add(new AccessorySpeciesInfo("Humid Moss & Woody Timber", "COMMENSAL", "Digestible cellulosic biomass", 100));
            list.add(new AccessorySpeciesInfo("Mealworm Larvae (Protein Prey)", "PROIE", "Residual soil arthropod prey", 30));
            list.add(new AccessorySpeciesInfo("Hunting Wasp / Subterranean Spider", "PRÃ‰DATEUR", "Gallery specialist predators", 12));
        } else if (lower.contains("vespula") || lower.contains("guÃªpe")) {
            list.add(new AccessorySpeciesInfo("Mealworm Larvae & Caterpillars (Prey)", "PROIE", "Insect prey hunted for brood protein", 60));
            list.add(new AccessorySpeciesInfo("Grasses & Flowers (Nectar)", "COMMENSAL", "Sugary energy sources for adults", 80));
            list.add(new AccessorySpeciesInfo("Entomopathogenic Fungus (Cordyceps)", "PATHOGEN", "Spores carried during flight", 15));
        } else { // Lasius, Solenopsis, Formica, etc.
            list.add(new AccessorySpeciesInfo("Pine Aphids (Cinara / Honeydew)", "COMMENSAL", "Aphids farmed in trophobiosis for sugary honeydew", 80));
            list.add(new AccessorySpeciesInfo("Seeded Grasses (Herbs & Biomass)", "COMMENSAL", "Local vegetation providing substrate and shade", 100));
            list.add(new AccessorySpeciesInfo("Mealworm Larvae (Protein Prey)", "PROIE", "Arthropodes proies apportant les protÃ©ines", 50));
            list.add(new AccessorySpeciesInfo("Antlion Pitfall Trap (Myrmeleon)", "PRÃ‰DATEUR", "Natural predator of patrolling workers", 15));
            list.add(new AccessorySpeciesInfo("Entomopathogenic Fungus (Cordyceps)", "PATHOGEN", "Mycelial parasite targeting colony", 10));
        }
        list.sort(Comparator.comparing(AccessorySpeciesInfo::name));
        return list;
    }
}
