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
 * Propolis & Chemical Shield Fortress System.
 * Models tree resin foraging by honeybees (Apis mellifera) to coat nest walls with antimicrobial propolis,
 * reducing colony pathogen load and sealing structural drafts.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class PropolisShieldSystem {

    private float propolisCoveragePercent = 0.0f;

    public void applyPropolisResin(Individual worker, float resinAmount) {
        if (worker == null || worker.getSpecies() == null) return;
        if (!worker.getSpecies().canCollectPropolis()) return;

        this.propolisCoveragePercent = Math.min(100.0f, propolisCoveragePercent + resinAmount * 2.0f);
    }

    public float getPathogenTransmissionMultiplier() {
        // High propolis coverage reduces bacterial/fungal pathogen spread by up to 60%
        return 1.0f - (propolisCoveragePercent / 100.0f) * 0.60f;
    }

    public float getPropolisCoveragePercent() {
        return propolisCoveragePercent;
    }
}
