/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.species;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.BitSet;

/**
 * Base ethological capability traits and bitset foundation for social insects (Batches I to VIII).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class BaseSpeciesCapabilities implements Species {

    @JsonIgnore
    protected final BitSet capabilitiesBitSet = new BitSet(256);

    protected boolean canPerformBiostructures = false;
    protected boolean canPerformNecrophoresis = true;
    protected boolean canPerformSocialThermoregulation = false;
    protected boolean isUnicolonial = false;
    protected boolean hasMandibularWearPolyethism = true;
    protected boolean canPerformTandemRunning = false;
    protected boolean canPerformWaggleDance = false;
    protected boolean canPerformLarvalSalivaryTrophallaxis = false;
    protected boolean hasTermiteGutSymbiosis = false;
    protected boolean canFarmAphids = false;
    protected boolean canFarmFungus = false;
    protected boolean hasRoyalPheromoneInhibition = true;
    protected boolean canDrumSubstrate = false;
    protected boolean isPolycalic = false;
    protected boolean canCollectPropolis = false;
    protected boolean canSewLeavesWithLarvalSilk = false;
    protected boolean canWeedFungusGarden = false;
    protected boolean canMakeStercoralCement = false;
    protected boolean hasProctodealTrophallaxis = false;
    protected boolean canPerformPhragmosis = false;
    protected boolean canPerformEvaporativeCooling = false;
    protected boolean hasTrapJawMechanism = false;
    protected boolean isSlaveMakingSpecies = false;
    protected boolean canFormLivingBivouac = false;
    protected boolean hasSolarOrientedMound = false;
    protected boolean canPerformAllogrooming = true;
    protected boolean canPerformTrembleDance = false;
    protected boolean hasThermalTrailDecay = true;
    protected boolean canPerformThoracicIncubation = false;
    protected boolean canPerformRitualJousting = false;
    protected boolean hasTerritorialRepellentPheromone = false;
    protected boolean canDetectHydrostaticPressure = true;
    protected boolean isRobberBeeSpecies = false;
    protected boolean canStridulateRescueCall = false;
    protected boolean isHoneypotStorageCaste = false;
    protected boolean canPlugContaminatedGalleries = false;
    protected boolean hasOleicAcidThresholdNecrophoresis = true;
    protected boolean hasUVPolarizedLightNavigation = false;
    protected boolean canPerformThermalBalling = false;
    protected boolean canFormLivingRaft = false;
    protected boolean canInhabitDomatia = false;
    protected boolean canSelfIsolateWhenInfected = true;
    protected boolean canSprayFormicResinDisinfectant = false;
    protected boolean canTriggerEmergencySwarming = true;
    protected boolean canConstructClayPillars = false;
    protected boolean canDeGermStoredSeeds = false;
    protected boolean canPerformQueenPiping = false;
    protected boolean canPerformWaterTrophallaxis = true;
    protected boolean canEnforceAphidSanitaryCordon = false;
    protected boolean canFormLivingBridges = false;
    protected boolean canEmitAcousticPreySurge = false;
    protected boolean canSortExternalRefusePits = true;
    protected boolean canCultivateWoodFungus = false;
    protected boolean hasEmergencyEscapePheromone = true;
    protected boolean canSealQueenChamberWax = false;
    protected boolean hasCasteRatioPheromoneInhibition = true;
    protected boolean canPerformSuctionEscapePosture = false;
    protected boolean canStridulateQueenRecognition = false;
    protected boolean canPerformPulsatileVentilation = false;
    protected boolean canRepairBreachesClay = true;
    protected boolean hasDepletingTrailPheromone = true;
    protected boolean canRecycleInviableEggs = true;
    protected boolean canQuarantineInvasiveParasites = false;
    protected boolean canPerformArborealGlidingEscape = false;
    protected boolean canPerformSolarBroodBasking = false;
    protected boolean canHarmonizeChcGestalt = true;
    protected boolean canConstructCollapsiblePitTraps = false;
    protected boolean canHarvestDewCondensation = true;
    protected boolean canPerformExoskeletonAntiFungalPatrol = true;
    protected boolean canPerformGuardShiftVibrationalWhisper = false;
    protected boolean canConstructThermoregulatedConduits = false;
    protected boolean canRaidToxicPlantResin = false;
    protected boolean canApplyDustSubstrateCamouflage = false;
    protected boolean canTransportChainBrood = true;
    protected boolean hasTrophallacticOvaryInhibition = true;
    protected boolean canPerformDroughtVibratoDance = false;
    protected boolean canEncapsulateLargeIntrudersClay = true;
    protected boolean canConstructPhonicIsolationChambers = false;
    protected boolean canApplyHydrophobicTrailCoating = false;
    protected boolean canConsumeFermentedSapAnesthetic = false;
    protected boolean canPerformRelaySeedTransport = true;
    protected boolean hasPreySizeSelectivePheromones = true;
    protected boolean canDryLarvaeWoodDust = true;
    protected boolean canPerformFanoutEscapeFormicAcid = true;
    protected boolean canEmitMoundOverheatVibrato = false;
    protected boolean canNourishVirginQueensPreFlight = true;
    protected boolean canPlugHoneyStoresBricks = true;


    @Override public boolean canPerformBiostructures() { return canPerformBiostructures; }
    public void setCanPerformBiostructures(boolean val) { this.canPerformBiostructures = val; }

    @Override public boolean canPerformNecrophoresis() { return canPerformNecrophoresis; }
    public void setCanPerformNecrophoresis(boolean val) { this.canPerformNecrophoresis = val; }

    @Override public boolean canPerformSocialThermoregulation() { return canPerformSocialThermoregulation; }
    public void setCanPerformSocialThermoregulation(boolean val) { this.canPerformSocialThermoregulation = val; }

    @Override public boolean isUnicolonial() { return isUnicolonial; }
    public void setIsUnicolonial(boolean val) { this.isUnicolonial = val; }

    @Override public boolean hasMandibularWearPolyethism() { return hasMandibularWearPolyethism; }
    public void setHasMandibularWearPolyethism(boolean val) { this.hasMandibularWearPolyethism = val; }

    @Override public boolean canPerformTandemRunning() { return canPerformTandemRunning; }
    public void setCanPerformTandemRunning(boolean val) { this.canPerformTandemRunning = val; }

    @Override public boolean canPerformWaggleDance() { return canPerformWaggleDance; }
    public void setCanPerformWaggleDance(boolean val) { this.canPerformWaggleDance = val; }

    @Override public boolean canPerformLarvalSalivaryTrophallaxis() { return canPerformLarvalSalivaryTrophallaxis; }
    public void setCanPerformLarvalSalivaryTrophallaxis(boolean val) { this.canPerformLarvalSalivaryTrophallaxis = val; }

    @Override public boolean hasTermiteGutSymbiosis() { return hasTermiteGutSymbiosis; }
    public void setHasTermiteGutSymbiosis(boolean val) { this.hasTermiteGutSymbiosis = val; }

    @Override public boolean canFarmAphids() { return canFarmAphids; }
    public void setCanFarmAphids(boolean val) { this.canFarmAphids = val; }

    @Override public boolean canFarmFungus() { return canFarmFungus || canWeedFungusGarden || "FUNGUS".equalsIgnoreCase(getPrimaryDiet()); }
    public void setCanFarmFungus(boolean val) { this.canFarmFungus = val; }

    @Override public boolean hasRoyalPheromoneInhibition() { return hasRoyalPheromoneInhibition; }
    public void setHasRoyalPheromoneInhibition(boolean val) { this.hasRoyalPheromoneInhibition = val; }

    @Override public boolean canDrumSubstrate() { return canDrumSubstrate; }
    public void setCanDrumSubstrate(boolean val) { this.canDrumSubstrate = val; }

    @Override public boolean isPolycalic() { return isPolycalic; }
    public void setIsPolycalic(boolean val) { this.isPolycalic = val; }

    @Override public boolean canCollectPropolis() { return canCollectPropolis; }
    public void setCanCollectPropolis(boolean val) { this.canCollectPropolis = val; }

    @Override public boolean canSewLeavesWithLarvalSilk() { return canSewLeavesWithLarvalSilk; }
    public void setCanSewLeavesWithLarvalSilk(boolean val) { this.canSewLeavesWithLarvalSilk = val; }

    @Override public boolean canWeedFungusGarden() { return canWeedFungusGarden; }
    public void setCanWeedFungusGarden(boolean val) { this.canWeedFungusGarden = val; }

    @Override public boolean canMakeStercoralCement() { return canMakeStercoralCement; }
    public void setCanMakeStercoralCement(boolean val) { this.canMakeStercoralCement = val; }

    @Override public boolean hasProctodealTrophallaxis() { return hasProctodealTrophallaxis; }
    public void setHasProctodealTrophallaxis(boolean val) { this.hasProctodealTrophallaxis = val; }

    @Override public boolean canPerformPhragmosis() { return canPerformPhragmosis; }
    public void setCanPerformPhragmosis(boolean val) { this.canPerformPhragmosis = val; }

    @Override public boolean canPerformEvaporativeCooling() { return canPerformEvaporativeCooling; }
    public void setCanPerformEvaporativeCooling(boolean val) { this.canPerformEvaporativeCooling = val; }

    @Override public boolean hasTrapJawMechanism() { return hasTrapJawMechanism; }
    public void setHasTrapJawMechanism(boolean val) { this.hasTrapJawMechanism = val; }

    @Override public boolean isSlaveMakingSpecies() { return isSlaveMakingSpecies; }
    public void setIsSlaveMakingSpecies(boolean val) { this.isSlaveMakingSpecies = val; }

    @Override public boolean canFormLivingBivouac() { return canFormLivingBivouac; }
    public void setCanFormLivingBivouac(boolean val) { this.canFormLivingBivouac = val; }

    @Override public boolean hasSolarOrientedMound() { return hasSolarOrientedMound; }
    public void setHasSolarOrientedMound(boolean val) { this.hasSolarOrientedMound = val; }

    @Override public boolean canPerformAllogrooming() { return canPerformAllogrooming; }
    public void setCanPerformAllogrooming(boolean val) { this.canPerformAllogrooming = val; }

    @Override public boolean canPerformTrembleDance() { return canPerformTrembleDance; }
    public void setCanPerformTrembleDance(boolean val) { this.canPerformTrembleDance = val; }

    @Override public boolean hasThermalTrailDecay() { return hasThermalTrailDecay; }
    public void setHasThermalTrailDecay(boolean val) { this.hasThermalTrailDecay = val; }

    @Override public boolean canPerformThoracicIncubation() { return canPerformThoracicIncubation; }
    public void setCanPerformThoracicIncubation(boolean val) { this.canPerformThoracicIncubation = val; }

    @Override public boolean canPerformRitualJousting() { return canPerformRitualJousting; }
    public void setCanPerformRitualJousting(boolean val) { this.canPerformRitualJousting = val; }

    @Override public boolean hasTerritorialRepellentPheromone() { return hasTerritorialRepellentPheromone; }
    public void setHasTerritorialRepellentPheromone(boolean val) { this.hasTerritorialRepellentPheromone = val; }

    @Override public boolean canDetectHydrostaticPressure() { return canDetectHydrostaticPressure; }
    public void setCanDetectHydrostaticPressure(boolean val) { this.canDetectHydrostaticPressure = val; }

    @Override public boolean isRobberBeeSpecies() { return isRobberBeeSpecies; }
    public void setIsRobberBeeSpecies(boolean val) { this.isRobberBeeSpecies = val; }

    @Override public boolean canStridulateRescueCall() { return canStridulateRescueCall; }
    public void setCanStridulateRescueCall(boolean val) { this.canStridulateRescueCall = val; }

    @Override public boolean isHoneypotStorageCaste() { return isHoneypotStorageCaste; }
    public void setIsHoneypotStorageCaste(boolean val) { this.isHoneypotStorageCaste = val; }

    @Override public boolean canPlugContaminatedGalleries() { return canPlugContaminatedGalleries; }
    public void setCanPlugContaminatedGalleries(boolean val) { this.canPlugContaminatedGalleries = val; }

    @Override public boolean hasOleicAcidThresholdNecrophoresis() { return hasOleicAcidThresholdNecrophoresis; }
    public void setHasOleicAcidThresholdNecrophoresis(boolean val) { this.hasOleicAcidThresholdNecrophoresis = val; }

    @Override public boolean hasUVPolarizedLightNavigation() { return hasUVPolarizedLightNavigation; }
    public void setHasUVPolarizedLightNavigation(boolean val) { this.hasUVPolarizedLightNavigation = val; }

    @Override public boolean canPerformThermalBalling() { return canPerformThermalBalling; }
    public void setCanPerformThermalBalling(boolean val) { this.canPerformThermalBalling = val; }

    @Override public boolean canFormLivingRaft() { return canFormLivingRaft; }
    public void setCanFormLivingRaft(boolean val) { this.canFormLivingRaft = val; }

    @Override public boolean canInhabitDomatia() { return canInhabitDomatia; }
    public void setCanInhabitDomatia(boolean val) { this.canInhabitDomatia = val; }

    @Override public boolean canSelfIsolateWhenInfected() { return canSelfIsolateWhenInfected; }
    public void setCanSelfIsolateWhenInfected(boolean val) { this.canSelfIsolateWhenInfected = val; }

    @Override public boolean canSprayFormicResinDisinfectant() { return canSprayFormicResinDisinfectant; }
    public void setCanSprayFormicResinDisinfectant(boolean val) { this.canSprayFormicResinDisinfectant = val; }

    @Override public boolean canTriggerEmergencySwarming() { return canTriggerEmergencySwarming; }
    public void setCanTriggerEmergencySwarming(boolean val) { this.canTriggerEmergencySwarming = val; }

    @Override public boolean canConstructClayPillars() { return canConstructClayPillars; }
    public void setCanConstructClayPillars(boolean val) { this.canConstructClayPillars = val; }

    @Override public boolean canDeGermStoredSeeds() { return canDeGermStoredSeeds; }
    public void setCanDeGermStoredSeeds(boolean val) { this.canDeGermStoredSeeds = val; }

    @Override public boolean canPerformQueenPiping() { return canPerformQueenPiping; }
    public void setCanPerformQueenPiping(boolean val) { this.canPerformQueenPiping = val; }

    @Override public boolean canPerformWaterTrophallaxis() { return canPerformWaterTrophallaxis; }
    public void setCanPerformWaterTrophallaxis(boolean val) { this.canPerformWaterTrophallaxis = val; }

    @Override public boolean canEnforceAphidSanitaryCordon() { return canEnforceAphidSanitaryCordon; }
    public void setCanEnforceAphidSanitaryCordon(boolean val) { this.canEnforceAphidSanitaryCordon = val; }

    @Override public boolean canFormLivingBridges() { return canFormLivingBridges; }
    public void setCanFormLivingBridges(boolean val) { this.canFormLivingBridges = val; }

    @Override public boolean canEmitAcousticPreySurge() { return canEmitAcousticPreySurge; }
    public void setCanEmitAcousticPreySurge(boolean val) { this.canEmitAcousticPreySurge = val; }

    @Override public boolean canSortExternalRefusePits() { return canSortExternalRefusePits; }
    public void setCanSortExternalRefusePits(boolean val) { this.canSortExternalRefusePits = val; }

    @Override public boolean canCultivateWoodFungus() { return canCultivateWoodFungus; }
    public void setCanCultivateWoodFungus(boolean val) { this.canCultivateWoodFungus = val; }

    @Override public boolean hasEmergencyEscapePheromone() { return hasEmergencyEscapePheromone; }
    public void setHasEmergencyEscapePheromone(boolean val) { this.hasEmergencyEscapePheromone = val; }

    @Override public boolean canSealQueenChamberWax() { return canSealQueenChamberWax; }
    public void setCanSealQueenChamberWax(boolean val) { this.canSealQueenChamberWax = val; }

    @Override public boolean hasCasteRatioPheromoneInhibition() { return hasCasteRatioPheromoneInhibition; }
    public void setHasCasteRatioPheromoneInhibition(boolean val) { this.hasCasteRatioPheromoneInhibition = val; }

    @Override public boolean canPerformSuctionEscapePosture() { return canPerformSuctionEscapePosture; }
    public void setCanPerformSuctionEscapePosture(boolean val) { this.canPerformSuctionEscapePosture = val; }

    @Override public boolean canStridulateQueenRecognition() { return canStridulateQueenRecognition; }
    public void setCanStridulateQueenRecognition(boolean val) { this.canStridulateQueenRecognition = val; }

    @Override public boolean canPerformPulsatileVentilation() { return canPerformPulsatileVentilation; }
    public void setCanPerformPulsatileVentilation(boolean val) { this.canPerformPulsatileVentilation = val; }

    @Override public boolean canRepairBreachesClay() { return canRepairBreachesClay; }
    public void setCanRepairBreachesClay(boolean val) { this.canRepairBreachesClay = val; }

    @Override public boolean hasDepletingTrailPheromone() { return hasDepletingTrailPheromone; }
    public void setHasDepletingTrailPheromone(boolean val) { this.hasDepletingTrailPheromone = val; }

    @Override public boolean canRecycleInviableEggs() { return canRecycleInviableEggs; }
    public void setCanRecycleInviableEggs(boolean val) { this.canRecycleInviableEggs = val; }

    @Override public boolean canQuarantineInvasiveParasites() { return canQuarantineInvasiveParasites; }
    public void setCanQuarantineInvasiveParasites(boolean val) { this.canQuarantineInvasiveParasites = val; }

    @Override public boolean canPerformArborealGlidingEscape() { return canPerformArborealGlidingEscape; }
    public void setCanPerformArborealGlidingEscape(boolean val) { this.canPerformArborealGlidingEscape = val; }

    @Override public boolean canPerformSolarBroodBasking() { return canPerformSolarBroodBasking; }
    public void setCanPerformSolarBroodBasking(boolean val) { this.canPerformSolarBroodBasking = val; }

    @Override public boolean canHarmonizeChcGestalt() { return canHarmonizeChcGestalt; }
    public void setCanHarmonizeChcGestalt(boolean val) { this.canHarmonizeChcGestalt = val; }

    @Override public boolean canConstructCollapsiblePitTraps() { return canConstructCollapsiblePitTraps; }
    public void setCanConstructCollapsiblePitTraps(boolean val) { this.canConstructCollapsiblePitTraps = val; }

    @Override public boolean canHarvestDewCondensation() { return canHarvestDewCondensation; }
    public void setCanHarvestDewCondensation(boolean val) { this.canHarvestDewCondensation = val; }

    @Override public boolean canPerformExoskeletonAntiFungalPatrol() { return canPerformExoskeletonAntiFungalPatrol; }
    public void setCanPerformExoskeletonAntiFungalPatrol(boolean val) { this.canPerformExoskeletonAntiFungalPatrol = val; }

    @Override public boolean canPerformGuardShiftVibrationalWhisper() { return canPerformGuardShiftVibrationalWhisper; }
    public void setCanPerformGuardShiftVibrationalWhisper(boolean val) { this.canPerformGuardShiftVibrationalWhisper = val; }

    @Override public boolean canConstructThermoregulatedConduits() { return canConstructThermoregulatedConduits; }
    public void setCanConstructThermoregulatedConduits(boolean val) { this.canConstructThermoregulatedConduits = val; }

    @Override public boolean canRaidToxicPlantResin() { return canRaidToxicPlantResin; }
    public void setCanRaidToxicPlantResin(boolean val) { this.canRaidToxicPlantResin = val; }

    @Override public boolean canApplyDustSubstrateCamouflage() { return canApplyDustSubstrateCamouflage; }
    public void setCanApplyDustSubstrateCamouflage(boolean val) { this.canApplyDustSubstrateCamouflage = val; }

    @Override public boolean canTransportChainBrood() { return canTransportChainBrood; }
    public void setCanTransportChainBrood(boolean val) { this.canTransportChainBrood = val; }

    @Override public boolean hasTrophallacticOvaryInhibition() { return hasTrophallacticOvaryInhibition; }
    public void setHasTrophallacticOvaryInhibition(boolean val) { this.hasTrophallacticOvaryInhibition = val; }

    @Override public boolean canPerformDroughtVibratoDance() { return canPerformDroughtVibratoDance; }
    public void setCanPerformDroughtVibratoDance(boolean val) { this.canPerformDroughtVibratoDance = val; }

    @Override public boolean canEncapsulateLargeIntrudersClay() { return canEncapsulateLargeIntrudersClay; }
    public void setCanEncapsulateLargeIntrudersClay(boolean val) { this.canEncapsulateLargeIntrudersClay = val; }

    @Override public boolean canConstructPhonicIsolationChambers() { return canConstructPhonicIsolationChambers; }
    public void setCanConstructPhonicIsolationChambers(boolean val) { this.canConstructPhonicIsolationChambers = val; }

    @Override public boolean canApplyHydrophobicTrailCoating() { return canApplyHydrophobicTrailCoating; }
    public void setCanApplyHydrophobicTrailCoating(boolean val) { this.canApplyHydrophobicTrailCoating = val; }

    @Override public boolean canConsumeFermentedSapAnesthetic() { return canConsumeFermentedSapAnesthetic; }
    public void setCanConsumeFermentedSapAnesthetic(boolean val) { this.canConsumeFermentedSapAnesthetic = val; }

    @Override public boolean canPerformRelaySeedTransport() { return canPerformRelaySeedTransport; }
    public void setCanPerformRelaySeedTransport(boolean val) { this.canPerformRelaySeedTransport = val; }

    @Override public boolean hasPreySizeSelectivePheromones() { return hasPreySizeSelectivePheromones; }
    public void setHasPreySizeSelectivePheromones(boolean val) { this.hasPreySizeSelectivePheromones = val; }

    @Override public boolean canDryLarvaeWoodDust() { return canDryLarvaeWoodDust; }
    public void setCanDryLarvaeWoodDust(boolean val) { this.canDryLarvaeWoodDust = val; }

    @Override public boolean canPerformFanoutEscapeFormicAcid() { return canPerformFanoutEscapeFormicAcid; }
    public void setCanPerformFanoutEscapeFormicAcid(boolean val) { this.canPerformFanoutEscapeFormicAcid = val; }

    @Override public boolean canEmitMoundOverheatVibrato() { return canEmitMoundOverheatVibrato; }
    public void setCanEmitMoundOverheatVibrato(boolean val) { this.canEmitMoundOverheatVibrato = val; }

    @Override public boolean canNourishVirginQueensPreFlight() { return canNourishVirginQueensPreFlight; }
    public void setCanNourishVirginQueensPreFlight(boolean val) { this.canNourishVirginQueensPreFlight = val; }

    @Override public boolean canPlugHoneyStoresBricks() { return canPlugHoneyStoresBricks; }
    public void setCanPlugHoneyStoresBricks(boolean val) { this.canPlugHoneyStoresBricks = val; }

}
