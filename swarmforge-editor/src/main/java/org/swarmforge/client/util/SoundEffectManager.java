/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.util;

import javax.sound.sampled.*;
import java.io.File;
import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Procedural & Resource-Based Sound Effect Manager for SwarmForge Simulation Studio.
 * Generates bio-acoustic environmental ambience (river water flow, rain, thunder, wind, fire)
 * using real-time audio synthesis (javax.sound.sampled) and supports loading sound bank .wav files.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SoundEffectManager {

    private static final SoundEffectManager INSTANCE = new SoundEffectManager();
    public static SoundEffectManager getInstance() { return INSTANCE; }

    private final ExecutorService soundPool = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "SwarmForge-Audio-Thread");
        t.setDaemon(true);
        return t;
    });

    private float masterVolume = 0.8f;
    private float ambientVolume = 0.6f;
    private boolean muted = false;

    private Clip riverLoopClip = null;
    private Clip weatherLoopClip = null;

    private SoundEffectManager() {}

    public void setMasterVolume(float vol) {
        this.masterVolume = Math.max(0.0f, Math.min(1.0f, vol));
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        if (muted) {
            stopAmbience();
        }
    }

    public boolean isMuted() {
        return muted;
    }

    /**
     * Plays a short procedural click / interaction sound effect.
     */
    public void playClickSound() {
        if (muted) return;
        soundPool.submit(() -> synthesizeBeep(800, 40, 0.15f * masterVolume));
    }

    /**
     * Plays a capture / snapshot shutter sound effect.
     */
    public void playCaptureSound() {
        if (muted) return;
        soundPool.submit(() -> synthesizeWhiteNoiseBurst(60, 0.3f * masterVolume));
    }

    /**
     * Plays an ant step / mandibles movement sound.
     */
    public void playAntStepSound() {
        if (muted) return;
        soundPool.submit(() -> synthesizeBeep(1200 + new Random().nextInt(400), 15, 0.08f * masterVolume));
    }

    /**
     * Plays continuous river water noise ambience.
     */
    public synchronized void startRiverWaterAmbience() {
        if (muted || riverLoopClip != null) return;
        soundPool.submit(() -> {
            try {
                byte[] pcmData = generateFilteredNoisePCM(3.0, 300, 1200, 0.25f * ambientVolume * masterVolume);
                AudioFormat format = new AudioFormat(22050, 8, 1, false, false);
                riverLoopClip = AudioSystem.getClip();
                riverLoopClip.open(format, pcmData, 0, pcmData.length);
                riverLoopClip.loop(Clip.LOOP_CONTINUOUSLY);
                riverLoopClip.start();
            } catch (Exception ignored) { }
        });
    }

    /**
     * Stops river water noise ambience.
     */
    public synchronized void stopRiverWaterAmbience() {
        if (riverLoopClip != null) {
            try {
                riverLoopClip.stop();
                riverLoopClip.close();
            } catch (Exception ignored) { }
            riverLoopClip = null;
        }
    }

    /**
     * Plays weather sound effect (Rain, Thunder, Wind, Fire).
     */
    public void playWeatherEffect(String effectType) {
        if (muted) return;
        soundPool.submit(() -> {
            switch (effectType.toUpperCase()) {
                case "THUNDER":
                    synthesizeLowRumble(1.2, 80, 0.5f * masterVolume);
                    break;
                case "WIND":
                    synthesizeModulatedNoise(2.0, 200, 600, 0.25f * masterVolume);
                    break;
                case "FIRE":
                    synthesizeCracklingNoise(1.5, 0.3f * masterVolume);
                    break;
                case "HAIL":
                case "RAIN":
                default:
                    try {
                        byte[] rainBuf = generateFilteredNoisePCM(1.0, 500, 2500, 0.2f * masterVolume);
                        AudioFormat format = new AudioFormat(22050, 8, 1, true, false);
                        SourceDataLine line = AudioSystem.getSourceDataLine(format);
                        line.open(format);
                        line.start();
                        line.write(rainBuf, 0, rainBuf.length);
                        line.drain();
                        line.close();
                    } catch (Exception ignored) { }
                    break;
            }
        });
    }

    /**
     * Plays external sound file from sound bank if present, or falls back gracefully to procedural synthesis.
     */
    public void playSoundFromBank(String soundFileName) {
        if (muted || soundFileName == null) return;
        soundPool.submit(() -> {
            try {
                String targetQuery = soundFileName.toLowerCase().replace(".wav", "").replace(".mp3", "").replace(".ogg", "");

                // Search directories for audio bank files
                String[] searchDirs = new String[]{
                    "swarmforge-web/public/sounds",
                    "sounds",
                    "swarmforge-web/dist/sounds",
                    "src/main/resources/sounds"
                };

                for (String dirPath : searchDirs) {
                    File dir = new File(dirPath);
                    if (dir.exists() && dir.isDirectory()) {
                        File[] files = dir.listFiles();
                        if (files != null) {
                            for (File f : files) {
                                String fName = f.getName().toLowerCase();
                                if (fName.equals(soundFileName.toLowerCase()) || fName.contains(targetQuery)) {
                                    try {
                                        AudioInputStream ais = AudioSystem.getAudioInputStream(f);
                                        Clip clip = AudioSystem.getClip();
                                        clip.open(ais);
                                        setClipVolume(clip, masterVolume);
                                        clip.start();
                                        return;
                                    } catch (Exception ignored) { }
                                }
                            }
                        }
                    }
                }

                InputStream is = getClass().getResourceAsStream("/sounds/" + soundFileName);
                if (is != null) {
                    AudioInputStream ais = AudioSystem.getAudioInputStream(is);
                    Clip clip = AudioSystem.getClip();
                    clip.open(ais);
                    setClipVolume(clip, masterVolume);
                    clip.start();
                    return;
                }
            } catch (Exception ignored) { }
            // Fallback procedural synthesis
            playClickSound();
        });
    }

    /**
     * Returns a list of recommended sound asset filenames for all biomes & climatic conditions.
     */
    public String[] getSoundBankSuggestions() {
        return new String[]{
            "ant_colony_activity.mp3 (Activités & stridulations collectives de la colonie)",
            "anthill_nest_sounds.mp3 (Sons internes de la fourmilière & galeries souterraines)",
            "sand_soil_digging.mp3 (Bruitage de creusement dans le sol & grattage de sable)",
            "dry_leaves_rustling.mp3 (Bruissement de feuilles sèches & litière forestière)",
            "soft_wind_leaves.mp3 (Brise légère traversant le feuillage)",
            "wind_gust_leaves.mp3 (Rafales de vent & bruissement d'arbres)",
            "strong_howling_wind.mp3 (Vent violent & bourrasques de tempête)",
            "desert_wind_ambient.mp3 (Vent chaud & aride du désert)",
            "water_splash2.ogg (Clapotis d'eau & éclaboussures de cours d'eau)",
            "river_stream.wav (Bruit d'eau de rivière fluide)",
            "rainforest_day.wav (Ambiance jungle tropicale & oiseaux néotropicaux)",
            "crickets_night.wav (Ambiance nocturne & grillons)",
            "rain_heavy.wav (Averse de pluie sur la canopée)",
            "thunder_strike.wav (Gondlement du tonnerre distant)"
        };
    }

    public synchronized void stopAmbience() {
        stopRiverWaterAmbience();
        if (weatherLoopClip != null) {
            try {
                weatherLoopClip.stop();
                weatherLoopClip.close();
            } catch (Exception ignored) { }
            weatherLoopClip = null;
        }
    }

    // ── Audio Synthesis Utilities ──────────────────────────────────────────────

    private void synthesizeBeep(int hz, int msec, float vol) {
        try {
            byte[] buf = new byte[msec * 22];
            for (int i = 0; i < buf.length; i++) {
                double angle = i / (22050.0 / hz) * 2.0 * Math.PI;
                buf[i] = (byte) (Math.sin(angle) * 127.0 * vol);
            }
            AudioFormat format = new AudioFormat(22050, 8, 1, true, false);
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();
            line.write(buf, 0, buf.length);
            line.drain();
            line.close();
        } catch (Exception ignored) { }
    }

    private void synthesizeWhiteNoiseBurst(int msec, float vol) {
        try {
            byte[] buf = new byte[msec * 22];
            Random rand = new Random();
            for (int i = 0; i < buf.length; i++) {
                double decay = 1.0 - ((double) i / buf.length);
                buf[i] = (byte) ((rand.nextInt(256) - 128) * vol * decay);
            }
            AudioFormat format = new AudioFormat(22050, 8, 1, true, false);
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();
            line.write(buf, 0, buf.length);
            line.drain();
            line.close();
        } catch (Exception ignored) { }
    }

    private void synthesizeLowRumble(double durationSec, int baseHz, float vol) {
        try {
            int samples = (int) (durationSec * 22050);
            byte[] buf = new byte[samples];
            Random rand = new Random();
            for (int i = 0; i < samples; i++) {
                double envelope = Math.sin(Math.PI * i / samples);
                double sine = Math.sin(2.0 * Math.PI * i * baseHz / 22050.0);
                double noise = (rand.nextDouble() - 0.5);
                buf[i] = (byte) ((sine * 0.7 + noise * 0.3) * 127.0 * vol * envelope);
            }
            AudioFormat format = new AudioFormat(22050, 8, 1, true, false);
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();
            line.write(buf, 0, buf.length);
            line.drain();
            line.close();
        } catch (Exception ignored) { }
    }

    private void synthesizeModulatedNoise(double durationSec, int minCutoff, int maxCutoff, float vol) {
        try {
            int samples = (int) (durationSec * 22050);
            byte[] buf = new byte[samples];
            Random rand = new Random();
            double val = 0;
            for (int i = 0; i < samples; i++) {
                double noise = (rand.nextDouble() - 0.5) * 2.0;
                double mod = 0.5 + 0.5 * Math.sin(2.0 * Math.PI * i * 0.5 / 22050.0);
                val = val * 0.85 + noise * 0.15 * mod;
                buf[i] = (byte) (val * 127.0 * vol);
            }
            AudioFormat format = new AudioFormat(22050, 8, 1, true, false);
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();
            line.write(buf, 0, buf.length);
            line.drain();
            line.close();
        } catch (Exception ignored) { }
    }

    private void synthesizeCracklingNoise(double durationSec, float vol) {
        try {
            int samples = (int) (durationSec * 22050);
            byte[] buf = new byte[samples];
            Random rand = new Random();
            for (int i = 0; i < samples; i++) {
                if (rand.nextDouble() < 0.02) {
                    buf[i] = (byte) ((rand.nextInt(256) - 128) * vol);
                } else {
                    buf[i] = (byte) ((rand.nextDouble() - 0.5) * 20.0 * vol);
                }
            }
            AudioFormat format = new AudioFormat(22050, 8, 1, true, false);
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();
            line.write(buf, 0, buf.length);
            line.drain();
            line.close();
        } catch (Exception ignored) { }
    }

    private byte[] generateFilteredNoisePCM(double durationSec, int lowCut, int highCut, float vol) {
        int samples = (int) (durationSec * 22050);
        byte[] buf = new byte[samples];
        Random rand = new Random();
        double lastVal = 0;
        for (int i = 0; i < samples; i++) {
            double noise = (rand.nextDouble() - 0.5) * 2.0;
            // Simple low-pass filter for smooth water flow sound
            lastVal = lastVal * 0.75 + noise * 0.25;
            buf[i] = (byte) (lastVal * 127.0 * vol);
        }
        return buf;
    }

    private void setClipVolume(Clip clip, float vol) {
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (Math.log(Math.max(0.0001f, vol)) / Math.log(10.0) * 20.0);
                gainControl.setValue(dB);
            }
        } catch (Exception ignored) { }
    }
}
