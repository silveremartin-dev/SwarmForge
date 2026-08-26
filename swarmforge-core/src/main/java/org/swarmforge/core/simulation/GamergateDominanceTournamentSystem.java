package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles gamergate physical dominance tournaments with abdominal rubbing and sting-smearing to establish reproductive status.
 */
public class GamergateDominanceTournamentSystem {

    public void processGamergateTournament(Individual gamergateCandidate, Individual rivalWorker) {
        if (gamergateCandidate == null || rivalWorker == null) return;
        Species species = gamergateCandidate.getSpecies();
        if (species == null || !species.canPerformGamergateDominanceTournament()) return;

        if (gamergateCandidate.isAlive() && rivalWorker.isAlive()) {
            gamergateCandidate.setEnergy(Math.max(0.0f, gamergateCandidate.getEnergy() - 0.02f));
        }
    }
}
