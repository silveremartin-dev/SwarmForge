/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import org.swarmforge.client.view.vegetation.ProceduralFloraBuilder;
import org.swarmforge.client.view.vegetation.VegetationAssetPack;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;
import org.swarmforge.core.world.Biome;
import org.swarmforge.core.world.Season;
import org.swarmforge.core.world.VegetationSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Handles realistic micro-faunal ground scatter, fairy ring formations, and realistic flora model placement
 * for {@link VegetationVisualizer}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
final class VegetationRealisticScatterHelper {

    private VegetationRealisticScatterHelper() {
    }

    static void spawnFairyRing(float cx, float cy, float cz, Terrarium terrarium,
                              float minX, float maxX, float minZ, float maxZ, Random rand,
                              VegetationAssetPack assetPack, ProceduralFloraBuilder floraBuilder,
                              AtmosphereMicroEcologyVisualizer atmosphereVisualizer,
                              Node rootNode, double currentLatitude) {
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

    static void spawnRealisticGroundScatter(Terrarium terrarium, int cutX, Biome biome, Season season, Random rand,
                                           VegetationAssetPack assetPack, ProceduralFloraBuilder floraBuilder,
                                           AtmosphereMicroEcologyVisualizer atmosphereVisualizer,
                                           Node rootNode, double currentLatitude) {
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

    static void createRealisticFloraForPlant(float x, float y, float z, VegetationSystem.Plant plant,
                                           Biome biome, Season season, Random rand,
                                           VegetationAssetPack assetPack, ProceduralFloraBuilder floraBuilder,
                                           AtmosphereMicroEcologyVisualizer atmosphereVisualizer,
                                           Node rootNode, double currentLatitude,
                                           int bambooPct, int cactusPct) {
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

    static void createRealisticFlora(float x, float y, float z, Biome biome, Season season, Random rand, int speciesIdx,
                                    VegetationAssetPack assetPack, ProceduralFloraBuilder floraBuilder,
                                    Node rootNode, double currentLatitude,
                                    int bambooPct, int cactusPct) {
        Spatial chosenModel = pickModelForSpecies(speciesIdx, biome, rand, assetPack, bambooPct);
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

    static Spatial pickModelForSpecies(int speciesIdx, Biome biome, Random rand,
                                       VegetationAssetPack assetPack, int bambooPct) {
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
}
