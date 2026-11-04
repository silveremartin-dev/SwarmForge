/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import org.swarmforge.client.ui.WorldEditorPane;
import org.swarmforge.client.util.ThemeManager;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.world.TerrainGenerator;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Builds and configures the standalone World Editor tab for {@link SwarmForgeClient}.
 */
class ClientWorldEditorTabBuilder {

    private static final Logger LOG = Logger.getLogger(ClientWorldEditorTabBuilder.class.getName());

    private ClientWorldEditorTabBuilder() {}

    static Node createWorldEditor(SwarmForgeClient client) {
        client.worldEditorPane = new WorldEditorPane();
        client.worldEditorPane.setSimulationMode(false);
        client.worldEditorPane.setOnGenerate(config -> {
            try {
                double sizeMeters = (double) config.getOrDefault("surfaceSizeMeters", 2.0);
                double resMm = (double) config.getOrDefault("resolutionMm", 0.5);
                double roughness = (double) config.getOrDefault("roughness", 0.45);

                int gridW = (int) (sizeMeters * 32);
                int gridH = (int) (sizeMeters * 32);
                int gridDepth = 32;

                Terrarium terrarium = new Terrarium(gridW, gridH, gridDepth);
                TerrainGenerator gen = new TerrainGenerator(System.currentTimeMillis());

                gen.generate(terrarium, gridDepth / 2, 10, (float) roughness * 0.1f);
                if (Boolean.TRUE.equals(config.get("hasRiver"))) {
                    gen.addWater(terrarium, gridDepth / 2 - 2, 0.4f);
                }

                client.lastGeneratedTerrarium = terrarium;
                if (client.gameView != null && client.gameView.getGameApp() != null) {
                    client.gameView.getGameApp().renderTerrarium(terrarium);
                }

                client.localSimulation = new Simulation(terrarium);
                client.localSimulation.setTicksPerSecond(20);
                client.hudUpdater.startLocalSimulationUpdates();

                ThemeManager.createAlert(Alert.AlertType.INFORMATION, "Monde généré avec succès ! (Taille: " + sizeMeters + "m, Voxel: " + resMm + "mm)").show();
            } catch (Exception ex) {
                ThemeManager.createAlert(Alert.AlertType.ERROR, "Échec de génération: " + ex.getMessage()).show();
                LOG.log(Level.SEVERE, "Echec de generation: " + ex.getMessage(), ex);
            }
        });

        return client.worldEditorPane;
    }
}
