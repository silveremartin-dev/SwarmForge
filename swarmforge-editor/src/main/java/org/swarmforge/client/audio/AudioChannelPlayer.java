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

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.File;

/**
 * Audio channel player supporting cross-fading (fade-in, sustain, fade-out),
 * JavaFX {@link MediaPlayer} streaming, and fallback to low-latency {@link Clip}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class AudioChannelPlayer {

    public enum FadeState {
        STOPPED,
        FADING_IN,
        SUSTAINING,
        FADING_OUT
    }

    private final String channelName;
    private File currentFile = null;
    private double currentVolume = 0.0;
    private double targetVolume = 0.0;
    private double startFadeVolume = 0.0;
    private FadeState fadeState = FadeState.STOPPED;

    private MediaPlayer fxPlayer = null;
    private Clip javaClip = null;
    private static final double FADE_DURATION_SEC = 1.5;
    private double lastAppliedVol = -1.0;

    public AudioChannelPlayer(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelName() {
        return channelName;
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
                    media.setOnError(() -> createJavaClipFallback(file, initialVol * masterVolume));
                    MediaPlayer player = new MediaPlayer(media);
                    player.setOnError(() -> createJavaClipFallback(file, initialVol * masterVolume));
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
        if (Math.abs(lastAppliedVol - v) < 0.005) {
            return;
        }
        lastAppliedVol = v;
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
        lastAppliedVol = -1.0;
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

    public static void setClipGain(Clip clip, double vol) {
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (Math.log(Math.max(0.0001, vol)) / Math.log(10.0) * 20.0);
                gainControl.setValue(Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB)));
            }
        } catch (Exception ignored) {}
    }

    public static void runOnFx(Runnable r) {
        try {
            if (Platform.isFxApplicationThread()) {
                r.run();
            } else {
                Platform.runLater(r);
            }
        } catch (Exception ignored) {}
    }
}
