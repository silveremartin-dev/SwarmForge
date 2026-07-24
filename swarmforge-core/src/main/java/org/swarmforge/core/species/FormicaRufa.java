/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.species;

/**
 * Formica rufa - Wood Ant
 * Large mound-building ants, aggressive defense with formic acid spray.
 */
public class FormicaRufa implements Species {

    @Override
    public String getScientificName() {
        return "Formica rufa";
    }

    @Override
    public String getCommonName() {
        return "Wood Ant";
    }

    @Override
    public int getWorkerLifespan() {
        return 365 * 2;
    } // ~2 years

    @Override
    public int getQueenLifespan() {
        return 365 * 20;
    } // ~20 years

    @Override
    public float getWorkerSpeed() {
        return 0.6f;
    }

    @Override
    public float getViewDistance() {
        return 4.0f;
    }

    @Override
    public int getTypicalColonySize() {
        return 400000;
    } // Can be huge

    @Override
    public boolean formsMegaColonies() {
        return true;
    }

    @Override
    public java.util.List<org.swarmforge.core.domain.CasteTemplate> getCastes() {
        var acidShooter = new org.swarmforge.core.domain.CasteTemplate("Acid Shooter", 150f, 15f);
        acidShooter.setDescription("Specialized caste capable of spraying formic acid.");
        acidShooter.setBaseDefense(2f);
        acidShooter.setProteinCost(20f);
        acidShooter.setCarbohydrateCost(50f);
        acidShooter.setWaterCost(10f);
        acidShooter.setAttribute("acid_potency", 0.8f);

        return java.util.List.of(acidShooter);
    }
}
