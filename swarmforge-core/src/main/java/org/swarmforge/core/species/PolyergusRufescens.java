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
 * Polyergus rufescens - European Amazon / Slave-Making Ant
 * Obligate dulotic species with sickle-shaped mandibles for specialized slave-making raids against Formica fusca.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class PolyergusRufescens extends CustomSpecies {

    public PolyergusRufescens() {
        super("Polyergus", InsectOrder.ANT);
        setPresetName("Fourmi Amazone Duloce (Polyergus rufescens)");
        setCommonName("Amazon Slave-Making Ant");
        setScientificName("Polyergus rufescens");
        setDescription("Obligate slave-making ant with sickle mandibles, conducting summer raids to capture Formica brood.");
        setQueenCountMode("MONOGYNE");
        setQueenCount(1);
        setQueenLifespan(365 * 15);
        setQueenEggLayingRate(30.0f);
        setWorkerLifespan(365 * 2);
        setWorkerSpeed(0.85f);
        setViewDistance(6.0f);
        setTypicalColonySize(5000);
        setFormsMegaColonies(false);
        setPrimaryDiet("HONEYDEW");
        setSecondaryDiet("INSECTS_MEAT");
        setOptimalTempCelsius(22.0f);
        setMinTempCelsius(6.0f);
        setMaxTempCelsius(36.0f);
        setNestType("UNDERGROUND_BURROW");
        setVenomType("POWERFUL_MANDIBLES");
        setAggression(0.95f);

        // Ethological capabilities
        setIsSlaveMakingSpecies(true);
        setCanHarmonizeChcGestalt(true);
        setHasPreySizeSelectivePheromones(true);

        CasteTemplate queen = new CasteTemplate("Reine Amazone", 550f, 20f);
        queen.setLifespan(365 * 15);
        queen.setCanFly(true);
        queen.setWalkSpeedMps(0.25f);
        queen.setFlySpeedMps(3.0f);
        queen.setBodyLengthMm(9.5f);
        queen.setHeadWidthMm(2.8f);

        CasteTemplate raider = new CasteTemplate("Guerrière Amazone", 180f, 18f);
        raider.setDescription("Highly specialized warrior armed with falcate mandibles for decapitating defenders and rapturing cocoons.");
        raider.setBaseDefense(4f);
        raider.setWalkSpeedMps(0.55f);
        raider.setFlySpeedMps(0.0f);
        raider.setProteinCost(30f);
        raider.setCarbohydrateCost(40f);
        raider.setWaterCost(10f);
        raider.setAttribute("raid_speed_boost", 1.4f);
        raider.setAttribute("pupae_carrying_capacity", 2.0f);
        raider.setBodyLengthMm(7.0f);
        raider.setHeadWidthMm(2.1f);

        CasteTemplate male = new CasteTemplate("Mâle Reproducteur (Alé)", 60f, 0f);
        male.setDescription("Mâle ailé pour le vol nuptial");
        male.setLifespan(30);
        male.setCanFly(true);
        male.setWalkSpeedMps(0.25f);
        male.setFlySpeedMps(3.5f);
        male.setBodyLengthMm(6.5f);
        male.setHeadWidthMm(1.8f);

        setCasteTemplates(List.of(queen, raider, male));
    }
}
