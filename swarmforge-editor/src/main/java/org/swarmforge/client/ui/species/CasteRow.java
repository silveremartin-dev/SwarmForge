/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.species;

/**
 * Data model for a single caste row in the Species Editor caste summary table
 * and fine-grained inspector.
 */
public class CasteRow {
    private String name;
    private double bodyLengthMm;
    private double headWidthMm;
    private int lifespan;
    private float health;
    private float damage;
    private float defense = 0.0f;
    private boolean canFly;

    // Task Weights & Caste Ratios
    private float foragingWeight = 0.30f;
    private float defenseWeight = 0.20f;
    private float excavationWeight = 0.20f;
    private float nursingWeight = 0.15f;
    private float queenCareWeight = 0.10f;
    private float sanitationWeight = 0.05f;
    private float targetRatio = 0.25f;

    // Cognitive Model Architecture
    private String decisionArch = "BDI"; // BDI, NEURAL_NETWORK, FSM, BEHAVIOR_TREE, FUZZY_LOGIC

    // Venom Systems
    private String venomType = "NONE";
    private float venomToxicity = 10.0f;
    private float venomRangeMm = 2.0f;

    // Motor & Biomechanical Caste Traits
    private float wingbeatFrequencyHz = 0.0f;
    private boolean hasHoveringCapability = false;
    private float maxPayloadRatio = 5.0f;
    private float bitingForceMpa = 15.0f;
    private boolean hasAutothysis = false;
    private boolean hasAroliaAdhesion = true;

    private float walkSpeedMps = 0.5f;
    private float flySpeedMps = 0.0f;

    public CasteRow(String name, double bodyLengthMm, double headWidthMm, int lifespan, float health, float damage, boolean canFly) {
        this.name = name;
        this.bodyLengthMm = bodyLengthMm;
        this.headWidthMm = headWidthMm;
        this.lifespan = lifespan;
        this.health = health;
        this.damage = damage;
        this.canFly = canFly;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getBodyLengthMm() { return bodyLengthMm; }
    public void setBodyLengthMm(double bodyLengthMm) { this.bodyLengthMm = bodyLengthMm; }

    public double getHeadWidthMm() { return headWidthMm; }
    public void setHeadWidthMm(double headWidthMm) { this.headWidthMm = headWidthMm; }

    public double getMinTunnelMm() { return Math.max(1.0, Math.round(headWidthMm * 1.4 * 10.0) / 10.0); }

    public int getLifespan() { return lifespan; }
    public void setLifespan(int lifespan) { this.lifespan = lifespan; }

    public float getHealth() { return health; }
    public void setHealth(float health) { this.health = health; }

    public float getDamage() { return damage; }
    public void setDamage(float damage) { this.damage = damage; }

    public float getDefense() { return defense; }
    public void setDefense(float defense) { this.defense = defense; }

    public boolean isCanFly() { return canFly; }
    public void setCanFly(boolean canFly) { this.canFly = canFly; }

    public float getWalkSpeedMps() { return walkSpeedMps; }
    public void setWalkSpeedMps(float walkSpeedMps) { this.walkSpeedMps = walkSpeedMps; }

    public float getFlySpeedMps() { return flySpeedMps; }
    public void setFlySpeedMps(float flySpeedMps) { this.flySpeedMps = flySpeedMps; }

    public float getForagingWeight() { return foragingWeight; }
    public void setForagingWeight(float foragingWeight) { this.foragingWeight = foragingWeight; }

    public float getDefenseWeight() { return defenseWeight; }
    public void setDefenseWeight(float defenseWeight) { this.defenseWeight = defenseWeight; }

    public float getExcavationWeight() { return excavationWeight; }
    public void setExcavationWeight(float excavationWeight) { this.excavationWeight = excavationWeight; }

    public float getNursingWeight() { return nursingWeight; }
    public void setNursingWeight(float nursingWeight) { this.nursingWeight = nursingWeight; }

    public float getQueenCareWeight() { return queenCareWeight; }
    public void setQueenCareWeight(float queenCareWeight) { this.queenCareWeight = queenCareWeight; }

    public float getSanitationWeight() { return sanitationWeight; }
    public void setSanitationWeight(float sanitationWeight) { this.sanitationWeight = sanitationWeight; }

    public float getTargetRatio() { return targetRatio; }
    public void setTargetRatio(float targetRatio) { this.targetRatio = targetRatio; }

    public String getDecisionArch() { return decisionArch; }
    public void setDecisionArch(String decisionArch) { this.decisionArch = decisionArch; }

    public String getVenomType() { return venomType; }
    public void setVenomType(String venomType) { this.venomType = venomType; }

    public float getVenomToxicity() { return venomToxicity; }
    public void setVenomToxicity(float venomToxicity) { this.venomToxicity = venomToxicity; }

    public float getVenomRangeMm() { return venomRangeMm; }
    public void setVenomRangeMm(float venomRangeMm) { this.venomRangeMm = venomRangeMm; }

    public float getWingbeatFrequencyHz() { return wingbeatFrequencyHz; }
    public void setWingbeatFrequencyHz(float wingbeatFrequencyHz) { this.wingbeatFrequencyHz = wingbeatFrequencyHz; }

    public boolean isHasHoveringCapability() { return hasHoveringCapability; }
    public void setHasHoveringCapability(boolean hasHoveringCapability) { this.hasHoveringCapability = hasHoveringCapability; }

    public float getMaxPayloadRatio() { return maxPayloadRatio; }
    public void setMaxPayloadRatio(float maxPayloadRatio) { this.maxPayloadRatio = maxPayloadRatio; }

    public float getBitingForceMpa() { return bitingForceMpa; }
    public void setBitingForceMpa(float bitingForceMpa) { this.bitingForceMpa = bitingForceMpa; }

    public boolean isHasAutothysis() { return hasAutothysis; }
    public void setHasAutothysis(boolean hasAutothysis) { this.hasAutothysis = hasAutothysis; }

    public boolean isHasAroliaAdhesion() { return hasAroliaAdhesion; }
    public void setHasAroliaAdhesion(boolean hasAroliaAdhesion) { this.hasAroliaAdhesion = hasAroliaAdhesion; }
}
