/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.TerrariumCell;

/**
 * Insect Kinematics, Appendage Collision & Wall Climbing Adhesion (Arolia Pads).
 * Evaluates thigmotactic antennae wall contact, leg span clearance in tunnels,
 * and adhesive pad suction forces on vertical and ceiling surfaces.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class InsectKinematics {

    /**
     * Evaluates if an insect can adhere to and climb a vertical wall or ceiling
     * based on substrate material, roughness, and relative humidity (arolium liquid film).
     *
     * @param material Surface material type
     * @param surfaceRoughness Substrate roughness index (0.0 to 1.0)
     * @param relativeHumidity Humidity percentage (0% to 100%)
     * @param insectWeightMg Insect body mass in milligrams
     * @return true if insect can adhere and climb upside down
     */
    public static boolean canAdhereToSurface(TerrariumCell.Material material, float surfaceRoughness, float relativeHumidity, float insectWeightMg) {
        if (material == TerrariumCell.Material.AIR) return false;

        // Arolia adhesion requires a thin capillary liquid film (humidity > 20%)
        float capillaryAdhesionForce = (relativeHumidity / 100.0f) * 15.0f; // Force in mN

        // Mechanical interlocking from tarsal claws on rough surfaces
        float clawInterlockForce = surfaceRoughness * 25.0f; // Force in mN

        float totalAdhesionMN = capillaryAdhesionForce + clawInterlockForce;
        float gravityWeightMN = (insectWeightMg * 0.00981f); // Weight in mN

        // Insect can climb ceiling if total holding force exceeds body weight
        return totalAdhesionMN >= gravityWeightMN * 1.2f; // 20% safety factor
    }

    /**
     * Evaluates thigmotactic antennae wall contact in narrow tunnels.
     * Returns optimal locomotion speed multiplier (0.4 to 1.2).
     */
    public static float computeThigmotaxisSpeedMultiplier(float antWidthMm, float tunnelDiameterMm) {
        if (tunnelDiameterMm < antWidthMm) return 0.0f; // Blocked completely
        float ratio = tunnelDiameterMm / antWidthMm;

        if (ratio < 1.3f) {
            // Optimal thigmotactic contact: ant touches walls with antennae and moves fast
            return 1.15f;
        } else if (ratio < 3.0f) {
            return 1.00f; // Normal gallery movement
        } else {
            return 0.85f; // Open space: slower wall-following behavior
        }
    }

    /**
     * Checks if a predator or intruder of width predatorWidthMm can enter a gallery
     * of effective diameter galleryDiameterMm.
     */
    public static boolean canEnterGallery(float predatorWidthMm, float galleryDiameterMm) {
        return galleryDiameterMm >= predatorWidthMm * 1.05f; // 5% clearance required
    }
}
