/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.species;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.swarmforge.client.ui.GlossaryDialog;
import org.swarmforge.client.util.I18nManager;

/**
 * Common UI building and formatting helper for Species Editor tabs.
 */
public final class SpeciesUIHelper {

    private SpeciesUIHelper() {
    }

    public static GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(15));
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setMinWidth(240);
        c1.setPrefWidth(260);
        c1.setHgrow(Priority.NEVER);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);
        return grid;
    }

    public static ScrollPane wrapScroll(Node content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return scroll;
    }

    public static Label createTooltipLabel(String text, String tooltipText) {
        return createTooltipLabel(text, tooltipText, (Node) null, null);
    }

    public static Label createTooltipLabel(String text, String tooltipText, Node targetControl) {
        return createTooltipLabel(text, tooltipText, targetControl, null);
    }

    public static Label createTooltipLabel(String text, String tooltipText, String glossaryTerm) {
        return createTooltipLabel(text, tooltipText, null, glossaryTerm);
    }

    public static Label createTooltipLabel(String text, String tooltipText, Node targetControl, String glossaryTerm) {
        Label l = new Label();
        String labelKey = lookupKeyForLabel(text);
        l.textProperty().bind(I18nManager.getInstance().createStringBinding(labelKey));
        l.setStyle("-fx-font-weight: bold;");
        if (tooltipText != null && !tooltipText.isEmpty()) {
            Tooltip tt = new Tooltip();
            tt.textProperty().bind(I18nManager.getInstance().createStringBinding(tooltipText));
            tt.setStyle("-fx-font-size: 12px; -fx-max-width: 380px; -fx-wrap-text: true;");
            l.setTooltip(tt);
            if (targetControl != null) {
                if (targetControl instanceof Control ctrl) {
                    ctrl.setTooltip(tt);
                } else {
                    Tooltip.install(targetControl, tt);
                }
            }
        }
        if (glossaryTerm != null && !glossaryTerm.isEmpty()) {
            l.getStyleClass().add("glossary-link");
            l.setCursor(javafx.scene.Cursor.HAND);
            l.setOnMouseClicked(e -> GlossaryDialog.show(glossaryTerm));
        } else {
            l.getStyleClass().add("bold-label");
        }
        return l;
    }

    public static String lookupKeyForLabel(String textOrKey) {
        if (textOrKey == null) return "";
        if (I18nManager.getInstance().containsKey(textOrKey)) return textOrKey;
        return switch (textOrKey.trim()) {
            case "Common Name:" -> "species.taxonomy.common_name";
            case "Scientific Name (Binomial):" -> "species.taxonomy.scientific_name";
            case "Taxonomic Genus:" -> "species.taxonomy.genus";
            case "Taxonomic Order / Family:" -> "species.taxonomy.group";
            case "Ecological Role / Category:" -> "species.taxonomy.category";
            case "Description & Ecological Notes:" -> "species.taxonomy.description";

            case "Gynic Structure (Queen Mode):", "Queen Reproduction System:" -> "species.queens.mode";
            case "Founding Queens Count (ind):", "Number of Queens:" -> "species.queens.count";
            case "Queen Lifespan (days):", "Queen Lifespan (Days):" -> "species.queens.lifespan";
            case "Queen Egg Laying Rate (eggs/day):", "Queen Oviposition Rate (eggs/day):" -> "species.queens.egg_rate";
            case "Reproductive King (Isoptera):" -> "species.queens.has_king";
            case "King Lifespan (days):" -> "species.queens.king_lifespan";
            case "Nuptial Flight / Swarming Mode:", "Nuptial Flight & Swarming Mode:" -> "species.queens.nuptial_flight";
            case "Mature Colony Population (ind):" -> "species.diet.colony_size";
            case "Supercolonies (Unicoloniality):" -> "species.diet.supercolonies";

            case "Egg Stage Duration (days):" -> "species.stages.egg_duration";
            case "Larval Stage Duration (days):" -> "species.stages.larva_duration";
            case "Larval Diet Requirement:" -> "species.stages.larva_diet";
            case "Pupal Stage / Cocoon Duration (days):" -> "species.stages.pupa_duration";
            case "Minor Worker Protein Threshold (%):" -> "species.stages.protein_minor";
            case "Major Worker Protein Threshold (%):" -> "species.stages.protein_major";
            case "Soldier Protein Threshold (%):" -> "species.stages.protein_soldier";
            case "Royal Food Protein Threshold (%):" -> "species.stages.protein_queen";
            case "Queen Pheromonal Inhibition:" -> "species.stages.phero_inhib";
            case "Male Determination (Arrhenotoky):" -> "species.stages.haplodiploidy";
            case "Pathogen Immune Resistance (%):" -> "species.stages.pathogen_resistance";
            case "Social Grooming Efficacy (%):" -> "species.stages.grooming_efficacy";

            case "Primary Diet Source:" -> "species.diet.primary";
            case "Secondary Diet Source:" -> "species.diet.secondary";
            case "Metabolic Consumption (g/ind/day):" -> "species.diet.consumption";
            case "Water Requirement (mL/ind/day):" -> "species.diet.water";
            case "Worker Lifespan (days):" -> "species.diet.worker_lifespan";
            case "Locomotion Speed (m/s):" -> "species.diet.worker_speed";
            case "Visual Detection Distance (cm):" -> "species.diet.view_distance";
            case "Worker Flight Capability:" -> "species.diet.worker_fly";
            case "Global Metabolic Factor (0.1-5.0):" -> "species.diet.metabolism";
            case "Physio-Muscular Strength (N):" -> "species.diet.strength";

            case "Specific Nest Type (NestType):" -> "species.nest.type";
            case "Optimal Temperature (°C):" -> "species.nest.opt_temp";
            case "Minimum Temperature (°C):" -> "species.nest.min_temp";
            case "Maximum Temperature (°C):" -> "species.nest.max_temp";
            case "Aggression Level:" -> "species.nest.aggression";
            case "Territoriality:" -> "species.nest.territoriality";
            case "Synchronized Nest Generation:" -> "species.nest.sync_gen";

            default -> textOrKey;
        };
    }

    public static int parseInt(String s, int fallback) {
        if (s == null || s.isBlank()) return fallback;
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return fallback; }
    }

    public static float parseFloat(String s, float fallback) {
        if (s == null || s.isBlank()) return fallback;
        try { return Float.parseFloat(s.trim()); } catch (Exception e) { return fallback; }
    }
}
