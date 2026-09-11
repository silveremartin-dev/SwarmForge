/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.spatial;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.simulation.Simulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Multi-Terrarium Spatial Boundary & Seamless Entity Migration System.
 * Manages spatial continuity across adjacent terrarium tiles (Ix, Iy) in a Megaterrarium cluster.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class BorderMigrationSystem {

    public record TerrariumCoord(int gridX, int gridY) implements Serializable {
        @Override
        public String toString() {
            return "[" + gridX + "," + gridY + "]";
        }
    }

    public record MigrationPayload(
            Individual individual,
            TerrariumCoord sourceCoord,
            TerrariumCoord targetCoord,
            float newX,
            float newY,
            float newZ
    ) implements Serializable {}

    @FunctionalInterface
    public interface MigrationListener {
        void onEntityMigrated(MigrationPayload payload);
    }

    private final List<MigrationListener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(MigrationListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(MigrationListener listener) {
        listeners.remove(listener);
    }

    /**
     * Checks all individuals in the simulation against terrarium boundaries and triggers migrations.
     */
    public List<MigrationPayload> checkAndProcessBoundaryMigrations(Simulation simulation, TerrariumCoord currentCoord) {
        List<MigrationPayload> migrations = new ArrayList<>();
        if (simulation == null || simulation.getTerrarium() == null) {
            return migrations;
        }

        Terrarium terrarium = simulation.getTerrarium();
        float width = terrarium.getWidth();
        float height = terrarium.getHeight();

        for (Colony colony : simulation.getColonies()) {
            for (Individual ind : colony.getLivingIndividuals()) {
                float x = ind.getX();
                float y = ind.getY();
                float z = ind.getZ();

                int deltaGridX = 0;
                int deltaGridY = 0;
                float translatedX = x;
                float translatedY = y;

                if (x >= width) {
                    deltaGridX = 1;
                    translatedX = 0.5f;
                } else if (x < 0.0f) {
                    deltaGridX = -1;
                    translatedX = width - 0.5f;
                }

                if (y >= height) {
                    deltaGridY = 1;
                    translatedY = 0.5f;
                } else if (y < 0.0f) {
                    deltaGridY = -1;
                    translatedY = height - 0.5f;
                }

                if (deltaGridX != 0 || deltaGridY != 0) {
                    TerrariumCoord target = new TerrariumCoord(currentCoord.gridX() + deltaGridX, currentCoord.gridY() + deltaGridY);
                    MigrationPayload payload = new MigrationPayload(ind, currentCoord, target, translatedX, translatedY, z);
                    migrations.add(payload);

                    // Update entity coordinates and notify listeners
                    ind.setPosition(translatedX, translatedY, z);
                    for (MigrationListener listener : listeners) {
                        listener.onEntityMigrated(payload);
                    }
                }
            }
        }

        return migrations;
    }
}
