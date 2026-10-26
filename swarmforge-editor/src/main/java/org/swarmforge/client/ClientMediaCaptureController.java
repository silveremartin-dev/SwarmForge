/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.swarmforge.client.audio.SimulationAudioManager;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.MediaCaptureUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * Controller handling HD screenshot capture and MP4/GIF video recording
 * for the 3D simulation viewport.
 */
class ClientMediaCaptureController {

    private static final Logger LOG = Logger.getLogger(ClientMediaCaptureController.class.getName());
    private final SwarmForgeClient client;
    private final I18nManager i18n = I18nManager.getInstance();

    private boolean isVideoRecording = false;
    private boolean isVideoArmed = false;
    private final List<BufferedImage> recordedVideoFrames = Collections.synchronizedList(new ArrayList<>());
    private Timeline videoCaptureTimeline;
    private long videoRecordingStartMs = 0;
    private Runnable stopVideoRecordingAndExport;
    private Button btnRecVideo;

    ClientMediaCaptureController(SwarmForgeClient client) {
        this.client = client;
    }

    private void setButtonState(Button btn, String stateClass) {
        btn.getStyleClass().removeAll("media-button-neutral", "media-button-recording", "media-button-processing", "media-button-success");
        btn.getStyleClass().add(stateClass);
    }

    VBox buildMediaSection(StackPane viewportStack, Button btnFullscreenMode) {
        this.btnRecVideo = new Button(i18n.get("sidebar.btn.video"));
        this.btnRecVideo.setMaxWidth(Double.MAX_VALUE);
        setButtonState(this.btnRecVideo, "media-button-neutral");
        File videoDir = new File("captures/videos");
        Tooltip ttRecVideo = new Tooltip("🎥 Enregistrement Vidéo 3D (MP4 / GIF)\n📂 Emplacement: " + videoDir.getAbsolutePath());
        this.btnRecVideo.setTooltip(ttRecVideo);

        setupRecordingHandlers();

        VBox mediaSection = new VBox(6);
        mediaSection.getStyleClass().add("sidebar-section-card");
        Label lblMedia = new Label();
        lblMedia.textProperty().bind(i18n.createStringBinding("sidebar.media.title"));
        lblMedia.getStyleClass().add("sidebar-section-label");

        Button btnPhoto = new Button(i18n.get("sidebar.btn.photo"));
        btnPhoto.setMaxWidth(Double.MAX_VALUE);
        setButtonState(btnPhoto, "media-button-neutral");
        File screenshotDir = new File("captures/screenshots");
        Tooltip ttPhoto = new Tooltip("📸 Capture d'écran HD (PNG)\n📂 Emplacement: " + screenshotDir.getAbsolutePath());
        btnPhoto.setTooltip(ttPhoto);

        btnPhoto.setOnAction(e -> takeScreenshot(viewportStack, btnPhoto));

        btnRecVideo.setOnAction(e -> {
            boolean isSimRunning = (client.simControlPanel != null && client.simControlPanel.isPlaying());
            if (isVideoRecording) {
                if (stopVideoRecordingAndExport != null) stopVideoRecordingAndExport.run();
            } else if (isVideoArmed) {
                disarmVideoRecording();
            } else if (!isSimRunning) {
                isVideoArmed = true;
                btnRecVideo.textProperty().unbind();
                btnRecVideo.setText("⏳ REC Armé (En attente)");
                setButtonState(btnRecVideo, "media-button-processing");
            } else {
                startVideoRecordingInternal();
            }
        });

        mediaSection.getChildren().addAll(lblMedia, btnFullscreenMode, btnPhoto, btnRecVideo);
        return mediaSection;
    }

    private void takeScreenshot(StackPane viewportStack, Button btnPhoto) {
        try {
            Rectangle flash = new Rectangle();
            flash.widthProperty().bind(viewportStack.widthProperty());
            flash.heightProperty().bind(viewportStack.heightProperty());
            flash.setFill(Color.WHITE);
            flash.setOpacity(0.75);
            flash.setMouseTransparent(true);
            viewportStack.getChildren().add(flash);

            FadeTransition ft = new FadeTransition(Duration.millis(350), flash);
            ft.setFromValue(0.75);
            ft.setToValue(0.0);
            ft.setOnFinished(ev -> viewportStack.getChildren().remove(flash));
            ft.play();

            String scName = (client.simControlPanel != null) ? client.simControlPanel.getSelectedScenarioName() : "Scenario";
            Node captureNode = (client.simWorldViewer != null && client.simWorldViewer.getCanvas3D() != null) ? client.simWorldViewer.getCanvas3D() : client.simWorldViewer;
            File screenshotFile = MediaCaptureUtil.takeScreenshot(captureNode, scName);
            LOG.info("[SwarmForge] Screenshot HD enregistré: " + screenshotFile.getAbsolutePath());

            btnPhoto.setText("✓ Screenshot Saved!");
            setButtonState(btnPhoto, "media-button-success");

            PauseTransition pt = new PauseTransition(Duration.seconds(3));
            pt.setOnFinished(ev -> {
                btnPhoto.setText(i18n.get("sidebar.btn.photo"));
                setButtonState(btnPhoto, "media-button-neutral");
            });
            pt.play();
        } catch (Exception ex) {
            LOG.severe("Error capturing HD screenshot: " + ex.getMessage());
        }
    }

    void startVideoRecordingInternal() {
        if (isVideoRecording) return;
        isVideoRecording = true;
        isVideoArmed = false;
        recordedVideoFrames.clear();
        videoRecordingStartMs = System.currentTimeMillis();
        SimulationAudioManager.getInstance().startAudioRecording();

        btnRecVideo.setText(i18n.get("sidebar.btn.video_recording", "🔴 Stop (REC 00:00 / 10:00 - 0 frames)"));
        setButtonState(btnRecVideo, "media-button-recording");

        videoCaptureTimeline = new Timeline(
            new KeyFrame(Duration.millis(100), ev -> {
                if (!isVideoRecording) return;
                try {
                    Node captureNode = (client.simWorldViewer != null && client.simWorldViewer.getCanvas3D() != null) ? client.simWorldViewer.getCanvas3D() : client.simWorldViewer;
                    WritableImage snap = captureNode.snapshot(new SnapshotParameters(), null);
                    BufferedImage frame = MediaCaptureUtil.convertToBufferedImage(snap);
                    recordedVideoFrames.add(frame);

                    long elapsedMs = System.currentTimeMillis() - videoRecordingStartMs;
                    long elapsedSec = elapsedMs / 1000;
                    long minutes = elapsedSec / 60;
                    long seconds = elapsedSec % 60;

                    btnRecVideo.setText(String.format("🔴 Stop (REC %02d:%02d / 10:00 - %d frames)", minutes, seconds, recordedVideoFrames.size()));

                    if (elapsedSec >= 600) {
                        if (stopVideoRecordingAndExport != null) stopVideoRecordingAndExport.run();
                    }
                } catch (Exception err) {
                    LOG.warning("Failed frame capture: " + err.getMessage());
                }
            })
        );
        videoCaptureTimeline.setCycleCount(Animation.INDEFINITE);
        videoCaptureTimeline.play();
    }

    void disarmVideoRecording() {
        if (isVideoArmed) {
            isVideoArmed = false;
            btnRecVideo.setText(i18n.get("sidebar.btn.video"));
            setButtonState(btnRecVideo, "media-button-neutral");
        }
    }

    void stopVideoRecordingAndExport() {
        if (stopVideoRecordingAndExport != null) {
            stopVideoRecordingAndExport.run();
        }
    }

    void cancelAndResetVideoRecording() {
        Runnable action = () -> {
            isVideoRecording = false;
            isVideoArmed = false;
            if (videoCaptureTimeline != null) {
                videoCaptureTimeline.stop();
                videoCaptureTimeline = null;
            }
            recordedVideoFrames.clear();
            try {
                SimulationAudioManager.getInstance().stopAudioRecording();
            } catch (Exception ignored) {}
            if (btnRecVideo != null) {
                btnRecVideo.setText(i18n.get("sidebar.btn.video"));
                setButtonState(btnRecVideo, "media-button-neutral");
            }
        };
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }

    void onSimulationPlay() {
        if (isVideoArmed && !isVideoRecording) {
            startVideoRecordingInternal();
        }
    }

    void onSimulationPauseOrStop() {
        if (isVideoRecording && stopVideoRecordingAndExport != null) {
            stopVideoRecordingAndExport.run();
        } else if (isVideoArmed) {
            disarmVideoRecording();
        }
    }

    private void setupRecordingHandlers() {
        this.stopVideoRecordingAndExport = () -> {
            if (!isVideoRecording) return;
            isVideoRecording = false;
            isVideoArmed = false;
            if (videoCaptureTimeline != null) {
                videoCaptureTimeline.stop();
                videoCaptureTimeline = null;
            }

            final byte[] pcmAudioData = SimulationAudioManager.getInstance().stopAudioRecording();

            btnRecVideo.setText(i18n.get("sidebar.btn.video"));
            setButtonState(btnRecVideo, "media-button-neutral");

            final List<BufferedImage> framesToExport = new ArrayList<>(recordedVideoFrames);
            final String scName = (client.simControlPanel != null) ? client.simControlPanel.getSelectedScenarioName() : "Scenario";

            if (framesToExport.isEmpty()) return;

            CompletableFuture.runAsync(() -> {
                try {
                    File videoFile = MediaCaptureUtil.exportMp4VideoClip(framesToExport, pcmAudioData, scName, 10);
                    Platform.runLater(() -> {
                        LOG.info("[SwarmForge] Vidéo 3D MP4 exportée: " + videoFile.getAbsolutePath());
                        btnRecVideo.setText("✓ Video Recorded!");
                        setButtonState(btnRecVideo, "media-button-success");

                        PauseTransition pt = new PauseTransition(Duration.seconds(3));
                        pt.setOnFinished(ev -> {
                            btnRecVideo.setText(i18n.get("sidebar.btn.video"));
                            setButtonState(btnRecVideo, "media-button-neutral");
                        });
                        pt.play();
                    });
                } catch (Exception ex) {
                    LOG.warning("MP4 encoding failed, falling back to Animated GIF: " + ex.getMessage());
                    try {
                        File videoFile = MediaCaptureUtil.exportGifVideoClip(framesToExport, scName, 100);
                        Platform.runLater(() -> {
                            LOG.info("[SwarmForge] Vidéo 3D GIF exportée: " + videoFile.getAbsolutePath());
                            btnRecVideo.setText("✓ Video Recorded!");
                            setButtonState(btnRecVideo, "media-button-success");

                            PauseTransition pt = new PauseTransition(Duration.seconds(3));
                            pt.setOnFinished(ev -> {
                                btnRecVideo.setText(i18n.get("sidebar.btn.video"));
                                setButtonState(btnRecVideo, "media-button-neutral");
                            });
                            pt.play();
                        });
                    } catch (Exception gifEx) {
                        LOG.severe("Error exporting video clip: " + gifEx.getMessage());
                    }
                }
            });
        };

        client.cancelAndResetVideoRecording = this::cancelAndResetVideoRecording;
    }
}
