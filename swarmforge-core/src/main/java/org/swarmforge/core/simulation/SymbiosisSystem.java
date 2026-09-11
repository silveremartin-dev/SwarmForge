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

        org.swarmforge.core.world.VegetationSystem vegSystem = simulation.getVegetationSystem();
        int plantCount = vegSystem != null ? vegSystem.getPlantCount() : 10;
        org.swarmforge.core.domain.Terrarium terrarium = simulation.getTerrarium();

        for (Colony colony : simulation.getColonies()) {
            if (colony.getSpecies() == null) continue;

            // 1. Trophobiosis: Aphid honeydew harvesting linked to living plant vegetation & foragers
            if (colony.getSpecies().canFarmAphids()) {
                boolean hasAphidHerd = simulation.getFoodSources().stream()
                        .anyMatch(f -> f instanceof Aphid || f.getType() == org.swarmforge.core.domain.ResourceType.HONEYDEW);
                int foragers = colony.getPopulation();
                if ((hasAphidHerd || plantCount > 0) && foragers > 0) {
                    // Sap conversion rate scaled by available flora and tending workforce
                    float sapYield = Math.min(1.0f, (plantCount / 20.0f) * 0.4f);
                    colony.addResource(org.swarmforge.core.domain.ResourceType.HONEYDEW, sapYield);
                }
            }

            // 2. Attine / Macrotermes fungal cultivar dynamics & bio-thermogenesis
            if (colony.getSpecies().canFarmFungus()) {
                FungusGarden garden = colony.getFungusGarden();
                if (garden != null && garden.getHealth() > 0.2f && terrarium != null) {
                    // Fungal bio-thermogenesis: metabolizing mycelium releases heat (+0.3°C) and CO2 into nest chamber
                    int nx = (int) colony.getNestX();
                    int ny = (int) colony.getNestY();
                    int nz = (int) colony.getNestZ();
                    if (terrarium.inBounds(nx, ny, nz)) {
                        org.swarmforge.core.domain.TerrariumCell cell = terrarium.getCell(nx, ny, nz);
                        float updatedCo2 = Math.min(5000.0f, cell.co2() + 15.0f * garden.getHealth());
                        float updatedTemp = Math.min(28.0f, cell.temperature() + 0.1f * garden.getHealth());
                        terrarium.setCell(new org.swarmforge.core.domain.TerrariumCell(
                                nx, ny, nz, cell.material(), cell.pheromones(),
                                updatedTemp, cell.humidity(), updatedCo2, cell.o2(), cell.n2o(),
                                cell.light(), cell.windX(), cell.windY(), cell.pressure()
                        ));
                    }
                }
            }
        }
    }
}
