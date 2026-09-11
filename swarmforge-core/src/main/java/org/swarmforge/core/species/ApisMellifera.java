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
 * Apis mellifera - Western Honey Bee
 * Eusocial aerial nest builder, dances to communicate nectar locations, forms winter thermal clusters.
 * Refactored to extend CustomSpecies for full JSON & Species Editor parameter compatibility.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ApisMellifera extends CustomSpecies {

    public ApisMellifera() {
        super("Apis", InsectOrder.BEE);
        setPresetName("Abeille à Miel (Apis mellifera)");
        setCommonName("Western Honey Bee");
        setScientificName("Apis mellifera");
        setDescription("Eusocial flying insect constructing wax combs. Forages nectar and pollen.");
        setQueenCountMode("MONOGYNE");
        setQueenCount(1);
        setQueenLifespan(365 * 4);
        setQueenEggLayingRate(150.0f);
        setNuptialFlightType("SWARM_DIVISION");
        setWorkerLifespan(45);
        setWorkerSpeed(1.2f);
        setViewDistance(8.0f);
        setWorkersCanFly(true);
        setTypicalColonySize(50000);
        setAggression(0.2f);
        setOptimalTempCelsius(25.0f);
        setMinTempCelsius(10.0f);
        setMaxTempCelsius(38.0f);

        // Ethological capabilities
        setCanPerformWaggleDance(true);
        setCanEncodeWaggleDanceSunCompass(true);
        setCanPerformTrembleDance(true);
        setCanCollectPropolis(true);
        setCanSealNestGapsWithPropolis(true);
        setCanPerformQueenPiping(true);
        setCanDouseNestWaterCooling(true);
        setCanShieldSwarmCoreHeat(true);
        setCanPerformSocialThermoregulation(true);
        setCanFanWingsForBroodThermoregulation(true);
        setCanPerformThoracicIncubation(true);
        setCanPackCorbiculaPollenBaskets(true);
        setCanLapNectarTongueExtension(true);
        setHasElectrosensing(true);
        setHasPolarizedLightNavigation(true);
        setCanPerformEvaporativeCooling(true);
        setCanPerformNecrophoresis(true);

        CasteTemplate queen = new CasteTemplate("Reine Abeille", 400f, 5f);
        queen.setLifespan(365 * 4);
        queen.setCanFly(true);
        queen.setWalkSpeedMps(0.20f);
        queen.setFlySpeedMps(4.5f);
        queen.setBodyLengthMm(20.0f);
        queen.setHeadWidthMm(4.0f);

        CasteTemplate worker = new CasteTemplate("Ouvrière Butineuse", 90f, 12f);
        worker.setLifespan(45);
        worker.setCanFly(true);
        worker.setCanCarry(true);
        worker.setWalkSpeedMps(0.25f);
        worker.setFlySpeedMps(6.5f);
        worker.setBodyLengthMm(14.0f);
        worker.setHeadWidthMm(3.5f);

        CasteTemplate drone = new CasteTemplate("Drone (Male)", 120f, 0f);
        drone.setLifespan(30);
        drone.setCanFly(true);
        drone.setWalkSpeedMps(0.20f);
        drone.setFlySpeedMps(7.0f);
        drone.setBodyLengthMm(16.0f);
        drone.setHeadWidthMm(4.2f);

        setCasteTemplates(List.of(queen, worker, drone));
    }
}
