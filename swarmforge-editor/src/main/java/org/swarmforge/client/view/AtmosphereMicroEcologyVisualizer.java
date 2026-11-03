/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import org.swarmforge.client.ui.WorldEditorPane.RenderMode;
import org.swarmforge.client.view.atmosphere.AtmosphereEntities.*;
import org.swarmforge.client.view.atmosphere.AtmosphereEntityFactory;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;
import org.swarmforge.core.world.Biome;
import org.swarmforge.core.world.Season;
import org.swarmforge.core.world.WeatherSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 3D Ambient Atmosphere and Biological Micro-Ecology Visualizer for JMonkeyEngine.
 * Handles procedural rendering and lifecycle simulation of ambient fauna, aerobiology,
 * micro-hydrology, and biotic interactions:
 * <ul>
 *   <li>Butterflies (nectar foraging across blooming flowers)</li>
 *   <li>Nocturnal fireflies (bioluminescent mating pulses)</li>
 *   <li>Autumn falling leaves (phenological senescence drift)</li>
 *   <li>High-altitude soaring raptors/birds</li>
 *   <li>Aerobiological pollen grain dispersion</li>
 *   <li>Aeolian desert dust transport</li>
 *   <li>Hydrophobic water striders (Gerridae surface tension navigation)</li>
 *   <li>Dew-covered orb spider webs (hygroscopic shimmer & degradation)</li>
 *   <li>Post-rain telluric soil steam evaporation</li>
 *   <li>Bioacoustic stridulation auras (cicada/orthoptera acoustic activity)</li>
 *   <li>Dynamic mushroom flush and moisture-coupled desiccation</li>
 *   <li>Anemochoric dandelion pappus seed lofting</li>
 *   <li>Ephemeral rain puddles (precipitation pooling & evaporation)</li>
 *   <li>Canopy drip drainage (foliar interception runoff)</li>
 *   <li>Crepuscular gnat swarms (twilight dipteran mating lekking)</li>
 *   <li>Trophobiotic aphid herds with glistening honeydew droplets</li>
 *   <li>Extrafloral nectary secretions on foliage</li>
 *   <li>Parasitic Ophiocordyceps zombie ant cadavers with sporulating stalks</li>
 *   <li>Desiccation mudcrack lattices in severe drought</li>
 * </ul>
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class AtmosphereMicroEcologyVisualizer {

    private final AssetManager assetManager;
    private final AtmosphereEntityFactory entityFactory;
    private final Node ambientAtmosphereNode = new Node("AmbientAtmosphereNode");

    // Location Anchors for Biotic Spawns
    final List<Vector3f> activeFlowerLocations = new ArrayList<>();
    final List<Vector3f> activeCanopyLocations = new ArrayList<>();

    // Active Biological Agents & Particle Lists
    final List<ButterflyAgent> activeButterflies = new ArrayList<>();
    final List<FireflyParticle> activeFireflies = new ArrayList<>();
    final List<FallingLeafParticle> activeFallingLeaves = new ArrayList<>();
    final List<BirdAgent> activeBirds = new ArrayList<>();
    final List<PollenParticle> activePollen = new ArrayList<>();
    final List<DesertDustParticle> activeDesertDust = new ArrayList<>();
    final List<WaterStriderAgent> activeWaterStriders = new ArrayList<>();
    final List<OrbWebAgent> activeOrbWebs = new ArrayList<>();
    final List<SoilSteamParticle> activeSoilSteam = new ArrayList<>();
    final List<StridulationAura> activeStridulations = new ArrayList<>();
    final List<DandelionPappusParticle> activePappus = new ArrayList<>();
    final List<MushroomClusterAgent> activeMushroomClusters = new ArrayList<>();
    final List<DynamicPuddleAgent> activePuddles = new ArrayList<>();
    final List<CanopyDripParticle> activeCanopyDrips = new ArrayList<>();
    final List<GnatSwarmAgent> activeGnatSwarms = new ArrayList<>();
    final List<AphidClusterAgent> activeAphidClusters = new ArrayList<>();
    final List<ExtrafloralNectaryAgent> activeNectaries = new ArrayList<>();
    final List<CordycepsCadaverAgent> activeCordycepsCadavers = new ArrayList<>();
    final List<Geometry> activeMudcracks = new ArrayList<>();

    private float rainWetness = 0.0f;

    public AtmosphereMicroEcologyVisualizer(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.entityFactory = new AtmosphereEntityFactory(assetManager);
    }

    public Node getNode() {
        return ambientAtmosphereNode;
    }

    public void clear() {
        ambientAtmosphereNode.detachAllChildren();
        activeFlowerLocations.clear();
        activeCanopyLocations.clear();
        activeButterflies.clear();
        activeFireflies.clear();
        activeFallingLeaves.clear();
        activeBirds.clear();
        activePollen.clear();
        activeDesertDust.clear();
        activeWaterStriders.clear();
        activeOrbWebs.clear();
        activeSoilSteam.clear();
        activeStridulations.clear();
        activePappus.clear();
        activeMushroomClusters.clear();
        activePuddles.clear();
        activeCanopyDrips.clear();
        activeGnatSwarms.clear();
        activeAphidClusters.clear();
        activeNectaries.clear();
        activeCordycepsCadavers.clear();
        activeMudcracks.clear();
    }

    public void addFlowerLocation(Vector3f pos) {
        activeFlowerLocations.add(pos);
    }

    public void addCanopyLocation(Vector3f pos) {
        activeCanopyLocations.add(pos);
    }

    public void addMushroomCluster(List<Spatial> instances, float x, float z) {
        if (instances != null && !instances.isEmpty()) {
            activeMushroomClusters.add(new MushroomClusterAgent(instances, x, z));
        }
    }

    public void addPuddle(Vector3f center, float maxRadius) {
        DynamicPuddleAgent puddle = entityFactory.createDynamicPuddle(center, maxRadius);
        activePuddles.add(puddle);
        ambientAtmosphereNode.attachChild(puddle.geom);
    }

    public void rebuildAtmosphericFauna(Terrarium terrarium, Biome biome, Season effectiveSeason,
                                        RenderMode currentRenderMode, int gridWidth, int gridHeight, Random rand) {
        if (currentRenderMode != RenderMode.REALISTIC || terrarium == null) return;

        // 1. Butterflies: Spawned strictly over active blooming flower clusters in Spring / Summer (hemisphere)
        boolean flowersInBloom = (effectiveSeason == Season.SPRING || effectiveSeason == Season.SUMMER) && !activeFlowerLocations.isEmpty();
        if (flowersInBloom) {
            int butterflyCount = Math.min(16, Math.max(2, activeFlowerLocations.size() * 2));
            for (int i = 0; i < butterflyCount; i++) {
                Vector3f flowerPos = activeFlowerLocations.get(i % activeFlowerLocations.size());
                ButterflyAgent b = entityFactory.createButterfly(flowerPos, rand, i);
                activeButterflies.add(b);
                ambientAtmosphereNode.attachChild(b.node);
            }
        }

        // 2. Fireflies (Lucioles): Spawned for warm Spring/Summer nights in vegetative biomes
        if ((effectiveSeason == Season.SPRING || effectiveSeason == Season.SUMMER) && biome != Biome.DESERT && biome != Biome.ALPINE_SNOW) {
            int fireflyCount = 20;
            for (int i = 0; i < fireflyCount; i++) {
                float fx = 2.0f + rand.nextFloat() * (gridWidth - 4.0f);
                float fz = 2.0f + rand.nextFloat() * (gridHeight - 4.0f);
                float felev = terrarium.getSurfaceElevation(fx, fz);
                FireflyParticle fp = entityFactory.createFirefly(new Vector3f(fx, felev + 0.35f + rand.nextFloat() * 1.2f, fz), rand);
                activeFireflies.add(fp);
                ambientAtmosphereNode.attachChild(fp.geom);
            }
        }

        // 3. Autumn Falling Leaves: Spawned in Fall for temperate forest biomes
        if (effectiveSeason == Season.FALL && biome != Biome.DESERT && biome != Biome.ALPINE_SNOW) {
            int leafCount = 28;
            for (int i = 0; i < leafCount; i++) {
                float lx = 2.0f + rand.nextFloat() * (gridWidth - 4.0f);
                float lz = 2.0f + rand.nextFloat() * (gridHeight - 4.0f);
                float lelev = terrarium.getSurfaceElevation(lx, lz);
                FallingLeafParticle leaf = entityFactory.createFallingLeaf(new Vector3f(lx, lelev + 4.0f + rand.nextFloat() * 9.0f, lz), lelev, rand);
                activeFallingLeaves.add(leaf);
                ambientAtmosphereNode.attachChild(leaf.geom);
            }
        }

        // 4. Distant Soaring Birds (Oiseaux d'ambiance - rapaces thermiques)
        if (biome != Biome.ALPINE_SNOW && effectiveSeason != Season.WINTER) {
            int birdCount = 3 + rand.nextInt(2);
            Vector3f center = new Vector3f(gridWidth * 0.5f, 0, gridHeight * 0.5f);
            for (int i = 0; i < birdCount; i++) {
                BirdAgent bird = entityFactory.createBird(center, gridWidth, gridHeight, rand, i);
                activeBirds.add(bird);
                ambientAtmosphereNode.attachChild(bird.geom);
            }
        }

        // 5. Spring Aerobiological Pollen Drift (Dérive de pollen printanier)
        if ((effectiveSeason == Season.SPRING || effectiveSeason == Season.SUMMER) && (biome == Biome.FOREST || biome == Biome.GRASSLAND || biome == Biome.WETLAND)) {
            int pollenCount = 30;
            for (int i = 0; i < pollenCount; i++) {
                float px = 2.0f + rand.nextFloat() * (gridWidth - 4.0f);
                float pz = 2.0f + rand.nextFloat() * (gridHeight - 4.0f);
                float pelev = terrarium.getSurfaceElevation(px, pz);
                PollenParticle p = entityFactory.createPollen(new Vector3f(px, pelev + 0.8f + rand.nextFloat() * 6.0f, pz), pelev, rand);
                activePollen.add(p);
                ambientAtmosphereNode.attachChild(p.geom);
            }
        }

        // 6. Desert Dust Drift (Poussière éolienne du désert)
        if (biome == Biome.DESERT) {
            int dustCount = 25;
            for (int i = 0; i < dustCount; i++) {
                float dx = 2.0f + rand.nextFloat() * (gridWidth - 4.0f);
                float dz = 2.0f + rand.nextFloat() * (gridHeight - 4.0f);
                float delev = terrarium.getSurfaceElevation(dx, dz);
                DesertDustParticle d = entityFactory.createDesertDust(new Vector3f(dx, delev + 0.2f + rand.nextFloat() * 2.5f, dz), delev, rand);
                activeDesertDust.add(d);
                ambientAtmosphereNode.attachChild(d.geom);
            }
        }

        // 7. Water Striders (Gerridae / Patineurs d'eau sur les surfaces aquatiques)
        List<Vector3f> waterSpots = new ArrayList<>();
        int step = Math.max(2, gridWidth / 20);
        for (int x = 1; x < gridWidth - 1; x += step) {
            for (int z = 1; z < gridHeight - 1; z += step) {
                float elev = terrarium.getSurfaceElevation(x, z);
                int ix = Math.max(0, Math.min(terrarium.getWidth() - 1, x));
                int iz = Math.max(0, Math.min(terrarium.getHeight() - 1, z));
                int iy = Math.max(0, Math.min(terrarium.getDepth() - 1, Math.round(elev)));
                TerrariumCell cell = terrarium.getCell(ix, iz, iy);
                if (cell != null && cell.material() == TerrariumCell.Material.WATER) {
                    waterSpots.add(new Vector3f(x, elev + 0.02f, z));
                }
            }
        }
        if (!waterSpots.isEmpty()) {
            int striderCount = Math.min(6, Math.max(2, waterSpots.size()));
            for (int i = 0; i < striderCount; i++) {
                Vector3f spawnPos = waterSpots.get(rand.nextInt(waterSpots.size()));
                WaterStriderAgent ws = entityFactory.createWaterStrider(spawnPos, gridWidth, gridHeight, rand, i);
                activeWaterStriders.add(ws);
                ambientAtmosphereNode.attachChild(ws.node);
            }
        }

        // 8. Dew-covered Orb Webs (Toiles d'araignées perlées de rosée dans les branchages)
        if (biome == Biome.FOREST || biome == Biome.GRASSLAND || biome == Biome.WETLAND) {
            int webCount = 4 + rand.nextInt(3);
            for (int i = 0; i < webCount; i++) {
                float wx = 2.0f + rand.nextFloat() * (gridWidth - 4.0f);
                float wz = 2.0f + rand.nextFloat() * (gridHeight - 4.0f);
                float welev = terrarium.getSurfaceElevation(wx, wz);
                OrbWebAgent web = entityFactory.createOrbWeb(new Vector3f(wx, welev + 0.6f + rand.nextFloat() * 1.8f, wz), gridWidth, gridHeight, terrarium, rand, i);
                activeOrbWebs.add(web);
                ambientAtmosphereNode.attachChild(web.geom);
            }
        }

        // 9. Post-Rain Soil Steaming (Particules de vapeur d'eau tellurique)
        int steamCount = 20;
        for (int i = 0; i < steamCount; i++) {
            float sx = 2.0f + rand.nextFloat() * (gridWidth - 4.0f);
            float sz = 2.0f + rand.nextFloat() * (gridHeight - 4.0f);
            float selev = terrarium.getSurfaceElevation(sx, sz);
            SoilSteamParticle sp = entityFactory.createSoilSteam(new Vector3f(sx, selev + 0.05f, sz), rand);
            activeSoilSteam.add(sp);
            ambientAtmosphereNode.attachChild(sp.geom);
        }

        // 10. Dandelion Pappus Drift (Aigrettes de pissenlit et graines anémochores)
        if ((effectiveSeason == Season.SPRING || effectiveSeason == Season.SUMMER) && (biome == Biome.GRASSLAND || biome == Biome.FOREST || biome == Biome.WETLAND)) {
            int pappusCount = 18;
            for (int i = 0; i < pappusCount; i++) {
                float px = 2.0f + rand.nextFloat() * (gridWidth - 4.0f);
                float pz = 2.0f + rand.nextFloat() * (gridHeight - 4.0f);
                float pelev = terrarium.getSurfaceElevation(px, pz);
                DandelionPappusParticle dp = entityFactory.createDandelionPappus(new Vector3f(px, pelev + 0.3f + rand.nextFloat() * 2.5f, pz), pelev, rand);
                activePappus.add(dp);
                ambientAtmosphereNode.attachChild(dp.geom);
            }
        }

        // 11. Canopy Drip Particles (Interception foliaire et égouttement sous la canopée)
        if (!activeCanopyLocations.isEmpty()) {
            int dripCount = Math.min(24, activeCanopyLocations.size() * 3);
            for (int i = 0; i < dripCount; i++) {
                Vector3f canopyPos = activeCanopyLocations.get(i % activeCanopyLocations.size());
                float groundY = terrarium.getSurfaceElevation(canopyPos.x, canopyPos.z);
                CanopyDripParticle drip = entityFactory.createCanopyDrip(canopyPos, groundY, rand.nextFloat() * 2.0f);
                activeCanopyDrips.add(drip);
                ambientAtmosphereNode.attachChild(drip.geom);
            }
        }

        // 12. Crepuscular Gnat Swarms (Nuées de moucherons crépusculaires)
        int gnatCount = 4;
        for (int i = 0; i < gnatCount; i++) {
            float gx = 4.0f + rand.nextFloat() * (gridWidth - 8.0f);
            float gz = 4.0f + rand.nextFloat() * (gridHeight - 8.0f);
            float gelev = terrarium.getSurfaceElevation(gx, gz);
            GnatSwarmAgent gnatSwarm = entityFactory.createGnatSwarm(new Vector3f(gx, gelev + 1.2f + rand.nextFloat() * 0.8f, gz), 0.65f, rand, i);
            activeGnatSwarms.add(gnatSwarm);
            ambientAtmosphereNode.attachChild(gnatSwarm.swarmNode);
        }

        // 13. Trophobiotic Aphid Herds (Grappes de pucerons avec perles de miellat)
        int aphidClusterCount = 5;
        for (int i = 0; i < aphidClusterCount; i++) {
            float ax = 3.0f + rand.nextFloat() * (gridWidth - 6.0f);
            float az = 3.0f + rand.nextFloat() * (gridHeight - 6.0f);
            float aelev = terrarium.getSurfaceElevation(ax, az);
            AphidClusterAgent aphidCluster = entityFactory.createAphidCluster(new Vector3f(ax, aelev + 0.45f + rand.nextFloat() * 0.5f, az), rand, i);
            activeAphidClusters.add(aphidCluster);
            ambientAtmosphereNode.attachChild(aphidCluster.node);
        }

        // 14. Extrafloral Nectary Beads (Nectaires extra-floraux perlés sur les tiges de buissons)
        int nectaryCount = 8;
        for (int i = 0; i < nectaryCount; i++) {
            float nx = 3.0f + rand.nextFloat() * (gridWidth - 6.0f);
            float nz = 3.0f + rand.nextFloat() * (gridHeight - 6.0f);
            float nelev = terrarium.getSurfaceElevation(nx, nz);
            ExtrafloralNectaryAgent nectary = entityFactory.createExtrafloralNectary(new Vector3f(nx, nelev + 0.35f + rand.nextFloat() * 0.4f, nz), rand);
            activeNectaries.add(nectary);
            ambientAtmosphereNode.attachChild(nectary.geom);
        }

        // 15. Parasitic Ophiocordyceps Zombie Cadavers (Cadavres de fourmis zombies)
        int cordycepsCount = 3;
        for (int i = 0; i < cordycepsCount; i++) {
            float cx = 4.0f + rand.nextFloat() * (gridWidth - 8.0f);
            float cz = 4.0f + rand.nextFloat() * (gridHeight - 8.0f);
            float celev = terrarium.getSurfaceElevation(cx, cz);
            CordycepsCadaverAgent cordyceps = entityFactory.createCordycepsCadaver(new Vector3f(cx, celev + 1.2f + rand.nextFloat() * 0.8f, cz), rand, i);
            activeCordycepsCadavers.add(cordyceps);
            ambientAtmosphereNode.attachChild(cordyceps.node);
        }

        // 16. Desiccation Mudcracks Grid (Craquelures de sol en sécheresse)
        int crackPatches = 6;
        for (int i = 0; i < crackPatches; i++) {
            float mx = 3.0f + rand.nextFloat() * (gridWidth - 6.0f);
            float mz = 3.0f + rand.nextFloat() * (gridHeight - 6.0f);
            float melev = terrarium.getSurfaceElevation(mx, mz);
            Geometry crack = entityFactory.createMudcrackPatch(new Vector3f(mx, melev + 0.02f, mz), 0.75f + rand.nextFloat() * 0.5f, rand, i);
            activeMudcracks.add(crack);
            ambientAtmosphereNode.attachChild(crack);
        }
    }

    public void update(WeatherSystem weather, Season effectiveSeason, Biome currentBiome,
                       RenderMode currentRenderMode, float swayTime, float tpf,
                       int gridWidth, int gridHeight) {
        this.rainWetness = AtmosphereUpdateController.update(this, weather, effectiveSeason, currentBiome,
                currentRenderMode, swayTime, tpf, gridWidth, gridHeight, this.rainWetness);
    }

    public AtmosphereEntityFactory getEntityFactory() { return entityFactory; }
}