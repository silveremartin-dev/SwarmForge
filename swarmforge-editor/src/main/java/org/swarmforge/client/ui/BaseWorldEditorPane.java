package org.swarmforge.client.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.swarmforge.client.ui.world.*;
import org.swarmforge.client.view.GameViewPane;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.simulation.TunnelNetwork.TunnelNode;

import java.util.*;
import java.util.function.Consumer;

/**
 * Base World Editor Pane holding UI components, 3D grids, camera parameters, and accessors.
 *
 * Grounded in the SwarmForge modular architecture to maintain classes strictly below 1,000 lines.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public abstract class BaseWorldEditorPane extends BorderPane {

    public static final int GRID_SIZE = 128;
    public static final int SOIL_DEPTH = 32;
    public static final int MAX_TRAIL_LENGTH = 120;



    // Viewport Canvases
    ResizableCanvas canvas3D, canvasSide, canvasTop;
    GraphicsContext gc3D, gcSide, gcTop;

    boolean isActive = true;
    GameViewPane gameView;

    // 3D Camera Controls
    double azimuth = 45, elevation = 35, zoom = 7.5;
    double pan3DX = 0, pan3DY = 0;
    double lastMX, lastMY;

    // 2D Minimap Cameras
    double sideZoom = 1.0;
    double sidePanX = 0, sidePanY = 0;
    double lastSideMX, lastSideMY;
    double topZoom = 1.0;
    double topPanX = 0, topPanY = 0;
    double lastTopMX, lastTopMY;
    double previousZoom = 7.5;

    // Preset & Species Adaptation
    final WorldPresetManager presetManager = new WorldPresetManager();
    final TerrainUndoManager undoManager = new TerrainUndoManager();
    ComboBox<String> presetsCombo;
    ComboBox<String> speciesAdaptCombo;
    Label lblAdaptStatus;

    // View Synchronization & Layout
    CheckBox syncViewsCheckBox;
    ScrollPane rightScroll;
    VBox headerBox;
    HBox presetRow;
    Label headerTitleLabel;
    Label headerSubtitleLabel;

    // Geographic Coordinates & Seeds
    TextField cityNameField;
    TextField citySearchField;
    Label geoStatusLabel;
    TextField latField;
    TextField lonField;
    TextField altField;

    TextField scaleSeedField;
    TextField reliefSeedField;
    TextField soilSeedField;
    TextField hydroSeedField;
    TextField structSeedField;
    TextField floraSeedField;

    Slider slopeAngleSlider;
    ComboBox<String> slopeDirectionCombo;

    // Layer & Soil Substrate Toggles
    CheckBox showTerrainCheck;
    CheckBox showEarthCheck;
    CheckBox showSandCheck;
    CheckBox showClayCheck;
    CheckBox showSiltCheck;
    CheckBox showPeatCheck;
    CheckBox showStoneCheck;
    CheckBox showGravelCheck;
    CheckBox showOrganicCheck;
    CheckBox showVegetationCheck;
    CheckBox showGalleriesCheck;
    CheckBox showSubstrateStratigraphyCheck;
    CheckBox showChamferedBezelCheck;
    CheckBox showGravelInclusionsCheck;
    CheckBox showTranslucentVolumetricModeCheck;

    // Inspector HUD
    Label lblHoverInfo;
    VBox floatingMouseInspectorCard;
    Label lblFInspTitle, lblFInspAlt, lblFInspSubstrat, lblFInspClimate, lblFInspChem, lblFInspGas, lblFInspPhero, lblFInspBio;
    double lastGameViewMouseX = 0, lastGameViewMouseY = 0;
    double hoverMX = 0, hoverMY = 0;
    StackPane h3d;

    // Sliders & Spinners: Scale, Soil, Relief
    Slider surfaceSizeSlider;
    Slider depthSlider;
    Slider resolutionSlider;
    Label lblVoxelMemoryEstimate;

    Slider roughnessSlider;
    Slider compactionSlider;
    Spinner<Integer> earthSpinner;
    Spinner<Integer> sandSpinner;
    Spinner<Integer> siltSpinner;
    Spinner<Integer> claySpinner;
    Spinner<Integer> gravelSpinner;
    Spinner<Integer> peatSpinner;
    Spinner<Integer> stoneSpinner;
    Spinner<Integer> organicSpinner;

    // Flora & Ecosystem
    Slider leafLitterSlider;
    Slider twigDebrisSlider;
    Slider edibleDensitySlider;
    Slider nonEdibleDensitySlider;
    CheckBox aphidPlantCheck;
    CheckBox nectarFlowersCheck;
    CheckBox seedGrassCheck;
    CheckBox fungusFoliageCheck;
    CheckBox mossCheck;
    CheckBox pineLitterCheck;
    CheckBox fernObstacleCheck;
    List<WorldEditorPane.SurfaceFloraItem> surfaceFloraItems = new ArrayList<>();

    WorldEditorPane.RenderMode currentRenderMode = WorldEditorPane.RenderMode.REALISTIC;
    boolean isSimulationMode = false;
    VBox nestLegendBox, pheroLegendBox, castesLegendBox;
    boolean isTerrainVisible = true;
    boolean isGalleriesVisible = true;
    boolean isPheromonesVisible = true;
    boolean isColonyVisible = true;
    boolean isWeatherVisible = true;
    boolean isDualMinimapVisible = true;
    boolean isVoxelInfoVisible = true;
    boolean isAntTrackingEnabled = true;
    CheckBox showMinimapCheck;
    CheckBox showVoxelInfoCheck;
    CheckBox enableAntTrackingCheck;
    boolean isUVVisionMode = false;
    CheckBox showUVVisionModeCheck;
    boolean isOmmatidialVisionMode = false;
    CheckBox showOmmatidialVisionModeCheck;

    WorldEditorPane.PheromoneRenderMode pheromoneRenderMode = WorldEditorPane.PheromoneRenderMode.HEATMAP_GRADIENT;
    boolean showFoodPheromone = true;
    boolean showHomePheromone = true;
    boolean showAlarmPheromone = true;
    boolean showRecruitmentPheromone = true;
    boolean showQueenPheromone = true;
    boolean showDeathPheromone = true;

    double antVisualScaleMultiplier = 1.0;
    boolean showDebugRuler = false;

    ScrollPane configScrollPane;
    VBox sideMinimapsBox;
    VBox rightRenderOptions;
    VBox legendPanel;
    VBox legendContentBox;
    Separator legendSeparator;

    boolean isGalleriesLegendHUDVisible = false;
    long lastMeteoPollMs = 0;
    String cachedMeteoReportText = "";
    String cachedPhotoperiodText = "";

    // Hydrology
    CheckBox riverCheck;
    Slider riverWidthSlider;
    Slider riverVelocitySlider;
    Slider staticPoolsSlider;
    Slider waterTableDepthSlider;

    // Vertical Structures & Trees
    Slider treeCountSlider;
    Slider hollowLogsSlider;
    Slider rockCrevicesSlider;
    ComboBox<String> comboTreeSpecies;
    Spinner<Integer> oakPctSpinner;
    Spinner<Integer> pinePctSpinner;
    Spinner<Integer> acaciaPctSpinner;
    Spinner<Integer> birchPctSpinner;
    Spinner<Integer> bambooPctSpinner;
    Spinner<Integer> cactusPctSpinner;
    Spinner<Integer> deadWoodPctSpinner;

    Label lblBioclimaticZoneBadge = new Label("🌳 Temperate Deciduous Forest");
    Label lblAttaCompatScore = new Label("85%");
    Label lblAphidCompatScore = new Label("70%");
    Label lblWoodNestCompatScore = new Label("90%");
    Label lblAcaciaAntCompatScore = new Label("60%");
    Label lblCactusAntCompatScore = new Label("40%");
    Label lblPogonomyrmexCompatScore = new Label("75%");
    Label lblTermiteCompatScore = new Label("80%");
    Label lblApisCompatScore = new Label("85%");
    Label lblVespulaCompatScore = new Label("70%");
    Label lblSolenopsisCompatScore = new Label("65%");

    // Sculpting
    CheckBox enableSculptingCheck;
    ComboBox<String> brushModeSelect;
    ComboBox<String> brushSubstrateSelect;
    Slider brushRadiusSlider;
    Slider brushStrengthSlider;

    // Simulation & Tracking
    Simulation activeSimulation;
    Individual followedAnt = null;
    final List<double[]> antTrailHistory = new ArrayList<>();
    boolean isFollowAntCameraEnabled = false;
    CameraFollowMode cameraFollowMode = CameraFollowMode.FREE;
    TrackedIndividualPane trackedAntPane;
    ChamberInfoPane chamberInfoPane;
    VBox weatherOverlayPane;
    boolean isWeatherOverlayVisible = true;
    Label lblWeatherCoords, lblWeatherTemp, lblWeatherHum, lblWeatherLight, lblWeatherWindSpd, lblWeatherWindDir, lblWeatherRain, lblWeatherPressure, lblWeatherMoon, lblWeatherStatus, lblWeatherPhotoperiod;
    TunnelNode selectedChamberNode = null;
    CheckBox showAntTrackingCheck;
    CheckBox showChamberOverlayCheck;
    CheckBox showLegendCheckBox;
    CheckBox showElevationIsolinesCheck;
    CheckBox showClimateIsolinesCheck;
    CheckBox showPheromoneIsolinesCheck;
    Label lblSideMinimapTitle;
    Label lblTopdownMinimapTitle;

    double simWindSpeed = 15.0;
    String simWindDirection = "SW";
    String simSeason = "Summer";

    // Substrate 3D Grids & Layers
    final WorldEditor2DMinimapRenderer minimapRenderer = new WorldEditor2DMinimapRenderer();
    final WorldStratigraphy3DRenderer stratigraphyRenderer = new WorldStratigraphy3DRenderer();
    final WorldEntityMorphologyRenderer morphologyRenderer = new WorldEntityMorphologyRenderer();
    final WorldNestArchitecture3DRenderer nestArchitectureRenderer = new WorldNestArchitecture3DRenderer();

    double[][] heightGrid = new double[GRID_SIZE][GRID_SIZE];
    byte[][][] soilLayers = new byte[GRID_SIZE][GRID_SIZE][SOIL_DEPTH];
    float[][][] humidityGrid = new float[GRID_SIZE][GRID_SIZE][SOIL_DEPTH];
    boolean[][][] voidGrid = new boolean[GRID_SIZE][GRID_SIZE][SOIL_DEPTH];
    float[][][] rootGrid = new float[GRID_SIZE][GRID_SIZE][SOIL_DEPTH];
    float[][][] tempGrid = new float[GRID_SIZE][GRID_SIZE][SOIL_DEPTH];
    float[][][] phGrid = new float[GRID_SIZE][GRID_SIZE][SOIL_DEPTH];
    boolean[][] carvedVoxelGrid = new boolean[GRID_SIZE][GRID_SIZE];
    List<int[]> riverPath = new ArrayList<>();

    Slider stratificationSlider;
    Slider mixingRateSlider;
    Slider baseHumiditySlider;
    Slider voidDensitySlider;
    Slider basePhSlider;
    Slider rootDensitySlider;
    CheckBox showHumidityCheck;
    CheckBox showRootsCheck;
    CheckBox showPhCheck;
    Slider slicePlaneSlider;

    Consumer<Map<String, Object>> onGenerateCallback;
    Consumer<Individual> onIndividualSelectedListener;
    Consumer<Double> slicePlaneChangeListener;
    final Deque<UUID> individualNavigationHistory = new ArrayDeque<>();
    int[] hover3DCell = null;

    boolean isDirty = false;
    boolean isUpdatingFields = false;
    String lastSelectedPreset = null;

    public TerrainUndoManager getUndoManager() { return undoManager; }
    public boolean isDualMinimapVisible() { return sideMinimapsBox != null && sideMinimapsBox.isVisible(); }
    public VBox getLegendPanel() { return legendPanel; }
    public Label getLblHoverInfo() { return lblHoverInfo; }
    public double[][] getHeightGrid() { return heightGrid; }
    public byte[][][] getSoilLayers() { return soilLayers; }
    public boolean[][][] getVoidGrid() { return voidGrid; }
    public boolean[][] getCarvedVoxelGrid() { return carvedVoxelGrid; }
    public float[][][] getHumidityGrid() { return humidityGrid; }
    public float[][][] getPhGrid() { return phGrid; }
    public float[][][] getRootGrid() { return rootGrid; }
    public boolean isGalleriesVisible() { return isGalleriesVisible; }
    public boolean isShowGalleries() { return showGalleriesCheck == null || showGalleriesCheck.isSelected(); }
    public boolean isShowHumidity() { return showHumidityCheck != null && showHumidityCheck.isSelected(); }
    public boolean isShowPh() { return showPhCheck != null && showPhCheck.isSelected(); }
    public boolean isShowRoots() { return showRootsCheck != null && showRootsCheck.isSelected(); }
    public boolean isTranslucentVolumetricMode() { return showTranslucentVolumetricModeCheck != null && showTranslucentVolumetricModeCheck.isSelected(); }
    public boolean isShowGravelInclusions() { return showGravelInclusionsCheck != null && showGravelInclusionsCheck.isSelected(); }
    public boolean isShowChamferedBezel() { return showChamferedBezelCheck != null && showChamferedBezelCheck.isSelected(); }
    public double getWaterTableDepthMeters() {
        double wtDepthCm = waterTableDepthSlider != null ? waterTableDepthSlider.getValue() : 1500;
        return wtDepthCm / 100.0;
    }
    public double getTotalDepthMeters() { return depthSlider != null ? depthSlider.getValue() : 3.0; }
    public double getTopZoom() { return topZoom; }
    public double getTopPanX() { return topPanX; }
    public double getTopPanY() { return topPanY; }
    public double getSliceCutRatio() { return slicePlaneSlider != null ? (slicePlaneSlider.getValue() / 100.0) : 1.0; }
    public Label getLblSideMinimapTitle() { return lblSideMinimapTitle; }
    public Label getLblTopdownMinimapTitle() { return lblTopdownMinimapTitle; }
    public boolean isTerrainVisible() { return isTerrainVisible; }
    public String getSpeciesAdaptationValue() { return speciesAdaptCombo != null && speciesAdaptCombo.getValue() != null ? speciesAdaptCombo.getValue() : ""; }
    public String getSimSeason() { return simSeason; }
    public double getWaterTableDepthSliderValue() { return waterTableDepthSlider != null ? waterTableDepthSlider.getValue() : 15.0; }
    public double getBaseHumiditySliderValue() { return baseHumiditySlider != null ? baseHumiditySlider.getValue() : 0.35; }
    public boolean hasRiver() { return riverCheck != null && riverCheck.isSelected(); }
    public List<int[]> getRiverPath() { return riverPath; }
    public double getRiverWidthSliderValue() { return riverWidthSlider != null ? riverWidthSlider.getValue() : 100.0; }
    public int getHollowLogsCount() { return hollowLogsSlider != null ? (int) hollowLogsSlider.getValue() : 0; }
    public List<WorldEditorPane.SurfaceFloraItem> getSurfaceFloraItems() { return surfaceFloraItems; }
    public boolean isShowVegetation() { return treeCountSlider != null && (showVegetationCheck == null || showVegetationCheck.isSelected()); }
    public double getSurfaceSizeMeters() { return surfaceSizeSlider != null ? surfaceSizeSlider.getValue() : 25.0; }
    public boolean isSimulationMode() { return isSimulationMode; }
    public boolean isPheromonesVisible() { return isPheromonesVisible; }
    public boolean isShowHomePheromone() { return showHomePheromone; }
    public boolean isShowFoodPheromone() { return showFoodPheromone; }
    public boolean isShowAlarmPheromone() { return showAlarmPheromone; }
    public boolean isShowRecruitmentPheromone() { return showRecruitmentPheromone; }
    public boolean isShowQueenPheromone() { return showQueenPheromone; }
    public boolean isShowDeathPheromone() { return showDeathPheromone; }
    public boolean isShowElevationIsolines() { return showElevationIsolinesCheck != null && showElevationIsolinesCheck.isSelected(); }
    public boolean isShowClimateIsolines() { return showClimateIsolinesCheck != null && showClimateIsolinesCheck.isSelected(); }
    public boolean isShowPheromoneIsolines() { return showPheromoneIsolinesCheck != null && showPheromoneIsolinesCheck.isSelected(); }
    public double getLatitude() {
        try { return latField != null ? Double.parseDouble(latField.getText().trim()) : 48.4047; } catch (Exception e) { return 48.4047; }
    }
    public double getLongitude() {
        try { return lonField != null ? Double.parseDouble(lonField.getText().trim()) : 2.7016; } catch (Exception e) { return 2.7016; }
    }
    public double getElevation() {
        try { return altField != null ? Double.parseDouble(altField.getText().trim()) : 35.0; } catch (Exception e) { return 35.0; }
    }
    public TrackedIndividualPane getTrackedIndividualPane() { return trackedAntPane; }
    public String getCityName() { return cityNameField != null ? cityNameField.getText().trim() : "Fontainebleau, FR"; }
    public boolean isFollowAntCameraEnabled() { return isFollowAntCameraEnabled; }
    public CameraFollowMode getCameraFollowMode() { return cameraFollowMode; }
    public Individual getFollowedIndividual() { return followedAnt; }
    public Simulation getActiveSimulation() { return activeSimulation; }
}
