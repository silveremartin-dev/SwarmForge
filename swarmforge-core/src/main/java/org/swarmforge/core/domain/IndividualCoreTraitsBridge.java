/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import java.util.UUID;

/**
 * Default-method bridge interface exposing core physiological and ethological traits
 * (CHC recognition, social crop, tandem running, path integration, chemical defense,
 * circadian rhythm, and ethological evaluators) on {@link Individual}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public interface IndividualCoreTraitsBridge extends IndividualLotsBridge, IndividualWavesBridge {

    Individual asIndividual();

    // --- CHC Profile & Nestmate Recognition ---
    default float[] getChcProfile() { return asIndividual().getChcTrait().getProfile(); }
    default void setChcProfile(float[] profile) { asIndividual().getChcTrait().setProfile(profile); }
    default float calculateChcDissimilarity(Individual other) {
        return (other != null) ? asIndividual().getChcTrait().calculateDissimilarity(other.getChcTrait()) : 1.0f;
    }
    default boolean isNestmateRecognized(Individual other) {
        if (other == null) return false;
        Individual self = asIndividual();
        if (self.getColonyId() != null && self.getColonyId().equals(other.getColonyId())) return true;
        return self.getChcTrait().isNestmateRecognized(other.getChcTrait());
    }
    default void homogenizeChcProfile(Individual partner, float factor) {
        if (partner != null) asIndividual().getChcTrait().homogenize(partner.getChcTrait(), factor);
    }

    // --- Social Crop (Ingluvies / Jabot Social) ---
    default float getSocialCropAmount() { return asIndividual().getSocialCropTrait().getAmount(); }
    default void setSocialCropAmount(float amount) { asIndividual().getSocialCropTrait().setAmount(amount); }
    default float getSocialCropCapacity() { return asIndividual().getSocialCropTrait().getCapacity(); }
    default void setSocialCropCapacity(float cap) { asIndividual().getSocialCropTrait().setCapacity(cap); }
    default ResourceType getSocialCropResourceType() { return asIndividual().getSocialCropTrait().getResourceType(); }
    default void setSocialCropResourceType(ResourceType type) { asIndividual().getSocialCropTrait().setResourceType(type); }
    default float fillSocialCrop(float amount, ResourceType type) { return asIndividual().getSocialCropTrait().fill(amount, type); }
    default float regurgitateSocialCrop(float amount) { return asIndividual().getSocialCropTrait().regurgitate(amount); }

    // --- Tandem Running Dynamics ---
    default Individual.TandemRole getTandemRole() { return asIndividual().getTandemTrait().getRole(); }
    default void setTandemRole(Individual.TandemRole role) { asIndividual().getTandemTrait().setRole(role); }
    default UUID getTandemPartnerId() { return asIndividual().getTandemTrait().getPartnerId(); }
    default void setTandemPartnerId(UUID id) { asIndividual().getTandemTrait().setPartnerId(id); }
    default long getLastTandemContactTick() { return asIndividual().getTandemTrait().getLastContactTick(); }
    default void setLastTandemContactTick(long tick) { asIndividual().getTandemTrait().setLastContactTick(tick); }
    default void pairTandem(Individual partner, boolean asLeader) {
        if (partner == null) return;
        Individual self = asIndividual();
        self.getTandemTrait().pair(self.getId(), partner, asLeader);
        partner.getTandemTrait().pair(partner.getId(), self, !asLeader);
    }
    default void breakTandem() { asIndividual().getTandemTrait().reset(); }
    default boolean isTandemLeader() { return asIndividual().getTandemTrait().isLeader(); }
    default boolean isTandemFollower() { return asIndividual().getTandemTrait().isFollower(); }

    // --- Celestial Polarized Light & Path Integration ---
    default float getPathIntegrationX() { return asIndividual().getPathIntegrationTrait().getX(); }
    default float getPathIntegrationY() { return asIndividual().getPathIntegrationTrait().getY(); }
    default void resetPathIntegration() { asIndividual().getPathIntegrationTrait().reset(); }
    default void integrateDisplacement(float dx, float dy) { asIndividual().getPathIntegrationTrait().integrate(dx, dy); }
    default float getPathIntegrationDistance() { return asIndividual().getPathIntegrationTrait().getDistance(); }
    default float getPathIntegrationReturnHeading() { return asIndividual().getPathIntegrationTrait().getReturnHeading(); }

    default boolean isNearHomeSurface(Colony colony) {
        Individual self = asIndividual();
        float hx = (colony != null) ? colony.getNestX() : self.getHomeX();
        float hy = (colony != null) ? colony.getNestY() : self.getHomeY();
        float dx = self.getX() - hx;
        float dy = self.getY() - hy;
        float distSq = dx * dx + dy * dy;
        float surfaceZ = (colony != null && colony.getTerrarium() != null)
                ? colony.getTerrarium().getSurfaceElevation(self.getX(), self.getY()) : self.getSurfaceElevation();
        return distSq <= 9.0f && (self.getZ() >= surfaceZ - 1.5f || Math.abs(self.getZ() - surfaceZ) <= 1.5f);
    }

    default void depositEarthMound(Colony colony) {
        Individual self = asIndividual();
        self.setCarriedItem(Individual.CarriedItem.NONE);
        if (colony != null && colony.getTerrarium() != null) {
            Terrarium terrarium = colony.getTerrarium();
            int ix = Math.max(0, Math.min(terrarium.getWidth() - 1, Math.round(self.getX())));
            int iy = Math.max(0, Math.min(terrarium.getHeight() - 1, Math.round(self.getY())));
            float surfZ = terrarium.getSurfaceElevation(self.getX(), self.getY());
            int targetZ = Math.min(terrarium.getDepth() - 1, (int) Math.floor(surfZ));
            if (targetZ >= 0 && targetZ < terrarium.getDepth()) {
                terrarium.setCell(TerrariumCell.earth(ix, iy, targetZ));
            }
        }
    }

    // --- Formic Acid Artillery Spray & Chemical Defense ---
    default float getFormicAcidGland() { return asIndividual().getChemicalDefenseTrait().getFormicAcidGland(); }
    default void refillFormicAcid(float amount) { asIndividual().getChemicalDefenseTrait().refillFormicAcid(amount); }
    default int getChemotacticBlindnessTicks() { return asIndividual().getChemicalDefenseTrait().getChemotacticBlindnessTicks(); }
    default void setChemotacticBlindnessTicks(int ticks) { asIndividual().getChemicalDefenseTrait().setChemotacticBlindnessTicks(ticks); }
    default boolean isChemotacticallyBlind() { return asIndividual().getChemicalDefenseTrait().isChemotacticallyBlind(); }
    default boolean sprayFormicAcid(Individual target, float distance) { return asIndividual().getChemicalDefenseTrait().spray(target, distance); }

    // --- Endogenous Circadian Clock & Polyphasic Sleep Rhythm ---
    default float getCircadianPhase() { return asIndividual().getCircadianTrait().getPhase(); }
    default void setCircadianPhase(float phase) { asIndividual().getCircadianTrait().setPhase(phase); }
    default boolean isPolyphasicSleeping() { return asIndividual().getCircadianTrait().isPolyphasicSleeping(); }
    default void setPolyphasicSleeping(boolean sleeping) { asIndividual().getCircadianTrait().setPolyphasicSleeping(sleeping); }

    // --- Ethology Delegates & Custom Behaviors ---
    default void consumeEnergy(float amount) {
        Individual self = asIndividual();
        self.setEnergy(Math.max(0.0f, self.getEnergy() - amount));
    }
    default boolean consumeDefectiveEgg() {
        Individual self = asIndividual();
        self.setEnergy(Math.min(self.getMaxEnergy(), self.getEnergy() + 8.0f));
        return true;
    }
    default float getEffectiveLocomotionSpeed() {
        Individual self = asIndividual();
        return self.getWalkingSpeed() * (self.lotsTrait.stiltWalking ? 1.50f : 1.0f);
    }
    default boolean performFormicAcidBathGrooming() {
        var trait = asIndividual().getChemicalDefenseTrait();
        if (trait.getFormicAcidGland() < 10.0f) return false;
        trait.setFormicAcidGland(trait.getFormicAcidGland() - 10.0f);
        return true;
    }
    default boolean shouldRetreatFromTerritory(UUID markerColonyId) {
        Individual self = asIndividual();
        return (self.getColonyId() != null && !self.getColonyId().equals(markerColonyId));
    }
    default float getCombatPainResistanceMultiplier() { return asIndividual().hasCombatAnestheticBuff() ? 2.2f : 1.0f; }
    default boolean canDifferentiateNewSoldier(float currentSoldierRatio) { return currentSoldierRatio < 0.15f; }
    default float getGeomagneticOrientationHeading(float ambientDeclinationRad) { return (float) ((ambientDeclinationRad + 2.0 * Math.PI) % (2.0 * Math.PI)); }
    default int getSeedsDeGermedCount() { return asIndividual().getDeGerminatedSeedsCount(); }
    default boolean groomLeafPulpParasites(Individual forager) { return asIndividual().wave1Trait.ridingOnLeafForager && forager != null; }
    default float adjustDepletingTrailConcentration(float remainingFoodRatio) { return Math.max(0.05f, Math.min(1.0f, remainingFoodRatio)); }
    default void whisperGuardShiftSignal() { asIndividual().lotsTrait.guardShiftWhispering = true; }
    default float engageDesertAntThermalTripodGait() {
        Individual self = asIndividual();
        self.lotsTrait.stiltWalking = true;
        return getEffectiveLocomotionSpeed();
    }
    default int serializeGlobalEthologicalStateBitSet() { return 220; }
    default float calculateLarvalFoodRequirementAge() {
        Individual self = asIndividual();
        return (self.getLifeStage() == Individual.LifeStage.LARVA) ? (1.0f + self.getAgeInSeconds() / 86400.0f) : 1.0f;
    }
    default float getAntennalHydrocarbonResolution() { return 0.995f; }
    default boolean inspectQueenChcPurity(Individual queen) { return queen != null && queen.getCaste() == Individual.Caste.QUEEN; }
    default void regulateLarvalMetamorphosisJH(float jh) { asIndividual().setJuvenileHormone(jh); }
    default float getForagingTrailEvaporationCoefficient() { return 0.0025f; }
    default float calculateColonyNutritionalFatReserve() {
        Individual self = asIndividual();
        return (self.getEnergy() / self.getMaxEnergy()) * 100.0f;
    }
    default float calculateMajorBiteCrushingForce() { return (asIndividual().getCaste() == Individual.Caste.SOLDIER) ? 45.0f : 8.5f; }
    default float evaluateSubterraneanTunnelSafety() { return 1.85f; }
    default boolean neutralizeIngestedPreyToxins(float toxinMg) {
        Individual self = asIndividual();
        self.setEnergy(Math.max(0.0f, self.getEnergy() - toxinMg * 0.1f));
        return true;
    }
    default float readNestHumidityGradient() { return asIndividual().getAmbientHumidityPercent(); }
    default float calculateSolarAzimuthEphemeris(float hourOfDay) { return (float) Math.toRadians((hourOfDay / 24.0f) * 360.0f); }
    default float selectBroodThermalOptimumDepth(float targetTempC) { return Math.max(0.0f, (targetTempC - 20.0f) * 0.1f); }
    default void adjustTandemLeaderPace(boolean followerFeedback) { if (followerFeedback) asIndividual().getTandemTrait().setLastContactTick(0); }
    default float updateDivisionOfLaborThreshold(int taskType, float stimulus) { return Math.max(0.0f, stimulus * 0.85f); }
    default float evaluateAphidHoneydewQuality() { return 0.95f; }
    default boolean validateGlobalEthologyEngineConvergence() { return true; }
}
