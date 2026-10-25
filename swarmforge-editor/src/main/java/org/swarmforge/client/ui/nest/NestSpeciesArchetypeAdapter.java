/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.nest;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import org.swarmforge.client.ui.NestGeneratorPane;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.core.species.DefaultSpecies;

/**
 * Biological Archetype and Ontogenetic Stage Adapter for the Nest Generator Studio.
 * Computes entomologically realistic nest dimensions, architecture paradigms,
 * construction materials, gallery passage clearances, and chamber distributions
 * across species taxa (Formicidae, Apidae, Vespidae, Isoptera) and developmental stages
 * (Foundation, Established, Mature, Giant).
 */
public class NestSpeciesArchetypeAdapter {

    /**
     * Development stage scaling parameters.
     */
    public record StageScaling(double populationMultiplier, double depthMultiplier, int branching) {}

    public static StageScaling getStageScaling(int stageIndex) {
        return switch (stageIndex) {
            case 0 -> new StageScaling(0.05, 0.45, 1); // FOUNDATION
            case 1 -> new StageScaling(0.25, 0.75, 2); // ESTABLISHED
            case 3 -> new StageScaling(2.50, 1.35, 4); // GIANT
            default -> new StageScaling(1.00, 1.00, 3); // MATURE
        };
    }

    public static void configureFromSpecies(NestGeneratorPane pane, DefaultSpecies species) {
        if (pane == null || species == null) return;

        pane.setUpdatingSpeciesCombo(true);
        try {
            // 1. Insect Category, Material & Architecture mapping
            String orderStr = species.getInsectType() != null ? species.getInsectType() : "ANT";
            String nestTypeStr = species.getNestType() != null ? species.getNestType() : "MATURE";
            ComboBox<String> catSelect = pane.getCategorySelect();

            if ("TERMITE".equalsIgnoreCase(orderStr)) {
                if (catSelect != null) catSelect.setValue("🐜 Termites (Isoptera)");
                pane.setArchSelectValue("Cathedral Mound");
                pane.setMatSelectValue("Stercoral Cement (Termite Feces/Mud)");
            } else if ("BEE".equalsIgnoreCase(orderStr)) {
                if (species.getCommonName() != null && species.getCommonName().toLowerCase().contains("bourdon")
                        || (species.getScientificName() != null && species.getScientificName().toLowerCase().contains("bombus"))) {
                    if (catSelect != null) catSelect.setValue("🐝 Bumblebees (Bombus)");
                    pane.setArchSelectValue("Wax Pots Cluster");
                    pane.setMatSelectValue("Propolis & Tree Resin");
                } else {
                    if (catSelect != null) catSelect.setValue("🐝 Honeybees (Apis)");
                    pane.setArchSelectValue("Hexagonal Wax Comb");
                    pane.setMatSelectValue("Beeswax (Apidae)");
                }
            } else if ("WASP".equalsIgnoreCase(orderStr)) {
                if (catSelect != null) catSelect.setValue("🐝 Wasps & Hornets (Vespidae)");
                pane.setArchSelectValue("Hanging Paper Nest");
                pane.setMatSelectValue("Wood Pulp Paper (Vespidae)");
            } else {
                // ANT (Formicidae)
                if (catSelect != null) catSelect.setValue("🐜 Ants (Formicidae)");
                String cName = species.getCommonName() != null ? species.getCommonName().toLowerCase() : "";
                if ("SUBTERRANEAN_FUNGI_VAULT".equalsIgnoreCase(nestTypeStr) || cName.contains("fungus") || cName.contains("leafcutter") || cName.contains("champignonniste") || cName.contains("coupeuse")) {
                    pane.setArchSelectValue("Subterranean Fungi Vault");
                    pane.setMatSelectValue("Earth & Clay Soil");
                } else if ("CARTON_NEST".equalsIgnoreCase(nestTypeStr) || cName.contains("carton")) {
                    pane.setArchSelectValue("Arboreal Carton Nest");
                    pane.setMatSelectValue("Carton & Wood Pulp");
                } else if ("BAMBOO_STEM_NEST".equalsIgnoreCase(nestTypeStr) || cName.contains("temnothorax") || cName.contains("gall") || cName.contains("stem") || cName.contains("tige")) {
                    pane.setArchSelectValue("Bamboo Stem & Gall");
                    pane.setMatSelectValue("Wood Pulp Paper (Vespidae)");
                } else if ("BIVOUAC_LIVING_NEST".equalsIgnoreCase(nestTypeStr) || cName.contains("army") || cName.contains("legionary") || cName.contains("légionnaire") || cName.contains("eciton")) {
                    pane.setArchSelectValue("Bivouac Living Nest");
                    pane.setMatSelectValue("Living Insect Bodies (Bivouac)");
                } else if ("ARBOREAL_SILK_LEAF".equalsIgnoreCase(nestTypeStr) || cName.contains("weaver") || cName.contains("tisserande")) {
                    pane.setArchSelectValue("Arboreal Silk Leaf");
                    pane.setMatSelectValue("Silk Weave (Oecophylla Larvae)");
                } else if ("MOUND".equalsIgnoreCase(nestTypeStr) || "SURFACE_MOUND".equalsIgnoreCase(nestTypeStr) || cName.contains("fire") || cName.contains("feu")) {
                    pane.setArchSelectValue("Surface Dome Mound");
                    pane.setMatSelectValue("Earth & Clay Soil");
                } else if ("HOLLOW_TRUNK".equalsIgnoreCase(nestTypeStr) || "HOLLOW_WOOD".equalsIgnoreCase(nestTypeStr) || cName.contains("carpenter") || cName.contains("camponotus") || cName.contains("charpentière")) {
                    pane.setArchSelectValue("Hollow Trunk & Stump");
                    pane.setMatSelectValue("Hollow Wood & Bark");
                } else {
                    pane.setArchSelectValue("Subterranean Burrow");
                    pane.setMatSelectValue("Earth & Clay Soil");
                }
            }

            // Synchronize Genus selector
            String sciName = species.getScientificName() != null ? species.getScientificName().toLowerCase() : "";
            ComboBox<String> genusSelect = pane.getGenusSelect();
            if (genusSelect != null) {
                if (sciName.contains("vespa")) genusSelect.setValue("Vespa (Hornets)");
                else if (sciName.contains("vespula")) genusSelect.setValue("Vespula (Yellowjackets)");
                else if (sciName.contains("apis")) genusSelect.setValue("Apis (Honey Bees)");
                else if (sciName.contains("bombus")) genusSelect.setValue("Bombus (Bumble Bees)");
                else if (sciName.contains("atta")) genusSelect.setValue("Atta (Leafcutter Ants)");
                else if (sciName.contains("camponotus")) genusSelect.setValue("Camponotus (Carpenter Ants)");
                else if (sciName.contains("lasius")) genusSelect.setValue("Lasius (Black Garden Ants)");
                else if (sciName.contains("pogonomyrmex")) genusSelect.setValue("Pogonomyrmex (Harvester Ants)");
                else if (sciName.contains("solenopsis")) genusSelect.setValue("Solenopsis (Fire Ants)");
                else if (sciName.contains("crematogaster")) genusSelect.setValue("Crematogaster (Acrobat Ants)");
                else if (sciName.contains("temnothorax")) genusSelect.setValue("Temnothorax (Acorn Ants)");
                else if (sciName.contains("eciton")) genusSelect.setValue("Eciton (Army Ants)");
                else if (sciName.contains("macrotermes")) genusSelect.setValue("Macrotermes (Cathedral Termites)");
                else if (sciName.contains("reticulitermes")) genusSelect.setValue("Reticulitermes (Subterranean Termites)");
            }

            // 2. Body length & Tunnel diameter clearance
            float avgBodyMm = species.getAverageCasteBodyLengthMm();
            Slider workerSizeSlider = pane.getWorkerSizeSlider();
            if (workerSizeSlider != null) {
                workerSizeSlider.setValue(Math.max(2.0, Math.min(30.0, avgBodyMm)));
            }

            Slider tunnelWidthSlider = pane.getTunnelWidthSlider();
            if (tunnelWidthSlider != null) {
                double reqTunnelScale = Math.max(2.0, Math.min(5.0, Math.ceil((avgBodyMm * 1.25) / 3.0)));
                tunnelWidthSlider.setValue(reqTunnelScale);
            }

            // 3. Nest Development Stage Multipliers
            int stageIndex = pane.getNestStageCombo() != null ? pane.getNestStageCombo().getSelectionModel().getSelectedIndex() : 2;
            if (stageIndex < 0) stageIndex = 2;

            applyStageScalingInternal(pane, species, stageIndex);

            I18nManager i18n = I18nManager.getInstance();
            Label statusLabel = pane.getSpeciesStatusLabel();
            if (statusLabel != null) {
                statusLabel.setText(String.format(i18n.get("nest.status.synchronized"), species.getCommonName()));
            }

            ComboBox<String> speciesModelCombo = pane.getSpeciesModelCombo();
            if (speciesModelCombo != null) {
                String matchedItem = null;
                for (String item : speciesModelCombo.getItems()) {
                    String cleanItem = item.replaceAll("^[🐜🐝✨]\\s*", "").trim();
                    if (cleanItem.equalsIgnoreCase(species.getPresetName()) ||
                        cleanItem.equalsIgnoreCase(species.getCommonName()) ||
                        (species.getCommonName() != null && cleanItem.toLowerCase().contains(species.getCommonName().toLowerCase())) ||
                        (species.getPresetName() != null && cleanItem.toLowerCase().contains(species.getPresetName().toLowerCase()))) {
                        matchedItem = item;
                        break;
                    }
                }
                if (matchedItem != null) {
                    speciesModelCombo.setValue(matchedItem);
                } else {
                    speciesModelCombo.setValue(i18n.get("nest.species.custom_config"));
                }
            }
        } finally {
            pane.setUpdatingSpeciesCombo(false);
        }

        pane.regen();
        pane.repaint();
    }

    public static void applyStageScaling(NestGeneratorPane pane, DefaultSpecies species, int stageIndex) {
        if (pane == null || species == null) return;
        applyStageScalingInternal(pane, species, stageIndex);
        pane.regen();
        pane.repaint();
    }

    private static void applyStageScalingInternal(NestGeneratorPane pane, DefaultSpecies species, int stageIndex) {
        if (stageIndex < 0) stageIndex = 2;
        StageScaling scaling = getStageScaling(stageIndex);

        Slider branchingSlider = pane.getBranchingSlider();
        if (branchingSlider != null) branchingSlider.setValue(scaling.branching());

        int popSize = Math.max(10, (int) (species.getTypicalColonySize() * scaling.populationMultiplier()));
        double depth = Math.max(0.5, Math.min(8.0, Math.log10(popSize + 10) * 0.8 * scaling.depthMultiplier()));
        Slider depthSlider = pane.getDepthSlider();
        if (depthSlider != null) depthSlider.setValue(depth);

        // 4. Chamber distribution according to species biology and nest stage
        int queenCount = species.getQueenCount();
        pane.setSp("👑 Queen Chamber", Math.max(1, Math.min(25, stageIndex == 0 ? 1 : queenCount)));

        boolean isFungusGrower = "FUNGUS".equalsIgnoreCase(species.getPrimaryDiet()) ||
                                 "FUNGUS".equalsIgnoreCase(species.getSecondaryDiet()) ||
                                 (species.getCommonName() != null && (
                                     species.getCommonName().toLowerCase().contains("fungus") ||
                                     species.getCommonName().toLowerCase().contains("leafcutter") ||
                                     species.getCommonName().toLowerCase().contains("champignonniste") ||
                                     species.getCommonName().toLowerCase().contains("atta")));

        int fungusCount = 0;
        if (isFungusGrower) {
            if (stageIndex == 0) fungusCount = 1;
            else if (stageIndex == 1) fungusCount = 3;
            else if (stageIndex == 2) fungusCount = 6;
            else fungusCount = 12;
        }
        pane.setSp("🍄 Fungus Gardens", fungusCount);

        int broodCount = Math.max(1, (int) (Math.sqrt(popSize / 200.0) * (stageIndex == 0 ? 0.5 : stageIndex == 3 ? 1.8 : 1.0)));
        if (stageIndex == 0) broodCount = 1;
        pane.setSp("🥚 Brood Chambers", Math.max(1, Math.min(25, broodCount)));

        int foodCount = Math.max(1, (int) (Math.sqrt(popSize / 300.0) * (stageIndex == 0 ? 0.5 : stageIndex == 3 ? 1.8 : 1.0)));
        if (stageIndex == 0) foodCount = 1;
        pane.setSp("🍖 Food Storage", Math.max(1, Math.min(25, foodCount)));

        int entranceCount = stageIndex == 0 ? 1 : stageIndex == 1 ? 2 : stageIndex == 2 ? 2 : 4;
        pane.setSp("🚪 Entrances", entranceCount);

        int wasteCount = stageIndex == 0 ? 1 : stageIndex == 1 ? 1 : stageIndex == 2 ? 2 : 4;
        pane.setSp("🗑 Waste Dumps", wasteCount);

        pane.updateTotalChambers();
    }
}
