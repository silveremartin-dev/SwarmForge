/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import org.swarmforge.core.simulation.FungusGarden;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Serializable domain trait encapsulating specialized biological subsystems
 * (Lots A through L: chemical/thermal defense, living civil engineering,
 * agronomy/storage, bio-acoustic dance & royal signaling, gamergate dominance,
 * extreme eco-physiology, social immunity/pharmacy, masonry/ventilation,
 * substrate telegraphy, allelochemistry, and crisis metabolism) for {@link Individual}.
 */
public class IndividualSpecializedLotsTrait implements Serializable {
    private static final long serialVersionUID = 1L;

    // Tripartite Symbiosis: Actinobacteria Bio-Weeding
    float actinobacteriaResin = 100.0f;

    // 1. Necrophoresis & Cemetery Refuse Deposition
    float decompositionAgeSeconds = 0.0f;

    // 2. Active Social Thermoregulation
    boolean shiveringThermogenesis = false;
    boolean wingFanning = false;
    float thoraxTemperatureC = 25.0f;

    // 3. Dulosis & Slave-Making Raids
    boolean enslaved = false;
    UUID hostColonyId = null;

    // 4. Bio-Acoustic Stridulation Rescue Call
    boolean stridulatingRescueCall = false;
    float stridulationFrequencyHz = 850.0f;
    float stridulationIntensityDb = 80.0f;

    // LOT A: Advanced Biological & Mechanical Defense
    boolean autothysed = false;
    boolean phragmoticShieldActive = false;
    boolean inThermalBallCluster = false;
    float nasuteResinReservoir = 100.0f;

    // LOT B: Living Civil Engineering & Subterranean Architecture
    boolean formingLivingBridge = false;
    boolean formingLivingRaft = false;
    boolean inhabitingDomatia = false;
    float stercoralMortarReservoir = 100.0f;

    // LOT C: Agronomy, Storage & Plant/Insect Symbiosis
    boolean repleteStorageCaste = false;
    float repleteStorageVolume = 0.0f;
    int deGerminatedSeedsCount = 0;
    int managedAphidsCount = 0;

    // LOT D: Bio-Acoustic Communication, Dance & Royal Signaling
    boolean waggleDancing = false;
    float waggleTargetHeading = 0.0f;
    float waggleDistanceMeters = 0.0f;
    boolean trembleDancing = false;
    boolean queenPipingActive = false;
    float queenPipingFrequencyHz = 450.0f;

    // LOT E: Genetics, Reproduction & Social Regulation
    float royalPheromoneInhibitionTiter = 1.0f;
    boolean gamergate = false;
    int dominanceScore = 0;
    int trophicEggsLaid = 0;

    // LOT F: Extreme Eco-Physiology & Climatic Adaptation
    float glycerolConcentrationMgMl = 0.0f;
    boolean stiltWalking = false;
    boolean floodEvacuating = false;

    // LOT G: Advanced Sanitation, Social Immunity & Pharmacy
    boolean voluntarySelfIsolating = false;
    float propolisVarnishCarried = 0.0f;

    // LOT H: Vegetal Masonry & Microclimate Control
    boolean holdingSilkLarva = false;
    float silkReservoir = 100.0f;
    boolean rainSiphonConstructed = false;
    float drainedFloodWaterLiters = 0.0f;
    boolean dualConduitsActive = false;
    int antisepticGravelCarried = 0;
    int sealedContaminatedGalleries = 0;

    // LOT I: Bio-Acoustics & Substrate Telegraphy
    boolean drummingSubstrateAlarm = false;
    float drumFrequencyHz = 1000.0f;
    boolean guardShiftWhispering = false;
    boolean eggLayingSyncStridulation = false;

    // LOT J: Chemical Ecology & Allelochemistry
    float territorialRepellentCarried = 100.0f;
    boolean fanoutEscapeActive = false;
    float plantCuticularCamouflagePercent = 0.0f;

    // LOT K: Pharmacy & Specialized Refuse Management
    float sulfurDustCarried = 0.0f;
    float woodDustCarried = 0.0f;
    boolean participatingInParasiteQuarantine = false;
    boolean refuseSortingDuty = false;
    int sortedRefuseItems = 0;

    // LOT L: Crisis Metabolism & Social Regulation
    boolean repletesHoneypot = false;
    float honeypotStorageGrams = 0.0f;
    float fermentedSapCombatBuffSeconds = 0.0f;
    float gutCellulolyticProtozoaTiter = 1.0f;

    public boolean applyActinobacteriaBioWeeding(FungusGarden garden) {
        if (garden == null || actinobacteriaResin < 15.0f) return false;
        actinobacteriaResin -= 15.0f;
        garden.setContaminationLevel(Math.max(0.0f, garden.getContaminationLevel() - 0.3f));
        garden.treatWithActinobacteria(10.0f);
        return true;
    }

    public boolean pickUpCorpse(Individual self, Individual corpse) {
        if (corpse == null || corpse.isAlive() || self.getCarriedItem() != Individual.CarriedItem.NONE) {
            return false;
        }
        self.setCarriedItem(Individual.CarriedItem.DEAD_ANT);
        return true;
    }

    public boolean depositCorpseAtRefuse(Individual self) {
        if (self.getCarriedItem() != Individual.CarriedItem.DEAD_ANT) {
            return false;
        }
        self.setCarriedItem(Individual.CarriedItem.NONE);
        return true;
    }

    public boolean stealBrood(Individual self, Individual broodTarget) {
        if (broodTarget == null || self.getCarriedItem() != Individual.CarriedItem.NONE) {
            return false;
        }
        self.setCarriedItem(Individual.CarriedItem.BROOD);
        return true;
    }

    public void integrateAsEnslavedWorker(Individual self, UUID masterColonyId) {
        this.enslaved = true;
        this.hostColonyId = self.getColonyId();
        self.setColonyIdInternal(masterColonyId);
        self.setLifeStage(Individual.LifeStage.ADULT);
        self.setJob(Individual.Job.NURSE);
    }

    public boolean performAutothysis(Individual self, List<Individual> nearbyEnemies) {
        if (!self.isAlive() || autothysed) return false;
        autothysed = true;
        self.die("Autothysis Defensive Rupture");
        if (nearbyEnemies != null) {
            for (Individual enemy : nearbyEnemies) {
                if (enemy != null && enemy.isAlive() && enemy.getColonyId() != self.getColonyId()) {
                    enemy.takeDamage(35.0f, "Toxic Polyketone Adhesive Glue");
                    enemy.setChemotacticBlindnessTicks(180);
                }
            }
        }
        return true;
    }

    public boolean performThermalBalling(Individual self, Individual targetPredator, float clusterPower) {
        if (!self.isAlive() || targetPredator == null || !targetPredator.isAlive()) return false;
        this.inThermalBallCluster = true;
        self.consumeEnergy(0.5f);
        this.thoraxTemperatureC = Math.min(47.0f, this.thoraxTemperatureC + 4.0f);
        targetPredator.takeDamage(10.0f * clusterPower, "Thermal Ball Asphyxiation");
        return true;
    }

    public boolean squirtNasuteResin(Individual self, Individual targetEnemy, float distance) {
        if (!self.isAlive() || targetEnemy == null || !targetEnemy.isAlive() || distance > 0.8f || nasuteResinReservoir < 15.0f) {
            return false;
        }
        nasuteResinReservoir -= 15.0f;
        targetEnemy.takeDamage(12.0f, "Nasute Monoterpene Resin");
        targetEnemy.setChemotacticBlindnessTicks(100);
        return true;
    }

    public boolean performBarbedBeeSting(Individual self, Individual targetEnemy) {
        if (!self.isAlive() || targetEnemy == null || !targetEnemy.isAlive()) {
            return false;
        }
        targetEnemy.takeDamage(40.0f, "Apitoxin Envenomation (Barbed Sting)");
        targetEnemy.setChemotacticBlindnessTicks(150);
        self.consumeEnergy(8.0f);
        if (Math.random() < 0.12) {
            self.setHealth(0.0f);
            self.die("Autotomie post-piqûre (Dard coincé dans la cuticule)");
        }
        return true;
    }

    public boolean performMammalianDefensiveSting(Individual self, float envenomationDamage) {
        if (!self.isAlive()) return false;
        self.setHealth(0.0f);
        self.die("Autotomie défensive post-piqûre mammifère (Dard arraché)");
        return true;
    }

    public boolean performBarbedBeeSting(Individual self, Predator targetPredator) {
        if (!self.isAlive() || targetPredator == null || !targetPredator.isAlive()) {
            return false;
        }
        targetPredator.takeDamage(40.0f);
        self.consumeEnergy(8.0f);
        if (Math.random() < 0.12) {
            self.setHealth(0.0f);
            self.die("Autotomie post-piqûre contre prédateur");
        }
        return true;
    }

    public boolean performSmoothWaspSting(Individual self, Individual targetEnemy) {
        if (!self.isAlive() || targetEnemy == null || !targetEnemy.isAlive()) {
            return false;
        }
        self.consumeEnergy(5.0f);
        targetEnemy.takeDamage(20.0f, "Vespula Envenomation (Smooth Sting)");
        return true;
    }

    public boolean performSmoothWaspSting(Individual self, Predator targetPredator) {
        if (!self.isAlive() || targetPredator == null || !targetPredator.isAlive()) {
            return false;
        }
        self.consumeEnergy(5.0f);
        targetPredator.takeDamage(20.0f);
        return true;
    }

    public boolean sewLeavesWithLarvalSilk(Individual self, Individual silkLarva) {
        if (silkLarva == null || silkLarva.getLifeStage() != Individual.LifeStage.LARVA || self.getCarriedItem() != Individual.CarriedItem.NONE) {
            return false;
        }
        self.setCarriedItem(Individual.CarriedItem.BROOD);
        return true;
    }

    public boolean applyStercoralCement() {
        if (stercoralMortarReservoir < 10.0f) return false;
        stercoralMortarReservoir -= 10.0f;
        return true;
    }

    public boolean deGermStoredSeed(Individual self) {
        if (self.getCarriedItem() != Individual.CarriedItem.FOOD) return false;
        deGerminatedSeedsCount++;
        return true;
    }

    public float milkAphid(Individual self, float honeydewYield) {
        managedAphidsCount++;
        float harvested = Math.max(0.0f, honeydewYield);
        self.setEnergy(Math.min(self.getMaxEnergy(), self.getEnergy() + harvested * 5.0f));
        if (repleteStorageCaste) {
            repleteStorageVolume = Math.min(100.0f, repleteStorageVolume + harvested * 10.0f);
        }
        return harvested;
    }

    public void performWaggleDance(float targetHeading, float distanceMeters) {
        this.waggleDancing = true;
        this.waggleTargetHeading = targetHeading;
        this.waggleDistanceMeters = Math.max(0.0f, distanceMeters);
    }

    public void triggerQueenPiping(Individual self) {
        if (self.getCaste() == Individual.Caste.QUEEN) {
            this.queenPipingActive = true;
            this.queenPipingFrequencyHz = 450.0f;
        }
    }

    public boolean isOvariesActivated(Individual self) {
        return (self.getCaste() == Individual.Caste.WORKER || self.getCaste() == Individual.Caste.NURSE)
                && royalPheromoneInhibitionTiter < 0.15f;
    }

    public boolean layTrophicEgg(Individual self) {
        if (!isOvariesActivated(self) && !gamergate) return false;
        trophicEggsLaid++;
        self.consumeEnergy(10.0f);
        return true;
    }

    public boolean feedOnLarvalHemolymph(Individual self, Individual larva) {
        if (larva == null || larva.getLifeStage() != Individual.LifeStage.LARVA || !larva.isAlive()) return false;
        larva.takeDamage(4.0f, "Non-lethal Larval Hemolymph Tap");
        self.setEnergy(Math.min(self.getMaxEnergy(), self.getEnergy() + 15.0f));
        return true;
    }

    public boolean engageDominanceTournament(Individual self, Individual rival) {
        if (rival == null || !rival.isAlive() || rival.getColonyId() != self.getColonyId()) return false;
        if (self.getHealth() >= rival.getHealth()) {
            this.dominanceScore += 2;
            rival.lotsTrait.dominanceScore = Math.max(0, rival.lotsTrait.dominanceScore - 1);
            if (this.dominanceScore >= 10 && !this.gamergate) {
                this.gamergate = true;
                self.setCaste(Individual.Caste.QUEEN);
            }
            return true;
        } else {
            rival.lotsTrait.dominanceScore += 2;
            this.dominanceScore = Math.max(0, this.dominanceScore - 1);
            return false;
        }
    }

    public boolean canSurviveSubzero(float tempC) {
        if (tempC >= 0.0f) return true;
        float criticalMinTemp = -(this.glycerolConcentrationMgMl * 0.375f);
        return tempC >= criticalMinTemp;
    }

    public void detectHydrostaticFloodPressure(float barometricDropHpa, float soilMoisturePercent) {
        if (barometricDropHpa > 15.0f || soilMoisturePercent > 85.0f) {
            this.floodEvacuating = true;
        }
    }

    public void checkPathogenSelfIsolation(float fungalSporeTiter) {
        if (fungalSporeTiter >= 75.0f) {
            this.voluntarySelfIsolating = true;
        }
    }

    public boolean applyPropolisVarnish() {
        if (this.propolisVarnishCarried < 5.0f) return false;
        this.propolisVarnishCarried -= 5.0f;
        return true;
    }

    public boolean weaveLeafSilkSeam(float silkCost) {
        if (!holdingSilkLarva || silkReservoir < silkCost) return false;
        silkReservoir -= silkCost;
        return true;
    }

    public float getSubterraneanMicroclimateTemp(float surfaceTempC) {
        if (!dualConduitsActive) return surfaceTempC;
        return 24.0f + (surfaceTempC - 24.0f) * 0.25f;
    }

    public boolean sealContaminatedGallery() {
        if (antisepticGravelCarried < 3) return false;
        antisepticGravelCarried -= 3;
        sealedContaminatedGalleries++;
        return true;
    }

    public boolean performGuardShiftWhisper(Individual self, Individual replacementGuard) {
        if (replacementGuard == null || !replacementGuard.isAlive()) return false;
        this.guardShiftWhispering = true;
        self.setJob(Individual.Job.IDLE);
        replacementGuard.setJob(Individual.Job.GUARD);
        return true;
    }

    public boolean depositTerritorialRepellent(float amount) {
        if (territorialRepellentCarried < amount) return false;
        territorialRepellentCarried -= amount;
        return true;
    }

    public void triggerFormicAcidFanoutEscape(Individual self, float stimulusHeading, float dispersionAngle) {
        this.fanoutEscapeActive = true;
        float escapeHeading = (float) ((stimulusHeading + Math.PI + dispersionAngle) % (2.0 * Math.PI));
        if (escapeHeading < 0) escapeHeading += (float) (2.0 * Math.PI);
        self.setHeading(escapeHeading);
        self.setState(Individual.AiState.FLEEING);
    }

    public float calculatePheromoneHalfLifeSeconds(float soilTempC, float baseHalfLifeSeconds) {
        double factor = (soilTempC - 20.0) / 10.0;
        return (float) (baseHalfLifeSeconds * Math.pow(2.0, -factor));
    }

    public boolean dustParasiticMites() {
        if (sulfurDustCarried < 2.0f) return false;
        sulfurDustCarried -= 2.0f;
        return true;
    }

    public boolean applyLarvalWoodDustDrying(Individual larva) {
        if (woodDustCarried < 1.0f || larva == null) return false;
        woodDustCarried -= 1.0f;
        return true;
    }

    public boolean depositSortedRefuseOutside(Individual self) {
        if (!refuseSortingDuty || self.getCarriedItem() == Individual.CarriedItem.NONE) return false;
        self.setCarriedItem(Individual.CarriedItem.NONE);
        this.sortedRefuseItems++;
        return true;
    }

    public float storeHoneypotNectar(float amount) {
        if (!repletesHoneypot) return 0.0f;
        float space = 0.35f - honeypotStorageGrams;
        float stored = Math.min(space, amount);
        this.honeypotStorageGrams += stored;
        return stored;
    }

    public float dispenseHoneypotNectar(float amount) {
        if (honeypotStorageGrams <= 0.0f) return 0.0f;
        float dispensed = Math.min(honeypotStorageGrams, amount);
        this.honeypotStorageGrams -= dispensed;
        return dispensed;
    }

    public boolean transferProctodealMicrobiome(Individual nymph) {
        if (nymph == null || !nymph.isAlive() || this.gutCellulolyticProtozoaTiter < 0.3f) return false;
        nymph.setGutCellulolyticProtozoaTiter(Math.min(1.0f, nymph.getGutCellulolyticProtozoaTiter() + 0.6f));
        return true;
    }
}
