/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

/**
 * Botanical Tree & Plant Species for realistic ecological simulation.
 * Models physical traits, foliage yield, extrafloral nectar exudation, resin production,
 * and trophic mutualisms with insect colonies (Atta leafcutters, Cinara aphids, Pseudomyrmex mutualists, Camponotus).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public enum TreeSpecies {

    OAK("Oak Tree (Quercus robur)", "Fagaceae", 0.85f, 0.90f, 0.40f, 0.60f, 0.70f, 0.95f),
    PINE("Scots Pine (Pinus sylvestris)", "Pinaceae", 0.40f, 0.30f, 0.95f, 0.90f, 0.85f, 0.40f),
    ACACIA("Thorn Acacia (Vachellia)", "Fabaceae", 0.60f, 0.95f, 0.20f, 0.30f, 0.50f, 1.00f),
    BIRCH("Silver Birch (Betula pendula)", "Betulaceae", 0.75f, 0.60f, 0.50f, 0.75f, 0.60f, 0.80f),
    BAMBOO("Giant Bamboo (Phyllostachys)", "Poaceae", 0.30f, 0.20f, 0.10f, 0.20f, 0.90f, 0.30f),
    CACTUS("Saguaro / Prickly Pear Cactus (Opuntia)", "Cactaceae", 0.05f, 0.85f, 0.10f, 0.15f, 0.95f, 0.40f),
    DEAD_LOG("Decaying Stump / Dead Trunk", "Cellulose Decay", 0.10f, 0.05f, 0.15f, 0.40f, 1.00f, 0.10f);

    private final String displayName;
    private final String family;
    private final float leafBiomassDensity;         // Leafcutters (Atta) suitability
    private final float extrafloralNectarYield;    // Sweet EFN secretions yield
    private final float resinPropolisYield;       // Bee & wasp propolis/resin harvest
    private final float aphidSuitability;          // Trophic mutualism with aphids (Cinara, Aphis)
    private final float woodCavityNestSuitability; // Carpenter ants (Camponotus), termites
    private final float preySupportIndex;          // Support index for prey caterpillars/beetles

    TreeSpecies(String displayName, String family, float leafBiomassDensity, float extrafloralNectarYield,
                float resinPropolisYield, float aphidSuitability, float woodCavityNestSuitability, float preySupportIndex) {
        this.displayName = displayName;
        this.family = family;
        this.leafBiomassDensity = leafBiomassDensity;
        this.extrafloralNectarYield = extrafloralNectarYield;
        this.resinPropolisYield = resinPropolisYield;
        this.aphidSuitability = aphidSuitability;
        this.woodCavityNestSuitability = woodCavityNestSuitability;
        this.preySupportIndex = preySupportIndex;
    }

    public String getDisplayName() { return displayName; }
    public String getFamily() { return family; }
    public float getLeafBiomassDensity() { return leafBiomassDensity; }
    public float getExtrafloralNectarYield() { return extrafloralNectarYield; }
    public float getResinPropolisYield() { return resinPropolisYield; }
    public float getAphidSuitability() { return aphidSuitability; }
    public float getWoodCavityNestSuitability() { return woodCavityNestSuitability; }
    public float getPreySupportIndex() { return preySupportIndex; }
}
