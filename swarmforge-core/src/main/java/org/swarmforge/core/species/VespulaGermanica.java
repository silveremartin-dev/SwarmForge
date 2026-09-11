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
 * Vespula germanica - German Wasp / Yellowjacket
 * Predatory paper wasp, annual colony cycle, high aggression.
 * Refactored to extend CustomSpecies for full JSON & Species Editor parameter compatibility.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class VespulaGermanica extends CustomSpecies {

    public VespulaGermanica() {
        super("Vespula", InsectOrder.WASP);
        setPresetName("Guêpe Commune (Vespula germanica)");
        setCommonName("European Yellowjacket Wasp");
        setScientificName("Vespula germanica");
        setDescription("Flying carnivorous hunter constructing paper nests from woody pulp.");
        setQueenCountMode("MONOGYNE");
        setQueenCount(1);
        setQueenLifespan(365);
        setQueenEggLayingRate(40.0f);
        setWorkerLifespan(30);
        setWorkerSpeed(1.4f);
        setViewDistance(6.0f);
        setWorkersCanFly(true);
        setTypicalColonySize(4000);
        setAggression(0.85f);
        setOptimalTempCelsius(25.0f);
        setMinTempCelsius(12.0f);
        setMaxTempCelsius(38.0f);

        // Ethological capabilities
        setCanPerformLarvalSalivaryTrophallaxis(true);
        setCanMasticatePaperPulpCarton(true);
        setCanScrapeWoodPulpCarton(true);
        setCanHarvestLarvalSalivaDroplets(true);
        setCanApplyPedicelAntRepellent(true);
        setCanCoatWaspPedicelAntRepellent(true);
        setCanRecognizeFacialVisualPatterns(true);
        setCanDrumAbdomenWaspCellRim(true);

        CasteTemplate queen = new CasteTemplate("Fondatrice (Reine)", 450f, 20f);
        queen.setLifespan(365);
        queen.setCanFly(true);
        queen.setWalkSpeedMps(0.25f);
        queen.setFlySpeedMps(6.5f);
        queen.setBodyLengthMm(19.0f);
        queen.setHeadWidthMm(4.5f);

        CasteTemplate worker = new CasteTemplate("Ouvrière Chasseresse", 100f, 22f);
        worker.setLifespan(30);
        worker.setCanFly(true);
        worker.setWalkSpeedMps(0.25f);
        worker.setFlySpeedMps(6.0f);
        worker.setBodyLengthMm(13.0f);
        worker.setHeadWidthMm(3.2f);

        CasteTemplate male = new CasteTemplate("Mâle / Faux-Bourdon", 90f, 0f);
        male.setDescription("Mâle haploïde sans dard");
        male.setLifespan(30);
        male.setCanFly(true);
        male.setWalkSpeedMps(0.20f);
        male.setFlySpeedMps(6.5f);
        male.setBodyLengthMm(15.0f);
        male.setHeadWidthMm(3.5f);

        setCasteTemplates(List.of(queen, worker, male));
    }
}
