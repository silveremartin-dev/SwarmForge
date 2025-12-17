/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.species;

/**
 * Solenopsis invicta - Red Imported Fire Ant
 * Highly aggressive invasive species with painful venomous sting.
 */
public class SolenopsisInvicta implements Species {

    @Override
    public String getScientificName() {
        return "Solenopsis invicta";
    }

    @Override
    public String getCommonName() {
        return "Fire Ant";
    }

    @Override
    public int getWorkerLifespan() {
        return 180;
    } // ~6 months

    @Override
    public int getQueenLifespan() {
        return 365 * 7;
    } // ~7 years

    @Override
    public float getWorkerSpeed() {
        return 0.7f;
    } // Fast

    @Override
    public float getViewDistance() {
        return 2.5f;
    }

    @Override
    public int getTypicalColonySize() {
        return 250000;
    }

    @Override
    public boolean formsMegaColonies() {
        return true;
    }

    @Override
    public java.util.List<org.swarmforge.core.domain.CasteTemplate> getCastes() {
        var tank = new org.swarmforge.core.domain.CasteTemplate("Major (Tank)", 300f, 25f);
        tank.setDescription("Heavily armored major worker built for combat.");
        tank.setBaseDefense(10f);
        tank.setBaseSpeed(0.5f); // Slower
        tank.setProteinCost(80f);
        tank.setCarbohydrateCost(60f);
        tank.setWaterCost(30f);
        tank.setAttribute("armor_piercing", 0.5f);

        return java.util.List.of(tank);
    }
}
