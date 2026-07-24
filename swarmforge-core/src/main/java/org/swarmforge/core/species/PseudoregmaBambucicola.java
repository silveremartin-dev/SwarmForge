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
 * Pseudoregma bambucicola - Social Bamboo Aphid
 * Subsocial/Eusocial aphid producing 1st instar sterile horned soldiers for gall and colony defense.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class PseudoregmaBambucicola extends CustomSpecies {

    public PseudoregmaBambucicola() {
        setPresetName("Puceron du Bambou (Pseudoregma bambucicola)");
        setCommonName("Social Bamboo Aphid");
        setScientificName("Pseudoregma bambucicola");
        setInsectType("APHID");
        setDescription("Puceron gallicole produisant une caste de soldats 1er stade stériles dotés de cornes frontales pour percer les prédateurs.");
        setQueenCountMode("PARTHENOGENETIC_COLONY");
        setQueenCount(1);
        setQueenLifespan(180);
        setQueenEggLayingRate(80.0f);
        setWorkerLifespan(60 * 24);
        setWorkerSpeed(0.25f);
        setViewDistance(2.0f);
        setWorkersCanFly(false);
        setTypicalColonySize(5000);
        setPrimaryDiet("PLANT_SAP");
        setSecondaryDiet("SUGARS_NECTAR");
        setNestType("PLANT_GALL");
        setVenomType("FRONTAL_HORNS");
        setAggression(0.6f);

        CasteTemplate matriarch = new CasteTemplate("Matriarche Parthénogénétique", 200f, 1f);
        matriarch.setBodyLengthMm(3.5f);
        matriarch.setHeadWidthMm(1.0f);

        CasteTemplate worker = new CasteTemplate("Puceron Succeur de Sève", 30f, 0f);
        worker.setLifespan(45 * 24);
        worker.setBodyLengthMm(2.2f);
        worker.setHeadWidthMm(0.7f);

        CasteTemplate soldier = new CasteTemplate("Soldat à Cornes Frontales (Stérile)", 150f, 25f);
        soldier.setBaseDefense(4f);
        soldier.setBodyLengthMm(1.6f);
        soldier.setHeadWidthMm(0.9f);

        setCasteTemplates(List.of(matriarch, worker, soldier));
    }

    @Override
    public InsectOrder getInsectOrder() {
        return InsectOrder.APHID;
    }
}
