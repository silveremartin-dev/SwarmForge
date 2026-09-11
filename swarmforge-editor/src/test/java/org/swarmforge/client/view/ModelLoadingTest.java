package org.swarmforge.client.view;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;
import com.jme3.system.JmeSystem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ModelLoadingTest {

    private static AssetManager assetManager;

    @BeforeAll
    public static void setUp() {
        assetManager = JmeSystem.newAssetManager(
                Thread.currentThread().getContextClassLoader().getResource("com/jme3/asset/Desktop.cfg"));
    }

    @Test
    public void testLoadCactusModel() {
        Spatial model = assetManager.loadModel("models/cactus.obj");
        assertNotNull(model, "Cactus OBJ model should load successfully");
    }

    @Test
    public void testLoadBambooModel() {
        Spatial model = assetManager.loadModel("models/bamboo_set.obj");
        assertNotNull(model, "Bamboo OBJ model should load successfully");
    }

    @Test
    public void testLoadNaturePackModels() {
        Spatial tree = assetManager.loadModel("models/nature_pack_obj/forest_Tree_average_lush_Cube_004.obj");
        assertNotNull(tree, "Nature pack deciduous tree model should load");

        Spatial spruce = assetManager.loadModel("models/nature_pack_obj/forest_Tree_Spruce_small_01_Cylinder_016.obj");
        assertNotNull(spruce, "Nature pack spruce tree model should load");

        Spatial mushroom = assetManager.loadModel("models/nature_pack_obj/forest_Mushroom_big_brown_Icosphere_019.obj");
        assertNotNull(mushroom, "Nature pack mushroom model should load");

        Spatial stone = assetManager.loadModel("models/nature_pack_obj/forest_Stone_average_01_Icosphere.obj");
        assertNotNull(stone, "Nature pack stone model should load");

        Spatial bush = assetManager.loadModel("models/nature_pack_obj/forest_Bush_average_Plane_001.obj");
        assertNotNull(bush, "Nature pack bush model should load");
    }

    @Test
    public void testVegetationVisualizerLoadsAssets() {
        VegetationVisualizer visualizer = new VegetationVisualizer(assetManager);
        assertNotNull(visualizer.getRootNode(), "VegetationVisualizer rootNode should be initialized");
    }
}
