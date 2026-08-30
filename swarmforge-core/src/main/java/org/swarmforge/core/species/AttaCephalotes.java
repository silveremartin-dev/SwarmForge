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
 * Atta cephalotes - Leafcutter Ant
 * Complex fungus-farming species from South America.
 * Refactored to extend CustomSpecies for full JSON & Species Editor parameter compatibility.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class AttaCephalotes extends CustomSpecies {

    public AttaCephalotes() {
        setPresetName("Leafcutter Ant (Atta cephalotes)");
        setCommonName("Leafcutter Ant");
        setScientificName("Atta cephalotes");
        setInsectType("ANT");
        setDescription("Harvests foliage to cultivate a symbiotic fungus garden inside immense subterranean nests.");
        setQueenCountMode("MONOGYNE");
        setQueenCount(1);
        setQueenLifespan(365 * 20);
        setQueenEggLayingRate(100.0f);
        setWorkerLifespan(365 * 2);
        setWorkerSpeed(0.6f);
        setViewDistance(4.0f);
        setTypicalColonySize(500000);
        setPrimaryDiet("FUNGUS");
        setSecondaryDiet("SUGARS_NECTAR");
        setNestType("UNDERGROUND_BURROW");
        setVenomType("POWERFUL_MANDIBLES");
        setAggression(0.5f);
        setCanWeedFungusGarden(true);

        CasteTemplate queen = new CasteTemplate("Reine Géante", 1000f, 20f);
        queen.setLifespan(365 * 20);
        queen.setBodyLengthMm(30.0f);
        queen.setHeadWidthMm(7.0f);

        CasteTemplate minima = new CasteTemplate("Ouvrière Minime (Nourrice)", 40f, 2f);
        minima.setBodyLengthMm(2.0f);
        minima.setHeadWidthMm(0.6f);

        CasteTemplate media = new CasteTemplate("Ouvrière Média (Coupeuse)", 100f, 10f);
        media.setCanCarry(true);
        media.setBodyLengthMm(7.0f);
        media.setHeadWidthMm(2.2f);

        CasteTemplate major = new CasteTemplate("Soldat Majeur (Garde)", 300f, 45f);
        major.setBaseDefense(8f);
        major.setBodyLengthMm(16.0f);
        major.setHeadWidthMm(6.0f);

        setCasteTemplates(List.of(queen, minima, media, major));
    }

    @Override
    public java.util.Set<org.swarmforge.core.domain.ResourceType> getForagingTypes() {
        return java.util.Set.of(
                org.swarmforge.core.domain.ResourceType.LEAF,
                org.swarmforge.core.domain.ResourceType.FUNGUS);
    }
}
