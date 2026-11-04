/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * High-Fidelity Audio Synthesizer & Sound Bank Manager for SwarmForge.
 * Features smooth cross-fading (fade-in, sustain, fade-out), multi-channel audio mixing,
 * and full access to the 90+ bio-acoustic sound bank covering all biomes, climates, weather, rivers, and insect activities.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SimulationAudioManager {

    private static final Logger LOG = LoggerFactory.getLogger(SimulationAudioManager.class);
    private static final SimulationAudioManager INSTANCE = new SimulationAudioManager();

    public static SimulationAudioManager getInstance() {
        return INSTANCE;
    }

    private final ExecutorService audioExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "SwarmForge-Audio-Synthesizer");
        t.setDaemon(true);
        return t;
    });

    private final AtomicBoolean running = new AtomicBoolean(false);
    private boolean ambientEnabled = true;
    private boolean riverEnabled = true;
    private boolean weatherEnabled = true;
    private boolean insectEnabled = true;
    private double masterVolume = 0.7; // 0.0 - 1.0

    private String currentBiome = "Forest";
    private String currentWeather = "Clear";
    private String currentSeason = "SUMMER";
    private boolean hasRiver = false;
    private boolean hasTrees = true;
    private double cameraZoom = 7.5;
    private int populationCount = 100;
    private double windSpeedMps = 5.0;
    private double rainRateMmHr = 0.0;
    private double cameraDepth = 0.0; // 0.0 = surface, >0.5 = subterranean
    private boolean simRunning = false;

    private double latitude = 48.8;
    private int dayOfYear = 180;
    private double ambientLightLevel = 1.0;
    private double timeOfDayHours = 12.0;

    private SourceDataLine line;
    private static final int SAMPLE_RATE = 22050;

    // Sound Bank file index mapping filename/basename -> File
    private final Map<String, File> soundBankIndex = new ConcurrentHashMap<>();

    // Multi-Channel Audio Players with Fading Engine
    private final Map<String, AudioChannelPlayer> channels = new ConcurrentHashMap<>();

    // Audio Recorder & PCM Synthesizer
    private final SimulationAudioRecorder audioRecorder;

    private SimulationAudioManager() {
        scanSoundBank();
        initChannels();
        this.audioRecorder = new SimulationAudioRecorder(this::findSoundFile);
        startAudioEngine();
    }

    private void initChannels() {
        channels.put("BIOME", new AudioChannelPlayer("BIOME"));
        channels.put("RIVER", new AudioChannelPlayer("RIVER"));
        channels.put("WEATHER_RAIN", new AudioChannelPlayer("WEATHER_RAIN"));
        channels.put("WEATHER_WIND", new AudioChannelPlayer("WEATHER_WIND"));
        channels.put("WEATHER_FIRE", new AudioChannelPlayer("WEATHER_FIRE"));
        channels.put("INSECT", new AudioChannelPlayer("INSECT"));
    }

    /**
     * Scans known sound asset directories and builds a registry of all sound bank files (~90+ sounds).
     */
    private void scanSoundBank() {
        String[] searchPaths = new String[]{
            "swarmforge-web/public/sounds",
            "../swarmforge-web/public/sounds",
            "public/sounds",
            "sounds",
            "src/main/resources/sounds",
            "swarmforge-editor/src/main/resources/sounds"
        };

        for (String path : searchPaths) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isFile()) {
                            String name = f.getName();
                            String lowerName = name.toLowerCase();
                            soundBankIndex.putIfAbsent(lowerName, f);
                            int dot = lowerName.lastIndexOf('.');
                            if (dot > 0) {
                                soundBankIndex.putIfAbsent(lowerName.substring(0, dot), f);
                            }
                        }
                    }
                }
            }
        }
    }

    private synchronized void startAudioEngine() {
        if (running.get()) return;
        running.set(true);

        audioExecutor.submit(() -> {
            try {
                AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

                if (AudioSystem.isLineSupported(info)) {
                    line = (SourceDataLine) AudioSystem.getLine(info);
                    line.open(format, 4410); // ~200ms buffer
                    line.start();
                }

                byte[] buffer = new byte[1024];

                while (running.get()) {
                    updateSoundBankClips();

                    if (line != null && line.isOpen()) {
                        Arrays.fill(buffer, (byte) 0);
                        line.write(buffer, 0, buffer.length);
                    }
                    try { Thread.sleep(40); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
                }

                if (line != null) {
                    line.drain();
                    line.close();
                }
            } catch (Exception e) {
                LOG.debug("[Audio] Audio engine loop note: {}", e.getMessage());
            }
        });
    }

    // ── Speed-of-Sound Delayed Lightning Thunder Strike ─────────────────────────

    public void triggerLightningThunderStrike(double distanceMeters) {
        if (!weatherEnabled || masterVolume <= 0.001) return;
        long delayMs = Math.max(0, (long) ((distanceMeters / 343.0) * 1000.0));

        audioExecutor.submit(() -> {
            try {
                if (delayMs > 0) {
                    Thread.sleep(delayMs);
                }
                double distAttenuation = 1.0 / (1.0 + Math.pow(distanceMeters / 300.0, 2.0));
                String[] thunderFiles = new String[]{
                    "mixkit-thunder-strike-in-storm-2405.wav",
                    "THUN_Thunder 2 (ID 3113)_BigSoundBank.com.mp3",
                    "THUN_Thunder 3 (ID 3114)_BigSoundBank.com.mp3",
                    "mixkit-thunder-rumble-during-a-storm-2395.wav"
                };
                String selectedStrike = thunderFiles[new Random().nextInt(thunderFiles.length)];
                playOneShotSound(selectedStrike, masterVolume * distAttenuation * 0.90);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private void playOneShotSound(String filename, double vol) {
        File soundFile = findSoundFile(filename);
        if (soundFile == null) return;
        try {
            final String uriStr = soundFile.toURI().toString();
            AudioChannelPlayer.runOnFx(() -> {
                try {
                    Media media = new Media(uriStr);
                    MediaPlayer player = new MediaPlayer(media);
                    player.setVolume(vol);
                    player.play();
                } catch (Exception e) {
                    if (soundFile.getName().toLowerCase().endsWith(".wav")) {
                        try {
                            AudioInputStream ais = AudioSystem.getAudioInputStream(soundFile);
                            Clip clip = AudioSystem.getClip();
                            clip.open(ais);
                            AudioChannelPlayer.setClipGain(clip, vol);
                            clip.start();
                        } catch (Exception ignored) {}
                    }
                }
            });
        } catch (Exception ignored) {}
    }

    // ── Sound Bank Clip Selection & Channel Fading Management ───────────────────

    private void updateSoundBankClips() {
        SimulationAudioTrackSelector.updateSoundBankClips(
                this,
                channels,
                simRunning,
                masterVolume,
                currentWeather,
                currentBiome,
                riverEnabled,
                weatherEnabled,
                ambientEnabled,
                insectEnabled,
                rainRateMmHr,
                windSpeedMps,
                cameraZoom,
                cameraDepth,
                populationCount,
                latitude,
                dayOfYear,
                timeOfDayHours,
                ambientLightLevel
        );
    }

    private void stopAllClips() {
        for (AudioChannelPlayer channel : channels.values()) {
            channel.stopImmediately();
        }
    }

    File findSoundFile(String filename) {
        if (filename == null) return null;
        String lower = filename.toLowerCase();

        File cached = soundBankIndex.get(lower);
        if (cached != null && cached.exists() && cached.isFile()) return cached;

        int dot = lower.lastIndexOf('.');
        if (dot > 0) {
            cached = soundBankIndex.get(lower.substring(0, dot));
            if (cached != null && cached.exists() && cached.isFile()) return cached;
        }

        String[] paths = new String[]{
            "swarmforge-web/public/sounds/" + filename,
            "../swarmforge-web/public/sounds/" + filename,
            "public/sounds/" + filename,
            "sounds/" + filename,
            "src/main/resources/sounds/" + filename,
            "swarmforge-editor/src/main/resources/sounds/" + filename
        };
        for (String p : paths) {
            File f = new File(p);
            if (f.exists() && f.isFile()) {
                soundBankIndex.put(lower, f);
                return f;
            }
        }
        return null;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────────

    public void setAmbientEnabled(boolean enabled) {
        this.ambientEnabled = enabled;
        if (!enabled && channels.containsKey("BIOME")) channels.get("BIOME").stopWithFade();
    }

    public boolean isAmbientEnabled() {
        return ambientEnabled;
    }

    public void setRiverEnabled(boolean enabled) {
        this.riverEnabled = enabled;
        if (!enabled && channels.containsKey("RIVER")) channels.get("RIVER").stopWithFade();
    }

    public boolean isRiverEnabled() {
        return riverEnabled;
    }

    public void setWeatherEnabled(boolean enabled) {
        this.weatherEnabled = enabled;
        if (!enabled) {
            if (channels.containsKey("WEATHER_RAIN")) channels.get("WEATHER_RAIN").stopWithFade();
            if (channels.containsKey("WEATHER_WIND")) channels.get("WEATHER_WIND").stopWithFade();
            if (channels.containsKey("WEATHER_FIRE")) channels.get("WEATHER_FIRE").stopWithFade();
        }
    }

    public boolean isWeatherEnabled() {
        return weatherEnabled;
    }

    public void setInsectEnabled(boolean enabled) {
        this.insectEnabled = enabled;
        if (!enabled && channels.containsKey("INSECT")) channels.get("INSECT").stopWithFade();
    }

    public boolean isInsectEnabled() {
        return insectEnabled;
    }

    public void setMasterVolume(double volume) {
        this.masterVolume = Math.max(0.0, Math.min(1.0, volume));
        if (masterVolume <= 0.001) stopAllClips();
    }

    public double getMasterVolume() {
        return masterVolume;
    }

    boolean isRainType(String w) {
        if (w == null) return false;
        return w.contains("RAIN") || w.contains("PLUIE") || w.contains("LLUVIA") || w.contains("REGEN") || w.contains("雨") ||
               w.contains("STORM") || w.contains("ORAGE") || w.contains("AVERSE") || w.contains("SCHAUER");
    }

    boolean isStormType(String w) {
        if (w == null) return false;
        return w.contains("WIND") || w.contains("VENT") || w.contains("VIENTO") || w.contains("WINDIG") || w.contains("风") ||
               w.contains("TEMPÊTE") || w.contains("TORMENTA") || w.contains("UNWETTER") || w.contains("暴风");
    }

    public void setHasRiver(boolean hasRiver) { this.hasRiver = hasRiver; }
    public void setPopulationCount(int populationCount) { this.populationCount = populationCount; }
    public void setHasTrees(boolean hasTrees) { this.hasTrees = hasTrees; }
    public void setCameraZoom(double zoom) { this.cameraZoom = zoom; }
    public double getCameraZoom() { return cameraZoom; }
    public void setCameraDepth(double depth) { this.cameraDepth = depth; }
    public double getCameraDepth() { return cameraDepth; }
    public void setSeason(String season) { if (season != null) this.currentSeason = season; }
    public void setTimeOfDayHours(double hour) { this.timeOfDayHours = hour; }

    public void setWindAndPrecipitation(double windSpeed, double rainRate) {
        this.windSpeedMps = windSpeed;
        this.rainRateMmHr = rainRate;
    }

    public void setEnvironmentPhysics(double latitude, int dayOfYear, double timeOfDayHours, double ambientLightLevel) {
        this.latitude = latitude;
        this.dayOfYear = dayOfYear;
        this.timeOfDayHours = timeOfDayHours;
        this.ambientLightLevel = ambientLightLevel;
    }

    public void updateState(String biome, String weather, double cameraDepth, boolean isSimRunning) {
        if (biome != null) this.currentBiome = biome;
        if (weather != null) this.currentWeather = weather;
        this.simRunning = isSimRunning;
        this.cameraDepth = cameraDepth;
    }

    public void updateState(String biome, String weather, String season, boolean hasRiver, int populationCount, double cameraZoom, boolean isSimRunning) {
        if (biome != null) this.currentBiome = biome;
        if (weather != null) this.currentWeather = weather;
        if (season != null) this.currentSeason = season;
        this.hasRiver = hasRiver;
        this.populationCount = populationCount;
        this.cameraZoom = cameraZoom;
        this.simRunning = isSimRunning;
    }

    // ── Audio Recording for Video Export ─────────────────────────────────────────

    public synchronized void startAudioRecording() {
        audioRecorder.startAudioRecording();
    }

    public synchronized byte[] stopAudioRecording() {
        return audioRecorder.stopAudioRecording(
                riverEnabled,
                hasRiver,
                weatherEnabled,
                currentWeather,
                rainRateMmHr,
                windSpeedMps,
                ambientEnabled,
                insectEnabled,
                populationCount,
                masterVolume
        );
    }

    public byte[] generateAmbientPcmAudio(double durationSec) {
        return audioRecorder.generateAmbientPcmAudio(
                durationSec,
                riverEnabled,
                hasRiver,
                weatherEnabled,
                currentWeather,
                rainRateMmHr,
                windSpeedMps,
                ambientEnabled,
                insectEnabled,
                populationCount,
                masterVolume
        );
    }

    public void stop() {
        running.set(false);
        stopAllClips();
        if (line != null && line.isOpen()) {
            try {
                line.close();
            } catch (Exception ignored) {}
        }
        try {
            audioExecutor.shutdownNow();
        } catch (Exception ignored) {}
    }
}
