/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;

/**
 * Magnetoreception & Geomagnetic Navigation System.
 * Enables magnetic orientation in termites (Amitermes meridionalis) and desert ants (Cataglyphis)
 * based on Earth's geomagnetic field inclination and intensity (~50 microTesla).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class MagnetoreceptionSystem {

    private static final float GEOMAGNETIC_NORTH_ANGLE_RAD = 0.0f; // 0 degrees North
    private static final float GEOMAGNETIC_FIELD_INTENSITY_UT = 50.0f; // 50 µT

    public static float calculateGeomagneticHeadingHeadingCorrection(Individual ind, float currentHeading) {
        if (ind == null || ind.getSpecies() == null || !ind.getSpecies().hasMagnetoreception()) {
            return currentHeading;
        }

        float sensitivity = ind.getSpecies().getMagnetoreceptionSensitivity();
        if (sensitivity <= 0) return currentHeading;

        // Correct heading towards alignment with North-South axis (magnetic mound orientation)
        float targetAngle = (currentHeading > Math.PI / 2 && currentHeading < 3 * Math.PI / 2) ? (float) Math.PI : 0.0f;
        float diff = targetAngle - currentHeading;

        return currentHeading + diff * 0.05f; // Gentle magnetic alignment pull
    }

    public static void alignMoundStructureGeomagnetically(Colony colony) {
        if (colony == null || colony.getSpecies() == null || !colony.getSpecies().hasMagnetoreception()) {
            return;
        }
        // Align primary mound axis to North-South to minimize solar thermal flux
    }
}
