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
 * Camponotus - Carpenter Ant
 * Large ants that nest in wood, primarily nocturnal foragers.
 * Refactored to extend CustomSpecies for full JSON & Species Editor parameter compatibility.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class Camponotus extends CustomSpecies {

    public Camponotus() {
        setPresetName("Fourmi Charpentière (Camponotus)");
        setCommonName("Carpenter Ant");
        setScientificName("Camponotus");
        setInsectType("ANT");
        setDescription("Large ants nesting in dead wood, primarily nocturnal.");
        setQueenCountMode("MONOGYNE");
        setQueenCount(1);
        setQueenLifespan(365 * 25);
        setQueenEggLayingRate(30.0f);
        setWorkerLifespan(365 * 7);
        setWorkerSpeed(0.45f);
        setViewDistance(3.5f);
        setTypicalColonySize(10000);
        setFormsMegaColonies(false);
        setPrimaryDiet("HONEYDEW");
        setSecondaryDiet("INSECTS_MEAT");
        setNestType("WOOD_TUNNELS");
        setVenomType("POWERFUL_MANDIBLES");
        setAggression(0.4f);

        CasteTemplate queen = new CasteTemplate("Reine", 700f, 15f);
        queen.setLifespan(365 * 25);
        queen.setBodyLengthMm(18.0f);
        queen.setHeadWidthMm(4.5f);

        CasteTemplate worker = new CasteTemplate("Ouvrière Major", 180f, 18f);
        worker.setLifespan(365 * 7);
        worker.setCanDig(true);
        worker.setCanCarry(true);
        worker.setBodyLengthMm(12.0f);
        worker.setHeadWidthMm(3.5f);

        setCasteTemplates(List.of(queen, worker));
    }
}
