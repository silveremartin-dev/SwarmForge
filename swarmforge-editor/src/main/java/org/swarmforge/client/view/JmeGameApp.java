/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
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
import com.jme3.scene.Node;

import com.jme3.system.AppSettings;

import com.jme3.util.BufferUtils;
import javafx.application.Platform;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.List;

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

    // Recording
    private boolean recording = false;
    private int recordingFrameCount = 0;
    private long lastRecordTime = 0;
    private final java.io.File recordingDir = new java.io.File("exports/timelapse");

    private org.swarmforge.core.simulation.Simulation simulation;
    private org.swarmforge.client.network.SimulationClient networkClient;
    private java.util.Map<String, com.jme3.scene.Spatial> antVisuals = new java.util.HashMap<>();
    private AntVisualizer antVisualizer;
    private TunnelVisualizer tunnelVisualizer;
    private Node terrainNode;
    private String currentTool = "View Mode";
    private TerrainModificationListener terrainListener;
    private PheromoneVisualizer pheromoneVisualizer;
    private WeatherVisualizer weatherVisualizer;
    private VegetationVisualizer vegetationVisualizer;
    private DirectionalLight sunLight;
    private float antVisualScaleMultiplier = 1.0f;
    private org.swarmforge.client.ui.WorldEditorPane.RenderMode currentRenderMode = org.swarmforge.client.ui.WorldEditorPane.RenderMode.REALISTIC;

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
        void onAntSelected(String id, String caste, String stage, float health, float energy, float hunger, float age, String job);
        default void onChamberSelected(String chamberId) {}
    }

    private ObjectSelectionListener selectionListener;
    private boolean isAntTrackingEnabled = true;

    public boolean isAntTrackingEnabled() {
        return isAntTrackingEnabled;
    }

    public void setAntTrackingEnabled(boolean enabled) {
        this.isAntTrackingEnabled = enabled;
        if (!enabled) {
            this.followedAntId = null;
        }
    }

    public void setSelectionListener(ObjectSelectionListener listener) {
        this.selectionListener = listener;
    }

    public void followAnt(String antId) {
        this.followedAntId = antId;
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
        settings.setRenderer(AppSettings.LWJGL_OPENGL33); // Use modern OpenGL
        settings.setAudioRenderer(null); // Disable audio renderer to avoid OpenAL device initialization blockages
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
            // Disable JME built-in stats display (we have our own LIVE STATUS overlay)
            setDisplayFps(false);
            setDisplayStatView(false);

            DirectionalLight sun = new DirectionalLight();
            sun.setDirection(new Vector3f(-0.5f, -0.5f, -0.5f).normalizeLocal());
            sun.setColor(new ColorRGBA(1.35f, 1.30f, 1.18f, 1.0f));
            rootNode.addLight(sun);
            this.sunLight = sun;

            com.jme3.light.AmbientLight al = new com.jme3.light.AmbientLight();
            al.setColor(new ColorRGBA(0.55f, 0.58f, 0.65f, 1.0f));
            rootNode.addLight(al);

            // Enhanced Soft Directional Shadows
            com.jme3.shadow.DirectionalLightShadowRenderer dlsr = new com.jme3.shadow.DirectionalLightShadowRenderer(
                    assetManager, 2048, 3);
            dlsr.setLight(sun);
            dlsr.setShadowIntensity(0.40f);
            dlsr.setEdgeFilteringMode(com.jme3.shadow.EdgeFilteringMode.PCF8);
            dlsr.setShadowZExtend(150f);
            viewPort.addProcessor(dlsr);

            // Disable standard flyCam to use custom mouse control
            if (flyCam != null) {
                flyCam.setEnabled(false);
                flyCam.setDragToRotate(true);
            }
            cam.setLocation(new Vector3f(32, 45, 65));
            cam.lookAt(new Vector3f(32, 10, 32), Vector3f.UNIT_Y);
            viewPort.setBackgroundColor(ColorRGBA.Black);

            // Attach empty terrain node; terrain will be rendered when simulation or terrarium is loaded
            this.terrainNode = new com.jme3.scene.Node("TerrainNode");
            rootNode.attachChild(terrainNode);

            // Initialize visualizers
            this.vegetationVisualizer = new VegetationVisualizer(assetManager);
            this.vegetationVisualizer.setRenderMode(currentRenderMode);
            rootNode.attachChild(this.vegetationVisualizer.getRootNode());

            this.tunnelVisualizer = new TunnelVisualizer(assetManager);
            rootNode.attachChild(this.tunnelVisualizer.getRootNode());

            this.antVisualizer = new AntVisualizer(assetManager);
            initializeInstancing();

            this.weatherVisualizer = new WeatherVisualizer(assetManager, this.sunLight);
            rootNode.attachChild(this.weatherVisualizer.getRootNode());

            // Initialize 3D Target Spotlight Selection Reticle
            initSelectionReticle();

            // Initialize simple buffer
            pixelBuffer = BufferUtils.createByteBuffer(width * height * 4);
            pixelData = new byte[width * height * 4];

            // Input Mappings
            if (inputManager != null) {
                inputManager.addMapping("Click",
                        new com.jme3.input.controls.MouseButtonTrigger(com.jme3.input.MouseInput.BUTTON_LEFT));
                inputManager.addListener(actionListener, "Click");

                // Camera keyboard mappings
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

                inputManager.addListener(cameraAnalogListener, "Pan_Left", "Pan_Right", "Pan_Forward", "Pan_Back",
                        "Rotate_Left", "Rotate_Right", "Zoom_In", "Zoom_Out", "Zoom_Wheel", "Zoom_Wheel_Neg");
                inputManager.addListener(cameraActionListener, "Center_Colony");

                // Recording
                inputManager.addMapping("Toggle_Record",
                        new com.jme3.input.controls.KeyTrigger(com.jme3.input.KeyInput.KEY_F9));
                inputManager.addListener(recordListener, "Toggle_Record");
            }
        } catch (Throwable t) {
            System.err.println("[JME] Error during simpleInitApp: " + t.getMessage());
            t.printStackTrace();
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

    private final com.jme3.input.controls.ActionListener actionListener = new com.jme3.input.controls.ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("Click") && !isPressed) { // On Release
                // Ray Cast
                com.jme3.math.Vector2f click2d = inputManager.getCursorPosition();
                Vector3f click3d = cam.getWorldCoordinates(new com.jme3.math.Vector2f(click2d.x, click2d.y), 0f)
                        .clone();
                Vector3f dir = cam.getWorldCoordinates(new com.jme3.math.Vector2f(click2d.x, click2d.y), 1f)
                        .subtractLocal(click3d).normalizeLocal();
                com.jme3.math.Ray ray = new com.jme3.math.Ray(click3d, dir);

                com.jme3.collision.CollisionResults results = new com.jme3.collision.CollisionResults();
                rootNode.collideWith(ray, results);

                if (results.size() > 0) {
                    com.jme3.collision.CollisionResult closes = results.getClosestCollision();
                    Geometry geom = closes.getGeometry();

                    // Check for ant selection (both direct mesh hit and proximity raycasting)
                    if (isAntTrackingEnabled) {
                        for (int i = 0; i < results.size(); i++) {
                            Geometry hitGeom = results.getCollision(i).getGeometry();
                            com.jme3.scene.Spatial antSpatial = hitGeom;
                            while (antSpatial != null && antSpatial.getUserData("ID") == null) {
                                antSpatial = antSpatial.getParent();
                            }
                            if (antSpatial != null && antSpatial.getUserData("ID") != null) {
                                selectAndFollowAnt((String) antSpatial.getUserData("ID"), (String) antSpatial.getUserData("LifeStage"));
                                return;
                            }
                        }

                        // Proximity check to all active ants along ray (1.8m selection radius)
                        String closestAntId = null;
                        String closestAntStage = null;
                        float minRayDist = 1.8f;

                        for (java.util.Map.Entry<String, com.jme3.scene.Spatial> entry : antVisuals.entrySet()) {
                            com.jme3.scene.Spatial antSpatial = entry.getValue();
                            if (antSpatial != null && antSpatial.getCullHint() != com.jme3.scene.Spatial.CullHint.Always) {
                                Vector3f antPos = antSpatial.getWorldTranslation();
                                Vector3f v = antPos.subtract(click3d);
                                float proj = v.dot(dir);
                                if (proj > 0) {
                                    Vector3f closestPointOnRay = click3d.add(dir.mult(proj));
                                    float dist = antPos.distance(closestPointOnRay);
                                    if (dist < minRayDist) {
                                        minRayDist = dist;
                                        closestAntId = entry.getKey();
                                        closestAntStage = antSpatial.getUserData("LifeStage") != null ? (String) antSpatial.getUserData("LifeStage") : "ADULT";
                                    }
                                }
                            }
                        }

                        if (closestAntId != null) {
                            selectAndFollowAnt(closestAntId, closestAntStage);
                            return;
                        }
                    }

                    // Check if clicked spatial or parent is a Chamber Node
                    com.jme3.scene.Spatial chamberSpatial = geom;
                    while (chamberSpatial != null && chamberSpatial.getUserData("ChamberID") == null && (chamberSpatial.getName() == null || !chamberSpatial.getName().startsWith("Node_"))) {
                        chamberSpatial = chamberSpatial.getParent();
                    }
                    if (chamberSpatial != null) {
                        String chamberIdStr = (String) chamberSpatial.getUserData("ChamberID");
                        if (chamberIdStr == null && chamberSpatial.getName() != null && chamberSpatial.getName().startsWith("Node_")) {
                            chamberIdStr = chamberSpatial.getName().substring(5);
                        }
                        if (chamberIdStr != null && selectionListener != null) {
                            final String fChamberId = chamberIdStr;
                            Platform.runLater(() -> selectionListener.onChamberSelected(fChamberId));
                            return;
                        }
                    }

                    // Parse name "Voxel_x_y_z"
                    String[] parts = geom.getName().split("_");
                    if (parts.length == 4 && parts[0].equals("Voxel")) {
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);
                        int z = Integer.parseInt(parts[3]);

                        if (currentTool.equals("Remove Block")) {
                            // Visual update
                            if (terrainListener != null) {
                                terrainListener.onBlockChanged(x, y, z, false);
                            }
                            enqueueTask(() -> geom.removeFromParent());

                        } else if (currentTool.equals("Add Block")) {
                            Vector3f normal = closes.getContactNormal();
                            int ax = x + (int) Math.signum(normal.x);
                            int ay = y + (int) Math.signum(normal.y);
                            int az = z + (int) Math.signum(normal.z);

                            if (terrainListener != null) {
                                terrainListener.onBlockChanged(ax, ay, az, true);
                            }
                        } else {
                            // Inspection mode
                            if (selectionListener != null) {
                                String matName = "Humus / Sol Organique";
                                float moisture = 45.0f;
                                float temp = 18.5f;
                                float compaction = 65.0f;
                                if (simulation != null && simulation.getTerrarium() != null) {
                                    org.swarmforge.core.domain.TerrariumCell cell = simulation.getTerrarium().getCell(x, y, z);
                                    if (cell != null) {
                                        matName = cell.material().name();
                                        moisture = cell.humidity() * 100.0f;
                                        temp = cell.temperature();
                                    }
                                }
                                final String fMat = matName;
                                final float fM = moisture, fT = temp, fC = compaction;
                                Platform.runLater(() -> selectionListener.onVoxelSelected(x, y, z, fMat, fM, fT, fC));
                            }
                        }
                    }
                }
            }
        }
    };

    private void selectAndFollowAnt(String antId, String stage) {
        followedAntId = antId;
        String fStage = stage != null ? stage : "ADULT";
        System.out.println("Following Ant: " + followedAntId);
        if (selectionListener != null) {
            String roleName = "Ouvrière (Worker)";
            String taskName = "Forager";
            float health = 95.0f, energy = 88.0f, hydration = 80.0f, distanceTravelled = 450.0f;
            if (simulation != null && !simulation.getColonies().isEmpty()) {
                for (org.swarmforge.core.domain.Colony colony : simulation.getColonies()) {
                    for (org.swarmforge.core.domain.Individual ind : colony.getLivingIndividuals()) {
                        if (ind.getId().toString().equals(antId)) {
                            health = (float) ind.getEnergy();
                            energy = (float) ind.getEnergy();
                            if (ind.getCaste() != null) roleName = ind.getCaste().name();
                            break;
                        }
                    }
                }
            }
            final String finalRole = roleName;
            final String finalTask = taskName;
            final float finalHealth = health;
            final float finalEnergy = energy;
            final float finalHydration = hydration;
            final float finalDistance = distanceTravelled;
            Platform.runLater(() -> selectionListener.onAntSelected(antId, finalRole, fStage, finalHealth, finalEnergy, finalHydration, finalDistance, finalTask));
        }
    }

    public String getFollowedAntId() {
        return followedAntId;
    }

    private String followedAntId = null;

    private final com.jme3.input.controls.AnalogListener cameraAnalogListener = (name, value, tpf) -> {
        if (followedAntId != null && (name.startsWith("Pan") || name.startsWith("Rotate"))) {
            followedAntId = null; // Break follow on manual control
        }

        float panSpeed = 15f;
        float rotateSpeed = 1.5f;
        float zoomSpeed = 30f;

        switch (name) {
            case "Pan_Left" -> cam.setLocation(cam.getLocation().add(cam.getLeft().mult(panSpeed * tpf)));
            case "Pan_Right" -> cam.setLocation(cam.getLocation().add(cam.getLeft().mult(-panSpeed * tpf)));
            case "Pan_Forward" -> cam.setLocation(cam.getLocation().add(cam.getDirection().mult(panSpeed * tpf)));
            case "Pan_Back" -> cam.setLocation(cam.getLocation().add(cam.getDirection().mult(-panSpeed * tpf)));
            case "Rotate_Left" -> {
                com.jme3.math.Quaternion rot = new com.jme3.math.Quaternion().fromAngleAxis(rotateSpeed * tpf,
                        Vector3f.UNIT_Y);
                cam.setRotation(cam.getRotation().mult(rot));
            }
            case "Rotate_Right" -> {
                com.jme3.math.Quaternion rot = new com.jme3.math.Quaternion().fromAngleAxis(-rotateSpeed * tpf,
                        Vector3f.UNIT_Y);
                cam.setRotation(cam.getRotation().mult(rot));
            }
            case "Zoom_In", "Zoom_Wheel" ->
                cam.setLocation(cam.getLocation().add(cam.getDirection().mult(zoomSpeed * value)));
            case "Zoom_Out", "Zoom_Wheel_Neg" ->
                cam.setLocation(cam.getLocation().add(cam.getDirection().mult(-zoomSpeed * value)));
        }
    };

    private final com.jme3.input.controls.ActionListener cameraActionListener = (name, isPressed, tpf) -> {
        if (name.equals("Center_Colony") && isPressed) {
            // Center on first colony's nest
            if (simulation != null && !simulation.getColonies().isEmpty()) {
                org.swarmforge.core.domain.Colony colony = simulation.getColonies().get(0);
                float targetX = colony.getNestX();
                float targetY = colony.getNestY();
                float targetZ = colony.getNestZ();
                cam.setLocation(new Vector3f(targetX, targetZ + 30, targetY + 30));
                cam.lookAt(new Vector3f(targetX, targetZ, targetY), Vector3f.UNIT_Y);
            }
        }
    };

    public void focusOnPosition(float targetX, float targetY, float targetZ) {
        enqueueTask(() -> {
            if (cam != null) {
                cam.setLocation(new Vector3f(targetX, targetZ + 30, targetY + 30));
                cam.lookAt(new Vector3f(targetX, targetZ, targetY), Vector3f.UNIT_Y);
            }
        });
    };

    private float slicePlaneRatio = 1.0f;
    private boolean showSkirt = true;
    private boolean showElevationIsolines = false;

    public void setSlicePlaneRatio(float ratio) {
        this.slicePlaneRatio = Math.max(0.05f, Math.min(1.0f, ratio));
        enqueueTask(() -> {
            if (vegetationVisualizer != null) {
                vegetationVisualizer.setSlicePlaneRatio(this.slicePlaneRatio);
            }
            rebuildTerrainMesh();
        });
    }

    public void setShowSkirt(boolean show) {
        this.showSkirt = show;
        enqueueTask(() -> {
            rebuildTerrainMesh();
        });
    }

    public void setShowElevationIsolines(boolean show) {
        this.showElevationIsolines = show;
        enqueueTask(() -> {
            rebuildTerrainMesh();
        });
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

    private org.swarmforge.core.domain.Terrarium lastTerrarium;

    public void rebuildTerrainMesh() {
        if (simulation != null && simulation.getTerrarium() != null) {
            renderTerrarium(simulation.getTerrarium());
        } else if (lastTerrarium != null) {
            renderTerrarium(lastTerrarium);
        }
    }

    public void renderTerrarium(org.swarmforge.core.domain.Terrarium terrarium) {
        this.lastTerrarium = terrarium;
        enqueueTask(() -> {
            if (terrainNode != null) {
                terrainNode.removeFromParent();
            }
            terrainNode = new Node("Terrain");

            int w = terrarium.getWidth();
            int d = terrarium.getDepth();
            int h = terrarium.getHeight();

            Material soilMat;
            if (isGamifiedVoxelMode) {
                // GAMIFIED: Stylized voxel block texturing with nearest-neighbor pixel sampling
                soilMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
                soilMat.setBoolean("UseMaterialColors", true);
                soilMat.setBoolean("UseVertexColor", true);
                soilMat.setColor("Diffuse", ColorRGBA.White);
                soilMat.setColor("Ambient", new ColorRGBA(0.85f, 0.85f, 0.85f, 1f));
                try {
                    com.jme3.texture.Texture diffuseTex = assetManager.loadTexture("models/textures/pbr/Ground049A/Ground049A_1K-JPG_Color.jpg");
                    diffuseTex.setWrap(com.jme3.texture.Texture.WrapMode.Repeat);
                    diffuseTex.setMinFilter(com.jme3.texture.Texture.MinFilter.NearestNearestMipMap);
                    diffuseTex.setMagFilter(com.jme3.texture.Texture.MagFilter.Nearest);
                    soilMat.setTexture("DiffuseMap", diffuseTex);
                } catch (Exception ignored) {}
            } else {
                // REALISTIC: Lit with vertex color multiplied by diffuse texture
                soilMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
                soilMat.setBoolean("UseMaterialColors", true);
                soilMat.setBoolean("UseVertexColor", true);
                soilMat.setColor("Diffuse", ColorRGBA.White);
                soilMat.setColor("Ambient", new ColorRGBA(0.55f, 0.58f, 0.62f, 1f));
                soilMat.setColor("Specular", new ColorRGBA(0.08f, 0.08f, 0.08f, 1f));
                soilMat.setFloat("Shininess", 3f);

                // Pick dominant substrate texture from terrarium top-layer material distribution
                double lat = Math.abs(terrarium.getLatitude());
                int sandCount = 0, rockCount = 0, snowCount = 0, grassCount = 0;
                int sampleStep = Math.max(1, terrarium.getWidth() / 16);
                for (int sx = 0; sx < terrarium.getWidth(); sx += sampleStep) {
                    for (int sy = 0; sy < terrarium.getHeight(); sy += sampleStep) {
                        float elev = terrarium.getSurfaceElevation(sx, sy);
                        int sz = Math.max(0, Math.min(terrarium.getDepth() - 1, (int) elev));
                        org.swarmforge.core.domain.TerrariumCell cell = terrarium.getCell(sx, sy, sz);
                        if (cell != null) {
                            switch (cell.material()) {
                                case SAND -> sandCount++;
                                case ROCK, GRAVEL -> rockCount++;
                                default -> grassCount++;
                            }
                        }
                    }
                }
                if (lat > 60.0) snowCount = 999; // polar override

                String pbrFolder;
                if (snowCount > 0) {
                    pbrFolder = "Ground061"; // Alpine / snow
                } else if (sandCount > grassCount && sandCount > rockCount) {
                    pbrFolder = "Ground025"; // Desert / sand
                } else if (rockCount > grassCount) {
                    pbrFolder = "Ground037"; // Rocky / humus
                } else {
                    pbrFolder = "Ground049A"; // Default: temperate grass
                }

                try {
                    com.jme3.texture.Texture diffuseTex = assetManager.loadTexture(
                        "models/textures/pbr/" + pbrFolder + "/" + pbrFolder + "_1K-JPG_Color.jpg");
                    diffuseTex.setWrap(com.jme3.texture.Texture.WrapMode.Repeat);
                    diffuseTex.setMinFilter(com.jme3.texture.Texture.MinFilter.Trilinear);
                    diffuseTex.setMagFilter(com.jme3.texture.Texture.MagFilter.Bilinear);
                    soilMat.setTexture("DiffuseMap", diffuseTex);
                } catch (Exception e) {
                    try {
                        com.jme3.texture.Texture fallbackTex = assetManager.loadTexture(
                            "models/textures/pbr/Ground049A/Ground049A_1K-JPG_Color.jpg");
                        fallbackTex.setWrap(com.jme3.texture.Texture.WrapMode.Repeat);
                        soilMat.setTexture("DiffuseMap", fallbackTex);
                    } catch (Exception ignored) {}
                }
                try {
                    com.jme3.texture.Texture normalTex = assetManager.loadTexture(
                        "models/textures/pbr/" + pbrFolder + "/" + pbrFolder + "_1K-JPG_NormalGL.jpg");
                    normalTex.setWrap(com.jme3.texture.Texture.WrapMode.Repeat);
                    soilMat.setTexture("NormalMap", normalTex);
                } catch (Exception ignored) {}
            }

            TerrainMeshGenerator generator = new TerrainMeshGenerator();
            com.jme3.scene.Mesh terrainMesh = generator.generateMesh(terrarium, slicePlaneRatio, showSkirt, isGamifiedVoxelMode);
            Geometry terrainGeom = new Geometry("TerrainMesh", terrainMesh);
            terrainGeom.setMaterial(soilMat);
            terrainGeom.setShadowMode(com.jme3.renderer.queue.RenderQueue.ShadowMode.Receive);
            terrainNode.attachChild(terrainGeom);

            // 3D Elevation Isolines Contour Mesh
            if (showElevationIsolines) {
                com.jme3.scene.Mesh isoMesh = generator.generateIsolinesMesh(terrarium, slicePlaneRatio, 2.0f);
                if (isoMesh != null) {
                    Geometry isoGeom = new Geometry("ElevationIsolines", isoMesh);
                    Material isoMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    isoMat.setColor("Color", new ColorRGBA(0.35f, 0.85f, 1.0f, 0.9f));
                    isoGeom.setMaterial(isoMat);
                    terrainNode.attachChild(isoGeom);
                }
            }

            rootNode.attachChild(terrainNode);

            if (vegetationVisualizer != null) {
                vegetationVisualizer.setLatitude(terrarium.getLatitude());
                vegetationVisualizer.rebuildVegetation(w, h, terrarium, simulation != null ? simulation.getVegetationSystem() : null);
            }

            if (tunnelVisualizer != null) {
                tunnelVisualizer.setTerrarium(terrarium);
                tunnelVisualizer.setTerrainDimensions(10.0f, w);
            }
            if (antVisualizer != null) {
                antVisualizer.setTerrainDimensions(10.0f, w);
            }

            // Initialize Pheromone Visualizer aligned with exact ground surface elevation
            float surfaceY = terrarium.getSurfaceElevation(w / 2f, h / 2f);
            if (pheromoneVisualizer == null) {
                pheromoneVisualizer = new PheromoneVisualizer(assetManager);
                pheromoneVisualizer.initialize(w, h, surfaceY);
                rootNode.attachChild(pheromoneVisualizer.getRootNode());
            } else {
                pheromoneVisualizer.initialize(w, h, surfaceY);
            }

            // Auto-Focus & Camera Centering on Colony Nest or World Center
            float targetX = w / 2f;
            float targetZ = h / 2f;
            float targetY = surfaceY;
            if (simulation != null && !simulation.getColonies().isEmpty()) {
                org.swarmforge.core.domain.Colony colony = simulation.getColonies().get(0);
                targetX = colony.getNestX();
                targetZ = colony.getNestY();
                targetY = terrarium.getSurfaceElevation(targetX, targetZ);
            }
            cameraTarget.set(targetX, targetY, targetZ);
            cam.setLocation(new Vector3f(targetX, targetY + 16.0f, targetZ + 22.0f));
            cam.lookAt(new Vector3f(targetX, targetY + 0.5f, targetZ), Vector3f.UNIT_Y);
        });
    }

    @Override
    public void simpleUpdate(float tpf) {
        // Process tasks from JavaFX thread
        while (!taskQueue.isEmpty()) {
            taskQueue.poll().run();
        }

        // Update Simulation Visuals
        if (simulation != null) {
            updateAntVisuals(tpf);
            updateEnvironmentVisuals(tpf);

            if (pheromoneVisualizer != null && simulation.getPheromoneGrid() != null) {
                pheromoneVisualizer.update(simulation.getPheromoneGrid());
            }

            // 3D Ant Selection & Target Spotlight Reticle Animation
            reticleAnimationTimer += tpf;
            if (selectionReticleNode != null) {
                if (followedAntId != null && antVisuals.containsKey(followedAntId)) {
                    com.jme3.scene.Spatial antSpatial = antVisuals.get(followedAntId);
                    if (antSpatial != null) {
                        selectionReticleNode.setCullHint(com.jme3.scene.Spatial.CullHint.Never);
                        Vector3f antPos = antSpatial.getWorldTranslation();
                        selectionReticleNode.setLocalTranslation(antPos.x, antPos.y + 0.08f, antPos.z);
                        selectionReticleNode.rotate(0, tpf * 2.2f, 0);
                        float pulse = 1.0f + 0.12f * (float) Math.sin(reticleAnimationTimer * 5.0f);
                        selectionReticleNode.setLocalScale(pulse);
                    } else {
                        selectionReticleNode.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
                    }
                } else {
                    selectionReticleNode.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
                }
            }

            // Camera Follow (FREE, TPS, FPS)
            if (followedAntId != null) {
                com.jme3.scene.Spatial ant = antVisuals.get(followedAntId);
                if (ant != null) {
                    Vector3f target = ant.getLocalTranslation();
                    com.jme3.math.Quaternion rot = ant.getLocalRotation();
                    Vector3f fwd = rot.mult(Vector3f.UNIT_Z);

                    if (cameraFollowMode == CameraFollowMode.FPS) {
                        // First-Person ground perspective directly through ant eye level
                        Vector3f eyePos = target.add(fwd.mult(0.12f)).add(new Vector3f(0, 0.06f, 0));
                        cam.setLocation(eyePos);
                        cam.lookAt(eyePos.add(fwd.mult(15.0f)), Vector3f.UNIT_Y);
                    } else if (cameraFollowMode == CameraFollowMode.TPS) {
                        // Third-Person chase cam positioned behind and above ant
                        Vector3f chasePos = target.subtract(fwd.mult(5.0f)).add(new Vector3f(0, 2.8f, 0));
                        Vector3f camPos = cam.getLocation();
                        cam.setLocation(camPos.interpolateLocal(chasePos, tpf * 7.0f));
                        cam.lookAt(target.add(new Vector3f(0, 0.4f, 0)), Vector3f.UNIT_Y);
                    } else {
                        // Smooth overhead free follow
                        Vector3f offset = new Vector3f(0, 18, 18);
                        Vector3f camPos = cam.getLocation();
                        Vector3f desiredPos = target.add(offset);
                        cam.setLocation(camPos.interpolateLocal(desiredPos, tpf * 5.0f));
                        cam.lookAt(target, Vector3f.UNIT_Y);
                    }
                } else {
                    followedAntId = null; // Lost (died/despawned)
                }
            }
        }
    }

    private void updateAntVisuals(float tpf) {
        if (antVisualizer == null) {
            antVisualizer = new AntVisualizer(assetManager);
            initializeInstancing();
        }

        if (simulation != null && simulation.getTerrarium() != null) {
            antVisualizer.setTerrainDimensions(10.0f, simulation.getTerrarium().getWidth());
        }

        java.util.Set<String> activeIds = new java.util.HashSet<>();

        // Logic to update instanced geometries
        // Note: For InstancedNode, we modify the Geometry's transform.
        // InstancedNode automatically updates.

        List<org.swarmforge.protocol.grpc.IndividualDelta> individuals = (networkClient != null
                && networkClient.isConnected())
                        ? networkClient.getLatestIndividuals()
                        : java.util.Collections.emptyList();

        if (!individuals.isEmpty()) {
            for (org.swarmforge.protocol.grpc.IndividualDelta ind : individuals) {
                String id = ind.getId();
                org.swarmforge.protocol.grpc.IndividualDelta.LifeStage protoStage = ind.getLifeStage();
                org.swarmforge.core.domain.Individual.LifeStage lifeStage = org.swarmforge.core.domain.Individual.LifeStage
                        .valueOf(protoStage.name());
                updateSingleAntVisual(id, org.swarmforge.core.domain.Individual.Caste.WORKER, lifeStage,
                        ind.getPosition().getX(), ind.getPosition().getZ(), ind.getPosition().getY(), ind.getHeading(), activeIds, null);
            }
        } else if (simulation != null) {
            for (org.swarmforge.core.domain.Colony colony : simulation.getColonies()) {
                for (org.swarmforge.core.domain.Individual ind : colony.getLivingIndividuals()) {
                    String id = ind.getId().toString();
                    org.swarmforge.core.domain.Individual.Caste caste = ind.getCaste() != null ? ind.getCaste() : org.swarmforge.core.domain.Individual.Caste.WORKER;
                    org.swarmforge.core.domain.Individual.LifeStage stage = ind.getLifeStage() != null ? ind.getLifeStage() : org.swarmforge.core.domain.Individual.LifeStage.ADULT;
                    updateSingleAntVisual(id, caste, stage, ind.getX(), ind.getZ(), ind.getY(), ind.getHeading(), activeIds, ind.getSpecies());
                }
            }
            if (simulation.getPredatorManager() != null) {
                for (org.swarmforge.core.domain.Predator pred : simulation.getPredatorManager().getPredators()) {
                    if (pred != null && pred.isAlive()) {
                        String id = "Predator_" + pred.getId();
                        updateSingleAntVisual(id, org.swarmforge.core.domain.Individual.Caste.SOLDIER, org.swarmforge.core.domain.Individual.LifeStage.ADULT,
                                pred.getX(), pred.getZ(), pred.getY(), pred.getHeading(), activeIds, null);
                    }
                }
            }
        }

        // Cleanup
        java.util.Iterator<java.util.Map.Entry<String, com.jme3.scene.Spatial>> it = antVisuals.entrySet().iterator();
        while (it.hasNext()) {
            java.util.Map.Entry<String, com.jme3.scene.Spatial> entry = it.next();
            if (!activeIds.contains(entry.getKey())) {
                entry.getValue().removeFromParent(); // Detach from InstancedNode
                it.remove();
            }
        }
    }

    public void pick(double fxX, double fxY, double paneW, double paneH) {
        enqueueTask(() -> {
            float jmeX = (float) ((fxX / Math.max(1.0, paneW)) * width);
            float jmeY = (float) ((1.0 - (fxY / Math.max(1.0, paneH))) * height);

            Vector3f click3d = cam.getWorldCoordinates(new com.jme3.math.Vector2f(jmeX, jmeY), 0f).clone();
            Vector3f dir = cam.getWorldCoordinates(new com.jme3.math.Vector2f(jmeX, jmeY), 1f).subtractLocal(click3d).normalizeLocal();
            com.jme3.math.Ray ray = new com.jme3.math.Ray(click3d, dir);

            com.jme3.collision.CollisionResults results = new com.jme3.collision.CollisionResults();
            rootNode.collideWith(ray, results);

            if (results.size() > 0) {
                com.jme3.collision.CollisionResult closes = results.getClosestCollision();
                Geometry geom = closes.getGeometry();

                // 1. Check if clicked spatial is an ANT
                if (isAntTrackingEnabled) {
                    com.jme3.scene.Spatial antSpatial = geom;
                    while (antSpatial != null && antSpatial.getUserData("ID") == null) {
                        antSpatial = antSpatial.getParent();
                    }

                    if (antSpatial != null && antSpatial.getUserData("ID") != null) {
                        followedAntId = (String) antSpatial.getUserData("ID");
                        String stage = antSpatial.getUserData("LifeStage") != null ? (String) antSpatial.getUserData("LifeStage") : "ADULT";
                        if (selectionListener != null) {
                            final String id = followedAntId;
                            final String fStage = stage;
                            Platform.runLater(() -> selectionListener.onAntSelected(id, "Ouvrière (Worker)", fStage, 95.0f, 88.0f, 12.0f, 450.0f, "Forager"));
                        }
                        return;
                    }
                }

                // 2. Check if clicked spatial is a Chamber Node
                com.jme3.scene.Spatial chamberSpatial = geom;
                while (chamberSpatial != null && chamberSpatial.getUserData("ChamberID") == null && (chamberSpatial.getName() == null || !chamberSpatial.getName().startsWith("Node_"))) {
                    chamberSpatial = chamberSpatial.getParent();
                }
                if (chamberSpatial != null) {
                    String chamberIdStr = (String) chamberSpatial.getUserData("ChamberID");
                    if (chamberIdStr == null && chamberSpatial.getName() != null && chamberSpatial.getName().startsWith("Node_")) {
                        chamberIdStr = chamberSpatial.getName().substring(5);
                    }
                    if (chamberIdStr != null && selectionListener != null) {
                        final String fChamberId = chamberIdStr;
                        Platform.runLater(() -> selectionListener.onChamberSelected(fChamberId));
                        return;
                    }
                }

                // 3. Check if clicked spatial is Terrain or Voxel
                Vector3f contact = closes.getContactPoint();
                if (contact != null && simulation != null && simulation.getTerrarium() != null && selectionListener != null) {
                    org.swarmforge.core.domain.Terrarium terr = simulation.getTerrarium();
                    int vx = Math.max(0, Math.min(terr.getWidth() - 1, Math.round(contact.x)));
                    int vz = Math.max(0, Math.min(terr.getDepth() - 1, Math.round(contact.y))); // In JME Y is vertical altitude
                    int vy = Math.max(0, Math.min(terr.getHeight() - 1, Math.round(contact.z))); // In JME Z is horizontal Y
                    org.swarmforge.core.domain.TerrariumCell cell = terr.getCell(vx, vy, vz);
                    String mat = cell != null ? cell.material().name() : "HUMUS";
                    float hum = cell != null ? cell.humidity() * 100.0f : 45.0f;
                    float temp = cell != null ? cell.temperature() : 19.0f;
                    float compaction = 60.0f;
                    final int fx = vx, fy = vy, fz = vz;
                    final String fMat = mat;
                    final float fHum = hum, fTemp = temp, fComp = compaction;
                    Platform.runLater(() -> selectionListener.onVoxelSelected(fx, fy, fz, fMat, fHum, fTemp, fComp));
                }
            }
        });
    }

    private void updateSingleAntVisual(String id, org.swarmforge.core.domain.Individual.Caste caste,
                                       org.swarmforge.core.domain.Individual.LifeStage lifeStage,
                                       float x, float y, float z, float heading,
                                       java.util.Set<String> activeIds,
                                       org.swarmforge.core.species.Species species) {
        if (!antsVisible) return;
        activeIds.add(id);

        com.jme3.scene.Geometry antGeom = (com.jme3.scene.Geometry) antVisuals.get(id);

        if (antGeom != null) {
            String currentStageName = (String) antGeom.getUserData("LifeStage");
            if (currentStageName != null && !currentStageName.equals(lifeStage.name())) {
                antGeom.removeFromParent();
                antVisuals.remove(id);
                antGeom = null;
            }
        }

        if (antGeom == null) {
            antGeom = antVisualizer.createOrganismGeometry(caste, lifeStage, species);
            antGeom.setUserData("LifeStage", lifeStage.name());
            antGeom.setUserData("ID", id);

            // Deterministic individual size polymorphism (±6% subtle variation around CasteTemplate average)
            int hash = id != null ? Math.abs(id.hashCode()) : 0;
            float variance = (1.0f + (((hash % 1000) / 1000.0f) - 0.5f) * 0.12f) * antVisualScaleMultiplier;
            antGeom.setLocalScale(variance);

            com.jme3.scene.Node node = antVisualizer.getInstancedNode(caste);
            if (node != null) {
                node.attachChild(antGeom);
            } else {
                rootNode.attachChild(antGeom);
            }
            antVisuals.put(id, antGeom);
        }

        // Ground elevation snapping for walking ants:
        // In JME coordinates, X is x, Y is altitude (vertical up), Z is z (Terrarium Y depth).
        if (simulation != null && simulation.getTerrarium() != null) {
            float surfaceElevation = simulation.getTerrarium().getSurfaceElevation(x, z);
            boolean isUndergroundChamber = false;
            if (y < surfaceElevation - 1.0f) {
                org.swarmforge.core.domain.TerrariumCell cell = simulation.getTerrarium().getCell((int) x, (int) z, (int) y);
                if (cell != null && cell.isPassable()) {
                    isUndergroundChamber = true;
                }
            }
            if (!isUndergroundChamber) {
                y = surfaceElevation + 0.5f + 0.05f;
            }
        }

        antGeom.setLocalTranslation(x, y, z);
        antGeom.setLocalRotation(new com.jme3.math.Quaternion().fromAngles(0, heading, 0));
    }

    private void initializeInstancing() {
        for (org.swarmforge.core.domain.Individual.Caste caste : org.swarmforge.core.domain.Individual.Caste.values()) {
            com.jme3.scene.Node node = new com.jme3.scene.Node("CasteNode_" + caste);
            antVisualizer.registerInstancedNode(caste, node);
            rootNode.attachChild(node);
        }
    }

    private final Vector3f cameraTarget = new Vector3f(32, 10, 32);

    // Camera Controls (Orbit around camera target)
    public void rotateCamera(float x, float y) {
        enqueueTask(() -> {
            Vector3f offset = cam.getLocation().subtract(cameraTarget);
            float dist = Math.max(0.5f, offset.length());

            com.jme3.math.Quaternion qYaw = new com.jme3.math.Quaternion().fromAngleAxis(-x * 0.006f, Vector3f.UNIT_Y);
            Vector3f camLeft = cam.getLeft();
            com.jme3.math.Quaternion qPitch = new com.jme3.math.Quaternion().fromAngleAxis(-y * 0.006f, camLeft);

            Vector3f newOffset = qYaw.mult(qPitch.mult(offset));
            // Prevent camera from flipping under ground or passing straight overhead
            if (newOffset.y < 0.2f) newOffset.y = 0.2f;
            cam.setLocation(cameraTarget.add(newOffset));
            cam.lookAt(cameraTarget, Vector3f.UNIT_Y);
        });
    }

    public void panCamera(float dx, float dy) {
        enqueueTask(() -> {
            Vector3f left = cam.getLeft().mult(dx * 0.08f);
            Vector3f up = cam.getUp().mult(dy * 0.08f);
            cameraTarget.addLocal(left).addLocal(up);
            cam.setLocation(cam.getLocation().add(left).add(up));
        });
    }

    public void zoomCamera(float delta) {
        enqueueTask(() -> {
            Vector3f toTarget = cameraTarget.subtract(cam.getLocation());
            float dist = toTarget.length();
            float move = delta * (dist * 0.08f + 0.4f);
            Vector3f dir = cam.getDirection().mult(move);
            if (delta > 0 && dist - move < 0.35f) {
                // Minimum distance clamp for macro ant inspection
                cam.setLocation(cameraTarget.subtract(cam.getDirection().mult(0.35f)));
            } else if (delta < 0 && dist + Math.abs(move) > 150.0f) {
                // Maximum distance clamp
                cam.setLocation(cameraTarget.subtract(cam.getDirection().mult(150.0f)));
            } else {
                cam.setLocation(cam.getLocation().add(dir));
            }
        });
    }

    /**
     * Pan camera to look at a specific world position.
     */
    public void panCameraTo(float x, float y, float z) {
        enqueueTask(() -> {
            cameraTarget.set(x, y, z);
            float distance = Math.max(10.0f, cam.getLocation().distance(cameraTarget));
            // Keep roughly the same viewing distance
            Vector3f newPos = cameraTarget.add(new Vector3f(0, distance * 0.6f, distance * 0.6f));
            cam.setLocation(newPos);
            cam.lookAt(cameraTarget, Vector3f.UNIT_Y);
        });
    }

    /**
     * Focus camera on 2D world coordinates (X, Y) from minimap or navigation controls.
     */
    public void focusCameraOnWorldCoords(double x, double y) {
        panCameraTo((float) x, 15.0f, (float) y);
    }

    public void focusCameraOnWorldCoords(float x, float y) {
        panCameraTo(x, 15.0f, y);
    }


    /**
     * Reset camera to default 3D perspective position.
     */
    public void resetCamera() {
        enqueueTask(() -> {
            if (simulation != null && simulation.getTerrarium() != null) {
                int w = simulation.getTerrarium().getWidth();
                int d = simulation.getTerrarium().getDepth();
                int h = simulation.getTerrarium().getHeight();
                cameraTarget.set(w / 2f, d / 2f, h / 2f);
                cam.setLocation(new Vector3f(w / 2f, d + 25, h + 25));
                cam.lookAt(cameraTarget, Vector3f.UNIT_Y);
            } else {
                cameraTarget.set(32, 10, 32);
                cam.setLocation(new Vector3f(32, 45, 65));
                cam.lookAt(cameraTarget, Vector3f.UNIT_Y);
            }
        });
    }

    /**
     * Set camera to direct top-down view overhead.
     */
    public void setTopDownView() {
        enqueueTask(() -> {
            int w = 64, d = 32, h = 64;
            if (simulation != null && simulation.getTerrarium() != null) {
                w = simulation.getTerrarium().getWidth();
                d = simulation.getTerrarium().getDepth();
                h = simulation.getTerrarium().getHeight();
            }
            cam.setLocation(new Vector3f(w / 2f, d + 45, h / 2f));
            cam.lookAt(new Vector3f(w / 2f, 0, h / 2f), Vector3f.UNIT_Z);
        });
    }

    @Override
    public void simpleRender(com.jme3.renderer.RenderManager rm) {
        // Post-render: Read pixels
        if (targetImage != null) {
            renderer.readFrameBuffer(null, pixelBuffer);

            // Invert Y axis (not handled here, JME does bottom-up, FX needs top-down...
            // usually need flip)
            // But let's assume it works for FX transfer.
            // For recording, we might need a flip.

            pixelBuffer.get(pixelData);
            pixelBuffer.clear();

            // Swap R and B channels: OpenGL framebuffer reads GL_RGBA, while JavaFX ByteBgra expects BGRA
            for (int i = 0; i < pixelData.length; i += 4) {
                byte r = pixelData[i];
                pixelData[i] = pixelData[i + 2];
                pixelData[i + 2] = r;
            }

            final byte[] sendData = pixelData.clone();
            Platform.runLater(() -> {
                if (targetImage != null) {
                    PixelWriter pw = targetImage.getPixelWriter();
                    pw.setPixels(0, 0, width, height, PixelFormat.getByteBgraInstance(),
                            sendData, 0, width * 4);
                }
            });

            // Timelapse Recording
            if (recording) {
                long now = System.currentTimeMillis();
                if (now - lastRecordTime > 100) { // Max 10 FPS
                    lastRecordTime = now;
                    saveFrame();
                }
            }
        }
    }

    private void saveFrame() {
        // Run IO in separate thread to avoid stalling render
        final byte[] frameData = pixelData.clone(); // Copy buffer
        final int fNum = recordingFrameCount++;

        Thread saveThread = new Thread(() -> {
            try {
                // Convert BGRA byte array to BufferedImage
                java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(width, height,
                        java.awt.image.BufferedImage.TYPE_INT_RGB);
                int[] intPixels = new int[width * height];

                // Convert bytes to ints (BGRA -> RGB, flip Y)
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int i = ((height - 1 - y) * width + x) * 4;
                        int b = frameData[i] & 0xFF;
                        int g = frameData[i + 1] & 0xFF;
                        int r = frameData[i + 2] & 0xFF;

                        int argb = (0xFF << 24) | (r << 16) | (g << 8) | b;
                        intPixels[y * width + x] = argb;
                    }
                }
                image.setRGB(0, 0, width, height, intPixels, 0, width);

                java.io.File file = new java.io.File(recordingDir, String.format("frame_%05d.png", fNum));
                javax.imageio.ImageIO.write(image, "png", file);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "SwarmForge-FrameSaver-" + fNum);
        saveThread.setDaemon(true);
        saveThread.start();
    }

    public void enqueueTask(Runnable task) {
        taskQueue.add(task);
    }

    private void updateEnvironmentVisuals(float tpf) {
        if (tunnelVisualizer == null) {
            tunnelVisualizer = new TunnelVisualizer(assetManager);
            rootNode.attachChild(tunnelVisualizer.getRootNode());
        }
        if (weatherVisualizer == null) {
            weatherVisualizer = new WeatherVisualizer(assetManager, sunLight);
            rootNode.attachChild(weatherVisualizer.getRootNode());
        }
        if (vegetationVisualizer == null) {
            vegetationVisualizer = new VegetationVisualizer(assetManager);
            if (simulation != null && simulation.getTerrarium() != null) {
                vegetationVisualizer.setLatitude(simulation.getTerrarium().getLatitude());
            }
            vegetationVisualizer.rebuildVegetation(
                simulation != null && simulation.getTerrarium() != null ? simulation.getTerrarium().getWidth() : 64,
                simulation != null && simulation.getTerrarium() != null ? simulation.getTerrarium().getHeight() : 64,
                simulation != null ? simulation.getTerrarium() : null,
                simulation != null ? simulation.getVegetationSystem() : null
            );
            rootNode.attachChild(vegetationVisualizer.getRootNode());
        }

        if (simulation != null) {
            if (simulation.getTerrarium() != null) {
                vegetationVisualizer.setLatitude(simulation.getTerrarium().getLatitude());
            }
            if (simulation.getSeasonManager() != null) {
                vegetationVisualizer.setSeason(simulation.getSeasonManager().getCurrentSeason());
            }
            for (org.swarmforge.core.domain.Colony colony : simulation.getColonies()) {
                if (colony.getTunnelNetwork() != null) {
                    tunnelVisualizer.update(colony.getTunnelNetwork());
                }

                // Check for 3D Beehive or Wasp Nest tree anchoring
                String spName = colony.getSpecies() != null ? colony.getSpecies().getCommonName().toLowerCase() : "";
                String arch = colony.getSpecies() != null && colony.getSpecies().getNestType() != null ? colony.getSpecies().getNestType().toUpperCase() : "";
                
                if (spName.contains("bee") || spName.contains("abeille") || spName.contains("apis") || arch.contains("BEEHIVE")) {
                    // Check if 3D beehive is already attached
                    String hiveName = "3D_Beehive_" + (int) colony.getNestX() + "_" + (int) colony.getNestY();
                    if (rootNode.getChild(hiveName) == null) {
                        vegetationVisualizer.renderBeehive(colony.getNestX(), colony.getNestY(), colony.getNestZ(), 45.0f);
                    }
                } else if (spName.contains("wasp") || spName.contains("guêpe") || spName.contains("vespula") || arch.contains("PAPER") || arch.contains("ARBOREAL")) {
                    vegetationVisualizer.ensureHostTreeForWaspNest(colony.getNestX(), colony.getNestY(), colony.getNestZ());
                }
            }
            if (simulation.getWeather() != null) {
                weatherVisualizer.update(simulation.getWeather(), tpf);
            }
            if (vegetationVisualizer != null) {
                vegetationVisualizer.update(simulation.getWeather(), tpf);
            }
        } else if (vegetationVisualizer != null) {
            vegetationVisualizer.update(null, tpf);
        }
    }

    public void setUVVisionMode(boolean enabled) {
        enqueueTask(() -> {
            if (vegetationVisualizer != null) {
                vegetationVisualizer.setUVVisionMode(enabled);
            }
        });
    }

    public boolean isUVVisionMode() {
        return vegetationVisualizer != null && vegetationVisualizer.isUVVisionMode();
    }

    private boolean isGamifiedVoxelMode = false;

    public void setGamifiedVoxelMode(boolean gamified) {
        this.isGamifiedVoxelMode = gamified;
        enqueueTask(() -> {
            applyRenderModeInternal(gamified ? org.swarmforge.client.ui.WorldEditorPane.RenderMode.GAMIFIED : org.swarmforge.client.ui.WorldEditorPane.RenderMode.REALISTIC);
        });
    }

    public void setScientificMode(boolean scientific) {
        enqueueTask(() -> {
            applyRenderModeInternal(scientific ? org.swarmforge.client.ui.WorldEditorPane.RenderMode.SCIENTIFIC : org.swarmforge.client.ui.WorldEditorPane.RenderMode.REALISTIC);
        });
    }

    public void setRenderMode(org.swarmforge.client.ui.WorldEditorPane.RenderMode mode) {
        enqueueTask(() -> {
            applyRenderModeInternal(mode);
        });
    }

    private void applyRenderModeInternal(org.swarmforge.client.ui.WorldEditorPane.RenderMode mode) {
        if (mode == null) mode = org.swarmforge.client.ui.WorldEditorPane.RenderMode.REALISTIC;
        this.isGamifiedVoxelMode = (mode == org.swarmforge.client.ui.WorldEditorPane.RenderMode.GAMIFIED);

        if (viewPort != null) {
            if (mode == org.swarmforge.client.ui.WorldEditorPane.RenderMode.GAMIFIED) {
                viewPort.setBackgroundColor(new ColorRGBA(0.22f, 0.47f, 0.88f, 1.0f)); // Minecraft vibrant sky
            } else if (mode == org.swarmforge.client.ui.WorldEditorPane.RenderMode.REALISTIC) {
                viewPort.setBackgroundColor(new ColorRGBA(0.48f, 0.68f, 0.85f, 1.0f)); // Natural atmosphere sky
            } else {
                viewPort.setBackgroundColor(ColorRGBA.Black); // Scientific dark background
            }
            if (sunLight != null) {
                if (mode == org.swarmforge.client.ui.WorldEditorPane.RenderMode.GAMIFIED) {
                    sunLight.setColor(new ColorRGBA(1.3f, 1.25f, 1.2f, 1.0f));
                } else if (mode == org.swarmforge.client.ui.WorldEditorPane.RenderMode.REALISTIC) {
                    sunLight.setColor(new ColorRGBA(1.35f, 1.30f, 1.18f, 1.0f));
                } else {
                    sunLight.setColor(ColorRGBA.White);
                }
            }
        }
        if (vegetationVisualizer != null) {
            vegetationVisualizer.setRenderMode(mode);
        }
        if (weatherVisualizer != null) {
            weatherVisualizer.setRenderMode(mode);
        }
        if (pheromoneVisualizer != null) {
            pheromoneVisualizer.setRenderMode(mode);
        }
        rebuildTerrainMesh();
    }

    private boolean terrainVisible = true;
    private boolean tunnelsVisible = true;
    private boolean antsVisible = true;
    private boolean pheromonesVisible = true;
    private boolean weatherVisible = true;

    public void setTerrainVisible(boolean visible) {
        this.terrainVisible = visible;
        enqueueTask(() -> {
            if (terrainNode != null) {
                if (visible) {
                    if (terrainNode.getParent() == null) rootNode.attachChild(terrainNode);
                } else {
                    terrainNode.removeFromParent();
                }
            }
        });
    }

    public void setTunnelsVisible(boolean visible) {
        this.tunnelsVisible = visible;
        enqueueTask(() -> {
            if (tunnelVisualizer != null && tunnelVisualizer.getRootNode() != null) {
                if (visible) {
                    if (tunnelVisualizer.getRootNode().getParent() == null) rootNode.attachChild(tunnelVisualizer.getRootNode());
                } else {
                    tunnelVisualizer.getRootNode().removeFromParent();
                }
            }
        });
    }

    public void setAntsVisible(boolean visible) {
        this.antsVisible = visible;
        enqueueTask(() -> {
            if (antVisualizer != null) {
                for (org.swarmforge.core.domain.Individual.Caste caste : org.swarmforge.core.domain.Individual.Caste.values()) {
                    com.jme3.scene.Node node = antVisualizer.getInstancedNode(caste);
                    if (node != null) {
                        if (visible) {
                            if (node.getParent() == null) rootNode.attachChild(node);
                        } else {
                            node.removeFromParent();
                        }
                    }
                }
            }
            if (!visible) {
                for (com.jme3.scene.Spatial ant : antVisuals.values()) {
                    ant.removeFromParent();
                }
                antVisuals.clear();
            }
        });
    }

    public void setPheromonesVisible(boolean visible) {
        this.pheromonesVisible = visible;
        enqueueTask(() -> {
            if (pheromoneVisualizer != null && pheromoneVisualizer.getRootNode() != null) {
                if (visible) {
                    if (pheromoneVisualizer.getRootNode().getParent() == null) rootNode.attachChild(pheromoneVisualizer.getRootNode());
                } else {
                    pheromoneVisualizer.getRootNode().removeFromParent();
                }
            }
        });
    }

    public void setWeatherVisible(boolean visible) {
        this.weatherVisible = visible;
        enqueueTask(() -> {
            if (weatherVisualizer != null && weatherVisualizer.getRootNode() != null) {
                if (visible) {
                    if (weatherVisualizer.getRootNode().getParent() == null) rootNode.attachChild(weatherVisualizer.getRootNode());
                } else {
                    weatherVisualizer.getRootNode().removeFromParent();
                }
            }
        });
    }

    private com.jme3.scene.Node selectionReticleNode;
    private float reticleAnimationTimer = 0.0f;

    private void initSelectionReticle() {
        selectionReticleNode = new com.jme3.scene.Node("SelectionReticle");

        // Outer Ring (Glowing Cyan)
        com.jme3.scene.shape.Torus outerTorus = new com.jme3.scene.shape.Torus(16, 24, 0.025f, 0.45f);
        com.jme3.scene.Geometry outerGeom = new com.jme3.scene.Geometry("OuterRing", outerTorus);
        com.jme3.material.Material matOuter = new com.jme3.material.Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matOuter.setColor("Color", new ColorRGBA(0.22f, 0.74f, 0.97f, 0.85f));
        matOuter.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        matOuter.getAdditionalRenderState().setDepthWrite(false);
        outerGeom.setMaterial(matOuter);
        outerGeom.rotate((float) Math.PI / 2f, 0, 0);
        selectionReticleNode.attachChild(outerGeom);

        // Inner Ring (Glowing Amber)
        com.jme3.scene.shape.Torus innerTorus = new com.jme3.scene.shape.Torus(16, 24, 0.018f, 0.28f);
        com.jme3.scene.Geometry innerGeom = new com.jme3.scene.Geometry("InnerRing", innerTorus);
        com.jme3.material.Material matInner = new com.jme3.material.Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matInner.setColor("Color", new ColorRGBA(0.96f, 0.62f, 0.04f, 0.90f));
        matInner.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        matInner.getAdditionalRenderState().setDepthWrite(false);
        innerGeom.setMaterial(matInner);
        innerGeom.rotate((float) Math.PI / 2f, 0, 0);
        selectionReticleNode.attachChild(innerGeom);

        // 4 Crosshairs
        for (int i = 0; i < 4; i++) {
            com.jme3.scene.shape.Box tickBox = new com.jme3.scene.shape.Box(0.015f, 0.015f, 0.07f);
            com.jme3.scene.Geometry tickGeom = new com.jme3.scene.Geometry("Tick_" + i, tickBox);
            tickGeom.setMaterial(matOuter);
            float angle = i * ((float) Math.PI / 2.0f);
            float dist = 0.45f;
            tickGeom.setLocalTranslation((float) Math.cos(angle) * dist, 0, (float) Math.sin(angle) * dist);
            tickGeom.rotate(0, angle, 0);
            selectionReticleNode.attachChild(tickGeom);
        }

        selectionReticleNode.setQueueBucket(com.jme3.renderer.queue.RenderQueue.Bucket.Transparent);
        selectionReticleNode.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        rootNode.attachChild(selectionReticleNode);
    }

    private boolean vegetationVisible = true;

    public void setVegetationVisible(boolean visible) {
        this.vegetationVisible = visible;
        enqueueTask(() -> {
            if (vegetationVisualizer != null && vegetationVisualizer.getRootNode() != null) {
                if (visible) {
                    if (vegetationVisualizer.getRootNode().getParent() == null) rootNode.attachChild(vegetationVisualizer.getRootNode());
                } else {
                    vegetationVisualizer.getRootNode().removeFromParent();
                }
            }
        });
    }

    public double getCameraDepth() {
        if (cam != null) {
            return Math.max(0.0, -cam.getLocation().y);
        }
        return 0.0;
    }

    public void setAntVisualScaleMultiplier(float multiplier) {
        this.antVisualScaleMultiplier = multiplier;
    }
}
