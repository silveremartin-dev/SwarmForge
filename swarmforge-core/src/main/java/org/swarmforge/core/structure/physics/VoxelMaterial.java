/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.structure.physics;

import java.io.Serializable;

/**
 * Defines physical material properties for voxel-based nest mechanics:
 * Density (kg/m3), Shear Strength (kPa), Cohesion (kPa), Thermal Conductivity (W/mK), Porosity (0-1).
 */
public enum VoxelMaterial implements Serializable {
    AIR(1.225f, 0.0f, 0.0f, 0.026f, 1.0f),
    SOIL(1400.0f, 15.0f, 10.0f, 0.85f, 0.40f),
    CLAY(1800.0f, 45.0f, 35.0f, 1.25f, 0.25f),
    SAND(1600.0f, 5.0f, 2.0f, 0.45f, 0.45f),
    STONE(2600.0f, 250.0f, 180.0f, 2.80f, 0.05f),
    CARTON(600.0f, 30.0f, 25.0f, 0.12f, 0.50f),
    RESIN_PROPOLIS(1100.0f, 80.0f, 75.0f, 0.18f, 0.10f),
    REINFORCED(2000.0f, 150.0f, 120.0f, 1.10f, 0.20f);

    private final float densityKgM3;
    private final float shearStrengthKPa;
    private final float cohesionKPa;
    private final float thermalConductivityWMK;
    private final float porosity;

    VoxelMaterial(float densityKgM3, float shearStrengthKPa, float cohesionKPa,
                  float thermalConductivityWMK, float porosity) {
        this.densityKgM3 = densityKgM3;
        this.shearStrengthKPa = shearStrengthKPa;
        this.cohesionKPa = cohesionKPa;
        this.thermalConductivityWMK = thermalConductivityWMK;
        this.porosity = porosity;
    }

    public float getDensityKgM3() {
        return densityKgM3;
    }

    public float getShearStrengthKPa() {
        return shearStrengthKPa;
    }

    public float getCohesionKPa() {
        return cohesionKPa;
    }

    public float getThermalConductivityWMK() {
        return thermalConductivityWMK;
    }

    public float getPorosity() {
        return porosity;
    }

    public boolean isSolid() {
        return this != AIR;
    }
}
