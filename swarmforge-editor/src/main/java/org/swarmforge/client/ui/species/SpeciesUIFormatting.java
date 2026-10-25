/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.species;

import org.swarmforge.client.util.I18nManager;
import org.swarmforge.core.behavior.BrainDescriptor;
import org.swarmforge.core.behavior.BrainPluginRegistry;
import org.swarmforge.core.behavior.ReasoningArchitecture.ArchitectureType;

import java.util.ArrayList;
import java.util.List;

/**
 * UI Internationalization and Formatting helpers for Species attributes (diets, nest types, venoms, brain architectures).
 */
public final class SpeciesUIFormatting {

    private SpeciesUIFormatting() {
    }

    public static String getDietTitle(String diet) {
        if (diet == null) return "";
        I18nManager i18n = I18nManager.getInstance();
        return switch (diet) {
            case "SUGARS_NECTAR" -> i18n.get("species.diet.sugars_nectar");
            case "INSECTS_MEAT" -> i18n.get("species.diet.insects_meat");
            case "SEEDS" -> i18n.get("species.diet.seeds");
            case "FUNGUS" -> i18n.get("species.diet.fungus");
            case "WOOD_CELLULOSE" -> i18n.get("species.diet.wood_cellulose");
            case "HONEYDEW" -> i18n.get("species.diet.honeydew");
            case "OMNIVORE" -> i18n.get("species.diet.omnivore");
            case "NONE" -> i18n.get("species.diet.none");
            default -> diet;
        };
    }

    public static String getDietDescription(String diet) {
        if (diet == null) return "";
        I18nManager i18n = I18nManager.getInstance();
        return switch (diet) {
            case "SUGARS_NECTAR" -> i18n.get("species.diet.sugars_nectar.desc");
            case "INSECTS_MEAT" -> i18n.get("species.diet.insects_meat.desc");
            case "SEEDS" -> i18n.get("species.diet.seeds.desc");
            case "FUNGUS" -> i18n.get("species.diet.fungus.desc");
            case "WOOD_CELLULOSE" -> i18n.get("species.diet.wood_cellulose.desc");
            case "HONEYDEW" -> i18n.get("species.diet.honeydew.desc");
            case "OMNIVORE" -> i18n.get("species.diet.omnivore.desc");
            case "NONE" -> i18n.get("species.diet.none.desc");
            default -> "";
        };
    }

    public static String getNestTypeTitle(String type) {
        if (type == null) return "";
        I18nManager i18n = I18nManager.getInstance();
        return switch (type) {
            case "WAX_COMB_HEXAGONAL" -> i18n.get("species.nest.wax_comb_hexagonal");
            case "WAX_POTS_CLUSTER" -> i18n.get("species.nest.wax_pots_cluster");
            case "PAPER_PEDUNCULATE" -> i18n.get("species.nest.paper_pedunculate");
            case "CATHEDRAL_MOUND" -> i18n.get("species.nest.cathedral_mound");
            case "ARBOREAL_SILK_LEAF" -> i18n.get("species.nest.arboreal_silk_leaf");
            case "SUBTERRANEAN_FUNGI_VAULT" -> i18n.get("species.nest.subterranean_fungi_vault");
            case "CARTON_NEST" -> i18n.get("species.nest.carton_nest");
            case "BAMBOO_STEM_NEST" -> i18n.get("species.nest.bamboo_stem_nest");
            case "BIVOUAC_LIVING_NEST" -> i18n.get("species.nest.bivouac_living_nest");
            case "MOUND" -> i18n.get("species.nest.mound");
            case "TREE" -> i18n.get("species.nest.tree");
            case "MATURE", "MATURE_BURROW", "SUBTERRANEAN" -> i18n.get("species.nest.mature");
            case "SIMPLE", "UNDERGROUND_BURROW", "BURROW_UNDERGROUND", "SUBTERRANEAN_BURROW", "BURROW" -> i18n.get("species.nest.simple");
            default -> type;
        };
    }

    public static String getNestTypeDescription(String type) {
        if (type == null) return "";
        I18nManager i18n = I18nManager.getInstance();
        return switch (type) {
            case "WAX_COMB_HEXAGONAL" -> i18n.get("species.nest.wax_comb_hexagonal.desc");
            case "WAX_POTS_CLUSTER" -> i18n.get("species.nest.wax_pots_cluster.desc");
            case "PAPER_PEDUNCULATE" -> i18n.get("species.nest.paper_pedunculate.desc");
            case "CATHEDRAL_MOUND" -> i18n.get("species.nest.cathedral_mound.desc");
            case "ARBOREAL_SILK_LEAF" -> i18n.get("species.nest.arboreal_silk_leaf.desc");
            case "SUBTERRANEAN_FUNGI_VAULT" -> i18n.get("species.nest.subterranean_fungi_vault.desc");
            case "CARTON_NEST" -> i18n.get("species.nest.carton_nest.desc");
            case "BAMBOO_STEM_NEST" -> i18n.get("species.nest.bamboo_stem_nest.desc");
            case "BIVOUAC_LIVING_NEST" -> i18n.get("species.nest.bivouac_living_nest.desc");
            case "MOUND" -> i18n.get("species.nest.mound.desc");
            case "TREE" -> i18n.get("species.nest.tree.desc");
            case "MATURE", "MATURE_BURROW", "SUBTERRANEAN" -> i18n.get("species.nest.mature.desc");
            case "SIMPLE", "UNDERGROUND_BURROW", "BURROW_UNDERGROUND", "SUBTERRANEAN_BURROW", "BURROW" -> i18n.get("species.nest.simple.desc");
            default -> "";
        };
    }

    public static String getVenomTitle(String v) {
        if (v == null || v.trim().isEmpty()) return "";
        if (v.contains(",")) {
            String[] parts = v.split(",");
            List<String> titles = new ArrayList<>();
            for (String p : parts) {
                String title = getVenomTitle(p.trim());
                if (!title.isEmpty()) titles.add(title);
            }
            return String.join(", ", titles);
        }
        I18nManager i18n = I18nManager.getInstance();
        return switch (v.trim()) {
            case "NONE" -> i18n.get("species.venom.none");
            case "FORMIC_ACID" -> i18n.get("species.venom.formic_acid");
            case "VENOMOUS_STING" -> i18n.get("species.venom.venomous_sting");
            case "CHEMICAL_SPRAY" -> i18n.get("species.venom.chemical_spray");
            case "ACID_SPRAY" -> i18n.get("species.venom.acid_spray");
            case "POWERFUL_MANDIBLES" -> i18n.get("species.venom.powerful_mandibles");
            case "SOLENOPSIN" -> i18n.get("species.venom.solenopsin");
            case "NEUROTOXIN" -> i18n.get("species.venom.neurotoxin");
            case "CYTOTOXIN" -> i18n.get("species.venom.cytotoxin");
            case "TERPENE_RESIN" -> i18n.get("species.venom.terpene_resin");
            case "AUTOTHYSIS_BOMB" -> i18n.get("species.venom.autothysis_bomb");
            default -> v.trim();
        };
    }

    public static String getVenomDescription(String v) {
        if (v == null || v.trim().isEmpty()) return "";
        if (v.contains(",")) {
            String[] parts = v.split(",");
            List<String> descs = new ArrayList<>();
            for (String p : parts) {
                String desc = getVenomDescription(p.trim());
                if (!desc.isEmpty()) descs.add(desc);
            }
            return String.join(" | ", descs);
        }
        I18nManager i18n = I18nManager.getInstance();
        return switch (v.trim()) {
            case "NONE" -> i18n.get("species.venom.none.desc");
            case "FORMIC_ACID" -> i18n.get("species.venom.formic_acid.desc");
            case "VENOMOUS_STING" -> i18n.get("species.venom.venomous_sting.desc");
            case "CHEMICAL_SPRAY", "ACID_SPRAY" -> i18n.get("species.venom.chemical_spray.desc");
            case "POWERFUL_MANDIBLES" -> i18n.get("species.venom.powerful_mandibles.desc");
            case "SOLENOPSIN" -> i18n.get("species.venom.solenopsin.desc");
            case "NEUROTOXIN" -> i18n.get("species.venom.neurotoxin.desc");
            case "CYTOTOXIN" -> i18n.get("species.venom.cytotoxin.desc");
            case "TERPENE_RESIN" -> i18n.get("species.venom.terpene_resin.desc");
            case "AUTOTHYSIS_BOMB" -> i18n.get("species.venom.autothysis_bomb.desc");
            default -> "";
        };
    }

    public static String getDecisionArchTitle(String arch) {
        if (arch == null || arch.isBlank()) return "";
        if (BrainPluginRegistry.getInstance().hasBrain(arch)) {
            var desc = BrainPluginRegistry.getInstance().getDescriptor(arch);
            if (desc.isPresent()) return desc.get().getDisplayName();
        }
        for (BrainDescriptor d : BrainPluginRegistry.getInstance().getRegisteredBrains()) {
            if (d.getDisplayName().equalsIgnoreCase(arch)) return d.getDisplayName();
        }
        ArchitectureType t = ArchitectureType.parse(arch);
        I18nManager i18n = I18nManager.getInstance();
        return switch (t) {
            case BDI -> i18n.get("species.arch.bdi");
            case BEHAVIOR_TREE -> i18n.get("species.arch.behavior_tree");
            case BLACKBOARD -> i18n.get("species.arch.blackboard");
            case FINITE_STATE_MACHINE -> i18n.get("species.arch.fsm");
            case FUZZY_LOGIC -> i18n.get("species.arch.fuzzy_logic");
            case HYBRID -> i18n.get("species.arch.hybrid");
            case NEURAL_NETWORK -> i18n.get("species.arch.neural_network");
        };
    }

    public static String getDecisionArchDescription(String arch) {
        if (arch == null || arch.isBlank()) return "";
        if (BrainPluginRegistry.getInstance().hasBrain(arch)) {
            var desc = BrainPluginRegistry.getInstance().getDescriptor(arch);
            if (desc.isPresent()) return desc.get().getDescription();
        }
        for (BrainDescriptor d : BrainPluginRegistry.getInstance().getRegisteredBrains()) {
            if (d.getDisplayName().equalsIgnoreCase(arch)) return d.getDescription();
        }
        ArchitectureType t = ArchitectureType.parse(arch);
        I18nManager i18n = I18nManager.getInstance();
        return switch (t) {
            case BDI -> i18n.get("species.arch.bdi.desc");
            case BEHAVIOR_TREE -> i18n.get("species.arch.behavior_tree.desc");
            case BLACKBOARD -> i18n.get("species.arch.blackboard.desc");
            case FINITE_STATE_MACHINE -> i18n.get("species.arch.fsm.desc");
            case FUZZY_LOGIC -> i18n.get("species.arch.fuzzy_logic.desc");
            case HYBRID -> i18n.get("species.arch.hybrid.desc");
            case NEURAL_NETWORK -> i18n.get("species.arch.neural_network.desc");
        };
    }
}
