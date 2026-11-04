/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view;

import com.jme3.input.controls.ActionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Manages frame captures and disk persistence for timelapse recording in {@link JmeGameApp}.
 */
class JmeTimelapseRecorder {

    private static final Logger LOG = LoggerFactory.getLogger(JmeTimelapseRecorder.class);

    private final JmeGameApp app;
    private boolean recording = false;
    private int recordingFrameCount = 0;
    private long lastRecordTime = 0;
    private final File recordingDir = new File("exports/timelapse");

    final ActionListener recordListener = (name, isPressed, tpf) -> {
        if ("Toggle_Record".equals(name) && !isPressed) {
            recording = !recording;
            if (recording) {
                LOG.info("Recording Started...");
                if (!recordingDir.exists()) {
                    recordingDir.mkdirs();
                }
                recordingFrameCount = 0;
            } else {
                LOG.info("Recording Stopped.");
            }
        }
    };

    JmeTimelapseRecorder(JmeGameApp app) {
        this.app = app;
    }

    void onRenderFrame(byte[] pixelData, int width, int height) {
        if (!recording) return;
        long now = System.currentTimeMillis();
        if (now - lastRecordTime > 100) {
            lastRecordTime = now;
            app.getSceneBuilder().saveFrame(pixelData, width, height, recordingDir, recordingFrameCount++);
        }
    }
}
