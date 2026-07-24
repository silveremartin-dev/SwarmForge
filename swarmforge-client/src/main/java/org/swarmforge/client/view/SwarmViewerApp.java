/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;

import com.jme3.scene.instancing.InstancedNode;
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
 * Optimized with Hardware Instancing for mass rendering.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class SwarmViewerApp extends SimpleApplication {

    private final ConcurrentLinkedQueue<Runnable> updateQueue = new ConcurrentLinkedQueue<>();
    // Keep track of geometries to update their transforms
    private final Map<String, Geometry> entities = new ConcurrentHashMap<>();
    
    // InstancedNode for optimized rendering (Hardware Instancing)
    private InstancedNode antNode;

    private Material antMaterial;
    private Material floorMaterial;
    private Box antMesh; // Shared mesh

    public SwarmViewerApp() {
        // Configure settings
        AppSettings settings = new AppSettings(true);
        settings.setTitle("SwarmForge 3D Viewport");
        settings.setResolution(1280, 720);
        settings.setVSync(true);
        settings.setSamples(0); // Optimization for mass rendering
        // Important: Enable Gamma Correction if needed, but not strictly for perf
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
        // Use Lighting definition for proper shading if available, but Unshaded is faster for tests
        antMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        antMaterial.setColor("Color", ColorRGBA.Red);
        // ENABLE INSTANCING
        antMaterial.setBoolean("UseInstancing", true);

        floorMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        floorMaterial.setColor("Color", ColorRGBA.DarkGray);

        // 4. Create Floor
        Box floorMesh = new Box(100f, 0.1f, 100f);
        Geometry floor = new Geometry("Floor", floorMesh);
        floor.setMaterial(floorMaterial);
        floor.setLocalTranslation(50, -0.1f, 50);
        rootNode.attachChild(floor);

        // 5. Ant Container (InstancedNode)
        antNode = new InstancedNode("Ants");
        rootNode.attachChild(antNode);
        
        // Pre-create shared mesh
        antMesh = new Box(0.4f, 0.2f, 0.6f);
    }

    @Override
    public void simpleUpdate(float tpf) {
        // Process updates from network thread
        Runnable task;
        boolean needsInstanceUpdate = false;
        
        // Limit number of updates per frame to avoid choking if queue is huge
        int updates = 0;
        int maxUpdates = 10000; 

        while (updates < maxUpdates && (task = updateQueue.poll()) != null) {
            task.run();
            needsInstanceUpdate = true;
            updates++;
        }
        
        // If we added/moved stuff, we might need to refresh instances if using InstancedNode
        // Note: For InstancedNode, simply moving the geometry children requires call to instance()
        // if the structure changed. But for simple translation updates of children, 
        // JME's InstancedNode might need a re-instance call to update buffers.
        // This is the trade-off. For 100k moving items, re-uploading the buffer is necessary.
        if (needsInstanceUpdate) {
            antNode.instance();
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
                geom = new Geometry(id, antMesh);
                geom.setMaterial(antMaterial);
                // Attach to InstancedNode
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
                geom.removeFromParent(); // Detach from InstancedNode
            }
        });
    }

    public void startViewer() {
        this.start();
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
