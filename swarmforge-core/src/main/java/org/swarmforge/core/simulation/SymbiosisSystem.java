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
            if (colony.getSpecies().canFarmAphids()) {
                // Trophobiosis bonus: small continuous carbohydrate influx when foragers maintain herds
                float foodStock = colony.getFoodStored();
                if (colony.getPopulation() > 10) {
                    colony.setFoodStored(foodStock + 0.1f);
                }
            }

            if (colony.getSpecies().canFarmFungus()) {
                // Attine / Macrotermes fungal cultivar dynamics
                float foodStock = colony.getFoodStored();
                if (colony.getPopulation() > 20) {
                    colony.setFoodStored(foodStock + 0.2f);
                }
            }
        }
    }
}
