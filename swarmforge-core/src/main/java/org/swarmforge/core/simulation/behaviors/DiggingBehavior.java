/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
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

    private final Random random = new Random();

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

        var startNode = nodes.get(random.nextInt(nodes.size()));

        // Dig in random direction, mostly down or sideways
        float dx = (random.nextFloat() - 0.5f) * 2f; // -1 to 1
        float dy = (random.nextFloat() - 0.5f) * 2f; // -1 to 1
        float dz = -random.nextFloat(); // 0 to -1 (down)

        // Normalize
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        dx /= len;
        dy /= len;
        dz /= len;

        // Dig 2 units away
        network.dig(startNode.id(), dx * 2, dy * 2, dz * 2, TunnelNetwork.ChamberType.TUNNEL);

        // Cost energy
        individual.setEnergy(individual.getEnergy() - 2f);
    }
}
