/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.NotificationOverlay;
import org.swarmforge.client.util.ThemeManager;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
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
    private ResizableCanvas canvas3D, canvasSide, canvasTop;
    private GraphicsContext gc3D, gcSide, gcTop;

    private boolean isActive = true;

    public void setActive(boolean active) {
        this.isActive = active;
        if (active) repaintAllViews();
    }

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

    // View Synchronization CheckBox & Right Legend ScrollPane
    private CheckBox syncViewsCheckBox;
    private ScrollPane rightScroll;

    // Controls: 0. Terrain Source & Geo Location
    private TextField cityNameField;
    private TextField citySearchField;
    private Label geoStatusLabel;
    private TextField latField;
    private TextField lonField;

    // Controls: Block Seeds & Procedural Generators
    private TextField scaleSeedField;
    private TextField reliefSeedField;
    private TextField soilSeedField;
    private TextField hydroSeedField;
    private TextField structSeedField;
    private TextField floraSeedField;

    // Mountain Slope Controls
    private Slider slopeAngleSlider;
    private ComboBox<String> slopeDirectionCombo;

    // Controls: Layer, Vegetation & Nest Visibility Toggles
    private CheckBox showTerrainCheck;
    private CheckBox showEarthCheck;
    private CheckBox showSandCheck;
    private CheckBox showClayCheck;
    private CheckBox showSiltCheck;
    private CheckBox showPeatCheck;
    private CheckBox showStoneCheck;
    private CheckBox showGravelCheck;
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
    public static class SurfaceFloraItem {
        public int gx, gy;
        public int type; // 0=Grass, 1=AphidPlant, 2=NectarFlower, 3=Moss, 4=PineLitter, 5=TwigDebris, 6=Pebble
        public double scale;
        public double rotation;
        public boolean isCharred = false;

        public SurfaceFloraItem(int gx, int gy, int type, double scale, double rotation) {
            this.gx = gx; this.gy = gy; this.type = type; this.scale = scale; this.rotation = rotation;
        }
    }
    private List<SurfaceFloraItem> surfaceFloraItems = new ArrayList<>();

    public enum RenderMode { REALISTIC, SCIENTIFIC, GAMIFIED }
    private RenderMode currentRenderMode = RenderMode.SCIENTIFIC;

    // Viewport Layer Visibility Flags
    private boolean isSimulationMode = false;
    private VBox nestLegendBox;
    private boolean isTerrainVisible = true;
    private boolean isGalleriesVisible = true;
    private boolean isPheromonesVisible = true;
    private boolean isColonyVisible = true;
    private boolean isWeatherVisible = true;
    private boolean isDualMinimapVisible = true;

    public enum PheromoneRenderMode {
        HEATMAP_GRADIENT,  // Continuous density surface / gradient field
        VOXEL_PARTICLES,   // Discrete points / voxels with dynamic radius
        HYBRID_GIS         // Multi-layer SIG (Gradient + Particle Core)
    }

    private PheromoneRenderMode pheromoneRenderMode = PheromoneRenderMode.HEATMAP_GRADIENT;

    // Multi-Channel GIS Layer Toggles
    private boolean showFoodPheromone = true;
    private boolean showHomePheromone = true;
    private boolean showAlarmPheromone = true;
    private boolean showRecruitmentPheromone = true;
    private boolean showQueenPheromone = true;
    private boolean showDeathPheromone = true;

    private ScrollPane configScrollPane;
    private VBox sideMinimapsBox;
    private VBox rightRenderOptions;

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

    public void setHideRightRenderOptions(boolean hide) {
        if (rightRenderOptions != null) {
            rightRenderOptions.setVisible(!hide);
            rightRenderOptions.setManaged(!hide);
        }
    }

    public void setHeaderVisible(boolean visible) {
        if (headerBox != null) {
            headerBox.setVisible(visible);
            headerBox.setManaged(visible);
        }
    }

    public void setRightLegendVisible(boolean visible) {
        if (rightScroll != null) {
            rightScroll.setVisible(visible);
            rightScroll.setManaged(visible);
        }
    }

    private double previousZoom = 7.5;

    public void setFullscreenMode(boolean fs) {
        setRightLegendVisible(!fs);
        setDualMinimapVisible(!fs);
        setHeaderVisible(!fs);
        if (fs) {
            this.previousZoom = this.zoom;
            this.zoom = Math.min(22.0, this.zoom * 1.25);
            if (isSync()) { this.sideZoom = Math.max(0.3, Math.min(6.0, zoom / 7.5)); this.topZoom = sideZoom; }
            setHideHeaderPresets(true);
            setHideConfigPanel(true);
            setHideRightRenderOptions(true);
        } else {
            this.zoom = this.previousZoom;
            if (isSync()) { this.sideZoom = Math.max(0.3, Math.min(6.0, zoom / 7.5)); this.topZoom = sideZoom; }
            if (!isSimulationMode) {
                setHideHeaderPresets(false);
                setHideConfigPanel(false);
                setHideRightRenderOptions(false);
            }
        }
        repaintAllViews();
    }

    public void setSimulationMode(boolean simMode) {
        this.isSimulationMode = simMode;
        setHideHeaderPresets(simMode);
        setHideConfigPanel(simMode);
        setHideRightRenderOptions(simMode);
        if (nestLegendBox != null) {
            nestLegendBox.setVisible(simMode);
            nestLegendBox.setManaged(simMode);
        }
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

    public void setPheromoneRenderMode(PheromoneRenderMode mode) {
        this.pheromoneRenderMode = mode != null ? mode : PheromoneRenderMode.HEATMAP_GRADIENT;
        repaintAllViews();
    }

    public PheromoneRenderMode getPheromoneRenderMode() {
        return pheromoneRenderMode;
    }

    public void setPheromoneChannelFilter(int index) {
        switch (index) {
            case 1 -> { // Alarm Pheromone
                showHomePheromone = false;
                showFoodPheromone = false;
                showAlarmPheromone = true;
                showRecruitmentPheromone = false;
                showQueenPheromone = false;
                showDeathPheromone = false;
            }
            case 2 -> { // Food Pheromone
                showHomePheromone = false;
                showFoodPheromone = true;
                showAlarmPheromone = false;
                showRecruitmentPheromone = false;
                showQueenPheromone = false;
                showDeathPheromone = false;
            }
            case 3 -> { // Home Pheromone
                showHomePheromone = true;
                showFoodPheromone = false;
                showAlarmPheromone = false;
                showRecruitmentPheromone = false;
                showQueenPheromone = false;
                showDeathPheromone = false;
            }
            case 4 -> { // Recruitment Pheromone
                showHomePheromone = false;
                showFoodPheromone = false;
                showAlarmPheromone = false;
                showRecruitmentPheromone = true;
                showQueenPheromone = false;
                showDeathPheromone = false;
            }
            case 5 -> { // Queen Pheromone
                showHomePheromone = false;
                showFoodPheromone = false;
                showAlarmPheromone = false;
                showRecruitmentPheromone = false;
                showQueenPheromone = true;
                showDeathPheromone = false;
            }
            case 6 -> { // Death Pheromone
                showHomePheromone = false;
                showFoodPheromone = false;
                showAlarmPheromone = false;
                showRecruitmentPheromone = false;
                showQueenPheromone = false;
                showDeathPheromone = true;
            }
            default -> { // 0: All Pheromones
                showHomePheromone = true;
                showFoodPheromone = true;
                showAlarmPheromone = true;
                showRecruitmentPheromone = true;
                showQueenPheromone = true;
                showDeathPheromone = true;
            }
        }
        repaintAllViews();
    }

    private final Map<String, Image> plantTexturesCache = new HashMap<>();

    private Image getPlantTexture(String path) {
        return plantTexturesCache.computeIfAbsent(path, p -> {
            try {
                var url = getClass().getResource(p);
                if (url != null) {
                    return new Image(url.toExternalForm());
                }
            } catch (Exception ignored) {}
            return null;
        });
    }

    private double simWindSpeed = 15.0;
    private String simWindDirection = "SW";
    private String simSeason = "Summer";

    public void updateWeatherSimulationParameters(double windSpeed, String windDirection, String season) {
        this.simWindSpeed = windSpeed;
        if (windDirection != null) this.simWindDirection = windDirection;
        if (season != null) this.simSeason = season;
        repaintAllViews();
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
    private Label lblBioclimaticZoneBadge = new Label("🌳 Temperate Deciduous Forest");
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
    private float[][][] humidityGrid = new float[GRID_SIZE][GRID_SIZE][SOIL_DEPTH];  // 0.0-1.0 (3D Tensor)
    private boolean[][][] voidGrid  = new boolean[GRID_SIZE][GRID_SIZE][SOIL_DEPTH]; // cavernes
    private float[][][] rootGrid = new float[GRID_SIZE][GRID_SIZE][SOIL_DEPTH]; // 3D Root Network (0.0-1.0)
    private float[][][] tempGrid = new float[GRID_SIZE][GRID_SIZE][SOIL_DEPTH]; // Soil Temperature (°C)
    private float[][][] phGrid   = new float[GRID_SIZE][GRID_SIZE][SOIL_DEPTH]; // Soil pH (4.0-9.0, 3D Tensor)
    private boolean[][] carvedVoxelGrid = new boolean[GRID_SIZE][GRID_SIZE];
    private List<int[]> riverPath = new ArrayList<>();

    // Substrate Generation Sliders
    private Slider stratificationSlider;
    private Slider mixingRateSlider;
    private Slider baseHumiditySlider;
    private Slider voidDensitySlider;
    private Slider basePhSlider;
    private Slider rootDensitySlider;
    private CheckBox showHumidityCheck;
    private CheckBox showRootsCheck;
    private CheckBox showPhCheck;

    // Volumetric & Rendering Options
    private CheckBox showChamferedBezelCheck;
    private CheckBox showGravelInclusionsCheck;
    private Slider slicePlaneSlider;
    private CheckBox showTranslucentVolumetricModeCheck;

    // Callbacks
    private Consumer<Map<String, Object>> onGenerateCallback;

    private boolean isDirty = false;
    private boolean isUpdatingFields = false;
    private String lastSelectedPreset = null;

    public boolean isDirty() {
        return isDirty;
    }

    public void onFieldEdited() {
        if (isUpdatingFields) return;
        isDirty = true;
        if (presetsCombo != null) {
            isUpdatingFields = true;
            try {
                presetsCombo.getSelectionModel().clearSelection();
                presetsCombo.setValue("");
            } finally {
                isUpdatingFields = false;
            }
        }
    }

    public boolean promptUnsavedChanges() {
        if (!isDirty) return true;
        String currentName = lastSelectedPreset != null ? lastSelectedPreset : "";
        boolean hasCurrentPreset = !currentName.isEmpty();

        Alert alert = ThemeManager.createAlert(
            Alert.AlertType.CONFIRMATION,
            "You have unsaved changes in the World Editor.\n"
            + (hasCurrentPreset ? "Current preset: \"" + currentName + "\"" : "No preset selected.")
        );
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText("Exit World Editor?");

        ButtonType btnUpdate  = hasCurrentPreset
            ? new ButtonType("💾 Update \"" + currentName + "\"", ButtonBar.ButtonData.OK_DONE)
            : null;
        ButtonType btnSaveAs  = new ButtonType("📝 Save As...", ButtonBar.ButtonData.OTHER);
        ButtonType btnDiscard = new ButtonType("🗑 Discard", ButtonBar.ButtonData.OTHER);
        ButtonType btnCancel  = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        if (btnUpdate != null) {
            alert.getButtonTypes().setAll(btnUpdate, btnSaveAs, btnDiscard, btnCancel);
        } else {
            alert.getButtonTypes().setAll(btnSaveAs, btnDiscard, btnCancel);
        }
        Optional<ButtonType> result = alert.showAndWait();

        if (!result.isPresent() || result.get() == btnCancel) return false;
        if (result.get() == btnDiscard) { isDirty = false; return true; }
        if (btnUpdate != null && result.get() == btnUpdate) {
            presetManager.save(currentName, getConfiguration());
            isUpdatingFields = true;
            try {
                presetsCombo.getItems().setAll(presetManager.names());
                presetsCombo.setValue(currentName);
            } finally {
                isUpdatingFields = false;
            }
            lastSelectedPreset = currentName;
            isDirty = false;
            NotificationOverlay.show(this, "Preset \"" + currentName + "\" updated.", NotificationOverlay.NotificationType.SUCCESS);
            return true;
        }
        if (result.get() == btnSaveAs) {
            handleSavePreset();
            return !isDirty;
        }
        return false;
    }

    public WorldEditorPane() {
        initDefaultTerrainGrid();
        setTop(buildHeader());
        setLeft(buildConfig());
        setCenter(buildViews());
        if (presetsCombo != null && presetsCombo.getValue() != null && presetManager.contains(presetsCombo.getValue())) {
            loadConfiguration(presetManager.get(presetsCombo.getValue()));
        } else {
            repaintAllViews();
        }
        ThemeManager.getInstance().currentThemeProperty().addListener((obs, oldTheme, newTheme) -> repaintAllViews());
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

        long seed = 1337L;
        if (hydroSeedField != null) {
            try { seed = Long.parseLong(hydroSeedField.getText().trim()); } catch (Exception ignored) {}
        }
        Random rand = new Random(seed);
        int startX = 15 + rand.nextInt(GRID_SIZE - 30);
        int cx = startX, cy = 0;

        while (cy < GRID_SIZE && cx >= 0 && cx < GRID_SIZE) {
            path.add(new int[]{cx, cy});
            if (cy == GRID_SIZE - 1) break;
            cy++;
            double meanderProb = 0.35 + (rand.nextDouble() * 0.30);
            if (rand.nextDouble() < meanderProb) {
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
        generateSoilLayersSeeded(stratification, mixing, 123456L);
    }

    private byte pickSurfaceMaterialCoherent(int x, int y, int[] surfacePct) {
        if (riverCheck != null && riverCheck.isSelected() && isNearRiver(x, y, 3)) {
            double rNoise = valueNoise3D(x * 0.15, y * 0.15, 88);
            return rNoise < 0.6 ? (byte) 1 : (byte) 3;
        }
        double h = heightGrid[x][y];
        if (h > 0.72) return 3;
        if (h < 0.28 && humidityGrid[x][y][0] > 0.45) return 2;

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
        int e = earthSpinner   != null ? earthSpinner.getValue()   : 50;
        int s = sandSpinner    != null ? sandSpinner.getValue()    : 20;
        int c = claySpinner    != null ? claySpinner.getValue()    : 20;
        int st= stoneSpinner   != null ? stoneSpinner.getValue()   : 10;
        int org = organicSpinner != null ? organicSpinner.getValue() : 10;
        int total = Math.max(1, e + s + c + st + org);
        return new int[]{e * 100 / total, s * 100 / total, c * 100 / total, st * 100 / total, org * 100 / total};
    }

    private byte pickSurfaceMaterial(int[] pct, double rand01) {
        double r = rand01 * 100;
        if (r < pct[0]) return 0;
        r -= pct[0];
        if (r < pct[1]) return 1;
        r -= pct[1];
        if (r < pct[2]) return 2;
        r -= pct[2];
        if (r < pct[3]) return 3;
        return 5; // Horizon O (Litière / Matière Organique)
    }

    private void generateHumidity(double baseHumidity) {
        double wtDepth = waterTableDepthSlider != null ? waterTableDepthSlider.getValue() : 15;
        double wtFactor = 1.0 - Math.min(1.0, wtDepth / 50.0);
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                boolean nearRiver = riverCheck != null && riverCheck.isSelected() && isNearRiver(x, y, 4);
                double riverBoost = nearRiver ? 0.35 : 0.0;
                for (int d = 0; d < SOIL_DEPTH; d++) {
                    double depthRatio = (double) d / (SOIL_DEPTH - 1);
                    byte mat = soilLayers != null ? soilLayers[x][y][d] : 0;
                    double matRetention = switch (mat) {
                        case 2 -> 0.15;  // Clay retains humidity
                        case 1 -> -0.12; // Sand drains fast
                        case 0 -> 0.08;  // Earth
                        default -> 0.0;
                    };
                    double depthMoistureGain = depthRatio * 0.30 * wtFactor;
                    double noise = valueNoise3D(x * 0.15, y * 0.15, d * 0.25 + 99);
                    humidityGrid[x][y][d] = (float) Math.max(0.0, Math.min(1.0,
                            baseHumidity + depthMoistureGain + matRetention + riverBoost + (noise - 0.5) * 0.2));
                }
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

    private static class BotanicalTreeData {
        int gx, gy;
        int speciesIdx; // 0: Bambou, 1: Souche, 2: Bouleau, 3: Cactus, 4: Chêne, 5: Pin, 6: Acacia
        double ageScale;
    }

    private List<BotanicalTreeData> getBotanicalTreeInstances() {
        List<BotanicalTreeData> list = new ArrayList<>();
        if (treeCountSlider == null) return list;
        int count = (int) treeCountSlider.getValue();

        long tSeed = parseSeed(structSeedField, 555123L);
        Random rand = new Random(tSeed);

        double rWidthMm = riverWidthSlider != null ? riverWidthSlider.getValue() : 120.0;
        int rAvoid = (int) Math.max(4, Math.round(rWidthMm / 25.0 / 2.0) + 3);

        for (int i = 0; i < count; i++) {
            int gx = 10 + (int)(rand.nextDouble() * (GRID_SIZE - 20));
            int gy = 10 + (int)(rand.nextDouble() * (GRID_SIZE - 20));
            double ageScale = 0.55 + rand.nextDouble() * 0.75;
            int speciesIdx = pickBotanicalTreeSpecies(rand);

            if (riverCheck != null && riverCheck.isSelected() && isNearRiver(gx, gy, rAvoid)) continue;
            if (carvedVoxelGrid[gx][gy]) continue;

            BotanicalTreeData tree = new BotanicalTreeData();
            tree.gx = gx;
            tree.gy = gy;
            tree.ageScale = ageScale;
            tree.speciesIdx = speciesIdx;
            list.add(tree);
        }
        return list;
    }

    private void generateRoots(double globalDensity) {
        List<BotanicalTreeData> trees = getBotanicalTreeInstances();

        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                for (int d = 0; d < SOIL_DEPTH; d++) {
                    rootGrid[x][y][d] = 0.0f;

                    // 1. Graminées / Herbaceous background rootlets near surface (d < 4)
                    double bgRoot = 0.0;
                    if (d < 4) {
                        double noiseBg = valueNoise3D(x * 0.3, y * 0.3, d * 0.5 + 12);
                        bgRoot = (0.04 + noiseBg * 0.08) * (1.0 - d / 4.0) * globalDensity;
                    }

                    // 2. Tree Root Networks & Subterranean Stumps ("Souches enterrées")
                    double maxTreeRoot = 0.0;
                    for (BotanicalTreeData tree : trees) {
                        double dx = x - tree.gx;
                        double dy = y - tree.gy;
                        double distSq = dx * dx + dy * dy;
                        double dist = Math.sqrt(distSq);

                        // Species-specific root properties
                        // speciesIdx: 0: Bambou, 1: Souche, 2: Bouleau, 3: Cactus, 4: Chêne, 5: Pin, 6: Acacia
                        double baseRadius;
                        int maxDepth;
                        double stumpThickness; // Subterranean stump core radius
                        double speciesWeight;

                        switch (tree.speciesIdx) {
                            case 4: // Chêne (Oak) - Large deep taproot & thick lateral root plate
                                baseRadius = 15.0 * tree.ageScale;
                                maxDepth = 24;
                                stumpThickness = 3.6 * tree.ageScale;
                                speciesWeight = 1.0;
                                break;
                            case 5: // Pin (Pine) - Deep taproot & widespread anchor roots
                                baseRadius = 13.0 * tree.ageScale;
                                maxDepth = 22;
                                stumpThickness = 3.2 * tree.ageScale;
                                speciesWeight = 0.95;
                                break;
                            case 6: // Acacia - Spreading root network & deep taproot
                                baseRadius = 12.0 * tree.ageScale;
                                maxDepth = 20;
                                stumpThickness = 3.0 * tree.ageScale;
                                speciesWeight = 0.88;
                                break;
                            case 2: // Bouleau (Birch) - Moderate shallow root plate
                                baseRadius = 10.0 * tree.ageScale;
                                maxDepth = 15;
                                stumpThickness = 2.6 * tree.ageScale;
                                speciesWeight = 0.80;
                                break;
                            case 1: // Souche (Deadwood Stump) - Rotting subterranean stump core
                                baseRadius = 8.0 * tree.ageScale;
                                maxDepth = 14;
                                stumpThickness = 3.0 * tree.ageScale;
                                speciesWeight = 0.85;
                                break;
                            case 0: // Bambou - Shallow rhizome mesh
                                baseRadius = 6.0 * tree.ageScale;
                                maxDepth = 9;
                                stumpThickness = 1.8 * tree.ageScale;
                                speciesWeight = 0.65;
                                break;
                            case 3: // Cactus - Shallow widespread surface roots
                                baseRadius = 5.5 * tree.ageScale;
                                maxDepth = 7;
                                stumpThickness = 1.5 * tree.ageScale;
                                speciesWeight = 0.55;
                                break;
                            default:
                                baseRadius = 11.0 * tree.ageScale;
                                maxDepth = 18;
                                stumpThickness = 2.8 * tree.ageScale;
                                speciesWeight = 0.85;
                                break;
                        }

                        if (d > maxDepth) continue;

                        double rVal = 0.0;

                        // Subterranean Stump / Taproot Core ("Souche enterrée" right beneath the trunk)
                        if (dist <= stumpThickness) {
                            double coreDepthRatio = 1.0 - ((double) d / (maxDepth * 0.70));
                            if (coreDepthRatio > 0) {
                                double coreNoise = valueNoise3D(x * 0.4, y * 0.4, d * 0.5 + 77);
                                double stumpVal = (0.80 + 0.20 * coreNoise) * coreDepthRatio * speciesWeight * Math.max(0.65, globalDensity) * 1.4;
                                rVal = Math.max(rVal, stumpVal);
                            }
                        }

                        // Spreading Lateral Root Branches
                        if (dist <= baseRadius) {
                            double distRatio = 1.0 - (dist / baseRadius);
                            double depthRatio = 1.0 - ((double) d / maxDepth);
                            double noiseBranch = valueNoise3D(x * 0.22, y * 0.22, d * 0.35 + 100);
                            double lateralVal = distRatio * depthRatio * speciesWeight * globalDensity * (0.45 + noiseBranch * 0.8);
                            rVal = Math.max(rVal, lateralVal);
                        }

                        maxTreeRoot = Math.max(maxTreeRoot, rVal);
                    }

                    double totalRoot = Math.max(bgRoot, maxTreeRoot);
                    rootGrid[x][y][d] = (float) Math.max(0.0, Math.min(1.0, totalRoot));
                }
            }
        }
    }

    private void generateTemperatureGrid() {
        double surfaceTemp = 20.0;
        if (latField != null) {
            try {
                double lat = Double.parseDouble(latField.getText().trim());
                surfaceTemp = 30.0 - Math.abs(lat) * 0.35;
            } catch (Exception ignored) {}
        }

        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                for (int d = 0; d < SOIL_DEPTH; d++) {
                    double depthRatio = (double) d / (SOIL_DEPTH - 1);
                    double depthCooling = depthRatio * 4.5;
                    double moistureCooling = humidityGrid[x][y][d] * 1.5;
                    double noise = valueNoise3D(x * 0.15, y * 0.15, d * 0.3 + 200);
                    tempGrid[x][y][d] = (float) Math.max(-5.0, surfaceTemp - depthCooling - moistureCooling + (noise - 0.5) * 1.2);
                }
            }
        }
    }

    private void generatePHGrid(double basePh) {
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                for (int d = 0; d < SOIL_DEPTH; d++) {
                    byte mat = soilLayers != null ? soilLayers[x][y][d] : 0;
                    double depthRatio = (double) d / (SOIL_DEPTH - 1);
                    double phOffset = switch (mat) {
                        case 0 -> -0.3; // Humus organic slightly acidic
                        case 1 -> 0.1;  // Sand neutral
                        case 2 -> -0.4; // Clay acidic
                        case 3 -> 0.9;  // Limestone stone alkaline
                        case 4 -> 0.4;  // Gravel alkaline
                        case 5 -> -0.6; // Organic litter acidic
                        default -> 0.0;
                    };
                    if (pineLitterCheck != null && pineLitterCheck.isSelected() && d < 3) {
                        phOffset -= 0.5; // Pine needles acidify top soil
                    }
                    double depthAlkalinity = depthRatio * 0.45; // Deep bedrock tends towards alkaline
                    double noise = valueNoise3D(x * 0.1, y * 0.1, d * 0.2 + 350);
                    phGrid[x][y][d] = (float) Math.max(4.0, Math.min(9.0, basePh + phOffset + depthAlkalinity + (noise - 0.5) * 0.6));
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
        int organicPct = organicSpinner != null ? organicSpinner.getValue() : 10;
        double organicHorizonLimit = (organicPct > 0) ? Math.min(0.06, 0.01 + (organicPct / 100.0) * 0.05) : 0.0;

        double offset = (seed % 10000) * 0.1;
        double rWidthMm = riverWidthSlider != null ? riverWidthSlider.getValue() : 120.0;
        int rAvoid = (int) Math.max(4, Math.round(rWidthMm / 25.0 / 2.0) + 3);

        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                boolean isRiverNear = riverCheck != null && riverCheck.isSelected() && isNearRiver(x, y, rAvoid);
                for (int d = 0; d < SOIL_DEPTH; d++) {
                    double depthRatio = (double) d / (SOIL_DEPTH - 1);
                    double noise = valueNoise3D(x * 0.22 + offset, y * 0.22 + offset, d * 0.5 + 10 + offset);

                    // Authentic Pedological Stratigraphy (Horizons du Sol):
                    // Horizon O (0-3% depth): Litière Organique / Détritus (5) (si organicPct > 0)
                    // Horizon A (3-20% depth): Sol de Surface / Humus / Sable / Terre (selon spinners)
                    // Horizon B (20-55% depth): Argile Limoneuse (2)
                    // Horizon C (55-80% depth): Gravier & Cailloutis d'Altération (4)
                    // Horizon R (80-100% depth): Bedrock / Roche Mère Continue (3)
                    double blend = (noise - 0.5) * mixing * 1.4;
                    double effectiveDepth = Math.max(0.0, Math.min(1.0, depthRatio + blend * (1.0 - stratification * 0.65)));

                    byte mat;
                    if (effectiveDepth < organicHorizonLimit) {
                        mat = 5; // Horizon O (Litière Organique / Humus)
                    } else if (effectiveDepth < 0.20) {
                        mat = isRiverNear ? (byte) 1 : pickSurfaceMaterialCoherent(x, y, surfacePct); // Horizon A (Sol de surface: Sable/Terre)
                    } else if (effectiveDepth < 0.55) {
                        mat = 2; // Horizon B (Argile)
                    } else if (effectiveDepth < 0.80) {
                        mat = 4; // Horizon C (Gravier / Cailloutis d'altération)
                    } else {
                        mat = 3; // Horizon R (Roche Mère / Bedrock)
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
        double phVal = basePhSlider        != null ? basePhSlider.getValue()        : 6.5;
        double rDens = rootDensitySlider   != null ? rootDensitySlider.getValue()   : 40.0;

        generateHumidity(hum);
        riverPath = computeRiverPath();
        carveRiverBed();
        generateSoilLayersSeeded(strat, mix, sSeed);
        generateVoids(voids);
        generateSurfaceFlora();
        generateRoots(rDens / 100.0);
        generateTemperatureGrid();
        generatePHGrid(phVal);
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
        int crevicesCount = rockCrevicesSlider != null ? (int) rockCrevicesSlider.getValue() : 3;

        int step = 3;
        for (int x = 4; x < GRID_SIZE - 4; x += step) {
            for (int y = 4; y < GRID_SIZE - 4; y += step) {
                double r = rand.nextDouble();
                if (r < ediblePct * 0.45) {
                    if (seedGrassCheck != null && seedGrassCheck.isSelected() && rand.nextDouble() < 0.5) {
                        surfaceFloraItems.add(new SurfaceFloraItem(x, y, 0, 0.7 + rand.nextDouble() * 0.5, rand.nextDouble() * 360));
                    } else if (aphidPlantCheck != null && aphidPlantCheck.isSelected() && rand.nextDouble() < 0.35) {
                        surfaceFloraItems.add(new SurfaceFloraItem(x, y, 1, 0.8 + rand.nextDouble() * 0.4, rand.nextDouble() * 360));
                    } else if (nectarFlowersCheck != null && nectarFlowersCheck.isSelected() && rand.nextDouble() < 0.35) {
                        surfaceFloraItems.add(new SurfaceFloraItem(x, y, 2, 0.6 + rand.nextDouble() * 0.5, rand.nextDouble() * 360));
                    } else if (fungusFoliageCheck != null && fungusFoliageCheck.isSelected() && rand.nextDouble() < 0.35) {
                        surfaceFloraItems.add(new SurfaceFloraItem(x, y, 7, 0.7 + rand.nextDouble() * 0.5, rand.nextDouble() * 360));
                    }
                }
                if (mossCheck != null && mossCheck.isSelected() && humidityGrid[x][y][0] > 0.25 && rand.nextDouble() < nonEdiblePct * 0.5) {
                    surfaceFloraItems.add(new SurfaceFloraItem(x, y, 3, 0.6 + rand.nextDouble() * 0.8, rand.nextDouble() * 360));
                }
                if (fernObstacleCheck != null && fernObstacleCheck.isSelected() && rand.nextDouble() < nonEdiblePct * 0.4) {
                    surfaceFloraItems.add(new SurfaceFloraItem(x, y, 8, 0.8 + rand.nextDouble() * 0.6, rand.nextDouble() * 360));
                }
                if (pineLitterCheck != null && pineLitterCheck.isSelected() && rand.nextDouble() < litterPct * 0.5) {
                    surfaceFloraItems.add(new SurfaceFloraItem(x, y, 4, 0.5 + rand.nextDouble() * 0.7, rand.nextDouble() * 360));
                }
                if (rand.nextDouble() < debrisPct * 0.4) {
                    int debrisType = rand.nextBoolean() ? 5 : 6;
                    surfaceFloraItems.add(new SurfaceFloraItem(x, y, debrisType, 0.5 + rand.nextDouble() * 0.6, rand.nextDouble() * 360));
                }
            }
        }

        // Generate Rock Crevices (Fissures / Rentrées Rocheuses)
        if (crevicesCount > 0) {
            Random creviceRand = new Random(seed ^ 0x9e3779b9L);
            for (int i = 0; i < crevicesCount * 4; i++) {
                int cx = 6 + creviceRand.nextInt(GRID_SIZE - 12);
                int cy = 6 + creviceRand.nextInt(GRID_SIZE - 12);
                surfaceFloraItems.add(new SurfaceFloraItem(cx, cy, 9, 0.8 + creviceRand.nextDouble() * 0.7, creviceRand.nextDouble() * 360));
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
    private double bilerp(double c00,double c10,double c01,double c11,double u,double v){return lrp(lrp(c00,c10,u),lrp(c01,c11,u),v);}

    private VBox headerBox;
    private HBox presetRow;
    private Label headerTitleLabel;
    private Label headerSubtitleLabel;

    private VBox buildHeader() {
        headerBox = new VBox(8);
        headerBox.setPadding(new Insets(8, 12, 6, 12));
        headerBox.setStyle("-fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 0 0 1 0;");

        presetRow = new HBox(10);
        presetRow.setAlignment(Pos.CENTER_LEFT);

        headerTitleLabel = new Label("World Editor");
        headerTitleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        headerTitleLabel.getStyleClass().add("header-title-large");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label lblPreset = new Label("Preset :");
        lblPreset.setStyle("-fx-font-weight: bold;");
        lblPreset.setGraphic(new FontIcon(Feather.SLIDERS));

        presetsCombo = new ComboBox<>();
        presetsCombo.setEditable(true);
        presetsCombo.setPromptText("Select a preset...");
        presetsCombo.getItems().setAll(presetManager.names());
        if (!presetsCombo.getItems().isEmpty()) {
            presetsCombo.getSelectionModel().selectFirst();
        }
        presetsCombo.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.presets.combo.tt"));
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
        bSave.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.presets.save.tt"));
        bSave.setOnAction(e -> handleSavePreset());

        Button bDelete = new Button(I18nManager.getInstance().get("common.btn.delete"));
        bDelete.setGraphic(new FontIcon(Feather.TRASH_2));
        bDelete.getStyleClass().add("btn-danger");
        bDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold;");
        bDelete.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.presets.delete.tt"));
        bDelete.setOnAction(e -> handleDeletePreset());

        Button bExport = new Button(I18nManager.getInstance().get("common.btn.export"));
        bExport.setGraphic(new FontIcon(Feather.DOWNLOAD));
        bExport.getStyleClass().add("btn-secondary");
        bExport.setOnAction(e -> doExport());

        Button bNewPreset = new Button("➕ New Preset");
        bNewPreset.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold;");
        bNewPreset.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.presets.new.tt"));
        bNewPreset.setOnAction(e -> handleSavePreset());

        presetRow.getChildren().addAll(headerTitleLabel, sp, lblPreset, presetsCombo, bNewPreset, bSave, bDelete, new Separator(Orientation.VERTICAL), bExport);

        headerSubtitleLabel = new Label("Relief generation, substrate layers, flora cover, hydrography, 3D sculpting & voxel deformation (0.1-1.0mm)");
        headerSubtitleLabel.setStyle("-fx-font-size: 11px;");
        headerSubtitleLabel.getStyleClass().add("header-subtitle");

        headerBox.getChildren().addAll(presetRow, headerSubtitleLabel);
        return headerBox;
    }

    public void setHideHeaderPresets(boolean hide) {
        if (presetRow != null) {
            presetsCombo.setVisible(!hide);
            presetsCombo.setManaged(!hide);
            presetRow.getChildren().forEach(node -> {
                if (node != headerTitleLabel) {
                    node.setVisible(!hide);
                    node.setManaged(!hide);
                }
            });
        }
        if (hide && headerTitleLabel != null) {
            headerTitleLabel.textProperty().bind(I18nManager.getInstance().createStringBinding("world.view_title"));
            if (headerSubtitleLabel != null) {
                headerSubtitleLabel.textProperty().bind(I18nManager.getInstance().createStringBinding("world.view_subtitle"));
            }
        }
    }


    private org.swarmforge.core.simulation.Simulation activeSimulation = null;
    private org.swarmforge.core.domain.Individual followedAnt = null;
    private boolean isFollowAntCameraEnabled = false;
    private final java.util.LinkedList<double[]> antTrailHistory = new java.util.LinkedList<>();
    private static final int MAX_TRAIL_LENGTH = 100;
    private TrackedAntPane trackedAntPane;

    public TrackedAntPane getTrackedAntPane() {
        return trackedAntPane;
    }

    public void updateTrackedAntTelemetry() {
        if (trackedAntPane != null && followedAnt != null) {
            trackedAntPane.updateAnt(followedAnt, isFollowAntCameraEnabled || followedAnt != null);
        }
    }

    public void setSimulation(org.swarmforge.core.simulation.Simulation sim) {
        this.activeSimulation = sim;
        repaintAllViews();
    }

    public void setFollowedAnt(org.swarmforge.core.domain.Individual ant) {
        this.followedAnt = ant;
        this.antTrailHistory.clear();
        if (trackedAntPane != null) {
            trackedAntPane.updateAnt(ant, ant != null);
        }
        repaintAllViews();
    }

    public void setFollowedAntById(String query) {
        if (query == null || query.trim().isEmpty()) return;
        String q = query.trim().toLowerCase();
        org.swarmforge.core.domain.Individual match = null;

        if (activeSimulation != null) {
            int indexToFind = -1;
            if (q.startsWith("ant_")) {
                try { indexToFind = Integer.parseInt(q.substring(4)) - 1; } catch (Exception ignored) {}
            } else {
                try { indexToFind = Integer.parseInt(q) - 1; } catch (Exception ignored) {}
            }

            int currentIndex = 0;
            for (org.swarmforge.core.domain.Colony colony : activeSimulation.getColonies()) {
                for (org.swarmforge.core.domain.Individual ind : colony.getLivingIndividuals()) {
                    String fullId = ind.getId() != null ? ind.getId().toString().toLowerCase() : "";
                    String caste = ind.getCaste() != null ? ind.getCaste().name().toLowerCase() : "";

                    if (fullId.equalsIgnoreCase(q) || fullId.startsWith(q) || fullId.contains(q)) {
                        match = ind;
                        break;
                    }
                    if (q.equalsIgnoreCase(caste) || q.contains(caste)) {
                        match = ind;
                        break;
                    }
                    if (indexToFind >= 0 && currentIndex == indexToFind) {
                        match = ind;
                        break;
                    }
                    currentIndex++;
                }
                if (match != null) break;
            }
        }

        if (match != null) {
            setFollowedAnt(match);
            setFollowAntCameraEnabled(true);
            if (trackedAntPane != null) {
                trackedAntPane.updateAnt(match, true);
            }
        } else {
            if (trackedAntPane != null) {
                trackedAntPane.setSearchStatusError("Fourmi introuvable : '" + query + "'");
            }
        }
    }

    public org.swarmforge.core.domain.Individual getFollowedAnt() {
        return followedAnt;
    }

    public void setFollowAntCameraEnabled(boolean enabled) {
        this.isFollowAntCameraEnabled = enabled;
        repaintAllViews();
    }

    public boolean isFollowAntCameraEnabled() {
        return isFollowAntCameraEnabled;
    }

    public void setShowTrees(boolean visible) {
        if (showVegetationCheck != null) showVegetationCheck.setSelected(visible);
        repaintAllViews();
    }

    public void setShow3DSkirt(boolean visible) {
        if (showSubstrateStratigraphyCheck != null) showSubstrateStratigraphyCheck.setSelected(visible);
        repaintAllViews();
    }

    public void setVolumetricScannerEnabled(boolean enabled) {
        repaintAllViews();
    }

    public void setSlicePlane(double sliceValue) {
        if (slicePlaneSlider != null) slicePlaneSlider.setValue(sliceValue);
        repaintAllViews();
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
                String pKey = "Tropical Rainforest (Manaus, BR)";
                if (presetManager.contains(pKey)) {
                    presetsCombo.getSelectionModel().select(pKey);
                    loadConfiguration(presetManager.get(pKey));
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
                String pKey = "Arid Desert (Erg Chebbi, MA)";
                if (presetManager.contains(pKey)) {
                    presetsCombo.getSelectionModel().select(pKey);
                    loadConfiguration(presetManager.get(pKey));
                } else {
                    surfaceSizeSlider.setValue(25.0); depthSlider.setValue(3.0); baseHumiditySlider.setValue(0.12);
                    sandSpinner.getValueFactory().setValue(80); stoneSpinner.getValueFactory().setValue(15);
                    comboTreeSpecies.getSelectionModel().select(3); // Cactus
                    riverCheck.setSelected(false);
                }
            }
            case 4 -> { // Formica rufa (Taiga Boreal Forest)
                String pKey = "Permafrost Tundra (Svalbard, NO)";
                if (presetManager.contains(pKey)) {
                    presetsCombo.getSelectionModel().select(pKey);
                    loadConfiguration(presetManager.get(pKey));
                } else {
                    surfaceSizeSlider.setValue(25.0); depthSlider.setValue(3.0); baseHumiditySlider.setValue(0.55);
                    earthSpinner.getValueFactory().setValue(40); stoneSpinner.getValueFactory().setValue(25);
                    comboTreeSpecies.getSelectionModel().select(1); // Pinède
                    pineLitterCheck.setSelected(true); mossCheck.setSelected(true);
                }
            }
            case 5 -> { // Lasius niger (Temperate Forest)
                String pKey = "Temperate Deciduous (Fontainebleau, FR)";
                if (presetManager.contains(pKey)) {
                    presetsCombo.getSelectionModel().select(pKey);
                    loadConfiguration(presetManager.get(pKey));
                } else {
                    surfaceSizeSlider.setValue(25.0); depthSlider.setValue(3.0); baseHumiditySlider.setValue(0.45);
                    earthSpinner.getValueFactory().setValue(50); sandSpinner.getValueFactory().setValue(20);
                    comboTreeSpecies.getSelectionModel().select(0); // Chênes
                    aphidPlantCheck.setSelected(true); nectarFlowersCheck.setSelected(true);
                }
            }
            case 6 -> { // Messor barbarus (Semi-Arid Steppe)
                String pKey = "Mediterranean Shrubland (Corsica, FR)";
                if (presetManager.contains(pKey)) {
                    presetsCombo.getSelectionModel().select(pKey);
                    loadConfiguration(presetManager.get(pKey));
                } else {
                    surfaceSizeSlider.setValue(25.0); depthSlider.setValue(3.0); baseHumiditySlider.setValue(0.30);
                    sandSpinner.getValueFactory().setValue(35); earthSpinner.getValueFactory().setValue(45);
                    seedGrassCheck.setSelected(true);
                }
            }
            case 7 -> { // Pseudomyrmex gracilis (Acacia Savanna)
                String pKey = "Acacia Savanna (Serengeti, TZ)";
                if (presetManager.contains(pKey)) {
                    presetsCombo.getSelectionModel().select(pKey);
                    loadConfiguration(presetManager.get(pKey));
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
            lblAdaptStatus.setText("🟢 Ecosystem successfully adapted to: " + speciesAdaptCombo.getValue());
        }
    }

    private void handleSavePreset() {
        String defaultName = (presetsCombo.getEditor() != null && !presetsCombo.getEditor().getText().isBlank())
                ? presetsCombo.getEditor().getText().trim()
                : (presetsCombo.getValue() != null ? presetsCombo.getValue() : "Nouveau Preset Monde");
        TextInputDialog dialog = org.swarmforge.client.util.ThemeManager.createTextInputDialog(defaultName);
        dialog.setTitle("Enregistrer le Preset Monde");
        dialog.setHeaderText("Saisissez un nom pour ce preset de monde :");
        dialog.setContentText("Nom :");
        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                String cleanName = name.trim();
                if (presetManager.contains(cleanName)) {
                    Alert confirmAlert = org.swarmforge.client.util.ThemeManager.createAlert(
                        Alert.AlertType.CONFIRMATION,
                        "World preset '" + cleanName + "' already exists.\n\nDo you want to overwrite it with current configuration?"
                    );
                    confirmAlert.setTitle("Overwrite Existing Preset");
                    confirmAlert.setHeaderText("Overwrite Confirmation");
                    java.util.Optional<ButtonType> res = confirmAlert.showAndWait();
                    if (res.isEmpty() || res.get() != ButtonType.OK) {
                        return;
                    }
                }
                presetManager.save(cleanName, getConfiguration());
                presetsCombo.getItems().setAll(presetManager.names());
                presetsCombo.getSelectionModel().select(cleanName);
                lastSelectedPreset = cleanName;
                isDirty = false;
                NotificationOverlay.show(this, "World preset saved: " + cleanName, NotificationOverlay.NotificationType.SUCCESS);
            }
        });
    }

    private void handleDeletePreset() {
        String selected = presetsCombo.getValue();
        if (selected == null || selected.isEmpty()) return;

        Alert confirmAlert = org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer le preset '" + selected + "' ?");
        confirmAlert.setTitle("Supprimer le Preset");
        confirmAlert.setHeaderText("Supprimer le Preset Monde");

        confirmAlert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                presetManager.delete(selected);
                presetsCombo.getItems().setAll(presetManager.names());
                if (!presetsCombo.getItems().isEmpty()) {
                    presetsCombo.getSelectionModel().selectFirst();
                } else {
                    presetsCombo.getSelectionModel().clearSelection();
                }
                org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.INFORMATION, "World preset deleted.").show();
            }
        });
    }

    private VBox makeCard(String titleIcon, VBox content) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(10));
        card.getStyleClass().add("card-pane");
        Label lblHeader = new Label(titleIcon);
        lblHeader.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        lblHeader.getStyleClass().add("accent-title");
        card.getChildren().addAll(lblHeader, new Separator(), content);
        return card;
    }

    private ScrollPane buildConfig() {
        VBox cfg = new VBox(12);
        cfg.setPadding(new Insets(10));
        cfg.setPrefWidth(420);
        cfg.setMinWidth(400);
        cfg.setStyle("-fx-background-color: transparent;");

        VBox cardSource = makeCard("🌐 Geographic Data & Location", buildTerrainSourceBlock());
        VBox cardScale  = makeCard("📐 Scale & Voxel Dimensions", buildScaleBlock());
        VBox cardRelief = makeCard("⛰️ Relief, Topography & Slope", buildReliefBlock());
        VBox cardSoil   = makeCard("🗻 Substrate, Soil & Stratification", buildSoilBlock());
        VBox cardHydro  = makeCard("💧 Hydrography & Watercourses", buildHydroBlock());
        VBox cardFlora  = makeCard("🌿 Vegetation & Flora Cover", buildFloraBlock());
        VBox cardStruct = makeCard("🪵 Host Structures & Trees", buildStructBlock());
        VBox cardDiag   = makeCard("🧪 Ecological Attraction Diagnostic", buildDiagBlock());
        VBox cardAdapt  = buildAdaptBlock();

        cfg.getChildren().addAll(cardSource, cardScale, cardRelief, cardSoil, cardHydro, cardFlora, cardStruct, cardDiag, cardAdapt);

        ScrollPane sc = new ScrollPane(cfg);
        sc.setFitToWidth(true);
        sc.setPrefWidth(435);
        sc.setMinWidth(415);
        return sc;
    }

    private VBox buildTerrainSourceBlock() {
        cityNameField = new TextField("Serengeti, TZ");
        cityNameField.setPromptText("Ex: Serengeti, Paris, Manaus...");
        cityNameField.setPrefWidth(160);
        cityNameField.setOnAction(e -> fetchCityCoordinates(cityNameField.getText()));

        latField = new TextField("-2.3333");
        latField.setPrefWidth(70);
        lonField = new TextField("34.8333");
        lonField.setPrefWidth(70);

        Button btnSyncGeo = new Button("🌐 Synchronize GIS");
        btnSyncGeo.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
        btnSyncGeo.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.geo.sync.tt"));

        geoStatusLabel = new Label("ℹ️ Enter city name OR Lat/Lon then click Synchronize.");
        geoStatusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8; -fx-wrap-text: true;");

        btnSyncGeo.setOnAction(e -> {
            String cName = cityNameField.getText().trim();
            if (!cName.isEmpty()) {
                fetchCityCoordinates(cName);
            } else {
                try {
                    double lat = Double.parseDouble(latField.getText().trim());
                    double lon = Double.parseDouble(lonField.getText().trim());
                    geoStatusLabel.setText("🟢 GIS applied for Lat: " + String.format(java.util.Locale.US, "%.4f", lat) + "°, Lon: " + String.format(java.util.Locale.US, "%.4f", lon) + "°");
                    geoStatusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #4ade80; -fx-wrap-text: true;");
                    applyBioclimaticAdaptation(lat, lon);
                    regenerateAndRepaint();
                } catch (Exception ex) {
                    geoStatusLabel.setText("⚠️ Invalid GPS coordinates.");
                    geoStatusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #ef4444;");
                }
            }
        });

        HBox cityRow = new HBox(6, new Label() {{ textProperty().bind(I18nManager.getInstance().createStringBinding("world.geo.city")); setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;"); }}, cityNameField);
        cityRow.setAlignment(Pos.CENTER_LEFT);

        HBox gpsRow = new HBox(6,
            new Label() {{ textProperty().bind(I18nManager.getInstance().createStringBinding("world.geo.lat")); setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;"); }}, latField,
            new Label() {{ textProperty().bind(I18nManager.getInstance().createStringBinding("world.geo.lon")); setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;"); }}, lonField,
            btnSyncGeo
        );
        gpsRow.setAlignment(Pos.CENTER_LEFT);

        return new VBox(8, cityRow, gpsRow, geoStatusLabel);
    }

    private void fetchCityCoordinates(String cityQuery) {
        if (cityQuery == null || cityQuery.isBlank()) return;
        geoStatusLabel.setText("⏳ Fetching coordinates for \"" + cityQuery + "\"...");
        geoStatusLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 10px;");

        new Thread(() -> {
            try {
                String geoUrlStr = "https://geocoding-api.open-meteo.com/v1/search?name="
                        + java.net.URLEncoder.encode(cityQuery, java.nio.charset.StandardCharsets.UTF_8)
                        + "&count=1&language=en";

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
                    geoStatusLabel.setText("⚠️ City \"" + cityQuery + "\" not found.");
                    geoStatusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 10px;");
                });
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    geoStatusLabel.setText("⚠️ Geocoding error : " + ex.getMessage());
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
        scaleSeedField = new TextField("384729");
        scaleSeedField.setPrefWidth(90);
        scaleSeedField.setOnAction(e -> regenerateAndRepaint());
        scaleSeedField.textProperty().addListener((o, a, b) -> regenerateAndRepaint());

        Button btnRandomScaleSeed = new Button("🎲 Seed");
        btnRandomScaleSeed.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
        btnRandomScaleSeed.setOnAction(e -> {
            scaleSeedField.setText(String.valueOf(new Random().nextLong(100000, 9999999)));
            regenerateAndRepaint();
        });

        HBox scaleSeedRow = new HBox(6, new Label() {{ textProperty().bind(I18nManager.getInstance().createStringBinding("world.seed.scale")); setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;"); }}, scaleSeedField, btnRandomScaleSeed);
        scaleSeedRow.setAlignment(Pos.CENTER_LEFT);

        surfaceSizeSlider = mkSlider(0.5, 100.0, 25.0);
        surfaceSizeSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.dim.side_length.tt"));
        depthSlider = mkSlider(0.2, 10.0, 3.0);
        depthSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.dim.depth.tt"));
        resolutionSlider = mkSlider(0.1, 1.0, 0.5);
        resolutionSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.dim.resolution.tt"));

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

        lblVoxelMemoryEstimate = new Label("📊 Voxel Estimate : ~0.5M voxels (32MB)");
        lblVoxelMemoryEstimate.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
        lblVoxelMemoryEstimate.getStyleClass().add("accent-title");

        updateVoxelMemoryEstimate();

        Label lblSideLength = new Label(); lblSideLength.textProperty().bind(I18nManager.getInstance().createStringBinding("world.dim.side_length"));
        Label lblDepth = new Label(); lblDepth.textProperty().bind(I18nManager.getInstance().createStringBinding("world.dim.depth"));
        Label lblRes = new Label(); lblRes.textProperty().bind(I18nManager.getInstance().createStringBinding("world.dim.resolution"));

        return new VBox(8,
                lblSideLength, sv(surfaceSizeSlider, "m"),
                lblDepth, sv(depthSlider, "m"),
                lblRes, sv(resolutionSlider, "mm"),
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
            lblVoxelMemoryEstimate.setText(String.format(java.util.Locale.US, "⚡ SVO Multi-LOD Active : ~%.1fM effective voxels (%.1f MB RAM)", lodVoxels / 1_000_000.0, lodMb));
            lblVoxelMemoryEstimate.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #16a34a;");
        } else {
            double rawMb = (GRID_SIZE * GRID_SIZE * SOIL_DEPTH * 4.0) / (1024.0 * 1024.0);
            lblVoxelMemoryEstimate.setText(String.format(java.util.Locale.US, "📊 Voxel Macro 128x128x32 : %.1f MB RAM", rawMb));
            lblVoxelMemoryEstimate.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
            lblVoxelMemoryEstimate.getStyleClass().add("accent-title");
        }
    }

    private VBox buildReliefBlock() {
        reliefSeedField = new TextField("774829");
        reliefSeedField.setPrefWidth(90);
        reliefSeedField.setOnAction(e -> regenerateAndRepaint());
        reliefSeedField.textProperty().addListener((o, a, b) -> regenerateAndRepaint());

        Button btnRandomRelief = new Button("🎲 Seed");
        btnRandomRelief.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
        btnRandomRelief.setOnAction(e -> {
            reliefSeedField.setText(String.valueOf(new Random().nextLong(100000, 9999999)));
            regenerateAndRepaint();
        });

        HBox seedBox = new HBox(6, new Label() {{ textProperty().bind(I18nManager.getInstance().createStringBinding("world.seed.relief")); setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;"); }}, reliefSeedField, btnRandomRelief);
        seedBox.setAlignment(Pos.CENTER_LEFT);

        roughnessSlider = mkSlider(0.0, 1.0, 0.45);
        roughnessSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.relief.perlin.tt"));
        compactionSlider = mkSlider(10.0, 100.0, 65.0);
        compactionSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.soil.compaction.tt"));

        roughnessSlider.valueProperty().addListener((o, a, b) -> regenerateAndRepaint());
        compactionSlider.valueProperty().addListener((o, a, b) -> regenerateAndRepaint());

        slopeAngleSlider = mkSlider(0.0, 200.0, 0.0);
        slopeAngleSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.relief.slope_angle.tt"));
        slopeAngleSlider.valueProperty().addListener((o, a, b) -> regenerateAndRepaint());

        slopeDirectionCombo = new ComboBox<>();
        slopeDirectionCombo.getItems().addAll("East ➔ West", "North ➔ South", "South ➔ North", "West ➔ East");
        slopeDirectionCombo.getSelectionModel().selectFirst();
        slopeDirectionCombo.valueProperty().addListener((o, a, b) -> regenerateAndRepaint());

        HBox slopeRow = new HBox(6, new Label() {{ textProperty().bind(I18nManager.getInstance().createStringBinding("world.relief.slope_dir")); setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;"); }}, slopeDirectionCombo);
        slopeRow.setAlignment(Pos.CENTER_LEFT);

        VBox sculptSubBlock = buildSculptBlock();

        Label lblReliefPerlin = new Label(); lblReliefPerlin.textProperty().bind(I18nManager.getInstance().createStringBinding("world.relief.perlin"));
        Label lblCompaction = new Label(); lblCompaction.textProperty().bind(I18nManager.getInstance().createStringBinding("world.soil.compaction"));
        Label lblSlopeAngle = new Label(); lblSlopeAngle.textProperty().bind(I18nManager.getInstance().createStringBinding("world.relief.slope_angle"));

        return new VBox(8,
                seedBox,
                lblReliefPerlin, sv(roughnessSlider, ""),
                lblCompaction, sv(compactionSlider, "%"),
                lblSlopeAngle, sv(slopeAngleSlider, "%"),
                slopeRow,
                new Separator(),
                sculptSubBlock
        );
    }

    private VBox buildSoilBlock() {
        soilSeedField = new TextField("123456");
        soilSeedField.setPrefWidth(90);
        soilSeedField.setOnAction(e -> regenerateAndRepaint());
        soilSeedField.textProperty().addListener((o, a, b) -> regenerateAndRepaint());

        Button btnRandomSoil = new Button("🎲 Seed");
        btnRandomSoil.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
        btnRandomSoil.setOnAction(e -> {
            soilSeedField.setText(String.valueOf(new Random().nextLong(100000, 9999999)));
            regenerateAndRepaint();
        });

        HBox seedBox = new HBox(6, new Label() {{ textProperty().bind(I18nManager.getInstance().createStringBinding("world.seed.soil")); setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;"); }}, soilSeedField, btnRandomSoil);
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

        Label lblHumus = new Label(); lblHumus.textProperty().bind(I18nManager.getInstance().createStringBinding("world.sub.humus_pct"));
        Label lblSand = new Label(); lblSand.textProperty().bind(I18nManager.getInstance().createStringBinding("world.sub.sand_pct"));
        Label lblClay = new Label(); lblClay.textProperty().bind(I18nManager.getInstance().createStringBinding("world.sub.clay_pct"));
        Label lblRock = new Label(); lblRock.textProperty().bind(I18nManager.getInstance().createStringBinding("world.sub.rock_pct"));
        Label lblLitter = new Label(); lblLitter.textProperty().bind(I18nManager.getInstance().createStringBinding("world.sub.litter_pct"));

        grid.add(lblHumus, 0, 0); grid.add(earthSpinner, 1, 0);
        grid.add(lblSand, 0, 1); grid.add(sandSpinner, 1, 1);
        grid.add(lblClay, 0, 2); grid.add(claySpinner, 1, 2);
        grid.add(lblRock, 0, 3); grid.add(stoneSpinner, 1, 3);
        grid.add(lblLitter, 0, 4); grid.add(organicSpinner, 1, 4);

        stratificationSlider = mkSlider(0.0, 1.0, 0.7);
        stratificationSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.soil.stratification.tt"));
        mixingRateSlider     = mkSlider(0.0, 1.0, 0.3);
        mixingRateSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.soil.mixing.tt"));
        baseHumiditySlider   = mkSlider(0.0, 1.0, 0.35);
        baseHumiditySlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.soil.base_humidity.tt"));
        voidDensitySlider    = mkSlider(0.0, 0.3, 0.08);
        voidDensitySlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.soil.void_density.tt"));
        basePhSlider         = mkSlider(4.0, 9.0, 6.5);
        basePhSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.soil.base_ph.tt"));
        rootDensitySlider    = mkSlider(0.0, 100.0, 40.0);
        rootDensitySlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.soil.root_density.tt"));

        stratificationSlider.valueProperty().addListener((o, a, b) -> regenerateAndRepaint());
        mixingRateSlider.valueProperty().addListener((o, a, b) -> regenerateAndRepaint());
        baseHumiditySlider.valueProperty().addListener((o, a, b) -> regenerateAndRepaint());
        voidDensitySlider.valueProperty().addListener((o, a, b) -> regenerateAndRepaint());
        basePhSlider.valueProperty().addListener((o, a, b) -> regenerateAndRepaint());
        rootDensitySlider.valueProperty().addListener((o, a, b) -> regenerateAndRepaint());

        Label lblStrat = new Label(); lblStrat.textProperty().bind(I18nManager.getInstance().createStringBinding("world.soil.stratification"));
        Label lblMixing = new Label(); lblMixing.textProperty().bind(I18nManager.getInstance().createStringBinding("world.soil.mixing"));
        Label lblBaseHum = new Label(); lblBaseHum.textProperty().bind(I18nManager.getInstance().createStringBinding("world.soil.base_humidity"));
        Label lblVoid = new Label(); lblVoid.textProperty().bind(I18nManager.getInstance().createStringBinding("world.soil.void_density"));
        Label lblBasePh = new Label(); lblBasePh.textProperty().bind(I18nManager.getInstance().createStringBinding("world.soil.base_ph"));
        Label lblRootDens = new Label(); lblRootDens.textProperty().bind(I18nManager.getInstance().createStringBinding("world.soil.root_density"));

        return new VBox(8,
                seedBox,
                grid,
                new Separator(),
                lblStrat, sv(stratificationSlider, ""),
                lblMixing, sv(mixingRateSlider, ""),
                lblBaseHum, sv(baseHumiditySlider, ""),
                lblVoid, sv(voidDensitySlider, ""),
                lblBasePh, sv(basePhSlider, " pH"),
                lblRootDens, sv(rootDensitySlider, "%")
        );
    }

    private VBox buildFloraBlock() {
        floraSeedField = new TextField("774829");
        floraSeedField.setPrefWidth(95);
        floraSeedField.setOnAction(e -> regenerateAndRepaint());
        floraSeedField.textProperty().addListener((o, a, b) -> regenerateAndRepaint());

        Button btnRandomizeFloraSeed = new Button("🎲 Seed");
        btnRandomizeFloraSeed.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
        btnRandomizeFloraSeed.setOnAction(e -> {
            long newSeed = new Random().nextLong(100000, 9999999);
            floraSeedField.setText(String.valueOf(newSeed));
            regenerateAndRepaint();
        });

        HBox seedBox = new HBox(6,
            new Label() {{ textProperty().bind(I18nManager.getInstance().createStringBinding("world.seed.flora")); setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;"); }},
            floraSeedField, btnRandomizeFloraSeed
        );
        seedBox.setAlignment(Pos.CENTER_LEFT);

        edibleDensitySlider = mkSlider(0, 100, 40);
        edibleDensitySlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.flora.edible_density.tt"));
        nonEdibleDensitySlider = mkSlider(0, 100, 60);
        nonEdibleDensitySlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.flora.nonedible_density.tt"));
        leafLitterSlider = mkSlider(0, 100, 50);
        leafLitterSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.flora.leaf_litter.tt"));
        twigDebrisSlider = mkSlider(0, 100, 40);
        twigDebrisSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.flora.twig_debris.tt"));
        addFloraLsn(edibleDensitySlider, nonEdibleDensitySlider, leafLitterSlider, twigDebrisSlider);

        aphidPlantCheck    = new CheckBox("🟢 Cirsium / Vicia (Aphid Host)"); aphidPlantCheck.setSelected(true);
        nectarFlowersCheck = new CheckBox("🌸 Nectar Flowers");                  nectarFlowersCheck.setSelected(true);
        seedGrassCheck     = new CheckBox("🌾 Grasses & Caryopses");            seedGrassCheck.setSelected(true);
        fungusFoliageCheck = new CheckBox("🍃 Leafcutter Foliage (Atta)");      fungusFoliageCheck.setSelected(false);
        mossCheck          = new CheckBox("🟢 Polytrichum Moss Cover");          mossCheck.setSelected(true);
        pineLitterCheck    = new CheckBox("🍂 Pine Needle Litter Layer");       pineLitterCheck.setSelected(true);
        fernObstacleCheck  = new CheckBox("🌿 Fern Understory Obstacles");       fernObstacleCheck.setSelected(true);

        addFloraBoolLsn(aphidPlantCheck, nectarFlowersCheck, seedGrassCheck, fungusFoliageCheck,
                       mossCheck, pineLitterCheck, fernObstacleCheck);

        Label lblEdibleHeader = new Label(); lblEdibleHeader.textProperty().bind(I18nManager.getInstance().createStringBinding("world.flora.edible_header"));
        Label lblEdibleDens = new Label(); lblEdibleDens.textProperty().bind(I18nManager.getInstance().createStringBinding("world.flora.edible_density"));
        Label lblCoverHeader = new Label(); lblCoverHeader.textProperty().bind(I18nManager.getInstance().createStringBinding("world.flora.cover_header"));
        Label lblLeafLitter = new Label(); lblLeafLitter.textProperty().bind(I18nManager.getInstance().createStringBinding("world.flora.leaf_litter"));
        Label lblTwigDebris = new Label(); lblTwigDebris.textProperty().bind(I18nManager.getInstance().createStringBinding("world.flora.twig_debris"));
        Label lblNonEdibleDens = new Label(); lblNonEdibleDens.textProperty().bind(I18nManager.getInstance().createStringBinding("world.flora.nonedible_density"));

        return new VBox(6,
                seedBox,
                new Separator(),
                lblEdibleHeader,
                lblEdibleDens, sv(edibleDensitySlider, "%"),
                aphidPlantCheck, nectarFlowersCheck, seedGrassCheck, fungusFoliageCheck,
                new Separator(),
                lblCoverHeader,
                lblLeafLitter, sv(leafLitterSlider, "%"),
                lblTwigDebris, sv(twigDebrisSlider, "%"),
                lblNonEdibleDens, sv(nonEdibleDensitySlider, "%"),
                mossCheck, pineLitterCheck, fernObstacleCheck
        );
    }

    private VBox buildHydroBlock() {
        hydroSeedField = new TextField("987654");
        hydroSeedField.setPrefWidth(90);
        hydroSeedField.setOnAction(e -> regenerateAndRepaint());
        hydroSeedField.textProperty().addListener((o, a, b) -> regenerateAndRepaint());

        Button btnRandomHydro = new Button("🎲 Seed");
        btnRandomHydro.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
        btnRandomHydro.setOnAction(e -> {
            hydroSeedField.setText(String.valueOf(new Random().nextLong(100000, 9999999)));
            regenerateAndRepaint();
        });

        HBox seedBox = new HBox(6, new Label() {{ textProperty().bind(I18nManager.getInstance().createStringBinding("world.seed.hydro")); setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;"); }}, hydroSeedField, btnRandomHydro);
        seedBox.setAlignment(Pos.CENTER_LEFT);

        riverCheck = new CheckBox(I18nManager.getInstance().get("world.river.enable")); riverCheck.setSelected(true);
        riverWidthSlider = mkSlider(30, 500, 120);
        riverWidthSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.river.width.tt"));
        riverVelocitySlider = mkSlider(0.0, 1.5, 0.3);
        riverVelocitySlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.river.speed.tt"));
        staticPoolsSlider = mkSlider(0, 5, 2);
        staticPoolsSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.river.ponds.tt"));
        waterTableDepthSlider = mkSlider(5, 500, 50); // Max 500 cm = 5 mètres
        waterTableDepthSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.river.watertable.tt"));
        addLsn(riverWidthSlider, riverVelocitySlider, staticPoolsSlider, waterTableDepthSlider);
        riverCheck.setOnAction(e -> regenerateAndRepaint());

        Label hydroHint = new Label();
        hydroHint.textProperty().bind(I18nManager.getInstance().createStringBinding("world.hydro.planar_hint"));
        hydroHint.setStyle("-fx-font-size: 10px; -fx-text-fill: #38bdf8; -fx-wrap-text: true;");

        Label lblRiverWidth = new Label(); lblRiverWidth.textProperty().bind(I18nManager.getInstance().createStringBinding("world.river.width"));
        Label lblRiverSpeed = new Label(); lblRiverSpeed.textProperty().bind(I18nManager.getInstance().createStringBinding("world.river.speed"));
        Label lblRiverPonds = new Label(); lblRiverPonds.textProperty().bind(I18nManager.getInstance().createStringBinding("world.river.ponds"));
        Label lblWaterTable = new Label(); lblWaterTable.textProperty().bind(I18nManager.getInstance().createStringBinding("world.river.watertable"));

        return new VBox(8,
                seedBox,
                riverCheck,
                lblRiverWidth, sv(riverWidthSlider, "mm"),
                lblRiverSpeed, sv(riverVelocitySlider, "m/s"),
                new Separator(),
                lblRiverPonds, sv(staticPoolsSlider, ""),
                lblWaterTable, sv(waterTableDepthSlider, "cm"),
                hydroHint
        );
    }

    private VBox buildStructBlock() {
        structSeedField = new TextField("555123");
        structSeedField.setPrefWidth(90);
        structSeedField.setOnAction(e -> regenerateAndRepaint());
        structSeedField.textProperty().addListener((o, a, b) -> regenerateAndRepaint());

        Button btnRandomStruct = new Button("🎲 Seed");
        btnRandomStruct.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
        btnRandomStruct.setOnAction(e -> {
            structSeedField.setText(String.valueOf(new Random().nextLong(100000, 9999999)));
            regenerateAndRepaint();
        });

        HBox seedBox = new HBox(6, new Label() {{ textProperty().bind(I18nManager.getInstance().createStringBinding("world.seed.struct")); setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;"); }}, structSeedField, btnRandomStruct);
        seedBox.setAlignment(Pos.CENTER_LEFT);

        treeCountSlider = mkSlider(0, 200, 15);
        treeCountSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.struct.tree_count.tt"));
        hollowLogsSlider = mkSlider(0, 8, 3);
        hollowLogsSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.struct.hollow_logs.tt"));
        rockCrevicesSlider = mkSlider(0, 8, 3);
        rockCrevicesSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.struct.rock_crevices.tt"));
        addLsn(treeCountSlider, hollowLogsSlider);
        addFloraLsn(rockCrevicesSlider);

        comboTreeSpecies = new ComboBox<>();
        comboTreeSpecies.getItems().addAll(
            "🌵 Acacia Savannah Biome (Pseudomyrmex / Nectaries)",
            "🎋 Bamboo Grove Biome (Temnothorax / Colobopsis)",
            "🌿 Birch & Grasses Biome (Messor / Foraging)",
            "🪵 Dead Wood & Rotting Stumps Biome",
            "🌵 Desert & Saguaro Cactus Biome (Myrmecocystus / Cephalotes)",
            "🌳 Oak & Hardwood Forest Biome (Atta / Camponotus)",
            "🌲 Pine Forest Biome (Formica / Cinara Aphids)"
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
            sp.valueProperty().addListener((o, a, b) -> {
                updateEcologicalCompatibilityScores();
                repaintAllViews();
            });
        }

        GridPane botGrid = new GridPane();
        botGrid.setHgap(8); botGrid.setVgap(6);

        Label lblOak = new Label(); lblOak.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.oak_pct"));
        Label lblPine = new Label(); lblPine.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.pine_pct"));
        Label lblAcacia = new Label(); lblAcacia.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.acacia_pct"));
        Label lblCactus = new Label(); lblCactus.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.cactus_pct"));
        Label lblBirch = new Label(); lblBirch.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.birch_pct"));
        Label lblBamboo = new Label(); lblBamboo.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.bamboo_pct"));
        Label lblDeadwood = new Label(); lblDeadwood.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.deadwood_pct"));

        botGrid.add(lblOak, 0, 0); botGrid.add(oakPctSpinner, 1, 0);
        botGrid.add(lblPine, 0, 1); botGrid.add(pinePctSpinner, 1, 1);
        botGrid.add(lblAcacia, 0, 2); botGrid.add(acaciaPctSpinner, 1, 2);
        botGrid.add(lblCactus, 0, 3); botGrid.add(cactusPctSpinner, 1, 3);
        botGrid.add(lblBirch, 0, 4); botGrid.add(birchPctSpinner, 1, 4);
        botGrid.add(lblBamboo, 0, 5); botGrid.add(bambooPctSpinner, 1, 5);
        botGrid.add(lblDeadwood, 0, 6); botGrid.add(deadWoodPctSpinner, 1, 6);

        Label lblTreeCount = new Label(); lblTreeCount.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.tree_count"));
        Label lblHollowLogs = new Label(); lblHollowLogs.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.hollow_logs"));
        Label lblRockCrevices = new Label(); lblRockCrevices.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.rock_crevices"));
        Label lblDominantSpecies = new Label(); lblDominantSpecies.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.dominant_species"));
        Label lblMatrixHeader = new Label(); lblMatrixHeader.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.matrix_header"));

        return new VBox(8,
                seedBox,
                lblTreeCount, sv(treeCountSlider, ""),
                lblHollowLogs, sv(hollowLogsSlider, ""),
                lblRockCrevices, sv(rockCrevicesSlider, ""),
                new Separator(),
                lblDominantSpecies,
                comboTreeSpecies,
                lblMatrixHeader,
                botGrid
        );
    }

    private VBox buildDiagBlock() {
        lblBioclimaticZoneBadge.getStyleClass().add("accent-title");

        GridPane diagGrid = new GridPane();
        diagGrid.setHgap(10); diagGrid.setVgap(4);
        diagGrid.getStyleClass().add("diag-card");

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

        Label lblDiagZone = new Label(); lblDiagZone.textProperty().bind(I18nManager.getInstance().createStringBinding("world.diag.zone"));
        Label lblDiagAtta = new Label(); lblDiagAtta.textProperty().bind(I18nManager.getInstance().createStringBinding("world.diag.atta"));
        Label lblDiagAphid = new Label(); lblDiagAphid.textProperty().bind(I18nManager.getInstance().createStringBinding("world.diag.aphid"));
        Label lblDiagWood = new Label(); lblDiagWood.textProperty().bind(I18nManager.getInstance().createStringBinding("world.diag.wood"));
        Label lblDiagPogo = new Label(); lblDiagPogo.textProperty().bind(I18nManager.getInstance().createStringBinding("world.diag.pogonomyrmex"));
        Label lblDiagSole = new Label(); lblDiagSole.textProperty().bind(I18nManager.getInstance().createStringBinding("world.diag.solenopsis"));

        diagGrid.add(lblDiagZone, 0, 0); diagGrid.add(lblBioclimaticZoneBadge, 1, 0);
        diagGrid.add(lblDiagAtta, 0, 1); diagGrid.add(lblAttaCompatScore, 1, 1);
        diagGrid.add(lblDiagAphid, 0, 2); diagGrid.add(lblAphidCompatScore, 1, 2);
        diagGrid.add(lblDiagWood, 0, 3); diagGrid.add(lblWoodNestCompatScore, 1, 3);
        diagGrid.add(new Label("🌵 Pseudomyrmex (Acacia) :"), 0, 4); diagGrid.add(lblAcaciaAntCompatScore, 1, 4);
        diagGrid.add(new Label("🏜️ Desert Ants (Cactus) :"), 0, 5); diagGrid.add(lblCactusAntCompatScore, 1, 5);
        diagGrid.add(lblDiagPogo, 0, 6); diagGrid.add(lblPogonomyrmexCompatScore, 1, 6);
        diagGrid.add(new Label("🪵 Reticulitermes (Termites) :"), 0, 7); diagGrid.add(lblTermiteCompatScore, 1, 7);
        diagGrid.add(new Label("🐝 Apis mellifera (Honey Bees) :"), 0, 8); diagGrid.add(lblApisCompatScore, 1, 8);
        diagGrid.add(new Label("🐝 Vespula vulgaris (Wasps) :"), 0, 9); diagGrid.add(lblVespulaCompatScore, 1, 9);
        diagGrid.add(lblDiagSole, 0, 10); diagGrid.add(lblSolenopsisCompatScore, 1, 10);

        updateEcologicalCompatibilityScores();

        Label lblDiagTitle = new Label(); lblDiagTitle.textProperty().bind(I18nManager.getInstance().createStringBinding("world.diag.title"));

        return new VBox(8,
                lblDiagTitle,
                diagGrid
        );
    }

    private VBox buildAdaptBlock() {
        VBox adaptBox = new VBox(6);
        adaptBox.getStyleClass().add("diag-card");

        Label lblAdapt = new Label();
        lblAdapt.textProperty().bind(I18nManager.getInstance().createStringBinding("world.adapt.title"));
        lblAdapt.getStyleClass().add("purple-title");

        speciesAdaptCombo = new ComboBox<>();
        speciesAdaptCombo.getItems().addAll(
            "🐝 Apis mellifera (Honey Bee - Flowery Meadow & Nectar)",
            "🍃 Atta sexdens (Leafcutter Ant - Tropical Rainforest)",
            "🐜 Camponotus ligniperda (Carpenter Ant - Oak Forest & Dead Wood)",
            "🌵 Cataglyphis bombycina (Sahara Silver Ant - Arid Sahara Desert)",
            "🌲 Formica rufa (Red Wood Ant - Taiga / Pine Forest)",
            "🍯 Lasius niger (Black Garden Ant - Temperate Forest)",
            "🌾 Messor barbarus (Harvester Ant - Semi-Arid Steppe)",
            "🌵 Pseudomyrmex gracilis (Acacia Ant - Tropical Savanna)",
            "🕳️ Reticulitermes lucifugus (Termite - Decaying Wood & Caves)",
            "🔥 Solenopsis invicta (Red Imported Fire Ant - Humid Plains & Savanna)"
        );
        FXCollections.sort(speciesAdaptCombo.getItems());
        speciesAdaptCombo.getSelectionModel().selectFirst();
        speciesAdaptCombo.setMaxWidth(Double.MAX_VALUE);

        Button btnAdapt = new Button();
        btnAdapt.textProperty().bind(I18nManager.getInstance().createStringBinding("world.adapt.btn"));
        btnAdapt.setMaxWidth(Double.MAX_VALUE);
        btnAdapt.getStyleClass().add("btn-primary");
        btnAdapt.setGraphic(new FontIcon(Feather.CPU));
        btnAdapt.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.adapt.btn.tt"));
        btnAdapt.setOnAction(e -> handleAdaptWorldToSpecies());

        lblAdaptStatus = new Label();
        lblAdaptStatus.textProperty().bind(I18nManager.getInstance().createStringBinding("world.adapt.status"));
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
            case 6 -> { oakPctSpinner.getValueFactory().setValue(0); pinePctSpinner.getValueFactory().setValue(0); acaciaPctSpinner.getValueFactory().setValue(90); cactusPctSpinner.getValueFactory().setValue(0); birchPctSpinner.getValueFactory().setValue(0); bambooPctSpinner.getValueFactory().setValue(0); deadWoodPctSpinner.getValueFactory().setValue(10); } // Savane d'Acacias
        }
        updateEcologicalCompatibilityScores();
        repaintAllViews();
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

        if (lblAttaCompatScore != null) lblAttaCompatScore.setText(attaScore + "% (Foliage / Fungus)");
        if (lblAphidCompatScore != null) lblAphidCompatScore.setText(aphidScore + "% (Aphids / Honeydew)");
        if (lblWoodNestCompatScore != null) lblWoodNestCompatScore.setText(woodScore + "% (Wood Excavation)");
        if (lblAcaciaAntCompatScore != null) lblAcaciaAntCompatScore.setText(acaciaScore + "% (Acacia Nectaries)");
        if (lblCactusAntCompatScore != null) lblCactusAntCompatScore.setText(cactusScore + "% (Arid Soil / Cactus)");
        if (lblPogonomyrmexCompatScore != null) lblPogonomyrmexCompatScore.setText(pogoScore + "% (Grasses / Seeds)");
        if (lblTermiteCompatScore != null) lblTermiteCompatScore.setText(termiteScore + "% (Dead Wood / Cellulose)");
        if (lblApisCompatScore != null) lblApisCompatScore.setText(apisScore + "% (Nectar & Pollen)");
        if (lblVespulaCompatScore != null) lblVespulaCompatScore.setText(vespulaScore + "% (Hunting / Paper Nest)");
        if (lblSolenopsisCompatScore != null) lblSolenopsisCompatScore.setText(solenopsisScore + "% (Soil Mounds / Carnivorous)");
    }

    private VBox buildSculptBlock() {
        enableSculptingCheck = new CheckBox("🖌️ Enable Direct Sculpting Mode (Mouse Drag)");
        enableSculptingCheck.setSelected(false);
        enableSculptingCheck.setStyle("-fx-font-weight: bold;");
        enableSculptingCheck.getStyleClass().add("accent-title");
        enableSculptingCheck.setOnAction(e -> repaintAllViews());

        brushModeSelect = new ComboBox<>();
        brushModeSelect.getItems().addAll("Lower Elevation", "Raise Elevation", "Smooth Relief");
        ComboBoxTooltipHelper.setupDescriptiveComboBox(brushModeSelect,
            val -> val,
            val -> switch (val != null ? val : "") {
                case "Raise Elevation" -> "Increases the terrain elevation at painted coordinates.";
                case "Lower Elevation" -> "Digs into the terrain and lowers surface voxel elevation.";
                case "Smooth Relief" -> "Calculates local average elevation to smooth sharp slopes.";
                default -> "";
            }
        );
        brushModeSelect.getSelectionModel().selectFirst();
        brushModeSelect.setPrefWidth(240);
        brushModeSelect.valueProperty().addListener((o, a, b) -> repaintAllViews());

        brushSubstrateSelect = new ComboBox<>();
        brushSubstrateSelect.getItems().addAll(
            "Earth Substrate", "Sand Substrate", "Clay Substrate",
            "Silt Substrate", "Peat Substrate", "Gravel Substrate",
            "Stone Substrate", "Natural Cavity / Void"
        );

        brushRadiusSlider = mkSlider(1, 15, 4);
        brushStrengthSlider = mkSlider(10, 100, 50);
        addLsn(brushRadiusSlider, brushStrengthSlider);

        Label lblBrushMode = new Label();
        lblBrushMode.textProperty().bind(I18nManager.getInstance().createStringBinding("world.mode_brush"));
        Label lblBrushRadius = new Label();
        lblBrushRadius.textProperty().bind(I18nManager.getInstance().createStringBinding("world.radius_brush"));
        Label lblBrushStrength = new Label();
        lblBrushStrength.textProperty().bind(I18nManager.getInstance().createStringBinding("world.strength_brush"));

        return new VBox(8,
                enableSculptingCheck,
                lblBrushMode, brushModeSelect,
                lblBrushRadius, sv(brushRadiusSlider, "vx"),
                lblBrushStrength, sv(brushStrengthSlider, "%")
        );
    }

    // ── Resizable Tri-View Area ────────────────────────────────────────────────

    // ── Resizable Tri-View Area ────────────────────────────────────────────────

    private HBox buildViews() {
        canvas3D = new ResizableCanvas(540, 510); gc3D = canvas3D.getGraphicsContext2D();
        canvasSide = new ResizableCanvas(190, 150); gcSide = canvasSide.getGraphicsContext2D();
        canvasTop = new ResizableCanvas(190, 150); gcTop = canvasTop.getGraphicsContext2D();

        setupMouseControls();

        trackedAntPane = new TrackedAntPane();
        trackedAntPane.setPickOnBounds(true);
        trackedAntPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        trackedAntPane.setVisible(isSimulationMode);
        StackPane.setAlignment(trackedAntPane, Pos.BOTTOM_LEFT);
        StackPane.setMargin(trackedAntPane, new Insets(10, 10, 40, 10));

        trackedAntPane.setOnFollowAnt(ant -> {
            setFollowedAnt(ant);
            setFollowAntCameraEnabled(true);
        });

        trackedAntPane.setOnFollowAntById(query -> {
            setFollowedAntById(query);
        });

        trackedAntPane.setOnStopFollow(() -> {
            setFollowedAnt(null);
            setFollowAntCameraEnabled(false);
            trackedAntPane.setNoAntSelectedState();
        });

        StackPane hSide = new StackPane(canvasSide);
        hSide.setStyle("-fx-border-color: rgba(255,255,255,0.2); -fx-border-radius: 4; -fx-background-radius: 4; -fx-background-color: #0f172a;");
        StackPane hTop = new StackPane(canvasTop);
        hTop.setStyle("-fx-border-color: rgba(255,255,255,0.2); -fx-border-radius: 4; -fx-background-radius: 4; -fx-background-color: #0f172a;");

        Label ls = new Label(I18nManager.getInstance().get("minimap.sideview"));
        ls.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
        ls.getStyleClass().add("accent-title");

        Label lt = new Label(); lt.textProperty().bind(I18nManager.getInstance().createStringBinding("world.top_view"));
        lt.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
        lt.getStyleClass().add("accent-title");

        this.sideMinimapsBox = new VBox(4, lt, hTop, ls, hSide);
        sideMinimapsBox.setPadding(new Insets(6));
        sideMinimapsBox.setStyle("-fx-background-color: rgba(15, 23, 42, 0.85); -fx-background-radius: 8; -fx-border-color: rgba(56, 189, 248, 0.4); -fx-border-width: 1; -fx-border-radius: 8;");
        sideMinimapsBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        sideMinimapsBox.setPickOnBounds(true);
        StackPane.setAlignment(sideMinimapsBox, Pos.TOP_RIGHT);
        StackPane.setMargin(sideMinimapsBox, new Insets(10, 10, 10, 10));

        StackPane h3d = new StackPane(canvas3D, sideMinimapsBox, trackedAntPane);
        h3d.setStyle("-fx-border-color: #555; -fx-border-width: 1; -fx-background-color: #0b0f19;");
        HBox.setHgrow(h3d, Priority.ALWAYS);
        VBox.setVgrow(h3d, Priority.ALWAYS);

        canvas3D.widthProperty().bind(h3d.widthProperty());
        canvas3D.heightProperty().bind(h3d.heightProperty());

        canvas3D.widthProperty().addListener((obs, oldV, newV) -> repaintAllViews());
        canvas3D.heightProperty().addListener((obs, oldV, newV) -> repaintAllViews());

        this.rightRenderOptions = buildRightRenderOptionsPanel();
        VBox legendPanel = buildLegendPanel();
        if (isSimulationMode) {
            setHideRightRenderOptions(true);
        }

        VBox rightSideContent = new VBox(10, legendPanel, new Separator(), rightRenderOptions);
        rightSideContent.setPadding(new Insets(4));
        rightSideContent.setPrefWidth(245);

        this.rightScroll = new ScrollPane(rightSideContent);
        this.rightScroll.setFitToWidth(true);
        this.rightScroll.setPrefWidth(260);
        this.rightScroll.setMinWidth(250);
        this.rightScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        HBox area = new HBox(6, h3d, this.rightScroll);
        area.setPadding(new Insets(8, 8, 4, 8));
        VBox.setVgrow(area, Priority.ALWAYS);
        return area;
    }

    private VBox buildRightRenderOptionsPanel() {
        VBox panel = new VBox(6);
        panel.setPadding(new Insets(8));
        panel.getStyleClass().add("render-options-panel");

        Label title = new Label("⚙️ 3D Rendering & Layer Options:");
        title.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold;");
        title.getStyleClass().add("legend-title");

        this.showTerrainCheck = new CheckBox("🏞️ Render 3D Terrain");
        this.showTerrainCheck.setSelected(true);
        this.showTerrainCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.terrain.tt"));
        this.showTerrainCheck.selectedProperty().addListener((obs, oldV, newV) -> setTerrainVisible(newV));

        this.showChamferedBezelCheck = new CheckBox("📐 Substrate Chamfered Bezel");
        this.showChamferedBezelCheck.setSelected(true);
        this.showChamferedBezelCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.bezel.tt"));

        this.showGravelInclusionsCheck = new CheckBox("🪨 Gravel Inclusions");
        this.showGravelInclusionsCheck.setSelected(true);
        this.showGravelInclusionsCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.gravel.tt"));

        this.showSubstrateStratigraphyCheck = new CheckBox("🧱 Lateral Stratigraphy");
        this.showSubstrateStratigraphyCheck.setSelected(true);
        this.showSubstrateStratigraphyCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.stratigraphy.tt"));

        this.showTranslucentVolumetricModeCheck = new CheckBox("🔮 Volumetric Translucency");
        this.showTranslucentVolumetricModeCheck.setSelected(false);
        this.showTranslucentVolumetricModeCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.translucent.tt"));

        this.showHumidityCheck = new CheckBox("💧 Substrate Humidity Map");
        this.showHumidityCheck.setSelected(false);
        this.showHumidityCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.humidity.tt"));

        this.showOrganicCheck = new CheckBox("🍂 Organic Matter"); this.showOrganicCheck.setSelected(true);
        this.showEarthCheck = new CheckBox("🟤 Earth / Humus"); this.showEarthCheck.setSelected(true);
        this.showSandCheck = new CheckBox("🟡 Sand"); this.showSandCheck.setSelected(true);
        this.showClayCheck = new CheckBox("🔴 Clay"); this.showClayCheck.setSelected(true);
        this.showSiltCheck = new CheckBox("🟨 Silt"); this.showSiltCheck.setSelected(true);
        this.showPeatCheck = new CheckBox("⬛ Peat"); this.showPeatCheck.setSelected(true);
        this.showGravelCheck = new CheckBox("⚪ Gravel / Pebbles"); this.showGravelCheck.setSelected(true);
        this.showStoneCheck = new CheckBox("⚪ Stone / Rock"); this.showStoneCheck.setSelected(true);

        this.showGalleriesCheck = new CheckBox("🕳️ Natural Cavities & Voids"); this.showGalleriesCheck.setSelected(true);
        this.showGalleriesCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.galleries.tt"));

        this.showVegetationCheck = new CheckBox("🌿 Vegetation & Trees"); this.showVegetationCheck.setSelected(true);
        this.showRootsCheck = new CheckBox("🌳 Subterranean 3D Roots"); this.showRootsCheck.setSelected(true);
        this.showRootsCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.roots.tt"));
        this.showPhCheck = new CheckBox("🧪 Soil pH Map"); this.showPhCheck.setSelected(false);
        this.showPhCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.ph.tt"));

        this.slicePlaneSlider = mkSlider(0.0, 100.0, 100.0);
        addLsn(this.slicePlaneSlider);

        addBoolLsn(showTerrainCheck, showChamferedBezelCheck, showGravelInclusionsCheck, showSubstrateStratigraphyCheck,
                   showTranslucentVolumetricModeCheck, showHumidityCheck,
                   showOrganicCheck, showEarthCheck, showSandCheck, showClayCheck, showSiltCheck, showPeatCheck, showGravelCheck, showStoneCheck, showGalleriesCheck,
                   showVegetationCheck, showRootsCheck, showPhCheck);

        Label visHeader = new Label("👁️ Layer Visibility:");
        visHeader.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
        visHeader.getStyleClass().add("purple-title");

        VBox visBox = new VBox(4,
            visHeader,
            showTerrainCheck, showOrganicCheck, showEarthCheck, showSandCheck, showClayCheck, showSiltCheck, showPeatCheck, showGravelCheck, showStoneCheck, showGalleriesCheck,
            new Separator(),
            showVegetationCheck, showRootsCheck, showPhCheck, showSubstrateStratigraphyCheck,
            showHumidityCheck
        );

        panel.getChildren().addAll(title, showTerrainCheck, showChamferedBezelCheck, showSubstrateStratigraphyCheck,
                                   new Label("Scan Cut Plane (%) :"), sv(slicePlaneSlider, "%"),
                                   showTranslucentVolumetricModeCheck, new Separator(), visBox);
        return panel;
    }

    private VBox buildLegendPanel() {
        VBox panel = new VBox(6);
        panel.setPadding(new Insets(6));
        panel.getStyleClass().add("legend-card-pane");

        syncViewsCheckBox = new CheckBox("🔗 Synchronize Views");
        syncViewsCheckBox.setSelected(true);
        syncViewsCheckBox.getStyleClass().add("legend-checkbox");

        // 1. Substrate Legend Header & Items
        Label titleSubstrates = new Label("🌱 Substrates Legend:");
        titleSubstrates.getStyleClass().add("legend-title");

        FlowPane substrateItemsPane = new FlowPane(4, 4);
        substrateItemsPane.setPrefWrapLength(220);

        lblHoverInfo = new Label(I18nManager.getInstance().get("world.hover_info"));
        lblHoverInfo.getStyleClass().add("legend-hover-info");

        List<String[]> substrateList = new ArrayList<>();
        substrateList.add(new String[]{"Organic Matter", "#523219", "🍂 Organic Matter: Surface litter and detritus."});
        substrateList.add(new String[]{"Earth", "#3d2817", "🟤 Earth / Humus: Loose organic topsoil."});
        substrateList.add(new String[]{"Sand", "#eab308", "🟡 Sand: Granular substrate with low cohesion."});
        substrateList.add(new String[]{"Clay", "#9a3412", "🔴 Clay: Dense mineral plastic substrate."});
        substrateList.add(new String[]{"Silt", "#ca8a04", "🟨 Silt / Limon: Fine-grained mineral sediment with moderate cohesion."});
        substrateList.add(new String[]{"Peat", "#451a03", "⬛ Peat / Tourbe: Dark organic-rich subterranean soil."});
        substrateList.add(new String[]{"Gravel", "#94a3b8", "⚪ Gravel / Pebbles: Coarse excavable mineral substrate."});
        substrateList.add(new String[]{"Stone", "#64748b", "⚪ Stone / Rock: Bedrock and unexcavable boulders."});
        substrateList.add(new String[]{"Cavities", "#0f172a", "🕳️ Natural Cavities: Worm galleries, karst, and air pockets."});
        substrateList.add(new String[]{"Roots", "#78350f", "🌳 Subterranean 3D Roots: Excavation obstacle & root aphids."});
        substrateList.add(new String[]{"River", "#0284c7", "💧 River / Planar Water: Horizontal liquid surface."});
        substrateList.add(new String[]{"Vegetation", "#15803d", "🌿 Vegetation Cover: Vegetation and trees."});

        for (String[] it : substrateList) {
            HBox item = new HBox(4);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setPadding(new Insets(2, 4, 2, 4));
            item.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-background-radius: 4;");

            Canvas dot = new Canvas(8, 8);
            GraphicsContext g = dot.getGraphicsContext2D();
            g.setFill(Color.web(it[1]));
            g.fillOval(0, 0, 8, 8);
            g.setStroke(Color.WHITE);
            g.setLineWidth(0.6);
            g.strokeOval(0, 0, 8, 8);

            Label lbl = new Label(it[0]);
            item.getChildren().addAll(dot, lbl);
            substrateItemsPane.getChildren().add(item);
        }

        panel.getChildren().addAll(
            syncViewsCheckBox,
            titleSubstrates,
            substrateItemsPane
        );

        // 2. Nest Interior & Chamber Galleries Full Multi-Species Legend (Visible in Simulation mode)
        Label titleNestInterior = new Label("🏰 Nest & Multi-Species Chambers Legend:");
        titleNestInterior.getStyleClass().add("legend-title");

        VBox nestItemsBox = new VBox(3);
        nestItemsBox.setPadding(new Insets(2, 0, 2, 0));

        List<String[]> nestList = new ArrayList<>();
        nestList.add(new String[]{"Royal Chamber (Queen Quarters & Egg-laying)", "#d946ef", "dot", "👑 Royal Chamber: Ingestion and egg-laying quarters for queens."});
        nestList.add(new String[]{"Brood (Eggs, Larvae & Pupae)", "#f8fafc", "dot_stroke", "🥚 Brood Chambers: Developing eggs, larvae, and pupae."});
        nestList.add(new String[]{"Fungus Garden (Atta / Macrotermes)", "#a855f7", "dot", "🍄 Fungus Garden: Symbiotic basidiomycete cultivation."});
        nestList.add(new String[]{"Aphid Farm & Livestock", "#ec4899", "dot", "🌸 Aphid Farm: Homopteran livestock and honeydew harvesting."});
        nestList.add(new String[]{"Granary / Food Stores (Seeds, Nectar, Pollen)", "#22c55e", "dot", "🍖 Storage Chambers: Dried food reserves and resin."});
        nestList.add(new String[]{"Diapause / Hibernation Chamber", "#0284c7", "dot", "❄️ Diapause Chamber: Thermal winter refuge."});
        nestList.add(new String[]{"Refuse Dump / Trash & Remains", "#eab308", "dot", "⚠️ Refuse Dump: Sanitary management of corpses and waste."});
        nestList.add(new String[]{"Excavated Voxel Shafts & Tunnels", "#f59e0b", "line", "🕳️ 3D Galleries: Subterranean excavated transit paths."});
        nestList.add(new String[]{"Subnivean Tunnels (Under Snow/Ice)", "#e0f2fe", "line", "🌨️ Subnivean Tunnels: Winter respiration and access routes."});
        nestList.add(new String[]{"Nest Entrances & Excavated Mounds", "#9a3412", "dot", "🚪 Nest Entrances: Entrance holes and ventilation mounds."});

        for (String[] it : nestList) {
            HBox item = new HBox(6);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setPadding(new Insets(2, 4, 2, 4));
            item.getStyleClass().add("legend-item");

            javafx.scene.Node symbolNode;
            if ("line".equals(it[2])) {
                Canvas lineCanvas = new Canvas(12, 8);
                GraphicsContext g = lineCanvas.getGraphicsContext2D();
                g.setStroke(Color.web(it[1]));
                g.setLineWidth(2.5);
                g.strokeLine(0, 4, 12, 4);
                symbolNode = lineCanvas;
            } else {
                Canvas dot = new Canvas(8, 8);
                GraphicsContext g = dot.getGraphicsContext2D();
                g.setFill(Color.web(it[1]));
                g.fillOval(0, 0, 8, 8);
                if ("dot_stroke".equals(it[2])) {
                    g.setStroke(Color.web("#94a3b8"));
                    g.setLineWidth(0.8);
                    g.strokeOval(0, 0, 8, 8);
                } else {
                    g.setStroke(Color.WHITE);
                    g.setLineWidth(0.6);
                    g.strokeOval(0, 0, 8, 8);
                }
                symbolNode = dot;
            }

            Label lbl = new Label(it[0]);
            lbl.setTooltip(new Tooltip(it[3]));
            item.getChildren().addAll(symbolNode, lbl);
            nestItemsBox.getChildren().add(item);
        }

        Label lblVoxelNote = new Label(); lblVoxelNote.textProperty().bind(I18nManager.getInstance().createStringBinding("world.voxel_note"));
        lblVoxelNote.setStyle("-fx-text-fill: #4ade80; -fx-font-size: 9px; -fx-font-style: italic; -fx-padding: 3 0 0 0;");

        this.nestLegendBox = new VBox(4);
        this.nestLegendBox.getChildren().addAll(new Separator(), titleNestInterior, nestItemsBox, lblVoxelNote);
        this.nestLegendBox.setVisible(isSimulationMode);
        this.nestLegendBox.setManaged(isSimulationMode);

        panel.getChildren().add(this.nestLegendBox);

        panel.getChildren().add(lblHoverInfo);
        return panel;
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
        canvas3D.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) { resetAllCameras(); return; }
            if (enableSculptingCheck != null && enableSculptingCheck.isSelected()) return;

            double mx = e.getX();
            double my = e.getY();
            double w = canvas3D.getWidth();
            double h = canvas3D.getHeight();
            double radAz = Math.toRadians(azimuth);
            double radEl = Math.toRadians(elevation);
            double cx = w / 2 + pan3DX;
            double cy = h / 2 + pan3DY + 40;
            double scale = zoom * 12.0;

            org.swarmforge.core.domain.Individual clickedAnt = null;
            double minAntDistSq = 900.0;
            if (activeSimulation != null) {
                for (org.swarmforge.core.domain.Colony colony : activeSimulation.getColonies()) {
                    for (org.swarmforge.core.domain.Individual ind : colony.getLivingIndividuals()) {
                        double ax = ind.getX();
                        double ay = ind.getY();
                        double az = ind.getZ();

                        double gx = Math.max(0.0, Math.min(GRID_SIZE - 1.0, (ax / (float) Math.max(1, activeSimulation.getTerrarium().getWidth())) * GRID_SIZE));
                        double gy = Math.max(0.0, Math.min(GRID_SIZE - 1.0, (ay / (float) Math.max(1, activeSimulation.getTerrarium().getHeight())) * GRID_SIZE));
                        int igx = (int) gx;
                        int igy = (int) gy;
                        double gz = heightGrid[igx][igy] * 40.0 + az * 2.0 + 1.5;

                        double[] p = project3DPoint(gx, gy, gz, cx, cy, scale, radAz, radEl);
                        double dSq = (p[0] - mx) * (p[0] - mx) + (p[1] - my) * (p[1] - my);
                        if (dSq < minAntDistSq) {
                            minAntDistSq = dSq;
                            clickedAnt = ind;
                        }
                    }
                }
            }

            if (clickedAnt != null) {
                setFollowedAnt(clickedAnt);
                if (trackedAntPane != null) {
                    trackedAntPane.updateAnt(clickedAnt, true);
                    trackedAntPane.setVisible(true);
                }
            } else if (trackedAntPane != null) {
                trackedAntPane.clearSearchStatus();
            }
        });

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
        int bestX = -1, bestY = -1, bestD = 0;
        double minDstSq = Double.MAX_VALUE;
        int step = 2;

        double cutRatio = slicePlaneSlider != null ? (slicePlaneSlider.getValue() / 100.0) : 1.0;
        int cutXLimit = (int) (GRID_SIZE * cutRatio);
        double targetDepthVal = depthSlider != null ? depthSlider.getValue() : 1.5;
        double maxDepthPx = targetDepthVal * 22.0;
        double layerDepthPx = maxDepthPx / SOIL_DEPTH;

        // 1. Check Subterranean Cut Plane Wall (if active and facing camera)
        boolean isCutPlaneFacingCamera = Math.sin(radAz) >= 0;
        if (cutXLimit > 0 && cutXLimit < GRID_SIZE && (!isTerrainVisible || isCutPlaneFacingCamera)) {
            for (int y = 0; y < GRID_SIZE - step; y += step) {
                double surfZ0 = heightGrid[cutXLimit][y] * 40.0;
                double surfZ1 = heightGrid[cutXLimit][y + step] * 40.0;
                if (carvedVoxelGrid[cutXLimit][y]) surfZ0 -= 15.0;
                if (carvedVoxelGrid[cutXLimit][y + step]) surfZ1 -= 15.0;

                double refZ0 = 32.0;
                double refZ1 = 32.0;

                for (int d = 0; d < SOIL_DEPTH; d++) {
                    double topZ0 = Math.min(surfZ0, refZ0 - d * layerDepthPx);
                    double topZ1 = Math.min(surfZ1, refZ1 - d * layerDepthPx);
                    double botZ0 = Math.min(surfZ0, refZ0 - (d + 1) * layerDepthPx);
                    double botZ1 = Math.min(surfZ1, refZ1 - (d + 1) * layerDepthPx);

                    if (topZ0 <= botZ0 && topZ1 <= botZ1) continue;

                    double[] pTop0 = project3DPoint(cutXLimit, y, topZ0, cx, cy, scale, radAz, radEl);
                    double[] pTop1 = project3DPoint(cutXLimit, y + step, topZ1, cx, cy, scale, radAz, radEl);
                    double[] pBot1 = project3DPoint(cutXLimit, y + step, botZ1, cx, cy, scale, radAz, radEl);
                    double[] pBot0 = project3DPoint(cutXLimit, y, botZ0, cx, cy, scale, radAz, radEl);

                    double midPx = (pTop0[0] + pTop1[0] + pBot1[0] + pBot0[0]) / 4.0;
                    double midPy = (pTop0[1] + pTop1[1] + pBot1[1] + pBot0[1]) / 4.0;

                    double dSq = (midPx - mx) * (midPx - mx) + (midPy - my) * (midPy - my);
                    if (dSq < minDstSq) {
                        minDstSq = dSq;
                        bestX = cutXLimit;
                        bestY = y;
                        bestD = d;
                    }
                }
            }
        }

        // 2. Check 4 Subterranean Skirt Walls ("Jupe")
        int[][] skirtEdges = new int[][]{
            {0, 0, GRID_SIZE - 1, 0},
            {GRID_SIZE - 1, 0, GRID_SIZE - 1, GRID_SIZE - 1},
            {0, GRID_SIZE - 1, GRID_SIZE - 1, GRID_SIZE - 1},
            {0, 0, 0, GRID_SIZE - 1}
        };
        for (int[] edge : skirtEdges) {
            int ex0 = edge[0], ey0 = edge[1], ex1 = edge[2], ey1 = edge[3];
            int edx = Integer.compare(ex1, ex0);
            int edy = Integer.compare(ey1, ey0);
            int elen = Math.max(Math.abs(ex1 - ex0), Math.abs(ey1 - ey0));
            for (int i = 0; i < elen; i += step) {
                int wx0 = ex0 + i * edx;
                int wy0 = ey0 + i * edy;
                int wx1 = ex0 + Math.min(elen, i + step) * edx;
                int wy1 = ey0 + Math.min(elen, i + step) * edy;
                double surfZ0 = heightGrid[wx0][wy0] * 40.0;
                double surfZ1 = heightGrid[wx1][wy1] * 40.0;

                for (int d = 0; d < SOIL_DEPTH; d++) {
                    double topZ0 = Math.min(surfZ0, 32.0 - d * layerDepthPx);
                    double topZ1 = Math.min(surfZ1, 32.0 - d * layerDepthPx);
                    double botZ0 = Math.min(surfZ0, 32.0 - (d + 1) * layerDepthPx);
                    double botZ1 = Math.min(surfZ1, 32.0 - (d + 1) * layerDepthPx);
                    if (topZ0 <= botZ0 && topZ1 <= botZ1) continue;

                    double[] pTop0 = project3DPoint(wx0, wy0, topZ0, cx, cy, scale, radAz, radEl);
                    double[] pTop1 = project3DPoint(wx1, wy1, topZ1, cx, cy, scale, radAz, radEl);
                    double[] pBot1 = project3DPoint(wx1, wy1, botZ1, cx, cy, scale, radAz, radEl);
                    double[] pBot0 = project3DPoint(wx0, wy0, botZ0, cx, cy, scale, radAz, radEl);

                    double midPx = (pTop0[0] + pTop1[0] + pBot1[0] + pBot0[0]) / 4.0;
                    double midPy = (pTop0[1] + pTop1[1] + pBot1[1] + pBot0[1]) / 4.0;

                    double dSq = (midPx - mx) * (midPx - mx) + (midPy - my) * (midPy - my);
                    if (dSq < minDstSq) {
                        minDstSq = dSq;
                        bestX = wx0;
                        bestY = wy0;
                        bestD = d;
                    }
                }
            }
        }

        // 3. Check Surface Terrain Quads (up to cutXLimit)
        for (int x = 0; x < Math.min(GRID_SIZE, cutXLimit); x += step) {
            for (int y = 0; y < GRID_SIZE; y += step) {
                double z = heightGrid[x][y] * 40.0;
                double[] p = project3DPoint(x, y, z, cx, cy, scale, radAz, radEl);
                double dSq = (p[0] - mx) * (p[0] - mx) + (p[1] - my) * (p[1] - my);
                if (dSq < minDstSq) {
                    minDstSq = dSq;
                    bestX = x;
                    bestY = y;
                    bestD = 0;
                }
            }
        }

        if (minDstSq < 4900.0) { // within 70px radius
            return new int[]{bestX, bestY, bestD};
        }
        return null;
    }

    private void updateHoverInfo(double mx, double my, String viewType) {
        if (lblHoverInfo == null) return;
        double cw = "3D".equals(viewType) ? canvas3D.getWidth() : ("SIDE".equals(viewType) ? canvasSide.getWidth() : canvasTop.getWidth());
        double ch = "3D".equals(viewType) ? canvas3D.getHeight() : ("SIDE".equals(viewType) ? canvasSide.getHeight() : canvasTop.getHeight());
        if (cw <= 0 || ch <= 0) return;

        int gx, gy;
        int inspectedY;

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
                inspectedY = picked[2];
            } else {
                hover3DCell = null;
                gx = (int) Math.max(0, Math.min(GRID_SIZE - 1, (mx / cw) * GRID_SIZE));
                gy = (int) Math.max(0, Math.min(GRID_SIZE - 1, (my / ch) * GRID_SIZE));
                inspectedY = 0;
            }
            repaintAllViews();
        } else {
            gx = (int) Math.max(0, Math.min(GRID_SIZE - 1, (mx / cw) * GRID_SIZE));
            gy = (int) Math.max(0, Math.min(GRID_SIZE - 1, (my / ch) * GRID_SIZE));
            if ("SIDE".equals(viewType)) {
                inspectedY = (int) Math.max(0, Math.min(SOIL_DEPTH - 1, (my / ch) * SOIL_DEPTH));
            } else if (!isTerrainVisible || (slicePlaneSlider != null && slicePlaneSlider.getValue() < 99.0)) {
                double sliceVal = slicePlaneSlider != null ? slicePlaneSlider.getValue() : 100.0;
                inspectedY = (int) Math.max(0, Math.min(SOIL_DEPTH - 1, Math.round((1.0 - sliceVal / 100.0) * (SOIL_DEPTH - 1))));
            } else {
                inspectedY = (int) Math.max(0, Math.min(SOIL_DEPTH - 1, Math.round(heightGrid[gx][gy] * (SOIL_DEPTH - 1))));
            }
        }

        double sM = surfaceSizeSlider != null ? surfaceSizeSlider.getValue() : 25.0;
        double dM = depthSlider != null ? depthSlider.getValue() : 3.0;
        double altM = ((double) (SOIL_DEPTH - 1 - inspectedY) / (double) (SOIL_DEPTH - 1)) * dM;

        int humPct = (int) (humidityGrid[gx][gy][inspectedY] * 100);
        byte mat = soilLayers[gx][gy][inspectedY];
        boolean isVoid = voidGrid[gx][gy][inspectedY] || carvedVoxelGrid[gx][gy];
        String matName;
        if (isVoid) {
            matName = "🕳️ Excavated Gallery / Cavity";
        } else {
            matName = switch (mat) {
                case 0 -> "Humus (Topsoil)";
                case 1 -> "Xeric Sand";
                case 2 -> "Silty Clay";
                case 3 -> "Bedrock / Rock";
                case 4 -> "Gravel & Pebbles";
                case 5 -> "Organic Litter";
                case 6 -> "Subterranean Gallery";
                case 7 -> "Brood Chamber";
                case 8 -> "Royal Chamber";
                default -> "Soil / Earth";
            };
        }
        boolean isRiver = isNearRiver(gx, gy, 1);

        double tempC = tempGrid[gx][gy][inspectedY];
        if (tempC == 0) {
            double surfaceTemp = 20.0;
            double depthFactor = (double) (SOIL_DEPTH - 1 - inspectedY) / (double) (SOIL_DEPTH - 1);
            tempC = surfaceTemp - (depthFactor * 3.5);
        }
        float phVal = phGrid[gx][gy][inspectedY] > 0 ? phGrid[gx][gy][inspectedY] : 6.5f;
        int rootPct = (int) (rootGrid[gx][gy][inspectedY] * 100);

        String antHoverStr = "";
        if (activeSimulation != null && ("3D".equals(viewType) || "TOP".equals(viewType))) {
            double radAz = Math.toRadians(azimuth);
            double radEl = Math.toRadians(elevation);
            double cx = cw / 2 + pan3DX;
            double cy = ch / 2 + pan3DY + 40;
            double scale = zoom * 12.0;

            org.swarmforge.core.domain.Individual hoveredAnt = null;
            double minAntDistSq = 1225.0; // 35px threshold
            for (org.swarmforge.core.domain.Colony colony : activeSimulation.getColonies()) {
                for (org.swarmforge.core.domain.Individual ind : colony.getLivingIndividuals()) {
                    double ax = ind.getX();
                    double ay = ind.getY();
                    double az = ind.getZ();

                    double igx = Math.max(0.0, Math.min(GRID_SIZE - 1.0, (ax / (float) Math.max(1, activeSimulation.getTerrarium().getWidth())) * GRID_SIZE));
                    double igy = Math.max(0.0, Math.min(GRID_SIZE - 1.0, (ay / (float) Math.max(1, activeSimulation.getTerrarium().getHeight())) * GRID_SIZE));
                    int iigx = (int) igx;
                    int iigy = (int) igy;
                    double gz = heightGrid[iigx][iigy] * 40.0 + az * 2.0 + 1.5;

                    double[] p = project3DPoint(igx, igy, gz, cx, cy, scale, radAz, radEl);
                    double dSq = (p[0] - mx) * (p[0] - mx) + (p[1] - my) * (p[1] - my);
                    if (dSq < minAntDistSq) {
                        minAntDistSq = dSq;
                        hoveredAnt = ind;
                    }
                }
            }
            if (hoveredAnt != null) {
                antHoverStr = String.format(Locale.US, " | 🐜 Ant: %s [%s | %s] HP:%.0f%% E:%.0f%% Task:%s",
                    hoveredAnt.getSpecies() != null ? hoveredAnt.getSpecies().getCommonName() : "Formicidae",
                    hoveredAnt.getCaste(), hoveredAnt.getJob(), hoveredAnt.getHealth() * 100, hoveredAnt.getEnergy() * 100,
                    hoveredAnt.getState() != null ? hoveredAnt.getState().name() : "Active");
            }
        }

        lblHoverInfo.setText(String.format(Locale.US,
            "📍 Voxel [%d, %d, Prof %d/%d] | Alt: %.1fm | Substrat: %s | Temp: %.1f°C | Humidité: %d%% | pH: %.1f | Racines: %d%% | %s%s",
            gx, gy, inspectedY, SOIL_DEPTH - 1, altM, matName, tempC, humPct, phVal, rootPct, isRiver ? "💧 River" : "🌱 Terrestrial", antHoverStr));
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
                    if (mode != null && (mode.contains("RAISE") || mode.contains("Raise"))) {
                        heightGrid[cx][cy2] = Math.min(1.0, heightGrid[cx][cy2] + strength*falloff);
                    } else if (mode != null && (mode.contains("LOWER") || mode.contains("Lower"))) {
                        heightGrid[cx][cy2] = Math.max(0.01, heightGrid[cx][cy2] - strength*falloff);
                    } else if (mode != null && (mode.contains("SMOOTH") || mode.contains("Smooth"))) {
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

    private void regenerateFloraAndRepaint() {
        generateSurfaceFlora();
        repaintAllViews();
    }

    private void addFloraLsn(Slider... sliders) {
        for (Slider s : sliders) {
            if (s != null) s.valueProperty().addListener((o, a, b) -> regenerateFloraAndRepaint());
        }
    }

    private void addFloraBoolLsn(CheckBox... boxes) {
        for (CheckBox cb : boxes) {
            if (cb != null) cb.selectedProperty().addListener((o, a, b) -> regenerateFloraAndRepaint());
        }
    }

    private void addBoolLsn(CheckBox... boxes) {
        for (CheckBox cb : boxes) {
            if (cb != null) cb.selectedProperty().addListener((o, a, b) -> repaintAllViews());
        }
    }

    // ── Drawing Methods for 3D, Top-Down, and Side Views ───────────────────────

    public void repaintAllViews() {
        if (!isActive) return;
        draw3D();
        drawSide();
        drawTop();
        updateLiveInspector();
    }

    private void updateLiveInspector() {
        // Updated live inspector state
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

    private void drawVoxelBlockTexture(double[] pxs, double[] pys, byte mat, boolean isTop, boolean isSide, boolean isRightSide, int gx, int gy, int gz) {
        Color baseCol = switch (mat) {
            case 0 -> isTop ? Color.web("#15803d") : Color.web("#78350f");
            case 1 -> isSide ? Color.web("#ca8a04") : Color.web("#eab308");
            case 2 -> isSide ? Color.web("#92400e") : Color.web("#b45309");
            case 3 -> isSide ? Color.web("#334155") : Color.web("#64748b");
            case 4 -> isSide ? Color.web("#475569") : Color.web("#94a3b8");
            case 5 -> isSide ? Color.web("#854d0e") : Color.web("#ca8a04"); // Silt (Limon)
            case 6 -> isSide ? Color.web("#291605") : Color.web("#451a03"); // Peat (Tourbe)
            case 7 -> Color.web("#090d16", 0.92);                           // Cavity / Void
            default -> isSide ? Color.web("#451a03") : Color.web("#78350f");
        };
        if (isRightSide) baseCol = baseCol.darker();

        gc3D.setFill(baseCol);
        gc3D.fillPolygon(pxs, pys, 4);

        double midX = (pxs[0] + pxs[1] + pxs[2] + pxs[3]) / 4.0;
        double midY = (pys[0] + pys[1] + pys[2] + pys[3]) / 4.0;
        double faceW = Math.abs(pxs[1] - pxs[0]) + Math.abs(pys[2] - pys[1]);

        if (faceW > 3.0) {
            int hash = (gx * 73856093) ^ (gy * 19349663) ^ (gz * 83492791);
            Random pRand = new Random(hash);

            for (int p = 0; p < 5; p++) {
                double rx = (pRand.nextDouble() - 0.5) * 0.7;
                double ry = (pRand.nextDouble() - 0.5) * 0.7;
                double dotX = midX + rx * (pxs[1] - pxs[0]) + ry * (pxs[3] - pxs[0]);
                double dotY = midY + rx * (pys[1] - pys[0]) + ry * (pys[3] - pys[0]);
                double dotSz = Math.max(1.2, faceW * 0.15);

                Color speckCol = switch (mat) {
                    case 0 -> isTop ? (pRand.nextBoolean() ? Color.web("#86efac") : Color.web("#14532d"))
                                    : (pRand.nextBoolean() ? Color.web("#451a03") : Color.web("#9a3412"));
                    case 1 -> pRand.nextBoolean() ? Color.web("#fde047") : Color.web("#a16207");
                    case 2 -> pRand.nextBoolean() ? Color.web("#d97706") : Color.web("#7c2d12");
                    case 3 -> pRand.nextBoolean() ? Color.web("#94a3b8") : Color.web("#1e293b");
                    case 4 -> pRand.nextBoolean() ? Color.web("#cbd5e1") : Color.web("#1e293b");
                    case 5 -> pRand.nextBoolean() ? Color.web("#fef08a") : Color.web("#713f12");
                    case 6 -> pRand.nextBoolean() ? Color.web("#78350f") : Color.web("#1c0d02");
                    case 7 -> pRand.nextBoolean() ? Color.web("#0f172a") : Color.web("#1e293b");
                    default -> pRand.nextBoolean() ? Color.web("#451a03") : Color.web("#9a3412");
                };
                gc3D.setFill(speckCol);
                gc3D.fillRect(dotX - dotSz / 2, dotY - dotSz / 2, dotSz, dotSz);
            }

            if (mat == 0 && isSide) {
                gc3D.setStroke(Color.web("#22c55e", 0.95));
                gc3D.setLineWidth(Math.max(1.5, faceW * 0.12));
                gc3D.strokeLine(pxs[0], pys[0], pxs[1], pys[1]);
            }
        }

        gc3D.setStroke(Color.web("#0f172a", 0.85));
        gc3D.setLineWidth(1.4);
        gc3D.strokePolygon(pxs, pys, 4);
    }

    private void drawGamifiedVoxelCube3D(
        double wx, double wy, double wz,
        double sizeX, double sizeY, double sizeZ,
        Color topCol, Color sideCol,
        double cx, double cy, double scale, double radAz, double radEl
    ) {
        double[] p0 = project3DPoint(wx, wy, wz + sizeZ, cx, cy, scale, radAz, radEl);
        double[] p1 = project3DPoint(wx + sizeX, wy, wz + sizeZ, cx, cy, scale, radAz, radEl);
        double[] p2 = project3DPoint(wx + sizeX, wy + sizeY, wz + sizeZ, cx, cy, scale, radAz, radEl);
        double[] p3 = project3DPoint(wx, wy + sizeY, wz + sizeZ, cx, cy, scale, radAz, radEl);

        double[] b0 = project3DPoint(wx, wy, wz, cx, cy, scale, radAz, radEl);
        double[] b1 = project3DPoint(wx + sizeX, wy, wz, cx, cy, scale, radAz, radEl);
        double[] b2 = project3DPoint(wx + sizeX, wy + sizeY, wz, cx, cy, scale, radAz, radEl);
        double[] b3 = project3DPoint(wx, wy + sizeY, wz, cx, cy, scale, radAz, radEl);

        // 1. Top face
        gc3D.setFill(topCol);
        gc3D.fillPolygon(new double[]{p0[0], p1[0], p2[0], p3[0]}, new double[]{p0[1], p1[1], p2[1], p3[1]}, 4);
        gc3D.setStroke(Color.web("#0f172a", 0.7));
        gc3D.setLineWidth(1.0);
        gc3D.strokePolygon(new double[]{p0[0], p1[0], p2[0], p3[0]}, new double[]{p0[1], p1[1], p2[1], p3[1]}, 4);

        // 2. Front face (+Y)
        gc3D.setFill(sideCol.darker());
        gc3D.fillPolygon(new double[]{p3[0], p2[0], b2[0], b3[0]}, new double[]{p3[1], p2[1], b2[1], b3[1]}, 4);
        gc3D.strokePolygon(new double[]{p3[0], p2[0], b2[0], b3[0]}, new double[]{p3[1], p2[1], b2[1], b3[1]}, 4);

        // 3. Right face (+X)
        gc3D.setFill(sideCol);
        gc3D.fillPolygon(new double[]{p1[0], p2[0], b2[0], b1[0]}, new double[]{p1[1], p2[1], b2[1], b1[1]}, 4);
        gc3D.strokePolygon(new double[]{p1[0], p2[0], b2[0], b1[0]}, new double[]{p1[1], p2[1], b2[1], b1[1]}, 4);

        // 4. Back face (-Y)
        gc3D.setFill(sideCol.darker());
        gc3D.fillPolygon(new double[]{p0[0], p1[0], b1[0], b0[0]}, new double[]{p0[1], p1[1], b1[1], b0[1]}, 4);
        gc3D.strokePolygon(new double[]{p0[0], p1[0], b1[0], b0[0]}, new double[]{p0[1], p1[1], b1[1], b0[1]}, 4);

        // 5. Left face (-X)
        gc3D.setFill(sideCol);
        gc3D.fillPolygon(new double[]{p0[0], p3[0], b3[0], b0[0]}, new double[]{p0[1], p3[1], b3[1], b0[1]}, 4);
        gc3D.strokePolygon(new double[]{p0[0], p3[0], b3[0], b0[0]}, new double[]{p0[1], p3[1], b3[1], b0[1]}, 4);
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
            boolean isDark = ThemeManager.getInstance().getCurrentTheme() == ThemeManager.Theme.DARK;
            gc3D.setFill(isDark ? Color.web("#0b0f19") : Color.web("#e2e8f0"));
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

        double cutRatio = slicePlaneSlider != null ? (slicePlaneSlider.getValue() / 100.0) : 1.0;
        int cutXLimit = (int) (GRID_SIZE * cutRatio);
        cutXLimit = (cutXLimit / step) * step;
        cutXLimit = Math.max(step, Math.min(GRID_SIZE - step, cutXLimit));
        boolean isScanning = false;
        boolean isCutPlaneFacingCamera = Math.sin(radAz) >= 0;

        // 2a. Render Subterranean Cut Plane BEFORE terrain when facing away from camera (so terrain masks it)
        if (!isCutPlaneFacingCamera && (cutRatio < 0.99 || isScanning)) {
            drawSubterraneanCutPlane(cutXLimit, cx, cy, scale, radAz, radEl, maxDepthPx, isScanning);
        }

        // 2b. Render Solid 3D Continuous Quad Surface Mesh (Respecting Coupe 3D Cut Plane)
        if (isTerrainVisible) {
            boolean sinPos = Math.sin(radAz) >= 0;
            boolean cosPos = Math.cos(radAz) >= 0;

            if (currentRenderMode == RenderMode.GAMIFIED) {
                // Minecraft-Style Block Terraces (Gradins) System:
                // Pre-compute uniform flat block top elevations to prevent any sloped quads / contour curves
                double[][] blockZ = new double[GRID_SIZE][GRID_SIZE];
                for (int x = 0; x < GRID_SIZE; x += step) {
                    for (int y = 0; y < GRID_SIZE; y += step) {
                        double rawZ = heightGrid[x][y] * 40.0;
                        if (carvedVoxelGrid[x][y]) rawZ -= 15.0;
                        blockZ[x][y] = Math.floor(rawZ / 6.0) * 6.0;
                    }
                }

                int limitX = Math.min(GRID_SIZE - step, cutXLimit);
                int xStart = sinPos ? 0 : limitX - step;
                int xEnd = sinPos ? limitX : -step;
                int xDir = sinPos ? step : -step;

                int yStart = cosPos ? 0 : GRID_SIZE - 1 - step;
                int yEnd = cosPos ? GRID_SIZE - step : -step;
                int yDir = cosPos ? step : -step;

                for (int x = xStart; (xDir > 0 ? x < xEnd : x >= 0); x += xDir) {
                    for (int y = yStart; (yDir > 0 ? y < yEnd : y >= 0); y += yDir) {
                        double zTop = blockZ[x][y];

                        // 1. Top Flat Quad of Block (x, y)
                        double[] p0 = project3DPoint(x, y, zTop, cx, cy, scale, radAz, radEl);
                        double[] p1 = project3DPoint(x + step, y, zTop, cx, cy, scale, radAz, radEl);
                        double[] p2 = project3DPoint(x + step, y + step, zTop, cx, cy, scale, radAz, radEl);
                        double[] p3 = project3DPoint(x, y + step, zTop, cx, cy, scale, radAz, radEl);

                        byte mat = soilLayers[x][y][0];
                        drawVoxelBlockTexture(new double[]{p0[0], p1[0], p2[0], p3[0]}, new double[]{p0[1], p1[1], p2[1], p3[1]}, mat, true, false, false, x, y, (int)zTop);

                        // 2. Front Vertical Side Wall (Facing +Y) if Front Neighbor is lower
                        double zFront = (y + step < GRID_SIZE) ? blockZ[x][y + step] : 0;
                        if (zFront < zTop) {
                            double[] f2 = project3DPoint(x + step, y + step, zFront, cx, cy, scale, radAz, radEl);
                            double[] f3 = project3DPoint(x, y + step, zFront, cx, cy, scale, radAz, radEl);

                            drawVoxelBlockTexture(new double[]{p3[0], p2[0], f2[0], f3[0]}, new double[]{p3[1], p2[1], f2[1], f3[1]}, mat, false, true, false, x, y, (int)zTop);
                        }

                        // 3. Back Vertical Side Wall (Facing -Y) if Back Neighbor is lower
                        double zBack = (y - step >= 0) ? blockZ[x][y - step] : 0;
                        if (zBack < zTop) {
                            double[] b0 = project3DPoint(x, y, zBack, cx, cy, scale, radAz, radEl);
                            double[] b1 = project3DPoint(x + step, y, zBack, cx, cy, scale, radAz, radEl);

                            drawVoxelBlockTexture(new double[]{p0[0], p1[0], b1[0], b0[0]}, new double[]{p0[1], p1[1], b1[1], b0[1]}, mat, false, true, false, x, y, (int)zTop);
                        }

                        // 4. Right Vertical Side Wall (Facing +X) if Right Neighbor is lower
                        double zRight = (x + step < cutXLimit) ? blockZ[x + step][y] : 0;
                        if (zRight < zTop) {
                            double[] r2 = project3DPoint(x + step, y + step, zRight, cx, cy, scale, radAz, radEl);
                            double[] r3 = project3DPoint(x + step, y, zRight, cx, cy, scale, radAz, radEl);

                            drawVoxelBlockTexture(new double[]{p1[0], p2[0], r2[0], r3[0]}, new double[]{p1[1], p2[1], r2[1], r3[1]}, mat, false, true, true, x, y, (int)zTop);
                        }

                        // 5. Left Vertical Side Wall (Facing -X) if Left Neighbor is lower
                        double zLeft = (x - step >= 0) ? blockZ[x - step][y] : 0;
                        if (zLeft < zTop) {
                            double[] l0 = project3DPoint(x, y, zLeft, cx, cy, scale, radAz, radEl);
                            double[] l3 = project3DPoint(x, y + step, zLeft, cx, cy, scale, radAz, radEl);

                            drawVoxelBlockTexture(new double[]{p0[0], p3[0], l3[0], l0[0]}, new double[]{p0[1], p3[1], l3[1], l0[0]}, mat, false, true, true, x, y, (int)zTop);
                        }
                    }
                }
            } else {
                int limitX = Math.min(GRID_SIZE - step, cutXLimit);
                int xStart = sinPos ? 0 : limitX - step;
                int xEnd = sinPos ? limitX : -step;
                int xDir = sinPos ? step : -step;

                int yStart = cosPos ? 0 : GRID_SIZE - 1 - step;
                int yEnd = cosPos ? GRID_SIZE - step : -step;
                int yDir = cosPos ? step : -step;

                for (int x = xStart; (xDir > 0 ? x < xEnd : x >= 0); x += xDir) {
                    for (int y = yStart; (yDir > 0 ? y < yEnd : y >= 0); y += yDir) {
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

                        double humFactor = humidityGrid[x][y][0];
                        Color baseMatCol;
                        if (visibleMat) {
                            if (currentRenderMode == RenderMode.REALISTIC) {
                                baseMatCol = getGaussianSplatSoilColor(x, y);
                            } else {
                                baseMatCol = getMaterialColor(mat);
                            }
                        } else {
                            baseMatCol = Color.web("#1e293b", 0.35);
                        }
                        Color col = baseMatCol.deriveColor(0, 1.0, 1.0 - humFactor * 0.25, 1.0);
                        if (carvedVoxelGrid[x][y]) col = Color.web("#d97706");

                        gc3D.setFill(col);
                        gc3D.fillPolygon(pxs, pys, 4);

                        gc3D.setStroke(col.darker());
                        gc3D.setLineWidth(visibleMat ? 0.3 : 0.1);
                        gc3D.strokePolygon(pxs, pys, 4);

                        // Render Procedural Organic Litter Detritus (Fallen Leaves & Twigs) when Organic Matter is toggled ON
                        if (mat == 5 && visibleMat && (x + y) % 3 == 0) {
                            double zG = z0 + 0.8;
                            double[] pG = project3DPoint(x, y, zG, cx, cy, scale, radAz, radEl);
                            double gSc = (zoom / 7.5);
                            gc3D.setFill(Color.web("#78350f"));
                            gc3D.fillOval(pG[0] - 2 * gSc, pG[1] - 1 * gSc, 4 * gSc, 2.5 * gSc);
                            gc3D.setFill(Color.web("#b45309"));
                            gc3D.fillOval(pG[0] - 1 * gSc, pG[1] - 1.5 * gSc, 3 * gSc, 1.8 * gSc);
                        }

                        // Render Procedural 3D Grass Blades & Wild Dandelion Flowers popping out of ground in REALISTIC mode
                        if (currentRenderMode == RenderMode.REALISTIC && mat == 0 && (x + y) % 5 == 0 && (showVegetationCheck == null || showVegetationCheck.isSelected())) {
                            double zG = z0 + 1.0;
                            double[] pG = project3DPoint(x, y, zG, cx, cy, scale, radAz, radEl);
                            double gSc = (zoom / 7.5);

                            gc3D.setStroke(Color.web("#4ade80")); gc3D.setLineWidth(1.2 * gSc);
                            gc3D.strokeLine(pG[0], pG[1], pG[0] - 2 * gSc, pG[1] - 6 * gSc);
                            gc3D.strokeLine(pG[0], pG[1], pG[0] + 3 * gSc, pG[1] - 7 * gSc);
                            gc3D.strokeLine(pG[0], pG[1], pG[0] + 1 * gSc, pG[1] - 9 * gSc);

                            // Yellow Dandelion / Red Clover Flowers
                            if ((x * 7 + y * 13) % 11 == 0) {
                                gc3D.setFill(Color.web("#facc15")); // Yellow dandelion bloom
                                gc3D.fillOval(pG[0] + 1 * gSc - 1.5, pG[1] - 9 * gSc - 1.5, 3.0 * gSc, 3.0 * gSc);
                            }
                        }
                    }
                }
            }
        }

        // 2c. Render Subterranean Cut Plane AFTER terrain when facing camera (so cut plane renders cleanly on top of background)
        if (isCutPlaneFacingCamera && (cutRatio < 0.99 || isScanning)) {
            drawSubterraneanCutPlane(cutXLimit, cx, cy, scale, radAz, radEl, maxDepthPx, isScanning);
        }

        // 2b. Render FLAT PLANAR WATER SURFACES (Horizontal Water Level for River & Static Pools)
        if (isTerrainVisible) {
            double planarWaterZ = 0.22 * 40.0; // Flat horizontal liquid level elevation
            if (riverCheck != null && riverCheck.isSelected() && riverPath != null && riverPath.size() > 1) {
                double rWidthPx = Math.max(4.0, (riverWidthSlider != null ? riverWidthSlider.getValue() : 120.0) / 20.0 * (zoom / 7.5));
                boolean isWinterRiver = simSeason != null && (simSeason.toLowerCase().contains("hiver") || simSeason.toLowerCase().contains("winter"));
                boolean isDryRiver = simSeason != null && (simSeason.toLowerCase().contains("été") || simSeason.toLowerCase().contains("summer")) && baseHumiditySlider != null && baseHumiditySlider.getValue() < 0.25;

                Color rCol = isWinterRiver ? Color.web("#e0f2fe", 0.95) : (isDryRiver ? Color.web("#78350f", 0.85) : Color.web("#0284c7", 0.90));
                Color rHighlightCol = isWinterRiver ? Color.web("#ffffff", 0.90) : (isDryRiver ? Color.web("#a16207", 0.70) : Color.web("#7dd3fc", 0.75));

                // Draw horizontal flat planar river surface
                gc3D.setStroke(rCol);
                gc3D.setLineWidth(rWidthPx);
                gc3D.beginPath();
                boolean firstPt = true;
                for (int[] rPt : riverPath) {
                    int rx = rPt[0], ry = rPt[1];
                    if (rx > cutXLimit) continue; // Respect Coupe 3D cut plane
                    double rz = Math.min(planarWaterZ, heightGrid[rx][ry] * 40.0 + 1.5);

                    // Occlusion Check: If terrain in front of (rx, ry) is higher than river water level (deep ravine), clip line segment
                    int dyCam = (int) Math.signum(Math.cos(radAz));
                    int dxCam = (int) Math.signum(Math.sin(radAz));
                    int frontX = Math.max(0, Math.min(GRID_SIZE - 1, rx + dxCam));
                    int frontY = Math.max(0, Math.min(GRID_SIZE - 1, ry + dyCam));
                    double frontZ = heightGrid[frontX][frontY] * 40.0;
                    if (frontZ > rz + 6.0) {
                        firstPt = true;
                        continue;
                    }

                    double[] rP = project3DPoint(rx, ry, rz, cx, cy, scale, radAz, radEl);
                    if (firstPt) { gc3D.moveTo(rP[0], rP[1]); firstPt = false; }
                    else gc3D.lineTo(rP[0], rP[1]);
                }
                gc3D.stroke();

                // Specular water sheen highlight / ice sheen / mud crack highlight
                gc3D.setStroke(rHighlightCol);
                gc3D.setLineWidth(Math.max(1.5, rWidthPx * 0.45));
                gc3D.beginPath();
                firstPt = true;
                for (int[] rPt : riverPath) {
                    int rx = rPt[0], ry = rPt[1];
                    if (rx > cutXLimit) continue;
                    double rz = Math.min(planarWaterZ + 0.3, heightGrid[rx][ry] * 40.0 + 1.8);

                    int dyCam = (int) Math.signum(Math.cos(radAz));
                    int dxCam = (int) Math.signum(Math.sin(radAz));
                    int frontX = Math.max(0, Math.min(GRID_SIZE - 1, rx + dxCam));
                    int frontY = Math.max(0, Math.min(GRID_SIZE - 1, ry + dyCam));
                    double frontZ = heightGrid[frontX][frontY] * 40.0;
                    if (frontZ > rz + 6.0) {
                        firstPt = true;
                        continue;
                    }

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
                    if (poolX > cutXLimit) continue;
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
                if (sx > cutXLimit) continue;
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
                if (item.gx > cutXLimit) continue;
                double z = heightGrid[item.gx][item.gy] * 40.0 + 1.2;
                double[] p = project3DPoint(item.gx, item.gy, z, cx, cy, scale, radAz, radEl);
                double sc = item.scale * (zoom / 7.5);

                if (currentRenderMode == RenderMode.GAMIFIED) {
                    switch (item.type) {
                        case 0 -> {
                            gc3D.setFill(Color.web("#22c55e"));
                            gc3D.fillRect(p[0] - 2 * sc, p[1] - 8 * sc, 4 * sc, 8 * sc);
                            gc3D.setFill(Color.web("#86efac"));
                            gc3D.fillRect(p[0] - 1 * sc, p[1] - 9 * sc, 2 * sc, 2 * sc);
                        }
                        case 1, 2 -> {
                            gc3D.setFill(Color.web("#15803d"));
                            gc3D.fillRect(p[0] - 1 * sc, p[1] - 10 * sc, 2 * sc, 10 * sc);
                            gc3D.setFill(item.type == 1 ? Color.web("#facc15") : Color.web("#f43f5e"));
                            gc3D.fillRect(p[0] - 3 * sc, p[1] - 13 * sc, 6 * sc, 5 * sc);
                            gc3D.setStroke(Color.web("#0f172a", 0.9)); gc3D.setLineWidth(1.0);
                            gc3D.strokeRect(p[0] - 3 * sc, p[1] - 13 * sc, 6 * sc, 5 * sc);
                        }
                        case 3, 4 -> {
                            gc3D.setFill(Color.web("#166534"));
                            gc3D.fillRect(p[0] - 4 * sc, p[1] - 6 * sc, 8 * sc, 6 * sc);
                            gc3D.setFill(Color.web("#22c55e"));
                            gc3D.fillRect(p[0] - 3 * sc, p[1] - 8 * sc, 6 * sc, 4 * sc);
                            gc3D.setStroke(Color.web("#0f172a", 0.9)); gc3D.setLineWidth(1.2);
                            gc3D.strokeRect(p[0] - 4 * sc, p[1] - 6 * sc, 8 * sc, 6 * sc);
                        }
                        default -> {
                            gc3D.setFill(Color.web("#64748b"));
                            gc3D.fillRect(p[0] - 3 * sc, p[1] - 3 * sc, 6 * sc, 3 * sc);
                            gc3D.setStroke(Color.web("#0f172a", 0.9)); gc3D.setLineWidth(1.0);
                            gc3D.strokeRect(p[0] - 3 * sc, p[1] - 3 * sc, 6 * sc, 3 * sc);
                        }
                    }
                } else if (item.isCharred) {
                    // Charred / Incinerated flora remnant (ash, burnt carbonized twig)
                    gc3D.setStroke(Color.web("#1e293b"));
                    gc3D.setLineWidth(1.4);
                    gc3D.strokeLine(p[0] - 4 * sc, p[1], p[0] + 4 * sc, p[1] - 2 * sc);
                    gc3D.setFill(Color.web("#334155", 0.8));
                    gc3D.fillOval(p[0] - 3 * sc, p[1] - 2 * sc, 6 * sc, 4 * sc);
                } else {
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
                        case 3: // Polytrichum Moss Cover
                            gc3D.setFill(Color.web("#15803d", 0.85));
                            gc3D.fillOval(p[0] - 5 * sc, p[1] - 2 * sc, 10 * sc, 4 * sc);
                            gc3D.setFill(Color.web("#22c55e", 0.70));
                            gc3D.fillOval(p[0] - 3 * sc, p[1] - 2.5 * sc, 6 * sc, 3 * sc);
                            break;
                        case 4: // Pine Needle Litter Layer
                            gc3D.setFill(Color.web("#854d0e", 0.85));
                            gc3D.fillOval(p[0] - 6 * sc, p[1] - 3 * sc, 12 * sc, 6 * sc);
                            gc3D.setFill(Color.web("#a16207", 0.7));
                            gc3D.fillOval(p[0] - 3 * sc, p[1] - 4 * sc, 8 * sc, 5 * sc);
                            gc3D.setStroke(Color.web("#451a03"));
                            gc3D.setLineWidth(0.8 * sc);
                            gc3D.strokeLine(p[0] - 5 * sc, p[1], p[0] + 5 * sc, p[1] - 2 * sc);
                            break;
                        case 5: // Twig & Micro Debris
                            gc3D.setStroke(Color.web("#451a03"));
                            gc3D.setLineWidth(1.4 * sc);
                            gc3D.strokeLine(p[0] - 5 * sc, p[1], p[0] + 5 * sc, p[1] - 3 * sc);
                            break;
                        case 6: // Pebble / Small Stone
                            gc3D.setFill(Color.web("#94a3b8"));
                            gc3D.fillOval(p[0] - 2 * sc, p[1] - 2 * sc, 4 * sc, 4 * sc);
                            break;
                        case 7: // Leafcutter Foliage & Psilocybe/Mushrooms
                            gc3D.setStroke(Color.web("#e2e8f0"));
                            gc3D.setLineWidth(1.2 * sc);
                            gc3D.strokeLine(p[0], p[1], p[0], p[1] - 8 * sc);
                            gc3D.setFill(Color.web("#fef08a"));
                            gc3D.fillOval(p[0] - 3 * sc, p[1] - 10 * sc, 6 * sc, 4 * sc);
                            gc3D.setFill(Color.web("#ca8a04"));
                            gc3D.fillOval(p[0] - 2 * sc, p[1] - 11 * sc, 4 * sc, 2 * sc);
                            break;
                        case 8: // Fern Understory (Fougère)
                            double fernWindSway = 0.0;
                            if (isSimulationMode) {
                                fernWindSway = Math.sin(System.currentTimeMillis() * 0.003 + item.gx * 0.5) * (4.0 * sc * Math.min(1.5, Math.max(0.1, simWindSpeed / 30.0)));
                            }
                            gc3D.setStroke(Color.web("#15803d"));
                            gc3D.setLineWidth(1.4 * sc);
                            double[] fAngles = {-0.60, -0.30, 0.0, 0.30, 0.60};
                            for (double fAng : fAngles) {
                                double frondLen = 8.5 * sc;
                                double tipX = p[0] + Math.sin(fAng) * frondLen + fernWindSway;
                                double tipY = p[1] - Math.cos(fAng) * frondLen * 0.7;
                                gc3D.strokeLine(p[0], p[1], tipX, tipY);
                                gc3D.setFill(Color.web("#22c55e", 0.90));
                                gc3D.fillOval(tipX - 2.5 * sc, tipY - 2.0 * sc, 5.0 * sc, 4.0 * sc);
                            }
                            gc3D.setFill(Color.web("#14532d"));
                            gc3D.fillOval(p[0] - 2.5 * sc, p[1] - 1.5 * sc, 5.0 * sc, 3.0 * sc);
                            break;
                        case 9: // Rock Crevice / Fissure Rocheuse
                            gc3D.setStroke(Color.web("#0f172a", 0.95));
                            gc3D.setLineWidth(2.0 * sc);
                            gc3D.strokeLine(p[0] - 7 * sc, p[1] + 2 * sc, p[0] - 1 * sc, p[1] - 1 * sc);
                            gc3D.strokeLine(p[0] - 1 * sc, p[1] - 1 * sc, p[0] + 7 * sc, p[1] + 3 * sc);
                            gc3D.setFill(Color.web("#334155"));
                            gc3D.fillOval(p[0] - 3 * sc, p[1] - 1 * sc, 6 * sc, 3 * sc);
                            break;
                    }
                }
            }
        }

        // 3D Bounding box hidden per design preferences

        // Draw Trees & Sub-surface 3D Roots in 3D Viewport
        if (treeCountSlider != null && (showVegetationCheck == null || showVegetationCheck.isSelected())) {
            List<BotanicalTreeData> treeDataList = getBotanicalTreeInstances();

            double sideM = surfaceSizeSlider != null ? surfaceSizeSlider.getValue() : 25.0;
            double pixelsPerMeter = Math.max(1.5, (GRID_SIZE * scale * (zoom / 7.5)) / Math.max(1.0, sideM));

            class TreeInstance {
                double[] p;
                double depth;
                int gx, gy;
                int speciesIdx;
                double ageScale;
            }
            List<TreeInstance> trees = new ArrayList<>();

            for (BotanicalTreeData btd : treeDataList) {
                int gx = btd.gx;
                int gy = btd.gy;
                if (gx > cutXLimit) continue; // Respect Coupe 3D cut plane

                double z = heightGrid[gx][gy] * 40.0;
                double[] p = project3DPoint(gx, gy, z, cx, cy, scale, radAz, radEl);

                // Back-to-front depth calculation relative to 3D view camera
                double depth = (gx - GRID_SIZE / 2.0) * Math.sin(radAz) + (gy - GRID_SIZE / 2.0) * Math.cos(radAz) + (z / 40.0) * Math.sin(radEl);
                TreeInstance ti = new TreeInstance();
                ti.p = p;
                ti.depth = depth;
                ti.gx = gx; ti.gy = gy;
                ti.ageScale = btd.ageScale;
                ti.speciesIdx = btd.speciesIdx;
                trees.add(ti);
            }

            // Sort ascending by base Y screen position: back (higher screen Y / background) rendered FIRST, front (lower screen Y / foreground) rendered LAST on top!
            trees.sort((t1, t2) -> Double.compare(t1.p[1], t2.p[1]));

            for (TreeInstance ti : trees) {
                double[] p = ti.p;
                int speciesIdx = ti.speciesIdx;

                // Render 3D Sub-surface Root Network under tree trunk base
                if (showRootsCheck != null && showRootsCheck.isSelected()) {
                    gc3D.setStroke(Color.web("#78350f", 0.85));
                    double rootRadiusPx = Math.max(6.0, 12.0 * ti.ageScale * (zoom / 7.5));
                    gc3D.setLineWidth(Math.max(1.2, 2.5 * (zoom / 7.5)));
                    for (int rAng = 0; rAng < 5; rAng++) {
                        double ang = rAng * (Math.PI * 2.0 / 5.0) + (ti.gx * 0.3);
                        double rx = p[0] + Math.cos(ang) * rootRadiusPx;
                        double ry = p[1] + Math.sin(ang) * (rootRadiusPx * 0.45) + 4.0;
                        gc3D.strokeLine(p[0], p[1], rx, ry);
                        gc3D.strokeLine(rx, ry, rx + Math.cos(ang + 0.4) * (rootRadiusPx * 0.5), ry + 3.0);
                    }
                }

                double baseTreeHeightM = switch (speciesIdx) {
                    case 0 -> 2.5;   // Bambouseraie
                    case 1 -> 0.8;   // Souche / Bois mort
                    case 2 -> 4.5;   // Bouleau
                    case 3 -> 2.5;   // Cactus Saguaro
                    case 4 -> 5.5;   // Quercus (Chêne)
                    case 5 -> 4.5;   // Pinus (Pin)
                    case 6 -> 4.0;   // Acacia
                    default -> 4.5;
                };

                double canopyRadiusM = switch (speciesIdx) {
                    case 0 -> 0.8;
                    case 1 -> 0.5;
                    case 2 -> 1.4;
                    case 3 -> 0.6;
                    case 4 -> 2.2;
                    case 5 -> 1.5;   // Pin canopy radius 1.5m (diameter 3m, realistic for 25m terrain)
                    case 6 -> 2.5;   // Acacia umbrella canopy radius 2.5m (diameter 5m)
                    default -> 1.8;
                } * ti.ageScale;

                // Bound tree height relative to terrain parcel size so trees remain proportioned
                double treeHeightM = Math.min(baseTreeHeightM * ti.ageScale, Math.max(1.8, sideM * 0.35));

                // Trunk height to branching base (30-45% of tree height)
                double trunkRatio = switch (speciesIdx) {
                    case 0 -> 0.60;
                    case 1 -> 0.40;
                    case 2 -> 0.35;
                    case 3 -> 0.85;
                    case 5 -> 0.30;
                    case 6 -> 0.45;
                    default -> 0.35;
                };
                double trunkH = treeHeightM * pixelsPerMeter * trunkRatio;

                // Trunk width (DBH): ~3.8% of tree height in meters, scaled to pixels (~20-35cm DBH)
                double trunkW = Math.max(2.5, (treeHeightM * 0.038) * pixelsPerMeter);

                // Canopy radius scaled to pixels
                double canopyR = Math.max(5.0, canopyRadiusM * pixelsPerMeter);

                // 1. Dynamic Directional Soft Cast Shadow (ONLY in Simulation mode, zero in 3D Editor mode)
                if (isSimulationMode) {
                    gc3D.setFill(Color.web("#020617", 0.32));
                    gc3D.fillOval(p[0] - canopyR * 0.8 + 6, p[1] - canopyR * 0.2, canopyR * 1.8, canopyR * 0.5);
                }

                // 2. Animated Wind Sway offset (ONLY in Simulation mode, zero in 3D Editor mode)
                double windSwayX = 0.0;
                double windSwayY = 0.0;
                if (isSimulationMode) {
                    double windFactor = Math.min(1.5, Math.max(0.1, simWindSpeed / 30.0));
                    double windAngleRad = switch (simWindDirection.toUpperCase()) {
                        case "N" -> -Math.PI / 2;
                        case "NE" -> -Math.PI / 4;
                        case "E" -> 0.0;
                        case "SE" -> Math.PI / 4;
                        case "S" -> Math.PI / 2;
                        case "SW" -> 3.0 * Math.PI / 4.0;
                        case "W" -> Math.PI;
                        case "NW" -> -3.0 * Math.PI / 4.0;
                        default -> 3.0 * Math.PI / 4.0;
                    };
                    double swayAmp = Math.sin(System.currentTimeMillis() * 0.0022 + p[0] * 0.02) * (canopyR * 0.12 * windFactor);
                    windSwayX = Math.cos(windAngleRad) * swayAmp;
                    windSwayY = Math.sin(windAngleRad) * swayAmp * 0.4;
                }
                double swayX = p[0] + windSwayX;

                // Seasonal State & Palette Determination (0=Spring, 1=Summer, 2=Autumn, 3=Winter)
                int seasonIdx = 1; // Summer default in World Editor mode
                if (isSimulationMode) {
                    if (simSeason != null) {
                        String sName = simSeason.toLowerCase();
                        if (sName.contains("printemps") || sName.contains("spring")) seasonIdx = 0;
                        else if (sName.contains("été") || sName.contains("summer")) seasonIdx = 1;
                        else if (sName.contains("automne") || sName.contains("autumn") || sName.contains("fall")) seasonIdx = 2;
                        else if (sName.contains("hiver") || sName.contains("winter")) seasonIdx = 3;
                    }

                    String bName = presetsCombo != null && presetsCombo.getValue() != null ? presetsCombo.getValue().toUpperCase() : "";
                    boolean isWarmBiome = bName.contains("SAVANNA") || bName.contains("DESERT") || bName.contains("TROPICAL") || bName.contains("EQUATORIAL");

                    // Southern Hemisphere season invert (only for seasonal temperate/boreal biomes)
                    if (!isWarmBiome && latField != null && latField.getText() != null) {
                        String latTxt = latField.getText().trim();
                        if (latTxt.startsWith("-") || latTxt.toLowerCase().contains("s") || latTxt.toLowerCase().contains("sud")) {
                            seasonIdx = (seasonIdx + 2) % 4; // Invert seasons for Southern Hemisphere
                        }
                    }
                }

                if (currentRenderMode == RenderMode.GAMIFIED) {
                    // Minecraft 3D Voxel Block Tree with bark & leaf voxel cubes
                    double worldX = ti.gx;
                    double worldY = ti.gy;
                    double worldZ = heightGrid[ti.gx][ti.gy] * 40.0;
                    double vSize = step * 0.8;

                    // 1. Stacked 3D Trunk Voxel Cubes
                    double trunkHeight3D = Math.max(step * 2.0, (treeHeightM / sideM) * GRID_SIZE * 0.5);
                    double trunkTopZ = worldZ + trunkHeight3D;
                    int trunkCubeCount = Math.max(2, (int)(trunkHeight3D / vSize));
                    double dz = trunkHeight3D / trunkCubeCount;

                    for (int i = 0; i < trunkCubeCount; i++) {
                        drawGamifiedVoxelCube3D(
                            worldX - vSize * 0.5, worldY - vSize * 0.5, worldZ + i * dz,
                            vSize, vSize, dz,
                            Color.web("#78350f"), Color.web("#451a03"),
                            cx, cy, scale, radAz, radEl
                        );
                    }

                    // 2. 3D Voxel Canopy Cubes (3x3x2 voxel grid)
                    Color leafBaseCol = switch (speciesIdx) {
                        case 2 -> Color.web("#86efac"); // Birch
                        case 5 -> Color.web("#14532d"); // Pine
                        case 6 -> Color.web("#a16207"); // Acacia
                        default -> Color.web("#15803d"); // Oak / Default
                    };
                    Color leafSideCol = switch (speciesIdx) {
                        case 2 -> Color.web("#4ade80");
                        case 5 -> Color.web("#064e3b");
                        case 6 -> Color.web("#78350f");
                        default -> Color.web("#166534");
                    };

                    double cSize = vSize * 1.1;
                    for (int bx = -1; bx <= 1; bx++) {
                        for (int by = -1; by <= 1; by++) {
                            for (int bz = 0; bz <= 1; bz++) {
                                if (bx != 0 && by != 0 && bz == 1) continue; // Skip corners on top layer for rounded voxel crown
                                double cxWorld = worldX + bx * cSize - cSize * 0.5;
                                double cyWorld = worldY + by * cSize - cSize * 0.5;
                                double czWorld = trunkTopZ + bz * cSize;

                                drawGamifiedVoxelCube3D(
                                    cxWorld, cyWorld, czWorld,
                                    cSize, cSize, cSize,
                                    leafBaseCol, leafSideCol,
                                    cx, cy, scale, radAz, radEl
                                );
                            }
                        }
                    }
                    continue;
                }

                if (currentRenderMode == RenderMode.SCIENTIFIC) {
                    // SCIENTIFIC MODE: Minimalist structural schematic tree representation
                    gc3D.setStroke(Color.web("#38bdf8", 0.85));
                    gc3D.setLineWidth(Math.max(1.2, trunkW * 0.35));
                    gc3D.strokeLine(p[0], p[1], p[0], p[1] - trunkH);
                    gc3D.setFill(Color.web("#0284c7", 0.7));
                    gc3D.fillOval(p[0] - canopyR * 0.45, p[1] - trunkH - canopyR * 0.45, canopyR * 0.9, canopyR * 0.9);
                    gc3D.setStroke(Color.web("#7dd3fc", 0.9));
                    gc3D.strokeOval(p[0] - canopyR * 0.45, p[1] - trunkH - canopyR * 0.45, canopyR * 0.9, canopyR * 0.9);
                    continue;
                } else if (currentRenderMode == RenderMode.REALISTIC) {
                    // REALISTIC NATURALIST MODE: Maximizing 3D OBJ model asset utilization!
                    loadObjMeshesIfNeeded();
                    double worldX = ti.gx;
                    double worldY = ti.gy;
                    double worldZ = heightGrid[ti.gx][ti.gy] * 40.0;

                    if (speciesIdx == 0 && bambooObjMeshes != null && !bambooObjMeshes.isEmpty()) {
                        drawObjMesh3D(bambooObjMeshes, worldX, worldY, worldZ, 2.5, Color.web("#84cc16"), cx, cy, scale, radAz, radEl);
                        continue;
                    } else if (speciesIdx == 3 && cactusObjMeshes != null && !cactusObjMeshes.isEmpty()) {
                        drawObjMesh3D(cactusObjMeshes, worldX, worldY, worldZ, 0.4, Color.web("#15803d"), cx, cy, scale, radAz, radEl);
                        continue;
                    } else if (speciesIdx == 6 && tropicalObjMeshes != null && !tropicalObjMeshes.isEmpty()) {
                        drawObjMesh3D(tropicalObjMeshes, worldX, worldY, worldZ, 1.8, Color.web("#65a30d"), cx, cy, scale, radAz, radEl);
                        continue;
                    }
                }

                switch (speciesIdx) {
                    case 0 -> { // Bambouseraie (Bamboo Cluster)
                        gc3D.setStroke(Color.web("#84cc16")); gc3D.setLineWidth(Math.max(2.0, trunkW * 0.35));
                        gc3D.strokeLine(p[0] - trunkW * 0.6, p[1], swayX - trunkW * 0.4, p[1] - trunkH * 1.1);
                        gc3D.strokeLine(p[0] + trunkW * 0.6, p[1], swayX + trunkW * 0.7, p[1] - trunkH * 1.25);
                        gc3D.strokeLine(p[0], p[1], swayX, p[1] - trunkH * 1.2);
                        Color fCol = switch (seasonIdx) {
                            case 0 -> Color.web("#84cc16"); // Spring fresh
                            case 2 -> Color.web("#eab308"); // Autumn yellowing
                            case 3 -> Color.web("#a1a1aa"); // Winter frosted
                            default -> Color.web("#4ade80"); // Summer
                        };
                        gc3D.setFill(fCol);
                        gc3D.fillOval(swayX - canopyR * 0.6, p[1] - trunkH * 1.25, canopyR * 1.2, canopyR * 0.7);
                    }
                    case 1 -> { // Souche / Bois Mort & Champignons (Dead Stump)
                        gc3D.setFill(Color.web("#78350f"));
                        gc3D.fillRect(p[0] - trunkW * 0.9, p[1] - trunkH * 0.5, trunkW * 1.8, trunkH * 0.5);
                        gc3D.setFill(Color.web("#451a03"));
                        gc3D.fillOval(p[0] - trunkW * 0.9, p[1] - trunkH * 0.55, trunkW * 1.8, trunkH * 0.25);
                        gc3D.setFill(Color.web("#b45309"));
                        gc3D.fillOval(p[0] - trunkW * 0.6, p[1] - trunkH * 0.52, trunkW * 1.2, trunkH * 0.15);
                        gc3D.setFill(Color.web("#f59e0b"));
                        gc3D.fillOval(p[0] + trunkW * 0.7, p[1] - trunkH * 0.3, trunkW * 0.6, trunkH * 0.12);
                    }
                    case 2 -> { // Bouleau (Betula - Birch with 3D cylindrical trunk)
                        draw3DCylinderTrunk(p[0], p[1], trunkW, trunkH, Color.web("#f8fafc"), Color.web("#475569"), Color.web("#ffffff"));
                        // Bark lenticels (Birch black horizontal stripes)
                        gc3D.setFill(Color.web("#0f172a"));
                        gc3D.fillRect(p[0] - trunkW * 0.45, p[1] - trunkH * 0.7, trunkW * 0.9, Math.max(2.0, trunkW * 0.15));
                        gc3D.fillRect(p[0] - trunkW * 0.45, p[1] - trunkH * 0.4, trunkW * 0.9, Math.max(2.0, trunkW * 0.15));

                        gc3D.setStroke(Color.web("#334155"));
                        gc3D.setLineWidth(Math.max(1.5, trunkW * 0.3));
                        gc3D.strokeLine(p[0], p[1] - trunkH * 0.4, swayX - canopyR * 0.6, p[1] - trunkH * 0.75);
                        gc3D.strokeLine(p[0], p[1] - trunkH * 0.4, swayX + canopyR * 0.6, p[1] - trunkH * 0.75);
                        gc3D.strokeLine(p[0], p[1] - trunkH * 0.65, swayX - canopyR * 0.4, p[1] - trunkH * 0.95);
                        gc3D.strokeLine(p[0], p[1] - trunkH * 0.65, swayX + canopyR * 0.4, p[1] - trunkH * 0.95);

                        if (seasonIdx != 3) {
                            Color bCol1 = switch (seasonIdx) {
                                case 0 -> Color.web("#86efac"); // Spring fresh green
                                case 2 -> Color.web("#fde047"); // Autumn golden yellow
                                default -> Color.web("#4ade80"); // Summer
                            };
                            gc3D.setFill(bCol1);
                            gc3D.fillOval(swayX - canopyR * 0.75, p[1] - trunkH - canopyR * 0.7, canopyR * 1.5, canopyR * 1.2);

                            // Spring Cherry/Birch Blossoms
                            if (seasonIdx == 0) {
                                gc3D.setFill(Color.web("#f472b6", 0.9));
                                gc3D.fillOval(swayX - canopyR * 0.5, p[1] - trunkH - canopyR * 0.8, 5, 5);
                                gc3D.fillOval(swayX + canopyR * 0.3, p[1] - trunkH - canopyR * 0.4, 4, 4);
                            }
                            // Autumn Falling Leaves
                            if (seasonIdx == 2) {
                                double fallShift = (System.currentTimeMillis() * 0.002 + p[0]) % 12.0;
                                gc3D.setFill(Color.web("#eab308", 0.85));
                                gc3D.fillOval(swayX - canopyR * 0.4 + fallShift * 0.5, p[1] - trunkH * 0.6 + fallShift, 3.5, 2.5);
                            }
                        } else {
                            gc3D.setFill(Color.web("#f8fafc", 0.9));
                            gc3D.fillOval(swayX - canopyR * 0.4, p[1] - trunkH - 3, canopyR * 0.8, 5);
                        }
                    }
                    case 3 -> { // Cactus Saguaro
                        draw3DCylinderTrunk(p[0], p[1], trunkW, trunkH, Color.web("#15803d"), Color.web("#14532d"), Color.web("#4ade80"));
                        gc3D.setFill(Color.web("#166534"));
                        gc3D.fillRect(p[0] - trunkH * 0.3, p[1] - trunkH * 0.6, trunkH * 0.6, trunkW * 0.7);
                        gc3D.fillRect(p[0] - trunkH * 0.3, p[1] - trunkH * 0.85, trunkW * 0.7, trunkH * 0.3);
                        gc3D.fillRect(p[0] + trunkH * 0.2, p[1] - trunkH * 0.9, trunkW * 0.7, trunkH * 0.35);
                        if (seasonIdx == 0) { // Spring Flowering Saguaro Crown
                            gc3D.setFill(Color.web("#fef08a"));
                            gc3D.fillOval(p[0] - 3, p[1] - trunkH - 4, 6, 6);
                        }
                    }
                    case 5 -> { // Pin Sylvestre (Scotch Pine - 3D Cylindrical Trunk)
                        draw3DCylinderTrunk(p[0], p[1], trunkW, trunkH, Color.web("#78350f"), Color.web("#3d1a04"), Color.web("#a16207"));
                        gc3D.setStroke(Color.web("#451a03"));
                        gc3D.setLineWidth(Math.max(1.8, trunkW * 0.35));
                        gc3D.strokeLine(p[0], p[1] - trunkH * 0.35, swayX - canopyR * 0.85, p[1] - trunkH * 0.4);
                        gc3D.strokeLine(p[0], p[1] - trunkH * 0.35, swayX + canopyR * 0.85, p[1] - trunkH * 0.4);
                        gc3D.strokeLine(p[0], p[1] - trunkH * 0.65, swayX - canopyR * 0.65, p[1] - trunkH * 0.7);
                        gc3D.strokeLine(p[0], p[1] - trunkH * 0.65, swayX + canopyR * 0.65, p[1] - trunkH * 0.7);
                        gc3D.strokeLine(p[0], p[1] - trunkH * 0.85, swayX - canopyR * 0.45, p[1] - trunkH * 0.88);
                        gc3D.strokeLine(p[0], p[1] - trunkH * 0.85, swayX + canopyR * 0.45, p[1] - trunkH * 0.88);

                        Color pCol1 = seasonIdx == 3 ? Color.web("#14532d") : Color.web("#166534");
                        Color pCol2 = seasonIdx == 3 ? Color.web("#166534") : Color.web("#15803d");
                        Color pCol3 = seasonIdx == 3 ? Color.web("#15803d") : Color.web("#22c55e");

                        double[] pxs1 = {swayX, swayX - canopyR * 0.95, swayX + canopyR * 0.95};
                        double[] pys1 = {p[1] - trunkH - canopyR * 0.7, p[1] - trunkH * 0.35, p[1] - trunkH * 0.35};
                        gc3D.setFill(pCol1); gc3D.fillPolygon(pxs1, pys1, 3);

                        double[] pxs2 = {swayX, swayX - canopyR * 0.75, swayX + canopyR * 0.75};
                        double[] pys2 = {p[1] - trunkH - canopyR * 1.1, p[1] - trunkH * 0.62, p[1] - trunkH * 0.62};
                        gc3D.setFill(pCol2); gc3D.fillPolygon(pxs2, pys2, 3);

                        double[] pxs3 = {swayX, swayX - canopyR * 0.5, swayX + canopyR * 0.5};
                        double[] pys3 = {p[1] - trunkH - canopyR * 1.45, p[1] - trunkH * 0.82, p[1] - trunkH * 0.82};
                        gc3D.setFill(pCol3); gc3D.fillPolygon(pxs3, pys3, 3);

                        String curBiome = presetsCombo != null && presetsCombo.getValue() != null ? presetsCombo.getValue().toUpperCase() : "";
                        boolean isSnowyRegion = curBiome.contains("ALPINE") || curBiome.contains("TAIGA") || curBiome.contains("ARCTIC") || curBiome.contains("BOREAL") || curBiome.contains("PERMAFROST");
                        if (isSimulationMode && seasonIdx == 3 && isSnowyRegion) {
                            gc3D.setFill(Color.web("#f8fafc", 0.95));
                            gc3D.fillPolygon(new double[]{swayX, swayX - canopyR * 0.5, swayX + canopyR * 0.5}, new double[]{p[1] - trunkH - canopyR * 1.45, p[1] - trunkH - canopyR * 1.15, p[1] - trunkH - canopyR * 1.15}, 3);
                        }
                    }
                    case 6 -> { // Acacia (Savanna Parasol - 3D Cylindrical Trunk)
                        draw3DCylinderTrunk(p[0], p[1], trunkW, trunkH, Color.web("#78350f"), Color.web("#3d1a04"), Color.web("#a16207"));
                        gc3D.setStroke(Color.web("#451a03"));
                        gc3D.setLineWidth(Math.max(2.0, trunkW * 0.35));
                        gc3D.strokeLine(p[0], p[1] - trunkH * 0.5, swayX - canopyR * 0.85, p[1] - trunkH * 0.95);
                        gc3D.strokeLine(p[0], p[1] - trunkH * 0.5, swayX + canopyR * 0.85, p[1] - trunkH * 0.95);
                        gc3D.strokeLine(swayX - canopyR * 0.4, p[1] - trunkH * 0.7, swayX - canopyR * 1.2, p[1] - trunkH * 1.05);
                        gc3D.strokeLine(swayX + canopyR * 0.4, p[1] - trunkH * 0.7, swayX + canopyR * 1.2, p[1] - trunkH * 1.05);

                        Color aColBase = (seasonIdx == 2 || seasonIdx == 3) ? Color.web("#a16207") : Color.web("#4d7c0f");
                        Color aColTop  = (seasonIdx == 2 || seasonIdx == 3) ? Color.web("#eab308") : Color.web("#65a30d");

                        gc3D.setFill(aColBase);
                        gc3D.fillOval(swayX - canopyR * 1.1, p[1] - trunkH * 0.95 - canopyR * 0.45, canopyR * 2.2, canopyR * 0.75);
                        gc3D.setFill(aColTop);
                        gc3D.fillOval(swayX - canopyR * 1.2, p[1] - trunkH * 1.05 - canopyR * 0.50, canopyR * 2.4, canopyR * 0.85);
                        gc3D.setFill(Color.web("#84cc16", 0.65));
                        gc3D.fillOval(swayX - canopyR * 0.8, p[1] - trunkH * 1.15 - canopyR * 0.40, canopyR * 1.6, canopyR * 0.65);
                    }
                    default -> { // Chêne Quercus (Oak Tree - 3D Cylindrical Trunk)
                        draw3DCylinderTrunk(p[0], p[1], trunkW, trunkH, Color.web("#78350f"), Color.web("#3d1a04"), Color.web("#a16207"));
                        gc3D.setStroke(Color.web("#451a03"));
                        gc3D.setLineWidth(Math.max(2.0, trunkW * 0.4));
                        gc3D.strokeLine(p[0], p[1] - trunkH * 0.35, swayX - canopyR * 0.75, p[1] - trunkH * 0.7);
                        gc3D.strokeLine(p[0], p[1] - trunkH * 0.35, swayX + canopyR * 0.75, p[1] - trunkH * 0.7);
                        gc3D.strokeLine(p[0], p[1] - trunkH * 0.55, swayX - canopyR * 0.45, p[1] - trunkH * 0.95);
                        gc3D.strokeLine(p[0], p[1] - trunkH * 0.55, swayX + canopyR * 0.45, p[1] - trunkH * 0.95);
                        gc3D.strokeLine(p[0], p[1] - trunkH * 0.7, swayX, p[1] - trunkH * 1.15);

                        if (seasonIdx == 0) { // Spring Blossoms
                            gc3D.setFill(Color.web("#16a34a"));
                            gc3D.fillOval(swayX - canopyR, p[1] - trunkH - canopyR * 0.9, canopyR * 2.0, canopyR * 1.3);
                            gc3D.setFill(Color.web("#4ade80"));
                            gc3D.fillOval(swayX - canopyR * 0.8, p[1] - trunkH - canopyR * 1.15, canopyR * 1.6, canopyR * 1.1);
                            // Spring Blossom Flecks
                            gc3D.setFill(Color.web("#f472b6", 0.85));
                            gc3D.fillOval(swayX - canopyR * 0.4, p[1] - trunkH - canopyR * 1.2, 5, 5);
                            gc3D.fillOval(swayX + canopyR * 0.2, p[1] - trunkH - canopyR * 0.8, 4, 4);
                        } else if (seasonIdx == 1) { // Summer
                            gc3D.setFill(Color.web("#14532d"));
                            gc3D.fillOval(swayX - canopyR * 1.05, p[1] - trunkH - canopyR * 0.9, canopyR * 2.1, canopyR * 1.3);
                            gc3D.setFill(Color.web("#166534"));
                            gc3D.fillOval(swayX - canopyR * 0.85, p[1] - trunkH - canopyR * 1.15, canopyR * 1.7, canopyR * 1.1);
                            gc3D.setFill(Color.web("#15803d"));
                            gc3D.fillOval(swayX - canopyR * 0.55, p[1] - trunkH - canopyR * 1.3, canopyR * 1.1, canopyR * 0.9);
                        } else if (seasonIdx == 2) { // Autumn Leaves & Falling Leaves
                            gc3D.setFill(Color.web("#b45309"));
                            gc3D.fillOval(swayX - canopyR * 1.05, p[1] - trunkH - canopyR * 0.9, canopyR * 2.1, canopyR * 1.3);
                            gc3D.setFill(Color.web("#d97706"));
                            gc3D.fillOval(swayX - canopyR * 0.85, p[1] - trunkH - canopyR * 1.15, canopyR * 1.7, canopyR * 1.1);
                            gc3D.setFill(Color.web("#ef4444", 0.85));
                            gc3D.fillOval(swayX - canopyR * 0.55, p[1] - trunkH - canopyR * 1.3, canopyR * 1.1, canopyR * 0.9);
                            // Falling Autumn Leaf Animation
                            double fallShift = (System.currentTimeMillis() * 0.0022 + p[0]) % 15.0;
                            gc3D.setFill(Color.web("#dc2626", 0.9));
                            gc3D.fillOval(swayX - canopyR * 0.5 + Math.sin(fallShift) * 8.0, p[1] - trunkH * 0.5 + fallShift, 4, 3);
                            gc3D.setFill(Color.web("#f59e0b", 0.9));
                            gc3D.fillOval(swayX + canopyR * 0.3 - Math.sin(fallShift) * 6.0, p[1] - trunkH * 0.7 + fallShift, 3.5, 2.5);
                        } else { // Winter
                            gc3D.setFill(Color.web("#f8fafc", 0.9));
                            gc3D.fillOval(swayX - canopyR * 0.6, p[1] - trunkH * 0.7 - 4, canopyR * 0.6, 5);
                            gc3D.fillOval(swayX, p[1] - trunkH * 1.15 - 4, canopyR * 0.6, 5);
                        }
                    }
                }

                if (isSimulationMode && currentRenderMode == RenderMode.REALISTIC && speciesIdx != 1) {
                    int pTexIdx = (speciesIdx % 6) + 1;
                    Image plantAlphaImg = getPlantTexture("/textures/Plant alpha " + pTexIdx + ".png");
                    if (plantAlphaImg != null) {
                        gc3D.drawImage(plantAlphaImg, swayX - canopyR * 0.85, p[1] - trunkH - canopyR * 0.85, canopyR * 1.7, canopyR * 1.7);
                    }
                }
            }
        }

        // Draw 3D Overlays for Underground Galleries, Nests, Colony Insects, Pheromones, and Weather (Simulation Mode ONLY)
        if (isSimulationMode && (isGalleriesVisible || !isTerrainVisible)) {
            drawGalleriesOverlay3D(cx, cy, scale, radAz, radEl);
        }
        if (isSimulationMode && isColonyVisible) drawColonyOverlay3D(cx, cy, scale, radAz, radEl);
        if (isSimulationMode && isPheromonesVisible) drawPheromoneOverlay3D(cx, cy, scale, radAz, radEl);
        if (isSimulationMode && isWeatherVisible) drawWeatherOverlay3D(w, h);

        // Draw 3D Hover Info Overlay on top of 3D Canvas
        draw3DHoverOverlay(w, h, cx, cy, scale, radAz, radEl);

        // Draw Metric Scale Bar in bottom right of 3D Canvas
        drawMetricScaleBar3D(w, h, cx, cy, scale, radAz, radEl);

        // Nest interior legend is displayed in the sidebar legend panel under substrates
    }

    private List<org.swarmforge.client.util.ObjModelLoader.ObjMesh> bambooObjMeshes;
    private List<org.swarmforge.client.util.ObjModelLoader.ObjMesh> cactusObjMeshes;
    private List<org.swarmforge.client.util.ObjModelLoader.ObjMesh> tropicalObjMeshes;

    private void loadObjMeshesIfNeeded() {
        if (bambooObjMeshes == null) {
            bambooObjMeshes = org.swarmforge.client.util.ObjModelLoader.loadObjModel("/models/bamboo_set.obj");
            cactusObjMeshes = org.swarmforge.client.util.ObjModelLoader.loadObjModel("/models/cactus.obj");
            tropicalObjMeshes = org.swarmforge.client.util.ObjModelLoader.loadObjModel("/models/tropical_plants.obj");
        }
    }

    private void drawObjMesh3D(List<org.swarmforge.client.util.ObjModelLoader.ObjMesh> objMeshes, double wx, double wy, double wz, double scaleObj, Color baseColor, double cx, double cy, double scale, double radAz, double radEl) {
        if (objMeshes == null || objMeshes.isEmpty()) return;

        for (org.swarmforge.client.util.ObjModelLoader.ObjMesh mesh : objMeshes) {
            if (mesh.vertices == null || mesh.vertices.isEmpty() || mesh.faces == null || mesh.faces.isEmpty()) continue;

            Color meshCol = baseColor;
            if (mesh.name != null) {
                String nameLower = mesh.name.toLowerCase();
                if (nameLower.contains("shrub") || nameLower.contains("plant") || nameLower.contains("leaf")) {
                    meshCol = baseColor.brighter();
                } else if (nameLower.contains("stick") || nameLower.contains("trunk") || nameLower.contains("wood")) {
                    meshCol = Color.web("#78350f");
                }
            }

            gc3D.setFill(meshCol);
            gc3D.setStroke(meshCol.darker());
            gc3D.setLineWidth(0.35);

            int maxFaces = Math.min(160, mesh.faces.size());
            int step = Math.max(1, mesh.faces.size() / maxFaces);

            for (int i = 0; i < mesh.faces.size(); i += step) {
                int[] face = mesh.faces.get(i);
                if (face.length < 3) continue;

                double[] xPts = new double[face.length];
                double[] yPts = new double[face.length];
                boolean valid = true;

                for (int k = 0; k < face.length; k++) {
                    int vIdx = face[k];
                    if (vIdx >= mesh.vertices.size()) { valid = false; break; }
                    float[] v = mesh.vertices.get(vIdx);

                    double vx = wx + v[0] * scaleObj;
                    double vy = wy + v[1] * scaleObj;
                    double vz = wz + v[2] * scaleObj;

                    double[] p = project3DPoint(vx, vy, vz, cx, cy, scale, radAz, radEl);
                    xPts[k] = p[0];
                    yPts[k] = p[1];
                }

                if (valid) {
                    gc3D.fillPolygon(xPts, yPts, face.length);
                    gc3D.strokePolygon(xPts, yPts, face.length);
                }
            }
        }
    }

    private void draw3DCylinderTrunk(double px, double py, double trunkW, double trunkH, Color mainCol, Color shadowCol, Color highlightCol) {
        double halfW = Math.max(1.2, trunkW / 2.0);
        double topY = py - trunkH;
        double capH = Math.max(1.8, trunkW * 0.35);

        // 1. Ground Anchor Ring (3D Cylinder Base Oval on terrain ground)
        gc3D.setFill(shadowCol.darker());
        gc3D.fillOval(px - halfW * 1.15, py - capH * 0.4, trunkW * 1.15, capH * 0.9);

        // 2. 3D Cylindrical Trunk Body (Shaded Curved Surface with Linear Gradient shading)
        LinearGradient trunkGrad = new LinearGradient(
            0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, shadowCol),
            new Stop(0.35, mainCol),
            new Stop(0.75, highlightCol),
            new Stop(1.0, shadowCol.darker())
        );
        gc3D.setFill(trunkGrad);
        gc3D.fillRect(px - halfW, topY, trunkW, trunkH);

        // 3. Bottom Base Curve
        gc3D.fillOval(px - halfW, py - capH * 0.5, trunkW, capH);

        // 4. Top Cap Oval
        gc3D.setFill(mainCol.brighter());
        gc3D.fillOval(px - halfW, topY - capH * 0.5, trunkW, capH);

        // 5. 3D Trunk Bark Contour Outline
        gc3D.setStroke(shadowCol.darker());
        gc3D.setLineWidth(Math.max(0.8, trunkW * 0.12));
        gc3D.strokeLine(px - halfW, py, px - halfW, topY);
        gc3D.strokeLine(px + halfW, py, px + halfW, topY);
        // Base rim facing front: stroke ONLY the bottom semi-ellipse (Arc from 180 to 360 degrees) so no ellipse line crosses through the trunk!
        gc3D.strokeArc(px - halfW, py - capH * 0.5, trunkW, capH, 180, 180, javafx.scene.shape.ArcType.OPEN);
        // Top cap rim facing camera
        gc3D.strokeOval(px - halfW, topY - capH * 0.5, trunkW, capH);
    }

    private int pickBotanicalTreeSpecies(Random rand) {
        int oak = oakPctSpinner != null ? oakPctSpinner.getValue() : 45;
        int pine = pinePctSpinner != null ? pinePctSpinner.getValue() : 20;
        int acacia = acaciaPctSpinner != null ? acaciaPctSpinner.getValue() : 10;
        int birch = birchPctSpinner != null ? birchPctSpinner.getValue() : 10;
        int cactus = cactusPctSpinner != null ? cactusPctSpinner.getValue() : 0;
        int bamboo = bambooPctSpinner != null ? bambooPctSpinner.getValue() : 5;
        int deadWood = deadWoodPctSpinner != null ? deadWoodPctSpinner.getValue() : 10;

        int total = oak + pine + acacia + birch + cactus + bamboo + deadWood;
        if (total <= 0) return 4;

        int roll = rand.nextInt(total);
        int accum = 0;

        if (roll < (accum += oak)) return 4;
        if (roll < (accum += pine)) return 5;
        if (roll < (accum += acacia)) return 6;
        if (roll < (accum += birch)) return 2;
        if (roll < (accum += cactus)) return 3;
        if (roll < (accum += bamboo)) return 0;
        if (roll < (accum += deadWood)) return 1;
        return 4;
    }

    private void drawSubterraneanCutPlane(int cutXLimit, double cx, double cy, double scale, double radAz, double radEl, double maxDepthPx, boolean isScanning) {
        if (cutXLimit <= 0 || cutXLimit >= GRID_SIZE) return;

        double stepY = 2;
        double layerDepthPx = maxDepthPx / SOIL_DEPTH;
        boolean showInclusions = showGravelInclusionsCheck != null && showGravelInclusionsCheck.isSelected();

        for (int y = 0; y < GRID_SIZE - (int)stepY; y += (int)stepY) {
            double surfZ0 = heightGrid[cutXLimit][y] * 40.0;
            double surfZ1 = heightGrid[cutXLimit][y + (int)stepY] * 40.0;
            if (carvedVoxelGrid[cutXLimit][y]) surfZ0 -= 15.0;
            if (carvedVoxelGrid[cutXLimit][y + (int)stepY]) surfZ1 -= 15.0;

            double refZ0 = 32.0;
            double refZ1 = 32.0;
            for (int d = 0; d < SOIL_DEPTH; d++) {
                double topZ0 = Math.min(surfZ0, refZ0 - d * layerDepthPx);
                double topZ1 = Math.min(surfZ1, refZ1 - d * layerDepthPx);
                double botZ0 = Math.min(surfZ0, refZ0 - (d + 1) * layerDepthPx);
                double botZ1 = Math.min(surfZ1, refZ1 - (d + 1) * layerDepthPx);

                if (topZ0 <= botZ0 && topZ1 <= botZ1) continue;

                double[] pTop0 = project3DPoint(cutXLimit, y, topZ0, cx, cy, scale, radAz, radEl);
                double[] pTop1 = project3DPoint(cutXLimit, y + (int)stepY, topZ1, cx, cy, scale, radAz, radEl);
                double[] pBot1 = project3DPoint(cutXLimit, y + (int)stepY, botZ1, cx, cy, scale, radAz, radEl);
                double[] pBot0 = project3DPoint(cutXLimit, y, botZ0, cx, cy, scale, radAz, radEl);

                double[] pxs = {pTop0[0], pTop1[0], pBot1[0], pBot0[0]};
                double[] pys = {pTop0[1], pTop1[1], pBot1[1], pBot0[1]};

                byte mat = soilLayers[cutXLimit][y][d];
                boolean isVoid = voidGrid[cutXLimit][y][d];
                Color matCol;
                if (currentRenderMode == RenderMode.GAMIFIED) {
                    if (isVoid) {
                        gc3D.setFill(Color.web("#090d16"));
                        gc3D.fillPolygon(pxs, pys, 4);
                        gc3D.setStroke(Color.web("#0f172a", 0.95));
                        gc3D.setLineWidth(1.4);
                        gc3D.strokePolygon(pxs, pys, 4);
                    } else {
                        drawVoxelBlockTexture(pxs, pys, mat, false, true, false, cutXLimit, y, d);
                    }
                    continue;
                } else if (isVoid) {
                    matCol = Color.web("#090d16");
                } else if (!isMaterialVisible(mat)) {
                    matCol = Color.web("#1e293b", 0.35);
                } else {
                    matCol = getMaterialColor(mat).deriveColor(0, 1.15, 1.1, 1.0);
                }

                if (isVoid) {
                    if (showGalleriesCheck == null || showGalleriesCheck.isSelected()) {
                        gc3D.setFill(matCol);
                        gc3D.fillPolygon(pxs, pys, 4);
                    }
                } else {
                    gc3D.setFill(matCol);
                    gc3D.fillPolygon(pxs, pys, 4);
                }

                // Render gravel & pebble inclusions on 3D cutplane
                double cutInclNoise = valueNoise3D(cutXLimit * 0.35, y * 0.35, d * 0.35);
                if (showInclusions && !isVoid && isMaterialVisible(mat) && cutInclNoise > 0.68) {
                    double midX = (pTop0[0] + pTop1[0] + pBot1[0] + pBot0[0]) / 4.0;
                    double midY = (pTop0[1] + pTop1[1] + pBot1[1] + pBot0[1]) / 4.0;
                    double sz = Math.max(1.5, Math.abs(pTop1[0] - pTop0[0]) * 0.35);

                    if (mat == 3) {
                        gc3D.setFill(Color.web("#e2e8f0"));
                        gc3D.fillOval(midX - sz * 0.4, midY - sz * 0.3, sz * 0.8, sz * 0.6);
                    } else if (mat == 4) {
                        gc3D.setFill(Color.web("#cbd5e1"));
                        gc3D.fillOval(midX - sz * 0.3, midY - sz * 0.3, sz * 0.6, sz * 0.5);
                    } else if (mat == 2) {
                        gc3D.setFill(Color.web("#7c2d12"));
                        gc3D.fillRect(midX - sz * 0.4, midY - sz * 0.3, sz * 0.8, sz * 0.5);
                    } else if (mat == 1) {
                        gc3D.setFill(Color.web("#fef08a"));
                        gc3D.fillOval(midX - sz * 0.3, midY - sz * 0.3, sz * 0.6, sz * 0.6);
                    }
                }

                // Render 3D Scanner Humidity Overlay
                if (showHumidityCheck != null && showHumidityCheck.isSelected() && humidityGrid != null) {
                    float hum = humidityGrid[cutXLimit][y][d];
                    gc3D.setFill(Color.web("#0284c7", Math.min(0.70, hum * 0.50 + 0.05)));
                    gc3D.fillPolygon(pxs, pys, 4);
                }

                // Render 3D Scanner pH Level Overlay
                if (showPhCheck != null && showPhCheck.isSelected() && phGrid != null) {
                    float ph = phGrid[cutXLimit][y][d] > 0 ? phGrid[cutXLimit][y][d] : 6.5f;
                    Color phColor;
                    if (ph < 6.5f) {
                        phColor = Color.web("#eab308", Math.min(0.65, (6.5 - ph) * 0.22 + 0.15));
                    } else if (ph > 7.5f) {
                        phColor = Color.web("#1e40af", Math.min(0.65, (ph - 7.5) * 0.22 + 0.15));
                    } else {
                        phColor = Color.web("#22c55e", 0.35);
                    }
                    gc3D.setFill(phColor);
                    gc3D.fillPolygon(pxs, pys, 4);
                }

                // Render 3D Scanner Root System Overlay
                if (showRootsCheck != null && showRootsCheck.isSelected() && rootGrid != null && rootGrid[cutXLimit][y][d] > 0.02f) {
                    double midX = (pTop0[0] + pTop1[0] + pBot1[0] + pBot0[0]) / 4.0;
                    double midY = (pTop0[1] + pTop1[1] + pBot1[1] + pBot0[1]) / 4.0;
                    double rSc = Math.max(1.0, rootGrid[cutXLimit][y][d] * 2.2 * (zoom / 7.5));
                    gc3D.setStroke(Color.web("#78350f", Math.min(0.95, rootGrid[cutXLimit][y][d] * 1.3)));
                    gc3D.setLineWidth(rSc);
                    gc3D.strokeLine(pTop0[0], pTop0[1], pBot1[0], pBot1[1]);
                    gc3D.strokeLine(pTop1[0], pTop1[1], pBot0[0], pBot0[1]);
                    gc3D.setFill(Color.web("#451a03"));
                    gc3D.fillOval(midX - rSc, midY - rSc, rSc * 2, rSc * 2);
                }

                gc3D.setStroke(matCol.darker());
                gc3D.setLineWidth(0.3);
                gc3D.strokePolygon(pxs, pys, 4);
            }
        }

        if (isScanning) {
            double scanZTop = heightGrid[cutXLimit][GRID_SIZE / 2] * 40.0 + 10.0;
            double scanZBot = scanZTop - maxDepthPx - 10.0;

            double[] topStart = project3DPoint(cutXLimit, 0, scanZTop, cx, cy, scale, radAz, radEl);
            double[] topEnd = project3DPoint(cutXLimit, GRID_SIZE, scanZTop, cx, cy, scale, radAz, radEl);
            double[] botStart = project3DPoint(cutXLimit, 0, scanZBot, cx, cy, scale, radAz, radEl);
            double[] botEnd = project3DPoint(cutXLimit, GRID_SIZE, scanZBot, cx, cy, scale, radAz, radEl);

            gc3D.setStroke(Color.web("#22c55e", 0.75));
            gc3D.setLineWidth(2.2);
            gc3D.strokeLine(topStart[0], topStart[1], topEnd[0], topEnd[1]);
            gc3D.strokeLine(botStart[0], botStart[1], botEnd[0], botEnd[1]);

            gc3D.setStroke(Color.web("#4ade80", 0.45));
            gc3D.setLineWidth(1.0);
            int gridLines = 8;
            for (int i = 0; i <= gridLines; i++) {
                double t = (double) i / gridLines;
                double lx1 = topStart[0] + t * (topEnd[0] - topStart[0]);
                double ly1 = topStart[1] + t * (topEnd[1] - topStart[1]);
                double lx2 = botStart[0] + t * (botEnd[0] - botStart[0]);
                double ly2 = botStart[1] + t * (botEnd[1] - botStart[1]);
                gc3D.strokeLine(lx1, ly1, lx2, ly2);
            }
        }
    }

    private void draw3DHoverOverlay(double w, double h, double cx, double cy, double scale, double radAz, double radEl) {
        if (hover3DCell == null) return;
        int gx = hover3DCell[0];
        int gy = hover3DCell[1];
        int gd = hover3DCell.length > 2 ? hover3DCell[2] : 0;
        if (gx < 0 || gx >= GRID_SIZE || gy < 0 || gy >= GRID_SIZE) return;

        double cutRatio = slicePlaneSlider != null ? (slicePlaneSlider.getValue() / 100.0) : 1.0;
        int cutXLimit = (int) (GRID_SIZE * cutRatio);
        double targetDepthVal = depthSlider != null ? depthSlider.getValue() : 1.5;
        double maxDepthPx = targetDepthVal * 22.0;

        // If hovering on subterranean cut plane wall (gx == cutXLimit and cut plane is active)
        if (gx == cutXLimit && cutXLimit > 0 && cutXLimit < GRID_SIZE) {
            double stepY = 2;
            double layerDepthPx = maxDepthPx / SOIL_DEPTH;
            double surfZ0 = heightGrid[gx][gy] * 40.0;
            double surfZ1 = heightGrid[gx][Math.min(GRID_SIZE - 1, gy + (int)stepY)] * 40.0;
            if (carvedVoxelGrid[gx][gy]) surfZ0 -= 15.0;

            double refZ0 = 32.0;
            double refZ1 = 32.0;
            double topZ0 = Math.min(surfZ0, refZ0 - gd * layerDepthPx);
            double topZ1 = Math.min(surfZ1, refZ1 - gd * layerDepthPx);
            double botZ0 = Math.min(surfZ0, refZ0 - (gd + 1) * layerDepthPx);
            double botZ1 = Math.min(surfZ1, refZ1 - (gd + 1) * layerDepthPx);

            double[] pTop0 = project3DPoint(gx, gy, topZ0, cx, cy, scale, radAz, radEl);
            double[] pTop1 = project3DPoint(gx, Math.min(GRID_SIZE - 1, gy + (int)stepY), topZ1, cx, cy, scale, radAz, radEl);
            double[] pBot1 = project3DPoint(gx, Math.min(GRID_SIZE - 1, gy + (int)stepY), botZ1, cx, cy, scale, radAz, radEl);
            double[] pBot0 = project3DPoint(gx, gy, botZ0, cx, cy, scale, radAz, radEl);

            double[] pxs = {pTop0[0], pTop1[0], pBot1[0], pBot0[0]};
            double[] pys = {pTop0[1], pTop1[1], pBot1[1], pBot0[1]};

            gc3D.setFill(Color.web("#38bdf8", 0.65));
            gc3D.fillPolygon(pxs, pys, 4);
            gc3D.setStroke(Color.web("#7dd3fc"));
            gc3D.setLineWidth(2.2);
            gc3D.strokePolygon(pxs, pys, 4);
        } else {
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
        }

        // Draw HUD Box on 3D Viewport
        double boxW = 270.0;
        double boxH = 145.0;
        double hx = Math.max(10, Math.min(w - boxW - 10, hoverMX + 15));
        double hy = Math.max(10, Math.min(h - boxH - 10, hoverMY - 20));

        gc3D.setFill(Color.web("rgba(15, 23, 42, 0.95)"));
        gc3D.fillRoundRect(hx, hy, boxW, boxH, 8, 8);
        gc3D.setStroke(gd > 0 ? Color.web("#38bdf8", 0.9) : Color.web("#f59e0b", 0.85));
        gc3D.setLineWidth(1.4);
        gc3D.strokeRoundRect(hx, hy, boxW, boxH, 8, 8);

        double sM = surfaceSizeSlider != null ? surfaceSizeSlider.getValue() : 25.0;
        double dM = depthSlider != null ? depthSlider.getValue() : 3.0;
        double surfAltM = heightGrid[gx][gy] * dM;
        double depthM = ((double) gd / (double) (SOIL_DEPTH - 1)) * dM;
        double voxelAltM = surfAltM - depthM;

        int humPct = (int) (humidityGrid[gx][gy][gd] * 100);
        byte mat = soilLayers[gx][gy][gd];
        boolean isVoid = voidGrid[gx][gy][gd] || carvedVoxelGrid[gx][gy];

        String matName;
        if (isVoid) {
            matName = "🕳️ Excavated Gallery / Cavity";
        } else {
            matName = switch (mat) {
                case 0 -> "Humus (Topsoil)";
                case 1 -> "Xeric Sand";
                case 2 -> "Argile Limoneuse";
                case 3 -> "Bedrock / Rock";
                case 4 -> "Gravier & Cailloutis";
                case 5 -> "Organic Litter";
                case 6 -> "Galerie Souterraine";
                case 7 -> "Chambre de Couvain";
                case 8 -> "Chambre Royale";
                default -> "Substrat Terrestre";
            };
        }
        boolean isRiver = isNearRiver(gx, gy, 1);
        boolean hasTree = isForestArea(gx, gy);
        double tempC = tempGrid[gx][gy][gd];
        if (tempC == 0) {
            tempC = 20.0 - (((double) (SOIL_DEPTH - 1 - gd) / (double) (SOIL_DEPTH - 1)) * 3.5);
        }
        int rootPct = (int) (rootGrid[gx][gy][gd] * 100);
        float phVal = phGrid[gx][gy][gd] > 0 ? phGrid[gx][gy][gd] : 6.5f;

        gc3D.setFill(gd > 0 ? Color.web("#38bdf8") : Color.web("#fbbf24"));
        gc3D.setFont(Font.font("System", javafx.scene.text.FontWeight.BOLD, 11));
        gc3D.fillText(String.format(Locale.US, "📍 Voxel [%d, %d, Profondeur: %d/%d]", gx, gy, gd, SOIL_DEPTH - 1), hx + 10, hy + 18);

        gc3D.setFill(Color.web("#f8fafc"));
        gc3D.setFont(Font.font("System", 10));
        gc3D.fillText(String.format(Locale.US, "📏 Alt: %.2fm | Profondeur: -%.2fm (%dcm)", voxelAltM, depthM, (int)(depthM * 100)), hx + 10, hy + 36);
        gc3D.fillText(String.format(Locale.US, "🟤 Substrat: %s", matName), hx + 10, hy + 52);
        gc3D.fillText(String.format(Locale.US, "💧 Humidity: %d%% | 🌡️ Temp: %.1f°C", humPct, tempC), hx + 10, hy + 68);
        gc3D.fillText(String.format(Locale.US, "🧪 pH: %.1f | 🌿 Root Density: %d%%", phVal, rootPct), hx + 10, hy + 84);
        gc3D.fillText(String.format(Locale.US, "🌲 Environment: %s", hasTree ? "🌳 Tree Canopy" : (isRiver ? "💧 River Flow" : "🌾 Terrestrial")), hx + 10, hy + 100);
        gc3D.fillText(String.format(Locale.US, "🐜 Insect Activity: %s", isSimulationMode ? (followedAnt != null ? "🎯 Ant Tracking Active..." : "Patrolling Workers (4/m²)") : "Editor Mode - No Insects"), hx + 10, hy + 116);
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
        if (!isSimulationMode) return;
        if (activeSimulation != null && !activeSimulation.getColonies().isEmpty()) {
            for (org.swarmforge.core.domain.Colony colony : activeSimulation.getColonies()) {
                for (org.swarmforge.core.domain.Individual ind : colony.getLivingIndividuals()) {
                    double ax = ind.getX();
                    double ay = ind.getY();
                    double az = ind.getZ();

                    double gx = Math.max(0.0, Math.min(GRID_SIZE - 1.0, (ax / (float) Math.max(1, activeSimulation.getTerrarium().getWidth())) * GRID_SIZE));
                    double gy = Math.max(0.0, Math.min(GRID_SIZE - 1.0, (ay / (float) Math.max(1, activeSimulation.getTerrarium().getHeight())) * GRID_SIZE));
                    int igx = (int) gx;
                    int igy = (int) gy;

                    // Occlusion check: hide subterranean ants (az < 0) when inside solid un-cut terrain
                    double cutRatio = slicePlaneSlider != null ? (slicePlaneSlider.getValue() / 100.0) : 1.0;
                    int cutXLimit = (int) (GRID_SIZE * cutRatio);
                    boolean isTranslucent = showTranslucentVolumetricModeCheck != null && showTranslucentVolumetricModeCheck.isSelected();
                    boolean isAntExposed = !isTerrainVisible || az >= 0 || igx >= cutXLimit || isTranslucent;
                    if (!isAntExposed) continue;

                    double gz = heightGrid[igx][igy] * 40.0 + az * 2.0 + 1.5;

                    double[] p = project3DPoint(gx, gy, gz, cx, cy, scale, radAz, radEl);
                    double sideM = surfaceSizeSlider != null ? surfaceSizeSlider.getValue() : 25.0;
                    double canvasW = canvas3D != null && canvas3D.getWidth() > 0 ? canvas3D.getWidth() : 800.0;
                    double pixelsPerMeter = (canvasW * 0.45) / sideM;
                    double bodyLengthMm = switch (ind.getCaste()) {
                        case QUEEN -> 15.0;
                        case SOLDIER -> 10.0;
                        case MALE -> 8.0;
                        default -> 6.0;
                    };
                    double antPhysPx = (bodyLengthMm / 1000.0) * pixelsPerMeter;
                    double antR = Math.max(2.0, antPhysPx * 3.5 * (zoom / 7.5));

                    Color casteColor;
                    if (currentRenderMode == RenderMode.REALISTIC) {
                        casteColor = switch (ind.getCaste()) {
                            case QUEEN -> Color.web("#1c120c");
                            case SOLDIER -> Color.web("#422212");
                            case WORKER, FORAGER, NURSE -> Color.web("#2b1d14");
                            case MALE -> Color.web("#150f0a");
                        };
                    } else {
                        casteColor = switch (ind.getCaste()) {
                            case QUEEN -> Color.web("#d946ef");
                            case SOLDIER -> Color.web("#ef4444");
                            case WORKER, FORAGER, NURSE -> Color.web("#f59e0b");
                            case MALE -> Color.web("#06b6d4");
                        };
                    }

                    drawRealisticAnt(gc3D, p[0], p[1], ind.getHeading(), ind.getCaste(), casteColor, antR, ind.isCarryingFood());

                    if (followedAnt == null) {
                        followedAnt = ind; // Select first ant as default tracked ant
                    }

                    if (followedAnt != null && followedAnt.getId().equals(ind.getId())) {
                        if (antTrailHistory.isEmpty() || Math.hypot(antTrailHistory.getLast()[0] - gx, antTrailHistory.getLast()[1] - gy) > 0.5) {
                            antTrailHistory.add(new double[]{gx, gy, gz});
                            if (antTrailHistory.size() > MAX_TRAIL_LENGTH) {
                                antTrailHistory.removeFirst();
                            }
                        }

                        if (isFollowAntCameraEnabled) {
                            pan3DX = (GRID_SIZE / 2.0 - gx) * 8.0;
                            pan3DY = (GRID_SIZE / 2.0 - gy) * 8.0;
                        }
                    }
                }
            }
        } else {
            Random rand = new Random(4321);
            int antCount = 35;
            for (int i = 0; i < antCount; i++) {
                int gx = 12 + (int)(rand.nextDouble() * (GRID_SIZE - 24));
                int gy = 12 + (int)(rand.nextDouble() * (GRID_SIZE - 24));
                double z = heightGrid[gx][gy] * 40.0 + 1.5;
                double[] p = project3DPoint(gx, gy, z, cx, cy, scale, radAz, radEl);
                double antR = Math.max(1.8, 2.5 * (zoom / 7.5));
                double heading = rand.nextDouble() * Math.PI * 2;
                org.swarmforge.core.domain.Individual.Caste caste = (i % 10 == 0) ? org.swarmforge.core.domain.Individual.Caste.QUEEN : ((i % 6 == 0) ? org.swarmforge.core.domain.Individual.Caste.SOLDIER : org.swarmforge.core.domain.Individual.Caste.WORKER);
                Color casteColor;
                if (currentRenderMode == RenderMode.REALISTIC) {
                    casteColor = (caste == org.swarmforge.core.domain.Individual.Caste.QUEEN) ? Color.web("#1c120c") : ((caste == org.swarmforge.core.domain.Individual.Caste.SOLDIER) ? Color.web("#422212") : Color.web("#2b1d14"));
                } else {
                    casteColor = (caste == org.swarmforge.core.domain.Individual.Caste.QUEEN) ? Color.web("#d946ef") : ((caste == org.swarmforge.core.domain.Individual.Caste.SOLDIER) ? Color.web("#ef4444") : Color.web("#f59e0b"));
                }

                drawRealisticAnt(gc3D, p[0], p[1], heading, caste, casteColor, antR, i % 4 == 0);
            }
        }

        drawTrackedAntFX(cx, cy, scale, radAz, radEl);
    }

    private void drawRealisticAnt(GraphicsContext gc, double px, double py, double heading,
                                  org.swarmforge.core.domain.Individual.Caste caste,
                                  Color baseColor, double baseR, boolean isCarryingFood) {
        if (caste == null) caste = org.swarmforge.core.domain.Individual.Caste.WORKER;

        double scale = switch (caste) {
            case QUEEN -> 1.8;
            case SOLDIER -> 1.4;
            case MALE -> 1.25;
            default -> 1.0;
        };
        double r = baseR * scale;

        double cosH = Math.cos(heading);
        double sinH = Math.sin(heading);
        double pxH = -sinH;
        double pyH = cosH;

        double headDist = r * 1.6;
        double petioleDist = -r * 0.8;
        double gasterDist = -r * 2.3;

        double headX = px + cosH * headDist;
        double headY = py + sinH * headDist;
        double petioleX = px + cosH * petioleDist;
        double petioleY = py + sinH * petioleDist;
        double gasterX = px + cosH * gasterDist;
        double gasterY = py + sinH * gasterDist;

        double headR = (caste == org.swarmforge.core.domain.Individual.Caste.SOLDIER) ? r * 1.3 : r * 0.85;
        double thoraxR = r * 0.75;
        double gasterR = (caste == org.swarmforge.core.domain.Individual.Caste.QUEEN) ? r * 1.7 : r * 1.25;

        // 1. LEGS (6 Articulated Legs)
        gc.setLineWidth(Math.max(1.0, r * 0.35));
        gc.setStroke(baseColor.darker().darker());

        double[][] legOffsets = {
            {r * 0.5, 0.95, 0.4},
            {0.0, 1.1, 0.0},
            {-r * 0.5, 1.0, -0.4}
        };

        double animTime = (System.currentTimeMillis() % 600) / 600.0;
        int legIdx = 0;

        for (double[] legDef : legOffsets) {
            double offFwd = legDef[0];
            double legLen = r * 1.8 * legDef[1];
            double fwdAng = legDef[2];
            double swing = Math.sin(animTime * Math.PI * 2 + legIdx * Math.PI / 3) * 0.25;
            legIdx++;

            for (int side : new int[]{1, -1}) {
                double rootX = px + cosH * offFwd + pxH * (thoraxR * 0.6 * side);
                double rootY = py + sinH * offFwd + pyH * (thoraxR * 0.6 * side);

                double kneeAngle = heading + (fwdAng + swing * side) + (Math.PI / 2.0 * side);
                double kneeX = rootX + Math.cos(kneeAngle) * (legLen * 0.55);
                double kneeY = rootY + Math.sin(kneeAngle) * (legLen * 0.55);

                double tipAngle = kneeAngle + (0.4 * side);
                double tipX = kneeX + Math.cos(tipAngle) * (legLen * 0.5);
                double tipY = kneeY + Math.sin(tipAngle) * (legLen * 0.5);

                gc.strokeLine(rootX, rootY, kneeX, kneeY);
                gc.strokeLine(kneeX, kneeY, tipX, tipY);
            }
        }

        // 2. GASTER (Abdomen)
        gc.setFill(baseColor.darker());
        gc.fillOval(gasterX - gasterR, gasterY - gasterR, gasterR * 2, gasterR * 2);
        gc.setStroke(Color.web("#0f172a"));
        gc.setLineWidth(1.0);
        gc.strokeOval(gasterX - gasterR, gasterY - gasterR, gasterR * 2, gasterR * 2);

        gc.setStroke(baseColor.brighter());
        gc.setLineWidth(1.2);
        gc.strokeArc(gasterX - gasterR * 0.7, gasterY - gasterR * 0.7, gasterR * 1.4, gasterR * 1.4, Math.toDegrees(heading) + 120, 120, javafx.scene.shape.ArcType.OPEN);

        // 3. PETIOLE
        gc.setFill(baseColor.darker().darker());
        double petR = Math.max(1.2, r * 0.4);
        gc.fillOval(petioleX - petR, petioleY - petR, petR * 2, petR * 2);

        // 4. THORAX
        gc.setFill(baseColor);
        gc.fillOval(px - thoraxR, py - thoraxR, thoraxR * 2, thoraxR * 2);
        gc.setStroke(Color.web("#0f172a"));
        gc.setLineWidth(1.0);
        gc.strokeOval(px - thoraxR, py - thoraxR, thoraxR * 2, thoraxR * 2);

        if (caste == org.swarmforge.core.domain.Individual.Caste.MALE || caste == org.swarmforge.core.domain.Individual.Caste.QUEEN) {
            gc.setFill(Color.web("rgba(224, 242, 254, 0.55)"));
            gc.setStroke(Color.web("rgba(255, 255, 255, 0.85)"));
            gc.setLineWidth(0.8);

            double wingLen = r * 2.6;
            for (int side : new int[]{1, -1}) {
                double wAngle = heading - Math.PI * 0.75 * side;
                double wx = px + Math.cos(wAngle) * wingLen;
                double wy = py + Math.sin(wAngle) * wingLen;
                gc.fillOval(px + (wx - px) * 0.5 - r * 0.4, py + (wy - py) * 0.5 - r * 0.4, r * 1.5, r * 0.7);
                gc.strokeOval(px + (wx - px) * 0.5 - r * 0.4, py + (wy - py) * 0.5 - r * 0.4, r * 1.5, r * 0.7);
            }
        }

        // 5. HEAD & EYES
        gc.setFill(baseColor);
        gc.fillOval(headX - headR, headY - headR, headR * 2, headR * 2);
        gc.setStroke(Color.web("#0f172a"));
        gc.setLineWidth(1.0);
        gc.strokeOval(headX - headR, headY - headR, headR * 2, headR * 2);

        gc.setFill(Color.web("#090d16"));
        double eyeR = Math.max(1.0, headR * 0.35);
        double eyeOffX = pxH * (headR * 0.65);
        double eyeOffY = pyH * (headR * 0.65);
        gc.fillOval(headX + eyeOffX - eyeR, headY + eyeOffY - eyeR, eyeR * 2, eyeR * 2);
        gc.fillOval(headX - eyeOffX - eyeR, headY - eyeOffY - eyeR, eyeR * 2, eyeR * 2);

        // 6. ANTENNAE
        gc.setStroke(baseColor.brighter().brighter());
        gc.setLineWidth(Math.max(1.0, r * 0.3));

        double antLen = r * 2.2;
        for (int side : new int[]{1, -1}) {
            double scapeAng = heading + (0.35 * side);
            double jointX = headX + Math.cos(scapeAng) * (antLen * 0.5);
            double jointY = headY + Math.sin(scapeAng) * (antLen * 0.5);

            double funicleAng = scapeAng + (0.45 * side);
            double tipX = jointX + Math.cos(funicleAng) * (antLen * 0.5);
            double tipY = jointY + Math.sin(funicleAng) * (antLen * 0.5);

            gc.strokeLine(headX + Math.cos(scapeAng) * (headR * 0.5), headY + Math.sin(scapeAng) * (headR * 0.5), jointX, jointY);
            gc.strokeLine(jointX, jointY, tipX, tipY);
        }

        // 7. MANDIBLES
        gc.setStroke(caste == org.swarmforge.core.domain.Individual.Caste.SOLDIER ? Color.web("#7f1d1d") : Color.web("#451a03"));
        gc.setLineWidth(caste == org.swarmforge.core.domain.Individual.Caste.SOLDIER ? Math.max(1.8, r * 0.5) : Math.max(1.0, r * 0.3));

        double mandLen = (caste == org.swarmforge.core.domain.Individual.Caste.SOLDIER) ? r * 1.3 : r * 0.7;
        for (int side : new int[]{1, -1}) {
            double mRootX = headX + cosH * (headR * 0.7) + pxH * (headR * 0.4 * side);
            double mRootY = headY + sinH * (headR * 0.7) + pyH * (headR * 0.4 * side);

            double mTipX = mRootX + cosH * mandLen - pxH * (mandLen * 0.35 * side);
            double mTipY = mRootY + sinH * mandLen - pyH * (mandLen * 0.35 * side);

            gc.strokeLine(mRootX, mRootY, mTipX, mTipY);
        }

        // 8. CARRIED ITEM
        if (isCarryingFood) {
            double foodX = headX + cosH * (headR * 1.5);
            double foodY = headY + sinH * (headR * 1.5);
            double foodR = r * 0.9;
            gc.setFill(Color.web("#22c55e"));
            gc.fillOval(foodX - foodR, foodY - foodR * 0.6, foodR * 2, foodR * 1.2);
            gc.setStroke(Color.web("#15803d"));
            gc.setLineWidth(1.0);
            gc.strokeOval(foodX - foodR, foodY - foodR * 0.6, foodR * 2, foodR * 1.2);
        }
    }

    private void drawTrackedAntFX(double cx, double cy, double scale, double radAz, double radEl) {
        if (followedAnt == null) return;

        // 1. Draw Trajectory Trail (Fil d'Ariane)
        if (antTrailHistory.size() > 1) {
            gc3D.setLineWidth(2.0);
            for (int i = 0; i < antTrailHistory.size() - 1; i++) {
                double[] pt1 = antTrailHistory.get(i);
                double[] pt2 = antTrailHistory.get(i + 1);
                double[] s1 = project3DPoint(pt1[0], pt1[1], pt1[2], cx, cy, scale, radAz, radEl);
                double[] s2 = project3DPoint(pt2[0], pt2[1], pt2[2], cx, cy, scale, radAz, radEl);
                double alpha = (double) (i + 1) / antTrailHistory.size();
                gc3D.setStroke(Color.web("#f59e0b", alpha * 0.85));
                gc3D.strokeLine(s1[0], s1[1], s2[0], s2[1]);
            }
        }

        // 2. Target Spotlight Reticle
        double gx = followedAnt.getX();
        double gy = followedAnt.getY();
        double gz = followedAnt.getZ();
        if (activeSimulation != null) {
            gx = (gx / (float) Math.max(1, activeSimulation.getTerrarium().getWidth())) * GRID_SIZE;
            gy = (gy / (float) Math.max(1, activeSimulation.getTerrarium().getHeight())) * GRID_SIZE;
            int igx = Math.max(0, Math.min(GRID_SIZE - 1, (int) gx));
            int igy = Math.max(0, Math.min(GRID_SIZE - 1, (int) gy));
            gz = heightGrid[igx][igy] * 40.0 + gz * 2.0 + 1.5;
        } else {
            gx = GRID_SIZE / 2.0; gy = GRID_SIZE / 2.0; gz = 20.0;
        }

        double[] p = project3DPoint(gx, gy, gz, cx, cy, scale, radAz, radEl);
        double timeMs = (System.currentTimeMillis() % 2000) / 2000.0;
        double pulseR = 14.0 + Math.sin(timeMs * Math.PI * 2) * 4.0;

        gc3D.setStroke(Color.web("#38bdf8"));
        gc3D.setLineWidth(2.0);
        gc3D.strokeOval(p[0] - pulseR, p[1] - pulseR, pulseR * 2, pulseR * 2);
        gc3D.setStroke(Color.web("#f59e0b"));
        gc3D.setLineWidth(1.2);
        gc3D.strokeOval(p[0] - pulseR * 0.6, p[1] - pulseR * 0.6, pulseR * 1.2, pulseR * 1.2);

        // Crosshairs
        gc3D.strokeLine(p[0] - pulseR - 6, p[1], p[0] - pulseR + 2, p[1]);
        gc3D.strokeLine(p[0] + pulseR - 2, p[1], p[0] + pulseR + 6, p[1]);
        gc3D.strokeLine(p[0], p[1] - pulseR - 6, p[0], p[1] - pulseR + 2);
        gc3D.strokeLine(p[0], p[1] + pulseR - 2, p[0], p[1] + pulseR + 6);

        // 3. Render Live Ant Constants & Metrics HUD Card
        updateTrackedAntTelemetry();
    }

    private void drawTrackedAntHUDCard() {
        // Handled interactively by TrackedAntPane JavaFX overlay
    }

    private void drawGalleriesOverlay3D(double cx, double cy, double scale, double radAz, double radEl) {
        // 1. Render 3D Surface Nest Architecture Structure (Termite Cathedral, Paper Wasp Hive on Branch, Thatch Mound, etc.)
        drawRealistic3DNestStructure(cx, cy, scale, radAz, radEl);

        int nestX = GRID_SIZE / 2;
        int nestY = GRID_SIZE / 2;
        double baseZ = heightGrid[nestX][nestY] * 40.0;
        double zSc = zoom / 7.5;

        // Determine if current species architecture is arboreal/suspended or leafcutter/termite
        boolean isArboreal = false;
        boolean isLeafcutterOrTermite = false;
        if (speciesAdaptCombo != null && speciesAdaptCombo.getValue() != null) {
            String s = speciesAdaptCombo.getValue().toLowerCase();
            if (s.contains("vespula") || s.contains("polistes") || s.contains("guêpe") ||
                s.contains("cremato") || s.contains("oeco") || s.contains("apis") || s.contains("abeille")) {
                isArboreal = true;
            }
            if (s.contains("atta") || s.contains("coupeuse") || s.contains("termit") || s.contains("macrotermes")) {
                isLeafcutterOrTermite = true;
            }
        }

        // Live population & voxel excavation scan count
        int workerCount = 500;
        if (activeSimulation != null && !activeSimulation.getColonies().isEmpty()) {
            workerCount = activeSimulation.getColonies().get(0).getLivingIndividuals().size();
        }

        // Scan actual carved voidGrid voxels (Real Voxel-by-Voxel Excavation Matrix)
        int carvedVoxCount = 0;
        for (int x = nestX - 10; x <= nestX + 10; x++) {
            for (int y = nestY - 10; y <= nestY + 10; y++) {
                if (x < 0 || x >= GRID_SIZE || y < 0 || y >= GRID_SIZE) continue;
                for (int d = 0; d < SOIL_DEPTH; d++) {
                    if (voidGrid[x][y][d]) carvedVoxCount++;
                }
            }
        }

        // Physical real chamber scale derived from actual carved voxel volume
        double volScale = Math.max(0.75, Math.min(2.5, Math.cbrt(Math.max(40, carvedVoxCount) / 80.0)));

        // Real physical chamber radii (cm converted to projected 3D screen px)
        double rQueen   = Math.max(6.0, 11.0 * zSc * volScale);   // Loge Royale (~25cm real diameter)
        double rBrood   = Math.max(5.0, 9.5 * zSc * volScale);    // Loge Couvain (~20cm real diameter)
        double rFood    = Math.max(4.5, 8.5 * zSc * volScale);    // Grenier / Stock (~18cm real diameter)
        double rFungus  = Math.max(5.5, 10.0 * zSc * volScale);   // Champignonnière Atta / Termites (~22cm diameter)
        double rAphid   = Math.max(4.5, 8.0 * zSc * volScale);    // Laiterie à Pucerons / Élevage
        double rHibern  = Math.max(5.0, 9.0 * zSc * volScale);    // Chambre Hibernation / Diapause
        double rTrash   = Math.max(4.0, 7.0 * zSc * volScale);    // Dépotoir / Déchets

        boolean isWinter = simSeason != null && (simSeason.toLowerCase().contains("hiver") || simSeason.toLowerCase().contains("winter"));
        double wtDepth = waterTableDepthSlider != null ? waterTableDepthSlider.getValue() : 15.0;
        boolean isFlooded = wtDepth < 18.0 || (baseHumiditySlider != null && baseHumiditySlider.getValue() > 0.70);

        if (isArboreal) {
            // ARBOREAL / BRANCH-SUSPENDED NEST TOPOLOGY (Chambers inside/around tree canopy branch)
            double branchZ = baseZ + 35.0;
            double[] pBranch = project3DPoint(nestX, nestY, branchZ, cx, cy, scale, radAz, radEl);

            double[] pQ = project3DPoint(nestX + 2, nestY, branchZ + 6.0, cx, cy, scale, radAz, radEl);
            double[] pB = project3DPoint(nestX - 3, nestY + 2, branchZ - 2.0, cx, cy, scale, radAz, radEl);
            double[] pF = project3DPoint(nestX + 3, nestY - 2, branchZ - 4.0, cx, cy, scale, radAz, radEl);

            gc3D.setStroke(Color.web("#f59e0b", 0.70));
            gc3D.setLineWidth(Math.max(2.0, 3.5 * zSc));
            gc3D.strokeLine(pBranch[0], pBranch[1], pQ[0], pQ[1]);
            gc3D.strokeLine(pBranch[0], pBranch[1], pB[0], pB[1]);
            gc3D.strokeLine(pBranch[0], pBranch[1], pF[0], pF[1]);

            // 1. Loge Royale (Magenta)
            gc3D.setFill(Color.web("#d946ef", 0.65));
            gc3D.fillOval(pQ[0] - rQueen, pQ[1] - rQueen, rQueen * 2, rQueen * 2);
            gc3D.setStroke(Color.web("#f0abfc", 0.90)); gc3D.setLineWidth(1.5);
            gc3D.strokeOval(pQ[0] - rQueen, pQ[1] - rQueen, rQueen * 2, rQueen * 2);

            // 2. Chambre du Couvain (Blanc)
            gc3D.setFill(Color.web("#f8fafc", 0.65));
            gc3D.fillOval(pB[0] - rBrood, pB[1] - rBrood, rBrood * 2, rBrood * 2);
            gc3D.setStroke(Color.web("#ffffff", 0.90)); gc3D.setLineWidth(1.2);
            gc3D.strokeOval(pB[0] - rBrood, pB[1] - rBrood, rBrood * 2, rBrood * 2);

            // 3. Grenier / Stockage (Vert)
            gc3D.setFill(Color.web("#22c55e", 0.65));
            gc3D.fillOval(pF[0] - rFood, pF[1] - rFood, rFood * 2, rFood * 2);
            gc3D.setStroke(Color.web("#4ade80", 0.90)); gc3D.setLineWidth(1.2);
            gc3D.strokeOval(pF[0] - rFood, pF[1] - rFood, rFood * 2, rFood * 2);
        } else {
            // SUBTERRANEAN / MOUND NEST TOPOLOGY (Multiple Exit Craters + Excavated Soil Mounds + Deep Stratified Chambers)

            int exit1X = nestX, exit1Y = nestY;
            int exit2X = nestX + 6, exit2Y = nestY - 5;
            int exit3X = nestX - 7, exit3Y = nestY + 4;

            double zE1 = heightGrid[exit1X][exit1Y] * 40.0;
            double zE2 = heightGrid[exit2X][exit2Y] * 40.0;
            double zE3 = heightGrid[exit3X][exit3Y] * 40.0;

            double[] pE1 = project3DPoint(exit1X, exit1Y, zE1, cx, cy, scale, radAz, radEl);
            double[] pE2 = project3DPoint(exit2X, exit2Y, zE2, cx, cy, scale, radAz, radEl);
            double[] pE3 = project3DPoint(exit3X, exit3Y, zE3, cx, cy, scale, radAz, radEl);

            // Render Excavated Soil Granule Rings (Déblais de terre rapportés par les ouvrières)
            double dirtR = 7.0 * zSc;
            for (double[] pExit : new double[][]{pE1, pE2, pE3}) {
                gc3D.setFill(Color.web("#9a3412", 0.80)); // Terracotta soil granule ring
                gc3D.fillOval(pExit[0] - dirtR, pExit[1] - dirtR * 0.5, dirtR * 2, dirtR * 1.0);
                gc3D.setFill(Color.web("#18181b")); // Entrance aperture
                gc3D.fillOval(pExit[0] - dirtR * 0.4, pExit[1] - dirtR * 0.25, dirtR * 0.8, dirtR * 0.5);
            }

            // SNOW COVER & SNOW TUNNELING DYNAMICS (Hiver / Couverture Neigeuse)
            if (isWinter) {
                for (double[] pExit : new double[][]{pE1, pE2, pE3}) {
                    // Snow Blanket Ring around entrance
                    gc3D.setFill(Color.web("#f1f5f9", 0.85));
                    gc3D.fillOval(pExit[0] - dirtR * 1.2, pExit[1] - dirtR * 0.7 - 4, dirtR * 2.4, dirtR * 1.2);
                    
                    // Translucent Snow Tunnel Shaft carved through snow
                    gc3D.setStroke(Color.web("#e0f2fe", 0.90));
                    gc3D.setLineWidth(Math.max(2.0, 3.5 * zSc));
                    gc3D.strokeLine(pExit[0], pExit[1] - 12.0, pExit[0], pExit[1]);
                }
            }

            // Occlusion & Cut-Plane check: subterranean chambers & tunnels are only rendered if terrain is hidden, sliced open at/past nestX, or in translucent volumetric mode
            double cutRatio = slicePlaneSlider != null ? (slicePlaneSlider.getValue() / 100.0) : 1.0;
            int cutXLimit = (int) (GRID_SIZE * cutRatio);
            boolean isTranslucent = showTranslucentVolumetricModeCheck != null && showTranslucentVolumetricModeCheck.isSelected();
            boolean isSubterraneanExposed = !isTerrainVisible || (cutXLimit <= nestX) || isTranslucent;

            if (isSubterraneanExposed) {
                // Underground Central Distribution Hub
                double hubZ = baseZ - 12.0;
                double[] pHub = project3DPoint(nestX, nestY, hubZ, cx, cy, scale, radAz, radEl);

                // Tunnels connecting exits to central hub
                Color tunnelCol = isWinter ? Color.web("#e0f2fe", 0.85) : Color.web("#f59e0b", 0.70);
                gc3D.setStroke(tunnelCol);
                gc3D.setLineWidth(Math.max(2.2, 4.0 * zSc));
                gc3D.strokeLine(pE1[0], pE1[1], pHub[0], pHub[1]);
                gc3D.strokeLine(pE2[0], pE2[1], pHub[0], pHub[1]);
                gc3D.strokeLine(pE3[0], pE3[1], pHub[0], pHub[1]);

                // 1. Loge Royale (Magenta - Deepest subterranean strata)
                double[] pQueen = project3DPoint(nestX + 5, nestY + 2, baseZ - 24.0, cx, cy, scale, radAz, radEl);
                gc3D.strokeLine(pHub[0], pHub[1], pQueen[0], pQueen[1]);

                gc3D.setFill(Color.web("#d946ef", 0.60));
                gc3D.fillOval(pQueen[0] - rQueen, pQueen[1] - rQueen, rQueen * 2, rQueen * 2);
                gc3D.setStroke(Color.web("#f0abfc", 0.95)); gc3D.setLineWidth(1.8);
                gc3D.strokeOval(pQueen[0] - rQueen, pQueen[1] - rQueen, rQueen * 2, rQueen * 2);

                // 2. Chambre du Couvain (Blanc - Upper moist strata)
                double[] pBrood = project3DPoint(nestX - 5, nestY + 3, baseZ - 16.0, cx, cy, scale, radAz, radEl);
                gc3D.strokeLine(pHub[0], pHub[1], pBrood[0], pBrood[1]);

                gc3D.setFill(Color.web("#f8fafc", 0.60));
                gc3D.fillOval(pBrood[0] - rBrood, pBrood[1] - rBrood, rBrood * 2, rBrood * 2);
                gc3D.setStroke(Color.web("#ffffff", 0.95)); gc3D.setLineWidth(1.4);
                gc3D.strokeOval(pBrood[0] - rBrood, pBrood[1] - rBrood, rBrood * 2, rBrood * 2);

                // 3. Champignonnière (Violet / Purple - Culture Atta / Macrotermes) OR Grenier
                double[] pFungus = project3DPoint(nestX + 6, nestY - 3, baseZ - 18.0, cx, cy, scale, radAz, radEl);
                gc3D.strokeLine(pHub[0], pHub[1], pFungus[0], pFungus[1]);

                if (isLeafcutterOrTermite) {
                    gc3D.setFill(Color.web("#a855f7", 0.65)); // Purple Fungus Garden
                    gc3D.fillOval(pFungus[0] - rFungus, pFungus[1] - rFungus, rFungus * 2, rFungus * 2);
                    gc3D.setStroke(Color.web("#c084fc", 0.95)); gc3D.setLineWidth(1.6);
                    gc3D.strokeOval(pFungus[0] - rFungus, pFungus[1] - rFungus, rFungus * 2, rFungus * 2);
                } else {
                    gc3D.setFill(Color.web("#22c55e", 0.60)); // Green Granary / Stock
                    gc3D.fillOval(pFungus[0] - rFood, pFungus[1] - rFood, rFood * 2, rFood * 2);
                    gc3D.setStroke(Color.web("#4ade80", 0.95)); gc3D.setLineWidth(1.4);
                    gc3D.strokeOval(pFungus[0] - rFood, pFungus[1] - rFood, rFood * 2, rFood * 2);
                }

                // 4. Laiterie à Pucerons / Élevage Myrmécophile (Rose / Pink)
                double[] pAphid = project3DPoint(nestX + 2, nestY - 7, baseZ - 11.0, cx, cy, scale, radAz, radEl);
                gc3D.strokeLine(pHub[0], pHub[1], pAphid[0], pAphid[1]);

                gc3D.setFill(Color.web("#ec4899", 0.60)); // Pink Aphid Pen
                gc3D.fillOval(pAphid[0] - rAphid, pAphid[1] - rAphid, rAphid * 2, rAphid * 2);
                gc3D.setStroke(Color.web("#f472b6", 0.90)); gc3D.setLineWidth(1.3);
                gc3D.strokeOval(pAphid[0] - rAphid, pAphid[1] - rAphid, rAphid * 2, rAphid * 2);

                // 5. Loge d'Hibernation / Diapause (Cyan/Bleu)
                double[] pHibern = project3DPoint(nestX - 4, nestY - 6, baseZ - 20.0, cx, cy, scale, radAz, radEl);
                gc3D.strokeLine(pHub[0], pHub[1], pHibern[0], pHibern[1]);

                Color hCol = isWinter ? Color.web("#06b6d4", 0.75) : Color.web("#0284c7", 0.50);
                gc3D.setFill(hCol);
                gc3D.fillOval(pHibern[0] - rHibern, pHibern[1] - rHibern, rHibern * 2, rHibern * 2);
                gc3D.setStroke(Color.web("#38bdf8", 0.90)); gc3D.setLineWidth(1.3);
                gc3D.strokeOval(pHibern[0] - rHibern, pHibern[1] - rHibern, rHibern * 2, rHibern * 2);

                // 6. Dépotoir / Déchets (Jaune/Ambre)
                double[] pTrash = project3DPoint(nestX - 8, nestY - 1, baseZ - 13.0, cx, cy, scale, radAz, radEl);
                gc3D.strokeLine(pHub[0], pHub[1], pTrash[0], pTrash[1]);

                gc3D.setFill(Color.web("#eab308", 0.55));
                gc3D.fillOval(pTrash[0] - rTrash, pTrash[1] - rTrash, rTrash * 2, rTrash * 2);
                gc3D.setStroke(Color.web("#fef08a", 0.85)); gc3D.setLineWidth(1.2);
                gc3D.strokeOval(pTrash[0] - rTrash, pTrash[1] - rTrash, rTrash * 2, rTrash * 2);

                // Hydrological Flooding Effect in lower chambers
                if (isFlooded) {
                    gc3D.setFill(Color.web("#0284c7", 0.55));
                    gc3D.fillOval(pQueen[0] - rQueen * 0.8, pQueen[1], rQueen * 1.6, rQueen * 0.7);
                    gc3D.fillOval(pHibern[0] - rHibern * 0.8, pHibern[1], rHibern * 1.6, rHibern * 0.7);
                }
            }
        }
    }

    private void drawGalleriesLegendHUD(double w, double h) {
        double boxW = 290.0;
        double boxH = 240.0;
        double hx = w - boxW - 15.0;
        double hy = 15.0;

        // HUD Glassmorphism Card
        gc3D.setFill(Color.web("rgba(15, 23, 42, 0.94)"));
        gc3D.fillRoundRect(hx, hy, boxW, boxH, 10, 10);
        gc3D.setStroke(Color.web("#38bdf8", 0.85));
        gc3D.setLineWidth(1.2);
        gc3D.strokeRoundRect(hx, hy, boxW, boxH, 10, 10);

        // Header Title
        gc3D.setFill(Color.web("#38bdf8"));
        gc3D.setFont(Font.font("System", javafx.scene.text.FontWeight.BOLD, 11));
        gc3D.fillText("🏰 NEST & MULTI-SPECIES CHAMBERS LEGEND", hx + 12, hy + 20);

        gc3D.setStroke(Color.web("rgba(56, 189, 248, 0.3)"));
        gc3D.setLineWidth(1.0);
        gc3D.strokeLine(hx + 10, hy + 26, hx + boxW - 10, hy + 26);

        gc3D.setFont(Font.font("System", 10));

        // 1. Loge Royale
        gc3D.setFill(Color.web("#d946ef")); gc3D.fillOval(hx + 14, hy + 35, 10, 10);
        gc3D.setFill(Color.web("#f8fafc"));
        gc3D.fillText("Loge Royale (Reine, Ponte & Cellule)", hx + 32, hy + 44);

        // 2. Chambre du Couvain
        gc3D.setFill(Color.web("#f8fafc")); gc3D.fillOval(hx + 14, hy + 53, 10, 10);
        gc3D.setStroke(Color.web("#94a3b8")); gc3D.strokeOval(hx + 14, hy + 53, 10, 10);
        gc3D.setFill(Color.web("#f8fafc"));
        gc3D.fillText("Brood (Eggs, Larvae & Pupae)", hx + 32, hy + 62);

        // 3. Champignonnière (Atta / Macrotermes)
        gc3D.setFill(Color.web("#a855f7")); gc3D.fillOval(hx + 14, hy + 71, 10, 10);
        gc3D.setFill(Color.web("#f8fafc"));
        gc3D.fillText("Fungus Garden (Atta / Macrotermes)", hx + 32, hy + 80);

        // 4. Laiterie à Pucerons / Élevage
        gc3D.setFill(Color.web("#ec4899")); gc3D.fillOval(hx + 14, hy + 89, 10, 10);
        gc3D.setFill(Color.web("#f8fafc"));
        gc3D.fillText("Aphid Farm & Livestock", hx + 32, hy + 98);

        // 5. Grenier & Stockage
        gc3D.setFill(Color.web("#22c55e")); gc3D.fillOval(hx + 14, hy + 107, 10, 10);
        gc3D.setFill(Color.web("#f8fafc"));
        gc3D.fillText("Grenier / Stock (Graines, Nectar, Pollen)", hx + 32, hy + 116);

        // 6. Loge d'Hibernation / Diapause
        gc3D.setFill(Color.web("#0284c7")); gc3D.fillOval(hx + 14, hy + 125, 10, 10);
        gc3D.setFill(Color.web("#f8fafc"));
        String hibState = (simSeason != null && simSeason.toLowerCase().contains("hiver")) ? "Actif (Hiver)" : "Inactif";
        gc3D.fillText("Chambre Hibernation / Diapause (" + hibState + ")", hx + 32, hy + 134);

        // 7. Dépotoir / Déchets
        gc3D.setFill(Color.web("#eab308")); gc3D.fillOval(hx + 14, hy + 143, 10, 10);
        gc3D.setFill(Color.web("#f8fafc"));
        gc3D.fillText("Refuse Dump / Organic Waste & Remains", hx + 32, hy + 152);

        // 8. Galeries & Puits Voxel Excavés
        gc3D.setStroke(Color.web("#f59e0b")); gc3D.setLineWidth(2.5);
        gc3D.strokeLine(hx + 14, hy + 166, hx + 24, hy + 166);
        gc3D.setFill(Color.web("#f8fafc"));
        gc3D.fillText("Excavated Voxel Shafts & Galleries (3D)", hx + 32, hy + 170);

        // 9. Tunnels sous la Neige / Glace
        gc3D.setStroke(Color.web("#e0f2fe")); gc3D.setLineWidth(2.5);
        gc3D.strokeLine(hx + 14, hy + 184, hx + 24, hy + 184);
        gc3D.setFill(Color.web("#f8fafc"));
        gc3D.fillText("Tunnels sous la Neige / Glace (Winter Access)", hx + 32, hy + 188);

        // 10. Déblais de Terre & Exits
        gc3D.setFill(Color.web("#9a3412")); gc3D.fillOval(hx + 14, hy + 199, 10, 6);
        gc3D.setFill(Color.web("#18181b")); gc3D.fillOval(hx + 17, hy + 200, 4, 3);
        gc3D.setFill(Color.web("#f8fafc"));
        gc3D.fillText("Nest Entrances & Excavated Mounds", hx + 32, hy + 207);

        // Live Dynamic Status Note
        gc3D.setFill(Color.web("#4ade80"));
        gc3D.setFont(Font.font("System", 9));
        gc3D.fillText("⛏️ Excavation Voxel par Voxel (128x128x32 Matrix)", hx + 12, hy + 230);
    }

    private void drawRealistic3DNestStructure(double cx, double cy, double scale, double radAz, double radEl) {
        int nestX = GRID_SIZE / 2;
        int nestY = GRID_SIZE / 2;
        double surfaceZ = heightGrid[nestX][nestY] * 40.0;
        double[] surfaceP = project3DPoint(nestX, nestY, surfaceZ, cx, cy, scale, radAz, radEl);

        int workerCount = 1000;
        if (activeSimulation != null && !activeSimulation.getColonies().isEmpty()) {
            workerCount = activeSimulation.getColonies().get(0).getLivingIndividuals().size();
        }
        double maturityScale = Math.max(0.65, Math.min(2.5, Math.sqrt(Math.max(10, workerCount) / 400.0)));

        String nestType = "Subterranean";
        if (speciesAdaptCombo != null && speciesAdaptCombo.getValue() != null) {
            String s = speciesAdaptCombo.getValue().toLowerCase();
            if (s.contains("termit") || s.contains("macrotermes")) nestType = "TermiteCathedral";
            else if (s.contains("vespula") || s.contains("polistes") || s.contains("guêpe")) nestType = "PaperWaspSuspended";
            else if (s.contains("apis") || s.contains("abeille")) nestType = "HoneybeeTreeComb";
            else if (s.contains("formica") || s.contains("rufous")) nestType = "ThatchMound";
            else if (s.contains("atta") || s.contains("coupeuse")) nestType = "LeafcutterVault";
            else if (s.contains("cremato")) nestType = "ArborealCarton";
            else if (s.contains("eciton")) nestType = "LivingBivouac";
            else if (s.contains("oeco")) nestType = "WeaverLeaf";
        }

        double zScale = zoom / 7.5;

        switch (nestType) {
            case "TermiteCathedral" -> {
                // Render Sculpted Terracotta Termite Cathedral Spire Mound (Macrotermes)
                double moundH = 35.0 * maturityScale * zScale;
                double moundW = 18.0 * maturityScale * zScale;

                // Main central cathedral spire
                gc3D.setFill(Color.web("#9a3412"));
                gc3D.fillPolygon(
                    new double[]{surfaceP[0] - moundW * 0.5, surfaceP[0], surfaceP[0] + moundW * 0.5},
                    new double[]{surfaceP[1], surfaceP[1] - moundH, surfaceP[1]}, 3
                );
                gc3D.setFill(Color.web("#7c2d12"));
                gc3D.fillPolygon(
                    new double[]{surfaceP[0], surfaceP[0] + moundW * 0.5, surfaceP[0] + moundW * 0.2},
                    new double[]{surfaceP[1] - moundH, surfaceP[1], surfaceP[1]}, 3
                );

                // Secondary side spires
                gc3D.setFill(Color.web("#b45309"));
                gc3D.fillPolygon(
                    new double[]{surfaceP[0] - moundW * 0.8, surfaceP[0] - moundW * 0.2, surfaceP[0]},
                    new double[]{surfaceP[1], surfaceP[1] - moundH * 0.65, surfaceP[1]}, 3
                );
                gc3D.fillPolygon(
                    new double[]{surfaceP[0] + moundW * 0.1, surfaceP[0] + moundW * 0.7, surfaceP[0] + moundW * 0.9},
                    new double[]{surfaceP[1], surfaceP[1] - moundH * 0.55, surfaceP[1]}, 3
                );

                // Chimney ventilation vents
                gc3D.setFill(Color.web("#292524"));
                gc3D.fillOval(surfaceP[0] - 2 * zScale, surfaceP[1] - moundH + 4 * zScale, 4 * zScale, 3 * zScale);
                gc3D.fillOval(surfaceP[0] - moundW * 0.35, surfaceP[1] - moundH * 0.6, 3 * zScale, 2 * zScale);
            }
            case "PaperWaspSuspended" -> {
                // Render 3D Tree Branch with Suspended Hexagonal Paper Wasp Hive (Vespula / Polistes)
                double branchY = surfaceP[1] - 45.0 * zScale;
                double branchLen = 50.0 * zScale;

                // Tree branch
                gc3D.setStroke(Color.web("#78350f"));
                gc3D.setLineWidth(4.5 * zScale);
                gc3D.strokeLine(surfaceP[0] - branchLen * 0.6, branchY - 5 * zScale, surfaceP[0] + branchLen * 0.6, branchY + 5 * zScale);

                // Green leaf foliage canopy above branch
                gc3D.setFill(Color.web("#15803d", 0.85));
                gc3D.fillOval(surfaceP[0] - branchLen * 0.7, branchY - 20 * zScale, branchLen * 1.4, 25 * zScale);
                gc3D.setFill(Color.web("#22c55e", 0.70));
                gc3D.fillOval(surfaceP[0] - branchLen * 0.4, branchY - 24 * zScale, branchLen * 0.8, 18 * zScale);

                // Pedicel attachment string
                gc3D.setStroke(Color.web("#451a03"));
                gc3D.setLineWidth(1.8 * zScale);
                gc3D.strokeLine(surfaceP[0], branchY, surfaceP[0], branchY + 8 * zScale);

                // Suspended Hexagonal Comb Paper Nest Envelope
                double nestR = 14.0 * maturityScale * zScale;
                double nestH = 18.0 * maturityScale * zScale;
                double nestTopY = branchY + 8 * zScale;

                gc3D.setFill(Color.web("#a1a1aa"));
                gc3D.fillOval(surfaceP[0] - nestR, nestTopY, nestR * 2, nestH);
                gc3D.setStroke(Color.web("#71717a"));
                gc3D.setLineWidth(1.2 * zScale);
                gc3D.strokeOval(surfaceP[0] - nestR, nestTopY, nestR * 2, nestH);

                // Bottom entrance aperture hole
                gc3D.setFill(Color.web("#18181b"));
                gc3D.fillOval(surfaceP[0] - nestR * 0.3, nestTopY + nestH - 4 * zScale, nestR * 0.6, 5 * zScale);
            }
            case "HoneybeeTreeComb" -> {
                // Render Hollow Tree Trunk Cavity with Golden Honeycomb Plates (Apis mellifera)
                double trunkW = 20.0 * zScale;
                double trunkH = 50.0 * zScale;
                double topY = surfaceP[1] - trunkH;

                gc3D.setFill(Color.web("#451a03"));
                gc3D.fillRect(surfaceP[0] - trunkW * 0.6, topY, trunkW * 1.2, trunkH);

                gc3D.setFill(Color.web("#18181b"));
                gc3D.fillOval(surfaceP[0] - trunkW * 0.4, topY + trunkH * 0.25, trunkW * 0.8, trunkH * 0.55);

                gc3D.setFill(Color.web("#eab308"));
                for (int c = -2; c <= 2; c++) {
                    gc3D.fillRect(surfaceP[0] + c * 2.5 * zScale - 1 * zScale, topY + trunkH * 0.3, 2 * zScale, trunkH * 0.4);
                }
            }
            case "ThatchMound" -> {
                // Render Surface Conical Thatch Mound of Pine Needles & Twigs (Formica rufa)
                double domeW = 22.0 * maturityScale * zScale;
                double domeH = 16.0 * maturityScale * zScale;

                gc3D.setFill(Color.web("#78350f"));
                gc3D.fillOval(surfaceP[0] - domeW, surfaceP[1] - domeH, domeW * 2, domeH * 1.6);
                gc3D.setFill(Color.web("#14532d", 0.6));
                gc3D.fillOval(surfaceP[0] - domeW * 0.8, surfaceP[1] - domeH * 0.9, domeW * 1.6, domeH * 1.3);

                gc3D.setStroke(Color.web("#a16207"));
                gc3D.setLineWidth(1.0 * zScale);
                Random r = new Random(55);
                for (int k = 0; k < 12; k++) {
                    double dx = (r.nextDouble() - 0.5) * domeW * 1.2;
                    double dy = surfaceP[1] - r.nextDouble() * domeH;
                    gc3D.strokeLine(surfaceP[0] + dx, dy, surfaceP[0] + dx + (r.nextDouble() - 0.5) * 6, dy - 4 * zScale);
                }
            }
            default -> { // Subterranean Ant Burrow Crater Mound (Lasius / Messor)
                double craterR = 10.0 * maturityScale * zScale;
                gc3D.setFill(Color.web("#a16207", 0.85));
                gc3D.fillOval(surfaceP[0] - craterR, surfaceP[1] - craterR * 0.5, craterR * 2, craterR * 1.0);
                gc3D.setFill(Color.web("#451a03"));
                gc3D.fillOval(surfaceP[0] - craterR * 0.4, surfaceP[1] - craterR * 0.25, craterR * 0.8, craterR * 0.5);
            }
        }
    }


    private void drawPheromoneOverlay3D(double cx, double cy, double scale, double radAz, double radEl) {
        if (!isSimulationMode || activeSimulation == null) return;
        org.swarmforge.core.gpu.SparsePheromoneGrid grid = activeSimulation.getPheromoneGrid();
        if (grid == null) return;

        java.util.Map<Long, float[]> entries = grid.getAllEntries();
        if (entries.isEmpty()) return;

        double baseDotR = Math.max(2.0, 4.0 * (zoom / 7.5));

        for (java.util.Map.Entry<Long, float[]> e : entries.entrySet()) {
            long key = e.getKey();
            float[] vals = e.getValue();
            if (vals == null) continue;

            int[] coords = org.swarmforge.core.spatial.Morton3D.decode(key);
            int x = coords[0];
            int y = coords[1];
            int zVox = coords[2];

            if (x < 0 || x >= GRID_SIZE || y < 0 || y >= GRID_SIZE) continue;

            // Extract multi-channel pheromone intensities mapped to PheromoneType indices
            float homeVal = (vals.length > 0 && showHomePheromone) ? vals[0] : 0f;
            float foodVal = (vals.length > 1 && showFoodPheromone) ? vals[1] : 0f;
            float alarmVal = (vals.length > 2 && showAlarmPheromone) ? vals[2] : 0f;
            float recruitVal = (vals.length > 3 && showRecruitmentPheromone) ? vals[3] : 0f;
            float queenVal = (vals.length > 4 && showQueenPheromone) ? vals[4] : 0f;
            float deathVal = (vals.length > 5 && showDeathPheromone) ? vals[5] : 0f;

            float maxVal = Math.max(homeVal, Math.max(foodVal, Math.max(alarmVal, Math.max(recruitVal, Math.max(queenVal, deathVal)))));
            if (maxVal < 0.02f) continue;

            // Chemical channel color mapping
            String colorHex;
            if (alarmVal == maxVal) colorHex = "#ef4444";       // Alarm/Danger: Red
            else if (foodVal == maxVal) colorHex = "#a855f7";   // Food Trail: Purple
            else if (homeVal == maxVal) colorHex = "#0284c7";   // Nest/Home: Blue
            else if (recruitVal == maxVal) colorHex = "#f59e0b";// Recruitment: Amber
            else if (queenVal == maxVal) colorHex = "#ec4899";  // Queen Scent: Pink
            else colorHex = "#64748b";                           // Death/Necrophoric: Slate

            double terrainZ = heightGrid[x][y] * 40.0 + (zVox * 0.5) + 0.8;
            double[] p = project3DPoint(x, y, terrainZ, cx, cy, scale, radAz, radEl);
            double alpha = Math.min(0.9, maxVal * 0.75);

            switch (pheromoneRenderMode) {
                case HEATMAP_GRADIENT -> {
                    // Mode 1: Continuous Density Surface / Fading Heatmap Gradient
                    double heatRadius = baseDotR * (1.6 + maxVal * 2.6);
                    gc3D.setFill(Color.web(colorHex, alpha * 0.45));
                    gc3D.fillOval(p[0] - heatRadius, p[1] - heatRadius * 0.6, heatRadius * 2, heatRadius * 1.2);
                }
                case VOXEL_PARTICLES -> {
                    // Mode 2: Discrete Points / Voxels with Dynamic Radius & Core Glow
                    double particleR = baseDotR * (0.8 + maxVal * 1.3);
                    gc3D.setFill(Color.web(colorHex, Math.min(1.0, alpha + 0.15)));
                    gc3D.fillOval(p[0] - particleR, p[1] - particleR, particleR * 2, particleR * 2);
                    gc3D.setFill(Color.web("#ffffff", Math.min(1.0, alpha + 0.35)));
                    gc3D.fillOval(p[0] - particleR * 0.3, p[1] - particleR * 0.3, particleR * 0.6, particleR * 0.6);
                }
                case HYBRID_GIS -> {
                    // Mode 3: Multi-Layer SIG Hybrid (Gradient Surface Halo + Voxel Core Glow)
                    double outerHaloR = baseDotR * (1.4 + maxVal * 1.8);
                    double coreR = baseDotR * (0.7 + maxVal * 0.8);

                    gc3D.setFill(Color.web(colorHex, alpha * 0.35));
                    gc3D.fillOval(p[0] - outerHaloR, p[1] - outerHaloR * 0.65, outerHaloR * 2, outerHaloR * 1.3);

                    gc3D.setFill(Color.web(colorHex, Math.min(1.0, alpha + 0.25)));
                    gc3D.fillOval(p[0] - coreR, p[1] - coreR, coreR * 2, coreR * 2);
                    gc3D.setFill(Color.web("#fef08a", Math.min(1.0, alpha + 0.4)));
                    gc3D.fillOval(p[0] - coreR * 0.4, p[1] - coreR * 0.4, coreR * 0.8, coreR * 0.8);
                }
            }
        }
    }

    private void drawWeatherOverlay3D(double w, double h) {
        if (activeSimulation == null || activeSimulation.getWeather() == null) {
            return;
        }

        org.swarmforge.core.world.WeatherSystem weather = activeSimulation.getWeather();
        org.swarmforge.core.world.WeatherMarkovChain.WeatherState state = weather.getWeatherState();
        if (state == null) state = org.swarmforge.core.world.WeatherMarkovChain.WeatherState.SUNNY;

        long timeMs = System.currentTimeMillis();
        double timeSec = timeMs / 1000.0;

        // 1. Dynamic Atmosphere & Lighting Vignette dimming based on Weather State & Light Level
        float lightLevel = weather.getLightLevel();
        if (lightLevel < 0.85f || state != org.swarmforge.core.world.WeatherMarkovChain.WeatherState.SUNNY) {
            double opacity = Math.max(0.0, Math.min(0.35, (0.85 - lightLevel) * 0.40));
            if (opacity > 0.01) {
                gc3D.setFill(Color.web("#020617", opacity));
                gc3D.fillRect(0, 0, w, h);
            }
        }

        // 2. Rolling Cloud Shadows for Overcast / Storm / Rain
        if (state == org.swarmforge.core.world.WeatherMarkovChain.WeatherState.OVERCAST ||
            state == org.swarmforge.core.world.WeatherMarkovChain.WeatherState.LIGHT_RAIN ||
            state == org.swarmforge.core.world.WeatherMarkovChain.WeatherState.HEAVY_RAIN ||
            state == org.swarmforge.core.world.WeatherMarkovChain.WeatherState.THUNDERSTORM ||
            state == org.swarmforge.core.world.WeatherMarkovChain.WeatherState.FOG) {

            gc3D.setFill(Color.web("#0f172a", 0.15));
            double cloudX1 = (timeMs * 0.02) % (w + 300) - 150;
            double cloudX2 = (timeMs * 0.035) % (w + 400) - 200;
            gc3D.fillOval(cloudX1, 10, 260, 60);
            gc3D.fillOval(cloudX2, 30, 320, 70);
        }

        // 3. Fog Effect Overlay
        if (state == org.swarmforge.core.world.WeatherMarkovChain.WeatherState.FOG) {
            gc3D.setFill(Color.web("#cbd5e1", 0.22));
            gc3D.fillRect(0, 0, w, h);
        }

        // 4. Precipitation Effects (Rain / Snow / Hail) aligned with Camera Yaw & Wind Direction
        float windSpeed = weather.getWindSpeed();
        double effectiveWindAngleRad = Math.toRadians(weather.getWindDirectionAngle()) - Math.toRadians(azimuth);
        double windDx = Math.cos(effectiveWindAngleRad) * (windSpeed * 0.35);
        double windDy = 14.0 + Math.sin(effectiveWindAngleRad) * (windSpeed * 0.15);

        Random r = new Random(54321);

        if (state == org.swarmforge.core.world.WeatherMarkovChain.WeatherState.LIGHT_RAIN ||
            state == org.swarmforge.core.world.WeatherMarkovChain.WeatherState.HEAVY_RAIN ||
            state == org.swarmforge.core.world.WeatherMarkovChain.WeatherState.THUNDERSTORM) {

            int particleCount = (state == org.swarmforge.core.world.WeatherMarkovChain.WeatherState.HEAVY_RAIN ||
                                 state == org.swarmforge.core.world.WeatherMarkovChain.WeatherState.THUNDERSTORM) ? 80 : 35;

            gc3D.setStroke(Color.web("#38bdf8", 0.50));
            gc3D.setLineWidth(1.1);
            for (int i = 0; i < particleCount; i++) {
                double rx = (r.nextDouble() * w + timeSec * (120.0 + windDx * 10.0) + i * 47) % w;
                double ry = (r.nextDouble() * h + timeSec * (350.0 + windDy * 10.0) + i * 61) % h;
                gc3D.strokeLine(rx, ry, rx + windDx, ry + windDy);
            }
        }

        // 5. Snow Flakes
        if (state == org.swarmforge.core.world.WeatherMarkovChain.WeatherState.SNOW ||
            state == org.swarmforge.core.world.WeatherMarkovChain.WeatherState.BLIZZARD) {

            int snowCount = (state == org.swarmforge.core.world.WeatherMarkovChain.WeatherState.BLIZZARD) ? 90 : 40;
            gc3D.setFill(Color.web("#f8fafc", 0.80));
            for (int i = 0; i < snowCount; i++) {
                double sx = (r.nextDouble() * w + timeSec * (40.0 + windDx * 5.0) + Math.sin(timeSec + i) * 15.0 + i * 31) % w;
                double sy = (r.nextDouble() * h + timeSec * (80.0 + windDy * 2.0) + i * 53) % h;
                double size = 1.5 + r.nextDouble() * 2.0;
                gc3D.fillOval(sx, sy, size, size);
            }
        }

        // 6. Hail Impact Particles (ONLY when weather state is HAIL)
        if (state == org.swarmforge.core.world.WeatherMarkovChain.WeatherState.HAIL) {
            gc3D.setFill(Color.web("#f8fafc", 0.90));
            for (int i = 0; i < 30; i++) {
                double hx = (r.nextDouble() * w + timeSec * 60.0 + i * 67) % w;
                double hy = (r.nextDouble() * h + timeSec * 420.0 + i * 89) % h;
                gc3D.fillOval(hx, hy, 3.0, 3.0);
                if (hy > h * 0.6) {
                    gc3D.setStroke(Color.web("#ffffff", 0.50));
                    gc3D.setLineWidth(0.9);
                    gc3D.strokeOval(hx - 3, hy - 1, 6, 2);
                }
            }
        }

        // 7. Thunderstorm Strobe Flash & Speed-of-Sound Delayed Thunder Strike
        if (state == org.swarmforge.core.world.WeatherMarkovChain.WeatherState.THUNDERSTORM) {
            if (r.nextDouble() < 0.02) {
                gc3D.setFill(Color.web("#ffffff", 0.35));
                gc3D.fillRect(0, 0, w, h);

                // Distance to lightning strike (simulated 3D distance 350m - 1400m)
                double strikeDistMeters = 350.0 + r.nextDouble() * 1050.0;
                org.swarmforge.client.audio.SimulationAudioManager.getInstance().triggerLightningThunderStrike(strikeDistMeters);
            }
        }
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
        boolean isAdvMode = true;

        double refZ0 = 32.0;
        double refZ1 = 32.0;
        for (int d = 0; d < SOIL_DEPTH; d++) {
            double topZ0 = Math.min(surfZ0, refZ0 - d * layerDepthPx);
            double topZ1 = Math.min(surfZ1, refZ1 - d * layerDepthPx);
            double botZ0 = Math.min(surfZ0, refZ0 - (d + 1) * layerDepthPx);
            double botZ1 = Math.min(surfZ1, refZ1 - (d + 1) * layerDepthPx);

            if (topZ0 <= botZ0 && topZ1 <= botZ1) continue;

            double[] pTop0 = project3DPoint(x0, y0, topZ0, cx, cy, scale, radAz, radEl);
            double[] pTop1 = project3DPoint(x1, y1, topZ1, cx, cy, scale, radAz, radEl);
            double[] pBot1 = project3DPoint(x1, y1, botZ1, cx, cy, scale, radAz, radEl);
            double[] pBot0 = project3DPoint(x0, y0, botZ0, cx, cy, scale, radAz, radEl);

            byte mat = soilLayers[x0][y0][d];
            boolean isVoid = voidGrid[x0][y0][d] && (showGalleriesCheck == null || showGalleriesCheck.isSelected());

            Color matCol;
            if (isVoid) {
                matCol = Color.web("#0f172a");
            } else if (!isMaterialVisible(mat)) {
                matCol = Color.web("#1e293b", 0.3);
            } else {
                matCol = getMaterialColor(mat);
                if (isAdvMode) {
                    double depthDim = 1.0 - (d / (double) SOIL_DEPTH) * 0.32;
                    matCol = matCol.deriveColor(0, 1.0, depthDim, 1.0);
                }
            }

            if (isSliceCutaway) {
                matCol = matCol.deriveColor(0, 1.1, 1.2, 1.0);
            }

            if (isTranslucent) {
                matCol = Color.color(matCol.getRed(), matCol.getGreen(), matCol.getBlue(), 0.55);
            }

            gc3D.setFill(matCol);
            gc3D.fillPolygon(new double[]{pTop0[0], pTop1[0], pBot1[0], pBot0[0]}, new double[]{pTop0[1], pTop1[1], pBot1[1], pBot0[1]}, 4);

            double wallInclNoise = valueNoise3D(x0 * 0.35, y0 * 0.35, d * 0.35);
            if (isAdvMode && showInclusions && !isVoid && isMaterialVisible(mat) && wallInclNoise > 0.68) {
                double midX = (pTop0[0] + pTop1[0] + pBot1[0] + pBot0[0]) / 4.0;
                double midY = (pTop0[1] + pTop1[1] + pBot1[1] + pBot0[1]) / 4.0;
                double voxSize = Math.max(1.2, Math.abs(pTop1[0] - pTop0[0]) * 0.25);

                if (mat == 4) { // Gravel / Cailloutis pebble inclusions
                    gc3D.setFill(Color.web("#cbd5e1"));
                    gc3D.fillOval(midX - voxSize * 0.3, midY - voxSize * 0.2, voxSize * 0.4, voxSize * 0.35);
                    gc3D.setFill(Color.web("#475569"));
                    gc3D.fillOval(midX + voxSize * 0.05, midY + voxSize * 0.05, voxSize * 0.25, voxSize * 0.2);
                } else if (mat == 5) { // Organic Litter / Matière Organique detritus
                    gc3D.setFill(Color.web("#78350f"));
                    gc3D.fillOval(midX - voxSize * 0.3, midY - voxSize * 0.2, voxSize * 0.5, voxSize * 0.3);
                    gc3D.setFill(Color.web("#523219"));
                    gc3D.fillRect(midX, midY, voxSize * 0.25, voxSize * 0.2);
                } else if (mat == 3) { // Rock / Stone inclusion
                    gc3D.setFill(Color.web("#e2e8f0"));
                    gc3D.fillOval(midX - voxSize * 0.35, midY - voxSize * 0.3, voxSize * 0.7, voxSize * 0.5);
                } else if (mat == 2) { // Clay inclusion
                    gc3D.setFill(Color.web("#7c2d12"));
                    gc3D.fillRect(midX - voxSize * 0.3, midY - voxSize * 0.2, voxSize * 0.6, voxSize * 0.4);
                } else if (mat == 1) { // Sand grain inclusion
                    gc3D.setFill(Color.web("#fef08a"));
                    gc3D.fillOval(midX - voxSize * 0.25, midY - voxSize * 0.25, voxSize * 0.5, voxSize * 0.5);
                }
            }

            // Natural Caverns & Subterranean Cavities Rendering (isVoid)
            if (isVoid) {
                double midX = (pTop0[0] + pTop1[0] + pBot1[0] + pBot0[0]) / 4.0;
                double midY = (pTop0[1] + pTop1[1] + pBot1[1] + pBot0[1]) / 4.0;
                // Cave ambient depth glow
                gc3D.setFill(Color.web("#0284c7", 0.22));
                gc3D.fillPolygon(new double[]{pTop0[0], pTop1[0], pBot1[0], pBot0[0]}, new double[]{pTop0[1], pTop1[1], pBot1[1], pBot0[1]}, 4);
                // Cave Stalactite Spikes
                gc3D.setFill(Color.web("#334155"));
                gc3D.fillPolygon(new double[]{midX - 2, midX + 2, midX}, new double[]{pTop0[1], pTop0[1], pTop0[1] + 6}, 3);
            }

            // Depth-mapped Soil Moisture Tensor Visualization (3D Array Indexing)
            if (showHumidityCheck != null && showHumidityCheck.isSelected() && humidityGrid != null) {
                float hum = humidityGrid[x0][y0][d];
                gc3D.setFill(Color.web("#0284c7", Math.min(0.70, hum * 0.50 + 0.05)));
                gc3D.fillPolygon(new double[]{pTop0[0], pTop1[0], pBot1[0], pBot0[0]}, new double[]{pTop0[1], pTop1[1], pBot1[1], pBot0[1]}, 4);
            }

            // Depth-mapped Soil pH Level Tensor Visualization (3D Array Indexing)
            if (showPhCheck != null && showPhCheck.isSelected() && phGrid != null) {
                float ph = phGrid[x0][y0][d] > 0 ? phGrid[x0][y0][d] : 6.5f;
                // Acidic (<6.5): Amber/Yellow #eab308, Neutral (6.5-7.5): Green #22c55e, Alkaline (>7.5): Deep Blue #1e40af
                Color phColor;
                if (ph < 6.5f) {
                    phColor = Color.web("#eab308", Math.min(0.65, (6.5 - ph) * 0.22 + 0.15));
                } else if (ph > 7.5f) {
                    phColor = Color.web("#1e40af", Math.min(0.65, (ph - 7.5) * 0.22 + 0.15));
                } else {
                    phColor = Color.web("#22c55e", 0.35);
                }
                gc3D.setFill(phColor);
                gc3D.fillPolygon(new double[]{pTop0[0], pTop1[0], pBot1[0], pBot0[0]}, new double[]{pTop0[1], pTop1[1], pBot1[1], pBot0[1]}, 4);
            }

            if (showRootsCheck != null && showRootsCheck.isSelected() && rootGrid != null && rootGrid[x0][y0][d] > 0.02f) {
                double midX = (pTop0[0] + pTop1[0] + pBot1[0] + pBot0[0]) / 4.0;
                double midY = (pTop0[1] + pTop1[1] + pBot1[1] + pBot0[1]) / 4.0;
                double rSc = Math.max(1.0, rootGrid[x0][y0][d] * 2.2 * (zoom / 7.5));
                gc3D.setStroke(Color.web("#78350f", Math.min(0.95, rootGrid[x0][y0][d] * 1.3)));
                gc3D.setLineWidth(rSc);
                gc3D.strokeLine(pTop0[0], pTop0[1], pBot1[0], pBot1[1]);
                gc3D.strokeLine(pTop1[0], pTop1[1], pBot0[0], pBot0[1]);
                gc3D.setFill(Color.web("#451a03"));
                gc3D.fillOval(midX - rSc, midY - rSc, rSc * 2, rSc * 2);
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
    }

    private boolean isMaterialVisible(byte mat) {
        if (mat == 0 && showEarthCheck != null && !showEarthCheck.isSelected()) return false;
        if (mat == 1 && showSandCheck != null && !showSandCheck.isSelected()) return false;
        if (mat == 2 && showClayCheck != null && !showClayCheck.isSelected()) return false;
        if (mat == 3 && showStoneCheck != null && !showStoneCheck.isSelected()) return false;
        if (mat == 4 && showGravelCheck != null && !showGravelCheck.isSelected()) return false;
        if (mat == 5 && showOrganicCheck != null && !showOrganicCheck.isSelected()) return false;
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

        double cutRatio = slicePlaneSlider != null ? (slicePlaneSlider.getValue() / 100.0) : 1.0;
        int cutX = Math.max(0, Math.min(GRID_SIZE - 1, (int) (GRID_SIZE * cutRatio)));

        double blockW = (w - 20.0) / GRID_SIZE;
        double blockH = (h - 90.0) / SOIL_DEPTH;

        double refSurfaceH = 0.50 * 25.0;
        for (int y = 0; y < GRID_SIZE; y++) {
            int yIdx = GRID_SIZE - 1 - y; // Inverted left-to-right to match 3D viewport orientation
            double surfaceH = heightGrid[cutX][yIdx] * 25.0;
            double pySurf = 65 - surfaceH;

            for (int d = 0; d < SOIL_DEPTH; d++) {
                double px = 10 + y * blockW;
                double topY = 65 - refSurfaceH + d * blockH;
                double botY = 65 - refSurfaceH + (d + 1) * blockH;

                if (botY <= pySurf) continue; // Upper soil layer excavated by river bed cut

                double pyDraw = Math.max(topY, pySurf);
                double hDraw = botY - pyDraw;

                byte mat = soilLayers[cutX][yIdx][d];
                boolean isVoid = voidGrid[cutX][yIdx][d] && (showGalleriesCheck == null || showGalleriesCheck.isSelected());
                if (isVoid || !isMaterialVisible(mat)) {
                    gcSide.setFill(Color.web("#0f172a"));
                } else {
                    gcSide.setFill(getMaterialColor(mat));
                }
                gcSide.fillRect(px, pyDraw, Math.max(1, blockW + 0.5), Math.max(1, hDraw));

                if (showHumidityCheck != null && showHumidityCheck.isSelected() && humidityGrid != null) {
                    float hum = humidityGrid[cutX][yIdx][d];
                    gcSide.setFill(Color.web("#0284c7", Math.min(0.60, hum * 0.50)));
                    gcSide.fillRect(px, pyDraw, Math.max(1, blockW + 0.5), Math.max(1, hDraw));
                }
                if (showPhCheck != null && showPhCheck.isSelected() && phGrid != null) {
                    float ph = phGrid[cutX][yIdx][d] > 0 ? phGrid[cutX][yIdx][d] : 6.5f;
                    Color phColor = ph < 6.5f ? Color.web("#eab308", 0.35) : (ph > 7.5f ? Color.web("#1e40af", 0.35) : Color.web("#22c55e", 0.25));
                    gcSide.setFill(phColor);
                    gcSide.fillRect(px, pyDraw, Math.max(1, blockW + 0.5), Math.max(1, hDraw));
                }
                if (showRootsCheck != null && showRootsCheck.isSelected() && rootGrid != null && rootGrid[cutX][yIdx][d] > 0.15f) {
                    gcSide.setStroke(Color.web("#78350f", 0.85));
                    gcSide.setLineWidth(Math.max(1.0, rootGrid[cutX][yIdx][d] * 2.0));
                    gcSide.strokeLine(px + blockW/2, pyDraw, px + blockW/2, pyDraw + hDraw);
                }
            }

            double px = 10 + y * blockW;
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
        for (int y = 0; y < GRID_SIZE; y += 2) {
            int yIdx = GRID_SIZE - 1 - y;
            if (carvedVoxelGrid[cutX][yIdx]) {
                double px = 10 + y * blockW;
                double py = 70;
                gcSide.fillOval(px, py, 6, 6);
            }
        }

        gcSide.restore();

        gcSide.setStroke(Color.web("#38bdf8"));
        gcSide.setLineWidth(1.0);
        gcSide.strokeRect(10, 10, w - 20, h - 20);

        gcSide.setFill(Color.WHITE);
        gcSide.setFont(Font.font("System", javafx.scene.text.FontWeight.BOLD, 10));
        gcSide.fillText(String.format(Locale.US, "Profil Stratigraphique Scanner 3D (Coupe X=%d — Scanner: %.0f%%)", cutX, cutRatio * 100.0), 20, 30);
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
        double cutRatio = slicePlaneSlider != null ? (slicePlaneSlider.getValue() / 100.0) : 1.0;

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
                    gcTop.setGlobalAlpha(humidityGrid[x][y][0] * 0.6);
                    gcTop.fillRect(15 + x * cellW + cx, 15 + y * cellH + cy, cellW * 2, cellH * 2);
                    gcTop.setGlobalAlpha(1.0);
                }
            }
        }

        if (isTerrainVisible && riverCheck != null && riverCheck.isSelected() && riverPath != null && riverPath.size() > 1) {
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
            List<BotanicalTreeData> treeDataList = getBotanicalTreeInstances();
            double sideM = surfaceSizeSlider != null ? surfaceSizeSlider.getValue() : 25.0;

            for (BotanicalTreeData btd : treeDataList) {
                int gx = btd.gx;
                int gy = btd.gy;
                double baseTreeHeightM = switch (btd.speciesIdx) {
                    case 0 -> 6.0;   // Bambou
                    case 1 -> 2.5;   // Souche
                    case 2 -> 12.0;  // Bouleau
                    case 3 -> 4.0;   // Cactus
                    case 4 -> 15.0;  // Chêne
                    case 5 -> 18.0;  // Pin
                    case 6 -> 8.0;   // Acacia
                    default -> 15.0;
                };
                double topTreeRadius = Math.max(6.0, (baseTreeHeightM * 0.30 * btd.ageScale) * ((cellW * GRID_SIZE) / Math.max(1.0, sideM)) * tZoom);

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

        // Draw Root Network on Top View Minimap when toggled ON
        if (showRootsCheck != null && showRootsCheck.isSelected()) {
            gcTop.setStroke(Color.web("#78350f", 0.75));
            gcTop.setLineWidth(1.2 * tZoom);
            for (int rx = 0; rx < GRID_SIZE; rx += 4) {
                for (int ry = 0; ry < GRID_SIZE; ry += 4) {
                    if (rootGrid[rx][ry][0] > 0.02f) {
                        double rpx = 15 + rx * cellW + cx;
                        double rpy = 15 + ry * cellH + cy;
                        gcTop.strokeOval(rpx - 3 * tZoom, rpy - 3 * tZoom, 6 * tZoom, 6 * tZoom);
                    }
                }
            }
        }

        // Draw 2D Pheromone Heatmap Overlay on Top View Minimap when toggled ON
        if (isSimulationMode && activeSimulation != null && isPheromonesVisible) {
            org.swarmforge.core.gpu.SparsePheromoneGrid pGrid = activeSimulation.getPheromoneGrid();
            if (pGrid != null) {
                java.util.Map<Long, float[]> pEntries = pGrid.getAllEntries();
                if (!pEntries.isEmpty()) {
                    for (java.util.Map.Entry<Long, float[]> e : pEntries.entrySet()) {
                        long key = e.getKey();
                        float[] vals = e.getValue();
                        if (vals == null) continue;

                        int[] coords = org.swarmforge.core.spatial.Morton3D.decode(key);
                        int px = coords[0];
                        int py = coords[1];

                        if (px < 0 || px >= GRID_SIZE || py < 0 || py >= GRID_SIZE) continue;

                        float homeVal = (vals.length > 0 && showHomePheromone) ? vals[0] : 0f;
                        float foodVal = (vals.length > 1 && showFoodPheromone) ? vals[1] : 0f;
                        float alarmVal = (vals.length > 2 && showAlarmPheromone) ? vals[2] : 0f;
                        float recruitVal = (vals.length > 3 && showRecruitmentPheromone) ? vals[3] : 0f;
                        float queenVal = (vals.length > 4 && showQueenPheromone) ? vals[4] : 0f;
                        float deathVal = (vals.length > 5 && showDeathPheromone) ? vals[5] : 0f;

                        float maxVal = Math.max(homeVal, Math.max(foodVal, Math.max(alarmVal, Math.max(recruitVal, Math.max(queenVal, deathVal)))));
                        if (maxVal < 0.02f) continue;

                        String colorHex = (alarmVal == maxVal) ? "#ef4444" :
                                         (foodVal == maxVal) ? "#a855f7" :
                                         (homeVal == maxVal) ? "#0284c7" :
                                         (recruitVal == maxVal) ? "#f59e0b" :
                                         (queenVal == maxVal) ? "#ec4899" : "#64748b";

                        double topPx = 15 + px * cellW + cx;
                        double topPy = 15 + py * cellH + cy;
                        double dotR = cellW * (1.2 + maxVal * 1.5);

                        gcTop.setFill(Color.web(colorHex, Math.min(0.85, maxVal * 0.7)));
                        gcTop.fillOval(topPx - dotR * 0.5, topPy - dotR * 0.5, dotR, dotR);
                    }
                }
            }
        }

        // Draw Scanner 3D Cut Line Overlay on Top View Minimap
        if (cutRatio < 0.99) {
            int cutX = Math.max(0, Math.min(GRID_SIZE - 1, (int) (GRID_SIZE * cutRatio)));
            double scanLineX = 15 + cutX * cellW + cx;
            gcTop.setFill(Color.web("#22c55e"));
            gcTop.setFont(Font.font("System", javafx.scene.text.FontWeight.BOLD, 10));
            gcTop.fillText(String.format(Locale.US, "📡 Scanner Coupe 3D (%d%%)", (int)(cutRatio * 100)), Math.min(w - 140, scanLineX + 5), 25);
        }

        gcTop.restore();

        gcTop.setStroke(Color.web("#38bdf8"));
        gcTop.setLineWidth(1.0);
        gcTop.strokeRect(10, 10, w - 20, h - 20);

        gcTop.setFill(Color.WHITE);
        double sM = surfaceSizeSlider != null ? surfaceSizeSlider.getValue() : 2.0;
        gcTop.fillText(String.format("Top-Down View (Side: %.1fm — Area: %.1fm²)", sM, sM * sM), 20, 30);
    }

    private Color getMaterialColor(byte mat) {
        switch (mat) {
            case 1: return Color.web("#eab308"); // Sand
            case 2: return Color.web("#9a3412"); // Clay
            case 3: return Color.web("#64748b"); // Stone (Pierre / Roche)
            case 4: return Color.web("#94a3b8"); // Gravel (Gravier / Cailloutis)
            case 5: return Color.web("#ca8a04"); // Silt (Limon)
            case 6: return Color.web("#451a03"); // Peat (Tourbe)
            case 7: return Color.web("#0f172a"); // Cavity / Natural Void
            case 8: return Color.web("#523219"); // Organic Litter
            default: return Color.web("#3d2817"); // Earth (Humus)
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
            if (s != null) {
                s.valueProperty().addListener((o, a, b) -> repaintAllViews());
            }
        }
    }


    private void triggerGenerate() {
        if (onGenerateCallback != null) {
            onGenerateCallback.accept(getConfiguration());
        } else {
            Alert alert = org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.INFORMATION, "World Generation executed (Sub-millimeter resolution: " + resolutionSlider.getValue() + "mm).");
            alert.setTitle("World Editor");
            alert.setHeaderText("World Generated Successfully");
            alert.show();
        }
    }

    public void setOnGenerate(Consumer<Map<String, Object>> cb) {
        this.onGenerateCallback = cb;
    }

    public Map<String, Object> getConfiguration() {
        Map<String, Object> cfg = new LinkedHashMap<>();
        cfg.put("presetName", presetsCombo != null && presetsCombo.getValue() != null ? presetsCombo.getValue() : "Custom World");
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

        cfg.put("surfaceSizeMeters", surfaceSizeSlider != null ? surfaceSizeSlider.getValue() : 25.0);
        cfg.put("depthMeters", depthSlider != null ? depthSlider.getValue() : 1.5);
        cfg.put("resolutionMm", resolutionSlider != null ? resolutionSlider.getValue() : 25.0);
        cfg.put("roughness", roughnessSlider != null ? roughnessSlider.getValue() : 0.45);
        cfg.put("compaction", compactionSlider != null ? compactionSlider.getValue() : 0.6);
        cfg.put("stratification", stratificationSlider != null ? stratificationSlider.getValue() : 0.7);
        cfg.put("mixingRate", mixingRateSlider != null ? mixingRateSlider.getValue() : 0.3);
        cfg.put("baseHumidity", baseHumiditySlider != null ? baseHumiditySlider.getValue() : 0.35);
        cfg.put("voidDensity", voidDensitySlider != null ? voidDensitySlider.getValue() : 0.08);

        Map<String, Object> soilMap = new LinkedHashMap<>();
        soilMap.put("earth", earthSpinner != null ? earthSpinner.getValue() : 50);
        soilMap.put("sand", sandSpinner != null ? sandSpinner.getValue() : 20);
        soilMap.put("clay", claySpinner != null ? claySpinner.getValue() : 20);
        soilMap.put("stone", stoneSpinner != null ? stoneSpinner.getValue() : 10);
        soilMap.put("organic", organicSpinner != null ? organicSpinner.getValue() : 10);
        cfg.put("soilComposition", soilMap);

        cfg.put("treeSpeciesIndex", comboTreeSpecies != null ? comboTreeSpecies.getSelectionModel().getSelectedIndex() : 0);

        Map<String, Object> treeMap = new LinkedHashMap<>();
        treeMap.put("oak", oakPctSpinner != null ? oakPctSpinner.getValue() : 40);
        treeMap.put("pine", pinePctSpinner != null ? pinePctSpinner.getValue() : 30);
        treeMap.put("acacia", acaciaPctSpinner != null ? acaciaPctSpinner.getValue() : 10);
        treeMap.put("cactus", cactusPctSpinner != null ? cactusPctSpinner.getValue() : 5);
        treeMap.put("birch", birchPctSpinner != null ? birchPctSpinner.getValue() : 5);
        treeMap.put("bamboo", bambooPctSpinner != null ? bambooPctSpinner.getValue() : 5);
        treeMap.put("deadWood", deadWoodPctSpinner != null ? deadWoodPctSpinner.getValue() : 5);
        cfg.put("treeComposition", treeMap);

        cfg.put("edibleFloraDensity", edibleDensitySlider != null ? edibleDensitySlider.getValue() : 35.0);
        cfg.put("nonEdibleFloraDensity", nonEdibleDensitySlider != null ? nonEdibleDensitySlider.getValue() : 25.0);
        cfg.put("leafLitter", leafLitterSlider != null ? leafLitterSlider.getValue() : 15.0);
        cfg.put("twigDebris", twigDebrisSlider != null ? twigDebrisSlider.getValue() : 10.0);

        cfg.put("aphidPlant", aphidPlantCheck != null && aphidPlantCheck.isSelected());
        cfg.put("nectarFlowers", nectarFlowersCheck != null && nectarFlowersCheck.isSelected());
        cfg.put("seedGrass", seedGrassCheck != null && seedGrassCheck.isSelected());
        cfg.put("fungusFoliage", fungusFoliageCheck != null && fungusFoliageCheck.isSelected());
        cfg.put("moss", mossCheck != null && mossCheck.isSelected());
        cfg.put("pineLitter", pineLitterCheck != null && pineLitterCheck.isSelected());
        cfg.put("fernObstacle", fernObstacleCheck != null && fernObstacleCheck.isSelected());

        cfg.put("hasRiver", riverCheck != null && riverCheck.isSelected());
        cfg.put("riverWidthMm", riverWidthSlider != null ? riverWidthSlider.getValue() : 120.0);
        cfg.put("riverVelocity", riverVelocitySlider != null ? riverVelocitySlider.getValue() : 0.5);
        cfg.put("staticPools", staticPoolsSlider != null ? staticPoolsSlider.getValue() : 2.0);
        cfg.put("waterTableDepthCm", waterTableDepthSlider != null ? waterTableDepthSlider.getValue() : 15.0);
        cfg.put("treeCount", treeCountSlider != null ? (int) treeCountSlider.getValue() : 8);
        cfg.put("hollowLogs", hollowLogsSlider != null ? (int) hollowLogsSlider.getValue() : 4);
        cfg.put("rockCrevices", rockCrevicesSlider != null ? (int) rockCrevicesSlider.getValue() : 6);
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
            org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.INFORMATION, "Preset saved successfully.").show();
        } catch (Exception ex) {
            org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.ERROR, "Erreur d'exportation: " + ex.getMessage()).show();
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
            org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.INFORMATION, "World preset loaded.").show();
        } catch (Exception ex) {
            org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.ERROR, "Erreur d'importation: " + ex.getMessage()).show();
        }
    }

    public void loadConfiguration(Map<String, Object> cfg) {
        if (cfg == null) return;
        isUpdatingFields = true;
        try {
            if (cfg.containsKey("presetName") && presetsCombo != null) {
                String pName = String.valueOf(cfg.get("presetName"));
                if (presetsCombo.getItems().contains(pName)) {
                    presetsCombo.setValue(pName);
                }
            }
            if (cfg.containsKey("cityName") && cityNameField != null) {
                cityNameField.setText(String.valueOf(cfg.get("cityName")));
            } else if (cfg.containsKey("presetName") && cityNameField != null) {
                String pName = String.valueOf(cfg.get("presetName"));
                if (pName.contains("(") && pName.contains(")")) {
                    cityNameField.setText(pName.substring(pName.indexOf("(") + 1, pName.indexOf(")")));
                }
            }
            if (citySearchField != null && cityNameField != null) {
                citySearchField.setText(cityNameField.getText());
            }
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
                if (geoStatusLabel != null) {
                    String cName = cityNameField != null ? cityNameField.getText() : "Serengeti, TZ";
                    geoStatusLabel.setText("🟢 " + cName + " (Lat: " + String.format(java.util.Locale.US, "%.4f", lat) + "°, Lon: " + String.format(java.util.Locale.US, "%.4f", lon) + "°)");
                    geoStatusLabel.setStyle("-fx-text-fill: #4ade80; -fx-font-size: 10px;");
                }
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
        } finally {
            isUpdatingFields = false;
        }

        regenerateAndRepaint();
    }

    private Color getGaussianSplatSoilColor(int x, int y) {
        double rAcc = 0.0, gAcc = 0.0, bAcc = 0.0;
        double totalWeight = 0.0;
        int radius = 2;
        double sigma = 1.25;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                int nx = Math.min(GRID_SIZE - 1, Math.max(0, x + dx));
                int ny = Math.min(GRID_SIZE - 1, Math.max(0, y + dy));
                byte nMat = soilLayers[nx][ny][0];
                if (!isMaterialVisible(nMat)) continue;

                double distSq = dx * dx + dy * dy;
                double w = Math.exp(-distSq / (2.0 * sigma * sigma));
                Color c = getMaterialColor(nMat);

                rAcc += c.getRed() * w;
                gAcc += c.getGreen() * w;
                bAcc += c.getBlue() * w;
                totalWeight += w;
            }
        }

        if (totalWeight <= 0.001) return getMaterialColor(soilLayers[x][y][0]);
        return Color.color(
            Math.min(1.0, Math.max(0.0, rAcc / totalWeight)),
            Math.min(1.0, Math.max(0.0, gAcc / totalWeight)),
            Math.min(1.0, Math.max(0.0, bAcc / totalWeight))
        );
    }

    private HBox sv(Slider s) {
        return sv(s, "");
    }

    private HBox sv(Slider s, String unit) {
        HBox b = new HBox(8);
        b.setAlignment(Pos.CENTER_LEFT);
        Label v = new Label(fmt(s != null ? s.getValue() : 0.0) + (unit == null || unit.isEmpty() ? "" : " " + unit));
        v.setStyle("-fx-text-fill:#00d4ff;-fx-min-width:36;-fx-font-weight:bold;");
        if (s != null) {
            s.valueProperty().addListener((o, a, n) -> v.setText(fmt(n.doubleValue()) + (unit == null || unit.isEmpty() ? "" : " " + unit)));
            b.getChildren().add(s);
        }
        b.getChildren().add(v);
        return b;
    }

    private String fmt(double d) {
        if (d == (long) d) {
            return String.format("%d", (long) d);
        }
        return String.format("%.1f", d);
    }

    private boolean isForestArea(int gx, int gy) {
        return (gx * 17 + gy * 31) % 11 == 0;
    }

    public void triggerWildfire(int cx, int cy, int radius) {
        if (surfaceFloraItems != null) {
            for (SurfaceFloraItem item : surfaceFloraItems) {
                double distSq = (item.gx - cx) * (item.gx - cx) + (item.gy - cy) * (item.gy - cy);
                if (distSq <= radius * radius) {
                    item.isCharred = true;
                    if (item.type < 4) {
                        item.type = 5; // Convert green flora to charred twig remnant
                    }
                }
            }
        }
        if (tempGrid != null && humidityGrid != null && phGrid != null) {
            for (int x = Math.max(0, cx - radius); x <= Math.min(GRID_SIZE - 1, cx + radius); x++) {
                for (int y = Math.max(0, cy - radius); y <= Math.min(GRID_SIZE - 1, cy + radius); y++) {
                    double distSq = (x - cx) * (x - cx) + (y - cy) * (y - cy);
                    if (distSq <= radius * radius) {
                        for (int d = 0; d < SOIL_DEPTH; d++) {
                            humidityGrid[x][y][d] = 0.0f; // Evaporates all soil moisture
                            phGrid[x][y][d] = Math.min(10.0f, phGrid[x][y][d] + 1.2f); // Alkaline ash deposit
                            tempGrid[x][y][d] = Math.min(120.0f, tempGrid[x][y][d] + 85.0f); // Thermal spike
                        }
                    }
                }
            }
        }
        repaintAllViews();
    }

    public void triggerFlood(int cx, int cy, int radius) {
        if (tempGrid != null && humidityGrid != null) {
            for (int x = Math.max(0, cx - radius); x <= Math.min(GRID_SIZE - 1, cx + radius); x++) {
                for (int y = Math.max(0, cy - radius); y <= Math.min(GRID_SIZE - 1, cy + radius); y++) {
                    double distSq = (x - cx) * (x - cx) + (y - cy) * (y - cy);
                    if (distSq <= radius * radius) {
                        for (int d = 0; d < SOIL_DEPTH; d++) {
                            humidityGrid[x][y][d] = 1.0f; // Saturate moisture to 100%
                            tempGrid[x][y][d] = Math.max(5.0f, tempGrid[x][y][d] - 12.0f); // Water cooling effect
                        }
                    }
                }
            }
        }
        repaintAllViews();
    }

    public void triggerDrought() {
        if (tempGrid != null && humidityGrid != null) {
            for (int x = 0; x < GRID_SIZE; x++) {
                for (int y = 0; y < GRID_SIZE; y++) {
                    for (int d = 0; d < SOIL_DEPTH; d++) {
                        humidityGrid[x][y][d] = Math.max(0.02f, humidityGrid[x][y][d] * 0.25f);
                        tempGrid[x][y][d] = Math.min(45.0f, tempGrid[x][y][d] + 8.0f);
                    }
                }
            }
        }
        repaintAllViews();
    }

    public double getZoom() {
        return zoom;
    }

    public void setZoom(double z) {
        this.zoom = Math.max(2.5, Math.min(22.0, z));
        if (isSync()) {
            this.sideZoom = Math.max(0.3, Math.min(6.0, zoom / 7.5));
            this.topZoom = sideZoom;
        }
        repaintAllViews();
    }
}
