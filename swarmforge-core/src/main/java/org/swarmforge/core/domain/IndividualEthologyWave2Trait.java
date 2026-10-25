/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import java.io.Serializable;

/**
 * Serializable domain trait encapsulating Mega-Batch Wave 2 behavioral systems
 * (#116 -> #168) for {@link Individual}.
 */
public class IndividualEthologyWave2Trait implements Serializable {
    private static final long serialVersionUID = 1L;

    boolean hatchingAnnouncementVibratoActive = false;
    int resinNymphalMummificationCount = 0;
    int sandPitfallTrapsCount = 0;
    boolean injuredPheromonalStretcherTransportActive = false;
    int abandonedWaxVaultsRaidedCount = 0;
    boolean ritualMandibularWrestlingActive = false;
    float pulsedAirConvectiveVentilationRateLpm = 0.0f;
    boolean streptomycesAntibioticCryptsActive = true;
    boolean pedestrianSwarmBuddingActive = false;
    float paperPulpCartonMasticationMg = 0.0f;
    float larvalAminoSalivaHarvestedMl = 0.0f;
    boolean pedicelAntRepellentCoatingApplied = false;
    boolean facialPatternVisualRecognitionTrained = false;
    float buzzPollinationSonicationHz = 0.0f;
    boolean bumblebeeAbdominalIncubationActive = false;
    float eusocialShrimpCavitationSnapShockwaveDb = 0.0f;
    boolean passalidFrassParentalStridulationActive = false;
    boolean physogastricQueenPeristalsisActive = false;
    float magneticMoundOrientationNSRad = 0.0f;
    boolean hornetGroupAlarmVenomSprayActive = false;
    float stenogastrinePaperJellyMg = 0.0f;
    int termiteFungalCombInoculationCount = 0;
    float waspCellRimDrummingDb = 0.0f;
    int stinglessBeeSpiralingBroodCellsCount = 0;
    float livingBridgeTensionForceNewtons = 0.0f;
    boolean subterraneanWaterSiphonPrimed = false;
    int soilHydraulicDrainageChannelCount = 0;
    boolean toxicResinBurrowRepellentApplied = false;
    float siltCamouflageReflectanceReduction = 0.0f;
    int mandibleLinkedChainEscortSize = 0;
    float trophallacticOvaryInhibitoryPeptideTiter = 1.0f;
    boolean clayPropolisCarcassHermeticSeal = false;
    float hydrophobicEpicuticularLipidWallCoverage = 0.0f;
    float fermentedSapEnduranceMultiplier = 1.0f;
    float preFlightLipidCropFullness = 0.0f;
    boolean honeyStoreBrickDefensivePlugging = false;
    boolean thermalInfraredSensillaActive = false;
    float arborealCanopySilkBridgeSpanM = 0.0f;
    float synchronizedGyneEggStridulationHz = 0.0f;
    float tarsalNotchBrushCleansingPercent = 100.0f;
    float saltCrystalOsmoticRetentionBar = 1.0f;
    float gravityDrainageConduitLitres = 0.0f;
    float hostTreeHydrocarbonMimicryScore = 0.0f;
    float mineralSulfurAcaricideDustMg = 0.0f;
    float hatchingVibratoScoutAnnouncementDb = 0.0f;
    boolean antisepticResinEnvelopeBuilt = false;
    float funnelSandyPitfallSlopeDeg = 0.0f;
    float cryoprotectantPolyolSynthesisRate = 0.0f;
    boolean battlefieldStretcherSquadActive = false;
    float feralWaxVaultScavengingYieldMg = 0.0f;

    public boolean mummifyNymphalCocoonResin(float resinMg) {
        if (resinMg < 4.0f) return false;
        this.resinNymphalMummificationCount++;
        return true;
    }

    public boolean transportInjuredOnStretcher(Individual self, Individual injured) {
        if (injured == null || self.getCarriedItem() != Individual.CarriedItem.NONE) return false;
        this.injuredPheromonalStretcherTransportActive = true;
        self.setCarriedItem(Individual.CarriedItem.FOOD);
        return true;
    }

    public boolean engageRitualMandibularWrestling(Individual rival) {
        if (rival == null || !rival.isAlive()) return false;
        this.ritualMandibularWrestlingActive = true;
        rival.wave2Trait.ritualMandibularWrestlingActive = true;
        return true;
    }

    public boolean applyStreptomycesAntibiotics(Individual gardenWorker) {
        if (gardenWorker == null) return false;
        gardenWorker.setHealth(Math.min(gardenWorker.getMaxHealth(), gardenWorker.getHealth() + 8.0f));
        return true;
    }

    public void triggerPedestrianSwarmBudding(Individual self) {
        this.pedestrianSwarmBuddingActive = true;
        self.setState(Individual.AiState.WANDER);
    }

    public boolean harvestLarvalAminoSaliva(Individual self, Individual larva) {
        if (larva == null || larva.getLifeStage() != Individual.LifeStage.LARVA) return false;
        this.larvalAminoSalivaHarvestedMl += 0.08f;
        self.setEnergy(Math.min(self.getMaxEnergy(), self.getEnergy() + 8.0f));
        return true;
    }

    public boolean recognizeFacialVisualPattern(Individual self, Individual nestmate) {
        if (nestmate == null || self.getColonyId() != nestmate.getColonyId()) return false;
        this.facialPatternVisualRecognitionTrained = true;
        return true;
    }

    public void performBumblebeeAbdominalIncubation(Individual brood) {
        this.bumblebeeAbdominalIncubationActive = true;
        if (brood != null) brood.setAge(brood.getAge() + 10.0f);
    }

    public boolean performAphidSoldierHornStabbing(Individual predator) {
        if (predator == null || !predator.isAlive()) return false;
        predator.takeDamage(15.0f, "Sterile Soldier Aphid Horn Pierce");
        return true;
    }

    public boolean crushGallIntruderThrips(Individual intruder) {
        if (intruder == null || !intruder.isAlive()) return false;
        intruder.takeDamage(22.0f, "Thrips Raptorial Foreleg Squeeze");
        return true;
    }

    public boolean snapClawAcousticShockwave(Individual invader) {
        this.eusocialShrimpCavitationSnapShockwaveDb = 210.0f;
        if (invader != null) invader.takeDamage(35.0f, "Cavitation Bubble Collapse Shockwave");
        return true;
    }

    public void stridulatePassalidParentalFrass(Individual grub) {
        this.passalidFrassParentalStridulationActive = true;
        if (grub != null) grub.setEnergy(Math.min(grub.getMaxEnergy(), grub.getEnergy() + 10.0f));
    }

    public void performPhysogastricQueenPeristalsis(Individual self) {
        if (self.getCaste() == Individual.Caste.QUEEN) {
            this.physogastricQueenPeristalsisActive = true;
        }
    }

    public void sprayHornetGroupAlarmVenom(Individual beeTarget) {
        this.hornetGroupAlarmVenomSprayActive = true;
        if (beeTarget != null) beeTarget.takeDamage(18.0f, "Hornet Spray Venom Marker");
    }

    public boolean deliverTrophallacticOvaryInhibition(Individual worker) {
        if (worker == null) return false;
        worker.setRoyalPheromoneInhibitionTiter(1.0f);
        return true;
    }

    public void gorgePreFlightVirginQueenLipids(Individual gyne) {
        if (gyne != null && gyne.getCaste() == Individual.Caste.QUEEN) {
            this.preFlightLipidCropFullness = 1.0f;
            gyne.setMaxEnergy(150.0f);
            gyne.setEnergy(150.0f);
        }
    }

    public boolean detectThermalInfraredPrey(Individual prey) {
        this.thermalInfraredSensillaActive = true;
        return prey != null && prey.isAlive();
    }
}
