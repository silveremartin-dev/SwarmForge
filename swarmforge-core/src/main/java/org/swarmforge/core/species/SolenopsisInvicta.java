/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.species;

import org.swarmforge.core.domain.CasteTemplate;
import java.util.List;

/**
 * Solenopsis invicta - Red Imported Fire Ant
 * Highly aggressive invasive species with painful venomous sting.
 * Refactored to extend CustomSpecies for full JSON & Species Editor parameter compatibility.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SolenopsisInvicta extends CustomSpecies {

    public SolenopsisInvicta() {
        setPresetName("Red Imported Fire Ant (Solenopsis invicta)");
        setCommonName("Fire Ant");
        setScientificName("Solenopsis invicta");
        setInsectType("ANT");
        setDescription("Highly aggressive species with venomous stings, often polygyne colonies forming raised mounds.");
        setQueenCountMode("POLYGYNE");
        setQueenCount(12);
        setQueenLifespan(365 * 7);
        setQueenEggLayingRate(80.0f);
        setWorkerLifespan(180);
        setWorkerSpeed(0.7f);
        setViewDistance(2.5f);
        setTypicalColonySize(250000);
        setFormsMegaColonies(true);
        setPrimaryDiet("INSECTS_MEAT");
        setSecondaryDiet("SUGARS_NECTAR");
        setNestType("MOUND");
        setVenomType("VENOMOUS_STING");
        setAggression(0.9f);
        setCanPerformBiostructures(true);

        CasteTemplate queen = new CasteTemplate("Reine", 450f, 15f);
        queen.setLifespan(365 * 7);
        queen.setBodyLengthMm(8.0f);
        queen.setHeadWidthMm(2.0f);

        CasteTemplate minor = new CasteTemplate("Ouvrière Mineure", 60f, 8f);
        minor.setLifespan(180);
        minor.setCanDig(true);
        minor.setBodyLengthMm(3.0f);
        minor.setHeadWidthMm(0.8f);

        CasteTemplate tank = new CasteTemplate("Major (Tank)", 300f, 25f);
        tank.setDescription("Heavily armored major worker built for combat.");
        tank.setBaseDefense(10f);
        tank.setBaseSpeed(0.5f);
        tank.setProteinCost(80f);
        tank.setCarbohydrateCost(60f);
        tank.setWaterCost(30f);
        tank.setAttribute("armor_piercing", 0.5f);
        tank.setBodyLengthMm(6.0f);
        tank.setHeadWidthMm(1.8f);

        setCasteTemplates(List.of(queen, minor, tank));
    }
}
