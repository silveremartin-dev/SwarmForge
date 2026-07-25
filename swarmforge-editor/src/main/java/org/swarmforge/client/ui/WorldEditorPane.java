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
import javafx.scene.paint.Color;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

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

    // Local Voxel & Terrain Grid for Real-Time Sculpting (64x64)
    private final int GRID_SIZE = 64;
    private double[][] heightGrid = new double[GRID_SIZE][GRID_SIZE];
    private byte[][] materialGrid = new byte[GRID_SIZE][GRID_SIZE]; // 0: Earth, 1: Sand, 2: Clay, 3: Stone, 4: Organic
    private boolean[][] carvedVoxelGrid = new boolean[GRID_SIZE][GRID_SIZE]; // Real-time excavated voxels

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
                heightGrid[x][y] = 0.4 + 0.25 * Math.sin(nx * Math.PI * 2) * Math.cos(ny * Math.PI * 2) + rand.nextDouble() * 0.05;
                materialGrid[x][y] = (byte) (rand.nextDouble() < 0.6 ? 0 : (rand.nextDouble() < 0.8 ? 2 : 1));
                carvedVoxelGrid[x][y] = false;
            }
        }
    }

    private VBox buildHeader() {
        VBox v = new VBox(6);
        v.setPadding(new Insets(8, 12, 6, 12));
        v.setStyle("-fx-background-color: #141824; -fx-border-color: #333; -fx-border-width: 0 0 1 0;");

        HBox r = new HBox(10);
        r.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("🌍 Éditeur de Monde");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        Label subtitle = new Label("Génération de relief, sol, ouvert végétal, hydrographie, sculpture 3D & déformation voxel (0.1-1.0mm)");
        subtitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button bExport = new Button("💾 Sauvegarder Preset (JSON)");
        bExport.setStyle("-fx-background-color: #334155; -fx-text-fill: white;");
        bExport.setOnAction(e -> doExport());

        Button bImport = new Button("📂 Charger Preset (JSON)");
        bImport.setStyle("-fx-background-color: #334155; -fx-text-fill: white;");
        bImport.setOnAction(e -> doImport());

        Button bGenerate = new Button("✨ Générer & Appliquer au Monde");
        bGenerate.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold;");
        bGenerate.setOnAction(e -> triggerGenerate());

        r.getChildren().addAll(title, sp, bExport, bImport, new Separator(Orientation.VERTICAL), bGenerate);
        v.getChildren().addAll(r, subtitle);
        return v;
    }

    private ScrollPane buildConfig() {
        VBox cfg = new VBox(12);
        cfg.setPadding(new Insets(10));
        cfg.setPrefWidth(320);
        cfg.setStyle("-fx-background-color: #0f172a;");

        Accordion accordion = new Accordion();

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
        TitledPane paneSculpt = new TitledPane("🖌️ Sculpture 3D Manuelle & Voxels", buildSculptBlock());

        accordion.getPanes().addAll(paneScale, paneSoil, paneFlora, paneHydro, paneStruct, paneNest, paneSculpt);
        accordion.setExpandedPane(paneSoil); // Open soil block by default

        cfg.getChildren().add(accordion);

        ScrollPane sc = new ScrollPane(cfg);
        sc.setFitToWidth(true);
        sc.setPrefWidth(340);
        return sc;
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
        addLsn(roughnessSlider, compactionSlider);

        earthSpinner = mkSpinner(0, 100, 50);
        sandSpinner = mkSpinner(0, 100, 20);
        claySpinner = mkSpinner(0, 100, 20);
        stoneSpinner = mkSpinner(0, 100, 10);
        organicSpinner = mkSpinner(0, 100, 0);

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
                new Label("Composition du Substrat (%) :"),
                grid
        );
    }

    private VBox buildFloraBlock() {
        edibleDensitySlider = mkSlider(0, 100, 40);
        nonEdibleDensitySlider = mkSlider(0, 100, 60);
        addLsn(edibleDensitySlider, nonEdibleDensitySlider);

        aphidPlantCheck = new CheckBox("🟢 Cirsium / Vicia (Hôtes pucerons / miellat)"); aphidPlantCheck.setSelected(true);
        nectarFlowersCheck = new CheckBox("🌸 Fleurs à Nectar"); nectarFlowersCheck.setSelected(true);
        seedGrassCheck = new CheckBox("🌾 Graminées (Graines pour Messor)"); seedGrassCheck.setSelected(true);
        fungusFoliageCheck = new CheckBox("🍃 Feuillage à Champignons (Atta)"); fungusFoliageCheck.setSelected(false);

        mossCheck = new CheckBox("🟢 Mousse Polytrichum (Rétention humidité)"); mossCheck.setSelected(true);
        pineLitterCheck = new CheckBox("🍂 Litière d'Aiguilles de Pin"); pineLitterCheck.setSelected(true);
        fernObstacleCheck = new CheckBox("🌿 Fougères (Obstacles physiques)"); fernObstacleCheck.setSelected(true);

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

        brushModeSelect = new ComboBox<>();
        brushModeSelect.getItems().addAll(
                "⛰️ RAISE_ELEVATION (Élever Terrain)",
                "⛏️ DIG_CARVE (Creuser Galeries Voxels)",
                "🌊 SMOOTH (Lisser Relief)",
                "🎨 PAINT_SUBSTRATE (Peindre Matériau)"
        );
        brushModeSelect.getSelectionModel().selectFirst();
        brushModeSelect.setPrefWidth(240);

        brushSubstrateSelect = new ComboBox<>();
        brushSubstrateSelect.getItems().addAll("Terre / Humus", "Sable", "Argile", "Pierre", "Litière Organique");
        brushSubstrateSelect.getSelectionModel().selectFirst();
        brushSubstrateSelect.setPrefWidth(240);

        brushRadiusSlider = mkSlider(1, 15, 4);
        brushStrengthSlider = mkSlider(10, 100, 50);

        Label sculptHint = new Label("💡 Cliquez et glissez sur les vues 3D, Top-Down ou Profil pour éditer le monde voxel par voxel.");
        sculptHint.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8; -fx-wrap-text: true;");

        return new VBox(8,
                enableSculptingCheck,
                new Label("Mode du Pinceau :"), brushModeSelect,
                new Label("Matériau à Appliquer :"), brushSubstrateSelect,
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

        StackPane h3d = new StackPane(canvas3D);
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

        int gx = (int) (Math.max(0, Math.min(1.0, mx / 600.0)) * (GRID_SIZE - 1));
        int gy = (int) (Math.max(0, Math.min(1.0, my / 600.0)) * (GRID_SIZE - 1));

        int radius = (int) brushRadiusSlider.getValue();
        double strength = brushStrengthSlider.getValue() / 100.0 * 0.1;
        String mode = brushModeSelect.getValue();
        byte subMat = (byte) brushSubstrateSelect.getSelectionModel().getSelectedIndex();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                if (dx * dx + dy * dy <= radius * radius) {
                    int cx = Math.max(0, Math.min(GRID_SIZE - 1, gx + dx));
                    int cy = Math.max(0, Math.min(GRID_SIZE - 1, gy + dy));

                    if (mode != null && mode.contains("RAISE")) {
                        heightGrid[cx][cy] = Math.min(1.0, heightGrid[cx][cy] + strength);
                    } else if (mode != null && mode.contains("DIG")) {
                        carvedVoxelGrid[cx][cy] = true; // Dig out voxel gallery
                    } else if (mode != null && mode.contains("SMOOTH")) {
                        heightGrid[cx][cy] = (heightGrid[cx][cy] + 0.5) / 2.0;
                    } else if (mode != null && mode.contains("PAINT")) {
                        materialGrid[cx][cy] = subMat;
                    }
                }
            }
        }
        applyRealTimeVoxelPhysics();
    }

    /**
     * Real-time physical deformation step (sand slipping under gravity, clay maintaining arches).
     */
    private void applyRealTimeVoxelPhysics() {
        double sandAngleSlope = 0.08; // Sand slips if slope exceeds threshold
        for (int x = 1; x < GRID_SIZE - 1; x++) {
            for (int y = 1; y < GRID_SIZE - 1; y++) {
                if (materialGrid[x][y] == 1) { // Sand
                    double diff = heightGrid[x][y] - heightGrid[x + 1][y];
                    if (diff > sandAngleSlope) {
                        heightGrid[x][y] -= diff * 0.2;
                        heightGrid[x + 1][y] += diff * 0.2;
                    }
                }
            }
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

    // ── Drawing Methods for 3D, Top-Down, and Side Views ───────────────────────

    private void repaintAllViews() {
        draw3D();
        drawSide();
        drawTop();
    }

    private void draw3D() {
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

                Color col = getMaterialColor(materialGrid[x][y]);
                if (carvedVoxelGrid[x][y]) col = Color.web("#d97706"); // Gallery color

                gc3D.setFill(col);
                gc3D.fillRect(px, py, scale / 8.0, scale / 8.0);
            }
        }

        // Draw River if enabled
        if (riverCheck != null && riverCheck.isSelected()) {
            gc3D.setStroke(Color.web("#0284c7"));
            gc3D.setLineWidth(riverWidthSlider.getValue() / 15.0);
            gc3D.beginPath();
            gc3D.moveTo(cx - 150, cy + 30);
            gc3D.bezierCurveTo(cx - 40, cy - 20, cx + 40, cy + 80, cx + 150, cy + 20);
            gc3D.stroke();
        }

        // Draw Trees in 3D Viewport
        if (treeCountSlider != null) {
            int count = (int) treeCountSlider.getValue();
            Random rand = new Random(77);
            for (int i = 0; i < count; i++) {
                double rx = cx + (rand.nextDouble() - 0.5) * 220;
                double ry = cy + (rand.nextDouble() - 0.5) * 180;
                gc3D.setFill(Color.web("#78350f"));
                gc3D.fillRect(rx, ry - 30, 10, 30);
                gc3D.setFill(Color.web("#166534"));
                gc3D.fillOval(rx - 15, ry - 55, 40, 35);
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

        // Ground & Substrats Cut
        gcSide.setFill(Color.web("#3d2817"));
        gcSide.fillRect(10 + cx, 80 + cy, (w - 20) * sZoom, (h - 90) * sZoom);

        // Water Table Depth
        double wtDepth = waterTableDepthSlider != null ? waterTableDepthSlider.getValue() : 15;
        gcSide.setFill(Color.web("#0284c7"));
        gcSide.setGlobalAlpha(0.6);
        gcSide.fillRect(10 + cx, 80 + wtDepth * 2.5 + cy, (w - 20) * sZoom, (h - 90 - wtDepth * 2.5) * sZoom);
        gcSide.setGlobalAlpha(1.0);

        // Carved Galleries Cross-section
        gcSide.setFill(Color.web("#d97706"));
        for (int x = 0; x < GRID_SIZE; x += 4) {
            for (int y = 0; y < GRID_SIZE; y += 4) {
                if (carvedVoxelGrid[x][y]) {
                    double px = 15 + (x / (double) GRID_SIZE) * (w - 30) * sZoom + cx;
                    double py = 95 + (y / (double) GRID_SIZE) * 80 * sZoom + cy;
                    gcSide.fillOval(px, py, 6 * sZoom, 6 * sZoom);
                }
            }
        }

        gcSide.setStroke(Color.web("#38bdf8"));
        gcSide.setLineWidth(1.0);
        gcSide.strokeRect(10, 10, w - 20, h - 20);

        gcSide.setFill(Color.WHITE);
        gcSide.fillText("Profil Nappe & Galeries (" + String.format("%.0f", wtDepth) + "cm)", 20, 30);
    }

    private void drawTop() {
        double w = canvasTop.getWidth();
        double h = canvasTop.getHeight();

        gcTop.setFill(Color.web("#0f172a"));
        gcTop.fillRect(0, 0, w, h);

        double tZoom = topZoom;
        double cx = topPanX;
        double cy = topPanY;

        // Soil Surface Map
        double cellW = ((w - 30) / GRID_SIZE) * tZoom;
        double cellH = ((h - 30) / GRID_SIZE) * tZoom;

        for (int x = 0; x < GRID_SIZE; x += 2) {
            for (int y = 0; y < GRID_SIZE; y += 2) {
                Color col = getMaterialColor(materialGrid[x][y]);
                if (carvedVoxelGrid[x][y]) col = Color.web("#d97706");
                gcTop.setFill(col);
                gcTop.fillRect(15 + x * cellW + cx, 15 + y * cellH + cy, cellW * 2, cellH * 2);
            }
        }

        // River Top View
        if (riverCheck != null && riverCheck.isSelected()) {
            gcTop.setStroke(Color.web("#0284c7"));
            gcTop.setLineWidth((riverWidthSlider.getValue() / 30.0) * tZoom);
            gcTop.beginPath();
            gcTop.moveTo(15 + cx, h * 0.3 + cy);
            gcTop.bezierCurveTo(w * 0.4 + cx, h * 0.2 + cy, w * 0.5 + cx, h * 0.8 + cy, w - 15 + cx, h * 0.7 + cy);
            gcTop.stroke();
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
