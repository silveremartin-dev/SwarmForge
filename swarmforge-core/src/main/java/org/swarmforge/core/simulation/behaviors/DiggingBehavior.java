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

import java.util.Arrays;
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
        // Only workers dig, and only if not already carrying soil or payload
        if (individual.getCaste() != Individual.Caste.WORKER)
            return 0f;

        if (individual.getCarriedItem() != Individual.CarriedItem.NONE)
            return 0f;

        // Dig if population is high relative to nest size
        int population = colony.getLivingIndividuals().size();
        int tunnelNodes = colony.getTunnelNetwork().getNodeCount();

        // Desire more space if crowded (e.g. > 10 ants per node)
        float crowding = (float) population / Math.max(1, tunnelNodes * 10);

        return Math.min(1.0f, crowding);
    }

    public void execute(Individual individual, Simulation simulation, Colony colony) {
        if (individual == null || colony == null)
            return;

        TunnelNetwork network = colony.getTunnelNetwork();
        var nodes = network.getNodes();
        if (nodes.isEmpty())
            return;

        Random rng = (individual.getRandom() != null) 
                ? individual.getRandom() 
                : (simulation != null && simulation.getRandom() != null ? simulation.getRandom() : new Random(1337L));

        var startNode = nodes.get(rng.nextInt(nodes.size()));

        // Dig in random direction, biased downward and sideways
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

        int vx = (int) targetX;
        int vy = (int) targetY;
        int vz = (int) targetZ;

        // Check if destination cell is solid rock or impenetrable obstacle
        if (terrarium != null && terrarium.inBounds(vx, vy, vz)) {
            var currentMat = terrarium.getCell(vx, vy, vz).material();
            if (currentMat == org.swarmforge.core.domain.TerrariumCell.Material.ROCK) {
                // Impassable stone: abort without metabolic penalty
                return;
            }
        }

        boolean excavationSuccess = true;
        if (terrarium != null && soilSystem != null) {
            float compaction = colony.getSpecies() != null ? Math.min(90.0f, colony.getSpecies().getMandibularBitingForceMPa() * 3.0f) : 50.0f;
            excavationSuccess = soilSystem.digGalleryVoxel(terrarium, vx, vy, vz, compaction);
        }

        if (excavationSuccess) {
            // Determine if new area forms an expanded chamber or narrow tunnel
            TunnelNetwork.ChamberType nodeType = TunnelNetwork.ChamberType.TUNNEL;
            if (terrarium != null && isSurroundingVoxelHollow(terrarium, vx, vy, vz)) {
                nodeType = TunnelNetwork.ChamberType.BROOD_CHAMBER;
            }

            // Connect gallery node in topological graph
            network.dig(startNode.id(), dx * 2, dy * 2, dz * 2, nodeType);
            individual.setEnergy(Math.max(0f, individual.getEnergy() - 0.01f));

            // Ant picks up extracted soil pellet
            individual.setCarriedItem(Individual.CarriedItem.EARTH);

            // Deposit chemical construction marker / trail pheromone in the newly opened voxel
            if (terrarium != null && terrarium.inBounds(vx, vy, vz)) {
                var cell = terrarium.getCell(vx, vy, vz);
                float[] pheromones = Arrays.copyOf(cell.pheromones(), cell.pheromones().length);
                pheromones[org.swarmforge.core.domain.TerrariumCell.PHEROMONE_TRAIL] = Math.min(1.0f, pheromones[org.swarmforge.core.domain.TerrariumCell.PHEROMONE_TRAIL] + 0.3f);
                terrarium.setCell(new org.swarmforge.core.domain.TerrariumCell(
                        vx, vy, vz, cell.material(), pheromones, cell.temperature(), cell.humidity()));
            }
        } else {
            // Tunnel collapsed under shear stress: extra metabolic exertion
            individual.setEnergy(Math.max(0f, individual.getEnergy() - 0.03f));
        }
    }

    private boolean isSurroundingVoxelHollow(org.swarmforge.core.domain.Terrarium terrarium, int cx, int cy, int cz) {
        int airCount = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    int x = cx + dx, y = cy + dy, z = cz + dz;
                    if (terrarium.inBounds(x, y, z) && !terrarium.getCell(x, y, z).material().isSolid()) {
                        airCount++;
                    }
                }
            }
        }
        return airCount >= 8; // Dense cavity detected
    }
}
