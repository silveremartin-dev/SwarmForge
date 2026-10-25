/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import java.io.Serializable;

/**
 * Serializable domain trait encapsulating Mega-Batch Wave 1 behavioral systems
 * (#89 -> #141) for {@link Individual}.
 */
public class IndividualEthologyWave1Trait implements Serializable {
    private static final long serialVersionUID = 1L;

    boolean mudResinFunnelConstructed = false;
    int fungalCombAerationPerforations = 0;
    boolean phonicIsolationChamberBuilt = false;
    int clayBreachRepairCount = 0;
    float harvestedDewMl = 0.0f;
    float mandibleWaterDropletMl = 0.0f;
    float saltCrystalsMg = 0.0f;
    boolean cockedGasterDefense = false;
    boolean shimmeringWaveActive = false;
    float lastPreyPheromoneMassDeposited = 0.0f;
    boolean ridingOnLeafForager = false;
    boolean solarBroodBaskingActive = false;
    boolean droughtVibratoDancing = false;
    float stercoralCementMortarMg = 0.0f;
    boolean evaporativeCoolingActive = false;
    float moundSolarOrientationDegrees = 180.0f;
    boolean thoracicIncubationActive = false;
    boolean ritualJoustingActive = false;
    boolean robberRaidActive = false;
    float plunderedHoneyReserves = 0.0f;
    boolean emergencySwarmingTriggered = false;
    float spiralClayPillarHeightM = 0.0f;
    int culledInfectedAphidsCount = 0;
    boolean livingBridgeActive = false;
    float livingBridgeSpanMeters = 0.0f;
    float preySurgeAcousticDb = 0.0f;
    float woodFungusCombSubstrateMg = 0.0f;
    boolean emergencyEscapeAlarmActive = false;
    boolean queenChamberWaxSealed = false;
    boolean suctionEscapeClamped = false;
    float ventilationFlowRateLpm = 0.0f;
    float collapsiblePitTrapRadiusM = 0.0f;
    int thermoregulatedConduitsCount = 0;
    float toxicPlantResinCarriedMg = 0.0f;
    boolean soilDustCamouflageApplied = false;
    int interlockedMandibleBroodChainCount = 0;
    int encapsulatedLargeMummiesCount = 0;
    boolean hydrophobicGalleryFilmApplied = false;
    boolean honeyStoreBrickPluggingActive = false;

    public void harvestDewDrops(Individual self, float ml) {
        this.harvestedDewMl += Math.max(0.0f, ml);
        self.setThirst(Math.max(0.0f, self.getThirst() - ml * 10.0f));
    }

    public float unloadMandibleWaterDroplet() {
        float drop = this.mandibleWaterDropletMl;
        this.mandibleWaterDropletMl = 0.0f;
        return drop;
    }

    public boolean feedSaltCrystalsToLarva(Individual larva) {
        if (saltCrystalsMg < 2.0f || larva == null) return false;
        saltCrystalsMg -= 2.0f;
        larva.setHealth(Math.min(larva.getMaxHealth(), larva.getHealth() + 5.0f));
        return true;
    }

    public boolean relaySeedHandoff(Individual self, Individual receiver) {
        if (self.getCarriedItem() != Individual.CarriedItem.FOOD || receiver == null || receiver.getCarriedItem() != Individual.CarriedItem.NONE) return false;
        self.setCarriedItem(Individual.CarriedItem.NONE);
        receiver.setCarriedItem(Individual.CarriedItem.FOOD);
        receiver.setCarriedResourceType(ResourceType.SEED);
        return true;
    }

    public boolean dischargeCockedGasterVenom(Individual target) {
        if (!cockedGasterDefense || target == null || !target.isAlive()) return false;
        target.takeDamage(12.0f, "Acrobat Ant Venom Droplet");
        return true;
    }

    public boolean coatCallowExoskeletonAcid(Individual self, Individual callow) {
        if (callow == null || self.getChemicalDefenseTrait().getFormicAcidGland() < 5.0f) return false;
        self.getChemicalDefenseTrait().setFormicAcidGland(self.getChemicalDefenseTrait().getFormicAcidGland() - 5.0f);
        callow.setHealth(Math.min(callow.getMaxHealth(), callow.getHealth() + 10.0f));
        return true;
    }

    public void baskBroodInSun(Individual self, float durationSec) {
        this.solarBroodBaskingActive = true;
        self.setEnergy(Math.min(self.getMaxEnergy(), self.getEnergy() + durationSec * 0.1f));
    }

    public boolean recycleLarvalExuviaChitin(Individual larva) {
        if (larva == null || !larva.isAlive()) return false;
        larva.setEnergy(Math.min(larva.getMaxEnergy(), larva.getEnergy() + 12.0f));
        return true;
    }

    public void performEvaporativeCoolingFanning(Individual self, float waterDropMl) {
        this.evaporativeCoolingActive = true;
        self.lotsTrait.wingFanning = true;
        self.lotsTrait.thoraxTemperatureC = Math.max(22.0f, self.lotsTrait.thoraxTemperatureC - waterDropMl * 15.0f);
    }

    public boolean allogroomPartner(Individual self, Individual partner) {
        if (partner == null || !partner.isAlive() || self.getColonyId() != partner.getColonyId()) return false;
        partner.setHealth(Math.min(partner.getMaxHealth(), partner.getHealth() + 4.0f));
        return true;
    }

    public void incubateBroodThoracicHeat(Individual self, Individual brood) {
        this.thoracicIncubationActive = true;
        self.lotsTrait.shiveringThermogenesis = true;
        self.lotsTrait.thoraxTemperatureC = 39.5f;
        if (brood != null) {
            brood.setAge(brood.getAge() + 15.0f);
        }
    }

    public boolean engageRitualJoustingDisplay(Individual self, Individual rival) {
        if (rival == null || !rival.isAlive() || self.getColonyId().equals(rival.getColonyId())) return false;
        this.ritualJoustingActive = true;
        rival.wave1Trait.ritualJoustingActive = true;
        return true;
    }

    public void lootEnemyHiveReserves(float sugarAmt, float propolisAmt) {
        this.robberRaidActive = true;
        this.plunderedHoneyReserves += (sugarAmt + propolisAmt);
    }

    public void triggerEmergencySwarmingFission(Individual self) {
        this.emergencySwarmingTriggered = true;
        self.setState(Individual.AiState.FLEEING);
    }

    public boolean waterTrophallaxisTransfer(Individual recipient, float amountMl) {
        if (recipient == null || !recipient.isAlive() || amountMl <= 0) return false;
        recipient.setThirst(Math.max(0.0f, recipient.getThirst() - amountMl * 50.0f));
        return true;
    }

    public void formLivingBridgeSpan(float gapMeters) {
        this.livingBridgeActive = true;
        this.livingBridgeSpanMeters = Math.max(0.0f, gapMeters);
    }

    public void emitPreySurgeAcousticSignal(Individual self) {
        this.preySurgeAcousticDb = 75.0f;
        self.setOctopamine(1.0f);
    }

    public void depositEmergencyEscapeAlarm(Individual self) {
        this.emergencyEscapeAlarmActive = true;
        self.setState(Individual.AiState.FLEEING);
    }

    public boolean stridulateQueenRecognitionPacification(Individual self, Individual worker) {
        if (worker == null || self.getCaste() != Individual.Caste.QUEEN) return false;
        worker.setOctopamine(0.3f);
        return true;
    }

    public boolean carryBroodToSunlitMound(Individual self, Individual brood) {
        if (brood == null || self.getCarriedItem() != Individual.CarriedItem.NONE) return false;
        self.setCarriedItem(Individual.CarriedItem.BROOD);
        this.solarBroodBaskingActive = true;
        return true;
    }

    public boolean exchangeChcGestalt(Individual self, Individual nestmate) {
        return nestmate != null && nestmate.isAlive() && self.getColonyId() == nestmate.getColonyId();
    }

    public void excavateThermoregulatedConduits(Individual self, int count) {
        this.thermoregulatedConduitsCount += Math.max(0, count);
        self.lotsTrait.dualConduitsActive = true;
    }

    public void applySoilDustCamouflage(Individual self) {
        this.soilDustCamouflageApplied = true;
        self.lotsTrait.plantCuticularCamouflagePercent = Math.min(100.0f, self.lotsTrait.plantCuticularCamouflagePercent + 60.0f);
    }

    public boolean linkMandibleBroodChain(Individual self, Individual larva) {
        if (larva == null) return false;
        this.interlockedMandibleBroodChainCount++;
        self.setCarriedItem(Individual.CarriedItem.BROOD);
        return true;
    }

    public boolean transferInhibitoryOvaryPeptides(Individual self, Individual worker) {
        if (worker == null || self.getCaste() != Individual.Caste.QUEEN) return false;
        worker.setRoyalPheromoneInhibitionTiter(1.0f);
        return true;
    }

    public boolean encapsulateLargeCarcassMummy(float clayMg, float propolisMg) {
        if (clayMg < 10.0f || propolisMg < 5.0f) return false;
        this.encapsulatedLargeMummiesCount++;
        return true;
    }

    public boolean preFlightVirginQueenLipidEnrichment(Individual virginQueen) {
        if (virginQueen == null || virginQueen.getCaste() != Individual.Caste.QUEEN) return false;
        virginQueen.setMaxEnergy(150.0f);
        virginQueen.setEnergy(150.0f);
        return true;
    }
}
