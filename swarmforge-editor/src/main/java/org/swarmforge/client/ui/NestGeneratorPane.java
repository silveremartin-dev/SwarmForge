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

public class NestGeneratorPane extends BorderPane {

    // canvases
    private Canvas canvas3D, canvasSide, canvasTop;
    private GraphicsContext gc3D, gcSide, gcTop;

    // camera
    private double azimuth = 45, elevation = 35, zoom = 7.5;
    private double lastMX, lastMY;

    // controls
    private ComboBox<String> nestTypeSelect;
    private ComboBox<String> archSelect;
    private ComboBox<String> matSelect;
    private Slider workerSizeSlider;
    private Slider depthSlider, tunnelWidthSlider, branchingSlider, chamberCountSlider;
    private final Map<String, Spinner<Integer>> chamberSpinners = new LinkedHashMap<>();

    // presets
    private ComboBox<String> presetsCombo;
    private final NestPresetManager presetMgr = new NestPresetManager();

    // model
    private GeneratedNest nest;
    private Consumer<Map<String, Object>> onApplyCallback;

    public NestGeneratorPane() {
        setTop(buildHeader());
        setLeft(buildConfig());
        setCenter(buildViews());
        refreshPresetsCombo();
        regen();
        repaint();
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private VBox buildHeader() {
        VBox v = new VBox(6);
        v.setPadding(new Insets(8, 10, 5, 10));

        HBox r = new HBox(8);
        r.setAlignment(Pos.CENTER_LEFT);

        Label t = new Label("🐜 Universal Nest Generator");
        t.setStyle("-fx-font-size:18;-fx-font-weight:bold;-fx-text-fill:#00d4ff;");

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Label lp = new Label("Preset:");
        presetsCombo = new ComboBox<>();
        presetsCombo.setPrefWidth(175);
        presetsCombo.setPromptText("Select…");
        presetsCombo.setOnAction(e -> {
            String s = presetsCombo.getValue();
            if (s != null && presetMgr.contains(s)) applyCfg(presetMgr.get(s));
        });

        Button bAdd = btn("💾 Add to Presets", "#17a2b8");
        bAdd.setOnAction(e -> doAddPreset());

        Button bExp = new Button("📤 Export"); bExp.setOnAction(e -> doExport());
        Button bImp = new Button("📂 Import"); bImp.setOnAction(e -> doImport());

        Button bApply = btn("✓ Apply to World", "#28a745");
        bApply.setOnAction(e -> applyToWorld());

        r.getChildren().addAll(t, sp, lp, presetsCombo, bAdd,
            new Separator(Orientation.VERTICAL), bExp, bImp,
            new Separator(Orientation.VERTICAL), bApply);
        v.getChildren().addAll(r, new Separator());
        return v;
    }

    private Button btn(String text, String bg) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:white;");
        return b;
    }

    // ── Config panel ──────────────────────────────────────────────────────────

    private ScrollPane buildConfig() {
        VBox cfg = new VBox(10);
        cfg.setPadding(new Insets(10));
        cfg.setPrefWidth(272);

        // Nest Architecture (collapsible)
        VBox arch = new VBox(7); arch.setPadding(new Insets(8));

        Label tl = new Label("Preset Name:");
        nestTypeSelect = new ComboBox<>();
        nestTypeSelect.getItems().addAll("Young Ant Burrow (Lasius)","Mature Ant Burrow",
            "Complex Supercolony","Leafcutter Fungus Farm (Atta)",
            "Honeybee Wax Comb (Apis)", "Bumblebee Pot Cluster (Bombus)",
            "Paper Wasp Nest (Vespula)", "Termite Cathedral Mound (Macrotermes)",
            "Weaver Ant Leaf Nest (Oecophylla)", "Custom");
        nestTypeSelect.getSelectionModel().selectFirst();
        nestTypeSelect.setPrefWidth(218);
        nestTypeSelect.setOnAction(e -> {
            String v = nestTypeSelect.getValue();
            if (presetMgr.contains(v)) applyCfg(presetMgr.get(v));
            else { regen(); repaint(); }
        });

        Label al = new Label("Architecture Type:");
        archSelect = new ComboBox<>();
        archSelect.getItems().addAll(
            "BURROW_UNDERGROUND", "SURFACE_MOUND", "WAX_COMB_HEXAGONAL",
            "WAX_POTS_CLUSTER", "PAPER_PEDUNCULATE", "CATHEDRAL_MOUND", "ARBOREAL_SILK_LEAF");
        archSelect.getSelectionModel().selectFirst();
        archSelect.setPrefWidth(218);
        archSelect.setOnAction(e -> { regen(); repaint(); });

        Label ml = new Label("Nest Material:");
        matSelect = new ComboBox<>();
        matSelect.getItems().addAll("EARTH", "WOOD_PULP_PAPER", "BEESWAX",
            "STERCORAL_CEMENT", "SILK_WEAVE", "PROPOLIS");
        matSelect.getSelectionModel().selectFirst();
        matSelect.setPrefWidth(218);
        matSelect.setOnAction(e -> { regen(); repaint(); });

        workerSizeSlider  = mkSlider(2.0, 30.0, 5.0);
        depthSlider       = mkSlider(4,  60, 20);
        tunnelWidthSlider = mkSlider(1,   5,  2);
        branchingSlider   = mkSlider(1,   5,  3);
        addLsn(workerSizeSlider, depthSlider, tunnelWidthSlider, branchingSlider);

        arch.getChildren().addAll(tl, nestTypeSelect,
            al, archSelect, ml, matSelect,
            lbl("Worker Scale (mm):"),  sv(workerSizeSlider),
            lbl("Max Depth / H (blk):"),sv(depthSlider),
            lbl("Tunnel Width:"),       sv(tunnelWidthSlider),
            lbl("Branching Factor:"),   sv(branchingSlider));

        TitledPane tp = new TitledPane("Nest Architecture & Species", arch);
        tp.setExpanded(true);

        // Chamber Distribution (non-collapsible)
        Label cdTitle = new Label("Chamber Distribution");
        cdTitle.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-padding:8 0 2 0;");

        chamberCountSlider = mkSlider(3, 50, 15);
        addLsn(chamberCountSlider);

        GridPane grid = new GridPane();
        grid.setHgap(8); grid.setVgap(7); grid.setPadding(new Insets(6));

        String[][] defs = {
            {"👑 Queen Chamber","1"},{"🥚 Brood Chambers","3"},
            {"🍖 Food Storage","4"},{"🚪 Entrances","2"},
            {"🗑 Waste Dumps","1"},{"🍄 Fungus Gardens","0"}
        };
        int row = 0;
        for (String[] d : defs) {
            Spinner<Integer> s = new Spinner<>(0, 25, Integer.parseInt(d[1]));
            s.setPrefWidth(70); s.setEditable(true);
            s.valueProperty().addListener((o,a,b) -> { regen(); repaint(); });
            chamberSpinners.put(d[0], s);
            grid.add(new Label(d[0]), 0, row);
            grid.add(s, 1, row++);
        }

        VBox cd = new VBox(4, cdTitle, new Separator(),
            lbl("Total Chambers:"), sv(chamberCountSlider), grid);

        cfg.getChildren().addAll(tp, cd);
        ScrollPane sc = new ScrollPane(cfg);
        sc.setFitToWidth(true); sc.setPrefWidth(290); sc.setMaxWidth(290);
        return sc;
    }

    private Label lbl(String s) { return new Label(s); }

    private void addLsn(Slider... sliders) {
        for (Slider s : sliders)
            s.valueProperty().addListener((o,a,b) -> { regen(); repaint(); });
    }

    private Slider mkSlider(double min, double max, double val) {
        Slider s = new Slider(min, max, val);
        s.setShowTickLabels(true); s.setShowTickMarks(true);
        s.setMajorTickUnit((max-min)/4); s.setPrefWidth(165);
        return s;
    }

    private HBox sv(Slider s) {
        HBox b = new HBox(8); b.setAlignment(Pos.CENTER_LEFT);
        Label v = new Label(fmt(s.getValue()));
        v.setStyle("-fx-text-fill:#00d4ff;-fx-min-width:32;-fx-font-weight:bold;");
        s.valueProperty().addListener((o,a,n) -> v.setText(fmt(n.doubleValue())));
        b.getChildren().addAll(s, v);
        return b;
    }

    private String fmt(double d) { return String.format("%.1f", d); }

    // ── View area ─────────────────────────────────────────────────────────────

    private HBox buildViews() {
        canvas3D = new Canvas(540, 510); gc3D = canvas3D.getGraphicsContext2D();
        canvasSide = new Canvas(215, 245); gcSide = canvasSide.getGraphicsContext2D();
        canvasTop  = new Canvas(215, 245); gcTop  = canvasTop.getGraphicsContext2D();

        setupMouse();

        StackPane h3d = new StackPane(canvas3D);
        h3d.setStyle("-fx-border-color:#555;-fx-border-width:1;");
        StackPane hSide = new StackPane(canvasSide);
        hSide.setStyle("-fx-border-color:#444;-fx-border-width:1;");
        StackPane hTop  = new StackPane(canvasTop);
        hTop.setStyle("-fx-border-color:#444;-fx-border-width:1;");

        Label ls = new Label("⬛ Side View"); ls.setStyle("-fx-font-size:11;-fx-font-weight:bold;");
        Label lt = new Label("⬜ Top View");  lt.setStyle("-fx-font-size:11;-fx-font-weight:bold;");

        VBox side = new VBox(5, ls, hSide, lt, hTop);
        side.setPadding(new Insets(0,4,0,8)); side.setAlignment(Pos.TOP_CENTER);

        HBox area = new HBox(6, h3d, side);
        area.setPadding(new Insets(8));
        return area;
    }

    private void setupMouse() {
        canvas3D.setOnMousePressed(e -> { lastMX = e.getX(); lastMY = e.getY(); });
        canvas3D.setOnMouseDragged(e -> {
            azimuth   = (azimuth + (e.getX()-lastMX)*0.65) % 360;
            if (azimuth < 0) azimuth += 360;
            elevation = Math.max(5, Math.min(85, elevation - (e.getY()-lastMY)*0.35));
            lastMX = e.getX(); lastMY = e.getY();
            draw3D();
        });
        canvas3D.setOnScroll(e -> {
            zoom = Math.max(2.5, Math.min(22, zoom + e.getDeltaY()*0.025));
            draw3D();
        });
    }

    // ── Preset helpers ────────────────────────────────────────────────────────

    private void refreshPresetsCombo() {
        String cur = presetsCombo.getValue();
        presetsCombo.getItems().setAll(presetMgr.names());
        if (cur != null) presetsCombo.setValue(cur);
    }

    private void doAddPreset() {
        TextInputDialog d = new TextInputDialog(nestTypeSelect.getValue());
        d.setTitle("Add to Presets"); d.setHeaderText("Nom du preset :"); d.setContentText("Nom :");
        d.showAndWait().ifPresent(name -> {
            if (name == null || name.isBlank()) return;
            presetMgr.save(name, getConfiguration());
            refreshPresetsCombo();
            presetsCombo.setValue(name);
            new Alert(Alert.AlertType.INFORMATION, "Preset \"" + name + "\" sauvegardé.").show();
        });
    }

    private void applyCfg(Map<String, Object> c) {
        if (c.containsKey("nestType"))     nestTypeSelect.setValue((String) c.get("nestType"));
        if (c.containsKey("architecture")) archSelect.setValue((String) c.get("architecture"));
        if (c.containsKey("material"))     matSelect.setValue((String) c.get("material"));
        if (c.containsKey("workerSizeMm")) workerSizeSlider.setValue(num(c, "workerSizeMm"));
        if (c.containsKey("depth"))        depthSlider.setValue(num(c,"depth"));
        if (c.containsKey("chamberCount")) chamberCountSlider.setValue(num(c,"chamberCount"));
        if (c.containsKey("tunnelWidth"))  tunnelWidthSlider.setValue(num(c,"tunnelWidth"));
        if (c.containsKey("branching"))    branchingSlider.setValue(num(c,"branching"));
        if (c.containsKey("chamberDistribution")) {
            @SuppressWarnings("unchecked")
            Map<String,Object> dist = (Map<String,Object>) c.get("chamberDistribution");
            dist.forEach((k,v) -> setSp(k, ((Number)v).intValue()));
        }
    }

    private double num(Map<String,Object> m, String k) { return ((Number)m.get(k)).doubleValue(); }
    private void setSp(String k, int v) { Spinner<Integer> s = chamberSpinners.get(k); if (s!=null) s.getValueFactory().setValue(v); }
    private int getSp(String k) { Spinner<Integer> s = chamberSpinners.get(k); return s!=null ? s.getValue() : 0; }

    private void regen()   { nest = NestAlgorithm.generate(this); }
    private void repaint() { draw3D(); drawSide(); drawTop(); }

    // ── File I/O ──────────────────────────────────────────────────────────────

    private void doExport() {
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Export"); fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("JSON","*.json"));
        fc.setInitialFileName("nest.json");
        File f = fc.showSaveDialog(getScene().getWindow());
        if (f == null) return;
        try { new com.fasterxml.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(f, getConfiguration()); }
        catch (Exception ex) { new Alert(Alert.AlertType.ERROR, ex.getMessage()).show(); }
    }

    private void doImport() {
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Import"); fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("JSON","*.json"));
        File f = fc.showOpenDialog(getScene().getWindow());
        if (f == null) return;
        try {
            @SuppressWarnings("unchecked")
            Map<String,Object> cfg = new com.fasterxml.jackson.databind.ObjectMapper().readValue(f, Map.class);
            applyCfg(cfg); regen(); repaint();
        } catch (Exception ex) { new Alert(Alert.AlertType.ERROR, ex.getMessage()).show(); }
    }

    private void applyToWorld() {
        if (onApplyCallback != null) onApplyCallback.accept(getConfiguration());
        else new Alert(Alert.AlertType.WARNING,"No world editor connected.").show();
    }

    public void setOnApply(Consumer<Map<String,Object>> cb) { this.onApplyCallback = cb; }

    public Map<String,Object> getConfiguration() {
        Map<String,Object> c = new LinkedHashMap<>();
        c.put("nestType",     nestTypeSelect.getValue());
        c.put("architecture", archSelect.getValue());
        c.put("material",     matSelect.getValue());
        c.put("workerSizeMm", workerSizeSlider.getValue());
        c.put("depth",        (int) depthSlider.getValue());
        c.put("chamberCount", (int) chamberCountSlider.getValue());
        c.put("tunnelWidth",  (int) tunnelWidthSlider.getValue());
        c.put("branching",    (int) branchingSlider.getValue());
        Map<String,Integer> dist = new LinkedHashMap<>();
        chamberSpinners.forEach((k,v) -> dist.put(k, v.getValue()));
        c.put("chamberDistribution", dist);
        return c;
    }

    // ── Expose params for NestAlgorithm ──────────────────────────────────────

    String getArchitecture() { return archSelect.getValue() != null ? archSelect.getValue() : "BURROW_UNDERGROUND"; }
    String getMaterial()     { return matSelect.getValue() != null ? matSelect.getValue() : "EARTH"; }
    double getWorkerSizeMm() { return workerSizeSlider.getValue(); }
    double getDepth()        { return depthSlider.getValue(); }
    double getTunnelWidth()  { return tunnelWidthSlider.getValue(); }
    double getBranching()    { return branchingSlider.getValue(); }
    double getChamberCount() { return chamberCountSlider.getValue(); }
    int    sp(String k)      { return getSp(k); }

    // ── Drawing delegates ─────────────────────────────────────────────────────

    private void draw3D()   { NestRenderer.draw3D(nest, gc3D, canvas3D.getWidth(), canvas3D.getHeight(), azimuth, elevation, zoom, getTunnelWidth()); }
    private void drawSide() { NestRenderer.drawSide(nest, gcSide, canvasSide.getWidth(), canvasSide.getHeight(), getTunnelWidth()); }
    private void drawTop()  { NestRenderer.drawTop(nest,  gcTop,  canvasTop.getWidth(),  canvasTop.getHeight(),  getTunnelWidth()); }

    // ── Inner model classes ───────────────────────────────────────────────────

    public static class NestNode {
        public double x, y, z, radius, rx, ry, rz; // Anatomical lenticular radii
        public String type; public Color color;
        public NestNode(double x, double y, double z, String type, double r, Color c) {
            this.x=x; this.y=y; this.z=z; this.type=type; this.radius=r; this.color=c;
            this.rx = r; this.ry = r; this.rz = r * 0.55; // Lenticular dome ratio by default
        }
        public NestNode(double x, double y, double z, String type, double rx, double ry, double rz, Color c) {
            this.x=x; this.y=y; this.z=z; this.type=type; this.radius=Math.max(rx, ry);
            this.rx = rx; this.ry = ry; this.rz = rz; this.color=c;
        }
    }

    public static class NestEdge {
        public NestNode from, to;
        public List<double[]> pts;
        public NestEdge(NestNode f, NestNode t, List<double[]> p) { from=f; to=t; pts=p; }
    }

    public static class GeneratedNest {
        public List<NestNode> nodes = new ArrayList<>();
        public List<NestEdge> edges = new ArrayList<>();
        public double maxDepth;
        public String architecture = "BURROW_UNDERGROUND";
        public String material = "EARTH";
        public double workerSizeMm = 5.0;
    }
}
