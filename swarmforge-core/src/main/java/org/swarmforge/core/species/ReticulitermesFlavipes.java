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
 * Reticulitermes flavipes - Eastern Subterranean Termite
 * Subterranean eusocial insect, builds shelter tubes from soil and fecal cement, feeds on cellulose.
 * Refactored to extend CustomSpecies for full JSON & Species Editor parameter compatibility.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ReticulitermesFlavipes extends CustomSpecies {

    public ReticulitermesFlavipes() {
        setPresetName("Termite Souterrain (Reticulitermes flavipes)");
        setCommonName("Eastern Subterranean Termite");
        setScientificName("Reticulitermes flavipes");
        setInsectType("TERMITE");
        setDescription("Eusocial insect of the order Isoptera. Queen and King present in royal chamber, feeding on cellulose.");
        setQueenCountMode("MONOGYNE");
        setQueenCount(1);
        setHasKing(true);
        setKingLifespan(365 * 20);
        setQueenLifespan(365 * 25);
        setQueenEggLayingRate(60.0f);
        setWorkerLifespan(365 * 2);
        setWorkerSpeed(0.35f);
        setViewDistance(1.5f);
        setTypicalColonySize(250000);
        setPrimaryDiet("WOOD_CELLULOSE");
        setSecondaryDiet("FUNGUS");
        setNestType("WOOD_TUNNELS");
        setVenomType("POWERFUL_MANDIBLES");
        setAggression(0.4f);

        setHasTermiteGutSymbiosis(true);
        setHasProctodealTrophallaxis(true);
        setHasMagnetoreception(true);
        setMagnetoreceptionSensitivity(2.5f); // High sensitivity to geomagnetic field
        setThermoreceptionSensitivity(0.2f); // High thermal gradient sensitivity for subterranean chambers
        setGasSensitivityCo2Ppm(350.0f); // High sensitivity to CO2 accumulation
        setVisualAcuity(0.2f); // Reduced vision in subterranean workers
        setMinLightLevelThreshold(0.01f);

        CasteTemplate termiteQueen = new CasteTemplate("Reine Physogastre", 600f, 2f);
        termiteQueen.setBodyLengthMm(22.0f);
        termiteQueen.setHeadWidthMm(3.0f);

        CasteTemplate termiteKing = new CasteTemplate("Roi Reproducteur", 300f, 5f);
        termiteKing.setBodyLengthMm(10.0f);
        termiteKing.setHeadWidthMm(2.0f);

        CasteTemplate termiteWorker = new CasteTemplate("Ouvrier Termite", 50f, 3f);
        termiteWorker.setCanDig(true);
        termiteWorker.setBodyLengthMm(5.0f);
        termiteWorker.setHeadWidthMm(1.2f);

        CasteTemplate termiteSoldier = new CasteTemplate("Soldat à Mandiboles", 200f, 35f);
        termiteSoldier.setBaseDefense(6f);
        termiteSoldier.setBodyLengthMm(7.0f);
        termiteSoldier.setHeadWidthMm(2.5f);

        setCasteTemplates(List.of(termiteQueen, termiteKing, termiteWorker, termiteSoldier));
    }
}
