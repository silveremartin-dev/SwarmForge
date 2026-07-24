/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.world.providers;

import org.swarmforge.core.world.ElevationProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/**
 * Elevation provider using OpenTopography API (SRTM data).
 * 
 * @author Gemini AI Assistant
 */
public class OpenTopographyProvider implements ElevationProvider {

    private static final String API_URL = "https://portal.opentopography.org/API/globaldem";
    private static final String DATASET = "SRTMGL1"; // 30m resolution

    @Override
    public String getSourceName() {
        return "OpenTopography (SRTMGL1)";
    }

    @Override
    public boolean hasDataFor(double latitude, double longitude) {
        // SRTM covers approx 56S to 60N
        return latitude >= -56 && latitude <= 60;
    }

    @Override
    public float getElevation(double latitude, double longitude) {
        // Not implemented for single point efficiency, use grid
        return 0;
    }

    @Override
    public float[][] getElevationGrid(double minLat, double maxLat, double minLon, double maxLon, int resolution) {
        // OpenTopography API Request
        // demtype=SRTMGL1&south=minLat&north=maxLat&west=minLon&east=maxLon&outputFormat=AAIGrid

        try {
            String urlStr = String.format(
                    "%s?demtype=%s&south=%.6f&north=%.6f&west=%.6f&east=%.6f&outputFormat=AAIGrid",
                    API_URL, DATASET, minLat, maxLat, minLon, maxLon);

            URL url = new URI(urlStr).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);

            if (conn.getResponseCode() != 200) {
                throw new IOException("API Request failed: " + conn.getResponseCode());
            }

            return parseAAIGrid(new BufferedReader(new InputStreamReader(conn.getInputStream())));

        } catch (Exception e) {
            System.err.println("Failed to fetch elevation data: " + e.getMessage());
            return new float[0][0]; // Return empty grid on failure
        }
    }

    private float[][] parseAAIGrid(BufferedReader reader) throws IOException {
        int ncols = 0;
        int nrows = 0;
        float noData = -9999;

        // Store header info
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty())
                continue;

            if (Character.isDigit(line.charAt(0)) || line.charAt(0) == '-') {
                // Found numeric data, rewind or handle?
                // BufferedReader doesn't support rewind easily unless marked.
                // We assume header comes first.
                // If we hit numbers, we successfully parsed header and this is data.
                break;
            }

            String[] parts = line.split("\\s+");
            if (parts.length >= 2) {
                switch (parts[0].toLowerCase()) {
                    case "ncols" -> ncols = Integer.parseInt(parts[1]);
                    case "nrows" -> nrows = Integer.parseInt(parts[1]);
                    case "nodata_value" -> noData = Float.parseFloat(parts[1]);
                }
            }
        }

        if (ncols == 0 || nrows == 0)
            return new float[0][0];

        float[][] grid = new float[nrows][ncols];
        int r = 0;
        int c = 0;

        // Parse logic for 'line' (current) and subsequent lines
        // Use a Scanner or manual split

        java.util.Scanner scanner = new java.util.Scanner(reader); // Wrap remainder
        // Prepend valid data from the line that broke the loop
        if (line != null && (Character.isDigit(line.charAt(0)) || line.charAt(0) == '-')) {
            // This line contains data. We need to feed it to scanner along with reader?
            // Since we can't easily concatenate, let's just parse this line manually first
            String[] vals = line.split("\\s+");
            for (String v : vals) {
                float val = Float.parseFloat(v);
                grid[r][c] = (val == noData) ? Float.NaN : val;
                c++;
                if (c >= ncols) {
                    c = 0;
                    r++;
                }
            }
        }

        while (scanner.hasNextFloat() && r < nrows) {
            float val = scanner.nextFloat();
            grid[r][c] = (val == noData) ? Float.NaN : val;
            c++;
            if (c >= ncols) {
                c = 0;
                r++;
            }
        }

        scanner.close();
        return grid;
    }

    // Improved parser using stream tokenizing approach would be better,
    // but let's implement a simpler version that fills the buffer.

}
