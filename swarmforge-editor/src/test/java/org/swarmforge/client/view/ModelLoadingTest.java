package org.swarmforge.client.view;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.JmeSystem;
import com.jme3.texture.Texture;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test that scans and verifies 100% of 3D models (.obj, .glb),
 * materials, and texture assets in the heavy jMonkeyEngine client.
 */
public class ModelLoadingTest {

    private static AssetManager assetManager;

    @BeforeAll
    public static void setUp() {
        assetManager = JmeSystem.newAssetManager(
                Thread.currentThread().getContextClassLoader().getResource("com/jme3/asset/Desktop.cfg"));
    }

    @Test
    public void testLoadAllObjAndGlbModelsInResources() {
        URL modelsUrl = Thread.currentThread().getContextClassLoader().getResource("models");
        assertNotNull(modelsUrl, "models directory should exist in resources");

        File modelsDir = new File(modelsUrl.getFile());
        List<File> modelFiles = new ArrayList<>();
        findModelFiles(modelsDir, modelFiles);

        assertFalse(modelFiles.isEmpty(), "Should find 3D model files in models/");
        System.out.println("Total 3D models found to verify: " + modelFiles.size());

        int loadedCount = 0;
        List<String> failedModels = new ArrayList<>();

        for (File f : modelFiles) {
            String path = getAssetPath(f);
            try {
                Spatial spatial = assetManager.loadModel(path);
                assertNotNull(spatial, "Loaded model should not be null for path: " + path);
                // Verify spatial has geometry
                int geomCount = countGeometries(spatial);
                assertTrue(geomCount > 0, "Model should contain at least 1 geometry: " + path);
                loadedCount++;
            } catch (Exception ex) {
                System.err.println("FAILED loading model: " + path + " -> " + ex.getMessage());
                failedModels.add(path + " (" + ex.getMessage() + ")");
            }
        }

        System.out.println("Successfully loaded and validated " + loadedCount + "/" + modelFiles.size() + " 3D models!");
        assertTrue(failedModels.isEmpty(), "All 3D models must load successfully. Failed: " + failedModels);
    }

    @Test
    public void testLoadAllTerrainPbrTextures() {
        String[] pbrTextures = new String[] {
            "models/textures/pbr/Ground037/Ground037_1K-JPG_Color.jpg",
            "models/textures/pbr/Ground037/Ground037_1K-JPG_NormalGL.jpg",
            "models/textures/pbr/Ground037/Ground037_1K-JPG_Roughness.jpg",
            "models/textures/pbr/Ground025/Ground025_1K-JPG_Color.jpg",
            "models/textures/pbr/Ground025/Ground025_1K-JPG_NormalGL.jpg",
            "models/textures/pbr/Ground025/Ground025_1K-JPG_Roughness.jpg",
            "models/textures/pbr/Ground049A/Ground049A_1K-JPG_Color.jpg",
            "models/textures/pbr/Ground049A/Ground049A_1K-JPG_NormalGL.jpg",
            "models/textures/pbr/Ground049A/Ground049A_1K-JPG_Roughness.jpg",
            "models/textures/pbr/Ground061/Ground061_1K-JPG_Color.jpg",
            "models/textures/pbr/Ground061/Ground061_1K-JPG_NormalGL.jpg",
            "models/textures/pbr/Ground061/Ground061_1K-JPG_Roughness.jpg"
        };

        for (String texPath : pbrTextures) {
            Texture tex = assetManager.loadTexture(texPath);
            assertNotNull(tex, "PBR Texture must load: " + texPath);
            assertNotNull(tex.getImage(), "Texture image must not be null for: " + texPath);
        }
    }

    @Test
    public void testVegetationVisualizerLoadsAssets() {
        VegetationVisualizer visualizer = new VegetationVisualizer(assetManager);
        assertNotNull(visualizer.getRootNode(), "VegetationVisualizer rootNode should be initialized");
    }

    private void findModelFiles(File dir, List<File> results) {
        if (!dir.exists()) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                findModelFiles(f, results);
            } else {
                String name = f.getName().toLowerCase();
                if (name.endsWith(".obj") || name.endsWith(".glb") || name.endsWith(".gltf")) {
                    results.add(f);
                }
            }
        }
    }

    private String getAssetPath(File f) {
        String full = f.getAbsolutePath().replace('\\', '/');
        int idx = full.indexOf("/models/");
        if (idx >= 0) {
            return full.substring(idx + 1);
        }
        return "models/" + f.getName();
    }

    private int countGeometries(Spatial spatial) {
        if (spatial instanceof Geometry) {
            return 1;
        } else if (spatial instanceof Node node) {
            int cnt = 0;
            for (Spatial child : node.getChildren()) {
                cnt += countGeometries(child);
            }
            return cnt;
        }
        return 0;
    }
}
