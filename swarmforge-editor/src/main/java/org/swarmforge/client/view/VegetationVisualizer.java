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
        int count = 5 + rand.nextInt(4);
        float ringRadius = 1.8f + rand.nextFloat() * 1.2f;
        float baseAngle = rand.nextFloat() * FastMath.TWO_PI;
        List<Spatial> ringInstances = new ArrayList<>();
        for (int m = 0; m < count; m++) {
            float angle = baseAngle + (m / (float) count) * FastMath.TWO_PI + (rand.nextFloat() - 0.5f) * 0.25f;
            float mx = cx + FastMath.cos(angle) * ringRadius;
            float mz = cz + FastMath.sin(angle) * ringRadius;
            if (mx >= minX && mx <= maxX && mz >= minZ && mz <= maxZ) {
                float melev = terrarium.getSurfaceElevation(mx, mz);
                Spatial mModel = assetPack.pickRandomFromList(assetPack.getMushrooms(), rand);
                if (mModel != null) {
                    Spatial mInst = mModel.clone();
                    float targetH = 0.32f + rand.nextFloat() * 0.22f;
                    mInst.setUserData("OriginalScaleY", targetH);
                    floraBuilder.normalizeAndPositionModel(mInst, mx, melev, mz, targetH, rand, rootNode, currentLatitude, assetPack.getMossyRocks());
                    ringInstances.add(mInst);
                }
            }
        }
        if (!ringInstances.isEmpty()) {
            atmosphereVisualizer.addMushroomCluster(ringInstances, cx, cz);
        }
    }

    private void spawnRealisticGroundScatter(Terrarium terrarium, int cutX, Biome biome, Season season, Random rand) {
        int width = terrarium.getWidth();
        int height = terrarium.getHeight();
        int depth = terrarium.getDepth();

        int step = Math.max(1, Math.min(width, height) / 36);
        for (int x = 1; x < cutX - 1; x += step) {
            for (int y = 1; y < height - 1; y += step) {
                float jx = Math.max(1.0f, Math.min(cutX - 1.0f, x + (rand.nextFloat() - 0.5f) * (step * 0.9f)));
                float jz = Math.max(1.0f, Math.min(height - 1.0f, y + (rand.nextFloat() - 0.5f) * (step * 0.9f)));
                if (jx >= cutX - 0.5f || jx < 0.5f || jz < 0.5f || jz >= height - 0.5f) continue;

                float elev = terrarium.getSurfaceElevation(jx, jz);
                int ix = Math.max(0, Math.min(width - 1, Math.round(jx)));
                int iy = Math.max(0, Math.min(height - 1, Math.round(jz)));
                int iz = Math.max(0, Math.min(depth - 1, Math.round(elev)));

                TerrariumCell topCell = terrarium.getCell(ix, iy, iz);
                if (topCell == null || topCell.material() == TerrariumCell.Material.AIR || topCell.material() == TerrariumCell.Material.WATER) {
                    continue;
                }

                TerrariumCell.Material mat = topCell.material();
                float roll = rand.nextFloat();
                Spatial scatterModel = null;
                float targetScale = 0.40f;

                if (mat == TerrariumCell.Material.ROCK || mat == TerrariumCell.Material.GRAVEL) {
                    if (roll < 0.35f && !assetPack.getMossyRocks().isEmpty()) {
                        scatterModel = assetPack.pickRandomFromList(assetPack.getMossyRocks(), rand);
                        targetScale = 0.40f + rand.nextFloat() * 0.50f;
                    } else if (roll < 0.65f && !assetPack.getRocks().isEmpty()) {
                        scatterModel = assetPack.pickRandomFromList(assetPack.getRocks(), rand);
                        targetScale = (mat == TerrariumCell.Material.GRAVEL) ? (0.25f + rand.nextFloat() * 0.35f) : (0.50f + rand.nextFloat() * 0.85f);
                    } else if (roll < 0.85f && !assetPack.getTwigsAndBranches().isEmpty()) {
                        scatterModel = assetPack.pickRandomFromList(assetPack.getTwigsAndBranches(), rand);
                        targetScale = 0.30f + rand.nextFloat() * 0.25f;
                    }
                } else if (mat == TerrariumCell.Material.PEAT || mat == TerrariumCell.Material.LEAF_LITTER) {
                    if (roll < 0.35f && !assetPack.getMushrooms().isEmpty()) {
                        scatterModel = assetPack.pickRandomFromList(assetPack.getMushrooms(), rand);
                        targetScale = 0.32f + rand.nextFloat() * 0.25f;
                    } else if (roll < 0.60f && !assetPack.getTwigsAndBranches().isEmpty()) {
                        scatterModel = assetPack.pickRandomFromList(assetPack.getTwigsAndBranches(), rand);
                        targetScale = 0.35f + rand.nextFloat() * 0.30f;
                    } else if (roll < 0.80f && !assetPack.getDeadwoodLogs().isEmpty()) {
                        scatterModel = assetPack.pickRandomFromList(assetPack.getDeadwoodLogs(), rand);
                        targetScale = 0.65f + rand.nextFloat() * 0.55f;
                    } else if (!assetPack.getMossyRocks().isEmpty()) {
                        scatterModel = assetPack.pickRandomFromList(assetPack.getMossyRocks(), rand);
                        targetScale = 0.38f + rand.nextFloat() * 0.45f;
                    }
                } else if (mat == TerrariumCell.Material.SAND) {
                    if (roll < 0.35f && !assetPack.getRocks().isEmpty()) {
                        scatterModel = assetPack.pickRandomFromList(assetPack.getRocks(), rand);
                        targetScale = 0.25f + rand.nextFloat() * 0.35f;
                    } else if (roll < 0.60f && !assetPack.getTwigsAndBranches().isEmpty()) {
                        scatterModel = assetPack.pickRandomFromList(assetPack.getTwigsAndBranches(), rand);
                        targetScale = 0.28f + rand.nextFloat() * 0.20f;
                    }
                } else if (mat == TerrariumCell.Material.EARTH || mat == TerrariumCell.Material.SILT) {
                    if (roll < 0.30f && !assetPack.getGrassTufts().isEmpty()) {
                        scatterModel = assetPack.pickRandomFromList(assetPack.getGrassTufts(), rand);
                        targetScale = 0.35f + rand.nextFloat() * 0.40f;
                    } else if (roll < 0.55f && !assetPack.getFlowers().isEmpty()) {
                        scatterModel = assetPack.pickRandomFromList(assetPack.getFlowers(), rand);
                        targetScale = 0.40f + rand.nextFloat() * 0.45f;
                        atmosphereVisualizer.addFlowerLocation(new Vector3f(jx, elev, jz));
                    } else if (roll < 0.72f && !assetPack.getTwigsAndBranches().isEmpty()) {
                        scatterModel = assetPack.pickRandomFromList(assetPack.getTwigsAndBranches(), rand);
                        targetScale = 0.32f + rand.nextFloat() * 0.28f;
                    } else if (roll < 0.85f && !assetPack.getBushes().isEmpty()) {
                        scatterModel = assetPack.pickRandomFromList(assetPack.getBushes(), rand);
                        targetScale = 0.60f + rand.nextFloat() * 0.60f;
                    } else if (roll < 0.94f && !assetPack.getMushrooms().isEmpty()) {
                        scatterModel = assetPack.pickRandomFromList(assetPack.getMushrooms(), rand);
                        targetScale = 0.32f + rand.nextFloat() * 0.25f;
                    } else if (!assetPack.getDeadwoodLogs().isEmpty()) {
                        scatterModel = assetPack.pickRandomFromList(assetPack.getDeadwoodLogs(), rand);
                        targetScale = 0.70f + rand.nextFloat() * 0.50f;
                    }
                }

                if (scatterModel != null) {
                    Spatial instance = scatterModel.clone();
                    floraBuilder.applySeasonalTint(instance, season, biome);
                    instance.setUserData("OriginalScaleY", targetScale);
                    floraBuilder.normalizeAndPositionModel(instance, jx, elev, jz, targetScale, rand, rootNode, currentLatitude, assetPack.getMossyRocks());
                    if (assetPack.getMushrooms().contains(scatterModel)) {
                        List<Spatial> single = new ArrayList<>(1);
                        single.add(instance);
                        atmosphereVisualizer.addMushroomCluster(single, jx, jz);
                    }
                }
            }
        }
    }

    private void createRealisticFloraForPlant(float x, float y, float z, VegetationSystem.Plant plant, Biome biome, Season season, Random rand) {
        Spatial chosenModel = null;
        float targetHeight = 8.0f * Math.max(0.4f, plant.growth);

        switch (plant.type) {
            case TREE -> {
                chosenModel = assetPack.pickModelForBiome(biome, rand, true, bambooPct, cactusPct);
                String name = (chosenModel != null && chosenModel.getName() != null) ? chosenModel.getName().toLowerCase() : "";
                if (name.contains("stump") || name.contains("log")) {
                    targetHeight = (0.9f + rand.nextFloat() * 0.6f) * Math.max(0.4f, plant.growth);
                } else if (biome == Biome.ALPINE_SNOW || biome == Biome.TUNDRA) {
                    targetHeight = (7.5f + rand.nextFloat() * 2.5f) * Math.max(0.4f, plant.growth);
                } else {
                    targetHeight = (7.0f + rand.nextFloat() * 2.5f) * Math.max(0.4f, plant.growth);
                }
            }
            case SHRUB -> {
                targetHeight = (1.6f + rand.nextFloat() * 1.1f) * Math.max(0.4f, plant.growth);
                chosenModel = assetPack.pickRandomFromList(assetPack.getBushes(), rand);
            }
            case FLOWER -> {
                targetHeight = (0.22f + rand.nextFloat() * 0.20f) * Math.max(0.4f, plant.growth);
                chosenModel = assetPack.pickRandomFromList(assetPack.getFlowers(), rand);
                atmosphereVisualizer.addFlowerLocation(new Vector3f(x, y, z));
            }
            case MOSS, GRASS -> {
                targetHeight = (0.15f + rand.nextFloat() * 0.20f) * Math.max(0.4f, plant.growth);
                chosenModel = !assetPack.getGrassTufts().isEmpty() ? assetPack.pickRandomFromList(assetPack.getGrassTufts(), rand) : assetPack.pickRandomFromList(assetPack.getFlowers(), rand);
            }
        }

        if (chosenModel != null) {
            Spatial instance = chosenModel.clone();
            floraBuilder.applySeasonalTint(instance, season, biome);
            floraBuilder.normalizeAndPositionModel(instance, x, y, z, Math.max(0.20f, targetHeight), rand, rootNode, currentLatitude, assetPack.getMossyRocks());
            if (plant.type == VegetationSystem.PlantType.TREE) {
                atmosphereVisualizer.addCanopyLocation(new Vector3f(x, y + targetHeight, z));
            }
        } else {
            floraBuilder.createProceduralTree3D(x, y, z, (biome == Biome.ALPINE_SNOW) ? 4 : 0, rand, season, rootNode);
        }
    }

    private void createRealisticFlora(float x, float y, float z, Biome biome, Season season, Random rand, int speciesIdx) {
        Spatial chosenModel = pickModelForSpecies(speciesIdx, biome, rand);
        if (chosenModel == null) {
            chosenModel = assetPack.pickModelForBiome(biome, rand, true, bambooPct, cactusPct);
        }
        float targetHeight;

        if (speciesIdx == 3 || chosenModel == assetPack.getCactusModel()) {
            targetHeight = 3.5f + rand.nextFloat() * 2.0f;
        } else if (speciesIdx == 5 || chosenModel == assetPack.getBambooModel()) {
            targetHeight = 3.5f + rand.nextFloat() * 2.0f;
        } else if (speciesIdx == 6 || (chosenModel != null && assetPack.getDeadwoodLogs().contains(chosenModel))) {
            targetHeight = 0.9f + rand.nextFloat() * 0.6f;
        } else if (speciesIdx == 2 || (chosenModel != null && assetPack.getPalmTrees().contains(chosenModel))) {
            targetHeight = 6.5f + rand.nextFloat() * 2.5f;
        } else if (speciesIdx == 1 || biome == Biome.ALPINE_SNOW || biome == Biome.TUNDRA) {
            targetHeight = 7.5f + rand.nextFloat() * 2.5f;
        } else {
            targetHeight = 7.0f + rand.nextFloat() * 2.5f;
        }

        if (chosenModel != null) {
            Spatial instance = chosenModel.clone();
            floraBuilder.applySeasonalTint(instance, season, biome);
            floraBuilder.normalizeAndPositionModel(instance, x, y, z, targetHeight, rand, rootNode, currentLatitude, assetPack.getMossyRocks());
        } else {
            floraBuilder.createProceduralTree3D(x, y, z, speciesIdx, rand, season, rootNode);
        }
    }

    private Spatial pickModelForSpecies(int speciesIdx, Biome biome, Random rand) {
        return switch (speciesIdx) {
            case 1 -> !assetPack.getConiferTrees().isEmpty() ? assetPack.pickRandomFromList(assetPack.getConiferTrees(), rand) : assetPack.pickRandomFromList(assetPack.getDeciduousTrees(), rand);
            case 2 -> !assetPack.getPalmTrees().isEmpty() ? assetPack.pickRandomFromList(assetPack.getPalmTrees(), rand) : (!assetPack.getDeciduousTrees().isEmpty() ? assetPack.pickRandomFromList(assetPack.getDeciduousTrees(), rand) : null);
            case 3 -> assetPack.getCactusModel() != null ? assetPack.getCactusModel() : (!assetPack.getDeadTrees().isEmpty() ? assetPack.pickRandomFromList(assetPack.getDeadTrees(), rand) : null);
            case 4 -> !assetPack.getDeciduousTrees().isEmpty() ? assetPack.pickRandomFromList(assetPack.getDeciduousTrees(), rand) : null;
            case 5 -> (bambooPct > 0) ? assetPack.getBambooModel() : null;
            case 6 -> !assetPack.getDeadwoodLogs().isEmpty() ? assetPack.pickRandomFromList(assetPack.getDeadwoodLogs(), rand) : (!assetPack.getDeadTrees().isEmpty() ? assetPack.pickRandomFromList(assetPack.getDeadTrees(), rand) : null);
            default -> !assetPack.getDeciduousTrees().isEmpty() ? assetPack.pickRandomFromList(assetPack.getDeciduousTrees(), rand) : null;
        };
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
        applyUVColoration(rootNode, enabled);
    }

    public boolean isUVVisionMode() {
        return uvVisionMode;
    }

    public void setOmmatidialVisionMode(boolean enabled) {
        this.ommatidialVisionMode = enabled;
        if (enabled) {
            rebuildOmmatidialOverlay();
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

    private void rebuildOmmatidialOverlay() {
        ommatidialOverlayNode.detachAllChildren();
        if (activeTerrarium == null) return;

        float cx = currentGridWidth * 0.5f;
        float cz = currentGridHeight * 0.5f;
        float cy = activeTerrarium.getSurfaceElevation(cx, cz) + 12.0f;

        Material polMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        polMat.setColor("Color", new ColorRGBA(0.20f, 0.65f, 0.95f, 0.40f));
        polMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        for (int r = 1; r <= 3; r++) {
            Geometry polRing = new Geometry("PolarizationRing_" + r, new Cylinder(24, 24, r * 6.0f, 0.05f, false));
            polRing.setMaterial(polMat);
            polRing.setQueueBucket(RenderQueue.Bucket.Transparent);
            polRing.setLocalRotation(new Quaternion().fromAngles(-FastMath.HALF_PI, 0, 0));
            polRing.setLocalTranslation(cx, cy + 25.0f, cz);
            ommatidialOverlayNode.attachChild(polRing);
        }

        Material facetMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        facetMat.setColor("Color", new ColorRGBA(0.70f, 0.85f, 1.0f, 0.22f));
        facetMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        for (int i = -3; i <= 3; i++) {
            for (int j = -3; j <= 3; j++) {
                Geometry facet = new Geometry("Ommatidium_" + i + "_" + j, new Cylinder(6, 6, 1.4f, 0.02f, false));
                facet.setMaterial(facetMat);
                facet.setQueueBucket(RenderQueue.Bucket.Transparent);
                facet.setLocalRotation(new Quaternion().fromAngles(-FastMath.HALF_PI, 0, 0));
                facet.setLocalTranslation(cx + i * 2.8f + (j % 2) * 1.4f, cy + 18.0f, cz + j * 2.4f);
                ommatidialOverlayNode.attachChild(facet);
            }
        }
    }

    private void applyUVColoration(Spatial spatial, boolean uv) {
        if (spatial instanceof Geometry geom) {
            Material mat = geom.getMaterial();
            if (mat != null) {
                try {
                    boolean hasDiffuse = mat.getMaterialDef().getMaterialParam("Diffuse") != null;
                    boolean hasAmbient = mat.getMaterialDef().getMaterialParam("Ambient") != null;
                    boolean hasColor = mat.getMaterialDef().getMaterialParam("Color") != null;

                    if (uv) {
                        if (hasDiffuse && mat.getParam("Diffuse") != null && geom.getUserData("origDiffuse") == null) {
                            geom.setUserData("origDiffuse", mat.getParam("Diffuse").getValue());
                        }
                        if (hasAmbient && mat.getParam("Ambient") != null && geom.getUserData("origAmbient") == null) {
                            geom.setUserData("origAmbient", mat.getParam("Ambient").getValue());
                        }
                        if (hasColor && mat.getParam("Color") != null && geom.getUserData("origColor") == null) {
                            geom.setUserData("origColor", mat.getParam("Color").getValue());
                        }

                        String gName = geom.getName() != null ? geom.getName().toLowerCase() : "";
                        ColorRGBA uvDiffuse;
                        ColorRGBA uvAmbient;

                        if (gName.contains("flower") || gName.contains("petal") || gName.contains("blossom") || gName.contains("nectar")) {
                            uvDiffuse = new ColorRGBA(0.85f, 0.20f, 0.98f, 1.0f);
                            uvAmbient = new ColorRGBA(0.45f, 0.10f, 0.65f, 1.0f);
                        } else if (gName.contains("leaf") || gName.contains("leaves") || gName.contains("canopy") || gName.contains("bush")) {
                            uvDiffuse = new ColorRGBA(0.22f, 0.38f, 0.28f, 1.0f);
                            uvAmbient = new ColorRGBA(0.12f, 0.18f, 0.15f, 1.0f);
                        } else if (gName.contains("water") || gName.contains("river") || gName.contains("puddle")) {
                            uvDiffuse = new ColorRGBA(0.20f, 0.75f, 1.0f, 0.90f);
                            uvAmbient = new ColorRGBA(0.10f, 0.40f, 0.70f, 0.90f);
                        } else {
                            uvDiffuse = new ColorRGBA(0.40f, 0.30f, 0.55f, 1.0f);
                            uvAmbient = new ColorRGBA(0.20f, 0.15f, 0.35f, 1.0f);
                        }

                        if (hasDiffuse) mat.setColor("Diffuse", uvDiffuse);
                        if (hasAmbient) mat.setColor("Ambient", uvAmbient);
                        if (hasColor) mat.setColor("Color", uvDiffuse);
                    } else {
                        if (hasDiffuse && geom.getUserData("origDiffuse") instanceof ColorRGBA origD) {
                            mat.setColor("Diffuse", origD);
                        }
                        if (hasAmbient && geom.getUserData("origAmbient") instanceof ColorRGBA origA) {
                            mat.setColor("Ambient", origA);
                        }
                        if (hasColor && geom.getUserData("origColor") instanceof ColorRGBA origC) {
                            mat.setColor("Color", origC);
                        }
                    }
                } catch (Throwable ignored) {}
            }
        } else if (spatial instanceof Node node) {
            for (Spatial child : node.getChildren()) {
                applyUVColoration(child, uv);
            }
        }
    }
}
