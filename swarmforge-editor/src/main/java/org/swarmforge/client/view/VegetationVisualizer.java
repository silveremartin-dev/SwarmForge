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
import com.jme3.scene.shape.Cylinder;
import org.swarmforge.client.ui.WorldEditorPane.RenderMode;
import org.swarmforge.client.view.vegetation.ProceduralFloraBuilder;
import org.swarmforge.client.view.vegetation.VegetationAssetPack;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;
import org.swarmforge.core.world.Biome;
import org.swarmforge.core.world.Season;
import org.swarmforge.core.world.VegetationSystem;
import org.swarmforge.core.world.WeatherSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 3D Vegetation, Nest Architectures and Naturalistic Flora/Fauna visualizer for JMonkeyEngine.
 * Handles rendering of trees, plants, biological nests, and ambient natural phenomena across 3 distinct modes:
 * - REALISTIC: 150+ native standalone OBJ Nature Pack models (Oaks, Birches, Spruces, Palms, Bushes, Rocks, Cacti)
 * - SCIENTIFIC: Metric parametric trees with DBH markers, LAI foliage envelopes, and sensor nodes
 * - GAMIFIED: Authentic full-scale Minecraft cubic voxel trees and structured block nests with active shadows
 *
 * Features physical wind sway coupling, hemisphere-aware seasonal foliage tinting, and micro-faunal agents.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class VegetationVisualizer {

    private final AssetManager assetManager;
    private final Node rootNode;
    private RenderMode currentRenderMode = RenderMode.REALISTIC;
    private boolean visible = true;
    private float swayTime = 0.0f;
    private double currentLatitude = 45.0; // Default temperate Northern hemisphere
    private Season currentSeason = Season.SPRING;
    private Terrarium activeTerrarium;
    private VegetationSystem activeVegetationSystem;
    private int currentGridWidth = 64;
    private int currentGridHeight = 64;
    private float slicePlaneRatio = 1.0f;

    // Subsystems
    private final VegetationAssetPack assetPack;
    private final ProceduralFloraBuilder floraBuilder;
    private final NestStructure3DVisualizer nestVisualizer;
    private final AtmosphereMicroEcologyVisualizer atmosphereVisualizer;

    // Insect Vision Modes
    private boolean uvVisionMode = false;
    private boolean ommatidialVisionMode = false;
    private final Node ommatidialOverlayNode = new Node("OmmatidialOverlayNode");

    // Tree species distribution percentages
    private int oakPct = 60;
    private int pinePct = 20;
    private int acaciaPct = 0;
    private int cactusPct = 0;
    private int birchPct = 10;
    private int bambooPct = 0;
    private int deadWoodPct = 10;

    public VegetationVisualizer(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.rootNode = new Node("VegetationNode");
        this.rootNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        this.assetPack = new VegetationAssetPack(assetManager);
        this.floraBuilder = new ProceduralFloraBuilder(assetManager);
        this.atmosphereVisualizer = new AtmosphereMicroEcologyVisualizer(assetManager);
        this.rootNode.attachChild(atmosphereVisualizer.getNode());

        this.nestVisualizer = new NestStructure3DVisualizer(assetManager, rootNode);
        if (assetPack.getBeehiveModel() != null) {
            nestVisualizer.setBeehiveModel(assetPack.getBeehiveModel());
        }
    }

    public Node getRootNode() {
        return rootNode;
    }

    public void setTreeSpeciesComposition(int oak, int pine, int acacia, int cactus, int birch, int bamboo, int deadWood) {
        this.oakPct = Math.max(0, oak);
        this.pinePct = Math.max(0, pine);
        this.acaciaPct = Math.max(0, acacia);
        this.cactusPct = Math.max(0, cactus);
        this.birchPct = Math.max(0, birch);
        this.bambooPct = Math.max(0, bamboo);
        this.deadWoodPct = Math.max(0, deadWood);
    }

    public int sampleTreeSpeciesIndex(Random rand) {
        int total = oakPct + pinePct + acaciaPct + cactusPct + birchPct + bambooPct + deadWoodPct;
        if (total <= 0) return 0;
        int roll = rand.nextInt(total);
        if (roll < oakPct) return 0;
        roll -= oakPct;
        if (roll < pinePct) return 1;
        roll -= pinePct;
        if (roll < acaciaPct) return 2;
        roll -= acaciaPct;
        if (roll < cactusPct) return 3;
        roll -= cactusPct;
        if (roll < birchPct) return 4;
        roll -= birchPct;
        if (roll < bambooPct) return 5;
        return 6;
    }

    public void setLatitude(double latitude) {
        this.currentLatitude = latitude;
    }

    public void setSeason(Season season) {
        if (this.currentSeason != season) {
            this.currentSeason = season;
            rebuildVegetation(currentGridWidth, currentGridHeight, activeTerrarium, activeVegetationSystem);
        }
    }

    public void setRenderMode(RenderMode mode) {
        if (this.currentRenderMode != mode) {
            this.currentRenderMode = mode;
            rebuildVegetation(currentGridWidth, currentGridHeight, activeTerrarium, activeVegetationSystem);
        }
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        rootNode.setCullHint(visible ? Spatial.CullHint.Dynamic : Spatial.CullHint.Always);
    }

    public Season getEffectiveSeason() {
        if (currentLatitude < 0) {
            return switch (currentSeason) {
                case SPRING -> Season.FALL;
                case SUMMER -> Season.WINTER;
                case FALL -> Season.SPRING;
                case WINTER -> Season.SUMMER;
            };
        }
        return currentSeason;
    }

    public void setSlicePlaneRatio(float ratio) {
        this.slicePlaneRatio = Math.max(0.05f, Math.min(1.0f, ratio));
        rebuildVegetation(currentGridWidth, currentGridHeight, activeTerrarium, activeVegetationSystem);
    }

    public void rebuildVegetation(int gridWidth, int gridHeight) {
        rebuildVegetation(gridWidth, gridHeight, activeTerrarium, activeVegetationSystem);
    }

    public void rebuildVegetation(int gridWidth, int gridHeight, Terrarium terrarium, VegetationSystem vegSystem) {
        this.currentGridWidth = gridWidth;
        this.currentGridHeight = gridHeight;
        this.activeTerrarium = terrarium;
        this.activeVegetationSystem = vegSystem;

        rootNode.detachAllChildren();
        rootNode.attachChild(atmosphereVisualizer.getNode());
        rootNode.attachChild(nestVisualizer.getNestArchitectureNode());
        rootNode.attachChild(nestVisualizer.getColonyInfrastructureNode());
        nestVisualizer.setActiveTerrarium(terrarium);
        if (ommatidialVisionMode) {
            rootNode.attachChild(ommatidialOverlayNode);
        }
        atmosphereVisualizer.clear();
        nestVisualizer.getColonyInfrastructureNode().detachAllChildren();
        rebuildNestArchitectures();

        if (!visible) return;

        Biome biome = Biome.forLatitude(currentLatitude);
        Season effectiveSeason = getEffectiveSeason();
        Random rand = new Random(42);
        int cutX = Math.max(2, Math.min(gridWidth, (int) Math.ceil(gridWidth * Math.max(0.05f, Math.min(1.0f, slicePlaneRatio)))));

        float marginX = Math.min(3.5f, cutX * 0.12f);
        float marginZ = Math.min(3.5f, gridHeight * 0.12f);
        float minX = marginX;
        float maxX = Math.max(minX, cutX - marginX);
        float minZ = marginZ;
        float maxZ = Math.max(minZ, gridHeight - marginZ);

        if (vegSystem != null && !vegSystem.getPlants().isEmpty()) {
            for (VegetationSystem.Plant plant : vegSystem.getPlants()) {
                if (plant.x >= cutX - 0.5f) continue;
                float x = Math.max(minX, Math.min(maxX, plant.x));
                float z = Math.max(minZ, Math.min(maxZ, plant.y));
                float y = (terrarium != null) ? terrarium.getSurfaceElevation(x, z) : 0.0f;

                if (currentRenderMode == RenderMode.GAMIFIED) {
                    floraBuilder.createGamifiedFloraForPlant(x, y, z, plant, biome, effectiveSeason, rand, rootNode, cactusPct);
                } else if (currentRenderMode == RenderMode.SCIENTIFIC) {
                    floraBuilder.createScientificFloraForPlant(x, y, z, plant, biome, rand, rootNode);
                } else {
                    createRealisticFloraForPlant(x, y, z, plant, biome, effectiveSeason, rand);
                }
            }
        } else if (terrarium != null) {
            float treeStep = 8.0f;
            for (float tx = minX + 0.5f; tx <= maxX - 0.5f; tx += treeStep) {
                for (float tz = minZ + 0.5f; tz <= maxZ - 0.5f; tz += treeStep) {
                    float jx = Math.max(minX, Math.min(maxX, tx + (rand.nextFloat() - 0.5f) * (treeStep * 0.75f)));
                    float jz = Math.max(minZ, Math.min(maxZ, tz + (rand.nextFloat() - 0.5f) * (treeStep * 0.75f)));

                    int ix = Math.max(0, Math.min(gridWidth - 1, Math.round(jx)));
                    int iz = Math.max(0, Math.min(gridHeight - 1, Math.round(jz)));
                    float elev = terrarium.getSurfaceElevation(jx, jz);
                    int iy = Math.max(0, Math.min(terrarium.getDepth() - 1, Math.round(elev)));

                    TerrariumCell cell = terrarium.getCell(ix, iz, iy);
                    if (cell == null || cell.material() == TerrariumCell.Material.WATER || cell.material() == TerrariumCell.Material.CAVITY) {
                        continue;
                    }

                    int speciesIdx = sampleTreeSpeciesIndex(rand);
                    if (currentRenderMode == RenderMode.GAMIFIED) {
                        floraBuilder.createProceduralTreeGamified(jx, elev, jz, biome, effectiveSeason, rand, speciesIdx, rootNode, cactusPct);
                    } else if (currentRenderMode == RenderMode.SCIENTIFIC) {
                        floraBuilder.createProceduralTreeScientific(jx, elev, jz, biome, rand, speciesIdx, rootNode);
                    } else {
                        createRealisticFlora(jx, elev, jz, biome, effectiveSeason, rand, speciesIdx);

                        // Fairy Rings
                        if ((biome == Biome.FOREST || biome == Biome.GRASSLAND || biome == Biome.MEDITERRANEAN || biome == Biome.WETLAND)
                                && rand.nextFloat() < 0.38f && !assetPack.getMushrooms().isEmpty()) {
                            spawnFairyRing(jx, elev, jz, terrarium, minX, maxX, minZ, maxZ, rand);
                        }

                        // Deadwood Logs
                        if ((biome == Biome.FOREST || biome == Biome.GRASSLAND || biome == Biome.WETLAND)
                                && rand.nextFloat() < 0.35f && !assetPack.getDeadwoodLogs().isEmpty()) {
                            float dx = (rand.nextFloat() - 0.5f) * 3.4f;
                            float dz = (rand.nextFloat() - 0.5f) * 3.4f;
                            float lx = Math.max(minX, Math.min(maxX, jx + dx));
                            float lz = Math.max(minZ, Math.min(maxZ, jz + dz));
                            float lelev = terrarium.getSurfaceElevation(lx, lz);
                            Spatial logModel = assetPack.pickRandomFromList(assetPack.getDeadwoodLogs(), rand);
                            if (logModel != null) {
                                Spatial logInst = logModel.clone();
                                floraBuilder.normalizeAndPositionModel(logInst, lx, lelev, lz, 0.8f + rand.nextFloat() * 0.7f, rand, rootNode, currentLatitude, assetPack.getMossyRocks());
                            }
                        }

                        // Secondary Understory Shrub
                        if (rand.nextFloat() < 0.45f && !assetPack.getBushes().isEmpty()) {
                            float sx = Math.max(minX, Math.min(maxX, jx + (rand.nextFloat() - 0.5f) * 3.2f));
                            float sz = Math.max(minZ, Math.min(maxZ, jz + (rand.nextFloat() - 0.5f) * 3.2f));
                            float selev = terrarium.getSurfaceElevation(sx, sz);
                            Spatial shrubInstance = assetPack.pickRandomFromList(assetPack.getBushes(), rand).clone();
                            floraBuilder.applySeasonalTint(shrubInstance, effectiveSeason, biome);
                            floraBuilder.normalizeAndPositionModel(shrubInstance, sx, selev, sz, 1.4f + rand.nextFloat() * 1.2f, rand, rootNode, currentLatitude, assetPack.getMossyRocks());
                        }
                    }
                }
            }
        }

        if (currentRenderMode == RenderMode.REALISTIC && terrarium != null) {
            spawnRealisticGroundScatter(terrarium, cutX, biome, effectiveSeason, rand);
            atmosphereVisualizer.rebuildAtmosphericFauna(terrarium, biome, effectiveSeason, currentRenderMode, currentGridWidth, currentGridHeight, rand);
        }
    }

    private void spawnFairyRing(float cx, float cy, float cz, Terrarium terrarium, float minX, float maxX, float minZ, float maxZ, Random rand) {
        VegetationRealisticScatterHelper.spawnFairyRing(cx, cy, cz, terrarium, minX, maxX, minZ, maxZ, rand,
                assetPack, floraBuilder, atmosphereVisualizer, rootNode, currentLatitude);
    }

    private void spawnRealisticGroundScatter(Terrarium terrarium, int cutX, Biome biome, Season season, Random rand) {
        VegetationRealisticScatterHelper.spawnRealisticGroundScatter(terrarium, cutX, biome, season, rand,
                assetPack, floraBuilder, atmosphereVisualizer, rootNode, currentLatitude);
    }

    private void createRealisticFloraForPlant(float x, float y, float z, VegetationSystem.Plant plant, Biome biome, Season season, Random rand) {
        VegetationRealisticScatterHelper.createRealisticFloraForPlant(x, y, z, plant, biome, season, rand,
                assetPack, floraBuilder, atmosphereVisualizer, rootNode, currentLatitude, bambooPct, cactusPct);
    }

    private void createRealisticFlora(float x, float y, float z, Biome biome, Season season, Random rand, int speciesIdx) {
        VegetationRealisticScatterHelper.createRealisticFlora(x, y, z, biome, season, rand, speciesIdx,
                assetPack, floraBuilder, rootNode, currentLatitude, bambooPct, cactusPct);
    }

    public void update(WeatherSystem weather, float tpf) {
        if (!visible || rootNode.getChildren().isEmpty()) return;

        for (Spatial child : rootNode.getChildren()) {
            if (child == atmosphereVisualizer.getNode()) continue;
            if (child instanceof Node treeNode) {
                Float baseRotY = (Float) treeNode.getUserData("BaseRotY");
                if (baseRotY != null) {
                    Float tiltX = (Float) treeNode.getUserData("TiltX");
                    Float tiltZ = (Float) treeNode.getUserData("TiltZ");
                    float tx = tiltX != null ? tiltX : 0.0f;
                    float tz = tiltZ != null ? tiltZ : 0.0f;
                    treeNode.setLocalRotation(new Quaternion().fromAngles(tx, baseRotY, tz));
                }
            }
        }

        Season effectiveSeason = getEffectiveSeason();
        Biome currentBiome = Biome.forLatitude(currentLatitude);
        atmosphereVisualizer.update(weather, effectiveSeason, currentBiome, currentRenderMode, swayTime, tpf, currentGridWidth, currentGridHeight);
    }

    public AtmosphereMicroEcologyVisualizer getAtmosphereVisualizer() {
        return atmosphereVisualizer;
    }

    public void updateNestArchitectures(List<Colony> colonies, RenderMode mode) {
        nestVisualizer.updateNestArchitectures(colonies, mode);
    }

    public void rebuildNestArchitectures() {
        nestVisualizer.rebuildNestArchitectures();
    }

    public Node getNestArchitectureNode() {
        return nestVisualizer.getNestArchitectureNode();
    }

    public Node getColonyInfrastructureNode() {
        return nestVisualizer.getColonyInfrastructureNode();
    }

    public Spatial renderBeehive(float x, float y, float z, float orientationAngle) {
        return nestVisualizer.renderBeehive(x, y, z, orientationAngle);
    }

    public void ensureHostTreeForWaspNest(float x, float y, float nestAltitude) {
        for (Spatial child : rootNode.getChildren()) {
            if (child.getName() != null && child.getName().startsWith("Tree_")) {
                Vector3f pos = child.getLocalTranslation();
                float dist = FastMath.sqrt(FastMath.sqr(pos.x - x) + FastMath.sqr(pos.z - y));
                if (dist < 3.5f) {
                    return;
                }
            }
        }

        Spatial hostTree = !assetPack.getDeciduousTrees().isEmpty() ? assetPack.pickRandomFromList(assetPack.getDeciduousTrees(), new Random()) : null;
        if (hostTree != null) {
            Spatial treeInstance = hostTree.clone();
            treeInstance.setName("Tree_WaspHost_" + (int) x + "_" + (int) y);
            float targetHeight = Math.max(nestAltitude + 4.0f, 14.0f);
            float surfY = (activeTerrarium != null) ? activeTerrarium.getSurfaceElevation(x, y) : 0.0f;
            floraBuilder.normalizeAndPositionModel(treeInstance, x, surfY, y, targetHeight, new Random(42), rootNode, currentLatitude, assetPack.getMossyRocks());
        }
    }

    public void setUVVisionMode(boolean enabled) {
        this.uvVisionMode = enabled;
        InsectVisionVisualizer.applyUVColoration(rootNode, enabled);
    }

    public boolean isUVVisionMode() {
        return uvVisionMode;
    }

    public void setOmmatidialVisionMode(boolean enabled) {
        this.ommatidialVisionMode = enabled;
        if (enabled) {
            InsectVisionVisualizer.rebuildOmmatidialOverlay(ommatidialOverlayNode, activeTerrarium, currentGridWidth, currentGridHeight, assetManager);
            if (ommatidialOverlayNode.getParent() == null) {
                rootNode.attachChild(ommatidialOverlayNode);
            }
        } else {
            ommatidialOverlayNode.detachAllChildren();
            ommatidialOverlayNode.removeFromParent();
        }
    }

    public boolean isOmmatidialVisionMode() {
        return ommatidialVisionMode;
    }
}
