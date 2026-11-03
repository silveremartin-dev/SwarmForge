/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.util.Random;
import java.util.function.Function;

/**
 * Audio Recording and Procedural PCM Audio Synthesizer for video export and audio capture.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SimulationAudioRecorder {

    private final Function<String, File> soundResolver;
    private boolean recordingAudio = false;
    private long audioRecordingStartMs = 0;

    public SimulationAudioRecorder(Function<String, File> soundResolver) {
        this.soundResolver = soundResolver;
    }

    public synchronized void startAudioRecording() {
        this.recordingAudio = true;
        this.audioRecordingStartMs = System.currentTimeMillis();
    }

    public synchronized byte[] stopAudioRecording(
            boolean riverEnabled,
            boolean hasRiver,
            boolean weatherEnabled,
            String currentWeather,
            double rainRateMmHr,
            double windSpeedMps,
            boolean ambientEnabled,
            boolean insectEnabled,
            int populationCount,
            double masterVolume
    ) {
        if (!recordingAudio) {
            return new byte[0];
        }
        this.recordingAudio = false;
        long elapsedMs = System.currentTimeMillis() - audioRecordingStartMs;
        double durationSec = elapsedMs / 1000.0;
        return generateAmbientPcmAudio(
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

    public byte[] generateAmbientPcmAudio(
            double durationSec,
            boolean riverEnabled,
            boolean hasRiver,
            boolean weatherEnabled,
            String currentWeather,
            double rainRateMmHr,
            double windSpeedMps,
            boolean ambientEnabled,
            boolean insectEnabled,
            int populationCount,
            double masterVolume
    ) {
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
        File soundFile = soundResolver.apply(filename);
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

    private static boolean isRainType(String w) {
        if (w == null) return false;
        return w.contains("RAIN") || w.contains("PLUIE") || w.contains("LLUVIA") || w.contains("REGEN") || w.contains("雨") ||
               w.contains("STORM") || w.contains("ORAGE") || w.contains("AVERSE") || w.contains("SCHAUER");
    }

    private static boolean isStormType(String w) {
        if (w == null) return false;
        return w.contains("WIND") || w.contains("VENT") || w.contains("VIENTO") || w.contains("WINDIG") || w.contains("风") ||
               w.contains("TEMPÊTE") || w.contains("TORMENTA") || w.contains("UNWETTER") || w.contains("暴风");
    }
}
