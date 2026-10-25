/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.species;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.core.species.SpeciesCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates biological parameter consistency for Species configuration in the editor
 * and updates the warning banner with actionable fix buttons.
 */
public final class SpeciesValidator {

    private SpeciesValidator() {
    }

    public static List<String> validateAndRender(
            SpeciesTaxonomyPane taxonomyPane,
            SpeciesQueensPane queensPane,
            SpeciesNestPreferencesPane nestPane,
            ObservableList<CasteRow> casteRows,
            VBox warningBannerBox,
            Runnable onRevalidate
    ) {
        List<String> warnings = new ArrayList<>();
        if (warningBannerBox == null) return warnings;

        // 1. Monogyne vs Queen count
        String qMode = queensPane.queenModeCombo.getValue() != null ? queensPane.queenModeCombo.getValue() : "MONOGYNE";
        int qCount = queensPane.queenCountSpinner.getValue() != null ? queensPane.queenCountSpinner.getValue() : 1;
        if ("MONOGYNE".equals(qMode) && qCount > 1) {
            warnings.add("Monogyne Gynic Structure: only 1 reproductive queen is permitted per colony (current value: " + qCount + ").");
        }

        // 2. Has King vs King Caste in caste table
        boolean hasKing = queensPane.hasKingCheckBox.isSelected();
        if (hasKing) {
            boolean hasKingCaste = casteRows.stream().anyMatch(r ->
                r.getName().equalsIgnoreCase("Roi") ||
                r.getName().equalsIgnoreCase("King") ||
                r.getName().toLowerCase().contains("roi") ||
                r.getName().toLowerCase().contains("king")
            );
            if (!hasKingCaste) {
                warnings.add("Reproductive King enabled (Isoptera), but no 'King' caste is defined in the caste table.");
            }
        }

        // 3. Category non-eusocial warning
        SpeciesCategory cat = taxonomyPane.categoryCombo.getValue();
        if (cat != null && cat != SpeciesCategory.EUSOCIAL_PRIMARY) {
            warnings.add("Catégorie " + cat.label + " selected. Note: prey, predators, and commensals are configured in the environment accessory fauna.");
        }

        // 4. Temperature consistency
        try {
            float optT = SpeciesUIHelper.parseFloat(nestPane.optTempField.getText(), 24.0f);
            float minT = SpeciesUIHelper.parseFloat(nestPane.minTempField.getText(), 10.0f);
            float maxT = SpeciesUIHelper.parseFloat(nestPane.maxTempField.getText(), 38.0f);
            if (minT >= maxT) {
                warnings.add("Invalid thermal range: minimum temperature (" + minT + "°C) must be lower than maximum (" + maxT + "°C).");
            } else if (optT < minT || optT > maxT) {
                warnings.add("Optimal temperature (" + optT + "°C) outside bounds [Min: " + minT + "°C, Max: " + maxT + "°C].");
            }
        } catch (Exception ignored) {}

        // 5. Male Caste warning check for Hymenoptera
        String insectType = taxonomyPane.insectTypeCombo.getValue();
        if (insectType != null && (insectType.equalsIgnoreCase("ANT") || insectType.equalsIgnoreCase("BEE") || insectType.equalsIgnoreCase("WASP"))) {
            boolean hasMaleCaste = casteRows.stream().anyMatch(r ->
                r.getName().toLowerCase().contains("mâle") ||
                r.getName().toLowerCase().contains("male") ||
                r.getName().toLowerCase().contains("drone") ||
                r.getName().toLowerCase().contains("bourdon")
            );
            if (!hasMaleCaste) {
                warnings.add("Nuptial Flight & Mating: No 'Reproductive Male' caste (Alate / Drone) is defined in the caste table.");
            }
        }

        // Render to UI warning banner
        warningBannerBox.getChildren().clear();
        if (warnings.isEmpty()) {
            warningBannerBox.setVisible(false);
            warningBannerBox.setManaged(false);
        } else {
            warningBannerBox.setVisible(true);
            warningBannerBox.setManaged(true);

            Label titleLabel = new Label("⚠️ Parameter Consistency Warnings:");
            titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #f59e0b; -fx-font-size: 13px;");
            warningBannerBox.getChildren().add(titleLabel);

            for (String w : warnings) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);

                Label wLbl = new Label("• " + w);
                wLbl.setStyle("-fx-text-fill: #fbbf24; -fx-font-size: 12px;");
                wLbl.setWrapText(true);

                if (w.contains("King") || w.contains("Roi")) {
                    Button btnAddKing = new Button("⚡ Add 'King' Caste (Isoptera)", new FontIcon(Feather.PLUS_CIRCLE));
                    btnAddKing.setStyle("-fx-background-color: #d97706; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
                    btnAddKing.setOnAction(e -> {
                        CasteRow roiRow = new CasteRow("Reproductive King", 8.0, 2.2, 15000, 150, 5, false);
                        casteRows.add(roiRow);
                        if (onRevalidate != null) onRevalidate.run();
                    });
                    row.getChildren().addAll(wLbl, btnAddKing);
                } else if (w.contains("Reproductive Male") || w.contains("Drone")) {
                    Button btnAddMale = new Button("⚡ Add 'Reproductive Male' Caste", new FontIcon(Feather.PLUS_CIRCLE));
                    btnAddMale.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
                    btnAddMale.setOnAction(e -> {
                        CasteRow maleRow = new CasteRow("Reproductive Male (Alate)", 4.5, 1.1, 500, 45, 0, true);
                        casteRows.add(maleRow);
                        if (onRevalidate != null) onRevalidate.run();
                    });
                    row.getChildren().addAll(wLbl, btnAddMale);
                } else if (w.contains("Monogyne")) {
                    Button btnFixMonogyne = new Button("⚡ Set to 1 Queen", new FontIcon(Feather.CHECK));
                    btnFixMonogyne.setStyle("-fx-background-color: #d97706; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
                    btnFixMonogyne.setOnAction(e -> {
                        queensPane.queenCountSpinner.getValueFactory().setValue(1);
                        if (onRevalidate != null) onRevalidate.run();
                    });
                    row.getChildren().addAll(wLbl, btnFixMonogyne);
                } else {
                    row.getChildren().add(wLbl);
                }

                warningBannerBox.getChildren().add(row);
            }
        }

        return warnings;
    }
}
