/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.weather;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.ThemeManager;

/**
 * Interactive Spline & Monthly Curves Editor Pane for SwarmForge Studio.
 * Visualizes and manipulates monthly climate curves across 12 calendar months:
 * <ul>
 *   <li>Temperature envelopes (Min / Avg / Max in °C)</li>
 *   <li>Wind speed curves (Min / Avg / Max in km/h)</li>
 *   <li>Precipitation monthly sums (Min / Avg / Max in mm)</li>
 *   <li>Relative humidity envelopes (Min / Avg / Max in %)</li>
 *   <li>Multi-parameter global overview grid</li>
 * </ul>
 * Features real-time bezier curve dragging handles, hover tooltip inspect, and synchronized numerical spinners.
 */
public class WeatherCurvesEditorPane extends VBox {

    public static final String[] MONTH_KEYS = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

    public enum ParameterType {
        TEMPERATURE("🌡 Temperature (°C)", -40, 50, "°C", 10),
        WIND("💨 Wind (km/h)", 0, 150, " km/h", 25),
        RAIN("🌧 Precipitation (mm)", 0, 500, " mm", 100),
        HUMIDITY("💧 Relative Humidity (%)", 0, 100, "%", 20),
        OVERVIEW("📊 Global Overview", 0, 100, "", 20);

        public final String title;
        public final double minVal;
        public final double maxVal;
        public final String unit;
        public final double tickStep;

        ParameterType(String title, double minVal, double maxVal, String unit, double tickStep) {
            this.title = title;
            this.minVal = minVal;
            this.maxVal = maxVal;
            this.unit = unit;
            this.tickStep = tickStep;
        }
    }

    private final double[] tempMin;
    private final double[] tempAvg;
    private final double[] tempMax;

    private final double[] windMin;
    private final double[] windAvg;
    private final double[] windMax;

    private final double[] rainMin;
    private final double[] rainAvg;
    private final double[] rainMax;

    private final double[] humidityMin;
    private final double[] humidityAvg;
    private final double[] humidityMax;

    private final Runnable onSyncAndValidate;
    private final Runnable onFieldEdited;

    private ParameterType activeParam = ParameterType.TEMPERATURE;
    private ResizableCanvas curveCanvas;
    private GraphicsContext gc;
    private ToggleGroup paramToggleGroup;

    private int draggedMonth = -1;
    private int draggedCurve = -1; // 0 = Min, 1 = Avg, 2 = Max
    private String hoverInfoText = "";

    @SuppressWarnings("unchecked")
    private final Spinner<Double>[] minSpinners = new Spinner[12];
    @SuppressWarnings("unchecked")
    private final Spinner<Double>[] avgSpinners = new Spinner[12];
    @SuppressWarnings("unchecked")
    private final Spinner<Double>[] maxSpinners = new Spinner[12];
    private boolean updatingSpinners = false;

    public WeatherCurvesEditorPane(
            double[] tempMin, double[] tempAvg, double[] tempMax,
            double[] windMin, double[] windAvg, double[] windMax,
            double[] rainMin, double[] rainAvg, double[] rainMax,
            double[] humidityMin, double[] humidityAvg, double[] humidityMax,
            Runnable onSyncAndValidate,
            Runnable onFieldEdited) {
        this.tempMin = tempMin;
        this.tempAvg = tempAvg;
        this.tempMax = tempMax;
        this.windMin = windMin;
        this.windAvg = windAvg;
        this.windMax = windMax;
        this.rainMin = rainMin;
        this.rainAvg = rainAvg;
        this.rainMax = rainMax;
        this.humidityMin = humidityMin;
        this.humidityAvg = humidityAvg;
        this.humidityMax = humidityMax;
        this.onSyncAndValidate = onSyncAndValidate;
        this.onFieldEdited = onFieldEdited;

        setSpacing(8);
        setPadding(new Insets(10));
        getStyleClass().add("card-pane");

        buildUI();
    }

    private void buildUI() {
        I18nManager i18n = I18nManager.getInstance();

        Label title = new Label();
        title.textProperty().bind(i18n.createStringBinding("weather.curves.title"));
        title.getStyleClass().add("card-title");
        title.setStyle("-fx-font-size: 14px;");

        // Parameter selector toolbar
        HBox paramBar = new HBox(8);
        paramBar.setAlignment(Pos.CENTER_LEFT);
        paramToggleGroup = new ToggleGroup();

        for (ParameterType type : ParameterType.values()) {
            ToggleButton tb = new ToggleButton(type.title);
            tb.setToggleGroup(paramToggleGroup);
            tb.setUserData(type);
            tb.setStyle("-fx-font-size:11;-fx-font-weight:bold;");
            if (type == ParameterType.TEMPERATURE) tb.setSelected(true);
            paramBar.getChildren().add(tb);
        }

        paramToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                activeParam = (ParameterType) newVal.getUserData();
                updateSpinnersForActiveParam();
                redrawCurves();
            }
        });

        // Canvas setup
        curveCanvas = new ResizableCanvas(760, 260);
        gc = curveCanvas.getGraphicsContext2D();

        setupCanvasInteractions();

        StackPane canvasHolder = new StackPane(curveCanvas);
        canvasHolder.getStyleClass().add("chart-holder");
        canvasHolder.setMinHeight(200);
        canvasHolder.setPrefHeight(260);
        canvasHolder.setMaxHeight(260);

        curveCanvas.widthProperty().bind(canvasHolder.widthProperty());
        curveCanvas.heightProperty().bind(canvasHolder.heightProperty());
        curveCanvas.widthProperty().addListener((obs, oldV, newV) -> redrawCurves());
        curveCanvas.heightProperty().addListener((obs, oldV, newV) -> redrawCurves());

        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.widthProperty().bind(canvasHolder.widthProperty());
        clip.heightProperty().bind(canvasHolder.heightProperty());
        canvasHolder.setClip(clip);

        HBox legendBar = buildCurvesLegendBar();

        GridPane spinnerGrid = buildMonthlySpinnersGrid();
        ScrollPane spinnerScroll = new ScrollPane(spinnerGrid);
        spinnerScroll.setFitToWidth(true);
        spinnerScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spinnerScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");

        getChildren().addAll(title, paramBar, canvasHolder, legendBar, spinnerScroll);
    }

    private HBox buildCurvesLegendBar() {
        I18nManager i18n = I18nManager.getInstance();
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(4, 8, 4, 8));

        Label minLbl = createDotLabel(i18n.get("weather.curves.min") + " (Cyan)", Color.web("#00d4ff"));
        Label avgLbl = createDotLabel(i18n.get("weather.curves.avg") + " (Or)", Color.web("#ffc107"));
        Label maxLbl = createDotLabel(i18n.get("weather.curves.max") + " (Rouge)", Color.web("#ff4757"));

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label hint = new Label();
        hint.textProperty().bind(i18n.createStringBinding("weather.curves.hint"));
        hint.setStyle("-fx-text-fill:#888;-fx-font-size:10;-fx-font-style:italic;");

        bar.getChildren().addAll(minLbl, avgLbl, maxLbl, sp, hint);
        return bar;
    }

    private Label createDotLabel(String text, Color color) {
        Label l = new Label("● " + text);
        l.setStyle("-fx-text-fill: " + toHexString(color) + ";-fx-font-weight:bold;-fx-font-size:11;");
        return l;
    }

    private String toHexString(Color c) {
        return String.format("#%02X%02X%02X",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
    }

    private GridPane buildMonthlySpinnersGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(4);
        grid.setVgap(4);
        grid.setPadding(new Insets(6, 0, 0, 0));

        for (int m = 0; m < 12; m++) {
            Label mLbl = new Label(MONTH_KEYS[m]);
            mLbl.setAlignment(Pos.CENTER);
            mLbl.setStyle("-fx-font-size:10;-fx-font-weight:bold;-fx-text-fill:#aaa;-fx-min-width:55;");
            grid.add(mLbl, m, 0);

            maxSpinners[m] = new Spinner<>(-100.0, 1000.0, 0.0, 1.0);
            styleSpinner(maxSpinners[m], "#ff4757");

            avgSpinners[m] = new Spinner<>(-100.0, 1000.0, 0.0, 1.0);
            styleSpinner(avgSpinners[m], "#ffc107");

            minSpinners[m] = new Spinner<>(-100.0, 1000.0, 0.0, 1.0);
            styleSpinner(minSpinners[m], "#00d4ff");

            final int monthIdx = m;
            maxSpinners[m].valueProperty().addListener((o, a, b) -> {
                if (updatingSpinners) return;
                setParamVal(activeParam, 2, monthIdx, b);
                notifyChanged();
                redrawCurves();
            });

            avgSpinners[m].valueProperty().addListener((o, a, b) -> {
                if (updatingSpinners) return;
                setParamVal(activeParam, 1, monthIdx, b);
                notifyChanged();
                redrawCurves();
            });

            minSpinners[m].valueProperty().addListener((o, a, b) -> {
                if (updatingSpinners) return;
                setParamVal(activeParam, 0, monthIdx, b);
                notifyChanged();
                redrawCurves();
            });

            grid.add(maxSpinners[m], m, 1);
            grid.add(avgSpinners[m], m, 2);
            grid.add(minSpinners[m], m, 3);
        }

        return grid;
    }

    private void notifyChanged() {
        if (onFieldEdited != null) onFieldEdited.run();
        if (onSyncAndValidate != null) onSyncAndValidate.run();
    }

    private void styleSpinner(Spinner<Double> s, String textColor) {
        s.setPrefWidth(58);
        s.setEditable(true);
        s.setStyle("-fx-font-size:9;-fx-text-fill:" + textColor + ";");
    }

    public void updateSpinnersForActiveParam() {
        if (activeParam == ParameterType.OVERVIEW) return;
        updatingSpinners = true;

        double[] minArr = getArr(activeParam, 0);
        double[] avgArr = getArr(activeParam, 1);
        double[] maxArr = getArr(activeParam, 2);

        for (int m = 0; m < 12; m++) {
            minSpinners[m].getValueFactory().setValue(Math.round(minArr[m] * 10.0) / 10.0);
            avgSpinners[m].getValueFactory().setValue(Math.round(avgArr[m] * 10.0) / 10.0);
            maxSpinners[m].getValueFactory().setValue(Math.round(maxArr[m] * 10.0) / 10.0);
        }

        updatingSpinners = false;
    }

    private void setupCanvasInteractions() {
        curveCanvas.setOnMousePressed(e -> {
            if (activeParam == ParameterType.OVERVIEW) return;
            double mx = e.getX();
            double my = e.getY();

            double padL = 50, padR = 20, padT = 30, padB = 30;
            double w = curveCanvas.getWidth() - padL - padR;
            double h = curveCanvas.getHeight() - padT - padB;

            double monthStep = w / 11.0;
            int closestM = (int) Math.round((mx - padL) / monthStep);
            closestM = Math.max(0, Math.min(11, closestM));

            double xM = padL + closestM * monthStep;
            if (Math.abs(mx - xM) > 25) return;

            double[] minArr = getArr(activeParam, 0);
            double[] avgArr = getArr(activeParam, 1);
            double[] maxArr = getArr(activeParam, 2);

            double yMin = valToY(minArr[closestM], activeParam, padT, h);
            double yAvg = valToY(avgArr[closestM], activeParam, padT, h);
            double yMax = valToY(maxArr[closestM], activeParam, padT, h);

            double dMin = Math.abs(my - yMin);
            double dAvg = Math.abs(my - yAvg);
            double dMax = Math.abs(my - yMax);

            if (dMin <= dAvg && dMin <= dMax && dMin < 20) {
                draggedCurve = 0;
                draggedMonth = closestM;
            } else if (dAvg <= dMin && dAvg <= dMax && dAvg < 20) {
                draggedCurve = 1;
                draggedMonth = closestM;
            } else if (dMax < 20) {
                draggedCurve = 2;
                draggedMonth = closestM;
            }
        });

        curveCanvas.setOnMouseDragged(e -> {
            if (draggedMonth < 0 || activeParam == ParameterType.OVERVIEW) return;

            double padT = 30;
            double h = curveCanvas.getHeight() - 30 - 30;
            double newVal = yToVal(e.getY(), activeParam, padT, h);

            setParamVal(activeParam, draggedCurve, draggedMonth, newVal);
            notifyChanged();
            updateSpinnersForActiveParam();
            redrawCurves();
        });

        curveCanvas.setOnMouseReleased(e -> {
            draggedMonth = -1;
            draggedCurve = -1;
        });

        curveCanvas.setOnMouseMoved(e -> {
            if (activeParam == ParameterType.OVERVIEW) return;
            double mx = e.getX();
            double padL = 50, padR = 20;
            double w = curveCanvas.getWidth() - padL - padR;
            double monthStep = w / 11.0;

            int m = (int) Math.round((mx - padL) / monthStep);
            if (m >= 0 && m < 12 && Math.abs(mx - (padL + m * monthStep)) < 25) {
                double[] minArr = getArr(activeParam, 0);
                double[] avgArr = getArr(activeParam, 1);
                double[] maxArr = getArr(activeParam, 2);
                String precipType = getPrecipitationType(m);
                hoverInfoText = String.format("%s (%s): Min=%.1f%s, Avg=%.1f%s, Max=%.1f%s",
                        MONTH_KEYS[m], precipType, minArr[m], activeParam.unit, avgArr[m], activeParam.unit, maxArr[m], activeParam.unit);
            } else {
                hoverInfoText = "";
            }
            redrawCurves();
        });

        curveCanvas.setOnMouseExited(e -> {
            hoverInfoText = "";
            redrawCurves();
        });
    }

    private String getPrecipitationType(int month) {
        if (tempMax[month] < 0) return "❄️ Snow";
        if (tempAvg[month] > 28 && rainAvg[month] > 150) return "🧊 Storms / Hail";
        if (rainAvg[month] > 5) return "🌧 Rain";
        return "☀️ Clear";
    }

    public void redrawCurves() {
        if (curveCanvas == null || gc == null || curveCanvas.getWidth() < 10 || curveCanvas.getHeight() < 10) return;
        double w = curveCanvas.getWidth();
        double h = curveCanvas.getHeight();

        boolean isDark = ThemeManager.getInstance().getCurrentTheme() == ThemeManager.Theme.DARK;
        Color bgCol = isDark ? Color.web("#18181b") : Color.web("#ffffff");
        Color gridCol = isDark ? Color.web("#3f3f46") : Color.web("#e2e8f0");
        Color textCol = isDark ? Color.web("#a1a1aa") : Color.web("#475569");
        Color tooltipBg = isDark ? Color.web("#27272a", 0.95) : Color.web("#ffffff", 0.95);
        Color tooltipText = isDark ? Color.web("#f4f4f5") : Color.web("#0f172a");

        gc.clearRect(0, 0, w, h);
        gc.setFill(bgCol);
        gc.fillRect(0, 0, w, h);

        if (activeParam == ParameterType.OVERVIEW) {
            drawOverviewMode(w, h);
            return;
        }

        double padL = 50, padR = 20, padT = 30, padB = 30;
        double chartW = w - padL - padR;
        double chartH = h - padT - padB;

        gc.setStroke(gridCol);
        gc.setLineWidth(1.0);
        gc.setFont(javafx.scene.text.Font.font(10));
        gc.setFill(textCol);

        double range = activeParam.maxVal - activeParam.minVal;
        for (double v = activeParam.minVal; v <= activeParam.maxVal; v += activeParam.tickStep) {
            double y = padT + chartH * (1.0 - (v - activeParam.minVal) / range);
            gc.strokeLine(padL, y, padL + chartW, y);
            gc.fillText(String.format("%.0f%s", v, activeParam.unit), 5, y + 4);
        }

        double monthStep = chartW / 11.0;
        for (int m = 0; m < 12; m++) {
            double x = padL + m * monthStep;
            gc.strokeLine(x, padT, x, padT + chartH);
            gc.fillText(MONTH_KEYS[m], x - 10, h - 10);
        }

        double[] minArr = getArr(activeParam, 0);
        double[] avgArr = getArr(activeParam, 1);
        double[] maxArr = getArr(activeParam, 2);

        gc.setFill(Color.web("#0284c7", 0.12));
        gc.beginPath();
        for (int m = 0; m < 12; m++) {
            double x = padL + m * monthStep;
            double yMax = valToY(maxArr[m], activeParam, padT, chartH);
            if (m == 0) gc.moveTo(x, yMax);
            else gc.lineTo(x, yMax);
        }
        for (int m = 11; m >= 0; m--) {
            double x = padL + m * monthStep;
            double yMin = valToY(minArr[m], activeParam, padT, chartH);
            gc.lineTo(x, yMin);
        }
        gc.closePath();
        gc.fill();

        drawSingleCurve(minArr, Color.web("#0284c7"), padL, padT, monthStep, chartH);
        drawSingleCurve(avgArr, Color.web("#eab308"), padL, padT, monthStep, chartH);
        drawSingleCurve(maxArr, Color.web("#ef4444"), padL, padT, monthStep, chartH);

        for (int m = 0; m < 12; m++) {
            double x = padL + m * monthStep;
            drawHandle(x, valToY(minArr[m], activeParam, padT, chartH), Color.web("#0284c7"), draggedMonth == m && draggedCurve == 0);
            drawHandle(x, valToY(avgArr[m], activeParam, padT, chartH), Color.web("#eab308"), draggedMonth == m && draggedCurve == 1);
            drawHandle(x, valToY(maxArr[m], activeParam, padT, chartH), Color.web("#ef4444"), draggedMonth == m && draggedCurve == 2);
        }

        if (!hoverInfoText.isEmpty()) {
            gc.setFill(tooltipBg);
            gc.setStroke(Color.web("#0284c7"));
            gc.fillRect(padL + 10, padT + 5, 290, 24);
            gc.strokeRect(padL + 10, padT + 5, 290, 24);

            gc.setFill(tooltipText);
            gc.setFont(javafx.scene.text.Font.font("System", 11));
            gc.fillText(hoverInfoText, padL + 18, padT + 21);
        }
    }

    private void drawOverviewMode(double w, double h) {
        ParameterType[] params = { ParameterType.TEMPERATURE, ParameterType.WIND, ParameterType.RAIN, ParameterType.HUMIDITY };
        double subW = (w - 30) / 2.0;
        double subH = (h - 30) / 2.0;

        boolean isDark = ThemeManager.getInstance().getCurrentTheme() == ThemeManager.Theme.DARK;
        Color subBg = isDark ? Color.web("#27272a") : Color.web("#f1f5f9");
        Color subBorder = isDark ? Color.web("#3f3f46") : Color.web("#cbd5e1");
        Color titleCol = isDark ? Color.web("#f4f4f5") : Color.web("#0f172a");

        for (int i = 0; i < 4; i++) {
            double rx = 10 + (i % 2) * (subW + 10);
            double ry = 10 + (i / 2) * (subH + 10);

            gc.setFill(subBg);
            gc.fillRect(rx, ry, subW, subH);
            gc.setStroke(subBorder);
            gc.strokeRect(rx, ry, subW, subH);

            gc.setFill(titleCol);
            gc.setFont(javafx.scene.text.Font.font(11));
            gc.fillText(params[i].title, rx + 8, ry + 16);

            double padL = rx + 30;
            double padT = ry + 25;
            double cW = subW - 40;
            double cH = subH - 35;
            double mStep = cW / 11.0;

            double[] minArr = getArr(params[i], 0);
            double[] avgArr = getArr(params[i], 1);
            double[] maxArr = getArr(params[i], 2);

            drawSingleCurve(minArr, Color.web("#0284c7"), padL, padT, mStep, cH, params[i]);
            drawSingleCurve(avgArr, Color.web("#eab308"), padL, padT, mStep, cH, params[i]);
            drawSingleCurve(maxArr, Color.web("#ef4444"), padL, padT, mStep, cH, params[i]);
        }
    }

    private void drawSingleCurve(double[] arr, Color col, double padL, double padT, double mStep, double chartH) {
        drawSingleCurve(arr, col, padL, padT, mStep, chartH, activeParam);
    }

    private void drawSingleCurve(double[] arr, Color col, double padL, double padT, double mStep, double chartH, ParameterType param) {
        gc.setStroke(col);
        gc.setLineWidth(2.0);
        gc.beginPath();
        for (int m = 0; m < 12; m++) {
            double x = padL + m * mStep;
            double y = valToY(arr[m], param, padT, chartH);
            if (m == 0) gc.moveTo(x, y);
            else gc.lineTo(x, y);
        }
        gc.stroke();
    }

    private void drawHandle(double x, double y, Color color, boolean isHovered) {
        gc.setFill(isHovered ? Color.WHITE : color);
        gc.fillOval(x - 4, y - 4, 8, 8);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.0);
        gc.strokeOval(x - 4, y - 4, 8, 8);
    }

    private double valToY(double val, ParameterType param, double padT, double chartH) {
        double range = param.maxVal - param.minVal;
        double clamped = Math.max(param.minVal, Math.min(param.maxVal, val));
        return padT + chartH * (1.0 - (clamped - param.minVal) / range);
    }

    private double yToVal(double y, ParameterType param, double padT, double chartH) {
        double range = param.maxVal - param.minVal;
        double norm = 1.0 - (y - padT) / chartH;
        norm = Math.max(0.0, Math.min(1.0, norm));
        return param.minVal + norm * range;
    }

    private double[] getArr(ParameterType param, int curve) {
        return switch (param) {
            case TEMPERATURE -> curve == 0 ? tempMin : (curve == 1 ? tempAvg : tempMax);
            case WIND -> curve == 0 ? windMin : (curve == 1 ? windAvg : windMax);
            case RAIN -> curve == 0 ? rainMin : (curve == 1 ? rainAvg : rainMax);
            case HUMIDITY -> curve == 0 ? humidityMin : (curve == 1 ? humidityAvg : humidityMax);
            case OVERVIEW -> tempAvg;
        };
    }

    private void setParamVal(ParameterType param, int curve, int month, double val) {
        double[] arr = getArr(param, curve);
        arr[month] = val;
    }

    public ParameterType getActiveParam() {
        return activeParam;
    }

    public void setActiveParam(ParameterType type) {
        this.activeParam = type;
        if (paramToggleGroup != null) {
            for (javafx.scene.control.Toggle t : paramToggleGroup.getToggles()) {
                if (t.getUserData() == type) {
                    paramToggleGroup.selectToggle(t);
                    break;
                }
            }
        }
    }

    private static class ResizableCanvas extends Canvas {
        public ResizableCanvas(double width, double height) {
            super(width, height);
        }

        @Override
        public boolean isResizable() {
            return true;
        }

        @Override
        public double prefWidth(double height) {
            return getWidth();
        }

        @Override
        public double prefHeight(double width) {
            return getHeight();
        }
    }
}
