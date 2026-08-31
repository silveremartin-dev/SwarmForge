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
 * Formica fusca - Dusky Slave Ant / Fourmi Cendrée
 * Common woodland species of Europe, primary host target for dulotic slave-making raids.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class FormicaFusca extends CustomSpecies {

    public FormicaFusca() {
        setPresetName("Fourmi Cendrée Hôte (Formica fusca)");
        setCommonName("Dusky Slave Ant");
        setScientificName("Formica fusca");
        setInsectType("ANT");
        setDescription("Common European woodland host species, primary target of Polyergus rufescens slave raids.");
        setQueenCountMode("POLYGYNE");
        setQueenCount(3);
        setQueenLifespan(365 * 10);
        setQueenEggLayingRate(40.0f);
        setWorkerLifespan(365 * 2);
        setWorkerSpeed(0.65f);
        setViewDistance(4.5f);
        setTypicalColonySize(2000);
        setFormsMegaColonies(false);
        setPrimaryDiet("HONEYDEW");
        setSecondaryDiet("INSECTS_MEAT");
        setOptimalTempCelsius(22.0f);
        setMinTempCelsius(0.0f);
        setMaxTempCelsius(40.0f);
        setNestType("SUBTERRANEAN");
        setVenomType("FORMIC_ACID");
        setAggression(0.35f);
        setCanPerformSocialThermoregulation(false);

        CasteTemplate queen = new CasteTemplate("Reine Cendrée", 450f, 15f);
        queen.setLifespan(365 * 10);
        queen.setBodyLengthMm(8.0f);
        queen.setHeadWidthMm(2.2f);

        CasteTemplate worker = new CasteTemplate("Ouvrière Cendrée", 120f, 12f);
        worker.setDescription("Agile, versatile worker managing foraging, brood care, and nest defense.");
        worker.setBaseDefense(1.5f);
        worker.setProteinCost(15f);
        worker.setCarbohydrateCost(30f);
        worker.setWaterCost(10f);
        worker.setAttribute("flee_threshold", 0.6f);
        worker.setBodyLengthMm(5.5f);
        worker.setHeadWidthMm(1.6f);

        setCasteTemplates(List.of(queen, worker));
    }
}
