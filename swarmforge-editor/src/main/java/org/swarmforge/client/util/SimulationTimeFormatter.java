/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.util;

/**
 * Utility class for formatting simulation time and tick numbers into human-readable metric durations.
 * Adheres strictly to AGENTS.md directives regarding metric temporal representations.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public final class SimulationTimeFormatter {

    private SimulationTimeFormatter() {
        // Utility class
    }

    /**
     * Formats elapsed simulation seconds into days, hours, minutes, seconds and centiseconds.
     *
     * @param totalSeconds the elapsed duration in seconds
     * @return a formatted human-readable duration string
     */
    public static String formatSimulationTimeSeconds(double totalSeconds) {
        long wholeSec = (long) totalSeconds;
        long days = wholeSec / 86400;
        long hours = (wholeSec % 86400) / 3600;
        long minutes = (wholeSec % 3600) / 60;
        long seconds = wholeSec % 60;
        long cs = (long) Math.round((totalSeconds - Math.floor(totalSeconds)) * 100);
        if (cs >= 100) cs = 99;
        if (days > 0) {
            return String.format("%dj %02dh %02dm %02ds", days, hours, minutes, seconds);
        }
        return String.format("%02dh %02dm %02ds %02dcs", hours, minutes, seconds, cs);
    }

    /**
     * Formats simulation ticks and step duration into human-readable temporal units.
     *
     * @param tick the simulation tick index
     * @param stepSeconds the physical duration of one simulation step in seconds
     * @return a formatted human-readable duration string
     */
    public static String formatSimulationTime(long tick, double stepSeconds) {
        return formatSimulationTimeSeconds(tick * stepSeconds);
    }

    /**
     * Calculates the maximum simulation ticks from a duration value, unit, and step delta.
     *
     * @param val the numeric duration value
     * @param unit the time unit (Seconds, Minutes, Hours, Days, Months, Years, or Ticks)
     * @param dt the step duration in seconds
     * @return the total number of simulation ticks
     */
    public static long calculateMaxSimulationTicks(Double val, String unit, double dt) {
        if (unit != null && (unit.contains("Unlimited") || unit.contains("Illimité"))) {
            return Long.MAX_VALUE;
        }
        double numericVal = (val != null) ? val : 100_000.0;
        double stepDt = (dt > 0) ? dt : 0.016666666666666666;

        double ticks;
        if (unit != null && (unit.startsWith("Seconds") || unit.startsWith("Secondes"))) {
            ticks = numericVal / stepDt;
        } else if (unit != null && unit.startsWith("Minutes")) {
            ticks = (numericVal * 60.0) / stepDt;
        } else if (unit != null && (unit.startsWith("Hours") || unit.startsWith("Heures"))) {
            ticks = (numericVal * 3600.0) / stepDt;
        } else if (unit != null && (unit.startsWith("Days") || unit.startsWith("Jours"))) {
            ticks = (numericVal * 86400.0) / stepDt;
        } else if (unit != null && (unit.startsWith("Months") || unit.startsWith("Mois"))) {
            ticks = (numericVal * 30.0 * 86400.0) / stepDt;
        } else if (unit != null && (unit.startsWith("Years") || unit.startsWith("Années"))) {
            ticks = (numericVal * 365.25 * 86400.0) / stepDt;
        } else {
            ticks = numericVal;
        }
        return (long) Math.min(Long.MAX_VALUE, Math.max(1.0, ticks));
    }
}
