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
 * European Hornet (Frelon européen - Vespa crabro).
 * Large apex eusocial aerial predator building wood-pulp paper nests,
 * hunting honeybees, caterpillars, and flies.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class VespaCrabro extends CustomSpecies {
    private static final long serialVersionUID = 1L;

    public VespaCrabro() {
        setPresetName("European Hornet (Vespa crabro)");
        setCommonName("European Hornet");
        setScientificName("Vespa crabro");
        setInsectType("WASP");
        setDescription("Large eusocial vespid, apex aerial predator constructing paper nests.");
        setQueenCountMode("MONOGYNE");
        setQueenCount(1);
        setQueenLifespan(365);
        setWorkerLifespan(30 * 24);
        setWorkerSpeed(1.5f);
        setViewDistance(9.0f);
        setWorkersCanFly(true);
        setTypicalColonySize(2000);
        setPrimaryDiet("INSECTS_MEAT");
        setSecondaryDiet("SUGARS_NECTAR");
        setNestType("PAPER_NEST");
        setVenomType("VENOMOUS_STING");
        setAggression(0.9f);

        // Fondatrice / Reine
        CasteTemplate queen = new CasteTemplate("Reine Frelon", 350f, 30f);
        queen.setCanFly(true);
        queen.setCanDig(false);
        queen.setCanCarry(true);
        queen.setBodyLengthMm(30.0f);
        queen.setHeadWidthMm(6.0f);
        queen.setVenomType("NEUROTOXIN");
        queen.setVenomToxicity(25.0f);

        // Ouvrière Chasseuse
        CasteTemplate worker = new CasteTemplate("Ouvrière Frelon", 200f, 20f);
        worker.setCanFly(true);
        worker.setCanDig(false);
        worker.setCanCarry(true);
        worker.setBodyLengthMm(22.0f);
        worker.setHeadWidthMm(4.8f);
        worker.setVenomType("NEUROTOXIN");
        worker.setVenomToxicity(20.0f);

        // Mâle / Faux-bourdon
        CasteTemplate male = new CasteTemplate("Male Hornet", 150f, 5f);
        male.setCanFly(true);
        male.setCanDig(false);
        male.setCanCarry(false);
        male.setBodyLengthMm(24.0f);
        male.setHeadWidthMm(4.5f);

        setCasteTemplates(List.of(queen, worker, male));
    }
}
