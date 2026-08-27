/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.audio;

import javax.sound.sampled.*;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Procedural Audio Manager for SwarmForge Simulation.
 * Synthesizes ambient biome sounds, weather audio effects, and subterranean insect activity
 * using native Java Sound API to guarantee zero OpenAL lockups and zero external file dependencies.
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
    private int populationCount = 100;
    private double windSpeedMps = 5.0;
    private double rainRateMmHr = 0.0;
    private double cameraDepth = 0.0; // 0.0 = surface, >0.5 = subterranean
    boolean simRunning = false;

    private SourceDataLine line;
    private static final int SAMPLE_RATE = 22050;

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
                Random rand = new Random();
                double phaseAmbient = 0.0;
                double phaseWeather = 0.0;
                double phaseInsect = 0.0;
                int tick = 0;

                // Low-pass filter memory states for smooth acoustics
                double rainFilter = 0.0;
                double windFilter = 0.0;
                double riverFilter = 0.0;
                double fireFilter = 0.0;
                double clickEnv = 0.0;
                double bubbleEnv = 0.0;

                while (running.get()) {
                    if (masterVolume <= 0.001 || !simRunning) {
                        // Silent when paused or muted
                        java.util.Arrays.fill(buffer, (byte) 0);
                        line.write(buffer, 0, buffer.length);
                        try { Thread.sleep(20); } catch (InterruptedException ignored) {}
                        continue;
                    }

                    int samples = buffer.length / 2;
                    String weatherUpper = currentWeather != null ? currentWeather.toUpperCase() : "CLEAR";
                    String seasonUpper = currentSeason != null ? currentSeason.toUpperCase() : "SUMMER";
                    boolean isWinter = seasonUpper.contains("WINTER") || seasonUpper.contains("HIVER") || seasonUpper.contains("INVIERNO") || seasonUpper.contains("冬");

                    for (int i = 0; i < samples; i++) {
                        tick++;
                        double sampleVal = 0.0;

                        // 1. Biome Ambiance (Breeze, foliage rustle & bird chirps)
                        if (ambientEnabled) {
                            double rawWind = (rand.nextDouble() - 0.5) * 0.08;
                            windFilter = 0.96 * windFilter + 0.04 * rawWind;
                            double baseWindFactor = Math.max(0.3, windSpeedMps / 10.0);
                            double windMod = (Math.sin(tick * 0.0003) * 0.5 + 0.5) * windFilter * 2.0 * baseWindFactor;

                            // Leaf rustle audio when trees/foliage are present
                            double leafRustle = 0.0;
                            if (hasTrees && !isWinter) {
                                double rawLeaf = (rand.nextDouble() - 0.5) * 0.035;
                                leafRustle = Math.sin(tick * 0.02) * rawLeaf * (Math.sin(tick * 0.0008) * 0.5 + 0.5);
                            }

                            // Bird chirps (Muted in winter)
                            double chirp = 0.0;
                            if (!isWinter) {
                                int cycle = tick % 14000;
                                if (cycle < 400) {
                                    double env = Math.sin(cycle / 400.0 * Math.PI);
                                    double chirpFreq = 2200.0 + Math.sin(cycle * 0.08) * 600.0;
                                    chirp = Math.sin(chirpFreq * (tick / (double) SAMPLE_RATE) * 2.0 * Math.PI) * 0.08 * env;
                                }
                            }
                            sampleVal += (windMod + leafRustle + chirp);
                        }

                        // 1b. Procedural River Water Stream & Flow Audio (STRICTLY conditioned on hasRiver presence)
                        if (riverEnabled && hasRiver) {
                            double rawWater = (rand.nextDouble() - 0.5) * 0.12;
                            riverFilter = 0.88 * riverFilter + 0.12 * rawWater; // Low-pass water flow
                            double flowMod = Math.sin(tick * 0.004) * 0.35 + 0.65;

                            if (rand.nextDouble() < 0.004) {
                                bubbleEnv = 1.0;
                            }
                            double gurgle = 0.0;
                            if (bubbleEnv > 0.001) {
                                gurgle = Math.sin(780.0 * (tick / (double) SAMPLE_RATE) * 2.0 * Math.PI) * bubbleEnv * 0.03;
                                bubbleEnv *= 0.96;
                            }
                            sampleVal += (riverFilter * 0.50 + gurgle) * flowMod;
                        }

                        // 2. Weather Effects (Rain patter, Wind storm, Hail, Fire & Thunder across 5 languages)
                        if (weatherEnabled) {
                            boolean isRain = isRainType(weatherUpper) || rainRateMmHr > 0.1;
                            boolean isStorm = isStormType(weatherUpper) || windSpeedMps > 18.0;
                            boolean isThunder = weatherUpper.contains("THUNDER") || weatherUpper.contains("ORAGE") || weatherUpper.contains("TORMENTA") || weatherUpper.contains("GEWITTER") || weatherUpper.contains("雷");
                            boolean isFire = weatherUpper.contains("FIRE") || weatherUpper.contains("INCENDIE") || weatherUpper.contains("INCENDIO") || weatherUpper.contains("FEUER") || weatherUpper.contains("火");

                            if (isRain) {
                                double intensity = Math.max(0.3, Math.min(2.0, rainRateMmHr / 5.0 + 0.5));
                                double rawRain = (rand.nextDouble() - 0.5) * 0.22 * intensity;
                                rainFilter = 0.82 * rainFilter + 0.18 * rawRain;
                                sampleVal += rainFilter * 0.7;
                            }

                            if (isStorm) {
                                double stormWind = (rand.nextDouble() - 0.5) * 0.20;
                                windFilter = 0.92 * windFilter + 0.08 * stormWind;
                                sampleVal += windFilter * 0.8;
                            }

                            if (isFire) {
                                double fireRoar = (rand.nextDouble() - 0.5) * 0.15;
                                fireFilter = 0.90 * fireFilter + 0.10 * fireRoar;
                                double crackle = rand.nextDouble() < 0.012 ? (rand.nextDouble() - 0.5) * 0.28 : 0.0;
                                sampleVal += (fireFilter * 0.45 + crackle);
                            }

                            if (isThunder) {
                                int tCycle = tick % 16000;
                                if (tCycle < 800) {
                                    double tEnv = Math.sin(tCycle / 800.0 * Math.PI);
                                    double rumble = Math.sin(42.0 * (tick / (double) SAMPLE_RATE) * 2.0 * Math.PI) * tEnv * 0.35;
                                    sampleVal += rumble;
                                }
                            }
                        }

                        // 3. Insect Colony Activity (mandible clicking & chitin rustle scaled by active population & zoom)
                        if (insectEnabled && populationCount > 0) {
                            double popScale = Math.min(2.5, Math.log10(Math.max(10, populationCount)) * 0.7);
                            double zoomFactor = Math.max(0.2, Math.min(1.5, cameraZoom / 7.5));
                            double insectVol = 0.015 * popScale * zoomFactor;

                            if (rand.nextDouble() < (0.002 * popScale)) {
                                clickEnv = 1.0;
                            }
                            if (clickEnv > 0.001) {
                                double diggingTone = Math.sin(380.0 * (tick / (double) SAMPLE_RATE) * 2.0 * Math.PI) * clickEnv * insectVol;
                                sampleVal += diggingTone;
                                clickEnv *= 0.965;
                            }
                            double earthHum = Math.sin(70.0 * (tick / (double) SAMPLE_RATE) * 2.0 * Math.PI) * (insectVol * 0.4);
                            sampleVal += earthHum;
                        }

                        // Master Volume & Soft Limiting
                        sampleVal *= masterVolume;
                        sampleVal = Math.tanh(sampleVal); // Smooth soft-knee saturation prevents digital clipping

                        short pcmShort = (short) (sampleVal * 32767.0);
                        buffer[i * 2] = (byte) (pcmShort & 0xFF);
                        buffer[i * 2 + 1] = (byte) ((pcmShort >> 8) & 0xFF);
                    }

                    line.write(buffer, 0, buffer.length);
                }

                line.drain();
                line.close();
            } catch (Exception e) {
                System.err.println("[Audio] Error in audio synthesizer thread: " + e.getMessage());
            }
        });
    }

    public void setAmbientEnabled(boolean enabled) {
        this.ambientEnabled = enabled;
    }

    public boolean isAmbientEnabled() {
        return ambientEnabled;
    }

    public void setRiverEnabled(boolean enabled) {
        this.riverEnabled = enabled;
    }

    public boolean isRiverEnabled() {
        return riverEnabled;
    }

    public void setWeatherEnabled(boolean enabled) {
        this.weatherEnabled = enabled;
    }

    public boolean isWeatherEnabled() {
        return weatherEnabled;
    }

    public void setInsectEnabled(boolean enabled) {
        this.insectEnabled = enabled;
    }

    public boolean isInsectEnabled() {
        return insectEnabled;
    }

    public void setMasterVolume(double volume) {
        this.masterVolume = Math.max(0.0, Math.min(1.0, volume));
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

    public void stop() {
        running.set(false);
        if (line != null && line.isOpen()) {
            line.close();
        }
    }
}
