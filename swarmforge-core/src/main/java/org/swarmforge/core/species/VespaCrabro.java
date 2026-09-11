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
        super("Vespa", InsectOrder.WASP);
        setPresetName("European Hornet (Vespa crabro)");
        setCommonName("European Hornet");
        setScientificName("Vespa crabro");
        setDescription("Large eusocial vespid, apex aerial predator constructing paper nests.");
        setQueenCountMode("MONOGYNE");
        setQueenCount(1);
        setQueenLifespan(365);
        setWorkerLifespan(30);
        setWorkerSpeed(1.5f);
        setViewDistance(9.0f);
        setWorkersCanFly(true);
        setTypicalColonySize(2000);
        setAggression(0.9f);
        setOptimalTempCelsius(26.0f);
        setMinTempCelsius(12.0f);
        setMaxTempCelsius(40.0f);

        // Ethological capabilities
        setCanEmitHornetGroupAlarmPheromone(true);
        setCanDrumAbdomenWaspCellRim(true);
        setCanMasticatePaperPulpCarton(true);
        setCanScrapeWoodPulpCarton(true);
        setCanHarvestLarvalSalivaDroplets(true);
        setCanApplyPedicelAntRepellent(true);
        setCanCoatWaspPedicelAntRepellent(true);
        setCanRecognizeFacialVisualPatterns(true);

        // Fondatrice / Reine
        CasteTemplate queen = new CasteTemplate("Reine Frelon", 350f, 30f);
        queen.setLifespan(365);
        queen.setCanFly(true);
        queen.setCanDig(false);
        queen.setCanCarry(true);
        queen.setWalkSpeedMps(0.25f);
        queen.setFlySpeedMps(7.0f);
        queen.setBodyLengthMm(30.0f);
        queen.setHeadWidthMm(6.0f);
        queen.setVenomType("NEUROTOXIN");
        queen.setVenomToxicity(25.0f);

        // Ouvrière Chasseuse
        CasteTemplate worker = new CasteTemplate("Ouvrière Frelon", 200f, 20f);
        worker.setLifespan(30);
        worker.setCanFly(true);
        worker.setCanDig(false);
        worker.setCanCarry(true);
        worker.setWalkSpeedMps(0.25f);
        worker.setFlySpeedMps(7.5f);
        worker.setBodyLengthMm(22.0f);
        worker.setHeadWidthMm(4.8f);
        worker.setVenomType("NEUROTOXIN");
        worker.setVenomToxicity(20.0f);

        // Mâle / Faux-bourdon
        CasteTemplate male = new CasteTemplate("Male Hornet", 150f, 5f);
        male.setLifespan(30);
        male.setCanFly(true);
        male.setCanDig(false);
        male.setCanCarry(false);
        male.setWalkSpeedMps(0.20f);
        male.setFlySpeedMps(7.5f);
        male.setBodyLengthMm(24.0f);
        male.setHeadWidthMm(4.5f);

        setCasteTemplates(List.of(queen, worker, male));
    }
}
