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
 * Kladothrips harteri - Eusocial Acacia Gall Thrips
 * Australian gall-inducing thrips with a morphologically distinct wingless soldier caste.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class KladothripsHarteri extends CustomSpecies {

    public KladothripsHarteri() {
        setPresetName("Thrips Eusocial (Kladothrips harteri)");
        setCommonName("Acacia Gall Thrips");
        setScientificName("Kladothrips harteri");
        setInsectType("THRIPS");
        setDescription("Thrips gallicole australien présentant une caste de soldats aptères défendant la galle d'acacia contre les parasites.");
        setQueenCountMode("MONOGYNE");
        setQueenCount(1);
        setQueenLifespan(200);
        setQueenEggLayingRate(30.0f);
        setWorkerLifespan(90 * 24);
        setWorkerSpeed(0.3f);
        setViewDistance(2.0f);
        setWorkersCanFly(true);
        setTypicalColonySize(300);
        setPrimaryDiet("PLANT_SAP");
        setSecondaryDiet("LEAF");
        setNestType("PLANT_GALL");
        setVenomType("POWERFUL_FORELEGS");
        setAggression(0.7f);

        CasteTemplate foundress = new CasteTemplate("Fondatrice de Galle", 180f, 3f);
        foundress.setCanFly(true);
        foundress.setBodyLengthMm(3.0f);
        foundress.setHeadWidthMm(0.8f);

        CasteTemplate wingedWorker = new CasteTemplate("Ouvrier Dispersant Alé", 40f, 2f);
        wingedWorker.setCanFly(true);
        wingedWorker.setBodyLengthMm(2.5f);
        wingedWorker.setHeadWidthMm(0.7f);

        CasteTemplate soldier = new CasteTemplate("Soldat Aptère de Galle", 120f, 18f);
        soldier.setCanFly(false);
        soldier.setBaseDefense(5f);
        soldier.setBodyLengthMm(2.2f);
        soldier.setHeadWidthMm(0.9f);

        setCasteTemplates(List.of(foundress, wingedWorker, soldier));
    }

    @Override
    public InsectOrder getInsectOrder() {
        return InsectOrder.THRIPS;
    }
}
