/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.species;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A user-definable ant species that can be serialized/deserialized.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomSpecies implements Species {

    private String scientificName = "Formica genericus";
    private String commonName = "Generic Ant";
    private int workerLifespan = 5000;
    private int queenLifespan = 25000;
    private float workerSpeed = 0.5f;
    private float viewDistance = 5.0f;
    private int typicalColonySize = 1000;
    private boolean formsMegaColonies = false;
    private float aggression = 0.3f;
    private float metabolism = 1.0f;
    private float strength = 5.0f;

    // Default constructor for Jackson
    public CustomSpecies() {
    }

    @Override
    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    @Override
    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    @Override
    public int getWorkerLifespan() {
        return workerLifespan;
    }

    public void setWorkerLifespan(int workerLifespan) {
        this.workerLifespan = workerLifespan;
    }

    @Override
    public int getQueenLifespan() {
        return queenLifespan;
    }

    public void setQueenLifespan(int queenLifespan) {
        this.queenLifespan = queenLifespan;
    }

    @Override
    public float getWorkerSpeed() {
        return workerSpeed;
    }

    public void setWorkerSpeed(float workerSpeed) {
        this.workerSpeed = workerSpeed;
    }

    @Override
    public float getViewDistance() {
        return viewDistance;
    }

    public void setViewDistance(float viewDistance) {
        this.viewDistance = viewDistance;
    }

    @Override
    public int getTypicalColonySize() {
        return typicalColonySize;
    }

    public void setTypicalColonySize(int typicalColonySize) {
        this.typicalColonySize = typicalColonySize;
    }

    @Override
    public boolean formsMegaColonies() {
        return formsMegaColonies;
    }

    public void setFormsMegaColonies(boolean formsMegaColonies) {
        this.formsMegaColonies = formsMegaColonies;
    }

    @Override
    public float getAggression() {
        return aggression;
    }

    public void setAggression(float aggression) {
        this.aggression = aggression;
    }

    @Override
    public float getMetabolism() {
        return metabolism;
    }

    public void setMetabolism(float metabolism) {
        this.metabolism = metabolism;
    }

    @Override
    public float getStrength() {
        return strength;
    }

    public void setStrength(float strength) {
        this.strength = strength;
    }
}
