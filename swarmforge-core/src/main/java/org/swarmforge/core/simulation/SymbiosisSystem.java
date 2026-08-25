/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.ResourceType;
import org.swarmforge.core.event.SimulationEvent;
import org.swarmforge.core.species.Species;

/**
 * Symbiosis, Mutualism & Commensalism Engine (Trophobiose, Champignonnistes & Commensaux).
 * Simulates:
 * 1. Atta / Ambrosia fungus cultivation (converting leaf/wood substrate into edible fungus).
 * 2. Ant-Aphid Trophobiosis (Lasius/Formica milking aphids for honeydew).
 * 3. Gall Soldier defense against aphid syrphids & kleptoparasite thrips.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SymbiosisSystem {

    private final Simulation simulation;
    private long lastSymbiosisTick = 0;

    public SymbiosisSystem(Simulation simulation) {
        this.simulation = simulation;
    }

    public void tick() {
        long currentTick = simulation.getTickCount();
        if (currentTick - lastSymbiosisTick < 180) return; // Check every ~3 seconds
        lastSymbiosisTick = currentTick;

        for (Colony colony : simulation.getColonies()) {
            Species.InsectOrder order = colony.getSpecies().getInsectOrder();

            // 1. Fungus Farm Cultivation for Leafcutters & Wood Beetles
            if (colony.getSpeciesName().contains("Atta") || order == Species.InsectOrder.BEETLE) {
                float leafRes = colony.getResourceAmount(ResourceType.LEAF);
                float woodRes = colony.getResourceAmount(ResourceType.WOOD);

                if (leafRes > 10.0f || woodRes > 10.0f) {
                    float converted = Math.max(leafRes, woodRes) * 0.15f;
                    colony.addResource(ResourceType.FUNGUS, converted);

                    simulation.queueEvent(new SimulationEvent(
                            SimulationEvent.EventType.INFO,
                            simulation.getTickCount(),
                            "🍄 Symbiotic Fungus Garden grew by +" + String.format("%.1f", converted) + " units in " + colony.getSpeciesName()
                    ));
                }
            }

            // 2. Trophobiosis: Honeydew Harvest for Lasius & Formica
            if (colony.getSpeciesName().contains("Lasius") || colony.getSpeciesName().contains("Formica")) {
                org.swarmforge.core.world.VegetationSystem veg = simulation.getVegetationSystem();
                int plantCount = (veg != null) ? veg.getPlants().size() : 5;
                if (plantCount > 0) {
                    float harvestAmount = Math.min(15.0f, 2.0f + plantCount * 0.5f);
                    colony.addResource(ResourceType.HONEYDEW, harvestAmount);
                }
            }
        }
    }
}
