/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.audio;

import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

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
    private final Map<String, ChannelPlayer> channels = new ConcurrentHashMap<>();

    private SimulationAudioManager() {
        scanSoundBank();
        initChannels();
        startAudioEngine();
    }

    private void initChannels() {
        channels.put("BIOME", new ChannelPlayer("BIOME"));
        channels.put("RIVER", new ChannelPlayer("RIVER"));
        channels.put("WEATHER_RAIN", new ChannelPlayer("WEATHER_RAIN"));
        channels.put("WEATHER_WIND", new ChannelPlayer("WEATHER_WIND"));
        channels.put("WEATHER_FIRE", new ChannelPlayer("WEATHER_FIRE"));
        channels.put("INSECT", new ChannelPlayer("INSECT"));
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
                            // Also store without extension for convenient key lookup
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
                    // Update state-based sound bank selections and channel fading steps
                    updateSoundBankClips();

                    if (line != null && line.isOpen()) {
                        Arrays.fill(buffer, (byte) 0);
                        line.write(buffer, 0, buffer.length);
                    }
                    try { Thread.sleep(40); } catch (InterruptedException ignored) {}
                }

                if (line != null) {
                    line.drain();
                    line.close();
                }
            } catch (Exception e) {
                System.err.println("[Audio] Audio engine loop note: " + e.getMessage());
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
            } catch (InterruptedException ignored) {}
        });
    }

    private void playOneShotSound(String filename, double vol) {
        File soundFile = findSoundFile(filename);
        if (soundFile == null) return;
        try {
            final String uriStr = soundFile.toURI().toString();
            runOnFx(() -> {
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
                            setClipGain(clip, vol);
                            clip.start();
                        } catch (Exception ignored) {}
                    }
                }
            });
        } catch (Exception ignored) {}
    }

    // ── Sound Bank Clip Selection & Channel Fading Management ───────────────────

    private void updateSoundBankClips() {
        double deltaSec = 0.04; // ~40ms per tick

        if (!simRunning || masterVolume <= 0.001) {
            for (ChannelPlayer channel : channels.values()) {
                channel.stopWithFade();
                channel.updateFadeTick(deltaSec, masterVolume);
            }
            return;
        }

        String wUpper = currentWeather != null ? currentWeather.toUpperCase() : "CLEAR";
        String bUpper = currentBiome != null ? currentBiome.toUpperCase() : "FOREST";

        // Solar astronomical elevation calculation
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
        if (riverEnabled && hasRiver) {
            File riverFile = findSoundFile("mixkit-river-water-flow-and-surroundings-2452.wav");
            if (riverFile == null) riverFile = findSoundFile("WATRFlow_Small stream 4 (ID 1354)_BigSoundBank.com.mp3");
            if (riverFile == null) riverFile = findSoundFile("AMB_Nature Pack Vol 1_Water Enviroments_Mountain Stream.mp3");
            channels.get("RIVER").playTrack(riverFile, 0.65, masterVolume);
        } else {
            channels.get("RIVER").stopWithFade();
        }

        // 2. Weather Rain / Hail Sound
        boolean isHail = wUpper.contains("HAIL") || wUpper.contains("GRÊLE");
        boolean isRain = isRainType(wUpper) || rainRateMmHr > 0.1;

        if (weatherEnabled && isHail) {
            File hailFile = findSoundFile("saturn-3-music-strong-hail-falling-against-the-window-116182.mp3");
            if (hailFile == null) hailFile = findSoundFile("freesound_community-hail-74904.mp3");
            channels.get("WEATHER_RAIN").playTrack(hailFile, 0.70, masterVolume);
        } else if (weatherEnabled && isRain) {
            boolean heavy = wUpper.contains("HEAVY") || wUpper.contains("FORTE") || wUpper.contains("THUNDER") || wUpper.contains("ORAGE") || rainRateMmHr > 10.0;
            File rainFile = heavy ? findSoundFile("mixkit-heavy-rain-2403.wav") : findSoundFile("mixkit-light-rain-loop-2393.wav");
            if (rainFile == null) rainFile = findSoundFile("Sound_of_rain.ogg");
            channels.get("WEATHER_RAIN").playTrack(rainFile, 0.75, masterVolume);
        } else {
            channels.get("WEATHER_RAIN").stopWithFade();
        }

        // 3. Weather Wind Sound
        boolean isTornado = wUpper.contains("TEMPEST") || wUpper.contains("TORNADO") || wUpper.contains("TORNADE");
        boolean isWindy = isStormType(wUpper) || windSpeedMps > 8.0 || isTornado;

        if (weatherEnabled && isTornado) {
            File tornadoFile = findSoundFile("April_19,_2011_-_Tornado_at_Girard_Illinois.ogg");
            if (tornadoFile == null) tornadoFile = findSoundFile("strong_howling_wind.mp3");
            channels.get("WEATHER_WIND").playTrack(tornadoFile, 0.75, masterVolume);
        } else if (weatherEnabled && isWindy) {
            File windFile = (windSpeedMps > 15.0) ? findSoundFile("strong_howling_wind.mp3") : findSoundFile("mixkit-wind-blowing-ambience-2658.wav");
            if (windFile == null) windFile = findSoundFile("Bourne_woods_windy_2020-05-05_0753.mp3");
            channels.get("WEATHER_WIND").playTrack(windFile, 0.60, masterVolume);
        } else {
            channels.get("WEATHER_WIND").stopWithFade();
        }

        // 4. Weather Fire Sound
        if (weatherEnabled && (wUpper.contains("FIRE") || wUpper.contains("INCENDIE") || wUpper.contains("FEU"))) {
            File fireFile = findSoundFile("AMB_Nature Pack Vol 1_ Fire & Elemental_Large Bonfire.mp3");
            if (fireFile == null) fireFile = findSoundFile("AMB_Nature Pack Vol 1_ Fire & Elemental_Campfire Crackling.mp3");
            channels.get("WEATHER_FIRE").playTrack(fireFile, 0.70, masterVolume);
        } else {
            channels.get("WEATHER_FIRE").stopWithFade();
        }

        // 5. Biome Ambient Sound Loop
        if (ambientEnabled && !isRain && !isWindy) {
            File biomeFile = null;
            if (bUpper.contains("DESERT") || bUpper.contains("DÉSERT") || bUpper.contains("ARID")) {
                biomeFile = findSoundFile("AMB_Nature Pack Vol 1_Weather_Desert Wind.mp3");
                if (biomeFile == null) biomeFile = findSoundFile("desert_wind_ambient.mp3");
            } else if (bUpper.contains("JUNGLE") || bUpper.contains("TROPICAL")) {
                biomeFile = findSoundFile("mixkit-birds-in-the-jungle-2434.wav");
                if (biomeFile == null) biomeFile = findSoundFile("Sound_of_the_jungle_in_Thailand.flac");
            } else if (bUpper.contains("SWAMP") || bUpper.contains("MARAIS")) {
                biomeFile = findSoundFile("AMB_Nature Pack Vol 1_Water Enviroments_Swamp.mp3");
                if (biomeFile == null) biomeFile = findSoundFile("Nature_sounds_ambience_in_a_Dordogne_pond.ogg");
            } else if (isDay) {
                biomeFile = findSoundFile("AMB_Nature Pack Vol 1_Forest Enviroments_Forest Day.mp3");
                if (biomeFile == null) biomeFile = findSoundFile("AMBForst_Forest (ID 0100)_BigSoundBank.com.mp3");
                if (biomeFile == null) biomeFile = findSoundFile("mixkit-morning-birds-2472.wav");
            } else { // Night
                biomeFile = findSoundFile("ElevenLabs_Ambiance_nocturne_animée,_grillons_qui_chantent_et_lucioles_qui_brillent_dans_le_noir.mp3");
                if (biomeFile == null) biomeFile = findSoundFile("AMB_Nature Pack Vol 1_Forest Enviroments_Forest Night.mp3");
                if (biomeFile == null) biomeFile = findSoundFile("AMBRurl_Nocturnal insects 4 (ID 1470)_BigSoundBank.com.mp3");
            }
            channels.get("BIOME").playTrack(biomeFile, 0.50, masterVolume);
        } else {
            channels.get("BIOME").stopWithFade();
        }

        // 6. Insect Colony Activity & Digging Sound
        if (insectEnabled && populationCount > 5) {
            double popVolScale = Math.min(1.0, Math.log10(populationCount) / 3.5);
            File insectFile = null;
            if (cameraDepth > 0.4 || cameraZoom < 3.0) {
                // Subterranean nest view / close-up digging
                insectFile = findSoundFile("anthill_nest_sounds.mp3");
                if (insectFile == null) insectFile = findSoundFile("sand_soil_digging.mp3");
                if (insectFile == null) insectFile = findSoundFile("termites-and-ants-sound.mp3");
            } else {
                // Surface activity
                insectFile = findSoundFile("ant_colony_activity.mp3");
                if (insectFile == null) insectFile = findSoundFile("freesound_community-ants-23656.mp3");
                if (insectFile == null) insectFile = findSoundFile("antscolony.mp3");
            }
            channels.get("INSECT").playTrack(insectFile, 0.55 * popVolScale, masterVolume);
        } else {
            channels.get("INSECT").stopWithFade();
        }

        // Update fade tick for all channels
        for (ChannelPlayer channel : channels.values()) {
            channel.updateFadeTick(deltaSec, masterVolume);
        }
    }

    private void stopAllClips() {
        for (ChannelPlayer channel : channels.values()) {
            channel.stopImmediately();
        }
    }

    private File findSoundFile(String filename) {
        if (filename == null) return null;
        String lower = filename.toLowerCase();

        // 1. Direct index match
        File cached = soundBankIndex.get(lower);
        if (cached != null && cached.exists() && cached.isFile()) return cached;

        // 2. Base name match
        int dot = lower.lastIndexOf('.');
        if (dot > 0) {
            cached = soundBankIndex.get(lower.substring(0, dot));
            if (cached != null && cached.exists() && cached.isFile()) return cached;
        }

        // 3. Fallback path search
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

    private static void setClipGain(Clip clip, double vol) {
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (Math.log(Math.max(0.0001, vol)) / Math.log(10.0) * 20.0);
                gainControl.setValue(Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB)));
            }
        } catch (Exception ignored) {}
    }

    private static void runOnFx(Runnable r) {
        try {
            if (Platform.isFxApplicationThread()) {
                r.run();
            } else {
                Platform.runLater(r);
            }
        } catch (Exception ignored) {
            // JavaFX toolkit not initialized (e.g. headless unit tests)
        }
    }

    // ── Channel Player Inner Class (Fade In / Sustain / Fade Out Engine) ──────────

    public enum FadeState {
        STOPPED,
        FADING_IN,
        SUSTAINING,
        FADING_OUT
    }

    private static class ChannelPlayer {
        private final String channelName;
        private File currentFile = null;
        private double currentVolume = 0.0;
        private double targetVolume = 0.0;
        private double startFadeVolume = 0.0;
        private FadeState fadeState = FadeState.STOPPED;

        private MediaPlayer fxPlayer = null;
        private Clip javaClip = null;
        private static final double FADE_DURATION_SEC = 1.5;

        public ChannelPlayer(String channelName) {
            this.channelName = channelName;
        }

        public synchronized void playTrack(File soundFile, double targetVol, double masterVolume) {
            if (soundFile == null) {
                stopWithFade();
                return;
            }

            this.targetVolume = Math.max(0.0, Math.min(1.0, targetVol));

            if (currentFile != null && currentFile.equals(soundFile) && fadeState != FadeState.STOPPED) {
                if (fadeState == FadeState.FADING_OUT) {
                    fadeState = FadeState.FADING_IN;
                }
                return;
            }

            stopImmediately();

            this.currentFile = soundFile;
            this.currentVolume = 0.0;
            this.startFadeVolume = 0.0;
            this.fadeState = FadeState.FADING_IN;

            createAndStartPlayer(soundFile, 0.0, masterVolume);
        }

        public synchronized void updateFadeTick(double deltaSec, double masterVolume) {
            if (fadeState == FadeState.STOPPED) return;

            double effectiveMasterVol = Math.max(0.0001, masterVolume);

            if (fadeState == FadeState.FADING_IN) {
                double step = (targetVolume / FADE_DURATION_SEC) * deltaSec;
                currentVolume += step;
                if (currentVolume >= targetVolume) {
                    currentVolume = targetVolume;
                    fadeState = FadeState.SUSTAINING;
                }
                applyVolume(currentVolume * effectiveMasterVol);
            } else if (fadeState == FadeState.SUSTAINING) {
                currentVolume = targetVolume;
                applyVolume(currentVolume * effectiveMasterVol);
            } else if (fadeState == FadeState.FADING_OUT) {
                double step = (Math.max(0.01, startFadeVolume) / FADE_DURATION_SEC) * deltaSec;
                currentVolume -= step;
                if (currentVolume <= 0.001) {
                    currentVolume = 0.0;
                    fadeState = FadeState.STOPPED;
                    stopImmediately();
                } else {
                    applyVolume(currentVolume * effectiveMasterVol);
                }
            }
        }

        public synchronized void stopWithFade() {
            if (fadeState != FadeState.STOPPED && fadeState != FadeState.FADING_OUT) {
                this.startFadeVolume = currentVolume;
                this.fadeState = FadeState.FADING_OUT;
            }
        }

        private void createAndStartPlayer(File file, double initialVol, double masterVolume) {
            try {
                final String uriStr = file.toURI().toString();
                runOnFx(() -> {
                    try {
                        Media media = new Media(uriStr);
                        MediaPlayer player = new MediaPlayer(media);
                        player.setCycleCount(MediaPlayer.INDEFINITE);
                        player.setVolume(initialVol * masterVolume);
                        player.play();
                        fxPlayer = player;
                    } catch (Exception e) {
                        if (file.getName().toLowerCase().endsWith(".wav")) {
                            createJavaClipFallback(file, initialVol * masterVolume);
                        }
                    }
                });
            } catch (Exception e) {
                if (file.getName().toLowerCase().endsWith(".wav")) {
                    createJavaClipFallback(file, initialVol * masterVolume);
                }
            }
        }

        private void createJavaClipFallback(File file, double initialVol) {
            try {
                AudioInputStream ais = AudioSystem.getAudioInputStream(file);
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                setClipGain(clip, initialVol);
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();
                javaClip = clip;
            } catch (Exception ignored) {}
        }

        private void applyVolume(double vol) {
            final double v = Math.max(0.0, Math.min(1.0, vol));
            if (fxPlayer != null) {
                runOnFx(() -> {
                    if (fxPlayer != null) {
                        fxPlayer.setVolume(v);
                    }
                });
            }
            if (javaClip != null && javaClip.isOpen()) {
                setClipGain(javaClip, v);
            }
        }

        public synchronized void stopImmediately() {
            if (fxPlayer != null) {
                final MediaPlayer p = fxPlayer;
                fxPlayer = null;
                runOnFx(() -> {
                    try {
                        p.stop();
                        p.dispose();
                    } catch (Exception ignored) {}
                });
            }
            if (javaClip != null) {
                try {
                    javaClip.stop();
                    javaClip.close();
                } catch (Exception ignored) {}
                javaClip = null;
            }
            currentFile = null;
            currentVolume = 0.0;
            fadeState = FadeState.STOPPED;
        }
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
        byte[] pcm = new byte[totalSamples * 2]; // 16-bit mono PCM

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
