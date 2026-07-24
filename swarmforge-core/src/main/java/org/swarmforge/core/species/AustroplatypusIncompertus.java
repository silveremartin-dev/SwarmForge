/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.species;

import org.swarmforge.core.domain.CasteTemplate;
import java.util.List;

/**
 * Austroplatypus incompertus - Eusocial Ambrosia Wood Beetle
 * The only known eusocial beetle species, tunneling into live Eucalyptus trees and cultivating Ambrosia fungi.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class AustroplatypusIncompertus extends CustomSpecies {

    public AustroplatypusIncompertus() {
        setPresetName("Scolyte Eusocial (Austroplatypus incompertus)");
        setCommonName("Ambrosia Wood Beetle");
        setScientificName("Austroplatypus incompertus");
        setInsectType("BEETLE");
        setDescription("Seul coléoptère eusocial connu. Creuse des galeries dans les eucalyptus et cultive des champignons ambroisie.");
        setQueenCountMode("MONOGYNE");
        setQueenCount(1);
        setQueenLifespan(365 * 3);
        setQueenEggLayingRate(20.0f);
        setWorkerLifespan(365 * 2);
        setWorkerSpeed(0.4f);
        setViewDistance(2.5f);
        setWorkersCanFly(false);
        setTypicalColonySize(200);
        setPrimaryDiet("FUNGUS");
        setSecondaryDiet("MULCH");
        setNestType("WOOD_TUNNELS");
        setVenomType("HARDENED_ELyTRA");
        setAggression(0.5f);

        CasteTemplate queen = new CasteTemplate("Reine Reproductrice", 250f, 8f);
        queen.setCanDig(true);
        queen.setBodyLengthMm(6.0f);
        queen.setHeadWidthMm(1.8f);

        CasteTemplate sterileWorker = new CasteTemplate("Ouvrière Stérile Défensive", 110f, 15f);
        sterileWorker.setCanDig(true);
        sterileWorker.setBaseDefense(8f);
        sterileWorker.setBodyLengthMm(5.5f);
        sterileWorker.setHeadWidthMm(1.6f);

        setCasteTemplates(List.of(queen, sterileWorker));
    }

    @Override
    public InsectOrder getInsectOrder() {
        return InsectOrder.BEETLE;
    }
}
