/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.world;

/**
 * Interface for elevation data providers.
 * Implementations can provide real-world elevation data from sources like SRTM.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public interface ElevationProvider {

    /**
     * Get elevation at given coordinates.
     * 
     * @param latitude  Latitude in degrees
     * @param longitude Longitude in degrees
     * @return Elevation in meters above sea level
     */
    float getElevation(double latitude, double longitude);

    /**
     * Get elevation for an area as a 2D array.
     * 
     * @param minLat     Minimum latitude
     * @param maxLat     Maximum latitude
     * @param minLon     Minimum longitude
     * @param maxLon     Maximum longitude
     * @param resolution Number of samples per degree
     * @return 2D array of elevations [lat][lon]
     */
    float[][] getElevationGrid(double minLat, double maxLat,
            double minLon, double maxLon, int resolution);

    /**
     * Check if data is available for the given location.
     */
    boolean hasDataFor(double latitude, double longitude);

    /**
     * Get the data source name.
     */
    String getSourceName();
}
