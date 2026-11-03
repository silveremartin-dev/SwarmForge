/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.nest;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.swarmforge.client.util.I18nManager;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Chamber Distribution Card for configuring queen, brood, food storage, entrance,
 * waste dump, and fungus cultivation chambers.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class NestChamberDistributionPane extends VBox {

    private final I18nManager i18n = I18nManager.getInstance();
    private final Map<String, Spinner<Integer>> chamberSpinners = new LinkedHashMap<>();
    private final Label lblTotalChambersValue;
    private final Runnable onParameterChanged;

    public NestChamberDistributionPane(Runnable onParameterChanged) {
        super(6);
        this.onParameterChanged = onParameterChanged;
        getStyleClass().add("card-pane");

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
        grid.setHgap(8);
        grid.setVgap(7);
        grid.setPadding(new Insets(6));

        String[][] defs = {
            {"nest.chambers.queen", "1", "\uD83D\uDC51 Queen Chamber", "Number of royal chambers reserved for the queen and egg laying."},
            {"nest.chambers.brood", "3", "\uD83E\uDD5A Brood Chambers", "Nursery chambers for eggs, larvae, and pupae."},
            {"nest.chambers.food", "4", "\uD83C\uDF6F Food Storage", "Storage chambers and pots for food (honey, pollen, seeds, prey)."},
            {"nest.chambers.entrance", "2", "\uD83D\uDEAA Entrances", "Number of exits and entrance holes to the outside."},
            {"nest.chambers.waste", "1", "\uD83D\uDDD1 Waste Dumps", "Waste dumps for refuse, corpses, and debris."},
            {"nest.chambers.fungus", "0", "\uD83C\uDF44 Fungus Gardens", "Cultivation chambers for symbiotic fungus (Atta)."}
        };

        int row = 0;
        for (String[] d : defs) {
            Spinner<Integer> s = new Spinner<>(0, 25, Integer.parseInt(d[1]));
            s.setPrefWidth(70);
            s.setEditable(true);
            s.setTooltip(new Tooltip(d[3]));
            s.valueProperty().addListener((o, a, b) -> {
                updateTotalChambers();
                if (this.onParameterChanged != null) {
                    this.onParameterChanged.run();
                }
            });
            chamberSpinners.put(d[2], s);

            Label chLbl = new Label();
            chLbl.textProperty().bind(i18n.createStringBinding(d[0]));
            chLbl.setTooltip(new Tooltip(d[3]));
            grid.add(chLbl, 0, row);
            grid.add(s, 1, row++);
        }

        updateTotalChambers();
        getChildren().addAll(cdTitle, new Separator(), totalChambersBox, grid);
    }

    public void updateTotalChambers() {
        int total = chamberSpinners.values().stream().mapToInt(Spinner::getValue).sum();
        if (lblTotalChambersValue != null) {
            lblTotalChambersValue.setText(String.valueOf(total));
        }
    }

    public int getTotalChambers() {
        return chamberSpinners.values().stream().mapToInt(Spinner::getValue).sum();
    }

    public int getSp(String key) {
        Spinner<Integer> s = chamberSpinners.get(key);
        return s != null ? s.getValue() : 0;
    }

    public void setSp(String key, int val) {
        Spinner<Integer> s = chamberSpinners.get(key);
        if (s != null) {
            s.getValueFactory().setValue(val);
        }
    }

    public Map<String, Integer> getChamberDistribution() {
        Map<String, Integer> dist = new LinkedHashMap<>();
        chamberSpinners.forEach((k, v) -> dist.put(k, v.getValue()));
        return dist;
    }

    public void setChamberDistribution(Map<?, ?> distMap) {
        if (distMap == null) return;
        distMap.forEach((k, v) -> setSp(String.valueOf(k), ((Number) v).intValue()));
        updateTotalChambers();
    }

    public Map<String, Spinner<Integer>> getChamberSpinners() {
        return chamberSpinners;
    }
}
