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
        super("Reticulitermes", InsectOrder.TERMITE);
        setPresetName("Termite Souterrain (Reticulitermes flavipes)");
        setCommonName("Eastern Subterranean Termite");
        setScientificName("Reticulitermes flavipes");
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
        setAggression(0.4f);
        setOptimalTempCelsius(25.0f);
        setMinTempCelsius(10.0f);
        setMaxTempCelsius(36.0f);

        // Ethological capabilities
        setHasTermiteGutSymbiosis(true);
        setHasProctodealTrophallaxis(true);
        setCanTrophallaxisProtozoa(true);
        setCanDrumSubstrate(true);
        setCanSynchronizeSoldierAlarmDrumming(true);
        setCanBlockRoyalChamberSentry(true);
        setCanPlasterWoodWallGallery(true);
        setCanPerformQueenPhysogastricPeristalsis(true);
        setCanExchangeRoyalPairGrooming(true);
        setHasMagnetoreception(true);
        setMagnetoreceptionSensitivity(2.5f); // High sensitivity to geomagnetic field
        setThermoreceptionSensitivity(0.2f); // High thermal gradient sensitivity for subterranean chambers
        setGasSensitivityCo2Ppm(350.0f); // High sensitivity to CO2 accumulation
        setVisualAcuity(0.2f); // Reduced vision in subterranean workers
        setMinLightLevelThreshold(0.01f);

        CasteTemplate termiteQueen = new CasteTemplate("Reine Physogastre", 600f, 2f);
        termiteQueen.setWalkSpeedMps(0.15f);
        termiteQueen.setFlySpeedMps(0.0f);
        termiteQueen.setBodyLengthMm(22.0f);
        termiteQueen.setHeadWidthMm(3.0f);

        CasteTemplate termiteKing = new CasteTemplate("Roi Reproducteur", 300f, 5f);
        termiteKing.setWalkSpeedMps(0.18f);
        termiteKing.setFlySpeedMps(0.0f);
        termiteKing.setBodyLengthMm(10.0f);
        termiteKing.setHeadWidthMm(2.0f);

        CasteTemplate termiteWorker = new CasteTemplate("Ouvrier Termite", 50f, 3f);
        termiteWorker.setCanDig(true);
        termiteWorker.setWalkSpeedMps(0.25f);
        termiteWorker.setFlySpeedMps(0.0f);
        termiteWorker.setBodyLengthMm(5.0f);
        termiteWorker.setHeadWidthMm(1.2f);

        CasteTemplate termiteSoldier = new CasteTemplate("Soldat à Mandiboles", 200f, 35f);
        termiteSoldier.setBaseDefense(6f);
        termiteSoldier.setWalkSpeedMps(0.20f);
        termiteSoldier.setFlySpeedMps(0.0f);
        termiteSoldier.setBodyLengthMm(7.0f);
        termiteSoldier.setHeadWidthMm(2.5f);

        CasteTemplate termiteAlate = new CasteTemplate("Alé Reproducteur (Essaimage)", 60f, 1f);
        termiteAlate.setDescription("Individu ailé reproducteur participant au vol de dispersion printanier.");
        termiteAlate.setCanFly(true);
        termiteAlate.setWalkSpeedMps(0.20f);
        termiteAlate.setFlySpeedMps(2.5f);
        termiteAlate.setBodyLengthMm(6.0f);
        termiteAlate.setHeadWidthMm(1.3f);

        setCasteTemplates(List.of(termiteQueen, termiteKing, termiteWorker, termiteSoldier, termiteAlate));
    }
}
