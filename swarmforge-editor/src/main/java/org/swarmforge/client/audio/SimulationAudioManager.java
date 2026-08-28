/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.audio;

import javax.sound.sampled.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Audio Synthesizer & Sound Bank Manager for SwarmForge Simulation.
 * Loads and mixes real bio-acoustic environment audio files (river water stream, rain, wind, birds, insects)
 * from the sound bank (swarmforge-web/public/sounds/) with soft real-time audio synthesis.
 * Zero OpenAL lockups and zero rhythmic audio artifacts.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SimulationAudioManager {

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

    private double timeOfDayHours = 12.0;

    private SourceDataLine line;
    private static final int SAMPLE_RATE = 22050;

    // Active looping clips for sound bank audio
    private final Map<String, Clip> activeClips = new HashMap<>();

    private SimulationAudioManager() {
        startAudioEngine();
    }

    private synchronized void startAudioEngine() {
        if (running.get()) return;
        running.set(true);

        audioExecutor.submit(() -> {
            try {
                AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

                if (!AudioSystem.isLineSupported(info)) {
                    System.err.println("[Audio] JavaSound line not supported.");
                    return;
                }

                line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format, 4410); // ~200ms buffer
                line.start();

                byte[] buffer = new byte[1024];

                while (running.get()) {
                    // Manage real sound bank clips based on simulation state
                    updateSoundBankClips();

                    // Sleep audio synthesis loop while real sound clips are playing
                    java.util.Arrays.fill(buffer, (byte) 0);
                    line.write(buffer, 0, buffer.length);
                    try { Thread.sleep(25); } catch (InterruptedException ignored) {}
                }

                line.drain();
                line.close();
            } catch (Exception e) {
                System.err.println("[Audio] Error in audio engine thread: " + e.getMessage());
            }
        });
    }

    // ── Speed-of-Sound Delayed Lightning Thunder Strike ─────────────────────────

    public void triggerLightningThunderStrike(double distanceMeters) {
        if (!weatherEnabled || masterVolume <= 0.001) return;
        // Speed of sound: ~343 m/s => delay in milliseconds
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
                    "THUN_Thunder 3 (ID 3114)_BigSoundBank.com.mp3"
                };
                String selectedStrike = thunderFiles[new Random().nextInt(thunderFiles.length)];
                playOneShotSound(selectedStrike, masterVolume * distAttenuation * 0.90);
            } catch (InterruptedException ignored) {}
        });
    }

    private void playOneShotSound(String filename, double vol) {
        File soundFile = findSoundFile(filename);
        if (soundFile == null) return;
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            setClipGain(clip, vol);
            clip.start();
        } catch (Exception ignored) {}
    }

    // ── Sound Bank Clip Management ────────────────────────────────────────────────

    private void updateSoundBankClips() {
        if (!simRunning || masterVolume <= 0.001) {
            stopAllClips();
            return;
        }

        String wUpper = currentWeather != null ? currentWeather.toUpperCase() : "CLEAR";
        String bUpper = currentBiome != null ? currentBiome.toUpperCase() : "FOREST";
        double hour = timeOfDayHours;
        boolean isDay = (hour >= 6.0 && hour < 18.0);

        // 1. River Water Sound Loop
        if (riverEnabled && hasRiver) {
            ensureClipPlaying("RIVER", "mixkit-river-water-flow-and-surroundings-2452.wav", masterVolume * 0.65);
        } else {
            stopClip("RIVER");
        }

        // 2. Weather Rain / Hail Sound Loop
        boolean isHail = wUpper.contains("HAIL") || wUpper.contains("GRÊLE");
        boolean isRain = isRainType(wUpper) || rainRateMmHr > 0.1;

        if (weatherEnabled && isHail) {
            ensureClipPlaying("WEATHER_RAIN", "saturn-3-music-strong-hail-falling-against-the-window-116182.mp3", masterVolume * 0.70);
        } else if (weatherEnabled && isRain) {
            String rainFile = (wUpper.contains("HEAVY") || wUpper.contains("FORTE") || wUpper.contains("THUNDER") || wUpper.contains("ORAGE") || rainRateMmHr > 10.0)
                    ? "mixkit-heavy-rain-2403.wav"
                    : "mixkit-light-rain-loop-2393.wav";
            ensureClipPlaying("WEATHER_RAIN", rainFile, masterVolume * 0.75);
        } else {
            stopClip("WEATHER_RAIN");
        }

        // 3. Weather Wind Sound Loop
        boolean isWindy = isStormType(wUpper) || windSpeedMps > 12.0;
        if (weatherEnabled && isWindy) {
            String windFile = (windSpeedMps > 20.0 || wUpper.contains("TEMPEST") || wUpper.contains("BLIZZARD"))
                    ? "strong_howling_wind.mp3"
                    : "mixkit-wind-blowing-ambience-2658.wav";
            ensureClipPlaying("WEATHER_WIND", windFile, masterVolume * 0.60);
        } else {
            stopClip("WEATHER_WIND");
        }

        // 4. Biome Ambient Sound Loop (Day Birds vs Night Crickets vs Desert Wind)
        if (ambientEnabled && !isRain && !isWindy) {
            if (bUpper.contains("DESERT") || bUpper.contains("DÉSERT") || bUpper.contains("ARID")) {
                ensureClipPlaying("BIOME", "AMB_Nature Pack Vol 1_Weather_Desert Wind.mp3", masterVolume * 0.50);
            } else if (isDay) {
                ensureClipPlaying("BIOME", "AMB_Nature Pack Vol 1_Forest Enviroments_Forest Day.mp3", masterVolume * 0.50);
            } else {
                ensureClipPlaying("BIOME", "ElevenLabs_Ambiance_nocturne_animée,_grillons_qui_chantent_et_lucioles_qui_brillent_dans_le_noir.mp3", masterVolume * 0.55);
            }
        } else {
            stopClip("BIOME");
        }

        // 5. Insect Colony Activity Sound Loop
        if (insectEnabled && populationCount > 10) {
            double popVolScale = Math.min(1.0, Math.log10(populationCount) / 3.5);
            ensureClipPlaying("INSECT", "ant_colony_activity.mp3", masterVolume * 0.45 * popVolScale);
        } else {
            stopClip("INSECT");
        }
    }

    private void ensureClipPlaying(String key, String filename, double vol) {
        Clip clip = activeClips.get(key);
        if (clip != null && clip.isRunning()) {
            setClipGain(clip, vol);
            return;
        }

        File soundFile = findSoundFile(filename);
        if (soundFile == null) {
            return;
        }

        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundFile);
            Clip newClip = AudioSystem.getClip();
            newClip.open(ais);
            setClipGain(newClip, vol);
            newClip.loop(Clip.LOOP_CONTINUOUSLY);
            newClip.start();

            if (clip != null) {
                clip.stop();
                clip.close();
            }
            activeClips.put(key, newClip);
        } catch (Exception ignored) {}
    }

    private void stopClip(String key) {
        Clip clip = activeClips.remove(key);
        if (clip != null) {
            try {
                clip.stop();
                clip.close();
            } catch (Exception ignored) {}
        }
    }

    private void stopAllClips() {
        for (Clip clip : activeClips.values()) {
            if (clip != null) {
                try {
                    clip.stop();
                    clip.close();
                } catch (Exception ignored) {}
            }
        }
        activeClips.clear();
    }

    private static File findSoundFile(String filename) {
        String[] paths = new String[]{
            "swarmforge-web/public/sounds/" + filename,
            "../swarmforge-web/public/sounds/" + filename,
            "public/sounds/" + filename,
            "sounds/" + filename,
            "src/main/resources/sounds/" + filename
        };
        for (String p : paths) {
            File f = new File(p);
            if (f.exists() && f.isFile()) {
                return f;
            }
        }
        return null;
    }

    private void setClipGain(Clip clip, double vol) {
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (Math.log(Math.max(0.0001, vol)) / Math.log(10.0) * 20.0);
                gainControl.setValue(Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB)));
            }
        } catch (Exception ignored) {}
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────────

    public void setAmbientEnabled(boolean enabled) {
        this.ambientEnabled = enabled;
        if (!enabled) stopClip("BIOME");
    }

    public boolean isAmbientEnabled() {
        return ambientEnabled;
    }

    public void setRiverEnabled(boolean enabled) {
        this.riverEnabled = enabled;
        if (!enabled) stopClip("RIVER");
    }

    public boolean isRiverEnabled() {
        return riverEnabled;
    }

    public void setWeatherEnabled(boolean enabled) {
        this.weatherEnabled = enabled;
        if (!enabled) {
            stopClip("WEATHER_RAIN");
            stopClip("WEATHER_WIND");
        }
    }

    public boolean isWeatherEnabled() {
        return weatherEnabled;
    }

    public void setInsectEnabled(boolean enabled) {
        this.insectEnabled = enabled;
        if (!enabled) stopClip("INSECT");
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

    private boolean isRainType(String w) {
        if (w == null) return false;
        return w.contains("RAIN") || w.contains("PLUIE") || w.contains("LLUVIA") || w.contains("REGEN") || w.contains("雨") ||
               w.contains("STORM") || w.contains("ORAGE") || w.contains("AVERSE") || w.contains("SCHAUER");
    }

    private boolean isStormType(String w) {
        if (w == null) return false;
        return w.contains("WIND") || w.contains("VENT") || w.contains("VIENTO") || w.contains("WINDIG") || w.contains("风") ||
               w.contains("TEMPÊTE") || w.contains("TORMENTA") || w.contains("UNWETTER") || w.contains("暴风");
    }

    public void setHasRiver(boolean hasRiver) { this.hasRiver = hasRiver; }
    public void setPopulationCount(int populationCount) { this.populationCount = populationCount; }
    public void setHasTrees(boolean hasTrees) { this.hasTrees = hasTrees; }
    public void setCameraZoom(double zoom) { this.cameraZoom = zoom; }
    public void setSeason(String season) { if (season != null) this.currentSeason = season; }
    public void setTimeOfDayHours(double hour) { this.timeOfDayHours = hour; }

    public void setWindAndPrecipitation(double windSpeed, double rainRate) {
        this.windSpeedMps = windSpeed;
        this.rainRateMmHr = rainRate;
    }

    public void updateState(String biome, String weather, double cameraDepth, boolean isSimRunning) {
        if (biome != null) this.currentBiome = biome;
        if (weather != null) this.currentWeather = weather;
        this.simRunning = isSimRunning;
        this.cameraZoom = cameraDepth;
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

    private boolean recordingAudio = false;
    private long audioRecordingStartMs = 0;

    public synchronized void startAudioRecording() {
        this.recordingAudio = true;
        this.audioRecordingStartMs = System.currentTimeMillis();
    }

    public synchronized byte[] stopAudioRecording() {
        if (!recordingAudio) {
            return new byte[0];
        }
        this.recordingAudio = false;
        long elapsedMs = System.currentTimeMillis() - audioRecordingStartMs;
        double durationSec = elapsedMs / 1000.0;
        return generateAmbientPcmAudio(durationSec);
    }

    public byte[] generateAmbientPcmAudio(double durationSec) {
        if (durationSec <= 0.1) return new byte[0];
        int sampleRate = 22050;
        int totalSamples = (int) (sampleRate * durationSec);
        byte[] pcm = new byte[totalSamples * 2]; // 16-bit mono PCM (2 bytes per sample)

        boolean river = riverEnabled && hasRiver;
        boolean rain = weatherEnabled && (isRainType(currentWeather) || rainRateMmHr > 0.1);
        boolean wind = weatherEnabled && (isStormType(currentWeather) || windSpeedMps > 12.0);
        boolean biome = ambientEnabled;
        boolean insects = insectEnabled && populationCount > 10;
        double vol = masterVolume;

        if (vol <= 0.001) return pcm;

        double[] mixedBuffer = new double[totalSamples];

        if (river) {
            mixSoundFile("mixkit-river-water-flow-and-surroundings-2452.wav", mixedBuffer, vol * 0.65);
        }
        if (rain) {
            String rainFile = (rainRateMmHr > 10.0 || (currentWeather != null && currentWeather.toUpperCase().contains("HEAVY")))
                ? "mixkit-heavy-rain-2403.wav"
                : "mixkit-light-rain-loop-2393.wav";
            mixSoundFile(rainFile, mixedBuffer, vol * 0.70);
        }
        if (wind) {
            mixSoundFile("mixkit-wind-blowing-ambience-2658.wav", mixedBuffer, vol * 0.60);
        }
        if (biome && !rain && !wind) {
            mixSoundFile("mixkit-birds-chirping-near-the-river-2473.wav", mixedBuffer, vol * 0.50);
        }
        if (insects) {
            double popVolScale = Math.min(1.0, Math.log10(populationCount) / 3.5);
            mixSoundFile("mixkit-night-forest-with-insects-2414.wav", mixedBuffer, vol * 0.45 * popVolScale);
        }

        boolean hasSampleData = false;
        for (int i = 0; i < Math.min(100, totalSamples); i++) {
            if (Math.abs(mixedBuffer[i]) > 0.0001) {
                hasSampleData = true;
                break;
            }
        }

        if (!hasSampleData) {
            Random rand = new Random(12345);
            double pink0 = 0, pink1 = 0, pink2 = 0;
            for (int i = 0; i < totalSamples; i++) {
                double white = (rand.nextDouble() * 2.0 - 1.0);
                pink0 = 0.99886 * pink0 + white * 0.0555179;
                pink1 = 0.99332 * pink1 + white * 0.0750759;
                pink2 = 0.96900 * pink2 + white * 0.1538520;
                double breeze = (pink0 + pink1 + pink2) * 0.1 * vol;
                double t = (double) i / sampleRate;
                double chirp = Math.sin(2.0 * Math.PI * 3200.0 * t) * Math.sin(2.0 * Math.PI * 4.0 * t) * 0.02 * vol;
                mixedBuffer[i] = breeze + chirp;
            }
        }

        for (int i = 0; i < totalSamples; i++) {
            double val = Math.max(-1.0, Math.min(1.0, mixedBuffer[i]));
            short sample = (short) (val * 32767.0);
            pcm[2 * i] = (byte) (sample & 0xFF);
            pcm[2 * i + 1] = (byte) ((sample >> 8) & 0xFF);
        }

        return pcm;
    }

    private void mixSoundFile(String filename, double[] targetBuffer, double volume) {
        File soundFile = findSoundFile(filename);
        if (soundFile == null || !soundFile.exists()) return;
        try {
            AudioInputStream origAis = AudioSystem.getAudioInputStream(soundFile);
            AudioFormat origFormat = origAis.getFormat();
            AudioFormat pcmFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                22050,
                16,
                1,
                2,
                22050,
                false
            );
            AudioInputStream ais = AudioSystem.isConversionSupported(pcmFormat, origFormat)
                ? AudioSystem.getAudioInputStream(pcmFormat, origAis)
                : origAis;

            byte[] rawBytes = ais.readAllBytes();
            ais.close();
            origAis.close();

            int numSourceSamples = rawBytes.length / 2;
            if (numSourceSamples == 0) return;

            for (int i = 0; i < targetBuffer.length; i++) {
                int srcIndex = i % numSourceSamples;
                short s = (short) ((rawBytes[2 * srcIndex] & 0xFF) | (rawBytes[2 * srcIndex + 1] << 8));
                double sampleNorm = (s / 32768.0) * volume;
                targetBuffer[i] += sampleNorm;
            }
        } catch (Exception ignored) {}
    }

    public void stop() {
        running.set(false);
        stopAllClips();
        if (line != null && line.isOpen()) {
            line.close();
        }
    }
}
