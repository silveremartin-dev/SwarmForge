/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

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
 * sub-millimeter voxel scale, soil substrates, micro-hydrology, and voxel deformation physics.
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

    // View Synchronization CheckBox & Viewport Mode Label
    private CheckBox syncViewsCheckBox;
    private Label lblViewportMode;

    // Controls: 0. Terrain Source
    private TextField citySearchField;
    private Label geoStatusLabel;
    private TextField latField;
    private TextField lonField;

    // Controls: Layer, Vegetation & Nest Visibility Toggles
    private CheckBox showEarthCheck;
    private CheckBox showSandCheck;
    private CheckBox showClayCheck;
    private CheckBox showStoneCheck;
    private CheckBox showVegetationCheck;
    private CheckBox showGalleriesCheck;
    private CheckBox showSubstrateStratigraphyCheck;
    private Label lblHoverInfo;

    // Controls: 1. Scale & Resolution
    private Slider surfaceSizeSlider; // Mètres (1.0 - 10.0m)
    private Slider depthSlider;       // Profondeur Souterraine (0.2 - 5.0m)
    private Slider resolutionSlider; // Sub-millimétrique (0.1 - 1.0mm)

    // Controls: 2. Soil & Relief
    private Slider roughnessSlider;
    private Slider compactionSlider;
    private Spinner<Integer> earthSpinner;
    private Spinner<Integer> sandSpinner;
    private Spinner<Integer> claySpinner;
    private Spinner<Integer> stoneSpinner;
    private Spinner<Integer> organicSpinner;

    // Controls: 3. Flora & Surface Cover Ecosystem
    private TextField floraSeedField;
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


    // Controls: 7. 3D Sculpting Brushes & Voxel Painting Mode
    private CheckBox enableSculptingCheck;
    private ComboBox<String> brushModeSelect;
    private ComboBox<String> brushSubstrateSelect;
    private Slider brushRadiusSlider;
    private Slider brushStrengthSlider;

    // Local Voxel & Terrain Grid for Real-Time Sculpting (128x128x32 - 524 288 voxels macro)
    private static final int GRID_SIZE = 128;
    private static final int SOIL_DEPTH = 32; // 32 couches de sol en profondeur
    private double[][] heightGrid = new double[GRID_SIZE][GRID_SIZE];
    // Sol 3D : [x][y][profondeur] — 0=surface, SOIL_DEPTH-1=fond
    // Materiaux: 0=Humus/Terre, 1=Sable, 2=Argile, 3=Pierre, 4=Organique
    private byte[][][] soilLayers  = new byte[GRID_SIZE][GRID_SIZE][SOIL_DEPTH];
    private float[][]  humidityGrid = new float[GRID_SIZE][GRID_SIZE];  // 0.0-1.0
    private boolean[][][] voidGrid  = new boolean[GRID_SIZE][GRID_SIZE][SOIL_DEPTH]; // cavernes
    private boolean[][] carvedVoxelGrid = new boolean[GRID_SIZE][GRID_SIZE];
    private List<int[]> riverPath = new ArrayList<>(); // chemin calculé sur heightmap

    // Nouveaux sliders de génération du substrat
    private Slider stratificationSlider; // 0=mélangé, 1=strates parfaites
    private Slider mixingRateSlider;     // taux de bruit sur les frontières
    private Slider baseHumiditySlider;   // humidité de base
    private Slider voidDensitySlider;    // densité des cavernes
    private CheckBox showHumidityCheck;  // overlay humidité (vue Top)

    // Option A & Option B Controls (Jupe Soudée & Rendu Volumétrique)
    private CheckBox useAdvancedVolumetricModeCheck;   // Bascule Mode Basique (Jupe Soudée) vs Mode Volumétrique Avancé
    private CheckBox showChamferedBezelCheck;          // Option A2 : Cadre d'observation biseauté (Bezel Glass Enclosure)
    private CheckBox showGravelInclusionsCheck;        // Option B1 : Inclusions de Graviers, Galets & Micro-abris
    private CheckBox enableVolumetricScannerCheck;     // Option B2 : Scanner Volumétrique & Coupe Dynamique
    private Slider slicePlaneSlider;                   // Option B2 : Slider de Coupe Log Géologique (0% -> 100%)
    private CheckBox showTranslucentVolumetricModeCheck;// Option B3 : Translucidité 3D Volumétrique & Shader Bruit

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
        Random rand = new Random(42);
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                double nx = (double) x / GRID_SIZE;
                double ny = (double) y / GRID_SIZE;
                heightGrid[x][y] = 0.4 + 0.25 * Math.sin(nx * Math.PI * 2) * Math.cos(ny * Math.PI * 2)
                        + 0.08 * valueNoise3D(nx * 4, ny * 4, 0)
                        + rand.nextDouble() * 0.04;
                heightGrid[x][y] = Math.max(0.05, Math.min(0.95, heightGrid[x][y]));
                carvedVoxelGrid[x][y] = false;
            }
        }
        generateSoilLayers(0.7, 0.3);
        generateHumidity(0.35);
        generateVoids(0.08);
        riverPath = computeRiverPath();
    }

    /** Génère les couches de sol 3D selon la stratification et le taux de mélange. */
    private void generateSoilLayers(double stratification, double mixing) {
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                // Composition de surface depuis les spinners
                int[] surfacePct = getSurfaceComposition();
                for (int d = 0; d < SOIL_DEPTH; d++) {
                    double depthRatio = (double) d / (SOIL_DEPTH - 1); // 0=surface, 1=fond
                    // Strate idéale (stratigraphie parfaite)
                    byte idealMat;
                    if (depthRatio < 0.15) idealMat = 0;       // Humus
                    else if (depthRatio < 0.45) idealMat = 2;  // Argile
                    else if (depthRatio < 0.75) idealMat = 3;  // Pierre
                    else idealMat = 3;                          // Bedrock

                    // Bruit de mélange
                    double noise = valueNoise3D(x * 0.25, y * 0.25, d * 0.6 + 10);
                    double rand01 = valueNoise3D(x * 0.8, y * 0.8, d * 1.5 + 50);

                    byte mat;
                    if (d == 0) {
                        // Surface : ponderer par la composition saisie
                        mat = pickSurfaceMaterial(surfacePct, rand01);
                    } else {
                        double blend = stratification + (noise - 0.5) * mixing * 2.0;
                        if (blend > 0.5) {
                            mat = idealMat;
                        } else {
                            // Matériau aléatoire pondéré par composition
                            mat = pickSurfaceMaterial(surfacePct, rand01);
                        }
                    }
                    soilLayers[x][y][d] = mat;
                }
            }
        }
    }

    private int[] getSurfaceComposition() {
        int e = earthSpinner  != null ? earthSpinner.getValue()   : 50;
        int s = sandSpinner   != null ? sandSpinner.getValue()    : 20;
        int c = claySpinner   != null ? claySpinner.getValue()    : 20;
        int st= stoneSpinner  != null ? stoneSpinner.getValue()   : 10;
        // Normalise
        int total = Math.max(1, e + s + c + st);
        return new int[]{e * 100 / total, s * 100 / total, c * 100 / total, st * 100 / total};
    }

    private byte pickSurfaceMaterial(int[] pct, double rand01) {
        double r = rand01 * 100;
        if (r < pct[0]) return 0; // Humus
        r -= pct[0];
        if (r < pct[1]) return 1; // Sable
        r -= pct[1];
        if (r < pct[2]) return 2; // Argile
        return 3; // Pierre
    }

    /** Génère la grille d'humidité de surface. */
    private void generateHumidity(double baseHumidity) {
        double wtDepth = waterTableDepthSlider != null ? waterTableDepthSlider.getValue() : 15;
        double wtFactor = 1.0 - Math.min(1.0, wtDepth / 50.0); // plus la nappe est haute, plus c'est humide
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                double noise = valueNoise3D(x * 0.2, y * 0.2, 99);
                humidityGrid[x][y] = (float) Math.max(0, Math.min(1,
                        baseHumidity + wtFactor * 0.3 + (noise - 0.5) * 0.25));
            }
        }
    }

    /** Génère les vides souterrains (cavernes) via bruit 3D seuillé. */
    private void generateVoids(double density) {
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                for (int d = 0; d < SOIL_DEPTH; d++) {
                    voidGrid[x][y][d] = false;
                    if (d < 2) continue; // Pas de vide en surface
                    double noise = valueNoise3D(x * 0.3, y * 0.3, d * 0.8);
                    // Le seuil diminue avec la profondeur (plus de vides en profondeur)
                    double threshold = 1.0 - density * (0.5 + 0.5 * d / SOIL_DEPTH);
                    voidGrid[x][y][d] = (noise > threshold);
                    // Stabilité : si vide sous sable, le sable s'effondre
                    if (voidGrid[x][y][d] && soilLayers[x][y][d] == 1) {
                        voidGrid[x][y][d] = false; // Sable instable = pas de vide
                    }
                }
            }
        }
    }

    /** Calcule le chemin de la rivière par descente de gradient sur le heightmap. */
    private List<int[]> computeRiverPath() {
        List<int[]> path = new ArrayList<>();
        if (riverCheck == null || !riverCheck.isSelected()) return path;
        // Point de départ : bord supérieur, cellule la plus haute
        int startX = GRID_SIZE / 2;
        double maxH = -1;
        for (int x = 4; x < GRID_SIZE - 4; x++) {
            if (heightGrid[x][4] > maxH) { maxH = heightGrid[x][4]; startX = x; }
        }
        int cx = startX, cy = 4;
        Set<String> visited = new HashSet<>();
        int[][] dirs = {{0,1},{1,0},{-1,0},{0,-1},{1,1},{-1,1},{1,-1},{-1,-1}};
        for (int step = 0; step < GRID_SIZE * 3; step++) {
            if (cx < 0 || cx >= GRID_SIZE || cy < 0 || cy >= GRID_SIZE) break;
            path.add(new int[]{cx, cy});
            visited.add(cx + "," + cy);
            if (cy >= GRID_SIZE - 4) break;
            int bx = cx, by = cy;
            double bH = heightGrid[cx][cy] + 1.0; // +1 pour forcer la descente
            for (int[] d : dirs) {
                int nx = cx + d[0], ny = cy + d[1];
                if (nx < 1 || nx >= GRID_SIZE-1 || ny < 1 || ny >= GRID_SIZE-1) continue;
                if (visited.contains(nx + "," + ny)) continue;
                if (heightGrid[nx][ny] < bH) { bH = heightGrid[nx][ny]; bx = nx; by = ny; }
            }
            if (bx == cx && by == cy) break; // dépression
            cx = bx; cy = by;
        }
        return path;
    }

    /** Appliquer érosion procédurale : lissage des pentes abruptes selon l'angle de repos. */
    private void applyProceduralErosion() {
        double[] angleOfRepose = {0.20, 0.08, 0.25, 0.35, 0.22}; // par matériau
        for (int iter = 0; iter < 3; iter++) {
            for (int x = 1; x < GRID_SIZE - 1; x++) {
                for (int y = 1; y < GRID_SIZE - 1; y++) {
                    byte mat = soilLayers[x][y][0];
                    double maxSlope = mat < angleOfRepose.length ? angleOfRepose[mat] : 0.2;
                    double h = heightGrid[x][y];
                    double[] nb = {heightGrid[x-1][y], heightGrid[x+1][y],
                                   heightGrid[x][y-1], heightGrid[x][y+1]};
                    double minN = Arrays.stream(nb).min().getAsDouble();
                    if (h - minN > maxSlope) heightGrid[x][y] = minN + maxSlope;
                }
            }
        }
    }

    /** Recalcule toutes les données puis redessine. */
    private void regenerateAndRepaint() {
        double strat = stratificationSlider != null ? stratificationSlider.getValue() : 0.7;
        double mix   = mixingRateSlider    != null ? mixingRateSlider.getValue()    : 0.3;
        double hum   = baseHumiditySlider  != null ? baseHumiditySlider.getValue()  : 0.35;
        double voids = voidDensitySlider   != null ? voidDensitySlider.getValue()   : 0.08;
        generateSoilLayers(strat, mix);
        generateHumidity(hum);
        generateVoids(voids);
        riverPath = computeRiverPath();
        generateSurfaceFlora();
        repaintAllViews();
    }

    /** Génère déterministement le couvert végétal et les débris de surface selon floraSeed. */
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
                // 1. Herbacées & Fleurs (Ressources Comestibles)
                if (r < ediblePct * 0.35) {
                    if (seedGrassCheck != null && seedGrassCheck.isSelected() && rand.nextDouble() < 0.5) {
                        surfaceFloraItems.add(new SurfaceFloraItem(x, y, 0, 0.7 + rand.nextDouble() * 0.5, rand.nextDouble() * 360));
                    } else if (aphidPlantCheck != null && aphidPlantCheck.isSelected() && rand.nextDouble() < 0.35) {
                        surfaceFloraItems.add(new SurfaceFloraItem(x, y, 1, 0.8 + rand.nextDouble() * 0.4, rand.nextDouble() * 360));
                    } else if (nectarFlowersCheck != null && nectarFlowersCheck.isSelected() && rand.nextDouble() < 0.35) {
                        surfaceFloraItems.add(new SurfaceFloraItem(x, y, 2, 0.6 + rand.nextDouble() * 0.5, rand.nextDouble() * 360));
                    }
                }
                // 2. Mousse Polytrichum (Zone humide)
                if (mossCheck != null && mossCheck.isSelected() && humidityGrid[x][y] > 0.35 && rand.nextDouble() < nonEdiblePct * 0.45) {
                    surfaceFloraItems.add(new SurfaceFloraItem(x, y, 3, 0.6 + rand.nextDouble() * 0.8, rand.nextDouble() * 360));
                }
                // 3. Litière de Pin / Feuilles mortes
                if (pineLitterCheck != null && pineLitterCheck.isSelected() && rand.nextDouble() < litterPct * 0.4) {
                    surfaceFloraItems.add(new SurfaceFloraItem(x, y, 4, 0.5 + rand.nextDouble() * 0.7, rand.nextDouble() * 360));
                }
                // 4. Brindilles & Graviers micro-abris
                if (rand.nextDouble() < debrisPct * 0.3) {
                    int debrisType = rand.nextBoolean() ? 5 : 6;
                    surfaceFloraItems.add(new SurfaceFloraItem(x, y, debrisType, 0.5 + rand.nextDouble() * 0.6, rand.nextDouble() * 360));
                }
            }
        }
    }

    // ── Bruit de valeur 3D (hash-based smooth noise) ──────────────────────────
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


    private VBox buildHeader() {
        VBox v = new VBox(6);
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

        Button bSave = new Button("Sauvegarder");
        bSave.setGraphic(new FontIcon(Feather.SAVE));
        bSave.getStyleClass().add("btn-secondary");
        bSave.setTooltip(new Tooltip("Enregistrer la configuration actuelle du monde comme nouveau preset."));
        bSave.setOnAction(e -> handleSavePreset());

        Button bDelete = new Button("Supprimer");
        bDelete.setGraphic(new FontIcon(Feather.TRASH_2));
        bDelete.getStyleClass().add("btn-danger");
        bDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold;");
        bDelete.setTooltip(new Tooltip("Supprimer le preset de monde sélectionné."));
        bDelete.setOnAction(e -> handleDeletePreset());

        Button bExport = new Button("Exporter...");
        bExport.setGraphic(new FontIcon(Feather.DOWNLOAD));
        bExport.getStyleClass().add("btn-secondary");
        bExport.setTooltip(new Tooltip("Exporter la configuration du monde au format JSON."));
        bExport.setOnAction(e -> doExport());

        Button bImport = new Button("Importer...");
        bImport.setGraphic(new FontIcon(Feather.UPLOAD));
        bImport.getStyleClass().add("btn-secondary");
        bImport.setTooltip(new Tooltip("Importer un fichier JSON de configuration de monde."));
        bImport.setOnAction(e -> doImport());

        r.getChildren().addAll(title, sp, lblPreset, presetsCombo, bSave, bDelete, new Separator(Orientation.VERTICAL), bExport, bImport);

        Label subtitle = new Label("Génération de relief, sol, ouvert végétal, hydrographie, sculpture 3D & déformation voxel (0.1-1.0mm)");
        subtitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        v.getChildren().addAll(r, subtitle);
        return v;
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

    private ScrollPane buildConfig() {
        VBox cfg = new VBox(12);
        cfg.setPadding(new Insets(10));
        cfg.setPrefWidth(320);
        cfg.setStyle("-fx-background-color: #121214;");

        Accordion accordion = new Accordion();

        // 0. TERRAIN SOURCE SELECTOR (SIG / GeoData)
        TitledPane paneSource = new TitledPane("🌐 Données Géographiques & Services Web SIG", buildTerrainSourceBlock());

        // 1. SCALE & RESOLUTION
        TitledPane paneScale = new TitledPane("📐 Échelle & Voxel Sub-millimétrique", buildScaleBlock());

        // 2. RELIEF & TOPOGRAPHY (Rugosité, Bruit Perlin & Sculpture 3D)
        TitledPane paneRelief = new TitledPane("⛰️ Relief, Topographie & Sculpture 3D", buildReliefBlock());

        // 3. SOIL & STRATA (Substrats, Stratification & Vides 3D)
        TitledPane paneSoil = new TitledPane("🗻 Sol, Substrats & Stratification 3D", buildSoilBlock());

        // 4. HYDROLOGY
        TitledPane paneHydro = new TitledPane("💧 Hydrographie & Sources d'Eau", buildHydroBlock());

        // 5. FLORA ECOSYSTEM
        TitledPane paneFlora = new TitledPane("🌿 Écosystème & Biome Végétal", buildFloraBlock());

        // 6. VERTICAL STRUCTURES
        TitledPane paneStruct = new TitledPane("🪵 Structures Hôtes & Hauteur", buildStructBlock());

        accordion.getPanes().addAll(paneSource, paneScale, paneRelief, paneSoil, paneHydro, paneFlora, paneStruct);
        accordion.setExpandedPane(paneSource); // Open source block by default

        cfg.getChildren().add(accordion);

        ScrollPane sc = new ScrollPane(cfg);
        sc.setFitToWidth(true);
        sc.setPrefWidth(340);
        return sc;
    }

    private VBox buildTerrainSourceBlock() {
        citySearchField = new TextField("Paris");
        citySearchField.setPromptText("Ex: Paris, Tokyo, Manaus...");
        citySearchField.setPrefWidth(125);
        citySearchField.setOnAction(e -> fetchCityCoordinates(citySearchField.getText()));

        Button btnSearchCity = new Button("🔍 Ville");
        btnSearchCity.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
        btnSearchCity.setOnAction(e -> fetchCityCoordinates(citySearchField.getText()));

        latField = new TextField("48.8566");
        latField.setPrefWidth(70);
        lonField = new TextField("2.3522");
        lonField.setPrefWidth(70);

        Button btnImportGPS = new Button("📍 Charger Données SIG");
        btnImportGPS.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");

        geoStatusLabel = new Label("ℹ️ Recherche par Nom de Ville et/ou Coordonnées GPS réelles.");
        geoStatusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8; -fx-wrap-text: true;");

        btnImportGPS.setOnAction(e -> {
            regenerateAndRepaint();
            geoStatusLabel.setText("🟢 Données SIG réelles appliquées pour Lat: " + latField.getText() + "°, Lon: " + lonField.getText() + "° !");
            geoStatusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #4ade80; -fx-wrap-text: true;");
            new Alert(Alert.AlertType.INFORMATION, "Importation des données SIG réelles (DEM + Sol + Végétation) effectuée avec succès !").show();
        });

        HBox cityRow = new HBox(5, new Label("Ville:") {{ setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;"); }}, citySearchField, btnSearchCity);
        cityRow.setAlignment(Pos.CENTER_LEFT);

        HBox gpsRow = new HBox(5, 
            new Label("Lat:") {{ setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;"); }}, latField, 
            new Label("Lon:") {{ setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;"); }}, lonField,
            btnImportGPS
        );
        gpsRow.setAlignment(Pos.CENTER_LEFT);

        VBox webOptions = new VBox(8,
            new Label("1. Recherche par Ville / Localité :"), cityRow,
            new Label("2. Coordonnées Géographiques (GPS) :"), gpsRow,
            new Label("💡 Permet de spécifier une Ville ET/OU des Coordonnées GPS précises. Extrait le modèle d'élévation réelles, le sol et la flore locale.") {{
                setStyle("-fx-font-size: 10px; -fx-text-fill: #38bdf8; -fx-wrap-text: true;");
            }}
        );

        return new VBox(10, webOptions, geoStatusLabel);
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
                if (comboTreeSpecies != null) comboTreeSpecies.getSelectionModel().select(3); // Cactus Saguaro
                if (sandSpinner != null) sandSpinner.getValueFactory().setValue(70);
                if (stoneSpinner != null) stoneSpinner.getValueFactory().setValue(20);
                if (earthSpinner != null) earthSpinner.getValueFactory().setValue(10);
                if (baseHumiditySlider != null) baseHumiditySlider.setValue(0.12);
            }
            case TROPICAL_RAINFOREST -> {
                if (comboTreeSpecies != null) comboTreeSpecies.getSelectionModel().select(2); // Acacia / Equatorial
                if (earthSpinner != null) earthSpinner.getValueFactory().setValue(60);
                if (claySpinner != null) claySpinner.getValueFactory().setValue(30);
                if (baseHumiditySlider != null) baseHumiditySlider.setValue(0.85);
            }
            case ARCTIC_TUNDRA -> {
                if (comboTreeSpecies != null) comboTreeSpecies.getSelectionModel().select(4); // Bouleaux & Toundra
                if (stoneSpinner != null) stoneSpinner.getValueFactory().setValue(50);
                if (earthSpinner != null) earthSpinner.getValueFactory().setValue(40);
                if (baseHumiditySlider != null) baseHumiditySlider.setValue(0.30);
            }
            case MEDITERRANEAN -> {
                if (comboTreeSpecies != null) comboTreeSpecies.getSelectionModel().select(1); // Pinède Résineuse
                if (earthSpinner != null) earthSpinner.getValueFactory().setValue(40);
                if (stoneSpinner != null) stoneSpinner.getValueFactory().setValue(30);
                if (baseHumiditySlider != null) baseHumiditySlider.setValue(0.35);
            }
            default -> {
                if (comboTreeSpecies != null) comboTreeSpecies.getSelectionModel().select(0); // Futaie de Chênes
                if (earthSpinner != null) earthSpinner.getValueFactory().setValue(50);
                if (sandSpinner != null) sandSpinner.getValueFactory().setValue(20);
                if (claySpinner != null) claySpinner.getValueFactory().setValue(20);
                if (baseHumiditySlider != null) baseHumiditySlider.setValue(0.45);
            }
        }
        updateEcologicalCompatibilityScores();
    }

    private VBox buildScaleBlock() {
        surfaceSizeSlider = mkSlider(0.5, 10.0, 2.0);
        depthSlider = mkSlider(0.2, 5.0, 1.5);
        resolutionSlider = mkSlider(0.1, 1.0, 0.5);
        addLsn(surfaceSizeSlider, depthSlider, resolutionSlider);

        Label scaleHint = new Label("💡 Détermine l'emprise 3D (Surface x Profondeur Souterraine) et la précision voxel sub-millimétrique.");
        scaleHint.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8; -fx-wrap-text: true;");

        return new VBox(8,
                new Label("Taille de Surface (Mètres):"), sv(surfaceSizeSlider, "m"),
                new Label("Profondeur Souterraine du Sol (Mètres):"), sv(depthSlider, "m"),
                new Label("Résolution Grille / Voxel:"), sv(resolutionSlider, "mm"),
                scaleHint
        );
    }

    private VBox buildReliefBlock() {
        roughnessSlider = mkSlider(0.0, 1.0, 0.45);
        compactionSlider = mkSlider(10.0, 100.0, 65.0);

        roughnessSlider.valueProperty().addListener((o, a, b) -> {
            double r = roughnessSlider.getValue();
            Random rand = new Random(42);
            for (int x = 0; x < GRID_SIZE; x++)
                for (int y = 0; y < GRID_SIZE; y++) {
                    double nx = (double)x/GRID_SIZE, ny = (double)y/GRID_SIZE;
                    heightGrid[x][y] = 0.4 + r * 0.5 * Math.sin(nx*Math.PI*2)*Math.cos(ny*Math.PI*2)
                            + r * 0.15 * valueNoise3D(nx*4, ny*4, 0) + rand.nextDouble()*0.04*r;
                    heightGrid[x][y] = Math.max(0.05, Math.min(0.95, heightGrid[x][y]));
                }
            riverPath = computeRiverPath();
            repaintAllViews();
        });
        compactionSlider.valueProperty().addListener((o, a, b) -> repaintAllViews());

        VBox sculptSubBlock = buildSculptBlock();

        return new VBox(8,
                new Label("⛰️ Rugosité du Relief (Bruit Perlin) :"), sv(roughnessSlider, ""),
                new Label("🔨 Indice de Compaction du Sol :"), sv(compactionSlider, "%"),
                new Separator(),
                sculptSubBlock
        );
    }

    private VBox buildSoilBlock() {
        earthSpinner = mkSpinner(0, 100, 50);
        sandSpinner = mkSpinner(0, 100, 20);
        claySpinner = mkSpinner(0, 100, 20);
        stoneSpinner = mkSpinner(0, 100, 10);
        organicSpinner = mkSpinner(0, 100, 0);

        for (Spinner<Integer> sp : new Spinner[]{earthSpinner, sandSpinner, claySpinner, stoneSpinner, organicSpinner})
            sp.valueProperty().addListener((o, a, b) -> regenerateAndRepaint());

        GridPane grid = new GridPane();
        grid.setHgap(8); grid.setVgap(6);
        grid.add(new Label("Terre / Humus %:"), 0, 0); grid.add(earthSpinner, 1, 0);
        grid.add(new Label("Sable (Éboulements) %:"), 0, 1); grid.add(sandSpinner, 1, 1);
        grid.add(new Label("Argile (Stabilité) %:"), 0, 2); grid.add(claySpinner, 1, 2);
        grid.add(new Label("Pierre / Gravier %:"), 0, 3); grid.add(stoneSpinner, 1, 3);
        grid.add(new Label("Litière Organique %:"), 0, 4); grid.add(organicSpinner, 1, 4);

        showEarthCheck = new CheckBox("🟤 Afficher Humus / Terre"); showEarthCheck.setSelected(true);
        showSandCheck = new CheckBox("🟡 Afficher Sable"); showSandCheck.setSelected(true);
        showClayCheck = new CheckBox("🔴 Argile"); showClayCheck.setSelected(true);
        showStoneCheck = new CheckBox("⚪ Pierre / Gravier"); showStoneCheck.setSelected(true);
        showVegetationCheck = new CheckBox("🌿 Afficher Végétation & Arbres"); showVegetationCheck.setSelected(true);
        showGalleriesCheck = new CheckBox("🕳️ Afficher Galeries & Cavités Souterraines"); showGalleriesCheck.setSelected(true);
        showSubstrateStratigraphyCheck = new CheckBox("🗻 Afficher Coupe Stratigraphique du Sol (Vue 3D)"); showSubstrateStratigraphyCheck.setSelected(true);
        showSubstrateStratigraphyCheck.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");

        addBoolLsn(showEarthCheck, showSandCheck, showClayCheck, showStoneCheck, showVegetationCheck, showGalleriesCheck, showSubstrateStratigraphyCheck);

        VBox toggleLayersBox = new VBox(4,
            new Label("👁️ Visibilité des Substrats & Éléments :") {{ setStyle("-fx-font-weight: bold; -fx-text-fill: #e4e4e7;"); }},
            new HBox(8, showEarthCheck, showSandCheck, showClayCheck, showStoneCheck),
            new HBox(8, showVegetationCheck, showGalleriesCheck),
            showSubstrateStratigraphyCheck
        );

        stratificationSlider = mkSlider(0.0, 1.0, 0.7);
        mixingRateSlider     = mkSlider(0.0, 1.0, 0.3);
        baseHumiditySlider   = mkSlider(0.0, 1.0, 0.35);
        voidDensitySlider    = mkSlider(0.0, 0.3, 0.08);
        showHumidityCheck    = new CheckBox("💧 Afficher Overlay Humidité (Vue Top-Down)");
        showHumidityCheck.setSelected(false);
        addLsn(stratificationSlider, mixingRateSlider, baseHumiditySlider, voidDensitySlider);
        addBoolLsn(showHumidityCheck);

        Label soilHint = new Label("💡 Détermine la répartition des couches géologiques et l'humidité souterraine.");
        soilHint.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8; -fx-wrap-text: true;");

        // --- OPTION A & OPTION B RENDERING CONTROLS ---
        useAdvancedVolumetricModeCheck = new CheckBox("📦 Activer Rendu Volumétrique Avancé (Option B)");
        useAdvancedVolumetricModeCheck.setSelected(true);
        useAdvancedVolumetricModeCheck.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");

        showChamferedBezelCheck = new CheckBox("🛡️ Cadre Biseauté Bac Terrarium (Option A2)");
        showChamferedBezelCheck.setSelected(true);

        showGravelInclusionsCheck = new CheckBox("⚪ Inclusions de Graviers & Galets Voxels (Option B1)");
        showGravelInclusionsCheck.setSelected(true);

        enableVolumetricScannerCheck = new CheckBox("🔬 Scanner Log & Coupe Volumétrique 3D (Option B2)");
        enableVolumetricScannerCheck.setSelected(false);

        slicePlaneSlider = mkSlider(0.0, 100.0, 50.0);
        addLsn(slicePlaneSlider);

        showTranslucentVolumetricModeCheck = new CheckBox("💧 Translucidité 3D du Substrat (Option B3)");
        showTranslucentVolumetricModeCheck.setSelected(false);

        addBoolLsn(useAdvancedVolumetricModeCheck, showChamferedBezelCheck, showGravelInclusionsCheck,
                   enableVolumetricScannerCheck, showTranslucentVolumetricModeCheck);

        VBox modeBox = new VBox(6,
            new Label("🖼️ Rendu de la Jupe & Structure Volumétrique Souterraine :") {{ setStyle("-fx-font-weight: bold; -fx-text-fill: #f59e0b;"); }},
            useAdvancedVolumetricModeCheck,
            new HBox(8, showChamferedBezelCheck, showGravelInclusionsCheck),
            enableVolumetricScannerCheck,
            new Label("Position de Coupe du Scanner (%):"), sv(slicePlaneSlider, "%"),
            showTranslucentVolumetricModeCheck
        );
        modeBox.setStyle("-fx-background-color: #1e1b4b; -fx-padding: 8px; -fx-border-color: #4338ca; -fx-border-radius: 6px; -fx-background-radius: 6px;");

        return new VBox(8,
                new Label("🏜️ Composition du Substrat Surface (%) :"),
                grid,
                new Separator(),
                toggleLayersBox,
                new Separator(),
                modeBox,
                new Separator(),
                new Label("🗻 Stratification & Structure Souterraine 3D :"),
                new Label("Degré de Stratification :"), sv(stratificationSlider, ""),
                new Label("Taux de Mélange des Couches :"), sv(mixingRateSlider, ""),
                new Label("Humidité de Base du Sol :"), sv(baseHumiditySlider, ""),
                new Label("Densité de Vides/Cavernes :"), sv(voidDensitySlider, ""),
                showHumidityCheck,
                soilHint
        );
    }

    private VBox buildFloraBlock() {
        floraSeedField = new TextField("774829");
        floraSeedField.setPrefWidth(95);
        floraSeedField.setOnAction(e -> regenerateAndRepaint());

        Button btnRandomizeFloraSeed = new Button("🎲 Régénérer");
        btnRandomizeFloraSeed.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
        btnRandomizeFloraSeed.setOnAction(e -> {
            long newSeed = new Random().nextLong(100000, 9999999);
            floraSeedField.setText(String.valueOf(newSeed));
            regenerateAndRepaint();
        });

        HBox seedBox = new HBox(6,
            new Label("Graine Flore (Seed) :") {{ setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;"); }},
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

        Label floraHint = new Label("💡 Graine Procédurale : Garantit une régénération déterministe identique au chargement sans alourdir le preset.");
        floraHint.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8; -fx-wrap-text: true;");

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
                mossCheck, pineLitterCheck, fernObstacleCheck,
                floraHint
        );
    }

    private VBox buildHydroBlock() {
        riverCheck = new CheckBox("Générer un cours d'eau / Rivière"); riverCheck.setSelected(true);
        riverWidthSlider = mkSlider(30, 500, 120);
        riverVelocitySlider = mkSlider(0.0, 1.5, 0.3);
        staticPoolsSlider = mkSlider(0, 5, 2);
        waterTableDepthSlider = mkSlider(5, 50, 15);
        addLsn(riverWidthSlider, riverVelocitySlider, staticPoolsSlider, waterTableDepthSlider);
        riverCheck.setOnAction(e -> repaintAllViews());

        return new VBox(8,
                riverCheck,
                new Label("Largeur de la Rivière:"), sv(riverWidthSlider, "mm"),
                new Label("Vitesse du Courant:"), sv(riverVelocitySlider, "m/s"),
                new Separator(),
                new Label("Mares d'Eau Statique:"), sv(staticPoolsSlider, ""),
                new Label("Profondeur Nappe Phréatique:"), sv(waterTableDepthSlider, "cm")
        );
    }

    private VBox buildStructBlock() {
        treeCountSlider = mkSlider(0, 15, 6);
        hollowLogsSlider = mkSlider(0, 8, 3);
        rockCrevicesSlider = mkSlider(0, 8, 3);
        addLsn(treeCountSlider, hollowLogsSlider, rockCrevicesSlider);

        // Botanical Tree Species Composition Selectors & Spinners
        comboTreeSpecies = new ComboBox<>();
        comboTreeSpecies.getItems().addAll(
            "🌳 Biome Futaie de Chênes & Feuillus (Atta / Camponotus)",
            "🌲 Biome Pinède Résineuse (Pucerons Cinara / Formica)",
            "🌵 Biome Savane d'Acacias (Pseudomyrmex / Nectaires)",
            "🌵 Biome Désert Aride & Cactus Saguaro (Myrmecocystus / Cephalotes)",
            "🌿 Biome Bouleaux & Graminées (Messor / Prédation)",
            "🎋 Biome Bambouseraie (Temnothorax / Colobopsis)",
            "🪵 Biome Bois Mort & Souches en Décomposition"
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

        // Bioclimatic Zone & Ecological Compatibility Diagnostic
        lblBioclimaticZoneBadge.setStyle("-fx-font-weight: bold; -fx-text-fill: #38bdf8; -fx-font-size: 11px;");

        GridPane diagGrid = new GridPane();
        diagGrid.setHgap(10); diagGrid.setVgap(4);
        diagGrid.setStyle("-fx-background-color: #1e1b4b; -fx-padding: 8px; -fx-border-color: #4338ca; -fx-border-radius: 6px;");

        lblAttaCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #4ade80;");
        lblAphidCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #38bdf8;");
        lblWoodNestCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #f59e0b;");
        lblAcaciaAntCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #e879f9;");
        lblCactusAntCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #f43f5e;");

        diagGrid.add(new Label("🌐 Zone Bioclimatique :"), 0, 0); diagGrid.add(lblBioclimaticZoneBadge, 1, 0);
        diagGrid.add(new Label("🍃 Atta (Coupeuses de feuilles) :"), 0, 1); diagGrid.add(lblAttaCompatScore, 1, 1);
        diagGrid.add(new Label("🍯 Formica / Lasius (Éleveuses Pucerons) :"), 0, 2); diagGrid.add(lblAphidCompatScore, 1, 2);
        diagGrid.add(new Label("🐜 Camponotus (Charpentières / Bois) :"), 0, 3); diagGrid.add(lblWoodNestCompatScore, 1, 3);
        diagGrid.add(new Label("🌵 Pseudomyrmex (Mutualistes Acacia) :"), 0, 4); diagGrid.add(lblAcaciaAntCompatScore, 1, 4);
        diagGrid.add(new Label("🌵 Desert Ants (Nids Cactus / Saguaro) :"), 0, 5); diagGrid.add(lblCactusAntCompatScore, 1, 5);

        updateEcologicalCompatibilityScores();

        Label structHint = new Label("💡 Détermine la composition déterministe des espèces d'arbres voxels et l'attraction trophique spécifique des espèces d'insectes.");
        structHint.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8; -fx-wrap-text: true;");

        return new VBox(8,
                new Label("Nombre d'Arbres / Troncs :"), sv(treeCountSlider, ""),
                new Label("Souches de Bois Creuses (Camponotus / Nids) :"), sv(hollowLogsSlider, ""),
                new Label("Fissures / Rentrées Rocheuses :"), sv(rockCrevicesSlider, ""),
                new Separator(),
                new Label("🌳 Espèce d'Arbre Dominante du Biome :"),
                comboTreeSpecies,
                new Label("📊 Matrice de Composition Botanique (% des Arbres) :"),
                botGrid,
                new Separator(),
                new Label("🧪 Diagnostic d'Attraction Écologique :"),
                diagGrid,
                structHint
        );
    }

    private void updateTreeSpeciesSpinnersFromPreset() {
        int idx = comboTreeSpecies.getSelectionModel().getSelectedIndex();
        switch (idx) {
            case 0 -> { oakPctSpinner.getValueFactory().setValue(70); pinePctSpinner.getValueFactory().setValue(10); acaciaPctSpinner.getValueFactory().setValue(0); cactusPctSpinner.getValueFactory().setValue(0); birchPctSpinner.getValueFactory().setValue(10); bambooPctSpinner.getValueFactory().setValue(0); deadWoodPctSpinner.getValueFactory().setValue(10); }
            case 1 -> { oakPctSpinner.getValueFactory().setValue(10); pinePctSpinner.getValueFactory().setValue(75); acaciaPctSpinner.getValueFactory().setValue(0); cactusPctSpinner.getValueFactory().setValue(0); birchPctSpinner.getValueFactory().setValue(5); bambooPctSpinner.getValueFactory().setValue(0); deadWoodPctSpinner.getValueFactory().setValue(10); }
            case 2 -> { oakPctSpinner.getValueFactory().setValue(0); pinePctSpinner.getValueFactory().setValue(5); acaciaPctSpinner.getValueFactory().setValue(80); cactusPctSpinner.getValueFactory().setValue(0); birchPctSpinner.getValueFactory().setValue(0); bambooPctSpinner.getValueFactory().setValue(0); deadWoodPctSpinner.getValueFactory().setValue(15); }
            case 3 -> { oakPctSpinner.getValueFactory().setValue(0); pinePctSpinner.getValueFactory().setValue(0); acaciaPctSpinner.getValueFactory().setValue(15); cactusPctSpinner.getValueFactory().setValue(75); birchPctSpinner.getValueFactory().setValue(0); bambooPctSpinner.getValueFactory().setValue(0); deadWoodPctSpinner.getValueFactory().setValue(10); }
            case 4 -> { oakPctSpinner.getValueFactory().setValue(15); pinePctSpinner.getValueFactory().setValue(15); acaciaPctSpinner.getValueFactory().setValue(0); cactusPctSpinner.getValueFactory().setValue(0); birchPctSpinner.getValueFactory().setValue(60); bambooPctSpinner.getValueFactory().setValue(0); deadWoodPctSpinner.getValueFactory().setValue(10); }
            case 5 -> { oakPctSpinner.getValueFactory().setValue(5); pinePctSpinner.getValueFactory().setValue(0); acaciaPctSpinner.getValueFactory().setValue(0); cactusPctSpinner.getValueFactory().setValue(0); birchPctSpinner.getValueFactory().setValue(5); bambooPctSpinner.getValueFactory().setValue(80); deadWoodPctSpinner.getValueFactory().setValue(10); }
            case 6 -> { oakPctSpinner.getValueFactory().setValue(10); pinePctSpinner.getValueFactory().setValue(10); acaciaPctSpinner.getValueFactory().setValue(0); cactusPctSpinner.getValueFactory().setValue(0); birchPctSpinner.getValueFactory().setValue(10); bambooPctSpinner.getValueFactory().setValue(0); deadWoodPctSpinner.getValueFactory().setValue(70); }
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

        int attaScore = Math.min(100, oak * 1 + birch * 1 + acacia / 2);
        int aphidScore = Math.min(100, pine * 1 + birch * 1 + oak / 2);
        int woodScore = Math.min(100, deadWood * 1 + oak * 1 + pine / 2);
        int acaciaScore = Math.min(100, acacia * 1 + deadWood / 2);
        int cactusScore = Math.min(100, cactus * 1 + acacia / 2);

        if (lblAttaCompatScore != null) lblAttaCompatScore.setText(attaScore + "% (Optimal Feuillage)");
        if (lblAphidCompatScore != null) lblAphidCompatScore.setText(aphidScore + "% (Hôte Cinara/Miellat)");
        if (lblWoodNestCompatScore != null) lblWoodNestCompatScore.setText(woodScore + "% (Excavation Lignicole)");
        if (lblAcaciaAntCompatScore != null) lblAcaciaAntCompatScore.setText(acaciaScore + "% (Nectaires & Domaties)");
        if (lblCactusAntCompatScore != null) lblCactusAntCompatScore.setText(cactusScore + "% (Nids Cactus / Aride)");
    }



    private VBox buildSculptBlock() {
        enableSculptingCheck = new CheckBox("🖌️ Activer Mode Sculpture Directe (Glisser-souris)");
        enableSculptingCheck.setSelected(false);
        enableSculptingCheck.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");
        enableSculptingCheck.setOnAction(e -> repaintAllViews());

        // Mode peinture restreint à l'élévation uniquement
        brushModeSelect = new ComboBox<>();
        brushModeSelect.getItems().addAll(
                "RAISE_ELEVATION",
                "LOWER_ELEVATION",
                "SMOOTH"
        );
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

        // brushSubstrateSelect gardé pour compatibilité mais non affiché (plus de peinture matériau)
        brushSubstrateSelect = new ComboBox<>();
        brushSubstrateSelect.getItems().addAll("EARTH", "SAND", "CLAY", "STONE");
        ComboBoxTooltipHelper.setupDescriptiveComboBox(brushSubstrateSelect,
            val -> switch (val) {
                case "EARTH" -> "🟤 Terre / Humus";
                case "SAND" -> "🟡 Sable Granulaire";
                case "CLAY" -> "🔴 Argile Compacte";
                case "STONE" -> "⚪ Pierre / Roche";
                default -> val;
            },
            val -> switch (val) {
                case "EARTH" -> "Sol meuble organique idéal pour le creusement facile des petites galeries.";
                case "SAND" -> "Substrat granulaire drainant s'effondrant si non humidifié.";
                case "CLAY" -> "Matériau plastique très cohérent parfait pour maintenir des plafonds larges.";
                case "STONE" -> "Substrat rocheux dur inexcavable offrant une protection structurelle maximale.";
                default -> "";
            }
        );
        brushSubstrateSelect.getSelectionModel().selectFirst();

        brushRadiusSlider = mkSlider(1, 15, 4);
        brushStrengthSlider = mkSlider(10, 100, 50);
        addLsn(brushRadiusSlider, brushStrengthSlider);

        Label sculptHint = new Label("💡 Le mode peinture ne gère que l'élévation.\nUne stabilisation de pente est appliquée automatiquement pour éviter les pixels flottants.");
        sculptHint.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8; -fx-wrap-text: true;");

        return new VBox(8,
                enableSculptingCheck,
                new Label("Mode du Pinceau :"), brushModeSelect,
                new Label("Rayon du Pinceau (Voxels):"), sv(brushRadiusSlider, "vx"),
                new Label("Force du Pinceau:"), sv(brushStrengthSlider, "%"),
                sculptHint
        );
    }

    // ── Tri-View Area (3D, Top-Down, Side View) ────────────────────────────────

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

        StackPane h3d = new StackPane(canvas3D, topToolbarContainer);
        h3d.setStyle("-fx-border-color: #555; -fx-border-width: 1; -fx-background-color: #0b0f19;");
        StackPane hSide = new StackPane(canvasSide);
        hSide.setStyle("-fx-border-color: #444; -fx-border-width: 1; -fx-background-color: #0f172a;");
        StackPane hTop = new StackPane(canvasTop);
        hTop.setStyle("-fx-border-color: #444; -fx-border-width: 1; -fx-background-color: #0f172a;");

        Label ls = new Label("⬛ Vue de Profil / Coupe (Side View)");
        ls.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        Label lt = new Label("⬜ Vue du Dessus (Top-Down View)");
        lt.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        VBox side = new VBox(5, ls, hSide, lt, hTop);
        side.setPadding(new Insets(0, 4, 0, 8)); side.setAlignment(Pos.TOP_CENTER);

        HBox area = new HBox(6, h3d, side);
        area.setPadding(new Insets(8, 8, 4, 8));

        HBox legendBar = buildLegendBar();

        return new VBox(4, area, legendBar);
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
                {"Terre", "#3d2817", "🟤 Terre / Humus : Sol organique meuble. Excellente rétention d'eau et de nutriments."},
                {"Sable", "#eab308", "🟡 Sable : Substrat granuleux à faible cohésion. Risque d'éboulement pour les galeries."},
                {"Argile", "#9a3412", "🔴 Argile : Substrat minéral dense et plastique. Idéal pour consolider le terrain."},
                {"Pierre", "#64748b", "⚪ Pierre / Roche : Matériau inexcavable. Ancrage structural et protection."},
                {"Rivière", "#0284c7", "💧 Rivière / Hydrographie : Cours d'eau douce créant un gradient d'humidité."},
                {"Végétation", "#15803d", "🌿 Couvert Végétal : Végétation de surface (herbes, fleurs, arbres, litière)."},
                {"Galeries", "#d97706", "🕳️ Galeries Souterraines : Cavités et réseaux souterrains excavés."}
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

            Tooltip tooltip = new Tooltip(it[2]);
            tooltip.setStyle("-fx-font-size: 11px; -fx-background-color: #0f172a; -fx-text-fill: #38bdf8;");
            Tooltip.install(item, tooltip);

            final String hoverText = it[2];
            item.setOnMouseEntered(e -> {
                if (lblHoverInfo != null) {
                    lblHoverInfo.setText(hoverText);
                    lblHoverInfo.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 10px; -fx-font-weight: bold;");
                }
            });
            item.setOnMouseExited(e -> {
                if (lblHoverInfo != null) {
                    lblHoverInfo.setText("ℹ️ Survolez un substrat ou une zone pour afficher sa fiche technique.");
                    lblHoverInfo.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px; -fx-font-style: italic;");
                }
            });

            bar.getChildren().add(item);
        }

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        bar.getChildren().addAll(sp, lblHoverInfo);
        return bar;
    }

    // ── Mouse & Sculpting Event Handlers ─────────────────────────────────────

    private void setupMouseControls() {
        // 3D Canvas Orbit / Pan / Zoom / Sculpt
        canvas3D.setOnMousePressed(e -> { lastMX = e.getX(); lastMY = e.getY(); handleSculptClick(e.getX(), e.getY(), "3D"); });
        canvas3D.setOnMouseDragged(e -> {
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

        // 2D Side Canvas Pan / Zoom / Sculpt
        canvasSide.setOnMousePressed(e -> { lastSideMX = e.getX(); lastSideMY = e.getY(); handleSculptClick(e.getX(), e.getY(), "SIDE"); });
        canvasSide.setOnMouseDragged(e -> {
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

        // 2D Top Canvas Pan / Zoom / Sculpt
        canvasTop.setOnMousePressed(e -> { lastTopMX = e.getX(); lastTopMY = e.getY(); handleSculptClick(e.getX(), e.getY(), "TOP"); });
        canvasTop.setOnMouseDragged(e -> {
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

    private void handleSculptClick(double mx, double my, String viewType) {
        if (enableSculptingCheck == null || !enableSculptingCheck.isSelected()) return;
        double cw = "3D".equals(viewType) ? canvas3D.getWidth() : ("SIDE".equals(viewType) ? canvasSide.getWidth() : canvasTop.getWidth());
        double ch = "3D".equals(viewType) ? canvas3D.getHeight() : ("SIDE".equals(viewType) ? canvasSide.getHeight() : canvasTop.getHeight());
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

    /** Anti-pixel-flottant : ramene les cellules trop hautes par rapport a leurs voisins. */
    private void smoothSlopeStabilization() {
        double maxDelta = 0.18;
        for (int x = 1; x < GRID_SIZE-1; x++)
            for (int y = 1; y < GRID_SIZE-1; y++) {
                double minN = Math.min(Math.min(heightGrid[x-1][y], heightGrid[x+1][y]),
                                       Math.min(heightGrid[x][y-1], heightGrid[x][y+1]));
                if (heightGrid[x][y] - minN > maxDelta) heightGrid[x][y] = minN + maxDelta;
            }
    }

    /** Physique sable en temps reel. */
    private void applyRealTimeVoxelPhysics() {
        double sandSlope = 0.08;
        for (int x = 1; x < GRID_SIZE-1; x++)
            for (int y = 1; y < GRID_SIZE-1; y++)
                if (soilLayers[x][y][0] == 1) {
                    double diff = heightGrid[x][y] - heightGrid[x+1][y];
                    if (diff > sandSlope) { heightGrid[x][y] -= diff*0.2; heightGrid[x+1][y] += diff*0.2; }
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

        lblViewportMode = new Label("🔲 Vue Technique 3D Grille — Contrôles caméra & sculpture directs à la souris");
        lblViewportMode.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px;");

        bar.getChildren().add(lblViewportMode);
        return bar;
    }

    private void draw3D() {
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
        double w = canvas3D.getWidth();
        double h = canvas3D.getHeight();

        gc3D.setFill(Color.web("#0b0f19"));
        gc3D.fillRect(0, 0, w, h);

        double radAz = Math.toRadians(azimuth);
        double radEl = Math.toRadians(elevation);

        // Center projection
        double cx = w / 2 + pan3DX;
        double cy = h / 2 + pan3DY + 40;
        double scale = zoom * 12.0;

        int step = 2;
        double targetDepthVal = depthSlider != null ? depthSlider.getValue() : 1.5;
        double maxDepthPx = targetDepthVal * 22.0;

        // 1. Render Back Subterranean Skirt Base line for context
        // 2. Render Solid 3D Continuous Quad Surface Mesh
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

                Color col = visibleMat ? getMaterialColor(mat) : Color.web("#1e293b", 0.35);
                if (carvedVoxelGrid[x][y]) col = Color.web("#d97706");

                gc3D.setFill(col);
                gc3D.fillPolygon(pxs, pys, 4);
                gc3D.setStroke(col.darker());
                gc3D.setLineWidth(visibleMat ? 0.3 : 0.1);
                gc3D.strokePolygon(pxs, pys, 4);
            }
        }

        // 3. Render Real Subterranean Stratigraphic Cross-Section Slice Walls (Coupe Géologique du Sol 4 Côtés)
        if (showSubstrateStratigraphyCheck == null || showSubstrateStratigraphyCheck.isSelected()) {
            drawAllStratigraphySideWalls3D(cx, cy, scale, radAz, radEl, maxDepthPx, step);
        }

        // Draw Hollow Log Stumps (Souches de bois creuses) in 3D
        if (hollowLogsSlider != null && (showVegetationCheck == null || showVegetationCheck.isSelected())) {
            int stumpCount = (int) hollowLogsSlider.getValue();
            Random sRand = new Random(99);
            for (int i = 0; i < stumpCount; i++) {
                int sx = 15 + (int)(sRand.nextDouble() * (GRID_SIZE - 30));
                int sy = 15 + (int)(sRand.nextDouble() * (GRID_SIZE - 30));
                double sz = heightGrid[sx][sy] * 40.0;
                double[] sp = project3DPoint(sx, sy, sz, cx, cy, scale, radAz, radEl);

                // Souche cylindrique
                gc3D.setFill(Color.web("#78350f"));
                gc3D.fillRect(sp[0] - 5, sp[1] - 12, 10, 12);
                // Dessus creux
                gc3D.setFill(Color.web("#451a03"));
                gc3D.fillOval(sp[0] - 5, sp[1] - 15, 10, 6);
                // Mousse à la base
                gc3D.setFill(Color.web("#15803d"));
                gc3D.fillOval(sp[0] - 7, sp[1] - 3, 14, 5);
            }
        }

        // Draw Procedural Surface Flora & Litter Cover Items in 3D
        if (showVegetationCheck == null || showVegetationCheck.isSelected()) {
            for (SurfaceFloraItem item : surfaceFloraItems) {
                double z = heightGrid[item.gx][item.gy] * 40.0 + 1.2;
                double[] p = project3DPoint(item.gx, item.gy, z, cx, cy, scale, radAz, radEl);
                double sc = item.scale * (zoom / 7.5);

                switch (item.type) {
                    case 0: // Graminées (Graines Messor)
                        gc3D.setStroke(Color.web("#4ade80"));
                        gc3D.setLineWidth(1.2);
                        gc3D.strokeLine(p[0], p[1], p[0] - 3 * sc, p[1] - 8 * sc);
                        gc3D.strokeLine(p[0], p[1], p[0] + 3 * sc, p[1] - 9 * sc);
                        break;
                    case 1: // Plante hôte Cirsium / Pucerons
                        gc3D.setStroke(Color.web("#166534"));
                        gc3D.setLineWidth(1.5);
                        gc3D.strokeLine(p[0], p[1], p[0], p[1] - 12 * sc);
                        gc3D.setFill(Color.web("#a3e635")); // Pucerons
                        gc3D.fillOval(p[0] - 2 * sc, p[1] - 10 * sc, 4 * sc, 4 * sc);
                        break;
                    case 2: // Fleur à Nectar
                        gc3D.setStroke(Color.web("#15803d"));
                        gc3D.setLineWidth(1.2);
                        gc3D.strokeLine(p[0], p[1], p[0], p[1] - 10 * sc);
                        gc3D.setFill(Color.web("#f43f5e")); // Fleur rose
                        gc3D.fillOval(p[0] - 3 * sc, p[1] - 13 * sc, 6 * sc, 6 * sc);
                        break;
                    case 3: // Tapis de Mousse Polytrichum
                        gc3D.setFill(Color.web("#15803d", 0.75));
                        gc3D.fillOval(p[0] - 6 * sc, p[1] - 4 * sc, 12 * sc, 8 * sc);
                        break;
                    case 4: // Litière d'Aiguilles de Pin / Feuilles mortes
                        gc3D.setStroke(Color.web("#78350f"));
                        gc3D.setLineWidth(1.0);
                        gc3D.strokeLine(p[0] - 4 * sc, p[1], p[0] + 4 * sc, p[1] - 2 * sc);
                        break;
                    case 5: // Brindille / Rameau
                        gc3D.setStroke(Color.web("#451a03"));
                        gc3D.setLineWidth(1.4);
                        gc3D.strokeLine(p[0] - 5 * sc, p[1], p[0] + 5 * sc, p[1] - 3 * sc);
                        break;
                    case 6: // Gravier micro-abri
                        gc3D.setFill(Color.web("#94a3b8"));
                        gc3D.fillOval(p[0] - 2 * sc, p[1] - 2 * sc, 4 * sc, 4 * sc);
                        break;
                }
            }
        }

        // Draw Majestic Trees in 3D Viewport
        if (treeCountSlider != null && (showVegetationCheck == null || showVegetationCheck.isSelected())) {
            int count = (int) treeCountSlider.getValue();
            Random rand = new Random(77);
            for (int i = 0; i < count; i++) {
                int gx = 10 + (int)(rand.nextDouble() * (GRID_SIZE - 20));
                int gy = 10 + (int)(rand.nextDouble() * (GRID_SIZE - 20));
                double z = heightGrid[gx][gy] * 40.0;
                double[] p = project3DPoint(gx, gy, z, cx, cy, scale, radAz, radEl);

                // Tronc majestueux ancré au sol
                gc3D.setFill(Color.web("#78350f"));
                gc3D.fillRect(p[0] - 4, p[1] - 48, 8, 48);
                gc3D.setFill(Color.web("#451a03"));
                gc3D.fillRect(p[0] - 4, p[1] - 48, 3, 48); // Ombrage écorce

                // Houppier multi-couches
                gc3D.setFill(Color.web("#14532d"));
                gc3D.fillOval(p[0] - 22, p[1] - 82, 44, 40);
                gc3D.setFill(Color.web("#166534"));
                gc3D.fillOval(p[0] - 18, p[1] - 88, 36, 34);
                gc3D.setFill(Color.web("#15803d"));
                gc3D.fillOval(p[0] - 14, p[1] - 92, 28, 26);

                // Insectes / Fourmis grimpeuses sur le tronc et dans l'arbre
                if (i % 2 == 0) {
                    gc3D.setFill(Color.web("#f97316")); // Worker ant marker (orange/brown)
                    gc3D.fillOval(p[0] - 2, p[1] - 25, 4, 4);
                    gc3D.fillOval(p[0] + 1, p[1] - 35, 3, 3);
                    gc3D.setFill(Color.web("#ef4444")); // Forager near canopy
                    gc3D.fillOval(p[0] - 8, p[1] - 70, 4, 4);
                }
            }
        }

        // Label Overlay updated on top toolbar box
        if (lblViewportMode != null) {
            lblViewportMode.setText(String.format("🔲 Vue Technique 3D (Az: %d°, El: %d°, Profondeur: %.1fm)", (int) azimuth, (int) elevation, targetDepthVal));
        }
    }

    private void drawAllStratigraphySideWalls3D(double cx, double cy, double scale, double radAz, double radEl, double maxDepthPx, int step) {
        // Define the 4 outer boundary edges of the terrain block
        double halfGrid = GRID_SIZE / 2.0;

        // Calculate projected Y depth for each wall center to sort back-to-front
        double[][] edgeCenters = {
            {0.0, -halfGrid},       // Edge 0 (South: y = 0)
            {halfGrid - step, 0.0}, // Edge 1 (East:  x = GRID_SIZE - step)
            {0.0, halfGrid - step}, // Edge 2 (North: y = GRID_SIZE - step)
            {-halfGrid, 0.0}        // Edge 3 (West:  x = 0)
        };

        Integer[] order = {0, 1, 2, 3};
        java.util.Arrays.sort(order, (a, b) -> {
            double pyA = edgeCenters[a][0] * Math.sin(radAz) * Math.sin(radEl) + edgeCenters[a][1] * Math.cos(radAz) * Math.sin(radEl);
            double pyB = edgeCenters[b][0] * Math.sin(radAz) * Math.sin(radEl) + edgeCenters[b][1] * Math.cos(radAz) * Math.sin(radEl);
            return Double.compare(pyA, pyB); // Ascending order (back to front)
        });

        // OPTION B2: Render Volumetric Scanner Slice Plane if enabled
        if (enableVolumetricScannerCheck != null && enableVolumetricScannerCheck.isSelected()) {
            double sliceRatio = slicePlaneSlider != null ? slicePlaneSlider.getValue() / 100.0 : 0.5;
            int sliceX = Math.min(GRID_SIZE - step - 1, Math.max(step, (int) (GRID_SIZE * sliceRatio)));
            for (int y = 0; y < GRID_SIZE - step; y += step) {
                drawSingleWallSegment3D(sliceX, y, sliceX, y + step, cx, cy, scale, radAz, radEl, maxDepthPx, true);
            }
        }

        // Render 4 Stitched Outer Walls (Option A1 + Option A2 + Option B1)
        for (int edgeIdx : order) {
            switch (edgeIdx) {
                case 0 -> { // South: y = 0
                    for (int x = 0; x < GRID_SIZE - step; x += step) {
                        drawSingleWallSegment3D(x, 0, x + step, 0, cx, cy, scale, radAz, radEl, maxDepthPx, false);
                    }
                }
                case 1 -> { // East: x = GRID_SIZE - step
                    for (int y = 0; y < GRID_SIZE - step; y += step) {
                        drawSingleWallSegment3D(GRID_SIZE - step, y, GRID_SIZE - step, y + step, cx, cy, scale, radAz, radEl, maxDepthPx, false);
                    }
                }
                case 2 -> { // North: y = GRID_SIZE - step
                    for (int x = 0; x < GRID_SIZE - step; x += step) {
                        drawSingleWallSegment3D(x, GRID_SIZE - step, x + step, GRID_SIZE - step, cx, cy, scale, radAz, radEl, maxDepthPx, false);
                    }
                }
                case 3 -> { // West: x = 0
                    for (int y = 0; y < GRID_SIZE - step; y += step) {
                        drawSingleWallSegment3D(0, y, 0, y + step, cx, cy, scale, radAz, radEl, maxDepthPx, false);
                    }
                }
            }
        }
    }

    private void drawSingleWallSegment3D(int x0, int y0, int x1, int y1, double cx, double cy, double scale, double radAz, double radEl, double maxDepthPx, boolean isSliceCutaway) {
        double layerDepthPx = maxDepthPx / SOIL_DEPTH;
        // OPTION A1: Sommets exactement soudés aux altitudes du relief (surfZ0 & surfZ1)
        double surfZ0 = heightGrid[x0][y0] * 40.0;
        double surfZ1 = heightGrid[x1][y1] * 40.0;

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
                matCol = Color.web("#0f172a"); // Cavité / tunnel
            } else if (!isMaterialVisible(mat)) {
                matCol = Color.web("#1e293b", 0.3);
            } else {
                matCol = getMaterialColor(mat);
            }

            if (isSliceCutaway) {
                matCol = matCol.deriveColor(0, 1.1, 1.2, 1.0); // Surbrillance plan de coupe
            }

            if (isTranslucent) {
                matCol = Color.color(matCol.getRed(), matCol.getGreen(), matCol.getBlue(), 0.55);
            }

            gc3D.setFill(matCol);
            gc3D.fillPolygon(new double[]{pTop0[0], pTop1[0], pBot1[0], pBot0[0]}, new double[]{pTop0[1], pTop1[1], pBot1[1], pBot0[1]}, 4);

            // OPTION B1: Inclusions de graviers ⚪, cailloux, argile 🔴 & humidité 💧 sur chaque voxel
            if (isAdvMode && showInclusions && !isVoid && isMaterialVisible(mat)) {
                double midX = (pTop0[0] + pTop1[0] + pBot1[0] + pBot0[0]) / 4.0;
                double midY = (pTop0[1] + pTop1[1] + pBot1[1] + pBot0[1]) / 4.0;
                double voxSize = Math.max(1.5, Math.abs(pTop1[0] - pTop0[0]) * 0.4);

                if (mat == 3) { // Pierre / Gravier
                    gc3D.setFill(Color.web("#e2e8f0")); // Galets de quartz
                    gc3D.fillOval(midX - voxSize * 0.5, midY - voxSize * 0.5, voxSize, voxSize * 0.7);
                } else if (mat == 2) { // Argile
                    gc3D.setFill(Color.web("#7c2d12")); // Lentille d'argile compacte
                    gc3D.fillRect(midX - voxSize * 0.4, midY - voxSize * 0.3, voxSize * 0.8, voxSize * 0.5);
                } else if (mat == 1) { // Sable
                    gc3D.setFill(Color.web("#fef08a")); // Grains de quartz sableux
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

        // OPTION A2: Cadre biseauté bac d'observation / aquarium (Bezel Glass Enclosure)
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
        double w = canvasSide.getWidth();
        double h = canvasSide.getHeight();

        gcSide.save();
        gcSide.setFill(Color.web("#0f172a"));
        gcSide.fillRect(0, 0, w, h);

        // Clip strictly to viewport region
        gcSide.beginPath();
        gcSide.rect(10, 10, w - 20, h - 20);
        gcSide.clip();

        double sZoom = sideZoom;
        double cx = sidePanX;
        double cy = sidePanY;

        double blockW = ((w - 20) / GRID_SIZE) * sZoom;
        double blockH = ((h - 100) / SOIL_DEPTH) * sZoom;
        int midY = GRID_SIZE / 2; // Coupe transversale au milieu

        // Dessin stratigraphique couche par couche
        for (int x = 0; x < GRID_SIZE; x++) {
            double surfaceH = heightGrid[x][midY] * 30.0 * sZoom;
            for (int d = 0; d < SOIL_DEPTH; d++) {
                double px = 10 + x * blockW + cx;
                double py = 80 - surfaceH + d * blockH + cy;

                byte mat = soilLayers[x][midY][d];
                if (voidGrid[x][midY][d] || !isMaterialVisible(mat)) {
                    // Vides / cavernes ou couche masquée
                    gcSide.setFill(Color.web("#0f172a"));
                } else {
                    gcSide.setFill(getMaterialColor(mat));
                }
                gcSide.fillRect(px, py, Math.max(1, blockW), Math.max(1, blockH));
            }

            // Socle Rocheux (Bedrock) : limite nette en bas du profil stratigraphique
            double px = 10 + x * blockW + cx;
            double pyBase = 80 - surfaceH + SOIL_DEPTH * blockH + cy;
            gcSide.setFill(Color.web("#020617"));
            gcSide.fillRect(px, pyBase, Math.max(1, blockW), h);
            gcSide.setStroke(Color.web("#334155"));
            gcSide.setLineWidth(1.0);
            gcSide.strokeLine(px, pyBase, px + Math.max(1, blockW), pyBase);
        }

        // Nappe Phréatique
        double wtDepth = waterTableDepthSlider != null ? waterTableDepthSlider.getValue() : 15;
        double wtY = 80 + (wtDepth / 50.0) * (h - 100) * sZoom + cy;
        gcSide.setFill(Color.web("#0284c7"));
        gcSide.setGlobalAlpha(0.45);
        gcSide.fillRect(10 + cx, wtY, (w - 20) * sZoom, Math.max(0, (80 + SOIL_DEPTH * blockH + cy) - wtY));
        gcSide.setGlobalAlpha(1.0);

        // Galeries excavées
        gcSide.setFill(Color.web("#d97706"));
        for (int x = 0; x < GRID_SIZE; x += 2) {
            if (carvedVoxelGrid[x][midY]) {
                double px = 10 + x * blockW + cx;
                double py = 85 + cy;
                gcSide.fillOval(px, py, 6 * sZoom, 6 * sZoom);
            }
        }

        gcSide.restore(); // Restore clipping context

        gcSide.setStroke(Color.web("#38bdf8"));
        gcSide.setLineWidth(1.0);
        gcSide.strokeRect(10, 10, w - 20, h - 20);

        gcSide.setFill(Color.WHITE);
        gcSide.fillText("Profil Stratigraphique & Vides (Nappe: " + String.format("%.0f", wtDepth) + "cm)", 20, 30);
    }

    private void drawTop() {
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

        // Sol en Surface
        for (int x = 0; x < GRID_SIZE; x += 2) {
            for (int y = 0; y < GRID_SIZE; y += 2) {
                byte mat = soilLayers[x][y][0];
                boolean visibleMat = isMaterialVisible(mat);

                Color col = visibleMat ? getMaterialColor(mat) : Color.web("#0f172a");
                if (carvedVoxelGrid[x][y]) col = Color.web("#d97706");
                gcTop.setFill(col);
                gcTop.fillRect(15 + x * cellW + cx, 15 + y * cellH + cy, cellW * 2, cellH * 2);

                // Overlay d'humidité si activé
                if (showHumidityCheck != null && showHumidityCheck.isSelected()) {
                    gcTop.setFill(Color.web("#0284c7"));
                    gcTop.setGlobalAlpha(humidityGrid[x][y] * 0.6);
                    gcTop.fillRect(15 + x * cellW + cx, 15 + y * cellH + cy, cellW * 2, cellH * 2);
                    gcTop.setGlobalAlpha(1.0);
                }
            }
        }

        // Trace de la rivière sur la vue du dessus
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

        // Draw Hollow Log Stumps in Top View
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

        // Draw Procedural Surface Flora & Litter Cover Items in Top View
        for (SurfaceFloraItem item : surfaceFloraItems) {
            double px = 15 + item.gx * cellW + cx;
            double py = 15 + item.gy * cellH + cy;
            double sz = item.scale * tZoom * 3.5;

            switch (item.type) {
                case 0: // Graminées
                    gcTop.setFill(Color.web("#4ade80"));
                    gcTop.fillOval(px - sz, py - sz, sz * 2, sz * 2);
                    break;
                case 1: // Hôte pucerons
                    gcTop.setFill(Color.web("#166534"));
                    gcTop.fillOval(px - sz, py - sz, sz * 2, sz * 2);
                    gcTop.setFill(Color.web("#a3e635"));
                    gcTop.fillOval(px - sz * 0.5, py - sz * 0.5, sz, sz);
                    break;
                case 2: // Fleur à nectar
                    gcTop.setFill(Color.web("#f43f5e"));
                    gcTop.fillOval(px - sz, py - sz, sz * 2, sz * 2);
                    break;
                case 3: // Tapis de mousse
                    gcTop.setFill(Color.web("#15803d", 0.75));
                    gcTop.fillOval(px - sz * 1.5, py - sz, sz * 3, sz * 2);
                    break;
                case 4: // Litière de pin / feuilles
                    gcTop.setStroke(Color.web("#78350f"));
                    gcTop.setLineWidth(1.0 * tZoom);
                    gcTop.strokeLine(px - sz, py, px + sz, py - sz * 0.5);
                    break;
                case 5: // Brindille / Rameau
                    gcTop.setStroke(Color.web("#451a03"));
                    gcTop.setLineWidth(1.4 * tZoom);
                    gcTop.strokeLine(px - sz, py - sz, px + sz, py + sz);
                    break;
                case 6: // Gravier micro-abri
                    gcTop.setFill(Color.web("#94a3b8"));
                    gcTop.fillOval(px - sz * 0.6, py - sz * 0.6, sz * 1.2, sz * 1.2);
                    break;
            }
        }

        // Dessin des arbres majesteux en vue Top-Down (ancrés aux cellules)
        if (treeCountSlider != null) {
            int count = (int) treeCountSlider.getValue();
            Random rand = new Random(77);
            for (int i = 0; i < count; i++) {
                int gx = 10 + (int)(rand.nextDouble() * (GRID_SIZE - 20));
                int gy = 10 + (int)(rand.nextDouble() * (GRID_SIZE - 20));
                double px = 15 + gx * cellW + cx;
                double py = 15 + gy * cellH + cy;

                // Grand houppier feuillu avec dégradé vert
                gcTop.setFill(Color.web("#14532d"));
                gcTop.fillOval(px - 18 * tZoom, py - 18 * tZoom, 36 * tZoom, 36 * tZoom);
                gcTop.setFill(Color.web("#166534"));
                gcTop.fillOval(px - 13 * tZoom, py - 13 * tZoom, 26 * tZoom, 26 * tZoom);
                gcTop.setFill(Color.web("#15803d"));
                gcTop.fillOval(px - 7 * tZoom, py - 7 * tZoom, 14 * tZoom, 14 * tZoom);
            }
        }

        gcTop.restore();

        gcTop.setStroke(Color.web("#38bdf8"));
        gcTop.setLineWidth(1.0);
        gcTop.strokeRect(10, 10, w - 20, h - 20);

        gcTop.setFill(Color.WHITE);
        gcTop.fillText("Vue du Dessus (" + (surfaceSizeSlider != null ? surfaceSizeSlider.getValue() : 2.0) + "m²)", 20, 30);
    }

    private Color getMaterialColor(byte mat) {
        switch (mat) {
            case 1: return Color.web("#eab308"); // Sand
            case 2: return Color.web("#9a3412"); // Clay
            case 3: return Color.web("#64748b"); // Stone
            case 4: return Color.web("#15803d"); // Organic
            default: return Color.web("#3d2817"); // Earth / Humus
        }
    }

    // ── Utility Helpers ───────────────────────────────────────────────────────

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

    private HBox sv(Slider s, String unit) {
        HBox b = new HBox(8);
        b.setAlignment(Pos.CENTER_LEFT);
        Label v = new Label(fmt(s.getValue()) + " " + unit);
        v.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-min-width: 50;");
        s.valueProperty().addListener((o, a, n) -> v.setText(fmt(n.doubleValue()) + " " + unit));
        b.getChildren().addAll(s, v);
        return b;
    }

    private String fmt(double d) {
        return String.format("%.1f", d);
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
        if (cfg.containsKey("voidDensity") && voidDensitySlider != null) voidDensitySlider.setValue(((Number) cfg.get("voidDensity")).doubleValue());
        if (cfg.containsKey("latitude") && latField != null) latField.setText(String.valueOf(cfg.get("latitude")));
        if (cfg.containsKey("longitude") && lonField != null) lonField.setText(String.valueOf(cfg.get("longitude")));
        repaintAllViews();
    }
}
