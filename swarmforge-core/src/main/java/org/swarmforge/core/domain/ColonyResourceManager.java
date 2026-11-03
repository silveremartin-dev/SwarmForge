/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.swarmforge.core.species.Species;

/**
 * Manages resource stores, biomass calculation, consumption, and inter-colony transfers.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ColonyResourceManager implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<ResourceType, Float> resources = new ConcurrentHashMap<>();
    private float waterStored;
    private float proteinStored;
    private float carbohydrateStored;

    public ColonyResourceManager() {
    }

    public float getWaterStored() {
        return waterStored;
    }

    public void setWaterStored(float waterStored) {
        this.waterStored = waterStored;
    }

    public float getProteinStored() {
        return proteinStored;
    }

    public void setProteinStored(float proteinStored) {
        this.proteinStored = proteinStored;
    }

    public void addProtein(float v) {
        this.proteinStored += v;
    }

    public float getCarbohydrateStored() {
        return carbohydrateStored;
    }

    public void setCarbohydrateStored(float carbohydrateStored) {
        this.carbohydrateStored = carbohydrateStored;
    }

    public void addCarbohydrate(float v) {
        this.carbohydrateStored += v;
    }

    public float getSugarStored() {
        return getCarbohydrateStored();
    }

    public void setSugarStored(float v) {
        setCarbohydrateStored(v);
    }

    public float getResourceAmount(ResourceType type) {
        return resources.getOrDefault(type, 0f);
    }

    public void setResourceAmount(ResourceType type, float amount) {
        resources.put(type, amount);
    }

    public Map<ResourceType, Float> getResourcesMap() {
        return resources;
    }

    public void addResource(ResourceType type, float amount) {
        resources.merge(type, amount, Float::sum);
        if (type != null) {
            if (type == ResourceType.PROTEIN || type == ResourceType.INSECT) {
                proteinStored += amount;
            } else if (type == ResourceType.CARBOHYDRATE || type == ResourceType.SUGAR || type == ResourceType.NECTAR || type == ResourceType.HONEYDEW) {
                carbohydrateStored += amount;
            } else if (type == ResourceType.WATER) {
                waterStored += amount;
            }
        }
    }

    public float consumeResource(ResourceType type, float amount) {
        float current = getResourceAmount(type);
        float toTake = Math.min(current, amount);
        if (toTake > 0) {
            resources.put(type, current - toTake);
            if (type != null) {
                if (type == ResourceType.PROTEIN || type == ResourceType.INSECT) {
                    proteinStored = Math.max(0f, proteinStored - toTake);
                } else if (type == ResourceType.CARBOHYDRATE || type == ResourceType.SUGAR || type == ResourceType.NECTAR || type == ResourceType.HONEYDEW) {
                    carbohydrateStored = Math.max(0f, carbohydrateStored - toTake);
                } else if (type == ResourceType.WATER) {
                    waterStored = Math.max(0f, waterStored - toTake);
                }
            }
        }
        return toTake;
    }

    public float getFoodStored() {
        return (float) resources.entrySet().stream()
                .filter(e -> e.getKey() != ResourceType.WATER)
                .mapToDouble(Map.Entry::getValue)
                .sum();
    }

    public float consumeFood(float amount) {
        if (amount <= 0) return 0f;
        float remaining = amount;
        ResourceType[] foodTypes = {
                ResourceType.SUGAR, ResourceType.CARBOHYDRATE, ResourceType.SEED,
                ResourceType.NECTAR, ResourceType.PROTEIN, ResourceType.INSECT,
                ResourceType.FUNGUS, ResourceType.HONEYDEW
        };
        for (ResourceType type : foodTypes) {
            if (remaining <= 0) break;
            float available = getResourceAmount(type);
            if (available > 0) {
                float taken = consumeResource(type, Math.min(available, remaining));
                remaining -= taken;
            }
        }
        return amount - remaining;
    }

    public void setFoodStored(float food) {
        float f = Math.max(0, food);
        for (ResourceType type : ResourceType.values()) {
            if (type != ResourceType.WATER) {
                resources.put(type, 0f);
            }
        }
        carbohydrateStored = f * 0.5f;
        proteinStored = f * 0.5f;
        setResourceAmount(ResourceType.SEED, f * 0.5f);
        setResourceAmount(ResourceType.SUGAR, f * 0.5f);
    }

    public void bootstrapDefaultResources(Species species, String speciesName, int population) {
        int pop = Math.max(20, population);
        float baseAmount = Math.max(1000.0f, pop * 20.0f);

        this.waterStored = baseAmount;
        this.proteinStored = baseAmount * 0.75f;
        this.carbohydrateStored = baseAmount * 0.75f;

        addResource(ResourceType.SEED, baseAmount * 0.4f);
        addResource(ResourceType.SUGAR, baseAmount * 0.4f);
        addResource(ResourceType.NECTAR, baseAmount * 0.3f);
        addResource(ResourceType.PROTEIN, baseAmount * 0.35f);
        addResource(ResourceType.INSECT, baseAmount * 0.35f);
        addResource(ResourceType.WATER, baseAmount);

        if ((species != null && ("FUNGUS".equalsIgnoreCase(species.getPrimaryDiet()) || "Atta".equalsIgnoreCase(species.getGenus())))
                || (speciesName != null && speciesName.contains("Atta"))) {
            float fungusAmount = Math.max(400.0f, pop * 10.0f);
            addResource(ResourceType.FUNGUS, fungusAmount);
            addResource(ResourceType.LEAF, 300.0f);
            addResource(ResourceType.MULCH, 200.0f);
        }
    }

    public boolean sendResource(Colony sender, Colony target, ResourceType type, float amount) {
        if (sender == null || target == null || type == null || amount <= 0) {
            return false;
        }
        float current = getResourceAmount(type);
        if (current < amount) {
            return false;
        }
        consumeResource(type, amount);
        target.addResource(type, amount);
        return true;
    }

    public float calculateTotalBiomass(int individualCount) {
        float total = getFoodStored() + waterStored + getResourceAmount(ResourceType.PROTEIN)
                + getResourceAmount(ResourceType.CARBOHYDRATE);
        total += individualCount * 10.0f; // 10mg per individual average
        return total;
    }
}
