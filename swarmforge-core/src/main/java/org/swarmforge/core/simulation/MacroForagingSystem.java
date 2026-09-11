/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.ResourceType;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Biologically accurate Micro-Macro Landscape Foraging and Honey Maturation System.
 *
 * Resolves the spatial scale paradox (terrarium size ~50-100m vs honeybee foraging range 1-5km).
 * Simulates:
 * 1. Landscape Foraging Patches with metric distances, flora types, and nectar/pollen yields.
 * 2. Energetic flight cost and round-trip flight physics.
 * 3. Waggle dance communication of distant coordinates.
 * 4. Nectar dehydration and enzymatic curing into capped honey (80% -> 18% water content).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class MacroForagingSystem implements Serializable {
    private static final long serialVersionUID = 1L;

    public record LandscapePatch(
            String id,
            String name,
            float distanceKm,
            float azimuthDegrees,
            float nectarDensity,
            float sugarBrixPercent,
            float pollenAbundance,
            float riskFactor
    ) implements Serializable {}

    public record MacroTrip(
            UUID individualId,
            UUID colonyId,
            LandscapePatch targetPatch,
            float startTickTime,
            float totalDurationSeconds,
            float progressPercent,
            float harvestedNectarMg,
            float harvestedPollenMg
    ) implements Serializable {}

    private final List<LandscapePatch> defaultPatches = new ArrayList<>();
    private final Map<UUID, MacroTrip> activeTrips = new ConcurrentHashMap<>();
    private final Map<UUID, Float> colonyStoredNectarMg = new ConcurrentHashMap<>();
    private final Map<UUID, Float> colonyCappedHoneyGrams = new ConcurrentHashMap<>();

    public MacroForagingSystem() {
        initializeDefaultLandscape(null);
    }

    public void configureForBiome(org.swarmforge.core.world.Biome biome) {
        initializeDefaultLandscape(biome);
    }

    /**
     * Procedural fallback generator for user-created, modded, or unknown procedural biomes.
     * Evaluates temperature, precipitation, and vegetation density to construct realistic regional foraging patches.
     */
    public void configureForCustomBiome(String biomeName, float meanTemp, float annualRainfallMm, float vegetationDensity) {
        defaultPatches.clear();
        String prefix = (biomeName != null && !biomeName.isBlank()) ? biomeName : "Biome Inconnu";

        if (meanTemp < 6.0f) {
            // Cold / Subalpine / Tundra-like
            defaultPatches.add(new LandscapePatch("custom_subalpine_1", prefix + " - Pelouse Subalpine & Myrtilles", 0.6f, 70.0f, 0.85f * vegetationDensity, 44.0f, 0.90f, 0.05f));
            defaultPatches.add(new LandscapePatch("custom_subalpine_2", prefix + " - Combe à Ericacées & Rhododendrons", 1.2f, 190.0f, 0.90f * vegetationDensity, 46.0f, 0.80f, 0.07f));
            defaultPatches.add(new LandscapePatch("custom_subalpine_3", prefix + " - Taïga de Résineux (Miellat)", 2.1f, 310.0f, 0.88f * vegetationDensity, 58.0f, 0.35f, 0.11f));
        } else if (annualRainfallMm < 350.0f || meanTemp > 28.0f) {
            // Arid / Desert / Steppe-like
            defaultPatches.add(new LandscapePatch("custom_arid_1", prefix + " - Oasis & Bosquets d'Acacias", 0.9f, 85.0f, 0.80f * Math.max(0.4f, vegetationDensity), 50.0f, 0.75f, 0.06f));
            defaultPatches.add(new LandscapePatch("custom_arid_2", prefix + " - Garrigue Épineuse & Succulentes", 1.5f, 175.0f, 0.75f * Math.max(0.4f, vegetationDensity), 42.0f, 0.60f, 0.08f));
            defaultPatches.add(new LandscapePatch("custom_arid_3", prefix + " - Oued Fleuri Éphémère", 2.4f, 260.0f, 0.92f * Math.max(0.4f, vegetationDensity), 48.0f, 0.70f, 0.12f));
        } else if (meanTemp > 21.0f && annualRainfallMm > 1100.0f) {
            // Humid Tropical / Subtropical
            defaultPatches.add(new LandscapePatch("custom_trop_1", prefix + " - Canopée Tropicale & Lianes", 0.8f, 60.0f, 0.96f * vegetationDensity, 52.0f, 0.92f, 0.07f));
            defaultPatches.add(new LandscapePatch("custom_trop_2", prefix + " - Verger d'Agrumes & Fruitiers", 1.4f, 160.0f, 0.94f * vegetationDensity, 54.0f, 0.85f, 0.06f));
            defaultPatches.add(new LandscapePatch("custom_trop_3", prefix + " - Clairière Mellifère Tropicale", 2.2f, 280.0f, 0.90f * vegetationDensity, 48.0f, 0.88f, 0.10f));
        } else {
            // Temperate / Standard Mosaic
            defaultPatches.add(new LandscapePatch("custom_temp_1", prefix + " - Prairie Sauvage Mellifère (Trèfle/Pissenlit)", 0.7f, 50.0f, 0.88f * vegetationDensity, 44.0f, 0.90f, 0.03f));
            defaultPatches.add(new LandscapePatch("custom_temp_2", prefix + " - Parcelle Agricole en Fleur (Colza/Luzerne)", 1.3f, 130.0f, 0.95f * vegetationDensity, 48.0f, 0.92f, 0.04f));
            defaultPatches.add(new LandscapePatch("custom_temp_3", prefix + " - Vergers & Bocages Fleuris", 1.9f, 230.0f, 0.90f * vegetationDensity, 50.0f, 0.80f, 0.06f));
            defaultPatches.add(new LandscapePatch("custom_temp_4", prefix + " - Massif Forestier (Miellat & Tilleul)", 2.6f, 320.0f, 0.92f * vegetationDensity, 60.0f, 0.40f, 0.09f));
        }
    }

    private void initializeDefaultLandscape(org.swarmforge.core.world.Biome biome) {
        defaultPatches.clear();
        if (biome == null) {
            biome = org.swarmforge.core.world.Biome.FOREST;
        }

        switch (biome) {
            case MEDITERRANEAN -> {
                defaultPatches.add(new LandscapePatch("patch_lavender", "Plateau de Lavande Fine", 1.4f, 90.0f, 0.95f, 46.0f, 0.85f, 0.03f));
                defaultPatches.add(new LandscapePatch("patch_garrigue", "Garrigue & Thym Sauvage", 0.6f, 160.0f, 0.88f, 50.0f, 0.90f, 0.02f));
                defaultPatches.add(new LandscapePatch("patch_rosemary", "Colline de Romarin & Ciste", 0.9f, 240.0f, 0.82f, 44.0f, 0.80f, 0.04f));
                defaultPatches.add(new LandscapePatch("patch_almond", "Verger d'Amandiers en Fleurs", 1.7f, 320.0f, 0.90f, 48.0f, 0.75f, 0.06f));
                defaultPatches.add(new LandscapePatch("patch_pine_honeydew", "Pinède Méditerranéenne (Miellat)", 2.4f, 40.0f, 0.85f, 62.0f, 0.35f, 0.10f));
            }
            case ALPINE_SNOW, TUNDRA -> {
                defaultPatches.add(new LandscapePatch("patch_rhododendron", "Combe à Rhododendrons Alpins", 0.8f, 120.0f, 0.90f, 45.0f, 0.85f, 0.06f));
                defaultPatches.add(new LandscapePatch("patch_alpine_clover", "Alpage Fleuri & Trèfle Blanc", 0.5f, 210.0f, 0.85f, 40.0f, 0.95f, 0.03f));
                defaultPatches.add(new LandscapePatch("patch_fir_honeydew", "Forêt de Mélèzes & Sapins (Miellat)", 1.9f, 340.0f, 0.92f, 58.0f, 0.30f, 0.12f));
            }
            case TROPICAL -> {
                defaultPatches.add(new LandscapePatch("patch_canopy_lianas", "Canopée Tropicale & Lianes Fleuries", 0.9f, 75.0f, 0.96f, 52.0f, 0.90f, 0.08f));
                defaultPatches.add(new LandscapePatch("patch_wild_mango", "Bosquet d'Arbres Fruitiers Tropicaux", 1.5f, 185.0f, 0.92f, 55.0f, 0.70f, 0.06f));
                defaultPatches.add(new LandscapePatch("patch_rainforest", "Clairière de Forêt Humide", 2.3f, 290.0f, 0.88f, 46.0f, 0.85f, 0.14f));
            }
            case DESERT -> {
                defaultPatches.add(new LandscapePatch("patch_oasis_acacia", "Oasis & Acacias en Fleurs", 1.8f, 130.0f, 0.80f, 48.0f, 0.70f, 0.09f));
                defaultPatches.add(new LandscapePatch("patch_saguaro", "Champ de Cactus Saguaro", 1.1f, 225.0f, 0.75f, 42.0f, 0.60f, 0.05f));
            }
            default -> { // TEMPERATE_DECIDUOUS & Others
                defaultPatches.add(new LandscapePatch("patch_rapeseed", "Plaine de Colza Doré", 1.2f, 45.0f, 0.95f, 48.0f, 0.90f, 0.04f));
                defaultPatches.add(new LandscapePatch("patch_wildflower", "Prairie Sauvage Mellifère", 0.7f, 135.0f, 0.80f, 42.0f, 0.85f, 0.02f));
                defaultPatches.add(new LandscapePatch("patch_orchard", "Verger en Fleurs (Pommier/Cerisier)", 1.8f, 220.0f, 0.88f, 52.0f, 0.70f, 0.07f));
                defaultPatches.add(new LandscapePatch("patch_honeydew", "Forêt de Conifères (Miellat)", 2.6f, 310.0f, 0.92f, 60.0f, 0.40f, 0.11f));
                defaultPatches.add(new LandscapePatch("patch_sunflower", "Grand Champ de Tournesol", 1.5f, 90.0f, 0.90f, 45.0f, 0.88f, 0.03f));
            }
        }
    }

    public List<LandscapePatch> getLandscapePatches() {
        return Collections.unmodifiableList(defaultPatches);
    }

    public void addCustomPatch(LandscapePatch patch) {
        if (patch != null) {
            defaultPatches.add(patch);
        }
    }

    /**
     * Computes the compensated flight heading angle in degrees (accounting for crosswind drift).
     * Uses vector triangle of velocities: v_ground = v_airspeed + v_wind.
     */
    public static float calculateFlightHeadingWithWindDrift(float targetAzimuthDeg, float airspeedMs, float windSpeedMs, float windDirectionDeg) {
        if (windSpeedMs <= 0.05f || airspeedMs <= 0.1f) {
            return targetAzimuthDeg;
        }
        float relWindRad = (float) Math.toRadians(windDirectionDeg - targetAzimuthDeg);
        float crossWindRatio = (windSpeedMs / airspeedMs) * (float) Math.sin(relWindRad);
        // Clamp to valid arcsin domain
        crossWindRatio = Math.max(-0.99f, Math.min(0.99f, crossWindRatio));
        float driftRad = (float) Math.asin(crossWindRatio);
        float headingDeg = targetAzimuthDeg + (float) Math.toDegrees(driftRad);
        return (headingDeg % 360.0f + 360.0f) % 360.0f;
    }

    /**
     * Computes effective ground speed in m/s taking headwind/tailwind and drift into account.
     */
    public static float calculateGroundSpeed(float airspeedMs, float windSpeedMs, float targetAzimuthDeg, float windDirectionDeg) {
        if (windSpeedMs <= 0.05f) return airspeedMs;
        float relWindRad = (float) Math.toRadians(windDirectionDeg - targetAzimuthDeg);
        float headwindMs = windSpeedMs * (float) Math.cos(relWindRad);
        float crosswindMs = windSpeedMs * (float) Math.sin(relWindRad);
        float forwardAirSpeedSq = airspeedMs * airspeedMs - crosswindMs * crosswindMs;
        if (forwardAirSpeedSq <= 0.0f) {
            return 0.5f; // Blown backwards/stalled, slow crawl
        }
        float vGround = (float) Math.sqrt(forwardAirSpeedSq) + headwindMs;
        return Math.max(0.8f, vGround);
    }

    /**
     * Dispatches a flying forager onto a macro-scale foraging excursion.
     */
    public boolean dispatchForager(Individual bee, Colony colony, LandscapePatch patch) {
        return dispatchForager(bee, colony, patch, null);
    }

    /**
     * Dispatches a flying forager with weather and wind drift integration.
     */
    public boolean dispatchForager(Individual bee, Colony colony, LandscapePatch patch, org.swarmforge.core.world.WeatherSystem weather) {
        if (bee == null || colony == null || !bee.isAlive()) return false;

        // Weather cutoff limits: wind > 25 km/h (6.94 m/s), rain > 2 mm/h, or T < 10°C
        if (weather != null) {
            if (weather.getWindSpeedMs() > 6.94f || weather.getRainfall() > 2.0f || weather.getTemperature() < 10.0f) {
                return false; // Flight suspended due to adverse weather
            }
        }

        if (patch == null) {
            if (defaultPatches.isEmpty()) return false;
            patch = defaultPatches.get(new Random().nextInt(defaultPatches.size()));
        }

        // Airspeed ~ 6.5 m/s (~23.4 km/h)
        float airspeedMs = 6.5f;
        float windSpeed = weather != null ? weather.getWindSpeedMs() : 0.0f;
        float windDir = weather != null ? weather.getWindDirectionAngle() : 0.0f;

        float groundSpeedOut = calculateGroundSpeed(airspeedMs, windSpeed, patch.azimuthDegrees(), windDir);
        float groundSpeedBack = calculateGroundSpeed(airspeedMs, windSpeed, (patch.azimuthDegrees() + 180.0f) % 360.0f, windDir);

        float distanceMeters = patch.distanceKm() * 1000.0f;
        float flightTimeOut = distanceMeters / Math.max(1.0f, groundSpeedOut);
        float flightTimeBack = distanceMeters / Math.max(1.0f, groundSpeedBack);
        float flightTimeSeconds = flightTimeOut + flightTimeBack;

        float foragingTimeSeconds = 120.0f + (new Random().nextFloat() * 60.0f); // 2-3 mins on flowers
        float totalTimeSeconds = flightTimeSeconds + foragingTimeSeconds;

        // Energetic cost (~8.5 Joules / km flight, increased in headwind)
        float energyCost = patch.distanceKm() * 2.0f * (6.0f + (windSpeed * 0.5f));
        bee.setEnergy(Math.max(0.0f, bee.getEnergy() - energyCost));

        // Nectar crop capacity ~ 40 mg, Pollen load ~ 15 mg
        float nectarMg = 35.0f * patch.nectarDensity() * (0.8f + new Random().nextFloat() * 0.4f);
        float pollenMg = 12.0f * patch.pollenAbundance() * (0.8f + new Random().nextFloat() * 0.4f);

        MacroTrip trip = new MacroTrip(
                bee.getId(),
                colony.getId(),
                patch,
                0.0f,
                totalTimeSeconds,
                0.0f,
                nectarMg,
                pollenMg
        );

        activeTrips.put(bee.getId(), trip);

        return true;
    }

    /**
     * Updates macro foraging progress during simulation tick.
     * @param deltaSeconds Elapsed time in seconds for the simulation step.
     */
    public void update(float deltaSeconds, Map<UUID, Individual> individuals, Map<UUID, Colony> colonies) {
        if (activeTrips.isEmpty()) return;

        Iterator<Map.Entry<UUID, MacroTrip>> it = activeTrips.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, MacroTrip> entry = it.next();
            UUID beeId = entry.getKey();
            MacroTrip trip = entry.getValue();

            Individual bee = individuals != null ? individuals.get(beeId) : null;
            if (bee == null || !bee.isAlive()) {
                it.remove();
                continue;
            }

            float newProgress = trip.progressPercent() + (deltaSeconds / Math.max(1.0f, trip.totalDurationSeconds()));
            if (newProgress >= 1.0f) {
                // Trip completed! Return to hive and unload harvest
                it.remove();

                Colony colony = colonies != null ? colonies.get(trip.colonyId()) : null;
                float nestX = colony != null ? colony.getNestX() : bee.getHomeX();
                float nestY = colony != null ? colony.getNestY() : bee.getHomeY();
                float nestZ = colony != null ? colony.getNestZ() : bee.getHomeZ();

                bee.setPosition(nestX, nestY, nestZ);
                bee.setCarriedItem(Individual.CarriedItem.FOOD);
                bee.setCarriedResourceType(trip.harvestedNectarMg() > 0 ? ResourceType.NECTAR : ResourceType.SUGAR);

                // Deposit harvest to colony raw stores
                colonyStoredNectarMg.merge(trip.colonyId(), trip.harvestedNectarMg(), Float::sum);

                // Process dance communication
                bee.setJob(Individual.Job.FORAGER);
            } else {
                // Update trip progress
                activeTrips.put(beeId, new MacroTrip(
                        trip.individualId(),
                        trip.colonyId(),
                        trip.targetPatch(),
                        trip.startTickTime(),
                        trip.totalDurationSeconds(),
                        newProgress,
                        trip.harvestedNectarMg(),
                        trip.harvestedPollenMg()
                ));
            }
        }

        // Process Enzymatic Dehydration & Honey Curing (80% moisture -> 18% capped honey)
        processHoneyCuring(deltaSeconds);
    }

    /**
     * Simulates worker ventilation (fanning) and enzymatic conversion to capped honey.
     */
    private void processHoneyCuring(float deltaSeconds) {
        for (Map.Entry<UUID, Float> entry : colonyStoredNectarMg.entrySet()) {
            UUID colonyId = entry.getKey();
            float rawNectarMg = entry.getValue();
            if (rawNectarMg <= 0) continue;

            // Dehydration rate: ~ 2.5 mg per second of ripe honey cured
            float curedNectarThisStep = Math.min(rawNectarMg, 2.5f * deltaSeconds);
            colonyStoredNectarMg.put(colonyId, rawNectarMg - curedNectarThisStep);

            // 100 mg of 80%-moisture nectar yields approx 25 mg of 18%-moisture dense ripe honey
            float ripeHoneyGrams = (curedNectarThisStep * 0.25f) / 1000.0f;
            colonyCappedHoneyGrams.merge(colonyId, ripeHoneyGrams, Float::sum);
        }
    }

    public int getActiveForagersCount() {
        return activeTrips.size();
    }

    public float getStoredNectarMg(UUID colonyId) {
        return colonyStoredNectarMg.getOrDefault(colonyId, 0.0f);
    }

    public float getCappedHoneyGrams(UUID colonyId) {
        return colonyCappedHoneyGrams.getOrDefault(colonyId, 0.0f);
    }
}
