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
 * Camponotus pennsylvanicus - Black Carpenter Ant
 * Large wood-nesting species from North America.
 * Refactored to extend CustomSpecies for full JSON & Species Editor parameter compatibility.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class CamponotusPennsylvanicus extends CustomSpecies {

    public CamponotusPennsylvanicus() {
        setPresetName("Fourmi Charpentière Noire (Camponotus pennsylvanicus)");
        setCommonName("Black Carpenter Ant");
        setScientificName("Camponotus pennsylvanicus");
        setInsectType("ANT");
        setDescription("Large North American species nesting in decaying wood.");
        setQueenCountMode("MONOGYNE");
        setQueenCount(1);
        setQueenLifespan(365 * 25);
        setWorkerLifespan(365 * 7);
        setWorkerSpeed(0.4f);
        setViewDistance(5.0f);
        setTypicalColonySize(3000);
        setFormsMegaColonies(false);
        setPrimaryDiet("HONEYDEW");
        setSecondaryDiet("INSECTS_MEAT");
        setNestType("WOOD_TUNNELS");
        setVenomType("POWERFUL_MANDIBLES");
        setCanDrumSubstrate(true);

        CasteTemplate queen = new CasteTemplate("Reine", 750f, 15f);
        queen.setLifespan(365 * 25);
        queen.setCanFly(true);
        queen.setWalkSpeedMps(0.22f);
        queen.setFlySpeedMps(2.8f);
        queen.setBodyLengthMm(19.0f);
        queen.setHeadWidthMm(4.8f);

        CasteTemplate minor = new CasteTemplate("Ouvrière Mineure", 95f, 8f);
        minor.setLifespan(365 * 7);
        minor.setCanDig(true);
        minor.setCanCarry(true);
        minor.setWalkSpeedMps(0.38f);
        minor.setFlySpeedMps(0.0f);
        minor.setBodyLengthMm(7.5f);
        minor.setHeadWidthMm(1.9f);

        CasteTemplate worker = new CasteTemplate("Ouvrière Major", 190f, 18f);
        worker.setLifespan(365 * 7);
        worker.setCanDig(true);
        worker.setCanCarry(true);
        worker.setWalkSpeedMps(0.32f);
        worker.setFlySpeedMps(0.0f);
        worker.setBodyLengthMm(13.0f);
        worker.setHeadWidthMm(3.7f);

        CasteTemplate male = new CasteTemplate("Mâle Reproducteur (Alé)", 85f, 0f);
        male.setDescription("Mâle ailé pour le vol nuptial");
        male.setLifespan(30);
        male.setCanFly(true);
        male.setWalkSpeedMps(0.22f);
        male.setFlySpeedMps(3.2f);
        male.setBodyLengthMm(10.5f);
        male.setHeadWidthMm(2.3f);

        setCasteTemplates(List.of(queen, minor, worker, male));
    }
}
