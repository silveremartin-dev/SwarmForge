/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.weather;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.swarmforge.client.ui.weather.WeatherCurvesEditorPane.ParameterType;
import org.swarmforge.client.util.ThemeManager;

/**
 * Renders spline curves, grid axes, interactive handles, and multi-parameter overview grids
 * onto the WeatherCurvesEditorPane graphics canvas.
 */
class WeatherCurvesCanvasRenderer {

    static void redrawCurves(
            GraphicsContext gc,
            double w,
            double h,
            ParameterType activeParam,
            WeatherCurvesDataProvider provider,
            int draggedMonth,
            int draggedCurve,
            String hoverInfoText
    ) {
        if (gc == null || w < 10 || h < 10) return;

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
            drawOverviewMode(gc, w, h, provider);
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
            gc.fillText(WeatherCurvesEditorPane.MONTH_KEYS[m], x - 10, h - 10);
        }

        double[] minArr = provider.getArr(activeParam, 0);
        double[] avgArr = provider.getArr(activeParam, 1);
        double[] maxArr = provider.getArr(activeParam, 2);

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

        drawSingleCurve(gc, minArr, Color.web("#0284c7"), padL, padT, monthStep, chartH, activeParam);
        drawSingleCurve(gc, avgArr, Color.web("#eab308"), padL, padT, monthStep, chartH, activeParam);
        drawSingleCurve(gc, maxArr, Color.web("#ef4444"), padL, padT, monthStep, chartH, activeParam);

        for (int m = 0; m < 12; m++) {
            double x = padL + m * monthStep;
            drawHandle(gc, x, valToY(minArr[m], activeParam, padT, chartH), Color.web("#0284c7"), draggedMonth == m && draggedCurve == 0);
            drawHandle(gc, x, valToY(avgArr[m], activeParam, padT, chartH), Color.web("#eab308"), draggedMonth == m && draggedCurve == 1);
            drawHandle(gc, x, valToY(maxArr[m], activeParam, padT, chartH), Color.web("#ef4444"), draggedMonth == m && draggedCurve == 2);
        }

        if (hoverInfoText != null && !hoverInfoText.isEmpty()) {
            gc.setFill(tooltipBg);
            gc.setStroke(Color.web("#0284c7"));
            gc.fillRect(padL + 10, padT + 5, 290, 24);
            gc.strokeRect(padL + 10, padT + 5, 290, 24);

            gc.setFill(tooltipText);
            gc.setFont(javafx.scene.text.Font.font("System", 11));
            gc.fillText(hoverInfoText, padL + 18, padT + 21);
        }
    }

    private static void drawOverviewMode(GraphicsContext gc, double w, double h, WeatherCurvesDataProvider provider) {
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

            double[] minArr = provider.getArr(params[i], 0);
            double[] avgArr = provider.getArr(params[i], 1);
            double[] maxArr = provider.getArr(params[i], 2);

            drawSingleCurve(gc, minArr, Color.web("#0284c7"), padL, padT, mStep, cH, params[i]);
            drawSingleCurve(gc, avgArr, Color.web("#eab308"), padL, padT, mStep, cH, params[i]);
            drawSingleCurve(gc, maxArr, Color.web("#ef4444"), padL, padT, mStep, cH, params[i]);
        }
    }

    static void drawSingleCurve(GraphicsContext gc, double[] arr, Color col, double padL, double padT, double mStep, double chartH, ParameterType param) {
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

    private static void drawHandle(GraphicsContext gc, double x, double y, Color color, boolean isHovered) {
        gc.setFill(isHovered ? Color.WHITE : color);
        gc.fillOval(x - 4, y - 4, 8, 8);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.0);
        gc.strokeOval(x - 4, y - 4, 8, 8);
    }

    static double valToY(double val, ParameterType param, double padT, double chartH) {
        double range = param.maxVal - param.minVal;
        double clamped = Math.max(param.minVal, Math.min(param.maxVal, val));
        return padT + chartH * (1.0 - (clamped - param.minVal) / range);
    }

    static double yToVal(double y, ParameterType param, double padT, double chartH) {
        double range = param.maxVal - param.minVal;
        double norm = Math.max(0.0, Math.min(1.0, 1.0 - (y - padT) / chartH));
        return param.minVal + norm * range;
    }

    interface WeatherCurvesDataProvider {
        double[] getArr(ParameterType param, int curve);
    }
}
