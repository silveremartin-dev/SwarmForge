/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.jme3.util.BufferUtils;
import javafx.application.Platform;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * JMonkeyEngine application rendering to an image buffer for JavaFX.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class JmeGameApp extends SimpleApplication {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(JmeGameApp.class);

    final int width;
    final int height;
    private final ConcurrentLinkedQueue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();

    // JavaFX image transfer
    private WritableImage targetImage;
    private ByteBuffer pixelBuffer;
    private byte[] pixelData;
    private byte[] transferData;
    private final java.util.concurrent.atomic.AtomicBoolean isTransferring = new java.util.concurrent.atomic.AtomicBoolean(false);
    private final java.util.concurrent.atomic.AtomicBoolean isStopped = new java.util.concurrent.atomic.AtomicBoolean(false);

    // Recording
    private boolean recording = false;
    private int recordingFrameCount = 0;
    private long lastRecordTime = 0;
    private final java.io.File recordingDir = new java.io.File("exports/timelapse");

    org.swarmforge.core.simulation.Simulation simulation;
    private org.swarmforge.client.network.SimulationClient networkClient;
    final java.util.Map<String, com.jme3.scene.Spatial> antVisuals = new java.util.HashMap<>();
    InsectVisualizer insectVisualizer;
    TunnelVisualizer tunnelVisualizer;
    Node terrainNode;
    String currentTool = "View Mode";
    TerrainModificationListener terrainListener;
    PheromoneVisualizer pheromoneVisualizer;
    WeatherVisualizer weatherVisualizer;
    VegetationVisualizer vegetationVisualizer;
    DirectionalLight sunLight;
    private float antVisualScaleMultiplier = 1.0f;
    org.swarmforge.client.ui.WorldEditorPane.RenderMode currentRenderMode = org.swarmforge.client.ui.WorldEditorPane.RenderMode.REALISTIC;
    boolean isGamifiedVoxelMode = false;
    Node selectionReticleNode;
    private float reticleAnimationTimer = 0.0f;

    private final JmeViewportInteractionController interactionController = new JmeViewportInteractionController(this);
    private final JmeSceneBuilderHelper sceneBuilder = new JmeSceneBuilderHelper(this);
    private final JmeAntVisualsUpdater antVisualsUpdater = new JmeAntVisualsUpdater(this);
    private final JmeLayerAndRenderModeController layerController = new JmeLayerAndRenderModeController(this);

    public org.swarmforge.client.network.SimulationClient getNetworkClient() {
        return networkClient;
    }

    public boolean isAntsVisible() {
        return layerController.antsVisible;
    }

    public float getAntVisualScaleMultiplier() {
        return antVisualScaleMultiplier;
    }

    public JmeSceneBuilderHelper getSceneBuilder() {
        return sceneBuilder;
    }

    public enum CameraFollowMode { FREE, TPS, FPS }
    private CameraFollowMode cameraFollowMode = CameraFollowMode.FREE;

    public void setCameraFollowMode(CameraFollowMode mode) {
        this.cameraFollowMode = mode != null ? mode : CameraFollowMode.FREE;
    }

    public CameraFollowMode getCameraFollowMode() {
        return cameraFollowMode;
    }

    public interface TerrainModificationListener {
        void onBlockChanged(int x, int y, int z, boolean added);
    }

    public interface ObjectSelectionListener {
        void onVoxelSelected(int x, int y, int z, String material, float moisture, float temp, float compaction);
        default void onIndividualSelected(String id, String caste, String stage, float health, float energy, float hunger, float age, String job) {}
        default void onChamberSelected(String chamberId) {}
        default void onHoverInfo(String text) {}
    }

    ObjectSelectionListener selectionListener;
    boolean isIndividualTrackingEnabled = true;

    public boolean isIndividualTrackingEnabled() {
        return isIndividualTrackingEnabled;
    }

    public void setIndividualTrackingEnabled(boolean enabled) {
        this.isIndividualTrackingEnabled = enabled;
        if (!enabled) {
            this.followedAntId = null;
        }
    }

    public void setSelectionListener(ObjectSelectionListener listener) {
        this.selectionListener = listener;
    }

    public void followIndividual(String individualId) {
        this.followedAntId = individualId;
    }

    public void setTerrainListener(TerrainModificationListener listener) {
        this.terrainListener = listener;
    }

    public JmeGameApp(int width, int height) {
        this.width = width;
        this.height = height;

        AppSettings settings = new AppSettings(true);
        settings.setWidth(width);
        settings.setHeight(height);
        settings.setFrameRate(60);
        settings.setRenderer(AppSettings.LWJGL_OPENGL33);
        settings.setAudioRenderer(null);
        setSettings(settings);
        setShowSettings(false);
        setPauseOnLostFocus(false);
    }

    public void setTargetImage(WritableImage image) {
        this.targetImage = image;
    }

    public void setSimulation(org.swarmforge.core.simulation.Simulation simulation) {
        this.simulation = simulation;
    }

    public void setNetworkClient(org.swarmforge.client.network.SimulationClient client) {
        this.networkClient = client;
    }

    public void setTool(String tool) {
        this.currentTool = tool;
    }

    @Override
    public void simpleInitApp() {
        try {
            setDisplayFps(false);
            setDisplayStatView(false);

            DirectionalLight sun = new DirectionalLight();
            sun.setDirection(new Vector3f(-0.5f, -0.5f, -0.5f).normalizeLocal());
            sun.setColor(new ColorRGBA(1.40f, 1.38f, 1.25f, 1.0f));
            rootNode.addLight(sun);
            this.sunLight = sun;

            com.jme3.light.AmbientLight al = new com.jme3.light.AmbientLight();
            al.setColor(new ColorRGBA(0.70f, 0.72f, 0.75f, 1.0f));
            rootNode.addLight(al);

            com.jme3.shadow.DirectionalLightShadowRenderer dlsr = new com.jme3.shadow.DirectionalLightShadowRenderer(
                    assetManager, 2048, 3);
            dlsr.setLight(sun);
            dlsr.setShadowIntensity(0.40f);
            dlsr.setEdgeFilteringMode(com.jme3.shadow.EdgeFilteringMode.PCF8);
            dlsr.setShadowZExtend(150f);
            viewPort.addProcessor(dlsr);

            if (flyCam != null) {
                flyCam.setEnabled(false);
                flyCam.setDragToRotate(true);
            }
            cam.setLocation(new Vector3f(32, 45, 65));
            cam.lookAt(new Vector3f(32, 10, 32), Vector3f.UNIT_Y);
            viewPort.setBackgroundColor(ColorRGBA.Black);

            this.terrainNode = new com.jme3.scene.Node("TerrainNode");
            rootNode.attachChild(terrainNode);

            this.vegetationVisualizer = new VegetationVisualizer(assetManager);
            this.vegetationVisualizer.setRenderMode(currentRenderMode);
            rootNode.attachChild(this.vegetationVisualizer.getRootNode());

            this.tunnelVisualizer = new TunnelVisualizer(assetManager);
            rootNode.attachChild(this.tunnelVisualizer.getRootNode());

            this.insectVisualizer = new InsectVisualizer(assetManager);
            antVisualsUpdater.initializeInstancing();

            this.weatherVisualizer = new WeatherVisualizer(assetManager, this.sunLight);
            rootNode.attachChild(this.weatherVisualizer.getRootNode());

            this.selectionReticleNode = interactionController.createSelectionReticle();

            pixelBuffer = BufferUtils.createByteBuffer(width * height * 4);
            pixelData = new byte[width * height * 4];
            transferData = new byte[width * height * 4];

            if (inputManager != null) {
                inputManager.addMapping("Click",
                        new com.jme3.input.controls.MouseButtonTrigger(com.jme3.input.MouseInput.BUTTON_LEFT));
                inputManager.addListener(interactionController.actionListener, "Click");

                inputManager.addMapping("Pan_Left", new com.jme3.input.controls.KeyTrigger(com.jme3.input.KeyInput.KEY_A));
                inputManager.addMapping("Pan_Right", new com.jme3.input.controls.KeyTrigger(com.jme3.input.KeyInput.KEY_D));
                inputManager.addMapping("Pan_Forward", new com.jme3.input.controls.KeyTrigger(com.jme3.input.KeyInput.KEY_W));
                inputManager.addMapping("Pan_Back", new com.jme3.input.controls.KeyTrigger(com.jme3.input.KeyInput.KEY_S));
                inputManager.addMapping("Rotate_Left", new com.jme3.input.controls.KeyTrigger(com.jme3.input.KeyInput.KEY_Q));
                inputManager.addMapping("Rotate_Right", new com.jme3.input.controls.KeyTrigger(com.jme3.input.KeyInput.KEY_E));
                inputManager.addMapping("Zoom_In", new com.jme3.input.controls.KeyTrigger(com.jme3.input.KeyInput.KEY_R));
                inputManager.addMapping("Zoom_Out", new com.jme3.input.controls.KeyTrigger(com.jme3.input.KeyInput.KEY_F));
                inputManager.addMapping("Center_Colony", new com.jme3.input.controls.KeyTrigger(com.jme3.input.KeyInput.KEY_C));
                inputManager.addMapping("Zoom_Wheel",
                        new com.jme3.input.controls.MouseAxisTrigger(com.jme3.input.MouseInput.AXIS_WHEEL, false));
                inputManager.addMapping("Zoom_Wheel_Neg",
                        new com.jme3.input.controls.MouseAxisTrigger(com.jme3.input.MouseInput.AXIS_WHEEL, true));

                inputManager.addListener(interactionController.cameraAnalogListener, "Pan_Left", "Pan_Right", "Pan_Forward", "Pan_Back",
                        "Rotate_Left", "Rotate_Right", "Zoom_In", "Zoom_Out", "Zoom_Wheel", "Zoom_Wheel_Neg");
                inputManager.addListener(interactionController.cameraActionListener, "Center_Colony");

                inputManager.addMapping("Toggle_Record",
                        new com.jme3.input.controls.KeyTrigger(com.jme3.input.KeyInput.KEY_F9));
                inputManager.addListener(recordListener, "Toggle_Record");
            }
        } catch (Throwable t) {
            LOG.error("Error during simpleInitApp: {}", t.getMessage(), t);
        }
    }

    private final com.jme3.input.controls.ActionListener recordListener = (name, isPressed, tpf) -> {
        if (name.equals("Toggle_Record") && !isPressed) {
            recording = !recording;
            if (recording) {
                System.out.println("Recording Started...");
                if (!recordingDir.exists())
                    recordingDir.mkdirs();
                recordingFrameCount = 0;
            } else {
                System.out.println("Recording Stopped.");
            }
        }
    };

    public String getFollowedIndividualId() {
        return followedAntId;
    }

    String followedAntId = null;

    public void focusOnPosition(float targetX, float targetY, float targetZ) {
        enqueueTask(() -> {
            if (cam != null) {
                cam.setLocation(new Vector3f(targetX, targetZ + 30, targetY + 30));
                cam.lookAt(new Vector3f(targetX, targetZ, targetY), Vector3f.UNIT_Y);
            }
        });
    }

    float slicePlaneRatio = 1.0f;
    boolean showSkirt = true;
    boolean showElevationIsolines = false;
    private long lastSliceRebuildMs = 0L;

    public void setSlicePlaneRatio(float ratio) {
        this.slicePlaneRatio = Math.max(0.05f, Math.min(1.0f, ratio));
        long now = System.currentTimeMillis();
        if (now - lastSliceRebuildMs < 40L) {
            return;
        }
        lastSliceRebuildMs = now;
        enqueueTask(() -> {
            if (vegetationVisualizer != null) {
                vegetationVisualizer.setSlicePlaneRatio(this.slicePlaneRatio);
            }
            rebuildTerrainMesh();
        });
    }

    public void setShowSkirt(boolean show) {
        this.showSkirt = show;
        enqueueTask(this::rebuildTerrainMesh);
    }

    public void setShowElevationIsolines(boolean show) {
        this.showElevationIsolines = show;
        enqueueTask(this::rebuildTerrainMesh);
    }

    public void setShowClimateIsolines(boolean show) {
        // Climate isolines handled via overlay/weather system
    }

    public void setShowPheromoneIsolines(boolean show) {
        enqueueTask(() -> {
            if (pheromoneVisualizer != null) {
                pheromoneVisualizer.setShowIsolines(show);
            }
        });
    }

    public void setTreeSpeciesComposition(int oak, int pine, int acacia, int cactus, int birch, int bamboo, int deadWood) {
        enqueueTask(() -> {
            if (vegetationVisualizer != null) {
                vegetationVisualizer.setTreeSpeciesComposition(oak, pine, acacia, cactus, birch, bamboo, deadWood);
                if (lastTerrarium != null) {
                    vegetationVisualizer.rebuildVegetation(lastTerrarium.getWidth(), lastTerrarium.getHeight(), lastTerrarium, simulation != null ? simulation.getVegetationSystem() : null);
                }
            }
        });
    }

    org.swarmforge.core.domain.Terrarium lastTerrarium;

    public void rebuildTerrainMesh() {
        if (simulation != null && simulation.getTerrarium() != null) {
            renderTerrarium(simulation.getTerrarium());
        } else if (lastTerrarium != null) {
            renderTerrarium(lastTerrarium);
        }
    }

    public void renderTerrarium(org.swarmforge.core.domain.Terrarium terrarium) {
        sceneBuilder.renderTerrarium(terrarium);
    }

    private boolean simulationPaused = false;

    public void setSimulationPaused(boolean paused) {
        this.simulationPaused = paused;
    }

    public boolean isSimulationPaused() {
        if (simulationPaused) return true;
        if (simulation == null) return true;
        return !simulation.isRunning()
                || simulation.getState() == org.swarmforge.core.simulation.Simulation.State.STOPPED
                || simulation.getState() == org.swarmforge.core.simulation.Simulation.State.PAUSED;
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (isStopped.get()) {
            return;
        }
        while (!taskQueue.isEmpty()) {
            Runnable task = taskQueue.poll();
            if (task != null) {
                try {
                    task.run();
                } catch (Throwable t) {
                    LOG.error("JME task error: {}", t.getMessage(), t);
                }
            }
        }

        boolean pausedOrStopped = isSimulationPaused();
        float effectiveTpf = pausedOrStopped ? 0.0f : tpf;

        if (simulation != null) {
            antVisualsUpdater.updateAntVisuals(effectiveTpf);
            sceneBuilder.updateEnvironmentVisuals(effectiveTpf);

            if (pheromoneVisualizer != null && simulation.getPheromoneGrid() != null) {
                try {
                    pheromoneVisualizer.update(simulation.getPheromoneGrid());
                } catch (Throwable t) {
                    System.err.println("[PheromoneVisualizer Error] " + t.getMessage());
                }
            }

            if (!pausedOrStopped) {
                reticleAnimationTimer += tpf;
            }
            if (selectionReticleNode != null) {
                if (followedAntId != null && antVisuals.containsKey(followedAntId)) {
                    com.jme3.scene.Spatial antSpatial = antVisuals.get(followedAntId);
                    if (antSpatial != null) {
                        selectionReticleNode.setCullHint(com.jme3.scene.Spatial.CullHint.Never);
                        Vector3f antPos = antSpatial.getWorldTranslation();
                        selectionReticleNode.setLocalTranslation(antPos.x, antPos.y + 0.08f, antPos.z);
                        if (!pausedOrStopped) {
                            selectionReticleNode.rotate(0, tpf * 2.2f, 0);
                        }
                        float pulse = 1.0f + 0.12f * (float) Math.sin(reticleAnimationTimer * 5.0f);
                        selectionReticleNode.setLocalScale(pulse);
                    } else {
                        selectionReticleNode.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
                    }
                } else {
                    selectionReticleNode.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
                }
            }

            if (followedAntId != null) {
                com.jme3.scene.Spatial ant = antVisuals.get(followedAntId);
                if (ant != null) {
                    Vector3f target = ant.getLocalTranslation();
                    com.jme3.math.Quaternion rot = ant.getLocalRotation();
                    Vector3f fwd = rot.mult(Vector3f.UNIT_Z);

                    if (cameraFollowMode == CameraFollowMode.FPS) {
                        Vector3f eyePos = target.add(fwd.mult(0.12f)).add(new Vector3f(0, 0.06f, 0));
                        cam.setLocation(eyePos);
                        cam.lookAt(eyePos.add(fwd.mult(15.0f)), Vector3f.UNIT_Y);
                    } else if (cameraFollowMode == CameraFollowMode.TPS) {
                        Vector3f chasePos = target.subtract(fwd.mult(5.0f)).add(new Vector3f(0, 2.8f, 0));
                        Vector3f camPos = cam.getLocation();
                        cam.setLocation(camPos.interpolateLocal(chasePos, tpf * 7.0f));
                        cam.lookAt(target.add(new Vector3f(0, 0.4f, 0)), Vector3f.UNIT_Y);
                    } else {
                        Vector3f offset = new Vector3f(0, 18, 18);
                        Vector3f camPos = cam.getLocation();
                        Vector3f desiredPos = target.add(offset);
                        cam.setLocation(camPos.interpolateLocal(desiredPos, tpf * 5.0f));
                        cam.lookAt(target, Vector3f.UNIT_Y);
                    }
                } else {
                    followedAntId = null;
                }
            }
        }
    }

    public void pick(double fxX, double fxY, double paneW, double paneH) { interactionController.pick(fxX, fxY, paneW, paneH); }
    final Vector3f cameraTarget = new Vector3f(32, 10, 32);
    boolean initialCameraConfigured = false;
    public void hover(double fxX, double fxY, double paneW, double paneH) { interactionController.hover(fxX, fxY, paneW, paneH); }
    public void rotateCamera(float x, float y) { interactionController.rotateCamera(x, y); }
    public void panCamera(float dx, float dy) { interactionController.panCamera(dx, dy); }
    public void zoomCamera(float delta) { zoomCamera(delta, width / 2.0, height / 2.0, width, height); }
    public void zoomCamera(float delta, double fxX, double fxY, double paneW, double paneH) { interactionController.zoomCamera(delta, fxX, fxY, paneW, paneH); }
    public void panCameraTo(float x, float y, float z) { interactionController.panCameraTo(x, y, z); }
    public void focusCameraOnWorldCoords(double x, double y) { panCameraTo((float) x, 15.0f, (float) y); }
    public void focusCameraOnWorldCoords(float x, float y) { panCameraTo(x, 15.0f, y); }
    public void resetCamera() { interactionController.resetCamera(); }
    public void setTopDownView() { interactionController.setTopDownView(); }

    @Override
    public void simpleRender(com.jme3.renderer.RenderManager rm) {
        if (isStopped.get()) return;
        if (targetImage != null) {
            if (isTransferring.compareAndSet(false, true)) {
                try {
                    renderer.readFrameBuffer(null, pixelBuffer);
                    pixelBuffer.rewind(); pixelBuffer.get(pixelData); pixelBuffer.clear();
                    for (int i = 0; i < pixelData.length; i += 4) {
                        byte r = pixelData[i]; pixelData[i] = pixelData[i + 2]; pixelData[i + 2] = r;
                    }
                    System.arraycopy(pixelData, 0, transferData, 0, pixelData.length);
                    Platform.runLater(() -> {
                        try {
                            if (targetImage != null) targetImage.getPixelWriter().setPixels(0, 0, width, height, PixelFormat.getByteBgraInstance(), transferData, 0, width * 4);
                        } finally { isTransferring.set(false); }
                    });
                } catch (Throwable t) { isTransferring.set(false); }
            }
            if (recording) {
                long now = System.currentTimeMillis();
                if (now - lastRecordTime > 100) {
                    lastRecordTime = now;
                    sceneBuilder.saveFrame(pixelData, width, height, recordingDir, recordingFrameCount++);
                }
            }
        }
    }

    public void enqueueTask(Runnable task) { taskQueue.add(task); }
    int lastColonySignature = 0;
    org.swarmforge.client.ui.WorldEditorPane.RenderMode lastNestRenderMode = null;
    public void setUVVisionMode(boolean enabled) { layerController.setUVVisionMode(enabled); }
    public boolean isUVVisionMode() { return layerController.isUVVisionMode(); }
    public void setOmmatidialVisionMode(boolean enabled) { layerController.setOmmatidialVisionMode(enabled); }
    public boolean isOmmatidialVisionMode() { return layerController.isOmmatidialVisionMode(); }
    public void setGamifiedVoxelMode(boolean gamified) { enqueueTask(() -> layerController.applyRenderModeInternal(gamified ? org.swarmforge.client.ui.WorldEditorPane.RenderMode.GAMIFIED : org.swarmforge.client.ui.WorldEditorPane.RenderMode.REALISTIC)); }
    public void setScientificMode(boolean scientific) { enqueueTask(() -> layerController.applyRenderModeInternal(scientific ? org.swarmforge.client.ui.WorldEditorPane.RenderMode.SCIENTIFIC : org.swarmforge.client.ui.WorldEditorPane.RenderMode.REALISTIC)); }
    public void setRenderMode(org.swarmforge.client.ui.WorldEditorPane.RenderMode mode) { enqueueTask(() -> layerController.applyRenderModeInternal(mode)); }
    public void setTerrainVisible(boolean visible) { layerController.setTerrainVisible(visible); }
    public void setTunnelsVisible(boolean visible) { layerController.setTunnelsVisible(visible); }
    public void setAntsVisible(boolean visible) { layerController.setAntsVisible(visible); }
    public void setPheromonesVisible(boolean visible) { layerController.setPheromonesVisible(visible); }
    public void setWeatherVisible(boolean visible) { layerController.setWeatherVisible(visible); }
    public void setVegetationVisible(boolean visible) { layerController.setVegetationVisible(visible); }
    public double getCameraDepth() { return cam != null ? Math.max(0.0, -cam.getLocation().y) : 0.0; }
    public void setInsectVisualScaleMultiplier(float multiplier) { this.antVisualScaleMultiplier = multiplier; }

    public void stopApp() {
        if (isStopped.compareAndSet(false, true)) {
            this.targetImage = null;
            try { this.stop(false); } catch (Throwable ignored) {}
        }
    }
}
