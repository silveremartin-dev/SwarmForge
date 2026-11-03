/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view;

import org.swarmforge.core.domain.CasteTemplate;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Calculates biological body dimensions, scale factors, and world-to-millimeter ratios
 * for 3D insect entity rendering.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class InsectScaleCalculator {

    private float terrainSideMeters = 10.0f;
    private int gridWidth = 64;

    public void setTerrainDimensions(float terrainSideMeters, int gridWidth) {
        if (terrainSideMeters > 0) this.terrainSideMeters = terrainSideMeters;
        if (gridWidth > 0) this.gridWidth = gridWidth;
    }

    public float getMmPerWorldUnit() {
        return (terrainSideMeters * 1000.0f) / Math.max(1, gridWidth);
    }

    public static float getIndividualBodyLengthMm(Individual.Caste caste, Species species, CasteTemplate template) {
        if (template != null && template.getBodyLengthMm() > 0) {
            return template.getBodyLengthMm();
        }
        if (species != null) {
            if (species.getCastes() != null) {
                for (CasteTemplate ct : species.getCastes()) {
                    if (matchesCaste(ct, caste) && ct.getBodyLengthMm() > 0) {
                        return ct.getBodyLengthMm();
                    }
                }
            }
            float avg = species.getAverageCasteBodyLengthMm();
            if (avg > 0) {
                return switch (caste != null ? caste : Individual.Caste.WORKER) {
                    case QUEEN -> avg * 2.2f;
                    case SOLDIER -> avg * 1.6f;
                    case MALE -> avg * 1.2f;
                    default -> avg;
                };
            }
        }
        return switch (caste != null ? caste : Individual.Caste.WORKER) {
            case QUEEN -> 14.0f;
            case SOLDIER -> 9.0f;
            case MALE -> 6.0f;
            default -> 4.5f;
        };
    }

    public static boolean matchesCaste(CasteTemplate ct, Individual.Caste caste) {
        if (ct == null || caste == null) return false;
        String name = ct.getName() != null ? ct.getName().toUpperCase() : "";
        CasteTemplate.CasteCategory cat = ct.getCategory();
        return switch (caste) {
            case QUEEN -> cat == CasteTemplate.CasteCategory.PRIMARY_REPRODUCTIVE
                    || cat == CasteTemplate.CasteCategory.SECONDARY_REPRODUCTIVE
                    || cat == CasteTemplate.CasteCategory.QUEEN_DICHTHADIIGYNE
                    || cat == CasteTemplate.CasteCategory.QUEEN_ERGATOGYNE
                    || name.contains("QUEEN") || name.contains("REINE");
            case SOLDIER -> cat == CasteTemplate.CasteCategory.SOLDIER_DEFENSIVE
                    || cat == CasteTemplate.CasteCategory.SUPER_SOLDIER
                    || cat == CasteTemplate.CasteCategory.SOLDIER_PHRAGMOTIC
                    || cat == CasteTemplate.CasteCategory.SOLDIER_NASUTE
                    || cat == CasteTemplate.CasteCategory.WORKER_MAJOR
                    || name.contains("SOLDIER") || name.contains("MAJOR");
            case MALE -> cat == CasteTemplate.CasteCategory.MALE_DRONE
                    || cat == CasteTemplate.CasteCategory.MALE_ERGATANDROUS
                    || name.contains("MALE") || name.contains("DRONE");
            default -> cat == CasteTemplate.CasteCategory.WORKER_MEDIA
                    || cat == CasteTemplate.CasteCategory.WORKER_MINIMA
                    || cat == CasteTemplate.CasteCategory.GENERAL_WORKER
                    || cat == CasteTemplate.CasteCategory.METABOLIC_REPLETE
                    || name.contains("WORKER") || name.contains("OUVRIERE");
        };
    }

    public float getScaleForIndividual(Individual.Caste caste, Individual.LifeStage stage, Species species, CasteTemplate template) {
        float mmPerWorldUnit = getMmPerWorldUnit();
        if (stage == Individual.LifeStage.EGG) return 0.8f / mmPerWorldUnit;
        if (stage == Individual.LifeStage.LARVA) return 2.2f / mmPerWorldUnit;
        if (stage == Individual.LifeStage.PUPA) return 4.0f / mmPerWorldUnit;
        return getIndividualBodyLengthMm(caste, species, template) / mmPerWorldUnit;
    }
}
