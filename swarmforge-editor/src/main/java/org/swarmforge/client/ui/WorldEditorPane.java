/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import org.swarmforge.client.util.I18nManager;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.Font;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * World Editor Pane for SwarmForge Studio.
 * Features 3D, Top-Down, and Side synchronized views, manual 3D sculpting brushes,
 * sub-millimeter voxel scale, soil substrates, micro-hydrology, planar water bodies,
 * species-adapted world generator, and multi-LOD voxel scaling.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class WorldEditorPane extends BorderPane {

    // Canvases (3-View system identical to NestGeneratorPane)
    private Canvas canvas3D, canvasSide, canvasTop;
    private GraphicsContext gc3D, gcSide, gcTop;

    // 3D Camera Controls
    private double azimuth = 45, elevation = 35, zoom = 7.5;
    private double pan3DX = 0, pan3DY = 0;
    private double lastMX, lastMY;

    // 2D Side View Camera
    private double sideZoom = 1.0;
    private double sidePanX = 0, sidePanY = 0;
    private double lastSideMX, lastSideMY;

    // 2D Top View Camera
    private double topZoom = 1.0;
    private double topPanX = 0, topPanY = 0;
    private double lastTopMX, lastTopMY;

    // Preset Manager & ComboBox
    private final WorldPresetManager presetManager = new WorldPresetManager();
    private ComboBox<String> presetsCombo;

    // Adapt World to Species ComboBox & Action Controls
    private ComboBox<String> speciesAdaptCombo;
    private Label lblAdaptStatus;

    // View Synchronization CheckBox & Viewport Mode Label
    private CheckBox syncViewsCheckBox;
    private Label lblViewportMode;

    // Controls: 0. Terrain Source & Geo Location
    private TextField cityNameField;
    private TextField citySearchField;
    private Label geoStatusLabel;
    private TextField latField;
    private TextField lonField;

    // Controls: Block Seeds & Procedural Generators
    private TextField reliefSeedField;
    private TextField soilSeedField;
    private TextField hydroSeedField;
    private TextField structSeedField;
    private TextField floraSeedField;

    // Mountain Slope Controls
    private Slider slopeAngleSlider;
    private ComboBox<String> slopeDirectionCombo;

    // Controls: Layer, Vegetation & Nest Visibility Toggles
    private CheckBox showEarthCheck;
    private CheckBox showSandCheck;
    private CheckBox showClayCheck;
    private CheckBox showStoneCheck;
    private CheckBox showOrganicCheck;
    private CheckBox showVegetationCheck;
    private CheckBox showGalleriesCheck;
    private CheckBox showSubstrateStratigraphyCheck;
    private Label lblHoverInfo;

    // Controls: 1. Scale & Resolution
    private Slider surfaceSizeSlider; // Mètres (0.5 - 50.0m)
    private Slider depthSlider;       // Profondeur Souterraine (0.2 - 5.0m)
    private Slider resolutionSlider; // Sub-millimétrique (0.1 - 1.0mm)
    private Label lblVoxelMemoryEstimate;

    // Controls: 2. Soil & Relief
    private Slider roughnessSlider;
    private Slider compactionSlider;
    private Spinner<Integer> earthSpinner;
    private Spinner<Integer> sandSpinner;
    private Spinner<Integer> claySpinner;
    private Spinner<Integer> stoneSpinner;
    private Spinner<Integer> organicSpinner;

    // Controls: 3. Flora & Surface Cover Ecosystem
    private Slider leafLitterSlider;
    private Slider twigDebrisSlider;
    private Slider edibleDensitySlider;
    private Slider nonEdibleDensitySlider;
    private CheckBox aphidPlantCheck;
    private CheckBox nectarFlowersCheck;
    private CheckBox seedGrassCheck;
    private CheckBox fungusFoliageCheck;
    private CheckBox mossCheck;
    private CheckBox pineLitterCheck;
    private CheckBox fernObstacleCheck;

    // Generated Procedural Surface Flora & Litter Instances (Determined by floraSeed)
    private static class SurfaceFloraItem {
        int gx, gy;
        int type; // 0=Grass, 1=AphidPlant, 2=NectarFlower, 3=Moss, 4=PineLitter, 5=TwigDebris, 6=Pebble
        double scale;
        double rotation;

        SurfaceFloraItem(int gx, int gy, int type, double scale, double rotation) {
            this.gx = gx; this.gy = gy; this.type = type; this.scale = scale; this.rotation = rotation;
        }
    }
    private List<SurfaceFloraItem> surfaceFloraItems = new ArrayList<>();

    public enum RenderMode { REALISTIC, SCIENTIFIC, GAMIFIED }
    private RenderMode currentRenderMode = RenderMode.REALISTIC;

    // Viewport Layer Visibility Flags
    private boolean isSimulationMode = false;
    private boolean isTerrainVisible = true;
    private boolean isGalleriesVisible = true;
    private boolean isPheromonesVisible = true;
    private boolean isColonyVisible = true;
    private boolean isWeatherVisible = true;
    private boolean isDualMinimapVisible = true;

    private ScrollPane configScrollPane;
    private VBox sideMinimapsBox;

    public void setHideConfigPanel(boolean hide) {
        if (hide) {
            setLeft(null);
        } else {
            if (configScrollPane == null) {
                configScrollPane = buildConfig();
            }
            setLeft(configScrollPane);
        }
    }

    public void setSimulationMode(boolean simMode) {
        this.isSimulationMode = simMode;
        repaintAllViews();
    }

    public void setTerrainVisible(boolean visible) {
        this.isTerrainVisible = visible;
        repaintAllViews();
    }

    public void setGalleriesVisible(boolean visible) {
        this.isGalleriesVisible = visible;
        repaintAllViews();
    }

    public void setPheromonesVisible(boolean visible) {
        this.isPheromonesVisible = visible;
        repaintAllViews();
    }

    public void setColonyVisible(boolean visible) {
        this.isColonyVisible = visible;
        repaintAllViews();
    }

    public void setWeatherVisible(boolean visible) {
        this.isWeatherVisible = visible;
        repaintAllViews();
    }

    public void setDualMinimapVisible(boolean visible) {
        this.isDualMinimapVisible = visible;
        if (sideMinimapsBox != null) {
            sideMinimapsBox.setVisible(visible);
            sideMinimapsBox.setManaged(visible);
        }
        repaintAllViews();
    }

    public void setRenderMode(RenderMode mode) {
        this.currentRenderMode = mode != null ? mode : RenderMode.REALISTIC;
        repaintAllViews();
    }

    public RenderMode getRenderMode() {
        return currentRenderMode;
    }

    // Controls: 4. Hydrology
    private CheckBox riverCheck;
    private Slider riverWidthSlider;
    private Slider riverVelocitySlider;
    private Slider staticPoolsSlider;
    private Slider waterTableDepthSlider;

    // Controls: 5. Vertical Host Structures & Botanical Tree Species
    private Slider treeCountSlider;
    private Slider hollowLogsSlider;
    private Slider rockCrevicesSlider;

    // Botanical Tree Species Composition Spinners & Selectors
    private ComboBox<String> comboTreeSpecies;
    private Spinner<Integer> oakPctSpinner;
    private Spinner<Integer> pinePctSpinner;
    private Spinner<Integer> acaciaPctSpinner;
    private Spinner<Integer> birchPctSpinner;
    private Spinner<Integer> bambooPctSpinner;
    private Spinner<Integer> cactusPctSpinner;
    private Spinner<Integer> deadWoodPctSpinner;

    // Bioclimatic Zone Badge & Insect Compatibility Labels
    private Label lblBioclimaticZoneBadge = new Label("🌳 Forêt Tempérée Décidue");
    private Label lblAttaCompatScore = new Label("85%");
    private Label lblAphidCompatScore = new Label("70%");
    private Label lblWoodNestCompatScore = new Label("90%");
    private Label lblAcaciaAntCompatScore = new Label("60%");
    private Label lblCactusAntCompatScore = new Label("40%");
    private Label lblPogonomyrmexCompatScore = new Label("75%");
    private Label lblTermiteCompatScore = new Label("80%");
    private Label lblApisCompatScore = new Label("85%");
    private Label lblVespulaCompatScore = new Label("70%");
    private Label lblSolenopsisCompatScore = new Label("65%");

    // Controls: 7. 3D Sculpting Brushes & Voxel Painting Mode
    private CheckBox enableSculptingCheck;
    private ComboBox<String> brushModeSelect;
    private ComboBox<String> brushSubstrateSelect;
    private Slider brushRadiusSlider;
    private Slider brushStrengthSlider;

    // Local Voxel & Terrain Grid for Real-Time Sculpting (128x128x32)
    private static final int GRID_SIZE = 128;
    private static final int SOIL_DEPTH = 32;
    private double[][] heightGrid = new double[GRID_SIZE][GRID_SIZE];
    private byte[][][] soilLayers  = new byte[GRID_SIZE][GRID_SIZE][SOIL_DEPTH];
    private float[][]  humidityGrid = new float[GRID_SIZE][GRID_SIZE];  // 0.0-1.0
    private boolean[][][] voidGrid  = new boolean[GRID_SIZE][GRID_SIZE][SOIL_DEPTH]; // cavernes
    private boolean[][] carvedVoxelGrid = new boolean[GRID_SIZE][GRID_SIZE];
    private List<int[]> riverPath = new ArrayList<>();

    // Substrate Generation Sliders
    private Slider stratificationSlider;
    private Slider mixingRateSlider;
    private Slider baseHumiditySlider;
    private Slider voidDensitySlider;
    private CheckBox showHumidityCheck;

    // Volumetric & Rendering Options
    private CheckBox useAdvancedVolumetricModeCheck;
    private CheckBox showChamferedBezelCheck;
    private CheckBox showGravelInclusionsCheck;
    private CheckBox enableVolumetricScannerCheck;
    private Slider slicePlaneSlider;
    private CheckBox showTranslucentVolumetricModeCheck;

    // Callbacks
    private Consumer<Map<String, Object>> onGenerateCallback;

    public WorldEditorPane() {
        initDefaultTerrainGrid();
        setTop(buildHeader());
        setLeft(buildConfig());
        setCenter(buildViews());
        repaintAllViews();
    }

    private void initDefaultTerrainGrid() {
        double r = roughnessSlider != null ? roughnessSlider.getValue() : 0.45;
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                double nx = (double) x / GRID_SIZE;
                double ny = (double) y / GRID_SIZE;
                double noiseVal = fBmNoise(nx * 3.5, ny * 3.5, 4, 0.5, 2.0);
                heightGrid[x][y] = 0.20 + 0.55 * noiseVal;
                heightGrid[x][y] = Math.max(0.05, Math.min(0.95, heightGrid[x][y]));
                carvedVoxelGrid[x][y] = false;
            }
        }
        generateHumidity(0.35);
        riverPath = computeRiverPath();
        carveRiverBed();
        generateSoilLayers(0.7, 0.3);
        generateVoids(0.08);
    }

    private void carveRiverBed() {
        if (riverCheck == null || !riverCheck.isSelected() || riverPath == null || riverPath.isEmpty()) return;
        double rWidthMm = riverWidthSlider != null ? riverWidthSlider.getValue() : 120.0;
        double riverWidthVox = Math.max(2.0, rWidthMm / 25.0);
        int rRad = (int) Math.max(1, Math.round(riverWidthVox / 2.0));
        for (int i = 0; i < riverPath.size(); i++) {
            int[] pt = riverPath.get(i);
            int rx = pt[0], ry = pt[1];
            double riverZ = heightGrid[rx][ry];
            for (int dx = -rRad - 2; dx <= rRad + 2; dx++) {
                for (int dy = -rRad - 2; dy <= rRad + 2; dy++) {
                    int cx = rx + dx, cy = ry + dy;
                    if (cx >= 0 && cx < GRID_SIZE && cy >= 0 && cy < GRID_SIZE) {
                        double dist = Math.sqrt(dx * dx + dy * dy);
                        if (dist <= rRad + 2) {
                            double factor = Math.max(0, 1.0 - dist / (rRad + 2.5));
                            double targetZ = riverZ - 0.03 * factor;
                            heightGrid[cx][cy] = Math.min(heightGrid[cx][cy], Math.max(0.02, targetZ + (1.0 - factor) * 0.05));
                        }
                    }
                }
            }
        }
    }

    private List<int[]> computeRiverPath() {
        List<int[]> path = new ArrayList<>();
        if (riverCheck == null || !riverCheck.isSelected()) return path;

        int startX = 25 + (GRID_SIZE - 50) / 3;
        int cx = startX, cy = 0;
        Random rand = new Random(1337);

        while (cy < GRID_SIZE && cx >= 0 && cx < GRID_SIZE) {
            path.add(new int[]{cx, cy});
            if (cy == GRID_SIZE - 1) break;
            cy++;
            if (rand.nextDouble() < 0.40) {
                cx += (rand.nextBoolean() ? 1 : -1);
            }
            cx = Math.max(4, Math.min(GRID_SIZE - 5, cx));
        }

        // Post-process river elevation along path: strictly monotonic downhill descent
        if (!path.isEmpty()) {
            double currentZ = Math.max(0.35, heightGrid[path.get(0)[0]][path.get(0)[0]]);
            double maxDescent = 0.003;
            for (int i = 0; i < path.size(); i++) {
                int[] pt = path.get(i);
                int px = pt[0], py = pt[1];
                currentZ = Math.min(currentZ - maxDescent, heightGrid[px][py]);
                currentZ = Math.max(0.03, currentZ);
                heightGrid[px][py] = currentZ;
            }
        }

        return path;
    }

    private void generateSoilLayers(double stratification, double mixing) {
        int[] surfacePct = getSurfaceComposition();
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                for (int d = 0; d < SOIL_DEPTH; d++) {
                    double depthRatio = (double) d / (SOIL_DEPTH - 1);
                    byte idealMat;
                    if (depthRatio < 0.15) idealMat = 0;       // Humus / Terre
                    else if (depthRatio < 0.45) idealMat = 2;  // Argile
                    else if (depthRatio < 0.75) idealMat = 3;  // Pierre
                    else idealMat = 3;                          // Bedrock

                    double noise = valueNoise3D(x * 0.25, y * 0.25, d * 0.6 + 10);

                    byte mat;
                    if (d == 0) {
                        mat = pickSurfaceMaterialCoherent(x, y, surfacePct);
                    } else {
                        double blend = stratification + (noise - 0.5) * mixing * 2.0;
                        if (blend > 0.5) {
                            mat = idealMat;
                        } else {
                            mat = pickSurfaceMaterialCoherent(x, y, surfacePct);
                        }
                    }
                    soilLayers[x][y][d] = mat;
                }
            }
        }
    }

    private byte pickSurfaceMaterialCoherent(int x, int y, int[] surfacePct) {
        if (riverCheck != null && riverCheck.isSelected() && isNearRiver(x, y, 3)) {
            double rNoise = valueNoise3D(x * 0.15, y * 0.15, 88);
            return rNoise < 0.6 ? (byte) 1 : (byte) 3;
        }
        double h = heightGrid[x][y];
        if (h > 0.72) return 3;
        if (h < 0.28 && humidityGrid[x][y] > 0.45) return 2;

        double spatialNoise = valueNoise3D(x * 0.05, y * 0.05, 12);
        return pickSurfaceMaterial(surfacePct, spatialNoise);
    }

    private boolean isNearRiver(int x, int y, int distMax) {
        if (riverPath == null || riverPath.isEmpty()) return false;
        for (int[] pt : riverPath) {
            if (Math.abs(pt[0] - x) <= distMax && Math.abs(pt[1] - y) <= distMax) {
                if ((pt[0] - x) * (pt[0] - x) + (pt[1] - y) * (pt[1] - y) <= distMax * distMax) {
                    return true;
                }
            }
        }
        return false;
    }

    private int[] getSurfaceComposition() {
        int e = earthSpinner  != null ? earthSpinner.getValue()   : 50;
        int s = sandSpinner   != null ? sandSpinner.getValue()    : 20;
        int c = claySpinner   != null ? claySpinner.getValue()    : 20;
        int st= stoneSpinner  != null ? stoneSpinner.getValue()   : 10;
        int total = Math.max(1, e + s + c + st);
        return new int[]{e * 100 / total, s * 100 / total, c * 100 / total, st * 100 / total};
    }

    private byte pickSurfaceMaterial(int[] pct, double rand01) {
        double r = rand01 * 100;
        if (r < pct[0]) return 0;
        r -= pct[0];
        if (r < pct[1]) return 1;
        r -= pct[1];
        if (r < pct[2]) return 2;
        return 3;
    }

    private void generateHumidity(double baseHumidity) {
        double wtDepth = waterTableDepthSlider != null ? waterTableDepthSlider.getValue() : 15;
        double wtFactor = 1.0 - Math.min(1.0, wtDepth / 50.0);
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                double noise = valueNoise3D(x * 0.2, y * 0.2, 99);
                humidityGrid[x][y] = (float) Math.max(0, Math.min(1,
                        baseHumidity + wtFactor * 0.3 + (noise - 0.5) * 0.25));
            }
        }
    }

    private void generateVoids(double density) {
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                for (int d = 0; d < SOIL_DEPTH; d++) {
                    voidGrid[x][y][d] = false;
                    if (d < 2) continue;
                    double noise = valueNoise3D(x * 0.3, y * 0.3, d * 0.8);
                    double threshold = 1.0 - density * (0.5 + 0.5 * d / SOIL_DEPTH);
                    voidGrid[x][y][d] = (noise > threshold);
                    if (voidGrid[x][y][d] && soilLayers[x][y][d] == 1) {
                        voidGrid[x][y][d] = false;
                    }
                }
            }
        }
    }


    private long parseSeed(TextField field, long defaultSeed) {
        if (field != null && field.getText() != null) {
            try { return Long.parseLong(field.getText().trim()); } catch (Exception ignored) {}
        }
        return defaultSeed;
    }

    private double fBmNoiseSeeded(double x, double y, int octaves, double persistence, double lacunarity, long seed) {
        double total = 0;
        double frequency = 1.0;
        double amplitude = 1.0;
        double maxValue = 0;
        double offset = (seed % 10000) * 0.1;
        for (int i = 0; i < octaves; i++) {
            total += valueNoise3D(x * frequency + i * 17.3 + offset, y * frequency + i * 31.7 + offset, i * 11.1 + offset) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }
        return total / maxValue;
    }

    private void generateSoilLayersSeeded(double stratification, double mixing, long seed) {
        int[] surfacePct = getSurfaceComposition();
        double offset = (seed % 10000) * 0.1;
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                for (int d = 0; d < SOIL_DEPTH; d++) {
                    double depthRatio = (double) d / (SOIL_DEPTH - 1);
                    byte idealMat;
                    if (depthRatio < 0.15) idealMat = 0;       // Humus / Terre
                    else if (depthRatio < 0.45) idealMat = 2;  // Argile
                    else if (depthRatio < 0.75) idealMat = 3;  // Pierre
                    else idealMat = 3;                          // Bedrock

                    double noise = valueNoise3D(x * 0.25 + offset, y * 0.25 + offset, d * 0.6 + 10 + offset);

                    byte mat;
                    if (d == 0) {
                        mat = pickSurfaceMaterialCoherent(x, y, surfacePct);
                    } else {
                        double blend = stratification + (noise - 0.5) * mixing * 2.0;
                        if (blend > 0.5) {
                            mat = idealMat;
                        } else {
                            mat = pickSurfaceMaterialCoherent(x, y, surfacePct);
                        }
                    }
                    soilLayers[x][y][d] = mat;
                }
            }
        }
    }

    private void regenerateAndRepaint() {
        double r = roughnessSlider != null ? roughnessSlider.getValue() : 0.45;
        double slope = slopeAngleSlider != null ? slopeAngleSlider.getValue() / 100.0 : 0.0;
        int slopeDir = slopeDirectionCombo != null ? slopeDirectionCombo.getSelectionModel().getSelectedIndex() : 0;

        long rSeed = parseSeed(reliefSeedField, 774829L);
        long sSeed = parseSeed(soilSeedField, 123456L);

        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                double nx = (double) x / GRID_SIZE;
                double ny = (double) y / GRID_SIZE;
                double noiseVal = fBmNoiseSeeded(nx * 3.5, ny * 3.5, 4, 0.5, 2.0, rSeed);

                double slopeGrad = switch (slopeDir) {
                    case 0 -> ny; // Sud -> Nord incline
                    case 1 -> 1.0 - ny; // Nord -> Sud incline
                    case 2 -> nx; // Ouest -> Est incline
                    case 3 -> 1.0 - nx; // Est -> Ouest incline
                    default -> ny;
                };

                double baseH = 0.15 + 0.50 * noiseVal * (0.4 + 0.6 * r) + slope * 0.70 * slopeGrad;
                heightGrid[x][y] = Math.max(0.02, Math.min(0.98, baseH));
            }
        }
        double strat = stratificationSlider != null ? stratificationSlider.getValue() : 0.7;
        double mix   = mixingRateSlider    != null ? mixingRateSlider.getValue()    : 0.3;
        double hum   = baseHumiditySlider  != null ? baseHumiditySlider.getValue()  : 0.35;
        double voids = voidDensitySlider   != null ? voidDensitySlider.getValue()   : 0.08;

        generateHumidity(hum);
        riverPath = computeRiverPath();
        carveRiverBed();
        generateSoilLayersSeeded(strat, mix, sSeed);
        generateVoids(voids);
        generateSurfaceFlora();
        updateVoxelMemoryEstimate();
        repaintAllViews();
    }

    private void generateSurfaceFlora() {
        surfaceFloraItems.clear();
        long seed = 774829L;
        if (floraSeedField != null) {
            try {
                seed = Long.parseLong(floraSeedField.getText().trim());
            } catch (Exception ignored) { }
        }

        Random rand = new Random(seed);

        double ediblePct = edibleDensitySlider != null ? edibleDensitySlider.getValue() / 100.0 : 0.4;
        double nonEdiblePct = nonEdibleDensitySlider != null ? nonEdibleDensitySlider.getValue() / 100.0 : 0.6;
        double litterPct = leafLitterSlider != null ? leafLitterSlider.getValue() / 100.0 : 0.5;
        double debrisPct = twigDebrisSlider != null ? twigDebrisSlider.getValue() / 100.0 : 0.4;

        int step = 3;
        for (int x = 4; x < GRID_SIZE - 4; x += step) {
            for (int y = 4; y < GRID_SIZE - 4; y += step) {
                double r = rand.nextDouble();
                if (r < ediblePct * 0.35) {
                    if (seedGrassCheck != null && seedGrassCheck.isSelected() && rand.nextDouble() < 0.5) {
                        surfaceFloraItems.add(new SurfaceFloraItem(x, y, 0, 0.7 + rand.nextDouble() * 0.5, rand.nextDouble() * 360));
                    } else if (aphidPlantCheck != null && aphidPlantCheck.isSelected() && rand.nextDouble() < 0.35) {
                        surfaceFloraItems.add(new SurfaceFloraItem(x, y, 1, 0.8 + rand.nextDouble() * 0.4, rand.nextDouble() * 360));
                    } else if (nectarFlowersCheck != null && nectarFlowersCheck.isSelected() && rand.nextDouble() < 0.35) {
                        surfaceFloraItems.add(new SurfaceFloraItem(x, y, 2, 0.6 + rand.nextDouble() * 0.5, rand.nextDouble() * 360));
                    }
                }
                if (mossCheck != null && mossCheck.isSelected() && humidityGrid[x][y] > 0.35 && rand.nextDouble() < nonEdiblePct * 0.45) {
                    surfaceFloraItems.add(new SurfaceFloraItem(x, y, 3, 0.6 + rand.nextDouble() * 0.8, rand.nextDouble() * 360));
                }
                if (pineLitterCheck != null && pineLitterCheck.isSelected() && rand.nextDouble() < litterPct * 0.4) {
                    surfaceFloraItems.add(new SurfaceFloraItem(x, y, 4, 0.5 + rand.nextDouble() * 0.7, rand.nextDouble() * 360));
                }
                if (rand.nextDouble() < debrisPct * 0.3) {
                    int debrisType = rand.nextBoolean() ? 5 : 6;
                    surfaceFloraItems.add(new SurfaceFloraItem(x, y, debrisType, 0.5 + rand.nextDouble() * 0.6, rand.nextDouble() * 360));
                }
            }
        }
    }

    private double fBmNoise(double x, double y, int octaves, double persistence, double lacunarity) {
        double total = 0;
        double frequency = 1.0;
        double amplitude = 1.0;
        double maxValue = 0;
        for (int i = 0; i < octaves; i++) {
            total += valueNoise3D(x * frequency + i * 17.3, y * frequency + i * 31.7, i * 11.1) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }
        return total / maxValue;
    }

    private double valueNoise3D(double x, double y, double z) {
        int ix=(int)Math.floor(x), iy=(int)Math.floor(y), iz=(int)Math.floor(z);
        double fx=x-ix, fy=y-iy, fz=z-iz;
        fx=fx*fx*(3-2*fx); fy=fy*fy*(3-2*fy); fz=fz*fz*(3-2*fz);
        double c000=h3(ix,iy,iz),   c100=h3(ix+1,iy,iz),   c010=h3(ix,iy+1,iz),   c110=h3(ix+1,iy+1,iz);
        double c001=h3(ix,iy,iz+1), c101=h3(ix+1,iy,iz+1), c011=h3(ix,iy+1,iz+1), c111=h3(ix+1,iy+1,iz+1);
        return lrp(bilerp(c000,c100,c010,c110,fx,fy), bilerp(c001,c101,c011,c111,fx,fy), fz);
    }
    private double h3(int x,int y,int z){int n=x*1301+y*14057+z*199999;n^=(n>>>8);n*=0x45d9f3b;n^=(n>>>8);return((n&0x7fffffff)/(double)0x7fffffff);}
    private double lrp(double a,double b,double t){return a+t*(b-a);}
    private double bilerp(double c00,double c10,double c01,double c11,double u,double v){return lrp(lrp(c00,c10,u),lrp(c01,c10,u),v);}

    private VBox buildHeader() {
        VBox v = new VBox(8);
        v.setPadding(new Insets(8, 12, 6, 12));
        v.setStyle("-fx-background-color: #18181b; -fx-border-color: #27272a; -fx-border-width: 0 0 1 0;");

        HBox r = new HBox(10);
        r.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Éditeur de Monde");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label lblPreset = new Label("Preset :");
        lblPreset.setStyle("-fx-font-weight: bold; -fx-text-fill: #e4e4e7;");
        lblPreset.setGraphic(new FontIcon(Feather.SLIDERS));

        presetsCombo = new ComboBox<>();
        presetsCombo.setEditable(true);
        presetsCombo.setPromptText("Sélectionner un preset...");
        presetsCombo.getItems().setAll(presetManager.names());
        presetsCombo.setTooltip(new Tooltip("Sélectionnez un preset de monde 3D pré-configuré (Forêt, Désert, Rivières, etc.)."));
        presetsCombo.setPrefWidth(220);
        presetsCombo.setOnAction(e -> {
            String selected = presetsCombo.getValue();
            if (selected != null && presetManager.contains(selected)) {
                loadConfiguration(presetManager.get(selected));
            }
        });

        Button bSave = new Button(I18nManager.getInstance().get("common.btn.save"));
        bSave.setGraphic(new FontIcon(Feather.SAVE));
        bSave.getStyleClass().add("btn-secondary");
        bSave.setTooltip(new Tooltip("Enregistrer la configuration actuelle du monde comme nouveau preset."));
        bSave.setOnAction(e -> handleSavePreset());

        Button bDelete = new Button(I18nManager.getInstance().get("common.btn.delete"));
        bDelete.setGraphic(new FontIcon(Feather.TRASH_2));
        bDelete.getStyleClass().add("btn-danger");
        bDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold;");
        bDelete.setTooltip(new Tooltip("Supprimer le preset de monde sélectionné."));
        bDelete.setOnAction(e -> handleDeletePreset());

        Button bExport = new Button(I18nManager.getInstance().get("common.btn.export"));
        bExport.setGraphic(new FontIcon(Feather.DOWNLOAD));
        bExport.getStyleClass().add("btn-secondary");
        bExport.setOnAction(e -> doExport());

        Button bImport = new Button(I18nManager.getInstance().get("common.btn.import"));
        bImport.setGraphic(new FontIcon(Feather.UPLOAD));
        bImport.getStyleClass().add("btn-secondary");
        bImport.setOnAction(e -> doImport());

        r.getChildren().addAll(title, sp, lblPreset, presetsCombo, bSave, bDelete, new Separator(Orientation.VERTICAL), bExport, bImport);

        Label subtitle = new Label("Génération de relief, sol, ouvert végétal, hydrographie planaires, sculpture 3D & déformation voxel (0.1-1.0mm)");
        subtitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        v.getChildren().addAll(r, subtitle, new Separator());
        return v;
    }

    private void handleAdaptWorldToSpecies() {
        int idx = speciesAdaptCombo.getSelectionModel().getSelectedIndex();
        switch (idx) {
            case 0 -> { // Apis mellifera (Flowering Meadow)
                surfaceSizeSlider.setValue(25.0); depthSlider.setValue(3.0);
                nectarFlowersCheck.setSelected(true); edibleDensitySlider.setValue(80);
                staticPoolsSlider.setValue(2);
            }
            case 1 -> { // Atta sexdens (Tropical Rainforest)
                if (presetsCombo.getItems().contains("Forêt Tropicale (Manaus, BR)")) {
                    presetsCombo.getSelectionModel().select("Forêt Tropicale (Manaus, BR)");
                } else {
                    surfaceSizeSlider.setValue(25.0); depthSlider.setValue(3.0); baseHumiditySlider.setValue(0.85);
                    earthSpinner.getValueFactory().setValue(45); claySpinner.getValueFactory().setValue(40);
                    comboTreeSpecies.getSelectionModel().select(2); // Acacia / Canopy
                    fungusFoliageCheck.setSelected(true); aphidPlantCheck.setSelected(true);
                }
            }
            case 2 -> { // Camponotus ligniperda (Oak Forest & Dead Wood)
                surfaceSizeSlider.setValue(25.0); depthSlider.setValue(3.0);
                comboTreeSpecies.getSelectionModel().select(6); // Bois mort
                deadWoodPctSpinner.getValueFactory().setValue(50); oakPctSpinner.getValueFactory().setValue(40);
                hollowLogsSlider.setValue(6); treeCountSlider.setValue(8);
            }
            case 3 -> { // Cataglyphis bombycina (Sahara Arid Desert)
                if (presetsCombo.getItems().contains("Désert Aride (Erg Chebbi, MA)")) {
                    presetsCombo.getSelectionModel().select("Désert Aride (Erg Chebbi, MA)");
                } else {
                    surfaceSizeSlider.setValue(25.0); depthSlider.setValue(3.0); baseHumiditySlider.setValue(0.12);
                    sandSpinner.getValueFactory().setValue(80); stoneSpinner.getValueFactory().setValue(15);
                    comboTreeSpecies.getSelectionModel().select(3); // Cactus
                    riverCheck.setSelected(false);
                }
            }
            case 4 -> { // Formica rufa (Taiga Boreal Forest)
                if (presetsCombo.getItems().contains("Taïga Boréale (Rovaniemi, FI)")) {
                    presetsCombo.getSelectionModel().select("Taïga Boréale (Rovaniemi, FI)");
                } else {
                    surfaceSizeSlider.setValue(25.0); depthSlider.setValue(3.0); baseHumiditySlider.setValue(0.55);
                    earthSpinner.getValueFactory().setValue(40); stoneSpinner.getValueFactory().setValue(25);
                    comboTreeSpecies.getSelectionModel().select(1); // Pinède
                    pineLitterCheck.setSelected(true); mossCheck.setSelected(true);
                }
            }
            case 5 -> { // Lasius niger (Temperate Forest)
                if (presetsCombo.getItems().contains("Tempéré Decidu (Fontainebleau, FR)")) {
                    presetsCombo.getSelectionModel().select("Tempéré Decidu (Fontainebleau, FR)");
                } else {
                    surfaceSizeSlider.setValue(25.0); depthSlider.setValue(3.0); baseHumiditySlider.setValue(0.45);
                    earthSpinner.getValueFactory().setValue(50); sandSpinner.getValueFactory().setValue(20);
                    comboTreeSpecies.getSelectionModel().select(0); // Chênes
                    aphidPlantCheck.setSelected(true); nectarFlowersCheck.setSelected(true);
                }
            }
            case 6 -> { // Messor barbarus (Semi-Arid Steppe)
                if (presetsCombo.getItems().contains("Steppe Semi-Aride (Astana, KZ)")) {
                    presetsCombo.getSelectionModel().select("Steppe Semi-Aride (Astana, KZ)");
                } else {
                    surfaceSizeSlider.setValue(25.0); depthSlider.setValue(3.0); baseHumiditySlider.setValue(0.30);
                    sandSpinner.getValueFactory().setValue(35); earthSpinner.getValueFactory().setValue(45);
                    seedGrassCheck.setSelected(true);
                }
            }
            case 7 -> { // Pseudomyrmex gracilis (Acacia Savanna)
                if (presetsCombo.getItems().contains("Savane d'Acacias (Serengeti, TZ)")) {
                    presetsCombo.getSelectionModel().select("Savane d'Acacias (Serengeti, TZ)");
                } else {
                    comboTreeSpecies.getSelectionModel().select(2); // Acacia
                    acaciaPctSpinner.getValueFactory().setValue(80); nectarFlowersCheck.setSelected(true);
                }
            }
            case 8 -> { // Reticulitermes lucifugus (Subterranean Termite)
                surfaceSizeSlider.setValue(25.0); depthSlider.setValue(3.0); voidDensitySlider.setValue(0.20);
                baseHumiditySlider.setValue(0.60); deadWoodPctSpinner.getValueFactory().setValue(60);
                hollowLogsSlider.setValue(7);
            }
            case 9 -> { // Solenopsis invicta (Moist Grassland & Savanna)
                surfaceSizeSlider.setValue(25.0); depthSlider.setValue(3.0); baseHumiditySlider.setValue(0.70);
                earthSpinner.getValueFactory().setValue(60); claySpinner.getValueFactory().setValue(25);
                waterTableDepthSlider.setValue(10); staticPoolsSlider.setValue(3);
            }
        }
        regenerateAndRepaint();
        if (lblAdaptStatus != null) {
            lblAdaptStatus.setText("🟢 Écosystème adapté avec succès à : " + speciesAdaptCombo.getValue());
        }
    }

    private void handleSavePreset() {
        String defaultName = (presetsCombo.getEditor() != null && !presetsCombo.getEditor().getText().isBlank())
                ? presetsCombo.getEditor().getText().trim()
                : (presetsCombo.getValue() != null ? presetsCombo.getValue() : "Nouveau Preset Monde");
        TextInputDialog dialog = new TextInputDialog(defaultName);
        dialog.setTitle("Enregistrer le Preset Monde");
        dialog.setHeaderText("Saisissez un nom pour ce preset de monde :");
        dialog.setContentText("Nom :");
        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                String cleanName = name.trim();
                presetManager.save(cleanName, getConfiguration());
                presetsCombo.getItems().setAll(presetManager.names());
                presetsCombo.getSelectionModel().select(cleanName);
                new Alert(Alert.AlertType.INFORMATION, "Preset monde enregistré : " + cleanName).show();
            }
        });
    }

    private void handleDeletePreset() {
        String selected = presetsCombo.getValue();
        if (selected == null || selected.isEmpty()) return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Supprimer le Preset");
        confirmAlert.setHeaderText("Supprimer le Preset Monde");
        confirmAlert.setContentText("Voulez-vous vraiment supprimer le preset '" + selected + "' ?");

        confirmAlert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                presetManager.delete(selected);
                presetsCombo.getItems().setAll(presetManager.names());
                if (!presetsCombo.getItems().isEmpty()) {
                    presetsCombo.getSelectionModel().selectFirst();
                } else {
                    presetsCombo.getSelectionModel().clearSelection();
                }
                new Alert(Alert.AlertType.INFORMATION, "Preset monde supprimé.").show();
            }
        });
    }

    private VBox makeCard(String titleIcon, VBox content) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #18181b; -fx-border-color: #27272a; -fx-border-radius: 8px; -fx-background-radius: 8px;");
        Label lblHeader = new Label(titleIcon);
        lblHeader.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");
        card.getChildren().addAll(lblHeader, new Separator(), content);
        return card;
    }

    private ScrollPane buildConfig() {
        VBox cfg = new VBox(12);
        cfg.setPadding(new Insets(10));
        cfg.setPrefWidth(420);
        cfg.setMinWidth(400);
        cfg.setStyle("-fx-background-color: #121214;");

        VBox cardSource = makeCard("🌐 Données Géographiques & Localisation", buildTerrainSourceBlock());
        VBox cardScale  = makeCard("📐 Échelle & Dimensions Voxel", buildScaleBlock());
        VBox cardRelief = makeCard("⛰️ Relief, Topographie & Pente", buildReliefBlock());
        VBox cardSoil   = makeCard("🗻 Sol, Substrats & Stratification", buildSoilBlock());
        VBox cardHydro  = makeCard("💧 Hydrographie & Cours d'Eau", buildHydroBlock());
        VBox cardFlora  = makeCard("🌿 Végétation & Couvert Végétal", buildFloraBlock());
        VBox cardStruct = makeCard("🪵 Structures Hôtes & Arbres", buildStructBlock());
        VBox cardDiag   = makeCard("🧪 Diagnostic d'Attraction Écologique", buildDiagBlock());
        VBox cardAdapt  = buildAdaptBlock();

        cfg.getChildren().addAll(cardSource, cardScale, cardRelief, cardSoil, cardHydro, cardFlora, cardStruct, cardDiag, cardAdapt);

        ScrollPane sc = new ScrollPane(cfg);
        sc.setFitToWidth(true);
        sc.setPrefWidth(435);
        sc.setMinWidth(415);
        return sc;
    }

    private VBox buildTerrainSourceBlock() {
        cityNameField = new TextField("Fontainebleau, FR");
        cityNameField.setPrefWidth(130);
        cityNameField.setOnAction(e -> regenerateAndRepaint());

        citySearchField = new TextField("Fontainebleau");
        citySearchField.setPromptText("Ex: Paris, Tokyo, Manaus...");
        citySearchField.setPrefWidth(110);
        citySearchField.setOnAction(e -> fetchCityCoordinates(citySearchField.getText()));

        Button btnSearchCity = new Button("🔍 Ville");
        btnSearchCity.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
        btnSearchCity.setOnAction(e -> fetchCityCoordinates(citySearchField.getText()));

        latField = new TextField("48.4047");
        latField.setPrefWidth(70);
        lonField = new TextField("2.7016");
        lonField.setPrefWidth(70);

        Button btnImportGPS = new Button("📍 Appliquer SIG");
        btnImportGPS.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");

        geoStatusLabel = new Label("ℹ️ Recherche par Nom de Ville et/ou Coordonnées GPS réelles.");
        geoStatusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8; -fx-wrap-text: true;");

        btnImportGPS.setOnAction(e -> {
            regenerateAndRepaint();
            geoStatusLabel.setText("🟢 SIG appliqué pour Lat: " + latField.getText() + "°, Lon: " + lonField.getText() + "° !");
            geoStatusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #4ade80; -fx-wrap-text: true;");
        });

        HBox presetNameRow = new HBox(5, new Label("Ville/Nom:") {{ setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;"); }}, cityNameField);
        presetNameRow.setAlignment(Pos.CENTER_LEFT);

        HBox cityRow = new HBox(5, new Label("Chercher:") {{ setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;"); }}, citySearchField, btnSearchCity);
        cityRow.setAlignment(Pos.CENTER_LEFT);

        HBox gpsRow = new HBox(5,
            new Label("Lat:") {{ setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;"); }}, latField,
            new Label("Lon:") {{ setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;"); }}, lonField,
            btnImportGPS
        );
        gpsRow.setAlignment(Pos.CENTER_LEFT);

        return new VBox(8, presetNameRow, cityRow, gpsRow, geoStatusLabel);
    }

    private void fetchCityCoordinates(String cityQuery) {
        if (cityQuery == null || cityQuery.isBlank()) return;
        geoStatusLabel.setText("⏳ Recherche des coordonnées pour \"" + cityQuery + "\"...");
        geoStatusLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 10px;");

        new Thread(() -> {
            try {
                String geoUrlStr = "https://geocoding-api.open-meteo.com/v1/search?name="
                        + java.net.URLEncoder.encode(cityQuery, java.nio.charset.StandardCharsets.UTF_8)
                        + "&count=1&language=fr";

                java.net.URL url = new java.net.URL(geoUrlStr);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() == 200) {
                    try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) sb.append(line);

                        com.fasterxml.jackson.databind.JsonNode root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(sb.toString());
                        if (root.has("results") && !root.get("results").isEmpty()) {
                            com.fasterxml.jackson.databind.JsonNode loc = root.get("results").get(0);
                            String name = loc.get("name").asText();
                            double lat = loc.get("latitude").asDouble();
                            double lon = loc.get("longitude").asDouble();

                            javafx.application.Platform.runLater(() -> {
                                latField.setText(String.format(java.util.Locale.US, "%.4f", lat));
                                lonField.setText(String.format(java.util.Locale.US, "%.4f", lon));
                                if (cityNameField != null) cityNameField.setText(name);
                                geoStatusLabel.setText("🟢 " + name + " (Lat: " + String.format(java.util.Locale.US, "%.4f", lat) + "°, Lon: " + String.format(java.util.Locale.US, "%.4f", lon) + "°)");
                                geoStatusLabel.setStyle("-fx-text-fill: #4ade80; -fx-font-size: 10px;");
                                applyBioclimaticAdaptation(lat, lon);
                                regenerateAndRepaint();
                            });
                            return;
                        }
                    }
                }
                javafx.application.Platform.runLater(() -> {
                    geoStatusLabel.setText("⚠️ Ville \"" + cityQuery + "\" introuvable.");
                    geoStatusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 10px;");
                });
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    geoStatusLabel.setText("⚠️ Erreur de géocodage : " + ex.getMessage());
                    geoStatusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 10px;");
                });
            }
        }).start();
    }

    private void applyBioclimaticAdaptation(double lat, double lon) {
        org.swarmforge.core.domain.BioclimaticZone zone = org.swarmforge.core.domain.BioclimaticZone.classify(
            lat, 20.0 * (1.0 - Math.abs(lat) / 90.0), Math.abs(lat) < 23.5 ? 2000.0 : (Math.abs(lat) > 60 ? 300.0 : 700.0)
        );

        if (lblBioclimaticZoneBadge != null) {
            lblBioclimaticZoneBadge.setText(zone.getDisplayName());
        }

        switch (zone) {
            case ARID_DESERT -> {
                if (comboTreeSpecies != null) comboTreeSpecies.getSelectionModel().select(3);
                if (sandSpinner != null) sandSpinner.getValueFactory().setValue(80);
                if (stoneSpinner != null) stoneSpinner.getValueFactory().setValue(15);
                if (earthSpinner != null) earthSpinner.getValueFactory().setValue(5);
                if (baseHumiditySlider != null) baseHumiditySlider.setValue(0.12);
            }
            case TROPICAL_RAINFOREST -> {
                if (comboTreeSpecies != null) comboTreeSpecies.getSelectionModel().select(2);
                if (earthSpinner != null) earthSpinner.getValueFactory().setValue(45);
                if (claySpinner != null) claySpinner.getValueFactory().setValue(40);
                if (baseHumiditySlider != null) baseHumiditySlider.setValue(0.85);
            }
            case ARCTIC_TUNDRA -> {
                if (comboTreeSpecies != null) comboTreeSpecies.getSelectionModel().select(4);
                if (stoneSpinner != null) stoneSpinner.getValueFactory().setValue(40);
                if (earthSpinner != null) earthSpinner.getValueFactory().setValue(35);
                if (baseHumiditySlider != null) baseHumiditySlider.setValue(0.30);
            }
            case MEDITERRANEAN -> {
                if (comboTreeSpecies != null) comboTreeSpecies.getSelectionModel().select(1);
                if (earthSpinner != null) earthSpinner.getValueFactory().setValue(35);
                if (stoneSpinner != null) stoneSpinner.getValueFactory().setValue(25);
                if (baseHumiditySlider != null) baseHumiditySlider.setValue(0.35);
            }
            default -> {
                if (comboTreeSpecies != null) comboTreeSpecies.getSelectionModel().select(0);
                if (earthSpinner != null) earthSpinner.getValueFactory().setValue(50);
                if (sandSpinner != null) sandSpinner.getValueFactory().setValue(20);
                if (claySpinner != null) claySpinner.getValueFactory().setValue(20);
                if (baseHumiditySlider != null) baseHumiditySlider.setValue(0.45);
            }
        }
        updateEcologicalCompatibilityScores();
    }

    private VBox buildScaleBlock() {
        surfaceSizeSlider = mkSlider(0.5, 100.0, 25.0);
        depthSlider = mkSlider(0.2, 10.0, 3.0);
        resolutionSlider = mkSlider(0.1, 1.0, 0.5);

        surfaceSizeSlider.valueProperty().addListener((o, a, b) -> {
            updateVoxelMemoryEstimate();
            regenerateAndRepaint();
        });
        depthSlider.valueProperty().addListener((o, a, b) -> {
            updateVoxelMemoryEstimate();
            regenerateAndRepaint();
        });
        resolutionSlider.valueProperty().addListener((o, a, b) -> {
            updateVoxelMemoryEstimate();
            regenerateAndRepaint();
        });

        lblVoxelMemoryEstimate = new Label("📊 Estimation Voxel : ~0.5M voxels (32MB)");
        lblVoxelMemoryEstimate.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        updateVoxelMemoryEstimate();

        return new VBox(8,
                new Label(I18nManager.getInstance().get("world.dim.side_length")), sv(surfaceSizeSlider, "m"),
                new Label(I18nManager.getInstance().get("world.dim.depth")), sv(depthSlider, "m"),
                new Label(I18nManager.getInstance().get("world.dim.resolution")), sv(resolutionSlider, "mm"),
                lblVoxelMemoryEstimate
        );
    }

    private void updateVoxelMemoryEstimate() {
        if (lblVoxelMemoryEstimate == null) return;
        double sideM = surfaceSizeSlider != null ? surfaceSizeSlider.getValue() : 2.0;
        double depthM = depthSlider != null ? depthSlider.getValue() : 1.5;
        double resMm = resolutionSlider != null ? resolutionSlider.getValue() : 0.5;

        long rawVoxels = (long) ((sideM * 1000.0 / resMm) * (sideM * 1000.0 / resMm) * (depthM * 1000.0 / resMm));

        if (sideM > 5.0) {
            long lodVoxels = (long) (GRID_SIZE * GRID_SIZE * SOIL_DEPTH * (1.0 + Math.log(sideM)));
            double lodMb = lodVoxels * 4.0 / (1024.0 * 1024.0);
            lblVoxelMemoryEstimate.setText(String.format(java.util.Locale.US, "⚡ SVO Multi-LOD Active : ~%.1fM voxels efficaces (%.1f MB RAM)", lodVoxels / 1_000_000.0, lodMb));
            lblVoxelMemoryEstimate.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #4ade80;");
        } else {
            double rawMb = (GRID_SIZE * GRID_SIZE * SOIL_DEPTH * 4.0) / (1024.0 * 1024.0);
            lblVoxelMemoryEstimate.setText(String.format(java.util.Locale.US, "📊 Voxel Macro 128x128x32 : %.1f MB RAM", rawMb));
            lblVoxelMemoryEstimate.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");
        }
    }

    private VBox buildReliefBlock() {
        reliefSeedField = new TextField("774829");
        reliefSeedField.setPrefWidth(90);
        reliefSeedField.setOnAction(e -> regenerateAndRepaint());

        Button btnRandomRelief = new Button("🎲 Seed");
        btnRandomRelief.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
        btnRandomRelief.setOnAction(e -> {
            reliefSeedField.setText(String.valueOf(new Random().nextLong(100000, 9999999)));
            regenerateAndRepaint();
        });

        HBox seedBox = new HBox(6, new Label("Graine Relief:") {{ setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;"); }}, reliefSeedField, btnRandomRelief);
        seedBox.setAlignment(Pos.CENTER_LEFT);

        roughnessSlider = mkSlider(0.0, 1.0, 0.45);
        compactionSlider = mkSlider(10.0, 100.0, 65.0);

        roughnessSlider.valueProperty().addListener((o, a, b) -> regenerateAndRepaint());
        compactionSlider.valueProperty().addListener((o, a, b) -> regenerateAndRepaint());

        slopeAngleSlider = mkSlider(0.0, 200.0, 0.0);
        slopeAngleSlider.valueProperty().addListener((o, a, b) -> regenerateAndRepaint());

        slopeDirectionCombo = new ComboBox<>();
        slopeDirectionCombo.getItems().addAll("Sud ➔ Nord", "Nord ➔ Sud", "Ouest ➔ Est", "Est ➔ Ouest");
        slopeDirectionCombo.getSelectionModel().selectFirst();
        slopeDirectionCombo.valueProperty().addListener((o, a, b) -> regenerateAndRepaint());

        HBox slopeRow = new HBox(6, new Label("Direction Pente:") {{ setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;"); }}, slopeDirectionCombo);
        slopeRow.setAlignment(Pos.CENTER_LEFT);

        VBox sculptSubBlock = buildSculptBlock();

        return new VBox(8,
                seedBox,
                new Label(I18nManager.getInstance().get("world.relief.perlin")), sv(roughnessSlider, ""),
                new Label(I18nManager.getInstance().get("world.soil.compaction")), sv(compactionSlider, "%"),
                new Label("🏔️ Inclinaison Pente Montagne (0-200%) :"), sv(slopeAngleSlider, "%"),
                slopeRow,
                new Separator(),
                sculptSubBlock
        );
    }

    private VBox buildSoilBlock() {
        soilSeedField = new TextField("123456");
        soilSeedField.setPrefWidth(90);
        soilSeedField.setOnAction(e -> regenerateAndRepaint());

        Button btnRandomSoil = new Button("🎲 Seed");
        btnRandomSoil.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
        btnRandomSoil.setOnAction(e -> {
            soilSeedField.setText(String.valueOf(new Random().nextLong(100000, 9999999)));
            regenerateAndRepaint();
        });

        HBox seedBox = new HBox(6, new Label("Graine Sol:") {{ setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;"); }}, soilSeedField, btnRandomSoil);
        seedBox.setAlignment(Pos.CENTER_LEFT);

        earthSpinner = mkSpinner(0, 100, 50);
        sandSpinner = mkSpinner(0, 100, 20);
        claySpinner = mkSpinner(0, 100, 20);
        stoneSpinner = mkSpinner(0, 100, 10);
        organicSpinner = mkSpinner(0, 100, 0);

        for (Spinner<Integer> sp : new Spinner[]{earthSpinner, sandSpinner, claySpinner, stoneSpinner, organicSpinner})
            sp.valueProperty().addListener((o, a, b) -> regenerateAndRepaint());

        GridPane grid = new GridPane();
        grid.setHgap(8); grid.setVgap(6);
        grid.add(new Label(I18nManager.getInstance().get("world.sub.humus_pct")), 0, 0); grid.add(earthSpinner, 1, 0);
        grid.add(new Label(I18nManager.getInstance().get("world.sub.sand_pct")), 0, 1); grid.add(sandSpinner, 1, 1);
        grid.add(new Label(I18nManager.getInstance().get("world.sub.clay_pct")), 0, 2); grid.add(claySpinner, 1, 2);
        grid.add(new Label(I18nManager.getInstance().get("world.sub.rock_pct")), 0, 3); grid.add(stoneSpinner, 1, 3);
        grid.add(new Label(I18nManager.getInstance().get("world.sub.litter_pct")), 0, 4); grid.add(organicSpinner, 1, 4);

        stratificationSlider = mkSlider(0.0, 1.0, 0.7);
        mixingRateSlider     = mkSlider(0.0, 1.0, 0.3);
        baseHumiditySlider   = mkSlider(0.0, 1.0, 0.35);
        voidDensitySlider    = mkSlider(0.0, 0.3, 0.08);

        stratificationSlider.valueProperty().addListener((o, a, b) -> regenerateAndRepaint());
        mixingRateSlider.valueProperty().addListener((o, a, b) -> regenerateAndRepaint());
        baseHumiditySlider.valueProperty().addListener((o, a, b) -> regenerateAndRepaint());
        voidDensitySlider.valueProperty().addListener((o, a, b) -> regenerateAndRepaint());

        return new VBox(8,
                seedBox,
                grid,
                new Separator(),
                new Label("Degré de Stratification :"), sv(stratificationSlider, ""),
                new Label("Taux de Mélange des Couches :"), sv(mixingRateSlider, ""),
                new Label("Humidité de Base du Sol :"), sv(baseHumiditySlider, ""),
                new Label("Densité de Vides/Cavernes :"), sv(voidDensitySlider, "")
        );
    }

    private VBox buildFloraBlock() {
        floraSeedField = new TextField("774829");
        floraSeedField.setPrefWidth(95);
        floraSeedField.setOnAction(e -> regenerateAndRepaint());

        Button btnRandomizeFloraSeed = new Button("🎲 Seed");
        btnRandomizeFloraSeed.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
        btnRandomizeFloraSeed.setOnAction(e -> {
            long newSeed = new Random().nextLong(100000, 9999999);
            floraSeedField.setText(String.valueOf(newSeed));
            regenerateAndRepaint();
        });

        HBox seedBox = new HBox(6,
            new Label("Graine Flore:") {{ setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;"); }},
            floraSeedField, btnRandomizeFloraSeed
        );
        seedBox.setAlignment(Pos.CENTER_LEFT);

        edibleDensitySlider = mkSlider(0, 100, 40);
        nonEdibleDensitySlider = mkSlider(0, 100, 60);
        leafLitterSlider = mkSlider(0, 100, 50);
        twigDebrisSlider = mkSlider(0, 100, 40);
        addLsn(edibleDensitySlider, nonEdibleDensitySlider, leafLitterSlider, twigDebrisSlider);

        aphidPlantCheck    = new CheckBox("🟢 Cirsium / Vicia (Hôtes pucerons)"); aphidPlantCheck.setSelected(true);
        nectarFlowersCheck = new CheckBox("🌸 Fleurs à Nectar");                   nectarFlowersCheck.setSelected(true);
        seedGrassCheck     = new CheckBox("🌾 Graminées (Graines Messor)");         seedGrassCheck.setSelected(true);
        fungusFoliageCheck = new CheckBox("🍃 Feuillage Champignons (Atta)");      fungusFoliageCheck.setSelected(false);
        mossCheck          = new CheckBox("🟢 Mousse Polytrichum");                mossCheck.setSelected(true);
        pineLitterCheck    = new CheckBox("🍂 Litière Aiguilles de Pin");          pineLitterCheck.setSelected(true);
        fernObstacleCheck  = new CheckBox("🌿 Fougères (Obstacles)");              fernObstacleCheck.setSelected(true);

        addBoolLsn(aphidPlantCheck, nectarFlowersCheck, seedGrassCheck, fungusFoliageCheck,
                   mossCheck, pineLitterCheck, fernObstacleCheck);

        return new VBox(6,
                seedBox,
                new Separator(),
                new Label("🍎 Espèces Comestibles (Ressources) :"),
                new Label("Densité Comestibles :"), sv(edibleDensitySlider, "%"),
                aphidPlantCheck, nectarFlowersCheck, seedGrassCheck, fungusFoliageCheck,
                new Separator(),
                new Label("🍂 Couvert & Débris de Surface :"),
                new Label("Litière Organique / Feuilles :"), sv(leafLitterSlider, "%"),
                new Label("Brindilles & Micro-Débris :"), sv(twigDebrisSlider, "%"),
                new Label("Densité Non-Comestibles :"), sv(nonEdibleDensitySlider, "%"),
                mossCheck, pineLitterCheck, fernObstacleCheck
        );
    }

    private VBox buildHydroBlock() {
        hydroSeedField = new TextField("987654");
        hydroSeedField.setPrefWidth(90);
        hydroSeedField.setOnAction(e -> regenerateAndRepaint());

        Button btnRandomHydro = new Button("🎲 Seed");
        btnRandomHydro.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
        btnRandomHydro.setOnAction(e -> {
            hydroSeedField.setText(String.valueOf(new Random().nextLong(100000, 9999999)));
            regenerateAndRepaint();
        });

        HBox seedBox = new HBox(6, new Label("Graine Hydro:") {{ setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;"); }}, hydroSeedField, btnRandomHydro);
        seedBox.setAlignment(Pos.CENTER_LEFT);

        riverCheck = new CheckBox(I18nManager.getInstance().get("world.river.enable")); riverCheck.setSelected(true);
        riverWidthSlider = mkSlider(30, 500, 120);
        riverVelocitySlider = mkSlider(0.0, 1.5, 0.3);
        staticPoolsSlider = mkSlider(0, 5, 2);
        waterTableDepthSlider = mkSlider(5, 500, 50); // Max 500 cm = 5 mètres
        addLsn(riverWidthSlider, riverVelocitySlider, staticPoolsSlider, waterTableDepthSlider);
        riverCheck.setOnAction(e -> regenerateAndRepaint());

        Label hydroHint = new Label("💧 Rendu Planaire de l'Eau : Les cours d'eau et étangs sont générés avec un niveau d'eau horizontal fixe et continu remplissant les cuvettes de terrain.");
        hydroHint.setStyle("-fx-font-size: 10px; -fx-text-fill: #38bdf8; -fx-wrap-text: true;");

        return new VBox(8,
                seedBox,
                riverCheck,
                new Label(I18nManager.getInstance().get("world.river.width")), sv(riverWidthSlider, "mm"),
                new Label(I18nManager.getInstance().get("world.river.speed")), sv(riverVelocitySlider, "m/s"),
                new Separator(),
                new Label(I18nManager.getInstance().get("world.river.ponds")), sv(staticPoolsSlider, ""),
                new Label(I18nManager.getInstance().get("world.river.watertable")), sv(waterTableDepthSlider, "cm"),
                hydroHint
        );
    }

    private VBox buildStructBlock() {
        structSeedField = new TextField("555123");
        structSeedField.setPrefWidth(90);
        structSeedField.setOnAction(e -> regenerateAndRepaint());

        Button btnRandomStruct = new Button("🎲 Seed");
        btnRandomStruct.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
        btnRandomStruct.setOnAction(e -> {
            structSeedField.setText(String.valueOf(new Random().nextLong(100000, 9999999)));
            regenerateAndRepaint();
        });

        HBox seedBox = new HBox(6, new Label("Graine Struct:") {{ setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;"); }}, structSeedField, btnRandomStruct);
        seedBox.setAlignment(Pos.CENTER_LEFT);

        treeCountSlider = mkSlider(0, 150, 15);
        hollowLogsSlider = mkSlider(0, 8, 3);
        rockCrevicesSlider = mkSlider(0, 8, 3);
        addLsn(treeCountSlider, hollowLogsSlider, rockCrevicesSlider);

        comboTreeSpecies = new ComboBox<>();
        comboTreeSpecies.getItems().addAll(
            "🎋 Biome Bambouseraie (Temnothorax / Colobopsis)",
            "🪵 Biome Bois Mort & Souches en Décomposition",
            "🌿 Biome Bouleaux & Graminées (Messor / Prédation)",
            "🌵 Biome Désert Aride & Cactus Saguaro (Myrmecocystus / Cephalotes)",
            "🌳 Biome Futaie de Chênes & Feuillus (Atta / Camponotus)",
            "🌲 Biome Pinède Résineuse (Pucerons Cinara / Formica)",
            "🌵 Biome Savane d'Acacias (Pseudomyrmex / Nectaires)"
        );
        comboTreeSpecies.getSelectionModel().selectFirst();
        comboTreeSpecies.setPrefWidth(270);
        comboTreeSpecies.setOnAction(e -> updateTreeSpeciesSpinnersFromPreset());

        oakPctSpinner      = mkSpinner(0, 100, 45);
        pinePctSpinner     = mkSpinner(0, 100, 20);
        acaciaPctSpinner   = mkSpinner(0, 100, 10);
        birchPctSpinner    = mkSpinner(0, 100, 10);
        bambooPctSpinner   = mkSpinner(0, 100, 5);
        cactusPctSpinner   = mkSpinner(0, 100, 0);
        deadWoodPctSpinner = mkSpinner(0, 100, 10);

        for (Spinner<Integer> sp : new Spinner[]{oakPctSpinner, pinePctSpinner, acaciaPctSpinner, birchPctSpinner, bambooPctSpinner, cactusPctSpinner, deadWoodPctSpinner}) {
            sp.valueProperty().addListener((o, a, b) -> updateEcologicalCompatibilityScores());
        }

        GridPane botGrid = new GridPane();
        botGrid.setHgap(8); botGrid.setVgap(6);
        botGrid.add(new Label("🌳 Chêne (Quercus) % :"), 0, 0); botGrid.add(oakPctSpinner, 1, 0);
        botGrid.add(new Label("🌲 Pin Sylvestre (Pinus) % :"), 0, 1); botGrid.add(pinePctSpinner, 1, 1);
        botGrid.add(new Label("🌵 Acacia (Vachellia EFN) % :"), 0, 2); botGrid.add(acaciaPctSpinner, 1, 2);
        botGrid.add(new Label("🌵 Cactus Saguaro (Opuntia) % :"), 0, 3); botGrid.add(cactusPctSpinner, 1, 3);
        botGrid.add(new Label("🌿 Bouleau (Betula) % :"), 0, 4); botGrid.add(birchPctSpinner, 1, 4);
        botGrid.add(new Label("🎋 Bambou (Phyllostachys) % :"), 0, 5); botGrid.add(bambooPctSpinner, 1, 5);
        botGrid.add(new Label("🪵 Bois Mort / Souches % :"), 0, 6); botGrid.add(deadWoodPctSpinner, 1, 6);

        return new VBox(8,
                seedBox,
                new Label("Nombre d'Arbres / Troncs (Max 150) :"), sv(treeCountSlider, ""),
                new Label("Souches de Bois Creuses (Camponotus / Nids) :"), sv(hollowLogsSlider, ""),
                new Label("Fissures / Rentrées Rocheuses :"), sv(rockCrevicesSlider, ""),
                new Separator(),
                new Label("🌳 Espèce d'Arbre Dominante du Biome :"),
                comboTreeSpecies,
                new Label("📊 Matrice de Composition Botanique (% des Arbres) :"),
                botGrid
        );
    }

    private VBox buildDiagBlock() {
        lblBioclimaticZoneBadge.setStyle("-fx-font-weight: bold; -fx-text-fill: #38bdf8; -fx-font-size: 11px;");

        GridPane diagGrid = new GridPane();
        diagGrid.setHgap(10); diagGrid.setVgap(4);
        diagGrid.setStyle("-fx-background-color: #1e1b4b; -fx-padding: 8px; -fx-border-color: #4338ca; -fx-border-radius: 6px;");

        lblAttaCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #4ade80;");
        lblAphidCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #38bdf8;");
        lblWoodNestCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #f59e0b;");
        lblAcaciaAntCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #e879f9;");
        lblCactusAntCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #f43f5e;");
        lblPogonomyrmexCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #eab308;");
        lblTermiteCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #a3e635;");
        lblApisCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #fbbf24;");
        lblVespulaCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #f97316;");
        lblSolenopsisCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #ef4444;");

        diagGrid.add(new Label("🌐 Zone Bioclimatique :"), 0, 0); diagGrid.add(lblBioclimaticZoneBadge, 1, 0);
        diagGrid.add(new Label("🍃 Atta sexdens (Coupeuses) :"), 0, 1); diagGrid.add(lblAttaCompatScore, 1, 1);
        diagGrid.add(new Label("🍯 Lasius / Formica (Pucerons) :"), 0, 2); diagGrid.add(lblAphidCompatScore, 1, 2);
        diagGrid.add(new Label("🐜 Camponotus (Charpentières) :"), 0, 3); diagGrid.add(lblWoodNestCompatScore, 1, 3);
        diagGrid.add(new Label("🌵 Pseudomyrmex (Acacia) :"), 0, 4); diagGrid.add(lblAcaciaAntCompatScore, 1, 4);
        diagGrid.add(new Label("🏜️ Desert Ants (Cactus) :"), 0, 5); diagGrid.add(lblCactusAntCompatScore, 1, 5);
        diagGrid.add(new Label("🌾 Pogonomyrmex (Granivores) :"), 0, 6); diagGrid.add(lblPogonomyrmexCompatScore, 1, 6);
        diagGrid.add(new Label("🪵 Reticulitermes (Termites) :"), 0, 7); diagGrid.add(lblTermiteCompatScore, 1, 7);
        diagGrid.add(new Label("🐝 Apis mellifera (Abeilles) :"), 0, 8); diagGrid.add(lblApisCompatScore, 1, 8);
        diagGrid.add(new Label("🐝 Vespula vulgaris (Guêpes) :"), 0, 9); diagGrid.add(lblVespulaCompatScore, 1, 9);
        diagGrid.add(new Label("🔥 Solenopsis (Fourmis de Feu) :"), 0, 10); diagGrid.add(lblSolenopsisCompatScore, 1, 10);

        updateEcologicalCompatibilityScores();

        return new VBox(8,
                new Label("🧪 Diagnostic d'Attraction Écologique :"),
                diagGrid
        );
    }

    private VBox buildAdaptBlock() {
        VBox adaptBox = new VBox(6);
        adaptBox.setStyle("-fx-background-color: #1e1b4b; -fx-padding: 10px; -fx-background-radius: 8px; -fx-border-color: #4338ca; -fx-border-radius: 8px;");

        Label lblAdapt = new Label("✨ Adapter automatiquement le terrain & l'écosystème à l'espèce :");
        lblAdapt.setStyle("-fx-font-weight: bold; -fx-text-fill: #a78bfa; -fx-font-size: 11px;");

        speciesAdaptCombo = new ComboBox<>();
        speciesAdaptCombo.getItems().addAll(
            "🐝 Apis mellifera (Abeille mellifère — Prairie Fleurie & Nectar)",
            "🍃 Atta sexdens (Coupeuses de feuilles — Forêt Tropicale Humide)",
            "🐜 Camponotus ligniperda (Charpentières — Futaie de Chênes & Bois Mort)",
            "🌵 Cataglyphis bombycina (Fourmi argentée — Désert Aride du Sahara)",
            "🌲 Formica rufa (Fourmi rousse des bois — Taïga / Pinède)",
            "🍯 Lasius niger (Fourmi noire des jardins — Forêt Tempérée)",
            "🌾 Messor barbarus (Fourmis moissonneuses — Steppe Semi-Aride)",
            "🌵 Pseudomyrmex gracilis (Fourmi d'acacia — Savane Tropicale)",
            "🕳️ Reticulitermes lucifugus (Termite — Bois Décomposé & Cavernes)",
            "🔥 Solenopsis invicta (Fourmis de feu — Plaines Humides & Savane)"
        );
        speciesAdaptCombo.getSelectionModel().selectFirst();
        speciesAdaptCombo.setMaxWidth(Double.MAX_VALUE);

        Button btnAdapt = new Button("⚡ Adapter le Monde & Générer");
        btnAdapt.setMaxWidth(Double.MAX_VALUE);
        btnAdapt.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        btnAdapt.setGraphic(new FontIcon(Feather.CPU));
        btnAdapt.setTooltip(new Tooltip("Configure automatiquement le relief, sol, végétation, arbres, eau et climat adaptés à cette espèce d'insecte."));
        btnAdapt.setOnAction(e -> handleAdaptWorldToSpecies());

        lblAdaptStatus = new Label("🟢 Écosystème 3D prêt.");
        lblAdaptStatus.setStyle("-fx-font-size: 10px; -fx-text-fill: #4ade80;");

        adaptBox.getChildren().addAll(lblAdapt, speciesAdaptCombo, btnAdapt, lblAdaptStatus);
        return adaptBox;
    }

    private void updateTreeSpeciesSpinnersFromPreset() {
        int idx = comboTreeSpecies.getSelectionModel().getSelectedIndex();
        switch (idx) {
            case 0 -> { oakPctSpinner.getValueFactory().setValue(5); pinePctSpinner.getValueFactory().setValue(0); acaciaPctSpinner.getValueFactory().setValue(0); cactusPctSpinner.getValueFactory().setValue(0); birchPctSpinner.getValueFactory().setValue(5); bambooPctSpinner.getValueFactory().setValue(80); deadWoodPctSpinner.getValueFactory().setValue(10); } // Bambouseraie
            case 1 -> { oakPctSpinner.getValueFactory().setValue(10); pinePctSpinner.getValueFactory().setValue(10); acaciaPctSpinner.getValueFactory().setValue(0); cactusPctSpinner.getValueFactory().setValue(0); birchPctSpinner.getValueFactory().setValue(10); bambooPctSpinner.getValueFactory().setValue(0); deadWoodPctSpinner.getValueFactory().setValue(70); } // Bois Mort
            case 2 -> { oakPctSpinner.getValueFactory().setValue(15); pinePctSpinner.getValueFactory().setValue(15); acaciaPctSpinner.getValueFactory().setValue(0); cactusPctSpinner.getValueFactory().setValue(0); birchPctSpinner.getValueFactory().setValue(60); bambooPctSpinner.getValueFactory().setValue(0); deadWoodPctSpinner.getValueFactory().setValue(10); } // Bouleaux
            case 3 -> { oakPctSpinner.getValueFactory().setValue(0); pinePctSpinner.getValueFactory().setValue(0); acaciaPctSpinner.getValueFactory().setValue(15); cactusPctSpinner.getValueFactory().setValue(75); birchPctSpinner.getValueFactory().setValue(0); bambooPctSpinner.getValueFactory().setValue(0); deadWoodPctSpinner.getValueFactory().setValue(10); } // Désert & Cactus
            case 4 -> { oakPctSpinner.getValueFactory().setValue(70); pinePctSpinner.getValueFactory().setValue(10); acaciaPctSpinner.getValueFactory().setValue(0); cactusPctSpinner.getValueFactory().setValue(0); birchPctSpinner.getValueFactory().setValue(10); bambooPctSpinner.getValueFactory().setValue(0); deadWoodPctSpinner.getValueFactory().setValue(10); } // Chênes
            case 5 -> { oakPctSpinner.getValueFactory().setValue(10); pinePctSpinner.getValueFactory().setValue(75); acaciaPctSpinner.getValueFactory().setValue(0); cactusPctSpinner.getValueFactory().setValue(0); birchPctSpinner.getValueFactory().setValue(5); bambooPctSpinner.getValueFactory().setValue(0); deadWoodPctSpinner.getValueFactory().setValue(10); } // Pinède
            case 6 -> { oakPctSpinner.getValueFactory().setValue(0); pinePctSpinner.getValueFactory().setValue(5); acaciaPctSpinner.getValueFactory().setValue(80); cactusPctSpinner.getValueFactory().setValue(0); birchPctSpinner.getValueFactory().setValue(0); bambooPctSpinner.getValueFactory().setValue(0); deadWoodPctSpinner.getValueFactory().setValue(15); } // Savane d'Acacias
        }
        updateEcologicalCompatibilityScores();
    }

    private void updateEcologicalCompatibilityScores() {
        int oak = oakPctSpinner != null ? oakPctSpinner.getValue() : 45;
        int pine = pinePctSpinner != null ? pinePctSpinner.getValue() : 20;
        int acacia = acaciaPctSpinner != null ? acaciaPctSpinner.getValue() : 10;
        int birch = birchPctSpinner != null ? birchPctSpinner.getValue() : 10;
        int cactus = cactusPctSpinner != null ? cactusPctSpinner.getValue() : 0;
        int deadWood = deadWoodPctSpinner != null ? deadWoodPctSpinner.getValue() : 10;

        double lat = 48.8;
        if (latField != null) {
            try { lat = Double.parseDouble(latField.getText().trim()); } catch (Exception ignored) {}
        }
        double baseHum = baseHumiditySlider != null ? baseHumiditySlider.getValue() : 0.35;
        org.swarmforge.core.domain.BioclimaticZone zone = org.swarmforge.core.domain.BioclimaticZone.classify(lat, 20.0 * (1.0 - Math.abs(lat) / 90.0), baseHum * 2000.0);
        if (lblBioclimaticZoneBadge != null) {
            lblBioclimaticZoneBadge.setText(zone.getDisplayName());
        }

        boolean seedGrass = seedGrassCheck != null && seedGrassCheck.isSelected();
        boolean nectarFlowers = nectarFlowersCheck != null && nectarFlowersCheck.isSelected();
        boolean aphidPlant = aphidPlantCheck != null && aphidPlantCheck.isSelected();
        boolean fungus = fungusFoliageCheck != null && fungusFoliageCheck.isSelected();

        int earth = earthSpinner != null ? earthSpinner.getValue() : 50;
        int sand = sandSpinner != null ? sandSpinner.getValue() : 20;

        int attaScore = Math.min(100, (fungus ? 30 : 0) + oak * 1 + birch * 1 + acacia / 2);
        int aphidScore = Math.min(100, (aphidPlant ? 25 : 0) + pine * 1 + birch * 1 + oak / 2);
        int woodScore = Math.min(100, deadWood * 1 + oak * 1 + pine / 2);
        int acaciaScore = Math.min(100, (nectarFlowers ? 20 : 0) + acacia * 1 + deadWood / 2);
        int cactusScore = Math.min(100, cactus * 1 + acacia / 2 + (sand > 40 ? 20 : 0));
        int pogoScore = Math.min(100, (seedGrass ? 35 : 0) + (sand > 30 ? 25 : 10) + birch / 2 + 20);
        int termiteScore = Math.min(100, deadWood * 1 + (earth > 40 ? 25 : 10) + oak / 2 + 15);
        int apisScore = Math.min(100, (nectarFlowers ? 40 : 10) + oak / 2 + birch / 2 + acacia / 2 + 15);
        int vespulaScore = Math.min(100, deadWood / 2 + oak / 2 + pine / 2 + (nectarFlowers ? 20 : 0) + 30);
        int solenopsisScore = Math.min(100, (earth > 40 ? 30 : 10) + (baseHum > 0.4 ? 30 : 10) + 30);

        if (lblAttaCompatScore != null) lblAttaCompatScore.setText(attaScore + "% (Feuillage/Fongique)");
        if (lblAphidCompatScore != null) lblAphidCompatScore.setText(aphidScore + "% (Cinara/Miellat)");
        if (lblWoodNestCompatScore != null) lblWoodNestCompatScore.setText(woodScore + "% (Excavation Bois)");
        if (lblAcaciaAntCompatScore != null) lblAcaciaAntCompatScore.setText(acaciaScore + "% (Nectaires Acacia)");
        if (lblCactusAntCompatScore != null) lblCactusAntCompatScore.setText(cactusScore + "% (Sol Aride/Cactus)");
        if (lblPogonomyrmexCompatScore != null) lblPogonomyrmexCompatScore.setText(pogoScore + "% (Graminées/Graines)");
        if (lblTermiteCompatScore != null) lblTermiteCompatScore.setText(termiteScore + "% (Bois Mort/Cellulose)");
        if (lblApisCompatScore != null) lblApisCompatScore.setText(apisScore + "% (Nectar & Pollen)");
        if (lblVespulaCompatScore != null) lblVespulaCompatScore.setText(vespulaScore + "% (Chasse/Nid Papier)");
        if (lblSolenopsisCompatScore != null) lblSolenopsisCompatScore.setText(solenopsisScore + "% (Dômes Sol/Carnivore)");
    }

    private VBox buildSculptBlock() {
        enableSculptingCheck = new CheckBox("🖌️ Activer Mode Sculpture Directe (Glisser-souris)");
        enableSculptingCheck.setSelected(false);
        enableSculptingCheck.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");
        enableSculptingCheck.setOnAction(e -> repaintAllViews());

        brushModeSelect = new ComboBox<>();
        brushModeSelect.getItems().addAll("RAISE_ELEVATION", "LOWER_ELEVATION", "SMOOTH");
        ComboBoxTooltipHelper.setupDescriptiveComboBox(brushModeSelect,
            val -> switch (val) {
                case "RAISE_ELEVATION" -> "⛰️ RAISE_ELEVATION (Surélever Relief)";
                case "LOWER_ELEVATION" -> "⛏️ LOWER_ELEVATION (Creuser Relief)";
                case "SMOOTH" -> "🌊 SMOOTH (Lisser Relief)";
                default -> val;
            },
            val -> switch (val) {
                case "RAISE_ELEVATION" -> "Augmente progressivement l'altitude du relief aux coordonnées peintes.";
                case "LOWER_ELEVATION" -> "Creuse le terrain et abaisse l'altitude des voxels de surface.";
                case "SMOOTH" -> "Calcule la moyenne d'altitude locale pour lisser les pentes brusques.";
                default -> "";
            }
        );
        brushModeSelect.getSelectionModel().selectFirst();
        brushModeSelect.setPrefWidth(240);
        brushModeSelect.valueProperty().addListener((o, a, b) -> repaintAllViews());

        brushSubstrateSelect = new ComboBox<>();
        brushSubstrateSelect.getItems().addAll("EARTH", "SAND", "CLAY", "STONE");

        brushRadiusSlider = mkSlider(1, 15, 4);
        brushStrengthSlider = mkSlider(10, 100, 50);
        addLsn(brushRadiusSlider, brushStrengthSlider);

        return new VBox(8,
                enableSculptingCheck,
                new Label("Mode du Pinceau :"), brushModeSelect,
                new Label("Rayon du Pinceau (Voxels):"), sv(brushRadiusSlider, "vx"),
                new Label("Force du Pinceau:"), sv(brushStrengthSlider, "%")
        );
    }

    // ── Resizable Tri-View Area ────────────────────────────────────────────────

    private VBox buildViews() {
        canvas3D = new Canvas(540, 510); gc3D = canvas3D.getGraphicsContext2D();
        canvasSide = new Canvas(215, 245); gcSide = canvasSide.getGraphicsContext2D();
        canvasTop = new Canvas(215, 245); gcTop = canvasTop.getGraphicsContext2D();

        setupMouseControls();

        HBox topToolbar = buildTopViewportToolbar();
        VBox topToolbarContainer = new VBox(topToolbar);
        topToolbarContainer.setPadding(new Insets(6, 6, 0, 6));
        topToolbarContainer.setAlignment(Pos.TOP_CENTER);
        topToolbarContainer.setPickOnBounds(false);

        Pane h3d = new Pane(canvas3D, topToolbarContainer);
        h3d.setStyle("-fx-border-color: #555; -fx-border-width: 1; -fx-background-color: #0b0f19;");
        HBox.setHgrow(h3d, Priority.ALWAYS);
        VBox.setVgrow(h3d, Priority.ALWAYS);

        canvas3D.widthProperty().bind(h3d.widthProperty());
        canvas3D.heightProperty().bind(h3d.heightProperty());
        topToolbarContainer.prefWidthProperty().bind(h3d.widthProperty());

        canvas3D.widthProperty().addListener((obs, oldV, newV) -> repaintAllViews());
        canvas3D.heightProperty().addListener((obs, oldV, newV) -> repaintAllViews());

        StackPane hSide = new StackPane(canvasSide);
        hSide.setStyle("-fx-border-color: #444; -fx-border-width: 1; -fx-background-color: #0f172a;");
        StackPane hTop = new StackPane(canvasTop);
        hTop.setStyle("-fx-border-color: #444; -fx-border-width: 1; -fx-background-color: #0f172a;");

        Label ls = new Label(I18nManager.getInstance().get("minimap.sideview"));
        ls.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        Label lt = new Label("⬜ Vue du Dessus (Top-Down View)");
        lt.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        VBox rightRenderOptions = buildRightRenderOptionsPanel();

        VBox rightSideContent = new VBox(6, ls, hSide, lt, hTop, new Separator(), rightRenderOptions);
        rightSideContent.setPadding(new Insets(4));
        rightSideContent.setPrefWidth(240);

        ScrollPane rightScroll = new ScrollPane(rightSideContent);
        rightScroll.setFitToWidth(true);
        rightScroll.setPrefWidth(255);
        rightScroll.setMinWidth(245);
        rightScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        this.sideMinimapsBox = new VBox(rightScroll);
        sideMinimapsBox.setPadding(new Insets(0, 4, 0, 4));
        sideMinimapsBox.setAlignment(Pos.TOP_CENTER);

        HBox area = new HBox(6, h3d, sideMinimapsBox);
        area.setPadding(new Insets(8, 8, 4, 8));
        VBox.setVgrow(area, Priority.ALWAYS);

        HBox legendBar = buildLegendBar();

        VBox viewsBox = new VBox(4, area, legendBar);
        VBox.setVgrow(viewsBox, Priority.ALWAYS);
        return viewsBox;
    }

    private VBox buildRightRenderOptionsPanel() {
        VBox panel = new VBox(6);
        panel.setPadding(new Insets(8));
        panel.setStyle("-fx-background-color: #18181b; -fx-border-color: #3f3f46; -fx-border-radius: 6px; -fx-background-radius: 6px;");

        Label title = new Label("🖼️ Options Rendu 3D & Couches");
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #38bdf8; -fx-font-size: 11px;");

        useAdvancedVolumetricModeCheck = new CheckBox("📦 Rendu Volumétrique (Option B)");
        useAdvancedVolumetricModeCheck.setSelected(true);
        useAdvancedVolumetricModeCheck.setStyle("-fx-text-fill: #e4e4e7; -fx-font-size: 11px;");

        showChamferedBezelCheck = new CheckBox("🛡️ Cadre Biseauté Terrarium");
        showChamferedBezelCheck.setSelected(true);
        showChamferedBezelCheck.setStyle("-fx-text-fill: #e4e4e7; -fx-font-size: 11px;");

        showGravelInclusionsCheck = new CheckBox("⚪ Inclusions Graviers / Voxels");
        showGravelInclusionsCheck.setSelected(true);
        showGravelInclusionsCheck.setStyle("-fx-text-fill: #e4e4e7; -fx-font-size: 11px;");

        enableVolumetricScannerCheck = new CheckBox("🔬 Scanner Log & Coupe 3D");
        enableVolumetricScannerCheck.setSelected(false);
        enableVolumetricScannerCheck.setStyle("-fx-text-fill: #e4e4e7; -fx-font-size: 11px;");

        slicePlaneSlider = mkSlider(0.0, 100.0, 50.0);
        slicePlaneSlider.valueProperty().addListener((o, a, b) -> repaintAllViews());

        showTranslucentVolumetricModeCheck = new CheckBox("💧 Translucidité 3D Substrat");
        showTranslucentVolumetricModeCheck.setSelected(false);
        showTranslucentVolumetricModeCheck.setStyle("-fx-text-fill: #e4e4e7; -fx-font-size: 11px;");

        showEarthCheck = new CheckBox("🟤 Humus / Terre"); showEarthCheck.setSelected(true);
        showSandCheck = new CheckBox("🟡 Sable"); showSandCheck.setSelected(true);
        showClayCheck = new CheckBox("🔴 Argile"); showClayCheck.setSelected(true);
        showStoneCheck = new CheckBox("⚪ Roche"); showStoneCheck.setSelected(true);
        showOrganicCheck = new CheckBox("🍂 Litière Organique"); showOrganicCheck.setSelected(true);
        showVegetationCheck = new CheckBox("🌿 Végétation & Arbres"); showVegetationCheck.setSelected(true);
        showGalleriesCheck = new CheckBox("🕳️ Galeries Souterraines"); showGalleriesCheck.setSelected(true);
        showSubstrateStratigraphyCheck = new CheckBox("🗻 Stratigraphie 3D"); showSubstrateStratigraphyCheck.setSelected(true);
        showSubstrateStratigraphyCheck.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");

        addBoolLsn(useAdvancedVolumetricModeCheck, showChamferedBezelCheck, showGravelInclusionsCheck,
                   enableVolumetricScannerCheck, showTranslucentVolumetricModeCheck,
                   showEarthCheck, showSandCheck, showClayCheck, showStoneCheck, showOrganicCheck,
                   showVegetationCheck, showGalleriesCheck, showSubstrateStratigraphyCheck);

        VBox visBox = new VBox(4,
            new Label("👁️ Visibilité Couches & Végétation :") {{ setStyle("-fx-font-weight: bold; -fx-text-fill: #a78bfa; -fx-font-size: 11px;"); }},
            showEarthCheck, showSandCheck, showClayCheck, showStoneCheck, showOrganicCheck,
            new Separator(),
            showVegetationCheck, showGalleriesCheck, showSubstrateStratigraphyCheck
        );

        panel.getChildren().addAll(title, useAdvancedVolumetricModeCheck, showChamferedBezelCheck, showGravelInclusionsCheck,
                                   enableVolumetricScannerCheck, new Label("Coupe Scanner (%) :"), sv(slicePlaneSlider, "%"),
                                   showTranslucentVolumetricModeCheck, new Separator(), visBox);
        return panel;
    }

    private HBox buildLegendBar() {
        HBox bar = new HBox(8);
        bar.setPadding(new Insets(4, 10, 6, 10));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #141824; -fx-border-color: #333; -fx-border-width: 1 0 0 0;");

        syncViewsCheckBox = new CheckBox("🔗 Synchroniser les vues");
        syncViewsCheckBox.setSelected(true);
        syncViewsCheckBox.setStyle("-fx-text-fill: #00d4ff; -fx-font-weight: bold; -fx-font-size: 11px;");

        Label title = new Label("Substrats :");
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #aaa; -fx-font-size: 11px;");

        bar.getChildren().addAll(syncViewsCheckBox, new Separator(Orientation.VERTICAL), title);

        lblHoverInfo = new Label("ℹ️ Survolez un substrat ou une zone pour afficher sa fiche technique.");
        lblHoverInfo.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px; -fx-font-style: italic;");

        String[][] items = {
                {"Terre", "#3d2817", "🟤 Terre / Humus : Sol organique meuble."},
                {"Sable", "#eab308", "🟡 Sable : Substrat granuleux à faible cohésion."},
                {"Argile", "#9a3412", "🔴 Argile : Substrat minéral dense et plastique."},
                {"Pierre", "#64748b", "⚪ Pierre / Roche : Matériau inexcavable."},
                {"Rivière", "#0284c7", "💧 Rivière / Eau Planaire : Surface liquide horizontale en cuvette."},
                {"Végétation", "#15803d", "🌿 Couvert Végétal : Végétation et arbres proportionnels."},
                {"Galeries", "#d97706", "🕳️ Galeries Souterraines : Cavités excavées."}
        };

        for (String[] it : items) {
            HBox item = new HBox(4);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setPadding(new Insets(2, 4, 2, 4));
            item.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-background-radius: 4; -fx-cursor: hand;");

            Canvas dot = new Canvas(10, 10);
            GraphicsContext g = dot.getGraphicsContext2D();
            g.setFill(Color.web(it[1]));
            g.fillOval(0, 0, 10, 10);
            g.setStroke(Color.WHITE);
            g.setLineWidth(0.6);
            g.strokeOval(0, 0, 10, 10);

            Label lbl = new Label(it[0]);
            lbl.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 10px; -fx-font-weight: bold;");
            item.getChildren().addAll(dot, lbl);

            bar.getChildren().add(item);
        }

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        bar.getChildren().addAll(sp, lblHoverInfo);
        return bar;
    }

    private void setupMouseControls() {
        canvas3D.setOnMouseMoved(e -> updateHoverInfo(e.getX(), e.getY(), "3D"));
        canvas3D.setOnMousePressed(e -> { lastMX = e.getX(); lastMY = e.getY(); handleSculptClick(e.getX(), e.getY(), "3D"); });
        canvas3D.setOnMouseDragged(e -> {
            updateHoverInfo(e.getX(), e.getY(), "3D");
            if (enableSculptingCheck != null && enableSculptingCheck.isSelected()) {
                handleSculptClick(e.getX(), e.getY(), "3D");
            } else {
                double dx = e.getX() - lastMX;
                double dy = e.getY() - lastMY;
                if (e.isSecondaryButtonDown() || e.isShiftDown()) {
                    pan3DX += dx; pan3DY += dy;
                    if (isSync()) { sidePanX = pan3DX; sidePanY = pan3DY; topPanX = pan3DX; topPanY = pan3DY; }
                } else {
                    azimuth = (azimuth + dx * 0.65) % 360;
                    if (azimuth < 0) azimuth += 360;
                    elevation = Math.max(5, Math.min(85, elevation - dy * 0.35));
                }
                lastMX = e.getX(); lastMY = e.getY();
            }
            repaintAllViews();
        });
        canvas3D.setOnScroll(e -> {
            zoom = Math.max(2.5, Math.min(22.0, zoom + e.getDeltaY() * 0.025));
            if (isSync()) { sideZoom = Math.max(0.3, Math.min(6.0, zoom / 7.5)); topZoom = sideZoom; }
            repaintAllViews();
        });
        canvas3D.setOnMouseClicked(e -> { if (e.getClickCount() == 2) resetAllCameras(); });

        canvasSide.setOnMouseMoved(e -> updateHoverInfo(e.getX(), e.getY(), "SIDE"));
        canvasSide.setOnMousePressed(e -> { lastSideMX = e.getX(); lastSideMY = e.getY(); handleSculptClick(e.getX(), e.getY(), "SIDE"); });
        canvasSide.setOnMouseDragged(e -> {
            updateHoverInfo(e.getX(), e.getY(), "SIDE");
            if (enableSculptingCheck != null && enableSculptingCheck.isSelected()) {
                handleSculptClick(e.getX(), e.getY(), "SIDE");
            } else {
                double dx = e.getX() - lastSideMX;
                double dy = e.getY() - lastSideMY;
                sidePanX += dx; sidePanY += dy;
                if (isSync()) { topPanX = sidePanX; topPanY = sidePanY; pan3DX = sidePanX; pan3DY = sidePanY; }
                lastSideMX = e.getX(); lastSideMY = e.getY();
            }
            repaintAllViews();
        });
        canvasSide.setOnScroll(e -> {
            sideZoom = Math.max(0.3, Math.min(6.0, sideZoom + e.getDeltaY() * 0.003));
            if (isSync()) { topZoom = sideZoom; zoom = Math.max(2.5, Math.min(22.0, sideZoom * 7.5)); }
            repaintAllViews();
        });
        canvasSide.setOnMouseClicked(e -> { if (e.getClickCount() == 2) resetAllCameras(); });

        canvasTop.setOnMouseMoved(e -> updateHoverInfo(e.getX(), e.getY(), "TOP"));
        canvasTop.setOnMousePressed(e -> { lastTopMX = e.getX(); lastTopMY = e.getY(); handleSculptClick(e.getX(), e.getY(), "TOP"); });
        canvasTop.setOnMouseDragged(e -> {
            updateHoverInfo(e.getX(), e.getY(), "TOP");
            if (enableSculptingCheck != null && enableSculptingCheck.isSelected()) {
                handleSculptClick(e.getX(), e.getY(), "TOP");
            } else {
                double dx = e.getX() - lastTopMX;
                double dy = e.getY() - lastTopMY;
                topPanX += dx; topPanY += dy;
                if (isSync()) { sidePanX = topPanX; sidePanY = topPanY; pan3DX = topPanX; pan3DY = topPanY; }
                lastTopMX = e.getX(); lastTopMY = e.getY();
            }
            repaintAllViews();
        });
        canvasTop.setOnScroll(e -> {
            topZoom = Math.max(0.3, Math.min(6.0, topZoom + e.getDeltaY() * 0.003));
            if (isSync()) { sideZoom = topZoom; zoom = Math.max(2.5, Math.min(22.0, topZoom * 7.5)); }
            repaintAllViews();
        });
        canvasTop.setOnMouseClicked(e -> { if (e.getClickCount() == 2) resetAllCameras(); });
    }

    private int[] hover3DCell = null;
    private double hoverMX = 0, hoverMY = 0;

    private int[] pick3DGridCell(double mx, double my, double cx, double cy, double scale, double radAz, double radEl) {
        int bestX = -1, bestY = -1;
        double minDstSq = Double.MAX_VALUE;
        int step = 2;
        for (int x = 0; x < GRID_SIZE; x += step) {
            for (int y = 0; y < GRID_SIZE; y += step) {
                double z = heightGrid[x][y] * 40.0;
                double[] p = project3DPoint(x, y, z, cx, cy, scale, radAz, radEl);
                double dSq = (p[0] - mx) * (p[0] - mx) + (p[1] - my) * (p[1] - my);
                if (dSq < minDstSq) {
                    minDstSq = dSq;
                    bestX = x;
                    bestY = y;
                }
            }
        }
        if (minDstSq < 4900.0) { // within 70px radius
            return new int[]{bestX, bestY};
        }
        return null;
    }

    private void updateHoverInfo(double mx, double my, String viewType) {
        if (lblHoverInfo == null) return;
        double cw = "3D".equals(viewType) ? canvas3D.getWidth() : ("SIDE".equals(viewType) ? canvasSide.getWidth() : canvasTop.getWidth());
        double ch = "3D".equals(viewType) ? canvas3D.getHeight() : ("SIDE".equals(viewType) ? canvasSide.getHeight() : canvasTop.getHeight());
        if (cw <= 0 || ch <= 0) return;

        int gx, gy;

        if ("3D".equals(viewType)) {
            double radAz = Math.toRadians(azimuth);
            double radEl = Math.toRadians(elevation);
            double cx = cw / 2 + pan3DX;
            double cy = ch / 2 + pan3DY + 40;
            double scale = zoom * 12.0;

            int[] picked = pick3DGridCell(mx, my, cx, cy, scale, radAz, radEl);
            if (picked != null) {
                hover3DCell = picked;
                hoverMX = mx; hoverMY = my;
                gx = picked[0]; gy = picked[1];
            } else {
                hover3DCell = null;
                gx = (int) Math.max(0, Math.min(GRID_SIZE - 1, (mx / cw) * GRID_SIZE));
                gy = (int) Math.max(0, Math.min(GRID_SIZE - 1, (my / ch) * GRID_SIZE));
            }
            repaintAllViews();
        } else {
            gx = (int) Math.max(0, Math.min(GRID_SIZE - 1, (mx / cw) * GRID_SIZE));
            gy = (int) Math.max(0, Math.min(GRID_SIZE - 1, (my / ch) * GRID_SIZE));
        }

        double sM = surfaceSizeSlider != null ? surfaceSizeSlider.getValue() : 25.0;
        double dM = depthSlider != null ? depthSlider.getValue() : 3.0;
        double altM = heightGrid[gx][gy] * dM;
        int humPct = (int) (humidityGrid[gx][gy] * 100);
        byte mat = soilLayers[gx][gy][0];
        String matName = switch (mat) {
            case 0 -> "Humus / Terre";
            case 1 -> "Sable";
            case 2 -> "Argile";
            case 3 -> "Pierre / Roche";
            case 4 -> "Litière Organique";
            default -> "Terre";
        };
        boolean isRiver = isNearRiver(gx, gy, 1);

        lblHoverInfo.setText(String.format(Locale.US,
            "📍 Voxel [%d, %d] | Alt: %.1fm | Substrat: %s | Humidité: %d%% | %s",
            gx, gy, altM, matName, humPct, isRiver ? "💧 Rivière" : "🌱 Terrestre"));
    }

    private void handleSculptClick(double mx, double my, String viewType) {
        if (enableSculptingCheck == null || !enableSculptingCheck.isSelected()) return;
        double cw = "3D".equals(viewType) ? canvas3D.getWidth() : ("SIDE".equals(viewType) ? canvasSide.getWidth() : canvasTop.getWidth());
        double ch = "3D".equals(viewType) ? canvas3D.getHeight() : ("SIDE".equals(viewType) ? canvasSide.getHeight() : canvasTop.getHeight());
        if (cw <= 0 || ch <= 0) return;

        int gx = (int)(Math.max(0, Math.min(1.0, mx/cw)) * (GRID_SIZE-1));
        int gy = (int)(Math.max(0, Math.min(1.0, my/ch)) * (GRID_SIZE-1));
        int radius = (int) brushRadiusSlider.getValue();
        double strength = brushStrengthSlider.getValue() / 100.0 * 0.08;
        String mode = brushModeSelect.getValue();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                if (dx*dx + dy*dy <= radius*radius) {
                    int cx = Math.max(0, Math.min(GRID_SIZE-1, gx+dx));
                    int cy2 = Math.max(0, Math.min(GRID_SIZE-1, gy+dy));
                    double falloff = 1.0 - Math.sqrt(dx*dx+dy*dy)/(double)(radius+1);
                    if (mode != null && mode.contains("RAISE")) {
                        heightGrid[cx][cy2] = Math.min(1.0, heightGrid[cx][cy2] + strength*falloff);
                    } else if (mode != null && mode.contains("LOWER")) {
                        heightGrid[cx][cy2] = Math.max(0.01, heightGrid[cx][cy2] - strength*falloff);
                    } else if (mode != null && mode.contains("SMOOTH")) {
                        heightGrid[cx][cy2] = lrp(heightGrid[cx][cy2], 0.5, strength*2*falloff);
                    }
                }
            }
        }
        smoothSlopeStabilization();
        repaintAllViews();
    }

    private void smoothSlopeStabilization() {
        double maxDelta = 0.18;
        for (int x = 1; x < GRID_SIZE-1; x++)
            for (int y = 1; y < GRID_SIZE-1; y++) {
                double minN = Math.min(Math.min(heightGrid[x-1][y], heightGrid[x+1][y]),
                                       Math.min(heightGrid[x][y-1], heightGrid[x][y+1]));
                if (heightGrid[x][y] - minN > maxDelta) heightGrid[x][y] = minN + maxDelta;
            }
    }

    private boolean isSync() {
        return syncViewsCheckBox != null && syncViewsCheckBox.isSelected();
    }

    private void resetAllCameras() {
        azimuth = 45; elevation = 35; zoom = 7.5;
        pan3DX = 0; pan3DY = 0;
        sideZoom = 1.0; sidePanX = 0; sidePanY = 0;
        topZoom = 1.0; topPanX = 0; topPanY = 0;
        repaintAllViews();
    }

    private void addBoolLsn(CheckBox... boxes) {
        for (CheckBox cb : boxes) {
            if (cb != null) cb.selectedProperty().addListener((o, a, b) -> repaintAllViews());
        }
    }

    // ── Drawing Methods for 3D, Top-Down, and Side Views ───────────────────────

    private void repaintAllViews() {
        draw3D();
        drawSide();
        drawTop();
    }

    private HBox buildTopViewportToolbar() {
        HBox bar = new HBox(6);
        bar.setPadding(new Insets(6, 12, 6, 12));
        bar.setAlignment(Pos.CENTER);
        bar.setStyle("-fx-background-color: rgba(15, 23, 42, 0.88); -fx-background-radius: 8; -fx-border-color: rgba(255, 255, 255, 0.12); -fx-border-radius: 8;");

        lblViewportMode = new Label("🔲 Vue Technique 3D Grille — Resizable & Hydrologie Planaire");
        lblViewportMode.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px;");

        bar.getChildren().add(lblViewportMode);
        return bar;
    }

    private void draw3D() {
        if (canvas3D == null || canvas3D.getWidth() < 10 || canvas3D.getHeight() < 10) return;
        draw3DTechnical();
    }

    private double[] project3DPoint(double x, double y, double z, double cx, double cy, double scale, double radAz, double radEl) {
        double isoX = (x - GRID_SIZE / 2.0) * Math.cos(radAz) - (y - GRID_SIZE / 2.0) * Math.sin(radAz);
        double isoY = (x - GRID_SIZE / 2.0) * Math.sin(radAz) * Math.sin(radEl) + (y - GRID_SIZE / 2.0) * Math.cos(radAz) * Math.sin(radEl) - z * Math.cos(radEl);
        double px = cx + isoX * (scale / 10.0);
        double py = cy + isoY * (scale / 10.0);
        return new double[]{px, py};
    }

    private void draw3DTechnical() {
        if (canvas3D == null || canvas3D.getWidth() < 10 || canvas3D.getHeight() < 10) return;
        double w = canvas3D.getWidth();
        double h = canvas3D.getHeight();

        if (currentRenderMode == RenderMode.SCIENTIFIC) {
            gc3D.setFill(Color.web("#030712"));
        } else if (currentRenderMode == RenderMode.GAMIFIED) {
            gc3D.setFill(Color.web("#1e1b4b"));
        } else {
            gc3D.setFill(Color.web("#0b0f19"));
        }
        gc3D.fillRect(0, 0, w, h);

        double radAz = Math.toRadians(azimuth);
        double radEl = Math.toRadians(elevation);

        double cx = w / 2 + pan3DX;
        double cy = h / 2 + pan3DY + 40;
        double scale = zoom * 12.0;

        int step = currentRenderMode == RenderMode.GAMIFIED ? 4 : 2;
        double targetDepthVal = depthSlider != null ? depthSlider.getValue() : 1.5;
        double maxDepthPx = targetDepthVal * 22.0;

        // 1. Render Back Subterranean Stratigraphic Skirt Walls
        if ((showSubstrateStratigraphyCheck == null || showSubstrateStratigraphyCheck.isSelected()) && currentRenderMode != RenderMode.SCIENTIFIC) {
            drawStratigraphySideWalls3D(cx, cy, scale, radAz, radEl, maxDepthPx, step, true);
        }

        // 2. Render Solid 3D Continuous Quad Surface Mesh
        if (isTerrainVisible) {
            for (int x = 0; x < GRID_SIZE - step; x += step) {
                for (int y = 0; y < GRID_SIZE - step; y += step) {
                    double z0 = heightGrid[x][y] * 40.0;
                    double z1 = heightGrid[x + step][y] * 40.0;
                    double z2 = heightGrid[x + step][y + step] * 40.0;
                    double z3 = heightGrid[x][y + step] * 40.0;

                    if (carvedVoxelGrid[x][y]) z0 -= 15.0;
                    if (carvedVoxelGrid[x + step][y]) z1 -= 15.0;
                    if (carvedVoxelGrid[x + step][y + step]) z2 -= 15.0;
                    if (carvedVoxelGrid[x][y + step]) z3 -= 15.0;

                    double[] p0 = project3DPoint(x, y, z0, cx, cy, scale, radAz, radEl);
                    double[] p1 = project3DPoint(x + step, y, z1, cx, cy, scale, radAz, radEl);
                    double[] p2 = project3DPoint(x + step, y + step, z2, cx, cy, scale, radAz, radEl);
                    double[] p3 = project3DPoint(x, y + step, z3, cx, cy, scale, radAz, radEl);

                    double[] pxs = new double[]{p0[0], p1[0], p2[0], p3[0]};
                    double[] pys = new double[]{p0[1], p1[1], p2[1], p3[1]};

                    byte mat = soilLayers[x][y][0];
                    boolean visibleMat = isMaterialVisible(mat);

                    Color col;
                    if (currentRenderMode == RenderMode.SCIENTIFIC) {
                        col = Color.web("#0f172a", 0.7);
                    } else {
                        col = visibleMat ? getMaterialColor(mat) : Color.web("#1e293b", 0.35);
                        if (carvedVoxelGrid[x][y]) col = Color.web("#d97706");
                    }

                    gc3D.setFill(col);
                    gc3D.fillPolygon(pxs, pys, 4);
                    if (currentRenderMode == RenderMode.SCIENTIFIC) {
                        gc3D.setStroke(Color.web("#38bdf8", 0.35));
                        gc3D.setLineWidth(0.4);
                    } else if (currentRenderMode == RenderMode.GAMIFIED) {
                        gc3D.setStroke(Color.web("#0f172a", 0.8));
                        gc3D.setLineWidth(1.4);
                    } else {
                        gc3D.setStroke(col.darker());
                        gc3D.setLineWidth(visibleMat ? 0.3 : 0.1);
                    }
                    gc3D.strokePolygon(pxs, pys, 4);
                }
            }
        }

        // 2b. Render FLAT PLANAR WATER SURFACES (Horizontal Water Level for River & Static Pools)
        double planarWaterZ = 0.22 * 40.0; // Flat horizontal liquid level elevation
        if (riverCheck != null && riverCheck.isSelected() && riverPath != null && riverPath.size() > 1) {
            double rWidthPx = Math.max(4.0, (riverWidthSlider != null ? riverWidthSlider.getValue() : 120.0) / 20.0 * (zoom / 7.5));
            
            // Draw horizontal flat planar river surface
            gc3D.setStroke(Color.web("#0284c7", 0.90));
            gc3D.setLineWidth(rWidthPx);
            gc3D.beginPath();
            boolean firstPt = true;
            for (int[] rPt : riverPath) {
                int rx = rPt[0], ry = rPt[1];
                // Water level is flat horizontal (not riding terrain slope)
                double rz = Math.min(planarWaterZ, heightGrid[rx][ry] * 40.0 + 1.5);
                double[] rP = project3DPoint(rx, ry, rz, cx, cy, scale, radAz, radEl);
                if (firstPt) { gc3D.moveTo(rP[0], rP[1]); firstPt = false; }
                else gc3D.lineTo(rP[0], rP[1]);
            }
            gc3D.stroke();

            // Specular water sheen highlight
            gc3D.setStroke(Color.web("#7dd3fc", 0.75));
            gc3D.setLineWidth(Math.max(1.5, rWidthPx * 0.45));
            gc3D.beginPath();
            firstPt = true;
            for (int[] rPt : riverPath) {
                int rx = rPt[0], ry = rPt[1];
                double rz = Math.min(planarWaterZ + 0.3, heightGrid[rx][ry] * 40.0 + 1.8);
                double[] rP = project3DPoint(rx, ry, rz, cx, cy, scale, radAz, radEl);
                if (firstPt) { gc3D.moveTo(rP[0] - 1, rP[1] - 1); firstPt = false; }
                else gc3D.lineTo(rP[0] - 1, rP[1] - 1);
            }
            gc3D.stroke();
        }

        // 2c. Render Static Pools / Lakes as Organic Flat Horizontal Water Surfaces filling depressions
        if (staticPoolsSlider != null && staticPoolsSlider.getValue() > 0) {
            int poolCount = (int) staticPoolsSlider.getValue();
            Random pRand = new Random(1234);
            for (int i = 0; i < poolCount; i++) {
                int poolX = 20 + (i * 27 + pRand.nextInt(15)) % (GRID_SIZE - 40);
                int poolY = 20 + (i * 31 + pRand.nextInt(15)) % (GRID_SIZE - 40);
                double baseRadius = 3.5 + (i % 3) * 1.5;

                // Calculate constant flat water elevation for this lake basin
                double minTerrainH = heightGrid[poolX][poolY];
                for (int dx = -3; dx <= 3; dx++) {
                    for (int dy = -3; dy <= 3; dy++) {
                        int gx = Math.min(GRID_SIZE - 1, Math.max(0, poolX + dx));
                        int gy = Math.min(GRID_SIZE - 1, Math.max(0, poolY + dy));
                        minTerrainH = Math.min(minTerrainH, heightGrid[gx][gy]);
                    }
                }
                double flatWaterZ = (minTerrainH + 0.02) * 40.0;

                // Build smooth organic 16-point oval/blob water surface
                int numPts = 16;
                double[] xPts = new double[numPts];
                double[] yPts = new double[numPts];
                for (int p = 0; p < numPts; p++) {
                    double angle = (2.0 * Math.PI * p) / numPts;
                    double rPerturb = baseRadius * (0.85 + 0.30 * Math.sin(angle * 3.0 + i));
                    double vx = poolX + Math.cos(angle) * rPerturb;
                    double vy = poolY + Math.sin(angle) * rPerturb;
                    double[] proj = project3DPoint(vx, vy, flatWaterZ, cx, cy, scale, radAz, radEl);
                    xPts[p] = proj[0];
                    yPts[p] = proj[1];
                }

                gc3D.setFill(Color.web("#0284c7", 0.85));
                gc3D.fillPolygon(xPts, yPts, numPts);
                gc3D.setStroke(Color.web("#38bdf8", 0.7));
                gc3D.setLineWidth(1.2 * (zoom / 7.5));
                gc3D.strokePolygon(xPts, yPts, numPts);
            }
        }

        // 3. Render Front Subterranean Stratigraphic Skirt Walls
        if (showSubstrateStratigraphyCheck == null || showSubstrateStratigraphyCheck.isSelected()) {
            drawStratigraphySideWalls3D(cx, cy, scale, radAz, radEl, maxDepthPx, step, false);
        }

        // Draw Hollow Log Stumps in 3D
        if (hollowLogsSlider != null && (showVegetationCheck == null || showVegetationCheck.isSelected())) {
            int stumpCount = (int) hollowLogsSlider.getValue();
            Random sRand = new Random(99);
            for (int i = 0; i < stumpCount; i++) {
                int sx = 15 + (int)(sRand.nextDouble() * (GRID_SIZE - 30));
                int sy = 15 + (int)(sRand.nextDouble() * (GRID_SIZE - 30));
                double sz = heightGrid[sx][sy] * 40.0;
                double[] sp = project3DPoint(sx, sy, sz, cx, cy, scale, radAz, radEl);

                gc3D.setFill(Color.web("#78350f"));
                gc3D.fillRect(sp[0] - 5, sp[1] - 12, 10, 12);
                gc3D.setFill(Color.web("#451a03"));
                gc3D.fillOval(sp[0] - 5, sp[1] - 15, 10, 6);
                gc3D.setFill(Color.web("#15803d"));
                gc3D.fillOval(sp[0] - 7, sp[1] - 3, 14, 5);
            }
        }

        // Draw Surface Flora Items in 3D
        if (showVegetationCheck == null || showVegetationCheck.isSelected()) {
            for (SurfaceFloraItem item : surfaceFloraItems) {
                double z = heightGrid[item.gx][item.gy] * 40.0 + 1.2;
                double[] p = project3DPoint(item.gx, item.gy, z, cx, cy, scale, radAz, radEl);
                double sc = item.scale * (zoom / 7.5);

                switch (item.type) {
                    case 0:
                        gc3D.setStroke(Color.web("#4ade80"));
                        gc3D.setLineWidth(1.2);
                        gc3D.strokeLine(p[0], p[1], p[0] - 3 * sc, p[1] - 8 * sc);
                        gc3D.strokeLine(p[0], p[1], p[0] + 3 * sc, p[1] - 9 * sc);
                        break;
                    case 1:
                        gc3D.setStroke(Color.web("#166534"));
                        gc3D.setLineWidth(1.5);
                        gc3D.strokeLine(p[0], p[1], p[0], p[1] - 12 * sc);
                        gc3D.setFill(Color.web("#a3e635"));
                        gc3D.fillOval(p[0] - 2 * sc, p[1] - 10 * sc, 4 * sc, 4 * sc);
                        break;
                    case 2:
                        gc3D.setStroke(Color.web("#15803d"));
                        gc3D.setLineWidth(1.2);
                        gc3D.strokeLine(p[0], p[1], p[0], p[1] - 10 * sc);
                        gc3D.setFill(Color.web("#f43f5e"));
                        gc3D.fillOval(p[0] - 3 * sc, p[1] - 13 * sc, 6 * sc, 6 * sc);
                        break;
                    case 3:
                        gc3D.setFill(Color.web("#15803d", 0.75));
                        gc3D.fillOval(p[0] - 6 * sc, p[1] - 4 * sc, 12 * sc, 8 * sc);
                        break;
                    case 4:
                        gc3D.setFill(Color.web("#854d0e", 0.85));
                        gc3D.fillOval(p[0] - 6 * sc, p[1] - 3 * sc, 12 * sc, 6 * sc);
                        gc3D.setFill(Color.web("#a16207", 0.7));
                        gc3D.fillOval(p[0] - 3 * sc, p[1] - 4 * sc, 8 * sc, 5 * sc);
                        gc3D.setStroke(Color.web("#451a03"));
                        gc3D.setLineWidth(0.8 * sc);
                        gc3D.strokeLine(p[0] - 5 * sc, p[1], p[0] + 5 * sc, p[1] - 2 * sc);
                        break;
                    case 5:
                        gc3D.setStroke(Color.web("#451a03"));
                        gc3D.setLineWidth(1.4);
                        gc3D.strokeLine(p[0] - 5 * sc, p[1], p[0] + 5 * sc, p[1] - 3 * sc);
                        break;
                    case 6:
                        gc3D.setFill(Color.web("#94a3b8"));
                        gc3D.fillOval(p[0] - 2 * sc, p[1] - 2 * sc, 4 * sc, 4 * sc);
                        break;
                }
            }
        }

        // Draw Trees in 3D Viewport (Proportionally scaled to tree species & terrain side meters)
        if (treeCountSlider != null && (showVegetationCheck == null || showVegetationCheck.isSelected())) {
            int count = (int) treeCountSlider.getValue();

            // Species tree height proportions
            int treeIdx = comboTreeSpecies != null ? comboTreeSpecies.getSelectionModel().getSelectedIndex() : 4;
            double baseTreeHeightM = switch (treeIdx) {
                case 0 -> 6.0;   // Bambouseraie
                case 1 -> 2.5;   // Souche
                case 2 -> 12.0;  // Bouleau
                case 3 -> 4.0;   // Cactus Saguaro
                case 4 -> 15.0;  // Quercus (Chêne)
                case 5 -> 18.0;  // Pinus (Pin)
                case 6 -> 8.0;   // Acacia
                default -> 15.0;
            };

            double sideM = surfaceSizeSlider != null ? surfaceSizeSlider.getValue() : 25.0;
            double pixelsPerMeter = Math.max(1.5, (GRID_SIZE * scale * (zoom / 7.5)) / Math.max(1.0, sideM));

            double trunkH = baseTreeHeightM * pixelsPerMeter * 0.50;
            double trunkW = Math.max(3.0, (baseTreeHeightM * 0.07) * pixelsPerMeter);
            double canopyR = Math.max(6.0, (baseTreeHeightM * 0.30) * pixelsPerMeter);

            Random rand = new Random(77);
            class TreeInstance {
                double[] p;
                double depth;
            }
            List<TreeInstance> trees = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                int gx = 10 + (int)(rand.nextDouble() * (GRID_SIZE - 20));
                int gy = 10 + (int)(rand.nextDouble() * (GRID_SIZE - 20));
                if (isNearRiver(gx, gy, 3)) continue; // Never spawn trees in river!

                double z = heightGrid[gx][gy] * 40.0;
                double[] p = project3DPoint(gx, gy, z, cx, cy, scale, radAz, radEl);

                // Back-to-front depth calculation relative to 3D view camera
                double depth = gx * Math.sin(radAz) + gy * Math.cos(radAz) - (z / 40.0) * Math.sin(radEl);
                TreeInstance ti = new TreeInstance();
                ti.p = p;
                ti.depth = depth;
                trees.add(ti);
            }

            // Sort back-to-front (farther trees rendered first)
            trees.sort((t1, t2) -> Double.compare(t2.depth, t1.depth));

            for (TreeInstance ti : trees) {
                double[] p = ti.p;

                if (treeIdx == 3) { // Cactus Saguaro
                    gc3D.setFill(Color.web("#15803d"));
                    gc3D.fillRect(p[0] - trunkW / 2, p[1] - trunkH, trunkW, trunkH);
                    gc3D.fillRect(p[0] - trunkH * 0.4, p[1] - trunkH * 0.7, trunkH * 0.8, trunkW * 0.8);
                } else if (treeIdx == 1) { // Souche
                    gc3D.setFill(Color.web("#78350f"));
                    gc3D.fillRect(p[0] - trunkW, p[1] - trunkH, trunkW * 2, trunkH);
                } else {
                    // Trunk Base Anchored precisely at terrain surface p[1]
                    gc3D.setFill(Color.web("#78350f"));
                    gc3D.fillRect(p[0] - trunkW / 2, p[1] - trunkH, trunkW, trunkH);
                    gc3D.setFill(Color.web("#451a03"));
                    gc3D.fillRect(p[0] - trunkW / 2, p[1] - trunkH, trunkW * 0.4, trunkH);

                    // Canopy anchored atop trunk
                    gc3D.setFill(Color.web("#14532d"));
                    gc3D.fillOval(p[0] - canopyR, p[1] - trunkH - canopyR * 1.4, canopyR * 2, canopyR * 1.6);
                    gc3D.setFill(Color.web("#166534"));
                    gc3D.fillOval(p[0] - canopyR * 0.8, p[1] - trunkH - canopyR * 1.6, canopyR * 1.6, canopyR * 1.4);
                    gc3D.setFill(Color.web("#15803d"));
                    gc3D.fillOval(p[0] - canopyR * 0.6, p[1] - trunkH - canopyR * 1.8, canopyR * 1.2, canopyR * 1.1);
                }
            }
        }

        // Draw 3D Overlays for Insects, Nests & Underground Galleries, and Pheromones ONLY IN SIMULATION MODE
        if (isSimulationMode) {
            if (isColonyVisible) drawColonyOverlay3D(cx, cy, scale, radAz, radEl);
            if (isGalleriesVisible) drawGalleriesOverlay3D(cx, cy, scale, radAz, radEl);
            if (isPheromonesVisible) drawPheromoneOverlay3D(cx, cy, scale, radAz, radEl);
        }
        if (isWeatherVisible) drawWeatherOverlay3D(w, h);

        // Draw 3D Hover Info Overlay on top of 3D Canvas
        draw3DHoverOverlay(w, h, cx, cy, scale, radAz, radEl);

        // Draw Metric Scale Bar in bottom right of 3D Canvas
        drawMetricScaleBar3D(w, h, cx, cy, scale, radAz, radEl);

        if (lblViewportMode != null) {
            lblViewportMode.setText(String.format("🔲 Vue Technique 3D (Côté: %.1fm, Az: %d°, El: %d°, Eau Planaire Active)", targetDepthVal, (int) azimuth, (int) elevation));
        }
    }

    private void draw3DHoverOverlay(double w, double h, double cx, double cy, double scale, double radAz, double radEl) {
        if (hover3DCell == null) return;
        int gx = hover3DCell[0];
        int gy = hover3DCell[1];
        if (gx < 0 || gx >= GRID_SIZE || gy < 0 || gy >= GRID_SIZE) return;

        // Highlight quad on terrain surface mesh
        double z0 = heightGrid[gx][gy] * 40.0;
        int step = 2;
        int gx2 = Math.min(GRID_SIZE - 1, gx + step);
        int gy2 = Math.min(GRID_SIZE - 1, gy + step);
        double z1 = heightGrid[gx2][gy] * 40.0;
        double z2 = heightGrid[gx2][gy2] * 40.0;
        double z3 = heightGrid[gx][gy2] * 40.0;

        double[] p0 = project3DPoint(gx, gy, z0, cx, cy, scale, radAz, radEl);
        double[] p1 = project3DPoint(gx2, gy, z1, cx, cy, scale, radAz, radEl);
        double[] p2 = project3DPoint(gx2, gy2, z2, cx, cy, scale, radAz, radEl);
        double[] p3 = project3DPoint(gx, gy2, z3, cx, cy, scale, radAz, radEl);

        gc3D.setFill(Color.web("#f59e0b", 0.45));
        gc3D.fillPolygon(new double[]{p0[0], p1[0], p2[0], p3[0]}, new double[]{p0[1], p1[1], p2[1], p3[1]}, 4);
        gc3D.setStroke(Color.web("#fbbf24"));
        gc3D.setLineWidth(2.0);
        gc3D.strokePolygon(new double[]{p0[0], p1[0], p2[0], p3[0]}, new double[]{p0[1], p1[1], p2[1], p3[1]}, 4);

        // Draw HUD Box on 3D Viewport
        double boxW = 215.0;
        double boxH = 95.0;
        double hx = Math.max(10, Math.min(w - boxW - 10, hoverMX + 15));
        double hy = Math.max(10, Math.min(h - boxH - 10, hoverMY - 20));

        gc3D.setFill(Color.web("rgba(15, 23, 42, 0.92)"));
        gc3D.fillRoundRect(hx, hy, boxW, boxH, 8, 8);
        gc3D.setStroke(Color.web("#38bdf8", 0.85));
        gc3D.setLineWidth(1.2);
        gc3D.strokeRoundRect(hx, hy, boxW, boxH, 8, 8);

        double sM = surfaceSizeSlider != null ? surfaceSizeSlider.getValue() : 25.0;
        double dM = depthSlider != null ? depthSlider.getValue() : 3.0;
        double altM = heightGrid[gx][gy] * dM;
        int humPct = (int) (humidityGrid[gx][gy] * 100);
        byte mat = soilLayers[gx][gy][0];
        String matName = switch (mat) {
            case 0 -> "Humus / Terre";
            case 1 -> "Sable";
            case 2 -> "Argile";
            case 3 -> "Pierre / Roche";
            case 4 -> "Litière Organique";
            default -> "Terre";
        };
        boolean isRiver = isNearRiver(gx, gy, 1);

        gc3D.setFill(Color.web("#38bdf8"));
        gc3D.setFont(Font.font("System", javafx.scene.text.FontWeight.BOLD, 11));
        gc3D.fillText(String.format(Locale.US, "📍 Cellule Voxel [%d, %d]", gx, gy), hx + 10, hy + 18);

        gc3D.setFill(Color.web("#f8fafc"));
        gc3D.setFont(Font.font("System", 10));
        gc3D.fillText(String.format(Locale.US, "📏 Altitude : %.2f m (%.0f cm)", altM, altM * 100), hx + 10, hy + 36);
        gc3D.fillText(String.format(Locale.US, "🟤 Substrat : %s", matName), hx + 10, hy + 52);
        gc3D.fillText(String.format(Locale.US, "💧 Humidité : %d%% | %s", humPct, isRiver ? "💧 Rivière" : "🌱 Terrestre"), hx + 10, hy + 68);
        gc3D.fillText(String.format(Locale.US, "🔍 Résolution : %.1f mm / voxel", (sM / GRID_SIZE) * 1000), hx + 10, hy + 84);
    }

    private void drawMetricScaleBar3D(double w, double h, double cx, double cy, double scale, double radAz, double radEl) {
        double sideM = surfaceSizeSlider != null ? surfaceSizeSlider.getValue() : 2.5;
        double refM;
        String scaleLabel;
        if (sideM >= 50.0) { refM = 10.0; scaleLabel = "10 m"; }
        else if (sideM >= 10.0) { refM = 2.0; scaleLabel = "2 m"; }
        else if (sideM >= 2.0) { refM = 0.5; scaleLabel = "50 cm"; }
        else { refM = 0.1; scaleLabel = "10 cm"; }

        // Calculate screen length of projected terrain side edge in pixels
        double[] p0 = project3DPoint(0, 0, 0, cx, cy, scale, radAz, radEl);
        double[] p1 = project3DPoint(GRID_SIZE, 0, 0, cx, cy, scale, radAz, radEl);
        double projLen = Math.hypot(p1[0] - p0[0], p1[1] - p0[1]);

        double barWidthPx = Math.max(35.0, (refM / sideM) * projLen);

        double bx = w - barWidthPx - 20.0;
        double by = h - 25.0;

        gc3D.setStroke(Color.web("#38bdf8"));
        gc3D.setLineWidth(2.5);
        gc3D.strokeLine(bx, by, bx + barWidthPx, by);
        gc3D.strokeLine(bx, by - 4, bx, by + 4);
        gc3D.strokeLine(bx + barWidthPx, by - 4, bx + barWidthPx, by + 4);

        gc3D.setFill(Color.web("#38bdf8"));
        gc3D.setFont(Font.font("System", javafx.scene.text.FontWeight.BOLD, 10));
        gc3D.fillText(scaleLabel, bx + barWidthPx / 2.0 - 14.0, by - 6.0);
    }

    private void drawColonyOverlay3D(double cx, double cy, double scale, double radAz, double radEl) {
        Random rand = new Random(4321);
        int antCount = 35;
        for (int i = 0; i < antCount; i++) {
            int gx = 12 + (int)(rand.nextDouble() * (GRID_SIZE - 24));
            int gy = 12 + (int)(rand.nextDouble() * (GRID_SIZE - 24));
            double z = heightGrid[gx][gy] * 40.0 + 1.5;
            double[] p = project3DPoint(gx, gy, z, cx, cy, scale, radAz, radEl);
            double antR = Math.max(1.8, 2.5 * (zoom / 7.5));

            gc3D.setFill(i % 6 == 0 ? Color.web("#ef4444") : Color.web("#f59e0b"));
            gc3D.fillOval(p[0] - antR, p[1] - antR, antR * 2, antR * 2);
            gc3D.setFill(Color.web("#0f172a"));
            gc3D.fillOval(p[0] - antR * 0.5, p[1] - antR * 0.5, antR, antR);
        }
    }

    private void drawGalleriesOverlay3D(double cx, double cy, double scale, double radAz, double radEl) {
        int nestX = GRID_SIZE / 2;
        int nestY = GRID_SIZE / 2;
        double baseZ = heightGrid[nestX][nestY] * 40.0;

        // Draw subterranean entrance & shafts
        double[] pEnt = project3DPoint(nestX, nestY, baseZ, cx, cy, scale, radAz, radEl);
        double[] pShaft = project3DPoint(nestX, nestY, baseZ - 14.0, cx, cy, scale, radAz, radEl);

        gc3D.setStroke(Color.web("#f59e0b", 0.85));
        gc3D.setLineWidth(Math.max(2.5, 4.5 * (zoom / 7.5)));
        gc3D.strokeLine(pEnt[0], pEnt[1], pShaft[0], pShaft[1]);

        // Queen Chamber (Magenta glow)
        double[] pQueen = project3DPoint(nestX + 4, nestY, baseZ - 22.0, cx, cy, scale, radAz, radEl);
        gc3D.strokeLine(pShaft[0], pShaft[1], pQueen[0], pQueen[1]);
        gc3D.setFill(Color.web("#d946ef", 0.90));
        double rQ = Math.max(5.0, 9.0 * (zoom / 7.5));
        gc3D.fillOval(pQueen[0] - rQ, pQueen[1] - rQ, rQ * 2, rQ * 2);
        gc3D.setStroke(Color.web("#f0abfc"));
        gc3D.setLineWidth(1.5);
        gc3D.strokeOval(pQueen[0] - rQ, pQueen[1] - rQ, rQ * 2, rQ * 2);

        // Brood Chamber (Soft White glow)
        double[] pBrood = project3DPoint(nestX - 4, nestY + 3, baseZ - 16.0, cx, cy, scale, radAz, radEl);
        gc3D.setStroke(Color.web("#f59e0b", 0.85));
        gc3D.setLineWidth(Math.max(2.0, 3.5 * (zoom / 7.5)));
        gc3D.strokeLine(pShaft[0], pShaft[1], pBrood[0], pBrood[1]);
        gc3D.setFill(Color.web("#f8fafc", 0.90));
        double rB = Math.max(4.0, 7.0 * (zoom / 7.5));
        gc3D.fillOval(pBrood[0] - rB, pBrood[1] - rB, rB * 2, rB * 2);

        // Food Storage Chamber (Green glow)
        double[] pFood = project3DPoint(nestX + 3, nestY - 4, baseZ - 10.0, cx, cy, scale, radAz, radEl);
        gc3D.strokeLine(pShaft[0], pShaft[1], pFood[0], pFood[1]);
        gc3D.setFill(Color.web("#22c55e", 0.90));
        double rF = Math.max(4.0, 7.0 * (zoom / 7.5));
        gc3D.fillOval(pFood[0] - rF, pFood[1] - rF, rF * 2, rF * 2);
    }

    private void drawPheromoneOverlay3D(double cx, double cy, double scale, double radAz, double radEl) {
        int nestX = GRID_SIZE / 2, nestY = GRID_SIZE / 2;
        int foodX = 14, foodY = 16;
        Random rand = new Random(888);

        // Render food trail spots as glowing purple/pink particles rather than blue line
        for (int step = 0; step <= 25; step++) {
            double t = step / 25.0;
            int px = (int)(nestX + t * (foodX - nestX) + (rand.nextDouble() - 0.5) * 1.5);
            int py = (int)(nestY + t * (foodY - nestY) + (rand.nextDouble() - 0.5) * 1.5);
            if (px < 0 || px >= GRID_SIZE || py < 0 || py >= GRID_SIZE) continue;

            double z = heightGrid[px][py] * 40.0 + 0.8;
            double[] p = project3DPoint(px, py, z, cx, cy, scale, radAz, radEl);
            double dotR = Math.max(2.0, 4.0 * (zoom / 7.5));

            gc3D.setFill(Color.web("#a855f7", 0.65)); // Purple glowing trail spot
            gc3D.fillOval(p[0] - dotR, p[1] - dotR, dotR * 2, dotR * 2);
            gc3D.setFill(Color.web("#ec4899", 0.80)); // Pink center
            gc3D.fillOval(p[0] - dotR * 0.5, p[1] - dotR * 0.5, dotR, dotR);
        }
    }

    private void drawWeatherOverlay3D(double w, double h) {
        gc3D.setFill(Color.web("#38bdf8", 0.08));
        gc3D.fillOval(w * 0.1, 15, w * 0.35, 45);
        gc3D.fillOval(w * 0.55, 25, w * 0.30, 40);
    }

    private void drawStratigraphySideWalls3D(double cx, double cy, double scale, double radAz, double radEl, double maxDepthPx, int step, boolean renderBackWallsOnly) {
        double halfGrid = GRID_SIZE / 2.0;

        double[][] edgeCenters = {
            {0.0, -halfGrid},
            {halfGrid - step, 0.0},
            {0.0, halfGrid - step},
            {-halfGrid, 0.0}
        };

        Integer[] order = {0, 1, 2, 3};
        java.util.Arrays.sort(order, (a, b) -> {
            double pyA = edgeCenters[a][0] * Math.sin(radAz) + edgeCenters[a][1] * Math.cos(radAz);
            double pyB = edgeCenters[b][0] * Math.sin(radAz) + edgeCenters[b][1] * Math.cos(radAz);
            return Double.compare(pyA, pyB);
        });

        for (int edgeIdx : order) {
            double depthVal = edgeCenters[edgeIdx][0] * Math.sin(radAz) + edgeCenters[edgeIdx][1] * Math.cos(radAz);
            boolean isBack = depthVal < 0;
            if (renderBackWallsOnly != isBack) continue;

            switch (edgeIdx) {
                case 0 -> {
                    for (int x = 0; x < GRID_SIZE - step; x += step) {
                        drawSingleWallSegment3D(x, 0, x + step, 0, cx, cy, scale, radAz, radEl, maxDepthPx, false);
                    }
                }
                case 1 -> {
                    for (int y = 0; y < GRID_SIZE - step; y += step) {
                        drawSingleWallSegment3D(GRID_SIZE - step, y, GRID_SIZE - step, y + step, cx, cy, scale, radAz, radEl, maxDepthPx, false);
                    }
                }
                case 2 -> {
                    for (int x = 0; x < GRID_SIZE - step; x += step) {
                        drawSingleWallSegment3D(x, GRID_SIZE - step, x + step, GRID_SIZE - step, cx, cy, scale, radAz, radEl, maxDepthPx, false);
                    }
                }
                case 3 -> {
                    for (int y = 0; y < GRID_SIZE - step; y += step) {
                        drawSingleWallSegment3D(0, y, 0, y + step, cx, cy, scale, radAz, radEl, maxDepthPx, false);
                    }
                }
            }
        }
    }

    private void drawSingleWallSegment3D(int x0, int y0, int x1, int y1, double cx, double cy, double scale, double radAz, double radEl, double maxDepthPx, boolean isSliceCutaway) {
        double layerDepthPx = maxDepthPx / SOIL_DEPTH;
        double surfZ0 = heightGrid[x0][y0] * 40.0;
        double surfZ1 = heightGrid[x1][y1] * 40.0;

        if (carvedVoxelGrid[x0][y0]) surfZ0 -= 15.0;
        if (carvedVoxelGrid[x1][y1]) surfZ1 -= 15.0;

        boolean isTranslucent = showTranslucentVolumetricModeCheck != null && showTranslucentVolumetricModeCheck.isSelected();
        boolean showInclusions = showGravelInclusionsCheck != null && showGravelInclusionsCheck.isSelected();
        boolean isAdvMode = useAdvancedVolumetricModeCheck == null || useAdvancedVolumetricModeCheck.isSelected();

        for (int d = 0; d < SOIL_DEPTH; d++) {
            double topZ0 = surfZ0 - d * layerDepthPx;
            double topZ1 = surfZ1 - d * layerDepthPx;
            double botZ0 = surfZ0 - (d + 1) * layerDepthPx;
            double botZ1 = surfZ1 - (d + 1) * layerDepthPx;

            double[] pTop0 = project3DPoint(x0, y0, topZ0, cx, cy, scale, radAz, radEl);
            double[] pTop1 = project3DPoint(x1, y1, topZ1, cx, cy, scale, radAz, radEl);
            double[] pBot1 = project3DPoint(x1, y1, botZ1, cx, cy, scale, radAz, radEl);
            double[] pBot0 = project3DPoint(x0, y0, botZ0, cx, cy, scale, radAz, radEl);

            byte mat = soilLayers[x0][y0][d];
            boolean isVoid = voidGrid[x0][y0][d];

            Color matCol;
            if (isVoid) {
                matCol = Color.web("#0f172a");
            } else if (!isMaterialVisible(mat)) {
                matCol = Color.web("#1e293b", 0.3);
            } else {
                matCol = getMaterialColor(mat);
            }

            if (isSliceCutaway) {
                matCol = matCol.deriveColor(0, 1.1, 1.2, 1.0);
            }

            if (isTranslucent) {
                matCol = Color.color(matCol.getRed(), matCol.getGreen(), matCol.getBlue(), 0.55);
            }

            gc3D.setFill(matCol);
            gc3D.fillPolygon(new double[]{pTop0[0], pTop1[0], pBot1[0], pBot0[0]}, new double[]{pTop0[1], pTop1[1], pBot1[1], pBot0[1]}, 4);

            if (isAdvMode && showInclusions && !isVoid && isMaterialVisible(mat)) {
                double midX = (pTop0[0] + pTop1[0] + pBot1[0] + pBot0[0]) / 4.0;
                double midY = (pTop0[1] + pTop1[1] + pBot1[1] + pBot0[1]) / 4.0;
                double voxSize = Math.max(1.5, Math.abs(pTop1[0] - pTop0[0]) * 0.4);

                if (mat == 3) {
                    gc3D.setFill(Color.web("#e2e8f0"));
                    gc3D.fillOval(midX - voxSize * 0.5, midY - voxSize * 0.5, voxSize, voxSize * 0.7);
                } else if (mat == 2) {
                    gc3D.setFill(Color.web("#7c2d12"));
                    gc3D.fillRect(midX - voxSize * 0.4, midY - voxSize * 0.3, voxSize * 0.8, voxSize * 0.5);
                } else if (mat == 1) {
                    gc3D.setFill(Color.web("#fef08a"));
                    gc3D.fillOval(midX - voxSize * 0.3, midY - voxSize * 0.3, voxSize * 0.6, voxSize * 0.6);
                }
            }

            if (showHumidityCheck != null && showHumidityCheck.isSelected()) {
                gc3D.setFill(Color.web("#0284c7", humidityGrid[x0][y0] * 0.45));
                gc3D.fillPolygon(new double[]{pTop0[0], pTop1[0], pBot1[0], pBot0[0]}, new double[]{pTop0[1], pTop1[1], pBot1[1], pBot0[1]}, 4);
            }

            gc3D.setStroke(matCol.darker());
            gc3D.setLineWidth(0.2);
            gc3D.strokePolygon(new double[]{pTop0[0], pTop1[0], pBot1[0], pBot0[0]}, new double[]{pTop0[1], pTop1[1], pBot1[1], pBot0[1]}, 4);
        }

        if (showChamferedBezelCheck != null && showChamferedBezelCheck.isSelected()) {
            double[] pTop0 = project3DPoint(x0, y0, surfZ0, cx, cy, scale, radAz, radEl);
            double[] pTop1 = project3DPoint(x1, y1, surfZ1, cx, cy, scale, radAz, radEl);

            gc3D.setStroke(Color.web("#38bdf8", 0.9));
            gc3D.setLineWidth(1.6);
            gc3D.strokeLine(pTop0[0], pTop0[1], pTop1[0], pTop1[1]);
        }

        double botBed0 = surfZ0 - maxDepthPx;
        double botBed1 = surfZ1 - maxDepthPx;
        double[] pBed0 = project3DPoint(x0, y0, botBed0, cx, cy, scale, radAz, radEl);
        double[] pBed1 = project3DPoint(x1, y1, botBed1, cx, cy, scale, radAz, radEl);
        gc3D.setStroke(Color.web("#020617"));
        gc3D.setLineWidth(1.8);
        gc3D.strokeLine(pBed0[0], pBed0[1], pBed1[0], pBed1[1]);
    }

    private boolean isMaterialVisible(byte mat) {
        if (mat == 0 && showEarthCheck != null && !showEarthCheck.isSelected()) return false;
        if (mat == 1 && showSandCheck != null && !showSandCheck.isSelected()) return false;
        if (mat == 2 && showClayCheck != null && !showClayCheck.isSelected()) return false;
        if (mat == 3 && showStoneCheck != null && !showStoneCheck.isSelected()) return false;
        return true;
    }

    private void drawSide() {
        if (canvasSide == null || canvasSide.getWidth() < 10 || canvasSide.getHeight() < 10) return;
        double w = canvasSide.getWidth();
        double h = canvasSide.getHeight();

        gcSide.save();
        gcSide.setFill(Color.web("#0f172a"));
        gcSide.fillRect(0, 0, w, h);

        gcSide.beginPath();
        gcSide.rect(10, 10, w - 20, h - 20);
        gcSide.clip();

        double blockW = (w - 20.0) / GRID_SIZE;
        double blockH = (h - 90.0) / SOIL_DEPTH;
        int midY = GRID_SIZE / 2;

        for (int x = 0; x < GRID_SIZE; x++) {
            double surfaceH = heightGrid[x][midY] * 25.0;
            for (int d = 0; d < SOIL_DEPTH; d++) {
                double px = 10 + x * blockW;
                double py = 65 - surfaceH + d * blockH;

                byte mat = soilLayers[x][midY][d];
                if (voidGrid[x][midY][d] || !isMaterialVisible(mat)) {
                    gcSide.setFill(Color.web("#0f172a"));
                } else {
                    gcSide.setFill(getMaterialColor(mat));
                }
                gcSide.fillRect(px, py, Math.max(1, blockW + 0.5), Math.max(1, blockH + 0.5));
            }

            double px = 10 + x * blockW;
            double pyBase = 65 - surfaceH + SOIL_DEPTH * blockH;
            gcSide.setFill(Color.web("#020617"));
            gcSide.fillRect(px, pyBase, Math.max(1, blockW + 0.5), h - pyBase);
            gcSide.setStroke(Color.web("#334155"));
            gcSide.setLineWidth(1.0);
            gcSide.strokeLine(px, pyBase, px + Math.max(1, blockW), pyBase);
        }

        double wtDepth = waterTableDepthSlider != null ? waterTableDepthSlider.getValue() : 15;
        double wtY = 65 + (wtDepth / 50.0) * (h - 100);
        gcSide.setFill(Color.web("#0284c7"));
        gcSide.setGlobalAlpha(0.45);
        gcSide.fillRect(10, wtY, w - 20, Math.max(0, h - wtY - 10));
        gcSide.setGlobalAlpha(1.0);

        gcSide.setFill(Color.web("#d97706"));
        for (int x = 0; x < GRID_SIZE; x += 2) {
            if (carvedVoxelGrid[x][midY]) {
                double px = 10 + x * blockW;
                double py = 70;
                gcSide.fillOval(px, py, 6, 6);
            }
        }

        gcSide.restore();

        gcSide.setStroke(Color.web("#38bdf8"));
        gcSide.setLineWidth(1.0);
        gcSide.strokeRect(10, 10, w - 20, h - 20);

        gcSide.setFill(Color.WHITE);
        gcSide.fillText("Profil Stratigraphique & Vides (Coupe 2D — Nappe: " + String.format("%.0f", wtDepth) + "cm)", 20, 30);
    }

    private void drawTop() {
        if (canvasTop == null || canvasTop.getWidth() < 10 || canvasTop.getHeight() < 10) return;
        double w = canvasTop.getWidth();
        double h = canvasTop.getHeight();

        gcTop.save();
        gcTop.setFill(Color.web("#0f172a"));
        gcTop.fillRect(0, 0, w, h);

        gcTop.beginPath();
        gcTop.rect(10, 10, w - 20, h - 20);
        gcTop.clip();

        double tZoom = topZoom;
        double cx = topPanX;
        double cy = topPanY;

        double side = Math.min((w - 30) / GRID_SIZE, (h - 30) / GRID_SIZE) * tZoom;
        double cellW = side;
        double cellH = side;

        for (int x = 0; x < GRID_SIZE; x += 2) {
            for (int y = 0; y < GRID_SIZE; y += 2) {
                byte mat = soilLayers[x][y][0];
                boolean visibleMat = isMaterialVisible(mat);

                Color col = visibleMat ? getMaterialColor(mat) : Color.web("#0f172a");
                if (carvedVoxelGrid[x][y]) col = Color.web("#d97706");
                gcTop.setFill(col);
                gcTop.fillRect(15 + x * cellW + cx, 15 + y * cellH + cy, cellW * 2, cellH * 2);

                if (showHumidityCheck != null && showHumidityCheck.isSelected()) {
                    gcTop.setFill(Color.web("#0284c7"));
                    gcTop.setGlobalAlpha(humidityGrid[x][y] * 0.6);
                    gcTop.fillRect(15 + x * cellW + cx, 15 + y * cellH + cy, cellW * 2, cellH * 2);
                    gcTop.setGlobalAlpha(1.0);
                }
            }
        }

        if (riverCheck != null && riverCheck.isSelected() && riverPath != null && riverPath.size() > 1) {
            gcTop.setStroke(Color.web("#0284c7"));
            gcTop.setLineWidth((riverWidthSlider.getValue() / 35.0) * tZoom);
            gcTop.beginPath();
            boolean first = true;
            for (int[] pt : riverPath) {
                double px = 15 + pt[0] * cellW + cx;
                double py = 15 + pt[1] * cellH + cy;
                if (first) { gcTop.moveTo(px, py); first = false; }
                else gcTop.lineTo(px, py);
            }
            gcTop.stroke();
        }

        if (hollowLogsSlider != null) {
            int stumpCount = (int) hollowLogsSlider.getValue();
            Random sRand = new Random(99);
            for (int i = 0; i < stumpCount; i++) {
                int sx = 15 + (int)(sRand.nextDouble() * (GRID_SIZE - 30));
                int sy = 15 + (int)(sRand.nextDouble() * (GRID_SIZE - 30));
                double px = 15 + sx * cellW + cx;
                double py = 15 + sy * cellH + cy;

                gcTop.setFill(Color.web("#78350f"));
                gcTop.fillRect(px - 5 * tZoom, py - 5 * tZoom, 10 * tZoom, 10 * tZoom);
                gcTop.setFill(Color.web("#451a03"));
                gcTop.fillOval(px - 3 * tZoom, py - 3 * tZoom, 6 * tZoom, 6 * tZoom);
            }
        }

        for (SurfaceFloraItem item : surfaceFloraItems) {
            double px = 15 + item.gx * cellW + cx;
            double py = 15 + item.gy * cellH + cy;
            double sz = item.scale * tZoom * 3.5;

            switch (item.type) {
                case 0:
                    gcTop.setFill(Color.web("#4ade80"));
                    gcTop.fillOval(px - sz, py - sz, sz * 2, sz * 2);
                    break;
                case 1:
                    gcTop.setFill(Color.web("#166534"));
                    gcTop.fillOval(px - sz, py - sz, sz * 2, sz * 2);
                    gcTop.setFill(Color.web("#a3e635"));
                    gcTop.fillOval(px - sz * 0.5, py - sz * 0.5, sz, sz);
                    break;
                case 2:
                    gcTop.setFill(Color.web("#f43f5e"));
                    gcTop.fillOval(px - sz, py - sz, sz * 2, sz * 2);
                    break;
                case 3:
                    gcTop.setFill(Color.web("#15803d", 0.75));
                    gcTop.fillOval(px - sz * 1.5, py - sz, sz * 3, sz * 2);
                    break;
                case 4:
                    gcTop.setFill(Color.web("#854d0e", 0.85));
                    gcTop.fillOval(px - sz * 1.5, py - sz * 0.8, sz * 3.0, sz * 1.6);
                    gcTop.setFill(Color.web("#a16207", 0.7));
                    gcTop.fillOval(px - sz * 0.8, py - sz * 1.0, sz * 2.0, sz * 1.5);
                    break;
                case 5:
                    gcTop.setStroke(Color.web("#451a03"));
                    gcTop.setLineWidth(1.4 * tZoom);
                    gcTop.strokeLine(px - sz, py - sz, px + sz, py + sz);
                    break;
                case 6:
                    gcTop.setFill(Color.web("#94a3b8"));
                    gcTop.fillOval(px - sz * 0.6, py - sz * 0.6, sz * 1.2, sz * 1.2);
                    break;
            }
        }

        if (treeCountSlider != null && (showVegetationCheck == null || showVegetationCheck.isSelected())) {
            int count = (int) treeCountSlider.getValue();
            int treeIdx = comboTreeSpecies != null ? comboTreeSpecies.getSelectionModel().getSelectedIndex() : 4;
            double baseTreeHeightM = switch (treeIdx) {
                case 0 -> 6.0;   // Bambou
                case 1 -> 2.5;   // Souche
                case 2 -> 12.0;  // Bouleau
                case 3 -> 4.0;   // Cactus
                case 4 -> 15.0;  // Chêne
                case 5 -> 18.0;  // Pin
                case 6 -> 8.0;   // Acacia
                default -> 15.0;
            };
            double sideM = surfaceSizeSlider != null ? surfaceSizeSlider.getValue() : 25.0;
            double topTreeRadius = Math.max(6.0, (baseTreeHeightM * 0.30) * ((cellW * GRID_SIZE) / Math.max(1.0, sideM)) * tZoom);
            Random rand = new Random(77);
            for (int i = 0; i < count; i++) {
                int gx = 10 + (int)(rand.nextDouble() * (GRID_SIZE - 20));
                int gy = 10 + (int)(rand.nextDouble() * (GRID_SIZE - 20));
                if (isNearRiver(gx, gy, 3)) continue; // Never spawn trees in river!

                double px = 15 + gx * cellW + cx;
                double py = 15 + gy * cellH + cy;

                gcTop.setFill(Color.web("#14532d"));
                gcTop.fillOval(px - topTreeRadius, py - topTreeRadius, topTreeRadius * 2, topTreeRadius * 2);
                gcTop.setFill(Color.web("#166534"));
                gcTop.fillOval(px - topTreeRadius * 0.7, py - topTreeRadius * 0.7, topTreeRadius * 1.4, topTreeRadius * 1.4);
                gcTop.setFill(Color.web("#15803d"));
                gcTop.fillOval(px - topTreeRadius * 0.4, py - topTreeRadius * 0.4, topTreeRadius * 0.8, topTreeRadius * 0.8);
            }
        }

        gcTop.restore();

        gcTop.setStroke(Color.web("#38bdf8"));
        gcTop.setLineWidth(1.0);
        gcTop.strokeRect(10, 10, w - 20, h - 20);

        gcTop.setFill(Color.WHITE);
        double sM = surfaceSizeSlider != null ? surfaceSizeSlider.getValue() : 2.0;
        gcTop.fillText(String.format("Vue du Dessus (Côté : %.1fm — Surface : %.1fm²)", sM, sM * sM), 20, 30);
    }

    private Color getMaterialColor(byte mat) {
        switch (mat) {
            case 1: return Color.web("#eab308"); // Sand
            case 2: return Color.web("#9a3412"); // Clay
            case 3: return Color.web("#64748b"); // Stone
            case 4: return Color.web("#523219"); // Organic
            default: return Color.web("#3d2817"); // Earth
        }
    }

    private Slider mkSlider(double min, double max, double val) {
        Slider s = new Slider(min, max, val);
        s.setShowTickLabels(true);
        s.setShowTickMarks(true);
        s.setPrefWidth(180);
        return s;
    }

    private Spinner<Integer> mkSpinner(int min, int max, int val) {
        Spinner<Integer> sp = new Spinner<>(min, max, val);
        sp.setPrefWidth(75);
        sp.setEditable(true);
        sp.valueProperty().addListener((o, a, b) -> repaintAllViews());
        return sp;
    }

    private void addLsn(Slider... sliders) {
        for (Slider s : sliders) {
            s.valueProperty().addListener((o, a, b) -> repaintAllViews());
        }
    }


    private void triggerGenerate() {
        if (onGenerateCallback != null) {
            onGenerateCallback.accept(getConfiguration());
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Génération du Monde exécutée (Résolution sub-millimétrique: " + resolutionSlider.getValue() + "mm).");
            alert.setTitle("Éditeur de Monde");
            alert.setHeaderText("Monde Généré avec Succès");
            alert.show();
        }
    }

    public void setOnGenerate(Consumer<Map<String, Object>> cb) {
        this.onGenerateCallback = cb;
    }

    public Map<String, Object> getConfiguration() {
        Map<String, Object> cfg = new LinkedHashMap<>();
        cfg.put("cityName", cityNameField != null ? cityNameField.getText() : "Fontainebleau, FR");
        cfg.put("latitude", latField != null ? latField.getText() : "48.4047");
        cfg.put("longitude", lonField != null ? lonField.getText() : "2.7016");
        cfg.put("reliefSeed", reliefSeedField != null ? reliefSeedField.getText() : "774829");
        cfg.put("soilSeed", soilSeedField != null ? soilSeedField.getText() : "123456");
        cfg.put("hydroSeed", hydroSeedField != null ? hydroSeedField.getText() : "987654");
        cfg.put("floraSeed", floraSeedField != null ? floraSeedField.getText() : "774829");
        cfg.put("structSeed", structSeedField != null ? structSeedField.getText() : "555123");

        cfg.put("mountainSlopeAngle", slopeAngleSlider != null ? slopeAngleSlider.getValue() : 0.0);
        cfg.put("mountainSlopeDirection", slopeDirectionCombo != null ? slopeDirectionCombo.getSelectionModel().getSelectedIndex() : 0);

        cfg.put("surfaceSizeMeters", surfaceSizeSlider.getValue());
        cfg.put("depthMeters", depthSlider != null ? depthSlider.getValue() : 1.5);
        cfg.put("resolutionMm", resolutionSlider.getValue());
        cfg.put("roughness", roughnessSlider.getValue());
        cfg.put("compaction", compactionSlider.getValue());
        cfg.put("stratification", stratificationSlider != null ? stratificationSlider.getValue() : 0.7);
        cfg.put("mixingRate", mixingRateSlider != null ? mixingRateSlider.getValue() : 0.3);
        cfg.put("baseHumidity", baseHumiditySlider != null ? baseHumiditySlider.getValue() : 0.35);
        cfg.put("voidDensity", voidDensitySlider != null ? voidDensitySlider.getValue() : 0.08);
        cfg.put("soilComposition", Map.of(
                "earth", earthSpinner.getValue(),
                "sand", sandSpinner.getValue(),
                "clay", claySpinner.getValue(),
                "stone", stoneSpinner.getValue(),
                "organic", organicSpinner.getValue()
        ));
        cfg.put("edibleFloraDensity", edibleDensitySlider.getValue());
        cfg.put("nonEdibleFloraDensity", nonEdibleDensitySlider.getValue());
        cfg.put("hasRiver", riverCheck.isSelected());
        cfg.put("riverWidthMm", riverWidthSlider.getValue());
        cfg.put("riverVelocity", riverVelocitySlider.getValue());
        cfg.put("waterTableDepthCm", waterTableDepthSlider.getValue());
        cfg.put("treeCount", (int) treeCountSlider.getValue());
        cfg.put("hollowLogs", (int) hollowLogsSlider.getValue());
        return cfg;
    }

    private void doExport() {
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Exporter Configuration du Monde");
        fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("JSON", "*.json"));
        fc.setInitialFileName("world_preset.json");
        File f = fc.showSaveDialog(getScene().getWindow());
        if (f == null) return;
        try {
            new com.fasterxml.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(f, getConfiguration());
            new Alert(Alert.AlertType.INFORMATION, "Preset sauvegardé avec succès.").show();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Erreur d'exportation: " + ex.getMessage()).show();
        }
    }

    private void doImport() {
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Importer Configuration du Monde");
        fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("JSON", "*.json"));
        File f = fc.showOpenDialog(getScene().getWindow());
        if (f == null) return;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> cfg = new com.fasterxml.jackson.databind.ObjectMapper().readValue(f, Map.class);
            loadConfiguration(cfg);
            new Alert(Alert.AlertType.INFORMATION, "Preset de monde chargé.").show();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Erreur d'importation: " + ex.getMessage()).show();
        }
    }

    public void loadConfiguration(Map<String, Object> cfg) {
        if (cfg == null) return;
        if (cfg.containsKey("cityName") && cityNameField != null) cityNameField.setText(String.valueOf(cfg.get("cityName")));
        if (cfg.containsKey("reliefSeed") && reliefSeedField != null) reliefSeedField.setText(String.valueOf(cfg.get("reliefSeed")));
        if (cfg.containsKey("soilSeed") && soilSeedField != null) soilSeedField.setText(String.valueOf(cfg.get("soilSeed")));
        if (cfg.containsKey("hydroSeed") && hydroSeedField != null) hydroSeedField.setText(String.valueOf(cfg.get("hydroSeed")));
        if (cfg.containsKey("floraSeed") && floraSeedField != null) floraSeedField.setText(String.valueOf(cfg.get("floraSeed")));
        if (cfg.containsKey("structSeed") && structSeedField != null) structSeedField.setText(String.valueOf(cfg.get("structSeed")));

        if (cfg.containsKey("mountainSlopeAngle") && slopeAngleSlider != null) slopeAngleSlider.setValue(((Number) cfg.get("mountainSlopeAngle")).doubleValue());
        if (cfg.containsKey("mountainSlopeDirection") && slopeDirectionCombo != null) {
            int idx = ((Number) cfg.get("mountainSlopeDirection")).intValue();
            if (idx >= 0 && idx < slopeDirectionCombo.getItems().size()) slopeDirectionCombo.getSelectionModel().select(idx);
        }

        if (cfg.containsKey("surfaceSizeMeters")) surfaceSizeSlider.setValue(((Number) cfg.get("surfaceSizeMeters")).doubleValue());
        if (cfg.containsKey("depthMeters") && depthSlider != null) depthSlider.setValue(((Number) cfg.get("depthMeters")).doubleValue());
        if (cfg.containsKey("depth") && depthSlider != null) depthSlider.setValue(((Number) cfg.get("depth")).doubleValue());
        if (cfg.containsKey("resolutionMm")) resolutionSlider.setValue(((Number) cfg.get("resolutionMm")).doubleValue());
        if (cfg.containsKey("resolution")) resolutionSlider.setValue(((Number) cfg.get("resolution")).doubleValue());
        if (cfg.containsKey("roughness")) roughnessSlider.setValue(((Number) cfg.get("roughness")).doubleValue());
        if (cfg.containsKey("compaction")) compactionSlider.setValue(((Number) cfg.get("compaction")).doubleValue());
        if (cfg.containsKey("stratification") && stratificationSlider != null) stratificationSlider.setValue(((Number) cfg.get("stratification")).doubleValue());
        if (cfg.containsKey("mixingRate") && mixingRateSlider != null) mixingRateSlider.setValue(((Number) cfg.get("mixingRate")).doubleValue());
        if (cfg.containsKey("baseHumidity") && baseHumiditySlider != null) baseHumiditySlider.setValue(((Number) cfg.get("baseHumidity")).doubleValue());
        if (cfg.containsKey("latitude") && latField != null) latField.setText(String.valueOf(cfg.get("latitude")));
        if (cfg.containsKey("longitude") && lonField != null) lonField.setText(String.valueOf(cfg.get("longitude")));
        if (cfg.containsKey("latitude") && cfg.containsKey("longitude")) {
            try {
                double lat = Double.parseDouble(String.valueOf(cfg.get("latitude")));
                double lon = Double.parseDouble(String.valueOf(cfg.get("longitude")));
                applyBioclimaticAdaptation(lat, lon);
            } catch (Exception ignored) {}
        }

        if (cfg.containsKey("soilComposition") && cfg.get("soilComposition") instanceof Map<?,?> soilMap) {
            if (soilMap.containsKey("earth") && earthSpinner != null) earthSpinner.getValueFactory().setValue(((Number) soilMap.get("earth")).intValue());
            if (soilMap.containsKey("sand") && sandSpinner != null) sandSpinner.getValueFactory().setValue(((Number) soilMap.get("sand")).intValue());
            if (soilMap.containsKey("clay") && claySpinner != null) claySpinner.getValueFactory().setValue(((Number) soilMap.get("clay")).intValue());
            if (soilMap.containsKey("stone") && stoneSpinner != null) stoneSpinner.getValueFactory().setValue(((Number) soilMap.get("stone")).intValue());
            if (soilMap.containsKey("organic") && organicSpinner != null) organicSpinner.getValueFactory().setValue(((Number) soilMap.get("organic")).intValue());
        }

        if (cfg.containsKey("treeSpeciesIndex") && comboTreeSpecies != null) {
            int idx = ((Number) cfg.get("treeSpeciesIndex")).intValue();
            if (idx >= 0 && idx < comboTreeSpecies.getItems().size()) comboTreeSpecies.getSelectionModel().select(idx);
        }

        if (cfg.containsKey("treeComposition") && cfg.get("treeComposition") instanceof Map<?,?> treeMap) {
            if (treeMap.containsKey("oak") && oakPctSpinner != null) oakPctSpinner.getValueFactory().setValue(((Number) treeMap.get("oak")).intValue());
            if (treeMap.containsKey("pine") && pinePctSpinner != null) pinePctSpinner.getValueFactory().setValue(((Number) treeMap.get("pine")).intValue());
            if (treeMap.containsKey("acacia") && acaciaPctSpinner != null) acaciaPctSpinner.getValueFactory().setValue(((Number) treeMap.get("acacia")).intValue());
            if (treeMap.containsKey("cactus") && cactusPctSpinner != null) cactusPctSpinner.getValueFactory().setValue(((Number) treeMap.get("cactus")).intValue());
            if (treeMap.containsKey("birch") && birchPctSpinner != null) birchPctSpinner.getValueFactory().setValue(((Number) treeMap.get("birch")).intValue());
            if (treeMap.containsKey("bamboo") && bambooPctSpinner != null) bambooPctSpinner.getValueFactory().setValue(((Number) treeMap.get("bamboo")).intValue());
            if (treeMap.containsKey("deadWood") && deadWoodPctSpinner != null) deadWoodPctSpinner.getValueFactory().setValue(((Number) treeMap.get("deadWood")).intValue());
        }

        if (cfg.containsKey("floraSeed") && floraSeedField != null) floraSeedField.setText(String.valueOf(cfg.get("floraSeed")));
        if (cfg.containsKey("edibleFloraDensity") && edibleDensitySlider != null) edibleDensitySlider.setValue(((Number) cfg.get("edibleFloraDensity")).doubleValue());
        if (cfg.containsKey("nonEdibleFloraDensity") && nonEdibleDensitySlider != null) nonEdibleDensitySlider.setValue(((Number) cfg.get("nonEdibleFloraDensity")).doubleValue());
        if (cfg.containsKey("leafLitter") && leafLitterSlider != null) leafLitterSlider.setValue(((Number) cfg.get("leafLitter")).doubleValue());
        if (cfg.containsKey("twigDebris") && twigDebrisSlider != null) twigDebrisSlider.setValue(((Number) cfg.get("twigDebris")).doubleValue());

        if (cfg.containsKey("aphidPlant") && aphidPlantCheck != null) aphidPlantCheck.setSelected((Boolean) cfg.get("aphidPlant"));
        if (cfg.containsKey("nectarFlowers") && nectarFlowersCheck != null) nectarFlowersCheck.setSelected((Boolean) cfg.get("nectarFlowers"));
        if (cfg.containsKey("seedGrass") && seedGrassCheck != null) seedGrassCheck.setSelected((Boolean) cfg.get("seedGrass"));
        if (cfg.containsKey("fungusFoliage") && fungusFoliageCheck != null) fungusFoliageCheck.setSelected((Boolean) cfg.get("fungusFoliage"));
        if (cfg.containsKey("moss") && mossCheck != null) mossCheck.setSelected((Boolean) cfg.get("moss"));
        if (cfg.containsKey("pineLitter") && pineLitterCheck != null) pineLitterCheck.setSelected((Boolean) cfg.get("pineLitter"));
        if (cfg.containsKey("fernObstacle") && fernObstacleCheck != null) fernObstacleCheck.setSelected((Boolean) cfg.get("fernObstacle"));

        if (cfg.containsKey("hasRiver") && riverCheck != null) riverCheck.setSelected((Boolean) cfg.get("hasRiver"));
        if (cfg.containsKey("riverWidthMm") && riverWidthSlider != null) riverWidthSlider.setValue(((Number) cfg.get("riverWidthMm")).doubleValue());
        if (cfg.containsKey("riverVelocity") && riverVelocitySlider != null) riverVelocitySlider.setValue(((Number) cfg.get("riverVelocity")).doubleValue());
        if (cfg.containsKey("staticPools") && staticPoolsSlider != null) staticPoolsSlider.setValue(((Number) cfg.get("staticPools")).doubleValue());
        if (cfg.containsKey("waterTableDepthCm") && waterTableDepthSlider != null) waterTableDepthSlider.setValue(((Number) cfg.get("waterTableDepthCm")).doubleValue());
        else if (cfg.containsKey("waterTableDepth") && waterTableDepthSlider != null) waterTableDepthSlider.setValue(((Number) cfg.get("waterTableDepth")).doubleValue());

        if (cfg.containsKey("treeCount") && treeCountSlider != null) treeCountSlider.setValue(((Number) cfg.get("treeCount")).doubleValue());
        if (cfg.containsKey("hollowLogs") && hollowLogsSlider != null) hollowLogsSlider.setValue(((Number) cfg.get("hollowLogs")).doubleValue());
        if (cfg.containsKey("rockCrevices") && rockCrevicesSlider != null) rockCrevicesSlider.setValue(((Number) cfg.get("rockCrevices")).doubleValue());

        regenerateAndRepaint();
    }

    private HBox sv(Slider s) {
        return sv(s, "");
    }

    private HBox sv(Slider s, String unit) {
        HBox b = new HBox(8);
        b.setAlignment(Pos.CENTER_LEFT);
        Label v = new Label(fmt(s.getValue()) + (unit == null || unit.isEmpty() ? "" : " " + unit));
        v.setStyle("-fx-text-fill:#00d4ff;-fx-min-width:36;-fx-font-weight:bold;");
        s.valueProperty().addListener((o, a, n) -> v.setText(fmt(n.doubleValue()) + (unit == null || unit.isEmpty() ? "" : " " + unit)));
        b.getChildren().addAll(s, v);
        return b;
    }

    private String fmt(double d) {
        if (d == (long) d) {
            return String.format("%d", (long) d);
        }
        return String.format("%.1f", d);
    }
}
