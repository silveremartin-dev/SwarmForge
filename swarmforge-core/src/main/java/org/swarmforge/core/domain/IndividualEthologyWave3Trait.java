/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import java.io.Serializable;

/**
 * Serializable domain trait encapsulating Mega-Batch Wave 3 behavioral systems
 * (#169 -> #220) for {@link Individual}.
 */
public class IndividualEthologyWave3Trait implements Serializable {
    private static final long serialVersionUID = 1L;

    float attaGardenWasteChamberDigVolumeM3 = 0.0f;
    float termiteRoyalPairMutualGroomingSec = 0.0f;
    boolean universalEmergencyEvacuationActive = false;
    float myrmecocystusRepleteGasterVolumeUl = 0.0f;
    int floatingAntRaftClawInterlockCount = 0;
    float mudResinTrumpetFunnelHeightCm = 0.0f;
    float bombusOverwinteringHibernaculumDepthCm = 0.0f;
    float acromyrmexLeafMicroMasticationEnzymeMg = 0.0f;
    boolean dinoponeraGamergateDominanceStingSmear = false;
    float draculaLarvalHemolymphSustenanceDose = 0.0f;
    float oecophyllaTarsalFrictionBridgeTensileKg = 0.0f;
    float pachycondylaMandibleSurfaceTensionDropUl = 0.0f;
    float giantHoneybeeShimmeringWavePhaseRad = 0.0f;
    float paperWaspEvaporativeWaterDouseMl = 0.0f;
    int termiteFungalCombClayMicroPoresCount = 0;
    float passalidLarvalChitinNitrogenRecyclingMg = 0.0f;
    int attaMinimPhoridFlyEggGroomingCount = 0;
    float socialSpiderWebDebrisDisguisePercent = 0.0f;
    boolean acrobatAntGasterVenomAerosolActive = false;
    float lasiusAphidAntennalStrokingRateHz = 0.0f;
    int formicaSolarHeatCollectorClusterCount = 0;
    boolean nectarReceiverTrembleDanceResponseActive = false;
    float subterraneanAirCurrentFlappingHz = 0.0f;
    float socialCropNectarSucroseBrixPercent = 35.0f;
    int foragerVisualLandmarkMemoryEntries = 0;
    float subterraneanCarbonDioxideTolerancePpm = 25000.0f;
    float fungalGardenSterilizingMetapleuralSecretionMg = 0.0f;
    float nurseAntFatBodyVitellogeninTiter = 0.8f;
    float queenNuptialFlightSpermathecaCapacity = 5000000.0f;
    float callowCuticularTanningSclerotizationScore = 1.0f;
    int interColonyWarfareCasualtyAttritionIndex = 0;
    float leafCutterMandibleChitinZincHardeningGpa = 3.2f;
    float subterraneanFungalMoistureSpongeTranslocation = 0.0f;

    public void triggerUniversalEmergencyEvacuationAll(Individual self) {
        this.universalEmergencyEvacuationActive = true;
        self.setState(Individual.AiState.FLEEING);
    }

    public boolean applyDominanceStingSmearTournament(Individual self, Individual rival) {
        if (rival == null) return false;
        this.dinoponeraGamergateDominanceStingSmear = true;
        self.lotsTrait.gamergate = true;
        self.setCaste(Individual.Caste.QUEEN);
        return true;
    }

    public void consumeDraculaLarvalHemolymphDose(Individual self, Individual larva) {
        if (larva != null) {
            this.draculaLarvalHemolymphSustenanceDose += 1.0f;
            self.setEnergy(Math.min(self.getMaxEnergy(), self.getEnergy() + 15.0f));
        }
    }

    public void feedLarvaeNitrogenousExuvia(Individual larva, float mg) {
        if (larva != null) {
            this.passalidLarvalChitinNitrogenRecyclingMg += Math.max(0.0f, mg);
            larva.setEnergy(Math.min(larva.getMaxEnergy(), larva.getEnergy() + mg * 2.0f));
        }
    }

    public void dischargeAcrobatVenomAerosol(Individual attacker) {
        this.acrobatAntGasterVenomAerosolActive = true;
        if (attacker != null) attacker.takeDamage(16.0f, "Acrobat Ant Aerosol Venom");
    }

    public void strokeAphidAntennalHoneydew(Individual self, float durationSec) {
        this.lasiusAphidAntennalStrokingRateHz = 4.5f;
        self.setEnergy(Math.min(self.getMaxEnergy(), self.getEnergy() + durationSec * 2.0f));
    }

    public void baskInSolarMoundCollectorCluster(Individual self, int clusterSize) {
        this.formicaSolarHeatCollectorClusterCount = Math.max(0, clusterSize);
        self.lotsTrait.thoraxTemperatureC = 36.0f;
    }

    public boolean evaluateUnicolonialChcAcceptance(Individual self, Individual alienAnt) {
        if (alienAnt == null) return false;
        if (self.getSpecies() == null || alienAnt.getSpecies() == null) return true;
        return self.getSpecies().equals(alienAnt.getSpecies());
    }
}
