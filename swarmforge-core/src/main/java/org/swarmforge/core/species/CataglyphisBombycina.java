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
 * Cataglyphis bombycina - Saharan Silver Ant
 * Thermophilic desert scavenger capable of stilt-walking locomotion to exploit the thermal boundary layer
 * and UV polarized light celestial compass path integration under extreme desert surface temperatures (>50°C).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public class CataglyphisBombycina extends CustomSpecies {

    public CataglyphisBombycina() {
        setPresetName("Fourmi Argentée du Sahara (Cataglyphis bombycina)");
        setCommonName("Saharan Silver Ant");
        setScientificName("Cataglyphis bombycina");
        setInsectType("ANT");
        setDescription("Extreme thermophile desert ant using stilt-walking and UV polarization compass to forage across hot sand dunes.");
        setQueenCountMode("MONOGYNE");
        setQueenCount(1);
        setQueenLifespan(365 * 10);
        setWorkerLifespan(365);
        setWorkerSpeed(1.2f);
        setViewDistance(8.0f);
        setTypicalColonySize(3000);
        setFormsMegaColonies(false);
        setPrimaryDiet("INSECTS_MEAT");
        setSecondaryDiet("SUGARS_NECTAR");
        setOptimalTempCelsius(42.0f);
        setMinTempCelsius(16.0f);
        setMaxTempCelsius(53.6f);
        setOptimalHumidityPercent(20.0f);
        setMinHumidityPercent(5.0f);
        setMaxHumidityPercent(60.0f);
        setNestType("UNDERGROUND_BURROW");
        setVenomType("NONE");
        setAggression(0.4f);

        // Ethological & Biomechanical specializations
        setCanStiltWalkThermalRegim(true);
        setHasUVPolarizedLightNavigation(true);
        setHasPolarizedLightNavigation(true);
        setCanNavigatePolarizedTwilightUV(true);
        setCanNavigatePolarizedLightCompass(true);
        setCanHarvestDewCondensation(true);

        CasteTemplate queen = new CasteTemplate("Reine Argentée", 450f, 15f);
        queen.setLifespan(365 * 10);
        queen.setWalkSpeedMps(0.35f);
        queen.setBodyLengthMm(11.0f);
        queen.setHeadWidthMm(2.6f);

        CasteTemplate forager = new CasteTemplate("Ouvrière Forageuse (Échassière)", 90f, 8f);
        forager.setDescription("Long-legged silver-coated thermophilic scavenger running at high speed across sand dunes.");
        forager.setLifespan(365);
        forager.setWalkSpeedMps(1.1f);
        forager.setFlySpeedMps(0.0f);
        forager.setProteinCost(20f);
        forager.setCarbohydrateCost(35f);
        forager.setWaterCost(5f);
        forager.setBodyLengthMm(8.5f);
        forager.setHeadWidthMm(1.8f);

        CasteTemplate major = new CasteTemplate("Soldat Major", 160f, 22f);
        major.setDescription("Large desert soldier with robust mandibles defending nest entrances from solifuges and lizards.");
        major.setLifespan(365);
        major.setBaseDefense(6f);
        major.setWalkSpeedMps(0.9f);
        major.setBodyLengthMm(12.0f);
        major.setHeadWidthMm(3.2f);

        CasteTemplate male = new CasteTemplate("Mâle Reproducteur (Alé)", 50f, 0f);
        male.setDescription("Mâle ailé");
        male.setLifespan(30);
        male.setCanFly(true);
        male.setWalkSpeedMps(0.3f);
        male.setFlySpeedMps(3.2f);
        male.setBodyLengthMm(7.0f);
        male.setHeadWidthMm(1.6f);

        setCasteTemplates(List.of(queen, forager, major, male));
    }
}
