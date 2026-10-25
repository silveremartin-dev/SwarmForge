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
    private final List<Vector3f> activeFlowerLocations = new ArrayList<>();
    private final List<Vector3f> activeCanopyLocations = new ArrayList<>();

    // Active Biological Agents & Particle Lists
    private final List<ButterflyAgent> activeButterflies = new ArrayList<>();
    private final List<FireflyParticle> activeFireflies = new ArrayList<>();
    private final List<FallingLeafParticle> activeFallingLeaves = new ArrayList<>();
    private final List<BirdAgent> activeBirds = new ArrayList<>();
    private final List<PollenParticle> activePollen = new ArrayList<>();
    private final List<DesertDustParticle> activeDesertDust = new ArrayList<>();
    private final List<WaterStriderAgent> activeWaterStriders = new ArrayList<>();
    private final List<OrbWebAgent> activeOrbWebs = new ArrayList<>();
    private final List<SoilSteamParticle> activeSoilSteam = new ArrayList<>();
    private final List<StridulationAura> activeStridulations = new ArrayList<>();
    private final List<DandelionPappusParticle> activePappus = new ArrayList<>();
    private final List<MushroomClusterAgent> activeMushroomClusters = new ArrayList<>();
    private final List<DynamicPuddleAgent> activePuddles = new ArrayList<>();
    private final List<CanopyDripParticle> activeCanopyDrips = new ArrayList<>();
    private final List<GnatSwarmAgent> activeGnatSwarms = new ArrayList<>();
    private final List<AphidClusterAgent> activeAphidClusters = new ArrayList<>();
    private final List<ExtrafloralNectaryAgent> activeNectaries = new ArrayList<>();
    private final List<CordycepsCadaverAgent> activeCordycepsCadavers = new ArrayList<>();
    private final List<Geometry> activeMudcracks = new ArrayList<>();

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
        DynamicPuddleAgent puddle = createDynamicPuddle(center, maxRadius);
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
                ButterflyAgent b = createButterfly(flowerPos, rand, i);
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
                FireflyParticle fp = createFirefly(new Vector3f(fx, felev + 0.35f + rand.nextFloat() * 1.2f, fz), rand);
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
                FallingLeafParticle leaf = createFallingLeaf(new Vector3f(lx, lelev + 4.0f + rand.nextFloat() * 9.0f, lz), lelev, rand);
                activeFallingLeaves.add(leaf);
                ambientAtmosphereNode.attachChild(leaf.geom);
            }
        }

        // 4. Distant Soaring Birds (Oiseaux d'ambiance - rapaces thermiques)
        if (biome != Biome.ALPINE_SNOW && effectiveSeason != Season.WINTER) {
            int birdCount = 3 + rand.nextInt(2);
            Vector3f center = new Vector3f(gridWidth * 0.5f, 0, gridHeight * 0.5f);
            for (int i = 0; i < birdCount; i++) {
                BirdAgent bird = createBird(center, gridWidth, gridHeight, rand, i);
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
                PollenParticle p = createPollen(new Vector3f(px, pelev + 0.8f + rand.nextFloat() * 6.0f, pz), pelev, rand);
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
                DesertDustParticle d = createDesertDust(new Vector3f(dx, delev + 0.2f + rand.nextFloat() * 2.5f, dz), delev, rand);
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
                WaterStriderAgent ws = createWaterStrider(spawnPos, gridWidth, gridHeight, rand, i);
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
                OrbWebAgent web = createOrbWeb(new Vector3f(wx, welev + 0.6f + rand.nextFloat() * 1.8f, wz), gridWidth, gridHeight, terrarium, rand, i);
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
            SoilSteamParticle sp = createSoilSteam(new Vector3f(sx, selev + 0.05f, sz), rand);
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
                DandelionPappusParticle dp = createDandelionPappus(new Vector3f(px, pelev + 0.3f + rand.nextFloat() * 2.5f, pz), pelev, rand);
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
                CanopyDripParticle drip = createCanopyDrip(canopyPos, groundY, rand.nextFloat() * 2.0f);
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
            GnatSwarmAgent gnatSwarm = createGnatSwarm(new Vector3f(gx, gelev + 1.2f + rand.nextFloat() * 0.8f, gz), 0.65f, rand, i);
            activeGnatSwarms.add(gnatSwarm);
            ambientAtmosphereNode.attachChild(gnatSwarm.swarmNode);
        }

        // 13. Trophobiotic Aphid Herds (Grappes de pucerons avec perles de miellat)
        int aphidClusterCount = 5;
        for (int i = 0; i < aphidClusterCount; i++) {
            float ax = 3.0f + rand.nextFloat() * (gridWidth - 6.0f);
            float az = 3.0f + rand.nextFloat() * (gridHeight - 6.0f);
            float aelev = terrarium.getSurfaceElevation(ax, az);
            AphidClusterAgent aphidCluster = createAphidCluster(new Vector3f(ax, aelev + 0.45f + rand.nextFloat() * 0.5f, az), rand, i);
            activeAphidClusters.add(aphidCluster);
            ambientAtmosphereNode.attachChild(aphidCluster.node);
        }

        // 14. Extrafloral Nectary Beads (Nectaires extra-floraux perlés sur les tiges de buissons)
        int nectaryCount = 8;
        for (int i = 0; i < nectaryCount; i++) {
            float nx = 3.0f + rand.nextFloat() * (gridWidth - 6.0f);
            float nz = 3.0f + rand.nextFloat() * (gridHeight - 6.0f);
            float nelev = terrarium.getSurfaceElevation(nx, nz);
            ExtrafloralNectaryAgent nectary = createExtrafloralNectary(new Vector3f(nx, nelev + 0.35f + rand.nextFloat() * 0.4f, nz), rand);
            activeNectaries.add(nectary);
            ambientAtmosphereNode.attachChild(nectary.geom);
        }

        // 15. Parasitic Ophiocordyceps Zombie Cadavers (Cadavres de fourmis zombies)
        int cordycepsCount = 3;
        for (int i = 0; i < cordycepsCount; i++) {
            float cx = 4.0f + rand.nextFloat() * (gridWidth - 8.0f);
            float cz = 4.0f + rand.nextFloat() * (gridHeight - 8.0f);
            float celev = terrarium.getSurfaceElevation(cx, cz);
            CordycepsCadaverAgent cordyceps = createCordycepsCadaver(new Vector3f(cx, celev + 1.2f + rand.nextFloat() * 0.8f, cz), rand, i);
            activeCordycepsCadavers.add(cordyceps);
            ambientAtmosphereNode.attachChild(cordyceps.node);
        }

        // 16. Desiccation Mudcracks Grid (Craquelures de sol en sécheresse)
        int crackPatches = 6;
        for (int i = 0; i < crackPatches; i++) {
            float mx = 3.0f + rand.nextFloat() * (gridWidth - 6.0f);
            float mz = 3.0f + rand.nextFloat() * (gridHeight - 6.0f);
            float melev = terrarium.getSurfaceElevation(mx, mz);
            Geometry crack = createMudcrackPatch(new Vector3f(mx, melev + 0.02f, mz), 0.75f + rand.nextFloat() * 0.5f, rand, i);
            activeMudcracks.add(crack);
            ambientAtmosphereNode.attachChild(crack);
        }
    }

    public void update(WeatherSystem weather, Season effectiveSeason, Biome currentBiome,
                       RenderMode currentRenderMode, float swayTime, float tpf,
                       int gridWidth, int gridHeight) {
        boolean isDay = (weather == null || weather.isDaytime());
        float temp = (weather != null) ? weather.getTemperature() : 20.0f;
        float rain = (weather != null) ? weather.getRainfall() : 0.0f;
        float windSpeedMs = (weather != null) ? weather.getWindSpeedMs() : 3.0f;
        float windAngle = (weather != null) ? (float) Math.toRadians(weather.getWindDirectionAngle()) : 0.785f;
        float windSin = FastMath.sin(windAngle);
        float windCos = FastMath.cos(windAngle);
        float windDx = windSin * (windSpeedMs * 0.45f);
        float windDz = -windCos * (windSpeedMs * 0.45f);

        // 1. Butterflies: Flowers blooming in Spring / Summer & warm daytime (>= 14°C) without heavy rain
        boolean flowersInBloom = (effectiveSeason == Season.SPRING || effectiveSeason == Season.SUMMER) && !activeFlowerLocations.isEmpty();
        boolean butterflyWeatherActive = isDay && temp >= 14.0f && rain < 0.20f;
        boolean butterfliesVisible = flowersInBloom && butterflyWeatherActive && currentRenderMode == RenderMode.REALISTIC;

        for (ButterflyAgent b : activeButterflies) {
            if (!butterfliesVisible) {
                b.node.setCullHint(Spatial.CullHint.Always);
            } else {
                b.node.setCullHint(Spatial.CullHint.Dynamic);
                float t = swayTime * b.flightSpeed + b.phaseOffset;
                float ox = FastMath.cos(t) * b.flightRadius + FastMath.sin(t * 2.3f) * (b.flightRadius * 0.35f);
                float oz = FastMath.sin(t * 1.15f) * b.flightRadius + FastMath.cos(t * 1.7f) * (b.flightRadius * 0.35f);
                float oy = FastMath.sin(t * 2.0f) * 0.30f + 0.45f;

                b.node.setLocalTranslation(b.anchor.x + ox, b.anchor.y + oy, b.anchor.z + oz);

                float vx = -FastMath.sin(t) * b.flightRadius;
                float vz = FastMath.cos(t * 1.15f) * b.flightRadius * 1.15f;
                float heading = FastMath.atan2(vx, vz);
                b.node.setLocalRotation(new Quaternion().fromAngles(0, heading, 0));

                float flap = FastMath.sin(swayTime * b.flapSpeed + b.phaseOffset) * 0.85f;
                b.leftWing.setLocalRotation(new Quaternion().fromAngles(0, 0, flap));
                b.rightWing.setLocalRotation(new Quaternion().fromAngles(0, 0, -flap));
            }
        }

        // 2. Nocturnal Fireflies: Warm Nighttime (>= 12°C) in Spring / Summer
        boolean fireflySeasonActive = (effectiveSeason == Season.SPRING || effectiveSeason == Season.SUMMER);
        boolean fireflyWeatherActive = !isDay && temp >= 12.0f && rain < 0.30f;
        boolean firefliesVisible = fireflySeasonActive && fireflyWeatherActive && currentRenderMode == RenderMode.REALISTIC;

        for (FireflyParticle fp : activeFireflies) {
            if (!firefliesVisible) {
                fp.geom.setCullHint(Spatial.CullHint.Always);
            } else {
                fp.geom.setCullHint(Spatial.CullHint.Dynamic);
                float ft = swayTime * fp.speed + fp.phase;
                float dx = FastMath.sin(ft * 0.7f) * 0.45f;
                float dy = FastMath.cos(ft * 1.1f) * 0.25f;
                float dz = FastMath.sin(ft * 0.9f) * 0.45f;
                fp.geom.setLocalTranslation(fp.basePos.x + dx, fp.basePos.y + dy, fp.basePos.z + dz);

                float pulse = 0.35f + 0.65f * FastMath.sqr(FastMath.sin(ft * 2.5f));
                Material fMat = fp.geom.getMaterial();
                if (fMat != null && fMat.getMaterialDef().getMaterialParam("Color") != null) {
                    fMat.setColor("Color", new ColorRGBA(0.68f * pulse, 0.92f * pulse, 0.22f * pulse, pulse));
                }
            }
        }

        // 3. Autumn Falling Leaves
        boolean leavesActive = (effectiveSeason == Season.FALL) && currentRenderMode == RenderMode.REALISTIC;
        for (FallingLeafParticle leaf : activeFallingLeaves) {
            if (!leavesActive) {
                leaf.geom.setCullHint(Spatial.CullHint.Always);
            } else {
                leaf.geom.setCullHint(Spatial.CullHint.Dynamic);
                leaf.currentY -= leaf.fallSpeed * tpf;
                if (leaf.currentY <= leaf.groundY + 0.05f) {
                    leaf.currentY = leaf.spawnPos.y;
                }

                float lt = swayTime * leaf.swayFrequency + leaf.phase;
                float lx = leaf.spawnPos.x + FastMath.sin(lt) * leaf.swayRadius;
                float lz = leaf.spawnPos.z + FastMath.cos(lt * 1.3f) * leaf.swayRadius;

                leaf.geom.setLocalTranslation(lx, leaf.currentY, lz);
                leaf.geom.setLocalRotation(new Quaternion().fromAngles(FastMath.sin(lt * 2.0f) * 0.6f, lt * 0.5f, FastMath.cos(lt * 1.5f) * 0.6f));
            }
        }

        // 4. Distant Soaring Birds
        boolean birdsVisible = (effectiveSeason != Season.WINTER && isDay && rain < 0.25f && currentRenderMode == RenderMode.REALISTIC);
        for (BirdAgent bird : activeBirds) {
            if (!birdsVisible) {
                bird.geom.setCullHint(Spatial.CullHint.Always);
            } else {
                bird.geom.setCullHint(Spatial.CullHint.Dynamic);
                if (tpf > 0.0001f) {
                    bird.currentAngle += bird.speed * tpf;
                }
                float bx = bird.center.x + FastMath.cos(bird.currentAngle) * bird.radius;
                float bz = bird.center.z + FastMath.sin(bird.currentAngle) * bird.radius;
                bird.geom.setLocalTranslation(bx, bird.altitude, bz);
                bird.geom.setLocalRotation(new Quaternion().fromAngles(0, -bird.currentAngle + FastMath.HALF_PI, 0.12f));
            }
        }

        // 5. Spring Aerobiological Pollen Drift
        boolean pollenActive = (effectiveSeason == Season.SPRING || effectiveSeason == Season.SUMMER) && isDay && rain < 0.10f && currentRenderMode == RenderMode.REALISTIC;
        for (PollenParticle p : activePollen) {
            if (!pollenActive) {
                p.geom.setCullHint(Spatial.CullHint.Always);
            } else {
                p.geom.setCullHint(Spatial.CullHint.Dynamic);
                if (tpf > 0.0001f) {
                    Vector3f pos = p.geom.getLocalTranslation();
                    pos.x += (windDx * 0.25f + FastMath.sin(swayTime * p.driftFreq + p.phase) * 0.15f) * tpf;
                    pos.z += (windDz * 0.25f + FastMath.cos(swayTime * p.driftFreq + p.phase) * 0.15f) * tpf;
                    pos.y += FastMath.sin(swayTime * 0.8f + p.phase) * 0.08f * tpf;
                    if (pos.x < 1.0f) pos.x = gridWidth - 2.0f;
                    if (pos.x > gridWidth - 1.0f) pos.x = 2.0f;
                    if (pos.z < 1.0f) pos.z = gridHeight - 2.0f;
                    if (pos.z > gridHeight - 1.0f) pos.z = 2.0f;
                    p.geom.setLocalTranslation(pos);
                }
            }
        }

        // 6. Desert Dust Drift Simulation
        boolean dustActive = (currentBiome == Biome.DESERT) && (windSpeedMs > 2.5f || temp > 30.0f) && currentRenderMode == RenderMode.REALISTIC;
        for (DesertDustParticle d : activeDesertDust) {
            if (!dustActive) {
                d.geom.setCullHint(Spatial.CullHint.Always);
            } else {
                d.geom.setCullHint(Spatial.CullHint.Dynamic);
                if (tpf > 0.0001f) {
                    Vector3f pos = d.geom.getLocalTranslation();
                    pos.x += windDx * 0.65f * tpf;
                    pos.z += windDz * 0.65f * tpf;
                    pos.y += (FastMath.sin(swayTime * 2.0f + d.phase) * 0.20f) * tpf;
                    if (pos.x < 1.0f) pos.x = gridWidth - 2.0f;
                    if (pos.x > gridWidth - 1.0f) pos.x = 2.0f;
                    if (pos.z < 1.0f) pos.z = gridHeight - 2.0f;
                    if (pos.z > gridHeight - 1.0f) pos.z = 2.0f;
                    d.geom.setLocalTranslation(pos);
                }
            }
        }

        // Rain Wetness Calculation
        if (rain > 0.1f) {
            rainWetness = Math.min(1.0f, rainWetness + 0.35f * tpf);
        } else {
            float dryingRate = 0.02f + 0.05f * (Math.max(0.0f, temp) / 30.0f);
            rainWetness = Math.max(0.0f, rainWetness - dryingRate * tpf);
        }

        // 7. Water Striders
        boolean stridersActive = (effectiveSeason != Season.WINTER && rain < 0.40f && currentRenderMode == RenderMode.REALISTIC);
        for (WaterStriderAgent ws : activeWaterStriders) {
            if (!stridersActive) {
                ws.node.setCullHint(Spatial.CullHint.Always);
            } else {
                ws.node.setCullHint(Spatial.CullHint.Dynamic);
                ws.update(tpf, swayTime);
            }
        }

        // 8. Dew-covered Orb Webs
        boolean websActive = currentRenderMode == RenderMode.REALISTIC;
        for (OrbWebAgent web : activeOrbWebs) {
            if (!websActive) {
                web.geom.setCullHint(Spatial.CullHint.Always);
            } else {
                web.update(tpf, swayTime, rainWetness, isDay, windSpeedMs);
            }
        }

        // 9. Post-Rain Soil Steaming
        boolean steamActive = (rainWetness > 0.15f && isDay && rain < 0.10f && temp > 10.0f && currentRenderMode == RenderMode.REALISTIC);
        for (SoilSteamParticle sp : activeSoilSteam) {
            if (!steamActive) {
                sp.geom.setCullHint(Spatial.CullHint.Always);
            } else {
                sp.geom.setCullHint(Spatial.CullHint.Dynamic);
                sp.update(tpf, windDx, windDz);
            }
        }

        // 10. Bioacoustic Stridulation Auras
        boolean stridActive = (temp > 22.0f && isDay && rain < 0.20f && (effectiveSeason == Season.SPRING || effectiveSeason == Season.SUMMER) && currentRenderMode == RenderMode.REALISTIC);
        for (StridulationAura sa : activeStridulations) {
            if (!stridActive) {
                sa.geom.setCullHint(Spatial.CullHint.Always);
            } else {
                sa.geom.setCullHint(Spatial.CullHint.Dynamic);
                sa.update(tpf);
            }
        }

        // 11. Dynamic Mushroom Flush and Desiccation Lifecycle
        if (currentRenderMode == RenderMode.REALISTIC) {
            for (MushroomClusterAgent mc : activeMushroomClusters) {
                mc.update(tpf, rainWetness, effectiveSeason, temp);
            }
        }

        // 12. Dandelion Pappus Drift
        boolean pappusActive = (effectiveSeason == Season.SPRING || effectiveSeason == Season.SUMMER) && isDay && rain < 0.10f && currentRenderMode == RenderMode.REALISTIC;
        for (DandelionPappusParticle dp : activePappus) {
            if (!pappusActive) {
                dp.geom.setCullHint(Spatial.CullHint.Always);
            } else {
                dp.geom.setCullHint(Spatial.CullHint.Dynamic);
                dp.update(tpf, windDx, windDz, swayTime);
            }
        }

        // 13. Dynamic Ephemeral Puddles
        if (currentRenderMode == RenderMode.REALISTIC) {
            for (DynamicPuddleAgent puddle : activePuddles) {
                puddle.update(tpf, rain, temp, swayTime);
            }
        }

        // 14. Canopy Drip Particles
        if (currentRenderMode == RenderMode.REALISTIC) {
            for (CanopyDripParticle drip : activeCanopyDrips) {
                drip.update(tpf, rainWetness);
            }
        }

        // 15. Crepuscular Gnat Swarms
        float hour = (weather != null) ? weather.getTimeOfDay() : 19.0f;
        boolean isTwilight = (hour >= 5.5f && hour <= 7.5f) || (hour >= 18.5f && hour <= 21.0f);
        if (currentRenderMode == RenderMode.REALISTIC) {
            for (GnatSwarmAgent swarm : activeGnatSwarms) {
                swarm.update(tpf, swayTime, isTwilight, rain, temp);
            }
        }

        // 16. Aphid Clusters & Honeydew Shimmer
        if (currentRenderMode == RenderMode.REALISTIC) {
            for (AphidClusterAgent aphid : activeAphidClusters) {
                aphid.update(tpf, swayTime);
            }
        }

        // 17. Extrafloral Nectary Secretions
        if (currentRenderMode == RenderMode.REALISTIC) {
            for (ExtrafloralNectaryAgent nectary : activeNectaries) {
                nectary.update(tpf, swayTime);
            }
        }

        // 18. Parasitic Ophiocordyceps Zombie Fungi
        if (currentRenderMode == RenderMode.REALISTIC) {
            for (CordycepsCadaverAgent cordyceps : activeCordycepsCadavers) {
                cordyceps.update(tpf, swayTime);
            }
        }

        // 19. Desiccation Mudcracks in Severe Drought
        boolean drought = (temp > 28.0f && rain < 0.05f && rainWetness < 0.05f && currentRenderMode == RenderMode.REALISTIC);
        for (Geometry mc : activeMudcracks) {
            mc.setCullHint(drought ? Spatial.CullHint.Dynamic : Spatial.CullHint.Always);
        }
    }

    // =========================================================================
    // Factory Methods for Particles & Biological Agents (Delegated)
    // =========================================================================

    public FallingLeafParticle createFallingLeaf(Vector3f spawnPos, float groundY, Random rand) {
        return entityFactory.createFallingLeaf(spawnPos, groundY, rand);
    }

    public ButterflyAgent createButterfly(Vector3f flowerPos, Random rand, int index) {
        return entityFactory.createButterfly(flowerPos, rand, index);
    }

    public FireflyParticle createFirefly(Vector3f basePos, Random rand) {
        return entityFactory.createFirefly(basePos, rand);
    }

    public BirdAgent createBird(Vector3f center, int gridWidth, int gridHeight, Random rand, int index) {
        return entityFactory.createBird(center, gridWidth, gridHeight, rand, index);
    }

    public PollenParticle createPollen(Vector3f spawnPos, float groundY, Random rand) {
        return entityFactory.createPollen(spawnPos, groundY, rand);
    }

    public DesertDustParticle createDesertDust(Vector3f spawnPos, float groundY, Random rand) {
        return entityFactory.createDesertDust(spawnPos, groundY, rand);
    }

    public WaterStriderAgent createWaterStrider(Vector3f spawnPos, int gridWidth, int gridHeight, Random rand, int index) {
        return entityFactory.createWaterStrider(spawnPos, gridWidth, gridHeight, rand, index);
    }

    public OrbWebAgent createOrbWeb(Vector3f pos, int gridWidth, int gridHeight, Terrarium terrarium, Random rand, int index) {
        return entityFactory.createOrbWeb(pos, gridWidth, gridHeight, terrarium, rand, index);
    }

    public SoilSteamParticle createSoilSteam(Vector3f spawnPos, Random rand) {
        return entityFactory.createSoilSteam(spawnPos, rand);
    }

    public StridulationAura createStridulationAura(Vector3f centerPos, Random rand) {
        return entityFactory.createStridulationAura(centerPos, rand);
    }

    public DandelionPappusParticle createDandelionPappus(Vector3f spawnPos, float groundY, Random rand) {
        return entityFactory.createDandelionPappus(spawnPos, groundY, rand);
    }

    public DynamicPuddleAgent createDynamicPuddle(Vector3f center, float maxRadius) {
        return entityFactory.createDynamicPuddle(center, maxRadius);
    }

    public CanopyDripParticle createCanopyDrip(Vector3f canopyPos, float groundY, float initialDelay) {
        return entityFactory.createCanopyDrip(canopyPos, groundY, initialDelay);
    }

    public GnatSwarmAgent createGnatSwarm(Vector3f center, float radius, Random rand, int index) {
        return entityFactory.createGnatSwarm(center, radius, rand, index);
    }

    public AphidClusterAgent createAphidCluster(Vector3f stemPos, Random rand, int index) {
        return entityFactory.createAphidCluster(stemPos, rand, index);
    }

    public ExtrafloralNectaryAgent createExtrafloralNectary(Vector3f axilPos, Random rand) {
        return entityFactory.createExtrafloralNectary(axilPos, rand);
    }

    public CordycepsCadaverAgent createCordycepsCadaver(Vector3f anchorPos, Random rand, int index) {
        return entityFactory.createCordycepsCadaver(anchorPos, rand, index);
    }

    public Geometry createMudcrackPatch(Vector3f pos, float size, Random rand, int index) {
        return entityFactory.createMudcrackPatch(pos, size, rand, index);
    }
}