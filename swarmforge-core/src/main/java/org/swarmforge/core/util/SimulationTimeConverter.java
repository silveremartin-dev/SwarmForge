/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.util;

import java.util.Locale;

/**
 * Converter utility for mapping raw simulation ticks to human-readable SI metric time units.
 * Supports flexible formatting based on active master simulation clock configuration.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public final class SimulationTimeConverter {

    public static final float DEFAULT_STEP_SECONDS = 1.0f; // 1 tick = 1s standard in SwarmForge

    private SimulationTimeConverter() {
        // Utility class
    }

    /**
     * Converts a tick count to raw seconds based on simulation step duration in seconds.
     */
    public static double ticksToSeconds(long ticks, float stepSeconds) {
        return ticks * (double) (stepSeconds > 0 ? stepSeconds : DEFAULT_STEP_SECONDS);
    }

    /**
     * Converts a tick count to raw seconds using the default 1.0s step duration.
     */
    public static double ticksToSeconds(long ticks) {
        return ticksToSeconds(ticks, DEFAULT_STEP_SECONDS);
    }

    /**
     * Formats simulation ticks into a concise SI metric time string.
     * Examples: "0 s", "45 s", "2 min 15 s", "1.5 h", "3.2 j", "1.5 ans"
     */
    public static String formatTicks(long ticks, float stepSeconds) {
        double totalSeconds = ticksToSeconds(ticks, stepSeconds);
        return formatSeconds(totalSeconds);
    }

    public static String formatTicks(long ticks) {
        return formatTicks(ticks, DEFAULT_STEP_SECONDS);
    }

    /**
     * Formats total simulation seconds into human-readable SI metric units.
     */
    public static String formatSeconds(double totalSeconds) {
        if (Double.isNaN(totalSeconds) || Double.isInfinite(totalSeconds) || totalSeconds <= 0) {
            return "0 s";
        }

        if (totalSeconds < 60.0) {
            if (totalSeconds == (long) totalSeconds) {
                return String.format(Locale.ROOT, "%.2f s", totalSeconds);
            }
            return String.format(Locale.ROOT, "%.2f s", totalSeconds);
        }

        double totalMinutes = totalSeconds / 60.0;
        if (totalMinutes < 60.0) {
            long mins = (long) totalMinutes;
            double remSecs = totalSeconds % 60.0;
            if (remSecs == 0.0) {
                return String.format(Locale.ROOT, "%d min", mins);
            }
            if (remSecs == (long) remSecs) {
                return String.format(Locale.ROOT, "%d min %d s", mins, (long) remSecs);
            }
            return String.format(Locale.ROOT, "%d min %.2f s", mins, remSecs);
        }

        double totalHours = totalMinutes / 60.0;
        if (totalHours < 24.0) {
            long hrs = (long) totalHours;
            long mins = (long) (totalMinutes % 60.0);
            if (mins == 0) {
                return String.format(Locale.ROOT, "%d h", hrs);
            }
            return String.format(Locale.ROOT, "%d h %d min", hrs, mins);
        }

        double totalDays = totalHours / 24.0;
        if (totalDays < 365.0) {
            if (totalDays == (long) totalDays) {
                return String.format(Locale.ROOT, "%d j", (long) totalDays);
            }
            return String.format(Locale.ROOT, "%.1f j", totalDays);
        }

        double totalYears = totalDays / 365.0;
        if (totalYears == (long) totalYears) {
            return String.format(Locale.ROOT, "%d ans", (long) totalYears);
        }
        return String.format(Locale.ROOT, "%.1f ans", totalYears);
    }

    /**
     * Returns technical tooltip text combining SI formatted time with raw tick count.
     * Example: "1 min 30 s\n(Tick #90 | step=1.00s)"
     */
    public static String getTechnicalTooltip(long ticks, float stepSeconds) {
        String formatted = formatTicks(ticks, stepSeconds);
        return String.format(Locale.ROOT, "%s\n(Tick #%d | dt=%.2fs)", formatted, ticks, stepSeconds > 0 ? stepSeconds : DEFAULT_STEP_SECONDS);
    }
}
