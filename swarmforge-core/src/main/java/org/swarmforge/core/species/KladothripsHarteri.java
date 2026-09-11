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
        super("Kladothrips", InsectOrder.THRIPS);
        setPresetName("Thrips Eusocial (Kladothrips harteri)");
        setCommonName("Acacia Gall Thrips");
        setScientificName("Kladothrips harteri");
        setDescription("Australian gall thrips featuring a wingless soldier caste defending acacia galls against parasites.");
        setQueenCountMode("MONOGYNE");
        setQueenCount(1);
        setQueenLifespan(200);
        setWorkerLifespan(90);
        setWorkerSpeed(0.3f);
        setViewDistance(2.0f);
        setWorkersCanFly(true);
        setTypicalColonySize(300);
        setAggression(0.7f);

        // Ethological capabilities
        setCanSqueezeGallIntrudersThrips(true);
        setCanRepairGallSubstratalSecretion(true);
        setCanPlugGallWithChitinousTube(true);
        setCanSecreteGallClosingFluid(true);

        CasteTemplate foundress = new CasteTemplate("Gall Foundress", 180f, 3f);
        foundress.setLifespan(200);
        foundress.setCanFly(true);
        foundress.setWalkSpeedMps(0.20f);
        foundress.setFlySpeedMps(2.0f);
        foundress.setBodyLengthMm(3.0f);
        foundress.setHeadWidthMm(0.8f);

        CasteTemplate wingedWorker = new CasteTemplate("Ouvrier Dispersant Alé", 40f, 2f);
        wingedWorker.setLifespan(90);
        wingedWorker.setCanFly(true);
        wingedWorker.setWalkSpeedMps(0.20f);
        wingedWorker.setFlySpeedMps(2.5f);
        wingedWorker.setBodyLengthMm(2.5f);
        wingedWorker.setHeadWidthMm(0.7f);

        CasteTemplate soldier = new CasteTemplate("Wingless Gall Soldier", 120f, 18f);
        soldier.setLifespan(120);
        soldier.setCanFly(false);
        soldier.setBaseDefense(5f);
        soldier.setWalkSpeedMps(0.25f);
        soldier.setFlySpeedMps(0.0f);
        soldier.setBodyLengthMm(2.2f);
        soldier.setHeadWidthMm(0.9f);

        setCasteTemplates(List.of(foundress, wingedWorker, soldier));
    }

    @Override
    public InsectOrder getInsectOrder() {
        return InsectOrder.THRIPS;
    }
}
