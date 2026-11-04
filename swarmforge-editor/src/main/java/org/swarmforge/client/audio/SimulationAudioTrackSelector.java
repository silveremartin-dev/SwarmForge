/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.audio;

import java.io.File;
import java.util.Map;

/**
 * Evaluates environmental, meteorological, and ecological simulation parameters to select
 * and cross-fade audio tracks across multi-channel audio players.
 */
final class SimulationAudioTrackSelector {

    private SimulationAudioTrackSelector() {}

    static void updateSoundBankClips(
            SimulationAudioManager manager,
            Map<String, AudioChannelPlayer> channels,
            boolean simRunning,
            double masterVolume,
            String currentWeather,
            String currentBiome,
            boolean riverEnabled,
            boolean weatherEnabled,
            boolean ambientEnabled,
            boolean insectEnabled,
            double rainRateMmHr,
            double windSpeedMps,
            double cameraZoom,
            double cameraDepth,
            int populationCount,
            double latitude,
            int dayOfYear,
            double timeOfDayHours,
            double ambientLightLevel
    ) {
        double deltaSec = 0.04;

        if (!simRunning || masterVolume <= 0.001) {
            for (AudioChannelPlayer channel : channels.values()) {
                channel.stopWithFade();
                channel.updateFadeTick(deltaSec, masterVolume);
            }
            return;
        }

        String wUpper = currentWeather != null ? currentWeather.toUpperCase() : "CLEAR";
        String bUpper = currentBiome != null ? currentBiome.toUpperCase() : "FOREST";

        double declinationRad = Math.toRadians(23.45 * Math.sin(2.0 * Math.PI * (284.0 + dayOfYear) / 365.0));
        double latRad = Math.toRadians(latitude);
        double hourAngleRad = Math.toRadians(15.0 * (timeOfDayHours - 12.0));
        double sinElevation = Math.sin(latRad) * Math.sin(declinationRad) +
                              Math.cos(latRad) * Math.cos(declinationRad) * Math.cos(hourAngleRad);

        double declinationDay = 23.45 * Math.sin(Math.toRadians((dayOfYear - 81) * 360.0 / 365.0));
        double tanProduct = -Math.tan(latRad) * Math.tan(Math.toRadians(declinationDay));
        double dayLength = (tanProduct >= 1.0) ? 0.0 : (tanProduct <= -1.0) ? 24.0 : (2.0 * Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, tanProduct)))) / 15.0);
        double sunrise = 12.0 - dayLength * 0.5;
        double sunset = 12.0 + dayLength * 0.5;

        boolean isSunAboveHorizon = sinElevation > 0;
        boolean isAstronomicalDay = (timeOfDayHours >= sunrise && timeOfDayHours <= sunset);
        boolean isDay = (ambientLightLevel > 0.15) || (isAstronomicalDay && isSunAboveHorizon);

        // 1. River Water Sound
        if (riverEnabled) {
            File riverFile = manager.findSoundFile("WATRFlow_Small stream 4 (ID 1354)_BigSoundBank.com.mp3");
            if (riverFile == null) riverFile = manager.findSoundFile("AMB_Nature Pack Vol 1_Water Enviroments_Mountain Stream.mp3");
            if (riverFile == null) riverFile = manager.findSoundFile("WATRFlow_Watercourse 5 2 (ID 3137)_BigSoundBank.com.mp3");
            if (riverFile == null) riverFile = manager.findSoundFile("mixkit-river-water-flow-and-surroundings-2452.wav");
            if (riverFile == null) riverFile = manager.findSoundFile("mixkit-water-flowing-ambience-loop-3126.wav");

            double riverZoomAtten = Math.max(0.08, Math.min(1.0, 1.0 - (cameraZoom - 2.0) / 12.0));
            channels.get("RIVER").playTrack(riverFile, 0.35 * riverZoomAtten, masterVolume);
        } else {
            channels.get("RIVER").stopWithFade();
        }

        // 2. Weather Rain / Hail Sound
        boolean isHail = wUpper.contains("HAIL") || wUpper.contains("GRÊLE");
        boolean isRain = manager.isRainType(wUpper) || rainRateMmHr > 0.1;
        boolean heavyRain = wUpper.contains("HEAVY") || wUpper.contains("FORTE") || wUpper.contains("THUNDER") || wUpper.contains("ORAGE") || rainRateMmHr > 10.0;

        if (weatherEnabled && isHail) {
            File hailFile = manager.findSoundFile("saturn-3-music-strong-hail-falling-against-the-window-116182.mp3");
            if (hailFile == null) hailFile = manager.findSoundFile("freesound_community-hail-74904.mp3");
            channels.get("WEATHER_RAIN").playTrack(hailFile, 0.60, masterVolume);
        } else if (weatherEnabled && isRain) {
            File rainFile = heavyRain ? manager.findSoundFile("mixkit-heavy-rain-2403.wav") : manager.findSoundFile("mixkit-light-rain-loop-2393.wav");
            if (rainFile == null) rainFile = manager.findSoundFile("Sound_of_rain.ogg");
            channels.get("WEATHER_RAIN").playTrack(rainFile, 0.50, masterVolume);
        } else {
            channels.get("WEATHER_RAIN").stopWithFade();
        }

        // 3. Weather Wind Sound
        boolean isTornado = wUpper.contains("TEMPEST") || wUpper.contains("TORNADO") || wUpper.contains("TORNADE");
        boolean isWindy = manager.isStormType(wUpper) || windSpeedMps > 8.0 || isTornado;

        if (weatherEnabled && isTornado) {
            File tornadoFile = manager.findSoundFile("April_19,_2011_-_Tornado_at_Girard_Illinois.ogg");
            if (tornadoFile == null) tornadoFile = manager.findSoundFile("strong_howling_wind.mp3");
            channels.get("WEATHER_WIND").playTrack(tornadoFile, 0.18, masterVolume);
        } else if (weatherEnabled && isWindy) {
            File windFile = (windSpeedMps > 15.0) ? manager.findSoundFile("strong_howling_wind.mp3") : manager.findSoundFile("mixkit-wind-blowing-ambience-2658.wav");
            if (windFile == null) windFile = manager.findSoundFile("Bourne_woods_windy_2020-05-05_0753.mp3");
            double windVolScale = Math.min(1.0, windSpeedMps / 25.0);
            channels.get("WEATHER_WIND").playTrack(windFile, 0.05 * windVolScale, masterVolume);
        } else {
            channels.get("WEATHER_WIND").stopWithFade();
        }

        // 4. Weather Fire Sound
        if (weatherEnabled && (wUpper.contains("FIRE") || wUpper.contains("INCENDIE") || wUpper.contains("FEU"))) {
            File fireFile = manager.findSoundFile("AMB_Nature Pack Vol 1_ Fire & Elemental_Large Bonfire.mp3");
            if (fireFile == null) fireFile = manager.findSoundFile("AMB_Nature Pack Vol 1_ Fire & Elemental_Campfire Crackling.mp3");
            channels.get("WEATHER_FIRE").playTrack(fireFile, 0.50, masterVolume);
        } else {
            channels.get("WEATHER_FIRE").stopWithFade();
        }

        // 5. Biome Ambient Sound Loop
        if (ambientEnabled && !heavyRain) {
            File biomeFile = null;
            if (bUpper.contains("DESERT") || bUpper.contains("DÉSERT") || bUpper.contains("ARID")) {
                biomeFile = manager.findSoundFile("AMBRurl_Nocturnal insects 4 (ID 1470)_BigSoundBank.com.mp3");
                if (biomeFile == null) biomeFile = manager.findSoundFile("desert_fauna_ambient.mp3");
            } else if (bUpper.contains("JUNGLE") || bUpper.contains("TROPICAL")) {
                biomeFile = manager.findSoundFile("mixkit-birds-in-the-jungle-2434.wav");
                if (biomeFile == null) biomeFile = manager.findSoundFile("Sound_of_the_jungle_in_Thailand.flac");
            } else if (bUpper.contains("SWAMP") || bUpper.contains("MARAIS")) {
                biomeFile = manager.findSoundFile("AMB_Nature Pack Vol 1_Water Enviroments_Swamp.mp3");
                if (biomeFile == null) biomeFile = manager.findSoundFile("Nature_sounds_ambience_in_a_Dordogne_pond.ogg");
            } else if (isDay) {
                biomeFile = manager.findSoundFile("AMB_Nature Pack Vol 1_Forest Enviroments_Forest Day.mp3");
                if (biomeFile == null) biomeFile = manager.findSoundFile("AMBForst_Forest (ID 0100)_BigSoundBank.com.mp3");
                if (biomeFile == null) biomeFile = manager.findSoundFile("mixkit-morning-birds-2472.wav");
            } else {
                biomeFile = manager.findSoundFile("ElevenLabs_Ambiance_nocturne_animée,_grillons_qui_chantent_et_lucioles_qui_brillent_dans_le_noir.mp3");
                if (biomeFile == null) biomeFile = manager.findSoundFile("AMB_Nature Pack Vol 1_Forest Enviroments_Forest Night.mp3");
                if (biomeFile == null) biomeFile = manager.findSoundFile("AMBRurl_Nocturnal insects 4 (ID 1470)_BigSoundBank.com.mp3");
            }
            channels.get("BIOME").playTrack(biomeFile, 0.70, masterVolume);
        } else {
            channels.get("BIOME").stopWithFade();
        }

        // 6. Insect Colony Activity & Digging Sound
        double insectDistanceAtten = 0.0;
        if (cameraDepth > 0.4) {
            insectDistanceAtten = 1.0;
        } else if (cameraZoom > 4.5) {
            double zoomProximity = Math.max(0.0, Math.min(1.0, (cameraZoom - 4.5) / 7.5));
            insectDistanceAtten = Math.pow(zoomProximity, 1.8);
        } else {
            insectDistanceAtten = 0.0;
        }

        if (insectEnabled && populationCount > 5 && insectDistanceAtten > 0.01) {
            double popVolScale = Math.min(1.0, Math.log10(populationCount) / 3.5);
            File insectFile = null;
            if (cameraDepth > 0.4 || cameraZoom > 10.0) {
                insectFile = manager.findSoundFile("nest_sounds.mp3");
                if (insectFile == null) insectFile = manager.findSoundFile("anthill_nest_sounds.mp3");
                if (insectFile == null) insectFile = manager.findSoundFile("sand_soil_digging.mp3");
                if (insectFile == null) insectFile = manager.findSoundFile("termites-and-ants-sound.mp3");
            } else {
                insectFile = manager.findSoundFile("colony_activity.mp3");
                if (insectFile == null) insectFile = manager.findSoundFile("ant_colony_activity.mp3");
                if (insectFile == null) insectFile = manager.findSoundFile("freesound_community-ants-23656.mp3");
                if (insectFile == null) insectFile = manager.findSoundFile("antscolony.mp3");
            }
            channels.get("INSECT").playTrack(insectFile, 0.28 * popVolScale * insectDistanceAtten, masterVolume);
        } else {
            channels.get("INSECT").stopWithFade();
        }

        for (AudioChannelPlayer channel : channels.values()) {
            channel.updateFadeTick(deltaSec, masterVolume);
        }
    }
}
