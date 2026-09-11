/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;

import java.util.logging.Logger;

/**
 * Ecological Symbiosis & Mutualism System.
 * Simulates inter-species mutualisms including trophobiosis (aphid honeydew harvesting)
 * and attine fungal garden cultivation (*Leucoagaricus gongylophorus*).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SymbiosisSystem {

    private static final Logger LOG = Logger.getLogger(SymbiosisSystem.class.getName());

    private final Simulation simulation;
    private long lastSymbiosisTick = 0;

    public SymbiosisSystem(Simulation simulation) {
        this.simulation = simulation;
    }

    /**
     * Executes periodic mutualism updates across colonies.
     */
    public void tick() {
        long currentTick = simulation.getTickCount();
        if (currentTick - lastSymbiosisTick < 120) { // Check every ~2 seconds
            return;
        }
        lastSymbiosisTick = currentTick;

        for (Colony colony : simulation.getColonies()) {
            if (colony.getSpecies() != null && colony.getSpecies().canFarmAphids()) {
                // Trophobiosis: carbohydrate and honeydew influx when colony maintains aphid herds or has active foragers
                boolean hasAphids = simulation.getFoodSources().stream()
                        .anyMatch(f -> f instanceof Aphid || f.getType() == org.swarmforge.core.domain.ResourceType.HONEYDEW);
                if (hasAphids || colony.getPopulation() > 0) {
                    colony.addResource(org.swarmforge.core.domain.ResourceType.HONEYDEW, 0.5f);
                }
            }

            if (colony.getSpecies() != null && colony.getSpecies().canFarmFungus()) {
                // Attine / Macrotermes fungal cultivar dynamics
                colony.addResource(org.swarmforge.core.domain.ResourceType.FUNGUS, 0.5f);
            }
        }
    }
}
