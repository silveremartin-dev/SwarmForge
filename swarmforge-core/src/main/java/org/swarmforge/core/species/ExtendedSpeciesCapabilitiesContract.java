/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.species;

/**
 * Extended ethological capabilities contract (Batches XIV to XVII, capabilities 141-220)
 * inherited by {@link Species}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public interface ExtendedSpeciesCapabilitiesContract {

    // --- Batch XI & XII Eusocial/Subsocial Arthropod Capabilities (101-120) ---
    default boolean canTrophallaxisProtozoa() { return false; } // Anal trophallactic transfer of flagellate protozoa essential for cellulose digestion in termites
    default boolean canSquirtNasuteChemical() { return false; } // Specialized nasus nozzle squirt of sticky terpenoid defense glue
    default boolean canMasticatePaperPulpCarton() { return false; } // Scraping and saliva mastication of weathered wood fibers for carton paper nest cell building
    default boolean canHarvestLarvalSalivaDroplets() { return false; } // Solicit nutrient-rich amino acid saliva droplets from larvae in exchange for meat
    default boolean canApplyPedicelAntRepellent() { return false; } // Glandular organ secretion coating on nest pedicel stalk to block ant invasion
    default boolean canRecognizeFacialVisualPatterns() { return false; } // Visual recognition of unique facial markings maintaining wasp dominance hierarchy
    default boolean canPerformBuzzPollination() { return false; } // High-frequency thorax muscular vibration (sonication) to dislodge pollen
    default boolean canIncubateBroodAbdominalHeat() { return false; } // Pressing hairless ventral abdomen onto brood cells to transfer metabolic heat
    default boolean canStabFrontalHornsAphid() { return false; } // Sterile soldier aphid horn stabbing and hemolymph gluing of ladybug predators
    default boolean canSqueezeGallIntrudersThrips() { return false; } // Eusocial thrips soldier raptorial foreleg crushing of gall invaders
    default boolean canSnapClawAcousticShockwave() { return false; } // Eusocial shrimp giant chela cavitation snap producing acoustic shockwaves in sponge canals
    default boolean canStridulatePassalidParentalCare() { return false; } // Passalid beetle adult/larval stridulatory communication during wood frass feeding
    default boolean canPerformPhysogastricPeristalsis() { return false; } // Termite queen massive abdominal peristalsis supporting high daily egg laying
    default boolean canOrientMagneticMound() { return false; } // Building wedge-shaped mounds aligned along geomagnetic lines for thermoregulation
    default boolean canEmitHornetGroupAlarmPheromone() { return false; } // Venom spray alarm recruitment triggering mass coordinated hornet raids
    default boolean canWeaveStenogastrinePaperJelly() { return false; } // Mixing salivary gelatinous secretions with fine plant fibers for hair-thin suspended nests
    default boolean canInoculateFungalCombTermite() { return false; } // Termite cultivation of Termitomyces fungal gardens on chewed fecal pellets
    default boolean canDrumAbdomenWaspCellRim() { return false; } // Rhythmic abdominal drumming against paper cell walls to alert nestmates
    default boolean canConstructNectarWaxPots() { return false; } // Molding urn-shaped wax pots near nest entrance for honey storage
    default boolean canPerformMaternalShieldGuarding() { return false; } // Subsocial parent bug maternal body shield guarding over egg clutch

    // --- Batch XIII Arthropod & Eusocial Capabilities (121-140) ---
    default boolean canWeaveCommunalSpiderSilk() { return false; } // Cooperative spinning of massive capture webs in social spiders
    default boolean canFormProcessionarySilkTrail() { return false; } // Single file procession guided by silk thread and abdominal trail pheromones
    default boolean canConstructClayVaultArches() { return false; } // Subterranean gothic arches built from clay-saliva pellets
    default boolean canDeliverStenogastrinePapFood() { return false; } // Secretion of proteinaceous pap droplets into egg cells before hatching
    default boolean canPlasterFrassGalleryWalls() { return false; } // Plastering gallery walls with frass to prevent resin inundation
    default boolean canLearnTrapliningFlightRoutes() { return false; } // Traplining spatial memory trajectories for bumblebee foraging
    default boolean canCoolNestWaterRegurgitation() { return false; } // Water droplet regurgitation on cell caps for evaporative cooling
    default boolean canEjectHoneydewSignalingDroplets() { return false; } // Abdominal flicks ejecting honeydew to recruit mutualist ants
    default boolean canSnapMandibleAcousticAlarm() { return false; } // Mandible snapping against wooden gallery walls creating acoustic alarms
    default boolean canPerformEggLickingGrooming() { return false; } // Maternal mouthpart licking and salivary coating of earwig eggs
    default boolean canConstructChaffGarbageDunes() { return false; } // Dumping seed chaff refuse in specialized exterior crescent dunes
    default boolean canDrumAntennaeLarvalStimulation() { return false; } // Antennal drumming on larval heads soliciting saliva release
    default boolean canFormLeafPullingChains() { return false; } // Interlocked worker body chains pulling tree leaves together for weaver nest assembly
    default boolean canApplySalivaryCementMoistureSeal() { return false; } // Water-resistant salivary-clay mortar sealing gallery breaches against desiccation
    default boolean canForageSubZeroBumblebee() { return false; } // Flight muscle shivering allowing Arctic bumblebee nectar foraging at 0°C
    default boolean canRepairGallSubstratalSecretion() { return false; } // Abdominal secretion of growth stimulants to repair thrips gall cracks
    default boolean canTrophallaxisPassalidWoodFrass() { return false; } // Oral regurgitation of pre-digested wood pulp to passalid beetle grubs
    default boolean canPerformCrecheRegurgitationSpider() { return false; } // Maternal liquefied gut regurgitation feeding of social spider crèches
    default boolean canBlockRoyalChamberSentry() { return false; } // Interlocked soldier head capsules blocking access to termite royal chamber
    default boolean canEmitParentBugAlarmGathering() { return false; } // Abdominal alarm pheromone burst signaling nymphs to cluster under maternal shield

    // --- Batch XIV Capabilities (141-160) ---
    default boolean canApplyBeeBreadHydrophobicCoating() { return false; }
    default boolean canBindParasitesWithSilk() { return false; }
    default boolean canEmitSubstrateObstacleVibrato() { return false; }
    default boolean canPerformFormicAcidBathGrooming() { return false; }
    default boolean canExcavateVerticalDrainageShafts() { return false; }
    default boolean canIngestPhenolicResinMedication() { return false; }
    default boolean canConstructSphagnumMoistureDomes() { return false; }
    default boolean canMarkParasitizedCadaverRepellent() { return false; }
    default boolean canDrumNuptialFlightSynchronization() { return false; }
    default boolean canHarvestCuticularWaterCondensation() { return false; }
    default boolean canStridulateLarvalHungerChirp() { return false; }
    default boolean canConstructThermalChimneyFlues() { return false; }
    default boolean canDepositLarvalFoodSalivaDrop() { return false; }
    default boolean canApplyEggMassMucilageEnvelope() { return false; }
    default boolean canWeaveSilkPavilionAphidShelter() { return false; }
    default boolean canFormHotBallThermalDefense() { return false; }
    default boolean canPerformFontanelleAutothysis() { return false; }
    default boolean canSensePreySignalWireTripping() { return false; }
    default boolean canMutilateSeedRadicles() { return false; }
    default boolean canBiteNectarTheftHoles() { return false; }

    // --- Batch XV Capabilities (161-180) ---
    default boolean canSowFungalSporeCombs() { return false; }
    default boolean canHarnessLarvalSilkCocoon() { return false; }
    default boolean canFormBiomechanicalBivouac() { return false; }
    default boolean canPerformBuzzPollinationSonication() { return false; }
    default boolean canRegurgitateEarwigMaternalFood() { return false; }
    default boolean canRecognizeWaspFacialPatterns() { return false; }
    default boolean canFireShrimpAcousticCannon() { return false; }
    default boolean canDuetPassalidSubstrateVibration() { return false; }
    default boolean canTurnGranarySeedsAeration() { return false; }
    default boolean canEncodeWaggleDanceSunCompass() { return false; }
    default boolean canDigSubterraneanClayAqueducts() { return false; }
    default boolean canFireFormicAcidArtilleryJet() { return false; }
    default boolean canEjectGarbageChuteRefuse() { return false; }
    default boolean canFanWingsForBroodThermoregulation() { return false; }
    default boolean canPlugGallWithChitinousTube() { return false; }
    default boolean canCoatWaspPedicelAntRepellent() { return false; }
    default boolean canSquirtNasuteViscousResin() { return false; }
    default boolean canSqueezeIntrudersWithForelegs() { return false; }
    default boolean canShieldEggsFromParasitoidWasps() { return false; }
    default boolean canPlasterWoodWallGallery() { return false; }

    // --- Batch XVI Capabilities (181-200) ---
    default boolean canShearLeafCrescentMandible() { return false; }
    default boolean canShieldSwarmCoreHeat() { return false; }
    default boolean canPerformQueenPhysogastricPeristalsis() { return false; }
    default boolean canWeaveSocialSilkHammock() { return false; }
    default boolean canPackCorbiculaPollenBaskets() { return false; }
    default boolean canScrapeWoodPulpCarton() { return false; }
    default boolean canLayTrophicNourishmentEggs() { return false; }
    default boolean canNavigatePolarizedLightCompass() { return false; }
    default boolean canBuryFungalWasteInGallery() { return false; }
    default boolean canChewSeedHuskBreadPulp() { return false; }
    default boolean canWrapPreyInCommunalSilk() { return false; }
    default boolean canSealNestGapsWithPropolis() { return false; }
    default boolean canSynchronizeSoldierAlarmDrumming() { return false; }
    default boolean canPerformDominanceMounting() { return false; }
    default boolean canLapNectarTongueExtension() { return false; }
    default boolean canSecreteGallClosingFluid() { return false; }
    default boolean canGroomNymphCuticularSurface() { return false; }
    default boolean canExcavateGardenWasteChambers() { return false; }
    default boolean canExchangeRoyalPairGrooming() { return false; }
    default boolean canTriggerUniversalEmergencyEvacuation() { return false; }

    // --- Batch XVII Capabilities (201-220) ---
    default boolean canStoreNectarAsHoneypotReplete() { return false; }
    default boolean canFormFloatingAntRaft() { return false; }
    default boolean canConstructMudResinEntranceFunnel() { return false; }
    default boolean canExcavateHibernationBurrow() { return false; }
    default boolean canInoculateLeafPulpEnzymes() { return false; }
    default boolean canPerformGamergateDominanceTournament() { return false; }
    default boolean canFeedOnLarvalHemolymphDracula() { return false; }
    default boolean canFormTarsalFrictionBridge() { return false; }
    default boolean canTransportWaterInMandibleDroplet() { return false; }
    default boolean canStiltWalkThermalRegim() { return false; }
    default boolean canPerformAntiPredatorShimmeringWave() { return false; }
    default boolean canDouseNestWaterCooling() { return false; }
    default boolean canAerateFungalCombChambers() { return false; }
    default boolean canFeedLarvaeExuviaRecycling() { return false; }
    default boolean canGroomLeafPulpParasitesMinim() { return false; }
    default boolean canCamouflageWebWithPlantDebris() { return false; }
    default boolean canCockGasterFormicAcidRepellent() { return false; }
    default boolean canMilkAphidHoneydewStroking() { return false; }
    default boolean canClusterSolarHeatCollector() { return false; }
    default boolean canSerializeGlobalEthologicalBitSet() { return false; }

    // --- Academic Ethological & Biological Aliases ---
    default boolean canPerformLarvalHemolymphophily() {
        return canFeedOnLarvalHemolymphDracula();
    }

    default boolean canPerformEclosionVibratoryStimulation() {
        return true;
    }

    default boolean canPerformSubharmonicSentinelStridulation() {
        return true;
    }

    default boolean canExtendProboscisForNectar() {
        return canLapNectarTongueExtension();
    }

    default boolean canSealCellsWithOpercula() {
        return true;
    }

    default boolean canConstructMiddenRefusePiles() {
        return canConstructChaffGarbageDunes();
    }

    default boolean canSecreteAbdominalGelSupport() {
        return canWeaveStenogastrinePaperJelly();
    }

    default boolean canDeliverSalivaryLarvalPap() {
        return canDeliverStenogastrinePapFood();
    }
}
