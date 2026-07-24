/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.event.SimulationEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages territory control and inter-colony warfare.
 * Tracks territory claims, border conflicts, and raid outcomes.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class TerritoryManager {

    /**
     * Territory cell ownership.
     */
    public record TerritoryCell(int x, int y, UUID ownerColonyId, float controlStrength) {
    }

    /**
     * Conflict event between colonies.
     */
    public record ConflictEvent(
            UUID attackerColony,
            UUID defenderColony,
            int attackerCasualties,
            int defenderCasualties,
            boolean attackerWon,
            long tick) {
    }

    private final Simulation simulation;
    private final Map<Long, TerritoryCell> territoryGrid = new ConcurrentHashMap<>();
    private final List<ConflictEvent> conflictHistory = new ArrayList<>();
    private final Random random = new Random();

    // Grid settings
    private int cellSize = 5; // Territory cells are 5x5 world units

    // War settings
    private float aggressionRange = 15f;
    private float raidChance = 0.001f;
    private boolean warEnabled = true;

    // Statistics
    private int totalConflicts = 0;
    private int totalCasualties = 0;

    public TerritoryManager(Simulation simulation) {
        this.simulation = simulation;
    }

    /**
     * Update territory and check for conflicts.
     */
    public void tick() {
        if (!warEnabled)
            return;

        updateTerritories();
        checkBorderConflicts();
    }

    /**
     * Update territory control based on ant positions.
     */
    private void updateTerritories() {
        Map<Long, Map<UUID, Integer>> cellCounts = new HashMap<>();

        // Count ants per colony per cell
        for (Colony colony : simulation.getColonies()) {
            for (Individual ant : colony.getLivingIndividuals()) {
                if (ant.getCaste() == Individual.Caste.WORKER ||
                        ant.getCaste() == Individual.Caste.SOLDIER) {

                    int cellX = (int) (ant.getX() / cellSize);
                    int cellY = (int) (ant.getY() / cellSize);
                    long cellKey = packCoords(cellX, cellY);

                    cellCounts.computeIfAbsent(cellKey, k -> new HashMap<>())
                            .merge(colony.getId(), 1, (a, b) -> a + b);
                }
            }
        }

        // Determine territory control
        for (var entry : cellCounts.entrySet()) {
            long cellKey = entry.getKey();
            Map<UUID, Integer> counts = entry.getValue();

            // Find dominant colony
            UUID dominant = null;
            int maxCount = 0;
            int totalCount = 0;

            for (var colonyEntry : counts.entrySet()) {
                totalCount += colonyEntry.getValue();
                if (colonyEntry.getValue() > maxCount) {
                    maxCount = colonyEntry.getValue();
                    dominant = colonyEntry.getKey();
                }
            }

            if (dominant != null && maxCount > 0) {
                float strength = (float) maxCount / Math.max(1, totalCount);
                int[] coords = unpackCoords(cellKey);
                territoryGrid.put(cellKey, new TerritoryCell(coords[0], coords[1], dominant, strength));
            }
        }
    }

    /**
     * Check for conflicts at territory borders.
     */
    private void checkBorderConflicts() {
        List<Colony> colonies = simulation.getColonies();

        for (int i = 0; i < colonies.size(); i++) {
            for (int j = i + 1; j < colonies.size(); j++) {
                Colony colony1 = colonies.get(i);
                Colony colony2 = colonies.get(j);

                // Check if colonies are near each other
                float[] conflictPos = findConflictPosition(colony1, colony2);
                if (conflictPos != null) {
                    if (random.nextFloat() < raidChance) {
                        resolveConflict(colony1, colony2, conflictPos[0], conflictPos[1]);
                    }
                }
            }
        }
    }

    /**
     * Check if two colonies have ants within aggression range and return position.
     * Returns float[]{x, y} if conflict, null otherwise.
     */
    private float[] findConflictPosition(Colony c1, Colony c2) {
        for (Individual a1 : c1.getLivingIndividuals()) {
            if (a1.getCaste() != Individual.Caste.SOLDIER)
                continue;

            for (Individual a2 : c2.getLivingIndividuals()) {
                float dx = a1.getX() - a2.getX();
                float dy = a1.getY() - a2.getY();
                if (dx * dx + dy * dy < aggressionRange * aggressionRange) {
                    return new float[] { a1.getX(), a1.getY() };
                }
            }
        }
        return null;
    }

    /**
     * Resolve a conflict between two colonies.
     */
    private void resolveConflict(Colony attacker, Colony defender, float conflictX, float conflictY) {
        List<Individual> attackerSoldiers = attacker.getLivingIndividuals().stream()
                .filter(i -> i.getCaste() == Individual.Caste.SOLDIER)
                .toList();
        List<Individual> defenderSoldiers = defender.getLivingIndividuals().stream()
                .filter(i -> i.getCaste() == Individual.Caste.SOLDIER)
                .toList();

        if (attackerSoldiers.isEmpty() || defenderSoldiers.isEmpty())
            return;

        // Simple combat: compare soldier counts with some randomness
        float attackerStrength = attackerSoldiers.size() * (0.8f + random.nextFloat() * 0.4f);
        float defenderStrength = defenderSoldiers.size() * (0.8f + random.nextFloat() * 0.4f);

        // Home advantage
        defenderStrength *= 1.2f;

        boolean attackerWon = attackerStrength > defenderStrength;

        // Calculate casualties
        int attackerCasualties = Math.min(attackerSoldiers.size(),
                (int) (attackerSoldiers.size() * (defenderStrength / attackerStrength) * 0.3f));
        int defenderCasualties = Math.min(defenderSoldiers.size(),
                (int) (defenderSoldiers.size() * (attackerStrength / defenderStrength) * 0.3f));

        // Apply casualties
        for (int i = 0; i < attackerCasualties && i < attackerSoldiers.size(); i++) {
            attackerSoldiers.get(i).setHealth(0);
        }
        for (int i = 0; i < defenderCasualties && i < defenderSoldiers.size(); i++) {
            defenderSoldiers.get(i).setHealth(0);
        }

        // Nest Raid Logic
        boolean nestRaid = false;
        if (attackerWon) {
            float distToNest = (float) Math.sqrt(
                    Math.pow(conflictX - defender.getNestX(), 2) +
                            Math.pow(conflictY - defender.getNestY(), 2));

            if (distToNest < 20.0f) { // Close to nest
                nestRaid = true;
                // Steal Food
                float stolenFood = Math.min(defender.getFoodStored(), 50f);
                defender.setFoodStored(defender.getFoodStored() - stolenFood);
                attacker.setFoodStored(attacker.getFoodStored() + stolenFood);

                // Attack Queen (Regicide)
                if (random.nextFloat() < 0.1f) { // 10% chance if reached nest
                    defender.getLivingIndividuals().stream()
                            .filter(i -> i.getCaste() == Individual.Caste.QUEEN)
                            .findFirst()
                            .ifPresent(queen -> {
                                queen.takeDamage(1000f); // Assassinate
                                simulation.queueEvent(new SimulationEvent(
                                        SimulationEvent.EventType.DEATH,
                                        simulation.getTickCount(),
                                        "QUEEN SLAIN! Colony " + defender.getSpeciesName() + " has fallen!"));
                            });
                }
            }
        }

        // Record conflict
        ConflictEvent event = new ConflictEvent(
                attacker.getId(), defender.getId(),
                attackerCasualties, defenderCasualties,
                attackerWon, simulation.getTickCount());
        conflictHistory.add(event);
        totalConflicts++;
        totalCasualties += attackerCasualties + defenderCasualties;

        // Notify
        String msg = nestRaid ? "Nest Raid! " : "Skirmish: ";
        simulation.queueEvent(new SimulationEvent(
                SimulationEvent.EventType.DEATH,
                simulation.getTickCount(),
                msg + attackerCasualties + " vs " + defenderCasualties + " casualties"));
    }

    // === Utility Methods ===

    private long packCoords(int x, int y) {
        return ((long) x << 32) | (y & 0xFFFFFFFFL);
    }

    private int[] unpackCoords(long packed) {
        return new int[] { (int) (packed >> 32), (int) packed };
    }

    // === Getters ===

    public UUID getTerritoryOwner(float worldX, float worldY) {
        int cellX = (int) (worldX / cellSize);
        int cellY = (int) (worldY / cellSize);
        TerritoryCell cell = territoryGrid.get(packCoords(cellX, cellY));
        return cell != null ? cell.ownerColonyId() : null;
    }

    public float getTerritoryStrength(float worldX, float worldY) {
        int cellX = (int) (worldX / cellSize);
        int cellY = (int) (worldY / cellSize);
        TerritoryCell cell = territoryGrid.get(packCoords(cellX, cellY));
        return cell != null ? cell.controlStrength() : 0f;
    }

    public int getTotalConflicts() {
        return totalConflicts;
    }

    public int getTotalCasualties() {
        return totalCasualties;
    }

    public List<ConflictEvent> getConflictHistory() {
        return List.copyOf(conflictHistory);
    }

    public int getTerritoryCountForColony(UUID colonyId) {
        return (int) territoryGrid.values().stream()
                .filter(c -> c.ownerColonyId().equals(colonyId))
                .count();
    }

    // === Configuration ===

    public void setWarEnabled(boolean enabled) {
        this.warEnabled = enabled;
    }

    public void setAggressionRange(float range) {
        this.aggressionRange = range;
    }

    public void setRaidChance(float chance) {
        this.raidChance = chance;
    }

    public void setCellSize(int size) {
        this.cellSize = size;
    }
}
