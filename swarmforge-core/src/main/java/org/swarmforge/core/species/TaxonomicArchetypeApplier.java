/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.species;

/**
 * Service applying baseline taxonomic archetypes and ethological capabilities
 * based on taxonomic order and genus to a {@link DefaultSpecies}.
 */
public final class TaxonomicArchetypeApplier {

    private TaxonomicArchetypeApplier() {
    }

    /**
     * Applies baseline taxonomic archetype parameters and ethological capabilities based on
     * the taxonomic order and genus, establishing a biologically sound foundation that specific species
     * can specialize or override.
     *
     * @param species the target DefaultSpecies instance
     * @param genus taxonomic genus
     * @param order insect order
     */
    public static void apply(DefaultSpecies species, String genus, Species.InsectOrder order) {
        if (species == null) return;

        if (genus != null && !genus.isBlank()) {
            species.setGenus(genus);
        }
        if (order != null) {
            species.setInsectType(switch (order) {
                case BEE -> "BEE";
                case WASP -> "WASP";
                case TERMITE -> "TERMITE";
                case APHID -> "APHID";
                case THRIPS -> "THRIPS";
                case BEETLE -> "BEETLE";
                default -> "ANT";
            });
        }

        String insectType = species.getInsectType();

        // 1. Order-Level Archetypes
        if (order == Species.InsectOrder.BEE || "BEE".equalsIgnoreCase(insectType)) {
            species.setWorkersCanFly(true);
            if (species.getPrimaryDiet() == null) species.setPrimaryDiet("SUGARS_NECTAR");
            if (species.getSecondaryDiet() == null) species.setSecondaryDiet("SEEDS");
            if (species.getNestType() == null) species.setNestType("WAX_COMB");
            if (species.getVenomType() == null) species.setVenomType("VENOMOUS_STING");
            if (species.getQueenCountMode() == null) species.setQueenCountMode("MONOGYNE");
            species.setOptimalTempCelsius(25.0f);
            species.setMinTempCelsius(10.0f);
            species.setMaxTempCelsius(38.0f);
            species.setFlyingSpeed(6.0f);
            species.setWalkingSpeed(0.25f);
            species.setWingbeatFrequencyHz(220.0f);
            species.setHasHoveringCapability(true);
            species.setHasElectrosensing(true);
            species.setHasPolarizedLightNavigation(true);
            species.setCanPerformSocialThermoregulation(true);
            species.setCanPerformNecrophoresis(true);
            species.setCanPerformEvaporativeCooling(true);
            species.setCanLapNectarTongueExtension(true);
            species.setCanPackCorbiculaPollenBaskets(true);
            species.setCanFanWingsForBroodThermoregulation(true);
            species.setCanPerformThoracicIncubation(true);
            species.setCanApplyBeeBreadHydrophobicCoating(true);
            species.setCanHarvestDewCondensation(true);
        } else if (order == Species.InsectOrder.WASP || "WASP".equalsIgnoreCase(insectType)) {
            species.setWorkersCanFly(true);
            if (species.getPrimaryDiet() == null) species.setPrimaryDiet("INSECTS_MEAT");
            if (species.getSecondaryDiet() == null) species.setSecondaryDiet("SUGARS_NECTAR");
            if (species.getNestType() == null) species.setNestType("PAPER_NEST");
            if (species.getVenomType() == null) species.setVenomType("VENOMOUS_STING");
            species.setOptimalTempCelsius(26.0f);
            species.setMinTempCelsius(12.0f);
            species.setMaxTempCelsius(40.0f);
            species.setFlyingSpeed(6.5f);
            species.setWalkingSpeed(0.28f);
            species.setWingbeatFrequencyHz(160.0f);
            species.setHasHoveringCapability(true);
            species.setHasPolarizedLightNavigation(true);
            species.setCanMasticatePaperPulpCarton(true);
            species.setCanScrapeWoodPulpCarton(true);
            species.setCanHarvestLarvalSalivaDroplets(true);
            species.setCanApplyPedicelAntRepellent(true);
            species.setCanCoatWaspPedicelAntRepellent(true);
            species.setCanRecognizeFacialVisualPatterns(true);
            species.setCanDrumAbdomenWaspCellRim(true);
            species.setCanPerformNecrophoresis(true);
        } else if (order == Species.InsectOrder.TERMITE || "TERMITE".equalsIgnoreCase(insectType)) {
            species.setWorkersCanFly(false);
            species.setHasKing(true);
            if (species.getPrimaryDiet() == null) species.setPrimaryDiet("WOOD_CELLULOSE");
            if (species.getSecondaryDiet() == null) species.setSecondaryDiet("FUNGUS");
            if (species.getNestType() == null) species.setNestType("WOOD_TUNNELS");
            if (species.getVenomType() == null) species.setVenomType("POWERFUL_MANDIBLES");
            species.setOptimalTempCelsius(25.0f);
            species.setMinTempCelsius(10.0f);
            species.setMaxTempCelsius(36.0f);
            species.setWalkingSpeed(0.25f);
            species.setHasTermiteGutSymbiosis(true);
            species.setHasProctodealTrophallaxis(true);
            species.setCanTrophallaxisProtozoa(true);
            species.setCanDrumSubstrate(true);
            species.setCanSynchronizeSoldierAlarmDrumming(true);
            species.setCanBlockRoyalChamberSentry(true);
            species.setCanPlasterWoodWallGallery(true);
            species.setCanPerformQueenPhysogastricPeristalsis(true);
            species.setCanExchangeRoyalPairGrooming(true);
            species.setHasMagnetoreception(true);
            species.setHasHygroreception(true);
        } else if (order == Species.InsectOrder.APHID || "APHID".equalsIgnoreCase(insectType)) {
            species.setWorkersCanFly(false);
            if (species.getPrimaryDiet() == null) species.setPrimaryDiet("SUGARS_NECTAR");
            if (species.getSecondaryDiet() == null) species.setSecondaryDiet("SUGARS_NECTAR");
            if (species.getNestType() == null) species.setNestType("ARBOREAL_LEAF");
            if (species.getVenomType() == null) species.setVenomType("CHEMICAL_SPRAY");
            species.setCanEjectHoneydewSignalingDroplets(true);
            species.setCanStabFrontalHornsAphid(true);
        } else if (order == Species.InsectOrder.THRIPS || "THRIPS".equalsIgnoreCase(insectType)) {
            species.setWorkersCanFly(false);
            if (species.getPrimaryDiet() == null) species.setPrimaryDiet("SUGARS_NECTAR");
            if (species.getNestType() == null) species.setNestType("WOOD_TUNNELS");
            species.setCanSqueezeGallIntrudersThrips(true);
            species.setCanRepairGallSubstratalSecretion(true);
            species.setCanPlugGallWithChitinousTube(true);
            species.setCanSecreteGallClosingFluid(true);
        } else { // Formicidae (Ants)
            species.setWorkersCanFly(false);
            if (species.getPrimaryDiet() == null) species.setPrimaryDiet("SUGARS_NECTAR");
            if (species.getSecondaryDiet() == null) species.setSecondaryDiet("INSECTS_MEAT");
            if (species.getNestType() == null) species.setNestType("UNDERGROUND_BURROW");
            species.setCanPerformNecrophoresis(true);
            species.setCanPerformAllogrooming(true);
            species.setHasSubstrateAdhesionArolia(true);
            species.setCanHarvestDewCondensation(true);
            species.setCanTransportChainBrood(true);
            species.setCanPerformExoskeletonAntiFungalPatrol(true);
            species.setCanRepairBreachesClay(true);
            species.setHasThermalTrailDecay(true);
        }

        // 2. Genus-Level Archetypes
        String g = (genus != null) ? genus.trim().toLowerCase() : "";
        if (g.startsWith("apis")) {
            species.setCanPerformWaggleDance(true);
            species.setCanEncodeWaggleDanceSunCompass(true);
            species.setCanPerformTrembleDance(true);
            species.setCanCollectPropolis(true);
            species.setCanSealNestGapsWithPropolis(true);
            species.setCanPerformQueenPiping(true);
            species.setCanDouseNestWaterCooling(true);
            species.setCanShieldSwarmCoreHeat(true);
        } else if (g.startsWith("bombus")) {
            species.setCanPerformBuzzPollination(true);
            species.setCanPerformBuzzPollinationSonication(true);
            species.setCanForageSubZeroBumblebee(true);
            species.setCanIncubateBroodAbdominalHeat(true);
            species.setCanConstructNectarWaxPots(true);
            species.setCanLearnTrapliningFlightRoutes(true);
            species.setMinTempCelsius(4.0f);
        } else if (g.startsWith("vespa")) {
            species.setCanEmitHornetGroupAlarmPheromone(true);
            species.setAggression(0.8f);
        } else if (g.startsWith("formica")) {
            if (species.getVenomType() == null) species.setVenomType("FORMIC_ACID");
            species.setCanSprayFormicResinDisinfectant(true);
            species.setCanFireFormicAcidArtilleryJet(true);
            species.setCanFarmAphids(true);
            species.setCanMilkAphidHoneydewStroking(true);
            species.setCanClusterSolarHeatCollector(true);
            species.setHasSolarOrientedMound(true);
            species.setCanPerformThoracicIncubation(true);
            species.setIsPolycalic(true);
        } else if (g.startsWith("atta") || g.startsWith("acromyrmex")) {
            if (species.getPrimaryDiet() == null) species.setPrimaryDiet("FUNGUS");
            if (species.getVenomType() == null) species.setVenomType("POWERFUL_MANDIBLES");
            species.setCanFarmFungus(true);
            species.setCanWeedFungusGarden(true);
            species.setCanShearLeafCrescentMandible(true);
            species.setCanInoculateLeafPulpEnzymes(true);
            species.setCanGroomLeafPulpParasitesMinim(true);
            species.setCanExcavateGardenWasteChambers(true);
            species.setCanAerateFungalCombChambers(true);
            species.setCanStridulateRescueCall(true);
            species.setCanCultivateStreptomycesAntibiotics(true);
        } else if (g.startsWith("camponotus")) {
            if (species.getNestType() == null) species.setNestType("WOOD_TUNNELS");
            species.setCanDrumSubstrate(true);
            species.setCanSnapMandibleAcousticAlarm(true);
            species.setCanPlasterWoodWallGallery(true);
            species.setHasTermiteGutSymbiosis(true); // Blochmannia endosymbiont
        } else if (g.startsWith("solenopsis")) {
            if (species.getVenomType() == null) species.setVenomType("VENOMOUS_STING");
            species.setCanFormFloatingAntRaft(true);
            species.setCanFormLivingRaft(true);
            species.setHasTerritorialRepellentPheromone(true);
            species.setCanPerformBiostructures(true);
        } else if (g.startsWith("cataglyphis")) {
            species.setCanStiltWalkThermalRegim(true);
            species.setHasUVPolarizedLightNavigation(true);
            species.setCanNavigatePolarizedTwilightUV(true);
            species.setHasPolarizedLightNavigation(true);
        } else if (g.startsWith("odontomachus")) {
            if (species.getVenomType() == null) species.setVenomType("POWERFUL_MANDIBLES");
            species.setHasTrapJawMechanism(true);
            species.setCanSnapTrapMandiblesCatapult(true);
            species.setCanPerformGamergateDominanceTournament(true);
        } else if (g.startsWith("linepithema")) {
            species.setIsUnicolonial(true);
            species.setFormsMegaColonies(true);
            species.setHasThermalTrailDecay(true);
            species.setHasTerritorialRepellentPheromone(true);
        } else if (g.startsWith("lasius")) {
            species.setCanFarmAphids(true);
            species.setCanMilkAphidHoneydewStroking(true);
            species.setCanEnforceAphidSanitaryCordon(true);
            species.setCanPlugContaminatedGalleries(true);
        } else if (g.startsWith("polyergus")) {
            species.setIsSlaveMakingSpecies(true);
            species.setCanHarmonizeChcGestalt(true);
        }
    }
}
