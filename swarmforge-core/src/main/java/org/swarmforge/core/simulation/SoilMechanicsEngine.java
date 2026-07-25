/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.TerrariumCell;

/**
 * Advanced Geomechanics Engine for Soil Stability, Mohr-Coulomb Rupture & Arch Collapse.
 * Simulates real-world soil cohesion, suction matrix, sand angle of repose,
 * and maximum span calculation for excavated gallery ceilings.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SoilMechanicsEngine {

    /**
     * Evaluates Mohr-Coulomb shear strength tau = c' + (sigma - u_a) * tan(phi).
     *
     * @param material Soil substrate material type
     * @param compactionIndex Soil compaction percentage (10% to 100%)
     * @param relativeHumidity Relative soil moisture (0% to 100%)
     * @return Effective shear strength in kPa
     */
    public static float computeShearStrength(TerrariumCell.Material material, float compactionIndex, float relativeHumidity) {
        float cPrime; // Effective cohesion (kPa)
        float phi;    // Internal friction angle (degrees)

        switch (material) {
            case SAND:
                cPrime = (relativeHumidity > 15.0f && relativeHumidity < 85.0f) ? 1.5f : 0.0f; // Apparent cohesion from capillary suction
                phi = 30.0f + (compactionIndex * 0.1f);
                break;
            case CLAY:
                cPrime = 12.0f + (compactionIndex * 0.25f);
                if (relativeHumidity > 90.0f) cPrime *= 0.3f; // Saturation weakens clay
                phi = 18.0f;
                break;
            case ROCK:
                cPrime = 150.0f;
                phi = 45.0f;
                break;
            case ORGANIC:
                cPrime = 4.0f + (compactionIndex * 0.1f);
                phi = 25.0f;
                break;
            case EARTH:
            default:
                cPrime = 5.0f + (compactionIndex * 0.15f);
                if (relativeHumidity > 92.0f) cPrime *= 0.4f; // Liquefaction during flood
                phi = 24.0f;
                break;
        }

        // Mohr-Coulomb calculation
        double phiRad = Math.toRadians(phi);
        float normalStress = 5.0f + (compactionIndex * 0.05f); // Normal stress from upper soil load
        return (float) (cPrime + normalStress * Math.tan(phiRad));
    }

    /**
     * Calculates the maximum stable span S_max for an excavated gallery ceiling (in millimeters)
     * before structural collapse occurs. S_max = 2 * c' / gamma.
     */
    public static float computeMaxGallerySpanMm(TerrariumCell.Material material, float compactionIndex, float relativeHumidity) {
        if (material == TerrariumCell.Material.ROCK) return 500.0f; // Extremely stable
        float shearStrength = computeShearStrength(material, compactionIndex, relativeHumidity);
        float unitWeight = 16.0f; // Unit weight gamma (~16 kN/m^3)
        float spanMeters = (2.0f * shearStrength) / unitWeight;
        return Math.max(1.5f, Math.min(250.0f, spanMeters * 10.0f)); // Max span in mm
    }

    /**
     * Granulometry check: determines if an ant mandible of width W_mandible
     * can extract a particle/stone of given diameter.
     */
    public static boolean canMandibleExtract(float mandibleWidthMm, TerrariumCell.Material material, float particleDiameterMm) {
        if (material == TerrariumCell.Material.ROCK && particleDiameterMm > mandibleWidthMm * 1.2f) {
            return false; // Cannot extract pebbles larger than mandible span!
        }
        return true;
    }
}
