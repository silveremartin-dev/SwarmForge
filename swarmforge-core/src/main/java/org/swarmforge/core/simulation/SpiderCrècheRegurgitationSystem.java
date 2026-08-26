package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles maternal liquefied gut regurgitation feeding of social spider crèches.
 */
public class SpiderCrècheRegurgitationSystem {

    public void processCrècheRegurgitation(Individual motherSpider, boolean spiderlingClusterPresent) {
        if (motherSpider == null) return;
        Species species = motherSpider.getSpecies();
        if (species == null || !species.canPerformCrècheRegurgitationSpider()) return;

        if (spiderlingClusterPresent && motherSpider.isAlive()) {
            motherSpider.setEnergy(Math.max(0.0f, motherSpider.getEnergy() - 0.03f));
        }
    }
}
