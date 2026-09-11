/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation.behaviors;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.simulation.TunnelNetwork;

import java.util.Random;

/**
 * Behavior for ants to dig and expand the nest.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class DiggingBehavior {

    public String getName() {
        return "Digging";
    }

    public float evaluate(Individual individual, Simulation simulation, Colony colony) {
        // Only workers dig
        if (individual.getCaste() != Individual.Caste.WORKER)
            return 0f;

        // Dig if population is high relative to nest size
        int population = colony.getLivingIndividuals().size();
        int tunnelNodes = colony.getTunnelNetwork().getNodeCount();

        // Desire more space if crowded (e.g. > 10 ants per node)
        float crowding = (float) population / Math.max(1, tunnelNodes * 10);

        // Also triggered if near a diggable location (simplified)

        return Math.min(1.0f, crowding);
    }

    public void execute(Individual individual, Simulation simulation, Colony colony) {
        // Simple random walk digging for now
        // Find nearest tunnel node
        TunnelNetwork network = colony.getTunnelNetwork();

        // If crowded, dig new tunnel
        // Find a random node to extend from
        var nodes = network.getNodes();
        if (nodes.isEmpty())
            return;

        Random rng = (individual != null && individual.getRandom() != null) 
                ? individual.getRandom() 
                : (simulation != null && simulation.getRandom() != null ? simulation.getRandom() : new Random(1337L));

        var startNode = nodes.get(rng.nextInt(nodes.size()));

        // Dig in random direction, mostly down or sideways
        float dx = (rng.nextFloat() - 0.5f) * 2f; // -1 to 1
        float dy = (rng.nextFloat() - 0.5f) * 2f; // -1 to 1
        float dz = -rng.nextFloat(); // 0 to -1 (down)

        // Normalize
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len > 0.0001f) {
            dx /= len;
            dy /= len;
            dz /= len;
        } else {
            dx = 0f;
            dy = 0f;
            dz = -1f;
        }

        float targetX = startNode.x() + dx * 2f;
        float targetY = startNode.y() + dy * 2f;
        org.swarmforge.core.domain.Terrarium terrarium = simulation != null ? simulation.getTerrarium() : null;
        int maxZ = terrarium != null ? terrarium.getDepth() - 1 : 100;
        float targetZ = Math.max(0.0f, Math.min(maxZ, startNode.z() + dz * 2f));
        org.swarmforge.core.simulation.SoilStructureSystem soilSystem = simulation != null ? simulation.getSoilStructureSystem() : null;

        boolean excavationSuccess = true;
        if (terrarium != null && soilSystem != null) {
            float compaction = colony.getSpecies() != null ? Math.min(90.0f, colony.getSpecies().getMandibularBitingForceMPa() * 3.0f) : 50.0f;
            int vx = (int) targetX;
            int vy = (int) targetY;
            int vz = (int) targetZ;
            excavationSuccess = soilSystem.digGalleryVoxel(terrarium, vx, vy, vz, compaction);
        }

        if (excavationSuccess) {
            // Dig 2 units away and connect gallery node
            network.dig(startNode.id(), dx * 2, dy * 2, dz * 2, TunnelNetwork.ChamberType.TUNNEL);
            individual.setEnergy(Math.max(0f, individual.getEnergy() - 0.01f));
            if (individual.getCarriedItem() == Individual.CarriedItem.NONE) {
                individual.setCarriedItem(Individual.CarriedItem.EARTH);
            }
        } else {
            // Tunnel collapsed under shear stress: extra metabolic exertion
            individual.setEnergy(Math.max(0f, individual.getEnergy() - 0.03f));
        }
    }
}
