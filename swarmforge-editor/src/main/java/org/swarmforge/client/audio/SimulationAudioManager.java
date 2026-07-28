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
    private boolean weatherEnabled = true;
    private boolean insectEnabled = true;
    private double masterVolume = 0.7; // 0.0 - 1.0

    private String currentBiome = "Forest";
    private String currentWeather = "Clear";
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

                while (running.get()) {
                    if (masterVolume <= 0.001 || !simRunning) {
                        // Silent when paused or muted
                        java.util.Arrays.fill(buffer, (byte) 0);
                        line.write(buffer, 0, buffer.length);
                        try { Thread.sleep(20); } catch (InterruptedException ignored) {}
                        continue;
                    }

                    int samples = buffer.length / 2;
                    for (int i = 0; i < samples; i++) {
                        tick++;
                        double sampleVal = 0.0;

                        // 1. Biome Ambiance (Bird chirps / Wind rustle)
                        if (ambientEnabled) {
                            if ("Forest".equalsIgnoreCase(currentBiome) || "Tempérée".equalsIgnoreCase(currentBiome)) {
                                // Gentle wind breeze + occasional bird chirp
                                double wind = Math.sin(phaseAmbient * 0.005) * 0.08 * (rand.nextDouble() * 0.2 + 0.9);
                                phaseAmbient += 1.0;
                                double chirp = 0.0;
                                if ((tick % 12000) < 150) { // Periodic bird chirp
                                    double chirpFreq = 2200.0 + Math.sin((tick % 12000) * 0.1) * 600.0;
                                    chirp = Math.sin(chirpFreq * (tick / (double) SAMPLE_RATE) * 2.0 * Math.PI) * 0.15;
                                }
                                sampleVal += (wind + chirp);
                            } else if ("Desert".equalsIgnoreCase(currentBiome) || "Désert".equalsIgnoreCase(currentBiome)) {
                                // Dry wind hiss
                                double dryWind = (rand.nextDouble() - 0.5) * 0.06 * Math.sin(tick * 0.0003);
                                sampleVal += dryWind;
                            }
                        }

                        // 2. Weather Effects (Rain / Thunder / Storm)
                        if (weatherEnabled) {
                            if (currentWeather.contains("Rain") || currentWeather.contains("Pluie") || currentWeather.contains("Caniculaire")) {
                                double rainNoise = (rand.nextDouble() - 0.5) * 0.12; // Rain patter
                                sampleVal += rainNoise;
                            }
                            if (currentWeather.contains("Thunder") || currentWeather.contains("Orage")) {
                                if ((tick % 25000) < 400) { // Thunder rumble
                                    double rumble = Math.sin(50.0 * (tick / (double) SAMPLE_RATE) * 2.0 * Math.PI) * (rand.nextDouble() * 0.3);
                                    sampleVal += rumble;
                                }
                            }
                        }

                        // 3. Insect & Subterranean Activity (Excavation / Clicking in galleries)
                        if (insectEnabled) {
                            if (cameraDepth > 0.3) {
                                // Subterranean tunnel echo & mandibles crunching
                                if (rand.nextDouble() < 0.003) { // Mandible click
                                    double click = (rand.nextDouble() - 0.5) * 0.25;
                                    sampleVal += click;
                                }
                                double lowTunnelHum = Math.sin(80.0 * (tick / (double) SAMPLE_RATE) * 2.0 * Math.PI) * 0.04;
                                sampleVal += lowTunnelHum;
                            } else {
                                // Surface foraging rustle
                                if (rand.nextDouble() < 0.001) {
                                    sampleVal += (rand.nextDouble() - 0.5) * 0.15;
                                }
                            }
                        }

                        // Master Volume & Clipping clamp
                        sampleVal *= masterVolume;
                        sampleVal = Math.max(-1.0, Math.min(1.0, sampleVal));

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

    public void updateState(String biome, String weather, double cameraDepth, boolean isSimRunning) {
        this.currentBiome = biome;
        this.currentWeather = weather;
        this.cameraDepth = cameraDepth;
        this.simRunning = isSimRunning;
    }

    public void stop() {
        running.set(false);
        if (line != null && line.isOpen()) {
            line.close();
        }
    }
}
