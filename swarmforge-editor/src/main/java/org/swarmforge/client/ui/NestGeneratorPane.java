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
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.NotificationOverlay;

public class NestGeneratorPane extends BorderPane {

    // Canvases
    private Canvas canvas3D, canvasSide, canvasTop;
    private GraphicsContext gc3D, gcSide, gcTop;

    // 3D camera
    private double azimuth = 45, elevation = 35, zoom = 7.5;
    private double pan3DX = 0, pan3DY = 0;
    private double lastMX, lastMY;

    // 2D Side view camera (Zoom & Pan)
    private double sideZoom = 1.0;
    private double sidePanX = 0, sidePanY = 0;
    private double lastSideMX, lastSideMY;

    // 2D Top view camera (Zoom & Pan)
    private double topZoom = 1.0;
    private double topPanX = 0, topPanY = 0;
    private double lastTopMX, lastTopMY;

    // Synchronization control
    private CheckBox syncViewsCheckBox;

    // Controls
    private ComboBox<String> speciesModelCombo;
    private ComboBox<String> nestStageCombo;
    private Label speciesStatusLabel;
    private org.swarmforge.core.species.CustomSpecies activeCustomSpecies;
    private boolean isUpdatingSpeciesCombo = false;

    private ComboBox<String> categorySelect;
    private ComboBox<String> archSelect;
    private ComboBox<String> matSelect;
    private Slider workerSizeSlider;
    private Slider depthSlider, tunnelWidthSlider, branchingSlider;
    private Label lblTotalChambersValue;
    private final Map<String, Spinner<Integer>> chamberSpinners = new LinkedHashMap<>();

    // Presets
    private ComboBox<String> presetsCombo;
    private final NestPresetManager presetMgr = new NestPresetManager();

    // Model
    private GeneratedNest nest;
    private Consumer<Map<String, Object>> onApplyCallback;

    public NestGeneratorPane() {
        setTop(buildHeader());
        setLeft(buildConfig());
        setCenter(buildViews());
        refreshPresetsCombo();
        if (!presetsCombo.getItems().isEmpty()) {
            presetsCombo.getSelectionModel().selectFirst();
            String first = presetsCombo.getValue();
            if (presetMgr.contains(first)) applyCfg(presetMgr.get(first));
        }
        regen();
        repaint();
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private VBox buildHeader() {
        I18nManager i18n = I18nManager.getInstance();
        VBox v = new VBox(6);
        v.setPadding(new Insets(8, 10, 5, 10));

        HBox r = new HBox(8);
        r.setAlignment(Pos.CENTER_LEFT);

        Label t = new Label();
        t.textProperty().bind(i18n.createStringBinding("nest.title"));
        t.setStyle("-fx-font-size:18;-fx-font-weight:bold;");

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Label lp = new Label();
        lp.textProperty().bind(i18n.createStringBinding("preset.label"));
        lp.setStyle("-fx-font-weight: bold;");

        presetsCombo = new ComboBox<>();
        presetsCombo.setPrefWidth(210);
        presetsCombo.promptTextProperty().bind(i18n.createStringBinding("preset.prompt"));
        refreshPresetsCombo();
        presetsCombo.setOnAction(e -> {
            String s = presetsCombo.getValue();
            if (s != null && presetMgr.contains(s)) applyCfg(presetMgr.get(s));
        });

        Button bAdd = new Button();
        bAdd.textProperty().bind(i18n.createStringBinding("preset.save"));
        bAdd.getStyleClass().add("btn-secondary");
        bAdd.setOnAction(e -> doAddPreset());

        Button bDel = new Button();
        bDel.textProperty().bind(i18n.createStringBinding("preset.delete"));
        bDel.getStyleClass().add("btn-danger");
        bDel.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold;");
        bDel.setOnAction(e -> doDeletePreset());

        Button bExp = new Button();
        bExp.textProperty().bind(i18n.createStringBinding("preset.export"));
        bExp.getStyleClass().add("btn-secondary");
        bExp.setOnAction(e -> doExport());

        Button bImp = new Button();
        bImp.textProperty().bind(i18n.createStringBinding("preset.import"));
        bImp.getStyleClass().add("btn-secondary");
        bImp.setOnAction(e -> doImport());

        r.getChildren().addAll(t, sp, lp, presetsCombo, bAdd, bDel,
            new Separator(Orientation.VERTICAL), bExp, bImp);
        v.getChildren().addAll(r, new Separator());
        return v;
    }

    private void doDeletePreset() {
        String sel = presetsCombo.getValue();
        if (sel == null || sel.isEmpty()) return;

        I18nManager i18n = I18nManager.getInstance();
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(i18n.get("preset.delete.title"));
        confirmAlert.setHeaderText("Supprimer le Preset Nid");
        confirmAlert.setContentText(String.format(i18n.get("preset.delete.confirm"), sel));

        confirmAlert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                presetMgr.delete(sel);
                refreshPresetsCombo();
                if (!presetsCombo.getItems().isEmpty()) {
                    presetsCombo.getSelectionModel().selectFirst();
                } else {
                    presetsCombo.getSelectionModel().clearSelection();
                }
                NotificationOverlay.show(this, "Preset nid supprimé.", NotificationOverlay.NotificationType.INFO);
            }
        });
    }

    private Button btn(String text, String bg) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:white;");
        return b;
    }

    // ── Config panel ──────────────────────────────────────────────────────────

    private void populateSpeciesModelCombo() {
        if (speciesModelCombo == null) return;
        isUpdatingSpeciesCombo = true;
        try {
            String curSel = speciesModelCombo.getValue();
            speciesModelCombo.getItems().clear();
            org.swarmforge.client.ui.SpeciesPresetManager mgr = new org.swarmforge.client.ui.SpeciesPresetManager();
            for (String pName : mgr.getPresetNames()) {
                org.swarmforge.core.species.CustomSpecies sp = mgr.getPreset(pName);
                String icon = "🐜";
                if (sp != null && sp.getInsectType() != null) {
                    String t = sp.getInsectType().toUpperCase();
                    if (t.contains("BEE") || t.contains("WASP")) icon = "🐝";
                    else if (t.contains("TERMITE")) icon = "🐜";
                }
                speciesModelCombo.getItems().add(icon + " " + pName);
            }
            speciesModelCombo.getItems().add("✨ Espèce Personnalisée Active");
            if (curSel != null && speciesModelCombo.getItems().contains(curSel)) {
                speciesModelCombo.setValue(curSel);
            } else if (!speciesModelCombo.getItems().isEmpty()) {
                speciesModelCombo.getSelectionModel().selectFirst();
            }
        } finally {
            isUpdatingSpeciesCombo = false;
        }
    }

    private ScrollPane buildConfig() {
        I18nManager i18n = I18nManager.getInstance();
        VBox cfg = new VBox(10);
        cfg.setPadding(new Insets(10));
        cfg.setPrefWidth(272);

        // Species Link Selector (Connection with Species Management)
        Label speciesTitle = new Label("🔗 Modèle d'Espèce de Référence");
        speciesTitle.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-padding:2 0 2 0;-fx-text-fill:#38bdf8;");

        speciesModelCombo = new ComboBox<>();
        populateSpeciesModelCombo();
        speciesModelCombo.setPrefWidth(218);
        speciesModelCombo.setOnAction(e -> {
            if (!isUpdatingSpeciesCombo) {
                onSpeciesModelSelected();
            }
        });

        Label stageTitle = new Label();
        stageTitle.textProperty().bind(i18n.createStringBinding("nest.species.age"));
        stageTitle.setStyle("-fx-font-size:11;-fx-font-weight:bold;-fx-text-fill:#94a3b8;-fx-padding:2 0 0 0;");

        nestStageCombo = new ComboBox<>();
        nestStageCombo.getItems().addAll(
            i18n.get("nest.species.age.foundation"),
            i18n.get("nest.species.age.established"),
            i18n.get("nest.species.age.mature"),
            i18n.get("nest.species.age.giant")
        );
        nestStageCombo.setPrefWidth(218);
        nestStageCombo.getSelectionModel().select(2); // Mature by default
        nestStageCombo.setOnAction(e -> {
            if (!isUpdatingSpeciesCombo) {
                onNestStageChanged();
            }
        });

        speciesStatusLabel = new Label("Nid synchronisé avec le modèle d'espèce.");
        speciesStatusLabel.setStyle("-fx-font-size:10;-fx-text-fill:#94a3b8;-fx-wrap-text:true;");

        VBox speciesBlock = new VBox(6,
            speciesTitle, new Separator(),
            speciesModelCombo,
            stageTitle, nestStageCombo,
            speciesStatusLabel
        );
        speciesBlock.getStyleClass().add("card-pane");

        // Nest Architecture & Species (non-collapsible block)
        Label archTitle = new Label();
        archTitle.textProperty().bind(i18n.createStringBinding("nest.arch.title"));
        archTitle.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-padding:2 0 2 0;");

        Label cl = new Label();
        cl.textProperty().bind(i18n.createStringBinding("nest.arch.category"));
        categorySelect = new ComboBox<>();
        categorySelect.getItems().addAll(
            "🐜 Ants (Formicidae)",
            "🐝 Honeybees (Apis)",
            "🐝 Bumblebees (Bombus)",
            "🐝 Wasps & Hornets (Vespidae)",
            "🐜 Termites (Isoptera)"
        );
        categorySelect.getSelectionModel().selectFirst();
        categorySelect.setPrefWidth(218);
        categorySelect.setOnAction(e -> {
            onCategoryChanged();
            onManualParameterChanged();
        });

        Label al = new Label();
        al.textProperty().bind(i18n.createStringBinding("nest.arch.type"));
        archSelect = new ComboBox<>();
        archSelect.getItems().addAll(
            "BURROW_UNDERGROUND", "SURFACE_MOUND", "WAX_COMB_HEXAGONAL",
            "WAX_POTS_CLUSTER", "PAPER_PEDUNCULATE", "CATHEDRAL_MOUND", "ARBOREAL_SILK_LEAF");
        archSelect.getSelectionModel().selectFirst();
        archSelect.setPrefWidth(218);
        archSelect.setOnAction(e -> {
            onManualParameterChanged();
            regen();
            repaint();
        });

        Label ml = new Label();
        ml.textProperty().bind(i18n.createStringBinding("nest.arch.material"));
        matSelect = new ComboBox<>();
        matSelect.getItems().addAll("EARTH", "WOOD_PULP_PAPER", "BEESWAX",
            "STERCORAL_CEMENT", "SILK_WEAVE", "PROPOLIS");
        matSelect.getSelectionModel().selectFirst();
        matSelect.setPrefWidth(218);
        matSelect.setOnAction(e -> {
            onManualParameterChanged();
            regen();
            repaint();
        });

        workerSizeSlider  = mkSlider(2.0, 30.0, 5.0);
        depthSlider       = mkSlider(4,  60, 20);
        tunnelWidthSlider = mkSlider(1,   5,  2);
        branchingSlider   = mkSlider(1,   5,  3);
        addLsn(workerSizeSlider, depthSlider, tunnelWidthSlider, branchingSlider);

        Label lblWorkerScale = new Label(); lblWorkerScale.textProperty().bind(i18n.createStringBinding("nest.arch.worker_scale"));
        Label lblMaxDepth = new Label(); lblMaxDepth.textProperty().bind(i18n.createStringBinding("nest.arch.max_depth"));
        Label lblTunnelWidth = new Label(); lblTunnelWidth.textProperty().bind(i18n.createStringBinding("nest.arch.tunnel_width"));
        Label lblBranching = new Label(); lblBranching.textProperty().bind(i18n.createStringBinding("nest.arch.branching"));

        VBox archBlock = new VBox(7,
            archTitle, new Separator(),
            cl, categorySelect,
            al, archSelect,
            ml, matSelect,
            lblWorkerScale,  sv(workerSizeSlider),
            lblMaxDepth, sv(depthSlider),
            lblTunnelWidth,       sv(tunnelWidthSlider),
            lblBranching,   sv(branchingSlider)
        );
        archBlock.getStyleClass().add("card-pane");

        // Chamber Distribution (non-collapsible)
        Label cdTitle = new Label();
        cdTitle.textProperty().bind(i18n.createStringBinding("nest.chambers.title"));
        cdTitle.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-padding:2 0 2 0;");

        Label lblTotalChambersTitle = new Label();
        lblTotalChambersTitle.textProperty().bind(i18n.createStringBinding("nest.chambers.total"));
        lblTotalChambersTitle.setStyle("-fx-font-weight:bold;");

        lblTotalChambersValue = new Label("11");
        lblTotalChambersValue.setStyle("-fx-font-size:14;-fx-font-weight:bold;");

        HBox totalChambersBox = new HBox(8, lblTotalChambersTitle, lblTotalChambersValue);
        totalChambersBox.setAlignment(Pos.CENTER_LEFT);
        totalChambersBox.setStyle("-fx-padding:2 0 4 0;");

        GridPane grid = new GridPane();
        grid.setHgap(8); grid.setVgap(7); grid.setPadding(new Insets(6));

        String[][] defs = {
            {"nest.chambers.queen", "1", "👑 Queen Chamber"},
            {"nest.chambers.brood", "3", "🥚 Brood Chambers"},
            {"nest.chambers.food", "4", "🍖 Food Storage"},
            {"nest.chambers.entrance", "2", "🚪 Entrances"},
            {"nest.chambers.waste", "1", "🗑 Waste Dumps"},
            {"nest.chambers.fungus", "0", "🍄 Fungus Gardens"}
        };
        int row = 0;
        for (String[] d : defs) {
            Spinner<Integer> s = new Spinner<>(0, 25, Integer.parseInt(d[1]));
            s.setPrefWidth(70); s.setEditable(true);
            s.valueProperty().addListener((o,a,b) -> {
                updateTotalChambers();
                onManualParameterChanged();
                regen();
                repaint();
            });
            chamberSpinners.put(d[2], s);

            Label chLbl = new Label();
            chLbl.textProperty().bind(i18n.createStringBinding(d[0]));
            grid.add(chLbl, 0, row);
            grid.add(s, 1, row++);
        }

        updateTotalChambers();

        VBox cdBlock = new VBox(5, cdTitle, new Separator(),
            totalChambersBox, grid);
        cdBlock.getStyleClass().add("card-pane");

        cfg.getChildren().addAll(speciesBlock, archBlock, cdBlock);
        ScrollPane sc = new ScrollPane(cfg);
        sc.setFitToWidth(true); sc.setPrefWidth(290); sc.setMaxWidth(290);
        return sc;
    }

    private void onCategoryChanged() {
        String cat = categorySelect.getValue();
        if (cat == null) return;
        if (cat.contains("Honeybees")) {
            archSelect.setValue("WAX_COMB_HEXAGONAL");
            matSelect.setValue("BEESWAX");
        } else if (cat.contains("Bumblebees")) {
            archSelect.setValue("WAX_POTS_CLUSTER");
            matSelect.setValue("PROPOLIS");
        } else if (cat.contains("Wasps")) {
            archSelect.setValue("PAPER_PEDUNCULATE");
            matSelect.setValue("WOOD_PULP_PAPER");
        } else if (cat.contains("Termites")) {
            archSelect.setValue("CATHEDRAL_MOUND");
            matSelect.setValue("STERCORAL_CEMENT");
        } else if (cat.contains("Ants")) {
            if (!archSelect.getValue().equals("ARBOREAL_SILK_LEAF") && !archSelect.getValue().equals("SURFACE_MOUND")) {
                archSelect.setValue("BURROW_UNDERGROUND");
            }
            matSelect.setValue("EARTH");
        }
        regen(); repaint();
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

    private VBox buildViews() {
        I18nManager i18n = I18nManager.getInstance();
        canvas3D   = new Canvas(540, 510); gc3D   = canvas3D.getGraphicsContext2D();
        canvasSide = new Canvas(215, 245); gcSide = canvasSide.getGraphicsContext2D();
        canvasTop  = new Canvas(215, 245); gcTop  = canvasTop.getGraphicsContext2D();

        setupMouse();

        StackPane h3d = new StackPane(canvas3D);
        h3d.setStyle("-fx-border-color:#555;-fx-border-width:1;");
        StackPane hSide = new StackPane(canvasSide);
        hSide.setStyle("-fx-border-color:#444;-fx-border-width:1;");
        StackPane hTop  = new StackPane(canvasTop);
        hTop.setStyle("-fx-border-color:#444;-fx-border-width:1;");

        Label ls = new Label();
        ls.textProperty().bind(i18n.createStringBinding("nest.view.side"));
        ls.setStyle("-fx-font-size:11;-fx-font-weight:bold;");

        Label lt = new Label();
        lt.textProperty().bind(i18n.createStringBinding("nest.view.top"));
        lt.setStyle("-fx-font-size:11;-fx-font-weight:bold;");

        VBox side = new VBox(5, ls, hSide, lt, hTop);
        side.setPadding(new Insets(0,4,0,8)); side.setAlignment(Pos.TOP_CENTER);

        HBox area = new HBox(6, h3d, side);
        area.setPadding(new Insets(8, 8, 4, 8));

        // UI Legend & Control Bar under canvas area
        HBox legendBar = buildLegendBar();

        return new VBox(4, area, legendBar);
    }

    private HBox buildLegendBar() {
        I18nManager i18n = I18nManager.getInstance();
        HBox bar = new HBox(10);
        bar.setPadding(new Insets(4, 10, 6, 10));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-border-color:#3f3f46;-fx-border-width:1 0 0 0;");

        syncViewsCheckBox = new CheckBox("🔗 Synchroniser les vues (Zoom & Panning)");
        syncViewsCheckBox.setSelected(true);
        syncViewsCheckBox.setStyle("-fx-text-fill:#00d4ff;-fx-font-weight:bold;-fx-font-size:11;");

        Label title = new Label();
        title.textProperty().bind(i18n.createStringBinding("nest.legend.title"));
        title.setStyle("-fx-font-weight:bold;-fx-text-fill:#aaa;-fx-font-size:11;");

        bar.getChildren().addAll(syncViewsCheckBox, new Separator(Orientation.VERTICAL), title);

        String[][] items = {
            {"nest.legend.entrance", "#32CD32"},
            {"nest.legend.queen", "#FFD700"},
            {"nest.legend.brood", "#00BFFF"},
            {"nest.legend.storage", "#FFA500"},
            {"nest.legend.fungus", "#9370DB"},
            {"nest.legend.waste", "#CD5C5C"},
            {"nest.legend.tunnel", "#708090"}
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

            Label lbl = new Label();
            lbl.textProperty().bind(i18n.createStringBinding(it[0]));
            lbl.setStyle("-fx-text-fill:#ccc;-fx-font-size:10;");
            item.getChildren().addAll(dot, lbl);
            bar.getChildren().add(item);
        }

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label hint = new Label();
        hint.textProperty().bind(i18n.createStringBinding("nest.legend.hint"));
        hint.setStyle("-fx-text-fill:#888;-fx-font-size:10;-fx-font-style:italic;");

        bar.getChildren().addAll(sp, hint);
        return bar;
    }

    private void setupMouse() {
        // 3D Canvas Orbit & Zoom & Pan
        canvas3D.setOnMousePressed(e -> { lastMX = e.getX(); lastMY = e.getY(); });
        canvas3D.setOnMouseDragged(e -> {
            double dx = e.getX() - lastMX;
            double dy = e.getY() - lastMY;
            if (e.isSecondaryButtonDown() || e.isShiftDown()) {
                // Pan 3D camera
                pan3DX += dx;
                pan3DY += dy;
                if (isSync()) {
                    sidePanX = pan3DX; sidePanY = pan3DY;
                    topPanX = pan3DX; topPanY = pan3DY;
                }
            } else {
                // Orbit 3D camera
                azimuth = (azimuth + dx * 0.65) % 360;
                if (azimuth < 0) azimuth += 360;
                elevation = Math.max(5, Math.min(85, elevation - dy * 0.35));
            }
            lastMX = e.getX(); lastMY = e.getY();
            repaint();
        });
        canvas3D.setOnScroll(e -> {
            zoom = Math.max(2.5, Math.min(22.0, zoom + e.getDeltaY() * 0.025));
            if (isSync()) {
                sideZoom = Math.max(0.3, Math.min(6.0, zoom / 7.5));
                topZoom = sideZoom;
            }
            repaint();
        });
        canvas3D.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                resetAllCameras();
            }
        });

        // 2D Side View Zoom & Pan
        canvasSide.setOnMousePressed(e -> { lastSideMX = e.getX(); lastSideMY = e.getY(); });
        canvasSide.setOnMouseDragged(e -> {
            double dx = e.getX() - lastSideMX;
            double dy = e.getY() - lastSideMY;
            sidePanX += dx;
            sidePanY += dy;
            if (isSync()) {
                topPanX = sidePanX; topPanY = sidePanY;
                pan3DX = sidePanX; pan3DY = sidePanY;
            }
            lastSideMX = e.getX(); lastSideMY = e.getY();
            repaint();
        });
        canvasSide.setOnScroll(e -> {
            sideZoom = Math.max(0.3, Math.min(6.0, sideZoom + e.getDeltaY() * 0.003));
            if (isSync()) {
                topZoom = sideZoom;
                zoom = Math.max(2.5, Math.min(22.0, sideZoom * 7.5));
            }
            repaint();
        });
        canvasSide.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                resetAllCameras();
            }
        });

        // 2D Top View Zoom & Pan
        canvasTop.setOnMousePressed(e -> { lastTopMX = e.getX(); lastTopMY = e.getY(); });
        canvasTop.setOnMouseDragged(e -> {
            double dx = e.getX() - lastTopMX;
            double dy = e.getY() - lastTopMY;
            topPanX += dx;
            topPanY += dy;
            if (isSync()) {
                sidePanX = topPanX; sidePanY = topPanY;
                pan3DX = topPanX; pan3DY = topPanY;
            }
            lastTopMX = e.getX(); lastTopMY = e.getY();
            repaint();
        });
        canvasTop.setOnScroll(e -> {
            topZoom = Math.max(0.3, Math.min(6.0, topZoom + e.getDeltaY() * 0.003));
            if (isSync()) {
                sideZoom = topZoom;
                zoom = Math.max(2.5, Math.min(22.0, topZoom * 7.5));
            }
            repaint();
        });
        canvasTop.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                resetAllCameras();
            }
        });
    }

    private boolean isSync() {
        return syncViewsCheckBox != null && syncViewsCheckBox.isSelected();
    }

    private void resetAllCameras() {
        azimuth = 45; elevation = 35; zoom = 7.5;
        pan3DX = 0; pan3DY = 0;
        sideZoom = 1.0; sidePanX = 0; sidePanY = 0;
        topZoom = 1.0; topPanX = 0; topPanY = 0;
        repaint();
    }

    // ── Preset helpers ────────────────────────────────────────────────────────

    private void refreshPresetsCombo() {
        String cur = presetsCombo.getValue();
        presetsCombo.getItems().setAll(presetMgr.names());
        if (cur != null) presetsCombo.setValue(cur);
    }

    private void doAddPreset() {
        TextInputDialog d = new TextInputDialog(presetsCombo.getValue() != null ? presetsCombo.getValue() : "Custom Nest Preset");
        d.setTitle("Save Preset"); d.setHeaderText("Nom du preset :"); d.setContentText("Nom :");
        d.showAndWait().ifPresent(name -> {
            if (name == null || name.isBlank()) return;
            presetMgr.save(name, getConfiguration());
            refreshPresetsCombo();
            presetsCombo.setValue(name);
            new Alert(Alert.AlertType.INFORMATION, "Preset \"" + name + "\" sauvegardé.").show();
        });
    }

    private void updateTotalChambers() {
        int total = chamberSpinners.values().stream().mapToInt(Spinner::getValue).sum();
        if (lblTotalChambersValue != null) {
            lblTotalChambersValue.setText(String.valueOf(total));
        }
    }

    private void onManualParameterChanged() {
        if (isUpdatingSpeciesCombo) return;
        isUpdatingSpeciesCombo = true;
        try {
            if (speciesModelCombo != null) {
                speciesModelCombo.setValue("✨ Espèce Personnalisée Active");
            }
            if (speciesStatusLabel != null) {
                speciesStatusLabel.setText("Paramètres du nid modifiés (Nid sur mesure).");
            }
        } finally {
            isUpdatingSpeciesCombo = false;
        }
    }

    private void applyCfg(Map<String, Object> c) {
        isUpdatingSpeciesCombo = true;
        try {
            if (c.containsKey("taxonCategory")) categorySelect.setValue((String) c.get("taxonCategory"));
            if (c.containsKey("architecture"))  archSelect.setValue((String) c.get("architecture"));
            if (c.containsKey("material"))      matSelect.setValue((String) c.get("material"));
            if (c.containsKey("workerSizeMm"))  workerSizeSlider.setValue(num(c, "workerSizeMm"));
            if (c.containsKey("depth"))         depthSlider.setValue(num(c,"depth"));
            if (c.containsKey("tunnelWidth"))   tunnelWidthSlider.setValue(num(c,"tunnelWidth"));
            if (c.containsKey("branching"))     branchingSlider.setValue(num(c,"branching"));
            if (c.containsKey("nestStageIndex") && nestStageCombo != null) {
                int stg = ((Number) c.get("nestStageIndex")).intValue();
                if (stg >= 0 && stg < nestStageCombo.getItems().size()) {
                    nestStageCombo.getSelectionModel().select(stg);
                }
            }
            if (c.containsKey("chamberDistribution")) {
                @SuppressWarnings("unchecked")
                Map<String,Object> dist = (Map<String,Object>) c.get("chamberDistribution");
                dist.forEach((k,v) -> setSp(k, ((Number)v).intValue()));
            }
            updateTotalChambers();
        } finally {
            isUpdatingSpeciesCombo = false;
        }
        regen();
        repaint();
    }

    public void configureFromSpecies(org.swarmforge.core.species.CustomSpecies species) {
        if (species == null) return;
        this.activeCustomSpecies = species;

        isUpdatingSpeciesCombo = true;
        try {
            // 1. Insect Category & Material & Architecture mapping
            String orderStr = species.getInsectType() != null ? species.getInsectType() : "ANT";
            String nestTypeStr = species.getNestType() != null ? species.getNestType() : "MATURE";

            if ("TERMITE".equalsIgnoreCase(orderStr)) {
                categorySelect.setValue("🐜 Termites (Isoptera)");
                archSelect.setValue("CATHEDRAL_MOUND");
                matSelect.setValue("STERCORAL_CEMENT");
            } else if ("BEE".equalsIgnoreCase(orderStr)) {
                if (species.getCommonName().toLowerCase().contains("bourdon") || (species.getScientificName() != null && species.getScientificName().toLowerCase().contains("bombus"))) {
                    categorySelect.setValue("🐝 Bumblebees (Bombus)");
                    archSelect.setValue("WAX_POTS_CLUSTER");
                    matSelect.setValue("PROPOLIS");
                } else {
                    categorySelect.setValue("🐝 Honeybees (Apis)");
                    archSelect.setValue("WAX_COMB_HEXAGONAL");
                    matSelect.setValue("BEESWAX");
                }
            } else if ("WASP".equalsIgnoreCase(orderStr)) {
                categorySelect.setValue("🐝 Wasps & Hornets (Vespidae)");
                archSelect.setValue("PAPER_PEDUNCULATE");
                matSelect.setValue("WOOD_PULP_PAPER");
            } else {
                // ANT
                categorySelect.setValue("🐜 Ants (Formicidae)");
                if ("ARBOREAL_SILK_LEAF".equalsIgnoreCase(nestTypeStr) || (species.getCommonName() != null && species.getCommonName().toLowerCase().contains("tisserande"))) {
                    archSelect.setValue("ARBOREAL_SILK_LEAF");
                    matSelect.setValue("SILK_WEAVE");
                } else if ("MOUND".equalsIgnoreCase(nestTypeStr) || (species.getCommonName() != null && (species.getCommonName().toLowerCase().contains("fire") || species.getCommonName().toLowerCase().contains("feu")))) {
                    archSelect.setValue("SURFACE_MOUND");
                    matSelect.setValue("EARTH");
                } else {
                    archSelect.setValue("BURROW_UNDERGROUND");
                    matSelect.setValue("EARTH");
                }
            }

            // 2. Body length & Tunnel diameter
            float avgBodyMm = species.getAverageCasteBodyLengthMm();
            workerSizeSlider.setValue(Math.max(2.0, Math.min(30.0, avgBodyMm)));

            float reqTunnelMm = species.getRequiredTunnelDiameterMm();
            tunnelWidthSlider.setValue(Math.max(1.0, Math.min(5.0, Math.round(reqTunnelMm / 1.5))));

            // 3. Nest Development Stage Multipliers (Fondation, Établi, Mature, Vieux Nid)
            int stageIndex = nestStageCombo != null ? nestStageCombo.getSelectionModel().getSelectedIndex() : 2;
            if (stageIndex < 0) stageIndex = 2; // Default MATURE

            double popMultiplier = 1.0;
            double depthMultiplier = 1.0;
            int branchingVal = 3;

            switch (stageIndex) {
                case 0: // FOUNDATION
                    popMultiplier = 0.05;
                    depthMultiplier = 0.45;
                    branchingVal = 1;
                    break;
                case 1: // ESTABLISHED
                    popMultiplier = 0.25;
                    depthMultiplier = 0.75;
                    branchingVal = 2;
                    break;
                case 2: // MATURE
                default:
                    popMultiplier = 1.0;
                    depthMultiplier = 1.0;
                    branchingVal = 3;
                    break;
                case 3: // GIANT
                    popMultiplier = 2.5;
                    depthMultiplier = 1.35;
                    branchingVal = 4;
                    break;
            }

            branchingSlider.setValue(branchingVal);

            int popSize = Math.max(10, (int) (species.getTypicalColonySize() * popMultiplier));
            double depth = Math.max(4, Math.min(60, Math.log10(popSize + 10) * 10 * depthMultiplier));
            depthSlider.setValue(depth);

            // 4. Chamber distribution according to species biology and nest stage
            int queenCount = species.getQueenCount();
            setSp("👑 Queen Chamber", Math.max(1, Math.min(25, stageIndex == 0 ? 1 : queenCount)));

            boolean isFungusGrower = "FUNGUS".equalsIgnoreCase(species.getPrimaryDiet()) || 
                                     "FUNGUS".equalsIgnoreCase(species.getSecondaryDiet()) ||
                                     (species.getCommonName() != null && (species.getCommonName().toLowerCase().contains("champignonniste") ||
                                     species.getCommonName().toLowerCase().contains("atta")));

            int fungusCount = 0;
            if (isFungusGrower) {
                if (stageIndex == 0) fungusCount = 1;
                else if (stageIndex == 1) fungusCount = 3;
                else if (stageIndex == 2) fungusCount = 6;
                else fungusCount = 12;
            }
            setSp("🍄 Fungus Gardens", fungusCount);

            int broodCount = Math.max(1, (int) (Math.sqrt(popSize / 200.0) * (stageIndex == 0 ? 0.5 : stageIndex == 3 ? 1.8 : 1.0)));
            if (stageIndex == 0) broodCount = 1;
            setSp("🥚 Brood Chambers", Math.max(1, Math.min(25, broodCount)));

            int foodCount = Math.max(1, (int) (Math.sqrt(popSize / 300.0) * (stageIndex == 0 ? 0.5 : stageIndex == 3 ? 1.8 : 1.0)));
            if (stageIndex == 0) foodCount = 1;
            setSp("🍖 Food Storage", Math.max(1, Math.min(25, foodCount)));

            int entranceCount = stageIndex == 0 ? 1 : stageIndex == 1 ? 2 : stageIndex == 2 ? 2 : 4;
            setSp("🚪 Entrances", entranceCount);

            int wasteCount = stageIndex == 0 ? 1 : stageIndex == 1 ? 1 : stageIndex == 2 ? 2 : 4;
            setSp("🗑 Waste Dumps", wasteCount);

            updateTotalChambers();

            if (speciesStatusLabel != null) {
                speciesStatusLabel.setText("Nid synchronisé (" + species.getCommonName() + ")");
            }
            if (speciesModelCombo != null) {
                String matchedItem = null;
                for (String item : speciesModelCombo.getItems()) {
                    String cleanItem = item.replaceAll("^[🐜🐝✨]\\s*", "").trim();
                    if (cleanItem.equalsIgnoreCase(species.getPresetName()) ||
                        cleanItem.equalsIgnoreCase(species.getCommonName()) ||
                        (species.getCommonName() != null && cleanItem.toLowerCase().contains(species.getCommonName().toLowerCase())) ||
                        (species.getPresetName() != null && cleanItem.toLowerCase().contains(species.getPresetName().toLowerCase()))) {
                        matchedItem = item;
                        break;
                    }
                }
                if (matchedItem != null) {
                    speciesModelCombo.setValue(matchedItem);
                } else {
                    speciesModelCombo.setValue("✨ Espèce Personnalisée Active");
                }
            }
        } finally {
            isUpdatingSpeciesCombo = false;
        }

        regen();
        repaint();
    }

    private void onSpeciesModelSelected() {
        String sel = speciesModelCombo.getValue();
        if (sel == null) return;

        if (sel.contains("Espèce Personnalisée Active")) {
            if (activeCustomSpecies != null) {
                configureFromSpecies(activeCustomSpecies);
            } else if (speciesStatusLabel != null) {
                speciesStatusLabel.setText("Nid sur mesure (Paramètres personnalisés).");
            }
            return;
        }

        org.swarmforge.client.ui.SpeciesPresetManager presetManager = new org.swarmforge.client.ui.SpeciesPresetManager();
        String cleanSel = sel.replaceAll("^[🐜🐝✨]\\s*", "").trim();
        for (String presetName : presetManager.getPresetNames()) {
            if (cleanSel.equalsIgnoreCase(presetName) || cleanSel.toLowerCase().contains(presetName.toLowerCase()) || presetName.toLowerCase().contains(cleanSel.toLowerCase())) {
                configureFromSpecies(presetManager.getPreset(presetName));
                return;
            }
        }
    }

    private void onNestStageChanged() {
        if (activeCustomSpecies != null) {
            configureFromSpecies(activeCustomSpecies);
        } else {
            String sel = speciesModelCombo.getValue();
            if (sel != null && !sel.contains("Espèce Personnalisée Active")) {
                onSpeciesModelSelected();
            }
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
            applyCfg(cfg);
        } catch (Exception ex) { new Alert(Alert.AlertType.ERROR, ex.getMessage()).show(); }
    }

    private void applyToWorld() {
        if (onApplyCallback != null) onApplyCallback.accept(getConfiguration());
        else new Alert(Alert.AlertType.WARNING,"No world editor connected.").show();
    }

    public void setOnApply(Consumer<Map<String,Object>> cb) { this.onApplyCallback = cb; }

    public Map<String,Object> getConfiguration() {
        Map<String,Object> c = new LinkedHashMap<>();
        c.put("presetName",   presetsCombo.getValue() != null ? presetsCombo.getValue() : "Custom");
        c.put("taxonCategory",categorySelect.getValue());
        c.put("architecture", archSelect.getValue());
        c.put("material",     matSelect.getValue());
        c.put("workerSizeMm", workerSizeSlider.getValue());
        c.put("depth",        (int) depthSlider.getValue());
        c.put("chamberCount", (int) getChamberCount());
        c.put("tunnelWidth",  (int) tunnelWidthSlider.getValue());
        c.put("branching",    (int) branchingSlider.getValue());
        c.put("nestStageIndex", nestStageCombo != null ? nestStageCombo.getSelectionModel().getSelectedIndex() : 2);
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
    double getChamberCount() { return chamberSpinners.values().stream().mapToInt(Spinner::getValue).sum(); }
    int    sp(String k)      { return getSp(k); }

    // ── Drawing delegates ─────────────────────────────────────────────────────

    private void draw3D()   { NestRenderer.draw3D(nest, gc3D, canvas3D.getWidth(), canvas3D.getHeight(), azimuth, elevation, zoom, getTunnelWidth(), pan3DX, pan3DY); }
    private void drawSide() { NestRenderer.drawSide(nest, gcSide, canvasSide.getWidth(), canvasSide.getHeight(), getTunnelWidth(), sideZoom, sidePanX, sidePanY); }
    private void drawTop()  { NestRenderer.drawTop(nest,  gcTop,  canvasTop.getWidth(),  canvasTop.getHeight(),  getTunnelWidth(), topZoom, topPanX, topPanY); }

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

