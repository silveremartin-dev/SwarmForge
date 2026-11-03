/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.nest;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.swarmforge.client.ui.NestGeneratorPane;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Serializer and Deserializer for nest configurations, preset mappings, and environment evaluation parameters.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public final class NestConfigSerializer {

    private NestConfigSerializer() {}

    private static double num(Map<String, Object> m, String k) {
        Object val = m.get(k);
        return val instanceof Number ? ((Number) val).doubleValue() : 0.0;
    }

    public static Map<String, Object> exportConfiguration(
            NestGeneratorPane pane,
            ComboBox<String> presetsCombo,
            NestPlacementViabilityPane viabilityPane,
            NestChamberDistributionPane chamberPane
    ) {
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("presetName", presetsCombo != null && presetsCombo.getValue() != null ? presetsCombo.getValue() : "Custom");
        ComboBox<String> spCombo = pane.getSpeciesModelCombo();
        c.put("referenceSpecies", spCombo != null && spCombo.getValue() != null ? spCombo.getValue() : "Custom");
        c.put("seed", pane.getSeed());
        ComboBox<String> catSel = pane.getCategorySelect();
        c.put("taxonCategory", catSel != null && catSel.getValue() != null ? catSel.getValue() : "Ants (Formicidae)");
        ComboBox<String> gSel = pane.getGenusSelect();
        c.put("genus", gSel != null && gSel.getValue() != null ? gSel.getValue() : "");
        c.put("architecture", pane.getArchitecture());
        c.put("material", pane.getMaterial());
        c.put("workerSizeMm", pane.getWorkerSizeSlider().getValue());
        c.put("depth", (int) pane.getDepthSlider().getValue());
        c.put("chamberCount", (int) pane.getChamberCount());
        c.put("tunnelWidth", (int) pane.getTunnelWidthSlider().getValue());
        c.put("branching", (int) pane.getBranchingSlider().getValue());

        ComboBox<String> stgCombo = pane.getNestStageCombo();
        c.put("nestStageIndex", stgCombo != null ? stgCombo.getSelectionModel().getSelectedIndex() : 2);

        c.put("envHeight", (viabilityPane != null && viabilityPane.getEvalHeightSlider() != null) ? viabilityPane.getEvalHeightSlider().getValue() : 0.0);
        c.put("envOrientation", (viabilityPane != null && viabilityPane.getEvalOrientationCombo() != null && viabilityPane.getEvalOrientationCombo().getValue() != null) ? viabilityPane.getEvalOrientationCombo().getValue() : "East (Morning Light)");
        c.put("envTemp", (viabilityPane != null && viabilityPane.getEvalTempSlider() != null) ? viabilityPane.getEvalTempSlider().getValue() : 22.0);
        c.put("envMoisture", (viabilityPane != null && viabilityPane.getEvalMoistureSlider() != null) ? viabilityPane.getEvalMoistureSlider().getValue() : 45.0);
        c.put("envForaging", (viabilityPane != null && viabilityPane.getEvalForagingSlider() != null) ? viabilityPane.getEvalForagingSlider().getValue() : 35.0);
        c.put("envCompaction", (viabilityPane != null && viabilityPane.getEvalCompactionSlider() != null) ? viabilityPane.getEvalCompactionSlider().getValue() : 65.0);

        if (chamberPane != null) {
            c.put("chamberDistribution", chamberPane.getChamberDistribution());
        }
        return c;
    }

    public static void applyConfiguration(
            NestGeneratorPane pane,
            ComboBox<String> presetsCombo,
            NestPlacementViabilityPane viabilityPane,
            NestChamberDistributionPane chamberPane,
            Map<String, Object> cfg
    ) {
        if (cfg == null) return;
        pane.setUpdatingSpeciesCombo(true);
        try {
            if (cfg.containsKey("seed")) {
                try {
                    pane.setSeed(((Number) cfg.get("seed")).longValue());
                } catch (Exception ignored) {}
            }
            if (cfg.containsKey("presetName") && presetsCombo != null) {
                String pName = String.valueOf(cfg.get("presetName"));
                if (presetsCombo.getItems().contains(pName)) {
                    presetsCombo.setValue(pName);
                }
            }
            ComboBox<String> catSel = pane.getCategorySelect();
            if (cfg.containsKey("taxonCategory") && catSel != null) {
                pane.setCategorySelectValue(String.valueOf(cfg.get("taxonCategory")));
                pane.populateGenusCombo();
            }
            ComboBox<String> gSel = pane.getGenusSelect();
            if (cfg.containsKey("genus") && gSel != null) {
                String savedGenus = String.valueOf(cfg.get("genus"));
                if (!savedGenus.isEmpty()) {
                    if (gSel.getItems().contains(savedGenus)) {
                        gSel.setValue(savedGenus);
                    } else {
                        for (String item : gSel.getItems()) {
                            if (item.toLowerCase().startsWith(savedGenus.toLowerCase()) || item.toLowerCase().contains(savedGenus.toLowerCase())) {
                                gSel.setValue(item);
                                break;
                            }
                        }
                    }
                }
            }
            if (cfg.containsKey("architecture")) pane.setArchSelectValue(String.valueOf(cfg.get("architecture")));
            if (cfg.containsKey("material")) pane.setMatSelectValue(String.valueOf(cfg.get("material")));
            if (cfg.containsKey("workerSizeMm") && pane.getWorkerSizeSlider() != null) pane.getWorkerSizeSlider().setValue(num(cfg, "workerSizeMm"));
            if (cfg.containsKey("depth") && pane.getDepthSlider() != null) pane.getDepthSlider().setValue(num(cfg, "depth"));
            if (cfg.containsKey("tunnelWidth") && pane.getTunnelWidthSlider() != null) pane.getTunnelWidthSlider().setValue(num(cfg, "tunnelWidth"));
            if (cfg.containsKey("branching") && pane.getBranchingSlider() != null) pane.getBranchingSlider().setValue(num(cfg, "branching"));

            ComboBox<String> stgCombo = pane.getNestStageCombo();
            if (cfg.containsKey("nestStageIndex") && stgCombo != null) {
                int idx = ((Number) cfg.get("nestStageIndex")).intValue();
                if (idx >= 0 && idx < stgCombo.getItems().size()) stgCombo.getSelectionModel().select(idx);
            }

            if (cfg.containsKey("chamberDistribution") && cfg.get("chamberDistribution") instanceof Map<?, ?> distMap && chamberPane != null) {
                chamberPane.setChamberDistribution(distMap);
            }

            if (viabilityPane != null) {
                if (cfg.containsKey("envHeight") && viabilityPane.getEvalHeightSlider() != null) viabilityPane.getEvalHeightSlider().setValue(num(cfg, "envHeight"));
                if (cfg.containsKey("envOrientation") && viabilityPane.getEvalOrientationCombo() != null) viabilityPane.getEvalOrientationCombo().setValue(String.valueOf(cfg.get("envOrientation")));
                if (cfg.containsKey("envTemp") && viabilityPane.getEvalTempSlider() != null) viabilityPane.getEvalTempSlider().setValue(num(cfg, "envTemp"));
                if (cfg.containsKey("envMoisture") && viabilityPane.getEvalMoistureSlider() != null) viabilityPane.getEvalMoistureSlider().setValue(num(cfg, "envMoisture"));
                if (cfg.containsKey("envForaging") && viabilityPane.getEvalForagingSlider() != null) viabilityPane.getEvalForagingSlider().setValue(num(cfg, "envForaging"));
                if (cfg.containsKey("envCompaction") && viabilityPane.getEvalCompactionSlider() != null) viabilityPane.getEvalCompactionSlider().setValue(num(cfg, "envCompaction"));
                viabilityPane.updatePlacementViabilityScore();
            }

            // Resolve and select matching Reference Species for this preset
            pane.populateSpeciesModelCombo();
            String matchedSpecies = null;
            ComboBox<String> spCombo = pane.getSpeciesModelCombo();
            if (cfg.containsKey("referenceSpecies")) {
                matchedSpecies = String.valueOf(cfg.get("referenceSpecies"));
            }
            if (spCombo != null) {
                if (matchedSpecies != null && !matchedSpecies.isBlank()) {
                    for (String item : spCombo.getItems()) {
                        if (item.toLowerCase().contains(matchedSpecies.toLowerCase())) {
                            spCombo.setValue(item);
                            matchedSpecies = item;
                            break;
                        }
                    }
                } else {
                    String currentArch = pane.getArchitecture();
                    String defaultMatch = switch (currentArch) {
                        case "WAX_COMB_HEXAGONAL", "WOODEN_BEEHIVE" -> "Apis";
                        case "WAX_POTS_CLUSTER" -> "Bombus";
                        case "PAPER_PEDUNCULATE" -> "Vespa";
                        case "CATHEDRAL_MOUND" -> "Macrotermes";
                        case "BIVOUAC_LIVING_NEST" -> "Eciton";
                        case "SUBTERRANEAN_FUNGI_VAULT" -> "Atta";
                        case "CARTON_NEST" -> "Crematogaster";
                        case "BAMBOO_STEM_NEST" -> "Temnothorax";
                        case "ARBOREAL_SILK_LEAF" -> "Oecophylla";
                        case "HOLLOW_TRUNK_NEST" -> "Camponotus";
                        case "SURFACE_MOUND" -> "Formica";
                        default -> "Lasius";
                    };
                    for (String item : spCombo.getItems()) {
                        if (item.toLowerCase().contains(defaultMatch.toLowerCase())) {
                            spCombo.setValue(item);
                            matchedSpecies = item;
                            break;
                        }
                    }
                    if (matchedSpecies == null && !spCombo.getItems().isEmpty()) {
                        spCombo.getSelectionModel().selectFirst();
                        matchedSpecies = spCombo.getValue();
                    }
                }
            }
            Label spStatusLbl = pane.getSpeciesStatusLabel();
            if (spStatusLbl != null && matchedSpecies != null) {
                spStatusLbl.setText("Nest preset loaded (Reference: " + matchedSpecies.replaceAll("^[^a-zA-Z0-9(]+\\s*", "") + ")");
            }

            pane.regen();
            pane.repaint();
        } finally {
            pane.setUpdatingSpeciesCombo(false);
            pane.setDirty(false);
        }
    }
}
