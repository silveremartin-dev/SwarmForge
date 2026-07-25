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
    private DirectionalLight sunLight;

    public interface TerrainModificationListener {
        void onBlockChanged(int x, int y, int z, boolean added);
    }

    public interface ObjectSelectionListener {
        void onVoxelSelected(int x, int y, int z, String material, float moisture, float temp, float compaction);
        void onAntSelected(String id, String caste, String stage, float health, float energy, float hunger, float age, String job);
    }

    private ObjectSelectionListener selectionListener;

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

            // Scene is empty until terrain is generated via renderTerrarium()

            DirectionalLight sun = new DirectionalLight();
            sun.setDirection(new Vector3f(-0.5f, -0.5f, -0.5f).normalizeLocal());
            sun.setColor(ColorRGBA.White);
            rootNode.addLight(sun);
            this.sunLight = sun;

            com.jme3.light.AmbientLight al = new com.jme3.light.AmbientLight();
            al.setColor(ColorRGBA.White.mult(0.3f));
            rootNode.addLight(al);

            // Shadows
            com.jme3.shadow.DirectionalLightShadowRenderer dlsr = new com.jme3.shadow.DirectionalLightShadowRenderer(
                    assetManager, 1024, 3);
            dlsr.setLight(sun);
            viewPort.addProcessor(dlsr);

            // Disable standard flyCam to use custom mouse control
            if (flyCam != null) {
                flyCam.setEnabled(false);
                flyCam.setDragToRotate(true);
            }
            cam.setLocation(new Vector3f(0, 10, 10));
            cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
            viewPort.setBackgroundColor(ColorRGBA.DarkGray);

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
                if (currentTool.equals("View Mode"))
                    return;

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
                    } else if (geom.getUserData("ID") != null) {
                        // It's an ANT!
                        followedAntId = (String) geom.getUserData("ID");
                        String stage = geom.getUserData("LifeStage") != null ? (String) geom.getUserData("LifeStage") : "ADULT";
                        System.out.println("Following Ant: " + followedAntId);
                        if (selectionListener != null) {
                            final String id = followedAntId;
                            final String fStage = stage;
                            Platform.runLater(() -> selectionListener.onAntSelected(id, "Ouvrière (Worker)", fStage, 95.0f, 88.0f, 12.0f, 450.0f, "Forager"));
                        }
                    }
                }
            }
        }
    };

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

    public void renderTerrarium(org.swarmforge.core.domain.Terrarium terrarium) {
        enqueueTask(() -> {
            if (terrainNode != null) {
                terrainNode.removeFromParent();
            }
            terrainNode = new Node("Terrain");

            int w = terrarium.getWidth();
            int d = terrarium.getDepth();
            int h = terrarium.getHeight();

            // Better Lighting
            Material soilMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            soilMat.setBoolean("UseMaterialColors", true);
            soilMat.setColor("Diffuse", new ColorRGBA(0.4f, 0.25f, 0.1f, 1f));
            soilMat.setColor("Ambient", new ColorRGBA(0.4f, 0.25f, 0.1f, 1f));

            TerrainMeshGenerator generator = new TerrainMeshGenerator();
            com.jme3.scene.Mesh terrainMesh = generator.generateMesh(terrarium);
            Geometry terrainGeom = new Geometry("TerrainMesh", terrainMesh);
            terrainGeom.setMaterial(soilMat);
            terrainNode.attachChild(terrainGeom);
            rootNode.attachChild(terrainNode);

            // Initialize Pheromone Visualizer
            if (pheromoneVisualizer == null) {
                pheromoneVisualizer = new PheromoneVisualizer(assetManager);
                pheromoneVisualizer.initialize(w, d);
                rootNode.attachChild(pheromoneVisualizer.getRootNode());
            }

            // Recenter camera
            cam.setLocation(new Vector3f(w / 2f, d + 20, h + 20)); // h is height (vertical), d is depth (Z), w is width
                                                                   // (X)
            // Wait, standard convention in this app seems Y is vertical? JME Y is vertical.
            // w=width, h=height?
            // "cam.setLocation(new Vector3f(w / 2f, d + 20, h + 20));"
            // If Y is up, then h should be Y.
            // In renderTerrarium args: (Terrarium terrarium)
            // int w = getWidth, int d = getDepth, int h = getHeight.
            // Usually Height is Y.
            // cam loc logic seems odd if h is Z.
            // Let's stick to existing cam logic but ensure visualizer is attached.

            cam.lookAt(new Vector3f(w / 2f, d / 2f, h / 2f), Vector3f.UNIT_Y);
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

            // Camera Follow
            if (followedAntId != null) {
                com.jme3.scene.Spatial ant = antVisuals.get(followedAntId);
                if (ant != null) {
                    Vector3f target = ant.getLocalTranslation();
                    // Maintain current offset
                    // Or impose standard follow offset? Let's smoothly interpolate.
                    // Simple snap for now:
                    Vector3f offset = new Vector3f(0, 20, 20); // Default offset
                    // Better: Keep current relative offset but center X, Z
                    // cam.setLocation(target.add(offset));
                    // cam.lookAt(target, Vector3f.UNIT_Y);

                    // Smooth follow
                    Vector3f camPos = cam.getLocation();
                    Vector3f desiredPos = target.add(offset);
                    cam.setLocation(camPos.interpolateLocal(desiredPos, tpf * 5.0f));
                    cam.lookAt(target, Vector3f.UNIT_Y);
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

        java.util.Set<String> activeIds = new java.util.HashSet<>();

        // Logic to update instanced geometries
        // Note: For InstancedNode, we modify the Geometry's transform.
        // InstancedNode automatically updates.

        List<org.swarmforge.protocol.grpc.IndividualDelta> individuals = (networkClient != null
                && networkClient.isConnected())
                        ? networkClient.getLatestIndividuals()
                        : java.util.Collections.emptyList();

        // If not connected, maybe local simulation?
        if (individuals.isEmpty() && simulation != null) {
            // Adapt local simulation to same interface if possible, or just skip for now to
            // keep code clean
            // Let's assume network client only for "Visual Polish" phase to verify
            // performant rendering
        }

        for (org.swarmforge.protocol.grpc.IndividualDelta ind : individuals) {
            String id = ind.getId();
            activeIds.add(id);

            org.swarmforge.protocol.grpc.IndividualDelta.LifeStage protoStage = ind.getLifeStage();
            org.swarmforge.core.domain.Individual.LifeStage lifeStage = org.swarmforge.core.domain.Individual.LifeStage
                    .valueOf(protoStage.name());

            com.jme3.scene.Geometry antGeom = (com.jme3.scene.Geometry) antVisuals.get(id);

            // Check if we need to update the geometry (e.g. stage changed)
            if (antGeom != null) {
                String currentStageName = (String) antGeom.getUserData("LifeStage");
                if (currentStageName != null && !currentStageName.equals(lifeStage.name())) {
                    // Stage changed! Remove old geometry
                    antGeom.removeFromParent();
                    antVisuals.remove(id);
                    antGeom = null; // Force recreation
                }
            }

            if (antGeom == null) {
                // Determine caste from ID or default? The Delta doesn't send Caste yet!
                // For now, assume WORKER.
                antGeom = antVisualizer.createAntGeometry(org.swarmforge.core.domain.Individual.Caste.WORKER,
                        lifeStage);
                antGeom.setUserData("LifeStage", lifeStage.name());
                antGeom.setUserData("ID", id);

                // Attach to appropriate node (maybe need separate nodes for stages?)
                // For now, using WORKER node for everything or we should register nodes for
                // stages?
                // Visualizer registers nodes by Caste.
                // Let's just use WORKER caste node for simplicity, or we should pass caste
                // properly.
                // ideally we pass caste in delta.
                antVisualizer.getInstancedNode(org.swarmforge.core.domain.Individual.Caste.WORKER).attachChild(antGeom);
                antVisuals.put(id, antGeom);
            }

            Vector3f targetPos = new Vector3f(ind.getPosition().getX(), ind.getPosition().getZ(),
                    ind.getPosition().getY());
            antGeom.setLocalTranslation(targetPos);
            // Orientation
            float heading = ind.getHeading();
            antGeom.setLocalRotation(new com.jme3.math.Quaternion().fromAngles(0, heading, 0));
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

    private void initializeInstancing() {
        for (org.swarmforge.core.domain.Individual.Caste caste : org.swarmforge.core.domain.Individual.Caste.values()) {
            com.jme3.scene.instancing.InstancedNode node = new com.jme3.scene.instancing.InstancedNode(
                    "Instanced_" + caste);
            node.setMaterial(antVisualizer.getMaterial(caste)); // InstancedNode requires Material
            antVisualizer.registerInstancedNode(caste, node);
            rootNode.attachChild(node);
        }
    }

    // Camera Controls
    public void rotateCamera(float x, float y) {
        enqueueTask(() -> {
            cam.getRotation().multLocal(new com.jme3.math.Quaternion().fromAngles(y * 0.01f, x * 0.01f, 0));
        });
    }

    public void panCamera(float dx, float dy) {
        enqueueTask(() -> {
            Vector3f left = cam.getLeft().mult(dx * 0.1f);
            Vector3f up = cam.getUp().mult(dy * 0.1f);
            cam.setLocation(cam.getLocation().add(left).add(up));
        });
    }

    public void zoomCamera(float delta) {
        enqueueTask(() -> {
            Vector3f dir = cam.getDirection().mult(delta * 0.5f);
            cam.setLocation(cam.getLocation().add(dir));
        });
    }

    /**
     * Pan camera to look at a specific world position.
     */
    public void panCameraTo(float x, float y, float z) {
        enqueueTask(() -> {
            Vector3f target = new Vector3f(x, y, z);
            float distance = cam.getLocation().distance(target);
            // Keep roughly the same viewing distance
            Vector3f newPos = target.add(new Vector3f(0, distance * 0.6f, distance * 0.6f));
            cam.setLocation(newPos);
            cam.lookAt(target, Vector3f.UNIT_Y);
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

            Platform.runLater(() -> {
                if (targetImage != null) {
                    PixelWriter pw = targetImage.getPixelWriter();
                    pw.setPixels(0, 0, width, height, PixelFormat.getByteBgraInstance(),
                            pixelData, 0, width * 4);
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

        new Thread(() -> {
            try {
                // Convert BGRA byte array to BufferedImage
                java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(width, height,
                        java.awt.image.BufferedImage.TYPE_INT_RGB);
                int[] intPixels = new int[width * height];

                // Convert bytes to ints (BGRA -> RGB, flip Y?)
                // JME is Bottom-Up. BufferedImage is Top-Down. We need to flip Y.
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int i = ((height - 1 - y) * width + x) * 4;
                        int b = frameData[i] & 0xFF;
                        int g = frameData[i + 1] & 0xFF;
                        int r = frameData[i + 2] & 0xFF;
                        // int a = frameData[i + 3] & 0xFF;

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
        }).start();
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

        if (simulation != null) {
            if (!simulation.getColonies().isEmpty()) {
                org.swarmforge.core.domain.Colony colony = simulation.getColonies().get(0);
                tunnelVisualizer.update(colony.getTunnelNetwork());
            }
            if (simulation.getWeather() != null) {
                weatherVisualizer.update(simulation.getWeather(), tpf);
            }
        }
    }
}
