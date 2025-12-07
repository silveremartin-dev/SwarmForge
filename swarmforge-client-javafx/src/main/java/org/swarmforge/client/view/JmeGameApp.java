/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.texture.Image;
import com.jme3.util.BufferUtils;
import javafx.application.Platform;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * JMonkeyEngine application rendering to an image buffer for JavaFX.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class JmeGameApp extends SimpleApplication {

    private final int width;
    private final int height;
    private final ConcurrentLinkedQueue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();

    // JavaFX image transfer
    private WritableImage targetImage;
    private ByteBuffer pixelBuffer;
    private byte[] pixelData;

    public JmeGameApp(int width, int height) {
        this.width = width;
        this.height = height;

        AppSettings settings = new AppSettings(true);
        settings.setWidth(width);
        settings.setHeight(height);
        settings.setFrameRate(60);
        settings.setCustomRenderer(AppSettings.LWJGL_OPENGL2); // Force compatibility
        // Offscreen settings
        // Note: Real offscreen usually requires passing a frame buffer,
        // but for this demo we'll just read from the main buffer.
        setSettings(settings);
        setShowSettings(false);
        setPauseOnLostFocus(false);
    }

    public void setTargetImage(WritableImage image) {
        this.targetImage = image;
    }

    @Override
    public void simpleInitApp() {
        // Setup scene
        Box box = new Box(1, 1, 1);
        Geometry geom = new Geometry("Box", box);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);
        rootNode.attachChild(geom);

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.5f, -0.5f, -0.5f).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);

        flyCam.setMoveSpeed(10);
        viewPort.setBackgroundColor(ColorRGBA.DarkGray);

        // Initialize simple buffer
        pixelBuffer = BufferUtils.createByteBuffer(width * height * 4);
        pixelData = new byte[width * height * 4];
    }

    @Override
    public void simpleUpdate(float tpf) {
        // Process tasks from JavaFX thread
        while (!taskQueue.isEmpty()) {
            taskQueue.poll().run();
        }
    }

    @Override
    public void simpleRender(com.jme3.renderer.RenderManager rm) {
        // Post-render: Read pixels
        if (targetImage != null) {
            renderer.readFrameBuffer(null, pixelBuffer);

            // Invert Y axis (OpenGL vs JavaFX) not handled here for simplicity
            // Just copying raw bytes for now
            pixelBuffer.get(pixelData);
            pixelBuffer.clear();

            Platform.runLater(() -> {
                if (targetImage != null) {
                    PixelWriter pw = targetImage.getPixelWriter();
                    // This is slow, but works for a demo
                    // Standard JME readFrameBuffer returns BGRA or RGBA depending on system
                    // We assume RGBA here
                    pw.setPixels(0, 0, width, height, PixelFormat.getByteBgraInstance(),
                            pixelData, 0, width * 4);
                }
            });
        }
    }

    public void enqueueTask(Runnable task) {
        taskQueue.add(task);
    }
}
