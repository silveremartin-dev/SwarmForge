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
 * Linepithema humile - Argentine Ant
 * Invasive species known for forming supercolonies.
 * Refactored to extend CustomSpecies for full JSON & Species Editor parameter compatibility.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class LinepithemaHumile extends CustomSpecies {

    public LinepithemaHumile() {
        super("Linepithema", InsectOrder.ANT);
        setPresetName("Fourmi d'Argentine (Linepithema humile)");
        setCommonName("Argentine Ant");
        setScientificName("Linepithema humile");
        setDescription("Invasive species forming massive supercolonies with zero inter-nest aggression.");
        setQueenCountMode("POLYGYNE");
        setQueenCount(20);
        setQueenLifespan(365 * 10);
        setWorkerLifespan(365);
        setWorkerSpeed(0.7f);
        setViewDistance(2.5f);
        setTypicalColonySize(100000);
        setFormsMegaColonies(true);
        setPrimaryDiet("HONEYDEW");
        setSecondaryDiet("INSECTS_MEAT");
        setNestType("UNDERGROUND_BURROW");
        setAggression(0.6f);
        setOptimalTempCelsius(24.0f);
        setMinTempCelsius(8.0f);
        setMaxTempCelsius(38.0f);

        // Ethological capabilities
        setIsUnicolonial(true);
        setHasThermalTrailDecay(true);
        setHasTerritorialRepellentPheromone(true);

        CasteTemplate queen = new CasteTemplate("Reine", 300f, 8f);
        queen.setLifespan(365 * 10);
        queen.setWalkSpeedMps(0.25f);
        queen.setFlySpeedMps(0.0f);
        queen.setBodyLengthMm(5.0f);
        queen.setHeadWidthMm(1.2f);

        CasteTemplate worker = new CasteTemplate("Ouvrière", 50f, 3f);
        worker.setLifespan(365);
        worker.setCanDig(true);
        worker.setWalkSpeedMps(0.45f);
        worker.setFlySpeedMps(0.0f);
        worker.setBodyLengthMm(2.8f);
        worker.setHeadWidthMm(0.7f);

        CasteTemplate male = new CasteTemplate("Mâle Reproducteur (Alé)", 35f, 0f);
        male.setDescription("Mâle ailé (3mm)");
        male.setLifespan(30);
        male.setCanFly(true);
        male.setWalkSpeedMps(0.25f);
        male.setFlySpeedMps(2.5f);
        male.setBodyLengthMm(3.0f);
        male.setHeadWidthMm(0.8f);

        setCasteTemplates(List.of(queen, worker, male));
    }
}
