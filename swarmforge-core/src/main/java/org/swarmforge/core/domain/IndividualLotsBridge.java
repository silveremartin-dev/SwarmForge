/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import java.util.UUID;

/**
 * Interface providing default delegates for Specialized Ethology LOTS A through L.
 */
public interface IndividualLotsBridge {

    IndividualSpecializedLotsTrait getLotsTrait();

    default Individual asIndividual() {
        return (Individual) this;
    }

    default float getActinobacteriaResin() { return getLotsTrait().actinobacteriaResin; }
    default void refillActinobacteriaResin(float amt) { getLotsTrait().actinobacteriaResin = Math.min(100.0f, getLotsTrait().actinobacteriaResin + amt); }
    default boolean applyActinobacteriaBioWeeding(org.swarmforge.core.simulation.FungusGarden garden) { return getLotsTrait().applyActinobacteriaBioWeeding(garden); }
    default float getDecompositionAgeSeconds() { return getLotsTrait().decompositionAgeSeconds; }
    default void setDecompositionAgeSeconds(float age) { getLotsTrait().decompositionAgeSeconds = Math.max(0.0f, age); }
    default boolean isOleicAcidEmitted() { return !asIndividual().isAlive() && getLotsTrait().decompositionAgeSeconds >= 180.0f; }
    default boolean pickUpCorpse(Individual corpse) { return getLotsTrait().pickUpCorpse(asIndividual(), corpse); }
    default boolean depositCorpseAtRefuse(float cemeteryX, float cemeteryY, float cemeteryZ) { return getLotsTrait().depositCorpseAtRefuse(asIndividual()); }
    default boolean isShiveringThermogenesis() { return getLotsTrait().shiveringThermogenesis; }
    default void setShiveringThermogenesis(boolean s) { getLotsTrait().shiveringThermogenesis = s; }
    default boolean isWingFanning() { return getLotsTrait().wingFanning; }
    default void setWingFanning(boolean f) { getLotsTrait().wingFanning = f; }
    default float getThoraxTemperatureC() { return getLotsTrait().thoraxTemperatureC; }
    default void setThoraxTemperatureC(float t) { getLotsTrait().thoraxTemperatureC = t; }
    default boolean isEnslaved() { return getLotsTrait().enslaved; }
    default void setEnslaved(boolean enslaved) { getLotsTrait().enslaved = enslaved; }
    default UUID getHostColonyId() { return getLotsTrait().hostColonyId; }
    default void setHostColonyId(UUID id) { getLotsTrait().hostColonyId = id; }
    default boolean stealBrood(Individual broodTarget) { return getLotsTrait().stealBrood(asIndividual(), broodTarget); }
    default void integrateAsEnslavedWorker(UUID masterColonyId) { getLotsTrait().integrateAsEnslavedWorker(asIndividual(), masterColonyId); }
    default boolean isStridulatingRescueCall() { return getLotsTrait().stridulatingRescueCall; }
    default void setStridulatingRescueCall(boolean s) { getLotsTrait().stridulatingRescueCall = s; }
    default float getStridulationFrequencyHz() { return getLotsTrait().stridulationFrequencyHz; }
    default float getStridulationIntensityDb() { return getLotsTrait().stridulationIntensityDb; }
    default void triggerStridulationRescue() { getLotsTrait().stridulatingRescueCall = true; }

    // LOT A: Advanced Biological & Mechanical Defense
    default boolean hasAutothysed() { return getLotsTrait().autothysed; }
    default boolean isPhragmoticShieldActive() { return getLotsTrait().phragmoticShieldActive; }
    default void setPhragmoticShieldActive(boolean active) { getLotsTrait().phragmoticShieldActive = active; }
    default boolean isInThermalBallCluster() { return getLotsTrait().inThermalBallCluster; }
    default void setInThermalBallCluster(boolean cluster) { getLotsTrait().inThermalBallCluster = cluster; }
    default float getNasuteResinReservoir() { return getLotsTrait().nasuteResinReservoir; }
    default void refillNasuteResin(float amt) { getLotsTrait().nasuteResinReservoir = Math.min(100.0f, getLotsTrait().nasuteResinReservoir + amt); }
    default boolean performAutothysis(java.util.List<Individual> nearbyEnemies) { return getLotsTrait().performAutothysis(asIndividual(), nearbyEnemies); }
    default boolean performThermalBalling(Individual targetPredator, float clusterPower) { return getLotsTrait().performThermalBalling(asIndividual(), targetPredator, clusterPower); }
    default boolean squirtNasuteResin(Individual targetEnemy, float distance) { return getLotsTrait().squirtNasuteResin(asIndividual(), targetEnemy, distance); }
    default boolean performBarbedBeeSting(Individual targetEnemy) { return getLotsTrait().performBarbedBeeSting(asIndividual(), targetEnemy); }
    default boolean performMammalianDefensiveSting(float envenomationDamage) { return getLotsTrait().performMammalianDefensiveSting(asIndividual(), envenomationDamage); }
    default boolean performBarbedBeeSting(Predator targetPredator) { return getLotsTrait().performBarbedBeeSting(asIndividual(), targetPredator); }
    default boolean performSmoothWaspSting(Individual targetEnemy) { return getLotsTrait().performSmoothWaspSting(asIndividual(), targetEnemy); }
    default boolean performSmoothWaspSting(Predator targetPredator) { return getLotsTrait().performSmoothWaspSting(asIndividual(), targetPredator); }

    // LOT B: Living Civil Engineering & Subterranean Architecture
    default boolean isFormingLivingBridge() { return getLotsTrait().formingLivingBridge; }
    default void setFormingLivingBridge(boolean bridge) { getLotsTrait().formingLivingBridge = bridge; }
    default boolean isFormingLivingRaft() { return getLotsTrait().formingLivingRaft; }
    default void setFormingLivingRaft(boolean raft) { getLotsTrait().formingLivingRaft = raft; }
    default boolean isInhabitingDomatia() { return getLotsTrait().inhabitingDomatia; }
    default void setInhabitingDomatia(boolean domatia) { getLotsTrait().inhabitingDomatia = domatia; }
    default float getStercoralMortarReservoir() { return getLotsTrait().stercoralMortarReservoir; }
    default void refillStercoralMortar(float amt) { getLotsTrait().stercoralMortarReservoir = Math.min(100.0f, getLotsTrait().stercoralMortarReservoir + amt); }
    default boolean sewLeavesWithLarvalSilk(Individual silkLarva) { return getLotsTrait().sewLeavesWithLarvalSilk(asIndividual(), silkLarva); }
    default boolean applyStercoralCement() { return getLotsTrait().applyStercoralCement(); }

    // LOT C: Agronomy, Storage & Plant/Insect Symbiosis
    default boolean isRepleteStorageCaste() { return getLotsTrait().repleteStorageCaste; }
    default void setRepleteStorageCaste(boolean replete) { getLotsTrait().repleteStorageCaste = replete; }
    default float getRepleteStorageVolume() { return getLotsTrait().repleteStorageVolume; }
    default void setRepleteStorageVolume(float vol) { getLotsTrait().repleteStorageVolume = Math.max(0.0f, Math.min(100.0f, vol)); }
    default int getDeGerminatedSeedsCount() { return getLotsTrait().deGerminatedSeedsCount; }
    default int getManagedAphidsCount() { return getLotsTrait().managedAphidsCount; }
    default boolean deGermStoredSeed() { return getLotsTrait().deGermStoredSeed(asIndividual()); }
    default float milkAphid(float honeydewYield) { return getLotsTrait().milkAphid(asIndividual(), honeydewYield); }

    // LOT D: Bio-Acoustic Communication, Dance & Royal Signaling
    default boolean isWaggleDancing() { return getLotsTrait().waggleDancing; }
    default float getWaggleTargetHeading() { return getLotsTrait().waggleTargetHeading; }
    default float getWaggleDistanceMeters() { return getLotsTrait().waggleDistanceMeters; }
    default boolean isTrembleDancing() { return getLotsTrait().trembleDancing; }
    default void setTrembleDancing(boolean tremble) { getLotsTrait().trembleDancing = tremble; }
    default void triggerTrembleDance() { getLotsTrait().trembleDancing = true; }
    default void stopTrembleDance() { getLotsTrait().trembleDancing = false; }
    default boolean isQueenPipingActive() { return getLotsTrait().queenPipingActive; }
    default float getQueenPipingFrequencyHz() { return getLotsTrait().queenPipingFrequencyHz; }
    default void performWaggleDance(float targetHeading, float distanceMeters) { getLotsTrait().performWaggleDance(targetHeading, distanceMeters); }
    default void stopWaggleDance() { getLotsTrait().waggleDancing = false; }
    default void triggerQueenPiping() { getLotsTrait().triggerQueenPiping(asIndividual()); }
    default void stopQueenPiping() { getLotsTrait().queenPipingActive = false; }

    // LOT E: Genetics, Reproduction & Social Regulation
    default float getRoyalPheromoneInhibitionTiter() { return getLotsTrait().royalPheromoneInhibitionTiter; }
    default void setRoyalPheromoneInhibitionTiter(float titer) { getLotsTrait().royalPheromoneInhibitionTiter = Math.max(0.0f, Math.min(1.0f, titer)); }
    default boolean isGamergate() { return getLotsTrait().gamergate; }
    default void setGamergate(boolean g) { getLotsTrait().gamergate = g; }
    default int getDominanceScore() { return getLotsTrait().dominanceScore; }
    default int getTrophicEggsLaid() { return getLotsTrait().trophicEggsLaid; }
    default boolean isOvariesActivated() { return getLotsTrait().isOvariesActivated(asIndividual()); }
    default boolean layTrophicEgg() { return getLotsTrait().layTrophicEgg(asIndividual()); }
    default boolean feedOnLarvalHemolymph(Individual larva) { return getLotsTrait().feedOnLarvalHemolymph(asIndividual(), larva); }
    default boolean engageDominanceTournament(Individual rival) { return getLotsTrait().engageDominanceTournament(asIndividual(), rival); }

    // LOT F: Extreme Eco-Physiology & Climatic Adaptation
    default float getGlycerolConcentrationMgMl() { return getLotsTrait().glycerolConcentrationMgMl; }
    default void setGlycerolConcentrationMgMl(float conc) { getLotsTrait().glycerolConcentrationMgMl = Math.max(0.0f, conc); }
    default boolean isStiltWalking() { return getLotsTrait().stiltWalking; }
    default void setStiltWalking(boolean sw) { getLotsTrait().stiltWalking = sw; }
    default boolean isFloodEvacuating() { return getLotsTrait().floodEvacuating; }
    default void setFloodEvacuating(boolean fe) { getLotsTrait().floodEvacuating = fe; }
    default void synthesizeGlycerol(float amount) { getLotsTrait().glycerolConcentrationMgMl = Math.min(50.0f, getLotsTrait().glycerolConcentrationMgMl + amount); }
    default boolean canSurviveSubzero(float tempC) { return getLotsTrait().canSurviveSubzero(tempC); }
    default void detectHydrostaticFloodPressure(float barometricDropHpa, float soilMoisturePercent) { getLotsTrait().detectHydrostaticFloodPressure(barometricDropHpa, soilMoisturePercent); }

    // LOT G: Advanced Sanitation, Social Immunity & Pharmacy
    default boolean isVoluntarySelfIsolating() { return getLotsTrait().voluntarySelfIsolating; }
    default void setVoluntarySelfIsolating(boolean iso) { getLotsTrait().voluntarySelfIsolating = iso; }
    default float getPropolisVarnishCarried() { return getLotsTrait().propolisVarnishCarried; }
    default void setPropolisVarnishCarried(float amt) { getLotsTrait().propolisVarnishCarried = Math.max(0.0f, amt); }
    default void checkPathogenSelfIsolation(float fungalSporeTiter) { getLotsTrait().checkPathogenSelfIsolation(fungalSporeTiter); }
    default boolean applyPropolisVarnish() { return getLotsTrait().applyPropolisVarnish(); }
    default boolean clipAphidWingBuds() { getLotsTrait().managedAphidsCount++; return true; }

    // LOT H: Vegetal Masonry & Microclimate Control
    default boolean isHoldingSilkLarva() { return getLotsTrait().holdingSilkLarva; }
    default void setHoldingSilkLarva(boolean h) { getLotsTrait().holdingSilkLarva = h; }
    default float getSilkReservoir() { return getLotsTrait().silkReservoir; }
    default void refillSilkReservoir(float amt) { getLotsTrait().silkReservoir = Math.min(100.0f, getLotsTrait().silkReservoir + amt); }
    default boolean weaveLeafSilkSeam(float silkCost) { return getLotsTrait().weaveLeafSilkSeam(silkCost); }
    default boolean isRainSiphonConstructed() { return getLotsTrait().rainSiphonConstructed; }
    default void constructRainSiphon() { getLotsTrait().rainSiphonConstructed = true; }
    default float getDrainedFloodWaterLiters() { return getLotsTrait().drainedFloodWaterLiters; }
    default void drainFloodWater(float liters) { getLotsTrait().drainedFloodWaterLiters += Math.max(0.0f, liters); }
    default boolean isDualConduitsActive() { return getLotsTrait().dualConduitsActive; }
    default void setDualConduitsActive(boolean active) { getLotsTrait().dualConduitsActive = active; }
    default float getSubterraneanMicroclimateTemp(float surfaceTempC) { return getLotsTrait().getSubterraneanMicroclimateTemp(surfaceTempC); }
    default int getAntisepticGravelCarried() { return getLotsTrait().antisepticGravelCarried; }
    default void pickUpAntisepticGravel(int count) { getLotsTrait().antisepticGravelCarried += Math.max(0, count); }
    default int getSealedContaminatedGalleries() { return getLotsTrait().sealedContaminatedGalleries; }
    default boolean sealContaminatedGallery() { return getLotsTrait().sealContaminatedGallery(); }

    // LOT I: Bio-Acoustics & Substrate Telegraphy
    default boolean isDrummingSubstrateAlarm() { return getLotsTrait().drummingSubstrateAlarm; }
    default void triggerDrummingSubstrateAlarm() { getLotsTrait().drummingSubstrateAlarm = true; }
    default void stopDrummingSubstrateAlarm() { getLotsTrait().drummingSubstrateAlarm = false; }
    default float getDrumFrequencyHz() { return getLotsTrait().drumFrequencyHz; }
    default float getSubstrateVibrationPropagationDistanceMeters() { return 8.5f; }
    default boolean isGuardShiftWhispering() { return getLotsTrait().guardShiftWhispering; }
    default void setGuardShiftWhispering(boolean w) { getLotsTrait().guardShiftWhispering = w; }
    default boolean performGuardShiftWhisper(Individual replacementGuard) { return getLotsTrait().performGuardShiftWhisper(asIndividual(), replacementGuard); }
    default boolean isQueenPiping() { return getLotsTrait().queenPipingActive; }
    default float getPipingFrequencyHz() { return getLotsTrait().queenPipingFrequencyHz; }
    default boolean isEggLayingSyncStridulation() { return getLotsTrait().eggLayingSyncStridulation; }
    default void triggerEggLayingSync() { getLotsTrait().eggLayingSyncStridulation = true; }
    default float calculateSynchronizedFertilityMultiplier() { return getLotsTrait().eggLayingSyncStridulation ? 1.45f : 1.0f; }

    // LOT J: Chemical Ecology & Allelochemistry
    default float getTerritorialRepellentCarried() { return getLotsTrait().territorialRepellentCarried; }
    default boolean depositTerritorialRepellent(float amount) { return getLotsTrait().depositTerritorialRepellent(amount); }
    default boolean isFanoutEscapeActive() { return getLotsTrait().fanoutEscapeActive; }
    default void setFanoutEscapeActive(boolean active) { getLotsTrait().fanoutEscapeActive = active; }
    default void triggerFormicAcidFanoutEscape(float stimulusHeading, float dispersionAngle) { getLotsTrait().triggerFormicAcidFanoutEscape(asIndividual(), stimulusHeading, dispersionAngle); }
    default float calculatePheromoneHalfLifeSeconds(float soilTempC, float baseHalfLifeSeconds) { return getLotsTrait().calculatePheromoneHalfLifeSeconds(soilTempC, baseHalfLifeSeconds); }
    default float getPlantCuticularCamouflagePercent() { return getLotsTrait().plantCuticularCamouflagePercent; }
    default void rubAgainstHostBark(float durationSeconds) { getLotsTrait().plantCuticularCamouflagePercent = Math.min(100.0f, getLotsTrait().plantCuticularCamouflagePercent + durationSeconds * 2.5f); }

    // LOT K: Pharmacy & Specialized Refuse Management
    default float getSulfurDustCarried() { return getLotsTrait().sulfurDustCarried; }
    default void collectSulfurDust(float amt) { getLotsTrait().sulfurDustCarried = Math.min(50.0f, getLotsTrait().sulfurDustCarried + amt); }
    default boolean dustParasiticMites() { return getLotsTrait().dustParasiticMites(); }
    default float getWoodDustCarried() { return getLotsTrait().woodDustCarried; }
    default void collectWoodDust(float amt) { getLotsTrait().woodDustCarried = Math.min(50.0f, getLotsTrait().woodDustCarried + amt); }
    default boolean applyLarvalWoodDustDrying(Individual larva) { return getLotsTrait().applyLarvalWoodDustDrying(larva); }
    default boolean isParticipatingInParasiteQuarantine() { return getLotsTrait().participatingInParasiteQuarantine; }
    default void joinParasiteQuarantineEncirclement() { getLotsTrait().participatingInParasiteQuarantine = true; }
    default void leaveParasiteQuarantineEncirclement() { getLotsTrait().participatingInParasiteQuarantine = false; }
    default boolean isRefuseSortingDuty() { return getLotsTrait().refuseSortingDuty; }
    default void setRefuseSortingDuty(boolean duty) { getLotsTrait().refuseSortingDuty = duty; }
    default int getSortedRefuseItems() { return getLotsTrait().sortedRefuseItems; }
    default boolean depositSortedRefuseOutside() { return getLotsTrait().depositSortedRefuseOutside(asIndividual()); }

    // LOT L: Crisis Metabolism & Social Regulation
    default boolean isRepletesHoneypot() { return getLotsTrait().repletesHoneypot; }
    default void setRepletesHoneypot(boolean replete) { getLotsTrait().repletesHoneypot = replete; }
    default float getHoneypotStorageGrams() { return getLotsTrait().honeypotStorageGrams; }
    default float storeHoneypotNectar(float amount) { return getLotsTrait().storeHoneypotNectar(amount); }
    default float dispenseHoneypotNectar(float amount) { return getLotsTrait().dispenseHoneypotNectar(amount); }
    default float getFermentedSapCombatBuffSeconds() { return getLotsTrait().fermentedSapCombatBuffSeconds; }
    default void ingestFermentedSap(float amt) { getLotsTrait().fermentedSapCombatBuffSeconds = Math.min(300.0f, getLotsTrait().fermentedSapCombatBuffSeconds + amt * 60.0f); }
    default boolean hasCombatAnestheticBuff() { return getLotsTrait().fermentedSapCombatBuffSeconds > 0.0f; }
    default float getGutCellulolyticProtozoaTiter() { return getLotsTrait().gutCellulolyticProtozoaTiter; }
    default void setGutCellulolyticProtozoaTiter(float titer) { getLotsTrait().gutCellulolyticProtozoaTiter = Math.max(0.0f, Math.min(1.0f, titer)); }
    default boolean transferProctodealMicrobiome(Individual nymph) { return getLotsTrait().transferProctodealMicrobiome(nymph); }
}
