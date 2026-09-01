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
        lasius.setQueenLifespan(5475);
        lasius.setQueenEggLayingRate(25.0f);
        lasius.setNuptialFlightType("AERIAL_SWARM");
        lasius.setWorkerLifespan(1095);
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
        lasius.setMinTempCelsius(0.0f);
        lasius.setMaxTempCelsius(42.0f);
        lasius.setOptimalHumidityPercent(75.0f);
        lasius.setMinHumidityPercent(25.0f);
        lasius.setMaxHumidityPercent(95.0f);
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
        lasiusQueen.setLifespan(5475);
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
        lasiusWorker.setLifespan(1095);
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
        lasiusMale.setLifespan(21);
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
        solenopsis.setQueenLifespan(2555);
        solenopsis.setQueenEggLayingRate(80.0f);
        solenopsis.setNuptialFlightType("AERIAL_SWARM");
        solenopsis.setWorkerLifespan(180);
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
        solenopsis.setOptimalHumidityPercent(75.0f);
        solenopsis.setMinHumidityPercent(20.0f);
        solenopsis.setMaxHumidityPercent(95.0f);
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
        fireQueen.setLifespan(2555);
        fireQueen.setCanFly(true);
        fireQueen.setBodyLengthMm(8.0f);
        fireQueen.setHeadWidthMm(2.0f);
        fireQueen.setWingbeatFrequencyHz(190.0f);
        fireQueen.setMaxCarryingPayloadRatio(2.0f);
        fireQueen.setMandibularBitingForceMPa(10.0f);
        fireQueen.setTargetRatio(0.00f);

        CasteTemplate fireMinor = new CasteTemplate("Minor Worker", 60f, 8f);
        fireMinor.setLifespan(60);
        fireMinor.setCanDig(true);
        fireMinor.setBodyLengthMm(3.0f);
        fireMinor.setHeadWidthMm(0.8f);
        fireMinor.setWingbeatFrequencyHz(0.0f);
        fireMinor.setMaxCarryingPayloadRatio(4.0f);
        fireMinor.setMandibularBitingForceMPa(8.0f);
        fireMinor.setTargetRatio(0.85f);

        CasteTemplate fireMajor = new CasteTemplate("Major Worker / Soldier", 150f, 25f);
        fireMajor.setLifespan(180);
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
        atta.setQueenLifespan(5475);
        atta.setQueenEggLayingRate(100.0f);
        atta.setNuptialFlightType("AERIAL_SWARM");
        atta.setWorkerLifespan(500);
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
        atta.setOptimalHumidityPercent(90.0f);
        atta.setMinHumidityPercent(60.0f);
        atta.setMaxHumidityPercent(100.0f);
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
        attaQueen.setLifespan(5475);
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
        apis.setQueenLifespan(1460);
        apis.setQueenEggLayingRate(150.0f);
        apis.setNuptialFlightType("SWARM_DIVISION");
        apis.setWorkerLifespan(60);
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
        apisQueen.setLifespan(1460);
        apisQueen.setCanFly(true);
        apisQueen.setBodyLengthMm(20.0f);
        apisQueen.setHeadWidthMm(4.0f);
        apisQueen.setWingbeatFrequencyHz(180.0f);
        apisQueen.setHasHoveringCapability(false);
        apisQueen.setMaxCarryingPayloadRatio(1.5f);
        apisQueen.setMandibularBitingForceMPa(3.0f);
        apisQueen.setTargetRatio(0.00f);

        CasteTemplate apisWorker = new CasteTemplate("Forager Worker", 90f, 12f);
        apisWorker.setLifespan(60);
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
        apisDrone.setLifespan(40);
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
        vespula.setQueenLifespan(365);
        vespula.setQueenEggLayingRate(40.0f);
        vespula.setNuptialFlightType("AERIAL_SWARM");
        vespula.setWorkerLifespan(30);
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
        vespulaQueen.setLifespan(365);
        vespulaQueen.setCanFly(true);
        vespulaQueen.setBodyLengthMm(19.0f);
        vespulaQueen.setHeadWidthMm(4.5f);
        vespulaQueen.setWingbeatFrequencyHz(160.0f);
        vespulaQueen.setHasHoveringCapability(true);
        vespulaQueen.setMaxCarryingPayloadRatio(3.0f);
        vespulaQueen.setMandibularBitingForceMPa(18.0f);
        vespulaQueen.setTargetRatio(0.00f);

        CasteTemplate vespulaWorker = new CasteTemplate("Hunter Worker", 100f, 22f);
        vespulaWorker.setLifespan(30);
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

        // 5b. European Hornet (Vespa crabro)
        CustomSpecies vespa = new CustomSpecies();
        vespa.setPresetName("European Hornet (Vespa crabro)");
        vespa.setCommonName("European Hornet");
        vespa.setScientificName("Vespa crabro");
        vespa.setInsectType("WASP");
        vespa.setDescription("Large apex predatory vespid. Constructs expansive multi-tiered paper nests in hollow trees or sheltered cavities.");
        vespa.setQueenCountMode("MONOGYNE");
        vespa.setQueenCount(1);
        vespa.setQueenLifespan(365);
        vespa.setQueenEggLayingRate(50.0f);
        vespa.setNuptialFlightType("AERIAL_SWARM");
        vespa.setWorkerLifespan(35);
        vespa.setWorkerSpeed(1.6f);
        vespa.setWorkersCanFly(true);
        vespa.setTypicalColonySize(5000);
        vespa.setFormsMegaColonies(false);
        vespa.setPrimaryDiet("INSECTS_MEAT");
        vespa.setSecondaryDiet("SUGARS_NECTAR");
        vespa.setDailyFoodConsumption(0.65f);
        vespa.setWaterRequirement(0.25f);
        vespa.setNestType("PAPER_PEDUNCULATE");
        vespa.setVenomType("VENOMOUS_STING");
        vespa.setAggression(0.75f);
        vespa.setTerritoriality(0.80f);
        vespa.setOptimalTempCelsius(26.0f);
        vespa.setMinTempCelsius(10.0f);
        vespa.setMaxTempCelsius(38.0f);
        vespa.setEggStageDuration(6);
        vespa.setLarvaStageDuration(10);
        vespa.setPupaStageDuration(14);
        vespa.setLarvaDietRequirement("High Protein Meat & Large Insects");
        vespa.setProteinThresholdMinor(0.30f);
        vespa.setProteinThresholdMajor(0.60f);
        vespa.setProteinThresholdSoldier(0.99f);
        vespa.setProteinThresholdQueen(0.95f);
        vespa.setQueenPheromoneInhibitionFactor(0.85f);
        vespa.setHaplodiploidyEnabled(true);
        vespa.setPathogenResistance(0.70f);
        vespa.setGroomingDefenseEfficacy(0.75f);
        vespa.setHasMagnetoreception(false);
        vespa.setThermoreceptionSensitivity(0.25f);
        vespa.setGasSensitivityCo2Ppm(350.0f);
        vespa.setVisualAcuity(4.5f);
        vespa.setMinLightLevelThreshold(0.02f); // Hunts day & night
        vespa.setHasSubstrateVibrationSensing(true);
        vespa.setVibrationSensitivityDb(5.0f);
        vespa.setHasHygroreception(true);
        vespa.setHygroreceptionSensitivityPercent(1.5f);
        vespa.setHasElectrosensing(false);
        vespa.setHasPolarizedLightNavigation(true);
        vespa.setWingbeatFrequencyHz(170.0f);
        vespa.setHasHoveringCapability(true);
        vespa.setMaxCarryingPayloadRatio(8.0f);
        vespa.setMandibularBitingForceMPa(45.0f);
        vespa.setHasAutothysis(false);
        vespa.setHasSubstrateAdhesionArolia(true);
        vespa.setViewDistance(35.0f);
        vespa.setMetabolism(2.2f);
        vespa.setStrength(12.0f);

        CasteTemplate vespaQueen = new CasteTemplate("Queen Hornet", 600f, 30f);
        vespaQueen.setLifespan(365);
        vespaQueen.setCanFly(true);
        vespaQueen.setBodyLengthMm(30.0f);
        vespaQueen.setHeadWidthMm(6.5f);
        vespaQueen.setWingbeatFrequencyHz(140.0f);
        vespaQueen.setHasHoveringCapability(true);
        vespaQueen.setMaxCarryingPayloadRatio(4.0f);
        vespaQueen.setMandibularBitingForceMPa(35.0f);
        vespaQueen.setTargetRatio(0.00f);

        CasteTemplate vespaWorker = new CasteTemplate("Hornet Worker", 160f, 35f);
        vespaWorker.setLifespan(35);
        vespaWorker.setCanFly(true);
        vespaWorker.setBodyLengthMm(22.0f);
        vespaWorker.setHeadWidthMm(4.8f);
        vespaWorker.setWingbeatFrequencyHz(170.0f);
        vespaWorker.setHasHoveringCapability(true);
        vespaWorker.setMaxCarryingPayloadRatio(8.0f);
        vespaWorker.setMandibularBitingForceMPa(45.0f);
        vespaWorker.setTargetRatio(1.00f);

        vespa.setCasteTemplates(List.of(vespaQueen, vespaWorker));
        map.put(vespa.getPresetName(), vespa);

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
        termite.setKingLifespan(5475);
        termite.setQueenLifespan(7300);
        termite.setQueenEggLayingRate(60.0f);
        termite.setNuptialFlightType("AERIAL_SWARM");
        termite.setWorkerLifespan(500);
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
        termiteQueen.setLifespan(7300);
        termiteQueen.setBodyLengthMm(22.0f);
        termiteQueen.setHeadWidthMm(3.0f);
        termiteQueen.setWingbeatFrequencyHz(0.0f);
        termiteQueen.setMaxCarryingPayloadRatio(0.5f);
        termiteQueen.setMandibularBitingForceMPa(2.0f);
        termiteQueen.setTargetRatio(0.00f);

        CasteTemplate termiteKing = new CasteTemplate("Reproductive King", 300f, 5f);
        termiteKing.setLifespan(5475);
        termiteKing.setBodyLengthMm(10.0f);
        termiteKing.setHeadWidthMm(2.0f);
        termiteKing.setWingbeatFrequencyHz(0.0f);
        termiteKing.setMaxCarryingPayloadRatio(1.0f);
        termiteKing.setMandibularBitingForceMPa(3.0f);
        termiteKing.setTargetRatio(0.00f);

        CasteTemplate termiteWorker = new CasteTemplate("Termite Worker", 50f, 3f);
        termiteWorker.setLifespan(500);
        termiteWorker.setCanDig(true);
        termiteWorker.setBodyLengthMm(5.0f);
        termiteWorker.setHeadWidthMm(1.2f);
        termiteWorker.setWingbeatFrequencyHz(0.0f);
        termiteWorker.setMaxCarryingPayloadRatio(6.0f);
        termiteWorker.setMandibularBitingForceMPa(8.0f);
        termiteWorker.setTargetRatio(0.85f);

        CasteTemplate termiteSoldier = new CasteTemplate("Mandibulate Soldier", 200f, 35f);
        termiteSoldier.setLifespan(730);
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
        harvester.setQueenLifespan(7300);
        harvester.setQueenEggLayingRate(20.0f);
        harvester.setNuptialFlightType("AERIAL_SWARM");
        harvester.setWorkerLifespan(500);
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
        harvestQueen.setLifespan(7300);
        harvestQueen.setBodyLengthMm(12.0f);
        harvestQueen.setHeadWidthMm(3.0f);
        harvestQueen.setWingbeatFrequencyHz(170.0f);
        harvestQueen.setMaxCarryingPayloadRatio(3.0f);
        harvestQueen.setMandibularBitingForceMPa(20.0f);
        harvestQueen.setTargetRatio(0.00f);

        CasteTemplate harvestWorker = new CasteTemplate("Harvester Worker", 90f, 14f);
        harvestWorker.setLifespan(500);
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
        aphid.setWorkerLifespan(25);
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
        aphidWorker.setLifespan(25);
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
        caterpillar.setWorkerLifespan(25);
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
        caterpillarPrey.setLifespan(25);
        caterpillarPrey.setTargetRatio(1.00f);
        caterpillar.setCasteTemplates(List.of(caterpillarPrey));
        map.put(caterpillar.getPresetName(), caterpillar);

        // 10. European Antlion (Myrmeleon formicarius)
        CustomSpecies antlion = new CustomSpecies();
        antlion.setPresetName("European Antlion (Myrmeleon formicarius)");
        antlion.setCommonName("Pitfall Antlion");
        antlion.setScientificName("Myrmeleon formicarius");
        antlion.setInsectType("OTHER");
        antlion.setCategory(org.swarmforge.core.species.SpeciesCategory.PARASITE_PREDATOR);
        antlion.setDescription("Ambush predator digging funnels in sand to capture passing workers.");
        antlion.setQueenCountMode("GAMERGATES");
        antlion.setQueenCount(0);
        antlion.setWorkerLifespan(500);
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
        antlionPredator.setLifespan(500);
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
        isopod.setWorkerLifespan(730);
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
        isopodWorker.setLifespan(730);
        isopodWorker.setTargetRatio(1.00f);
        isopod.setCasteTemplates(List.of(isopodWorker));
        map.put(isopod.getPresetName(), isopod);

        // 12. Black Wood Ant (Formica fusca)
        CustomSpecies formicaFusca = new CustomSpecies();
        formicaFusca.setPresetName("Formica fusca (Black Wood Ant)");
        formicaFusca.setCommonName("Black Wood Ant");
        formicaFusca.setScientificName("Formica fusca");
        formicaFusca.setInsectType("ANT");
        formicaFusca.setDescription("Fast-moving, timid wood ant common in Europe. Excellent vision and honeydew harvester.");
        formicaFusca.setQueenCountMode("FACULTATIVE_POLYGYNE");
        formicaFusca.setQueenCount(3);
        formicaFusca.setQueenLifespan(4000);
        formicaFusca.setQueenEggLayingRate(35.0f);
        formicaFusca.setNuptialFlightType("AERIAL_SWARM");
        formicaFusca.setWorkerLifespan(730);
        formicaFusca.setWorkerSpeed(0.85f);
        formicaFusca.setTypicalColonySize(2000);
        formicaFusca.setFormsMegaColonies(false);
        formicaFusca.setPrimaryDiet("HONEYDEW");
        formicaFusca.setSecondaryDiet("INSECTS_MEAT");
        formicaFusca.setDailyFoodConsumption(0.35f);
        formicaFusca.setWaterRequirement(0.20f);
        formicaFusca.setNestType("MATURE");
        formicaFusca.setVenomType("FORMIC_ACID");
        formicaFusca.setAggression(0.35f);
        formicaFusca.setTerritoriality(0.40f);
        formicaFusca.setOptimalTempCelsius(22.0f);
        formicaFusca.setMinTempCelsius(0.0f);
        formicaFusca.setMaxTempCelsius(42.0f);
        formicaFusca.setOptimalHumidityPercent(75.0f);
        formicaFusca.setMinHumidityPercent(25.0f);
        formicaFusca.setMaxHumidityPercent(95.0f);
        formicaFusca.setEggStageDuration(14);
        formicaFusca.setLarvaStageDuration(12);
        formicaFusca.setPupaStageDuration(14);
        formicaFusca.setLarvaDietRequirement("Protein & Honeydew");
        formicaFusca.setProteinThresholdMinor(0.30f);
        formicaFusca.setProteinThresholdMajor(0.65f);
        formicaFusca.setProteinThresholdSoldier(0.80f);
        formicaFusca.setProteinThresholdQueen(0.88f);
        formicaFusca.setQueenPheromoneInhibitionFactor(0.75f);
        formicaFusca.setHaplodiploidyEnabled(true);
        formicaFusca.setPathogenResistance(0.65f);
        formicaFusca.setGroomingDefenseEfficacy(0.80f);
        formicaFusca.setHasMagnetoreception(false);
        formicaFusca.setThermoreceptionSensitivity(0.35f);
        formicaFusca.setGasSensitivityCo2Ppm(480.0f);
        formicaFusca.setVisualAcuity(2.2f);
        formicaFusca.setMinLightLevelThreshold(0.02f);
        formicaFusca.setHasSubstrateVibrationSensing(true);
        formicaFusca.setVibrationSensitivityDb(7.5f);
        formicaFusca.setHasHygroreception(true);
        formicaFusca.setHygroreceptionSensitivityPercent(1.2f);
        formicaFusca.setHasElectrosensing(false);
        formicaFusca.setHasPolarizedLightNavigation(true);
        formicaFusca.setWingbeatFrequencyHz(190.0f);
        formicaFusca.setHasHoveringCapability(false);
        formicaFusca.setMaxCarryingPayloadRatio(6.0f);
        formicaFusca.setMandibularBitingForceMPa(14.0f);
        formicaFusca.setHasAutothysis(false);
        formicaFusca.setHasSubstrateAdhesionArolia(true);
        formicaFusca.setViewDistance(8.5f);
        formicaFusca.setMetabolism(1.1f);
        formicaFusca.setStrength(6.0f);

        CasteTemplate ffQueen = new CasteTemplate("Queen", 450f, 8f);
        ffQueen.setDescription("Gyne (8-10mm)");
        ffQueen.setLifespan(4000);
        ffQueen.setCanFly(true);
        ffQueen.setBodyLengthMm(9.5f);
        ffQueen.setHeadWidthMm(2.3f);
        ffQueen.setWingbeatFrequencyHz(190.0f);
        ffQueen.setTargetRatio(0.00f);

        CasteTemplate ffWorker = new CasteTemplate("Worker", 90f, 5f);
        ffWorker.setDescription("Agile Forager Worker (5-7mm)");
        ffWorker.setLifespan(730);
        ffWorker.setCanDig(true);
        ffWorker.setCanCarry(true);
        ffWorker.setBodyLengthMm(5.5f);
        ffWorker.setHeadWidthMm(1.3f);
        ffWorker.setWingbeatFrequencyHz(0.0f);
        ffWorker.setMaxCarryingPayloadRatio(6.0f);
        ffWorker.setMandibularBitingForceMPa(14.0f);
        ffWorker.setTargetRatio(1.00f);

        CasteTemplate ffMale = new CasteTemplate("Reproductive Male (Alate)", 40f, 0f);
        ffMale.setDescription("Alate male (5-6mm)");
        ffMale.setLifespan(30);
        ffMale.setCanFly(true);
        ffMale.setBodyLengthMm(5.5f);
        ffMale.setHeadWidthMm(1.2f);
        ffMale.setWingbeatFrequencyHz(220.0f);
        ffMale.setTargetRatio(0.00f);

        formicaFusca.setCasteTemplates(List.of(ffQueen, ffWorker, ffMale));
        map.put(formicaFusca.getPresetName(), formicaFusca);

        // 13. European Amazon Ant (Polyergus rufescens - Obligate Dulosis Slave-Maker)
        CustomSpecies polyergus = new CustomSpecies();
        polyergus.setPresetName("Fourmi Amazone Duloce (Polyergus rufescens)");
        polyergus.setCommonName("Amazon Slave-Making Ant");
        polyergus.setScientificName("Polyergus rufescens");
        polyergus.setInsectType("ANT");
        polyergus.setDescription("Obligate dulotic slave-making ant with sickle mandibles, conducting summer raids to capture Formica brood.");
        polyergus.setQueenCountMode("MONOGYNE");
        polyergus.setQueenCount(1);
        polyergus.setQueenLifespan(5475);
        polyergus.setQueenEggLayingRate(30.0f);
        polyergus.setNuptialFlightType("AERIAL_SWARM");
        polyergus.setWorkerLifespan(730);
        polyergus.setWorkerSpeed(0.85f);
        polyergus.setTypicalColonySize(5000);
        polyergus.setFormsMegaColonies(false);
        polyergus.setPrimaryDiet("HONEYDEW");
        polyergus.setSecondaryDiet("INSECTS_MEAT");
        polyergus.setDailyFoodConsumption(0.4f);
        polyergus.setWaterRequirement(0.2f);
        polyergus.setNestType("MATURE");
        polyergus.setVenomType("POWERFUL_MANDIBLES");
        polyergus.setAggression(0.95f);
        polyergus.setTerritoriality(0.85f);
        polyergus.setOptimalTempCelsius(22.0f);
        polyergus.setMinTempCelsius(0.0f);
        polyergus.setMaxTempCelsius(42.0f);
        polyergus.setOptimalHumidityPercent(65.0f);
        polyergus.setMinHumidityPercent(20.0f);
        polyergus.setMaxHumidityPercent(90.0f);
        polyergus.setMetabolism(1.2f);
        polyergus.setStrength(10.0f);

        CasteTemplate polyergusQueen = new CasteTemplate("Reine Amazone", 550f, 20f);
        polyergusQueen.setLifespan(5475);
        polyergusQueen.setCanFly(true);
        polyergusQueen.setBodyLengthMm(9.5f);
        polyergusQueen.setHeadWidthMm(2.8f);
        polyergusQueen.setTargetRatio(0.00f);

        CasteTemplate polyergusRaider = new CasteTemplate("Guerrière Amazone", 180f, 18f);
        polyergusRaider.setDescription("Highly specialized warrior armed with falcate mandibles for raids.");
        polyergusRaider.setLifespan(730);
        polyergusRaider.setCanCarry(true);
        polyergusRaider.setBodyLengthMm(7.0f);
        polyergusRaider.setHeadWidthMm(2.1f);
        polyergusRaider.setTargetRatio(1.00f);

        polyergus.setCasteTemplates(List.of(polyergusQueen, polyergusRaider));
        map.put(polyergus.getPresetName(), polyergus);

        // 14. Argentine Ant (Linepithema humile - Invasive Supercolony)
        CustomSpecies linepithema = new CustomSpecies();
        linepithema.setPresetName("Fourmi d'Argentine (Linepithema humile)");
        linepithema.setCommonName("Argentine Ant");
        linepithema.setScientificName("Linepithema humile");
        linepithema.setInsectType("ANT");
        linepithema.setDescription("Highly invasive super-colonial ant forming massive multi-queen unicolonial networks.");
        linepithema.setQueenCountMode("POLYGYNE");
        linepithema.setQueenCount(10);
        linepithema.setQueenLifespan(365);
        linepithema.setQueenEggLayingRate(60.0f);
        linepithema.setNuptialFlightType("BUDDING");
        linepithema.setWorkerLifespan(180);
        linepithema.setWorkerSpeed(0.9f);
        linepithema.setTypicalColonySize(1000000);
        linepithema.setFormsMegaColonies(true);
        linepithema.setPrimaryDiet("HONEYDEW");
        linepithema.setSecondaryDiet("INSECTS_MEAT");
        linepithema.setDailyFoodConsumption(0.25f);
        linepithema.setWaterRequirement(0.15f);
        linepithema.setNestType("MATURE");
        linepithema.setVenomType("FORMIC_ACID");
        linepithema.setAggression(0.8f);
        linepithema.setTerritoriality(0.2f);
        linepithema.setOptimalTempCelsius(25.0f);
        linepithema.setMinTempCelsius(10.0f);
        linepithema.setMaxTempCelsius(38.0f);
        linepithema.setOptimalHumidityPercent(75.0f);
        linepithema.setMinHumidityPercent(30.0f);
        linepithema.setMaxHumidityPercent(95.0f);
        linepithema.setMetabolism(1.3f);
        linepithema.setStrength(4.0f);

        CasteTemplate linepithemaQueen = new CasteTemplate("Queen", 300f, 5f);
        linepithemaQueen.setLifespan(365);
        linepithemaQueen.setBodyLengthMm(5.0f);
        linepithemaQueen.setHeadWidthMm(1.4f);
        linepithemaQueen.setTargetRatio(0.00f);

        CasteTemplate linepithemaWorker = new CasteTemplate("Worker", 50f, 3f);
        linepithemaWorker.setLifespan(180);
        linepithemaWorker.setCanDig(true);
        linepithemaWorker.setCanCarry(true);
        linepithemaWorker.setBodyLengthMm(2.8f);
        linepithemaWorker.setHeadWidthMm(0.7f);
        linepithemaWorker.setTargetRatio(1.00f);

        linepithema.setCasteTemplates(List.of(linepithemaQueen, linepithemaWorker));
        map.put(linepithema.getPresetName(), linepithema);

        // 15. European Harvester Ant (Messor barbarus - Granivore)
        CustomSpecies messor = new CustomSpecies();
        messor.setPresetName("Fourmi Moissonneuse (Messor barbarus)");
        messor.setCommonName("European Harvester Ant");
        messor.setScientificName("Messor barbarus");
        messor.setInsectType("ANT");
        messor.setDescription("Polymorphic granivorous ant collecting seeds and processing ant bread in underground granaries.");
        messor.setQueenCountMode("MONOGYNE");
        messor.setQueenCount(1);
        messor.setQueenLifespan(7300);
        messor.setQueenEggLayingRate(35.0f);
        messor.setNuptialFlightType("AERIAL_SWARM");
        messor.setWorkerLifespan(1095);
        messor.setWorkerSpeed(0.5f);
        messor.setTypicalColonySize(10000);
        messor.setFormsMegaColonies(false);
        messor.setPrimaryDiet("SEEDS");
        messor.setSecondaryDiet("HONEYDEW");
        messor.setDailyFoodConsumption(0.45f);
        messor.setWaterRequirement(0.1f);
        messor.setNestType("MATURE");
        messor.setVenomType("POWERFUL_MANDIBLES");
        messor.setAggression(0.5f);
        messor.setTerritoriality(0.6f);
        messor.setOptimalTempCelsius(28.0f);
        messor.setMinTempCelsius(10.0f);
        messor.setMaxTempCelsius(40.0f);
        messor.setOptimalHumidityPercent(55.0f);
        messor.setMinHumidityPercent(15.0f);
        messor.setMaxHumidityPercent(85.0f);
        messor.setMetabolism(1.0f);
        messor.setStrength(10.0f);

        CasteTemplate messorQueen = new CasteTemplate("Queen", 600f, 15f);
        messorQueen.setLifespan(7300);
        messorQueen.setBodyLengthMm(15.0f);
        messorQueen.setHeadWidthMm(3.5f);
        messorQueen.setTargetRatio(0.00f);

        CasteTemplate messorMinor = new CasteTemplate("Minor Worker", 60f, 4f);
        messorMinor.setLifespan(1095);
        messorMinor.setCanDig(true);
        messorMinor.setBodyLengthMm(4.0f);
        messorMinor.setHeadWidthMm(1.0f);
        messorMinor.setTargetRatio(0.60f);

        CasteTemplate messorMajor = new CasteTemplate("Major Worker (Seed Crusher)", 220f, 30f);
        messorMajor.setLifespan(1095);
        messorMajor.setCanCarry(true);
        messorMajor.setBodyLengthMm(11.0f);
        messorMajor.setHeadWidthMm(3.2f);
        messorMajor.setTargetRatio(0.40f);

        messor.setCasteTemplates(List.of(messorQueen, messorMinor, messorMajor));
        map.put(messor.getPresetName(), messor);

        // 16. Giant Carpenter Ant (Camponotus ligniperda - Wood Nesting)
        CustomSpecies camponotus = new CustomSpecies();
        camponotus.setPresetName("Fourmi Charpentière (Camponotus ligniperda)");
        camponotus.setCommonName("Giant Carpenter Ant");
        camponotus.setScientificName("Camponotus ligniperda");
        camponotus.setInsectType("ANT");
        camponotus.setDescription("One of the largest European ant species, nesting in decaying wood and exhibits active trophallaxis.");
        camponotus.setQueenCountMode("MONOGYNE");
        camponotus.setQueenCount(1);
        camponotus.setQueenLifespan(5475);
        camponotus.setQueenEggLayingRate(20.0f);
        camponotus.setNuptialFlightType("AERIAL_SWARM");
        camponotus.setWorkerLifespan(1460);
        camponotus.setWorkerSpeed(0.6f);
        camponotus.setTypicalColonySize(8000);
        camponotus.setFormsMegaColonies(false);
        camponotus.setPrimaryDiet("HONEYDEW");
        camponotus.setSecondaryDiet("INSECTS_MEAT");
        camponotus.setDailyFoodConsumption(0.5f);
        camponotus.setWaterRequirement(0.2f);
        camponotus.setNestType("MATURE");
        camponotus.setVenomType("FORMIC_ACID");
        camponotus.setAggression(0.6f);
        camponotus.setTerritoriality(0.7f);
        camponotus.setOptimalTempCelsius(22.0f);
        camponotus.setMinTempCelsius(0.0f);
        camponotus.setMaxTempCelsius(42.0f);
        camponotus.setOptimalHumidityPercent(70.0f);
        camponotus.setMinHumidityPercent(30.0f);
        camponotus.setMaxHumidityPercent(95.0f);
        camponotus.setMetabolism(1.0f);
        camponotus.setStrength(15.0f);

        CasteTemplate campQueen = new CasteTemplate("Queen", 700f, 25f);
        campQueen.setLifespan(5475);
        campQueen.setBodyLengthMm(18.0f);
        campQueen.setHeadWidthMm(4.0f);
        campQueen.setTargetRatio(0.00f);

        CasteTemplate campMinor = new CasteTemplate("Minor Worker", 100f, 8f);
        campMinor.setLifespan(1460);
        campMinor.setCanDig(true);
        campMinor.setCanCarry(true);
        campMinor.setBodyLengthMm(7.0f);
        campMinor.setHeadWidthMm(1.8f);
        campMinor.setTargetRatio(0.70f);

        CasteTemplate campMajor = new CasteTemplate("Major Worker", 250f, 35f);
        campMajor.setLifespan(1460);
        campMajor.setCanCarry(true);
        campMajor.setBodyLengthMm(14.0f);
        campMajor.setHeadWidthMm(3.8f);
        campMajor.setTargetRatio(0.30f);

        camponotus.setCasteTemplates(List.of(campQueen, campMinor, campMajor));
        map.put(camponotus.getPresetName(), camponotus);

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

    public CustomSpecies getPresetOrFallback(String name) {
        if (name == null || name.isBlank()) {
            return presets.isEmpty() ? null : presets.values().iterator().next();
        }
        CustomSpecies sp = presets.get(name);
        if (sp != null) return sp;
        String cleanName = name.replaceAll("\\s*\\([^)]*\\)", "").trim();
        for (CustomSpecies s : presets.values()) {
            if (s.getPresetName().equalsIgnoreCase(name) ||
                s.getPresetName().equalsIgnoreCase(cleanName) ||
                (s.getCommonName() != null && (s.getCommonName().equalsIgnoreCase(name) || s.getCommonName().equalsIgnoreCase(cleanName))) ||
                (s.getScientificName() != null && (s.getScientificName().equalsIgnoreCase(name) || s.getScientificName().equalsIgnoreCase(cleanName))) ||
                (s.getScientificName() != null && !cleanName.isBlank() && (cleanName.toLowerCase().contains(s.getScientificName().toLowerCase()) || s.getScientificName().toLowerCase().contains(cleanName.toLowerCase()))) ||
                (s.getPresetName() != null && !cleanName.isBlank() && (cleanName.toLowerCase().contains(s.getPresetName().toLowerCase()) || s.getPresetName().toLowerCase().contains(cleanName.toLowerCase())))) {
                return s;
            }
        }
        CustomSpecies fallback = new CustomSpecies();
        fallback.setPresetName(name);
        fallback.setCommonName(cleanName.isEmpty() ? name : cleanName);
        fallback.setScientificName(cleanName.isEmpty() ? name : cleanName);
        fallback.setInsectType("ANT");
        fallback.setNestType("MATURE");
        fallback.setOptimalTempCelsius(24.0f);
        fallback.setMinTempCelsius(0.0f);
        fallback.setMaxTempCelsius(45.0f);
        fallback.setOptimalHumidityPercent(75.0f);
        fallback.setMinHumidityPercent(15.0f);
        fallback.setMaxHumidityPercent(100.0f);
        fallback.setDescription("Dynamically synthesized fallback species.");
        presets.put(name, fallback);
        org.swarmforge.core.species.SpeciesRegistry.getInstance().register(fallback);
        return fallback;
    }
}
