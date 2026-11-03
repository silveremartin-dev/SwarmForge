/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.species;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.swarmforge.client.ui.SpeciesEditorPane.FormattedDoubleStringConverter;
import org.swarmforge.client.ui.SpeciesEditorPane.FormattedFloatStringConverter;

/**
 * Builds and configures the polymorphic Caste summary {@link TableView}
 * with customized editors and SI metric formatting.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public final class SpeciesCasteTableBuilder {

    private SpeciesCasteTableBuilder() {}

    public static TableView<CasteRow> buildCasteTable(ObservableList<CasteRow> casteRows) {
        TableView<CasteRow> casteTable = new TableView<>(casteRows);
        casteTable.setEditable(true);
        casteTable.setPrefHeight(240);

        TableColumn<CasteRow, String> nameCol = new TableColumn<>("Caste");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(e -> e.getRowValue().setName(e.getNewValue()));
        nameCol.setPrefWidth(130);

        TableColumn<CasteRow, Double> bodyCol = new TableColumn<>("Length (mm)");
        bodyCol.setCellValueFactory(new PropertyValueFactory<>("bodyLengthMm"));
        bodyCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new FormattedDoubleStringConverter()));
        bodyCol.setOnEditCommit(e -> {
            e.getRowValue().setBodyLengthMm(e.getNewValue());
            casteTable.refresh();
        });
        bodyCol.setPrefWidth(95);

        TableColumn<CasteRow, Double> headCol = new TableColumn<>("Head (mm)");
        headCol.setCellValueFactory(new PropertyValueFactory<>("headWidthMm"));
        headCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new FormattedDoubleStringConverter()));
        headCol.setOnEditCommit(e -> {
            e.getRowValue().setHeadWidthMm(e.getNewValue());
            casteTable.refresh();
        });
        headCol.setPrefWidth(75);

        TableColumn<CasteRow, Double> tunnelCol = new TableColumn<>("Ø Tunnel Min (mm)");
        tunnelCol.setCellValueFactory(new PropertyValueFactory<>("minTunnelMm"));
        tunnelCol.setCellFactory(col -> new TableCell<CasteRow, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : SpeciesCastesPane.formatDec(item));
            }
        });
        tunnelCol.setPrefWidth(125);

        TableColumn<CasteRow, Integer> lifeCol = new TableColumn<>("Lifespan (d)");
        lifeCol.setCellValueFactory(new PropertyValueFactory<>("lifespan"));
        lifeCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new javafx.util.converter.IntegerStringConverter()));
        lifeCol.setOnEditCommit(e -> e.getRowValue().setLifespan(e.getNewValue()));
        lifeCol.setPrefWidth(85);

        TableColumn<CasteRow, Float> healthCol = new TableColumn<>("Health");
        healthCol.setCellValueFactory(new PropertyValueFactory<>("health"));
        healthCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new FormattedFloatStringConverter()));
        healthCol.setOnEditCommit(e -> e.getRowValue().setHealth(e.getNewValue()));
        healthCol.setPrefWidth(65);

        TableColumn<CasteRow, Float> dmgCol = new TableColumn<>("Attack");
        dmgCol.setCellValueFactory(new PropertyValueFactory<>("damage"));
        dmgCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new FormattedFloatStringConverter()));
        dmgCol.setOnEditCommit(e -> e.getRowValue().setDamage(e.getNewValue()));
        dmgCol.setPrefWidth(65);

        TableColumn<CasteRow, Float> armorCol = new TableColumn<>("Armor");
        armorCol.setCellValueFactory(new PropertyValueFactory<>("defense"));
        armorCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new FormattedFloatStringConverter()));
        armorCol.setOnEditCommit(e -> e.getRowValue().setDefense(e.getNewValue()));
        armorCol.setPrefWidth(65);

        TableColumn<CasteRow, Boolean> flyCol = new TableColumn<>("Flying");
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

        TableColumn<CasteRow, Float> walkSpeedCol = new TableColumn<>("Walk (m/s)");
        walkSpeedCol.setCellValueFactory(new PropertyValueFactory<>("walkSpeedMps"));
        walkSpeedCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new FormattedFloatStringConverter()));
        walkSpeedCol.setOnEditCommit(e -> e.getRowValue().setWalkSpeedMps(e.getNewValue()));
        walkSpeedCol.setPrefWidth(80);

        TableColumn<CasteRow, Boolean> aroliaCol = new TableColumn<>("Arolia");
        aroliaCol.setCellValueFactory(new PropertyValueFactory<>("hasAroliaAdhesion"));
        aroliaCol.setCellFactory(col -> new TableCell<CasteRow, Boolean>() {
            private final CheckBox cb = new CheckBox();
            {
                cb.setOnAction(e -> {
                    CasteRow row = getTableView().getItems().get(getIndex());
                    if (row != null) {
                        row.setHasAroliaAdhesion(cb.isSelected());
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
        aroliaCol.setPrefWidth(60);

        TableColumn<CasteRow, Float> flySpeedCol = new TableColumn<>("Fly (m/s)");
        flySpeedCol.setCellValueFactory(new PropertyValueFactory<>("flySpeedMps"));
        flySpeedCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new FormattedFloatStringConverter()));
        flySpeedCol.setOnEditCommit(e -> e.getRowValue().setFlySpeedMps(e.getNewValue()));
        flySpeedCol.setPrefWidth(75);

        TableColumn<CasteRow, Float> hzCol = new TableColumn<>("Wings (Hz)");
        hzCol.setCellValueFactory(new PropertyValueFactory<>("wingbeatFrequencyHz"));
        hzCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new FormattedFloatStringConverter()));
        hzCol.setOnEditCommit(e -> e.getRowValue().setWingbeatFrequencyHz(e.getNewValue()));
        hzCol.setPrefWidth(80);

        TableColumn<CasteRow, Boolean> hoverCol = new TableColumn<>("Hover");
        hoverCol.setCellValueFactory(new PropertyValueFactory<>("hasHoveringCapability"));
        hoverCol.setCellFactory(col -> new TableCell<CasteRow, Boolean>() {
            private final CheckBox cb = new CheckBox();
            {
                cb.setOnAction(e -> {
                    CasteRow row = getTableView().getItems().get(getIndex());
                    if (row != null) {
                        row.setHasHoveringCapability(cb.isSelected());
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
        hoverCol.setPrefWidth(60);

        TableColumn<CasteRow, Float> biteCol = new TableColumn<>("Bite (MPa)");
        biteCol.setCellValueFactory(new PropertyValueFactory<>("bitingForceMpa"));
        biteCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new FormattedFloatStringConverter()));
        biteCol.setOnEditCommit(e -> e.getRowValue().setBitingForceMpa(e.getNewValue()));
        biteCol.setPrefWidth(85);

        TableColumn<CasteRow, Float> loadCol = new TableColumn<>("Payload (g/g)");
        loadCol.setCellValueFactory(new PropertyValueFactory<>("maxPayloadRatio"));
        loadCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new FormattedFloatStringConverter()));
        loadCol.setOnEditCommit(e -> e.getRowValue().setMaxPayloadRatio(e.getNewValue()));
        loadCol.setPrefWidth(90);

        TableColumn<CasteRow, Float> ratioCol = new TableColumn<>("Target Ratio");
        ratioCol.setCellValueFactory(new PropertyValueFactory<>("targetRatio"));
        ratioCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new FormattedFloatStringConverter()));
        ratioCol.setOnEditCommit(e -> e.getRowValue().setTargetRatio(e.getNewValue()));
        ratioCol.setPrefWidth(75);

        TableColumn<CasteRow, String> archCol = new TableColumn<>("Decision Model");
        archCol.setCellValueFactory(new PropertyValueFactory<>("decisionArch"));
        archCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
        archCol.setOnEditCommit(e -> e.getRowValue().setDecisionArch(e.getNewValue()));
        archCol.setPrefWidth(110);

        TableColumn<CasteRow, Float> forageCol = new TableColumn<>("Foraging Weight");
        forageCol.setCellValueFactory(new PropertyValueFactory<>("foragingWeight"));
        forageCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new FormattedFloatStringConverter()));
        forageCol.setOnEditCommit(e -> e.getRowValue().setForagingWeight(e.getNewValue()));
        forageCol.setPrefWidth(85);

        TableColumn<CasteRow, Float> defCol = new TableColumn<>("Defense Weight");
        defCol.setCellValueFactory(new PropertyValueFactory<>("defenseWeight"));
        defCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new FormattedFloatStringConverter()));
        defCol.setOnEditCommit(e -> e.getRowValue().setDefenseWeight(e.getNewValue()));
        defCol.setPrefWidth(85);

        TableColumn<CasteRow, Float> excCol = new TableColumn<>("Excavation Weight");
        excCol.setCellValueFactory(new PropertyValueFactory<>("excavationWeight"));
        excCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new FormattedFloatStringConverter()));
        excCol.setOnEditCommit(e -> e.getRowValue().setExcavationWeight(e.getNewValue()));
        excCol.setPrefWidth(95);

        TableColumn<CasteRow, Float> nurseCol = new TableColumn<>("Nursing Weight");
        nurseCol.setCellValueFactory(new PropertyValueFactory<>("nursingWeight"));
        nurseCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new FormattedFloatStringConverter()));
        nurseCol.setOnEditCommit(e -> e.getRowValue().setNursingWeight(e.getNewValue()));
        nurseCol.setPrefWidth(75);

        TableColumn<CasteRow, String> venomTypeCol = new TableColumn<>("Weapons / Venom");
        venomTypeCol.setCellValueFactory(new PropertyValueFactory<>("venomType"));
        venomTypeCol.setCellFactory(col -> new TableCell<CasteRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(SpeciesUIFormatting.getVenomTitle(item));
                }
            }
        });
        venomTypeCol.setPrefWidth(140);

        TableColumn<CasteRow, Float> venomToxCol = new TableColumn<>("Venom Toxicity");
        venomToxCol.setCellValueFactory(new PropertyValueFactory<>("venomToxicity"));
        venomToxCol.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn(new FormattedFloatStringConverter()));
        venomToxCol.setOnEditCommit(e -> e.getRowValue().setVenomToxicity(e.getNewValue()));
        venomToxCol.setPrefWidth(90);

        TableColumn<CasteRow, Boolean> autothysisCol = new TableColumn<>("Autothysis");
        autothysisCol.setCellValueFactory(new PropertyValueFactory<>("hasAutothysis"));
        autothysisCol.setCellFactory(col -> new TableCell<CasteRow, Boolean>() {
            private final CheckBox cb = new CheckBox();
            {
                cb.setOnAction(e -> {
                    CasteRow row = getTableView().getItems().get(getIndex());
                    if (row != null) {
                        row.setHasAutothysis(cb.isSelected());
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
        autothysisCol.setPrefWidth(75);

        casteTable.getColumns().addAll(
            nameCol, bodyCol, headCol, tunnelCol, healthCol, dmgCol, armorCol, lifeCol,
            walkSpeedCol, aroliaCol, flyCol, flySpeedCol, hzCol, hoverCol, biteCol, loadCol,
            archCol, ratioCol, forageCol, defCol, excCol, nurseCol,
            venomTypeCol, venomToxCol, autothysisCol
        );

        return casteTable;
    }
}
