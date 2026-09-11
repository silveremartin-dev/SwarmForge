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
 * Formica rufa - Wood Ant
 * Large mound-building ants, aggressive defense with formic acid spray.
 * Refactored to extend CustomSpecies for full JSON & Species Editor parameter compatibility.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class FormicaRufa extends CustomSpecies {

    public FormicaRufa() {
        super("Formica", InsectOrder.ANT);
        setPresetName("Red Wood Ant (Formica rufa)");
        setCommonName("Wood Ant");
        setScientificName("Formica rufa");
        setDescription("Large coniferous forest ant constructing large mounds of pine needles.");
        setQueenCountMode("POLYGYNE");
        setQueenCount(5);
        setQueenLifespan(365 * 20);
        setQueenEggLayingRate(50.0f);
        setWorkerLifespan(365 * 2);
        setWorkerSpeed(0.6f);
        setViewDistance(4.0f);
        setTypicalColonySize(400000);
        setFormsMegaColonies(true);
        setPrimaryDiet("HONEYDEW");
        setSecondaryDiet("INSECTS_MEAT");
        setNestType("MOUND");
        setVenomType("FORMIC_ACID");
        setAggression(0.75f);
        setOptimalTempCelsius(22.0f);
        setMinTempCelsius(6.0f);
        setMaxTempCelsius(34.0f);

        // Ethological capabilities
        setCanSprayFormicResinDisinfectant(true);
        setCanFireFormicAcidArtilleryJet(true);
        setCanFarmAphids(true);
        setCanMilkAphidHoneydewStroking(true);
        setCanClusterSolarHeatCollector(true);
        setHasSolarOrientedMound(true);
        setIsPolycalic(true);
        setCanPerformThoracicIncubation(true);
        setCanPerformSocialThermoregulation(true);

        CasteTemplate queen = new CasteTemplate("Reine", 600f, 15f);
        queen.setLifespan(365 * 20);
        queen.setCanFly(true);
        queen.setWalkSpeedMps(0.25f);
        queen.setFlySpeedMps(3.0f);
        queen.setBodyLengthMm(12.0f);
        queen.setHeadWidthMm(3.2f);

        CasteTemplate worker = new CasteTemplate("Ouvrière Généraliste", 100f, 8f);
        worker.setDescription("Ouvrière des bois active dans la récolte et la construction du dôme.");
        worker.setLifespan(365 * 2);
        worker.setCanDig(true);
        worker.setCanCarry(true);
        worker.setWalkSpeedMps(0.45f);
        worker.setFlySpeedMps(0.0f);
        worker.setBodyLengthMm(7.0f);
        worker.setHeadWidthMm(1.8f);

        CasteTemplate acidShooter = new CasteTemplate("Acid Shooter", 150f, 15f);
        acidShooter.setDescription("Specialized caste capable of spraying formic acid.");
        acidShooter.setBaseDefense(2f);
        acidShooter.setWalkSpeedMps(0.42f);
        acidShooter.setFlySpeedMps(0.0f);
        acidShooter.setProteinCost(20f);
        acidShooter.setCarbohydrateCost(50f);
        acidShooter.setWaterCost(10f);
        acidShooter.setAttribute("acid_potency", 0.8f);
        acidShooter.setBodyLengthMm(9.0f);
        acidShooter.setHeadWidthMm(2.2f);

        CasteTemplate male = new CasteTemplate("Mâle Reproducteur (Alé)", 60f, 0f);
        male.setDescription("Mâle ailé pour le vol nuptial");
        male.setLifespan(30);
        male.setCanFly(true);
        male.setWalkSpeedMps(0.25f);
        male.setFlySpeedMps(3.5f);
        male.setBodyLengthMm(9.0f);
        male.setHeadWidthMm(2.0f);

        setCasteTemplates(List.of(queen, worker, acidShooter, male));
    }
}
