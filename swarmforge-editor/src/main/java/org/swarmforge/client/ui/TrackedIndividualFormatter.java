/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 SilvÃ¨re Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import org.swarmforge.client.util.I18nManager;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Predator;
import org.swarmforge.core.simulation.TunnelNetwork;

import java.util.Locale;

/**
 * Utility formatter providing localized and formatted representations for tracked entities,
 * biological castes, life stages, jobs, chambers, and predators.
 *
 * @author SilvÃ¨re Martin-Michiellot
 * @author Gemini AI Assistant
 */
public final class TrackedIndividualFormatter {

    private TrackedIndividualFormatter() {
        // Utility class
    }

    public static String formatCaste(Individual.Caste caste) {
        if (caste == null) return "N/A";
        I18nManager i18n = I18nManager.getInstance();
        return switch (caste) {
            case QUEEN -> i18n.get("legend.caste.queen", "ðŸ‘‘ Reine (Gyne)");
            case WORKER -> i18n.get("legend.caste.worker", "ðŸ œ OuvriÃ¨re");
            case SOLDIER -> i18n.get("legend.caste.soldier", "ðŸ›¡ï¸  Soldat / Major");
            case NURSE -> i18n.get("god.caste.nurse", "ðŸ ¼ Nourrice");
            case FORAGER -> i18n.get("god.caste.forager", "ðŸŒ¿ Fourrageuse");
            case MALE -> i18n.get("legend.caste.drone", "ðŸª½ MÃ¢le / Drone");
            default -> caste.name();
        };
    }

    public static String formatLifeStage(Individual.LifeStage stage) {
        if (stage == null) return "Adulte";
        I18nManager i18n = I18nManager.getInstance();
        return switch (stage) {
            case EGG -> i18n.get("god.caste.egg", "ðŸ¥š Å’uf");
            case LARVA -> i18n.get("god.caste.larva", "ðŸ › Larve");
            case PUPA -> i18n.get("god.caste.pupa", "ðŸ¥¥ Nymphe / Cocon");
            case ADULT -> i18n.get("species.tab.adult", "ðŸ œ Adulte Imago");
            default -> stage.name();
        };
    }

    public static String formatJob(Individual.Job job) {
        if (job == null || job == Individual.Job.NONE) return "GÃ©nÃ©raliste";
        I18nManager i18n = I18nManager.getInstance();
        return switch (job) {
            case NURSE -> i18n.get("job.nurse", "ðŸ ¼ Soins au couvain (Nourrice)");
            case BUILDER -> i18n.get("job.builder", "ðŸ§± Construction & Galerie");
            case FORAGER -> i18n.get("job.forager", "ðŸŒ¿ RÃ©colte & Fourrageage");
            case GUARD -> i18n.get("job.guard", "ðŸ›¡ï¸  Garde & SÃ©curitÃ©");
            case UNDERTAKER -> i18n.get("job.undertaker", "ðŸ§¹ Ã‰boueuse & HygiÃ¨ne");
            case DANCING -> i18n.get("job.dancing", "ðŸ’ƒ Danse frÃ©tillante");
            case IDLE -> i18n.get("job.idle", "ðŸ’¤ Repos / Inactive");
            default -> job.name();
        };
    }

    public static String formatCarriedItem(Individual.CarriedItem item) {
        if (item == null || item == Individual.CarriedItem.NONE) {
            return I18nManager.getInstance().get("tracked_ant.cargo_none", "Rien (Vide)");
        }
        I18nManager i18n = I18nManager.getInstance();
        return switch (item) {
            case FOOD -> i18n.get("resource.food", "ðŸ¥© Nourriture / ProtÃ©ines");
            case WATER -> i18n.get("resource.water", "ðŸ’§ Gouttelette d'Eau");
            case EARTH -> i18n.get("resource.earth", "ðŸª¨ Granule de Terre");
            case BROOD -> i18n.get("resource.brood", "ðŸ¥š Couvain / Larve");
            case DEAD_ANT -> i18n.get("resource.dead_ant", "ðŸ’€ Cadavre / DÃ©chet");
            default -> item.name();
        };
    }

    public static String formatActionType(Object actionType) {
        if (actionType == null) return "Recherche autonome";
        String s = actionType.toString().toUpperCase();
        return switch (s) {
            case "MOVE" -> "DÃ©placement & Orientation";
            case "FORAGE" -> "RÃ©colte & Recherche";
            case "RETURN_HOME" -> "Retour Ã  la colonie (Homing)";
            case "DEPOSIT_FOOD" -> "DÃ©pÃ´t de nourriture au grenier";
            case "DEPOSIT_PHEROMONE" -> "Marquage phÃ©romonal";
            case "ATTACK" -> "Combat & Attaque dÃ©fensive";
            case "FLEE" -> "Fuite & Ã‰vitement du danger";
            case "GROOM" -> "Toilettage social (Allogrooming)";
            case "NURSE", "TEND_BROOD" -> "Soins et alimentation du couvain";
            case "REST" -> "Repos & Ã‰conomie mÃ©tabolique";
            case "EXPLORE" -> "Exploration libre";
            case "FOLLOW_TRAIL" -> "Suivi de piste phÃ©romonale";
            case "COMMUNICATE" -> "Antennation & Trophallaxie";
            case "LAY_EGG" -> "Ponte d'ovocytes (Reine)";
            case "DEFEND_PATROL" -> "Patrouille sentinelle";
            case "DIG" -> "Excavation de galeries";
            default -> actionType.toString();
        };
    }

    public static String formatNodeType(Object type) {
        if (type == null) return "Chambre Standard";
        String s = type.toString().toUpperCase();
        I18nManager i18n = I18nManager.getInstance();
        if (s.contains("ENTRANCE") || s.contains("ENTREE")) return i18n.get("chamber.entrance", "ðŸšª EntrÃ©e Principale / DÃ´me");
        if (s.contains("QUEEN") || s.contains("ROYAL")) return i18n.get("chamber.royal", "ðŸ‘‘ Chambre Royale & Ponte");
        if (s.contains("NURSERY") || s.contains("BROOD") || s.contains("COUVAIN")) return i18n.get("chamber.nursery", "ðŸ¥š Couvain & PouponniÃ¨re");
        if (s.contains("FOOD") || s.contains("STORAGE") || s.contains("GRENIER")) return i18n.get("chamber.food", "ðŸŒ¾ Grenier Ã  Graines & RÃ©serves");
        if (s.contains("FUNGUS") || s.contains("CHAMPIGNON")) return i18n.get("chamber.fungus", "ðŸ „ Chambre Ã  Meules Fongiques");
        if (s.contains("WASTE") || s.contains("DUMP") || s.contains("DEPOTOIR")) return i18n.get("chamber.waste", "ðŸ—‘ï¸  DÃ©potoir & DÃ©charge");
        if (s.contains("VENTILATION") || s.contains("CHIMNEY")) return i18n.get("chamber.vent", "ðŸŒªï¸  Puits d'AÃ©ration & CheminÃ©e");
        if (s.contains("JUNCTION") || s.contains("TUNNEL") || s.contains("GALERIE")) return i18n.get("chamber.junction", "ðŸ”€ Jonction & Galerie de Transit");
        return type.toString();
    }

    public static String buildAntAiTooltip(Individual ant) {
        String rawBehaviors = ant.getActiveBehaviorsSummary();
        return String.format(
            "ðŸ§  IA - Ã‰tat Cognitif : %s\n" +
            "ðŸ’¼ TÃ¢che / RÃ´le AssignÃ© : %s\n" +
            "âš¡ Action en cours : %s\n" +
            "ðŸ“‹ CapacitÃ©s & Comportements IA Actifs :\n  â€¢ %s\n" +
            "ðŸ§¬ Profil Ã‰thologique : %s",
            ant.getState() != null ? ant.getState() : "PATROUILLE",
            formatJob(ant.getJob()),
            ant.getCachedAction() != null && ant.getCachedAction().type() != null ? formatActionType(ant.getCachedAction().type()) : "Recherche autonome",
            (rawBehaviors != null && !rawBehaviors.isBlank()) ? rawBehaviors.replace(";", "\n  â€¢ ") : "Patrouille & navigation standard",
            ant.getSpecies() != null ? (ant.getSpecies().getCommonName() != null ? ant.getSpecies().getCommonName() : ant.getSpecies().getScientificName()) : "Standard"
        );
    }

    public static String buildPredatorAiTooltip(Predator predator, String targetName) {
        return String.format(
            "ðŸ§  IA - Ã‰tat DÃ©cisionnel : %s\n" +
            "ðŸŽ¯ Cible VerrouillÃ©e : %s\n" +
            "ðŸ ¹ Tactique de Chasse IA : %s\n" +
            "ðŸ‘ ï¸  Rayon de Perception Sensorielle : %.1f m\n" +
            "ðŸ•¸ï¸  PiÃ¨ge / Embuscade IA : %s\n" +
            "âš¡ Vitesse de Traque IA : %.1f m/s",
            predator.getState(),
            targetName,
            predator.getType().getHuntingStyle(),
            predator.getType().getVisionRange(),
            predator.isTrapBuilt() ? "PosÃ© & Actif" : "En prospection",
            predator.getType().getBaseSpeed()
        );
    }

    public static ChamberOccupantStats computeChamberOccupants(TunnelNetwork.TunnelNode node, Colony colony) {
        int occupants = 0;
        int queens = 0, workers = 0, soldiers = 0, males = 0;
        if (colony != null) {
            for (Individual ind : colony.getLivingIndividuals()) {
                double dist = Math.hypot(ind.getX() - node.x(), Math.hypot(ind.getY() - node.y(), ind.getZ() - node.z()));
                if (dist <= Math.max(node.radiusX(), node.radiusZ()) * 1.5) {
                    occupants++;
                    if (ind.getCaste() == Individual.Caste.QUEEN) queens++;
                    else if (ind.getCaste() == Individual.Caste.SOLDIER) soldiers++;
                    else if (ind.getCaste() == Individual.Caste.MALE) males++;
                    else workers++;
                }
            }
        }
        return new ChamberOccupantStats(occupants, queens, workers, soldiers, males);
    }

    public record ChamberOccupantStats(int total, int queens, int workers, int soldiers, int males) {}
}
