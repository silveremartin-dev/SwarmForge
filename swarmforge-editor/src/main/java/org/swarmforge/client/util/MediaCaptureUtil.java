/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.util;

import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import org.jcodec.api.awt.AWTSequenceEncoder;

import org.jcodec.codecs.h264.H264Encoder;
import org.jcodec.common.Codec;
import org.jcodec.common.MuxerTrack;
import org.jcodec.common.VideoCodecMeta;
import org.jcodec.common.VideoEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Packet;
import org.jcodec.common.model.Picture;
import org.jcodec.common.model.Size;
import org.jcodec.containers.mp4.muxer.MP4Muxer;
import org.jcodec.containers.mp4.muxer.PCMMP4MuxerTrack;
import org.jcodec.scale.AWTUtil;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility for taking HD Screenshots and exporting 3D Video Clips (MP4 & Animated GIF) with scenario name and timestamp.
 */
public class MediaCaptureUtil {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public static String sanitizeFilename(String rawName) {
        if (rawName == null || rawName.trim().isEmpty()) {
            return "Scenario";
        }
        String clean = rawName.replaceAll("[^a-zA-Z0-9_-]", "_");
        clean = clean.replaceAll("_+", "_");
        if (clean.startsWith("_") && clean.length() > 1) {
            clean = clean.substring(1);
        }
        if (clean.endsWith("_") && clean.length() > 1) {
            clean = clean.substring(0, clean.length() - 1);
        }
        return clean.isEmpty() ? "Scenario" : clean;
    }

    public static File takeScreenshot(Node targetNode, String scenarioName) throws Exception {
        String cleanScenario = sanitizeFilename(scenarioName);
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String fileName = cleanScenario + "_" + timestamp + ".png";

        File outputDir = new File("captures/screenshots");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File outFile = new File(outputDir, fileName);

        WritableImage fxImage = targetNode.snapshot(new SnapshotParameters(), null);
        BufferedImage bImage = convertToBufferedImage(fxImage);

        ImageIO.write(bImage, "png", outFile);
        return outFile;
    }

    /**
     * Export video frames as native MP4 file at the given FPS (e.g. 10 FPS).
     */
    public static File exportMp4VideoClip(List<BufferedImage> frames, String scenarioName, int fps) throws Exception {
        return exportMp4VideoClip(frames, null, scenarioName, fps);
    }

    /**
     * Export video frames and PCM audio track as native MP4 file at the given FPS.
     */
    public static File exportMp4VideoClip(List<BufferedImage> frames, byte[] pcmAudioData, String scenarioName, int fps) throws Exception {
        String cleanScenario = sanitizeFilename(scenarioName);
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String fileName = cleanScenario + "_video_" + timestamp + ".mp4";

        File outputDir = new File("captures/videos");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File outFile = new File(outputDir, fileName);

        if (frames.isEmpty()) {
            throw new IllegalArgumentException("Aucune image enregistrée pour exporter le clip vidéo.");
        }

        int frameFps = fps > 0 ? fps : 10;
        int rawW = frames.get(0).getWidth();
        int rawH = frames.get(0).getHeight();
        int evenW = rawW & ~1;
        int evenH = rawH & ~1;

        SeekableByteChannel out = NIOUtils.writableChannel(outFile);
        MP4Muxer muxer = MP4Muxer.createMP4MuxerToChannel(out);

        MuxerTrack videoTrack = muxer.addVideoTrack(Codec.H264, VideoCodecMeta.createSimpleVideoCodecMeta(new Size(evenW, evenH), ColorSpace.YUV420J));
        H264Encoder encoder = H264Encoder.createH264Encoder();
        ByteBuffer videoBuf = ByteBuffer.allocate(evenW * evenH * 3);

        for (int i = 0; i < frames.size(); i++) {
            BufferedImage frame = ensureEvenDimensions(frames.get(i));
            Picture pic = AWTUtil.fromBufferedImage(frame, ColorSpace.YUV420J);
            videoBuf.clear();
            VideoEncoder.EncodedFrame encoded = encoder.encodeFrame(pic, videoBuf);
            videoTrack.addFrame(Packet.createPacket(
                encoded.getData(),
                i * 1000,
                10000,
                1000,
                i,
                encoded.isKeyFrame() ? Packet.FrameType.KEY : Packet.FrameType.INTER,
                null
            ));
        }

        if (pcmAudioData != null && pcmAudioData.length > 0) {
            org.jcodec.common.AudioFormat audioFormat = new org.jcodec.common.AudioFormat(22050, 16, 1, true, false);
            PCMMP4MuxerTrack audioTrack = muxer.addPCMAudioTrack(audioFormat);
            audioTrack.addSamples(ByteBuffer.wrap(pcmAudioData));
        }

        muxer.finish();
        out.close();

        return outFile;
    }

    /**
     * Export video frames as Animated GIF.
     */
    public static File exportGifVideoClip(List<BufferedImage> frames, String scenarioName, int frameDelayMs) throws Exception {
        String cleanScenario = sanitizeFilename(scenarioName);
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String fileName = cleanScenario + "_video_" + timestamp + ".gif";

        File outputDir = new File("captures/videos");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File outFile = new File(outputDir, fileName);

        if (frames.isEmpty()) {
            throw new IllegalArgumentException("Aucune image enregistrée pour exporter le clip vidéo.");
        }

        int imgType = frames.get(0).getType();
        if (imgType == BufferedImage.TYPE_CUSTOM || imgType == 0) {
            imgType = BufferedImage.TYPE_INT_ARGB;
        }

        try (ImageOutputStream output = new FileImageOutputStream(outFile)) {
            GifSequenceWriter writer = new GifSequenceWriter(output, imgType, frameDelayMs, true);
            for (BufferedImage frame : frames) {
                writer.writeToSequence(frame);
            }
            writer.close();
        }

        return outFile;
    }

    public static BufferedImage convertToBufferedImage(WritableImage fxImage) {
        int w = (int) fxImage.getWidth();
        int h = (int) fxImage.getHeight();
        BufferedImage bImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        PixelReader reader = fxImage.getPixelReader();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                bImage.setRGB(x, y, reader.getArgb(x, y));
            }
        }
        return bImage;
    }

    private static BufferedImage ensureEvenDimensions(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int evenW = w & ~1;
        int evenH = h & ~1;
        if (w == evenW && h == evenH && img.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            return img;
        }
        BufferedImage evenImg = new BufferedImage(evenW, evenH, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = evenImg.createGraphics();
        g.drawImage(img, 0, 0, evenW, evenH, null);
        g.dispose();
        return evenImg;
    }
}
