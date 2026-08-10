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
 * Buff-Tailed Bumblebee (Bourdon terrestre - Bombus terrestris).
 * Eusocial hymenopteran featuring buzz pollination, cold-tolerant flight,
 * and subterranean/cavity wax-pot nests.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class BombusTerrestris extends CustomSpecies {
    private static final long serialVersionUID = 1L;

    public BombusTerrestris() {
        setPresetName("Bourdon Terrestre (Bombus terrestris)");
        setCommonName("Buff-Tailed Bumblebee");
        setScientificName("Bombus terrestris");
        setInsectType("BEE");
        setDescription("Bourdon eusocial robuste capable de thermorégulation et de pollinisation vibratile.");
        setQueenCountMode("MONOGYNE");
        setQueenCount(1);
        setQueenLifespan(365);
        setWorkerLifespan(40 * 24);
        setWorkerSpeed(1.1f);
        setViewDistance(7.0f);
        setWorkersCanFly(true);
        setTypicalColonySize(400);
        setPrimaryDiet("SUGARS_NECTAR");
        setSecondaryDiet("SEEDS");
        setNestType("WAX_COMB");
        setVenomType("VENOMOUS_STING");
        setAggression(0.15f);

        // Queen
        CasteTemplate queen = new CasteTemplate("Reine Bourdon", 250f, 15f);
        queen.setCanFly(true);
        queen.setCanDig(true);
        queen.setCanCarry(true);
        queen.setBodyLengthMm(22.0f);
        queen.setHeadWidthMm(4.5f);

        // Worker
        CasteTemplate worker = new CasteTemplate("Ouvrière Bourdon", 120f, 8f);
        worker.setCanFly(true);
        worker.setCanDig(false);
        worker.setCanCarry(true);
        worker.setBodyLengthMm(14.0f);
        worker.setHeadWidthMm(3.2f);

        // Male / Drone
        CasteTemplate male = new CasteTemplate("Mâle Bourdon", 100f, 2f);
        male.setCanFly(true);
        male.setCanDig(false);
        male.setCanCarry(false);
        male.setBodyLengthMm(15.0f);
        male.setHeadWidthMm(3.0f);

        setCasteTemplates(List.of(queen, worker, male));
    }
}
