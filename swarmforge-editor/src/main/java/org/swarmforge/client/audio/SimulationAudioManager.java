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

                // Low-pass filter memory states for smooth acoustics
                double rainFilter = 0.0;
                double windFilter = 0.0;
                double clickEnv = 0.0;

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

                        // 1. Biome Ambiance (Soft Wind breeze & harmonic bird calls)
                        if (ambientEnabled) {
                            double rawWind = (rand.nextDouble() - 0.5) * 0.08;
                            windFilter = 0.96 * windFilter + 0.04 * rawWind; // Deep low-pass wind
                            double windMod = (Math.sin(tick * 0.0003) * 0.5 + 0.5) * windFilter * 2.0;

                            double chirp = 0.0;
                            int cycle = tick % 12000;
                            if (cycle < 400) {
                                double env = Math.sin(cycle / 400.0 * Math.PI); // Smooth envelope
                                double chirpFreq = 2200.0 + Math.sin(cycle * 0.08) * 600.0;
                                chirp = Math.sin(chirpFreq * (tick / (double) SAMPLE_RATE) * 2.0 * Math.PI) * 0.12 * env;
                            }
                            sampleVal += (windMod + chirp);
                        }

                        // 2. Weather Effects (Low-passed soft rain patter & thunder rumble)
                        if (weatherEnabled) {
                            if (currentWeather != null && (currentWeather.contains("Rain") || currentWeather.contains("Pluie") || currentWeather.contains("Orage") || currentWeather.contains("Snow") || currentWeather.contains("Neige"))) {
                                double rawRain = (rand.nextDouble() - 0.5) * 0.15;
                                rainFilter = 0.85 * rainFilter + 0.15 * rawRain; // Soft pink noise rain
                                sampleVal += rainFilter * 0.6;
                            }
                            if (currentWeather != null && (currentWeather.contains("Thunder") || currentWeather.contains("Orage"))) {
                                int tCycle = tick % 18000;
                                if (tCycle < 800) { // Deep thunder rumble
                                    double tEnv = Math.sin(tCycle / 800.0 * Math.PI);
                                    double rumble = Math.sin(40.0 * (tick / (double) SAMPLE_RATE) * 2.0 * Math.PI) * tEnv * 0.25;
                                    sampleVal += rumble;
                                }
                            }
                        }

                        // 3. Insect & Subterranean Activity (Resonant mandible clicks & low earth resonance)
                        if (insectEnabled) {
                            if (rand.nextDouble() < 0.003) {
                                clickEnv = 1.0; // Trigger click pulse
                            }
                            if (clickEnv > 0.001) {
                                double clickTone = Math.sin(3200.0 * (tick / (double) SAMPLE_RATE) * 2.0 * Math.PI) * clickEnv * 0.10;
                                sampleVal += clickTone;
                                clickEnv *= 0.985; // Damped decay
                            }
                            double earthHum = Math.sin(65.0 * (tick / (double) SAMPLE_RATE) * 2.0 * Math.PI) * 0.04;
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

    private boolean hasTrees = true;
    private double cameraZoom = 7.5;

    public void setHasTrees(boolean hasTrees) {
        this.hasTrees = hasTrees;
    }

    public void setCameraZoom(double zoom) {
        this.cameraZoom = zoom;
    }

    public void updateState(String biome, String weather, double cameraDepth, boolean isSimRunning) {
        this.currentBiome = biome;
        this.currentWeather = weather;
        this.simRunning = isSimRunning;
        this.cameraZoom = cameraDepth;
    }

    public void stop() {
        running.set(false);
        if (line != null && line.isOpen()) {
            line.close();
        }
    }
}
