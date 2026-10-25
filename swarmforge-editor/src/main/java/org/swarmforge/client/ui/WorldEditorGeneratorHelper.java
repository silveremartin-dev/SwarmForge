package org.swarmforge.client.ui;

import org.swarmforge.client.ui.world.TerrainProceduralGenerator;
import org.swarmforge.client.ui.WorldEditorPane.BotanicalTreeData;
import org.swarmforge.client.ui.WorldEditorPane.SurfaceFloraItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.swarmforge.client.ui.WorldEditorPane.GRID_SIZE;
import static org.swarmforge.client.ui.WorldEditorPane.SOIL_DEPTH;

/**
 * Handles procedural terrain and flora generation algorithms for {@link WorldEditorPane}.
 */
public final class WorldEditorGeneratorHelper {

    private WorldEditorGeneratorHelper() {}

    public static void generateTerrain(WorldEditorPane pane) {
        long rSeed = parseSeed(pane.reliefSeedField, 774829L);
        long sSeed = parseSeed(pane.soilSeedField, 123456L);

        double rough = pane.roughnessSlider != null ? pane.roughnessSlider.getValue() : 0.45;
        double slopeAngle = pane.slopeAngleSlider != null ? pane.slopeAngleSlider.getValue() : 0.0;
        int slopeDir = pane.slopeDirectionCombo != null ? pane.slopeDirectionCombo.getSelectionModel().getSelectedIndex() : 0;

        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                double nx = (double) x / GRID_SIZE;
                double ny = (double) y / GRID_SIZE;

                double noiseVal = TerrainProceduralGenerator.fBmNoiseSeeded(nx * 3.5, ny * 3.5, 4, 0.5, 2.0, rSeed);

                double slopeGrad = switch (slopeDir) {
                    case 0 -> (1.0 - ny);
                    case 1 -> ny;
                    case 2 -> nx;
                    case 3 -> (1.0 - nx);
                    case 4 -> ((1.0 - nx) + (1.0 - ny)) * 0.5;
                    case 5 -> (nx + (1.0 - ny)) * 0.5;
                    case 6 -> ((1.0 - nx) + ny) * 0.5;
                    case 7 -> (nx + ny) * 0.5;
                    default -> (1.0 - ny);
                };

                double slopeFactor = Math.sin(Math.toRadians(slopeAngle)) * 0.65;
                double reliefHeight = (0.20 + 0.55 * noiseVal) * (1.0 - slopeFactor * 0.5) + (slopeGrad * slopeFactor);

                pane.heightGrid[x][y] = Math.max(0.05, Math.min(0.95, reliefHeight));
                pane.carvedVoxelGrid[x][y] = false;
            }
        }

        pane.riverPath = computeRiverPath(pane);
        carveRiverBed(pane);

        double strat = pane.stratificationSlider != null ? pane.stratificationSlider.getValue() : 0.7;
        double mix = pane.mixingRateSlider != null ? pane.mixingRateSlider.getValue() : 0.3;
        generateSoilLayersSeeded(pane, strat, mix, sSeed);

        double vDens = pane.voidDensitySlider != null ? pane.voidDensitySlider.getValue() : 0.08;
        generateVoids(pane, vDens);

        double bHum = pane.baseHumiditySlider != null ? pane.baseHumiditySlider.getValue() : 0.35;
        generateHumidity(pane, bHum);

        double rDens = pane.rootDensitySlider != null ? pane.rootDensitySlider.getValue() : 0.50;
        generateRoots(pane, rDens);
    }

    public static void generateSurfaceFlora(WorldEditorPane pane) {
        pane.surfaceFloraItems.clear();
        long fSeed = parseSeed(pane.floraSeedField, 774829L);
        Random rand = new Random(fSeed);

        int floraCount = 95;
        for (int i = 0; i < floraCount; i++) {
            int gx = 2 + (int)(rand.nextDouble() * (GRID_SIZE - 4));
            int gy = 2 + (int)(rand.nextDouble() * (GRID_SIZE - 4));
            double scale = 0.6 + rand.nextDouble() * 0.8;
            double rotation = rand.nextDouble() * Math.PI * 2;

            int type = 0;
            double roll = rand.nextDouble();
            if (pane.aphidPlantCheck != null && pane.aphidPlantCheck.isSelected() && roll < 0.20) type = 1;
            else if (pane.nectarFlowersCheck != null && pane.nectarFlowersCheck.isSelected() && roll < 0.35) type = 2;
            else if (pane.mossCheck != null && pane.mossCheck.isSelected() && roll < 0.50) type = 3;
            else if (pane.pineLitterCheck != null && pane.pineLitterCheck.isSelected() && roll < 0.65) type = 4;
            else if (roll < 0.75) type = 5;
            else if (roll < 0.85) type = 6;
            else if (pane.fungusFoliageCheck != null && pane.fungusFoliageCheck.isSelected() && roll < 0.92) type = 7;
            else if (pane.fernObstacleCheck != null && pane.fernObstacleCheck.isSelected()) type = 8;
            else type = 9;

            pane.surfaceFloraItems.add(new SurfaceFloraItem(gx, gy, type, scale, rotation));
        }
    }

    public static void regenerateAndRepaint(WorldEditorPane pane) {
        generateTerrain(pane);
        generateSurfaceFlora(pane);
        pane.repaintAllViews();
        if (pane.gameView != null && pane.gameView.getGameApp() != null) {
            pane.gameView.getGameApp().renderTerrarium(pane.exportToTerrarium());
        }
    }

    public static void initDefaultTerrainGrid(WorldEditorPane pane) {
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                double nx = (double) x / GRID_SIZE;
                double ny = (double) y / GRID_SIZE;
                double noiseVal = TerrainProceduralGenerator.fBmNoise(nx * 3.5, ny * 3.5, 4, 0.5, 2.0);
                pane.heightGrid[x][y] = 0.20 + 0.55 * noiseVal;
                pane.heightGrid[x][y] = Math.max(0.05, Math.min(0.95, pane.heightGrid[x][y]));
                pane.carvedVoxelGrid[x][y] = false;
            }
        }
        generateHumidity(pane, 0.35);
        pane.riverPath = computeRiverPath(pane);
        carveRiverBed(pane);
        generateSoilLayersSeeded(pane, 0.7, 0.3, 123456L);
        generateVoids(pane, 0.08);
    }

    static void carveRiverBed(WorldEditorPane pane) {
        if (pane.riverCheck == null || !pane.riverCheck.isSelected() || pane.riverPath == null || pane.riverPath.isEmpty()) return;
        double rWidthMm = pane.riverWidthSlider != null ? pane.riverWidthSlider.getValue() : 120.0;
        TerrainProceduralGenerator.carveRiverBed(pane.heightGrid, pane.riverPath, rWidthMm, GRID_SIZE);
    }

    static List<int[]> computeRiverPath(WorldEditorPane pane) {
        if (pane.riverCheck == null || !pane.riverCheck.isSelected()) return new ArrayList<>();
        long seed = parseSeed(pane.hydroSeedField, 1337L);
        return TerrainProceduralGenerator.computeRiverPath(seed, GRID_SIZE);
    }

    static void generateSoilLayersSeeded(WorldEditorPane pane, double stratification, double mixing, long seed) {
        Random rand = new Random(seed);
        int[] surfacePct = getSurfaceComposition(pane);
        double depthVal = pane.depthSlider != null ? pane.depthSlider.getValue() : 1.5;
        double wtDepthCm = pane.waterTableDepthSlider != null ? pane.waterTableDepthSlider.getValue() : 15.0;
        double wtDepthMeters = wtDepthCm / 100.0;

        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                byte surfaceMat = TerrainProceduralGenerator.pickSurfaceMaterialCoherent(
                        x, y, surfacePct, pane.heightGrid, pane.humidityGrid, pane.riverPath
                );

                for (int d = 0; d < SOIL_DEPTH; d++) {
                    double normD = (double) d / SOIL_DEPTH;
                    double currentDepthMeters = normD * depthVal;
                    byte mat;

                    if (d == 0) {
                        mat = surfaceMat;
                    } else if (normD < 0.20) {
                        mat = (rand.nextDouble() < stratification) ? surfaceMat : (byte) rand.nextInt(3);
                    } else if (normD < 0.55) {
                        double claySandRoll = rand.nextDouble();
                        mat = (claySandRoll < 0.55) ? (byte) 2 : ((claySandRoll < 0.85) ? (byte) 1 : (byte) 0);
                    } else if (normD < 0.82) {
                        mat = (rand.nextDouble() < 0.65) ? (byte) 4 : (byte) 3;
                    } else {
                        mat = (byte) 3;
                    }

                    if (d > 0 && rand.nextDouble() < mixing) {
                        int prevD = Math.max(0, d - 1);
                        mat = pane.soilLayers[x][y][prevD];
                    }

                    if (normD > 0.40 && currentDepthMeters >= wtDepthMeters && rand.nextDouble() < 0.45) {
                        mat = (byte) 2;
                    }

                    pane.soilLayers[x][y][d] = mat;
                }
            }
        }
    }

    static void generateHumidity(WorldEditorPane pane, double baseHumidity) {
        double wtDepthCm = pane.waterTableDepthSlider != null ? pane.waterTableDepthSlider.getValue() : 1500;
        double wtDepthMeters = wtDepthCm / 100.0;
        double totalDepthM = pane.depthSlider != null ? pane.depthSlider.getValue() : 3.0;
        boolean hasRiver = pane.riverCheck != null && pane.riverCheck.isSelected();
        TerrainProceduralGenerator.generateHumidity(pane.humidityGrid, pane.heightGrid, pane.soilLayers, pane.riverPath, hasRiver, baseHumidity, wtDepthMeters, totalDepthM, GRID_SIZE, SOIL_DEPTH);
    }

    static void generateVoids(WorldEditorPane pane, double density) {
        TerrainProceduralGenerator.generateVoids(pane.voidGrid, pane.soilLayers, density, GRID_SIZE, SOIL_DEPTH);
    }

    public static List<BotanicalTreeData> getBotanicalTreeInstances(WorldEditorPane pane) {
        List<BotanicalTreeData> list = new ArrayList<>();
        if (pane.activeSimulation != null && pane.activeSimulation.getVegetationSystem() != null) {
            org.swarmforge.core.domain.Terrarium terr = pane.activeSimulation.getTerrarium();
            int tW = terr != null ? terr.getWidth() : GRID_SIZE;
            int tH = terr != null ? terr.getHeight() : GRID_SIZE;
            for (var plant : pane.activeSimulation.getVegetationSystem().getPlants()) {
                if (plant.type != org.swarmforge.core.world.VegetationSystem.PlantType.TREE && plant.type != org.swarmforge.core.world.VegetationSystem.PlantType.SHRUB) {
                    continue;
                }
                int gx = Math.max(0, Math.min(GRID_SIZE - 1, (int) Math.round((plant.x / (double) tW) * GRID_SIZE)));
                int gy = Math.max(0, Math.min(GRID_SIZE - 1, (int) Math.round((plant.y / (double) tH) * GRID_SIZE)));
                BotanicalTreeData tree = new BotanicalTreeData();
                tree.gx = gx;
                tree.gy = gy;
                tree.ageScale = Math.max(0.35, Math.min(1.2, (double) plant.growth));
                tree.speciesIdx = (plant.type == org.swarmforge.core.world.VegetationSystem.PlantType.SHRUB) ? 4 : 5;
                list.add(tree);
            }
            return list;
        }

        if (pane.treeCountSlider == null) return list;
        int count = (int) pane.treeCountSlider.getValue();

        long tSeed = parseSeed(pane.structSeedField, 555123L);
        Random rand = new Random(tSeed);

        double rWidthMm = pane.riverWidthSlider != null ? pane.riverWidthSlider.getValue() : 120.0;
        int rAvoid = (int) Math.max(4, Math.round(rWidthMm / 25.0 / 2.0) + 3);

        for (int i = 0; i < count; i++) {
            int gx = 10 + (int)(rand.nextDouble() * (GRID_SIZE - 20));
            int gy = 10 + (int)(rand.nextDouble() * (GRID_SIZE - 20));
            double ageScale = 0.55 + rand.nextDouble() * 0.75;
            int speciesIdx = pickBotanicalTreeSpecies(pane, rand);

            if (pane.riverCheck != null && pane.riverCheck.isSelected() && TerrainProceduralGenerator.isNearRiver(pane.riverPath, gx, gy, rAvoid)) continue;
            if (pane.carvedVoxelGrid[gx][gy]) continue;

            BotanicalTreeData tree = new BotanicalTreeData();
            tree.gx = gx;
            tree.gy = gy;
            tree.ageScale = ageScale;
            tree.speciesIdx = speciesIdx;
            list.add(tree);
        }
        return list;
    }

    public static int pickBotanicalTreeSpecies(WorldEditorPane pane, Random rand) {
        int oak = pane.oakPctSpinner != null ? pane.oakPctSpinner.getValue() : 60;
        int pine = pane.pinePctSpinner != null ? pane.pinePctSpinner.getValue() : 20;
        int acacia = pane.acaciaPctSpinner != null ? pane.acaciaPctSpinner.getValue() : 0;
        int cactus = pane.cactusPctSpinner != null ? pane.cactusPctSpinner.getValue() : 0;
        int birch = pane.birchPctSpinner != null ? pane.birchPctSpinner.getValue() : 10;
        int bamboo = pane.bambooPctSpinner != null ? pane.bambooPctSpinner.getValue() : 0;
        int deadWood = pane.deadWoodPctSpinner != null ? pane.deadWoodPctSpinner.getValue() : 10;

        int total = oak + pine + acacia + birch + cactus + bamboo + deadWood;
        if (total <= 0) return 0;

        int roll = rand.nextInt(total);
        int accum = 0;

        if (roll < (accum += oak)) return 0;
        if (roll < (accum += pine)) return 1;
        if (roll < (accum += acacia)) return 2;
        if (roll < (accum += cactus)) return 3;
        if (roll < (accum += birch)) return 4;
        if (roll < (accum += bamboo)) return 5;
        if (roll < (accum += deadWood)) return 6;
        return 0;
    }

    public static void syncTreeSpeciesTo3DView(WorldEditorPane pane) {
        if (pane.gameView != null && pane.gameView.getGameApp() != null) {
            int oak = pane.oakPctSpinner != null ? pane.oakPctSpinner.getValue() : 60;
            int pine = pane.pinePctSpinner != null ? pane.pinePctSpinner.getValue() : 20;
            int acacia = pane.acaciaPctSpinner != null ? pane.acaciaPctSpinner.getValue() : 0;
            int cactus = pane.cactusPctSpinner != null ? pane.cactusPctSpinner.getValue() : 0;
            int birch = pane.birchPctSpinner != null ? pane.birchPctSpinner.getValue() : 10;
            int bamboo = pane.bambooPctSpinner != null ? pane.bambooPctSpinner.getValue() : 0;
            int deadwood = pane.deadWoodPctSpinner != null ? pane.deadWoodPctSpinner.getValue() : 10;
            pane.gameView.getGameApp().setTreeSpeciesComposition(oak, pine, acacia, cactus, birch, bamboo, deadwood);
        }
    }

    static void generateRoots(WorldEditorPane pane, double globalDensity) {
        List<BotanicalTreeData> trees = getBotanicalTreeInstances(pane);

        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                for (int d = 0; d < SOIL_DEPTH; d++) {
                    pane.rootGrid[x][y][d] = 0.0f;

                    double bgRoot = 0.0;
                    if (d < 4) {
                        double noiseBg = pane.valueNoise3D(x * 0.3, y * 0.3, d * 0.5 + 12);
                        bgRoot = (0.04 + noiseBg * 0.08) * (1.0 - d / 4.0) * globalDensity;
                    }

                    double maxTreeRoot = 0.0;
                    for (BotanicalTreeData tree : trees) {
                        double dx = x - tree.gx;
                        double dy = y - tree.gy;
                        double distSq = dx * dx + dy * dy;
                        double dist = Math.sqrt(distSq);

                        double baseRadius;
                        int maxDepth;
                        double stumpThickness;
                        double speciesWeight;

                        switch (tree.speciesIdx) {
                            case 0 -> {
                                baseRadius = 15.0 * tree.ageScale; maxDepth = 24; stumpThickness = 3.6 * tree.ageScale; speciesWeight = 1.0;
                            }
                            case 1 -> {
                                baseRadius = 13.0 * tree.ageScale; maxDepth = 22; stumpThickness = 3.2 * tree.ageScale; speciesWeight = 0.95;
                            }
                            case 2 -> {
                                baseRadius = 12.0 * tree.ageScale; maxDepth = 20; stumpThickness = 3.0 * tree.ageScale; speciesWeight = 0.88;
                            }
                            case 3 -> {
                                baseRadius = 5.5 * tree.ageScale; maxDepth = 7; stumpThickness = 1.5 * tree.ageScale; speciesWeight = 0.55;
                            }
                            case 4 -> {
                                baseRadius = 10.0 * tree.ageScale; maxDepth = 15; stumpThickness = 2.6 * tree.ageScale; speciesWeight = 0.80;
                            }
                            case 5 -> {
                                baseRadius = 6.0 * tree.ageScale; maxDepth = 9; stumpThickness = 1.8 * tree.ageScale; speciesWeight = 0.65;
                            }
                            case 6 -> {
                                baseRadius = 8.0 * tree.ageScale; maxDepth = 14; stumpThickness = 3.0 * tree.ageScale; speciesWeight = 0.85;
                            }
                            default -> {
                                baseRadius = 11.0 * tree.ageScale; maxDepth = 18; stumpThickness = 2.8 * tree.ageScale; speciesWeight = 0.85;
                            }
                        }

                        if (dist > baseRadius || d > maxDepth) continue;

                        double distRatio = Math.max(0.0, 1.0 - (dist / baseRadius));
                        double depthRatio = Math.max(0.0, 1.0 - ((double) d / maxDepth));
                        double rVal = 0.0;

                        if (dist <= stumpThickness && d < (maxDepth * 0.45)) {
                            double coreDepthRatio = Math.max(0.0, 1.0 - ((double) d / (maxDepth * 0.45)));
                            double coreNoise = pane.valueNoise3D(x * 0.4, y * 0.4, d * 0.5 + 77);
                            double stumpVal = (0.80 + 0.20 * coreNoise) * coreDepthRatio * speciesWeight * Math.max(0.65, globalDensity) * 1.4;
                            rVal = Math.max(rVal, stumpVal);
                        }

                        double angle = Math.atan2(dy, dx);
                        double radialNoise = Math.sin(angle * 5.0 + tree.gx * 0.5) * 0.5 + 0.5;
                        double noiseBranch = pane.valueNoise3D(x * 0.22, y * 0.22, d * 0.35 + 100);
                        double lateralVal = distRatio * depthRatio * speciesWeight * globalDensity * (0.45 + noiseBranch * 0.8);
                        if (radialNoise > 0.55) lateralVal *= 1.45;
                        rVal = Math.max(rVal, lateralVal);

                        if (rVal > maxTreeRoot) maxTreeRoot = rVal;
                    }

                    double totalRoot = Math.max(bgRoot, maxTreeRoot);
                    pane.rootGrid[x][y][d] = (float) Math.min(1.0, totalRoot);
                }
            }
        }
    }

    private static int[] getSurfaceComposition(WorldEditorPane pane) {
        int e = pane.earthSpinner != null ? pane.earthSpinner.getValue() : 50;
        int s = pane.sandSpinner != null ? pane.sandSpinner.getValue() : 20;
        int c = pane.claySpinner != null ? pane.claySpinner.getValue() : 20;
        int st = pane.stoneSpinner != null ? pane.stoneSpinner.getValue() : 10;
        int org = pane.organicSpinner != null ? pane.organicSpinner.getValue() : 10;
        int total = Math.max(1, e + s + c + st + org);
        return new int[]{e * 100 / total, s * 100 / total, c * 100 / total, st * 100 / total, org * 100 / total};
    }

    static boolean isNearRiver(WorldEditorPane pane, int gx, int gy, int radius) {
        return TerrainProceduralGenerator.isNearRiver(pane.riverPath, gx, gy, radius);
    }

    private static long parseSeed(javafx.scene.control.TextField field, long defaultVal) {
        if (field == null || field.getText() == null) return defaultVal;
        try {
            return Long.parseLong(field.getText().trim());
        } catch (Exception ignored) {
            return defaultVal;
        }
    }
}
