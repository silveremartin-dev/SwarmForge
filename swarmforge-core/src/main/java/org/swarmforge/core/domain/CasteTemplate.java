/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * Data-driven template for defining custom ant castes.
 * Replaces hardcoded enums for greater flexibility.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class CasteTemplate implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String description;

    // Stats
    private float baseHealth;
    private float baseDamage;
    private float baseDefense;
    private float baseSpeed;

    // Life
    private int lifespan; // ticks

    // Cost
    private float waterCost; // New Resource Management
    private float proteinCost; // New Resource Management
    private float carbohydrateCost; // New Resource Management

    // Capabilities
    private boolean canFly;
    private boolean canDig;
    private boolean canCarry;
    private Map<String, Float> attributes = new HashMap<>();

    public CasteTemplate() {
        // Default constructor for serialization
    }

    public CasteTemplate(String name, float health, float damage) {
        this.name = name;
        this.baseHealth = health;
        this.baseDamage = damage;
        this.baseSpeed = 1.0f;
    }

    // Getters and Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getBaseHealth() {
        return baseHealth;
    }

    public void setBaseHealth(float baseHealth) {
        this.baseHealth = baseHealth;
    }

    public float getBaseDamage() {
        return baseDamage;
    }

    public void setBaseDamage(float baseDamage) {
        this.baseDamage = baseDamage;
    }

    public float getBaseDefense() {
        return baseDefense;
    }

    public void setBaseDefense(float baseDefense) {
        this.baseDefense = baseDefense;
    }

    public float getBaseSpeed() {
        return baseSpeed;
    }

    public void setBaseSpeed(float baseSpeed) {
        this.baseSpeed = baseSpeed;
    }

    public int getLifespan() {
        return lifespan;
    }

    public void setLifespan(int lifespan) {
        this.lifespan = lifespan;
    }

    public float getWaterCost() {
        return waterCost;
    }

    public void setWaterCost(float waterCost) {
        this.waterCost = waterCost;
    }

    public float getProteinCost() {
        return proteinCost;
    }

    public void setProteinCost(float proteinCost) {
        this.proteinCost = proteinCost;
    }

    public float getCarbohydrateCost() {
        return carbohydrateCost;
    }

    public void setCarbohydrateCost(float carbohydrateCost) {
        this.carbohydrateCost = carbohydrateCost;
    }

    public boolean isCanFly() {
        return canFly;
    }

    public void setCanFly(boolean canFly) {
        this.canFly = canFly;
    }

    public boolean isCanDig() {
        return canDig;
    }

    public void setCanDig(boolean canDig) {
        this.canDig = canDig;
    }

    public boolean isCanCarry() {
        return canCarry;
    }

    public void setCanCarry(boolean canCarry) {
        this.canCarry = canCarry;
    }

    public void setAttribute(String key, float value) {
        attributes.put(key, value);
    }

    public float getAttribute(String key, float defaultValue) {
        return attributes.getOrDefault(key, defaultValue);
    }
}
