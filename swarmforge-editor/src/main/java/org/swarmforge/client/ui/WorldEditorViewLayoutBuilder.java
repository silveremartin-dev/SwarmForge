package org.swarmforge.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.view.GameViewPane;

/**
 * Builds the top header and main view layouts (3D canvas, minimaps, overlay cards) for {@link WorldEditorPane}.
 */
public final class WorldEditorViewLayoutBuilder {

    private WorldEditorViewLayoutBuilder() {}

    public static VBox buildHeader(WorldEditorPane pane) {
        pane.headerBox = new VBox(8);
        pane.headerBox.setPadding(new Insets(8, 12, 6, 12));
        pane.headerBox.setStyle("-fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 0 0 1 0;");

        pane.presetRow = new HBox(10);
        pane.presetRow.setAlignment(Pos.CENTER_LEFT);

        pane.headerTitleLabel = new Label();
        pane.headerTitleLabel.textProperty().bind(I18nManager.getInstance().createStringBinding("world.editor_title"));
        pane.headerTitleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        pane.headerTitleLabel.getStyleClass().add("header-title-large");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label lblPreset = new Label();
        lblPreset.textProperty().bind(I18nManager.getInstance().createStringBinding("preset.label"));
        lblPreset.setStyle("-fx-font-weight: bold;");
        lblPreset.setGraphic(new FontIcon(Feather.SLIDERS));

        pane.presetsCombo = new ComboBox<>();
        pane.presetsCombo.setEditable(true);
        pane.presetsCombo.promptTextProperty().bind(I18nManager.getInstance().createStringBinding("preset.prompt"));
        pane.presetsCombo.getItems().setAll(pane.presetManager.names());
        if (!pane.presetsCombo.getItems().isEmpty()) {
            pane.presetsCombo.getSelectionModel().selectFirst();
        }
        pane.presetsCombo.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.presets.combo.tt"));
        pane.presetsCombo.setPrefWidth(220);
        pane.presetsCombo.setOnAction(e -> {
            String selected = pane.presetsCombo.getValue();
            if (selected != null && pane.presetManager.contains(selected)) {
                pane.loadConfiguration(pane.presetManager.get(selected));
            }
        });

        Button bSave = new Button();
        bSave.textProperty().bind(I18nManager.getInstance().createStringBinding("preset.save"));
        bSave.setGraphic(new FontIcon(Feather.SAVE));
        bSave.getStyleClass().add("btn-secondary");
        bSave.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.presets.save.tt"));
        bSave.setOnAction(e -> pane.handleSavePreset());

        Button bDelete = new Button();
        bDelete.textProperty().bind(I18nManager.getInstance().createStringBinding("preset.delete"));
        bDelete.setGraphic(new FontIcon(Feather.TRASH_2));
        bDelete.getStyleClass().add("btn-danger");
        bDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold;");
        bDelete.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.presets.delete.tt"));
        bDelete.setOnAction(e -> pane.handleDeletePreset());

        Button bExport = new Button();
        bExport.textProperty().bind(I18nManager.getInstance().createStringBinding("preset.export"));
        bExport.setGraphic(new FontIcon(Feather.DOWNLOAD));
        bExport.getStyleClass().add("btn-secondary");
        bExport.setOnAction(e -> pane.doExport());

        Button bNewPreset = new Button();
        bNewPreset.textProperty().bind(I18nManager.getInstance().createStringBinding("world.preset.new"));
        bNewPreset.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold;");
        bNewPreset.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.presets.new.tt"));
        bNewPreset.setOnAction(e -> pane.handleSavePreset());

        pane.presetRow.getChildren().addAll(pane.headerTitleLabel, sp, lblPreset, pane.presetsCombo, bNewPreset, bSave, bDelete, new Separator(Orientation.VERTICAL), bExport);

        pane.headerSubtitleLabel = new Label();
        pane.headerSubtitleLabel.textProperty().bind(I18nManager.getInstance().createStringBinding("world.editor_subtitle"));
        pane.headerSubtitleLabel.setStyle("-fx-font-size: 11px;");
        pane.headerSubtitleLabel.getStyleClass().add("header-subtitle");

        pane.headerBox.getChildren().addAll(pane.presetRow, pane.headerSubtitleLabel);
        return pane.headerBox;
    }

    public static HBox buildViews(WorldEditorPane pane) {
        pane.canvas3D = new ResizableCanvas(540, 510); pane.gc3D = pane.canvas3D.getGraphicsContext2D();
        pane.canvasSide = new ResizableCanvas(190, 150); pane.gcSide = pane.canvasSide.getGraphicsContext2D();
        pane.canvasTop = new ResizableCanvas(190, 150); pane.gcTop = pane.canvasTop.getGraphicsContext2D();

        pane.canvasSide.setWidth(190); pane.canvasSide.setHeight(150);
        pane.canvasTop.setWidth(190); pane.canvasTop.setHeight(150);

        WorldEditorInteractionAndInspectorHelper.setupMouseControls(pane);

        pane.trackedAntPane = new TrackedIndividualPane();
        pane.trackedAntPane.setPickOnBounds(false);
        pane.trackedAntPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        pane.trackedAntPane.setVisible(pane.isSimulationMode);
        StackPane.setAlignment(pane.trackedAntPane, Pos.BOTTOM_LEFT);
        StackPane.setMargin(pane.trackedAntPane, new Insets(10, 10, 40, 10));

        pane.trackedAntPane.setOnFollowAnt(ant -> {
            pane.setFollowedIndividual(ant);
            pane.setCameraFollowMode(CameraFollowMode.TPS);
        });
        pane.trackedAntPane.setOnFollowAntMode((ant, mode) -> {
            pane.setFollowedIndividual(ant);
            pane.setCameraFollowMode(mode);
        });
        pane.trackedAntPane.setOnFollowAntById(pane::setFollowedIndividualById);
        pane.trackedAntPane.setOnStopFollow(() -> {
            pane.setFollowedIndividual(null);
            pane.setCameraFollowMode(CameraFollowMode.FREE);
            pane.trackedAntPane.setNoAntSelectedState();
        });
        pane.trackedAntPane.setOnCenter(pane::centerCameraOnSelection);
        pane.trackedAntPane.setOnNextAnt(pane::selectNextNearestIndividual);
        pane.trackedAntPane.setOnPreviousAnt(pane::selectPreviousNearestIndividual);
        pane.trackedAntPane.setOnClose(() -> {
            if (pane.showAntTrackingCheck != null) pane.showAntTrackingCheck.setSelected(false);
            pane.setAntTrackingEnabled(false);
        });

        pane.chamberInfoPane = new ChamberInfoPane();
        pane.chamberInfoPane.setPickOnBounds(false);
        pane.chamberInfoPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        pane.chamberInfoPane.setVisible(false);
        StackPane.setAlignment(pane.chamberInfoPane, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(pane.chamberInfoPane, new Insets(10, 10, 40, 10));
        pane.chamberInfoPane.setOnCenter(pane::centerCameraOnSelection);
        pane.chamberInfoPane.setOnClose(() -> {
            pane.setChamberOverlayVisible(false);
            pane.selectedChamberNode = null;
        });

        StackPane hSide = new StackPane(pane.canvasSide);
        hSide.setPrefSize(190, 150); hSide.setMinSize(190, 150);
        hSide.setStyle("-fx-background-color: transparent;");
        StackPane hTop = new StackPane(pane.canvasTop);
        hTop.setPrefSize(190, 150); hTop.setMinSize(190, 150);
        hTop.setStyle("-fx-background-color: transparent;");

        pane.lblSideMinimapTitle = new Label();
        pane.lblSideMinimapTitle.textProperty().bind(I18nManager.getInstance().createStringBinding("minimap.sideview"));
        pane.lblSideMinimapTitle.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
        pane.lblSideMinimapTitle.getStyleClass().add("accent-title");

        pane.lblTopdownMinimapTitle = new Label();
        pane.lblTopdownMinimapTitle.textProperty().bind(I18nManager.getInstance().createStringBinding("minimap.topdown"));
        pane.lblTopdownMinimapTitle.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
        pane.lblTopdownMinimapTitle.getStyleClass().add("accent-title");

        pane.sideMinimapsBox = new VBox(4, pane.lblTopdownMinimapTitle, hTop, pane.lblSideMinimapTitle, hSide);
        pane.sideMinimapsBox.setPadding(new Insets(6));
        pane.sideMinimapsBox.setPrefWidth(206);
        pane.sideMinimapsBox.setMinWidth(206);
        pane.sideMinimapsBox.setStyle("-fx-background-color: rgba(15, 23, 42, 0.85); -fx-background-radius: 8; -fx-border-color: rgba(56, 189, 248, 0.4); -fx-border-width: 1; -fx-border-radius: 8;");
        pane.sideMinimapsBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        pane.sideMinimapsBox.setPickOnBounds(false);
        StackPane.setAlignment(pane.sideMinimapsBox, Pos.TOP_RIGHT);
        StackPane.setMargin(pane.sideMinimapsBox, new Insets(10, 10, 10, 10));

        pane.gameView = GameViewPane.getSharedInstance(1024, 768);
        pane.floatingMouseInspectorCard = WorldEditorInteractionAndInspectorHelper.createFloatingMouseInspectorCard(pane);
        pane.weatherOverlayPane = WorldEditorRightSidebarAndLegendBuilder.createWeatherOverlayPane(pane);

        boolean isSci = (pane.currentRenderMode == WorldEditorPane.RenderMode.SCIENTIFIC);
        pane.canvas3D.setVisible(isSci);
        pane.canvas3D.setManaged(isSci);

        if (pane.gameView.getParent() == null) {
            pane.h3d = new StackPane(pane.canvas3D, pane.gameView, pane.floatingMouseInspectorCard, pane.sideMinimapsBox, pane.trackedAntPane, pane.chamberInfoPane, pane.weatherOverlayPane);
            pane.bindGameViewListeners();
            pane.gameView.setVisible(!isSci);
            pane.gameView.setManaged(!isSci);
            if (!isSci) pane.gameView.setRenderMode(pane.currentRenderMode);
        } else {
            pane.h3d = new StackPane(pane.canvas3D, pane.floatingMouseInspectorCard, pane.sideMinimapsBox, pane.trackedAntPane, pane.chamberInfoPane, pane.weatherOverlayPane);
        }
        pane.h3d.setStyle("-fx-border-color: #555; -fx-border-width: 1; -fx-background-color: #0b0f19;");
        HBox.setHgrow(pane.h3d, Priority.ALWAYS);
        VBox.setVgrow(pane.h3d, Priority.ALWAYS);

        pane.h3d.setOnScroll(e -> {
            if (pane.gameView != null && pane.gameView.isVisible() && pane.gameView.getGameApp() != null) {
                pane.gameView.getGameApp().zoomCamera((float) e.getDeltaY() * 0.05f, e.getX(), e.getY(), pane.gameView.getWidth(), pane.gameView.getHeight());
                pane.repaintAllViews();
            }
        });

        pane.canvas3D.widthProperty().bind(pane.h3d.widthProperty());
        pane.canvas3D.heightProperty().bind(pane.h3d.heightProperty());
        pane.canvas3D.widthProperty().addListener((obs, oldV, newV) -> pane.repaintAllViews());
        pane.canvas3D.heightProperty().addListener((obs, oldV, newV) -> pane.repaintAllViews());

        pane.rightRenderOptions = WorldEditorRightSidebarAndLegendBuilder.buildRightRenderOptionsPanel(pane);
        pane.legendPanel = WorldEditorRightSidebarAndLegendBuilder.buildLegendPanel(pane);
        pane.legendSeparator = new Separator();

        VBox rightSideContent = new VBox(10, pane.legendPanel, pane.legendSeparator, pane.rightRenderOptions);
        rightSideContent.setPadding(new Insets(4));
        rightSideContent.setPrefWidth(245);

        pane.rightScroll = new ScrollPane(rightSideContent);
        pane.rightScroll.setFitToWidth(true);
        pane.rightScroll.setPrefWidth(260);
        pane.rightScroll.setMinWidth(250);
        pane.rightScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        pane.updateRightScrollVisibility();

        HBox area = new HBox(6, pane.h3d, pane.rightScroll);
        area.setPadding(new Insets(8, 8, 4, 8));
        VBox.setVgrow(area, Priority.ALWAYS);
        return area;
    }
}
