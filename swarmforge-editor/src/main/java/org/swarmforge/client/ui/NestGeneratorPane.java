/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

import java.util.*;

/**
 * Nest Generator - Configure and preview realistic ant nest structures.
 * Supports 2D Side View, 2D Top View, and 3D View with interactive rotation
 * and biologically inspired tunnel generation algorithms.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class NestGeneratorPane extends BorderPane {

    // Nest Configuration Controls
    private ComboBox<String> nestTypeSelect;
    private Slider depthSlider;
    private Slider chamberCountSlider;
    private Slider tunnelWidthSlider;
    private Slider branchingSlider;

    // View Mode & 3D Controls
    private ComboBox<String> viewModeSelect;
    private Slider azimuthSlider;
    private Slider elevationSlider;
    private HBox controls3DBox;

    // Chamber Counts by Type
    private final Map<String, Spinner<Integer>> chamberSpinners = new HashMap<>();

    // Preview Canvas
    private final Canvas previewCanvas;
    private final GraphicsContext gc;

    // Mouse Interaction for 3D Orbiting
    private double lastMouseX, lastMouseY;

    // Generated Biological Nest Model
    private GeneratedNest currentNest;

    public NestGeneratorPane() {
        setPadding(new Insets(15));
        // Note: Top-level background style removed to use application default theme wallpaper

        // Top Header Bar: Title, Action Buttons, and View Mode Selectors (placed at the top for accessibility)
        VBox topHeader = createTopHeaderBar();
        setTop(topHeader);

        // Left Side: Configuration Panel
        VBox configPane = createConfigPane();
        setLeft(configPane);

        // Center: Canvas Preview Holder
        previewCanvas = new Canvas(520, 520);
        gc = previewCanvas.getGraphicsContext2D();

        setupMouseInteraction();

        VBox previewBox = new VBox(10);
        previewBox.setPadding(new Insets(10));
        previewBox.setAlignment(Pos.TOP_CENTER);

        StackPane canvasHolder = new StackPane(previewCanvas);
        canvasHolder.setStyle("-fx-border-color: #444; -fx-border-width: 1; -fx-border-radius: 4;");
        VBox.setVgrow(canvasHolder, Priority.ALWAYS);

        previewBox.getChildren().add(canvasHolder);
        setCenter(previewBox);

        // Generate initial nest graph and update preview
        regenerateNestGraph();
        updatePreview();
    }

    /**
     * Creates top header bar with title, accessible action buttons, and view mode controls.
     */
    private VBox createTopHeaderBar() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(0, 0, 10, 0));

        // Row 1: Title and Main Action Buttons (Top Placement)
        HBox topRow = new HBox(15);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("🐜 Nest Generator");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #00d4ff;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnGenerate = new Button("🔄 Regenerate Preview");
        btnGenerate.setStyle("-fx-font-weight: bold;");
        btnGenerate.setOnAction(e -> {
            regenerateNestGraph();
            updatePreview();
        });

        Button btnApplyToWorld = new Button("✓ Apply to World Editor");
        btnApplyToWorld.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
        btnApplyToWorld.setOnAction(e -> applyToWorld());

        Button btnSave = new Button("💾 Save Template");
        btnSave.setOnAction(e -> saveTemplate());

        Button btnLoad = new Button("📂 Load Template");
        btnLoad.setOnAction(e -> loadTemplate());

        topRow.getChildren().addAll(title, spacer, btnGenerate, btnApplyToWorld, btnSave, btnLoad);

        // Row 2: View Mode and 3D Rotation Controls
        HBox modeRow = new HBox(15);
        modeRow.setAlignment(Pos.CENTER_LEFT);

        Label modeLabel = new Label("View Mode:");
        modeLabel.setStyle("-fx-font-weight: bold;");

        viewModeSelect = new ComboBox<>();
        viewModeSelect.getItems().addAll("2D Side View", "2D Top View", "3D View");
        viewModeSelect.getSelectionModel().select("3D View");
        viewModeSelect.setOnAction(e -> {
            boolean is3D = "3D View".equals(viewModeSelect.getValue());
            if (controls3DBox != null) {
                controls3DBox.setVisible(is3D);
                controls3DBox.setManaged(is3D);
            }
            updatePreview();
        });

        // 3D Rotation Controls
        controls3DBox = new HBox(10);
        controls3DBox.setAlignment(Pos.CENTER_LEFT);

        Label azLabel = new Label("3D Orbit:");
        azimuthSlider = new Slider(0, 360, 45);
        azimuthSlider.setPrefWidth(120);
        azimuthSlider.valueProperty().addListener((o, a, b) -> updatePreview());

        Label elLabel = new Label("Elevation:");
        elevationSlider = new Slider(10, 80, 35);
        elevationSlider.setPrefWidth(100);
        elevationSlider.valueProperty().addListener((o, a, b) -> updatePreview());

        controls3DBox.getChildren().addAll(azLabel, azimuthSlider, elLabel, elevationSlider);

        modeRow.getChildren().addAll(modeLabel, viewModeSelect, new Separator(javafx.geometry.Orientation.VERTICAL), controls3DBox);

        header.getChildren().addAll(topRow, modeRow, new Separator());
        return header;
    }

    private VBox createConfigPane() {
        VBox config = new VBox(12);
        config.setPadding(new Insets(10));
        config.setPrefWidth(290);
        // Default style without hardcoded background color

        // === Nest Type Preset ===
        Label typeLabel = new Label("Nest Preset:");
        typeLabel.setStyle("-fx-font-weight: bold;");

        nestTypeSelect = new ComboBox<>();
        nestTypeSelect.getItems().addAll("Young Colony", "Mature Colony", "Complex Supercolony",
                "Leafcutter Fungus Farm", "Custom");
        nestTypeSelect.getSelectionModel().selectFirst();
        nestTypeSelect.setPrefWidth(220);
        nestTypeSelect.setOnAction(e -> applyPreset(nestTypeSelect.getValue()));

        // === Depth ===
        Label depthLabel = new Label("Max Depth (blocks):");
        depthSlider = createSlider(5, 50, 20);
        HBox depthBox = sliderWithValue(depthSlider);

        // === Chamber Count ===
        Label chamberLabel = new Label("Total Chambers:");
        chamberCountSlider = createSlider(3, 50, 15);
        HBox chamberBox = sliderWithValue(chamberCountSlider);

        // === Tunnel Width ===
        Label tunnelLabel = new Label("Tunnel Width:");
        tunnelWidthSlider = createSlider(1, 5, 2);
        HBox tunnelBox = sliderWithValue(tunnelWidthSlider);

        // === Branching Factor ===
        Label branchLabel = new Label("Branching & Complexity:");
        branchingSlider = createSlider(1, 5, 3);
        HBox branchBox = sliderWithValue(branchingSlider);

        // === Chamber Types ===
        TitledPane chamberPane = createChamberTypesPane();

        config.getChildren().addAll(
                typeLabel, nestTypeSelect,
                new Separator(),
                depthLabel, depthBox,
                chamberLabel, chamberBox,
                tunnelLabel, tunnelBox,
                branchLabel, branchBox,
                new Separator(),
                chamberPane);

        // Listen for parameter changes to auto-regenerate preview
        depthSlider.valueProperty().addListener((o, a, b) -> { regenerateNestGraph(); updatePreview(); });
        chamberCountSlider.valueProperty().addListener((o, a, b) -> { regenerateNestGraph(); updatePreview(); });
        tunnelWidthSlider.valueProperty().addListener((o, a, b) -> updatePreview());
        branchingSlider.valueProperty().addListener((o, a, b) -> { regenerateNestGraph(); updatePreview(); });

        return config;
    }

    private TitledPane createChamberTypesPane() {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(6);
        grid.setPadding(new Insets(8));

        String[][] chamberTypes = {
                { "👑 Queen Chamber", "1" },
                { "🥚 Brood Chambers", "3" },
                { "🍖 Food Storage", "4" },
                { "🚪 Entrances", "2" },
                { "🗑 Waste Dumps", "1" },
                { "🍄 Fungus Gardens", "0" }
        };

        int row = 0;
        for (String[] type : chamberTypes) {
            Label label = new Label(type[0]);
            Spinner<Integer> spinner = new Spinner<>(0, 25, Integer.parseInt(type[1]));
            spinner.setPrefWidth(70);
            spinner.valueProperty().addListener((o, a, b) -> { regenerateNestGraph(); updatePreview(); });

            chamberSpinners.put(type[0], spinner);

            grid.add(label, 0, row);
            grid.add(spinner, 1, row);
            row++;
        }

        TitledPane pane = new TitledPane("Chamber Distribution", grid);
        pane.setExpanded(true);
        return pane;
    }

    private Slider createSlider(double min, double max, double value) {
        Slider slider = new Slider(min, max, value);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit((max - min) / 4);
        slider.setPrefWidth(180);
        return slider;
    }

    private HBox sliderWithValue(Slider slider) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        Label value = new Label(String.format("%.0f", slider.getValue()));
        value.setStyle("-fx-text-fill: #00d4ff; -fx-min-width: 35; -fx-font-weight: bold;");
        slider.valueProperty().addListener((obs, old, val) -> value.setText(String.format("%.0f", val.doubleValue())));
        box.getChildren().addAll(slider, value);
        return box;
    }

    private void setupMouseInteraction() {
        previewCanvas.setOnMousePressed(e -> {
            lastMouseX = e.getX();
            lastMouseY = e.getY();
        });

        previewCanvas.setOnMouseDragged(e -> {
            if ("3D View".equals(viewModeSelect.getValue())) {
                double dx = e.getX() - lastMouseX;
                double dy = e.getY() - lastMouseY;

                double newAz = (azimuthSlider.getValue() + dx * 0.8) % 360;
                if (newAz < 0) newAz += 360;
                azimuthSlider.setValue(newAz);

                double newEl = Math.max(10, Math.min(80, elevationSlider.getValue() - dy * 0.5));
                elevationSlider.setValue(newEl);

                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }
        });
    }

    private void applyPreset(String preset) {
        switch (preset) {
            case "Young Colony" -> {
                depthSlider.setValue(10);
                chamberCountSlider.setValue(5);
                tunnelWidthSlider.setValue(2);
                branchingSlider.setValue(2);
                setSpinner("👑 Queen Chamber", 1);
                setSpinner("🥚 Brood Chambers", 2);
                setSpinner("🍖 Food Storage", 1);
                setSpinner("🚪 Entrances", 1);
                setSpinner("🗑 Waste Dumps", 0);
                setSpinner("🍄 Fungus Gardens", 0);
            }
            case "Mature Colony" -> {
                depthSlider.setValue(25);
                chamberCountSlider.setValue(20);
                tunnelWidthSlider.setValue(3);
                branchingSlider.setValue(3);
                setSpinner("👑 Queen Chamber", 1);
                setSpinner("🥚 Brood Chambers", 5);
                setSpinner("🍖 Food Storage", 6);
                setSpinner("🚪 Entrances", 3);
                setSpinner("🗑 Waste Dumps", 2);
                setSpinner("🍄 Fungus Gardens", 0);
            }
            case "Complex Supercolony" -> {
                depthSlider.setValue(45);
                chamberCountSlider.setValue(50);
                tunnelWidthSlider.setValue(4);
                branchingSlider.setValue(5);
                setSpinner("👑 Queen Chamber", 5);
                setSpinner("🥚 Brood Chambers", 12);
                setSpinner("🍖 Food Storage", 15);
                setSpinner("🚪 Entrances", 8);
                setSpinner("🗑 Waste Dumps", 5);
                setSpinner("🍄 Fungus Gardens", 0);
            }
            case "Leafcutter Fungus Farm" -> {
                depthSlider.setValue(35);
                chamberCountSlider.setValue(30);
                tunnelWidthSlider.setValue(3);
                branchingSlider.setValue(4);
                setSpinner("👑 Queen Chamber", 1);
                setSpinner("🥚 Brood Chambers", 4);
                setSpinner("🍖 Food Storage", 3);
                setSpinner("🚪 Entrances", 4);
                setSpinner("🗑 Waste Dumps", 3);
                setSpinner("🍄 Fungus Gardens", 10);
            }
        }
        regenerateNestGraph();
        updatePreview();
    }

    private void setSpinner(String name, int value) {
        Spinner<Integer> spinner = chamberSpinners.get(name);
        if (spinner != null) {
            spinner.getValueFactory().setValue(value);
        }
    }

    private int getSpinnerValue(String name) {
        Spinner<Integer> spinner = chamberSpinners.get(name);
        return spinner != null ? spinner.getValue() : 0;
    }

    // =========================================================================
    // BIOLOGICAL NEST GENERATION ALGORITHM
    // =========================================================================

    public static class NestNode {
        public double x, y, z; // 3D coordinates (units: blocks)
        public String type;    // ENTRANCE, QUEEN, BROOD, FOOD, FUNGUS, WASTE, JUNCTION
        public double radius;
        public Color color;

        public NestNode(double x, double y, double z, String type, double radius, Color color) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.type = type;
            this.radius = radius;
            this.color = color;
        }
    }

    public static class NestEdge {
        public NestNode from;
        public NestNode to;
        public List<double[]> waypoints; // Organic curved 3D points

        public NestEdge(NestNode from, NestNode to, List<double[]> waypoints) {
            this.from = from;
            this.to = to;
            this.waypoints = waypoints;
        }
    }

    public static class GeneratedNest {
        public List<NestNode> nodes = new ArrayList<>();
        public List<NestEdge> edges = new ArrayList<>();
        public double maxDepth;
    }

    /**
     * Generates a biologically plausible 3D nest graph based on ant excavation behaviors:
     * - Entrances at surface Z=0
     * - Main undulating vertical/helical shafts descending deep
     * - Radial branching subtunnels with organic curvature
     * - Zoned chamber placement according to microclimate needs (Brood mid-depth, Queen deep, Waste lateral)
     */
    private void regenerateNestGraph() {
        GeneratedNest nest = new GeneratedNest();
        double maxDepth = depthSlider.getValue();
        nest.maxDepth = maxDepth;

        int entranceCount = Math.max(1, getSpinnerValue("🚪 Entrances"));
        int queenCount = getSpinnerValue("👑 Queen Chamber");
        int broodCount = getSpinnerValue("🥚 Brood Chambers");
        int foodCount = getSpinnerValue("🍖 Food Storage");
        int wasteCount = getSpinnerValue("🗑 Waste Dumps");
        int fungusCount = getSpinnerValue("🍄 Fungus Gardens");
        int branchingFactor = (int) branchingSlider.getValue();

        Random rand = new Random((long) (maxDepth * 100 + entranceCount * 10 + queenCount * 5 + broodCount));

        // 1. Create Entrances at surface (Z=0)
        List<NestNode> entrances = new ArrayList<>();
        for (int i = 0; i < entranceCount; i++) {
            double angle = (2 * Math.PI * i) / entranceCount + rand.nextDouble() * 0.4;
            double dist = (entranceCount > 1 ? 4.0 + rand.nextDouble() * 8.0 : 0.0);
            double ex = dist * Math.cos(angle);
            double ey = dist * Math.sin(angle);
            NestNode node = new NestNode(ex, ey, 0, "ENTRANCE", 2.0, Color.LIMEGREEN);
            nest.nodes.add(node);
            entrances.add(node);
        }

        // Central hub junction below entrance
        NestNode mainHub = new NestNode(0, 0, 2.0 + rand.nextDouble() * 2.0, "JUNCTION", 1.2, Color.LIGHTSLATEGRAY);
        nest.nodes.add(mainHub);

        for (NestNode ent : entrances) {
            nest.edges.add(createOrganicEdge(ent, mainHub, rand));
        }

        // 2. Primary Helical / Winding Main Shafts
        List<NestNode> mainShaftNodes = new ArrayList<>();
        mainShaftNodes.add(mainHub);

        int shaftSteps = 4 + (int) (maxDepth / 6.0);
        NestNode prevNode = mainHub;
        for (int step = 1; step <= shaftSteps; step++) {
            double currentZ = (step / (double) shaftSteps) * maxDepth * 0.95;
            // Helical / organic wobble
            double wobbleAngle = step * 0.8 + rand.nextDouble() * 0.5;
            double wobbleRadius = 2.0 + rand.nextDouble() * 3.0;
            double sx = wobbleRadius * Math.cos(wobbleAngle);
            double sy = wobbleRadius * Math.sin(wobbleAngle);

            NestNode shaftNode = new NestNode(sx, sy, currentZ, "JUNCTION", 1.2, Color.LIGHTSLATEGRAY);
            nest.nodes.add(shaftNode);
            nest.edges.add(createOrganicEdge(prevNode, shaftNode, rand));
            mainShaftNodes.add(shaftNode);
            prevNode = shaftNode;
        }

        // 3. Collect target chamber assignments with realistic biological depth zones
        List<String> chamberQueue = new ArrayList<>();
        for (int i = 0; i < queenCount; i++) chamberQueue.add("QUEEN");
        for (int i = 0; i < broodCount; i++) chamberQueue.add("BROOD");
        for (int i = 0; i < foodCount; i++) chamberQueue.add("FOOD");
        for (int i = 0; i < fungusCount; i++) chamberQueue.add("FUNGUS");
        for (int i = 0; i < wasteCount; i++) chamberQueue.add("WASTE");

        // 4. Distribute chambers attached to main shaft nodes or radial branches
        for (String type : chamberQueue) {
            double targetDepthRatio;
            Color chamberColor;
            double radius;

            switch (type) {
                case "QUEEN" -> {
                    targetDepthRatio = 0.80 + rand.nextDouble() * 0.15; // Deepest, safest
                    chamberColor = Color.GOLD;
                    radius = 4.5;
                }
                case "BROOD" -> {
                    targetDepthRatio = 0.25 + rand.nextDouble() * 0.40; // Mid depth (warmth/humidity)
                    chamberColor = Color.DEEPSKYBLUE;
                    radius = 3.2;
                }
                case "FOOD" -> {
                    targetDepthRatio = 0.15 + rand.nextDouble() * 0.35; // Near surface for easy transport
                    chamberColor = Color.ORANGE;
                    radius = 3.5;
                }
                case "FUNGUS" -> {
                    targetDepthRatio = 0.45 + rand.nextDouble() * 0.30; // Deep humid cavities
                    chamberColor = Color.MEDIUMPURPLE;
                    radius = 4.0;
                }
                case "WASTE" -> {
                    targetDepthRatio = 0.60 + rand.nextDouble() * 0.30; // Lateral bottom branches
                    chamberColor = Color.INDIANRED;
                    radius = 3.0;
                }
                default -> {
                    targetDepthRatio = 0.5;
                    chamberColor = Color.SANDYBROWN;
                    radius = 2.5;
                }
            }

            double targetZ = targetDepthRatio * maxDepth;
            // Find closest shaft node by depth
            NestNode parentShaft = mainShaftNodes.get(0);
            double minDiff = Double.MAX_VALUE;
            for (NestNode sn : mainShaftNodes) {
                double diff = Math.abs(sn.z - targetZ);
                if (diff < minDiff) {
                    minDiff = diff;
                    parentShaft = sn;
                }
            }

            // Radial branch out
            double branchAngle = rand.nextDouble() * Math.PI * 2;
            double branchLength = 6.0 + rand.nextDouble() * (6.0 + branchingFactor * 2.5);
            double cx = parentShaft.x + branchLength * Math.cos(branchAngle);
            double cy = parentShaft.y + branchLength * Math.sin(branchAngle);
            double cz = Math.min(maxDepth, parentShaft.z + (rand.nextDouble() - 0.3) * 3.0);

            NestNode chamberNode = new NestNode(cx, cy, cz, type, radius, chamberColor);
            nest.nodes.add(chamberNode);

            // Create curved connecting tunnel
            nest.edges.add(createOrganicEdge(parentShaft, chamberNode, rand));
        }

        // 5. Cross-Connecting Tunnels (Loops) for Realistic Ant Traffic Networks
        if (branchingFactor >= 3 && mainShaftNodes.size() >= 4) {
            for (int i = 1; i < mainShaftNodes.size() - 2; i += 2) {
                NestNode n1 = mainShaftNodes.get(i);
                NestNode n2 = mainShaftNodes.get(i + 2);
                nest.edges.add(createOrganicEdge(n1, n2, rand));
            }
        }

        this.currentNest = nest;
    }

    /**
     * Generates a curved 3D tunnel path between two nodes using organic sinusoidal perturbation.
     */
    private NestEdge createOrganicEdge(NestNode from, NestNode to, Random rand) {
        List<double[]> waypoints = new ArrayList<>();
        waypoints.add(new double[]{from.x, from.y, from.z});

        int segments = 5;
        double dx = (to.x - from.x) / segments;
        double dy = (to.y - from.y) / segments;
        double dz = (to.z - from.z) / segments;

        double perpX = -dy;
        double perpY = dx;
        double len = Math.hypot(perpX, perpY);
        if (len > 0.001) {
            perpX /= len;
            perpY /= len;
        }

        for (int i = 1; i < segments; i++) {
            double factor = Math.sin((i / (double) segments) * Math.PI);
            double offset = (rand.nextDouble() - 0.5) * 2.5 * factor;
            double wx = from.x + dx * i + perpX * offset;
            double wy = from.y + dy * i + perpY * offset;
            double wz = from.z + dz * i + (rand.nextDouble() - 0.5) * 1.0 * factor;
            waypoints.add(new double[]{wx, wy, wz});
        }

        waypoints.add(new double[]{to.x, to.y, to.z});
        return new NestEdge(from, to, waypoints);
    }

    // =========================================================================
    // RENDERING PIPELINE (2D Side View, 2D Top View, 3D Isometric View)
    // =========================================================================

    private void updatePreview() {
        if (currentNest == null) regenerateNestGraph();

        double w = previewCanvas.getWidth();
        double h = previewCanvas.getHeight();

        gc.clearRect(0, 0, w, h);

        String mode = viewModeSelect.getValue();
        switch (mode) {
            case "2D Side View" -> render2DSideView(w, h);
            case "2D Top View" -> render2DTopView(w, h);
            case "3D View" -> render3DView(w, h);
            default -> render3DView(w, h);
        }
    }

    /**
     * 2D Side View: Cross-section elevation showing soil layers, curved tunnels, and chambers.
     */
    private void render2DSideView(double w, double h) {
        // Dark background
        gc.setFill(Color.rgb(18, 18, 30));
        gc.fillRect(0, 0, w, h);

        // Sky & Surface Ground
        gc.setFill(Color.rgb(35, 45, 60));
        gc.fillRect(0, 0, w, 40);

        gc.setFill(Color.rgb(60, 90, 45));
        gc.fillRect(0, 40, w, 8);

        // Soil Body
        gc.setFill(Color.rgb(55, 38, 25));
        gc.fillRect(0, 48, w, h - 48);

        // Soil depth gradient lines
        gc.setStroke(Color.rgb(75, 52, 35, 0.4));
        gc.setLineWidth(1);
        for (double y = 80; y < h; y += 40) {
            gc.strokeLine(0, y, w, y);
        }

        double maxDepth = currentNest.maxDepth;
        double scaleY = (h - 90) / Math.max(1, maxDepth);
        double scaleX = 8.0;
        double centerX = w / 2;
        double startY = 48;

        // Render Tunnels (Edges)
        double tunnelW = tunnelWidthSlider.getValue() * 2.2;
        gc.setStroke(Color.rgb(130, 90, 55));
        gc.setLineWidth(tunnelW);

        for (NestEdge edge : currentNest.edges) {
            gc.beginPath();
            for (int i = 0; i < edge.waypoints.size(); i++) {
                double[] pt = edge.waypoints.get(i);
                double px = centerX + pt[0] * scaleX;
                double py = startY + pt[2] * scaleY;
                if (i == 0) gc.moveTo(px, py);
                else gc.lineTo(px, py);
            }
            gc.stroke();
        }

        // Render Chambers (Nodes)
        for (NestNode node : currentNest.nodes) {
            double nx = centerX + node.x * scaleX;
            double ny = startY + node.z * scaleY;
            double rad = node.radius * 2.8;

            if ("ENTRANCE".equals(node.type)) {
                gc.setFill(Color.DARKGREEN);
                gc.fillOval(nx - 8, startY - 5, 16, 12);
            } else if (!"JUNCTION".equals(node.type)) {
                gc.setFill(node.color.darker());
                gc.fillOval(nx - rad, ny - rad * 0.65, rad * 2, rad * 1.3);

                gc.setStroke(node.color);
                gc.setLineWidth(2);
                gc.strokeOval(nx - rad, ny - rad * 0.65, rad * 2, rad * 1.3);
            }
        }

        // Legend Overlay
        renderLegend(w, h, "2D Elevation View");
    }

    /**
     * 2D Top View: Plan view overhead showing entrance spread and color-coded depth layers.
     */
    private void render2DTopView(double w, double h) {
        gc.setFill(Color.rgb(20, 20, 32));
        gc.fillRect(0, 0, w, h);

        double centerX = w / 2;
        double centerY = h / 2;
        double scale = 8.5;

        // Concentric depth guides
        gc.setStroke(Color.rgb(50, 50, 75, 0.4));
        gc.setLineWidth(1);
        for (double r = 40; r < w / 2; r += 40) {
            gc.strokeOval(centerX - r, centerY - r, r * 2, r * 2);
        }

        // Tunnels
        double tunnelW = tunnelWidthSlider.getValue() * 2.0;
        gc.setStroke(Color.rgb(120, 95, 65));
        gc.setLineWidth(tunnelW);

        for (NestEdge edge : currentNest.edges) {
            gc.beginPath();
            for (int i = 0; i < edge.waypoints.size(); i++) {
                double[] pt = edge.waypoints.get(i);
                double px = centerX + pt[0] * scale;
                double py = centerY + pt[1] * scale;
                if (i == 0) gc.moveTo(px, py);
                else gc.lineTo(px, py);
            }
            gc.stroke();
        }

        // Chambers
        for (NestNode node : currentNest.nodes) {
            double nx = centerX + node.x * scale;
            double ny = centerY + node.y * scale;
            double rad = node.radius * 2.5;

            double depthRatio = node.z / currentNest.maxDepth;
            Color depthTone = Color.hsb(200 - depthRatio * 160, 0.8, 0.9);

            gc.setFill(node.color);
            gc.fillOval(nx - rad, ny - rad, rad * 2, rad * 2);

            gc.setStroke(depthTone);
            gc.setLineWidth(2);
            gc.strokeOval(nx - rad, ny - rad, rad * 2, rad * 2);
        }

        renderLegend(w, h, "2D Top (Plan) View");
    }

    /**
     * 3D Isometric View: Render 3D space with depth sorting, 3D tunnel paths, and shaded spheres.
     */
    private void render3DView(double w, double h) {
        // Dark space background
        gc.setFill(Color.rgb(12, 14, 25));
        gc.fillRect(0, 0, w, h);

        double azimuth = azimuthSlider.getValue();
        double elevation = elevationSlider.getValue();

        double radAz = Math.toRadians(azimuth);
        double radEl = Math.toRadians(elevation);

        double cosAz = Math.cos(radAz);
        double sinAz = Math.sin(radAz);
        double cosEl = Math.cos(radEl);
        double sinEl = Math.sin(radEl);

        double centerX = w / 2;
        double centerY = h / 2 - 20;
        double scale = 7.5;

        // Helper 3D Projection Lambda: (x, y, z) -> screen [sx, sy, depthKey]
        // 3D coords: X right, Y forward, Z down
        java.util.function.Function<double[], double[]> project = (pt3d) -> {
            double x = pt3d[0];
            double y = pt3d[1];
            double z = pt3d[2];

            double rx = x * cosAz - y * sinAz;
            double ry = x * sinAz + y * cosAz;
            double rz = z;

            double sx = centerX + rx * scale;
            double sy = centerY + (ry * sinEl + rz * cosEl) * scale;
            double depthKey = ry * cosEl - rz * sinEl;

            return new double[]{sx, sy, depthKey};
        };

        // Render Ground Plane Grid (Z=0)
        gc.setStroke(Color.rgb(40, 70, 50, 0.5));
        gc.setLineWidth(1);
        double gridSize = 25;
        for (double g = -gridSize; g <= gridSize; g += 5) {
            double[] p1 = project.apply(new double[]{g, -gridSize, 0});
            double[] p2 = project.apply(new double[]{g, gridSize, 0});
            gc.strokeLine(p1[0], p1[1], p2[0], p2[1]);

            double[] p3 = project.apply(new double[]{-gridSize, g, 0});
            double[] p4 = project.apply(new double[]{gridSize, g, 0});
            gc.strokeLine(p3[0], p3[1], p4[0], p4[1]);
        }

        // Collect Drawable 3D Primitives for Depth Sorting
        class RenderItem implements Comparable<RenderItem> {
            double depth;
            Runnable renderAction;

            RenderItem(double depth, Runnable renderAction) {
                this.depth = depth;
                this.renderAction = renderAction;
            }

            @Override
            public int compareTo(RenderItem o) {
                return Double.compare(this.depth, o.depth); // Draw back to front
            }
        }

        List<RenderItem> renderList = new ArrayList<>();
        double tunnelW = tunnelWidthSlider.getValue() * 2.2;

        // Add 3D Tunnel Edges
        for (NestEdge edge : currentNest.edges) {
            double avgDepth = 0;
            List<double[]> projectedPts = new ArrayList<>();
            for (double[] wp : edge.waypoints) {
                double[] proj = project.apply(wp);
                projectedPts.add(proj);
                avgDepth += proj[2];
            }
            avgDepth /= edge.waypoints.size();

            renderList.add(new RenderItem(avgDepth - 1000, () -> {
                gc.setStroke(Color.rgb(150, 110, 70, 0.85));
                gc.setLineWidth(tunnelW);
                gc.beginPath();
                for (int i = 0; i < projectedPts.size(); i++) {
                    double[] p = projectedPts.get(i);
                    if (i == 0) gc.moveTo(p[0], p[1]);
                    else gc.lineTo(p[0], p[1]);
                }
                gc.stroke();
            }));
        }

        // Add 3D Chambers (Nodes)
        for (NestNode node : currentNest.nodes) {
            double[] proj = project.apply(new double[]{node.x, node.y, node.z});
            double px = proj[0];
            double py = proj[1];
            double depth = proj[2];
            double rad = node.radius * 2.6;

            renderList.add(new RenderItem(depth, () -> {
                if ("ENTRANCE".equals(node.type)) {
                    gc.setFill(Color.LIMEGREEN);
                    gc.fillOval(px - 6, py - 4, 12, 8);
                } else if (!"JUNCTION".equals(node.type)) {
                    // Shaded 3D Sphere with Radial Gradient
                    RadialGradient grad = new RadialGradient(
                            0, 0,
                            px - rad * 0.3, py - rad * 0.3, rad * 1.2, false,
                            CycleMethod.NO_CYCLE,
                            new Stop(0, node.color.brighter()),
                            new Stop(0.7, node.color),
                            new Stop(1.0, node.color.darker().darker())
                    );
                    gc.setFill(grad);
                    gc.fillOval(px - rad, py - rad, rad * 2, rad * 2);

                    gc.setStroke(Color.rgb(255, 255, 255, 0.4));
                    gc.setLineWidth(1);
                    gc.strokeOval(px - rad, py - rad, rad * 2, rad * 2);
                }
            }));
        }

        // Sort items back to front and render
        Collections.sort(renderList);
        for (RenderItem item : renderList) {
            item.renderAction.run();
        }

        renderLegend(w, h, "3D View (Drag mouse to orbit)");
    }

    private void renderLegend(double w, double h, String title) {
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("SansSerif", 13));
        gc.fillText(title, 12, 22);

        // Stats summary at bottom-left of preview
        gc.setFont(javafx.scene.text.Font.font("SansSerif", 11));
        gc.setFill(Color.LIGHTGRAY);
        gc.fillText(String.format("Depth: %.0f blocks | Chambers: %d | Nodes: %d",
                currentNest.maxDepth, getSpinnerValue("👑 Queen Chamber") + getSpinnerValue("🥚 Brood Chambers") + getSpinnerValue("🍖 Food Storage"),
                currentNest.nodes.size()), 12, h - 12);
    }

    // Callback for applying configuration to world
    private java.util.function.Consumer<Map<String, Object>> onApplyCallback;

    public void setOnApply(java.util.function.Consumer<Map<String, Object>> callback) {
        this.onApplyCallback = callback;
    }

    private void applyToWorld() {
        if (onApplyCallback != null) {
            onApplyCallback.accept(getConfiguration());
        } else {
            new Alert(Alert.AlertType.WARNING,
                    "No world editor connected.\n" +
                            "This feature requires the full SwarmForge Studio.")
                    .show();
        }
    }

    private void saveTemplate() {
        TextInputDialog dialog = new TextInputDialog("My Nest Template");
        dialog.setTitle("Save Nest Template");
        dialog.setHeaderText("Enter template name:");
        dialog.showAndWait().ifPresent(name -> {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.io.File file = new java.io.File("nest_template_" + name.replace(" ", "_") + ".json");
                mapper.writerWithDefaultPrettyPrinter().writeValue(file, getConfiguration());
                new Alert(Alert.AlertType.INFORMATION, "Template saved to " + file.getName()).show();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Save failed: " + ex.getMessage()).show();
            }
        });
    }

    private void loadTemplate() {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Load Nest Template");
        dialog.setHeaderText("Enter template name (without .json):");
        dialog.showAndWait().ifPresent(name -> {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.io.File file = new java.io.File("nest_template_" + name.replace(" ", "_") + ".json");
                if (!file.exists()) {
                    new Alert(Alert.AlertType.ERROR, "Template not found: " + file.getName()).show();
                    return;
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> config = mapper.readValue(file, Map.class);

                if (config.containsKey("nestType"))
                    nestTypeSelect.setValue((String) config.get("nestType"));
                if (config.containsKey("depth"))
                    depthSlider.setValue(((Number) config.get("depth")).doubleValue());
                if (config.containsKey("chamberCount"))
                    chamberCountSlider.setValue(((Number) config.get("chamberCount")).doubleValue());
                if (config.containsKey("tunnelWidth"))
                    tunnelWidthSlider.setValue(((Number) config.get("tunnelWidth")).doubleValue());
                if (config.containsKey("branching"))
                    branchingSlider.setValue(((Number) config.get("branching")).doubleValue());

                if (config.containsKey("chamberDistribution")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Integer> dist = (Map<String, Integer>) config.get("chamberDistribution");
                    dist.forEach((k, v) -> setSpinner(k, v));
                }

                regenerateNestGraph();
                updatePreview();
                new Alert(Alert.AlertType.INFORMATION, "Template loaded!").show();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Load failed: " + ex.getMessage()).show();
                ex.printStackTrace();
            }
        });
    }

    /**
     * Get current nest configuration as a map.
     */
    public Map<String, Object> getConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("nestType", nestTypeSelect.getValue());
        config.put("depth", (int) depthSlider.getValue());
        config.put("chamberCount", (int) chamberCountSlider.getValue());
        config.put("tunnelWidth", (int) tunnelWidthSlider.getValue());
        config.put("branching", (int) branchingSlider.getValue());

        Map<String, Integer> chambers = new HashMap<>();
        chamberSpinners.forEach((k, v) -> chambers.put(k, v.getValue()));
        config.put("chamberDistribution", chambers);

        return config;
    }
}
