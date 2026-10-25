/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.swarmforge.client.ui.world.*;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.ThemeManager;
import org.swarmforge.client.view.GameViewPane;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.simulation.TunnelNetwork.TunnelNode;

import java.util.*;
import java.util.function.Consumer;

/**
 * World Editor Pane for SwarmForge Studio.
 * Synchronized 3D, Top-Down, and Side views, procedural terrain, soil substrates,
 * micro-hydrology, species adaptation, and simulation overlays.
 */
public class WorldEditorPane extends BorderPane {

    public static final int GRID_SIZE = 128;
    public static final int SOIL_DEPTH = 32;
    public static final int MAX_TRAIL_LENGTH = 120;

    public enum RenderMode { REALISTIC, SCIENTIFIC, GAMIFIED }
    public enum PheromoneRenderMode { HEATMAP_GRADIENT, VOXEL_PARTICLES, HYBRID_GIS }
    public enum BiomePaletteMode { STANDARD, SATELLITE_GIS, TOPOGRAPHIC_CONTOUR }

    public static class SurfaceFloraItem {
        public int gx, gy;
        public int type;
        public double scale;
        public double rotation;
        public boolean isCharred = false;

        public SurfaceFloraItem(int gx, int gy, int type, double scale, double rotation) {
            this.gx = gx; this.gy = gy; this.type = type; this.scale = scale; this.rotation = rotation;
        }
    }

    public static class BotanicalTreeData {
        public int gx, gy;
        public int speciesIdx;
        public double ageScale;
    }

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
    List<SurfaceFloraItem> surfaceFloraItems = new ArrayList<>();

    RenderMode currentRenderMode = RenderMode.REALISTIC;
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

    PheromoneRenderMode pheromoneRenderMode = PheromoneRenderMode.HEATMAP_GRADIENT;
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

    public WorldEditorPane() {
        WorldEditorGeneratorHelper.initDefaultTerrainGrid(this);
        setTop(WorldEditorViewLayoutBuilder.buildHeader(this));
        setLeft(WorldEditorConfigCardsBuilder.buildConfig(this));
        setCenter(WorldEditorViewLayoutBuilder.buildViews(this));
        if (presetsCombo != null && presetsCombo.getValue() != null && presetManager.contains(presetsCombo.getValue())) {
            loadConfiguration(presetManager.get(presetsCombo.getValue()));
        } else {
            repaintAllViews();
        }
        ThemeManager.getInstance().currentThemeProperty().addListener((obs, oldTheme, newTheme) -> repaintAllViews());
    }

    public void bindGameViewListeners() {
        WorldEditorInteractionAndInspectorHelper.bindGameViewListeners(this);
    }

    public void setActive(boolean active) {
        this.isActive = active;
        if (active) {
            if (currentRenderMode != RenderMode.SCIENTIFIC) attachGameView();
            repaintAllViews();
        }
    }

    public void setSimulation(Simulation sim) {
        this.activeSimulation = sim;
        if (gameView != null && gameView.getGameApp() != null) {
            gameView.getGameApp().setSimulation(sim);
            if (sim != null && sim.getTerrarium() != null) {
                gameView.getGameApp().renderTerrarium(sim.getTerrarium());
            }
        }
        repaintAllViews();
    }

    public Simulation getActiveSimulation() { return activeSimulation; }

    public void setOnIndividualSelected(Consumer<Individual> listener) {
        this.onIndividualSelectedListener = listener;
    }

    public void setFollowedIndividual(Individual individual) { WorldEditorCameraAndTrackingHelper.setFollowedIndividual(this, individual); }
    public void selectNextNearestIndividual() { WorldEditorCameraAndTrackingHelper.selectNextNearestIndividual(this); }
    public void selectPreviousNearestIndividual() { WorldEditorCameraAndTrackingHelper.selectPreviousNearestIndividual(this); }
    public void resetIndividualTracking() { WorldEditorCameraAndTrackingHelper.resetIndividualTracking(this); }
    public void setFollowedIndividualById(String query) { WorldEditorCameraAndTrackingHelper.setFollowedIndividualById(this, query); }
    public Individual getFollowedIndividual() { return followedAnt; }

    public void focusOnLocation(float x, float y, float z) {
        if (gameView != null && gameView.getGameApp() != null) gameView.getGameApp().focusOnPosition(x, y, z);
        repaintAllViews();
    }

    public void focusOnLocation(float x, float y) {
        float z = 0f;
        if (activeSimulation != null && activeSimulation.getTerrarium() != null) {
            z = activeSimulation.getTerrarium().getSurfaceElevation(x, y);
        }
        focusOnLocation(x, y, z);
    }

    public void setFollowAntCameraEnabled(boolean enabled) {
        this.isFollowAntCameraEnabled = enabled;
        if (!enabled) this.cameraFollowMode = CameraFollowMode.FREE;
        else if (this.cameraFollowMode == CameraFollowMode.FREE) this.cameraFollowMode = CameraFollowMode.TPS;
        repaintAllViews();
    }

    public boolean isFollowAntCameraEnabled() { return isFollowAntCameraEnabled; }
    public CameraFollowMode getCameraFollowMode() { return cameraFollowMode; }

    public void setCameraFollowMode(CameraFollowMode mode) {
        this.cameraFollowMode = mode != null ? mode : CameraFollowMode.FREE;
        this.isFollowAntCameraEnabled = (this.cameraFollowMode != CameraFollowMode.FREE);
        repaintAllViews();
    }

    public void centerCameraOnSelection() {
        WorldEditorCameraAndTrackingHelper.centerCameraOnSelection(this);
    }

    public void setSimulationMode(boolean simMode) {
        this.isSimulationMode = simMode;
        setHideHeaderPresets(simMode);
        setHideConfigPanel(simMode);
        setHideRightRenderOptions(simMode);
        setSlicePlane(100.0);
        if (showAntTrackingCheck != null) {
            showAntTrackingCheck.setVisible(simMode);
            showAntTrackingCheck.setManaged(simMode);
        }
        if (showChamberOverlayCheck != null) {
            showChamberOverlayCheck.setVisible(simMode);
            showChamberOverlayCheck.setManaged(simMode);
        }
        boolean show = (showLegendCheckBox == null || showLegendCheckBox.isSelected());
        if (nestLegendBox != null) { nestLegendBox.setVisible(simMode && show); nestLegendBox.setManaged(simMode && show); }
        if (pheroLegendBox != null) { pheroLegendBox.setVisible(simMode && show); pheroLegendBox.setManaged(simMode && show); }
        if (castesLegendBox != null) { castesLegendBox.setVisible(simMode && show); castesLegendBox.setManaged(simMode && show); }
        if (weatherOverlayPane != null) { weatherOverlayPane.setVisible(simMode && isWeatherOverlayVisible); weatherOverlayPane.setManaged(simMode && isWeatherOverlayVisible); }
        if (gameView != null && gameView.getGameApp() != null) gameView.getGameApp().setSimulationPaused(!simMode);
        updateRightScrollVisibility();
        repaintAllViews();
    }

    public void setRenderMode(RenderMode mode) {
        this.currentRenderMode = mode != null ? mode : RenderMode.REALISTIC;
        boolean isSci = (currentRenderMode == RenderMode.SCIENTIFIC);
        if (canvas3D != null) {
            canvas3D.setVisible(isSci);
            canvas3D.setManaged(isSci);
            if (isSci) canvas3D.toFront();
        }
        if (!isSci) attachGameView();
        else if (gameView != null) {
            gameView.setVisible(false);
            gameView.setManaged(false);
        }
        if (floatingMouseInspectorCard != null) floatingMouseInspectorCard.toFront();
        if (sideMinimapsBox != null) sideMinimapsBox.toFront();
        if (trackedAntPane != null) trackedAntPane.toFront();
        if (chamberInfoPane != null) chamberInfoPane.toFront();
        if (weatherOverlayPane != null) weatherOverlayPane.toFront();
        if (isSci) this.isActive = true;
        repaintAllViews();
    }

    public void attachGameView() {
        if (gameView == null) gameView = GameViewPane.getSharedInstance(1024, 768);
        if (h3d != null && gameView != null) {
            boolean reparented = false;
            if (gameView.getParent() != this.h3d) {
                if (gameView.getParent() instanceof Pane oldParent) oldParent.getChildren().remove(gameView);
                int idx = h3d.getChildren().indexOf(canvas3D);
                h3d.getChildren().add(Math.max(0, idx + 1), gameView);
                reparented = true;
            }
            boolean isSci = (currentRenderMode == RenderMode.SCIENTIFIC);
            gameView.setVisible(!isSci);
            gameView.setManaged(!isSci);
            if (reparented) {
                bindGameViewListeners();
                if (gameView.getGameApp() != null) {
                    gameView.getGameApp().setSimulationPaused(!isSimulationMode);
                    if (activeSimulation != null) {
                        gameView.getGameApp().setSimulation(activeSimulation);
                        if (activeSimulation.getTerrarium() != null) {
                            gameView.getGameApp().renderTerrarium(activeSimulation.getTerrarium());
                        }
                    }
                }
            }
            if (!isSci) gameView.setRenderMode(currentRenderMode);
        }
    }

    public void setTerrainVisible(boolean visible) {
        this.isTerrainVisible = visible;
        if (gameView != null && gameView.getGameApp() != null) gameView.getGameApp().setTerrainVisible(visible);
        repaintAllViews();
    }

    public void setGalleriesVisible(boolean visible) {
        this.isGalleriesVisible = visible;
        if (showGalleriesCheck != null && showGalleriesCheck.isSelected() != visible) showGalleriesCheck.setSelected(visible);
        if (gameView != null && gameView.getGameApp() != null) gameView.getGameApp().setTunnelsVisible(visible);
        repaintAllViews();
    }

    public void setPheromonesVisible(boolean visible) {
        this.isPheromonesVisible = visible;
        if (gameView != null && gameView.getGameApp() != null) gameView.getGameApp().setPheromonesVisible(visible);
        repaintAllViews();
    }

    public void setColonyVisible(boolean visible) {
        this.isColonyVisible = visible;
        if (gameView != null && gameView.getGameApp() != null) gameView.getGameApp().setAntsVisible(visible);
        repaintAllViews();
    }

    public void setWeatherVisible(boolean visible) {
        this.isWeatherVisible = visible;
        if (gameView != null && gameView.getGameApp() != null) gameView.getGameApp().setWeatherVisible(visible);
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

    public void setHeaderVisible(boolean visible) {
        if (headerBox != null) {
            headerBox.setVisible(visible);
            headerBox.setManaged(visible);
        }
    }

    public void setRightLegendVisible(boolean visible) {
        if (rightScroll != null) {
            if (!visible) {
                rightScroll.setVisible(false);
                rightScroll.setManaged(false);
            } else {
                updateRightScrollVisibility();
            }
        }
        repaintAllViews();
    }

    public void setSyncViews(boolean sync) {
        if (this.syncViewsCheckBox != null) this.syncViewsCheckBox.setSelected(sync);
        repaintAllViews();
    }

    public void setShowLegend(boolean show) {
        if (this.showLegendCheckBox != null) this.showLegendCheckBox.setSelected(show);
        if (this.legendPanel != null) { this.legendPanel.setVisible(show); this.legendPanel.setManaged(show); }
        if (this.legendContentBox != null) { this.legendContentBox.setVisible(show); this.legendContentBox.setManaged(show); }
        if (this.nestLegendBox != null) { this.nestLegendBox.setVisible(show && isSimulationMode); this.nestLegendBox.setManaged(show && isSimulationMode); }
        if (this.pheroLegendBox != null) { this.pheroLegendBox.setVisible(show && isSimulationMode); this.pheroLegendBox.setManaged(show && isSimulationMode); }
        if (this.castesLegendBox != null) { this.castesLegendBox.setVisible(show && isSimulationMode); this.castesLegendBox.setManaged(show && isSimulationMode); }
        if (this.legendSeparator != null) {
            boolean showSep = show && (rightRenderOptions != null && rightRenderOptions.isVisible());
            this.legendSeparator.setVisible(showSep);
            this.legendSeparator.setManaged(showSep);
        }
        updateRightScrollVisibility();
    }

    public void setFullscreenMode(boolean fs) {
        setDualMinimapVisible(!fs);
        setHeaderVisible(!fs);
        if (fs) {
            this.previousZoom = this.zoom;
            this.zoom = Math.min(80.0, this.zoom * 1.25);
            if (isSync()) { this.sideZoom = Math.max(0.3, Math.min(20.0, zoom / 7.5)); this.topZoom = sideZoom; }
            setHideHeaderPresets(true);
            setHideConfigPanel(true);
            setHideRightRenderOptions(true);
            setRightLegendVisible(false);
        } else {
            this.zoom = this.previousZoom;
            if (isSync()) { this.sideZoom = Math.max(0.3, Math.min(6.0, zoom / 7.5)); this.topZoom = sideZoom; }
            if (!isSimulationMode) {
                setHideHeaderPresets(false);
                setHideConfigPanel(false);
                setHideRightRenderOptions(false);
            }
            updateRightScrollVisibility();
        }
        repaintAllViews();
    }

    public void setHideHeaderPresets(boolean hide) {
        if (presetRow != null) {
            presetRow.setVisible(!hide);
            presetRow.setManaged(!hide);
        }
    }

    public void setHideConfigPanel(boolean hide) {
        if (hide) setLeft(null);
        else {
            if (configScrollPane == null) configScrollPane = WorldEditorConfigCardsBuilder.buildConfig(this);
            setLeft(configScrollPane);
        }
    }

    public void setHideRightRenderOptions(boolean hide) {
        if (rightRenderOptions != null) {
            rightRenderOptions.setVisible(!hide);
            rightRenderOptions.setManaged(!hide);
        }
        if (legendSeparator != null) {
            boolean showSep = !hide && (legendPanel != null && legendPanel.isVisible());
            legendSeparator.setVisible(showSep);
            legendSeparator.setManaged(showSep);
        }
        updateRightScrollVisibility();
    }

    public void updateRightScrollVisibility() {
        if (rightScroll != null) {
            boolean hasLegend = (legendPanel != null && legendPanel.isVisible() && (showLegendCheckBox == null || showLegendCheckBox.isSelected()));
            boolean hasOptions = (rightRenderOptions != null && rightRenderOptions.isVisible());
            boolean hasVisibleContent = hasLegend || hasOptions;
            rightScroll.setVisible(hasVisibleContent);
            rightScroll.setManaged(hasVisibleContent);
        }
    }

    public void setSlicePlane(double sliceValue) {
        double clamped = Math.max(0.0, Math.min(100.0, sliceValue));
        if (slicePlaneSlider != null && Math.abs(slicePlaneSlider.getValue() - clamped) > 0.01) {
            slicePlaneSlider.setValue(clamped);
        }
        if (gameView != null && gameView.getGameApp() != null) {
            gameView.getGameApp().setSlicePlaneRatio((float) (clamped / 100.0));
        }
        if (slicePlaneChangeListener != null) slicePlaneChangeListener.accept(clamped);
        repaintAllViews();
    }

    public void setOnSlicePlaneChanged(Consumer<Double> listener) { this.slicePlaneChangeListener = listener; }

    public void setChamberOverlayVisible(boolean visible) {
        if (chamberInfoPane != null) chamberInfoPane.setVisible(visible);
        if (showChamberOverlayCheck != null) showChamberOverlayCheck.setSelected(visible);
        repaintAllViews();
    }

    public boolean isChamberOverlayVisible() { return chamberInfoPane != null && chamberInfoPane.isVisible(); }

    public void selectChamberById(String chamberId) {
        WorldEditorInteractionAndInspectorHelper.selectChamberById(this, chamberId);
    }

    public void updateFloatingMouseInspector(int gx, int gy, int gd, double screenX, double screenY, String bioOverride) {
        WorldEditorInteractionAndInspectorHelper.updateFloatingMouseInspector(this, gx, gy, gd, screenX, screenY, bioOverride);
    }

    public void updateTrackedAntTelemetry() {
        if (trackedAntPane != null && followedAnt != null) {
            trackedAntPane.updateAnt(followedAnt, isFollowAntCameraEnabled);
        }
    }

    public void updateWeatherOverlay() {
        WorldEditorRightSidebarAndLegendBuilder.updateWeatherOverlay(this);
    }

    public void repaintAllViews() {
        if (!isActive) return;
        WorldEditor3DTechnicalRenderer.draw3DTechnical(this);
        minimapRenderer.drawSide(this);
        minimapRenderer.drawTop(this);
    }

    public void resetAllCameras() {
        azimuth = 45; elevation = 35; zoom = 7.5; pan3DX = 0; pan3DY = 0;
        sideZoom = 1.0; sidePanX = 0; sidePanY = 0;
        topZoom = 1.0; topPanX = 0; topPanY = 0;
        repaintAllViews();
    }

    public boolean isSync() {
        return syncViewsCheckBox != null && syncViewsCheckBox.isSelected();
    }

    public double[] project3DPoint(double x, double y, double z, double cx, double cy, double scale, double radAz, double radEl) {
        return WorldEditor3DTechnicalRenderer.project3DPoint(x, y, z, cx, cy, scale, radAz, radEl);
    }

    public boolean isPointOccluded(double gx, double gy, double gz, double radAz, double radEl) {
        return WorldEditor3DTechnicalRenderer.isPointOccluded(this, gx, gy, gz, radAz, radEl);
    }

    public void drawGamifiedVoxelCube3D(double wx, double wy, double wz, double sizeX, double sizeY, double sizeZ, Color topCol, Color sideCol, double cx, double cy, double scale, double radAz, double radEl) {
        WorldEditor3DTechnicalRenderer.drawGamifiedVoxelCube3D(this, wx, wy, wz, sizeX, sizeY, sizeZ, topCol, sideCol, cx, cy, scale, radAz, radEl);
    }

    public void draw3DHoverOverlay(double w, double h, double cx, double cy, double scale, double radAz, double radEl) {
        WorldEditor3DTechnicalRenderer.draw3DHoverOverlay(this, w, h, cx, cy, scale, radAz, radEl);
    }

    public void drawMetricScaleBar3D(double w, double h, double cx, double cy, double scale, double radAz, double radEl) {
        WorldEditor3DTechnicalRenderer.drawMetricScaleBar3D(this, w, h, cx, cy, scale, radAz, radEl);
    }

    public int pickBotanicalTreeSpecies(Random rand) {
        return WorldEditorGeneratorHelper.pickBotanicalTreeSpecies(this, rand);
    }

    public void syncTreeSpeciesTo3DView() {
        WorldEditorGeneratorHelper.syncTreeSpeciesTo3DView(this);
    }

    public List<BotanicalTreeData> getBotanicalTreeInstances() {
        return WorldEditorGeneratorHelper.getBotanicalTreeInstances(this);
    }

    public boolean isForestArea(int gx, int gy) {
        return (gx * 17 + gy * 31) % 11 == 0;
    }

    public boolean isMaterialVisible(byte mat) {
        if (mat == 0 && showEarthCheck != null && !showEarthCheck.isSelected()) return false;
        if (mat == 1 && showSandCheck != null && !showSandCheck.isSelected()) return false;
        if (mat == 2 && showClayCheck != null && !showClayCheck.isSelected()) return false;
        if (mat == 3 && showStoneCheck != null && !showStoneCheck.isSelected()) return false;
        if (mat == 4 && showGravelCheck != null && !showGravelCheck.isSelected()) return false;
        if (mat == 5 && showOrganicCheck != null && !showOrganicCheck.isSelected()) return false;
        return true;
    }

    public double valueNoise3D(double x, double y, double z) {
        return TerrainProceduralGenerator.valueNoise3D(x, y, z);
    }

    public void regenerateAndRepaint() { WorldEditorGeneratorHelper.regenerateAndRepaint(this); }
    public void handleAdaptWorldToSpecies() { WorldEditorGeoAndBioclimateHelper.handleAdaptWorldToSpecies(this); }
    public void applyBioclimaticAdaptation(double lat, double lon, double alt) { WorldEditorGeoAndBioclimateHelper.applyBioclimaticAdaptation(this, lat, lon, alt); }
    public void applyBioclimaticSoilAdaptation(double lat, double lon, double alt) { WorldEditorGeoAndBioclimateHelper.applyBioclimaticAdaptation(this, lat, lon, alt); }
    public boolean promptUnsavedChanges() { return WorldEditorPresetIoHelper.promptUnsavedChanges(this); }
    public void handleSavePreset() { WorldEditorPresetIoHelper.handleSavePreset(this); }
    public void handleDeletePreset() { WorldEditorPresetIoHelper.handleDeletePreset(this); }
    public Map<String, Object> getConfiguration() { return WorldEditorPresetIoHelper.getConfiguration(this); }
    public void loadConfiguration(Map<String, Object> cfg) { WorldEditorPresetIoHelper.loadConfiguration(this, cfg); }
    public void doExport() { WorldEditorPresetIoHelper.doExport(this); }
    public void doImport() { WorldEditorPresetIoHelper.doImport(this); }
    public void triggerWildfire(int cx, int cy, int radius) { WorldEditorDisasterAndExportHelper.triggerWildfire(this, cx, cy, radius); }
    public void triggerFlood(int cx, int cy, int radius) { WorldEditorDisasterAndExportHelper.triggerFlood(this, cx, cy, radius); }
    public void triggerDrought() { WorldEditorDisasterAndExportHelper.triggerDrought(this); }
    public Terrarium exportToTerrarium() { return WorldEditorDisasterAndExportHelper.exportToTerrarium(this); }

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

    Slider mkSlider(double min, double max, double val) {
        Slider s = new Slider(min, max, val);
        s.setShowTickLabels(true); s.setShowTickMarks(true); s.setPrefWidth(180);
        return s;
    }

    Spinner<Integer> mkSpinner(int min, int max, int val) {
        Spinner<Integer> sp = new Spinner<>(min, max, val);
        sp.setPrefWidth(75); sp.setEditable(true);
        sp.valueProperty().addListener((o, a, b) -> repaintAllViews());
        return sp;
    }

    void addLsn(Slider... sliders) {
        for (Slider s : sliders) if (s != null) s.valueProperty().addListener((o, a, b) -> repaintAllViews());
    }

    void addFloraLsn(Slider... sliders) {
        for (Slider s : sliders) if (s != null) s.valueProperty().addListener((o, a, b) -> { WorldEditorGeneratorHelper.generateSurfaceFlora(this); repaintAllViews(); });
    }

    void addFloraBoolLsn(CheckBox... boxes) {
        for (CheckBox b : boxes) if (b != null) b.selectedProperty().addListener((o, a, n) -> { WorldEditorGeneratorHelper.generateSurfaceFlora(this); repaintAllViews(); });
    }

    void addBoolLsn(CheckBox... boxes) {
        for (CheckBox b : boxes) if (b != null) b.selectedProperty().addListener((o, a, n) -> repaintAllViews());
    }

    HBox sv(Slider s) { return sv(s, ""); }

    HBox sv(Slider s, String unit) {
        HBox b = new HBox(8); b.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label v = new Label(fmt(s != null ? s.getValue() : 0.0) + (unit == null || unit.isEmpty() ? "" : " " + unit));
        v.setStyle("-fx-text-fill:#00d4ff;-fx-min-width:36;-fx-font-weight:bold;");
        if (s != null) s.valueProperty().addListener((o, a, n) -> v.setText(fmt(n.doubleValue()) + (unit == null || unit.isEmpty() ? "" : " " + unit)));
        b.getChildren().addAll(s, v);
        return b;
    }

    HBox svPct(Slider s) {
        HBox b = new HBox(8); b.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label v = new Label(String.format(Locale.US, "%.0f%%", (s != null ? s.getValue() : 0.0) * 100.0));
        v.setStyle("-fx-text-fill:#00d4ff;-fx-min-width:40;-fx-font-weight:bold;");
        if (s != null) s.valueProperty().addListener((o, a, n) -> v.setText(String.format(Locale.US, "%.0f%%", n.doubleValue() * 100.0)));
        b.getChildren().addAll(s, v);
        return b;
    }

    private String fmt(double d) { return d == (long) d ? String.format("%d", (long) d) : String.format("%.1f", d); }

    // Getters and helper bindings
    public ResizableCanvas getCanvas3D() { return canvas3D; }
    public Canvas getCanvasSide() { return canvasSide; }
    public GraphicsContext getGcSide() { return gcSide; }
    public Canvas getCanvasTop() { return canvasTop; }
    public GraphicsContext getGcTop() { return gcTop; }
    public GameViewPane getGameView() { return gameView; }
    public RenderMode getRenderMode() { return currentRenderMode; }
    public PheromoneRenderMode getPheromoneRenderMode() { return pheromoneRenderMode; }
    public void setPheromoneRenderMode(PheromoneRenderMode mode) {
        this.pheromoneRenderMode = mode != null ? mode : PheromoneRenderMode.HEATMAP_GRADIENT;
        repaintAllViews();
    }
    public double getZoom() { return zoom; }
    public void setZoom(double z) {
        this.zoom = Math.max(2.5, Math.min(22.0, z));
        if (isSync()) {
            this.sideZoom = Math.max(0.3, Math.min(6.0, zoom / 7.5));
            this.topZoom = sideZoom;
        }
        repaintAllViews();
    }
    public boolean isDirty() { return isDirty; }
    public boolean isUVVisionMode() { return isUVVisionMode; }
    public void setUVVisionMode(boolean enabled) {
        this.isUVVisionMode = enabled;
        if (showUVVisionModeCheck != null) showUVVisionModeCheck.setSelected(enabled);
        if (gameView != null && gameView.getGameApp() != null) gameView.getGameApp().setUVVisionMode(enabled);
        repaintAllViews();
    }
    public boolean isOmmatidialVisionMode() { return isOmmatidialVisionMode; }
    public void setOmmatidialVisionMode(boolean enabled) {
        this.isOmmatidialVisionMode = enabled;
        if (showOmmatidialVisionModeCheck != null) showOmmatidialVisionModeCheck.setSelected(enabled);
        if (gameView != null && gameView.getGameApp() != null) gameView.getGameApp().setOmmatidialVisionMode(enabled);
        repaintAllViews();
    }
    public boolean isVoxelInfoVisible() { return isVoxelInfoVisible; }
    public void setVoxelInfoVisible(boolean visible) {
        this.isVoxelInfoVisible = visible;
        if (showVoxelInfoCheck != null) showVoxelInfoCheck.setSelected(visible);
        if (floatingMouseInspectorCard != null && !visible) floatingMouseInspectorCard.setVisible(false);
        if (!visible && lblHoverInfo != null) lblHoverInfo.setText("");
        repaintAllViews();
    }
    public boolean isAntTrackingEnabled() { return isAntTrackingEnabled; }
    public void setAntTrackingEnabled(boolean enabled) {
        this.isAntTrackingEnabled = enabled;
        if (showAntTrackingCheck != null && showAntTrackingCheck.isSelected() != enabled) showAntTrackingCheck.setSelected(enabled);
        if (!enabled) {
            setFollowedIndividual(null);
            if (trackedAntPane != null) {
                trackedAntPane.clearSearchStatus();
                trackedAntPane.setVisible(false);
            }
        }
        repaintAllViews();
    }
    public double getAntVisualScaleMultiplier() { return antVisualScaleMultiplier; }
    public void setAntVisualScaleMultiplier(double multiplier) {
        this.antVisualScaleMultiplier = multiplier;
        repaintAllViews();
    }
    public boolean isShowDebugRuler() { return showDebugRuler; }
    public void setShowDebugRuler(boolean show) {
        this.showDebugRuler = show;
        repaintAllViews();
    }
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
    public List<SurfaceFloraItem> getSurfaceFloraItems() { return surfaceFloraItems; }
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
    public void setShowElevationIsolines(boolean show) {
        if (showElevationIsolinesCheck != null) showElevationIsolinesCheck.setSelected(show);
        if (gameView != null && gameView.getGameApp() != null) gameView.getGameApp().setShowElevationIsolines(show);
        repaintAllViews();
    }
    public void setShowClimateIsolines(boolean show) {
        if (showClimateIsolinesCheck != null) showClimateIsolinesCheck.setSelected(show);
        if (gameView != null && gameView.getGameApp() != null) gameView.getGameApp().setShowClimateIsolines(show);
        repaintAllViews();
    }
    public void setShowPheromoneIsolines(boolean show) {
        if (showPheromoneIsolinesCheck != null) showPheromoneIsolinesCheck.setSelected(show);
        if (gameView != null && gameView.getGameApp() != null) gameView.getGameApp().setShowPheromoneIsolines(show);
        repaintAllViews();
    }
    public boolean isShowElevationIsolines() { return showElevationIsolinesCheck != null && showElevationIsolinesCheck.isSelected(); }
    public boolean isShowClimateIsolines() { return showClimateIsolinesCheck != null && showClimateIsolinesCheck.isSelected(); }
    public boolean isShowPheromoneIsolines() { return showPheromoneIsolinesCheck != null && showPheromoneIsolinesCheck.isSelected(); }
    public void updateWeatherSimulationParameters(double windSpeed, String windDirection, String season) {
        this.simWindSpeed = windSpeed;
        if (windDirection != null) this.simWindDirection = windDirection;
        if (season != null) this.simSeason = season;
        repaintAllViews();
    }
    public void setShowTrees(boolean visible) {
        if (showVegetationCheck != null) showVegetationCheck.setSelected(visible);
        if (gameView != null && gameView.getGameApp() != null) gameView.getGameApp().setVegetationVisible(visible);
        repaintAllViews();
    }
    public void setShow3DSkirt(boolean visible) {
        if (showSubstrateStratigraphyCheck != null) showSubstrateStratigraphyCheck.setSelected(visible);
        if (showChamferedBezelCheck != null && showChamferedBezelCheck.isSelected() != visible) showChamferedBezelCheck.setSelected(visible);
        if (gameView != null && gameView.getGameApp() != null) gameView.getGameApp().setShowSkirt(visible);
        repaintAllViews();
    }
    public void setVolumetricScannerEnabled(boolean enabled) { repaintAllViews(); }
    public void setOnGenerate(Consumer<Map<String, Object>> cb) { this.onGenerateCallback = cb; }
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
    public void setPheromoneChannelFilter(int channel) { repaintAllViews(); }
    public void setWeatherOverlayVisible(boolean visible) { this.isWeatherOverlayVisible = visible; updateWeatherOverlay(); }
    public String getCityName() { return cityNameField != null ? cityNameField.getText().trim() : "Fontainebleau, FR"; }
    public boolean isSubterraneanExposed() {
        boolean isTranslucent = showTranslucentVolumetricModeCheck != null && showTranslucentVolumetricModeCheck.isSelected();
        double cutRatio = slicePlaneSlider != null ? (slicePlaneSlider.getValue() / 100.0) : 1.0;
        return !isTerrainVisible || cutRatio < 0.85 || isTranslucent;
    }
}
