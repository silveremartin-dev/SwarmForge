package org.swarmforge.client.ui;

import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.ThemeManager;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Handles exporting simulation statistics history to CSV files for reproducibility and scientific analysis.
 */
public final class StatisticsCsvExporter {

    private StatisticsCsvExporter() {
        // Utility class
    }

    public static void exportToCSV(Window parentWindow, String scenarioName, List<? extends ColonyStats> historyList, I18nManager i18n) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(i18n.get("stats.export.dialog_title", "Export Detailed Statistical Analysis (CSV / Excel)"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
        String sanitizedScenario = (scenarioName != null && !scenarioName.isBlank())
                ? scenarioName.trim().toLowerCase().replaceAll("[^a-z0-9_-]", "_").replaceAll("_+", "_")
                : "swarmforge";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        fileChooser.setInitialFileName(sanitizedScenario + "_analytics_" + timestamp + ".csv");

        File file = fileChooser.showSaveDialog(parentWindow);
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
                writer.println("# SwarmForge Simulation Analytics Export");
                writer.println("# Export_Date; " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                writer.println("# Total_Records; " + historyList.size());
                writer.println("Local_Timestamp;Simulated_Time_Seconds;Formatted_Duration;Engine_Tick;dt_Step_Sec;Total_Population;Queens;Workers;Soldiers;Foraging;Digging;Nursing;Guarding;Royal_Care;Resting;Food;Water;Proteins;Temperature_C;Rainfall_mm;TPS_Speed;Births;Deaths;Active_Event");

                for (ColonyStats s : historyList) {
                    double timeSec = s.getSimTimeSeconds();
                    String timeFormatted = formatTime(s.simTicks, s.stepTimeSeconds);
                    String localTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                    writer.printf("%s;%.2f;%s;%d;%.4f;%d;%d;%d;%d;%d;%d;%d;%d;%d;%d;%.2f;%.2f;%.2f;%.1f;%.1f;%.2f;%d;%d;%s%n",
                            localTime, timeSec, timeFormatted, s.simTicks, s.stepTimeSeconds,
                            s.population, s.queens, s.workers, s.soldiers,
                            s.stateForaging, s.stateDigging, s.stateNursing, s.stateGuarding, s.stateRoyalCare, s.stateResting,
                            s.food, s.water, s.protein, s.temperature, s.rainfall,
                            s.tickRate, s.births, s.deaths, s.activeEvent);
                }
                ThemeManager.createAlert(Alert.AlertType.INFORMATION, i18n.get("stats.export.success", file.getAbsolutePath())).show();
            } catch (Exception ex) {
                ThemeManager.createAlert(Alert.AlertType.ERROR, i18n.get("stats.export.error", ex.getMessage())).show();
            }
        }
    }

    public static String formatTime(long ticks, double stepSeconds) {
        double totalSeconds = ticks * stepSeconds;
        long wholeSec = (long) totalSeconds;
        long hours = wholeSec / 3600;
        long minutes = (wholeSec % 3600) / 60;
        long seconds = wholeSec % 60;
        long cs = (long) Math.round((totalSeconds - Math.floor(totalSeconds)) * 100);
        if (cs >= 100) cs = 99;
        long simulatedDay = 1 + (wholeSec / 86400);
        return String.format("Day %d | %02dh %02dm %02ds %02dcs", simulatedDay, hours, minutes, seconds, cs);
    }
}
