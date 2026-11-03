/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client;

import javafx.scene.control.Alert;
import org.swarmforge.client.ui.NestAlgorithm;
import org.swarmforge.client.ui.NestGeneratorPane;
import org.swarmforge.client.ui.NestPresetManager;
import org.swarmforge.client.util.ThemeManager;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.world.NestGenerator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Helper encapsulating nest preset resolution and 3D nest generation for {@link ClientScenarioExecutionHelper}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
final class ClientNestGenerationHelper {

    private static final Logger LOG = Logger.getLogger(ClientNestGenerationHelper.class.getName());

    private ClientNestGenerationHelper() {
    }

    static float resolveScaleFactor(String rawNestType) {
        float scaleFactor = 1.2f;
        if (rawNestType != null) {
            String ntl = rawNestType.toLowerCase();
            if (ntl.contains("mound") || ntl.contains("dôme") || ntl.contains("solar") || ntl.contains("bivouac") || ntl.contains("army")) {
                scaleFactor = 1.6f;
            } else if (ntl.contains("tree") || ntl.contains("arbre") || ntl.contains("trunk") || ntl.contains("hollow") || ntl.contains("paper") || ntl.contains("guêpe") || ntl.contains("suspended") || ntl.contains("carton")) {
                scaleFactor = 1.4f;
            } else if (ntl.contains("fungi") || ntl.contains("champignon") || ntl.contains("fungal") || ntl.contains("vault")) {
                scaleFactor = 1.8f;
            } else if (ntl.contains("cathedral") || ntl.contains("cathédrale") || ntl.contains("termite")) {
                scaleFactor = 2.0f;
            } else if (ntl.contains("wax comb") || ntl.contains("beehive") || ntl.contains("hexagonal")) {
                scaleFactor = 1.5f;
            } else if (ntl.contains("pots") || ntl.contains("bourdon") || ntl.contains("propolis") || ntl.contains("silk") || ntl.contains("soie") || ntl.contains("leaf")) {
                scaleFactor = 1.3f;
            } else if (ntl.contains("jeune") || ntl.contains("young")) {
                scaleFactor = 0.8f;
            } else if (ntl.contains("supercolony") || ntl.contains("supercolonie")) {
                scaleFactor = 3.5f;
            } else if (ntl.contains("old") || ntl.contains("âgé")) {
                scaleFactor = 2.2f;
            }
            if (ntl.contains("jeune") || ntl.contains("young")) {
                scaleFactor *= 0.7f;
            } else if (ntl.contains("supercolony") || ntl.contains("supercolonie")) {
                scaleFactor *= 2.0f;
            }
        }
        return scaleFactor;
    }

    static Map<String, Object> resolveNestConfig(String rawNestType, float scaleFactor) {
        Map<String, Object> nestConfig = null;
        if (rawNestType != null) {
            String typeKey = rawNestType.trim();
            NestPresetManager presetMgr = new NestPresetManager();
            if (presetMgr.contains(typeKey)) {
                nestConfig = presetMgr.get(typeKey);
            } else {
                for (String pName : presetMgr.names()) {
                    if (pName.equalsIgnoreCase(typeKey) || pName.toLowerCase().contains(typeKey.toLowerCase())) {
                        nestConfig = presetMgr.get(pName);
                        break;
                    }
                }
            }
        }
        if (nestConfig == null) {
            nestConfig = new LinkedHashMap<>();
            nestConfig.put("architecture", rawNestType != null ? rawNestType : "BURROW_UNDERGROUND");
            nestConfig.put("depth", 25.0);
            nestConfig.put("workerSizeMm", 5.0 * scaleFactor);
        }
        return nestConfig;
    }

    static void generateNest(SwarmForgeClient client, Map<String, Object> config) {
        if (client.lastGeneratedTerrarium == null) return;
        try {
            NestGenerator generator = new NestGenerator(client.lastGeneratedTerrarium, System.currentTimeMillis());

            if (config.containsKey("depth"))
                generator.maxDepth((int) config.get("depth"));
            if (config.containsKey("tunnelWidth"))
                generator.tunnelRadius(((Number) config.get("tunnelWidth")).floatValue());
            if (config.containsKey("branching"))
                generator.branchingFactor(((Number) config.get("branching")).intValue());
            if (config.containsKey("chamberDistribution")) {
                @SuppressWarnings("unchecked")
                Map<String, Integer> dist = (Map<String, Integer>) config.get("chamberDistribution");
                generator.setChamberCounts(dist);
            }

            long nestSeed = config.containsKey("seed") && config.get("seed") instanceof Number n ? n.longValue() : 123456L;
            NestGeneratorPane.GeneratedNest genNest = NestAlgorithm.generate(config, nestSeed);
            int chambers = genNest.nodes.size();

            if (client.localSimulation != null && !client.localSimulation.getColonies().isEmpty()) {
                for (Colony colony : client.localSimulation.getColonies()) {
                    if (colony.getTunnelNetwork() != null) {
                        NestAlgorithm.applyToTunnelNetwork(
                                genNest, colony.getNestX(), colony.getNestY(), colony.getNestZ(),
                                colony.getTunnelNetwork(), colony
                        );
                    }
                }
            }

            if (client.gameView != null) {
                client.gameView.getGameApp().renderTerrarium(client.lastGeneratedTerrarium);
            }
            if (client.worldEditorPane != null && client.localSimulation != null) {
                client.worldEditorPane.setSimulation(client.localSimulation);
                client.worldEditorPane.repaintAllViews();
            }

            ThemeManager.createAlert(Alert.AlertType.INFORMATION, "Nest generated! (" + chambers + " chambers created)").show();
        } catch (Exception e) {
            ThemeManager.createAlert(Alert.AlertType.ERROR, "Generation failed: " + e.getMessage()).show();
            LOG.log(java.util.logging.Level.SEVERE, "Generation failed: " + e.getMessage(), e);
        }
    }
}
