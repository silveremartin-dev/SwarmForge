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

    // View Synchronization CheckBox
    private CheckBox syncViewsCheckBox;

    // Viewport Mode Switch & Action Tool Selection (Matching Mockup Interface)
    private boolean gamifiedVoxelMode = true; // Mode Rendu Gamifié Voxel 3D vs Technique Grid
    private String activeViewportTool = "BRUSH"; // BRUSH, PLACE, ERASE, SMOOTH, SELECT, VIEW, ORBIT, PAN, ZOOM
    private Button btnRenderModeToggle;

    // Controls: 0. Terrain Source
    private CheckBox useWebServiceTerrainCheck;
    private ComboBox<String> webServiceProviderCombo;
    private TextField latField;
    private TextField lonField;

    // Controls: 1. Scale & Resolution
    private Slider surfaceSizeSlider; // Mètres (1.0 - 10.0m)
    private Slider resolutionSlider; // Sub-millimétrique (0.1 - 1.0mm)

    // Controls: 2. Soil & Relief
    private Slider roughnessSlider;
    private Slider compactionSlider;
    private Spinner<Integer> earthSpinner;
    private Spinner<Integer> sandSpinner;
    private Spinner<Integer> claySpinner;
    private Spinner<Integer> stoneSpinner;
    private Spinner<Integer> organicSpinner;

    // Controls: 3. Flora Ecosystem
    private Slider edibleDensitySlider;
    private Slider nonEdibleDensitySlider;
    private CheckBox aphidPlantCheck;
    private CheckBox nectarFlowersCheck;
    private CheckBox seedGrassCheck;
    private CheckBox fungusFoliageCheck;
    private CheckBox mossCheck;
    private CheckBox pineLitterCheck;
    private CheckBox fernObstacleCheck;

    // Controls: 4. Hydrology
    private CheckBox riverCheck;
    private Slider riverWidthSlider;
    private Slider riverVelocitySlider;
    private Slider staticPoolsSlider;
    private Slider waterTableDepthSlider;

    // Controls: 5. Vertical Host Structures
    private Slider treeCountSlider;
    private Slider hollowLogsSlider;
    private Slider rockCrevicesSlider;

    // Controls: 6. Initial Nest Mode
    private RadioButton foundingQueenRadio;
    private RadioButton prebuiltNestRadio;
    private ToggleGroup nestModeGroup;

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
        repaintAllViews();
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

        Label title = new Label("🌍 Éditeur de Monde");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        Label subtitle = new Label("Génération de relief, sol, ouvert végétal, hydrographie, sculpture 3D & déformation voxel (0.1-1.0mm)");
        subtitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button bExport = new Button("💾 Sauvegarder Preset (JSON)");
        bExport.getStyleClass().add("btn-secondary");
        bExport.setOnAction(e -> doExport());

        Button bImport = new Button("📂 Charger Preset (JSON)");
        bImport.getStyleClass().add("btn-secondary");
        bImport.setOnAction(e -> doImport());

        Button bGenerate = new Button("✨ Générer & Appliquer au Monde");
        bGenerate.getStyleClass().add("btn-primary");
        bGenerate.setOnAction(e -> triggerGenerate());

        r.getChildren().addAll(title, sp, bExport, bImport, new Separator(Orientation.VERTICAL), bGenerate);
        v.getChildren().addAll(r, subtitle);
        return v;
    }

    private ScrollPane buildConfig() {
        VBox cfg = new VBox(12);
        cfg.setPadding(new Insets(10));
        cfg.setPrefWidth(320);
        cfg.setStyle("-fx-background-color: #121214;");

        Accordion accordion = new Accordion();

        // 0. TERRAIN SOURCE SELECTOR
        TitledPane paneSource = new TitledPane("🌐 Source du Terrain (Procédural vs Service Web)", buildTerrainSourceBlock());

        // 1. SCALE & RESOLUTION
        TitledPane paneScale = new TitledPane("📐 Échelle & Voxel Sub-millimétrique", buildScaleBlock());

        // 2. SOIL & RELIEF
        TitledPane paneSoil = new TitledPane("⛰️ Relief & Substrats de Sol", buildSoilBlock());

        // 3. FLORA ECOSYSTEM
        TitledPane paneFlora = new TitledPane("🌿 Écosystème & Couvert Végétal", buildFloraBlock());

        // 4. HYDROLOGY
        TitledPane paneHydro = new TitledPane("💧 Hydrographie & Sources d'Eau", buildHydroBlock());

        // 5. VERTICAL STRUCTURES
        TitledPane paneStruct = new TitledPane("🪵 Structures en Hauteur & Hôtes", buildStructBlock());

        // 6. INITIAL NEST MODE
        TitledPane paneNest = new TitledPane("🏰 Nid Initial & Mode de Fondation", buildNestBlock());

        // 7. 3D SCULPTING BRUSHES
        TitledPane paneSculpt = new TitledPane("🖌️ Sculpture 3D (Élévation uniquement)", buildSculptBlock());

        // 8. SOL 3D : STRATIFICATION & VIDES
        TitledPane paneSoil3D = new TitledPane("🗻 Sol 3D : Stratification, Humidité & Vides", buildSoil3DBlock());

        accordion.getPanes().addAll(paneSource, paneScale, paneSoil, paneSoil3D, paneFlora, paneHydro, paneStruct, paneNest, paneSculpt);
        accordion.setExpandedPane(paneSource); // Open source block by default

        cfg.getChildren().add(accordion);

        ScrollPane sc = new ScrollPane(cfg);
        sc.setFitToWidth(true);
        sc.setPrefWidth(340);
        return sc;
    }

    private VBox buildTerrainSourceBlock() {
        useWebServiceTerrainCheck = new CheckBox("Importer depuis un service web (GeoData)");
        useWebServiceTerrainCheck.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");

        webServiceProviderCombo = new ComboBox<>();
        webServiceProviderCombo.getItems().addAll(
            "Google Earth / Maps Elevation API",
            "OpenTopography SRTM DEM (Global 30m)",
            "EU-DEM v1.1 Vegetation & Relief",
            "USGS 3DEP High Res Elevation"
        );
        webServiceProviderCombo.getSelectionModel().selectFirst();
        webServiceProviderCombo.setPrefWidth(260);

        latField = new TextField("45.1885");
        latField.setPrefWidth(120);
        lonField = new TextField("5.7245");
        lonField.setPrefWidth(120);

        HBox geoBox = new HBox(8, new Label("Lat:") {{ setStyle("-fx-text-fill: #aaa;"); }}, latField, 
                                 new Label("Lon:") {{ setStyle("-fx-text-fill: #aaa;"); }}, lonField);
        geoBox.setAlignment(Pos.CENTER_LEFT);

        VBox webOptions = new VBox(6,
            new Label("Service Web GeoData:"), webServiceProviderCombo,
            new Label("Coordonnées Géographiques:"), geoBox,
            new Label("💡 Télécharge l'élévation et la couverture végétale réelle aux coordonnées données.") {{
                setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8; -fx-wrap-text: true;");
            }}
        );
        webOptions.setDisable(true);

        useWebServiceTerrainCheck.selectedProperty().addListener((obs, oldV, newV) -> {
            webOptions.setDisable(!newV);
            regenerateAndRepaint();
        });

        return new VBox(10, useWebServiceTerrainCheck, webOptions);
    }

    private VBox buildScaleBlock() {
        surfaceSizeSlider = mkSlider(0.5, 10.0, 2.0);
        resolutionSlider = mkSlider(0.1, 1.0, 0.5);
        addLsn(surfaceSizeSlider, resolutionSlider);

        Label scaleHint = new Label("💡 Permet de simuler précisément le diamètre des galeries (3-8 mm) et le corps des fourmis (2-15 mm).");
        scaleHint.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8; -fx-wrap-text: true;");

        return new VBox(8,
                new Label("Taille de Surface (Mètres):"), sv(surfaceSizeSlider, "m"),
                new Label("Résolution Grille / Voxel:"), sv(resolutionSlider, "mm"),
                scaleHint
        );
    }

    private VBox buildSoilBlock() {
        roughnessSlider = mkSlider(0.0, 1.0, 0.45);
        compactionSlider = mkSlider(10.0, 100.0, 65.0);
        // roughness déclenche une régénération complète (affecte le heightmap)
        roughnessSlider.valueProperty().addListener((o, a, b) -> {
            // Appliquer la rugosité en perturbant le heightmap
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

        earthSpinner = mkSpinner(0, 100, 50);
        sandSpinner = mkSpinner(0, 100, 20);
        claySpinner = mkSpinner(0, 100, 20);
        stoneSpinner = mkSpinner(0, 100, 10);
        organicSpinner = mkSpinner(0, 100, 0);
        // Les spinners de composition déclenchent une regénération des couches
        for (Spinner<Integer> sp : new Spinner[]{earthSpinner, sandSpinner, claySpinner, stoneSpinner, organicSpinner})
            sp.valueProperty().addListener((o, a, b) -> regenerateAndRepaint());

        GridPane grid = new GridPane();
        grid.setHgap(8); grid.setVgap(6);
        grid.add(new Label("Terre / Humus %:"), 0, 0); grid.add(earthSpinner, 1, 0);
        grid.add(new Label("Sable (Éboulements) %:"), 0, 1); grid.add(sandSpinner, 1, 1);
        grid.add(new Label("Argile (Stabilité) %:"), 0, 2); grid.add(claySpinner, 1, 2);
        grid.add(new Label("Pierre / Gravier %:"), 0, 3); grid.add(stoneSpinner, 1, 3);
        grid.add(new Label("Litière Organique %:"), 0, 4); grid.add(organicSpinner, 1, 4);

        return new VBox(8,
                new Label("Rugosité du Relief (Bruit Perlin):"), sv(roughnessSlider, ""),
                new Label("Indice de Compaction du Sol:"), sv(compactionSlider, "%"),
                new Separator(),
                new Label("🏜️ Composition du Substrat Surface (%) :"),
                grid
        );
    }

    /** Nouveau panneau : Stratification, Humidité, Vides souterrains. */
    private VBox buildSoil3DBlock() {
        stratificationSlider = mkSlider(0.0, 1.0, 0.7);
        mixingRateSlider     = mkSlider(0.0, 1.0, 0.3);
        baseHumiditySlider   = mkSlider(0.0, 1.0, 0.35);
        voidDensitySlider    = mkSlider(0.0, 0.5, 0.08);
        showHumidityCheck    = new CheckBox("💧 Afficher overlay humidité (vue dessus)");
        showHumidityCheck.setSelected(false);

        // Tous ces sliders déclenchent une régénération complète
        for (Slider s : new Slider[]{stratificationSlider, mixingRateSlider, baseHumiditySlider, voidDensitySlider})
            s.valueProperty().addListener((o, a, b) -> regenerateAndRepaint());
        showHumidityCheck.setOnAction(e -> repaintAllViews());

        Label hint = new Label("💡 Stratification = 1 : couches nettes (humus → argile → pierre).\nStratification = 0 : mélange aléatoire.\nDensité vides = chance de cavernes en profondeur.");
        hint.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8; -fx-wrap-text: true;");

        return new VBox(8,
                new Label("🗻 Degré de Stratification:"), sv(stratificationSlider, ""),
                new Label("🌀 Taux de Mélange des Couches:"), sv(mixingRateSlider, ""),
                new Separator(),
                new Label("💧 Humidité de Base:"), sv(baseHumiditySlider, ""),
                new Separator(),
                new Label("🚫 Densité de Vides/Cavernes:"), sv(voidDensitySlider, ""),
                showHumidityCheck,
                hint
        );
    }

    private VBox buildFloraBlock() {
        edibleDensitySlider = mkSlider(0, 100, 40);
        nonEdibleDensitySlider = mkSlider(0, 100, 60);
        addLsn(edibleDensitySlider, nonEdibleDensitySlider);

        aphidPlantCheck    = new CheckBox("🟢 Cirsium / Vicia (Hôtes pucerons / miellat)"); aphidPlantCheck.setSelected(true);
        nectarFlowersCheck = new CheckBox("🌸 Fleurs à Nectar");                            nectarFlowersCheck.setSelected(true);
        seedGrassCheck     = new CheckBox("🌾 Graminées (Graines pour Messor)");              seedGrassCheck.setSelected(true);
        fungusFoliageCheck = new CheckBox("🍃 Feuillage à Champignons (Atta)");               fungusFoliageCheck.setSelected(false);
        mossCheck          = new CheckBox("🟢 Mousse Polytrichum (Rétention humidité)");      mossCheck.setSelected(true);
        pineLitterCheck    = new CheckBox("🍂 Litière d'Aiguilles de Pin");                    pineLitterCheck.setSelected(true);
        fernObstacleCheck  = new CheckBox("🌿 Fougères (Obstacles physiques)");               fernObstacleCheck.setSelected(true);

        // Connexion des listeners manquants
        addBoolLsn(aphidPlantCheck, nectarFlowersCheck, seedGrassCheck, fungusFoliageCheck,
                   mossCheck, pineLitterCheck, fernObstacleCheck);

        return new VBox(6,
                new Label("🍎 Espèces Comestibles (Ressources) :"),
                new Label("Densité :"), sv(edibleDensitySlider, "%"),
                aphidPlantCheck, nectarFlowersCheck, seedGrassCheck, fungusFoliageCheck,
                new Separator(),
                new Label("🌲 Espèces Non-Comestibles (Structure & Abri) :"),
                new Label("Densité :"), sv(nonEdibleDensitySlider, "%"),
                mossCheck, pineLitterCheck, fernObstacleCheck
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
        treeCountSlider = mkSlider(0, 5, 2);
        hollowLogsSlider = mkSlider(0, 4, 1);
        rockCrevicesSlider = mkSlider(0, 5, 3);
        addLsn(treeCountSlider, hollowLogsSlider, rockCrevicesSlider);

        Label structHint = new Label("💡 Permet l'hébergement de nids arboricoles (Crematogaster, Oecophylla) ou de nids de guêpes/abeilles sociales.");
        structHint.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8; -fx-wrap-text: true;");

        return new VBox(8,
                new Label("Nombre d'Arbres / Troncs:"), sv(treeCountSlider, ""),
                new Label("Souches de Bois Creuses (Camponotus / Nids):"), sv(hollowLogsSlider, ""),
                new Label("Fissures / Rentrées Rocheuses:"), sv(rockCrevicesSlider, ""),
                structHint
        );
    }

    private VBox buildNestBlock() {
        nestModeGroup = new ToggleGroup();
        foundingQueenRadio = new RadioButton("👑 Reine Seule (Fondation Claustrale en Sol Vierge)");
        foundingQueenRadio.setToggleGroup(nestModeGroup);
        foundingQueenRadio.setSelected(true);

        prebuiltNestRadio = new RadioButton("🏛️ Nid Pré-construit (Utiliser Algorithme de Nid)");
        prebuiltNestRadio.setToggleGroup(nestModeGroup);

        // Connexion des listeners manquants
        nestModeGroup.selectedToggleProperty().addListener((o, a, b) -> repaintAllViews());

        Label nestHint = new Label("💡 En mode Reine Seule, le sol est laissé intact pour permettre l'excavation dynamique par la reine fondatrice.");
        nestHint.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8; -fx-wrap-text: true;");

        return new VBox(10,
                foundingQueenRadio,
                prebuiltNestRadio,
                nestHint
        );
    }

    private VBox buildSculptBlock() {
        enableSculptingCheck = new CheckBox("🖌️ Activer Mode Sculpture Directe (Glisser-souris)");
        enableSculptingCheck.setSelected(false);
        enableSculptingCheck.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");
        enableSculptingCheck.setOnAction(e -> repaintAllViews());

        // Mode peinture restreint à l'élévation uniquement
        brushModeSelect = new ComboBox<>();
        brushModeSelect.getItems().addAll(
                "⛰️ RAISE_ELEVATION (Élever Terrain)",
                "⛏️ LOWER_ELEVATION (Creuser Terrain)",
                "🌊 SMOOTH (Lisser Relief)"
        );
        brushModeSelect.getSelectionModel().selectFirst();
        brushModeSelect.setPrefWidth(240);
        brushModeSelect.valueProperty().addListener((o, a, b) -> repaintAllViews());

        // brushSubstrateSelect gardé pour compatibilité mais non affiché (plus de peinture matériau)
        brushSubstrateSelect = new ComboBox<>();
        brushSubstrateSelect.getItems().addAll("Terre / Humus", "Sable", "Argile", "Pierre", "Litière Organique");
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
        HBox bar = new HBox(10);
        bar.setPadding(new Insets(4, 10, 6, 10));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #141824; -fx-border-color: #333; -fx-border-width: 1 0 0 0;");

        syncViewsCheckBox = new CheckBox("🔗 Synchroniser les vues (Zoom & Panning)");
        syncViewsCheckBox.setSelected(true);
        syncViewsCheckBox.setStyle("-fx-text-fill: #00d4ff; -fx-font-weight: bold; -fx-font-size: 11px;");

        Label title = new Label("Légende Substrats :");
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #aaa; -fx-font-size: 11px;");

        bar.getChildren().addAll(syncViewsCheckBox, new Separator(Orientation.VERTICAL), title);

        String[][] items = {
                {"Terre", "#3d2817"},
                {"Sable", "#eab308"},
                {"Argile", "#9a3412"},
                {"Pierre", "#64748b"},
                {"Rivière", "#0284c7"},
                {"Végétation", "#15803d"},
                {"Galeries/Nid", "#d97706"}
        };

        for (String[] it : items) {
            HBox item = new HBox(4);
            item.setAlignment(Pos.CENTER_LEFT);
            Canvas dot = new Canvas(9, 9);
            GraphicsContext g = dot.getGraphicsContext2D();
            g.setFill(Color.web(it[1]));
            g.fillOval(0, 0, 9, 9);
            g.setStroke(Color.WHITE);
            g.setLineWidth(0.5);
            g.strokeOval(0, 0, 9, 9);

            Label lbl = new Label(it[0]);
            lbl.setStyle("-fx-text-fill: #ccc; -fx-font-size: 10px;");
            item.getChildren().addAll(dot, lbl);
            bar.getChildren().add(item);
        }

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label hint = new Label("💡 Double-clic : réinitialiser caméras");
        hint.setStyle("-fx-text-fill: #888; -fx-font-size: 10px; -fx-font-style: italic;");

        bar.getChildren().addAll(sp, hint);
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
        bar.setPadding(new Insets(6, 10, 6, 10));
        bar.setAlignment(Pos.CENTER);
        bar.setStyle("-fx-background-color: rgba(15, 23, 42, 0.88); -fx-background-radius: 8; -fx-border-color: rgba(255, 255, 255, 0.12); -fx-border-radius: 8;");

        // Mode Toggle Button (Gamified Voxel vs Technical Engine)
        btnRenderModeToggle = new Button("🎮 Mode: Voxel Gamifié");
        btnRenderModeToggle.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand;");
        btnRenderModeToggle.setOnAction(e -> {
            gamifiedVoxelMode = !gamifiedVoxelMode;
            btnRenderModeToggle.setText(gamifiedVoxelMode ? "🎮 Mode: Voxel Gamifié" : "🔲 Mode: Technique Grid");
            btnRenderModeToggle.setStyle(gamifiedVoxelMode ?
                "-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand;" :
                "-fx-background-color: #334155; -fx-text-fill: #cbd5e1; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand;");
            repaintAllViews();
        });

        Separator sep = new Separator(Orientation.VERTICAL);

        String[][] tools = {
            {"BRUSH", "🖌️ Brush"},
            {"PLACE", "🏗️ Place"},
            {"ERASE", "🧹 Erase"},
            {"SMOOTH", "🌊 Smooth"},
            {"SELECT", "🔲 Select"},
            {"VIEW", "👁️ View"},
            {"ORBIT", "🔄 Orbit"},
            {"PAN", "✋ Pan"},
            {"ZOOM", "🔍 Zoom"}
        };

        List<Button> toolBtns = new ArrayList<>();
        for (String[] t : tools) {
            Button btn = new Button(t[1]);
            boolean isActive = t[0].equals(activeViewportTool);
            btn.setStyle(isActive ?
                "-fx-background-color: #38bdf8; -fx-text-fill: #090d16; -fx-font-weight: bold; -fx-font-size: 10px; -fx-cursor: hand;" :
                "-fx-background-color: rgba(255, 255, 255, 0.06); -fx-text-fill: #e2e8f0; -fx-font-size: 10px; -fx-cursor: hand;");
            btn.setOnAction(e -> {
                activeViewportTool = t[0];
                for (Button b : toolBtns) {
                    boolean act = b.getText().toUpperCase().contains(activeViewportTool);
                    b.setStyle(act ?
                        "-fx-background-color: #38bdf8; -fx-text-fill: #090d16; -fx-font-weight: bold; -fx-font-size: 10px; -fx-cursor: hand;" :
                        "-fx-background-color: rgba(255, 255, 255, 0.06); -fx-text-fill: #e2e8f0; -fx-font-size: 10px; -fx-cursor: hand;");
                }
                repaintAllViews();
            });
            toolBtns.add(btn);
        }

        bar.getChildren().add(btnRenderModeToggle);
        bar.getChildren().add(sep);
        bar.getChildren().addAll(toolBtns);

        return bar;
    }

    private void draw3D() {
        if (gamifiedVoxelMode) {
            draw3DGamified();
        } else {
            draw3DTechnical();
        }
    }

    private void draw3DGamified() {
        double w = canvas3D.getWidth();
        double h = canvas3D.getHeight();

        // 1. Dark Gradient Atmosphere Backdrop
        gc3D.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#0a0f1d")),
            new Stop(1, Color.web("#141e33"))));
        gc3D.fillRect(0, 0, w, h);

        double radAz = Math.toRadians(azimuth);
        double radEl = Math.toRadians(elevation);

        double cx = w / 2 + pan3DX;
        double cy = h / 2 + pan3DY + 50;
        double scale = zoom * 12.0;

        // 2. Render Stylized Isometric 3D Voxel Cubes with Top/Left/Right Shading
        int step = 2;
        double blockW = scale * 0.16;
        double blockH = scale * 0.10;

        for (int x = 0; x < GRID_SIZE - step; x += step) {
            for (int y = 0; y < GRID_SIZE - step; y += step) {
                double z = heightGrid[x][y] * 45.0;
                if (carvedVoxelGrid[x][y]) z -= 25.0;

                double isoX = (x - GRID_SIZE / 2.0) * Math.cos(radAz) - (y - GRID_SIZE / 2.0) * Math.sin(radAz);
                double isoY = (x - GRID_SIZE / 2.0) * Math.sin(radAz) * Math.sin(radEl) + (y - GRID_SIZE / 2.0) * Math.cos(radAz) * Math.sin(radEl) - z * Math.cos(radEl);

                double px = cx + isoX * (scale / 10.0);
                double py = cy + isoY * (scale / 10.0);

                Color topColor = getMaterialColor(soilLayers[x][y][0]);
                if (soilLayers[x][y][0] == 0) topColor = Color.web("#22c55e"); // Grass / Humus
                if (carvedVoxelGrid[x][y]) topColor = Color.web("#d97706"); // Gallery interior glow

                // Top Face (Isometric Diamond Polygon)
                double[] xPoints = { px, px + blockW / 2, px, px - blockW / 2 };
                double[] yPoints = { py - blockH / 2, py, py + blockH / 2, py };

                gc3D.setFill(topColor);
                gc3D.fillPolygon(xPoints, yPoints, 4);

                // Side Shading for Voxel Height Blocks
                double fillH = Math.max(2, z * 0.35);
                // Left Shadow Face
                double[] xLeft = { px - blockW / 2, px, px, px - blockW / 2 };
                double[] yLeft = { py, py + blockH / 2, py + blockH / 2 + fillH, py + fillH };
                gc3D.setFill(topColor.darker().darker());
                gc3D.fillPolygon(xLeft, yLeft, 4);

                // Right Ambient Light Face
                double[] xRight = { px, px + blockW / 2, px + blockW / 2, px };
                double[] yRight = { py + blockH / 2, py, py + fillH, py + blockH / 2 + fillH };
                gc3D.setFill(topColor.darker());
                gc3D.fillPolygon(xRight, yRight, 4);

                // Outline Grid Edges
                gc3D.setStroke(Color.rgb(0, 0, 0, 0.15));
                gc3D.setLineWidth(0.5);
                gc3D.strokePolygon(xPoints, yPoints, 4);
            }
        }

        // 3. Subterranean Strata Cutaway Front & Side Wall (Underground Ant Nest Cutaway View)
        renderSubterraneanCutawayWall(cx, cy, scale, radAz, radEl);

        // 4. Draw River Path
        if (riverCheck != null && riverCheck.isSelected() && riverPath != null && riverPath.size() > 1) {
            gc3D.setStroke(Color.web("#38bdf8"));
            gc3D.setLineWidth(riverWidthSlider.getValue() / 15.0 * (zoom / 7.5));
            gc3D.beginPath();
            boolean first = true;
            for (int[] pt : riverPath) {
                int rx = pt[0], ry = pt[1];
                double rz = heightGrid[rx][ry] * 45.0 + 3.0;
                double isoX = (rx - GRID_SIZE / 2.0) * Math.cos(radAz) - (ry - GRID_SIZE / 2.0) * Math.sin(radAz);
                double isoY = (rx - GRID_SIZE / 2.0) * Math.sin(radAz) * Math.sin(radEl) + (ry - GRID_SIZE / 2.0) * Math.cos(radAz) * Math.sin(radEl) - rz * Math.cos(radEl);
                double px = cx + isoX * (scale / 10.0);
                double py = cy + isoY * (scale / 10.0);
                if (first) { gc3D.moveTo(px, py); first = false; }
                else gc3D.lineTo(px, py);
            }
            gc3D.stroke();
        }

        // 5. Draw 3D Voxel Trees, Fungi & Ant Agents
        drawVoxelEcosystemElements(cx, cy, scale, radAz, radEl);

        // 6. Draw Radar Inset Mini-Map (Bottom Right Overlay)
        drawRadarMiniMap(w, h);

        // 7. HUD Title Label
        gc3D.setFill(Color.web("#38bdf8"));
        gc3D.setFont(Font.font("SansSerif", 11));
        gc3D.fillText("🎮 Vue Voxel 3D Gamifiée (Az: " + (int) azimuth + "°, El: " + (int) elevation + "°, Zoom: " + String.format("%.1f", zoom) + "x)", 15, 25);
    }

    private void renderSubterraneanCutawayWall(double cx, double cy, double scale, double radAz, double radEl) {
        int edgeY = GRID_SIZE - 4;
        for (int x = 0; x < GRID_SIZE - 4; x += 3) {
            double zTop = heightGrid[x][edgeY] * 45.0;
            double isoX = (x - GRID_SIZE / 2.0) * Math.cos(radAz) - (edgeY - GRID_SIZE / 2.0) * Math.sin(radAz);
            double isoY = (x - GRID_SIZE / 2.0) * Math.sin(radAz) * Math.sin(radEl) + (edgeY - GRID_SIZE / 2.0) * Math.cos(radAz) * Math.sin(radEl) - zTop * Math.cos(radEl);
            double px = cx + isoX * (scale / 10.0);
            double py = cy + isoY * (scale / 10.0);

            double wallH = 65.0 * (scale / 70.0);
            // Topsoil slice
            gc3D.setFill(Color.web("#451a03"));
            gc3D.fillRect(px - 3, py, 6, wallH * 0.15);
            // Subsoil slice
            gc3D.setFill(Color.web("#78350f"));
            gc3D.fillRect(px - 3, py + wallH * 0.15, 6, wallH * 0.35);
            // Clay slice
            gc3D.setFill(Color.web("#b45309"));
            gc3D.fillRect(px - 3, py + wallH * 0.50, 6, wallH * 0.25);
            // Bedrock slice
            gc3D.setFill(Color.web("#334155"));
            gc3D.fillRect(px - 3, py + wallH * 0.75, 6, wallH * 0.25);

            // Cavern Chamber / Gallery Openings
            if (carvedVoxelGrid[x][edgeY] || (x % 18 == 0)) {
                gc3D.setFill(Color.web("#1e1b4b"));
                gc3D.fillOval(px - 7, py + wallH * 0.35, 14, 10);
                gc3D.setStroke(Color.web("#f59e0b"));
                gc3D.setLineWidth(1);
                gc3D.strokeOval(px - 7, py + wallH * 0.35, 14, 10);

                // Ant silhouettes inside cavern
                gc3D.setFill(Color.web("#ef4444"));
                gc3D.fillOval(px - 2, py + wallH * 0.35 + 2, 4, 3);
            }
        }
    }

    private void drawVoxelEcosystemElements(double cx, double cy, double scale, double radAz, double radEl) {
        Random rand = new Random(88);
        int treeCount = treeCountSlider != null ? (int) treeCountSlider.getValue() : 12;

        for (int i = 0; i < treeCount; i++) {
            int gx = 12 + (int)(rand.nextDouble() * (GRID_SIZE - 24));
            int gy = 12 + (int)(rand.nextDouble() * (GRID_SIZE - 24));
            double z = heightGrid[gx][gy] * 45.0;

            double isoX = (gx - GRID_SIZE / 2.0) * Math.cos(radAz) - (gy - GRID_SIZE / 2.0) * Math.sin(radAz);
            double isoY = (gx - GRID_SIZE / 2.0) * Math.sin(radAz) * Math.sin(radEl) + (gy - GRID_SIZE / 2.0) * Math.cos(radAz) * Math.sin(radEl) - z * Math.cos(radEl);

            double px = cx + isoX * (scale / 10.0);
            double py = cy + isoY * (scale / 10.0);

            // Voxel Tree Trunk
            gc3D.setFill(Color.web("#78350f"));
            gc3D.fillRect(px - 2, py - 22, 4, 22);

            // Voxel Leaf Sphere / Canopy
            gc3D.setFill(Color.web("#15803d"));
            gc3D.fillOval(px - 12, py - 42, 24, 24);
            gc3D.setFill(Color.web("#22c55e"));
            gc3D.fillOval(px - 9, py - 40, 14, 14);

            // Voxel Mushrooms nearby
            if (i % 3 == 0) {
                gc3D.setFill(Color.web("#ef4444"));
                gc3D.fillOval(px + 8, py - 6, 7, 5);
                gc3D.setFill(Color.web("#f8fafc"));
                gc3D.fillRect(px + 10, py - 3, 3, 4);
            }
        }

        // Ant Foraging Colony Agents walking on terrain
        for (int a = 0; a < 8; a++) {
            int ax = 30 + (a * 9) % (GRID_SIZE - 60);
            int ay = 40 + (a * 13) % (GRID_SIZE - 60);
            double az = heightGrid[ax][ay] * 45.0 + 1.0;

            double isoX = (ax - GRID_SIZE / 2.0) * Math.cos(radAz) - (ay - GRID_SIZE / 2.0) * Math.sin(radAz);
            double isoY = (ax - GRID_SIZE / 2.0) * Math.sin(radAz) * Math.sin(radEl) + (ay - GRID_SIZE / 2.0) * Math.cos(radAz) * Math.sin(radEl) - az * Math.cos(radEl);

            double px = cx + isoX * (scale / 10.0);
            double py = cy + isoY * (scale / 10.0);

            gc3D.setFill(Color.web("#ef4444"));
            gc3D.fillOval(px - 2, py - 2, 5, 3);
            gc3D.setFill(Color.web("#7f1d1d"));
            gc3D.fillOval(px + 2, py - 2, 2, 2);
        }
    }

    private void drawRadarMiniMap(double w, double h) {
        double mw = 135, mh = 135;
        double mx = w - mw - 12, my = h - mh - 12;

        // Mini-map background card (Glassmorphism inset)
        gc3D.setFill(Color.rgb(15, 23, 42, 0.88));
        gc3D.fillRoundRect(mx, my, mw, mh, 10, 10);
        gc3D.setStroke(Color.rgb(56, 189, 248, 0.6));
        gc3D.setLineWidth(1.2);
        gc3D.strokeRoundRect(mx, my, mw, mh, 10, 10);

        // Header label
        gc3D.setFill(Color.web("#38bdf8"));
        gc3D.setFont(Font.font("SansSerif", 9));
        gc3D.fillText("🗺️ Radar Mini-Map", mx + 8, my + 14);

        // Mini heightmap thumbnail
        double mapAreaX = mx + 8, mapAreaY = my + 20, mapAreaSize = 108;
        double step = mapAreaSize / GRID_SIZE;

        for (int x = 0; x < GRID_SIZE; x += 4) {
            for (int y = 0; y < GRID_SIZE; y += 4) {
                double val = heightGrid[x][y];
                Color c = Color.hsb(110 + val * 60, 0.7, 0.4 + val * 0.5);
                gc3D.setFill(c);
                gc3D.fillRect(mapAreaX + x * step, mapAreaY + y * step, step * 4, step * 4);
            }
        }

        // Render River path on mini-map
        if (riverPath != null && riverPath.size() > 1) {
            gc3D.setStroke(Color.web("#0284c7"));
            gc3D.setLineWidth(1.5);
            gc3D.beginPath();
            boolean first = true;
            for (int[] pt : riverPath) {
                double px = mapAreaX + pt[0] * step;
                double py = mapAreaY + pt[1] * step;
                if (first) { gc3D.moveTo(px, py); first = false; }
                else gc3D.lineTo(px, py);
            }
            gc3D.stroke();
        }

        // Nest origin marker
        gc3D.setFill(Color.web("#ef4444"));
        gc3D.fillOval(mapAreaX + mapAreaSize / 2 - 3, mapAreaY + mapAreaSize / 2 - 3, 6, 6);
        gc3D.setStroke(Color.WHITE);
        gc3D.setLineWidth(1);
        gc3D.strokeOval(mapAreaX + mapAreaSize / 2 - 3, mapAreaY + mapAreaSize / 2 - 3, 6, 6);

        // Camera Direction Pointer / Frustum Line
        double radAz = Math.toRadians(azimuth);
        double viewX = mapAreaX + mapAreaSize / 2 + Math.cos(radAz) * 18;
        double viewY = mapAreaY + mapAreaSize / 2 + Math.sin(radAz) * 18;
        gc3D.setStroke(Color.web("#facc15"));
        gc3D.setLineWidth(1.5);
        gc3D.strokeLine(mapAreaX + mapAreaSize / 2, mapAreaY + mapAreaSize / 2, viewX, viewY);
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

        // Render Voxel Heightmap & Substrates in 3D Grid
        int step = 2;
        for (int x = 0; x < GRID_SIZE - step; x += step) {
            for (int y = 0; y < GRID_SIZE - step; y += step) {
                double z = heightGrid[x][y] * 40.0;
                if (carvedVoxelGrid[x][y]) z -= 20.0; // Carved gallery depth

                // 3D Isometric projection
                double isoX = (x - GRID_SIZE / 2.0) * Math.cos(radAz) - (y - GRID_SIZE / 2.0) * Math.sin(radAz);
                double isoY = (x - GRID_SIZE / 2.0) * Math.sin(radAz) * Math.sin(radEl) + (y - GRID_SIZE / 2.0) * Math.cos(radAz) * Math.sin(radEl) - z * Math.cos(radEl);

                double px = cx + isoX * (scale / 10.0);
                double py = cy + isoY * (scale / 10.0);

                Color col = getMaterialColor(soilLayers[x][y][0]);
                if (carvedVoxelGrid[x][y]) col = Color.web("#d97706"); // Gallery color

                gc3D.setFill(col);
                gc3D.fillRect(px, py, scale / 8.0, scale / 8.0);
            }
        }

        // Draw River along riverPath (suivi réel de la pente)
        if (riverCheck != null && riverCheck.isSelected() && riverPath != null && riverPath.size() > 1) {
            gc3D.setStroke(Color.web("#0284c7"));
            gc3D.setLineWidth(riverWidthSlider.getValue() / 18.0 * (zoom / 7.5));
            gc3D.beginPath();
            boolean first = true;
            for (int[] pt : riverPath) {
                int rx = pt[0], ry = pt[1];
                double rz = heightGrid[rx][ry] * 40.0 + 2.0; // légèrement au-dessus du sol
                double isoX = (rx - GRID_SIZE / 2.0) * Math.cos(radAz) - (ry - GRID_SIZE / 2.0) * Math.sin(radAz);
                double isoY = (rx - GRID_SIZE / 2.0) * Math.sin(radAz) * Math.sin(radEl) + (ry - GRID_SIZE / 2.0) * Math.cos(radAz) * Math.sin(radEl) - rz * Math.cos(radEl);
                double px = cx + isoX * (scale / 10.0);
                double py = cy + isoY * (scale / 10.0);
                if (first) { gc3D.moveTo(px, py); first = false; }
                else gc3D.lineTo(px, py);
            }
            gc3D.stroke();
        }

        // Draw Trees in 3D Viewport (ancrés au relief du sol)
        if (treeCountSlider != null) {
            int count = (int) treeCountSlider.getValue();
            Random rand = new Random(77);
            for (int i = 0; i < count; i++) {
                int gx = 10 + (int)(rand.nextDouble() * (GRID_SIZE - 20));
                int gy = 10 + (int)(rand.nextDouble() * (GRID_SIZE - 20));
                double z = heightGrid[gx][gy] * 40.0;

                double isoX = (gx - GRID_SIZE / 2.0) * Math.cos(radAz) - (gy - GRID_SIZE / 2.0) * Math.sin(radAz);
                double isoY = (gx - GRID_SIZE / 2.0) * Math.sin(radAz) * Math.sin(radEl) + (gy - GRID_SIZE / 2.0) * Math.cos(radAz) * Math.sin(radEl) - z * Math.cos(radEl);

                double px = cx + isoX * (scale / 10.0);
                double py = cy + isoY * (scale / 10.0);

                // Tronc ancré au sol
                gc3D.setFill(Color.web("#78350f"));
                gc3D.fillRect(px - 3, py - 25, 6, 25);
                // Houppier
                gc3D.setFill(Color.web("#166534"));
                gc3D.fillOval(px - 14, py - 45, 28, 26);
            }
        }

        // Label Overlay
        gc3D.setFill(Color.WHITE);
        gc3D.fillText("Vue 3D (Az: " + (int) azimuth + "°, El: " + (int) elevation + "°, Zoom: " + String.format("%.1f", zoom) + "x)", 15, 25);
    }

    private void drawSide() {
        double w = canvasSide.getWidth();
        double h = canvasSide.getHeight();

        gcSide.setFill(Color.web("#0f172a"));
        gcSide.fillRect(0, 0, w, h);

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

                if (voidGrid[x][midY][d]) {
                    // Vides / cavernes
                    gcSide.setFill(Color.web("#0f172a"));
                } else {
                    gcSide.setFill(getMaterialColor(soilLayers[x][midY][d]));
                }
                gcSide.fillRect(px, py, Math.max(1, blockW), Math.max(1, blockH));
            }
        }

        // Nappe Phréatique
        double wtDepth = waterTableDepthSlider != null ? waterTableDepthSlider.getValue() : 15;
        double wtY = 80 + (wtDepth / 50.0) * (h - 100) * sZoom + cy;
        gcSide.setFill(Color.web("#0284c7"));
        gcSide.setGlobalAlpha(0.45);
        gcSide.fillRect(10 + cx, wtY, (w - 20) * sZoom, (h - 20 - wtY));
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

        gcSide.setStroke(Color.web("#38bdf8"));
        gcSide.setLineWidth(1.0);
        gcSide.strokeRect(10, 10, w - 20, h - 20);

        gcSide.setFill(Color.WHITE);
        gcSide.fillText("Profil Stratigraphique & Vides (Nappe: " + String.format("%.0f", wtDepth) + "cm)", 20, 30);
    }

    private void drawTop() {
        double w = canvasTop.getWidth();
        double h = canvasTop.getHeight();

        gcTop.setFill(Color.web("#0f172a"));
        gcTop.fillRect(0, 0, w, h);

        double tZoom = topZoom;
        double cx = topPanX;
        double cy = topPanY;

        double cellW = ((w - 30) / GRID_SIZE) * tZoom;
        double cellH = ((h - 30) / GRID_SIZE) * tZoom;

        // Sol en Surface
        for (int x = 0; x < GRID_SIZE; x += 2) {
            for (int y = 0; y < GRID_SIZE; y += 2) {
                Color col = getMaterialColor(soilLayers[x][y][0]);
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

        // Dessin des arbres en vue Top-Down (ancrés aux cellules)
        if (treeCountSlider != null) {
            int count = (int) treeCountSlider.getValue();
            Random rand = new Random(77);
            for (int i = 0; i < count; i++) {
                int gx = 10 + (int)(rand.nextDouble() * (GRID_SIZE - 20));
                int gy = 10 + (int)(rand.nextDouble() * (GRID_SIZE - 20));
                double px = 15 + gx * cellW + cx;
                double py = 15 + gy * cellH + cy;
                gcTop.setFill(Color.web("#166534"));
                gcTop.fillOval(px - 6, py - 6, 12, 12);
                gcTop.setFill(Color.web("#15803d"));
                gcTop.fillOval(px - 3, py - 3, 6, 6);
            }
        }

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
        cfg.put("nestMode", foundingQueenRadio.isSelected() ? "FOUNDING_QUEEN" : "PREBUILT");
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
            if (cfg.containsKey("surfaceSizeMeters")) surfaceSizeSlider.setValue(((Number) cfg.get("surfaceSizeMeters")).doubleValue());
            if (cfg.containsKey("resolutionMm")) resolutionSlider.setValue(((Number) cfg.get("resolutionMm")).doubleValue());
            if (cfg.containsKey("roughness")) roughnessSlider.setValue(((Number) cfg.get("roughness")).doubleValue());
            repaintAllViews();
            new Alert(Alert.AlertType.INFORMATION, "Preset de monde chargé.").show();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Erreur d'importation: " + ex.getMessage()).show();
        }
    }
}
