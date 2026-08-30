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
        setPresetName("Guêpe Commune (Vespula germanica)");
        setCommonName("European Yellowjacket Wasp");
        setScientificName("Vespula germanica");
        setInsectType("WASP");
        setDescription("Flying carnivorous hunter constructing paper nests from woody pulp.");
        setQueenCountMode("MONOGYNE");
        setQueenCount(1);
        setQueenLifespan(365);
        setQueenEggLayingRate(40.0f);
        setWorkerLifespan(30 * 24);
        setWorkerSpeed(1.4f);
        setViewDistance(6.0f);
        setWorkersCanFly(true);
        setTypicalColonySize(4000);
        setPrimaryDiet("INSECTS_MEAT");
        setSecondaryDiet("SUGARS_NECTAR");
        setNestType("PAPER_NEST");
        setVenomType("VENOMOUS_STING");
        setAggression(0.85f);
        setCanPerformLarvalSalivaryTrophallaxis(true);

        CasteTemplate queen = new CasteTemplate("Fondatrice (Reine)", 450f, 20f);
        queen.setCanFly(true);
        queen.setBodyLengthMm(19.0f);
        queen.setHeadWidthMm(4.5f);

        CasteTemplate worker = new CasteTemplate("Ouvrière Chasseresse", 100f, 22f);
        worker.setLifespan(30 * 24);
        worker.setCanFly(true);
        worker.setBodyLengthMm(13.0f);
        worker.setHeadWidthMm(3.2f);

        setCasteTemplates(List.of(queen, worker));
    }
}
