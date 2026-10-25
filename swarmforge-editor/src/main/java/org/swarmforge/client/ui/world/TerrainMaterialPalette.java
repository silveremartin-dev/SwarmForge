/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.world;

import javafx.scene.paint.Color;

/**
 * Standard stratigraphic material color palette for soil layers, voxels, and minimaps.
 */
public final class TerrainMaterialPalette {

    private TerrainMaterialPalette() {
    }

    /**
     * Maps a soil material identifier byte to its representative JavaFX rendering color.
     *
     * @param mat material byte identifier (0: Humus, 1: Sand, 2: Clay, 3: Stone, 4: Gravel, 5: Silt, 6: Peat, 7: Cavity, 8: Organic Litter)
     * @return JavaFX Color
     */
    public static Color getMaterialColor(byte mat) {
        return switch (mat) {
            case 1 -> Color.web("#facc15"); // Sand (Bright Golden Yellow)
            case 2 -> Color.web("#c2410c"); // Clay (Warm Terracotta Red)
            case 3 -> Color.web("#64748b"); // Stone (Pierre / Roche)
            case 4 -> Color.web("#cbd5e1"); // Gravel (Gravier / Cailloutis)
            case 5 -> Color.web("#d97706"); // Silt (Limon)
            case 6 -> Color.web("#78350f"); // Peat (Tourbe)
            case 7 -> Color.web("#0f172a"); // Cavity / Natural Void
            case 8 -> Color.web("#b45309"); // Organic Litter
            default -> Color.web("#4d7c0f"); // Earth / Humus (Vibrant Forest Topsoil)
        };
    }
}
