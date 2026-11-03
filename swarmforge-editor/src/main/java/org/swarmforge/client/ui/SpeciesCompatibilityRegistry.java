/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.scene.control.ComboBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Registry containing scientific compatibility maps between social insect species,
 * physical nest architectures, capacity estimates, and accessory ecological species.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SpeciesCompatibilityRegistry {

    public record AccessorySpeciesInfo(String name, String role, String description, int defaultCount) {
        public AccessorySpeciesInfo(String name, String role, String description) {
            this(name, role, description, 50);
        }
    }

    public static int estimateNestCapacity(String nestType) {
        if (nestType == null) return 25000;
        String lower = nestType.toLowerCase();
        if (lower.contains("stem") || lower.contains("gall") || lower.contains("galle") || lower.contains("tige") || lower.contains("founding") || lower.contains("claustral")) {
            return 500;
        } else if (lower.contains("bivouac") || lower.contains("pot") || lower.contains("silk") || lower.contains("paper") || lower.contains("guêpe")) {
            return 5000;
        } else if (lower.contains("tree") || lower.contains("cavity") || lower.contains("carton") || lower.contains("comb") || lower.contains("beehive") || lower.contains("ruche")) {
            return 20000;
        } else if (lower.contains("subterranean") || lower.contains("burrow") || lower.contains("galerie") || lower.contains("souterrain") || lower.contains("needle") || lower.contains("dôme")) {
            return 50000;
        } else if (lower.contains("supercolonie") || lower.contains("supercolony") || lower.contains("fungi") || lower.contains("vault") || lower.contains("cathédrale") || lower.contains("cathedral")) {
            return 500000;
        }
        return 25000;
    }

    /**
     * Helper method to filter physical nest architectures based on biological species capability.
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
            } else if (lower.contains("vespula") || lower.contains("guêpe")) {
                nests.add("Suspended Paper Nest");
            } else if (lower.contains("reticulitermes") || lower.contains("macrotermes") || lower.contains("termite")) {
                nests.add("Cathedral Termite Mound");
            } else if (lower.contains("crematogaster") || lower.contains("carton")) {
                nests.add("Carton Wooden Nest");
            } else if (lower.contains("temnothorax") || lower.contains("gall") || lower.contains("galle")) {
                nests.add("Hollow Stem & Gall Nest");
            } else if (lower.contains("camponotus") || lower.contains("carpenter") || lower.contains("charpentière")) {
                nests.add("Tree Trunk Cavity Nest");
            } else if (lower.contains("eciton") || lower.contains("army") || lower.contains("légionnaire")) {
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

    /**
     * Helper method to filter compatible accessory species (Proies, Prédateurs, Commensaux, Pathogènes).
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
            list.add(new AccessorySpeciesInfo("Antlion Pitfall Trap (Myrmeleon)", "PRÉDATEUR", "Funnel-web pitfall predator in sand", 10));
        } else if (lower.contains("apis") || lower.contains("abeille")) {
            list.add(new AccessorySpeciesInfo("Nectariferous Grasses & Flowers", "COMMENSAL", "Melliferous flowers supplying nectar and pollen", 120));
            list.add(new AccessorySpeciesInfo("Parasitic Mite (Varroa destructor)", "PATHOGEN", "Hematophagous parasitic mite targeting bee brood", 30));
            list.add(new AccessorySpeciesInfo("Hunting Spider / Robber Fly", "PRÉDATEUR", "Predators capturing foragers in flight", 15));
        } else if (lower.contains("reticulitermes") || lower.contains("macrotermes") || lower.contains("termite")) {
            list.add(new AccessorySpeciesInfo("Humid Moss & Woody Timber", "COMMENSAL", "Digestible cellulosic biomass", 100));
            list.add(new AccessorySpeciesInfo("Mealworm Larvae (Protein Prey)", "PROIE", "Residual soil arthropod prey", 30));
            list.add(new AccessorySpeciesInfo("Hunting Wasp / Subterranean Spider", "PRÉDATEUR", "Gallery specialist predators", 12));
        } else if (lower.contains("vespula") || lower.contains("guêpe")) {
            list.add(new AccessorySpeciesInfo("Mealworm Larvae & Caterpillars (Prey)", "PROIE", "Insect prey hunted for brood protein", 60));
            list.add(new AccessorySpeciesInfo("Grasses & Flowers (Nectar)", "COMMENSAL", "Sugary energy sources for adults", 80));
            list.add(new AccessorySpeciesInfo("Entomopathogenic Fungus (Cordyceps)", "PATHOGEN", "Spores carried during flight", 15));
        } else {
            list.add(new AccessorySpeciesInfo("Pine Aphids (Cinara / Honeydew)", "COMMENSAL", "Aphids farmed in trophobiosis for sugary honeydew", 80));
            list.add(new AccessorySpeciesInfo("Seeded Grasses (Herbs & Biomass)", "COMMENSAL", "Local vegetation providing substrate and shade", 100));
            list.add(new AccessorySpeciesInfo("Mealworm Larvae (Protein Prey)", "PROIE", "Arthropodes proies apportant les protéines", 50));
            list.add(new AccessorySpeciesInfo("Antlion Pitfall Trap (Myrmeleon)", "PRÉDATEUR", "Natural predator of patrolling workers", 15));
            list.add(new AccessorySpeciesInfo("Entomopathogenic Fungus (Cordyceps)", "PATHOGEN", "Mycelial parasite targeting colony", 10));
        }
        list.sort(Comparator.comparing(AccessorySpeciesInfo::name));
        return list;
    }
}
