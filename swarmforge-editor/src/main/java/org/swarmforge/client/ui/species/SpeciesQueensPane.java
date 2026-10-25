/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.species;

import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.swarmforge.client.ui.ComboBoxTooltipHelper;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.core.species.DefaultSpecies;

/**
 * UI Pane for Species Gynic Structure, Queen reproduction dynamics, founding parameters and mating flight.
 */
public class SpeciesQueensPane {

    public final ComboBox<String> queenModeCombo = new ComboBox<>(FXCollections.observableArrayList("GAMERGATES", "MONOGYNE", "POLYGYNE"));
    public final Spinner<Integer> queenCountSpinner = new Spinner<>(1, 500, 1);
    public final TextField queenLifespanField = new TextField("25000");
    public final TextField queenEggRateField = new TextField("25.0");
    public final CheckBox hasKingCheckBox = new CheckBox("Presence of Reproductive King (Termites)");
    public final TextField kingLifespanField = new TextField("15000");
    public final ComboBox<String> nuptialFlightCombo = new ComboBox<>(FXCollections.observableArrayList("AERIAL_SWARM", "BUDDING", "IN_NEST", "SWARM_DIVISION"));
    public final TextField colonySizeField = new TextField("15000");
    public final CheckBox megaColonyCheckBox = new CheckBox("Forms Supercolonies (Unicoloniality / Multi-nest Network)");

    private final ScrollPane container;

    public SpeciesQueensPane(Runnable onFieldEdited, Runnable onValidate) {
        GridPane grid = SpeciesUIHelper.createGrid();

        ComboBoxTooltipHelper.setupDescriptiveComboBox(queenModeCombo,
            val -> switch (val != null ? val : "") {
                case "MONOGYNE" -> I18nManager.getInstance().get("species.queen_mode.monogyne");
                case "POLYGYNE" -> I18nManager.getInstance().get("species.queen_mode.polygyne");
                case "GAMERGATES" -> I18nManager.getInstance().get("species.queen_mode.gamergates");
                default -> "";
            },
            val -> switch (val != null ? val : "") {
                case "MONOGYNE" -> I18nManager.getInstance().get("species.queen_mode.monogyne.desc");
                case "POLYGYNE" -> I18nManager.getInstance().get("species.queen_mode.polygyne.desc");
                case "GAMERGATES" -> I18nManager.getInstance().get("species.queen_mode.gamergates.desc");
                default -> "";
            }
        );
        queenModeCombo.getSelectionModel().select("MONOGYNE");
        queenModeCombo.setOnAction(e -> {
            if ("MONOGYNE".equals(queenModeCombo.getValue())) {
                queenCountSpinner.getValueFactory().setValue(1);
            }
            if (onValidate != null) onValidate.run();
        });

        queenCountSpinner.valueProperty().addListener((obs, oldV, newV) -> {
            if ("MONOGYNE".equals(queenModeCombo.getValue()) && newV > 1) {
                queenCountSpinner.getValueFactory().setValue(1);
            }
            if (onValidate != null) onValidate.run();
        });

        hasKingCheckBox.setOnAction(e -> {
            if (onValidate != null) onValidate.run();
        });

        ComboBoxTooltipHelper.setupDescriptiveComboBox(nuptialFlightCombo,
            val -> switch (val != null ? val : "") {
                case "AERIAL_SWARM" -> I18nManager.getInstance().get("species.nuptial.aerial_swarm");
                case "BUDDING" -> I18nManager.getInstance().get("species.nuptial.budding");
                case "IN_NEST" -> I18nManager.getInstance().get("species.nuptial.in_nest");
                case "SWARM_DIVISION" -> I18nManager.getInstance().get("species.nuptial.swarm_division");
                default -> "";
            },
            val -> switch (val != null ? val : "") {
                case "AERIAL_SWARM" -> I18nManager.getInstance().get("species.nuptial.aerial_swarm.desc");
                case "BUDDING" -> I18nManager.getInstance().get("species.nuptial.budding.desc");
                case "IN_NEST" -> I18nManager.getInstance().get("species.nuptial.in_nest.desc");
                case "SWARM_DIVISION" -> I18nManager.getInstance().get("species.nuptial.swarm_division.desc");
                default -> "";
            }
        );
        nuptialFlightCombo.getSelectionModel().select("AERIAL_SWARM");

        grid.addRow(0, SpeciesUIHelper.createTooltipLabel("Gynic Structure (Queen Mode):", "Reproductive queen organization mode: Monogyne (1 queen), Polygyne (multiple queens), or Gamergates (reproductive workers).", queenModeCombo, "Monogyne"), queenModeCombo);
        grid.addRow(1, SpeciesUIHelper.createTooltipLabel("Founding Queens Count (ind):", "Initial or maximum fertile reproductive queens residing in colony.", queenCountSpinner), queenCountSpinner);
        grid.addRow(2, SpeciesUIHelper.createTooltipLabel("Queen Lifespan (days):", "Maximum queen lifespan before natural senescence.", queenLifespanField), queenLifespanField);
        grid.addRow(3, SpeciesUIHelper.createTooltipLabel("Queen Egg Laying Rate (eggs/day):", "Daily eggs laid per queen under optimal conditions.", queenEggRateField), queenEggRateField);
        grid.addRow(4, SpeciesUIHelper.createTooltipLabel("Reproductive King (Isoptera):", "Presence of a permanent reproductive male (king) living alongside the queen (termites).", hasKingCheckBox, "King"), hasKingCheckBox);
        grid.addRow(5, SpeciesUIHelper.createTooltipLabel("King Lifespan (days):", "Reproductive king lifespan in isopteran species.", kingLifespanField), kingLifespanField);
        grid.addRow(6, SpeciesUIHelper.createTooltipLabel("Nuptial Flight / Swarming Mode:", "Dispersal and mating strategy: Aerial swarm, swarm division, budding, or in-nest mating.", nuptialFlightCombo, "Nuptial"), nuptialFlightCombo);
        grid.addRow(7, SpeciesUIHelper.createTooltipLabel("Mature Colony Population (ind):", "Average population size of a mature colony at ecological equilibrium.", colonySizeField), colonySizeField);
        grid.addRow(8, SpeciesUIHelper.createTooltipLabel("Supercolonies (Unicoloniality):", "Ability to form interconnected nest networks without intraspecific aggression.", megaColonyCheckBox), megaColonyCheckBox);

        container = SpeciesUIHelper.wrapScroll(grid);

        if (onFieldEdited != null) {
            queenModeCombo.valueProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            queenCountSpinner.valueProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            queenLifespanField.textProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            queenEggRateField.textProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            hasKingCheckBox.selectedProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            kingLifespanField.textProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            nuptialFlightCombo.valueProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            colonySizeField.textProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            megaColonyCheckBox.selectedProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
        }
    }

    public ScrollPane getContainer() {
        return container;
    }

    public void populateFromSpecies(DefaultSpecies s) {
        if (s == null) return;
        if (s.getQueenCountMode() != null) queenModeCombo.getSelectionModel().select(s.getQueenCountMode());
        queenCountSpinner.getValueFactory().setValue(s.getQueenCount());
        queenLifespanField.setText(String.valueOf(s.getQueenLifespan()));
        queenEggRateField.setText(String.valueOf(s.getQueenEggLayingRate()));
        hasKingCheckBox.setSelected(s.isHasKing());
        kingLifespanField.setText(String.valueOf(s.getKingLifespan()));
        if (s.getNuptialFlightType() != null) nuptialFlightCombo.getSelectionModel().select(s.getNuptialFlightType());
        colonySizeField.setText(String.valueOf(s.getTypicalColonySize()));
        megaColonyCheckBox.setSelected(s.formsMegaColonies());
    }

    public void applyToSpecies(DefaultSpecies s) {
        if (s == null) return;
        s.setQueenCountMode(queenModeCombo.getValue());
        s.setQueenCount(queenCountSpinner.getValue());
        s.setQueenLifespan(SpeciesUIHelper.parseInt(queenLifespanField.getText(), 25000));
        s.setQueenEggLayingRate(SpeciesUIHelper.parseFloat(queenEggRateField.getText(), 15.0f));
        s.setHasKing(hasKingCheckBox.isSelected());
        s.setKingLifespan(SpeciesUIHelper.parseInt(kingLifespanField.getText(), 15000));
        s.setNuptialFlightType(nuptialFlightCombo.getValue());
        s.setTypicalColonySize(SpeciesUIHelper.parseInt(colonySizeField.getText(), 1000));
        s.setFormsMegaColonies(megaColonyCheckBox.isSelected());
    }
}
