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
        super("Lasius", InsectOrder.ANT);
        setPresetName("Black Garden Ant (Lasius niger)");
        setCommonName("Black Garden Ant");
        setScientificName("Lasius niger");
        setDescription("Widespread European monogyne ant species. Tends aphids and harvests honeydew.");
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
        setOptimalTempCelsius(22.0f);
        setMinTempCelsius(6.0f);
        setMaxTempCelsius(35.0f);

        // Ethological capabilities
        setCanPerformTandemRunning(true);
        setCanFarmAphids(true);
        setCanMilkAphidHoneydewStroking(true);
        setCanEnforceAphidSanitaryCordon(true);
        setCanPlugContaminatedGalleries(true);

        CasteTemplate queen = new CasteTemplate("Reine", 500f, 10f);
        queen.setDescription("Reine fondatrice (9mm)");
        queen.setLifespan(365 * 15);
        queen.setCanFly(true);
        queen.setWalkSpeedMps(0.25f);
        queen.setFlySpeedMps(3.0f);
        queen.setBodyLengthMm(9.0f);
        queen.setHeadWidthMm(2.4f);

        CasteTemplate worker = new CasteTemplate("Ouvrière", 80f, 4f);
        worker.setDescription("Ouvrière généraliste (4mm)");
        worker.setLifespan(365 * 3);
        worker.setCanDig(true);
        worker.setCanCarry(true);
        worker.setWalkSpeedMps(0.35f);
        worker.setFlySpeedMps(0.0f);
        worker.setBodyLengthMm(4.0f);
        worker.setHeadWidthMm(1.0f);

        CasteTemplate male = new CasteTemplate("Mâle Reproducteur (Alé)", 45f, 0f);
        male.setDescription("Mâle haploïde ailé pour le vol nuptial (4.5mm)");
        male.setLifespan(30);
        male.setCanFly(true);
        male.setWalkSpeedMps(0.20f);
        male.setFlySpeedMps(3.5f);
        male.setBodyLengthMm(4.5f);
        male.setHeadWidthMm(1.1f);

        setCasteTemplates(List.of(queen, worker, male));
    }

    @Override
    public boolean canFarmAphids() {
        return true;
    }
}
