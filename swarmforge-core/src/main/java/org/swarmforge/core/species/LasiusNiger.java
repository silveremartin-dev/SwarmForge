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
 * Lasius niger - Black Garden Ant
 * Common European ant species, excellent for simulation.
 * Refactored to extend CustomSpecies for full JSON & Species Editor parameter compatibility.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class LasiusNiger extends CustomSpecies {

    public LasiusNiger() {
        setPresetName("Fourmi Noire des Jardins (Lasius niger)");
        setCommonName("Black Garden Ant");
        setScientificName("Lasius niger");
        setInsectType("ANT");
        setDescription("Espèce monogyne très répandue en Europe. Élevage de pucerons et récolte de miellat.");
        setQueenCountMode("MONOGYNE");
        setQueenCount(1);
        setQueenLifespan(365 * 15);
        setQueenEggLayingRate(25.0f);
        setWorkerLifespan(365 * 3);
        setWorkerSpeed(0.5f);
        setViewDistance(3.0f);
        setTypicalColonySize(15000);
        setFormsMegaColonies(false);
        setPrimaryDiet("HONEYDEW");
        setSecondaryDiet("INSECTS_MEAT");
        setNestType("UNDERGROUND_BURROW");
        setVenomType("FORMIC_ACID");
        setAggression(0.3f);
        setCanPerformTandemRunning(true);

        CasteTemplate queen = new CasteTemplate("Reine", 500f, 10f);
        queen.setDescription("Reine fondatrice (9mm)");
        queen.setLifespan(365 * 15);
        queen.setBodyLengthMm(9.0f);
        queen.setHeadWidthMm(2.4f);

        CasteTemplate worker = new CasteTemplate("Ouvrière", 80f, 4f);
        worker.setDescription("Ouvrière généraliste (4mm)");
        worker.setLifespan(365 * 3);
        worker.setCanDig(true);
        worker.setCanCarry(true);
        worker.setBodyLengthMm(4.0f);
        worker.setHeadWidthMm(1.0f);

        setCasteTemplates(List.of(queen, worker));
    }
}
