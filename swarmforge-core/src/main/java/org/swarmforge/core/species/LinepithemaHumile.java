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
        setPresetName("Fourmi d'Argentine (Linepithema humile)");
        setCommonName("Argentine Ant");
        setScientificName("Linepithema humile");
        setInsectType("ANT");
        setDescription("Espèce invasive formant d'immenses supercolonies sans agressivité inter-nids.");
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
        setIsUnicolonial(true);

        CasteTemplate queen = new CasteTemplate("Reine", 300f, 8f);
        queen.setLifespan(365 * 10);
        queen.setBodyLengthMm(5.0f);
        queen.setHeadWidthMm(1.2f);

        CasteTemplate worker = new CasteTemplate("Ouvrière", 50f, 3f);
        worker.setLifespan(365);
        worker.setCanDig(true);
        worker.setBodyLengthMm(2.8f);
        worker.setHeadWidthMm(0.7f);

        setCasteTemplates(List.of(queen, worker));
    }
}
