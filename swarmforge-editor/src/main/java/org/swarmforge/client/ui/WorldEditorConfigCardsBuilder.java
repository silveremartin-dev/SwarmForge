/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import java.util.Locale;
import java.util.Random;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.client.util.I18nManager;

import static org.swarmforge.client.ui.WorldEditorPane.GRID_SIZE;
import static org.swarmforge.client.ui.WorldEditorPane.SOIL_DEPTH;

/**
 * Builds the left configuration sidebar cards (scale, relief, soil, hydrology,
 * flora, structures, ecological diagnostics, species adaptation, and sculpting brushes)
 * for {@link WorldEditorPane}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
final class WorldEditorConfigCardsBuilder {

    private WorldEditorConfigCardsBuilder() {
    }

    static VBox makeCard(ObservableValue<String> titleBinding, VBox content) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(10));
        card.getStyleClass().add("card-pane");
        Label lblHeader = new Label();
        lblHeader.textProperty().bind(titleBinding);
        lblHeader.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        lblHeader.getStyleClass().add("accent-title");
        card.getChildren().addAll(lblHeader, new Separator(), content);
        return card;
    }

    static ScrollPane buildConfig(WorldEditorPane pane) {
        VBox cfg = new VBox(12);
        cfg.setPadding(new Insets(10));
        cfg.setPrefWidth(420);
        cfg.setMinWidth(400);
        cfg.setStyle("-fx-background-color: transparent;");

        I18nManager i18n = I18nManager.getInstance();
        VBox cardSource = makeCard(i18n.createStringBinding("world.card.geo"), WorldEditorGeoAndBioclimateHelper.buildTerrainSourceBlock(pane));
        VBox cardScale  = makeCard(i18n.createStringBinding("world.card.scale"), buildScaleBlock(pane));
        VBox cardRelief = makeCard(i18n.createStringBinding("world.card.relief"), buildReliefBlock(pane));
        VBox cardSoil   = makeCard(i18n.createStringBinding("world.card.soil"), buildSoilBlock(pane));
        VBox cardHydro  = makeCard(i18n.createStringBinding("world.card.hydro"), buildHydroBlock(pane));
        VBox cardFlora  = makeCard(i18n.createStringBinding("world.card.flora"), buildFloraBlock(pane));
        VBox cardStruct = makeCard(i18n.createStringBinding("world.card.struct"), buildStructBlock(pane));
        VBox cardDiag   = makeCard(i18n.createStringBinding("world.card.diag"), buildDiagBlock(pane));
        VBox cardAdapt  = buildAdaptBlock(pane);

        cfg.getChildren().addAll(cardSource, cardScale, cardRelief, cardSoil, cardHydro, cardFlora, cardStruct, cardDiag, cardAdapt);

        ScrollPane sc = new ScrollPane(cfg);
        sc.setFitToWidth(true);
        sc.setPrefWidth(435);
        sc.setMinWidth(415);
        return sc;
    }

    static VBox buildScaleBlock(WorldEditorPane pane) {
        pane.scaleSeedField = new TextField("384729");
        pane.scaleSeedField.setPrefWidth(90);
        pane.scaleSeedField.setOnAction(e -> pane.regenerateAndRepaint());
        pane.scaleSeedField.textProperty().addListener((o, a, b) -> pane.regenerateAndRepaint());

        Button btnRandomScaleSeed = new Button("🎲 Seed");
        btnRandomScaleSeed.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
        btnRandomScaleSeed.setOnAction(e -> {
            pane.scaleSeedField.setText(String.valueOf(new Random().nextLong(100000, 9999999)));
            pane.regenerateAndRepaint();
        });

        pane.surfaceSizeSlider = pane.mkSlider(0.5, 100.0, 25.0);
        pane.surfaceSizeSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.dim.side_length.tt"));
        pane.depthSlider = pane.mkSlider(0.2, 10.0, 3.0);
        pane.depthSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.dim.depth.tt"));
        pane.resolutionSlider = pane.mkSlider(0.1, 1.0, 0.5);
        pane.resolutionSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.dim.resolution.tt"));

        pane.surfaceSizeSlider.valueProperty().addListener((o, a, b) -> {
            updateVoxelMemoryEstimate(pane);
            pane.regenerateAndRepaint();
        });
        pane.depthSlider.valueProperty().addListener((o, a, b) -> {
            updateVoxelMemoryEstimate(pane);
            pane.regenerateAndRepaint();
        });
        pane.resolutionSlider.valueProperty().addListener((o, a, b) -> {
            updateVoxelMemoryEstimate(pane);
            pane.regenerateAndRepaint();
        });

        pane.lblVoxelMemoryEstimate = new Label("📊 Voxel Estimate : ~0.5M voxels (32MB)");
        pane.lblVoxelMemoryEstimate.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
        pane.lblVoxelMemoryEstimate.getStyleClass().add("accent-title");
        pane.lblVoxelMemoryEstimate.setWrapText(true);
        pane.lblVoxelMemoryEstimate.setMinHeight(Region.USE_PREF_SIZE);

        updateVoxelMemoryEstimate(pane);

        Label lblSideLength = new Label(); lblSideLength.textProperty().bind(I18nManager.getInstance().createStringBinding("world.dim.side_length"));
        Label lblDepth = new Label(); lblDepth.textProperty().bind(I18nManager.getInstance().createStringBinding("world.dim.depth"));
        Label lblRes = new Label(); lblRes.textProperty().bind(I18nManager.getInstance().createStringBinding("world.dim.resolution"));

        return new VBox(8,
                lblSideLength, pane.sv(pane.surfaceSizeSlider, "m"),
                lblDepth, pane.sv(pane.depthSlider, "m"),
                lblRes, pane.sv(pane.resolutionSlider, "mm"),
                pane.lblVoxelMemoryEstimate
        );
    }

    static void updateVoxelMemoryEstimate(WorldEditorPane pane) {
        if (pane.lblVoxelMemoryEstimate == null) return;
        double sideM = pane.surfaceSizeSlider != null ? pane.surfaceSizeSlider.getValue() : 2.0;

        if (sideM > 5.0) {
            long lodVoxels = (long) (GRID_SIZE * GRID_SIZE * SOIL_DEPTH * (1.0 + Math.log(sideM)));
            double lodMb = lodVoxels * 4.0 / (1024.0 * 1024.0);
            pane.lblVoxelMemoryEstimate.setText(String.format(Locale.US, "⚡ SVO Multi-LOD Active : ~%.1fM effective voxels (%.1f MB RAM)", lodVoxels / 1_000_000.0, lodMb));
            pane.lblVoxelMemoryEstimate.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #16a34a;");
        } else {
            double rawMb = (GRID_SIZE * GRID_SIZE * SOIL_DEPTH * 4.0) / (1024.0 * 1024.0);
            pane.lblVoxelMemoryEstimate.setText(String.format(Locale.US, "📊 Voxel Macro 128x128x32 : %.1f MB RAM", rawMb));
            pane.lblVoxelMemoryEstimate.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
            pane.lblVoxelMemoryEstimate.getStyleClass().add("accent-title");
        }
    }

    static VBox buildReliefBlock(WorldEditorPane pane) {
        pane.reliefSeedField = new TextField("774829");
        pane.reliefSeedField.setPrefWidth(90);
        pane.reliefSeedField.setOnAction(e -> pane.regenerateAndRepaint());
        pane.reliefSeedField.textProperty().addListener((o, a, b) -> pane.regenerateAndRepaint());

        Button btnRandomRelief = new Button("🎲 Seed");
        btnRandomRelief.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
        btnRandomRelief.setOnAction(e -> {
            pane.reliefSeedField.setText(String.valueOf(new Random().nextLong(100000, 9999999)));
            pane.regenerateAndRepaint();
        });

        Label lblSeedRelief = new Label();
        lblSeedRelief.textProperty().bind(I18nManager.getInstance().createStringBinding("world.seed.relief"));
        lblSeedRelief.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;");
        HBox seedBox = new HBox(6, lblSeedRelief, pane.reliefSeedField, btnRandomRelief);
        seedBox.setAlignment(Pos.CENTER_LEFT);

        pane.roughnessSlider = pane.mkSlider(0.0, 1.0, 0.45);
        pane.roughnessSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.relief.perlin.tt"));
        pane.compactionSlider = pane.mkSlider(10.0, 100.0, 65.0);
        pane.compactionSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.soil.compaction.tt"));

        pane.roughnessSlider.valueProperty().addListener((o, a, b) -> pane.regenerateAndRepaint());
        pane.compactionSlider.valueProperty().addListener((o, a, b) -> pane.regenerateAndRepaint());

        pane.slopeAngleSlider = pane.mkSlider(0.0, 200.0, 0.0);
        pane.slopeAngleSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.relief.slope_angle.tt"));
        pane.slopeAngleSlider.valueProperty().addListener((o, a, b) -> pane.regenerateAndRepaint());

        pane.slopeDirectionCombo = new ComboBox<>();
        pane.slopeDirectionCombo.getItems().addAll("East ➔ West", "North ➔ South", "South ➔ North", "West ➔ East");
        pane.slopeDirectionCombo.getSelectionModel().selectFirst();
        pane.slopeDirectionCombo.valueProperty().addListener((o, a, b) -> pane.regenerateAndRepaint());

        Label lblSlopeDir = new Label();
        lblSlopeDir.textProperty().bind(I18nManager.getInstance().createStringBinding("world.relief.slope_dir"));
        lblSlopeDir.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;");
        HBox slopeRow = new HBox(6, lblSlopeDir, pane.slopeDirectionCombo);
        slopeRow.setAlignment(Pos.CENTER_LEFT);

        VBox sculptSubBlock = buildSculptBlock(pane);

        Label lblReliefPerlin = new Label(); lblReliefPerlin.textProperty().bind(I18nManager.getInstance().createStringBinding("world.relief.perlin"));
        Label lblCompaction = new Label(); lblCompaction.textProperty().bind(I18nManager.getInstance().createStringBinding("world.soil.compaction"));
        Label lblSlopeAngle = new Label(); lblSlopeAngle.textProperty().bind(I18nManager.getInstance().createStringBinding("world.relief.slope_angle"));

        return new VBox(8,
                seedBox,
                lblReliefPerlin, pane.sv(pane.roughnessSlider, ""),
                lblCompaction, pane.sv(pane.compactionSlider, "%"),
                lblSlopeAngle, pane.sv(pane.slopeAngleSlider, "%"),
                slopeRow,
                new Separator(),
                sculptSubBlock
        );
    }

    static VBox buildSoilBlock(WorldEditorPane pane) {
        pane.soilSeedField = new TextField("123456");
        pane.soilSeedField.setPrefWidth(90);
        pane.soilSeedField.setOnAction(e -> pane.regenerateAndRepaint());
        pane.soilSeedField.textProperty().addListener((o, a, b) -> pane.regenerateAndRepaint());

        Button btnRandomSoil = new Button("🎲 Seed");
        btnRandomSoil.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
        btnRandomSoil.setOnAction(e -> {
            pane.soilSeedField.setText(String.valueOf(new Random().nextLong(100000, 9999999)));
            pane.regenerateAndRepaint();
        });

        Label lblSeedSoil = new Label();
        lblSeedSoil.textProperty().bind(I18nManager.getInstance().createStringBinding("world.seed.soil"));
        lblSeedSoil.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;");
        HBox seedBox = new HBox(6, lblSeedSoil, pane.soilSeedField, btnRandomSoil);
        seedBox.setAlignment(Pos.CENTER_LEFT);

        pane.earthSpinner   = pane.mkSpinner(0, 100, 40);
        pane.sandSpinner    = pane.mkSpinner(0, 100, 20);
        pane.siltSpinner    = pane.mkSpinner(0, 100, 15);
        pane.claySpinner    = pane.mkSpinner(0, 100, 15);
        pane.gravelSpinner  = pane.mkSpinner(0, 100, 5);
        pane.peatSpinner    = pane.mkSpinner(0, 100, 0);
        pane.stoneSpinner   = pane.mkSpinner(0, 100, 5);
        pane.organicSpinner = pane.mkSpinner(0, 100, 0);

        for (Spinner<Integer> sp : new Spinner[]{pane.earthSpinner, pane.sandSpinner, pane.siltSpinner, pane.claySpinner, pane.gravelSpinner, pane.peatSpinner, pane.stoneSpinner, pane.organicSpinner}) {
            sp.valueProperty().addListener((o, a, b) -> pane.regenerateAndRepaint());
        }

        GridPane grid = new GridPane();
        grid.setHgap(8); grid.setVgap(6);

        Label lblHumus  = new Label(); lblHumus.textProperty().bind(I18nManager.getInstance().createStringBinding("world.sub.humus_pct"));
        Label lblSand   = new Label(); lblSand.textProperty().bind(I18nManager.getInstance().createStringBinding("world.sub.sand_pct"));
        Label lblSilt   = new Label(); lblSilt.textProperty().bind(I18nManager.getInstance().createStringBinding("world.sub.silt_pct"));
        Label lblClay   = new Label(); lblClay.textProperty().bind(I18nManager.getInstance().createStringBinding("world.sub.clay_pct"));
        Label lblGravel = new Label(); lblGravel.textProperty().bind(I18nManager.getInstance().createStringBinding("world.sub.gravel_pct"));
        Label lblPeat   = new Label(); lblPeat.textProperty().bind(I18nManager.getInstance().createStringBinding("world.sub.peat_pct"));
        Label lblRock   = new Label(); lblRock.textProperty().bind(I18nManager.getInstance().createStringBinding("world.sub.rock_pct"));
        Label lblLitter = new Label(); lblLitter.textProperty().bind(I18nManager.getInstance().createStringBinding("world.sub.litter_pct"));

        grid.add(lblHumus, 0, 0);  grid.add(pane.earthSpinner, 1, 0);
        grid.add(lblSand, 0, 1);   grid.add(pane.sandSpinner, 1, 1);
        grid.add(lblSilt, 0, 2);   grid.add(pane.siltSpinner, 1, 2);
        grid.add(lblClay, 0, 3);   grid.add(pane.claySpinner, 1, 3);
        grid.add(lblGravel, 0, 4); grid.add(pane.gravelSpinner, 1, 4);
        grid.add(lblPeat, 0, 5);   grid.add(pane.peatSpinner, 1, 5);
        grid.add(lblRock, 0, 6);   grid.add(pane.stoneSpinner, 1, 6);
        grid.add(lblLitter, 0, 7); grid.add(pane.organicSpinner, 1, 7);

        pane.stratificationSlider = pane.mkSlider(0.0, 1.0, 0.7);
        pane.stratificationSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.soil.stratification.tt"));
        pane.mixingRateSlider     = pane.mkSlider(0.0, 1.0, 0.3);
        pane.mixingRateSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.soil.mixing.tt"));
        pane.baseHumiditySlider   = pane.mkSlider(0.0, 1.0, 0.35);
        pane.baseHumiditySlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.soil.base_humidity.tt"));
        pane.voidDensitySlider    = pane.mkSlider(0.0, 0.3, 0.08);
        pane.voidDensitySlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.soil.void_density.tt"));
        pane.basePhSlider         = pane.mkSlider(4.0, 9.0, 6.5);
        pane.basePhSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.soil.base_ph.tt"));
        pane.rootDensitySlider    = pane.mkSlider(0.0, 100.0, 40.0);
        pane.rootDensitySlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.soil.root_density.tt"));

        pane.stratificationSlider.valueProperty().addListener((o, a, b) -> pane.regenerateAndRepaint());
        pane.mixingRateSlider.valueProperty().addListener((o, a, b) -> pane.regenerateAndRepaint());
        pane.baseHumiditySlider.valueProperty().addListener((o, a, b) -> pane.regenerateAndRepaint());
        pane.voidDensitySlider.valueProperty().addListener((o, a, b) -> pane.regenerateAndRepaint());
        pane.basePhSlider.valueProperty().addListener((o, a, b) -> pane.regenerateAndRepaint());
        pane.rootDensitySlider.valueProperty().addListener((o, a, b) -> pane.regenerateAndRepaint());

        Label lblStrat    = new Label(); lblStrat.textProperty().bind(I18nManager.getInstance().createStringBinding("world.soil.stratification"));
        Label lblMixing   = new Label(); lblMixing.textProperty().bind(I18nManager.getInstance().createStringBinding("world.soil.mixing"));
        Label lblBaseHum  = new Label(); lblBaseHum.textProperty().bind(I18nManager.getInstance().createStringBinding("world.soil.base_humidity"));
        Label lblVoid     = new Label(); lblVoid.textProperty().bind(I18nManager.getInstance().createStringBinding("world.soil.void_density"));
        Label lblBasePh   = new Label(); lblBasePh.textProperty().bind(I18nManager.getInstance().createStringBinding("world.soil.base_ph"));
        Label lblRootDens = new Label(); lblRootDens.textProperty().bind(I18nManager.getInstance().createStringBinding("world.soil.root_density"));

        return new VBox(8,
                seedBox,
                grid,
                new Separator(),
                lblStrat, pane.svPct(pane.stratificationSlider),
                lblMixing, pane.svPct(pane.mixingRateSlider),
                lblBaseHum, pane.svPct(pane.baseHumiditySlider),
                lblVoid, pane.svPct(pane.voidDensitySlider),
                lblBasePh, pane.sv(pane.basePhSlider, " pH"),
                lblRootDens, pane.sv(pane.rootDensitySlider, "%")
        );
    }

    static VBox buildFloraBlock(WorldEditorPane pane) {
        pane.floraSeedField = new TextField("774829");
        pane.floraSeedField.setPrefWidth(95);
        pane.floraSeedField.setOnAction(e -> pane.regenerateAndRepaint());
        pane.floraSeedField.textProperty().addListener((o, a, b) -> pane.regenerateAndRepaint());

        Button btnRandomizeFloraSeed = new Button("🎲 Seed");
        btnRandomizeFloraSeed.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
        btnRandomizeFloraSeed.setOnAction(e -> {
            long newSeed = new Random().nextLong(100000, 9999999);
            pane.floraSeedField.setText(String.valueOf(newSeed));
            pane.regenerateAndRepaint();
        });

        Label lblSeedFlora = new Label();
        lblSeedFlora.textProperty().bind(I18nManager.getInstance().createStringBinding("world.seed.flora"));
        lblSeedFlora.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;");
        HBox seedBox = new HBox(6, lblSeedFlora, pane.floraSeedField, btnRandomizeFloraSeed);
        seedBox.setAlignment(Pos.CENTER_LEFT);

        pane.edibleDensitySlider = pane.mkSlider(0, 100, 40);
        pane.edibleDensitySlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.flora.edible_density.tt"));
        pane.nonEdibleDensitySlider = pane.mkSlider(0, 100, 60);
        pane.nonEdibleDensitySlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.flora.nonedible_density.tt"));
        pane.leafLitterSlider = pane.mkSlider(0, 100, 50);
        pane.leafLitterSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.flora.leaf_litter.tt"));
        pane.twigDebrisSlider = pane.mkSlider(0, 100, 40);
        pane.twigDebrisSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.flora.twig_debris.tt"));
        pane.addFloraLsn(pane.edibleDensitySlider, pane.nonEdibleDensitySlider, pane.leafLitterSlider, pane.twigDebrisSlider);

        pane.aphidPlantCheck    = new CheckBox("🟢 Cirsium / Vicia (Aphid Host)"); pane.aphidPlantCheck.setSelected(true);
        pane.nectarFlowersCheck = new CheckBox("🌸 Nectar Flowers");                  pane.nectarFlowersCheck.setSelected(true);
        pane.seedGrassCheck     = new CheckBox("🌾 Grasses & Caryopses");            pane.seedGrassCheck.setSelected(true);
        pane.fungusFoliageCheck = new CheckBox("🍃 Leafcutter Foliage (Atta)");      pane.fungusFoliageCheck.setSelected(false);
        pane.mossCheck          = new CheckBox("🟢 Polytrichum Moss Cover");          pane.mossCheck.setSelected(true);
        pane.pineLitterCheck    = new CheckBox("🍂 Pine Needle Litter Layer");       pane.pineLitterCheck.setSelected(true);
        pane.fernObstacleCheck  = new CheckBox("🌿 Fern Understory Obstacles");       pane.fernObstacleCheck.setSelected(true);

        pane.addFloraBoolLsn(pane.aphidPlantCheck, pane.nectarFlowersCheck, pane.seedGrassCheck, pane.fungusFoliageCheck,
                       pane.mossCheck, pane.pineLitterCheck, pane.fernObstacleCheck);

        Label lblEdibleHeader  = new Label(); lblEdibleHeader.textProperty().bind(I18nManager.getInstance().createStringBinding("world.flora.edible_header"));
        Label lblEdibleDens    = new Label(); lblEdibleDens.textProperty().bind(I18nManager.getInstance().createStringBinding("world.flora.edible_density"));
        Label lblCoverHeader   = new Label(); lblCoverHeader.textProperty().bind(I18nManager.getInstance().createStringBinding("world.flora.cover_header"));
        Label lblLeafLitter    = new Label(); lblLeafLitter.textProperty().bind(I18nManager.getInstance().createStringBinding("world.flora.leaf_litter"));
        Label lblTwigDebris    = new Label(); lblTwigDebris.textProperty().bind(I18nManager.getInstance().createStringBinding("world.flora.twig_debris"));
        Label lblNonEdibleDens = new Label(); lblNonEdibleDens.textProperty().bind(I18nManager.getInstance().createStringBinding("world.flora.nonedible_density"));

        return new VBox(6,
                seedBox,
                new Separator(),
                lblEdibleHeader,
                lblEdibleDens, pane.sv(pane.edibleDensitySlider, "%"),
                pane.aphidPlantCheck, pane.nectarFlowersCheck, pane.seedGrassCheck, pane.fungusFoliageCheck,
                new Separator(),
                lblCoverHeader,
                lblLeafLitter, pane.sv(pane.leafLitterSlider, "%"),
                lblTwigDebris, pane.sv(pane.twigDebrisSlider, "%"),
                lblNonEdibleDens, pane.sv(pane.nonEdibleDensitySlider, "%"),
                pane.mossCheck, pane.pineLitterCheck, pane.fernObstacleCheck
        );
    }

    static VBox buildHydroBlock(WorldEditorPane pane) {
        pane.hydroSeedField = new TextField("987654");
        pane.hydroSeedField.setPrefWidth(90);
        pane.hydroSeedField.setOnAction(e -> pane.regenerateAndRepaint());
        pane.hydroSeedField.textProperty().addListener((o, a, b) -> pane.regenerateAndRepaint());

        Button btnRandomHydro = new Button("🎲 Seed");
        btnRandomHydro.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
        btnRandomHydro.setOnAction(e -> {
            pane.hydroSeedField.setText(String.valueOf(new Random().nextLong(100000, 9999999)));
            pane.regenerateAndRepaint();
        });

        Label lblSeedHydro = new Label();
        lblSeedHydro.textProperty().bind(I18nManager.getInstance().createStringBinding("world.seed.hydro"));
        lblSeedHydro.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;");
        HBox seedBox = new HBox(6, lblSeedHydro, pane.hydroSeedField, btnRandomHydro);
        seedBox.setAlignment(Pos.CENTER_LEFT);

        pane.riverCheck = new CheckBox(I18nManager.getInstance().get("world.river.enable")); pane.riverCheck.setSelected(true);
        pane.riverWidthSlider = pane.mkSlider(30, 500, 120);
        pane.riverWidthSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.river.width.tt"));
        pane.riverVelocitySlider = pane.mkSlider(0.0, 1.5, 0.3);
        pane.riverVelocitySlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.river.speed.tt"));
        pane.staticPoolsSlider = pane.mkSlider(0, 5, 2);
        pane.staticPoolsSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.river.ponds.tt"));
        pane.waterTableDepthSlider = pane.mkSlider(50, 4000, 1500);
        pane.waterTableDepthSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.river.watertable.tt"));
        pane.addLsn(pane.riverWidthSlider, pane.riverVelocitySlider, pane.staticPoolsSlider, pane.waterTableDepthSlider);
        pane.riverCheck.setOnAction(e -> pane.regenerateAndRepaint());

        Label hydroHint = new Label();
        hydroHint.textProperty().bind(I18nManager.getInstance().createStringBinding("world.hydro.planar_hint"));
        hydroHint.setStyle("-fx-font-size: 10px; -fx-text-fill: #38bdf8; -fx-wrap-text: true;");

        Label lblRiverWidth = new Label(); lblRiverWidth.textProperty().bind(I18nManager.getInstance().createStringBinding("world.river.width"));
        Label lblRiverSpeed = new Label(); lblRiverSpeed.textProperty().bind(I18nManager.getInstance().createStringBinding("world.river.speed"));
        Label lblRiverPonds = new Label(); lblRiverPonds.textProperty().bind(I18nManager.getInstance().createStringBinding("world.river.ponds"));
        Label lblWaterTable = new Label(); lblWaterTable.textProperty().bind(I18nManager.getInstance().createStringBinding("world.river.watertable"));

        return new VBox(8,
                seedBox,
                pane.riverCheck,
                lblRiverWidth, pane.sv(pane.riverWidthSlider, "mm"),
                lblRiverSpeed, pane.sv(pane.riverVelocitySlider, "m/s"),
                new Separator(),
                lblRiverPonds, pane.sv(pane.staticPoolsSlider, ""),
                lblWaterTable, pane.sv(pane.waterTableDepthSlider, "cm"),
                hydroHint
        );
    }

    static VBox buildStructBlock(WorldEditorPane pane) {
        pane.structSeedField = new TextField("555123");
        pane.structSeedField.setPrefWidth(90);
        pane.structSeedField.setOnAction(e -> pane.regenerateAndRepaint());
        pane.structSeedField.textProperty().addListener((o, a, b) -> pane.regenerateAndRepaint());

        Button btnRandomStruct = new Button("🎲 Seed");
        btnRandomStruct.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
        btnRandomStruct.setOnAction(e -> {
            pane.structSeedField.setText(String.valueOf(new Random().nextLong(100000, 9999999)));
            pane.regenerateAndRepaint();
        });

        Label lblSeedStruct = new Label();
        lblSeedStruct.textProperty().bind(I18nManager.getInstance().createStringBinding("world.seed.struct"));
        lblSeedStruct.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;");
        HBox seedBox = new HBox(6, lblSeedStruct, pane.structSeedField, btnRandomStruct);
        seedBox.setAlignment(Pos.CENTER_LEFT);

        pane.treeCountSlider = pane.mkSlider(0, 200, 15);
        pane.treeCountSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.struct.tree_count.tt"));
        pane.hollowLogsSlider = pane.mkSlider(0, 8, 3);
        pane.hollowLogsSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.struct.hollow_logs.tt"));
        pane.rockCrevicesSlider = pane.mkSlider(0, 8, 3);
        pane.rockCrevicesSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.struct.rock_crevices.tt"));
        pane.addLsn(pane.treeCountSlider, pane.hollowLogsSlider);
        pane.addFloraLsn(pane.rockCrevicesSlider);

        pane.comboTreeSpecies = new ComboBox<>();
        pane.comboTreeSpecies.getItems().addAll(
            "🌳 Oak & Hardwood Forest Biome (Atta / Camponotus)",
            "🌲 Pine Forest Biome (Formica / Cinara Aphids)",
            "🌵 Acacia Savannah Biome (Pseudomyrmex / Nectaries)",
            "🏜️ Desert & Saguaro Cactus Biome (Myrmecocystus / Cephalotes)",
            "🌿 Birch & Grasses Biome (Messor / Foraging)",
            "🎋 Bamboo Grove Biome (Temnothorax / Colobopsis)",
            "🪵 Dead Wood & Rotting Stumps Biome"
        );
        pane.comboTreeSpecies.getSelectionModel().selectFirst();
        pane.comboTreeSpecies.setPrefWidth(270);
        pane.comboTreeSpecies.setOnAction(e -> WorldEditorGeoAndBioclimateHelper.updateTreeSpeciesSpinnersFromPreset(pane));

        pane.oakPctSpinner      = pane.mkSpinner(0, 100, 60);
        pane.pinePctSpinner     = pane.mkSpinner(0, 100, 20);
        pane.acaciaPctSpinner   = pane.mkSpinner(0, 100, 0);
        pane.birchPctSpinner    = pane.mkSpinner(0, 100, 10);
        pane.bambooPctSpinner   = pane.mkSpinner(0, 100, 0);
        pane.cactusPctSpinner   = pane.mkSpinner(0, 100, 0);
        pane.deadWoodPctSpinner = pane.mkSpinner(0, 100, 10);

        for (Spinner<Integer> sp : new Spinner[]{pane.oakPctSpinner, pane.pinePctSpinner, pane.acaciaPctSpinner, pane.birchPctSpinner, pane.bambooPctSpinner, pane.cactusPctSpinner, pane.deadWoodPctSpinner}) {
            sp.valueProperty().addListener((o, a, b) -> {
                WorldEditorGeoAndBioclimateHelper.updateEcologicalCompatibilityScores(pane);
                pane.syncTreeSpeciesTo3DView();
                pane.repaintAllViews();
            });
        }

        GridPane botGrid = new GridPane();
        botGrid.setHgap(8); botGrid.setVgap(6);

        Label lblOak      = new Label(); lblOak.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.oak_pct"));
        Label lblPine     = new Label(); lblPine.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.pine_pct"));
        Label lblAcacia   = new Label(); lblAcacia.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.acacia_pct"));
        Label lblCactus   = new Label(); lblCactus.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.cactus_pct"));
        Label lblBirch    = new Label(); lblBirch.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.birch_pct"));
        Label lblBamboo   = new Label(); lblBamboo.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.bamboo_pct"));
        Label lblDeadwood = new Label(); lblDeadwood.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.deadwood_pct"));

        botGrid.add(lblOak, 0, 0);      botGrid.add(pane.oakPctSpinner, 1, 0);
        botGrid.add(lblPine, 0, 1);     botGrid.add(pane.pinePctSpinner, 1, 1);
        botGrid.add(lblAcacia, 0, 2);   botGrid.add(pane.acaciaPctSpinner, 1, 2);
        botGrid.add(lblCactus, 0, 3);   botGrid.add(pane.cactusPctSpinner, 1, 3);
        botGrid.add(lblBirch, 0, 4);    botGrid.add(pane.birchPctSpinner, 1, 4);
        botGrid.add(lblBamboo, 0, 5);   botGrid.add(pane.bambooPctSpinner, 1, 5);
        botGrid.add(lblDeadwood, 0, 6); botGrid.add(pane.deadWoodPctSpinner, 1, 6);

        Label lblTreeCount       = new Label(); lblTreeCount.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.tree_count"));
        Label lblHollowLogs      = new Label(); lblHollowLogs.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.hollow_logs"));
        Label lblRockCrevices    = new Label(); lblRockCrevices.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.rock_crevices"));
        Label lblDominantSpecies = new Label(); lblDominantSpecies.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.dominant_species"));
        Label lblMatrixHeader    = new Label(); lblMatrixHeader.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.matrix_header"));

        return new VBox(8,
                seedBox,
                lblTreeCount, pane.sv(pane.treeCountSlider, ""),
                lblHollowLogs, pane.sv(pane.hollowLogsSlider, ""),
                lblRockCrevices, pane.sv(pane.rockCrevicesSlider, ""),
                new Separator(),
                lblDominantSpecies,
                pane.comboTreeSpecies,
                lblMatrixHeader,
                botGrid
        );
    }

    static VBox buildDiagBlock(WorldEditorPane pane) {
        pane.lblBioclimaticZoneBadge.getStyleClass().add("accent-title");

        GridPane diagGrid = new GridPane();
        diagGrid.setHgap(10); diagGrid.setVgap(4);
        diagGrid.getStyleClass().add("diag-card");

        pane.lblAttaCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #4ade80;");
        pane.lblAphidCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #38bdf8;");
        pane.lblWoodNestCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #f59e0b;");
        pane.lblAcaciaAntCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #e879f9;");
        pane.lblCactusAntCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #f43f5e;");
        pane.lblPogonomyrmexCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #eab308;");
        pane.lblTermiteCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #a3e635;");
        pane.lblApisCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #fbbf24;");
        pane.lblVespulaCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #f97316;");
        pane.lblSolenopsisCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #ef4444;");

        Label lblDiagZone = new Label(); lblDiagZone.textProperty().bind(I18nManager.getInstance().createStringBinding("world.diag.zone"));
        Label lblDiagAtta = new Label(); lblDiagAtta.textProperty().bind(I18nManager.getInstance().createStringBinding("world.diag.atta"));
        Label lblDiagAphid = new Label(); lblDiagAphid.textProperty().bind(I18nManager.getInstance().createStringBinding("world.diag.aphid"));
        Label lblDiagWood = new Label(); lblDiagWood.textProperty().bind(I18nManager.getInstance().createStringBinding("world.diag.wood"));
        Label lblDiagPogo = new Label(); lblDiagPogo.textProperty().bind(I18nManager.getInstance().createStringBinding("world.diag.pogonomyrmex"));
        Label lblDiagSole = new Label(); lblDiagSole.textProperty().bind(I18nManager.getInstance().createStringBinding("world.diag.solenopsis"));

        diagGrid.add(lblDiagZone, 0, 0); diagGrid.add(pane.lblBioclimaticZoneBadge, 1, 0);
        diagGrid.add(lblDiagAtta, 0, 1); diagGrid.add(pane.lblAttaCompatScore, 1, 1);
        diagGrid.add(lblDiagAphid, 0, 2); diagGrid.add(pane.lblAphidCompatScore, 1, 2);
        diagGrid.add(lblDiagWood, 0, 3); diagGrid.add(pane.lblWoodNestCompatScore, 1, 3);
        diagGrid.add(new Label("🌵 Pseudomyrmex (Acacia) :"), 0, 4); diagGrid.add(pane.lblAcaciaAntCompatScore, 1, 4);
        diagGrid.add(new Label("🏜️ Desert Ants (Cactus) :"), 0, 5); diagGrid.add(pane.lblCactusAntCompatScore, 1, 5);
        diagGrid.add(lblDiagPogo, 0, 6); diagGrid.add(pane.lblPogonomyrmexCompatScore, 1, 6);
        diagGrid.add(new Label("🪵 Reticulitermes (Termites) :"), 0, 7); diagGrid.add(pane.lblTermiteCompatScore, 1, 7);
        diagGrid.add(new Label("🐝 Apis mellifera (Honey Bees) :"), 0, 8); diagGrid.add(pane.lblApisCompatScore, 1, 8);
        diagGrid.add(new Label("🐝 Vespula vulgaris (Wasps) :"), 0, 9); diagGrid.add(pane.lblVespulaCompatScore, 1, 9);
        diagGrid.add(lblDiagSole, 0, 10); diagGrid.add(pane.lblSolenopsisCompatScore, 1, 10);

        WorldEditorGeoAndBioclimateHelper.updateEcologicalCompatibilityScores(pane);

        Label lblDiagTitle = new Label(); lblDiagTitle.textProperty().bind(I18nManager.getInstance().createStringBinding("world.diag.title"));

        return new VBox(8,
                lblDiagTitle,
                diagGrid
        );
    }

    static VBox buildAdaptBlock(WorldEditorPane pane) {
        VBox adaptBox = new VBox(6);
        adaptBox.getStyleClass().add("diag-card");

        Label lblAdapt = new Label();
        lblAdapt.textProperty().bind(I18nManager.getInstance().createStringBinding("world.adapt.title"));
        lblAdapt.getStyleClass().add("purple-title");

        pane.speciesAdaptCombo = new ComboBox<>();
        pane.speciesAdaptCombo.getItems().addAll(
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
        FXCollections.sort(pane.speciesAdaptCombo.getItems());
        pane.speciesAdaptCombo.getSelectionModel().selectFirst();
        pane.speciesAdaptCombo.setMaxWidth(Double.MAX_VALUE);

        Button btnAdapt = new Button();
        btnAdapt.textProperty().bind(I18nManager.getInstance().createStringBinding("world.adapt.btn"));
        btnAdapt.setMaxWidth(Double.MAX_VALUE);
        btnAdapt.getStyleClass().add("btn-primary");
        btnAdapt.setGraphic(new FontIcon(Feather.CPU));
        btnAdapt.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.adapt.btn.tt"));
        btnAdapt.setOnAction(e -> WorldEditorGeoAndBioclimateHelper.handleAdaptWorldToSpecies(pane));

        pane.lblAdaptStatus = new Label();
        pane.lblAdaptStatus.textProperty().bind(I18nManager.getInstance().createStringBinding("world.adapt.status"));
        pane.lblAdaptStatus.setStyle("-fx-font-size: 10px; -fx-text-fill: #4ade80;");

        adaptBox.getChildren().addAll(lblAdapt, pane.speciesAdaptCombo, btnAdapt, pane.lblAdaptStatus);
        return adaptBox;
    }

    static VBox buildSculptBlock(WorldEditorPane pane) {
        pane.enableSculptingCheck = new CheckBox("🖌️ Enable Direct Sculpting Mode (Mouse Drag)");
        pane.enableSculptingCheck.setSelected(false);
        pane.enableSculptingCheck.setStyle("-fx-font-weight: bold;");
        pane.enableSculptingCheck.getStyleClass().add("accent-title");
        pane.enableSculptingCheck.setOnAction(e -> pane.repaintAllViews());

        pane.brushModeSelect = new ComboBox<>();
        pane.brushModeSelect.getItems().addAll("Lower Elevation", "Raise Elevation", "Smooth Relief");
        ComboBoxTooltipHelper.setupDescriptiveComboBox(pane.brushModeSelect,
            val -> val,
            val -> switch (val != null ? val : "") {
                case "Raise Elevation" -> "Increases the terrain elevation at painted coordinates.";
                case "Lower Elevation" -> "Digs into the terrain and lowers surface voxel elevation.";
                case "Smooth Relief" -> "Calculates local average elevation to smooth sharp slopes.";
                default -> "";
            }
        );
        pane.brushModeSelect.getSelectionModel().selectFirst();
        pane.brushModeSelect.setPrefWidth(240);
        pane.brushModeSelect.valueProperty().addListener((o, a, b) -> pane.repaintAllViews());

        pane.brushSubstrateSelect = new ComboBox<>();
        pane.brushSubstrateSelect.getItems().addAll(
            "Earth Substrate", "Sand Substrate", "Clay Substrate",
            "Silt Substrate", "Peat Substrate", "Gravel Substrate",
            "Stone Substrate", "Natural Cavity / Void"
        );

        pane.brushRadiusSlider = pane.mkSlider(1, 15, 4);
        pane.brushStrengthSlider = pane.mkSlider(10, 100, 50);
        pane.addLsn(pane.brushRadiusSlider, pane.brushStrengthSlider);

        Label lblBrushMode = new Label();
        lblBrushMode.textProperty().bind(I18nManager.getInstance().createStringBinding("world.mode_brush"));
        Label lblBrushRadius = new Label();
        lblBrushRadius.textProperty().bind(I18nManager.getInstance().createStringBinding("world.radius_brush"));
        Label lblBrushStrength = new Label();
        lblBrushStrength.textProperty().bind(I18nManager.getInstance().createStringBinding("world.strength_brush"));

        return new VBox(8,
                pane.enableSculptingCheck,
                lblBrushMode, pane.brushModeSelect,
                lblBrushRadius, pane.sv(pane.brushRadiusSlider, "vx"),
                lblBrushStrength, pane.sv(pane.brushStrengthSlider, "%")
        );
    }
}
