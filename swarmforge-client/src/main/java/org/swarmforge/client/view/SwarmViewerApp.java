/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.light.DirectionalLight;
import com.jme3.light.AmbientLight;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * JMonkeyEngine 3D Viewer for SwarmForge.
 * Runs in a separate window/thread from the JavaFX controller.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class SwarmViewerApp extends SimpleApplication {

    private final ConcurrentLinkedQueue<Runnable> updateQueue = new ConcurrentLinkedQueue<>();
    private final Map<String, Geometry> entities = new ConcurrentHashMap<>();
    private Node antNode;

    private Material antMaterial;
    private Material floorMaterial;

    public SwarmViewerApp() {
        // Configure settings
        AppSettings settings = new AppSettings(true);
        settings.setTitle("SwarmForge 3D Viewport");
        settings.setResolution(1280, 720);
        settings.setVSync(true);
        setSettings(settings);
    }

    @Override
    public void simpleInitApp() {
        // 1. Setup Lighting
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.5f, -1.0f, -0.5f).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);

        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(0.3f));
        rootNode.addLight(ambient);

        // 2. Setup Camera
        flyCam.setMoveSpeed(50f);
        cam.setLocation(new Vector3f(50, 60, 100));
        cam.lookAt(new Vector3f(50, 0, 50), Vector3f.UNIT_Y);

        // 3. Setup Materials
        antMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        antMaterial.setColor("Color", ColorRGBA.Red);

        floorMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        floorMaterial.setColor("Color", ColorRGBA.DarkGray);

        // 4. Create Floor
        Box floorMesh = new Box(100f, 0.1f, 100f);
        Geometry floor = new Geometry("Floor", floorMesh);
        floor.setMaterial(floorMaterial);
        floor.setLocalTranslation(50, -0.1f, 50);
        rootNode.attachChild(floor);

        // 5. Ant Container
        antNode = new Node("Ants");
        rootNode.attachChild(antNode);
    }

    @Override
    public void simpleUpdate(float tpf) {
        // Process updates from network thread
        Runnable task;
        while ((task = updateQueue.poll()) != null) {
            task.run();
        }
    }

    /**
     * Thread-safe method to update or create an entity.
     */
    public void updateEntity(String id, float x, float y, float z) {
        updateQueue.offer(() -> {
            Geometry geom = entities.get(id);
            if (geom == null) {
                // Create new ant
                Box b = new Box(0.4f, 0.2f, 0.6f);
                geom = new Geometry(id, b);
                geom.setMaterial(antMaterial);
                antNode.attachChild(geom);
                entities.put(id, geom);
            }
            // Update position
            geom.setLocalTranslation(x, y, z);
        });
    }

    /**
     * Thread-safe method to remove an entity.
     */
    public void removeEntity(String id) {
        updateQueue.offer(() -> {
            Geometry geom = entities.remove(id);
            if (geom != null) {
                geom.removeFromParent();
            }
        });
    }

    public void startViewer() {
        // Start JME in a new thread context implicitly handled by start()
        // But we want to call this from JavaFX thread without blocking it?
        // JME start() blocks if appType is not distinct.
        // Actually, start() spawns a thread.
        this.start();
    }

    @Override
    public void destroy() {
        super.destroy();
        // Notify JavaFX controller if needed
    }
}
