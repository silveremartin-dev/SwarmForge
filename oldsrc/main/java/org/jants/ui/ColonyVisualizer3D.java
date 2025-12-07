 /*
 *  Copyright 2022 Silvere Martin-Michiellot
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
 package org.jants.ui;

 import com.jme3.app.SimpleApplication;
        import com.jme3.audio.AudioData;
        import com.jme3.audio.AudioNode;
        import com.jme3.export.Savable;
        import com.jme3.export.binary.BinaryExporter;
        import com.jme3.export.binary.BinaryImporter;
        import com.jme3.font.BitmapText;
        import com.jme3.input.KeyInput;
        import com.jme3.input.controls.ActionListener;
        import com.jme3.input.controls.KeyTrigger;
        import com.jme3.light.DirectionalLight;
        import com.jme3.material.Material;
        import com.jme3.math.ColorRGBA;
        import com.jme3.math.Quaternion;
        import com.jme3.math.Vector3f;
        import com.jme3.scene.Node;
        import com.jme3.terrain.Terrain;
        import com.jme3.terrain.geomipmap.TerrainLodControl;
        import com.jme3.terrain.geomipmap.TerrainQuad;
        import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
        import com.jme3.terrain.heightmap.AbstractHeightMap;
        import com.jme3.terrain.heightmap.ImageBasedHeightMap;
        import com.jme3.texture.Texture;
        import com.jme3.texture.Texture.WrapMode;
        import java.io.*;
        import java.util.logging.Level;
        import java.util.logging.Logger;

/**
 * Saves and loads terrain.
 *
 * https://github.com/jMonkeyEngine/jmonkeyengine/blob/master/jme3-examples/src/main/java/jme3test/terrain/TerrainTest.java
 *
 * @author Brent Owens
 */
//also see for grass https://hub.jmonkeyengine.org/t/solved-texture-splat-maps-imagerasters/39342/9
    //https://wiki.jmonkeyengine.org/docs/3.4/core/audio/audio.html
    //https://wiki.jmonkeyengine.org/docs/3.4/tutorials/beginner/hello_audio.html
public class ColonyVisualizer3D extends SimpleApplication {

    private Terrain terrain;
    final private float grassScale = 64;
    final private float dirtScale = 16;
    final private float rockScale = 128;

    public static void main(String[] args) {
        ColonyVisualizer3D app = new ColonyVisualizer3D();
        app.start();
        //testHeightmapBuilding();
    }

    @Override
    public void initialize() {
        super.initialize();
        loadHintText();
    }

    @Override
    public void simpleInitApp() {
        createControls();
        createMap();
        initAudio();
    }

    private void createMap() {
        Material matTerrain = new Material(assetManager,
                "Common/MatDefs/Terrain/TerrainLighting.j3md");
        matTerrain.setBoolean("useTriPlanarMapping", false);
        matTerrain.setBoolean("WardIso", true);

        // ALPHA map (for splat textures)
        matTerrain.setTexture("AlphaMap", assetManager.loadTexture("Textures/Terrain/splat/alphamap.png"));

        // HEIGHTMAP image (for the terrain heightmap)
        Texture heightMapImage = assetManager.loadTexture("Textures/Terrain/splat/mountains512.png");

        // GRASS texture
        Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
        grass.setWrap(WrapMode.Repeat);
        matTerrain.setTexture("DiffuseMap", grass);
        matTerrain.setFloat("DiffuseMap_0_scale", grassScale);


        // DIRT texture
        Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
        dirt.setWrap(WrapMode.Repeat);
        matTerrain.setTexture("DiffuseMap_1", dirt);
        matTerrain.setFloat("DiffuseMap_1_scale", dirtScale);

        // ROCK texture
        Texture rock = assetManager.loadTexture("Textures/Terrain/splat/road.jpg");
        rock.setWrap(WrapMode.Repeat);
        matTerrain.setTexture("DiffuseMap_2", rock);
        matTerrain.setFloat("DiffuseMap_2_scale", rockScale);

        Texture normalMap0 = assetManager.loadTexture("Textures/Terrain/splat/grass_normal.jpg");
        normalMap0.setWrap(WrapMode.Repeat);
        Texture normalMap1 = assetManager.loadTexture("Textures/Terrain/splat/dirt_normal.png");
        normalMap1.setWrap(WrapMode.Repeat);
        Texture normalMap2 = assetManager.loadTexture("Textures/Terrain/splat/road_normal.png");
        normalMap2.setWrap(WrapMode.Repeat);
        matTerrain.setTexture("NormalMap", normalMap0);
        matTerrain.setTexture("NormalMap_1", normalMap1);
        matTerrain.setTexture("NormalMap_2", normalMap2);

        Material matWire = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        matWire.getAdditionalRenderState().setWireframe(true);
        matWire.setColor("Color", ColorRGBA.Green);


        // CREATE HEIGHTMAP
        AbstractHeightMap heightmap = null;
        try {
            heightmap = new ImageBasedHeightMap(heightMapImage.getImage(), 1f);
            heightmap.load();

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (new File("terrainsave.jme").exists()) {
            loadTerrain();
        } else {
            // create the terrain as normal, and give it a control for LOD management
            TerrainQuad terrainQuad = new TerrainQuad("terrain", 65, 129, heightmap.getHeightMap());//, new LodPerspectiveCalculatorFactory(getCamera(), 4)); // add this in to see it use entropy for LOD calculations
            TerrainLodControl control = new TerrainLodControl(terrainQuad, getCamera());
            control.setLodCalculator( new DistanceLodCalculator(65, 2.7f) ); // patch size, and a multiplier
            terrainQuad.addControl(control);
            terrainQuad.setMaterial(matTerrain);
            terrainQuad.setLocalTranslation(0, -100, 0);
            terrainQuad.setLocalScale(4f, 0.25f, 4f);
            rootNode.attachChild(terrainQuad);

            this.terrain = terrainQuad;
        }

        DirectionalLight light = new DirectionalLight();
        light.setDirection((new Vector3f(-0.5f, -1f, -0.5f)).normalize());
        rootNode.addLight(light);
    }

    /**
     * Create the save and load actions and add them to the input listener
     */
    private void createControls() {
        flyCam.setMoveSpeed(50);
        cam.setLocation(new Vector3f(0, 100, 0));
        cam.setRotation(new Quaternion(-0.1779f, 0.821934f, -0.39033f, -0.3747f));

        inputManager.addMapping("save", new KeyTrigger(KeyInput.KEY_T));
        inputManager.addListener(saveActionListener, "save");

        inputManager.addMapping("load", new KeyTrigger(KeyInput.KEY_Y));
        inputManager.addListener(loadActionListener, "load");

        inputManager.addMapping("clone", new KeyTrigger(KeyInput.KEY_C));
        inputManager.addListener(cloneActionListener, "clone");
    }

    public void loadHintText() {
        BitmapText hintText = new BitmapText(guiFont);
        hintText.setSize(guiFont.getCharSet().getRenderedSize());
        hintText.setLocalTranslation(0, getCamera().getHeight(), 0);
        hintText.setText("Hit T to save, and Y to load");
        guiNode.attachChild(hintText);
    }

    final private ActionListener saveActionListener = new ActionListener() {

        @Override
        public void onAction(String name, boolean pressed, float tpf) {
            if (name.equals("save") && !pressed) {

                FileOutputStream fos = null;
                try {
                    long start = System.currentTimeMillis();
                    fos = new FileOutputStream(new File("terrainsave.jme"));

                    // we just use the exporter and pass in the terrain
                    BinaryExporter.getInstance().save((Savable)terrain, new BufferedOutputStream(fos));

                    fos.flush();
                    float duration = (System.currentTimeMillis() - start) / 1000.0f;
                    System.out.println("Save took " + duration + " seconds");
                } catch (IOException ex) {
                    Logger.getLogger(ColonyVisualizer3D.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        Logger.getLogger(ColonyVisualizer3D.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
            }
        }
    };

    private void loadTerrain() {
        FileInputStream fis = null;
        try {
            long start = System.currentTimeMillis();
            // remove the existing terrain and detach it from the root node.
            if (terrain != null) {
                Node existingTerrain = (Node)terrain;
                existingTerrain.removeFromParent();
                existingTerrain.removeControl(TerrainLodControl.class);
                existingTerrain.detachAllChildren();
                terrain = null;
            }

            // import the saved terrain, and attach it back to the root node
            File f = new File("terrainsave.jme");
            fis = new FileInputStream(f);
            BinaryImporter imp = BinaryImporter.getInstance();
            imp.setAssetManager(assetManager);
            terrain = (TerrainQuad) imp.load(new BufferedInputStream(fis));
            rootNode.attachChild((Node)terrain);

            float duration = (System.currentTimeMillis() - start) / 1000.0f;
            System.out.println("Load took " + duration + " seconds");

            // now we have to add back the camera to the LOD control
            TerrainLodControl lodControl = ((Node)terrain).getControl(TerrainLodControl.class);
            if (lodControl != null)
                lodControl.setCamera(getCamera());

        } catch (IOException ex) {
            Logger.getLogger(ColonyVisualizer3D.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(ColonyVisualizer3D.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    final private ActionListener loadActionListener = new ActionListener() {

        @Override
        public void onAction(String name, boolean pressed, float tpf) {
            if (name.equals("load") && !pressed) {
                loadTerrain();
            }
        }
    }
    ;
    final private ActionListener cloneActionListener = new ActionListener() {

        @Override
        public void onAction(String name, boolean pressed, float tpf) {
            if (name.equals("clone") && !pressed) {

                Terrain clone = (Terrain) ((Node)terrain).clone();
                ((Node)terrain).removeFromParent();
                terrain = clone;
                getRootNode().attachChild((Node)terrain);
            }
        }
    };

    private void initAudio() {
        /* nature sound - keeps playing in a loop. */
        AudioNode audio_nature = new AudioNode(assetManager, "Sound/Environment/Ocean Waves.ogg", AudioData.DataType.Stream);
        audio_nature.setLooping(true);  // activate continuous playing
        audio_nature.setPositional(true);
        audio_nature.setVolume(3);
        rootNode.attachChild(audio_nature);
        audio_nature.play(); // play continuously!
    }

    @Override
    public void simpleUpdate(float tpf) {
        listener.setLocation(cam.getLocation());
        listener.setRotation(cam.getRotation());
    }

}
