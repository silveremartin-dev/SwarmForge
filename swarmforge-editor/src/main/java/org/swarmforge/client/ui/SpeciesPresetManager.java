/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.swarmforge.core.domain.CasteTemplate;
import org.swarmforge.core.species.CustomSpecies;

import java.io.File;
import java.util.*;

/**
 * Manages Species presets: built-in biological defaults + user custom presets.
 * Persists presets to {@code species_presets.json} in the working directory.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SpeciesPresetManager {

    public static final File PRESETS_FILE = new File("species_presets.json");

    private final Map<String, CustomSpecies> presets = new LinkedHashMap<>();

    public SpeciesPresetManager() {
        loadAll();
    }

    private void loadAll() {
        presets.clear();
        presets.putAll(createBuiltins());

        if (PRESETS_FILE.exists()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                Map<String, CustomSpecies> saved = mapper.readValue(PRESETS_FILE, new TypeReference<LinkedHashMap<String, CustomSpecies>>() {});
                presets.putAll(saved);
            } catch (Exception e) {
                System.err.println("[SpeciesPresetManager] Could not load " + PRESETS_FILE + ": " + e.getMessage());
            }
        }
        
        // Register all presets in core SpeciesRegistry
        for (CustomSpecies species : presets.values()) {
            org.swarmforge.core.species.SpeciesRegistry.getInstance().register(species);
        }
    }

    private Map<String, CustomSpecies> createBuiltins() {
        Map<String, CustomSpecies> map = new LinkedHashMap<>();

        // 1. Black Garden Ant (Lasius niger)
        CustomSpecies lasius = new CustomSpecies();
        lasius.setPresetName("Black Garden Ant (Lasius niger)");
        lasius.setCommonName("Black Garden Ant");
        lasius.setScientificName("Lasius niger");
        lasius.setInsectType("ANT");
        lasius.setDescription("Widespread monogynous species in Europe. Tends aphids and harvests honeydew.");
        lasius.setQueenCountMode("MONOGYNE");
        lasius.setQueenCount(1);
        lasius.setQueenLifespan(30000);
        lasius.setQueenEggLayingRate(25.0f);
        lasius.setNuptialFlightType("AERIAL_SWARM");
        lasius.setWorkerLifespan(6000);
        lasius.setWorkerSpeed(0.5f);
        lasius.setTypicalColonySize(15000);
        lasius.setFormsMegaColonies(false);
        lasius.setPrimaryDiet("HONEYDEW");
        lasius.setSecondaryDiet("INSECTS_MEAT");
        lasius.setDailyFoodConsumption(0.3f);
        lasius.setWaterRequirement(0.15f);
        lasius.setNestType("MATURE");
        lasius.setVenomType("FORMIC_ACID");
        lasius.setAggression(0.3f);
        lasius.setTerritoriality(0.45f);
        lasius.setOptimalTempCelsius(24.0f);
        lasius.setMinTempCelsius(5.0f);
        lasius.setMaxTempCelsius(35.0f);
        lasius.setEggStageDuration(15);
        lasius.setLarvaStageDuration(14);
        lasius.setPupaStageDuration(14);
        lasius.setLarvaDietRequirement("High Protein Meat & Insects");
        lasius.setProteinThresholdMinor(0.30f);
        lasius.setProteinThresholdMajor(0.65f);
        lasius.setProteinThresholdSoldier(0.80f);
        lasius.setProteinThresholdQueen(0.90f);
        lasius.setQueenPheromoneInhibitionFactor(0.85f);
        lasius.setHaplodiploidyEnabled(true);
        lasius.setPathogenResistance(0.60f);
        lasius.setGroomingDefenseEfficacy(0.75f);
        lasius.setHasMagnetoreception(false);
        lasius.setThermoreceptionSensitivity(0.4f);
        lasius.setGasSensitivityCo2Ppm(500.0f);
        lasius.setVisualAcuity(1.2f);
        lasius.setMinLightLevelThreshold(0.05f);
        lasius.setHasSubstrateVibrationSensing(true);
        lasius.setVibrationSensitivityDb(8.0f);
        lasius.setHasHygroreception(true);
        lasius.setHygroreceptionSensitivityPercent(1.5f);
        lasius.setHasElectrosensing(false);
        lasius.setHasPolarizedLightNavigation(true);
        lasius.setWingbeatFrequencyHz(180.0f);
        lasius.setHasHoveringCapability(false);
        lasius.setMaxCarryingPayloadRatio(5.0f);
        lasius.setMandibularBitingForceMPa(12.0f);
        lasius.setHasAutothysis(false);
        lasius.setHasSubstrateAdhesionArolia(true);
        lasius.setViewDistance(6.0f);
        lasius.setMetabolism(1.0f);
        lasius.setStrength(5.0f);

        CasteTemplate lasiusQueen = new CasteTemplate("Queen", 500f, 10f);
        lasiusQueen.setDescription("Founding Queen / Gyne (9mm)");
        lasiusQueen.setLifespan(30000);
        lasiusQueen.setCanFly(true);
        lasiusQueen.setBodyLengthMm(9.0f);
        lasiusQueen.setHeadWidthMm(2.4f);
        lasiusQueen.setWingbeatFrequencyHz(180.0f);
        lasiusQueen.setHasHoveringCapability(false);
        lasiusQueen.setMaxCarryingPayloadRatio(2.5f);
        lasiusQueen.setMandibularBitingForceMPa(8.0f);
        lasiusQueen.setTargetRatio(0.00f);

        CasteTemplate lasiusWorker = new CasteTemplate("Worker", 80f, 4f);
        lasiusWorker.setDescription("Generalist Worker (4mm)");
        lasiusWorker.setLifespan(6000);
        lasiusWorker.setCanDig(true);
        lasiusWorker.setCanCarry(true);
        lasiusWorker.setBodyLengthMm(4.0f);
        lasiusWorker.setHeadWidthMm(1.0f);
        lasiusWorker.setWingbeatFrequencyHz(0.0f);
        lasiusWorker.setMaxCarryingPayloadRatio(5.0f);
        lasiusWorker.setMandibularBitingForceMPa(12.0f);
        lasiusWorker.setTargetRatio(1.00f);

        CasteTemplate lasiusMale = new CasteTemplate("Reproductive Male (Alate)", 45f, 0f);
        lasiusMale.setDescription("Haploid winged male for nuptial flight (4.5mm)");
        lasiusMale.setLifespan(500);
        lasiusMale.setCanFly(true);
        lasiusMale.setBodyLengthMm(4.5f);
        lasiusMale.setHeadWidthMm(1.1f);
        lasiusMale.setWingbeatFrequencyHz(210.0f);
        lasiusMale.setMaxCarryingPayloadRatio(1.0f);
        lasiusMale.setMandibularBitingForceMPa(1.0f);
        lasiusMale.setTargetRatio(0.00f);

        lasius.setCasteTemplates(List.of(lasiusQueen, lasiusWorker, lasiusMale));
        map.put(lasius.getPresetName(), lasius);

        // 2. Red Imported Fire Ant (Solenopsis invicta)
        CustomSpecies solenopsis = new CustomSpecies();
        solenopsis.setPresetName("Red Imported Fire Ant (Solenopsis invicta)");
        solenopsis.setCommonName("Red Imported Fire Ant");
        solenopsis.setScientificName("Solenopsis invicta");
        solenopsis.setInsectType("ANT");
        solenopsis.setDescription("Highly aggressive species with venomous sting, forming polygynous colonies and tall mounds.");
        solenopsis.setQueenCountMode("POLYGYNE");
        solenopsis.setQueenCount(12);
        solenopsis.setQueenLifespan(20000);
        solenopsis.setQueenEggLayingRate(80.0f);
        solenopsis.setNuptialFlightType("AERIAL_SWARM");
        solenopsis.setWorkerLifespan(4000);
        solenopsis.setWorkerSpeed(0.75f);
        solenopsis.setTypicalColonySize(250000);
        solenopsis.setFormsMegaColonies(true);
        solenopsis.setPrimaryDiet("INSECTS_MEAT");
        solenopsis.setSecondaryDiet("SUGARS_NECTAR");
        solenopsis.setDailyFoodConsumption(0.55f);
        solenopsis.setWaterRequirement(0.25f);
        solenopsis.setNestType("MOUND");
        solenopsis.setVenomType("VENOMOUS_STING");
        solenopsis.setAggression(0.9f);
        solenopsis.setTerritoriality(0.85f);
        solenopsis.setOptimalTempCelsius(27.0f);
        solenopsis.setMinTempCelsius(12.0f);
        solenopsis.setMaxTempCelsius(42.0f);
        solenopsis.setEggStageDuration(8);
        solenopsis.setLarvaStageDuration(10);
        solenopsis.setPupaStageDuration(12);
        solenopsis.setLarvaDietRequirement("High Protein Meat & Insects");
        solenopsis.setProteinThresholdMinor(0.25f);
        solenopsis.setProteinThresholdMajor(0.60f);
        solenopsis.setProteinThresholdSoldier(0.75f);
        solenopsis.setProteinThresholdQueen(0.90f);
        solenopsis.setQueenPheromoneInhibitionFactor(0.60f);
        solenopsis.setHaplodiploidyEnabled(true);
        solenopsis.setPathogenResistance(0.70f);
        solenopsis.setGroomingDefenseEfficacy(0.80f);
        solenopsis.setHasMagnetoreception(true);
        solenopsis.setMagnetoreceptionSensitivity(4.0f);
        solenopsis.setThermoreceptionSensitivity(0.3f);
        solenopsis.setGasSensitivityCo2Ppm(450.0f);
        solenopsis.setVisualAcuity(1.0f);
        solenopsis.setMinLightLevelThreshold(0.05f);
        solenopsis.setHasSubstrateVibrationSensing(true);
        solenopsis.setVibrationSensitivityDb(6.0f);
        solenopsis.setHasHygroreception(true);
        solenopsis.setHygroreceptionSensitivityPercent(1.0f);
        solenopsis.setHasElectrosensing(false);
        solenopsis.setHasPolarizedLightNavigation(true);
        solenopsis.setWingbeatFrequencyHz(190.0f);
        solenopsis.setHasHoveringCapability(false);
        solenopsis.setMaxCarryingPayloadRatio(8.0f);
        solenopsis.setMandibularBitingForceMPa(35.0f);
        solenopsis.setHasAutothysis(false);
        solenopsis.setHasSubstrateAdhesionArolia(true);
        solenopsis.setViewDistance(7.5f);
        solenopsis.setMetabolism(1.3f);
        solenopsis.setStrength(8.0f);

        CasteTemplate fireQueen = new CasteTemplate("Queen", 450f, 15f);
        fireQueen.setLifespan(20000);
        fireQueen.setCanFly(true);
        fireQueen.setBodyLengthMm(8.0f);
        fireQueen.setHeadWidthMm(2.0f);
        fireQueen.setWingbeatFrequencyHz(190.0f);
        fireQueen.setMaxCarryingPayloadRatio(2.0f);
        fireQueen.setMandibularBitingForceMPa(10.0f);
        fireQueen.setTargetRatio(0.00f);

        CasteTemplate fireMinor = new CasteTemplate("Minor Worker", 60f, 8f);
        fireMinor.setLifespan(3500);
        fireMinor.setCanDig(true);
        fireMinor.setBodyLengthMm(3.0f);
        fireMinor.setHeadWidthMm(0.8f);
        fireMinor.setWingbeatFrequencyHz(0.0f);
        fireMinor.setMaxCarryingPayloadRatio(4.0f);
        fireMinor.setMandibularBitingForceMPa(8.0f);
        fireMinor.setTargetRatio(0.85f);

        CasteTemplate fireMajor = new CasteTemplate("Major Worker / Soldier", 150f, 25f);
        fireMajor.setLifespan(5000);
        fireMajor.setBaseDefense(5f);
        fireMajor.setBodyLengthMm(6.0f);
        fireMajor.setHeadWidthMm(1.8f);
        fireMajor.setWingbeatFrequencyHz(0.0f);
        fireMajor.setMaxCarryingPayloadRatio(8.0f);
        fireMajor.setMandibularBitingForceMPa(35.0f);
        fireMajor.setTargetRatio(0.15f);

        solenopsis.setCasteTemplates(List.of(fireQueen, fireMinor, fireMajor));
        map.put(solenopsis.getPresetName(), solenopsis);

        // 3. Leafcutter Ant (Atta sexdens)
        CustomSpecies atta = new CustomSpecies();
        atta.setPresetName("Leafcutter Ant (Atta sexdens)");
        atta.setCommonName("Leafcutter Ant");
        atta.setScientificName("Atta sexdens");
        atta.setInsectType("ANT");
        atta.setDescription("Harvests foliage to cultivate a symbiotic fungus garden inside massive underground vaults.");
        atta.setQueenCountMode("MONOGYNE");
        atta.setQueenCount(1);
        atta.setQueenLifespan(35000);
        atta.setQueenEggLayingRate(100.0f);
        atta.setNuptialFlightType("AERIAL_SWARM");
        atta.setWorkerLifespan(7000);
        atta.setWorkerSpeed(0.6f);
        atta.setTypicalColonySize(500000);
        atta.setFormsMegaColonies(false);
        atta.setPrimaryDiet("FUNGUS");
        atta.setSecondaryDiet("SUGARS_NECTAR");
        atta.setDailyFoodConsumption(0.9f);
        atta.setWaterRequirement(0.35f);
        atta.setNestType("SUBTERRANEAN_FUNGI_VAULT");
        atta.setVenomType("POWERFUL_MANDIBLES");
        atta.setAggression(0.5f);
        atta.setTerritoriality(0.55f);
        atta.setOptimalTempCelsius(26.0f);
        atta.setMinTempCelsius(15.0f);
        atta.setMaxTempCelsius(38.0f);
        atta.setEggStageDuration(20);
        atta.setLarvaStageDuration(25);
        atta.setPupaStageDuration(25);
        atta.setLarvaDietRequirement("Fungus Garden Mycelium");
        atta.setProteinThresholdMinor(0.20f);
        atta.setProteinThresholdMajor(0.55f);
        atta.setProteinThresholdSoldier(0.80f);
        atta.setProteinThresholdQueen(0.95f);
        atta.setQueenPheromoneInhibitionFactor(0.90f);
        atta.setHaplodiploidyEnabled(true);
        atta.setPathogenResistance(0.85f);
        atta.setGroomingDefenseEfficacy(0.90f);
        atta.setHasMagnetoreception(true);
        atta.setMagnetoreceptionSensitivity(3.0f);
        atta.setThermoreceptionSensitivity(0.2f);
        atta.setGasSensitivityCo2Ppm(300.0f);
        atta.setVisualAcuity(0.9f);
        atta.setMinLightLevelThreshold(0.02f);
        atta.setHasSubstrateVibrationSensing(true);
        atta.setVibrationSensitivityDb(5.0f);
        atta.setHasHygroreception(true);
        atta.setHygroreceptionSensitivityPercent(0.5f);
        atta.setHasElectrosensing(false);
        atta.setHasPolarizedLightNavigation(true);
        atta.setWingbeatFrequencyHz(110.0f);
        atta.setHasHoveringCapability(false);
        atta.setMaxCarryingPayloadRatio(20.0f);
        atta.setMandibularBitingForceMPa(120.0f);
        atta.setHasAutothysis(false);
        atta.setHasSubstrateAdhesionArolia(true);
        atta.setViewDistance(8.0f);
        atta.setMetabolism(1.5f);
        atta.setStrength(20.0f);

        CasteTemplate attaQueen = new CasteTemplate("Giant Queen", 1000f, 20f);
        attaQueen.setLifespan(35000);
        attaQueen.setCanFly(true);
        attaQueen.setBodyLengthMm(30.0f);
        attaQueen.setHeadWidthMm(7.0f);
        attaQueen.setWingbeatFrequencyHz(110.0f);
        attaQueen.setMaxCarryingPayloadRatio(1.5f);
        attaQueen.setMandibularBitingForceMPa(25.0f);
        attaQueen.setTargetRatio(0.00f);

        CasteTemplate attaMinima = new CasteTemplate("Minima Worker (Gardener)", 40f, 2f);
        attaMinima.setBodyLengthMm(2.0f);
        attaMinima.setHeadWidthMm(0.6f);
        attaMinima.setWingbeatFrequencyHz(0.0f);
        attaMinima.setMaxCarryingPayloadRatio(2.0f);
        attaMinima.setMandibularBitingForceMPa(2.0f);
        attaMinima.setTargetRatio(0.45f);

        CasteTemplate attaMedia = new CasteTemplate("Media Worker (Cutter)", 100f, 10f);
        attaMedia.setCanCarry(true);
        attaMedia.setBodyLengthMm(7.0f);
        attaMedia.setHeadWidthMm(2.2f);
        attaMedia.setWingbeatFrequencyHz(0.0f);
        attaMedia.setMaxCarryingPayloadRatio(20.0f);
        attaMedia.setMandibularBitingForceMPa(35.0f);
        attaMedia.setTargetRatio(0.45f);

        CasteTemplate attaMajor = new CasteTemplate("Major Soldier (Guard)", 300f, 45f);
        attaMajor.setBaseDefense(8f);
        attaMajor.setBodyLengthMm(16.0f);
        attaMajor.setHeadWidthMm(6.0f);
        attaMajor.setWingbeatFrequencyHz(0.0f);
        attaMajor.setMaxCarryingPayloadRatio(10.0f);
        attaMajor.setMandibularBitingForceMPa(120.0f);
        attaMajor.setTargetRatio(0.10f);

        atta.setCasteTemplates(List.of(attaQueen, attaMinima, attaMedia, attaMajor));
        map.put(atta.getPresetName(), atta);

        // 4. European Honey Bee (Apis mellifera)
        CustomSpecies apis = new CustomSpecies();
        apis.setPresetName("European Honey Bee (Apis mellifera)");
        apis.setCommonName("European Honey Bee");
        apis.setScientificName("Apis mellifera");
        apis.setInsectType("BEE");
        apis.setDescription("Flying eusocial insect building hexagonal wax combs. Forages nectar and pollen.");
        apis.setQueenCountMode("MONOGYNE");
        apis.setQueenCount(1);
        apis.setQueenLifespan(18000);
        apis.setQueenEggLayingRate(150.0f);
        apis.setNuptialFlightType("SWARM_DIVISION");
        apis.setWorkerLifespan(3000);
        apis.setWorkerSpeed(1.2f);
        apis.setWorkersCanFly(true);
        apis.setTypicalColonySize(50000);
        apis.setFormsMegaColonies(false);
        apis.setPrimaryDiet("SUGARS_NECTAR");
        apis.setSecondaryDiet("SEEDS");
        apis.setDailyFoodConsumption(0.4f);
        apis.setWaterRequirement(0.30f);
        apis.setNestType("WAX_COMB_HEXAGONAL");
        apis.setVenomType("VENOMOUS_STING");
        apis.setAggression(0.2f);
        apis.setTerritoriality(0.35f);
        apis.setOptimalTempCelsius(35.0f);
        apis.setMinTempCelsius(10.0f);
        apis.setMaxTempCelsius(42.0f);
        apis.setEggStageDuration(3);
        apis.setLarvaStageDuration(6);
        apis.setPupaStageDuration(12);
        apis.setLarvaDietRequirement("Sugars, Honey & Nectar");
        apis.setProteinThresholdMinor(0.30f);
        apis.setProteinThresholdMajor(0.65f);
        apis.setProteinThresholdSoldier(0.99f);
        apis.setProteinThresholdQueen(0.95f);
        apis.setQueenPheromoneInhibitionFactor(0.95f);
        apis.setHaplodiploidyEnabled(true);
        apis.setPathogenResistance(0.65f);
        apis.setGroomingDefenseEfficacy(0.80f);
        apis.setHasMagnetoreception(true);
        apis.setMagnetoreceptionSensitivity(2.0f);
        apis.setThermoreceptionSensitivity(0.1f);
        apis.setGasSensitivityCo2Ppm(250.0f);
        apis.setVisualAcuity(3.5f);
        apis.setMinLightLevelThreshold(0.10f);
        apis.setHasSubstrateVibrationSensing(true);
        apis.setVibrationSensitivityDb(4.0f);
        apis.setHasHygroreception(true);
        apis.setHygroreceptionSensitivityPercent(1.0f);
        apis.setHasElectrosensing(true);
        apis.setElectroceptionSensitivityVolts(15.0f);
        apis.setHasPolarizedLightNavigation(true);
        apis.setWingbeatFrequencyHz(230.0f);
        apis.setHasHoveringCapability(true);
        apis.setMaxCarryingPayloadRatio(6.0f);
        apis.setMandibularBitingForceMPa(5.0f);
        apis.setHasAutothysis(false);
        apis.setHasSubstrateAdhesionArolia(true);
        apis.setViewDistance(25.0f);
        apis.setMetabolism(1.8f);
        apis.setStrength(6.0f);

        CasteTemplate apisQueen = new CasteTemplate("Queen Bee", 400f, 5f);
        apisQueen.setLifespan(18000);
        apisQueen.setCanFly(true);
        apisQueen.setBodyLengthMm(20.0f);
        apisQueen.setHeadWidthMm(4.0f);
        apisQueen.setWingbeatFrequencyHz(180.0f);
        apisQueen.setHasHoveringCapability(false);
        apisQueen.setMaxCarryingPayloadRatio(1.5f);
        apisQueen.setMandibularBitingForceMPa(3.0f);
        apisQueen.setTargetRatio(0.00f);

        CasteTemplate apisWorker = new CasteTemplate("Forager Worker", 90f, 12f);
        apisWorker.setLifespan(3000);
        apisWorker.setCanFly(true);
        apisWorker.setCanCarry(true);
        apisWorker.setBodyLengthMm(14.0f);
        apisWorker.setHeadWidthMm(3.5f);
        apisWorker.setWingbeatFrequencyHz(230.0f);
        apisWorker.setHasHoveringCapability(true);
        apisWorker.setMaxCarryingPayloadRatio(6.0f);
        apisWorker.setMandibularBitingForceMPa(5.0f);
        apisWorker.setTargetRatio(1.00f);

        CasteTemplate apisDrone = new CasteTemplate("Drone (Male)", 120f, 0f);
        apisDrone.setLifespan(2500);
        apisDrone.setCanFly(true);
        apisDrone.setBodyLengthMm(16.0f);
        apisDrone.setHeadWidthMm(4.2f);
        apisDrone.setWingbeatFrequencyHz(200.0f);
        apisDrone.setHasHoveringCapability(false);
        apisDrone.setMaxCarryingPayloadRatio(1.0f);
        apisDrone.setMandibularBitingForceMPa(1.0f);
        apisDrone.setTargetRatio(0.00f);

        apis.setCasteTemplates(List.of(apisQueen, apisWorker, apisDrone));
        map.put(apis.getPresetName(), apis);

        // 5. Common Wasp (Vespula vulgaris)
        CustomSpecies vespula = new CustomSpecies();
        vespula.setPresetName("Common Wasp (Vespula vulgaris)");
        vespula.setCommonName("Common Wasp");
        vespula.setScientificName("Vespula vulgaris");
        vespula.setInsectType("WASP");
        vespula.setDescription("Carnivorous flying hunter building paper nests from wood pulp.");
        vespula.setQueenCountMode("MONOGYNE");
        vespula.setQueenCount(1);
        vespula.setQueenLifespan(12000);
        vespula.setQueenEggLayingRate(40.0f);
        vespula.setNuptialFlightType("AERIAL_SWARM");
        vespula.setWorkerLifespan(2500);
        vespula.setWorkerSpeed(1.4f);
        vespula.setWorkersCanFly(true);
        vespula.setTypicalColonySize(8000);
        vespula.setFormsMegaColonies(false);
        vespula.setPrimaryDiet("INSECTS_MEAT");
        vespula.setSecondaryDiet("SUGARS_NECTAR");
        vespula.setDailyFoodConsumption(0.45f);
        vespula.setWaterRequirement(0.20f);
        vespula.setNestType("PAPER_PEDUNCULATE");
        vespula.setVenomType("VENOMOUS_STING");
        vespula.setAggression(0.85f);
        vespula.setTerritoriality(0.70f);
        vespula.setOptimalTempCelsius(28.0f);
        vespula.setMinTempCelsius(8.0f);
        vespula.setMaxTempCelsius(40.0f);
        vespula.setEggStageDuration(5);
        vespula.setLarvaStageDuration(9);
        vespula.setPupaStageDuration(12);
        vespula.setLarvaDietRequirement("High Protein Meat & Insects");
        vespula.setProteinThresholdMinor(0.30f);
        vespula.setProteinThresholdMajor(0.60f);
        vespula.setProteinThresholdSoldier(0.99f);
        vespula.setProteinThresholdQueen(0.95f);
        vespula.setQueenPheromoneInhibitionFactor(0.80f);
        vespula.setHaplodiploidyEnabled(true);
        vespula.setPathogenResistance(0.55f);
        vespula.setGroomingDefenseEfficacy(0.65f);
        vespula.setHasMagnetoreception(false);
        vespula.setThermoreceptionSensitivity(0.3f);
        vespula.setGasSensitivityCo2Ppm(400.0f);
        vespula.setVisualAcuity(4.0f);
        vespula.setMinLightLevelThreshold(0.08f);
        vespula.setHasSubstrateVibrationSensing(true);
        vespula.setVibrationSensitivityDb(7.0f);
        vespula.setHasHygroreception(true);
        vespula.setHygroreceptionSensitivityPercent(2.0f);
        vespula.setHasElectrosensing(false);
        vespula.setHasPolarizedLightNavigation(true);
        vespula.setWingbeatFrequencyHz(210.0f);
        vespula.setHasHoveringCapability(true);
        vespula.setMaxCarryingPayloadRatio(5.0f);
        vespula.setMandibularBitingForceMPa(22.0f);
        vespula.setHasAutothysis(false);
        vespula.setHasSubstrateAdhesionArolia(true);
        vespula.setViewDistance(30.0f);
        vespula.setMetabolism(2.0f);
        vespula.setStrength(7.0f);

        CasteTemplate vespulaQueen = new CasteTemplate("Foundress (Queen)", 450f, 20f);
        vespulaQueen.setCanFly(true);
        vespulaQueen.setBodyLengthMm(19.0f);
        vespulaQueen.setHeadWidthMm(4.5f);
        vespulaQueen.setWingbeatFrequencyHz(160.0f);
        vespulaQueen.setHasHoveringCapability(true);
        vespulaQueen.setMaxCarryingPayloadRatio(3.0f);
        vespulaQueen.setMandibularBitingForceMPa(18.0f);
        vespulaQueen.setTargetRatio(0.00f);

        CasteTemplate vespulaWorker = new CasteTemplate("Hunter Worker", 100f, 22f);
        vespulaWorker.setLifespan(2500);
        vespulaWorker.setCanFly(true);
        vespulaWorker.setBodyLengthMm(13.0f);
        vespulaWorker.setHeadWidthMm(3.2f);
        vespulaWorker.setWingbeatFrequencyHz(210.0f);
        vespulaWorker.setHasHoveringCapability(true);
        vespulaWorker.setMaxCarryingPayloadRatio(5.0f);
        vespulaWorker.setMandibularBitingForceMPa(22.0f);
        vespulaWorker.setTargetRatio(1.00f);

        vespula.setCasteTemplates(List.of(vespulaQueen, vespulaWorker));
        map.put(vespula.getPresetName(), vespula);

        // 6. Subterranean Termite (Reticulitermes flavipes)
        CustomSpecies termite = new CustomSpecies();
        termite.setPresetName("Subterranean Termite (Reticulitermes flavipes)");
        termite.setCommonName("Eastern Subterranean Termite");
        termite.setScientificName("Reticulitermes flavipes");
        termite.setInsectType("TERMITE");
        termite.setDescription("Eusocial Isoptera species. Primary King and Queen in royal chamber, feeding on cellulose.");
        termite.setQueenCountMode("MONOGYNE");
        termite.setQueenCount(1);
        termite.setHasKing(true);
        termite.setKingLifespan(20000);
        termite.setQueenLifespan(30000);
        termite.setQueenEggLayingRate(60.0f);
        termite.setNuptialFlightType("AERIAL_SWARM");
        termite.setWorkerLifespan(5000);
        termite.setWorkerSpeed(0.4f);
        termite.setTypicalColonySize(100000);
        termite.setFormsMegaColonies(false);
        termite.setPrimaryDiet("WOOD_CELLULOSE");
        termite.setDailyFoodConsumption(0.6f);
        termite.setWaterRequirement(0.50f);
        termite.setNestType("CATHEDRAL_MOUND");
        termite.setVenomType("POWERFUL_MANDIBLES");
        termite.setAggression(0.4f);
        termite.setTerritoriality(0.60f);
        termite.setOptimalTempCelsius(28.0f);
        termite.setMinTempCelsius(15.0f);
        termite.setMaxTempCelsius(40.0f);
        termite.setEggStageDuration(24);
        termite.setLarvaStageDuration(30);
        termite.setPupaStageDuration(30);
        termite.setLarvaDietRequirement("Cellulose & Wood Fibers");
        termite.setProteinThresholdMinor(0.15f);
        termite.setProteinThresholdMajor(0.50f);
        termite.setProteinThresholdSoldier(0.75f);
        termite.setProteinThresholdQueen(0.90f);
        termite.setQueenPheromoneInhibitionFactor(0.85f);
        termite.setHaplodiploidyEnabled(false);
        termite.setPathogenResistance(0.70f);
        termite.setGroomingDefenseEfficacy(0.85f);
        termite.setHasMagnetoreception(true);
        termite.setMagnetoreceptionSensitivity(3.0f);
        termite.setThermoreceptionSensitivity(0.2f);
        termite.setGasSensitivityCo2Ppm(200.0f);
        termite.setVisualAcuity(0.2f);
        termite.setMinLightLevelThreshold(0.001f);
        termite.setHasSubstrateVibrationSensing(true);
        termite.setVibrationSensitivityDb(2.0f);
        termite.setHasHygroreception(true);
        termite.setHygroreceptionSensitivityPercent(0.5f);
        termite.setHasElectrosensing(false);
        termite.setHasPolarizedLightNavigation(false);
        termite.setWingbeatFrequencyHz(0.0f);
        termite.setHasHoveringCapability(false);
        termite.setMaxCarryingPayloadRatio(6.0f);
        termite.setMandibularBitingForceMPa(55.0f);
        termite.setHasAutothysis(false);
        termite.setHasSubstrateAdhesionArolia(true);
        termite.setViewDistance(2.0f);
        termite.setMetabolism(0.8f);
        termite.setStrength(6.0f);

        CasteTemplate termiteQueen = new CasteTemplate("Physogastric Queen", 600f, 2f);
        termiteQueen.setBodyLengthMm(22.0f);
        termiteQueen.setHeadWidthMm(3.0f);
        termiteQueen.setWingbeatFrequencyHz(0.0f);
        termiteQueen.setMaxCarryingPayloadRatio(0.5f);
        termiteQueen.setMandibularBitingForceMPa(2.0f);
        termiteQueen.setTargetRatio(0.00f);

        CasteTemplate termiteKing = new CasteTemplate("Reproductive King", 300f, 5f);
        termiteKing.setBodyLengthMm(10.0f);
        termiteKing.setHeadWidthMm(2.0f);
        termiteKing.setWingbeatFrequencyHz(0.0f);
        termiteKing.setMaxCarryingPayloadRatio(1.0f);
        termiteKing.setMandibularBitingForceMPa(3.0f);
        termiteKing.setTargetRatio(0.00f);

        CasteTemplate termiteWorker = new CasteTemplate("Termite Worker", 50f, 3f);
        termiteWorker.setCanDig(true);
        termiteWorker.setBodyLengthMm(5.0f);
        termiteWorker.setHeadWidthMm(1.2f);
        termiteWorker.setWingbeatFrequencyHz(0.0f);
        termiteWorker.setMaxCarryingPayloadRatio(6.0f);
        termiteWorker.setMandibularBitingForceMPa(8.0f);
        termiteWorker.setTargetRatio(0.85f);

        CasteTemplate termiteSoldier = new CasteTemplate("Mandibulate Soldier", 200f, 35f);
        termiteSoldier.setBaseDefense(6f);
        termiteSoldier.setBodyLengthMm(7.0f);
        termiteSoldier.setHeadWidthMm(2.5f);
        termiteSoldier.setWingbeatFrequencyHz(0.0f);
        termiteSoldier.setMaxCarryingPayloadRatio(4.0f);
        termiteSoldier.setMandibularBitingForceMPa(55.0f);
        termiteSoldier.setTargetRatio(0.15f);

        termite.setCasteTemplates(List.of(termiteQueen, termiteKing, termiteWorker, termiteSoldier));
        map.put(termite.getPresetName(), termite);

        // 7. Red Harvester Ant (Pogonomyrmex barbatus)
        CustomSpecies harvester = new CustomSpecies();
        harvester.setPresetName("Red Harvester Ant (Pogonomyrmex barbatus)");
        harvester.setCommonName("Red Harvester Ant");
        harvester.setScientificName("Pogonomyrmex barbatus");
        harvester.setInsectType("ANT");
        harvester.setDescription("Granivorous species collecting seeds and storing them in underground granaries.");
        harvester.setQueenCountMode("MONOGYNE");
        harvester.setQueenCount(1);
        harvester.setQueenLifespan(22000);
        harvester.setQueenEggLayingRate(20.0f);
        harvester.setNuptialFlightType("AERIAL_SWARM");
        harvester.setWorkerLifespan(5500);
        harvester.setWorkerSpeed(0.55f);
        harvester.setTypicalColonySize(12000);
        harvester.setFormsMegaColonies(false);
        harvester.setPrimaryDiet("SEEDS");
        harvester.setSecondaryDiet("SUGARS_NECTAR");
        harvester.setDailyFoodConsumption(0.4f);
        harvester.setWaterRequirement(0.10f);
        harvester.setNestType("UNDERGROUND_BURROW");
        harvester.setVenomType("VENOMOUS_STING");
        harvester.setAggression(0.6f);
        harvester.setTerritoriality(0.65f);
        harvester.setOptimalTempCelsius(30.0f);
        harvester.setMinTempCelsius(8.0f);
        harvester.setMaxTempCelsius(45.0f);
        harvester.setEggStageDuration(14);
        harvester.setLarvaStageDuration(18);
        harvester.setPupaStageDuration(18);
        harvester.setLarvaDietRequirement("Seeds & Harvested Grains");
        harvester.setProteinThresholdMinor(0.25f);
        harvester.setProteinThresholdMajor(0.65f);
        harvester.setProteinThresholdSoldier(0.80f);
        harvester.setProteinThresholdQueen(0.90f);
        harvester.setQueenPheromoneInhibitionFactor(0.85f);
        harvester.setHaplodiploidyEnabled(true);
        harvester.setPathogenResistance(0.65f);
        harvester.setGroomingDefenseEfficacy(0.75f);
        harvester.setHasMagnetoreception(true);
        harvester.setMagnetoreceptionSensitivity(4.0f);
        harvester.setThermoreceptionSensitivity(0.2f);
        harvester.setGasSensitivityCo2Ppm(600.0f);
        harvester.setVisualAcuity(2.5f);
        harvester.setMinLightLevelThreshold(0.12f);
        harvester.setHasSubstrateVibrationSensing(true);
        harvester.setVibrationSensitivityDb(6.0f);
        harvester.setHasHygroreception(true);
        harvester.setHygroreceptionSensitivityPercent(1.0f);
        harvester.setHasElectrosensing(false);
        harvester.setHasPolarizedLightNavigation(true);
        harvester.setWingbeatFrequencyHz(170.0f);
        harvester.setHasHoveringCapability(false);
        harvester.setMaxCarryingPayloadRatio(12.0f);
        harvester.setMandibularBitingForceMPa(40.0f);
        harvester.setHasAutothysis(false);
        harvester.setHasSubstrateAdhesionArolia(true);
        harvester.setViewDistance(12.0f);
        harvester.setMetabolism(1.1f);
        harvester.setStrength(12.0f);

        CasteTemplate harvestQueen = new CasteTemplate("Queen", 450f, 10f);
        harvestQueen.setBodyLengthMm(12.0f);
        harvestQueen.setHeadWidthMm(3.0f);
        harvestQueen.setWingbeatFrequencyHz(170.0f);
        harvestQueen.setMaxCarryingPayloadRatio(3.0f);
        harvestQueen.setMandibularBitingForceMPa(20.0f);
        harvestQueen.setTargetRatio(0.00f);

        CasteTemplate harvestWorker = new CasteTemplate("Harvester Worker", 90f, 14f);
        harvestWorker.setCanCarry(true);
        harvestWorker.setCanDig(true);
        harvestWorker.setBodyLengthMm(7.0f);
        harvestWorker.setHeadWidthMm(2.0f);
        harvestWorker.setWingbeatFrequencyHz(0.0f);
        harvestWorker.setMaxCarryingPayloadRatio(12.0f);
        harvestWorker.setMandibularBitingForceMPa(40.0f);
        harvestWorker.setTargetRatio(1.00f);

        harvester.setCasteTemplates(List.of(harvestQueen, harvestWorker));
        map.put(harvester.getPresetName(), harvester);

        // 8. Elderberry Aphid (Aphis fabae - Honeydew Producer)
        CustomSpecies aphid = new CustomSpecies();
        aphid.setPresetName("Elderberry Aphid (Aphis fabae)");
        aphid.setCommonName("Black Bean Aphid");
        aphid.setScientificName("Aphis fabae");
        aphid.setInsectType("OTHER");
        aphid.setCategory(org.swarmforge.core.species.SpeciesCategory.HONEYDEW_PRODUCER);
        aphid.setDescription("Sap-sucking homopteran forming dense colonies. Excretes honeydew harvested by ants.");
        aphid.setQueenCountMode("GAMERGATES");
        aphid.setQueenCount(0);
        aphid.setWorkerLifespan(1200);
        aphid.setWorkerSpeed(0.05f);
        aphid.setViewDistance(1.5f);
        aphid.setTypicalColonySize(500);
        aphid.setFormsMegaColonies(false);
        aphid.setPrimaryDiet("SUGARS_NECTAR");
        aphid.setSecondaryDiet("NONE");
        aphid.setDailyFoodConsumption(0.1f);
        aphid.setWaterRequirement(0.05f);
        aphid.setNestType("ARBOREAL_SILK_LEAF");
        aphid.setVenomType("NONE");
        aphid.setAggression(0.0f);
        aphid.setTerritoriality(0.0f);
        aphid.setOptimalTempCelsius(22.0f);
        aphid.setMinTempCelsius(8.0f);
        aphid.setMaxTempCelsius(32.0f);
        aphid.setMetabolism(0.6f);
        aphid.setStrength(1.0f);

        CasteTemplate aphidWorker = new CasteTemplate("Wingless Aphid (Honeydew Producer)", 15f, 0f);
        aphidWorker.setBodyLengthMm(2.0f);
        aphidWorker.setHeadWidthMm(0.5f);
        aphidWorker.setLifespan(1200);
        aphidWorker.setTargetRatio(1.00f);
        aphid.setCasteTemplates(List.of(aphidWorker));
        map.put(aphid.getPresetName(), aphid);

        // 9. Cabbage White Caterpillar (Pieris brassicae)
        CustomSpecies caterpillar = new CustomSpecies();
        caterpillar.setPresetName("Large White Caterpillar (Pieris brassicae)");
        caterpillar.setCommonName("Cabbage White Caterpillar");
        caterpillar.setScientificName("Pieris brassicae");
        caterpillar.setInsectType("OTHER");
        caterpillar.setCategory(org.swarmforge.core.species.SpeciesCategory.PREY_ORGANISM);
        caterpillar.setDescription("Phytophagous larva serving as protein-rich prey for carnivorous colonies.");
        caterpillar.setQueenCountMode("GAMERGATES");
        caterpillar.setQueenCount(0);
        caterpillar.setWorkerLifespan(800);
        caterpillar.setWorkerSpeed(0.10f);
        caterpillar.setViewDistance(2.0f);
        caterpillar.setTypicalColonySize(50);
        caterpillar.setFormsMegaColonies(false);
        caterpillar.setPrimaryDiet("WOOD_CELLULOSE");
        caterpillar.setSecondaryDiet("NONE");
        caterpillar.setDailyFoodConsumption(0.8f);
        caterpillar.setWaterRequirement(0.30f);
        caterpillar.setNestType("SIMPLE");
        caterpillar.setVenomType("NONE");
        caterpillar.setAggression(0.0f);
        caterpillar.setTerritoriality(0.0f);
        caterpillar.setOptimalTempCelsius(23.0f);
        caterpillar.setMinTempCelsius(6.0f);
        caterpillar.setMaxTempCelsius(34.0f);
        caterpillar.setMetabolism(1.2f);
        caterpillar.setStrength(4.0f);

        CasteTemplate caterpillarPrey = new CasteTemplate("Herbivorous Caterpillar (Protein Prey)", 80f, 2f);
        caterpillarPrey.setBodyLengthMm(25.0f);
        caterpillarPrey.setHeadWidthMm(2.5f);
        caterpillarPrey.setLifespan(800);
        caterpillarPrey.setTargetRatio(1.00f);
        caterpillar.setCasteTemplates(List.of(caterpillarPrey));
        map.put(caterpillar.getPresetName(), caterpillar);

        // 10. European Antlion (Myrmeleon formicarius)
        CustomSpecies antlion = new CustomSpecies();
        antlion.setPresetName("European Antlion (Myrmeleon formicarius)");
        antlion.setCommonName("Trap-Jaw Antlion");
        antlion.setScientificName("Myrmeleon formicarius");
        antlion.setInsectType("OTHER");
        antlion.setCategory(org.swarmforge.core.species.SpeciesCategory.PARASITE_PREDATOR);
        antlion.setDescription("Ambush predator digging sand funnels to capture passing workers.");
        antlion.setQueenCountMode("GAMERGATES");
        antlion.setCommonName("Fourmilion Piégeur");
        antlion.setScientificName("Myrmeleon formicarius");
        antlion.setInsectType("OTHER");
        antlion.setCategory(org.swarmforge.core.species.SpeciesCategory.PARASITE_PREDATOR);
        antlion.setDescription("Prédateur embusqué creusant des entonnoirs dans le sable pour capturer les ouvrières de passage.");
        antlion.setQueenCountMode("GAMERGATES");
        antlion.setQueenCount(0);
        antlion.setWorkerLifespan(3000);
        antlion.setWorkerSpeed(0.20f);
        antlion.setViewDistance(5.0f);
        antlion.setTypicalColonySize(10);
        antlion.setFormsMegaColonies(false);
        antlion.setPrimaryDiet("INSECTS_MEAT");
        antlion.setSecondaryDiet("NONE");
        antlion.setDailyFoodConsumption(0.5f);
        antlion.setWaterRequirement(0.10f);
        antlion.setNestType("UNDERGROUND_BURROW");
        antlion.setVenomType("NEUROTOXIN");
        antlion.setAggression(0.95f);
        antlion.setTerritoriality(0.90f);
        antlion.setOptimalTempCelsius(28.0f);
        antlion.setMinTempCelsius(10.0f);
        antlion.setMaxTempCelsius(44.0f);
        antlion.setMetabolism(1.4f);
        antlion.setStrength(15.0f);
        antlion.setMandibularBitingForceMPa(85.0f);

        CasteTemplate antlionPredator = new CasteTemplate("Antlion Larva (Trap Predator)", 180f, 35f);
        antlionPredator.setBodyLengthMm(12.0f);
        antlionPredator.setHeadWidthMm(3.5f);
        antlionPredator.setLifespan(3000);
        antlionPredator.setVenomType("NEUROTOXIN");
        antlionPredator.setVenomToxicity(25.0f);
        antlionPredator.setMandibularBitingForceMPa(85.0f);
        antlionPredator.setTargetRatio(1.00f);
        antlion.setCasteTemplates(List.of(antlionPredator));
        map.put(antlion.getPresetName(), antlion);

        // 11. Common Woodlouse (Porcellio scaber)
        CustomSpecies isopod = new CustomSpecies();
        isopod.setPresetName("Common Woodlouse (Porcellio scaber)");
        isopod.setCommonName("Common Rough Woodlouse");
        isopod.setScientificName("Porcellio scaber");
        isopod.setInsectType("OTHER");
        isopod.setCategory(org.swarmforge.core.species.SpeciesCategory.SOIL_FAUNA);
        isopod.setDescription("Terrestrial crustacean decomposer participating in leaf litter recycling and soil aeration.");
        isopod.setQueenCountMode("GAMERGATES");
        isopod.setQueenCount(0);
        isopod.setWorkerLifespan(4000);
        isopod.setWorkerSpeed(0.30f);
        isopod.setViewDistance(3.0f);
        isopod.setTypicalColonySize(200);
        isopod.setFormsMegaColonies(false);
        isopod.setPrimaryDiet("WOOD_CELLULOSE");
        isopod.setSecondaryDiet("FUNGUS");
        isopod.setDailyFoodConsumption(0.2f);
        isopod.setWaterRequirement(0.40f);
        isopod.setNestType("SIMPLE");
        isopod.setVenomType("NONE");
        isopod.setAggression(0.05f);
        isopod.setTerritoriality(0.10f);
        isopod.setOptimalTempCelsius(18.0f);
        isopod.setMinTempCelsius(2.0f);
        isopod.setMaxTempCelsius(28.0f);
        isopod.setMetabolism(0.7f);
        isopod.setStrength(3.0f);

        CasteTemplate isopodWorker = new CasteTemplate("Decomposer Woodlouse (Soil Fauna)", 60f, 0f);
        isopodWorker.setBodyLengthMm(14.0f);
        isopodWorker.setHeadWidthMm(2.0f);
        isopodWorker.setLifespan(4000);
        isopodWorker.setTargetRatio(1.00f);
        isopod.setCasteTemplates(List.of(isopodWorker));
        map.put(isopod.getPresetName(), isopod);

        return map;
    }

    public Map<String, CustomSpecies> getAll() {
        return Collections.unmodifiableMap(presets);
    }

    public Set<String> getPresetNames() {
        return new TreeSet<>(presets.keySet());
    }

    public CustomSpecies getPreset(String name) {
        return presets.get(name);
    }

    public boolean contains(String name) {
        return presets.containsKey(name);
    }

    public void addPreset(String name, CustomSpecies species) {
        species.setPresetName(name);
        presets.put(name, species);
        persist();
    }

    public boolean delete(String name) {
        if (presets.containsKey(name)) {
            presets.remove(name);
            persist();
            return true;
        }
        return false;
    }

    public void persist() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(PRESETS_FILE, presets);
        } catch (Exception e) {
            System.err.println("[SpeciesPresetManager] Could not save " + PRESETS_FILE + ": " + e.getMessage());
        }
    }

    public void saveToFile(File file, CustomSpecies species) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, species);
    }

    public CustomSpecies loadFromFile(File file) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(file, CustomSpecies.class);
    }
}
