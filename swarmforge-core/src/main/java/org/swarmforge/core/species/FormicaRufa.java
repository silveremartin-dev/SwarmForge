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
 * Formica rufa - Wood Ant
 * Large mound-building ants, aggressive defense with formic acid spray.
 * Refactored to extend CustomSpecies for full JSON & Species Editor parameter compatibility.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class FormicaRufa extends CustomSpecies {

    public FormicaRufa() {
        setPresetName("Fourmi Rousse des Bois (Formica rufa)");
        setCommonName("Wood Ant");
        setScientificName("Formica rufa");
        setInsectType("ANT");
        setDescription("Grande fourmi des forêts résineuses construisant de grands dômes d'aiguilles de pin.");
        setQueenCountMode("POLYGYNE");
        setQueenCount(5);
        setQueenLifespan(365 * 20);
        setQueenEggLayingRate(50.0f);
        setWorkerLifespan(365 * 2);
        setWorkerSpeed(0.6f);
        setViewDistance(4.0f);
        setTypicalColonySize(400000);
        setFormsMegaColonies(true);
        setPrimaryDiet("HONEYDEW");
        setSecondaryDiet("INSECTS_MEAT");
        setNestType("MOUND");
        setVenomType("FORMIC_ACID");
        setAggression(0.75f);
        setCanPerformSocialThermoregulation(true);

        CasteTemplate queen = new CasteTemplate("Reine", 600f, 15f);
        queen.setLifespan(365 * 20);
        queen.setBodyLengthMm(12.0f);
        queen.setHeadWidthMm(3.2f);

        CasteTemplate acidShooter = new CasteTemplate("Acid Shooter", 150f, 15f);
        acidShooter.setDescription("Specialized caste capable of spraying formic acid.");
        acidShooter.setBaseDefense(2f);
        acidShooter.setProteinCost(20f);
        acidShooter.setCarbohydrateCost(50f);
        acidShooter.setWaterCost(10f);
        acidShooter.setAttribute("acid_potency", 0.8f);
        acidShooter.setBodyLengthMm(9.0f);
        acidShooter.setHeadWidthMm(2.2f);

        setCasteTemplates(List.of(queen, acidShooter));
    }
}
