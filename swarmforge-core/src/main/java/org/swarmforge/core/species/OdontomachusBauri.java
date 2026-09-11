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
 * Odontomachus bauri - Trap-Jaw Ant
 * Ponerine predator with ultra-fast spring-loaded latch-mediated mandible strikes (up to 60 m/s)
 * used for stunning prey and ballistic catapult escape jumps when threatened.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public class OdontomachusBauri extends CustomSpecies {

    public OdontomachusBauri() {
        setPresetName("Fourmi Mâchoire-Piège (Odontomachus bauri)");
        setCommonName("Trap-Jaw Ant");
        setScientificName("Odontomachus bauri");
        setInsectType("ANT");
        setDescription("Predatory ant with spring-loaded catapult mandibles snapping shut at ultra-high velocity for predation and escape jumps.");
        setQueenCountMode("MONOGYNE");
        setQueenCount(1);
        setQueenLifespan(365 * 8);
        setWorkerLifespan(365 * 2);
        setWorkerSpeed(0.75f);
        setViewDistance(6.5f);
        setTypicalColonySize(200);
        setFormsMegaColonies(false);
        setPrimaryDiet("INSECTS_MEAT");
        setSecondaryDiet("SUGARS_NECTAR");
        setOptimalTempCelsius(26.0f);
        setMinTempCelsius(12.0f);
        setMaxTempCelsius(38.0f);
        setOptimalHumidityPercent(85.0f);
        setMinHumidityPercent(50.0f);
        setMaxHumidityPercent(98.0f);
        setNestType("DEAD_WOOD_LEAF_LITTER");
        setVenomType("VENOMOUS_STING");
        setAggression(0.85f);
        setMandibularBitingForceMPa(45.0f);

        // Ethological & Biomechanical specializations
        setHasTrapJawMechanism(true);
        setCanSnapTrapMandiblesCatapult(true);

        CasteTemplate queen = new CasteTemplate("Reine Trap-Jaw", 500f, 25f);
        queen.setLifespan(365 * 8);
        queen.setWalkSpeedMps(0.3f);
        queen.setBodyLengthMm(14.0f);
        queen.setHeadWidthMm(3.0f);

        CasteTemplate hunter = new CasteTemplate("Ouvrière Chasseresse", 120f, 20f);
        hunter.setDescription("Solitary nocturnal and crepuscular hunter armed with 180° spring-loaded trap jaws.");
        hunter.setLifespan(365 * 2);
        hunter.setBaseDefense(5f);
        hunter.setWalkSpeedMps(0.6f);
        hunter.setFlySpeedMps(0.0f);
        hunter.setProteinCost(35f);
        hunter.setCarbohydrateCost(25f);
        hunter.setWaterCost(15f);
        hunter.setBodyLengthMm(12.0f);
        hunter.setHeadWidthMm(2.4f);

        CasteTemplate male = new CasteTemplate("Mâle Reproducteur (Alé)", 45f, 0f);
        male.setDescription("Mâle ailé");
        male.setLifespan(30);
        male.setCanFly(true);
        male.setWalkSpeedMps(0.25f);
        male.setFlySpeedMps(3.0f);
        male.setBodyLengthMm(8.0f);
        male.setHeadWidthMm(1.8f);

        setCasteTemplates(List.of(queen, hunter, male));
    }
}
